"""设备运维工单离线分析。

默认从 MySQL 读取脱敏工单；也可用 --csv 对导出的样例数据复现。
输出 Excel 明细/指标、月度趋势图和故障帕累托图。所有指标只基于输入数据计算。
"""
from __future__ import annotations

import argparse
import os
from pathlib import Path
from typing import Final

import matplotlib.pyplot as plt
import pandas as pd
from sqlalchemy import create_engine

REQUIRED: Final = {
    "order_no", "fault_type", "priority", "status", "assignee_name",
    "created_at", "accepted_at", "closed_at", "sla_deadline", "repair_cost",
}


def load_data(csv_path: str | None) -> pd.DataFrame:
    if csv_path:
        return pd.read_csv(csv_path)
    url = os.getenv(
        "ANALYSIS_DB_URL",
        "mysql+pymysql://analysis_reader:analysis123@localhost:3306/maintenance?charset=utf8mb4",
    )
    sql = """
        SELECT w.order_no, w.fault_type, w.priority, w.status,
               COALESCE(u.display_name, '未派单') AS assignee_name,
               w.created_at, w.accepted_at, w.closed_at, w.sla_deadline, w.repair_cost
        FROM work_order w
        LEFT JOIN sys_user u ON u.id = w.assignee_id
        WHERE w.deleted = 0
    """
    return pd.read_sql(sql, create_engine(url))


def clean(df: pd.DataFrame) -> pd.DataFrame:
    missing = REQUIRED - set(df.columns)
    if missing:
        raise ValueError(f"缺少必需字段: {', '.join(sorted(missing))}")
    result = df.copy()
    result = result.drop_duplicates(subset=["order_no"], keep="last")
    for column in ["created_at", "accepted_at", "closed_at", "sla_deadline"]:
        result[column] = pd.to_datetime(result[column], errors="coerce")
    result = result.dropna(subset=["order_no", "fault_type", "created_at", "sla_deadline"])
    result["repair_cost"] = pd.to_numeric(result["repair_cost"], errors="coerce").fillna(0).clip(lower=0)
    result["first_response_hours"] = (
        (result["accepted_at"] - result["created_at"]).dt.total_seconds() / 3600
    ).clip(lower=0)
    result["repair_hours"] = (
        (result["closed_at"] - result["created_at"]).dt.total_seconds() / 3600
    ).clip(lower=0)
    result["sla_met"] = result["closed_at"].notna() & (result["closed_at"] <= result["sla_deadline"])
    result["month"] = result["created_at"].dt.to_period("M").astype(str)
    return result


def metrics(df: pd.DataFrame) -> dict[str, float | int]:
    closed = df[df["closed_at"].notna()]
    responded = df[df["accepted_at"].notna()]
    return {
        "工单总数": int(len(df)),
        "已关闭数": int(len(closed)),
        "SLA达标率": float(closed["sla_met"].mean()) if len(closed) else 0.0,
        "平均首次响应小时": float(responded["first_response_hours"].mean()) if len(responded) else 0.0,
        "平均修复小时MTTR": float(closed["repair_hours"].mean()) if len(closed) else 0.0,
        "维修费用合计": float(df["repair_cost"].sum()),
    }


def aggregate(df: pd.DataFrame) -> tuple[pd.DataFrame, pd.DataFrame, pd.DataFrame]:
    fault = df.groupby("fault_type", as_index=False).size().rename(columns={"size": "工单数"})
    fault = fault.sort_values("工单数", ascending=False)
    fault["累计占比"] = fault["工单数"].cumsum() / fault["工单数"].sum()
    monthly = df.groupby("month", as_index=False).agg(
        新建工单=("order_no", "count"),
        已关闭工单=("closed_at", lambda x: int(x.notna().sum())),
        平均修复小时=("repair_hours", "mean"),
    )
    workload = df.groupby("assignee_name", as_index=False).agg(
        工单数=("order_no", "count"),
        已关闭数=("closed_at", lambda x: int(x.notna().sum())),
        平均修复小时=("repair_hours", "mean"),
    ).sort_values("工单数", ascending=False)
    return fault, monthly, workload


def write_excel(output: Path, df: pd.DataFrame, kpis: dict, fault: pd.DataFrame,
                monthly: pd.DataFrame, workload: pd.DataFrame) -> None:
    with pd.ExcelWriter(output, engine="openpyxl") as writer:
        pd.DataFrame({"指标": list(kpis), "数值": list(kpis.values())}).to_excel(writer, "指标摘要", index=False)
        fault.to_excel(writer, "故障帕累托", index=False)
        monthly.to_excel(writer, "月度趋势", index=False)
        workload.to_excel(writer, "人员工作量", index=False)
        df.to_excel(writer, "脱敏工单明细", index=False)
        for sheet in writer.book.worksheets:
            sheet.freeze_panes = "A2"
            sheet.auto_filter.ref = sheet.dimensions
            sheet.sheet_view.showGridLines = False
            for cell in sheet[1]:
                cell.font = cell.font.copy(bold=True, color="FFFFFF")
                cell.fill = cell.fill.copy(fill_type="solid", fgColor="164E63")
            for column in sheet.columns:
                values = [str(c.value or "") for c in column[:200]]
                sheet.column_dimensions[column[0].column_letter].width = min(max(max(map(len, values)) + 2, 12), 32)
        writer.book["指标摘要"]["B4"].number_format = "0.0%"
        writer.book["故障帕累托"]["C2"].number_format = "0.0%"
        for row in range(2, writer.book["故障帕累托"].max_row + 1):
            writer.book["故障帕累托"][f"C{row}"].number_format = "0.0%"


def write_charts(output_dir: Path, fault: pd.DataFrame, monthly: pd.DataFrame) -> None:
    plt.rcParams["font.sans-serif"] = ["Microsoft YaHei", "SimHei", "Arial Unicode MS"]
    plt.rcParams["axes.unicode_minus"] = False
    fig, ax1 = plt.subplots(figsize=(10, 5.4))
    top = fault.head(10)
    ax1.bar(top["fault_type"], top["工单数"], color="#167d71")
    ax1.set_ylabel("工单数")
    ax1.tick_params(axis="x", rotation=25)
    ax2 = ax1.twinx()
    ax2.plot(top["fault_type"], top["累计占比"], color="#dc6b3f", marker="o")
    ax2.set_ylim(0, 1.05); ax2.set_ylabel("累计占比"); ax2.yaxis.set_major_formatter(lambda x, _: f"{x:.0%}")
    fig.tight_layout(); fig.savefig(output_dir / "fault_pareto.png", dpi=160); plt.close(fig)

    fig, ax = plt.subplots(figsize=(10, 5.2))
    ax.plot(monthly["month"], monthly["新建工单"], marker="o", label="新建工单")
    ax.plot(monthly["month"], monthly["已关闭工单"], marker="o", label="已关闭工单")
    ax.set_ylabel("工单数"); ax.legend(); ax.grid(alpha=.2); fig.tight_layout()
    fig.savefig(output_dir / "monthly_trend.png", dpi=160); plt.close(fig)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--csv", help="使用CSV代替MySQL数据源")
    parser.add_argument("--output", default="output", help="输出目录")
    args = parser.parse_args()
    output_dir = Path(args.output); output_dir.mkdir(parents=True, exist_ok=True)
    df = clean(load_data(args.csv))
    kpis = metrics(df); fault, monthly, workload = aggregate(df)
    write_excel(output_dir / "设备运维月度分析.xlsx", df, kpis, fault, monthly, workload)
    write_charts(output_dir, fault, monthly)
    print({"rows": len(df), "output": str(output_dir.resolve()), **kpis})


if __name__ == "__main__":
    main()


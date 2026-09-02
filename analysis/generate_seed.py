"""为本地性能验证生成不少于5万条脱敏模拟工单并批量写入MySQL。"""
from __future__ import annotations

import argparse
import os
import random
from datetime import datetime, timedelta

import pymysql

FAULTS = ["机械故障", "电气故障", "压力异常", "温度异常", "网络中断", "传感器故障"]
PRIORITIES = [("LOW", 72), ("MEDIUM", 48), ("HIGH", 24), ("URGENT", 4)]
STATUSES = ["PENDING_ACCEPTANCE", "PENDING_DISPATCH", "IN_REPAIR", "PENDING_INSPECTION", "CLOSED"]


def row(index: int):
    created = datetime(2025, 9, 1) + timedelta(minutes=random.randint(0, 525600))
    priority, sla = random.choice(PRIORITIES)
    status = random.choices(STATUSES, weights=[6, 6, 16, 10, 62])[0]
    accepted = created + timedelta(minutes=random.randint(5, 360)) if status != "PENDING_ACCEPTANCE" else None
    dispatched = accepted + timedelta(minutes=random.randint(5, 240)) if accepted and status not in {"PENDING_DISPATCH"} else None
    closed = created + timedelta(hours=random.randint(1, 120)) if status == "CLOSED" else None
    return (
        f"SIM{created:%Y%m%d}{index:08d}", random.randint(1, 4), f"模拟故障-{index}", random.choice(FAULTS),
        "用于索引和分页性能验证的脱敏模拟记录", priority, status, 2, 3 if accepted else None,
        random.choice([4, 6]) if dispatched else None, 5 if closed else None,
        "模拟维修说明" if closed else None, round(random.uniform(0, 3000), 2) if closed else None,
        accepted, dispatched, closed, closed, created + timedelta(hours=sla), 0, 0, created, closed or dispatched or accepted or created,
    )


def main():
    parser = argparse.ArgumentParser(); parser.add_argument("--rows", type=int, default=50000); parser.add_argument("--batch", type=int, default=1000)
    args = parser.parse_args()
    connection = pymysql.connect(host=os.getenv("MYSQL_HOST", "localhost"), port=int(os.getenv("MYSQL_PORT", "3306")),
        user=os.getenv("MYSQL_USER", "maintenance"), password=os.getenv("MYSQL_PASSWORD", "maintenance123"),
        database=os.getenv("MYSQL_DATABASE", "maintenance"), charset="utf8mb4", autocommit=False)
    sql = """INSERT INTO work_order(order_no,equipment_id,title,fault_type,fault_description,priority,status,reporter_id,
      dispatcher_id,assignee_id,inspector_id,repair_description,repair_cost,accepted_at,dispatched_at,repaired_at,closed_at,
      sla_deadline,version,deleted,created_at,updated_at) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)"""
    try:
        with connection.cursor() as cursor:
            for start in range(0, args.rows, args.batch):
                values = [row(i) for i in range(start, min(start + args.batch, args.rows))]
                cursor.executemany(sql, values); connection.commit(); print(f"inserted {min(start + args.batch, args.rows)}/{args.rows}")
    finally:
        connection.close()


if __name__ == "__main__": main()


package com.lwq.maintenance.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lwq.maintenance.domain.entity.WorkOrder;
import com.lwq.maintenance.mapper.WorkOrderMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final WorkOrderMapper workOrderMapper;

    public byte[] exportWorkOrders() throws IOException {
        List<WorkOrder> orders = workOrderMapper.selectList(Wrappers.<WorkOrder>lambdaQuery()
                .orderByDesc(WorkOrder::getCreatedAt));
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("工单明细");
            String[] headers = {"工单号", "设备ID", "标题", "故障类型", "优先级", "状态", "报修人ID", "维修人ID", "维修费用", "创建时间", "关闭时间"};
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
            int rowIndex = 1;
            for (WorkOrder order : orders) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(order.getOrderNo());
                row.createCell(1).setCellValue(order.getEquipmentId());
                row.createCell(2).setCellValue(order.getTitle());
                row.createCell(3).setCellValue(order.getFaultType());
                row.createCell(4).setCellValue(String.valueOf(order.getPriority()));
                row.createCell(5).setCellValue(String.valueOf(order.getStatus()));
                row.createCell(6).setCellValue(order.getReporterId());
                if (order.getAssigneeId() != null) row.createCell(7).setCellValue(order.getAssigneeId());
                if (order.getRepairCost() != null) row.createCell(8).setCellValue(order.getRepairCost().doubleValue());
                row.createCell(9).setCellValue(String.valueOf(order.getCreatedAt()));
                row.createCell(10).setCellValue(order.getClosedAt() == null ? "" : String.valueOf(order.getClosedAt()));
            }
            workbook.write(out);
            workbook.dispose();
            return out.toByteArray();
        }
    }
}

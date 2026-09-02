# 测试方案与指标口径

## 自动化测试

后端测试覆盖：

- 已受理工单不能重复受理。
- 派单对象必须是启用状态的维修人员。
- 维修人员不能处理他人工单。
- 验收驳回必须返回维修中并写审计日志。
- 乐观锁更新失败时返回409且不写审计日志。
- SLA 与 MTTR 只使用已关闭工单，开放工单不进入分母。
- Spring 上下文真实启动后，管理员可登录并携带 JWT 成功访问看板聚合接口。

执行：`cd backend; mvn test`。

本次验证结果（2026-09-01，Windows、Java 17）：共 7 项测试，0 失败、0 错误；前端 Vite 生产构建通过；Python 指标测试 1 项通过。Docker 未安装，因此 Compose 配置尚未做容器级启动验证。

## 手工验收场景

1. 报修人创建紧急工单，SLA 截止时间应为创建后4小时。
2. 报修人无法查看其他人的工单。
3. 调度员完成受理和派单。
4. 两个请求同时派发同一工单，只有一个成功，另一个返回409。
5. 非被指派维修人员提交结果，返回403。
6. 验收驳回不填写原因，返回400；填写原因后返回维修中。
7. 验收通过后工单关闭、设备恢复运行并生成审计记录。
8. 上传脚本文件或超大文件被拒绝。

## 指标定义

- FRT：`accepted_at - created_at`，只统计已受理工单。
- MTTR：`closed_at - created_at`，只统计已关闭工单。
- SLA 达标率：已关闭且 `closed_at <= sla_deadline` 的数量 / 已关闭工单数。
- 维修人员工作量：按 `assignee_id` 统计工单数量；不等同于绩效评价。

## SQL 性能验证

在本地测试库生成5万条模拟数据后执行：

```sql
EXPLAIN SELECT * FROM work_order
WHERE status='IN_REPAIR'
ORDER BY created_at DESC LIMIT 20;

EXPLAIN SELECT * FROM work_order
WHERE assignee_id=4 AND status='IN_REPAIR'
ORDER BY created_at DESC LIMIT 20;

EXPLAIN SELECT * FROM work_order
WHERE equipment_id=2 AND created_at >= '2026-01-01'
ORDER BY created_at DESC LIMIT 20;
```

记录测试环境、数据量、执行计划、平均耗时和 P95。未经实测不得写入简历。

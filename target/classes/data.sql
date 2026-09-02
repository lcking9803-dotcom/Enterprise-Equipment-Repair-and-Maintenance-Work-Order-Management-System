INSERT IGNORE INTO sys_user(id, username, password, display_name, role_code, department, enabled) VALUES
(1, 'admin', '{noop}Admin@123', '系统管理员', 'ADMIN', '信息中心', TRUE),
(2, 'reporter', '{noop}Reporter@123', '报修人员', 'REPORTER', '生产一部', TRUE),
(3, 'dispatcher', '{noop}Dispatcher@123', '调度人员', 'DISPATCHER', '设备管理部', TRUE),
(4, 'maintainer', '{noop}Maintainer@123', '维修工程师', 'MAINTAINER', '维修班组', TRUE),
(5, 'acceptor', '{noop}Acceptor@123', '验收人员', 'ACCEPTOR', '质量管理部', TRUE),
(6, 'maintainer2', '{noop}Maintainer@123', '维修工程师二', 'MAINTAINER', '维修班组', TRUE);

INSERT IGNORE INTO equipment(id, equipment_code, name, category, location, department, responsible_person, status,
 maintenance_cycle_days, last_maintenance_date, next_maintenance_date, description, version, deleted) VALUES
(1, 'EQ-CNC-001', '数控加工中心A', '生产设备', '一号车间-A区', '生产一部', '张工', 'RUNNING', 90, '2026-06-01', '2026-08-30', '关键生产设备', 0, 0),
(2, 'EQ-AIR-002', '空气压缩机B', '动力设备', '动力站-2号位', '设备管理部', '李工', 'FAULT', 60, '2026-07-10', '2026-09-08', '为生产线提供压缩空气', 0, 0),
(3, 'EQ-UPS-003', '机房UPS主机', '信息设备', '数据机房', '信息中心', '王工', 'RUNNING', 180, '2026-03-15', '2026-09-11', '核心系统后备供电', 0, 0),
(4, 'EQ-HVAC-004', '中央空调冷水机组', '环境设备', '综合楼负一层', '行政保障部', '赵工', 'MAINTENANCE', 120, '2026-05-01', '2026-08-29', '办公区域制冷', 0, 0);

INSERT IGNORE INTO work_order(id, order_no, equipment_id, title, fault_type, fault_description, priority, status,
 reporter_id, dispatcher_id, assignee_id, inspector_id, repair_description, repair_cost, accepted_at, dispatched_at,
 repaired_at, closed_at, sla_deadline, version, deleted, created_at, updated_at) VALUES
(1, 'WO202608280900001', 2, '空压机压力异常', '压力异常', '出口压力低于正常范围并伴随异响', 'HIGH', 'IN_REPAIR',
 2, 3, 4, NULL, NULL, NULL, '2026-08-28 09:20:00', '2026-08-28 09:35:00', NULL, NULL,
 '2026-08-29 09:00:00', 0, 0, '2026-08-28 09:00:00', '2026-08-28 09:35:00'),
(2, 'WO202608201030002', 3, 'UPS电池组告警', '电气故障', '监控提示电池组内阻偏高', 'MEDIUM', 'CLOSED',
 2, 3, 4, 5, '更换两节异常电池并完成放电测试', 680.00, '2026-08-20 10:45:00', '2026-08-20 11:10:00',
 '2026-08-20 16:30:00', '2026-08-20 17:00:00', '2026-08-22 10:30:00', 0, 0,
 '2026-08-20 10:30:00', '2026-08-20 17:00:00');

INSERT IGNORE INTO work_order_log(id, work_order_id, from_status, to_status, action, operator_id, operator_name, remark, created_at) VALUES
(1, 1, NULL, 'PENDING_ACCEPTANCE', 'CREATE', 2, '报修人员', '现场报修', '2026-08-28 09:00:00'),
(2, 1, 'PENDING_ACCEPTANCE', 'PENDING_DISPATCH', 'ACCEPT', 3, '调度人员', '确认属于设备维修范围', '2026-08-28 09:20:00'),
(3, 1, 'PENDING_DISPATCH', 'IN_REPAIR', 'DISPATCH', 3, '调度人员', '安排维修班组处理', '2026-08-28 09:35:00'),
(4, 2, NULL, 'PENDING_ACCEPTANCE', 'CREATE', 2, '报修人员', '监控告警转报修', '2026-08-20 10:30:00'),
(5, 2, 'PENDING_ACCEPTANCE', 'PENDING_DISPATCH', 'ACCEPT', 3, '调度人员', NULL, '2026-08-20 10:45:00'),
(6, 2, 'PENDING_DISPATCH', 'IN_REPAIR', 'DISPATCH', 3, '调度人员', NULL, '2026-08-20 11:10:00'),
(7, 2, 'IN_REPAIR', 'PENDING_INSPECTION', 'SUBMIT_REPAIR', 4, '维修工程师', '放电测试正常', '2026-08-20 16:30:00'),
(8, 2, 'PENDING_INSPECTION', 'CLOSED', 'INSPECTION_PASS', 5, '验收人员', '现场验收通过', '2026-08-20 17:00:00');

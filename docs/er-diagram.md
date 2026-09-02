# ER 图

```mermaid
erDiagram
    SYS_USER ||--o{ WORK_ORDER : reports
    SYS_USER ||--o{ WORK_ORDER : dispatches
    SYS_USER ||--o{ WORK_ORDER : repairs
    SYS_USER ||--o{ WORK_ORDER : inspects
    EQUIPMENT ||--o{ WORK_ORDER : has
    WORK_ORDER ||--o{ WORK_ORDER_LOG : records
    WORK_ORDER ||--o{ ATTACHMENT : owns

    SYS_USER {
      bigint id PK
      varchar username UK
      varchar password
      varchar role_code
      boolean enabled
    }
    EQUIPMENT {
      bigint id PK
      varchar equipment_code UK
      varchar category
      varchar location
      varchar department
      varchar status
      int version
    }
    WORK_ORDER {
      bigint id PK
      varchar order_no UK
      bigint equipment_id FK
      varchar priority
      varchar status
      bigint reporter_id FK
      bigint assignee_id FK
      datetime sla_deadline
      int version
    }
    WORK_ORDER_LOG {
      bigint id PK
      bigint work_order_id FK
      varchar from_status
      varchar to_status
      varchar action
      bigint operator_id
      datetime created_at
    }
    ATTACHMENT {
      bigint id PK
      bigint work_order_id FK
      varchar stage
      varchar object_key UK
      bigint size_bytes
    }
```

## 索引设计

- `work_order(status, created_at)`：状态列表按时间倒序。
- `work_order(equipment_id, created_at)`：设备维修历史。
- `work_order(assignee_id, status)`：维修人员待办。
- `work_order(reporter_id, created_at)`：报修人员的数据范围。
- `work_order(sla_deadline, status)`：超时工单扫描。


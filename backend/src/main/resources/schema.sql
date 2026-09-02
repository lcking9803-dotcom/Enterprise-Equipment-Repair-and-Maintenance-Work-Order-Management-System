CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    display_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(30) NOT NULL,
    department VARCHAR(80),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS equipment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    equipment_code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    location VARCHAR(120) NOT NULL,
    department VARCHAR(80) NOT NULL,
    responsible_person VARCHAR(50),
    status VARCHAR(30) NOT NULL,
    maintenance_cycle_days INT,
    last_maintenance_date DATE,
    next_maintenance_date DATE,
    description VARCHAR(500),
    version INT NOT NULL DEFAULT 0,
    deleted INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS work_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    equipment_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    fault_type VARCHAR(60) NOT NULL,
    fault_description VARCHAR(2000) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    reporter_id BIGINT NOT NULL,
    dispatcher_id BIGINT,
    assignee_id BIGINT,
    inspector_id BIGINT,
    repair_description VARCHAR(2000),
    repair_cost DECIMAL(12,2),
    rejection_reason VARCHAR(500),
    accepted_at DATETIME,
    dispatched_at DATETIME,
    repaired_at DATETIME,
    closed_at DATETIME,
    sla_deadline DATETIME NOT NULL,
    version INT NOT NULL DEFAULT 0,
    deleted INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_work_order_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(id),
    CONSTRAINT fk_work_order_reporter FOREIGN KEY (reporter_id) REFERENCES sys_user(id)
);

CREATE TABLE IF NOT EXISTS work_order_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    work_order_id BIGINT NOT NULL,
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    action VARCHAR(40) NOT NULL,
    operator_id BIGINT NOT NULL,
    operator_name VARCHAR(50) NOT NULL,
    remark VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_work_order FOREIGN KEY (work_order_id) REFERENCES work_order(id)
);

CREATE TABLE IF NOT EXISTS attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    work_order_id BIGINT NOT NULL,
    stage VARCHAR(30) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    object_key VARCHAR(500) NOT NULL UNIQUE,
    content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    uploader_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attachment_work_order FOREIGN KEY (work_order_id) REFERENCES work_order(id)
);

CREATE INDEX idx_equipment_category_status ON equipment(category, status);
CREATE INDEX idx_equipment_department ON equipment(department);
CREATE INDEX idx_order_status_created ON work_order(status, created_at);
CREATE INDEX idx_order_equipment_created ON work_order(equipment_id, created_at);
CREATE INDEX idx_order_assignee_status ON work_order(assignee_id, status);
CREATE INDEX idx_order_reporter_created ON work_order(reporter_id, created_at);
CREATE INDEX idx_order_sla_deadline ON work_order(sla_deadline, status);
CREATE INDEX idx_log_order_created ON work_order_log(work_order_id, created_at);


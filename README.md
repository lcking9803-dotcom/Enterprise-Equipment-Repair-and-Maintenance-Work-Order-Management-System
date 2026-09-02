# 企业设备报修与运维工单管理系统

面向企业设备运维场景的前后端分离单体应用，覆盖设备台账、故障报修、受理派单、维修处理、结果验收、附件留存、审计日志、指标看板和离线分析。

> 本仓库是独立设计的学习项目。示例用户、设备、工单和图表数据均为脱敏模拟数据，不代表真实企业生产规模或性能。

## 1. 项目亮点

- 明确状态机：`待受理 → 待派单 → 维修中 → 待验收 → 已关闭`，验收驳回返回维修中。
- RBAC 与数据范围：报修人只看自己的工单，维修人只看分配给自己的工单。
- 一致性保护：状态更新和审计日志共用事务；`@Version` 乐观锁阻止并发覆盖。
- 可解释指标：FRT、MTTR、SLA 达标率、高频故障和人员工作量均由工单时间戳计算。
- 可交付：Knife4j 接口文档、Docker Compose、Excel 导出、Pandas 分析、自动化测试和演示脚本。

## 2. 技术栈

- 后端：Java 17、Spring Boot 3、Spring Security、JWT、MyBatis-Plus、MySQL 8、Redis 7、MinIO、Knife4j、Apache POI
- 前端：Vue 3、Vue Router、Element Plus、ECharts、Axios、Vite
- 分析：Python、Pandas、Matplotlib、SQLAlchemy、Excel
- 部署：Docker Compose、Nginx

## 3. 目录

```text
enterprise-maintenance/
├─ backend/       Spring Boot API、数据库脚本和测试
├─ frontend/      Vue 3 管理端
├─ analysis/      脱敏工单分析与5万条模拟数据生成器
├─ deploy/        Nginx 配置
├─ docs/          架构、ER、部署、测试、面试和演示材料
└─ docker-compose.yml
```
其中src,target,uploads,Dockerfile,pom.xml都是属于backend文件夹里面的！！！

## 4. 本地启动（适合开发学习）

后端开发环境默认使用 H2 内存数据库和本地附件目录，不要求先安装 MySQL、Redis、MinIO。

### 最简单：双击启动

1. 打开项目根目录 `enterprise-maintenance`。
2. 双击 `启动系统.bat`。
3. 等待窗口显示 `System is ready: http://localhost:5173`，浏览器会自动打开。
4. 使用 `admin / Admin@123` 登录。
5. 使用结束后双击 `停止系统.bat`，不要直接到任务管理器结束不确定的 Java 或 Node 进程。

批处理文件实际会调用 PowerShell；启动后的后端和前端在后台运行，因此看到成功提示后可以关闭启动提示窗口。

### PowerShell 启动

```powershell
.\start-system.ps1
```

停止命令：

```powershell
.\stop-system.ps1
```

如需重新构建后端，可进入 `backend` 手工执行 `mvn package`。直接运行可执行 JAR 的命令为 `java -jar target\enterprise-maintenance-1.0.0.jar`。

访问：

- API：`http://localhost:8080`
- Knife4j：`http://localhost:8080/doc.html`
- H2 控制台：`http://localhost:8080/h2-console`

另开终端：

```powershell
cd frontend
npm install
npm run dev
```

浏览器访问 `http://localhost:5173`。

## 5. 演示账号

| 角色 | 用户名 | 密码 | 核心权限 |
|---|---|---|---|
| 管理员 | admin | Admin@123 | 全部功能 |
| 报修人员 | reporter | Reporter@123 | 新建并查看自己的工单 |
| 调度人员 | dispatcher | Dispatcher@123 | 受理、派单、设备维护 |
| 维修人员 | maintainer | Maintainer@123 | 处理分配给自己的工单 |
| 验收人员 | acceptor | Acceptor@123 | 通过或驳回维修结果 |

这些明文密码仅用于本地演示。生产部署前必须修改初始化数据，并使用 BCrypt 密码。

## 6. Docker 部署

```powershell
Copy-Item .env.example .env
# 修改 .env 中的全部默认口令
docker compose up -d --build
```

访问 `http://localhost:8088`，MinIO 控制台位于 `http://localhost:9001`。详细步骤见 [部署手册](docs/deployment.md)。

## 7. 离线分析

```powershell
cd analysis
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
$env:ANALYSIS_DB_URL='mysql+pymysql://analysis_reader:实际密码@localhost:3306/maintenance?charset=utf8mb4'
python analyze.py --output output
```

输出包括分析工作簿、月度趋势图和故障帕累托图。指标口径见 [测试与指标说明](docs/testing.md)。

生成压力测试数据前请仅在本地测试库执行：

```powershell
python generate_seed.py --rows 50000 --batch 1000
```

## 8. 验证命令

```powershell
cd backend
mvn test

cd ..\frontend
npm run build

cd ..\analysis
python -m unittest test_analyze.py
```

当前仓库已验证后端 7 项测试、前端生产构建和 Python 指标单测通过；Docker 运行仍需在安装 Docker Desktop 的机器上验证。只有记录真实测试环境和结果后，才可把数字写入简历。建议继续阅读：[测试方案](docs/testing.md)、[演示脚本](docs/demo-script.md)、[面试问答](docs/interview.md)、[简历模板](docs/resume.md)。


# Enterprise Equipment Repair & Maintenance Work Order Management System

A frontend-backend separated monolithic application for enterprise equipment maintenance scenarios, covering equipment ledger, fault reporting, acceptance & dispatch, repair processing, result acceptance, attachment retention, audit logs, metric dashboards, and offline analysis.

> 
> This repository is an independently designed learning project. Sample users, equipment, work orders, and chart data are all desensitized simulated data and do not represent real enterprise production scale or performance.

## 1. Project Highlights

- Clear state machine: `Pending Acceptance → Pending Dispatch → Under Repair → Pending Acceptance Check → Closed`; rejected acceptance returns to Under Repair.
- RBAC and data scope: reporters only see their own work orders, maintainers only see work orders assigned to them.
- Consistency protection: status updates and audit logs share the same transaction; `@Version` optimistic locking prevents concurrent overwrites.
- Explainable metrics: FRT, MTTR, SLA compliance rate, high-frequency faults, and personnel workload are all calculated from work order timestamps.
- Deliverable: Knife4j API docs, Docker Compose, Excel export, Pandas analysis, automated tests, and demo scripts.

## 2. Tech Stack

- Backend: Java 17, Spring Boot 3, Spring Security, JWT, MyBatis-Plus, MySQL 8, Redis 7, MinIO, Knife4j, Apache POI
- Frontend: Vue 3, Vue Router, Element Plus, ECharts, Axios, Vite
- Analysis: Python, Pandas, Matplotlib, SQLAlchemy, Excel
- Deployment: Docker Compose, Nginx

## 3. Directory Structure

```
enterprise-maintenance/
├─ backend/       Spring Boot API, database scripts, and tests
├─ frontend/      Vue 3 admin console
├─ analysis/      Desensitized work order analysis and 50k-row mock data generator
├─ deploy/        Nginx configuration
├─ docs/          Architecture, ER, deployment, testing, interview, and demo materials
└─ docker-compose.yml
```

Note: `src`, `target`, `uploads`, `Dockerfile`, and `pom.xml` all belong inside the `backend` folder!

## 4. Local Startup (for development & learning)

The backend development environment uses an H2 in-memory database and a local attachment directory by default — no need to install MySQL, Redis, or MinIO first.

### Easiest: Double-click to start

1. Open the project root directory `enterprise-maintenance`.
2. Double-click `启动系统.bat`.
3. Wait until the window shows `System is ready: http://localhost:5173`; the browser will open automatically.
4. Log in with `admin / Admin@123`.
5. When finished, double-click `停止系统.bat` — do not go to Task Manager and kill uncertain Java or Node processes directly.

The batch files actually call PowerShell; the backend and frontend run in the background after startup, so you can close the startup prompt window once you see the success message.

### PowerShell startup

```
.\start-system.ps1
```

Stop command:

```
.\stop-system.ps1
```

If you need to rebuild the backend, go into `backend` and manually run `mvn package`. The command to run the executable JAR directly is `java -jar target\enterprise-maintenance-1.0.0.jar`.

Access:

- API: `http://localhost:8080`
- Knife4j: `http://localhost:8080/doc.html`
- H2 Console: `http://localhost:8080/h2-console`

In another terminal:

```
cd frontend
npm install
npm run dev
```

Visit `http://localhost:5173` in your browser.

## 5. Demo Accounts

表格

| Role | Username | Password | Core Permissions |
| --- | --- | --- | --- |
| Administrator | admin | Admin@123 | All features |
| Reporter | reporter | Reporter@123 | Create and view own work orders |
| Dispatcher | dispatcher | Dispatcher@123 | Accept, dispatch, equipment maintenance |
| Maintainer | maintainer | Maintainer@123 | Process work orders assigned to self |
| Acceptor | acceptor | Acceptor@123 | Approve or reject repair results |

These plaintext passwords are for local demo only. Before production deployment, you must change the initialization data and use BCrypt passwords.

## 6. Docker Deployment

```
Copy-Item .env.example .env
# Modify all default passwords in .env
docker compose up -d --build
```

Visit `http://localhost:8088`; the MinIO console is at `http://localhost:9001`. See the [Deployment Guide](docs/deployment.md) for detailed steps.

## 7. Offline Analysis

```
cd analysis
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
$env:ANALYSIS_DB_URL='mysql+pymysql://analysis_reader:actual_password@localhost:3306/maintenance?charset=utf8mb4'
python analyze.py --output output
```

Output includes an analysis workbook, monthly trend charts, and fault Pareto charts. See [Testing & Metrics Documentation](docs/testing.md) for metric definitions.

Before generating stress test data, execute only against a local test database:

```
python generate_seed.py --rows 50000 --batch 1000
```

## 8. Verification Commands

```
cd backend
mvn test
cd ..\frontend
npm run build
cd ..\analysis
python -m unittest test_analyze.py
```

The current repository has been verified with 7 backend tests passing, frontend production build passing, and Python metric unit tests passing; Docker runtime still needs to be verified on a machine with Docker Desktop installed. Only write numbers into your resume after recording the real test environment and results. Recommended further reading: [Testing Plan](docs/testing.md), [Demo Script](docs/demo-script.md), [Interview Q&A](docs/interview.md), [Resume Template](docs/resume.md).

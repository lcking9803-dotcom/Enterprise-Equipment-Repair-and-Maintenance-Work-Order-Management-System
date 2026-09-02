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

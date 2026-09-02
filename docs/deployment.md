# 部署手册

## 前置条件

- Docker Engine 24+ 与 Docker Compose v2。
- 至少 4GB 可用内存、10GB 可用磁盘。
- 端口 8088 和 9001 未被占用。

## 首次部署

1. 复制 `.env.example` 为 `.env`。
2. 修改 MySQL、JWT 和 MinIO 的所有默认口令；JWT 密钥至少32个随机字符。
3. 执行 `docker compose config`，确认变量替换和 YAML 正确。
4. 执行 `docker compose up -d --build`。
5. 执行 `docker compose ps`，确认 MySQL、Redis 健康，后端和前端为运行状态。
6. 打开 `http://localhost:8088` 并依次用五种角色完成工单闭环。

## 日志与排障

```powershell
docker compose logs --tail 200 backend
docker compose logs --tail 100 mysql
docker compose logs --tail 100 redis
```

- 后端无法连接 MySQL：检查 `mysql` 健康状态与 `.env` 密码是否一致。
- 上传失败：检查 MinIO 容器、桶创建日志和12MB限制。
- 页面刷新404：检查 Nginx 的 `try_files ... /index.html`。
- 401：检查请求头是否携带 Bearer Token，以及 JWT 密钥是否发生变化。

## 数据备份

```powershell
docker compose exec mysql mysqldump -uroot -p maintenance > maintenance-backup.sql
```

恢复、删除卷或重建数据库均属于有损操作，必须先确认备份可用。本项目不提供自动执行的删除脚本。


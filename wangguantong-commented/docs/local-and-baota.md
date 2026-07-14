# 注释学习副本运行说明

本文件仅适用于 `wangguantong-commented` 历史快照。当前主系统的本地与宝塔说明见 [根目录文档](../../docs/local-and-baota.md)。

## 本地运行

学习副本前端使用 `5173`，后端使用 `8087`，Vite 将 `/api` 代理到后端。

```bash
cd wangguantong-commented/wangguantong-server
mvn spring-boot:run
```

```bash
cd wangguantong-commented/wangguantong-web
npm install
npm run dev
```

访问用户端 `http://localhost:5173/`，后台为 `http://localhost:5173/admin`。

## 数据库警告

本副本的 `database/wangguantong.sql` 只有 8 张早期基础表，并会重建同名数据库。不要对当前主系统数据库执行。建议为学习副本使用独立 MySQL 实例或先完整备份。

## 宝塔说明

不建议部署此历史快照。需要部署当前系统时，使用仓库根目录的 `wangguantong-server`、`wangguantong-web`、`database/wangguantong.sql`，并按 [当前宝塔部署文档](../../docs/baota-deploy.md) 操作。

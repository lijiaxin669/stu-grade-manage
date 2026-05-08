# Git 改动记录

**日期**: 2026-01-24
**类型**: refactor
**关键词**: directory-structure-rename, config-update

## 1. 改动概览

本次提交主要涉及项目目录结构的重构，将 `server` 和 `client` 目录分别重命名为更通用的 `backend` 和 `frontend`，并同步更新了所有相关的配置文件和文档引用。同时包含了部分代码逻辑的增强（CourseRepository）。

- **修改文件数**: 140+ (大量重命名)
- **新增/删除行数**: 大量路径变更

## 2. 详细改动说明

### 2.1 目录重构 (Directory Refactoring)

- **Backend**: `server/` -> `backend/`
- **Frontend**: `client/` -> `frontend/`

### 2.2 配置同步 (Configuration)

- **Docker Compose**: 更新 `docker-compose.yml` 和 `docker-compose.dev.yml` 中的构建上下文路径 (`build: ./backend`, `build: ./frontend`)。
- **Maven**: `pom.xml` 位置变更。
- **Vite**: `vite.config.ts` 位置变更。
- **Nginx**: 新增/更新 `nginx.conf` (如有)。

### 2.3 文档更新 (Documentation)

- **README.md**:
  - 更新启动命令 (`cd backend`, `cd frontend`)。
  - 更新架构描述章节中的目录引用。
  - 补充详细的原始需求描述 (基于 User Edit)。
- **SKELETON**: 更新 `BACKEND_SKELETON.md` 和 `FRONTEND_SKELETON.md` 匹配新结构。
- **Check.md**: 新增验证结果。

### 2.4 代码增强 (Code Enhancements)

- **CourseRepository**:
  - 新增 `existsByNameAndSemester`
  - 新增 `findByNameAndSemester`
  - 新增 `findByTeacherId` (支持教师端课程查询)

## 3. 已知问题与建议

- **IDE 索引**: 目录重构后，建议开发者重启 IDE 或 Invalid Caches 以确保索引正确。
- **本地环境**: 需重新执行 `pnpm install` (前端) 和 `mvn clean install` (后端) 以适应路径变更。

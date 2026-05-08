# Git Change Summary - 2026-01-23

## 1. 改动概览

- **类型**: Initial Feature (feat)
- **文件数**: ~100 Files
- **描述**: 初始化学生成绩管理系统，包含完整的前后端实现。

## 2. 详细改动说明

### 2.1 后端 (Spring Boot)

- **核心架构**:
  - JDK 17 + Spring Boot 3.2.1 + PostgreSQL (H2 dev)
  - 典型的 Controller-Service-Repository 分层架构。
- **安全模块**:
  - 集成 Spring Security + JWT (Session-based fallback implemented).
  - 配置 `SecurityConfig`：修复了 Context Path 问题，实现了自定义的 Login/Logout/AccessDenied 处理。
  - 实现了 `CustomUserDetailsService` 和基于角色的权限控制 (`@PreAuthorize`).
- **业务模块**:
  - `User`: 用户管理，支持 Admin/Teacher/Student 角色。查询功能改用 JPA `Specification` 以修复 PostgreSQL Null 参数问题。
  - `Course`: 课程管理 (CRUD)。
  - `Enrollment`: 选课系统，支持学生选课/退课。
  - `Grade`: 成绩管理，包括录入、统计、导出 Excel。
- **数据初始化**:
  - `DataInitializer`: 启动时自动创建默认 Admin/Teacher/Student 账号。

### 2.2 前端 (Vue 3 + Vite)

- **技术栈**: Vue 3 (Script Setup), TypeScript, Vite, Pinia, Vue Router.
- **页面实现**:
  - `Login`: 登录页，支持重定向。
  - `Dashboard`: 仪表盘。
  - `System/User`: 用户管理列表。
  - `Academic/Course`: 课程管理。
  - `Academic/Grade`:
    - `TeacherGrade.vue`: 教师端成绩录入与管理。
    - `StudentGrade.vue`: 学生端查分。
  - `Academic/Stats`: 成绩统计图表（基础版）。
- **状态管理**:
  - `useUserStore`: 管理用户信息、角色和权限。
  - `request.ts`: Axios 封装，拦截器处理全局 200 业务码和 1002/1003 错误码跳转。

### 2.3 基础设施

- **Docker Compose**:
  - `docker-compose.dev.yml`: 定义 backend (8080) 和 postgres (5432) 服务。
  - 修复了 `docker-compose.yml` 中前端构建路径的问题。
- **文档**:
  - `README.md`, `*_DESIGN.md`, `VERIFICATION_CHECKLIST.md` 等完整工程文档。

## 3. 关键修复记录 (Troubleshooting)

1. **Login 401 Error**: 修复了 `SecurityConfig` 中路径包含 `/api` 前缀导致无法匹配 Servlet Path 的问题。
2. **Query 500 Error**: 修复了 `UserRepository` 中 JPQL 对 Null 参数处理不当导致 `Unknown Types value` 的问题，改用 `Specification`。
3. **Frontend Redirect Fail**: 修复了前端 `request.ts` 误判后端成功状态码 (0 vs 200) 导致登录后不跳转的问题。

## 4. 已知问题与建议

- 目前使用 H2 文件数据库进行开发验证，生产环境建议切换至独立 PostgreSQL。
- 前端组件样式较基础，建议后续引入 Element Plus 或 Ant Design Vue 提升体验。

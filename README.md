# 学生成绩管理系统 (Grade Management System)

这是一个基于 Spring Boot + Vue 3 的全栈应用，旨在为学校提供一个轻量级、高效的学生成绩管理平台。支持超级管理员、教师、学生三种角色，覆盖课程管理、成绩录入、成绩查询及统计分析功能。

## ✨ 原始需求

> 基于 SpringBoot+Vue 的学生成绩管理系统
> 一、系统核心设计
> 角色与权限划分（满足权限要求）
> 超级管理员 ：管理所有用户（教师 / 学生）、设置角色权限、维护课程信息，拥有最高权限
> 教师 ：管理所带课程的学生成绩（CURD）、查看成绩统计、导出成绩表，无用户管理权限
> 学生 ：仅能查看自己的各科成绩、查看课程信息，无修改 / 删除权限
> 核心功能模块
> 用户模块 ：登录、注册、密码重置（满足登录注册要求）
> 权限模块 ：角色分配、菜单权限控制、接口权限校验
> 课程模块 ：课程信息的增删改查（仅管理员 / 教师）
> 成绩模块 ：成绩录入、修改、查询、删除（教师操作），成绩查看（学生操作）（满足 CURD 要求）
> 统计模块 ：成绩平均分、及格率统计（教师 / 管理员）

## 🚀 运行步骤

### 方式一：Docker 一键启动（推荐）

项目支持 Docker Compose 编排，可一键启动数据库、后端和前端服务。

```bash
# 在项目根目录下 (grade-manage)
docker-compose up
```

启动后访问：`http://localhost:3000`

### 方式二：手动启动

启动后端 (Server)

确保本地已安装 JDK 17+ 和 Maven，并运行了 PostgreSQL 数据库。

```bash
cd backend
# 修改 application.yml 中的数据库配置
mvn clean package
java -jar target/grade-manage-backend.jar
```

启动前端 (Client)

确保本地已安装 Node.js (推荐 18+) 和 pnpm。

```bash
cd frontend
pnpm install
pnpm dev
```

启动后访问控制台输出的本地地址（通常为 `http://localhost:5173`）。

## 🧪 测试步骤

项目内置了三种角色的演示账号，可用于验证功能的完整性：

1. **管理员登录**
   - 账号/密码：`admin` / `123456`
   - **测试点**：用户管理（增删改查）、课程管理、全站数据查看。

2. **教师登录**
   - 账号/密码：`teacher` / `123456`
   - **测试点**：
     - 进入“我的课程”查看通过后台分配的课程。
     - 对学生进行成绩录入和修改。
     - 查看课程成绩统计（平均分、及格率）。
     - 导出成绩单为 Excel。

3. **学生登录**
   - 账号/密码：`student` / `123456`
   - **测试点**：
     - 仅能查看自己的成绩单。
     - 无法查看他人成绩或进行修改操作。

## 🏗 代码架构

项目采用典型的前后端分离开发模式，强调模块化与规范化：

### 前端架构 (Frontend)

- **技术栈**: Vue 3 (Composition API) + TypeScript + Vite + Pinia
- **目录结构**:
  - `src/layout/`: 布局组件。
    - `AdminLayout`: 侧边栏+顶部导航结构，适用于管理员和教师。
    - `UserLayout`: 顶部导航结构，适用于学生端。
  - `src/views/`: 页面视图，按角色 (`admin`, `teacher`, `student`) 物理隔离。
  - `src/store/`: 状态管理。
    - `user`: 管理用户信息及登录状态。
    - `permission`: 基于角色的动态路由生成。
  - `src/utils/`: 核心工具链。
    - `request.ts`: Axios 封装，统一处理请求拦截与 401/403 响应。
    - `confirm.ts`: 基于 Promise 的命令式确认弹窗。

### 后端架构 (Backend)

- **技术栈**: Spring Boot 3 + JDK 17 + MyBatis Plus + PostgreSQL
- **分层设计**:
  - `Controller`: 处理 HTTP 请求，参数校验。
  - `Service`: 核心业务逻辑，事务控制 (`@Transactional`)。
  - `Repository`: 数据访问层，继承 MyBatis Plus `BaseMapper`。
  - `config/SecurityConfig`: 安全配置与用户上下文管理。

## 🛠 工程细节

### 1. 🔐 安全认证与交互 (Security)

- **交互模式**: 实现了 SPA 友好的 Spring Security 配置。重写了 `AuthenticationSuccessHandler` 和 `AuthenticationFailureHandler`，登录成功/失败时返回统一的 JSON 格式数据，而非传统的页面跳转。
- **会话管理**: 使用标准 Session/Cookie 机制维持登录态，通过 `SecurityContextHolder` 获取当前登录用户信息。
- **权限控制**:
  - **方法级安全**: 启用 `@EnableMethodSecurity`，支持在 Controller 层使用权限注解。
  - **动态菜单**: 前端根据用户角色 (`ROLE_ADMIN`, `ROLE_TEACHER`) 动态过滤路由表，生成侧边栏菜单。

### 2. 🛡 统一异常处理 (Global Exception Handling)

- 后端实现了 `GlobalExceptionHandler` (`@RestControllerAdvice`)，将所有异常收敛为标准响应：
  - `BusinessException`: 业务逻辑错误（如“成绩已存在”），返回对应错误码。
  - `MethodArgumentNotValidException`: 参数校验失败，自动提取校验错误信息。
  - `Exception`: 未知系统错误，统一屏蔽堆栈信息，返回“系统繁忙”。

### 3. 🔌 扩展性设计

- **MyBatis Plus**: 启用了自动填充 (AutoFill) 功能，自动维护 `create_time` 和 `update_time` 字段。
- **数据隔离**: 在 Service 层严格区分数据权限，例如 `GradeService` 在查询时会根据 `Context` 中的用户角色自动附加 `student_id` 或 `teacher_id` 过滤条件。

### 4. 🎨 交互体验优化

- **Confirm 组件**: 封装了自定义的 `confirm` 函数，替代原生 `window.confirm`。支持 `await` 调用，代码逻辑更线性。
- **Toast 组件**: 实现了轻量级的消息提示，支持 success/error/warning/info 多种状态。

---

通过以上设计，本项目实现了一个功能完备、安全可靠的成绩管理系统，既满足了教务管理的刚需，又具备良好的扩展性。

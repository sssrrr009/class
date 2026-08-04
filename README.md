# 智慧班级管理系统

基于 B/S 结构的班级管理信息化系统，采用 **Spring Boot + Vue 3 + MySQL** 前后端分离架构。

## 系统角色

| 角色 | 说明 |
|---|---|
| 管理员 ADMIN | 系统全部数据管理（学院/专业/教师/干部/学生/班级/课程/成绩/公告/文件/投票） |
| 教职工 TEACHER | 课程与成绩管理、发布公告与投票 |
| 学生 STUDENT | 查询公告/课程/成绩、选课、参与投票、下载文件 |
| 干部 CADRE | 复用学生账号，班长限班级、学生会按范围分级发布 |

## 技术栈

- 后端：Spring Boot 2.7 / MyBatis-Plus / MySQL 8.0 / JWT / BCrypt / EasyExcel
- 前端：Vue 3 / Vite / Element Plus / Pinia / Vue Router / Axios

## 目录结构

```
├── backend/                 # 后端 Spring Boot 工程
│   ├── pom.xml
│   └── src/main/java/com/classmanage/
│       ├── ClassManageApplication.java   # 启动类
│       ├── common/          # 统一返回、异常处理
│       ├── config/          # Web/MyBatis-Plus 配置
│       ├── controller/      # 控制器（Auth/Health 已实现）
│       ├── dto/             # 请求/响应 DTO
│       ├── entity/          # 17 张表的实体类
│       ├── mapper/          # MyBatis-Plus Mapper
│       └── security/        # JWT 鉴权（拦截器/注解/上下文）
├── frontend/                # 前端 Vue 工程
│   ├── package.json
│   └── src/
│       ├── api/             # 接口调用
│       ├── layouts/         # 主布局
│       ├── router/          # 路由与登录守卫
│       ├── stores/          # Pinia 状态
│       ├── utils/           # Axios 封装
│       └── views/           # 页面（login + 4 角色首页）
├── sql/
│   └── init.sql             # 数据库初始化脚本（17 表 + 预置数据）
└── README.md
```

## 快速开始

### 1. 初始化数据库

```bash
# 需先安装 MySQL 8.0
mysql -uroot -p < sql/init.sql
```

### 2. 启动后端

```bash
cd backend
# 若本机已配置 Maven 可直接执行；否则使用 IDE 运行 ClassManageApplication
mvn spring-boot:run
```

> 启动前请修改 `src/main/resources/application.yml` 中的数据库账号密码（默认 root/root）。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

浏览器访问 http://localhost:5173

### 4. 打包构建（生产）

```bash
cd frontend && npm run build   # 产物在 dist/
cd backend && mvn clean package  # 产物在 target/classmanage-backend-1.0.0.jar
```

## 预置账号

| 角色 | 账号 | 密码 |
|---|---|---|
| 管理员 | admin | admin123 |
| 教师 | T001 | 123456 |
| 学生 | 2023001 | 011234（身份证后6位） |
| 干部 | 2023001（复用） | 011234 |

## 已实现功能

**基础架构**
- ✅ 四角色 JWT 登录鉴权（后端 AuthController + 拦截器，干部登录校验 cadre 身份）
- ✅ 统一响应/异常处理、分页插件、时间自动填充
- ✅ 前端登录页、四角色路由守卫、主布局、角色动态菜单

**基础管理模块（管理员端）**
- ✅ 学院管理、专业管理、教师管理（工号/职称/所属学院）
- ✅ 学生管理（学号/姓名/身份证，初始密码=身份证后6位，班级人数自动统计）
- ✅ 学生 Excel 批量导入（EasyExcel，返回逐行错误明细）
- ✅ 班级管理（关联专业与班主任教师）
- ✅ 干部管理（班长/学生会，学生会按学校/学院/专业分级）
- ✅ 课程管理（编号/学分/任课教师/上课时间地点/状态）
- ✅ 专业计划管理（指定专业可选课程与学年学期）
- ✅ 个人中心（查看信息、修改密码、修改资料）

**业务模块（第 5 周）**
- ✅ 学生选课（可选课程=专业计划内+未开课/开课中，重复选课校验，可退课）
- ✅ 成绩管理（仅已选课学生可录成绩，分数自动映射等级，学生查本人成绩）
- ✅ 公告管理（学校/学院/专业/班级/个人五级发布，按角色聚合可见范围，教师/干部/管理员共用接口）
- ✅ 文件分类管理（文本/压缩/Word 等类型字典）
- ✅ 文件管理（上传/下载/修改/删除/搜索，文件类型占比饼图，学生端下载）
- ✅ 投票评选（多票制 max_votes 上限校验，管理员/教师/干部共用创建接口，结果饼图可视化）
- ✅ 学生投票（投票+删除记录+查看本人投票+结果饼图）

## 接口约定

- 统一前缀 `/api`，返回 `{ code, msg, data }`，`code=200` 成功
- 登录接口：`POST /api/auth/login`，参数 `{ username, password, role }`
- 健康检查：`GET /api/health`
- 除登录外接口需携带 `Authorization: Bearer <token>`

**已提供接口**

| 模块 | 接口 |
|---|---|
| 认证 | `POST /api/auth/login` |
| 个人中心 | `GET /api/user/info` `PUT /api/user/password` `PUT /api/user/profile` |
| 学院 | `GET /api/college/{list,page}` `POST/PUT/DELETE /api/college` |
| 专业 | `GET /api/major/{list,page}` `POST/PUT/DELETE /api/major` |
| 教师 | `GET /api/teacher/{list,page}` `POST/PUT/DELETE /api/teacher` |
| 学生 | `GET /api/student/page` `POST/PUT/DELETE /api/student` `POST /api/student/import` |
| 班级 | `GET /api/class/{list,page}` `POST/PUT/DELETE /api/class` |
| 干部 | `GET /api/cadre/page` `POST/PUT/DELETE /api/cadre` |
| 课程 | `GET /api/course/{list,page}` `POST/PUT/DELETE /api/course` |
| 专业计划 | `GET /api/plan/page` `POST/PUT/DELETE /api/plan` |
| 选课 | `GET /api/enroll/{candidates,my}` `POST/DELETE /api/enroll/{courseId}` |
| 成绩 | `GET /api/score/{page,my}` `POST/PUT/DELETE /api/score` |
| 公告 | `GET /api/notice/page` `POST/PUT/DELETE /api/notice` |
| 文件分类 | `GET /api/category/list` `POST/PUT/DELETE /api/category` |
| 文件 | `GET /api/file/page` `POST /api/file` `PUT/DELETE /api/file/{id}` `GET /api/file/download/{id}` `GET /api/file/stat` |
| 投票 | `GET /api/vote/page` `POST/PUT/DELETE /api/vote` `POST /api/vote/cast` `GET /api/vote/{my,result/{id},detail}` `DELETE /api/vote/my/{id}` |

**Excel 导入模板格式**（学生，`.xlsx`）：

| 学号 | 姓名 | 性别 | 联系电话 | 身份证号码 | 班级号 |
|---|---|---|---|---|---|
| 2023001 | 张三 | 男 | 13900000001 | 110101200501011234 | 2023-01 |

导入后学生密码自动设为身份证后 6 位。

# 面向大学生就业的招聘数据分析与智能求职辅助平台

本仓库是软件工程课程项目的基础框架，采用 **前后端分离 + 独立 AI 服务** 的架构。

当前项目已经可以本地启动三端服务：

- 前端：Vue 3 页面框架。
- 后端：Spring Boot 基础接口。
- AI 服务：FastAPI 模拟简历解析与岗位匹配接口。

现阶段重点是给团队提供一个清晰、可运行、方便继续开发的项目骨架。页面和接口中有一部分是模拟数据，后续由各模块负责人继续完善真实业务逻辑。

## 项目结构

```text
ai-software
├── frontend/          # Vue 3 + Vite + Element Plus 前端
├── backend/           # Spring Boot 后端
├── ai-service/        # FastAPI AI 服务
├── docs/              # 架构、接口和实施说明
├── data/              # 示例招聘数据
├── scripts/           # 启动脚本
├── 项目立项说明.md
└── README.md
```

## 当前能做什么

### 前端

访问地址：

```text
http://localhost:5173/
```

目前已有页面：

- 首页看板
- 岗位列表
- 岗位详情
- 数据分析
- 简历辅助
- 后台管理
- 登录页

说明：

- 登录页目前只是页面骨架，没有真实账号密码校验。
- 岗位列表、首页看板、数据分析页面目前使用模拟数据。
- 简历上传页面目前只有 UI，暂未接入真实文件上传接口。

### 后端

访问地址：

```text
http://localhost:8080
```

目前已有基础接口：

```http
GET /api/health
GET /api/job/list
GET /api/analysis/city-job-count
GET /api/admin/dashboard
```

说明：

- 当前后端默认使用 H2 内存数据库，方便本地直接启动。
- MySQL 已经预留依赖，后续接入真实数据库时修改 `backend/src/main/resources/application.yml` 即可。
- 业务接口目前返回模拟数据，后续需要替换为数据库查询。

### AI 服务

访问地址：

```text
http://localhost:8000
```

目前已有基础接口：

```http
GET  /health
POST /ai/resume/parse
POST /ai/job/match
POST /ai/resume/suggestion
```

说明：

- 当前 AI 服务使用关键词模拟简历解析和岗位匹配。
- 后续可以替换为真实 NLP、LLM 或规则评分逻辑。

## 技术栈

| 模块 | 技术 |
| --- | --- |
| 前端 | Vue 3、Vite、Element Plus、Vue Router、Pinia、Axios、ECharts |
| 后端 | Spring Boot、Spring MVC、MyBatis-Plus、H2、本地预留 MySQL |
| AI 服务 | Python、FastAPI、Uvicorn、Pydantic、Pytest |

## 环境要求与依赖安装

运行本项目之前，需要先在电脑上安装以下基础软件。

| 工具 | 建议版本 | 说明 |
| --- | --- | --- |
| Git | 2.0 或以上 | 拉取代码、版本管理 |
| Node.js | 18 或以上 | 前端运行环境 |
| npm | 9 或以上 | 前端依赖管理，安装 Node.js 后通常自带 |
| Java | 17 或以上 | 后端运行环境 |
| Maven | 3.8 或以上 | 后端依赖管理 |
| Python | 3.10 或以上 | AI 服务运行环境 |
| pip | Python 对应版本 | Python 依赖管理，安装 Python 后通常自带 |

可以用下面命令检查：

```bash
git --version
node -v
npm -v
java -version
mvn -v
python3 --version
python3 -m pip --version
```

### macOS 安装示例

如果使用 macOS 并已安装 Homebrew，可以参考：

```bash
brew install git
brew install node
brew install openjdk@17
brew install maven
brew install python
```

如果安装了多个 Java 版本，请确认 `java -version` 输出的是 17 或以上版本。

### Windows 安装建议

Windows 可以分别下载安装：

- Git：https://git-scm.com/
- Node.js：https://nodejs.org/
- JDK 17：https://adoptium.net/
- Maven：https://maven.apache.org/
- Python：https://www.python.org/

安装完成后，重新打开终端，再运行上面的检查命令。

### 当前阶段不需要提前安装的服务

以下服务在项目立项中有规划，但当前基础框架暂时不强制要求安装：

| 服务 | 当前状态 |
| --- | --- |
| MySQL | 后端当前默认使用 H2 内存数据库，后续做真实数据持久化时再接入 MySQL |
| Redis | 当前未使用，后续做缓存或 AI 结果缓存时再接入 |
| Nginx | 当前本地开发不需要 |
| Docker | 当前本地开发不需要，后续部署时可选 |

### 项目依赖是否需要手动安装

一般不需要手动逐个安装项目依赖，启动脚本会自动处理一部分依赖：

| 模块 | 依赖文件 | 安装方式 |
| --- | --- | --- |
| 前端 | `frontend/package.json` | `./scripts/start-frontend.sh` 会在没有 `node_modules` 时自动执行 `npm install` |
| 后端 | `backend/pom.xml` | `./scripts/start-backend.sh` 启动 Maven 时会自动下载依赖 |
| AI 服务 | `ai-service/requirements.txt` | `./scripts/start-ai-service.sh` 会创建 `.venv` 并安装 Python 依赖 |

如果想手动安装，也可以执行：

```bash
# 前端依赖
cd frontend
npm install

# 后端依赖和测试
cd ../backend
mvn test

# AI 服务依赖
cd ../ai-service
python3 -m venv .venv
.venv/bin/python -m pip install -r requirements.txt
```

## 快速启动

先进入项目根目录：

```bash
cd /Users/lee/vscode/ai-software
```

> 三个服务都需要长期运行，因此建议打开三个终端窗口，分别启动前端、后端和 AI 服务。

### 1. 启动前端

在第一个终端运行：

```bash
./scripts/start-frontend.sh
```

启动成功后会看到类似输出：

```text
VITE ready
Local: http://localhost:5173/
```

浏览器打开：

```text
http://localhost:5173/
```

### 2. 启动后端

在第二个终端运行：

```bash
./scripts/start-backend.sh
```

启动成功后会看到类似输出：

```text
Tomcat started on port 8080
Started JobPlatformApplication
```

浏览器打开健康检查：

```text
http://localhost:8080/api/health
```

正常返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "UP",
    "service": "backend"
  }
}
```

### 3. 启动 AI 服务

在第三个终端运行：

```bash
./scripts/start-ai-service.sh
```

启动成功后会看到类似输出：

```text
Uvicorn running on http://0.0.0.0:8000
Application startup complete
```

浏览器打开健康检查：

```text
http://localhost:8000/health
```

正常返回：

```json
{
  "service": "ai-service",
  "status": "UP"
}
```

## 手动启动方式

如果不使用 `scripts` 目录下的脚本，也可以分别进入三个子项目手动启动。

### 前端

```bash
cd frontend
npm install
npm run dev
```

### 后端

```bash
cd backend
mvn spring-boot:run
```

### AI 服务

```bash
cd ai-service
python3 -m venv .venv
.venv/bin/python -m pip install -r requirements.txt
.venv/bin/uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

## 测试命令

### 前端

```bash
cd frontend
npm test
```

### 后端

```bash
cd backend
mvn test
```

### AI 服务

```bash
cd ai-service
.venv/bin/pytest
```

## 接口测试示例

### 1. 测试后端健康检查

```bash
curl http://localhost:8080/api/health
```

### 2. 测试后端岗位列表

```bash
curl http://localhost:8080/api/job/list
```

### 3. 测试后端城市岗位统计

```bash
curl http://localhost:8080/api/analysis/city-job-count
```

### 4. 测试 AI 服务健康检查

```bash
curl http://localhost:8000/health
```

### 5. 测试 AI 岗位匹配接口

```bash
curl -X POST http://localhost:8000/ai/job/match \
  -H "Content-Type: application/json" \
  -d '{
    "resumeText": "熟悉 Java、Spring Boot、Vue 和 MySQL。",
    "jobDescription": "要求掌握 Java、Spring Boot、MySQL，了解 Redis。"
  }'
```

正常会返回类似：

```json
{
  "score": 90,
  "matchedSkills": ["Java", "MySQL", "Spring Boot"],
  "missingSkills": ["Redis"],
  "suggestion": "当前匹配度较好，建议补充 Redis 相关经历。"
}
```

## 停止服务

如果是在终端中启动的服务，直接在对应终端按：

```text
Ctrl + C
```

如果需要查找并停止端口进程，可以使用：

```bash
lsof -iTCP:5173 -sTCP:LISTEN
lsof -iTCP:8080 -sTCP:LISTEN
lsof -iTCP:8000 -sTCP:LISTEN
```

找到 PID 后停止：

```bash
kill -9 PID
```

把 `PID` 替换成实际查到的进程号。

## 常见问题

### 1. 端口被占用

如果看到 `port already in use`，说明对应服务已经启动或端口被其他程序占用。

检查端口：

```bash
lsof -iTCP:5173 -sTCP:LISTEN
lsof -iTCP:8080 -sTCP:LISTEN
lsof -iTCP:8000 -sTCP:LISTEN
```

### 2. 脚本没有执行权限

如果运行脚本时报 `Permission denied`，执行：

```bash
chmod +x scripts/*.sh
```

然后重新运行启动脚本。

### 3. 前端依赖安装失败

可以进入前端目录重新安装：

```bash
cd frontend
rm -rf node_modules
npm install
```

### 4. 后端启动慢

第一次运行 Maven 会下载 Spring Boot、MyBatis-Plus、数据库驱动等依赖，耗时较长。依赖下载完成后，后续启动会快很多。

### 5. AI 服务虚拟环境问题

如果 AI 服务依赖异常，可以重建虚拟环境：

```bash
cd ai-service
rm -rf .venv
python3 -m venv .venv
.venv/bin/python -m pip install -r requirements.txt
```

## 说明文档

- [项目立项说明.md](./项目立项说明.md)
- [docs/系统架构说明.md](./docs/系统架构说明.md)
- [docs/接口说明.md](./docs/接口说明.md)
- [docs/实施计划.md](./docs/实施计划.md)

## 团队开发分工建议

### 前端负责人

主要目录：

```text
frontend/src
```

重点任务：

- 完善登录、注册页面。
- 接入后端接口。
- 实现路由守卫。
- 完善岗位列表、岗位详情、个人中心等页面。

### 前端可视化负责人

主要目录：

```text
frontend/src/views/AnalysisView.vue
frontend/src/components
```

重点任务：

- 拆分 ECharts 图表组件。
- 接入后端分析接口。
- 完善薪资分布、城市分布、热门技能、学历占比等图表。

### 后端负责人

主要目录：

```text
backend/src/main/java/com/example/jobplatform
```

重点任务：

- 实现用户注册、登录、JWT 鉴权。
- 实现岗位数据 CRUD。
- 实现收藏、浏览历史、简历上传接口。
- 接入 MySQL。
- 对接 AI 服务。

### 数据与分析负责人

主要目录：

```text
data
backend/src/main/resources
```

重点任务：

- 整理 CSV / Excel 招聘数据。
- 设计 MySQL 表结构。
- 编写数据导入和清洗逻辑。
- 输出统计分析结果。

### AI 与测试负责人

主要目录：

```text
ai-service/app
ai-service/tests
```

重点任务：

- 完善简历解析。
- 完善岗位匹配算法。
- 生成简历优化建议。
- 编写 AI 服务测试和系统联调测试。

## 后续开发建议

1. 前端负责人继续完善页面、路由守卫、接口封装和权限状态。
2. 后端负责人继续补用户、岗位、收藏、简历、管理员等模块。
3. 数据负责人补 MySQL 表结构、导入脚本、清洗规则和统计指标。
4. AI 与测试负责人替换当前模拟逻辑，实现真实简历解析和岗位匹配。
5. 每个成员开发前先阅读 `docs/系统架构说明.md` 和 `docs/接口说明.md`，避免接口命名和模块边界不一致。

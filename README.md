# AI4Novel - AI辅助小说写作工具

基于 Spring Boot + Vue 3 的前后端分离架构，帮助小说作者高效管理创作过程。

## 功能

- **小说管理**：创建、浏览、删除小说，自动生成标准化文件夹结构（人物设定、剧情走向、世界观设定、伏笔、章节）
- **文件系统**：可视化文件夹树，支持新建/删除文件和文件夹，文本文件的读写
- **章节管理**：独立的章节目录，快速定位和编辑各章节内容
- **AI写作助手**：右侧聊天面板，可与AI对话辅助创作（待接入AI服务）
- **持久化存储**：MySQL + MyBatis 持久化小说元数据，重启不丢失

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.6.13 |
| 持久化 | MyBatis 2.2.2 + MySQL 8.0 |
| 前端框架 | Vue 3 (Composition API) |
| 构建工具 | Vite 5 + Maven |
| 路由 | Vue Router 4 (Hash模式) |
| 语言 | Java 8 + JavaScript |

## 快速开始

### 环境要求

- JDK 8+
- Maven 3.x
- Node.js 18+（推荐 20+）
- MySQL 8.0

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS ai4novel DEFAULT CHARSET utf8mb4;
```

### 2. 配置数据库

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai4novel?...
    username: root
    password: 你的密码
```

### 3. 启动后端

```bash
mvn spring-boot:run
```

后端运行在 `http://localhost:8080`

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 `http://localhost:3000`

## 项目结构

```
AI4Novel/
├── src/main/java/org/example/ai4novel/
│   ├── entity/              # 实体类
│   │   ├── Novel.java       # 小说模型
│   │   └── TreeNode.java    # 文件树节点
│   ├── mapper/
│   │   └── NovelMapper.java # MyBatis映射
│   ├── service/
│   │   ├── NovelService.java # 小说业务逻辑
│   │   └── FileService.java  # 文件系统操作
│   ├── controller/
│   │   ├── NovelController.java  # 小说API
│   │   ├── FileController.java   # 文件API
│   │   └── ChatController.java   # AI对话API
│   └── config/
│       └── CorsConfig.java  # 跨域配置
├── src/main/resources/
│   ├── application.yml      # 应用配置
│   └── schema.sql           # 建表语句
├── frontend/
│   └── src/
│       ├── views/
│       │   ├── HomePage.vue       # 首页（小说列表）
│       │   └── WorkspacePage.vue  # 工作区（三栏布局）
│       ├── components/
│       │   ├── LeftSidebar.vue    # 文件树面板
│       │   ├── TreeNode.vue       # 递归树节点
│       │   ├── ChapterList.vue    # 章节列表
│       │   ├── ContentEditor.vue  # 内容编辑器
│       │   └── RightSidebar.vue   # AI对话面板
│       └── router/index.js        # 路由配置
└── novels/                   # 小说文件存储目录（自动生成）
```

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/novels` | 创建小说 |
| GET | `/api/novels` | 小说列表 |
| GET | `/api/novels/{id}` | 小说详情 |
| DELETE | `/api/novels/{id}` | 删除小说 |
| GET | `/api/novels/{id}/tree` | 文件目录树 |
| GET | `/api/novels/{id}/file?path=` | 读取文件 |
| PUT | `/api/novels/{id}/file` | 保存文件 |
| POST | `/api/novels/{id}/resource` | 创建文件/文件夹 |
| DELETE | `/api/novels/{id}/resource` | 删除文件/文件夹 |
| POST | `/api/novels/{id}/chat` | AI对话（占位） |

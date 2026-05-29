# AI4Novel 项目状态

## 更新时间
2026-05-30

## 当前版本
v0.2.0

## 功能完成度

| 模块 | 功能 | 状态 |
|------|------|------|
| 小说管理 | 创建/浏览/删除小说 | ✅ 完成 |
| | 自动生成文件夹结构 | ✅ 完成 |
| 文件系统 | 可视化文件树 | ✅ 完成 |
| | 新建/删除文件和文件夹 | ✅ 完成 |
| | 文本文件读写 | ✅ 完成 |
| 章节管理 | 独立章节目录 | ✅ 完成 |
| | 快速定位编辑 | ✅ 完成 |
| AI写作助手 | 右侧对话面板 | ✅ 完成 |
| | 多AI配置切换 | ✅ 完成 |
| | AI配置管理页面 | ✅ 完成 |
| | 集成外部AI服务 | ✅ 完成 |
| 持久化存储 | MySQL + MyBatis | ✅ 完成 |

## 最近更新 (2026-05-30)

### 新增功能
- **AI配置管理**
  - 新增 AI配置页面 (`/settings`)
  - 支持创建、编辑、删除AI配置
  - 支持设置默认AI配置
  - AI配置包含：名称、API URL、API KEY

- **AI切换功能**
  - 右侧对话面板上方添加AI选择器
  - 实时切换不同的AI配置
  - 自动选中默认配置

- **API扩展**
  - 新增 AI配置相关API端点
  - ChatController 现在使用配置的AI服务进行实际对话

### 后端变更
```
src/main/java/org/example/ai4novel/
├── entity/AiConfig.java          # 新增
├── mapper/AiConfigMapper.java    # 新增
├── service/AiConfigService.java  # 新增
├── controller/AiConfigController.java  # 新增
└── controller/ChatController.java      # 更新（集成AI配置）
```

### 前端变更
```
frontend/src/
├── views/ConfigPage.vue          # 新增
├── components/AiSelector.vue     # 新增
├── views/HomePage.vue            # 更新（添加设置入口）
├── components/RightSidebar.vue   # 更新（集成AI选择器）
└── router/index.js               # 更新（添加设置路由）
```

### 数据库变更
```sql
-- 新增 ai_config 表
CREATE TABLE IF NOT EXISTS ai_config (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    api_url VARCHAR(500) NOT NULL,
    api_key VARCHAR(200) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
);
```

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 2.6.13 |
| 持久化 | MyBatis | 2.2.2 |
| 数据库 | MySQL | 8.0 |
| 前端框架 | Vue 3 | - |
| 构建工具 | Vite | 5 |
| 路由 | Vue Router | 4 (Hash模式) |
| HTTP客户端 | RestTemplate | Spring内置 |

## 使用说明

### 启动应用
```bash
# 后端
mvn spring-boot:run

# 前端
cd frontend
npm install
npm run dev
```

### 配置AI
1. 访问首页，点击右上角"⚙️ AI设置"
2. 填写配置信息：
   - 配置名称（如：GPT-3.5）
   - API URL（如：https://api.openai.com/v1/chat/completions）
   - API KEY
3. 点击"添加配置"
4. 第一个配置自动设为默认，或手动点击"设为默认"

### 使用AI助手
1. 进入小说工作区
2. 右侧面板上方选择要使用的AI配置
3. 输入消息，按 Enter 发送

## 待办事项

- [ ] AI对话历史持久化
- [ ] 支持流式响应
- [ ] 添加预设提示词模板
- [ ] 支持导出AI配置
- [ ] 添加配置导入/导出功能
- [ ] 支持多个AI同时对话对比

## 已知问题
- AI响应格式依赖 OpenAI API 兼容格式，其他格式需要手动适配
- API KEY 明文存储，生产环境建议加密

## Git 状态
当前分支: `master`
最近提交: 添加AI配置功能，支持多AI切换
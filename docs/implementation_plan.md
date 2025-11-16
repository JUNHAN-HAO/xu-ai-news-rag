项目实现计划（更新：含测试用例与前端具体调整）

- 概述：按 MVP / 二期 / 三期 拆分，支持 RSS/网页抓取、嵌入、向量检索、Ollama LLM、Spring Security + JWT、邮件通知、Excel 导入、聚类分析、英文/中文双语展示。
- 测试：后端单元测试（JUnit + Mockito）、后端集成测试（Testcontainers + Spring Boot Test）、嵌入服务单元测试与端到端测试、前端单元/集成（Jest + React Testing Library 或 Vitest + Vue Testing Library）、端到端 e2e（Playwright/Cypress）。
- 前端（React 推荐）文件调整：统一 i18n 方案（react-i18next），提供 zh/en 两套文案文件；组件：Login、Register、NewsList、NewsDetail、Upload (Excel/CSV)、SemanticSearch、AnalysisReport。
- CI：GitHub Actions 运行后端与前端测试、静态检查、构建镜像。
- 替代：仓库 issues 被禁用时，将 issue 草稿放入 docs/roadmap_issues.md。
- 接下来：确认要提交的示例文件和你偏好的前端框架（React / Vue）。

# 从这里继续：每人完成自己的完整功能

**2026-11-20 当前进度：**Fuxuan 的导出核心 [PR #4](https://github.com/fanfan170721/team-project/pull/4) 和 CSV 写入器 [PR #8](https://github.com/fanfan170721/team-project/pull/8)、Xinyu 的复习计划核心 [PR #7](https://github.com/fanfan170721/team-project/pull/7) 均已合并。不要重复第一轮。

**本轮按每人约 2–3 小时估算，完成自己的界面、接线、测试和 PR。**这是我们的安排估计，不是教授规定的最短时间、最长时间或完成保证。原先的 30 分钟上限已取消；可以连续完成，也可以分段继续，提前完成不用等待，遇阻及时反馈。

教授的项目要求强调每人主导完整用例并持续贡献，没有在本次核对的项目要求中规定每次开发的小时上限。完整用例包括自己的 Controller、Presenter、触发界面和结果更新；已有核心无需重做。参见 [Project Expectations](https://q.utoronto.ca/courses/444184/pages/project-expectations) 和 [MVP 要求](https://q.utoronto.ca/courses/444184/pages/project-readme-and-minimum-viable-product-mvp)。

**现在只打开自己这一页，复制顶部“当前任务”的提示词给自己的 AI：**

- **Fuxuan：**先审核 [PR #6](https://github.com/fanfan170721/team-project/pull/6)，再完成[导出成绩完整功能](FIRST_TASK_FUXUAN.zh-CN.md)。沿用分支：`Emilia/xfx-export-adapters`。
- **Xinyu：**完成[今日复习计划完整功能](FIRST_TASK_XINYU.zh-CN.md)。沿用分支：`Emilia/xinyu-review-adapters`。

两页已写明如何检查现有工作、从最新 `origin/Emilia/project-delivery` 开分支、实现和测试。已经按上一版任务开始的，就在原分支接着做，不覆盖或重建。本轮允许为自己的功能做最小必要的 Swing 和应用接线；两人独立提交，Allen 在合并时整合共同文件，保留双方改动。不要为避免冲突而留下无法从真实应用进入的界面。

本人检查实际差异和结果后，可以让自己的 AI 使用本人正常 Git 身份协助提交、push 和创建 PR。已完成且检查通过的 PR 可直接 Ready；尚未完成则保留 Draft 并列明缺项，不自己批准自己的 PR。按实际完成的改动提交，无需等满预计时长。群里回复：`姓名：已实现___；实际测试/演示___；PR 链接___；剩余/卡点___。`

## 仅当本机还没有可用项目时

已有能运行的 clone 就跳过本节。

1. 在 IntelliJ 使用 **Clone Repository / Get from VCS**，地址填 `https://github.com/fanfan170721/team-project.git`，选择一个不存在的新文件夹；登录自己的 GitHub 账号，不覆盖旧项目。
2. 导入根目录的 `pom.xml` 为 Maven 项目，选择已有的 JDK 11 或 17。未安装或无法访问仓库时，先反馈卡点。
3. 回到自己的当前任务页，让 AI 先检查分支和工作区，再从最新交付分支开始本轮。已有分支或改动时先检查，不重复创建。
4. 没有命令行 Maven 时，可在 IntelliJ 的 **Maven → Lifecycle → verify** 运行检查。以 **BUILD SUCCESS / BUILD FAILURE** 为准；失败时提供最后约 20 行报错，不删测试或降低覆盖率。

操作参考：[IntelliJ 克隆仓库](https://www.jetbrains.com/help/idea/set-up-a-git-repository.html)、[Maven 工具窗口](https://www.jetbrains.com/help/idea/maven-projects-tool-window.html)。按钮文字可能随版本不同。

[TEAM_TASKS](TEAM_TASKS.zh-CN.md) 保留完整用例说明和历史计划；实际执行任务以上面的两份个人页面为准，不执行历史提示词。

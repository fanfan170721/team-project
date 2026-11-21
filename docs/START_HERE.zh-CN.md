# 从这里继续：本轮最多 30 分钟

**2026-11-20 当前进度：**Fuxuan 的导出核心 [PR #4](https://github.com/fanfan170721/team-project/pull/4) 和 CSV 写入器 [PR #8](https://github.com/fanfan170721/team-project/pull/8)、Xinyu 的复习计划核心 [PR #7](https://github.com/fanfan170721/team-project/pull/7) 均已合并。不要重复第一轮。

**现在只打开自己这一页，复制顶部“当前任务”的提示词给自己的 AI：**

- **Fuxuan：**[导出结果的 Controller / Presenter / ViewModel](FIRST_TASK_FUXUAN.zh-CN.md)。新分支：`Emilia/xfx-export-adapters`。
- **Xinyu：**[复习计划的 Controller / Presenter / ViewModel](FIRST_TASK_XINYU.zh-CN.md)。新分支：`Emilia/xinyu-review-adapters`。

两页已写明如何检查现有工作、从最新 `origin/Emilia/project-delivery` 开新分支、实现和测试。先保留并检查已有本地修改，不覆盖；本轮不接 Swing 界面或共享应用。本人检查结果并确认后，可以让自己的 AI 使用本人正常 Git 身份协助提交、push 和创建 Draft PR，不能代替别人审核。

从准备开始计时 30 分钟，安装、下载、解释和检查都算在内。到时未完成就保留工作、报告真实进度，下轮继续；不要求一轮保证做完。群里只需回复：`姓名：测试通过/卡在___；PR 链接___（未创建就写未创建）。`

## 仅当本机还没有可用项目时

已有能运行的 clone 就跳过本节。

1. 在 IntelliJ 使用 **Clone Repository / Get from VCS**，地址填 `https://github.com/fanfan170721/team-project.git`，选择一个不存在的新文件夹；登录自己的 GitHub 账号，不覆盖旧项目。
2. 导入根目录的 `pom.xml` 为 Maven 项目，选择已有的 JDK 11 或 17。未安装或无法访问仓库时，先反馈卡点。
3. 回到自己的当前任务页，让 AI 先检查分支和工作区，再从最新交付分支开始本轮。已有分支或改动时先检查，不重复创建。
4. 没有命令行 Maven 时，可在 IntelliJ 的 **Maven → Lifecycle → verify** 运行检查。以 **BUILD SUCCESS / BUILD FAILURE** 为准；失败时提供最后约 20 行报错，不删测试或降低覆盖率。

操作参考：[IntelliJ 克隆仓库](https://www.jetbrains.com/help/idea/set-up-a-git-repository.html)、[Maven 工具窗口](https://www.jetbrains.com/help/idea/maven-projects-tool-window.html)。按钮文字可能随版本不同。

[TEAM_TASKS](TEAM_TASKS.zh-CN.md) 保留完整用例说明和历史计划；实际执行任务以上面的两份个人页面为准，不执行历史提示词。

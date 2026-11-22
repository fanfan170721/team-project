# 两位组员的当前任务与历史参考

**2026-11-22 状态覆盖说明：下面的功能实现安排已完成，仅作历史参考。**Fuxuan 的 #9、Xinyu 的 #11 均已合并；Xinyu 对修复后 [#10](https://github.com/fanfan170721/team-project/pull/10) 的复核批准已记录，#10 已合并。接下来审核发布到 `main` 的整合版本和独立的 [#12](https://github.com/fanfan170721/team-project/pull/12) 演示准备，随后完成各自讲解和真实排练。以 [HANDOFF](HANDOFF.md) 和实际 PR 状态为准，不重复实现功能。

## 以下为已执行的历史任务

**2026-11-20 更新：原先的 30 分钟上限已取消。当前按每人约 2–3 小时安排一个完整功能，包括适配器、Swing 界面、应用接线、测试和 PR。**这是工作量估计，不是教授规定的最低/最高时长，也不是完成保证。只执行各自个人页面顶部的当前提示词；[START_HERE](START_HERE.zh-CN.md) 是简短入口，下面的历史计划不需要通读。

Fuxuan 的 UC-09 核心已通过 [PR #4](https://github.com/fanfan170721/team-project/pull/4)、CSV 写入器已通过 [PR #8](https://github.com/fanfan170721/team-project/pull/8) 合并。Xinyu 的 UC-10 核心和 13 个测试已通过 [PR #7](https://github.com/fanfan170721/team-project/pull/7) 合并。两人继续完成各自的 Controller、Presenter、ViewModel、界面、接线和测试，不重复已有核心。Fuxuan 先审核 [PR #6](https://github.com/fanfan170721/team-project/pull/6)，再继续自己的功能。

| 实施负责人 | 当前唯一执行入口 | 从最新交付分支创建的新分支 |
| --- | --- | --- |
| Fuxuan | [当前轮：完整导出成绩功能](FIRST_TASK_FUXUAN.zh-CN.md) | `Emilia/xfx-export-adapters` |
| Xinyu | [当前轮：完整今日复习计划功能](FIRST_TASK_XINYU.zh-CN.md) | `Emilia/xinyu-review-adapters` |

已开始上一版任务就沿用已有分支和代码；尚未开始才从最新交付分支创建。各自可以对共享文件做最小必要接线；Allen 负责在合并时整合冲突，保留双方功能。不要为隔离文件而留下无法从实际应用进入的界面。

每人应实际主导自己的 Interactor、相关接口、专属 Controller/Presenter、触发操作与结果显示的 UI，检查改动和运行结果，并能讲解自己的用例。保留实际贡献归属；共享工作或分工调整先沟通，不能把已有实现重新署名为自己的代码。

成员可以使用自己的 AI 帮助生成、修改、运行、测试和解释代码。本人检查差异与实际测试结果、确认内容后，也可以授权自己的 AI 使用本人正常 Git 身份协助 commit、push 和创建 Draft PR；不要求必须手动点击或输入 Git 命令。使用自己的账号，由实际审核者提交审核，按事实描述测试和贡献。课程允许使用生成式 AI，同时要求每人对自己的学习负责；AI 不代替本人参与。正式用例规格见 `docs/USE_CASES.md` 的 **UC-09/UC-10**。

本任务单依据 2026-11-17 保存、并于 11 月 20 日针对时长和个人职责重新核对的课程要求整理。教授要求每人主导 MVP 用例、提交实质代码 PR、review 他人、参与展示；没有在这些项目页面中规定单次编码时长上限。两三小时包括检查、实现、测试、说明和提交，可连续做也可分段继续。提前完成即可交付；遇到具体阻碍或预计时间不够，就报告已完成部分和剩余工作，不保证本轮全部完成。测试数不等于覆盖率。

## 历史参考：截至 2026-11-19 的完整规格与实施计划

**从本节开始到文末都是历史参考，不是当前可执行任务。旧状态、分支命令、目录建议和 AI 提示词均已由上方 2026-11-20 进度及两份个人任务页取代。不要复制执行旧提示词、重新实现已合并的核心或 CSV，也不要重复创建第一轮 PR。当前只使用上方两份个人任务页；完整用例规格以 `docs/USE_CASES.md` 为准。**

## 历史设置：先用自己的账号和自己的新文件夹

仅首次设置时执行下面步骤；已有 clone 和分支就从现有进度继续，Fuxuan 不重复已完成的核心任务。两个人分别在自己的电脑上操作，不要操作别人现有的 `team-project` 目录。仓库是：

<https://github.com/fanfan170721/team-project>

先确认自己是该仓库的 collaborator，且能看到 `Emilia/project-delivery` 分支。若分支还没有发布，先等负责人发出“基线已可用”的消息，不要用别的分支猜测替代。

在 PowerShell 复制下面这一段。它只在用户目录中新建 `team-project-work`；同名路径已存在时会停止，不会删除或覆盖它。

```powershell
& {
$taskFolder = Join-Path $env:USERPROFILE 'team-project-work'
if (Test-Path -LiteralPath $taskFolder) {
    throw 'team-project-work 已存在。请先确认它是什么；不要删除或覆盖。选择另一个未使用的目录名后再继续。'
}
git clone --branch Emilia/project-delivery --single-branch https://github.com/fanfan170721/team-project.git "$taskFolder"
if ($LASTEXITCODE -ne 0) { throw 'Clone 没有成功。先解决网络、本人登录或仓库权限问题，不继续下面的步骤。' }
Set-Location -LiteralPath $taskFolder
git status --short --branch
git config user.name
git config user.email
}
```

最后两行只显示现有 Git 身份，不修改它。应是你本人正常使用的身份；若为空或显示别人，先告诉负责人处理，不借用他人登录、不通过改身份制造他人贡献。GitHub 的登录也使用自己的账号。

Fuxuan 第一轮的分支命令（历史参考，不重复执行）：

```powershell
git switch -c Emilia/xfx-export-results origin/Emilia/project-delivery
git status --short --branch
```

Xinyu 接着执行：

```powershell
git switch -c Emilia/xinyu-due-review origin/Emilia/project-delivery
git status --short --branch
```

然后在 IntelliJ 打开新文件夹里的 `pom.xml`，作为 Maven 项目导入，选择 JDK 11 或更高版本。在同一 PowerShell 窗口运行：

```powershell
mvn verify
```

如果启动基线就失败，保留报错并先解决，不把已有错误隐藏在自己的新功能里。第一次下载依赖可能超过一个工作段，可以下一段继续。后面的命令都在新 clone 目录执行。

## Fuxuan：导出本人测验成绩 CSV

### 用例规格

- **Name：** Export completed quiz results。
- **Primary actor：** 已登录学习者。
- **Preconditions：** 学习者已登录，应用能够读取其学习记录。
- **Trigger：** 学习者请求将自己的测验成绩保存为文件。
- **Success outcome：** 学习者选定位置得到一个 CSV 文件；界面显示成功及导出条数；学习记录保持原样。
- **Main success scenario：**
  1. 学习者发起导出请求。
  2. 系统让学习者选择保存位置。
  3. 学习者确认位置。
  4. 系统读取该学习者已完成的测验，按完成时间从早到晚导出。
  5. 系统告知文件保存成功及导出条数。
- **Extensions：**
  - 3a. 学习者取消：结束操作，不创建文件、不报成功。
  - 3b. 目标文件已存在：明确询问是否覆盖；拒绝则不写入。
  - 4a. 没有已完成测验：生成只有表头的 CSV，成功提示为 0 条。
  - 4b. 读取或写入失败：显示可理解的错误，不显示成功，不修改学习记录；导出未完整写成时保留目标位置已有的文件。
  - 4c. 已退出登录：拒绝导出，不读取其他账号的数据。

### 具体做哪些文件

以下路径均相对于项目根目录。核心及其接口、DTO、测试已经存在，先读最新基线，不重复创建。剩余实现等分工回复后再开始；不依赖旧 `AppController` 的内部字段。

| 文件或位置 | 你的职责 |
| --- | --- |
| `src/main/java/com/wordtrail/usecase/ExportResultsInputBoundary.java` | 定义导出入口，例如 `void execute(String destination)` |
| 同目录 `ExportResultsOutputBoundary.java`、`ExportResultsResponse.java` | 定义成功/失败输出；成功包含保存位置与条数；核心层不依赖 Swing |
| 同目录 `ExportResultsInteractor.java` | 通过 `SessionContext` 获取本人账号，读取 `ProgressRepository`，选择本人已完成测验并排序，调用导出端口和输出边界 |
| 同目录 `ExportResultsRow.java` | 导出用普通数据对象；至少含完成时间、主题、正确数、总题数 |
| `src/main/java/com/wordtrail/usecase/port/QuizResultsExporter.java` | 已有文件导出端口：`void export(String destination, List<ExportResultsRow> rows)` |
| `src/main/java/com/wordtrail/data/CsvQuizResultsExporter.java` | 实现 UTF-8 CSV；正确转义逗号、引号和换行；先写同目录临时文件，完整成功后再原子替换；失败清理临时文件并保留旧目标；文件 I/O 不进入 Interactor |
| `src/main/java/com/wordtrail/adapter/exportresults/` | 新增 `ExportResultsController`、`ExportResultsPresenter`、自己的 ViewModel；Presenter 实现该用例的输出边界 |
| `src/main/java/com/wordtrail/view/ExportResultsPanel.java` | 独立小面板：导出按钮、文件选择、覆盖确认、成功/错误显示；文件选择在 Swing 层处理 |
| `src/main/java/com/wordtrail/app/Application.java`、`view/HomePanel.java`，必要时 `view/AppPanel.java` | 仅做构造依赖与加入独立页签的接线；保留其他功能 |
| `src/test/java/com/wordtrail/` 下相应包 | 写 Interactor、CSV adapter、Presenter 的行为测试；新增核心代码应覆盖成功和失败分支 |

时间用明确的格式，例如带 `Z` 的 UTC ISO 时间；表头写清楚 `completed_at_utc,topic,correct,total`。只导出这些成绩字段，不导出账号密码、密码散列或其他人的记录。操作通过现有后台任务机制运行，界面不因文件操作卡住。

### 三个实施阶段，每轮最多 30 分钟，可分多轮完成

1. **第一阶段已完成：用例核心和 8 个行为测试。** 输入/输出边界、Row/Response、导出端口及 Interactor 已通过 PR #4 合并。假导出器测试覆盖账号隔离、排序、空成绩和失败输出；这不表示实际 CSV 或 UI 已完成。[第一轮页面](FIRST_TASK_FUXUAN.zh-CN.md) 仅供参考。
2. **第二阶段待分工回复：CSV 文件实现和有效测试。** 确认后实现导出 adapter，测试普通成绩、包含逗号/引号/换行的主题、空记录以及写入失败时保留旧文件，保留已有核心和测试。提交消息可用 `Implement CSV export and failure handling`。
3. **第三阶段：接 UI，验证完整流程。** 完成专属 Controller/Presenter/Panel 和少量接线，运行 `mvn verify`，注册测试账号并完成一场测验，实际导出、打开 CSV 检查内容。本人检查结果，更新 README 和用户故事的实际实现状态。后续新工作创建新的 PR，不更新已合并的 PR #4。

一个阶段没有完成，就下一轮继续，不跳过理解和测试。若课程要求的覆盖率检查失败，补充与行为相关的测试，不降低阈值或把新类排除来让检查变绿。

### 验收清单与本人解释

- [ ] 本账号两场已完成测验导出两行，时间从早到晚；未完成测验不出现。
- [ ] 换另一个账号导出，不包含前一个账号的成绩。
- [ ] 无成绩时只有表头，界面显示 0 条。
- [ ] CSV 中含逗号、引号、换行的主题仍为一个正确字段。
- [ ] 取消、拒绝覆盖、写入失败时不显示成功；旧文件保留，失败临时文件清理；导出不会更改学习进度。
- [ ] 能指出请求经过哪个 Controller、Interactor、输出边界、Presenter 和文件 adapter。
- [ ] 能解释为什么 Interactor 不直接调用 Swing 或 `Files.write`，以及失败测试在验证什么。

### 历史 AI 提示词（已停用，不作为当前任务执行）

以下提示词在后续分工确认后使用；当前先等待 Fuxuan 回复，不开始 CSV 实现。

```text
我是在 CSC207 项目中负责 MVP 用例“导出本人已完成测验成绩 CSV”的 Fuxuan。第一轮核心和 8 个测试已经通过 PR #4 合并。先核实后续分工是否已确认；没有确认时只说明现有进度，不开始 CSV 工作。确认后在我的 clone 内按约定的后续分支继续，检查它基于最新交付分支；不碰别的 team-project 目录、不改 Git 身份、不登录他人账号。

先读 docs/TEAM_TASKS.zh-CN.md 中我的任务、README、docs/USER_STORIES.md、docs/ARCHITECTURE.md，以及最新的 Application、HomePanel、专属 Controller/Presenter 结构。当前基线可能已重构，不依赖旧 AppController 内部。我的功能尚未完成，请不要编造完成或测试记录。

不要重复第一轮，按确认后的分工继续本任务单的后续阶段；一个阶段可以分多轮，每轮从准备开始最多 30 分钟。可以帮我生成、修改、运行和测试当前轮代码，同时先说明输入、输出、业务规则及修改原因，让我检查和提问。我负责主导自己的任务并理解最终实现；我检查差异和测试结果、确认内容后，你可以使用我的正常 Git 身份协助 commit、push 和创建新 Draft PR。保留实际贡献归属。到时间就报告真实进度，不保证三轮完成。

实现目标：遵循 docs/USE_CASES.md 的 UC-09。当前登录账号选择位置后导出已完成测验，UTF-8 CSV，列 completed_at_utc/topic/correct/total，时间从早到晚；空记录导出表头及 0 条；取消不写文件；覆盖需本人明确选择；失败显示错误；不泄露别的账号、不修改进度。先写同目录临时文件，完整成功后再原子替换；不支持原子替换时报告失败并保留旧目标，清理临时文件，不以截断旧文件作为后备方案。

复用已有 ExportResultsInputBoundary/OutputBoundary/Response/Row/Interactor 和 QuizResultsExporter 端口，不重写已合并的核心。后续按确认分工实现 CsvQuizResultsExporter、adapter/exportresults 专属 Controller/Presenter/ViewModel、ExportResultsPanel。只有组合根和首页添加必要接线。核心层无 Swing 或文件 I/O；文件操作使用 adapter；界面后台任务和 EDT 按现有工程模式处理。

按当前阶段实际范围测试账号隔离、排序、空记录、CSV 转义、取消、拒绝覆盖、失败输出、失败时保留旧文件及清理临时文件；保持已有测试和覆盖率检查，不关闭它们。核心测试已合并，后续完成 UI 后再实际导出、打开文件验证。运行 mvn verify，让我检查结果并能复述一次调用流程。每轮结束说明真实完成内容、未完成项、测试结果，让我检查 git diff；我确认后，可以协助提交/push，并创建 base 为 Emilia/project-delivery 的新 Draft PR。不要使用他人身份、冒充他人签名或审核，也不要编造贡献或测试结果。现在先核对后续分工是否已确认。
```

## Xinyu：生成今日到期复习清单

### 用例规格

- **Name：** Generate today's due-review plan。
- **Primary actor：** 已登录学习者。
- **Preconditions：** 学习者已登录，系统能够读取其可见词汇和个人复习记录。
- **Trigger：** 学习者请求生成今天的复习清单，并指定主题和最多单词数。
- **Success outcome：** 系统显示符合条件的到期单词、个人当前释义和安排原因；现有学习记录不因生成清单改变。
- **Main success scenario：**
  1. 学习者选择主题或所有主题，并指定 1–20 个单词。
  2. 学习者请求生成清单。
  3. 系统根据今天的日期和该学习者的复习记录判断到期词。
  4. 系统按优先次序选择不超过指定数量的词。
  5. 系统显示清单及每个词入选的原因。
- **Extensions：**
  - 1a. 数量不在 1–20：显示错误，不生成无效结果。
  - 3a. 没有符合条件的词：显示“今天没有符合条件的待复习单词”。
  - 3b. 未登录或读取失败：显示对应错误，不显示其他账号的数据。

首版规则固定且需要在 UI 简单说明：从未复习的词今天可安排；完成过 1 次复习的词距上次复习 1 天后到期，2 次为 3 天，3 次及以上为 7 天。这是可解释的简单间隔规则，不宣称为经过验证的个性化记忆模型。

以应用时区的本地日期计算，到期日期等于今天也算到期。没有 `lastReviewed` 的旧记录按“尚无复习日期”处理，今天可安排。未复习词优先，其余按到期日期从早到晚；相同优先级按词语排序，再以 ID 保持稳定次序。只从该账号可见词汇中选取，使用其个人释义。生成清单不等于完成复习，不更新次数或日期。

### 具体做哪些文件

| 文件或位置 | 你的职责 |
| --- | --- |
| `src/main/java/com/wordtrail/usecase/ReviewPlanInputBoundary.java` | 定义入口，例如 `void execute(String topic, int limit)` |
| 同目录 `ReviewPlanOutputBoundary.java`、`ReviewPlanResponse.java` | 定义清单和失败输出；普通数据，不携带 Swing 控件 |
| 同目录 `ReviewPlanInteractor.java`、`ReviewPlanEntry.java` | 根据账号、主题、日期、复习次数计算、筛选、排序和限制数量；输出词 ID、词语、释义、到期日、原因 |
| `src/main/java/com/wordtrail/adapter/reviewplan/` | 新增 `ReviewPlanController`、`ReviewPlanPresenter`、自己的 ViewModel；Presenter 实现该用例的输出边界 |
| `src/main/java/com/wordtrail/view/ReviewPlanPanel.java` | 独立小面板：主题选择、数量输入、生成按钮、结果表、空状态与错误 |
| `src/main/java/com/wordtrail/app/Application.java`、`view/HomePanel.java`，必要时 `view/AppPanel.java` | 仅构造依赖并加入自己的页签，不重写其他面板 |
| `src/test/java/com/wordtrail/usecase/ReviewPlanInteractorTest.java` 及 adapter 对应测试 | 注入固定 `Clock` 测试日期边界、账号隔离、排序、限制、空结果与失败输出 |

Interactor 依赖已有 `ContentRepository`、`ProgressRepository`、`SessionContext` 和 `Clock`。新 Interactor 放在 `com.wordtrail.usecase` 包内，可以复用 package-private 的 `ContentQueries.visibleWords`；不要改成从 UI 直接访问仓库。不需要新增 JSON 字段，也不需要重写现有 FlashcardInteractor。主题选项可以复用最新基线公开的目录适配接口；先检查它，不访问旧控制器私有字段。

### 三个实施阶段，每轮最多 30 分钟，可分多轮完成

1. **第一阶段：用 AI 实现用例核心和固定日期测试。** 优先按 [Xinyu 第一轮任务](FIRST_TASK_XINYU.zh-CN.md) 操作，实现边界、Entry/Response 和 Interactor，理解“从未复习”“昨天复习过 1 次”“两天前复习过 2 次”的例子。检查通过并确认内容后，可让自己的 AI 协助提交、push、创建 Draft PR，消息可用 `Implement due-review core with fixed-clock tests`。这一阶段未完成就下一轮继续。
2. **第二阶段：补齐规则和失败测试，写专属适配器。** 检查 1/3/7 天边界、没有日期的旧记录、主题、数量、稳定排序、个人词汇和个人释义；使用 AI 协助实现自己的 Controller/Presenter/ViewModel。提交消息可用 `Test review-plan rules and add presentation adapters`。
3. **第三阶段：接独立 UI，实际生成计划。** 使用 AI 协助完成 ReviewPlanPanel 和必要接线。运行 `mvn verify`，用两个测试账号验证隔离，操作界面生成结果；本人检查单击生成不会增加复习次数。更新 README 和用户故事的实际实现状态，然后提交、push，更新已有 PR。

一个阶段没做完时下一轮继续，不用为了“按时完成”省略检查和理解。可以使用 AI 辅助实现和运行，但本人要检查实际结果并能解释。保持所有覆盖率检查；通过固定 Clock 和假的仓库测试分支，不通过真实等待几天来验证日期。

### 验收清单与本人解释

- [ ] 从未复习及没有复习日期的词可以安排；明天才到期的词不出现。
- [ ] 恰好今天到期的词出现；1/3/7 天规则和应用时区计算正确。
- [ ] 主题与数量限制生效，顺序可重复；无结果时有清楚提示。
- [ ] 其他账号私有词不出现；本人修改过的释义正确显示。
- [ ] 连续生成两次，学习次数、日期、成绩和保存的进度均不改变。
- [ ] 能解释为什么注入 Clock，以及为什么在 Interactor 中计算到期而不是在 Swing Panel 中计算。
- [ ] 能从自己的按钮指到 Controller、Interactor、输出边界、Presenter，再指回结果表。

### 历史 AI 提示词（已停用，不作为当前任务执行）

```text
我是在 CSC207 项目中负责新 MVP 用例“生成今日到期复习清单”的 Xinyu。请在我当前 clone 的 team-project-work 内辅导我完成，当前分支应为 Emilia/xinyu-due-review。先核实路径和分支；不碰别的 team-project 目录、不改 Git 身份、不登录他人账号。

先读 docs/TEAM_TASKS.zh-CN.md 中我的任务、README、docs/USER_STORIES.md、docs/ARCHITECTURE.md，以及最新的 Application、HomePanel、专属 Controller/Presenter 结构。基线可能已重构，不依赖旧 AppController 内部。我的功能尚未完成，不要编造完成记录。

先以 docs/FIRST_TASK_XINYU.zh-CN.md 的第一轮范围为准，再按本任务单的三个阶段继续；一个阶段可以分多轮，每轮从准备开始最多 30 分钟。先让我理解边界例子，再协助实现当前轮；可以帮我生成、修改、运行和测试代码，同时解释每项规则、依赖和修改原因，让我检查和提问。我负责主导这项任务并理解最终实现；我检查差异和测试结果、确认内容后，你可以使用我的正常 Git 身份协助 commit、push 和创建 Draft PR。保留实际贡献归属。到时间就报告真实进度，不保证三轮完成。

功能遵循 docs/USE_CASES.md 的 UC-10：已登录用户选主题和 1–20 个词，生成今日到期清单。只使用本人可见词汇及个人释义。没有 lastReviewed 的词今天可安排；完成过 1 次复习的间隔为 1 天，2 次为 3 天，3 次及以上为 7 天；以 Clock 的应用时区本地日期算到期，等于今天也纳入。未复习优先，其余到期日从早到晚，再按词语及 ID 稳定排序，应用主题与数量限制。清单给出词语、释义、到期日及原因。生成不更新学习记录，空结果和错误清楚显示。

按任务单创建 ReviewPlanInputBoundary/OutputBoundary/Response/Entry/Interactor、adapter/reviewplan 专属 Controller/Presenter/ViewModel、ReviewPlanPanel；只对组合根和首页做必要接线。Interactor 放在 com.wordtrail.usecase 以复用 ContentQueries，依赖现有仓库接口、SessionContext、Clock。不要让 UI 直接读仓库，不改 JSON 格式，不重写 flashcard 用例。主题列表使用最新基线公开接口，不访问旧控制器私有字段。

用固定 Clock 和假仓库测试日期边界、稳定排序、数量、主题、空记录、失败、个人释义与账号隔离，并验证生成不保存进度。保持已有测试及覆盖率要求，不关闭它们。第一轮只做核心和测试，后续完成 UI 后再操作两个测试账号验证。运行 mvn verify，让我检查结果并能讲清一次调用流程。每轮结束只记录事实，让我检查 git diff；我确认后，可以协助提交/push，并创建或更新 base 为 Emilia/project-delivery 的 Draft PR。不要使用他人身份、冒充他人签名或审核，也不要编造贡献或测试结果。现在只开始个人任务页的第一轮。
```

## 每轮产生真实进展后：本人检查，可由自己的 AI 协助 commit、push

下面步骤可以由本人操作，也可以在本人检查确认后，由自己的 AI 使用本人正常 Git 身份协助执行。第一次先确认 `git branch --show-current` 是自己的分支。只提交当前轮实际完成、能解释的内容；没有变化时不创建空提交。每轮可以是一小次有意义的进展，不需要人为拖延、改时间或拆分虚假贡献。

```powershell
git branch --show-current
git status --short
git diff --check
git diff
mvn verify
```

只在检查通过后执行下一块。这里的路径限定在源代码及测试，避免误加入个人数据、打包文件、IDE 设置或凭据：

```powershell
git add -- src/main/java src/test/java
git diff --cached --stat
git diff --cached
```

看完暂存差异，确认全是自己这项任务的变更，再提交。例如第一阶段的核心和测试完成时分别使用：

Fuxuan 第一轮命令（历史参考，已完成，不重复执行）：

```powershell
git commit -m "Implement export-results core with behavior tests"
git push -u origin Emilia/xfx-export-results
```

Xinyu：

```powershell
git commit -m "Implement due-review core with fixed-clock tests"
git push -u origin Emilia/xinyu-due-review
```

尚未合并的个人 PR 可在同一分支继续；Fuxuan 的 PR #4 已合并，后续需先确认分工和新分支。commit message 写真实对应内容。第三阶段如果已经更新 README 和用户故事，提交前另执行：

```powershell
git add -- README.md docs/USER_STORIES.md
git diff --cached
```

`git add` 或 `mvn verify` 失败就先处理失败，不连续照抄到 push。不要运行 `git push --force`。别人已经向自己分支推送时，先检查对方改动并协商，不用覆盖来解决。

## 创建个人 PR，并由另一人实际审核

一旦有可审查的真实代码，本人检查确认后可以自己创建 Draft PR，也可以让自己的 AI 使用本人账号协助创建，随后继续在同一分支添加自己的提交。功能和检查齐全后再标为 ready；不需要等到整个项目做完才第一次开 PR。

- Fuxuan 已合并的核心 PR：<https://github.com/fanfan170721/team-project/pull/4>。后续新工作另开 PR，不重复提交核心。
- Xinyu 的比较页面：<https://github.com/fanfan170721/team-project/compare/Emilia%2Fproject-delivery...Emilia%2Fxinyu-due-review?expand=1>

网页上确认 **base 是 `Emilia/project-delivery`，compare 是自己的分支**，Files changed 只包含自己的新增用例及必要接线。若团队交付基线已经合入 `main`，负责人会指导把 base 改为 `main`，再确认差异仍只含自己的功能。这样不会把基线已有实现算作个人新写代码。最终课程证据仍要包含本人实质 PR 合入团队 `main` 的记录，停留在 Draft 或未整合分支不能说已完成。

PR 描述可以由自己的 AI 草拟，经本人检查后按事实发布：实现的用户目标、主要设计、真正跑过的测试、实际验收、尚未完成的项目。不要预先勾选本文件的验收框，也不要填写别人没有给出的审核意见。

两人可以互相作为 reviewer：打开对方 PR，读用例与变更，运行功能或检查测试，再给具体反馈。认同最终版本后才用 `Files changed → Review changes → Approve → Submit review`；有问题就写出来。PR 作者不能批准自己的 PR。不代替他人点击批准，也不把普通聊天“没意见”写成已经进行过代码 review。

## 两个分支的接线冲突怎么处理

两个新功能的核心、adapter、panel、测试文件互不重名，可以分别实施。容易碰到的是 `Application.java`、`HomePanel.java`，以及构造参数需要经过的 `AppPanel.java` 和 README。

两人只增加自己的构造对象、参数和独立页签，不统一重写这些共享文件、不对整个文件自动格式化。开始接线前在群里说明自己正在改哪些文件。合并时同一位置可能出现普通冲突，负责人可以帮助把两套必要接线都保留；不选“全部用我的”覆盖队友功能，不重写队友历史。

需要更新基线或解决冲突时，先保存自己的真实进展并确认 `git status`；让负责人一起看实际差异后再操作。本任务单不让初学者盲目运行 reset、强制 push 或删除目录。每人最终仍应能讲解自己的完整调用链，并亲自完成个人展示、reflection 和 peer evaluation。

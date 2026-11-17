# 两位组员的下一步：各完成一个新的 MVP 用例

**第一次开始请只看 [START_HERE：最多 30 分钟](START_HERE.zh-CN.md)。**下面是后续工作参考，不要求现在通读或一次完成。每次最多工作 30 分钟，一个实施阶段可以分多次完成。

这是一份待执行任务单，不是完成记录。以下两项纳入本项目的 **MVP**，不是 optional 或留到未来的功能：

| 实施负责人 | 新用户故事与用例 | 自己的工作分支 |
| --- | --- | --- |
| Fuxuan | 导出本人已完成测验的成绩 CSV | `Emilia/xfx-export-results` |
| Xinyu | 根据复习记录生成今日到期复习清单 | `Emilia/xinyu-due-review` |

分工不等于代码贡献已经产生。每人应实际主导自己的 Interactor、相关接口、专属 Controller/Presenter、触发操作与结果显示的 UI，并亲自检查、运行、提交、讲解。不要把已经存在的整套实现重新署名为自己的代码，也不要替另一位组员完成其用例。

可以让自己的 AI 帮助生成、修改、测试和解释代码，再由本人检查、运行和提交；如实说明 AI 做了什么，不把生成代码称为全部本人手写。已读项目页面没有单列完整的 AI 使用政策，本任务单不添加“只能手写”的限制，也不替课程承诺所有 AI 使用方式均获批准。正式用例规格见 `docs/USE_CASES.md` 的 **UC-09/UC-10**；两项目前均为 assigned、pending。

本任务单依据 2026-11-17 保存的课程要求整理，负责人电脑上的完整材料位于 `C:\Users\Administrator\Documents\Codex\CSC207-project-requirements\2026-11-17\`。课程要求每人主导 MVP 用例、提交实质代码 PR、review 他人、参与展示；测试数不等于覆盖率。这里的三段是每次不超过 30 分钟的工作安排，不保证基础薄弱者 90 分钟内一定做完。计时结束可以停在当前段，下次继续，不需要赶时间制造完成记录。

## 先用自己的账号和自己的新文件夹

两个人分别在自己的电脑上操作。代码已经有运行基础；不要操作别人现有的 `team-project` 目录。仓库是：

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

Fuxuan 接着执行：

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

以下路径均相对于新 clone 的项目根目录。先读最新基线，再新增自己专属的实现；不依赖旧 `AppController` 的内部字段。

| 文件或位置 | 你的职责 |
| --- | --- |
| `src/main/java/com/wordtrail/usecase/ExportResultsInputBoundary.java` | 定义导出入口，例如 `void execute(String destination)` |
| 同目录 `ExportResultsOutputBoundary.java`、`ExportResultsResponse.java` | 定义成功/失败输出；成功包含保存位置与条数；核心层不依赖 Swing |
| 同目录 `ExportResultsInteractor.java` | 通过 `SessionContext` 获取本人账号，读取 `ProgressRepository`，选择本人已完成测验并排序，调用导出端口和输出边界 |
| 同目录 `ExportResultsRow.java` | 导出用普通数据对象；至少含完成时间、主题、正确数、总题数 |
| `src/main/java/com/wordtrail/usecase/port/QuizResultsExporter.java` | 文件导出端口，例如 `void write(String destination, List<ExportResultsRow> rows)` |
| `src/main/java/com/wordtrail/data/CsvQuizResultsExporter.java` | 实现 UTF-8 CSV；正确转义逗号、引号和换行；先写同目录临时文件，完整成功后再原子替换；失败清理临时文件并保留旧目标；文件 I/O 不进入 Interactor |
| `src/main/java/com/wordtrail/adapter/exportresults/` | 新增 `ExportResultsController`、`ExportResultsPresenter`、自己的 ViewModel；Presenter 实现该用例的输出边界 |
| `src/main/java/com/wordtrail/view/ExportResultsPanel.java` | 独立小面板：导出按钮、文件选择、覆盖确认、成功/错误显示；文件选择在 Swing 层处理 |
| `src/main/java/com/wordtrail/app/Application.java`、`view/HomePanel.java`，必要时 `view/AppPanel.java` | 仅做构造依赖与加入独立页签的接线；保留其他功能 |
| `src/test/java/com/wordtrail/` 下相应包 | 写 Interactor、CSV adapter、Presenter 的行为测试；新增核心代码应覆盖成功和失败分支 |

时间用明确的格式，例如带 `Z` 的 UTC ISO 时间；表头写清楚 `completed_at_utc,topic,correct,total`。只导出这些成绩字段，不导出账号密码、密码散列或其他人的记录。操作通过现有后台任务机制运行，界面不因文件操作卡住。

### 后续三个实施阶段，每次只推进最多 30 分钟

1. **第一段：搞清楚入口，写自己的边界和导出核心。** 读 `QuizAttempt`、`ProgressRepository`、`SessionContext` 和一个现有专属 Controller/Presenter。自己实现输入/输出边界、Row/Response、导出端口及 Interactor；先用假导出器检查数据归属、排序和空记录。解释清楚后按下面“每段提交”提交真实进展，消息可用 `Add export-results use case and boundaries`。
2. **第二段：做 CSV 文件实现和有效测试。** 实现导出 adapter，测试普通成绩、包含逗号/引号/换行的主题、空记录以及写入失败时保留旧文件；补齐 Interactor 的失败输出测试。提交消息可用 `Implement CSV export and failure handling`。
3. **第三段：接自己的 UI，亲手走完流程。** 完成自己的 Controller/Presenter/Panel 和少量接线，运行 `mvn verify`，注册测试账号并完成一场测验，实际导出、打开 CSV 检查内容。更新 README 和用户故事中的实现状态，只写实际做到的内容。然后提交、push、创建 PR。

第一或第二段没有完成，就继续该段，不跳过理解和测试。若课程要求的覆盖率检查失败，补充与行为相关的测试，不降低阈值或把新类排除来让检查变绿。

### 验收清单与本人解释

- [ ] 本账号两场已完成测验导出两行，时间从早到晚；未完成测验不出现。
- [ ] 换另一个账号导出，不包含前一个账号的成绩。
- [ ] 无成绩时只有表头，界面显示 0 条。
- [ ] CSV 中含逗号、引号、换行的主题仍为一个正确字段。
- [ ] 取消、拒绝覆盖、写入失败时不显示成功；旧文件保留，失败临时文件清理；导出不会更改学习进度。
- [ ] 能指出请求经过哪个 Controller、Interactor、输出边界、Presenter 和文件 adapter。
- [ ] 能解释为什么 Interactor 不直接调用 Swing 或 `Files.write`，以及失败测试在验证什么。

### 可以发给自己 AI 的完整提示词

```text
我是在 CSC207 项目中负责新 MVP 用例“导出本人已完成测验成绩 CSV”的 Fuxuan。请在我当前 clone 的 team-project-work 内辅导我完成，当前分支应为 Emilia/xfx-export-results。先核实路径和分支；不碰别的 team-project 目录、不改 Git 身份、不登录他人账号。

先读 docs/TEAM_TASKS.zh-CN.md 中我的任务、README、docs/USER_STORIES.md、docs/ARCHITECTURE.md，以及最新的 Application、HomePanel、专属 Controller/Presenter 结构。当前基线可能已重构，不依赖旧 AppController 内部。我的功能尚未完成，请不要编造完成或测试记录。

请按任务单的三个工作段辅导和实施，一次最多安排 30 分钟。可以帮我生成和修改当前段代码，同时先说明输入、输出、业务规则及修改原因，让我检查、运行和提问。我负责主导这项任务、理解最终实现并亲自提交。实际使用了多少 AI 辅助就如实记录多少，不把生成代码冒充全部本人手写；项目材料没有单独确认完整的课程 AI 政策，不要编造老师的允许或禁止。

实现目标：遵循 docs/USE_CASES.md 的 UC-09。当前登录账号选择位置后导出已完成测验，UTF-8 CSV，列 completed_at_utc/topic/correct/total，时间从早到晚；空记录导出表头及 0 条；取消不写文件；覆盖需本人明确选择；失败显示错误；不泄露别的账号、不修改进度。先写同目录临时文件，完整成功后再原子替换；不支持原子替换时报告失败并保留旧目标，清理临时文件，不以截断旧文件作为后备方案。

按任务单创建 ExportResultsInputBoundary/OutputBoundary/Response/Row/Interactor、QuizResultsExporter 端口、CsvQuizResultsExporter、adapter/exportresults 专属 Controller/Presenter/ViewModel、ExportResultsPanel。只有组合根和首页添加必要接线。核心层无 Swing 或文件 I/O；文件操作使用 adapter；界面后台任务和 EDT 按现有工程模式处理。

测试账号隔离、排序、空记录、CSV 转义、取消、拒绝覆盖、失败输出、失败时保留旧文件及清理临时文件；保持已有测试和覆盖率检查，不关闭它们。让我亲自运行 mvn verify、操作 UI 导出、打开文件并复述一次调用流程。每段结束说明真实完成内容、未完成项、测试结果，让我本人检查 git diff 并提交/push。不要替我 commit、push、签名、提交 review 或合并 PR；不要生成虚假贡献说明。现在只开始第一段。
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

### 后续三个实施阶段，每次只推进最多 30 分钟

1. **第一段：先手算例子，再写用例核心。** 用纸或注释算清楚“从未复习”“昨天复习过 1 次”“两天前复习过 2 次”的安排。读 `LearningProgress` 和 `ContentQueries`，自己实现边界、Entry/Response 和 Interactor。先用固定日期测试到期筛选，再提交真实进展，消息可用 `Add due-review planning use case`。
2. **第二段：补齐规则和失败测试，写专属适配器。** 检查 1/3/7 天边界、没有日期的旧记录、主题、数量、稳定排序、个人词汇和个人释义；实现自己的 Controller/Presenter/ViewModel。提交消息可用 `Test review-plan rules and add presentation adapters`。
3. **第三段：接独立 UI，实际生成计划。** 完成 ReviewPlanPanel 和必要接线。运行 `mvn verify`，用两个测试账号验证隔离，操作界面生成结果；确认单击生成不会增加复习次数。更新 README 和用户故事的真实实现状态，然后提交、push、创建 PR。

一段没做完时继续该段，不用为了“按时完成”省略检查和理解。可以使用 AI 辅助实现，但本人要实际运行并解释结果。保持所有覆盖率检查；通过固定 Clock 和假的仓库测试分支，不通过真实等待几天来验证日期。

### 验收清单与本人解释

- [ ] 从未复习及没有复习日期的词可以安排；明天才到期的词不出现。
- [ ] 恰好今天到期的词出现；1/3/7 天规则和应用时区计算正确。
- [ ] 主题与数量限制生效，顺序可重复；无结果时有清楚提示。
- [ ] 其他账号私有词不出现；本人修改过的释义正确显示。
- [ ] 连续生成两次，学习次数、日期、成绩和保存的进度均不改变。
- [ ] 能解释为什么注入 Clock，以及为什么在 Interactor 中计算到期而不是在 Swing Panel 中计算。
- [ ] 能从自己的按钮指到 Controller、Interactor、输出边界、Presenter，再指回结果表。

### 可以发给自己 AI 的完整提示词

```text
我是在 CSC207 项目中负责新 MVP 用例“生成今日到期复习清单”的 Xinyu。请在我当前 clone 的 team-project-work 内辅导我完成，当前分支应为 Emilia/xinyu-due-review。先核实路径和分支；不碰别的 team-project 目录、不改 Git 身份、不登录他人账号。

先读 docs/TEAM_TASKS.zh-CN.md 中我的任务、README、docs/USER_STORIES.md、docs/ARCHITECTURE.md，以及最新的 Application、HomePanel、专属 Controller/Presenter 结构。基线可能已重构，不依赖旧 AppController 内部。我的功能尚未完成，不要编造完成记录。

按任务单的三个工作段辅导和实施，一次最多安排 30 分钟。先让我手算边界例子，再协助实现当前段；可以帮我生成和修改代码，同时解释每项规则、依赖和修改原因，让我检查、运行和提问。我负责主导这项任务并理解最终实现。实际使用了多少 AI 辅助就如实记录多少，不把生成代码冒充全部本人手写；不要编造课程关于 AI 使用的允许或禁止。

功能遵循 docs/USE_CASES.md 的 UC-10：已登录用户选主题和 1–20 个词，生成今日到期清单。只使用本人可见词汇及个人释义。没有 lastReviewed 的词今天可安排；完成过 1 次复习的间隔为 1 天，2 次为 3 天，3 次及以上为 7 天；以 Clock 的应用时区本地日期算到期，等于今天也纳入。未复习优先，其余到期日从早到晚，再按词语及 ID 稳定排序，应用主题与数量限制。清单给出词语、释义、到期日及原因。生成不更新学习记录，空结果和错误清楚显示。

按任务单创建 ReviewPlanInputBoundary/OutputBoundary/Response/Entry/Interactor、adapter/reviewplan 专属 Controller/Presenter/ViewModel、ReviewPlanPanel；只对组合根和首页做必要接线。Interactor 放在 com.wordtrail.usecase 以复用 ContentQueries，依赖现有仓库接口、SessionContext、Clock。不要让 UI 直接读仓库，不改 JSON 格式，不重写 flashcard 用例。主题列表使用最新基线公开接口，不访问旧控制器私有字段。

用固定 Clock 和假仓库测试日期边界、稳定排序、数量、主题、空记录、失败、个人释义与账号隔离，并验证生成不保存进度。保持已有测试及覆盖率要求，不关闭它们。让我亲自运行 mvn verify、操作两个测试账号并讲清一次调用流程。每段结束只记录事实，让我检查 git diff 后亲自提交/push。不要替我 commit、push、签名、review 或 merge，也不编造个人贡献。现在只开始第一段。
```

## 每一段真实完成后：本人检查、commit、push

下面步骤由该分支的本人操作。第一次先确认 `git branch --show-current` 是自己的分支。只提交当前段实际写过、能解释的内容；没有变化时不创建空提交。每段可以是一小次有意义的进展，不需要人为拖延、改时间或拆分虚假贡献。

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

看完暂存差异，确认全是自己这项任务的变更，再提交。例如第一段分别使用：

Fuxuan：

```powershell
git commit -m "Add export-results use case and boundaries"
git push -u origin Emilia/xfx-export-results
```

Xinyu：

```powershell
git commit -m "Add due-review planning use case"
git push -u origin Emilia/xinyu-due-review
```

第二、三段将 commit message 改成真实对应内容，仍 push 到自己同一分支。第三段如果已经更新 README 和用户故事，提交前另执行：

```powershell
git add -- README.md docs/USER_STORIES.md
git diff --cached
```

`git add` 或 `mvn verify` 失败就先处理失败，不连续照抄到 push。不要运行 `git push --force`。别人已经向自己分支推送时，先检查对方改动并协商，不用覆盖来解决。

## 创建个人 PR，并由另一人实际审核

一旦有可审查的真实代码，可以先开 Draft PR，随后继续在同一分支添加自己的提交。功能和检查齐全后再标为 ready；不需要等到整个项目做完才第一次开 PR。

- Fuxuan 的比较页面：<https://github.com/fanfan170721/team-project/compare/Emilia%2Fproject-delivery...Emilia%2Fxfx-export-results?expand=1>
- Xinyu 的比较页面：<https://github.com/fanfan170721/team-project/compare/Emilia%2Fproject-delivery...Emilia%2Fxinyu-due-review?expand=1>

网页上确认 **base 是 `Emilia/project-delivery`，compare 是自己的分支**，Files changed 只包含自己的新增用例及必要接线。若团队交付基线已经合入 `main`，负责人会指导把 base 改为 `main`，再确认差异仍只含自己的功能。这样不会把基线整套 AI-assisted 实现算作个人新写代码。最终课程证据仍要包含本人实质 PR 合入团队 `main` 的记录，停留在 Draft 或未整合分支不能说已完成。

PR 描述由本人按事实填写：实现的用户目标、主要设计、真正跑过的测试、手动验收、AI 辅助范围、尚未完成的项目。不要预先勾选本文件的验收框，也不要填写别人没有给出的审核意见。

两人可以互相作为 reviewer：打开对方 PR，读用例与变更，运行功能或检查测试，再给具体反馈。认同最终版本后才用 `Files changed → Review changes → Approve → Submit review`；有问题就写出来。PR 作者不能批准自己的 PR。不代替他人点击批准，也不把普通聊天“没意见”写成已经进行过代码 review。

## 两个分支的接线冲突怎么处理

两个新功能的核心、adapter、panel、测试文件互不重名，可以分别实施。容易碰到的是 `Application.java`、`HomePanel.java`，以及构造参数需要经过的 `AppPanel.java` 和 README。

两人只增加自己的构造对象、参数和独立页签，不统一重写这些共享文件、不对整个文件自动格式化。开始接线前在群里说明自己正在改哪些文件。合并时同一位置可能出现普通冲突，负责人可以帮助把两套必要接线都保留；不选“全部用我的”覆盖队友功能，不重写队友历史。

需要更新基线或解决冲突时，先保存自己的真实进展并确认 `git status`；让负责人一起看实际差异后再操作。本任务单不让初学者盲目运行 reset、强制 push 或删除目录。每人最终仍应能讲解自己的完整调用链，并亲自完成个人展示、reflection 和 peer evaluation。

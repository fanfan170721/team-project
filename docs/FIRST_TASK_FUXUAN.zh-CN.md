# Fuxuan 当前任务：完成 UC-09 测验成绩导出的完整功能

**2026-11-21：本页开发任务已完成，下面提示词仅作历史参考，不要重复执行。**你对 [PR #6](https://github.com/fanfan170721/team-project/pull/6) 的正式审核已记录；完整导出界面、适配器、接线和验收测试已通过 [PR #9](https://github.com/fanfan170721/team-project/pull/9) 合并（`f968b44`）。交付分支也已整合 Xinyu 的 #11。保留真实演示记录，准备本人讲解；后续共享审核以 [HANDOFF](HANDOFF.md) 和更新后的 [PR #10](https://github.com/fanfan170721/team-project/pull/10) 为准。

## 以下为已完成任务的历史提示词

**当前安排（2026-11-20）：先实际审核 PR #6，再完成自己的导出功能并提交 PR。**预计需要约 **2–3 小时**，这只是工作量估计，不是教授规定，也不保证到点完成；原先的短时限已取消。不需要故意等待或拆出虚假提交，遇到阻碍就报告实际问题和进度。

核心和 8 个测试已通过 [PR #4](https://github.com/fanfan170721/team-project/pull/4) 合并，CSV 写入器和 11 个测试已通过 [PR #8](https://github.com/fanfan170721/team-project/pull/8) 合并。复用这些代码，不重新实现。本轮目标是用户能在正式应用里选择位置、确认覆盖、导出本人成绩并看到结果，而不只是新增一个未接入的面板。

把下面整段交给能访问你自己项目的 AI；可以让 AI 帮助实现、运行和检查，自己需要看过结果并能解释这一条用例。

```text
我是 CSC207 team-project 的 Fuxuan / Xfx689781。本轮完成 UC-09 的 Controller、Presenter、ViewModel、文件选择/覆盖确认、Swing 界面、正式应用接线和有效测试。预计约 2–3 小时，不需要按旧的小步骤停下等下一次派工；不故意拖延，不保证预计时间内完成。代码、注释、测试描述、界面消息和提交/PR 内容用英文，向我解释用中文。遇阻先报告，不编造完成或测试结果。

A. 先完成真实审核，再准备自己的分支

先打开 https://github.com/fanfan170721/team-project/pull/6，查看最新提交的实际 diff、CI 和说明，重点检查启动前保护 target 内用户数据、运行说明及 review-time/topic 修复。让 AI 帮我解释风险和测试证据，我自己确认后，用自己的 GitHub 账号提交有具体依据的审核：确有问题就说明具体位置/触发条件，没有问题才 Approve；不要照抄预设批准语句、冒充别人或声称跑了未运行的测试。已有我的有效审核时检查新提交是否影响结论，不制造重复记录。PR #6 尚未合并不妨碍从当前交付分支开始自己的功能，发现问题先报告。

然后检查 git status、分支、remote 和 Git 姓名/邮箱。remote 应为 https://github.com/fanfan170721/team-project.git，身份应为我本人；不改身份、使用他人账号或重写历史。若已有本地修改、分支或本轮 PR，先检查并延续已有工作，不 reset/clean、覆盖、强制推送或重复创建。尚未开始且工作区干净时，git fetch origin，再从最新 origin/Emilia/project-delivery 创建 Emilia/xfx-export-adapters；已经用这个分支开始就继续用，不为了任务扩大而换名。确认已包含 PR #4 和 PR #8。

B. 读清现有接口，按本用例范围实施

先读 docs/USE_CASES.md 的 UC-09、ExportResultsInputBoundary / OutputBoundary / Response / Interactor、QuizResultsExporter、CsvQuizResultsExporter，以及 PersonalVocabularyController / Presenter / ViewModel 和对应测试。再读 Application、AppController、AppPanel、HomePanel、UiTasks、Refreshable 和 SwingWorkflowTest，说明一条“按钮→Controller→Interactor→CSV→Presenter→界面”的调用链后实施。

预期新增；若文件已存在先接着完善，不覆盖：
- src/main/java/com/wordtrail/adapter/ExportResultsController.java
- src/main/java/com/wordtrail/adapter/ExportResultsPresenter.java
- src/main/java/com/wordtrail/adapter/ExportResultsViewModel.java
- src/main/java/com/wordtrail/view/ExportResultsPanel.java
- src/main/java/com/wordtrail/view/ExportDestinationChooser.java
- src/main/java/com/wordtrail/view/SwingExportDestinationChooser.java
- src/test/java/com/wordtrail/adapter/ExportResultsAdaptersTest.java
- src/test/java/com/wordtrail/view/ExportResultsPanelTest.java
- src/test/java/com/wordtrail/view/SwingExportDestinationChooserTest.java
- src/test/java/com/wordtrail/app/ApplicationExportResultsTest.java

允许的最小接线修改：src/main/java/com/wordtrail/app/Application.java、adapter/AppController.java、view/HomePanel.java；AppPanel.java 仅在确需传递测试用选择器或依赖时最小修改。现有直接构造 AppController 的测试和 SwingWorkflowTest 可以为新构造参数/页签作必要调整，不重写整套测试。共享 ViewModels 不增加本用例状态，使用自己的 ViewModel。不改其他成员功能、JSON 格式或既有核心业务规则；若发现现有核心/CSV 的真实阻挡，先报告具体证据与最小修复建议，不悄悄重写。

C. 完成适配器和显示状态

Controller 依赖 ExportResultsInputBoundary 和自己的 ViewModel，提供 export(String destination)，每次先清旧结果，只调用一次 input.execute(destination)，原样传入路径，让核心处理验证。返回不可变显示结果；参考 PersonalVocabularyController 的同步模式。

Presenter 实现 ExportResultsOutputBoundary 的 prepareSuccessView(ExportResultsResponse) / prepareFailView(String)。成功读取 response.getDestination() / getExportedCount()，结果含 success、destination、exportedCount 和英文 message；0 条也是成功，明确显示 0。失败保留本次错误消息，success=false，清空旧路径/成功文案、计数置 0。ViewModel 沿用 beginRequest / complete / result 思路；用例没有输出时抛清楚的 IllegalStateException，不复用上次成功。返回结果不暴露用例 DTO、实体、仓库或 Swing 对象，后续请求不改变旧结果。

D. 完成可用的文件选择和界面

ExportDestinationChooser 作为小型视图接口，用 Optional<Path> choose(Component parent) 表示“最终已确认的路径或取消”。SwingExportDestinationChooser 使用 JFileChooser 的保存对话框；已存在的目标必须经 JOptionPane 明确确认覆盖，关闭确认框、No 或 Cancel 都返回空，不调用导出。若自动补 .csv 后缀，必须先得出最终路径，再检查并确认该最终文件；显示的路径和实际写入路径一致。文件选择/确认在 Swing EDT，实际 CSV I/O 交给现有后台任务；不在 Presenter/Interactor 弹对话框。为选择/确认保留少量可替换的对话调用，测试可给定选择和确认结果，不打开原生窗口；不要只用一个永远返回成功的假选择器跳过覆盖拒绝的真实判断。

ExportResultsPanel 使用稳定组件名（例如 export.choose、export.result），显示导出按钮、目标/条数和成功/错误/取消状态。开始新请求前清除旧结果；取消或拒绝覆盖时不导出、不报成功，也不留下上次的成功提示。拿到确认路径后通过 UiTasks.run 调用 AppController 的导出入口，成功回调根据结果.success 显示正确状态；意外异常也不能继续显示旧成功。现有后台机制叫 UiTasks，没有 BusyRunner，不另建线程池；UiTasks 会暂时禁用应用控件，完成后恢复。导出按钮不得造成重复后台任务。

Panel 必须 implements Refreshable：HomePanel 会对所选页签作此转换。refresh 不得自动导出，只清理或刷新显示；新账号进入 HomePanel 时不能看到前一账号的导出路径/结果。新增的 Export results 页签对所有已登录账号可见，在现有页签后追加，不改变既有页签顺序。文件选择由用户主动点击触发，不能切页签就弹框或写文件。

E. 真正接入 Application，不留下死代码

Application 使用既有 JsonFileStore 和同一个 SessionContext，构造 ExportResultsViewModel、ExportResultsPresenter、ExportResultsInteractor(store, new CsvQuizResultsExporter(), session, presenter)、ExportResultsController，并注入 AppController。AppController 增加专属控制器字段与薄委托方法，例如 exportResults(String destination)；不要在这里复制 CSV 或业务规则。HomePanel 创建并加入真实 ExportResultsPanel，默认使用 SwingExportDestinationChooser，使正常启动程序即可访问本功能。

Xinyu 同时会在自己的分支添加复习计划功能，可能也修改 Application/AppController/HomePanel。我们各自只新增自己功能所需的字段、构造参数、方法和页签，不重排或大范围格式化共享文件。Allen 最后整合两份功能、解决共享接线冲突；这不代表可以省略自己的接线。若当前基线已经有另一功能，完整保留它。

F. 验证完整行为，更新自己的少量说明

ExportResultsAdaptersTest：传参/调用次数、成功各字段、0 条成功、成功→失败清旧值、失败→成功清旧错、无输出不能复用结果、旧结果不可变。

SwingExportDestinationChooserTest：用可注入的对话结果检查取消、不存在的目标、已有目标的 Yes/No/关闭，以及最终路径一致；不实际弹出窗口。ExportResultsPanelTest：在 EDT 点击真实按钮、运行真实 UiTasks，模拟批准/取消/拒绝覆盖，验证实际是否调用用例、成功/失败/0 条/取消显示、重新点击与刷新无意外导出、失败后无旧成功；用临时目录的已有哨兵文件验证取消和拒绝覆盖后字节不变。测试不读个人数据，不依赖机器语言、权限技巧、音频或外网。

ApplicationExportResultsTest：使用真实 Application + 固定 Clock + 假 DictionaryGateway + @TempDir，从公开账号/测验入口创建两名用户和已完成成绩，经 AppController 导出并读取真实 CSV；验证本人账号隔离、空记录只有表头且数量 0、UTC 列和排序、写入失败返回失败且学习进度不变。保留已有 CSV 原子替换/旧文件保护测试。正式应用中的导出入口和新页签也必须有测试证明已接上；用组件名/页签标题寻找新页签。SwingWorkflowTest 原有固定 6/5 页签数量断言要调整为核对所需页签存在、普通账号没有 Admin，保留其原行为断言，不靠删除测试通过。

运行完整 mvn verify，报告实际测试总数和 Entity/Use Case 行覆盖率；保持核心 100% 门槛，不排除代码、不关闭检查。再用独立临时/演示目录做一次完整可见操作：完成测验→导出新 CSV→检查内容→取消/拒绝覆盖→确认覆盖→切换另一账号验证隔离；已有数据目录不放在 target 下，不操作真实个人数据。未做的手工操作明确写未做，自动测试不能冒充人工演示。

只按实际完成情况更新 README 和 docs/USE_CASES.md 的 UC-09 状态及简短使用说明，保持英文，不把 UC-10 或别人的未完成工作标为已完成。测试可使用各自独立文件，避免往 Xinyu 的测试中加入本功能。非必要不添加依赖或通用框架。

G. 本人检查、提交和交给他人审核

最后展示改动、测试结果、演示结果和未完成事项；让我能解释为什么 CSV I/O 不在核心层、如何确认覆盖、如何只读取本人记录，以及取消/失败如何避免误报成功。本人确认后，可以使用我自己的正常 Git 身份协助 commit、push 到当前功能分支，并创建或更新 base 为 Emilia/project-delivery 的 PR。仍有未完成或未验证项目时保持 Draft，并准确列出；全部验收完成且本人检查后可标 Ready，请另一位成员真实审核，不代替他人签名或批准。不要把已有核心/CSV 提交重新署名，不制造虚假提交时间或贡献。遇到问题就反馈实际阻碍和下一步，不因预计工时已到而假称完成。
```

群里回复即可：`Fuxuan：PR #6 审核结论___；UC-09 已完成___/卡在___；实际测试和演示___；我的 PR___。` 不需要每做一个小动作都等 Allen 分派。

---
## 历史：已完成的第一轮参考（以下不是当前任务）

核心和 8 个测试已通过 PR #4 合入交付分支。下面保留当时的提示和命令供核对；不要再次执行旧分支的提交/推送步骤，也不要重复创建核心 PR。历史文字中的“尚未完成”描述的是当时状态，当前进度以上方为准。

**最多 30 分钟，包含准备与检查时间。**先按 [START_HERE 第 1–3 步](START_HERE.zh-CN.md) 打开自己的新 clone，并切到 `Emilia/xfx-export-results`。已做好就跳过。没完成就下次继续，不要求 30 分钟内保证写完。

把下面整段发给能访问你这个项目的 AI。AI 看不到本地项目时先反馈，不用逐个粘贴文件。

```text
我是 CSC207 team-project 的 Fuxuan / Xfx689781。本轮负责 UC-09“导出本人已完成测验成绩”的核心实现和测试。请协助我实际修改当前项目，第一轮最多工作 30 分钟（包括解释、测试和检查）；到时间或遇阻就报告真实进度，不继续扩展。

先确认 git remote 指向 https://github.com/fanfan170721/team-project.git，当前目录是我的新 clone，分支是 Emilia/xfx-export-results，基于 origin/Emilia/project-delivery。若不符或已有不明未提交修改，先停下说明。不要改 Git 身份、使用他人账号或重写历史。

先读 README 的 MVP、docs/USE_CASES.md 的 UC-09，以及 QuizAttempt、LearningProgress、SessionContext、ProgressRepository 和现有 UseCaseFixture 测试。QuizAttempt 本身表示已完成测验，未完成测验不在 progress.attempts 中。先用不超过五句话解释输入、输出、规则，再实施下面这一轮。

本轮只新增：
src/main/java/com/wordtrail/usecase/ExportResultsInputBoundary.java
src/main/java/com/wordtrail/usecase/ExportResultsOutputBoundary.java
src/main/java/com/wordtrail/usecase/ExportResultsResponse.java
src/main/java/com/wordtrail/usecase/ExportResultsRow.java
src/main/java/com/wordtrail/usecase/ExportResultsInteractor.java
src/main/java/com/wordtrail/usecase/port/QuizResultsExporter.java
src/test/java/com/wordtrail/usecase/ExportResultsInteractorTest.java
若确需其他改动，先解释；不要重写共享类。

入口接收目标位置，核心通过 SessionContext 获取当前账号、通过 ProgressRepository 只读取该账号的 attempts；按完成时间升序转为含 UTC 完成时间、主题、正确数、总题数的普通行对象，调用 QuizResultsExporter 端口。成功通过 OutputBoundary 输出目标位置和条数；空成绩也调用端口并报告 0 条。未登录、空目标、读取或导出失败要有明确失败输出，不报告成功、不保存或改动进度。核心不使用 Swing、文件 I/O 或 HTTP。本轮导出器只定义接口，测试用 fake 实现；实际 CSV 写入器、Controller、Presenter、UI 和接线下一轮再做。

测试必须验证本人账号隔离、不同时间记录的排序、空成绩、未登录、空目标、读取失败、导出失败和进度不变，验证输出内容，不写只检查非 null 的形式测试。复用测试中合适的假仓库，测试保持独立；覆盖新增 DTO 的实际可用字段。遵守已有 Entity/Use Case 100% line coverage，不降低阈值、不排除新类、不关闭检查。

运行完整 mvn verify。若命令行没有 Maven，找已安装 IntelliJ 使用的 Maven 或指导我从 Maven 面板运行 verify；未实际运行就明确说未运行。成功时报告实际测试数和新增核心覆盖率，不沿用旧结果。

最后只给我：改了哪些文件、实际测试结果、未完成事项，以及“为什么只导出本人记录、为什么 Interactor 不直接写文件”的简短解释。先让我检查 git diff 和结果；我确认后，你可以使用我自己的正常 Git 身份协助提交、push，并创建以 Emilia/project-delivery 为 base 的 Draft PR，正文写实际结果及尚缺文件实现和 UI。不要代替其他人审核，不要假称功能界面已经完成。
```

AI 做完后，你看一遍改动和测试结果，并能指出测试如何验证“没有读到另一账号成绩”。不清楚就让 AI 解释该测试。本轮实际文件导出和界面尚未完成。

**检查通过、你也确认内容后，可以让自己的 AI 协助 commit、push 和创建 Draft PR。**如需自己操作，在当前项目 Terminal 一行一行执行下面命令（报错就停）：

```sh
git branch --show-current
git config user.name
git config user.email
git diff --check
git diff --stat
git diff
```

分支应是 `Emilia/xfx-export-results`，Git 姓名和邮箱应是本人正常使用的身份；为空或是别人时先反馈，不借用别人的身份。确认本轮测试通过后暂存并检查：

```sh
git add -- src/main/java/com/wordtrail/usecase/ExportResults*.java src/main/java/com/wordtrail/usecase/port/QuizResultsExporter.java src/test/java/com/wordtrail/usecase/ExportResultsInteractorTest.java
git diff --cached
```

确认暂存内容只属于自己的本轮任务，再执行：

```sh
git commit -m "Implement export-results core with behavior tests"
git push -u origin Emilia/xfx-export-results
```

第一轮已通过 [PR #4](https://github.com/fanfan170721/team-project/pull/4) 完成提交和合并。后续若有确认分工后的新改动，应使用新的 PR，正文填写实际改动、测试结果及尚未完成事项，不重复创建本轮 PR。

群里回复：`Fuxuan：测试通过/卡在___；本轮 PR 链接___。` 30 分钟到了未完成就保留文件、反馈问题，下轮继续。完整 MVP 仍需后续适配器、文件实现、界面和审核。

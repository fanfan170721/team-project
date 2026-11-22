# Xinyu 当前任务：完成今日复习计划的界面、接线与验收

**2026-11-21：本页开发任务已完成，下面提示词仅作历史参考，不要重复执行。**完整复习计划界面、适配器、接线和验收测试已通过 [PR #11](https://github.com/fanfan170721/team-project/pull/11) 合并（`3f4b4f3`），与 Fuxuan 的导出功能整合后 147 项测试及 8 项线上检查通过。保留真实演示记录，准备本人讲解；后续共享审核以 [HANDOFF](HANDOFF.md) 和更新后的 [PR #10](https://github.com/fanfan170721/team-project/pull/10) 为准。

## 以下为已完成任务的历史提示词

**历史任务（2026-11-20）：把 UC-10 做成可以从应用界面实际使用的完整功能，并提交自己的 PR。**核心和 13 个测试已通过 [PR #7](https://github.com/fanfan170721/team-project/pull/7) 合并（`e93804d`），继续复用，不重复第一轮。现在一起完成 Controller / Presenter / ViewModel、Swing 面板、应用接线和有意义的验收测试。

**预计总计 2–3 小时，这是我们的工作量估计，不是教授规定的最短或最长时限，也不保证在这个时间内完成。**不再要求每 30 分钟停下，也不用人为等待；能继续就连续做。遇到真实阻碍或需要休息时保留进度，说明具体卡点和剩余工作。

把下面整段交给能访问你自己项目的 AI；已有本地工作先检查并接着做，不覆盖。沿用 `Emilia/xinyu-review-adapters` 分支名，避免换名字丢失上下文。

```text
我是 CSC207 team-project 的 Xinyu / XinyuuHuang。请协助我完成 UC-10“生成今日到期复习清单”的整个剩余流程：专属适配器、Swing 界面、应用接线、测试和自己的 PR。预计 2–3 小时只是团队的工作量估计，不是课程时限或完成保证；没有 30 分钟强制停止要求，也不要人为等待。完成一段就接着做，遇到真实阻碍再说明原因、当前进度和下一步。

一、检查项目与现有进度
先检查 git status、分支、remote 和 Git 姓名/邮箱。remote 应为 https://github.com/fanfan170721/team-project.git，身份应为我本人；不改身份、借用他人账号或重写历史。若有未提交内容或已有 Emilia/xinyu-review-adapters 分支，先读清楚并继续，不 reset/clean、强制切分支或覆盖。只有工作区干净且该分支不存在时，git fetch origin，再从最新 origin/Emilia/project-delivery 创建 Emilia/xinyu-review-adapters。确认已有 PR #7 的核心；不重复创建旧核心 PR。

先读 docs/USE_CASES.md 的 UC-10、ReviewPlanInputBoundary / OutputBoundary / Response / Entry / Interactor、PersonalVocabularyController / Presenter / ViewModel 及其适配器测试。再读真实 Application、AppController、AppPanel、HomePanel、UiTasks、Refreshable、Ui 和 SwingWorkflowTest。当前后台工具叫 UiTasks，不是 BusyRunner。代码、注释、UI 消息、测试说明和 PR 描述全部英文。

二、文件范围
自己的主要实现与测试文件：
src/main/java/com/wordtrail/adapter/ReviewPlanController.java
src/main/java/com/wordtrail/adapter/ReviewPlanPresenter.java
src/main/java/com/wordtrail/adapter/ReviewPlanViewModel.java
src/main/java/com/wordtrail/view/ReviewPlanPanel.java
src/test/java/com/wordtrail/adapter/ReviewPlanAdaptersTest.java
src/test/java/com/wordtrail/view/ReviewPlanPanelTest.java
src/test/java/com/wordtrail/view/ReviewPlanWorkflowTest.java

允许对 Application.java、adapter/AppController.java、view/HomePanel.java 做必要依赖接线；AppPanel.java 仅在确实需要时少量调整。若 AppController 构造签名改变，可对应修正 LibraryReviewOrderTest 的构造调用；新增页签导致 SwingWorkflowTest 的旧页签数量断言失效时，改为核对实际页签名称和功能仍存在，不能删掉旧功能行为检查来让测试变绿。可以更新 README 与 USER_STORIES 的本用例实际完成状态。不重写 usecase/entity，不修改 JSON 格式，不把新结果塞进共享 ViewModels，不整体格式化共享文件。Fuxuan 也会改组合根和首页；保留他的已有接线，局部冲突可交 Allen 协调整合，不覆盖对方功能。

三、专属 Controller / Presenter / ViewModel
Controller 依赖 ReviewPlanInputBoundary 和自己的 ViewModel，提供 generate(String topic, int limit)，返回不可变显示结果。每次请求先 beginRequest 清除旧结果，再调用 input.execute(topic, limit) 一次，原样传入主题和数量；日期、1/3/7 天间隔、排序、账号隔离、数量验证都留在现有 Interactor。沿用现有同步 Controller 模式，后台执行交给 UI 的 UiTasks。

Presenter 实现已有 presentPlan(ReviewPlanResponse) 和 presentFailure(String)。成功按 response.entries 原顺序转成独立、不可变显示行，含 wordId、term、definition、dueDate、reason；dueDate 用 yyyy-MM-dd，不重新计算日期或时区。结果包含 success、不可变 rows 和英文 message。空结果也是成功，明确提示没有符合条件的到期词并清空旧行；失败必须 success=false、显示本次错误且 rows 为空。新请求没有输出时抛出清楚的 IllegalStateException，不能返回上次计划。结果不暴露实体、Entry/Response、仓库或 Swing 对象，旧结果不能随后续请求变化。

四、Swing 面板与线程
ReviewPlanPanel 放在 com.wordtrail.view，必须 implements Refreshable，因为 HomePanel 切换页签会调用这个接口。只依赖 adapter 与既有 view 工具，不能 import usecase、entity、data 或直接访问仓库。

界面需要英文标题、主题下拉框、1–20 数量输入、Generate plan 按钮、不可编辑结果表和明确的空状态/失败信息。表格显示 Word、Definition、Due date、Reason，保留 Interactor 给出的优先次序，不默认重排为字母顺序；长释义或原因应可读。给关键控件稳定名称，例如 reviewPlan.topic、reviewPlan.limit、reviewPlan.generate、reviewPlan.table、reviewPlan.message，方便无窗口验收。

主题列表通过 AppController.browse(null, "").topics 获取，复用 Ui.TopicOption、Ui.topics 和 Ui.topic；null 表示所有主题，不能把真实名称为 "All topics"、"All" 或空串的显示标签拿来猜测语义。更新下拉选项时避免监听器引发重复请求，能保留当前有效主题就保留。

refresh() 安全加载当前账号的主题并清除可能过期的计划，不增加学习次数，也不偷偷触发两次生成。换主题或数量时清除旧结果或明确要求重新生成，不能把旧表当成新过滤结果。点击 Generate 先清空旧表和旧消息，再提交数量编辑、处理非法文本；输入有效时通过 UiTasks.run 在后台调用 Controller，在 onSuccess 回调中于 EDT 更新表格、状态和本次错误。UiTasks 已串行化请求并恢复控件状态，直接复用，不新建线程框架。预期失败通过 success=false 结果显示；异常由现有错误通道报告，旧数据仍不可残留。

登录切换后必须是当前账号的新面板/新状态。AppPanel 目前每次登录会新建 HomePanel，保留这个行为；不要把上一个账号的 rows、主题或错误放入静态变量。离开再进入页签应安全刷新，之后生成反映当前个人释义与复习记录。生成计划只读，不应触发 studyCard、saveProgress 或改变测验记录。

五、应用接线
在 Application 使用现有 store、session 和注入的 Clock，构造 ReviewPlanViewModel → ReviewPlanPresenter → new ReviewPlanInteractor(store, store, session, clock, presenter) → ReviewPlanController。把 Controller 交给 AppController；Facade 只新增薄的 generateReviewPlan(String topic, int limit) 转发方法，返回自己的显示结果，不放业务规则或直接调用仓库。

在 HomePanel 现有页签之后追加 "Review plan" 页签；放在现有可选 Admin 页签之后，保留 Vocabulary / Flashcards / Quiz / Review later / Progress / Admin 的现有次序。使用现有 AppController 和 UiTasks，不另造 SessionContext/Clock/数据库连接，不通过反射读取 Facade 私有字段。新测试按控件名/页签标题定位，避免再依赖数字索引。

六、有意义的测试与验收
适配器测试使用真实 Controller → Presenter → ViewModel，验证：topic/limit 原样传入且 execute 只调用一次；全部显示字段、日期和原顺序；成功空列表；成功后失败或空结果清除旧行；失败后成功清除旧错误；下一次没有输出会抛异常；列表不可修改且旧结果保持不变。ReviewPlanEntry 构造器是包内可见，非空样例使用真实 ReviewPlanInteractor、固定 Clock 和假仓库生成；通过公开 AuthInteractor 与假账号仓库/密码工具建立测试会话，不用反射、不改核心构造器可见性、不改共享测试夹具绕过边界。

ReviewPlanPanelTest 可以用受控 InputBoundary 输出配合真实适配器，验证点击动作、主题与数量传递、错误/空结果清旧表、重复点击不会并发，以及完成后控件恢复。非空计划样例仍通过公开用例路径产生，不在 view 主代码引入核心依赖。

ReviewPlanWorkflowTest 使用 Application 的现有 Path/DictionaryGateway/Clock 构造器、@TempDir、固定 Clock、假字典和真实 AppPanel，不创建 JFrame、不访问互联网或播放音频。参照 SwingWorkflowTest 在 EDT 创建/操作控件，等待 UiTasks 完成；不是只构造面板后断言非 null。至少通过实际 Generate 按钮验证：
1. 登录后能看到 Review plan 页签，生成有词语、个人释义、日期和原因的计划，显示次序与核心一致。
2. 主题与数量过滤正确，含一个真实名称为 "All topics" 的主题；默认所有主题与该主题本身可以区分。
3. 至少一个固定日期的到期/未到期例子、一个空结果，且空结果/失败不会留下此前成功的行；只验证集成所需例子，不复制全部既有 13 个核心测试。
4. 同一账号连续生成不会改变复习次数、日期、成绩或存储进度，可比较生成前后的持久化进度；先等待 setup/add/review 等准备操作写完，再比较生成本身。
5. 登出并登录另一个账号后不见前一个账号私有词或旧计划；返回个人词编辑后重新生成使用最新释义。
可通过已公开 Controller/应用操作准备数据、关闭后用已有存储适配器设置测试夹具并重新打开；不要在应用持有文件锁时另开同目录存储，也不要直接修改正式数据。界面失败分支可在专属 Panel 测试使用受控输入边界验证；完整工作流保持真实接线。

七、验证与交付
运行完整 mvn verify；保持已有测试、ArchitectureTest 和 Entity/Use Case 100% line coverage，不排除类、不降低阈值、不关闭检查。若命令行没有 Maven，使用已安装 IntelliJ 的 Maven 或 Maven 面板。未实际运行、失败或未跑完必须明说，报告实际结果，不沿用以前的测试数字。必要时使用非个人临时演示数据检查完整界面，不触碰真实学习档案。

完成后给我简短说明：按钮到 Controller/Interactor/Presenter 再回表格的调用链、实际测试结果、可实际操作的效果和仍存在的问题。展示 git diff 让我检查；我确认后，可以使用我的正常 Git 身份协助 commit、push 到 Emilia/xinyu-review-adapters，并创建或更新 base 为 Emilia/project-delivery 的个人 PR。工作未完成时保持 Draft，完整功能和检查齐全后再请求其他成员审核。PR 描述写用户可见行为、设计要点、真正跑过的测试和未完成项；不代替别人批准，也不声称尚未完成的 TA 演示已经发生。无需为凑时长等待或制造提交；遇到真实阻碍就具体报告，不再按 30 分钟强制停工。
```

完成或遇到阻碍时，在群里只回复：`Xinyu：UC-10 界面与接线已完成/卡在___；测试结果___；PR 链接___（未创建就写未创建）。`

---

## 历史：已完成的第一轮参考（以下不是当前任务）

核心和 13 个测试已通过 PR #7 合入交付分支。下面保留当时的提示和命令供核对；不要再次执行旧分支的提交/推送步骤，也不要重复创建核心 PR。历史文字中的“尚未完成”描述的是当时状态，当前进度以上方为准。

**最多 30 分钟，包含准备与检查时间。**先按 [START_HERE 第 1–3 步](START_HERE.zh-CN.md) 打开自己的新 clone，并切到 `Emilia/xinyu-due-review`。已做好就跳过。没完成就下次继续，不要求 30 分钟内保证写完。

把下面整段发给能访问你这个项目的 AI。AI 看不到本地项目时先反馈，不用逐个粘贴文件。

```text
我是 CSC207 team-project 的 Xinyu / XinyuuHuang。本轮负责 UC-10“生成今日到期复习清单”的核心实现和测试。请协助我实际修改当前项目，第一轮最多工作 30 分钟（包括解释、测试和检查）；到时间或遇阻就报告真实进度，不继续扩展。

先确认 git remote 指向 https://github.com/fanfan170721/team-project.git，当前目录是我的新 clone，分支是 Emilia/xinyu-due-review，基于 origin/Emilia/project-delivery。若不符或已有不明未提交修改，先停下说明。不要改 Git 身份、使用他人账号或重写历史。

先读 README 的 MVP、docs/USE_CASES.md 的 UC-10，以及 LearningProgress、ContentQueries、SessionContext、ContentRepository、ProgressRepository 和现有 UseCaseFixture 测试。先用不超过五句话解释输入、输出、规则，再实施下面这一轮。

本轮只新增：
src/main/java/com/wordtrail/usecase/ReviewPlanInputBoundary.java
src/main/java/com/wordtrail/usecase/ReviewPlanOutputBoundary.java
src/main/java/com/wordtrail/usecase/ReviewPlanResponse.java
src/main/java/com/wordtrail/usecase/ReviewPlanEntry.java
src/main/java/com/wordtrail/usecase/ReviewPlanInteractor.java
src/test/java/com/wordtrail/usecase/ReviewPlanInteractorTest.java
若确需其他改动，先解释；不要重写共享类。

Interactor 依赖现有 ContentRepository、ProgressRepository、SessionContext 和注入的 Clock，放在 com.wordtrail.usecase 包以复用 ContentQueries.visibleWords。入口接收主题和数量（1–20，null/空主题表示所有主题）；只使用当前账号可见词汇和该账号个人释义。没有 lastReviewed 的词今天可安排；历史已复习 1 次的间隔为 1 天、2 次为 3 天、3 次及以上为 7 天，以 Clock 的应用时区本地日期计算，恰好今天到期也要选入。未复习词优先，其余按到期日升序，相同优先级再按词语和 ID 稳定排序，然后应用数量限制。遇到有时间但次数缺失等不一致旧记录，要明确一个一致的兼容规则、说明理由并测试，不悄悄修改历史记录。

通过 OutputBoundary 输出普通结果对象，包含词 ID、词语、个人有效释义、到期日和安排原因；空结果是正常的空清单；数量无效、未登录、仓库读取失败通过失败输出表示，不泄露其他账号数据。生成计划不保存或改变学习进度。核心无 Swing、文件 I/O 或 HTTP。本轮不做 Controller/Presenter、界面、接线或 JSON 格式修改。

用固定 Clock 和假仓库做行为测试：从未复习、1/3/7 天的今天与未来边界、跨时区本地日期、旧记录无日期、主题、1–20 限制、稳定排序、空结果、本人个人释义、账号隔离、未登录和读取失败；验证生成两次都没有调用 saveProgress。固定例子“今天 2026-11-17”：从未复习应纳入；11 月 16 日已复习 1 次应纳入；11 月 15 日已复习 2 次尚未到期。遵守已有 Entity/Use Case 100% line coverage，不降低阈值、不排除新类、不关闭检查。

运行完整 mvn verify。若命令行没有 Maven，找已安装 IntelliJ 使用的 Maven 或指导我从 Maven 面板运行 verify；未实际运行就明确说未运行。成功时报告实际测试数和新增核心覆盖率，不沿用旧结果。

最后只给我：改了哪些文件、实际测试结果、未完成事项，以及“为什么注入 Clock、为什么生成计划不能增加复习次数”的简短解释。先让我检查 git diff 和结果；我确认后，你可以使用我自己的正常 Git 身份协助提交、push，并创建以 Emilia/project-delivery 为 base 的 Draft PR，正文写实际结果及尚缺适配器和 UI。不要代替其他人审核，不要假称功能界面已经完成。
```

AI 做完后，你看一遍改动和测试结果，并能用上面的三个日期例子说明谁今天到期。不清楚就让 AI 解释对应测试。本轮界面尚未完成。

**检查通过、你也确认内容后，可以让自己的 AI 协助 commit、push 和创建 Draft PR。**如需自己操作，在当前项目 Terminal 一行一行执行下面命令（报错就停）：

```sh
git branch --show-current
git config user.name
git config user.email
git diff --check
git diff --stat
git diff
```

分支应是 `Emilia/xinyu-due-review`，Git 姓名和邮箱应是本人正常使用的身份；为空或是别人时先反馈，不借用别人的身份。确认本轮测试通过后暂存并检查：

```sh
git add -- src/main/java/com/wordtrail/usecase/ReviewPlan*.java src/test/java/com/wordtrail/usecase/ReviewPlanInteractorTest.java
git diff --cached
```

确认暂存内容只属于自己的本轮任务，再执行：

```sh
git commit -m "Implement due-review core with fixed-clock tests"
git push -u origin Emilia/xinyu-due-review
```

推送成功后，可让 AI 按上面的要求创建 Draft PR；也可打开[本轮比较页面](https://github.com/fanfan170721/team-project/compare/Emilia%2Fproject-delivery...Emilia%2Fxinyu-due-review?expand=1)，确认 base 为 `Emilia/project-delivery`，compare 为自己的分支，选择 **Create draft pull request**。标题可用 `Implement due-review core with fixed-clock tests`，正文填写实际改动、测试结果，以及尚未完成适配器和 UI。

群里回复：`Xinyu：测试通过/卡在___；本轮 PR 链接___。` 30 分钟到了未完成就保留文件、反馈问题，下轮继续。完整 MVP 仍需后续适配器、界面和审核。

# Xinyu 第二轮（当前）：复习计划的 Controller / Presenter / ViewModel

**当前任务（2026-11-20）：只做下面这一轮，最多 30 分钟。**核心和 13 个测试已通过 [PR #7](https://github.com/fanfan170721/team-project/pull/7) 合并（`e93804d`），不要重复第一轮。现在补上自己的输入与显示适配器；Swing 界面和应用接线留到下一轮。

把下面整段交给能访问你自己项目的 AI；已有本地工作先检查并接着做，不覆盖。

```text
我是 CSC207 team-project 的 Xinyu / XinyuuHuang。本轮只做 UC-10 复习计划的 Controller、Presenter、ViewModel 和行为测试，最多工作 30 分钟（含检查、测试）；到时间或遇阻就保留工作、报告实际进度，不保证一轮完成。

先检查 git status、分支、remote 和 Git 姓名/邮箱。remote 应为 https://github.com/fanfan170721/team-project.git，身份应为我本人；不要改身份、借用他人账号或重写历史。若有未提交工作或同名分支，先读清现有内容、说明怎样继续，不 reset/clean、强制切分支或覆盖。工作区干净且无同名分支时，git fetch origin，再从最新 origin/Emilia/project-delivery 创建 Emilia/xinyu-review-adapters。确认已包含 PR #7 的核心代码。

先读 docs/USE_CASES.md 的 UC-10、ReviewPlanInputBoundary / OutputBoundary / Response / Entry，以及 adapter 包的 PersonalVocabularyController、PersonalVocabularyPresenter、PersonalVocabularyViewModel 和 PersonalVocabularyAdaptersTest。代码、注释和界面消息使用英文。只新增以下四个文件；若确需其他文件，先解释，不自行扩展：
src/main/java/com/wordtrail/adapter/ReviewPlanController.java
src/main/java/com/wordtrail/adapter/ReviewPlanPresenter.java
src/main/java/com/wordtrail/adapter/ReviewPlanViewModel.java
src/test/java/com/wordtrail/adapter/ReviewPlanAdaptersTest.java

Controller 依赖 ReviewPlanInputBoundary 和自己的 ViewModel，提供 generate(String topic, int limit) 并返回不可变的显示结果。每次请求先清除上一次结果，再调用 input.execute(topic, limit) 一次，不能重复调用；原样传入主题和数量，让用例负责验证，不复制日期、排序、账号或数量规则。沿用现有同步 Controller 的模式，不增加异步机制。

Presenter 实现现有 ReviewPlanOutputBoundary：presentPlan(ReviewPlanResponse) 和 presentFailure(String)。成功保留 response.entries 的顺序，将每项 wordId、term、definition、dueDate、reason 转为自己的不可变显示行；dueDate 用 yyyy-MM-dd 的 ISO 本地日期字符串，不重新换时区或计算到期日。ViewModel 结果包含 success、不可变的显示行列表和清晰的英文 message。空列表也是成功，明确提示今天没有符合条件的到期词，并清空旧行；失败 success=false，保留本次错误消息，行列表为空，不能显示上次计划。结果不得暴露实体、用例 Entry/Response、仓库或 Swing 对象。

ViewModel 参考 PersonalVocabularyViewModel 的 beginRequest / complete / result：每次请求独立。若用例没有调用输出边界，应抛出清楚的 IllegalStateException，不能返回旧计划。新结果不能修改已返回的旧列表或旧显示行。

用真实 Controller → Presenter → ViewModel 做行为测试：原样传递 topic/limit 且只调用一次；非空计划全部显示字段、日期和原顺序；成功空列表；成功后失败清空旧行；非空成功后空成功清空旧行；失败后成功清除旧错误；成功后下一次无输出时抛异常；结果列表不能被修改且旧结果不随后续请求变化。可用假 InputBoundary 测传参与输出缺失。注意 ReviewPlanEntry 构造器是包内可见：非空样例使用真实 ReviewPlanInteractor、固定 Clock 和假仓库生成，测试账号可通过公开 AuthInteractor 配合假账号仓库/密码工具登录；不要使用反射、改核心构造器可见性或改现有测试夹具来绕过边界。测试检查具体行为，不只断言非 null。

本轮不要修改 usecase/entity、共享 ViewModels、AppController、Application 或 Swing 面板，不接按钮、不改 JSON 格式。保持 1/3/7 天等既有核心规则和覆盖率门槛，不关闭检查。运行完整 mvn verify，报告实际测试数及 Entity/Use Case 覆盖结果；没有运行或未跑完必须明说，不能沿用以前的数据。

最后给我简短说明：四个文件做了什么、测试结果、尚缺 Swing/应用接线，以及为什么失败或空计划不能继续显示旧行。展示 git diff 让我检查；我确认后，可使用我自己的正常 Git 身份协助 commit、push 到 Emilia/xinyu-review-adapters，并创建 base 为 Emilia/project-delivery 的 Draft PR。正文写本轮真实范围、实际测试结果和未完成事项，不宣称界面已可用，不代替别人审核。到 30 分钟未完成就停止扩展，反馈卡点和已完成部分，下轮继续。
```

本轮结束在群里只回复：`Xinyu：适配器测试通过/卡在___；PR 链接___（未创建就写未创建）。` 下一轮才接自己的复习计划界面。

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

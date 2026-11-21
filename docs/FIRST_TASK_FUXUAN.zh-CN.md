# Fuxuan 第二轮（当前）：导出结果的 Controller / Presenter / ViewModel

**当前任务（2026-11-20）：只做下面这一轮，最多 30 分钟。**核心已通过 [PR #4](https://github.com/fanfan170721/team-project/pull/4) 合并，CSV 写入器已通过 [PR #8](https://github.com/fanfan170721/team-project/pull/8) 合并（`cebde6d`）。不要重做这两项。现在补上自己的输入与显示适配器；Swing 界面和应用接线留到下一轮。

把下面整段交给能访问你自己项目的 AI；已有本地工作先检查并接着做，不覆盖。

```text
我是 CSC207 team-project 的 Fuxuan / Xfx689781。本轮只做 UC-09 导出结果的 Controller、Presenter、ViewModel 和行为测试，最多工作 30 分钟（含检查、测试）；到时间或遇阻就保留工作、报告实际进度，不保证一轮完成。

先检查 git status、分支、remote 和 Git 姓名/邮箱。remote 应为 https://github.com/fanfan170721/team-project.git，身份应为我本人；不要改身份、借用他人账号或重写历史。若有未提交工作或同名分支，先读清现有内容、说明怎样继续，不 reset/clean、强制切分支或覆盖。工作区干净且无同名分支时，git fetch origin，再从最新 origin/Emilia/project-delivery 创建 Emilia/xfx-export-adapters。确认已包含 PR #4 和 PR #8 的代码。

先读 docs/USE_CASES.md 的 UC-09、ExportResultsInputBoundary / OutputBoundary / Response，以及 adapter 包的 PersonalVocabularyController、PersonalVocabularyPresenter、PersonalVocabularyViewModel 和 PersonalVocabularyAdaptersTest。代码、注释和界面消息使用英文。只新增以下四个文件；若确需其他文件，先解释，不自行扩展：
src/main/java/com/wordtrail/adapter/ExportResultsController.java
src/main/java/com/wordtrail/adapter/ExportResultsPresenter.java
src/main/java/com/wordtrail/adapter/ExportResultsViewModel.java
src/test/java/com/wordtrail/adapter/ExportResultsAdaptersTest.java

Controller 依赖 ExportResultsInputBoundary 和自己的 ViewModel，提供 export(String destination) 并返回不可变的显示结果。每次请求先清除上一次结果，再调用 input.execute(destination) 一次，不能重复调用；将输入原样交给用例验证，不复制账号检查、CSV 写入或业务规则。沿用现有同步 Controller 的模式，不增加异步机制。

Presenter 实现现有 ExportResultsOutputBoundary：方法名必须是 prepareSuccessView(ExportResultsResponse) 和 prepareFailView(String)。成功通过 response.getDestination() / getExportedCount() 转为 ViewModel 的显示结果，包含 success、destination、exportedCount 和清晰的英文 message；0 条也是成功，明确显示 0，不能显示失败。失败结果 success=false，保留本次错误消息，清空 destination 和旧成功内容，计数置 0。Presenter 不写文件、不访问仓库、不触发 Swing。

ViewModel 参考 PersonalVocabularyViewModel 的 beginRequest / complete / result：结果不可变，每次请求独立。若用例没有调用输出边界，应抛出清楚的 IllegalStateException，不能返回上次成功。Controller 返回的显示结果不暴露用例 Response、实体、仓库或 Swing 对象。

使用假 InputBoundary 驱动真实 Controller → Presenter → ViewModel 做行为测试：准确传递含空格的目标路径且只调用一次；成功的目标位置、数量和消息；0 条成功；先成功再失败不泄漏旧目标/数量；先失败再成功清除旧错误；成功后下一次完全没有输出时抛异常而非复用旧结果；后续请求不能改变此前返回的不可变结果。检查具体字段与调用次数，不只断言非 null。不需要实际写文件或重复 CSV 测试。

本轮不要修改 usecase/entity、CSV 写入器、共享 ViewModels、AppController、Application 或 Swing 面板，不接按钮、不改 JSON 格式。保持现有业务规则和覆盖率门槛，不关闭检查。运行完整 mvn verify，报告实际测试数及 Entity/Use Case 覆盖结果；没有运行或未跑完必须明说，不能沿用以前的数据。

最后给我简短说明：四个文件做了什么、测试结果、尚缺 Swing/应用接线，以及为什么每次请求要清除旧结果。展示 git diff 让我检查；我确认后，可使用我自己的正常 Git 身份协助 commit、push 到 Emilia/xfx-export-adapters，并创建 base 为 Emilia/project-delivery 的 Draft PR。正文写本轮真实范围、实际测试结果和未完成事项，不宣称界面已可用，不代替别人审核。到 30 分钟未完成就停止扩展，反馈卡点和已完成部分，下轮继续。
```

本轮结束在群里只回复：`Fuxuan：适配器测试通过/卡在___；PR 链接___（未创建就写未创建）。` 下一轮才接自己的导出界面。

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

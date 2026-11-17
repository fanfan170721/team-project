# Xinyu 第一轮：用 AI 实现今日复习计划核心

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

运行完整 mvn verify。若命令行没有 Maven，找已安装 IntelliJ 使用的 Maven 或指导我从 Maven 面板运行 verify；未实际运行就明确说未运行。成功时报告实际测试数和新增核心覆盖率，不沿用旧的 47 作为本次结果。

最后只给我：改了哪些文件、实际测试结果、未完成事项、真实 AI 辅助范围，以及“为什么注入 Clock、为什么生成计划不能增加复习次数”的简短解释。先让我检查 git diff 和结果；我确认后，你可以使用我自己的正常 Git 身份协助提交、push，并创建以 Emilia/project-delivery 为 base 的 Draft PR，正文写实际结果及尚缺适配器和 UI。不要代替其他人审核，不要假称功能界面已经完成。
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

推送成功后，可让 AI 按上面的要求创建 Draft PR；也可打开[本轮比较页面](https://github.com/fanfan170721/team-project/compare/Emilia%2Fproject-delivery...Emilia%2Fxinyu-due-review?expand=1)，确认 base 为 `Emilia/project-delivery`，compare 为自己的分支，选择 **Create draft pull request**。标题可用 `Implement due-review core with fixed-clock tests`，正文填写实际改动、测试结果、AI 辅助范围，以及尚未完成适配器和 UI。

群里回复：`Xinyu：测试通过/卡在___；本轮 PR 链接___。` 30 分钟到了未完成就保留文件、反馈问题，下轮继续。完整 MVP 仍需后续适配器、界面和审核。

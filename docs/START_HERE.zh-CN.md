# 第一次只做这些：最多 30 分钟

这次目标是把项目在自己的电脑上检查通过，并看懂自己用例的一个小例子。**这只是准备，不代表已完成代码贡献。**后续再分小步实现自己的用例、测试、提交 PR 和相互审核。

从开始操作起计时 30 分钟。安装、下载或报错占用时间也算在内；到时停下，反馈进度，下一次从停下的位置继续。只使用自己的 GitHub 账号。

## 1. 克隆到新文件夹（约 5 分钟）

在 IntelliJ 欢迎页点击 **Clone Repository / Get from VCS**。已打开项目时，可用 **File → New → Project from Version Control**。

- URL：`https://github.com/fanfan170721/team-project.git`
- Directory：选一个新位置，文件夹名用 `team-project-work`。若同名文件夹已存在，换一个新名字，不覆盖原来的项目。
- 点击 **Clone**，打开刚克隆的项目。需要 GitHub 登录时使用你自己的账号。

若 Git 或 IntelliJ 尚未安装、仓库不能访问，先反馈卡在哪里，本次不必赶着完成剩余步骤。

## 2. 从共同基础创建自己的本地分支（约 3 分钟）

打开这个新项目里的 **Terminal**，只执行自己名字下面的命令。先执行第一行，成功后再执行第二行。

**Fuxuan / Xfx689781：**

```sh
git switch -c Emilia/xfx-export-results origin/Emilia/project-delivery
git branch --show-current
```

第二行应显示 `Emilia/xfx-export-results`。

**Xinyu / XinyuuHuang：**

```sh
git switch -c Emilia/xinyu-due-review origin/Emilia/project-delivery
git branch --show-current
```

第二行应显示 `Emilia/xinyu-due-review`。若提示分支已存在或其他错误，不重复创建、不重置分支，反馈错误即可。

## 3. 检查基础项目（最多用到第 20 分钟）

1. 若提示加载 Maven 项目，点击 **Load Maven Project**。没有提示时，右键根目录的 `pom.xml` → **Add as Maven Project**。
2. 在 **File → Project Structure → Project → SDK** 选择已有的 JDK 11 或 17；都没有时用 **Download JDK** 选择 17。下载没完成可以留到下一次。
3. 打开 **View → Tool Windows → Maven**，展开项目的 **Lifecycle**，双击 **verify**。这样使用 IntelliJ 配置的 Maven，无需先安装命令行 `mvn`。
4. 等待输出里的 **BUILD SUCCESS**。首次下载依赖可能较久；黄色 Checkstyle 提示不等同于构建失败，最终以构建结果为准。

若到第 20 分钟仍在下载或有错误，可以直接把当前状态发群里，本次先停。若出现 **BUILD FAILURE**，发最后约 20 行报错文字；不要删测试、降低覆盖率或随意重装项目来掩盖错误。

## 4. 用剩下的 5–10 分钟看自己的一个例子

### Fuxuan

你负责 **UC-09：导出当前账号的测验成绩为 CSV 文件**。

打开 [`QuizAttempt.java`](../src/main/java/com/wordtrail/entity/QuizAttempt.java)，看最后四个 `get...()` 方法。这个类的一条对象已经代表一场完成的测验，无需另外找“是否完成”字段。

假设当前账号有一场成绩：时间 `2026-11-17T18:00:00Z`，主题 `Travel`，答对 3 题、总共 4 题。目标文件应是：

```csv
completed_at_utc,topic,correct,total
2026-11-17T18:00:00Z,Travel,3,4
```

**看过代码后反馈一句：**这四列分别从哪四个 getter 取值。可以请 AI 解释这个类，自己对照代码确认。本次先不写完整导出功能。

### Xinyu

你负责 **UC-10：生成今天到期的复习清单**。

打开 [`LearningProgress.java`](../src/main/java/com/wordtrail/entity/LearningProgress.java)，只看 `reviewCounts`（已复习次数）、`lastReviewed`（上次复习时间）及它们的 getter。

首版规则：从未复习的词今天可安排；已复习 1 次的间隔为 1 天，2 次为 3 天，3 次及以上为 7 天；到期日等于今天也算到期。这里的次数是历史累计次数。

**固定例子里的“今天”为 2026-11-17：**

- A 从未复习。
- B 已复习 1 次，上次为 11 月 16 日。
- C 已复习 2 次，上次为 11 月 15 日。

**看过代码后反馈一句：**A、B、C 中哪些今天应该出现，分别为什么。可以请 AI 解释字段，自己核对日期。本次先不写完整计划功能。

## 做到这里就停

在群里回复：`我的分支：___；构建：BUILD SUCCESS / 卡在___；我的小例子：___。`

这次未改源文件，无需 commit 或 push。下一次才安排一个小功能及其测试，检查后由本人提交和 push；不会把空提交或环境配置算成实质实现。完整的后续说明在 [TEAM_TASKS](TEAM_TASKS.zh-CN.md)，现在不用通读。

IntelliJ 操作参考：[克隆仓库](https://www.jetbrains.com/help/idea/set-up-a-git-repository.html)、[Maven 工具窗口](https://www.jetbrains.com/help/idea/maven-projects-tool-window.html)。不同版本的按钮文字可能稍有不同。

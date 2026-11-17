# 第一次只做这些：最多 30 分钟

这次目标是准备本地分支，然后**用自己的 AI 开始实现自己用例的核心逻辑和测试**。不要求手写；本人需要检查实际改动、运行结果并说明核心规则。核心完成也不代表整个 MVP 用例完成，后续还有界面、适配器和审核。

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

## 3. 检查基础项目（已检查通过可跳过）

1. 若提示加载 Maven 项目，点击 **Load Maven Project**。没有提示时，右键根目录的 `pom.xml` → **Add as Maven Project**。
2. 在 **File → Project Structure → Project → SDK** 选择已有的 JDK 11 或 17；都没有时用 **Download JDK** 选择 17。下载没完成可以留到下一次。
3. 打开 **View → Tool Windows → Maven**，展开项目的 **Lifecycle**，双击 **verify**。这样使用 IntelliJ 配置的 Maven，无需先安装命令行 `mvn`。
4. 等待输出里的 **BUILD SUCCESS**。首次下载依赖可能较久；黄色 Checkstyle 提示不等同于构建失败，最终以构建结果为准。

下载较久时可以把状态发群里，本次先停；安装与下载也算在 30 分钟内。若出现 **BUILD FAILURE**，发最后约 20 行报错文字；不要删测试、降低覆盖率或随意重装项目来掩盖错误。

## 4. 把自己任务页中的提示词发给自己的 AI

### Fuxuan

你负责 **UC-09：导出当前账号的测验成绩为 CSV 文件**。

打开 [Fuxuan 第一轮任务](FIRST_TASK_FUXUAN.zh-CN.md)，复制提示词。第一轮让 AI 实现“取本人测验成绩、按时间排序、交给导出接口”的核心和测试；实际 CSV 文件与界面留给后续轮次。

### Xinyu

你负责 **UC-10：生成今天到期的复习清单**。

打开 [Xinyu 第一轮任务](FIRST_TASK_XINYU.zh-CN.md)，复制提示词。第一轮让 AI 实现“按复习日期生成今日清单”的核心和测试；专属 Controller/Presenter 与界面留给后续轮次。

## 做到这里就停

有真实代码、检查通过并且本人看过后，按自己任务页的命令提交和 push。在群里回复：`我的分支：___；测试：通过 / 卡在___；提交链接或剩余问题：___。`

30 分钟到时未完成就停，反馈剩余内容，不必制造提交或勾选未完成的验收项。完整的后续说明在 [TEAM_TASKS](TEAM_TASKS.zh-CN.md)，现在不用通读。

IntelliJ 操作参考：[克隆仓库](https://www.jetbrains.com/help/idea/set-up-a-git-repository.html)、[Maven 工具窗口](https://www.jetbrains.com/help/idea/maven-projects-tool-window.html)。不同版本的按钮文字可能稍有不同。

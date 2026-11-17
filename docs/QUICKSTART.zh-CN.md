# 中文启动与修改说明

## 运行程序

项目使用 Java 11、Maven、Java Swing，当前电脑已经安装了 JDK 和 Maven。
在项目根目录终端执行：

```powershell
mvn verify
java -jar target/wordtrail.jar
```

也可以在 IntelliJ IDEA 中打开 `pom.xml`，等待 Maven 导入，选择 JDK 11
或更高版本，然后运行 `src/main/java/com/wordtrail/app/Main.java`。
运行目录保持为项目根目录。

Windows 下也可以直接双击项目根目录的 `run.cmd`，或在 PowerShell 中执行：

```powershell
.\run.ps1
```

启动脚本会固定使用项目根目录，并在源码更新后自动重新构建。需要强制重新编译和
测试时使用 `.\run.ps1 -Build`。`.\run.ps1 -Check` 只检查启动入口，不打开窗口，
也不访问网络。

`mvn verify` 会生成 `target/wordtrail.jar`。它包含运行依赖和示例内容，运行它只需
Java 11 或更高版本。构建产物和本地账户数据不提交到 Git。

第一次打开会要求设置管理员。用户名使用 3–30 位英文字母、数字或下划线，
密码使用 8–128 个字符。用户名不区分大小写；没有预设账户和密码。
设置完成后会自动登录。退出后，将 Action 改为 Register a learner account
即可注册普通学习者。管理员也能使用全部学习功能。

## 各页面怎么用

1. **Vocabulary**：选主题、搜索单词或词义。选中表格行，下方显示解释和例句。
   Save for review later 保存到个人复习列表；在线查询按钮调用真实词典 API。
   **Add my word** 可以添加个人单词；**Edit my definition** 修改选中单词的个人释义。
   新增和修改只影响当前账户，也会用于闪卡和之后新开的测验。
2. **Flashcards**：选主题并 Start deck；勾选 Use review list only 可以只学收藏。
   先回忆词义，再 Reveal definition，最后 **Complete review** 记录一次完成复习。
   每次访问一张卡只能记一次；点击 Next card，之后再回到同一词可以再次记录，
   同一天也可以累计。每天学过的不同单词数仍然去重。
   Next card 切换下一张；到最后一张会循环到第一张。
3. **Quiz**：选择主题和题数。题库不足时使用已有题目。选择一个选项并提交，
   查看反馈后进入下一题。完成最后一题才保存成绩，中途退出不记录。
4. **Review later**：查看和移除当前账户保存的单词。
   Reviews 列显示完成复习次数，Last reviewed 显示最近完成时间。
   Order 可以选择按字母、复习次数从少到多、从多到少或最近复习排序；
   表格列标题也能排序。Vocabulary 中同样提供这些排序方式。
5. **Progress**：查看学过的不同单词数、连续学习天数、最近 14 天每日学习量、
   历次测验及总体正确率。正确率按所有作答题目加权计算。
6. **Admin**：只有管理员有此页。先添加单词，再在 Add quiz question 中关联
   单词、输入题目及四个不同选项，并选择正确答案。主题可以自行新增。
   在线查询可填入释义，保存前可以编辑。

## 数据与 API

默认数据保存在运行目录下 `.wordtrail/wordtrail.json`。关闭程序后复制此文件
即可备份。不要将自己的账户数据提交到 Git；目录已在 `.gitignore` 中排除。
如需单独的演示环境，可以运行：

```powershell
java -jar target/wordtrail.jar --data-dir target/my-demo
```

同一个数据目录只允许打开一个程序实例。数据损坏时程序会报错并保留原文件，
不会偷偷清空。已有数据不会因为重新编译而消失。

主要外部 API 是 [Free Dictionary API](https://dictionaryapi.dev/)。更新时已复现该
服务返回 HTTP 530，所以加入 [Datamuse](https://www.datamuse.com/api/) 作为在线备用来源。
界面会显示实际结果的来源链接，并把你的已保存释义单独列出来；在线查询本身不保存修改。
两种服务都失败时会明确报错，原数据保留。Datamuse 官方预告 2027 年会调整为需要 Key，
届时继续使用需按其最新文档配置或更换备用适配器。
附件中的课程 DAO 用作设计参考，没有照搬其共享 HTTP 服务器及明文密码方式。
本项目通过词典 API 满足 API 要求，通过本地 JSON 满足持久化要求。
断网时 16 个示例单词和 16 道题仍然能学习；在线查询会显示连接错误。

新版本兼容已有的账户和学习记录，不需要删除 `.wordtrail`。
旧记录没有复习次数字段的，次数从本次更新后开始记录，不推算历史次数。
更新后请关闭之前打开的 WordTrail 窗口，再用 `run.cmd` 或 `Main.main()` 重新启动。

可以单独检查真实 API：

```powershell
java -jar target/wordtrail.jar --check-api hello
```

## 之后想改哪里

| 想修改的内容 | 主要位置 |
| --- | --- |
| 页面、文字、布局 | `src/main/java/com/wordtrail/view/` |
| 输入与界面数据转换 | `src/main/java/com/wordtrail/adapter/` |
| 每个功能的业务逻辑 | `src/main/java/com/wordtrail/usecase/` |
| 领域数据 | `src/main/java/com/wordtrail/entity/` |
| 文件、API、密码哈希 | `src/main/java/com/wordtrail/data/` |
| 组装与程序入口 | `src/main/java/com/wordtrail/app/` |
| 初始词库 | `src/main/resources/seed.json` |
| 单元和集成测试 | `src/test/java/com/wordtrail/` |

注意：`seed.json` 只在首次创建数据目录时导入。修改它后若想验证新的初始内容，
请使用一个新的 `--data-dir`，或者在管理员表单中添加，不要直接删除有用的学习记录。

## Git 和团队部分

整合分支是 `Emilia/project-delivery`，保留原有 AI 辅助实现的作者和提交历史，并已
纳入 `main` 上签署完成的 TeamContract。合同 PR #1 已合并。

README 列出实际完成的基线与下一步 MVP 分工。Fuxuan 的 CSV 导出和 Xinyu 的今日
复习计划尚待本人实现；操作步骤见 `TEAM_TASKS.zh-CN.md`。不要把分工表、合同签名
或仅修改文档当成已经完成个人用例。

每次功能改动使用分支、测试和 PR；保留实际审核记录。`mvn verify` 还会产生
覆盖率和 Checkstyle 报告，详见 `VERIFICATION.md` 和 `DEVELOPMENT.md`。

请先读 `ARCHITECTURE.md` 和 `DEMO.md`，能解释代码和设计选择后再用于展示。

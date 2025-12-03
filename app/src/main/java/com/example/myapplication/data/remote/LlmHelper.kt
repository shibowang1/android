package com.example.myapplication.data.remote

// ==========================================
// 模拟 AI 回复的数据库 (为了测试各种 Markdown 效果)
// ==========================================
fun getSimulatedResponse(userQuestion: String): String {
    return if (userQuestion.contains("picture")) {
        """
        好的，这是你要的图片：测试git功能

        ![风景](https://picsum.photos/600/350)

        图片加载需要一点时间，请耐心等待。
        """.trimIndent()
    } else if (userQuestion.contains("code") || userQuestion.contains("Kotlin")) {
        """
        没问题，这是一个使用 **Kotlin** 协程的示例：

        ```kotlin
        import kotlinx.coroutines.*
        //函数
        fun main() = runBlocking {
            launch {
                delay(1000L)
                println("World!")
            }
            print("Hello ")
        }
        ```

        你可以看到 `runBlocking` 和 `launch` 的用法。
        """.trimIndent()
    } else {
        """
        我收到了你的问题：**$userQuestion**


        作为 AI 助手，我可以帮你：
        1. **写代码**：支持高亮和复制。
        2. **看图片**：发送“图片”试试。
        3. **讲笑话**：虽然我可能不太幽默。
        这只是一个流式输出的测试，你会看到这些字**一个接一个**地蹦出来，就像我在实时打字一样！
        """.trimIndent()
    }
}
package com.example.myapplication.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext

// ...
import com.example.myapplication.domain.entities.Message
import com.example.myapplication.data.remote.getSimulatedResponse
import com.example.myapplication.ui.components.ChatBubble
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.text.forEach

//import com.example.myapplication.data.remote.LlmHelper // 假设你把网络库放在了 data.remote

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoubaoScreen() {
    // 消息列表
    val messages = remember {
        mutableStateListOf(
            Message("1", "你好！我是升级版豆包。\n\n我现在支持：\n- **代码高亮**\n- **图片显示**\n- **流式打字效果**\n\n快试着问我点什么吧！", false)
        )
    }
    val context = LocalContext.current

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    // 协程作用域，用于处理延时任务（模拟打字）
    val scope = rememberCoroutineScope()
    // 标记是否正在生成中，防止用户重复点击
    var isGenerating by remember { mutableStateOf(false) }

    // 当消息列表变化时，自动滚动到底部
    LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("豆包 (Module 3 Completed)", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            com.example.myapplication.ui.components.InputArea(
                value = inputText,
                onValueChange = { inputText = it },
                // 只有不在生成时才能发送
                onSend = {
                    if (inputText.isNotBlank() && !isGenerating) {
                        val userText = inputText
                        inputText = "" // 清空输入框
                        isGenerating = true // 锁定状态

                        // 1. 添加用户消息
                        messages.add(Message(System.currentTimeMillis().toString(), userText, true))

                        // 2. 添加一个"空"的 AI 消息占位符
                        val aiMsgId = System.currentTimeMillis().toString() + "_ai"
                        messages.add(Message(aiMsgId, "", false))
                        val aiMsgIndex = messages.lastIndex

                        // 3. 开启协程模拟流式打字
                        scope.launch {
                            val fullResponse = getSimulatedResponse(userText)
                            var currentText = ""

                            // 逐字追加，模拟网络流
                            fullResponse.forEach { char ->
                                delay(20) // 打字速度：20ms 一个字
                                currentText += char
                                // 更新列表中的那条消息
                                messages[aiMsgIndex] = messages[aiMsgIndex].copy(text = currentText)
                            }
                            isGenerating = false // 解锁
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(
                    message = msg,
                    onRegenerate = {
                        // 这里是"重新生成"的业务逻辑
                        // 目前我们只做 UI 演示，实际需调用 ViewModel
                        if (!isGenerating) {
                            // 演示：删除当前这条 AI 消息，并提示
                            // 实际逻辑应该是：拿到上一条用户的提问，重新请求 LlmHelper
                            android.widget.Toast.makeText(
                                context,
                                "正在重新生成...",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()

                            // 这里为了演示效果，你可以把当前这条消息删掉，或者清空内容重新触发流式
                            // 比如: scope.launch { ... 重新调用 LlmHelper ... }
                        }
                    }
                )
            }

        }
    }
}
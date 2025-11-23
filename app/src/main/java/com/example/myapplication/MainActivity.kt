//package com.example.myapplication
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.selection.SelectionContainer
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ContentCopy
//import androidx.compose.material.icons.filled.KeyboardVoice
//import androidx.compose.material.icons.filled.Send
//import androidx.compose.material.icons.outlined.SmartToy
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.AnnotatedString
//import androidx.compose.ui.text.SpanStyle
//import androidx.compose.ui.text.buildAnnotatedString
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.text.font.FontStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import org.commonmark.node.*
//import org.commonmark.parser.Parser
//import coil.compose.AsyncImage // 记得导入
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import org.commonmark.node.BulletList
//import org.commonmark.node.OrderedList
//import org.commonmark.node.ListItem
//// ==========================================
//// 1. Markdown 核心数据模型 (Pipeline 的产物)
//// ==========================================
//
//// 定义 Markdown 的块级结构，UI 渲染器将遍历这个列表
//sealed class MarkdownBlock {
//    // 普通段落（包含富文本：加粗、斜体等）
//    data class Paragraph(val content: AnnotatedString) : MarkdownBlock()
//    // 标题 (#, ##)
//    data class Heading(val content: AnnotatedString, val level: Int) : MarkdownBlock()
//    // 代码块 (```)
//    data class CodeFence(val code: String, val language: String) : MarkdownBlock()
//    // 新增：图片类型
//    data class Image(val url: String, val alt: String) : MarkdownBlock()
//}
//
//// ==========================================
//// 2. Markdown 解析器 (Parser)
//// ==========================================
//
//object MarkdownParser {
//    // 初始化 CommonMark 解析器
//    private val parser: Parser = Parser.builder().build()
//
//    fun parse(markdown: String): List<MarkdownBlock> {
//        val document = parser.parse(markdown)
//        val blocks = mutableListOf<MarkdownBlock>()
//
//        var node = document.firstChild
//        while (node != null) {
//            when (node) {
//                // 1. 普通段落 & 图片处理
//                is org.commonmark.node.Paragraph -> {
//                    val firstChild = node.firstChild
//                    if (firstChild is org.commonmark.node.Image && firstChild.next == null) {
//                        blocks.add(MarkdownBlock.Image(firstChild.destination, firstChild.title ?: ""))
//                    } else {
//                        blocks.add(MarkdownBlock.Paragraph(parseInlineStyles(node)))
//                    }
//                }
//                // 2. 标题处理
//                is org.commonmark.node.Heading -> {
//                    blocks.add(MarkdownBlock.Heading(parseInlineStyles(node), node.level))
//                }
//                // 3. 代码块处理
//                is FencedCodeBlock -> {
//                    blocks.add(MarkdownBlock.CodeFence(node.literal ?: "", node.info ?: ""))
//                }
//
//                // ✅✅✅ 新增：列表处理 (无序 BulletList & 有序 OrderedList) ✅✅✅
//                is BulletList, is OrderedList -> {
//                    var listItem = node.firstChild
//                    var index = 1
//                    while (listItem != null) {
//                        if (listItem is ListItem) {
//                            // 决定前缀：如果是无序列表用 "• "，有序列表用 "1. "
//                            val prefix = if (node is BulletList) "•  " else "$index. "
//
//                            // 列表项里面通常包含一个 Paragraph，我们把它挖出来
//                            var contentNode = listItem.firstChild
//                            while (contentNode != null) {
//                                if (contentNode is org.commonmark.node.Paragraph) {
//                                    // 把前缀和内容拼起来
//                                    val fullText = buildAnnotatedString {
//                                        append(prefix)
//                                        append(parseInlineStyles(contentNode))
//                                    }
//                                    blocks.add(MarkdownBlock.Paragraph(fullText))
//                                }
//                                // 这里简化处理，如果列表里有代码块暂时忽略，防止太复杂
//                                contentNode = contentNode.next
//                            }
//                        }
//                        listItem = listItem.next
//                        index++
//                    }
//                }
//            }
//            node = node.next
//        }
//        return blocks
//    }
//
//    // 递归解析行内样式 (Inline Parser) -> 转换为 Compose 的 AnnotatedString
//    private fun parseInlineStyles(blockNode: Node): AnnotatedString {
//        return buildAnnotatedString {
//            var child = blockNode.firstChild
//            while (child != null) {
//                when (child) {
//                    is Text -> append(child.literal)
//                    is StrongEmphasis -> { // **加粗**
//                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
//                        append(parseInlineStyles(child)) // 递归处理内部文字
//                        pop()
//                    }
//                    is Emphasis -> { // *斜体*
//                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
//                        append(parseInlineStyles(child))
//                        pop()
//                    }
//                    is Code -> { // `行内代码`
//                        pushStyle(SpanStyle(
//                            fontFamily = FontFamily.Monospace,
//                            background = Color(0xFFEFEFEF),
//                            color = Color(0xFFE91E63) // 骚粉色强调
//                        ))
//                        append(child.literal)
//                        pop()
//                    }
//                    is org.commonmark.node.Image -> {
//                        append("[图片: ${child.title}]")
//                    }
//                    is SoftLineBreak, is HardLineBreak -> append("\n")
//                    else -> append(parseInlineStyles(child))
//
//                }
//                child = child.next
//            }
//        }
//    }
//}
//
//
//// ==========================================
//// 新增：简易语法高亮器 (Module 2)
//// ==========================================
//object SimpleSyntaxHighlighter {
//    private val keywords = listOf("fun", "val", "var", "if", "else", "for", "while", "return", "class", "object", "import", "package")
//
//    fun highlight(code: String, language: String): AnnotatedString {
//        return buildAnnotatedString {
//            append(code)
//
//            // 1. 高亮关键字 (橙色)
//            keywords.forEach { keyword ->
//                val regex = "\\b$keyword\\b".toRegex()
//                regex.findAll(code).forEach { result ->
//                    addStyle(SpanStyle(color = Color(0xFFCC7832), fontWeight = FontWeight.Bold), result.range.first, result.range.last + 1)
//                }
//            }
//
//            // 2. 高亮字符串 "..." (绿色)
//            val stringRegex = "\".*?\"".toRegex()
//            stringRegex.findAll(code).forEach { result ->
//                addStyle(SpanStyle(color = Color(0xFF6A8759)), result.range.first, result.range.last + 1)
//            }
//
//            // 3. 高亮数字 (蓝色)
//            val numberRegex = "\\b\\d+\\b".toRegex()
//            numberRegex.findAll(code).forEach { result ->
//                addStyle(SpanStyle(color = Color(0xFF6897BB)), result.range.first, result.range.last + 1)
//            }
//
//            // 4. 高亮注释 //... (灰色)
//            val commentRegex = "//.*".toRegex()
//            commentRegex.findAll(code).forEach { result ->
//                addStyle(SpanStyle(color = Color.Gray, fontStyle = FontStyle.Italic), result.range.first, result.range.last + 1)
//            }
//        }
//    }
//}
//
//// ==========================================
//// 3. Markdown 渲染组件 (Renderer Widgets)
//// ==========================================
//
//
//
//@Composable
//fun MarkdownMessageDisplay(rawText: String, textColor: Color) {
//    val blocks = remember(rawText) { MarkdownParser.parse(rawText) }
//
//    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//        blocks.forEach { block ->
//            when (block) {
//                is MarkdownBlock.Paragraph -> {
//                    SelectionContainer {
//                        Text(
//                            text = block.content,
//                            color = textColor,
//                            fontSize = 16.sp,
//                            lineHeight = 24.sp
//                        )
//                    }
//                }
//                is MarkdownBlock.Heading -> {
//                    Text(
//                        text = block.content,
//                        color = textColor,
//                        fontWeight = FontWeight.Bold,
//                        fontSize = if (block.level == 1) 22.sp else 18.sp
//                    )
//                }
//                is MarkdownBlock.CodeFence -> {
//                    CodeBlockDisplay(code = block.code, language = block.language)
//                }
//                // 新增：图片渲染
//                is MarkdownBlock.Image -> {
//                    AsyncImage(
//                        model = block.url,
//                        contentDescription = block.alt,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clip(RoundedCornerShape(8.dp))
//                            .background(Color.LightGray), // 加载时的占位背景
//                        contentScale = androidx.compose.ui.layout.ContentScale.FillWidth
//                    )
//                }
//            }
//        }
//    }
//}
//
//// 这是一个独立的代码块组件，对应任务中的“代码高亮/折叠/复制组件”基础
//@Composable
//fun CodeBlockDisplay(code: String, language: String) {
//    val context = androidx.compose.ui.platform.LocalContext.current
//    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
//    // 状态：是否展开
//    var isExpanded by remember { mutableStateOf(true) }
//
//    Card(
//        shape = RoundedCornerShape(8.dp),
//        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2B2B)), // 更像 IDE 的深色
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Column {
//            // --- 顶部栏 ---
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(Color(0xFF3C3F41)) // 顶部栏颜色
//                    .padding(horizontal = 12.dp, vertical = 8.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Text(
//                        text = language.ifBlank { "Code" },
//                        color = Color(0xFFA9B7C6),
//                        fontSize = 12.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    // 折叠/展开 按钮
//                    TextButton(
//                        onClick = { isExpanded = !isExpanded },
//                        contentPadding = PaddingValues(0.dp),
//                        modifier = Modifier.height(24.dp)
//                    ) {
//                        Text(if (isExpanded) "收起" else "展开", fontSize = 12.sp, color = DoubaoBlue)
//                    }
//                }
//
//                // 复制按钮
//                IconButton(
//                    onClick = {
//                        clipboardManager.setText(AnnotatedString(code))
//                        // 这里简单用 Toast 提示，实际可以用 Snackbar
//                        android.widget.Toast.makeText(context, "代码已复制", android.widget.Toast.LENGTH_SHORT).show()
//                    },
//                    modifier = Modifier.size(20.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.ContentCopy,
//                        contentDescription = "Copy",
//                        tint = Color.Gray
//                    )
//                }
//            }
//
//            // --- 代码内容区域 (带动画) ---
//            androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
//                SelectionContainer {
//                    Text(
//                        text = SimpleSyntaxHighlighter.highlight(code, language),
//                        // ✅✅✅ 核心修改：强制将基础文字颜色设为浅灰色，防止看不清
//                        color = Color(0xFFA9B7C6),
//
//                        fontFamily = FontFamily.Monospace,
//                        fontSize = 13.sp,
//                        lineHeight = 20.sp,
//                        modifier = Modifier
//                            .padding(12.dp)
//                            .fillMaxWidth()
//                    )
//                }
//            }
//        }
//    }
//}
//
//
//// ==========================================
//// 4. 原有的 APP 代码 (已修改 ChatBubble 调用)
//// ==========================================
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            DoubaoTheme {
//                DoubaoScreen()
//            }
//        }
//    }
//}
//
//data class Message(
//    val id: String,
//    val text: String,
//    val isUser: Boolean
//)
//
//val DoubaoBlue = Color(0xFF4E75F6)
//val MyGray = Color(0xFFF5F6F8)
//val TextGray = Color(0xFF333333)
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DoubaoScreen() {
//    // 消息列表
//    val messages = remember {
//        mutableStateListOf(
//            Message("1", "你好！我是升级版豆包。\n\n我现在支持：\n- **代码高亮**\n- **图片显示**\n- **流式打字效果**\n\n快试着问我点什么吧！", false)
//        )
//    }
//
//    var inputText by remember { mutableStateOf("") }
//    val listState = rememberLazyListState()
//    // 协程作用域，用于处理延时任务（模拟打字）
//    val scope = rememberCoroutineScope()
//    // 标记是否正在生成中，防止用户重复点击
//    var isGenerating by remember { mutableStateOf(false) }
//
//    // 当消息列表变化时，自动滚动到底部
//    LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length) {
//        if (messages.isNotEmpty()) {
//            listState.animateScrollToItem(messages.size - 1)
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("豆包 (Module 3 Completed)", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
//            )
//        },
//        bottomBar = {
//            InputArea(
//                value = inputText,
//                onValueChange = { inputText = it },
//                // 只有不在生成时才能发送
//                onSend = {
//                    if (inputText.isNotBlank() && !isGenerating) {
//                        val userText = inputText
//                        inputText = "" // 清空输入框
//                        isGenerating = true // 锁定状态
//
//                        // 1. 添加用户消息
//                        messages.add(Message(System.currentTimeMillis().toString(), userText, true))
//
//                        // 2. 添加一个"空"的 AI 消息占位符
//                        val aiMsgId = System.currentTimeMillis().toString() + "_ai"
//                        messages.add(Message(aiMsgId, "", false))
//                        val aiMsgIndex = messages.lastIndex
//
//                        // 3. 开启协程模拟流式打字
//                        scope.launch {
//                            val fullResponse = getSimulatedResponse(userText)
//                            var currentText = ""
//
//                            // 逐字追加，模拟网络流
//                            fullResponse.forEach { char ->
//                                delay(20) // 打字速度：20ms 一个字
//                                currentText += char
//                                // 更新列表中的那条消息
//                                messages[aiMsgIndex] = messages[aiMsgIndex].copy(text = currentText)
//                            }
//                            isGenerating = false // 解锁
//                        }
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        LazyColumn(
//            state = listState,
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//                .background(Color.White),
//            contentPadding = PaddingValues(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            items(messages) { msg ->
//                ChatBubble(message = msg)
//            }
//
//            // 如果正在生成，可以在最底部加一个小光标或loading（可选）
//            if (isGenerating && messages.last().isUser) {
//                item {
//                    Text("豆包正在思考...", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))
//                }
//            }
//        }
//    }
//}
//
//// ==========================================
//// 模拟 AI 回复的数据库 (为了测试各种 Markdown 效果)
//// ==========================================
//fun getSimulatedResponse(userQuestion: String): String {
//    return if (userQuestion.contains("picture")) {
//        """
//        好的，这是你要的图片：
//
//        ![风景](https://picsum.photos/600/350)
//
//        图片加载需要一点时间，请耐心等待。
//        """.trimIndent()
//    } else if (userQuestion.contains("code") || userQuestion.contains("Kotlin")) {
//        """
//        没问题，这是一个使用 **Kotlin** 协程的示例：
//
//        ```kotlin
//        import kotlinx.coroutines.*
//        //函数
//        fun main() = runBlocking {
//            launch {
//                delay(1000L)
//                println("World!")
//            }
//            print("Hello ")
//        }
//        ```
//
//        你可以看到 `runBlocking` 和 `launch` 的用法。
//        """.trimIndent()
//    } else {
//        """
//        我收到了你的问题：**$userQuestion**
//
//
//        作为 AI 助手，我可以帮你：
//        1. **写代码**：支持高亮和复制。
//        2. **看图片**：发送“图片”试试。
//        3. **讲笑话**：虽然我可能不太幽默。
//        这只是一个流式输出的测试，你会看到这些字**一个接一个**地蹦出来，就像我在实时打字一样！
//        """.trimIndent()
//    }
//}
//
//@Composable
//fun ChatBubble(message: com.example.myapplication.domain.entities.Message) {
//    val bubbleColor = if (message.isUser) DoubaoBlue else MyGray
//    val textColor = if (message.isUser) Color.White else TextGray
//    val shape = if (message.isUser) RoundedCornerShape(18.dp, 18.dp, 2.dp, 18.dp) else RoundedCornerShape(18.dp, 18.dp, 18.dp, 2.dp)
//
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
//        verticalAlignment = Alignment.Top
//    ) {
//        if (!message.isUser) {
//            Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = Color(0xFFE0E0E0)) {
//                Icon(Icons.Outlined.SmartToy, contentDescription = null, modifier = Modifier.padding(6.dp), tint = DoubaoBlue)
//            }
//            Spacer(modifier = Modifier.width(8.dp))
//        }
//
//        Surface(
//            color = bubbleColor,
//            shape = shape,
//            // 移除 max width 限制或设大一点，让代码块显示更宽
//            modifier = Modifier.weight(1f, fill = false)
//        ) {
//            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
//                if (message.isUser) {
//                    // 用户消息保持简单 Text
//                    Text(text = message.text, color = textColor, fontSize = 16.sp, lineHeight = 24.sp)
//                } else {
//                    // ------------------------------------------------------------
//                    // 核心修改点：这里不再使用 MarkdownText 库
//                    // 而是调用我们自己写的 MarkdownMessageDisplay
//                    // ------------------------------------------------------------
//                    MarkdownMessageDisplay(rawText = message.text, textColor = textColor)
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun InputArea(value: String, onValueChange: (String) -> Unit, onSend: () -> Unit) {
//    Surface(tonalElevation = 2.dp, color = Color.White) {
//        Row(
//            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp).fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            IconButton(onClick = { }, modifier = Modifier.size(40.dp).background(MyGray, CircleShape)) {
//                Icon(Icons.Default.KeyboardVoice, contentDescription = "Voice", tint = Color.Black)
//            }
//            Spacer(modifier = Modifier.width(8.dp))
//            TextField(
//                value = value, onValueChange = onValueChange,
//                placeholder = { Text("问我任何问题...", color = Color.Gray) },
//                modifier = Modifier.weight(1f).heightIn(min = 50.dp),
//                shape = RoundedCornerShape(24.dp),
//                colors = TextFieldDefaults.colors(
//                    focusedContainerColor = MyGray, unfocusedContainerColor = MyGray,
//                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
//                    cursorColor = DoubaoBlue
//                )
//            )
//            Spacer(modifier = Modifier.width(8.dp))
//            IconButton(onClick = onSend, modifier = Modifier.size(40.dp).background(DoubaoBlue, CircleShape)) {
//                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
//            }
//        }
//    }
//}
//
//@Composable
//fun DoubaoTheme(content: @Composable () -> Unit) {
//    MaterialTheme(content = content)
//}
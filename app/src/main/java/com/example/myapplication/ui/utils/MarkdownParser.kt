package com.example.myapplication.ui.utils


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import org.commonmark.node.*
import org.commonmark.parser.Parser
import org.commonmark.node.BulletList
import org.commonmark.node.OrderedList
import org.commonmark.node.ListItem
// ==========================================
// 1. Markdown 核心数据模型 (Pipeline 的产物)
// ==========================================

// 定义 Markdown 的块级结构，UI 渲染器将遍历这个列表
sealed class MarkdownBlock {
    // 普通段落（包含富文本：加粗、斜体等）
    data class Paragraph(val content: AnnotatedString) : MarkdownBlock()
    // 标题 (#, ##)
    data class Heading(val content: AnnotatedString, val level: Int) : MarkdownBlock()
    // 代码块 (```)
    data class CodeFence(val code: String, val language: String) : MarkdownBlock()
    // 新增：图片类型
    data class Image(val url: String, val alt: String) : MarkdownBlock()
}


// ==========================================
// 2. Markdown 解析器 (Parser)
// ==========================================

object MarkdownParser {
    // 初始化 CommonMark 解析器
    private val parser: Parser = Parser.builder().build()

    fun parse(markdown: String): List<com.example.myapplication.ui.utils.MarkdownBlock> {
        val document = parser.parse(markdown)
        val blocks = mutableListOf<com.example.myapplication.ui.utils.MarkdownBlock>()

        var node = document.firstChild
        while (node != null) {
            when (node) {
                // 1. 普通段落 & 图片处理
                is org.commonmark.node.Paragraph -> {
                    val firstChild = node.firstChild
                    if (firstChild is org.commonmark.node.Image && firstChild.next == null) {
                        blocks.add(com.example.myapplication.ui.utils.MarkdownBlock.Image(firstChild.destination, firstChild.title ?: ""))
                    } else {
                        blocks.add(com.example.myapplication.ui.utils.MarkdownBlock.Paragraph(parseInlineStyles(node)))
                    }
                }
                // 2. 标题处理
                is org.commonmark.node.Heading -> {
                    blocks.add(com.example.myapplication.ui.utils.MarkdownBlock.Heading(parseInlineStyles(node), node.level))
                }
                // 3. 代码块处理
                is FencedCodeBlock -> {
                    blocks.add(com.example.myapplication.ui.utils.MarkdownBlock.CodeFence(node.literal ?: "", node.info ?: ""))
                }

                // 新增：列表处理 (无序 BulletList & 有序 OrderedList)
                is BulletList, is OrderedList -> {
                    var listItem = node.firstChild
                    var index = 1
                    while (listItem != null) {
                        if (listItem is ListItem) {
                            // 决定前缀：如果是无序列表用 "• "，有序列表用 "1. "
                            val prefix = if (node is BulletList) "•  " else "$index. "

                            // 列表项里面通常包含一个 Paragraph，我们把它挖出来
                            var contentNode = listItem.firstChild
                            while (contentNode != null) {
                                if (contentNode is org.commonmark.node.Paragraph) {
                                    // 把前缀和内容拼起来
                                    val fullText = buildAnnotatedString {
                                        append(prefix)
                                        append(parseInlineStyles(contentNode))
                                    }
                                    blocks.add(com.example.myapplication.ui.utils.MarkdownBlock.Paragraph(fullText))
                                }
                                // 这里简化处理，如果列表里有代码块暂时忽略，防止太复杂
                                contentNode = contentNode.next
                            }
                        }
                        listItem = listItem.next
                        index++
                    }
                }
            }
            node = node.next
        }
        return blocks
    }

    // 递归解析行内样式 (Inline Parser) -> 转换为 Compose 的 AnnotatedString
    private fun parseInlineStyles(blockNode: Node): AnnotatedString {
        return buildAnnotatedString {
            var child = blockNode.firstChild
            while (child != null) {
                when (child) {
                    is Text -> append(child.literal)
                    is StrongEmphasis -> { // **加粗**
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                        append(parseInlineStyles(child)) // 递归处理内部文字
                        pop()
                    }
                    is Emphasis -> { // *斜体*
                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        append(parseInlineStyles(child))
                        pop()
                    }
                    is Code -> { // `行内代码`
                        pushStyle(SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color(0xFFEFEFEF),
                            color = Color(0xFFE91E63) // 粉色强调
                        ))
                        append(child.literal)
                        pop()
                    }
                    is org.commonmark.node.Image -> {
                        append("[图片: ${child.title}]")
                    }
                    is SoftLineBreak, is HardLineBreak -> append("\n")
                    else -> append(parseInlineStyles(child))

                }
                child = child.next
            }
        }
    }
}


// ==========================================
// 新增：简易语法高亮器 (Module 2)
// ==========================================
object SimpleSyntaxHighlighter {
    private val keywords = listOf("fun", "val", "var", "if", "else", "for", "while", "return", "class", "object", "import", "package")

    fun highlight(code: String, language: String): AnnotatedString {
        return buildAnnotatedString {
            append(code)

            // 1. 高亮关键字 (橙色)
            keywords.forEach { keyword ->
                val regex = "\\b$keyword\\b".toRegex()
                regex.findAll(code).forEach { result ->
                    addStyle(SpanStyle(color = Color(0xFFCC7832), fontWeight = FontWeight.Bold), result.range.first, result.range.last + 1)
                }
            }

            // 2. 高亮字符串 "..." (绿色)
            val stringRegex = "\".*?\"".toRegex()
            stringRegex.findAll(code).forEach { result ->
                addStyle(SpanStyle(color = Color(0xFF6A8759)), result.range.first, result.range.last + 1)
            }

            // 3. 高亮数字 (蓝色)
            val numberRegex = "\\b\\d+\\b".toRegex()
            numberRegex.findAll(code).forEach { result ->
                addStyle(SpanStyle(color = Color(0xFF6897BB)), result.range.first, result.range.last + 1)
            }

            // 4. 高亮注释 //... (灰色)
            val commentRegex = "//.*".toRegex()
            commentRegex.findAll(code).forEach { result ->
                addStyle(SpanStyle(color = Color.Gray, fontStyle = FontStyle.Italic), result.range.first, result.range.last + 1)
            }
        }
    }
}
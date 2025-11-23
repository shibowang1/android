package com.example.myapplication.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
// ... 导入 Coil, Compose 等包
import coil.compose.AsyncImage // 记得导入
import androidx.compose.ui.Modifier
// ... 导入其他必要的 Compose 包
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



@Composable
fun MarkdownMessageDisplay(rawText: String, textColor: Color) {
    val blocks = remember(rawText) { com.example.myapplication.ui.utils.MarkdownParser.parse(rawText) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        blocks.forEach { block ->
            when (block) {
                is com.example.myapplication.ui.utils.MarkdownBlock.Paragraph -> {
                    SelectionContainer {
                        Text(
                            text = block.content,
                            color = textColor,
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        )
                    }
                }
                is com.example.myapplication.ui.utils.MarkdownBlock.Heading -> {
                    Text(
                        text = block.content,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (block.level == 1) 22.sp else 18.sp
                    )
                }
                is com.example.myapplication.ui.utils.MarkdownBlock.CodeFence -> {
                    CodeBlockDisplay(code = block.code, language = block.language)
                }
                // 新增：图片渲染
                is com.example.myapplication.ui.utils.MarkdownBlock.Image -> {
                    AsyncImage(
                        model = block.url,
                        contentDescription = block.alt,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray), // 加载时的占位背景
                        contentScale = androidx.compose.ui.layout.ContentScale.FillWidth
                    )
                }
            }
        }
    }
}
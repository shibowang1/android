package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ChatBubble(message: com.example.myapplication.domain.entities.Message) {
    val bubbleColor = if (message.isUser) com.example.myapplication.ui.theme.DoubaoBlue else com.example.myapplication.ui.theme.MyGray
    val textColor = if (message.isUser) Color.White else com.example.myapplication.ui.theme.TextGray
    val shape = if (message.isUser) RoundedCornerShape(18.dp, 18.dp, 2.dp, 18.dp) else RoundedCornerShape(18.dp, 18.dp, 18.dp, 2.dp)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!message.isUser) {
            Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = Color(0xFFE0E0E0)) {
                Icon(Icons.Outlined.SmartToy, contentDescription = null, modifier = Modifier.padding(6.dp), tint = com.example.myapplication.ui.theme.DoubaoBlue)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            color = bubbleColor,
            shape = shape,
            // 移除 max width 限制或设大一点，让代码块显示更宽
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                if (message.isUser) {
                    // 用户消息保持简单 Text
                    Text(text = message.text, color = textColor, fontSize = 16.sp, lineHeight = 24.sp)
                } else {
                    // ------------------------------------------------------------
                    // 核心修改点：这里不再使用 MarkdownText 库
                    // 而是调用我们自己写的 MarkdownMessageDisplay
                    // ------------------------------------------------------------
                    MarkdownMessageDisplay(rawText = message.text, textColor = textColor)
                }
            }
        }
    }
}
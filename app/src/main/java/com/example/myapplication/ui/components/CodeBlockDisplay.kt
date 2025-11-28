package com.example.myapplication.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
// ... 导入其他必要的 Compose 包
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.DoubaoBlue

// 这是一个独立的代码块组件，对应任务中的“代码高亮/折叠/复制组件”基础
@Composable
fun CodeBlockDisplay(code: String, language: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    // 状态：是否展开
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2B2B)), // 更像 IDE 的深色
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // --- 顶部栏 ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3C3F41)) // 顶部栏颜色
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = language.ifBlank { "Code" },
                        color = Color(0xFFA9B7C6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // 折叠/展开 按钮
                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text(if (isExpanded) "收起" else "展开", fontSize = 12.sp, color = DoubaoBlue)
                    }
                }

                // 复制按钮
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(code))
                        // 这里简单用 Toast 提示，实际可以用 Snackbar
                        android.widget.Toast.makeText(context, "代码已复制", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = Color.Gray
                    )
                }
            }

            // --- 代码内容区域 (带动画) ---
            androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
                SelectionContainer {
                    Text(
                        text = com.example.myapplication.ui.utils.SimpleSyntaxHighlighter.highlight(code, language),
                        //  核心修改：强制将基础文字颜色设为浅灰色，防止看不清
                        color = Color(0xFFA9B7C6),

                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}
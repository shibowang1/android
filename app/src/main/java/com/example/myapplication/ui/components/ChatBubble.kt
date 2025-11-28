

package com.example.myapplication.ui.components

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextOverflow
import com.example.myapplication.domain.entities.Message
import com.example.myapplication.ui.theme.DoubaoBlue
import com.example.myapplication.ui.theme.MyGray
import com.example.myapplication.ui.theme.TextGray

@OptIn(ExperimentalFoundationApi::class) // 必须加上这个，因为 combinedClickable 是实验性 API
@Composable
fun ChatBubble(
    message: Message,
    onRegenerate: () -> Unit = {} // 新增：重新生成的回调
) {
    val bubbleColor = if (message.isUser) DoubaoBlue else MyGray
    val textColor = if (message.isUser) Color.White else TextGray
    val shape = if (message.isUser) RoundedCornerShape(18.dp, 18.dp, 2.dp, 18.dp) else RoundedCornerShape(18.dp, 18.dp, 18.dp, 2.dp)

    // 状态：控制菜单是否显示
    var showMenu by remember { mutableStateOf(false) }

    // 工具：剪贴板和 Context
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // AI 头像
        if (!message.isUser) {
            Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = Color(0xFFE0E0E0)) {
                Icon(Icons.Outlined.SmartToy, contentDescription = null, modifier = Modifier.padding(6.dp), tint = DoubaoBlue)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // 气泡主体与菜单的容器
        Box(
            modifier = Modifier
                //  改正 1：把 weight 移到这里 (Box 是 Row 的直接子元素)
                .weight(1f, fill = false)
        ) {
            Surface(
                color = bubbleColor,
                shape = shape,
                modifier = Modifier
                    //  改正 2：这里删掉 weight，保留长按点击事件
                    .combinedClickable(
                        onClick = { /* 普通点击不做处理 */ },
                        onLongClick = {
                            showMenu = true
                        }
                    )
            ) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    if (message.isUser) {
//                        Text(text = message.text, color = textColor, fontSize = 16.sp, lineHeight = 24.sp)
                        // 1. 定义状态
                        var isExpanded by remember { mutableStateOf(false) } // 是否已展开
                        var showToggle by remember { mutableStateOf(false) } // 是否显示"展开/收起"按钮

                        Column(
                            modifier = Modifier.animateContentSize() // 添加平滑动画
                        ) {
                            Text(
                                text = message.text,
                                color = textColor,
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                // 2. 核心逻辑：没展开时限制6行，展开后不限制
                                maxLines = if (isExpanded) Int.MAX_VALUE else 6,
                                overflow = TextOverflow.Ellipsis,
                                // 3. 测量逻辑：检测是否文字太长
                                onTextLayout = { textLayoutResult ->
                                    // 只有在折叠状态下检测，一旦检测到溢出，就标记为需要显示按钮
                                    if (!isExpanded && textLayoutResult.hasVisualOverflow) {
                                        showToggle = true
                                    }
                                }
                            )

                            // 4. 只有文字超长时，才显示这个按钮
                            if (showToggle) {
                                Text(
                                    text = if (isExpanded) "收起" else "展开全文",
                                    color = Color.White.copy(alpha = 0.7f), // 稍微淡一点的白色
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .clickable { isExpanded = !isExpanded } // 点击切换状态
                                )
                            }
                        }
                    } else {
                        MarkdownMessageDisplay(rawText = message.text, textColor = textColor)
                    }
                }
            }

            //  弹出菜单 (DropdownMenu)
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                offset = DpOffset(x = 0.dp, y = 0.dp) // 可以调整菜单出现的位置
            ) {
                // 1. 复制选项
                DropdownMenuItem(
                    text = { Text("复制内容") },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                    onClick = {
                        clipboardManager.setText(AnnotatedString(message.text))
                        Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
                        showMenu = false
                    }
                )

                // 2. 重新生成选项 (只有 AI 消息才显示)
                if (!message.isUser) {
                    DropdownMenuItem(
                        text = { Text("重新生成") },
                        leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                        onClick = {
                            onRegenerate() // 调用父组件传入的逻辑
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}
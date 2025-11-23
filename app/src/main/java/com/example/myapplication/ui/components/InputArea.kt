package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun InputArea(value: String, onValueChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(tonalElevation = 2.dp, color = Color.White) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }, modifier = Modifier.size(40.dp).background(com.example.myapplication.ui.theme.MyGray, CircleShape)) {
                Icon(Icons.Default.KeyboardVoice, contentDescription = "Voice", tint = Color.Black)
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = value, onValueChange = onValueChange,
                placeholder = { Text("问我任何问题...", color = Color.Gray) },
                modifier = Modifier.weight(1f).heightIn(min = 50.dp),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = com.example.myapplication.ui.theme.MyGray, unfocusedContainerColor = com.example.myapplication.ui.theme.MyGray,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = com.example.myapplication.ui.theme.DoubaoBlue
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onSend, modifier = Modifier.size(40.dp).background(com.example.myapplication.ui.theme.DoubaoBlue, CircleShape)) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}
package com.example.myapplication.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.myapplication.ui.theme.DoubaoTheme

import com.example.myapplication.ui.pages.DoubaoScreen

//输入 “code” 查看代码输出的渲染效果
//输入 “picture” 查看图片的输出效果
//输出其他任意的字符 查看一般输出文字的效果
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DoubaoTheme {
                DoubaoScreen()
            }
        }
    }
}


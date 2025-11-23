package com.example.myapplication.domain.entities

// 这是一个纯数据类，没有任何 UI 代码
data class Message(
    val id: String,
    val text: String,
    val isUser: Boolean
)
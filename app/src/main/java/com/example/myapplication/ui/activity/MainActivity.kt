package com.example.myapplication.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.myapplication.ui.theme.DoubaoTheme

import com.example.myapplication.ui.pages.DoubaoScreen

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


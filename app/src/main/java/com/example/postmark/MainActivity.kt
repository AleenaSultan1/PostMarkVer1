package com.example.postmark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.postmark.navigation.PostmarkNavGraph
import com.example.postmark.ui.theme.Parchment
import com.example.postmark.ui.theme.PostmarkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PostmarkTheme {
                Surface(modifier = Modifier.fillMaxSize().background(Parchment)) {
                    PostmarkNavGraph()
                }
            }
        }
    }
}

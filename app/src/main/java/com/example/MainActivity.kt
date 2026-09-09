package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.model.ToolItem
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ToolDetailScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      ImageToolsApp()
    }
  }
}

@Composable
fun ImageToolsApp() {
  val systemDark = isSystemInDarkTheme()
  var isDarkTheme by remember { mutableStateOf(systemDark) }
  var selectedTool by remember { mutableStateOf<ToolItem?>(null) }
  var recentTools by remember { mutableStateOf<List<ToolItem>>(emptyList()) }

  MyApplicationTheme(darkTheme = isDarkTheme) {
    Surface(modifier = Modifier.fillMaxSize()) {
      if (selectedTool == null) {
        HomeScreen(
          isDarkTheme = isDarkTheme,
          onToggleTheme = { isDarkTheme = !isDarkTheme },
          onSelectTool = { tool ->
            selectedTool = tool
            recentTools = (listOf(tool) + recentTools.filter { it.id != tool.id }).take(6)
          },
          recentTools = recentTools
        )
      } else {
        BackHandler {
          selectedTool = null
        }
        ToolDetailScreen(
          tool = selectedTool!!,
          onBack = { selectedTool = null }
        )
      }
    }
  }
}


package com.financehub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        
        setContent {
            // Android usa DocumentProvider/SAF para acessar arquivos
            App(
                directoryPath = "", // Vazio - usuário escolherá via SAF
                coroutineScope = coroutineScope
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

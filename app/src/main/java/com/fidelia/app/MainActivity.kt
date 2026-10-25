package com.fidelia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.fidelia.app.ui.FideliaApp
import com.fidelia.app.ui.FideliaViewModel
import com.fidelia.app.ui.theme.FideliaTheme

class MainActivity : ComponentActivity() {

    private val viewModel: FideliaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FideliaTheme {
                FideliaApp(viewModel)
            }
        }
    }
}

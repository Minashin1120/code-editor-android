package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.data.AppDatabase
import com.example.data.HtmlDocumentRepository
import com.example.ui.EditorScreen
import com.example.ui.EditorViewModel
import com.example.ui.EditorViewModelFactory
import com.example.ui.LaunchSplashScreen
import com.example.ui.theme.MyApplicationTheme
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : ComponentActivity() {

    private val db by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { HtmlDocumentRepository(db.htmlDocumentDao()) }
    private val viewModel: EditorViewModel by viewModels { EditorViewModelFactory(repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        val keepSystemSplash = AtomicBoolean(true)
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSystemSplash.get() }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingIntent(intent)

        val playLaunchAnimation = savedInstanceState == null

        setContent {
            var showLaunchAnimation by remember { mutableStateOf(playLaunchAnimation) }

            SideEffect {
                keepSystemSplash.set(false)
            }

            MyApplicationTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        EditorScreen(viewModel = viewModel)
                    }

                    if (showLaunchAnimation) {
                        LaunchSplashScreen(
                            onFinished = { showLaunchAnimation = false },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            Intent.ACTION_SEND -> {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (!sharedText.isNullOrEmpty()) {
                    val title = intent.getStringExtra(Intent.EXTRA_TITLE) ?: "共有コード.html"
                    viewModel.handleSharedCode(sharedText, title)
                } else {
                    val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                    uri?.let { viewModel.loadContentFromUri(this, it) }
                }
            }
            Intent.ACTION_VIEW -> {
                intent.data?.let { uri ->
                    viewModel.loadContentFromUri(this, uri)
                }
            }
        }
    }
}

package com.mangbaam.forceupdateexample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.mangbaam.forceupdateexample.ui.theme.ForceUpdateExampleTheme

class MainActivity : ComponentActivity() {
    private val remoteConfig = FirebaseRemoteConfig.getInstance().apply {
        val configSettings by lazy {
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = if (com.google.firebase.remoteconfig.BuildConfig.DEBUG) 0 else 60
            }
        }
        setConfigSettingsAsync(configSettings)
    }
    private val dataSource = RemoteConfigDataSource(remoteConfig)
    private val repository = ConfigRepository(dataSource)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val shouldUpdate by repository.shouldUpdate().collectAsStateWithLifecycle(
                initialValue = null,
                minActiveState = Lifecycle.State.STARTED,
            )
            ForceUpdateExampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (shouldUpdate) {
                        true -> ForceUpdateDialog(
                            onDismiss = {
                                // 업데이트하지 않고 다이얼로그를 닫은 경우 앱을 종료한다.
                                finish()
                            },
                            onClickUpdate = {
                                val playStoreUrl =
                                    "http://play.google.com/store/apps/details?id=${BuildConfig.PACKAGE_NAME}"
                                startActivity(
                                    Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse(playStoreUrl)
                                        setPackage("com.android.vending")
                                    }
                                )
                            },
                        )

                        false -> Main(modifier = Modifier.padding(innerPadding))
                        null -> Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Main(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column {
            Text(
                text = "Welcome!",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayLarge,
            )
        }
    }
}

@Composable
fun ForceUpdateDialog(
    modifier: Modifier = Modifier,
    onClickUpdate: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
        title = {
            Text(text = "업데이트가 필요합니다.")
        },
        text = {
            Text(text = "원활한 서비스 이용을 위해 최신 버전으로 업데이트가 필요합니다.")
        },
        confirmButton = {
            TextButton(onClick = onClickUpdate) {
                Text(
                    text = "업데이트 하기",
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        },
    )
}

@Preview
@Composable
private fun ForceUpdateDialogPreview() {
    ForceUpdateExampleTheme {
        ForceUpdateDialog {}
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    ForceUpdateExampleTheme {
        Main()
    }
}

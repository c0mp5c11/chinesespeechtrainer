package charity.c0mpu73rpr09r4m.chinesespeechtrainer2

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import charity.c0mpu73rpr09r4m.chinesespeechtrainer2.ui.theme.ChineseSpeechTrainerTheme
import java.io.IOException

class MenuActivity : ComponentActivity() {
    private val speechLogic: SpeechLogic = SpeechLogic()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Thread {
            try {
                speechLogic.copyFolder(this)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()

        setContent {
            ChineseSpeechTrainerTheme {
                val context = LocalContext.current

                val requestPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "The internal application audio recording permission request feature stops working as designed if granted or denied once, but permission can be manually set in the operating system application settings. Reinstalling this application will restore functionality if the security setting button stops working.",
                                fontSize = 12.sp,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Grant Permission")
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "To hear audible Chinese pronunciation, its voice data must be installed at the operating system level. This is in addition to United States (US) English.",
                                fontSize = 12.sp,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    val installIntent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                                    context.startActivity(installIntent)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Install Voice")
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Speak and listen to Mandarin Chinese language in this US English training word game.",
                                fontSize = 12.sp,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    context.startActivity(Intent(context, MainActivity::class.java))
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Start")
                            }
                        }
                    }
                }
            }
        }
    }
}
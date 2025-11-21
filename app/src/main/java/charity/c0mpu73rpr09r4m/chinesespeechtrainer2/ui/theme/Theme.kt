package charity.c0mpu73rpr09r4m.chinesespeechtrainer2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ColorScheme = darkColorScheme(
    primary = Green,
    secondary = Green,
    background = Black,
    tertiary = Green
)

@Composable
fun ChineseSpeechTrainerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = Typography,
        content = content
    )
}
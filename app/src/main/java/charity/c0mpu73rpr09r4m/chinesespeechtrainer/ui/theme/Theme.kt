package charity.c0mpu73rpr09r4m.chinesespeechtrainer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
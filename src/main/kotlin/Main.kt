import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.application
import java.awt.FileDialog
import java.io.File

fun main() = application {
    EditorWindow(
        ::exitApplication, onSaveAsClicked = { pastedImages ->
            val savedMap = SavedMap(
                pastedImages.entries.associate {
                    Pair(it.key, it.value.origin)
                }
            )

            FileDialog(ComposeWindow(), "Save As...", FileDialog.SAVE).apply {
                isVisible = true

                file?.let { path ->
                    val saveFile = File(directory, path)
                    saveFile.writeText(json.encodeToString(SavedMap.serializer(), savedMap))
                }
            }
        }
    )
}
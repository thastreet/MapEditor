import androidx.compose.ui.awt.ComposeWindow
import java.awt.FileDialog
import java.io.File

object FileUtil {
    fun showSaveAsDialog(savedMap: SavedMap) {
        FileDialog(ComposeWindow(), "Save As...", FileDialog.SAVE).apply {
            isVisible = true

            file?.let { path ->
                val saveFile = File(directory, path)
                saveFile.writeText(json.encodeToString(SavedMap.serializer(), savedMap))
            }
        }
    }

    fun showLoadDialog(onSavedMapLoaded: (SavedMap) -> Unit) {
        FileDialog(ComposeWindow(), "Load", FileDialog.LOAD).apply {
            isVisible = true

            file?.let { path ->
                val loadFile = File(directory, path)
                val savedMap = json.decodeFromString(SavedMap.serializer(), loadFile.readText())
                onSavedMapLoaded(savedMap)
            }
        }
    }
}
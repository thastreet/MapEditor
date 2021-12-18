import Const.CASE_SIZE
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import org.jetbrains.skiko.toBufferedImage
import java.awt.FileDialog
import java.io.File

@Composable
fun EditorWindow(onCloseRequest: () -> Unit, onSaveAsClicked: (Map<Point, CopiedImage>) -> Unit) {
    var mapOpened: Boolean by remember { mutableStateOf(true) }
    var copiedImage: CopiedImage? by remember { mutableStateOf(null) }
    var pastedImages: Map<Point, CopiedImage> by remember { mutableStateOf(emptyMap()) }

    fun loadMap(savedMap: SavedMap) {
        val imageBitmap = useResource("map.png") { loadImageBitmap(it) }

        val caseSizeInt = CASE_SIZE.value.toInt()

        pastedImages = savedMap.points.entries.associate {
            val subImage = imageBitmap.asSkiaBitmap().toBufferedImage().getSubimage(
                it.value.x * caseSizeInt,
                it.value.y * caseSizeInt,
                caseSizeInt,
                caseSizeInt
            )

            Pair(
                it.key,
                CopiedImage(subImage, it.value)
            )
        }
    }

    fun showLoadMapDialog() {
        FileDialog(ComposeWindow(), "Load", FileDialog.LOAD).apply {
            isVisible = true

            file?.let { path ->
                val loadFile = File(directory, path)
                val savedMap = json.decodeFromString(SavedMap.serializer(), loadFile.readText())
                loadMap(savedMap)
            }
        }
    }

    Window(
        onCloseRequest = onCloseRequest,
        title = "Editor"
    ) {
        MenuBar {
            Menu("File") {
                Item("Save As...", onClick = {
                    onSaveAsClicked(pastedImages)
                })
                Item("Load", onClick = ::showLoadMapDialog)
            }
            Menu("View") {
                Item("Tileset", onClick = { mapOpened = true })
            }
        }
        TilesetWindow(mapOpened, {
            mapOpened = false
        }, {
            copiedImage = it
        })

        Box(
            Modifier.fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        copiedImage?.let {
                            pastedImages = pastedImages.toMutableMap().apply {
                                set(offset.toIndexPoint(), it)
                            }
                        }
                    }
                }
        ) {
            pastedImages.forEach {
                val position = it.key.toPosition()
                Image(
                    it.value.bufferedImage.toComposeImageBitmap(),
                    "",
                    Modifier.offset(position.x.dp, position.y.dp)
                )
            }
        }
    }
}
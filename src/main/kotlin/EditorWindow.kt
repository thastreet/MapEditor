import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window

@Composable
fun EditorWindow(onCloseRequest: () -> Unit) {
    var mapOpened: Boolean by remember { mutableStateOf(true) }
    var copiedImage: CopiedImage? by remember { mutableStateOf(null) }
    var pastedImages: Map<IndexPoint, CopiedImage> by remember { mutableStateOf(emptyMap()) }

    fun loadMap(savedMap: SavedMap) {
        val imageBitmap = useResource("map.png") { loadImageBitmap(it) }

        pastedImages = savedMap.points.entries.associate { (destination, origin) ->
            Pair(
                destination,
                CopiedImage(imageBitmap.getSubImage(origin.toAbsolutePoint()), origin)
            )
        }
    }

    fun clearMap() {
        pastedImages = emptyMap()
    }

    Window(
        onCloseRequest = onCloseRequest,
        title = "Editor"
    ) {
        MenuBar {
            Menu("File") {
                Item("Save As...", onClick = {
                    FileUtil.showSaveAsDialog(SavedMap.from(pastedImages))
                })
                Item("Load", onClick = { FileUtil.showLoadDialog { loadMap(it) } })
            }
            Menu("Edit") {
                Item("Clear", onClick = { clearMap() })
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
                val absolutePoint = it.key.toAbsolutePoint()
                Image(
                    it.value.bufferedImage.toComposeImageBitmap(),
                    "",
                    Modifier.offset(absolutePoint.x.dp, absolutePoint.y.dp)
                )
            }
        }
    }
}
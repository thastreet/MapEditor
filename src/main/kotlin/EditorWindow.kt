import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import java.util.Stack

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditorWindow(onCloseRequest: () -> Unit) {
    var mapOpened: Boolean by remember { mutableStateOf(true) }
    var copiedImage: CopiedImage? by remember { mutableStateOf(null) }
    var pastedImages: PastedImages by remember { mutableStateOf(emptyMap()) }
    val stateStack: Stack<State> by remember {
        mutableStateOf(Stack<State>().apply {
            push(State(pastedImages))
        })
    }

    fun loadMap(savedMap: SavedMap) {
        stateStack.clear()

        val imageBitmap = useResource("map.png") { loadImageBitmap(it) }
        pastedImages = savedMap.toPastedImages(imageBitmap)
    }

    fun clearMap() {
        val newState = State(emptyMap())
        stateStack.push(newState)
        pastedImages = newState.pastedImages
    }

    fun pasteImageIfNecessary(offset: Offset, saveState: Boolean) {
        copiedImage?.let {
            val newState = State(
                pastedImages.toMutableMap().apply {
                    set(offset.toIndexPoint(), it)
                }
            )
            if (saveState) {
                stateStack.push(newState)
            }
            pastedImages = newState.pastedImages
        }
    }

    fun undoIfPossible() {
        if (stateStack.size <= 1) return
        stateStack.pop()
        pastedImages = stateStack.peek().pastedImages
    }

    Window(
        onCloseRequest = onCloseRequest,
        title = "Editor"
    ) {
        MenuBar {
            Menu("File") {
                Item("Save As...", onClick = {
                    FileDialogUtil.showSaveAsDialog(SavedMap.from(pastedImages))
                })
                Item("Load", onClick = { FileDialogUtil.showLoadDialog { loadMap(it) } })
            }
            Menu("Edit") {
                Item(
                    "Undo",
                    enabled = stateStack.size > 1,
                    shortcut = KeyShortcut(meta = true, key = Key.Z),
                    onClick = { undoIfPossible() })
                Item("Clear Map", onClick = { clearMap() })
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
                .pointerInput("tap") {
                    detectTapGestures(onPress = { offset -> pasteImageIfNecessary(offset, true) })
                }
                .pointerInput("drag") {
                    detectDragGestures(onDrag = { change, _ -> pasteImageIfNecessary(change.position, false) })
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
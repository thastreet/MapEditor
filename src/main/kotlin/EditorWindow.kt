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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import java.util.Stack

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditorWindow(onCloseRequest: () -> Unit) {
    var mapOpened: Boolean by remember { mutableStateOf(true) }
    var collisionsMode: Boolean by remember { mutableStateOf(false) }
    var copiedImages: Set<CopiedImage> by remember { mutableStateOf(emptySet()) }
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

    fun pasteImageIfNecessary(offset: Offset) {
        copiedImages
            .takeIf { it.isNotEmpty() }
            ?.let { copiedImages ->
                val newState = State(
                    pastedImages.toMutableMap().apply {
                        val minX = copiedImages.minOfOrNull { it.origin.x } ?: 0
                        val minY = copiedImages.minOfOrNull { it.origin.y } ?: 0

                        copiedImages.forEach {
                            val indexPoint = offset.toIndexPoint()
                            val translatedIndexPoint = IndexPoint(indexPoint.x + (it.origin.x - minX), indexPoint.y + (it.origin.y - minY))

                            set(translatedIndexPoint, it)
                        }
                    }
                )
                pastedImages = newState.pastedImages
            }
    }

    fun saveState() {
        stateStack.push(State(pastedImages))
    }

    fun onTapped(offset: Offset) {
        if (collisionsMode) {

        } else {
            pasteImageIfNecessary(offset)
        }
    }

    fun onDragged(change: PointerInputChange) {
        if (collisionsMode) {

        } else {
            pasteImageIfNecessary(change.position)
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
                CheckboxItem("Collisions", collisionsMode) {
                    collisionsMode = it
                }
            }
            Menu("View") {
                Item("Tileset", onClick = { mapOpened = true })
            }
        }

        TilesetWindow(mapOpened, {
            mapOpened = false
        }, {
            copiedImages = it
        })

        Box(
            Modifier.fillMaxSize()
                .pointerInput("tap") {
                    detectTapGestures(
                        onPress = { offset ->
                            onTapped(offset)
                            saveState()
                        }
                    )
                }
                .pointerInput("drag") {
                    detectDragGestures(
                        onDrag = { change, _ -> onDragged(change) },
                        onDragEnd = ::saveState
                    )
                }
        ) {
            pastedImages.forEach {
                val absolutePoint = it.key.toAbsolutePoint()
                val offsetX = with(LocalDensity.current) { absolutePoint.x.toDp() }
                val offsetY = with(LocalDensity.current) { absolutePoint.y.toDp() }

                Image(
                    painter = BitmapPainter(it.value.bufferedImage.toComposeImageBitmap(), filterQuality = FilterQuality.None),
                    contentDescription = null,
                    modifier = Modifier.offset(offsetX, offsetY),
                    contentScale = FixedScale(Const.SCALE)
                )
            }
        }
    }
}
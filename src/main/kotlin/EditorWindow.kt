import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import java.util.Stack
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditorWindow(onCloseRequest: () -> Unit) {
    var mapOpened: Boolean by remember { mutableStateOf(true) }
    var collisionsMode: Boolean by remember { mutableStateOf(false) }
    var copiedImages: Set<CopiedImage> by remember { mutableStateOf(emptySet()) }
    var pastedImages: PastedImages by remember { mutableStateOf(emptyMap()) }
    var collisions: Set<IndexPoint> by remember { mutableStateOf(emptySet()) }
    val stateStack: Stack<State> by remember {
        mutableStateOf(Stack<State>().apply {
            push(State(pastedImages, collisions))
        })
    }

    fun loadMap(savedMap: SavedMap) {
        stateStack.clear()

        val imageBitmap = useResource("map.png") { loadImageBitmap(it) }
        pastedImages = savedMap.toPastedImages(imageBitmap)
        collisions = savedMap.collisions
    }

    fun clearMap() {
        val newState = State(emptyMap(), emptySet())
        stateStack.push(newState)
        pastedImages = newState.pastedImages
    }

    fun pasteImageIfNecessary(offset: Offset) {
        copiedImages
            .takeIf { it.isNotEmpty() }
            ?.let { copiedImages ->
                pastedImages = pastedImages.toMutableMap().apply {
                    val minX = copiedImages.minOfOrNull { it.origin.x } ?: 0
                    val minY = copiedImages.minOfOrNull { it.origin.y } ?: 0

                    copiedImages.forEach {
                        val indexPoint = offset.toIndexPoint()
                        val translatedIndexPoint = IndexPoint(indexPoint.x + (it.origin.x - minX), indexPoint.y + (it.origin.y - minY))

                        set(translatedIndexPoint, it)
                    }
                }
            }
    }

    fun saveState() {
        stateStack.push(State(pastedImages, collisions))
    }

    fun toggleCollision(offset: Offset) {
        val indexPoint = offset.toIndexPoint()

        collisions = collisions.toMutableSet().apply {
            if (collisions.contains(indexPoint)) {
                remove(indexPoint)
            } else {
                add(indexPoint)
            }
        }

        saveState()
    }

    fun onTapped(offset: Offset) {
        if (collisionsMode) {
            toggleCollision(offset)
        } else {
            pasteImageIfNecessary(offset)
            saveState()
        }
    }

    fun onDragged(change: PointerInputChange) {
        if (!collisionsMode) {
            pasteImageIfNecessary(change.position)
        }
    }

    fun undoIfPossible() {
        if (stateStack.size <= 1) return
        stateStack.pop()
        stateStack.peek().let {
            pastedImages = it.pastedImages
            collisions = it.collisions
        }
    }

    Window(
        onCloseRequest = onCloseRequest,
        title = "Editor"
    ) {
        MenuBar {
            Menu("File") {
                Item("Save As...", onClick = {
                    FileDialogUtil.showSaveAsDialog(SavedMap.from(pastedImages, collisions))
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

        Canvas(
            Modifier.fillMaxSize()
                .pointerInput("tap") {
                    detectTapGestures(
                        onPress = { offset ->
                            onTapped(offset)
                        }
                    )
                }
                .pointerInput("drag") {
                    detectDragGestures(
                        onDrag = { change, _ -> onDragged(change) },
                        onDragEnd = {
                            if (!collisionsMode) {
                                saveState()
                            }
                        }
                    )
                }
        ) {
            pastedImages.forEach {
                val absolutePoint = it.key.toAbsolutePoint()

                drawImage(
                    image = it.value.bufferedImage.toComposeImageBitmap(),
                    dstOffset = IntOffset(absolutePoint.x, absolutePoint.y),
                    dstSize = IntSize((Const.CASE_SIZE * Const.SCALE).roundToInt(), (Const.CASE_SIZE * Const.SCALE).roundToInt()),
                    filterQuality = FilterQuality.None
                )
            }

            if (collisionsMode) {
                collisions.forEach {
                    val absolutePoint = it.toAbsolutePoint()

                    drawCircle(
                        Color.Red,
                        center = Offset(absolutePoint.x + Const.CASE_SIZE / 2f * Const.SCALE, absolutePoint.y + Const.CASE_SIZE / 2f * Const.SCALE),
                        radius = Const.CASE_SIZE / 2f * Const.SCALE,
                        alpha = Const.COMPONENT_ALPHA
                    )
                }
            }
        }
    }
}
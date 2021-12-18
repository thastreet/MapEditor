import Const.CASE_SIZE
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window

@Composable
fun TilesetWindow(visible: Boolean, onCloseRequest: () -> Unit, onImageCopied: (CopiedImage) -> Unit) {
    var indexPoint by remember { mutableStateOf(IndexPoint()) }

    Window(
        onCloseRequest = onCloseRequest,
        visible = visible,
        title = "Tileset",
        resizable = false
    ) {
        Box {
            val scrollBarAlpha = 0.35f
            val horizontalScrollState = rememberScrollState()
            val verticalScrollState = rememberScrollState()

            val imageBitmap = useResource("map.png") { loadImageBitmap(it) }
            Image(
                imageBitmap,
                "Map",
                Modifier
                    .width(imageBitmap.width.dp)
                    .height(imageBitmap.height.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            indexPoint = offset.copy(x = offset.x + horizontalScrollState.value, y = offset.y + verticalScrollState.value).toIndexPoint()
                            onImageCopied(
                                CopiedImage(
                                    imageBitmap.getSubImage(indexPoint.toAbsolutePoint()),
                                    indexPoint
                                )
                            )
                        }
                    }
                    .horizontalScroll(horizontalScrollState)
                    .verticalScroll(verticalScrollState),
                alignment = Alignment.TopStart,
                contentScale = ContentScale.None
            )
            HorizontalScrollbar(
                adapter = rememberScrollbarAdapter(horizontalScrollState),
                modifier = Modifier.align(Alignment.BottomEnd),
                style = LocalScrollbarStyle.current.copy(
                    unhoverColor = LocalScrollbarStyle.current.hoverColor.copy(
                        alpha = scrollBarAlpha
                    )
                )
            )
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(verticalScrollState),
                modifier = Modifier.align(Alignment.BottomEnd),
                style = LocalScrollbarStyle.current.copy(
                    unhoverColor = LocalScrollbarStyle.current.hoverColor.copy(
                        alpha = scrollBarAlpha
                    )
                )
            )
            Canvas(Modifier) {
                drawRect(
                    color = Color.Red,
                    topLeft = indexPoint.toAbsolutePoint().toOffset(extraX = -horizontalScrollState.value, extraY = -verticalScrollState.value),
                    size = Size(CASE_SIZE.value, CASE_SIZE.value),
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}
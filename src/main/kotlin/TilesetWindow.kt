import Const.CASE_SIZE
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import kotlin.math.abs

@Composable
fun TilesetWindow(visible: Boolean, onCloseRequest: () -> Unit, onImageCopied: (Set<CopiedImage>) -> Unit) {
    var originIndexPoint: IndexPoint? by remember { mutableStateOf(null) }
    var destinationIndexPoint: IndexPoint? by remember { mutableStateOf(null) }

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
                    .pointerInput("tap") {
                        detectTapGestures(onPress = { offset ->
                            originIndexPoint = offset.copy(
                                x = offset.x + horizontalScrollState.value,
                                y = offset.y + verticalScrollState.value
                            ).toIndexPoint().also {
                                onImageCopied(
                                    setOf(
                                        CopiedImage(
                                            imageBitmap.getSubImage(it.toAbsolutePoint()),
                                            it
                                        )
                                    )
                                )
                            }
                            destinationIndexPoint = originIndexPoint
                        })
                    }
                    .pointerInput("drag") {
                        detectDragGestures(onDrag = { change, _ ->
                            val offset = change.position
                            val localOriginIndexPoint = originIndexPoint ?: return@detectDragGestures

                            destinationIndexPoint = offset
                                .copy(x = offset.x + horizontalScrollState.value, y = offset.y + verticalScrollState.value)
                                .toIndexPoint()
                                .also {
                                    val minX = minOf(localOriginIndexPoint.x, it.x)
                                    val maxX = maxOf(localOriginIndexPoint.x, it.x)
                                    val minY = minOf(localOriginIndexPoint.y, it.y)
                                    val maxY = maxOf(localOriginIndexPoint.y, it.y)

                                    val images = mutableSetOf<CopiedImage>()

                                    (minX..maxX).forEach { x ->
                                        (minY..maxY).forEach { y ->
                                            val point = IndexPoint(x, y)
                                            images.add(
                                                CopiedImage(
                                                    imageBitmap.getSubImage(point.toAbsolutePoint()),
                                                    point
                                                )
                                            )
                                        }
                                    }

                                    onImageCopied(images)
                                }
                        })
                    }
                    .horizontalScroll(horizontalScrollState)
                    .verticalScroll(verticalScrollState),
                alignment = Alignment.TopStart,
                contentScale = FixedScale(Const.SCALE)
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
            originIndexPoint?.let { localOriginIndexPoint ->
                Canvas(Modifier) {
                    val localDestinationIndexPoint = destinationIndexPoint

                    val width: Int
                    val height: Int
                    val rectX: Int
                    val rectY: Int

                    when {
                        localDestinationIndexPoint != null -> {
                            width =
                                CASE_SIZE + abs(localDestinationIndexPoint.x - localOriginIndexPoint.x) * CASE_SIZE
                            height =
                                CASE_SIZE + abs(localDestinationIndexPoint.y - localOriginIndexPoint.y) * CASE_SIZE
                            rectX =
                                if (localOriginIndexPoint.x <= localDestinationIndexPoint.x)
                                    localOriginIndexPoint.toAbsolutePoint().x
                                else
                                    localDestinationIndexPoint.toAbsolutePoint().x
                            rectY =
                                if (localOriginIndexPoint.y <= localDestinationIndexPoint.y)
                                    localOriginIndexPoint.toAbsolutePoint().y
                                else
                                    localDestinationIndexPoint.toAbsolutePoint().y
                        }

                        else -> {
                            width = CASE_SIZE
                            height = CASE_SIZE
                            rectX = localOriginIndexPoint.toAbsolutePoint().x
                            rectY = localOriginIndexPoint.toAbsolutePoint().y
                        }
                    }

                    drawRect(
                        color = Color.Red,
                        topLeft = Offset(rectX.toFloat() - horizontalScrollState.value, rectY.toFloat() - verticalScrollState.value),
                        size = Size(width * Const.SCALE, height * Const.SCALE),
                        style = Stroke(width = 2f)
                    )
                }
            }
        }
    }
}
import Const.CASE_SIZE
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
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
                            destinationIndexPoint = offset.copy(
                                x = offset.x + horizontalScrollState.value,
                                y = offset.y + verticalScrollState.value
                            ).toIndexPoint().also {
                                val localOriginIndexPoint = originIndexPoint!!

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
            originIndexPoint?.let { localOriginIndexPoint ->
                Canvas(Modifier) {
                    val localDestinationIndexPoint = destinationIndexPoint
                    val caseSizeFloat = CASE_SIZE.value

                    val width: Float
                    val height: Float
                    val rectX: Float
                    val rectY: Float

                    when {
                        localDestinationIndexPoint != null -> {
                            width =
                                caseSizeFloat + abs(localDestinationIndexPoint.x - localOriginIndexPoint.x) * caseSizeFloat
                            height =
                                caseSizeFloat + abs(localDestinationIndexPoint.y - localOriginIndexPoint.y) * caseSizeFloat
                            rectX =
                                if (localOriginIndexPoint.x <= localDestinationIndexPoint.x)
                                    localOriginIndexPoint.toAbsolutePoint().x.toFloat()
                                else
                                    localDestinationIndexPoint.toAbsolutePoint().x.toFloat()
                            rectY =
                                if (localOriginIndexPoint.y <= localDestinationIndexPoint.y)
                                    localOriginIndexPoint.toAbsolutePoint().y.toFloat()
                                else
                                    localDestinationIndexPoint.toAbsolutePoint().y.toFloat()
                        }
                        else -> {
                            width = caseSizeFloat
                            height = caseSizeFloat
                            rectX = localOriginIndexPoint.toAbsolutePoint().x.toFloat()
                            rectY = localOriginIndexPoint.toAbsolutePoint().y.toFloat()
                        }
                    }

                    drawRect(
                        color = Color.Red,
                        topLeft = Offset(rectX - horizontalScrollState.value, rectY - verticalScrollState.value),
                        size = Size(width, height),
                        style = Stroke(width = 2f)
                    )
                }
            }
        }
    }
}
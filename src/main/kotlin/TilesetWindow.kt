import Const.CASE_SIZE
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import org.jetbrains.skiko.toBufferedImage

@Composable
fun TilesetWindow(visible: Boolean, onCloseRequest: () -> Unit, onImageCopied: (CopiedImage) -> Unit) {
    var indexPoint by remember { mutableStateOf(Point()) }

    Window(
        onCloseRequest = onCloseRequest,
        visible = visible,
        title = "Tileset"
    ) {
        val imageBitmap = useResource("map.png") { loadImageBitmap(it) }

        Image(
            imageBitmap,
            "Map",
            Modifier
                .width(imageBitmap.width.dp)
                .height(imageBitmap.height.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        indexPoint = offset.toIndexPoint()
                        val position = indexPoint.toPosition()

                        val caseSizeInt = CASE_SIZE.value.toInt()
                        onImageCopied(
                            CopiedImage(
                                imageBitmap.asSkiaBitmap().toBufferedImage().getSubimage(
                                    position.x,
                                    position.y,
                                    caseSizeInt,
                                    caseSizeInt
                                ),
                                indexPoint
                            )
                        )
                    }
                },
            alignment = Alignment.TopStart,
            contentScale = ContentScale.None
        )
        Canvas(Modifier) {
            drawRect(
                color = Color.Red,
                topLeft = indexPoint.toPosition().toOffset(),
                size = Size(CASE_SIZE.value, CASE_SIZE.value),
                style = Stroke(width = 2f)
            )
        }
    }
}
import Const.CASE_SIZE
import Const.SCALE
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skiko.toBufferedImage
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

private val SCALED_CASE_SIZE = (CASE_SIZE * SCALE).roundToInt()

fun IndexPoint.toAbsolutePoint(): AbsolutePoint =
    AbsolutePoint(x * SCALED_CASE_SIZE, y * SCALED_CASE_SIZE)

fun Offset.toIndexPoint(): IndexPoint =
    IndexPoint(
        x.roundToInt() / SCALED_CASE_SIZE,
        y.roundToInt() / SCALED_CASE_SIZE
    )

fun ImageBitmap.getSubImage(absolutePoint: AbsolutePoint): BufferedImage =
    asSkiaBitmap()
        .toBufferedImage()
        .getSubimage(
            (absolutePoint.x / SCALE).roundToInt(),
            (absolutePoint.y / SCALE).roundToInt(),
            CASE_SIZE,
            CASE_SIZE
        )

fun SavedMap.toPastedImages(imageBitmap: ImageBitmap): PastedImages =
    points.entries.associate { (destination, origin) ->
        Pair(
            destination,
            CopiedImage(imageBitmap.getSubImage(origin.toAbsolutePoint()), origin)
        )
    }
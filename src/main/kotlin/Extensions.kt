import Const.CASE_SIZE
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skiko.toBufferedImage
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

fun IndexPoint.toAbsolutePoint(): AbsolutePoint =
    AbsolutePoint(x * CASE_SIZE, y * CASE_SIZE)

fun Offset.toIndexPoint(): IndexPoint =
    IndexPoint(
        x.roundToInt() / CASE_SIZE,
        y.roundToInt() / CASE_SIZE
    )

fun ImageBitmap.getSubImage(absolutePoint: AbsolutePoint): BufferedImage =
    asSkiaBitmap()
        .toBufferedImage()
        .getSubimage(
            absolutePoint.x,
            absolutePoint.y,
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
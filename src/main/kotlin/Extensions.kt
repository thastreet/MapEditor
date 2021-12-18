import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skiko.toBufferedImage
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

fun IndexPoint.toAbsolutePoint(): AbsolutePoint {
    val caseSizeInt = Const.CASE_SIZE.value.toInt()
    return AbsolutePoint(x * caseSizeInt, y * caseSizeInt)
}

fun AbsolutePoint.toOffset(): Offset =
    Offset(x.toFloat(), y.toFloat())

fun Offset.toIndexPoint(): IndexPoint {
    val caseSizeInt = Const.CASE_SIZE.value.toInt()
    return IndexPoint(
        x.roundToInt() / caseSizeInt,
        y.roundToInt() / caseSizeInt
    )
}

fun ImageBitmap.getSubImage(absolutePoint: AbsolutePoint): BufferedImage {
    val caseSizeInt = Const.CASE_SIZE.value.toInt()
    return asSkiaBitmap().toBufferedImage().getSubimage(
        absolutePoint.x,
        absolutePoint.y,
        caseSizeInt,
        caseSizeInt
    )
}

fun SavedMap.toPastedImages(imageBitmap: ImageBitmap): PastedImages =
    points.entries.associate { (destination, origin) ->
        Pair(
            destination,
            CopiedImage(imageBitmap.getSubImage(origin.toAbsolutePoint()), origin)
        )
    }
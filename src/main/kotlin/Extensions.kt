import androidx.compose.ui.geometry.Offset
import kotlin.math.roundToInt

fun Point.toPosition(): Point {
    val caseSizeInt = Const.CASE_SIZE.value.toInt()
    return Point(x * caseSizeInt, y * caseSizeInt)
}

fun Point.toOffset(): Offset =
    Offset(x.toFloat(), y.toFloat())

fun Offset.toIndexPoint(): Point {
    val caseSizeInt = Const.CASE_SIZE.value.toInt()
    return Point(
        x.roundToInt() / caseSizeInt,
        y.roundToInt() / caseSizeInt
    )
}
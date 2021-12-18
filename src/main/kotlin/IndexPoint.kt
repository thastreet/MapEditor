import kotlinx.serialization.Serializable

@Serializable
data class IndexPoint(
    override val x: Int = 0,
    override val y: Int = 0
) : Point

import kotlinx.serialization.Serializable

@Serializable
data class SavedMap(
    val points: Map<IndexPoint, IndexPoint>,
    val collisions: Set<IndexPoint>
) {
    companion object {
        fun from(pastedImages: PastedImages, collisions: Set<IndexPoint>): SavedMap =
            SavedMap(
                pastedImages.mapValues { it.value.origin },
                collisions
            )
    }
}

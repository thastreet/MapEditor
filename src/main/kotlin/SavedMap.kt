import kotlinx.serialization.Serializable

@Serializable
data class SavedMap(
    val points: Map<IndexPoint, IndexPoint>
) {
    companion object {
        fun from(pastedImages: Map<IndexPoint, CopiedImage>): SavedMap =
            SavedMap(
                pastedImages.entries.associate {
                    Pair(it.key, it.value.origin)
                }
            )
    }
}

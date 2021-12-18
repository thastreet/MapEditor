import kotlinx.serialization.Serializable

@Serializable
data class SavedMap(
    val points: Map<IndexPoint, IndexPoint>
) {
    companion object {
        fun from(pastedImages: PastedImages): SavedMap =
            SavedMap(
                pastedImages.entries.associate {
                    Pair(it.key, it.value.origin)
                }
            )
    }
}

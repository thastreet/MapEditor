import kotlinx.serialization.json.Json

val json = Json {
    allowStructuredMapKeys = true
    encodeDefaults = true
}
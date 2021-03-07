package pl.voytech.exporter.core.model

data class ColumnKey<T> internal constructor(
    val id: String? = null,
    val ref: ((record: T) -> Any?)? = null,
) {
    companion object {
        fun <T> field(fieldRef: ((record: T) -> Any?)) = ColumnKey(ref = fieldRef)
    }
}
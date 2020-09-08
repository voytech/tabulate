package pl.voytech.exporter.core.model

data class CellKey<T> internal constructor(
    val columnIndex: Int? = null,
    val id: String? = null,
    val ref: ((record: T) -> Any?)? = null,
) {
    fun columnKey(): ColumnKey<T> = ColumnKey(id,ref)

    companion object {
        fun <T> cellKey(columnKey: ColumnKey<T>): CellKey<T> = CellKey(id = columnKey.id, ref = columnKey.ref)
    }
}
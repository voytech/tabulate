package pl.voytech.exporter.core.model

data class RowData<T>(
    val index: Int,
    val record: T? = null,
    val dataset: Collection<T>
)

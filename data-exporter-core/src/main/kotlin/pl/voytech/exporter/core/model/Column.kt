package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.api.builder.fluent.ColumnBuilder
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.ColumnExtension

data class Column<T> internal constructor(
    val id: Key<T>,
    val index: Int?,
    val columnType: CellType?,
    val columnExtensions: Set<ColumnExtension>?,
    val cellExtensions: Set<CellExtension>?,
    val dataFormatter: ((field: Any) -> Any)? = null
) {
    companion object {
        @JvmStatic
        fun <T> builder() = ColumnBuilder<T>()
    }
}
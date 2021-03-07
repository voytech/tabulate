package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.attributes.alias.CellAttribute
import pl.voytech.exporter.core.model.attributes.ColumnAttribute

data class Column<T> internal constructor(
    val id: ColumnKey<T>,
    val index: Int?,
    val columnType: CellType?,
    val columnAttributes: Set<ColumnAttribute>?,
    val cellAttributes: Set<CellAttribute>?,
    val dataFormatter: ((field: Any) -> Any)? = null
)
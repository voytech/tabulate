package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.ColumnExtension

data class Column<T>(
    val id: Key<T>,
    val index: Int?,
    val columnTitle: Description?,
    val columnType: CellType?,
    val columnExtensions: Set<ColumnExtension>?,
    val cellExtensions: Set<CellExtension>?,
    val dataFormatter: ((field: Any) -> Any)? = null
)
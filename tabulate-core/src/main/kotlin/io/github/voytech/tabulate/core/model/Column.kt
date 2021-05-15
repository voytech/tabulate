package io.github.voytech.tabulate.core.model

import io.github.voytech.tabulate.core.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.core.model.attributes.alias.ColumnAttribute

data class Column<T> internal constructor(
    val id: ColumnKey<T>,
    val index: Int?,
    val columnType: CellType?,
    val columnAttributes: Set<ColumnAttribute>?,
    val cellAttributes: Set<CellAttribute>?,
    val dataFormatter: ((field: Any) -> Any)? = null
) {
    internal fun resolveRawValue(value: T?): Any? = value?.let { id.ref?.invoke(it) }
}

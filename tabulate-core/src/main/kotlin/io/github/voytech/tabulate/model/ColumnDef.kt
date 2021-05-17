package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute

data class ColumnDef<T> internal constructor(
    val id: ColumnKey<T>,
    val index: Int?,
    val columnType: CellType?,
    val columnAttributes: Set<ColumnAttribute>?,
    val cellAttributes: Set<CellAttribute>?
) {
    internal fun resolveRawValue(value: T?): Any? = value?.let { id.ref?.invoke(it) }
}

package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.template.context.CellValue

data class Row<T> internal constructor(
    val selector: RowSelector<T>? = null,
    val createAt: Int? = null,
    val rowAttributes: Set<RowAttribute>?,
    val cellAttributes: Set<CellAttribute>?,
    val cells: Map<ColumnKey<T>, Cell<T>>?
)

internal fun <T> Map<ColumnKey<T>, Cell<T>>?.resolveCellValue(column: Column<T>, maybeRow: SourceRow<T>? = null): CellValue? {
    return this?.get(column.id).resolveCellValue(column, maybeRow)
}

@JvmName("resolveCell2")
internal fun <T> Map<Column<T>, Cell<T>>?.resolveCellValue(column: Column<T>, maybeRow: SourceRow<T>? = null): CellValue? {
    return this?.get(column).resolveCellValue(column, maybeRow)
}
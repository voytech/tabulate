package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.attributes.alias.CellAttribute
import pl.voytech.exporter.core.model.attributes.alias.RowAttribute
import pl.voytech.exporter.core.template.context.CellValue

data class Row<T> internal constructor(
    val selector: RowSelector<T>? = null,
    val createAt: Int? = null,
    val rowAttributes: Set<RowAttribute>?,
    val cellAttributes: Set<CellAttribute>?,
    val cells: Map<ColumnKey<T>, Cell<T>>?
)

fun <T> Map<ColumnKey<T>, Cell<T>>?.resolveCell(column: Column<T>, maybeRow: SourceRow<T>? = null): CellValue? {
    return this?.get(column.id)?.let { cell ->
        cell.resolveValue(maybeRow)?.let { rawValue ->
            CellValue(
                rawValue,
                cell.type ?: column.columnType,
                cell.colSpan,
                cell.rowSpan
            )
        }
    } ?: column.resolveValue(maybeRow?.record)?.let { CellValue(it, column.columnType) }
}

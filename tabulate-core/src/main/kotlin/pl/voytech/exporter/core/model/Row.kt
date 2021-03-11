package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.attributes.RowAttribute
import pl.voytech.exporter.core.model.attributes.alias.CellAttribute

data class Row<T> internal constructor(
    val selector: RowSelector<T>? = null,
    val createAt: Int? = null,
    val rowAttributes: Set<RowAttribute>?,
    val cellAttributes: Set<CellAttribute>?,
    val cells: Map<ColumnKey<T>, Cell<T>>?
)

fun <T> Map<ColumnKey<T>, Cell<T>>?.resolveCellValue(key: ColumnKey<T>, maybeRow: SourceRow<T>? = null): Any? {
    return this?.get(key)?.let { cell ->
        maybeRow?.let { row ->
            cell.eval?.invoke(row)
                ?: cell.value
                ?: row.record?.let { record -> key.ref?.invoke(record) }
        } ?: cell.value
    } ?: maybeRow?.let { row ->
        row.record?.let { record -> key.ref?.invoke(record) }
    }
}
package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.CellKey.Companion.cellKey
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.RowExtension

data class Row<T> internal constructor(
    val selector: RowSelector<T>? = null,
    val createAt: Int? = null,
    val rowExtensions: Set<RowExtension>?,
    val cellExtensions: Set<CellExtension>?,
    val cells: Map<CellKey<T>, Cell<T>>?
)

fun <T> Map<CellKey<T>, Cell<T>>.get(columnKey: ColumnKey<T>, columnIndex: Int? = -1): Cell<T>? {
    return get(cellKey(columnKey), columnIndex)
}

fun <T> Map<CellKey<T>, Cell<T>>.get(cellKey: CellKey<T>, columnIndex: Int? = -1): Cell<T>? {
    return get(cellKey) ?: (filter { it.key.columnIndex == columnIndex }).toList().map { it.second }.firstOrNull()
}

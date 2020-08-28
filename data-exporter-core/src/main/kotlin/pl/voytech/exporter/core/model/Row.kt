package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.api.builder.fluent.RowBuilder
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.RowExtension

data class Row<T> internal constructor(
    val selector: RowSelector<T>? = null,
    val createAt: Int? = null,
    val rowExtensions: Set<RowExtension>?,
    val cellExtensions: Set<CellExtension>?,
    val cells: Map<Key<T>, Cell<T>>?
) {
    companion object {
        @JvmStatic
        fun <T> builder() = RowBuilder<T>()
    }
}

package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.api.builder.fluent.TableBuilder
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.TableExtension

/**
 * A top-level model class. Defines how records from repositories will be handled by file rendering strategies.
 *
 * special property class - '...extensions' holds a list of 'custom attributes' for enriching
 * capabilities of exporters for different output formats.
 * It has been called 'extensions' rather than 'customAttributes' because it should be possible to:
 * - always export data without any extensions applied using each implemented exporter,
 * - custom attributes reserved for particular exporter does not restricts model for being exported only by this
 *   particular exporter,
 * - effectively any model with any custom attribute should be compatible with any exporter,
 * - 'extension' seems to better describe the model entity than 'custom attribute' as custom attribute seems to have the same
 *   priority as canonical attributes (fields of Table class). Also 'custom attribute' seems to be applicable by all
 *   exporters in the same manner (it is custom only because it is unknown at compile time e.g.). extension on the other hand
 *   is just a 'suggestion' which may not be applicable for exporter in all available contexts, but this fact should not break compatibility between model
 *   and exporter.
 *
 *   @author Wojciech Mąka
 */
data class Table<T> internal constructor(
    val name: String? = "untitled",
    val firstRow: Int? = 0,
    val firstColumn: Int? = 0,
    val columns: List<Column<T>> = emptyList(),
    val rows: List<Row<T>>?,
    val showHeader: Boolean? = false,
    val showFooter: Boolean? = false,
    val columnsDescription: Description?,
    val rowsDescription: Description?,
    val tableExtensions: Set<TableExtension>?,
    val cellExtensions: Set<CellExtension>?
) {
    companion object {
        @JvmStatic
        fun <T> builder() = TableBuilder<T>()
    }
}

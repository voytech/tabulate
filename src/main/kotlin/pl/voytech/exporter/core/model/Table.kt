package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.hints.TableHint

/**
 * A top-level model class. Defines how records from repositories will be handled by file rendering strategies.
 *
 * 'hints' is special field which opens model for extensions. It holds a list of 'custom attributes' for enriching
 * capabilities of exporters for different output formats.
 * It has been called 'hints' rather than 'customAttributes' because it should be possible to:
 * - always export data without any hints applied using each implemented exporter,
 * - custom attributes (hints) reserved for particular exporter does not restricts model for being exported only by this
 *   particular exporter,
 * - effectively any model with any custom attribute should be compatible with any exporter,
 * - 'hint' seems to better describe the model entity than 'custom attribute' as custom attribute seems to have the same
 *   priority as canonical attributes (fields of Table class). Also 'custom attribute' seems to be applicable by all
 *   exporters in the same manner (it is custom only because it is unknown at compile time e.g.). Hint on the other hand
 *   is just a 'suggestion' which may not be applicable for exporter, but should not break compatibility between model
 *   and exporter.
 */
data class Table<T>(
   val columns : List<Column<T>> = emptyList(),
   val rowRanges: List<LongRange> = listOf(infinite()),
   val showHeader: Boolean? = false,
   val showFooter: Boolean? = false,
   val name: String? = "untitled",
   val headerText: String? = "",
   val footerText: String? ="",
   val hints: List<TableHint>? = emptyList()
)

package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.attributes.ColumnAttribute


enum class ColumnRenderPhase {
    BEFORE_FIRST_ROW,
    AFTER_LAST_ROW
}

class ColumnOperationTableData {
    var columnValues: List<CellValue>? = null
        internal set
    var columnAttributes: Set<ColumnAttribute>? = null
        internal set
    var currentPhase: ColumnRenderPhase = ColumnRenderPhase.BEFORE_FIRST_ROW
}

data class OperationContext<E>(
    val additionalAttributes: MutableMap<String, Any>
) {
    lateinit var coordinates: Coordinates
        internal set
    var data: E? = null
        internal set
}

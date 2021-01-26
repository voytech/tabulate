package pl.voytech.exporter.core.template.context

import pl.voytech.exporter.core.model.attributes.ColumnAttribute


enum class ColumnRenderPhase {
    BEFORE_FIRST_ROW,
    AFTER_LAST_ROW
}

class ColumnOperationTableData {
    var columnAttributes: Set<ColumnAttribute>? = null
        internal set
    var currentPhase: ColumnRenderPhase = ColumnRenderPhase.BEFORE_FIRST_ROW
}

open class OperationContext<E>(val additionalAttributes: MutableMap<String, Any>) {
    var data: E? = null
        internal set
}

open class BaseOperationContext<E>(open val tableId: String, additionalAttributes: MutableMap<String, Any>) : OperationContext<E>(
    additionalAttributes
) {
    var rowIndex: Int = 0
        internal set
}

class RowOperationContext<T>(override val tableId: String, additionalAttributes: MutableMap<String, Any>) :
    BaseOperationContext<AttributedRow<T>>(tableId, additionalAttributes)

class CellOperationContext(override val tableId: String, additionalAttributes: MutableMap<String, Any>) :
    BaseOperationContext<AttributedCell>(tableId, additionalAttributes) {
    var columnIndex: Int = 0
        internal set
}

class ColumnOperationContext(val tableId: String, additionalAttributes: MutableMap<String, Any>) :
    OperationContext<ColumnOperationTableData>(
        additionalAttributes
    ) {
    var columnIndex: Int = 0
        internal set
}







package pl.voytech.exporter.core.template.context

enum class ColumnRenderPhase {
    BEFORE_FIRST_ROW,
    AFTER_LAST_ROW
}

open class ContextData<T> {
    var additionalAttributes: MutableMap<String, Any>? = null
        internal set

    fun getTableId(): String {
        return (additionalAttributes?.get("_tableId") ?: error("")) as String
    }
}


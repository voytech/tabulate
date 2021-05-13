package pl.voytech.exporter.core.template.context

open class ContextData<T> {
    var additionalAttributes: MutableMap<String, Any>? = null
        internal set

    fun getTableId(): String {
        return (additionalAttributes?.get("_tableId") ?: error("")) as String
    }
}
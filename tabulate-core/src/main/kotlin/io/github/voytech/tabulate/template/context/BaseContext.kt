package io.github.voytech.tabulate.template.context

sealed interface Context {
    fun getContextAttributes() : MutableMap<String, Any>?
}

sealed class ContextData : Context {
    internal var additionalAttributes: MutableMap<String, Any>? = null
    override fun getContextAttributes(): MutableMap<String, Any>? = additionalAttributes
}

sealed interface ContextPart: Context

fun Context.getTableId(): String {
    return (getContextAttributes()?.get("_tableId") ?: error("")) as String
}
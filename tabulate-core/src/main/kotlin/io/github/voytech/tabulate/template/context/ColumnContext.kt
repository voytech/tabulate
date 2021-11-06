package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.attributes.ColumnAttribute

abstract class ColumnContext: ContextData(), ColumnCoordinate {
    abstract fun <T: ColumnAttribute<T>> getAttribute(clazz: Class<ColumnAttribute<T>>):  List<ColumnAttribute<T>>
    abstract fun currentPhase(): ColumnRenderPhase?
}

fun AttributedColumn.crop(): ColumnContext = object: ColumnContext() {
    private val attributeMap: Map<Class<ColumnAttribute<*>>,List<ColumnAttribute<*>>> by lazy {
        columnAttributes?.groupBy { it.javaClass } ?: emptyMap()
    }

    init {
        additionalAttributes = this@crop.additionalAttributes
    }

    override fun getColumn(): Int  = columnIndex

    @Suppress("UNCHECKED_CAST")
    override fun <T : ColumnAttribute<T>> getAttribute(clazz: Class<ColumnAttribute<T>>): List<ColumnAttribute<T>> =
            attributeMap[clazz as Class<ColumnAttribute<*>>] as List<ColumnAttribute<T>>

    override fun currentPhase(): ColumnRenderPhase? = currentPhase

}

enum class ColumnRenderPhase {
    BEFORE_FIRST_ROW,
    AFTER_LAST_ROW
}
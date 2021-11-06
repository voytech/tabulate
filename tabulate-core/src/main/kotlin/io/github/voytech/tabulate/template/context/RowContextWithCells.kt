package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.ColumnKey
import io.github.voytech.tabulate.model.attributes.RowAttribute


abstract class RowContext : ContextData(), RowCoordinate {
    abstract fun <T: RowAttribute<T>> getAttributes(clazz: Class<T>):  List<T>
}

abstract class RowContextWithCells<T> : RowContext() {
    abstract fun getCells(): Map<ColumnKey<T>, RowCellContext>
}

fun <T> AttributedRowWithCells<T>.crop(): RowContextWithCells<T> = object : RowContextWithCells<T>() {
    private val attributeMap: Map<Class<RowAttribute<*>>,List<RowAttribute<*>>> by lazy {
        rowAttributes?.groupBy { it.javaClass } ?: emptyMap()
    }

    init {
        additionalAttributes = this@crop.additionalAttributes
    }

    override fun getRow(): Int = rowIndex

    @Suppress("UNCHECKED_CAST")
    override fun <T : RowAttribute<T>> getAttributes(clazz: Class<T>): List<T> =
        if (attributeMap.containsKey(clazz as Class<RowAttribute<*>>)) {
            attributeMap[clazz] as List<T>
        } else emptyList()

    override fun getCells(): Map<ColumnKey<T>, RowCellContext> = rowCellValues.crop()

}

fun <T> AttributedRow<T>.crop(): RowContext = object: RowContext() {
    private val attributeMap: Map<Class<RowAttribute<*>>,List<RowAttribute<*>>> by lazy {
        rowAttributes?.groupBy { it.javaClass } ?: emptyMap()
    }

    init {
        additionalAttributes = this@crop.additionalAttributes
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : RowAttribute<T>> getAttributes(clazz: Class<T>): List<T> =
        if (attributeMap.containsKey(clazz as Class<RowAttribute<*>>)) {
            attributeMap[clazz] as List<T>
        } else emptyList()

    override fun getRow(): Int = rowIndex

}

private fun <T> Map<ColumnKey<T>, AttributedCell>.crop(): Map<ColumnKey<T>, RowCellContext> {
    return entries.associate {
        it.key to it.value.crop()
    }
}
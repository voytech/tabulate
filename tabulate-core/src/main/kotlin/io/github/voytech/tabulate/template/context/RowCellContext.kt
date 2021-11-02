package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.attributes.CellAttribute

abstract class RowCellContext : ContextData(), RowCellCoordinate {
    abstract fun getValue(): CellValue
    abstract fun getRawValue(): Any
    abstract fun <T: CellAttribute<T>> getAttribute(clazz: Class<CellAttribute<T>>):  List<CellAttribute<T>>
}

@Suppress("UNCHECKED_CAST")
fun AttributedCell.crop(): RowCellContext = object : RowCellContext() {
    private val attributeMap: Map<Class<CellAttribute<*>>,List<CellAttribute<*>>> by lazy {
         attributes?.groupBy { it.javaClass } ?: emptyMap()
    }

    init {
        additionalAttributes = this@crop.additionalAttributes
    }

    override fun getValue(): CellValue = value

    override fun getRawValue(): Any = value.value

    override fun getRow(): Int = rowIndex

    override fun getColumn(): Int = columnIndex

    override fun <T : CellAttribute<T>> getAttribute(clazz: Class<CellAttribute<T>>): List<CellAttribute<T>> =
        attributeMap[clazz as Class<CellAttribute<*>>] as List<CellAttribute<T>>
}

//fun RowCellContext.getTypeHintAttribute(): CellAttribute<TypeHintAttribute>? =
//    getAttribute<TypeHintAttribute>(TypeHintAttribute::class.java).firstOrNull()
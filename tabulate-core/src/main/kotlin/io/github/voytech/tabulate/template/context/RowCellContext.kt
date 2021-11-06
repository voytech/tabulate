package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.model.attributes.cell.TypeHintAttribute

abstract class RowCellContext : ContextData(), RowCellCoordinate {
    abstract fun getValue(): CellValue
    abstract fun getRawValue(): Any
    abstract fun <T: CellAttribute<T>> getAttributes(clazz: Class<T>):  List<T>
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

    override fun <T : CellAttribute<T>> getAttributes(clazz: Class<T>): List<T> =
        if (attributeMap.containsKey(clazz as Class<CellAttribute<*>>)) {
            attributeMap[clazz] as List<T>
        } else emptyList()

}

inline fun <reified T: CellAttribute<T>> RowCellContext.getAttributes():  List<T> = getAttributes(T::class.java)

inline fun <reified T: CellAttribute<T>> RowCellContext.getFirstAttributeOrNull():  T? =
        getAttributes(T::class.java).firstOrNull()

fun RowCellContext.getTypeHint(): TypeHintAttribute? = getFirstAttributeOrNull<TypeHintAttribute>()
package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.attributes.*

/**
 * Class encapsulates shared, generic logic for accessing attribute instance of selected attribute class.
 * Class is intended to be derived by all classes representing attribute-trimmed contexts like:
 * [RowCellContext], [ColumnContext], [RowContext], [RowContextWithCells], [TableContext]
 * @author Wojciech Mąka
 */
sealed class ModelAttributeAccessor<A : Attribute<*>>(attributedContext: AttributedModel<A>) {
    private val attributeMap: Map<Class<out A>,List<A>> by lazy {
        attributedContext.attributes?.groupBy { it.javaClass } ?: emptyMap()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : A> getModelAttributes(clazz: Class<T>): List<T> =
            if (attributeMap.containsKey(clazz)) {
                attributeMap[clazz] as List<T>
            } else emptyList()
}

@JvmName("getCellModelAttributes")
inline fun <reified T: CellAttribute<T>> ModelAttributeAccessor<CellAttribute<*>>.getModelAttributes(): List<T> =
        getModelAttributes(T::class.java)

@JvmName("getColumnModelAttributes")
inline fun <reified T: ColumnAttribute<T>> ModelAttributeAccessor<ColumnAttribute<*>>.getModelAttributes(): List<T> =
        getModelAttributes(T::class.java)

@JvmName("getRowModelAttributes")
inline fun <reified T: RowAttribute<T>> ModelAttributeAccessor<RowAttribute<*>>.getModelAttributes(): List<T> =
        getModelAttributes(T::class.java)

@JvmName("getTableModelAttributes")
inline fun <reified T: TableAttribute<T>> ModelAttributeAccessor<TableAttribute<*>>.getModelAttributes(): List<T> =
        getModelAttributes(T::class.java)

inline fun <reified T: CellAttribute<T>> ModelAttributeAccessor<CellAttribute<*>>.getFirstAttributeOrNull(): T? =
        getModelAttributes(T::class.java).firstOrNull()

inline fun <reified T: ColumnAttribute<T>> ModelAttributeAccessor<ColumnAttribute<*>>.getFirstAttributeOrNull(): T? =
        getModelAttributes(T::class.java).firstOrNull()

inline fun <reified T: RowAttribute<T>> ModelAttributeAccessor<RowAttribute<*>>.getFirstAttributeOrNull(): T? =
        getModelAttributes(T::class.java).firstOrNull()

inline fun <reified T: TableAttribute<T>> ModelAttributeAccessor<TableAttribute<*>>.getFirstAttributeOrNull(): T? =
        getModelAttributes(T::class.java).firstOrNull()
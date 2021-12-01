package io.github.voytech.tabulate.model

import kotlin.reflect.KProperty1

/**
 * Defines column's unique identifier which is used to bind cell definition with specific column.
 * [ColumnKey] may use [name] or [property] id internally. Name is simple text id to identify columns composed only from
 * custom cells - cells that cannot extract value from collection element.
 * Property key is in other hand an instance of [PropertyReferenceColumnKey] which is a property getter literal solving
 * two problems at once:
 *  - identifies column which allows to locate row cell,
 *  - extracts value from collection element at given property to be later rendered as cell value.
 */
data class ColumnKey<T> internal constructor(
    /**
     * Simple textual id of a column.
     */
    @get:JvmSynthetic
    internal val name: String? = null,
    /**
     * Multi-purpose property reference literal id used to identify column and extract value from object at given property.
     */
    @get:JvmSynthetic
    internal val property: PropertyReferenceColumnKey<T>? = null
) {
    companion object {
        fun <T> field(fieldRef: KProperty1<T,Any>?) = ColumnKey(property = fieldRef?.id())
    }
    @JvmSynthetic
    fun resolveValue(value: T?): Any? = value?.let { property?.getPropertyValue(it) }

    @Override
    override fun toString(): String {
        return "ColumnKey = ${name ?: property}"
    }
}
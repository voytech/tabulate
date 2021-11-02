package io.github.voytech.tabulate.model

import kotlin.reflect.KProperty1

data class ColumnKey<T> internal constructor(
    @get:JvmSynthetic
    internal val name: String? = null,
    @get:JvmSynthetic
    internal val property: PropertyBindingKey<T>? = null
) {
    companion object {
        fun <T> field(fieldRef: KProperty1<T,Any>?) = ColumnKey(property = fieldRef?.id())
    }
    @JvmSynthetic
    fun resolveValue(value: T?): Any? = value?.let { property?.invoke(it) }

    @Override
    override fun toString(): String {
        return "ColumnKey = ${name ?: property}"
    }
}
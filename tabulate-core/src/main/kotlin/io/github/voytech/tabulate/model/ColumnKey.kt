package io.github.voytech.tabulate.model

import kotlin.reflect.KProperty1

data class ColumnKey<T> internal constructor(
    @get:JvmSynthetic
    internal val id: String? = null,
    @get:JvmSynthetic
    internal val ref: PropertyBindingKey<T>? = null
) {
    companion object {
        fun <T> field(fieldRef: KProperty1<T,Any>?) = ColumnKey(ref = fieldRef?.id())
    }
    @JvmSynthetic
    fun resolveValue(value: T?): Any? = value?.let { ref?.invoke(it) }

    @Override
    override fun toString(): String {
        return "ColumnKey = ${id ?: ref}"
    }
}
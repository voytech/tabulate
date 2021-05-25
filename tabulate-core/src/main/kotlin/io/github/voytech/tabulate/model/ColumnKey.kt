package io.github.voytech.tabulate.model

import kotlin.reflect.KProperty1

data class ColumnKey<T> internal constructor(
    val id: String? = null,
    val ref: ColRefId<T>? = null//KProperty1<T,Any?>? = null//((record: T) -> Any?)? = null,
) {
    companion object {
        fun <T> field(fieldRef: KProperty1<T,Any>?) = ColumnKey(ref = fieldRef?.id())
    }

    fun resolveValue(value: T?): Any? = value?.let { ref?.invoke(it) }
}
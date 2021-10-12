package io.github.voytech.tabulate.model

import kotlin.reflect.KProperty1

fun interface RowCellExpression<T> {
    fun evaluate(context: SourceRow<T>): Any?
}

fun interface PropertyBindingKey<T> {
    fun invoke(record: T) : Any?
}

private object PropertyBindingsCache {

    private val CACHE: MutableMap<String, PropertyBindingKey<*>> by lazy { mutableMapOf() }

    @Suppress("UNCHECKED_CAST")
    fun <T> cached(ref: KProperty1<T, Any?>): PropertyBindingKey<T> {
        return CACHE.computeIfAbsent(ref.toString()) {
            PropertyBindingKey<T> { row -> ref.get(row) }
        } as PropertyBindingKey<T>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> cached(ref: java.util.function.Function<T, Any?>): PropertyBindingKey<T> {
        return CACHE.computeIfAbsent(ref.hashCode().toString()) {
            PropertyBindingKey<T> { row -> ref.apply(row) }
        } as PropertyBindingKey<T>
    }
}

fun <T> KProperty1<T, Any?>.id() : PropertyBindingKey<T> {
    return PropertyBindingsCache.cached(this)
}

fun <T> java.util.function.Function<T, Any?>.id() : PropertyBindingKey<T>  {
    return PropertyBindingsCache.cached(this)
}
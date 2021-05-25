package io.github.voytech.tabulate.model

import kotlin.reflect.KProperty1

fun interface RowCellExpression<T> {
    fun evaluate(context: SourceRow<T>): Any?
}

fun interface ColRefId<T> {
    fun invoke(record: T) : Any?
}

private object PropCache {

    private val cache: MutableMap<Int, ColRefId<*>> by lazy { mutableMapOf() }

    @Suppress("UNCHECKED_CAST")
    fun <T> cached(ref: KProperty1<T, Any?>): ColRefId<T> {
        return cache.computeIfAbsent(ref.hashCode()) {
            ColRefId<T> { row -> ref.get(row) }
        } as ColRefId<T>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> cached(ref: java.util.function.Function<T, Any?>): ColRefId<T> {
        return cache.computeIfAbsent(ref.hashCode()) {
            ColRefId<T> { row -> ref.apply(row) }
        } as ColRefId<T>
    }
}

fun <T> KProperty1<T, Any?>.id() : ColRefId<T> {
    return PropCache.cached(this)
}

fun <T> java.util.function.Function<T, Any?>.id() : ColRefId<T>  {
    return PropCache.cached(this)
}
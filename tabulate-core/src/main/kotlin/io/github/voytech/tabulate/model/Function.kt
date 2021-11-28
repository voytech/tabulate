package io.github.voytech.tabulate.model

import kotlin.reflect.KProperty1

fun interface RowCellExpression<T> {
    fun evaluate(context: SourceRow<T>): Any?
}

fun interface PropertyReferenceColumnKey<T> {
    fun getPropertyValue(record: T) : Any?
}

private object PropertyReferencesCache {

    private val CACHE: MutableMap<String, PropertyReferenceColumnKey<*>> by lazy { mutableMapOf() }

    @Suppress("UNCHECKED_CAST")
    fun <T> cached(ref: KProperty1<T, Any?>): PropertyReferenceColumnKey<T> {
        return CACHE.computeIfAbsent(ref.toString()) {
            PropertyReferenceColumnKey<T> { row -> ref(row) }
        } as PropertyReferenceColumnKey<T>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> cached(ref: java.util.function.Function<T, Any?>): PropertyReferenceColumnKey<T> {
        return CACHE.computeIfAbsent(ref.hashCode().toString()) {
            PropertyReferenceColumnKey<T> { row -> ref.apply(row) }
        } as PropertyReferenceColumnKey<T>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> cached(uniqueName: String, ref: java.util.function.Function<T, Any?>): NamedPropertyReference<T> =
        CACHE.computeIfAbsent(uniqueName) { NamedPropertyReference(ref) } as NamedPropertyReference<T>

    @Suppress("UNCHECKED_CAST")
    fun <T> cached(uniqueName: String): NamedPropertyReference<T> =
        (CACHE[uniqueName] as?  NamedPropertyReference<T>)
            ?: error("PropertyReferenceColumnKey for key $uniqueName not found!")


}

fun <T> KProperty1<T, Any?>.id() : PropertyReferenceColumnKey<T> {
    return PropertyReferencesCache.cached(this)
}

class NamedPropertyReference<T>(private val reference: java.util.function.Function<T, Any?>): PropertyReferenceColumnKey<T> {

    override fun getPropertyValue(record: T): Any? = reference.apply(record)

    companion object {
        @JvmStatic
        fun <T> of(uniqueName: String, reference: java.util.function.Function<T, Any?>): NamedPropertyReference<T> {
            return PropertyReferencesCache.cached(uniqueName, reference)
        }
        @JvmStatic
        fun <T> of(uniqueName: String): NamedPropertyReference<T> {
            return PropertyReferencesCache.cached(uniqueName)
        }
    }
}

fun <T> java.util.function.Function<T, Any?>.id() : PropertyReferenceColumnKey<T>  {
    return PropertyReferencesCache.cached(this)
}
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
    fun <T> cached(uniqueName: String, ref: java.util.function.Function<T, Any?>): NamedPropertyReferenceColumnKey<T> =
        CACHE.computeIfAbsent(uniqueName) { NamedPropertyReferenceColumnKey(ref) } as NamedPropertyReferenceColumnKey<T>

    @Suppress("UNCHECKED_CAST")
    fun <T> cached(uniqueName: String): NamedPropertyReferenceColumnKey<T> =
        (CACHE[uniqueName] as?  NamedPropertyReferenceColumnKey<T>)
            ?: error("PropertyReferenceColumnKey for key $uniqueName not found!")

}

fun <T> KProperty1<T, Any?>.id() : PropertyReferenceColumnKey<T> {
    return PropertyReferencesCache.cached(this)
}

class NamedPropertyReferenceColumnKey<T>(private val reference: java.util.function.Function<T, Any?>): PropertyReferenceColumnKey<T> {

    override fun getPropertyValue(record: T): Any? = reference.apply(record)

    companion object {
        @JvmStatic
        fun <T> of(uniqueName: String, reference: java.util.function.Function<T, Any?>): NamedPropertyReferenceColumnKey<T> {
            return PropertyReferencesCache.cached(uniqueName, reference)
        }
        @JvmStatic
        fun <T> of(uniqueName: String): NamedPropertyReferenceColumnKey<T> {
            return PropertyReferencesCache.cached(uniqueName)
        }
    }
}

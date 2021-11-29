package io.github.voytech.tabulate.model

import kotlin.reflect.KProperty1

fun interface RowCellExpression<T> {
    fun evaluate(context: SourceRow<T>): Any?
}

fun interface PropertyReferenceColumnKey<T> {
    fun getPropertyValue(record: T) : Any?
}

private object PropertyReferencesCache {

    private val CACHE: MutableMap<Any, PropertyReferenceColumnKey<*>> by lazy { mutableMapOf() }

    @Suppress("UNCHECKED_CAST")
    fun <T> cached(ref: KProperty1<T, Any?>): PropertyReferenceColumnKey<T> {
        return CACHE.computeIfAbsent(ref) {
            PropertyLiteralColumnKey(ref)
        } as PropertyReferenceColumnKey<T>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> cached(uniqueName: String, ref: java.util.function.Function<T, Any?>): NamedPropertyReferenceColumnKey<T> =
        CACHE.computeIfAbsent(uniqueName) {
            NamedPropertyReferenceColumnKey(uniqueName, ref)
        } as NamedPropertyReferenceColumnKey<T>

    @Suppress("UNCHECKED_CAST")
    fun <T> cached(uniqueName: String): NamedPropertyReferenceColumnKey<T> =
        (CACHE[uniqueName] as?  NamedPropertyReferenceColumnKey<T>)
            ?: error("PropertyReferenceColumnKey for key $uniqueName not found!")

}

fun <T> KProperty1<T, Any?>.id() : PropertyReferenceColumnKey<T> {
    return PropertyReferencesCache.cached(this)
}

@JvmInline
value class PropertyLiteralColumnKey<T>(private val propertyLiteral: KProperty1<T, Any?>) : PropertyReferenceColumnKey<T> {
    override fun getPropertyValue(record: T): Any? = propertyLiteral(record)
}

class NamedPropertyReferenceColumnKey<T>(
    private val key: String,
    private val reference: java.util.function.Function<T, Any?>
): PropertyReferenceColumnKey<T> {

    override fun getPropertyValue(record: T): Any? = reference.apply(record)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as NamedPropertyReferenceColumnKey<*>
        if (key != other.key) return false
        return true
    }

    override fun hashCode(): Int = key.hashCode()

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

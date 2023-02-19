package io.github.voytech.tabulate.components.table.model

import kotlin.reflect.KProperty1

/**
 * Defines object's property reference "literal" based column key.
 */
fun interface PropertyReferenceColumnKey<T> {
    fun getPropertyValue(record: T) : Any?
}

/**
 * Internal property reference literal cache.
 * @author Wojciech Mąka
 */
internal class PropertyReferencesCache {

    private val cache: MutableMap<String, NamedPropertyReferenceColumnKey<*>> by lazy { mutableMapOf() }

    @Suppress("UNCHECKED_CAST")
    fun <T> cached(ref: NamedPropertyReferenceColumnKey<T>): NamedPropertyReferenceColumnKey<T> {
        return cache.computeIfAbsent(ref.key) { ref } as NamedPropertyReferenceColumnKey<T>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> cached(key: String): NamedPropertyReferenceColumnKey<T>? {
        return cache[key] as NamedPropertyReferenceColumnKey<T>?
    }

}

fun <T> KProperty1<T, Any?>.id() : PropertyReferenceColumnKey<T> {
    return PropertyLiteralColumnKey(this)
}

/**
 * Kotlin [KProperty1] based property reference literal
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@JvmInline
value class PropertyLiteralColumnKey<T>(private val propertyLiteral: KProperty1<T, Any?>) : PropertyReferenceColumnKey<T> {
    override fun getPropertyValue(record: T): Any? = propertyLiteral(record)
}

/**
 * Java [java.util.function.Function] based property literal. Should be used by passing getter method reference as function,
 * and unique id to make this key referencable from cell definition.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
internal class NamedPropertyReferenceColumnKey<T>(
    internal val key: String,
    private val reference: java.util.function.Function<T, Any?>,
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
}

package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.model.attributes.*

@JvmInline
internal value class AttributeClassBasedCache<T : Attribute<*>>(
    private val cache: MutableMap<Set<T>, MutableMap<String, Any>> = mutableMapOf()
) {
    operator fun get(key: Set<T>): MutableMap<String, Any> = cache[key] ?: kotlin.run {
        cache[key] = mutableMapOf()
        cache[key]!!
    }

    operator fun set(key: Set<T>, value: MutableMap<String, Any>) {
        cache[key] = value
    }
}

@Suppress("UNCHECKED_CAST")
class AttributeSetBasedCache {
    private val rowAttributesAsKeyCache: AttributeClassBasedCache<RowAttribute<*>> = AttributeClassBasedCache()
    private val cellAttributesAsKeyCache: AttributeClassBasedCache<CellAttribute<*>> = AttributeClassBasedCache()
    private val columnAttributesAsKeyCache: AttributeClassBasedCache<ColumnAttribute<*>> = AttributeClassBasedCache()
    private val tableAttributesAsKeyCache: AttributeClassBasedCache<TableAttribute<*>> = AttributeClassBasedCache()

    internal inline fun <reified T : Attribute<*>> getCache(attributes: Set<T>): MutableMap<String, Any> {
        return when  {
            TableAttribute::class.java == T::class.java -> tableAttributesAsKeyCache[attributes as Set<TableAttribute<*>>]
            ColumnAttribute::class.java == T::class.java -> columnAttributesAsKeyCache[attributes as Set<ColumnAttribute<*>>]
            RowAttribute::class.java == T::class.java -> rowAttributesAsKeyCache[attributes as Set<RowAttribute<*>>]
            CellAttribute::class.java == T::class.java -> cellAttributesAsKeyCache[attributes as Set<CellAttribute<*>>]
            else -> error("Requested attribute class is not supported!")
        }
    }
}

internal fun ContextData.ensureAttributeSetBasedCache(): AttributeSetBasedCache {
    additionalAttributes!!.putIfAbsent("_attribute_set_based_cache", AttributeSetBasedCache())
    return additionalAttributes!!["_attribute_set_based_cache"] as AttributeSetBasedCache
}

inline fun <reified T : Attribute<*>> getAttributeClassId(): String = when  {
    TableAttribute::class.java == T::class.java -> "table"
    ColumnAttribute::class.java == T::class.java -> "column"
    RowAttribute::class.java == T::class.java -> "row"
    CellAttribute::class.java == T::class.java -> "cell"
    else -> error("Requested attribute class is not supported!")
}

internal inline fun <reified T : Attribute<*>> AttributedModel<T>.setupCacheAndGet(): MutableMap<String, Any>? {
    return this.attributes?.takeIf { it.isNotEmpty() }?.let {
        ensureAttributeSetBasedCache().getCache(it)
    }?.also {
        additionalAttributes?.put("_current_${getAttributeClassId<T>()}_attributes_cache", it)
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified M : Attribute<*>, T> T.putCachedValueIfAbsent(key: String, value: Any): Any
        where T : ModelAttributeAccessor<M>,
              T : Context =
    (getContextAttributes()?.get("_current_${getAttributeClassId<M>()}_attributes_cache") as MutableMap<String, Any>)
        .let { it.computeIfAbsent(key) { value } }

@Suppress("UNCHECKED_CAST")
inline fun <reified M : Attribute<*>, T> T.getCachedValue(key: String): Any?
        where T : ModelAttributeAccessor<M>,
              T : Context =
    (getContextAttributes()?.get("_current_${getAttributeClassId<M>()}_attributes_cache") as MutableMap<String, Any>)
        .let { it[key] }
package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.model.attributes.*

@JvmInline
internal value class AttributeClassBasedCache<T : Attribute<*>>(
    private val cache: MutableMap<Set<T>, MutableMap<String, Any>> = mutableMapOf()
) {
    @JvmSynthetic
    operator fun get(key: Set<T>): MutableMap<String, Any> = cache[key] ?: kotlin.run {
        cache[key] = mutableMapOf()
        cache[key]!!
    }

    @JvmSynthetic
    operator fun set(key: Set<T>, value: MutableMap<String, Any>) {
        cache[key] = value
    }
}

/**
 * Multi-level cache that resides in dedicated [ContextData.additionalAttributes] state entry. This cache have following
 * properties:
 *  - it uses attribute-set as cache key which enables passing attribute-set bound context across different rows, cells and columns.
 *  - it consists of nested internal caches representing all attribute categories (row, cell, table, column)
 * Usage Scenario:
 * If we have the same set of cell attributes defined on column level then it is possible that some of cells
 * belonging to this particular column shares exactly the same attributes. This means that as long as third party rendering context
 * maintains its own internal state per given attribute set, then it can be simply cached and applied on all compatible cells,
 * without need of instantiating logically equal objects multiple times.
 * @author Wojciech Mąka
 */
@Suppress("UNCHECKED_CAST")
internal class AttributeSetBasedCache {
    private val rowAttributesAsKeyCache: AttributeClassBasedCache<RowAttribute<*>> = AttributeClassBasedCache()
    private val cellAttributesAsKeyCache: AttributeClassBasedCache<CellAttribute<*>> = AttributeClassBasedCache()
    private val columnAttributesAsKeyCache: AttributeClassBasedCache<ColumnAttribute<*>> = AttributeClassBasedCache()
    private val tableAttributesAsKeyCache: AttributeClassBasedCache<TableAttribute<*>> = AttributeClassBasedCache()

    @JvmSynthetic
    internal inline fun <reified T : Attribute<*>> getCache(attributes: Attributes<T>): MutableMap<String, Any> {
        return when {
            TableAttribute::class.java == T::class.java -> tableAttributesAsKeyCache[attributes.attributeSet as Set<TableAttribute<*>>]
            ColumnAttribute::class.java == T::class.java -> columnAttributesAsKeyCache[attributes.attributeSet as Set<ColumnAttribute<*>>]
            RowAttribute::class.java == T::class.java -> rowAttributesAsKeyCache[attributes.attributeSet as Set<RowAttribute<*>>]
            CellAttribute::class.java == T::class.java -> cellAttributesAsKeyCache[attributes.attributeSet as Set<CellAttribute<*>>]
            else -> error("Requested attribute class (category) is not supported!")
        }
    }
}

/**
 * Obtains or creates (if does not exist) a cache instance. Cache resides in [ContextData.additionalAttributes]
 * @author Wojciech Mąka
 */
@JvmSynthetic
internal fun ContextData.ensureAttributeSetBasedCache(): AttributeSetBasedCache {
    additionalAttributes!!.putIfAbsent("_attribute_set_based_cache", AttributeSetBasedCache())
    return additionalAttributes!!["_attribute_set_based_cache"] as AttributeSetBasedCache
}

/**
 * Given attribute, resolves attribute category identifier. One of: [table|column|row|cell].
 * @author Wojciech Mąka
 */
inline fun <reified T : Attribute<*>> getAttributeClassId(): String = when {
    TableAttribute::class.java == T::class.java -> "table"
    ColumnAttribute::class.java == T::class.java -> "column"
    RowAttribute::class.java == T::class.java -> "row"
    CellAttribute::class.java == T::class.java -> "cell"
    else -> error("Requested attribute class is not supported!")
}

/**
 * Given [AttributedModel], resolves mutable map (internal cache). This internal cache is accessed by attribute-set
 * (`attributes` property) of this particular [AttributedModel] receiver.
 * @author Wojciech Mąka
 */
@JvmSynthetic
internal inline fun <reified T : Attribute<*>> AttributedModel<T>.setupCacheAndGet(): MutableMap<String, Any>? {
    return this.attributes?.takeIf { it.isNotEmpty() }?.let {
        ensureAttributeSetBasedCache().getCache(it)
    }
}

/**
 * Allows to perform operations in scope of given [AttributedModel] with its internal cache exposed using [AttributedModel]'s
 * attribute-set.
 * When using invocations like :
 * `attributedCell.skipAttributes().let { cellContext -> cellContext.cacheOnAttributeSet("someKey", "someVal") }`
 * we can put value to, or query internal cache valid for attribute-set `attributeCell.attributes` from `cellContext` which
 * itself does not expose attributes to consumer.
 * @author Wojciech Mąka
 */
@JvmSynthetic
internal inline fun <reified T : Attribute<*>> AttributedModel<T>.withAttributeSetBasedCache(block: (cache: MutableMap<String, Any>?) -> Unit) {
    return setupCacheAndGet()?.also {
        additionalAttributes?.put("_current_${getAttributeClassId<T>()}_attributes_cache", it)
    }.run(block).also {
        additionalAttributes?.remove("_current_${getAttributeClassId<T>()}_attributes_cache")
    }
}

/**
 * Given [ModelAttributeAccessor] (truncated, attribute-set-less [AttributedModel] view), caches any value under specific key.
 * Key-value pair is stored in internal cache valid for / accessed by [AttributedModel]'s attributes (attribute-set).
 * @author Wojciech Mąka
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified M : Attribute<*>, T> T.cacheOnAttributeSet(key: String, value: Any): Any
        where T : ModelAttributeAccessor<M>,
              T : Context =
    (getContextAttributes()?.get("_current_${getAttributeClassId<M>()}_attributes_cache") as? MutableMap<String, Any>)
        ?.let { it.computeIfAbsent(key) { value } } ?: error("cannot resolve cached value in scope!")

/**
 * Given [ModelAttributeAccessor] (truncated, attribute-set-less [AttributedModel] view), gets cached value stored under given key.
 * Key-value pair is stored in internal cache valid for / accessed by [AttributedModel]'s attributes (attribute-set).
 * @author Wojciech Mąka
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified M : Attribute<*>, T> T.getCachedOnAttributeSet(key: String): Any?
        where T : ModelAttributeAccessor<M>,
              T : Context =
    (getContextAttributes()?.get("_current_${getAttributeClassId<M>()}_attributes_cache") as? MutableMap<String, Any>)
        ?.let { it[key] } ?: error("cannot resolve cached value in scope!")

/**
 * Given [ModelAttributeAccessor] (truncated, attribute-set-less [AttributedModel] view), checks if there is value stored under given key.
 * Key-value pair is stored in internal cache valid for / accessed by [AttributedModel]'s attributes (attribute-set).
 * @author Wojciech Mąka
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified M : Attribute<*>, T> T.hasCachedOnAttributeSet(key: String): Boolean
        where T : ModelAttributeAccessor<M>,
              T : Context =
    (getContextAttributes()?.get("_current_${getAttributeClassId<M>()}_attributes_cache") as? MutableMap<String, Any>)
        ?.containsKey(key) ?: false
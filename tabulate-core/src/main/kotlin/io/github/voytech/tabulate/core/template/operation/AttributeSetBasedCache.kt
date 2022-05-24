package io.github.voytech.tabulate.core.template.operation

import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeCategoryAware
import io.github.voytech.tabulate.core.model.Attributes


@JvmInline
internal value class AttributeClassBasedCache<K : Attribute<*>, V>(
    private val cache: MutableMap<Attributes<K>, V> = mutableMapOf(),
) {
    @JvmSynthetic
    operator fun get(key: Attributes<K>): V? = cache[key]

    @JvmSynthetic
    operator fun set(key: Attributes<K>, value: V) {
        cache[key] = value
    }

    @JvmSynthetic
    fun compute(key: Attributes<K>, provider: () -> V): V =
        cache.computeIfAbsent(key) {
            provider()
        }
}

internal typealias AttributeClassBasedMapCache<K> = AttributeClassBasedCache<K,MutableMap<String, Any>>

/**
 * Obtains or creates (if does not exist) a cache instance. Cache resides in [ContextData.additionalAttributes]
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@JvmSynthetic
internal fun <T:  Attribute<*>> AttributedContext<T>.ensureAttributeSetBasedCache(): AttributeClassBasedMapCache<T> {
    additionalAttributes!!.putIfAbsent("_attribute_set_based_cache", AttributeClassBasedMapCache())
    return additionalAttributes!!["_attribute_set_based_cache"] as AttributeClassBasedMapCache<T>
}

/**
 * Given attribute, resolves attribute category identifier. One of: [table|column|row|cell].
 * @author Wojciech Mąka
 * @since 0.1.0
 */
fun <T : Attribute<*>> AttributeCategoryAware<T>.getAttributeClassId(): String = getAttributeCategoryClass().name


/**
 * Given [AttributedContext], resolves mutable map (internal cache). This internal cache is accessed by attribute set
 * (`attributes` property) of this particular [AttributedContext] receiver.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@JvmSynthetic
internal fun <T : Attribute<*>> AttributedContext<T>.setupCacheAndGet(): MutableMap<String, Any>? {
    return this.attributes?.takeIf { it.isNotEmpty() }?.let {
        this.ensureAttributeSetBasedCache().compute(it) { mutableMapOf() }
    }
}

/**
 * Allows to perform operations in scope of given [AttributedContext] with its internal cache exposed using [AttributedContext]'s
 * attribute-set.
 * When using invocations like :
 * `attributedCell.skipAttributes().let { cellContext -> cellContext.cacheOnAttributeSet("someKey", "someVal") }`
 * we can put value to, or query internal cache valid for attribute-set `attributeCell.attributes` from `cellContext` which
 * itself does not expose attributes to consumer.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@JvmSynthetic
internal fun <T : Attribute<*>> AttributedContext<T>.withAttributeSetBasedCache(block: (cache: MutableMap<String, Any>?) -> Unit) {
    return setupCacheAndGet()?.also {
        additionalAttributes?.put("_current_${attributes?.getAttributeClassId()}_attributes_cache", it)
    }.run(block).also {
        additionalAttributes?.remove("_current_${attributes?.getAttributeClassId()}_attributes_cache")
    }
}

/**
 * Given [ModelAttributeAccessor] (truncated, attribute-set-less [AttributedContext] view), caches any value under specific key.
 * Key-value pair is stored in internal cache valid for / accessed by [AttributedContext]'s attributes (attribute-set).
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@Suppress("UNCHECKED_CAST")
fun <M : Attribute<*>, T> T.cacheOnAttributeSet(key: String, value: Any): Any
        where T : AttributedContext<M>,
              T : Context =
    (getContextAttributes()?.get("_current_${getAttributeClassId()}_attributes_cache") as? MutableMap<String, Any>)
        ?.let { it.computeIfAbsent(key) { value } } ?: error("cannot resolve cached value in scope!")

/**
 * Given [ModelAttributeAccessor] (truncated, attribute-set-less [AttributedContext] view), gets cached value stored under given key.
 * Key-value pair is stored in internal cache valid for / accessed by [AttributedContext]'s attributes (attribute-set).
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@Suppress("UNCHECKED_CAST")
fun <M : Attribute<*>, T> T.getCachedOnAttributeSet(key: String): Any
        where T : AttributedContext<M>,
              T : Context =
    (getContextAttributes()?.get("_current_${getAttributeClassId()}_attributes_cache") as? MutableMap<String, Any>)
        ?.let { it[key] } ?: error("cannot resolve cached value in scope!")

/**
 * Given [ModelAttributeAccessor] (truncated, attribute-set-less [AttributedContext] view), checks if there is value stored under given key.
 * Key-value pair is stored in internal cache valid for / accessed by [AttributedContext]'s attributes (attribute-set).
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@Suppress("UNCHECKED_CAST")
fun <M : Attribute<*>, T> T.hasCachedOnAttributeSet(key: String): Boolean
        where T : AttributedContext<M>,
              T : Context =
    (getContextAttributes()?.get("_current_${getAttributeClassId()}_attributes_cache") as? MutableMap<String, Any>)
        ?.containsKey(key) ?: false
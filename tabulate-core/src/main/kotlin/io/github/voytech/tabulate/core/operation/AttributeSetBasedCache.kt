package io.github.voytech.tabulate.core.operation

import io.github.voytech.tabulate.core.model.AttributeAware
import io.github.voytech.tabulate.core.model.Attributes

@JvmInline
internal value class AttributeClassBasedCache<K : AttributeAware, V>(
    private val cache: MutableMap<Attributes, V> = mutableMapOf(),
) {
    @JvmSynthetic
    operator fun get(key: Attributes): V? = cache[key]

    @JvmSynthetic
    operator fun set(key: Attributes, value: V) {
        cache[key] = value
    }

    @JvmSynthetic
    fun compute(key: Attributes, provider: () -> V): V =
        cache.computeIfAbsent(key) {
            provider()
        }
}

internal typealias AttributeClassBasedMapCache<K> = AttributeClassBasedCache<K, MutableMap<String, Any>>

/**
 * Obtains or creates (if does not exist) a cache instance. Cache resides in [CustomAttributesData.additionalAttributes]
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@JvmSynthetic
internal fun <T : AttributedEntity> T.ensureAttributeSetBasedCache(): AttributeClassBasedMapCache<T> {
    additionalAttributes.putIfAbsent("_attribute_set_based_cache", AttributeClassBasedMapCache<T>())
    return additionalAttributes["_attribute_set_based_cache"] as AttributeClassBasedMapCache<T>
}


/**
 * Given [AttributedEntity], resolves mutable map (internal cache). This internal cache is accessed by attribute set
 * (`attributes` property) of this particular [AttributedEntity] receiver.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@JvmSynthetic
internal fun <T : AttributedEntity> T.setupCacheAndGet(): MutableMap<String, Any>? {
    return this.attributes?.takeIf { it.isNotEmpty() }?.let {
        ensureAttributeSetBasedCache().compute(it) { mutableMapOf() }
    }
}

/**
 * Allows to perform operations in scope of given [AttributedEntity] with its internal cache exposed using [AttributedEntity]'s
 * attribute-set.
 * When using invocations like :
 * `attributedCell.skipAttributes().let { cellContext -> cellContext.cacheOnAttributeSet("someKey", "someVal") }`
 * we can put value to, or query internal cache valid for attribute-set `attributeCell.attributes` from `cellContext` which
 * itself does not expose attributes to consumer.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@JvmSynthetic
internal fun <T : AttributedEntity> T.withAttributeSetBasedCache(block: (cache: MutableMap<String, Any>?) -> Unit) =
    setupCacheAndGet().apply(block)


/**
 * Given [ModelAttributeAccessor] (truncated, attribute-set-less [AttributedEntity] view), caches any value under specific key.
 * Key-value pair is stored in internal cache valid for / accessed by [AttributedEntity]'s attributes (attribute-set).
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@Suppress("UNCHECKED_CAST")
fun <T> T.cacheOnAttributeSet(key: String, value: () -> Any): Any
        where T : AttributedEntity,
              T : CustomAttributes = setupCacheAndGet()?.computeIfAbsent(key) { value() } ?: value()

/**
 * Given [ModelAttributeAccessor] (truncated, attribute-set-less [AttributedEntity] view), gets cached value stored under given key.
 * Key-value pair is stored in internal cache valid for / accessed by [AttributedEntity]'s attributes (attribute-set).
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@Suppress("UNCHECKED_CAST")
fun <T> T.getCachedOnAttributeSet(key: String): Any
        where T : AttributedEntity,
              T : CustomAttributes = setupCacheAndGet()?.get(key) ?: error("cannot resolve cached value in scope!")



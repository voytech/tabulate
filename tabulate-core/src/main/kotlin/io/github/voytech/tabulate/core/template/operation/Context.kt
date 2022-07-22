package io.github.voytech.tabulate.core.template.operation

import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeAware
import io.github.voytech.tabulate.core.model.AttributedModelOrPart
import io.github.voytech.tabulate.core.model.Attributes


/**
 * Basic interface providing custom attributes that are shared throughout entire exporting process.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
sealed interface Context {
    fun getContextAttributes(): MutableMap<String, Any>?
}

/**
 * Basic implementation of [Context] interface
 * @see Context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
sealed class ContextData : Context {
    var additionalAttributes: MutableMap<String, Any>? = null
    override fun getContextAttributes(): MutableMap<String, Any>? = additionalAttributes

    inline fun <reified C: Any> getContextAttribute(key: String): C? = additionalAttributes?.get(key) as C?

    inline fun <reified C: Any> removeContextAttribute(key: String): C? = additionalAttributes?.remove(key) as C?
}

/**
 * A base class for all operation context, where each includes additional model attributes for table appearance
 * customisation.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class AttributedContext(@JvmSynthetic override val attributes: Attributes? = null) :
    ContextData(),
    AttributeAware {

    val id: String by lazy { "_${javaClass}-${hashCode()}" }

    @Suppress("UNCHECKED_CAST")
    fun <T : Attribute<T>> getModelAttribute(clazz: Class<T>): T? =
        attributes?.get(clazz)

    inline fun <reified T : Attribute<T>> getModelAttribute(): T? =
        getModelAttribute(T::class.java)
}



class AttributesByContexts<T : AttributedModelOrPart<T>>(
    from: T, to: List<Class<out AttributedContext>>,
    val attributes: Map<Class<out AttributedContext>, Attributes> =
        to.associate { (it to (from.attributes?.forContext(it) ?: Attributes())) },
) {
    @Suppress("UNCHECKED_CAST")
    internal inline fun <reified E : AttributedContext> get(): Attributes = attributes[E::class.java] ?: Attributes()
}

fun <T : AttributedModelOrPart<T>> T.distributeAttributesForContexts(
    vararg clazz: Class<out AttributedContext>,
): AttributesByContexts<T> = AttributesByContexts(this, listOf(*clazz))

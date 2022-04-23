package io.github.voytech.tabulate.core.template.operation

import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeCategoryAware
import io.github.voytech.tabulate.core.model.Attributes


/**
 * Basic interface providing custom attributes that are shared throughout entire exporting process.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
sealed interface Context {
    fun getContextAttributes() : MutableMap<String, Any>?
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
}

fun Context.getId(): String {
    return (getContextAttributes()?.get("_id") ?: error("")) as String
}

/**
 * A base class for all operation context, where each includes additional model attributes for table appearance
 * customisation.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
open class AttributedModel<A : Attribute<*>>(@JvmSynthetic internal open val attributes: Attributes<A>? = null) : ContextData(),
    AttributeCategoryAware<A> {
    @Suppress("UNCHECKED_CAST")
    fun <T : A> getModelAttribute(clazz: Class<T>): T? =
        attributes?.get(clazz)

    override fun getAttributeCategoryClass() : Class<A> = attributes!!.getAttributeCategoryClass()
}

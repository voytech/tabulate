package io.github.voytech.tabulate.core.template.operation

import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeAware
import io.github.voytech.tabulate.core.model.AttributedModelOrPart
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.template.layout.Layout
import io.github.voytech.tabulate.core.template.layout.LayoutElement
import io.github.voytech.tabulate.core.template.layout.LayoutElementBoundingBox
import io.github.voytech.tabulate.core.template.layout.LayoutPolicy


/**
 * Basic interface providing custom attributes that are shared throughout entire exporting process.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
sealed interface Context {
    fun getCustomAttributes(): MutableMap<String, Any>?
}

/**
 * Basic implementation of [Context] interface
 * @see Context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
sealed class ContextData : Context {

    var additionalAttributes: MutableMap<String, Any> = mutableMapOf()

    override fun getCustomAttributes(): MutableMap<String, Any>? = additionalAttributes

    inline fun <reified C: Any> getCustomAttribute(key: String): C? = additionalAttributes[key] as C?

    inline fun <reified C: Any> removeCustomAttribute(key: String): C? = additionalAttributes.remove(key) as C?

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

    fun <T : Attribute<T>> getModelAttribute(clazz: Class<T>): T? =
        attributes?.get(clazz)

    inline fun <reified T : Attribute<T>> getModelAttribute(): T? =
        getModelAttribute(T::class.java)

    fun <A: Any> setContextAttribute(key: String, value: A) {
        additionalAttributes["$id[$key]"] = value
    }

    @Suppress("UNCHECKED_CAST")
    fun <A: Any> getContextAttribute(key: String): A? = if (additionalAttributes.containsKey("$id[$key]")) {
        additionalAttributes["$id[$key]"] as? A
    } else null

    @Suppress("UNCHECKED_CAST")
    fun <A: Any> removeContextAttribute(key: String): A? = if (additionalAttributes.containsKey("$id[$key]")) {
        additionalAttributes.remove("$id[$key]") as? A
    } else null

}

abstract class RenderableContext<EL: LayoutPolicy>(@JvmSynthetic override val attributes: Attributes? = null) : AttributedContext(), LayoutElement<EL> {
    lateinit var boundingBox: LayoutElementBoundingBox
        private set

    fun Layout.initBoundingBox(policy: EL,initializer: ((LayoutElementBoundingBox) -> LayoutElementBoundingBox)?): LayoutElementBoundingBox {
        boundingBox = computeBoundingBox(policy)
        initializer?.let { boundingBox += it(boundingBox) }
        return boundingBox
    }
}

fun AttributedContext.boundingBox(): LayoutElementBoundingBox? = if (this is RenderableContext<*>) this.boundingBox else null

class AttributesByContexts<T : AttributedModelOrPart<T>>(
    from: T, to: List<Class<out AttributedContext>>,
    val attributes: Map<Class<out AttributedContext>, Attributes> =
        to.associate { (it to (from.attributes?.forContext(it) ?: Attributes())) },
) {
    internal inline fun <reified E : AttributedContext> get(): Attributes = attributes[E::class.java] ?: Attributes()
}

fun <T : AttributedModelOrPart<T>> T.distributeAttributesForContexts(
    vararg clazz: Class<out AttributedContext>,
): AttributesByContexts<T> = AttributesByContexts(this, listOf(*clazz))


interface HasValue<V: Any> {
    val value: V
}

interface HasText: HasValue<String>

interface HasImage {
    val imageUri: String
}
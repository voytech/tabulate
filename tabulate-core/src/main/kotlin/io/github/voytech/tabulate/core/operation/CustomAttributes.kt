package io.github.voytech.tabulate.core.operation

import io.github.voytech.tabulate.core.RelatedLayouts
import io.github.voytech.tabulate.core.layout.*
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeAware
import io.github.voytech.tabulate.core.model.AttributedModelOrPart
import io.github.voytech.tabulate.core.model.Attributes


/**
 * Basic interface providing custom attributes that are shared throughout entire exporting process.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
sealed interface CustomAttributes {
    fun getCustomAttributes(): MutableMap<String, Any>
}

/**
 * Basic implementation of [CustomAttributes] interface
 * @see CustomAttributes
 * @author Wojciech Mąka
 * @since 0.1.0
 */
sealed class CustomAttributesData : CustomAttributes {

    var additionalAttributes: MutableMap<String, Any> = mutableMapOf()

    override fun getCustomAttributes(): MutableMap<String, Any> = additionalAttributes

    inline fun <reified C : Any> getCustomAttribute(key: String): C? = additionalAttributes[key] as C?

    inline fun <reified C : Any> setCustomAttribute(key: String, value: C) {
        additionalAttributes[key] = value
    }

    inline fun <reified C : Any> removeCustomAttribute(key: String): C? = additionalAttributes.remove(key) as C?

}

/**
 * A base class for all operation context, where each includes additional model attributes for table appearance
 * customisation.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class AttributedEntity(@JvmSynthetic override val attributes: Attributes? = null) :
    CustomAttributesData(),
    AttributeAware {

    val id: String by lazy { "_${javaClass}-${hashCode()}" }

    fun <T : Attribute<T>> getModelAttribute(clazz: Class<T>): T? =
        attributes?.get(clazz)

    inline fun <reified T : Attribute<T>> getModelAttribute(): T? =
        getModelAttribute(T::class.java)

    inline fun <reified T : Attribute<T>> hasModelAttribute(): Boolean =
        getModelAttribute(T::class.java) != null

    fun <A : Any> setContextAttribute(key: String, value: A) {
        additionalAttributes["$id[$key]"] = value
    }

    @Suppress("UNCHECKED_CAST")
    fun <A : Any> getContextAttribute(key: String): A? = if (additionalAttributes.containsKey("$id[$key]")) {
        additionalAttributes["$id[$key]"] as? A
    } else null

    @Suppress("UNCHECKED_CAST")
    fun <A : Any> removeContextAttribute(key: String): A? = if (additionalAttributes.containsKey("$id[$key]")) {
        additionalAttributes.remove("$id[$key]") as? A
    } else null

}

abstract class RenderableEntity<EL : Layout>(@JvmSynthetic override val attributes: Attributes? = null) :
    AttributedEntity(), LayoutElement<EL> {

    lateinit var boundingBox: RenderableBoundingBox
        private set

    fun hasBoundingBox() = this::boundingBox.isInitialized

    internal fun initBoundingBox(layouts: RelatedLayouts): RenderableBoundingBox = with(layouts.layout) {
        if (hasBoundingBox()) return boundingBox
        @Suppress("UNCHECKED_CAST")
        boundingBox = (layouts.layout as EL).defineBoundingBox()
            .convertUnits(layouts.layout, boundaryToFit, layouts.parent)
        return boundingBox
    }
}

fun AttributedEntity.boundingBox(): RenderableBoundingBox? =
    if (this is RenderableEntity<*> && hasBoundingBox()) {
        this.boundingBox
    } else null

fun <L : Layout> AttributedEntity.asLayoutElement(): ApplyLayoutElement<L>? =
    if (this is ApplyLayoutElement<*>) {
        @Suppress("UNCHECKED_CAST")
        this as ApplyLayoutElement<L>
    } else null

fun AttributedEntity.layoutBoundaryToFit(): BoundaryType =
    if (this is RenderableEntity<*>) {
        boundaryToFit
    } else BoundaryType.BORDER

class AttributesByContexts<T : AttributedModelOrPart>(
    from: T, to: List<Class<out AttributedEntity>>,
    val attributes: Map<Class<out AttributedEntity>, Attributes> =
        to.associate { (it to (from.attributes?.forContext(it) ?: Attributes())) },
) {
    internal inline fun <reified E : AttributedEntity> get(): Attributes = attributes[E::class.java] ?: Attributes()
}

fun <T : AttributedModelOrPart> T.distributeAttributesForContexts(
    vararg clazz: Class<out AttributedEntity>,
): AttributesByContexts<T> = AttributesByContexts(this, listOf(*clazz))


interface HasValue<V : Any> {
    val value: V
}

interface HasText : HasValue<String>

interface HasImage {
    val imageUri: String
}
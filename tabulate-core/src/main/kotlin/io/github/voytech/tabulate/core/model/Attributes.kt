package io.github.voytech.tabulate.core.model

import io.github.voytech.tabulate.core.template.loadAttributeConstraints
import io.github.voytech.tabulate.core.template.operation.AttributedContext
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

interface AttributeConstraint

data class ModelAttributeConstraint<A : Attribute<A>, MA : AttributedModelOrPart<MA>>(
    val model: Class<MA>,
    val attribute: Class<A>,
) : AttributeConstraint

data class ContextAttributeConstraint<A : Attribute<A>, CA : AttributedContext<CA>>(
    val context: Class<CA>,
    val attribute: Class<A>,
) : AttributeConstraint


data class AttributesConstraints(
    private val disabledOnModelsIn: List<ModelAttributeConstraint<*, *>>,
    private val enabledForContextsIn: List<ContextAttributeConstraint<*, *>>,
    val disabledOnModels: Map<Class<out AttributedModelOrPart<*>>, List<ModelAttributeConstraint<*, *>>> = disabledOnModelsIn.groupBy { it.model },
    val enabledForContext: Map<Class<out AttributedContext<*>>, List<ContextAttributeConstraint<*, *>>> = enabledForContextsIn.groupBy { it.context },
)

class AttributeConstraintsBuilder {
    private val modelConstraints: MutableList<ModelAttributeConstraint<*, *>> = mutableListOf()
    private val contextConstraints: MutableList<ContextAttributeConstraint<*, *>> = mutableListOf()

    fun disableOnModel(
        model: Class<out AttributedModelOrPart<*>>,
        attribute: Class<out Attribute<*>>,
    ) {
        modelConstraints.add(ModelAttributeConstraint(model, attribute))
    }

    inline fun <reified A: Attribute<A>,reified T: AttributedModelOrPart<T>> disable() = disableOnModel(T::class.java,A::class.java)

    fun enableOnContext(
        context: Class<out AttributedContext<*>>,
        attribute: Class<out Attribute<*>>,
    ) {
        contextConstraints.add(ContextAttributeConstraint(context, attribute))
    }

    inline fun <reified A: Attribute<A>,reified T: AttributedContext<T>> enable() = enableOnContext(T::class.java,A::class.java)

    @JvmSynthetic
    internal fun build(): AttributesConstraints = AttributesConstraints(modelConstraints, contextConstraints)
}

abstract class Attribute<T : Attribute<T>> {
    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var nonDefaultProps: Set<String> = emptySet()

    open fun overrideWith(other: T): T = other

    operator fun plus(other: T): T = overrideWith(other)

    protected fun isModified(property: KProperty<*>): Boolean {
        return nonDefaultProps.contains(property.name)
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <P> takeIfChanged(other: T, property: KProperty1<T, P>): P =
        if (other.isModified(property)) property.invoke(other) else property.invoke(this as T)

}

typealias AttributeMap = MutableMap<Class<Attribute<*>>, Attribute<*>>

class Attributes<A : AttributeAware<A>>(
    private val ownerClass: Class<A>,
    internal val attributeSet: Set<Attribute<*>> = emptySet(),
) {
    private val constraints: AttributesConstraints by lazy {
        loadAttributeConstraints()
    }

    private var attributeMap: AttributeMap = LinkedHashMap()

    private val attributeSetHashCode by lazy { attributeSet.hashCode() }

    val size: Int by attributeSet::size

    private constructor(ownerClass: Class<A>, attributeSet: Set<Attribute<*>>, map: AttributeMap) : this(
        ownerClass,
        attributeSet
    ) {
        this.attributeMap = map
    }

    init {
        if (attributeMap.isEmpty()) {
            attributeSet.mergeAttributes().forEach {
                attributeMap[it.javaClass] = it
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <I : Attribute<I>> Attribute<*>.overrideAttribute(other: Attribute<*>): Attribute<*> =
        (this as I) + (other as I)

    operator fun plus(other: Attributes<A>): Attributes<A> {
        val result = HashMap<Class<Attribute<*>>, Attribute<*>>()
        val set = mutableSetOf<Attribute<*>>()
        attributeMap.forEach { (clazz, attribute) ->
            result[clazz] = attribute
            set.add(attribute)
        }
        other.attributeMap.forEach { (clazz, attribute) ->
            if (result.containsKey(clazz)) {
                set.remove(result[clazz])
                result[clazz] = result[clazz]!!.overrideAttribute(attribute)
            } else {
                result[clazz] = attribute
            }
            set.add(result[clazz]!!)
        }
        return Attributes(ownerClass, set, result)
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <E : Attribute<E>> get(clazz: Class<E>): E? = attributeMap[clazz as Class<Attribute<*>>] as E?

    fun <O : AttributedModelOrPart<O>> cast(clazz: Class<O>): Attributes<O> {
        return constraints.disabledOnModels[clazz]?.map { it.attribute }?.let { incompatibleClasses ->
            Attributes(clazz, attributeSet.filter { it.javaClass !in incompatibleClasses }.toSet())
        } ?: Attributes(clazz, attributeSet)
    }

    fun <O : AttributedContext<O>> forContext(clazz: Class<O>): Attributes<O> {
        return constraints.enabledForContext[clazz]?.map { it.attribute }?.let { compatibleClasses ->
            Attributes(clazz, attributeSet.filter { it.javaClass in compatibleClasses }.toSet())
        } ?: Attributes(clazz)
    }

    inline fun <reified O : AttributedModelOrPart<O>> cast(): Attributes<O> = cast(O::class.java)

    inline fun <reified O : AttributedContext<O>> forContext(): Attributes<O> = forContext(O::class.java)

    fun isNotEmpty(): Boolean = size > 0

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        return if (other is Attributes<*>) {
            other.attributeSet == attributeSet
        } else false
    }

    override fun hashCode(): Int = attributeSetHashCode

    companion object {
        inline operator fun <reified A : AttributeAware<A>> invoke() = Attributes(A::class.java)
    }
}


inline fun <reified A : AttributeAware<A>> Attributes<A>?.orEmpty() = this ?: Attributes(A::class.java, emptySet())

fun <A : AttributeAware<A>> Attributes<A>?.isNullOrEmpty(): Boolean = this?.size == 0

/**
 * Takes a list of same class attributes and merges them into single attribute.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
internal fun <A : Attribute<A>> List<Attribute<*>>.mergeAttributes(): A {
    return (this as List<A>).takeLast(this.size - 1)
        .fold(this.first()) { acc: A, attribute: A ->
            acc.overrideWith(attribute)
        }
}

/**
 * Takes set of attributes and merges all attributes with same class together.
 * Resulting set contains one attribute for given attribute class.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
internal fun <C : Attribute<C>> Set<Attribute<*>>.mergeAttributes(): Set<C> =
    groupBy { it.javaClass }
        .map { it.value.mergeAttributes() }
        .toSet() as Set<C>



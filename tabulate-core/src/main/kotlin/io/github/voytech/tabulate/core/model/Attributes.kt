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

data class AttributesConstraints(
    private val disabledOnModelsIn: List<ModelAttributeConstraint<*, *>>,
    val disabledOnModels: Map<Class<out AttributedModelOrPart<*>>, List<ModelAttributeConstraint<*, *>>> = disabledOnModelsIn.groupBy { it.model },
)

class AttributeConstraintsBuilder {
    private val modelConstraints: MutableList<ModelAttributeConstraint<*, *>> = mutableListOf()

    fun disableOnModel(
        model: Class<out AttributedModelOrPart<*>>,
        attribute: Class<out Attribute<*>>,
    ) {
        modelConstraints.add(ModelAttributeConstraint(model, attribute))
    }

    inline fun <reified A: Attribute<A>,reified T: AttributedModelOrPart<T>> disable() = disableOnModel(T::class.java,A::class.java)

    @JvmSynthetic
    internal fun build(): AttributesConstraints = AttributesConstraints(modelConstraints)
}

abstract class Attribute<T : Attribute<T>> {
    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var nonDefaultProps: Set<String> = emptySet()
    internal lateinit var ownerClass: Class<out AttributeAware>

    protected open fun overrideWith(other: T): T = other

    operator fun plus(other: T): T = overrideWith(other).apply { ownerClass = other.ownerClass }

    protected fun isModified(property: KProperty<*>): Boolean {
        return nonDefaultProps.contains(property.name)
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <P> takeIfChanged(other: T, property: KProperty1<T, P>): P =
        if (other.isModified(property)) property.invoke(other) else property.invoke(this as T)

}

typealias AttributeMap = MutableMap<Class<Attribute<*>>, Attribute<*>>

@Suppress("UNCHECKED_CAST")
private fun <I : Attribute<I>> Attribute<*>.uncheckedMerge(other: Attribute<*>): Attribute<*> {
    assert(javaClass == other.javaClass) { "only attributes of same class can be merged" }
    return (this as I) + (other as I)
}

class Attributes(
    internal val attributeSet: Set<Attribute<*>> = emptySet(),
) {
    private val constraints: AttributesConstraints by lazy {
        loadAttributeConstraints()
    }

    private var attributeMap: AttributeMap = LinkedHashMap()

    private val attributeSetHashCode by lazy { attributeSet.hashCode() }

    val size: Int by attributeSet::size

    private constructor(attributeSet: Set<Attribute<*>>, map: AttributeMap) : this(attributeSet) {
        this.attributeMap = map
    }

    init {
        if (attributeMap.isEmpty()) {
            attributeSet.mergeAttributes().forEach {
                attributeMap[it.javaClass] = it
            }
        }
    }

    operator fun plus(other: Attributes): Attributes {
        val result = HashMap<Class<Attribute<*>>, Attribute<*>>()
        val set = mutableSetOf<Attribute<*>>()
        attributeMap.forEach { (clazz, attribute) ->
            result[clazz] = attribute
            set.add(attribute)
        }
        other.attributeMap.forEach { (clazz, attribute) ->
            if (result.containsKey(clazz)) {
                set.remove(result[clazz])
                result[clazz] = result[clazz]!!.uncheckedMerge(attribute)
            } else {
                result[clazz] = attribute
            }
            set.add(result[clazz]!!)
        }
        return Attributes(set, result)
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <E : Attribute<E>> get(clazz: Class<E>): E? = attributeMap[clazz as Class<Attribute<*>>] as E?

    fun <O : AttributedModelOrPart<O>> cast(clazz: Class<O>): Attributes {
        return constraints.disabledOnModels[clazz]?.map { it.attribute }?.let { incompatibleClasses ->
            Attributes(attributeSet.filter { it.javaClass !in incompatibleClasses }.toSet())
        } ?: Attributes(attributeSet)
    }

    fun <O : AttributedContext> forContext(clazz: Class<O>): Attributes =
        Attributes(attributeSet.filter { it.ownerClass.isAssignableFrom(clazz) }.toSet())

    inline fun <reified O : AttributedModelOrPart<O>> cast(): Attributes = cast(O::class.java)

    inline fun <reified O : AttributedContext> forContext(): Attributes = forContext(O::class.java)

    fun isNotEmpty(): Boolean = size > 0

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        return if (other is Attributes) {
            other.attributeSet == attributeSet
        } else false
    }

    override fun hashCode(): Int = attributeSetHashCode

    companion object {
        inline operator fun invoke() = Attributes()
    }
}


inline fun Attributes?.orEmpty() = this ?: Attributes(emptySet())

fun Attributes?.isNullOrEmpty(): Boolean = this?.size == 0

/**
 * Takes a list of same class attributes and merges them into single attribute.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
internal fun List<Attribute<*>>.mergeAttributes(): Attribute<*> {
    return takeLast(this.size - 1)
        .fold(this.first()) { acc , attribute ->
            acc.uncheckedMerge(attribute)
        }
}

/**
 * Takes set of attributes and merges all attributes with same class together.
 * Resulting set contains one attribute for given attribute class.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
internal fun Set<Attribute<*>>.mergeAttributes(): Set<Attribute<*>> =
    groupBy { it.javaClass }
        .map { it.value.mergeAttributes() }
        .toSet()
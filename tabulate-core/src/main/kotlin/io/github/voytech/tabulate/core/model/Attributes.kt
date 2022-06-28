package io.github.voytech.tabulate.core.model

import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

data class AttributeClassifier<CAT : Attribute<*>, ARM : Model<ARM>>(
    val attributeCategory: Class<CAT>,
    val model: Class<ARM>
) {
    companion object {
        inline fun <reified CAT : Attribute<*>, reified ARM : Model<ARM>> classify() =
            AttributeClassifier(CAT::class.java, ARM::class.java)
    }
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

    abstract fun getClassifier(): AttributeClassifier<*, *>

}

inline fun <reified CAT : Attribute<*>, ARM : Model<ARM>> Class<CAT>.classifyUnder(rootModel: Class<ARM>) =
    AttributeClassifier(CAT::class.java, rootModel)

inline fun <CAT : Attribute<*>, reified ARM : Model<ARM>> Class<ARM>.classifyWith(category: Class<CAT>) =
    AttributeClassifier(category, ARM::class.java)

fun interface AttributeCategoryAware<A : Attribute<*>> {
    fun getAttributeCategoryClass(): Class<A>
}

class Attributes<A : Attribute<*>>(
    internal val attributeSet: Set<A> = emptySet(), private val attributeCategory: Class<A>
) : AttributeCategoryAware<A> {
    private var attributeMap: MutableMap<Class<A>, A> = LinkedHashMap()

    private val attributeSetHashCode by lazy { attributeSet.hashCode() }

    val size: Int by attributeSet::size

    private constructor(attributeSet: Set<A>, map: HashMap<Class<A>, A>, attributeCategory: Class<A>) : this(
        attributeSet,
        attributeCategory
    ) {
        this.attributeMap = map
    }

    init {
        if (attributeMap.isEmpty()) {
            attributeSet.mergeAttributes().forEach {
                attributeMap[(it as A).javaClass] = it
            }
        }
    }

    override fun getAttributeCategoryClass(): Class<A> = attributeCategory

    @Suppress("UNCHECKED_CAST")
    private fun <I: Attribute<I>> Attribute<*>.overrideAttribute(other: Attribute<*>): Attribute<*> = (this as I) + (other as I)

    operator fun plus(other: Attributes<A>): Attributes<A> {
        val result = HashMap<Class<A>, A>()
        val set = mutableSetOf<A>()
        attributeMap.forEach { (clazz, attribute) ->
            result[clazz] = attribute
            set.add(attribute)
        }
        other.attributeMap.forEach { (clazz, attribute) ->
            if (result.containsKey(clazz)) {
                set.remove(result[clazz])
                result[clazz] = result[clazz]!!.overrideAttribute(attribute) as A
            } else {
                result[clazz] = attribute
            }
            set.add(result[clazz]!!)
        }
        return Attributes(set, result, attributeCategory)
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <E : A> get(clazz: Class<E>): E? = (attributeMap as Map<Class<out A>, A>) [clazz] as E?

    fun isNotEmpty(): Boolean = size > 0

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        return if (other is Attributes<*>) {
            other.attributeSet == attributeSet
        } else false
    }

    override fun hashCode(): Int = attributeSetHashCode
}

inline fun <reified A: Attribute<*>> Attributes<A>?.orEmpty() = this ?: Attributes(emptySet(), A::class.java)

fun <A: Attribute<*>> Attributes<A>?.isNullOrEmpty(): Boolean = this?.size == 0

data class CategorizedAttributes(
    private val categories: Set<Attributes<*>>,
    internal val byCategory: Map<Class<out Attribute<*>>, Attributes<*>> = categories.associateBy { it.getAttributeCategoryClass() }
)

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



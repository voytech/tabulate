package io.github.voytech.tabulate.model.attributes

import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

abstract class Attribute<T: Attribute<T>> {
    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var nonDefaultProps: Set<String> = emptySet()

    open fun overrideWith(other: T): T = other

    operator fun plus(other: T): T = overrideWith(other)

    protected fun isModified(property: KProperty<*>): Boolean {
       return nonDefaultProps.contains(property.name)
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <P> takeIfChanged(other: T, property: KProperty1<T, P>) :P =
        if (other.isModified(property)) property.invoke(other) else property.invoke(this as T)

}

class Attributes<A: Attribute<*>>(internal val attributeSet: Set<A> = emptySet()) {
    private val attributeMap: MutableMap<Class<out A>, A> = LinkedHashMap()

    val size : Int by attributeSet::size

    private constructor(attributeSet: Set<A>, map: HashMap<Class<out A>,A>) : this(attributeSet) {
        this.attributeMap.putAll(map)
    }

    init {
      if (attributeMap.isEmpty()) {
          attributeSet.mergeAttributes().forEach {
              attributeMap[it.javaClass] = it
          }
      }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <I: Attribute<I>> A.overrideAttribute(other: A, clazz: Class<I>): I = (this as I) + (other as I)


    operator fun plus(other: Attributes<A>): Attributes<A> {
          val result = HashMap<Class<out A>,A>()
          val set = mutableSetOf<A>()
          attributeMap.forEach { (clazz, attribute) ->
              result[clazz] = attribute
              set.add(attribute)
          }
          other.attributeMap.forEach { (clazz, attribute) ->
              if (result.containsKey(clazz)) {
                  set.remove(result[clazz])
                  result[clazz] = result[clazz]!!.overrideAttribute(attribute, clazz)
              } else {
                  result[clazz] = attribute
              }
              set.add(result[clazz]!!)
          }
          return Attributes(set, result)
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <E: A> get(clazz: Class<E>): E? = attributeMap[clazz] as E?

    fun isNotEmpty(): Boolean = size > 0

    override fun equals(other: Any?): Boolean = attributeSet == other

    override fun hashCode(): Int = attributeSet.hashCode()
}

fun <A: Attribute<*>> Attributes<A>?.orEmpty() = this ?: Attributes<A>(emptySet())

fun <A: Attribute<*>> Attributes<A>?.isNullOrEmpty(): Boolean = this?.size == 0

abstract class CellAttribute<T : CellAttribute<T>> : Attribute<T>()

abstract class ColumnAttribute<T : ColumnAttribute<T>> : Attribute<T>()

abstract class RowAttribute<T : RowAttribute<T>>  : Attribute<T>()

abstract class TableAttribute<T : TableAttribute<T>>  : Attribute<T>()

/**
 * Takes a list of same class attributes and merges them into single attribute.
 * @author Wojciech Mąka
 */
fun <A : Attribute<A>> List<A>.mergeAttributes(): A {
    return this.takeLast(this.size - 1)
        .fold(this.first()) { acc: A, attribute: A ->
            acc.overrideWith(attribute)
        }
}

/**
 * Takes set of attributes and merges all attributes with same class together.
 * Resulting set contains one attribute for given attribute class.
 * @author Wojciech Mąka
 */
fun <C: Attribute<*>> Set<C>.mergeAttributes(): Set<C> {
    return groupBy { it.javaClass }
        .map { it.value.mergeAttributes() }
        .toSet()
}
package io.github.voytech.tabulate.model.attributes

import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

abstract class Attribute<T: Attribute<T>> {
    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var nonDefaultProps: Set<String> = emptySet()

    open fun overrideWith(other: T): T = other

    protected fun isModified(property: KProperty<*>): Boolean {
       return nonDefaultProps.contains(property.name)
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <P> takeIfChanged(other: T, property: KProperty1<T, P>) :P =
        if (other.isModified(property)) property.invoke(other) else property.invoke(this as T)

}

abstract class CellAttribute<T : CellAttribute<T>> : Attribute<T>()

abstract class ColumnAttribute<T : ColumnAttribute<T>> : Attribute<T>()

abstract class RowAttribute<T : RowAttribute<T>>  : Attribute<T>()

abstract class TableAttribute<T : TableAttribute<T>>  : Attribute<T>()

fun <A : Attribute<A>> List<A>.mergeAttributes(): A {
    return this.takeLast(this.size - 1)
        .fold(this.first()) { acc: A, attribute: A ->
            acc.overrideWith(attribute)
        }
}

fun <C: Attribute<*>> overrideAttributesLeftToRight(attributeSet: Set<C>): Set<C> {
    return attributeSet.groupBy { it.javaClass }
        .map { it.value.mergeAttributes() }
        .toSet()
}
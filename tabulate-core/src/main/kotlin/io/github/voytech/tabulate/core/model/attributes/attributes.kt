package io.github.voytech.tabulate.core.model.attributes

import java.util.*

abstract class Attribute<T: Attribute<T>> {
    open fun mergeWith(other: T): T = other

    //TODO Try find better solution. Overcoming type system limitations in terms of generics and all issues with cyclic self references.
    @Suppress("UNCHECKED_CAST")
    fun uncheckedMergeWith(other: Attribute<*>): T = mergeWith(other as T)
}

abstract class CellAttribute<T : CellAttribute<T>> : Attribute<T>()

abstract class ColumnAttribute<T : ColumnAttribute<T>> : Attribute<T>() {
    open fun beforeFirstRow(): Boolean = true
    open fun afterLastRow(): Boolean = false
}

abstract class RowAttribute<T : RowAttribute<T>>  : Attribute<T>()

abstract class TableAttribute<T : TableAttribute<T>>  : Attribute<T>()

fun <A : Attribute<A>> List<A>.mergeAttributes(): A {
    return this.takeLast(this.size - 1)
        .fold(this.first(),{ acc: A, attribute: A ->
            acc.mergeWith(attribute)
        })
}

private fun List<CellAttribute<*>>.mergeUncheckedAttributes(): CellAttribute<*> {
    val requiredClass = this.first().javaClass
    return this.takeLast(this.size - 1)
        .fold(this.first(),{ acc: CellAttribute<*>, attribute: CellAttribute<*> ->
            assert(requiredClass == attribute.javaClass)
            acc.uncheckedMergeWith(attribute)
        })
}

fun overrideAttributesRightToLeft(attributeSet: LinkedHashSet<CellAttribute<*>>): Set<CellAttribute<*>> {
    return attributeSet.groupBy { it.javaClass }
        .map { it.value.mergeUncheckedAttributes() }
        .toSet()
}

fun overrideAttributesRightToLeft(vararg attributeSets: Set<CellAttribute<*>>?): Set<CellAttribute<*>> {
    val linkedSet = linkedSetOf<CellAttribute<*>>()
    attributeSets.forEach {
        it?.forEach { cellAttribute -> linkedSet.add(cellAttribute)}
    }
    return overrideAttributesRightToLeft(linkedSet)
}
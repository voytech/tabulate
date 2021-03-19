package pl.voytech.exporter.core.model.attributes

import java.util.*

open class Attribute

abstract class CellAttribute<T : CellAttribute<T>> : Attribute() {

    abstract fun mergeWith(other: T): T

    //TODO Try find better solution. Overcoming type system limitations in terms of generics and all issues with cyclic self references.
    @Suppress("UNCHECKED_CAST")
    fun uncheckedMergeWith(other: CellAttribute<*>): T = mergeWith(other as T)
}

open class ColumnAttribute : Attribute() {
    open fun beforeFirstRow(): Boolean = true
    open fun afterLastRow(): Boolean = false
}

open class RowAttribute : Attribute()

open class TableAttribute : Attribute()

fun <A : CellAttribute<A>> List<A>.mergeAttributes(): A {
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

fun mergeLatterWins(attributeSet: LinkedHashSet<CellAttribute<*>>): Set<CellAttribute<*>> {
    return attributeSet.groupBy { it.javaClass }
        .map { it.value.mergeUncheckedAttributes() }
        .toSet()
}

fun mergeLatterWins(vararg attributeSets: Set<CellAttribute<*>>?): Set<CellAttribute<*>> {
    val linkedSet = linkedSetOf<CellAttribute<*>>()
    attributeSets.forEach {
        it?.forEach { cellAttribute -> linkedSet.add(cellAttribute)}
    }
    return mergeLatterWins(linkedSet)
}
package pl.voytech.exporter.core.model.attributes

open class Attribute

abstract class CellAttribute<T : CellAttribute<T>> : Attribute() {
    abstract fun mergeWith(other: T): T
}

open class ColumnAttribute : Attribute() {
    open fun beforeFirstRow(): Boolean = true
    open fun afterLastRow(): Boolean = false
}

open class RowAttribute : Attribute()

open class TableAttribute : Attribute()

fun mergeAttributes(vararg attributesByLevels: Set<CellAttribute<*>>?): Set<CellAttribute<*>> {
    return attributesByLevels.filterNotNull()
        .map { set -> set.groupBy { it.javaClass }.map { Pair(it.key, it.value.first()) }.toMap() }
        .fold(
            mapOf<Class<CellAttribute<*>>, CellAttribute<*>>(),
            { accumulated, currentLevel -> mergeAttributes(accumulated, currentLevel) })
        .values
        .toSet()
}

private fun <T : CellAttribute<T>> mergeAttributes(
    first: Map<Class<T>, T>,
    second: Map<Class<T>, T>
): Map<Class<T>, T> {
    val result = mutableMapOf<Class<T>, T>()
    first.keys.toSet().intersect(second.keys.toSet()).forEach {
        result[it] = (first[it] ?: error("")).mergeWith((second[it] ?: error("")))
    }
    first.keys.toSet().subtract(second.keys.toSet()).forEach { result[it] = first[it] ?: error("") }
    second.keys.toSet().subtract(first.keys.toSet()).forEach { result[it] = second[it] ?: error("") }
    return result.toMap()
}
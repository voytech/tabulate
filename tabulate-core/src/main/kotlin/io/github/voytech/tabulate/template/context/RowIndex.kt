package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.RowIndexDef


enum class DefaultSteps {
    TRAILING_ROWS,
}

data class IndexMarker(
    val step: String,
    val index: Int,
) {
    operator fun plus(increment: Int): IndexMarker = IndexMarker(step, index + increment)

    operator fun minus(increment: Int): IndexMarker = IndexMarker(step, index - increment)

    operator fun inc(): IndexMarker = IndexMarker(step, index + 1)
}

data class RowIndex(
    val value: Int = 0,
    val steps: Map<String, IndexMarker> = emptyMap(),
) : Comparable<RowIndex> {

    fun hasLabel(label: String) = steps.containsKey(label)

    fun getIndex(label:String? = null): Int = label?.let { steps[it]?.index } ?: value

    fun getIndexOrNull(label: String): Int? = steps[label]?.index

    override fun compareTo(other: RowIndex): Int = value.compareTo(other.value)

    internal operator fun plus(increment: Int): RowIndex = RowIndex(value + increment, steps + increment)
    internal operator fun minus(increment: Int): RowIndex = RowIndex(value - increment, steps - increment)

    internal operator fun plus(increment: RowIndexDef): RowIndex {
        return increment.step?.let {
            RowIndex(
                value = value + increment.index,
                steps = steps + mapOf(
                    it.name to IndexMarker(index = increment.index, step = it.name)
                )
            )
        } ?: RowIndex(increment.index)
    }

    internal operator fun inc(): RowIndex = RowIndex(value = value + 1, steps + 1)

}

internal operator fun Map<String, IndexMarker>.inc(): Map<String, IndexMarker> = mapValues { it.value + 1 }

internal operator fun Map<String, IndexMarker>.plus(increment: Int): Map<String, IndexMarker> =
    mapValues { it.value + increment }

internal operator fun Map<String, IndexMarker>.minus(increment: Int): Map<String, IndexMarker> =
    mapValues { it.value - increment }

internal class MutableRowIndex {
    var rowIndex: Int = 0
    private val steps: MutableMap<String, IndexMarker> = mutableMapOf()

    fun mark(step: String): RowIndex {
        if (!steps.containsKey(step)) {
            steps[step] = IndexMarker(step, 0)
        }
        return RowIndex(rowIndex, steps)
    }

    fun inc() {
        rowIndex++
        steps.forEach { steps[it.key] = it.value + 1 }
    }

    fun assign(value: Int) {
        val offset = value - rowIndex
        rowIndex = value
        steps.forEach { steps[it.key] = it.value + offset }
    }

    private fun cloneSteps(): Map<String, IndexMarker> =
        steps.mapValues { it.value.copy() }

    fun getRowIndex(): RowIndex = RowIndex(rowIndex, cloneSteps())
}
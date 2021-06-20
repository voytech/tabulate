package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.RowIndexDef

enum class IndexLabel {
    TRAILING_ROWS,
}

data class IndexMarker(
    val label: String,
    val index: Int,
) {
    operator fun plus(increment: Int): IndexMarker = IndexMarker(label, index + increment)

    operator fun inc(): IndexMarker = IndexMarker(label, index + 1)
}

data class RowIndex(
    val rowIndex: Int = 0,
    val labels: Map<String, IndexMarker> = emptyMap(),
) : Comparable<RowIndex> {

    fun hasLabel(label: String) = labels.containsKey(label)

    fun getIndex(label: String? = null): Int = label?.let { labels[it]?.index } ?: rowIndex

    override fun compareTo(other: RowIndex): Int = rowIndex.compareTo(other.rowIndex)

    operator fun plus(increment: Int): RowIndex = RowIndex(rowIndex + increment, labels + increment)

    operator fun plus(increment: RowIndexDef): RowIndex {
        return increment.offsetLabel?.let {
            RowIndex(
                rowIndex = rowIndex + increment.index,
                labels = labels + mapOf(
                    it to IndexMarker(index = increment.index, label = it)
                )
            )
        } ?: RowIndex(increment.index)
    }

    operator fun inc(): RowIndex = RowIndex(rowIndex = rowIndex + 1, labels + 1)


}

operator fun Map<String, IndexMarker>.inc(): Map<String, IndexMarker> = mapValues { it.value + 1 }

operator fun Map<String, IndexMarker>.plus(increment: Int): Map<String, IndexMarker> =
    mapValues { it.value + increment }

class MutableRowIndex {
    var rowIndex: Int = 0
    private val labels: MutableMap<String, IndexMarker> = mutableMapOf()

    fun mark(label: String): RowIndex {
        if (!labels.containsKey(label)) {
            labels[label] = IndexMarker(label, 0)
        }
        return RowIndex(rowIndex, labels)
    }

    fun inc() {
        rowIndex++
        labels.forEach { labels[it.key] = it.value + 1 }
    }

    fun assign(value: Int) {
        val offset = value - rowIndex
        rowIndex = value
        labels.forEach { labels[it.key] = it.value + offset }
    }

    private fun cloneLabels(): Map<String, IndexMarker> =
        labels.mapValues { it.value.copy() }

    fun getRowIndex(): RowIndex = RowIndex(rowIndex, cloneLabels())
}
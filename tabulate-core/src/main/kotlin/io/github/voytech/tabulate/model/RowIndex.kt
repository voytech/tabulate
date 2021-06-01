package io.github.voytech.tabulate.model

enum class IndexLabel {
    DATASET_PROCESSED,
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

operator fun IndexMarker.inc(): IndexMarker = IndexMarker(label, index + 1)

operator fun IndexMarker.plus(increment: Int): IndexMarker = IndexMarker(label, index + increment)

class MutableRowIndex {
    var rowIndex: Int = 0
    private val labels: MutableMap<String, IndexMarker> = mutableMapOf()

    fun mark(label: String): RowIndex {
        if (!labels.containsKey(label)) {
            labels[label] = IndexMarker(label, 0)
        }
        return RowIndex(rowIndex, labels)
    }

    fun hasLabel(label: String) = labels.containsKey(label)

    fun getIndex(label: String? = null): Int = label?.let { labels[it]?.index } ?: rowIndex

    fun inc() {
        rowIndex++
        labels.forEach { labels[it.key] = it.value + 1 }
    }

    fun getRowIndex(): RowIndex = RowIndex(rowIndex, labels.mapValues { IndexMarker(it.value.label,it.value.index) })
}
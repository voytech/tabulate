package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.RowIndexDef


enum class AdditionalSteps {
    TRAILING_ROWS,
}

data class Step(
    val step: String,
    val index: Int,
    val start: Int,
) {
    operator fun plus(increment: Int): Step = Step(step, index + increment, start)

    operator fun minus(increment: Int): Step = Step(step, index - increment, start)

    operator fun inc(): Step = Step(step, index + 1, start)
}

data class RowIndex(
    val value: Int = 0,
    val step: Step? = null
) : Comparable<RowIndex> {

    fun getIndex(label: String? = null): Int = step?.takeIf { it.step == label }?.index ?: value

    fun getIndexOrNull(label: String): Int? = step?.takeIf { it.step == label }?.index

    fun <T : Enum<T>> asRowIndexDef(enumClass: Class<T>): RowIndexDef = RowIndexDef(
        index = step?.index ?: value,
        step = step?.let { step -> enumClass.enumConstants.find { step.step == it.name } }
    )

    override fun compareTo(other: RowIndex): Int = value.compareTo(other.value)

    internal operator fun plus(increment: Int): RowIndex = RowIndex(value + increment, step?.let { it + increment })

    internal operator fun minus(increment: Int): RowIndex = RowIndex(value - increment, step?.let { it - increment })

    private fun Step?.findStepStart(increment: RowIndexDef): Int =
        this?.step.let { if (increment.step?.name != this?.step) value else this?.start } ?: value

    internal operator fun plus(increment: RowIndexDef): RowIndex {
        return increment.step?.let {
            step.findStepStart(increment).let { stepStart ->
                RowIndex(
                    value = stepStart + increment.index,
                    step = Step(increment.step.name, increment.index, stepStart)
                )
            }
        } ?: RowIndex(increment.index)
    }

    internal operator fun inc(): RowIndex = RowIndex(value = value + 1, step?.let { it + 1 })

}

internal class MutableRowIndex {
    var rowIndex: Int = 0
    var step: Step? = null

    fun inc() {
        rowIndex++
        step = step?.let { it.copy(index = it.index + 1) }
    }

    fun set(index: RowIndex) {
        rowIndex = index.value
        step = index.step?.copy()
    }

    fun set(step: Step) {
        rowIndex += step.index
        this.step = step.copy()
    }

    fun getRowIndex(): RowIndex = RowIndex(rowIndex, step?.copy())
}
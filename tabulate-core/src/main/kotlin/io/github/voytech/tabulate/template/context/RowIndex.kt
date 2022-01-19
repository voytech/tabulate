package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.RowIndexDef

/**
 * AdditionalSteps enum represents default additional steps to be executed after regular collection elements are exported.
 * Currently there is only single step supported - a TRAILING_ROWS step. This step is used when there is a need to
 * specify row index relatively to the end of exported collection (e.g: footer row)
 * @since 0.1.0
 * @author Wojciech Mąka
 */
enum class AdditionalSteps {
    TRAILING_ROWS,
}

/**
 * Relative index marker. Allows to define index as offset value relative to particular step.
 * @since 0.1.0
 * @author Wojciech Mąka
 */
data class Step(
    val step: String,
    val index: Int,
    val start: Int,
) {
    operator fun plus(increment: Int): Step = Step(step, index + increment, start)

    operator fun minus(increment: Int): Step = Step(step, index - increment, start)

    operator fun inc(): Step = Step(step, index + 1, start)
}

/**
 * Complex index. Allows to define index as absolute value, or relative to particular step.
 * @since 0.1.0
 * @author Wojciech Mąka
 */
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

/**
 * Mutable complex index. Its value is incremented by [RowContextIterator]
 * @since 0.1.0
 * @author Wojciech Mąka
 */
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
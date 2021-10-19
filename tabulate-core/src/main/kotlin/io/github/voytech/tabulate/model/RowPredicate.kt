package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.model.RowIndexDef.Companion.maxValue
import io.github.voytech.tabulate.model.RowIndexDef.Companion.minValue
import io.github.voytech.tabulate.template.context.RowIndex
import java.util.function.Predicate
import kotlin.ranges.IntProgression.Companion.fromClosedRange

fun interface RowPredicate<T> : Predicate<SourceRow<T>>

fun interface IndexPredicate : Predicate<RowIndex>

enum class Operator {
    EQ,
    GT,
    LT,
    GTE,
    LTE
}

enum class LogicalOperator {
    AND,
    OR
}

fun ClosedRange<RowIndexDef>.progression(): IntProgression = fromClosedRange(start.index, endInclusive.index, 1)

interface PredicateLiteral : IndexPredicate {
    fun computeRanges(): Array<ClosedRange<RowIndexDef>>
}

data class RowIndexPredicateLiteral<T>(
    private val indexPredicate: PredicateLiteral,
) : Predicate<SourceRow<T>> {

    private val computedRanges: Array<ClosedRange<RowIndexDef>> by lazy {
         indexPredicate.computeRanges()
    }

    private val computedIndices: Set<RowIndexDef> by lazy {
        computedRanges.fold(mutableSetOf()) { agg, next ->
            val label = next.start.offsetLabel
            next.progression().forEach { agg.add(RowIndexDef(it, label)) }.let { agg }
        }
    }

    override fun test(sourceRow: SourceRow<T>): Boolean = indexPredicate.test(sourceRow.rowIndex)
    fun computeRanges(): Array<ClosedRange<RowIndexDef>> = computedRanges
    fun lastIndex(): RowIndexDef = computeRanges().last().endInclusive
    fun firsIndex(): RowIndexDef = computeRanges().first().start
    fun materialize(): Set<RowIndexDef> = computedIndices
}

fun RowIndex.lookup(other: RowIndexDef): RowIndexDef? =
    if (other.offsetLabel != null) {
        getIndexOrNull(other.offsetLabel)?.let {
            RowIndexDef(it, other.offsetLabel)
        }
    } else {
        RowIndexDef(getIndex())
    }

sealed class OperatorBasedIndexPredicateLiteral(
    protected open val operator: Operator,
    protected open val operand: RowIndexDef,
) : PredicateLiteral

data class Eq(override val operand: RowIndexDef) : OperatorBasedIndexPredicateLiteral(Operator.EQ, operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = arrayOf(operand..operand)
    override fun test(index: RowIndex): Boolean = index.lookup(operand) == operand
}

fun eq(index: Int, label: String? = null): Eq = Eq(RowIndexDef(index, label))

data class Gt(override val operand: RowIndexDef) : OperatorBasedIndexPredicateLiteral(Operator.GT, operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = arrayOf(operand + 1..maxValue(operand.offsetLabel))
    override fun test(index: RowIndex): Boolean = index.lookup(operand)?.let { it > operand } ?: false
}

fun gt(index: Int, label: String? = null): Gt = Gt(RowIndexDef(index, label))

data class Lt(override val operand: RowIndexDef) : OperatorBasedIndexPredicateLiteral(Operator.LT, operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = arrayOf(minValue(operand.offsetLabel)..operand - 1)
    override fun test(index: RowIndex): Boolean = index.lookup(operand)?.let { it < operand } ?: false
}

fun lt(index: Int, label: String? = null): Lt = Lt(RowIndexDef(index, label))


data class Gte(override val operand: RowIndexDef) : OperatorBasedIndexPredicateLiteral(Operator.GTE, operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = arrayOf(operand..maxValue(operand.offsetLabel))
    override fun test(index: RowIndex): Boolean = index.lookup(operand)?.let { it >= operand } ?: false
}

fun gte(index: Int, label: String? = null): Gte = Gte(RowIndexDef(index, label))

data class Lte(override val operand: RowIndexDef) : OperatorBasedIndexPredicateLiteral(Operator.LTE, operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = arrayOf(minValue(operand.offsetLabel)..operand)
    override fun test(index: RowIndex): Boolean = index.lookup(operand)?.let { it <= operand } ?: false
}

fun lte(index: Int, label: String? = null): Lte = Lte(RowIndexDef(index, label))


sealed class LogicalOperation(
    protected open val operator: LogicalOperator,
    protected open val operandA: PredicateLiteral,
    protected open val operandB: PredicateLiteral,
) : PredicateLiteral {
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other == null) return false
        return if (other.javaClass == this.javaClass) {
            (other as LogicalOperation).let {
                operator == it.operator &&
                        ((operandA == it.operandA && operandB == it.operandB) ||
                                (operandA == it.operandB && operandB == it.operandA))
            }
        } else false
    }

    override fun hashCode(): Int {
        var result = operator.hashCode()
        result = 31 * result + operandA.hashCode()
        result = 31 * result + operandB.hashCode()
        return result
    }
}

class And(override val operandA: PredicateLiteral, override val operandB: PredicateLiteral) :
    LogicalOperation(LogicalOperator.AND, operandA, operandB) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> =
        operandA.computeRanges() and operandB.computeRanges()

    override fun test(index: RowIndex): Boolean = operandA.test(index).and(operandB.test(index))
}

infix fun PredicateLiteral.and(other: PredicateLiteral): And = And(this, other)


class Or(override val operandA: PredicateLiteral, override val operandB: PredicateLiteral) :
    LogicalOperation(LogicalOperator.OR, operandA, operandB) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = operandA.computeRanges() or operandB.computeRanges()

    override fun test(index: RowIndex): Boolean = operandA.test(index).or(operandB.test(index))
}

infix fun PredicateLiteral.or(other: PredicateLiteral): Or = Or(this, other)

infix fun ClosedRange<RowIndexDef>.intersects(other: ClosedRange<RowIndexDef>): Boolean =
    (endInclusive >= other.start && other.endInclusive >= start)

infix fun ClosedRange<RowIndexDef>.or(other: ClosedRange<RowIndexDef>): Array<ClosedRange<RowIndexDef>> {
    return if (this intersects other) {
        arrayOf(minOf(start, other.start)..maxOf(endInclusive, other.endInclusive))
    } else arrayOf(this, other)
}

infix fun Array<ClosedRange<RowIndexDef>>.or(other: ClosedRange<RowIndexDef>): Array<ClosedRange<RowIndexDef>> {
    return fold(mutableListOf(other)) { acc, next ->
        val newAcc = mutableListOf<ClosedRange<RowIndexDef>>()
        acc.forEach {
            if (it intersects next) {
                newAcc.add((it or next).first())
            } else {
                newAcc.addAll((it or next).toList())
            }
        }
        return newAcc.toTypedArray()
    }.toTypedArray()
}

infix fun Array<ClosedRange<RowIndexDef>>.or(other: Array<ClosedRange<RowIndexDef>>): Array<ClosedRange<RowIndexDef>> {
    return fold(other) { acc, next -> acc or next }
}

infix fun ClosedRange<RowIndexDef>.and(other: ClosedRange<RowIndexDef>): ClosedRange<RowIndexDef>? {
    return if (this intersects other) {
        maxOf(start, other.start)..minOf(endInclusive, other.endInclusive)
    } else null
}

infix fun Array<ClosedRange<RowIndexDef>>.and(other: Array<ClosedRange<RowIndexDef>>): Array<ClosedRange<RowIndexDef>> {
    return flatMap { left ->
        other.mapNotNull { right ->
            left and right
        }
    }.toTypedArray()
}
package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.model.RowIndexDef.Companion.maxValue
import io.github.voytech.tabulate.model.RowIndexDef.Companion.minValue
import io.github.voytech.tabulate.template.context.RowIndex
import java.util.function.Predicate


infix fun <T> RowPredicate<T>.and(other: RowPredicate<T>): RowPredicate<T> = RowPredicate { test(it) && other.test(it)}
infix fun <T> RowPredicate<T>.or(other: RowPredicate<T>): RowPredicate<T> = RowPredicate { test(it) || other.test(it)}

/**
 * A predicate that tests [SourceRow] instance
 * @author Wojciech Mąka
 * @since 0.1.0
 */
fun interface RowPredicate<T> : Predicate<SourceRow<T>>

/**
 * A predicate that tests [RowIndex] instance. A base class for predicate literals.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
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
        computedRanges.fold(setOf()) { agg, next -> agg + next.materialize() }
    }

    override fun test(sourceRow: SourceRow<T>): Boolean = indexPredicate.test(sourceRow.rowIndex)
    fun computeRanges(): Array<ClosedRange<RowIndexDef>> = computedRanges
    fun lastIndex(): RowIndexDef = computeRanges().last().endInclusive
    fun firstIndex(): RowIndexDef = computeRanges().first().start
    fun lastIndexValue(): Int = lastIndex().index
    fun firstIndexValue(): Int = firstIndex().index
    fun materialize(): Set<RowIndexDef> = computedIndices
}

fun RowIndex.lookup(other: RowIndexDef): RowIndexDef? =
    if (other.step != null) {
        getIndexOrNull(other.step.name)?.let {
            RowIndexDef(it, other.step)
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

fun eq(index: Int, step: Enum<*>? = null): Eq = Eq(RowIndexDef(index, step))

data class Gt(override val operand: RowIndexDef) : OperatorBasedIndexPredicateLiteral(Operator.GT, operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = arrayOf(operand + 1..maxValue(operand.step))
    override fun test(index: RowIndex): Boolean = index.lookup(operand)?.let { it > operand } ?: false
}

fun gt(index: Int, step: Enum<*>? = null): Gt = Gt(RowIndexDef(index, step))

data class Lt(override val operand: RowIndexDef) : OperatorBasedIndexPredicateLiteral(Operator.LT, operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = arrayOf(minValue(operand.step)..operand - 1)
    override fun test(index: RowIndex): Boolean = index.lookup(operand)?.let { it < operand } ?: false
}

fun lt(index: Int, step: Enum<*>? = null): Lt = Lt(RowIndexDef(index, step))


data class Gte(override val operand: RowIndexDef) : OperatorBasedIndexPredicateLiteral(Operator.GTE, operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = arrayOf(operand..maxValue(operand.step))
    override fun test(index: RowIndex): Boolean = index.lookup(operand)?.let { it >= operand } ?: false
}

fun gte(index: Int, step: Enum<*>? = null): Gte = Gte(RowIndexDef(index, step))

data class Lte(override val operand: RowIndexDef) : OperatorBasedIndexPredicateLiteral(Operator.LTE, operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = arrayOf(minValue(operand.step)..operand)
    override fun test(index: RowIndex): Boolean = index.lookup(operand)?.let { it <= operand } ?: false
}

fun lte(index: Int, step: Enum<*>? = null): Lte = Lte(RowIndexDef(index, step))


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
package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.model.RowIndexDef.Companion.maxValue
import io.github.voytech.tabulate.model.RowIndexDef.Companion.minValue
import io.github.voytech.tabulate.template.context.RowIndex
import java.util.function.Predicate

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

interface PredicateLiteral : IndexPredicate {
    fun computeRanges(): Array<ClosedRange<RowIndexDef>>
}

class RowIndexPredicateLiteral<T>(
    private val indexPredicate: PredicateLiteral,
) : Predicate<SourceRow<T>> {
    override fun test(sourceRow: SourceRow<T>): Boolean = indexPredicate.test(sourceRow.rowIndex)
    fun computeRanges(): Array<ClosedRange<RowIndexDef>> = indexPredicate.computeRanges()
}

fun RowIndex.getBy(other: RowIndexDef): RowIndexDef =
    if (other.offsetLabel != null) {
        RowIndexDef(getIndex(other.offsetLabel),other.offsetLabel)
    } else {
        RowIndexDef(getIndex())
    }

sealed class OperatorBasedIndexPredicateLiteral(
    private val operator: Operator,
    private val operand: RowIndexDef
): PredicateLiteral

class Eq(private val operand: RowIndexDef) : OperatorBasedIndexPredicateLiteral(Operator.EQ,operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = arrayOf(operand..operand)
    override fun test(index: RowIndex): Boolean = index.getBy(operand) == operand
}

fun eq(index: Int): Eq = Eq(RowIndexDef(index))

class Gt(private val operand: RowIndexDef) : OperatorBasedIndexPredicateLiteral(Operator.GT,operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = arrayOf(operand + 1 .. maxValue(operand.offsetLabel))
    override fun test(index: RowIndex): Boolean = index.getBy(operand) > operand
}

fun gt(index: Int): Gt = Gt(RowIndexDef(index))

class Lt(private val operand: RowIndexDef) : OperatorBasedIndexPredicateLiteral(Operator.LT,operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = arrayOf(minValue(operand.offsetLabel).. operand - 1)
    override fun test(index: RowIndex): Boolean = index.getBy(operand) < operand
}
fun lt(index: Int): Lt = Lt(RowIndexDef(index))


class Gte(private val operand: RowIndexDef) : OperatorBasedIndexPredicateLiteral(Operator.GTE,operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = arrayOf(operand..maxValue(operand.offsetLabel))
    override fun test(index: RowIndex): Boolean = index.getBy(operand) >= operand
}

fun gte(index: Int): Gte = Gte(RowIndexDef(index))

class Lte(private val operand: RowIndexDef) : OperatorBasedIndexPredicateLiteral(Operator.LTE,operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = arrayOf(minValue(operand.offsetLabel)..operand)
    override fun test(index: RowIndex): Boolean = index.getBy(operand) <= operand
}

fun lte(index: Int): Lte = Lte(RowIndexDef(index))


sealed class LogicalOperation(
    private val operator: LogicalOperator,
    private val operandA: PredicateLiteral,
    private val operandB: PredicateLiteral,
): PredicateLiteral

class And(private val operandA: PredicateLiteral, private val operandB: PredicateLiteral) : LogicalOperation(LogicalOperator.AND, operandA, operandB) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = operandA.computeRanges() and operandB.computeRanges()

    override fun test(index: RowIndex): Boolean = operandA.test(index).and(operandB.test(index))
}

infix fun PredicateLiteral.and(other: PredicateLiteral): And = And(this,other)


class Or(private val operandA: PredicateLiteral, private val  operandB: PredicateLiteral) : LogicalOperation(LogicalOperator.OR, operandA, operandB) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = operandA.computeRanges() or operandB.computeRanges()

    override fun test(index: RowIndex): Boolean = operandA.test(index).or(operandB.test(index))
}

infix fun PredicateLiteral.or(other: PredicateLiteral): Or = Or(this,other)

infix fun ClosedRange<RowIndexDef>.intersects(other: ClosedRange<RowIndexDef>) : Boolean =
    (endInclusive >= other.start && other.endInclusive >= start)

infix fun ClosedRange<RowIndexDef>.or(other: ClosedRange<RowIndexDef>): Array<ClosedRange<RowIndexDef>> {
    return if (this intersects other) {
        arrayOf(minOf(start,other.start) .. maxOf(endInclusive, other.endInclusive))
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
    return fold(other) { acc, next -> acc or next}
}

infix fun ClosedRange<RowIndexDef>.and(other: ClosedRange<RowIndexDef>): ClosedRange<RowIndexDef>? {
    return if (this intersects other) {
        maxOf(start,other.start) .. minOf(endInclusive, other.endInclusive)
    } else null
}

infix fun Array<ClosedRange<RowIndexDef>>.and(other: Array<ClosedRange<RowIndexDef>>): Array<ClosedRange<RowIndexDef>> {
    return flatMap { left ->
        other.mapNotNull { right ->
            left and right
        }
    }.toTypedArray()
}
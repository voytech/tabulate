package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.template.context.RowIndex
import java.util.function.Predicate

fun interface RowPredicate<T> : Predicate<SourceRow<T>>

fun interface IndexPredicate : Predicate<RowIndex>

fun interface IndexedValuePredicate<T>: Predicate<IndexedValue<T>>

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
    fun computeRanges(): Array<ClosedRange<RowIndex>>
}

class RowIndexPredicateLiteral<T>(
    private val indexPredicate: PredicateLiteral,
) : Predicate<SourceRow<T>> {
    override fun test(sourceRow: SourceRow<T>): Boolean = indexPredicate.test(sourceRow.rowIndex)
    fun computeRanges(): Array<ClosedRange<RowIndex>> = indexPredicate.computeRanges()
}

class RecordPredicateLiteral<T>(
    private val recordPredicate: IndexedValuePredicate<T>,
) : Predicate<SourceRow<T>> {
    override fun test(sourceRow: SourceRow<T>): Boolean =
        if (sourceRow.objectIndex != null && sourceRow.record != null)
            recordPredicate.test(IndexedValue(sourceRow.objectIndex, sourceRow.record))
        else false
}


sealed class OperatorBasedIndexPredicateLiteral(
    private val operator: Operator,
    private val operand: RowIndex
): PredicateLiteral

class Eq(private val operand: RowIndex) : OperatorBasedIndexPredicateLiteral(Operator.EQ,operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndex>> = arrayOf(operand..operand)
    override fun test(index: RowIndex): Boolean = index == operand
}

fun eq(index: Int): Eq = Eq(RowIndex(index))

class Gt(private val operand: RowIndex) : OperatorBasedIndexPredicateLiteral(Operator.GT,operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndex>> = arrayOf(operand + 1..RowIndex(Int.MAX_VALUE))
    override fun test(index: RowIndex): Boolean = index > operand
}

fun gt(index: Int): Gt = Gt(RowIndex(index))

class Lt(private val operand: RowIndex) : OperatorBasedIndexPredicateLiteral(Operator.LT,operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndex>> = arrayOf(RowIndex(0)..operand - 1)
    override fun test(index: RowIndex): Boolean = index < operand
}
fun lt(index: Int): Lt = Lt(RowIndex(index))


class Gte(private val operand: RowIndex) : OperatorBasedIndexPredicateLiteral(Operator.GTE,operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndex>> = arrayOf(operand..RowIndex(Int.MAX_VALUE))
    override fun test(index: RowIndex): Boolean = index >= operand
}

fun gte(index: Int): Gte = Gte(RowIndex(index))

class Lte(private val operand: RowIndex) : OperatorBasedIndexPredicateLiteral(Operator.LTE,operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndex>> = arrayOf(RowIndex(0)..operand)
    override fun test(index: RowIndex): Boolean = index <= operand
}

fun lte(index: Int): Lte = Lte(RowIndex(index))


sealed class LogicalOperation(
    private val operator: LogicalOperator,
    private val operandA: PredicateLiteral,
    private val operandB: PredicateLiteral,
): PredicateLiteral

class And(private val operandA: PredicateLiteral, private val operandB: PredicateLiteral) : LogicalOperation(LogicalOperator.AND, operandA, operandB) {
    override fun computeRanges(): Array<ClosedRange<RowIndex>> = operandA.computeRanges() and operandB.computeRanges()

    override fun test(index: RowIndex): Boolean = operandA.test(index).and(operandB.test(index))
}

infix fun PredicateLiteral.and(other: PredicateLiteral): And = And(this,other)


class Or(private val operandA: PredicateLiteral, private val  operandB: PredicateLiteral) : LogicalOperation(LogicalOperator.OR, operandA, operandB) {
    override fun computeRanges(): Array<ClosedRange<RowIndex>> = operandA.computeRanges() or operandB.computeRanges()

    override fun test(index: RowIndex): Boolean = operandA.test(index).or(operandB.test(index))
}

infix fun PredicateLiteral.or(other: PredicateLiteral): Or = Or(this,other)


infix fun ClosedRange<RowIndex>.intersects(other: ClosedRange<RowIndex>) : Boolean =
    (endInclusive >= other.start && other.endInclusive >= start)

infix fun ClosedRange<RowIndex>.or(other: ClosedRange<RowIndex>): Array<ClosedRange<RowIndex>> {
    return if (this intersects other) {
        arrayOf(minOf(start,other.start) .. maxOf(endInclusive, other.endInclusive))
    } else arrayOf(this, other)
}

infix fun Array<ClosedRange<RowIndex>>.or(other: ClosedRange<RowIndex>): Array<ClosedRange<RowIndex>> {
    return fold(mutableListOf(other)) { acc, next ->
        val newAcc = mutableListOf<ClosedRange<RowIndex>>()
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

infix fun Array<ClosedRange<RowIndex>>.or(other: Array<ClosedRange<RowIndex>>): Array<ClosedRange<RowIndex>> {
    return fold(other) { acc, next -> acc or next}
}

infix fun ClosedRange<RowIndex>.and(other: ClosedRange<RowIndex>): ClosedRange<RowIndex>? {
    return if (this intersects other) {
        maxOf(start,other.start) .. minOf(endInclusive, other.endInclusive)
    } else null
}

infix fun Array<ClosedRange<RowIndex>>.and(other: Array<ClosedRange<RowIndex>>): Array<ClosedRange<RowIndex>> {
    return flatMap { left ->
        other.mapNotNull { right ->
            left and right
        }
    }.toTypedArray()
}
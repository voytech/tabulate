package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.model.RowIndexDef.Companion.maxValue
import io.github.voytech.tabulate.model.RowIndexDef.Companion.minValue
import io.github.voytech.tabulate.template.context.RowIndex
import java.util.function.Predicate

/**
 * 'AND' operator infix function combining two instances of [RowPredicate]
 * @author Wojciech Mąka
 * @since 0.1.0
 */
infix fun <T> RowPredicate<T>.and(other: RowPredicate<T>): RowPredicate<T> = RowPredicate { test(it) && other.test(it)}

/**
 * 'OR' operator infix function combining two instances of [RowPredicate]
 * @author Wojciech Mąka
 * @since 0.1.0
 */
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

/**
 * Operators enumeration.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
enum class Operator {
    EQ,
    GT,
    LT,
    GTE,
    LTE
}

/**
 * Logical operators
 * @author Wojciech Mąka
 * @since 0.1.0
 */
enum class LogicalOperator {
    AND,
    OR
}

/**
 * Extends [IndexPredicate] with 'computeRanges' method. Each computed range contains lower and upper bounds which describe
 * predicate function as a literal value. It enables further optimizations.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface PredicateLiteral : IndexPredicate {
    fun computeRanges(): Array<ClosedRange<RowIndexDef>>
}

/**
 * [Predicate] that tests [SourceRow] using internal [PredicateLiteral].
 * Aggregates complex, nested index predicate expressions in order to compute index ranges. Each index range can
 * then materialize into indices satisfying predicates. It enables further optimizations.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class RowIndexPredicateLiteral<T>(
    private val indexPredicate: PredicateLiteral,
) : Predicate<SourceRow<T>> {

    private val computedRanges: Array<ClosedRange<RowIndexDef>> by lazy {
         indexPredicate.computeRanges()
    }

    private val computedIndices: Set<RowIndexDef> by lazy {
        computedRanges.fold(setOf()) { agg, next -> agg + next.materialize() }
    }

    /**
     * Predicate function.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    override fun test(sourceRow: SourceRow<T>): Boolean = indexPredicate.test(sourceRow.rowIndex)

    /**
     * Computes index range from predicate literal.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    fun computeRanges(): Array<ClosedRange<RowIndexDef>> = computedRanges

    /**
     * Returns last index in the range set.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    fun lastIndex(): RowIndexDef = computeRanges().last().endInclusive

    /**
     * Returns first index in the range set.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    fun firstIndex(): RowIndexDef = computeRanges().first().start

    /**
     * Returns first raw index value  in the range set.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    fun lastIndexValue(): Int = lastIndex().index

    /**
     * Returns last raw index value in the range set.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    fun firstIndexValue(): Int = firstIndex().index

    /**
     * Computes set of indices satisfying composite predicate.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    fun materialize(): Set<RowIndexDef> = computedIndices
}

/**
 * Converts [RowIndex] from operations domain into [RowIndexDef] which is an entity of canonical model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
fun RowIndex.lookup(other: RowIndexDef): RowIndexDef? =
    if (other.step != null) {
        getIndexOrNull(other.step.name)?.let {
            RowIndexDef(it, other.step)
        }
    } else {
        RowIndexDef(getIndex())
    }

/**
 * Base class for simple atomic index predicates.
 * Describes predicate using operator enumeration and [RowIndexDef] as immutable operand
 * @author Wojciech Mąka
 * @since 0.1.0
 */
sealed class OperatorBasedIndexPredicateLiteral(
    protected open val operator: Operator,
    protected open val operand: RowIndexDef,
) : PredicateLiteral

/**
 * Simple 'equals' predicate
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class Eq(override val operand: RowIndexDef) : OperatorBasedIndexPredicateLiteral(Operator.EQ, operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = arrayOf(operand..operand)
    override fun test(index: RowIndex): Boolean = index.lookup(operand) == operand
}

fun eq(index: Int, step: Enum<*>? = null): Eq = Eq(RowIndexDef(index, step))

/**
 * Simple 'greater than' predicate
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class Gt(override val operand: RowIndexDef) : OperatorBasedIndexPredicateLiteral(Operator.GT, operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = arrayOf(operand + 1..maxValue(operand.step))
    override fun test(index: RowIndex): Boolean = index.lookup(operand)?.let { it > operand } ?: false
}

/**
 * Simple 'greater than' predicate
 * @author Wojciech Mąka
 * @since 0.1.0
 */
fun gt(index: Int, step: Enum<*>? = null): Gt = Gt(RowIndexDef(index, step))

/**
 * Simple 'Lower than' predicate
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class Lt(override val operand: RowIndexDef) : OperatorBasedIndexPredicateLiteral(Operator.LT, operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = arrayOf(minValue(operand.step)..operand - 1)
    override fun test(index: RowIndex): Boolean = index.lookup(operand)?.let { it < operand } ?: false
}

/**
 * Simple 'Lower than' predicate
 * @author Wojciech Mąka
 * @since 0.1.0
 */
fun lt(index: Int, step: Enum<*>? = null): Lt = Lt(RowIndexDef(index, step))

/**
 * Simple 'Greater than or equal' predicate
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class Gte(override val operand: RowIndexDef) : OperatorBasedIndexPredicateLiteral(Operator.GTE, operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = arrayOf(operand..maxValue(operand.step))
    override fun test(index: RowIndex): Boolean = index.lookup(operand)?.let { it >= operand } ?: false
}

/**
 * Simple 'Greater than or equal' predicate
 * @author Wojciech Mąka
 * @since 0.1.0
 */
fun gte(index: Int, step: Enum<*>? = null): Gte = Gte(RowIndexDef(index, step))

/**
 * Simple 'Lower than or equal' predicate
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class Lte(override val operand: RowIndexDef) : OperatorBasedIndexPredicateLiteral(Operator.LTE, operand) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = arrayOf(minValue(operand.step)..operand)
    override fun test(index: RowIndex): Boolean = index.lookup(operand)?.let { it <= operand } ?: false
}

/**
 * Simple 'Lower than or equal' predicate
 * @author Wojciech Mąka
 * @since 0.1.0
 */
fun lte(index: Int, step: Enum<*>? = null): Lte = Lte(RowIndexDef(index, step))

/**
 * Base class for logical operations.
 * Describes all logical operations using operator enumeration and [RowIndexDef] two immutable operands (arguments).
 * @author Wojciech Mąka
 * @since 0.1.0
 */
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

/**
 * 'and' logical operation.
 * Describes 'and' using operator enumeration and [RowIndexDef] two immutable operands (arguments).
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class And(override val operandA: PredicateLiteral, override val operandB: PredicateLiteral) :
    LogicalOperation(LogicalOperator.AND, operandA, operandB) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> =
        operandA.computeRanges() and operandB.computeRanges()

    override fun test(index: RowIndex): Boolean = operandA.test(index).and(operandB.test(index))
}

/**
 * 'and' logical operation.
 * Describes 'and' using operator enumeration and [RowIndexDef] two immutable operands (arguments).
 * @author Wojciech Mąka
 * @since 0.1.0
 */
infix fun PredicateLiteral.and(other: PredicateLiteral): And = And(this, other)


/**
 * 'or' logical operation.
 * Describes 'or' using operator enumeration and [RowIndexDef] two immutable operands (arguments).
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class Or(override val operandA: PredicateLiteral, override val operandB: PredicateLiteral) :
    LogicalOperation(LogicalOperator.OR, operandA, operandB) {
    override fun computeRanges(): Array<ClosedRange<RowIndexDef>> = operandA.computeRanges() or operandB.computeRanges()

    override fun test(index: RowIndex): Boolean = operandA.test(index).or(operandB.test(index))
}

/**
 * 'or' logical operation.
 * Describes 'or' using operator enumeration and [RowIndexDef] two immutable operands (arguments).
 * @author Wojciech Mąka
 * @since 0.1.0
 */
infix fun PredicateLiteral.or(other: PredicateLiteral): Or = Or(this, other)

/**
 * Checks if two closed ranges intersects each other.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
infix fun ClosedRange<RowIndexDef>.intersects(other: ClosedRange<RowIndexDef>): Boolean =
    (endInclusive >= other.start && other.endInclusive >= start)

/**
 * Produces [ClosedRange] which is an alternative of two other instances [ClosedRange].
 * @author Wojciech Mąka
 * @since 0.1.0
 */
infix fun ClosedRange<RowIndexDef>.or(other: ClosedRange<RowIndexDef>): Array<ClosedRange<RowIndexDef>> {
    return if (this intersects other) {
        arrayOf(minOf(start, other.start)..maxOf(endInclusive, other.endInclusive))
    } else arrayOf(this, other)
}

/**
 * Produces array of [ClosedRange] instances which is an alternative of [ClosedRange] instance and another array of [ClosedRange] instances.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
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

/**
 * Produces array of [ClosedRange] instances as an alternative of two other arrays of [ClosedRange] instances.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
infix fun Array<ClosedRange<RowIndexDef>>.or(other: Array<ClosedRange<RowIndexDef>>): Array<ClosedRange<RowIndexDef>> {
    return fold(other) { acc, next -> acc or next }
}

/**
 * Produces [ClosedRange] as an intersection of two other [ClosedRange] instances.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
infix fun ClosedRange<RowIndexDef>.and(other: ClosedRange<RowIndexDef>): ClosedRange<RowIndexDef>? {
    return if (this intersects other) {
        maxOf(start, other.start)..minOf(endInclusive, other.endInclusive)
    } else null
}

/**
 * Produces [ClosedRange] array as an intersection of two other arrays of [ClosedRange] instances.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
infix fun Array<ClosedRange<RowIndexDef>>.and(other: Array<ClosedRange<RowIndexDef>>): Array<ClosedRange<RowIndexDef>> {
    return flatMap { left ->
        other.mapNotNull { right ->
            left and right
        }
    }.toTypedArray()
}
package io.github.voytech.tabulate.core.model

import io.github.voytech.tabulate.core.model.alignment.DefaultHorizontalAlignment
import io.github.voytech.tabulate.core.model.alignment.DefaultVerticalAlignment
import io.github.voytech.tabulate.core.model.alignment.HorizontalAlignment
import io.github.voytech.tabulate.core.model.alignment.VerticalAlignment
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.math.max
import kotlin.math.min

@JvmSynthetic
internal const val DEFAULT_GAP = 0.001F

fun Float.tbd(): BigDecimal = toBigDecimal().setScale(3, RoundingMode.HALF_UP)
fun Float.tbdc(): BigDecimal = toBigDecimal().setScale(1, RoundingMode.FLOOR)

//TODO make Width, Height X, Y inline extending Measure interface like that:
/*
interface Measure : Comparable<Float>{
    val value: Float
    override fun compareTo(other: Float): Int {
        return value.round(1).compareTo(other.round(1))
    }

   fun switchUnitOfMeasure(fromUnits: UnitsOfMeasure, referenceMeasure: Measure? = null, referenceUnits: UnitsOfMeasure? = null): Float
}

@JvmInline
value class Width(override val value: Float): Measure
value class Height(override val value: Float): Measure
value class X(override val value: Float): Measure
value class Y(override val value: Float): Measure
*/

enum class StandardUnits {
    PX,
    PT;

    fun asUnitsOfMeasure(): UnitsOfMeasure = UnitsOfMeasure.valueOf(name)

}

enum class UnitsOfMeasure {
    PX, //Pixels,
    PT, //Points,
    PC, //Percentage relative units,
    NU; //Nominal units (sequence of integers) e.g: row index in the table.

    companion object {
        fun default(): UnitsOfMeasure = PT
        val convertibleUnits: EnumSet<UnitsOfMeasure> = EnumSet.of(PT, PX)
    }
}

sealed class Measure<T : Measure<T>>(
    open val value: Float,
    open val unit: UnitsOfMeasure,
    private val clazz: Class<T>
) : Comparable<T> {

    override fun compareTo(other: T): Int {
        val otherValue = if (other.unit != unit) other.value.switchUnitOfMeasure(other.unit, unit) else other.value
        return value.tbdc().compareTo(otherValue.tbdc())
    }

    fun switchUnitOfMeasure(asUnitsOfMeasure: UnitsOfMeasure, referenceMeasure: Measure<*>? = null): T =
        if (asUnitsOfMeasure != unit && unit in UnitsOfMeasure.convertibleUnits) {
            clazz.new(value.switchUnitOfMeasure(unit, asUnitsOfMeasure), asUnitsOfMeasure)
        } else if (asUnitsOfMeasure != unit && unit == UnitsOfMeasure.PC && referenceMeasure != null) {
            val absoluteVal = value.tbd().divide(BigDecimal.valueOf(100), 3, RoundingMode.HALF_UP)
                .multiply(referenceMeasure.value.tbd()).toFloat()
            clazz.new(absoluteVal, referenceMeasure.unit).switchUnitOfMeasure(asUnitsOfMeasure)
        } else {
            @Suppress("UNCHECKED_CAST")
            this as T
        }

}

fun <T : Measure<T>> Class<T>.new(value: Float, unit: UnitsOfMeasure): T =
    getConstructor(Float::class.java, UnitsOfMeasure::class.java).newInstance(value, unit)

fun Float.switchUnitOfMeasure(sourceUnit: UnitsOfMeasure, targetUnit: UnitsOfMeasure): Float =
    if (targetUnit != sourceUnit) {
        when {
            sourceUnit == UnitsOfMeasure.PT && targetUnit == UnitsOfMeasure.PX -> (1.333f.tbd() * tbd()).toFloat()
            sourceUnit == UnitsOfMeasure.PX && targetUnit == UnitsOfMeasure.PT -> (0.75f.tbd() * tbd()).toFloat()
            else -> tbd().toFloat()
        }
    } else tbd().toFloat()


data class Width(override val value: Float, override val unit: UnitsOfMeasure) :
    Measure<Width>(value, unit, Width::class.java) {

    operator fun plus(other: Width): Width = copy(
        value = other.switchUnitOfMeasure(unit).value + value
    )

    operator fun plus(other: Float): Width = copy(
        value = other + value
    )

    operator fun minus(other: Width): Width = copy(
        value = value - other.switchUnitOfMeasure(unit).value
    )

    companion object {
        fun zero(uom: UnitsOfMeasure = UnitsOfMeasure.PT) = Width(0.0f, uom)
        fun max(uom: UnitsOfMeasure = UnitsOfMeasure.PT) = Width(Int.MAX_VALUE.toFloat(), uom)
    }
}

fun Width?.orZero(uom: UnitsOfMeasure = UnitsOfMeasure.PT) = this ?: Width.zero(uom)
fun Width?.orMax(uom: UnitsOfMeasure = UnitsOfMeasure.PT) = this ?: Width.max(uom)

data class Height(override val value: Float, override val unit: UnitsOfMeasure) :
    Measure<Height>(value, unit, Height::class.java) {

    fun asY(): Y = Y(value, unit)

    operator fun plus(other: Height): Height = copy(
        value = other.switchUnitOfMeasure(unit).value + value
    )

    operator fun plus(other: Float): Height = copy(
        value = other + value
    )

    operator fun minus(other: Height): Height = copy(
        value = value - other.switchUnitOfMeasure(unit).value
    )

    companion object {
        fun zero(uom: UnitsOfMeasure = UnitsOfMeasure.PT) = Height(0.0f, uom)
        fun max(uom: UnitsOfMeasure = UnitsOfMeasure.PT) = Height(Int.MAX_VALUE.toFloat(), uom)
    }
}

fun Height?.orZero(uom: UnitsOfMeasure = UnitsOfMeasure.PT) = this ?: Height.zero(uom)
fun Height?.orMax(uom: UnitsOfMeasure = UnitsOfMeasure.PT) = this ?: Height.max(uom)


data class Size(val width: Width, val height: Height) {
    operator fun plus(other: Size): Size = copy(
        width = width + other.width,
        height = height + other.height
    )

    fun nonZero(): Boolean = width.value > 0 || height.value > 0

    companion object {
        fun zero(uom: UnitsOfMeasure = UnitsOfMeasure.PT): Size = Size(Width.zero(uom), Height.zero(uom))
    }
}

data class X(override val value: Float, override val unit: UnitsOfMeasure) : Measure<X>(value, unit, X::class.java) {

    fun asWidth(): Width = Width(value, unit)

    operator fun plus(other: Width): X = copy(
        value = value + other.switchUnitOfMeasure(unit).value,
        unit = unit
    )

    operator fun plus(other: X): X = copy(
        value = value + other.switchUnitOfMeasure(unit).value,
        unit = unit
    )

    operator fun minus(other: X): X = copy(
        value = value - other.switchUnitOfMeasure(unit).value,
        unit = unit
    )

    operator fun minus(other: Width): X = copy(
        value = value - other.switchUnitOfMeasure(unit).value,
        unit = unit
    )

    operator fun plus(other: Float): X = copy(
        value = value + other,
        unit = unit
    )

    operator fun minus(other: Float): X = copy(
        value = value - other,
        unit = unit
    )

    companion object {
        fun zero(uom: UnitsOfMeasure = UnitsOfMeasure.PT) = X(0.0f, uom)
        fun max(uom: UnitsOfMeasure = UnitsOfMeasure.PT) = X(Int.MAX_VALUE.toFloat(), uom)
    }

}

fun X.align(alignment: HorizontalAlignment, outer: Width, inner: Width): X =
    if (outer > inner) {
        when (alignment) {
            DefaultHorizontalAlignment.CENTER -> this + ((outer - inner).value.tbd()
                .divide(2F.tbd(), 3, RoundingMode.HALF_UP)).toFloat()

            DefaultHorizontalAlignment.RIGHT -> this + (outer - inner)
            else -> this
        }
    } else this

fun Y.align(alignment: VerticalAlignment, outer: Height, inner: Height): Y =
    if (outer > inner) {
        when (alignment) {
            DefaultVerticalAlignment.MIDDLE -> this + ((outer - inner).value.tbd()
                .divide(2F.tbd(), 3, RoundingMode.HALF_UP)).toFloat()

            DefaultVerticalAlignment.BOTTOM -> this + (outer - inner)
            else -> this
        }
    } else this

data class Y(override val value: Float, override val unit: UnitsOfMeasure) : Measure<Y>(value, unit, Y::class.java) {

    fun asHeight(): Height = Height(value, unit)

    operator fun plus(other: Height): Y = copy(
        value = value + other.switchUnitOfMeasure(unit).value,
        unit = unit
    )

    operator fun plus(other: Y): Y = copy(
        value = value + other.switchUnitOfMeasure(unit).value,
        unit = unit
    )

    operator fun plus(other: Float): Y = copy(
        value = value + other,
        unit = unit
    )

    operator fun minus(other: Height): Y = copy(
        value = value - other.switchUnitOfMeasure(unit).value,
        unit = unit
    )

    operator fun minus(other: Y): Y = copy(
        value = value - other.switchUnitOfMeasure(unit).value,
        unit = unit
    )

    operator fun minus(other: Float): Y = copy(
        value = value - other,
        unit = unit
    )

    companion object {
        fun zero(uom: UnitsOfMeasure = UnitsOfMeasure.PT) = Y(0.0f, uom)
        fun max(uom: UnitsOfMeasure = UnitsOfMeasure.PT) = Y(Int.MAX_VALUE.toFloat(), uom)
    }
}

data class Position(val x: X, val y: Y) {

    operator fun plus(other: Position): Position = copy(
        x = x + other.x,
        y = y + other.y
    )

    operator fun minus(other: Position): Position = copy(
        x = x - other.x,
        y = y - other.y
    )

    operator fun plus(other: Size): Position = copy(
        x = x + other.width,
        y = y + other.height
    )

    operator fun minus(other: Size): Position = copy(
        x = x - other.width,
        y = y - other.height
    )


    fun asSize(): Size = Size(Width(x.value, x.unit), Height(y.value, y.unit))

    companion object {
        fun start(uom: UnitsOfMeasure = UnitsOfMeasure.PT) = Position(X.zero(uom), Y.zero(uom))
        fun max(uom: UnitsOfMeasure = UnitsOfMeasure.PT) = Position(X.max(uom), Y.max(uom))

        operator fun invoke(x: Float, y: Float, uom: UnitsOfMeasure = UnitsOfMeasure.PT): Position =
            Position(X(x, uom), Y(y, uom))
    }
}

fun Position?.orStart(uom: UnitsOfMeasure): Position = this ?: Position.start(uom)

fun orMax(left: Position, right: Position) = Position(
    X(max(left.x.value, right.x.switchUnitOfMeasure(left.x.unit).value), left.x.unit),
    Y(max(left.y.value, right.y.switchUnitOfMeasure(left.y.unit).value), left.y.unit)
)

fun orMin(left: Position, right: Position) = Position(
    X(min(left.x.value, right.x.switchUnitOfMeasure(left.x.unit).value), left.x.unit),
    Y(min(left.y.value, right.y.switchUnitOfMeasure(left.y.unit).value), left.y.unit)
)

inline fun <reified T : Measure<T>> orMin(left: T, right: T) =
    T::class.java.new(min(left.value, right.switchUnitOfMeasure(left.unit).value), left.unit)

inline fun <reified T : Measure<T>> orMax(left: T, right: T) =
    T::class.java.new(max(left.value, right.switchUnitOfMeasure(left.unit).value), left.unit)

data class BoundingRectangle(
    val leftTop: Position,
    val rightBottom: Position = leftTop,
) {
    operator fun plus(other: BoundingRectangle): BoundingRectangle = copy(
        leftTop = Position(
            if (leftTop.x <= other.leftTop.x) leftTop.x else other.leftTop.x,
            if (leftTop.y <= other.leftTop.y) leftTop.y else other.leftTop.y,
        ),
        rightBottom = Position(
            if (rightBottom.x <= other.rightBottom.x) other.rightBottom.x else rightBottom.x,
            if (rightBottom.y <= other.rightBottom.y) other.rightBottom.y else rightBottom.y,
        ),
    )

    fun getWidth(): Width = Width(rightBottom.x.value - leftTop.x.value, leftTop.x.unit)

    fun getHeight(): Height = Height(rightBottom.y.value - leftTop.y.value, leftTop.y.unit)

    fun size(): Size = Size(getWidth(), getHeight())
    override fun toString(): String = "LT=(${leftTop.x.value},${leftTop.y.value}),RB=(${rightBottom.x.value},${rightBottom.y.value})"
}

enum class Orientation {
    VERTICAL,
    HORIZONTAL,
}

fun Orientation.inverse(): Orientation = when (this) {
    Orientation.VERTICAL -> Orientation.HORIZONTAL
    Orientation.HORIZONTAL -> Orientation.VERTICAL
}

fun X.asColumn() = value.toInt()

fun Y.asRow() = value.toInt()

fun Int.asX() = X(toFloat(), UnitsOfMeasure.NU)

fun Int.asY() = Y(toFloat(), UnitsOfMeasure.NU)

fun Float.asWidth(uom: UnitsOfMeasure = UnitsOfMeasure.PT): Width = Width(this, uom)

fun Float.asHeight(uom: UnitsOfMeasure = UnitsOfMeasure.PT): Height = Height(this, uom)

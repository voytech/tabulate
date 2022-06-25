package io.github.voytech.tabulate.core.model

import kotlin.math.max

internal const val EPSILON = 0.001F

enum class StandardUnits {
    PX,
    PT;

    fun asUnitsOfMeasure(): UnitsOfMeasure = UnitsOfMeasure.valueOf(name)

}

enum class UnitsOfMeasure {
    PX,
    PT,
    NU;

    fun asStandardUnits(): StandardUnits? = if (NU != this) {
        StandardUnits.valueOf(name)
    } else null


    companion object {
        fun default(): UnitsOfMeasure = PT
    }
}

sealed class Measure(
    open val value: Float,
    open val unit: UnitsOfMeasure,
)

class MeasuredValue(override val value: Float, override val unit: UnitsOfMeasure) : Measure(value, unit) {
    fun width(): Width = Width(value, unit)

    fun height(): Height = Height(value, unit)

    fun x(): X = X(value, unit)

    fun y(): Y = Y(value, unit)

}

fun UnitsOfMeasure.switchUnitOfMeasure(value: Float, targetUnit: UnitsOfMeasure): Float = if (targetUnit != this) {
    when {
        this == UnitsOfMeasure.PT && targetUnit == UnitsOfMeasure.PX -> 1.333f * value
        this == UnitsOfMeasure.PX && targetUnit == UnitsOfMeasure.PT -> 0.75f * value
        else -> value
    }
} else value

data class Width(override val value: Float, override val unit: UnitsOfMeasure) : Measure(value, unit) {
    fun switchUnitOfMeasure(targetUnit: UnitsOfMeasure): Width = if (unit != targetUnit) {
        Width(unit.switchUnitOfMeasure(value, targetUnit), targetUnit)
    } else this

    companion object {
        fun zero(uom: UnitsOfMeasure = UnitsOfMeasure.PT) = Width(0.0f, uom)
    }
}

fun Width?.orZero(uom: UnitsOfMeasure = UnitsOfMeasure.PT) = this ?: Width.zero(uom)

data class Height(override val value: Float, override val unit: UnitsOfMeasure) : Measure(value, unit) {
    fun switchUnitOfMeasure(targetUnit: UnitsOfMeasure): Height = if (unit != targetUnit) {
        Height(unit.switchUnitOfMeasure(value, targetUnit), targetUnit)
    } else this

    companion object {
        fun zero(uom: UnitsOfMeasure = UnitsOfMeasure.PT) = Height(0.0f, uom)
    }
}

fun Height?.orZero(uom: UnitsOfMeasure = UnitsOfMeasure.PT) = this ?: Height.zero(uom)

data class Size(val width: Width, val height: Height)

data class X(override val value: Float, override val unit: UnitsOfMeasure) : Measure(value, unit) {
    fun switchUnitOfMeasure(targetUnit: UnitsOfMeasure): X = if (targetUnit != unit) {
        X(unit.switchUnitOfMeasure(value, targetUnit), targetUnit)
    } else this

    operator fun plus(other: Width): X = copy(
        value = value + other.switchUnitOfMeasure(unit).value,
        unit = unit
    )

    operator fun plus(other: X): X = copy(
        value = value + other.switchUnitOfMeasure(unit).value,
        unit = unit
    )

    operator fun plus(other: Float): X = copy(
        value = value + other,
        unit = unit
    )

}

data class Y(override val value: Float, override val unit: UnitsOfMeasure) : Measure(value, unit) {
    fun switchUnitOfMeasure(targetUnit: UnitsOfMeasure): Y = if (targetUnit != unit) {
        Y(unit.switchUnitOfMeasure(value, targetUnit), targetUnit)
    } else this

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

}

data class Position(val x: X, val y: Y) {
    operator fun plus(other: Position): Position = copy(
        x = X(x.value + other.x.switchUnitOfMeasure(x.unit).value, x.unit),
        y = Y(y.value + other.y.switchUnitOfMeasure(y.unit).value, y.unit)
    )

}

fun max(left: Position, right: Position) = Position(
    X(max(left.x.value, right.x.switchUnitOfMeasure(left.x.unit).value), left.x.unit),
    Y(max(left.y.value, right.y.switchUnitOfMeasure(left.y.unit).value), left.y.unit)
)

data class BoundingRectangle(
    val leftTop: Position,
    val rightBottom: Position = leftTop,
) {
    operator fun plus(other: BoundingRectangle): BoundingRectangle = copy(
        leftTop = Position(
            if (leftTop.x.value <= other.leftTop.x.value) leftTop.x else other.leftTop.x,
            if (leftTop.y.value <= other.leftTop.y.value) leftTop.y else other.leftTop.y,
        ),
        rightBottom = Position(
            if (rightBottom.x.value <= other.rightBottom.x.value) other.rightBottom.x else rightBottom.x,
            if (rightBottom.y.value <= other.rightBottom.y.value) other.rightBottom.y else rightBottom.y,
        ),
    )
}

enum class Orientation {
    VERTICAL,
    HORIZONTAL,
}

fun X.asColumn() = value.toInt()
fun Y.asRow() = value.toInt()

fun Int.asXPosition() = X(toFloat(), UnitsOfMeasure.NU)
fun Int.asYPosition() = Y(toFloat(), UnitsOfMeasure.NU)
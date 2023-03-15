package io.github.voytech.tabulate.core.template.layout

import io.github.voytech.tabulate.core.model.*
import java.util.UUID

interface AbsolutePositionPolicy {

    /**
     * Query for absolute position expressed in [targetUnit] by using current layout relative position.
     */
    fun getPosition(relativePosition: Position, targetUnit: UnitsOfMeasure): Position

    /**
     * Query for absolute X axis position expressed in [targetUnit] by using current layout relative X position.
     */
    fun getX(relativeX: X, targetUnit: UnitsOfMeasure): X

    /**
     * Query for absolute Y axis position expressed in [targetUnit] by using current layout relative Y position.
     */
    fun getY(relativeY: Y, targetUnit: UnitsOfMeasure): Y
}

interface SizeTrackingIteratorPolicyMethods {
    fun setCurrentWidth(width: Width)
    fun setCurrentHeight(height: Height)
    fun getCurrentWidth(): Width?
    fun getCurrentHeight(): Height?
    fun moveNext()
    fun begin()
    fun Layout.getCurrentX(): X
    fun Layout.getCurrentY(): Y
}

interface LayoutPolicy {

    var isSpaceMeasured: Boolean

    /**
     * Query for absolute position expressed in [targetUnit] by using current layout relative position.
     */
    fun Layout.getPosition(relativePosition: Position, targetUnit: UnitsOfMeasure): Position

    /**
     * Query for absolute X axis position expressed in [targetUnit] by using current layout relative X position.
     */
    fun Layout.getX(relativeX: X, targetUnit: UnitsOfMeasure): X

    /**
     * Query for absolute Y axis position expressed in [targetUnit] by using current layout relative Y position.
     */
    fun Layout.getY(relativeY: Y, targetUnit: UnitsOfMeasure): Y

    /**
     * Extend layout rendered space by specific [Width].
     */
    fun Layout.extend(width: Width)

    /**
     * Extend layout rendered space by specific [Height].
     */
    fun Layout.extend(height: Height)

    fun Layout.getLayoutBoundary(): BoundingRectangle = boundingRectangle

    fun Layout.elementBoundingBox(
        x: X,
        y: Y,
        width: Width? = null,
        height: Height? = null,
    ): LayoutElementBoundingBox {
        val uom = getLayoutBoundary().leftTop.x.unit
        return LayoutElementBoundingBox(
            layoutPosition = getLayoutBoundary().leftTop,
            absoluteX = x.switchUnitOfMeasure(uom),
            absoluteY = y.switchUnitOfMeasure(uom),
            width = width?.switchUnitOfMeasure(uom),
            height = height?.switchUnitOfMeasure(uom)
        )
    }

    fun Layout.setMeasured() { isSpaceMeasured = true }

    fun ModelExportContext.setOverflow(overflow: Overflow)

}

interface Layout {
    val id : String
    val uom: UnitsOfMeasure
    val leftTop: Position
    val maxRightBottom: Position?
    val boundingRectangle: BoundingRectangle
    val maxBoundingRectangle: BoundingRectangle?

    fun LayoutElementBoundingBox?.checkOverflow(): Overflow?

    fun LayoutElementBoundingBox.applyOnLayout() {
        extend(absoluteX + width.orZero())
        extend(absoluteY + height.orZero())
    }

    fun extend(position: Position)
    fun extend(width: Width)
    fun extend(x: X)
    fun extend(height: Height)
    fun extend(y: Y)
    fun exhaustX() = maxRightBottom?.x?.let { extend(it) }
    fun exhaustY() = maxRightBottom?.y?.let { extend(it) }

}

sealed class AbstractLayout(
    override val uom: UnitsOfMeasure,
    override val leftTop: Position,
    override val maxRightBottom: Position?,
    internal var rightBottom: Position = leftTop,
    override val id: String = UUID.randomUUID().toString()
) : Layout {

    override val boundingRectangle: BoundingRectangle
        get() = BoundingRectangle(leftTop, rightBottom)

    override val maxBoundingRectangle: BoundingRectangle?
        get() = maxRightBottom?.let { BoundingRectangle(leftTop, it) }

    private fun LayoutElementBoundingBox.isXOverflow(): Boolean = maxRightBottom?.let { pos ->
        ((absoluteX + width.orZero()) > pos.x).also {
            if (it) extend(pos.x)
        }
    } ?: false

    private fun LayoutElementBoundingBox.isYOverflow(): Boolean = maxRightBottom?.let { pos ->
        ((absoluteY + height.orZero()) > pos.y).also {
            if (it) extend(pos.y)
        }
    } ?: false

    override fun LayoutElementBoundingBox?.checkOverflow(): Overflow? = if (this == null) null else {
        if (isXOverflow()) {
            Overflow.X
        } else if (isYOverflow()) {
            Overflow.Y
        } else null
    }

    override fun extend(position: Position) {
        rightBottom = orMax(rightBottom, position)
    }

    override fun extend(width: Width) = extend(Position(rightBottom.x + width, rightBottom.y))

    override fun extend(x: X) = extend(Position(x, rightBottom.y))

    override fun extend(height: Height) = extend(Position(rightBottom.x, rightBottom.y + height))

    override fun extend(y: Y) = extend(Position(rightBottom.x, y))

}

class DefaultLayout(
    uom: UnitsOfMeasure,
    leftTop: Position,
    maxRightBottom: Position?,
) : AbstractLayout(uom, leftTop, maxRightBottom)

fun interface LayoutElement<PL : LayoutPolicy> {
    fun Layout.computeBoundingBox(policy: PL): LayoutElementBoundingBox
}

fun interface BoundingBoxModifier {
    fun Layout.alter(source: LayoutElementBoundingBox): LayoutElementBoundingBox
}

fun interface LayoutElementApply<PL : LayoutPolicy> {
    fun Layout.applyBoundingBox(context: LayoutElementBoundingBox, policy: PL)
}

data class LayoutElementBoundingBox(
    val layoutPosition: Position,
    val absoluteX: X,
    val absoluteY: Y,
    var width: Width? = null,
    var height: Height? = null,
) {
    val flags: MeasurementsFlags = MeasurementsFlags()

    fun unitsOfMeasure(): UnitsOfMeasure = layoutPosition.x.unit

    fun isDefined(): Boolean = width != null && height != null

    fun setFlags() {
        with(flags) { assignFlags() }
    }

    operator fun plus(other: LayoutElementBoundingBox): LayoutElementBoundingBox = copy(
        width = other.width?.switchUnitOfMeasure(unitsOfMeasure()) ?: width,
        height = other.height?.switchUnitOfMeasure(unitsOfMeasure()) ?: height
    )
}

class MeasurementsFlags {
    var shouldMeasureWidth: Boolean = false
        private set
    var shouldMeasureHeight: Boolean = false
        private set

    fun LayoutElementBoundingBox.assignFlags() {
        shouldMeasureWidth = (width == null)
        shouldMeasureHeight = (height == null)
    }
}

fun LayoutElementBoundingBox?.isDefined() = this?.isDefined() ?: false

enum class Overflow {
    X,
    Y
}

abstract class SizeTrackingLayoutIteratorPolicy : LayoutPolicy, SizeTrackingIteratorPolicyMethods {

    override var isSpaceMeasured: Boolean = false

    private val measurableWidths = mutableMapOf<Int, Boolean>()

    private val measurableHeights = mutableMapOf<Int, Boolean>()

    private val measurements: MutableMap<String, PositionAndSize> = mutableMapOf()

    data class PositionAndSize(val relativePosition: Position, var size: Size)



    fun markWidthForMeasure(index: Int, measured: Boolean = false) {
        if (isSpaceMeasured) return
        measurableWidths[index] = measured
    }

    fun markHeightForMeasure(index: Int, measured: Boolean = false) {
        if (isSpaceMeasured) return
        measurableHeights[index] = measured
    }
}
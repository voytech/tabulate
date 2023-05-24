package io.github.voytech.tabulate.core.layout

import io.github.voytech.tabulate.core.model.*
import java.util.UUID

interface AbsolutePositionMethods {

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

interface SizeTrackingIterableLayoutMethods : IterableLayout {
    fun setSlotWidth(width: Width)
    fun setSlotHeight(height: Height)
    fun getSlotWidth(uom: UnitsOfMeasure): Width?
    fun getSlotHeight(uom: UnitsOfMeasure): Height?
    fun LayoutSpace.setNextSlot()
    fun LayoutSpace.reset()
    fun LayoutSpace.getSlotX(): X
    fun LayoutSpace.getSlotY(): Y

}

data class MeasurementResults(val widthAligned: Boolean = false, val heightAligned: Boolean)

interface Layout {

    var isSpaceMeasured: Boolean

    val properties: LayoutProperties

    /**
     * Query for absolute position expressed in [targetUnit] by using current layout relative position.
     */
    fun LayoutSpace.getPosition(relativePosition: Position, targetUnit: UnitsOfMeasure): Position

    /**
     * Query for absolute X axis position expressed in [targetUnit] by using current layout relative X position.
     */
    fun LayoutSpace.getX(relativeX: X, targetUnit: UnitsOfMeasure): X

    /**
     * Query for absolute Y axis position expressed in [targetUnit] by using current layout relative Y position.
     */
    fun LayoutSpace.getY(relativeY: Y, targetUnit: UnitsOfMeasure): Y

    /**
     * Extend layout rendered space by specific [Width].
     */

    fun LayoutSpace.expandLayout(position: Position) = expand(position)

    fun LayoutSpace.isCrossingBounds(bbox: RenderableBoundingBox?): CrossedAxis?

    fun LayoutSpace.expandByRectangle(bbox: RenderableBoundingBox) = with(bbox) {
        expandLayout(Position(absoluteX + width.orZero(), absoluteY + height.orZero()))
    }

    fun handleChildExpansion(box: BoundingRectangle) {}

    /**
     * Extend layout rendered space by specific [Height].
     */

    fun LayoutSpace.getBoundingRectangle(): BoundingRectangle = boundingRectangle

    fun LayoutSpace.getMaxBoundingRectangle(): BoundingRectangle = maxBoundingRectangle ?: boundingRectangle

    fun LayoutSpace.elementBoundingBox(
        x: X,
        y: Y,
        width: Width? = null,
        height: Height? = null,
    ): RenderableBoundingBox {
        val uom = getBoundingRectangle().leftTop.x.unit
        return RenderableBoundingBox(
            layoutPosition = getBoundingRectangle().leftTop,
            absoluteX = x.switchUnitOfMeasure(uom),
            absoluteY = y.switchUnitOfMeasure(uom),
            width = width?.switchUnitOfMeasure(uom),
            height = height?.switchUnitOfMeasure(uom),
            maxWidth = maxBoundingRectangle?.getWidth(),
            maxHeight = maxBoundingRectangle?.getHeight()
        )
    }

    /**
     * Gets the size measured by the layout only when measuring is concluded (This is not the size of render space [LayoutSpace])
     */
    fun getMeasuredSize(): Size?

    /**
     * Gets the size measured by the layout so far. May be invoked while still measuring (This is not the size of render space [LayoutSpace])
     */
    fun getCurrentSize(): Size? = null

    fun getMeasurementResults(): MeasurementResults? = null

    fun LayoutSpace.setMeasured() {
        isSpaceMeasured = true
    }

}

interface IterableLayout {
    fun LayoutSpace.resolveNextPosition(): Position?
    fun LayoutSpace.hasSpaceLeft(): Boolean
}


abstract class AbstractLayout(override val properties: LayoutProperties) : Layout {
    override var isSpaceMeasured: Boolean = false
    private var measuredSize: Size? = null
    private var measurementResults: MeasurementResults? = null
    /**
     * Given layout policy in scope, returns absolute x,y position of relative position.
     * @author Wojciech Maka
     * @since 0.2.0
     */
    override fun LayoutSpace.getPosition(relativePosition: Position, targetUnit: UnitsOfMeasure): Position = Position(
        getX(relativePosition.x, targetUnit), getY(relativePosition.y, targetUnit)
    )

    /**
     * Given layout policy in scope, returns absolute X position of relative X.
     * @author Wojciech Maka
     * @since 0.2.0
     */
    override fun LayoutSpace.getX(relativeX: X, targetUnit: UnitsOfMeasure): X {
        val absoluteX = getBoundingRectangle().leftTop.x.switchUnitOfMeasure(targetUnit)
        return X(absoluteX.value + relativeX.value, targetUnit)
    }

    /**
     * Given layout policy in scope, returns absolute Y of relative Y.
     * @author Wojciech Maka
     * @since 0.2.0
     */
    override fun LayoutSpace.getY(relativeY: Y, targetUnit: UnitsOfMeasure): Y {
        val absoluteY = getBoundingRectangle().leftTop.y.switchUnitOfMeasure(targetUnit)
        return Y(absoluteY.value + relativeY.value, targetUnit)
    }

    final override fun LayoutSpace.setMeasured() {
        whileMeasuring {
            getCurrentSize()?.let {
                expandLayout(leftTop + it)
            }
            isSpaceMeasured = true
            var widthAligned = false
            var heightAligned = false
            maxRightBottom?.let {
                maxRightBottom = Position(
                    x = if (properties.fixedWidth == true) it.x else rightBottom.x.also { widthAligned = true },
                    y = if (properties.fixedHeight == true) it.y else rightBottom.y.also { heightAligned = true },
                )
                rightBottom = maxRightBottom!!
            }
            measuredSize = ((maxRightBottom ?: rightBottom) - leftTop).asSize()
            measurementResults = MeasurementResults(widthAligned, heightAligned)
        }
    }

    final override fun getMeasurementResults(): MeasurementResults? = measurementResults

    override fun getMeasuredSize(): Size? = whenMeasured { measuredSize }

    /**
     * Wraps a code block and executes only if [isSpaceMeasured] flag is set to false
     * (meaning that policy is used not for eventual guidance while rendering but only for measurements)
     * @author Wojciech Maka
     * @since 0.2.0
     */
    protected fun <R> whileMeasuring(block: () -> R): R? = if (!isSpaceMeasured) block() else null


    /**
     * Wraps a code block and executes only if [isSpaceMeasured] flag is set to true
     * (meaning that policy is now only used for eventual guidance while rendering)
     * @author Wojciech Maka
     * @since 0.2.0
     */
    protected fun <R> whenMeasured(block: () -> R): R? = if (isSpaceMeasured) block() else null

    open fun LayoutSpace.isXCrossed(bbox: RenderableBoundingBox): Boolean = maxRightBottom?.let { pos ->
        (bbox.absoluteX + bbox.width.orZero()) > pos.x
    } ?: false

    open fun LayoutSpace.isYCrossed(bbox: RenderableBoundingBox): Boolean = maxRightBottom?.let { pos ->
        (bbox.absoluteY + bbox.height.orZero()) > pos.y
    } ?: false

    override fun LayoutSpace.isCrossingBounds(bbox: RenderableBoundingBox?): CrossedAxis? =
        if (bbox == null) null else {
            if (isXCrossed(bbox)) {
                CrossedAxis.X
            } else if (isYCrossed(bbox)) {
                CrossedAxis.Y
            } else null
        }

    override fun LayoutSpace.expandByRectangle(bbox: RenderableBoundingBox) = with(bbox) {
        expandLayout(Position(absoluteX + width.orZero(), absoluteY + height.orZero()))
    }

    fun LayoutSpace.isExhausted(): Boolean = maxRightBottom?.let {
        if (properties.orientation == Orientation.HORIZONTAL) {
            it.x.value == rightBottom.x.value
        } else {
            it.y.value == rightBottom.y.value
        }
    } ?: false

}

class LayoutSpace(
    val uom: UnitsOfMeasure,
    @JvmSynthetic
    internal var leftTop: Position,
    @JvmSynthetic
    internal var maxRightBottom: Position?,
    @JvmSynthetic
    internal var rightBottom: Position = leftTop,
    val id: String = UUID.randomUUID().toString()
) {

    val boundingRectangle: BoundingRectangle
        get() = BoundingRectangle(leftTop, rightBottom)

    val maxBoundingRectangle: BoundingRectangle?
        get() = maxRightBottom?.let { BoundingRectangle(leftTop, it) }

    @JvmSynthetic
    internal fun expand(position: Position) {
        rightBottom = orMax(rightBottom, position).let { extendedRightBottom ->
            maxRightBottom?.let { orMin(extendedRightBottom, it) } ?: extendedRightBottom
        }
    }

    @JvmSynthetic
    internal fun restart(position: Position?) {
        position?.let {
            maxBoundingRectangle?.size()?.let { size ->
                maxRightBottom = it + size
            }
            leftTop = it
        }.also { rightBottom = leftTop }
    }

    companion object {
        operator fun invoke(uom: UnitsOfMeasure, constraints: SpaceConstraints): LayoutSpace {
            requireNotNull(constraints.leftTop)
            return LayoutSpace(uom, constraints.leftTop, constraints.maxRightBottom)
        }
    }
}

interface LayoutElement<PL : Layout> {
    fun LayoutSpace.defineBoundingBox(policy: PL): RenderableBoundingBox

}

fun interface MeasureLayoutElement<PL: Layout> {
    fun LayoutSpace.measureAt(policy: PL): Position
}

fun interface BoundingBoxModifier {
    fun LayoutSpace.alter(source: RenderableBoundingBox): RenderableBoundingBox
}

fun interface ApplyLayoutElement<PL : Layout> {
    fun LayoutSpace.applyBoundingBox(context: RenderableBoundingBox, policy: PL)
}

data class RenderableBoundingBox(
    val layoutPosition: Position,
    val absoluteX: X,
    val absoluteY: Y,
    // width - comes from model properties. (Set via model builder API)
    var width: Width? = null,
    // height - comes from model properties. (Set via model builder API)
    var height: Height? = null,
    // maxWidth - maximal allowed width, as constrained by enclosing model in model hierarchy
    var maxWidth: Width? = null,
    // maxHeight - maximal allowed height, as constrained by enclosing model in model hierarchy
    var maxHeight: Height? = null
) {
    val flags: MeasurementsFlags = MeasurementsFlags()

    fun unitsOfMeasure(): UnitsOfMeasure = layoutPosition.x.unit

    fun isDefined(): Boolean = width != null && height != null

    fun setFlags() {
        with(flags) { assignFlags() }
    }

    operator fun plus(other: RenderableBoundingBox): RenderableBoundingBox = copy(
        width = other.width?.switchUnitOfMeasure(unitsOfMeasure()) ?: width,
        height = other.height?.switchUnitOfMeasure(unitsOfMeasure()) ?: height
    )

    fun uniform(space: LayoutSpace): RenderableBoundingBox = copy(
        layoutPosition = space.leftTop,
        absoluteX = absoluteX.switchUnitOfMeasure(space.uom, space.maxBoundingRectangle?.getWidth()),
        absoluteY = absoluteY.switchUnitOfMeasure(space.uom, space.maxBoundingRectangle?.getHeight()),
        width = width?.switchUnitOfMeasure(space.uom, space.maxBoundingRectangle?.getWidth()),
        height = height?.switchUnitOfMeasure(space.uom, space.maxBoundingRectangle?.getHeight())
    )

}

class MeasurementsFlags {
    var shouldMeasureWidth: Boolean = false
        private set
    var shouldMeasureHeight: Boolean = false
        private set

    fun RenderableBoundingBox.assignFlags() {
        shouldMeasureWidth = (width == null)
        shouldMeasureHeight = (height == null)
    }
}

fun RenderableBoundingBox?.isDefined() = this?.isDefined() ?: false

enum class CrossedAxis {
    X,
    Y
}

data class SpaceConstraints(
    val leftTop: Position? = null,
    val maxRightBottom: Position? = null,
)

data class LayoutProperties(
    val orientation: Orientation = Orientation.VERTICAL,
    /**
     * when fixedWidth = true, maxRightBottom cannot align to measured content managed by that layout.
     * This means that the width of the component is effectively final when passed through this [SpaceConstraints] object at layout space creation.
     */
    val fixedWidth: Boolean? = false,
    /**
     * when fixedHeight = true, maxRightBottom cannot align to measured content managed by that layout.
     * This means that the height of the component is effectively final when passed through this [SpaceConstraints] object at layout space creation.
     */
    val fixedHeight: Boolean? = false,
)


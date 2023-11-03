package io.github.voytech.tabulate.core.layout

import io.github.voytech.tabulate.core.model.*
import java.util.UUID

enum class LayoutBoundaryType {
    INNER,
    OUTER
}

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

    fun LayoutSpace.reserveSpace(position: Position) = reserve(position)

    fun LayoutSpace.isCrossingBounds(
        bbox: RenderableBoundingBox, type: LayoutBoundaryType = LayoutBoundaryType.INNER
    ): CrossedAxis?

    fun LayoutSpace.reserveByRectangle(bbox: RenderableBoundingBox) = with(bbox) {
        reserveSpace(Position(absoluteX + width.orZero(), absoluteY + height.orZero()))
    }

    fun applyChildRectangle(box: BoundingRectangle) {}

    /**
     * Extend layout rendered space by specific [Height].
     */

    fun LayoutSpace.getActiveRectangle(): BoundingRectangle = activeRectangle

    fun LayoutSpace.getBoundingRectangle(type: LayoutBoundaryType? = LayoutBoundaryType.INNER): BoundingRectangle? =
        if (type == LayoutBoundaryType.INNER) innerBoundingRectangle else maxBoundingRectangle

    fun LayoutSpace.getMaxBoundingRectangle(): BoundingRectangle = maxBoundingRectangle ?: activeRectangle

    fun LayoutSpace.elementBoundingBox(
        x: X,
        y: Y,
        width: Width? = null,
        height: Height? = null,
        type: LayoutBoundaryType? = LayoutBoundaryType.INNER
    ): RenderableBoundingBox {
        val boundingRectangle = getBoundingRectangle(type)
        val uom = getActiveRectangle().leftTop.x.unit
        return RenderableBoundingBox(
            layoutPosition = getActiveRectangle().leftTop,
            absoluteX = x.switchUnitOfMeasure(uom),
            absoluteY = y.switchUnitOfMeasure(uom),
            width = width?.switchUnitOfMeasure(uom),
            height = height?.switchUnitOfMeasure(uom),
            maxWidth = boundingRectangle?.getWidth(),
            maxHeight = boundingRectangle?.getHeight()
        )
    }

    /**
     * Gets the size measured by the layout only when measuring is concluded (This is not the size of render space [LayoutSpace])
     */
    fun getMeasuredContentSize(): Size?

    /**
     * Gets the size measured by the layout so far. May be invoked while still measuring (This is not the size of render space [LayoutSpace])
     */
    fun getCurrentContentSize(): Size? = null

    /**
     * Gets the size measured by the layout (content) + size of paddings = [LayoutSpace] size)
     */
    fun LayoutSpace.getMeasuredSize(): Size?

    /**
     * Gets the size measured by the layout (content) + size of paddings = [LayoutSpace] size)
     */
    fun LayoutSpace.getCurrentSize(): Size?

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
    private var contentSize: Size? = null
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
        val absoluteX = getActiveRectangle().leftTop.x.switchUnitOfMeasure(targetUnit)
        return X(absoluteX.value + relativeX.value, targetUnit)
    }

    /**
     * Given layout policy in scope, returns absolute Y of relative Y.
     * @author Wojciech Maka
     * @since 0.2.0
     */
    override fun LayoutSpace.getY(relativeY: Y, targetUnit: UnitsOfMeasure): Y {
        val absoluteY = getActiveRectangle().leftTop.y.switchUnitOfMeasure(targetUnit)
        return Y(absoluteY.value + relativeY.value, targetUnit)
    }

    final override fun LayoutSpace.setMeasured() {
        whileMeasuring {
            getCurrentContentSize()?.let { reserveSpace(innerLeftTop + it) }
            isSpaceMeasured = true
            var widthAligned = false
            var heightAligned = false
            innerMaxRightBottom?.let {
                close(Position(
                    x = if (properties.declaredWidth) it.x else currentPosition.x.also { widthAligned = true },
                    y = if (properties.declaredHeight) it.y else currentPosition.y.also { heightAligned = true },
                ),LayoutBoundaryType.INNER)
            }
            contentSize = ((innerMaxRightBottom ?: currentPosition) - innerLeftTop).asSize()
            measurementResults = MeasurementResults(widthAligned, heightAligned)
        }
    }

    final override fun getMeasurementResults(): MeasurementResults? = measurementResults

    override fun getMeasuredContentSize(): Size? = whenMeasured { contentSize }

    override fun LayoutSpace.getMeasuredSize(): Size? = whenMeasured { maxBoundingRectangle?.size() }

    override fun LayoutSpace.getCurrentSize(): Size? = maxBoundingRectangle?.size()

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

    private fun LayoutSpace.rightBottomByType(type: LayoutBoundaryType): Position? =
        if (type == LayoutBoundaryType.INNER) innerMaxRightBottom else maxRightBottom

    open fun LayoutSpace.isXCrossed(bbox: RenderableBoundingBox, type: LayoutBoundaryType): Boolean =
        rightBottomByType(type)?.let { pos ->
            (bbox.absoluteX + bbox.width.orZero()) > pos.x
        } ?: false

    open fun LayoutSpace.isYCrossed(bbox: RenderableBoundingBox, type: LayoutBoundaryType): Boolean =
        rightBottomByType(type)?.let { pos ->
            (bbox.absoluteY + bbox.height.orZero()) > pos.y
        } ?: false

    override fun LayoutSpace.isCrossingBounds(bbox: RenderableBoundingBox, type: LayoutBoundaryType): CrossedAxis? =
        if (isXCrossed(bbox,type)) {
            CrossedAxis.X
        } else if (isYCrossed(bbox, type)) {
            CrossedAxis.Y
        } else null

    override fun LayoutSpace.reserveByRectangle(bbox: RenderableBoundingBox) = with(bbox) {
        reserveSpace(Position(absoluteX + width.orZero(), absoluteY + height.orZero()))
    }

}

class LayoutSpace(
    val uom: UnitsOfMeasure,
    @JvmSynthetic
    internal var leftTop: Position,
    @JvmSynthetic
    internal var maxRightBottom: Position?,
    // Inner corners are padding offsets. An area to be allocated by children component's layouts
    // In between outer and inner bounding boxes, layout owning component can only draw component aggregated drawables e.g: borders
    // By default - so without any padding applied from attributes - inner corners are set to values of outer corners.
    @JvmSynthetic
    internal var innerLeftTop: Position = leftTop,
    @JvmSynthetic
    internal var innerMaxRightBottom: Position? = maxRightBottom,
    @JvmSynthetic
    internal var currentPosition: Position = innerLeftTop,
    val id: String = UUID.randomUUID().toString()
) {

    private val rightBottomPadding: Size = innerMaxRightBottom?.let { inner ->
        maxRightBottom?.let { outer -> (outer - inner).asSize() } ?: Size.zero(uom)
    } ?: Size.zero(uom)

    private val leftTopPadding: Size = (innerLeftTop - leftTop).asSize()

    val activeRectangle: BoundingRectangle
        get() = BoundingRectangle(innerLeftTop, currentPosition)

    val maxBoundingRectangle: BoundingRectangle?
        get() = maxRightBottom?.let { BoundingRectangle(leftTop, it) }

    val innerBoundingRectangle: BoundingRectangle?
        get() = innerMaxRightBottom?.let { BoundingRectangle(innerLeftTop, it) }

    @JvmSynthetic
    internal fun reserve(position: Position) {
        currentPosition = orMax(currentPosition, position).let { reservedRightBottom ->
            innerMaxRightBottom?.let { orMin(reservedRightBottom, it) } ?: reservedRightBottom
        }
    }

    @JvmSynthetic
    internal fun close(position: Position, mode: LayoutBoundaryType = LayoutBoundaryType.OUTER) {
        if (mode == LayoutBoundaryType.OUTER) {
            maxRightBottom = position
            innerMaxRightBottom = position - rightBottomPadding
        } else {
            innerMaxRightBottom = position
            maxRightBottom = position + rightBottomPadding
        }
        innerMaxRightBottom?.let { currentPosition = it }
    }

    @JvmSynthetic
    internal fun restart(position: Position?) {
        position?.let {
            maxBoundingRectangle?.size()?.let { size ->
                maxRightBottom = it + size
            }
            val innerOffset = (innerLeftTop - leftTop).asSize()
            innerBoundingRectangle?.size()?.let { size ->
                innerMaxRightBottom = it + innerOffset + size
            }
            leftTop = it
            innerLeftTop = it + innerOffset
        }.also { currentPosition = innerLeftTop }
    }

    companion object {
        operator fun invoke(uom: UnitsOfMeasure, constraints: SpaceConstraints): LayoutSpace {
            requireNotNull(constraints.leftTop)
            val innerLeftTop = constraints.innerLeftTop ?: constraints.leftTop
            return LayoutSpace(
                uom, constraints.leftTop, constraints.maxRightBottom, innerLeftTop, constraints.innerMaxRightBottom
            )
        }
    }
}

interface LayoutElement<L : Layout> {

    val boundaryToFit: LayoutBoundaryType

    fun LayoutSpace.defineBoundingBox(layout: L): RenderableBoundingBox

}

fun interface MeasureLayoutElement<L : Layout> {
    fun LayoutSpace.measureAt(policy: L): Position
}

fun interface ApplyLayoutElement<L : Layout> {
    fun LayoutSpace.applyBoundingBox(context: RenderableBoundingBox, layout: L)
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

    fun normalize(space: LayoutSpace, type: LayoutBoundaryType): RenderableBoundingBox =
        (if (type == LayoutBoundaryType.OUTER) space.maxBoundingRectangle else space.innerBoundingRectangle).let { bbox ->
            copy(
                layoutPosition = space.leftTop,
                absoluteX = absoluteX.switchUnitOfMeasure(space.uom, bbox?.getWidth()),
                absoluteY = absoluteY.switchUnitOfMeasure(space.uom, bbox?.getHeight()),
                width = width?.switchUnitOfMeasure(space.uom, bbox?.getWidth()),
                height = height?.switchUnitOfMeasure(space.uom, bbox?.getHeight())
            )
        }


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
    val innerLeftTop: Position? = null,
    val innerMaxRightBottom: Position? = null
)

data class LayoutProperties(
    val orientation: Orientation = Orientation.VERTICAL,
    /**
     * when declaredWidth = true, maxRightBottom cannot align to measured content managed by that layout.
     * This means that the width of the component is effectively final when passed through this [SpaceConstraints] object at layout space creation.
     */
    val declaredWidth: Boolean = false,
    /**
     * when declaredHeight = true, maxRightBottom cannot align to measured content managed by that layout.
     * This means that the height of the component is effectively final when passed through this [SpaceConstraints] object at layout space creation.
     */
    val declaredHeight: Boolean = false,
)


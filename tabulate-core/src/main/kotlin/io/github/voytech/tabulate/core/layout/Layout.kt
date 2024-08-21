package io.github.voytech.tabulate.core.layout

import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.operation.Ok
import io.github.voytech.tabulate.core.operation.RenderingStatus
import java.util.UUID

/**
 * Enum class representing different types of layout boundaries.
 *
 * This enumeration categorizes layout boundaries into INNER and OUTER types, facilitating the distinction
 * between core content boundaries and those that include additional paddings and borders.
 *
 * The INNER boundary type defines the layout boundary for core content, encompassing all [Renderable] and [Model] nodes
 * that serve as children for the current model managing this layout.
 *
 * The OUTER boundary type extends the INNER boundary by adding paddings and borders.
 *
 * It is essential to differentiate between INNER and OUTER layouts during rendering operations that may lead to overflows.
 * Depending on the [Renderable] kind, overflow testing may be performed against OUTER layout boundaries (e.g., for rendering borders)
 * or INNER layout boundaries (e.g., for rendering actual nodes subject to layouting).
 *
 * @author Wojciech Mąka
 * @since 0.2.0
 */
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

/**
 * Interface representing an autonomous layout within a defined layout space.
 *
 * An autonomous layout is responsible for managing the positioning of elements within a layout space.
 * Implementing classes must provide methods for resetting the layout, resolving the next position for an element,
 * and determining if there is remaining space within the layout.
 *
 * This layout interface does not require invocation of additional methods during performing layout. All implementations
 * are entirely managed by layout engine and no client code is assumed to interact with this layout.
 *
 * @see Region
 * @see Position
 * @author Wojciech Mąka
 * @since 0.2.0
 */
interface AutonomousLayout {
    fun Region.reset()

    fun Region.resolveNextPosition(): Position?

    fun Region.hasSpaceLeft(): Boolean
}

data class MeasurementResults(val widthAligned: Boolean = false, val heightAligned: Boolean)

interface Layout {

    var isMeasured: Boolean

    val properties: LayoutProperties

    /**
     * Query for absolute position expressed in [targetUnit] by using current layout relative position.
     */
    fun Region.getPosition(relativePosition: Position, targetUnit: UnitsOfMeasure): Position

    /**
     * Query for absolute X axis position expressed in [targetUnit] by using current layout relative X position.
     */
    fun Region.getX(relativeX: X, targetUnit: UnitsOfMeasure): X

    /**
     * Query for absolute Y axis position expressed in [targetUnit] by using current layout relative Y position.
     */
    fun Region.getY(relativeY: Y, targetUnit: UnitsOfMeasure): Y

    /**
     * Extend layout rendered space by specific [Width].
     */

    fun Region.allocateSpace(position: Position)

    fun Region.allocateRectangle(bbox: RenderableBoundingBox)

    fun Region.isCrossingBounds(
        bbox: RenderableBoundingBox, type: LayoutBoundaryType = LayoutBoundaryType.INNER
    ): Axis?

    fun applyAllocatedRectangle(box: BoundingRectangle) {}

    /**
     * Extend layout rendered space by specific [Height].
     */

    fun Region.getActiveRectangle(): BoundingRectangle

    fun Region.getMaxBoundingRectangle(): BoundingRectangle

    fun Region.getBoundingRectangle(type: LayoutBoundaryType? = LayoutBoundaryType.INNER): BoundingRectangle

    fun Region.getRenderableBoundingBox(
        x: X, y: Y, width: Width? = null, height: Height? = null, type: LayoutBoundaryType
    ): RenderableBoundingBox

    /**
     * Gets the size measured by the layout only when measuring is concluded (This is not the size of render space [Region])
     */
    fun getMeasuredContentSize(): Size?

    /**
     * Gets the size measured by the layout so far. May be invoked while still measuring (This is not the size of render space [Region])
     */
    fun getCurrentContentSize(): Size? = null

    /**
     * Gets the size measured by the layout (content) + size of paddings = [Region] size)
     */
    fun Region.getMeasuredSize(): Size?

    /**
     * Gets the size measured by the layout (content) + size of paddings = [Region] size)
     */
    fun Region.getCurrentSize(): Size?

    /**
     * Gets the width declared by API (by WidthAttribute and HeightAttribute)
     */
    fun Region.getExplicitWidth(mode: LayoutBoundaryType = LayoutBoundaryType.OUTER): Width?

    /**
     * Gets the height declared by API (by WidthAttribute and HeightAttribute)
     */
    fun Region.getExplicitHeight(mode: LayoutBoundaryType = LayoutBoundaryType.OUTER): Height?

    fun getMeasurementResults(): MeasurementResults? = null

    fun Region.setMeasured() {
        isMeasured = true
    }

}

abstract class AbstractLayout(override val properties: LayoutProperties) : Layout {
    override var isMeasured: Boolean = false
    private var contentSize: Size? = null
    private var measurementResults: MeasurementResults? = null

    /**
     * Given layout policy in scope, returns absolute x,y position of relative position.
     * @author Wojciech Maka
     * @since 0.2.0
     */
    override fun Region.getPosition(relativePosition: Position, targetUnit: UnitsOfMeasure): Position = Position(
        getX(relativePosition.x, targetUnit), getY(relativePosition.y, targetUnit)
    )

    /**
     * Given layout policy in scope, returns absolute X position of relative X.
     * @author Wojciech Maka
     * @since 0.2.0
     */
    override fun Region.getX(relativeX: X, targetUnit: UnitsOfMeasure): X {
        val absoluteX = getActiveRectangle().leftTop.x.switchUnitOfMeasure(targetUnit)
        return X((absoluteX + relativeX).value, targetUnit)
    }

    /**
     * Given layout policy in scope, returns absolute Y of relative Y.
     * @author Wojciech Maka
     * @since 0.2.0
     */
    override fun Region.getY(relativeY: Y, targetUnit: UnitsOfMeasure): Y {
        val absoluteY = getActiveRectangle().leftTop.y.switchUnitOfMeasure(targetUnit)
        return Y((absoluteY + relativeY).value, targetUnit)
    }

    final override fun Region.setMeasured() {
        whileMeasuring {
            getCurrentContentSize()?.let { allocateSpace(innerLeftTop + it) }
            isMeasured = true
            var widthAligned = false
            var heightAligned = false
            close(
                Position(
                    x = if (properties.declaredWidth) innerMaxRightBottom.x else currentPosition.x
                        .also { widthAligned = true },
                    y = if (properties.declaredHeight) innerMaxRightBottom.y else currentPosition.y
                        .also { heightAligned = true },
                ), LayoutBoundaryType.INNER
            )
            contentSize = (innerMaxRightBottom - innerLeftTop).asSize()
            measurementResults = MeasurementResults(widthAligned, heightAligned)
        }
    }

    final override fun getMeasurementResults(): MeasurementResults? = measurementResults

    override fun getMeasuredContentSize(): Size? = whenMeasured { contentSize }

    override fun Region.getMeasuredSize(): Size? = whenMeasured { maxBoundingRectangle.size() }

    override fun Region.getCurrentSize(): Size? = maxBoundingRectangle.size()

    final override fun Region.getActiveRectangle(): BoundingRectangle = activeRectangle

    final override fun Region.getMaxBoundingRectangle(): BoundingRectangle = maxBoundingRectangle

    final override fun Region.getBoundingRectangle(type: LayoutBoundaryType?): BoundingRectangle =
        if (type == LayoutBoundaryType.INNER) innerBoundingRectangle else maxBoundingRectangle

    final override fun Region.getExplicitWidth(mode: LayoutBoundaryType): Width? = if (properties.declaredWidth) {
        if (mode == LayoutBoundaryType.OUTER) {
            maxBoundingRectangle.size().width
        } else {
            innerBoundingRectangle.size().width
        }
    } else null

    final override fun Region.getExplicitHeight(mode: LayoutBoundaryType): Height? =
        if (properties.declaredHeight) {
            if (mode == LayoutBoundaryType.OUTER) {
                maxBoundingRectangle.size().height
            } else {
                innerBoundingRectangle.size().height
            }
        } else null

    /**
     * Wraps a code block and executes only if [isMeasured] flag is set to false
     * (meaning that policy is used not for eventual guidance while rendering but only for measurements)
     * @author Wojciech Maka
     * @since 0.2.0
     */
    protected fun <R> whileMeasuring(block: () -> R): R? = if (!isMeasured) block() else null


    /**
     * Wraps a code block and executes only if [isMeasured] flag is set to true
     * (meaning that policy is now only used for eventual guidance while rendering)
     * @author Wojciech Maka
     * @since 0.2.0
     */
    protected fun <R> whenMeasured(block: () -> R): R? = if (isMeasured) block() else null

    private fun Region.rightBottomByType(type: LayoutBoundaryType): Position =
        if (type == LayoutBoundaryType.INNER) innerMaxRightBottom else maxRightBottom

    open fun Region.isXCrossed(bbox: RenderableBoundingBox, type: LayoutBoundaryType): Boolean =
        rightBottomByType(type).let { pos ->
            (bbox.absoluteX + bbox.width.orZero()) > pos.x
        }

    open fun Region.isYCrossed(bbox: RenderableBoundingBox, type: LayoutBoundaryType): Boolean =
        rightBottomByType(type).let { pos ->
            (bbox.absoluteY + bbox.height.orZero()) > pos.y
        }

    final override fun Region.isCrossingBounds(
        bbox: RenderableBoundingBox,
        type: LayoutBoundaryType
    ): Axis? =
        if (isXCrossed(bbox, type)) {
            Axis.X
        } else if (isYCrossed(bbox, type)) {
            Axis.Y
        } else null

    final override fun Region.allocateSpace(position: Position) {
        allocate(position)
    }

    final override fun Region.allocateRectangle(bbox: RenderableBoundingBox) = with(bbox) {
        allocateSpace(Position(absoluteX + width.orZero(), absoluteY + height.orZero()))
    }

    final override fun Region.getRenderableBoundingBox(
        x: X,
        y: Y,
        width: Width?,
        height: Height?,
        type: LayoutBoundaryType
    ): RenderableBoundingBox {
        val boundingRectangle = getBoundingRectangle(type)
        return RenderableBoundingBox(
            cropBoxLeftTop = boundingRectangle.leftTop,
            cropBoxRightBottom = boundingRectangle.leftTop + boundingRectangle.size(),
            absoluteX = x.switchUnitOfMeasure(uom),
            absoluteY = y.switchUnitOfMeasure(uom),
            width = width?.switchUnitOfMeasure(uom),
            height = height?.switchUnitOfMeasure(uom)
        )
    }

    fun Region.getHorizontalSpacing(): Float =
        if (innerLeftTop.x == currentPosition.x) 0F else properties.horizontalSpacing

    fun Region.getVerticalSpacing(): Float =
        if (innerLeftTop.y == currentPosition.y) 0F else properties.verticalSpacing

}

class Region(
    val uom: UnitsOfMeasure,
    @JvmSynthetic
    internal var leftTop: Position,
    @JvmSynthetic
    internal var maxRightBottom: Position,
    // Inner corners are padding offsets. An area to be allocated by children component's layouts
    // In between outer and inner bounding boxes, layout owning component can only draw component aggregated drawables e.g: borders
    // By default - so without any padding applied from attributes - inner corners are set to values of outer corners.
    @JvmSynthetic
    internal var innerLeftTop: Position = leftTop,
    @JvmSynthetic
    internal var innerMaxRightBottom: Position = maxRightBottom,
    @JvmSynthetic
    internal var currentPosition: Position = innerLeftTop,
    val id: String = UUID.randomUUID().toString()
) {

    private val rightBottomPadding: Size = (maxRightBottom - innerMaxRightBottom).asSize()

    val activeRectangle: BoundingRectangle
        get() = BoundingRectangle(innerLeftTop, currentPosition)

    val maxBoundingRectangle: BoundingRectangle
        get() = BoundingRectangle(leftTop, maxRightBottom)

    val innerBoundingRectangle: BoundingRectangle
        get() = BoundingRectangle(innerLeftTop, innerMaxRightBottom)

    @JvmSynthetic
    internal fun allocate(position: Position) {
        currentPosition = orMin(orMax(currentPosition, position), innerMaxRightBottom)
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
        currentPosition = innerMaxRightBottom
    }

    @JvmSynthetic
    internal fun restart(position: Position?) {
        position?.let {
            maxRightBottom = it + maxBoundingRectangle.size()
            val padding = (innerLeftTop - leftTop).asSize()
            innerMaxRightBottom = it + padding + innerBoundingRectangle.size()
            leftTop = it
            innerLeftTop = it + padding
        }.also { currentPosition = innerLeftTop }
    }

    override fun toString(): String {
        return "OUTER [$maxBoundingRectangle] INNER: [$innerBoundingRectangle]"
    }

    companion object {
        operator fun invoke(uom: UnitsOfMeasure, constraints: SpaceConstraints): Region {
            requireNotNull(constraints.leftTop)
            requireNotNull(constraints.maxRightBottom)
            val innerLeftTop = constraints.innerLeftTop ?: constraints.leftTop
            val innerMaxRightBottom = constraints.innerMaxRightBottom ?: constraints.maxRightBottom
            return Region(uom, constraints.leftTop, constraints.maxRightBottom, innerLeftTop, innerMaxRightBottom)
        }
    }
}

interface LayoutElement<L : Layout> {

    val boundaryToFit: LayoutBoundaryType

    fun Region.defineBoundingBox(layout: L): RenderableBoundingBox

}

fun interface MeasureLayoutElement<L : Layout> {
    fun Region.measureAt(policy: L): Position
}

interface ApplyLayoutElement<L : Layout> {
    fun Region.applyBoundingBox(bbox: RenderableBoundingBox, layout: L, status: RenderingStatus = Ok) = with(layout) {
        allocateRectangle(bbox)
    }
}

// TODO layoutPosition -> minLeftTop ; maxWidth+maxHeight -> maxRightBottom  (absolute boundaries of this renderable bounding box enforced by a layout in which context a renderable is being rendered.)
data class RenderableBoundingBox(
    val cropBoxLeftTop: Position,
    val cropBoxRightBottom: Position,
    val absoluteX: X,
    val absoluteY: Y,
    // width - comes from model properties. (Set via model builder API)
    var width: Width? = null,
    // height - comes from model properties. (Set via model builder API)
    var height: Height? = null
) {

    val maxRightBottom: Position = cropBoxRightBottom

    val maxWidth: Width
        get() = (cropBoxRightBottom.x - absoluteX).asWidth()

    val maxHeight: Height
        get() = (cropBoxRightBottom.y - absoluteY).asHeight()

    fun unitsOfMeasure(): UnitsOfMeasure = cropBoxLeftTop.x.unit

    fun isDefined(): Boolean = width != null && height != null

    operator fun plus(other: RenderableBoundingBox): RenderableBoundingBox = copy(
        width = other.width?.switchUnitOfMeasure(unitsOfMeasure()) ?: width,
        height = other.height?.switchUnitOfMeasure(unitsOfMeasure()) ?: height
    )

    fun convertUnits(child: Region, type: LayoutBoundaryType, parent: Region?): RenderableBoundingBox =
        (if (type == LayoutBoundaryType.OUTER) child.maxBoundingRectangle else child.innerBoundingRectangle).let { bbox ->
            val parentRect = parent?.innerBoundingRectangle ?: bbox
            copy(
                cropBoxLeftTop = bbox.leftTop,
                cropBoxRightBottom = bbox.leftTop + bbox.size(),
                absoluteX = absoluteX.switchUnitOfMeasure(child.uom, parentRect.getWidth()),
                absoluteY = absoluteY.switchUnitOfMeasure(child.uom, parentRect.getHeight()),
                width = width?.switchUnitOfMeasure(child.uom, parentRect.getWidth()),
                height = height?.switchUnitOfMeasure(child.uom, parentRect.getHeight())
            )
        }

    /**
     * Adjusts the dimensions of a {@link RenderableBoundingBox} to fit within the layout bounds
     * defined by the associated {@link LayoutApi}.
     *
     * This function updates the height and width of the given bounding box based on the maximum
     * height and width specified by the layout. If the height or width of the bounding box exceeds
     * the corresponding maximum value, it is set to the maximum allowed value.
     *
     * @param boundingBox The {@link RenderableBoundingBox} to be fitted into the layout bounds.
     * @author Wojciech Mąka
     * @since 0.2.0
     */
    fun revalidateSize(): RenderableBoundingBox = apply {
        height?.let { height = minOf(it, maxHeight) }
        width?.let { width = minOf(it, maxWidth) }
    }

}

fun RenderableBoundingBox?.isDefined() = this?.isDefined() ?: false

enum class Axis {
    X,
    Y
}

data class SpaceConstraints(
    val leftTop: Position? = null,
    val maxRightBottom: Position? = null,
    val innerLeftTop: Position? = null,
    val innerMaxRightBottom: Position? = null
) {
    companion object {
        fun atLeftTop(): SpaceConstraints = SpaceConstraints(leftTop = Position.start())
    }
}

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

    val horizontalSpacing: Float = 0.1F,
    val verticalSpacing: Float = 0.1F
)


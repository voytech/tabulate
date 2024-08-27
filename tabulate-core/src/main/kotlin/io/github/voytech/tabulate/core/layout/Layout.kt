package io.github.voytech.tabulate.core.layout

import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.operation.Ok
import io.github.voytech.tabulate.core.operation.RenderingStatus
import io.github.voytech.tabulate.round
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
    OUTER,
    CONTENT,
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

    fun resolveNextPosition(): Position?

    fun hasSpaceLeft(): Boolean
}

data class MeasurementResults(val widthAligned: Boolean = false, val heightAligned: Boolean)

interface Layout {

    var isMeasured: Boolean

    val properties: LayoutProperties

    val uom: UnitsOfMeasure

    fun initialize(constraints: RegionConstraints)

    fun reset(position: Position? = null)
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

    /**
     * Extend layout rendered space by specific [Width].
     */

    fun allocateSpace(position: Position)

    fun allocateRectangle(bbox: RenderableBoundingBox)

    fun absorb(layout: Layout)

    fun isCrossingBounds(
        bbox: RenderableBoundingBox, type: LayoutBoundaryType = LayoutBoundaryType.INNER
    ): Axis?


    /**
     * Extend layout rendered space by specific [Height].
     */

    fun getActiveRectangle(): BoundingRectangle

    fun getMaxBoundingRectangle(): BoundingRectangle = getBoundingRectangle(LayoutBoundaryType.OUTER)

    fun getInnerBoundingRectangle(): BoundingRectangle = getBoundingRectangle(LayoutBoundaryType.INNER)

    fun getContentBoundingRectangle(): BoundingRectangle = getBoundingRectangle(LayoutBoundaryType.INNER)

    fun getBoundingRectangle(type: LayoutBoundaryType? = LayoutBoundaryType.INNER): BoundingRectangle

    fun getRenderableBoundingBox(
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
    fun getMeasuredSize(): Size?

    /**
     * Gets the size measured by the layout (content) + size of paddings = [Region] size)
     */
    fun getCurrentSize(): Size?

    /**
     * Gets the width declared by API (by WidthAttribute and HeightAttribute)
     */
    fun getExplicitWidth(mode: LayoutBoundaryType = LayoutBoundaryType.OUTER): Width?

    /**
     * Gets the height declared by API (by WidthAttribute and HeightAttribute)
     */
    fun getExplicitHeight(mode: LayoutBoundaryType = LayoutBoundaryType.OUTER): Height?

    fun getMeasurementResults(): MeasurementResults? = null

    fun setMeasured() {
        isMeasured = true
    }

}

abstract class AbstractLayout(
    override val properties: LayoutProperties
) : Layout {
    internal lateinit var region: Region
        private set
    override var isMeasured: Boolean = false
    private var contentSize: Size? = null
    private var measurementResults: MeasurementResults? = null
    override val uom: UnitsOfMeasure = UnitsOfMeasure.PT

    override fun initialize(constraints: RegionConstraints) {
        if (this::region.isInitialized) return
        region = Region(uom, constraints)
        onBeginLayout()
    }

    final override fun reset(position: Position?) {
        region.reset(position)
        onBeginLayout()
    }

    protected open fun onBeginLayout() {}

    /**
     * Given layout policy in scope, returns absolute x,y position of relative position.
     * @author Wojciech Maka
     * @since 0.2.0
     */
    override fun getPosition(relativePosition: Position, targetUnit: UnitsOfMeasure): Position = Position(
        getX(relativePosition.x, targetUnit), getY(relativePosition.y, targetUnit)
    )

    /**
     * Given layout policy in scope, returns absolute X position of relative X.
     * @author Wojciech Maka
     * @since 0.2.0
     */
    override fun getX(relativeX: X, targetUnit: UnitsOfMeasure): X {
        val absoluteX = getActiveRectangle().leftTop.x.switchUnitOfMeasure(targetUnit)
        return X((absoluteX + relativeX).value, targetUnit)
    }

    /**
     * Given layout policy in scope, returns absolute Y of relative Y.
     * @author Wojciech Maka
     * @since 0.2.0
     */
    override fun getY(relativeY: Y, targetUnit: UnitsOfMeasure): Y {
        val absoluteY = getActiveRectangle().leftTop.y.switchUnitOfMeasure(targetUnit)
        return Y((absoluteY + relativeY).value, targetUnit)
    }

    final override fun setMeasured() {
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

    override fun getMeasuredSize(): Size? = whenMeasured { maxBoundingRectangle.size() }

    override fun getCurrentSize(): Size? = region.maxBoundingRectangle.size()

    final override fun getActiveRectangle(): BoundingRectangle = region.activeRectangle

    final override fun getMaxBoundingRectangle(): BoundingRectangle = region.maxBoundingRectangle

    final override fun getBoundingRectangle(type: LayoutBoundaryType?): BoundingRectangle =
        if (type == LayoutBoundaryType.INNER) region.innerBoundingRectangle else region.maxBoundingRectangle

    final override fun getExplicitWidth(mode: LayoutBoundaryType): Width? = if (properties.declaredWidth) {
        if (mode == LayoutBoundaryType.OUTER) {
            region.maxBoundingRectangle.size().width
        } else {
            region.innerBoundingRectangle.size().width
        }
    } else null

    final override fun getExplicitHeight(mode: LayoutBoundaryType): Height? =
        if (properties.declaredHeight) {
            if (mode == LayoutBoundaryType.OUTER) {
                region.maxBoundingRectangle.size().height
            } else {
                region.innerBoundingRectangle.size().height
            }
        } else null

    /**
     * Wraps a code block and executes only if [isMeasured] flag is set to false
     * (meaning that policy is used not for eventual guidance while rendering but only for measurements)
     * @author Wojciech Maka
     * @since 0.2.0
     */
    protected fun <R> whileMeasuring(block: Region.() -> R): R? = if (!isMeasured) block(region) else null


    /**
     * Wraps a code block and executes only if [isMeasured] flag is set to true
     * (meaning that policy is now only used for eventual guidance while rendering)
     * @author Wojciech Maka
     * @since 0.2.0
     */
    protected fun <R> whenMeasured(block: Region.() -> R): R? = if (isMeasured) block(region) else null

    private fun rightBottomByType(type: LayoutBoundaryType): Position =
        if (type == LayoutBoundaryType.INNER) region.innerMaxRightBottom else region.maxRightBottom

    open fun isXCrossed(bbox: RenderableBoundingBox, type: LayoutBoundaryType): Boolean =
        rightBottomByType(type).let { pos ->
            val bboxRight = bbox.absoluteX + bbox.width.orZero()
            return (bboxRight > pos.x).also { if (it) trace("X Overflow: bboxRightX: $bboxRight, pos.x: ${pos.x}") }
        }

    open fun isYCrossed(bbox: RenderableBoundingBox, type: LayoutBoundaryType): Boolean =
        rightBottomByType(type).let { pos ->
            val bboxBottom = bbox.absoluteY + bbox.height.orZero()
            return (bboxBottom > pos.y)
                .also {
                    if (it) trace(
                        "Y Overflow: bboxBottomY: ${bboxBottom.value.round(1)}, pos.y: ${
                            pos.y.value.round(
                                1
                            )
                        }"
                    )
                }
        }

    final override fun isCrossingBounds(
        bbox: RenderableBoundingBox,
        type: LayoutBoundaryType
    ): Axis? =
        if (isXCrossed(bbox, type)) {
            Axis.X
        } else if (isYCrossed(bbox, type)) {
            Axis.Y
        } else null

    final override fun allocateSpace(position: Position) {
        region.allocate(position)
    }

    final override fun allocateRectangle(bbox: RenderableBoundingBox) = with(bbox) {
        allocateSpace(Position(absoluteX + width.orZero(), absoluteY + height.orZero()))
    }

    final override fun absorb(layout: Layout) {
        val boundingRectangle = layout.getMaxBoundingRectangle()
        allocateSpace(boundingRectangle.rightBottom)
        onChildLayoutAbsorbed(boundingRectangle)
    }

    open fun onChildLayoutAbsorbed(boundingRectangle: BoundingRectangle) { }

    final override fun getRenderableBoundingBox(
        x: X,
        y: Y,
        width: Width?,
        height: Height?,
        type: LayoutBoundaryType
    ): RenderableBoundingBox {
        val uom = region.uom
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

    fun getHorizontalSpacing(): Float =
        if (region.innerLeftTop.x == region.currentPosition.x) 0F else properties.horizontalSpacing

    fun getVerticalSpacing(): Float =
        if (region.innerLeftTop.y == region.currentPosition.y) 0F else properties.verticalSpacing

}

class Region(
    val uom: UnitsOfMeasure,
    @JvmSynthetic
    internal var leftTop: Position,
    @JvmSynthetic
    internal var maxRightBottom: Position,
    @JvmSynthetic
    internal var innerLeftTop: Position = leftTop,
    @JvmSynthetic
    internal var innerMaxRightBottom: Position = maxRightBottom,
    @JvmSynthetic
    internal var contentLeftTop: Position = innerLeftTop,
    @JvmSynthetic
    internal var contentMaxRightBottom: Position = innerMaxRightBottom,
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
            maxRightBottom = position + rightBottomPadding
            innerMaxRightBottom = position
        }
        currentPosition = innerMaxRightBottom
    }

    @JvmSynthetic
    internal fun reset(position: Position? = null) {
        position?.let {
            val maxBoundingRectangleSize = maxBoundingRectangle.size()
            val innerBoundingRectangleSize = innerBoundingRectangle.size()
            val padding = (innerLeftTop - leftTop).asSize()
            leftTop = it
            innerLeftTop = it + padding
            maxRightBottom = leftTop + maxBoundingRectangleSize
            innerMaxRightBottom = innerLeftTop + innerBoundingRectangleSize
        }.also { currentPosition = innerLeftTop }
    }

    override fun toString(): String {
        return "OUTER [$maxBoundingRectangle] INNER: [$innerBoundingRectangle]"
    }

    companion object {
        operator fun invoke(uom: UnitsOfMeasure, constraints: RegionConstraints): Region {
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

    fun L.defineBoundingBox(): RenderableBoundingBox

}

fun interface MeasureLayoutElement<L : Layout> {
    fun measureAt(layout: L): Position
}

interface ApplyLayoutElement<L : Layout> {
    fun L.absorbRenderableBoundingBox(bbox: RenderableBoundingBox, status: RenderingStatus = Ok)
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

    fun convertUnits(child: Layout, type: LayoutBoundaryType, parent: Layout?): RenderableBoundingBox =
        (if (type == LayoutBoundaryType.OUTER) child.getMaxBoundingRectangle() else child.getInnerBoundingRectangle()).let { bbox ->
            val parentRect = parent?.getInnerBoundingRectangle() ?: bbox
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

data class RegionConstraints(
    val leftTop: Position? = null,
    val maxRightBottom: Position? = null,
    val innerLeftTop: Position? = null,
    val innerMaxRightBottom: Position? = null
) {
    companion object {
        fun atLeftTop(): RegionConstraints = RegionConstraints(leftTop = Position.start())
    }
}

data class LayoutProperties(
    val orientation: Orientation = Orientation.VERTICAL,
    /**
     * when declaredWidth = true, maxRightBottom cannot align to measured content managed by that layout.
     * This means that the width of the component is effectively final when passed through this [RegionConstraints] object at layout space creation.
     */
    val declaredWidth: Boolean = false,
    /**
     * when declaredHeight = true, maxRightBottom cannot align to measured content managed by that layout.
     * This means that the height of the component is effectively final when passed through this [RegionConstraints] object at layout space creation.
     */
    val declaredHeight: Boolean = false,

    val horizontalSpacing: Float = 0.1F,
    val verticalSpacing: Float = 0.1F
)


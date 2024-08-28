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
enum class BoundaryType {
    MARGIN,
    BORDER,
    PADDING,
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
 * Interface representing a sequential layout within a defined layout region.
 *
 * A sequential layout is responsible for managing the positioning of elements within a layout region.
 * Implementing classes must provide methods for resetting the layout, resolving the next position for an element,
 * and determining if there is remaining space within the layout.
 *
 * This layout interface does not require invocation of additional methods during performing layout. All implementations
 * are entirely managed by layout engine and no client code in model.export and model.measure methods are assumed to interact with this layout.
 * That is because sequential layout acts autonomously instructed by layouting logic that is its implementation detail.
 *
 * @see Region
 * @see Position
 * @author Wojciech Mąka
 * @since 0.2.0
 */
interface SequentialLayout {

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

    /**
     * Extend layout rendered space by specific [Height].
     */

    fun getActiveRectangle(): BoundingRectangle

    fun getMaxBoundingRectangle(): BoundingRectangle = getBoundingRectangle(BoundaryType.MARGIN)

    fun getBorderRectangle(): BoundingRectangle = getBoundingRectangle(BoundaryType.BORDER)

    fun getPaddingRectangle(): BoundingRectangle = getBoundingRectangle(BoundaryType.PADDING)

    fun getContentRectangle(): BoundingRectangle = getBoundingRectangle(BoundaryType.CONTENT)

    fun getBoundingRectangle(type: BoundaryType? = BoundaryType.CONTENT): BoundingRectangle

    fun getRenderableBoundingBox(
        x: X, y: Y, width: Width? = null, height: Height? = null, type: BoundaryType
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
    fun getExplicitWidth(type: BoundaryType = BoundaryType.MARGIN): Width?

    /**
     * Gets the height declared by API (by WidthAttribute and HeightAttribute)
     */
    fun getExplicitHeight(type: BoundaryType = BoundaryType.MARGIN): Height?

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
        //validate constraints
        region = Region(constraints)
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
            getCurrentContentSize()?.let { allocateSpace(contentLeftTop + it) }
            isMeasured = true
            var widthAligned = false
            var heightAligned = false
            val contentRightBottomX = if (properties.declaredWidth) contentRightBottom.x else contentCursorPosition.x
                .also { widthAligned = true }
            val contentRightBottomY = if (properties.declaredHeight) contentRightBottom.y else contentCursorPosition.y
                .also { heightAligned = true }
            close(Position(contentRightBottomX, contentRightBottomY), BoundaryType.CONTENT)
            contentSize = contentBoundingRectangle.size()
            measurementResults = MeasurementResults(widthAligned, heightAligned)
        }
    }

    final override fun getMeasurementResults(): MeasurementResults? = measurementResults

    override fun getMeasuredContentSize(): Size? = whenMeasured { contentSize }

    override fun getMeasuredSize(): Size? = whenMeasured { maxBoundingRectangle.size() }

    override fun getCurrentSize(): Size? = region.maxBoundingRectangle.size()

    final override fun getActiveRectangle(): BoundingRectangle = region.activeRectangle

    final override fun getMaxBoundingRectangle(): BoundingRectangle = region.maxBoundingRectangle

    final override fun getBoundingRectangle(type: BoundaryType?): BoundingRectangle =
        when (type) {
            BoundaryType.MARGIN -> region.maxBoundingRectangle
            BoundaryType.BORDER -> region.borderBoundingRectangle
            BoundaryType.PADDING -> region.paddingBoundingRectangle
            BoundaryType.CONTENT -> region.contentBoundingRectangle
            else -> region.activeRectangle
        }

    final override fun getExplicitWidth(type: BoundaryType): Width? =
        if (properties.declaredWidth) {
            getBoundingRectangle(type).getWidth()
        } else null

    final override fun getExplicitHeight(type: BoundaryType): Height? =
        if (properties.declaredHeight) {
            getBoundingRectangle(type).getHeight()
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
    fun <R> whenMeasured(block: Region.() -> R): R? = if (isMeasured) block(region) else null

    final override fun allocateSpace(position: Position) {
        region.allocate(position)
    }

    final override fun allocateRectangle(bbox: RenderableBoundingBox) = with(bbox) {
        allocateSpace(Position(absoluteX + width.orZero(), absoluteY + height.orZero()))
    }

    /**
     * Absorbs the layout of a child layout into the current layout.
     *
     * This method is responsible for integrating the bounding rectangle of a child layout into the current layout.
     * It updates the space allocation of the current layout to include the space occupied by the child layout.
     * It is important to note that child layout margin-level rectangle is used to determine the space allocation.
     * @param layout The child layout to be absorbed.
     */
    final override fun absorb(layout: Layout) {
        val boundingRectangle = layout.getMaxBoundingRectangle()
        allocateSpace(boundingRectangle.rightBottom)
        onChildLayoutAbsorbed(boundingRectangle)
    }

    /**
     * Hook method called after a child layout has been absorbed.
     *
     * This method can be overridden by subclasses to perform additional actions after a child layout has been absorbed.
     *
     * @param boundingRectangle The bounding rectangle of the absorbed child layout.
     */
    open fun onChildLayoutAbsorbed(boundingRectangle: BoundingRectangle) {}

    final override fun getRenderableBoundingBox(
        x: X,
        y: Y,
        width: Width?,
        height: Height?,
        type: BoundaryType
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
        if (region.borderLeftTop.x == region.contentCursorPosition.x) 0F else properties.horizontalSpacing

    fun getVerticalSpacing(): Float =
        if (region.borderLeftTop.y == region.contentCursorPosition.y) 0F else properties.verticalSpacing

    override fun toString(): String =
        "${this::class.simpleName}, $measurementResults OUTER[${region.maxBoundingRectangle}],BORDER[${region.borderBoundingRectangle}],PADDING[${region.paddingBoundingRectangle}],CONTENT[${region.contentBoundingRectangle}]"

}

class Region(
    @JvmSynthetic
    internal var leftTop: Position,
    @JvmSynthetic
    internal var maxRightBottom: Position,
    @JvmSynthetic
    internal var borderLeftTop: Position = leftTop,
    @JvmSynthetic
    internal var borderRightBottom: Position = maxRightBottom,
    @JvmSynthetic
    internal var paddingLeftTop: Position = borderLeftTop,
    @JvmSynthetic
    internal var paddingRightBottom: Position = borderRightBottom,
    @JvmSynthetic
    internal var contentLeftTop: Position = paddingLeftTop,
    @JvmSynthetic
    internal var contentRightBottom: Position = paddingRightBottom,
    val id: String = UUID.randomUUID().toString()
) {
    @JvmSynthetic
    internal var contentCursorPosition: Position = contentLeftTop

    val uom: UnitsOfMeasure = UnitsOfMeasure.PT

    // Below there are vectors representing the difference between the boundaries of the layout region.
    // They are immutable and are used to calculate the position of the next element within the layout
    // specifically while changing position of the region.
    private val marginToBorderLeftTopVector: Size = (borderLeftTop - leftTop).asSize()
    private val borderToPaddingLeftTopVector: Size = (paddingLeftTop - borderLeftTop).asSize()
    private val contentToPaddingLeftTopVector: Size = (contentLeftTop - paddingLeftTop).asSize()
    private val marginToBorderRightBottomVector: Size = (borderRightBottom - maxRightBottom).asSize()
    private val borderToPaddingRightBottomVector: Size = (borderRightBottom - paddingRightBottom).asSize()
    private val contentToPaddingRightBottomVector: Size = (paddingRightBottom - contentRightBottom).asSize()

    val activeRectangle: BoundingRectangle
        get() = BoundingRectangle(contentLeftTop, contentCursorPosition)

    val maxBoundingRectangle: BoundingRectangle
        get() = BoundingRectangle(leftTop, maxRightBottom)

    val borderBoundingRectangle: BoundingRectangle
        get() = BoundingRectangle(borderLeftTop, borderRightBottom)

    val paddingBoundingRectangle: BoundingRectangle
        get() = BoundingRectangle(paddingLeftTop, paddingRightBottom)

    val contentBoundingRectangle: BoundingRectangle
        get() = BoundingRectangle(contentLeftTop, contentRightBottom)

    /**
     * Updates the current position within the layout region.
     * The new position is determined by taking the minimum of:
     * 1. The maximum of the current position and the provided position.
     * 2. The content's right-bottom boundary.
     *
     * This ensures that the current position does not exceed the content's boundaries.
     *
     * @param position The new position to be allocated within the layout.
     */
    @JvmSynthetic
    internal fun allocate(position: Position) {
        contentCursorPosition = orMin(orMax(contentCursorPosition, position), contentRightBottom)
    }

    @JvmSynthetic
    internal fun close(position: Position, mode: BoundaryType = BoundaryType.MARGIN) {
        when (mode) {
            BoundaryType.MARGIN -> {
                maxRightBottom = position
                borderRightBottom = maxRightBottom - marginToBorderRightBottomVector
                paddingRightBottom = borderRightBottom - borderToPaddingRightBottomVector
                contentRightBottom = paddingRightBottom - contentToPaddingRightBottomVector
            }

            BoundaryType.CONTENT -> {
                contentRightBottom = position
                paddingRightBottom = contentRightBottom + contentToPaddingRightBottomVector
                borderRightBottom = paddingRightBottom + borderToPaddingRightBottomVector
                maxRightBottom = borderRightBottom + marginToBorderRightBottomVector
            }

            else -> error("Not implemented")
        }
        contentCursorPosition = contentRightBottom
    }

    @JvmSynthetic
    internal fun reset(position: Position? = null) {
        position?.let {
            val prevMaxBoundingRectangleSize = maxBoundingRectangle.size()
            leftTop = it
            maxRightBottom = it + prevMaxBoundingRectangleSize
            borderLeftTop = leftTop + marginToBorderLeftTopVector
            borderRightBottom = maxRightBottom - marginToBorderRightBottomVector
            paddingLeftTop = borderLeftTop + borderToPaddingLeftTopVector
            paddingRightBottom = borderRightBottom - borderToPaddingRightBottomVector
            contentLeftTop = paddingLeftTop + contentToPaddingLeftTopVector
            contentRightBottom = paddingRightBottom - contentToPaddingRightBottomVector
        }.also { contentCursorPosition = contentLeftTop }
    }

    override fun toString(): String {
        return "OUTER [$maxBoundingRectangle] INNER: [$borderBoundingRectangle]"
    }

    companion object {
        operator fun invoke(constraints: RegionConstraints): Region {
            requireNotNull(constraints.leftTop)
            requireNotNull(constraints.maxRightBottom)
            val borderLeftTop = constraints.borderLeftTop ?: constraints.leftTop
            val borderRightBottom = constraints.borderRightBottom ?: constraints.maxRightBottom
            val paddingLeftTop = constraints.paddingLeftTop ?: borderLeftTop
            val paddingRightBottom = constraints.paddingRightBottom ?: borderRightBottom
            val contentLeftTop = constraints.contentLeftTop ?: paddingLeftTop
            val contentRightBottom = constraints.contentRightBottom ?: paddingRightBottom
            return Region(
                constraints.leftTop, constraints.maxRightBottom,
                borderLeftTop, borderRightBottom,
                paddingLeftTop, paddingRightBottom,
                contentLeftTop, contentRightBottom
            )
        }
    }
}

interface LayoutElement<L : Layout> {

    val boundaryToFit: BoundaryType

    fun L.defineBoundingBox(): RenderableBoundingBox

}

fun interface MeasureLayoutElement<L : Layout> {
    fun measureAt(layout: L): Position
}

interface ApplyLayoutElement<L : Layout> {
    fun L.absorbRenderableBoundingBox(bbox: RenderableBoundingBox, status: RenderingStatus = Ok)
}

// TODO layoutPosition -> minLeftTop ; maxWidth+maxHeight -> maxRightBottom  (absolute boundaries of this renderable bounding box enforced by a layout in which context a renderable is being rendered.)

data class RenderableBoundingBox internal constructor(
    val cropBoxLeftTop: Position,
    val cropBoxRightBottom: Position,
    val absoluteX: X,
    val absoluteY: Y,
    var width: Width? = null,
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

    fun convertUnits(child: Layout, type: BoundaryType, parent: Layout?): RenderableBoundingBox =
        (if (type == BoundaryType.MARGIN) child.getMaxBoundingRectangle() else child.getBorderRectangle()).let { bbox ->
            val parentRect = parent?.getBorderRectangle() ?: bbox
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

    private fun isXCrossed(): Boolean {
        val rightBottomX = absoluteX + width.orZero()
        return (rightBottomX > cropBoxRightBottom.x).also {
            if (it) trace("X overflow detected: MAX.X: ${cropBoxRightBottom.x - rightBottomX}")
        }
    }

    private fun isYCrossed(): Boolean {
        val rightBottomY = absoluteY + height.orZero()
        return (rightBottomY > cropBoxRightBottom.y).also {
            if (it) trace("Y overflow detected: ${cropBoxRightBottom.y - rightBottomY}")
        }
    }

    fun isCrossingBounds(): Axis? =
        if (isXCrossed()) {
            Axis.X
        } else if (isYCrossed()) {
            Axis.Y
        } else null

}

fun RenderableBoundingBox?.isDefined() = this?.isDefined() ?: false

enum class Axis {
    X,
    Y
}

data class RegionConstraints(
    val leftTop: Position? = null,
    val maxRightBottom: Position? = null,
    val borderLeftTop: Position? = null,
    val borderRightBottom: Position? = null,
    val paddingLeftTop: Position? = null,
    val paddingRightBottom: Position? = null,
    val contentLeftTop: Position? = null,
    val contentRightBottom: Position? = null,
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


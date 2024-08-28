package io.github.voytech.tabulate.core

import io.github.voytech.tabulate.core.layout.*
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.model.alignment.orDefault
import io.github.voytech.tabulate.core.model.attributes.*


data class RelatedLayouts(val layout: Layout, val parent: Layout?) {

    fun endLayout() {
        layout.setMeasured()
    }

    fun getMaxBoundingRectangle(): BoundingRectangle = layout.getMaxBoundingRectangle()


    fun allocateSpace(position: Position) {
        layout.allocateSpace(position)
    }

    fun isMeasured() = layout.isMeasured

}

/**
 * Wrapper for layout context, which is responsible for managing layout data and calculations that are associated with
 * particular model export context. It creates scope for external rendering operations to be executed in.
 * It is ModelExportContext extension by composition thus can navigate through the model tree and access parent context
 * in order to resolve layout constraints for new or restarted layouts.
 * LayoutData is created usually during measuring phase. Layout can be also restarted at different position on rendering phase
 * e.g. when enclosing parent layout was previously repositioned.
 */
class ModelContextLayout(private val context: ModelExportContext) { // TODO make this class extending interface ContextScope
    lateinit var layout: Layout
        private set

    private fun uom(): UnitsOfMeasure = context.instance.uom

    private fun getParent(): ModelContextLayout? = context.parent()?.activeLayoutContext

    private fun getParentLayout(): Layout? = getParent()?.layout

    fun pairWithParent(): RelatedLayouts = RelatedLayouts(layout, getParentLayout())

    fun getMaxSize(): Size = layout.getMaxBoundingRectangle().size()

    private fun createLayout(box: RegionConstraints) {
        val (spaceConstraints, layoutProperties) = newLayoutConstraints(box)
        requireNotNull(spaceConstraints.leftTop)
        layout = context.model.resolveLayout(layoutProperties)
        layout.initialize(spaceConstraints)
    }

    fun beginLayout(constraints: RegionConstraints) {
        if (!this::layout.isInitialized) {
            createLayout(constraints)
        } else if (layout.isMeasured) {
            layout.reset(constraints.resolveEffectiveLeftTop(getParent()).leftTop)
        }
    }

    fun endLayout() {
        layout.setMeasured()
        getParentLayout()?.absorb(layout)
    }

    private fun nextItemPosition(): Position? = (layout as? SequentialLayout)?.run { resolveNextPosition() }

    private fun newLayoutConstraints(box: RegionConstraints): SpaceAndLayoutProperties =
        getParent().let {
            withMaxRightBottomResolved(
                it?.layout,
                box.resolveEffectiveLeftTop(it).applyLeftTopBoxModel()
            )
        }

    private fun RegionConstraints.resolveEffectiveLeftTop(parent: ModelContextLayout?): RegionConstraints {
        val parentContentLeftTop = parent?.layout?.getContentRectangle()?.leftTop
        return copy(leftTop = leftTop ?: parent?.nextItemPosition() ?: parentContentLeftTop ?: Position.start(uom()))
    }

    private fun withMaxRightBottomResolved(
        parent: Layout?, constraints: RegionConstraints
    ): SpaceAndLayoutProperties {
        requireNotNull(constraints.leftTop)
        val implicitMaxRightBottom =
            constraints.maxRightBottom ?: parent?.getContentRectangle()?.rightBottom
            ?: context.instance.getDocumentMaxRightBottom()
        val explicitWidth = getExplicitWidth(parent)
        val explicitHeight = getExplicitHeight(parent)
        return SpaceAndLayoutProperties(
            space = constraints.copy(
                maxRightBottom = Position(
                    x = explicitWidth?.let {
                        (constraints.leftTop.x + it).coerceAtMost(implicitMaxRightBottom.x)
                    } ?: implicitMaxRightBottom.x,
                    y = explicitHeight?.let {
                        (constraints.leftTop.y + it).coerceAtMost(implicitMaxRightBottom.y)
                    } ?: implicitMaxRightBottom.y
                )
            ).applyRightBottomBoxModel(),
            layout = LayoutProperties(
                declaredWidth = explicitWidth != null,
                declaredHeight = explicitHeight != null,
            )
        )
    }

    private fun RegionConstraints.applyLeftTopBoxModel(): RegionConstraints =
        applyLeftTopMarginOffsets()
            .applyLeftTopBorderOffsets()
            .applyLeftTopPaddingOffsets()

    private fun RegionConstraints.applyRightBottomBoxModel(): RegionConstraints =
        applyRightBottomMarginOffsets()
            .applyRightBottomBorderOffsets()
            .applyRightBottomPaddingOffsets()

    private fun RegionConstraints.applyLeftTopMarginOffsets(): RegionConstraints =
        context.getMarginOffsets().let {
            requireNotNull(leftTop)
            if (borderLeftTop == null) {
                copy(borderLeftTop = leftTop + Size(it.left, it.top))
            } else this
        }

    private fun RegionConstraints.applyRightBottomMarginOffsets(): RegionConstraints =
        context.getMarginOffsets().let {
            requireNotNull(maxRightBottom)
            if (borderRightBottom == null) {
                copy(borderRightBottom = maxRightBottom - Size(it.right, it.bottom))
            } else this
        }

    private fun RegionConstraints.applyLeftTopBorderOffsets(): RegionConstraints =
        context.getBorderOffsets().let {
            requireNotNull(borderLeftTop)
            if (paddingLeftTop == null) {
                copy(paddingLeftTop = borderLeftTop + Size(it.left, it.top))
            } else this
        }

    private fun RegionConstraints.applyRightBottomBorderOffsets(): RegionConstraints =
        context.getBorderOffsets().let {
            requireNotNull(borderRightBottom)
            if (paddingRightBottom == null) {
                copy(paddingRightBottom = borderRightBottom - Size(it.right, it.bottom))
            } else this
        }

    private fun RegionConstraints.applyLeftTopPaddingOffsets(): RegionConstraints =
        context.getPaddingOffsets().let {
            requireNotNull(paddingLeftTop)
            if (contentLeftTop == null) {
                copy(contentLeftTop = paddingLeftTop + Size(it.left, it.top))
            } else this
        }

    private fun RegionConstraints.applyRightBottomPaddingOffsets(): RegionConstraints =
        context.getPaddingOffsets().let {
            requireNotNull(paddingRightBottom)
            if (contentRightBottom == null) {
                copy(contentRightBottom = paddingRightBottom - Size(it.right, it.bottom))
            } else this
        }

    private data class SpaceAndLayoutProperties(
        val space: RegionConstraints,
        val layout: LayoutProperties = LayoutProperties()
    )

    private fun getExplicitHeight(layout: Layout?): Height? =
        lookupAttribute(HeightAttribute::class.java)?.switchUnitOfMeasure(uom(), layout)

    private fun getExplicitWidth(layout: Layout?): Width? =
        lookupAttribute(WidthAttribute::class.java)?.switchUnitOfMeasure(uom(), layout)

    private fun HeightAttribute.switchUnitOfMeasure(uom: UnitsOfMeasure, parent: Layout?): Height =
        value.switchUnitOfMeasure(uom, parent?.getContentRectangle()?.getHeight())

    private fun WidthAttribute.switchUnitOfMeasure(uom: UnitsOfMeasure, parent: Layout?): Width =
        value.switchUnitOfMeasure(uom, parent?.getContentRectangle()?.getWidth())

    private fun <A : Attribute<A>> lookupAttribute(attribute: Class<A>): A? =
        (context.model as? AttributedModelOrPart)?.attributes?.forContext(context.model.javaClass)?.get(attribute)

    internal fun debugInfo(): String = if (this::layout.isInitialized) {
        "$layout"
    } else {
        "?"
    }

}

fun Position.align(attribute: AlignmentAttribute, parentSize: Size, thisSize: Size): Position = Position(
    x = x.align(attribute.horizontal.orDefault(), parentSize.width, thisSize.width),
    y = y.align(attribute.vertical.orDefault(), parentSize.height, thisSize.height)
)

data class Padding(
    val left: Width,
    val top: Height,
    val right: Width,
    val bottom: Height,
) {
    operator fun plus(other: Padding): Padding = copy(
        left = left + other.left,
        top = top + other.top,
        right = right + other.right,
        bottom = bottom + other.bottom
    )
}

inline fun <reified A : Attribute<A>> Model.getAttribute(): A? =
    (this as? AttributedModelOrPart)?.attributes?.forContext(javaClass)?.get(A::class.java)


fun ModelExportContext.getMarginOffsets(): Padding {
    val margins = model.getAttribute<MarginsAttribute>()
    return Padding(
        left = margins?.left?.asWidth().orZero(),
        right = Width.zero(),
        top = margins?.top?.asHeight().orZero(),
        bottom = Height.zero(),
    )
}

fun ModelExportContext.getBorderOffsets(): Padding {
    val borders = model.getAttribute<BordersAttribute>()
    return Padding(
        left = borders?.leftBorderWidth.orZero(),
        right = borders?.rightBorderWidth.orZero(),
        top = borders?.topBorderHeight.orZero(),
        bottom = borders?.bottomBorderHeight.orZero(),
    )
}

fun ModelExportContext.getPaddingOffsets(): Padding =
    Padding(
        left = Width.zero(),
        top = Height.zero(),
        right = Width.zero(),
        bottom = Height.zero()
    )

// TODO add getPaddingOffsets() when PaddingAttribute implemented.
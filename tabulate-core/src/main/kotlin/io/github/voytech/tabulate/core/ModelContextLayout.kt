package io.github.voytech.tabulate.core

import io.github.voytech.tabulate.core.layout.*
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.model.alignment.orDefault
import io.github.voytech.tabulate.core.model.attributes.*


data class LayoutData(val region: Region, val parent: Region?, val layout: Layout) {

    fun endLayout() = with(layout) {
        region.setMeasured()
    }

    fun getMaxBoundingRectangle(): BoundingRectangle = with(layout) {
        region.getMaxBoundingRectangle()
    }

    fun applyChildRectangle(maxBoundingRectangle: BoundingRectangle) {
        layout.applyAllocatedRectangle(maxBoundingRectangle)
    }

    fun allocateSpace(position: Position) = with(layout) {
        region.allocateSpace(position)
    }

    fun isMeasured() = layout.isMeasured

}

class ModelContextLayout(private val context: ModelExportContext) {
    lateinit var layout: LayoutData
        private set

    private fun uom(): UnitsOfMeasure = context.instance.uom

    private fun getCurrentLayoutSpace(): Region = layout.region

    private fun getParentLayout(): ModelContextLayout? = context.parent()?.activeLayoutContext

    fun getMaxSize(): Size = getCurrentLayoutSpace().let {
        (it.maxRightBottom - it.leftTop).asSize()
    }

    private fun createLayout(box: RegionConstraints) {
        val (spaceConstraints, layoutProperties) = newLayoutConstraints(box)
        requireNotNull(spaceConstraints.leftTop)
        layout = LayoutData(
            Region(uom(), spaceConstraints),
            getParentLayout()?.layout?.region,
            context.model.resolveLayout(layoutProperties)
        )
    }

    fun beginLayout(constraints: RegionConstraints) {
        if (!this::layout.isInitialized) {
            createLayout(constraints)
        } else if (layout.layout.isMeasured) {
            layout.region.restart(constraints.resolveEffectiveLeftTop(getParentLayout()).leftTop)
        }
    }

    fun endLayout() {
        layout.endLayout()
        updateUpstream()
    }

    private fun updateUpstream() {
        val current = layout
        val upstream = getParentLayout()?.layout
        if (upstream != null) {
            upstream.allocateSpace(current.region.maxRightBottom)
            upstream.applyChildRectangle(current.getMaxBoundingRectangle())
        }
    }

    private fun nextNodePosition(): Position? = layout.let {
        (it.layout as? AutonomousLayout)?.run {
            it.region.resolveNextPosition()
        }
    }

    private fun RegionConstraints.resolveEffectiveLeftTop(parent: ModelContextLayout?): RegionConstraints {
        val parentSpace = parent?.getCurrentLayoutSpace()
        return copy(
            leftTop = leftTop ?: parent?.nextNodePosition() ?: parentSpace?.innerLeftTop ?: Position.start(uom())
        ).withMargins()
    }

    private fun newLayoutConstraints(box: RegionConstraints): SpaceAndLayoutProperties =
        getParentLayout().let {
            withMaxRightBottomResolved(
                it?.getCurrentLayoutSpace(),
                box.resolveEffectiveLeftTop(it).withInnerLeftTop()
            )
        }

    private fun RegionConstraints.withInnerLeftTop(): RegionConstraints =
        context.padding()?.let {
            requireNotNull(leftTop)
            if (innerLeftTop == null) {
                copy(innerLeftTop = leftTop + Size(it.left, it.top))
            } else this
        } ?: copy(innerLeftTop = leftTop)

    private fun RegionConstraints.withInnerMaxRightBottom(): RegionConstraints =
        context.padding()?.let {
            requireNotNull(maxRightBottom)
            if (innerMaxRightBottom == null) {
                copy(innerMaxRightBottom = maxRightBottom - Size(it.right, it.bottom))
            } else this
        } ?: copy(innerMaxRightBottom = maxRightBottom)

    private fun RegionConstraints.withMargins(): RegionConstraints =
        lookupAttribute(MarginsAttribute::class.java)?.let {
            requireNotNull(leftTop)
            copy(leftTop = Position(it.left + leftTop.x, it.top + leftTop.y))
        } ?: this

    private data class SpaceAndLayoutProperties(
        val space: RegionConstraints,
        val layout: LayoutProperties = LayoutProperties()
    )

    private fun withMaxRightBottomResolved(
        parent: Region?, constraints: RegionConstraints
    ): SpaceAndLayoutProperties {
        requireNotNull(constraints.leftTop)
        val implicitMaxRightBottom =
            constraints.maxRightBottom ?: parent?.innerMaxRightBottom ?: context.instance.getDocumentMaxRightBottom()
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
            ).withInnerMaxRightBottom(),
            layout = LayoutProperties(
                declaredWidth = explicitWidth != null,
                declaredHeight = explicitHeight != null,
            )
        )
    }

    private fun getExplicitHeight(layout: Region?): Height? =
        lookupAttribute(HeightAttribute::class.java)?.switchUnitOfMeasure(uom(), layout)

    private fun getExplicitWidth(layout: Region?): Width? =
        lookupAttribute(WidthAttribute::class.java)?.switchUnitOfMeasure(uom(), layout)

    private fun HeightAttribute.switchUnitOfMeasure(uom: UnitsOfMeasure, layout: Region?): Height =
        value.switchUnitOfMeasure(uom, layout?.innerBoundingRectangle?.getHeight())

    private fun WidthAttribute.switchUnitOfMeasure(uom: UnitsOfMeasure, layout: Region?): Width =
        value.switchUnitOfMeasure(uom, layout?.innerBoundingRectangle?.getWidth())

    private fun <A : Attribute<A>> lookupAttribute(attribute: Class<A>): A? =
        (context.model as? AttributedModelOrPart)?.attributes?.forContext(context.model.javaClass)?.get(attribute)

    internal fun debugInfo(): String = if (this::layout.isInitialized) {
        "${layout.region}"
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

fun ModelExportContext.padding(): Padding? =
    (model as? AttributedModelOrPart)
        ?.attributes
        ?.forContext(model.javaClass)
        ?.get<BordersAttribute>()?.let {
            Padding(
                left = it.leftBorderWidth,
                right = it.rightBorderWidth,
                bottom = it.bottomBorderHeight,
                top = it.topBorderHeight
            )
        }
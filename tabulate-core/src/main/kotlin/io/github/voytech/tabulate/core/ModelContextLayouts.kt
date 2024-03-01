package io.github.voytech.tabulate.core

import io.github.voytech.tabulate.MultiIterationSet
import io.github.voytech.tabulate.core.layout.*
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.model.alignment.orDefault
import io.github.voytech.tabulate.core.model.attributes.*
import io.github.voytech.tabulate.plusAssign


data class LayoutApi(val space: LayoutSpace, val layout: NavigableLayout) {
    @Suppress("UNCHECKED_CAST")
    fun <P : Layout> layout(): P = layout.delegate as P

    fun <P : Layout, R> layout(block: P.(LayoutSpace) -> R) =
        block(layout(), space)

    fun parentLayoutSpace(): LayoutSpace? = layout.getParentScope()?.space

    fun close() = with(layout) {
        space.setMeasured()
        space.finish()
    }
}

class NavigableLayout(
    private val layouts: ModelContextLayouts,
    @JvmSynthetic
    internal val delegate: Layout
) : Layout by delegate {

    @JvmSynthetic
    internal fun getParentScope() = layouts.getParentScope()

    private fun onParentScope(block: (LayoutApi) -> Unit) = getParentScope()?.let(block)

    private fun reserveSpaceOnParent(position: Position) { // TODO think if we need to expand parent after each renderable ? for some cases like table layouts with column/row auto-sizing this does not allow to reliably compute size of render space (it must be recomputed after full measuring). For now eager layout expanding on each renderable seems to be redundant step.
        onParentScope { parent ->
            with(parent.layout) {
                parent.space.allocateSpace(position)
            }
        }
    }

    override fun LayoutSpace.allocateSpace(position: Position) {
        reserveSpaceOnParent(position)
        with(delegate) { allocateSpace(position) }
    }

    override fun LayoutSpace.allocateRectangle(bbox: RenderableBoundingBox) = with(bbox) {
        allocateSpace(Position(absoluteX + width.orZero(), absoluteY + height.orZero()))
    }

    override fun LayoutSpace.setMeasured() = with(delegate) {
        setMeasured()
        reserveSpaceOnParent(maxRightBottom)
    }

    internal fun LayoutSpace.finish() = onParentScope {
        it.layout.delegate.applyChildRectangle(getMaxBoundingRectangle())
    }

}

class ModelContextLayouts(
    @JvmSynthetic
    internal val context: ModelExportContext,
    private val layouts: MultiIterationSet<LayoutApi, ExportPhase> = MultiIterationSet(),
) {

    private fun uom(): UnitsOfMeasure = context.instance.uom

    private fun lastScope(): LayoutApi? = layouts.lastOrNull()

    fun last(): LayoutSpace? = lastScope()?.space

    fun currentOrNull(phase: ExportPhase? = null): LayoutApi? = layouts.currentOrNull(phase ?: context.phase)

    fun current(phase: ExportPhase? = null): LayoutApi = layouts.current(phase ?: context.phase)

    fun currentIndex(phase: ExportPhase? = null): Int = layouts.currentIndex(phase ?: context.phase)

    private fun next(phase: ExportPhase? = null): LayoutApi = layouts.next(phase ?: context.phase)

    fun clear() {
        layouts.clear()
        layouts.reset(ExportPhase.MEASURING)
        layouts.reset(ExportPhase.RENDERING)
    }

    @JvmSynthetic
    internal fun needsMeasuring(): Boolean = with(layouts) { ExportPhase.MEASURING isAfter ExportPhase.RENDERING }

    fun getParentScope(): LayoutApi? = context.navigate {
        val phase = context.phase
        lookupAncestors { it.layouts.currentOrNull(phase) != null }
            ?.layouts?.currentOrNull(phase)
    }

    private fun getCurrentLayoutSpace(): LayoutSpace? = currentOrNull()?.space

    private fun getParentLayouts(): ModelContextLayouts? = context.navigate {
        lookupAncestors { it.layouts.last() != null }?.layouts
    }

    fun getMaxSize(): Size? = currentOrNull()?.space?.let {
        (it.maxRightBottom - it.leftTop).asSize()
    }

    private fun nextNodePosition(): Position? = currentOrNull()?.let {
        (it.layout.delegate as? AutonomousLayout)?.run {
            it.space.resolveNextPosition()
        }
    }

    @JvmSynthetic
    internal fun createLayout(box: SpaceConstraints): LayoutApi {
        val (spaceConstraints, layoutProperties) = newLayoutConstraints(box)
        requireNotNull(spaceConstraints.leftTop)
        layouts += LayoutApi(
            LayoutSpace(uom(), spaceConstraints),
            NavigableLayout(this, context.model.resolveLayout(layoutProperties))
        )
        return next()
    }

    private fun shouldCreateLayout(): Boolean = currentIndex() + 1 == layouts.size()

    private fun <R> requireNextMeasuredLayout(block: LayoutApi.() -> R): R = next().run {
        if (layout.isSpaceMeasured) {
            block(this@run)
        } else error("next layout needs to be measured.")
    }

    @JvmSynthetic
    internal fun <R> ensuringNextLayout(constraints: SpaceConstraints, block: LayoutApi.() -> R): R =
        if (shouldCreateLayout()) {
            createLayout(constraints).run {
                block(this).also { close() }
            }
        } else {
            requireNextMeasuredLayout {
                space.restart(constraints.resolveEffectiveLeftTop(getParentLayouts()).leftTop)
                block(this).also { close() }
            }
        }

    private fun SpaceConstraints.resolveEffectiveLeftTop(parent: ModelContextLayouts?): SpaceConstraints {
        val parentSpace = parent?.getCurrentLayoutSpace()
        return copy(
            leftTop = leftTop ?: parent?.nextNodePosition() ?: parentSpace?.innerLeftTop ?: Position.start(uom())
        ).withMargins()
    }

    private fun newLayoutConstraints(box: SpaceConstraints): SpaceAndLayoutProperties =
        getParentLayouts().let {
            withMaxRightBottomResolved(
                it?.getCurrentLayoutSpace(),
                box.resolveEffectiveLeftTop(it).withInnerLeftTop()
            )
        }

    private fun SpaceConstraints.withInnerLeftTop(): SpaceConstraints =
        context.padding()?.let {
            requireNotNull(leftTop)
            if (innerLeftTop == null) {
                copy(innerLeftTop = leftTop + Size(it.left, it.top))
            } else this
        } ?: copy(innerLeftTop = leftTop)

    private fun SpaceConstraints.withInnerMaxRightBottom(): SpaceConstraints =
        context.padding()?.let {
            requireNotNull(maxRightBottom)
            if (innerMaxRightBottom == null) {
                copy(innerMaxRightBottom = maxRightBottom - Size(it.right, it.bottom))
            } else this
        } ?: copy(innerMaxRightBottom = maxRightBottom)

    private fun SpaceConstraints.withMargins(): SpaceConstraints = lookupAttribute(MarginsAttribute::class.java)?.let {
        requireNotNull(leftTop)
        copy(leftTop = Position(it.left + leftTop.x, it.top + leftTop.y))
    } ?: this

    private data class SpaceAndLayoutProperties(
        val space: SpaceConstraints,
        val layout: LayoutProperties = LayoutProperties()
    )

    private fun withMaxRightBottomResolved(
        parent: LayoutSpace?, constraints: SpaceConstraints
    ): SpaceAndLayoutProperties {
        requireNotNull(constraints.leftTop)
        val implicitMaxRightBottom =
            constraints.maxRightBottom ?: parent?.innerMaxRightBottom ?: context.instance.getDocumentMaxRightBottom()
        val explicitWidth = getExplicitWidth(parent)
        val explicitHeight = getExplicitHeight(parent)
        return SpaceAndLayoutProperties(
            space = constraints.copy(
                maxRightBottom = Position(
                    x = explicitWidth?.let { constraints.leftTop.x + it } ?: implicitMaxRightBottom.x,
                    y = explicitHeight?.let { constraints.leftTop.y + it } ?: implicitMaxRightBottom.y
                )
            ).withInnerMaxRightBottom(),
            layout = LayoutProperties(
                declaredWidth = explicitWidth != null,
                declaredHeight = explicitHeight != null,
            )
        )
    }

    private fun getExplicitHeight(layout: LayoutSpace?): Height? =
        lookupAttribute(HeightAttribute::class.java)?.switchUnitOfMeasure(uom(), layout)

    private fun getExplicitWidth(layout: LayoutSpace?): Width? =
        lookupAttribute(WidthAttribute::class.java)?.switchUnitOfMeasure(uom(), layout)

    private fun HeightAttribute.switchUnitOfMeasure(uom: UnitsOfMeasure, layout: LayoutSpace?): Height =
        value.switchUnitOfMeasure(uom, layout?.innerBoundingRectangle?.getHeight())

    private fun WidthAttribute.switchUnitOfMeasure(uom: UnitsOfMeasure, layout: LayoutSpace?): Width =
        value.switchUnitOfMeasure(uom, layout?.innerBoundingRectangle?.getWidth())

    private fun <A : Attribute<A>> lookupAttribute(attribute: Class<A>): A? =
        (context.model as? AttributedModelOrPart)?.attributes?.forContext(context.model.javaClass)?.get(attribute)

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
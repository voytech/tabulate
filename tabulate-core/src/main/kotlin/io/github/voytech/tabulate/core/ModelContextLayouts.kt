package io.github.voytech.tabulate.core

import io.github.voytech.tabulate.MultiIterationSet
import io.github.voytech.tabulate.core.layout.*
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.model.alignment.orDefault
import io.github.voytech.tabulate.core.model.attributes.AlignmentAttribute
import io.github.voytech.tabulate.core.model.attributes.HeightAttribute
import io.github.voytech.tabulate.core.model.attributes.MarginsAttribute
import io.github.voytech.tabulate.core.model.attributes.WidthAttribute
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

    private fun expandParent(position: Position) { // TODO think if we need to expand parent after each renderable ? for some cases like table layouts with column/row auto-sizing this does not allow to reliably compute size of render space (it must be recomputed after full measuring). For now eager layout expanding on each renderable seems to be redundant step.
        onParentScope { parent ->
            with(parent.layout) {
                parent.space.expandLayout(position)
            }
        }
    }

    override fun LayoutSpace.expandLayout(position: Position) {
        expandParent(position)
        with(delegate) { expandLayout(position) }
    }

    override fun LayoutSpace.expandByRectangle(bbox: RenderableBoundingBox) = with(bbox) {
        expandLayout(Position(absoluteX + width.orZero(), absoluteY + height.orZero()))
    }

    override fun LayoutSpace.setMeasured() = with(delegate) {
        setMeasured()
        expandParent(rightBottom)
        //TODO The below rather does not make sense.
        /*layouts.context.whenMeasuring {
            if (getMeasurementResults()?.let { it.heightAligned || it.widthAligned } == true) {
                layouts.context.navigate {
                    traverseChildren { it.layouts.recalculateMaxRightBottom() }
                }
            }
        }*/
    }

    internal fun LayoutSpace.finish() = onParentScope {
        it.layout.delegate.handleChildExpansion(getMaxBoundingRectangle())
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

    fun getMaxSize(): Size? = currentOrNull()?.space?.let { space ->
        space.maxRightBottom?.let { maxRightBottom ->
            (maxRightBottom - space.leftTop).asSize()
        }
    }

    private fun nextNodePosition(): Position? = currentOrNull()?.let {
        (it.layout.delegate as? IterableLayout)?.run {
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
                space.restart(resolveEffectiveLeftTop(constraints))
                block(this).also { close() }
            }
        }

    private fun resolveEffectiveLeftTop(parent: ModelContextLayouts?, box: SpaceConstraints): Position {
        val parentSpace = parent?.getCurrentLayoutSpace()
        return (box.leftTop ?: parent?.nextNodePosition() ?: parentSpace?.leftTop
        ?: Position.start(uom())).withMargins()
    }

    private fun resolveEffectiveLeftTop(box: SpaceConstraints): Position =
        resolveEffectiveLeftTop(getParentLayouts(), box)

    private fun newLayoutConstraints(box: SpaceConstraints): SpaceAndLayoutProperties =
        getParentLayouts().let {
            withMaxRightBottomResolved(
                it?.getCurrentLayoutSpace(),
                box.copy(leftTop = resolveEffectiveLeftTop(it, box))
            )
        }

    private fun Position.withMargins(): Position = lookupAttribute(MarginsAttribute::class.java)?.let {
        Position(it.left + x, it.top + y)
    } ?: this

    private data class SpaceAndLayoutProperties(val space: SpaceConstraints, val layout: LayoutProperties)

    private fun withMaxRightBottomResolved(
        parent: LayoutSpace?, constraints: SpaceConstraints
    ): SpaceAndLayoutProperties =
        if (constraints.maxRightBottom == null) {
            requireNotNull(constraints.leftTop)
            val implicitMaxRightBottom = parent?.maxRightBottom ?: context.instance.getDocumentMaxRightBottom()
            val explicitWidth = getExplicitWidth(parent)
            val explicitHeight = getExplicitHeight(parent)
            SpaceAndLayoutProperties(
                space = constraints.copy(
                    maxRightBottom = Position(
                        x = explicitWidth?.let { constraints.leftTop.x + it } ?: implicitMaxRightBottom.x,
                        y = explicitHeight?.let { constraints.leftTop.y + it } ?: implicitMaxRightBottom.y
                    )
                ),
                layout = LayoutProperties(
                    fixedWidth = explicitWidth != null,
                    fixedHeight = explicitHeight != null,
                )
            )
        } else SpaceAndLayoutProperties(constraints, LayoutProperties(fixedHeight = true, fixedWidth = true))

    @JvmSynthetic
    internal fun recalculateMaxRightBottom() = context.whenMeasuring {
        val height = lookupAttribute(HeightAttribute::class.java)
        val width = lookupAttribute(WidthAttribute::class.java)
        val asPercentage = height?.value?.unit == UnitsOfMeasure.PC || width?.value?.unit == UnitsOfMeasure.PC
        if (asPercentage) {
            getParentLayouts()?.let { parent ->
                val recalculatedHeight = height?.switchUnitOfMeasure(uom(), parent.getCurrentLayoutSpace())
                val recalculatedWidth = width?.switchUnitOfMeasure(uom(), parent.getCurrentLayoutSpace())
                context.layouts.current().space.let { space ->
                    space.maxRightBottom?.let { maxRightBottom ->
                        space.maxRightBottom = Position(
                            recalculatedWidth?.let { space.leftTop.x + it } ?: maxRightBottom.x,
                            recalculatedHeight?.let { space.leftTop.y + it } ?: maxRightBottom.y
                        )
                    }
                }
            }
        }
    }

    private fun getExplicitHeight(layout: LayoutSpace?): Height? =
        lookupAttribute(HeightAttribute::class.java)?.switchUnitOfMeasure(uom(), layout)

    private fun getExplicitWidth(layout: LayoutSpace?): Width? =
        lookupAttribute(WidthAttribute::class.java)?.switchUnitOfMeasure(uom(), layout)

    private fun HeightAttribute.switchUnitOfMeasure(uom: UnitsOfMeasure, layout: LayoutSpace?): Height =
        value.switchUnitOfMeasure(uom, layout?.maxBoundingRectangle?.getHeight())

    private fun WidthAttribute.switchUnitOfMeasure(uom: UnitsOfMeasure, layout: LayoutSpace?): Width =
        value.switchUnitOfMeasure(uom, layout?.maxBoundingRectangle?.getWidth())

    private fun <A : Attribute<A>> lookupAttribute(attribute: Class<A>): A? =
        (context.model as? AttributedModelOrPart)?.attributes?.forContext(context.model.javaClass)?.get(attribute)

}

fun Position.align(attribute: AlignmentAttribute, parentSize: Size, thisSize: Size): Position = Position(
    x = x.align(attribute.horizontal.orDefault(), parentSize.width, thisSize.width),
    y = y.align(attribute.vertical.orDefault(), parentSize.height, thisSize.height)
)

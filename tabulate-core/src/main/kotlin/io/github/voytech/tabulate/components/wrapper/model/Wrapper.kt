package io.github.voytech.tabulate.components.wrapper.model

import io.github.voytech.tabulate.core.align
import io.github.voytech.tabulate.core.layout.LayoutProperties
import io.github.voytech.tabulate.core.layout.SpaceConstraints
import io.github.voytech.tabulate.core.layout.impl.SimpleLayout
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.model.attributes.AlignmentAttribute
import io.github.voytech.tabulate.core.model.attributes.MarginsAttribute

class Wrapper(
    override val attributes: Attributes?,
    @get:JvmSynthetic
    internal val child: AbstractModel,
) : ModelWithAttributes(), LayoutStrategy<SimpleLayout> {

    private val alignments by lazy {
        attributes?.get<AlignmentAttribute>()
    }
    override val needsMeasureBeforeExport: Boolean
        get() = false//alignments?.let { it.horizontal != null || it.vertical != null } ?: false

    override fun doExport(api: ExportApi) = api {
        withinCurrentLayout {
            alignments?.let { alignment ->
                child.currentSizeOrMeasure()?.let { childSize ->
                    it.maxBoundingRectangle?.size()?.let { size ->
                        val withMarginOrNot = getMarginSize(child)?.let { childSize + it } ?: childSize
                        child.export(SpaceConstraints(leftTop = it.leftTop.align(alignment, size, withMarginOrNot)))
                    }
                }
            } ?: child.export()
        }
    }

    private fun ExportApi.getMarginSize(model: AbstractModel): Size? = model.getAttribute<MarginsAttribute>()?.let {
        Size(it.left.asWidth(), it.top.asHeight())
    }

    override fun takeMeasures(api: ExportApi) = api {
        withinCurrentLayout {
            child.measure() // pre-measure all children if this method was invoked eagerly before rendering.
            it.maxRightBottom?.let { maxRightBottom -> it.reserveSpace(maxRightBottom) }
        }
    }

    override fun createLayout(properties: LayoutProperties): SimpleLayout = SimpleLayout(properties)

}
package io.github.voytech.tabulate.components.wrapper.model

import io.github.voytech.tabulate.core.align
import io.github.voytech.tabulate.core.layout.LayoutProperties
import io.github.voytech.tabulate.core.layout.RegionConstraints
import io.github.voytech.tabulate.core.layout.impl.SimpleLayout
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.model.attributes.AlignmentAttribute
import io.github.voytech.tabulate.core.model.attributes.MarginsAttribute
import java.util.*

class Wrapper(
    override val id: String = UUID.randomUUID().toString(),
    override val attributes: Attributes?,
    @get:JvmSynthetic
    internal val child: AbstractModel,
) : ModelWithAttributes(), HavingLayout<SimpleLayout> {

    private val alignments by lazy {
        attributes?.get<AlignmentAttribute>()
    }
    override val needsMeasureBeforeExport: Boolean
        get() = alignments?.let { it.horizontal != null || it.vertical != null } ?: false

    override fun doExport(api: ExportApi) = api {
        withinCurrentLayout {
            alignments?.let { alignment ->
                child.measure().let { childSize ->
                    it.maxBoundingRectangle.size().let { size ->
                        val withMarginOrNot = getMarginSize(child)?.let { childSize + it } ?: childSize
                        child.export(RegionConstraints(leftTop = it.leftTop.align(alignment, size, withMarginOrNot)))
                    }
                }
            } ?: child.export()
        }
    }

    private fun getMarginSize(model: AbstractModel): Size? = model.getAttribute<MarginsAttribute>()?.let {
        Size(it.left.asWidth(), it.top.asHeight())
    }

    override fun takeMeasures(api: ExportApi) = api {
        child.measure() // pre-measure all children if this method was invoked eagerly before rendering.
    }

    override fun createLayout(properties: LayoutProperties): SimpleLayout = SimpleLayout(properties)

}
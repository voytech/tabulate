package io.github.voytech.tabulate.components.wrapper.model

import io.github.voytech.tabulate.core.align
import io.github.voytech.tabulate.core.layout.LayoutProperties
import io.github.voytech.tabulate.core.layout.RegionConstraints
import io.github.voytech.tabulate.core.layout.impl.SimpleLayout
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.model.attributes.AlignmentAttribute
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

    override fun doExport(api: ExportApi): Unit = api {
        withinCurrentLayout {
            alignments?.let { alignment ->
                child.measure().let { childSize ->
                    val parentContent = getContentRectangle()
                    child.export(
                        RegionConstraints(
                            leftTop = parentContent.leftTop.align(alignment, parentContent.size(), childSize)
                        )
                    )
                }
            } ?: child.export()
        }
    }

    override fun takeMeasures(api: ExportApi): Unit = api {
        child.measure() // pre-measure all children if this method was invoked eagerly before rendering.
    }

    override fun createLayout(properties: LayoutProperties): SimpleLayout = SimpleLayout(properties)

}
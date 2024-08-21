package io.github.voytech.tabulate.components.container.model

import io.github.voytech.tabulate.components.container.opration.ContainerRenderable
import io.github.voytech.tabulate.core.layout.LayoutProperties
import io.github.voytech.tabulate.core.layout.impl.FlowLayout
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.model.attributes.BackgroundAttribute
import io.github.voytech.tabulate.core.model.attributes.BordersAttribute
import java.util.*

class Container(
    override val id: String = UUID.randomUUID().toString(),
    override val attributes: Attributes?,
    @get:JvmSynthetic
    internal val forcePreMeasure: Boolean,
    @get:JvmSynthetic
    internal val orientation: Orientation = Orientation.HORIZONTAL,
    @get:JvmSynthetic
    override val models: List<AbstractModel> = emptyList(),
    override val descendantsIterationsKind: DescendantsIterationsKind = DescendantsIterationsKind.POSTPONED,
) : AbstractContainerModelWithAttributes(), HavingLayout<FlowLayout> {

    override val needsMeasureBeforeExport = forcePreMeasure || hasRenderableAttributes()

    override fun doExport(api: ExportApi): Unit = api {
        if (hasRenderableAttributes()) {
            render(ContainerRenderable(attributes?.forContext<ContainerRenderable>(), api.getCustomAttributes()))
        }
        withinCurrentLayout { space ->
            space.reset()
            exportWithContinuations<FlowLayout>(models, descendantsIterationsKind)
        }
    }

    override fun takeMeasures(api: ExportApi) = api {
        withinCurrentLayout { space ->
            space.reset()
            measureWithContinuations<FlowLayout>(models, descendantsIterationsKind)
        }
    }

    private fun hasRenderableAttributes(): Boolean =
        attributes?.forContext<ContainerRenderable>()
            ?.let { it.has<BackgroundAttribute>() || it.has<BordersAttribute>() } ?: false

    override fun createLayout(properties: LayoutProperties): FlowLayout =
        FlowLayout(properties.copy(orientation = this.orientation))

}
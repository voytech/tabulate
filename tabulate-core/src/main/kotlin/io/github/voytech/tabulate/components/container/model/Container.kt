package io.github.voytech.tabulate.components.container.model

import io.github.voytech.tabulate.components.container.opration.ContainerRenderable
import io.github.voytech.tabulate.core.layout.LayoutProperties
import io.github.voytech.tabulate.core.layout.impl.FlowLayout
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.model.attributes.BackgroundAttribute
import io.github.voytech.tabulate.core.model.attributes.BordersAttribute

class Container(
    override val attributes: Attributes?,
    @get:JvmSynthetic
    internal val orientation: Orientation = Orientation.HORIZONTAL,
    @get:JvmSynthetic
    internal val models: List<AbstractModel> = emptyList(),
) : ModelWithAttributes(), LayoutProvider<FlowLayout> {

    override val needsMeasureBeforeExport = hasRenderableAttributes()

    override fun doExport(api: ExportApi): Unit = api {
        if (hasRenderableAttributes()) {
            render(ContainerRenderable(attributes?.forContext<ContainerRenderable>(), api.getCustomAttributes()))
        }
        exportOrMeasure {
            export()
        }
    }

    override fun takeMeasures(api: ExportApi) = api {
        exportOrMeasure { measure() }
    }

    private fun <R> ExportApi.exportOrMeasure(op: AbstractModel.() -> R) {
        withinCurrentLayout { space ->
            space.reset()
            models.forEach { nextModel ->
                while (nextModel.isRunning() && space.hasSpaceLeft()) { //TODO add continuations
                    nextModel.op()
                }
            }
        }
    }

    private fun hasRenderableAttributes(): Boolean =
        attributes?.forContext<ContainerRenderable>()
            ?.let { it.has<BackgroundAttribute>() || it.has<BordersAttribute>() } ?: false

    override fun createLayout(properties: LayoutProperties): FlowLayout =
        FlowLayout(properties.copy(orientation = this.orientation))

}
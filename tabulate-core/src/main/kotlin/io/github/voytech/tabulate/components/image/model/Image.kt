package io.github.voytech.tabulate.components.image.model

import io.github.voytech.tabulate.components.image.operation.ImageRenderable
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.ModelExportContext
import io.github.voytech.tabulate.core.model.ModelWithAttributes
import io.github.voytech.tabulate.core.model.orEmpty

class Image(
    @get:JvmSynthetic
    internal val filePath: String = "blank",
    override val attributes: Attributes?,
) : ModelWithAttributes<Image>() {


    override fun doExport(exportContext: ModelExportContext) = with(exportContext) {
        createLayoutScope {
            render(asRenderable())
        }
    }

    override fun doResume(exportContext: ModelExportContext) {
        doExport(exportContext)
    }

    override fun takeMeasures(exportContext: ModelExportContext) {
        with(exportContext) { measure(asRenderable()) }
    }

    private fun asRenderable(): ImageRenderable =
        ImageRenderable(filePath, attributes.orEmpty().forContext<ImageRenderable>(), context.customStateAttributes)

}
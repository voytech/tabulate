package io.github.voytech.tabulate.components.image.model

import io.github.voytech.tabulate.components.image.operation.ImageRenderable
import io.github.voytech.tabulate.core.model.*

class Image(
    @get:JvmSynthetic
    internal val filePath: String = "blank",
    override val attributes: Attributes?,
) : DirectlyRenderableModel<ImageRenderable>() {

    override fun ExportApi.asRenderable(): ImageRenderable =
        ImageRenderable(filePath, attributes.orEmpty().forContext<ImageRenderable>(), getCustomAttributes())

}
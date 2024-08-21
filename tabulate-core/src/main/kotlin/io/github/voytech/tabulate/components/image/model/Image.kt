package io.github.voytech.tabulate.components.image.model

import io.github.voytech.tabulate.components.image.operation.ImageRenderable
import io.github.voytech.tabulate.core.model.*
import java.util.*

class Image(
    override val id: String = UUID.randomUUID().toString(),
    @get:JvmSynthetic
    internal val filePath: String = "blank",
    override val attributes: Attributes?,
) : DirectlyRenderableModel<ImageRenderable>() {

    override fun ExportApi.asRenderable(): ImageRenderable =
        ImageRenderable(filePath, attributes.ensure().forContext<ImageRenderable>(), getCustomAttributes())

}
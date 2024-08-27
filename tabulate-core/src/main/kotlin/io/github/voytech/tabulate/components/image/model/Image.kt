package io.github.voytech.tabulate.components.image.model

import io.github.voytech.tabulate.components.image.operation.ImageRenderableEntity
import io.github.voytech.tabulate.core.model.*
import java.util.*

class Image(
    override val id: String = UUID.randomUUID().toString(),
    @get:JvmSynthetic
    internal val filePath: String = "blank",
    override val attributes: Attributes?,
) : DirectlyRenderableModel<ImageRenderableEntity>() {

    override fun ExportApi.asRenderable(): ImageRenderableEntity =
        ImageRenderableEntity(filePath, attributes.ensure().forContext<ImageRenderableEntity>(), getCustomAttributes())

}
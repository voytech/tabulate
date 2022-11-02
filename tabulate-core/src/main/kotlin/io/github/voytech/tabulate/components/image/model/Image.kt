package io.github.voytech.tabulate.components.image.model

import io.github.voytech.tabulate.components.image.template.ImageTemplate
import io.github.voytech.tabulate.components.image.template.ImageTemplateContext
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.ModelWithAttributes

class Image(
    @get:JvmSynthetic
    internal val filePath: String = "blank",
    override val attributes: Attributes?
): ModelWithAttributes<ImageTemplate, Image, ImageTemplateContext>() {

    override fun getExportTemplate() = ImageTemplate()
}
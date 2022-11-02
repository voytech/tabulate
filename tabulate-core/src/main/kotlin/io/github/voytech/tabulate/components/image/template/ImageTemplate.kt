package io.github.voytech.tabulate.components.image.template

import io.github.voytech.tabulate.components.image.model.Image
import io.github.voytech.tabulate.components.image.operation.ImageRenderable
import io.github.voytech.tabulate.core.model.SomeSize
import io.github.voytech.tabulate.core.model.attributes.HeightAttribute
import io.github.voytech.tabulate.core.model.attributes.WidthAttribute
import io.github.voytech.tabulate.core.model.orEmpty
import io.github.voytech.tabulate.core.template.ExportTemplate
import io.github.voytech.tabulate.core.template.ExportTemplateServices
import io.github.voytech.tabulate.core.template.TemplateContext


class ImageTemplate : ExportTemplate<ImageTemplate, Image, ImageTemplateContext>() {

    override fun doExport(templateContext: ImageTemplateContext) = with(templateContext) {
        createLayoutScope {
            render(ImageRenderable(model.filePath, model.attributes.orEmpty().forContext<ImageRenderable>()))
        }
    }

    override fun computeSize(parentContext: TemplateContext<*, *>, model: Image): SomeSize = SomeSize(
        model.attributes?.get<WidthAttribute>()?.value,
        model.attributes?.get<HeightAttribute>()?.value,
    )

    override fun createTemplateContext(parentContext: TemplateContext<*, *>, model: Image): ImageTemplateContext =
        ImageTemplateContext(model, parentContext.stateAttributes, parentContext.services)

}

class ImageTemplateContext(
    model: Image,
    stateAttributes: MutableMap<String, Any>,
    services: ExportTemplateServices,
) : TemplateContext<ImageTemplateContext, Image>(model, stateAttributes, services)
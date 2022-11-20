package io.github.voytech.tabulate.components.image.template

import io.github.voytech.tabulate.components.image.model.Image
import io.github.voytech.tabulate.components.image.operation.ImageRenderable
import io.github.voytech.tabulate.core.model.SomeSize
import io.github.voytech.tabulate.core.model.orEmpty
import io.github.voytech.tabulate.core.template.ExportTemplate
import io.github.voytech.tabulate.core.template.ExportInstance
import io.github.voytech.tabulate.core.template.TemplateContext


class ImageTemplate : ExportTemplate<ImageTemplate, Image, ImageTemplateContext>() {

    override fun doExport(templateContext: ImageTemplateContext) = with(templateContext) {
        createLayoutScope {
            render(model.asRenderable(templateContext))
        }
    }

    override fun takeMeasures(context: ImageTemplateContext) {
        with(context) { measure(model.asRenderable(context)) }
    }

    private fun Image.asRenderable(context: ImageTemplateContext): ImageRenderable =
        ImageRenderable(filePath, attributes.orEmpty().forContext<ImageRenderable>())

    override fun createTemplateContext(parentContext: TemplateContext<*, *>, model: Image): ImageTemplateContext =
        ImageTemplateContext(model, parentContext.stateAttributes, parentContext.instance)

}

class ImageTemplateContext(
    model: Image,
    stateAttributes: MutableMap<String, Any>,
    instance: ExportInstance,
) : TemplateContext<ImageTemplateContext, Image>(model, stateAttributes, instance)
package io.github.voytech.tabulate.components.text.template

import io.github.voytech.tabulate.components.text.model.Text
import io.github.voytech.tabulate.components.text.operation.TextRenderable
import io.github.voytech.tabulate.core.model.SomeSize
import io.github.voytech.tabulate.core.model.orEmpty
import io.github.voytech.tabulate.core.template.*


class TextTemplate : ExportTemplate<TextTemplate, Text, TextTemplateContext>() {

    override fun doExport(templateContext: TextTemplateContext) = with(templateContext) {
        model.asRenderable(templateContext).let { renderable ->
            createLayoutScope {
                render(renderable)
            }
        }
    }

    override fun doMeasures(context: TextTemplateContext): SomeSize = with(context) {
        model.asRenderable(context).let { renderable ->
            measure(renderable)
            SomeSize(renderable.boundingBox.width, renderable.boundingBox.height)
        }
    }

    private fun TemplateContext<*,*>.getTextValue(model: Text): String = model.valueSupplier?.let { value(it) } ?: model.value

    private fun Text.asRenderable(context: TemplateContext<*,*>): TextRenderable = with(context) {
        TextRenderable(getTextValue(this@asRenderable), attributes.orEmpty().forContext<TextRenderable>())
    }

    override fun createTemplateContext(parentContext: TemplateContext<*, *>, model: Text): TextTemplateContext =
        TextTemplateContext(model, parentContext.stateAttributes, parentContext.instance)

}

class TextTemplateContext(
    model: Text,
    stateAttributes: MutableMap<String, Any>,
    instance: ExportInstance,
) : TemplateContext<TextTemplateContext, Text>(model, stateAttributes, instance)
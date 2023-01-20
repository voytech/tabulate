package io.github.voytech.tabulate.components.text.model

import io.github.voytech.tabulate.components.text.operation.TextRenderable
import io.github.voytech.tabulate.core.model.*

class Text(
    @get:JvmSynthetic
    internal val value: String = "blank",
    private val valueSupplier: ReifiedValueSupplier<*,String>?,
    override val attributes: Attributes?
): ModelWithAttributes<Text>() {

    override fun doExport(templateContext: ModelExportContext<Text>) = with(templateContext) {
        model.asRenderable(templateContext).let { renderable ->
            createLayoutScope {
                render(renderable)
            }
        }
    }

    override fun takeMeasures(context: ModelExportContext<Text>) {
        with(context) { measure(asRenderable(context)) }
    }

    private fun asRenderable(context: ModelExportContext<Text>): TextRenderable = with(context) {
        TextRenderable(getTextValue(this), attributes.orEmpty().forContext<TextRenderable>())
    }

    private fun getTextValue(context: ModelExportContext<Text>): String = valueSupplier?.let { context.value(it) } ?: value

}
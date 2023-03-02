package io.github.voytech.tabulate.components.text.model

import io.github.voytech.tabulate.components.text.operation.TextRenderable
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.template.ResumeNext
import io.github.voytech.tabulate.core.template.operation.OverflowResult

class Text(
    @get:JvmSynthetic
    internal val value: String = "blank",
    private val valueSupplier: ReifiedValueSupplier<*, String>?,
    override val attributes: Attributes?,
) : ModelWithAttributes<Text>() {

    override fun doExport(exportContext: ModelExportContext) = with(exportContext) {
        asRenderable(exportContext).let { renderable ->
            createLayoutScope {
                if (render(renderable) is OverflowResult) suspendY()
            }
        }
    }

    override fun doResume(exportContext: ModelExportContext, resumeNext: ResumeNext) {
        doExport(exportContext)
        resumeNext()
    }

    override fun takeMeasures(exportContext: ModelExportContext) {
        with(exportContext) { measure(asRenderable(exportContext)) }
    }

    private fun asRenderable(context: ModelExportContext): TextRenderable = with(context) {
        TextRenderable(getTextValue(this), attributes.orEmpty().forContext<TextRenderable>(), customStateAttributes)
    }

    private fun getTextValue(context: ModelExportContext): String = valueSupplier?.let { context.value(it) } ?: value

}
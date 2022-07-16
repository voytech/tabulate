package io.github.voytech.tabulate.core.template

import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.template.operation.Operations

open class TemplateContext<ARM: Model<ARM>>(
    val model: ARM,
    val stateAttributes: MutableMap<String, Any>,
    val modelAttributes: Attributes<*>? = null
) {
    fun getCustomAttributes(): MutableMap<String, Any> = stateAttributes

    fun <R: RenderingContext> ExportTemplateApis<R>.getOperations(): Operations<R> = getOperations(model)
}
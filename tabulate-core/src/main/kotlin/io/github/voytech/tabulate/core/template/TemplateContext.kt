package io.github.voytech.tabulate.core.template

import io.github.voytech.tabulate.core.model.AttributedModelOrPart
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.model.ModelWithAttributes
import io.github.voytech.tabulate.core.template.operation.Operations

open class TemplateContext<ARM : Model<ARM>>(
    val model: ARM,
    val stateAttributes: MutableMap<String, Any>,
    val parentAttributes: Attributes<*>? = null,
    val modelAttributes: Attributes<*>? = null,

    ) {
    fun getCustomAttributes(): MutableMap<String, Any> = stateAttributes

    fun <R : RenderingContext> ExportTemplateApis<R>.getOperations(): Operations<R> = getOperations(model)
}

data class TransferAttributes<I : ModelWithAttributes<I>, O : AttributedModelOrPart<O>>(
    val attributedModel: O,
    val parentAttributes: Attributes<I>? = null,
    val attributes: Attributes<O> = parentAttributes?.cast(attributedModel.javaClass)
        ?: Attributes(attributedModel.javaClass),
)
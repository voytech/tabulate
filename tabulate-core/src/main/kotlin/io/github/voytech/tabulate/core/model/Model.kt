package io.github.voytech.tabulate.core.model

import io.github.voytech.tabulate.core.template.ExportTemplate
import io.github.voytech.tabulate.core.template.TemplateContext

interface Model<M: Model<M>> {
    fun getId(): String

    fun getExportTemplate(): ExportTemplate<M,out TemplateContext<M>>? = null

}

interface ModelPart

interface AttributeAware<A: AttributeAware<A>> {
    val attributes: Attributes<A>?
}

interface AttributedModelOrPart<A: AttributedModelOrPart<A>> : AttributeAware<A>, ModelPart

abstract class ModelWithAttributes<M: ModelWithAttributes<M>> : AttributedModelOrPart<M>, Model<M>

abstract class ModelPartWithAttributes<M: ModelPartWithAttributes<M>>: AttributedModelOrPart<M>, ModelPart
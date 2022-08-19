package io.github.voytech.tabulate.core.model

import io.github.voytech.tabulate.core.template.ExportTemplate
import io.github.voytech.tabulate.core.template.TemplateContext
import java.util.*


interface Model<M: Model<M,C>, C: TemplateContext<C,M>> {
    @get:JvmSynthetic
    val id: String
}

typealias UnconstrainedModel<SELF> = Model<SELF,out TemplateContext<*,SELF>>

interface ModelPart

interface AttributeAware {
    val attributes: Attributes?
}

interface AttributedModelOrPart<A: AttributedModelOrPart<A>> : AttributeAware, ModelPart

abstract class AbstractModel<E: ExportTemplate<E,M,C>,M: AbstractModel<E,M,C>, C: TemplateContext<C,M>>(override val id: String = UUID.randomUUID().toString()): Model<M,C>  {

    protected open fun getExportTemplate(): ExportTemplate<E, M, C>? = null

    @Suppress("UNCHECKED_CAST")
    fun export(parentContext: TemplateContext<*,*>) {
        getExportTemplate()?.export(parentContext, this as M)
    }
}

abstract class ModelWithAttributes<E: ExportTemplate<E,M,C>,M: ModelWithAttributes<E,M,C>,C: TemplateContext<C,M>> : AttributedModelOrPart<M>, AbstractModel<E,M,C>()

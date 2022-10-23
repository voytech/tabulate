package io.github.voytech.tabulate.core.model

import io.github.voytech.tabulate.core.template.ExportTemplate
import io.github.voytech.tabulate.core.template.LayoutContext
import io.github.voytech.tabulate.core.template.TemplateContext
import java.util.*

interface Model<M : AbstractModel<*,M, C>, C : TemplateContext<C, M>> {
    @get:JvmSynthetic
    val id: String
}

typealias UnconstrainedModel<SELF> = Model<SELF, out TemplateContext<*, SELF>>

interface ModelPart

interface AttributeAware {
    val attributes: Attributes?
}

interface AttributedModelOrPart<A : AttributedModelOrPart<A>> : AttributeAware, ModelPart

@Suppress("UNCHECKED_CAST")
abstract class AbstractModel<E : ExportTemplate<E, M, C>, M : AbstractModel<E, M, C>, C : TemplateContext<C, M>>(
    override val id: String = UUID.randomUUID().toString(),
) : Model<M, C> {

    internal val template by lazy { getExportTemplate() }

    protected abstract fun getExportTemplate(): ExportTemplate<E, M, C>

    fun export(parentContext: TemplateContext<*, *>, layoutContext: LayoutContext? = null) {
        template.export(parentContext, this as M, layoutContext)
    }

    fun getSize(parentContext: TemplateContext<*, *>): SomeSize? =
        template.computeSize(parentContext, this as M)
}

abstract class ModelWithAttributes<E : ExportTemplate<E, M, C>, M : ModelWithAttributes<E, M, C>, C : TemplateContext<C, M>> :
    AttributedModelOrPart<M>, AbstractModel<E, M, C>()

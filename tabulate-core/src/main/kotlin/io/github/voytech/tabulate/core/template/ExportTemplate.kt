package io.github.voytech.tabulate.core.template

import io.github.voytech.tabulate.core.model.BoundingRectangle
import io.github.voytech.tabulate.core.model.DataSourceBinding
import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.template.exception.OutputBindingResolvingException
import io.github.voytech.tabulate.core.template.layout.Layout
import io.github.voytech.tabulate.core.template.layout.Layouts
import io.github.voytech.tabulate.core.template.operation.EnableLayoutsAwareness
import io.github.voytech.tabulate.core.template.operation.Operations
import io.github.voytech.tabulate.core.template.operation.factories.ExportOperationsFactory
import io.github.voytech.tabulate.core.template.result.OutputBinding
import io.github.voytech.tabulate.core.template.spi.OutputBindingsProvider


class ExportTemplateApis<R : RenderingContext>(
    private val operationsFactory: ExportOperationsFactory<R>,
    private var layouts: Layouts = Layouts(),
) {

    fun <ARM : Model<ARM>> getOperations(model: ARM): Operations<R> =
        operationsFactory.createExportOperations(model, EnableLayoutsAwareness(layouts))

    fun getActiveLayout(): Layout = layouts.activeLayout()

    fun getActiveLayoutBoundaries(): BoundingRectangle = getActiveLayout().boundingRectangle

    fun getRenderingContextType(): Class<R> = operationsFactory.renderingContext

    fun resetLayouts() {
        layouts = Layouts()
    }

}

fun <ARM : Model<ARM>, R : RenderingContext> Model<ARM>.export(
    renderingContext: R,
    parentContext: TemplateContext<*>,
    registry: ExportTemplateApis<R>,
) {
    getExportTemplate()?.let { childTemplate ->
        val childContext = childTemplate.buildTemplateContext(parentContext, this as ARM)
        (childTemplate as ExportTemplate<ARM, TemplateContext<ARM>>).export(renderingContext, childContext, registry)
    }
}

interface ExportTemplate<ARM : Model<ARM>, CTX : TemplateContext<ARM>> {
    fun modelClass(): Class<ARM>
    fun buildTemplateContext(parentContext: TemplateContext<*>, childModel: ARM): CTX

    fun <R : RenderingContext> export(renderingContext: R, templateContext: CTX, apis: ExportTemplateApis<R>)
}

/**
 * Class wrapping ExportOperations into standalone ExportOperations.
 * @author Wojciech Mąka
 * @since 0.*.*
 */
class StandaloneExportTemplate<CTX : TemplateContext<ARM>, ARM : Model<ARM>>(
    private val format: DocumentFormat,
    private val delegate: ExportTemplate<ARM, CTX>,
) {

    private val outputBindingsProvider: OutputBindingsProvider<RenderingContext> by lazy {
        loadFirstByDocumentFormat<OutputBindingsProvider<RenderingContext>, RenderingContext>(format)!!
    }

    fun <O : Any, T : Any> export(model: ARM, output: O, dataSource: Iterable<T> = emptyList()) {
        val registry: ExportTemplateApis<RenderingContext> = ExportTemplateApis(ExportOperationsFactory(format))
        val renderingContext = loadRenderingContext(format)  //TODO it is inconvenient that in order to obtain valid DocumentFormat you need to load exportOperationFactory.
        val templateContext = delegate.buildTemplateContext(
            TemplateContext(
                model,
                if (dataSource.iterator().hasNext()) {
                    mutableMapOf("_dataSourceOverride" to DataSourceBinding(dataSource))
                } else mutableMapOf()
            ),
            model
        )
        resolveOutputBinding(output).run {
            setOutput(renderingContext, output)
            delegate.export(renderingContext, templateContext, registry)
            flush()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <O : Any> resolveOutputBinding(output: O): OutputBinding<RenderingContext, O> {
        return outputBindingsProvider.createOutputBindings()
            .filter {
                it.outputClass().isAssignableFrom(output::class.java)
            }.map { it as OutputBinding<RenderingContext, O> }
            .firstOrNull() ?: throw OutputBindingResolvingException()
    }

}
package io.github.voytech.tabulate.core.template

import io.github.voytech.tabulate.core.model.DataSourceBinding
import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.template.exception.OutputBindingResolvingException
import io.github.voytech.tabulate.core.template.operation.Operations
import io.github.voytech.tabulate.core.template.result.OutputBinding
import io.github.voytech.tabulate.core.template.spi.ExportOperationsProvider
import io.github.voytech.tabulate.core.template.spi.OutputBindingsProvider

typealias DiscoveredExportOperationFactories<R> = Map<Class<out Model>, ExportOperationsProvider<R, *>>
typealias DiscoveredTemplates = Map<Class<out Model>, ExportTemplate<*,*>>

@Suppress("UNCHECKED_CAST")
internal fun <R: RenderingContext, ARM: Model> DiscoveredExportOperationFactories<R>.getOperations(model: ARM): Operations<R>? =
    (this[model.javaClass] as? ExportOperationsProvider<R,ARM>)?.createExportOperations()

class ExportTemplateRegistry<R: RenderingContext>(
    private val operationsFactories: DiscoveredExportOperationFactories<R>,
    private val exportTemplates: DiscoveredTemplates
) {

    @Suppress("UNCHECKED_CAST")
    fun <ARM: Model, CTX: TemplateContext<ARM>> getChildTemplate(model: ARM): ExportTemplate<ARM,CTX> =
        (exportTemplates[model.javaClass] as? ExportTemplate<ARM,CTX> ?: error("not found"))

    internal fun <ARM: Model> getOperations(model: ARM): Operations<R>? =
        operationsFactories.getOperations(model)

}

fun <ARM: Model, R: RenderingContext> ARM.export(renderingContext: R, parentContext: TemplateContext<*>, registry: ExportTemplateRegistry<R>) {
    registry.getChildTemplate(this).let { childTemplate ->
        val childContext = childTemplate.buildTemplateContext(parentContext,this)
        when (childTemplate) {
            is CompositeExportTemplate -> childTemplate.export(renderingContext, registry.getOperations(this), childContext, registry)
            is SimpleExportTemplate -> childTemplate.export(renderingContext, registry.getOperations(this) ?: error("Simple export template requires operations!"), childContext)
        }
    }
}

sealed interface ExportTemplate<ARM : Model, CTX: TemplateContext<ARM>> {
    fun modelClass(): Class<ARM>
    fun buildTemplateContext(parentContext: TemplateContext<*>, childModel: ARM): CTX
}

abstract class CompositeExportTemplate<ARM : Model, CTX : TemplateContext<ARM>>: ExportTemplate<ARM, CTX> {

    abstract fun <R: RenderingContext> export(renderingContext: R, operations: Operations<R>?, templateContext: CTX, registry: ExportTemplateRegistry<R>)

}

abstract class SimpleExportTemplate<ARM : Model, CTX : TemplateContext<ARM>>: ExportTemplate<ARM, CTX>{

    abstract fun <R: RenderingContext> export(renderingContext: R, operations: Operations<R>, templateContext: CTX)

}

/**
 * Class wrapping ExportOperations into standalone ExportOperations.
 * @author Wojciech Mąka
 * @since 0.*.*
 */
class StandaloneExportTemplate<CTX : TemplateContext<ARM>, ARM : Model>(
    private val format: DocumentFormat,
    private val delegate: SimpleExportTemplate<ARM, CTX>,
) {

    private val outputBindingsProvider: OutputBindingsProvider<RenderingContext> by lazy {
        loadFirstByDocumentFormat<OutputBindingsProvider<RenderingContext>, RenderingContext>(format)!!
    }

    fun <O : Any, T> export(model: ARM, output: O, dataSource: Iterable<T> = emptyList()) {
        val factories = loadExportOperationFactories<RenderingContext>(format)
        val renderingContext = loadRenderingContext(format) //TODO it is inconvenient that in order to obtain valid DocumentFormat you need to load exportOperationFactory.
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
            delegate.export(
                renderingContext,
                factories.getOperations(model) ?: error("Standalone export template requires operations"),
                templateContext
            )
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
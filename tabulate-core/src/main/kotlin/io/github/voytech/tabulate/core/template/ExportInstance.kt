package io.github.voytech.tabulate.core.template

import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.template.exception.OutputBindingResolvingException
import io.github.voytech.tabulate.core.template.layout.*
import io.github.voytech.tabulate.core.template.operation.*
import io.github.voytech.tabulate.core.template.operation.factories.OperationsFactory
import io.github.voytech.tabulate.core.template.result.OutputBinding
import io.github.voytech.tabulate.core.template.spi.OutputBindingsProvider

typealias OperationsMap = MutableMap<Model<*>, Operations<RenderingContext>>

class ExportInstance(
    format: DocumentFormat, rootModel: AbstractModel<*>, stateAttributes: StateAttributes? = null,
    private val operationsFactory: OperationsFactory<RenderingContext> = OperationsFactory(format),
    internal val renderingContext: RenderingContext =
        operationsFactory.renderingContext.getDeclaredConstructor().newInstance(),
) {
    internal val uom: UnitsOfMeasure = UnitsOfMeasure.PT
    internal val root: ModelExportContext = rootModel.createStandaloneExportContext(this, stateAttributes)
    private val exportOperations: OperationsMap = mutableMapOf()
    private val measureOperations: OperationsMap = mutableMapOf()

    fun <M : AbstractModel<M>> getExportOperations(model: M): Operations<RenderingContext> =
        exportOperations.computeIfAbsent(model) {
            operationsFactory.createMeasureOperations(model).let { measuringOperations ->
                operationsFactory.createExportOperations(model,
                    EnableRenderingUsingLayouts(model.context, measuringOperations) { model.getActiveLayout() })
            }
        }

    fun <M : AbstractModel<M>> getMeasuringOperations(model: M): Operations<RenderingContext> =
        measureOperations.computeIfAbsent(model) {
            operationsFactory.createMeasureOperations(model,
                //TODO is this Enhancer required ? Consider not enhancing each operation, but instead create single operation which internally delegates operations returned from createMeasureOperations this will allow to enable measuring without single third-party rendering-context specific measuring operations.
                EnableMeasuringForLayouts { model.getActiveMeasuringLayout() }
            )
        }

    private fun <M : AbstractModel<M>> M.getActiveLayout(): LayoutScope =
        context.layouts().current(ExportPhase.RENDERING)

    private fun <M : AbstractModel<M>> M.getActiveMeasuringLayout(): LayoutScope =
        context.layouts().current(ExportPhase.MEASURING)

    internal fun getViewPortMaxRightBottom(): Position =
        if (renderingContext is HavingViewportSize) {
            Position(
                X(renderingContext.getWidth().orMax(uom).value, uom),
                Y(renderingContext.getHeight().orMax(uom).value, uom)
            )
        } else {
            Position(X.max(uom), Y.max(uom)) //TODO instead make it nullable - when null - renderer does not clip
        }

    fun <M : AbstractModel<M>> render(model: M, context: AttributedContext): OperationResult? =
        getExportOperations(model).invoke(renderingContext, context)

    fun <M : AbstractModel<M>> measure(model: M, context: AttributedContext): OperationResult? =
        getMeasuringOperations(model).invoke(renderingContext, context)

    fun clearLayouts() {
        root.navigation.traverse {
            it.context.layouts.clear()
        }.also {
            root.createLayout(LayoutConstraints(maxRightBottom = getViewPortMaxRightBottom()))
        }
    }

}

/**
 * A StandaloneExportTemplate is an entry point for model hierarchy export.
 * @author Wojciech Mąka
 * @since 0.*.*
 */
class StandaloneExportTemplate<M : AbstractModel<M>>(private val format: DocumentFormat) {

    private val outputBindingsProvider: OutputBindingsProvider<RenderingContext> by lazy {
        loadFirstByDocumentFormat<OutputBindingsProvider<RenderingContext>, RenderingContext>(format)!!
    }

    fun <O : Any> export(model: M, output: O) = with(ExportInstance(format, model)) {
        resolveOutputBinding(output).run {
            setOutput(renderingContext, output)
            model.export(this@with.root)
            flush()
        }
    }

    fun <O : Any, T : Any> export(model: M, output: O, dataSource: Iterable<T> = emptyList()) =
        with(ExportInstance(format, model, dataSource.asStateAttributes())) {
            resolveOutputBinding(output).run {
                setOutput(renderingContext, output)
                model.export(this@with.root)
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

    private fun <T : Any> Iterable<T>.asStateAttributes(): StateAttributes = StateAttributes(
        if (iterator().hasNext()) {
            mutableMapOf("_dataSourceOverride" to DataSourceBinding(this))
        } else mutableMapOf()
    )

}
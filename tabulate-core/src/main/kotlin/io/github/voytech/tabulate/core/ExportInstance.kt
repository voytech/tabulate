package io.github.voytech.tabulate.core

import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.model.exception.OutputBindingResolvingException
import io.github.voytech.tabulate.core.operation.factories.OperationsFactory
import io.github.voytech.tabulate.core.operation.*
import io.github.voytech.tabulate.core.result.OutputBinding
import io.github.voytech.tabulate.core.spi.OutputBindingsProvider

/**
 * An [ExportInstance] is a central state holder for particular export format.
 * @author Wojciech Mąka
 * @since 0.2.*
 */
class ExportInstance(
    format: DocumentFormat,
    private val operationsFactory: OperationsFactory<RenderingContext> = OperationsFactory(format),
    internal val renderingContext: RenderingContext =
        operationsFactory.renderingContext.getDeclaredConstructor().newInstance(),
) {
    internal val uom: UnitsOfMeasure = UnitsOfMeasure.PT

    private lateinit var root: ModelContextTreeNode

    private val nodeMap: MutableMap<AbstractModel, ModelContextTreeNode> = mutableMapOf()

    fun ModelExportContext.getExportOperations(): Operations<RenderingContext> {
        return operationsFactory.createMeasureOperations(model).let { measuringOperations ->
            operationsFactory.createExportOperations(
                model, EnableRenderingUsingLayouts(measuringOperations) { activeLayoutContext.pairWithParent() },
            )
        }
    }

    fun ModelExportContext.getMeasuringOperations(): Operations<RenderingContext> {
        return operationsFactory.createMeasureOperations(
            model, EnableMeasuringForLayouts { activeLayoutContext.pairWithParent() },
        )
    }

    internal fun getDocumentMaxRightBottom(): Position =
        if (renderingContext is HavingViewportSize) {
            Position(
                X(renderingContext.getWidth().orMax(uom).value, uom),
                Y(renderingContext.getHeight().orMax(uom).value, uom)
            )
        } else {
            Position(X.max(uom), Y.max(uom))
        }

    @JvmSynthetic
    internal operator fun get(model: AbstractModel): ModelContextTreeNode? = nodeMap[model]

    @JvmSynthetic
    internal operator fun set(model: AbstractModel, value: ModelContextTreeNode) {
        nodeMap[model] = value
    }

    @JvmSynthetic
    internal fun AbstractModel.createStandaloneExportContext(attributes: StateAttributes? = null): ModelExportContext =
        ModelExportContext(this@ExportInstance, this, attributes.ensure()).also {
            root = ModelContextTreeNode(it, null)
            nodeMap[this] = root
        }

}

/**
 * A [StandaloneExportTemplate] is an entry point for model hierarchy export.
 * @author Wojciech Mąka
 * @since 0.2.*
 */
class StandaloneExportTemplate(private val format: DocumentFormat) {

    private val outputBindingsProvider: OutputBindingsProvider<RenderingContext> by lazy {
        loadFirstByDocumentFormat<OutputBindingsProvider<RenderingContext>, RenderingContext>(format)!!
    }

    fun <O : Any> export(model: AbstractModel, output: O, params: Map<String, Any> = emptyMap()) =
        with(ExportInstance(format)) {
            model.createStandaloneExportContext(StateAttributes(params.toMutableMap())).api {
                resolveOutputBinding(output).run {
                    setOutput(renderingContext, output)
                    model.export()
                    flush()
                }
            }
        }

    fun <O : Any, T : Any> export(
        model: AbstractModel,
        output: O,
        dataSource: Iterable<T> = emptyList(),
        params: Map<String, Any> = emptyMap()
    ) =
        with(ExportInstance(format)) {
            model.createStandaloneExportContext(dataSource.asStateAttributes() + params).api {
                resolveOutputBinding(output).run {
                    setOutput(renderingContext, output)
                    model.export()
                    flush()
                }
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
package io.github.voytech.tabulate.core

import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.model.exception.OutputBindingResolvingException
import io.github.voytech.tabulate.core.layout.SpaceConstraints
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

    internal lateinit var root: ModelContextTreeNode

    private val nodeMap: MutableMap<AbstractModel, ModelContextTreeNode> = mutableMapOf()

    fun ModelExportContext.getExportOperations(): Operations<RenderingContext> =
        operationsFactory.createMeasureOperations(model).let { measuringOperations ->
            operationsFactory.createExportOperations(model,
                EnableRenderingUsingLayouts(measuringOperations) {
                    layouts.current(ExportPhase.RENDERING)
                },
            )
        }

    fun ModelExportContext.getMeasuringOperations(): Operations<RenderingContext> =
        operationsFactory.createMeasureOperations(model,
            EnableMeasuringForLayouts { layouts.current(ExportPhase.MEASURING) },
        )


    internal fun getDocumentMaxRightBottom(): Position =
        if (renderingContext is HavingViewportSize) {
            Position(
                X(renderingContext.getWidth().orMax(uom).value, uom),
                Y(renderingContext.getHeight().orMax(uom).value, uom)
            )
        } else {
            Position(X.max(uom), Y.max(uom)) //TODO instead make it nullable - when null - renderer does not clip
        }

    @JvmSynthetic
    internal operator fun get(model: AbstractModel): ModelContextTreeNode? = nodeMap[model]

    @JvmSynthetic
    internal operator fun set(model: AbstractModel, value: ModelContextTreeNode) {
        nodeMap[model] = value
    }

    fun clearAllLayouts() = with(root) {
        traverse { it.layouts.clear() }
        context.layouts.createLayout(SpaceConstraints(maxRightBottom = getDocumentMaxRightBottom()))
    }


    @JvmSynthetic
    internal fun AbstractModel.createStandaloneExportContext(attributes: StateAttributes? = null): ModelExportContext =
        ModelExportContext(this@ExportInstance, this, attributes.orEmpty()).also {
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

    fun <O : Any> export(model: AbstractModel, output: O,params: Map<String,Any> = emptyMap()) = with(ExportInstance(format)) {
        model.createStandaloneExportContext(StateAttributes(params.toMutableMap())).scope {
            resolveOutputBinding(output).run {
                setOutput(renderingContext, output)
                model.export()
                flush()
            }
        }
    }

    fun <O : Any, T : Any> export(model: AbstractModel, output: O, dataSource: Iterable<T> = emptyList(), params: Map<String,Any> = emptyMap()) =
        with(ExportInstance(format)) {
            model.createStandaloneExportContext(dataSource.asStateAttributes() + params).scope {
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
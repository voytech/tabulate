package io.github.voytech.tabulate.core.template

import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.template.exception.OutputBindingResolvingException
import io.github.voytech.tabulate.core.template.layout.*
import io.github.voytech.tabulate.core.template.operation.*
import io.github.voytech.tabulate.core.template.operation.factories.OperationsFactory
import io.github.voytech.tabulate.core.template.result.OutputBinding
import io.github.voytech.tabulate.core.template.spi.OutputBindingsProvider

typealias ResumeNext = () -> Unit

typealias OperationsMap = MutableMap<Model<*>, Operations<RenderingContext>>

class ExportInstance(
    format: DocumentFormat,
    private val operationsFactory: OperationsFactory<RenderingContext> = OperationsFactory(format),
    internal val renderingContext: RenderingContext = operationsFactory.renderingContext.newInstance(),
) {
    internal val uom: UnitsOfMeasure = UnitsOfMeasure.PT
    internal lateinit var root: RootNode<*>
    private val exportOperations: OperationsMap = mutableMapOf()
    private val measureOperations: OperationsMap = mutableMapOf()

    fun <M : Model<M>> getExportOperations(model: M): Operations<RenderingContext> =
        exportOperations.computeIfAbsent(model) {
            operationsFactory.createMeasureOperations(model).let { measuringOperations ->
                operationsFactory.createExportOperations(model,
                    JoinOperations(measuringOperations) { !it.boundingBox().isDefined() },
                    EnableLayoutsAwareness { getActiveLayout() }
                )
            }
        }

    fun <M : Model<M>> getMeasuringOperations(model: M): Operations<RenderingContext> =
        measureOperations.computeIfAbsent(model) {
            operationsFactory.createMeasureOperations(model,
                SkipRedundantMeasurements(),
                //TODO is this Enhancer required ? Consider not enhancing each operation, but instead create single operation which internally delegates operations returned from createMeasureOperations
                //TODO this will allow to enable measuring without single third-party rendering-context specific measuring operations.
                EnableLayoutsAwareness(checkOverflows = false) { getActiveMeasuringLayout() }
            )
        }

    private fun getActiveLayout(): Layout = root.activeNode.layout ?: when (root.activeNode) {
        is BranchNode -> (root.activeNode as BranchNode).getClosestAncestorLayout()
            ?: error("No active layout present!")

        else -> error("No active layout present!")
    }

    private fun getActiveMeasuringLayout(): Layout = root.activeNode.measuringLayout ?: when (root.activeNode) {
        is BranchNode -> (root.activeNode as BranchNode).getClosestLayoutAwareAncestor().measuringLayout!!
        else -> error("No active measuring layout to perform measures on!")
    }

    internal fun getViewPortMaxRightBottom(): Position =
        if (renderingContext is HavingViewportSize) {
            Position(
                X(renderingContext.getWidth().orMax(uom).value, uom),
                Y(renderingContext.getHeight().orMax(uom).value, uom)
            )
        } else {
            Position(X.max(uom), Y.max(uom)) //TODO instead make it nullable - when null - renderer does not clip
        }

    fun <M : Model<M>> render(model: M,context: AttributedContext) {
        getExportOperations(model).invoke(renderingContext, context)
    }

    fun <M : Model<M>> measure(model: M,context: AttributedContext) {
        getMeasuringOperations(model).invoke(renderingContext, context)
    }

    internal fun ModelExportContext<*>.node(): TreeNode<*> =
        root.nodes[model] ?: error("Could not find node by context")


    private fun <M : AbstractModel<M>> createNode(context: ModelExportContext<M>): TreeNode<M> = if (!::root.isInitialized) {
        RootNode(context).apply { root = this }
    } else {
        root.activeNode.appendChild(context)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <M : AbstractModel<M>> M.node(): TreeNode<M>? =
        if (::root.isInitialized) {
            root.nodes[this]?.let { (it as TreeNode<M>) }
        } else null

    fun <M : AbstractModel<M>, R> inScope(
        model: M, templateContext: () -> ModelExportContext<M>, block: TreeNode<M>.() -> R,
    ) = (model.node() ?: createNode(templateContext())).withActiveNode(block)

    private fun <M : AbstractModel<M>, R> TreeNode<M>.withActiveNode(
        block: TreeNode<M>.() -> R,
    ): R {
        setActive()
        return run(block).also {
            endActive()
        }
    }

    fun resetLayouts() = with(root) {
        traverse { it.dropLayout() }.also { resetLayout(getViewPortMaxRightBottom()) }
    }

    fun resumeAllSuspendedNodes() = with(root) {
        while (suspendedNodes.isNotEmpty()) {
            resetLayouts()
            resumeAll()
        }
    }

    private fun resumeAll() = with(root) {
        preserveActive { resume(root) }
    }

    private fun resumeChildren(node: TreeNode<*>) {
        node.forChildren { resume(it) }
    }

    private fun resume(node: TreeNode<*>) = with(root) {
        activeNode = node
        resumeTemplate(node)
    }

    private fun <M : AbstractModel<M>> resumeTemplate(node: TreeNode<M>) {
        node.context.model.resume(node.context) { resumeChildren(node) }
    }

}




/**
 * Class wrapping ExportOperations into standalone ExportOperations.
 * @author Wojciech Mąka
 * @since 0.*.*
 */
class StandaloneExportTemplate<M : AbstractModel<M>>(
    private val format: DocumentFormat,
) {

    private val outputBindingsProvider: OutputBindingsProvider<RenderingContext> by lazy {
        loadFirstByDocumentFormat<OutputBindingsProvider<RenderingContext>, RenderingContext>(format)!!
    }

    fun <O : Any> export(model: M, output: O) = with(ExportInstance(format)) {
        resolveOutputBinding(output).run {
            setOutput(renderingContext, output)
            model.export(ModelExportContext(model, mutableMapOf(), this@with))
            resumeAllSuspendedNodes()
            flush()
        }
    }

    fun <O : Any, T : Any> export(model: M, output: O, dataSource: Iterable<T> = emptyList()) =
        with(ExportInstance(format)) {
            resolveOutputBinding(output).run {
                val modelExportContext = ModelExportContext(model, dataSource.asStateAttributes(), this@with)
                setOutput(renderingContext, output)
                model.export(modelExportContext)
                resumeAllSuspendedNodes()
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

    private fun <T : Any> Iterable<T>.asStateAttributes(): MutableMap<String, Any> = if (iterator().hasNext()) {
        mutableMapOf("_dataSourceOverride" to DataSourceBinding(this))
    } else mutableMapOf()

}
package io.github.voytech.tabulate.core.template

import io.github.voytech.tabulate.components.commons.operation.newPage
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.template.exception.OutputBindingResolvingException
import io.github.voytech.tabulate.core.template.layout.*
import io.github.voytech.tabulate.core.template.operation.AttributedContext
import io.github.voytech.tabulate.core.template.operation.EnableLayoutsAwareness
import io.github.voytech.tabulate.core.template.operation.Operations
import io.github.voytech.tabulate.core.template.operation.factories.ExportOperationsFactory
import io.github.voytech.tabulate.core.template.result.OutputBinding
import io.github.voytech.tabulate.core.template.spi.OutputBindingsProvider

class ExportTemplateServices(
    format: DocumentFormat,
    private val operationsFactory: ExportOperationsFactory<RenderingContext> = ExportOperationsFactory(format),
    internal val renderingContext: RenderingContext = operationsFactory.renderingContext.newInstance(),
) {
    private val uom: UnitsOfMeasure = UnitsOfMeasure.PT
    private lateinit var root: RootNode<*, *, *>
    private val exportOperations: MutableMap<Model<*, *>, Operations<RenderingContext>> = mutableMapOf()

    fun <M : UnconstrainedModel<M>> getOperations(model: M): Operations<RenderingContext> =
        exportOperations.computeIfAbsent(model) {
            operationsFactory.createExportOperations(model, EnableLayoutsAwareness { getActiveLayout() })
        }

    fun <M : UnconstrainedModel<M>> M.render(context: AttributedContext) {
        getOperations(this).render(renderingContext, context)
    }

    private fun TemplateContext<*, *>.node(): TreeNode<*, *, *> =
        root.nodes[model] ?: error("Could not find node by context")

    private fun ensureRootLayout() {
        if (root.layout == null) {
            root.setLayout(maxRightBottom = viewPortMaxRightBottom())
        }
    }

    fun TemplateContext<*, *>.createLayoutScope(
        queries: AbstractLayoutQueries = DefaultLayoutQueries(),
        childLeftTopCorner: Position? = layoutContext?.leftTop,
        maxRightBottom: Position? = layoutContext?.maxRightBottom,
        orientation: Orientation = Orientation.HORIZONTAL,
        block: Layout<*, *, *>.() -> Unit,
    ): Unit = when (val current = node()) {
        is BranchNode -> {
            ensureRootLayout()
            current.createLayoutScope(queries, childLeftTopCorner, maxRightBottom, orientation, block)
        }

        is RootNode -> current.setLayout(
            queries, uom,
            childLeftTopCorner.orStart(uom),
            viewPortMaxRightBottom(),
            orientation
        ).let(block)
    }

    private fun getActiveLayout(): Layout<*, *, *> = root.activeNode.layout ?: when (root.activeNode) {
        is BranchNode -> (root.activeNode as BranchNode).getWrappingLayout()
        else -> error("No active layout present!")
    }

    fun getActiveLayoutBoundaries(): BoundingRectangle = getActiveLayout().boundingRectangle

    private fun viewPortMaxRightBottom(): Position =
        if (renderingContext is HavingViewportSize) {
            Position(
                X(renderingContext.getWidth().orMax(uom).value, uom),
                Y(renderingContext.getHeight().orMax(uom).value, uom)
            )
        } else {
            Position(X.max(uom), Y.max(uom)) //TODO instead make it nullable - when null - renderer does not clip
        }

    private fun <E : ExportTemplate<E, M, C>, C : TemplateContext<C, M>, M : AbstractModel<E, M, C>> createNode(
        template: E, context: C,
    ): TreeNode<M, E, C> = if (!::root.isInitialized) {
        RootNode(template, context).apply { root = this }
    } else {
        root.activeNode.appendChild(context)
    }

    fun <E : ExportTemplate<E, M, C>, C : TemplateContext<C, M>, M : AbstractModel<E, M, C>> inNewScope(
        template: E, context: C, block: TreeNode<*, *, *>.() -> Unit,
    ) = createNode(template, context).let {
        it.setActive()
        it.apply(block)
        it.endActive()
    }

    fun resetLayouts() = with(root) {
        traverse { it.dropLayout() }.also { ensureRootLayout() }
    }

    fun <M : UnconstrainedModel<M>> resumeAllSuspendedNodes(onModel: M) = with(root) {
        while (suspendedNodes.isNotEmpty()) {
            resetLayouts()
            onModel.render(newPage(onModel.id))
            resumeAll()
        }
    }

    private fun resumeAll() = with(root) {
        preserveActive {
            traverse {
                activeNode = it
                it.resume()
            }
        }
    }

    fun TemplateContext<*, *>.setSuspended() = with(root) {
        suspendedNodes.add(this@setSuspended)
    }

    fun TemplateContext<*, *>.setResumed() = with(root) {
        suspendedNodes.remove(this@setResumed)
    }

}

enum class TemplateStatus {
    ACTIVE,
    PARTIAL_X,
    PARTIAL_Y,
    PARTIAL_XY,
    PARTIAL_YX,
    FINISHED;

    internal fun isPartlyExported(): Boolean =
        PARTIAL_YX == this || PARTIAL_XY == this || PARTIAL_X == this || PARTIAL_Y == this

    internal fun isYOverflow(): Boolean = PARTIAL_YX == this || PARTIAL_XY == this || PARTIAL_Y == this

    internal fun isXOverflow(): Boolean = PARTIAL_YX == this || PARTIAL_XY == this || PARTIAL_X == this

}

data class LayoutContext(
    val leftTop: Position? = null,
    val maxRightBottom: Position? = null,
    val orientation: Orientation = Orientation.HORIZONTAL,
)

open class TemplateContext<C : TemplateContext<C, M>, M : AbstractModel<*, M, C>>(
    val model: M,
    val stateAttributes: MutableMap<String, Any>,
    val services: ExportTemplateServices,
    val parentAttributes: Attributes? = null,
    val modelAttributes: Attributes? = null,
    var status: TemplateStatus = TemplateStatus.ACTIVE,
) {
    val renderingContext: RenderingContext
        get() = services.renderingContext

    internal var layoutContext: LayoutContext? = null

    fun getCustomAttributes(): MutableMap<String, Any> = stateAttributes

    fun ExportTemplateServices.getOperations(): Operations<RenderingContext> = getOperations(model)

    fun suspendX() {
        status = when (status) {
            TemplateStatus.ACTIVE,
            TemplateStatus.PARTIAL_X,
            -> TemplateStatus.PARTIAL_X

            TemplateStatus.PARTIAL_Y -> TemplateStatus.PARTIAL_YX
            TemplateStatus.PARTIAL_XY -> TemplateStatus.PARTIAL_XY
            TemplateStatus.PARTIAL_YX -> TemplateStatus.PARTIAL_YX
            TemplateStatus.FINISHED -> error("Cannot suspend model exporting when it has finished.")
        }
        status = TemplateStatus.PARTIAL_X
    }

    fun suspendY() {
        status = when (status) {
            TemplateStatus.ACTIVE,
            TemplateStatus.PARTIAL_Y,
            -> TemplateStatus.PARTIAL_Y

            TemplateStatus.PARTIAL_X -> TemplateStatus.PARTIAL_XY
            TemplateStatus.PARTIAL_XY -> TemplateStatus.PARTIAL_XY
            TemplateStatus.PARTIAL_YX -> TemplateStatus.PARTIAL_YX
            TemplateStatus.FINISHED -> error("Cannot suspend model exporting when it has finished.")
        }
    }

    fun finishOrSuspend() = with(services) {
        if (status.isPartlyExported()) {
            setSuspended()
        } else {
            setResumed()
            status = TemplateStatus.FINISHED
        }
    }

    fun isPartlyExported(): Boolean = status.isPartlyExported()

    fun isYOverflow(): Boolean = status.isYOverflow()

    fun isXOverflow(): Boolean = status.isXOverflow()

}

abstract class ExportTemplate<E : ExportTemplate<E, M, C>, M : AbstractModel<E, M, C>, C : TemplateContext<C, M>> {

    internal fun export(parentContext: TemplateContext<*, *>, model: M, layoutContext: LayoutContext? = null) =
        with(parentContext.services) {
            createTemplateContext(parentContext, model).let { templateContext ->
                inNewScope(self(), templateContext) {
                    templateContext.layoutContext = layoutContext
                    doExport(templateContext)
                    templateContext.finishOrSuspend()
                }
            }
        }

    internal fun onResume(context: C) {
        context.status = TemplateStatus.ACTIVE
        doResume(context)
        context.finishOrSuspend()
    }

    open fun computeSize(parentContext: TemplateContext<*, *>, model: M): Size? = null

    protected abstract fun createTemplateContext(parentContext: TemplateContext<*, *>, model: M): C

    protected abstract fun doExport(templateContext: C)

    protected open fun doResume(templateContext: C) {}

    protected fun C.createLayoutScope(
        queries: AbstractLayoutQueries = DefaultLayoutQueries(),
        orientation: Orientation = Orientation.HORIZONTAL,
        childLeftTopCorner: Position? = null,
        maxRightBottom: Position? = null,
        block: Layout<*, *, *>.() -> Unit,
    ) = with(services) {
        createLayoutScope(queries, childLeftTopCorner, maxRightBottom, orientation, block)
    }

    protected fun C.render(context: AttributedContext) = with(services) {
        model.render(context)
    }

    protected fun C.resetLayouts() = services.resetLayouts()

    fun C.resumeAllSuspendedNodes() = with(services) {
        resumeAllSuspendedNodes(model)
    }

    @Suppress("UNCHECKED_CAST")
    private fun self(): E = (this as E)
}

/**
 * Class wrapping ExportOperations into standalone ExportOperations.
 * @author Wojciech Mąka
 * @since 0.*.*
 */
class StandaloneExportTemplate<E : ExportTemplate<E, M, C>, M : AbstractModel<E, M, C>, C : TemplateContext<C, M>>(
    private val format: DocumentFormat,
) {

    private val outputBindingsProvider: OutputBindingsProvider<RenderingContext> by lazy {
        loadFirstByDocumentFormat<OutputBindingsProvider<RenderingContext>, RenderingContext>(format)!!
    }

    fun <O : Any> export(model: M, output: O) = with(ExportTemplateServices(format)) {
        resolveOutputBinding(output).run {
            setOutput(renderingContext, output)
            model.template.export(TemplateContext(model, mutableMapOf(), this@with), model)
            resumeAllSuspendedNodes(model)
            flush()
        }
    }

    fun <O : Any, T : Any> export(model: M, output: O, dataSource: Iterable<T> = emptyList()) =
        with(ExportTemplateServices(format)) {
            resolveOutputBinding(output).run {
                val templateContext = TemplateContext(model, dataSource.asStateAttributes(), this@with)
                setOutput(renderingContext, output)
                model.template.export(templateContext, model)
                resumeAllSuspendedNodes(model)
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
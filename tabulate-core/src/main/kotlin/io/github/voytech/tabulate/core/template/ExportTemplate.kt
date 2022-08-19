package io.github.voytech.tabulate.core.template

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
    private lateinit var activeNode: TreeNode<*, *, *>
    private val nodes: MutableMap<TemplateContext<*, *>, TreeNode<*, *, *>> = mutableMapOf()
    internal var suspendedNodes: MutableSet<TemplateContext<*, *>> = mutableSetOf()

    //TODO add caching
    fun <M : Model<M, *>> getOperations(model: M): Operations<RenderingContext> =
        operationsFactory.createExportOperations(model, EnableLayoutsAwareness { getActiveLayout() })

    fun <M : Model<M, *>> M.render(context: AttributedContext) {
        getOperations(this).render(renderingContext, context)
    }

    private fun TemplateContext<*, *>.node(): TreeNode<*, *, *> = nodes[this] ?: error("Could not find node by context")

    private fun ensureRootLayout() {
        if (root.layout == null) {
            root.setLayout(maxHeight = maxHeight(uom), maxWidth = maxWidth(uom))
        }
    }

    fun TemplateContext<*, *>.createLayoutScope(
        queries: AbstractLayoutQueries = DefaultLayoutQueries(),
        childLeftTopCorner: Position? = null,
        orientation: Orientation = Orientation.HORIZONTAL,
        block: Layout<*, *, *>.() -> Unit,
    ): Unit = when (val current = node()) {
        is BranchNode -> {
            ensureRootLayout()
            current.createLayoutScope(queries, childLeftTopCorner, orientation, block)
        }
        is RootNode -> current.setLayout(
            queries, uom, maxHeight(uom), maxWidth(uom), orientation, childLeftTopCorner.orStart(uom)
        ).let(block)
    }

    private fun getActiveLayout(): Layout<*, *, *> = activeNode.layout ?: when (activeNode) {
        is BranchNode -> (activeNode as BranchNode).getWrappingLayout()
        else -> error("No active layout present!")
    }

    fun getActiveLayoutBoundaries(): BoundingRectangle = getActiveLayout().boundingRectangle

    private fun maxHeight(uom: UnitsOfMeasure): Height = if (renderingContext is RenderingContextAttributes) {
        renderingContext.getHeight()
    } else Height.max(uom)

    private fun maxWidth(uom: UnitsOfMeasure): Width = if (renderingContext is RenderingContextAttributes) {
        renderingContext.getWidth()
    } else Width.max(uom)

    private fun <E : ExportTemplate<E, M, C>, C : TemplateContext<C, M>, M : Model<M, C>> startScope(
        template: E, context: C,
    ): TreeNode<M, E, C> = if (!::root.isInitialized) {
        RootNode(template, context).apply {
            nodes[context] = this
            root = this
            activeNode = root
        }
    } else {
        BranchNode(template, context, activeNode, root).apply {
            nodes[context] = this
            activeNode.children.add(this)
            activeNode = this
        }
    }

    private fun endScope() {
        activeNode = when (activeNode) {
            is BranchNode<*, *, *> -> (activeNode as BranchNode<*, *, *>).parent
            else -> root
        }
    }

    fun <E : ExportTemplate<E, M, C>, C : TemplateContext<C, M>, M : Model<M, C>> inNewScope(
        template: E, context: C, block: TreeNode<*, *, *>.() -> Unit,
    ) {
        startScope(template, context).apply(block)
        endScope()
    }

    fun resetLayouts() = with(root) {
        traverse { it.dropLayout() }.also { ensureRootLayout() }
    }

    fun TemplateContext<*, *>?.suspensionsExist() =
        suspendedNodes.isNotEmpty() && suspendedNodes.toSet().intersect(setOf(this)) != setOf(this)

    fun resumeAll() = with(root) {
        keepingCurrentActiveNode {
            traverse {
                activeNode = it
                it.resume()
            }
        }
    }

    private fun keepingCurrentActiveNode(block: () -> Unit) {
        val saved = activeNode
        block()
        activeNode = saved
    }

}

enum class TemplateStatus {
    ACTIVE,
    PARTIAL_X,
    PARTIAL_Y,
    PARTIAL_XY,
    PARTIAL_YX,
    FINISHED;

    internal fun isPartiallyExported(): Boolean =
        PARTIAL_YX == this || PARTIAL_XY == this || PARTIAL_X == this || PARTIAL_Y == this

    internal fun isYOverflow(): Boolean = PARTIAL_YX == this || PARTIAL_XY == this || PARTIAL_Y == this

    internal fun isXOverflow(): Boolean = PARTIAL_YX == this || PARTIAL_XY == this || PARTIAL_X == this

}

open class TemplateContext<C : TemplateContext<C, M>, M : Model<M, C>>(
    val model: M,
    val stateAttributes: MutableMap<String, Any>,
    val services: ExportTemplateServices,
    val parentAttributes: Attributes? = null,
    val modelAttributes: Attributes? = null,
    var status: TemplateStatus = TemplateStatus.ACTIVE,
) {
    val renderingContext: RenderingContext
        get() = services.renderingContext

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

    fun finishOrSuspend() {
        if (status.isPartiallyExported()) {
            services.suspendedNodes.add(this)
        } else {
            services.suspendedNodes.remove(this)
            status = TemplateStatus.FINISHED
        }
    }

    fun isPartiallyExported(): Boolean = status.isPartiallyExported()

    fun isYOverflow(): Boolean = status.isYOverflow()

    fun isXOverflow(): Boolean = status.isXOverflow()
}

abstract class ExportTemplate<E : ExportTemplate<E, M, C>, M : Model<M, C>, C : TemplateContext<C, M>> {

    internal fun export(parentContext: TemplateContext<*, *>, model: M) {
        createTemplateContext(parentContext, model).let { templateContext ->
            with(templateContext.services) {
                inNewScope(self(), templateContext) {
                    doExport(templateContext)
                    templateContext.finishOrSuspend()
                }
            }
        }
    }

    internal fun onResume(context: C) {
        context.status = TemplateStatus.ACTIVE
        doResume(context)
        context.finishOrSuspend()
    }

    protected abstract fun createTemplateContext(parentContext: TemplateContext<*, *>, model: M): C

    protected abstract fun doExport(templateContext: C)

    protected open fun doResume(templateContext: C) {}

    protected fun C.createLayoutScope(
        queries: AbstractLayoutQueries = DefaultLayoutQueries(),
        childLeftTopCorner: Position? = null,
        orientation: Orientation = Orientation.HORIZONTAL,
        block: Layout<*, *, *>.() -> Unit,
    ) = with(services) {
        createLayoutScope(queries, childLeftTopCorner, orientation, block)
    }

    protected fun C.render(context: AttributedContext) = with(services) {
        model.render(context)
    }

    protected fun C.resetLayouts() = services.resetLayouts()

    protected fun C.resumeAll() = services.resumeAll()

    protected fun C.otherSuspensionsExist() = with(services) {
        suspensionsExist()
    }

    @Suppress("UNCHECKED_CAST")
    private fun self(): E = (this as E)
}

/**
 * Class wrapping ExportOperations into standalone ExportOperations.
 * @author Wojciech Mąka
 * @since 0.*.*
 */
class StandaloneExportTemplate<E : ExportTemplate<E, M, C>, M : Model<M, C>, C : TemplateContext<C, M>>(
    private val format: DocumentFormat,
    private val delegate: E,
) {

    private val outputBindingsProvider: OutputBindingsProvider<RenderingContext> by lazy {
        loadFirstByDocumentFormat<OutputBindingsProvider<RenderingContext>, RenderingContext>(format)!!
    }

    fun <O : Any> export(model: M, output: O) = with(ExportTemplateServices(format)) {
        resolveOutputBinding(output).run {
            setOutput(renderingContext, output)
            delegate.export(TemplateContext(model, mutableMapOf(), this@with), model)
            flush()
        }
    }

    fun <O : Any, T : Any> export(model: M, output: O, dataSource: Iterable<T> = emptyList()) =
        with(ExportTemplateServices(format)) {
            resolveOutputBinding(output).run {
                setOutput(renderingContext, output)
                delegate.export(TemplateContext(model, dataSource.asStateAttributes(), this@with), model)
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
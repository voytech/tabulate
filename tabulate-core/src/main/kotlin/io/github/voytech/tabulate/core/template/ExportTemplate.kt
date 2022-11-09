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

typealias ResumeNext = () -> Unit

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

    fun TemplateContext<*, *>.bubblePartialStatus() {
        node().getParent()?.let {
            if (status.isPartlyExported()) {
                it.context.status = status
                it.context.bubblePartialStatus()
            }
        }
    }

    private fun ensureRootLayout() {
        if (root.layout == null) {
            root.setLayout(maxRightBottom = viewPortMaxRightBottom())
        }
    }

    fun TemplateContext<*, *>.createLayoutScope(
        queries: AbstractLayoutPolicy = DefaultLayoutPolicy(),
        childLeftTopCorner: Position?,
        maxRightBottom: Position?,
        orientation: Orientation = Orientation.HORIZONTAL,
        block: Layout<*, *, *>.() -> Unit,
    ): Unit = when (val current = node()) {
        is BranchNode -> {
            ensureRootLayout()
            current.createLayoutScope(
                queries,
                childLeftTopCorner?:layoutContext?.leftTop,
                maxRightBottom?:layoutContext?.maxRightBottom,
                orientation, block
            )
        }

        is RootNode -> current.setLayout(
            queries, uom,
            childLeftTopCorner?:layoutContext?.leftTop.orStart(uom),
            viewPortMaxRightBottom(),
            orientation
        ).let(block)
    }

    private fun getActiveLayout(): Layout<*, *, *> = root.activeNode.layout ?: when (root.activeNode) {
        is BranchNode -> (root.activeNode as BranchNode).getWrappingLayoutOrThrow()
        else -> error("No active layout present!")
    }

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

    fun resumeAllSuspendedNodes() = with(root) {
        while (suspendedNodes.isNotEmpty()) {
            resetLayouts()
            resumeAll()
        }
    }

    private fun resumeAll() = with(root) {
        preserveActive { resume(root) }
    }

    private fun resumeChildren(node: TreeNode<*, *, *>) {
        node.forChildren { resume(it) }
    }

    private fun resume(node: TreeNode<*, *, *>) = with(root) {
        activeNode = node
        resumeTemplate(node)
    }

    private fun <M : AbstractModel<E, M, C>, E : ExportTemplate<E, M, C>, C : TemplateContext<C, M>> resumeTemplate(node: TreeNode<M, E, C>) {
        node.template.onResume(node.context) { resumeChildren(node) }
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

@JvmInline
value class StateAttributes(val stateAttributes: MutableMap<String, Any>) {

    inline fun <reified E: ExecutionContext> addExecutionContext(executionContext: E) {
        stateAttributes["executionContext-${E::class.java.canonicalName}"] = executionContext
    }

    inline fun <reified E: ExecutionContext> getExecutionContext(): E? = getExecutionContext(E::class.java)

    @Suppress("UNCHECKED_CAST")
    fun <E: ExecutionContext> getExecutionContext(_class: Class<E>): E? =
        stateAttributes["executionContext-${_class.canonicalName}"] as E?

    inline fun <reified E: ExecutionContext> ensureExecutionContext(provider: () -> E): E =
        getExecutionContext() ?: run { addExecutionContext(provider()); getExecutionContext()!! }

    inline fun <reified E: ExecutionContext> removeExecutionContext(): E? =
        stateAttributes.remove("executionContext-${E::class.java.canonicalName}") as E?

    inline fun <reified C: Any> getCustomAttribute(key: String): C? = stateAttributes[key] as C?

    inline fun <reified C: Any> removeCustomAttribute(key: String): C? = stateAttributes.remove(key) as C?

    @Suppress("UNCHECKED_CAST")
    fun <C: ExecutionContext,R: Any> ReifiedValueSupplier<C,R>.value(): R? =
        getExecutionContext(inClass)?.let { ctx -> this(ctx as C) }
}

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

    fun suspendX() = with(services) {
        status = when (status) {
            TemplateStatus.ACTIVE,
            TemplateStatus.PARTIAL_X,
            -> TemplateStatus.PARTIAL_X

            TemplateStatus.PARTIAL_Y -> TemplateStatus.PARTIAL_YX
            TemplateStatus.PARTIAL_XY -> TemplateStatus.PARTIAL_XY
            TemplateStatus.PARTIAL_YX -> TemplateStatus.PARTIAL_YX
            TemplateStatus.FINISHED -> error("Cannot suspend model exporting when it has finished.")
        }
        bubblePartialStatus()
    }

    fun suspendY() = with(services) {
        status = when (status) {
            TemplateStatus.ACTIVE,
            TemplateStatus.PARTIAL_Y,
            -> TemplateStatus.PARTIAL_Y

            TemplateStatus.PARTIAL_X -> TemplateStatus.PARTIAL_XY
            TemplateStatus.PARTIAL_XY -> TemplateStatus.PARTIAL_XY
            TemplateStatus.PARTIAL_YX -> TemplateStatus.PARTIAL_YX
            TemplateStatus.FINISHED -> error("Cannot suspend model exporting when it has finished.")
        }
        bubblePartialStatus()
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

fun <C : ExecutionContext, R: Any> TemplateContext<*, *>.value(supplier: ReifiedValueSupplier<C,R>): R? =
    with(StateAttributes(getCustomAttributes())) { supplier.value() }

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

    internal fun onResume(context: C, resumeNext: ResumeNext) {
        if (context.isPartlyExported()) {
            context.status = TemplateStatus.ACTIVE
            doResume(context, resumeNext)
            context.finishOrSuspend()
        }
    }

    open fun computeSize(parentContext: TemplateContext<*, *>, model: M): SomeSize? = null

    protected abstract fun createTemplateContext(parentContext: TemplateContext<*, *>, model: M): C

    protected abstract fun doExport(templateContext: C)

    protected open fun doResume(templateContext: C, resumeNext: ResumeNext) {
        resumeNext()
    }

    protected fun C.createLayoutScope(
        queries: AbstractLayoutPolicy = DefaultLayoutPolicy(),
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
        resumeAllSuspendedNodes()
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
            resumeAllSuspendedNodes()
            flush()
        }
    }

    fun <O : Any, T : Any> export(model: M, output: O, dataSource: Iterable<T> = emptyList()) =
        with(ExportTemplateServices(format)) {
            resolveOutputBinding(output).run {
                val templateContext = TemplateContext(model, dataSource.asStateAttributes(), this@with)
                setOutput(renderingContext, output)
                model.template.export(templateContext, model)
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
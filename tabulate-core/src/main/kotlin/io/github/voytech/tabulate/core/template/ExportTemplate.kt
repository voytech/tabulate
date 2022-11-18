package io.github.voytech.tabulate.core.template

import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.template.exception.OutputBindingResolvingException
import io.github.voytech.tabulate.core.template.layout.*
import io.github.voytech.tabulate.core.template.operation.*
import io.github.voytech.tabulate.core.template.operation.factories.OperationsFactory
import io.github.voytech.tabulate.core.template.result.OutputBinding
import io.github.voytech.tabulate.core.template.spi.OutputBindingsProvider

typealias ResumeNext = () -> Unit

typealias  OperationsMap = MutableMap<Model<*, *>, Operations<RenderingContext>>

class ExportInstance(
    format: DocumentFormat,
    private val operationsFactory: OperationsFactory<RenderingContext> = OperationsFactory(format),
    internal val renderingContext: RenderingContext = operationsFactory.renderingContext.newInstance(),
) {
    private val uom: UnitsOfMeasure = UnitsOfMeasure.PT
    private lateinit var root: RootNode<*, *, *>
    private val exportOperations: OperationsMap = mutableMapOf()
    private val measureOperations: OperationsMap = mutableMapOf()

    fun <M : UnconstrainedModel<M>> getExportOperations(model: M): Operations<RenderingContext> =
        exportOperations.computeIfAbsent(model) {
            operationsFactory.createMeasureOperations(model).let { measuringOperations ->
                operationsFactory.createExportOperations(model,
                    JoinOperations(measuringOperations) { !it.boundingBox().isDefined() },
                    EnableLayoutsAwareness { getActiveLayout() }
                )
            }
        }

    fun <M : UnconstrainedModel<M>> getMeasuringOperations(model: M): Operations<RenderingContext> =
        measureOperations.computeIfAbsent(model) {
            operationsFactory.createMeasureOperations(model,
                SkipRedundantMeasurements(),
                EnableLayoutsAwareness { getActiveMeasuringLayout() }
            )
        }

    fun <M : UnconstrainedModel<M>> M.render(context: AttributedContext) {
        getExportOperations(this).invoke(renderingContext, context)
    }

    fun <M : UnconstrainedModel<M>> M.measure(context: AttributedContext) {
        getMeasuringOperations(this).invoke(renderingContext, context)
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
        block: Layout.() -> Unit,
    ): Unit = when (val current = node()) {

        is BranchNode -> {
            ensureRootLayout()
            current.createLayoutScope(
                queries,
                childLeftTopCorner ?: layoutContext?.leftTop,
                maxRightBottom ?: layoutContext?.maxRightBottom,
                orientation, block
            )
        }

        is RootNode -> current.setLayout(
            queries, uom,
            childLeftTopCorner ?: layoutContext?.leftTop.orStart(uom),
            viewPortMaxRightBottom(),
            orientation
        ).let { block(it) }

    }

    fun <R> TreeNode<*, *, *>.setMeasuringLayout(policy: AbstractLayoutPolicy, block: (DefaultLayout) -> R): R {
        measuringGrid = DefaultLayout(
            UnitsOfMeasure.PT,
            Orientation.HORIZONTAL,
            Position(X.zero(), Y.zero()),
            viewPortMaxRightBottom(), //TODO this is not viewPortMaxRightBottom but relative to parent.
            policy
        )
        return block(measuringGrid!!)
    }

    private fun getActiveLayout(): Layout = root.activeNode.layout ?: when (root.activeNode) {
        is BranchNode -> (root.activeNode as BranchNode).getClosestAncestorLayout()!!
        else -> error("No active layout present!")
    }

    private fun getActiveMeasuringLayout(): Layout = root.activeNode.measuringGrid ?: when (root.activeNode) {
        is BranchNode -> (root.activeNode as BranchNode).getClosestLayoutAwareAncestor().measuringGrid!!
        else -> error("No active measuring layout to perform measures on!")
    }

    internal fun viewPortMaxRightBottom(): Position =
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

    @Suppress("UNCHECKED_CAST")
    private fun <E : ExportTemplate<E, M, C>, C : TemplateContext<C, M>, M : AbstractModel<E, M, C>> M.node(): TreeNode<M, E, C>? =
        if (::root.isInitialized) {
            root.nodes[this]?.let { (it as TreeNode<M, E, C>) }
        } else null

    fun <E : ExportTemplate<E, M, C>, C : TemplateContext<C, M>, M : AbstractModel<E, M, C>, R> M.inNodeScope(
        template: E, contextProvider: () -> C, block: TreeNode<M, E, C>.() -> R,
    ) = (node() ?: createNode(template, contextProvider())).runActiveNode(block)

    private fun <E : ExportTemplate<E, M, C>, C : TemplateContext<C, M>, M : AbstractModel<E, M, C>, R> TreeNode<M, E, C>.runActiveNode(
        block: TreeNode<M, E, C>.() -> R,
    ): R {
        setActive()
        return run(block).also {
            endActive()
        }
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

enum class ExportStatus {
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

    inline fun <reified E : ExecutionContext> addExecutionContext(executionContext: E) {
        stateAttributes["executionContext-${E::class.java.canonicalName}"] = executionContext
    }

    inline fun <reified E : ExecutionContext> getExecutionContext(): E? = getExecutionContext(E::class.java)

    @Suppress("UNCHECKED_CAST")
    fun <E : ExecutionContext> getExecutionContext(_class: Class<E>): E? =
        stateAttributes["executionContext-${_class.canonicalName}"] as E?

    inline fun <reified E : ExecutionContext> ensureExecutionContext(provider: () -> E): E =
        getExecutionContext() ?: run { addExecutionContext(provider()); getExecutionContext()!! }

    inline fun <reified E : ExecutionContext> removeExecutionContext(): E? =
        stateAttributes.remove("executionContext-${E::class.java.canonicalName}") as E?

    inline fun <reified C : Any> getCustomAttribute(key: String): C? = stateAttributes[key] as C?

    inline fun <reified C : Any> removeCustomAttribute(key: String): C? = stateAttributes.remove(key) as C?

    @Suppress("UNCHECKED_CAST")
    fun <C : ExecutionContext, R : Any> ReifiedValueSupplier<C, R>.value(): R? =
        getExecutionContext(inClass)?.let { ctx -> this(ctx as C) }
}

open class TemplateContext<C : TemplateContext<C, M>, M : AbstractModel<*, M, C>>(
    val model: M,
    val stateAttributes: MutableMap<String, Any>,
    val instance: ExportInstance,
    val parentAttributes: Attributes? = null,
    val modelAttributes: Attributes? = null,
    var status: ExportStatus = ExportStatus.ACTIVE,
) {
    val renderingContext: RenderingContext
        get() = instance.renderingContext

    internal var layoutContext: LayoutContext? = null

    fun getCustomAttributes(): MutableMap<String, Any> = stateAttributes

    fun ExportInstance.getOperations(): Operations<RenderingContext> = getExportOperations(model)

    fun suspendX() = with(instance) {
        status = when (status) {
            ExportStatus.ACTIVE,
            ExportStatus.PARTIAL_X,
            -> ExportStatus.PARTIAL_X

            ExportStatus.PARTIAL_Y -> ExportStatus.PARTIAL_YX
            ExportStatus.PARTIAL_XY -> ExportStatus.PARTIAL_XY
            ExportStatus.PARTIAL_YX -> ExportStatus.PARTIAL_YX
            ExportStatus.FINISHED -> error("Cannot suspend model exporting when it has finished.")
        }
        bubblePartialStatus()
    }

    fun suspendY() = with(instance) {
        status = when (status) {
            ExportStatus.ACTIVE,
            ExportStatus.PARTIAL_Y,
            -> ExportStatus.PARTIAL_Y

            ExportStatus.PARTIAL_X -> ExportStatus.PARTIAL_XY
            ExportStatus.PARTIAL_XY -> ExportStatus.PARTIAL_XY
            ExportStatus.PARTIAL_YX -> ExportStatus.PARTIAL_YX
            ExportStatus.FINISHED -> error("Cannot suspend model exporting when it has finished.")
        }
        bubblePartialStatus()
    }

    fun finishOrSuspend() = with(instance) {
        if (status.isPartlyExported()) {
            setSuspended()
        } else {
            setResumed()
            status = ExportStatus.FINISHED
        }
    }

    fun isPartlyExported(): Boolean = status.isPartlyExported()

    fun isYOverflow(): Boolean = status.isYOverflow()

    fun isXOverflow(): Boolean = status.isXOverflow()

}

fun <C : ExecutionContext, R : Any> TemplateContext<*, *>.value(supplier: ReifiedValueSupplier<C, R>): R? =
    with(StateAttributes(getCustomAttributes())) { supplier.value() }

abstract class ExportTemplate<E : ExportTemplate<E, M, C>, M : AbstractModel<E, M, C>, C : TemplateContext<C, M>> {

    internal fun export(parentContext: TemplateContext<*, *>, model: M, layoutContext: LayoutContext? = null) =
        with(parentContext.instance) {
            model.inNodeScope(self(), { createTemplateContext(parentContext, model) }) {
                context.layoutContext = layoutContext
                doExport(context)
                context.finishOrSuspend()
            }
        }

    internal fun onResume(context: C, resumeNext: ResumeNext) {
        if (context.isPartlyExported()) {
            context.status = ExportStatus.ACTIVE
            doResume(context, resumeNext)
            context.finishOrSuspend()
        }
    }

    //TODO on measure - should call probably the same logic as export and resumption but should inject measurement operations instead of export operations. We should not be able to use different exporting logic for those two paths.
    internal fun onMeasure(parentContext: TemplateContext<*, *>, model: M): SomeSize? = with(parentContext.instance) {
        model.inNodeScope(self(), { createTemplateContext(parentContext, model) }) {
            setMeasuringLayout(createLayoutPolicy(context)) { measuringLayout ->
                takeMeasures(context).also { measuringLayout.measured = true }
            }
        }
    }

    protected open fun takeMeasures(context: C): SomeSize? = null

    protected abstract fun createTemplateContext(parentContext: TemplateContext<*, *>, model: M): C

    protected abstract fun doExport(templateContext: C)

    protected open fun doResume(templateContext: C, resumeNext: ResumeNext) {
        resumeNext()
    }

    protected open fun createLayoutPolicy(templateContext: C): AbstractLayoutPolicy = DefaultLayoutPolicy()

    protected fun C.createLayoutScope(
        orientation: Orientation = Orientation.HORIZONTAL,
        childLeftTopCorner: Position? = null,
        maxRightBottom: Position? = null,
        block: Layout.() -> Unit,
    ) = with(instance) {
        createLayoutScope(
            createLayoutPolicy(this@createLayoutScope), childLeftTopCorner, maxRightBottom, orientation, block
        )
    }

    protected fun C.render(context: AttributedContext) = with(instance) {
        model.render(context)
    }

    protected fun C.measure(context: RenderableContext) = with(instance) {
        model.measure(context)
    }

    protected fun C.resetLayouts() = instance.resetLayouts()

    fun C.resumeAllSuspendedNodes() = with(instance) {
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

    fun <O : Any> export(model: M, output: O) = with(ExportInstance(format)) {
        resolveOutputBinding(output).run {
            setOutput(renderingContext, output)
            model.template.export(TemplateContext(model, mutableMapOf(), this@with), model)
            resumeAllSuspendedNodes()
            flush()
        }
    }

    fun <O : Any, T : Any> export(model: M, output: O, dataSource: Iterable<T> = emptyList()) =
        with(ExportInstance(format)) {
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
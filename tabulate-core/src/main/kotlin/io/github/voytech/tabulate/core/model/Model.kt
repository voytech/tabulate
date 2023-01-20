package io.github.voytech.tabulate.core.model

import io.github.voytech.tabulate.core.template.*
import io.github.voytech.tabulate.core.template.layout.AbstractLayoutPolicy
import io.github.voytech.tabulate.core.template.layout.DefaultLayoutPolicy
import io.github.voytech.tabulate.core.template.layout.Layout
import io.github.voytech.tabulate.core.template.operation.AttributedContext
import io.github.voytech.tabulate.core.template.operation.Operations
import io.github.voytech.tabulate.core.template.operation.RenderableContext
import java.util.*

interface Model<M : AbstractModel<M>> {
    @get:JvmSynthetic
    val id: String
}

interface ModelPart

interface AttributeAware {
    val attributes: Attributes?
}

interface AttributedModelOrPart<A : AttributedModelOrPart<A>> : AttributeAware, ModelPart


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

class ModelExportContext<M : AbstractModel<M>>(
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
            root.suspendedNodes.add(this@ModelExportContext)
        } else {
            root.suspendedNodes.remove(this@ModelExportContext)
            status = ExportStatus.FINISHED
        }
    }

    fun isPartlyExported(): Boolean = status.isPartlyExported()

    fun isYOverflow(): Boolean = status.isYOverflow()

    fun isXOverflow(): Boolean = status.isXOverflow()

    private fun bubblePartialStatus() = with(instance) {
        var current = node().getParent()
        while (current != null && status.isPartlyExported()) {
            current.context.status = status
            current = current.getParent()
        }
    }

}

fun <C : ExecutionContext, R : Any> ModelExportContext<*>.value(supplier: ReifiedValueSupplier<C, R>): R? =
    with(StateAttributes(getCustomAttributes())) { supplier.value() }


@Suppress("UNCHECKED_CAST")
abstract class AbstractModel<SELF: AbstractModel<SELF>>(
    override val id: String = UUID.randomUUID().toString(),
) : Model<SELF> {

    open val planSpaceOnExport: Boolean = false

    private fun ExportInstance.shouldMeasure(): Boolean {
        return planSpaceOnExport && !getMeasuringOperations(self()).isEmpty()
    }

    private fun <R> inScope(parentContext: ModelExportContext<*>, block: TreeNode<SELF>.() -> R): R =
        parentContext.instance.inScope(self(), { createExportContext(parentContext) }, block)

    fun export(parentContext: ModelExportContext<*>, layoutCxt: LayoutContext? = null) =
        with(parentContext.instance) {
            if (shouldMeasure()) measure(parentContext)
            inScope(parentContext) {
                doExport(context.apply { layoutContext = layoutCxt })
                context.finishOrSuspend()
            }
        }

    //TODO on measure - should call probably the same logic as export and resumption but should inject measurement operations instead of export operations. We should not be able to use different exporting logic for those two paths.
    fun measure(parentContext: ModelExportContext<*>): SomeSize = with(parentContext.instance) {
        inScope(parentContext) {
            setMeasuringLayout(createLayoutPolicy(context), uom) { measuringLayout ->
                takeMeasures(context).also { measuringLayout.spacePlanned = true }
                measuringLayout.boundingRectangle.let {
                    SomeSize(it.getWidth(), it.getHeight())
                }
            }
        }
    }

    internal fun resume(context: ModelExportContext<SELF>, resumeNext: ResumeNext) {
        if (context.isPartlyExported()) {
            context.status = ExportStatus.ACTIVE
            doResume(context, resumeNext)
            context.finishOrSuspend()
        }
    }

    protected open fun createExportContext(parentContext: ModelExportContext<*>): ModelExportContext<SELF> =
        ModelExportContext(
            self(),
            parentContext.stateAttributes,
            parentContext.instance,
            parentContext.parentAttributes
        )

    protected open fun takeMeasures(context: ModelExportContext<SELF>) {}

    protected abstract fun doExport(templateContext: ModelExportContext<SELF>)

    protected open fun doResume(templateContext: ModelExportContext<SELF>, resumeNext: ResumeNext) {
        resumeNext()
    }

    protected open fun createLayoutPolicy(templateContext: ModelExportContext<SELF>): AbstractLayoutPolicy = DefaultLayoutPolicy()

    fun ModelExportContext<SELF>.createLayoutScope(
        orientation: Orientation = Orientation.HORIZONTAL,
        childLeftTopCorner: Position? = null,
        maxRightBottom: Position? = null,
        block: Layout.() -> Unit,
    ) = with(instance) {
        val ctx = this@createLayoutScope
        node().let { currentNode ->
            currentNode.createLayoutScope(
                { createLayoutPolicy(ctx) }, uom,
                childLeftTopCorner ?: layoutContext?.leftTop,
                maxRightBottom ?: layoutContext?.maxRightBottom ?: getViewPortMaxRightBottom(),
                orientation, block
            )
        }
    }

    protected fun ModelExportContext<SELF>.render(context: AttributedContext) {
        instance.render(model,context)
    }

    protected fun ModelExportContext<SELF>.measure(context: RenderableContext) {
        instance.measure(model,context)
    }

    protected fun ModelExportContext<SELF>.resetLayouts() = instance.resetLayouts()

    fun ModelExportContext<SELF>.resumeAllSuspendedNodes() = with(instance) {
        resumeAllSuspendedNodes()
    }

    fun ModelExportContext<SELF>.getExportOperations(): Operations<RenderingContext> = with(instance) {
        getExportOperations(model)
    }

    fun ModelExportContext<SELF>.getMeasuringOperations(): Operations<RenderingContext> = with(instance) {
        getMeasuringOperations(model)
    }

    @Suppress("UNCHECKED_CAST")
    private fun self(): SELF = (this as SELF)
}

abstract class ModelWithAttributes<SELF: ModelWithAttributes<SELF>> :
    AttributedModelOrPart<SELF>, AbstractModel<SELF>()

interface ExecutionContext

fun interface ValueSupplier<C: ExecutionContext,V: Any>: (C) -> V

data class ReifiedValueSupplier<C: ExecutionContext,V : Any>(
    val inClass: Class<out ExecutionContext>,
    val retClass: Class<V>,
    val delegate: ValueSupplier<C,V>
): ValueSupplier<C,V> {
    override fun invoke(context: C): V = delegate(context)
}
package io.github.voytech.tabulate.core.model

import io.github.voytech.tabulate.core.template.*
import io.github.voytech.tabulate.core.template.layout.DefaultLayoutPolicy
import io.github.voytech.tabulate.core.template.layout.Layout
import io.github.voytech.tabulate.core.template.layout.LayoutPolicy
import io.github.voytech.tabulate.core.template.operation.AttributedContext
import io.github.voytech.tabulate.core.template.operation.OperationResult
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
value class StateAttributes(val data: MutableMap<String, Any>) {

    @Suppress("UNCHECKED_CAST")
    fun <S : Any> get(_class: Class<S>): S? = data["state-${_class.canonicalName}"] as S?

    inline fun <reified S : Any> set(state: S) {
        data["state-${S::class.java.canonicalName}"] = state
    }

    inline fun <reified S : Any> get(): S? = get(S::class.java)

    inline operator fun <reified S : Any> get(key: String): S? = data[key] as S?

    inline operator fun <reified S : Any> set(key: String, state: S) {
        data[key] = state
    }

    inline fun <reified E : ExecutionContext> addExecutionContext(executionContext: E) {
        data["executionContext-${E::class.java.canonicalName}"] = executionContext
    }

    @Suppress("UNCHECKED_CAST")
    fun <E : ExecutionContext> getExecutionContext(_class: Class<E>): E? =
        data["executionContext-${_class.canonicalName}"] as E?

    inline fun <reified E : ExecutionContext> getExecutionContext(): E? = getExecutionContext(E::class.java)

    inline fun <reified E : ExecutionContext> ensureExecutionContext(provider: () -> E): E =
        getExecutionContext() ?: run { addExecutionContext(provider()); getExecutionContext()!! }

    inline fun <reified E : ExecutionContext> removeExecutionContext(): E? =
        data.remove("executionContext-${E::class.java.canonicalName}") as E?

    @Suppress("UNCHECKED_CAST")
    fun <C : ExecutionContext, R : Any> ReifiedValueSupplier<C, R>.value(): R? =
        getExecutionContext(inClass)?.let { ctx -> this(ctx as C) }
}

fun StateAttributes?.orEmpty() = this ?: StateAttributes(mutableMapOf())

enum class ModelExportStatus {
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

class ModelExportContext(
    val instance: ExportInstance,
    val navigation: Navigation,
    val layouts: Layouts,
    val customStateAttributes: StateAttributes,
    val parentAttributes: Attributes? = null,
    val modelAttributes: Attributes? = null,
    var status: ModelExportStatus = ModelExportStatus.ACTIVE,
) {

    val renderingContext: RenderingContext
        get() = instance.renderingContext

    internal var layoutContext: LayoutContext? = null

    fun getCustomAttributes(): MutableMap<String, Any> = customStateAttributes.data

    fun suspendX() = with(instance) {
        status = when (status) {
            ModelExportStatus.ACTIVE,
            ModelExportStatus.PARTIAL_X,
            -> ModelExportStatus.PARTIAL_X

            ModelExportStatus.PARTIAL_Y -> ModelExportStatus.PARTIAL_YX
            ModelExportStatus.PARTIAL_XY -> ModelExportStatus.PARTIAL_XY
            ModelExportStatus.PARTIAL_YX -> ModelExportStatus.PARTIAL_YX
            ModelExportStatus.FINISHED -> error("Cannot suspend model exporting when it has finished.")
        }
        bubblePartialStatus()
    }

    fun suspendY() = with(instance) {
        status = when (status) {
            ModelExportStatus.ACTIVE,
            ModelExportStatus.PARTIAL_Y,
            -> ModelExportStatus.PARTIAL_Y

            ModelExportStatus.PARTIAL_X -> ModelExportStatus.PARTIAL_XY
            ModelExportStatus.PARTIAL_XY -> ModelExportStatus.PARTIAL_XY
            ModelExportStatus.PARTIAL_YX -> ModelExportStatus.PARTIAL_YX
            ModelExportStatus.FINISHED -> error("Cannot suspend model exporting when it has finished.")
        }
        bubblePartialStatus()
    }

    fun finishOrSuspend() {
        if (status.isPartlyExported()) {
            instance.suspendedModels += navigation.active
        } else {
            instance.suspendedModels -= navigation.active
            status = ModelExportStatus.FINISHED
        }
    }

    fun isPartlyExported(): Boolean = status.isPartlyExported()

    fun isYOverflow(): Boolean = status.isYOverflow()

    fun isXOverflow(): Boolean = status.isXOverflow()

    private fun bubblePartialStatus() = with(instance) {
        var parent = navigation.parent
        val currentLayout = layouts.renderingLayout
        while (parent != null && status.isPartlyExported()) {
            with(parent) {
                val parentLayout = context.layouts.renderingLayout
                context.status = status
                parent = context.navigation.parent
            }
        }
    }
}

fun <C : ExecutionContext, R : Any> ModelExportContext.value(supplier: ReifiedValueSupplier<C, R>): R? =
    with(customStateAttributes) { supplier.value() }

interface LayoutPolicyProvider<LP : LayoutPolicy> {
    val policy: LP
}

@Suppress("UNCHECKED_CAST")
abstract class AbstractModel<SELF : AbstractModel<SELF>>(
    override val id: String = UUID.randomUUID().toString(),
) : Model<SELF> {

    open val planSpaceOnExport: Boolean = false

    private val layoutPolicyHandle: LayoutPolicy by lazy { layoutPolicy() }

    internal lateinit var context: ModelExportContext

    private fun ExportInstance.shouldMeasure(): Boolean {
        return planSpaceOnExport && !getMeasuringOperations(self()).isEmpty()
    }

    private fun <R> withinInitializedContext(parent: ModelExportContext, block: () -> R): R =
        ensuringExportContext(parent).run { block() }

    fun export(parentContext: ModelExportContext, layoutCxt: LayoutContext? = null) =
        with(parentContext.instance) {
            if (shouldMeasure()) measure(parentContext)
            withinInitializedContext(parentContext) {
                doExport(context.apply { layoutContext = layoutCxt })
                context.finishOrSuspend()
            }
        }

    //TODO on measure - should call probably the same logic as export and resumption but should inject measurement operations instead of export operations. We should not be able to use different exporting logic for those two paths.
    fun measure(parentContext: ModelExportContext): SomeSize = with(parentContext.instance) {
        withinInitializedContext(parentContext) {
            context.setMeasuringLayout(uom) { measuringLayout ->
                takeMeasures(context).also { layoutPolicyHandle.isSpaceMeasured = true }
                measuringLayout.boundingRectangle.let {
                    SomeSize(it.getWidth(), it.getHeight())
                }
            }
        }
    }

    internal fun resume(context: ModelExportContext, resumeNext: ResumeNext) {
        if (context.isPartlyExported()) {
            context.status = ModelExportStatus.ACTIVE
            doResume(context, resumeNext)
            context.finishOrSuspend()
        }
    }

    private fun createExportContext(parentContext: ModelExportContext): ModelExportContext =
        with(parentContext) {
            navigation.addChild(self())
            ModelExportContext(instance,
                Navigation(navigation.root, navigation.active.takeIf { navigation.root != self() }, self()),
                Layouts(layoutPolicyHandle),
                customStateAttributes,
                parentAttributes
            ).also(::initialize)
        }

    private fun ensuringExportContext(parentContext: ModelExportContext): ModelExportContext =
        if (::context.isInitialized) context else run { context = createExportContext(parentContext);context }

    //TODO make safe scope (when calling within initialize: , doResume, resume, export, and so on should be all forbidden calls)
    protected open fun initialize(exportContext: ModelExportContext) {
        // method is not mandatory and should not provide default implementation
    }

    //TODO make safe scope (when calling within takeMeasures: , doResume, resume, export, and so on should be all forbidden calls)
    protected open fun takeMeasures(exportContext: ModelExportContext) {
        // method is not mandatory and should not provide default implementation
    }

    //TODO make safe scope (when calling within doExport: , doResume, resume, export, and so on should be all forbidden calls)
    protected abstract fun doExport(exportContext: ModelExportContext)

    //TODO make safe scope (when calling within doResume: , resume, export, doExport and so on should be all forbidden calls)
    protected open fun doResume(exportContext: ModelExportContext, resumeNext: ResumeNext) {
        resumeNext()
    }

    protected fun createLayoutScope(
        orientation: Orientation = Orientation.HORIZONTAL,
        childLeftTopCorner: Position? = null,
        maxRightBottom: Position? = null,
        block: Layout.() -> Unit,
    ) = with(context.instance) {
        context.createLayoutScope(
            uom,
            childLeftTopCorner ?: context.layoutContext?.leftTop,
            maxRightBottom ?: context.layoutContext?.maxRightBottom ?: getViewPortMaxRightBottom(),
            orientation,
            block
        )
    }

    private fun layoutPolicy(): LayoutPolicy = if (this is LayoutPolicyProvider<*>) policy else DefaultLayoutPolicy()

    protected fun ModelExportContext.render(context: AttributedContext): OperationResult? =
        instance.render(self(), context)


    protected fun ModelExportContext.measure(context: AttributedContext): OperationResult? =
        instance.measure(self(), context)

    protected fun ModelExportContext.clearLayouts() = instance.clearLayouts()

    fun ModelExportContext.resumeAllSuspendedNodes() = with(instance) {
        resumeAllSuspendedModels()
    }

    @Suppress("UNCHECKED_CAST")
    private fun self(): SELF = (this as SELF)
}

abstract class ModelWithAttributes<SELF : ModelWithAttributes<SELF>> :
    AttributedModelOrPart<SELF>, AbstractModel<SELF>()

interface ExecutionContext

fun interface ValueSupplier<C : ExecutionContext, V : Any> : (C) -> V

data class ReifiedValueSupplier<C : ExecutionContext, V : Any>(
    val inClass: Class<out ExecutionContext>,
    val retClass: Class<V>,
    val delegate: ValueSupplier<C, V>,
) : ValueSupplier<C, V> {
    override fun invoke(context: C): V = delegate(context)
}
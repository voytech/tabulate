package io.github.voytech.tabulate.core.model

import io.github.voytech.tabulate.MultiIterationSet
import io.github.voytech.tabulate.core.*
import io.github.voytech.tabulate.core.layout.LayoutSpace
import io.github.voytech.tabulate.core.layout.SpaceConstraints
import io.github.voytech.tabulate.core.operation.*
import io.github.voytech.tabulate.plusAssign


@JvmInline
value class StateAttributes(val data: MutableMap<String, Any> = mutableMapOf()) {

    @Suppress("UNCHECKED_CAST")
    fun <S : Any> get(clazz: Class<S>): S? = data["state-${clazz.canonicalName}"] as S?

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

enum class ExportPhase {
    MEASURING,
    RENDERING
}

enum class ExportState {
    STARTED,
    ACTIVE,
    FINISHED
}

@JvmInline
value class StateMap(private val map: MutableMap<ExportPhase, ExportState> = mutableMapOf()) {
    operator fun invoke(phase: ExportPhase): ExportState = map[phase] ?: ExportState.STARTED

    operator fun invoke(phase: ExportPhase, state: ExportState) {
        map[phase] = state
    }

}

data class ExportContinuation(
    private val pushOnIteration: Int = 0,
    internal val attributes: Map<String, Any>,
) {
    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(key: String): T? = attributes[key] as T?

    private fun merge(other: Map<String, Any>) = copy(attributes = attributes + other)

}

class ExportContinuationQueue(
    private val continuations: MultiIterationSet<ExportContinuation, ExportPhase> = MultiIterationSet(),
) {
    val size: Int
        get() = continuations.size()

    operator fun plusAssign(element: ExportContinuation) {
        continuations += element
    }

    operator fun invoke(): MultiIterationSet<ExportContinuation, ExportPhase> = continuations
    operator fun get(phase: ExportPhase): ExportContinuation? = continuations.nextOrNull(phase)

}

class OperationsInContext(private val ctx: ModelExportContext) {
    val render: Operations<RenderingContext> by lazy { with(ctx.instance) { ctx.getExportOperations() } }
    val measure: Operations<RenderingContext> by lazy { with(ctx.instance) { ctx.getMeasuringOperations() } }

    fun render(context: AttributedContext): RenderingResult =
        render(ctx.instance.renderingContext, context).trySetOverflowsFlag()

    fun measure(context: AttributedContext): RenderingResult =
        measure(ctx.instance.renderingContext, context).trySetOverflowsFlag()

    fun renderOrMeasure(context: AttributedContext): RenderingResult =
        if (ctx.phase == ExportPhase.MEASURING) measure(context) else render(context)

    private fun RenderingResult.trySetOverflowsFlag(): RenderingResult = this.also {
        when (it.status) {
            is RenderingSkipped -> ctx.hasOverflows = true
            else -> {}
        }
    }

    fun hasMeasuringOperations(): Boolean = measure.isEmpty().not()
}

class ModelExportContext(
    @JvmSynthetic
    internal val instance: ExportInstance,
    val model: AbstractModel,
    val customStateAttributes: StateAttributes,
    val parentAttributes: Attributes? = null,
    var phase: ExportPhase = ExportPhase.RENDERING,
    val state: StateMap = StateMap()
) {

    val layouts: ModelContextLayouts = ModelContextLayouts(this)

    val operations: OperationsInContext by lazy { OperationsInContext(this) }

    private val continuations: ExportContinuationQueue = ExportContinuationQueue()

    internal var hasOverflows: Boolean = false

    fun scope(block: ExportApi.(ModelExportContext) -> Unit) {
        block(ExportApi(this), this)
    }

    fun newContinuation(vararg attributes: Pair<String, Any>): Unit = with(continuations()) {
        continuations += ExportContinuation(currentIndex(phase) + 1, attributes.toMap())
    }

    @JvmSynthetic
    internal fun resumeContinuation(): Unit = with(continuations()) {
        if (ExportState.STARTED != state(phase)) nextOrNull(phase)
    }

    fun <E : Any> getCurrentContinuationAttributeOrNull(key: String): E? = with(continuations()) {
        currentOrNull(phase)?.get(key)
    }

    fun getCurrentContinuationAttributesOrNull(key: String): Map<String, Any> = with(continuations()) {
        currentOrNull(phase)?.attributes ?: emptyMap()
    }

    fun hasPendingContinuations(): Boolean = continuations().currentIndex(phase) < continuations.size - 1

    fun haveChildrenPendingContinuations(): Boolean = navigate {
        checkAnyChildren { it.hasPendingContinuations() }
    }

    fun getCustomAttributes(): StateAttributes = customStateAttributes

    @JvmSynthetic
    internal fun setNextState() {
        if (state(phase) == ExportState.STARTED) state(phase, ExportState.ACTIVE)
        if (!isRunning()) {
            state(phase, ExportState.FINISHED)
        }
    }

    private fun needsToBeContinued(): Boolean = hasPendingContinuations() || haveChildrenPendingContinuations()

    fun isRunning(): Boolean = (state(phase) == ExportState.STARTED) || hasOverflows || needsToBeContinued()

    @JvmSynthetic
    internal fun setExporting() = apply {
        phase = ExportPhase.RENDERING
    }

    @JvmSynthetic
    internal fun setMeasuring() = apply {
        phase = ExportPhase.MEASURING
    }

    @JvmSynthetic
    internal fun <R> whenMeasuring(block: (ModelExportContext) -> R) {
        if (phase == ExportPhase.MEASURING) {
            block(this)
        }
    }

}

class ContinuationsApi internal constructor(private val context: ModelExportContext) {
    fun newContinuation(vararg attributes: Pair<String, Any>) {
        context.newContinuation(*attributes)
    }

    fun <E : Any> getCurrentContinuationAttributeOrNull(key: String): E? =
        context.getCurrentContinuationAttributeOrNull(key)

    fun getCurrentContinuationAttributesOrNull(key: String): Map<String, Any> =
        context.getCurrentContinuationAttributesOrNull(key)

    fun hasPendingContinuations(): Boolean = context.hasPendingContinuations()

    fun haveChildrenPendingContinuations(): Boolean = context.haveChildrenPendingContinuations()

}

class ExportApi private constructor(private val context: ModelExportContext) {
    fun AbstractModel.export(constraints: SpaceConstraints? = null, force: Boolean = false) {
        withinInitializedContext { exportInContext(it, constraints, force) }
    }

    fun AbstractModel.measure(constraints: SpaceConstraints? = null, force: Boolean = false): Size? =
        withinInitializedContext { measureInContext(it, constraints, force) }

    fun AbstractModel.isRunning(): Boolean = withinInitializedContext { it.isRunning() }

    fun render(renderable: AttributedContext): RenderingResult = with(context) {
        operations.render(renderable)
    }

    fun measure(renderable: AttributedContext): RenderingResult = with(context) {
        operations.measure(renderable)
    }

    fun renderOrMeasure(renderable: AttributedContext): RenderingResult = with(context) {
        operations.renderOrMeasure(renderable)
    }

    fun currentLayoutScope(): LayoutApi = context.layouts.current()

    fun currentLayoutSpace(): LayoutSpace = currentLayoutScope().space

    fun clearLayouts() = with(context.instance) { clearLayouts() }

    fun continuations(): ContinuationsApi = ContinuationsApi(context)

    fun <A : Attribute<A>> AbstractModel.getAttribute(attribute: Class<A>): A? =
        (this as? AttributeAware)?.attributes?.get(attribute)

    inline fun <reified A : Attribute<A>> AbstractModel.getAttribute(): A? =
        (this as? AttributeAware)?.attributes?.get(A::class.java)

    fun getCustomAttributes(): StateAttributes = context.getCustomAttributes()

    private fun exportInContext(
        targetContext: ModelExportContext, constraints: SpaceConstraints? = null, force: Boolean = false
    ) = targetContext.setExporting().run {
        if (isRunningOrRestarted(force)) {
            if (targetContext.shouldMeasure()) {
                measureInContext(targetContext, constraints).also {
                    targetContext.setExporting()
                }
            }
            resumeContinuation()
            targetContext.model(Method.PREPARE, targetContext)
            inNextLayoutScope(constraints) { targetContext.model(Method.EXPORT, targetContext) }
            targetContext.model(Method.FINISH, targetContext)
            setNextState()
        }
    }

    private fun measureInContext(
        targetContext: ModelExportContext, constraints: SpaceConstraints? = null, force: Boolean = false
    ): Size? = targetContext.setMeasuring().run {
        if (isRunningOrRestarted(force)) {
            inNextLayoutScope(constraints) {
                resumeContinuation()
                targetContext.model(Method.MEASURE, targetContext)
                setNextState()
            }
            targetContext.layouts.getMaxSize()
        } else targetContext.layouts.getMaxSize()
    }

    private fun ModelExportContext.isRunningOrRestarted(restart: Boolean = false): Boolean {
        if (restart) state(phase, ExportState.STARTED)
        // A model is still exporting/rendering when `hasOverflows` flag is set,
        // it is just STARTED or any continuation are registered on the tree down from here.
        if (!isRunning()) return false
        // Reset overflows flag. As long as overflows flag was previously set due to incomplete content render,
        // we may resume continuation or restart rendering once again,
        // but overflow flag should be examined once again during process.
        hasOverflows = false
        return true
    }

    private fun ModelExportContext.shouldMeasure(): Boolean {
        return model.needsMeasureBeforeExport && layouts.needsMeasuring() && operations.hasMeasuringOperations()
    }

    private fun <R> ModelExportContext.inNextLayoutScope(
        constraints: SpaceConstraints?, block: LayoutApi.() -> R
    ): R = layouts.ensuringNextLayout(
        SpaceConstraints(
            leftTop = constraints?.leftTop,
            maxRightBottom = constraints?.maxRightBottom,
        ), block
    )


    private fun <R> AbstractModel.withinInitializedContext(block: (ModelExportContext) -> R): R =
        block(ensuringExportContext().also { it.phase = context.phase })

    private fun AbstractModel.ensuringExportContext(): ModelExportContext = let { model ->
        context.navigate {
            useParentIfApplicable() ?: getChildContext(model) ?: createExportContext()
        }
    }

    private fun AbstractModel.useParentIfApplicable(): ModelExportContext? = let { model ->
        context.takeIf { context.model === model }?.also {
            model(Method.INITIALIZE, it)
        }
    }

    private fun AbstractModel.createExportContext(): ModelExportContext = let { model ->
        context.navigate {
            createChildContext(model).context.also {
                model(Method.INITIALIZE, it)
            }
        }
    }

    operator fun invoke(scope: ExportApi.() -> Unit) = scope(this)

    companion object {
        @JvmSynthetic
        internal operator fun invoke(context: ModelExportContext): ExportApi = ExportApi(context)
    }
}
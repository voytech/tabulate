package io.github.voytech.tabulate.core.model

import io.github.voytech.tabulate.core.*
import io.github.voytech.tabulate.core.InputParams.Companion.allowMeasureBeforeRender
import io.github.voytech.tabulate.core.layout.*
import io.github.voytech.tabulate.core.model.attributes.HorizontalOverflowAttribute
import io.github.voytech.tabulate.core.model.attributes.VerticalOverflowAttribute
import io.github.voytech.tabulate.core.model.overflow.Overflow
import io.github.voytech.tabulate.core.operation.*

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

    operator fun plusAssign(other: StateAttributes) = plusAssign(other.data)

    operator fun plusAssign(other: Map<String, Any>) {
        other.forEach { (key, value) -> data[key] = value }
    }

    operator fun plus(other: Map<String, Any>): StateAttributes =
        apply { other.forEach { (key, value) -> this[key] = value } }

    inline fun <reified E : ExecutionContext> addExecutionContext(executionContext: E) {
        data["executionContext-${E::class.java.canonicalName}"] = executionContext
    }

    @Suppress("UNCHECKED_CAST")
    fun <E : ExecutionContext> getExecutionContext(clazz: Class<E>): E? =
        data["executionContext-${clazz.canonicalName}"] as E?

    inline fun <reified E : ExecutionContext> getExecutionContext(): E? = getExecutionContext(E::class.java)

    inline fun <reified E : ExecutionContext> ensureExecutionContext(provider: () -> E): E =
        getExecutionContext() ?: run { addExecutionContext(provider()); getExecutionContext()!! }

    inline fun <reified E : ExecutionContext> removeExecutionContext(): E? =
        data.remove("executionContext-${E::class.java.canonicalName}") as E?

    @Suppress("UNCHECKED_CAST")
    fun <C : ExecutionContext, R : Any> ReifiedValueSupplier<C, R>.value(): R? =
        getExecutionContext(inClass)?.let { ctx -> this(ctx as C) }
}

fun MutableMap<String, Any>.stateAttributes(): StateAttributes = StateAttributes(this)

fun StateAttributes?.ensure() = this ?: StateAttributes(mutableMapOf())

enum class Phase {
    MEASURING,
    RENDERING
}

enum class ExportState {
    STARTED,
    ACTIVE,
    FINISHED
}

@JvmInline
value class StateMap(private val map: MutableMap<Phase, ExportState> = mutableMapOf()) {
    operator fun invoke(phase: Phase): ExportState = map[phase] ?: ExportState.STARTED

    operator fun invoke(phase: Phase, state: ExportState) {
        map[phase] = state
    }

}

class OperationsInContext(private val ctx: ModelExportContext) {
    val render: Operations<RenderingContext> by lazy { with(ctx.instance) { ctx.getExportOperations() } }
    val measure: Operations<RenderingContext> by lazy { with(ctx.instance) { ctx.getMeasuringOperations() } }

    fun render(context: AttributedEntity): RenderingResult = render(ctx.instance.renderingContext, context)

    fun measure(context: AttributedEntity): RenderingResult = measure(ctx.instance.renderingContext, context)

    fun renderOrMeasure(context: AttributedEntity): RenderingResult =
        if (ctx.phase == Phase.MEASURING) measure(context) else render(context)

    fun hasMeasuringOperations(): Boolean = measure.isEmpty().not()
}

class ModelExportContext(
    @JvmSynthetic
    internal val instance: ExportInstance,
    val model: AbstractModel,
    val customStateAttributes: StateAttributes,
    val parentAttributes: Attributes? = null,
    var phase: Phase = Phase.RENDERING,
    val state: StateMap = StateMap()
) {

    val depth: Int by lazy { parents().size }

    val operations: OperationsInContext by lazy { OperationsInContext(this) }

    @JvmSynthetic
    internal val exportIterations: ExportIterations = ExportIterations(this)

    @get:JvmSynthetic
    internal val activeIteration: ExportIteration
        get() = exportIterations.current

    @get:JvmSynthetic
    internal val activeLayoutContext: ModelContextLayout
        get() = activeIteration.layoutContext

    @get:JvmSynthetic
    internal val activeLayout: Layout
        get() = activeLayoutContext.layout

    fun api(block: ExportApi.(ModelExportContext) -> Unit) {
        block(ExportApi(this), this)
    }

    fun <E : Any> getCurrentIterationAttributeOrNull(key: String): E? =
        exportIterations.getCurrentIterationAttributeOrNull(key)

    fun getCurrentIterationAttributesOrNull(): Map<String, Any> =
        exportIterations.getCurrentIterationAttributesOrNull()


    fun getCustomAttributes(): StateAttributes = customStateAttributes

    @JvmSynthetic
    internal fun setNextState() {
        if (state(phase) == ExportState.STARTED) state(phase, ExportState.ACTIVE)
        if (!hasAnyIteration()) {
            state(phase, ExportState.FINISHED)
        }
    }

    @JvmSynthetic
    internal fun hasAnyIteration(): Boolean =
        exportIterations.hasAnyIteration() || exportIterations.haveAnyChildAnyIteration()

    fun isRunning(): Boolean = state(phase) != ExportState.FINISHED

    @JvmSynthetic
    internal fun setExporting() = apply {
        phase = Phase.RENDERING
    }

    @JvmSynthetic
    internal fun setMeasuring() = apply {
        phase = Phase.MEASURING
    }

}

@Suppress("MemberVisibilityCanBePrivate")
class ExportIterationsApi internal constructor(private val context: ModelExportContext) {

    fun appendPending(vararg attributes: Pair<String, Any>) {
        context.exportIterations.appendIteration(attributes.toMap())
    }

    fun appendPending(attributes: Map<String, Any>) {
        context.exportIterations.appendIteration(attributes)
    }

    fun prependPending(attributes: Map<String, Any>) {
        context.exportIterations.prependIteration(attributes)
    }

    fun appendAttributes(vararg attributes: Pair<String, Any>) {
        context.exportIterations.appendAttributes(*attributes)
    }

    fun stop() = with(context.exportIterations) {
        discardScheduled()
        clearAttributes()
        setDryRunForActive()
    }

    fun retry() = with(context.exportIterations) {
        discardScheduled()
        prependPending(clearAttributes())
        setDryRunForActive()
    }

    fun finish() = with(context.exportIterations) {
        discardScheduled()
        setDryRunForActive()
    }

    fun <E : Any> getCurrentIterationAttributeOrNull(key: String): E? =
        context.getCurrentIterationAttributeOrNull(key)

    fun AbstractModel.getOverflowHandlingStrategy(result: RenderingResult): Overflow? =
        (result.status as? AxisBoundStatus)?.let {
            if (it.activeAxis == Axis.X) {
                getAttribute<HorizontalOverflowAttribute>()?.overflow
            } else {
                getAttribute<VerticalOverflowAttribute>()?.overflow
            }
        }

    fun AbstractModel.catchOverflow(result: RenderingResult) {
        val overflow = getOverflowHandlingStrategy(result)
        when (result.status) {
            is RenderingClipped -> {
                when (overflow) {
                    Overflow.STOP -> stop()
                    Overflow.FINISH -> finish()
                    else -> {}
                }
            }

            is RenderingSkipped -> {
                when (overflow) {
                    Overflow.RETRY, null -> retry()
                    Overflow.STOP -> stop()
                    else -> {}
                }
            }

            else -> {}
        }
    }

}

class ExportApi private constructor(private val context: ModelExportContext) {

    fun AbstractModel.export(constraints: RegionConstraints? = null, force: Boolean = false) {
        withinInitializedContext { exportInContext(it, constraints, force) }
    }

    fun AbstractModel.measure(constraints: RegionConstraints? = null, force: Boolean = false): Size =
        withinInitializedContext { measureInContext(it, constraints, force) }

    fun AbstractModel.isRunning(): Boolean = withinInitializedContext { it.isRunning() }

    fun render(renderable: AttributedEntity): RenderingResult = with(context) {
        operations.render(renderable)
    }

    fun measure(renderable: AttributedEntity): RenderingResult = with(context) {
        operations.measure(renderable)
    }

    fun renderOrMeasure(renderable: AttributedEntity): RenderingResult = with(context) {
        operations.renderOrMeasure(renderable)
    }

    fun currentLayout(): Layout = context.exportIterations.getCurrentLayout()

    fun iterations(): ExportIterationsApi = ExportIterationsApi(context)

    fun iterations(block: ExportIterationsApi.() -> Unit) = iterations().apply(block)

    fun <LP : Layout> layout(block: LP.() -> Unit) {
        context.exportIterations.current.layout().apply {
            @Suppress("UNCHECKED_CAST")
            block(this as LP)
        }
    }

    fun getCustomAttributes(): StateAttributes = context.getCustomAttributes()

    private fun exportInContext(
        target: ModelExportContext, constraints: RegionConstraints? = null, force: Boolean = false
    ) = target.setExporting().run {
        if (target.isRunningOrRestarted(force)) {
            var measuringLeftTop: Position? = null
            if (target.shouldMeasure()) {
                measureInContext(target, constraints).also {
                    measuringLeftTop = target.activeLayout.getMaxBoundingRectangle().leftTop
                    target.setExporting()
                }
            }
            target.exportIterations.executeIteration(constraints.ensure(measuringLeftTop)) {
                target.traceIteration("[ITERATION START]")
                target.model(Method.EXPORT, target)
            }
            target.setNextState()
            target.traceIteration("[ITERATION END]")
        }
    }

    private fun RegionConstraints?.ensure(
        leftTop: Position? = null,
        maxRightBottom: Position? = null
    ): RegionConstraints = RegionConstraints(
        leftTop = this?.leftTop ?: leftTop,
        maxRightBottom = this?.maxRightBottom ?: maxRightBottom,
    )

    private fun measureInContext(
        target: ModelExportContext, constraints: RegionConstraints? = null, force: Boolean = false
    ): Size = target.setMeasuring().run {
        if (target.isRunningOrRestarted(force)) {
            target.exportIterations.executeIteration(constraints.ensure()) {
                target.traceIteration("[ITERATION START]")
                target.model(Method.MEASURE, target)
            }
            target.setNextState()
            target.traceIteration("[ITERATION END]")
            target.activeLayoutContext.getMaxSize()
        } else target.activeLayoutContext.getMaxSize()
    }

    private fun ModelExportContext.isRunningOrRestarted(restart: Boolean = false): Boolean {
        if (restart) state(phase, ExportState.STARTED)
        // it is just STARTED or any continuation are registered on the tree down from here.
        return (isRunning() || hasAnyIteration())
            .also { traceSection("${this@isRunningOrRestarted.model}, isRunningOrRestarted: $it") }
    }

    private fun ModelExportContext.shouldMeasure(): Boolean {
        return model.needsMeasureBeforeExport &&
                !exportIterations.hasAnyMeasuredIteration() &&
                customStateAttributes.allowMeasureBeforeRender()
    }

    private fun <R> AbstractModel.withinInitializedContext(block: (ModelExportContext) -> R): R =
        ensuringExportContext().let { targetContext ->
            targetContext.phase = context.phase
            targetContext.markDepth()
            block(targetContext).also { unsetDepth() }
        }

    private fun AbstractModel.ensuringExportContext(): ModelExportContext = let { model ->
        context.treeNode {
            useParentIfApplicable() ?: getChildContext(model) ?: createExportContext()
        }
    }

    private fun AbstractModel.useParentIfApplicable(): ModelExportContext? = let { model ->
        context.takeIf { context.model === model }?.also {
            model(Method.CONTEXT_CREATED, it)
        }
    }

    private fun AbstractModel.createExportContext(): ModelExportContext = let { model ->
        context.treeNode {
            createChildContext(model).context.also {
                model(Method.CONTEXT_CREATED, it)
            }
        }
    }

    operator fun invoke(scope: ExportApi.() -> Unit) = scope(this)

    companion object {
        @JvmSynthetic
        internal operator fun invoke(context: ModelExportContext): ExportApi = ExportApi(context)
    }
}


fun <L : Layout> ExportApi.exportWithContinuations(models: List<AbstractModel>, mode: DescendantsIterationsKind) =
    when (mode) {
        DescendantsIterationsKind.POSTPONED -> exportWithPostponedContinuations<L>(models)
        DescendantsIterationsKind.IMMEDIATE -> exportWithImmediateContinuations<L>(models)
        else -> error("Currently only POSTPONED and IMMEDIATE iterations kind are implemented")
    }

fun <L : Layout> ExportApi.measureWithContinuations(models: List<AbstractModel>, mode: DescendantsIterationsKind) =
    when (mode) {
        DescendantsIterationsKind.POSTPONED -> measureWithPostponedContinuations<L>(models)
        DescendantsIterationsKind.IMMEDIATE -> measureWithImmediateContinuations<L>(models)
        else -> error("Currently only POSTPONED and IMMEDIATE iterations kind are implemented")
    }

fun <L : Layout> ExportApi.exportWithPostponedContinuations(models: List<AbstractModel>) =
    traverseAllThenContinue<L>(models) { export() }

fun <L : Layout> ExportApi.measureWithPostponedContinuations(models: List<AbstractModel>) =
    traverseAllThenContinue<L>(models) { measure() }

private fun <L : Layout> ExportApi.traverseAllThenContinue(
    models: List<AbstractModel>,
    action: AbstractModel.() -> Unit
) = layout<L> {
    if (this is AutonomousLayout) {
        var runnables = models
        while (runnables.isNotEmpty() && hasSpaceLeft()) {
            runnables.forEach {
                if (hasSpaceLeft()) {
                    it.action()
                }
            }
            runnables = runnables.filter { it.isRunning() }
        }
    }
}

fun <L : Layout> ExportApi.exportWithImmediateContinuations(models: List<AbstractModel>) =
    traverseWithContinuations<L>(models) { export() }


fun <L : Layout> ExportApi.measureWithImmediateContinuations(models: List<AbstractModel>) =
    traverseWithContinuations<L>(models) { measure() }

private fun <L : Layout> ExportApi.traverseWithContinuations(
    models: List<AbstractModel>,
    action: AbstractModel.() -> Unit
) = layout<L> {
    if (this is AutonomousLayout) {
        models.forEach {
            while (it.isRunning() && hasSpaceLeft()) {
                it.action()
            }
        }
    }
}
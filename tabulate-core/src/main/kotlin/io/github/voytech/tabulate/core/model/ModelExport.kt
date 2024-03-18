package io.github.voytech.tabulate.core.model

import io.github.voytech.tabulate.MultiIterationSet
import io.github.voytech.tabulate.core.*
import io.github.voytech.tabulate.core.InputParams.Companion.allowMeasureBeforeRender
import io.github.voytech.tabulate.core.layout.*
import io.github.voytech.tabulate.core.model.attributes.HorizontalOverflowAttribute
import io.github.voytech.tabulate.core.model.attributes.VerticalOverflowAttribute
import io.github.voytech.tabulate.core.model.overflow.Overflow
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

data class RenderIteration(
    internal val pushOnIteration: Int = 0,
    internal val attributes: MutableMap<String, Any>
) {
    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(key: String): T? = attributes[key] as T?

    internal fun merge(other: Map<String, Any>) {
        attributes += other
    }

}

class RenderIterations(
    private val iterations: MultiIterationSet<RenderIteration, ExportPhase> = MultiIterationSet(),
) {
    val size: Int
        get() = iterations.size()

    private fun findCollision(step: Int, attributes: Map<String, Any>): RenderIteration? = with(iterations) {
        find { iteration ->
            iteration.pushOnIteration == step && attributes.any { iteration.attributes.containsKey(it.key) }
        }
    }

    fun appendPending(phase: ExportPhase, vararg attributes: Pair<String, Any>) {
        appendPending(phase, attributes.toMap())
    }

    fun appendPending(phase: ExportPhase, attributes: Map<String, Any>) = with(iterations) {
        val step = currentIndex(phase) + 1
        val attribs = attributes.toMutableMap()
        if (findCollision(step, attribs) == null) {
            iterations += RenderIteration(step, attribs)
        }
    }

    fun prependPending(phase: ExportPhase, attributes: Map<String, Any>) = with(iterations) {
        val step = currentIndex(phase) + 1
        if (findCollision(step, attributes) == null) {
            iterations.insert(phase, RenderIteration(step, attributes.toMutableMap()))
        }
    }

    fun appendAttributes(phase: ExportPhase, vararg attributes: Pair<String, Any>) {
        iterations.currentOrNull(phase)?.merge(attributes.toMap())
    }

    fun clearAttributes(phase: ExportPhase): Map<String, Any> =
        iterations.currentOrNull(phase)?.run {
            attributes.toMap().also { attributes.clear() }
        } ?: emptyMap()

    fun discardPending() {
        iterations.deleteUntouched()
    }

    fun resume(phase: ExportPhase) {
        preventNewItems(false)
        if (size == 0) appendPending(phase)
        iterations.nextOrNull(phase)
    }

    fun <E : Any> getCurrentIterationAttributeOrNull(phase: ExportPhase, key: String): E? =
        iterations.currentOrNull(phase)?.get(key)

    fun getCurrentIterationAttributesOrNull(phase: ExportPhase): Map<String, Any> =
        iterations.currentOrNull(phase)?.attributes ?: emptyMap()

    private operator fun plusAssign(element: RenderIteration) {
        iterations += element
    }

    operator fun invoke(): MultiIterationSet<RenderIteration, ExportPhase> = iterations

    operator fun get(phase: ExportPhase): RenderIteration? = iterations.nextOrNull(phase)
    fun preventNewItems(prevent: Boolean = true) {
        iterations.preventNewItems(prevent)
    }

}

class OperationsInContext(private val ctx: ModelExportContext) {
    val render: Operations<RenderingContext> by lazy { with(ctx.instance) { ctx.getExportOperations() } }
    val measure: Operations<RenderingContext> by lazy { with(ctx.instance) { ctx.getMeasuringOperations() } }

    fun render(context: AttributedContext): RenderingResult = render(ctx.instance.renderingContext, context)

    fun measure(context: AttributedContext): RenderingResult = measure(ctx.instance.renderingContext, context)

    fun renderOrMeasure(context: AttributedContext): RenderingResult =
        if (ctx.phase == ExportPhase.MEASURING) measure(context) else render(context)

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

    @JvmSynthetic
    internal val renderIterations: RenderIterations = RenderIterations()

    internal var hasOverflows: Boolean = false

    fun api(block: ExportApi.(ModelExportContext) -> Unit) {
        block(ExportApi(this), this)
    }

    @JvmSynthetic
    internal fun resumeRenderIteration() {
        renderIterations.resume(phase)
    }

    fun <E : Any> getCurrentIterationAttributeOrNull(key: String): E? =
        renderIterations.getCurrentIterationAttributeOrNull(phase, key)

    fun getCurrentIterationAttributesOrNull(): Map<String, Any> =
        renderIterations.getCurrentIterationAttributesOrNull(phase)

    fun hasPendingIterations(): Boolean = renderIterations().currentIndex(phase) < renderIterations.size - 1

    fun haveChildrenPendingIterations(): Boolean = navigate {
        checkAnyChildren { it.hasPendingIterations() }
    }

    fun getCustomAttributes(): StateAttributes = customStateAttributes

    @JvmSynthetic
    internal fun setNextState() {
        if (state(phase) == ExportState.STARTED) state(phase, ExportState.ACTIVE)
        if (!isRunning()) {
            state(phase, ExportState.FINISHED)
        }
    }

    private fun needsToBeContinued(): Boolean = hasPendingIterations() || haveChildrenPendingIterations()

    fun isRunning(): Boolean = (state(phase) == ExportState.STARTED) || hasOverflows || needsToBeContinued()

    @JvmSynthetic
    internal fun setExporting() = apply {
        phase = ExportPhase.RENDERING
    }

    @JvmSynthetic
    internal fun setMeasuring() = apply {
        phase = ExportPhase.MEASURING
    }

}

@Suppress("MemberVisibilityCanBePrivate")
class RenderIterationsApi internal constructor(private val context: ModelExportContext) {

    fun appendPending(vararg attributes: Pair<String, Any>) {
        context.renderIterations.appendPending(context.phase, *attributes)
    }

    fun appendPending(attributes: Map<String, Any>) {
        context.renderIterations.appendPending(context.phase, attributes)
    }

    fun prependPending(attributes: Map<String, Any>) {
        context.renderIterations.prependPending(context.phase, attributes)
    }

    fun appendAttributes(vararg attributes: Pair<String, Any>) {
        context.renderIterations.appendAttributes(context.phase, *attributes)
    }

    private fun clearAttributes(): Map<String, Any> =
        context.renderIterations.clearAttributes(context.phase)

    private fun moveToPending() {
        prependPending(clearAttributes())
    }

    private fun discardPending() {
        context.renderIterations.discardPending()
    }

    private fun preventNewItems() {
        context.renderIterations.preventNewItems()
    }

    fun stop() {
        discardPending()
        clearAttributes()
        preventNewItems()
        appendAttributes("_stop" to true)
    }

    fun retry() {
        discardPending()
        moveToPending()
        preventNewItems()
        appendAttributes("_retry" to true)
    }

    fun finish() {
        discardPending()
        preventNewItems()
        appendAttributes("_stop" to true)
    }

    fun <E : Any> getCurrentIterationAttributeOrNull(key: String): E? =
        context.getCurrentIterationAttributeOrNull(key)

    fun getCurrentIterationAttributesOrNull(): Map<String, Any> = context.getCurrentIterationAttributesOrNull()

    fun hasPendingIterations(): Boolean = context.hasPendingIterations()

    fun haveChildrenPendingIterations(): Boolean = context.haveChildrenPendingIterations()

    private fun AbstractModel.getOverflowHandlingStrategy(result: RenderingResult): Overflow? =
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

    fun AbstractModel.export(constraints: SpaceConstraints? = null, force: Boolean = false) {
        withinInitializedContext { exportInContext(it, constraints, force) }
    }

    fun AbstractModel.measure(constraints: SpaceConstraints? = null, force: Boolean = false): Size? =
        withinInitializedContext { measureInContext(it, constraints, force) }

    fun AbstractModel.currentSizeOrMeasure(constraints: SpaceConstraints? = null, force: Boolean = false): Size? =
        withinInitializedContext { getSizeIfMeasured(it) ?: measureInContext(it, constraints, force) }

    fun AbstractModel.getSize(): Size? = withinInitializedContext { getSizeIfMeasured(it) }

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

    fun clearAllLayouts() = with(context.instance) { clearAllLayouts() }

    fun iterations(): RenderIterationsApi = RenderIterationsApi(context)

    fun iterations(block: RenderIterationsApi.() -> Unit) = iterations().apply(block)

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
            resumeRenderIteration()
            targetContext.model(Method.BEFORE_LAYOUT, targetContext)
            inLayoutScope(constraints) { targetContext.model(Method.EXPORT, targetContext) }
            targetContext.model(Method.FINISH, targetContext)
            setNextState()
        }
    }

    private fun measureInContext(
        targetContext: ModelExportContext, constraints: SpaceConstraints? = null, force: Boolean = false
    ): Size? = targetContext.setMeasuring().run {
        if (isRunningOrRestarted(force)) {
            inLayoutScope(constraints) {
                resumeRenderIteration()
                targetContext.model(Method.MEASURE, targetContext)
                setNextState()
            }
            targetContext.layouts.getMaxSize()
        } else targetContext.layouts.getMaxSize()
    }

    private fun getSizeIfMeasured(targetContext: ModelExportContext): Size? {
        return targetContext.layouts.getMaxSize()
    }

    private fun ModelExportContext.isRunningOrRestarted(restart: Boolean = false): Boolean {
        if (restart) state(phase, ExportState.STARTED)
        // it is just STARTED or any continuation are registered on the tree down from here.
        return isRunning()
    }

    private fun ModelExportContext.shouldMeasure(): Boolean {
        return model.needsMeasureBeforeExport && layouts.needsMeasuring() && customStateAttributes.allowMeasureBeforeRender()
    }

    private fun <R> ModelExportContext.inLayoutScope(
        constraints: SpaceConstraints?, block: LayoutApi.() -> R
    ): R = layouts.ensuringLayout(
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
            model(Method.EXPORT_CONTEXT_CREATED, it)
        }
    }

    private fun AbstractModel.createExportContext(): ModelExportContext = let { model ->
        context.navigate {
            createChildContext(model).context.also {
                model(Method.EXPORT_CONTEXT_CREATED, it)
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
) =
    currentLayoutScope().layout<L, Unit> { space ->
        if (this is AutonomousLayout) {
            var runnables = models
            while (runnables.isNotEmpty() && space.hasSpaceLeft()) {
                runnables.forEach {
                    if (space.hasSpaceLeft()) {
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
) =
    currentLayoutScope().layout<L, Unit> { space ->
        if (this is AutonomousLayout) {
            models.forEach {
                while (it.isRunning() && space.hasSpaceLeft()) {
                    it.action()
                }
            }
        }
    }
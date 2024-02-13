package io.github.voytech.tabulate.core.model

import io.github.voytech.tabulate.MultiIterationSet
import io.github.voytech.tabulate.core.*
import io.github.voytech.tabulate.core.InputParams.Companion.allowMeasureBeforeRender
import io.github.voytech.tabulate.core.layout.AutonomousLayout
import io.github.voytech.tabulate.core.layout.Layout
import io.github.voytech.tabulate.core.layout.LayoutSpace
import io.github.voytech.tabulate.core.layout.SpaceConstraints
import io.github.voytech.tabulate.core.operation.*
import io.github.voytech.tabulate.plusAssign
import java.util.logging.Logger


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

    fun newRenderIteration(phase: ExportPhase, vararg attributes: Pair<String, Any>) = with(iterations) {
        val step = currentIndex(phase) + 1
        val attribs = attributes.toMap().toMutableMap()
        if (findCollision(step, attribs) == null) {
            iterations += RenderIteration(step, attribs)
        }
    }

    fun insertAsNextRenderIteration(phase: ExportPhase, attributes: Map<String, Any>) = with(iterations) {
        val step = currentIndex(phase) + 1
        if (findCollision(step, attributes) == null) {
            iterations.insert(phase, RenderIteration(step, attributes.toMutableMap()))
        }
    }

    fun resumeRenderIteration(phase: ExportPhase) {
        if (size == 0) newRenderIteration(phase)
        iterations.nextOrNull(phase)
    }

    fun appendAttributes(phase: ExportPhase, vararg attributes: Pair<String, Any>) {
        iterations.currentOrNull(phase)?.merge(attributes.toMap())
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

    private val renderIterations: RenderIterations = RenderIterations()

    internal var hasOverflows: Boolean = false

    fun scope(block: ExportApi.(ModelExportContext) -> Unit) {
        block(ExportApi(this), this)
    }

    fun newRenderIteration(vararg attributes: Pair<String, Any>) {
        renderIterations.newRenderIteration(phase, *attributes)
    }

    fun insertAsNextRenderIteration(attributes: Map<String, Any>) {
        renderIterations.insertAsNextRenderIteration(phase, attributes)
    }

    fun appendAttributes(vararg attributes: Pair<String, Any>) {
        renderIterations.appendAttributes(phase, *attributes)
    }

    @JvmSynthetic
    internal fun resumeRenderIteration() {
        renderIterations.resumeRenderIteration(phase)
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

    @JvmSynthetic
    internal fun <R> whenMeasuring(block: (ModelExportContext) -> R) {
        if (phase == ExportPhase.MEASURING) {
            block(this)
        }
    }

}

class RenderIterationsApi internal constructor(private val context: ModelExportContext) {

    fun newRenderIteration(vararg attributes: Pair<String, Any>) {
        context.newRenderIteration(*attributes)
    }

    fun insertAsNextRenderIteration(attributes: Map<String, Any>) {
        context.insertAsNextRenderIteration(attributes)
    }

    fun appendAttributes(vararg attributes: Pair<String, Any>) {
        context.appendAttributes(*attributes)
    }

    fun <E : Any> getCurrentIterationAttributeOrNull(key: String): E? =
        context.getCurrentIterationAttributeOrNull(key)

    fun getCurrentIterationAttributesOrNull(): Map<String, Any> = context.getCurrentIterationAttributesOrNull()

    fun hasPendingIterations(): Boolean = context.hasPendingIterations()

    fun haveChildrenPendingIterations(): Boolean = context.haveChildrenPendingIterations()

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
            resumeRenderIteration()
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
                resumeRenderIteration()
                targetContext.model(Method.MEASURE, targetContext)
                setNextState()
            }
            targetContext.layouts.getMaxSize()
        } else targetContext.layouts.getMaxSize()
    }

    private fun getSizeIfMeasured(targetContext: ModelExportContext): Size? {
        val phase = targetContext.phase
        targetContext.phase = ExportPhase.MEASURING
        return targetContext.layouts.getMaxSize().also { targetContext.phase = phase }
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
        return model.needsMeasureBeforeExport && layouts.needsMeasuring() && customStateAttributes.allowMeasureBeforeRender()
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

fun <L : Layout> ExportApi.exportWithPostponedContinuations(models: List<AbstractModel>) =
    traverseAllThenContinue<L>(models) { export() }

fun <L : Layout> ExportApi.measureWithPostponedContinuations(models: List<AbstractModel>) =
    traverseAllThenContinue<L>(models) { measure() }

private fun <L : Layout> ExportApi.traverseAllThenContinue(models: List<AbstractModel>,  action: AbstractModel.() -> Unit) =
    currentLayoutScope().layout<L,Unit> { space ->
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


fun <L : Layout> ExportApi.measureWithImmediateContinuation(models: List<AbstractModel>) =
    traverseWithContinuations<L>(models) { measure() }

private fun <L : Layout> ExportApi.traverseWithContinuations(models: List<AbstractModel>,  action: AbstractModel.() -> Unit) =
    currentLayoutScope().layout<L,Unit> { space ->
        if (this is AutonomousLayout) {
            models.forEach {
                while (it.isRunning() && space.hasSpaceLeft()) {
                    it.action()
                }
            }
        }
    }
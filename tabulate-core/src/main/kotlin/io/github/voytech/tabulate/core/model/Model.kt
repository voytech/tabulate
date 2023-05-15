package io.github.voytech.tabulate.core.model

import io.github.voytech.tabulate.DefaultComplexIterator
import io.github.voytech.tabulate.ResettableComplexIterator
import io.github.voytech.tabulate.core.template.*
import io.github.voytech.tabulate.core.template.Navigation.Companion.rootNavigation
import io.github.voytech.tabulate.core.template.layout.LayoutConstraints
import io.github.voytech.tabulate.core.template.layout.LayoutPolicy
import io.github.voytech.tabulate.core.template.layout.policy.SimpleLayoutPolicy
import io.github.voytech.tabulate.core.template.operation.AttributedContext
import io.github.voytech.tabulate.core.template.operation.OperationResult
import mu.KLogging
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
value class StateAttributes(val data: MutableMap<String, Any> = mutableMapOf()) {

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

interface ContinuationAttributes

data class SimpleContinuationAttributes(
    private val attributes: MutableMap<String, Any> = mutableMapOf(),
) : ContinuationAttributes {

    operator fun set(key: String, value: Any) {
        attributes[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String): T? = attributes[key] as T?
}

class ExportContinuationQueue<ATTR : ContinuationAttributes>(
    private val continuations: MutableList<ATTR> = mutableListOf(),
    private val iterators: ResettableComplexIterator<ATTR, ExportPhase> = DefaultComplexIterator(continuations),
) {
    operator fun plusAssign(element: ATTR) {
        continuations += element
    }

    operator fun invoke(): ResettableComplexIterator<ATTR, ExportPhase> = iterators

    operator fun get(phase: ExportPhase): ATTR? = iterators.nextOrNull(phase)

}

enum class ExportPhase {
    MEASURING,
    RENDERING
}

enum class ExportStatus {
    ACTIVE,
    OVERFLOWED,
    SUSPENDED,
    FINISHED;

    internal fun isExporting(): Boolean = this != FINISHED

    internal fun isPartlyExported(): Boolean = this == SUSPENDED || this == OVERFLOWED

    fun isXOverflow(): Boolean = this == OVERFLOWED

}

class ModelExportContext(
    val instance: ExportInstance,
    val navigation: Navigation,
    val layouts: Layouts,
    val customStateAttributes: StateAttributes,
    val parentAttributes: Attributes? = null,
    val modelAttributes: Attributes? = null,
    val continuations: ExportContinuationQueue<SimpleContinuationAttributes> = ExportContinuationQueue(),
    var status: ExportStatus = ExportStatus.ACTIVE,
    var phase: ExportPhase = ExportPhase.RENDERING,
) {

    val renderingContext: RenderingContext
        get() = instance.renderingContext

    internal var layoutConstraints: LayoutConstraints? = null

    fun getCustomAttributes(): MutableMap<String, Any> = customStateAttributes.data


    fun currentLayout(): NavigableLayout = layouts().current(phase).layout

    private fun hasPartlyExportedChildren(): Boolean =
        navigation.checkAnyChildren { it.context.isPartlyExported() }

    fun finishOrSuspend() {
        if (!status.isExporting() && !hasPartlyExportedChildren()) {
            status = ExportStatus.FINISHED
        }
    }

    fun isExporting(): Boolean = status.isExporting()

    fun isPartlyExported(): Boolean = status.isPartlyExported()

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

    internal lateinit var context: ModelExportContext

    private fun ExportInstance.shouldMeasure(): Boolean {
        return planSpaceOnExport && !getMeasuringOperations(self()).isEmpty()
    }

    private fun <R> withinInitializedContext(parent: ModelExportContext, block: (ModelExportContext) -> R): R =
        ensuringExportContext(parent).run { block(this) }

    private fun ModelExportContext.withLayoutConstraints(constraints: LayoutConstraints? = null): ModelExportContext =
        apply {
            if (constraints != null) {
                layoutConstraints = constraints
            }
        }

    fun export(parentContext: ModelExportContext, constraints: LayoutConstraints? = null) {
        with(parentContext.instance) {
            withinInitializedContext(parentContext) {
                if (!it.isExporting()) return@withinInitializedContext
                if (shouldMeasure()) measure(parentContext)
                it.phase = ExportPhase.RENDERING
                it.continuations().nextOrNull(it.phase)
                prepareExport(it)
                it.withLayoutConstraints(constraints).let { ctx ->
                    withinLayoutScope {
                        doExport(ctx)
                    }
                }
                finishExport(it)
                it.finishOrSuspend()
            }
        }
    }

    fun exportResuming(parentContext: ModelExportContext, constraints: LayoutConstraints? = null) {
        withinInitializedContext(parentContext) {
            while (it.isExporting()) {
                export(parentContext, constraints)
            }
        }
    }

    //TODO make safe scope (when calling within doExport:, export, and so on should be all forbidden calls)
    protected open fun prepareExport(exportContext: ModelExportContext) {
        logger.warn { "Model.prepareExport hook not implemented" }
    }

    //TODO make safe scope (when calling within doExport: , export, and so on should be all forbidden calls)
    protected abstract fun doExport(exportContext: ModelExportContext)

    //TODO make safe scope (when calling within doExport: , export, and so on should be all forbidden calls)
    protected open fun finishExport(exportContext: ModelExportContext) {
        logger.warn { "Model.finishExport hook not implemented" }
    }

    //TODO on measure - should call probably the same logic as export and resumption but should inject measurement operations instead of export operations. We should not be able to use different exporting logic for those two paths.
    fun measure(parentContext: ModelExportContext, constraints: LayoutConstraints? = null): SomeSize =
        withinInitializedContext(parentContext) {
            it.phase = ExportPhase.MEASURING
            it.continuations().nextOrNull(it.phase)
            it.withLayoutConstraints(constraints).let { ctx ->
                withinLayoutScope {
                    takeMeasures(ctx)
                    policy.run { layout.setMeasured() }
                    layout.boundingRectangle.let { size ->
                        SomeSize(size.getWidth(), size.getHeight())
                    }
                }
            }
        }

    private fun createExportContext(parentContext: ModelExportContext): ModelExportContext =
        with(parentContext) {
            navigation.addChild(self())
            ModelExportContext(
                instance,
                Navigation(navigation.root, navigation.active.takeIf { navigation.root != self() }, self()),
                Layouts(::layoutPolicy),
                customStateAttributes,
                parentAttributes
            ).also(::initialize)
        }

    @JvmSynthetic
    internal fun createStandaloneExportContext(
        instance: ExportInstance,
        attributes: StateAttributes? = null,
    ): ModelExportContext =
        ModelExportContext(instance, rootNavigation(this), Layouts(::layoutPolicy), attributes.orEmpty())

    private fun ensuringExportContext(parentContext: ModelExportContext): ModelExportContext =
        if (::context.isInitialized) context else run { context = createExportContext(parentContext);context }

    //TODO make safe scope (when calling within initialize: , doResume, resume, export, and so on should be all forbidden calls)
    protected open fun initialize(exportContext: ModelExportContext) {
        logger.warn { "Model.initialize not implemented" }
    }

    //TODO make safe scope (when calling within takeMeasures: , doResume, resume, export, and so on should be all forbidden calls)
    protected open fun takeMeasures(exportContext: ModelExportContext) {
        logger.warn { "Model.takeMeasures not implemented" }
    }

    private fun <R> withinLayoutScope(constraints: LayoutConstraints? = null, block: LayoutScope.() -> R): R =
        with(context.instance) {
            context.withinLayoutScope(
                LayoutConstraints(
                    leftTop = constraints?.leftTop ?: context.layoutConstraints?.leftTop,
                    maxRightBottom = constraints?.maxRightBottom ?: context.layoutConstraints?.maxRightBottom
                    ?: getViewPortMaxRightBottom(),
                    orientation = constraints?.orientation ?: context.layoutConstraints?.orientation
                    ?: Orientation.HORIZONTAL,
                    uom = uom
                ), block
            )
        }

    private fun layoutPolicy(): LayoutPolicy = if (this is LayoutPolicyProvider<*>) policy else SimpleLayoutPolicy()

    protected fun ModelExportContext.render(context: AttributedContext): OperationResult? =
        instance.render(self(), context)

    protected fun ModelExportContext.measure(context: AttributedContext): OperationResult? =
        instance.measure(self(), context)

    protected fun ModelExportContext.clearLayouts() = instance.clearLayouts()

    @Suppress("UNCHECKED_CAST")
    private fun self(): SELF = (this as SELF)

    companion object : KLogging()
}

fun AbstractModel<*>.getPosition(): Position? = context.layouts.last()?.leftTop

fun AbstractModel<*>.exportWithStatus(
    parentContext: ModelExportContext,
    layoutCxt: LayoutConstraints? = null,
): ExportStatus =
    export(parentContext, layoutCxt).let { context.status }

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
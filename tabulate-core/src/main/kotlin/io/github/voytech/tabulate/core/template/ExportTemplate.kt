package io.github.voytech.tabulate.core.template

import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.template.Layouts.Companion.rootLayouts
import io.github.voytech.tabulate.core.template.Navigation.Companion.rootNavigation
import io.github.voytech.tabulate.core.template.exception.OutputBindingResolvingException
import io.github.voytech.tabulate.core.template.layout.*
import io.github.voytech.tabulate.core.template.operation.*
import io.github.voytech.tabulate.core.template.operation.factories.OperationsFactory
import io.github.voytech.tabulate.core.template.result.OutputBinding
import io.github.voytech.tabulate.core.template.spi.OutputBindingsProvider

typealias ResumeNext = () -> Unit

typealias OperationsMap = MutableMap<Model<*>, Operations<RenderingContext>>

class ExportInstance(
    format: DocumentFormat,
    rootModel: AbstractModel<*>,
    stateAttributes: StateAttributes? = null,
    private val operationsFactory: OperationsFactory<RenderingContext> = OperationsFactory(format),
    internal val renderingContext: RenderingContext = operationsFactory.renderingContext.newInstance(),
) {
    internal val uom: UnitsOfMeasure = UnitsOfMeasure.PT
    internal val root: ModelExportContext = ModelExportContext(
        this, rootNavigation(rootModel), rootLayouts(), stateAttributes.orEmpty()
    )
    internal var active: ModelExportContext = root
    internal val suspendedModels: MutableSet<AbstractModel<*>> = mutableSetOf()
    private val exportOperations: OperationsMap = mutableMapOf()
    private val measureOperations: OperationsMap = mutableMapOf()


    fun <M : Model<M>> getExportOperations(model: M): Operations<RenderingContext> =
        exportOperations.computeIfAbsent(model) {
            operationsFactory.createMeasureOperations(model).let { measuringOperations ->
                operationsFactory.createExportOperations(model,
                    JoinOperations(measuringOperations) { !it.boundingBox().isDefined() },
                    EnableLayoutsAwareness { getActiveLayout() }
                )
            }
        }

    fun <M : Model<M>> getMeasuringOperations(model: M): Operations<RenderingContext> =
        measureOperations.computeIfAbsent(model) {
            operationsFactory.createMeasureOperations(model,
                SkipRedundantMeasurements(),
                //TODO is this Enhancer required ? Consider not enhancing each operation, but instead create single operation which internally delegates operations returned from createMeasureOperations
                //TODO this will allow to enable measuring without single third-party rendering-context specific measuring operations.
                EnableLayoutsAwareness(checkOverflows = false) { getActiveMeasuringLayout() }
            )
        }

    private fun getActiveLayout(): LayoutWithPolicy =
        (active.layouts.renderingLayout ?: active.getClosestAncestorLayout())
            ?.let { LayoutWithPolicy(it, active.layouts.layoutPolicy) } ?: error("No active rendering layout!")

    private fun getActiveMeasuringLayout(): LayoutWithPolicy =
        (active.layouts.measuringLayout ?: active.getClosestLayoutAwareAncestor()?.layouts?.measuringLayout)
            ?.let { LayoutWithPolicy(it, active.layouts.layoutPolicy) } ?: error("No active rendering layout!")

    internal fun getViewPortMaxRightBottom(): Position =
        if (renderingContext is HavingViewportSize) {
            Position(
                X(renderingContext.getWidth().orMax(uom).value, uom),
                Y(renderingContext.getHeight().orMax(uom).value, uom)
            )
        } else {
            Position(X.max(uom), Y.max(uom)) //TODO instead make it nullable - when null - renderer does not clip
        }

    fun <M : Model<M>> render(model: M, context: AttributedContext) {
        getExportOperations(model).invoke(renderingContext, context)
    }

    fun <M : Model<M>> measure(model: M, context: AttributedContext) {
        getMeasuringOperations(model).invoke(renderingContext, context)
    }

    fun <R> setActive(ctx: ModelExportContext, block: () -> R): R {
        active = ctx
        return run(block).also {
            ctx.navigation.parentContext?.let { active = it }
        }
    }

    fun clearLayouts() {
        root.navigation.traverse {
            it.context.layouts.drop()
        }.also { root.setLayout(maxRightBottom = getViewPortMaxRightBottom()) }
    }

    fun resumeAllSuspendedNodes() {
        // it may happen that particular model can be suspended multiple times, that is why we need to repeat resumption till no suspended models exist.
        while (suspendedModels.isNotEmpty()) {
            clearLayouts()
            resumeAll()
        }
    }

    private fun preserveCurrentlyActive(block: () -> Unit) {
        val tmp = active
        block()
        active = tmp
    }

    private fun resumeAll() {
        preserveCurrentlyActive { resumeModel(root.navigation.active) }
    }

    private fun resumeChildren(model: AbstractModel<*>) {
        model.context.navigation.onEachChild { resumeModel(it) }
    }

    private fun resumeModel(model: AbstractModel<*>) = with(root) {
        active = model.context
        resumeModelExport(model)
    }

    private fun resumeModelExport(model: AbstractModel<*>) {
        model.resume(model.context) { resumeChildren(model) }
    }

}

/**
 * Class wrapping ExportOperations into standalone ExportOperations.
 * @author Wojciech Mąka
 * @since 0.*.*
 */
class StandaloneExportTemplate<M : AbstractModel<M>>(
    private val format: DocumentFormat,
) {

    private val outputBindingsProvider: OutputBindingsProvider<RenderingContext> by lazy {
        loadFirstByDocumentFormat<OutputBindingsProvider<RenderingContext>, RenderingContext>(format)!!
    }

    fun <O : Any> export(model: M, output: O) = with(ExportInstance(format, model)) {
        resolveOutputBinding(output).run {
            setOutput(renderingContext, output)
            model.export(this@with.root)
            resumeAllSuspendedNodes()
            flush()
        }
    }

    fun <O : Any, T : Any> export(model: M, output: O, dataSource: Iterable<T> = emptyList()) =
        with(ExportInstance(format, model, dataSource.asStateAttributes())) {
            resolveOutputBinding(output).run {
                setOutput(renderingContext, output)
                model.export(this@with.root)
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

    private fun <T : Any> Iterable<T>.asStateAttributes(): StateAttributes = StateAttributes(
        if (iterator().hasNext()) {
            mutableMapOf("_dataSourceOverride" to DataSourceBinding(this))
        } else mutableMapOf()
    )

}
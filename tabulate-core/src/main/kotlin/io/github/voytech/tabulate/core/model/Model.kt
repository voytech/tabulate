package io.github.voytech.tabulate.core.model

import io.github.voytech.tabulate.core.layout.Layout
import io.github.voytech.tabulate.core.layout.LayoutProperties
import io.github.voytech.tabulate.core.layout.SpaceConstraints
import io.github.voytech.tabulate.core.layout.LayoutSpace
import io.github.voytech.tabulate.core.layout.impl.SimpleLayout
import mu.KLogging
import java.util.*

interface Model {
    @get:JvmSynthetic
    val id: String
}

interface ModelPart

interface AttributeAware {
    val attributes: Attributes?
}

interface AttributedModelOrPart : AttributeAware, ModelPart

fun <C : ExecutionContext, R : Any> ExportApi.value(supplier: ReifiedValueSupplier<C, R>): R? =
    with(getCustomAttributes()) { supplier.value() }

interface LayoutStrategy<LP : Layout> {
    fun createLayout(properties: LayoutProperties): LP

    fun <R> ExportApi.withinCurrentLayout(block: (LP.(LayoutSpace) -> R)): R =
        currentLayoutScope().layout(block)

}

internal enum class Method {
    PREPARE,
    EXPORT,
    FINISH,
    INITIALIZE,
    MEASURE;
}

abstract class AbstractModel(
    override val id: String = UUID.randomUUID().toString(),
) : Model {

    open val needsMeasureBeforeExport: Boolean = false

    @JvmSynthetic
    internal operator fun invoke(method: Method, exportContext: ModelExportContext) = exportContext.api {
        when (method) {
            Method.INITIALIZE -> exportContextCreated(this)
            Method.MEASURE -> takeMeasures(this)
            Method.PREPARE -> beforeLayoutCreated(this)
            Method.EXPORT -> doExport(this)
            Method.FINISH -> finishExport(this)
        }
    }


    protected open fun beforeLayoutCreated(api: ExportApi) {
        logger.warn { "Model.beforeLayoutCreated hook not implemented" }
    }

    protected abstract fun doExport(api: ExportApi)

    protected open fun finishExport(api: ExportApi) {
        logger.warn { "Model.finishExport hook not implemented" }
    }

    protected open fun exportContextCreated(api: ExportApi) {
        logger.warn { "Model.exportContextCreated not implemented" }
    }

    protected open fun takeMeasures(api: ExportApi) {
        logger.warn { "Model.takeMeasures not implemented" }
    }

    protected open fun getLayoutConstraints(): SpaceConstraints? = null

    internal fun resolveLayout(properties: LayoutProperties): Layout =
        if (this is LayoutStrategy<*>) createLayout(properties) else SimpleLayout(properties)

    companion object : KLogging()
}

abstract class ModelWithAttributes :
    AttributedModelOrPart, AbstractModel()

enum class DescendantsIterationsKind {
    IMMEDIATE,
    POSTPONED,
    CUSTOM
}
abstract class AbstractContainerModel: AbstractModel() {
    protected abstract val models: List<AbstractModel>
    protected open val descendantsIterationsKind: DescendantsIterationsKind = DescendantsIterationsKind.POSTPONED
}

abstract class AbstractContainerModelWithAttributes: AbstractContainerModel(), AttributedModelOrPart

interface ExecutionContext

fun interface ValueSupplier<C : ExecutionContext, V : Any> : (C) -> V

data class ReifiedValueSupplier<C : ExecutionContext, V : Any>(
    val inClass: Class<out ExecutionContext>,
    val retClass: Class<V>,
    val delegate: ValueSupplier<C, V>,
) : ValueSupplier<C, V> {
    override fun invoke(context: C): V = delegate(context)
}
package io.github.voytech.tabulate.core.model

import io.github.voytech.tabulate.core.layout.Layout
import io.github.voytech.tabulate.core.layout.LayoutProperties
import io.github.voytech.tabulate.core.layout.RegionConstraints
import io.github.voytech.tabulate.core.layout.Region
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

interface HavingLayout<LP : Layout> {
    fun createLayout(properties: LayoutProperties): LP

    fun  ExportApi.withinCurrentLayout(block: (LP.() -> Unit)) {
        layout(block)
    }

}

internal enum class Method {
    EXPORT,
    CONTEXT_CREATED,
    MEASURE;
}

abstract class AbstractModel(
    override val id: String = UUID.randomUUID().toString(),
) : Model {

    open val needsMeasureBeforeExport: Boolean = false

    @JvmSynthetic
    internal operator fun invoke(method: Method, exportContext: ModelExportContext) = exportContext.api {
        when (method) {
            Method.CONTEXT_CREATED -> exportContextCreated(this)
            Method.MEASURE -> takeMeasures(this)
            Method.EXPORT -> doExport(this)
        }
    }

    protected abstract fun doExport(api: ExportApi)

    protected open fun finishExport(api: ExportApi) {}

    protected open fun exportContextCreated(api: ExportApi) {}


    //@TODO this should not be optional.
    protected open fun takeMeasures(api: ExportApi) {}

    protected open fun getLayoutConstraints(): RegionConstraints? = null

    internal fun resolveLayout(properties: LayoutProperties): Layout =
        if (this is HavingLayout<*>) createLayout(properties) else SimpleLayout(properties)


    fun <A : Attribute<A>> getAttribute(attribute: Class<A>): A? =
        (this as? AttributeAware)?.attributes?.get(attribute)

    inline fun <reified A : Attribute<A>> getAttribute(): A? =
        (this as? AttributeAware)?.attributes?.get(A::class.java)

    override fun toString(): String = "${javaClass.simpleName}[$id]"

    companion object : KLogging()
}

abstract class ModelWithAttributes :
    AttributedModelOrPart, AbstractModel()

enum class DescendantsIterationsKind {
    IMMEDIATE,
    POSTPONED,
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
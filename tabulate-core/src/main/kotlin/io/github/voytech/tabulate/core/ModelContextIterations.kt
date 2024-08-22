package io.github.voytech.tabulate.core

import io.github.voytech.tabulate.core.layout.RegionConstraints
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.model.debug
import io.github.voytech.tabulate.core.model.trace
import io.github.voytech.tabulate.core.model.traceIteration
import mu.KLogging

class ExportIterations(val context: ModelExportContext) {

    private val scheduledIterations = mutableListOf<ExportIteration>()
    private val runningIterations = mutableListOf<ExportIteration>()
    private var withinIteration = false

    var current: ExportIteration
        private set

    init {
        appendIteration()
        current = scheduledIterations[0]
    }

    private fun newIteration(newAttributes: Map<String, Any>? = null): ExportIteration {
        return ExportIteration(context).apply { newAttributes?.let { attributes += it } }
    }

    fun appendIteration(attributes: Map<String, Any>? = null) {
        val newOne = newIteration(attributes)
        if (scheduledIterations.any { it conflicts newOne } ||
            runningIterations.any { it conflicts newOne }) {
            debug("Iteration attributes conflict detected!")
        } else {
            scheduledIterations += newOne
        }
    }

    fun prependIteration(attributes: Map<String, Any>) {
        scheduledIterations.add(0, newIteration(attributes))
    }

    fun appendAttributes(vararg attributes: Pair<String, Any>) {
        current.let {
            if (!it.suspendAttributes) {
                it.attributes += attributes.toMap()
            }
        }
    }

    fun clearAttributes(): Map<String, Any> {
        return current.attributes.let { attributes ->
            attributes.toMap().also { attributes.clear() }
        }
    }

    fun suspendAttributes() {
        current.suspendAttributes = true
    }

    fun discardScheduled() {
        scheduledIterations.clear()
    }

    private fun ExportIteration.use(): ExportIteration = also { current = this }

    private fun moveForMeasure(): ExportIteration = scheduledIterations.removeFirstOrNull()?.let {
        runningIterations.add(it);it
    } ?: current

    private fun moveForRender(): ExportIteration =
        runningIterations.removeFirstOrNull() ?: scheduledIterations.removeFirst()

    private fun ensureIteration() {
        if (!hasAnyIteration() && (haveAnyChildAnyIteration() || context.isRunning())) appendIteration()
    }

    private fun setIteration(): ExportIteration {
        ensureIteration()
        if (hasAnyIteration()) {
            return when (context.phase) {
                Phase.MEASURING -> moveForMeasure().use()
                Phase.RENDERING -> moveForRender().use()
            }
        } else {
            logger.error { "Could not start iteration. No scheduled nor running iterations present!" }
            error("Could not start iteration. No scheduled nor running iterations present!")
        }
    }

    @JvmSynthetic
    internal fun <R> executeIteration(constraint: RegionConstraints, block: () -> R): R? {
        val iteration = setIteration()
        withinIteration = true
        return iteration.execute(constraint, block).also {
            withinIteration = false
        }
    }

    @JvmSynthetic
    internal fun hasAnyMeasuredIteration(): Boolean {
        require(!withinIteration) { "hasAnyMeasuredIteration can be invoked only outside of iteration scope" }
        return runningIterations.isNotEmpty()
    }

    @JvmSynthetic
    internal fun hasAnyScheduledIteration(): Boolean {
        require(!withinIteration) { "hasAnyScheduledIteration can be invoked only outside of iteration scope" }
        return scheduledIterations.isNotEmpty()
    }

    /**
     * This method is not allowed to be invoked within iteration block.
     * It can be only called before or after rendering/measuring iteration block
     */
    @JvmSynthetic
    internal fun hasAnyIteration(): Boolean {
        require(!withinIteration) { "hasAnyIteration can be invoked only outside of iteration scope" }
        return when (context.phase) {
            Phase.MEASURING -> hasAnyScheduledIteration()
            Phase.RENDERING -> hasAnyScheduledIteration() || hasAnyMeasuredIteration()
        }
    }

    @JvmSynthetic
    internal fun haveAnyChildAnyIteration(): Boolean = context.treeNode {
        require(!withinIteration) { "haveAnyChildAnyIteration can be invoked only outside of iteration scope" }
        checkAnyChildren { it.exportIterations.hasAnyIteration() }
    }

    @JvmSynthetic
    internal fun setDryRunForActive() {
        current.setDryRun()
    }

    fun getCurrentIterationAttributesOrNull(): Map<String, Any> = current.attributes.toMap()

    fun <E> getCurrentIterationAttributeOrNull(key: String): E? = getCurrentIterationAttributesOrNull()[key] as E?

    fun getCurrentLayout(): LayoutData = current.layout()

    companion object : KLogging()
}

class ExportIteration(val context: ModelExportContext) {
    var suspendAttributes: Boolean = false
    val attributes: MutableMap<String, Any> = mutableMapOf()
    val layoutContext = ModelContextLayout(context)

    infix fun conflicts(other: ExportIteration): Boolean =
        attributes.any { it.value == other.attributes[it.key] }

    @JvmSynthetic
    internal fun <R> execute(constraint: RegionConstraints, block: () -> R): R? {
        layoutContext.beginLayout(constraint)
        return (if (isDryRenderRun()) {
            layoutContext.endLayout()
            null.also { trace("Dry render run detected!") }
        } else {
            block().also { layoutContext.endLayout() }
        })
    }

    private fun isDryRenderRun(): Boolean = attributes["_dryRun"] as? Boolean ?: false

    fun setDryRun() {
        attributes["_dryRun"] = true
        suspendAttributes = true
    }

    fun layout(): LayoutData = layoutContext.layout

}
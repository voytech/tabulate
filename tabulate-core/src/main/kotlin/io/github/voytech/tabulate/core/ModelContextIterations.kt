package io.github.voytech.tabulate.core

import io.github.voytech.tabulate.core.layout.SpaceConstraints
import io.github.voytech.tabulate.core.model.ModelExportContext
import io.github.voytech.tabulate.core.model.Phase
import io.github.voytech.tabulate.core.model.debug
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

    private fun startIteration(constraints: SpaceConstraints): Boolean {
        if (!hasAnyIteration() && haveAnyChildAnyIteration()) appendIteration()
        if (hasAnyIteration()) {
            when (context.phase) {
                Phase.MEASURING -> moveForMeasure().use()
                Phase.RENDERING -> moveForRender().use()
            }.also { context.traceIteration("[ITERATION START]") }
                .begin(constraints)
            return true
        } else {
            logger.error { "Could not start iteration. No scheduled nor running iterations present!" }
            error("Could not start iteration. No scheduled nor running iterations present!")
        }
    }

    private fun endIteration() {
        current.end()
        context.traceIteration("[ITERATION END]")
    }

    fun <R> executeIteration(constraint: SpaceConstraints, block: () -> R): R? {
        val started = startIteration(constraint)
        return if (started) {
            withinIteration = true
            block().also {
                withinIteration = false
                endIteration()
            }
        } else null
    }

    fun hasAnyMeasuredIteration(): Boolean {
        require(!withinIteration) { "hasAnyMeasuredIteration can be invoked only outside of iteration scope" }
        return runningIterations.isNotEmpty()
    }

    fun hasAnyScheduledIteration(): Boolean {
        require(!withinIteration) { "hasAnyScheduledIteration can be invoked only outside of iteration scope" }
        return scheduledIterations.isNotEmpty()
    }

    /**
     * This method is not allowed to be invoked within iteration block.
     * It can be only called before or after rendering/measuring iteration block
     */
    fun hasAnyIteration(): Boolean {
        require(!withinIteration) { "hasAnyIteration can be invoked only outside of iteration scope" }
        return when (context.phase) {
            Phase.MEASURING -> hasAnyScheduledIteration()
            Phase.RENDERING -> hasAnyScheduledIteration() || hasAnyMeasuredIteration()
        }
    }

    fun haveAnyChildAnyIteration(): Boolean = context.treeNode {
        require(!withinIteration) { "haveAnyChildAnyIteration can be invoked only outside of iteration scope" }
        checkAnyChildren { it.exportIterations.hasAnyIteration() }
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

    fun begin(space: SpaceConstraints) {
        layoutContext.beginLayout(space)
    }

    fun end() {
        layoutContext.endLayout()
    }

    fun layout(): LayoutData = layoutContext.layout

    fun isRetried(): Boolean = attributes["_retry"] as? Boolean ?: false

    fun isStopped(): Boolean = attributes["_stop"] as? Boolean ?: false


}
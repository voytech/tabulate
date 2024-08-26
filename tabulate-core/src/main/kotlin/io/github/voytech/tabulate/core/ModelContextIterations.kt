package io.github.voytech.tabulate.core

import io.github.voytech.tabulate.core.layout.Layout
import io.github.voytech.tabulate.core.layout.RegionConstraints
import io.github.voytech.tabulate.core.model.ModelExportContext
import io.github.voytech.tabulate.core.model.Phase
import io.github.voytech.tabulate.core.model.debug
import io.github.voytech.tabulate.core.model.trace
import mu.KLogging

/**
 * Manages the iterations of the export process.
 *
 * @property context The model export context associated with this iteration manager.
 */
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

    /**
     * Creates a new export iteration with optional attributes.
     *
     * @param newAttributes Optional attributes to add to the new iteration.
     * @return The newly created export iteration.
     */
    private fun newIteration(newAttributes: Map<String, Any>? = null): ExportIteration {
        return ExportIteration(context).apply { newAttributes?.let { attributes += it } }
    }

    /**
     * Appends a new iteration with optional attributes to the scheduled iterations.
     *
     * @param attributes Optional attributes to add to the new iteration.
     */
    fun appendIteration(attributes: Map<String, Any>? = null) {
        val newOne = newIteration(attributes)
        if (scheduledIterations.any { it conflicts newOne } ||
            runningIterations.any { it conflicts newOne }) {
            debug("Iteration attributes conflict detected!")
        } else {
            scheduledIterations += newOne
        }
    }

    /**
     * Prepends a new iteration with attributes to the scheduled iterations.
     *
     * @param attributes Attributes to add to the new iteration.
     */
    fun prependIteration(attributes: Map<String, Any>) {
        scheduledIterations.add(0, newIteration(attributes))
    }

    /**
     * Appends attributes to the current iteration.
     *
     * @param attributes Attributes to add to the current iteration.
     */
    fun appendAttributes(vararg attributes: Pair<String, Any>) {
        current.let {
            if (!it.suspendAttributes) {
                it.attributes += attributes.toMap()
            }
        }
    }

    /**
     * Clears the attributes of the current iteration.
     *
     * @return The cleared attributes as a map.
     */
    fun clearAttributes(): Map<String, Any> {
        return current.attributes.let { attributes ->
            attributes.toMap().also { attributes.clear() }
        }
    }

    /**
     * Suspends the attributes of the current iteration.
     */
    fun suspendAttributes() {
        current.suspendAttributes = true
    }

    /**
     * Discards all scheduled iterations.
     */
    fun discardScheduled() {
        scheduledIterations.clear()
    }

    /**
     * Uses the given export iteration as the current iteration.
     *
     * @return The given export iteration.
     */
    private fun ExportIteration.use(): ExportIteration = also { current = this }

    /**
     * Moves the first scheduled iteration to the running iterations for measurement.
     *
     * @return The moved export iteration.
     */
    private fun moveForMeasure(): ExportIteration = scheduledIterations.removeFirstOrNull()?.let {
        runningIterations.add(it);it
    } ?: current

    /**
     * Moves the first running or scheduled iteration for rendering.
     *
     * @return The moved export iteration.
     */
    private fun moveForRender(): ExportIteration =
        runningIterations.removeFirstOrNull() ?: scheduledIterations.removeFirst()

    /**
     * Ensures that there is at least one iteration if export is not finished for model .
     */
    private fun ensureIteration() {
        if (!hasAnyIteration() && (haveAnyChildAnyIteration() || context.isRunning())) appendIteration()
    }

    /**
     * Sets the current iteration based on the context phase.
     *
     * @return The set export iteration.
     */
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

    /**
     * Executes the given block within the iteration scope.
     *
     * @param constraint The region constraints for the iteration.
     * @param block The block of code to execute.
     * @return The result of the block execution.
     */
    @JvmSynthetic
    internal fun <R> executeIteration(constraint: RegionConstraints, block: () -> R): R? {
        val iteration = setIteration()
        withinIteration = true
        return iteration.execute(constraint, block).also {
            withinIteration = false
        }
    }

    /**
     * Checks if there are any measured iterations.
     *
     * @return True if there are measured iterations, false otherwise.
     */
    @JvmSynthetic
    internal fun hasAnyMeasuredIteration(): Boolean {
        require(!withinIteration) { "hasAnyMeasuredIteration can be invoked only outside of iteration scope" }
        return runningIterations.isNotEmpty()
    }

    /**
     * Checks if there are any scheduled iterations.
     *
     * @return True if there are scheduled iterations, false otherwise.
     */
    @JvmSynthetic
    internal fun hasAnyScheduledIteration(): Boolean {
        require(!withinIteration) { "hasAnyScheduledIteration can be invoked only outside of iteration scope" }
        return scheduledIterations.isNotEmpty()
    }

    /**
     * Checks if any child context has any iterations.
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

    /**
     * Sets the current iteration to dry run mode.
     * Dry rendering mode is used to apply previously measured layout region onto parent layout without rendering
     * the content applicable for that specific iteration.
     * Dry rendering mode is set in MEASURING phase when due to not enough space, new iteration is prepended for retry or
     * iteration is skipped. Setting Dry rendering may/should be set when clipping = disabled (As all renderables cannot be
     * rendered at all when overflowing the layout boundaries).
     */
    @JvmSynthetic
    internal fun setDryRunForActive() {
        current.setDryRun()
    }

    /**
     * Retrieves the attributes of the current iteration.
     *
     * @return The attributes of the current iteration as a map.
     */
    fun getCurrentIterationAttributesOrNull(): Map<String, Any> = current.attributes.toMap()

    /**
     * Retrieves a specific attribute of the current iteration.
     *
     * @param key The key of the attribute to retrieve.
     * @return The value of the attribute, or null if not found.
     */
    fun <E> getCurrentIterationAttributeOrNull(key: String): E? = getCurrentIterationAttributesOrNull()[key] as E?

    /**
     * Retrieves the layout data of the current iteration.
     *
     * @return The layout data of the current iteration.
     */
    fun getCurrentLayout(): Layout = current.layout()

    companion object : KLogging()
}

/**
 * Represents a single export iteration.
 *
 * @property context The model export context associated with this iteration.
 */
class ExportIteration(val context: ModelExportContext) {
    var suspendAttributes: Boolean = false
    val attributes: MutableMap<String, Any> = mutableMapOf()
    val layoutContext = ModelContextLayout(context)

    /**
     * Checks if this iteration conflicts with another iteration.
     *
     * @param other The other iteration to check against.
     * @return True if there is a conflict, false otherwise.
     */
    infix fun conflicts(other: ExportIteration): Boolean =
        attributes.any { it.value == other.attributes[it.key] }

    /**
     * Executes the given block within the layout context.
     *
     * @param constraint The region constraints for the layout.
     * @param block The block of code to execute.
     * @return The result of the block execution, or null if it is a dry run.
     */
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

    /**
     * Checks if this iteration is a dry render run.
     *
     * @return True if it is a dry render run, false otherwise.
     */
    private fun isDryRenderRun(): Boolean = attributes["_dryRun"] as? Boolean ?: false

    /**
     * Sets this iteration to dry run mode.
     */
    fun setDryRun() {
        attributes["_dryRun"] = true
        suspendAttributes = true
    }

    /**
     * Retrieves the layout data of this iteration.
     *
     * @return The layout data of this iteration.
     */
    fun layout(): Layout = layoutContext.layout

}
package io.github.voytech.tabulate.core.model

import mu.KLogging

internal fun ModelExportContext.traceIteration(header: String = "") {
    traceSection(
        header,
        "Model: $model",
        "Phase: ${phase}, Status: ${state(phase)}",
        "Attributes: ${exportIterations.getCurrentIterationAttributesOrNull()}",
        "Layout: ${activeLayoutContext.debugInfo()}"
    )
}

internal object Global {

    private val threadLocal: ThreadLocal<ArrayDeque<Int>> = ThreadLocal.withInitial { ArrayDeque() }
    fun setDepth(toSet: Int): Int {
        threadLocal.get().add(toSet)
        return toSet
    }

    fun getDepth(): Int = threadLocal.get().last()

    fun unsetDepth(): Int = threadLocal.get().removeLast()
}

@JvmSynthetic
internal fun ModelExportContext.markDepth(): Int = Global.setDepth(depth)

@JvmSynthetic
internal fun getDepth(): Int = Global.getDepth()

@JvmSynthetic
internal fun unsetDepth(): Int = Global.unsetDepth()

internal fun traceSection(vararg messages: String) {
    val len = messages.max().length
    val depth = debugDepth()
    val lenRend = (0..len).fold(" _") { acc, _ -> "${acc}_" }
    ContextDebug.logger.trace { depth + lenRend }
    trace(*messages)
}

internal fun debug(vararg message: String) {
    message.forEach { ContextDebug.logger.debug { "${debugDepth()}| $it" } }
}

internal fun trace(vararg message: String) {
    message.forEach { ContextDebug.logger.trace { "${debugDepth()}| $it" } }
}

private fun debugDepth() = (0..getDepth()).fold("") { acc, _ -> "$acc   " }

object ContextDebug : KLogging()
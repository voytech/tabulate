package io.github.voytech.tabulate.core.result

import io.github.voytech.tabulate.core.RenderingContext


/**
 * Simple contract for binding rendering context with generic output.
 * Implementors of this interface should be aware of rendering context in order to convey rendered table into given output.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface OutputBinding<CTX: RenderingContext, O> {
    fun outputClass(): Class<O>
    fun setOutput(renderingContext: CTX, output: O)
    fun flush()
}
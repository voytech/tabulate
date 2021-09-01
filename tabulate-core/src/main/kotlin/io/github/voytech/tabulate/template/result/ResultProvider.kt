package io.github.voytech.tabulate.template.result

/**
 * Simple contract for binding rendering context with generic output.
 * Implementors of this interface should be aware of rendering context in order to convey rendered table into given output.
 * @author
 * Wojciech Mąka
 */
interface ResultProvider<O> {
    fun outputClass(): Class<O>
    fun flush(output: O)
}
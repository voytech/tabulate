package io.github.voytech.tabulate.template.result

/**
 * Simple contract for binding rendering context into generic output.
 * Implementors of this interface should be aware of rendering context in order to convey rendered table into specific output.
 * @author
 * Wojciech Mąka
 */
interface ResultProvider<O> {
    fun outputClass(): Class<O>
    fun flush(output: O)
}
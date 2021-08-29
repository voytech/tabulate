package io.github.voytech.tabulate.template.result

/**
 * Having rendering context, get rendered data and pass to the output.
 * this method can be implemented for various scenarios.
 * It can call context 3rd party method to flush entire content to the output, ot it
 * can take recently rendered row content and append to the output.
 */
fun interface ResultProvider<O> {
    fun flush(output: O)
}
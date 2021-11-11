package io.github.voytech.tabulate.template.iterators

import io.github.voytech.tabulate.template.context.MutableRowIndex
import io.github.voytech.tabulate.template.context.RowIndex
import io.github.voytech.tabulate.template.operations.ContextData
import io.github.voytech.tabulate.template.resolvers.IndexedContextResolver

fun interface StepProvider {
    fun provide(): String?
}

class EnumStepProvider<T: Enum<T>>(enum: Class<T>) : StepProvider {
    val iterator = enum.enumConstants.iterator()
    override fun provide(): String? = if (iterator.hasNext()) iterator.next().name else null
}

internal class RowContextIterator<T, CTX : ContextData>(
    private val resolver: IndexedContextResolver<T, CTX>,
    private val stepProvider: StepProvider
) : AbstractIterator<CTX>() {

    private val indexIncrement = MutableRowIndex()

    override fun computeNext() {
        resolver.resolve(indexIncrement.getRowIndex()).also {
            if (it != null) {
                val currentContext = it.value
                if (it.index > indexIncrement.getRowIndex().value) {
                    indexIncrement.assign(it.index)
                }
                setNext(currentContext)
                indexIncrement.inc()
            } else {
                stepProvider.provide().let { stepCode ->
                    if (stepCode == null) {
                        done()
                    } else {
                        mark(stepCode)
                        computeNext()
                    }
                }
            }
        }
    }

    fun mark(step: String): RowIndex {
        return indexIncrement.mark(step)
    }
}


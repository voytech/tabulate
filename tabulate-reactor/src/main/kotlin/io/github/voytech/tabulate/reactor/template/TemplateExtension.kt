package io.github.voytech.tabulate.reactor.template

import io.github.voytech.tabulate.api.builder.dsl.TableBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.table
import io.github.voytech.tabulate.template.TabulationFormat
import io.github.voytech.tabulate.template.TabulationTemplate
import reactor.core.publisher.Flux

fun <T, O> Flux<T>.tabulate(format: TabulationFormat, output: O, block: TableBuilderApi<T>.() -> Unit): Flux<T> {
    return TabulationTemplate<T>(format).create<O>(table(block)).let { api ->
        doOnNext {
            api.nextRow(it)
        }.doOnComplete {
            api.finish()
            api.flush(output)
        }
    }
}
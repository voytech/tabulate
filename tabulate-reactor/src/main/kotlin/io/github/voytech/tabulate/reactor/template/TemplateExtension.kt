package io.github.voytech.tabulate.reactor.template

import io.github.voytech.tabulate.api.builder.dsl.TableBuilderApi
import io.github.voytech.tabulate.template.TabulationFormat
import io.github.voytech.tabulate.template.TabulationTemplate
import io.github.voytech.tabulate.template.spi.ExportOperationsProvider
import io.github.voytech.tabulate.template.tabulationFormat
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers.boundedElastic
import java.io.File
import java.io.FileOutputStream

/**
 * Extension function invoked on a Flux publisher.
 * Takes [TabulationFormat], output handler and DSL table builder to define table appearance.
 *
 * @param format identifier of [ExportOperationsProvider] to be used in order to export table to specific file format (xlsx, pdf).
 * @param output reference to an output - may be e.g. OutputStream.
 * @param block [TableBuilderApi] a top level table DSL builder which defines table appearance.
 * @receiver Flux publishing records that are to be rendered into file.
 */
fun <T, O> Flux<T>.tabulate(format: TabulationFormat, output: O, block: TableBuilderApi<T>.() -> Unit): Flux<T> {
    return TabulationTemplate<T>(format).create(output, block).let { api ->
        publishOn(boundedElastic())
        .doOnNext {
            api.nextRow(it)
        }.doOnComplete {
            api.finish()
            api.flush()
        }
    }
}

/**
 * Extension function invoked on a Flux publisher.
 * Takes [File] as argument and DSL table builder to define table appearance.
 *
 * @param file a file to create a table in. File extension is being used for [ExportOperationsProvider] discovery.
 * @param block [TableBuilderApi] a top level table DSL builder which defines table appearance.
 * @receiver Flux publishing records that are to be rendered into file.
 */
fun <T> Flux<T>.tabulate(file: File, block: TableBuilderApi<T>.() -> Unit): Flux<T> {
    return file.tabulationFormat().let { format ->
        FileOutputStream(file).use {
            tabulate(format, it, block)
        }
    }
}

/**
 * Extension function invoked on a Flux publisher.
 * Takes [fileName] as argument and DSL table builder to define table appearance.
 *
 * @param fileName a file name to create.
 * @param block [TableBuilderApi] a top level table DSL builder which defines table appearance.
 * @receiver Flux publishing records that are to be rendered into file.
 */
fun <T> Flux<T>.tabulate(fileName: String, block: TableBuilderApi<T>.() -> Unit): Flux<T> = tabulate(File(fileName), block)

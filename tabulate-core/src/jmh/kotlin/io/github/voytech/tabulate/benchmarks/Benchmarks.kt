package io.github.voytech.tabulate.benchmarks

import io.github.voytech.tabulate.api.builder.dsl.CustomTable
import io.github.voytech.tabulate.api.builder.dsl.TableBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.createTable
import io.github.voytech.tabulate.model.Table
import io.github.voytech.tabulate.template.TabulationFormat
import io.github.voytech.tabulate.template.export
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

private fun createTableDefinition(count: Int): TableBuilderApi<Unit>.() -> Unit {
    return CustomTable {
        columns {
            column("c-1")
        }
        rows {
            for (i in 1..count) {
                newRow {
                    cell { value = "c-1" }
                }
            }
        }
    }
}


@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3)
@Measurement(iterations = 10, time = 5, timeUnit = TimeUnit.SECONDS)
@Threads(8)
@Fork(2)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
open class TableExportingBenchmark {

    lateinit var table: Table<Unit>

    @Setup
    fun setup() {
        table = createTable { createTableDefinition(10000)  }
    }

    @Benchmark
    fun testExporterInfrastructure() {
        table.export(TabulationFormat.format("benchmark"), Unit)
    }

}
fun main() {
    val runDateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    val options = OptionsBuilder()
        .include(TableExportingBenchmark::class.java.simpleName)
        .output("testExporterInfrastructure_$runDateTime.log")
        .build()
    Runner(options).run()
}
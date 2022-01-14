package io.github.voytech.tabulate.benchmarks

import io.github.voytech.tabulate.api.builder.dsl.createTable
import io.github.voytech.tabulate.model.Table
import io.github.voytech.tabulate.model.attributes.cell.Colors
import io.github.voytech.tabulate.model.attributes.cell.alignment
import io.github.voytech.tabulate.model.attributes.cell.background
import io.github.voytech.tabulate.model.attributes.cell.enums.DefaultHorizontalAlignment
import io.github.voytech.tabulate.model.attributes.cell.enums.DefaultWeightStyle
import io.github.voytech.tabulate.model.attributes.cell.text
import io.github.voytech.tabulate.template.TabulationFormat
import io.github.voytech.tabulate.template.export
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import org.openjdk.jmh.profile.GCProfiler
import org.openjdk.jmh.profile.HotspotMemoryProfiler
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3)
@Measurement(iterations = 10, time = 5, timeUnit = TimeUnit.SECONDS)
@Threads(8)
@Fork(2)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
open class Export25kCustomRowsBenchmark {

    lateinit var table: Table<Unit>

    @Setup
    fun setup() {
        table = createTable {
            columns {
                column("c-1")
            }
            rows {
                for (i in 1..25000) {
                    newRow {
                        cell { value = "c-1" }
                    }
                }
            }
        }
    }

    @Benchmark
    fun export25KRows(blackHole: Blackhole) {
        blackHole.consume(table.export(TabulationFormat.format("benchmark"), Unit))
    }
}

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3)
@Measurement(iterations = 10, time = 5, timeUnit = TimeUnit.SECONDS)
@Threads(8)
@Fork(2)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
open class ExportWithColumnDefinedAttributes {

    lateinit var table: Table<Unit>

    @Setup
    fun setup() {
        table = createTable {
            attributes {
                text { fontColor = Colors.WHITE }
                background { color = Colors.BLACK }
            }
            columns {
                column("c-1") {
                      attributes {
                          text { weight = DefaultWeightStyle.BOLD }
                          alignment { horizontal = DefaultHorizontalAlignment.LEFT }
                      }
                }
                column("c-2") {
                    attributes {
                        text { weight = DefaultWeightStyle.NORMAL }
                        alignment { horizontal = DefaultHorizontalAlignment.RIGHT }
                    }
                }
                column("c-3") {
                    attributes {
                        text { weight = DefaultWeightStyle.NORMAL }
                        alignment { horizontal = DefaultHorizontalAlignment.RIGHT }
                    }
                }
                column("c-4") {
                    attributes {
                        text { weight = DefaultWeightStyle.NORMAL }
                        alignment { horizontal = DefaultHorizontalAlignment.RIGHT }
                    }
                }
            }
            rows {
                for (i in 1..10000) {
                    newRow {
                        cell("c-1") { value = "c-1" }
                        cell("c-2") { value = "c-2" }
                        cell("c-3") { value = "c-3" }
                        cell("c-4") { value = "c-4" }
                    }
                }
            }
        }
    }

    @Benchmark
    fun exportWithAttributes(blackHole: Blackhole) {
        blackHole.consume(table.export(TabulationFormat.format("benchmark"), Unit))
    }
}

fun main() {
    val runDateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    val options = OptionsBuilder()
        .addProfiler(GCProfiler::class.java)
        .addProfiler(HotspotMemoryProfiler::class.java)
        .include(ExportWithColumnDefinedAttributes::class.java.simpleName)
        //.include(Export25kCustomRowsBenchmark::class.java.simpleName)
        .output("benchmarks_$runDateTime.log")
        .build()
    Runner(options).run()
}
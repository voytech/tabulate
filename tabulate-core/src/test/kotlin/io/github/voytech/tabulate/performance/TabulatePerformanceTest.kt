package io.github.voytech.tabulate.performance

import io.github.voytech.tabulate.components.table.api.builder.dsl.createTableBuilder
import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.template.AbstractRowContextResolver
import io.github.voytech.tabulate.components.table.template.AccumulatingRowContextResolver
import io.github.voytech.tabulate.components.table.template.TableContinuations
import io.github.voytech.tabulate.components.table.template.RowContextIterator
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.support.createTableContext
import io.github.voytech.tabulate.support.successfulRowComplete
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

class TabulatePerformanceTest {

    internal data class Wrapper<T: Any>(
        val iterator: RowContextIterator<T>,
        val resolver: AbstractRowContextResolver<T>,
        val customAttributes: Map<String, Any>
    )

    private fun <T: Any> createSlowIterator(table: Table<T>): Wrapper<T> =
        mutableMapOf<String, Any>().let {
            it to SlowRowResolver(table, it)
        }.let {
            Wrapper(
                iterator = RowContextIterator(it.second, TableContinuations(it.second.ctx)),
                resolver = it.second,
                customAttributes = it.first
            )
        }

    private fun <T: Any> createFastIterator(table: Table<T>): Wrapper<T> {
        val attributes = mutableMapOf<String, Any>()
        val ctx = table.createTableContext(attributes)
        return (attributes to AccumulatingRowContextResolver(table, StateAttributes(attributes), TableContinuations(ctx), successfulRowComplete())).let {
            Wrapper(
                iterator = RowContextIterator(it.second, TableContinuations(ctx)),
                resolver = it.second,
                customAttributes = it.first
            )
        }
    }

    private fun <T: Any> createTableDefinition(): Table<T> {
        return createTableBuilder<T> {
            columns {
                column("c-1")
            }
            rows {
                for (i in 1..50000) {
                    newRow {
                        cell { value = "c-1" }
                    }
                }
            }
        }.build()
    }

    @Disabled("To be executed only locally for now.")
    @Test
    fun `should export table with large amount of custom rows`() {
        val table = createTableDefinition<Unit>()
        println("ready")
        measureTimeMillis {
            createFastIterator(table).also {
                while(it.iterator.hasNext()) {
                    it.iterator.next()
                }
            }
        }.let { println("Time ms : $it") }
        measureTimeMillis {
            createSlowIterator(table).also {
                while(it.iterator.hasNext()) {
                    it.iterator.next()
                }
            }
        }.let { println("Time ms : $it") }
    }

}
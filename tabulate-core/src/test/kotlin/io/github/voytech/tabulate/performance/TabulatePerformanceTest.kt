package io.github.voytech.tabulate.performance

import io.github.voytech.tabulate.api.builder.dsl.createTableBuilder
import io.github.voytech.tabulate.model.Table
import io.github.voytech.tabulate.template.iterators.RowContextIterator
import io.github.voytech.tabulate.template.resolvers.AbstractRowContextResolver
import io.github.voytech.tabulate.template.resolvers.AccumulatingRowContextResolver
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

class TabulatePerformanceTest {

    internal data class Wrapper<T>(
        val iterator: RowContextIterator<T>,
        val resolver: AbstractRowContextResolver<T>,
        val customAttributes: Map<String, Any>
    )

    private fun <T> createSlowIterator(table: Table<T>): Wrapper<T> =
        mutableMapOf<String, Any>().let {
            it to SlowRowResolver(table, it)
        }.let {
            Wrapper(
                iterator = RowContextIterator(it.second),
                resolver = it.second,
                customAttributes = it.first
            )
        }

    private fun <T> createFastIterator(table: Table<T>): Wrapper<T> =
        mutableMapOf<String, Any>().let {
            it to AccumulatingRowContextResolver(table, it)
        }.let {
            Wrapper(
                iterator = RowContextIterator(it.second),
                resolver = it.second,
                customAttributes = it.first
            )
        }

    private fun <T> createTableDefinition(): Table<T> {
        return createTableBuilder<T> {
            columns {
                column("c-1")
            }
            rows {
                for (i in 1..20000) {
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
package io.github.voytech.tabulate.rowpredicate

import io.github.voytech.tabulate.components.table.model.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RowPredicatesTest {

    data class Record(private val field: String)

    @Test
    fun `should properly calculate range index on simple binary predicate literals`() {
        with(RowIndexPredicateLiteral<Record>(eq(2)).computeRanges()) {
            assertEquals(2, first().start.index)
            assertEquals(2, first().endInclusive.index)
        }
        with(RowIndexPredicateLiteral<Record>(lt(2)).computeRanges()) {
            assertEquals(0, first().start.index)
            assertEquals(1, first().endInclusive.index)
        }
        with(RowIndexPredicateLiteral<Record>(lte(2)).computeRanges()) {
            assertEquals(0, first().start.index)
            assertEquals(2, first().endInclusive.index)
        }
        with(RowIndexPredicateLiteral<Record>(gt(2)).computeRanges()) {
            assertEquals(3, first().start.index)
            assertEquals(Int.MAX_VALUE, first().endInclusive.index)
        }
        with(RowIndexPredicateLiteral<Record>(gte(2)).computeRanges()) {
            assertEquals(2, first().start.index)
            assertEquals(Int.MAX_VALUE, first().endInclusive.index)
        }
    }

    @Test
    fun `should properly calculate range index with 'and' predicate literal`() {
        with(RowIndexPredicateLiteral<Record>(gt(2) and lt(6)).computeRanges()) {
            assertEquals(3, first().start.index)
            assertEquals(5, first().endInclusive.index)
        }
        with(RowIndexPredicateLiteral<Record>(gte(2) and lte(6)).computeRanges()) {
            assertEquals(2, first().start.index)
            assertEquals(6, first().endInclusive.index)
        }
        with(RowIndexPredicateLiteral<Record>(eq(2) and lte(6)).computeRanges()) {
            assertEquals(2, first().start.index)
            assertEquals(2, first().endInclusive.index)
        }
        with(RowIndexPredicateLiteral<Record>(gte(2) and eq(1)).computeRanges()) {
            assertNull(firstOrNull())
        }
    }

    @Test
    fun `should properly calculate range index with 'or' predicate literal`() {
        with(RowIndexPredicateLiteral<Record>(gt(2) or lt(6)).computeRanges()) {
            assertEquals(0, first().start.index)
            assertEquals(Int.MAX_VALUE, first().endInclusive.index)
        }
        with(RowIndexPredicateLiteral<Record>(gt(6) or lt(5)).computeRanges()) {
            assertTrue(size == 2)
            assertNotNull(find { it.start.index == 0 && it.endInclusive.index == 4 })
            assertNotNull(find { it.start.index == 7 && it.endInclusive.index == Int.MAX_VALUE })
        }
        with(RowIndexPredicateLiteral<Record>(gt(6) or lt(8)).computeRanges()) {
            assertTrue(size == 1)
            assertEquals(0, first().start.index)
            assertEquals(Int.MAX_VALUE, first().endInclusive.index)
        }
        with(RowIndexPredicateLiteral<Record>(gte(6) or lte(6)).computeRanges()) {
            assertTrue(size == 1)
            assertEquals(0, first().start.index)
            assertEquals(Int.MAX_VALUE, first().endInclusive.index)
        }
    }

    @Test
    fun `should properly calculate range index for complex predicate literals`() {
        with(RowIndexPredicateLiteral<Record>(gt(2) and lt(6) and eq(4)).computeRanges()) {
            assertEquals(4, first().start.index)
            assertEquals(4, first().endInclusive.index)
        }
        with(RowIndexPredicateLiteral<Record>(gt(2) and lt(6) and gt(6) and lt(8)).computeRanges()) {
            assertNull(firstOrNull())
        }
        with(RowIndexPredicateLiteral<Record>((gt(2) and lt(6)) or (gt(6) and lt(8)))) {
            with(computeRanges()) {
                assertTrue(size == 2)
                assertNotNull(find { it.start.index == 3 && it.endInclusive.index == 5 })
                assertNotNull(find { it.start.index == 7 && it.endInclusive.index == 7 })
            }
            with(materialize()) {
                assertTrue(contains(RowIndexDef(3)))
                assertTrue(contains(RowIndexDef(4)))
                assertTrue(contains(RowIndexDef(5)))
                assertTrue(contains(RowIndexDef(7)))
            }

        }
    }

}
package io.github.voytech.tabulate.rowpredicate

import io.github.voytech.tabulate.model.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


/*
Row predicates will allow following notations:

1. Insertion of new custom row

 insertRow {
    index { gt(3) and lt(7) }
    cell {
        value = "Some spanned values"
        rowSpan = 2
    }
    cell {
        eval { "Value $it.index" }
    }
 }

2. Enrichment of existing row

 row {
    index { gt(3) and lt(7) }
    cell {
        value = "Some value"
    }
 }

 row {
    when {
        index { gt(3) and lt(7) }  // index works on absolute table row index.
        matching { it.price >= 1000 }  // matching works only on Indexed<T> - record.
    }
    cell {
        value = "Some value"
    }
 }

 */

class RowPredicatesTest {

    data class Record(private val field: String)

    @Test
    fun `should properly calculate range index on simple binary predicate literals`() {
        with(RowIndexPredicateLiteral<Record>(eq(2)).computeRanges()) {
            assertEquals(2, first().start.rowIndex)
            assertEquals(2, first().endInclusive.rowIndex)
        }
        with(RowIndexPredicateLiteral<Record>(lt(2)).computeRanges()) {
            assertEquals(0, first().start.rowIndex)
            assertEquals(1, first().endInclusive.rowIndex)
        }
        with(RowIndexPredicateLiteral<Record>(lte(2)).computeRanges()) {
            assertEquals(0, first().start.rowIndex)
            assertEquals(2, first().endInclusive.rowIndex)
        }
        with(RowIndexPredicateLiteral<Record>(gt(2)).computeRanges()) {
            assertEquals(3, first().start.rowIndex)
            assertEquals(Int.MAX_VALUE, first().endInclusive.rowIndex)
        }
        with(RowIndexPredicateLiteral<Record>(gte(2)).computeRanges()) {
            assertEquals(2, first().start.rowIndex)
            assertEquals(Int.MAX_VALUE, first().endInclusive.rowIndex)
        }
    }

    @Test
    fun `should properly calculate range index with 'and' predicate literal`() {
        with(RowIndexPredicateLiteral<Record>(gt(2) and lt(6)).computeRanges()) {
            assertEquals(3, first().start.rowIndex)
            assertEquals(5, first().endInclusive.rowIndex)
        }
        with(RowIndexPredicateLiteral<Record>(gte(2) and lte(6)).computeRanges()) {
            assertEquals(2, first().start.rowIndex)
            assertEquals(6, first().endInclusive.rowIndex)
        }
        with(RowIndexPredicateLiteral<Record>(eq(2) and lte(6)).computeRanges()) {
            assertEquals(2, first().start.rowIndex)
            assertEquals(2, first().endInclusive.rowIndex)
        }
        with(RowIndexPredicateLiteral<Record>(gte(2) and eq(1)).computeRanges()) {
            assertNull(firstOrNull())
        }
    }

    @Test
    fun `should properly calculate range index with 'or' predicate literal`() {
        with(RowIndexPredicateLiteral<Record>(gt(2) or lt(6)).computeRanges()) {
            assertEquals(0, first().start.rowIndex)
            assertEquals(Int.MAX_VALUE, first().endInclusive.rowIndex)
        }
        with(RowIndexPredicateLiteral<Record>(gt(6) or lt(5)).computeRanges()) {
            assertTrue(size == 2)
            assertNotNull(find { it.start.rowIndex == 0 && it.endInclusive.rowIndex == 4 })
            assertNotNull(find { it.start.rowIndex == 7 && it.endInclusive.rowIndex == Int.MAX_VALUE })
        }
        with(RowIndexPredicateLiteral<Record>(gt(6) or lt(8)).computeRanges()) {
            assertTrue(size == 1)
            assertEquals(0, first().start.rowIndex)
            assertEquals(Int.MAX_VALUE, first().endInclusive.rowIndex)
        }
        with(RowIndexPredicateLiteral<Record>(gte(6) or lte(6)).computeRanges()) {
            assertTrue(size == 1)
            assertEquals(0, first().start.rowIndex)
            assertEquals(Int.MAX_VALUE, first().endInclusive.rowIndex)
        }
    }

    @Test
    fun `should properly calculate range index for complex predicate literals`() {
        with(RowIndexPredicateLiteral<Record>(gt(2) and lt(6) and eq(4)).computeRanges()) {
            assertEquals(4, first().start.rowIndex)
            assertEquals(4, first().endInclusive.rowIndex)
        }
        with(RowIndexPredicateLiteral<Record>(gt(2) and lt(6) and gt(6) and lt(8)).computeRanges()) {
            assertNull(firstOrNull())
        }
        with(RowIndexPredicateLiteral<Record>((gt(2) and lt(6)) or (gt(6) and lt(8))).computeRanges()) {
            assertTrue(size == 2)
            assertNotNull(find { it.start.rowIndex == 3 && it.endInclusive.rowIndex == 5 })
            assertNotNull(find { it.start.rowIndex == 7 && it.endInclusive.rowIndex == 7 })
        }
    }

}
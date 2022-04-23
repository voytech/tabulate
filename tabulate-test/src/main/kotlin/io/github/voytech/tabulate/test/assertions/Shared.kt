package io.github.voytech.tabulate.test.assertions

import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.test.AttributeTest
import kotlin.reflect.KProperty1
import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun interface AssertAttribute<CAT: Attribute<*>, ATTR: CAT> {
    fun testAttribute(attribute: ATTR)
}

class AttributesAssertions<CAT: Attribute<*>>(private vararg val tests: AssertAttribute<CAT,*>) : AttributeTest<CAT> {

    @Suppress("UNCHECKED_CAST")
    override fun performTest(def: Set<CAT>) {
        def.associateBy { it::class.java }.forEach { entry ->
            tests.forEach { test ->
                (test as? AssertAttribute<CAT,CAT>)?.testAttribute(entry.value)
            }
        }
    }
}

class AssertContainsAttributes<CAT: Attribute<*>>(private vararg val targets:  CAT) : AttributeTest<CAT> {
    override fun performTest(def: Set<CAT>) {
        assertEquals(
            targets.toSet(),
            targets.toSet().intersect(def),
            "expected cell attribute set to contain all target extension instances"
        )
    }
}

class AssertMany<CAT: Attribute<*>>(private vararg val cellTests: AttributeTest<CAT>) : AttributeTest<CAT> {
    override fun performTest(def: Set<CAT>) {
        cellTests.forEach { it.performTest(def) }
    }
}

class AssertEqualsAttribute<CAT: Attribute<*>>(
    private val expectedAttribute: CAT,
    private val onlyProperties: Set<KProperty1<out CAT, Any?>>? = null
) : AttributeTest<CAT> {

    @Suppress("UNCHECKED_CAST")
    private fun <A : CAT> A.matchProperties(expected: A) =
        onlyProperties!!.map { it as KProperty1<A, Any?> }.all { it(this) == it(expected) }

    override fun performTest(def: Set<CAT>) {
        if (onlyProperties?.isNotEmpty() == true) {
            assertTrue(
                def.any { it.matchProperties(expectedAttribute) },
                "CellAttribute $expectedAttribute with matching properties ${onlyProperties.joinToString(",") { it.name }} not found"
            )
        } else {
            assertTrue(
                def.contains(expectedAttribute),
                "CellAttribute $expectedAttribute not found"
            )
        }
    }
}

class AssertNoAttribute<CAT: Attribute<*>>(private val expectedAttribute: CAT) : AttributeTest<CAT> {

    override fun performTest(def: Set<CAT>) {
        assertTrue(
            !def.contains(expectedAttribute),
            "CellAttribute $expectedAttribute found but not expected!"
        )
    }
}
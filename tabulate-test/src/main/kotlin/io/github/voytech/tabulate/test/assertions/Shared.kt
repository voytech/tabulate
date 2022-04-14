package io.github.voytech.tabulate.test.assertions

import io.github.voytech.tabulate.model.attributes.Attribute
import io.github.voytech.tabulate.model.attributes.TableAttribute
import io.github.voytech.tabulate.test.AttributeTest
import kotlin.reflect.KClass
import kotlin.test.assertEquals

interface AssertAttribute<CAT: Attribute<*>, ATTR: CAT> {
    fun testAttribute(attribute: ATTR)
    fun attributeClass(): KClass<ATTR>
}

class AttributesAssertions<CAT: Attribute<*>>(private vararg val tests: AssertAttribute<CAT,*>) : AttributeTest<CAT> {

    @Suppress("UNCHECKED_CAST")
    override fun performTest(sheetName: String, def: Set<CAT>?) {
        def?.associate { it::class.java to it }?.forEach { entry ->
            tests.forEach { test ->
                (test as? AssertAttribute<CAT,CAT>)?.testAttribute(entry.value)
            }
        }
    }
}

class AssertContainsAttributes<CAT: Attribute<*>>(private vararg val targets: TableAttribute<*>) : AttributeTest<CAT> {
    override fun performTest(sheetName: String, def: Set<CAT>?) {
        val existingAttributes = def ?: emptyList()
        assertEquals(
            targets.toSet(),
            targets.toSet().intersect(existingAttributes),
            "expected cell attribute set to contain all target extension instances"
        )
    }
}
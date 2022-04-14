package io.github.voytech.tabulate.test.assertions

import io.github.voytech.tabulate.model.attributes.TableAttribute
import kotlin.reflect.KClass

interface AssertTableAttribute {
    fun testTableAttribute(tableAttribute: TableAttribute<*>)
    fun attributeClass(): KClass<out TableAttribute<*>>
}


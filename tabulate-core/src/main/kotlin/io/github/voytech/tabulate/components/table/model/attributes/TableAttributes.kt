package io.github.voytech.tabulate.components.table.model.attributes

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeConstraint


abstract class CellAttribute<T : CellAttribute<T>> : Attribute<T>() {
    override fun getClassifier(): AttributeConstraint<CellAttribute<*>, Table<Any>> = AttributeConstraint.classify()
}

inline fun <reified T: Attribute<*>> Class<T>.classify() = AttributeConstraint.classify<T,Table<Any>>()

abstract class ColumnAttribute<T : ColumnAttribute<T>> : Attribute<T>() {
    override fun getClassifier(): AttributeConstraint<ColumnAttribute<*>, Table<Any>> = AttributeConstraint.classify()
}

abstract class RowAttribute<T : RowAttribute<T>>  : Attribute<T>() {
    override fun getClassifier(): AttributeConstraint<RowAttribute<*>, Table<Any>> = AttributeConstraint.classify()
}

abstract class TableAttribute<T : TableAttribute<T>>  : Attribute<T>() {
    override fun getClassifier(): AttributeConstraint<TableAttribute<*>, Table<Any>> = AttributeConstraint.classify()
}
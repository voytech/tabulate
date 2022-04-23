package io.github.voytech.tabulate.components.table.model.attributes

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeClassifier


abstract class CellAttribute<T : CellAttribute<T>> : Attribute<T>() {
    override fun getClassifier(): AttributeClassifier<CellAttribute<*>, Table<*>> = AttributeClassifier.classify()
}

inline fun <reified T: Attribute<*>> Class<T>.classify() = AttributeClassifier.classify<T,Table<*>>()

abstract class ColumnAttribute<T : ColumnAttribute<T>> : Attribute<T>() {
    override fun getClassifier(): AttributeClassifier<ColumnAttribute<*>, Table<*>> = AttributeClassifier.classify()
}

abstract class RowAttribute<T : RowAttribute<T>>  : Attribute<T>() {
    override fun getClassifier(): AttributeClassifier<RowAttribute<*>, Table<*>> = AttributeClassifier.classify()
}

abstract class TableAttribute<T : TableAttribute<T>>  : Attribute<T>() {
    override fun getClassifier(): AttributeClassifier<TableAttribute<*>, Table<*>> = AttributeClassifier.classify()
}
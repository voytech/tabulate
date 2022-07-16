package io.github.voytech.tabulate.components.table.model.attributes

import io.github.voytech.tabulate.core.model.Attribute

abstract class CellAttribute<T : CellAttribute<T>> : Attribute<T>()

abstract class ColumnAttribute<T : ColumnAttribute<T>> : Attribute<T>()

abstract class RowAttribute<T : RowAttribute<T>>  : Attribute<T>()

abstract class TableAttribute<T : TableAttribute<T>>  : Attribute<T>()
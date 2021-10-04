package io.github.voytech.tabulate.template.context

class TableContext : ContextData()

fun  AttributedTable.crop(): TableContext =
    TableContext().also { it.additionalAttributes = additionalAttributes }

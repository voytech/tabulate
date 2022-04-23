package io.github.voytech.tabulate.core.model

data class DataSourceBinding<T>(
    val dataSource: Iterable<T>,
    val dataSourceRecordClass: Class<*> = (dataSource as Iterable<Any>).first().javaClass
)
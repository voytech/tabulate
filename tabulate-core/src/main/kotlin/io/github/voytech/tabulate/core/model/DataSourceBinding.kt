package io.github.voytech.tabulate.core.model

data class DataSourceBinding<T : Any>(
    val dataSource: Iterable<T>,
    val dataSourceRecordClass: Class<T> = dataSource.first().javaClass
)
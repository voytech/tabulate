package io.github.voytech.tabulate

import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.test.sampledata.SampleCustomer

object Utils {
    fun sampleCustomersTable(data: List<SampleCustomer>): (TableBuilderApi<SampleCustomer>.() -> Unit) = typedTable {
        attributes {
            borders { bottom { 0.2.pt(); solid } }
            text { breakWords; black }
        }
        columns {
            column(SampleCustomer::firstName)
            column(SampleCustomer::lastName)
            column(SampleCustomer::country)
            column(SampleCustomer::city)
            column(SampleCustomer::street)
            column(SampleCustomer::houseNumber)
            column(SampleCustomer::flat)
        }
        rows {
            header {
                columnTitles("First Name", "Last Name", "Country", "City", "Street", "House Nr", "Flat Nr")
                attributes { text { bold; black; fontSize = 12 } }
            }
        }
        dataSource(data)
    }
}
package pl.voytech.exporter.core.builder.fluent;

import pl.voytech.exporter.core.model.*;

public class FluentBuilderTest {
    @SuppressWarnings("unchecked")
    Table<Employee> createTableDefinition() {
        return Table.<Employee>builder()
            .columns()
                .column(Employee::getId)
                    .columnType(CellType.STRING)
                    .build()
                .column(Employee::getFirstName)
                    .columnType(CellType.STRING)
                    .build()
                .column(Employee::getLastName)
                    .columnType(CellType.STRING)
                    .build()
                .build()
            .rows()
                .row(0)
                .cells()
                    .forColumn(Employee::getId)
                        .value("Employee ID")
                        .build()
                    .forColumn(Employee::getFirstName)
                        .value("Employee First Name")
                        .build()
                    .forColumn(Employee::getLastName)
                        .value("Employee Last Name")
                        .build()
                    .build()
                .build()
            .build()
        .build();
    }
}

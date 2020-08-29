package pl.voytech.exporter.core.builder.fluent;

import pl.voytech.exporter.core.model.*;

public class FluentBuilderTest {
    @SuppressWarnings("unchecked")
    Table<Employee> createTableDefinition() {
        return Table.<Employee>builder()
            .columns(
                Column.<Employee>builder().columnType(CellType.STRING),
                Column.<Employee>builder().columnType(CellType.STRING)
            )
            .rows(
                Row.<Employee>builder().createAt(0)
            )
        .build();
    }
}

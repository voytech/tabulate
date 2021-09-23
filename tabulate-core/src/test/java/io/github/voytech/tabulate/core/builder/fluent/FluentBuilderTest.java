package io.github.voytech.tabulate.core.builder.fluent;

import java.util.function.Function;

import io.github.voytech.tabulate.model.CellType;
import io.github.voytech.tabulate.model.SourceRow;
import io.github.voytech.tabulate.model.Table;
import io.github.voytech.tabulate.model.attributes.cell.CellTextStylesAttribute;
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute;
import io.github.voytech.tabulate.model.attributes.row.RowHeightAttribute;
import io.github.voytech.tabulate.model.attributes.table.TemplateFileAttribute;
import org.junit.jupiter.api.Test;

import static io.github.voytech.tabulate.api.qualifer.RowPredicates.allRows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FluentBuilderTest {

    @Test
    public void createTableDefinitionTest() {
        Function<Employee, String> ref = Employee::getId;
        final Table<Employee> employeeTable =
                Table.<Employee>builder()
                    .attribute(CellTextStylesAttribute.Builder::new)
                    .attribute(TemplateFileAttribute.Builder ::new,
                            builder -> builder.setFileName("")
                    )
                    .columns()
                        .column("rowNumbering")
                            .columnType(CellType.NUMERIC)
                            .columnAttribute(ColumnWidthAttribute.Builder::new)
                        .column(Employee::getId)
                            .columnType(CellType.NUMERIC)
                            .columnAttribute(ColumnWidthAttribute.Builder::new)
                        .column(Employee::getFirstName)
                            .columnType(CellType.STRING)
                            .columnAttribute(ColumnWidthAttribute.Builder::new)
                        .column(Employee::getLastName)
                            .columnType(CellType.STRING)
                            .columnAttribute(ColumnWidthAttribute.Builder::new)
                    .rows()
                        .row(0)
                            .rowAttribute(RowHeightAttribute.Builder::new,
                                    builder -> builder.setPx(100)
                            )
                            .cell(0)
                                .value("Nr")
                            .cell(Employee::getId)
                                .value("Employee ID")
                            .cell(2)
                                .value("Employee First Name")
                            .cell(Employee::getLastName)
                                .value("Employee Last Name")
                        .row()
                            .allMatching(allRows())
                            .cell("rowNumbering")
                                .eval(SourceRow::getRowIndex)
                    .build();

        assertNotNull(employeeTable);
        assertNotNull(employeeTable.getTableAttributes());
        assertNotNull(employeeTable.getCellAttributes());
        assertNotNull(employeeTable.getRows());
        assertEquals(employeeTable.getCellAttributes().size(), 1);
        assertEquals(employeeTable.getTableAttributes().size(), 1);
        assertEquals(employeeTable.getColumns().size(), 4);
        assertEquals(employeeTable.getRows().size(), 2);
    }
}

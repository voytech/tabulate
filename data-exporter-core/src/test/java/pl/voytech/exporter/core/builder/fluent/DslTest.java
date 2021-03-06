package pl.voytech.exporter.core.builder.fluent;
import kotlin.ranges.IntRange;
import org.junit.jupiter.api.Test;
import pl.voytech.exporter.core.model.CellType;
import pl.voytech.exporter.core.model.RowSelectors;
import pl.voytech.exporter.core.model.Table;
import pl.voytech.exporter.core.model.SourceRow;
import pl.voytech.exporter.core.model.attributes.functional.FilterAndSortTableAttribute;
import pl.voytech.exporter.core.model.attributes.style.CellFontAttribute;
import pl.voytech.exporter.core.model.attributes.style.ColumnWidthAttribute;
import pl.voytech.exporter.core.model.attributes.style.RowHeightAttribute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DslTest {

    @Test
    public void createTableDefinitionTest() {
        final Table<Employee> employeeTable =
                Table.<Employee>builder()
                    .attribute(new CellFontAttribute())
                    .attribute(new FilterAndSortTableAttribute(new IntRange(0,3), new IntRange(1, 999)))
                    .columns()
                        .column("rowNumbering")
                            .columnType(CellType.NUMERIC)
                            .attribute(new ColumnWidthAttribute())
                        .column(Employee::getId)
                            .columnType(CellType.NUMERIC)
                            .attribute(new ColumnWidthAttribute())
                        .column(Employee::getFirstName)
                            .columnType(CellType.STRING)
                            .attribute(new ColumnWidthAttribute())
                        .column(Employee::getLastName)
                            .columnType(CellType.STRING)
                            .attribute(new ColumnWidthAttribute())
                    .rows()
                        .row(0)
                            .attribute(new RowHeightAttribute(100))
                                .cell(0)
                                    .value("Nr")
                                .forColumn(Employee::getId)
                                    .value("Employee ID")
                                .cell(2)
                                    .value("Employee First Name")
                                .forColumn(Employee::getLastName)
                                    .value("Employee Last Name")
                        .row(RowSelectors.all())
                                .forColumn("rowNumbering")
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

package pl.voytech.exporter.core.builder.fluent;
import kotlin.ranges.IntRange;
import org.junit.Test;
import pl.voytech.exporter.core.model.CellType;
import pl.voytech.exporter.core.model.RowSelectors;
import pl.voytech.exporter.core.model.Table;
import pl.voytech.exporter.core.model.TypedRowData;
import pl.voytech.exporter.core.model.attributes.functional.FilterAndSortTableAttribute;
import pl.voytech.exporter.core.model.attributes.style.CellFontAttribute;
import pl.voytech.exporter.core.model.attributes.style.ColumnWidthAttribute;
import pl.voytech.exporter.core.model.attributes.style.RowHeightAttribute;

import static com.google.common.truth.Truth.assertThat;

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
                            .cells()
                                .cell(0)
                                    .value("Nr")
                                .forColumn(Employee::getId)
                                    .value("Employee ID")
                                .cell(2)
                                    .value("Employee First Name")
                                .forColumn(Employee::getLastName)
                                    .value("Employee Last Name")
                        .row(RowSelectors.all())
                            .cells()
                                .forColumn("rowNumbering")
                                    .eval(TypedRowData::getRowIndex)
                .build();

        assertThat(employeeTable).isNotNull();
        assertThat(employeeTable.getTableAttributes()).isNotNull();
        assertThat(employeeTable.getCellAttributes()).isNotNull();
        assertThat(employeeTable.getRows()).isNotNull();
        assertThat(employeeTable.getCellAttributes().size()).isEqualTo(1);
        assertThat(employeeTable.getTableAttributes().size()).isEqualTo(1);
        assertThat(employeeTable.getColumns().size()).isEqualTo(4);
        assertThat(employeeTable.getRows().size()).isEqualTo(2);
    }
}

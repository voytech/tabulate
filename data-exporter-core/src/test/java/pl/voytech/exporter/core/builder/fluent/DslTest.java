package pl.voytech.exporter.core.builder.fluent;
import kotlin.ranges.IntRange;
import org.junit.Test;
import pl.voytech.exporter.core.model.CellType;
import pl.voytech.exporter.core.model.RowSelectors;
import pl.voytech.exporter.core.model.Table;
import pl.voytech.exporter.core.model.TypedRowData;
import pl.voytech.exporter.core.model.extension.functional.FilterAndSortTableExtension;
import pl.voytech.exporter.core.model.extension.style.CellFontExtension;
import pl.voytech.exporter.core.model.extension.style.ColumnWidthExtension;
import pl.voytech.exporter.core.model.extension.style.RowHeightExtension;

import static com.google.common.truth.Truth.assertThat;

public class DslTest {

    @Test
    public void createTableDefinitionTest() {
        final Table<Employee> employeeTable =
                Table.<Employee>builder()
                    .extension(new CellFontExtension())
                    .extension(new FilterAndSortTableExtension(new IntRange(0,3), new IntRange(1, 999)))
                    .columns()
                        .column("rowNumbering")
                            .columnType(CellType.NUMERIC)
                            .extension(new ColumnWidthExtension())
                        .column(Employee::getId)
                            .columnType(CellType.NUMERIC)
                            .extension(new ColumnWidthExtension())
                        .column(Employee::getFirstName)
                            .columnType(CellType.STRING)
                            .extension(new ColumnWidthExtension())
                        .column(Employee::getLastName)
                            .columnType(CellType.STRING)
                            .extension(new ColumnWidthExtension())
                    .rows()
                        .row(0)
                            .extension(new RowHeightExtension(100))
                            .cells()
                                .forColumn(0)
                                    .value("Nr")
                                .forColumn(Employee::getId)
                                    .value("Employee ID")
                                .forColumn(2)
                                    .value("Employee First Name")
                                .forColumn(Employee::getLastName)
                                    .value("Employee Last Name")
                        .row(RowSelectors.all())
                            .cells()
                                .forColumn("rowNumbering")
                                    .eval(TypedRowData::getRowIndex)
                .build();

        assertThat(employeeTable).isNotNull();
        assertThat(employeeTable.getTableExtensions()).isNotNull();
        assertThat(employeeTable.getCellExtensions()).isNotNull();
        assertThat(employeeTable.getRows()).isNotNull();
        assertThat(employeeTable.getCellExtensions().size()).isEqualTo(1);
        assertThat(employeeTable.getTableExtensions().size()).isEqualTo(1);
        assertThat(employeeTable.getColumns().size()).isEqualTo(4);
        assertThat(employeeTable.getRows().size()).isEqualTo(2);
    }
}

package pl.voytech.exporter.core.builder.fluent;

import pl.voytech.exporter.core.model.CellType;
import pl.voytech.exporter.core.model.Table;
import pl.voytech.exporter.core.model.extension.style.CellFontExtension;
import pl.voytech.exporter.core.model.extension.style.ColumnWidthExtension;
import pl.voytech.exporter.core.model.extension.style.RowHeightExtension;

public class TableDefinitions {

    private static final int HEADER_ROW = 0;
    public static final Table<Employee> EMPLOYEE_TABLE =
            Table.<Employee>builder()
                .extension(new CellFontExtension())
                .columns()
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
                    .row(HEADER_ROW)
                        .extension(new RowHeightExtension(100))
                        .cells()
                            .forColumn(Employee::getId)
                                .value("Employee ID")
                            .forColumn(Employee::getFirstName)
                                .value("Employee First Name")
                            .forColumn(Employee::getLastName)
                                .value("Employee Last Name")
            .build();

}

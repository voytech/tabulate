package io.github.voytech.tabulate.core.builder.fluent;

import java.math.BigDecimal;
import java.util.List;

import com.google.common.collect.Lists;
import io.github.voytech.tabulate.api.builder.fluent.FluentTableBuilderApi;
import io.github.voytech.tabulate.api.builder.fluent.TableBuilder;
import io.github.voytech.tabulate.model.CellType;
import io.github.voytech.tabulate.model.SourceRow;
import io.github.voytech.tabulate.model.attributes.cell.CellTextStylesAttribute;
import io.github.voytech.tabulate.model.attributes.cell.Colors;
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute;
import io.github.voytech.tabulate.model.attributes.row.RowHeightAttribute;
import io.github.voytech.tabulate.model.attributes.table.TemplateFileAttribute;
import io.github.voytech.tabulate.template.TabulationFormat;
import io.github.voytech.tabulate.template.TabulationTemplate;
import io.github.voytech.tabulate.template.context.AttributedRowWithCells;
import io.github.voytech.tabulate.testsupport.AttributedRowTest;
import io.github.voytech.tabulate.testsupport.TestExportOperationsFactory;
import kotlin.Unit;
import org.junit.jupiter.api.Test;

import static io.github.voytech.tabulate.api.builder.RowPredicates.allRows;

public class FluentBuilderTest {

	@Test
	public void createTableDefinitionTest() {
		TestExportOperationsFactory.setRowTest(new AttributedRowTest() {
			public void test(AttributedRowWithCells row) {
				System.out.println(row.toString());
			}
		});
		final FluentTableBuilderApi<Employee> fluentBuilder =
				new TableBuilder<Employee>()
						.attribute(CellTextStylesAttribute::builder, builder -> {
							builder.setUnderline(true);
							builder.setItalic(true);
						})
						.attribute(TemplateFileAttribute::builder,
								builder -> builder.setFileName("file.table")
						)
						.attribute(ColumnWidthAttribute::builder, builder -> builder.setPx(20))
						.attribute(RowHeightAttribute::builder, builder -> builder.setPx(20))
						.columns()
							.column("rowNumbering")
								.columnType(CellType.NUMERIC)
								.columnAttribute(ColumnWidthAttribute::builder)
							.column(Employee::getId)
								.columnType(CellType.NUMERIC)
								.columnAttribute(ColumnWidthAttribute::builder)
							.column(Employee::getFirstName)
								.columnType(CellType.STRING)
								.columnAttribute(ColumnWidthAttribute::builder)
							.column(Employee::getLastName)
								.columnType(CellType.STRING)
								.columnAttribute(ColumnWidthAttribute::builder)
						.rows()
							.row(0)
								.rowAttribute(RowHeightAttribute::builder,
										builder -> builder.setPx(100)
								)
								.cell(0)
									.value("Nr")
									.cellAttribute(CellTextStylesAttribute::builder, builder -> {
										builder.setFontColor(Colors.INSTANCE.getBLUE());
										builder.setFontFamily("Times News Roman");
									})
								.cell(Employee::getId)
									.value("Employee ID")
								.cell(2)
									.value("Employee First Name")
								.cell(Employee::getLastName)
									.value("Employee Last Name")
							.row()
								.allMatching(allRows())
								.cell("rowNumbering")
									.eval(SourceRow::getRowIndex);
		final List<Employee> employeeList = Lists.newArrayList(new Employee(
				"firstName", "lastName", 25, BigDecimal.ZERO, "id"
		));
		new TabulationTemplate<Employee>(TabulationFormat.format("test")).export(
				employeeList, Unit.INSTANCE, fluentBuilder
		);
	}

}

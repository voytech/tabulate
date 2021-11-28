package io.github.voytech.tabulate.core.builder.fluent;

import com.google.common.collect.Lists;
import io.github.voytech.tabulate.api.builder.fluent.FluentTableBuilderApi;
import io.github.voytech.tabulate.api.builder.fluent.TableBuilder;
import io.github.voytech.tabulate.model.NamedPropertyReference;
import io.github.voytech.tabulate.model.SourceRow;
import io.github.voytech.tabulate.model.attributes.cell.CellTextStylesAttribute;
import io.github.voytech.tabulate.model.attributes.cell.Colors;
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute;
import io.github.voytech.tabulate.model.attributes.row.RowHeightAttribute;
import io.github.voytech.tabulate.model.attributes.table.TemplateFileAttribute;
import io.github.voytech.tabulate.template.TabulationFormat;
import io.github.voytech.tabulate.template.TabulationTemplate;
import io.github.voytech.tabulate.template.operations.AttributedRowWithCells;
import io.github.voytech.tabulate.testsupport.AttributedRowTest;
import io.github.voytech.tabulate.testsupport.TestExportOperationsFactory;
import kotlin.Unit;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static io.github.voytech.tabulate.api.builder.RowPredicates.all;


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
								.columnAttribute(ColumnWidthAttribute::builder)
							.column(NamedPropertyReference.of("id", Employee::getId))
								.columnAttribute(ColumnWidthAttribute::builder)
							.column(NamedPropertyReference.of("firstName", Employee::getFirstName))
								.columnAttribute(ColumnWidthAttribute::builder)
							.column(NamedPropertyReference.of("lastName", Employee::getLastName))
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
								.cell(NamedPropertyReference.of("id"))
									.value("Employee ID")
								.cell(2)
									.value("Employee First Name")
								.cell(NamedPropertyReference.of("lastName"))
									.value("Employee Last Name")
							.row(all())
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

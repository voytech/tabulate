package io.github.voytech.tabulate.core.builder.fluent;

import io.github.voytech.tabulate.api.builder.fluent.FluentTableBuilderApi;
import io.github.voytech.tabulate.api.builder.fluent.TableBuilder;
import io.github.voytech.tabulate.model.NamedPropertyReferenceColumnKey;
import io.github.voytech.tabulate.model.SourceRow;
import io.github.voytech.tabulate.model.attributes.cell.CellTextStylesAttribute;
import io.github.voytech.tabulate.model.attributes.cell.Colors;
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute;
import io.github.voytech.tabulate.model.attributes.row.RowHeightAttribute;
import io.github.voytech.tabulate.model.attributes.table.TemplateFileAttribute;
import io.github.voytech.tabulate.template.TabulationFormat;
import io.github.voytech.tabulate.template.TabulationTemplate;
import kotlin.Unit;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static io.github.voytech.tabulate.api.builder.RowPredicates.all;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class FluentBuilderTest {

	@Test
	public void createCustomTableDefinitionTest() {
		final FluentTableBuilderApi<Employee> fluentBuilder = new TableBuilder<Employee>()
			.rows()
				.row(0)
					.cell("nr")
						.value("Nr")
					.cell(NamedPropertyReferenceColumnKey.of("id",Employee::getId))
						.value("Employee ID")
					.cell(NamedPropertyReferenceColumnKey.of("firstName",Employee::getFirstName))
						.value("First Name")
					.cell(NamedPropertyReferenceColumnKey.of("lastName",Employee::getLastName))
						.value("Last Name")
					.cell(NamedPropertyReferenceColumnKey.of("age",Employee::getAge))
						.value("Age")
					.row(all())
						.cell("rowNumbering")
						.eval(SourceRow::getRowIndex);

		final List<Employee> employeeList = Collections.singletonList(new Employee(
				"firstName", "lastName", 25, BigDecimal.ZERO, "id"
		));
		new TabulationTemplate<Employee>(TabulationFormat.format("spy")).export(employeeList, Unit.INSTANCE, fluentBuilder.build());
		assertTrue(true);
	}

	@Test
	public void createTableDefinitionTest() {
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
							.column(NamedPropertyReferenceColumnKey.of("id", Employee::getId))
								.columnAttribute(ColumnWidthAttribute::builder)
							.column(NamedPropertyReferenceColumnKey.of("firstName", Employee::getFirstName))
								.columnAttribute(ColumnWidthAttribute::builder)
							.column(NamedPropertyReferenceColumnKey.of("lastName", Employee::getLastName))
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
								.cell(NamedPropertyReferenceColumnKey.of("id"))
									.value("Employee ID")
								.cell(2)
									.value("Employee First Name")
								.cell(NamedPropertyReferenceColumnKey.of("lastName"))
									.value("Employee Last Name")
							.row(all())
								.cell("rowNumbering")
									.eval(SourceRow::getRowIndex);
		final List<Employee> employeeList = Collections.singletonList(new Employee(
				"firstName", "lastName", 25, BigDecimal.ZERO, "id"
		));
		new TabulationTemplate<Employee>(TabulationFormat.format("spy")).export(
				employeeList, Unit.INSTANCE, fluentBuilder.build()
		);
	}

}

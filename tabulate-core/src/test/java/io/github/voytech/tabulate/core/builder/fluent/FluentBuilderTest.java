package io.github.voytech.tabulate.core.builder.fluent;

import io.github.voytech.tabulate.components.table.api.builder.fluent.FluentTableBuilderApi;
import io.github.voytech.tabulate.components.table.api.builder.fluent.TableAttributes;
import io.github.voytech.tabulate.components.table.api.builder.fluent.TableBuilder;
import io.github.voytech.tabulate.components.table.model.SourceRow;
import io.github.voytech.tabulate.components.table.model.attributes.table.TemplateFileAttribute;
import io.github.voytech.tabulate.core.model.Height;
import io.github.voytech.tabulate.core.model.UnitsOfMeasure;
import io.github.voytech.tabulate.core.model.Width;
import io.github.voytech.tabulate.core.model.color.Colors;
import io.github.voytech.tabulate.core.model.text.DefaultFonts;
import io.github.voytech.tabulate.support.mock.Spy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static io.github.voytech.tabulate.components.table.api.builder.RowPredicates.all;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class FluentBuilderTest {

	@AfterEach
	public void cleanup() {
		Spy.Companion.getSpy().readHistory();
	}

	@Test
	public void createCustomTableDefinitionTest() {
		final FluentTableBuilderApi<Employee> fluentBuilder = new TableBuilder<Employee>()
			.rows()
				.row(0)
					.cell("nr")
						.value("Nr")
					.cell("id",Employee::getId)
						.value("Employee ID")
					.cell("firstName",Employee::getFirstName)
						.value("First Name")
					.cell("lastName",Employee::getLastName)
						.value("Last Name")
					.cell("age",Employee::getAge)
						.value("Age")
					.row(all())
						.cell("rowNumbering")
						.eval(SourceRow::getRowIndex);

		final List<Employee> employeeList = Collections.singletonList(new Employee(
				"firstName", "lastName", 25, BigDecimal.ZERO, "id"
		));
		//new StandaloneExportTemplate(format("spy")).export(fluentBuilder.build(),Unit.INSTANCE,employeeList);
		assertTrue(true);
	}

	@Test
	public void createTableDefinitionTest() {
		final FluentTableBuilderApi<Employee> fluentBuilder =
				new TableBuilder<Employee>()
						.attribute(TableAttributes::textAttribute, builder -> {
							builder.setUnderline(true);
							builder.setItalic(true);
						})
						.attribute(TemplateFileAttribute::builder,
								builder -> builder.setFileName("file.table")
						)
						.attribute(
							TableAttributes::widthAttribute,
							builder -> builder.setValue(new Width(20f, UnitsOfMeasure.PT))
						)
						.attribute(
								TableAttributes::heightAttribute,
								builder -> builder.setValue(new Height(20f, UnitsOfMeasure.PT)))
						.columns()
							.column("rowNumbering")
								.attribute(TableAttributes::widthAttribute)
							.column("id", Employee::getId)
								.attribute(TableAttributes::widthAttribute)
							.column("firstName", Employee::getFirstName)
								.attribute(TableAttributes::widthAttribute)
							.column("lastName", Employee::getLastName)
								.attribute(TableAttributes::widthAttribute)
						.rows()
							.row(0)
								.attribute(TableAttributes::heightAttribute,
										builder -> builder.setValue(new Height(100f, UnitsOfMeasure.PT))
								)
								.cell(0)
									.value("Nr")
									.attribute(TableAttributes::textAttribute, builder -> {
										builder.setColor(Colors.BLUE);
										builder.setFontFamily(DefaultFonts.TIMES_NEW_ROMAN);
									})
								.cell("id")
									.value("Employee ID")
								.cell(2)
									.value("Employee First Name")
								.cell("lastName")
									.value("Employee Last Name")
							.row(all())
								.cell("rowNumbering")
									.eval(SourceRow::getRowIndex);
		final List<Employee> employeeList = Collections.singletonList(new Employee(
				"firstName", "lastName", 25, BigDecimal.ZERO, "id"
		));
		//new StandaloneExportTemplate(format("spy")).export(fluentBuilder.build(),Unit.INSTANCE,employeeList);
	}

}

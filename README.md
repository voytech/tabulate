
# Tabulate

[![CodeFactor](https://www.codefactor.io/repository/github/voytech/tabulate/badge?s=356351985a7dd58359040b23f6d896d28af928af)](https://www.codefactor.io/repository/github/voytech/tabulate)
[![Java CI with Gradle](https://github.com/voytech/tabulate/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/voytech/tabulate/actions/workflows/gradle.yml)

Tabulate aids you in exporting your data collections into various tabular file formats.    

## Why ?

Data reporting / exporting can be sometimes very tedious and cumbersome task - especially when your business is strongly tied with well established forms of reporting like spreadsheets and wants to have reports on each business path. Such a requirement often encourages us to create various abstractions, helpers and other utilities to hide vendor API usage details and to make the task less repetitive, more compact and faster.

## When ...    

... You need to export tabular data into following formats: 
- excel (xlsx),
- pdf (WIP)

... You want to have control over column, row, cell styles.  

... You do not want to expose imperative vendor API in business logic code.

... You do not want to write your own abstractions. 

... You want to easily extend functionalities with your own custom attributes as You require.
 
## Features (v0.1.0)

### Tabular data export from collection data sources.

Suppose we are developing kind of 'sales reports' service and we have following spring JPA repository method: 
```kotlin
fun loadProductsByDate(date: LocalDate): List<Product> { ... }
```
or
```kotlin
fun loadProductsByDate(date: LocalDate): Flux<Product> { ... }
```
then we can make following:
```kotlin
productsRepository.loadProductsByDate(now()).tabulate("file/path/products.xlsx") {
            name = "Products table"
            firstRow = 1
            columns {
                column(Product::code)
                column(Product::name)
                column(Product::description)
                    ...
            }
        }
```

### Creating custom rows and cell values.

### Mixing exporting from collection data sources and custom cell values.

### Injecting custom row definitions via type bound predicates.  

### Support for various attributes on all table levels: table, columns, row, cell.

You can use attributes for various purposes - for styling, for formatting, hooking with data and so on. 
All attributes are resolved from top to down and attributes at lower level takes precedence as they are more specific.
Attributes of same type are merged in such a way that higher level attribute fields are overridden by lower level corresponding fields if they are present. 

```kotlin
productsRepository.loadProductsByDate(now()).tabulate("product_with_styles.xlsx") {
    name = "Products table"
    columns {
        column(Product::code) {
            attributes(
                width { auto = true },
                text {
                    fontFamily = "Times New Roman"
                    fontColor = Colors.BLACK
                    fontSize = 12
                },
                background { color = Colors.BLUE }
            )
        }
        column(Product::name) {
            attributes(width { auto = true })
        }
        column(Product::description) {
            attributes(width { auto = true })
        }
        column(Product::manufacturer) {
            attributes(width { auto = true })
        }
        column(Product::price) {
            attributes(width { auto = true })
        }
        column(Product::distributionDate) {
            attributes(
                width { auto = true },
                dataFormat { value = "dd.mm.YYYY" }
            )
        }
    }
    rows {
        row {
            attributes(
                text {
                    fontFamily = "Times New Roman"
                    fontColor = Colors.BLACK
                    fontSize = 12
                },
                background { color = Colors.BLUE }
            )
        }
    }
}
```

### DSL Kotlin API.
Pretty neat:

```kotlin
loadProducts(1000).tabulate("test2.xlsx") {
            name = "Products table"
            firstRow = 1
            attributes(template { fileName = "src/test/resources/template.xlsx" })
            columns {
                column(Product::code)
                column(Product::name)
                column(Product::description)
                column(Product::manufacturer)
                column(Product::distributionDate) {
                    attributes(
                        dataFormat { value = "dd.mm.YYYY" }
                    )
                }
            }
        }
```

### Fluent builders Java API.
More verbose:

```java

Table<Employee> employeeTable =
		Table.<Employee>builder()
		.attribute(new TemplateFileAttribute("file.xlsx"))
		.columns()
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
		    .row()
		        .attribute(new RowHeightAttribute(100))
		.build();
```

### Extensible attribute model.

### Extensible and customizable exporting operations logic (Java SPI ServiceLoader).

## How ?
 

## License 

```
Copyright 2020 Wojciech Mąka.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

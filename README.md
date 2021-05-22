
# Tabulate

[![CodeFactor](https://www.codefactor.io/repository/github/voytech/tabulate/badge?s=356351985a7dd58359040b23f6d896d28af928af)](https://www.codefactor.io/repository/github/voytech/tabulate)
[![Java CI with Gradle](https://github.com/voytech/tabulate/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/voytech/tabulate/actions/workflows/gradle.yml)

Tabulate aids you in exporting your data collections into various tabular file formats.    

## Why ?

Data reporting and exporting can be sometimes very tedious and cumbersome task - especially when your business wants to have reports covering vast majority of system functionalities. Writing every exporting method using imperative 3rd party API directly will probably make code verbose, error-prone and hard to read/maintain. This is the reason why many developers choose to hide implementation details using clever abstractions. In the end there is another solution - One can delegate abstracting part to some external library which exposes declarative API.

Tabulate tries to mitigate those little inconveniences by offering You third option.

## You should consider using tabulate if:    

- You need to export collections of objects into: 
    - excel (xlsx),
    - pdf (WIP)
    
- You expect exported data to be formatted as table. 

- You need to create reports from the same data (and possibly using the same table structure) in many tabular formats (excel, pdf)
  
- You need to apply style attributes (font family, colors, borders ...) on columns, rows, or selected cells in format agnostic manner (e.g. same styles can be applied on both: xlsx and pdf files).  

- You don't like to mix high level business logic with more technical implementation.

- You want to register new attributes for cells, rows, columns or table.

- You want to extend (wrap) DSL type-safe API. 

## You should not use this library if: 

- You need to export collection into non-tabular format.

## Key concepts

### Take most from Kotlin DSL API.

Kotlin DSL API helps a lot with describing desired target table structure.
It makes it look very concise and readable. Maintaining table definition becomes much more easier not only because of visual aspects, but what is more important due to type-safety offered by Kotlin. At coding time, your IDE will make use of it by offering completion hints - this elevates developer experience as almost zero documentation is required to start. 
```kotlin
    loadProducts().tabulate("products.xlsx") {
          name = "Products table"
          attributes { 
            template { fileName = "src/test/resources/template.xlsx" } 
          }
          columns {
              column(Product::code)
              column(Product::name)
              column(Product::description)
              column(Product::manufacturer)
              column(Product::distributionDate) {
                  attributes {
                    dataFormat { value = "dd.mm.YYYY" }
                  }
              }
          }
    }
```
DSL is built from functions taking lambda receivers - this makes it possible to configure builders in scope with some language support. E.g. using loops as below:
```kotlin
    val additionalProducts = ...
    tabule {
          name = "Products table"   
          attributes { 
            template { fileName = "src/test/resources/template.xlsx" } 
          }
          rows { 
              header("Code", "Name", "Description", "Manufacturer")
              additionalProducts.forEach {
                  row { 
                      cells {
                          cell { value = it.code }
                          cell { value = it.name }
                          cell { value = it.description }
                          cell { value = it.manufacturer }
                      }
                  }
              }
          }  
    }.export("products.xlsx")
```

### sadsd
### dasdsad

## Other features (v0.1.0)


### Fluent builders Java API.
Old fashioned Java fluent builder API is also supported... but it looks much more verbose:

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

### Column bound object property extractors.
Column API makes it possible to pass property getter reference as a column key. 
Using this approach creates object to column binding to be used later at run time for cell value evaluation.
```kotlin
productsRepository.loadProductsByDate(now()).tabulate("file/path/products.xlsx") {
            name = "Products table"
            columns {
                column(Product::code)
                column(Product::name)
                column(Product::description)
            }
        }
```

### First class support to reactive streams API.

Assume we are developing reactive web application and we have already created reactive Spring JPA repository like: 
```kotlin
fun Flux<Product> loadProductsByDate() { ... }
```
You can now call extension method:
```kotlin
productsRepository.loadProductsByDate(now()).tabulate("file/path/products.xlsx")
```
and You are all done. When you call tabulate - You are in fact subscribing to publisher which will push records from database reactivelly. 

### Inserting custom rows.

- setting custom cell style

- defining row and col spans.

- inserting images.

### Merging rows (and derived attributes) qualified by row context predicates.  

### Mixing object collection rows with predefined rows definitions.

### Library of style and structural attributes for all table levels (table, column, row, cell).


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
### Extensible attribute model.

### Extensible and customizable exporting operations logic (Java SPI ServiceLoader).

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

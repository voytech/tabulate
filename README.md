
# Tabulate

[![CodeFactor](https://www.codefactor.io/repository/github/voytech/tabulate/badge?s=356351985a7dd58359040b23f6d896d28af928af)](https://www.codefactor.io/repository/github/voytech/tabulate)
[![Java CI with Gradle](https://github.com/voytech/tabulate/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/voytech/tabulate/actions/workflows/gradle.yml)

Tabulate aids you in exporting your data collections into various tabular file formats.    

## Why ?

Exporting data to external file formats can be tedious and cumbersome - especially when your business wants to have reports covering vast majority of system functionalities. Writing every exporting method using imperative API directly will soon make code verbose, error prone, hard to read and maintain. This is the reason why many developers choose to hide implementation details using clever abstractions. In the end there is another solution - One can delegate abstracting part to some external library which exposes declarative API.

Tabulate tries to mitigate those little inconveniences by offering You third option.

## You should consider using tabulate if:    

- You need to export objects into: 
    - excel (xlsx),
    - pdf (currently work in progress)
    
- Exported data needs to be table-formatted. 

- You want to reuse table structure and apply it to many tabular file formats (excel, pdf)
  
- You need to preserve consistent styling (e.g. same cell styles can be applied on both: xlsx and pdf files).  

- You want to add new styles or other attributes as long as they are not included in basic package.

- You want to operate using extensible and type-safe API (DSL)

## You should not use this library if: 

- You need to export collection into non-tabular format.

## Key concepts

### Table model with attributes.

Table model makes it possible to declare how table will look like after data binding and exporting. It is composed of standard well known entities:  

- column
- row  
- row cell 

Above entities are sufficient if You only want to layout cell values within table. 
In order to gain more control over underlying backend API You need to start using attributes.

Attributes are plain objects with inner properties that extends base model. Attributes can be installed on multiple levels: _table_, _column_, _row_ and single _cell_ levels. There can be many categories of attributes. E.g. there are cell style attributes, column and row structural attributes (column width, row height), global - table attributes (e.g. input template file to read and interpolate data on it)

### Template pattern and table export operations.

Template is an entry point to exporting facility. It orchestrates all table export operations: 
- it instantiates result object and 3rd party underlying APIs,
- it requests data records one by one and delegates context rendering to table operations implementors.
- it finalizes operations  (e.g. flushing output stream)

### Table DSL API.

Kotlin type-safe DSL helps a lot with describing target table structure.
It makes it look very concise and readable. Maintaining table definition becomes much easier due to type-safety given by Kotlin DSL. At coding time, your IDE will make use of it by offering completion hints - this elevates developer experience, as almost zero documentation is required to start. 
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
DSL functions take lambda receivers as arguments. This allows nested DSL APIs with lexical scoping restrictions + calling accessible functions which can be powerful. Look at below example - using 'forEach' in inner 'rows' API:
```kotlin
    val additionalProducts = ...
    tabulate {
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

### Table DSL extension functions.

Together with possibility of nesting DSLs comes another powerful feature - extending each DSL level by using extension functions on DSL API builder classes.  

Take the example from previous section: 
```kotlin
    val additionalProducts = ...
    tabulate {
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
method 'header' is implemented as follows: 

```kotlin
fun <T> RowsBuilderApi<T>.header(vararg names: String) =
    row {
        insertWhen { it.rowIndex == 0 && !it.hasRecord()}
        cells {
            names.forEach {
                cell { value = it }
            }
        }
    }
```
So You are free to extend BuilderApi DSL in order to create various shortcuts and templates. 
It is worth mentioning that by using extension functions on DSL builders - scope becomes restricted to extended DSL builder receiver.
Thus - it will not be allowed to break table definition by calling methods from patent builders. 

This property becomes especially attractive in contex of custom attributes:

### Column bound cell value extractors.

Column API makes it possible to pass property getter reference as a column key. 
This creates object to column binding that is applied later at run time for cell value evaluation.
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

### First-class support for reactive streams SPI.

Assume we are developing reactive web application and we have already created reactive Spring JPA repository like: 
```kotlin
fun Flux<Product> loadProducts() { ... }
```
You can now call extension method:
```kotlin
productsRepository.loadProducts().tabulate("file/path/products.xlsx") { .. configure table look and feel ...}
```
and You are all done. When you call tabulate - You are in fact subscribing to publisher which will push records from database reactivelly. 


## Other features (v0.1.0)


### Java interop - fluent builders Java API.
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

### Custom rows.

Sometimes in addition to record from external source - You need to add user defined rows.
Standard table typically needs a header row or summary footer row with totals.
It is also possible to define interleaving custom rows at specified index or matching qualifier predicate.

Row model allows to define custom cell values as well as cell styles and attributes only.
It acts as conveyor for additional features for existing external source derived rows, or as a factory for standalone custom rows that can be hooked at definition time.

Things You can achieve with Row model includes:

- setting custom cell styles,
- setting row-level attributes (e.g row height)
- defining row and col spans,
- inserting images,
- setting cell values of different types.


### Merging rows.  

When multiple Row model definitions are qualified by predicate, this will result in single synthetic merged row where:
- row level attributes will be concatenated or merged if are of same type,
- cell values will be concatenated, or overriden by last cell occurence at given column,
- cell level attributes will be concatenated, or merged if of same type.

### Library of attributes.

You can use attributes for various purposes - for styling, for formatting, hooking with data and so on. 
Currently with tabulate-core you will get following attributes included:

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

### Extensible exporting operations (Java SPI ServiceLoader).

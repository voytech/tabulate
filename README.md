
# Tabulate

[![CodeFactor](https://www.codefactor.io/repository/github/voytech/tabulate/badge?s=356351985a7dd58359040b23f6d896d28af928af)](https://www.codefactor.io/repository/github/voytech/tabulate)
[![Java CI with Gradle](https://github.com/voytech/tabulate/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/voytech/tabulate/actions/workflows/gradle.yml)

Tabulate aids you in exporting your data collections into various tabular file formats.    

## Why ?

Exporting data to external file formats can be tedious and cumbersome - especially when your business wants to have reports covering vast majority of system functionalities. Writing every exporting method using imperative API directly will soon make code verbose, error prone, hard to read and maintain. This is the reason why many developers choose to hide implementation details using clever abstractions. In the end there is another solution - One can delegate abstracting part to some external library which exposes declarative API.

Tabulate tries to mitigate those little inconveniences by offering You third option.

### You should consider using tabulate if:    

- You need to export objects into: 
    - excel (xlsx),
    - pdf (currently work in progress)
    
- Exported data needs to be table-formatted. 

- You want to reuse table structure and apply it to many tabular file formats (excel, pdf)
  
- You need to preserve consistent styling (e.g. same cell styles can be applied on both: xlsx and pdf files).  

- You want to add new styles or other attributes as long as they are not included in basic package.

- You want to operate using extensible and type-safe API (DSL)

### You should not use this library if: 

- You need to export collection into non-tabular format.

## Key concepts

### Table model with attributes.

Table model describes how table will look like after data binding and exporting. It is composed of standard well known entities:  

- column
- row  
- row cell 

Table model entities in typical arrangement: 

```kotlin
productList.tabulate("file.xlsx") {
    name = "Table id"
    columns {
        column("nr") {
            ...
        }
        column(Product::code) {
            ...
        }
        ...
    }
    rows {
        row {  // first row when no index provided.
            cells {
                cell("nr") { value = "Nr.:" }
                ...
            }
        }
        row { // second row when no index provided.                       
            cells {
                cell(Product::code) { ... }
            }
        }
    }
}
```
Above will suffice if You only want to layout cell values within a table. 
In order to gain more control over underlying backend API You need to start using attributes.

Attributes are plain objects with inner properties that extends base model. Attributes can be installed on multiple levels: _table_, _column_, _row_ and single _cell_ levels. There can be many categories of attributes. E.g. there are cell style attributes, column and row structural attributes (column width, row height), global - table attributes (e.g. input template file to read and interpolate data on it)

Example with attributes included
```kotlin
productList.tabulate("file.xlsx") {
    name = "Table id"
    attributes {
      filterAndSort {}
    }
    columns {
        column("nr") {
            attributes { width { px = 40 }}
            ...
        }
        column(Product::code) {
            attributes { width { auto = true}}
            ...
        }
        ...
    }
    rows {
        row {  // first row when no index provided.
            cells {
                cell("nr") { 
                  value = "Nr.:" 
                  attributes {
                    text {
                      fontFamily = "Times New Roman"
                      fontColor = Colors.BLACK
                      fontSize = 12
                    }
                    background { color = Colors.BLUE }
                  }  
                }
                ...
            }
        }
    }
}
```

### Template pattern and table export operations.

Template is an entry point to exporting facility. It orchestrates all table export operations: 
- it instantiates result object and 3rd party underlying APIs,
- it requests data records one by one and delegates context rendering to table operations implementors.
- it finalizes operations  (e.g. flushing output stream)

Template is also responsible for resolving export operations implementors. For this purpose it uses invocation argument - `TabulationFormat`.
There can be many implementations of `TabulationFormat`. As you will se on examples below - by default there is no explicit pointing what table operation implementors to choose.
Typically, it is sufficient to pass a file name with extension like 'file.xlsx'. Template will lookup for extension and apply default excel table export operations.

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
    tabulate {
          rows { 
              header("Code", "Name", "Description", "Manufacturer")
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

Extension functions for DSL API builders become trully attractive in context of user defined attributes:

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
Old fashioned Java fluent builder API is also supported. It is needless to say it looks much less attractive:

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

Sometimes, in addition to records from external source - You need to add user defined rows.
Standard table typically needs a header row or summary footer row with totals.
It is also possible to define interleaving custom rows at specified index or matching qualifier predicate.

Row model allows to define custom cell values as well as cell styles and attributes only.
It acts as conveyor for additional features for existing external source derived rows, or as a factory for standalone custom rows that can be hooked at definition time.

Things You can achieve with Row model in terms of custom rows includes:

- setting custom cell styles,
- setting row-level attributes (e.g. row height)
- defining row and col spans,
- inserting images,
- setting cell values of different types.


### Merging rows.  

When multiple `Row` model definitions are qualified by a predicate, they form a single synthetic merged row. Following rules regarding row merge applies:
- Row level attributes will be concatenated or merged if are of same type.
- Cell values will be concatenated, or overriden by last cell occurence at given column.
- Cell level attributes will be concatenated, or merged if of same type.
- Two attributes of same type are merged by overriding clashing attribute properties from left to right where on left side stands attribute from higher level (e.g. row level), and on right site stands attribute from lower level (e.g. cell level).

### Library of attributes.

You can use attributes for various purposes - for styling, for formatting, hooking with data and so on. 

Currently, with tabulate-core + tabulate-excel you will get following attributes included:

#### Table attributes
- Filter and sort attribute (Excel only),
- Template file attribute 

#### Column attributes
- Column width attribute 

#### Row attribtues 
- Row height attribute 

#### Cell attribtues 
- Cell text style attribute,
- Cell border attribute,
- Cell background attribute, 
- Cell text alignment attribute

Typical usage scenario for selected attributes:
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
## Extension points.

**Tabulate** was designed with extensibility in mind, so it is possible to:
- add user defined attributes,
- add custom renderers for an attribute to enable it in selected export operations implementation (e.g. single attribute needs to be rendered differently for xlsx and pdf ),  
- implement table export operations from scratch (e.g. html table, ascii table, mock renderer for testing)

### Implementing new table export operations
In order to support new tabular file format you have to extend `ExportOperationsConfiguringFactory<C, T, O>` where:
- `C` stands for rendering context - which is usually wrapper around 3rd party imperative api like Apache POI,
- `T` stands for object class representing entry in exported collection,
- `O` stands for type of result of operation (e.g. `OutputStream` for Apache POI)

As long as tabulate uses ServiceLoader infrastructure, You need to create file `resource/META-INF/io.github.voytech.tabulate.template.spi.ExportOperationsProvider`, and put fully qualified class name of our factory in the first line. **This step is required by a template in order to resolve your extension at run-time**. 

Basic implementation can look like this:

```kotlin

class ExampleExportOperationsConfiguringFactory<T> : ExportOperationsConfiguringFactory<Unit, T, OutputStream>() {
    
    private lateinit var stream: OutputStream
    private lateinit var writer: PrintStream

    override fun getFormat() = "txt"

    override fun provideRenderingContext() { }

    override fun getExportOperationsFactory(renderingContext: Unit): ExportOperationsFactory<T, OutputStream> =
        object : ExportOperationsFactory<T, OutputStream> {
            override fun createLifecycleOperations(): LifecycleOperations<T, Unit> =
                object : LifecycleOperations<T, Unit> {
                    override fun initialize(source: Publisher<T>, resultHandler: ResultHandler<T, OutputStream>) {
                        stream = resultHandler.createResult(source)
                        writer = PrintStream(stream)
                    }

                    override fun finish() {
                        writer.close()
                    }
                }

            override fun createTableRenderOperations(): TableRenderOperations<T> =
                object : TableRenderOperations<T> {

                    override fun beginRow(context: AttributedRow) = 
                         writer.print(context.rowCellValues.entries.map { it.value.getRawValue() }.joinToString { ","})
                    
                    override fun renderRowCell(context: AttributedCell) = {
                        println("Do nothing in here. Will write entire row at once.")
                    }
                  
                    override fun endRow(context: AttributedRow) = writer.print("\n")
                  
                }

            override fun createTableOperation(): TableOperation<T> = object : TableOperation<T> {
                override fun createTable(builder: TableBuilder<T>): Table<T> = super.createTable(builder)
            }
        }
}
```

If above would be something more sophisticated than simple csv, You could then add support for rendering default attributes as follow: 

```kotlin
class ExampleExportOperationsConfiguringFactory<T> : ExportOperationsConfiguringFactory<SomeRenderingContext, T, OutputStream>() {
    
    ...
    override fun getExportOperationsFactory(renderingContext: SomeRenderingContext): ExportOperationsFactory<T, OutputStream> = .. as in example above ..
    
    override fun getAttributeOperationsFactory(renderingContext: SomeRenderingContext): AttributeRenderOperationsFactory<T> =
      object : AttributeRenderOperationsFactory<T> {
        override fun createTableAttributeRenderOperations(): Set<AdaptingTableAttributeRenderOperation<SomeRenderingContext, out TableAttribute>> =
          tableAttributesOperations(renderingContext) // method returns set of table attribute render operations

        override fun createRowAttributeRenderOperations(): Set<AdaptingRowAttributeRenderOperation<Unit, T, out RowAttribute>> =
          rowAttributesOperations(renderingContext) // method returns set of row attribute render operations

        override fun createColumnAttributeRenderOperations(): Set<AdaptingColumnAttributeRenderOperation<SomeRenderingContext, out ColumnAttribute>> =
          columnAttributesOperations(renderingContext) // method returns set of column attribute render operations

        override fun createCellAttributeRenderOperations(): Set<AdaptingCellAttributeRenderOperation<SomeRenderingContext, out CellAttribute>> =
          cellAttributesOperations(renderingContext) // method returns set of cell attribute render operations
      }    
    
}
```

### Registering new attribute types for specific export operations implementation
It is possible that you have requirements which are not provided out of the box by default export operation implementation. Assume You want to use excel exporter, but there is lack of certain attribute support: 

```kotlin
class CustomAttributeRendersOperationsProvider<T> : AttributeRenderOperationsProvider<ApachePoiExcelFacade,T> {

    override fun getFormat() = "xlsx"

    override fun getAttributeOperationsFactory(creationContext: ApachePoiExcelFacade): AttributeRenderOperationsFactory<T> {
        return object : AttributeRenderOperationsFactory<T> {
            override fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<out CellAttributeAlias>> =
                setOf(MarkerCellAttributeRenderOperation(creationContext))
        }
    }
}

```
Then You need to implement this marker attribute togheter with DSL API extension function and attribute render operation to instruct 3rd party Apache Poi API how to render this attribute. 

```kotlin
data class MarkerCellAttribute(val text: String) : CellAttribute<MarkerCellAttribute>() {

    class Builder(var text: String = "") : CellAttributeBuilder<MarkerCellAttribute> {
        override fun build(): MarkerCellAttribute = MarkerCellAttribute(text)
    }
}

class SimpleMarkerCellAttributeRenderOperation(poi: ApachePoiExcelFacade) :
    AdaptingCellAttributeRenderOperation<ApachePoiExcelFacade, SimpleTestCellAttribute>(poi) {

    override fun attributeType(): Class<out MarkerCellAttribute> = MarkerCellAttribute::class.java

    override fun renderAttribute(context: RowCellContext, attribute: MarkerCellAttribute) {
        with(adaptee.assertCell(
            context.getTableId(),
            context.rowIndex,
            context.columnIndex
        )) {
            this.setCellValue("${this.stringCellValue} [ ${attribute.label} ]")
        }
    }

}

fun <T> CellLevelAttributesBuilderApi<T>.label(block: MarkerCellAttribute.Builder.() -> Unit) =
    attribute(MarkerCellAttribute.Builder().apply(block).build())
```
Finally, You need to create file `resource/META-INF/io.github.voytech.tabulate.template.spi.AttributeRenderOperationsProvider`, and put fully qualified class name of our factory in the first line.

### Extending Table DSL API

In the last section You saw how to define user defined attributes. The last step involves creating extension function on specific DSL attribute API. As DSL builder class suggests - `CellLevelAttributesBuilderApi<T>` - this builder is part of a Cell DSL API only , which means that it won't be possible to add this attribute on row, column and table, but only on a cell level. You can leverage this behaviour for restricting say 'mounting points' of specific attributes. In order to enable cell attribute on all levels You need to add more extension functions as follows: 

```kotlin
fun <T> ColumnLevelAttributesBuilderApi<T>.label(block: MarkerCellAttribute.Builder.() -> Unit) =
    attribute(MarkerCellAttribute.Builder().apply(block).build())
fun <T> RowLevelAttributesBuilderApi<T>.label(block: MarkerCellAttribute.Builder.() -> Unit) =
  attribute(MarkerCellAttribute.Builder().apply(block).build())
fun <T> TableLevelAttributesBuilderApi<T>.label(block: MarkerCellAttribute.Builder.() -> Unit) =
  attribute(MarkerCellAttribute.Builder().apply(block).build())
```
Now You can call `label` on all DSL API levels in `attributes` scope like:

```kotlin
productList.tabulate("file.xlsx") {
    name = "Table id"
    attributes {
      label { text = "TABLE" }
    }
    columns {
        column("nr") {
            attributes { label { text = "COLUMN" } }
            ...
        }
    }
    rows {
        row {
           attributes { label { text = "ROW" } }
            cells {
                cell("nr") { 
                  value = "Nr.:" 
                  attributes {
                    attributes { label { text = "CELL" } }
                  }  
                }
                ...
            }
        }
    }
}
```
The result of above configuration will be as such: 
- In the first row, cell at a column with id "nr" will end with `[ CELL ]`, and rest of cells will end with `[ ROW ]`,
- Remaining cells (starting from second row) in a column with id "nr" will end with `[ COLUMN ]`,
- All remaining cells will end with `[ TABLE ]`.

## Roadmap

Starting from version 0.1.0, minor version will advance relatively fast due to tiny milestones.
This is because of one person (me) who is currently in charge, and due to my intention of "non-blocking realese cycles" for too long.

### v0.2.x
 
- PDF table export operations implementation.
- Definition time validation for cell spans.

### v0.3.x

- Composition of multiple table models (TableBuilder.include).

### v0.4.x

- Multi-part output files. (chunking large files)

### v0.5.x

- CSS support.

### v0.6.x

- Explicit attribute categories (Statically typed).

### v0.7.x

- Codegen for user defined attributes.

### v0.8.x 

- Spring framework integration (Spring Data)

### TBD ...


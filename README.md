
# Tabulate

[![CodeFactor](https://www.codefactor.io/repository/github/voytech/tabulate/badge?s=356351985a7dd58359040b23f6d896d28af928af)](https://www.codefactor.io/repository/github/voytech/tabulate)
[![Java CI with Gradle](https://github.com/voytech/tabulate/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/voytech/tabulate/actions/workflows/gradle.yml)

Tabulate helps you with exporting collections of objects into various tabular file formats.    

## Why ?

Exporting data to external file formats can be tedious and cumbersome - especially when business wants to have reports covering vast majority of system functionalities. Writing every exporting method using imperative API directly will soon make code verbose, error prone, hard to read and maintain. In such cases You want to hide implementation details using abstractions, but this is additional effort which is not desirable. 

Tabulate tries to mitigate this little inconvenience by using powers of Kotlin DSL type-safe builders as consumer API.

### You may want to consider using `tabulate` if:    

- You need to export objects into: 
    - excel (xlsx),
    - pdf (WIP - 0.2.0),
    - cli (only plans)
    
- Exported data needs to be table-formatted. 

- You want to reuse table structure for many tabular file formats (excel, pdf)
  
- You need to preserve styles across different file formats.  

- You want to be able to implement your own attributes.

- You want to use extensible and type-safe DSL builder API.

## Snapshot repository coordinates

Library is not yet released into maven central.
Currently you can access its snapshot:

```
repositories {
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

dependencies {
    implementation(platform("io.github.voytech:tabulate-bom:0.1.0-SNAPSHOT"))
    implementation("io.github.voytech","tabulate-core")   // e.g. DSL,  
    implementation("io.github.voytech","tabulate-excel")  // streaming apache POI excel API.
}

```

First 0.1.0 version will not differ much from current snapshot version, and migration to release should be quick.

## Basic usage
Firstly, let us define list of exemplary addresses:
```kotlin
    val addressList = listOf(
        Address("Jesse", "Pinkman","New Mexico", "Cold street 200","87045"),
        Address("Walter", "White","New Mexico", "308 Negra Arroyo Lane","87045"),
        Address("Cynthia", "Andrew","New Mexico", "12000 – 12100 Coors Rd SW","87045"),
    )
```
Now define simple table structure (via property to column bindings):
```kotlin
    addressList.tabulate("address_list.xlsx") {
          name = "Address list"
          columns { 
              column(Address::firstName)
              column(Address::lastName)
              column(Address::street)
              column(Address::postCode)
          }
          attributes {
              width { auto = true }
          }  
    }
```
That is all! This will create an Excel with sheet named "Address list". Sheet will contain 3 entries with automatically adjusted column widths but without any other additional features (like header row) 

Want to see header row ? 

```kotlin
    addressList.tabulate("address_list.xlsx") {
          name = "Address list"
          attributes {
            width { auto = true }
          }
          columns { 
              column(Address::firstName)
              column(Address::lastName)
              column(Address::street)
              column(Address::postCode)
          }
          rows {
              header("First Name", "Last Name", "Street", "Post Code")
          }  
    }
```
Header row cells should have white font on black background ? 

```kotlin
    addressList.tabulate("address_list.xlsx") {
          name = "Address list"
          attributes {
            width { auto = true }
          }
          columns { 
              column(Address::firstName)
              column(Address::lastName)
              column(Address::street)
              column(Address::postCode)
          }
          rows {
              header { 
                columnTitles("First Name", "Last Name", "Street", "Post Code")
                attributes { 
                    text { fontColor = Colors.WHITE }
                    bacground { color = Colors.BLACK}
                }
              }
          }  
    }
```
If You do not want repeatation... 

```kotlin
object TableDefinitions {
  val addressTable = table {
    name = "Address list"
    attributes {
      width { auto = true }
    }
    columns {
      column(Address::firstName)
      column(Address::lastName)
      column(Address::street)
      column(Address::postCode)
    }
    rows {
      header {
        columnTitles("First Name", "Last Name", "Street", "Post Code")
        attributes {
          text { fontColor = Colors.WHITE }
          bacground { color = Colors.BLACK }
        }
      }
    }
  }
}
```
And now: 
```kotlin
addressList.tabulate("address_list.xlsx", TableDefinitions.addressTable.copy())
```
and soon: 
```kotlin
addressList.tabulate("address_list.pdf", TableDefinitions.addressTable.copy())
addressList.tabulate("address_list.txt", TableDefinitions.addressTable.copy()) // CLI ASCII table
```
Let as change sheet name from template table definition:
```kotlin
addressList.tabulate("address_list.xlsx",TableDefinitions.addressTable.copy { name = "Addresses" })
```

I think above requires no word of explanation.

## Key concepts

### Table model with attributes.

Table model describes how table will look like after data exporting. Its building blocks are:  

- `column` - easy to guess - defines a single column in target table,
- `row`  - may be user defined custom row or row that carries attributes for enriching existing record, 
- `row cell` - defines cell within row. Cell is bound to a column via column id.
- `attribute` - introduces extensions to basic presentation capabilities. 

Simple table model example: 

```kotlin
productList.tabulate("file.xlsx") {
    name = "Table id"
    columns {
        column("nr") {
            ..
        }
        ..
    }
    rows {
        row {  // first row when no index provided.           
            cell("nr") { value = "Nr.:" }
            ..           
        }
        ..
    }
}
```
Above is sufficient if You only want to layout cell values within a table. 
In order to gain more powers You will need to start using `attributes`.

`Attributes` are plain objects with inner properties that extends base model. Attributes can be mounted on multiple levels: _table_, _column_, _row_ and single _cell_ levels. 

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
```

### `TabulationTemplate` and table export operations.

`TabulationTemplate` is an entry-point facility that orchestrates all table export operations: 
- It instantiates result providers and 3rd party underlying APIs, (result providers allows to implement different result transmission strategies per output class),
- It requests data records one by one and delegates context rendering to table operations implementors,
- It finalizes exporting operations (e.g. flushing output stream),
- It resolves export operations implementors.


### Table DSL builders API.

Kotlin type-safe DSL builders API helps a lot with describing table structure.
It makes source code to look more concise and readable and makes maintenance tasks much easier due to DSL type-safety. At coding time, your IDE will make use of it by offering completion hints - this elevates developer experience, as almost zero documentation is required to start. 

DSL functions take lambda receivers as arguments which abstracts away API instantiation details from consumers. Within lambda, you can call API methods which can take downstream builders as arguments. We can end up having multi-level DSL API structure, where each level is extensible via Kotlin extension functions. On each DSL level You are allowed to invoke scope accessible methods and access variables which can lead to interesting results:
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
                      cell { value = it.code }
                      cell { value = it.name }
                      cell { value = it.description }
                      cell { value = it.manufacturer }
                  }
              }
          }  
    }.export("products.xlsx")
```
`additionalProducts` collection instance can be easily iterated over with lambda, adding new row definition per collection item. It is a mix of DSL type-safe builders with scope accessible references.

### Table DSL extension functions.

As already said, it is possible to extend each DSL level by using extension functions on DSL API builder classes.  

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
        names.forEach {
            cell { value = it }
        }
    }
```
This way, you can create various shortcuts and templates, making DSL vocabulary richer and more expressive. 
It is worth mentioning that by using extension functions on DSL builders - scope becomes restricted by DslMarker annotation,
so it is not possible to break table definition by calling methods from upstream builders.

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

### Support for project-reactor.

Assume we are developing reactive web application and we have already created reactive Spring JPA repository like: 
```kotlin
fun Flux<Product> loadProducts() { ... }
```
You can now call extension method:
```kotlin
productsRepository.loadProducts().tabulate("file/path/products.xlsx") { .. configure table look and feel ...}
```
On each new signal emission from upstream publisher, next row will be rendered.
After Flux completion, rendering context will flush the stream (this part is still blocking API) 

To enable project-reactor extensions you will need to add following to your gradle file: 
```groovy
dependencies {
    implementation(platform("io.github.voytech:tabulate-bom:$tabulateVersion"))
    implementation("io.github.voytech","tabulate-reactor")
}

```


## Other features (v0.1.0)


### Java interop - fluent builders Java API.
Old-fashioned Java fluent builder API is also supported. It is needless to say it looks much less attractive:

```java
Table<Employee> employeeTable =
		Table.<Employee>builder()
		.attribute(TemplateFileAttribute::builder, builder -> builder.setFileName("file.xlsx"))
		.columns()
		    .column(Employee::getId)
		        .columnType(CellType.NUMERIC)
		        .attribute(ColumnWidthAttribute::builder)
		    .column(Employee::getFirstName)
		        .columnType(CellType.STRING)
		        .attribute(ColumnWidthAttribute::builder)
		    .column(Employee::getLastName)
		        .columnType(CellType.STRING)
		        .attribute(ColumnWidthAttribute::builder)
		.rows()
		    .row()
		        .attribute(RowHeightAttribute::builder, builder -> builder.setPx(100))
		.build();
```

### Custom rows.

Sometimes, in addition to records from external source - You need to add user defined rows.
Table usually contains a header row or summary footer row.
It is also possible to define interleaving custom rows at specified index or rows that match specific predicate.

Row model allows to define custom cell values as well as cell styles and attributes only.
It acts as conveyor for additional features for existing external source derived rows, or as a factory for standalone custom rows that can be hooked at definition time.

Things You can achieve with Row model in terms of custom rows includes:

- setting custom cell styles,
- setting row-level attributes (e.g. row height)
- defining row and col spans,
- inserting images,
- setting cell values of different types.


### Merging rows.  

When multiple `Row` model definitions are qualified by a predicate, they form a single synthetic row. Following rules regarding row merge applies:
- Row level attributes will be concatenated or merged if are of same type.
- Cell values will be concatenated, or overriden by last cell occurence at given column.
- Cell level attributes will be concatenated, or merged if of same type.
- Two attributes of same type are merged by overriding clashing attribute properties from left to right where on left side stands attribute from higher level (e.g. row level), and on right site stands attribute from lower level (e.g. cell level).

### Library of attributes.

You may need attributes for various reasons - for styling, for formatting or other custom hooks. 

Currently, with `tabulate-core` and `tabulate-excel` modules, you will get following attributes included:

#### Table attributes
- `FilterAndSortAttribute` - enables filtering and sorting of excel table,
- `TemplateFileAttribute` - allows performing template file interpolation with source data collection of items, 

#### Column attributes
- `ColumnWidthAttribute` - sets the width of column (meaning all cells gathered under particular column will have same width), 

#### Row attribtues 
- `RowHeightAttribute` - sets the height of row (meaning all cells gathered within particular row will have same height),

#### Cell attribtues 
- `CellTextStylesAttribute` - allows controlling general, text related style attributes,
- `CellBordersAttribute` - sets borders on selected cells,
- `CellBackgroundAttribute` - sets background color and fill, 
- `CellAlignmentAttribute` - sets text vertical and horizontal alignment 

Typical usage scenario for attributes:
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

I have put lots of effort to make **Tabulate** extensible. Currently, it is possible to:
- add user defined attributes,
- add custom renderers for already defined attribute,
- implement table export operations from scratch (e.g. html table, cli table, mock renderer for testing),
- extend DSL type-safe builder APIS on all possible level.

### Implementing new table export operations.
In order to support new tabular file format you have to extend `ExportOperationsConfiguringFactory<C, T, O>` where:
- `C` stands for rendering context - which is usually wrapper around 3rd party api like Apache POI,
- `T` stands for object class representing entry in exported collection,
- `O` stands for type of result of operation (e.g. `OutputStream` for Apache POI)

As long as tabulate uses java ServiceLoader infrastructure, You need to create file `resource/META-INF/io.github.voytech.tabulate.template.spi.ExportOperationsProvider`, and put fully qualified class name of your custom factory in the first line. **This step is required by a template in order to resolve your extension at run-time**. 

Basic CSV implementation can look like this:

```kotlin
class PrintStreamRenderingContext(): OutputStreamResultProvider(), RenderingContext {
  lateinit var printer : PrintStream
  override fun onOutput(output: OutputStream) {
    printer = PrintStream(output)
  }
  override fun flush(output: OutputStream) {
    printer.close()
  }
}

class ExampleCsvExportOperationsConfiguringFactory<T> : ExportOperationsConfiguringFactory<T, PrintStreamRenderingContext>() {
  override fun createExportOperations(renderingContext: PrintStreamRenderingContext): TableExportOperations<T> = object : TableExportOperations<T> {

    override fun beginRow(context: AttributedRow<T>) {
      renderingContext.printer.print(context.rowCellValues.entries.map { it.value.value }.joinToString { ","})
    }

    override fun endRow(context: AttributedRow<T>) {
      renderingContext.printer.print("\n")
    }

    override fun renderRowCell(context: AttributedCell) {
      println("NOOP")
    }
  }

  override fun createResultProviders(renderingContext: PrintStreamRenderingContext): List<ResultProvider<*>> = listOf(renderingContext)

  override fun createRenderingContext(): PrintStreamRenderingContext = PrintStreamRenderingContext()

  override fun supportsFormat(): TabulationFormat = TabulationFormat.format("csv")

}
```

When output tabular format supports styles - You could then add support for rendering built-in attributes as follow: 

```kotlin
class ExampleExportOperationsConfiguringFactory<T> : ExportOperationsConfiguringFactory<T, SomeRenderingContext>() {
    
  ..
  override fun getAttributeOperationsFactory(renderingContext: SomeRenderingContext): AttributeRenderOperationsFactory<T> =
      StandardAttributeRenderOperationsFactory(renderingContext, object: StandardAttributeRenderOperationsProvider<ApachePoiExcelFacade,T>{
          override fun createTemplateFileRenderer(renderingContext: ApachePoiExcelFacade): TableAttributeRenderOperation<TemplateFileAttribute> =
            TemplateFileAttributeRenderOperation(renderingContext)
    
          override fun createColumnWidthRenderer(renderingContext: ApachePoiExcelFacade): ColumnAttributeRenderOperation<ColumnWidthAttribute> =
            ColumnWidthAttributeRenderOperation(renderingContext)
    
          override fun createRowHeightRenderer(renderingContext: ApachePoiExcelFacade): RowAttributeRenderOperation<T, RowHeightAttribute> =
            RowHeightAttributeRenderOperation(renderingContext)
    
          override fun createCellTextStyleRenderer(renderingContext: ApachePoiExcelFacade): CellAttributeRenderOperation<CellTextStylesAttribute> =
            CellTextStylesAttributeRenderOperation(renderingContext)
    
          override fun createCellBordersRenderer(renderingContext: ApachePoiExcelFacade): CellAttributeRenderOperation<CellBordersAttribute> =
            CellBordersAttributeRenderOperation(renderingContext)
    
          override fun createCellAlignmentRenderer(renderingContext: ApachePoiExcelFacade): CellAttributeRenderOperation<CellAlignmentAttribute> =
            CellAlignmentAttributeRenderOperation(renderingContext)
    
          override fun createCellBackgroundRenderer(renderingContext: ApachePoiExcelFacade): CellAttributeRenderOperation<CellBackgroundAttribute> =
            CellBackgroundAttributeRenderOperation(renderingContext)
      })
}
```
Factory class `StandardAttributeRenderOperationsFactory` exposes API which assumes specific standard library attributes. 
If your file format allow additional attributes which are not present in standard library (tabulate-core), you may use `AttributeRenderOperationsFactory` interface directly, or fill additional constructor properties on `StandardAttributeRenderOperationsFactory` as below:

```kotlin
class ExampleExportOperationsConfiguringFactory<T> : ExportOperationsConfiguringFactory<T,SomeRenderingContext>() {
    
  ..
  override fun getAttributeOperationsFactory(renderingContext: SomeRenderingContext): AttributeRenderOperationsFactory<T> =
      StandardAttributeRenderOperationsFactory(renderingContext, object: StandardAttributeRenderOperationsProvider<SomeRenderingContext,T>{
          override fun createTemplateFileRenderer(renderingContext: SomeRenderingContext): TableAttributeRenderOperation<TemplateFileAttribute> =
            TemplateFileAttributeRenderOperation(renderingContext)
              ..
      },
        additionalCellAttributeRenderers = setOf( .. )
        additionalTableAttributeRenderers = setOf( .. )
        ..
      )
}
```

### Registering new attribute types for specific export operations implementation
It is possible that you have requirements which are not provided out of the box, and your code is in different compilation unit than specific table export operation implementation. Assume You want to use existing Apache POI excel table exporter, but there is lack of certain attribute support. In such situation - You can still register attribute by implementing another service provider interface - `AttributeRenderOperationsProvider`: 

```kotlin
class CustomAttributeRendersOperationsProvider<T> : AttributeRenderOperationsProvider<T,ApachePoiExcelFacade> {

    override fun getContextClass() = ApachePoiExcelFacade::class.java

    override fun getAttributeOperationsFactory(creationContext: ApachePoiExcelFacade): AttributeRenderOperationsFactory<T> {
        return object : AttributeRenderOperationsFactory<T> {
            override fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<out CellAttributeAlias>> =
                setOf(MarkerCellAttributeRenderOperation(creationContext))
        }
    }
}

```
After creating factory - You need to implement particular attribute together with DSL API extension function and attribute render operation to instruct 3rd party Apache Poi API on how to proceed. 

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
    attribute(MarkerCellAttribute.Builder().apply(block))
```
Finally, You need to create file `resource/META-INF/io.github.voytech.tabulate.template.spi.AttributeRenderOperationsProvider`, and put fully qualified class name of our factory in it.

### Extending Table DSL API

In the last section You saw how to define custom user attributes. The last step involves creating extension function on specific DSL attribute API. As DSL builder class name suggests (`CellLevelAttributesBuilderApi<T>`) this builder is part of a Cell DSL API only , which means that it won't be possible to add this attribute on row, column and table. You can leverage this behaviour for restricting say 'mounting points' of specific attributes. In order to enable cell attribute on all levels You will need to add more extension functions: 

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
            ..
        }
    }
    rows {
        row {
           attributes { label { text = "ROW" } }
           cell("nr") { 
              value = "Nr.:" 
              attributes {
                attributes { label { text = "CELL" } }
              }  
           }
            ..
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

- CLI table

### v0.4.x

- Composition of multiple table models (TableBuilder.include).

### v0.5.x

- Multi-part output files. (chunking large files)

### v0.6.x

- Codegen for user defined attributes.


### TBD ...


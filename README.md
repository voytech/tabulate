
# Tabulate

[![CodeFactor](https://www.codefactor.io/repository/github/voytech/tabulate/badge?s=356351985a7dd58359040b23f6d896d28af928af)](https://www.codefactor.io/repository/github/voytech/tabulate)
[![Java CI with Gradle](https://github.com/voytech/tabulate/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/voytech/tabulate/actions/workflows/gradle.yml)

Tabulate helps you with exporting collections of elements into various tabular file formats.    

## Why ?

Exporting data to external file formats can be tedious and cumbersome - especially when business wants to have reports covering vast majority of system functionalities. Writing every exporting method using imperative API directly will soon make code verbose, error prone, hard to read and maintain. In such cases You want to hide implementation details using abstractions, but this is additional effort which is not desirable. 

`Tabulate` tries to mitigate above problems with the help of `Kotlin`, its `type-safe DSL builders` and `extension functions`.

### You may want to consider using `tabulate` if:    

- You need to export data into following formats: 
    - excel (xlsx),
    - pdf (WIP),
    - cli (WIP),
    
- Exported data needs to be in tabular format, 

- You want to reuse table definition for many exports targeting multiple file formats,
  
- You need to preserve look&feel across different file formats,  

- You want to customise table look&feel by your own-crafted attributes.

## Snapshot repository coordinates

Library is not yet released into maven central.
Currently, you can access its snapshot:

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
    implementation("io.github.voytech","tabulate-reactor")  // Support for tabulation on Flux
}

```

First 0.1.0 version will not differ much from current snapshot version and migration to release should be quick.

## Basic usage
Let's start simple. Given list of addresses as below:
```kotlin
    val listForGus = listOf(
        Address("Jesse", "Pinkman","New Mexico", "9809 Margo Street","87104"),
        Address("Walter", "White","New Mexico", "308 Negra Arroyo Lane","87045"),
        Address("Saul", "Goodman","New Mexico", "9800 Montgomery Blvd NE","87111"),
        Address("Mike", "Ehrmantraut","New Mexico", "204 Edith Blvd. NE","87102"),
  )
```
Invoke `tabulate` extension function with basic table definition passed as a function argument:
```kotlin
    listForGus.tabulate("address_list.xlsx") {
          name = "Associates"
          columns { 
              column(Address::firstName)
              column(Address::lastName)
              column(Address::street)
              column(Address::postCode)
          }
          attributes {
              columnWidth { auto = true }
          }  
    }
```
That's it. 
This will create an Excel file with sheet named "Associates". Sheet will contain 4 entries with automatically adjusted column widths but without any additional features (like header row) 

Let's add header now: 

```kotlin
    addressList.tabulate("address_list.xlsx") {
          name = "Associates"
          attributes {
            columnWidth { auto = true }
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
We can make header look a bit differently:  

```kotlin
    addressList.tabulate("address_list.xlsx") {
          name = "Associates"
          attributes {
            columnWidth { auto = true }
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
                    background { 
                      color = Colors.BLACK
                      fill = DefaultCellFill.SOLID
                    }
                }
              }
          }  
    }
```
Until now, we were passing table definition directly to `tabulate` method. In real life scenario we may want to keep table definition separated as below:

```kotlin
object TableDefinitions {
  val addressTable = Table<Address> {
    name = "Traitors address list"
    attributes {
      columnWidth { auto = true }
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
          background { 
            color = Colors.BLACK
            fill = DefaultCellFill.SOLID
          }
        }
      }
    }
  }
}
```
And now: 
```kotlin
addressList.tabulate("address_list.xlsx", TableDefinitions.addressTable)
```
and soon: 
```kotlin
addressList.tabulate("address_list.pdf", TableDefinitions.addressTable) // PDFbox implementation
addressList.tabulate("address_list.txt", TableDefinitions.addressTable) // CLI ASCII table - raw implementation
```
Keeping table definition as a separate object is a first step into templating. It is best seen on example below:
```kotlin
addressList.tabulate("address_list.xlsx",TableDefinitions.addressTable + { name = "Dealers Addresses" })
```
Above syntax is very intuitive and shows some powers of Kotlin. We have used overridden `+` operator in order to merge two table definitions. Merging evaluates in the same way as normal method's arguments. Logic behind this feature is very simple - `+` operator takes two lambdas with receiver, then it returns another lambda with receiver which internally delegates invocations to original lambdas one by one. Effectively it is nothing more than receiver configuration / re-configuration (invocation of subsequent builders on the same receiver one by one). This is simple solution, yet imposes few restrictions on how to manage underlying builder state. (Explanation is out of the scope of this README file. I will try to cover this subject in more details in documentation)

Far more real-life templating example:
```kotlin
object TableDefinitions {
  val appBasicTemplate = CustomTable {
    name = "Basic template"
    rows {
      atIndex { header() } newRow {
        attributes {
          background {
            color = Colors.BLACK
          }
          text {
            fontColor = Colors.WHITE
          }
        }
      }
    }
  }
}

addressList.tabulate("address_list.xlsx",appBasicTemplate + {
  columns {
    column(Address::firstName)
    column(Address::lastName)
    column(Address::street)
    column(Address::postCode)
  }
  rows {
    header {
      columnTitles("First Name", "Last Name", "Street", "Post Code")
    }
  }
})
 
```


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



# Tabulate - Kotlin Table Export

[![CodeFactor](https://www.codefactor.io/repository/github/voytech/kotlin-data-export/badge/master?s=44ac65ac3503bca6a7cd3cb3b869fa080a6db3d2)](https://www.codefactor.io/repository/github/voytech/kotlin-data-export/overview/master)
![Java CI with Gradle](https://github.com/voytech/kotlin-data-export/workflows/Java%20CI%20with%20Gradle/badge.svg)

An utility written in Kotlin/JVM for simplifying data export to tabular file formats.

# Why ?

Because Kotlin DSL makes it desirable.

# When ?    

If You need to dump collection of objects into following formats: 
- excel (xlsx), 
- csv,
- pdf,
- ... (here goes all future implementations)

and want to:
 - have control over table styles ( on table, column, row and cell level), 
 - add custom, leading, trailing or even interleaving rows (e.g. column headers, footer with summaries, or divider-like data rows ),
 - use compact, simple, type-safe DSL for that.  

# How ?

By using: 
- simple table domain model,
- plugable attributes (for styles, format-specific features),
- type-safe DSL builders,
- built-in renderers implementations (excel, pdf, csv) delegating to 3rd party libraries.


# License 

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


# Tabulate - Data into tabular files using convenient DSL (in Kotlin)

[![CodeFactor](https://www.codefactor.io/repository/github/voytech/tabulate/badge?s=356351985a7dd58359040b23f6d896d28af928af)](https://www.codefactor.io/repository/github/voytech/tabulate)
[![Java CI with Gradle](https://github.com/voytech/tabulate/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/voytech/tabulate/actions/workflows/gradle.yml)

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

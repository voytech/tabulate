
# Data Exporter

[![CodeFactor](https://www.codefactor.io/repository/github/voytech/kotlin-data-export/badge/master?s=44ac65ac3503bca6a7cd3cb3b869fa080a6db3d2)](https://www.codefactor.io/repository/github/voytech/kotlin-data-export/overview/master)
![Java CI with Gradle](https://github.com/voytech/kotlin-data-export/workflows/Java%20CI%20with%20Gradle/badge.svg)

A simple and tiny library written in Kotlin/JVM meant to simplify dumping plain object collections to various tabular data file formats.

# Why ?
Some time ago, being on a regular client project, I was asked to do exporting of collections into files. 
That was not just a single one-off task as our road map was full of similar requirements. 
It was clear, that we need to incorporate a common approach which will make entire solution 
cleaner and boost our development allowing us to focus on hardest stuff. For that purpose we've decided to create separate 
library hosted in separate repository and managed as foreign dependency. 
We have written all in Java using AOP and annotated repository get methods. Primarily it was doing job very well, but after 
months of development it started to give up :) and we have to get into iterative refactoring which was taking all business 
past and current wishes into account.

Several weeks ago I wanted to start new side project on my own. I have couple of ideas but could not find rationale for them. 
Fortunately one day I've recalled that small library I've created. I started to think that making this simple stuff again from scratch, 
using different approaches and toolset, might be useful, fun and experiencing. In fact everything here is different, there is no 
single line of code which looks the same. No copy paste. Even the way I'm using abstractions here differs a lot.   

I hope that this is not side project for fun and play and someone will find this tiny export automation lib matching his needs.     

If your task is to export collection of objects into tabular data format like: 
- xls, xlsx, 
- csv (heading or without),
- pdf table,
- static html table
- ... table (here goes all future contributions)

and You want to:
 - have control over table styles where possible (in CSV You wont), 
 - be able to create predefined interleaving rows (like column headers or footer with summaries, or even rows inserted in the middle of collection),
 - use compact, simple, type-safe domain specific language for that
 - and start exporting from collection via extension method.   

Then You may want to stay here a 2-4 minutes longer and see how this library can help You with that. 

No guarantee, but maybe it just solves your problem.    

# How ?

By using: 
- simple table domain model,
- plugable model extensions (for styles, format-specific features),
- type-safe DSL builders,
- extension methods on collection,
- template (export template) pattern,
- levels of abstraction for implementing various exporters,
- built-in exporters implementations (excel, pdf, html, csv) delegating to 3rd party well known libraries.

 
# Extension points. Levels of abstraction.

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

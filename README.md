
# Data Exporter
A simple and tiny library written in Kotlin/JVM meant to simplify dumping plain object collections to various tabular data file formats.

# Why ?
Some time ago, being on a regular client project, I was asked to do exporting collections of 
objects into several data formats. That was not just a single shot as our road map was full of similar requirements. 
It was clear, that we need to incorporate a common approach which will make entire solution 
cleaner and boost our development allowing us to focus on hard stuff. We decided to create shared library for that purpose. 
We have written it in Java using AOP with service methods annotations. Primarily it was doing job very well, but after 
months of development it started to give up :) and we have to get into iterative refactoring which was taking all business 
past and current wishes into account. We did not throw it away, we decided to support it.

Several weeks ago I wanted to start new side project on my own. I have couple of ideas but could not find rationale for them. 
And then I recalled that small library I've created. Wouldn't be good to create simple data export automation library 
and by the way taking into account all experience I gained in that area?   
Maybe even someone decides to use it somewhere :)         

So if your task is to export collection of objects into tabular data format like: 
- xls, xlsx, 
- plain csv,
- pdf table,
- static html table
- ... table (here goes all future contributions)

and You want to:
 - have control over table styles where possible, 
 - be able to create predefined interleaved rows (like column headers or footer with summaries),
 - use compact, simple, type-safe domain specific language for that   

You may want to stay here a 2-4 minutes longer and see how this library can help You with that. 
No guarantee, but maybe it just solves your problem.    

#How ?

By using: 
- simple table domain model,
- plugable model extensions (for styles, format-specific features),
- type-safe DSL builders,
- extension methods on collection,
- template (export template) pattern,
- levels of abstraction for implementing various exporters,
- built-in exporters implementations (excel, pdf, html, csv) delegating to 3rd party well known libraries.

 
#Extension points. Levels of abstraction.

#License 
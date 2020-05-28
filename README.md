
# kotlin-data-exporter
A simple library for simplifying things we are often doing the same way.

Many times, on many regular projects we are being asked to implement trivial things to see data
in various data formats. In many cases these are just a recurring problems we should solve almost in the same way.  
Suppose we have an object from domain model. We are using data repositories for querying collections of those objects to 
present them on page in form of table. Now business may want to additionally download a file with that table. 

It is often the case that business people got used to various file formats which makes them feel more comfortable. 
They like to see reports not only on the page, but they want to grab them, save them in EXCEL, CSV, PDF. 
They will use them in emails, on meetings and so on. 
It is like more canonical presentation model for other folks on the same level. 

What I am trying to sell with this small lib, is tiny automation for some sort of problems similar to above. 

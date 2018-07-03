# OData-example
Example application illustrating how to communicate with Mikromarc 3 using the OData-based MMWebApi.

## About OData
[OData](http://www.odata.org/) is an attempt add "best practices" for
[REST](https://en.wikipedia.org/wiki/Representational_state_transfer)-like web-services.
It mainly standardizes resource access and manipulation, filtering and search.
Adding to that it introduces a convention for [RPC](https://en.wikipedia.org/wiki/Remote_procedure_call)-like calls (called "actions").

The official [basic tutorial](http://www.odata.org/getting-started/basic-tutorial/) is a good introduction to most core concepts.

## About Apache Olingo
[Apache Olingo](https://olingo.apache.org/) is the Apache Foundations implementation of the OData standard.
It seems stable and has all the functionality, it's also actively maintained (unlike [odata4j](http://odata4j.org/)).

The [official documentation](https://olingo.apache.org/doc/odata4/index.html) can be a bit hard to digest.
Fortunately enough, these two blog posts exist and are a big help for those just getting started.

 - [Accessing data of OData v4 services with Olingo](https://templth.wordpress.com/2014/12/03/accessing-odata-v4-service-with-olingo/)
 - [Manipulating data of OData v4 services with Olingo](https://templth.wordpress.com/2014/12/05/manipulating-data-of-odata-v4-services-with-olingo/)

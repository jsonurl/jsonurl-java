# About JSON->URL
RFC8259 describes the JSON data model and interchange format, which is widely
used in application-level protocols including RESTful APIs. It is common for
applications to request resources via the HTTP POST method, with JSON entities,
however, POST is suboptimal for requests which do not modify a resource's
state. JSON->URL defines a text format for the JSON data model suitable for use
within a URL/URI (as described by RFC3986).

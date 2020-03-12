# JSON->URL
[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/jsonurl/jsonurl-java.svg?label=License)](https://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://travis-ci.com/jsonurl/jsonurl-java.svg?branch=master)](https://travis-ci.com/jsonurl/jsonurl-java)
[![Known Vulnerabilities](https://snyk.io/test/github/jsonurl/jsonurl-java/badge.svg?targetFile=module/jsonurl-jsonorg/pom.xml)](https://snyk.io/test/github/jsonurl/jsonurl-java?targetFile=module/jsonurl-jsonorg/pom.xml)
[![Known Vulnerabilities](https://snyk.io/test/github/jsonurl/jsonurl-java/badge.svg?targetFile=module/jsonurl-jsr374/pom.xml)](https://snyk.io/test/github/jsonurl/jsonurl-java?targetFile=module/jsonurl-jsr374/pom.xml)

## About
RFC8259 describes the JSON data model and interchange format, which is widely
used in application-level protocols including RESTful APIs. It is common for
applications to request resources via the HTTP POST method, with JSON entities,
however, POST is suboptimal for requests which do not modify a resource's
state. JSON->URL defines a text format for the JSON data model suitable for use
within a URL/URI (as described by RFC3986).

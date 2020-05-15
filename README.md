# JSON->URL
[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/jsonurl/jsonurl-java.svg?label=License)](https://www.apache.org/licenses/LICENSE-2.0)
[![Release](https://img.shields.io/github/release/jsonurl/jsonurl-java.svg?label=Release)](https://search.maven.org/search?q=g:org.jsonurl)
[![javadoc](https://javadoc.io/badge2/org.jsonurl/jsonurl-core/javadoc.svg)](https://javadoc.io/doc/org.jsonurl/jsonurl-core)
[![TravisCI Build Status](https://travis-ci.com/jsonurl/jsonurl-java.svg?branch=master)](https://travis-ci.com/jsonurl/jsonurl-java)
[![GitHub Build Status](https://github.com/jsonurl/jsonurl-java/workflows/ci/badge.svg)](https://github.com/jsonurl/jsonurl-java/actions?query=workflow%3Aci)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=jsonurl_jsonurl-java&metric=alert_status)](https://sonarcloud.io/dashboard?id=jsonurl_jsonurl-java)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=jsonurl_jsonurl-java&metric=coverage)](https://sonarcloud.io/dashboard?id=jsonurl_jsonurl-java)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=jsonurl_jsonurl-java&metric=ncloc)](https://sonarcloud.io/dashboard?id=jsonurl_jsonurl-java)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=jsonurl_jsonurl-java&metric=security_rating)](https://sonarcloud.io/dashboard?id=jsonurl_jsonurl-java)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=jsonurl_jsonurl-java&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=jsonurl_jsonurl-java)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=jsonurl_jsonurl-java&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=jsonurl_jsonurl-java)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fjsonurl%2Fjsonurl-java.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Fjsonurl%2Fjsonurl-java?ref=badge_shield)

org.json module: [![Known Vulnerabilities](https://snyk.io/test/github/jsonurl/jsonurl-java/badge.svg?targetFile=module/jsonurl-jsonorg/pom.xml)](https://snyk.io/test/github/jsonurl/jsonurl-java?targetFile=module/jsonurl-jsonorg/pom.xml)
[![javadoc](https://javadoc.io/badge2/org.jsonurl/jsonurl-jsonorg/javadoc.svg)](https://javadoc.io/doc/org.jsonurl/jsonurl-jsonorg)

javax.json module: [![Known Vulnerabilities](https://snyk.io/test/github/jsonurl/jsonurl-java/badge.svg?targetFile=module/jsonurl-jsr374/pom.xml)](https://snyk.io/test/github/jsonurl/jsonurl-java?targetFile=module/jsonurl-jsr374/pom.xml)
[![javadoc](https://javadoc.io/badge2/org.jsonurl/jsonurl-jsr374/javadoc.svg)](https://javadoc.io/doc/org.jsonurl/jsonurl-jsr374) 

## About
RFC8259 describes the JSON data model and interchange format, which is widely
used in application-level protocols including RESTful APIs. It is common for
applications to request resources via the HTTP POST method, with JSON entities,
however, POST is suboptimal for requests which do not modify a resource's
state. JSON->URL defines a text format for the JSON data model suitable for use
within a URL/URI (as described by RFC3986).

## Usage
The core library defines a [generic][java-generic] [JSON->URL parser][parser]
and includes an implementation based Java SE data types (e.g.
[java.util.Map][java-map], [java.util.List][java-list], etc).
There are two additional modules, distributed as separate artifacts, which
implement a parser based on JSR-374 and Douglas Crockford's Java API.

Java SE API example:

```java
import org.jsonurl.JavaValueParser;

JavaValueParser p = new JavaValueParser();
Map obj = p.parseObject( "(Hello:World!)" );
System.out.println(obj.get("Hello")) // World!
```

[Json.org][jsonorg-parser] example:

```java
import org.jsonurl.jsonorg.JsonUrlParser;

JsonUrlParser p = new JsonUrlParser();
JSONObject obj = p.parseObject( "(Hello:World!)" );
System.out.println(obj.get("Hello")) // World!
```

[JSR-374][jsr374-parser] example:

```java
import org.jsonurl.jsonp.JsonUrlParser;

JsonUrlParser p = new JsonUrlParser();
JsonObject obj = p.parseObject( "(Hello:World!)" );
System.out.println(obj.get("Hello")) // World!
```

The parser is designed for parsing untrusted input. It supports limits on
the number of values it will instantiate and depth of nested arrays or objects
that may be parsed before throwing a [LimitException][limit-exception], with
sane defaults.

## License
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fjsonurl%2Fjsonurl-java.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2Fjsonurl%2Fjsonurl-java?ref=badge_large)

[java-generic]: https://docs.oracle.com/javase/tutorial/java/generics/types.html
[parser]: module/jsonurl-core/src/main/java/org/jsonurl/Parser.java
[limit-exception]: module/jsonurl-core/src/main/java/org/jsonurl/LimitException.java
[jsonorg-parser]: module/jsonurl-jsonorg/src/main/java/org/jsonurl/jsonorg/JsonUrlParser.java
[jsr374-parser]: module/jsonurl-jsr374/src/main/java/org/jsonurl/jsonp/JsonUrlParser.java
[java-map]: https://docs.oracle.com/javase/8/docs/api/java/util/Map.html
[java-list]: https://docs.oracle.com/javase/8/docs/api/java/util/List.html


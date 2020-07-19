# JSON&#x2192;URL
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
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-v2.0%20adopted-ff69b4.svg)](CODE_OF_CONDUCT.md)
[![project chat](https://img.shields.io/badge/zulip-join_chat-brightgreen.svg)](https://jsonurl.zulipchat.com/)

[json.org module][jsonorg-module]: [![Known Vulnerabilities](https://snyk.io/test/github/jsonurl/jsonurl-java/badge.svg?targetFile=module/jsonurl-jsonorg/pom.xml)](https://snyk.io/test/github/jsonurl/jsonurl-java?targetFile=module/jsonurl-jsonorg/pom.xml)
[![javadoc](https://javadoc.io/badge2/org.jsonurl/jsonurl-jsonorg/javadoc.svg)](https://javadoc.io/doc/org.jsonurl/jsonurl-jsonorg)

[javax.json module][jsr374-module]: [![Known Vulnerabilities](https://snyk.io/test/github/jsonurl/jsonurl-java/badge.svg?targetFile=module/jsonurl-jsr374/pom.xml)](https://snyk.io/test/github/jsonurl/jsonurl-java?targetFile=module/jsonurl-jsr374/pom.xml)
[![javadoc](https://javadoc.io/badge2/org.jsonurl/jsonurl-jsr374/javadoc.svg)](https://javadoc.io/doc/org.jsonurl/jsonurl-jsr374) 

## About
[RFC8259][RFC8259] describes the JSON data model and interchange format, which is widely
used in application-level protocols including RESTful APIs. It is common for
applications to request resources via the HTTP POST method, with JSON entities.
However, POST is suboptimal for requests which do not modify a resource's
state. JSON&#x2192;URL defines a text format for the JSON data model suitable
for use within a [URL][RFC1738]/[URI][RFC3986].

## Usage
The core library defines a [generic][java-generic]
[JSON->URL parser][parser] and includes an implementation based Java SE
data types (e.g. [java.util.Map][java-map], [java.util.List][java-list], etc).
There are two additional modules, distributed as separate artifacts, which
implement a parser based on JSR-374 and Douglas Crockford's Java API.

[Java SE][java-util] API example:

```xml
<dependencies>
  <dependency>
    <groupId>org.jsonurl</groupId>
    <artifactId>jsonurl-core</artifactId>
    <version>${jsonurl.version}</version>
  </dependency>
</dependencies>
```
```java
import org.jsonurl.JavaValueParser;

JavaValueParser p = new JavaValueParser();
Map obj = p.parseObject( "(Hello:World!)" );
System.out.println(obj.get("Hello")) // World!
```

[Json.org][javadoc-org-json] example:

```xml
<dependencies>
  <dependency>
    <groupId>org.jsonurl</groupId>
    <artifactId>jsonurl-jsonorg</artifactId>
    <version>${jsonurl.version}</version>
  </dependency>
</dependencies>
```
```java
import org.jsonurl.jsonorg.JsonUrlParser;

JsonUrlParser p = new JsonUrlParser();
JSONObject obj = p.parseObject( "(Hello:World!)" );
System.out.println(obj.get("Hello")) // World!
```

[JSR-374][javadoc-javax-json] example:

```xml
<dependencies>
  <dependency>
    <groupId>org.jsonurl</groupId>
    <artifactId>jsonurl-jsr374</artifactId>
    <version>${jsonurl.version}</version>
  </dependency>
  <dependency>
    <groupId>org.glassfish</groupId>
    <artifactId>javax.json</artifactId>
    <version>${javax.json.version}</version>
  </dependency>
</dependencies>
```
```java
import org.jsonurl.jsonp.JsonUrlParser;

JsonUrlParser p = new JsonUrlParser();
JsonObject obj = p.parseObject( "(Hello:World!)" );
System.out.println(obj.get("Hello")) // World!
```

## Javadocs
All artifacts published to Maven Central include `sources` and `javadoc` JARs.
You can browse the current, and all previous revisions, via Javadoc.io:
  + [jsonurl-core](https://javadoc.io/doc/org.jsonurl/jsonurl-core)
  + [jsonurl-jsonorg](https://javadoc.io/doc/org.jsonurl/jsonurl-jsonorg)
  + [jsonurl-jsr374](https://javadoc.io/doc/org.jsonurl/jsonurl-jsr374)

Additionally, Javadocs are also generated automatically on pushes to
[master](https://jsonurl.github.io/jsonurl-java/). 
  

## Security
The parser is designed to parse untrusted input. It supports limits on
the number of parsed values and depth of nested arrays or objects.
When the limit is exceeded a [LimitException][limit-exception] is thrown.
Sane limit values are set by default. 

## License
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fjsonurl%2Fjsonurl-java.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2Fjsonurl%2Fjsonurl-java?ref=badge_large)

[RFC8259]: https://tools.ietf.org/html/rfc8259
[RFC3986]: https://tools.ietf.org/html/rfc3986
[RFC1738]: https://tools.ietf.org/html/rfc1738
[java-generic]: https://docs.oracle.com/javase/tutorial/java/generics/types.html
[parser]: module/jsonurl-core/src/main/java/org/jsonurl/Parser.java
[limit-exception]: module/jsonurl-core/src/main/java/org/jsonurl/LimitException.java
[jsonorg-module]: module/jsonurl-jsonorg/src/main/java/org/jsonurl/jsonorg/JsonUrlParser.java
[jsr374-module]: module/jsonurl-jsr374/src/main/java/org/jsonurl/jsonp/JsonUrlParser.java
[java-map]: https://docs.oracle.com/javase/8/docs/api/java/util/Map.html
[java-list]: https://docs.oracle.com/javase/8/docs/api/java/util/List.html
[javadoc-org-json]: https://javadoc.io/doc/org.json/json/
[javadoc-javax-json]: https://javadoc.io/doc/javax.json/javax.json-api/
[java-util]: https://docs.oracle.com/javase/8/docs/api/java/util/


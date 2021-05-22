# ChangeLog (jsonurl-java)

## v2.0.1

### Overview

Version 2 is a major refactor, and introduces breaking changes to
`core`. A new streaming event iterator interface provides the primary
parsing functionality. The abstract Parser interface has been moved to a new
`factory` package, and is implemented as a layer on top of `core`. The
`jsonorg` and `jsr374` interfaces are mostly backwards compatible, with
the primary difference that they now extend a class from `factory` rather
than `core`.

### New Features

*   Add support for Address Bar Query String Friendly (AQF) syntax [view commit](http://github.com/jsonurl/jsonurl-java/commit/f5b78c92451c0519bce47ebf5a6ef12737840426)
*   Add support for parsing implied arrays and objects [view commit](http://github.com/jsonurl/jsonurl-java/commit/bc8b3ef3f3687742a6781e4d20c324083d918521)
*   Add support for implied objects with missing values [view commit](http://github.com/jsonurl/jsonurl-java/commit/b6404d15fa841d241df2ffac7f7349ef7561156d)
*   Add support for implied-string-literals option [view commit](http://github.com/jsonurl/jsonurl-java/commit/9ee992a70a37a0fd9961b2552e40a538aaa45c49)
*   Add support for skip-nulls option [view commit](http://github.com/jsonurl/jsonurl-java/commit/968cb2d4b34f63fe03f2f5bb9e8945549d43811a)
*   Add support for coercing null to empty string on input/output [view commit](http://github.com/jsonurl/jsonurl-java/commit/6066dc0ceb09d5d8336cc9ea6c4a8cd3a7e31d2b)
*   Add support for user-supplied MathContext values [view commit](http://github.com/jsonurl/jsonurl-java/commit/20c104cc89a908d9321bef0469337f029fa3f9d8)

### Bug Fixes

*   Remove JsonUrlStringBuilder.add(char) method and replace with JsonUrlStringBuilder.addCodePoint(char) [view commit](http://github.com/jsonurl/jsonurl-java/commit/193b621bb6871cb2703ace7dfdfaed8efba480f5)
*   Fix bug when encoding quoted colon [view commit](http://github.com/jsonurl/jsonurl-java/commit/174193e0532479efc19b8dbec259af2e923dd6f4)
*   Fix bug in validation of empty composite values. [view commit](http://github.com/jsonurl/jsonurl-java/commit/7735274ba7398bf86dda860568e85895ed2d72db)

### Dependency Update

*   Upgrade org.json:json to 20210307 [view commit](http://github.com/jsonurl/jsonurl-java/commit/cd18de12a8bd8a0b818116c1851cd35b9f16fc54)

## v2.0.0

Unreleased.

## v1.2.0

### New Features

*   Improvements to API documentation [view commit](http://github.com/jsonurl/jsonurl-java/commit/275fefa47fa9f7a0ef131dc8dde834980deaa713)
*   Add ValueType class [view commit](http://github.com/jsonurl/jsonurl-java/commit/ffdbb0e35e5ec5f94d0fbe2ecd7512e31b52663e)

### Bug Fixes

*   Fix broken javadoc references [view commit](http://github.com/jsonurl/jsonurl-java/commit/b9905704ff5467cec08e943d041d385f54152460)
*   Fix NumberText and NumberBuilder [view commit](http://github.com/jsonurl/jsonurl-java/commit/5ff18b0fe461580e5d4338a745deb239705871a2)

### Dependency Update

*   Update org.json:json to 20200518 [view commit](http://github.com/jsonurl/jsonurl-java/commit/2ef47acbcb1079f07702d9131d69261ac797f35e)

## v1.1.0

### Bug Fixes

*   Make NumberBuilder no longer Serializable [view commit](http://github.com/jsonurl/jsonurl-java/commit/d4dc7595d42dc8ad8fa0a0f0e6044fcdc667997e)
*   Fixed a bug in the Parser.parse() [view commit](http://github.com/jsonurl/jsonurl-java/commit/c01e80d2b400dd51ffec7cdd5a793a0e9b58c147)
*   Fix improper encoding of "=" [view commit](http://github.com/jsonurl/jsonurl-java/commit/0136c0c6bb68080c0ab6289e1d7e6059310cc461)

## v1.0.0

*   Initial release

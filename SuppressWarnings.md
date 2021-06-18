# JSON&#x2192;URL: common uses of @SuppressWarnings

There are a number of places in the code where a `@SuppressWarnings`
annotation is used. Rather than repeating the `WONTFIX` or `FALSE POSITIVE`
justification for common use cases all over the code, this file serves as a
reference.

## !API!
Items marked with `!API!` would break the existing public API if the
PMD/sonar recommendation was followed. They're good candidates for future
improvement.

## Complexity

This is reported by PMD via
[CyclomaticComplexity](https://pmd.github.io/pmd/pmd_rules_java_design.html#cyclomaticcomplexity)
and
[NPathComplexity](https://pmd.github.io/pmd/pmd_rules_java_design.html#npathcomplexity),
and
reported by sonarlint via
[java:S3776](https://rules.sonarsource.com/c/RSPEC-3776).

While
[complexity](https://en.wikipedia.org/wiki/Cyclomatic_complexity)
is a thing one generally wants to avoid, in these cases the method's content is
really a single, logical thought. The tool's recommendation is to reduce the
complexity of the method, which usually means splitting the code into multiple
methods. However, splitting it into multiple methods, in this case, won't
allow them to be independently tested (a primary motivation), and it will likely
make it harder to follow the logic due to the additional indirection.

```java
@SuppressWarnings({
    // See SuppressWarnings.md#complexity
    "PMD.CyclomaticComplexity",
    "PMD.NPathComplexity",
    "java:S3776"
})
```

## Reassigning Loop Variables

Generally you want to avoid assigning to loop variables. However, sometimes it's
necessary to make the code function properly, and alternative implementations
are far less readable.

```java
@SuppressWarnings({
    // See SuppressWarnings.md
    "PMD.AvoidReassigningLoopVariables", "java:S127"
})
```
## Shadowing an instance variable with a local variable

It's often a bad idea to shadow an instance variable with a local variable.
However, in some cases that's exactly what you want, when the local variable
has the same semantic meaning as the instance variable, but it:

* serves as a cache (improves performance)
* needs to have some logic applied before it can be used

```java
@SuppressWarnings("java:S1117") // See SuppressWarnings.md
```

## Shadowing a class/interface name

It's usually bad practice to shadow a class or interface name that you
reference. However, the way the JSON&#x2192;URL API is intended to be used, a
user should never attempt to use both at the same time. Each derived class is
an implementation of the parent class for its implementation target (e.g.
json.org, JSR374, etc), and the user should only ever be using the derived
class.

```java
@SuppressWarnings("java:S2176") // See SuppressWarnings.md
```

## Type parameter names

Names should follow a naming convention, and generics are no exception.
JSON&#x2192;URL does follow a convention, it's just not the default, and I
couldn't find a PMD parameter that allows me to change the regex.

```java
@SuppressWarnings({"PMD.GenericsNaming", "java:S119"}) // See SuppressWarnings.md
```


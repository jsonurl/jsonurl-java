Contributing to jsonurl-java 
======================
So you found a bug or want to contribute a new feature? Great! Before
you dig into the code there are some guidelines I need contributors to follow
so that I have a reasonable chance of keeping things organized without too much
effort.

Getting Started
---------------
+ Make sure you have a [GitHub account](https://github.com/signup/free).
+ If you just want to make a small change see "Making Trivial Changes" below.
+ See if there is already a
  [discussion][discussions] or [issue][issues], or create a new one if
  necessary
  + For defects, clearly describe the problem, including steps to reproduce
  + For features, clearly describe the idea, including intent, audience, and
    use cases
+ If you're planning to implement a new feature it's a good idea to
  [discuss][discussions] it first. This can help make sure you're not wasting
  your time on something that's considered out of scope.
+ Fork the repository on GitHub.

Making and Submitting Changes
--------------
I accept pull requests via GitHub.  Here are some guidelines which will make
applying PRs easier for me:
+ Create a topic/feature branch from the `main` branch to base your work, and
  push your changes to that branch in your fork of the repository
+ Make commits of logical units and with [meaningful][commit-message-howto],
  [conventional commit][cc] messages that reference the related
  GitHub issue. The following commit types are currently in use:
  + feat - A new feature
  + fix - A bug fix
  + docs - Documentation changes
  + style - Changes that do not affect code execution (e.g. formatting)
  + refactor - A code change that neither fixes a bug nor adds a feature
  + perf - A code change that improves performance
  + test - A change to test configuration, or one or more files in the `test`
    directory; it must not affect the primary artifact.
  + chore - Changes to the build or workflow process; it must not affect the
    primary artifact.
  + update - dependency update
+ Use the [style][style] and [code conventions][conventions] of the existing
  codebase
+ Make sure you have added/updated tests for your changes, and that all tests
  pass
+ Submit a pull request once you're ready to merge

Making Trivial Changes
----------------------
For small changes, like tweaks to comments and/or documentation, you don't need
to create an issue. A fork + PR with a proper commit message (i.e. docs: ...)
is sufficient.

[discussions]: https://github.com/jsonurl/jsonurl-java/discussions
[issues]: https://github.com/jsonurl/jsonurl-java/issues
[style]: config/checkstyle.xml
[conventions]: config/pmd-ruleset.xml
[commit-message-howto]: https://chris.beams.io/posts/git-commit/
[cc]: https://www.conventionalcommits.org/en/v1.0.0/


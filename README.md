# PHP Open Redirect Companion

Warning on a `header('Location: ...')` call whose argument is built
directly from a PHP superglobal (`$_GET`, `$_POST`, `$_REQUEST`,
`$_COOKIE`). This is the textbook Open Redirect anti-pattern
(CWE-601): an attacker crafts a link to the trusted site that
redirects the victim to an attacker-controlled domain, commonly used
for phishing since the initial URL still shows the trusted host.
OWASP's own "Unvalidated Redirects and Forwards" guidance documents
this as a real, well-known vulnerability class.

Confirmed real gap: "PHP Inspections (EA Extended)" (one of the most
widely used PHP inspection plugins on Marketplace) does not cover
open redirect detection anywhere in its documented security feature
list — confirmed by reading it before building this.

## Why it exists

```php
header('Location: ' . $_GET['url']);
```

compiles and runs fine — until someone shares a link like
`?url=https://evil-lookalike.example.com`, and the trusted domain
silently forwards visitors straight to a phishing page.

## Why built this way

- **100% static text analysis** — a regex-based line scanner, not a
  real PHP parser, so it works whether the PHP plugin is installed or
  not.

## v0.1 scope — stated honestly, not exhaustively

Only flags a `header(` call whose argument text contains both
`Location:` and a direct superglobal reference on the same line. A
redirect built from an intermediate variable assigned from a
superglobal several lines earlier isn't traced (real data-flow
analysis, out of scope for a text scanner).

## Usage

Open any `.php` file. A `header('Location: ...')` call built from a
superglobal shows a warning.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.

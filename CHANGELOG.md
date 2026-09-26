<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# PHP Open Redirect Companion Changelog

## [Unreleased]

## [0.1.1]

### Fixed

- Review/star CTA now links to this plugin's own Marketplace
  reviews page instead of the vendor's generic plugin list.

## [0.1.0]

### Added

- Warning on `header('Location: ...')` built directly from a PHP
  superglobal -- an Open Redirect vulnerability (CWE-601), not
  covered by "PHP Inspections (EA Extended)".
- 100% static text analysis, no PHP plugin dependency, no network
  calls, no telemetry. Free.

[Unreleased]: https://github.com/GapHunterLabs/php-open-redirect-companion/compare/0.1.1...HEAD
[0.1.1]: https://github.com/GapHunterLabs/php-open-redirect-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/php-open-redirect-companion/commits/0.1.0

Gradle MaryTTS voicebuilding plugin
===================================

[Unreleased]
------------

## Added

- Dynamic task generation based on `build/basenames.lst` file (or `build/text/*.txt` files)
- Parallel processing of data files (when running with `--parallel -Dorg.gradle.parallel.intra=true`)
- New `bootstrap` task prepares project for `build`
- Workaround for legacy Praat version (<6): set environment variable `LEGACY_PRAAT=1`.

## Changed

- Rewrote several task classes used by data plugin
- Fixed Travis CI integration to work with trusty containers

[v5.2.2] - 2017-07-21
---------------------

### Changes

- Build with Gradle to v3.5
- Parameterized functional tests via buildscript resources

### Removed

- Excluded transitive `groovy-all` dependency
- Bundled help library split into separate artifact on JCenter

[v5.2.1] - 2017-04-13
---------------------

### Added

- Plugin publishing via Gradle Plugin Publishing plugin

[v5.2.0] - 2016-10-13
---------------------

### Changes

- Upgrade MaryTTS to v5.2
- Build with Gradle v2.14.1
- Configurable language component dependency
- Split into four distinct plugins

### Added

- CI testing via Travis
- Plugin functional testing via Gradle TestKit
- Bundled helper library code for batch processing

[v0.5.2.1] - 2015-03-05
-----------------------

### Changes

- Switched to GPL
- Build with Gradle v2.3

[v0.5.1.2] - 2015-02-09
-----------------------

### Changes

- Upgrade MaryTTS v5.1.2
- Build with Gradle v2.2.1

[v0.5.1] - 2014-10-07
---------------------

### Initial release

- Initial version indexed on plugins.gradle.org, extracted from project used to build voices for MaryTTS v5.1

[Unreleased]: https://github.com/marytts/gradle-marytts-voicebuilding-plugin/compare/v5.2.2...HEAD
[v5.2.2]: https://github.com/marytts/gradle-marytts-voicebuilding-plugin/releases/tag/v5.2.2
[v5.2.1]: https://github.com/marytts/gradle-marytts-voicebuilding-plugin/releases/tag/v5.2.1
[v5.2.0]: https://github.com/marytts/gradle-marytts-voicebuilding-plugin/releases/tag/v5.2.0
[v0.5.2.1]: https://github.com/marytts/gradle-marytts-voicebuilding-plugin/releases/tag/v0.5.2.1
[v0.5.1.2]: https://github.com/marytts/gradle-marytts-voicebuilding-plugin/releases/tag/v0.5.1.2
[v0.5.1]: https://github.com/marytts/gradle-marytts-voicebuilding-plugin/releases/tag/v0.5.1

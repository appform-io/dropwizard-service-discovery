# Changelog
All notable changes to this project will be documented in this file.

## [2.0.28-5]
- Added a criteriaResolver and introduced a TransportType resolver as subType
- Upgraded ranger version to 1.0-RC10

## [2.0.28-5]
- Removed region variable and added a node info resolver instead
- Upgraded ranger version to 1.0-RC9

## [2.0.28-4]
- Freeing up wasted ids in collision checker

## [2.0.28-2.RC5]
- Added region and tags to nodeData, to be able to run Predicate atop it with a finder.
- Upgraded ranger version to 1.0-RC7
- 
## [2.0.28-2.RC4]
- Collapsed the multiple modules into one, since the client is now contained in the ranger version 1.0-RC2 and above. 
- Made the necessary packaging changes to work with the new ranger (io.appform)
- Minor sonar and lombok fixes

## [1.3.13-5]

### Changed
- Optimized child node info read from ZK   


## [1.3.13-4]
### Added
- Dropwizard lifecycle listener healthcheck

### Changed
- Deprecated `initialDelaySeconds`  


## [1.3.13-3]
### Added
- Intelligent healthcheck update on zookeeper
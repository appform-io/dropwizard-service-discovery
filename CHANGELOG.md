# Changelog
All notable changes to this project will be documented in this file.

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
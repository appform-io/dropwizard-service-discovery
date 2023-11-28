# Changelog
All notable changes to this project will be documented in this file.
## [2.0.28-11]
- Java version upgrade to 17 (-releasse 11) and dropwizard version upgraded to 2.1.10

## [2.0.28-11]
- Fixed hierarchical environment aware shard selector  : If a service  is deployed with environment :  env.x.y.z then it should be able to discover other services  present in enviroment -  [env , env.x , env.x.y, env.x.y.z ]

## [2.0.28-10]
- Updated ranger version

## [2.0.28-9]
- Updated ranger version

## [2.0.28-8]
- Refactored to get and test non-empty published host from service discovery configuration

## [2.0.28-7]
- Added validation to prevent users from publishing local address to remote zookeeper

## [2.0.28-6]
- Added a criteriaResolver and introduced a PortScheme resolver as subType
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
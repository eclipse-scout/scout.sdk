# Instructions how to build

The Repository consists of 4 parts

1. Core modules
2. Eclipse feature and P2 update-site
3. IntelliJ plugin
4. Updatesite Maven plugin

## Build the core modules and the updatesite Maven plugin

execute the following Maven command in the root directory:

	mvn clean install

## Build the Eclipse feature and the P2 update-site

After the core modules build, execute the following additional Maven command in the root directory:

	mvn clean install -Dp2

This creates the P2 update-site here: org.eclipse.scout.sdk-repository/target/org.eclipse.scout.sdk-repository-*.zip

## Build the IntelliJ plugin

After the core modules build, execute the following Gradle command in the directory org.eclipse.scout.sdk.s2i

	clean buildPlugin -s -i --warning-mode=all

This creates the plugin here: org.eclipse.scout.sdk.s2i/build/distributions/org.eclipse.scout.sdk.s2i-*.zip

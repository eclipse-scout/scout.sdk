## Instructions how-to build

As the we have default maven modules with pom dependencies and also (plugins), a feature and a repository with p2 dependencies where we need a tycho-build we need two build steps.

So follow these 2 steps to get the update site:

	mvn clean install
	mvn clean install -Dp2

Now you have the update-site here:

 * org.eclipse.scout.sdk-repository/target/org.eclipse.scout.sdk-repository-*.zip

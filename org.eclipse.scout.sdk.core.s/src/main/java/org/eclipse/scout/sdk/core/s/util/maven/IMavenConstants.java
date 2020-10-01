/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.util.maven;

/**
 * <h3>{@link IMavenConstants}</h3>
 *
 * @since 5.2.0
 */
public interface IMavenConstants {

  String POM_XML_NAMESPACE = "http://maven.apache.org/POM/4.0.0";

  String POM = "pom.xml";
  String PROJECT = "project";
  String DEPENDENCIES = "dependencies";
  String DEPENDENCY = "dependency";
  String GROUP_ID = "groupId";
  String ARTIFACT_ID = "artifactId";
  String VERSION = "version";
  String PROPERTIES = "properties";
  String DEPENDENCY_MANAGEMENT = "dependencyManagement";
  String MODULES = "modules";
  String MODULE = "module";
  String EXECUTION = "execution";
  String EXECUTIONS = "executions";
  String ID = "id";
  String GOALS = "goals";
  String GOAL = "goal";
  String CONFIGURATION = "configuration";
  String PLUGIN = "plugin";
  String PLUGINS = "plugins";
  String BUILD = "build";
  String PARENT = "parent";
  String NAME = "name";
  String RELATIVE_PATH = "relativePath";
  String SCOPE = "scope";

  String LATEST = "LATEST";
}

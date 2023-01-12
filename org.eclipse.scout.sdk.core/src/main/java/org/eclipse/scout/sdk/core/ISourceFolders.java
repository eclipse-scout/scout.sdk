/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core;

/**
 * <h3>{@link ISourceFolders}</h3>
 *
 * @since 7.0.0
 */
public interface ISourceFolders {

  /**
   * Main java source folder
   */
  String MAIN_JAVA_SOURCE_FOLDER = "src/main/java";

  /**
   * Main resource folder
   */
  String MAIN_RESOURCE_FOLDER = "src/main/resources";

  /**
   * Test java source folder
   */
  String TEST_JAVA_SOURCE_FOLDER = "src/test/java";

  /**
   * Test resource folder
   */
  String TEST_RESOURCE_FOLDER = "src/test/resources";

  /**
   * Generated source folder for annotation processor generated sources.
   */
  String GENERATED_ANNOTATIONS_SOURCE_FOLDER = "target/generated-sources/annotations";

  /**
   * Generated source folder for web-service stubs
   */
  String GENERATED_WS_IMPORT_SOURCE_FOLDER = "target/generated-sources/wsimport";

}

package org.eclipse.scout.sdk.operation.project;

import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.PropertyMap;

public interface IScoutProjectNewOperation extends IOperation {

  // list of java projects that have been created so far
  String PROP_CREATED_BUNDLES = "CREATED_BUNDLES";

  // project properties that are always present
  String PROP_PROJECT_NAME = "GROUP";
  String PROP_PROJECT_ALIAS = "ALIAS";
  String PROP_PROJECT_NAME_POSTFIX = "POSTFIX";
  String PROP_PROJECT_CHECKED_NODES = "CHECKED_NODES";
  String PROP_SELECTED_TEMPLATE_NAME = "TEMPLATE_NAME";
  String PROP_TARGET_PLATFORM_VERSION = "TARGET_PLATFORM_VERSION";
  String PROP_USE_DEFAULT_JDT_PREFS = "PROP_USE_DEFAULT_JDT_PREFS";

  // system properties that are always present
  String PROP_OS = "OSGI_OS";
  String PROP_WS = "OSGI_WS";
  String PROP_ARCH = "OSGI_ARCH";
  String PROP_LOCALHOST = "LOCALHOST";
  String PROP_CURRENT_DATE = "CURRENT_DATE";
  String PROP_USER_NAME = "USER_NAME";
  String PROP_EXEC_ENV = "EXECUTION_ENVIRONMENT";

  /**
   * 1. step for an operation in the project creation pipeline: the properties are set
   * 
   * @param properties
   */
  void setProperties(PropertyMap properties);

  /**
   * 2. step for an operation in the project creation pipeline: the operation can define if it is required to execute
   * (based on the properties).
   * If this method returns false, the operation is not allowed to progress further in the pipeline.
   * 
   * @return
   */
  boolean isRelevant();

  /**
   * 3. step for an operation in the project creation pipeline: the operation can initiate local
   * members based on the already existing properties
   */
  void init();

  /**
   * 4. step for an operation in the project creation pipeline: the operation is validated
   * 
   * @see IOperation
   */

  /**
   * 5. step for an operation in the project creation pipeline: the operation is executed
   * 
   * @see IOperation
   */
}

/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core;

/**
 *
 */
public interface IJavaRuntimeTypes {

  // primitive types
  String _boolean = "boolean";
  String _byte = "byte";
  String _char = "char";
  String _double = "double";
  String _float = "float";
  String _int = "int";
  String _long = "long";
  String _short = "short";
  String _void = "void";

  // common types
  String java_lang_Boolean = "java.lang.Boolean";
  String java_lang_Byte = "java.lang.Byte";
  String java_lang_Character = "java.lang.Character";
  String java_lang_Class = "java.lang.Class";
  String java_lang_Double = "java.lang.Double";
  String java_lang_Exception = "java.lang.Exception";
  String java_lang_Float = "java.lang.Float";
  String java_lang_Integer = "java.lang.Integer";
  String java_lang_Long = "java.lang.Long";
  String java_lang_Number = "java.lang.Number";
  String java_lang_Object = "java.lang.Object";
  String java_lang_Short = "java.lang.Short";
  String java_lang_String = "java.lang.String";
  String java_lang_Throwable = "java.lang.Throwable";
  String java_lang_Void = "java.lang.Void";
  String java_math_BigDecimal = "java.math.BigDecimal";
  String java_math_BigInteger = "java.math.BigInteger";
  String java_sql_Blob = "java.sql.Blob";
  String java_sql_Clob = "java.sql.Clob";
  String java_sql_Date = "java.sql.Date";
  String java_sql_SQLException = "java.sql.SQLException";
  String java_sql_Time = "java.sql.Time";
  String java_sql_Timestamp = "java.sql.Timestamp";
  String java_sql_Types = "java.sql.Types";
  String java_util_Calendar = "java.util.Calendar";
  String java_util_Collection = "java.util.Collection";
  String java_util_Collections = "java.util.Collections";
  String java_util_Date = "java.util.Date";
  String java_util_List = "java.util.List";
  String java_util_Map = "java.util.Map";
  String java_util_Set = "java.util.Set";

  // common annotations
  String java_lang_CharSequence = "java.lang.CharSequence";
  String java_lang_Deprecated = "java.lang.Deprecated";
  String java_lang_Override = "java.lang.Override";
  String java_lang_SuppressWarnings = "java.lang.SuppressWarnings";
  String javax_annotation_Generated = "javax.annotation.Generated";

  String java_security_BasicPermission = "java.security.BasicPermission";
  String java_security_Permission = "java.security.Permission";
}

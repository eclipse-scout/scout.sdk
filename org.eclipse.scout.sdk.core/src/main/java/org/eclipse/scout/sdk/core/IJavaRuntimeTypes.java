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
 * Holds class names of the JRE.
 */
@SuppressWarnings("squid:S00115")
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
  String Boolean = "java.lang.Boolean";
  String Byte = "java.lang.Byte";
  String Character = "java.lang.Character";
  String Class = "java.lang.Class";
  String Double = "java.lang.Double";
  String Float = "java.lang.Float";
  String Integer = "java.lang.Integer";
  String Long = "java.lang.Long";
  String Number = "java.lang.Number";
  String Object = "java.lang.Object";
  String Short = "java.lang.Short";
  String String = "java.lang.String";
  String Void = "java.lang.Void";
  String BigDecimal = "java.math.BigDecimal";
  String BigInteger = "java.math.BigInteger";
  String Blob = "java.sql.Blob";
  String Clob = "java.sql.Clob";
  String SqlDate = "java.sql.Date";
  String SQLException = "java.sql.SQLException";
  String Time = "java.sql.Time";
  String Timestamp = "java.sql.Timestamp";
  String Types = "java.sql.Types";
  String Calendar = "java.util.Calendar";
  String Collection = "java.util.Collection";
  String Collections = "java.util.Collections";
  String UtilDate = "java.util.Date";
  String List = "java.util.List";
  String Map = "java.util.Map";
  String Set = "java.util.Set";
  String CharSequence = "java.lang.CharSequence";
  String BasicPermission = "java.security.BasicPermission";
  String Permission = "java.security.Permission";
  String ObjectStreamException = "java.io.ObjectStreamException";

  // common annotations
  String Deprecated = "java.lang.Deprecated";
  String Override = "java.lang.Override";
  String SuppressWarnings = "java.lang.SuppressWarnings";
  String Generated = "javax.annotation.Generated";
  String WebService = "javax.jws.WebService";
  String WebServiceClient = "javax.xml.ws.WebServiceClient";
}

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
package org.eclipse.scout.sdk.core.signature;

/**
 *
 */
public interface ISignatureConstants {
  /**
   * Character constant indicating an array type in a signature. Value is <code>'['</code>.
   */
  char C_ARRAY = org.eclipse.jdt.internal.compiler.util.Util.C_ARRAY;

  /**
   * Character constant indicating the primitive type boolean in a signature. Value is <code>'Z'</code>.
   */
  char C_BOOLEAN = org.eclipse.jdt.internal.compiler.util.Util.C_BOOLEAN;

  /**
   * Character constant indicating the primitive type byte in a signature. Value is <code>'B'</code>.
   */
  char C_BYTE = org.eclipse.jdt.internal.compiler.util.Util.C_BYTE;

  /**
   * Character constant indicating a capture of a wildcard type in a signature. Value is <code>'!'</code>.
   *
   * @since 3.1
   */
  char C_CAPTURE = org.eclipse.jdt.internal.compiler.util.Util.C_CAPTURE;

  /**
   * Character constant indicating the primitive type char in a signature. Value is <code>'C'</code>.
   */
  char C_CHAR = org.eclipse.jdt.internal.compiler.util.Util.C_CHAR;

  /**
   * Character constant indicating the colon in a signature. Value is <code>':'</code>.
   *
   * @since 3.0
   */
  char C_COLON = org.eclipse.jdt.internal.compiler.util.Util.C_COLON;

  /**
   * Character constant indicating the dollar in a signature. Value is <code>'$'</code>.
   */
  char C_DOLLAR = org.eclipse.jdt.internal.compiler.util.Util.C_DOLLAR;

  /**
   * Character constant indicating the dot in a signature. Value is <code>'.'</code>.
   */
  char C_DOT = org.eclipse.jdt.internal.compiler.util.Util.C_DOT;

  /**
   * Character constant indicating the primitive type double in a signature. Value is <code>'D'</code>.
   */
  char C_DOUBLE = org.eclipse.jdt.internal.compiler.util.Util.C_DOUBLE;

  /**
   * Character constant indicating an exception in a signature. Value is <code>'^'</code>.
   *
   * @since 3.1
   */
  char C_EXCEPTION_START = org.eclipse.jdt.internal.compiler.util.Util.C_EXCEPTION_START;

  /**
   * Character constant indicating a bound wildcard type argument in a signature with extends clause. Value is
   * <code>'+'</code>.
   *
   * @since 3.1
   */
  char C_EXTENDS = org.eclipse.jdt.internal.compiler.util.Util.C_EXTENDS;

  /**
   * Character constant indicating the primitive type float in a signature. Value is <code>'F'</code>.
   */
  char C_FLOAT = org.eclipse.jdt.internal.compiler.util.Util.C_FLOAT;

  /**
   * Character constant indicating the end of a generic type list in a signature. Value is <code>'&gt;'</code>.
   *
   * @since 3.0
   */
  char C_GENERIC_END = org.eclipse.jdt.internal.compiler.util.Util.C_GENERIC_END;

  /**
   * Character constant indicating the start of a formal type parameter (or type argument) list in a signature. Value is
   * <code>'&lt;'</code>.
   *
   * @since 3.0
   */
  char C_GENERIC_START = org.eclipse.jdt.internal.compiler.util.Util.C_GENERIC_START;

  /**
   * Character constant indicating the primitive type int in a signature. Value is <code>'I'</code>.
   */
  char C_INT = org.eclipse.jdt.internal.compiler.util.Util.C_INT;

  /**
   * Character constant indicating an intersection type in a signature. Value is <code>'|'</code>.
   *
   * @since 3.7.1
   */
  char C_INTERSECTION = '|';

  /**
   * Character constant indicating the primitive type long in a signature. Value is <code>'J'</code>.
   */
  char C_LONG = org.eclipse.jdt.internal.compiler.util.Util.C_LONG;

  /**
   * Character constant indicating the end of a named type in a signature. Value is <code>';'</code>.
   */
  char C_NAME_END = org.eclipse.jdt.internal.compiler.util.Util.C_NAME_END;

  /**
   * Character constant indicating the end of a parameter type list in a signature. Value is <code>')'</code>.
   */
  char C_PARAM_END = org.eclipse.jdt.internal.compiler.util.Util.C_PARAM_END;

  /**
   * Character constant indicating the start of a parameter type list in a signature. Value is <code>'('</code>.
   */
  char C_PARAM_START = org.eclipse.jdt.internal.compiler.util.Util.C_PARAM_START;

  /**
   * Character constant indicating the start of a resolved, named type in a signature. Value is <code>'L'</code>.
   */
  char C_RESOLVED = org.eclipse.jdt.internal.compiler.util.Util.C_RESOLVED;

  /**
   * Character constant indicating the semicolon in a signature. Value is <code>';'</code>.
   */
  char C_SEMICOLON = org.eclipse.jdt.internal.compiler.util.Util.C_SEMICOLON;

  /**
   * Character constant indicating the primitive type short in a signature. Value is <code>'S'</code>.
   */
  char C_SHORT = org.eclipse.jdt.internal.compiler.util.Util.C_SHORT;

  /**
   * Character constant indicating an unbound wildcard type argument in a signature. Value is <code>'*'</code>.
   *
   * @since 3.0
   */
  char C_STAR = org.eclipse.jdt.internal.compiler.util.Util.C_STAR;

  /**
   * Character constant indicating a bound wildcard type argument in a signature with super clause. Value is
   * <code>'-'</code>.
   *
   * @since 3.1
   */
  char C_SUPER = org.eclipse.jdt.internal.compiler.util.Util.C_SUPER;

  /**
   * Character constant indicating the start of a resolved type variable in a signature. Value is <code>'T'</code>.
   *
   * @since 3.0
   */
  char C_TYPE_VARIABLE = org.eclipse.jdt.internal.compiler.util.Util.C_TYPE_VARIABLE;

  /**
   * Character constant indicating the start of an unresolved, named type in a signature. Value is <code>'Q'</code>.
   */
  char C_UNRESOLVED = org.eclipse.jdt.internal.compiler.util.Util.C_UNRESOLVED;

  /**
   * Character constant indicating result type void in a signature. Value is <code>'V'</code>.
   */
  char C_VOID = org.eclipse.jdt.internal.compiler.util.Util.C_VOID;

  /**
   * String constant for the signature of the primitive type boolean. Value is <code>"Z"</code>.
   */
  String SIG_BOOLEAN = "Z";

  /**
   * String constant for the signature of the primitive type byte. Value is <code>"B"</code>.
   */
  String SIG_BYTE = "B";
  /**
   * String constant for the signature of the primitive type char. Value is <code>"C"</code>.
   */
  String SIG_CHAR = "C";
  /**
   * String constant for the signature of the primitive type double. Value is <code>"D"</code>.
   */
  String SIG_DOUBLE = "D";
  /**
   * String constant for the signature of the primitive type float. Value is <code>"F"</code>.
   */
  String SIG_FLOAT = "F";
  /**
   * String constant for the signature of the primitive type int. Value is <code>"I"</code>.
   */
  String SIG_INT = "I";
  /**
   * String constant for the signature of the primitive type long. Value is <code>"J"</code>.
   */
  String SIG_LONG = "J";
  /**
   * String constant for the signature of the primitive type short. Value is <code>"S"</code>.
   */
  String SIG_SHORT = "S";
  /**
   * String constant for the signature of result type void. Value is <code>"V"</code>.
   */
  String SIG_VOID = "V";

  /**
   * String constant for the resolved signature of java.lang.Object.
   */
  String SIG_OBJECT = Signature.createTypeSignature(Object.class.getName());

  /**
   * Kind constant for a class type signature.
   *
   * @see #getTypeSignatureKind(String)
   * @since 3.0
   */
  int CLASS_TYPE_SIGNATURE = 1;

  /**
   * Kind constant for a base (primitive or void) type signature.
   *
   * @see #getTypeSignatureKind(String)
   */
  int BASE_TYPE_SIGNATURE = 2;
  /**
   * Kind constant for a type variable signature.
   *
   * @see #getTypeSignatureKind(String)
   * @since 3.0
   */
  int TYPE_VARIABLE_SIGNATURE = 3;
  /**
   * Kind constant for an array type signature.
   *
   * @see #getTypeSignatureKind(String)
   */
  int ARRAY_TYPE_SIGNATURE = 4;

  /**
   * Kind constant for a wildcard type signature.
   *
   * @see #getTypeSignatureKind(String)
   * @since 3.1
   */
  int WILDCARD_TYPE_SIGNATURE = 5;

  /**
   * Kind constant for the capture of a wildcard type signature.
   *
   * @see #getTypeSignatureKind(String)
   * @since 3.1
   */
  int CAPTURE_TYPE_SIGNATURE = 6;

  /**
   * Kind constant for the intersection type signature.
   *
   * @see #getTypeSignatureKind(String)
   */
  int INTERSECTION_TYPE_SIGNATURE = 7;
}

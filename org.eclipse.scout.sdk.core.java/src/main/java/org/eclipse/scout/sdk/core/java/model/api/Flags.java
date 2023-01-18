/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api;

/**
 * Utility class for decoding modifier flags in Java elements.
 * <p>
 * This class provides static methods only.
 * </p>
 * <p>
 * Note that the numeric values of these flags match the ones for class files as described in the Java Virtual Machine
 * Specification (except for {@link #AccDeprecated}, {@link #AccAnnotationDefault}, and {@link #AccDefaultMethod}).
 * </p>
 */
@SuppressWarnings("squid:S00115")
public final class Flags {

  /**
   * Constant representing the absence of any flag.
   */
  public static final int AccDefault = 0;

  /**
   * Public access flag. See The Java Virtual Machine Specification for more details.
   */
  public static final int AccPublic = 0x0001;

  /**
   * Private access flag. See The Java Virtual Machine Specification for more details.
   */
  public static final int AccPrivate = 0x0002;

  /**
   * Protected access flag. See The Java Virtual Machine Specification for more details.
   */
  public static final int AccProtected = 0x0004;

  /**
   * Static access flag. See The Java Virtual Machine Specification for more details.
   */
  public static final int AccStatic = 0x0008;

  /**
   * Final access flag. See The Java Virtual Machine Specification for more details.
   */
  public static final int AccFinal = 0x0010;

  /**
   * Synchronized access flag. See The Java Virtual Machine Specification for more details.
   */
  public static final int AccSynchronized = 0x0020;

  /**
   * Volatile property flag. See The Java Virtual Machine Specification for more details.
   */
  public static final int AccVolatile = 0x0040;

  /**
   * Transient property flag. See The Java Virtual Machine Specification for more details.
   */
  public static final int AccTransient = 0x0080;

  /**
   * Native property flag. See The Java Virtual Machine Specification for more details.
   */
  public static final int AccNative = 0x0100;

  /**
   * Interface property flag. See The Java Virtual Machine Specification for more details.
   */
  public static final int AccInterface = 0x0200;

  /**
   * Abstract property flag. See The Java Virtual Machine Specification for more details.
   */
  public static final int AccAbstract = 0x0400;

  /**
   * Strictfp property flag. See The Java Virtual Machine Specification for more details.
   */
  public static final int AccStrictfp = 0x0800;

  /**
   * Super property flag. See The Java Virtual Machine Specification for more details.
   */
  public static final int AccSuper = 0x0020;

  /**
   * Synthetic property flag. See The Java Virtual Machine Specification for more details.
   */
  public static final int AccSynthetic = 0x1000;

  /**
   * Deprecated property flag.
   * <p>
   * Note that this flag's value is internal and is not defined in the Virtual Machine specification.
   * </p>
   */
  public static final int AccDeprecated = 0x100000;

  /**
   * Bridge method property flag (added in J2SE 1.5). Used to flag a compiler-generated bridge methods. See The Java
   * Virtual Machine Specification for more details.
   */
  public static final int AccBridge = 0x0040;

  /**
   * Varargs method property flag (added in J2SE 1.5). Used to flag variable arity method declarations. See The Java
   * Virtual Machine Specification for more details.
   */
  public static final int AccVarargs = 0x0080;

  /**
   * Enum property flag (added in J2SE 1.5). See The Java Virtual Machine Specification for more details.
   */
  public static final int AccEnum = 0x4000;

  /**
   * Annotation property flag (added in J2SE 1.5). See The Java Virtual Machine Specification for more details.
   */
  public static final int AccAnnotation = 0x2000;

  /**
   * Default method property flag.
   * <p>
   * Note that this flag's value is internal and is not defined in the Virtual Machine specification.
   * </p>
   */
  public static final int AccDefaultMethod = 0x10000;

  /**
   * Annotation method default property flag. Used to flag annotation type methods that declare a default value.
   * <p>
   * Note that this flag's value is internal and is not defined in the Virtual Machine specification.
   * </p>
   */
  public static final int AccAnnotationDefault = 0x20000;

  /**
   * From class file version 52 (compliance 1.8 up), meaning that a formal parameter is mandated by a language
   * specification, so all compilers for the language must emit it.
   */
  public static final int AccMandated = 0x8000;

  /**
   * Not instantiable.
   */
  private Flags() {
    // Not instantiable
  }

  /**
   * Returns whether the given integer includes the {@code abstract} modifier.
   *
   * @param flags
   *          the flags
   * @return {@code true} if the {@code abstract} modifier is included
   */
  public static boolean isAbstract(int flags) {
    return (flags & AccAbstract) != 0;
  }

  /**
   * Returns whether the given integer includes the indication that the element is deprecated ({@code @deprecated} tag
   * in Javadoc comment).
   *
   * @param flags
   *          the flags
   * @return {@code true} if the element is marked as deprecated
   */
  public static boolean isDeprecated(int flags) { //NOSONAR
    return (flags & AccDeprecated) != 0;
  }

  /**
   * Returns whether the given integer includes the {@code final} modifier.
   *
   * @param flags
   *          the flags
   * @return {@code true} if the {@code final} modifier is included
   */
  public static boolean isFinal(int flags) {
    return (flags & AccFinal) != 0;
  }

  /**
   * Returns whether the given integer includes the {@code interface} modifier.
   *
   * @param flags
   *          the flags
   * @return {@code true} if the {@code interface} modifier is included
   * @since 2.0
   */
  public static boolean isInterface(int flags) {
    return (flags & AccInterface) != 0;
  }

  /**
   * Returns whether the given integer includes the {@code native} modifier.
   *
   * @param flags
   *          the flags
   * @return {@code true} if the {@code native} modifier is included
   */
  public static boolean isNative(int flags) {
    return (flags & AccNative) != 0;
  }

  /**
   * Returns whether the given integer does not include one of the {@code public}, {@code private}, or {@code protected}
   * flags.
   *
   * @param flags
   *          the flags
   * @return {@code true} if no visibility flag is set
   * @since 3.2
   */
  public static boolean isPackageDefault(int flags) {
    return (flags & (AccPublic | AccPrivate | AccProtected)) == 0;
  }

  /**
   * Returns whether the given integer includes the {@code private} modifier.
   *
   * @param flags
   *          the flags
   * @return {@code true} if the {@code private} modifier is included
   */
  public static boolean isPrivate(int flags) {
    return (flags & AccPrivate) != 0;
  }

  /**
   * Returns whether the given integer includes the {@code protected} modifier.
   *
   * @param flags
   *          the flags
   * @return {@code true} if the {@code protected} modifier is included
   */
  public static boolean isProtected(int flags) {
    return (flags & AccProtected) != 0;
  }

  /**
   * Returns whether the given integer includes the {@code public} modifier.
   *
   * @param flags
   *          the flags
   * @return {@code true} if the {@code public} modifier is included
   */
  public static boolean isPublic(int flags) {
    return (flags & AccPublic) != 0;
  }

  /**
   * Returns whether the given integer includes the {@code static} modifier.
   *
   * @param flags
   *          the flags
   * @return {@code true} if the {@code static} modifier is included
   */
  public static boolean isStatic(int flags) {
    return (flags & AccStatic) != 0;
  }

  /**
   * Returns whether the given integer includes the {@code super} modifier.
   *
   * @param flags
   *          the flags
   * @return {@code true} if the {@code super} modifier is included
   * @since 3.2
   */
  public static boolean isSuper(int flags) {
    return (flags & AccSuper) != 0;
  }

  /**
   * Returns whether the given integer includes the {@code strictfp} modifier.
   *
   * @param flags
   *          the flags
   * @return {@code true} if the {@code strictfp} modifier is included
   */
  public static boolean isStrictfp(int flags) {
    return (flags & AccStrictfp) != 0;
  }

  /**
   * Returns whether the given integer includes the {@code synchronized} modifier.
   *
   * @param flags
   *          the flags
   * @return {@code true} if the {@code synchronized} modifier is included
   */
  public static boolean isSynchronized(int flags) {
    return (flags & AccSynchronized) != 0;
  }

  /**
   * Returns whether the given integer includes the indication that the element is synthetic.
   *
   * @param flags
   *          the flags
   * @return {@code true} if the element is marked synthetic
   */
  public static boolean isSynthetic(int flags) {
    return (flags & AccSynthetic) != 0;
  }

  /**
   * Returns whether the given integer includes the {@code transient} modifier.
   *
   * @param flags
   *          the flags
   * @return {@code true} if the {@code transient} modifier is included
   */
  public static boolean isTransient(int flags) {
    return (flags & AccTransient) != 0;
  }

  /**
   * Returns whether the given integer includes the {@code volatile} modifier.
   *
   * @param flags
   *          the flags
   * @return {@code true} if the {@code volatile} modifier is included
   */
  public static boolean isVolatile(int flags) {
    return (flags & AccVolatile) != 0;
  }

  /**
   * Returns whether the given integer has the {@code AccBridge} bit set.
   *
   * @param flags
   *          the flags
   * @return {@code true} if the {@code AccBridge} flag is included
   * @see #AccBridge
   * @since 3.0
   */
  public static boolean isBridge(int flags) {
    return (flags & AccBridge) != 0;
  }

  /**
   * Returns whether the given integer has the {@code AccVarargs} bit set.
   *
   * @param flags
   *          the flags
   * @return {@code true} if the {@code AccVarargs} flag is included
   * @see #AccVarargs
   * @since 3.0
   */
  public static boolean isVarargs(int flags) {
    return (flags & AccVarargs) != 0;
  }

  /**
   * Returns whether the given integer has the {@code AccEnum} bit set.
   *
   * @param flags
   *          the flags
   * @return {@code true} if the {@code AccEnum} flag is included
   * @see #AccEnum
   * @since 3.0
   */
  public static boolean isEnum(int flags) {
    return (flags & AccEnum) != 0;
  }

  /**
   * Returns whether the given integer has the {@code AccAnnotation} bit set.
   *
   * @param flags
   *          the flags
   * @return {@code true} if the {@code AccAnnotation} flag is included
   * @see #AccAnnotation
   * @since 3.0
   */
  public static boolean isAnnotation(int flags) {
    return (flags & AccAnnotation) != 0;
  }

  /**
   * Returns whether the given integer has the {@code AccDefaultMethod} bit set. Note that this flag represents the
   * usage of the 'default' keyword on a method and should not be confused with the 'package' access visibility (which
   * used to be called 'default access').
   *
   * @return {@code true} if the {@code AccDefaultMethod} flag is included
   * @see #AccDefaultMethod
   * @since 3.10
   */
  public static boolean isDefaultMethod(int flags) {
    return (flags & AccDefaultMethod) != 0;
  }

  /**
   * Returns whether the given integer has the {@link #AccAnnotationDefault} bit set.
   *
   * @return {@code true} if the {@code AccAnnotationDefault} flag is included
   * @see #AccAnnotationDefault
   * @since 3.10
   */
  public static boolean isAnnotationDefault(int flags) {
    return (flags & AccAnnotationDefault) != 0;
  }

  /**
   * @see #toString(int, boolean)
   */
  public static String toString(int flags) {
    return toString(flags, false);
  }

  /**
   * Returns a standard string describing the given modifier flags. Only modifier flags are included in the output;
   * deprecated, synthetic, bridge, etc. flags are ignored.
   * <p>
   * The flags are output in the following order:
   *
   * <pre>
   * public protected private
   * abstract default static final synchronized native strictfp transient volatile
   * </pre>
   *
   * This order is consistent with the recommendations in JLS8 ("*Modifier:" rules in chapters 8 and 9).
   * <p>
   * Note that the flags of a method can include the AccVarargs flag that has no standard description. Since the
   * AccVarargs flag has the same value as the AccTransient flag (valid for fields only), attempting to get the
   * description of method modifiers with the AccVarargs flag set would result in an unexpected description. Clients
   * should ensure that the AccVarargs is not included in the flags of a method as follows:
   *
   * <pre>
   * IMethod method = ...
   * int flags = method.getFlags() & ~Flags.AccVarargs;
   * return Flags.toString(flags);
   * </pre>
   *
   * Examples results:
   *
   * <pre>
   *    {@code "public static final"}
   *    {@code "private native"}
   * </pre>
   *
   * @param flags
   *          the flags
   * @return the standard string representation of the given flags
   */
  @SuppressWarnings("pmd:NPathComplexity")
  public static String toString(int flags, boolean includeTrailingSpace) {
    var sb = new StringBuilder(32);

    if (isPublic(flags)) {
      sb.append("public ");
    }
    if (isProtected(flags)) {
      sb.append("protected ");
    }
    if (isPrivate(flags)) {
      sb.append("private ");
    }
    if (isAbstract(flags)) {
      sb.append("abstract ");
    }
    if (isDefaultMethod(flags)) {
      sb.append("default ");
    }
    if (isStatic(flags)) {
      sb.append("static ");
    }
    if (isFinal(flags)) {
      sb.append("final ");
    }
    if (isSynchronized(flags)) {
      sb.append("synchronized ");
    }
    if (isNative(flags)) {
      sb.append("native ");
    }
    if (isStrictfp(flags)) {
      sb.append("strictfp ");
    }
    if (isTransient(flags)) {
      sb.append("transient ");
    }
    if (isVolatile(flags)) {
      sb.append("volatile ");
    }
    var len = sb.length();
    if (len == 0) {
      return "";
    }
    if (!includeTrailingSpace) {
      sb.setLength(len - 1);
    }
    return sb.toString();
  }
}

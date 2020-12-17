/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.util;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.eclipse.scout.sdk.core.util.Strings.indexOf;
import static org.eclipse.scout.sdk.core.util.Strings.lastIndexOf;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * <h3>{@link JavaTypes}</h3>
 * <p>
 * Helper class to deal with java type names.
 *
 * @since 6.1.0
 */
@SuppressWarnings("squid:S00115")
public final class JavaTypes {

  /**
   * type name for a primitive {@code boolean}.
   */
  public static final String _boolean = "boolean";
  /**
   * type name for a primitive {@code byte}.
   */
  public static final String _byte = "byte";
  /**
   * type name for a primitive {@code char}.
   */
  public static final String _char = "char";
  /**
   * type name for a primitive {@code double}.
   */
  public static final String _double = "double";
  /**
   * type name for a primitive {@code float}.
   */
  public static final String _float = "float";
  /**
   * type name for a primitive {@code int}.
   */
  public static final String _int = "int";
  /**
   * type name for a primitive {@code long}.
   */
  public static final String _long = "long";
  /**
   * type name for a primitive {@code short}.
   */
  public static final String _short = "short";
  /**
   * type name for a primitive {@code void}.
   */
  public static final String _void = "void";
  /**
   * type name for the complex {@link Boolean} type.
   */
  public static final String Boolean = "java.lang.Boolean";
  /**
   * type name for the complex {@link Byte} type.
   */
  public static final String Byte = "java.lang.Byte";
  /**
   * type name for the complex {@link Character} type.
   */
  public static final String Character = "java.lang.Character";
  /**
   * type name for the complex {@link Double} type.
   */
  public static final String Double = "java.lang.Double";
  /**
   * type name for the complex {@link Float} type.
   */
  public static final String Float = "java.lang.Float";
  /**
   * type name for the complex {@link Integer} type.
   */
  public static final String Integer = "java.lang.Integer";
  /**
   * type name for the complex {@link Long} type.
   */
  public static final String Long = "java.lang.Long";
  /**
   * type name for the complex {@link Short} type.
   */
  public static final String Short = "java.lang.Short";
  /**
   * type name for the complex {@link Void} type.
   */
  public static final String Void = "java.lang.Void";
  /**
   * Character constant representing a dot. Value is {@code '.'}.
   */
  public static final char C_DOT = '.';
  /**
   * Character constant indicating the start of a formal type parameter (or type argument). Value is {@code '<'}.
   */
  public static final char C_GENERIC_START = '<';
  /**
   * Character constant indicating the end of a generic type. Value is {@code '>'}.
   */
  public static final char C_GENERIC_END = '>';
  /**
   * Character constant indicating an inner type. Value is {@code '$'}.
   */
  public static final char C_DOLLAR = '$';
  /**
   * Character constant indicating a sequence delimiter. Value is {@code ','}
   */
  public static final char C_COMMA = ',';
  /**
   * Character constant indicating a wildcard type. Value is {@code '?'}
   */
  public static final char C_QUESTION_MARK = '?';
  /**
   * Character constant indicating a space. Value is {@code ' '}
   */
  public static final char C_SPACE = ' ';
  /**
   * The Java {@code extends} keyword.
   */
  public static final String EXTENDS = "extends";
  /**
   * The Java {@code implements} keyword.
   */
  public static final String IMPLEMENTS = "implements";
  /**
   * The Java {@code super} keyword.
   */
  public static final String SUPER = "super";
  /**
   * the file extension for Java files. Value is '{@code java}'.
   */
  public static final String JAVA_FILE_EXTENSION = "java";
  /**
   * the file suffix for Java files. Value is '{@code .java}'.
   */
  public static final String JAVA_FILE_SUFFIX = C_DOT + JAVA_FILE_EXTENSION;
  /**
   * the file extension for class files. Value is '{@code class}'.
   */
  public static final String CLASS_FILE_EXTENSION = "class";
  /**
   * the file suffix for class files. Value is '{@code .class}'.
   */
  public static final String CLASS_FILE_SUFFIX = C_DOT + CLASS_FILE_EXTENSION;
  /**
   * package info base name: {@code package-info}.
   */
  public static final String PackageInfo = "package-info";
  /**
   * package info java file name: {@code package-info.java}.
   */
  public static final String PackageInfoJava = PackageInfo + JAVA_FILE_SUFFIX;
  /**
   * module info base name: {@code module-info}.
   */
  public static final String ModuleInfo = "module-info";
  /**
   * module info java file name: {@code module-info.java}.
   */
  public static final String ModuleInfoJava = ModuleInfo + JAVA_FILE_SUFFIX;

  private static final char C_ARRAY = '[';
  private static final FinalValue<Set<String>> JAVA_KEYWORDS = new FinalValue<>();
  private static final char[][] WILDCARD_MARKERS = {
      (C_SPACE + EXTENDS + C_SPACE).toCharArray(),
      (C_SPACE + SUPER + C_SPACE).toCharArray(),
      (EXTENDS + C_SPACE).toCharArray(),
      (SUPER + C_SPACE).toCharArray()
  };

  private JavaTypes() {
  }

  /**
   * @return {@code true} if the given word is a reserved java keyword. Otherwise {@code false}.
   * @since 3.8.3
   */
  public static boolean isReservedJavaKeyword(CharSequence word) {
    return word != null && getJavaKeyWords().contains(word.toString());
  }

  /**
   * Gets all reserved java keywords.
   *
   * @return An unmodifiable {@link Set} holding all reserved java keywords.
   */
  public static Set<String> getJavaKeyWords() {
    return JAVA_KEYWORDS.computeIfAbsentAndGet(() -> Stream
        .of("abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum", EXTENDS, "final", "finally", "float", "for",
            "goto", "if", IMPLEMENTS, "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", SUPER, "switch", "synchronized", "this",
            "throw", "throws", "transient", "try", "void", "volatile", "while", "false", "null", "true", "yield", "var", "_", "record", "sealed", "permits", "non-sealed")
        .collect(toUnmodifiableSet()));
  }

  /**
   * Tries to box a primitive type to its corresponding complex type.<br>
   * If the given fqn has no boxed value the input is returned.
   *
   * @param fqn
   *          The primitive fqn (e.g. 'int' or 'boolean')
   * @return The corresponding fully qualified complex type fqn (e.g. java.lang.Long) or the input fqn if it could not
   *         be boxed.
   */
  @SuppressWarnings("DuplicatedCode")
  public static String boxPrimitive(CharSequence fqn) {
    if (fqn == null) {
      return null;
    }
    var fqnStr = fqn.toString();
    switch (fqnStr) {
      case _boolean:
        return Boolean;
      case _char:
        return Character;
      case _byte:
        return Byte;
      case _short:
        return Short;
      case _int:
        return Integer;
      case _long:
        return Long;
      case _float:
        return Float;
      case _double:
        return Double;
      case _void:
        return Void;
      default:
        return fqnStr;
    }
  }

  /**
   * Tries to unbox the given name to its corresponding primitive.<br>
   * If the given fqn cannot be unboxed, this method returns the input fqn.
   *
   * @param fqn
   *          The fully qualified complex type name (e.g. java.lang.Long)
   * @return The primitive type name or the input fqn if no primitive exists.
   */
  @SuppressWarnings("DuplicatedCode")
  public static String unboxToPrimitive(CharSequence fqn) {
    if (fqn == null) {
      return null;
    }
    var fqnStr = fqn.toString();
    switch (fqnStr) {
      case Boolean:
        return _boolean;
      case Character:
        return _char;
      case Byte:
        return _byte;
      case Short:
        return _short;
      case Integer:
        return _int;
      case Long:
        return _long;
      case Float:
        return _float;
      case Double:
        return _double;
      case Void:
        return _void;
      default:
        return fqnStr;
    }
  }

  /**
   * Checks if the given type name is a primitive type.
   *
   * @param fqn
   *          The name to check
   * @return {@code true} if the given name specifies a primitive data type. {@code false} otherwise.
   */
  public static boolean isPrimitive(CharSequence fqn) {
    if (fqn == null) {
      return false;
    }
    switch (fqn.toString()) {
      case _boolean:
      case _char:
      case _byte:
      case _short:
      case _int:
      case _long:
      case _float:
      case _double:
      case _void:
        return true;
      default:
        return false;
    }
  }

  /**
   * Gets the default value for the given data type.
   *
   * @param dataType
   *          The data type for which the default return value should be returned.
   * @return A {@link String} holding the default value for the given data type. Returns {@code null} if the given type
   *         is the void type or {@code null}.
   */
  public static String defaultValueOf(CharSequence dataType) {
    if (dataType == null) {
      return null;
    }

    switch (dataType.toString()) {
      case _boolean:
      case Boolean:
        return "false";
      case _byte:
      case Byte:
      case _char:
      case Character:
      case _int:
      case Integer:
      case _short:
      case Short:
        return "0";
      case _double:
      case Double:
        return "0.0";
      case _float:
      case Float:
        return "0.0f";
      case _long:
      case Long:
        return "0L";
      case _void:
        return null;
      default:
        return "null";
    }
  }

  /**
   * Returns a string containing the package of the given name. Returns the empty string if it is not qualified.
   * <p>
   * <b>Examples:</b>
   *
   * <pre>
   * {@code
   * qualifier("java.lang.Object") -> "java.lang"
   * qualifier("Outer.Inner") -> "Outer"
   * qualifier("java.util.List<java.lang.String>") -> "java.util"
   * qualifier("org.eclipse.scout.Blub$Inner$Inner2") -> "org.eclipse.scout"
   * }
   * </pre>
   *
   * @param name
   *          the name. Must not be {@code null}.
   * @return the qualifier prefix, or the empty string if the name contains no dots
   * @throws NullPointerException
   *           if name is null
   */
  public static String qualifier(CharSequence name) {
    var firstGenericStart = indexOf(C_GENERIC_START, name);
    var lastDot = lastIndexOf(C_DOT, name, 0, firstGenericStart == -1 ? name.length() - 1 : firstGenericStart);
    if (lastDot == -1) {
      return "";
    }
    return name.subSequence(0, lastDot).toString();
  }

  /**
   * Gets the last segment of the given fully qualified name.
   * <p>
   * <b>Examples:</b>
   *
   * <pre>
   * {@code
   * simpleName("java.lang.Object") -> "Object"
   * simpleName("java.util.Map$Entry") -> "Entry"
   * simpleName("java.util.List<java.lang.String>") -> "List"
   * }
   * </pre>
   *
   * @param fqn
   *          The fully qualified name for which the simple name should be calculated. Must not be {@code null}.
   * @return The simple name of the given fully qualified name.
   * @throws NullPointerException
   *           If the specified fqn is {@code null}.
   */
  public static String simpleName(CharSequence fqn) {
    var lastSegmentStart = 0;
    CharSequence erasure = erasure(fqn);
    for (var i = erasure.length() - 1; i >= 0; i--) {
      if (erasure.charAt(i) == C_DOT || erasure.charAt(i) == C_DOLLAR) {
        lastSegmentStart = i + 1;
        break;
      }
    }

    if (lastSegmentStart == 0) {
      return erasure.toString();
    }

    return erasure.subSequence(lastSegmentStart, erasure.length()).toString();
  }

  /**
   * Returns an unique identifier for a method with given name and given parameter types. The identifier looks like
   * '{@code methodName(dataTypeOfParam1,dataTypeOfParam2)}'.
   *
   * @param methodName
   *          The method name. Must not be {@code null}.
   * @param paramDataTypes
   *          The parameter data types of the method.
   * @return The created identifier
   */
  public static String createMethodIdentifier(CharSequence methodName, Collection<? extends CharSequence> paramDataTypes) {
    var methodIdBuilder = new StringBuilder(256);
    methodIdBuilder.append(methodName);
    methodIdBuilder.append('(');
    if (paramDataTypes != null && !paramDataTypes.isEmpty()) {
      var iterator = paramDataTypes.iterator();
      methodIdBuilder.append(iterator.next());
      while (iterator.hasNext()) {
        methodIdBuilder.append(C_COMMA);
        methodIdBuilder.append(iterator.next());
      }
    }
    methodIdBuilder.append(')');
    return methodIdBuilder.toString();
  }

  /**
   * Returns an unique identifier for the given {@link Method}. The identifier looks like
   * '{@code methodName(dataTypeOfParam1,dataTypeOfParam2)}'.
   *
   * @param method
   *          The method for which the identifier should be computed. Must not be {@code null}.
   * @return The created identifier
   */
  public static String createMethodIdentifier(Method method) {
    var args = Arrays.stream(method.getParameterTypes())
        .map(Class::getName)
        .collect(toList());
    return createMethodIdentifier(method.getName(), args);
  }

  /**
   * Extracts the type erasure from the given parameterized type. Returns the given type if it is not parameterized.
   * <p>
   * <b>Examples:</b>
   *
   * <pre>
   * {@code
   * erasure("java.util.List<java.lang.String>") -> "java.util.List"
   * erasure("X<List<T>,Map<U,ABC<T>>>.Member<Object>") -> "X.Member"
   * }
   * </pre>
   *
   * @param parameterizedType
   *          the parameterized type. Must not be {@code null}.
   * @return the type erasure of the given type.
   * @throws IllegalArgumentException
   *           if the given type is syntactically incorrect
   */
  public static String erasure(CharSequence parameterizedType) {
    var firstParamIndex = indexOf(C_GENERIC_START, parameterizedType);
    if (firstParamIndex < 0) {
      return parameterizedType.toString();
    }
    return erasure(parameterizedType, firstParamIndex);
  }

  private static String erasure(CharSequence parameterizedType, int firstParamIndex) {
    var result = new StringBuilder(parameterizedType.length());
    result.append(parameterizedType.subSequence(0, firstParamIndex));
    var depth = 1;
    for (var i = firstParamIndex + 1; i < parameterizedType.length(); i++) {
      var c = parameterizedType.charAt(i);
      if (c == C_GENERIC_START) {
        depth++;
      }
      else if (c == C_GENERIC_END) {
        depth--;
      }
      else if (depth < 1) {
        result.append(c);
      }
    }
    return result.toString();
  }

  /**
   * Extracts the type arguments from the given type. Returns an empty list if the type is not parameterized.
   * <p>
   * <b>Examples:</b>
   *
   * <pre>
   * {@code
   * typeArguments("List<T>") -> "T"
   * typeArguments("X<Object>.Member<List<T>,Map<U,ABC<T>>>") -> ["List<T>", "Map<U,ABC<T>>"]
   * }
   * </pre>
   *
   * @param parameterizedType
   *          the parameterized type
   * @return the type arguments
   * @throws IllegalArgumentException
   *           if the type is syntactically incorrect
   */
  public static List<String> typeArguments(CharSequence parameterizedType) {
    var length = parameterizedType.length();
    if (length < 4) {
      // cannot have type arguments
      return emptyList();
    }
    if (parameterizedType.charAt(length - 1) != C_GENERIC_END) {
      return emptyList();
    }

    Deque<String> args = new ArrayDeque<>();
    var depth = 1;
    var end = length - 1;
    for (var pos = end - 1; pos >= 0 && depth > 0; pos--) {
      var curChar = parameterizedType.charAt(pos);
      if (curChar == C_GENERIC_START) {
        depth--;
        if (depth == 0) {
          var arg = subElement(parameterizedType, pos + 1, end);
          args.addFirst(arg);
          end = pos;
        }
      }
      else if (curChar == C_GENERIC_END) {
        depth++;
      }
      else if (depth == 1 && curChar == C_COMMA) {
        var arg = subElement(parameterizedType, pos + 1, end);
        args.addFirst(arg);
        end = pos;
      }
    }
    return new ArrayList<>(args);
  }

  static String subElement(CharSequence src, int start, int end) {
    for (var i = start; i < end; i++) {
      start = i;
      if (src.charAt(i) != C_SPACE) {
        break;
      }
    }
    for (var i = end; i >= start; i--) {
      end = i;
      if (src.charAt(i - 1) != C_SPACE) {
        break;
      }
    }

    return src.subSequence(start, end).toString();
  }

  public static class ReferenceParser {

    private final BiFunction<CharSequence, Boolean, CharSequence> m_handler;

    public ReferenceParser(BiFunction<CharSequence, Boolean, CharSequence> handler) {
      m_handler = Ensure.notNull(handler);
    }

    public String useReference(CharSequence fullyQualifiedName) {
      var result = new StringBuilder(fullyQualifiedName.length());
      useReferenceInternal(fullyQualifiedName, result);
      return result.toString();
    }

    protected void useReferenceInternal(CharSequence ref, StringBuilder result) {
      var isFirst = true;
      var inWildcard = false;
      var depth = 0;
      var lastTypeArgStart = -1;

      for (var i = 0; i < ref.length(); i++) {
        var c = ref.charAt(i);
        switch (c) {
          case C_GENERIC_START:
            if (isFirst) {
              lastTypeArgStart = consumeType(i, 0, ref, depth, result);
              isFirst = false;
            }
            else if (depth > 0) {
              lastTypeArgStart = consumeType(i, lastTypeArgStart, ref, depth, result);
            }
            else {
              lastTypeArgStart = i + 1;
            }
            depth++;
            inWildcard = false;
            result.append(C_GENERIC_START);
            break;
          case C_QUESTION_MARK:
            inWildcard = true;
            break;
          case C_SPACE:
            if (!inWildcard) {
              lastTypeArgStart = consumeType(i, lastTypeArgStart, ref, depth, result);
            }
            break;
          case C_GENERIC_END:
            lastTypeArgStart = consumeType(i, lastTypeArgStart, ref, depth, result);
            depth--;
            inWildcard = false;
            result.append(C_GENERIC_END);
            break;
          case C_COMMA:
            lastTypeArgStart = consumeType(i, lastTypeArgStart, ref, depth, result);
            inWildcard = false;
            result.append(C_COMMA);
            break;
          default:
            if (!isFirst && depth == 0) {
              result.append(c);
            }
            break;
        }
      }

      if (isFirst) {
        consumeType(ref.length(), 0, ref, depth, result);
      }
    }

    protected int consumeType(int end, int start, CharSequence src, int depth, StringBuilder result) {
      if (end == start) {
        // empty string, nothing to do
        return end + 1;
      }

      var fqnStart = consumeWildcard(start, src, result);
      if (fqnStart == end) {
        return end + 1;
      }

      var arrayStart = indexOf(C_ARRAY, src, fqnStart, end);
      var isArray = arrayStart > fqnStart;

      var fqn = src.subSequence(fqnStart, isArray ? arrayStart : end);
      result.append(handler().apply(fqn, depth > 0));

      if (isArray) {
        result.append(src, arrayStart, end);
      }
      return end + 1;
    }

    protected static int consumeWildcard(int start, CharSequence src, StringBuilder result) {
      if (src.charAt(start) != C_QUESTION_MARK) {
        return start;
      }

      for (var wildcardMarker : WILDCARD_MARKERS) {
        if (fragmentEquals(wildcardMarker, src, start + 1)) {
          result.append(C_QUESTION_MARK);
          if (wildcardMarker[0] != C_SPACE) {
            result.append(C_SPACE);
          }
          result.append(wildcardMarker);
          return start + wildcardMarker.length + 1;
        }
      }

      result.append(C_QUESTION_MARK); // wildcard only without bounds
      return start + 1;
    }

    @SuppressWarnings("squid:S881")
    protected static boolean fragmentEquals(char[] fragment, CharSequence name, int startIndex) {
      var max = fragment.length;
      if (name.length() < max + startIndex) {
        return false;
      }
      for (var i = max; --i >= 0;) {
        if (fragment[i] != name.charAt(i + startIndex)) {
          return false;
        }
      }
      return true;
    }

    public BiFunction<CharSequence, Boolean, CharSequence> handler() {
      return m_handler;
    }
  }
}

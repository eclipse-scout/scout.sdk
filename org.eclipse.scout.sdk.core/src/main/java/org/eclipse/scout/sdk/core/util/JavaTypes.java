/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.util;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.eclipse.scout.sdk.core.util.Strings.indexOf;
import static org.eclipse.scout.sdk.core.util.Strings.lastIndexOf;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.model.spi.MethodParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;

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
   * The Java lambda arrow. Value is {@code ->}
   */
  public static final String LAMBDA_ARROW = "->";
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
  /**
   * The array marker for one array dimension: {@code []}. Use {@link #arrayMarker(int)} to get a marker for a certain
   * dimension.
   */
  private static final String ARRAY_MARKER = "[]";

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
    return switch (fqnStr) {
      case _boolean -> Boolean;
      case _char -> Character;
      case _byte -> Byte;
      case _short -> Short;
      case _int -> Integer;
      case _long -> Long;
      case _float -> Float;
      case _double -> Double;
      case _void -> Void;
      default -> fqnStr;
    };
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
    return switch (fqnStr) {
      case Boolean -> _boolean;
      case Character -> _char;
      case Byte -> _byte;
      case Short -> _short;
      case Integer -> _int;
      case Long -> _long;
      case Float -> _float;
      case Double -> _double;
      case Void -> _void;
      default -> fqnStr;
    };
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
    return switch (fqn.toString()) {
      case _boolean, _char, _byte, _short, _int, _long, _float, _double, _void -> true;
      default -> false;
    };
  }

  /**
   * Checks if the given type name is an array type (any array dimension returns {@code true}).
   *
   * @param fqn
   *          The name to check
   * @return {@code true} if the given reference is an array type (e.g. boolean[] or java.util.List[][]).
   */
  public static boolean isArray(CharSequence fqn) {
    return fqn != null
        && fqn.length() > 0
        && fqn.charAt(fqn.length() - 1) == ']';
  }

  /**
   * Checks if the given type is the wildcard type (?).
   *
   * @param fqn
   *          The type name to check.
   * @return {@code true} if the given reference is the wildcard type.
   */
  public static boolean isWildcard(CharSequence fqn) {
    return fqn != null
        && fqn.length() == 1
        && fqn.charAt(0) == C_QUESTION_MARK;
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

    return switch (dataType.toString()) {
      case _boolean, Boolean -> "false";
      case _byte, Byte, _char, Character, _int, Integer, _short, Short -> "0";
      case _double, Double -> "0.0";
      case _float, Float -> "0.0f";
      case _long, Long -> "0L";
      case _void -> null;
      default -> "null";
    };
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
   * qualifier("org.eclipse.scout.TopLevelClass$Inner$Inner2") -> "org.eclipse.scout"
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
   * Returns a unique identifier for a method with given name and given parameter types. The identifier looks like
   * '{@code methodName(dataTypeOfParam1,dataTypeOfParam2)}'.
   * 
   * @param method
   *          The method for which the identifier should be computed. Must not be {@code null}.
   * @return The created identifier
   */
  public static String createMethodIdentifier(MethodSpi method) {
    var paramTypeNames = method.getParameters().stream()
        .map(MethodParameterSpi::getDataType)
        .map(TypeSpi::getName)
        .collect(toList());
    return JavaTypes.createMethodIdentifier(method.getElementName(), paramTypeNames);
  }

  /**
   * Returns a unique identifier for a method with given name and given parameter types. The identifier looks like
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
   * Returns a unique identifier for the given {@link Method}. The identifier looks like
   * '{@code methodName(dataTypeOfParam1,dataTypeOfParam2)}'. Only the type erasure is used.
   *
   * @param method
   *          The method for which the identifier should be computed. Must not be {@code null}.
   * @return The created identifier containing the type erasure only.
   */
  public static String createMethodIdentifier(Method method) {
    return createMethodIdentifier(method, false);
  }

  /**
   * Returns a unique identifier for the given {@link Method}. The identifier looks like
   * '{@code methodName(dataTypeOfParam1,dataTypeOfParam2)}'.
   *
   * @param method
   *          The method for which the identifier should be computed. Must not be {@code null}.
   * @param includeTypeArguments
   *          If {@code true} the created identifier includes the type arguments. If {@code false} only the type erasure
   *          is used.
   * @return The created identifier
   */
  public static String createMethodIdentifier(Executable method, boolean includeTypeArguments) {
    var paramTypes = includeTypeArguments ? method.getGenericParameterTypes() : method.getParameterTypes();
    var args = Arrays.stream(paramTypes)
        .map(Type::getTypeName)
        .map(name -> name.replace(" ", ""))
        .collect(toList());
    return createMethodIdentifier(method.getName(), args);
  }

  /**
   * @return An array marker ({@code []}) for a one dimension array.
   */
  public static String arrayMarker() {
    return arrayMarker(1);
  }

  /**
   * @param dimension
   *          The number of array dimensions.
   * @return An array marker ({@code []}) for the given dimension number.
   */
  public static String arrayMarker(int dimension) {
    if (dimension < 1) {
      return "";
    }
    return ARRAY_MARKER.repeat(dimension);
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

    private final BiFunction<CharSequence, Integer, CharSequence> m_handler;

    /**
     * @param handler
     *          The {@link BiFunction} to consume types found in a reference. The first parameter contains the fully
     *          qualified name of the type, the second the type-argument depth (zero means it is no type argument). The
     *          result of the {@link BiFunction} is the value collected to the reference.
     */
    public ReferenceParser(BiFunction<CharSequence, Integer, CharSequence> handler) {
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
      result.append(handler().apply(fqn, depth));

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

    /**
     * @return The {@link BiFunction} to consume a type. The first parameter contains the fully qualified name of the
     *         type, the second the type-argument depth (zero means it is no type argument). The result of the
     *         {@link BiFunction} is the value collected to the reference.
     */
    public BiFunction<CharSequence, Integer, CharSequence> handler() {
      return m_handler;
    }
  }
}

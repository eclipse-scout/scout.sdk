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
package org.eclipse.scout.sdk.core.testing;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.sdk.core.imports.IImportCollector;
import org.eclipse.scout.sdk.core.imports.IImportValidator;
import org.eclipse.scout.sdk.core.imports.ImportCollector;
import org.eclipse.scout.sdk.core.imports.ImportValidator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.junit.jupiter.api.Assertions;

/**
 * <h3>{@link ApiTestGenerator}</h3>
 *
 * @since 3.10.0 2013-08-26
 */
public class ApiTestGenerator {
  public static final String NL = "\n";

  private final IType m_element;
  private final Set<String> m_usedMemberNames;

  public ApiTestGenerator(IType element) {
    m_element = element;
    m_usedMemberNames = new HashSet<>();
  }

  protected static void buildMethod(IMethod method, String methodVarName, StringBuilder source) {
    List<IMethodParameter> parameterSignatures = method.parameters().stream().collect(toList());
    if (!parameterSignatures.isEmpty()) {
      for (int i = 0; i < parameterSignatures.size(); i++) {
        source.append('"').append(parameterSignatures.get(i).dataType().reference()).append('"');
        if (i < parameterSignatures.size() - 1) {
          source.append(", ");
        }
      }
    }
    source.append("});").append(NL);
    if (method.isConstructor()) {
      source.append("assertTrue(").append(methodVarName).append(".isConstructor());").append(NL);
    }

    method.returnType()
        .map(IType::reference)
        .ifPresent(sig -> source.append("assertMethodReturnType(")
            .append(methodVarName)
            .append(", \"")
            .append(sig)
            .append("\");")
            .append(NL));

    createAnnotationsAsserts(method, source, methodVarName);
  }

  protected static void buildField(IField field, String fieldVarName, StringBuilder source, String flagsRef) {
    source.append("assertHasFlags(").append(fieldVarName).append(", ").append(getFlagsSource(field.flags(), flagsRef)).append(");").append(NL);
    source.append("assertFieldType(").append(fieldVarName).append(", ").append('"').append(field.dataType().reference()).append("\");").append(NL);
    createAnnotationsAsserts(field, source, fieldVarName);
  }

  protected static String getFlagsSource(int flags, String flagsRef) {
    return Arrays.stream(Flags.class.getDeclaredFields())
        .filter(f -> f.getType() == int.class)
        .filter(f -> (intValueOf(f) & flags) != 0)
        .map(f -> flagsRef + JavaTypes.C_DOT + f.getName())
        .collect(joining(" | "));
  }

  protected static int intValueOf(Field f) {
    try {
      return f.getInt(null);
    }
    catch (IllegalArgumentException | IllegalAccessException e) {
      throw new SdkException(e);
    }
  }

  public static void createAnnotationsAsserts(IAnnotatable annotatable, StringBuilder source, String annotatableRef) {
    List<IAnnotation> annotations = annotatable.annotations().stream().collect(toList());
    source.append("assertEquals(").append(annotations.size()).append(", ").append(annotatableRef).append(".annotations().stream().count(), \"annotation count\");").append(NL);
    for (IAnnotation a : annotations) {
      String annotationSignature = a.type().reference();
      source.append("assertAnnotation(").append(annotatableRef).append(", \"").append(annotationSignature).append("\");").append(NL);
    }
  }

  public String buildSource() {
    IImportCollector collector = new ImportCollector();
    IImportValidator validator = new ImportValidator(collector);
    StringBuilder sourceBuilder = new StringBuilder();
    String typeVarName = getMemberName(m_element.elementName());
    String iTypeRef = validator.useReference(IType.class.getName());

    sourceBuilder.append("/**").append(NL);
    sourceBuilder.append("* @Generated with ").append(getClass().getName()).append(NL);
    sourceBuilder.append("*/").append(NL);

    sourceBuilder.append("private static void testApiOf").append(m_element.elementName()).append('(').append("IType ").append(typeVarName).append(") {").append(NL);
    buildType(m_element, typeVarName, sourceBuilder, validator, iTypeRef);
    sourceBuilder.append('}');

    collector.addStaticImport(SdkAssertions.class.getName() + ".assertAnnotation");
    collector.addStaticImport(SdkAssertions.class.getName() + ".assertFieldExist");
    collector.addStaticImport(SdkAssertions.class.getName() + ".assertFieldType");
    collector.addStaticImport(SdkAssertions.class.getName() + ".assertHasFlags");
    collector.addStaticImport(SdkAssertions.class.getName() + ".assertHasSuperClass");
    collector.addStaticImport(SdkAssertions.class.getName() + ".assertHasSuperIntefaceSignatures");
    collector.addStaticImport(SdkAssertions.class.getName() + ".assertMethodExist");
    collector.addStaticImport(SdkAssertions.class.getName() + ".assertMethodReturnType");
    collector.addStaticImport(SdkAssertions.class.getName() + ".assertNoCompileErrors");
    collector.addStaticImport(SdkAssertions.class.getName() + ".assertTypeExists");
    collector.addStaticImport(Assertions.class.getName() + ".assertArrayEquals");
    collector.addStaticImport(Assertions.class.getName() + ".assertEquals");

    StringBuilder result = new StringBuilder();
    collector.createImportDeclarations()
        .forEach(i -> result.append(i).append(NL));
    result.append(NL);
    result.append(sourceBuilder);
    return result.toString();
  }

  protected void buildType(IType type, String typeVarName, StringBuilder source, IImportValidator validator, String iTypeRef) {
    String flagsRef = validator.useReference(Flags.class.getName());

    source.append("assertHasFlags(").append(typeVarName).append(", ").append(getFlagsSource(type.flags(), flagsRef)).append(");").append(NL);

    // super type
    type.superClass()
        .map(IType::reference)
        .ifPresent(sig -> source
            .append("assertHasSuperClass(")
            .append(typeVarName).append(", \"")
            .append(sig)
            .append("\");")
            .append(NL));

    // interfaces
    List<IType> interfaces = type.superInterfaces().collect(toList());
    if (!interfaces.isEmpty()) {
      source.append("assertHasSuperIntefaceSignatures(").append(typeVarName).append(", new String[]{");
      for (int i = 0; i < interfaces.size(); i++) {
        source.append('"').append(interfaces.get(i).reference()).append('"');
        if (i < interfaces.size() - 1) {
          source.append(", ");
        }
      }
      source.append("});").append(NL);
    }
    createAnnotationsAsserts(type, source, typeVarName);
    source.append(NL);

    // fields
    source.append("// fields of ").append(type.elementName()).append(NL);
    String iFieldRef = validator.useReference(IField.class.getName());
    List<IField> fields = type.fields().stream().collect(toList());
    source.append("assertEquals(").append(fields.size()).append(", ").append(typeVarName).append(".fields().stream().count(), \"field count of '").append(type.name()).append("'\");").append(NL);
    for (IField f : fields) {
      String fieldVarName = getMemberName(f.elementName());
      source.append(iFieldRef).append(JavaTypes.C_SPACE).append(fieldVarName).append(" = ").append("assertFieldExist(").append(typeVarName).append(", \"").append(f.elementName()).append("\");").append(NL);
      buildField(f, fieldVarName, source, flagsRef);
    }
    source.append(NL);

    // methods
    String iMethodRef = validator.useReference(IMethod.class.getName());
    List<IMethod> methods = type.methods().stream().collect(toList());
    source.append("assertEquals(").append(methods.size()).append(", ").append(typeVarName).append(".methods().stream().count(), \"method count of '").append(type.name()).append("'\");").append(NL);
    for (IMethod method : methods) {
      String methodVarName = getMemberName(method.elementName());
      source.append(iMethodRef).append(JavaTypes.C_SPACE).append(methodVarName).append(" = ").append("assertMethodExist(").append(typeVarName).append(", \"").append(method.elementName()).append("\", new String[]{");
      buildMethod(method, methodVarName, source);
    }
    source.append(NL);

    // inner types
    List<IType> innerTypes = type.innerTypes().stream().collect(toList());
    source.append("assertEquals(").append(innerTypes.size()).append(", ")
        .append(typeVarName).append(".innerTypes().stream().count(), \"inner types count of '").append(type.elementName()).append("'\");").append(NL);
    for (IType innerType : innerTypes) {
      String innerTypeVarName = getMemberName(innerType.elementName());
      source.append("// type ").append(innerType.elementName()).append(NL);
      source.append(iTypeRef).append(JavaTypes.C_SPACE).append(innerTypeVarName).append(" = ");
      source.append("assertTypeExists(").append(typeVarName).append(", \"").append(innerType.elementName()).append("\");").append(NL);
      buildType(innerType, innerTypeVarName, source, validator, iTypeRef);
    }
  }

  private String getMemberName(String e) {
    String memberName = Introspector.decapitalize(e);
    if (m_usedMemberNames.contains(memberName)) {
      int counter = 1;
      String workingName = memberName + counter;
      while (m_usedMemberNames.contains(workingName)) {
        counter++;
        workingName = memberName + counter;
      }
      memberName = workingName;
    }
    m_usedMemberNames.add(memberName);
    return memberName;
  }

}

/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.operation.form.formdata;

import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.util.SourceFormatOperation;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;

/**
 * <h3>{@link FormDataUtility}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 08.03.2011
 */
public class FormDataUtility {

  private static final HashSet<String> keyWords = new HashSet<String>();
  private static final Pattern CONSTANT_NAME_PATTERN = Pattern.compile("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
  static {
    keyWords.add("abstract");
    keyWords.add("assert");
    keyWords.add("boolean");
    keyWords.add("break");
    keyWords.add("byte");
    keyWords.add("case");
    keyWords.add("catch");
    keyWords.add("char");
    keyWords.add("class");
    keyWords.add("const");
    keyWords.add("continue");
    keyWords.add("default");
    keyWords.add("do");
    keyWords.add("double");
    keyWords.add("else");
    keyWords.add("enum");
    keyWords.add("extends");
    keyWords.add("final");
    keyWords.add("finally");
    keyWords.add("float");
    keyWords.add("for");
    keyWords.add("goto");
    keyWords.add("if");
    keyWords.add("implements");
    keyWords.add("import");
    keyWords.add("instanceof");
    keyWords.add("int");
    keyWords.add("interface");
    keyWords.add("long");
    keyWords.add("native");
    keyWords.add("new");
    keyWords.add("package");
    keyWords.add("private");
    keyWords.add("protected");
    keyWords.add("public");
    keyWords.add("return");
    keyWords.add("short");
    keyWords.add("static");
    keyWords.add("strictfp");
    keyWords.add("super");
    keyWords.add("switch");
    keyWords.add("synchronized");
    keyWords.add("this");
    keyWords.add("throw");
    keyWords.add("throws");
    keyWords.add("transient");
    keyWords.add("try");
    keyWords.add("void");
    keyWords.add("volatile");
    keyWords.add("while");
  }

  public static ITypeSourceBuilder getPrimaryTypeFormDataSourceBuilder(String superTypeSignature, IType formField, ITypeHierarchy hierarchy) {
    ITypeSourceBuilder builder = null;
    String typeErasure = Signature.getTypeErasure(superTypeSignature);

    if (Signature.getSignatureSimpleName(typeErasure).equals(Signature.getSimpleName(RuntimeClasses.AbstractTableFieldData))) {
      builder = new TableFieldSourceBuilder(formField, hierarchy);
    }
    else {
      builder = new CompositePrimaryTypeSourceBuilder(formField, hierarchy);
    }
    return builder;
  }

  public static ITypeSourceBuilder getInnerTypeFormDataSourceBuilder(String superTypeSignature, IType formField, ITypeHierarchy hierarchy) {
    ITypeSourceBuilder builder = null;
    String typeErasure = Signature.getTypeErasure(superTypeSignature);
    if (Signature.toString(typeErasure).equals(RuntimeClasses.AbstractTableFieldData)) {
      builder = new TableFieldSourceBuilder(formField, hierarchy);
    }
    else {
      builder = new SourceBuilderWithProperties(formField);
    }
    return builder;
  }

  public static String getFormDataSuperTypeSignature(FormDataAnnotation annotation, IType type, ITypeHierarchy hierarchy) {
    String superTypeSignature = annotation.getSuperTypeSignature();
    if (annotation.getGenericOrdinal() >= 0) {
      IType superType = TypeUtility.getTypeBySignature(superTypeSignature);
      if (TypeUtility.isGenericType(superType)) {
        try {
          String genericTypeSig = computeFormFieldGenericType(type, hierarchy);
          if (genericTypeSig != null) {
            superTypeSignature = superTypeSignature.replaceAll("\\;$", "<" + genericTypeSig + ">;");
          }
        }
        catch (JavaModelException e) {
          ScoutSdk.logError("could not find generic type for form data of type '" + type.getFullyQualifiedName() + "'.");
        }
      }

    }
    return superTypeSignature;
  }

  public static String getFieldNameWithoutSuffix(String s) {
    if (s.endsWith("Field")) {
      s = s.replaceAll("Field$", "");
    }
    else if (s.endsWith("Button")) {
      s = s.replaceAll("Button$", "");
    }
    else if (s.endsWith("Column")) {
      s = s.replaceAll("Column$", "");
    }
    return s;
  }

  public static String getBeanName(String name, boolean startWithUpperCase) {
    StringBuilder builder = new StringBuilder();
    if (!StringUtility.isNullOrEmpty(name)) {
      if (startWithUpperCase) {
        builder.append(Character.toUpperCase(name.charAt(0)));
      }
      else {
        builder.append(Character.toLowerCase(name.charAt(0)));
      }
      if (name.length() > 1) {
        builder.append(name.substring(1));
      }
    }
    return builder.toString();
  }

  public static String getConstantName(String name) {
    String[] words = CONSTANT_NAME_PATTERN.split(name);
    return StringUtility.join("_", words).toUpperCase();
  }

  public static String unboxPrimitiveSignature(String signature) {
    if (Signature.getTypeSignatureKind(signature) == Signature.BASE_TYPE_SIGNATURE) {
      if (Signature.SIG_BOOLEAN.equals(signature)) {
        signature = Signature.createTypeSignature(Boolean.class.getName(), true);
      }
      else if (Signature.SIG_BYTE.equals(signature)) {
        signature = Signature.createTypeSignature(Byte.class.getName(), true);
      }
      else if (Signature.SIG_CHAR.equals(signature)) {
        signature = Signature.createTypeSignature(Character.class.getName(), true);
      }
      else if (Signature.SIG_DOUBLE.equals(signature)) {
        signature = Signature.createTypeSignature(Double.class.getName(), true);
      }
      else if (Signature.SIG_FLOAT.equals(signature)) {
        signature = Signature.createTypeSignature(Float.class.getName(), true);
      }
      else if (Signature.SIG_INT.equals(signature)) {
        signature = Signature.createTypeSignature(Integer.class.getName(), true);
      }
      else if (Signature.SIG_LONG.equals(signature)) {
        signature = Signature.createTypeSignature(Long.class.getName(), true);
      }
      else if (Signature.SIG_SHORT.equals(signature)) {
        signature = Signature.createTypeSignature(Short.class.getName(), true);
      }
    }
    return signature;
  }

  public static String getValidMethodParameterName(String parameterName) {
    if (keyWords.contains(parameterName.toLowerCase())) {
      return parameterName + "Value";
    }
    return parameterName;
  }

  public static String createCompilationUnitSource(ITypeSourceBuilder builder, String packageName, IJavaProject project, IProgressMonitor monitor) throws CoreException, BadLocationException {
    IImportValidator validator = new SourceBuilderImportValidator(builder, packageName);
    String typeSource = builder.createSource(validator);
    Document bodyDocument = new Document(typeSource);
    MultiTextEdit multiEdit = new MultiTextEdit();
    multiEdit.addChild(new InsertEdit(0, "package " + packageName + ";" + bodyDocument.getDefaultLineDelimiter()));
    for (String imp : validator.getImportsToCreate()) {
      multiEdit.addChild(new InsertEdit(0, "import " + imp + ";" + bodyDocument.getDefaultLineDelimiter()));
    }
    multiEdit.apply(bodyDocument);

    SourceFormatOperation formatOp = new SourceFormatOperation(project, bodyDocument, null);
    formatOp.run(monitor, null);
    return formatOp.getDocument().get();
  }

  public static String getTypeSource(String icuSource, String formDataName) throws IOException {
    Matcher m = Pattern.compile("(public|private|protected)\\s*(abstract)?\\s*class\\s*" + formDataName, Pattern.MULTILINE).matcher(icuSource);
    if (m.find()) {
      return icuSource.substring(m.start());
    }
    else {
      ScoutSdk.logError("could not find type start of '" + formDataName + "'");
      return null;
    }
  }

  public static String computeFormFieldGenericType(IType type, ITypeHierarchy formFieldHierarchy) throws JavaModelException {
    if (type == null || type.getFullyQualifiedName().equals(Object.class.getName())) {
      return null;
    }
    IType superType = formFieldHierarchy.getSuperclass(type);
    if (TypeUtility.exists(superType)) {
      if (TypeUtility.isGenericType(superType)) {
        // compute generic parameter type by merging all super type generic parameter declarations
        //List<GenericSignatureMapping> signatureMapping = new ArrayList<GenericSignatureMapping>();
        IType currentType = type;
        IType currentSuperType = superType;
        while (currentSuperType != null) {
          if (TypeUtility.isGenericType(currentSuperType)) {
            //String superTypeGenericParameterName = currentSuperType.getTypeParameters()[0].getElementName();
            String currentSuperTypeSig = currentType.getSuperclassTypeSignature();
            String[] typeArgs = Signature.getTypeArguments(currentSuperTypeSig);
            if (typeArgs.length < 1) {
              // if the class has no generic type defined, use java.lang.Object as type for the formdata
              typeArgs = new String[]{Signature.C_RESOLVED + Object.class.getName() + Signature.C_SEMICOLON};
            }
            return SignatureUtility.getResolvedSignature(typeArgs[0], currentType);
            //return SignatureUtility.getResolvedSignature(superTypeGenericParameterSignature, type);
            /*signatureMapping.add(new GenericSignatureMapping(superTypeGenericParameterName, superTypeGenericParameterSignature));
            currentType = currentSuperType;
            currentSuperType = formFieldHierarchy.getSuperclass(currentSuperType);*/
          }
          else {
            break;
          }
        }
        return null;
        /*if (signatureMapping.size() < 1) {
          return null;
        }
        else {
          String signature = signatureMapping.get(0).getSuperTypeGenericParameterSignature();
          /*for (int i = 1; i < signatureMapping.size(); i++) {
            String replacement = signatureMapping.get(i).getSuperTypeGenericParameterSignature();
            replacement = replacement.substring(0, replacement.length() - 1);
            signature = signature.replaceAll("[T,L,Q]" + signatureMapping.get(i).getSuperTypeGenericParameterName(), replacement);
          }
          return SignatureUtility.getResolvedSignature(signature, type);
        }*/
      }
      else {
        return computeFormFieldGenericType(superType, formFieldHierarchy);
      }
    }
    else {
      return null;
    }
  }

  private static class GenericSignatureMapping {
    private final String m_superTypeGenericParameterName;
    private final String m_superTypeGenericParameterSignature;

    public GenericSignatureMapping(String superTypeGenericParameterName, String superTypeGenericParameterSignature) {
      m_superTypeGenericParameterName = superTypeGenericParameterName;
      m_superTypeGenericParameterSignature = superTypeGenericParameterSignature;
    }

    public String getSuperTypeGenericParameterName() {
      return m_superTypeGenericParameterName;
    }

    public String getSuperTypeGenericParameterSignature() {
      return m_superTypeGenericParameterSignature;
    }
  }

}

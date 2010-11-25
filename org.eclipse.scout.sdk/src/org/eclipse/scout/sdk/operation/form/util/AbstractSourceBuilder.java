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
package org.eclipse.scout.sdk.operation.form.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.jdt.signature.SimpleImportValidator;
import org.eclipse.scout.sdk.util.ScoutSignature;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.workspace.member.IPropertyBean;
import org.eclipse.scout.sdk.workspace.type.PropertyBeanComparators;
import org.eclipse.scout.sdk.workspace.type.PropertyBeanFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

public abstract class AbstractSourceBuilder implements ISourceBuilder {
  private final String m_superTypeSignature;
  private final String m_simpleTypeName;

  private final IImportValidator m_importValidator;
  private final int m_typeFlags;
  private List<String> m_fieldList;

  private List<String> m_propertyMethodList;
  private List<String> m_propertyTypeList;
  private List<String> m_fieldGetterMethodList;
  private List<String> m_innerClassList;

  public AbstractSourceBuilder(String simpleTypeName, String superTypeSignature, int typeFlags, IImportValidator importValidator) {
    m_simpleTypeName = simpleTypeName;
    m_typeFlags = typeFlags;
    m_superTypeSignature = superTypeSignature;
    if (importValidator == null) {
      importValidator = new SimpleImportValidator();
    }
    m_importValidator = importValidator;
    m_fieldList = new ArrayList<String>();
    m_propertyMethodList = new ArrayList<String>();
    m_propertyTypeList = new ArrayList<String>();
    m_fieldGetterMethodList = new ArrayList<String>();
    m_innerClassList = new ArrayList<String>();
  }

  public String getSimpleTypeName() {
    return m_simpleTypeName;
  }

  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }

  public IImportValidator getImportValidator() {
    return m_importValidator;
  }

  public int getTypeFlags() {
    return m_typeFlags;
  }

  protected boolean isAbstractType() {
    return (getTypeFlags() & Flags.AccAbstract) != 0;
  }

  public void addField(String field) {
    m_fieldList.add(field);
  }

  public String[] getAllFields() {
    return m_fieldList.toArray(new String[m_fieldList.size()]);
  }

  public void addPropertyMethod(String method) {
    m_propertyMethodList.add(method);
  }

  public String[] getAllPropertyMethods() {
    return m_propertyMethodList.toArray(new String[m_propertyMethodList.size()]);
  }

  public void addFieldGetterMethod(String method) {
    m_fieldGetterMethodList.add(method);
  }

  public String[] getAllFieldGetterMethods() {
    return m_fieldGetterMethodList.toArray(new String[m_fieldGetterMethodList.size()]);
  }

  public String[] getAllPropertyTypes() {
    return m_propertyTypeList.toArray(new String[m_propertyTypeList.size()]);
  }

  public void addInnerClass(String classSource) {
    m_innerClassList.add(classSource);
  }

  public String[] getAllInnerClasses() {
    return m_innerClassList.toArray(new String[m_innerClassList.size()]);
  }

  /**
   * @param outputFieldBuf
   *          and outputMethodBuf if non-null then the getters/setters are added outputBuf, otherwise they are added to
   *          the sourceBuilder
   *          intended: null for form beanProperties, non-null for field beanProperties
   */
  protected void addBeanPropertiesFrom(IType type, boolean includeInherited) throws CoreException {
    // find propertyMethods
    IPropertyBean[] beanPropertyDescriptors = TypeUtility.getPropertyBeans(type, PropertyBeanFilters.getFormDataPropertyFilter(), PropertyBeanComparators.getNameComparator());
    if (beanPropertyDescriptors != null) {
      for (IPropertyBean desc : beanPropertyDescriptors) {
        if (includeInherited || desc.getReadMethod() != null || desc.getWriteMethod() != null) {
          String beanTypeRef = ScoutSignature.getTypeReference(unboxPrimitiveSignature(desc.getBeanSignature()), desc.getDeclaringType(), getImportValidator());
          String beanName = FormDataUtility.getBeanName(desc.getBeanName(), true);
          String propName = beanName + "Property";
          StringBuilder propertyBuilder = new StringBuilder();
          propertyBuilder.append("public class");
          propertyBuilder.append(" " + propName);
          propertyBuilder.append(" extends " + getImportValidator().getSimpleTypeRef(Signature.createTypeSignature(RuntimeClasses.AbstractPropertyData, true)));
          if (!StringUtility.isNullOrEmpty(beanTypeRef)) {
            propertyBuilder.append("<" + beanTypeRef + ">");
          }
          propertyBuilder.append(" {\n");
          propertyBuilder.append("private static final long serialVersionUID = 1L;\n");
          propertyBuilder.append("}");
          m_propertyTypeList.add(propertyBuilder.toString());

          StringBuilder propertyGetterBuilder = new StringBuilder();
          propertyGetterBuilder.append("public " + propName + " get" + propName + "(){\n");
          propertyGetterBuilder.append("return getPropertyByClass(" + propName + ".class);\n");
          propertyGetterBuilder.append("}");
          m_propertyMethodList.add(propertyGetterBuilder.toString());

          // access methods
          String accessTypeRef = ScoutSignature.getTypeReference(desc.getBeanSignature(), desc.getDeclaringType(), getImportValidator());
          StringBuilder accessGetter = new StringBuilder();
          accessGetter.append("/**\n* access method for property " + beanName + ".\n*/");
          accessGetter.append("public " + accessTypeRef);
          if (Signature.SIG_BOOLEAN.equals(desc.getBeanSignature())) {
            accessGetter.append(" is");
          }
          else {
            accessGetter.append(" get");
          }
          accessGetter.append(beanName + "() {\n");
          if (Signature.getTypeSignatureKind(desc.getBeanSignature()) == Signature.BASE_TYPE_SIGNATURE) {
            String defaultValue = getPrimitiveDefaultValue(desc.getBeanSignature());
            accessGetter.append("return get" + propName + "().getValue() == null ? " + defaultValue + " : get" + propName + "().getValue();\n");
          }
          else {
            accessGetter.append("return get" + propName + "().getValue();\n");
          }
          accessGetter.append("}");
          m_propertyMethodList.add(accessGetter.toString());

          String parameterName = FormDataUtility.getBeanName(desc.getBeanName(), false);
          StringBuilder accessSetter = new StringBuilder();
          accessSetter.append("/**\n* access method for property " + beanName + ".\n*/");
          accessSetter.append("public void set" + beanName + "(" + accessTypeRef + " " + parameterName + ") {\n");
          accessSetter.append("get" + propName + "().setValue(" + parameterName + ");\n");
          accessSetter.append("}");
          m_propertyMethodList.add(accessSetter.toString());
        }
      }
    }
  }

  private String getPrimitiveDefaultValue(String signature) {
    String defaultValue = null;
    if (Signature.getTypeSignatureKind(signature) == Signature.BASE_TYPE_SIGNATURE) {
      if (Signature.SIG_BOOLEAN.equals(signature)) {
        defaultValue = "false";
      }
      else if (Signature.SIG_BYTE.equals(signature)) {
        defaultValue = "0";
      }
      else if (Signature.SIG_CHAR.equals(signature)) {
        defaultValue = "'\u0000'";
      }
      else if (Signature.SIG_DOUBLE.equals(signature)) {
        defaultValue = "0d";
      }
      else if (Signature.SIG_FLOAT.equals(signature)) {
        defaultValue = "0f";
      }
      else if (Signature.SIG_INT.equals(signature)) {
        defaultValue = "0";
      }
      else if (Signature.SIG_LONG.equals(signature)) {
        defaultValue = "0L";
      }
      else if (Signature.SIG_SHORT.equals(signature)) {
        defaultValue = "0";
      }
    }
    return defaultValue;
  }

  private String unboxPrimitiveSignature(String signature) {
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

  protected String createDocumentText() {
    StringBuilder buf = new StringBuilder();
    // imports
    // buf.append("#IMPORT_DECLARATIONS#\n");
    // for(String imp : getImportValidator().getImportsToCreate()){
    // buf.append("import "+imp+";\n");
    // }
    // buf.append("\n");
    // java doc
    buf.append("/** \n");
    buf.append("  * This class is auto generated and will be refreshed automatically.\n");
    buf.append("  */\n");

    buf.append("public" + (isAbstractType() ? " abstract" : "") + " class " + getSimpleTypeName());
    if (!StringUtility.isNullOrEmpty(getSuperTypeSignature())) {
      buf.append(" extends " + ScoutSdkUtility.getSimpleTypeRefName(getSuperTypeSignature(), getImportValidator()));
    }
    buf.append("{\n");

    buf.append(ScoutIdeProperties.TAB + "private static final long serialVersionUID=1L;\n");
    //
    String[] a = getAllFields();
    if (a.length > 0) {
      buf.append("\n");
      for (int i = 0; i < a.length; i++) {
        if (i > 0) buf.append("\n");
        buf.append(FormDataUtility.indent(a[i]));
      }
      buf.append("\n");
    }
    buf.append("\n");
    //
    buf.append(ScoutIdeProperties.TAB + "public " + getSimpleTypeName() + "(){}\n");
    //
    a = getAllPropertyMethods();
    if (a.length > 0) {
      buf.append("\n");
      for (int i = 0; i < a.length; i++) {
        if (i > 0) buf.append("\n\n");
        buf.append(FormDataUtility.indent(a[i]));
      }
//      buf.append("\n");
    }
    //
    a = getAllFieldGetterMethods();
    if (a.length > 0) {
      buf.append("\n");
      for (int i = 0; i < a.length; i++) {
        if (i > 0) buf.append("\n\n");
        buf.append(FormDataUtility.indent(a[i]));
      }
//      buf.append("\n");
    }

    a = getAllPropertyTypes();
    if (a.length > 0) {
      buf.append("\n");
      for (int i = 0; i < a.length; i++) {
        if (i > 0) buf.append("\n\n");
        buf.append(FormDataUtility.indent(a[i]));
      }
//      buf.append("\n");
    }
    //
    a = getAllInnerClasses();
    if (a.length > 0) {
      buf.append("\n");
      for (int i = 0; i < a.length; i++) {
        if (i > 0) buf.append("\n\n");
        buf.append(FormDataUtility.indent(a[i]));
      }
//      buf.append("\n");
    }
    //
    buf.append("}");

    return buf.toString();
  }

  public Document createDocument() {
    String text = createDocumentText() + "\n";
    // imports
    // StringBuilder importsBuilder = new StringBuilder();
    // for(String fqImp : getImportValidator().getImportsToCreate()){
    // if(!fqImp.matches("^java\\.lang\\.[^.]*$")){
    // importsBuilder.append("import "+fqImp+";\n");
    // }
    // }
    // text = text.replaceFirst("#IMPORT_DECLARATIONS#", importsBuilder.toString());
    return new Document(ScoutUtility.cleanLineSeparator(text, (ICompilationUnit) null));
  }

}

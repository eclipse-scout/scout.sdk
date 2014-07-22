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
package org.eclipse.scout.sdk.workspace.type;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ColumnData.SdkColumnCommand;
import org.eclipse.scout.commons.annotations.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.icon.IIconProvider;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.IRegEx;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.ast.AstUtility;
import org.eclipse.scout.sdk.util.ast.visitor.MethodBodyAstVisitor;
import org.eclipse.scout.sdk.util.ast.visitor.TypeAnnotationAstVisitor;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.IMethodFilter;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleGraph;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.workspace.dto.pagedata.PageDataAnnotation;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;

public class ScoutTypeUtility extends TypeUtility {

  private static final Pattern PATTERN = Pattern.compile("[^\\.]*$");
  private static final Pattern RETURN_TRUE_PATTERN = Pattern.compile("return\\s*true", Pattern.MULTILINE);
  private static final Pattern SUFF_CLASS_REGEX = Pattern.compile("\\.class$");

  protected ScoutTypeUtility() {
  }

  /**
   * Returns the immediate member types declared by the given type which are sub-types of the given super-type. The
   * results is sorted using the order annotation of the types.
   *
   * @param declaringType
   *          The type whose immediate inner types should be returned.
   * @param superType
   *          The super-type for which all returned types must be a sub-type.
   * @return the immediate member types declared by the given type which are sub-types of the given super-type.
   */
  public static Set<IType> getInnerTypesOrdered(IType declaringType, IType superType) {
    return getInnerTypesOrdered(declaringType, superType, ScoutTypeComparators.getOrderAnnotationComparator());
  }

  /**
   * Returns the immediate member types declared by the given type which are sub-types of the given super-type. The
   * results is sorted using the order annotation of the types.
   *
   * @param declaringType
   *          The type whose immediate inner types should be returned.
   * @param superType
   *          The super-type for which all returned types must be a sub-type.
   * @param localHierarchy
   *          The local type hierarchy to use.
   * @return the immediate member types declared by the given type which are sub-types of the given super-type.
   */
  public static Set<IType> getInnerTypesOrdered(IType declaringType, IType superType, ITypeHierarchy localHierarchy) {
    return getInnerTypesOrdered(declaringType, superType, ScoutTypeComparators.getOrderAnnotationComparator(), localHierarchy);
  }

  public static IScoutBundle getScoutBundle(IProject p) {
    return ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundle(p);
  }

  public static IScoutBundle getScoutBundle(IJavaElement element) {
    return ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundle(element);
  }

  /**
   * checks whether element is on the classpath of the given bundle
   *
   * @param element
   *          the element to search
   * @param bundle
   *          the bundle classpath to search in
   * @return true if element was found in the classpath of bundle
   */
  public static boolean isOnClasspath(IScoutBundle element, IScoutBundle bundle) {
    return isOnClasspath(ScoutUtility.getJavaProject(element), ScoutUtility.getJavaProject(bundle));
  }

  /**
   * checks whether element is on the classpath of the given bundle
   *
   * @param element
   *          the element to search
   * @param bundle
   *          the bundle classpath to search in
   * @return true if element was found in the classpath of bundle
   */
  public static boolean isOnClasspath(IJavaElement element, IScoutBundle bundle) {
    return isOnClasspath(element, ScoutUtility.getJavaProject(bundle));
  }

  /**
   * <xmp>
   * public void execCreateChildPages(Collection<IPage> pageList){
   * A a = new A();
   * pageList.add(a);
   * B b = new B();
   * pageList.add(b);
   * }
   * // execCreateChildPages.getAllNewTypeOccurrences() returns Set<IType>[A,B}]
   * </xmp>
   *
   * @return
   * @throws JavaModelException
   */
  public static Set<IType> getNewTypeOccurencesInMethod(IMethod method) {
    Set<IType> types = new LinkedHashSet<IType>();
    if (TypeUtility.exists(method)) {
      try {
        String src = method.getSource();
        if (src != null) {
          src = ScoutUtility.removeComments(src);
          Matcher matcher = IRegEx.METHOD_NEW_TYPE_OCCURRENCES.matcher(src);
          while (matcher.find()) {
            try {
              String resolvedSignature = SignatureUtility.getResolvedSignature(org.eclipse.jdt.core.Signature.createTypeSignature(matcher.group(1), false), method.getDeclaringType());
              if (!StringUtility.isNullOrEmpty(resolvedSignature)) {
                String pck = org.eclipse.jdt.core.Signature.getSignatureQualifier(resolvedSignature);
                String simpleName = org.eclipse.jdt.core.Signature.getSignatureSimpleName(resolvedSignature);
                if (!StringUtility.isNullOrEmpty(pck) && !StringUtility.isNullOrEmpty(simpleName)) {
                  IType candidate = TypeUtility.getType(pck + "." + simpleName);
                  if (TypeUtility.exists(candidate)) {
                    types.add(candidate);
                  }
                }
              }
            }
            catch (IllegalArgumentException e) {
              ScoutSdk.logWarning("could not parse signature '" + matcher.group(1) + "' in method '" + method.getElementName() + "' of type '" + method.getDeclaringType().getFullyQualifiedName() + "'. Trying to find page occurences.");
            }
            catch (CoreException ex) {
              ScoutSdk.logWarning("could not resolve signature '" + matcher.group(1) + "' in method '" + method.getElementName() + "' of type '" + method.getDeclaringType().getFullyQualifiedName() + "'. Trying to find page occurences.");
            }
          }
        }
      }
      catch (JavaModelException e) {
        ScoutSdk.logError("could not find new type occurences in method '" + method.getElementName() + "' on type '" + method.getDeclaringType().getFullyQualifiedName() + "'.", e);
      }
    }
    return types;
  }

  private static ASTVisitor getTypeLiteralCollectorVisitor(final List<IType> collector) {
    return new ASTVisitor() {
      @Override
      public boolean visit(TypeLiteral node) {
        ITypeBinding b = node.getType().resolveBinding();
        if (b != null) {
          IJavaElement e = b.getJavaElement();
          if (TypeUtility.exists(e) && e.getElementType() == IJavaElement.TYPE) {
            collector.add((IType) e);
          }
        }
        return false;
      }
    };
  }

  public static List<IType> getTypeOccurenceInMethod(IMethod member) throws JavaModelException {
    List<IType> types = new ArrayList<IType>();
    AstUtility.visitMember(member, new MethodBodyAstVisitor(member, getTypeLiteralCollectorVisitor(types)));
    return types;
  }

  /**
   * Gets all {@link IType}s referenced as {@link TypeLiteral}s in the given {@link IAnnotation}.
   *
   * @param annotation
   *          The annotation for which the referenced types should be returned.
   * @param declaringType
   *          The declaring type of the given annotation.
   * @return All {@link IType}s that are referenced within the given annotation.
   * @throws JavaModelException
   */
  public static List<IType> getTypeOccurenceInAnnotation(IAnnotation annotation, IType declaringType) throws JavaModelException {
    List<IType> types = new ArrayList<IType>();
    AstUtility.visitMember(declaringType, new TypeAnnotationAstVisitor(annotation, declaringType, getTypeLiteralCollectorVisitor(types)));
    return types;
  }

  public static INlsProject findNlsProject(IJavaElement element) {
    IScoutBundle scoutBundle = getScoutBundle(element);
    if (scoutBundle != null) {
      return scoutBundle.getNlsProject();
    }
    return null;
  }

  public static IIconProvider findIconProvider(IJavaElement element) {
    IScoutBundle scoutBundle = getScoutBundle(element);
    if (scoutBundle != null) {
      return scoutBundle.getIconProvider();
    }
    return null;
  }

  /**
   * Gets the form data type that is referenced in the form data annotation of the given form.<br>
   * If the annotation does not exist or points to an inexistent form data type, null is returned.
   *
   * @param form
   *          the form for which the form data should be returned.
   * @return the form data type or null if it could not be found.
   * @throws JavaModelException
   */
  public static IType findFormDataForForm(IType form) throws JavaModelException {
    if (TypeUtility.exists(form)) {
      FormDataAnnotation a = findFormDataAnnotation(form, TypeUtility.getSupertypeHierarchy(form));
      if (a != null) {
        return a.getFormDataType();
      }
    }
    return null;
  }

  /**
   * Gets the page data type that is referenced in the page data annotation of the given page type.<br>
   * If the annotation does not exist or points to an inexistent page data type, null is returned.
   *
   * @param page
   *          the page for which the page data should be returned.
   * @return the page data class or null.
   * @throws JavaModelException
   */
  public static IType findPageDataForPage(IType page) throws JavaModelException {
    if (TypeUtility.exists(page)) {
      PageDataAnnotation anot = findPageDataAnnotation(page, TypeUtility.getSupertypeHierarchy(page));
      if (anot != null && !StringUtility.isNullOrEmpty(anot.getPageDataTypeSignature())) {
        IType result = TypeUtility.getTypeBySignature(anot.getPageDataTypeSignature());
        if (TypeUtility.exists(result)) {
          return result;
        }
      }
    }
    return null;
  }

  /**
   * @return Returns <code>true</code> if the given type exists and if it is annotated with an
   *         {@link IRuntimeClasses#Replace} annotation.
   */
  public static boolean existsReplaceAnnotation(IAnnotatable element) {
    IAnnotation annotation = JdtUtility.getAnnotation(element, IRuntimeClasses.Replace);
    return TypeUtility.exists(annotation);
  }

  public static FormDataAnnotation findFormDataAnnotation(IType type, ITypeHierarchy hierarchy) throws JavaModelException {
    return findFormDataAnnnotationImpl(type, hierarchy);
  }

  public static FormDataAnnotation findFormDataAnnotation(IMethod method) throws JavaModelException {
    FormDataAnnotation annotation = new FormDataAnnotation();
    try {
      fillFormDataAnnotation(method, annotation, true);
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not parse formdata annotation of method '" + method.getElementName() + "' on '" + method.getDeclaringType().getFullyQualifiedName() + "'", e);
      return null;
    }
    return annotation;
  }

  private static FormDataAnnotation findFormDataAnnnotationImpl(IType type, ITypeHierarchy hierarchy) throws JavaModelException {
    FormDataAnnotation anot = new FormDataAnnotation();
    try {
      parseFormDataAnnotationReq(anot, type, hierarchy, true);
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not parse formdata annotation of type '" + type.getFullyQualifiedName() + "'.", e);
      return null;
    }
    return anot;
  }

  private static void parseFormDataAnnotationReq(FormDataAnnotation annotation, IType type, ITypeHierarchy hierarchy, boolean isOwner) {
    if (TypeUtility.exists(type)) {
      boolean replaceAnnotationPresent = existsReplaceAnnotation(type);
      IType superType = hierarchy.getSuperclass(type);
      parseFormDataAnnotationReq(annotation, superType, hierarchy, replaceAnnotationPresent);

      if (replaceAnnotationPresent) {
        if (!existsReplaceAnnotation(superType)) {
          // super type is the original field that is going to be replaced by the given type
          // check whether the super type is embedded into a form field that is annotated by @FormData with SdkCommand.IGNORE.
          try {
            IType declaringType = superType.getDeclaringType();
            while (TypeUtility.exists(declaringType)) {
              FormDataAnnotation declaringTypeformDataAnnotation = findFormDataAnnotation(declaringType, hierarchy);
              if (FormDataAnnotation.isIgnore(declaringTypeformDataAnnotation)) {
                // super type is embedded into a ignored form field. Hence this field is ignored as well. Adjust parsed annotation.
                annotation.setSdkCommand(SdkCommand.IGNORE);
                break;
              }
              declaringType = declaringType.getDeclaringType();
            }
          }
          catch (JavaModelException e) {
            ScoutSdk.logWarning("could not determine enclosing class's @FormData annotation", e);
          }
        }

        if (!FormDataAnnotation.isIgnore(annotation)) {
          // a form field data must always be created for a replacing fields if its parent has one and it must extend the parent field's field data class
          return;
        }
      }

      try {
        fillFormDataAnnotation(type, annotation, isOwner);
      }
      catch (JavaModelException e) {
        ScoutSdk.logWarning("could not parse form data annotation of '" + type.getFullyQualifiedName() + "'.", e);
      }
    }
  }

  @SuppressWarnings("null")
  private static void fillFormDataAnnotation(IJavaElement element, FormDataAnnotation formDataAnnotation, boolean isOwner) throws JavaModelException {
    IAnnotation annotation = null;
    if (element instanceof IAnnotatable) {
      annotation = JdtUtility.getAnnotation((IAnnotatable) element, IRuntimeClasses.FormData);
    }
    if (TypeUtility.exists(annotation)) {
      // context type
      IType contextType = null;
      if (element.getElementType() == IJavaElement.TYPE) {
        contextType = (IType) element;
      }
      else {
        contextType = (IType) element.getAncestor(IJavaElement.TYPE);
      }

      String valueSignature = null;
      SdkCommand sdkCommand = null;
      DefaultSubtypeSdkCommand subTypeCommand = null;
      int genericOrdinal = -1;
      List<String> interfaceSignatures = null;

      for (IMemberValuePair p : annotation.getMemberValuePairs()) {
        String memberName = p.getMemberName();
        Object value = p.getValue();
        if ("value".equals(memberName)) {
          try {
            String simpleName = SUFF_CLASS_REGEX.matcher((String) value).replaceAll("");
            valueSignature = SignatureUtility.getReferencedTypeSignature(contextType, simpleName, true);
          }
          catch (Exception e) {
            ScoutSdk.logError("could not parse formdata annotation value '" + value + "'.", e);
          }

        }
        else if ("sdkCommand".equals(memberName)) {
          try {
            Matcher m = PATTERN.matcher((String) value);
            if (m.find() && m.group().length() > 0) {
              String opString = m.group();
              sdkCommand = SdkCommand.valueOf(opString);
            }
          }
          catch (Exception e) {
            ScoutSdk.logError("could not parse formdata annotation sdkCommand '" + value + "'.", e);
          }
        }
        else if ("defaultSubtypeSdkCommand".equals(memberName)) {
          try {
            Matcher m = PATTERN.matcher((String) value);
            if (m.find() && m.group().length() > 0) {
              String opString = m.group();
              subTypeCommand = DefaultSubtypeSdkCommand.valueOf(opString);
            }
          }
          catch (Exception e) {
            ScoutSdk.logError("could not parse formdata annotation defaultSubtypeCommand '" + value + "'.", e);
          }
        }
        else if ("genericOrdinal".equals(memberName)) {
          try {
            genericOrdinal = ((Integer) value).intValue();
          }
          catch (Exception e) {
            ScoutSdk.logError("could not parse formdata annotation genericOrdinal '" + value + "'.", e);
          }
        }
        else if ("interfaces".equals(memberName)) {
          if (value instanceof Object[]) {
            Object[] interfaces = (Object[]) value;
            if (interfaces.length > 0) {
              interfaceSignatures = new ArrayList<String>(interfaces.length);
              for (Object o : interfaces) {
                if (o instanceof String && StringUtility.hasText(o.toString())) {
                  String referencedTypeSignature = SignatureUtility.getReferencedTypeSignature(contextType, o.toString(), true);
                  if (StringUtility.hasText(referencedTypeSignature)) {
                    interfaceSignatures.add(referencedTypeSignature);
                  }
                }
              }
            }
          }
        }
      }

      // default setup
      formDataAnnotation.setAnnotationOwner(element);
      if (interfaceSignatures != null && !interfaceSignatures.isEmpty()) {
        for (String sig : interfaceSignatures) {
          formDataAnnotation.addInterfaceSignature(sig);
        }
      }
      if (!StringUtility.isNullOrEmpty(valueSignature)) {
        if (isOwner) {
          formDataAnnotation.setFormDataTypeSignature(valueSignature);
        }
        else {
          formDataAnnotation.setSuperTypeSignature(valueSignature);
        }
      }
      if (isOwner && sdkCommand != null) {
        formDataAnnotation.setSdkCommand(sdkCommand);
      }
      if (subTypeCommand != null) {
        formDataAnnotation.setDefaultSubtypeSdkCommand(subTypeCommand);
      }
      if (genericOrdinal > -1) {
        formDataAnnotation.setGenericOrdinal(genericOrdinal);
      }
      // correction
      if (isOwner && sdkCommand == SdkCommand.USE && !StringUtility.isNullOrEmpty(valueSignature) && element.getParent().getElementType() != IJavaElement.COMPILATION_UNIT) {
        formDataAnnotation.setSuperTypeSignature(valueSignature);
        formDataAnnotation.setFormDataTypeSignature(null);
        formDataAnnotation.setSdkCommand(SdkCommand.CREATE);
      }

      if (element.getElementType() == IJavaElement.METHOD && formDataAnnotation.getSdkCommand() == null) {
        formDataAnnotation.setSdkCommand(SdkCommand.CREATE);
      }
    }
  }

  /**
   * Parses the possible available {@link IRuntimeClasses#PageData} annotation on the given type. If the type is not
   * annotated, <code>null</code> is returned.
   *
   * @since 3.10.0-M1
   */
  public static PageDataAnnotation findPageDataAnnotation(IType type, ITypeHierarchy superTypeHierarchy) throws JavaModelException {
    if (!TypeUtility.exists(type)) {
      return null;
    }

    String typeSignature = getPageDataAnnotationValue(type);
    if (StringUtility.isNullOrEmpty(typeSignature)) {
      return null;
    }

    IType tmpType = type;
    String superTypeSignature = null;
    IType iPageWithTable = TypeUtility.getType(IRuntimeClasses.IPageWithTable);

    do {
      if (!superTypeHierarchy.contains(iPageWithTable)) {
        break;
      }

      tmpType = superTypeHierarchy.getSuperclass(tmpType);
      superTypeSignature = getPageDataAnnotationValue(tmpType);
    }
    while (superTypeSignature == null && tmpType != null);

    if (superTypeSignature == null) {
      superTypeSignature = SignatureCache.createTypeSignature(IRuntimeClasses.AbstractTablePageData);
    }

    return new PageDataAnnotation(typeSignature, superTypeSignature);
  }

  /**
   * Checks whether the given type is annotated with a {@link IRuntimeClasses#PageData} annotation and if so, this
   * method returns its <code>value()</code> as resolved type signature. Otherwise <code>null</code>.
   *
   * @since 3.10.0-M1
   */
  private static String getPageDataAnnotationValue(IType type) throws JavaModelException {
    if (!TypeUtility.exists(type)) {
      return null;
    }

    IAnnotation annotation = JdtUtility.getAnnotation(type, IRuntimeClasses.PageData);
    if (!TypeUtility.exists(annotation)) {
      return null;
    }

    for (IMemberValuePair p : annotation.getMemberValuePairs()) {
      if ("value".equals(p.getMemberName())) {
        Object value = p.getValue();
        String simpleName = SUFF_CLASS_REGEX.matcher((String) value).replaceAll("");
        return SignatureUtility.getReferencedTypeSignature(type, simpleName, true);
      }
    }

    return null;
  }

  /**
   * Parses the possible available {@link IRuntimeClasses#ColumnData} annotation on the given type. If the type is not
   * annotated, <code>null</code> is returned.
   *
   * @since 3.10.0-M5
   */
  public static SdkColumnCommand findColumnDataSdkColumnCommand(IType type, ITypeHierarchy superTypeHierarchy) {
    if (!TypeUtility.exists(type)) {
      return null;
    }

    SdkColumnCommand sdkColumnCommand = getColumnDataAnnotationValue(type);
    if (sdkColumnCommand == SdkColumnCommand.IGNORE || !existsReplaceAnnotation(type)) {
      return sdkColumnCommand;
    }

    IType replacedType = superTypeHierarchy.getSuperclass(type);
    if (findColumnDataSdkColumnCommand(replacedType, superTypeHierarchy) != SdkColumnCommand.IGNORE) {
      return SdkColumnCommand.IGNORE;
    }
    if (sdkColumnCommand == null) {
      return SdkColumnCommand.IGNORE;
    }
    return sdkColumnCommand;
  }

  /**
   * Checks whether the given type is annotated with a {@link IRuntimeClasses#ColumnData} annotation and if so, this
   * method returns its <code>value()</code> as resolved type signature. Otherwise <code>null</code>.
   *
   * @since 3.10.0-M5
   */
  private static SdkColumnCommand getColumnDataAnnotationValue(IType type) {
    if (!TypeUtility.exists(type)) {
      return null;
    }

    IAnnotation annotation = JdtUtility.getAnnotation(type, IRuntimeClasses.ColumnData);
    if (!TypeUtility.exists(annotation)) {
      return null;
    }

    try {
      for (IMemberValuePair p : annotation.getMemberValuePairs()) {
        if ("value".equals(p.getMemberName())) {
          Object value = p.getValue();
          try {
            Matcher m = PATTERN.matcher((String) value);
            if (m.find() && m.group().length() > 0) {
              return SdkColumnCommand.valueOf(m.group());
            }
          }
          catch (Exception e) {
            ScoutSdk.logError("cannot parse @ColumnData.value(): '" + value + "'", e);
          }
          break;
        }
      }
    }
    catch (JavaModelException me) {
      ScoutSdk.logError("exception while reading values", me);
    }

    return null;
  }

  public static Set<IType> getPotentialMasterFields(IType field) {
    ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy(field.getCompilationUnit());
    IType mainbox = TypeUtility.getAncestor(field, TypeFilters.getRegexSimpleNameFilter("MainBox"));
    Set<IType> collector = new TreeSet<IType>(TypeComparators.getTypeNameComparator());
    if (TypeUtility.exists(mainbox)) {
      collectPotentialMasterFields(mainbox, collector, hierarchy);
    }
    collector.remove(field);
    return collector;
  }

  private static void collectPotentialMasterFields(IType type, Set<IType> collector, ITypeHierarchy formFieldHierarchy) {
    if (TypeUtility.exists(type)) {
      if (formFieldHierarchy.isSubtype(TypeUtility.getType(IRuntimeClasses.IValueField), type)) {
        collector.add(type);
      }
      for (IType subType : TypeUtility.getInnerTypes(type)) {
        collectPotentialMasterFields(subType, collector, formFieldHierarchy);
      }
    }
  }

  public static Set<IType> getInnerTypes(IType declaringType, IType superType, Comparator<IType> comparator) {
    if (TypeUtility.exists(declaringType)) {
      ITypeHierarchy typeHierarchy = TypeUtility.getLocalTypeHierarchy(declaringType);
      return TypeUtility.getInnerTypes(declaringType, TypeFilters.getSubtypeFilter(superType, typeHierarchy), comparator);
    }
    return CollectionUtility.hashSet();
  }

  public static Set<IType> getInnerTypes(IType declaringType, IType superType, ITypeHierarchy hierarchy, Comparator<IType> comparator) {
    if (TypeUtility.exists(declaringType)) {
      return TypeUtility.getInnerTypes(declaringType, TypeFilters.getSubtypeFilter(superType, hierarchy), comparator);
    }
    return CollectionUtility.hashSet();
  }

  public static Set<IType> getFormFields(IType declaringType) {
    return getInnerTypes(declaringType, TypeUtility.getType(IRuntimeClasses.IFormField), ScoutTypeComparators.getOrderAnnotationComparator());
  }

  public static Set<IType> getFormFields(IType declaringType, ITypeHierarchy hierarchy) {
    return getInnerTypes(declaringType, TypeUtility.getType(IRuntimeClasses.IFormField), hierarchy, ScoutTypeComparators.getOrderAnnotationComparator());
  }

  public static Double getOrderAnnotationValue(IAnnotatable a) throws JavaModelException {
    IAnnotation annotation = JdtUtility.getAnnotation(a, IRuntimeClasses.Order);
    return JdtUtility.getAnnotationValueNumeric(annotation, "value");
  }

  public static String getClassIdAnnotationValue(IType t) throws JavaModelException {
    IAnnotation annotation = JdtUtility.getAnnotation(t, IRuntimeClasses.ClassId);
    return JdtUtility.getAnnotationValueString(annotation, "value");
  }

  public static Set<IType> getTrees(IType declaringType) {
    return getInnerTypes(declaringType, TypeUtility.getType(IRuntimeClasses.ITree), TypeComparators.getTypeNameComparator());
  }

  public static Set<IType> getTables(IType declaringType) {
    return getInnerTypes(declaringType, TypeUtility.getType(IRuntimeClasses.ITable), TypeComparators.getTypeNameComparator());
  }

  public static Set<IType> getColumns(IType table) {
    return getInnerTypes(table, TypeUtility.getType(IRuntimeClasses.IColumn), ScoutTypeComparators.getOrderAnnotationComparator());
  }

  public static Set<IType> getPrimaryKeyColumns(IType table) {
    Set<IType> ret = new LinkedHashSet<IType>();
    for (IType col : getColumns(table)) {
      try {
        IMethod primKeyMethod = TypeUtility.getMethod(col, "getConfiguredPrimaryKey");
        if (TypeUtility.exists(primKeyMethod)) {
          String isPrimaryKey = PropertyMethodSourceUtility.getMethodReturnValue(primKeyMethod);
          if (Boolean.valueOf(isPrimaryKey)) {
            ret.add(col);
          }
        }
      }
      catch (CoreException e) {
        ScoutSdk.logError("cold not parse column '" + col.getFullyQualifiedName() + "' for primary key.", e);
      }
    }
    return ret;
  }

  public static String getCodeIdGenericTypeSignature(IType codeType) throws CoreException {
    if (!TypeUtility.exists(codeType)) {
      return null;
    }
    return getCodeIdGenericTypeSignature(codeType, ScoutSdkCore.getHierarchyCache().getSupertypeHierarchy(codeType));
  }

  /**
   * Gets the signature of the generic describing the data type of nested code types.
   *
   * @param codeType
   *          The code type whose generic attribute should be parsed
   * @param superTypeHierarchy
   * @return the signature of the 'CODE_ID' generic parameter of the given code type class or null.
   * @throws CoreException
   */
  public static String getCodeIdGenericTypeSignature(IType codeType, ITypeHierarchy superTypeHierarchy) throws CoreException {
    return SignatureUtility.resolveGenericParameterInSuperHierarchy(codeType, superTypeHierarchy, IRuntimeClasses.ICodeType, IRuntimeClasses.TYPE_PARAM_CODETYPE__CODE_ID);
  }

  public static String getCodeSignature(IType codeType, ITypeHierarchy superTypeHierarchy) throws CoreException {
    if (superTypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.AbstractCodeTypeWithGeneric))) {
      return SignatureUtility.resolveGenericParameterInSuperHierarchy(codeType, superTypeHierarchy, IRuntimeClasses.AbstractCodeTypeWithGeneric, IRuntimeClasses.TYPE_PARAM_CODETYPE__CODE);
    }
    else {
      String codeIdSig = getCodeIdGenericTypeSignature(codeType, superTypeHierarchy);
      if (codeIdSig == null) {
        return null;
      }
      return SignatureCache.createTypeSignature(IRuntimeClasses.ICode + '<' + Signature.toString(codeIdSig) + '>');
    }
  }

  public static Set<IType> getCodes(IType declaringType) {
    return getInnerTypes(declaringType, TypeUtility.getType(IRuntimeClasses.ICode), ScoutTypeComparators.getOrderAnnotationComparator());
  }

  public static Set<IType> getKeyStrokes(IType declaringType) {
    return getInnerTypes(declaringType, TypeUtility.getType(IRuntimeClasses.IKeyStroke), TypeComparators.getTypeNameComparator());
  }

  public static Set<IType> getCalendar(IType declaringType) {
    return getInnerTypes(declaringType, TypeUtility.getType(IRuntimeClasses.ICalendar), TypeComparators.getTypeNameComparator());
  }

  public static Set<IType> getCalendarItemProviders(IType declaringType) {
    return getInnerTypes(declaringType, TypeUtility.getType(IRuntimeClasses.ICalendarItemProvider), ScoutTypeComparators.getOrderAnnotationComparator());
  }

  public static Set<IType> getMenus(IType declaringType) {
    return getInnerTypes(declaringType, TypeUtility.getType(IRuntimeClasses.IMenu), ScoutTypeComparators.getOrderAnnotationComparator());
  }

  public static Set<IType> getDataModelEntities(IType declaringType) {
    return getInnerTypes(declaringType, TypeUtility.getType(IRuntimeClasses.IDataModelEntity), TypeComparators.getTypeNameComparator());
  }

  public static Set<IType> getDataModelAttributes(IType declaringType) {
    return getInnerTypes(declaringType, TypeUtility.getType(IRuntimeClasses.IDataModelAttribute), TypeComparators.getTypeNameComparator());
  }

  public static Set<IType> getFormHandlers(IType declaringType) {
    return getInnerTypes(declaringType, TypeUtility.getType(IRuntimeClasses.IFormHandler), TypeComparators.getTypeNameComparator());
  }

  public static IMethod getFormFieldGetterMethod(final IType formField) {
    ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy(formField.getCompilationUnit());
    return getFormFieldGetterMethod(formField, hierarchy);
  }

  public static IMethod getFormFieldGetterMethod(final IType formField, ITypeHierarchy hierarchy) {
    IType form = TypeUtility.getAncestor(formField, TypeFilters.getMultiTypeFilterOr(
        TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IForm), hierarchy),
        TypeFilters.getPrimaryTypeFilter()));

    if (TypeUtility.exists(form)) {

      final String formFieldSignature = SignatureCache.createTypeSignature(formField.getFullyQualifiedName());
      final String regex = "^get" + formField.getElementName();
      IMethod method = TypeUtility.getFirstMethod(form, new IMethodFilter() {
        @Override
        public boolean accept(IMethod candidate) {
          if (candidate.getElementName().matches(regex)) {
            try {
              String returnTypeSignature = Signature.getReturnType(candidate.getSignature());
              returnTypeSignature = SignatureUtility.getResolvedSignature(returnTypeSignature, candidate.getDeclaringType());
              return SignatureUtility.isEqualSignature(formFieldSignature, returnTypeSignature);
            }
            catch (CoreException e) {
              ScoutSdk.logError("could not parse signature of method '" + candidate.getElementName() + "' in type '" + candidate.getDeclaringType().getFullyQualifiedName() + "'.", e);
              return false;
            }
          }
          return false;
        }
      });
      return method;
    }
    return null;
  }

  public static IMethod getColumnGetterMethod(IType column) {
    IType table = column.getDeclaringType();
    final String formFieldSignature = IRegEx.DOLLAR_REPLACEMENT.matcher(SignatureCache.createTypeSignature(column.getFullyQualifiedName())).replaceAll(".");

    final String regex = "^get" + column.getElementName();
    IMethod method = TypeUtility.getFirstMethod(table, new IMethodFilter() {
      @Override
      public boolean accept(IMethod candidate) {
        if (candidate.getElementName().matches(regex)) {
          try {
            String returnTypeSignature = Signature.getReturnType(candidate.getSignature());
            returnTypeSignature = SignatureUtility.getResolvedSignature(returnTypeSignature, candidate.getDeclaringType());
            return formFieldSignature.equals(returnTypeSignature);
          }
          catch (CoreException e) {
            ScoutSdk.logError("could not parse signature of method '" + candidate.getElementName() + "' in type '" + candidate.getDeclaringType().getFullyQualifiedName() + "'.", e);
            return false;
          }
        }
        return false;
      }
    });
    return method;
  }

  public static IMethod getWizardStepGetterMethod(IType wizardStep) {
    IType wizard = wizardStep.getDeclaringType();
    final String formFieldSignature = SignatureCache.createTypeSignature(wizardStep.getFullyQualifiedName());
    String regex = "^get" + wizardStep.getElementName();
    final Pattern pat = Pattern.compile(regex);
    IMethod method = TypeUtility.getFirstMethod(wizard, new IMethodFilter() {
      @Override
      public boolean accept(IMethod candidate) {
        if (pat.matcher(candidate.getElementName()).matches()) {
          try {
            String returnTypeSignature = Signature.getReturnType(candidate.getSignature());
            returnTypeSignature = SignatureUtility.getResolvedSignature(returnTypeSignature, candidate.getDeclaringType());
            return formFieldSignature.equals(returnTypeSignature);
          }
          catch (CoreException e) {
            ScoutSdk.logError("could not parse signature of method '" + candidate.getElementName() + "' in type '" + candidate.getDeclaringType().getFullyQualifiedName() + "'.", e);
            return false;
          }
        }
        return false;
      }
    });
    return method;
  }

  public static ConfigurationMethod getConfigurationMethod(IType declaringType, String methodName) {
    ITypeHierarchy superTypeHierarchy = ScoutSdkCore.getHierarchyCache().getSupertypeHierarchy(declaringType);
    return getConfigurationMethod(declaringType, methodName, superTypeHierarchy);
  }

  public static ConfigurationMethod getConfigurationMethod(IType declaringType, String methodName, ITypeHierarchy superTypeHierarchy) {
    return getConfigurationMethod(declaringType, methodName, superTypeHierarchy, 0, null);
  }

  public static ConfigurationMethod getConfigurationMethod(IType declaringType, String methodName, ITypeHierarchy superTypeHierarchy, int methodType, String configPropertyType) {
    return getConfigurationMethod(declaringType, methodName, superTypeHierarchy, superTypeHierarchy.getSuperClassStack(declaringType), methodType, configPropertyType);
  }

  private static ConfigurationMethod getConfigurationMethod(IType declaringType, String methodName, ITypeHierarchy superTypeHierarchy, Deque<IType> bottomUpAffectedTypes, int methodType, String configPropertyType) {
    ConfigurationMethod newMethod = null;
    try {
      Iterator<IType> topDownIterator = bottomUpAffectedTypes.descendingIterator();
      while (topDownIterator.hasNext()) {
        IType t = topDownIterator.next();
        IMethod m = TypeUtility.getMethod(t, methodName);
        if (TypeUtility.exists(m)) {
          if (newMethod != null) {
            newMethod.pushMethod(m);
          }
          else {
            if (methodType == 0) {
              IAnnotation configPropAnnotation = JdtUtility.getAnnotation(m, IRuntimeClasses.ConfigProperty);
              if (TypeUtility.exists(configPropAnnotation)) {
                methodType = ConfigurationMethod.PROPERTY_METHOD;

                if (!StringUtility.hasText(configPropertyType)) {
                  configPropertyType = JdtUtility.getAnnotationValueString(configPropAnnotation, "value");
                }
              }
              else {
                IAnnotation configOpAnnotation = JdtUtility.getAnnotation(m, IRuntimeClasses.ConfigOperation);
                if (TypeUtility.exists(configOpAnnotation)) {
                  methodType = ConfigurationMethod.OPERATION_METHOD;
                }
              }
            }

            if (methodType != 0) {
              newMethod = new ConfigurationMethod(declaringType, superTypeHierarchy, methodName, methodType);
              newMethod.pushMethod(m);

              if (methodType == ConfigurationMethod.PROPERTY_METHOD && StringUtility.hasText(configPropertyType)) {
                newMethod.setConfigAnnotationType(configPropertyType);
              }
            }
          }
        }
      }
    }
    catch (JavaModelException e) {
      ScoutSdk.logError("could not build ConfigPropertyType for '" + methodName + "' in type '" + declaringType.getFullyQualifiedName() + "'.", e);
    }
    return newMethod;
  }

  public static IType getFistProcessButton(IType declaringType, ITypeHierarchy hierarchy) {
    ITypeFilter buttonFilter = TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IButton), hierarchy);
    for (IType field : getFormFields(declaringType, hierarchy)) {
      if (buttonFilter.accept(field)) {
        IMethod m = TypeUtility.getMethod(field, "getConfiguredProcessButton");
        if (!TypeUtility.exists(m)) {
          return field;
        }
        else {
          try {
            if (RETURN_TRUE_PATTERN.matcher(field.getSource()).find()) {
              return field;
            }
          }
          catch (JavaModelException e) {
            ScoutSdk.logError("could not get source of '" + m.getElementName() + "' on '" + m.getDeclaringType().getFullyQualifiedName() + "'.", e);
          }
        }

      }
    }
    return null;
  }

  public static IStructuredType createStructuredType(IType type) {
    ITypeHierarchy supertypeHierarchy = ScoutSdkCore.getHierarchyCache().getSupertypeHierarchy(type);
    if (supertypeHierarchy == null) {
      return null;
    }
    if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.ICompositeField))) {
      return createStructuredCompositeField(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.ITableField))) {
      return createStructuredTableField(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.ITreeField))) {
      return createStructuredTreeField(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IPlannerField))) {
      return createStructuredPlannerField(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IComposerField))) {
      return createStructuredComposer(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IDataModelAttribute))) {
      return createStructuredComposer(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IDataModelEntity))) {
      return createStructuredComposer(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IFormField))) {
      return createStructuredFormField(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IForm))) {
      return createStructuredForm(type, null);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.ICalendar))) {
      return createStructuredCalendar(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.ICodeType))) {
      return createStructuredCodeType(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.ICode))) {
      return createStructuredCode(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IDesktop))) {
      return createStructuredDesktop(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IDesktopExtension))) {
      return createStructuredDesktop(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IOutline))) {
      return createStructuredOutline(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IPageWithNodes))) {
      return createStructuredPageWithNodes(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IPageWithTable))) {
      return createStructuredPageWithTable(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.ITable))) {
      return createStructuredTable(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IWizard))) {
      return createStructuredWizard(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IWizardStep))) {
      return createStructuredWizardStep(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IMenu))) {
      return createStructuredMenu(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IColumn))) {
      return createStructuredColumn(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IActivityMap))) {
      return createStructuredActivityMap(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IFormHandler))) {
      return createStructuredFormHandler(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IKeyStroke))) {
      return createStructuredKeyStroke(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IButton))) {
      return createStructuredButton(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IViewButton))) {
      return createStructuredViewButton(type);
    }
    else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IToolButton))) {
      return createStructuredToolButton(type);
    }
    else {
      ScoutSdk.logInfo("no structured type defined for type '" + type.getFullyQualifiedName() + "'.");
      return createUnknownStructuredType(type);
    }
  }

  /**
   * don not hang on this object.
   *
   * @param type
   * @return
   */
  private static IStructuredType createUnknownStructuredType(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_LOGGER,
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_FORM_DATA_BEAN,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_START_HANDLER,
        CATEGORIES.METHOD_INNER_TYPE_GETTER,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_FORM_FIELD,
        CATEGORIES.TYPE_COLUMN,
        CATEGORIES.TYPE_CODE,
        CATEGORIES.TYPE_FORM,
        CATEGORIES.TYPE_TABLE,
        CATEGORIES.TYPE_ACTIVITY_MAP,
        CATEGORIES.TYPE_TREE,
        CATEGORIES.TYPE_CALENDAR,
        CATEGORIES.TYPE_CALENDAR_ITEM_PROVIDER,
        CATEGORIES.TYPE_WIZARD,
        CATEGORIES.TYPE_WIZARD_STEP,
        CATEGORIES.TYPE_MENU,
        CATEGORIES.TYPE_VIEW_BUTTON,
        CATEGORIES.TYPE_TOOL_BUTTON,
        CATEGORIES.TYPE_KEYSTROKE,
        CATEGORIES.TYPE_COMPOSER_ATTRIBUTE,
        CATEGORIES.TYPE_COMPOSER_ENTRY,
        CATEGORIES.TYPE_FORM_HANDLER,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredButton(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredViewButton(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredToolButton(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_TOOL_BUTTON,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredKeyStroke(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredMenu(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_INNER_TYPE_GETTER,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_MENU,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredColumn(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredActivityMap(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_MENU,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredDesktop(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_LOGGER,
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_MENU,
        CATEGORIES.TYPE_VIEW_BUTTON,
        CATEGORIES.TYPE_TOOL_BUTTON,
        CATEGORIES.TYPE_KEYSTROKE,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredFormHandler(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredForm(IType type) {
    return createStructuredForm(type, null);
  }

  public static IStructuredType createStructuredForm(IType type, ITypeHierarchy localHierarchy) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_LOGGER,
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_FORM_DATA_BEAN,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_START_HANDLER,
        CATEGORIES.METHOD_INNER_TYPE_GETTER,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_FORM_FIELD,
        CATEGORIES.TYPE_KEYSTROKE,
        CATEGORIES.TYPE_FORM_HANDLER,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled, localHierarchy);
  }

  public static IStructuredType createStructuredOutline(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_LOGGER,
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredFormField(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_LOGGER,
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.TYPE_MENU,
        CATEGORIES.METHOD_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredComposer(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_LOGGER,
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_FORM_DATA_BEAN,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_COMPOSER_ATTRIBUTE,
        CATEGORIES.TYPE_COMPOSER_ENTRY,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredPageWithNodes(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_LOGGER,
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_INNER_TYPE_GETTER,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_MENU,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredPageWithTable(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_LOGGER,
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_TABLE,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredTableField(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_LOGGER,
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_TABLE,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredTable(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_LOGGER,
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_MENU,
        CATEGORIES.TYPE_COLUMN,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredCompositeField(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_LOGGER,
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_FORM_FIELD,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredCodeType(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_LOGGER,
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_CODE,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredCode(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_LOGGER,
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_CODE,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredTreeField(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_LOGGER,
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_TREE,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredPlannerField(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_LOGGER,
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_TABLE,
        CATEGORIES.TYPE_ACTIVITY_MAP,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredWizard(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_LOGGER,
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_INNER_TYPE_GETTER,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_WIZARD_STEP,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredWizardStep(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_LOGGER,
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_INNER_TYPE_GETTER,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  public static IStructuredType createStructuredCalendar(IType type) {
    EnumSet<CATEGORIES> enabled = EnumSet.of(
        CATEGORIES.FIELD_LOGGER,
        CATEGORIES.FIELD_STATIC,
        CATEGORIES.FIELD_MEMBER,
        CATEGORIES.FIELD_UNKNOWN,
        CATEGORIES.METHOD_CONSTRUCTOR,
        CATEGORIES.METHOD_CONFIG_PROPERTY,
        CATEGORIES.METHOD_CONFIG_EXEC,
        CATEGORIES.METHOD_OVERRIDDEN,
        CATEGORIES.METHOD_INNER_TYPE_GETTER,
        CATEGORIES.METHOD_LOCAL_BEAN,
        CATEGORIES.METHOD_UNCATEGORIZED,
        CATEGORIES.TYPE_CALENDAR_ITEM_PROVIDER,
        CATEGORIES.TYPE_UNCATEGORIZED
        );
    return new ScoutStructuredType(type, enabled);
  }

  protected static class GenericSignatureMapping {
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

  /**
   * Gets all server session classes (not abstract, not an interface, not deprecated) that are in the given scout
   * bundle.
   *
   * @param bundle
   *          The scout bundle in which the session classes must be found.
   * @return All server session classes in the given scout bundle ordered by name.
   * @see IScoutBundle
   */
  public static Set<IType> getServerSessionTypes(IScoutBundle bundle) {
    return getSessionTypes(null, bundle, TypeUtility.getType(IRuntimeClasses.IServerSession));
  }

  /**
   * Gets all client session classes (not abstract, not an interface, not deprecated) that are in the given scout
   * bundle.
   *
   * @param bundle
   *          The scout bundle in which the session classes must be found.
   * @return All client session classes in the given scout bundle ordered by name.
   * @see IScoutBundle
   */
  public static Set<IType> getClientSessionTypes(IScoutBundle bundle) {
    return getSessionTypes(null, bundle, TypeUtility.getType(IRuntimeClasses.IClientSession));
  }

  /**
   * Gets all server session classes (not abstract, not an interface, not deprecated) that are on the classpath of the
   * given java project.<br>
   * The session must not be within the given project. It is sufficient if the session class is on the classpath of the
   * project to be part of the result!
   *
   * @param context
   *          The java project whose classpath should be evaluated.
   * @return All server sessions that are on the classpath of the given java project ordered by name.
   * @see IJavaProject
   */
  public static Set<IType> getServerSessionTypes(IJavaProject context) {
    return getSessionTypes(context, null, TypeUtility.getType(IRuntimeClasses.IServerSession));
  }

  /**
   * Gets all client session classes (not abstract, not an interface, not deprecated) that are on the classpath of the
   * given java project.<br>
   * The session must not be within the given project. It is sufficient if the session class is on the classpath of the
   * project to be part of the result!
   *
   * @param context
   *          The java project whose classpath should be evaluated.
   * @return All client sessions that are on the classpath of the given java project ordered by name.
   * @see IJavaProject
   */
  public static Set<IType> getClientSessionTypes(IJavaProject context) {
    return getSessionTypes(context, null, TypeUtility.getType(IRuntimeClasses.IClientSession));
  }

  /**
   * Gets all session classes (not abstract, not an interface, not deprecated) that are on the classpath of the given
   * java project.<br>
   * The session must not be within the given project. It is sufficient if the session class is on the classpath of the
   * project to be part of the result!<br>
   * <br>
   * The type of session to be searched is determined by the type of scout bundle that belongs to the given java
   * project. This means the given java project must match to a scout bundle in the scout bundle graph. Otherwise a
   * {@link NullPointerException} is thrown.<br>
   * If the scout bundle that belongs to the given java project is of type client, client sessions are searched. If it
   * is of type server, server sessions are returned. Otherwise null is returned.
   *
   * @param context
   * @return The session classes based on the project type ordered by name or null.
   * @throws NullPointerException
   *           if no {@link IScoutBundle} could be found that belongs to the given context.
   * @see IScoutBundle
   * @see IScoutBundleGraph
   */
  public static Set<IType> getSessionTypes(IJavaProject context) {
    String type = ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundle(context).getType();
    if (IScoutBundle.TYPE_CLIENT.equals(type)) {
      return getClientSessionTypes(context);
    }
    else if (IScoutBundle.TYPE_SERVER.equals(type)) {
      return getServerSessionTypes(context);
    }
    return null;
  }

  private static Set<IType> getSessionTypes(IJavaProject context, IScoutBundle containerBundle, IType sessionBaseType) {
    ITypeFilter sessionFilter = null;
    if (containerBundle == null) {
      if (context == null) {
        sessionFilter = TypeFilters.getClassFilter();
      }
      else {
        sessionFilter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getClassFilter(), TypeFilters.getTypesOnClasspath(context));
      }
    }
    else {
      sessionFilter = ScoutTypeFilters.getClassesInScoutBundles(containerBundle);
    }
    ICachedTypeHierarchy clientSessionHierarchy = TypeUtility.getPrimaryTypeHierarchy(sessionBaseType);
    return clientSessionHierarchy.getAllSubtypes(sessionBaseType, sessionFilter, TypeComparators.getTypeNameComparator());
  }

  /**
   * Gets the order value for a type created in declaringType just before the item sibling.
   *
   * @param declaringType
   *          The container in which the new ordered item should be created.
   * @param orderDefinitionType
   *          The {@link IType} that defines the siblings. E.g. {@link IRuntimeClasses#IFormField} when formfields
   *          should be considered as siblings.
   * @param sibling
   *          The sibling item that will be after. the created item. Therefore the new item will be before this sibling.
   *          If <code>null</code>, the order for the last position in declaringType will be calculated.
   * @return The order to use for a new item at the given position.
   * @throws JavaModelException
   */
  public static double getOrderNr(IType declaringType, IType orderDefinitionType, IJavaElement sibling) throws JavaModelException {
    ITypeHierarchy typeHierarchy = TypeUtility.getLocalTypeHierarchy(declaringType);
    return getOrderNr(declaringType, orderDefinitionType, sibling, typeHierarchy);
  }

  /**
   * Gets the order value for a type created in declaringType just before the item sibling.
   *
   * @param declaringType
   *          The container in which the new ordered item should be created.
   * @param orderDefinitionType
   *          The {@link IType} that defines the siblings. E.g. {@link IRuntimeClasses#IFormField} when formfields
   *          should be considered as siblings.
   * @param sibling
   *          The sibling item that will be after. the created item. Therefore the new item will be before this sibling.
   *          If <code>null</code>, the order for the last position in declaringType will be calculated.
   * @param typeHierarchy
   *          The local hierarchy of the declaringType to use.
   * @return The order to use for a new item at the given position.
   * @throws JavaModelException
   */
  public static double getOrderNr(IType declaringType, IType orderDefinitionType, IJavaElement sibling, ITypeHierarchy typeHierarchy) throws JavaModelException {
    if (!TypeUtility.exists(orderDefinitionType) || !TypeUtility.exists(declaringType)) {
      return -1.0;
    }

    // get all siblings
    Set<IType> innerTypes = TypeUtility.getInnerTypes(declaringType, TypeFilters.getSubtypeFilter(orderDefinitionType, typeHierarchy), ScoutTypeComparators.getOrderAnnotationComparator());

    // find direct neighbors
    IType typeBefore = null;
    IType typeAfter = null;
    IType lastType = null;
    for (IType innerType : innerTypes) {
      if (innerType.equals(sibling)) {
        typeAfter = innerType;
        typeBefore = lastType;
        break;
      }
      lastType = innerType;
    }
    if (sibling == null) {
      typeBefore = lastType;
    }

    // parse order value for neighbors
    Double orderValueBefore = null;
    Double orderValueAfter = null;
    if (typeBefore != null) {
      orderValueBefore = ScoutTypeUtility.getOrderAnnotationValue(typeBefore);
    }
    if (typeAfter != null) {
      orderValueAfter = ScoutTypeUtility.getOrderAnnotationValue(typeAfter);
    }

    // calculate next values
    if (orderValueBefore != null && orderValueAfter == null) {
      // insert at last position
      double v = Math.ceil(orderValueBefore.doubleValue() / SdkProperties.ORDER_ANNOTATION_VALUE_STEP) * SdkProperties.ORDER_ANNOTATION_VALUE_STEP;
      return v + SdkProperties.ORDER_ANNOTATION_VALUE_STEP;
    }
    else if (orderValueBefore == null && orderValueAfter != null) {
      // insert at first position
      double v = Math.floor(orderValueAfter.doubleValue() / SdkProperties.ORDER_ANNOTATION_VALUE_STEP) * SdkProperties.ORDER_ANNOTATION_VALUE_STEP;
      if (v > SdkProperties.ORDER_ANNOTATION_VALUE_STEP) {
        return SdkProperties.ORDER_ANNOTATION_VALUE_STEP;
      }
      return v - SdkProperties.ORDER_ANNOTATION_VALUE_STEP;
    }
    else if (orderValueBefore != null && orderValueAfter != null) {
      // insert between two types
      double a = orderValueBefore.doubleValue();
      double b = orderValueAfter.doubleValue();
      return getOrderValueInBetween(a, b);
    }

    // other cases. e.g. first item in a container
    return SdkProperties.ORDER_ANNOTATION_VALUE_STEP;
  }

  /**
   * Gets an order value that is between the two given values.<br>
   * The algorithm tries to stick to numbers without decimal places as long as possible.<br>
   * If a common pattern (like normal steps according to {@link SdkProperties#ORDER_ANNOTATION_VALUE_STEP}) are found,
   * the corresponding pattern is followed.
   *
   * @param a
   *          First value
   * @param b
   *          Second value
   * @return A value in between a and b.
   */
  public static double getOrderValueInBetween(double a, double b) {
    double low = Math.min(a, b);
    double high = Math.max(a, b);
    double dif = high - low;
    double lowFloor = Math.floor(low);
    double lowCeil = Math.ceil(low);
    double highFloor = Math.floor(high);
    double nextIntLow = Math.min(lowCeil, highFloor);
    double prevIntHigh = Math.max(lowCeil, highFloor);

    // special case for stepwise increase
    if (low % SdkProperties.ORDER_ANNOTATION_VALUE_STEP == 0 && low + SdkProperties.ORDER_ANNOTATION_VALUE_STEP < high) {
      return low + SdkProperties.ORDER_ANNOTATION_VALUE_STEP;
    }

    if (lowFloor != highFloor && ((lowFloor != low && highFloor != high) || dif > 1.0)) {
      // integer value possible
      double intDif = prevIntHigh - nextIntLow;
      if (intDif == 1.0) {
        return prevIntHigh;
      }
      else {
        return nextIntLow + Math.floor(intDif / 2.0);
      }
    }
    else {
      return low + (dif / 2);
    }
  }
}

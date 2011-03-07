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
package org.eclipse.scout.sdk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.pde.internal.core.iproduct.IConfigurationFileInfo;
import org.eclipse.pde.internal.core.iproduct.IProductPlugin;
import org.eclipse.pde.internal.core.product.WorkspaceProductModel;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

public class ScoutSdkUtility {

  public static String BUNDLE_ID_HTTP_REGISTRY = "org.eclipse.equinox.http.registry";
  public static String BUNDLE_ID_HTTP_SERVLETBRIDGE = "org.eclipse.equinox.http.servletbridge";

  private static final ScoutSdkUtility instance = new ScoutSdkUtility();

  private ScoutSdkUtility() {
  }

  public static String getSimpleTypeSignature(String signature) {
    return getSimpleTypeSignature(signature, new ArrayList<String>());
  }

  /**
   * <xmp>
   * getSimpleTypeSignature("Ljava.lang.String", emptyList) -> String ;List<java.lang.String>
   * getSimpleTypeSignature(Signature.SIG_LONG, emptyList) -> long ;List<>
   * getSimpleTypeSignature(
   * 
   * "[Ljava.util.HashMap<Ljava.util.ArrayList<[[Ljava.lang.String;>;Lorg.eclipse.scout.sdk.workspace.member.IScoutType;>;"
   * , emptyList)
   * -> HashMap[]<ArrayList<String[][]>,IScoutType>
   * ;List<java.util.HashMap,java.util.ArrayList,java.lang.String,org.eclipse.scout.sdk.workspace.member.IScoutType>
   * </xmp>
   * 
   * @param signature
   * @param fullyQuallifiedImports
   * @return
   */
  public static String getSimpleTypeSignature(String signature, List<String> fullyQuallifiedImports) {
    String simpleName = signature.replaceAll("^([^\\<]*).*(\\;)$", "$1$2");
    String imp = Signature.getSignatureQualifier(simpleName);
    if (!StringUtility.isNullOrEmpty(imp)) {
      String impSimpleName = Signature.getSimpleName(simpleName);
      if (!StringUtility.isNullOrEmpty(impSimpleName)) {
        impSimpleName = impSimpleName.replaceAll("\\;$", "");
        fullyQuallifiedImports.add(imp + "." + impSimpleName);
      }
    }
    simpleName = Signature.getSignatureSimpleName(simpleName);
    String[] typeArguments = Signature.getTypeArguments(signature);
    if (typeArguments != null && typeArguments.length > 0) {
      simpleName += "<";
      for (String typeArg : typeArguments) {
        simpleName += getSimpleTypeSignature(typeArg, fullyQuallifiedImports) + ",";
      }
      simpleName = simpleName.replaceAll("\\,$", ">");
    }
    return simpleName;
  }

  /**
   * To get the simple type reference name within a context represented by the given importValidator. Every fully
   * qualified type name
   * will be passed to the importValidator to decide if the import is already in use.<br>
   * 
   * @param fullyQuallifiedTypeName
   *          e.g. java.lang.String (not a signature).
   * @param importValidator
   *          to evaluate all fully qualified names for create an import and use simple names.
   * @return the simple reference type name in the given validator scope.
   * @see ScoutSdkUtility#getSimpleTypeRefName(String, IImportValidator)
   */
  public static String getSimpleTypeRefFromFqn(String fullyQualifiedTypeName, IImportValidator importValidator) {
    return instance.getSimpleTypeRefImpl(Signature.createTypeSignature(fullyQualifiedTypeName, true), importValidator);
  }

  /**
   * To get the simple type reference name within a context represented by the given importValidator. Every fully
   * qualified type name
   * will be passed to the importValidator to decide if the import is already in use.<br>
   * Type conflicts e.g. <code>'java.util.List&lt;java.awt.List&gt;'</code> are solved within this method and will return List<java.awt.List> with the
   * java.util.List import.
   * <xmp>
   * getSimpleTypeRefName("Ljava.lang.String", emptyList) -> String ;List<java.lang.String>
   * getSimpleTypeRefName(Signature.SIG_LONG, emptyList) -> long
   * getSimpleTypeRefName(
   * 
   * "[Ljava.util.HashMap<Ljava.util.ArrayList<[[Ljava.lang.String;>;Lorg.eclipse.scout.sdk.workspace.member.IScoutType;>;"
   * , emptyList)
   * -> HashMap[]<ArrayList<String[][]>,IScoutType>
   * </xmp>
   * 
   * @param signature
   * @param importValidator
   * @return the simple reference
   */
  public static String getSimpleTypeRefName(String signature, IImportValidator importValidator) {
    return instance.getSimpleTypeRefImpl(signature, importValidator);
  }

  private String getSimpleTypeRefImpl(String signature, IImportValidator importValidator) {
    boolean isSequence = false;
    if (signature.startsWith("|")) {
      signature.replaceFirst("^[\\|]{1}", "");
      isSequence = true;
    }
    signature = signature.replaceAll("^[\\|]*", "");
    int arrayCount = Signature.getArrayCount(signature);
    signature = signature.replaceAll("^[\\[]*", "");
    String simpleName = null;
    if (getSignatureType(signature) == Signature.BASE_TYPE_SIGNATURE) {
      simpleName = Signature.getSignatureSimpleName(signature);
    }
    else if (Signature.getTypeSignatureKind(signature) == Signature.WILDCARD_TYPE_SIGNATURE) {
      switch (signature.charAt(0)) {
        case Signature.C_STAR:
          simpleName = "?";
          break;
        case Signature.C_SUPER:
          simpleName = "? super ";
          break;
        case Signature.C_EXTENDS:
          simpleName = "? extends ";
          break;
      }
      if (signature.length() > 1) {
        simpleName = simpleName + getSimpleTypeRefImpl(signature.substring(1), importValidator);
      }
    }
    else {
      String[] typeArguments = Signature.getTypeArguments(signature);
      if (typeArguments != null && typeArguments.length > 0) {
        signature = Signature.getTypeErasure(signature);
        signature = signature.replaceAll("[\\$\\/]{1}", ".");
      }
      simpleName = importValidator.getSimpleTypeRef(signature);
      if (typeArguments != null && typeArguments.length > 0) {
        simpleName += "<";
        for (String typeArg : typeArguments) {
          String genericSimpleName = getSimpleTypeRefImpl(typeArg, importValidator);
          simpleName += genericSimpleName + ",";
        }
        simpleName = simpleName.replaceAll("\\,$", ">");

      }
    }
    String arraySufix = "";
    for (int i = 0; i < arrayCount; i++) {
      arraySufix += "[]";
    }
    StringBuilder simpleTypeName = new StringBuilder();
    simpleTypeName.append(simpleName);
    simpleTypeName.append(arraySufix);
    if (isSequence) {
      simpleTypeName.append("...");
    }
    return simpleTypeName.toString();
  }

  public static String getFullyQuallifiedTypeName(String signature, IType jdtType) throws JavaModelException {
    return instance.getFullyQuallifiedTypeNameImpl(signature, jdtType);
  }

  private String getFullyQuallifiedTypeNameImpl(String signature, IType jdtType) throws JavaModelException {
    if (Signature.getTypeArguments(signature).length > 0) {
      signature = Signature.getTypeErasure(signature);
    }
    if (getSignatureType(signature) == Signature.BASE_TYPE_SIGNATURE) {
      return Signature.getSignatureSimpleName(signature);
    }
    if (signature.startsWith("Q")) {
      String[][] resolvedTypeName = jdtType.resolveType(Signature.getSignatureSimpleName(signature));
      if (resolvedTypeName != null && resolvedTypeName.length == 1) {
        String fqName = resolvedTypeName[0][0];
        if (fqName != null && fqName.length() > 0) {
          fqName = fqName + ".";
        }
        fqName = fqName + resolvedTypeName[0][1];
        return fqName;
      }
    }
    return null;
  }

  public static String getNonGenericSimpleName(String signature) throws IllegalArgumentException {
    String simpleName = signature.replaceAll("^[\\[]*([^\\<]*).*(\\;)$", "$1$2");
    if (getSignatureType(simpleName) != Signature.CLASS_TYPE_SIGNATURE) {
      throw new IllegalArgumentException("the signature must be a class type signature!");
    }
    simpleName = Signature.getSignatureSimpleName(simpleName);
    return simpleName;
  }

  public static String getQuallifiedSignature(String signature, IType jdtType) throws JavaModelException {
    return instance.getQuallifiedSignatureImpl(signature, jdtType);
  }

  private String getQuallifiedSignatureImpl(String signature, IType jdtType) throws JavaModelException {
    if (getSignatureType(signature) == Signature.BASE_TYPE_SIGNATURE) {
      return signature;
    }
    else {
      String regex = "^([\\+\\[]*)([^\\<\\(\\;]*)(.*)$";
      Matcher m = Pattern.compile(regex).matcher(signature);
      if (m.find()) {
        String prefix = m.group(1);
        String simpleSignature = m.group(2);
        String postfix = m.group(3);
        if (simpleSignature.startsWith("Q")) {
          String[][] resolvedTypeName = jdtType.resolveType(Signature.getSignatureSimpleName(simpleSignature + ";"));
          if (resolvedTypeName != null && resolvedTypeName.length == 1) {
            String fqName = resolvedTypeName[0][0];
            if (fqName != null && fqName.length() > 0) {
              fqName = fqName + ".";
            }
            fqName = fqName + resolvedTypeName[0][1];
            simpleSignature = Signature.createTypeSignature(fqName, true).replaceAll("(^.*)\\;$", "$1");
            signature = prefix + simpleSignature + postfix;
          }
        }
        String[] typeArguments = Signature.getTypeArguments(signature);

        for (String typeArg : typeArguments) {
          signature.replaceFirst("^([^<]*\\<.*)(" + Regex.quoteRegexSpecialCharacters(typeArg) + ")(.*)$", "$1" + getQuallifiedSignatureImpl(typeArg, jdtType) + "$3");
        }
      }
      else {
        ScoutSdk.logWarning("could not quallify types of signature '" + signature + "'");
      }
      return signature;
    }
  }

  public static int getSignatureType(String signature) {
    String simpleName = signature.replaceAll("^[\\[]*([^\\<]*).*(\\;)$", "$1$2");
    return Signature.getTypeSignatureKind(simpleName);
  }

  public static String getMethodIdentifier(IMethod method) {
    return instance.getMethodIdentifierImpl(method);
  }

  public static <T> boolean equals(T a, T b) {
    if (a instanceof String) {
      if (((String) a).equalsIgnoreCase("null")) {
        a = null;
      }
    }
    if (b instanceof String) {
      if (((String) b).equalsIgnoreCase("null")) {
        b = null;
      }
    }
    return CompareUtility.equals(a, b);
  }

  private String getMethodIdentifierImpl(IMethod method) {
    String methodIdentifier = "@" + method.getElementName() + "@(";
    for (String paramType : method.getParameterTypes()) {
      methodIdentifier += getUnresolvedSignature(paramType);
    }
    methodIdentifier += ")";
    return methodIdentifier;
  }

  public static String getResolvedSignature(String signature, IType type) throws JavaModelException {
    return instance.getResolvedSignatureImpl(signature, type);
  }

  private String getResolvedSignatureImpl(String signature, IType type) throws JavaModelException {
    String workingSig = signature.replaceAll("/", ".");
    workingSig = Signature.getTypeErasure(workingSig);
    StringBuilder signatureBuilder = new StringBuilder();
    Matcher prefMatcher = Pattern.compile("^([\\+\\[]+)(.*)$").matcher(workingSig);
    if (prefMatcher.find()) {
      signatureBuilder.append(prefMatcher.group(1));
      workingSig = prefMatcher.group(2);
    }
    if (Signature.getTypeSignatureKind(workingSig) == Signature.BASE_TYPE_SIGNATURE) {
      signatureBuilder.append(workingSig);
      return signatureBuilder.toString();
    }
    else {
      if (workingSig.length() > 0 && workingSig.charAt(0) == Signature.C_UNRESOLVED) {
        String[][] resolvedTypeName = type.resolveType(Signature.getSignatureSimpleName(workingSig));
        if (resolvedTypeName != null && resolvedTypeName.length == 1) {
          String fqName = resolvedTypeName[0][0];
          if (fqName != null && fqName.length() > 0) {
            fqName = fqName + ".";
          }
          fqName = fqName + resolvedTypeName[0][1];
          workingSig = Signature.createTypeSignature(fqName, true);
        }
      }
      workingSig = workingSig.replaceAll("(^.*)\\;$", "$1");
      signatureBuilder.append(workingSig);
      String[] typeArguments = Signature.getTypeArguments(signature);
      if (typeArguments.length > 0) {
        signatureBuilder.append("<");
        for (int i = 0; i < typeArguments.length; i++) {
          signatureBuilder.append(getResolvedSignatureImpl(typeArguments[i], type));
        }
        signatureBuilder.append(">");
      }
      signatureBuilder.append(";");
    }

    return signatureBuilder.toString();
  }

  private String getUnresolvedSignature(String signature) {
    signature = signature.replaceAll("/", ".");
    StringBuilder unresolvedSignature = new StringBuilder();
    // remove generic
    String typeErasure = Signature.getTypeErasure(signature);
    Matcher prefMatcher = Pattern.compile("^([\\+\\[]+)(.*)$").matcher(typeErasure);
    if (prefMatcher.find()) {
      unresolvedSignature.append(prefMatcher.group(1));
      typeErasure = prefMatcher.group(2);
    }
    if (Signature.getTypeSignatureKind(typeErasure) == Signature.BASE_TYPE_SIGNATURE) {
      unresolvedSignature.append(typeErasure);
      return unresolvedSignature.toString();
    }
    else {
      typeErasure = Signature.createTypeSignature(Signature.getSignatureSimpleName(typeErasure), false);
      typeErasure = typeErasure.replaceAll("\\;$", "");
      unresolvedSignature.append(typeErasure);
      String[] typeArguments = Signature.getTypeArguments(signature);
      if (typeArguments != null && typeArguments.length > 0) {
        unresolvedSignature.append("<");
        for (String typeArg : typeArguments) {
          unresolvedSignature.append(getUnresolvedSignature(typeArg));
        }
        unresolvedSignature.append(">");
      }
      unresolvedSignature.append(";");
      return unresolvedSignature.toString();
    }
  }

  public static Double getOrderAnnotation(IType type) {
    Double sortNo = null;
    if (TypeUtility.exists(type)) {
      try {
        IAnnotation annotation = type.getAnnotation(NamingUtility.getSimpleName(RuntimeClasses.Order));
        if (annotation.exists()) {
          IMemberValuePair[] memberValues = annotation.getMemberValuePairs();
          for (IMemberValuePair p : memberValues) {

            if ("value".equals(p.getMemberName())) {
              switch (p.getValueKind()) {
                case IMemberValuePair.K_DOUBLE:
                  sortNo = ((Double) p.getValue()).doubleValue();
                  break;
                case IMemberValuePair.K_FLOAT:
                  sortNo = ((Float) p.getValue()).doubleValue();
                  break;
                case IMemberValuePair.K_INT:
                  sortNo = ((Integer) p.getValue()).doubleValue();
                  break;
                default:
                  ScoutSdk.logError("could not find order annotation of '" + type.getFullyQualifiedName() + "'. ");
                  break;
              }
              break;
            }
          }
        }
        return sortNo;

      }
      catch (Throwable t) {
        ScoutSdk.logWarning("no @Order annotation found on '" + type.getFullyQualifiedName() + "'.", t);
      }
    }
    return sortNo;
  }

  public static WorkspaceProductModel getProductModel(IFile productFile, boolean load) throws CoreException {
    if (productFile != null) {
      WorkspaceProductModel model = null;
      model = new WorkspaceProductModel(productFile, false);
      if (load) {
        model.load();
      }
      return model;
    }
    return null;
  }

  /**
   * @param productFile
   * @return {@link Status#OK_STATUS} if the given product is valid to deploy on a app server using the servlet bridge
   */
  @SuppressWarnings("restriction")
  public static IStatus getServletBridgeProductStatus(IFile productFile) {
    if (productFile == null) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "product file is null.");
    }
    WorkspaceProductModel model = null;
    try {
      model = getProductModel(productFile, true);
    }
    catch (CoreException e) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "could not parse product file.");
    }
    if (!model.isValid()) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "product file is not valid.");
    }
    IConfigurationFileInfo configurationFileInfo = model.getProduct().getConfigurationFileInfo();
    if (configurationFileInfo != null) {
      try {
        IProject project = productFile.getProject();
        Properties props = new Properties();
        IPath path = new Path(configurationFileInfo.getPath(Platform.getOS()));
        path = path.removeFirstSegments(1);
        props.load(project.getFile(path).getContents());
        String osgiBundleEntry = props.getProperty("osgi.bundles");
        if (osgiBundleEntry == null) {
          return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "osgi.bundles entry in config.ini is missing.");
        }
        else {
//            org.eclipse.equinox.common@2:start, org.eclipse.update.configurator@start, org.eclipse.equinox.http.servletbridge@start, org.eclipse.equinox.http.registry@start, org.eclipse.core.runtime@start
          if (!osgiBundleEntry.contains(BUNDLE_ID_HTTP_SERVLETBRIDGE)) {
            return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "osgi.bundles entry in config.ini file must conatin '" + BUNDLE_ID_HTTP_SERVLETBRIDGE + "' bundle.");
          }
          if (!osgiBundleEntry.contains(BUNDLE_ID_HTTP_REGISTRY)) {
            return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "osgi.bundles entry in config.ini file must conatin '" + BUNDLE_ID_HTTP_REGISTRY + "' bundle.");
          }
        }

      }
      catch (Exception e) {
        return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "congig.ini file for product is not valid.");
      }
    }
    // reqired plugins
    HashSet<String> plugins = new HashSet<String>();
    for (IProductPlugin p : model.getProduct().getPlugins()) {
      plugins.add(p.getId());
    }
    if (!plugins.contains(BUNDLE_ID_HTTP_REGISTRY)) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "product must contain '" + BUNDLE_ID_HTTP_REGISTRY + "' as required bundle.");
    }
    if (!plugins.contains(BUNDLE_ID_HTTP_SERVLETBRIDGE)) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "product must contain '" + BUNDLE_ID_HTTP_SERVLETBRIDGE + "' as required bundle.");
    }

    return Status.OK_STATUS;
  }

  public static long getAdler32Checksum(ICompilationUnit icu) {
    if (TypeUtility.exists(icu)) {
      IResource resource = icu.getResource();
      if (resource != null && resource.exists() && resource.getType() == IResource.FILE) {
        IFile file = (IFile) resource;
        CheckedInputStream cis = null;
        try {
          cis = new CheckedInputStream(file.getContents(), new Adler32());
          byte[] buf = new byte[1024];
          while (cis.read(buf) >= 0) {
          }
          return cis.getChecksum().getValue();
        }
        catch (Exception e) {

        }
        finally {
          if (cis != null) {
            try {
              cis.close();
            }
            catch (IOException e) {
              // void
            }
          }
        }
      }
    }
    return -1;
  }

  public static void main(String[] args) {

    // System.out.println(Matcher.quoteReplacement("+Abstract.Test"));
    // String methodSig1 = "()[Ljava.lang.Class<+[Lcom.bsiag.scout.client.ui.desktop.outline.IOutline;>;";
    String methodSig1 = "(QHashMap<QString;QIOutline;>;QList<+QIOutline;>;)QString;";

    System.out.println(Signature.getArrayCount(methodSig1));
    methodSig1 = methodSig1.replaceAll("^\\([^\\)]*\\)", "");
    String typeErasure = Signature.getTypeErasure(methodSig1);
    System.out.println(typeErasure);
    System.out.println(Signature.getSimpleName(Signature.getSignatureSimpleName(typeErasure)));
    // System.out.println(Signature.get);
    // String retType=Signature.getReturnType(methodSig1);
    // System.out.println(retType);
    // System.out.println(Signature.getElementType(retType));
    // for(String arg : Signature.getTypeArguments(retType)){
    // System.out.println(Signature.getSignatureSimpleName(arg));
    // }
    // System.out.println(Signature.createTypeSignature("java.lang.Class<? extends org.eclipse.scout.rt.client.ui.desktop.outline.IOutline[]>", true));
    // System.out.println(Signature.getSimpleName("Ljava.lang.Class<+Lcom.bsiag.scout.client.ui.desktop.outline.IOutline;>;"));
    //
    // String methodSig2 = "(Lcom/bsiag/scout/client/ui/basic/tree/ITreeNode;Lcom/bsiag/commons/dnd/TransferObject;)V";
    // String methSig = methodSig1;
    // IImportValidator ival = new SimpleImportValidator();
    // String returnType=Signature.getReturnType(methSig);
    // System.out.println(getSimpleTypeRefName(returnType, ival));
    // for(String imp : ival.getImportsToCreate()){
    // System.out.println("imp: "+imp);
    // }
    // System.out.println("retval: "+returnType);
    // String[] parameterTypes=Signature.getParameterTypes(methSig);
    // System.out.println("--- params");
    // for(String a : parameterTypes){
    // System.out.println(a);
    // System.out.println(Signature.getSignatureSimpleName(a.replace("/",".")));
    // }
    // System.out.println("--- end params");
    // String s=Signature.createTypeSignature(Order.class.getName()+"(10.0)",true);
    // System.out.println(s);
    // String typeArguments=Signature.getTypeErasure(s);
    // String[] typeParameters=Signature.getParameterTypes(s);
    // System.out.println(getSimpleTypeRefName(s, new SimpleImportValidator()));
    // String genericSig = RuntimeClasses.AbstractCalendarField;
    // genericSig += "<MyCalendarField."+ScoutIdeProperties.TYPE_NAME_CALENDARFIELD_CALENDAR+">";
    // ArrayList<String> imps = new ArrayList<String>();
    // String s = getSimpleTypeSignature(genericSig, imps);
    // System.out.println(s);
    // for(String k : imps){
    // System.out.println(k);
    // }
  }

}

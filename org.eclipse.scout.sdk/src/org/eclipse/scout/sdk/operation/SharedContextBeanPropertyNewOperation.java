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
package org.eclipse.scout.sdk.operation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.CompilationUnitImportValidator;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>SharedContextBeanPropertyNewOperation</h3> A shared context property looks like:
 * 
 * <pre>
 * public Long getCompanyNr() {
 *   return getSharedContextVariable(&quot;companyNr&quot;, Long.class);
 * }
 * 
 * public void setCompanyNr(Long newValue) {
 *   setSharedContextVariable(&quot;companyNr&quot;, Long.class, newValue);
 * }
 * </pre>
 */
public class SharedContextBeanPropertyNewOperation implements IBeanPropertyNewOperation, IOperation {

  // fields
  private String m_beanName;
  private int m_methodFlags;
  private String m_beanTypeSignature;

  private IType m_clientSession;
  private IType m_serverSession;
  private IJavaElement m_siblingClientSession;
  private IJavaElement m_siblingServerSession;

  public SharedContextBeanPropertyNewOperation(IType serverSession, IType clientSession) {
    m_serverSession = serverSession;
    m_clientSession = clientSession;
  }

  @Override
  public String getOperationName() {
    return "new shared context variable";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getBeanName())) {
      throw new IllegalArgumentException("bean name is null or empty.");
    }
    if (StringUtility.isNullOrEmpty(getBeanTypeSignature())) {
      throw new IllegalArgumentException("bean signature is null or empty.");
    }
    else {
      if (SignatureUtility.getTypeSignatureKind(getBeanTypeSignature()) != Signature.CLASS_TYPE_SIGNATURE) {
        throw new IllegalArgumentException("bean signature is not a class type signature.");
      }
    }
    if (getClientSession() == null) {
      throw new IllegalArgumentException("client session is null.");
    }
    if (getServerSession() == null) {
      throw new IllegalArgumentException("server session is null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (getServerSession() != null) {
      runServer(getServerSession(), monitor, workingCopyManager);
    }
    if (getClientSession() != null) {
      runClient(getClientSession(), monitor, workingCopyManager);
    }
  }

  protected void runServer(IType serverSession, IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
    manager.register(serverSession.getCompilationUnit(), monitor);
    IImportValidator validator = new CompilationUnitImportValidator(serverSession.getCompilationUnit());
    String beanReference = SignatureUtility.getTypeReference(getBeanTypeSignature(), serverSession, validator);
    String sigTypeSimpleName = beanReference.replaceAll("([^<]*).*", "$1");

    // setter
    StringBuilder sourceSetter = new StringBuilder();
    sourceSetter.append(methodFlagsToString());
    sourceSetter.append(" void set" + getBeanName(true) + "(");
    sourceSetter.append(beanReference);
    sourceSetter.append(" " + getBeanName(false) + "){\n" + SdkProperties.TAB);
    sourceSetter.append("setSharedContextVariable(\"" + getBeanName(false) + "\"," + sigTypeSimpleName + ".class," + getBeanName(false) + ");\n}\n");
    IMethod writeMethod = serverSession.createMethod(sourceSetter.toString(), getSiblingServerSession(), true, monitor);
    // getter
    StringBuilder sourceGetter = new StringBuilder();
    sourceGetter.append(methodFlagsToString());
    sourceGetter.append(" " + beanReference + " ");

    sourceGetter.append("get" + getBeanName(true) + "() {\n" + SdkProperties.TAB);
    sourceGetter.append("return getSharedContextVariable(\"" + getBeanName(false) + "\"," + sigTypeSimpleName + ".class);\n}\n");
    serverSession.createMethod(sourceGetter.toString(), writeMethod, true, monitor);
    // imports
    for (String imp : validator.getImportsToCreate()) {
      serverSession.getCompilationUnit().createImport(imp, null, monitor);
    }
  }

  protected void runClient(IType clientSession, IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
    manager.register(clientSession.getCompilationUnit(), monitor);
    IImportValidator validator = new CompilationUnitImportValidator(clientSession.getCompilationUnit());
    String beanReference = SignatureUtility.getTypeReference(getBeanTypeSignature(), clientSession, validator);
    String sigTypeSimpleName = beanReference.replaceAll("([^<]*).*", "$1");

    // getter
    StringBuilder sourceGetter = new StringBuilder();
    sourceGetter.append(methodFlagsToString());
    sourceGetter.append(" " + beanReference + " ");
    sourceGetter.append("get" + getBeanName(true) + "() {\n" + SdkProperties.TAB);
    sourceGetter.append("return getSharedContextVariable(\"" + getBeanName(false) + "\"," + sigTypeSimpleName + ".class);\n}\n");
    clientSession.createMethod(sourceGetter.toString(), getSiblingClientSession(), true, monitor);
    // imports
    for (String imp : validator.getImportsToCreate()) {
      clientSession.getCompilationUnit().createImport(imp, null, monitor);
    }
  }

  // protected String getBeanTypeName(IScoutType sessionType, boolean createImports, IProgressMonitor monitor) throws CoreException{
  // StringBuffer source=new StringBuffer();
  // if(m_beanType.getType()==IBcElementProposal.TYPE_PRIMITIVE){
  // source.append(((IPrimitiveTypeProposal)m_beanType).getPrimitiveName());
  // }
  // else if(m_beanType.getType()==IBcElementProposal.TYPE_BCTYPE){
  // IBCTypeProposal bcTypeProp=(IBCTypeProposal)m_beanType;
  // source.append(bcTypeProp.getBcType().getSimpleName());
  // sessionType.createImport(bcTypeProp.getBcType().getFullyQualifiedName(), monitor);
  // if(bcTypeProp.getBcType().isGenericType()&&getBeanGenericType()!=null){
  // source.append("<"+m_beanGenericType.getBcType().getSimpleName()+">");
  // sessionType.createImport(m_beanGenericType.getBcType().getFullyQualifiedName(), monitor);
  // }
  // }
  // return source.toString();
  // }

  public IType getClientSession() {
    return m_clientSession;
  }

  public void setClientSession(IType clientSession) {
    m_clientSession = clientSession;
  }

  public IType getServerSession() {
    return m_serverSession;
  }

  public void setServerSession(IType serverSession) {
    m_serverSession = serverSession;
  }

  protected String methodFlagsToString() {
    String methodFlagsString = " ";
    if ((getMethodFlags() & Flags.AccPrivate) != 0) {
      methodFlagsString = "private";
    }
    else if ((getMethodFlags() & Flags.AccPublic) != 0) {
      methodFlagsString = "public";
    }
    else if ((getMethodFlags() & Flags.AccProtected) != 0) {
      methodFlagsString = "protected";
    }
    if ((getMethodFlags() & Flags.AccAbstract) != 0) {
      methodFlagsString += " abstract";
    }
    if ((getMethodFlags() & Flags.AccStatic) != 0) {
      methodFlagsString += " static";
    }
    if ((getMethodFlags() & Flags.AccFinal) != 0) {
      methodFlagsString = " final";
    }
    return methodFlagsString;
  }

  @Override
  public String getBeanName(boolean startWithUpperCase) {
    if (StringUtility.isNullOrEmpty(getBeanName())) {
      return null;
    }
    if (startWithUpperCase) {
      return Character.toUpperCase(getBeanName().charAt(0)) + getBeanName().substring(1);
    }
    else {
      return Character.toLowerCase(getBeanName().charAt(0)) + getBeanName().substring(1);
    }
  }

  @Override
  public String getBeanName() {
    return m_beanName;
  }

  @Override
  public void setBeanName(String beanName) {
    m_beanName = beanName;
  }

  /**
   * @return a binary or combination of {@link Flags#AccAbstract}, {@link Flags#AccPrivate}, {@link Flags#AccProtected},
   *         {@link Flags#AccDefault}, {@link Flags#AccPublic}, {@link Flags#AccFinal}, {@link Flags#AccStatic}
   */
  @Override
  public int getMethodFlags() {
    return m_methodFlags;
  }

  /**
   * @param methodFlags
   *          a binary or combination of {@link Flags#AccAbstract}, {@link Flags#AccPrivate}, {@link Flags#AccProtected}
   *          , {@link Flags#AccDefault}, {@link Flags#AccPublic}, {@link Flags#AccFinal}, {@link Flags#AccStatic}
   */
  @Override
  public void setMethodFlags(int methodFlags) {
    m_methodFlags = methodFlags;
  }

  @Override
  public void setBeanTypeSignature(String beanTypeSignature) {
    m_beanTypeSignature = beanTypeSignature;
  }

  @Override
  public String getBeanTypeSignature() {
    return m_beanTypeSignature;
  }

  public void setSiblingClientSession(IJavaElement siblingClientSession) {
    m_siblingClientSession = siblingClientSession;
  }

  public IJavaElement getSiblingClientSession() {
    return m_siblingClientSession;
  }

  public void setSiblingServerSession(IJavaElement siblingServerSession) {
    m_siblingServerSession = siblingServerSession;
  }

  public IJavaElement getSiblingServerSession() {
    return m_siblingServerSession;
  }
}

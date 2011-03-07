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
package org.eclipse.scout.sdk.sql.binding;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.annotations.SqlBindingIgnoreValidation;
import org.eclipse.scout.commons.parsers.BindModel;
import org.eclipse.scout.commons.parsers.BindParser;
import org.eclipse.scout.commons.parsers.token.IToken;
import org.eclipse.scout.commons.parsers.token.ValueInputToken;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.jdt.ast.AstUtility;
import org.eclipse.scout.sdk.jdt.ast.VariableType;
import org.eclipse.scout.sdk.sql.binding.ast.SqlMethodIvocationVisitor;
import org.eclipse.scout.sdk.sql.binding.model.BindBaseNVPair;
import org.eclipse.scout.sdk.sql.binding.model.IBindBase;
import org.eclipse.scout.sdk.sql.binding.model.IgnoredBindBase;
import org.eclipse.scout.sdk.sql.binding.model.PropertyBasedBindBase;
import org.eclipse.scout.sdk.sql.binding.model.ServerSessionBindBase;
import org.eclipse.scout.sdk.sql.binding.model.SqlStatement;
import org.eclipse.scout.sdk.sql.binding.model.UnresolvedBindBase;
import org.eclipse.scout.sdk.workspace.type.MethodFilters;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.IPrimaryTypeTypeHierarchy;

/**
 * <h3>{@link FormDataSqlBindingValidator}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 24.02.2011
 */
public class FormDataSqlBindingValidator implements IRunnableWithProgress {

  private final IType[] m_processServices;
  private EventListenerList m_eventListeners;

  private HashMap<ICompilationUnit, CompilationUnit> m_astCache;

  public FormDataSqlBindingValidator(IType... processServices) {
    m_processServices = processServices;
    m_astCache = new HashMap<ICompilationUnit, CompilationUnit>();
    m_eventListeners = new EventListenerList();
  }

  public void addBindingValidateListener(ISqlFormDataBindingValidationListener listener) {
    m_eventListeners.add(ISqlFormDataBindingValidationListener.class, listener);
  }

  public void removeBindingValidateListener(ISqlFormDataBindingValidationListener listener) {
    m_eventListeners.remove(ISqlFormDataBindingValidationListener.class, listener);
  }

  @Override
  public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
    if (getProcessServices() != null) {
      for (IType t : getProcessServices()) {
        try {
          processService(t, monitor);
        }
        catch (Exception e) {
          ScoutSdk.logWarning("could not process service '" + t.getFullyQualifiedName() + "'.", e);
        }
      }
    }
  }

  protected void processService(IType service, IProgressMonitor monitor) throws JavaModelException {
    SqlBindingMarker.removeMarkers(service.getCompilationUnit());
    for (IMethod serviceMethod : service.getMethods()) {
      try {
        MethodSqlBindingModel processServiceMethod = processServiceMethod(serviceMethod, monitor);
        SqlBindingMarker.setMarkers(processServiceMethod);
      }
      catch (Exception e) {
        ScoutSdk.logWarning("could not process method '" + serviceMethod.getElementName() + "' on type '" + service.getFullyQualifiedName() + "'.", e);
      }
    }
    System.out.println("done");
  }

  protected MethodSqlBindingModel processServiceMethod(IMethod serviceMethod, IProgressMonitor monitor) throws JavaModelException {
    HashMap<String, IBindBase> globalBindings = new HashMap<String, IBindBase>();
    globalBindings.putAll(resolveServerSessionBindBases());
    IAnnotation ignoreBindAnnotation = TypeUtility.getAnnotation(serviceMethod, SqlBindingIgnoreValidation.class.getName());
    if (TypeUtility.exists(ignoreBindAnnotation)) {
      if (ignoreBindAnnotation.getSource().startsWith("@")) {
        for (IMemberValuePair p : ignoreBindAnnotation.getMemberValuePairs()) {
          String memberName = p.getMemberName();
          Object value = p.getValue();
          if ("value".equals(memberName) && p.getValueKind() == IMemberValuePair.K_STRING) {
            if (value instanceof Object[]) {
              Object[] values = (Object[]) value;
              for (Object v : values) {
                globalBindings.put((String) v, new IgnoredBindBase((String) v));
              }
            }
            else {
              globalBindings.put((String) value, new IgnoredBindBase((String) value));
            }
          }
        }
      }
    }
    MethodSqlBindingModel result = new MethodSqlBindingModel(serviceMethod);
    ASTNode methodNode = createMethodAst(serviceMethod);
    SqlMethodIvocationVisitor visitor = new SqlMethodIvocationVisitor(methodNode, serviceMethod);
    methodNode.accept(visitor);
    SqlStatement[] statements = visitor.getStatements();
    for (SqlStatement s : statements) {
      UnresolvedBindBase[] unresolvedBindBases = s.getUnresolvedBindBases();
      StringBuilder unresolvedBindings = new StringBuilder();
      for (int i = 0; i < unresolvedBindBases.length; i++) {
        unresolvedBindings.append(unresolvedBindBases[i].toString());
        if (i < unresolvedBindBases.length - 1) {
          unresolvedBindings.append(", ");
        }
      }
      HashMap<String, IBindBase> bindBases = new HashMap<String, IBindBase>(globalBindings);
      bindBases.putAll(resolveBindBases(s));

      boolean hasUnresolvedBindBases = unresolvedBindBases.length > 0;

      BindParser parser = new BindParser(s.buildStatement());
      BindModel bindModel = parser.parse();
      for (IToken t : bindModel.getAllTokens()) {
        if (t instanceof ValueInputToken) {
          String token = t.getParsedToken().toLowerCase();
          token = token.replaceAll("^([\\:\\{\\#]*)?([^\\}\\#]*)([\\}\\#]*)?", "$2");
          if (!bindBases.containsKey(token)) {
            // try dot seperated token
            if (token.contains(".")) {
              String[] segments = token.split("\\.");
              IBindBase property = bindBases.get(segments[0]);
              if (property.getType() == IBindBase.TYPE_NVPAIR) {
                // resolve
                HashMap<String, IBindBase> resolvedBinds = loadNVBindBasePropertyObject((BindBaseNVPair) property, methodNode, serviceMethod);
                bindBases.putAll(resolvedBinds);
                if (bindBases.containsKey(token)) {
                  continue;
                }
              }
            }
            if (hasUnresolvedBindBases) {
              result.add(t.getParsedToken(), new MethodSqlBindingModel.Marker(token, IMarker.SEVERITY_WARNING, s.getOffset(), s.getLength(), serviceMethod));
            }
            else {
              result.add(t.getParsedToken(), new MethodSqlBindingModel.Marker(token, IMarker.SEVERITY_ERROR, s.getOffset(), s.getLength(), serviceMethod));
            }
          }
        }
      }
    }
    return result;
  }

  protected HashMap<String, IBindBase> resolveServerSessionBindBases() {
    HashMap<String, IBindBase> bindBases = new HashMap<String, IBindBase>();
    // server sessions
    IType iServerSession = ScoutSdk.getType(RuntimeClasses.IServerSession);
    IPrimaryTypeTypeHierarchy serverSessionHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iServerSession);
    for (IType serverSession : serverSessionHierarchy.getAllSubtypes(iServerSession, TypeFilters.getClassFilter())) {
      for (IMethod m : TypeUtility.getMethods(serverSession, MethodFilters.getNameRegexFilter("get.*"))) {
        String methodName = m.getElementName().toLowerCase();
        methodName = methodName.replaceFirst("^(get|set|is)", "");
        bindBases.put(methodName, new ServerSessionBindBase(methodName, serverSession));
      }
    }
    return bindBases;
  }

  protected HashMap<String, IBindBase> resolveBindBases(SqlStatement statement) {
    HashMap<String, IBindBase> bindBases = new HashMap<String, IBindBase>();
    for (IBindBase b : statement.getBindBases()) {
      switch (b.getType()) {
        case IBindBase.TYPE_NVPAIR:
          BindBaseNVPair nvBind = (BindBaseNVPair) b;
          bindBases.put(nvBind.getBindVar().toLowerCase(), nvBind);
          break;
        case IBindBase.TYPE_PROPERTY_BASE:
          bindBases.putAll(resolveBindVariables(((PropertyBasedBindBase) b).getAssignedSignatures(), b, null));
          break;
        default:
          break;
      }
    }
    return bindBases;
  }

  protected HashMap<String, IBindBase> resolveBindVariables(String[] assignedSignatures, IBindBase bindBase, String keyPrefix) {
    if (keyPrefix == null) {
      keyPrefix = "";
    }
    HashMap<String, IBindBase> bindBases = new HashMap<String, IBindBase>();
    for (String assignedSignature : assignedSignatures) {
      HashSet<String> vars = getPropertyBindVars(ScoutSdk.getTypeBySignature(assignedSignature));
      if (!bindBases.isEmpty()) {
        HashMap<String, IBindBase> existingVars = bindBases;
        bindBases = new HashMap<String, IBindBase>();
        for (String s : vars) {
          if (existingVars.containsKey(s)) {
            bindBases.put(keyPrefix + s, bindBase);
          }
        }
      }
      else {
        for (String s : vars) {
          bindBases.put(keyPrefix + s, bindBase);

        }
      }
    }
    return bindBases;
  }

  protected HashMap<String, IBindBase> loadNVBindBasePropertyObject(BindBaseNVPair bindBase, ASTNode rootNode, IJavaElement containerElement) {
    VariableType var = AstUtility.getTypeSignature(bindBase.getValueNode(), rootNode, containerElement);
    if (var != null) {
      String[] assignedSignatures = var.getAssignedTypeSignatures();
      if (assignedSignatures.length > 0) {
        return resolveBindVariables(assignedSignatures, bindBase, bindBase.getBindVar() + ".");
      }
      else if (var.getTypeSignature() != null) {
        return resolveBindVariables(new String[]{var.getTypeSignature()}, bindBase, bindBase.getBindVar() + ".");
      }
    }
    return new HashMap<String, IBindBase>(0);

  }

  protected HashSet<String> getPropertyBindVars(IType type) {
    HashSet<String> bindVars = new HashSet<String>();
    if (TypeUtility.exists(type)) {
      for (IMethod m : TypeUtility.getMethods(type)) {
        String propName = m.getElementName().toLowerCase();
        propName = propName.replaceFirst("property$", "");
        propName = propName.replaceFirst("^(get|set|is)", "");
        bindVars.add(propName);
      }
    }
    return bindVars;
  }

  protected ASTNode createMethodAst(IMethod method) throws JavaModelException {
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setCompilerOptions(JavaCore.getOptions());
    parser.setBindingsRecovery(true);
    parser.setResolveBindings(true);
    parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
    parser.setSource(method.getSource().toCharArray());
    return parser.createAST(null);
  }

  protected CompilationUnit getCachedAst(ICompilationUnit icu) {
    CompilationUnit ast = m_astCache.get(icu);
    if (ast == null) {
      ASTParser parser = ASTParser.newParser(AST.JLS3);
      parser.setCompilerOptions(JavaCore.getOptions());
      parser.setKind(ASTParser.K_COMPILATION_UNIT);
      parser.setSource(icu);
      ast = (CompilationUnit) parser.createAST(null);
      m_astCache.put(icu, ast);
    }
    return ast;
  }

  /**
   * @return the processServices
   */
  public IType[] getProcessServices() {
    return m_processServices;
  }

}

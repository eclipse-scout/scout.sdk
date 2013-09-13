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
package org.eclipse.scout.sdk.util.ast.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.scout.sdk.util.ast.AstUtility;
import org.eclipse.scout.sdk.util.ast.VariableType;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;

/**
 * <h3>{@link TypeSignatureResolveVisitor}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 27.02.2011
 */
public class TypeSignatureResolveVisitor extends DefaultAstVisitor {

  private boolean m_debug = false;
  private boolean m_canceled;
  public String m_indent = "";

  private final IJavaElement m_containerElement;
  private final ASTNode m_rootNode;
  private int m_mode;
  private HashSet<String> m_assignedSignatures;
  private String m_typeSignature;
  private P_MethodInvocation m_methodInvocation;

  public TypeSignatureResolveVisitor(ASTNode rootNode, IJavaElement containerElement) {
    m_rootNode = rootNode;
    m_containerElement = containerElement;
    m_assignedSignatures = new HashSet<String>();
  }

  @Override
  public void preVisit(ASTNode node) {
    if (m_debug && !m_canceled) {
      SdkUtilActivator.logInfo(m_indent + "typeSigResolve " + node.getNodeType() + "  " + node + "     ");
      m_indent += "  ";
    }
  }

  @Override
  public void postVisit(ASTNode node) {
    if (m_debug && !m_canceled) {
      m_indent = m_indent.replaceFirst("\\s\\s$", "");
      SdkUtilActivator.logInfo(m_indent + "end " + node.getNodeType());
    }
  }

  @Override
  public boolean visitNode(ASTNode node) {
    return !m_canceled;
  }

  @Override
  public boolean visit(ClassInstanceCreation node) {
    if (m_canceled) {
      return false;
    }
    m_mode = node.getNodeType();
    return super.visit(node);
  }

  @Override
  public void endVisit(ClassInstanceCreation node) {
    if (m_mode == node.getNodeType()) {
      m_mode = -1;
      m_canceled = true;
    }
  }

  @Override
  public boolean visit(MethodInvocation node) {
    if (m_canceled) {
      return false;
    }
    m_methodInvocation = new P_MethodInvocation();
    m_mode = node.getNodeType();
    return super.visit(node);
  }

  @Override
  public void endVisit(MethodInvocation node) {
    if (m_mode == node.getNodeType()) {
      if (m_methodInvocation != null) {
        List<SimpleName> segments = m_methodInvocation.m_segments;
        ArrayList<String> fqSignatures = new ArrayList<String>();
        if (segments.size() > 1) {
          VariableType var = AstUtility.getTypeSignature(segments.get(0), m_rootNode, m_containerElement);
          if (var.getAssignedTypeSignatures().length > 0) {
            fqSignatures.addAll(Arrays.asList(var.getAssignedTypeSignatures()));
          }
          else if (var.getTypeSignature() != null) {
            fqSignatures.add(var.getTypeSignature());
          }
          for (int i = 1; i < segments.size(); i++) {
            ArrayList<String> newList = new ArrayList<String>();
            for (String sig : fqSignatures) {
              String newSig = AstUtility.resolveReturnValueSignature(sig, segments.get(i).getFullyQualifiedName());
              newList.add(newSig);
            }
            fqSignatures = newList;
          }
        }
        else if (segments.size() == 1) {
          IType declaringType = JdtUtility.findDeclaringType(m_containerElement);
          String resolveReturnValueSignature = AstUtility.resolveReturnValueSignature(SignatureCache.createTypeSignature(declaringType.getFullyQualifiedName()), node.getName().getFullyQualifiedName());
          m_typeSignature = resolveReturnValueSignature;
        }
        m_assignedSignatures.addAll(fqSignatures);
        m_canceled = true;
        m_methodInvocation = null;
      }
      m_mode = -1;
      m_canceled = true;
    }
  }

  @Override
  public boolean visit(SimpleType node) {
    if (m_canceled) {
      return false;
    }
    switch (m_mode) {
      case ASTNode.CLASS_INSTANCE_CREATION:

        String signature = Signature.createTypeSignature(node.getName().getFullyQualifiedName(), false);
        IType declaringType = JdtUtility.findDeclaringType(m_containerElement);
        try {
          String resolvedSignature = SignatureUtility.getResolvedSignature(signature, declaringType);
          if (resolvedSignature != null) {
            m_typeSignature = resolvedSignature;
          }
        }
        catch (CoreException e) {
          SdkUtilActivator.logError("could not resolve class instance creation of '" + signature + "' in '" + declaringType.getFullyQualifiedName() + "'.");
        }
        m_canceled = true;
        return false;
      case ASTNode.METHOD_INVOCATION:

        return false;
      default:
        break;
    }
    return super.visit(node);
  }

  @Override
  public boolean visit(SimpleName node) {
    if (m_canceled) {
      return false;
    }
    switch (m_mode) {
      case ASTNode.METHOD_INVOCATION:
        if (m_methodInvocation != null) {
          m_methodInvocation.m_segments.add(node);
        }
//        if (m_typeSignature == null) {
//          IType declaringType = JdtUtility.findDeclaringType(m_containerElement);
//          String resolveReturnValueSignature = AstUtility.resolveReturnValueSignature(SignatureCache.createTypeSignature(declaringType.getFullyQualifiedName()), node.getFullyQualifiedName());
//          m_typeSignature = resolveReturnValueSignature;
//        }
//        else {
//          String resolveReturnValueSignature = AstUtility.resolveReturnValueSignature(m_typeSignature, node.getFullyQualifiedName());
//          m_typeSignature = resolveReturnValueSignature;
//        }
        return false;

      default:
        if (!m_assignedSignatures.isEmpty() || m_typeSignature != null) {
          HashSet<String> newSet = new HashSet<String>();
          if (!m_assignedSignatures.isEmpty()) {
            for (String s : m_assignedSignatures) {
              newSet.add(AstUtility.resolveReturnValueSignature(s, node.getFullyQualifiedName()));
            }
          }
          else if (m_typeSignature != null) {
            newSet.add(AstUtility.resolveReturnValueSignature(m_typeSignature, node.getFullyQualifiedName()));
          }
          m_typeSignature = null;
          m_assignedSignatures.clear();
          m_assignedSignatures.addAll(newSet);
        }
        else {
          VariableResolveVisitor visitor = new VariableResolveVisitor(node.getFullyQualifiedName(), m_containerElement, m_rootNode, node);
          m_rootNode.accept(visitor);
          String[] assignedTypesSignatures = visitor.getAssignedTypesSignatures();
          if (assignedTypesSignatures.length > 0) {
            m_assignedSignatures.addAll(Arrays.asList(assignedTypesSignatures));
          }
          else if (visitor.getVariableTypeSignature() != null) {
            m_typeSignature = visitor.getVariableTypeSignature();
          }
        }
        break;
    }

    return super.visit(node);
  }

  /**
   * @return the assignedSignatures
   */
  public String[] getAssignedSignatures() {
    return m_assignedSignatures.toArray(new String[m_assignedSignatures.size()]);
  }

  /**
   * @return the typeSignature
   */
  public String getTypeSignature() {
    return m_typeSignature;
  }

  private class P_MethodInvocation {
    private List<SimpleName> m_segments = new ArrayList<SimpleName>();
  }
}

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
package org.eclipse.scout.sdk.sql.binding.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.sql.binding.model.BindBaseNVPair;
import org.eclipse.scout.sdk.sql.binding.model.IBindBase;
import org.eclipse.scout.sdk.sql.binding.model.PropertyBasedBindBase;
import org.eclipse.scout.sdk.util.ast.AstUtility;
import org.eclipse.scout.sdk.util.ast.VariableType;
import org.eclipse.scout.sdk.util.ast.visitor.DefaultAstVisitor;

/**
 * <h3>{@link BindBasesVisitor}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 25.02.2011
 */
public class BindBasesVisitor extends DefaultAstVisitor {

  private List<IBindBase> m_bindBases;
  private PropertyBasedBindBase m_currentBase;
  private final ASTNode m_rootNode;
  private final IMethod m_serviceMethod;

  public BindBasesVisitor(IMethod serviceMethod, ASTNode rootNode) {
    m_serviceMethod = serviceMethod;
    m_rootNode = rootNode;
    m_bindBases = new ArrayList<IBindBase>();
  }

  @Override
  public boolean visit(SimpleName node) {
    if (m_currentBase == null) {
      VariableType resolveVariable = AstUtility.resolveVariable(node.getFullyQualifiedName(), m_serviceMethod, m_rootNode, node);
      String[] assignedTypeSignatures = resolveVariable.getAssignedTypeSignatures();
      if (assignedTypeSignatures.length > 0) {
        PropertyBasedBindBase bindBase = new PropertyBasedBindBase();
        bindBase.addAssignedSignatures(assignedTypeSignatures);
        m_bindBases.add(bindBase);
      }
      else if (resolveVariable.getTypeSignature() != null) {
        PropertyBasedBindBase bindBase = new PropertyBasedBindBase();
        bindBase.addAssignedSignature(resolveVariable.getTypeSignature());
        m_bindBases.add(bindBase);
      }
    }
    else {
      if (m_currentBase.getAssignedSignatures().length == 0) {
        VariableType resolveVariable = AstUtility.resolveVariable(node.getFullyQualifiedName(), m_serviceMethod, m_rootNode, node);
        String[] assignedTypeSignatures = resolveVariable.getAssignedTypeSignatures();
        if (assignedTypeSignatures.length > 0) {
          PropertyBasedBindBase bindBase = new PropertyBasedBindBase();
          bindBase.addAssignedSignatures(assignedTypeSignatures);
          m_currentBase = bindBase;
        }
        else if (resolveVariable.getTypeSignature() != null) {
          PropertyBasedBindBase bindBase = new PropertyBasedBindBase();
          bindBase.addAssignedSignature(resolveVariable.getTypeSignature());
          m_currentBase = bindBase;
        }
      }
      else {
        ArrayList<String> resolvedAssignedSignatures = new ArrayList<String>();
        for (String assignedSignature : m_currentBase.getAssignedSignatures()) {
          String resolveReturnValueSignature = AstUtility.resolveReturnValueSignature(assignedSignature, node.getFullyQualifiedName());
          if (!StringUtility.isNullOrEmpty(resolveReturnValueSignature)) {
            resolvedAssignedSignatures.add(resolveReturnValueSignature);
          }
        }
        PropertyBasedBindBase base = new PropertyBasedBindBase();
        base.addAssignedSignatures(resolvedAssignedSignatures.toArray(new String[resolvedAssignedSignatures.size()]));
        m_currentBase = base;
      }
    }
//    if (m_currentBase != null) {
//      if (m_currentBase instanceof BindBaseFormData) {
//        BindBaseFormData bindBase = (BindBaseFormData) m_currentBase;
//        // if first check availability
//        if (!bindBase.hasSegments()) {
//          VariableType resolveVariable = AstUtility.resolveVariable(node.getFullyQualifiedName(), m_serviceMethod, m_rootNode, node);
//          if (m_formDataNames.contains(node.getFullyQualifiedName())) {
//            bindBase.addSegment(node.getFullyQualifiedName());
//          }
//          else {
//            // resolve
//            ReferencedBindBaseVisitor referencedBindBaseVisitor = new ReferencedBindBaseVisitor(node.getFullyQualifiedName(), m_rootNode, node, m_formDataNames);
//            m_rootNode.accept(referencedBindBaseVisitor);
//            m_bindBases.add(referencedBindBaseVisitor.getBindBase());
//          }
//        }
//        else {
//          bindBase.addSegment(node.getFullyQualifiedName());
//        }
//      }
//    }
//    else {
//      if (m_formDataNames.contains(node.getFullyQualifiedName())) {
//        m_bindBases.add(new BindBaseFormData(node.getFullyQualifiedName()));
//      }
//      else {
//        // resolve
//        ReferencedBindBaseVisitor referencedBindBaseVisitor = new ReferencedBindBaseVisitor(node.getFullyQualifiedName(), m_rootNode, node, m_formDataNames);
//        m_rootNode.accept(referencedBindBaseVisitor);
//        m_bindBases.add(referencedBindBaseVisitor.getBindBase());
//      }
//    }
    return false;
  }

  @Override
  public boolean visit(MethodInvocation node) {
    m_currentBase = new PropertyBasedBindBase();
    return true;
  }

  @Override
  public void endVisit(MethodInvocation node) {
    if (m_currentBase != null) {
      if (m_currentBase.getAssignedSignatures().length > 0) {
        m_bindBases.add(m_currentBase);
      }
    }

  }

  @Override
  public boolean visit(ClassInstanceCreation node) {
    String source = node.toString();
    Matcher nvPairMatcher = Pattern.compile("new\\s*(org\\.eclipse\\.scout\\.commons\\.holders)?NVPair\\s*\\(\\s*\\\"([^\\\"]*)\\\"").matcher(source);
    if (nvPairMatcher.find()) {
      NVPairBindVisitor subVisitor = new NVPairBindVisitor(m_rootNode);
      node.accept(subVisitor);
      BindBaseNVPair subBindBase = subVisitor.getBindBase();
      m_bindBases.add(subBindBase);
      return false;
    }
    Matcher tableHolderMatcher = Pattern.compile("new\\s*(org\\.eclipse\\.scout\\.commons\\.holders\\.)?TableHolderFilter").matcher(source);
    if (tableHolderMatcher.find()) {
      TableHolderBindVisitor subVisitor = new TableHolderBindVisitor(m_rootNode, m_serviceMethod);
      node.accept(subVisitor);
      PropertyBasedBindBase bindBase = subVisitor.getBindBase();
      if (bindBase != null) {
        m_bindBases.add(bindBase);
      }
      return false;
    }

    // try property holder
    VariableType var = AstUtility.getTypeSignature(node, m_rootNode, m_serviceMethod);
    if (var != null) {
      PropertyBasedBindBase base = new PropertyBasedBindBase();

      String[] assignedTypeSignatures = var.getAssignedTypeSignatures();
      if (assignedTypeSignatures.length > 0) {
        base.addAssignedSignatures(assignedTypeSignatures);
      }
      else if (var.getTypeSignature() != null) {
        base.addAssignedSignature(var.getTypeSignature());
      }
      if (base.getAssignedSignatures().length > 0) {
        m_bindBases.add(base);
      }
    }
    //  new TableHolderFilter(formData.getParticipantTable(), ITableHolder.STATUS_DELETED)

    // handle new FormData
    return false;
  }

  /**
   * @return the bindBases
   */
  public IBindBase[] getBindBases() {
    return m_bindBases.toArray(new IBindBase[m_bindBases.size()]);
  }
}

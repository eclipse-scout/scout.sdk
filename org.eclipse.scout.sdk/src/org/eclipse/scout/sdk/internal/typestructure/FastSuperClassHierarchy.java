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
package org.eclipse.scout.sdk.internal.typestructure;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdk;

/**
 *
 */
public class FastSuperClassHierarchy {
  private P_HierarchyNode rootNode;

  private FastSuperClassHierarchy(IType type) {
    rootNode = new P_HierarchyNode(type);
    try {
      createNodesReq(rootNode);
    }
    catch (JavaModelException e) {
      ScoutSdk.logError("could not create fast super type hierarchy of '" + type.getFullyQualifiedName() + "'.", e);
    }
  }

  private void createNodesReq(P_HierarchyNode node) throws JavaModelException {
    // superClasses
    try {
      IType superClass = getTypeFromSignature(node.getType().getSuperclassTypeSignature(), node.getType());
      if (superClass != null) {
        P_HierarchyNode superClassNode = new P_HierarchyNode(superClass);
        node.setSuperClass(superClassNode);
        createNodesReq(superClassNode);
      }
    }
    catch (JavaModelException e) {
      ScoutSdk.logWarning("could not find superclass of '" + node.getType().getFullyQualifiedName() + "'.", e);
    }
    // interfaces
    for (String interfaceSig : node.getType().getSuperInterfaceTypeSignatures()) {
      try {
        IType interfaceType = getTypeFromSignature(interfaceSig, node.getType());
        if (interfaceType != null) {
          P_HierarchyNode interfaceNode = new P_HierarchyNode(interfaceType);
          node.addInterface(interfaceNode);
          createNodesReq(interfaceNode);
        }
      }
      catch (JavaModelException e) {
        ScoutSdk.logWarning("could not find superclass of '" + node.getType().getFullyQualifiedName() + "'.", e);
      }
    }
  }

  private IType getTypeFromSignature(String signature, IType contextType) throws JavaModelException {
    if (StringUtility.isNullOrEmpty(signature)) {
      return null;
    }
    if (Signature.getSignatureSimpleName(signature).equals("Object")) {
      return null;
    }
    String fqName = null;
    if (signature.startsWith("Q")) {
      // unresolved
      String[][] resolvedTypeName = contextType.resolveType(Signature.getSignatureSimpleName(signature));
      if (resolvedTypeName != null && resolvedTypeName.length == 1) {
        fqName = resolvedTypeName[0][0];
        if (fqName != null && fqName.length() > 0) {
          fqName = fqName + ".";
        }
        fqName = fqName + resolvedTypeName[0][1];
        signature = Signature.createTypeSignature(fqName, true);
      }
    }
    else {
      fqName = Signature.getSignatureQualifier(signature) + "." + Signature.getSignatureSimpleName(signature);
    }
    return ScoutSdk.getType(fqName);
  }

  public boolean containsSupertype(IType type) {
    ArrayList<P_HierarchyNode> nodesToVisit = new ArrayList<P_HierarchyNode>();
    nodesToVisit.add(rootNode);
    while (!nodesToVisit.isEmpty()) {
      ArrayList<P_HierarchyNode> tempVisitees = new ArrayList<P_HierarchyNode>();
      for (Iterator<P_HierarchyNode> it = nodesToVisit.iterator(); it.hasNext();) {
        P_HierarchyNode current = it.next();
        it.remove();
        P_HierarchyNode superClass = current.getSuperClass();
        if (superClass != null && superClass.getType().equals(type)) {
          return true;
        }
        tempVisitees.add(superClass);
        for (P_HierarchyNode interfaceNode : current.getInterfaces()) {
          if (interfaceNode.getType().equals(type)) {
            return true;
          }
          tempVisitees.add(interfaceNode);
        }
      }
      nodesToVisit = tempVisitees;
    }
    return false;
  }

  public static FastSuperClassHierarchy createFastSuperHierarchy(IType t) {
    return new FastSuperClassHierarchy(t);
  }

  private class P_HierarchyNode {
    private IType m_type;
    private P_HierarchyNode m_superClass;
    private ArrayList<P_HierarchyNode> m_interfaces = new ArrayList<P_HierarchyNode>();

    public P_HierarchyNode(IType t) {
      m_type = t;
    }

    public IType getType() {
      return m_type;
    }

    public P_HierarchyNode getSuperClass() {
      return m_superClass;
    }

    public void setSuperClass(P_HierarchyNode superClass) {
      m_superClass = superClass;
    }

    public void addInterface(P_HierarchyNode interfaceNode) {
      m_interfaces.add(interfaceNode);
    }

    public P_HierarchyNode[] getInterfaces() {
      return m_interfaces.toArray(new P_HierarchyNode[m_interfaces.size()]);
    }

  }
}

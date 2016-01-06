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
package org.eclipse.scout.sdk.s2e.util.ast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.util.IFilter;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

/**
 * <h3>{@link AstUtils}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public final class AstUtils {
  private AstUtils() {
  }

  /**
   * Gets all children of the given {@link ASTNode}.
   *
   * @param node
   *          The parent node.
   * @return A {@link Deque} holding the children.
   */
  public static Deque<ASTNode> getChildren(ASTNode node) {
    P_ChildrenCollector visitor = new P_ChildrenCollector();
    node.accept(visitor);
    return visitor.m_result;
  }

  /**
   * Gets the IType from an {@link AbstractTypeDeclaration}.
   *
   * @param td
   *          The type declaration for which the {@link IType} should be returned.
   * @return The {@link IType} or <code>null</code> if no {@link IType} could be resolved.
   */
  public static IType getTypeBinding(AbstractTypeDeclaration td) {
    ITypeBinding binding = td.resolveBinding();
    if (binding == null) {
      return null;
    }
    IJavaElement javaElement = binding.getJavaElement();
    if (!S2eUtils.exists(javaElement) || javaElement.getElementType() != IJavaElement.TYPE) {
      return null;
    }
    return (IType) javaElement;
  }

  /**
   * Gets the binding resolver instance of the givn {@link AST}.
   *
   * @param ast
   *          The {@link AST} for which the binding resolver should be extracted.
   * @return The binding resolver
   * @throws SdkException
   *           if it could not be extracted
   */
  public static Object getBindingResolver(AST ast) {
    try {
      Method getBindingResolver = AST.class.getDeclaredMethod("getBindingResolver");
      getBindingResolver.setAccessible(true);
      return getBindingResolver.invoke(ast);
    }
    catch (Throwable t) {
      throw new SdkException(t);
    }
  }

  /**
   * Gets the {@link CompilationUnitScope} of the given {@link AST} using the given binding resolver (see
   * {@link #getBindingResolver(AST)}.
   *
   * @param ast
   *          The {@link AST} for which to return the {@link CompilationUnitScope}.
   * @param resolver
   *          The resolver of the {@link AST}
   * @return the {@link CompilationUnitScope}.
   * @throws SdkException
   *           if there was an error accessing the {@link CompilationUnitScope}.
   */
  public static CompilationUnitScope getCompilationUnitScope(AST ast, Object resolver) {
    try {
      Method scope = resolver.getClass().getDeclaredMethod("scope");
      scope.setAccessible(true);
      return (CompilationUnitScope) scope.invoke(resolver);
    }
    catch (Throwable t) {
      throw new SdkException(t);
    }
  }

  /**
   * Adds the given {@link Annotation} to the given body.
   *
   * @param a
   *          The {@link Annotation} to add.
   * @param owner
   *          The owner where the {@link Annotation} should be added.
   */
  @SuppressWarnings("unchecked")
  public static void addAnnotationTo(Annotation a, BodyDeclaration owner) {
    ASTNode sibling = getAnnotationSibling(owner, a);
    if (sibling != null) {
      int insertPos = owner.modifiers().indexOf(sibling);
      owner.modifiers().add(insertPos, a);
    }
    else {
      owner.modifiers().add(a);
    }
  }

  /**
   * Gets all declaring {@link TypeDeclaration}s of the given {@link TypeDeclaration}. The given start declaration is
   * also part of the resuling {@link Deque} and can be found at the first position. The {@link Deque} holds the
   * declaring types from inside out (meaning the primary type can be accessed using {@link Deque#getLast()}).
   *
   * @param start
   *          The start type declaration
   * @return A {@link Deque} with all declaring types
   */
  public static Deque<TypeDeclaration> getDeclaringTypes(TypeDeclaration start) {
    Deque<TypeDeclaration> result = new LinkedList<>();
    if (start == null) {
      return result;
    }
    result.add(start);
    TypeDeclaration parent = start;
    while ((parent = (TypeDeclaration) getParent(parent, ASTNode.TYPE_DECLARATION)) != null) {
      result.add(parent);
    }
    return result;
  }

  /**
   * Gets all {@link Annotation} on the given {@link BodyDeclaration}.
   *
   * @param owner
   *          The owner of the {@link Annotation}s.
   * @return All {@link Annotation}s on the given owner.
   */
  public static Deque<Annotation> getAnnotations(BodyDeclaration owner) {
    Deque<Annotation> annotations = new LinkedList<>();
    for (Object o : owner.modifiers()) {
      if (o instanceof Annotation) {
        annotations.add((Annotation) o);
      }
    }
    return annotations;
  }

  /**
   * Returns a unique identifier of a method. The identifier looks like 'methodname(param1Signature,param2Signature)'.
   *
   * @param md
   *          The method for which the identifier should be created
   * @return The created identifier
   */
  @SuppressWarnings("unchecked")
  public static String createMethodIdentifier(MethodDeclaration md) {
    List<SingleVariableDeclaration> parameters = md.parameters();
    List<String> paramSigs = new ArrayList<>(parameters.size());
    for (SingleVariableDeclaration svd : parameters) {
      Type type = svd.getType();
      String sig = Signature.createTypeSignature(type.toString());
      paramSigs.add(sig);
    }
    return SignatureUtils.createMethodIdentifier(md.getName().getIdentifier(), paramSigs);
  }

  /**
   * Gets the sibling where the given {@link Annotation} should be added to the given {@link BodyDeclaration}. The
   * {@link Annotation} must be added before the returned node.
   *
   * @param owner
   *          The owner of the {@link Annotation}.
   * @param newAnnotation
   *          The new {@link Annotation} that should be added.
   * @return The
   */
  public static ASTNode getAnnotationSibling(BodyDeclaration owner, Annotation newAnnotation) {
    Deque<Annotation> annotations = getAnnotations(owner);
    if (!annotations.isEmpty()) {
      int newAnnotLen = newAnnotation.toString().length();
      Iterator<Annotation> iterator = annotations.descendingIterator();
      while (iterator.hasNext()) {
        Annotation existingAnnotation = iterator.next();
        int len = existingAnnotation.getLength();
        if (len > 0 && len >= newAnnotLen) {
          return existingAnnotation;
        }
      }
    }
    for (Object o : owner.modifiers()) {
      if (o instanceof Modifier) {
        return (Modifier) o;
      }
    }
    return null;
  }

  /**
   * Checks if the given {@link ITypeBinding} is <code>instanceof</code> the given fully qualified name.
   *
   * @param hierarchyType
   *          The type to check.
   * @param fqn
   *          The fully qualified name to search in the super hierarchy of the given type.
   * @return <code>true</code> if the given name is in the super hierarchy of the given type.
   */
  public static boolean isInstanceOf(ITypeBinding hierarchyType, String fqn) {
    if (hierarchyType == null) {
      return false;
    }
    if (fqn == null) {
      return false;
    }
    P_InstanceOfBindingVisitor visitor = new P_InstanceOfBindingVisitor(fqn);
    visitHierarchy(hierarchyType, visitor);

    return visitor.m_isFound;
  }

  /**
   * Method to visit a type hierarchy defined by a given type. The given type itself is not visited.
   *
   * @param type
   *          the type whose hierarchy is to be visited
   * @param visitor
   *          the visitor
   * @return <code>true</code> if all types were visited, or <code>false</code> if the visiting got aborted because the
   *         <code>visit</code> method returned <code>false</code> for a type
   */
  public static boolean visitHierarchy(ITypeBinding type, ITypeBindingVisitor visitor) {
    Set<ITypeBinding> visited = new HashSet<>();
    return visitBindingRec(type, visitor, visited);
  }

  private static boolean visitBindingRec(ITypeBinding type, ITypeBindingVisitor visitor, Set<ITypeBinding> visited) {
    boolean unvisited = visited.add(type);
    if (!unvisited) {
      return true;
    }

    if (!visitor.visit(type)) {
      return false;
    }

    ITypeBinding superclass = type.getSuperclass();
    if (superclass != null && !visitBindingRec(superclass, visitor, visited)) {
      return false;
    }

    for (ITypeBinding ifc : type.getInterfaces()) {
      if (!visitBindingRec(ifc, visitor, visited)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Returns the closest ancestor of <code>node</code> whose type is <code>nodeType</code>, or <code>null</code> if
   * none.
   * <p>
   * <b>Warning:</b> This method does not stop at any boundaries like parentheses, statements, body declarations, etc.
   * The resulting node may be in a totally different scope than the given node. Consider using one of the
   * {@link ASTResolving}<code>.find(..)</code> methods instead.
   * </p>
   *
   * @param node
   *          the node
   * @param nodeType
   *          the node type constant from {@link ASTNode}
   * @return the closest ancestor of <code>node</code> whose type is <code>nodeType</code>, or <code>null</code> if none
   */
  public static ASTNode getParent(ASTNode node, int nodeType) {
    do {
      node = node.getParent();
    }
    while (node != null && node.getNodeType() != nodeType);
    return node;
  }

  /**
   * Gets the next node after the given position inside the given node that fulfills the given filter.
   *
   * @param n
   *          The parent nodes whose children are checked.
   * @param pos
   *          The position. The next node after this position is returned.
   * @param filter
   *          The filter that the node must fulfill or <code>null</code> if no additional filter is required.
   * @return The next node after the given pos or <code>null</code> if there is no such node.
   */
  public static ASTNode getNextNode(ASTNode n, int pos, IFilter<ASTNode> filter) {
    return getSiblingNode(n, pos, filter, false);
  }

  /**
   * Gets the last node before the given position inside the given node that fulfills the given filter.
   *
   * @param n
   *          The parent nodes whose children are checked.
   * @param pos
   *          The position. The node before this position is returned.
   * @param filter
   *          The filter that the node must fulfill or <code>null</code> if no additional filter is required.
   * @return The last node before the given pos or <code>null</code> if there is no such node.
   */
  public static ASTNode getPreviousNode(ASTNode n, int pos, IFilter<ASTNode> filter) {
    return getSiblingNode(n, pos, filter, true);
  }

  private static ASTNode getSiblingNode(ASTNode n, int pos, IFilter<ASTNode> filter, boolean prev) {
    Deque<ASTNode> children = getChildren(n);
    Iterator<ASTNode> iterator = null;
    if (prev) {
      iterator = children.descendingIterator();
    }
    else {
      iterator = children.iterator();
    }

    while (iterator.hasNext()) {
      ASTNode currentNode = iterator.next();
      int endOfCurrentNode = currentNode.getStartPosition() + currentNode.getLength();
      boolean isRangeOk = (prev && endOfCurrentNode < pos) || (!prev && pos < endOfCurrentNode);
      if (isRangeOk && (filter == null || filter.evaluate(currentNode))) {
        return currentNode;
      }
    }
    return null; // no sibling for this pos
  }

  /**
   * Gets the fully qualified name of the given type.
   *
   * @param type
   *          The type whose name should be returned.
   * @return The fully qualified name of the given type. Inner types are separated using '$'.
   */
  public static String getFullyQualifiedName(TypeDeclaration type) {
    return getFullyQualifiedName(type, null);
  }

  /**
   * Gets the fully qualified name of the given type inside the given declaring type.
   *
   * @param type
   *          The type whose name should be returned.
   * @param declaringType
   *          The declaring type of the type above.
   * @return The fully qualified name of the given type. Inner types are separated using '$'.
   */
  public static String getFullyQualifiedName(TypeDeclaration type, TypeDeclaration declaringType) {
    return getFullyQualifiedName(type, declaringType, '$');
  }

  /**
   * Gets the fully qualified name of the given type.
   *
   * @param type
   *          The type whose name should be returned.
   * @param innerTypeSeparator
   *          The separator for inner types.
   * @return The fully qualified name of the given type.
   */
  public static String getFullyQualifiedName(TypeDeclaration type, char innerTypeSeparator) {
    return getFullyQualifiedName(type, null, '$');
  }

  /**
   * Gets the fully qualified name of the given type inside the given declaring type.
   *
   * @param type
   *          The type whose name should be returned.
   * @param declaringType
   *          The declaring type of the type above.
   * @param innerTypeSeparator
   *          The separator for inner types.
   * @return The fully qualified name of the given type.
   */
  public static String getFullyQualifiedName(TypeDeclaration type, TypeDeclaration declaringType, char innerTypeSeparator) {
    ASTNode root = type.getRoot();
    if (root instanceof CompilationUnit) {
      Deque<TypeDeclaration> parentTypes = getDeclaringTypes(type);
      return AstUtils.getFullyQualifiedName(parentTypes, (CompilationUnit) root, innerTypeSeparator);
    }

    StringBuilder sb = new StringBuilder();
    if (declaringType != null) {
      sb.append(getFullyQualifiedName(declaringType, null, innerTypeSeparator));
    }

    Deque<TypeDeclaration> declaringTypes = AstUtils.getDeclaringTypes(type);
    for (TypeDeclaration td : declaringTypes) {
      sb.append(innerTypeSeparator).append(td.getName().getIdentifier());
    }
    return sb.toString();
  }

  /**
   * Gets the fully qualified name of the given types inside the given {@link CompilationUnit}.
   *
   * @param parentTypes
   *          The declaring types list (see {@link #getDeclaringTypes(TypeDeclaration)}).
   * @param cu
   *          The {@link CompilationUnit} in which the given types are
   * @return The fully qualified name of the given declaring types in the given {@link CompilationUnit}. Inner types are
   *         separated using '$'.
   */
  public static String getFullyQualifiedName(Deque<TypeDeclaration> parentTypes, CompilationUnit cu) {
    return getFullyQualifiedName(parentTypes, cu, '$');
  }

  /**
   * Gets the fully qualified name of the given types inside the given {@link CompilationUnit}.
   *
   * @param parentTypes
   *          The declaring types list (see {@link #getDeclaringTypes(TypeDeclaration)}).
   * @param cu
   *          The {@link CompilationUnit} in which the given types are
   * @param innerTypeSeparator
   *          The separator for inner types.
   * @return The fully qualified name of the given declaring types in the given {@link CompilationUnit}.
   */
  public static String getFullyQualifiedName(Deque<TypeDeclaration> parentTypes, CompilationUnit cu, char innerTypeSeparator) {
    StringBuilder fqnBuilder = new StringBuilder();

    // package
    PackageDeclaration pck = cu.getPackage();
    if (pck != null) {
      fqnBuilder.append(pck.getName().toString()).append('.');
    }

    Iterator<TypeDeclaration> descendingIterator = parentTypes.descendingIterator();
    if (descendingIterator.hasNext()) {
      fqnBuilder.append(descendingIterator.next().getName());
      while (descendingIterator.hasNext()) {
        fqnBuilder.append(innerTypeSeparator).append(descendingIterator.next().getName());
      }
    }
    return fqnBuilder.toString();
  }

  private static final class P_InstanceOfBindingVisitor implements ITypeBindingVisitor {

    private final String m_fqnToSearch;
    private boolean m_isFound;

    private P_InstanceOfBindingVisitor(String fqnToSearch) {
      m_fqnToSearch = fqnToSearch;
      m_isFound = false;
    }

    @Override
    public boolean visit(ITypeBinding type) {
      if (m_fqnToSearch.equals(type.getTypeDeclaration().getQualifiedName())) {
        m_isFound = true;
        return false;
      }
      return true;
    }
  }

  private static final class P_ChildrenCollector extends DefaultAstVisitor {
    private Deque<ASTNode> m_result;

    private P_ChildrenCollector() {
      super(true);
      m_result = null;
    }

    @Override
    protected boolean visitNode(ASTNode node) {
      if (m_result == null) { // first visitNode: on the node's parent: do nothing, return true
        m_result = new LinkedList<>();
        return true;
      }
      m_result.add(node);
      return false;
    }
  }
}
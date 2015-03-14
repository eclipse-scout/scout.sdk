/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.extension;

import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.jobs.JobEx;
import org.eclipse.scout.sdk.util.ast.visitor.DefaultAstVisitor;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.IMethodFilter;
import org.eclipse.scout.sdk.util.type.MethodFilters;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;

/**
 * <h3>{@link ExtensionValidation}</h3>
 *
 * @author Matthias Villiger
 * @since 4.2.0
 */
public final class ExtensionValidation {

  public static final String INVALID_OPERATION_METHOD_CALL_MARKER_ID = "org.eclipse.scout.sdk.extension.operation.call";
  public static final String INVALID_OPERATION_METHOD_ATTRIB = "org.eclipse.scout.sdk.extension.operation.call.attrib.methodName";
  public static final String EXTENSION_VALIDATION_JOB_FAMILY = "extension.validation.job.family";

  private static final Object LOCK = new Object();
  private static IElementChangedListener listener;
  private static volatile Map<String /*method name*/, Map<String /*method id*/, Set<String /* owner sig */>>> chainableMethods;

  private ExtensionValidation() {
  }

  public static synchronized void install() {
    if (listener == null) {
      listener = new P_ResourceChangeListener();
      JavaCore.addElementChangedListener(listener);
    }
  }

  public static synchronized void uninstall() {
    if (listener != null) {
      JavaCore.removeElementChangedListener(listener);
      listener = null;
    }
    Job.getJobManager().cancel(EXTENSION_VALIDATION_JOB_FAMILY);
    synchronized (LOCK) {
      chainableMethods = null;
    }
  }

  private static Map<String, Map<String, Set<String>>> getChainableMethods() throws CoreException {
    if (chainableMethods == null) {
      synchronized (LOCK) {
        if (chainableMethods == null) {
          chainableMethods = calcChainableMethods();
        }
      }
    }
    return chainableMethods;
  }

  private static Deque<String> getOwners(Set<IMethod> constructorsWithParams, IType chain) throws CoreException {
    Deque<String> owners = new LinkedList<>();
    for (IMethod constructor : constructorsWithParams) {
      for (ILocalVariable param : constructor.getParameters()) {
        String[] paramArgs = Signature.getTypeArguments(param.getTypeSignature());
        for (String arg : paramArgs) {
          String[] typeArguments = Signature.getTypeArguments(arg);
          for (String a : typeArguments) {
            if (a.startsWith("+")) {
              a = a.substring(1);
            }
            a = SignatureUtility.ensureSourceTypeParametersAreCorrect(a, chain);
            a = Signature.getTypeErasure(a);
            if (Signature.getTypeSignatureKind(a) != Signature.TYPE_VARIABLE_SIGNATURE) {
              if (SignatureUtility.isUnresolved(a)) {
                a = SignatureUtility.getResolvedSignature(a, chain, chain);
              }
              owners.add(a.replace('$', '.'));
            }
          }
        }
      }
    }
    return owners;
  }

  private static Map<String, Map<String, Set<String>>> calcChainableMethods() throws CoreException {
    Map<String, Map<String, Set<String>>> result = new HashMap<>();
    IType abstractExtensionChain = TypeUtility.getType(IRuntimeClasses.AbstractExtensionChain);
    if (!TypeUtility.exists(abstractExtensionChain)) {
      return result;
    }

    ICachedTypeHierarchy chainHierarchy = TypeUtility.getTypeHierarchy(abstractExtensionChain);
    Set<IType> allChains = chainHierarchy.getAllSubtypes(abstractExtensionChain, TypeFilters.getMultiTypeFilterAnd(TypeFilters.getClassFilter(), TypeFilters.getFlagsFilter(Flags.AccPublic | Flags.AccStatic)));
    IMethodFilter bridgeFilter = new IMethodFilter() {
      @Override
      public boolean accept(IMethod candidate) throws CoreException {
        return !Flags.isBridge(candidate.getFlags());
      }
    };
    for (IType chain : allChains) {
      // calc owners based on parameters of constructors
      Set<IMethod> constructorsWithParams = TypeUtility.getMethods(chain, MethodFilters.getMultiMethodFilter(new IMethodFilter() {
        @Override
        public boolean accept(IMethod candidate) throws CoreException {
          return candidate.isConstructor() && candidate.getParameters().length > 0;
        }
      }, bridgeFilter));
      Deque<String> owners = getOwners(constructorsWithParams, chain);
      if (owners.size() > 1) {
        ScoutSdk.logWarning("Multiple owner candidates found for Chain '" + chain.getFullyQualifiedName() + "'.");
      }

      // calc operation-method-identifiers based on public methods
      Set<IMethod> operationMethods = TypeUtility.getMethods(chain, MethodFilters.getMultiMethodFilter(new IMethodFilter() {
        @Override
        public boolean accept(IMethod candidate) throws CoreException {
          String src = candidate.getSource();
          if (src == null) {
            return false;
          }
          return !candidate.isConstructor() && src.contains("MethodInvocation") && src.contains("callChain");
        }
      }, bridgeFilter, MethodFilters.getFlagsFilter(Flags.AccPublic)));

      if (operationMethods.size() > 1) {
        ScoutSdk.logWarning("Multiple method invocations found for Chain '" + chain.getFullyQualifiedName() + "'.");
      }
      else if (operationMethods.size() < 1) {
        ScoutSdk.logWarning("No method invocations found for Chain '" + chain.getFullyQualifiedName() + "'.");
      }
      else if (owners.isEmpty()) {
        ScoutSdk.logWarning("No owner candidates found for Chain '" + chain.getFullyQualifiedName() + "'.");
      }
      else {
        IMethod m = CollectionUtility.firstElement(operationMethods);
        String methodName = m.getElementName();
        Map<String, Set<String>> map = result.get(methodName);
        if (map == null) {
          map = new HashMap<>();
          result.put(methodName, map);
        }
        String methodId = SignatureUtility.getMethodIdentifier(m);

        Set<String> set = map.get(methodId);
        if (set == null) {
          set = new HashSet<>();
          map.put(methodId, set);
        }
        set.add(owners.getLast());
      }
    }

    // trim maps
    Map<String, Map<String, Set<String>>> returnResult = new HashMap<>(result.size());
    for (Entry<String, Map<String, Set<String>>> a : result.entrySet()) {
      Map<String, Set<String>> b = new HashMap<>(a.getValue().size());
      for (Entry<String, Set<String>> c : a.getValue().entrySet()) {
        Set<String> d = new HashSet<>(c.getValue());
        b.put(c.getKey(), d);
      }
      returnResult.put(a.getKey(), b);
    }

    return returnResult;
  }

  private static final class P_MarkerCreationJob extends JobEx {

    private final CompilationUnit m_ast;

    public P_MarkerCreationJob(CompilationUnit ast) {
      super("Extension Validation Job");
      setSystem(true);
      setUser(false);
      setPriority(Job.BUILD);
      setRule(new P_SchedulingRule());
      m_ast = ast;
    }

    private void updateMarkers(ICompilationUnit icu, IResource res, Set<P_MarkerData> markerDatas) throws CoreException {
      Set<IMarker> existingMarkers = CollectionUtility.hashSet(res.findMarkers(INVALID_OPERATION_METHOD_CALL_MARKER_ID, true, IResource.DEPTH_ZERO));
      Iterator<IMarker> iterator = existingMarkers.iterator();
      while (iterator.hasNext()) {
        IMarker m = iterator.next();
        int start = m.getAttribute(IMarker.CHAR_START, -1);
        int end = m.getAttribute(IMarker.CHAR_END, -1);
        P_MarkerData curMarkerData = new P_MarkerData(start, end - start, m.getAttribute(INVALID_OPERATION_METHOD_ATTRIB, null));
        boolean removed = markerDatas.remove(curMarkerData);
        if (removed) {
          // this marker already exists -> nothing to do
          iterator.remove();
        }
      }

      // delete the ones that no longer exist
      for (IMarker m : existingMarkers) {
        m.delete();
      }

      // create the ones that are new
      for (P_MarkerData data : markerDatas) {
        IMarker marker = res.createMarker(INVALID_OPERATION_METHOD_CALL_MARKER_ID);
        marker.setAttribute(IMarker.MESSAGE, Texts.get("ChainableMethodCannotBeCalled", data.m_methodName));
        marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
        marker.setAttribute(IMarker.CHAR_START, data.m_start);
        marker.setAttribute(IMarker.CHAR_END, data.m_start + data.m_length);
        marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
        marker.setAttribute(INVALID_OPERATION_METHOD_ATTRIB, data.m_methodName);
        try {
          Document doc = new Document(icu.getSource());
          marker.setAttribute(IMarker.LINE_NUMBER, doc.getLineOfOffset(data.m_start) + 1);
        }
        catch (BadLocationException e) {
          //nop
        }
      }
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
      try {
        IJavaElement je = m_ast.getJavaElement();
        if (!(je instanceof ICompilationUnit)) {
          return Status.OK_STATUS;
        }
        ICompilationUnit icu = (ICompilationUnit) je;
        IResource resource = icu.getResource();
        if (!ResourceUtility.exists(resource)) {
          return Status.OK_STATUS;
        }
        if (monitor.isCanceled()) {
          return Status.CANCEL_STATUS;
        }

        final Map<String, Map<String, Set<String>>> interestingMethods = getChainableMethods();
        final Map<IMethod, P_ProblemCandidates> methodsToCheck = new HashMap<>();

        m_ast.accept(new DefaultAstVisitor() {

          @Override
          public boolean visitNode(ASTNode node) {
            if (monitor.isCanceled()) {
              return false;
            }
            return super.visitNode(node);
          }

          @Override
          public boolean visit(MethodInvocation node) {
            if (monitor.isCanceled()) {
              return false;
            }
            String methodName = node.getName().getIdentifier();
            Map<String, Set<String>> map = interestingMethods.get(methodName);
            if (map != null) {
              IMethodBinding bindings = node.resolveMethodBinding();
              if (bindings != null) {
                IJavaElement javaElement = bindings.getJavaElement();
                if (javaElement instanceof IMethod) {
                  IMethod m = (IMethod) javaElement;
                  IType declaringType = getDeclaringTypeOf(node);
                  if (monitor.isCanceled()) {
                    return false;
                  }
                  if (!isLocalExtension(declaringType, m)) {
                    try {
                      String id = SignatureUtility.getMethodIdentifier(m);
                      Set<String> owners = map.get(id);
                      if (owners != null) {
                        if (monitor.isCanceled()) {
                          return false;
                        }
                        P_ProblemCandidates candidate = new P_ProblemCandidates(owners, node.getStartPosition(), node.getLength(), m.getElementName());
                        methodsToCheck.put(m, candidate);
                      }
                    }
                    catch (CoreException e) {
                      ScoutSdk.logError("Unable to perform extension validation.", e);
                    }
                  }
                }
              }
            }
            return super.visit(node);
          }
        });
        if (monitor.isCanceled()) {
          return Status.CANCEL_STATUS;
        }

        Set<Entry<IMethod, P_ProblemCandidates>> entrySet = methodsToCheck.entrySet();
        Set<P_MarkerData> markers = new HashSet<>(entrySet.size());
        for (Entry<IMethod, P_ProblemCandidates> entry : entrySet) {
          ITypeHierarchy supertypeHierarchy = TypeUtility.getSupertypeHierarchy(entry.getKey().getDeclaringType());
          if (supertypeHierarchy != null) {
            P_MarkerData markerData = createErrorMarkerIfNecessary(entry.getValue(), supertypeHierarchy, monitor);
            if (markerData != null) {
              markers.add(markerData);
            }
          }
          if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
          }
        }

        updateMarkers(icu, resource, markers);
      }
      catch (Exception e) {
        ScoutSdk.logWarning("Unable to check for chainable method calls.", e);
      }
      return Status.OK_STATUS;
    }

    @Override
    public boolean belongsTo(Object family) {
      return EXTENSION_VALIDATION_JOB_FAMILY.equals(family);
    }

    private IType getDeclaringTypeOf(MethodInvocation mi) {
      TypeDeclaration td = null;
      ASTNode cur = mi;
      while (cur != null && td == null) {
        if (cur instanceof TypeDeclaration) {
          td = (TypeDeclaration) cur;
        }
        cur = cur.getParent();
      }

      if (td != null) {
        ITypeBinding binding = td.resolveBinding();
        if (binding != null) {
          IJavaElement javaElement = binding.getJavaElement();
          if (TypeUtility.exists(javaElement) && javaElement.getElementType() == IJavaElement.TYPE) {
            return (IType) javaElement;
          }
        }
      }

      return null;
    }

    private boolean isLocalExtension(IType declaringTypeOfMethodInvocation, IMethod methodDeclaration) {
      if (declaringTypeOfMethodInvocation == null || methodDeclaration == null) {
        return false;
      }

      IType declaringTypeOfLocalExtension = declaringTypeOfMethodInvocation.getDeclaringType();
      if (declaringTypeOfLocalExtension == null) {
        return false;
      }

      return declaringTypeOfLocalExtension.equals(methodDeclaration.getDeclaringType());
    }

    private P_MarkerData createErrorMarkerIfNecessary(P_ProblemCandidates problemCandidate, ITypeHierarchy supertypeHierarchy, IProgressMonitor monitor) throws CoreException {
      for (String ownerSig : problemCandidate.getOwnerSignatures()) {
        IType owner = TypeUtility.getTypeBySignature(ownerSig);
        if (TypeUtility.exists(owner) && supertypeHierarchy.contains(owner)) {
          return new P_MarkerData(problemCandidate.getStart(), problemCandidate.getLength(), problemCandidate.getMethodName());
        }
        if (monitor.isCanceled()) {
          return null;
        }
      }
      return null;
    }
  }

  private static final class P_MarkerData {
    private final int m_start;
    private final int m_length;
    private final String m_methodName;

    public P_MarkerData(int start, int length, String methodName) {
      super();
      m_start = start;
      m_length = length;
      this.m_methodName = methodName;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + m_length;
      result = prime * result + m_start;
      result = prime * result + ((m_methodName == null) ? 0 : m_methodName.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof P_MarkerData)) {
        return false;
      }
      P_MarkerData other = (P_MarkerData) obj;
      if (m_length != other.m_length) {
        return false;
      }
      if (m_start != other.m_start) {
        return false;
      }
      if (m_methodName == null) {
        if (other.m_methodName != null) {
          return false;
        }
      }
      else if (!m_methodName.equals(other.m_methodName)) {
        return false;
      }
      return true;
    }
  }

  private static final class P_ProblemCandidates {
    private final Set<String> m_ownerSignatures;
    private final int m_start;
    private final int m_length;
    private final String m_methodName;

    /**
     * @param owner
     * @param start
     * @param length
     */
    public P_ProblemCandidates(Set<String> owners, int start, int length, String methodName) {
      m_ownerSignatures = owners;
      m_start = start;
      m_length = length;
      m_methodName = methodName;
    }

    public Set<String> getOwnerSignatures() {
      return m_ownerSignatures;
    }

    public int getStart() {
      return m_start;
    }

    public int getLength() {
      return m_length;
    }

    public String getMethodName() {
      return m_methodName;
    }
  }

  private static final class P_ResourceChangeListener implements IElementChangedListener {
    @Override
    public void elementChanged(ElementChangedEvent event) {
      CompilationUnit compilationUnitAST = visitDelta(event.getDelta());
      if (compilationUnitAST != null) {
        Job.getJobManager().cancel(EXTENSION_VALIDATION_JOB_FAMILY);
        new P_MarkerCreationJob(compilationUnitAST).schedule();
      }
    }

    private CompilationUnit visitDelta(IJavaElementDelta delta) {
      CompilationUnit ast = delta.getCompilationUnitAST();
      if (ast != null) {
        return ast;
      }

      if ((delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0) {
        IJavaElementDelta[] childDeltas = delta.getAffectedChildren();
        if (childDeltas != null && childDeltas.length > 0) {
          for (int i = 0; i < childDeltas.length; i++) {
            ast = visitDelta(childDeltas[i]);
            if (ast != null) {
              return ast;
            }
          }
        }
      }
      return null;
    }
  }

  private static final class P_SchedulingRule implements ISchedulingRule {

    @Override
    public boolean contains(ISchedulingRule rule) {
      return rule instanceof P_SchedulingRule;
    }

    @Override
    public boolean isConflicting(ISchedulingRule rule) {
      return rule instanceof P_SchedulingRule;
    }
  }
}

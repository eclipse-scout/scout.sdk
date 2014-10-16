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
package org.eclipse.scout.sdk.operation.service;

import java.util.Date;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.MethodParameter;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class CalendarServiceNewOperation extends ServiceNewOperation {

  /**
   * @param serviceInterfaceName
   * @param serviceName
   */
  public CalendarServiceNewOperation(String serviceInterfaceName, String serviceName) {
    super(serviceInterfaceName, serviceName);
  }

  @Override
  public String getOperationName() {
    return "new Calendar service '" + getImplementationName() + "'...";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    // interface getItems method
    TypeSourceBuilder interfaceSourceBuilder = getInterfaceSourceBuilder();
    IMethodSourceBuilder interfaceGetItemsBuilder = new MethodSourceBuilder("getItems");
    interfaceGetItemsBuilder.setFlags(Flags.AccInterface);
    interfaceGetItemsBuilder.setReturnTypeSignature(SignatureCache.createTypeSignature(Set.class.getName() + "<" + IRuntimeClasses.ICalendarItem + ">"));
    interfaceGetItemsBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesMethodCommentBuilder());
    interfaceGetItemsBuilder.addParameter(new MethodParameter("minDate", SignatureCache.createTypeSignature(Date.class.getName())));
    interfaceGetItemsBuilder.addParameter(new MethodParameter("maxDate", SignatureCache.createTypeSignature(Date.class.getName())));
    interfaceGetItemsBuilder.addExceptionSignature(SignatureCache.createTypeSignature(IRuntimeClasses.ProcessingException));
    interfaceSourceBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(interfaceGetItemsBuilder), interfaceGetItemsBuilder);
    // interface storeItems method
    IMethodSourceBuilder interfaceStoreItemsBuilder = new MethodSourceBuilder("storeItems");
    interfaceStoreItemsBuilder.setFlags(Flags.AccInterface);
    interfaceStoreItemsBuilder.setReturnTypeSignature(Signature.SIG_VOID);
    interfaceStoreItemsBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesMethodCommentBuilder());
    interfaceStoreItemsBuilder.addParameter(new MethodParameter("items", SignatureCache.createTypeSignature(Set.class.getName() + "<" + IRuntimeClasses.ICalendarItem + ">")));
    interfaceStoreItemsBuilder.addParameter(new MethodParameter("delta", Signature.SIG_BOOLEAN));
    interfaceStoreItemsBuilder.addExceptionSignature(SignatureCache.createTypeSignature(IRuntimeClasses.ProcessingException));
    interfaceSourceBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(interfaceStoreItemsBuilder), interfaceStoreItemsBuilder);

    // implementation getItems method
    TypeSourceBuilder implementationSourceBuilder = getImplementationSourceBuilder();
    IMethodSourceBuilder implementationGetItemsBuilder = new MethodSourceBuilder("getItems");
    implementationGetItemsBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOverrideAnnotationSourceBuilder());
    implementationGetItemsBuilder.setFlags(Flags.AccPublic);
    implementationGetItemsBuilder.setReturnTypeSignature(SignatureCache.createTypeSignature(Set.class.getName() + "<" + IRuntimeClasses.ICalendarItem + ">"));
    implementationGetItemsBuilder.addParameter(new MethodParameter("minDate", SignatureCache.createTypeSignature(Date.class.getName())));
    implementationGetItemsBuilder.addParameter(new MethodParameter("maxDate", SignatureCache.createTypeSignature(Date.class.getName())));
    implementationGetItemsBuilder.addExceptionSignature(SignatureCache.createTypeSignature(IRuntimeClasses.ProcessingException));
    implementationGetItemsBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append(ScoutUtility.getCommentBlock("business logic here.")).append(lineDelimiter);
        source.append("return ").append(validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.CollectionUtility))).append(".hashSet();").append(lineDelimiter);
      }
    });
    implementationSourceBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(implementationGetItemsBuilder), implementationGetItemsBuilder);

    // implementation storeItems method
    IMethodSourceBuilder implementationStoreItemsBuilder = new MethodSourceBuilder("storeItems");
    implementationStoreItemsBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOverrideAnnotationSourceBuilder());
    implementationStoreItemsBuilder.setFlags(Flags.AccPublic);
    implementationStoreItemsBuilder.setReturnTypeSignature(Signature.SIG_VOID);
    implementationStoreItemsBuilder.addParameter(new MethodParameter("items", SignatureCache.createTypeSignature(Set.class.getName() + "<" + IRuntimeClasses.ICalendarItem + ">")));
    implementationStoreItemsBuilder.addParameter(new MethodParameter("delta", Signature.SIG_BOOLEAN));
    implementationStoreItemsBuilder.addExceptionSignature(SignatureCache.createTypeSignature(IRuntimeClasses.ProcessingException));
    implementationStoreItemsBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody(ScoutUtility.getCommentBlock("business logic here.")));
    implementationSourceBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(implementationStoreItemsBuilder), implementationStoreItemsBuilder);

    super.run(monitor, workingCopyManager);
  }

}

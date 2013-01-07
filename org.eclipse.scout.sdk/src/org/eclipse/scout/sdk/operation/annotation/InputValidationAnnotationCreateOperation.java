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
package org.eclipse.scout.sdk.operation.annotation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMember;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.text.edits.TextEdit;

/**
 * <h3>{@link InputValidationAnnotationCreateOperation}</h3> ...
 * 
 * @author mvi
 * @since 3.9.0 07.01.2013
 */
public class InputValidationAnnotationCreateOperation extends AnnotationCreateOperation {

  public InputValidationAnnotationCreateOperation(IMember annotationOwner) {
    super(annotationOwner, SignatureCache.createTypeSignature(RuntimeClasses.InputValidation));
    addParameter("IValidationStrategy.PROCESS.class");
  }

  @Override
  public TextEdit createEdit(IImportValidator validator, String NL) throws CoreException {
    validator.addImport(RuntimeClasses.InputValidation);
    validator.addImport(RuntimeClasses.IValidationStrategy);
    return super.createEdit(validator, NL);
  }
}

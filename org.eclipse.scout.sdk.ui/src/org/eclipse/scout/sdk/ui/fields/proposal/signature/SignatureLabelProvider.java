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
package org.eclipse.scout.sdk.ui.fields.proposal.signature;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.ui.fields.proposal.SelectionStateLabelProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.signature.SimpleImportValidator;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>{@link SignatureLabelProvider}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 09.02.2012
 */
public class SignatureLabelProvider extends SelectionStateLabelProvider {
  @Override
  public String getText(Object element) {
    String signature = (String) element;
    try {
      return SignatureUtility.getTypeReference(signature, new SimpleImportValidator());
    }
    catch (CoreException e) {
      ScoutSdkUi.logWarning("unable to get text of signature '" + element + "'", e);
      return (String) element;
    }
  }

  @Override
  public String getTextSelected(Object element) {
    if (element instanceof String) {
      String signature = (String) element;
      if (SignatureUtility.getTypeSignatureKind(signature) != Signature.BASE_TYPE_SIGNATURE) {
        // append packageName
        StringBuilder textBuilder = new StringBuilder(getText(element));
        textBuilder.append("  (").append(Signature.getSignatureQualifier(signature)).append(")");
        return textBuilder.toString();
      }
    }
    return getText(element);
  }

  @Override
  public Image getImage(Object element) {
    if (SignatureUtility.getTypeSignatureKind((String) element) == Signature.BASE_TYPE_SIGNATURE) {
      return ScoutSdkUi.getImage(ScoutSdkUi.FieldPublic);
    }
    else {
      return ScoutSdkUi.getImage(ScoutSdkUi.Class);
    }
  }

  @Override
  public Image getImageSelected(Object element) {
    return getImage(element);
  }
}

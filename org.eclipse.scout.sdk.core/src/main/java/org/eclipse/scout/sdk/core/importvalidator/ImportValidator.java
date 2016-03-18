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
package org.eclipse.scout.sdk.core.importvalidator;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.importcollector.IImportCollector;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureDescriptor;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;

/**
 * <h3>{@link ImportValidator}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ImportValidator implements IImportValidator {
  private IImportCollector m_importCollector;

  public ImportValidator(IImportCollector collector) {
    m_importCollector = collector;
  }

  @Override
  public void setImportCollector(IImportCollector collector) {
    m_importCollector = collector;
  }

  @Override
  public IImportCollector getImportCollector() {
    return m_importCollector;
  }

  @Override
  public String useType(IType type) {
    return useSignature(SignatureUtils.getTypeSignature(type));
  }

  @Override
  public String useName(String fullyQualifiedName) {
    return useSignature(Signature.createTypeSignature(fullyQualifiedName));
  }

  @Override
  public String useSignature(String signature) {
    return useSignatureInternal(signature, false);
  }

  protected String useSignatureInternal(String signature, boolean isTypeArg) {
    StringBuilder sigBuilder = new StringBuilder();
    int arrayCount = 0;
    switch (Signature.getTypeSignatureKind(signature)) {
      case ISignatureConstants.WILDCARD_TYPE_SIGNATURE:
        sigBuilder.append('?');
        if (signature.length() > 1) {
          sigBuilder.append(" extends ");
          sigBuilder.append(useSignatureInternal(signature.substring(1), false));
        }
        break;
      case ISignatureConstants.ARRAY_TYPE_SIGNATURE:
        arrayCount = Signature.getArrayCount(signature);
        sigBuilder.append(useSignatureInternal(signature.substring(arrayCount), false));
        break;
      case ISignatureConstants.BASE_TYPE_SIGNATURE:
        sigBuilder.append(Signature.getSignatureSimpleName(signature));
        break;
      case ISignatureConstants.TYPE_VARIABLE_SIGNATURE:
        sigBuilder.append(SignatureUtils.toFullyQualifiedName(signature));
        break;
      default:
        String[] typeArguments = Signature.getTypeArguments(signature);
        signature = Signature.getTypeErasure(signature);

        //check and register
        SignatureDescriptor cand = new SignatureDescriptor(signature);
        IImportCollector collector = getImportCollector();
        String use = collector.checkExistingImports(cand);
        if (use == null) {
          use = collector.checkCurrentScope(cand);
          boolean foundInCurrentScope = use != null && use.indexOf(ISignatureConstants.C_DOT) < 0;
          boolean inSamePackage = Objects.equals(collector.getQualifier(), cand.getQualifier()) || (StringUtils.isBlank(collector.getQualifier()) && StringUtils.isBlank(cand.getQualifier()));
          if (isTypeArg && foundInCurrentScope && inSamePackage) {
            // special case for type argument signature which are simple qualified because in same scope
            collector.registerElement(cand); // ensure it is registered as used so that it appears in the imports for inner types only
          }
        }
        if (use == null) {
          use = collector.registerElement(cand);
        }
        sigBuilder.append(use);

        if (typeArguments.length > 0) {
          sigBuilder.append(ISignatureConstants.C_GENERIC_START);
          sigBuilder.append(useSignatureInternal(typeArguments[0], true));
          for (int i = 1; i < typeArguments.length; i++) {
            sigBuilder.append(", ");
            sigBuilder.append(useSignatureInternal(typeArguments[i], true));
          }
          sigBuilder.append(ISignatureConstants.C_GENERIC_END);
        }
        break;
    }
    for (int i = 0; i < arrayCount; i++) {
      sigBuilder.append("[]");
    }
    return sigBuilder.toString();
  }
}

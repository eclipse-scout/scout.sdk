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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.scout.sdk.core.importcollector.IImportCollector;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureDescriptor;

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
    return useSignature(Validate.notNull(type).signature());
  }

  @Override
  public String useName(String fullyQualifiedName) {
    return useSignature(Signature.createTypeSignature(fullyQualifiedName));
  }

  @Override
  public String useSignature(String signature) {
    StringBuilder result = new StringBuilder(128);
    useSignatureInternal(signature, false, result);
    return result.toString();
  }

  protected void useSignatureInternal(String signature, boolean isTypeArg, StringBuilder sigBuilder) {
    int arrayCount = 0;
    switch (Signature.getTypeSignatureKind(signature)) {
      case ISignatureConstants.WILDCARD_TYPE_SIGNATURE:
        sigBuilder.append('?');
        if (signature.length() > 1) {
          sigBuilder.append(" extends ");
          useSignatureInternal(signature.substring(1), false, sigBuilder);
        }
        break;
      case ISignatureConstants.ARRAY_TYPE_SIGNATURE:
        arrayCount = Signature.getArrayCount(signature);
        useSignatureInternal(signature.substring(arrayCount), false, sigBuilder);
        break;
      case ISignatureConstants.BASE_TYPE_SIGNATURE:
        sigBuilder.append(Signature.getSignatureSimpleName(signature));
        break;
      case ISignatureConstants.TYPE_VARIABLE_SIGNATURE:
        String[] typeParameterBounds = Signature.getTypeParameterBounds(signature);
        String name = Signature.getTypeVariable(signature);
        sigBuilder.append(name);
        if (typeParameterBounds.length > 0) {
          sigBuilder.append(" extends ");
          useSignatureInternal(typeParameterBounds[0], false, sigBuilder);
          for (int i = 1; i < typeParameterBounds.length; i++) {
            sigBuilder.append(" & ");
            useSignatureInternal(typeParameterBounds[i], false, sigBuilder);
          }
        }
        break;
      default:
        List<String> segments = getSegments(signature);
        String signatureToImport = signature;
        int firstParameterizedSegmentIndex = getFirstSegmentWithTypeArgumentsInQualifier(segments);
        if (firstParameterizedSegmentIndex >= 0) {
          StringBuilder segmentsToFirstParameterized = new StringBuilder();
          for (int i = 0; i <= firstParameterizedSegmentIndex; i++) {
            if (i != 0) {
              segmentsToFirstParameterized.append(ISignatureConstants.C_DOT);
            }
            segmentsToFirstParameterized.append(segments.get(i));
          }
          if (segmentsToFirstParameterized.charAt(segmentsToFirstParameterized.length() - 1) != ISignatureConstants.C_SEMICOLON) {
            segmentsToFirstParameterized.append(ISignatureConstants.C_SEMICOLON);
          }
          signatureToImport = segmentsToFirstParameterized.toString();
        }

        //check and register
        SignatureDescriptor cand = new SignatureDescriptor(Signature.getTypeErasure(signatureToImport));
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

        // build reference
        sigBuilder.append(use);
        String[] typeArguments = Signature.getTypeArguments(signatureToImport);
        if (typeArguments.length > 0) {
          sigBuilder.append(ISignatureConstants.C_GENERIC_START);
          useSignatureInternal(typeArguments[0], true, sigBuilder);
          for (int i = 1; i < typeArguments.length; i++) {
            sigBuilder.append(", ");
            useSignatureInternal(typeArguments[i], true, sigBuilder);
          }
          sigBuilder.append(ISignatureConstants.C_GENERIC_END);
        }

        // subsequent segments
        if (firstParameterizedSegmentIndex >= 0) {
          for (int i = firstParameterizedSegmentIndex + 1; i < segments.size(); i++) {
            String segmentSig = ISignatureConstants.C_RESOLVED + segments.get(i) + ISignatureConstants.C_SEMICOLON;
            sigBuilder.append(ISignatureConstants.C_DOT);
            useSignatureInternal(segmentSig, false, sigBuilder);
          }
        }
        break;
    }
    for (int i = 0; i < arrayCount; i++) {
      sigBuilder.append("[]");
    }
  }

  protected static int getFirstSegmentWithTypeArgumentsInQualifier(List<String> segments) {
    for (int i = 0; i < segments.size(); i++) {
      if (segments.get(i).indexOf(ISignatureConstants.C_GENERIC_START) >= 0) {
        return i;
      }
    }
    return -1;
  }

  protected static List<String> getSegments(String signature) {
    List<String> segments = new ArrayList<>();
    StringBuilder segmentBuilder = new StringBuilder();
    char[] sig = signature.toCharArray();
    int argCount = 0;
    for (int i = 0; i < sig.length; i++) {
      char curChar = sig[i];
      switch (curChar) {
        case ISignatureConstants.C_DOT:
          boolean insideTypeArg = argCount > 0;
          if (insideTypeArg) {
            segmentBuilder.append(curChar);
          }
          else {
            // new segment
            segments.add(segmentBuilder.toString());
            segmentBuilder.delete(0, segmentBuilder.length());
          }
          break;
        case ISignatureConstants.C_GENERIC_START:
          argCount++;
          segmentBuilder.append(curChar);
          break;
        case ISignatureConstants.C_GENERIC_END:
          argCount--;
          segmentBuilder.append(curChar);
          break;
        default:
          segmentBuilder.append(curChar);
          break;
      }
    }
    segments.add(segmentBuilder.toString());
    return segments;
  }
}

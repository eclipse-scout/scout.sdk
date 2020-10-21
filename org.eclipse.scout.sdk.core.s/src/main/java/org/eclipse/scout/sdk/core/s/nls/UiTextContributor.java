/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.nls;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static java.util.stream.Stream.concat;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;
import static org.eclipse.scout.sdk.core.util.StreamUtils.allMatchResults;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * Contains all {@link ITranslation} keys in a Scout UiTextContributor. It considers keys that are listed as
 * {@link String} literals or loaded using references TextProviderServices.
 */
public class UiTextContributor {

  private static final Pattern KEY_LITERAL_PAT = Pattern.compile("\"(" + ITranslation.KEY_REGEX.pattern() + ")\"");
  private static final Pattern TEXT_SERVICE_CLASS_LITERAL_PAT = Pattern.compile("([\\w.]+TextProviderService)\\.class");

  private final IType m_contributor;
  private final FinalValue<Set<String>> m_keys;

  public UiTextContributor(IType contributor) {
    m_contributor = Ensure.notNull(contributor);
    m_keys = new FinalValue<>();
  }

  public IType type() {
    return m_contributor;
  }

  public Stream<String> keys() {
    return m_keys.opt().stream().flatMap(Collection::stream); // not yet loaded or load failed
  }

  public boolean load(IProgress progress) {
    return m_keys.computeIfAbsent(() -> loadAllKeys(progress));
  }

  protected Set<String> loadAllKeys(IProgress progress) {
    var methodName = type().javaEnvironment().requireApi(IScoutApi.class).UiTextContributor().contributeUiTextKeysMethodName();
    return type()
        .methods()
        .withName(methodName)
        .first()
        .flatMap(IMethod::sourceOfBody)
        .map(ISourceRange::asCharSequence)
        .map(CoreUtils::removeComments)
        .flatMap(Strings::notBlank)
        .map(src -> loadAllKeys(src, progress))
        .orElseThrow(() -> newFail("Could not calculate available translation keys for '{}'. No source code for method '{}' could be found.", type().name(), methodName));
  }

  protected Set<String> loadAllKeys(CharSequence methodSource, IProgress progress) {
    return concat(loadDirectLiterals(methodSource), loadReferencedTextProviderServices(methodSource, progress))
        .collect(toUnmodifiableSet());
  }

  protected static Stream<String> loadDirectLiterals(CharSequence contributeUiTextKeysMethodSource) {
    return allMatchResults(KEY_LITERAL_PAT, contributeUiTextKeysMethodSource)
        .map(match -> match.group(1));
  }

  protected Stream<String> loadReferencedTextProviderServices(CharSequence contributeUiTextKeysMethodSource, IProgress progress) {
    var contributor = type();
    var scoutApi = contributor.javaEnvironment().requireApi(IScoutApi.class);
    var referencedTextServices = allMatchResults(TEXT_SERVICE_CLASS_LITERAL_PAT, contributeUiTextKeysMethodSource)
        .map(match -> match.group(1))
        .map(contributor::resolveSimpleName)
        .flatMap(Optional::stream)
        .filter(type -> type.isInstanceOf(scoutApi.ITextProviderService()))
        .collect(toList());
    progress.init(referencedTextServices.size(), "Load referenced text provider service");
    return referencedTextServices.stream()
        .map(txtService -> TranslationStores.create(txtService, progress.newChild(1)))
        .flatMap(Optional::stream)
        .flatMap(ITranslationStore::keys);
  }

  @Override
  public String toString() {
    var builder = new StringBuilder();
    builder.append(UiTextContributor.class.getSimpleName()).append(" [")
        .append(type().name()).append(']');
    return builder.toString();
  }

  @Override
  public int hashCode() {
    return type().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    var other = (UiTextContributor) obj;
    return type().equals(other.type());
  }
}

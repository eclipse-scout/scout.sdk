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
package org.eclipse.scout.sdk.core.s.testing;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.util.Ensure;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * <h3>{@link AbstractBooleanPermutationArgumentsProvider}</h3>
 * <p>
 * A {@link ArgumentsProvider} to be used for test methods that use boolean parameters. This class may be used to create
 * dynamic tests for all combinations of boolean arguments. The specific subclasses can be registered using the
 * {@link ArgumentsSource} annotation.
 *
 * @since 7.1.0
 */
public abstract class AbstractBooleanPermutationArgumentsProvider implements ArgumentsProvider {

  private final int m_bits;

  protected AbstractBooleanPermutationArgumentsProvider(int numBits) {
    Ensure.isTrue(numBits < 21 && numBits > 0, "The number of bits must be between 1 and 20");
    m_bits = numBits;
  }

  protected static Object[] toBoolArray(int numBits, long mask) {
    Object[] result = new Object[numBits];
    for (int pos = 0; pos < result.length; pos++) {
      result[pos] = (mask & (1L << pos)) != 0;
    }
    return result;
  }

  protected static Stream<Object[]> permutations(int numBits) {
    int numCombinations = BigInteger.valueOf(2).pow(numBits).intValue();
    Collection<Object[]> result = new ArrayList<>(numCombinations);
    for (int cur = 0; cur < numCombinations; cur++) {
      result.add(toBoolArray(numBits, cur));
    }
    return result.stream();
  }

  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
    return permutations(bits()).map(Arguments::of);
  }

  public int bits() {
    return m_bits;
  }
}

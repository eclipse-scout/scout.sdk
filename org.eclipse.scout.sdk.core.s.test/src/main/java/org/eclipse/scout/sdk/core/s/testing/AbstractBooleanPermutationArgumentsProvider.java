/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.testing;

import java.math.BigInteger;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
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
    return IntStream.range(0, numBits)
        .mapToObj(pos -> (mask & (1L << pos)) != 0)
        .toArray();
  }

  protected static Stream<Object[]> permutations(int numBits) {
    var numCombinations = BigInteger.valueOf(2).pow(numBits).longValue();
    return LongStream.range(0, numCombinations)
        .mapToObj(cur -> toBoolArray(numBits, cur));
  }

  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
    return permutations(bits()).map(Arguments::of);
  }

  public int bits() {
    return m_bits;
  }
}

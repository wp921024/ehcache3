package org.ehcache.internal.tier;

import org.ehcache.exceptions.CacheAccessException;
import org.ehcache.expiry.Expirations;
import org.ehcache.function.Function;
import org.ehcache.spi.cache.Store;
import org.ehcache.spi.cache.tiering.CachingTier;
import org.ehcache.spi.test.After;
import org.ehcache.spi.test.Before;
import org.ehcache.spi.test.SPITest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the {@link org.ehcache.spi.cache.tiering.CachingTier#getOrComputeIfAbsent(Object, Function)} contract of the
 * {@link org.ehcache.spi.cache.tiering.CachingTier CachingTier} interface.
 * <p/>
 *
 * @author Aurelien Broszniowski
 */

public class CachingTierGetOrComputeIfAbsent<K, V> extends CachingTierTester<K, V> {

  private CachingTier tier;

  public CachingTierGetOrComputeIfAbsent(final CachingTierFactory<K, V> factory) {
    super(factory);
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
    if (tier != null) {
      tier.close();
      tier = null;
    }
  }

  @SPITest
  @SuppressWarnings("unchecked")
  public void returnTheValueHolderCurrentlyInTheCachingTier() {
    K key = factory.createKey(1);
    V value = factory.createValue(1);
    final Store.ValueHolder<V> computedValueHolder = mock(Store.ValueHolder.class);
    when(computedValueHolder.value()).thenReturn(value);

    tier = factory.newCachingTier(factory.newConfiguration(factory.getKeyType(), factory.getValueType(),
        1L, null, null, Expirations.noExpiration()));

    try {
      tier.getOrComputeIfAbsent(key, new Function() {   // actually put mapping in tier
        @Override
        public Object apply(final Object o) {
          return computedValueHolder;
        }
      });

      Store.ValueHolder<V> valueHolder = tier.getOrComputeIfAbsent(key, new Function<K, Store.ValueHolder<V>>() {
        @Override
        public Store.ValueHolder<V> apply(final K k) {
          return null;
        }
      });

      assertThat(valueHolder.value(), is(equalTo(value)));
    } catch (CacheAccessException e) {
      System.err.println("Warning, an exception is thrown due to the SPI test");
      e.printStackTrace();
    }
  }

  @SPITest
  @SuppressWarnings("unchecked")
  public void returnTheValueHolderNotInTheCachingTier() {
    K key = factory.createKey(1);
    V value = factory.createValue(1);

    final Store.ValueHolder<V> computedValueHolder = mock(Store.ValueHolder.class);
    when(computedValueHolder.value()).thenReturn(value);

    tier = factory.newCachingTier(factory.newConfiguration(factory.getKeyType(), factory.getValueType(),
        1L, null, null, Expirations.noExpiration()));

    try {
      Store.ValueHolder<V> valueHolder = tier.getOrComputeIfAbsent(key, new Function<K, Store.ValueHolder<V>>() {
        @Override
        public Store.ValueHolder<V> apply(final K k) {
          return computedValueHolder;
        }
      });

      assertThat(valueHolder.value(), is(equalTo(value)));
    } catch (CacheAccessException e) {
      System.err.println("Warning, an exception is thrown due to the SPI test");
      e.printStackTrace();
    }
  }

}

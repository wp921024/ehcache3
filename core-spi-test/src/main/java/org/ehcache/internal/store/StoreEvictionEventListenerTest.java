/*
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ehcache.internal.store;

import org.ehcache.Cache;
import org.ehcache.events.StoreEventListener;
import org.ehcache.exceptions.CacheAccessException;
import org.ehcache.function.BiFunction;
import org.ehcache.function.Function;
import org.ehcache.spi.cache.Store;
import org.ehcache.spi.test.After;
import org.ehcache.spi.test.Ignore;
import org.ehcache.spi.test.SPITest;
import org.mockito.InOrder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the eviction half of the {@link org.ehcache.spi.cache.Store#enableStoreEventNotifications(org.ehcache.events.StoreEventListener)} contract of the
 * {@link org.ehcache.spi.cache.Store Store} interface.
 */

public class StoreEvictionEventListenerTest<K, V> extends SPIStoreTester<K, V> {

  public StoreEvictionEventListenerTest(StoreFactory<K, V> factory) {
    super(factory);
  }

  final K k = factory.createKey(1L);
  final V v = factory.createValue(1l);
  final K k2 = factory.createKey(2L);
  final V v2 = factory.createValue(2l);
  final V v3 = factory.createValue(3l);

  protected Store<K, V> kvStore;

  @After
  public void tearDown() {
    if (kvStore != null) {
      factory.close(kvStore);
      kvStore = null;
    }
  }

  @SPITest
  public void testPutOnEviction() throws Exception {
    kvStore = factory.newStoreWithCapacity(1L);
    StoreEventListener<K, V> listener = addListener(kvStore);
    kvStore.put(k, v);
    kvStore.put(k2, v2);
    verifyListenerInteractions(listener);
  }

  @SPITest
  public void testPutIfAbsentOnEviction() throws Exception {
    kvStore = factory.newStoreWithCapacity(1L);
    StoreEventListener<K, V> listener = addListener(kvStore);
    kvStore.put(k, v);
    kvStore.putIfAbsent(k2, v2);
    verifyListenerInteractions(listener);
  }

  @SPITest
  @Ignore(reason = "See comment")
  public void testReplaceTwoArgsOnEviction() throws Exception {
    // The following makes no sense, what we may want to test here is that replace with a bigger value evicts
    // But that would also mean supporting the fact that this may not impact the store (think count based)
    kvStore = factory.newStoreWithCapacity(1L);
    StoreEventListener<K, V> listener = addListener(kvStore);
    kvStore.put(k, v);
    kvStore.put(k2, v2);
    verifyListenerInteractions(listener);
    kvStore.replace(getOnlyKey(kvStore.iterator()), v3);
    assertThat(kvStore.get(getOnlyKey(kvStore.iterator())).value(), is(v3));
  }

  @SPITest
  public void testComputeOnEviction() throws Exception {
    kvStore = factory.newStoreWithCapacity(1L);
    StoreEventListener<K, V> listener = addListener(kvStore);
    kvStore.put(k, v);
    kvStore.compute(k2, new BiFunction<K, V, V>() {
      @Override
      public V apply(K mappedKey, V mappedValue) {
        return v2;
      }
    });
    verifyListenerInteractions(listener);
  }

  @SPITest
  public void testComputeIfAbsentOnEviction() throws Exception {
    kvStore = factory.newStoreWithCapacity(1L);
    StoreEventListener<K, V> listener = addListener(kvStore);
    kvStore.put(k, v);
    kvStore.computeIfAbsent(k2, new Function<K, V>() {
      @Override
      public V apply(K mappedKey) {
        return v2;
      }
    });
    verifyListenerInteractions(listener);
  }

  private K getOnlyKey(Store.Iterator<Cache.Entry<K, Store.ValueHolder<V>>> iter)
      throws CacheAccessException {
    if (iter.hasNext()) {
      Cache.Entry<K, Store.ValueHolder<V>> entry = iter.next();
      return entry.getKey();
    }
    return null;
  }

  private void verifyListenerInteractions(StoreEventListener<K, V> listener) {InOrder inOrder = inOrder(listener);
    inOrder.verify(listener).hasListeners();
    inOrder.verify(listener).onCreation(any(factory.getKeyType()), any(factory.getValueType()));
    inOrder.verify(listener).onEviction(any(factory.getKeyType()), any(factory.getValueType()));
    inOrder.verify(listener).fireAllEvents();
    inOrder.verify(listener).purgeOrFireRemainingEvents();
    inOrder.verifyNoMoreInteractions();
  }

  private StoreEventListener<K, V> addListener(Store<K, V> kvStore) {
    StoreEventListener<K, V> listener = mock(StoreEventListener.class);
    when(listener.hasListeners()).thenReturn(true);

    kvStore.enableStoreEventNotifications(listener);
    return listener;
  }
}


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

import org.ehcache.events.StoreEventListener;
import org.ehcache.exceptions.CacheAccessException;
import org.ehcache.function.BiFunction;
import org.ehcache.spi.cache.Store;
import org.ehcache.spi.test.After;
import org.ehcache.spi.test.Before;
import org.ehcache.spi.test.LegalSPITesterException;
import org.ehcache.spi.test.SPITest;
import org.mockito.InOrder;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * StoreCreationEventListenerTest
 */
public class StoreRemovalEventListenerTest<K, V> extends SPIStoreTester<K, V> {


  private Store<K, V> store;

  public StoreRemovalEventListenerTest(StoreFactory<K, V> factory) {
    super(factory);
  }

  @Before
  public void setUp() {
    store = factory.newStore();
  }

  @After
  public void tearDown() {
    if (store != null) {
      factory.close(store);
      store = null;
    }
  }

  @SPITest
  public void testRemoveRemoves() throws LegalSPITesterException {
    try {
      K key = factory.createKey(8734L);
      store.put(key, factory.createValue(834L));
      StoreEventListener<K, V> listener = addListener(store);
      store.remove(key);
      verifyListenerInteractions(listener);
    } catch (CacheAccessException e) {
      throw new LegalSPITesterException("Warning, an exception is thrown due to the SPI test");
    }
  }

  @SPITest
  public void testRemove2ArgsRemoves() throws LegalSPITesterException {
    try {
      K key = factory.createKey(8734L);
      V value = factory.createValue(834L);
      store.put(key, value);
      StoreEventListener<K, V> listener = addListener(store);
      store.remove(key, value);
      verifyListenerInteractions(listener);
    } catch (CacheAccessException e) {
      throw new LegalSPITesterException("Warning, an exception is thrown due to the SPI test");
    }
  }

  @SPITest
  public void testComputeRemoves() throws LegalSPITesterException {

    try {
      K key = factory.createKey(125L);
      store.put(key, factory.createValue(125L));
      StoreEventListener<K, V> listener = addListener(store);
      store.compute(key, new BiFunction<K, V, V>() {
        @Override
        public V apply(K k, V v) {
          return null;
        }
      });
      verifyListenerInteractions(listener);
    } catch (CacheAccessException e) {
      throw new LegalSPITesterException("Warning, an exception is thrown due to the SPI test");
    }
  }

  @SPITest
  public void testComputeIfPresentRemoves() throws LegalSPITesterException {

    try {
      K key = factory.createKey(125L);
      store.put(key, factory.createValue(125L));
      StoreEventListener<K, V> listener = addListener(store);
      store.computeIfPresent(key, new BiFunction<K, V, V>() {
        @Override
        public V apply(K k, V v) {
          return null;
        }
      });
      verifyListenerInteractions(listener);
    } catch (CacheAccessException e) {
      throw new LegalSPITesterException("Warning, an exception is thrown due to the SPI test");
    }
  }

  private void verifyListenerInteractions(StoreEventListener<K, V> listener) {InOrder inOrder = inOrder(listener);
    inOrder.verify(listener).hasListeners();
    inOrder.verify(listener).onRemoval(any(factory.getKeyType()), any(factory.getValueType()));
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

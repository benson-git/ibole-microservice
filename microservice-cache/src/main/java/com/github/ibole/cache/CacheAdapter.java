/**
 * 
 */
package com.github.ibole.cache;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/*********************************************************************************************
 * .
 * 
 * 
 * <p>
 * Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p>
 * </p>
 *********************************************************************************************/


/**
 * Cache Adapter.
 * <p>
 * Default behaviour and Metrics handling (hits, misses, updates and removes meters).
 */
public abstract class CacheAdapter implements Cache {
  /**
   * Cache Event.
   * <p>
   * Used to collect metrics.
   * 
   */
  public static enum CacheEvent {
    HIT, MISS, GET, SET, REMOVE;

    /**
     * Close metric recording.
     * 
     */
    public interface Closeable extends AutoCloseable {
      @Override
      void close();
    }

    /**
     * No-operation closeable.
     */
    public static final Closeable NOOP_CLOSEABLE = new Closeable() {
      @Override
      public void close() {}
    };

    /**
     * No-operation metrics handler.
     */
    public static final Function<CacheEvent, Closeable> NOOP_HANDLER =
        new Function<CacheEvent, Closeable>() {
          @Override
          public Closeable apply(CacheEvent event) {
            return NOOP_CLOSEABLE;
          }
        };
  }

  private final Function<CacheEvent, CacheEvent.Closeable> handler;

  /**
   * Create a Cache without metrics handling.
   */
  protected CacheAdapter() {
    this(null);
  }

  /**
   * Create a Cache with metrics handling.
   * 
   * @param handler Metrics handler
   */
  protected CacheAdapter(Function<CacheEvent, CacheEvent.Closeable> handler) {
    this.handler = handler == null ? CacheEvent.NOOP_HANDLER : handler;
  }

  @Override
  public final boolean has(String key) {
    return get(key) != null;
  }

  @Override
  public final <T> T get(String key) {
    try (CacheEvent.Closeable getTimer = handler.apply(CacheEvent.GET)) {
      T value = doGet(key);
      handler.apply(value == null ? CacheEvent.MISS : CacheEvent.HIT).close();
      return value;
    }
  }

  @Override
  public final <T> T get(String key, String field) {
    try (CacheEvent.Closeable getTimer = handler.apply(CacheEvent.GET)) {
      T value = doGet(key, field);
      handler.apply(value == null ? CacheEvent.MISS : CacheEvent.HIT).close();
      return value;
    }
  }

  @Override
  public final <T> Optional<T> getOptional(String key) {
    return Optional.ofNullable(this.<T>get(key));
  }

  @Override
  public final <T> T getOrSetDefault(String key, T defaultValue) {
    return getOrSetDefault(key, 0, defaultValue);
  }

  @Override
  public final <T> T getOrSetDefault(String key, Supplier<T> defaultValueSupplier) {
    return getOrSetDefault(key, 0, defaultValueSupplier.get());
  }

  @Override
  public final <T> T getOrSetDefault(String key, int ttlSeconds, T defaultValue) {
    T value = this.<T>get(key);
    if (value == null) {
      set(ttlSeconds, key, defaultValue);
      return defaultValue;
    }
    return value;
  }

  @Override
  public final <T> T getOrSetDefault(String key, int ttlSeconds, Supplier<T> defaultValueSupplier) {
    return getOrSetDefault(key, ttlSeconds, defaultValueSupplier.get());
  }

  @Override
  public final <T> void expire(String key, int ttlSeconds) {
    try (CacheEvent.Closeable setTimer = handler.apply(CacheEvent.SET)) {
      doExpire(key, ttlSeconds);
    }
  }

  @Override
  public final <T> void set(String key, T value) {
    set(0, key, value);
  }

  @Override
  public final <T> void set(String key, String field, T value) {
    try (CacheEvent.Closeable setTimer = handler.apply(CacheEvent.SET)) {
      doSet(key, field, value);
    }
  }

  @Override
  public final <T> void set(int ttlSeconds, String key, T value) {
    try (CacheEvent.Closeable setTimer = handler.apply(CacheEvent.SET)) {
      doSet(ttlSeconds, key, value);
    }
  }

  @Override
  public final <T> void set(int ttlSeconds, String key, String field, T value) {
    try (CacheEvent.Closeable setTimer = handler.apply(CacheEvent.SET)) {
      doSet(ttlSeconds, key, field, value);
    }
  }

  @Override
  public final void remove(String key) {
    try (CacheEvent.Closeable removeTimer = handler.apply(CacheEvent.REMOVE)) {
      doRemove(key);
    }
  }

  /**
   * Concrete SET.
   * 
   * @param <T> Parameterized type of value
   * @param ttlSeconds TTL in seconds
   * @param key Cache Key
   */
  protected abstract <T> void doExpire(String key, int ttlSeconds);

  /**
   * Concrete GET.
   * 
   * @param <T> Parameterized type of value
   * @param key Cache Key
   * 
   * @return Value or null
   */
  protected abstract <T> T doGet(String key);

  /**
   * Concrete GET.
   * 
   * @param <T> Parameterized type of value
   * @param key Cache Key
   * @param field Cache field
   * @return Value or null
   */
  protected abstract <T> T doGet(String key, String field);

  /**
   * Concrete SET.
   * 
   * @param <T> Parameterized type of value
   * @param key Cache Key
   * @param value Value
   */
  protected abstract <T> void doSet(String key, T value);

  /**
   * Concrete SET.
   * 
   * @param <T> Parameterized type of value
   * @param field Cache field
   * @param key Cache Key
   * @param value Value
   */
  protected abstract <T> void doSet(String key, String field, T value);

  /**
   * Concrete SET.
   * 
   * @param <T> Parameterized type of value
   * @param ttlSeconds TTL in seconds
   * @param key Cache Key
   * @param value Value
   */
  protected abstract <T> void doSet(int ttlSeconds, String key, T value);

  /**
   * Concrete SET.
   * 
   * @param <T> Parameterized type of value
   * @param ttlSeconds TTL in seconds
   * @param key Cache Key
   * @param field Cache field
   * @param value Value
   */
  protected abstract <T> void doSet(int ttlSeconds, String key, String field, T value);

  /**
   * Concrete REMOVE.
   * 
   * @param key Cache Key
   */
  protected abstract void doRemove(String key);
}

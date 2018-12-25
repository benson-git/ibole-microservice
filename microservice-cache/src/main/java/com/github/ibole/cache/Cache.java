package com.github.ibole.cache;

import java.util.Optional;
import java.util.function.Supplier;

/*********************************************************************************************
 * .
 * 
 * 
 * <p>
 * Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p>.
 * </p>
 *********************************************************************************************/


/**
 * Refer to https://github.com/werval/werval.
 * 
 * @author bwang
 *
 */
public interface Cache {
  /**
   * Check if the Cache has an object for a given key.
   * 
   * @param key Cache Key
   * 
   * @return {@literal true} if the cache has an object for the given key
   */
  boolean has(String key);

  /**
   * Fetch cached object for a given key.
   * 
   * @param <T> Object Type
   * @param key Cache Key
   * 
   * @return The cached object for the given key, or {@literal null} if absent
   */
  <T> T get(String key);
  
  /**
   * Fetch cached object for a given key and field.
   * 
   * @param <T> Object Type
   * @param key Cache Key
   * @param field Cache field
   * 
   * @return The cached object for the given key, or {@literal null} if absent
   */
  <T> T get(String key, String field);

  /**
   * Optionaly fetch cached object for a given key.
   * 
   * @param <T> Object Type
   * @param key Cache Key
   * 
   * @return An Optional of the cached object for the given key
   */
  <T> Optional<T> getOptional(String key);

  /**
   * Fetch cached object for a given key or set a non-expiring default value.
   * <p>
   * If the cache has a non-expired object for the given key, it is returned.
   * <p>
   * Otherwise, the given default value is set in the cache and returned.
   * 
   * @param <T> Object Type
   * @param key Cache Key
   * @param defaultValue Default Value
   * 
   * @return The existing cached object for the given key, or the given default value, never return
   *         {@literal null}
   */
  <T> T getOrSetDefault(String key, T defaultValue);

  /**
   * Fetch cached object for a given key or set a non-expiring default value.
   * <p>
   * If the cache has a non-expired object for the given key, it is returned.
   * <p>
   * Otherwise, the given default value is set in the cache and returned.
   * 
   * @param <T> Object Type
   * @param key Cache Key
   * @param defaultValueSupplier Default Value Supplier
   * 
   * @return The existing cached object for the given key, or the given default value, never return
   *         {@literal null}
   */
  <T> T getOrSetDefault(String key, Supplier<T> defaultValueSupplier);

  /**
   * Fetch cached object for a given key or set an expiring default value.
   * <p>
   * If the cache has a non-expired object for the given key, it is returned.
   * <p>
   * Otherwise, the given default value is set in the cache and returned.
   * 
   * @param <T> Object Type
   * @param key Cache Key
   * @param ttlSeconds Default Value Time To Live in seconds. If {@literal 0} ({@literal ZERO}),
   *        then the entry will never expire
   * @param defaultValue Default Value
   * 
   * @return The existing cached object for the given key, or the given default value, never return
   *         {@literal null}
   */
  <T> T getOrSetDefault(String key, int ttlSeconds, T defaultValue);

  /**
   * Fetch cached object for a given key or set an expiring default value.
   * <p>
   * If the cache has a non-expired object for the given key, it is returned.
   * <p>
   * Otherwise, the given default value is set in the cache and returned.
   * 
   * @param <T> Object Type
   * @param key Cache Key
   * @param ttlSeconds Default Value Time To Live in seconds. If {@literal 0} ({@literal ZERO}),
   *        then the entry will never expire
   * @param defaultValueSupplier Default Value Supplier
   * 
   * @return The existing cached object for the given key, or the given default value, never return
   *         {@literal null}
   */
  <T> T getOrSetDefault(String key, int ttlSeconds, Supplier<T> defaultValueSupplier);
  

  /**
   * Set the ttl for the given key in the Cache.
   * 
   * @param <T> Object Type
   * @param ttlSeconds Time To Live in seconds. If {@literal 0} ({@literal ZERO}), then the entry
   *        will not expire
   * @param key Cache Key
   */
  <T> void expire(String key, int ttlSeconds);
  /**
   * Set a non-expiring object for a given key in the Cache.
   * 
   * @param <T> Object Type
   * @param key Cache Key
   * @param value Value Object
   */
  <T> void set(String key, T value);
  
  /**
   * Set a non-expiring object for a given key and field in the Cache.
   * 
   * @param <T> Object Type
   * @param key Cache Key
   * @param field Cache field
   * @param value Value Object
   */
  <T> void set(String key, String field, T value);

  /**
   * Set an expiring object for a given key in the Cache.
   * 
   * @param <T> Object Type
   * @param ttlSeconds Time To Live in seconds. If {@literal 0} ({@literal ZERO}), then the entry
   *        will not expire
   * @param key Cache Key
   * @param value Value Object
   */
  <T> void set(int ttlSeconds, String key, T value);
  
  /**
   * Set an expiring object for a given key and field in the Cache.
   * 
   * @param <T> Object Type
   * @param ttlSeconds Time To Live in seconds. If {@literal 0} ({@literal ZERO}), then the entry
   *        will not expire
   * @param key Cache Key
   * @param field Cache field
   * @param value Value Object
   */
  <T> void set(int ttlSeconds, String key,  String field, T value);

  /**
   * Remove a Cache entry.
   * 
   * @param key Key of the entry to remove
   */
  void remove(String key);
}

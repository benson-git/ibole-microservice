package com.github.ibole.microservice.common.utils;

import java.util.Arrays;


/**
 * This class provides constants and static methods to help in overriding {@code hashCode}. The
 * class supplies a constant {@code SEED} value and a set of overloaded {@code hash} methods to deal
 * with primitives, arrays and {@code java.lang.Object}s. Each {@code hash} method returns a new
 * hash code value resulting in the combination of an actual hash code value and a new field to take
 * into account in the hash code computation:
 * 
 * <pre>
 * int h = SEED;
 * h = hash(h, fieldA);
 * h = hash(h, fieldB);
 * return h;
 * </pre>
 * 
 * where {@code h} is the actual hash code value and {@code fieldA} and {@code
 * fieldB} are two significant fields to hash and combine with the actual value to produce a new
 * hash code value.
 * 
 */
public final class HashCodeUtil {

  /** A seed value for a hash code to compute. */
  public final static int SEED = 1;

  /*
   * Suppresses default constructor, ensuring non-instantiability
   */
  @SuppressWarnings("nls")
  private HashCodeUtil() {
    throw new AssertionError("not aimed to be instantiated");
  }

  /**
   * Returns a hash code value combining the given actual value and specified {@code int} value.
   * 
   * @param actual the actual hash code value
   * @param field the {@code int} value to be combined with {@code actual}
   * @return a new hash code value combining {@code actual} and {@code field}
   */
  public static int hash(final int actual, final int field) {
    /*
     * ((actual << 5) - actual) is a performance optimization for (31 * actual)
     */
    return (actual << 5) - actual + field;
  }

  /**
   * Returns a hash code value combining the given actual value and specified {@code boolean} value.
   * 
   * @param actual the actual hash code value
   * @param field the {@code boolean} value to be combined with {@code actual}
   * @return a new hash code value combining {@code actual} and {@code field}
   */
  public static int hash(final int actual, final boolean field) {
    final int h = field ? 1231 : 1237;
    return hash(actual, h);
  }

  /**
   * Returns a hash code value combining the given actual value and specified {@code long} value.
   * 
   * @param actual the actual hash code value
   * @param field the {@code long} value to be combined with {@code actual}
   * @return a new hash code value combining {@code actual} and {@code field}
   */
  public static int hash(final int actual, final long field) {
    final int h = (int) (field ^ field >>> 32);
    return hash(actual, h);
  }

  /**
   * Returns a hash code value combining the given actual value and specified {@code float} value.
   * 
   * @param actual the actual hash code value
   * @param field the {@code float} value to be combined with {@code actual}
   * @return a new hash code value combining {@code actual} and {@code field}
   */
  public static int hash(final int actual, final float field) {
    final int h = Float.floatToIntBits(field);
    return hash(actual, h);
  }

  /**
   * Returns a hash code value combining the given actual value and specified {@code double} value.
   * 
   * @param actual the actual hash code value
   * @param field the {@code double} value to be combined with {@code actual}
   * @return a new hash code value combining {@code actual} and {@code field}
   */
  public static int hash(final int actual, final double field) {
    final long bits = Double.doubleToLongBits(field);
    return hash(actual, bits);
  }

  /**
   * Returns a hash code value combining the given actual value and specified array of {@code int}s.
   * 
   * @param actual the actual hash code value
   * @param field the array of {@code int}s to be combined with {@code actual}
   * @return a new hash code value combining {@code actual} and {@code field}
   */
  public static int hash(final int actual, final int[] field) {
    final int h = Arrays.hashCode(field);
    return hash(actual, h);
  }

  /**
   * Returns a hash code value combining the given actual value and specified array of
   * {@code boolean}s.
   * 
   * @param actual the actual hash code value
   * @param field the array of {@code boolean}s to be combined with {@code actual}
   * @return a new hash code value combining {@code actual} and {@code field}
   */
  public static int hash(final int actual, final boolean[] field) {
    final int h = Arrays.hashCode(field);
    return hash(actual, h);
  }

  /**
   * Returns a hash code value combining the given actual value and specified array of {@code byte}
   * s.
   * 
   * @param actual the actual hash code value
   * @param field the array of {@code byte}s to be combined with {@code actual}
   * @return a new hash code value combining {@code actual} and {@code field}
   */
  public static int hash(final int actual, final byte[] field) {
    final int h = Arrays.hashCode(field);
    return hash(actual, h);
  }

  /**
   * Returns a hash code value combining the given actual value and specified array of {@code char}
   * s.
   * 
   * @param actual the actual hash code value
   * @param field the array of {@code char}s to be combined with {@code actual}
   * @return a new hash code value combining {@code actual} and {@code field}
   */
  public static int hash(final int actual, final char[] field) {
    final int h = Arrays.hashCode(field);
    return hash(actual, h);
  }

  /**
   * Returns a hash code value combining the given actual value and specified array of {@code short}
   * s.
   * 
   * @param actual the actual hash code value
   * @param field the array of {@code short}s to be combined with {@code actual}
   * @return a new hash code value combining {@code actual} and {@code field}
   */
  public static int hash(final int actual, final short[] field) {
    final int h = Arrays.hashCode(field);
    return hash(actual, h);
  }

  /**
   * Returns a hash code value combining the given actual value and specified array of {@code long}
   * s.
   * 
   * @param actual the actual hash code value
   * @param field the array of {@code long}s to be combined with {@code actual}
   * @return a new hash code value combining {@code actual} and {@code field}
   */
  public static int hash(final int actual, final long[] field) {
    final int h = Arrays.hashCode(field);
    return hash(actual, h);
  }

  /**
   * Returns a hash code value combining the given actual value and specified array of {@code float}
   * s.
   * 
   * @param actual the actual hash code value
   * @param field the array of {@code float}s to be combined with {@code actual}
   * @return a new hash code value combining {@code actual} and {@code field}
   */
  public static int hash(final int actual, final float[] field) {
    final int h = Arrays.hashCode(field);
    return hash(actual, h);
  }

  /**
   * Returns a hash code value combining the given actual value and specified array of
   * {@code double}s.
   * 
   * @param actual the actual hash code value
   * @param field the array of {@code double}s to be combined with {@code actual}
   * @return a new hash code value combining {@code actual} and {@code field}
   */
  public static int hash(final int actual, final double[] field) {
    final int h = Arrays.hashCode(field);
    return hash(actual, h);
  }

  /**
   * Returns a hash code value combining the given actual value and specified {@code Object}.
   * 
   * @param actual the actual hash code value
   * @param field the {@code Object} to be combined with {@code actual}
   * @return a new hash code value combining {@code actual} and {@code field}
   */
  public static int hash(final int actual, final Object field) {
    final int h = field == null ? 0 : field.hashCode();
    return hash(actual, h);
  }

  /**
   * Returns a hash code value combining the given actual value and specified array of
   * {@code Object}s.
   * 
   * @param actual the actual hash code value
   * @param field the array of {@code Object}s to be combined with {@code actual}
   * @return a new hash code value combining {@code actual} and {@code field}
   */
  public static int hash(final int actual, final Object[] field) {
    final int h = field == null ? 0 : Arrays.hashCode(field);
    return hash(actual, h);
  }
}


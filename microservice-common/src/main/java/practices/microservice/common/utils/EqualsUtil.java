package practices.microservice.common.utils;

import java.util.Arrays;


/**
 * This class provides static methods to help in overriding
 * {@link Object#equals(Object)}. The class provides a set of overloaded {@code
 * equal} methods to deal with primitives, arrays and {@code java.lang.Object}s.
 * <p>
 * Pattern:
 * 
 * <pre>
 * 
 * 
 * &#064;Override
 * public boolean equals(final Object obj) {
 *     if (this == obj) {
 * 	return true;
 *     }
 *     if (null == obj) {
 * 	return false;
 *     }
 *     if (!getClass().equals(obj.getClass())) {
 * 	return false;
 *     }
 *     final MyClass other = (MyClass) obj;
 *     return EqualsUtil.equal(getA(), other.getA())
 * 	    &amp;&amp; EqualsUtil.equal(getB(), other.getB());
 * }
 * </pre>
 * 
 * @see HashCodeUtil
 */
public final class EqualsUtil {

    /*
     * Suppresses default constructor, ensuring non-instantiability
     */
    @SuppressWarnings("nls")
    private EqualsUtil() {
	throw new AssertionError("not aimed to be instantiated");
    }

    /**
     * Returns {@code true} if the two specified {@code int}s are equal to one
     * another.
     * 
     * @param field
     *        one {@code int} to be tested for equality
     * @param other
     *        the other {@code int} to be tested for equality
     * @return {@code true} if the two {@code int}s are equal; {@code false}
     *         otherwise
     */
    public static boolean equal(final int field, final int other) {
	return field == other;
    }

    /**
     * Returns {@code true} if the two specified {@code boolean}s are equal to
     * one another.
     * 
     * @param field
     *        one {@code boolean} to be tested for equality
     * @param other
     *        the other {@code boolean} to be tested for equality
     * @return {@code true} if the two {@code boolean}s are equal; {@code false}
     *         otherwise
     */
    public static boolean equal(final boolean field, final boolean other) {
	return field == other;
    }

    /**
     * Returns {@code true} if the two specified {@code long}s are equal to one
     * another.
     * 
     * @param field
     *        one {@code long} to be tested for equality
     * @param other
     *        the other {@code long} to be tested for equality
     * @return {@code true} if the two {@code long}s are equal; {@code false}
     *         otherwise
     */
    public static boolean equal(final long field, final long other) {
	return field == other;
    }

    /**
     * Returns {@code true} if the two specified {@code float}s are equal to one
     * another.
     * 
     * @param field
     *        one {@code float} to be tested for equality
     * @param other
     *        the other {@code float} to be tested for equality
     * @return {@code true} if the two {@code float}s are equal; {@code false}
     *         otherwise
     */
    public static boolean equal(final float field, final float other) {
	return Float.floatToIntBits(field) == Float.floatToIntBits(other);
    }

    /**
     * Returns {@code true} if the two specified {@code double}s are equal to
     * one another.
     * 
     * @param field
     *        one {@code double} to be tested for equality
     * @param other
     *        the other {@code double} to be tested for equality
     * @return {@code true} if the two {@code double}s are equal; {@code false}
     *         otherwise
     */
    public static boolean equal(final double field, final double other) {
	return Double.doubleToLongBits(field) == Double.doubleToLongBits(other);
    }

    /**
     * Returns {@code true} if the two specified {@code String}s are equal to
     * one another ignoring case. Two strings {@code s1} and {@code s2} are
     * considered equal ignoring case if {@code (s1 == null ? s2 == null :
     * s1.equalsIgnoreCase(s2))}.
     * <p>
     * To compare two {@code String} objects case sensitively see
     * {@link #equal(Object, Object)}.
     * 
     * @param field
     *        one {@code String} to be tested for equality
     * @param other
     *        the other {@code String} to be tested for equality
     * @return {@code true} if the two {@code String}s are equal; {@code false}
     *         otherwise
     * @see String#equalsIgnoreCase(String)
     */
    public static boolean equalIgnoreCase(final String field, final String other) {
	return field == null ? other == null : field.equalsIgnoreCase(other);
    }

    /**
     * Returns {@code true} if the two specified {@code Object}s are equal to
     * one another. Two objects {@code o1} and {@code o2} are considered equal
     * if {@code (o1 == null ? o2 == null : o1.equals(o2))}.
     * 
     * @param field
     *        one {@code Object} to be tested for equality
     * @param other
     *        the other {@code Object} to be tested for equality
     * @return {@code true} if the two {@code Object}s are equal; {@code false}
     *         otherwise
     */
    public static boolean equal(final Object field, final Object other) {
	return field == null ? other == null : field.equals(other);
    }

    /**
     * Returns {@code true} if the two specified arrays of {@code int}s are
     * equal to one another. Two arrays are considered equal if both arrays
     * contain the same number of elements, and all corresponding pairs of
     * elements in the two arrays are equal. In other words, two arrays are
     * equal if they contain the same elements in the same order. Also, two
     * array references are considered equal if both are {@code null}.
     * 
     * @param field
     *        one array of {@code int}s to be tested for equality
     * @param other
     *        the other array of {@code int}s to be tested for equality
     * @return {@code true} if the two arrays of {@code int}s are equal; {@code
     *         false} otherwise
     */
    public static boolean equal(final int[] field, final int[] other) {
	return Arrays.equals(field, other);
    }

    /**
     * Returns {@code true} if the two specified arrays of {@code boolean}s are
     * equal to one another. Two arrays are considered equal if both arrays
     * contain the same number of elements, and all corresponding pairs of
     * elements in the two arrays are equal. In other words, two arrays are
     * equal if they contain the same elements in the same order. Also, two
     * array references are considered equal if both are {@code null}.
     * 
     * @param field
     *        one array of {@code boolean}s to be tested for equality
     * @param other
     *        the other array of {@code boolean}s to be tested for equality
     * @return {@code true} if the two arrays of {@code boolean}s are equal;
     *         {@code false} otherwise
     */
    public static boolean equal(final boolean[] field, final boolean[] other) {
	return Arrays.equals(field, other);
    }

    /**
     * Returns {@code true} if the two specified arrays of {@code byte}s are
     * equal to one another. Two arrays are considered equal if both arrays
     * contain the same number of elements, and all corresponding pairs of
     * elements in the two arrays are equal. In other words, two arrays are
     * equal if they contain the same elements in the same order. Also, two
     * array references are considered equal if both are {@code null}.
     * 
     * @param field
     *        one array of {@code byte}s to be tested for equality
     * @param other
     *        the other array of {@code byte}s to be tested for equality
     * @return {@code true} if the two arrays of {@code byte}s are equal;
     *         {@code false} otherwise
     */
    public static boolean equal(final byte[] field, final byte[] other) {
	return Arrays.equals(field, other);
    }

    /**
     * Returns {@code true} if the two specified arrays of {@code char}s are
     * equal to one another. Two arrays are considered equal if both arrays
     * contain the same number of elements, and all corresponding pairs of
     * elements in the two arrays are equal. In other words, two arrays are
     * equal if they contain the same elements in the same order. Also, two
     * array references are considered equal if both are {@code null}.
     * 
     * @param field
     *        one array of {@code char}s to be tested for equality
     * @param other
     *        the other array of {@code char}s to be tested for equality
     * @return {@code true} if the two arrays of {@code char}s are equal;
     *         {@code false} otherwise
     */
    public static boolean equal(final char[] field, final char[] other) {
	return Arrays.equals(field, other);
    }

    /**
     * Returns {@code true} if the two specified arrays of {@code short}s are
     * equal to one another. Two arrays are considered equal if both arrays
     * contain the same number of elements, and all corresponding pairs of
     * elements in the two arrays are equal. In other words, two arrays are
     * equal if they contain the same elements in the same order. Also, two
     * array references are considered equal if both are {@code null}.
     * 
     * @param field
     *        one array of {@code short}s to be tested for equality
     * @param other
     *        the other array of {@code short}s to be tested for equality
     * @return {@code true} if the two arrays of {@code short}s are equal;
     *         {@code false} otherwise
     */
    public static boolean equal(final short[] field, final short[] other) {
	return Arrays.equals(field, other);
    }

    /**
     * Returns {@code true} if the two specified arrays of {@code long}s are
     * equal to one another. Two arrays are considered equal if both arrays
     * contain the same number of elements, and all corresponding pairs of
     * elements in the two arrays are equal. In other words, two arrays are
     * equal if they contain the same elements in the same order. Also, two
     * array references are considered equal if both are {@code null}.
     * 
     * @param field
     *        one array of {@code long}s to be tested for equality
     * @param other
     *        the other array of {@code long}s to be tested for equality
     * @return {@code true} if the two arrays of {@code long}s are equal;
     *         {@code false} otherwise
     */
    public static boolean equal(final long[] field, final long[] other) {
	return Arrays.equals(field, other);
    }

    /**
     * Returns {@code true} if the two specified arrays of {@code float}s are
     * equal to one another. Two arrays are considered equal if both arrays
     * contain the same number of elements, and all corresponding pairs of
     * elements in the two arrays are equal. In other words, two arrays are
     * equal if they contain the same elements in the same order. Also, two
     * array references are considered equal if both are {@code null}.
     * 
     * @param field
     *        one array of {@code float}s to be tested for equality
     * @param other
     *        the other array of {@code float}s to be tested for equality
     * @return {@code true} if the two arrays of {@code float}s are equal;
     *         {@code false} otherwise
     */
    public static boolean equal(final float[] field, final float[] other) {
	return Arrays.equals(field, other);
    }

    /**
     * Returns {@code true} if the two specified arrays of {@code double}s are
     * equal to one another. Two arrays are considered equal if both arrays
     * contain the same number of elements, and all corresponding pairs of
     * elements in the two arrays are equal. In other words, two arrays are
     * equal if they contain the same elements in the same order. Also, two
     * array references are considered equal if both are {@code null}.
     * 
     * @param field
     *        one array of {@code double}s to be tested for equality
     * @param other
     *        the other array of {@code double}s to be tested for equality
     * @return {@code true} if the two arrays of {@code double}s are equal;
     *         {@code false} otherwise
     */
    public static boolean equal(final double[] field, final double[] other) {
	return Arrays.equals(field, other);
    }

    /**
     * Returns true if the two specified arrays of {@code Object}s are equal to
     * one another. The two arrays are considered equal if both arrays contain
     * the same number of elements, and all corresponding pairs of elements in
     * the two arrays are equal. Two elements {@code e1} and {@code e2} are
     * considered equal if {@code (e1 == null ? e2 == null : e1.equals(e2))}. In
     * other words, the two arrays are equal if they contain the same elements
     * in the same order. Also, two array references are considered equal if
     * both are {@code null}.
     * 
     * @param field
     *        one array of {@code Object}s to be tested for equality
     * @param other
     *        the other array of {@code Object}s to be tested for equality
     * @return {@code true} if the two arrays of {@code Object}s are equal;
     *         {@code false} otherwise
     */
    public static boolean equal(final Object[] field, final Object[] other) {
	return Arrays.equals(field, other);
    }
}

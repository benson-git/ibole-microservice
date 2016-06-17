package practices.microservice.common.utils;

/**
 * This class consists exclusively of static methods to help in
 * {@link Object#toString()} method overriding.
 * <p>
 * Pattern:
 * 
 * <pre>
 * 
 * 
 * &#064;Override
 * public String toString() {
 *     final StringBuilder sb = ToStringUtil.start(&quot;a&quot;, a);
 *     ToStringUtil.append(sb, &quot;b&quot;, b);
 *     ToStringUtil.append(sb, &quot;c&quot;, c);
 *     return ToStringUtil.end(sb);
 * }
 * </pre>
 */
public final class ToStringUtil {

    private final static int CAPACITY = 128;

    /*
     * Suppresses default constructor, ensuring non-instantiability
     */
    @SuppressWarnings("nls")
    private ToStringUtil() {
	throw new AssertionError("not aimed to be instantiated");
    }

    /**
     * Creates and return a new {@code StringBuilder} with the given {@code
     * field} and {@code value} appended after the opening brace.
     * <p>
     * The returned buffer is created with a default capacity of {@code 128}.
     * 
     * @param field
     *        the name of the field
     * @param value
     *        the value of the field
     * @return a new buffer
     * @see StringBuilder#StringBuilder(int)
     * @see #start(int, String, Object)
     */
    public static StringBuilder start(final String field, final Object value) {
	return start(CAPACITY, field, value);
    }

    /**
     * Creates and return a new {@code StringBuilder} with the given {@code
     * capacity}, and {@code field} and {@code value} appended after the opening
     * brace.
     * 
     * @param capacity
     *        the capacity for the buffer
     * @param field
     *        the name of the field
     * @param value
     *        the value of the field
     * @return a new buffer
     * @throws NegativeArraySizeException
     *         if {@code capacity} is negative
     * @see #start(String, Object)
     */
    public static StringBuilder start(final int capacity, final String field,
	    final Object value) {
	final StringBuilder buffer = new StringBuilder(capacity);
	buffer.append('{');
	makeKeyValue(buffer, field, value);
	return buffer;
    }

    /**
     * Appends the given {@code field} and {@code value} to {@code buffer}.
     * 
     * @param buffer
     *        the buffer to append {@code field} and {@code value} to
     * @param field
     *        the name of the field
     * @param value
     *        the value of the field
     * @return the buffer itself
     * @throws NullPointerException
     *         if {@code buffer} is {@code null}
     */
    public static StringBuilder append(final StringBuilder buffer,
	    final String field, final Object value) {
	buffer.append(',');
	buffer.append(' ');
	makeKeyValue(buffer, field, value);
	return buffer;
    }

    /**
     * Appends the given {@code field} and {@code value} to {@code buffer}. The
     * {@code char} value is converted to a {@code String} before the method
     * invokes and returns from {@link #append(StringBuilder, String, Object)}.
     * 
     * @param buffer
     *        the buffer to append {@code field} and {@code value} to
     * @param field
     *        the name of the field
     * @param value
     *        the {@code char} value of the field
     * @return the buffer itself
     * @throws NullPointerException
     *         if {@code buffer} is {@code null}
     */
    public static StringBuilder append(final StringBuilder buffer,
	    final String field, final char value) {
	return append(buffer, field, String.valueOf(value));
    }

    /**
     * Appends the closing brace and returns {@code buffer.toString()}.
     * 
     * @param buffer
     *        the buffer
     * @return {@code buffer.toString()} after appending the closing brace
     * @throws NullPointerException
     *         if {@code buffer} is {@code null}
     */
    public static String end(final StringBuilder buffer) {
	buffer.append('}');
	return buffer.toString();
    }

    private static void makeKeyValue(final StringBuilder buffer,
	    final String field, final Object value) {
	buffer.append(field);
	buffer.append('=');
	buffer.append(value);
    }
}

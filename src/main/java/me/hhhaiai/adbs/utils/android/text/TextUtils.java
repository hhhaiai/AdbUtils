package me.hhhaiai.adbs.utils.android.text;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.regex.Pattern;

public class TextUtils {
    private static final String TAG = "TextUtils";

    // Zero-width character used to fill ellipsized strings when codepoint length must be preserved.
    /* package */ static final char ELLIPSIS_FILLER = '\uFEFF'; // ZERO WIDTH NO-BREAK SPACE

    // TODO: Based on CLDR data, these need to be localized for Dzongkha (dz) and perhaps
    // Hong Kong Traditional Chinese (zh-Hant-HK), but that may need to depend on the actual word
    // being ellipsized and not the locale.
    private static final String ELLIPSIS_NORMAL = "\u2026"; // HORIZONTAL ELLIPSIS (…)
    private static final String ELLIPSIS_TWO_DOTS = "\u2025"; // TWO DOT LEADER (‥)

    private static final int LINE_FEED_CODE_POINT = 10;
    private static final int NBSP_CODE_POINT = 160;

    /**
     * Flags for {link #makeSafeForPresentation(String, int, float, int)}
     */
    public @interface SafeStringFlags {
    }

    public static String getEllipsisString(TextUtils.TruncateAt method) {
        return (method == TextUtils.TruncateAt.END_SMALL) ? ELLIPSIS_TWO_DOTS : ELLIPSIS_NORMAL;
    }


    private TextUtils() { /* cannot be instantiated */ }

    public static void getChars(CharSequence s, int start, int end,
                                char[] dest, int destoff) {
        Class<? extends CharSequence> c = s.getClass();

        if (c == String.class)
            ((String) s).getChars(start, end, dest, destoff);
        else if (c == StringBuffer.class)
            ((StringBuffer) s).getChars(start, end, dest, destoff);
        else if (c == StringBuilder.class)
            ((StringBuilder) s).getChars(start, end, dest, destoff);
        else if (s instanceof GetChars)
            ((GetChars) s).getChars(start, end, dest, destoff);
        else {
            for (int i = start; i < end; i++)
                dest[destoff++] = s.charAt(i);
        }
    }

    public static int indexOf(CharSequence s, char ch) {
        return indexOf(s, ch, 0);
    }

    public static int indexOf(CharSequence s, char ch, int start) {
        Class<? extends CharSequence> c = s.getClass();

        if (c == String.class)
            return ((String) s).indexOf(ch, start);

        return indexOf(s, ch, start, s.length());
    }

    public static int indexOf(CharSequence s, char ch, int start, int end) {
        Class<? extends CharSequence> c = s.getClass();

        if (s instanceof GetChars || c == StringBuffer.class ||
                c == StringBuilder.class || c == String.class) {
            final int INDEX_INCREMENT = 500;
            char[] temp = new char[INDEX_INCREMENT];

            while (start < end) {
                int segend = start + INDEX_INCREMENT;
                if (segend > end)
                    segend = end;

                getChars(s, start, segend, temp, 0);

                int count = segend - start;
                for (int i = 0; i < count; i++) {
                    if (temp[i] == ch) {
                        recycle(temp);
                        return i + start;
                    }
                }

                start = segend;
            }

            recycle(temp);
            return -1;
        }

        for (int i = start; i < end; i++)
            if (s.charAt(i) == ch)
                return i;

        return -1;
    }

    public static int lastIndexOf(CharSequence s, char ch) {
        return lastIndexOf(s, ch, s.length() - 1);
    }

    public static int lastIndexOf(CharSequence s, char ch, int last) {
        Class<? extends CharSequence> c = s.getClass();

        if (c == String.class)
            return ((String) s).lastIndexOf(ch, last);

        return lastIndexOf(s, ch, 0, last);
    }

    public static int lastIndexOf(CharSequence s, char ch,
                                  int start, int last) {
        if (last < 0)
            return -1;
        if (last >= s.length())
            last = s.length() - 1;

        int end = last + 1;

        Class<? extends CharSequence> c = s.getClass();

        if (s instanceof GetChars || c == StringBuffer.class ||
                c == StringBuilder.class || c == String.class) {
            final int INDEX_INCREMENT = 500;
            char[] temp = new char[INDEX_INCREMENT];

            while (start < end) {
                int segstart = end - INDEX_INCREMENT;
                if (segstart < start)
                    segstart = start;

                getChars(s, segstart, end, temp, 0);

                int count = end - segstart;
                for (int i = count - 1; i >= 0; i--) {
                    if (temp[i] == ch) {
                        recycle(temp);
                        return i + segstart;
                    }
                }

                end = segstart;
            }

            recycle(temp);
            return -1;
        }

        for (int i = end - 1; i >= start; i--)
            if (s.charAt(i) == ch)
                return i;

        return -1;
    }

    public static int indexOf(CharSequence s, CharSequence needle) {
        return indexOf(s, needle, 0, s.length());
    }

    public static int indexOf(CharSequence s, CharSequence needle, int start) {
        return indexOf(s, needle, start, s.length());
    }

    public static int indexOf(CharSequence s, CharSequence needle,
                              int start, int end) {
        int nlen = needle.length();
        if (nlen == 0)
            return start;

        char c = needle.charAt(0);

        for (; ; ) {
            start = indexOf(s, c, start);
            if (start > end - nlen) {
                break;
            }

            if (start < 0) {
                return -1;
            }

            if (regionMatches(s, start, needle, 0, nlen)) {
                return start;
            }

            start++;
        }
        return -1;
    }

    public static boolean regionMatches(CharSequence one, int toffset,
                                        CharSequence two, int ooffset,
                                        int len) {
        int tempLen = 2 * len;
        if (tempLen < len) {
            // Integer overflow; len is unreasonably large
            throw new IndexOutOfBoundsException();
        }
        char[] temp = new char[tempLen];

        getChars(one, toffset, toffset + len, temp, 0);
        getChars(two, ooffset, ooffset + len, temp, len);

        boolean match = true;
        for (int i = 0; i < len; i++) {
            if (temp[i] != temp[i + len]) {
                match = false;
                break;
            }
        }

        recycle(temp);
        return match;
    }

    /**
     * Create a new String object containing the given range of characters
     * from the source string.  This is different than simply calling
     * {link CharSequence#subSequence(int, int) CharSequence.subSequence}
     * in that it does not preserve any style runs in the source sequence,
     * allowing a more efficient implementation.
     */
    public static String substring(CharSequence source, int start, int end) {
        if (source instanceof String)
            return ((String) source).substring(start, end);
        if (source instanceof StringBuilder)
            return ((StringBuilder) source).substring(start, end);
        if (source instanceof StringBuffer)
            return ((StringBuffer) source).substring(start, end);

        char[] temp = new char[end - start];
        getChars(source, start, end, temp, 0);
        String ret = new String(temp, 0, end - start);
        recycle(temp);

        return ret;
    }

    /**
     * Returns a string containing the tokens joined by delimiters.
     *
     * @param delimiter a CharSequence that will be inserted between the tokens. If null, the string
     *                  "null" will be used as the delimiter.
     * @param tokens    an array objects to be joined. Strings will be formed from the objects by
     *                  calling object.toString(). If tokens is null, a NullPointerException will be thrown. If
     *                  tokens is an empty array, an empty string will be returned.
     */
    public static String join(CharSequence delimiter, Object[] tokens) {
        final int length = tokens.length;
        if (length == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(tokens[0]);
        for (int i = 1; i < length; i++) {
            sb.append(delimiter);
            sb.append(tokens[i]);
        }
        return sb.toString();
    }

    /**
     * Returns a string containing the tokens joined by delimiters.
     *
     * @param delimiter a CharSequence that will be inserted between the tokens. If null, the string
     *                  "null" will be used as the delimiter.
     * @param tokens    an array objects to be joined. Strings will be formed from the objects by
     *                  calling object.toString(). If tokens is null, a NullPointerException will be thrown. If
     *                  tokens is empty, an empty string will be returned.
     */
    public static String join(CharSequence delimiter, Iterable tokens) {
        final Iterator<?> it = tokens.iterator();
        if (!it.hasNext()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(it.next());
        while (it.hasNext()) {
            sb.append(delimiter);
            sb.append(it.next());
        }
        return sb.toString();
    }

    /**
     * This method yields the same result as {@code text.split(expression, -1)} except that if
     * {@code text.isEmpty()} then this method returns an empty array whereas
     * {@code "".split(expression, -1)} would have returned an array with a single {@code ""}.
     * <p>
     * The {@code -1} means that trailing empty Strings are not removed from the result; for
     * example split("a,", ","  ) returns {"a", ""}. Note that whether a leading zero-width match
     * can result in a leading {@code ""} depends on whether your app
     * {link android.content.pm.ApplicationInfo#targetSdkVersion targets an SDK version}
     * {code <= 28}; see {link Pattern#split(CharSequence, int)}.
     *
     * @param text       the string to split
     * @param expression the regular expression to match
     * @return an array of strings. The array will be empty if text is empty
     * @throws NullPointerException if expression or text is null
     */
    public static String[] split(String text, String expression) {
        if (text.length() == 0) {
            return EMPTY_STRING_ARRAY;
        } else {
            return text.split(expression, -1);
        }
    }

    /**
     * Splits a string on a pattern. This method yields the same result as
     * {@code pattern.split(text, -1)} except that if {@code text.isEmpty()} then this method
     * returns an empty array whereas {@code pattern.split("", -1)} would have returned an array
     * with a single {@code ""}.
     * <p>
     * The {@code -1} means that trailing empty Strings are not removed from the result;
     * Note that whether a leading zero-width match can result in a leading {@code ""} depends
     * on whether your app {link android.content.pm.ApplicationInfo#targetSdkVersion targets
     * an SDK version} {code <= 28}; see {link Pattern#split(CharSequence, int)}.
     *
     * @param text    the string to split
     * @param pattern the regular expression to match
     * @return an array of strings. The array will be empty if text is empty
     * @throws NullPointerException if expression or text is null
     */
    public static String[] split(String text, Pattern pattern) {
        if (text.length() == 0) {
            return EMPTY_STRING_ARRAY;
        } else {
            return pattern.split(text, -1);
        }
    }

    /**
     * An interface for splitting strings according to rules that are opaque to the user of this
     * interface. This also has less overhead than split, which uses regular expressions and
     * allocates an array to hold the results.
     *
     * <p>The most efficient way to use this class is:
     *
     * <pre>
     * // Once
     * TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(delimiter);
     *
     * // Once per string to split
     * splitter.setString(string);
     * for (String s : splitter) {
     *     ...
     * }
     * </pre>
     */
    public interface StringSplitter extends Iterable<String> {
        public void setString(String string);
    }

    /**
     * A simple string splitter.
     *
     * <p>If the final character in the string to split is the delimiter then no empty string will
     * be returned for the empty string after that delimeter. That is, splitting <tt>"a,b,"</tt> on
     * comma will return <tt>"a", "b"</tt>, not <tt>"a", "b", ""</tt>.
     */
    public static class SimpleStringSplitter implements StringSplitter, Iterator<String> {
        private String mString;
        private char mDelimiter;
        private int mPosition;
        private int mLength;

        /**
         * Initializes the splitter. setString may be called later.
         *
         * @param delimiter the delimeter on which to split
         */
        public SimpleStringSplitter(char delimiter) {
            mDelimiter = delimiter;
        }

        /**
         * Sets the string to split
         *
         * @param string the string to split
         */
        public void setString(String string) {
            mString = string;
            mPosition = 0;
            mLength = mString.length();
        }

        public Iterator<String> iterator() {
            return this;
        }

        public boolean hasNext() {
            return mPosition < mLength;
        }

        public String next() {
            int end = mString.indexOf(mDelimiter, mPosition);
            if (end == -1) {
                end = mLength;
            }
            String nextString = mString.substring(mPosition, end);
            mPosition = end + 1; // Skip the delimiter.
            return nextString;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public static CharSequence stringOrSpannedString(CharSequence source) {
        if (source == null)
            return null;
//        if (source instanceof SpannedString)
//            return source;
//        if (source instanceof Spanned)
//            return new SpannedString(source);

        return source.toString();
    }

    /**
     * Returns true if the string is null or 0-length.
     *
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    /**
     * {}
     */
    public static String nullIfEmpty(String str) {
        return isEmpty(str) ? null : str;
    }

    /**
     * {}
     */
    public static String emptyIfNull(String str) {
        return str == null ? "" : str;
    }

    /**
     * {}
     */
    public static String firstNotEmpty(String a, String b) {
        return !isEmpty(a) ? a : Preconditions.checkStringNotEmpty(b);
    }

    /**
     * {}
     */
    public static int length(String s) {
        return s != null ? s.length() : 0;
    }

    /**
     * @return interned string if it's null.
     */
    public static String safeIntern(String s) {
        return (s != null) ? s.intern() : null;
    }

    /**
     * Returns the length that the specified CharSequence would have if
     * spaces and ASCII control characters were trimmed from the start and end,
     * as by {link String#trim}.
     */
    public static int getTrimmedLength(CharSequence s) {
        int len = s.length();

        int start = 0;
        while (start < len && s.charAt(start) <= ' ') {
            start++;
        }

        int end = len;
        while (end > start && s.charAt(end - 1) <= ' ') {
            end--;
        }

        return end - start;
    }

    /**
     * Returns true if a and b are equal, including if they are both null.
     * <p><i>Note: In platform versions 1.1 and earlier, this method only worked well if
     * both the arguments were instances of String.</i></p>
     *
     * @param a first CharSequence to check
     * @param b second CharSequence to check
     * @return true if a and b are equal
     */
    public static boolean equals(CharSequence a, CharSequence b) {
        if (a == b) return true;
        int length;
        if (a != null && b != null && (length = a.length()) == b.length()) {
            if (a instanceof String && b instanceof String) {
                return a.equals(b);
            } else {
                for (int i = 0; i < length; i++) {
                    if (a.charAt(i) != b.charAt(i)) return false;
                }
                return true;
            }
        }
        return false;
    }


    /**
     *
     */
    public static final int ALIGNMENT_SPAN = 1;
    /**
     *
     */
    public static final int FIRST_SPAN = ALIGNMENT_SPAN;
    /**
     *
     */
    public static final int FOREGROUND_COLOR_SPAN = 2;
    /**
     *
     */
    public static final int RELATIVE_SIZE_SPAN = 3;
    /**
     *
     */
    public static final int SCALE_X_SPAN = 4;
    /**
     *
     */
    public static final int STRIKETHROUGH_SPAN = 5;
    /**
     *
     */
    public static final int UNDERLINE_SPAN = 6;
    /**
     *
     */
    public static final int STYLE_SPAN = 7;
    /**
     *
     */
    public static final int BULLET_SPAN = 8;
    /**
     *
     */
    public static final int QUOTE_SPAN = 9;
    /**
     *
     */
    public static final int LEADING_MARGIN_SPAN = 10;
    /**
     *
     */
    public static final int URL_SPAN = 11;
    /**
     *
     */
    public static final int BACKGROUND_COLOR_SPAN = 12;
    /**
     *
     */
    public static final int TYPEFACE_SPAN = 13;
    /**
     *
     */
    public static final int SUPERSCRIPT_SPAN = 14;
    /**
     *
     */
    public static final int SUBSCRIPT_SPAN = 15;
    /**
     *
     */
    public static final int ABSOLUTE_SIZE_SPAN = 16;
    /**
     *
     */
    public static final int TEXT_APPEARANCE_SPAN = 17;
    /**
     *
     */
    public static final int ANNOTATION = 18;
    /**
     *
     */
    public static final int SUGGESTION_SPAN = 19;
    /**
     *
     */
    public static final int SPELL_CHECK_SPAN = 20;
    /**
     *
     */
    public static final int SUGGESTION_RANGE_SPAN = 21;
    /**
     *
     */
    public static final int EASY_EDIT_SPAN = 22;
    /**
     *
     */
    public static final int LOCALE_SPAN = 23;
    /**
     *
     */
    public static final int TTS_SPAN = 24;
    /**
     *
     */
    public static final int ACCESSIBILITY_CLICKABLE_SPAN = 25;
    /**
     *
     */
    public static final int ACCESSIBILITY_URL_SPAN = 26;
    /**
     *
     */
    public static final int LINE_BACKGROUND_SPAN = 27;
    /**
     *
     */
    public static final int LINE_HEIGHT_SPAN = 28;
    /**
     *
     */
    public static final int LAST_SPAN = LINE_HEIGHT_SPAN;


    /**
     * Replace instances of "^1", "^2", etc. in the
     * <code>template</code> CharSequence with the corresponding
     * <code>values</code>.  "^^" is used to produce a single caret in
     * the output.  Only up to 9 replacement values are supported,
     * "^10" will be produce the first replacement value followed by a
     * '0'.
     *
     * @param template the input text containing "^1"-style
     *                 placeholder values.  This object is not modified; a copy is
     *                 returned.
     * @param values   CharSequences substituted into the template.  The
     *                 first is substituted for "^1", the second for "^2", and so on.
     * @return the new CharSequence produced by doing the replacement
     * @throws IllegalArgumentException if the template requests a
     *                                  value that was not provided, or if more than 9 values are
     *                                  provided.
     */
    public static CharSequence expandTemplate(CharSequence template,
                                              CharSequence... values) {
        if (values.length > 9) {
            throw new IllegalArgumentException("max of 9 values are supported");
        }

        StringBuilder ssb = new StringBuilder(template);

        try {
            int i = 0;
            while (i < ssb.length()) {
                if (ssb.charAt(i) == '^') {
                    char next = ssb.charAt(i + 1);
                    if (next == '^') {
                        ssb.delete(i + 1, i + 2);
                        ++i;
                        continue;
                    } else if (Character.isDigit(next)) {
                        int which = Character.getNumericValue(next) - 1;
                        if (which < 0) {
                            throw new IllegalArgumentException(
                                    "template requests value ^" + (which + 1));
                        }
                        if (which >= values.length) {
                            throw new IllegalArgumentException(
                                    "template requests value ^" + (which + 1) +
                                            "; only " + values.length + " provided");
                        }
                        ssb.replace(i, i + 2, values[which].toString());
                        i += values[which].length();
                        continue;
                    }
                }
                ++i;
            }
        } catch (IndexOutOfBoundsException ignore) {
            // happens when ^ is the last character in the string.
        }
        return ssb;
    }

    public static int getOffsetBefore(CharSequence text, int offset) {
        if (offset == 0)
            return 0;
        if (offset == 1)
            return 0;

        char c = text.charAt(offset - 1);

        if (c >= '\uDC00' && c <= '\uDFFF') {
            char c1 = text.charAt(offset - 2);

            if (c1 >= '\uD800' && c1 <= '\uDBFF')
                offset -= 2;
            else
                offset -= 1;
        } else {
            offset -= 1;
        }


        return offset;
    }

    public static int getOffsetAfter(CharSequence text, int offset) {
        int len = text.length();

        if (offset == len)
            return len;
        if (offset == len - 1)
            return len;

        char c = text.charAt(offset);

        if (c >= '\uD800' && c <= '\uDBFF') {
            char c1 = text.charAt(offset + 1);

            if (c1 >= '\uDC00' && c1 <= '\uDFFF')
                offset += 2;
            else
                offset += 1;
        } else {
            offset += 1;
        }

        return offset;
    }


    // helper method for toUpperCase()
    private static int toUpperMapToDest(Edits.Iterator iterator, int sourceIndex) {
        // Guaranteed to succeed if sourceIndex < source.length().
        iterator.findSourceIndex(sourceIndex);
        if (sourceIndex == iterator.sourceIndex()) {
            return iterator.destinationIndex();
        }
        // We handle the situation differently depending on if we are in the changed slice or an
        // unchanged one: In an unchanged slice, we can find the exact location the span
        // boundary was before and map there.
        //
        // But in a changed slice, we need to treat the whole destination slice as an atomic unit.
        // We adjust the span boundary to the end of that slice to reduce of the chance of adjacent
        // spans in the source overlapping in the result. (The choice for the end vs the beginning
        // is somewhat arbitrary, but was taken because we except to see slightly more spans only
        // affecting a base character compared to spans only affecting a combining character.)
        if (iterator.hasChange()) {
            return iterator.destinationIndex() + iterator.newLength();
        } else {
            // Move the index 1:1 along with this unchanged piece of text.
            return iterator.destinationIndex() + (sourceIndex - iterator.sourceIndex());
        }
    }

    public enum TruncateAt {
        START,
        MIDDLE,
        END,
        MARQUEE,
        END_SMALL
    }

    public interface EllipsizeCallback {
        /**
         * This method is called to report that the specified region of
         * text was ellipsized away by a call to {link #ellipsize}.
         */
        public void ellipsized(int start, int end);
    }


    // Returns true if the character's presence could affect RTL layout.
    //
    // In order to be fast, the code is intentionally rough and quite conservative in its
    // considering inclusion of any non-BMP or surrogate characters or anything in the bidi
    // blocks or any bidi formatting characters with a potential to affect RTL layout.
    /* package */
    static boolean couldAffectRtl(char c) {
        return (0x0590 <= c && c <= 0x08FF) ||  // RTL scripts
                c == 0x200E ||  // Bidi format character
                c == 0x200F ||  // Bidi format character
                (0x202A <= c && c <= 0x202E) ||  // Bidi format characters
                (0x2066 <= c && c <= 0x2069) ||  // Bidi format characters
                (0xD800 <= c && c <= 0xDFFF) ||  // Surrogate pairs
                (0xFB1D <= c && c <= 0xFDFF) ||  // Hebrew and Arabic presentation forms
                (0xFE70 <= c && c <= 0xFEFE);  // Arabic presentation forms
    }

    // Returns true if there is no character present that may potentially affect RTL layout.
    // Since this calls couldAffectRtl() above, it's also quite conservative, in the way that
    // it may return 'false' (needs bidi) although careful consideration may tell us it should
    // return 'true' (does not need bidi).
    /* package */
    static boolean doesNotNeedBidi(char[] text, int start, int len) {
        final int end = start + len;
        for (int i = start; i < end; i++) {
            if (couldAffectRtl(text[i])) {
                return false;
            }
        }
        return true;
    }


    /* package */
    static void recycle(char[] temp) {
        if (temp.length > 1000)
            return;

        synchronized (sLock) {
            sTemp = temp;
        }
    }

    /**
     * Html-encode the string.
     *
     * @param s the string to be encoded
     * @return the encoded string
     */
    public static String htmlEncode(String s) {
        StringBuilder sb = new StringBuilder();
        char c;
        for (int i = 0; i < s.length(); i++) {
            c = s.charAt(i);
            switch (c) {
                case '<':
                    sb.append("&lt;"); //$NON-NLS-1$
                    break;
                case '>':
                    sb.append("&gt;"); //$NON-NLS-1$
                    break;
                case '&':
                    sb.append("&amp;"); //$NON-NLS-1$
                    break;
                case '\'':
                    //http://www.w3.org/TR/xhtml1
                    // The named character reference &apos; (the apostrophe, U+0027) was introduced in
                    // XML 1.0 but does not appear in HTML. Authors should therefore use &#39; instead
                    // of &apos; to work as expected in HTML 4 user agents.
                    sb.append("&#39;"); //$NON-NLS-1$
                    break;
                case '"':
                    sb.append("&quot;"); //$NON-NLS-1$
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Returns a CharSequence concatenating the specified CharSequences,
     * retaining their spans if any.
     * <p>
     * If there are no parameters, an empty string will be returned.
     * <p>
     * If the number of parameters is exactly one, that parameter is returned as output, even if it
     * is null.
     * <p>
     * If the number of parameters is at least two, any null CharSequence among the parameters is
     * treated as if it was the string <code>"null"</code>.
     * <p>
     * If there are paragraph spans in the source CharSequences that satisfy paragraph boundary
     * requirements in the sources but would no longer satisfy them in the concatenated
     * CharSequence, they may get extended in the resulting CharSequence or not retained.
     */
    public static CharSequence concat(CharSequence... text) {
        if (text.length == 0) {
            return "";
        }

        if (text.length == 1) {
            return text[0];
        }

        boolean spanned = false;
        for (CharSequence piece : text) {
            if (piece instanceof Spanned) {
                spanned = true;
                break;
            }
        }

        if (spanned) {
            final StringBuilder ssb = new StringBuilder();
            for (CharSequence piece : text) {
                // If a piece is null, we append the string "null" for compatibility with the
                // behavior of StringBuilder and the behavior of the concat() method in earlier
                // versions of Android.
                ssb.append(piece == null ? "null" : piece);
            }
            return ssb.toString();
        } else {
            final StringBuilder sb = new StringBuilder();
            for (CharSequence piece : text) {
                sb.append(piece);
            }
            return sb.toString();
        }
    }

    /**
     * Returns whether the given CharSequence contains any printable characters.
     */
    public static boolean isGraphic(CharSequence str) {
        final int len = str.length();
        for (int cp, i = 0; i < len; i += Character.charCount(cp)) {
            cp = Character.codePointAt(str, i);
            int gc = Character.getType(cp);
            if (gc != Character.CONTROL
                    && gc != Character.FORMAT
                    && gc != Character.SURROGATE
                    && gc != Character.UNASSIGNED
                    && gc != Character.LINE_SEPARATOR
                    && gc != Character.PARAGRAPH_SEPARATOR
                    && gc != Character.SPACE_SEPARATOR) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether this character is a printable character.
     * <p>
     * This does not support non-BMP characters and should not be used.
     *
     * @deprecated Use {link #isGraphic(CharSequence)} instead.
     */
    @Deprecated
    public static boolean isGraphic(char c) {
        int gc = Character.getType(c);
        return gc != Character.CONTROL
                && gc != Character.FORMAT
                && gc != Character.SURROGATE
                && gc != Character.UNASSIGNED
                && gc != Character.LINE_SEPARATOR
                && gc != Character.PARAGRAPH_SEPARATOR
                && gc != Character.SPACE_SEPARATOR;
    }

    /**
     * Returns whether the given CharSequence contains only digits.
     */
    public static boolean isDigitsOnly(CharSequence str) {
        final int len = str.length();
        for (int cp, i = 0; i < len; i += Character.charCount(cp)) {
            cp = Character.codePointAt(str, i);
            if (!Character.isDigit(cp)) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     */
    public static boolean isPrintableAscii(final char c) {
        final int asciiFirst = 0x20;
        final int asciiLast = 0x7E;  // included
        return (asciiFirst <= c && c <= asciiLast) || c == '\r' || c == '\n';
    }

    public static boolean isPrintableAsciiOnly(final CharSequence str) {
        final int len = str.length();
        for (int i = 0; i < len; i++) {
            if (!isPrintableAscii(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Capitalization mode for {link #getCapsMode}: capitalize all
     * characters.  This value is explicitly defined to be the same as
     * {link InputType#TYPE_TEXT_FLAG_CAP_CHARACTERS}.
     */
    public static final int CAP_MODE_CHARACTERS
            = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;

    /**
     * Capitalization mode for {link #getCapsMode}: capitalize the first
     * character of all words.  This value is explicitly defined to be the same as
     * {link InputType#TYPE_TEXT_FLAG_CAP_WORDS}.
     */
    public static final int CAP_MODE_WORDS
            = InputType.TYPE_TEXT_FLAG_CAP_WORDS;

    /**
     * Capitalization mode for {link #getCapsMode}: capitalize the first
     * character of each sentence.  This value is explicitly defined to be the same as
     * {link InputType#TYPE_TEXT_FLAG_CAP_SENTENCES}.
     */
    public static final int CAP_MODE_SENTENCES
            = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;

    /**
     * Determine what caps mode should be in effect at the current offset in
     * the text.  Only the mode bits set in <var>reqModes</var> will be
     * checked.  Note that the caps mode flags here are explicitly defined
     * to match those in {link InputType}.
     *
     * @param cs       The text that should be checked for caps modes.
     * @param off      Location in the text at which to check.
     * @param reqModes The modes to be checked: may be any combination of
     *                 {link #CAP_MODE_CHARACTERS}, {link #CAP_MODE_WORDS}, and
     *                 {link #CAP_MODE_SENTENCES}.
     * @return Returns the actual capitalization modes that can be in effect
     * at the current position, which is any combination of
     * {link #CAP_MODE_CHARACTERS}, {link #CAP_MODE_WORDS}, and
     * {link #CAP_MODE_SENTENCES}.
     */
    public static int getCapsMode(CharSequence cs, int off, int reqModes) {
        if (off < 0) {
            return 0;
        }

        int i;
        char c;
        int mode = 0;

        if ((reqModes & CAP_MODE_CHARACTERS) != 0) {
            mode |= CAP_MODE_CHARACTERS;
        }
        if ((reqModes & (CAP_MODE_WORDS | CAP_MODE_SENTENCES)) == 0) {
            return mode;
        }

        // Back over allowed opening punctuation.

        for (i = off; i > 0; i--) {
            c = cs.charAt(i - 1);

            if (c != '"' && c != '\'' &&
                    Character.getType(c) != Character.START_PUNCTUATION) {
                break;
            }
        }

        // Start of paragraph, with optional whitespace.

        int j = i;
        while (j > 0 && ((c = cs.charAt(j - 1)) == ' ' || c == '\t')) {
            j--;
        }
        if (j == 0 || cs.charAt(j - 1) == '\n') {
            return mode | CAP_MODE_WORDS;
        }

        // Or start of word if we are that style.

        if ((reqModes & CAP_MODE_SENTENCES) == 0) {
            if (i != j) mode |= CAP_MODE_WORDS;
            return mode;
        }

        // There must be a space if not the start of paragraph.

        if (i == j) {
            return mode;
        }

        // Back over allowed closing punctuation.

        for (; j > 0; j--) {
            c = cs.charAt(j - 1);

            if (c != '"' && c != '\'' &&
                    Character.getType(c) != Character.END_PUNCTUATION) {
                break;
            }
        }

        if (j > 0) {
            c = cs.charAt(j - 1);

            if (c == '.' || c == '?' || c == '!') {
                // Do not capitalize if the word ends with a period but
                // also contains a period, in which case it is an abbreviation.

                if (c == '.') {
                    for (int k = j - 2; k >= 0; k--) {
                        c = cs.charAt(k);

                        if (c == '.') {
                            return mode;
                        }

                        if (!Character.isLetter(c)) {
                            break;
                        }
                    }
                }

                return mode | CAP_MODE_SENTENCES;
            }
        }

        return mode;
    }

    /**
     * Does a comma-delimited list 'delimitedString' contain a certain item?
     * (without allocating memory)
     */
    public static boolean delimitedStringContains(
            String delimitedString, char delimiter, String item) {
        if (isEmpty(delimitedString) || isEmpty(item)) {
            return false;
        }
        int pos = -1;
        int length = delimitedString.length();
        while ((pos = delimitedString.indexOf(item, pos + 1)) != -1) {
            if (pos > 0 && delimitedString.charAt(pos - 1) != delimiter) {
                continue;
            }
            int expectedDelimiterPos = pos + item.length();
            if (expectedDelimiterPos == length) {
                // Match at end of string.
                return true;
            }
            if (delimitedString.charAt(expectedDelimiterPos) == delimiter) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes empty spans from the <code>spans</code> array.
     * <p>
     * When parsing a Spanned using {link Spanned#nextSpanTransition(int, int, Class)}, empty spans
     * will (correctly) create span transitions, and calling getSpans on a slice of text bounded by
     * one of these transitions will (correctly) include the empty overlapping span.
     * <p>
     * However, these empty spans should not be taken into account when layouting or rendering the
     * string and this method provides a way to filter getSpans' results accordingly.
     *
     * @param spans   A list of spans retrieved using {link Spanned#getSpans(int, int, Class)} from
     *                the <code>spanned</code>
     * @param spanned The Spanned from which spans were extracted
     * @return A subset of spans where empty spans ({link Spanned#getSpanStart(Object)}  ==
     * {link Spanned#getSpanEnd(Object)} have been removed. The initial order is preserved
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] removeEmptySpans(T[] spans, Spanned spanned, Class<T> klass) {
        T[] copy = null;
        int count = 0;

        for (int i = 0; i < spans.length; i++) {
            final T span = spans[i];
            final int start = spanned.getSpanStart(span);
            final int end = spanned.getSpanEnd(span);

            if (start == end) {
                if (copy == null) {
                    copy = (T[]) Array.newInstance(klass, spans.length - 1);
                    System.arraycopy(spans, 0, copy, 0, i);
                    count = i;
                }
            } else {
                if (copy != null) {
                    copy[count] = span;
                    count++;
                }
            }
        }

        if (copy != null) {
            T[] result = (T[]) Array.newInstance(klass, count);
            System.arraycopy(copy, 0, result, 0, count);
            return result;
        } else {
            return spans;
        }
    }

    /**
     * Pack 2 int values into a long, useful as a return value for a range
     *
     * @see #unpackRangeStartFromLong(long)
     * @see #unpackRangeEndFromLong(long)
     */

    public static long packRangeInLong(int start, int end) {
        return (((long) start) << 32) | end;
    }

    /**
     * Get the start value from a range packed in a long by {link #packRangeInLong(int, int)}
     *
     * @see #unpackRangeEndFromLong(long)
     * @see #packRangeInLong(int, int)
     */

    public static int unpackRangeStartFromLong(long range) {
        return (int) (range >>> 32);
    }

    /**
     * Get the end value from a range packed in a long by {link #packRangeInLong(int, int)}
     *
     * @see #unpackRangeStartFromLong(long)
     * @see #packRangeInLong(int, int)
     */

    public static int unpackRangeEndFromLong(long range) {
        return (int) (range & 0x00000000FFFFFFFFL);
    }


    /**
     * If the {@code charSequence} is instance of {link Spanned}, creates a new copy and
     * {link NoCopySpan}'s are removed from the copy. Otherwise the given {@code charSequence} is
     * returned as it is.
     */

    public static CharSequence trimNoCopySpans(CharSequence charSequence) {
        if (charSequence != null && charSequence instanceof Spanned) {
            // StringBuilder copy constructor trims NoCopySpans.
            return new StringBuilder(charSequence);
        }
        return charSequence;
    }

    /**
     * Prepends {@code start} and appends {@code end} to a given {link StringBuilder}
     */
    public static void wrap(StringBuilder builder, String start, String end) {
        builder.insert(0, start);
        builder.append(end);
    }

    /**
     * Intent size limitations prevent sending over a megabyte of data. Limit
     * text length to 100K characters - 200KB.
     */
    private static final int PARCEL_SAFE_TEXT_LENGTH = 100000;

    /**
     * Trims the text to {link #PARCEL_SAFE_TEXT_LENGTH} length. Returns the string as it is if
     * the length() is smaller than {link #PARCEL_SAFE_TEXT_LENGTH}. Used for text that is parceled
     * into a {link Parcelable}.
     */

    public static <T extends CharSequence> T trimToParcelableSize(T text) {
        return trimToSize(text, PARCEL_SAFE_TEXT_LENGTH);
    }

    /**
     * Trims the text to {@code size} length. Returns the string as it is if the length() is
     * smaller than {@code size}. If chars at {@code size-1} and {@code size} is a surrogate
     * pair, returns a CharSequence of length {@code size-1}.
     *
     * @param size length of the result, should be greater than 0
     */

    public static <T extends CharSequence> T trimToSize(T text,
                                                        int size) {
        Preconditions.checkArgument(size > 0);
        if (TextUtils.isEmpty(text) || text.length() <= size) return text;
        if (Character.isHighSurrogate(text.charAt(size - 1))
                && Character.isLowSurrogate(text.charAt(size))) {
            size = size - 1;
        }
        return (T) text.subSequence(0, size);
    }

    /**
     * Trims the {@code text} to the first {@code size} characters and adds an ellipsis if the
     * resulting string is shorter than the input. This will result in an output string which is
     * longer than {@code size} for most inputs.
     *
     * @param size length of the result, should be greater than 0
     */

    public static <T extends CharSequence> T trimToLengthWithEllipsis(T text,
                                                                      int size) {
        T trimmed = trimToSize(text, size);
        if (trimmed.length() < text.length()) {
            trimmed = (T) (trimmed.toString() + "...");
        }
        return trimmed;
    }

    private static boolean isNewline(int codePoint) {
        int type = Character.getType(codePoint);
        return type == Character.PARAGRAPH_SEPARATOR || type == Character.LINE_SEPARATOR
                || codePoint == LINE_FEED_CODE_POINT;
    }

    private static boolean isWhiteSpace(int codePoint) {
        return Character.isWhitespace(codePoint) || codePoint == NBSP_CODE_POINT;
    }

    /**
     *
     */

    public static String withoutPrefix(String prefix, String str) {
        if (prefix == null || str == null) return str;
        return str.startsWith(prefix) ? str.substring(prefix.length()) : str;
    }


    private static Object sLock = new Object();

    private static char[] sTemp = null;

    private static String[] EMPTY_STRING_ARRAY = new String[]{};
}

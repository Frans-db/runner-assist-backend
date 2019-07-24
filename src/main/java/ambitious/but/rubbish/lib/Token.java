package ambitious.but.rubbish.lib;

import java.security.SecureRandom;

public class Token {
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = UPPER.toLowerCase();
    private static final String NUMS = "0123456789";
    private static final String ALPHANUM = UPPER + LOWER + NUMS;
    private SecureRandom rand = new SecureRandom();
    private char[] symbols;
    private char[] buffer;

    /**
     * Constructor for custom character pool.
     *
     * @param length Desired Length of Token
     * @param symbols Character Pool from which Token is Generated
     */
    public Token(int length, String symbols) {
        if (length < 1 || symbols.length() < 2) throw new IllegalArgumentException();
        this.symbols = symbols.toCharArray();
        this.buffer = new char[length];
    }

    /**
     * Constructor for alphanumeric character pool.
     *
     * @param length Desired Length of Token
     */
    public Token(int length) {
        this(length, ALPHANUM);
    }

    /**
     * Generates next token by concatenating random elements from character pool to a byte buffer then building a string with it.
     *
     * @return Randomly Generated Token
     */
    public String nextToken() {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = symbols[rand.nextInt(symbols.length)];
        }
        return new String(buffer);
    }
}
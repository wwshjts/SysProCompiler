package org.sypro.spc.lexer;

public enum GeneralCategory {
    // L - letters
    Lu, Lt, Ll, Lm, Lo,

    // Numbers
    Nl, Nd,

    // Punctuation
    Pc, Po, Pd, Pe, Ps, Pf, Pi,
    // Other stuff
    Mn, Mc, Cf;

    public static GeneralCategory getCategory(int codePoint) {
        return switch (Character.getType(codePoint)) {
            // Letters
            case Character.UPPERCASE_LETTER -> Lu;
            case Character.LOWERCASE_LETTER -> Lt;
            case Character.TITLECASE_LETTER -> Ll;
            case Character.MODIFIER_LETTER -> Lm;
            case Character.OTHER_LETTER -> Lo;

            // Numbers
            case Character.LETTER_NUMBER -> Nl;
            case Character.DECIMAL_DIGIT_NUMBER -> Nd;

            // Punctuation
            case Character.CONNECTOR_PUNCTUATION -> Pc;
            case Character.OTHER_PUNCTUATION -> Po;
            case Character.DASH_PUNCTUATION -> Pd;
            case Character.END_PUNCTUATION -> Pe;
            case Character.START_PUNCTUATION -> Ps;
            case Character.FINAL_QUOTE_PUNCTUATION -> Pf;
            case Character.INITIAL_QUOTE_PUNCTUATION -> Pi;

            // Other
            case Character.NON_SPACING_MARK -> Mn;
            case Character.COMBINING_SPACING_MARK -> Mc;
            case Character.FORMAT -> Cf;

            // TODO
            default -> throw new IllegalArgumentException("From: Lexer.\n Unexpected general category on codepoint" + codePoint);
        };
    }
}

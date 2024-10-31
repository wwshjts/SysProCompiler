package org.sypro.spc.lexer;

public enum GeneralCategory {
    // L - letters
    Lu, Lt, Ll, Lm, Lo,

    // Numbers
    Nl, Nd,

    // Punctuation
    Pc,

    // Other stuff
    Mn, Mc, Cf,

    // Error category
    BAD;

    public boolean isLetter() {
        return equals(Lu) || equals(Lt) || equals(Ll) || equals(Lm) || equals(Lo);
    }

    public static GeneralCategory of(int codePoint) {
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

            // Other
            case Character.NON_SPACING_MARK -> Mn;
            case Character.COMBINING_SPACING_MARK -> Mc;
            case Character.FORMAT -> Cf;

            default -> BAD;
        };
    }
}

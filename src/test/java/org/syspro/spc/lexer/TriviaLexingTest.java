package org.syspro.spc.lexer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sypro.spc.lexer.SpcLexer;

public class TriviaLexingTest {

    @Test
    @DisplayName("Indentation and commentaries")
    public void indentationAndCommentaries() {
        String input = "\n  #*comment*";
        SpcLexer lex = new SpcLexer();
        lex.lex(input);
    }

    @Test
    @DisplayName("Tricky indentation and commentaries")
    public void trickyIndentation() {
        /*
        \n__# *comment*\n___
         */
        String input1 = "Pixies\n  \n   # Where is my mind\n  Song";
        String input2 = "Low\n   \n # Down";
    }
}

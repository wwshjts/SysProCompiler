package org.syspro.spc.lexer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sypro.spc.lexer.SpcLexer;
import syspro.tm.lexer.*;
import utils.Logger;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IdentifierLexingTests {

    @Test
    @DisplayName("Boolean literals")
    public void booleanLiteral() {
        String input = "true false";
        SpcLexer lexer = new SpcLexer();
        SpcLexer.ResultOfLexing resultOfLexing = lexer.spcLex(input);
        List<Logger.Log> result = (resultOfLexing).logger.toList();
        List<Token> tokens = (resultOfLexing).lex_result;

        List<Logger.Log> expected = List.of(
                Logger.lexLogOf(0, 3, 0, 0, "true"),
                Logger.lexLogOf(4, input.length() - 1, 1, 0, "false")
        );

        Assertions.assertEquals(expected, result);

        for (Token tkn : tokens) {
            Assertions.assertInstanceOf(BooleanLiteralToken.class, tkn);
        }
    }

    @Test
    @DisplayName("Contextual keywords, that can be resolved in lexing stage")
    public void contextualKeyWord() {
        String input = "class\nobject\ninterface";
        SpcLexer lexer = new SpcLexer();
        SpcLexer.ResultOfLexing resultOfLexing = lexer.spcLex(input);
        List<Token> tokens = (resultOfLexing).lex_result;

        for (Token keyword : tokens) {
            Assertions.assertInstanceOf(KeywordToken.class, keyword);
        }
    }

    @Test
    @DisplayName("Contextual identifiers")
    public void contextualIdentifier() {
        String input = "\n  class object interface";
        SpcLexer lexer = new SpcLexer();
        SpcLexer.ResultOfLexing resultOfLexing = lexer.spcLex(input);
        List<Token> tokens = (resultOfLexing).lex_result;

        // Remove indentation tokens
        tokens.removeFirst();
        tokens.removeLast();

        for (Token identifier : tokens) {
            Assertions.assertInstanceOf(IdentifierToken.class, identifier);
            Assertions.assertNotNull( ((IdentifierToken) identifier).contextualKeyword);
        }
    }

    @Test
    @DisplayName("Some keywords")
    public void keywords() {
        String input = "this super override";
        SpcLexer lexer = new SpcLexer();
        SpcLexer.ResultOfLexing resultOfLexing = lexer.spcLex(input);
        List<Token> tokens = (resultOfLexing).lex_result;

        for (Token identifier : tokens) {
            Assertions.assertInstanceOf(KeywordToken.class, identifier);
        }
    }

    @Test
    @DisplayName("Non spacing mark")
    public void nonSpacingMark() {
        String input = "йоу";

        SpcLexer lexer = new SpcLexer();
        SpcLexer.ResultOfLexing resultOfLexing = lexer.spcLex(input);
        List<Token> tokens = (resultOfLexing).lex_result;

        Token tkn = tokens.getLast();
        Assertions.assertEquals(input, tkn.toString());
    }

}

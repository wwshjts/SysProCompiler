package org.sypro.spc.lexer;

import syspro.tm.lexer.*;
import utils.Logger;
import utils.UnicodeUtils;

import java.util.*;

/*
    some conventions, that code in SpcLexer uses:
    1) Every method, that begins with "scan" should modify Context in that way:
        Begin state: Context index points to first codePoint in lexeme
        End state: Context index points to the last codePoint in lexeme
 */

public class SpcLexer implements Lexer {

    public ResultOfLexing spcLex(String s) {
        LinkedList<Token> tokens = new LinkedList<>();
        Context ctx = new Context(s);

        while (ctx.has()) {
            tokens.addAll(scanToken(ctx));
            ctx.next();
        }

        List<Logger.Log> logs = ctx.logger.toList();

        assert logs.size() == tokens.size();

        // if there is trailing trivia on the last token
        if (!logs.isEmpty() && (logs.getLast().strRepresentation().equals("<DEDENT>") ||
                (logs.getLast()).end() < ctx.length)) {

            int last_tkn_index = logs.size() - 1;
            while ( last_tkn_index >= 0 && (logs.get(last_tkn_index).strRepresentation().equals("<DEDENT>") ||
                    logs.get(last_tkn_index).strRepresentation().equals("<INDENT>")) ) {
                last_tkn_index--;
            }



            if (last_tkn_index >= 0) {

                Logger.Log  last_log = logs.get(last_tkn_index);
                Token last_tkn = tokens.get(last_tkn_index);

                tokens.set(last_tkn_index, last_tkn.withEnd(ctx.length - 1 ).withTrailingTriviaLength(
                        ctx.length - last_log.end() - 1));

                logs.set(last_tkn_index, last_log.withEnd(ctx.length - 1).withTrailingTriviaLength(
                        ctx.length - last_log.end() - 1));
            }

        }

        if (ctx.getIndentationLevel() > 0) {
            int levels_to_drop = ctx.dropIndent();
            List<Token> drop = new ArrayList<>(levels_to_drop);
            for (int i = 0; i < levels_to_drop; i++) {
                ctx.logger.logToken(ctx.index, ctx.index, 0, 0, "<DEDENT>");
                drop.add(new IndentationToken(ctx.index, ctx.index, 0, 0, -1));
            }

            tokens.addAll(drop);
        }


        return new ResultOfLexing(tokens, ctx.logger, ctx.input);
    }

    @Override
    public List<Token> lex(String s) {
        return spcLex(s).lex_result;
    }


    // return's list because there is inputs, that not produce tokens, or produce two tokens at once
    private static List<Token> scanToken(Context ctx) {
        String ch = ctx.get();

        int codePoint = ctx.getCodePoint();
        //GeneralCategory category = GeneralCategory.getCategory(codePoint);

        List<Token> res = new LinkedList<>();

        int start_pos = ctx.getIndex();
        int end_of_trivia_pos = ctx.getIndex();

        if (UnicodeUtils.isSpace(ch) || UnicodeUtils.isNewLine(ch) || ch.equals("#")) {
            res.addAll(scanTrivia(ctx));

            if (ctx.hasNext()) {
                ctx.next();
                ch = ctx.get();
            } else {
                return res;
            }

        }

       /*
        while (UnicodeUtils.isSpace(ch)) {
            if (!ctx.hasNext()) {
                return List.of();
            }
            ctx.next();
            ch = ctx.get();
        }

        if (ch.equals("#")) {
            scanComment(ctx);
            if (!ctx.hasNext()) {
                return List.of();
            }
            ctx.next();
            ch = ctx.get();
        }


        // Scanning trivia produces IndentToken or don't produce any token
        if (UnicodeUtils.isNewLine(ch)) {
            res.addAll(scanIndent(ctx));
            Optional<String> next = ctx.seek();

            if(next.isEmpty()) {
                return res;
            }

            ctx.next();
        }
        */

        end_of_trivia_pos = ctx.getIndex();

        // scan token after leading trivia
        ch = ctx.get();
        int leading_trivia_len = end_of_trivia_pos - start_pos;

        Token tkn = switch (ch) {
            case "." -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.DOT);
            case ":" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.COLON);
            case "," -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.COMMA);
            case "+" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.PLUS);
            case "-" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.MINUS);
            case "*" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.ASTERISK);
            case "/" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.SLASH);
            case "%" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.PERCENT);
            case "!" -> {
                Symbol s = scanNextSymbol(ctx, "=") ? Symbol.EXCLAMATION_EQUALS : Symbol.EXCLAMATION;
                yield new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, s);
            }
            case "~" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.TILDE);
            case "&" -> {
                Symbol s = scanNextSymbol(ctx, "&") ? Symbol.AMPERSAND_AMPERSAND : Symbol.AMPERSAND;
                yield new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, s);
            }
            case "|" -> {
                Symbol s = scanNextSymbol(ctx, "|") ? Symbol.BAR_BAR : Symbol.BAR;
                yield new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, s);
            }
            case "^" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.CARET);
            case "<" -> {
                Symbol s;
                if (scanNextSymbol(ctx, "=")) {
                    s = Symbol.LESS_THAN_EQUALS;
                } else if (scanNextSymbol(ctx, "<")) {
                   s = Symbol.LESS_THAN_LESS_THAN;
                }  else if (scanNextSymbol(ctx, ":")) {
                   s = Symbol.BOUND;
                } else {
                   s = Symbol.LESS_THAN;
                }
                yield new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, s);
            }
            case ">" -> {
                Symbol s;
                if (scanNextSymbol(ctx, "=")) {
                    s = Symbol.GREATER_THAN_EQUALS;
                } else if (scanNextSymbol(ctx, ">")) {
                    s = Symbol.GREATER_THAN_GREATER_THAN;
                } else {
                    s = Symbol.GREATER_THAN;
                }
                yield new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, s);
            }
            case "[" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.OPEN_BRACKET);
            case "]" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.CLOSE_BRACKET);
            case "(" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.OPEN_PAREN);
            case ")" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.CLOSE_PAREN);
            case "=" -> {
                Symbol s = scanNextSymbol(ctx, "=") ? Symbol.EQUALS_EQUALS : Symbol.EQUALS;
                yield new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, s);
            }
            case "?" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.QUESTION);

            case "'" -> scanRune(start_pos, leading_trivia_len, ctx);

            case "\"" -> scanStringLiteral(start_pos, leading_trivia_len, ctx);

            // scan integer
            case "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> scanInteger(start_pos, leading_trivia_len, ctx);

            default -> scanIdentifier(start_pos, leading_trivia_len, ctx);
        };

        res.add(tkn);
        ctx.logger.logToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, tkn.toString());

        return res;
    }

    private static List<Token> scanTrivia(Context ctx) {
        String curr = ctx.get();                    // it is guaranteed that curr exists

        List<Token> res = new LinkedList<>();

        while (UnicodeUtils.isNewLine(curr) || UnicodeUtils.isSpace(curr) || curr.equals("#")) {

            if (curr.equals("#")) {
                scanComment(ctx);
            } else if (UnicodeUtils.isNewLine(curr)) {

                res.addAll(scanIndent(ctx));
            }

            // TODO: ugly
            Optional<String> next = ctx.seek();
            if (next.isPresent() && (UnicodeUtils.isSpace(next.get()) || UnicodeUtils.isNewLine(next.get()) || (next.get()).equals("#"))) {
                ctx.next();
                curr = ctx.get();
            } else {
                break;
            }

        }

        return res;
    }

    private static List<Token> scanIndent(Context ctx) {
        String curr = ctx.get();
        Optional<String> next = ctx.seek();

        assert UnicodeUtils.isNewLine(curr);

        int last_new_line = ctx.index;
        int indentation_length = 0;
        int last_cr_index = 0;

        while (next.isPresent() && (UnicodeUtils.isSpace(next.get()) || (UnicodeUtils.isNewLine(next.get())))) {
            if (ctx.get().equals("\r")) {
                last_cr_index = ctx.getIndex();
            }
            ctx.next();
            if (UnicodeUtils.isNewLine(ctx.get())) {
                last_new_line = ctx.index;
                indentation_length = 0;
                System.out.println(last_cr_index + " " + last_new_line);
            } else {
                indentation_length += UnicodeUtils.getNumberOfSpaces(ctx.get());
            }
            next = ctx.seek();
        }

        // *code*\n\t\t\t\n*code*

        int begin = last_new_line - last_cr_index == 1 ? last_cr_index : last_new_line;
        // situation like that *some_code*\n____
        // or *some_code*\n*code*
        if (next.isEmpty() || indentation_length == 0) {
            // if there was indentation level 0, we shouldn't produce [<DEDENT>]
            if (ctx.getIndentationLevel() == 0) {
               return List.of();
            }

            int levels_to_drop = ctx.dropIndent();
            assert levels_to_drop >= 0;
            List<Token> drop = new ArrayList<>(levels_to_drop);
            for (int i = 0; i < levels_to_drop; i++) {
                ctx.logger.logToken(begin, last_new_line, 0, 0, "<DEDENT>");
                drop.add(new IndentationToken(begin, last_new_line, 0, 0, -1));
            }

            return drop;
        }

        // \n____\n______\n\n\n\n\n___some code
        // in other words there is incorrect level of indentation
        if (indentation_length % 2 != 0) {
            return List.of();
        }

        /* situation like that
        * class Foo:
        * __field
         */
        if (ctx.getIndentationLevel() == 0) {
            ctx.increaseIndentationLevel();
            ctx.setIndentationLength(indentation_length);
            ctx.logger.logToken(begin, last_new_line, 0, 0, "<INDENT>");
            return List.of(new IndentationToken(begin, last_new_line, 0, 0, 1));
        }


        /* incorrect change if indentation
        class Foo:
        ____def foo():
         */


        if ( (indentation_length % ctx.getIndentationLength() != 0)) {
            return List.of();
        }

        int curr_indentation_len = ctx.getIndentationLevel() * ctx.getIndentationLength();


        int required_level = indentation_length / ctx.getIndentationLength();
        List<Token> indent = new ArrayList<>(required_level);

        if ( indentation_length > curr_indentation_len) {

            while (ctx.getIndentationLevel() != required_level) {
                ctx.increaseIndentationLevel();
                ctx.logger.logToken(begin, last_new_line, 0, 0, "<INDENT>");
                indent.add(new IndentationToken(begin, last_new_line, 0, 0, 1));
            }
            return indent;
        } else {
            while (ctx.getIndentationLevel() != required_level) {
                ctx.decreaseIndentationLevel();
                ctx.logger.logToken(begin, last_new_line, 0, 0, "<DEDENT>");
                indent.add(new IndentationToken(begin, last_new_line, 0, 0, -1));
            }
            return indent;
        }
    }




    // function that scan comment doesn't return token. It only modifies context.
    // it stops scanning when meets '\n' or '\r'
    private static void scanComment(Context ctx) {
        Optional<String> ch_opt = ctx.seek();

        // read all symbols in the comment string
        while (ch_opt.isPresent() && (!UnicodeUtils.isNewLine(ch_opt.get()))) {
            ctx.next();
            ch_opt = ctx.seek();
        }
    }

    // TODO: refract this to matchNext
    private static boolean scanNextSymbol(Context ctx, String symbol) {
        Optional<String> next = ctx.seek();
        if (next.isPresent() && next.get().equals(symbol)) {
            ctx.next();
            return true;
        }
        return false;
    }

    // TODO: think about REALLY large integers
    private static Token scanInteger(int start, int leadingTriviaLength, Context ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(ctx.get());
        Optional<String> next = ctx.seek();

        while (next.isPresent() && (UnicodeUtils.isDigit(next.get()))) {
            sb.append(next.get());
            ctx.next();
            next = ctx.seek();
        }

        // scan suffix
        BuiltInType type = BuiltInType.INT64;
        boolean hasSuffix = false;
        if (matchIntegerSuffix(ctx, "i32")) {
            type = BuiltInType.INT32;
            hasSuffix = true;
        } else if (matchIntegerSuffix(ctx, "u32")) {
            type = BuiltInType.UINT32;
            hasSuffix = true;
        } else if (matchIntegerSuffix(ctx, "i64")) {
            type = BuiltInType.INT64;
            hasSuffix = true;
        } else if (matchIntegerSuffix(ctx, "u64")) {
            type= BuiltInType.UINT64;
            hasSuffix =  true;
        }

        return new IntegerLiteralToken(start, ctx.getIndex(), leadingTriviaLength, 0,
                type, hasSuffix, Integer.parseInt(sb.toString()));
    }

    // scan identifier or boolean literal
    private static Token scanIdentifier(int start, int leadingTriviaLength, Context ctx) {
        int cp = ctx.getCodePoint();
        String ch = ctx.get();
        StringBuilder sb = new StringBuilder(); sb.append(ch);
        GeneralCategory generalCategory = GeneralCategory.of(cp);


        if (!generalCategory.isLetter() && !generalCategory.equals(GeneralCategory.Nl) && !ch.equals("_")) {
            return new BadToken(start, ctx.index, leadingTriviaLength, 0);
        }

        Optional<String> next = ctx.seek();
        Optional<Integer> next_cp = ctx.seekCodePoint();

        while (next_cp.isPresent() && (!GeneralCategory.of(next_cp.get()).equals(GeneralCategory.BAD))) {
            ctx.next();
            next_cp = ctx.seekCodePoint();
            sb.append(ctx.get());
        }

        String value = sb.toString();

        return switch (value) {
            case "true" -> new BooleanLiteralToken(start, ctx.getIndex(), leadingTriviaLength, 0, true);
            case "false" -> new BooleanLiteralToken(start, ctx.getIndex(), leadingTriviaLength, 0, false);
            // TODO: merge this???
            case "class" -> {
                Keyword keyword = ctx.getIndentationLevel() == 0 ? Keyword.CLASS : null;
                yield  new IdentifierToken(start, ctx.getIndex(), leadingTriviaLength, 0, value, keyword);
            }
            case "object" -> {
                Keyword keyword = ctx.getIndentationLevel() == 0 ? Keyword.OBJECT : null;
                yield  new IdentifierToken(start, ctx.getIndex(), leadingTriviaLength, 0, value, keyword);
            }
            case "interface" -> {
                Keyword keyword = ctx.getIndentationLevel() == 0 ? Keyword.INTERFACE : null;
                yield  new IdentifierToken(start, ctx.getIndex(), leadingTriviaLength, 0, value, keyword);
            }
            default -> new IdentifierToken(start, ctx.getIndex(), leadingTriviaLength, 0, value, null);
        };
    }

    private static Token scanRune(int start, int leadingTriviaLength, Context ctx) {
        Optional<String> next =  ctx.seek();

        if (next.isEmpty()) {
            return new BadToken(start, ctx.index, leadingTriviaLength, 0);
        }

        int rune_value;
        // scan escape
        if (next.get().equals("\\")) {
            ctx.next();

            // TODO: ugly
            Optional<Integer> value = evalShortEscape(ctx);
            next = ctx.seek();

            if (next.isEmpty() || !next.get().equals("'")) {
                value = Optional.empty();
            }

            if (value.isEmpty()) {
                value = evalUnicodeEscape(ctx);
                if (ctx.hasNext()) {
                    ctx.next();
                }
            }

            if (value.isEmpty()) {
                return new BadToken(start, ctx.index, 0, 0);
            }

            rune_value = value.get();
        } else { // else rune looks like this 'S' where S is some Unicode character
            ctx.next();
            int cp = ctx.getCodePoint();
            Optional<String> end_of_rune = ctx.seek();

            if (end_of_rune.isEmpty() || !end_of_rune.get().equals("'")) {
                return new BadToken(start, ctx.index, leadingTriviaLength, 0);
            }

            ctx.next();
            rune_value = cp;
        }

        return new RuneLiteralToken(start, ctx.getIndex(), leadingTriviaLength, 0, rune_value);
    }

    private static Token scanStringLiteral(int start, int leadingTriviaLength, Context ctx) {
        Optional<String> next = ctx.seek();
        StringBuilder sb = new StringBuilder();

        while (next.isPresent() && !next.get().equals("\"")) {
            ctx.next();
            String curr = ctx.get();
            next = ctx.seek();

            if (ctx.get().equals("\\") && next.isPresent()) {

                Optional<Integer> value = evalShortEscape(ctx);
                if (value.isEmpty()) {
                    value = evalUnicodeEscape(ctx);
                }

                if (value.isPresent()) {
                    sb.append(Character.toString(value.get()));
                    next = ctx.seek();
                } else {
                    return new BadToken(start, ctx.index, leadingTriviaLength, 0);
                }

                if (!ctx.hasNext()) {
                    break;
                }

            } else {
                sb.append(curr);
            }
        }

        if (next.isEmpty() || !next.get().equals("\"")) {
            return new BadToken(start, ctx.index, leadingTriviaLength, 0);
        }
        ctx.next();

        return new StringLiteralToken(start, ctx.index, leadingTriviaLength, 0, sb.toString());
    }

    private static Optional<Integer> evalShortEscape(Context ctx) {
        if (!ctx.has(2)) {
            return Optional.empty();
        }

        // lookup two symbols ahead
        ctx.next();
        String ch = ctx.get();
        ctx.next();
        String end_of_rune = ctx.get();

        int cp = switch (ch) {
            case "0" -> '\0';
            case "a" -> 0x07;
            case "b" -> '\b';
            case "r" -> '\r';
            case "n" -> '\n';
            case "t" -> '\t';
            case "v" ->  0x0b;
            case "'" -> '\'';
            case "\"" -> '\"';
            case "\\" -> '\\';
            default -> -1;
        };

        if (cp < 0) {
            ctx.back(2);
            return Optional.empty();
        }

        return Optional.of(cp);
    }

    private static Optional<Integer> evalUnicodeEscape(Context ctx) {
        int look_ahead = 0;

        if (ctx.has(2)) {
            look_ahead = 2;
        }

        // math two first characters
        ctx.next();
        String u = ctx.get();
        ctx.next();
        String plus = ctx.get();

        if (!u.equals("U") || !plus.equals("+")) {
            ctx.back(2);
            return Optional.empty();
        }


        Optional<String> next = ctx.seek();
        StringBuilder sb = new StringBuilder();

        while (next.isPresent() && UnicodeUtils.isHexDigit(next.get()) && (look_ahead < 7)) {
            ctx.next();
            next = ctx.seek();
            look_ahead++;
            sb.append(ctx.get());
        }

        if (next.isEmpty()) {
            ctx.back(look_ahead);
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.parseInt(sb.toString(), 16));
        } catch (NumberFormatException e) {
            ctx.back(look_ahead);
            return Optional.empty();
        }

    }

    // modify context if suffix is matched, otherwise it doesn't modify context
    // we can't have surrogate pair in suffix, so it is correct to compare just by symbols in suffix
    private static boolean matchIntegerSuffix(Context ctx, String suffix) {
        assert suffix.length() == 3;
        int suffix_length = 3;

        if (!ctx.has(suffix_length)) {
            return false;
        }

        for (int i = 0; i < suffix_length; i++) {
            ctx.next();
            String ch = ctx.get();
            if (!ch.equals(Character.toString(suffix.charAt(i)))) {
                ctx.back(i + 1);
                return false;
            }
        }

        return true;
    }


    /*
     *
     * Lexer should be thread-safe
     * so Context encapsulates state of SpcLexer
     *
     */
    private static class Context {
        // the string of code points of input
        private final String input;

        // number of read codePoints
        private int index = 0;

        // length of input string in code points
        private final int length;
        private final ArrayList<Token> tokens = new ArrayList<>();
        private int line = 1; // TODO: add line counting
        private int indentation_level = 0;
        private int indentation_length = -1;
        public final Logger logger = new Logger();

        Context (String input) {
            this.input = input;
            this.length = input.codePointCount(0, input.length());
        }

        String get() {
            assert has();
            int i = input.offsetByCodePoints(0,  index);
            return Character.toString(input.codePointAt(i));
        }

        int getCodePoint() {
            int i = input.offsetByCodePoints(0,  index);
            return input.codePointAt(i);
        }

        Optional<Integer> seekCodePoint() {
            if (hasNext()) {
                int i = input.offsetByCodePoints(0, index + 1);
                return Optional.of(input.codePointAt(i));
            } else {
                return Optional.empty();
            }
        }

        // modifies scanning position
        void next() {
            index++;
        }

        Optional<String> seek() {
            if (hasNext()) {
                int i = input.offsetByCodePoints(0,  index + 1);
                return Optional.of(Character.toString(input.codePointAt(i)));
            } else {
                return Optional.empty();
            }
        }

        int dropIndent() {
            int res = indentation_level;
            indentation_level = 0;
            indentation_length = 0;
            return res;
        }

        public boolean hasNext() {
            return index + 1 < length;
        }

        public boolean has() {
            return index < length;
        }

        public boolean has(int n) {
           return index + n < length;
        }

        public int getIndentationLevel() {
            return indentation_level;
        }

        public void increaseIndentationLevel() {
            this.indentation_level++;
        }

        public void decreaseIndentationLevel() {
            assert indentation_level > 0;
            this.indentation_level--;
        }

        public int getIndentationLength() {
            return indentation_length;
        }

        public void setIndentationLength(int indentation_length) {
            assert indentation_length >= 0;
            this.indentation_length = indentation_length;
        }

        public int getIndex() {
            return index;
        }

        // very dangerous
        private void back(int steps) {
            assert steps > 0 && index - steps > 0;
            index -= steps;
        }

    }

    public static class ResultOfLexing {
        public List<Token> lex_result;
        public Logger logger;
        String input;

        ResultOfLexing(List<Token> lex_result, Logger logger, String input) {
            this.lex_result = lex_result;
            this.logger = logger;
        }
    }
}

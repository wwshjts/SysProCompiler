package org.sypro.spc;

import org.sypro.spc.lexer.SpcLexer;
import syspro.tm.Tasks;
import syspro.tm.lexer.TestMode;

public class Main {
    public static void main(String[] args) {
        Tasks.Lexer.registerSolution(new SpcLexer());
    }
}
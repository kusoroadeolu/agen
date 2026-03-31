package io.github.kusoroadelu.ven;

import io.github.kusoroadeolu.clique.Clique;
import io.github.kusoroadeolu.clique.spi.AnsiCode;
import io.github.kusoroadeolu.veneer.theme.SyntaxTheme;

class OneDarkTheme implements SyntaxTheme {

    private static final AnsiCode KEYWORD        = Clique.rgb(213, 95, 222);  // #D55FDE
    private static final AnsiCode STRING         = Clique.rgb(137, 202, 120); // #89CA78
    private static final AnsiCode NUMBER_LITERAL = Clique.rgb(209, 154, 102); // #D19A66
    private static final AnsiCode COMMENT        = Clique.rgb(92, 99, 112);   // #5C6370
    private static final AnsiCode ANNOTATION     = Clique.rgb(229, 192, 123); // #E5C07B
    private static final AnsiCode METHOD         = Clique.rgb(97, 175, 239);  // #61AFEF
    private static final AnsiCode TYPES          = Clique.rgb(229, 192, 123); // #E5C07B
    private static final AnsiCode CONSTANTS      = Clique.rgb(239, 89, 111);  // #EF596F
    private static final AnsiCode GUTTER         = Clique.rgb(128, 128, 128); // same as DefaultSyntaxTheme

    @Override public AnsiCode keyword()       { return KEYWORD; }
    @Override public AnsiCode stringLiteral() { return STRING; }
    @Override public AnsiCode numberLiteral() { return NUMBER_LITERAL; }
    @Override public AnsiCode comment()       { return COMMENT; }
    @Override public AnsiCode annotation()    { return ANNOTATION; }
    @Override public AnsiCode method()        { return METHOD; }
    @Override public AnsiCode gutter()        { return GUTTER; }
    @Override public AnsiCode types()         { return TYPES; }
    @Override public AnsiCode constants()     { return CONSTANTS; }
}
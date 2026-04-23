package io.github.kusoroadelu.ven;

import io.github.kusoroadeolu.veneer.JavaSyntaxHighlighter;
import io.github.kusoroadeolu.veneer.PythonSyntaxHighlighter;
import io.github.kusoroadeolu.veneer.theme.SyntaxTheme;
import io.github.kusoroadeolu.veneer.theme.SyntaxThemes;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    void main() throws IOException {
        new JavaSyntaxHighlighter(new OneDarkTheme()).print(Path.of("C:\\Users\\eastw\\Git Projects\\Personal\\agen\\agen-expr\\src\\main\\java\\io\\github\\kusoradeolu\\agen\\expr\\queues\\MPMCQueue.java"));
    }
}

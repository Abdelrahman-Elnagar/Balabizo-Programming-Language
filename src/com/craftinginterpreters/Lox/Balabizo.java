package src.com.craftinginterpreters.Lox;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Balabizo {
  static boolean hadError = false; //to ensure we don’t try to execute code that has a known error
  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      System.exit(64); 
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  //give it a path to a file, it reads the file and executes it.
  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));
    // Indicate an error in the exit code.
    if (hadError) System.exit(65);
  }

  //prompt where you can enter and execute code one line at a time.
  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    for (;;) { 
      System.out.print("> ");
      String line = reader.readLine();
      if (line == null) break;
      run(line);
      hadError = false;
    }
  }

  private static void run(String source) {
    Scanner scanner = new Scanner(source); // Create an instance of Scanner
    List<Token> tokens = scanner.scanTokens(); // Call scanTokens() on the instance
  
    // For now, just print the tokens.
    for (Token token : tokens) {
      System.out.println(token);
    }
  }
  

  //handling errors and send them to the user 
  static void error(int line, String message) {
    report(line, "", message);
    //line to know which line the error is 
  }

  private static void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }
}

/*
overall comments:
1) it’s good engineering practice to separate the code that generates the errors from the code that reports them.
2) we would have an actual abstraction, some kind of “ErrorReporter” interface that gets passed to the scanner and parser 
   so that we can swap out different reporting strategies.
3) Scanner to tokens : scan through the list of characters and group them together into the smallest sequences that still represent something.  
4) return '\0' is for returning null   
5) important principle called maximal munch:
   When two lexical grammar rules can both match a chunk of code that the scanner is looking at, whichever one matches the most characters wins.
6) 
*/
// EBNF Grammar:
// expression    → literal
//               | unary
//               | binary
//               | grouping ;
//
// literal       → NUMBER | STRING | "true" | "false" | "nil" ;
// grouping      → "(" expression ")" ;
// unary         → ( "-" | "!" ) expression ;
// binary        → expression operator expression ;
// operator      → "==" | "!=" | "<" | "<=" | ">" | ">="
//               | "+"  | "-"  | "*" | "/" ;
/*
    define a separate rule for each precedence level.
    Each rule here only matches expressions at its precedence level or higher. like each is accepting the ones after it
    expression     → equality ; // The top expression rule matches any expression at any precedence level. Since equality has the lowest precedence, if we match that, then it covers everything.
    equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    term           → factor ( ( "-" | "+" ) factor )* ;
    factor         → unary ( ( "/" | "*" ) unary )* ;
    unary          → ( "!" | "-" ) unary
                  | primary ;
    primary        → NUMBER | STRING | "true" | "false" | "nil"
                  | "(" expression ")" ; // all the literals and grouping expressions.
 */

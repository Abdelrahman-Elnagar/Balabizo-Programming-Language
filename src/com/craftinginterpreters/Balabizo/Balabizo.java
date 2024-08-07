package src.com.craftinginterpreters.Balabizo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
public class Balabizo {

  public static final Interpreter interpreter = new Interpreter();
  static boolean hadError = false; //to ensure we don’t try to execute code that has a known error
  static boolean hadRuntimeError = false;

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jBalabizo [script]");
      System.exit(64); 
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runFile("C:\\Users\\abdel\\OneDrive\\Documents\\Balabizo\\Balabizo\\src\\text.Balabizo");
      runPrompt();
    }
  }

  //give it a path to a file, it reads the file and executes it.
  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));
    // Indicate an error in the exit code.
    if (hadError) System.exit(65);
    if (hadRuntimeError) System.exit(70);
    else
       System.out.println("Life is Good");
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
    /*for (Token token : tokens) {
      System.out.println(token);
    } */

    //
    Parser parser = new Parser(tokens);
    List<Stmt> statements = parser.parse();

    // Stop if there was a syntax error.
    if (hadError) return;
    Resolver resolver = new Resolver(interpreter);
    resolver.resolve(statements);
    if (hadError) return; // try to remove and have fun 
    //System.out.println(new AstPrinter().print(expression));
    interpreter.interpret(statements);

  }
  

  //handling errors and send them to the user 
  static void error(int line, String message) {
    report(line, "", message);
    //line to know which line the error is 
  }

  private static void report(int line, String where, String message) {
    System.err.println("Balabizo Code" + "[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }
  public static void reporteasily(String message) {
    System.err.println("Balabizo Code" + message);
    hadError = true;
  }
  
    // reports an error at a given token. It shows the token’s location and the token itself. 
  static void error(Token token, String message) { // diffrent parameters for parsing part 
    if (token.type == TokenType.EOF) {
      report(token.line, " at end", message);
    } else {
      report(token.line, " at '" + token.lexeme + "'", message);
    }
  }
  static void runtimeError(RuntimeError error) {
    System.err.println("Balabizo Runtime Code" + error.getMessage() +
        "\n[line " + error.token.line + "]");
    hadRuntimeError = true;
  }

}

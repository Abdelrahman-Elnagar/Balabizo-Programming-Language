package src.com.craftinginterpreters.Lox;

import java.util.List;

public class ParserTest {
  public static void main(String[] args) {
    // Test expression: print "Hello, world!";
    test("print \"Hello, world!\";");

    // Test expression: var breakfast = "bagels";
    test("var breakfast = \"bagels\"; print breakfast;");

    // Test expression: breakfast = "beignets";
    test("breakfast = \"beignets\"; print breakfast;");
  }

  private static void test(String source) {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    Parser parser = new Parser(tokens);
    Expr expression = parser.parse();

    if (Balabizo.hadError) {
      System.out.println("Syntax Error.");
      return;
    }

    System.out.println(new AstPrinter().print(expression));
  }
}

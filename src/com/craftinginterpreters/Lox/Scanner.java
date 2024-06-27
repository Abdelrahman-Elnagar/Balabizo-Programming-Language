package src.com.craftinginterpreters.Lox;

import static src.com.craftinginterpreters.Lox.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map; 

class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  //start field points to the first character in the lexeme being scanned
  //current points at the character currently being considered. 
  //The line field tracks what source line current is on so we can produce tokens that know their location.
  private int start = 0;
  private int current = 0;
  private int line = 1;

  private static final Map<String, TokenType> keywords;
  static {
    keywords = new HashMap<>();
    keywords.put("and",    AND);
    keywords.put("class",  CLASS);
    keywords.put("else",   ELSE);
    keywords.put("false",  FALSE);
    keywords.put("for",    FOR);
    keywords.put("fun",    FUN);
    keywords.put("if",     IF);
    keywords.put("nil",    NIL);
    keywords.put("or",     OR);
    keywords.put("print",  PRINT);
    keywords.put("return", RETURN);
    keywords.put("super",  SUPER);
    keywords.put("this",   THIS);
    keywords.put("true",   TRUE);
    keywords.put("var",    VAR);
    keywords.put("while",  WHILE);
  }

  Scanner(String source) { //tom use to scann the file
    this.source = source;
  }

  //if we’ve consumed all the characters. to be able to end the Scanner
  private boolean isAtEnd() {
    return current >= source.length();
  }

  List<Token> scanTokens() { //the list for the scanned tolens
    while (!isAtEnd()) {
      // We are at the beginning of the next lexeme.
      start = current;
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  // real heart of the scanner. where we advance on the source code and switch over the source inputs to get the tokens
  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(': addToken(LEFT_PAREN); break;
      case ')': addToken(RIGHT_PAREN); break;
      case '{': addToken(LEFT_BRACE); break;
      case '}': addToken(RIGHT_BRACE); break;
      case ',': addToken(COMMA); break;
      case '.': addToken(DOT); break;
      case '-': addToken(MINUS); break;
      case '+': addToken(PLUS); break;
      case ';': addToken(SEMICOLON); break;
      case '*': addToken(STAR); break; 
      case '!': // what if the next is something followed ! , use match() 
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
      case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
      case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
      case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
      case '/':
        if (match('/')) {
          // A comment goes until the end of the line. and we don't call add token to prevent adding it to the parser
          while (peek() != '\n' && !isAtEnd()) advance();
        } else {
          addToken(SLASH);
        }
        break;  
      case ' ':
      case '\r':
      case '\t':
        // Ignore whitespace.
        break;
      case '\n':
        line++;
        break;
      case '"': string(); break;

      default:
        if (isDigit(c)) { // more since that writing one for each number right ?
          number();  
        } else if (isAlpha(c)) {
          identifier();
        } else {
          Balabizo.error(line, "Balabizo character. (Unexpected character)");
        }
        break;
        
        //keep scanning.other errors later,we detect as many as possible in one go. Otherwise, they see one tiny error,fix it,only to have the next error appear.
        // what about excuting it ? HadError take care of this no run before check
    }
  }
  private char advance() { //to the next char
    return source.charAt(current++);
  }

  private void addToken(TokenType type) { 
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) { // put the token in the list we created to use it in the next step
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }

  private boolean match(char expected) { // to composite reading like ! we need to the the next to match them 
    if (isAtEnd()) return false;
    if (source.charAt(current) != expected) return false;

    current++;
    return true;
  }

  private char peek() { // like the match () function but diffrent use
    if (isAtEnd()) return '\0';
    return source.charAt(current);
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  } 

  private void string() {
    while (peek() != '"' && !isAtEnd()) { // as long ther string is not ended keep scanning as atring 
      if (peek() == '\n') line++; //supports multi-line strings : update line when we hit a newline inside a string.
      advance();
    }
    if (isAtEnd()) { //if ended without the string being closed alert the user 
      Balabizo.error(line, "Unterminated string. Balabizo String");
      return;
    }
    // The closing ".
    advance();
    // Trim the surrounding quotes.
    String value = source.substring(start + 1, current - 1);
    addToken(STRING, value);
  }

  private void number() {
    while (isDigit(peek())) advance();

    // Look for a fractional part.
    if (peek() == '.' && isDigit(peekNext())) {
      // Consume the "."
      advance();

      while (isDigit(peek())) advance();
    }

    addToken(NUMBER,
        Double.parseDouble(source.substring(start, current))); // using Java’s own parsing method to convert the lexeme to a real Java double (sorry for that)
  }

  private char peekNext() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  }
  
  private void identifier() {
    while (isAlphaNumeric(peek())) advance();
    String text = source.substring(start, current);
    TokenType type = keywords.get(text);
    
    if (type == null) type = IDENTIFIER;
        addToken(type);
  }

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') ||
           (c >= 'A' && c <= 'Z') ||
            c == '_';
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }


}
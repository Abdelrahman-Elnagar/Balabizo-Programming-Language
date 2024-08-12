# Balabizo Programming Language Documentation

## Introduction

**Balabizo** is a modern programming language have primary OOP features of classes and objects, designed to explore and demonstrate core principles of language development and interpreter design. It features a fully functional interpreter and a clean, expressive syntax, making it an ideal tool for learning and experimenting with programming languages.

**Behavior:** When code is successfully compiled and executed, the interpreter displays the message `"Life is good"`. If there is an error, the message `"You are being called Balabizo"` along with the error specification is shown.

## Features

- **Interpreter Implementation:** Handles syntax parsing, abstract syntax tree (AST) generation, and execution.
- **Expressive Syntax:** Designed to be intuitive and easy to understand.
- **Modular Design:** Codebase is organized into well-defined modules to ensure maintainability and scalability.
- **Version Control:** Managed via GitHub for tracking changes and collaboration.

## Components

### Interpreter

The core of Balabizo is its interpreter, which performs the following tasks:
- **Lexical Analysis:** Tokenizes the input source code.
- **Parsing:** Converts tokens into an abstract syntax tree (AST).
- **Execution:** Evaluates the AST and executes the code.

## Installation

### Prerequisites

- **Compiler:** Ensure you have a JDK installed.

### Steps

1. **Clone the Repository:**
    ```sh
    git clone https://github.com/yourusername/balabizo.git
    ```

2. **Navigate to the Project Directory:**
    ```sh
    cd balabizo
    ```

3. **Build the Project:**
    ```sh
    mkdir build
    cd build
    cmake ..
    make
    ```

4. **Run the Interpreter:**
    ```sh
    ./balabizo <path-to-your-script.bz>
    ```

## Usage

1. **Write a Script:** Create a file with the `.bz` extension. For example, `example.bz`.
2. **Run the Script:**
    ```sh
    ./balabizo example.bz
    ```
3. **Check Output:** The interpreter will output the results of executing your script. Success is indicated by `"Life is good"` and errors by `"You are being called Balabizo"` with error details.

## example program :
 ```sh
fun fibonacci (n) {
  var a = 0;
  var b = 1;
  if (n == 0) return a;
  if (n == 1) return b;
    for ( var i = 2; i <= n; i = i + 1) {
      var temp = a + b;
      a = b ;
      b = temp ;
    }
    return b;
}
for ( var i = 0; i < 10; i = i + 1) {
  print " fibonacci (" + i + ") = " + fibonacci ( i);
}

```

## Choices of Implementation

The design and implementation of the Balabizo programming language incorporate several key choices:

- **Lexical Analysis:** Tokenizes input code into a stream of tokens for easier parsing.
- **Parsing:** Constructs an abstract syntax tree (AST) from tokens, facilitating code analysis and execution.
- **Interpreter Design:** Executes code directly from the AST, focusing on simplicity and performance.
- **Error Handling:** Provides clear error messages and user-friendly feedback for debugging.
- **Modularity:** Codebase is divided into modules for better organization and maintainability.

## Development and Contribution

Contributions to the Balabizo project are welcome. To contribute:

- **Fork the Repository:** Create your own fork and make changes.
- **Submit a Pull Request:** Open a pull request with your proposed changes.
- **Open Issues:** Report bugs or request features by opening an issue.

## Licensing

This project is licensed under the **MIT License**. Please refer to the `LICENSE` file for details.

## Contact

For questions or further information, please reach out to `abdelrahmanelnagar123@gmail.com` or connect with me on **[LinkedIn](https://www.linkedin.com/in/abdelrahman-elnagar/)**.


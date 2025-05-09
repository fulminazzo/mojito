<p align="center">
    <a href="https://app.codacy.com/gh/Fulminazzo/mojito/"><img src="https://fulminazzo.it/badge/code/Fulminazzo/mojito?type=code" alt="Lines of Code" /></a>
    <a href="../../releases/latest"><img src="https://img.shields.io/github/v/release/Fulminazzo/mojito?display_name=tag&color=red" alt="Latest version" /></a>
    <a href="https://app.codacy.com/gh/Fulminazzo/mojito/"><img src="https://fulminazzo.it/badge/code/Fulminazzo/mojito?type=test" alt="Lines of Code" /></a>
</p>
<p align="center">
    <img src="https://fulminazzo.it/badge/coverage/Fulminazzo/mojito/gradle.yml" alt="Tests coverage" />
    <a href="https://app.codacy.com/gh/Fulminazzo/mojito/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade"><img src="https://app.codacy.com/project/badge/Grade/c9b24e43cb7c4658975624cc9862a8d3" alt="Codacy Grade" /></a>
</p>
<p align="center">
    <img src="https://img.shields.io/badge/Yes%2C%20I%20love-writing%20tests-00aa00?style=for-the-badge&labelColor=1FE417" alt="" />
</p>

**My Own Java Is Totally Overtested** (**mojito** for short) is a combination of **parser**, **typechecker** and **executor**
capable of **run** a **simplified version of Java 1.8**.

To verify all the capabilities available, please check the [grammar](#grammar), but in summary:
- **classes are NOT supported**. Only one file at a time might be read, and it will **not require** (nor it should be added)
  a **class declaration**;
- **functions are NOT supported**, however it is still possible to invoke methods and access fields from other classes;
- **imports are NOT supported**, but one could use the canonical declaration to access non-default classes.
  **Non-default classes** are, among the **primitive** and **wrapper** types, all those classes whose **package**
  starts with either `java.lang`, `java.util` or `java.io`. Therefore, it is **not required** to specify the full
  package for classes like [Map](https://docs.oracle.com/javase/8/docs/api/java/util/Map.html),
  [IOException](https://docs.oracle.com/javase/8/docs/api/java/io/IOException.html) or
  [Class](https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html).
  For others, the full **canonical name** will be needed.
- the `this` keyword will have a special meaning: it will refer to the **object currently executing the program**,
  which might be user defined. **WARNING**: when invoking implicit methods or obtaining implicit fields (without declaring
  a variable before them), `this` will be **inferred** automatically.
  This means that, if a script uses, for example, `System.out.println(toString())`, the `toString` method will be
  applied to the executing object;
- constants (`final`) are **not** supported.

## Usage

**Mojito** can be used in two ways:

### Command Line

To utilize the parser from command line, simply navigate to the folder containing the latest `.jar` file.
Then, use the following command:

```
java -jar mojito-LATEST.jar file_to_read.java
```

At the moment, the program does not support any command line argument.

### Import

**Mojito** can be imported using one of the most common **three methods** ([Gradle](https://gradle.org/),
[Maven](https://maven.apache.org/) or **JAR**) using `it.fulminazzo` as **group id** and `mojito` as **artifact**.

The [Fulminazzo repository (https://repo.fulminazzo.it/releases)](https://repo.fulminazzo.it/releases)
is **mandatory** for these dependencies to work.

- **Gradle**:

  ```groovy
  repositories {
      maven { url = 'https://repo.fulminazzo.it/releases' }
  }

  dependencies {
      implementation 'it.fulminazzo:mojito:latest.release'
  }
  ```

- **Maven**:

  ```xml
  <repository>
      <id>fulminazzo</id>
      <url>https://repo.fulminazzo.it/releases</url>
  </repository>
  ```

  ```xml
  <dependency>
      <groupId>it.fulminazzo</groupId>
      <artifact>mojito</artifact>
      <version>LATEST</version>
  </dependency>
  ```

Then, it is possible to start reading and executing code thanks to the 
[Mojito](./src/main/java/it/fulminazzo/mojito/Mojito.java) class, which provides two important functions:

- `newRunner()`: allows creating a new runner with an empty object;
- `newRunner(Object)`: allows creating a new runner and setting the given object as the result of the `this` keyword.

A [Runner](./src/main/java/it/fulminazzo/mojito/Runner.java) is a special entity capable of handling Java code
from **files**, **strings** or **input streams** thanks to the many versions of the `run` function.

It is also possible to pass default variables which will be initialized before executing the main program.

As an example, the code:

```java
String code = "return name;";
Map<String, Object> variables = new HashMap<>();
variables.put("name", "fulminazzo");

Runner runner = Mojito.newRunner();
runner.run(code, variables);

Optional<?> returnedValue = runner.latestResult();
```

will return the parsed variable `name` (`fulminazzo`) in the _returnedValue_ 
[Optional](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html).

## Roadmap

- [x] ternary operator (`a ? b : c`);
- [ ] diamond operator (generics);
- [ ] advanced wildcard handling (`super` and `extends`);
- [x] `final` keyword;
- [x] `instanceof` keyword;
- [ ] lambda expressions;
- [ ] `.class` for classes;
- [ ] Java 17 features (section to be expanded).

## Grammar

The following is the grammar respected by the parser:

```
JAVA_PROGRAM := SINGLE_STMT*

BLOCK := CODE_BLOCK | SINGLE_STMT
CODE_BLOCK := \{ SINGLE_STMT* \}
SINGLE_STMT := STMT | ;

STMT := return EXPR; | throw EXPR; break; | continue; | 
        TRY_STMT | SWITCH_STMT | FOR_STMT | DO_STMT | WHILE_STMT | IF_STMT
        ASSIGNMENT;

TRY_STMT := try ( \( ASSIGNMENT_BLOCK \) )? CODE_BLOCK CATCH+ ( finally CODE_BLOCK )?
ASSIGNMENT_BLOCK := (ARRAY_LITERAL LITERAL ( = EXPR? ); )+
CATCH := catch \( ( LITERAL \| )* LITERAL LITERAL \) CODE_BLOCK

SWITCH_STMT := switch \( EXPR \) \{ (CASE_BLOCK)* (DEFAULT_BLOCK)? \}
CASE_BLOCK := case EXPR: ( CODE_BLOCK | SINGLE_STMT* )
DEFAULT_BLOCK := default: ( CODE_BLOCK | SINGLE_STMT* )

FOR_STMT := for \( ASSIGNMENT?; EXPR?; EXPR? \) BLOCK | ENHANCED_FOR_STMT
ENHANCED_FOR_STMT := for \( ARRAY_LITERAL LITERAL : EXPR \) BLOCK

DO_STMT := do BLOCK while PAR_EXPR
WHILE_STMT := while PAR_EXPR BLOCK
IF_STMT := if PAR_EXPR BLOCK ( else IF_STMT )* ( else BLOCK )?

ASSIGNMENT := ( final )? ARRAY_LITERAL LITERAL ( = EXPR? ) | LITERAL = EXPR | EXPR

EXPR := NEW_OBJECT | INCREMENT | DECREMENT | TERNARY_OP
NEW_OBJECT := new LITERAL METHOD_INVOCATION |
              new ARRAY_LITERAL{ ( EXPR )? ( , EXPR )* \} |
              new LITERAL ( \[ NUMBER_VALUE \] )+
ARRAY_LITERAL := LITERAL ( \[\] )* | LITERAL ( \[ [0-9]+ \] )+

INCREMENT := ++ATOM
DECREMENT := --ATOM | MINUS

TERNARY_OP := INSTANCE_OF ( \? EXPR : EXPR )?

INSTANCE_OF := AND ( instanceof LITERAL )?

AND := OR (&& OR)*
OR := EQUAL (|| EQUAL)*
EQUAL := NOT_EQUAL (== NOT_EQUAL)* 
NOT_EQUAL := LESS_THAN (!= LESS_THAN)* 
LESS_THAN := LESS_THAN_EQUAL (< LESS_THAN_EQUAL)* 
LESS_THAN_EQUAL := GREATER_THAN (<= GREATER_THAN)* 
GREATER_THAN := GREATER_THAN_EQUAL (> GREATER_THAN_EQUAL)* 
GREATER_THAN_EQUAL := BIT_AND (>= BIT_AND)*

BIT_AND := BIT_OR ( (& BIT_OR)* | (&= BIT_OR) )
BIT_OR := BIT_XOR ( (| BIT_XOR)* | (|= BIT_XOR) )
BIT_XOR := LSHIFT ( (^ LSHIFT)* | (^= LSHIFT) )

LSHIFT := RSHIFT ( (<< RSHIFT)* | (<<= RSHIFT) )
RSHIFT := URSHIFT ( (>> URSHIFT)* | (>>= URSHIFT) )
URSHIFT := ADD ( (>>> ADD)* | (>>>= ADD) )

ADD := SUB ( (+ SUB)* | (+= SUB) | ++ )
SUB := MUL ( (- MUL)* | (-= MUL) | -- )
MUL := DIV ( (* DIV)* | (*= DIV) )
DIV := MOD ( (/ MOD)* | (/= MOD) )
MOD := UNARY_OPERATION ( (% UNARY_OPERATION)* | (%= UNARY_OPERATION) )

UNARY_OPERATION := CAST | NOT | METHOD_CALL

CAST := (PAR_EXPR)* ( EXPR | PAR_EXPR )
PAR_EXPR := \( EXPR \)

MINUS := - EXPR
NOT := ! EXPR

METHOD_CALL := ATOM ( .LITERAL METHOD_INVOCATION? )*
METHOD_INVOCATION := \( (EXPR)? (, EXPR)* \)

ATOM := NULL | THIS | ARRAY_LITERAL | TYPE_VALUE
NULL := null
THIS := this
LITERAL := [a-zA-Z_](?:[a-zA-Z0-9._]*[a-zA-Z0-9_])*

TYPE_VALUE := NUMBER_VALUE | LONG_VALUE | DOUBLE_VALUE | FLOAT_VALUE |
              BOOLEAN_VALUE | CHAR_VALUE | STRING_VALUE

NUMBER_VALUE := [0-9]+
LONG_VALUE := ([0-9]+)[Ll]?
DOUBLE_VALUE := [0-9]+(?:.[0-9]+)?(?:E[-0-9]+)?[Dd]?
FLOAT_VALUE := [0-9]+(?:.[0-9]+)?(?:E[-0-9]+)?[Ff]?
BOOLEAN_VALUE := true|false
STRING_VALUE := \"((?:[^\"]|\\\")*)\
CHAR_VALUE := '([^\r\n\t \]|\\[rbnft\\\"\'])'
```

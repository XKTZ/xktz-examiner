# Docs About How To Use The Examiner

## Examiner Settings

### "project" (required)

_project_ is the setting for the **language runtime** for projects. It specifies the language setting for the
application needs to examine.

### "examiner" (required)

_examiner_ is the setting for the **language runtime** for examiners. It is the application examines the project's
output by given input.

### "generator" (required)

_generator_ is the setting for the **language runtime** for the generator. It generates random input could be used for
the project.

### "charset" (required)

_charset_ is the charset for the application. The program will transform the output in stdout & stderr from given
charset, then put it into output.

#### charset "byte"

"byte" is a special charset. It means not giving the data a charset at all. Directly use the byte in std stream to
output.

## Language Runtime Settings

"Language runtime" is designed as the objects able to run a given project. Now it is supporting only limited c/c++ and
java. However, the support for plugins providing different language runtimes is on developing.

### Basic Settings

| field         | type           | description                                                                                                                                                | default    |
|---------------|----------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|------------|
| lang          | string         | language of project                                                                                                                                        | REQUIRED |
| dir           | string         | The project directory of language. It helps transform all the related path for the input file in to absolute.                                              | .          |
| commandOutput | list\<string\> | command output is the place outputting the system command (like dos or bash). Supporting <br/> <ul><li>"STDOUT": stdout</li><li>"STDERR": stderr</li></ul> | ["STDERR"] |

### CppRuntime

This is the runtime setting for for c/c++.

| field    | type           | description                                                                                                    | default  |
|----------|----------------|----------------------------------------------------------------------------------------------------------------|----------|
| time     | int            | time limit for runtime (millseconds)                                                                           | 1000     |
| compiler | string         | compiler (file path). If it is added into path then it can directly file name. Support Clang and GCC.          | g++      |
| files    | list\<string\> | files need to compile. Either a relative path (relative to project dir) or an absolute one.                    | []       |
| out      | string         | output file (**related to work directory**)                                                                    | REQUIRED |
| includes | list\<string\> | include folders                                                                                                | []       |
| libdirs  | list\<string\> | library folders                                                                                                | []       |
| libs     | list\<string\> | libraries used                                                                                                 | []       |
| lls      | list\<string\> | this is the clang's .ll files that needs to be added into the project. (require adding "llc" into path to run) | []       |
| args     | list\<string\> | additional arguments                                                                                           | []       |
| runby    | string         | a string in the form of arguments with $, standing for the placeholder of exectuable                           | $        |
An example C++ runtime setting:

```json
{
  "lang": "cpp",
  "compiler": "g++",
  "files": [
    "main.cpp",
    "utils.cpp"
  ],
  "includes": [
    "./utils",
    "./generator"
  ],
  "lls": [
    "generator.ll"
  ],
  "out": "project"
}
```


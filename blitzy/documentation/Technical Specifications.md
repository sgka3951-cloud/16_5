# Technical Specification

# 0. Agent Action Plan

## 0.1 Executive Summary

Based on the bug description, the Blitzy platform understands that the bug is the **complete absence of a Java Age Calculator application** in a repository that is expected to contain a fully functional command-line program computing a user's exact age (in years, months, and days) from a Date of Birth entered in `DD/MM/YYYY` format.

The repository (`16_5`) currently contains only a single `README.md` file with the placeholder heading `# 16_5`. No Java source files, build configurations, test suites, or project manifests exist. The user's specification defines a complete Java application with the following expected behavior:

- **Input**: Date of Birth in `DD/MM/YYYY` format via standard input
- **Processing**: Age calculation using `java.time.LocalDate`, `java.time.Period`, and `java.time.format.DateTimeFormatter`
- **Output**: Age displayed as `Your age is X years, Y months, and Z days.`
- **Validation**: Reject future dates, invalid calendar dates (e.g., `31/02/2020`), and malformed input strings with meaningful error messages

The bug manifests as a **total functional failure**: the application cannot execute because no code exists. This represents a missing-implementation defect requiring creation of the entire application from scratch, including:

- A main `AgeCalculator` class with OOP structure
- Date parsing with strict validation using `ResolverStyle.STRICT`
- Proper exception handling via `try-catch` blocks
- Correct leap year handling through Java's built-in `LocalDate` API
- Comprehensive edge case coverage for invalid and boundary inputs

**Critical Technical Nuance Identified**: Java's `DateTimeFormatter.ofPattern()` defaults to `ResolverStyle.SMART`, which silently corrects invalid dates (e.g., `31/02/2020` becomes `29/02/2020` or `28/02/2020`). The implementation must explicitly use `ResolverStyle.STRICT` with pattern `dd/MM/uuuu` (not `dd/MM/yyyy`) to properly reject invalid dates. Using `yyyy` with `STRICT` mode causes parse failures because `yyyy` means "year-of-era" in Java's DateTimeFormatter, which does not map directly to a `LocalDate` field.


## 0.2 Root Cause Identification

Based on research, THE root cause is: **The Java Age Calculator application was never implemented.** The repository is a greenfield project containing zero source code.

- **Located in**: Repository root `/` — the only file present is `README.md` (1 line: `# 16_5`)
- **Triggered by**: No Java source files, no `src/` directory, no build tool configuration (no `pom.xml`, `build.gradle`, or `Makefile`), and no `main()` entry point exist anywhere in the repository
- **Evidence**:
  - `find . -type f` in the repository root returned only `./README.md` and `.git/` internals
  - `git log --oneline --all` shows a single commit: `282b701 Initial commit` containing only `README.md`
  - No branches contain any Java code (the `dfsfs` remote branch points to the same initial commit)
  - Java was not installed on the system until provisioned during setup (OpenJDK 17.0.18)
- **This conclusion is definitive because**: The entire repository file tree was enumerated using both `get_source_folder_contents` (which lists the root as having a single child: `README.md`) and `find . -type f` (which confirmed no files exist outside of `.git/`). There is no code to compile, no class to execute, and no functionality to test.

### 0.2.1 Secondary Root Causes (Implementation Risks)

Even once the application is created, the following technical pitfalls must be addressed to avoid latent bugs:

- **Root Cause 2 — `yyyy` vs `uuuu` in STRICT mode**: Java's `DateTimeFormatter` interprets `yyyy` as "year-of-era" (BC/AD context). When combined with `ResolverStyle.STRICT`, using `yyyy` in a pattern causes `DateTimeParseException` for perfectly valid dates because `STRICT` mode cannot resolve "year-of-era" to a `LocalDate` field. The correct pattern character is `uuuu` (proleptic year).
- **Root Cause 3 — Default `ResolverStyle.SMART` silently corrects invalid dates**: `DateTimeFormatter.ofPattern("dd/MM/yyyy")` uses `ResolverStyle.SMART` by default. This means `31/02/2020` does NOT throw an exception — it silently adjusts to `29/02/2020` (or `28/02/2020` in a non-leap year). The user's requirement to reject `31/02/2020` with an error message requires explicit use of `.withResolverStyle(ResolverStyle.STRICT)`.
- **Root Cause 4 — Missing future-date validation**: `LocalDate.parse()` alone does not prevent future dates. A separate check (`birthDate.isAfter(LocalDate.now())`) is required, and it must be executed after successful parsing to provide a distinct error message.


## 0.3 Diagnostic Execution

### 0.3.1 Code Examination Results

- **File analyzed**: `README.md` (the only file in the repository)
- **Problematic code block**: Lines 1–1 (entire file content: `# 16_5`)
- **Specific failure point**: No Java source file exists; there is no `main()` method, no class definition, and no import statements for `java.time.*`
- **Execution flow leading to bug**: Any attempt to compile or run a Java application fails immediately because no `.java` files exist in the repository. The execution flow terminates at the very first step: "locate source file."

### 0.3.2 Repository File Analysis Findings

| Tool Used | Command Executed | Finding | File:Line |
|-----------|-----------------|---------|-----------|
| get_source_folder_contents | Root folder `""` | Single child: `README.md` — no Java files, no directories | `/README.md` |
| read_file | `README.md [1, -1]` | Content is exactly `# 16_5` — one line, no code | `README.md:1` |
| bash (find) | `find . -type f` (in repo root) | Only `README.md` and `.git/` internals found | Repository root |
| bash (git log) | `git log --oneline --all` | Single commit `282b701 Initial commit` with only `README.md` | `.git/` |
| bash (git diff) | `git diff main remotes/origin/dfsfs --stat` | No differences; `dfsfs` branch is identical to `main` | `.git/` |
| bash (java -version) | `java -version` before setup | `command not found` — Java was not installed | System |
| bash (java -version) | `java -version` after setup | `openjdk version "17.0.18"` — Successfully installed | `/usr/lib/jvm/java-17-openjdk-amd64` |
| bash (ls repo) | `ls -la` in repo root | Only `.git/` directory and `README.md` present | Repository root |

### 0.3.3 Fix Verification Analysis

- **Steps followed to reproduce bug**:
  - Cloned repository and listed all files — confirmed only `README.md` exists
  - Attempted to locate any `.java` files — none found
  - Verified all branches (`main`, `remotes/origin/dfsfs`) — both contain only `README.md`
  - Confirmed Java runtime was not pre-installed on the system

- **Confirmation tests to ensure the bug is fixed**:
  - After creating the Java source files, compile with `javac AgeCalculator.java` — must succeed with exit code 0
  - Execute with valid input `15/08/1998` — must produce output matching `Your age is X years, Y months, and Z days.`
  - Execute with invalid date `31/02/2020` — must produce a meaningful error message, NOT a silent correction
  - Execute with future date — must produce a meaningful error message
  - Execute with malformed input (e.g., `abc`) — must produce a meaningful error message

- **Boundary conditions and edge cases covered**:
  - Leap year DOB: `29/02/2000` — valid date, must calculate age correctly
  - Non-leap year Feb 29: `29/02/2019` — invalid date, must be rejected
  - Today's date as DOB: age should be `0 years, 0 months, and 0 days`
  - Dates with single-digit day/month: `01/01/2000` — must parse correctly
  - Very old dates: `01/01/1900` — must calculate correctly
  - Century boundary: `31/12/1999` and `01/01/2000` — must handle correctly

- **Verification confidence level**: 90% — High confidence that the fix will resolve all identified issues. The remaining 10% uncertainty accounts for potential locale-specific formatting differences across deployment environments.


## 0.4 Bug Fix Specification

### 0.4.1 The Definitive Fix

The fix requires creating the complete Java Age Calculator application from scratch. Two Java source files must be created at the repository root:

- **File to create**: `AgeCalculator.java` — Main application class containing the entry point, user interaction logic, date parsing, validation, and age computation
- **File to create**: `AgeCalculatorTest.java` — Test harness to validate all functional requirements and edge cases
- **This fixes the root cause by**: Implementing the entire missing application using Java 17, the `java.time` API (`LocalDate`, `Period`, `DateTimeFormatter` with `ResolverStyle.STRICT`), OOP principles, and comprehensive input validation with `try-catch` exception handling

### 0.4.2 Change Instructions

**CREATE file `AgeCalculator.java`** at repository root with the following structure:

- **Imports**: `java.time.LocalDate`, `java.time.Period`, `java.time.format.DateTimeFormatter`, `java.time.format.DateTimeParseException`, `java.time.format.ResolverStyle`, `java.util.Scanner`
- **Class `AgeCalculator`**: Public class following OOP principles with:
  - A `private static final DateTimeFormatter` field initialized with pattern `dd/MM/uuuu` and `ResolverStyle.STRICT` — this ensures invalid dates like `31/02/2020` are properly rejected rather than silently corrected, and uses `uuuu` instead of `yyyy` for correct behavior in STRICT mode
  - A `public static Period calculateAge(LocalDate birthDate, LocalDate currentDate)` method — accepts two `LocalDate` parameters and returns a `Period` object representing the difference. Throws `IllegalArgumentException` if `birthDate` is after `currentDate`
  - A `public static LocalDate parseDateOfBirth(String dobString)` method — parses a `DD/MM/YYYY` formatted string into a `LocalDate` using the STRICT formatter. Catches `DateTimeParseException` and re-throws with a user-friendly message
  - A `public static void main(String[] args)` method — entry point that reads DOB from `Scanner(System.in)`, calls the parse and calculate methods, and prints the result in the format `Your age is X years, Y months, and Z days.`

**Key implementation details**:

- The `DateTimeFormatter` MUST be created as:
  ```java
  DateTimeFormatter.ofPattern("dd/MM/uuuu")
      .withResolverStyle(ResolverStyle.STRICT);
  ```
  This is critical because the default `SMART` resolver silently corrects `31/02/2020` to a valid date, violating the user's validation requirement. Using `uuuu` instead of `yyyy` avoids the "year-of-era" ambiguity that causes failures in STRICT mode.

- The future-date check MUST be a separate validation step after successful parsing:
  ```java
  if (birthDate.isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("...");
  }
  ```

- The age calculation MUST use `Period.between()`:
  ```java
  Period period = Period.between(birthDate, currentDate);
  ```

- Output format MUST match exactly:
  ```java
  System.out.println("Your age is " + years
      + " years, " + months + " months, and "
      + days + " days.");
  ```

**CREATE file `AgeCalculatorTest.java`** at repository root with the following test cases:

- Valid DOB test: `15/08/1998` — verifies non-null `Period` with positive years
- Leap year DOB test: `29/02/2000` — verifies valid parsing and correct age
- Invalid date test: `31/02/2020` — verifies `DateTimeParseException` is thrown
- Future date test: A date 1 year ahead of `LocalDate.now()` — verifies `IllegalArgumentException` is thrown
- Malformed input test: `abc`, `15-08-1998`, `1998/08/15` — verifies `DateTimeParseException` is thrown for wrong formats
- Today's date test: `LocalDate.now()` formatted as `DD/MM/YYYY` — verifies age of 0 years, 0 months, 0 days
- Edge case test: `01/01/1900` — verifies the application handles very old dates

### 0.4.3 Fix Validation

- **Test command to verify fix**:
  ```
  javac AgeCalculator.java && echo "15/08/1998" | java AgeCalculator
  ```
- **Expected output after fix**: `Your age is X years, Y months, and Z days.` (where X, Y, Z are calculated from `15/08/1998` to the current system date)
- **Confirmation method**:
  - Compile both files: `javac AgeCalculator.java AgeCalculatorTest.java`
  - Run test harness: `java AgeCalculatorTest`
  - Verify all test cases pass with expected outputs
  - Manually test with piped input for edge cases:
    - `echo "31/02/2020" | java AgeCalculator` — must show error message
    - `echo "29/02/2000" | java AgeCalculator` — must show valid age
    - `echo "abc" | java AgeCalculator` — must show format error message


## 0.5 Scope Boundaries

### 0.5.1 Changes Required (Exhaustive List)

| Action | File Path | Description |
|--------|-----------|-------------|
| CREATE | `AgeCalculator.java` | Main application class with `main()` entry point, date parsing with `ResolverStyle.STRICT`, validation, age calculation using `Period.between()`, and formatted output. Implements OOP principles with separate static methods for parsing, validation, and calculation. |
| CREATE | `AgeCalculatorTest.java` | Test harness class containing test methods for all specified test cases: valid DOB, leap year DOB, invalid date, future date, wrong format input, today's date boundary, and very old date edge case. |
| UNMODIFIED | `README.md` | Existing file remains unchanged — contains repository title `# 16_5` |

No other files require creation or modification.

### 0.5.2 Explicitly Excluded

- **Do not create**: Build tool configurations (`pom.xml`, `build.gradle`, `Makefile`) — the user specified a straightforward Java application compiled directly with `javac`
- **Do not create**: GUI components (Java Swing or JavaFX) — these are listed as "Optional Enhancements" and are explicitly out of scope for the core bug fix
- **Do not create**: Utility class for reusable methods — listed as an optional enhancement, not a core requirement
- **Do not add**: Countdown to next birthday feature — optional enhancement, out of scope
- **Do not add**: Total age in months and days display — optional enhancement, out of scope
- **Do not modify**: `README.md` — the existing file serves as the repository title and does not need changes for the fix
- **Do not create**: Package structure (e.g., `src/main/java/...`) — the user's specification implies a flat file structure at the repository root
- **Do not add**: Third-party dependencies — the application uses only JDK standard library classes (`java.time.*`, `java.util.Scanner`)


## 0.6 Verification Protocol

### 0.6.1 Bug Elimination Confirmation

- **Execute**: `javac AgeCalculator.java && echo "15/08/1998" | java AgeCalculator`
- **Verify output matches**: `Your age is X years, Y months, and Z days.` where X, Y, Z are dynamically computed from `15/08/1998` to the current system date using `Period.between()`
- **Confirm error no longer appears in**: Standard output and standard error — the application must compile without warnings and execute without throwing unhandled exceptions for valid input
- **Validate functionality with**:
  - `echo "29/02/2000" | java AgeCalculator` — must produce valid age output (leap year DOB)
  - `echo "31/02/2020" | java AgeCalculator` — must display error: invalid date message (STRICT validation rejects impossible date)
  - `echo "01/01/2099" | java AgeCalculator` — must display error: future date message (if run before 2099)
  - `echo "not-a-date" | java AgeCalculator` — must display error: incorrect format message

### 0.6.2 Regression Check

- **Run existing test suite**: `javac AgeCalculatorTest.java AgeCalculator.java && java AgeCalculatorTest`
- **Verify unchanged behavior in**:
  - `README.md` — must remain at `# 16_5` with no modifications
  - Git history — only new files should be added; existing files must not be altered
- **Confirm the following test scenarios pass**:

| Test Case | Input | Expected Outcome |
|-----------|-------|-----------------|
| Normal DOB | `15/08/1998` | Valid age output: `Your age is X years, Y months, and Z days.` |
| Leap year DOB | `29/02/2000` | Valid age output with correct calculation |
| Invalid date | `31/02/2020` | Error message indicating invalid date |
| Future date | A date after today | Error message indicating DOB cannot be in the future |
| Wrong format | `1998/08/15` | Error message indicating incorrect format |
| Malformed input | `abc` | Error message indicating incorrect format |
| Empty input | `` (empty string) | Error message indicating incorrect format |
| Today's date | Current date in DD/MM/YYYY | `Your age is 0 years, 0 months, and 0 days.` |
| Very old date | `01/01/1900` | Valid age output with large year value |
| Century non-leap | `29/02/1900` | Error message (1900 is NOT a leap year) |
| Century leap | `29/02/2000` | Valid age output (2000 IS a leap year) |

- **Confirm performance metrics**: Compilation and execution must complete within 5 seconds on a standard system. The `javac` compilation should produce zero warnings.


## 0.7 Rules

The following rules and coding guidelines govern the implementation:

### 0.7.1 User-Specified Technical Requirements

- **Use `java.time.LocalDate`** for representing dates without time components
- **Use `java.time.Period`** for computing the difference between two dates in years, months, and days
- **Use `java.time.format.DateTimeFormatter`** for parsing the `DD/MM/YYYY` input format
- **Follow Object-Oriented Programming principles**: The application must be structured as a class with well-defined methods, not a monolithic `main()` method
- **Implement proper exception handling using `try-catch`**: All parsing and validation errors must be caught and converted to meaningful error messages
- **Handle leap years correctly**: The implementation must rely on `LocalDate`'s built-in leap year handling, which correctly identifies divisible-by-400 century years as leap years (e.g., 2000) and non-divisible-by-400 century years as non-leap years (e.g., 1900)
- **Work for users born in any valid year**: No artificial lower bound on birth year

### 0.7.2 Coding Standards

- **Date format pattern**: Use `dd/MM/uuuu` with `ResolverStyle.STRICT` — never `dd/MM/yyyy` which fails in STRICT mode due to "year-of-era" semantics
- **Input validation order**: (1) Parse the string to `LocalDate`, (2) Check if date is in the future, (3) Calculate age — each step with its own error handling
- **Output format**: Exactly `Your age is X years, Y months, and Z days.` — matching the user's specification verbatim
- **Error messages**: Must be user-friendly and specific (e.g., "Invalid date format. Please use DD/MM/YYYY." rather than raw exception stack traces)
- **No external dependencies**: Use only JDK standard library classes
- **Java version compatibility**: Target Java 17 (OpenJDK 17.0.18 installed) — the `java.time` API is available since Java 8, ensuring broad compatibility

### 0.7.3 Implementation Constraints

- Make only the exact specified changes: create `AgeCalculator.java` and `AgeCalculatorTest.java`
- Zero modifications to existing files (`README.md`)
- No optional enhancements in the initial implementation (no GUI, no birthday countdown, no utility class refactoring)
- Extensive testing to prevent regressions
- All test cases specified in the user's prompt must be covered


## 0.8 References

### 0.8.1 Repository Files and Folders Searched

| Path | Type | Tool Used | Finding |
|------|------|-----------|---------|
| `/` (repository root) | Folder | `get_source_folder_contents` | Single child: `README.md` — empty repository |
| `README.md` | File | `read_file` | Content: `# 16_5` (1 line) |
| `.git/` | Directory | `bash` (`find . -type f`) | Standard git internals only |
| `.git/refs/heads/main` | File | `bash` (`git log`) | Single commit `282b701 Initial commit` |
| `remotes/origin/dfsfs` | Branch | `bash` (`git diff`) | Identical to `main`; no additional code |
| System-wide | N/A | `bash` (`find / -name ".blitzyignore"`) | No `.blitzyignore` files found |

### 0.8.2 Web Search Queries and Key Findings

| Query | Key Finding | Source |
|-------|-------------|--------|
| `Java LocalDate Period age calculation DD/MM/YYYY common bugs` | `Period.between(birthDate, currentDate)` is the standard approach; `LocalDate` handles leap years automatically via `withYear()` adjustments | javaspring.net, baeldung.com, howtodoinjava.com |
| `Java DateTimeFormatter DD/MM/YYYY ResolverStyle STRICT invalid date` | Default `ResolverStyle.SMART` silently corrects invalid dates; STRICT mode with `uuuu` (not `yyyy`) pattern is required for proper validation | howtodoinjava.com, mkyong.com, baeldung.com |
| `Java Period.between leap year edge case February 29` | `LocalDate` correctly treats Feb 29 in non-leap years as invalid; `Period.between()` handles leap year boundaries correctly | Oracle JDK bug tracker, coderanch.com |

### 0.8.3 Technical Specification Sections Reviewed

| Section | Key Information Extracted |
|---------|-------------------------|
| 1.2 Executive Summary | Project `16_5` is in specification phase; no implementation exists |
| 1.4 Scope | Template-level scope definitions; no Java-specific requirements documented |
| 3.2 Programming Languages | Tech spec documents Python, TypeScript, Swift, Kotlin, Objective-C — Java not included in the original stack |
| 6.6 Testing Strategy | Comprehensive testing framework documented for Python/Flask and React; not applicable to this Java application but provides testing philosophy guidance |

### 0.8.4 Environment Configuration

| Component | Version | Path |
|-----------|---------|------|
| Java Runtime | OpenJDK 17.0.18 (2026-01-20) | `/usr/lib/jvm/java-17-openjdk-amd64` |
| Java Compiler | javac 17.0.18 | `/usr/lib/jvm/java-17-openjdk-amd64/bin/javac` |
| Operating System | Ubuntu (GitHub Actions runner) | N/A |
| Repository | `16_5` (branch: `main`) | `/tmp/blitzy/16_5/main_0d6e40` |

### 0.8.5 Attachments

No attachments were provided for this project. No Figma URLs or design assets were referenced.



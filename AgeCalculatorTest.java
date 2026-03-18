import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * AgeCalculatorTest — Comprehensive test harness for the AgeCalculator class.
 *
 * <p>This standalone test harness validates ALL functional requirements and edge cases
 * for the AgeCalculator class without any external testing frameworks. It uses only
 * JDK standard library classes (no JUnit, no TestNG).</p>
 *
 * <p>Test coverage includes:</p>
 * <ul>
 *   <li>Normal date-of-birth parsing and age calculation</li>
 *   <li>Leap year date handling (century and non-century)</li>
 *   <li>Invalid date rejection via {@link ResolverStyle#STRICT} resolver</li>
 *   <li>Future date rejection via {@link IllegalArgumentException}</li>
 *   <li>Malformed input rejection (wrong separators, wrong order, garbage text)</li>
 *   <li>Boundary conditions (today's date as DOB, very old dates)</li>
 *   <li>Empty input handling</li>
 * </ul>
 *
 * <p>Each test is isolated: a failure in one test does not prevent others from running.
 * Results are printed to standard output, and a summary is displayed at the end.
 * The process exits with code 0 if all tests pass, or code 1 if any test fails.</p>
 */
public class AgeCalculatorTest {

    /** Tracks the number of tests that passed. */
    private static int passCount = 0;

    /** Tracks the number of tests that failed. */
    private static int failCount = 0;

    /**
     * A strict date formatter matching AgeCalculator's internal formatter.
     * Used in tests to format {@link LocalDate} objects into DD/MM/YYYY strings
     * for input to {@link AgeCalculator#parseDateOfBirth(String)}.
     *
     * <p>Uses {@code "dd/MM/uuuu"} with {@link ResolverStyle#STRICT} to ensure
     * consistency with the AgeCalculator's own parsing behavior.</p>
     */
    private static final DateTimeFormatter TEST_FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    /**
     * Entry point for the test harness. Runs all test cases sequentially, reports
     * individual PASS/FAIL results, prints a summary, and exits with the appropriate
     * exit code.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("=== AgeCalculator Test Suite ===");
        System.out.println();

        // Test 3.1: Normal DOB
        runTest("Normal DOB (15/08/1998)", AgeCalculatorTest::testNormalDob);

        // Test 3.2: Leap Year DOB
        runTest("Leap Year DOB (29/02/2000)", AgeCalculatorTest::testLeapYearDob);

        // Test 3.3a: Invalid Date - February 31
        runTest("Invalid Date - Feb 31 (31/02/2020)",
            AgeCalculatorTest::testInvalidDateFeb31);

        // Test 3.3b: Invalid Date - Non-Leap Year Feb 29
        runTest("Invalid Date - Non-Leap Feb 29 (29/02/2019)",
            AgeCalculatorTest::testInvalidDateNonLeapFeb29);

        // Test 3.3c: Invalid Date - Century Non-Leap (29/02/1900)
        runTest("Invalid Date - Century Non-Leap (29/02/1900)",
            AgeCalculatorTest::testInvalidDateCenturyNonLeap1900);

        // Test 3.4: Future Date
        runTest("Future Date", AgeCalculatorTest::testFutureDate);

        // Test 3.5a: Malformed Input - garbage text
        runTest("Malformed Input - 'abc'", AgeCalculatorTest::testMalformedAbc);

        // Test 3.5b: Malformed Input - wrong separator
        runTest("Malformed Input - Wrong Separator '15-08-1998'",
            AgeCalculatorTest::testMalformedWrongSeparator);

        // Test 3.5c: Malformed Input - wrong format order
        runTest("Malformed Input - Wrong Format '1998/08/15'",
            AgeCalculatorTest::testMalformedWrongFormatOrder);

        // Test 3.6: Today's Date Boundary
        runTest("Today's Date Boundary",
            AgeCalculatorTest::testTodaysDateBoundary);

        // Test 3.7: Very Old Date
        runTest("Very Old Date (01/01/1900)", AgeCalculatorTest::testVeryOldDate);

        // Test 3.8: Century Non-Leap Year (explicit separate verification)
        runTest("Century Non-Leap Year - Explicit (29/02/1900)",
            AgeCalculatorTest::testCenturyNonLeapYear);

        // Test 3.9: Century Leap Year
        runTest("Century Leap Year (29/02/2000)",
            AgeCalculatorTest::testCenturyLeapYear);

        // Test 3.10: Empty Input
        runTest("Empty Input", AgeCalculatorTest::testEmptyInput);

        // Print summary
        System.out.println();
        System.out.println("================================");
        int total = passCount + failCount;
        System.out.println("Tests passed: " + passCount + "/" + total);

        if (failCount > 0) {
            System.out.println("SOME TESTS FAILED!");
            System.exit(1);
        } else {
            System.out.println("ALL TESTS PASSED!");
            System.exit(0);
        }
    }

    // =========================================================================
    // Test Infrastructure
    // =========================================================================

    /**
     * Functional interface for test cases that may throw checked exceptions.
     * This enables test methods to propagate exceptions naturally rather than
     * requiring verbose try-catch blocks inside each test.
     */
    @FunctionalInterface
    private interface TestCase {
        /** Executes the test logic; throws Exception on assertion failure. */
        void execute() throws Exception;
    }

    /**
     * Runs a single test case with full isolation. If the test passes (completes
     * without throwing), the pass counter increments and "PASS" is printed.
     * If any exception or assertion error is thrown, the fail counter increments
     * and "FAIL" is printed with the error details. One test failure never halts
     * the execution of subsequent tests.
     *
     * @param testName descriptive name of the test for the output report
     * @param testCase the test logic to execute
     */
    private static void runTest(String testName, TestCase testCase) {
        try {
            testCase.execute();
            passCount++;
            System.out.println("  PASS: " + testName);
        } catch (AssertionError e) {
            failCount++;
            System.out.println("  FAIL: " + testName + " - " + e.getMessage());
        } catch (Exception e) {
            failCount++;
            System.out.println("  FAIL: " + testName + " - Unexpected "
                + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * Asserts that a boolean condition is true.
     *
     * @param condition the condition to verify
     * @param message   descriptive error message if the assertion fails
     * @throws AssertionError if the condition is false
     */
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    /**
     * Asserts that two objects are equal using {@link Object#equals(Object)}.
     * Handles null values safely.
     *
     * @param expected the expected value
     * @param actual   the actual value produced by the code under test
     * @param message  descriptive context for the assertion
     * @throws AssertionError if the objects are not equal
     */
    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError(
                message + " - Expected: " + expected + ", Got: " + actual);
        }
    }

    /**
     * Asserts that two int values are equal. Provides a clear error message
     * showing both expected and actual values on mismatch.
     *
     * @param expected the expected int value
     * @param actual   the actual int value
     * @param message  descriptive context for the assertion
     * @throws AssertionError if the values differ
     */
    private static void assertIntEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(
                message + " - Expected: " + expected + ", Got: " + actual);
        }
    }

    /**
     * Asserts that an object reference is not null.
     *
     * @param obj     the object to check
     * @param message descriptive error message if the object is null
     * @throws AssertionError if the object is null
     */
    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message);
        }
    }

    // =========================================================================
    // Test Case Implementations
    // =========================================================================

    /**
     * Test 3.1: Normal DOB — Verifies that a standard valid date "15/08/1998"
     * is parsed correctly to LocalDate(1998, 8, 15) and that the age calculation
     * produces a Period with positive years.
     */
    private static void testNormalDob() throws Exception {
        LocalDate birthDate = AgeCalculator.parseDateOfBirth("15/08/1998");
        assertNotNull(birthDate,
            "parseDateOfBirth should return non-null for valid input '15/08/1998'");
        assertEquals(LocalDate.of(1998, 8, 15), birthDate,
            "Parsed date should be 1998-08-15");

        Period age = AgeCalculator.calculateAge(birthDate, LocalDate.now());
        assertNotNull(age, "calculateAge should return non-null Period");
        assertTrue(age.getYears() > 0,
            "Years should be positive for DOB 15/08/1998 (got " + age.getYears() + ")");
    }

    /**
     * Test 3.2: Leap Year DOB — Verifies that "29/02/2000" (valid because 2000
     * is a leap year, divisible by 400) parses correctly and produces a valid age
     * with positive years.
     */
    private static void testLeapYearDob() throws Exception {
        LocalDate birthDate = AgeCalculator.parseDateOfBirth("29/02/2000");
        assertNotNull(birthDate,
            "parseDateOfBirth should return non-null for leap year date '29/02/2000'");
        assertEquals(LocalDate.of(2000, 2, 29), birthDate,
            "Parsed date should be 2000-02-29");

        Period age = AgeCalculator.calculateAge(birthDate, LocalDate.now());
        assertNotNull(age, "calculateAge should return non-null Period");
        assertTrue(age.getYears() > 0,
            "Years should be positive for DOB 29/02/2000 (got " + age.getYears() + ")");
    }

    /**
     * Test 3.3a: Invalid Date — February 31 never exists in any year. The STRICT
     * resolver must reject "31/02/2020" with a DateTimeParseException rather than
     * silently correcting it to a valid date (which the default SMART resolver would do).
     */
    private static void testInvalidDateFeb31() throws Exception {
        boolean exceptionThrown = false;
        try {
            AgeCalculator.parseDateOfBirth("31/02/2020");
        } catch (DateTimeParseException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown,
            "parseDateOfBirth must throw DateTimeParseException for '31/02/2020'");
    }

    /**
     * Test 3.3b: Invalid Date — 2019 is not a leap year (not divisible by 4...
     * actually 2019 is not divisible by 4, so Feb 29 does not exist). The STRICT
     * resolver must reject "29/02/2019".
     */
    private static void testInvalidDateNonLeapFeb29() throws Exception {
        boolean exceptionThrown = false;
        try {
            AgeCalculator.parseDateOfBirth("29/02/2019");
        } catch (DateTimeParseException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown,
            "parseDateOfBirth must throw DateTimeParseException for '29/02/2019'");
    }

    /**
     * Test 3.3c: Invalid Date — 1900 is NOT a leap year because it is divisible
     * by 100 but NOT divisible by 400. The STRICT resolver must reject "29/02/1900".
     * This is the same underlying check as Test 3.8 but appears here as part of
     * the invalid-date test group.
     */
    private static void testInvalidDateCenturyNonLeap1900() throws Exception {
        boolean exceptionThrown = false;
        try {
            AgeCalculator.parseDateOfBirth("29/02/1900");
        } catch (DateTimeParseException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown,
            "parseDateOfBirth must throw DateTimeParseException for '29/02/1900' "
            + "(1900 is NOT a leap year)");
    }

    /**
     * Test 3.4: Future Date — Constructs a date 1 year in the future, formats it
     * as DD/MM/YYYY, parses it via AgeCalculator.parseDateOfBirth (which should
     * succeed since the date is calendrically valid), then verifies that
     * AgeCalculator.calculateAge throws IllegalArgumentException because the
     * birth date is after the current date.
     */
    private static void testFutureDate() throws Exception {
        LocalDate futureDate = LocalDate.now().plusYears(1);
        String futureDateStr = futureDate.format(TEST_FORMATTER);

        // Parsing a future date should succeed (it is a valid calendar date)
        LocalDate parsedDate = AgeCalculator.parseDateOfBirth(futureDateStr);
        assertNotNull(parsedDate,
            "parseDateOfBirth should successfully parse a future date string");

        // Age calculation must reject future birth dates
        boolean exceptionThrown = false;
        try {
            AgeCalculator.calculateAge(parsedDate, LocalDate.now());
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown,
            "calculateAge must throw IllegalArgumentException for future birth date");
    }

    /**
     * Test 3.5a: Malformed Input — "abc" is completely non-numeric garbage input.
     * The parser must reject it with a DateTimeParseException.
     */
    private static void testMalformedAbc() throws Exception {
        boolean exceptionThrown = false;
        try {
            AgeCalculator.parseDateOfBirth("abc");
        } catch (DateTimeParseException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown,
            "parseDateOfBirth must throw DateTimeParseException for 'abc'");
    }

    /**
     * Test 3.5b: Malformed Input — "15-08-1998" uses hyphens instead of slashes.
     * The DD/MM/YYYY pattern requires slash separators, so this must be rejected.
     */
    private static void testMalformedWrongSeparator() throws Exception {
        boolean exceptionThrown = false;
        try {
            AgeCalculator.parseDateOfBirth("15-08-1998");
        } catch (DateTimeParseException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown,
            "parseDateOfBirth must throw DateTimeParseException for '15-08-1998'");
    }

    /**
     * Test 3.5c: Malformed Input — "1998/08/15" is in YYYY/MM/DD format instead
     * of DD/MM/YYYY. The parser must reject this incorrect ordering.
     */
    private static void testMalformedWrongFormatOrder() throws Exception {
        boolean exceptionThrown = false;
        try {
            AgeCalculator.parseDateOfBirth("1998/08/15");
        } catch (DateTimeParseException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown,
            "parseDateOfBirth must throw DateTimeParseException for '1998/08/15'");
    }

    /**
     * Test 3.6: Today's Date Boundary — When the date of birth is today's date,
     * the calculated age must be exactly 0 years, 0 months, and 0 days. This
     * validates the boundary condition where DOB equals the current date.
     */
    private static void testTodaysDateBoundary() throws Exception {
        LocalDate today = LocalDate.now();
        String todayStr = today.format(TEST_FORMATTER);

        LocalDate parsedDate = AgeCalculator.parseDateOfBirth(todayStr);
        assertNotNull(parsedDate, "parseDateOfBirth should parse today's date");

        Period age = AgeCalculator.calculateAge(parsedDate, today);
        assertIntEquals(0, age.getYears(),
            "Years should be 0 when DOB is today");
        assertIntEquals(0, age.getMonths(),
            "Months should be 0 when DOB is today");
        assertIntEquals(0, age.getDays(),
            "Days should be 0 when DOB is today");
    }

    /**
     * Test 3.7: Very Old Date — "01/01/1900" is a valid historical date. The parser
     * must accept it, and the age calculation must produce a Period with years
     * greater than 100 (valid as long as the current year is after 2000).
     */
    private static void testVeryOldDate() throws Exception {
        LocalDate birthDate = AgeCalculator.parseDateOfBirth("01/01/1900");
        assertNotNull(birthDate,
            "parseDateOfBirth should return non-null for '01/01/1900'");
        assertEquals(LocalDate.of(1900, 1, 1), birthDate,
            "Parsed date should be 1900-01-01");

        Period age = AgeCalculator.calculateAge(birthDate, LocalDate.now());
        assertNotNull(age, "calculateAge should return non-null Period");
        assertTrue(age.getYears() > 100,
            "Years should be > 100 for DOB 01/01/1900 (got " + age.getYears() + ")");
    }

    /**
     * Test 3.8: Century Non-Leap Year — Explicit separate verification that 1900
     * is correctly identified as NOT a leap year. 1900 is divisible by 100 but
     * NOT by 400, so February 29, 1900 did not exist. The STRICT resolver must
     * reject "29/02/1900".
     */
    private static void testCenturyNonLeapYear() throws Exception {
        boolean exceptionThrown = false;
        try {
            AgeCalculator.parseDateOfBirth("29/02/1900");
        } catch (DateTimeParseException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown,
            "29/02/1900 must be rejected (1900 is NOT a leap year — century rule: "
            + "divisible by 100 but not by 400)");
    }

    /**
     * Test 3.9: Century Leap Year — Explicit verification that "29/02/2000" is
     * accepted as a valid date. 2000 IS a leap year because it is divisible by 400.
     * This is tested separately from 3.2 for explicit validation of the century
     * leap year rule.
     */
    private static void testCenturyLeapYear() throws Exception {
        LocalDate birthDate = AgeCalculator.parseDateOfBirth("29/02/2000");
        assertNotNull(birthDate,
            "parseDateOfBirth must accept '29/02/2000' (2000 is a century leap year)");
        assertEquals(LocalDate.of(2000, 2, 29), birthDate,
            "Parsed date should be 2000-02-29 for century leap year");
    }

    /**
     * Test 3.10: Empty Input — An empty string is not a valid date and must be
     * rejected by the parser with a DateTimeParseException.
     */
    private static void testEmptyInput() throws Exception {
        boolean exceptionThrown = false;
        try {
            AgeCalculator.parseDateOfBirth("");
        } catch (DateTimeParseException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown,
            "parseDateOfBirth must throw DateTimeParseException for empty string");
    }
}

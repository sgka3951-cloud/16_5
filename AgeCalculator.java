import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;

/**
 * AgeCalculator — A command-line Java application that computes a user's exact age
 * (in years, months, and days) from a Date of Birth entered in DD/MM/YYYY format.
 *
 * <p>This class follows OOP principles with separate methods for parsing, validation,
 * and calculation. It uses {@link java.time.LocalDate} and {@link java.time.Period}
 * for accurate date arithmetic, including correct leap year handling.</p>
 *
 * <p><strong>Key design decision:</strong> The {@link DateTimeFormatter} uses pattern
 * {@code "dd/MM/uuuu"} with {@link ResolverStyle#STRICT} to ensure invalid calendar
 * dates (e.g., 31/02/2020, 29/02/2019) are rejected with an exception rather than
 * silently corrected. The pattern character {@code uuuu} (proleptic year) is used
 * instead of {@code yyyy} (year-of-era) because STRICT mode cannot resolve
 * year-of-era to a LocalDate field.</p>
 */
public class AgeCalculator {

    /**
     * A strict date formatter for parsing DD/MM/YYYY input.
     *
     * <p>Uses {@code "dd/MM/uuuu"} pattern with {@link ResolverStyle#STRICT} to ensure:
     * <ul>
     *   <li>Invalid dates like 31/02/2020 throw {@link DateTimeParseException}</li>
     *   <li>Non-leap-year Feb 29 (e.g., 29/02/2019) throws {@link DateTimeParseException}</li>
     *   <li>Century non-leap years (e.g., 29/02/1900) are correctly rejected</li>
     *   <li>Century leap years (e.g., 29/02/2000, divisible by 400) are accepted</li>
     * </ul></p>
     */
    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    /**
     * Parses a date-of-birth string in DD/MM/YYYY format into a {@link LocalDate}.
     *
     * <p>This method uses the strict {@link #DATE_FORMATTER} to parse the input string.
     * Invalid calendar dates (e.g., 31/02/2020), malformed strings (e.g., "abc"),
     * and strings with incorrect separators (e.g., "15-08-1998") will cause a
     * {@link DateTimeParseException} to be thrown with a user-friendly message.</p>
     *
     * <p><strong>Note:</strong> This method does NOT validate whether the parsed date
     * is in the future. Future-date validation is handled separately in
     * {@link #calculateAge(LocalDate, LocalDate)}.</p>
     *
     * @param dobString the date of birth string in DD/MM/YYYY format
     * @return the parsed {@link LocalDate} representing the date of birth
     * @throws DateTimeParseException if the input string is not a valid date in DD/MM/YYYY format
     */
    public static LocalDate parseDateOfBirth(String dobString) {
        try {
            return LocalDate.parse(dobString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new DateTimeParseException(
                "Invalid date format. Please use DD/MM/YYYY format with a valid date.",
                dobString,
                e.getErrorIndex(),
                e
            );
        }
    }

    /**
     * Calculates the age as a {@link Period} between a birth date and a current date.
     *
     * <p>This method first validates that the birth date is not after the current date.
     * If the birth date is in the future, an {@link IllegalArgumentException} is thrown.
     * Otherwise, it computes the exact difference using {@link Period#between(LocalDate, LocalDate)}.</p>
     *
     * <p>This method is pure — it has no I/O or side effects — making it easily testable.</p>
     *
     * @param birthDate   the date of birth as a {@link LocalDate}
     * @param currentDate the reference date (typically today) as a {@link LocalDate}
     * @return a {@link Period} representing the age in years, months, and days
     * @throws IllegalArgumentException if birthDate is after currentDate
     */
    public static Period calculateAge(LocalDate birthDate, LocalDate currentDate) {
        if (birthDate.isAfter(currentDate)) {
            throw new IllegalArgumentException("Date of birth cannot be in the future.");
        }
        return Period.between(birthDate, currentDate);
    }

    /**
     * Application entry point. Reads a date of birth from standard input, computes
     * the user's age, and prints it in the format:
     * {@code Your age is X years, Y months, and Z days.}
     *
     * <p>Validation follows this order:
     * <ol>
     *   <li>Parse the input string to a {@link LocalDate} (catches format/invalid-date errors)</li>
     *   <li>Check if the parsed date is in the future (catches future-date errors)</li>
     *   <li>Calculate and display the age (only reached if both validations pass)</li>
     * </ol></p>
     *
     * <p>All exceptions are caught and converted to user-friendly messages.
     * No stack traces are printed to standard output or standard error.</p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("Enter your date of birth (DD/MM/YYYY):");
            String input = scanner.nextLine().trim();

            // Step 1: Parse the date string (validates format and calendar validity)
            LocalDate birthDate = parseDateOfBirth(input);

            // Step 2: Calculate age (validates the date is not in the future)
            Period age = calculateAge(birthDate, LocalDate.now());

            // Step 3: Extract and display the age components
            int years = age.getYears();
            int months = age.getMonths();
            int days = age.getDays();

            System.out.println("Your age is " + years + " years, " + months
                + " months, and " + days + " days.");

        } catch (DateTimeParseException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred. Please try again.");
        } finally {
            scanner.close();
        }
    }
}

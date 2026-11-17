package utilities;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class ValidationUtil {

    // Why these patterns are compiled ONCE as static finals, not created
    // fresh inside each method call: Pattern.compile() is a relatively
    // expensive operation — since these patterns never change, compiling
    // them once when the class loads (rather than every time a form is
    // submitted) avoids needless repeated work.
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Why exactly 10 digits: standard Indian mobile number length, matching
    // the "phone" field used throughout Customer registration/profile.
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");

    // Why this specific pattern: standard Indian PAN format is 5 letters,
    // 4 digits, 1 letter (e.g. ABCDE1234F) — this is the real-world format
    // your wireframe's "PANNO" field is meant to represent.
    private static final Pattern PAN_PATTERN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");

    // Why exactly 12 digits: standard Aadhaar number length.
    private static final Pattern AADHAAR_PATTERN = Pattern.compile("^[0-9]{12}$");

    // Why a private constructor: this class is a pure static utility, same
    // as PasswordUtil and ConnectionFactory — nobody should ever be able to
    // write "new ValidationUtil()", since there's no meaningful instance
    // state to construct. This is stricter than PasswordUtil/
    // ConnectionFactory (which just never bothered adding a constructor at
    // all), but the effect is the same: instantiation is blocked.
    private ValidationUtil() {
    }

    // Why a generic "not null/not blank" check exists as its own method,
    // separate from the more specific ones below: nearly every field in
    // every form needs this baseline check FIRST, before any
    // format-specific check can even run — e.g., checking email FORMAT on a
    // null string would throw a NullPointerException before ever reaching
    // the regex.
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return isNotEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return isNotEmpty(phone) && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidPan(String pan) {
        return isNotEmpty(pan) && PAN_PATTERN.matcher(pan.toUpperCase()).matches();
    }

    public static boolean isValidAadhaar(String aadhaar) {
        return isNotEmpty(aadhaar) && AADHAAR_PATTERN.matcher(aadhaar).matches();
    }

    // Why this takes a String (raw form input) and returns a BigDecimal
    // directly, rather than just a boolean: every amount field across the
    // project (deposit, withdraw, transfer, FD, RD, admin balance update)
    // needs to do the SAME two things together — check it's a valid
    // positive number, AND get a usable BigDecimal out of it. Returning
    // null on failure lets the caller check "if (amount == null)" as a
    // single combined validity+conversion step, instead of validating and
    // then separately re-parsing the same string.
    public static BigDecimal parsePositiveAmount(String amountStr) {
        if (!isNotEmpty(amountStr)) {
            return null;
        }
        try {
            BigDecimal amount = new BigDecimal(amountStr.trim());
            // Why compareTo(BigDecimal.ZERO) > 0, not >= 0: a deposit,
            // withdrawal, or transfer of exactly zero is meaningless and
            // shouldn't be treated as valid input, even though it's
            // technically a non-negative number.
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                return amount;
            }
            return null;
        } catch (NumberFormatException e) {
            // Why we catch this here, in ONE place, rather than leaving
            // every servlet to individually wrap its own
            // "new BigDecimal(...)" call in its own try-catch: this is
            // exactly the kind of repeated defensive code that belongs in a
            // shared utility — centralizing it means every servlet gets the
            // same safe behavior automatically, just by calling this method
            // instead of parsing directly.
            return null;
        }
    }

    // Why a separate method for whole numbers (years, months): FD's
    // "years" and RD's "months" fields are integers, not decimals — reusing
    // parsePositiveAmount() (which returns BigDecimal) would force an
    // awkward extra conversion step at every call site.
    public static Integer parsePositiveInteger(String intStr) {
        if (!isNotEmpty(intStr)) {
            return null;
        }
        try {
            int value = Integer.parseInt(intStr.trim());
            if (value > 0) {
                return value;
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
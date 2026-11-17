package utilities;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class ValidationUtil {

	private static final Pattern EMAIL_PATTERN =
			Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
	private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
	private static final Pattern PAN_PATTERN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");
	private static final Pattern AADHAAR_PATTERN = Pattern.compile("^[0-9]{12}$");

	// {4,} and {5,} instead of exactly 4 and 5 digits: the generators use
	// %04d / %05d, which pad to AT LEAST that many digits, so after CUST9999
	// the next ID is CUST10000. An exact-length pattern would reject it.
	private static final Pattern CID_PATTERN = Pattern.compile("^CUST[0-9]{4,}$");
	private static final Pattern ACCNO_PATTERN = Pattern.compile("^ACC[0-9]{5,}$");

	// Upper bounds, adjustable in one place. The amount cap keeps values well
	// inside DECIMAL(15,2); the rate cap rejects typos like 650 instead of 6.5.
	public static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000000");
	public static final BigDecimal MAX_RATE = new BigDecimal("20");
	public static final int MIN_PASSWORD_LENGTH = 8;

	private ValidationUtil() {
	}

	public static boolean isNotEmpty(String value) {
		return value != null && !value.trim().isEmpty();
	}

	public static boolean isValidEmail(String email) {
		return isNotEmpty(email) && EMAIL_PATTERN.matcher(email.trim()).matches();
	}

	public static boolean isValidPhone(String phone) {
		return isNotEmpty(phone) && PHONE_PATTERN.matcher(phone.trim()).matches();
	}

	public static boolean isValidPan(String pan) {
		return isNotEmpty(pan) && PAN_PATTERN.matcher(pan.trim().toUpperCase()).matches();
	}

	public static boolean isValidAadhaar(String aadhaar) {
		return isNotEmpty(aadhaar) && AADHAAR_PATTERN.matcher(aadhaar.trim()).matches();
	}

	public static boolean isValidCid(String cid) {
		return isNotEmpty(cid) && CID_PATTERN.matcher(cid.trim()).matches();
	}

	public static boolean isValidAccno(String accno) {
		return isNotEmpty(accno) && ACCNO_PATTERN.matcher(accno.trim()).matches();
	}

	// Why a whitelist: the form's dropdown only offers these two, but a
	// hand-built request could send anything, and the column is free text.
	public static boolean isValidAccountType(String type) {
		return "Saving".equals(type) || "Current".equals(type);
	}

	public static boolean isValidPassword(String password) {
		return password != null && password.trim().length() >= MIN_PASSWORD_LENGTH;
	}

	// Shared by the two public amount methods below: a valid number, at most
	// 2 decimal places, within the cap, and positive (or zero if allowed).
	// stripTrailingZeros() first, so "5.50" and "5.5" are both fine while
	// "5.555" is rejected instead of being silently rounded by MySQL.
	private static BigDecimal parseAmount(String s, boolean allowZero) {
		if (!isNotEmpty(s)) {
			return null;
		}
		try {
			BigDecimal a = new BigDecimal(s.trim());
			if (a.stripTrailingZeros().scale() > 2) {
				return null;
			}
			int sign = a.compareTo(BigDecimal.ZERO);
			if (sign < 0 || (sign == 0 && !allowZero)) {
				return null;
			}
			if (a.compareTo(MAX_AMOUNT) > 0) {
				return null;
			}
			return a;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	// Deposits, withdrawals, transfers, FD/RD amounts: must be above zero.
	public static BigDecimal parsePositiveAmount(String s) {
		return parseAmount(s, false);
	}

	// Admin balance and an account's opening balance: zero is a legitimate value.
	public static BigDecimal parseNonNegativeAmount(String s) {
		return parseAmount(s, true);
	}

	// Annual interest rate in percent: above 0, up to MAX_RATE, 2 decimals.
	public static BigDecimal parseRate(String s) {
		if (!isNotEmpty(s)) {
			return null;
		}
		try {
			BigDecimal r = new BigDecimal(s.trim());
			if (r.stripTrailingZeros().scale() > 2) return null;
			if (r.compareTo(BigDecimal.ZERO) <= 0 || r.compareTo(MAX_RATE) > 0) return null;
			return r;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	// Why a max: FD years and RD months feed straight into plusYears() and
	// plusMonths(), and a huge number would overflow the date calculation.
	public static Integer parsePositiveInteger(String s, int max) {
		if (!isNotEmpty(s)) {
			return null;
		}
		try {
			int v = Integer.parseInt(s.trim());
			return (v > 0 && v <= max) ? v : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	// Why LocalDate.parse instead of java.sql.Date.valueOf: valueOf is lenient
	// and rolls "2026-02-31" over to March, while LocalDate.parse rejects it.
	// An account can't be opened in the future, so those are rejected too.
	public static Date parseOpenDate(String s) {
		if (!isNotEmpty(s)) {
			return null;
		}
		try {
			LocalDate d = LocalDate.parse(s.trim());
			return d.isAfter(LocalDate.now()) ? null : Date.valueOf(d);
		} catch (DateTimeParseException e) {
			return null;
		}
	}
}
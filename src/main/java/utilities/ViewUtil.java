package utilities;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ViewUtil {

	private static final Locale INDIA = Locale.forLanguageTag("en-IN");

	private ViewUtil() {
	}

	// Why this exists: JSPs print user-supplied text (name, email...) straight
	// into HTML. If someone registers with the name <script>...</script>,
	// printing it raw would run that script in the browser (XSS). Escaping
	// turns the dangerous characters into harmless text. Use it on ANY value
	// a user could have typed, never on values you generate yourself.
	public static String escape(String s) {
		if (s == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder(s.length() + 16);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '&':  sb.append("&amp;");  break;
			case '<':  sb.append("&lt;");   break;
			case '>':  sb.append("&gt;");   break;
			case '"':  sb.append("&quot;"); break;
			case '\'': sb.append("&#39;");  break;
			default:   sb.append(c);
			}
		}
		return sb.toString();
	}

	// Why a new NumberFormat per call instead of one shared static instance:
	// NumberFormat is not thread-safe, and servlet containers handle many
	// requests on different threads at once. Creating it per call is cheap
	// and avoids garbled output under concurrent use.
	public static String money(BigDecimal amount) {
		if (amount == null) {
			return "";
		}
		return NumberFormat.getCurrencyInstance(INDIA).format(amount);
	}
	// Why a new SimpleDateFormat per call: it is not thread-safe, same reason as NumberFormat.
	// Works for java.sql.Timestamp and java.sql.Date too, since both extend java.util.Date.
	public static String dateTime(Date d) {
		if (d == null) return "";
		return new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(d);
	}

	public static String date(Date d) {
		if (d == null) return "";
		return new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(d);
	}

	// Maps a status value to its CSS badge class from style.css.
	public static String badge(String status) {
		if ("ACTIVE".equals(status)) return "badge-active";
		if ("MATURED".equals(status)) return "badge-matured";
		return "badge-closed";
	}
}
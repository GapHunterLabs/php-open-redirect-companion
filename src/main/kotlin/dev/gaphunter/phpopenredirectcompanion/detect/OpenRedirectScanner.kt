package dev.gaphunter.phpopenredirectcompanion.detect

import dev.gaphunter.phpopenredirectcompanion.model.OpenRedirectHit

/**
 * Plain-text line scanner for a PHP file -- flags a `header(...)`
 * call whose argument contains a `Location:` redirect built directly
 * from a PHP superglobal (`$_GET`, `$_POST`, `$_REQUEST`, `$_COOKIE`).
 * This is the textbook Open Redirect anti-pattern: an attacker crafts
 * a link to the trusted site that redirects the victim to an
 * attacker-controlled domain, commonly used for phishing since the
 * initial URL still shows the trusted host. OWASP's own "Unvalidated
 * Redirects and Forwards" guidance and AWS CodeGuru's detector
 * library both document this as a real, well-known vulnerability
 * class (CWE-601).
 *
 * Confirmed real gap: "PHP Inspections (EA Extended)" (one of the
 * most widely used PHP inspection plugins on Marketplace) does not
 * cover open redirect detection anywhere in its documented security
 * feature list -- confirmed by reading it before building this.
 *
 * **v0.1 scope, stated honestly:** plain-text regex matching, not
 * real PHP PSI -- only flags a `header(` call whose argument text
 * contains both `Location:` and a direct superglobal reference on the
 * same line. A redirect built from an intermediate variable assigned
 * from a superglobal several lines earlier isn't traced (real
 * data-flow analysis, out of scope for a text scanner).
 */
object OpenRedirectScanner {

    private val HEADER_CALL = Regex("""\bheader\s*\(""")
    private val LOCATION_HEADER = Regex("""Location\s*:""", RegexOption.IGNORE_CASE)
    private val SUPERGLOBAL = Regex("\\\$_(GET|POST|REQUEST|COOKIE)\\b")

    fun scan(text: String): List<OpenRedirectHit> {
        val hits = mutableListOf<OpenRedirectHit>()
        text.lines().forEachIndexed { index, rawLine ->
            val trimmed = rawLine.trimStart()
            if (trimmed.startsWith("//") || trimmed.startsWith("#") || trimmed.startsWith("*")) return@forEachIndexed

            val callMatch = HEADER_CALL.find(rawLine) ?: return@forEachIndexed
            val afterCall = rawLine.substring(callMatch.range.last + 1)
            if (!LOCATION_HEADER.containsMatchIn(afterCall)) return@forEachIndexed
            if (!SUPERGLOBAL.containsMatchIn(afterCall)) return@forEachIndexed

            hits += OpenRedirectHit(index + 1, callMatch.range.first, callMatch.range.last + 1)
        }
        return hits
    }
}

package dev.gaphunter.phpopenredirectcompanion.detect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenRedirectScannerTest {

    @Test
    fun `flags header Location built from a GET superglobal`() {
        val code = "header('Location: ' . \$_GET['url']);"
        val hits = OpenRedirectScanner.scan(code)
        assertEquals(1, hits.size)
    }

    @Test
    fun `flags header Location with interpolated REQUEST superglobal`() {
        val code = "header(\"Location: \$_REQUEST[redirect]\");"
        val hits = OpenRedirectScanner.scan(code)
        assertEquals(1, hits.size)
    }

    @Test
    fun `does not flag header Location with a static literal destination`() {
        val code = "header('Location: /dashboard');"
        assertTrue(OpenRedirectScanner.scan(code).isEmpty())
    }

    @Test
    fun `does not flag a non-Location header built from a superglobal`() {
        val code = "header('X-Custom-Header: ' . \$_GET['value']);"
        assertTrue(OpenRedirectScanner.scan(code).isEmpty())
    }

    @Test
    fun `does not flag header Location built from a local variable`() {
        val code = "header('Location: ' . \$validatedUrl);"
        assertTrue(OpenRedirectScanner.scan(code).isEmpty())
    }

    @Test
    fun `does not flag a commented-out line`() {
        val code = "// header('Location: ' . \$_GET['url']);"
        assertTrue(OpenRedirectScanner.scan(code).isEmpty())
    }
}

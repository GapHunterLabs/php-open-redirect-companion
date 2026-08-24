package dev.gaphunter.phpopenredirectcompanion.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.gaphunter.phpopenredirectcompanion.detect.OpenRedirectScanner
import dev.gaphunter.phpopenredirectcompanion.review.ReviewPrompt

/**
 * Flags a `header('Location: ...')` call built directly from a PHP
 * superglobal -- see [OpenRedirectScanner] for the full reasoning.
 * Runs via [checkFile] (whole-file text scan) -- see
 * `build.gradle.kts` for why no PHP-language PSI dependency is
 * taken.
 */
class OpenRedirectInspection : LocalInspectionTool() {

    companion object {
        const val MAX_FILE_LENGTH = 500_000
        private val PHP_FILE_NAME = Regex("""^[^.]+\.php$""", RegexOption.IGNORE_CASE)
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        val virtualFile = file.virtualFile ?: return null
        if (!PHP_FILE_NAME.matches(virtualFile.name)) return null

        val text = file.text
        if (text.length > MAX_FILE_LENGTH) return null

        val hits = OpenRedirectScanner.scan(text)
        if (hits.isEmpty()) return null

        val document = file.viewProvider.document ?: return null
        val problems = mutableListOf<ProblemDescriptor>()

        for (hit in hits) {
            if (hit.lineNumber - 1 !in 0 until document.lineCount) continue
            val lineStartOffset = document.getLineStartOffset(hit.lineNumber - 1)
            val absoluteStart = lineStartOffset + hit.columnStart
            val absoluteEnd = lineStartOffset + hit.columnEnd
            val anchor = leafElementAt(file, absoluteStart) ?: continue
            val anchorStart = anchor.textRange.startOffset
            val relativeRange = TextRange(
                (absoluteStart - anchorStart).coerceAtLeast(0),
                (absoluteEnd - anchorStart).coerceAtMost(anchor.textLength),
            )
            if (relativeRange.startOffset >= relativeRange.endOffset) continue

            problems += manager.createProblemDescriptor(
                anchor,
                relativeRange,
                "This Location redirect is built directly from a PHP superglobal (\$_GET/\$_POST/\$_REQUEST/" +
                    "\$_COOKIE) -- an attacker can craft a link that redirects victims to an attacker-controlled " +
                    "domain (CWE-601), commonly used for phishing. Validate against a strict allowlist of known " +
                    "destinations instead",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOnTheFly,
            )

            ReviewPrompt.recordHit(file.project, "${virtualFile.path}:${hit.lineNumber}")
        }

        return if (problems.isEmpty()) null else problems.toTypedArray()
    }

    private fun leafElementAt(file: PsiFile, startOffset: Int): PsiElement? {
        if (startOffset < 0 || startOffset >= file.textLength) return null
        var element = file.findElementAt(startOffset) ?: return file
        while (element.firstChild != null) {
            element = element.firstChild
        }
        return element
    }
}

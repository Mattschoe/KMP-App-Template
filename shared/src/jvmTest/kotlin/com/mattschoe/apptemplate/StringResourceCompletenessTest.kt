package com.mattschoe.apptemplate

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Parses the raw composeResources strings.xml files to verify translation
 * completeness and correctness.
 *
 * Catches translation management errors:
 * - Missing keys in locale files
 * - Empty/blank translated values
 * - Mismatched format placeholders between default and translated strings
 * - Orphaned extra keys in translations that don't exist in the default
 *
 * Runs on the JVM target only — it reads files off disk, which is exactly what
 * makes it work without a device or a Compose runtime. Every check loops all
 * locales and reports every failure at once rather than stopping at the first.
 */
class StringResourceCompletenessTest {

    private companion object {
        /** Gradle runs tests with the module directory as the working directory. */
        val resDir = File("src/commonMain/composeResources")

        val FORMAT_PLACEHOLDER_REGEX = Regex("""%\d+\$[a-zA-Z]""")

        fun parseStringsXml(file: File): Map<String, String> {
            val document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(file)
            val nodeList = document.getElementsByTagName("string")
            val result = mutableMapOf<String, String>()
            for (i in 0 until nodeList.length) {
                val element = nodeList.item(i) as Element
                result[element.getAttribute("name")] = element.textContent
            }
            return result
        }
    }

    /** Locale codes discovered by scanning for `values-{locale}/strings.xml`. */
    private val locales: List<String> = resDir.listFiles { file ->
        file.isDirectory && file.name.startsWith("values-") && File(file, "strings.xml").exists()
    }?.map { it.name.removePrefix("values-") }?.sorted() ?: emptyList()

    private val defaultStrings: Map<String, String> by lazy {
        val file = File(resDir, "values/strings.xml")
        if (!file.exists()) fail("No default strings.xml found at $file")
        parseStringsXml(file)
    }

    private fun stringsFor(locale: String) = parseStringsXml(File(resDir, "values-$locale/strings.xml"))

    /** Guards against the whole suite silently passing because discovery broke. */
    @Test
    fun defaultStringsAreNotEmpty() {
        assertTrue(
            defaultStrings.isNotEmpty(),
            "No strings found in values/strings.xml — is $resDir correct?"
        )
    }

    @Test
    fun allDefaultKeysExistInEveryLocale() {
        val problems = locales.mapNotNull { locale ->
            val missing = defaultStrings.keys - stringsFor(locale).keys
            if (missing.isEmpty()) null else "  $locale is missing ${missing.size} key(s): $missing"
        }
        assertTrue(problems.isEmpty(), "Incomplete translations:\n" + problems.joinToString("\n"))
    }

    @Test
    fun noTranslatedValueIsBlank() {
        val problems = locales.flatMap { locale ->
            stringsFor(locale)
                .filterValues { it.isBlank() }
                .keys
                .map { "  $locale: '$it' is blank" }
        }
        assertTrue(problems.isEmpty(), "Blank translations:\n" + problems.joinToString("\n"))
    }

    @Test
    fun formatPlaceholdersMatchTheDefault() {
        val problems = locales.flatMap { locale ->
            val translated = stringsFor(locale)
            defaultStrings.mapNotNull { (key, defaultValue) ->
                val translatedValue = translated[key] ?: return@mapNotNull null
                val expected = FORMAT_PLACEHOLDER_REGEX.findAll(defaultValue).map { it.value }.toSet()
                val actual = FORMAT_PLACEHOLDER_REGEX.findAll(translatedValue).map { it.value }.toSet()
                if (expected == actual) null
                else "  $locale: '$key' expected placeholders $expected but found $actual"
            }
        }
        assertTrue(problems.isEmpty(), "Placeholder mismatches:\n" + problems.joinToString("\n"))
    }

    @Test
    fun noLocaleHasOrphanedKeys() {
        val problems = locales.mapNotNull { locale ->
            val orphaned = stringsFor(locale).keys - defaultStrings.keys
            if (orphaned.isEmpty()) null
            else "  $locale has ${orphaned.size} key(s) not in the default: $orphaned"
        }
        assertTrue(problems.isEmpty(), "Orphaned translation keys:\n" + problems.joinToString("\n"))
    }
}

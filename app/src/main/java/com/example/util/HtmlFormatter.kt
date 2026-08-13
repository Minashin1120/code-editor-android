package com.example.util

object HtmlFormatter {
    fun formatHtml(html: String): String {
        val selfClosingTags = setOf(
            "area", "base", "br", "col", "embed", "hr", "img", "input",
            "link", "meta", "param", "source", "track", "wbr"
        )

        val clean = html.trim()
        if (clean.isEmpty()) return ""

        val sb = StringBuilder()
        var indentLevel = 0
        val indentStr = "  "

        // Tokens splitting by < and >
        val tokens = clean.split("(?=<)|(?<=>)".toRegex())

        for (token in tokens) {
            val trimmedToken = token.trim()
            if (trimmedToken.isEmpty()) continue

            if (trimmedToken.startsWith("</")) {
                // Closing tag
                indentLevel = (indentLevel - 1).coerceAtLeast(0)
                sb.append(indentStr.repeat(indentLevel)).append(trimmedToken).append("\n")
            } else if (trimmedToken.startsWith("<") && trimmedToken.endsWith(">")) {
                val tagName = trimmedToken
                    .drop(1)
                    .dropLast(1)
                    .trim()
                    .split("\\s+".toRegex())
                    .firstOrNull()
                    ?.lowercase() ?: ""

                val isSelfClosing = trimmedToken.endsWith("/>") || selfClosingTags.contains(tagName) || trimmedToken.startsWith("<!") || trimmedToken.startsWith("<?")

                sb.append(indentStr.repeat(indentLevel)).append(trimmedToken).append("\n")

                if (!isSelfClosing) {
                    indentLevel++
                }
            } else {
                // Text node
                sb.append(indentStr.repeat(indentLevel)).append(trimmedToken).append("\n")
            }
        }

        return sb.toString().trimEnd()
    }
}

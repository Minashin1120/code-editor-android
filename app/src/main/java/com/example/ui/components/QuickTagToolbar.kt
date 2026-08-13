package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatIndentDecrease
import androidx.compose.material.icons.automirrored.filled.FormatIndentIncrease
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuickTagToolbar(
    onInsertTag: (startTag: String, endTag: String) -> Unit,
    onFormatCode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickTags = listOf(
        Pair("div", "<div>\n  \n</div>"),
        Pair("p", "<p></p>"),
        Pair("a", "<a href=\"#\"></a>"),
        Pair("span", "<span></span>"),
        Pair("h1", "<h1></h1>"),
        Pair("img", "<img src=\"\" alt=\"\" />"),
        Pair("style", "<style>\n  \n</style>"),
        Pair("script", "<script>\n  \n</script>"),
        Pair("class", " class=\"\""),
        Pair("id", " id=\"\""),
        Pair("comment", "<!--  -->"),
        Pair("ul/li", "<ul>\n  <li></li>\n</ul>"),
        Pair("button", "<button></button>")
    )

    Surface(
        tonalElevation = 6.dp,
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = onFormatCode,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoFixHigh,
                    contentDescription = "コード整形",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            quickTags.forEach { (label, code) ->
                FilterChip(
                    selected = false,
                    onClick = { onInsertTag(code, "") },
                    label = {
                        Text(
                            text = label,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

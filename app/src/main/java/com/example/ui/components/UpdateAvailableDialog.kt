package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.update.RemoteUpdate

@Composable
fun UpdateAvailableDialog(
    currentVersionName: String,
    update: RemoteUpdate,
    onOpenUpdate: () -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新しいバージョンがあります") },
        text = {
            Column {
                Text("使用中: $currentVersionName")
                Text("最新: ${update.versionName}")
                val notes = update.notes
                if (!notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(notes)
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onSkip) {
                    Text("このバージョンをスキップ")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onOpenUpdate) {
                Text("更新ページを開く")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("あとで")
            }
        },
    )
}

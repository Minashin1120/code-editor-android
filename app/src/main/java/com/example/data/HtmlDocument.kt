package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "html_documents")
data class HtmlDocument(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isTemplate: Boolean = false,
    val fileUri: String? = null
)

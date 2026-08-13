package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HtmlDocumentDao {
    @Query("SELECT * FROM html_documents ORDER BY updatedAt DESC")
    fun getAllDocuments(): Flow<List<HtmlDocument>>

    @Query("SELECT * FROM html_documents WHERE id = :id")
    fun getDocumentById(id: Long): Flow<HtmlDocument?>

    @Query("SELECT * FROM html_documents WHERE id = :id")
    suspend fun getDocumentByIdSync(id: Long): HtmlDocument?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: HtmlDocument): Long

    @Update
    suspend fun updateDocument(document: HtmlDocument)

    @Delete
    suspend fun deleteDocument(document: HtmlDocument)

    @Query("DELETE FROM html_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)
}

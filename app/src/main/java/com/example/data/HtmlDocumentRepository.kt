package com.example.data

import kotlinx.coroutines.flow.Flow

class HtmlDocumentRepository(private val dao: HtmlDocumentDao) {
    val allDocuments: Flow<List<HtmlDocument>> = dao.getAllDocuments()

    fun getDocumentById(id: Long): Flow<HtmlDocument?> = dao.getDocumentById(id)

    suspend fun getDocumentByIdSync(id: Long): HtmlDocument? = dao.getDocumentByIdSync(id)

    suspend fun saveDocument(document: HtmlDocument): Long {
        return if (document.id == 0L) {
            dao.insertDocument(document)
        } else {
            dao.updateDocument(document)
            document.id
        }
    }

    suspend fun deleteDocument(id: Long) {
        dao.deleteDocumentById(id)
    }
}

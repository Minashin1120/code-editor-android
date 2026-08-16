package com.example.ui

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.HtmlDocumentRepository
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EditorViewModelSaveTest {

    private lateinit var database: AppDatabase
    private lateinit var viewModel: EditorViewModel
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        viewModel = EditorViewModel(HtmlDocumentRepository(database.htmlDocumentDao()))
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun overwriteFallsBackWhenDocumentHasNoDeviceFile() {
        assertFalse(viewModel.overwriteToDeviceFile(context))
    }

    @Test
    fun inaccessibleDeviceUriIsNotTreatableAsRealFile() {
        val missingUri = android.net.Uri.parse("content://unknown/missing.html")
        assertFalse(viewModel.isDeviceFileAccessible(context, missingUri))
        assertFalse(viewModel.overwriteToDeviceFile(context))
    }
}

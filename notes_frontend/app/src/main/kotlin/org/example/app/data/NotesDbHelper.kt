package org.example.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * SQLiteOpenHelper for the Notes database.
 */
class NotesDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, DatabaseContract.DB_NAME, null, DatabaseContract.DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DatabaseContract.Folders.CREATE)
        db.execSQL(DatabaseContract.Notes.CREATE)
        // Create indexes
        DatabaseContract.Notes.INDEXES.trimIndent().split(";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { db.execSQL("$it;") }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Initial version, no migrations yet.
    }

    companion object {
        @Volatile
        private var INSTANCE: NotesDbHelper? = null

        /**
         * PUBLIC_INTERFACE
         * Get a singleton instance of the database helper.
         */
        fun getInstance(context: Context): NotesDbHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotesDbHelper(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

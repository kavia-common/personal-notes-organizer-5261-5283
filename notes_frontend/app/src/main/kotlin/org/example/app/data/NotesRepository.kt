package org.example.app.data

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.os.Handler
import android.os.Looper
import org.example.app.data.DatabaseContract.Folders
import org.example.app.data.DatabaseContract.Notes
import org.example.app.data.model.Folder
import org.example.app.data.model.Note
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * PUBLIC_INTERFACE
 * NotesRepository provides asynchronous CRUD operations for notes and folders.
 * Each read method accepts a callback invoked on the main thread.
 */
class NotesRepository private constructor(private val context: Context) {

    private val dbHelper = NotesDbHelper.getInstance(context)
    private val io: ExecutorService = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    // region Notes

    /**
     * PUBLIC_INTERFACE
     * Fetch all notes sorted by pinned desc, updatedAt desc.
     */
    fun getAllNotes(query: String?, callback: (List<Note>) -> Unit) {
        io.execute {
            val db = dbHelper.readableDatabase
            val selection = buildSelection(query = query)
            val selectionArgs = buildSelectionArgs(query = query)
            val cursor = db.query(
                Notes.TABLE,
                null,
                selection,
                selectionArgs,
                null,
                null,
                "${Notes.COL_PINNED} DESC, ${Notes.COL_UPDATED_AT} DESC"
            )
            val list = cursor.use { readNotes(it) }
            mainHandler.post { callback(list) }
        }
    }

    /**
     * PUBLIC_INTERFACE
     * Fetch favorite notes optionally filtered by query.
     */
    fun getFavoriteNotes(query: String?, callback: (List<Note>) -> Unit) {
        io.execute {
            val db = dbHelper.readableDatabase
            val (sel, args) = combineSelections(
                baseSel = "${Notes.COL_FAVORITE}=?",
                baseArgs = arrayOf("1"),
                query = query
            )
            val cursor = db.query(
                Notes.TABLE, null, sel, args, null, null,
                "${Notes.COL_PINNED} DESC, ${Notes.COL_UPDATED_AT} DESC"
            )
            val list = cursor.use { readNotes(it) }
            mainHandler.post { callback(list) }
        }
    }

    /**
     * PUBLIC_INTERFACE
     * Fetch notes in a specific folder optionally filtered by query.
     */
    fun getNotesByFolder(folderId: Long, query: String?, callback: (List<Note>) -> Unit) {
        io.execute {
            val db = dbHelper.readableDatabase
            val (sel, args) = combineSelections(
                baseSel = "${Notes.COL_FOLDER_ID}=?",
                baseArgs = arrayOf(folderId.toString()),
                query = query
            )
            val cursor = db.query(
                Notes.TABLE, null, sel, args, null, null,
                "${Notes.COL_PINNED} DESC, ${Notes.COL_UPDATED_AT} DESC"
            )
            val list = cursor.use { readNotes(it) }
            mainHandler.post { callback(list) }
        }
    }

    /**
     * PUBLIC_INTERFACE
     * Insert a note. Returns the new id in callback.
     */
    fun insertNote(
        title: String,
        content: String,
        folderId: Long?,
        pinned: Boolean,
        favorite: Boolean,
        callback: (Long) -> Unit
    ) {
        io.execute {
            val db = dbHelper.writableDatabase
            val now = System.currentTimeMillis()
            val values = ContentValues().apply {
                put(Notes.COL_TITLE, title)
                put(Notes.COL_CONTENT, content)
                put(Notes.COL_UPDATED_AT, now)
                put(Notes.COL_FOLDER_ID, folderId)
                put(Notes.COL_PINNED, if (pinned) 1 else 0)
                put(Notes.COL_FAVORITE, if (favorite) 1 else 0)
            }
            val id = db.insert(Notes.TABLE, null, values)
            mainHandler.post { callback(id) }
        }
    }

    /**
     * PUBLIC_INTERFACE
     * Update a note by id. Callback is invoked when the operation completes.
     */
    fun updateNote(
        id: Long,
        title: String,
        content: String,
        folderId: Long?,
        pinned: Boolean,
        favorite: Boolean,
        callback: () -> Unit
    ) {
        io.execute {
            val db = dbHelper.writableDatabase
            val now = System.currentTimeMillis()
            val values = ContentValues().apply {
                put(Notes.COL_TITLE, title)
                put(Notes.COL_CONTENT, content)
                put(Notes.COL_UPDATED_AT, now)
                put(Notes.COL_FOLDER_ID, folderId)
                put(Notes.COL_PINNED, if (pinned) 1 else 0)
                put(Notes.COL_FAVORITE, if (favorite) 1 else 0)
            }
            db.update(Notes.TABLE, values, "${Notes.COL_ID}=?", arrayOf(id.toString()))
            mainHandler.post { callback() }
        }
    }

    /**
     * PUBLIC_INTERFACE
     * Delete a note by id.
     */
    fun deleteNote(id: Long, callback: () -> Unit) {
        io.execute {
            val db = dbHelper.writableDatabase
            db.delete(Notes.TABLE, "${Notes.COL_ID}=?", arrayOf(id.toString()))
            mainHandler.post { callback() }
        }
    }

    /**
     * PUBLIC_INTERFACE
     * Toggle favorite flag.
     */
    fun setFavorite(id: Long, favorite: Boolean, callback: () -> Unit) {
        io.execute {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(Notes.COL_FAVORITE, if (favorite) 1 else 0)
                put(Notes.COL_UPDATED_AT, System.currentTimeMillis())
            }
            db.update(Notes.TABLE, values, "${Notes.COL_ID}=?", arrayOf(id.toString()))
            mainHandler.post { callback() }
        }
    }

    /**
     * PUBLIC_INTERFACE
     * Toggle pinned flag.
     */
    fun setPinned(id: Long, pinned: Boolean, callback: () -> Unit) {
        io.execute {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(Notes.COL_PINNED, if (pinned) 1 else 0)
                put(Notes.COL_UPDATED_AT, System.currentTimeMillis())
            }
            db.update(Notes.TABLE, values, "${Notes.COL_ID}=?", arrayOf(id.toString()))
            mainHandler.post { callback() }
        }
    }
    // endregion

    // region Folders

    /**
     * PUBLIC_INTERFACE
     * Fetch all folders.
     */
    fun getFolders(callback: (List<Folder>) -> Unit) {
        io.execute {
            val db = dbHelper.readableDatabase
            val cursor = db.query(Folders.TABLE, null, null, null, null, null, Folders.COL_NAME)
            val list = cursor.use { readFolders(it) }
            mainHandler.post { callback(list) }
        }
    }

    /**
     * PUBLIC_INTERFACE
     * Insert a folder by name. Returns id or -1 if conflict.
     */
    fun insertFolder(name: String, callback: (Long) -> Unit) {
        io.execute {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply { put(Folders.COL_NAME, name.trim()) }
            val id = db.insert(Folders.TABLE, null, values)
            mainHandler.post { callback(id) }
        }
    }

    /**
     * PUBLIC_INTERFACE
     * Delete a folder and unassign notes from it.
     */
    fun deleteFolder(id: Long, callback: () -> Unit) {
        io.execute {
            val db = dbHelper.writableDatabase
            // set folder null on notes
            val cv = ContentValues().apply { putNull(Notes.COL_FOLDER_ID) }
            db.update(Notes.TABLE, cv, "${Notes.COL_FOLDER_ID}=?", arrayOf(id.toString()))
            db.delete(Folders.TABLE, "${Folders.COL_ID}=?", arrayOf(id.toString()))
            mainHandler.post { callback() }
        }
    }

    // endregion

    private fun buildSelection(query: String? = null): String? {
        return if (query.isNullOrBlank()) null else
            "(${Notes.COL_TITLE} LIKE ? OR ${Notes.COL_CONTENT} LIKE ?)"
    }

    private fun buildSelectionArgs(query: String? = null): Array<String>? {
        return if (query.isNullOrBlank()) null else {
            val q = "%${query.trim()}%"
            arrayOf(q, q)
        }
    }

    private fun combineSelections(
        baseSel: String,
        baseArgs: Array<String>,
        query: String?
    ): Pair<String, Array<String>> {
        val qSel = buildSelection(query)
        val qArgs = buildSelectionArgs(query)
        return if (qSel == null) {
            Pair(baseSel, baseArgs)
        } else {
            Pair("($baseSel) AND ($qSel)", baseArgs + qArgs!!)
        }
    }

    private fun readNotes(cursor: Cursor): List<Note> {
        val list = mutableListOf<Note>()
        val idxId = cursor.getColumnIndexOrThrow(Notes.COL_ID)
        val idxTitle = cursor.getColumnIndexOrThrow(Notes.COL_TITLE)
        val idxContent = cursor.getColumnIndexOrThrow(Notes.COL_CONTENT)
        val idxUpdated = cursor.getColumnIndexOrThrow(Notes.COL_UPDATED_AT)
        val idxFolder = cursor.getColumnIndexOrThrow(Notes.COL_FOLDER_ID)
        val idxPinned = cursor.getColumnIndexOrThrow(Notes.COL_PINNED)
        val idxFavorite = cursor.getColumnIndexOrThrow(Notes.COL_FAVORITE)
        while (cursor.moveToNext()) {
            list.add(
                Note(
                    id = cursor.getLong(idxId),
                    title = cursor.getString(idxTitle) ?: "",
                    content = cursor.getString(idxContent) ?: "",
                    updatedAt = cursor.getLong(idxUpdated),
                    folderId = if (cursor.isNull(idxFolder)) null else cursor.getLong(idxFolder),
                    pinned = cursor.getInt(idxPinned) == 1,
                    favorite = cursor.getInt(idxFavorite) == 1,
                )
            )
        }
        return list
    }

    private fun readFolders(cursor: Cursor): List<Folder> {
        val list = mutableListOf<Folder>()
        val idxId = cursor.getColumnIndexOrThrow(Folders.COL_ID)
        val idxName = cursor.getColumnIndexOrThrow(Folders.COL_NAME)
        while (cursor.moveToNext()) {
            list.add(Folder(cursor.getLong(idxId), cursor.getString(idxName) ?: ""))
        }
        return list
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile private var INSTANCE: NotesRepository? = null

        /**
         * PUBLIC_INTERFACE
         * Get repository singleton.
         */
        fun getInstance(context: Context): NotesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotesRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

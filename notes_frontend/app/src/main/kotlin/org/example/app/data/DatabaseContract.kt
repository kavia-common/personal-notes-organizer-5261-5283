package org.example.app.data

/**
 * Defines database schema constants and SQL creation statements.
 */
object DatabaseContract {
    const val DB_NAME = "notes.db"
    const val DB_VERSION = 1

    object Folders {
        const val TABLE = "folders"
        const val COL_ID = "id"
        const val COL_NAME = "name"

        const val CREATE = """
            CREATE TABLE $TABLE (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT NOT NULL UNIQUE
            );
        """
    }

    object Notes {
        const val TABLE = "notes"
        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_CONTENT = "content"
        const val COL_UPDATED_AT = "updated_at"
        const val COL_FOLDER_ID = "folder_id"
        const val COL_PINNED = "pinned"
        const val COL_FAVORITE = "favorite"

        const val CREATE = """
            CREATE TABLE $TABLE (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT,
                $COL_CONTENT TEXT,
                $COL_UPDATED_AT INTEGER NOT NULL,
                $COL_FOLDER_ID INTEGER NULL,
                $COL_PINNED INTEGER NOT NULL DEFAULT 0,
                $COL_FAVORITE INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY($COL_FOLDER_ID) REFERENCES ${Folders.TABLE}(${Folders.COL_ID}) ON DELETE SET NULL
            );
        """

        const val INDEXES = """
            CREATE INDEX IF NOT EXISTS idx_notes_updated ON $TABLE($COL_UPDATED_AT DESC);
            CREATE INDEX IF NOT EXISTS idx_notes_pinned ON $TABLE($COL_PINNED);
            CREATE INDEX IF NOT EXISTS idx_notes_favorite ON $TABLE($COL_FAVORITE);
            CREATE INDEX IF NOT EXISTS idx_notes_folder ON $TABLE($COL_FOLDER_ID);
        """
    }
}

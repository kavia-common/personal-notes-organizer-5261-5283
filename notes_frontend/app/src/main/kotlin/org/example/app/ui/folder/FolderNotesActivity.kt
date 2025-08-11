package org.example.app.ui.folder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.example.app.R
import org.example.app.data.NotesRepository
import org.example.app.data.model.Note
import org.example.app.ui.editor.NoteEditorActivity
import org.example.app.ui.notes.NotesListAdapter

/**
 * PUBLIC_INTERFACE
 * Displays notes belonging to a given folder.
 *
 * Extras:
 * - EXTRA_FOLDER_ID (Long) required
 * - EXTRA_FOLDER_NAME (String) optional
 */
class FolderNotesActivity : AppCompatActivity(), NotesListAdapter.NoteClickListener {

    private lateinit var repository: NotesRepository
    private lateinit var list: RecyclerView
    private lateinit var empty: TextView
    private lateinit var progress: ProgressBar
    private lateinit var adapter: NotesListAdapter

    private var folderId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder_notes)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarFolder)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        folderId = intent.getLongExtra(EXTRA_FOLDER_ID, -1L)
        val folderName = intent.getStringExtra(EXTRA_FOLDER_NAME)
        supportActionBar?.title = folderName ?: getString(R.string.title_folder_notes)

        repository = NotesRepository.getInstance(this)

        list = findViewById(R.id.recyclerNotesInFolder)
        empty = findViewById(R.id.textEmptyFolderNotes)
        progress = findViewById(R.id.progressFolderNotes)

        list.layoutManager = LinearLayoutManager(this)
        adapter = NotesListAdapter(this, this)
        list.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        showLoading(true)
        repository.getNotesByFolder(folderId, query = null) { notes ->
            showLoading(false)
            adapter.submitList(notes)
            empty.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showLoading(show: Boolean) {
        progress.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onNoteClicked(note: Note) {
        startActivity(NoteEditorActivity.newIntent(this, note.id))
    }

    override fun onNoteMenuClicked(anchor: View, note: Note) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, 1, 0, if (note.pinned) getString(R.string.action_unpin) else getString(R.string.action_pin))
        popup.menu.add(0, 2, 1, if (note.favorite) getString(R.string.action_unfavorite) else getString(R.string.action_favorite))
        popup.menu.add(0, 3, 2, getString(R.string.action_delete))

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                1 -> repository.setPinned(note.id, !note.pinned) { loadData() }
                2 -> repository.setFavorite(note.id, !note.favorite) { loadData() }
                3 -> repository.deleteNote(note.id) { loadData() }
            }
            true
        }
        popup.show()
    }

    companion object {
        private const val EXTRA_FOLDER_ID = "extra_folder_id"
        private const val EXTRA_FOLDER_NAME = "extra_folder_name"

        /**
         * PUBLIC_INTERFACE
         * Build an Intent to open FolderNotesActivity for a given folder.
         */
        fun newIntent(context: Context, folderId: Long, folderName: String?): Intent {
            return Intent(context, FolderNotesActivity::class.java).apply {
                putExtra(EXTRA_FOLDER_ID, folderId)
                putExtra(EXTRA_FOLDER_NAME, folderName)
            }
        }
    }
}

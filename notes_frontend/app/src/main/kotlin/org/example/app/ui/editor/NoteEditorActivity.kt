package org.example.app.ui.editor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.app.AppCompatActivity
import org.example.app.R
import org.example.app.data.NotesRepository
import org.example.app.data.model.Folder
import org.example.app.data.model.Note

/**
 * PUBLIC_INTERFACE
 * Activity for creating or editing a note.
 *
 * Extras:
 * - EXTRA_NOTE_ID (Long?) optional; if present, activity loads note for editing
 */
class NoteEditorActivity : AppCompatActivity() {

    private lateinit var repository: NotesRepository

    private lateinit var titleEdit: EditText
    private lateinit var contentEdit: EditText
    private lateinit var folderSpinner: Spinner
    private lateinit var pinSwitch: SwitchCompat
    private lateinit var favoriteCheck: CheckBox

    private var noteId: Long? = null
    private var folders: List<Folder> = emptyList()
    private var selectedFolderId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_editor)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarEditor)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        repository = NotesRepository.getInstance(this)

        titleEdit = findViewById(R.id.editTitle)
        contentEdit = findViewById(R.id.editContent)
        folderSpinner = findViewById(R.id.spinnerFolder)
        pinSwitch = findViewById(R.id.switchPinned)
        favoriteCheck = findViewById(R.id.checkFavorite)

        noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1L).let { if (it <= 0) null else it }

        loadFolders {
            if (noteId != null) {
                // load existing note by querying repository getAllNotes then finding by id (for simplicity)
                repository.getAllNotes(query = null) { list ->
                    val note = list.find { it.id == noteId }
                    if (note != null) {
                        bindNote(note)
                    }
                }
            }
        }
    }

    private fun loadFolders(onDone: () -> Unit) {
        repository.getFolders { folderList ->
            folders = folderList
            val names = mutableListOf(getString(R.string.label_no_folder))
            names.addAll(folderList.map { it.name })
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            folderSpinner.adapter = adapter
            folderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    selectedFolderId = if (position == 0) null else folders[position - 1].id
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedFolderId = null
                }
            }
            onDone()
        }
    }

    private fun bindNote(note: Note) {
        title = getString(R.string.title_edit_note)
        titleEdit.setText(note.title)
        contentEdit.setText(note.content)
        pinSwitch.isChecked = note.pinned
        favoriteCheck.isChecked = note.favorite

        // set spinner selection
        val index = if (note.folderId == null) 0 else {
            val i = folders.indexOfFirst { it.id == note.folderId }
            if (i >= 0) i + 1 else 0
        }
        folderSpinner.setSelection(index)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_editor, menu)
        // show delete only when editing existing
        menu.findItem(R.id.action_delete).isVisible = noteId != null
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            R.id.action_save -> { onSave(); true }
            R.id.action_delete -> { onDelete(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onSave() {
        val title = titleEdit.text.toString().trim()
        val content = contentEdit.text.toString().trim()
        val pinned = pinSwitch.isChecked
        val favorite = favoriteCheck.isChecked

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, R.string.msg_empty_note_warning, Toast.LENGTH_SHORT).show()
            return
        }

        if (noteId == null) {
            repository.insertNote(title, content, selectedFolderId, pinned, favorite) {
                Toast.makeText(this, R.string.msg_saved, Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            repository.updateNote(noteId!!, title, content, selectedFolderId, pinned, favorite) {
                Toast.makeText(this, R.string.msg_saved, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun onDelete() {
        val id = noteId ?: return
        repository.deleteNote(id) {
            Toast.makeText(this, R.string.msg_deleted, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    companion object {
        private const val EXTRA_NOTE_ID = "extra_note_id"

        /**
         * PUBLIC_INTERFACE
         * Build an Intent to open NoteEditorActivity.
         * @param context Context for building the intent
         * @param noteId Optional existing note id to edit; null to create new.
         */
        fun newIntent(context: Context, noteId: Long?): Intent {
            return Intent(context, NoteEditorActivity::class.java).apply {
                if (noteId != null) putExtra(EXTRA_NOTE_ID, noteId)
            }
        }
    }
}

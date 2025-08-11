package org.example.app.ui.favorites

import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.example.app.R
import org.example.app.data.NotesRepository
import org.example.app.data.model.Note
import org.example.app.ui.Searchable
import org.example.app.ui.editor.NoteEditorActivity
import org.example.app.ui.notes.NotesListAdapter

/**
 * Shows favorite notes. Supports search from toolbar.
 */
class FavoritesFragment : Fragment(), Searchable, NotesListAdapter.NoteClickListener {

    private lateinit var repository: NotesRepository
    private lateinit var list: RecyclerView
    private lateinit var empty: TextView
    private lateinit var progress: ProgressBar
    private lateinit var adapter: NotesListAdapter

    private var currentQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = NotesRepository.getInstance(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_notes_list, container, false)
        list = root.findViewById(R.id.recyclerNotes)
        empty = root.findViewById(R.id.textEmpty)
        progress = root.findViewById(R.id.progress)
        list.layoutManager = LinearLayoutManager(requireContext())
        adapter = NotesListAdapter(requireContext(), listener = this)
        list.adapter = adapter
        empty.setText(R.string.empty_favorites)
        return root
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onSearchQuery(query: String) {
        currentQuery = query
        loadData()
    }

    private fun loadData() {
        showLoading(true)
        repository.getFavoriteNotes(currentQuery) { notes ->
            showLoading(false)
            adapter.submitList(notes)
            empty.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showLoading(show: Boolean) {
        progress.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onNoteClicked(note: Note) {
        startActivity(NoteEditorActivity.newIntent(requireContext(), note.id))
    }

    override fun onNoteMenuClicked(anchor: View, note: Note) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menu.add(0, 1, 0, if (note.pinned) getString(R.string.action_unpin) else getString(R.string.action_pin))
        popup.menu.add(0, 2, 1, if (note.favorite) getString(R.string.action_unfavorite) else getString(R.string.action_favorite))
        popup.menu.add(0, 3, 2, getString(R.string.action_delete))

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                1 -> repository.setPinned(note.id, !note.pinned) { loadData() }
                2 -> repository.setFavorite(note.id, !note.favorite) { loadData() }
                3 -> confirmDelete(note)
            }
            true
        }
        popup.show()
    }

    private fun confirmDelete(note: Note) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_delete_note)
            .setMessage(R.string.msg_delete_note_confirm)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                repository.deleteNote(note.id) { loadData() }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object {
        fun newInstance(): FavoritesFragment = FavoritesFragment()
    }
}

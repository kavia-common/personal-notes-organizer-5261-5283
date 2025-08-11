package org.example.app.ui.folders

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.example.app.R
import org.example.app.data.NotesRepository
import org.example.app.data.model.Folder
import org.example.app.ui.folder.FolderNotesActivity

/**
 * Shows folders and allows adding/removing them.
 */
class FoldersFragment : Fragment(), FoldersListAdapter.FolderClickListener {

    private lateinit var repository: NotesRepository
    private lateinit var list: RecyclerView
    private lateinit var empty: TextView
    private lateinit var progress: ProgressBar
    private lateinit var addBtn: ImageButton

    private lateinit var adapter: FoldersListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = NotesRepository.getInstance(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_folders, container, false)
        list = root.findViewById(R.id.recyclerFolders)
        empty = root.findViewById(R.id.textEmptyFolders)
        progress = root.findViewById(R.id.progressFolders)
        addBtn = root.findViewById(R.id.buttonAddFolder)

        list.layoutManager = LinearLayoutManager(requireContext())
        adapter = FoldersListAdapter(listener = this)
        list.adapter = adapter

        addBtn.setOnClickListener { showAddFolderDialog() }
        return root
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        showLoading(true)
        repository.getFolders { folders ->
            showLoading(false)
            adapter.submitList(folders)
            empty.visibility = if (folders.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showLoading(show: Boolean) {
        progress.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showAddFolderDialog() {
        val input = EditText(requireContext())
        input.hint = getString(R.string.hint_folder_name)
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_new_folder)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    repository.insertFolder(name) { loadData() }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onFolderClicked(folder: Folder) {
        startActivity(FolderNotesActivity.newIntent(requireContext(), folder.id, folder.name))
    }

    override fun onDeleteFolder(folder: Folder) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_delete_folder)
            .setMessage(R.string.msg_delete_folder_confirm)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                repository.deleteFolder(folder.id) { loadData() }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object {
        fun newInstance(): FoldersFragment = FoldersFragment()
    }
}

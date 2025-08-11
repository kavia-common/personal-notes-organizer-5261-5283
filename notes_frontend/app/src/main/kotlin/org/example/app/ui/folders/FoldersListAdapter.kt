package org.example.app.ui.folders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.example.app.R
import org.example.app.data.model.Folder

/**
 * Adapter for folders list.
 */
class FoldersListAdapter(
    private val listener: FolderClickListener
) : RecyclerView.Adapter<FoldersListAdapter.VH>() {

    interface FolderClickListener {
        /**
         * PUBLIC_INTERFACE
         * Called when a folder is tapped.
         */
        fun onFolderClicked(folder: Folder)

        /**
         * PUBLIC_INTERFACE
         * Called when delete button is tapped for a folder.
         */
        fun onDeleteFolder(folder: Folder)
    }

    private val data = mutableListOf<Folder>()

    fun submitList(newList: List<Folder>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = data.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                data[oldItemPosition].id == newList[newItemPosition].id
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                data[oldItemPosition] == newList[newItemPosition]
        })
        data.clear()
        data.addAll(newList)
        diff.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_folder, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val folder = data[position]
        holder.bind(folder)
        holder.itemView.setOnClickListener { listener.onFolderClicked(folder) }
        holder.deleteBtn.setOnClickListener { listener.onDeleteFolder(folder) }
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.textFolderName)
        val deleteBtn: ImageButton = itemView.findViewById(R.id.buttonDeleteFolder)

        fun bind(f: Folder) {
            name.text = f.name
        }
    }
}

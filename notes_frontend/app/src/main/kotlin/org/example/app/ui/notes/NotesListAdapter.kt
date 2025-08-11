package org.example.app.ui.notes

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.example.app.R
import org.example.app.data.model.Note

/**
 * Adapter to show a list of notes with title, snippet, date, and indicators.
 */
class NotesListAdapter(
    private val context: Context,
    private val listener: NoteClickListener
) : RecyclerView.Adapter<NotesListAdapter.VH>() {

    private val data = mutableListOf<Note>()

    interface NoteClickListener {
        /**
         * PUBLIC_INTERFACE
         * Called when a note row is tapped.
         */
        fun onNoteClicked(note: Note)

        /**
         * PUBLIC_INTERFACE
         * Called when the overflow/menu button in a note row is tapped.
         */
        fun onNoteMenuClicked(anchor: View, note: Note)
    }

    fun submitList(newList: List<Note>) {
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
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val note = data[position]
        holder.bind(note)
        holder.itemView.setOnClickListener { listener.onNoteClicked(note) }
        holder.menuBtn.setOnClickListener { listener.onNoteMenuClicked(it, note) }
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.textTitle)
        private val snippet: TextView = itemView.findViewById(R.id.textSnippet)
        private val date: TextView = itemView.findViewById(R.id.textDate)
        private val pin: ImageView = itemView.findViewById(R.id.iconPin)
        private val fav: ImageView = itemView.findViewById(R.id.iconFavorite)
        val menuBtn: ImageButton = itemView.findViewById(R.id.buttonMenu)

        fun bind(n: Note) {
            title.text = if (n.title.isNotBlank()) n.title else context.getString(R.string.label_untitled)
            snippet.text = n.content.replace("\n", " ").let { it.take(120) + if (it.length > 120) "…" else "" }
            date.text = DateUtils.getRelativeTimeSpanString(n.updatedAt).toString()
            pin.visibility = if (n.pinned) View.VISIBLE else View.GONE
            fav.visibility = if (n.favorite) View.VISIBLE else View.GONE
        }
    }
}

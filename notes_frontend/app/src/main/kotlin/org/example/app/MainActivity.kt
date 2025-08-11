package org.example.app

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.example.app.ui.favorites.FavoritesFragment
import org.example.app.ui.folders.FoldersFragment
import org.example.app.ui.notes.NotesFragment
import org.example.app.ui.Searchable
import org.example.app.ui.editor.NoteEditorActivity

/**
 * PUBLIC_INTERFACE
 * MainActivity is the app's entry point.
 * It hosts the primary navigation using a BottomNavigationView and exposes a toolbar with search and theme toggle.
 *
 * Parameters: none
 * Returns: none
 */
class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fab: FloatingActionButton

    private val PREFS by lazy { getSharedPreferences("settings", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved theme before super.onCreate
        val isDark = PREFS.getBoolean("theme_dark", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        bottomNav = findViewById(R.id.bottomNavigation)
        fab = findViewById(R.id.fabAdd)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, NotesFragment.newInstance())
                .commit()
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_notes -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, NotesFragment.newInstance())
                        .commit()
                    true
                }
                R.id.menu_folders -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, FoldersFragment.newInstance())
                        .commit()
                    true
                }
                R.id.menu_favorites -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, FavoritesFragment.newInstance())
                        .commit()
                    true
                }
                else -> false
            }
        }

        fab.setOnClickListener {
            startActivity(NoteEditorActivity.newIntent(this, noteId = null))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.queryHint = getString(R.string.hint_search_notes)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                dispatchSearch(newText.orEmpty())
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                dispatchSearch(query.orEmpty())
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_theme -> {
                val current = PREFS.getBoolean("theme_dark", false)
                val newVal = !current
                PREFS.edit().putBoolean("theme_dark", newVal).apply()
                AppCompatDelegate.setDefaultNightMode(
                    if (newVal) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun dispatchSearch(query: String) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (currentFragment is Searchable) {
            currentFragment.onSearchQuery(query)
        }
    }
}

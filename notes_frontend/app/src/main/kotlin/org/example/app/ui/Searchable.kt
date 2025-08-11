package org.example.app.ui

/**
 * PUBLIC_INTERFACE
 * Implement this interface for a UI element (typically Fragment) that wants to receive search inputs.
 */
interface Searchable {
    /**
     * PUBLIC_INTERFACE
     * Called when the user updates the search query in the toolbar.
     * @param query The current search text.
     */
    fun onSearchQuery(query: String)
}

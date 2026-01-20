package com.example.mvi.mvidemo

data class HomeState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val selectedTab: HomeTab = HomeTab.ALL,
    val items: List<HomeItem> = emptyList(),
    val filteredItems: List<HomeItem> = emptyList(),
    val error: String? = null
) {
    val showEmptyState: Boolean
        get() = !isLoading && filteredItems.isEmpty() && error == null
    
    val showError: Boolean
        get() = error != null && !isLoading
}

data class HomeItem(
    val id: String,
    val title: String,
    val description: String,
    val isFavorite: Boolean,
    val createdAt: Long
)
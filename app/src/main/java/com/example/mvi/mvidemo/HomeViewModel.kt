package com.example.mvi.mvidemo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.mvi.mvibase.BaseViewModel

class HomeViewModel : BaseViewModel<HomeIntent, HomeState, HomeEvent>() {
    
    private val homeRepository = HomeRepository()
    
    override fun initialState(): HomeState = HomeState()
    
    override suspend fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadData -> loadData()
            is HomeIntent.RefreshData -> refreshData()
            is HomeIntent.UpdateSearchQuery -> updateSearchQuery(intent.query)
            is HomeIntent.ChangeTab -> changeTab(intent.tab)
            is HomeIntent.ItemClick -> handleItemClick(intent.itemId)
            is HomeIntent.ToggleFavorite -> toggleFavorite(intent.itemId)
        }
    }
    
    private suspend fun loadData() {
        updateState { it.copy(isLoading = true, error = null) }
        
        try {
            val items = homeRepository.loadHomeData()
            updateState { current ->
                current.copy(
                    isLoading = false,
                    items = items,
                    filteredItems = filterItems(items, current.searchQuery, current.selectedTab)
                )
            }
        } catch (e: Exception) {
            updateState { it.copy(isLoading = false, error = "加载失败: ${e.message}") }
            sendEvent(HomeEvent.ShowErrorDialog("数据加载失败，请重试"))
        }
    }
    
    private suspend fun refreshData() {
        updateState { it.copy(isRefreshing = true) }
        
        try {
            val items = homeRepository.refreshData()
            updateState { current ->
                current.copy(
                    isRefreshing = false,
                    items = items,
                    filteredItems = filterItems(items, current.searchQuery, current.selectedTab)
                )
            }
            sendEvent(HomeEvent.RefreshComplete)
            sendEvent(HomeEvent.ShowToast("刷新成功"))
        } catch (e: Exception) {
            updateState { it.copy(isRefreshing = false) }
            sendEvent(HomeEvent.ShowToast("刷新失败: ${e.message}"))
        }
    }
    
    private fun updateSearchQuery(query: String) {
        updateState { current ->
            val filteredItems = filterItems(current.items, query, current.selectedTab)
            current.copy(searchQuery = query, filteredItems = filteredItems)
        }
    }
    
    private fun changeTab(tab: HomeTab) {
        updateState { current ->
            val filteredItems = filterItems(current.items, current.searchQuery, tab)
            current.copy(selectedTab = tab, filteredItems = filteredItems)
        }
    }
    
    private suspend fun handleItemClick(itemId: String) {
        sendEvent(HomeEvent.NavigateToDetail(itemId))
    }
    
    private suspend fun toggleFavorite(itemId: String) {
        try {
            val success = homeRepository.toggleFavorite(itemId)
            if (success) {
                // 重新加载数据以更新状态
                val items = homeRepository.loadHomeData()
                updateState { current ->
                    current.copy(
                        items = items,
                        filteredItems = filterItems(items, current.searchQuery, current.selectedTab)
                    )
                }
                sendEvent(HomeEvent.ShowToast("收藏状态已更新"))
            }
        } catch (e: Exception) {
            sendEvent(HomeEvent.ShowToast("操作失败: ${e.message}"))
        }
    }
    
    private fun filterItems(
        items: List<HomeItem>,
        query: String,
        tab: HomeTab
    ): List<HomeItem> {
        return items
            .filter { item ->
                // 根据选项卡过滤
                when (tab) {
                    HomeTab.ALL -> true
                    HomeTab.FAVORITES -> item.isFavorite
                    HomeTab.RECENT -> item.createdAt > System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
                }
            }
            .filter { item ->
                // 根据搜索词过滤
                query.isEmpty() || 
                item.title.contains(query, ignoreCase = true) ||
                item.description.contains(query, ignoreCase = true)
            }
    }
}

// 模拟数据仓库
class HomeRepository {
    suspend fun loadHomeData(): List<HomeItem> {
        delay(1000) // 模拟网络请求
        return listOf(
            HomeItem("1", "项目1", "描述1", true, System.currentTimeMillis()),
            HomeItem("2", "项目2", "描述2", false, System.currentTimeMillis() - 1000),
            HomeItem("3", "项目3", "描述3", true, System.currentTimeMillis() - 2000)
        )
    }
    
    suspend fun refreshData(): List<HomeItem> {
        delay(500) // 模拟网络请求
        return loadHomeData()
    }
    
    suspend fun toggleFavorite(itemId: String): Boolean {
        delay(300) // 模拟网络请求
        return true
    }
}
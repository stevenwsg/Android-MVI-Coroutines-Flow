package com.example.mvi.mvidemo

import com.example.mvi.mvibase.BaseIntent

sealed class HomeIntent : BaseIntent {
    // 加载数据
    data object LoadData : HomeIntent()
    
    // 刷新数据
    data object RefreshData : HomeIntent()
    
    // 更新搜索关键词
    data class UpdateSearchQuery(val query: String) : HomeIntent()
    
    // 切换选项卡
    data class ChangeTab(val tab: HomeTab) : HomeIntent()
    
    // 点击项目
    data class ItemClick(val itemId: String) : HomeIntent()
    
    // 收藏项目
    data class ToggleFavorite(val itemId: String) : HomeIntent()
}

enum class HomeTab { ALL, FAVORITES, RECENT }
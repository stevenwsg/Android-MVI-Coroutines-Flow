package com.example.mvi.mvidemo

import com.example.mvi.mvibase.BaseEvent

sealed class HomeEvent : BaseEvent {
    // 显示 Toast 消息
    data class ShowToast(val message: String) : HomeEvent()
    
    // 导航到详情页
    data class NavigateToDetail(val itemId: String) : HomeEvent()
    
    // 显示错误对话框
    data class ShowErrorDialog(val message: String) : HomeEvent()
    
    // 显示确认对话框
    data class ShowConfirmationDialog(
        val title: String,
        val message: String,
        val onConfirm: () -> Unit
    ) : HomeEvent()
    
    // 完成刷新
    data object RefreshComplete : HomeEvent()
}
package com.example.mvi.mvidemo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mvi.mvibase.BaseActivity

class HomeActivity : BaseActivity<HomeIntent, HomeState, HomeEvent, HomeViewModel>() {
    
    override val viewModel: HomeViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 这里需要设置你的布局文件
        // binding = ActivityHomeBinding.inflate(layoutInflater)
        // setContentView(binding.root)
        
        setupViews()
        setupListeners()
        
        // 初始加载数据
        dispatchIntent(HomeIntent.LoadData)
    }
    
    private fun setupViews() {
        // 初始化UI组件
        // binding.swipeRefresh.setOnRefreshListener {
        //     dispatchIntent(HomeIntent.RefreshData)
        // }
        
        // binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        //     override fun onQueryTextSubmit(query: String): Boolean {
        //         dispatchIntent(HomeIntent.UpdateSearchQuery(query))
        //         return true
        //     }
        //     
        //     override fun onQueryTextChange(newText: String): Boolean {
        //         dispatchIntent(HomeIntent.UpdateSearchQuery(newText))
        //         return true
        //     }
        // })
        
        // binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        //     override fun onTabSelected(tab: TabLayout.Tab) {
        //         val homeTab = when (tab.position) {
        //             0 -> HomeTab.ALL
        //             1 -> HomeTab.FAVORITES
        //             2 -> HomeTab.RECENT
        //             else -> HomeTab.ALL
        //         }
        //         dispatchIntent(HomeIntent.ChangeTab(homeTab))
        //     }
        //     
        //     override fun onTabUnselected(tab: TabLayout.Tab) {}
        //     override fun onTabReselected(tab: TabLayout.Tab) {}
        // })
    }
    
    private fun setupListeners() {
        // binding.fab.setOnClickListener {
        //     // 处理FAB点击
        // }
    }
    
    override fun render(state: HomeState) {
        // 更新UI基于状态
        println("当前状态: $state")
        
        // 实际项目中应该更新真实的UI组件
        // binding.progressBar.isVisible = state.isLoading
        // binding.swipeRefresh.isRefreshing = state.isRefreshing
        // binding.emptyState.isVisible = state.showEmptyState
        // binding.errorState.isVisible = state.showError
        
        if (state.error != null) {
            // binding.errorText.text = state.error
            println("错误信息: ${state.error}")
        }
        
        // 更新列表数据
        println("过滤后的项目数量: ${state.filteredItems.size}")
        state.filteredItems.forEach { item ->
            println("项目: ${item.title} - ${item.description}")
        }
        
        // 更新选项卡选中状态
        // binding.tabLayout.getTabAt(
        //     when (state.selectedTab) {
        //         HomeTab.ALL -> 0
        //         HomeTab.FAVORITES -> 1
        //         HomeTab.RECENT -> 2
        //     }
        // )?.select()
    }
    
    override fun handleEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.ShowToast -> {
                Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
                println("Toast消息: ${event.message}")
            }
            
            is HomeEvent.NavigateToDetail -> {
                // 导航到详情页
                println("导航到详情页: ${event.itemId}")
                // startActivity(DetailActivity.createIntent(this, event.itemId))
            }
            
            is HomeEvent.ShowErrorDialog -> {
                AlertDialog.Builder(this)
                    .setTitle("错误")
                    .setMessage(event.message)
                    .setPositiveButton("确定", null)
                    .show()
            }
            
            is HomeEvent.ShowConfirmationDialog -> {
                AlertDialog.Builder(this)
                    .setTitle(event.title)
                    .setMessage(event.message)
                    .setPositiveButton("确认") { _, _ -> event.onConfirm() }
                    .setNegativeButton("取消", null)
                    .show()
            }
            
            is HomeEvent.RefreshComplete -> {
                // binding.swipeRefresh.isRefreshing = false
                println("刷新完成")
            }
        }
    }
}
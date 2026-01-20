# MVI 架构使用指南

## 🚀 快速开始

### 1. 添加依赖
确保在 `app/build.gradle.kts` 中包含必要的依赖：

```kotlin
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.ktx)  // 必需：提供 viewModels() 委托
    implementation(libs.androidx.constraintlayout)
    
    // MVI 架构核心依赖
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.kotlinx.coroutines.android)
}
```

### 2. 基础项目结构
```
src/main/java/
└── com/example/mvi/
    ├── mvibase/  # 基础架构组件
    │   ├── BaseIntent.kt
    │   ├── BaseEvent.kt
    │   ├── BaseViewModel.kt
    │   ├── BaseActivity.kt
    │   ├── MVIUtils.kt
    │   ├── DESIGN.md
    │   ├── IMPLEMENTATION.md
    │   └── USAGE.md
    ├── mvidemo/  # 示例实现
    │   ├── HomeIntent.kt
    │   ├── HomeState.kt
    │   ├── HomeEvent.kt
    │   ├── HomeViewModel.kt
    │   └── HomeActivity.kt
    └── MainActivity.kt
```

## 📋 完整使用示例

### 1. 定义 Intent（用户操作）

**文件**: `HomeIntent.kt`
```kotlin
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
```

### 2. 定义 State（UI状态）

**文件**: `HomeState.kt`
```kotlin
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
```

### 3. 定义 Event（一次性事件）

**文件**: `HomeEvent.kt`
```kotlin
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
```

### 4. 实现 ViewModel

**文件**: `HomeViewModel.kt`
```kotlin
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
    
    private fun handleItemClick(itemId: String) {
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
```

### 5. 实现 Activity

**文件**: `HomeActivity.kt`
```kotlin
package com.example.mvi.mvidemo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mvi.mvibase.BaseActivity

class HomeActivity : BaseActivity<HomeIntent, HomeState, HomeEvent, HomeViewModel>() {
    
    private lateinit var binding: ActivityHomeBinding
    override val viewModel: HomeViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViews()
        setupListeners()
        
        // 初始加载数据
        dispatchIntent(HomeIntent.LoadData)
    }
    
    private fun setupViews() {
        // 初始化UI组件
        binding.swipeRefresh.setOnRefreshListener {
            dispatchIntent(HomeIntent.RefreshData)
        }
        
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                dispatchIntent(HomeIntent.UpdateSearchQuery(query))
                return true
            }
            
            override fun onQueryTextChange(newText: String): Boolean {
                dispatchIntent(HomeIntent.UpdateSearchQuery(newText))
                return true
            }
        })
        
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val homeTab = when (tab.position) {
                    0 -> HomeTab.ALL
                    1 -> HomeTab.FAVORITES
                    2 -> HomeTab.RECENT
                    else -> HomeTab.ALL
                }
                dispatchIntent(HomeIntent.ChangeTab(homeTab))
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
    
    private fun setupListeners() {
        binding.fab.setOnClickListener {
            // 处理FAB点击
        }
    }
    
    override fun render(state: HomeState) {
        // 更新UI基于状态
        binding.progressBar.isVisible = state.isLoading
        binding.swipeRefresh.isRefreshing = state.isRefreshing
        binding.emptyState.isVisible = state.showEmptyState
        binding.errorState.isVisible = state.showError
        
        if (state.error != null) {
            binding.errorText.text = state.error
        }
        
        // 更新列表数据
        val adapter = HomeAdapter(state.filteredItems) { item ->
            dispatchIntent(HomeIntent.ItemClick(item.id))
        }
        binding.recyclerView.adapter = adapter
        
        // 更新选项卡选中状态
        binding.tabLayout.getTabAt(
            when (state.selectedTab) {
                HomeTab.ALL -> 0
                HomeTab.FAVORITES -> 1
                HomeTab.RECENT -> 2
            }
        )?.select()
    }
    
    override fun handleEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.ShowToast -> {
                Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
            }
            
            is HomeEvent.NavigateToDetail -> {
                // 导航到详情页
                startActivity(DetailActivity.createIntent(this, event.itemId))
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
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
}
```

### 6. 实现列表适配器

**文件**: `HomeAdapter.kt`
```kotlin
package com.example.app.feature.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.app.databinding.ItemHomeBinding

class HomeAdapter(
    private val items: List<HomeItem>,
    private val onItemClick: (HomeItem) -> Unit
) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {
    
    inner class ViewHolder(val binding: ItemHomeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HomeItem) {
            binding.title.text = item.title
            binding.description.text = item.description
            binding.favoriteIcon.isSelected = item.isFavorite
            
            binding.root.setOnClickListener { onItemClick(item) }
            binding.favoriteIcon.setOnClickListener {
                // 这里可以直接发送Intent，或者通过回调处理
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
    
    override fun getItemCount(): Int = items.size
}
```

## 🎯 最佳实践

### 1. Intent 设计原则
- **单一职责**: 每个Intent只做一件事
- **不可变性**: 使用data class确保不可变
- **描述性**: 命名清晰表达用户操作

### 2. State 设计原则
- **不可变性**: 所有状态都是不可变的
- **完整性**: 包含UI需要的所有信息
- **计算属性**: 使用派生属性减少冗余

### 3. Event 设计原则
- **一次性**: 事件只处理一次
- **轻量级**: 事件处理应该快速
- **副作用**: 只用于处理UI副作用

### 4. ViewModel 设计原则
- **业务逻辑**: 包含所有业务逻辑
- **异步处理**: 使用协程处理异步操作
- **错误处理**: 统一的错误处理机制

## 🔧 高级用法

### 1. 状态持久化
```kotlin
class PersistentViewModel : BaseViewModel<MyIntent, MyState, MyEvent>() {
    
    private val statePersister = StatePersister()
    
    override fun initialState(): MyState {
        return statePersister.load() ?: MyState()
    }
    
    override suspend fun handleIntent(intent: MyIntent) {
        when (intent) {
            // 处理意图
        }
        // 保存状态
        statePersister.save(state.value)
    }
}
```

### 2. 中间件
```kotlin
class AnalyticsMiddleware : Middleware<BaseIntent, Any, BaseEvent> {
    override suspend fun process(intent: BaseIntent, state: Any, next: (BaseIntent) -> Unit) {
        // 记录分析事件
        Analytics.trackIntent(intent)
        next(intent)
    }
}
```

### 3. 测试
```kotlin
class HomeViewModelTest {
    
    @Test
    fun `load data should update state correctly`() = runTest {
        val viewModel = HomeViewModel()
        
        viewModel.processIntent(HomeIntent.LoadData)
        
        // 验证状态变化
        assertTrue(viewModel.state.value.isLoading)
        
        // 等待异步操作完成
        advanceUntilIdle()
        
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(3, viewModel.state.value.items.size)
    }
}
```

这个使用指南提供了完整的MVI架构实现示例和最佳实践，帮助你快速上手和使用这个架构。
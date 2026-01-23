# Android MVI Architecture Template 🚀

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-14-green.svg)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)
[![MVI](https://img.shields.io/badge/Architecture-MVI-ff69b4.svg)](https://developer.android.com/jetpack/guide)

一个完整的、生产就绪的Android MVI（Model-View-Intent）架构模板项目，使用Kotlin、Flow、StateFlow和SharedFlow实现。

## ✨ 特性

- 🏗️ **完整的MVI架构** - 基于Intent、State、Event的完整数据流
- ⚡ **Kotlin Flow驱动** - 使用StateFlow管理状态，SharedFlow处理事件
- 🎯 **类型安全** - 完整的Kotlin密封类和泛型支持
- 📚 **详细文档** - 包含设计思路、实现细节和使用指南
- 🧪 **可运行示例** - 包含完整的示例代码，可直接运行学习
- 🔧 **易于扩展** - 模块化设计，易于添加新功能
- 🛡️ **线程安全** - 使用ViewModelScope和协程确保线程安全

## 🎯 设计理念

### 核心设计原则
- **单向数据流** - 确保数据流动的单一方向性，避免双向绑定带来的复杂性
- **不可变状态** - 所有状态都是不可变的，确保状态变化的可预测性  
- **响应式编程** - 基于Kotlin Flow实现响应式数据流
- **类型安全** - 使用泛型确保编译时类型检查
- **生命周期感知** - 自动处理Android生命周期

### 为什么选择MVI？
- ✅ **更严格的数据流控制** - 明确的Intent→State→View数据流向
- ✅ **更好的状态管理** - 单一可信数据源，状态变化可追溯
- ✅ **更清晰的副作用处理** - 使用Event明确处理一次性操作
- ✅ **更适合复杂业务场景** - 大规模应用的状态管理更可靠

### 技术栈决策
- **Kotlin Flow** - 替代RxJava，更轻量级，与Kotlin协程完美集成
- **StateFlow** - 用于状态管理，提供最新的状态值
- **SharedFlow** - 用于事件处理，支持多个订阅者
- **ViewModel** - 配合Android Jetpack生命周期管理

### 架构优势
- 🚀 **性能优化** - 差异更新、批量更新、防抖处理
- 🛡️ **异常处理** - 统一的错误状态管理和恢复机制
- 📈 **扩展性强** - 模块化设计，支持大规模应用
- 🧪 **测试友好** - 所有状态和意图都可测试

## 📦 项目结构

```
src/main/java/com/example/mvi/
├── mvibase/           # MVI基础架构组件
│   ├── BaseIntent.kt # Intent基类
│   ├── BaseEvent.kt  # Event基类
│   ├── BaseViewModel.kt # ViewModel基类
│   ├── BaseActivity.kt # Activity基类
│   ├── BaseFragment.kt # Fragment基类
│   ├── MVIUtils.kt   # 工具类和扩展函数
│   ├── DESIGN.md     # 架构设计文档
│   ├── IMPLEMENTATION.md # 实现细节文档
│   └── USAGE.md      # 使用指南文档
├── mvidemo/          # MVI示例实现
│   ├── HomeIntent.kt # 首页用户操作
│   ├── HomeState.kt  # 首页UI状态
│   ├── HomeEvent.kt  # 首页一次性事件
│   ├── HomeViewModel.kt # 首页业务逻辑
│   ├── HomeActivity.kt # 首页Activity实现
│   └── HomeFragment.kt # 首页Fragment实现（可选）
└── MainActivity.kt   # 应用入口
```

## 🚀 快速开始

###  prerequisites

- Android Studio Flamingo 或更高版本
- JDK 11+
- Android SDK 24+

### 安装

1. 克隆项目
```bash
git clone https://github.com/stevenwsg/Android-MVI-Coroutines-Flow.git
```

2. 使用Android Studio打开项目
3. 同步Gradle依赖
4. 运行应用到设备或模拟器

## 🎯 MVI架构核心概念

### Intent (用户意图)
表示用户的交互操作，如点击、输入等

```kotlin
sealed class HomeIntent : BaseIntent {
    data object LoadData : HomeIntent()
    data object RefreshData : HomeIntent()
    data class UpdateSearchQuery(val query: String) : HomeIntent()
    data class ChangeTab(val tab: HomeTab) : HomeIntent()
    data class ItemClick(val itemId: String) : HomeIntent()
    data class ToggleFavorite(val itemId: String) : HomeIntent()
}
```

### State (UI状态)
表示UI的当前状态，必须是不可变的数据类

```kotlin
data class HomeState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val selectedTab: HomeTab = HomeTab.ALL,
    val items: List<HomeItem> = emptyList(),
    val filteredItems: List<HomeItem> = emptyList(),
    val error: String? = null
) {
    val showEmptyState: Boolean get() = !isLoading && filteredItems.isEmpty() && error == null
    val showError: Boolean get() = error != null && !isLoading
}
```

### Event (一次性事件)
表示只需要处理一次的事件，如导航、显示Toast等

```kotlin
sealed class HomeEvent : BaseEvent {
    data class ShowToast(val message: String) : HomeEvent()
    data class NavigateToDetail(val itemId: String) : HomeEvent()
    data class ShowErrorDialog(val message: String) : HomeEvent()
    data class ShowConfirmationDialog(val title: String, val message: String, val onConfirm: () -> Unit) : HomeEvent()
    data object RefreshComplete : HomeEvent()
}
```

## 📖 使用方法

### 1. 定义你的Intent、State、Event

参考 `mvidemo` 包中的示例，创建你的业务逻辑组件。详细设计原则请参考 [DESIGN.md](app/src/main/java/com/example/mvi/mvibase/DESIGN.md)。

### 2. 实现ViewModel

继承 `BaseViewModel` 并实现业务逻辑，技术细节参考 [IMPLEMENTATION.md](app/src/main/java/com/example/mvi/mvibase/IMPLEMENTATION.md)：

```kotlin
class MyViewModel : BaseViewModel<MyIntent, MyState, MyEvent>() {
    override fun initialState(): MyState = MyState()
    
    override suspend fun handleIntent(intent: MyIntent) {
        when (intent) {
            is MyIntent.LoadData -> loadData()
            is MyIntent.RefreshData -> refreshData()
            // 处理其他Intent
        }
    }
    
    private suspend fun loadData() {
        updateState { it.copy(isLoading = true) }
        // 业务逻辑...
        updateState { it.copy(isLoading = false, items = data) }
    }
}
```

### 3. 实现Activity或Fragment

#### Activity实现（继承 `BaseActivity`）：
```kotlin
class MyActivity : BaseActivity<MyIntent, MyState, MyEvent, MyViewModel>() {
    override val viewModel: MyViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化UI
        dispatchIntent(MyIntent.LoadData)
    }
    
    override fun render(state: MyState) {
        // 根据状态更新UI
        binding.progressBar.isVisible = state.isLoading
        binding.recyclerView.adapter = MyAdapter(state.items)
    }
    
    override fun handleEvent(event: MyEvent) {
        when (event) {
            is MyEvent.ShowToast -> Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
            is MyEvent.NavigateToDetail -> startActivity(DetailActivity.createIntent(this, event.itemId))
        }
    }
}
```

#### Fragment实现（继承 `BaseFragment`）：
```kotlin
class MyFragment : BaseFragment<MyIntent, MyState, MyEvent, MyViewModel>() {
    override val viewModel: MyViewModel by viewModels()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_my, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 初始化UI
        dispatchIntent(MyIntent.LoadData)
    }
    
    override fun render(state: MyState) {
        // 根据状态更新UI
        binding.progressBar.isVisible = state.isLoading
        binding.recyclerView.adapter = MyAdapter(state.items)
    }
    
    override fun handleEvent(event: MyEvent) {
        when (event) {
            is MyEvent.ShowToast -> Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
            is MyEvent.NavigateToDetail -> findNavController().navigate(R.id.action_to_detail)
        }
    }
}
```

## 🔧 技术栈

- **语言**: Kotlin 1.9.0
- **架构**: MVI (Model-View-Intent)
- **异步**: Kotlin Coroutines + Flow
- **状态管理**: StateFlow + SharedFlow
- **UI**: Jetpack Compose (可选) / XML
- **依赖管理**: Gradle Version Catalog
- **最小SDK**: Android 8.0 (API 24)

## 📊 性能特性

- ✅ 状态不可变性
- ✅ 单向数据流
- ✅ 线程安全的状态更新
- ✅ 背压处理的Event流
- ✅ 生命周期感知的数据收集
- ✅ 可测试的纯函数

## 🧪 测试

项目包含完整的测试示例，测试策略参考 [IMPLEMENTATION.md](app/src/main/java/com/example/mvi/mvibase/IMPLEMENTATION.md)：

```bash
./gradlew test # 运行单元测试
./gradlew connectedAndroidTest # 运行仪器测试
```

## 🤝 贡献

欢迎提交Issue和Pull Request！

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 打开Pull Request


## � 详细文档

对于MVI架构的完整实现细节，请参考以下文档：

- [DESIGN.md](app/src/main/java/com/example/mvi/mvibase/DESIGN.md) - 架构设计思路和原则
- [IMPLEMENTATION.md](app/src/main/java/com/example/mvi/mvibase/IMPLEMENTATION.md) - 技术实现细节
- [USAGE.md](app/src/main/java/com/example/mvi/mvibase/USAGE.md) - 完整使用指南和示例

## �🙏 致谢

- 感谢 [Jetpack](https://developer.android.com/jetpack) 团队提供的优秀架构组件
- 感谢 [Kotlin](https://kotlinlang.org) 团队提供的现代化语言特性
- 灵感来源于多个优秀的MVI架构实现


## ⭐ 如果这个项目对你有帮助，请给它一个Star！

---

**Happy Coding!** 🎉
# MVI 架构实现细节

## 📦 核心组件实现

### 1. BaseIntent 实现

**文件位置**: `BaseIntent.kt`

```kotlin
package mvi

/**
 * Base interface for all intents in MVI architecture.
 * Intents represent user actions that trigger state changes.
 * 
 * 设计理念:
 * - 所有用户操作都封装为 Intent
 * - Intent 应该是不可变的数据类
 * - 使用密封类来组织相关的 Intent
 */
interface BaseIntent
```

**技术细节**:
- 使用接口而非抽象类，提供更大的灵活性
- 配合密封类实现类型安全的模式匹配
- 所有实现类应该是 `data class` 确保不可变性

### 2. BaseEvent 实现

**文件位置**: `BaseEvent.kt`

```kotlin
package mvi

/**
 * Base interface for all events in MVI architecture.
 * Events represent side effects or one-time occurrences that don't affect state directly.
 * 
 * 设计理念:
 * - Event 用于处理一次性操作（导航、Toast、对话框等）
 * - Event 不应该影响状态，只触发副作用
 * - 使用 SharedFlow 确保多个订阅者都能收到事件
 */
interface BaseEvent
```

**技术细节**:
- 事件通过 SharedFlow 发射，支持多个订阅者
- 事件处理应该快速且无阻塞
- 事件完成后立即清理，不保留历史

### 3. BaseViewModel 实现

**文件位置**: `BaseViewModel.kt`

#### 核心状态管理
```kotlin
private val _state: MutableStateFlow<S> by lazy {
    MutableStateFlow(initialState())
}
val state: StateFlow<S> = _state.asStateFlow()
```

**设计选择**:
- 使用 `by lazy` 延迟初始化，避免在构造函数中执行复杂逻辑
- `StateFlow` 提供最新的状态值，适合 UI 状态管理
- `MutableStateFlow` 封装在 ViewModel 内部，外部只能读取

#### 事件管理
```kotlin
private val _event: MutableSharedFlow<E> = MutableSharedFlow()
val event: SharedFlow<E> = _event.asSharedFlow()
```

**设计选择**:
- `SharedFlow` 用于事件，支持多个订阅者
- 使用默认配置（无重放，无缓存），确保事件只处理一次
- 事件发射使用 `emit()`，确保协程安全

#### 状态更新方法
```kotlin
protected fun updateState(update: (S) -> S) {
    _state.update(update)
}

protected fun setState(newState: S) {
    _state.value = newState
}
```

**设计选择**:
- `updateState`: 函数式更新，基于当前状态生成新状态
- `setState`: 直接设置新状态，适用于完全替换
- 两个方法提供不同的使用场景选择

#### 意图处理
```kotlin
fun processIntent(intent: I) {
    viewModelScope.launch {
        handleIntent(intent)
    }
}

protected abstract suspend fun handleIntent(intent: I)
```

**设计选择**:
- `processIntent`: 公共方法，自动在 ViewModelScope 中启动
- `handleIntent`: 抽象方法，子类实现具体业务逻辑
- 使用 `suspend` 支持异步操作

### 4. BaseActivity 实现

**文件位置**: `BaseActivity.kt`

#### 状态观察
```kotlin
private fun observeState() {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.state.collect { state ->
                render(state)
            }
        }
    }
}
```

**技术细节**:
- 使用 `repeatOnLifecycle(Lifecycle.State.STARTED)` 确保只在活跃状态收集
- 避免内存泄漏和不必要的资源消耗
- 自动处理配置变化时的状态恢复

#### 事件观察
```kotlin
private fun observeEvents() {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.event.collect { event ->
                handleEvent(event)
            }
        }
    }
}
```

**技术细节**:
- 事件收集同样遵循生命周期规则
- 每个事件只处理一次，多个订阅者都能收到
- 事件处理应该快速完成，避免阻塞

#### 意图分发
```kotlin
protected fun dispatchIntent(intent: I) {
    viewModel.processIntent(intent)
}
```

**设计选择**:
- 提供便捷的意图分发方法
- 封装 ViewModel 的调用细节
- 支持多种意图分发方式（点击、输入、生命周期等）

### 5. BaseFragment 实现

**文件位置**: `BaseFragment.kt`

#### Fragment特有的生命周期处理
```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    observeState()
    observeEvents()
}
```

**技术细节**:
- 在 `onViewCreated` 中开始观察，而不是 `onCreate`
- 确保View已经创建完成，避免空指针异常
- 遵循Fragment的最佳实践

#### 状态观察（Fragment版本）
```kotlin
private fun observeState() {
    viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.state.collect { state ->
                render(state)
            }
        }
    }
}
```

**技术细节**:
- 使用 `viewLifecycleOwner` 而不是 `lifecycleScope`
- 确保与Fragment的View生命周期同步
- 避免View销毁后的状态更新

#### 事件观察（Fragment版本）
```kotlin
private fun observeEvents() {
    viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.event.collect { event ->
                handleEvent(event)
            }
        }
    }
}
```

**技术细节**:
- 同样使用 `viewLifecycleOwner` 确保生命周期安全
- 事件处理与Activity版本保持一致
- 支持多个Fragment共享同一个ViewModel

#### 意图分发
```kotlin
protected fun dispatchIntent(intent: I) {
    viewModel.processIntent(intent)
}
```

**设计选择**:
- 与BaseActivity保持相同的API
- 提供一致的开发体验
- 支持Fragment间的意图通信

### 5. MVIUtils 实现

**文件位置**: `MVIUtils.kt`

#### ResultState 密封类
```kotlin
sealed interface ResultState<out T> {
    data object Loading : ResultState<Nothing>
    data class Success<T>(val data: T) : ResultState<T>
    data class Error(val message: String, val throwable: Throwable? = null) : ResultState<Nothing>
}
```

**设计理念**:
- 统一处理异步操作的状态
- 提供标准的加载、成功、错误状态
- 支持泛型，适用于各种数据类型

#### Flow 工具函数
```kotlin
fun <T1, T2, R> combineFlows(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    transform: (T1, T2) -> R
): Flow<R>
```

**技术细节**:
- 提供类型安全的 Flow 组合
- 支持自定义转换函数
- 简化多个数据源的组合操作

#### Result 扩展
```kotlin
fun <T> Result<T>.toResultState(): ResultState<T>
```

**设计选择**:
- 将 Kotlin 的 Result 转换为 ResultState
- 统一错误处理逻辑
- 提供更好的类型推断

## 🛠️ 技术实现细节

### 1. 协程和 Flow 配置

**ViewModelScope 配置**:
```kotlin
viewModelScope.launch {
    // 默认使用 Dispatchers.Main.immediate
    // 适合状态更新和事件发射
}
```

**Flow 配置**:
- `StateFlow`: replay = 1, 总是提供最新状态
- `SharedFlow`: replay = 0, 不缓存任何事件
- 使用 `asStateFlow()` 和 `asSharedFlow()` 确保不可变访问

### 2. 生命周期集成

**Activity 生命周期绑定**:
```kotlin
repeatOnLifecycle(Lifecycle.State.STARTED) {
    // 只在 STARTED 及以上状态收集
}
```

**优点**:
- 避免后台状态的不必要收集
- 减少资源消耗
- 防止内存泄漏

### 3. 类型安全设计

**泛型参数**:
```kotlin
abstract class BaseViewModel<I : BaseIntent, S, E : BaseEvent>
```

**设计好处**:
- 编译时类型检查
- 避免运行时类型错误
- 更好的 IDE 支持

### 4. 性能优化措施

**状态更新优化**:
```kotlin
_state.update { current ->
    if (current == newState) current else newState
}
```

**事件发射优化**:
```kotlin
// SharedFlow 默认不缓存，适合一次性事件
_event.emit(event)
```

## 🔧 扩展点设计

### 1. 中间件支持
```kotlin
interface Middleware<I : BaseIntent, S, E : BaseEvent> {
    suspend fun process(intent: I, state: S, next: (I) -> Unit)
}
```

### 2. 日志记录
```kotlin
class LoggingMiddleware : Middleware<BaseIntent, Any, BaseEvent> {
    override suspend fun process(intent: BaseIntent, state: Any, next: (BaseIntent) -> Unit) {
        Log.d("MVI", "Processing intent: $intent")
        next(intent)
    }
}
```

### 3. 状态持久化
```kotlin
interface StatePersister<S> {
    suspend fun save(state: S)
    suspend fun load(): S?
}
```

## 🧪 测试支持

### 1. ViewModel 测试
```kotlin
@Test
fun `should update state when intent is processed`() = runTest {
    val viewModel = TestViewModel()
    viewModel.processIntent(TestIntent.LoadData)
    
    assertEquals(TestState.Loading, viewModel.state.value)
}
```

### 2. 状态测试
```kotlin
@Test
fun `should emit correct state sequence`() = runTest {
    val viewModel = TestViewModel()
    val states = mutableListOf<TestState>()
    
    backgroundScope.launch {
        viewModel.state.collect { states.add(it) }
    }
    
    viewModel.processIntent(TestIntent.LoadData)
    
    assertEquals(2, states.size)
    assertEquals(TestState.Initial, states[0])
    assertEquals(TestState.Loading, states[1])
}
```

这个实现细节文档提供了完整的技术实现说明，包括设计选择、技术细节和扩展点。
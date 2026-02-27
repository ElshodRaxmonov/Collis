package com.example.collis.presentation.ui.student.task


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.collis.domain.model.Task
import com.example.collis.domain.model.TaskPriority
import com.example.collis.presentation.components.CollisTopBar
import com.example.collis.presentation.components.CollisFAB
import com.example.collis.presentation.components.EmptyState
import com.example.collis.presentation.components.ErrorState
import com.example.collis.presentation.components.TaskCard
import com.example.collis.presentation.components.TaskCardShimmer
import com.example.collis.ui.theme.CollisTheme
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * Task Manager Screen
 *
 * FEATURES:
 * - Full task list with filtering
 * - Search functionality
 * - Sort options
 * - Swipe to delete
 * - Bulk delete completed
 * - FAB for quick add
 * - Empty states
 * - Filter chips
 *
 * UX PATTERNS:
 * - Pull to refresh
 * - Bottom sheets for filter/sort
 * - Snackbar with undo
 * - Smooth animations
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TaskManagerScreen(
    onNavigateToTaskDetail: (Long) -> Unit,
    onNavigateToAddTask: () -> Unit,
    viewModel: TaskManagerViewModel = hiltViewModel()
) {
    /**
     * Collect state
     */
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val showFilterSheet by viewModel.showFilterSheet.collectAsStateWithLifecycle()
    val showSortSheet by viewModel.showSortSheet.collectAsStateWithLifecycle()

    TaskManagerScreenContent(
        uiState = uiState,
        searchQuery = searchQuery,
        showFilterSheet = showFilterSheet,
        showSortSheet = showSortSheet,
        onEvent = viewModel::onEvent,
        onNavigateToTaskDetail = onNavigateToTaskDetail,
        onNavigateToAddTask = onNavigateToAddTask,
        dismissFilterSheet = viewModel::dismissFilterSheet,
        dismissSortSheet = viewModel::dismissSortSheet
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskManagerScreenContent(
    uiState: TaskManagerUiState,
    searchQuery: String,
    showFilterSheet: Boolean,
    showSortSheet: Boolean,
    onEvent: (TaskManagerUiEvent) -> Unit,
    onNavigateToTaskDetail: (Long) -> Unit,
    onNavigateToAddTask: () -> Unit,
    dismissFilterSheet: () -> Unit,
    dismissSortSheet: () -> Unit
) {
    /**
     * Coroutine scope for UI actions (e.g., snackbar)
     */
    val scope = rememberCoroutineScope()

    /**
     * Snackbar state
     */
    val snackbarHostState = remember { SnackbarHostState() }

    /**
     * Search bar active state
     */
    var searchActive by remember { mutableStateOf(false) }

    /**
     * Filter bottom sheet state
     */
    val filterSheetState = rememberModalBottomSheetState()
    val sortSheetState = rememberModalBottomSheetState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            /**
             * Top App Bar with Search
             */
            if (searchActive) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { onEvent(TaskManagerUiEvent.SearchTasks(it)) },
                    onSearch = { searchActive = false },
                    active = true,
                    onActiveChange = { searchActive = it },
                    placeholder = { Text("Search tasks...") },
                    leadingIcon = {
                        IconButton(onClick = {
                            searchActive = false
                            onEvent(TaskManagerUiEvent.ClearSearch)
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                onEvent(TaskManagerUiEvent.ClearSearch)
                            }) {
                                Icon(Icons.Default.Clear, "Clear")
                            }
                        }
                    }
                ) {
                    // Search suggestions could go here
                }
            } else {
                CollisTopBar(
                    title = {
                        Column {
                            Text(
                                text = "Tasks",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,

                                modifier = Modifier.padding(start = 10.dp)
                            )

                            // Show count
                            if (uiState is TaskManagerUiState.Success) {
                                val state = uiState as TaskManagerUiState.Success
                                Text(
                                    text = "${state.displayedTasks.size} tasks",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,

                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    },
                    actions = {
                        /**
                         * Search icon
                         */
                        IconButton(onClick = { searchActive = true }) {
                            Icon(Icons.Default.Search, "Search")
                        }

                        /**
                         * More menu
                         */
                        var showMenu by remember { mutableStateOf(false) }

                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, "More")
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Delete Completed") },
                                    onClick = {
                                        showMenu = false
                                        onEvent(TaskManagerUiEvent.DeleteAllCompleted)
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.DeleteSweep, null)
                                    }
                                )
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            /**
             * FAB for add task
             */
            if (!searchActive) {
                CollisFAB(
                    onClick = onNavigateToAddTask,
                    icon = Icons.Default.Add,
                    contentDescription = "Add Task",
                    expanded = true,
                    text = "New Task"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Box(modifier = Modifier.padding(paddingValues)) {
            /**
             * Content based on state
             */
            when (val state = uiState) {
                is TaskManagerUiState.Loading -> {
                    LoadingContent()
                }

                is TaskManagerUiState.Success -> {
                    SuccessContent(
                        state = state,
                        onTaskClick = onNavigateToTaskDetail,
                        onToggleComplete = { taskId ->
                            onEvent(TaskManagerUiEvent.ToggleTaskCompletion(taskId))
                        },
                        onDeleteTask = { task ->
                            onEvent(TaskManagerUiEvent.DeleteTask(task))

                            // Show undo snackbar
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Task deleted",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )

                                if (result == SnackbarResult.ActionPerformed) {
                                    onEvent(TaskManagerUiEvent.UndoDelete(task))
                                }
                            }
                        },
                        onFilterClick = { filter ->
                            onEvent(TaskManagerUiEvent.ChangeFilter(filter))
                        }
                    )
                }

                is TaskManagerUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { onEvent(TaskManagerUiEvent.Refresh) }
                    )
                }

                is TaskManagerUiState.Empty -> {
                    EmptyState(
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        title = "No tasks yet",
                        message = "Create your first task to get started!",
                        actionButtonText = "Add Task",
                        onActionClick = onNavigateToAddTask
                    )
                }
            }
        }

        /**
         * Filter Bottom Sheet
         */
        if (showFilterSheet) {
            FilterBottomSheet(
                sheetState = filterSheetState,
                currentFilter = (uiState as? TaskManagerUiState.Success)?.selectedFilter ?: TaskFilter.ALL,
                pendingCount = (uiState as? TaskManagerUiState.Success)?.pendingCount ?: 0,
                completedCount = (uiState as? TaskManagerUiState.Success)?.completedCount ?: 0,
                overdueCount = (uiState as? TaskManagerUiState.Success)?.overdueCount ?: 0,
                onFilterSelected = { filter ->
                    onEvent(TaskManagerUiEvent.ChangeFilter(filter))
                },
                onDismiss = { dismissFilterSheet() }
            )
        }

        /**
         * Sort Bottom Sheet
         */
        if (showSortSheet) {
            SortBottomSheet(
                sheetState = sortSheetState,
                currentSort = (uiState as? TaskManagerUiState.Success)?.selectedSort ?: TaskSort.DUE_DATE_ASC,
                onSortSelected = { sort ->
                    onEvent(TaskManagerUiEvent.ChangeSort(sort))
                },
                onDismiss = { dismissSortSheet() }
            )
        }
    }
}

/**
 * Loading Content
 */
@Composable
private fun LoadingContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(5) {
            TaskCardShimmer()
        }
    }
}

/**
 * Success Content
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SuccessContent(
    state: TaskManagerUiState.Success,
    onTaskClick: (Long) -> Unit,
    onToggleComplete: (Long) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onFilterClick: (TaskFilter) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        /**
         * Filter Chips Row
         *
         * HORIZONTAL SCROLL:
         * Shows filter options as chips
         * Current filter is highlighted
         */
        FilterChipsRow(
            selectedFilter = state.selectedFilter,
            pendingCount = state.pendingCount,
            completedCount = state.completedCount,
            overdueCount = state.overdueCount,
            onFilterClick = onFilterClick
        )

        /**
         * Task List
         */
        if (state.displayedTasks.isEmpty()) {
            /**
             * Empty filtered state
             * Show when filter returns no results
             */
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterAltOff,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No tasks match this filter",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Try a different filter or create a new task",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = state.displayedTasks,
                    key = { it.id }
                ) { task ->
                    /**
                     * Animated Item Appearance
                     *
                     * AnimatedVisibility:
                     * Smooth enter/exit animations
                     */
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        TaskCard(
                            task = task,
                            onTaskClick = onTaskClick,
                            onToggleComplete = onToggleComplete,
                            onDeleteTask = onDeleteTask
                        )
                    }
                }

                // Bottom padding for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

/**
 * Filter Chips Row
 * Horizontal scrollable filter options — uses LazyRow so chips never overflow.
 */
@Composable
private fun FilterChipsRow(
    selectedFilter: TaskFilter,
    pendingCount: Int,
    completedCount: Int,
    overdueCount: Int,
    onFilterClick: (TaskFilter) -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedFilter == TaskFilter.ALL,
                onClick = { onFilterClick(TaskFilter.ALL) },
                label = { Text("All") },
                leadingIcon = if (selectedFilter == TaskFilter.ALL) {
                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                } else null
            )
        }

        item {
            FilterChip(
                selected = selectedFilter == TaskFilter.PENDING,
                onClick = { onFilterClick(TaskFilter.PENDING) },
                label = { Text("Pending ($pendingCount)") },
                leadingIcon = if (selectedFilter == TaskFilter.PENDING) {
                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                } else null
            )
        }

        item {
            FilterChip(
                selected = selectedFilter == TaskFilter.COMPLETED,
                onClick = { onFilterClick(TaskFilter.COMPLETED) },
                label = { Text("Completed ($completedCount)") },
                leadingIcon = if (selectedFilter == TaskFilter.COMPLETED) {
                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                } else null
            )
        }

        if (overdueCount > 0) {
            item {
                FilterChip(
                    selected = selectedFilter == TaskFilter.OVERDUE,
                    onClick = { onFilterClick(TaskFilter.OVERDUE) },
                    label = { Text("Overdue ($overdueCount)") },
                    leadingIcon = if (selectedFilter == TaskFilter.OVERDUE) {
                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }
        }

        item {
            FilterChip(
                selected = selectedFilter == TaskFilter.TODAY,
                onClick = { onFilterClick(TaskFilter.TODAY) },
                label = { Text("Today") },
                leadingIcon = if (selectedFilter == TaskFilter.TODAY) {
                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                } else null
            )
        }
    }
}

/**
 * Filter Bottom Sheet
 * Full filter options with counts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    sheetState: SheetState,
    currentFilter: TaskFilter,
    pendingCount: Int,
    completedCount: Int,
    overdueCount: Int,
    onFilterSelected: (TaskFilter) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            /**
             * Header
             */
            Text(
                text = "Filter Tasks",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            /**
             * Filter Options
             */
            TaskFilter.values().forEach { filter ->
                val count = when (filter) {
                    TaskFilter.PENDING -> pendingCount
                    TaskFilter.COMPLETED -> completedCount
                    TaskFilter.OVERDUE -> overdueCount
                    else -> null
                }

                FilterOption(
                    filter = filter,
                    isSelected = currentFilter == filter,
                    count = count,
                    onClick = { onFilterSelected(filter) }
                )

                if (filter != TaskFilter.values().last()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Filter Option Row
 */
@Composable
private fun FilterOption(
    filter: TaskFilter,
    isSelected: Boolean,
    count: Int?,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected)
            MaterialTheme.colorScheme.secondaryContainer
        else
            MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                /**
                 * Radio Button
                 */
                RadioButton(
                    selected = isSelected,
                    onClick = onClick
                )

                /**
                 * Filter Name
                 */
                Text(
                    text = filter.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }

            /**
             * Count Badge
             */
            if (count != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Sort Bottom Sheet
 * All sort options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortBottomSheet(
    sheetState: SheetState,
    currentSort: TaskSort,
    onSortSelected: (TaskSort) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            /**
             * Header
             */
            Text(
                text = "Sort Tasks",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            /**
             * Sort Options
             */
            TaskSort.values().forEach { sort ->
                SortOption(
                    sort = sort,
                    isSelected = currentSort == sort,
                    onClick = { onSortSelected(sort) }
                )

                if (sort != TaskSort.values().last()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Sort Option Row
 */
@Composable
private fun SortOption(
    sort: TaskSort,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected)
            MaterialTheme.colorScheme.secondaryContainer
        else
            MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            /**
             * Radio Button
             */
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )

            /**
             * Sort Name
             */
            Text(
                text = sort.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun TaskManagerScreenPreview() {
    val mockTasks = listOf(
        Task(
            id = 1,
            title = "Complete Android Project",
            description = "Finish the task manager screen with previews",
            dueDate = LocalDateTime.now().plusDays(2),
            priority = TaskPriority.HIGH,
            isCompleted = false
        ),
        Task(
            id = 2,
            title = "Buy Groceries",
            description = "Milk, Eggs, Bread",
            dueDate = LocalDateTime.now(),
            priority = TaskPriority.MEDIUM,
            isCompleted = true
        ),
        Task(
            id = 3,
            title = "Gym Session",
            dueDate = LocalDateTime.now().minusDays(1),
            priority = TaskPriority.LOW,
            isCompleted = false
        )
    )

    CollisTheme {
        TaskManagerScreenContent(
            uiState = TaskManagerUiState.Success(
                allTasks = mockTasks,
                displayedTasks = mockTasks,
                selectedFilter = TaskFilter.ALL,
                selectedSort = TaskSort.DUE_DATE_ASC,
                searchQuery = "",
                pendingCount = 2,
                completedCount = 1,
                overdueCount = 1
            ),
            searchQuery = "",
            showFilterSheet = false,
            showSortSheet = false,
            onEvent = {},
            onNavigateToTaskDetail = {},
            onNavigateToAddTask = {},
            dismissFilterSheet = {},
            dismissSortSheet = {}
        )
    }
}

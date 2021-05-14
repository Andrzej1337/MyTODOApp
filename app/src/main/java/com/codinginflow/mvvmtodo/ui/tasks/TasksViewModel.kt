package com.codinginflow.mvvmtodo.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.codinginflow.mvvmtodo.data.PreferenceManager
import com.codinginflow.mvvmtodo.data.SortOrder
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.ui.ADD_TASK_RESLUT_OK
import com.codinginflow.mvvmtodo.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferenceManager: PreferenceManager,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    val searchQuery = state.getLiveData("searchQuery", "")

    val preferencesFlow = preferenceManager.preferencesFlow

    private val  tasksEventChanel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChanel.receiveAsFlow()

    private val taskFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ){ query, filterPreferences ->
        Pair(query, filterPreferences)
    }

        .flatMapLatest {   (query, filterPreferences)->
        taskDao.getTask(query, filterPreferences.sortOrder,filterPreferences.hideCompleted)
    }
    val tasks = taskFlow.asLiveData()


    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferenceManager.updateSortOrder(sortOrder)
    }
    fun onHideCompletedClick(hideComleted: Boolean) = viewModelScope.launch {
        preferenceManager.updateHideCompleted(hideComleted)
    }

    fun onTaskSelected(task: Task) = viewModelScope.launch{
        tasksEventChanel.send(TasksEvent.NavigateToEditTaskScreen(task))

    }
    fun onTaskCheckChanged(task: Task, isCHecked: Boolean)=viewModelScope.launch {
        taskDao.update(task.copy(completed = true))
    }
    fun onTaskiSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        tasksEventChanel.send(TasksEvent.ShowUndoDeleteTaskMessage(task))
    }
    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
    }
    fun onAddNewTaskClick() = viewModelScope.launch {
        tasksEventChanel.send(TasksEvent.NavigateToAddTaskScreen)

    }

    fun onAddEditResult(result: Int){
        when (result){
            ADD_TASK_RESLUT_OK -> showTaskSavedConfirmationMessage("Dodano")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Edytowano")
        }
    }

    private fun showTaskSavedConfirmationMessage(text : String) = viewModelScope.launch {
            tasksEventChanel.send(TasksEvent.ShowTaskSavedConfirmnationMessage(text))
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        tasksEventChanel.send(TasksEvent.NavigateToDeleteAllCompletedScreen)

    }

    sealed class TasksEvent{
        object NavigateToAddTaskScreen : TasksEvent()
        data class NavigateToEditTaskScreen(val task: Task) : TasksEvent()
        data class ShowUndoDeleteTaskMessage(val task: Task) : TasksEvent()
        data class ShowTaskSavedConfirmnationMessage(val mgs: String) : TasksEvent()
        object NavigateToDeleteAllCompletedScreen : TasksEvent()
    }

}


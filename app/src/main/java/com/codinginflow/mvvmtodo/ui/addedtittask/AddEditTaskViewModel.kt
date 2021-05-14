package com.codinginflow.mvvmtodo.ui.addedtittask

import android.app.Activity
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.ui.ADD_TASK_RESLUT_OK
import com.codinginflow.mvvmtodo.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddEditTaskViewModel
    @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @Assisted private val state : SavedStateHandle
) : ViewModel() {


    val task = state.get<Task>("task")

    var taskName = state.get<String>("taskName") ?: task?.name ?: ""
        set(value) {
            field = value
            state.set("taskName", value)
        }
    var taskImportance = state.get<Boolean>("taskImportance") ?: task?.important ?: false
        set(value) {
            field = value
            state.set("taskImportance", value)

        }

    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()

    fun onSaveClick(){
        if (taskName.isBlank()) {
            showInvalidInputMessage("nie może być puste")
            return
        }

        if (task != null) {
            val updatedTask = task.copy(name = taskName, important = taskImportance)
            updatedTasks(updatedTask)

        }else{
            val newTask = Task(name = taskName, important = taskImportance)
            createTask(newTask)

        }
    }

    private fun createTask(task: Task) = viewModelScope.launch {
            taskDao.insert(task)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateTaskBackWithResult(ADD_TASK_RESLUT_OK))
    }
    private fun  updatedTasks(task: Task) =viewModelScope.launch {
        taskDao.update(task)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateTaskBackWithResult(EDIT_TASK_RESULT_OK))
    }

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidMessage(text))
    }

    sealed class AddEditTaskEvent {
        data class ShowInvalidMessage(val msg: String) : AddEditTaskEvent()
        data class NavigateTaskBackWithResult(val result: Int) : AddEditTaskEvent()
    }

}
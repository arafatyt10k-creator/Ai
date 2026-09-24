package com.example.data.repository

import com.example.data.local.dao.TaskDao
import com.example.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasks(): Flow<List<TaskEntity>>
    fun getPendingTasks(): Flow<List<TaskEntity>>
    fun getCompletedTasks(): Flow<List<TaskEntity>>
    suspend fun addTask(
        title: String,
        description: String = "",
        priority: String = "MEDIUM",
        dueDateMillis: Long? = null,
        dueTimeString: String? = null,
        category: String = "General"
    ): Long
    suspend fun updateTask(task: TaskEntity)
    suspend fun deleteTask(id: Long)
    suspend fun setTaskCompleted(id: Long, isCompleted: Boolean)
}

class TaskRepositoryImpl(private val taskDao: TaskDao) : TaskRepository {
    override fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()
    override fun getPendingTasks(): Flow<List<TaskEntity>> = taskDao.getPendingTasks()
    override fun getCompletedTasks(): Flow<List<TaskEntity>> = taskDao.getCompletedTasks()

    override suspend fun addTask(
        title: String,
        description: String,
        priority: String,
        dueDateMillis: Long?,
        dueTimeString: String?,
        category: String
    ): Long {
        val task = TaskEntity(
            title = title.ifBlank { "New Task" },
            description = description,
            priority = priority,
            dueDateMillis = dueDateMillis,
            dueTimeString = dueTimeString,
            isCompleted = false,
            createdAt = System.currentTimeMillis(),
            category = category
        )
        return taskDao.insertTask(task)
    }

    override suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
    }

    override suspend fun deleteTask(id: Long) {
        taskDao.deleteTaskById(id)
    }

    override suspend fun setTaskCompleted(id: Long, isCompleted: Boolean) {
        taskDao.updateTaskCompletion(id, isCompleted)
    }
}

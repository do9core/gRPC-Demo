package xyz.do9core.proto_todo.detail

import androidx.compose.runtime.*
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import xyz.do9core.proto_todo.TodoDetail
import xyz.do9core.proto_todo.TodoRepository
import xyz.do9core.proto_todo.TodoStep
import xyz.do9core.proto_todo.service.TodoServiceGrpcKt
import xyz.do9core.proto_todo.service.completeTodoStepRequest
import xyz.do9core.proto_todo.service.deCompleteTodoStepRequest
import xyz.do9core.proto_todo.service.todoDetailRequest

@Stable
class StepItem(source: TodoStep) {
    val id = source.id

    var title by mutableStateOf(source.title)
    var description by mutableStateOf(source.description)
    var completed by mutableStateOf(source.completed)
}

sealed interface DetailContent

object Loading : DetailContent

@Stable
class Success(source: TodoDetail) : DetailContent {
    val id = source.id

    var title by mutableStateOf(source.title)
    var description by mutableStateOf(source.description)
    val steps = mutableStateListOf<StepItem>().apply {
        addAll(source.stepsList.map(::StepItem))
    }
}

@Immutable
class Failed(val error: Throwable) : DetailContent

class DetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val stub: TodoServiceGrpcKt.TodoServiceCoroutineStub get() = TodoRepository.stub

    var content by mutableStateOf<DetailContent>(Loading)

    private val id: Long

    init {
        id = savedStateHandle.get<Long>("id") ?: error("Missing required argument `id`")
        fetchDetail(id)
    }

    fun fetchDetail(targetId: Long) {
        content = Loading
        viewModelScope.launch {
            val request = todoDetailRequest { id = targetId }
            content = try {
                val response = stub.todoDetail(request)
                Success(response.todo)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Failed(e)
            }
        }
    }

    suspend fun completeStep(targetStepId: Long): Result<*> {
        val request = completeTodoStepRequest {
            todoId = id
            stepId = targetStepId
        }
        return kotlin.runCatching { stub.completeTodoStep(request) }
    }

    suspend fun deCompleteStep(targetStepId: Long): Result<*> {
        val request = deCompleteTodoStepRequest {
            todoId = id
            stepId = targetStepId
        }
        return kotlin.runCatching { stub.deCompleteTodoStep(request) }
    }
}

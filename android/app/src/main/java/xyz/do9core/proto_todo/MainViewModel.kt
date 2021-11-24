package xyz.do9core.proto_todo

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import xyz.do9core.proto_todo.service.TodoServiceGrpcKt.TodoServiceCoroutineStub
import xyz.do9core.proto_todo.service.completeTodoRequest
import xyz.do9core.proto_todo.service.deCompleteTodoRequest
import xyz.do9core.proto_todo.service.listTodoRequest

@Immutable
class SummaryItem(source: TodoSummary) {

    val id: Long = source.id

    @Stable
    var title by mutableStateOf(source.title)

    @Stable
    var completed by mutableStateOf(source.completed)
}

sealed interface ViewContent

object NotRequested : ViewContent

data class SuccessResult(
    val summaryList: List<SummaryItem>
) : ViewContent

class FailedResult(val error: Throwable) : ViewContent

class MainViewModel : ViewModel() {

    private val stub: TodoServiceCoroutineStub = TodoRepository.stub

    var loading by mutableStateOf(false)
        private set

    var viewContent by mutableStateOf<ViewContent>(NotRequested)
        private set

    fun fetchTodoItems() {
        loading = true
        viewModelScope.launch {
            val request = listTodoRequest{}
            viewContent = try {
                val response = stub.listTodos(request)
                val items = response.todosList.map {
                    SummaryItem(it)
                }
                SuccessResult(items)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Log.e("GrpcService", e.message, e)
                FailedResult(e)
            } finally {
                loading = false
            }
        }
    }

    suspend fun completeTodo(targetId: Long): Result<*> {
        val request = completeTodoRequest { id = targetId }
        return kotlin.runCatching { stub.completeTodo(request) }
    }

    suspend fun deCompleteTodo(targetId: Long): Result<*> {
        val request = deCompleteTodoRequest { id = targetId }
        return kotlin.runCatching { stub.deCompleteTodo(request) }
    }
}

package xyz.do9core.proto_todo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import xyz.do9core.proto_todo.detail.DetailActivity
import xyz.do9core.proto_todo.ui.theme.MyTodoTheme

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTodoTheme {
                Surface {
                    MainContent(
                        contentData = viewModel.viewContent,
                        viewModel = viewModel,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchTodoItems()
    }
}

@Composable
fun MainContent(
    contentData: ViewContent,
    viewModel: MainViewModel,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = "To-do",
            style = MaterialTheme.typography.h3,
        )
        Spacer(modifier = Modifier.height(32.dp))
        when (contentData) {
            NotRequested -> {
                Text(
                    text = "Enter an id, then click button to request a todo detail.",
                    style = MaterialTheme.typography.h6,
                )
            }
            is SuccessResult -> {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(contentData.summaryList, key = { _, item -> item.id }) { index, summary ->
                        var updating by remember { mutableStateOf(false) }
                        Column {
                            @OptIn(ExperimentalMaterialApi::class)
                            Card(
                                elevation = 4.dp,
                                onClick = {
                                    val intent = Intent(context, DetailActivity::class.java)
                                    intent.putExtra("id", summary.id)
                                    context.startActivity(intent)
                                },
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillParentMaxWidth()
                                ) {
                                    TriStateCheckbox(
                                        state = when {
                                            updating -> ToggleableState.Indeterminate
                                            summary.completed -> ToggleableState.On
                                            else -> ToggleableState.Off
                                        },
                                        onClick = click@{
                                            if (updating) {
                                                return@click
                                            }
                                            updating = true
                                            scope.launch {
                                                if (summary.completed) {
                                                    viewModel.deCompleteTodo(summary.id)
                                                        .onSuccess { summary.completed = false }
                                                } else {
                                                    viewModel.completeTodo(summary.id)
                                                        .onSuccess { summary.completed = true }
                                                }
                                                updating = false
                                            }
                                        },
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = summary.title,
                                        style = MaterialTheme.typography.h6,
                                    )
                                }
                            }
                            if (index < contentData.summaryList.lastIndex) {
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }
            is FailedResult -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    var expandDetail by remember { mutableStateOf(false) }
                    Text(
                        text = contentData.error.message ?: "Unknown error",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.error,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .toggleable(expandDetail) { expandDetail = it }
                    ) {
                        Text(text = "detail")
                        Icon(
                            imageVector = if (expandDetail) {
                                Icons.Default.KeyboardArrowUp
                            } else {
                                Icons.Default.KeyboardArrowDown
                            },
                            modifier = Modifier.size(14.dp),
                            contentDescription = null
                        )
                    }
                    if (expandDetail) {
                        Text(
                            text = contentData.error.stackTraceToString(),
                            style = MaterialTheme.typography.overline,
                            color = MaterialTheme.colors.onSurface,
                            modifier = Modifier.verticalScroll(
                                state = rememberScrollState(),
                            )
                        )
                    }
                }
            }
        }
    }
}

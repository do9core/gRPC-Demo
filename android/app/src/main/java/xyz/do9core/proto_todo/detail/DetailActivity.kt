package xyz.do9core.proto_todo.detail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import xyz.do9core.proto_todo.ui.theme.MyTodoTheme

class DetailActivity : ComponentActivity() {

    private val viewModel by viewModels<DetailViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTodoTheme {
                TodoDetailContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TodoDetailContent(viewModel: DetailViewModel) {
    val scope = rememberCoroutineScope()
    Surface {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val content = viewModel.content) {
                Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
                is Success -> Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    Text(
                        text = content.title,
                        style = MaterialTheme.typography.h5,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Divider(startIndent = 4.dp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = content.description)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 6.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        itemsIndexed(content.steps, key = { _, item -> item.id }) { index, step ->
                            Column {
                                var updating by remember { mutableStateOf(false) }
                                Card {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .fillParentMaxWidth()
                                    ) {
                                        TriStateCheckbox(
                                            state = when {
                                                updating -> ToggleableState.Indeterminate
                                                step.completed -> ToggleableState.On
                                                else -> ToggleableState.Off
                                            },
                                            onClick = click@{
                                                if (updating) {
                                                    return@click
                                                }
                                                updating = true
                                                scope.launch {
                                                    if (step.completed) {
                                                        viewModel.deCompleteStep(step.id)
                                                            .onSuccess { step.completed = false }
                                                    } else {
                                                        viewModel.completeStep(step.id)
                                                            .onSuccess { step.completed = true }
                                                    }
                                                    updating = false
                                                }
                                            },
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = step.title,
                                            style = MaterialTheme.typography.h6,
                                        )
                                    }
                                }
                                if (index < content.steps.lastIndex) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                }
                is Failed -> Text(
                    text = content.error.message ?: "Unknown error",
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

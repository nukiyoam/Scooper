package scooper.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.KeyboardArrowDown
import androidx.compose.material.icons.twotone.Refresh
import androidx.compose.material.icons.twotone.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.koin.java.KoinJavaComponent.get
import scooper.util.cursorHand
import scooper.util.cursorInput
import scooper.util.onHover
import scooper.viewmodels.AppsViewModel

@Composable
fun SearchBox() {
    Surface(
        Modifier.fillMaxWidth().height(90.dp),
        elevation = 3.dp,
        shape = MaterialTheme.shapes.large
    ) {
        Layout(content = {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                SearchBar()
                Button(onClick = {}, modifier = Modifier.layout { _, _ ->
                    layout(0, 0) { }
                }) {
                    Icon(Icons.TwoTone.Refresh, "", modifier = Modifier.size(18.dp))
                }
            }
        }) { measurables, constraints ->
            val placeables = measurables.map { measurable ->
                // Measure each child
                measurable.measure(constraints)
            }
            var yPosition = 0
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeables.forEach { placeable ->
                    // Position item on the screen
                    placeable.placeRelative(x = 0, y = yPosition)

                    // Record the y co-ord placed up to
                    yPosition += placeable.height
                }
            }

        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchBar() {
    val appsViewModel: AppsViewModel = get(AppsViewModel::class.java)
    val modifier = Modifier.padding(0.dp)
        .border(1.dp, MaterialTheme.colors.primary, shape = MaterialTheme.shapes.medium)
        .onHover { on ->
            if (on) border(
                2.dp,
                MaterialTheme.colors.secondary,
                shape = MaterialTheme.shapes.medium
            )
        }
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        val state by appsViewModel.container.stateFlow.collectAsState()
        val buckets = mutableListOf("")
        buckets.addAll(state.buckets.map { it.name })

        var expand by remember { mutableStateOf(false) }
        var selectedItem by remember { mutableStateOf(-1) }
        var bucket by remember { mutableStateOf("") }

        Spacer(Modifier.width(10.dp))

        Row(
            Modifier.cursorHand().clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { expand = true }, verticalAlignment = Alignment.CenterVertically
        ) {
            DropdownMenu(
                expand, onDismissRequest = { expand = false },
                modifier = Modifier.width(120.dp).cursorHand(),
                offset = DpOffset(x = (-10).dp, y = 6.dp)
            ) {
                buckets.forEachIndexed() { idx, title ->
                    var hover by remember { mutableStateOf(false) }
                    DropdownMenuItem(
                        onClick = {
                            expand = false
                            selectedItem = idx
                            bucket = title
                            appsViewModel.applyFilters("", bucket = bucket)
                        },
                        modifier = Modifier.sizeIn(maxHeight = 40.dp)
                            .background(color = if (hover) MaterialTheme.colors.primaryVariant else MaterialTheme.colors.surface)
                            .onHover { hover = it }
                    ) {
                        Text(
                            title.ifBlank { "All" },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (hover) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface,
                        )
                    }
                }
            }

            val color =
                if (selectedItem > 0) MaterialTheme.colors.onSurface else Color.LightGray

            Text(
                bucket.ifBlank { "Select bucket" },
                modifier = Modifier.width(100.dp),
                style = MaterialTheme.typography.body1.copy(color = color),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                Icons.TwoTone.KeyboardArrowDown,
                "",
            )
        }
        var query by remember { mutableStateOf(TextFieldValue()) }
        BasicTextField(
            query,
            onValueChange = { query = it },
            modifier = Modifier.padding(start = 5.dp, end = 10.dp)
                .defaultMinSize(120.dp).fillMaxWidth(0.4f).cursorInput().onPreviewKeyEvent {
                    if (it.key == Key.Enter) {
                        appsViewModel.applyFilters(query.text, bucket = bucket)
                        true
                    } else false
                },
            singleLine = true,
        )
        Button(
            onClick = {
                appsViewModel.applyFilters(query.text, bucket = bucket)
            },
            modifier = Modifier
                .height(40.dp)
                .padding(0.dp)
                .width(108.dp).cursorHand(),
            shape = RoundedCornerShape(
                topStart = 0.dp,
                bottomStart = 0.dp,
                topEnd = 4.dp,
                bottomEnd = 4.dp
            )
        ) {
            Icon(
                Icons.TwoTone.Search,
                "",
                modifier = Modifier.size(18.dp)
            )
            Text("Search")
        }
    }
}

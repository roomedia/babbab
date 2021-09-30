package com.roomedia.babbab.ui.main.userList

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.roomedia.babbab.ui.theme.BabbabTheme
import com.roomedia.babbab.ui.theme.Shapes

@Composable
fun SearchBar(state: MutableState<TextFieldValue>) {
    TextField(
        value = state.value,
        onValueChange = { state.value = it },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("User#Tag or E-mail") },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search Icon",
                modifier = Modifier
                    .padding(15.dp)
                    .size(24.dp)
            )
        },
        trailingIcon = {
            if (state.value == TextFieldValue("")) return@TextField
            IconButton(
                onClick = { state.value = TextFieldValue("") },
                content = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Search Text Clear",
                        modifier = Modifier
                            .padding(15.dp)
                            .size(24.dp)
                    )
                },
            )
        },
        singleLine = true,
        shape = Shapes.medium,
        colors = TextFieldDefaults.textFieldColors(),
    )
}

@Preview(name = "Light Theme")
@Preview(
    name = "Dark Theme",
    uiMode = UI_MODE_NIGHT_YES,
)
@Composable
fun SearchBarPreview() {
    val textState = remember { mutableStateOf(TextFieldValue("")) }
    BabbabTheme {
        Scaffold(topBar = { SearchBar(textState) }) {
        }
    }
}

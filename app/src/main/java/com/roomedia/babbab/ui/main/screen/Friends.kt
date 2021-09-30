package com.roomedia.babbab.ui.main.screen

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.TextFieldValue
import com.roomedia.babbab.ui.main.userList.SearchBar
import com.roomedia.babbab.ui.main.userList.UserList
import com.roomedia.babbab.ui.theme.BabbabTheme

interface Friends {
    @Composable
    fun AppCompatActivity.Friends() {
        val textState = remember { mutableStateOf(TextFieldValue("")) }
        BabbabTheme {
            Scaffold(topBar = { SearchBar(textState) }) {
                UserList(textState)
            }
        }
    }
}

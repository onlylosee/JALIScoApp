package com.team.jalisco.activities.contents

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import com.team.jalisco.domain.CustomMenuIcon
import com.team.jalisco.domain.model.CustomDrawerState
import com.team.jalisco.domain.model.opposite

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CartContent(
    modifier: Modifier = Modifier,
    drawerState: CustomDrawerState,
    onDrawerClick: (CustomDrawerState) -> Unit
){
    val focus = LocalFocusManager.current


    Scaffold(
        modifier = modifier
            .clickable(enabled = drawerState == CustomDrawerState.Opened) {
                onDrawerClick(CustomDrawerState.Closed)
            },
        topBar = {
            IconButton(onClick = {
                focus.clearFocus()
                onDrawerClick(drawerState.opposite())
            }) {
                Icon(
                    painter = CustomMenuIcon("menu"),
                    contentDescription = "Menu Icon"
                )
            }
        }
    ){ paddingValues ->  

    }
}
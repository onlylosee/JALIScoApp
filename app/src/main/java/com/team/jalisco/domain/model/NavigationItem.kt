package com.team.jalisco.domain.model

import com.team.jalisco.R

enum class NavigationItem(
    val title: String,
    val icon: Int
) {
    Home(
        icon = R.drawable.home,
        title = "Home"
    ),
    Profile(
        icon = R.drawable.profile,
        title = "Profile"
    ),
    Seller(
        icon = R.drawable.sell,
        title = "For sellers"
    ),
    Settings(
        icon = R.drawable.settings,
        title = "Settings"
    ),
    Logout(
        icon = R.drawable.logout,
        title = "LogOut"
    )

}
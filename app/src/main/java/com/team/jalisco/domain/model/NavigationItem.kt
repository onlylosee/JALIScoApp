package com.team.jalisco.domain.model

import com.team.jalisco.R

enum class NavigationItem(
    val title: String,
    val icon: Int
) {
    Home(
        icon = R.drawable.home,
        title = "Market"
    ),
    Profile(
        icon = R.drawable.profile,
        title = "Profile"
    ),
    Seller(
        icon = R.drawable.sell,
        title = "For sellers"
    ),
    Cart(
        icon = R.drawable.cart,
        title = "Cart"
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
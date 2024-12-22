package com.team.jalisco

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

@Composable
fun ProductsCard(
    modifier: Modifier,
    name: String,
    cost: String,
    image: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            elevation = 0.dp,
            modifier = Modifier
                .fillMaxSize(),
            backgroundColor = Color.Transparent,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)) {
                Card(
                    backgroundColor = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .aspectRatio(1f / 1f),
                    shape = RoundedCornerShape(12.dp),
                    elevation = 0.dp
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(image),
                        contentDescription = "Product Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(12.dp)
                            )
                    )


                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = name,
                    overflow = TextOverflow.Clip,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp,
                    maxLines = 2,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "$$cost",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

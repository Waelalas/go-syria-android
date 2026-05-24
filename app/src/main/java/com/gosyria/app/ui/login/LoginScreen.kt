package com.gosyria.app.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosyria.app.R

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    isLoading: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // الشعار (Logo)
        Image(
            painter = painterResource(id = R.drawable.logo_gosyria),
            contentDescription = "Go Syria Logo",
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 32.dp)
        )

        Text(
            text = "Welcome to Go Syria",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF001F3F), // لون أزرق داكن مقارب للشعار
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Explore Syria with ease",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFFD4AF37)) // لون ذهبي
        } else {
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF001F3F) // أزرق داكن
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // أيقونة غوغل بسيطة (يمكن استبدالها بصورة فعيلة)
                    Text(
                        text = "G  ",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Sign in with Google",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

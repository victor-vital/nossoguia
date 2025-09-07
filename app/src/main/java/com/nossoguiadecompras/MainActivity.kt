package com.nossoguiadecompras

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.FirebaseApp
import com.nossoguiadecompras.presentation.navigation.AppNav

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Inicializa Firebase (requer google-services.json em app/)
    FirebaseApp.initializeApp(this)

    setContent { AppNav() }
  }

}

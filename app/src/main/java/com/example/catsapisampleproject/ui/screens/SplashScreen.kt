package com.example.catsapisampleproject.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.catsapisampleproject.R
import com.example.catsapisampleproject.ui.components.viewmodels.SplashScreenUIState
import com.example.catsapisampleproject.ui.components.viewmodels.SplashScreenViewModel
import com.example.catsapisampleproject.ui.misc.FullScreenLoadingOverlay

@Composable
fun SplashScreen (
    viewModel: SplashScreenViewModel = hiltViewModel(),
    navigateToMain: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    SplashScreenContent(
        uiState,
        navigateToMain = navigateToMain
    )

}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SplashScreenContent(
    uiState: SplashScreenUIState,
    navigateToMain: () -> Unit = {}
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        GlideImage(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp).align(Alignment.Center),
            model = R.drawable.ic_cat_api_logo,
            contentScale = ContentScale.Fit,
            contentDescription = "The CatAPI Splash Screen logo"
        )

        if (uiState is SplashScreenUIState.Loading) FullScreenLoadingOverlay()

        if (uiState is SplashScreenUIState.NavigateToMain) {
            uiState.message.let { msg ->
                if (msg.isNotEmpty()) {
                    Toast.makeText(LocalContext.current, msg, Toast.LENGTH_LONG).show()
                }
            }
            navigateToMain()
        }

    }
}

@Preview
@Composable
fun SplashScreenContentPreview() {
    SplashScreenContent(
        uiState = SplashScreenUIState.NavigateToMain()
    )
}
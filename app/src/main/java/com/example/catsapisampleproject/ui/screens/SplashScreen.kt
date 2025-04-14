package com.example.catsapisampleproject.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
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
    navigateToMain: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()


    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is SplashScreenUIState.Error) {
            snackbarHostState.showSnackbar((uiState as SplashScreenUIState.Error).message)
        }
    }

    SplashScreenContent(
        uiState,
        navigateToMain = navigateToMain,
        onRetryClicked = { viewModel.onRetryClicked() },
        snackbarHostState
    )

}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SplashScreenContent(
    uiState: SplashScreenUIState,
    navigateToMain: () -> Unit = {},
    onRetryClicked: () -> Unit,
    snackbarHostState: SnackbarHostState
) {

    Scaffold (
        snackbarHost = {SnackbarHost(snackbarHostState) }
    ){ innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Column(
                modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GlideImage(
                    modifier = Modifier
                        .padding(24.dp)
                        .align(Alignment.CenterHorizontally),
                    model = R.drawable.ic_cat_api_logo,
                    contentScale = ContentScale.Inside,
                    contentDescription = "The CatAPI Splash Screen logo"
                )

                if (uiState is SplashScreenUIState.Error) {
                    Button(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(24.dp),
                        onClick = onRetryClicked,
                    ) {
                        Text(text = stringResource(R.string.retry))
                    }
                }
            }

            if (uiState is SplashScreenUIState.Loading) FullScreenLoadingOverlay()

            if (uiState is SplashScreenUIState.NavigateToMain) {
                uiState.message.let { msg ->
                    if (msg.isNotEmpty()) {
                        LaunchedEffect(msg) {
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                }
                navigateToMain()
            }

        }
    }


}

@Preview
@Composable
fun SplashScreenContentPreview() {
    SplashScreenContent(
        uiState = SplashScreenUIState.Error("Error"),
        navigateToMain = { },
        onRetryClicked = { },
        snackbarHostState = remember { SnackbarHostState() }
    )
}
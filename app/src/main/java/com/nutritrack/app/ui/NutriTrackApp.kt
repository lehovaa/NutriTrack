package com.nutritrack.app.ui

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nutritrack.app.R
import com.nutritrack.app.data.auth.AuthPolicy
import com.nutritrack.app.data.auth.AuthState
import com.nutritrack.app.model.Account
import com.nutritrack.app.model.AppLanguage
import com.nutritrack.app.model.NutritionProfile
import com.nutritrack.app.ui.account.AccountScreen
import com.nutritrack.app.ui.account.EditAccountScreen
import com.nutritrack.app.ui.account.AccountEditState
import com.nutritrack.app.ui.account.AccountViewModel
import com.nutritrack.app.ui.account.PhotoEdit
import com.nutritrack.app.ui.auth.AuthFormState
import com.nutritrack.app.ui.auth.AuthViewModel
import com.nutritrack.app.ui.auth.CreateAccountScreen
import com.nutritrack.app.ui.auth.SignInScreen
import com.nutritrack.app.ui.auth.VerifyEmailScreen
import com.nutritrack.app.ui.auth.WelcomeScreen
import com.nutritrack.app.ui.home.AddFoodScreen
import com.nutritrack.app.ui.home.HistoryScreen
import com.nutritrack.app.ui.home.HomeScreen
import com.nutritrack.app.ui.profile.NutritionProfileViewModel
import com.nutritrack.app.ui.profile.ProfileLoadState
import com.nutritrack.app.ui.profile.ProfileScreen
import com.nutritrack.app.ui.settings.LanguageScreen
import com.nutritrack.app.ui.settings.SettingsViewModel
import com.nutritrack.app.ui.settings.currentAppLanguage

/** Screens available while signed out. */
private object AuthRoutes {
    const val WELCOME = "welcome"
    const val SIGN_IN = "sign_in"
    const val CREATE_ACCOUNT = "create_account"
}

/** Screens available while signed in. */
private object MainRoutes {
    const val SETUP = "setup"
    const val HOME = "home"
    const val ACCOUNT = "account"
    const val EDIT_ACCOUNT = "edit_account"
    const val PROFILE = "profile"
    const val LANGUAGE = "language"
    const val ADD_FOOD = "add_food"
    const val HISTORY = "history"
}

/**
 * Top-level routing. Firebase's auth state decides which part of the app is shown, so signing
 * in/out and verifying the email switch screens without navigation calls from the auth layer:
 * signed out -> Welcome; signed in but unverified -> Verify Email (release builds only, see
 * [AuthPolicy]); otherwise -> main app, once the account's saved NutritionProfile has loaded.
 */
@Composable
fun NutriTrackApp(
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    accountViewModel: AccountViewModel = viewModel(factory = AccountViewModel.Factory),
    profileViewModel: NutritionProfileViewModel = viewModel(factory = NutritionProfileViewModel.Factory),
    settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val formState by authViewModel.formState.collectAsStateWithLifecycle()
    val verifyState by authViewModel.verifyState.collectAsStateWithLifecycle()
    val accountEditState by accountViewModel.editState.collectAsStateWithLifecycle()
    val accountPhoto by accountViewModel.accountPhoto.collectAsStateWithLifecycle()
    val profileState by profileViewModel.profileState.collectAsStateWithLifecycle()
    val languageTag = currentAppLanguage().tag

    when (val state = authState) {
        AuthState.Loading -> StartupScreen()
        AuthState.SignedOut -> AuthNavHost(
            formState = formState,
            onSignIn = authViewModel::signIn,
            onCreateAccount = { name, email, password ->
                authViewModel.createAccount(name, email, password, emailLanguageTag = languageTag)
            },
            onClearError = authViewModel::clearError
        )
        is AuthState.SignedIn -> {
            val loadedProfile = (profileState as? ProfileLoadState.Loaded)
                ?.takeIf { it.uid == state.account.uid } // never show another account's profile
            when {
                !AuthPolicy.canEnterApp(state.account) -> VerifyEmailScreen(
                    email = state.account.email.orEmpty(),
                    state = verifyState,
                    onCheckVerified = authViewModel::checkEmailVerified,
                    onResend = { authViewModel.resendVerificationEmail(languageTag) },
                    onSignOut = authViewModel::signOut
                )
                // Wait for the saved profile so Setup never flashes before Home.
                loadedProfile == null -> StartupScreen()
                else -> key(state.account.uid) {
                    MainNavHost(
                        account = state.account,
                        accountPhoto = accountPhoto,
                        accountEditState = accountEditState,
                        onSaveAccount = accountViewModel::save,
                        onResetAccountEdit = accountViewModel::resetEdit,
                        onLanguageSelected = settingsViewModel::setLanguage,
                        profile = loadedProfile.profile,
                        onProfileSaved = profileViewModel::saveProfile,
                        // The saved profile stays on the device for when this account returns.
                        onSignOut = authViewModel::signOut
                    )
                }
            }
        }
    }
}

/** Shown only while Firebase restores a possible saved session, to avoid flashing the wrong screen. */
@Composable
private fun StartupScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun AuthNavHost(
    formState: AuthFormState,
    onSignIn: (email: String, password: String) -> Unit,
    onCreateAccount: (name: String, email: String, password: String) -> Unit,
    onClearError: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = AuthRoutes.WELCOME) {
        composable(AuthRoutes.WELCOME) {
            WelcomeScreen(
                onCreateAccountClick = { navController.openFromWelcome(AuthRoutes.CREATE_ACCOUNT) },
                onSignInClick = { navController.openFromWelcome(AuthRoutes.SIGN_IN) }
            )
        }
        composable(AuthRoutes.SIGN_IN) {
            SignInScreen(
                formState = formState,
                onSignIn = onSignIn,
                onCreateAccountClick = { navController.openFromWelcome(AuthRoutes.CREATE_ACCOUNT) },
                onBack = { navController.popBackTo(AuthRoutes.WELCOME) },
                onClearError = onClearError
            )
        }
        composable(AuthRoutes.CREATE_ACCOUNT) {
            CreateAccountScreen(
                formState = formState,
                onCreateAccount = onCreateAccount,
                onSignInClick = { navController.openFromWelcome(AuthRoutes.SIGN_IN) },
                onBack = { navController.popBackTo(AuthRoutes.WELCOME) },
                onClearError = onClearError
            )
        }
    }
}

@Composable
private fun MainNavHost(
    account: Account,
    accountPhoto: Uri?,
    accountEditState: AccountEditState,
    onSaveAccount: (displayName: String, photoEdit: PhotoEdit) -> Unit,
    onResetAccountEdit: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    profile: NutritionProfile?,
    onProfileSaved: (NutritionProfile) -> Unit,
    onSignOut: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    // Chosen once per signed-in session: without a profile, setup comes first and Home is never below it.
    val startDestination = rememberSaveable {
        if (profile == null) MainRoutes.SETUP else MainRoutes.HOME
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(MainRoutes.SETUP) {
            ProfileScreen(
                initialProfile = null,
                onSave = { saved ->
                    onProfileSaved(saved)
                    navController.finishSetup()
                },
                // No back arrow in setup mode; system back simply leaves the app.
                onBack = {}
            )
        }
        composable(MainRoutes.HOME) {
            HomeScreen(
                accountPhoto = accountPhoto,
                onAccountClick = {
                    navController.navigate(MainRoutes.ACCOUNT) { launchSingleTop = true }
                },
                onAddFoodClick = {
                    navController.navigate(MainRoutes.ADD_FOOD) { launchSingleTop = true }
                },
                onHistoryClick = {
                    navController.navigate(MainRoutes.HISTORY) {
                        popUpTo(MainRoutes.HOME)
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(MainRoutes.HISTORY) {
            HistoryScreen(
                onDiaryClick = { navController.popBackTo(MainRoutes.HOME) },
                onAddFoodClick = {
                    navController.navigate(MainRoutes.ADD_FOOD) { launchSingleTop = true }
                }
            )
        }
        composable(MainRoutes.ADD_FOOD) {
            AddFoodScreen(onClose = { navController.popBackStack() })
        }
        composable(MainRoutes.ACCOUNT) {
            AccountScreen(
                account = account,
                photo = accountPhoto,
                currentLanguage = currentAppLanguage(),
                onEditAccountClick = {
                    onResetAccountEdit()
                    navController.navigate(MainRoutes.EDIT_ACCOUNT) { launchSingleTop = true }
                },
                onEditNutritionProfileClick = {
                    navController.navigate(MainRoutes.PROFILE) { launchSingleTop = true }
                },
                onLanguageClick = {
                    navController.navigate(MainRoutes.LANGUAGE) { launchSingleTop = true }
                },
                // Signing out flips the auth state, which replaces this whole graph with Welcome.
                onSignOutClick = onSignOut,
                onBack = { navController.popBackTo(MainRoutes.HOME) }
            )
        }
        composable(MainRoutes.EDIT_ACCOUNT) {
            EditAccountScreen(
                account = account,
                currentPhoto = accountPhoto,
                editState = accountEditState,
                onSave = onSaveAccount,
                onSaved = {
                    onResetAccountEdit()
                    navController.popBackTo(MainRoutes.ACCOUNT)
                },
                onBack = { navController.popBackTo(MainRoutes.ACCOUNT) }
            )
        }
        composable(MainRoutes.LANGUAGE) {
            LanguageScreen(
                currentLanguage = currentAppLanguage(),
                onLanguageSelected = onLanguageSelected,
                onBack = { navController.popBackTo(MainRoutes.ACCOUNT) }
            )
        }
        composable(MainRoutes.PROFILE) {
            ProfileScreen(
                initialProfile = profile,
                onSave = { saved ->
                    onProfileSaved(saved)
                    navController.popBackTo(MainRoutes.ACCOUNT)
                },
                onBack = { navController.popBackTo(MainRoutes.ACCOUNT) }
            )
        }
    }
}

/** Opens a sign-in/create-account form with Welcome directly beneath it, so back returns to Welcome. */
private fun NavHostController.openFromWelcome(route: String) {
    navigate(route) {
        popUpTo(AuthRoutes.WELCOME)
        launchSingleTop = true
    }
}

/** Replaces the setup screen with Home so back from Home exits instead of returning to setup. */
private fun NavHostController.finishSetup() {
    if (currentDestination?.route != MainRoutes.SETUP) return // ignore repeated taps
    navigate(MainRoutes.HOME) {
        popUpTo(MainRoutes.SETUP) { inclusive = true }
    }
}

/** Pops to [route] without ever emptying the back stack (e.g. on a double tap). */
private fun NavHostController.popBackTo(route: String) {
    popBackStack(route, inclusive = false)
}

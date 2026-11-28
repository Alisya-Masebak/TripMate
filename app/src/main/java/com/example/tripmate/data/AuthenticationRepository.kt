package com.example.tripmate.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed interface RegistrationResult {
    data object EmailConfirmationRequired : RegistrationResult

    data class SignedIn(
        val profile: UserProfile
    ) : RegistrationResult
}

class AuthenticationRepository {

    private val supabase = SupabaseClient.client

    // Register a new Traveller account.
    suspend fun register(
        fullName: String,
        email: String,
        password: String
    ): RegistrationResult {

        supabase.auth.awaitInitialization()

        supabase.auth.signUpWith(Email) {

            this.email = email
            this.password = password

            // The database trigger uses this metadata
            // to create the user's profile.
            data = buildJsonObject {
                put("full_name", fullName)
            }
        }

        // If email confirmation is disabled, Supabase
        // may establish a session immediately.
        val currentUser = supabase.auth.currentUserOrNull()

        return if (currentUser == null) {

            RegistrationResult.EmailConfirmationRequired

        } else {

            RegistrationResult.SignedIn(
                getProfile(currentUser.id)
            )
        }
    }

    // Sign in using email and password.
    suspend fun signIn(
        email: String,
        password: String
    ): UserProfile {

        supabase.auth.awaitInitialization()

        supabase.auth.signInWith(Email) {

            this.email = email
            this.password = password
        }

        val currentUser = supabase.auth.currentUserOrNull()
            ?: throw IllegalStateException(
                "Unable to retrieve the signed-in user."
            )

        return getProfile(currentUser.id)
    }

    // Retrieve the currently authenticated user's profile.
    suspend fun getCurrentProfile(): UserProfile? {

        supabase.auth.awaitInitialization()

        val currentUser = supabase.auth.currentUserOrNull()
            ?: return null

        return getProfile(currentUser.id)
    }

    // Retrieve the profile and validate the assigned role.
    private suspend fun getProfile(
        userId: String
    ): UserProfile {

        val profile = supabase
            .from("profiles")
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeSingle<UserProfile>()

        if (
            profile.role != "traveller" &&
            profile.role != "admin"
        ) {
            throw IllegalStateException(
                "Your account has an invalid user role."
            )
        }

        return profile
    }

    // Sign out from Supabase.
    suspend fun signOut() {
        supabase.auth.signOut()
    }
}

package com.example.tripmate.data

import com.example.tripmate.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.SupabaseClient as SupabaseSdkClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {

    // Initialise the client only when it is first requested.
    val client: SupabaseSdkClient by lazy(
        LazyThreadSafetyMode.SYNCHRONIZED
    ) {

        val url = BuildConfig.SUPABASE_URL.trim()
        val publishableKey =
            BuildConfig.SUPABASE_PUBLISHABLE_KEY.trim()

        require(url.isNotBlank()) {
            "Supabase URL is missing. Check local.properties."
        }

        require(publishableKey.isNotBlank()) {
            "Supabase publishable key is missing. Check local.properties."
        }

        createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = publishableKey
        ) {

            // Authentication
            install(Auth)

            // Database access
            install(Postgrest)

            // Image and file storage
            install(Storage)
        }
    }
}

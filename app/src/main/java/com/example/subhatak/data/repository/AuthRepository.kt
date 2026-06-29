package com.example.subhatak.data.repository

import com.example.subhatak.data.model.User
import com.example.subhatak.ui.auth.Result
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        name: String,
        phoneNumber: String,
        profilePictureUrl: String?
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    name = name,
                    phoneNumber = phoneNumber,
                    profilePictureUrl = profilePictureUrl,
                    createdAt = System.currentTimeMillis()
                )

                // Save user to Firestore
                firestore.collection("SubhaTak")
                    .document(firebaseUser.uid)
                    .set(user)
                    .await()

                Result.Success(user)
            } ?: Result.Error("Unknown Error")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown Error")
        }
    }

    suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                val userDoc = firestore
                    .collection("SubhaTak")
                    .document(firebaseUser.uid)
                    .get()
                    .await()
                if (userDoc.exists()) {
                    Result.Success(userDoc.toObject(User::class.java)!!)
                } else {
                    Result.Error("User Not Found")
                }
            } ?: Result.Error("Unknown Error")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown Error")
        }
    }

    fun signOut() {
        auth.signOut()

    }

    suspend fun getCurrentUser(): User? = auth.currentUser?.let { firebaseUser ->
        try {
            val userDoc = firestore.collection("SubhaTak")
                .document(firebaseUser.uid)
                .get()
                .await()
            if (userDoc.exists()) {
                userDoc.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<User> =
        withContext(Dispatchers.IO) {
            try {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                val result = auth.signInWithCredential(credential).await()

                result.user?.let { firebaseUser ->
                    val userDoc = firestore.collection("SubhaTak")
                        .document(firebaseUser.uid)
                        .get()
                        .await()

                    if (userDoc.exists()) {
                        Result.Success(userDoc.toObject(User::class.java)!!)
                    } else {
                        val user = User(
                            uid = firebaseUser.uid,
                            email = firebaseUser.email ?: "",
                            name = firebaseUser.displayName ?: "",
                            phoneNumber = "",
                            profilePictureUrl = firebaseUser.photoUrl?.toString(),
                            createdAt = System.currentTimeMillis()
                        )

                        firestore.collection("SubhaTak")
                            .document(firebaseUser.uid)
                            .set(user)
                            .await()
                        Result.Success(user)
                    }
                } ?: Result.Error("Unknown Error")
            } catch (e: Exception) {
                Result.Error(e.message ?: "Unknown Error")
            }
        }
}



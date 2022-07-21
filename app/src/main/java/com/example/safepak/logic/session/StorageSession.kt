package com.example.safepak.logic.session

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


object StorageSession {
    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    private val currentUserRef: StorageReference
        get() = storageInstance.reference.child(FirebaseAuth.getInstance().currentUser?.uid ?: throw NullPointerException("UID is null."))

    fun uploadProfilePhoto(imageBytes: ByteArray, onSuccess: (imagePath: String) -> Unit) {
        val ref = currentUserRef.child("profilePictures/${FirebaseSession.userID}")
        ref.putBytes(imageBytes)
            .addOnSuccessListener {
                onSuccess(ref.path)
            }
    }

    fun uploadFacePhotos(imageBytes: ByteArray, callid : String, filename : String, onSuccess: (imagePath: String) -> Unit) {
        val ref = currentUserRef.child("faceExtracts/$callid/${filename}")
        ref.putBytes(imageBytes)
            .addOnSuccessListener {
                onSuccess(ref.path)
            }
    }

    fun pathToReference(path: String) = storageInstance.getReference(path)
}

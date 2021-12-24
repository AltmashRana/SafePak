
import com.example.safepak.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


object FirebaseSession {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val userID : String? = currentUser?.id
        get() {
            return field
        }

    private val currentUser: DocumentReference?
        get() = FirebaseAuth.getInstance().currentUser?.uid?.let {
            firestoreInstance.collection("users").document(
                it
            )
        }

//    fun initCurrentUserIfFirstTime(onComplete: () -> Unit) {
//        currentUser.get().addOnSuccessListener { documentSnapshot ->
//            if (!documentSnapshot.exists()) {
//                val newUser = User(
//                    FirebaseAuth.getInstance().currentUser?.uid ?: "",
//                    FirebaseAuth.getInstance().currentUser?., null, mutableListOf()
//                )
//                currentUser.set(newUser).addOnSuccessListener {
//                    onComplete()
//                }
//            } else
//                onComplete()
//        }
//    }

    fun updateCurrentUser(firstname: String = "", lastname: String = "", gender: String = "",bloodgroup: String = "",password: String = "",cnic: String = "",email: String = "",willing: Boolean = false, profilePicturePath: String? = null) {
        val userFieldMap = mutableMapOf<String, Any>()
        if (firstname.isNotBlank()) userFieldMap["firstname"] = firstname
        if (lastname.isNotBlank()) userFieldMap["lastname"] = lastname
        if (email.isNotBlank()) userFieldMap["email"] = email
        if (password.isNotBlank()) userFieldMap["password"] = password
        if (gender.isNotBlank()) userFieldMap["gender"] = gender
        if (bloodgroup.isNotBlank()) userFieldMap["bloodgroup"] = bloodgroup
        if (cnic.isNotBlank()) userFieldMap["cnic"] = cnic
        userFieldMap["willingToDonate"] = willing
        if (profilePicturePath != null)
            userFieldMap["img"] = profilePicturePath
        currentUser?.update(userFieldMap)
    }

    fun getCurrentUser(onComplete: (User) -> Unit) {
        currentUser?.get()?.addOnSuccessListener {
            onComplete(it.toObject(User::class.java)!!)
        }
    }
}
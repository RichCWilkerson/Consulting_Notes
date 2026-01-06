
# Firebase Authentication
- tools has firebase authentication built into android studio
- authenticate using custom authentication (email/password, google sign-in, etc)

- skip step 3 server keys
- remember to enable internet permission in manifest

- this is a documentation follow-along and implementation practice

- NOTE: constraints has a magic button like nav_graph that creates constraints for you
- tools:text="Email" -> only in design mode, not in runtime (for development purposes)

- on Firebase website we enable the SDK to allow email/password authentication and any other methods we want (google, facebook, etc)

## Setup    
```kotlin
// Activity.kt
class MainActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // grabs auth SDK (libraries in build.gradle) and initializes it
        // lets us use tools like signInWithEmailAndPassword, createUserWithEmailAndPassword, etc and analytics
        auth = Firebase.auth
        
        if (auth.currentUser != null) {
            // user is signed in
            // cache user details in currentUser
            // set a timer to auto sign out after a certain period of time for security purposes
        } else {
            // no user is signed in
        }
        
        binding.loginBtn.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val password = binding.passwordEt.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password must not be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // sign in existing user
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        // this gives us the user details like uid, email, etc
                        val user = auth.currentUser
                        // updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                        // updateUI(null)
                    }
                }
        }
    }
    
    // we are doing this here because what happens next is onResume() which is when the user can interact with the app and UI is drawn
    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            // user is signed in
        } else {
            // no user is signed in
        }
    }
}


```


--- 


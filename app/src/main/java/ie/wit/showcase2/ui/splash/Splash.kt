package ie.wit.showcase2.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import ie.wit.showcase2.R
import ie.wit.showcase2.ui.auth.Login

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)
        val handler = Handler(Looper.getMainLooper())
        val splashTimeOut:Long = 3000 // Hold splash screen for 30 seconds before moving to portfolio list (home)
        handler.postDelayed({
            val intent = Intent(this, Login::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        },splashTimeOut)
    }
}
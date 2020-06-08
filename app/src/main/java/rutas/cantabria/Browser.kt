package rutas.cantabria

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import kotlinx.android.synthetic.main.brouser.*
import java.lang.Exception

class Browser : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.brouser)

        try {
            supportActionBar?.hide()
        } catch (e:Exception){}

        webView.webChromeClient = WebChromeClient()
        webView.settings.javaScriptEnabled = true
        val data = intent.data
        webView.loadUrl(data.toString())
    }
}

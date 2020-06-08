package rutas.cantabria
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.rutas_cantabria.*
import org.json.JSONObject
import rutas.cantabria.util.Custom
import java.net.URL
class RotasCannabis : AppCompatActivity() {
    lateinit var listRoutesName: Array<String>
    lateinit var listDifficulty: Array<String>
    lateinit var listImg: Array<String>
    lateinit var listDistance: Array<String>
    lateinit var listTime: Array<String>
    lateinit var json: JSONObject
    private lateinit var mAdView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rutas_cantabria)

        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView2)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                generalOne.post{
                      val heidhtAdview = mAdView.height + 1
                      val layoutParams = linearLayout.layoutParams as RelativeLayout.LayoutParams
                      layoutParams.setMargins(0,0, 0,heidhtAdview)
                      linearLayout.layoutParams = layoutParams
                  }
            }
        }

        json = JSONObject("""{"name":"test name", "age":25}""")

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 121)
        }

        val readRadioStation = Thread{
            try{
                val response     = URL("https://diseno-desarrollo-web-app-cantabria.github.io/rutas.cantabria/rutas_info.js").readText()
                val res          = JSONObject(response)
                val name= res.getJSONArray("name")
                listRoutesName = Array( name.length()){name.getString(it) }
                val difficulty = res.getJSONArray("dfficulty")
                listDifficulty  = Array(difficulty.length()){ difficulty.getString(it) }
                val img= res.getJSONArray("img")
                listImg  = Array(img.length()){ img.getString(it) }
                val distance= res.getJSONArray("distance")
                listDistance  = Array(distance.length()){ distance.getString(it) }
                val time= res.getJSONArray("time")
                listTime  = Array(time.length()){ time.getString(it) }

                generalOne.post{
                    listView.adapter = Custom(this, listRoutesName, listDifficulty, listImg, listDistance, listTime)
                }
            } catch (e:Exception){ Log.d("tag", "Error -> " + e.message)}
        }

        val second = Thread {
            try{
                val response = URL("https://diseno-desarrollo-web-app-cantabria.github.io/rutas.cantabria/coordinates.js").readText()
                json = JSONObject(response)
                val res0= json.getJSONObject("1")
                 Log.d("tag", "response = $response")
            } catch (e:Exception){
                Log.d("tag", "" + e.message)
            }
        }

        second.start()
        readRadioStation.start()


        listView.setOnItemClickListener { parent, view, position, id ->
            val data = (json[position.toString()]).toString()
            val i = Intent(this, Maps::class.java)
            i.apply {
                putExtra("position", position)
                putExtra("title", listRoutesName[position])
                putExtra("data", data)
            }
            startActivity(i)
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.one_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.coment ->{
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=rutas.cantabria")))
                return true }
            R.id.share ->{
                val s     = Intent()
                s.action = Intent.ACTION_SEND
                s.type   = "text/plain"
                val name = resources.getString(R.string.app_name)
                s.putExtra(Intent.EXTRA_TEXT, "$name \n https://play.google.com/store/apps/details?id=rutas.cantabria")
                startActivity(Intent.createChooser(s, ""))
                return true }
            R.id.web ->{
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://rutas-por-cantabria.blogspot.com/")))
                return true }
        }
        return super.onOptionsItemSelected(item)
    }
}

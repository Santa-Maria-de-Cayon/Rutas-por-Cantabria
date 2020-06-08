package rutas.cantabria
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.RelativeLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import org.json.JSONObject
import rutas.cantabria.util.App
import java.lang.Exception
import java.net.URL

class Maps : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    lateinit var places: MutableList<LatLng>
    var query = "none"
    var position = -123
    private lateinit var mAdView : AdView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = object: AdListener() {
            override fun onAdLoaded() {
              //  relativeLayoutGeneral.post{
              //      val heidhtAdview = mAdView.height + 7
              //      val layoutParams = linearLayout.layoutParams as RelativeLayout.LayoutParams
              //      layoutParams.setMargins(0,7, 0,heidhtAdview)
              //      linearLayout.layoutParams = layoutParams
              //  }
            }
        }

        App.km = 0
        App.time = 0
        places = arrayListOf()
        position = intent.getIntExtra("position", -121)
        title   = intent.getStringExtra("title") ?: ""

        try{
            val data = JSONObject (intent.getStringExtra("data") ?: "")
            val keys = data.keys()

            while (keys.hasNext()) {
                val key = keys.next()
                if (data.get(key) is JSONObject) {
                    val test = data.getJSONObject(key)
                  // Log.d("tag", "a Maps = " +  test.getString("a") )
                  // Log.d("tag", "b Maps = " +  test.getString("b") )
                    places.add(LatLng(test.getString("a").toDouble(), test.getString("b").toDouble()))
                    if(query == "none") query = test.getString("a") + "," + test.getString("b")
                 }
            }
        } catch (e:Exception){
            Log.d("tag", "Error = " + e.message)
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 121)
        }
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.setting_file, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.go ->{
                val path = "https://www.google.com/maps/search/?api=1&query=$query"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(path)))
                return true }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap = googleMap
            mMap.isMyLocationEnabled = true

            val location1 = places[0]
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location1))
            val zoomLevel = 13.0f // < 21
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location1, zoomLevel))

            for (i in 0 until places.size - 1) {
                if(i == 0 ){
                   val options = MarkerOptions().position(places[i]).title("Start - Finish")
                   options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                   mMap.addMarker(options)
                } else {
                    val options = MarkerOptions().position(places[i]).title(i.toString())
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    mMap.addMarker(options)
                }
                val url = getDirectionUrl(places[i], places[i + 1])
                GetDataRequest(url, mMap).execute()
            }

        }
    }


    private fun getDirectionUrl(origin: LatLng, dest:LatLng): String{
        val a = "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=walking&key=AIzaSyBJcJoJrGaUb_Gx1GJ2Swnh2N3gdF926Gw"
        Log.d("t", "url = " + a)
        return  a
    }


    class GetDataRequest(private val url: String, val mMap: GoogleMap) : AsyncTask<Void, Void, List<List<LatLng>>>() {
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val data = URL(url).readText()
            val result = ArrayList<List<LatLng>>()
            try{
                val resObj = Gson().fromJson(data, GoogleMapDTO::class.java)
                                                                     App.km += resObj.routes[0].legs[0].distance.value
                                                                     Log.d("t", "duration === " + resObj.routes[0].legs[0].distance.value)
                                                                     App.time += resObj.routes[0].legs[0].duration.value
                                                                     Log.d("t", "duration === " + resObj.routes[0].legs[0].duration.value)
                val path = ArrayList<LatLng>()
                for(i in 0 until resObj.routes[0].legs[0].steps.size){
                  //   val startLatLng = LatLng(resObj.routes[0].legs[0].steps[i].start_location.lat.toDouble(), resObj.routes[0].legs[0].steps[i].start_location.lng.toDouble())
                  //   path.add(startLatLng)
                  //   val endLatLng = LatLng(resObj.routes[0].legs[0].steps[i].end_location.lat.toDouble(), resObj.routes[0].legs[0].steps[i].end_location.lng.toDouble())
                    path.addAll(decodePolyline(resObj.routes[0].legs[0].steps[i].polyline.points ))
                //    Log.d("t", "ERROR + + + " + resObj.routes[0].legs[0].steps[i].distance.text)
                }
                Log.d("t", "total time === " + App.time )
                Log.d("t", "total disatance === " + App.km )
                result.add(path)
            }catch (e:Exception){
                Log.d("t", "ERROR + + + " + e.message)
            }
            return result
        }
        override fun onPostExecute(result: List<List<LatLng>>?) {
            val lineOption = PolylineOptions()
            for( i in result!!.indices){
                lineOption.addAll(result[i])
                lineOption.width(4F)
                lineOption.color(Color.BLUE)
                lineOption.geodesic(true)
            }
            mMap.addPolyline(lineOption)
        }
        fun decodePolyline(encoded: String) : List<LatLng>{
            val poly = ArrayList<LatLng>()
            var index = 0
            val len = encoded.length
            var lat = 0
            var lng = 0

            while(index < len){
                var b : Int
                var shift = 0
                var result = 0
                do{
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlat = if(result and 1 != 0) (result shr 1).inv() else result shr 1
                lat += dlat

                shift = 0
                result = 0
                do{
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlng = if(result and 1 != 0) (result shr 1).inv() else result shr 1
                lng += dlng

                val latLng = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
              //  val hm = java.util.HashMap<String, String>()
              //  hm["lat"] = (lat.toDouble() / 1E5).toString()
              //  hm["lng"] = (lng.toDouble() / 1E5).toString()
                poly.add(latLng)
            }
            return poly
        }
    }


    class GoogleMapDTO{
        var routes = ArrayList<Routes>()
    }

    class Routes{
        var legs = ArrayList<Legs>()
    }

    class Legs{
        var distance = Distance()
        var duration = Duration()
        var end_adress = ""
        var start_adress = ""
        var end_location = Location()
        var start_location = Location()
        var steps = ArrayList<Steps>()
    }

    class Steps{
        var distance = Distance()
        var duration = Duration()
        var end_adress = ""
        var start_adress = ""
        var end_location = Location()
        var start_location = Location()
        var polyline = Polyline()
        var travel_mode = ""
        var maneuver = ""
    }

    class Duration{
        var text = ""
        var value = 0
    }

    class Distance{
        var text = ""
        var value = 0
    }

    class Polyline{
        var points = ""
    }

    class Location{
        var lat = ""
        var lng = ""
    }






































}

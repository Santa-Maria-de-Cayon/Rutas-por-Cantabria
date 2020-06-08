package rutas.cantabria.util

import android.app.Application

class App : Application() {

    companion object{
        var km = 0
        var time = 0
    }

    override fun onCreate() {
        super.onCreate()

        km = 0
        time = 0
    }
}
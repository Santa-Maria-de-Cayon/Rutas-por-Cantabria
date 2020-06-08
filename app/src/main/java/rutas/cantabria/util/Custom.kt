package rutas.cantabria.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import rutas.cantabria.R
import java.lang.Exception

class Custom (private val c: Context, private val listRoutesName: Array<String>,private val listDifficulty: Array<String>, private val listImg: Array<String>, private val listDistance : Array<String>,  private val listTime:  Array<String>) : BaseAdapter() {
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
        val layotInflater   = LayoutInflater.from(c)
        val rowMain         = layotInflater.inflate(R.layout.one , viewGroup, false)

        val imageView =  rowMain.findViewById<ImageView>(R.id.imgImageView)
        try{
            Picasso.get().load(listImg[position]).into(imageView)
        } catch (e: Exception){
            Log.d("tag", "" + e.message)
        }


        val title     = rowMain.findViewById<TextView>(R.id.titleTextView)
        title.text = listRoutesName[position]

        val difficulty     = rowMain.findViewById<TextView>(R.id.difficulty)
        difficulty.text = listDifficulty[position]

        val distance     = rowMain.findViewById<TextView>(R.id.distance)
        distance.text = listDistance[position]

        val time     = rowMain.findViewById<TextView>(R.id.time)
        time.text = listTime[position]

        return rowMain
    }

    override fun getItem(position: Int): Any {
        return listRoutesName[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return listRoutesName.size
    }
}
package com.example.lyricist

import android.os.Bundle
import android.text.Editable
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity(){
    companion object {
        private var id_artist: Int = 0
        private var id_track: Int = 0
        private var id_album: Int = 0
        private var lyrics:String = ""
    }
    private lateinit var submit:Button
    private lateinit var lyricsView: TextView
    private lateinit var input: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        submit = findViewById(R.id.button)
        input = findViewById(R.id.inputName)
        submit.setOnClickListener{
            val text = input.text
            GlobalScope.launch(Dispatchers.IO) {
                apicall(text)
            }
        }

    }
    private fun apicall(text: Editable) = try {
        val list = text.split(" ")
        val n = list.count()
        var url = "https://api.happi.dev/v1/music?q="
        for (i in 0 until n - 1) {
            url += (list[i] + "%20")
        }
        url += list[n - 1]
        url += "&limit=&apikey=1de270IreLImImBPWfrQJGxXC3AvlF1DWzFys2cwcwgspV7ye9zNDcv2&type=&lyrics=1"
        val client1 = OkHttpClient()
        val request1 = Request.Builder()
            .url(url)
            .build()

        //First call to check if track exists & stores track, artist, album IDs
        client1.newCall(request1).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Please try again", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()
                val obj = JSONObject(body)
                val length = obj.getInt("length")
                if (length == 0) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Try a different song", Toast.LENGTH_LONG)
                            .show()
                    }
                }
                else{
                    val details: JSONObject = obj.optJSONArray("result").getJSONObject(0)
                    id_artist = details.getInt("id_artist")
                    id_track = details.getInt("id_track")
                    id_album = details.getInt("id_album")
                }

                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Track Found!", Toast.LENGTH_LONG).show()
                }
            }
        })
        //Second call for lyrics using track, artist, album IDs
        var url2 = "https://api.happi.dev/v1/music/artists/"
        url2 += "$id_artist/albums/$id_album/tracks/$id_track"
        url2 += "/lyrics?apikey=1de270IreLImImBPWfrQJGxXC3AvlF1DWzFys2cwcwgspV7ye9zNDcv2"
        val client2 = OkHttpClient()
        val request2 = Request.Builder().url(url2).build()
        client2.newCall(request2).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Please try again", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()
                val obj = JSONObject(body)
                val lyricsArray: JSONObject? = obj.optJSONObject("result")
                if (lyricsArray != null) {
                    lyrics = lyricsArray.optString("lyrics")
                }
                runOnUiThread { setLyrics(lyrics) }
            }
        })

    }
    catch (e:Exception){
        runOnUiThread {
            Toast.makeText(this@MainActivity, "API is down", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun setLyrics(lyrics: String) {
        lyricsView = findViewById(R.id.lyricsView)
        lyricsView.movementMethod = ScrollingMovementMethod()
        lyricsView.setText(lyrics)
    }
}
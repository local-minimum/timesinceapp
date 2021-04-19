package se.localminimum.timesince

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ListTrackersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_trackers)

        GlobalScope.launch {
            loadTrackers()
        }
    }

    fun loadTrackers() {
        val db = DatabaseBuilder.getInstance(applicationContext)
        val dao = db.timeSinceTrackerDao()
        val trackers = dao.getAll()
        Log.i("LIST", "$trackers")
    }
}
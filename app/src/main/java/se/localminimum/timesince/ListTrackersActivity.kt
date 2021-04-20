package se.localminimum.timesince

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.database.sqlite.SQLiteConstraintException
import android.widget.*
import kotlinx.coroutines.*
import java.util.ArrayList
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.suspendCoroutine

class ListTrackersActivity : AppCompatActivity(), CoroutineScope {

    lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
    val ioContext: CoroutineContext
        get()  = Dispatchers.IO + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        setContentView(R.layout.activity_list_trackers)

        loadTrackers()

        val newTrackerName = findViewById<EditText>(R.id.newTrackerNameInput)
        newTrackerName.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                if (!hasFocus) return
                (v as EditText).apply {
                    if (text.toString() == getString(R.string.task_global)) {
                        text.clear()
                    }
                }
            }
        }

        val addTrackerBtn = findViewById<View>(R.id.btnAddTracker)
        addTrackerBtn.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {
                addNewTracker(newTrackerName, v!!)
            }
        })
    }

    fun addNewTracker(newTrackerName: EditText, addBtn: View) = launch {
        newTrackerName.apply {
            when (val t = text.toString()) {
                "" -> {
                    newTrackerName.requestFocus()
                    return@launch
                }
                getString(R.string.task_global) -> {
                    showActionDialog(this.context, "Error", "Activity '$t' is reserved and may not be created")
                    return@launch
                }
            }
            val newTracker = text.toString()

            // Disable inputs until inserted
            isEnabled = false
            addBtn.isEnabled = false

            val deferred: Deferred<Pair<Boolean, String>> = async(context = ioContext) {
                Log.i("NEW-TRACKER", newTracker)
                try {
                    DatabaseBuilder.getInstance(applicationContext).timeSinceTrackerDao().insertAll(
                            TimeSinceTracker(name = newTracker)
                    )
                    loadTrackers()
                    Pair(true, "")
                } catch (e: SQLiteConstraintException) {
                    Log.w("NEW-TRACKER", "Duplicate tracker: ${e.message}")
                    Pair(false, "Activity already exists")
                }
            }
            val result: Pair<Boolean, String> = deferred.await()
            isEnabled = true
            addBtn.isEnabled = true
            if (!result.first) {
                showActionDialog(this.context , "Error", result.second)
            } else {
                text.clear()
            }
        }
    }

    private suspend fun showActionDialog(context: Context, dialogTitle: String, dialogMessage: String) = suspendCoroutine<Int> {
        android.app.AlertDialog.Builder(context).apply {
            title = dialogTitle
            setMessage(dialogMessage)
            setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            show()
        }
    }

    fun loadTrackers() = launch {
        val deferred: Deferred<List<TimeSinceTracker>> = async(context = ioContext) {
            val db = DatabaseBuilder.getInstance(applicationContext)
            val dao = db.timeSinceTrackerDao()
            dao.getAll()
        }
        val trackers: List<TimeSinceTracker> = deferred.await()
        Log.i("LIST", "$trackers")
        findViewById<ScrollView>(R.id.trackersScrollView).apply {
        // findViewById<LinearLayout>(R.id.trackersLinearLayout).apply {
            removeAllViewsInLayout()
            addFocusables(trackers.map<TimeSinceTracker, View> {
                val t = TextView(applicationContext)
                t.text = it.name
                t
            } as ArrayList<View>, View.FOCUS_UP)
            Log.i("LIST", "Listed trackers")
        }
    }
}
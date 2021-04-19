package se.localminimum.timesince

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TimeSinceTrackerDAO {
    @Query("SELECT * FROM timesincetracker ORDER BY LOWER(name) ASC")
    fun getAll(): List<TimeSinceTracker>

    @Insert
    fun insertAll(vararg trackers: TimeSinceTracker)

    @Delete
    fun delete(tracker: TimeSinceTracker)
}
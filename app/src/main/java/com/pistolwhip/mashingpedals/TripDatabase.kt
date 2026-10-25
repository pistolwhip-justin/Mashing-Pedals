package com.pistolwhip.mashingpedals
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
data class SavedTrip(val date:String,val distance:Double,val seconds:Long,val maxSpeed:Double)
class TripDatabase(c:Context):SQLiteOpenHelper(c,"mashing_pedals.db",null,1){
 override fun onCreate(db:SQLiteDatabase){db.execSQL("CREATE TABLE trips(id INTEGER PRIMARY KEY AUTOINCREMENT,completed_at TEXT NOT NULL,distance_km REAL NOT NULL,active_seconds INTEGER NOT NULL,max_speed REAL NOT NULL,average_speed REAL,max_heart INTEGER,average_heart REAL,max_cadence INTEGER,average_cadence REAL)")}
 override fun onUpgrade(db:SQLiteDatabase,oldVersion:Int,newVersion:Int){}
 fun saveTrip(distance:Double,seconds:Long,maxSpeed:Double,averageSpeed:Double?,maxHeart:Int?,averageHeart:Double?,maxCadence:Int?,averageCadence:Double?){val v=ContentValues().apply{put("completed_at",SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US).format(Date()));put("distance_km",distance);put("active_seconds",seconds);put("max_speed",maxSpeed);num("average_speed",averageSpeed);num("max_heart",maxHeart);num("average_heart",averageHeart);num("max_cadence",maxCadence);num("average_cadence",averageCadence)};writableDatabase.insertOrThrow("trips",null,v)}
 private fun ContentValues.num(k:String,n:Number?){if(n==null)putNull(k)else when(n){is Int->put(k,n);is Long->put(k,n);is Double->put(k,n);is Float->put(k,n);else->put(k,n.toDouble())}}
 fun listTrips():List<SavedTrip>{val out=mutableListOf<SavedTrip>();readableDatabase.query("trips",arrayOf("completed_at","distance_km","active_seconds","max_speed"),null,null,null,null,"id DESC").use{c->while(c.moveToNext())out.add(SavedTrip(c.getString(0),c.getDouble(1),c.getLong(2),c.getDouble(3)))};return out}
}
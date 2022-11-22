package com.example.kotlinnativepercy.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.kotlinnativepercy.models.Trip
import com.google.firebase.database.*
import java.lang.Exception

class TripRepository {

    private val urlFirebase : String = "https://cw1-kotlin-android-default-rtdb.asia-southeast1.firebasedatabase.app"
    private val databaseReference : DatabaseReference = FirebaseDatabase.getInstance(urlFirebase).getReference("Trips")

    @Volatile private var INSTANCE : TripRepository ?= null

    fun getInstance() : TripRepository{
        return INSTANCE ?: synchronized(this){

            val instance = TripRepository()
            INSTANCE = instance
            instance
        }

    }

    fun loadTrips(tripList : MutableLiveData<List<Trip>>){

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                try {

                    val _tripList : List<Trip> = snapshot.children.map { dataSnapshot ->

                        dataSnapshot.getValue(Trip::class.java)!!

                    }

                    Log.d("Test", _tripList.toString())

                    tripList.postValue(_tripList)

                }catch (e : Exception){ }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}
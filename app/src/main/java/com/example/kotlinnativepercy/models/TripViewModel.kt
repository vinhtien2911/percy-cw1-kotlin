package com.example.kotlinnativepercy.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlinnativepercy.repository.TripRepository

class TripViewModel: ViewModel() {
    private val repository : TripRepository
    private val _allTrips = MutableLiveData<List<Trip>>()
    val allTrips : LiveData<List<Trip>> = _allTrips


    init {
        repository = TripRepository().getInstance()
        repository.loadTrips(_allTrips)
    }
}
package com.example.kotlinnativepercy.models

import android.os.Parcel
import android.os.Parcelable

data class Trip(
    val tripName : String ?= null,
    val destination : String ?= null,
    val date : String ?= null,
    val description : String ?= null,
    val phone : String ?= null,
    val hotel : String ?= null,
    val riskAssessment : String ?= null,
    val latitude: String ?= null,
    val longitude: String ?= null,
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(tripName)
        parcel.writeString(destination)
        parcel.writeString(date)
        parcel.writeString(description)
        parcel.writeString(phone)
        parcel.writeString(hotel)
        parcel.writeString(riskAssessment)
        parcel.writeString(latitude)
        parcel.writeString(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Trip> {
        override fun createFromParcel(parcel: Parcel): Trip {
            return Trip(parcel)
        }

        override fun newArray(size: Int): Array<Trip?> {
            return arrayOfNulls(size)
        }
    }

}

package com.example.kotlinnativepercy.fragment

import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import com.example.kotlinnativepercy.databinding.FragmentEntryBinding
import com.example.kotlinnativepercy.models.Trip
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class Entry : Fragment() {

    private var _binding: FragmentEntryBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var database : DatabaseReference
    private val urlFirebase : String = "https://cw1-kotlin-android-default-rtdb.asia-southeast1.firebasedatabase.app"

    private lateinit var tripName : TextInputEditText
    private lateinit var destination : TextInputEditText
    private lateinit var date : TextInputEditText
    private lateinit var description : TextInputEditText
    private lateinit var phone : TextInputEditText
    private lateinit var hotel: TextInputEditText

    private lateinit var tripNameLayout : TextInputLayout
    private lateinit var destinationLayout : TextInputLayout
    private lateinit var dateLayout : TextInputLayout
    private lateinit var descriptionLayout : TextInputLayout
    private lateinit var phoneLayout : TextInputLayout
    private lateinit var hotelLayout : TextInputLayout

    private lateinit var assessment : SwitchCompat
    private var assessmentValue : String = "No"
    private lateinit var submitButton : MaterialButton
    private lateinit var resetButton : MaterialButton

    private var longitude : Number = 0
    private var latitude : Number = 0

    private lateinit var tripNameList : List<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEntryBinding.inflate(inflater, container, false)

        //Set binding
        tripName = binding.tripName
        destination = binding.destination
        date = binding.date
        description = binding.description
        phone = binding.phone
        hotel = binding.hotel
        tripNameLayout = binding.tripNameLayout
        destinationLayout = binding.destinationLayout
        dateLayout = binding.dateLayout
        descriptionLayout = binding.descriptionLayout
        phoneLayout = binding.phoneLayout
        hotelLayout = binding.hotelLayout
        assessment = binding.assessment
        submitButton = binding.submitButton
        resetButton = binding.resetButton

        //Location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getCurrentLocation()

        //Connect database
        database = FirebaseDatabase.getInstance(urlFirebase).getReference("Trips")

        //Make trip list for validation
        makeTripList()

        //Date Picker
        val myCalendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            setDate(myCalendar)
        }

        date.setOnClickListener {
            DatePickerDialog(requireContext(), datePicker, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        //Validate
        inputListenerValidation()
        submitButton.setOnClickListener {
            submitForm()
        }

        assessment.setOnCheckedChangeListener { _, isChecked ->
            val value = if (isChecked)
                "Yes"
            else
                "No"
            setRiskAssessmentValue(value)
        }

        // Reset All Data
        resetButton.setOnClickListener {
            confirmResetAllData()
        }

        return binding.root
    }

    private fun getCurrentLocation() {
        if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
        }

        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object: CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken {
                return CancellationTokenSource().token
            }

            override fun isCancellationRequested(): Boolean {
                return false
            }
        }).addOnSuccessListener { location: Location? ->
            if(location != null) {
                setLocationValue(location)
            }
        }.addOnFailureListener {
            if(longitude == 0 && latitude == 0) {
                getCurrentLocation()
            }
        }
    }

    private fun setLocationValue(it : Location) {
        longitude = it.longitude
        latitude= it.latitude
    }

    private fun setDate(myCalendar: Calendar) {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        date.setText(sdf.format(myCalendar.time))
    }

    private fun setRiskAssessmentValue(value: String) {
        assessmentValue = value
    }

    private fun submitForm()
    {
        tripNameLayout.helperText = validTripName()
        destinationLayout.helperText = validDestination()
        dateLayout.helperText = validDate()


        val validTripName = tripNameLayout.helperText === null
        val validDestination = destinationLayout.helperText === null
        val validDate = dateLayout.helperText === null

        if (validTripName && validDestination && validDate) {
            return resetFormInput()
        }
        return
    }

    private fun resetFormInput()
    {
        var message = "Trip Name: ${tripName.text}"
        message += "\nDestination: ${destination.text}"
        message += "\nDate: ${date.text}"
        message += "\nDescription: ${description.text}"
        message += "\nPhone Number: ${phone.text}"
        message += "\nHotel: ${hotel.text}"
        message += "\nRisk Assessment: $assessmentValue"
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton("Confirm"){ _,_ ->
                saveDataOnCloud(
                    tripName.text.toString(),
                    destination.text.toString(),
                    date.text.toString(),
                    description.text.toString(),
                    phone.text.toString(),
                    hotel.text.toString(),
                    assessmentValue,
                    latitude,
                    longitude,
                )
            }
            .setNegativeButton("Edit") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun saveDataOnCloud(
        tripNameValue: String,
        destinationValue: String,
        dateValue: String,
        descriptionValue: String,
        phoneValue: String,
        hotelValue: String,
        riskAssessment: String,
        latitude: Number,
        longitude: Number
    ) {
        val newTrip = Trip(
            tripNameValue,
            destinationValue,
            dateValue,
            descriptionValue,
            phoneValue,
            hotelValue,
            riskAssessment,
            latitude.toString(),
            longitude.toString(),
        )
        database.child(tripNameValue).setValue(newTrip).addOnSuccessListener {
            tripName.text?.clear()
            destination.text?.clear()
            date.text?.clear()
            description.text?.clear()
            phone.text?.clear()
            hotel.text?.clear()
            Toast.makeText(requireContext(), "Successfully Saved Trip", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun makeTripList() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val itemList = mutableListOf<String>()
                    val trip : List<Trip> = snapshot.children.map { dataSnapshot ->

                        dataSnapshot.getValue(Trip::class.java)!!
                    }

                    for (item in trip) {
                        itemList.add(item.tripName!!)
                    }

                    tripNameList = itemList.toList()

                } catch (e: Exception) {
                    Log.d("Error Make Trip List: ", e.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Database Error: ", error.toString())
            }

        })

    }

    private fun confirmResetAllData() {
        val content = "Confirm to reset all data"
        AlertDialog.Builder(requireContext())
            .setMessage(content)
            .setPositiveButton("Confirm"){ _,_ ->
                database.removeValue().addOnSuccessListener {
                    Toast.makeText(requireContext(), "Reset Data Successfully", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Reset Data Failed", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun inputListenerValidation() {

        //Trip Name
        tripName.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                tripNameLayout.helperText = validTripName()
            }
        }

        //Destination
        destination.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                destinationLayout.helperText = validDestination()
            }
        }

        //Date
        date.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                dateLayout.helperText = validDate()
            }
        }
    }

    private fun validTripName(): String? {
        val tripNameValue = tripName.text.toString()
        if(tripNameValue.isEmpty()) {
            return "Required Input"
        }

        if(tripNameValue.trim() in tripNameList) {
            return "Trip Name already exist"
        }

        return null
    }


    private fun validDestination(): String? {
        val destinationValue = destination.text.toString()
        if(destinationValue.isEmpty()) {
            return "Required Input"
        }
        return null
    }

    private fun validDate(): String? {
        val dateValue = date.text.toString()
        if(dateValue.isEmpty()) {
            return "Required Input"
        }
        return null
    }
}
package com.example.kotlinnativepercy.fragment

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinnativepercy.R
import com.example.kotlinnativepercy.activity.DetailTrip
import com.example.kotlinnativepercy.adapter.TripAdapter
import com.example.kotlinnativepercy.databinding.FragmentEntryBinding
import com.example.kotlinnativepercy.databinding.FragmentShowBinding
import com.example.kotlinnativepercy.models.Trip
import com.example.kotlinnativepercy.models.TripViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class Show : Fragment() {

    private var _binding: FragmentShowBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel : TripViewModel
    private lateinit var tripRecyclerView: RecyclerView
    private lateinit var adapter: TripAdapter
    private val urlFirebase : String = "https://cw1-kotlin-android-default-rtdb.asia-southeast1.firebasedatabase.app"
    private lateinit var database : DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentShowBinding.inflate(inflater, container, false)

        //Connect database
        database = FirebaseDatabase.getInstance(urlFirebase).getReference("Trips")

        //Filter Trip
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchFilterTrip(newText)
                return false
            }
        })

        //Filter Button (Search Dialog)
        binding.filterButton.setOnClickListener {
            val dialogLayout = LayoutInflater.from(requireContext()).inflate(R.layout.filter_trip_dialog, null)
            val searchDialog =  AlertDialog.Builder(requireContext())
            searchDialog.setView(dialogLayout)
            searchDialog.show()

            val button = dialogLayout.findViewById<Button>(R.id.searchButton)
            val inputSearchDate = dialogLayout.findViewById<TextInputEditText>(R.id.searchDate)

            //Date Picker
            val myCalendar = Calendar.getInstance()
            val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                setDate(myCalendar, inputSearchDate)
            }

            inputSearchDate.setOnClickListener {
                DatePickerDialog(requireContext(), datePicker, myCalendar.get(Calendar.YEAR),
                    myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show()
            }

            //Search Button
            button.setOnClickListener {

                val tripNameParam = dialogLayout.findViewById<TextInputEditText>(R.id.searchTripName).text.toString()
                val destinationParam = dialogLayout.findViewById<TextInputEditText>(R.id.searchDestination).text.toString()
                val dateParam = dialogLayout.findViewById<TextInputEditText>(R.id.searchDate).text.toString()

                database.addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            val filteredItemList = mutableListOf<Trip>()
                            val trip : List<Trip> = snapshot.children.map { dataSnapshot ->

                                dataSnapshot.getValue(Trip::class.java)!!

                            }

                            searchCondition(tripNameParam, destinationParam, dateParam, filteredItemList, trip)

                            val filteredTrips: List<Trip> = filteredItemList.toList()
                            adapter.updateTripList(filteredTrips)
                        } catch (e: Exception) {

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })

            }


        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tripRecyclerView = binding.listShow
        tripRecyclerView.layoutManager = LinearLayoutManager(context)
        tripRecyclerView.setHasFixedSize(true)
        adapter = TripAdapter()
        tripRecyclerView.adapter = adapter

        viewModel = ViewModelProvider(this).get(TripViewModel::class.java)

        viewModel.allTrips.observe(viewLifecycleOwner, Observer {

            adapter.updateTripList(it)

            adapter.onItemClick = {
                val intent = Intent(requireContext(), DetailTrip::class.java)
                intent.putExtra("trip", it)
                startActivity(intent)
            }
        })
    }

    private fun setDate(myCalendar: Calendar, inputSearchDate: TextInputEditText) {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        inputSearchDate.setText(sdf.format(myCalendar.time))
    }

    private fun searchFilterTrip(queryText: String) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val filteredItemList = mutableListOf<Trip>()
                    val trip : List<Trip> = snapshot.children.map { dataSnapshot ->

                        dataSnapshot.getValue(Trip::class.java)!!

                    }

                    for (item in trip) {
                        if (
                            item.tripName!!.lowercase().contains(queryText.lowercase()) ||
                            item.date!!.lowercase().contains(queryText.lowercase()) ||
                            item.destination!!.lowercase().contains(queryText.lowercase())) {
                            filteredItemList.add(item)
                        }
                    }

                    val filteredTrips: List<Trip> = filteredItemList.toList()
                    adapter.updateTripList(filteredTrips)

                }catch (e : Exception){
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun searchCondition(tripNameSearchParam: String, destinationSearchParam: String,
                                dateSearchParam: String, filteredItemList: MutableList<Trip>, trip: List<Trip>) {
        if(tripNameSearchParam.isNotEmpty() && destinationSearchParam.isEmpty() && dateSearchParam.isEmpty()) {
            for (item in trip) {
                if (
                    item.tripName!!.lowercase().contains(tripNameSearchParam.lowercase())) {
                    filteredItemList.add(item)
                }
            }
        }

        if(tripNameSearchParam.isEmpty() && destinationSearchParam.isNotEmpty() && dateSearchParam.isEmpty()) {
            for (item in trip) {
                if (
                    item.destination!!.lowercase().contains(destinationSearchParam.lowercase())) {
                    filteredItemList.add(item)
                }
            }
        }

        if(tripNameSearchParam.isEmpty() && destinationSearchParam.isEmpty() && dateSearchParam.isNotEmpty()) {
            for (item in trip) {
                if (
                    item.date!!.lowercase().contains(dateSearchParam.lowercase())) {
                    filteredItemList.add(item)
                }
            }
        }

        if(tripNameSearchParam.isNotEmpty() && destinationSearchParam.isNotEmpty() && dateSearchParam.isEmpty()) {
            for (item in trip) {
                if (
                    item.tripName!!.lowercase().contains(tripNameSearchParam.lowercase()) &&
                    item.destination!!.lowercase().contains(destinationSearchParam.lowercase())) {
                    filteredItemList.add(item)
                }
            }
        }

        if(tripNameSearchParam.isNotEmpty() && destinationSearchParam.isEmpty() && dateSearchParam.isNotEmpty()) {
            for (item in trip) {
                if (
                    item.tripName!!.lowercase().contains(tripNameSearchParam.lowercase()) &&
                    item.date!!.lowercase().contains(dateSearchParam.lowercase())) {
                    filteredItemList.add(item)
                }
            }
        }

        if(tripNameSearchParam.isEmpty() && destinationSearchParam.isNotEmpty() && dateSearchParam.isNotEmpty()) {
            for (item in trip) {
                if (
                    item.destination!!.lowercase().contains(destinationSearchParam.lowercase()) &&
                    item.date!!.lowercase().contains(dateSearchParam.lowercase())) {
                    filteredItemList.add(item)
                }
            }
        }

        if(tripNameSearchParam.isNotEmpty() && destinationSearchParam.isNotEmpty() && dateSearchParam.isNotEmpty()) {
            for (item in trip) {
                if (
                    item.tripName!!.lowercase().contains(tripNameSearchParam.lowercase()) &&
                    item.destination!!.lowercase().contains(destinationSearchParam.lowercase()) &&
                    item.date!!.lowercase().contains(dateSearchParam.lowercase())) {
                    filteredItemList.add(item)
                }
            }
        }

        if(tripNameSearchParam.isEmpty() && destinationSearchParam.isEmpty() && dateSearchParam.isEmpty()) {
            for (item in trip) {
                filteredItemList.add(item)
            }
        }

    }

}
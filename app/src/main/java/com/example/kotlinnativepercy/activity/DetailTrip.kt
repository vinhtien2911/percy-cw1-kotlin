package com.example.kotlinnativepercy.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinnativepercy.R
import com.example.kotlinnativepercy.adapter.ExpenseAdapter
import com.example.kotlinnativepercy.databinding.ActivityDetailTripBinding
import com.example.kotlinnativepercy.models.Expense
import com.example.kotlinnativepercy.models.Trip
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class DetailTrip : AppCompatActivity() {

    private lateinit var binding : ActivityDetailTripBinding

    private lateinit var database : DatabaseReference
    private val urlFirebase : String = "https://cw1-kotlin-android-default-rtdb.asia-southeast1.firebasedatabase.app"

    private lateinit var tripNameDetailValue : String
    private lateinit var destinationDetailValue : String
    private lateinit var dateDetailValue : String
    private lateinit var riskAssessmentDetailValue : String
    private lateinit var descriptionDetailValue : String
    private lateinit var phoneDetailValue : String
    private lateinit var hotelDetailValue : String
    private lateinit var latitude : String
    private lateinit var longitude : String
    private lateinit var gpsGoogleMapUrl: String

    private lateinit var expenseRecyclerView: RecyclerView
    private lateinit var adapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Connect database
        database = FirebaseDatabase.getInstance(urlFirebase).getReference("Trips")

        //Pass data into activity
        val trip = intent.getParcelableExtra<Trip>("trip")
        if(trip?.tripName!!.isNotEmpty()) {

            tripNameDetailValue = trip.tripName

                readTripData(
                trip.tripName,
                binding.tripNameDetail,
                binding.destinationDetail,
                binding.dateDetail,
                binding.riskAssessmentDetail,
                binding.descriptionDetail,
                binding.phoneDetail,
                binding.hotelDetail)

            showExpenses(LinearLayoutManager(this))
        }

        //Update Dialog
        val updateButton: MaterialButton = binding.updateButton

        updateButton.setOnClickListener {
            updateTrip()
        }

        //Delete Dialog
        val deleteButton: MaterialButton = binding.deleteButton
        deleteButton.setOnClickListener {
            deleteTrip()
        }

        //Add Expense Dialog
        val addButton : MaterialButton = binding.addExpenseButton
        addButton.setOnClickListener {
            addExpense()
        }

        //Show On Map
        binding.copyToClipboard.setOnClickListener {

            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("GPS ON Google Map Web", "https://maps.google.com/?q=$latitude,$longitude")
            clipboard.setPrimaryClip(clip)

            Toast.makeText(this, "Copied Successfully", Toast.LENGTH_SHORT).show()
        }

    }

    private fun readTripData(tripName: String, tripNameView: TextView, destinationView: TextView,
                         dateView: TextView, riskAssessmentView: TextView, descriptionView: TextView,
                         phoneView: TextView, hotelView: TextView) {
        database.child(tripName).get().addOnSuccessListener {

            if (it.exists()){

                destinationDetailValue = it.child("destination").value.toString()
                dateDetailValue = it.child("date").value.toString()
                riskAssessmentDetailValue = it.child("riskAssessment").value.toString()
                descriptionDetailValue = it.child("description").value.toString()
                phoneDetailValue = it.child("phone").value.toString()
                hotelDetailValue = it.child("hotel").value.toString()

                latitude = it.child("latitude").value.toString()
                longitude = it.child("longitude").value.toString()

                gpsGoogleMapUrl = "https://maps.google.com/?q=$latitude,$longitude"

                tripNameView.text = tripNameDetailValue
                destinationView.text = destinationDetailValue
                dateView.text = dateDetailValue
                riskAssessmentView.text = riskAssessmentDetailValue
                descriptionView.text = descriptionDetailValue
                phoneView.text = phoneDetailValue
                hotelView.text = hotelDetailValue
                binding.placeOnMap.text = gpsGoogleMapUrl
            }
        }

    }

    private fun deleteTrip() {
        val message = "Click \"Confirm\" to delete this trip."
        AlertDialog.Builder(this)
            .setTitle("Delete Trip")
            .setMessage(message)
            .setPositiveButton("Confirm"){ _,_ ->
                database.child(tripNameDetailValue).removeValue().addOnSuccessListener {
                    Toast.makeText(this, "Successfully Deleted", Toast.LENGTH_SHORT).show()
                }
                finish()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateTrip() {
        val updateDialog = LayoutInflater.from(this).inflate(R.layout.update_trip_dialog, null)

        //Date Picker
        val myCalendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            setDate(myCalendar, updateDialog.findViewById(R.id.dateUpdate))
        }

        updateDialog.findViewById<TextInputEditText>(R.id.dateUpdate).setOnClickListener {
            DatePickerDialog(this, datePicker, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        val destinationLayout : TextInputLayout = updateDialog.findViewById(R.id.destinationLayout)
        val dateLayout : TextInputLayout = updateDialog.findViewById(R.id.dateLayout)
        val riskAssessment : SwitchCompat = updateDialog.findViewById(R.id.assessmentUpdate)
        val descriptionLayout : TextInputLayout = updateDialog.findViewById(R.id.descriptionLayout)
        val phoneLayout: TextInputLayout = updateDialog.findViewById(R.id.phoneLayout)
        val hotelLayout : TextInputLayout = updateDialog.findViewById(R.id.hotelLayout)

        setTextInput(destinationLayout, dateLayout, riskAssessment, descriptionLayout, phoneLayout, hotelLayout)


        riskAssessment.setOnCheckedChangeListener { _, isChecked ->
            val value = if(isChecked)
                "Yes"
            else
                "No"
            if(riskAssessmentDetailValue != value) {
                riskAssessmentDetailValue = value
            }
        }

        //Create dialog
        val searchDialog = AlertDialog.Builder(this)
        searchDialog.setTitle("Update Trip")
        searchDialog.setView(updateDialog)
        searchDialog.setPositiveButton("Update") { _, _ ->
            val destination : TextInputEditText = updateDialog.findViewById(R.id.destinationUpdate)
            val date : TextInputEditText = updateDialog.findViewById(R.id.dateUpdate)
            val description : TextInputEditText = updateDialog.findViewById(R.id.descriptionUpdate)
            val phone : TextInputEditText = updateDialog.findViewById(R.id.phoneUpdate)
            val hotel : TextInputEditText = updateDialog.findViewById(R.id.hotelUpdate)

            val newTripUpdate = mapOf(
                "destination" to destination.text.toString(),
                "date" to date.text.toString(),
                "description" to description.text.toString(),
                "phone" to phone.text.toString(),
                "hotel" to hotel.text.toString(),
                "riskAssessment" to riskAssessmentDetailValue
            )

            database.child(tripNameDetailValue).updateChildren(newTripUpdate).addOnSuccessListener {
                Toast.makeText(this, "Successfully Updated", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            }

            finish()
        }
        searchDialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()

        }
        searchDialog.show()
    }

    private fun setTextInput(destinationView: TextInputLayout,
         dateView: TextInputLayout, riskAssessmentView: SwitchCompat, descriptionView: TextInputLayout,
         phoneView: TextInputLayout, hotelView: TextInputLayout) {

        destinationView.editText?.setText(destinationDetailValue)
        dateView.editText?.setText(dateDetailValue)
        descriptionView.editText?.setText(descriptionDetailValue)
        phoneView.editText?.setText(phoneDetailValue)
        hotelView.editText?.setText(hotelDetailValue)

        if(riskAssessmentDetailValue == "Yes") {
            riskAssessmentView.isChecked = true
        }

    }

    private fun inputFocusListener(expenseTypeView: AutoCompleteTextView,
                                   amountView: TextInputEditText, dateView: TextInputEditText, timeView: TextInputEditText, dialog: View
    ) {

        //Expense Type
        expenseTypeView.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                dialog.findViewById<TextInputLayout>(R.id.expenseTypeLayout).helperText = validExpenseType(dialog)
            }
        }

        //Amount
        amountView.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                dialog.findViewById<TextInputLayout>(R.id.amountLayout).helperText = validAmount(dialog)
            }
        }

        //Date
        dateView.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                dialog.findViewById<TextInputLayout>(R.id.dateLayout).helperText = validDate(dialog)
            }
        }

        //Time
        timeView.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                dialog.findViewById<TextInputLayout>(R.id.timeLayout).helperText = validTime(dialog)
            }
        }

    }

    private fun validExpenseType(dialog: View): String? {
        val expenseType = dialog.findViewById<AutoCompleteTextView>(R.id.expenseType).text.toString()
        if(expenseType.isEmpty()) {
            return "Required*"
        }
        return null
    }

    private fun validAmount(dialog: View): String? {
        val amount = dialog.findViewById<TextInputEditText>(R.id.amount).text.toString()
        if(amount.isEmpty()) {
            return "Required*"
        }
        return null
    }

    private fun validDate(dialog: View): String? {
        val date = dialog.findViewById<TextInputEditText>(R.id.date).text.toString()
        if(date.isEmpty()) {
            return "Required*"
        }
        return null
    }

    private fun validTime(dialog: View): String? {
        val time = dialog.findViewById<TextInputEditText>(R.id.time).text.toString()
        if(time.isEmpty()) {
            return "Required*"
        }
        return null
    }

    private fun submitForm(dialog: View)
    {
        val expenseTypeView : TextInputLayout = dialog.findViewById(R.id.expenseTypeLayout)
        val amountView : TextInputLayout = dialog.findViewById(R.id.amountLayout)
        val dateView : TextInputLayout= dialog.findViewById(R.id.dateLayout)
        val timeView : TextInputLayout= dialog.findViewById(R.id.timeLayout)

        expenseTypeView.helperText = validExpenseType(dialog)
        amountView.helperText = validAmount(dialog)
        dateView.helperText = validDate(dialog)
        timeView.helperText = validTime(dialog)

        val validExpenseType = expenseTypeView.helperText === null
        val validAmount = amountView.helperText === null
        val validDate = dateView.helperText === null
        val validTime = timeView.helperText === null

        if (validExpenseType && validAmount && validDate && validTime) {
            return saveData(
                dialog.findViewById<AutoCompleteTextView>(R.id.expenseType).text.toString(),
                dialog.findViewById<TextInputEditText>(R.id.amount).text.toString(),
                dialog.findViewById<TextInputEditText>(R.id.date).text.toString(),
                dialog.findViewById<TextInputEditText>(R.id.time).text.toString(),
                dialog.findViewById<TextInputEditText>(R.id.comment).text.toString(),
                dialog)
        }
        return

    }

    private fun saveData(
        expenseType: String,
        amount: String,
        date: String,
        time: String,
        comment: String,
        dialog: View
    ) {
        val expense = Expense(
            expenseType,
            amount,
            date,
            time,
            comment
        )
        database.child(tripNameDetailValue).child("expenses").push().setValue(expense).addOnSuccessListener {
            dialog.findViewById<AutoCompleteTextView>(R.id.expenseType).text?.clear()
            dialog.findViewById<TextInputEditText>(R.id.amount).text?.clear()
            dialog.findViewById<TextInputEditText>(R.id.date).text?.clear()
            dialog.findViewById<TextInputEditText>(R.id.time).text?.clear()
            dialog.findViewById<TextInputEditText>(R.id.comment).text?.clear()

            Toast.makeText(this, "Successfully Added", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addExpense() {
        val addDialog = LayoutInflater.from(this).inflate(R.layout.add_expense_dialog, null)

        //Date Picker
        val myCalendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            setDate(myCalendar, addDialog.findViewById(R.id.date))
        }

        addDialog.findViewById<TextInputEditText>(R.id.date).setOnClickListener {
            DatePickerDialog(this, datePicker, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        //Time Picker
        val timeInput : TextInputEditText = addDialog.findViewById(R.id.time)
        timeInput.setOnClickListener {
            val currentTime = Calendar.getInstance()
            val startHour = currentTime.get(Calendar.HOUR_OF_DAY)
            val startMinute = currentTime.get(Calendar.MINUTE)

            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                timeInput.setText("$hourOfDay:$minute")
            }, startHour, startMinute, false).show()

        }

        //Dropdown Menu
        val items = listOf("Travel", "Food", "Other")
        val adapter = ArrayAdapter(this, R.layout.select_type_expense ,items)
        addDialog.findViewById<AutoCompleteTextView>(R.id.expenseType).setAdapter(adapter)

        //Create dialog
        val addExpenseDialog = AlertDialog.Builder(this)
        addExpenseDialog.setView(addDialog)
        addExpenseDialog.show()

        //Validate
        inputFocusListener(
            addDialog.findViewById(R.id.expenseType),
            addDialog.findViewById(R.id.amount),
            addDialog.findViewById(R.id.date),
            addDialog.findViewById(R.id.time),
            addDialog
        )

        addDialog.findViewById<MaterialButton>(R.id.submitButton).setOnClickListener {
            submitForm(addDialog)
        }

    }

    private fun setDate(myCalendar: Calendar, inputText: TextInputEditText) {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        inputText.setText(sdf.format(myCalendar.time))
    }

    private fun showExpenses(layout: LinearLayoutManager) {
        database.child(tripNameDetailValue).child("expenses").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {

                    val _expenseList : List<Expense> = snapshot.children.map { dataSnapshot ->

                        dataSnapshot.getValue(Expense::class.java)!!

                    }

                    expenseRecyclerView = binding.listViewExpense
                    expenseRecyclerView.layoutManager = layout
                    expenseRecyclerView.setHasFixedSize(true)
                    adapter = ExpenseAdapter()
                    expenseRecyclerView.adapter = adapter

                    adapter.updateExpenseList(_expenseList)

                }catch (e : Exception){
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}
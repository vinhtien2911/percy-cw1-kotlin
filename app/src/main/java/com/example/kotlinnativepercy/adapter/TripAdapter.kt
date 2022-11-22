package com.example.kotlinnativepercy.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinnativepercy.R
import com.example.kotlinnativepercy.models.Trip

class TripAdapter: RecyclerView.Adapter<TripAdapter.MyViewHolder>() {

    private val tripList = ArrayList<Trip>()
    var onItemClick: ((Trip) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.show_list_trip,
            parent,false
        )
        return MyViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        Log.d("Test Binding", tripList.toString())

        val currentItem = tripList[position]

        holder.tripName.text = currentItem.tripName
        holder.destination.text = currentItem.destination
        holder.date.text = currentItem.date

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return tripList.size
    }

    fun updateTripList(tripList : List<Trip>){
        this.tripList.clear()
        this.tripList.addAll(tripList)
        notifyDataSetChanged()

    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val tripName : TextView = itemView.findViewById(R.id.tripName)
        val destination : TextView = itemView.findViewById(R.id.destination)
        val date : TextView = itemView.findViewById(R.id.date)

    }
}
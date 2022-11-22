package com.example.kotlinnativepercy.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinnativepercy.R
import com.example.kotlinnativepercy.models.Expense

class ExpenseAdapter: RecyclerView.Adapter<ExpenseAdapter.MyViewHolder>() {

    private val expenseList = ArrayList<Expense>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.show_list_expense,
            parent,false
        )
        return ExpenseAdapter.MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = expenseList[position]

        holder.expenseType.text = currentItem.amount
        holder.amount.text = currentItem.expenseType
        holder.date.text = currentItem.date
        holder.time.text = currentItem.time
        holder.comment.text = currentItem.comment

    }

    override fun getItemCount(): Int {
        return expenseList.size
    }

    fun updateExpenseList(expenseList : List<Expense>){
        this.expenseList.clear()
        this.expenseList.addAll(expenseList)
        notifyDataSetChanged()

    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val expenseType : TextView = itemView.findViewById(R.id.expenseType)
        val amount : TextView = itemView.findViewById(R.id.amount)
        val date : TextView = itemView.findViewById(R.id.date)
        val time : TextView = itemView.findViewById(R.id.time)
        val comment : TextView = itemView.findViewById(R.id.comment)

    }
}
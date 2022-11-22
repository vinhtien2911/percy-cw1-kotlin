package com.example.kotlinnativepercy.models

data class Expense(
    val amount : String ?= null,
    val expenseType : String ?= null,
    val date : String ?= null,
    val time : String ?=  null,
    val comment : String ?= null
)

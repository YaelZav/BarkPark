package com.example.barkpark.model

data class DogItem(val photo:String?=null,
                   val name:String="",
                   val breed:String="",
                   val age:String="",
                   val gender:String="",
                   var Id:String="",
                   var OwnerId:String="")

object ItemManager {

    val items : MutableList<DogItem> = mutableListOf()

    fun add(item: DogItem) {
        items.add(item)
    }

    fun remove(index:Int) {
        items.removeAt(index)
    }
}
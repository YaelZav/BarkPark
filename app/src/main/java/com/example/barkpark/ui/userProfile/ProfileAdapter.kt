package com.example.barkpark.ui.userProfile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.barkpark.R
import com.example.barkpark.databinding.DogItemBinding
import com.example.barkpark.model.DogItem


class ProfileAdapter(private val items:  MutableList<DogItem>, val callBack:DogListener)
    : RecyclerView.Adapter<ProfileAdapter.DogViewHolder>() {

    private val dogs = ArrayList<DogItem>()

    fun setDogs(dogs : Collection<DogItem>) {
        this.dogs.clear()
        this.dogs.addAll(dogs)
        notifyDataSetChanged()
    }

    interface DogListener {
        fun onDogClicked(index: Int)
        fun onDogLongClicked(index: Int)
    }


    inner class DogViewHolder(private val binding: DogItemBinding)
        : RecyclerView.ViewHolder(binding.root), View.OnClickListener,
        View.OnLongClickListener {

        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)
        }

        override fun onClick(p0: View?) {
            callBack.onDogClicked(adapterPosition)
        }

        override fun onLongClick(p0: View?): Boolean {
            callBack.onDogLongClicked(adapterPosition)
            return false
        }

        fun bind(item: DogItem){
            binding.dogItemName.text = item.name
            binding.dogItemBreed.text = item.breed
            binding.dogItemAge.text = item.age
            binding.dogGender.setImageResource(when (item.gender) {
                "male" -> R.drawable.ic_baseline_male_24
                "female" -> R.drawable.ic_baseline_female_24
                else -> R.drawable.ic_baseline_question_mark_24 })
            Glide.with(binding.root).load(item.photo).circleCrop().into(binding.dogItemImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        DogViewHolder(DogItemBinding.inflate(LayoutInflater.from(parent.context), parent,false))

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) =
        holder.bind(items[position])

    override fun getItemCount() = items.size

    }




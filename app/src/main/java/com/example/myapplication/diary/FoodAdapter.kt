package com.example.myapplication.diary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemFoodEntryBinding // ViewBinding 사용

class FoodAdapter(private var foodItems: List<FoodItem>) :
    RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding =
            ItemFoodEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(foodItems[position])
    }

    override fun getItemCount(): Int = foodItems.size

    fun updateData(newFoodItems: List<FoodItem>) {
        foodItems = newFoodItems
        notifyDataSetChanged() // 실제 앱에서는 DiffUtil 사용을 권장합니다.
    }

    class FoodViewHolder(private val binding: ItemFoodEntryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(foodItem: FoodItem) {
            binding.textViewItemFoodName.text = foodItem.name
            binding.textViewItemCalorie.text = "${foodItem.calorie} kcal"
        }
    }
}

package com.example.myapplication.diary // 실제 패키지명으로 변경

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemDiaryEntryBinding // ViewBinding

class DiaryAdapter(
    private var diaryEntries: List<DiaryEntry>,
    private val onItemClick: (DiaryEntry) -> Unit // 아이템 클릭 리스너 추가 (선택 사항)
) : RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val binding =
            ItemDiaryEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiaryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        val entry = diaryEntries[position]
        holder.bind(entry)
        holder.itemView.setOnClickListener { onItemClick(entry) } // 아이템 클릭 시
    }

    override fun getItemCount(): Int = diaryEntries.size

    fun updateData(newEntries: List<DiaryEntry>) {
        diaryEntries = newEntries
        notifyDataSetChanged() // DiffUtil 사용 권장
    }

    class DiaryViewHolder(private val binding: ItemDiaryEntryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: DiaryEntry) {
            binding.textViewDiaryTitle.text = entry.title
            binding.textViewDiaryDate.text = entry.date
            binding.textViewDiaryTotalCalorie.text = "총 칼로리: ${entry.totalCalorie} kcal"
            // 기존 내용 미리보기는 제거되었으므로 관련 코드 삭제
        }
    }
}

package com.example.myapplication.diary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemDiaryEntryBinding
import java.text.SimpleDateFormat
import java.util.Locale


/*
*  LIST VIEW ADPATER
* */

// class DiaryAdapter --(상속)--> ListAdapter<DiaryEntry, ViewHolder> (안드로이드 내장 추상 클래스) --> RecyclerView.Adapter
/* [JAVA로 치면 ] public class DiaryAdapter extends ListAdapter<DiaryEntry, DiaryAdapter.DiaryViewHolder> {
    private OnItemClickListener onItemClick;

    public interface OnItemClickListener {
        void onItemClick(DiaryEntry entry);
    }

    public DiaryAdapter(OnItemClickListener listener) {
        super(new DiaryDiffCallback());
        this.onItemClick = listener;
    }
    ..이하 생략
* }
* */
class DiaryAdapter(
    private val onItemClick: (DiaryEntry) -> Unit
) : ListAdapter<DiaryEntry, DiaryAdapter.DiaryViewHolder>(DiaryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val binding = ItemDiaryEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DiaryViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiaryViewHolder(
        private val binding: ItemDiaryEntryBinding,
        private val onItemClick: (DiaryEntry) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) { // == extends RecyclearView.ViewHolder

        fun bind(entry: DiaryEntry) {
            binding.textviewMood.text = entry.mood
            binding.textviewTitle.text = entry.title
            binding.textviewContent.text = entry.content

            val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN)
            binding.textviewDate.text = dateFormat.format(entry.date)

            binding.root.setOnClickListener {
                onItemClick(entry)
            }
        }
    }

    class DiaryDiffCallback : DiffUtil.ItemCallback<DiaryEntry>() {
        override fun areItemsTheSame(oldItem: DiaryEntry, newItem: DiaryEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DiaryEntry, newItem: DiaryEntry): Boolean {
            return oldItem == newItem
        }
    }
}

/*
* 목록형 UI는 RecyclerView + Adapter 로 구현
버튼 클릭, 텍스트 표시 등은 ViewBinding이나 findViewById로 연결
* */
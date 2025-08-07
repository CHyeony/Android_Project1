package com.example.myapplication.diary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController // 네비게이션 사용 시
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R // R 클래스 경로 확인
import com.example.myapplication.databinding.FragmentSecondBinding

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var diaryManager: DiaryManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        diaryManager = DiaryManager.getInstance(requireContext()) // DiaryManager 초기화
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadDiaryEntries()

        binding.fabAddDiary.setOnClickListener {
            // DiaryWriteFragment로 이동하는 네비게이션 액션 ID (nav_graph.xml에 정의)
            findNavController().navigate(R.id.action_secondFragment_to_diaryWriteFragment)
        }
    }

    private fun setupRecyclerView() {
        diaryAdapter = DiaryAdapter(emptyList()) { diaryEntry ->
            // TODO: 일기 항목 클릭 시 상세 화면으로 이동 또는 수정/삭제 기능
            // val action = SecondFragmentDirections.actionSecondFragmentToDiaryDetailFragment(diaryEntry.id)
            // findNavController().navigate(action)
        }
        binding.recyclerviewDiary.apply {
            adapter = diaryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun loadDiaryEntries() {
        val entries = diaryManager.getAllDiaryEntries()
        if (entries.isEmpty()) {
            binding.textviewEmpty.visibility = View.VISIBLE
            binding.recyclerviewDiary.visibility = View.GONE
        } else {
            binding.textviewEmpty.visibility = View.GONE
            binding.recyclerviewDiary.visibility = View.VISIBLE
            diaryAdapter.updateData(entries)
        }
    }

    // 화면이 다시 보여질 때마다 데이터를 새로고침 (선택 사항)
    override fun onResume() {
        super.onResume()
        loadDiaryEntries() // 다른 화면에서 일기가 추가/수정되었을 수 있으므로
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

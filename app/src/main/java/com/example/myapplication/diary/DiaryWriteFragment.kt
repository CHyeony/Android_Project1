package com.example.myapplication.diary

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


import androidx.fragment.app.Fragment

import androidx.recyclerview.widget.LinearLayoutManager // RecyclerView에 필요
import com.example.myapplication.databinding.FragmentDiaryWriteBinding
import com.example.myapplication.network.OpenFoodFactsSearchResponse
import com.example.myapplication.network.RetrofitClient
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import retrofit2.Call

class DiaryWriteFragment : Fragment() {

    private var _binding: FragmentDiaryWriteBinding? = null
    private val binding get() = _binding!!

    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var foodAdapter: FoodAdapter
    private val recognizedFoodItems = mutableListOf<FoodItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDiaryWriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) // super.onViewCreated 추가

        setupRecyclerView()

        binding.buttonTakePhoto.setOnClickListener {
            dispatchTakePictureIntent()
            // 새 사진을 찍을 때 이전 결과 초기화
            recognizedFoodItems.clear()
            foodAdapter.updateData(recognizedFoodItems)
            updateTotalCalorie()
            binding.imageViewPreview.setImageResource(android.R.color.transparent) // 이전 미리보기 이미지 제거
        }
    }

    private fun setupRecyclerView() {
        foodAdapter = FoodAdapter(emptyList())
        binding.recyclerViewFoodItems.apply {
            adapter = foodAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Android 11 (API 30) 이상에서는 resolveActivity 대신 queryIntentActivities 사용 고려 및
        // AndroidManifest.xml에 <queries> 요소 추가 필요할 수 있음
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data) // super.onActivityResult 추가
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap // 안전한 캐스팅
            imageBitmap?.let {
                binding.imageViewPreview.setImageBitmap(it)
                recognizeFood(it)
            }
        }
    }

    private fun recognizeFood(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        // 옵션 설정 (신뢰도 임계값 등 조정 가능)
        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7f) // 신뢰도 70% 이상인 라벨만 가져오도록 설정 (예시)
            .build()
        val labeler = ImageLabeling.getClient(options) // 수정된 옵션 사용

        labeler.process(image)
            .addOnSuccessListener { labels ->
                // 이전 결과 초기화 (새로운 이미지 분석 시)
                // 이 부분은 버튼 클릭 시 초기화로 옮겼으므로, 중복될 수 있음.
                // 사진을 찍을 때마다 누적하고 싶다면 이 라인을 주석 처리하거나 제거
                // recognizedFoodItems.clear()

                if (labels.isNotEmpty()) {
                    for (label in labels) {
                        Log.d("MLKit", "Label: ${label.text}, Confidence: ${label.confidence}, Index: ${label.index}")
                        // TODO: 음식 관련 라벨인지 추가적인 필터링 로직이 필요할 수 있습니다.
                        // 예를 들어, ML Kit에서 제공하는 라벨 외에 자체적인 음식 카테고리 목록과 비교 등
                        val foodName = label.text
                        val calorie = getCalories(foodName) // 칼로리 가져오기
                        // 신뢰도와 함께 FoodItem 생성 (예시에서는 모든 라벨을 음식으로 간주)
                        if (calorie > 0) { // 칼로리가 0보다 큰 경우만 (유효한 음식으로 간주)
                            recognizedFoodItems.add(FoodItem(foodName, calorie, label.confidence))
                        }
                    }
                    // 중복 제거 (선택 사항 - 같은 음식이 여러 번 잡힐 경우)
                    // val distinctFoodItems = recognizedFoodItems.distinctBy { it.name }
                    // foodAdapter.updateData(distinctFoodItems)
                    foodAdapter.updateData(recognizedFoodItems.toList()) // toList()로 불변 리스트 전달
                } else {
                    // 인식된 라벨이 없을 경우 처리
                    // binding.textViewFoodName.text = "음식: 인식 실패" // 이전 UI 요소
                    // binding.textViewCalorie.text = "칼로리: -" // 이전 UI 요소
                    // 필요하다면 RecyclerView에 "인식 실패" 메시지를 표시하는 로직 추가
                }
                updateTotalCalorie() // 총 칼로리 업데이트
            }
            .addOnFailureListener { e ->
                Log.e("MLKit", "Image labeling failed", e)
                // 오류 처리
                // binding.textViewFoodName.text = "음식 인식 오류" // 이전 UI 요소
                // binding.textViewCalorie.text = "칼로리: -" // 이전 UI 요소
                updateTotalCalorie() // 오류 시에도 총 칼로리는 0으로 업데이트
            }
    }

    private fun updateTotalCalorie() {
        val totalCalorie = recognizedFoodItems.sumOf { it.calorie }
        binding.textViewTotalCalorie.text = "총 칼로리: $totalCalorie kcal"
    }

    // 칼로리 데이터베이스/로직 - 더 많은 음식을 추가하거나 외부 API 연동 고려
    private fun getCalories(foodName: String): Int {
        // 음식 이름을 소문자로 변환하고, 공백 제거 등 정규화 과정이 필요할 수 있음
        return when (foodName.lowercase().trim()) {
            "pizza" -> 285
            "hamburger", "burger" -> 250 // 여러 라벨에 대해 같은 칼로리 매핑 가능
            "banana" -> 105
            "apple" -> 95
            "donut", "doughnut" -> 195
            "rice" -> 206
            "food" -> 0 // "Food" 같은 일반적인 라벨은 제외하거나 기본값 처리
            "plate", "dish" -> 0 // 음식 용기 관련 라벨 제외
            // 더 많은 음식과 칼로리 추가...
            // 예: "salad" -> 150, "chicken" -> 220, "french fries" -> 312
            else -> 0 // 모르는 음식은 0 칼로리 또는 기본값 (평균 200 등)으로 처리할 수 있음
            // 0으로 하면 총 칼로리 계산에 영향을 주지 않음
        }
    }


    // ... DiaryWriteFragment.kt ...

    // ML Kit 라벨 처리 후 API 호출 (Open Food Facts 예시)
    private fun fetchFoodCaloriesFromOpenFoodFacts(foodNameFromLabel: String, mlKitConfidence: Float) {
        val userAgent = "MyFoodDiaryApp - Android - Version 1.0 - JOCHAEYEON" // User-Agent 설정

        RetrofitClient.openFoodFactsInstance.searchFoodByName(
            foodName = foodNameFromLabel,
            userAgent = userAgent // 헤더에 User-Agent 전달
        ).enqueue(object : retrofit2.Callback<OpenFoodFactsSearchResponse> {
            override fun onResponse(
                call: Call<OpenFoodFactsSearchResponse>,
                response: retrofit2.Response<OpenFoodFactsSearchResponse>
            ) {
                var calorie = 0
                var apiFoodName = foodNameFromLabel

                if (response.isSuccessful) {
                    val searchResponse = response.body()
                    if (searchResponse != null && searchResponse.products != null && searchResponse.products.isNotEmpty()) {
                        // 검색 결과 중 첫 번째 제품 사용 (또는 더 적절한 제품 선택 로직)
                        val product = searchResponse.products[0]
                        apiFoodName = product.product_name ?: foodNameFromLabel

                        // 칼로리 정보 추출 (energyKcal 또는 energyKcal100g)
                        calorie = product.nutriments?.energyKcal?.toInt()
                            ?: product.nutriments?.energyKcal100g?.toInt()
                                    ?: 0 // 칼로리 정보가 없으면 0

                        Log.d("OpenFoodFacts", "음식: $apiFoodName, API 칼로리: $calorie kcal")
                    } else {
                        Log.d("OpenFoodFacts", "'$foodNameFromLabel' 검색 결과 없음.")
                    }
                } else {
                    Log.e("OpenFoodFacts", "API 호출 실패: ${response.code()} - ${response.message()}")
                }

                // 로컬 DB 값과 비교하거나 API 값 우선
                if (calorie == 0) {
                    calorie = getCalories(foodNameFromLabel) // 로컬 getCalories 함수 호출 (이전 답변 참고)
                }

                // recognizedFoodItems 업데이트 또는 추가 (이전 답변의 로직과 유사하게)
                updateOrAddFoodItem(apiFoodName, calorie, mlKitConfidence)
            }

            override fun onFailure(call: Call<OpenFoodFactsSearchResponse>, t: Throwable) {
                Log.e("OpenFoodFacts", "API 네트워크 오류 ($foodNameFromLabel): ${t.message}", t)
                // 네트워크 오류 시 로컬 값 사용
                val localCalorie = getCalories(foodNameFromLabel)
                updateOrAddFoodItem(foodNameFromLabel, localCalorie, mlKitConfidence)
            }
        })
    }

    // recognizedFoodItems 업데이트 및 UI 갱신 (중복될 수 있으므로 기존 함수 재활용 또는 수정)
    private fun updateOrAddFoodItem(name: String, calorie: Int, confidence: Float) {
        val existingIndex = recognizedFoodItems.indexOfFirst { it.name.equals(name, ignoreCase = true) }

        if (calorie > 0) { // 유효한 칼로리만
            if (existingIndex != -1) {
                recognizedFoodItems[existingIndex] = FoodItem(name, calorie, confidence)
            } else {
                recognizedFoodItems.add(FoodItem(name, calorie, confidence))
            }
        } else if (existingIndex == -1 && getCalories(name) > 0) { // API에서 못찾고 로컬에만 있으면 로컬 값으로 추가
            recognizedFoodItems.add(FoodItem(name, getCalories(name), confidence))
        }


        // 중복 제거 및 UI 업데이트
        val distinctItems = recognizedFoodItems.distinctBy { it.name.lowercase() }
        recognizedFoodItems.clear()
        recognizedFoodItems.addAll(distinctItems)

        foodAdapter.updateData(recognizedFoodItems.toList())
        updateTotalCalorie()
    }


    // recognizeFood 함수 내에서 호출 변경
    // 기존: fetchFoodCaloriesFromApiForLabel(label.text, label.confidence)
    // 변경: fetchFoodCaloriesFromOpenFoodFacts(label.text, label.confidence)

    // 일기 저장 로직 (button_save_diary 클릭 시)
    // binding.buttonSaveDiary.setOnClickListener { saveDiaryEntry() }

    private fun saveDiaryEntry() {
        val title = binding.editTextDiaryTitle.text.toString().trim()
        if (title.isEmpty()) {
            binding.textInputLayoutTitle.error = "제목을 입력해주세요."
            return
        } else {
            binding.textInputLayoutTitle.error = null // 오류 메시지 제거
        }

        if (recognizedFoodItems.isEmpty() && binding.imageViewPreview.drawable == null) {
            // 사용자에게 사진을 찍거나 음식을 추가하라는 메시지 표시 (선택 사항)
            // Toast.makeText(requireContext(), "기록할 음식 사진이나 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            // return // 저장을 막을 수도 있음
        }

        val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val totalCalorie = recognizedFoodItems.sumOf { it.calorie }
        // TODO: 이미지 파일 저장 로직 및 imagePath 가져오기
        val imagePath: String? = null // 실제 이미지 저장 경로로 대체해야 함

        val newEntry = DiaryEntry(
            title = title,
            date = currentDate,
            foodItems = recognizedFoodItems.toList(), // 현재 인식된 음식 목록
            totalCalorie = totalCalorie,
            imagePath = imagePath
        )

        // TODO: DiaryManager를 사용하여 newEntry를 SharedPreferences 또는 Room DB에 저장
        // DiaryManager.getInstance(requireContext()).addDiaryEntry(newEntry)
        Log.d("DiarySave", "저장될 일기: $newEntry")

        // 저장 후 이전 화면으로 돌아가거나 사용자에게 피드백
        // findNavController().popBackStack() 또는 Toast 메시지
        // Toast.makeText(requireContext(), "일기가 저장되었습니다.", Toast.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

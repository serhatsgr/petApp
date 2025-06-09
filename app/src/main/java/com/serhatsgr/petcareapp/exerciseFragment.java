package com.serhatsgr.petcareapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;

public class exerciseFragment extends Fragment {

    TextInputEditText etExerciseType, etExerciseDuration, etExerciseDate, etExerciseTime;
    Button btn, btnH;
    TextView tvAnimalName; // Hayvan adı için TextView

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser mUsers;
    private HashMap<String, Object> mData;
    private FirebaseFirestore mFireStore;
    private String animalId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);

        etExerciseType = view.findViewById(R.id.etExerciseType);
        etExerciseDuration = view.findViewById(R.id.etExerciseDuration);
        etExerciseDate = view.findViewById(R.id.etExerciseDate);
        etExerciseTime = view.findViewById(R.id.etExerciseTime);
        btn = view.findViewById(R.id.btnadd);
        btnH = view.findViewById(R.id.btnHistory);
        tvAnimalName = view.findViewById(R.id.animalName); // TextView'i başlat

        mAuth = FirebaseAuth.getInstance();
        mFireStore = FirebaseFirestore.getInstance();

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("PetAppPrefs", Context.MODE_PRIVATE);
        animalId = sharedPreferences.getString("animalId", null);

        if (animalId != null) {
            loadAnimalName(animalId); // Hayvan adını yükle
        } else {
            Toast.makeText(requireContext(), "Hayvan ID alınamadı.", Toast.LENGTH_SHORT).show();
        }

        // Tarih seçici
        etExerciseDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireActivity(), (view1, year, month, dayOfMonth) -> {
                etExerciseDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        // Saat seçici
        etExerciseTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(requireActivity(), (view12, hourOfDay, minute) -> {
                etExerciseTime.setText(hourOfDay + ":" + String.format("%02d", minute));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        });

        btn.setOnClickListener(v -> {
            String type = etExerciseType.getText().toString();
            String duration = etExerciseDuration.getText().toString();
            String date = etExerciseDate.getText().toString();
            String time = etExerciseTime.getText().toString();

            if (TextUtils.isEmpty(type)) {
                Toast.makeText(getActivity(), "Egzersiz Türü Giriniz.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(duration)) {
                Toast.makeText(getActivity(), "Egzersiz Süresi Giriniz.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(date)) {
                Toast.makeText(getActivity(), "Egzersiz Tarihi Giriniz.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(time)) {
                Toast.makeText(getActivity(), "Egzersiz Zamanı Giriniz.", Toast.LENGTH_SHORT).show();
                return;
            }

            mUsers = mAuth.getCurrentUser();
            if (mUsers != null) {
                String userId = mUsers.getUid();

                mFireStore.collection("exercise")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (querySnapshot.size() >= 20) {
                                Toast.makeText(getActivity(), "Egzersiz Eklenemedi. Egzersiz Geçmişini Temizle!.", Toast.LENGTH_SHORT).show();
                            } else {
                                mData = new HashMap<>();
                                mData.put("type", type);
                                mData.put("duration", duration);
                                mData.put("date", date);
                                mData.put("time", time);
                                mData.put("userId", userId);
                                mData.put("animalId", animalId);

                                mFireStore.collection("exercise")
                                        .add(mData)
                                        .addOnSuccessListener(documentReference -> {
                                            Toast.makeText(getActivity(), "Egzersiz bilgisi kaydedildi.", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getActivity(), "Egzersiz bilgisi kaydedilirken bir hata oluştu.", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getActivity(), "Egzersiz bilgileri alınırken bir hata oluştu.", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(getActivity(), "Kullanıcı oturumu açılmamış.", Toast.LENGTH_SHORT).show();
            }
        });

        btnH.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), exerciseHistory.class);
            startActivity(intent);
        });

        return view;
    }

    private void loadAnimalName(String animalId) {
        mFireStore.collection("animals").document(animalId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String animalName = documentSnapshot.getString("name");
                        tvAnimalName.setText(animalName); // Hayvan adını TextView'e ayarla
                    } else {
                        Toast.makeText(requireContext(), "Hayvan bulunamadı.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Hayvan adı yüklenirken hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
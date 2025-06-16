package com.serhatsgr.petcareapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodFragment extends Fragment {

    private TimePicker timePicker;
    private Button btnAddRoutine, btnSetAlarms;
    private RecyclerView recyclerView;
    private MealTimeAdapter adapter;
    private List<Calendar> mealTimes;
    private FirebaseFirestore db;
    private CollectionReference mealTimeRef;
    private String animalId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food, container, false);

        // Görünümleri tanımla
        timePicker = view.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);
        btnAddRoutine = view.findViewById(R.id.btn_add_routine);
        btnSetAlarms = view.findViewById(R.id.btn_set_alarms);
        recyclerView = view.findViewById(R.id.recycler_view_meal_times);

        // Firebase Firestore başlat
        db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("PetAppPrefs", Context.MODE_PRIVATE);
        animalId = sharedPreferences.getString("animalId", null);

        if (animalId != null) {
            mealTimeRef = db.collection("MealTimes").document(animalId).collection("Times");

            // Liste ve adapter ayarla
            mealTimes = new ArrayList<>();
            adapter = new MealTimeAdapter(mealTimes, this::deleteMealTime);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(adapter);

            // Butonların tıklama olaylarını ayarla
            btnAddRoutine.setOnClickListener(v -> addMealRoutine());
            btnSetAlarms.setOnClickListener(v -> setAlarms());

            // Mama saatlerini Firestore'dan yükle
            loadMealTimesFromFirestore();
        } else {
            Toast.makeText(getContext(), "Hayvan bilgisi yüklenemedi.", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void addMealRoutine() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
        calendar.set(Calendar.MINUTE, timePicker.getMinute());
        calendar.set(Calendar.SECOND, 0);

        mealTimes.add(calendar);
        adapter.notifyDataSetChanged();

        // Firestore'a kaydet
        Map<String, Object> mealData = new HashMap<>();
        mealData.put("hour", timePicker.getHour());
        mealData.put("minute", timePicker.getMinute());

        mealTimeRef.add(mealData)
                .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "Mama saati eklendi.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Mama saati eklenirken hata: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadMealTimesFromFirestore() {
        mealTimeRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            mealTimes.clear();
            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                int hour = doc.getLong("hour").intValue();
                int minute = doc.getLong("minute").intValue();

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                mealTimes.add(calendar);
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Mama saatleri yüklenirken hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setAlarms() {
        AlarmManager alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);

        for (Calendar alarmTime : mealTimes) {
            Intent intent = new Intent(requireActivity(), AlarmReceiver.class);
            intent.putExtra("hour", alarmTime.get(Calendar.HOUR_OF_DAY));
            intent.putExtra("minute", alarmTime.get(Calendar.MINUTE));

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    requireActivity(),
                    alarmTime.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
        }

        Toast.makeText(getActivity(), "Hatırlatıcılar kaydedildi ve kuruldu.", Toast.LENGTH_SHORT).show();
    }

    private void deleteMealTime(Calendar calendar) {
        mealTimeRef.whereEqualTo("hour", calendar.get(Calendar.HOUR_OF_DAY))
                .whereEqualTo("minute", calendar.get(Calendar.MINUTE))
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                    mealTimes.remove(calendar);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getActivity(), "Mama saati silindi.", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Mama saati silinirken hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
package com.serhatsgr.petcareapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

public class healtFragment extends Fragment {

    private TextView tvAnimalName; // Hayvan adı için TextView
    private FirebaseFirestore mFireStore;

    private TextView txtHealtContent;
    private HealtTips healttips;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Layout'ı şişir
        View view = inflater.inflate(R.layout.fragment_healt, container, false);

        txtHealtContent = view.findViewById(R.id.txtHealtInfoContent);
        healttips = new HealtTips();
        txtHealtContent.setText(healttips.getRandomTip());

        Button asiButton = view.findViewById(R.id.asiBtn);
        Button medicationButton = view.findViewById(R.id.addMedication);
        Button allergyButton = view.findViewById(R.id.btnAllergy);
        Button weightButton = view.findViewById(R.id.btnWeight);
        tvAnimalName = view.findViewById(R.id.animalNameLabel); // TextView'i başlat

        mFireStore = FirebaseFirestore.getInstance();

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("PetAppPrefs", Context.MODE_PRIVATE);
        String animalId = sharedPreferences.getString("animalId", null);

        if (animalId != null) {
            loadAnimalName(animalId); // Hayvan adını yükle
        } else {
            Toast.makeText(requireContext(), "Hayvan ID alınamadı.", Toast.LENGTH_SHORT).show();
        }

        asiButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), vaccinesPage.class);
            startActivity(intent);
        });

        medicationButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MedicationTrackingPage.class);
            startActivity(intent);
        });

        allergyButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AllergyPage.class);
            startActivity(intent);
        });

        weightButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotesActivity.class);
            intent.putExtra("animalId", animalId); // animalId'yi aktar
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
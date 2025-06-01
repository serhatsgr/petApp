package com.serhatsgr.petcareapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class petMainFragment extends Fragment {

    private Button txtPetName, txtPetType, txtPetGender, txtPetWeight;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private Button uptadeBtn;

    private String animalId;

    private TextView txtInterestingFactsContent;
    private InterestingFacts interestingFacts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Fragment layout'ını inflate edin
        View view = inflater.inflate(R.layout.fragment_pet_main, container, false);

        // Görünümleri tanımlayın
        txtPetName = view.findViewById(R.id.txtPetName);
        txtPetType = view.findViewById(R.id.txtPetType);
        txtPetGender = view.findViewById(R.id.txtPetGender);
        txtPetWeight = view.findViewById(R.id.txtPetWeight);
        uptadeBtn = view.findViewById(R.id.buttonUpdatePet);

        txtInterestingFactsContent = view.findViewById(R.id.txtInterestingFactsContent);

        // İlginç bilgiler nesnesini oluşturun
        interestingFacts = new InterestingFacts();
        txtInterestingFactsContent.setText(interestingFacts.getRandomFact());

        // Firebase bileşenlerini başlatın
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Animal ID'yi al
         animalId = getArguments() != null ? getArguments().getString("animalId") : null;
        if (animalId != null) {
            loadPetDetails(animalId);
        } else {
            Toast.makeText(getContext(), "Hayvan bilgisi yüklenemedi.", Toast.LENGTH_SHORT).show();
        }

        uptadeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aktiviteye geçiş yapmak için getActivity() kullanın
                Intent intent = new Intent(getActivity(), UpdatePetPage.class);
                intent.putExtra("animalId", animalId); // Geçerli animalId'yi gönderin
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
         loadPetDetails(animalId);
    }

    private void loadPetDetails(String animalId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mFirestore.collection("animals").document(animalId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Verileri al ve UI'yi güncelle
                                String petName = document.getString("name");
                                String petType = document.getString("type");
                                String petGender = document.getString("gender");
                                String petWeight = document.getString("weight");

                                txtPetName.setText("İsim: "+petName);
                                txtPetType.setText("Tür: "+petType);
                                txtPetGender.setText("Cinsiyet: "+petGender);
                                txtPetWeight.setText("Ağırlık: "+petWeight);
                            } else {
                                Toast.makeText(getContext(), "Evcil hayvan bulunamadı.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Evcil hayvan bilgileri yüklenemedi.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
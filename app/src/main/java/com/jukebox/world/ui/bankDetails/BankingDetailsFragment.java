package com.jukebox.world.ui.bankDetails;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jukebox.world.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BankingDetailsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userID;

    private Spinner spinnerBank;
    private EditText editTextAccountNumber;
    private EditText editTextAccountHolder;
    private EditText editTextBranchCode;
    private ArrayList<String> arrayListBanks = new ArrayList<>();
    private String bankName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_banking_details, container, false);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);

        spinnerBank = root.findViewById(R.id.spBankName);

        arrayListBanks.add("Capitec");
        arrayListBanks.add("African Bank");
        arrayListBanks.add("Nedbank");
        arrayListBanks.add("FNB");
        arrayListBanks.add("Absa");
        arrayListBanks.add("Standard Bank");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, arrayListBanks);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBank.setAdapter(arrayAdapter);
        spinnerBank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bankName = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        editTextAccountNumber = root.findViewById(R.id.etAccountNumber);
        editTextAccountHolder = root.findViewById(R.id.etAccountHolder);
        editTextBranchCode = root.findViewById(R.id.etBranchCode);

        Button button = root.findViewById(R.id.btnSaveBankDetails);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String accountNumber = editTextAccountNumber.getText().toString();
                String accountHolder = editTextAccountHolder.getText().toString();
                String branchCode = editTextBranchCode.getText().toString();

                if (accountNumber == null || accountNumber.isEmpty()) {
                    editTextAccountNumber.setError("account number required");
                    return;

                } else if (accountHolder == null || accountHolder.isEmpty()) {
                    editTextAccountHolder.setError("account holder required");
                    return;

                } else if (branchCode == null || branchCode.isEmpty()) {
                    editTextBranchCode.setError("branch code required");
                    return;

                } else if (bankName == null || bankName.isEmpty()) {
                    Toast.makeText(getActivity(), "Please select bank", Toast.LENGTH_LONG).show();
                    return;
                }

                Map accountInfo = new HashMap();
                accountInfo.put("accountNumber", accountNumber);
                accountInfo.put("accountHolder", accountHolder);
                accountInfo.put("bankName", bankName);
                accountInfo.put("branchCode", branchCode);

                mDatabase.child("bankingDetails").updateChildren(accountInfo);
            }
        });

        showUserAccountInfo();

        return root;
    }

    private void showUserAccountInfo() {

        mDatabase.child("bankingDetails").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("accountNumber") != null) {
                        editTextAccountNumber.setText(map.get("accountNumber").toString());
                    }
                    if (map.get("accountHolder") != null) {
                        editTextAccountHolder.setText(map.get("accountHolder").toString());
                    }
                    if (map.get("bankName") != null) {
                        spinnerBank.setSelection(arrayListBanks.indexOf(map.get("bankName").toString()));
                    }
                    if (map.get("branchCode") != null) {
                        editTextBranchCode.setText(map.get("branchCode").toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

}
package ristevski.petar.pc.proektna;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

public class DialogFragmentClass extends DialogFragment {
private DataEntryListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    mListener = (DataEntryListener) activity;
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.dataentrydialog, container, false);
        Button bttnOk = rootView.findViewById(R.id.btnOk);
     final   EditText opisNaUsluga = rootView.findViewById(R.id.etOpisNaUsluga);
      final   EditText cenaNaUsluga = rootView.findViewById(R.id.etCena);
        final CheckBox cb = rootView.findViewById(R.id.checkBoxCena);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(cb.isChecked()){
                    cenaNaUsluga.setEnabled(true);
                } else {
                    cenaNaUsluga.setEnabled(false);
                }
            }
        });


        bttnOk.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {

                String usluga = opisNaUsluga.getText().toString();
                String cena =  cenaNaUsluga.getText().toString();
                if(usluga.length() != 0) {

                    mListener.onDataEntryCompleted(usluga, cena);
                    dismiss();
                }
           }
        });



        Button bttnCancel = rootView.findViewById(R.id.btnCancel);
        bttnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
               mListener.onDataEntryCompleted("","");
                dismiss();
            }
        });
        return rootView;
    }



    public interface  DataEntryListener{
        void onDataEntryCompleted(String usluga, String cena);
         }
}

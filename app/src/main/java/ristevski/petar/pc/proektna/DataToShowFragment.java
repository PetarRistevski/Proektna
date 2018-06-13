package ristevski.petar.pc.proektna;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class DataToShowFragment extends DialogFragment {

    public static final String USLUGA_KEY = "USLUGA_KEY";

    public static DataToShowFragment newInstance(Usluga usluga) {
        
        Bundle args = new Bundle();
        args.putParcelable(USLUGA_KEY, usluga);
        DataToShowFragment fragment = new DataToShowFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.datashowdialog, container, false);
        TextView tvOpis = rootView.findViewById(R.id.tvOpisUsluga);
        TextView tvCena = rootView.findViewById(R.id.tvCenaNaUsluga);


        Button Cancel = rootView.findViewById(R.id.nazad);

        Usluga usluga = getArguments().getParcelable(USLUGA_KEY);

        tvOpis.setText(usluga.getOpis());
        if(usluga.getCena().equals("0")){
            tvCena.setText("Корисникот не е во можност да плати за побараната услуга, и би сакал хуманитарна услуга");
        }else {
            tvCena.setText(usluga.getCena());
        }

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                dismiss();

            }
        });

        return rootView;
    }
}

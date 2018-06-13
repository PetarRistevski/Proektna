package ristevski.petar.pc.proektna;

import android.os.Parcel;
import android.os.Parcelable;

public class Usluga implements Parcelable{
    private String opis;
    private String cena;

    public Usluga() {

    }



    public Usluga(String opis, String cena) {
        this.opis = opis;
        this.cena = cena;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public String getCena() {
        return cena;
    }

    public void setCena(String cena) {
        this.cena = cena;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.opis);
        parcel.writeString(this.cena);


    }
    protected Usluga (Parcel in){
        this.opis = in.readString();
        this.cena = in.readString();
    }
    public static final Creator<Usluga> CREATOR = new Creator<Usluga>() {
        @Override
        public Usluga createFromParcel(Parcel parcel) {
            return new Usluga(parcel);
        }

        @Override
        public Usluga[] newArray(int Size) {
            return new Usluga[Size];
        }
    };
}


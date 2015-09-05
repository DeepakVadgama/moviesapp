package deepakvadgama.com.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Trailer implements Parcelable {

    private String trailerUrl;

    public Trailer(Parcel in) {
        this.trailerUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(trailerUrl);
    }

    public static final Parcelable.Creator<Trailer> CREATOR = new Parcelable.Creator<Trailer>() {
        public Trailer createFromParcel(Parcel in) {
            return new Trailer(in);
        }

        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };
}

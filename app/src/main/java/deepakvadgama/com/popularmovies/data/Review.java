package deepakvadgama.com.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Review implements Parcelable {

    private String author;
    private String reviewText;

    public Review(Parcel in) {
        this.author = in.readString();
        this.reviewText = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(author);
        dest.writeString(reviewText);
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        public Review[] newArray(int size) {
            return new Review[size];
        }
    };
}

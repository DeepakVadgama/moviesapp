package deepakvadgama.com.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Movie implements Parcelable {

    private String id;
    private String title;
    private Date releaseDate;
    private double voteAverage = -1d;
    private String plotSynopsis;
    private String imagePath;
    private boolean isFavorite = false;

    public Movie() {
    }

    public Movie(String id,
                 String title,
                 Date releaseDate,
                 double voteAverage,
                 String plotSynopsis,
                 String imagePath,
                 boolean isFavorite,
                 List<Trailer> trailerList,
                 List<Review> reviewList) {
        this.id = id;
        this.title = title;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
        this.plotSynopsis = plotSynopsis;
        this.imagePath = imagePath;
        this.isFavorite = isFavorite;
    }

    public Movie(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.voteAverage = in.readDouble();
        this.plotSynopsis = in.readString();
        this.imagePath = in.readString();
        this.isFavorite = in.readByte() != 0;
        long time = in.readLong();
        if (time < 0) {
            releaseDate = null;
        } else {
            releaseDate = new Date(time);
        }
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeDouble(voteAverage);
        dest.writeString(plotSynopsis);
        dest.writeString(imagePath);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
        if (releaseDate != null) {
            dest.writeLong(releaseDate.getTime());
        } else {
            dest.writeLong(-1l);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getPlotSynopsis() {
        return plotSynopsis;
    }

    public void setPlotSynopsis(String plotSynopsis) {
        this.plotSynopsis = plotSynopsis;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Movie that = (Movie) o;

        return !(title != null ? !title.equals(that.title) : that.title != null);

    }

    @Override
    public int hashCode() {
        return title != null ? title.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Movie{title='" + title + '\'' + '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }
}

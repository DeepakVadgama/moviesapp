package deepakvadgama.com.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Movie implements Parcelable {

    private String title;
    private Date releaseDate;
    private double voteAverage = -1d;
    private String plotSynopsis;
    private String imagePath;

    public Movie() {
    }

    public Movie(String title, Date releaseDate, double voteAverage, String plotSynopsis, String imagePath) {
        this.title = title;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
        this.plotSynopsis = plotSynopsis;
        this.imagePath = imagePath;
    }

    public Movie(Parcel in) {
        this.title = in.readString();
        this.voteAverage = in.readDouble();
        this.plotSynopsis = in.readString();
        this.imagePath = in.readString();
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeDouble(voteAverage);
        dest.writeString(plotSynopsis);
        dest.writeString(imagePath);
        if (releaseDate != null) {
            dest.writeLong(releaseDate.getTime());
        } else {
            dest.writeLong(-1l);
        }
    }
}

package uk.openvk.android.legacy.api.entities;


public class Audio {
    public String unique_id;
    public long id;
    public String artist;
    public String title;
    public String album;
    private int duration_sec;
    private String duration;
    public String genre;
    public boolean is_explicit;
    public String lyrics;
    public String url;
    public User sender;
    public Audio() {
    }

    public void setDuration(int duration_sec) {
        this.duration_sec = duration_sec;
        this.duration =
                String.format(
                        "%s:%s",
                        Math.floor((double)duration_sec / 60),
                        duration_sec % 60
                );
    }

    public String getDuration() {
        return duration;
    }

    public int getDurationInSeconds() {
        return duration_sec;
    }
}

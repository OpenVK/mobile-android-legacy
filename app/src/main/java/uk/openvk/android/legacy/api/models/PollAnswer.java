package uk.openvk.android.legacy.api.models;

/**
 * Created by Dmitry on 16.10.2022.
 */

public class PollAnswer {
    public int id;
    public int rate;
    public int votes;
    public String text;
    public boolean is_voted;
    public PollAnswer(int id, int rate, int votes, String text) {
        this.id = id;
        this.rate = rate;
        this.votes = votes;
        this.text = text;
    }
}

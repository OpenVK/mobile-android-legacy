package uk.openvk.android.legacy.api.models;

import java.util.ArrayList;

import uk.openvk.android.legacy.api.entities.Audio;
import uk.openvk.android.legacy.api.wrappers.JSONParser;

public class Audios {
    private JSONParser jsonParser;
    private ArrayList<Audio> audios;
    public Audios() {
        jsonParser = new JSONParser();
        audios = new ArrayList<>();
    }
}

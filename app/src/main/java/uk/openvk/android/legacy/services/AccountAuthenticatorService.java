package uk.openvk.android.legacy.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.net.Authenticator;

import uk.openvk.android.legacy.OvkAuthenticator;

public class AccountAuthenticatorService extends Service {

    private OvkAuthenticator auth;

    @Override
    public IBinder onBind(Intent intent) {
        auth = new OvkAuthenticator(this);
        return auth.getIBinder();
    }
}


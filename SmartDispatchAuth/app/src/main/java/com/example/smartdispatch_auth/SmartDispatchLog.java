package com.example.smartdispatch_auth;
import android.content.Context;
import android.widget.Toast;

import org.acra.*;
import org.acra.annotation.*;

@AcraCore(buildConfigClass = BuildConfig.class)
@AcraMailSender(mailTo = "monilsoni99@gmail.com")
@AcraToast(resText=R.string.acra_toast_text,
        length = Toast.LENGTH_LONG)

public class SmartDispatchLog extends UserClient {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // The following line triggers the initialization of ACRA
        ACRA.init(this);

    }
}

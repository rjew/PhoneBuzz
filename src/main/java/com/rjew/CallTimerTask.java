package com.rjew;

import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.TimerTask;

class CallTimerTask extends TimerTask {
    public static final String twilioNumber = "4158013853";
    private String number;

    public CallTimerTask(String number) {
        this.number = number;
    }

    @Override
    public void run() {
        try {
            Call call = Call.creator(new PhoneNumber(number),
                    new PhoneNumber(twilioNumber),
                    new URI("https://salty-retreat-88070.herokuapp.com/twiml")).create();
        } catch (URISyntaxException ex) {
            System.err.println(ex.toString());
            System.exit(-1);
        }
    }
}

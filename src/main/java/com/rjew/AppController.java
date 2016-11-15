package com.rjew;

import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.twilio.twiml.Say;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.Gather;
import com.twilio.twiml.Method;

import com.twilio.security.RequestValidator;
import com.twilio.Twilio;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AppController {
    public static final String ACCOUNT_SID = "ACd2301816a4d34f79b2920010cacb1dba";
    public static final String AUTH_TOKEN = "d9f359ca97607b11b5289df7371067bd";

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("number", new Number());
        return "number";
    }

    @PostMapping("/")
    public String call(HttpServletRequest request, HttpServletResponse response, Model model) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        //schedule phone call delay seconds
        new java.util.Timer().schedule(
                new CallTimerTask(request.getParameter("num")),
                Integer.parseInt(request.getParameter("delay")) * 1000 //milliseconds
        );

        model.addAttribute("number", new Number());
        return "number";
    }

    @RequestMapping("/twiml")
    @ResponseBody
    public String service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Initialize the validator
        RequestValidator validator = new RequestValidator(AUTH_TOKEN);

        String url = "https://salty-retreat-88070.herokuapp.com/twiml";

        Map<String, String> params = new HashMap<>();

        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            String val = request.getParameter(key);
            params.put(key, val);
        }

        if (!validator.validate(url, params, request.getHeader("X-Twilio-Signature"))) {
            return "Access Denied";
        }

        // Create a TwiML response and add friendly message.
        VoiceResponse voiceResponse = new VoiceResponse.Builder()
                .gather(new Gather.Builder()
                                .action("/handle-key")
                                .method(Method.POST)
                                .timeout(10)
                                .finishOnKey("#")
                                .say(new Say
                                        .Builder("Enter a number, followed by the pound sign")
                                        .build())
                                .build()
                )
                .say(new Say
                        .Builder("We didn't receive any input. Goodbye!") //Fallback if user does not enter any digits
                        .build())
                .build();

        response.setContentType("application/xml");
        try {
            response.getWriter().print(voiceResponse.toXml());
        } catch (TwiMLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @RequestMapping("/handle-key")
    @ResponseBody
    public void handleKey(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String digits = request.getParameter("Digits");
        int num = Integer.parseInt(digits);
        StringBuilder fizzBuzz = new StringBuilder();

        for (int i = 1; i <= num; i++) {
            if (i % 3 == 0 && i % 5 == 0) {
                fizzBuzz.append("Fizz Buzz ");
            } else if (i % 3 == 0) {
                fizzBuzz.append("Fizz ");
            } else if (i % 5 == 0) {
                fizzBuzz.append("Buzz ");
            } else {
                fizzBuzz.append(i);
                fizzBuzz.append(" ");
            }
        }

        VoiceResponse voiceResponse = new VoiceResponse.Builder()
                .say(new Say.Builder(fizzBuzz.toString()).build())
                .build();

        response.setContentType("application/xml");
        try {
            response.getWriter().print(voiceResponse.toXml());
        } catch (TwiMLException e) {
            e.printStackTrace();
        }
    }
}

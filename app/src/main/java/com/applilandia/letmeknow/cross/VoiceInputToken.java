package com.applilandia.letmeknow.cross;

import java.util.Locale;

/**
 * Created by JuanCarlos on 20/03/2015.
 * This class is used to parse a phrase received by an intent recognizer
 */
public class VoiceInputToken {

    private String mTaskName;
    //Phrase received through the intent recognizer
    private String mPhrase;

    public VoiceInputToken(String phrase) {
        mPhrase = phrase;
    }

    /**
     * Parse the phrase to get the task name.
     * TODO: In this moment the whole phrase is the task name.
     * TODO: Improve it to include date and time
     */
    private void parseTaskName() {
        mTaskName = mPhrase;
        if (mPhrase.length() > 0) {
            mTaskName = mPhrase.substring(0, 1).toUpperCase(Locale.getDefault());
            if (mPhrase.length() > 1) {
                mTaskName += mPhrase.substring(1);
            }
        }
    }

    /**
     * Parse the received phrase to get the different parts
     */
    public void parse() {
        parseTaskName();
    }

    /**
     * Return the task name obtained from the voice phrase
     *
     * @return
     */
    public String getTaskName() {
        return mTaskName;
    }
}

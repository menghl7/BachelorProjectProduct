package com.bachelorproject.lokeshmenghani.contactdictator;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by lokesh menghani.
 */
public class repeatNumRecognitionListener implements RecognitionListener {

    private String contactName;
    private String contactNum;
    private String numType;
    private TextToSpeech mTts;
    private SpeechRecognizer repeatNumSR;
    private callOrGetRecognitionListener rep;
    private String searchAgain = "To search again, press anywhere on the screen when ever you are ready.";
    HashMap<String, String> myHashName = new HashMap<String, String>();

    public repeatNumRecognitionListener(SpeechRecognizer repeatNumSR, String name, String number, String type)
    {
        this.mTts = IntroActivity.getmTts();
        this.repeatNumSR = repeatNumSR;
        this.contactName = name;
        this.contactNum = number;
        this.numType = type;
        this.rep = new callOrGetRecognitionListener();
    }


    @Override
    public void onReadyForSpeech(Bundle params) {
        myHashName.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ENABLE BUTTON");
        mTts.setOnUtteranceCompletedListener(new MyUtteranceCompletedListener());
        //Log.d(TAG, "onReadyForSpeech");
    }

    @Override
    public void onBeginningOfSpeech() {
        //Log.d(TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onRmsChanged(float rmsdB) {

//        Log.d(TAG, "onRmsChanged");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        //Log.d(TAG, "onBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {
        //Log.d(TAG, "onEndofSpeech");
    }

    @Override
    public void onError(int error) {
        // depending on the error output an error message
        repeatNumSR.stopListening();
        repeatNumSR.destroy();

        String errorMsg = "";
        if(error == 1 || error == 2){
            // network problem;
            errorMsg = "There was a problem with the network";
        }
        else if (error == 3){
            // audio problem
            errorMsg = "There was a problem with the audio.";
        }
        else if (error == 5){
            // client side
            errorMsg = "Your answer could not be understood.";
        }
        else if (error == 9){
            // permission error
            errorMsg = "Permission for speech recognition activity was not granted.";
        }
        else if (error == 7){
            // couldn't convert to string
            errorMsg = "Your answer could not be understood.";
        }
        else if (error == 4){
            // server sends error
            errorMsg = "There was a problem with the server.";
        }
        else if (error == 6){
            // speech timeout, nothing was spoken
            errorMsg = "Timeout, no answer was heard.";
        }
        mTts.speak(errorMsg, TextToSpeech.QUEUE_ADD, null);
        mTts.speak(searchAgain, TextToSpeech.QUEUE_ADD, myHashName);
    }

    @Override
    public void onResults(Bundle results) {
        repeatNumSR.stopListening();
        repeatNumSR.destroy();
        String confirmation = "";
        String yes = "yes";
        String no = "no";


        ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        for (int i = 0; i < data.size(); i++)
        {

            if(((String) data.get(i)).equals(yes)){
                confirmation = yes;
            }
            else if (((String) data.get(i)).equals(no)){
                confirmation = no;
            }

        }

        if(confirmation == yes){
            rep.dictateNumber(contactName, numType, contactNum);
        }

        else if(confirmation == no){
            mTts.speak(searchAgain, TextToSpeech.QUEUE_ADD, myHashName);
        }

        else if (confirmation == ""){
            mTts.speak("Your answer could not be understood.", TextToSpeech.QUEUE_ADD, null);
            mTts.speak(searchAgain, TextToSpeech.QUEUE_ADD, myHashName);
        }


    }

    @Override
    public void onPartialResults(Bundle partialResults) {
//        Log.d(TAG, "onPartialResults");
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
//        Log.d(TAG, "onEvent " + eventType);
    }


}


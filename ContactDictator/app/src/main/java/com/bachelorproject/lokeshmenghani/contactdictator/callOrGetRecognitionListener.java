package com.bachelorproject.lokeshmenghani.contactdictator;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by lokesh menghani.
 */
public class callOrGetRecognitionListener implements RecognitionListener {

    private String contactName;
    private String contactNum;
    private String numType;
    private TextToSpeech mTts = IntroActivity.getmTts();
    private SpeechRecognizer callOrGetSR;
    private String searchAgain = "To search again, press anywhere on the screen when ever you are ready.";
    HashMap<String, String> myHashName = new HashMap<String, String>();
    public static int counter = 0;

    public callOrGetRecognitionListener(){}

    public callOrGetRecognitionListener(
            SpeechRecognizer callOrGetSR,
            String contactName,
            String numType,
            String contactNum)
    {
        this.contactName = contactName;
        this.callOrGetSR = callOrGetSR;
        this.numType = numType;
        this.contactNum = contactNum;

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
        callOrGetSR.stopListening();
        callOrGetSR.destroy();



        String errorMsg = "";
        // couldn't convert to string.
        if(error == 7) {
            askAgain();
        }
        else{
            if (error == 1 || error == 2) {
                // network problem;
                errorMsg = "There was a problem with the network";
            } else if (error == 3) {
                // audio problem
                errorMsg = "There was a problem with the audio.";
            } else if (error == 5) {
                // client side
                errorMsg = "There was a problem with the recognition activity.";
            } else if (error == 9) {
                // permission error
                errorMsg = "Permission for speech recognition activity was not granted.";
            } else if (error == 4) {
                // server sends error
                errorMsg = "There was a problem with the server.";
            } else if (error == 6) {
                // speech timeout, nothing was spoken
                errorMsg = "Timeout, no answer was heard.";
            }
            mTts.speak(errorMsg, TextToSpeech.QUEUE_ADD, null);
            mTts.speak(searchAgain, TextToSpeech.QUEUE_ADD, myHashName);
        }
    }

    @Override
    public void onResults(Bundle results) {
        callOrGetSR.stopListening();
        callOrGetSR.destroy();
        String confirmation = "";
        String call = "call";
        String get = "get";


        ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        for (int i = 0; i < data.size(); i++)
        {

            if(((String) data.get(i)).equals(call)){
                confirmation = call;
            }
            else if (((String) data.get(i)).equals(get)){
                confirmation = get;
            }

        }

        if(numType != "") {
            //Log.d("NUMTYPE ", numType);
            //Log.d("CONFIRMATION ", confirmation);
            if(confirmation == call){
                // call number
                callNumber(contactNum);
            }
            else if(confirmation == get){
                // dictate number
                dictateNumber(contactName, numType, contactNum);
            }
            else if (confirmation == ""){
                askAgain();
            }
        }
        else{
            mTts.speak("Your answer could not be understood or there are no numbers associated for " + contactName + ".", TextToSpeech.QUEUE_ADD, null);
            mTts.speak(searchAgain, TextToSpeech.QUEUE_ADD, myHashName);
        }


    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        //Log.d(TAG, "onPartialResults");
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        //Log.d(TAG, "onEvent " + eventType);
    }

    private void askAgain(){
        mTts.speak("Your answer could not be understood.", TextToSpeech.QUEUE_ADD, null);
        IntroActivity.setCounter(IntroActivity.getCounter() + 1);
        //Log.d("COUNTER ", IntroActivity.getCounter() + " ");
        if(IntroActivity.getCounter() < 3) {
            //Log.d("Chance", "ANother chance should be given");
            mTts.speak("Could you repeat it.", TextToSpeech.QUEUE_ADD, null);
            while(mTts.isSpeaking()){
                // do nothing
            }


            if(!mTts.isSpeaking()) {
                SpeechRecognizer caOGe = IntroActivity.getRecognizeCallOrGet();
                caOGe.setRecognitionListener(new callOrGetRecognitionListener(caOGe, contactName, numType, contactNum));

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

                caOGe.startListening(intent);

            }
        }else{
            IntroActivity.setCounter(0);
            mTts.speak(searchAgain, TextToSpeech.QUEUE_ADD, myHashName);
        }

    }

    private void callNumber(String num){
        IntroActivity.getSearchButton().setEnabled(true);
        String number = "tel: " + num;
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number));
        if(callIntent != null) {
            IntroActivity.getCustomAppContext().startActivity(callIntent);
        }

    }

    public void dictateNumber(String name, String type, String number){

        String phoneTypeInfo = "The " + type + " number of " + name + " is the following:";
        mTts.speak(phoneTypeInfo, TextToSpeech.QUEUE_ADD, null);


        int len = number.length();
        char[] digit = number.toCharArray();
        for (int j = 0; j < len; j++) {
            if (digit[j] == '+' || Character.isDigit(digit[j])) {

                mTts.speak(digit[j] + "", TextToSpeech.QUEUE_ADD, null);
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }

        askToRepeatNumber(name, number, type);

    }

    public void askToRepeatNumber(String name, String number, String type) {
        mTts.speak("Would you like to hear the number again? ", TextToSpeech.QUEUE_ADD, null);
        mTts.speak("Answer after the beep, in yes or no. ", TextToSpeech.QUEUE_ADD, null);

        while (mTts.isSpeaking()) {
            // do nothing
        }


        if (!mTts.isSpeaking()) {
            SpeechRecognizer repeatNumSR = IntroActivity.getRepeatNumSR();

            repeatNumSR.setRecognitionListener(new repeatNumRecognitionListener(repeatNumSR, name, number, type));

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

            repeatNumSR.startListening(intent);

        }
    }

}

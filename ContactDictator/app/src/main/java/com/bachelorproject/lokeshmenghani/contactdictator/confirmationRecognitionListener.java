package com.bachelorproject.lokeshmenghani.contactdictator;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by lokesh menghani.
 */
public class confirmationRecognitionListener implements RecognitionListener {

    private String contactName;
    private SpeechRecognizer confirmationSR;
    private ArrayList<HashMap<String, String>> numberType;
    private TextToSpeech mTts = IntroActivity.getmTts();
    private ArrayList<String> names;
    private String searchAgain = "To search again, press anywhere on the screen when ever you are ready.";
    HashMap<String, String> myHashName = new HashMap<String, String>();

    public confirmationRecognitionListener(SpeechRecognizer confirmationSR, ArrayList<String> contactNames, ArrayList<HashMap<String, String>> numberType){
        this.numberType = numberType;
        this.confirmationSR = confirmationSR;
        this.names = contactNames;
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
        confirmationSR.stopListening();
        confirmationSR.destroy();

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
        confirmationSR.stopListening();
        confirmationSR.destroy();

        String confirmation = "";
        String yes = "yes";
        String no = "no";
        String index = "1";


        ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        for (int i = 0; i < data.size(); i++)
        {
            //Log.d("DATA ", data.get(i) + " ");
            if(names.size() == 1) {

                if (((String) data.get(i)).equals(yes)) {
                    confirmation = yes;
                } else if (((String) data.get(i)).equals(no)) {
                    confirmation = no;
                }
            }

            if(names.size() > 1) {
                for(int j = 0; j < names.size(); j++) {
                    index = Integer.toString(j+1);
                    //Log.d("INDEX ", index);
                    if (((String) data.get(i)).equals(index)) {
                        confirmation = index;
                        break;
                    }
                }
                break;
            }

        }

        if(confirmation == yes){
            HashMap<String, String> numType = numberType.get(0);
            if(numType.size() > 0) {
                informAvailableNumType(numType);
                askNumType(names.get(0), numType);
            }
            else{
                mTts.speak("Your answer could not be understood or there are no numbers associated for " + contactName + ".", TextToSpeech.QUEUE_ADD, null);
                mTts.speak(searchAgain, TextToSpeech.QUEUE_ADD, myHashName);
            }
        }
        else if(confirmation == index){
            int elem = Integer.parseInt(index) - 1;
            HashMap<String, String> numType = numberType.get(elem);
            if(numType.size() > 0) {
                informAvailableNumType(numType);
                askNumType(names.get(elem), numType);
            }
            else{
                mTts.speak("Your answer could not be understood or there are no numbers associated for " + contactName + ".", TextToSpeech.QUEUE_ADD, null);
                mTts.speak(searchAgain, TextToSpeech.QUEUE_ADD, myHashName);
            }
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
        //Log.d(TAG, "onPartialResults");
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        //Log.d(TAG, "onEvent " + eventType);
    }

    // this method informs the user they available number types the contact has.
    private void informAvailableNumType(HashMap<String, String> numberTypes){

        Set<String> listOfAvailableTypes = numberTypes.keySet();

        mTts.speak("Available phone types are the following: ", TextToSpeech.QUEUE_ADD, null);

        for (String s : listOfAvailableTypes){
            mTts.speak(s, TextToSpeech.QUEUE_ADD, null);
        }

        mTts.speak("Please choose one.", TextToSpeech.QUEUE_ADD, null);

        while(mTts.isSpeaking()){

            // Do nothing as we have to wait for until the user is informed.

        }


    }


    // this method allows the user to say which number he wants.
    private void askNumType(String contactName, HashMap<String, String> numType){
        if(!mTts.isSpeaking()) {
            SpeechRecognizer numTypSR = IntroActivity.getRecognizeNumberType();
            numTypSR.setRecognitionListener(new numberTypeRecognitionListener(numTypSR, contactName, numType));

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

            numTypSR.startListening(intent);

        }
    }



}
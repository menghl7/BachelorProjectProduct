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
public class numberTypeRecognitionListener implements RecognitionListener {
    private String contactName;
    private SpeechRecognizer numberTypeSR;
    private HashMap<String, String> numberType;
    private TextToSpeech mTts = IntroActivity.getmTts();
    private String searchAgain = "To search again, press anywhere on the screen when ever you are ready.";
    HashMap<String, String> myHashName = new HashMap<String, String>();


    public numberTypeRecognitionListener(SpeechRecognizer numberTypeSR, String contactName, HashMap<String, String> numberType){
        this.contactName = contactName;
        this.numberType = numberType;
        this.numberTypeSR = numberTypeSR;
    }


    @Override
    public void onReadyForSpeech(Bundle params) {
        myHashName.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ENABLE BUTTON");
        mTts.setOnUtteranceCompletedListener(new MyUtteranceCompletedListener());
        //Log.d(TAG, "onReadyForSpeech");
    }

    @Override
    public void onBeginningOfSpeech() {
        //.d(TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onRmsChanged(float rmsdB) {

//        Log.d(TAG, "onRmsChanged");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        // Log.d(TAG, "onBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {
        //Log.d(TAG, "onEndofSpeech");
    }

    @Override
    public void onError(int error) {
        // depending on the error output an error message
        numberTypeSR.stopListening();
        numberTypeSR.destroy();

        String errorMsg = "";
        // couldn't convert to string.
        if(error == 7) {
            askAgain();
        }
        else {
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
        String type = "";
        String contactNum;

        numberTypeSR.stopListening();
        numberTypeSR.destroy();

        ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        for (int i = 0; i < data.size(); i++)
        {
            type = findNumType((String) data.get(i), numberType.keySet(), type);
        }

        if(type != "") {
            contactNum = outputNum(type);
            askToCallOrGetNum(type, contactNum);
        }
        else{
            askAgain();
//            mTts.speak("Your answer could not be understood or the phone number type you requested does not exist.", TextToSpeech.QUEUE_ADD, null);
//            mTts.speak(searchAgain, TextToSpeech.QUEUE_ADD, myHashName);
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
        IntroActivity.setCounter2(IntroActivity.getCounter2() + 1);
        //Log.d("COUNTER ", IntroActivity.getCounter2() + " ");
        if(IntroActivity.getCounter2() < 3) {
            //Log.d("Chance", "Another chance should be given");
            mTts.speak("Could you repeat it.", TextToSpeech.QUEUE_ADD, null);
            while(mTts.isSpeaking()){

                // Do nothing as we have to wait for until the user is informed.

            }

            if(!mTts.isSpeaking()) {
                SpeechRecognizer numTypSR = IntroActivity.getRecognizeNumberType();
                numTypSR.setRecognitionListener(new numberTypeRecognitionListener(numTypSR, contactName, numberType));

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

                numTypSR.startListening(intent);

            }
        }else{
            mTts.speak("Or the phone number type you requested does not exist.", TextToSpeech.QUEUE_ADD, null);
            IntroActivity.setCounter2(0);
            mTts.speak(searchAgain, TextToSpeech.QUEUE_ADD, myHashName);
        }

    }

    // this method allows the user to decide if he wants to call or get the user's number.
    private void askToCallOrGetNum(String type, String contactNum){
        mTts.speak("To perform a direct call. Say ", TextToSpeech.QUEUE_ADD, null);
        mTts.speak("call, ", TextToSpeech.QUEUE_ADD, null);
        mTts.speak("after the beep. ", TextToSpeech.QUEUE_ADD, null);

        mTts.speak("To get the number dictated. Say ", TextToSpeech.QUEUE_ADD, null);
        mTts.speak("get, ", TextToSpeech.QUEUE_ADD, null);
        mTts.speak("after the beep. ", TextToSpeech.QUEUE_ADD, null);

        while(mTts.isSpeaking()){
            // do nothing
        }


        if(!mTts.isSpeaking()) {
            SpeechRecognizer caOGe = IntroActivity.getRecognizeCallOrGet();
            caOGe.setRecognitionListener(new callOrGetRecognitionListener(caOGe, contactName, type, contactNum));

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

            caOGe.startListening(intent);

        }
    }

    // find the phone number type chosen.
    private String findNumType(String inputData, Set<String> numTypes, String type){
        String copyType;

        for(String phoneType : numTypes){
            copyType = phoneType;
            if (inputData.equals(copyType.toLowerCase())) {
                type = phoneType;
                break;
            }

            // this is done in case there is noise around when the user says the contact name.
            else if (inputData.contains(copyType.toLowerCase())){
                type = phoneType;
            }
        }
        return type;
    }


    private String outputNum(String type){

        String contactNumber;
        contactNumber = numberType.get(type);
        return contactNumber;

    }
}

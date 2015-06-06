package com.bachelorproject.lokeshmenghani.contactdictator;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by lokesh menghani.
 */
public class nameRecognitionListener implements RecognitionListener {
    private SpeechRecognizer nameSR;
    private HashMap<String, HashMap<String, String>> phoneDir;
    private TextToSpeech mTts = IntroActivity.getmTts();
    private String searchAgain = "To search again, press anywhere on the screen when ever you are ready.";
    HashMap<String, String> myHashName = new HashMap<String, String>();

    public nameRecognitionListener(SpeechRecognizer nameSR){
        this.nameSR = nameSR;
        this.phoneDir = IntroActivity.getPhoneDirectory();
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
        nameSR.stopListening();
        nameSR.destroy();

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
        nameSR.stopListening();
        nameSR.destroy();

        String contactName = "";
        // Log.d(TAG, "onResults " + results);

        ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String length = " ";
        int len = data.size();


        Log.d("Length of array ", (length + len));

        for (int i = 0; i < data.size(); i++)
        {
            Log.d("onResults ", (String) data.get(i));
            contactName = findContactName((String) data.get(i), contactName);
            if(checkFoundContactName(contactName)){
                break;
            }
            Log.d("Contact name ", contactName);

        }

        if(contactName != "") {
            confirmContactName(contactName);
        }
        else{
            mTts.speak("Your answer could not be understood or the contact you requested does not exist.", TextToSpeech.QUEUE_ADD, null);
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

    // this method returns a contact name if it is matched to the input otherwise it returns empty string.
    private String findContactName(String inputData, String contactName){
        String copyName;
        inputData = inputData.toLowerCase();
        String copyInputData = inputData.replaceAll("\\s+","");


        // Here we check if there is a contact with the same first name in case the user just says
        // only the first name
        for(String name : phoneDir.keySet()){
            String[] names = name.split(" ");
            if(names.length > 1) {
                copyName = names[0];


                if (inputData.equals(copyName.toLowerCase())) {
                    contactName = copyName;
                    break;
                }

            }
        }

        // Here we check for an exact match.
        for(String name : phoneDir.keySet()){
            copyName = name;
            if (inputData.equals(copyName.toLowerCase())) {
                contactName = name;
                break;
            }

            // this is done in case there is noise around when the user says the contact name.
            else if (copyInputData.contains(copyName.toLowerCase())){
                contactName = name;
            }
        }

        int threshold = 2;
        int levDistance = 0;

        // if still no contact name has been matched then run the Levenshtein distance algorithm.
        if(contactName == ""){
            for(String name : phoneDir.keySet()){

                copyName = name;
                levDistance = getLevenshteinDistance(inputData, copyName.toLowerCase(), threshold);

                if(levDistance > -1 && levDistance <= threshold){
                    threshold = levDistance;
                    contactName = name;
                }
            }
        }

        return contactName;
    }

    // if a contact name was found at first return true so searching can be stopped.
    private boolean checkFoundContactName(String contactName) {
        for (String name : phoneDir.keySet()) {
            if (contactName.equals(name)) {
                Log.d("BOOLEAN ", "TRUE");
                return true;
            }
        }
        return false;
    }

    private void confirmContactName(String contactName){
        ArrayList<String> sameNames = new ArrayList<String>();
        ArrayList<HashMap<String, String>> numTypes = new ArrayList<HashMap<String, String>>();

        for(String name : phoneDir.keySet()){
            String[] names = name.split(" ");
            if(names.length > 1) {
                if(contactName.equals(names[0])){
                    sameNames.add(name);
                }

            }
        }

        if(sameNames.size() > 1){
            for(int i = 0; i < sameNames.size(); i++){
                numTypes.add(phoneDir.get(sameNames.get(i)));
                mTts.speak("Say, " + (i + 1), TextToSpeech.QUEUE_ADD, null);
                mTts.speak("if you want: " + sameNames.get(i), TextToSpeech.QUEUE_ADD, null);
            }

        }
        else {
            sameNames.add(contactName);
            numTypes.add(phoneDir.get(contactName));
            mTts.speak("Answer in yes or no. Did you say: " + contactName, TextToSpeech.QUEUE_ADD, null);
        }


        while(mTts.isSpeaking()){

            // Do nothing as we have to wait for until the user is informed.

        }

        if(!mTts.isSpeaking()) {
            SpeechRecognizer confSR = IntroActivity.getRecognizeConfirmation();

            confSR.setRecognitionListener(new confirmationRecognitionListener(confSR, sameNames, numTypes));
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

            confSR.startListening(intent);

        }
    }


    // copied from the String Util library.
    // This method returns the number of steps required to transform the recognized name to the one of the available contact name.
    public static int getLevenshteinDistance(CharSequence s, CharSequence t, final int threshold) {
        if (s == null || t == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }
        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold must not be negative");
        }


        int n = s.length(); // length of s
        int m = t.length(); // length of t

        // if one string is empty, the edit distance is necessarily the length of the other
        if (n == 0) {
            return m <= threshold ? m : -1;
        } else if (m == 0) {
            return n <= threshold ? n : -1;
        }

        if (n > m) {
            // swap the two strings to consume less memory
            final CharSequence tmp = s;
            s = t;
            t = tmp;
            n = m;
            m = t.length();
        }

        int p[] = new int[n + 1]; // 'previous' cost array, horizontally
        int d[] = new int[n + 1]; // cost array, horizontally
        int _d[]; // placeholder to assist in swapping p and d

        // fill in starting table values
        final int boundary = Math.min(n, threshold) + 1;
        for (int i = 0; i < boundary; i++) {
            p[i] = i;
        }
        // these fills ensure that the value above the rightmost entry of our
        // stripe will be ignored in following loop iterations
        Arrays.fill(p, boundary, p.length, Integer.MAX_VALUE);
        Arrays.fill(d, Integer.MAX_VALUE);

        // iterates through t
        for (int j = 1; j <= m; j++) {
            final char t_j = t.charAt(j - 1); // jth character of t
            d[0] = j;

            // compute stripe indices, constrain to array size
            final int min = Math.max(1, j - threshold);
            final int max = (j > Integer.MAX_VALUE - threshold) ? n : Math.min(n, j + threshold);

            // the stripe may lead off of the table if s and t are of different sizes
            if (min > max) {
                return -1;
            }

            // ignore entry left of leftmost
            if (min > 1) {
                d[min - 1] = Integer.MAX_VALUE;
            }

            // iterates through [min, max] in s
            for (int i = min; i <= max; i++) {
                if (s.charAt(i - 1) == t_j) {
                    // diagonally left and up
                    d[i] = p[i - 1];
                } else {
                    // 1 + minimum of cell to the left, to the top, diagonally left and up
                    d[i] = 1 + Math.min(Math.min(d[i - 1], p[i]), p[i - 1]);
                }
            }

            // copy current distance counts to 'previous row' distance counts
            _d = p;
            p = d;
            d = _d;
        }

        // if p[n] is greater than the threshold, there's no guarantee on it being the correct
        // distance
        if (p[n] <= threshold) {
            return p[n];
        }
        return -1;
    }
}

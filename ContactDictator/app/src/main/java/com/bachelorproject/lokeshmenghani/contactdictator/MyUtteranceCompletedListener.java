package com.bachelorproject.lokeshmenghani.contactdictator;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.widget.Button;

/**
 * Created by lokeshmenghani on 20/05/15.
 */
public class MyUtteranceCompletedListener implements TextToSpeech.OnUtteranceCompletedListener {

    private Button b = IntroActivity.getSearchButton();
    private TextToSpeech mTts = IntroActivity.getmTts();

    @Override
    public void onUtteranceCompleted(final String utteranceId) {

        IntroActivity.getCustomAppContext().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(utteranceId.equals("SAY NAME")) {

                    if (IntroActivity.getRecognizeName().isRecognitionAvailable(IntroActivity.getCustomAppContext())) {
                        IntroActivity.setRecognizeName(SpeechRecognizer.createSpeechRecognizer(IntroActivity.getCustomAppContext()));
                        IntroActivity.setRepeatNumSR(SpeechRecognizer.createSpeechRecognizer(IntroActivity.getCustomAppContext()));
                        IntroActivity.setRecognizeNumberType(SpeechRecognizer.createSpeechRecognizer(IntroActivity.getCustomAppContext()));
                        IntroActivity.setRecognizeConfirmation(SpeechRecognizer.createSpeechRecognizer(IntroActivity.getCustomAppContext()));
                        IntroActivity.setRecognizeCallOrGet(SpeechRecognizer.createSpeechRecognizer(IntroActivity.getCustomAppContext()));


                        IntroActivity.getRecognizeName().setRecognitionListener(new nameRecognitionListener(IntroActivity.getRecognizeName()));


                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

                        IntroActivity.getRecognizeName().startListening(intent);
                    } else {
                        String noRecAvai = "Sorry you don't have a speech recognition service available.";
                        mTts.speak(noRecAvai, TextToSpeech.QUEUE_ADD, null);
                    }
                }

                else if(utteranceId.equals("ENABLE BUTTON")){
                    b.setEnabled(true);
                }
            }
        });
    }
}

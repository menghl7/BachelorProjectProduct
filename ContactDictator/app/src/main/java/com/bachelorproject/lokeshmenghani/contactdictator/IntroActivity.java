package com.bachelorproject.lokeshmenghani.contactdictator;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;
import java.util.Locale;


public class IntroActivity extends Activity implements TextToSpeech.OnInitListener {

    private static SpeechRecognizer recognizeName;
    private static SpeechRecognizer recognizeNumberType;
    private static SpeechRecognizer recognizeConfirmation;
    private static SpeechRecognizer recognizeCallOrGet;
    private static SpeechRecognizer repeatNumSR;
    private static TextToSpeech mTts;
    private static HashMap<String, HashMap<String, String>> phoneDirectory = new HashMap<String, HashMap<String, String>>();
    private static Activity context;
    private static Button searchButton;
    private static int counter = 0;
    private static int counter2 = 0;

    public static int getCounter() {
        return counter;
    }

    public static void setCounter(int c) {
        counter = c;
    }

    public static int getCounter2() {
        return counter2;
    }

    public static void setCounter2(int c) {
        counter2 = c;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        context = this;

        searchButton = (Button) findViewById(R.id.speak_button);

        mTts = new TextToSpeech(this, this);
        importContacts();
    }

    public static Activity getCustomAppContext(){
        return context;
    }

    public static Button getSearchButton(){
        return searchButton;
    }

    public static TextToSpeech getmTts(){
        return mTts;
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS)
        {
            mTts.setLanguage(Locale.US);
            String welcome = "Please press anywhere on the screen when ever you are ready.";
            mTts.speak(welcome, TextToSpeech.QUEUE_FLUSH, null);

        }

    }


    // import all contacts from the phone-book application.
    // store in phoneDirectory the associated number type and the number
    private void importContacts() {

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cur.getCount() > 0)
        {
            while (cur.moveToNext())
            {

                HashMap<String, String> phoneNumbers = new HashMap<String, String>();
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", new String[]{id}, null);

                    while (pCur.moveToNext())
                    {
                        int phone_type = pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        switch (phone_type)
                        {
                            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                String phone_home = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                phoneNumbers.put("home", phone_home);
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                String phone_mob = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                phoneNumbers.put("mobile", phone_mob);
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                String phone_work = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                phoneNumbers.put("work", phone_work);
                                break;
                        }

                    }
                    pCur.close();
                }

                phoneDirectory.put(name, phoneNumbers);
            }
        }
        cur.close();

    }

    public static SpeechRecognizer getRecognizeName(){
        return recognizeName;
    }

    public static void setRecognizeName(SpeechRecognizer rN){
        recognizeName = rN;
    }

    public static SpeechRecognizer getRepeatNumSR(){
        return repeatNumSR;
    }

    public static void setRepeatNumSR(SpeechRecognizer rNum){
        repeatNumSR = rNum;
    }

    public static SpeechRecognizer getRecognizeNumberType(){
        return recognizeNumberType;
    }

    public static void setRecognizeNumberType(SpeechRecognizer recNumT){
        recognizeNumberType = recNumT;
    }

    public static SpeechRecognizer getRecognizeConfirmation(){
        return recognizeConfirmation;
    }

    public static void setRecognizeConfirmation(SpeechRecognizer rC){
        recognizeConfirmation = rC;
    }

    public static SpeechRecognizer getRecognizeCallOrGet(){
        return recognizeCallOrGet;
    }

    public static void setRecognizeCallOrGet(SpeechRecognizer cG){
        recognizeCallOrGet = cG;
    }

    public static HashMap getPhoneDirectory(){
        return phoneDirectory;
    }

    /** Called when the user clicks the Send button */
    public void sendMessage(View view){
        searchButton.setEnabled(false);

        HashMap<String, String> myHashName = new HashMap<String, String>();
        myHashName.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "SAY NAME");
        mTts.setOnUtteranceCompletedListener(new MyUtteranceCompletedListener());
        String instruction = "Say the contact name you wish to search after the following beep.";
        mTts.speak(instruction, TextToSpeech.QUEUE_ADD, myHashName);

    }

    @Override
    public void onBackPressed() {
        if(mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
        stopAllListeningServices();
        super.onBackPressed();
        this.finish();
    }

    public void stopAllListeningServices(){
        if(recognizeName != null){
            recognizeName.stopListening();
            recognizeName.destroy();
        }
        if(recognizeNumberType != null){
            recognizeNumberType.stopListening();
            recognizeNumberType.destroy();
        }
        if(recognizeConfirmation != null){
            recognizeConfirmation.stopListening();
            recognizeConfirmation.destroy();
        }
        if(recognizeCallOrGet != null){
            recognizeCallOrGet.stopListening();
            recognizeCallOrGet.destroy();
        }
        if(repeatNumSR != null){
            repeatNumSR.stopListening();
            repeatNumSR.destroy();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_intro);

    }
}

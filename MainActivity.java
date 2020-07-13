/*package com.example.dipamgoswami.eliza_bot;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}*/

package com.example.dipamgoswami.eliza_bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    ArrayList<String> saved_phrases = new ArrayList<String>();
    ArrayList<List<String>> list_of_list_of_phrases = new ArrayList<List<String>>();
    List<Replacer> replacer_list = new ArrayList<Replacer>();
    HashMap<String, String> phrase_conversion = new HashMap<String, String>();
    private enum Where{ STARTS, CONTAINS, ENDS, NO};

    private class Replacer {
        public String string;
        public Where loc;
        public int index;

        Replacer( Where loc, String string, int index ){
            this.string = string;
            this.loc = loc;
            this.index = index;
        }

        @Override
        public boolean equals( Object o) {
            return string.equals( ((Replacer)o).string);
        }

        @Override
        public int hashCode() {
            return string.hashCode();
        }

        boolean shouldReplace( String s ) {
            switch(loc) {
                case STARTS:
                    return s.startsWith(string);
                case CONTAINS:
                    return s.contains(string);
                case ENDS:
                    return s.endsWith(string);
                default:case NO:
                    return false;
            }
        }

        private String[] seperate( String s ) {
            String[] string = new String[3];
            switch(this.loc) {
                case STARTS:
                    string[0] = "";
                    string[1] = this.string;
                    string[2] = s.substring( this.string.length(), s.length());
                    break;
                case ENDS:
                    string[0] = s.substring(0, s.length()-this.string.length());
                    string[1] = this.string;
                    string[2] = "";
                    break;
                case CONTAINS:
                    string[0] = s.substring( 0, s.indexOf( this.string ));
                    string[1] = this.string;
                    string[2] = s.substring( s.indexOf( this.string)+ this.string.length(), s.length());
                    break;
                case NO:
                    string[0] = new String(s);
                    string[1] = new String(s);
                    string[2] = new String(s);
                    break;
            }
            return string;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializePhrases();
        initializeWordsToReplace();
        setContentView(R.layout.activity_main);
        if( savedInstanceState != null ) {
            saved_phrases.addAll( savedInstanceState.getStringArrayList("past_responses"));
            LinearLayout layout = (LinearLayout)findViewById(R.id.layout2);
            for( String string : savedInstanceState.getStringArrayList("messages")) {
                TextView message = new TextView( getApplicationContext() );
                message.setText(string);
                message.setLayoutParams( new LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT));
                message.setTextColor( Color.BLACK);
                layout.addView(message);
            }
            ((EditText)findViewById(R.id.editText1)).setText( savedInstanceState.getString("input"));
            scrollToBottom();
            Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(myToolbar);
        }
        else
            addMessage("<Eliza> आप कैसे हैं। कृपया मुझे अपनी समस्या बताएं।");
        final Button submit = (Button) findViewById(R.id.button1);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressButton();
            }});
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }*/

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //saving messages
        LinearLayout layout = (LinearLayout)findViewById(R.id.layout2);
        ArrayList<String> messages = new ArrayList<String>();
        for( int i = 0; i <  layout.getChildCount(); i++ ) {
            TextView message = (TextView) layout.getChildAt(i);
            messages.add(message.getText().toString());
        }
        outState.putStringArrayList("messages", messages);
        outState.putStringArrayList("past_responses", saved_phrases);
        outState.putString("input", ((EditText)findViewById(R.id.editText1)).getText().toString());
    }

    private void pressButton() {
        EditText text = (EditText) findViewById(R.id.editText1);
        String talk = text.getEditableText().toString();
        text.setText("");
        //Adding your message
        addMessage("<You> "+talk);
        //Adding Eliza's message
        addMessage("<Eliza> "+generateResponse(talk));
        scrollToBottom();
    }

    private void scrollToBottom() {
        //Scrolling down
        final ScrollView scrollview = (ScrollView)findViewById(R.id.scrollView1);
        scrollview.fullScroll( View.FOCUS_DOWN );
        scrollview.post(new Runnable() {
            public void run() {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void addMessage(String string) {
        LinearLayout layout = (LinearLayout)findViewById(R.id.layout2);
        TextView message = new TextView( getApplicationContext() );
        message.setText( string );
        message.setLayoutParams( new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        message.setTextColor( Color.BLACK);
        layout.addView(message);
    }

    private String generateResponse( String string ) {
        String prepared = prepareString( string );
        String response = searchForKnownResponses(  prepared );
        if(!response.equals(""))
            return response;
        else
            return genericResponse( prepared );
    }

    private String searchForKnownResponses( String string ) {
        for( Replacer phrase : replacer_list ) {
            if( phrase.shouldReplace(string)) {
                String[] x = toPhrase( phrase, string);
                return String.format( getRandomPhraseAt( phrase.index ), x[0], x[1], x[2] );
            }
        }
        return "";
    }

    private String genericResponse( String string) {
        String[] x = toPhrase( new Replacer( Where.NO, "", 35), string);
        return String.format( getRandomPhraseAt( 35 ), x[0], x[1], x[2] );
    }

    private String getRandomPhraseAt( int index ) {
        List<String> phrase = list_of_list_of_phrases.get( index );
        return phrase.get((int)Math.round(Math.random()*(phrase.size()-1)));
    }

    private String prepareString( String string ) {
        String newstring = string.toLowerCase(Locale.ENGLISH);
        if( newstring.matches("\\.$|\\?$|!$") && string.length() >1 )
            newstring = string.substring(0, string.length()-2);
        return newstring;
    }

    private String[] toPhrase( Replacer r, String string  ) {
        String[] seperate_strings = r.seperate(string);
        Log.e("Eliza", ""+seperate_strings[0]+","+seperate_strings[1]+","+seperate_strings[2]);
        for(int i = 0; i < 3; i++) {
            StringBuilder sb = new StringBuilder();
            String[] words = seperate_strings[i].replaceAll("\\p{P}", "").split(" ");
            for( String word : words ) {
                if( phrase_conversion.containsKey(word)) {
                    word = phrase_conversion.get(word);
                }
                sb.append(" ");
                sb.append(word);
            }
            seperate_strings[i] = sb.toString().trim();
        }
        saved_phrases.add( seperate_strings[2].toString() );
        return seperate_strings;
    }
    private void initializePhrases() {
        replacer_list.add(new Replacer(Where.CONTAINS,"मेरी मा", 0));
        replacer_list.add(new Replacer(Where.CONTAINS,"मेरे मा", 0));
        replacer_list.add(new Replacer(Where.CONTAINS,"मेरी मम्मी", 0));
        replacer_list.add(new Replacer(Where.CONTAINS,"मेरे मम्मी", 0));
        replacer_list.add(new Replacer(Where.CONTAINS, "मेरा भाई", 0));
        replacer_list.add(new Replacer(Where.CONTAINS, "मेरे भाई", 0));
        replacer_list.add(new Replacer(Where.CONTAINS,"मेरी बेहेन", 0));
        replacer_list.add(new Replacer(Where.CONTAINS,"मेरे बेहेन", 0));
        replacer_list.add(new Replacer(Where.CONTAINS,"मेरा पापा", 0));
        replacer_list.add(new Replacer(Where.CONTAINS,"मेरे पापा", 0));
        replacer_list.add(new Replacer(Where.CONTAINS,"मेरे पिताजी", 0));
        replacer_list.add(new Replacer(Where.CONTAINS,"मेरा चाचा", 0));
        replacer_list.add(new Replacer(Where.CONTAINS,"मेरे चाचा", 0));
        replacer_list.add(new Replacer(Where.CONTAINS,"मेरी चाची", 0));
        replacer_list.add(new Replacer(Where.CONTAINS,"मेरे चाची", 0));
        replacer_list.add(new Replacer(Where.CONTAINS, "मेरा बेटा", 0));
        replacer_list.add(new Replacer(Where.CONTAINS,"मेरे बेटे", 0));
        replacer_list.add(new Replacer(Where.CONTAINS,"मेरे बेटी", 0));
        replacer_list.add(new Replacer(Where.CONTAINS,"मेरी बेटी", 0));
        replacer_list.add(new Replacer(Where.CONTAINS,"मेरी नानी", 0));
        replacer_list.add(new Replacer(Where.CONTAINS,"मेरे नानी", 0));
        replacer_list.add(new Replacer(Where.CONTAINS,"मेरे नाना", 0));
        replacer_list.add(new Replacer(Where.CONTAINS,"मेरा नाना", 0));
        replacer_list.add(new Replacer(Where.CONTAINS,"मेरे दादा", 0));
        replacer_list.add(new Replacer(Where.CONTAINS, "मेरे दादी", 0));
        list_of_list_of_phrases.add( Arrays.asList( "मुझे अपने परिवार के बारे में और बताओ।",
                "आपके परिवार में और कौन है ?","जब आप अपन परीवार के बारे में सोचते हैं तो आपके दिमाग में और क्या आता है ?"));
        replacer_list.add(new Replacer(Where.CONTAINS, "दुख हुआ मुझे", 1));
        replacer_list.add(new Replacer(Where.CONTAINS, "दुखी हूं", 1));
        replacer_list.add(new Replacer(Where.CONTAINS, "दुख हो रहा है", 1));
        list_of_list_of_phrases.add( Arrays.asList("मुझे यह सुनकर दुख हुई कि आप खुश नहीं हैं।", "क्या आपको लगता है कि मुझसे बात करने से आपको मदद मिलेगी?","मुझे यकीन है कि आपको ये पसंद नहीं है।","क्या आप बता सकते हैं कि आपको दुखी होने के लिए किसने मजबूर किया?"));
        replacer_list.add(new Replacer(Where.CONTAINS, "मुझे पसंद", 2));
        replacer_list.add(new Replacer(Where.CONTAINS, "मुझे बोहत पसंद", 2));
        replacer_list.add(new Replacer(Where.CONTAINS, "अच्छा लगता है", 2));
        list_of_list_of_phrases.add( Arrays.asList( "तो आपको ये पसंद है।", "क्या आपको सही में पसंद है?","अच्छी बात है।") );
        replacer_list.add(new Replacer(Where.CONTAINS, "नफरत है",3));
        replacer_list.add(new Replacer(Where.CONTAINS, "नफरत करता हूं", 3));
        replacer_list.add(new Replacer(Where.CONTAINS, "नफरत करती हूं", 3));
        list_of_list_of_phrases.add( Arrays.asList( "ऐसा क्यों?", "आप इससे नफरत क्यों करते है?","नफरत करने से कुछ समाधान नहीं होने वाला है।"));
        replacer_list.add(new Replacer(Where.CONTAINS, "मुझे चाहिए", 4));
        replacer_list.add(new Replacer(Where.CONTAINS, "चाहिए मुझे", 4));
        list_of_list_of_phrases.add( Arrays.asList("आपको ये क्यों चाहिए?","अगर आपको वो मिलजाए तो आप क्या करोगे?"));
        replacer_list.add(new Replacer(Where.STARTS, "नमस्ते", 5));
        replacer_list.add(new Replacer(Where.STARTS, "सुप्रवत", 5));
        list_of_list_of_phrases.add( Arrays.asList("नमस्ते।", "आप कैसे है?"," कृपया मुझे अपनी समस्या बताएं।"));
        replacer_list.add(new Replacer(Where.CONTAINS, "आप कैसे हो", 6));
        replacer_list.add(new Replacer(Where.CONTAINS, "आप कैसे है", 6));
        list_of_list_of_phrases.add( Arrays.asList("में ठीक हूं। आप कैसे है?"));
        replacer_list.add(new Replacer(Where.CONTAINS, "सुंदर", 7));
        replacer_list.add(new Replacer(Where.CONTAINS, "बढ़िया", 7));
        replacer_list.add(new Replacer(Where.CONTAINS, "अच्छा", 7));
        replacer_list.add(new Replacer(Where.CONTAINS, "लाजवाब", 7));
        replacer_list.add(new Replacer(Where.CONTAINS, "बोहोत बढ़िया", 7));
        list_of_list_of_phrases.add( Arrays.asList("अच्छी बात है।","क्या ये सही में अच्छा है?", "अच्छा लगा सुनकर।"));
        replacer_list.add(new Replacer(Where.CONTAINS, "खराब", 8));
        replacer_list.add(new Replacer(Where.CONTAINS, "भयानक", 8));
        replacer_list.add(new Replacer(Where.CONTAINS,"बोहोत बुरा", 8));
        list_of_list_of_phrases.add( Arrays.asList("इतना बुरा मत सोचिए।", "क्या ये खराब है?","इसमें कुछ गलत लगा क्या?"));
        replacer_list.add(new Replacer(Where.STARTS, "चलो मिलते है फिर कबी।", 9));
        replacer_list.add(new Replacer(Where.STARTS, "अब मैं चलता हूं।", 9));
        replacer_list.add(new Replacer(Where.STARTS, "अब मैं चलती हूं।", 9));
        replacer_list.add(new Replacer(Where.STARTS, "बाद में बात करता हूं।", 9));
        replacer_list.add(new Replacer(Where.STARTS, "बाद में बात करती हूं।", 9));
        replacer_list.add(new Replacer(Where.STARTS, "बाद में बात करते है।", 9));
        replacer_list.add(new Replacer(Where.STARTS, "धन्यवाद।", 9));
        replacer_list.add(new Replacer(Where.STARTS, "अच्छा लगा आपसे बात करके।", 9));
        list_of_list_of_phrases.add( Arrays.asList("नमस्ते। मुझसे बात करने के लिए धन्यवाद।", "धन्यवाद।"," मुझे अच्छी लगी आपसे बात करके।",
                "फिर मिलते है कबी।", "आपना ध्यान रखना।"));
        replacer_list.add(new Replacer(Where.CONTAINS, "कंप्यूटर", 10));
        list_of_list_of_phrases.add( Arrays.asList("क्या कंप्यूटर आपको परेशान करते हैं ?", "आपने कंप्यूटरों की चर्चा क्यों की ?",
                "क्या आपको नहीं लगता कि कंप्यूटर 	लोगों की मदद कर सकता है ?"));
        replacer_list.add(new Replacer(Where.CONTAINS, "नाम", 11));
        list_of_list_of_phrases.add( Arrays.asList("मुझे नामों में कोई दिलचस्पी नहीं है।","कृपया आगे कहें "));
        replacer_list.add(new Replacer(Where.CONTAINS,"मशीन", 12));
        list_of_list_of_phrases.add( Arrays.asList("मशीनों के बारे में आपको क्या चिंता है ?","आप मशीनों के बारे में क्या सोचते हैं ?","आपको क्या लगता है कि मशीनों को आपकी समस्या से क्या लेना-देना है ?"));
        replacer_list.add(new Replacer(Where.CONTAINS, "क्षमा करना", 13));
        replacer_list.add(new Replacer(Where.CONTAINS,"माफी चाहता हूं", 13));
        replacer_list.add(new Replacer(Where.CONTAINS,"माफी चाहती हूं", 13));
        replacer_list.add(new Replacer(Where.CONTAINS,"माफ करना", 13));
        list_of_list_of_phrases.add( Arrays.asList("कृपया क्षमा न मांगिये।","माफी मांगना जरूरी नहीं है।"));
        replacer_list.add(new Replacer(Where.CONTAINS, "मुझे याद है कि", 14));
        list_of_list_of_phrases.add( Arrays.asList("क्या आप अक्सर सोचते हैं कि %3$s?","क्या इसके के बारे में सोचने से दिमाग में कुछ और आता है ?",
                "आप और क्या याद कर पा रहे हैं ?", " आपने इसको को अभी ही क्यों याद किया ? ", "मेरे और %3$s के बीच क्या संबंध है ? "));
        replacer_list.add(new Replacer(Where.CONTAINS, "क्या तुम्हें याद है", 15));
        list_of_list_of_phrases.add( Arrays.asList("क्या आपको लगा कि मै भूल जाऊंगी?","आपको क्यों लगता है कि मुझे  %3$s को अब याद करना चाहिए?", "आपने इस चीज का उल्लेख पहले किया है?"));
        replacer_list.add(new Replacer(Where.CONTAINS, "यदि", 16));
        list_of_list_of_phrases.add( Arrays.asList("क्या आपको लगता है कि इसकी संभावना है  कि %3$s?",
                "क्या आप चाहते हैं कि %3$s?", "वास्तव में, अगर ऐसा हुआ तो?"));
        replacer_list.add(new Replacer(Where.CONTAINS, "मैंने सपना देखा", 17));
        replacer_list.add(new Replacer(Where.CONTAINS, "मैंने सपने में देखा",17));
        replacer_list.add(new Replacer(Where.CONTAINS,"सपने में आया", 17));
        list_of_list_of_phrases.add( Arrays.asList("वास्तव में, %3$s ?","क्या आपने कभी जागते हुए ये सब कल्पना की है ? ",
                "क्या आपने पहले कभी ऐसा सपना देखा है ?", "वह सपना आपको क्या बताता है?", "क्या आप अक्सर सपने देखते हैं?","आपके सपने में कौन से व्यक्ति दिखाई देते हैं?","क्या आप मानते हैं कि सपनों का आपकी समस्याओं से कुछ लेना-देना है ?"));
        replacer_list.add(new Replacer(Where.CONTAINS, "क्युकी", 18));
        replacer_list.add(new Replacer(Where.CONTAINS, "क्यों की", 18));
        list_of_list_of_phrases.add( Arrays.asList(" क्या वही एक असली कारण है ?",
                "क्या कोई और कारण नहीं हैं जो आप सोच पा रहे हैं ?", "क्या वह कारण कुछ समझाने की कोशिश कर रहा है ?",
                "और क्या कारण हो सकते हैं ?"));
        replacer_list.add(new Replacer(Where.CONTAINS, "में खुश हूं", 19));
        replacer_list.add(new Replacer(Where.CONTAINS, "मुझे खुशी हुई", 19));
        replacer_list.add(new Replacer(Where.CONTAINS, "मुझे खुशी हुया", 19));
        list_of_list_of_phrases.add( Arrays.asList("मैंने आपको खुश होने में कैसे मदद की है ?","क्या मुझसे बात करने से आप खुश हैं ?","अच्चिं लगी सुनके।",
                "अभी आप खुश किस कारण से हुए ?","क्या आप बता सकते हैं कि आप अचानक खुश कैसे हो गए ?"));
        replacer_list.add(new Replacer(Where.CONTAINS, "दोस्त", 20));
        replacer_list.add(new Replacer(Where.CONTAINS, "दोस्ती", 20));
        replacer_list.add(new Replacer(Where.CONTAINS, "मित्र", 20));
        list_of_list_of_phrases.add( Arrays.asList("क्या आप अच्छे दोस्त है?","क्या आपको इस दोस्ती से कुछ परेशानी हो रही है?", "आपके दोस्त के बारे में और बताइए?"));
        replacer_list.add(new Replacer(Where.CONTAINS, "एक जैसा", 21));
        replacer_list.add(new Replacer(Where.CONTAINS, "कोई अंतर नहीं", 21));
        list_of_list_of_phrases.add( Arrays.asList("किस तरह से ?", "आप इसमें क्या समानता देखते हैं ?","यह समानता आपको क्या सुझाव देती है ?",
                "आप इसके अतिरिक्त और क्या संबंध देखते हैं ?","आपको क्या लगता है कि इस समानता का मतलब क्या है ?","आप उनके बीच क्या संबंध देखते हैं ?","क्या वास्तव में इससे कोई संबंध हो सकता है ?"));
        replacer_list.add(new Replacer(Where.CONTAINS, "मुझे लगा", 22));
        replacer_list.add(new Replacer(Where.CONTAINS, "मेरेको लगा", 22));
        replacer_list.add(new Replacer(Where.CONTAINS, "मुझे लगता है", 22));
        replacer_list.add(new Replacer(Where.CONTAINS, "मेरेको लगता है", 22));
        replacer_list.add(new Replacer(Where.CONTAINS, "मेरेको तो लगता है", 22));
        replacer_list.add(new Replacer(Where.CONTAINS, "मुझे तो लगता है",22));
        list_of_list_of_phrases.add( Arrays.asList("आपको ऐसा क्यों लगता है?","कोई बिसेस कारण है क्या ऐसा लगने का?", "ऐसा नहीं भी तो हो सकता है।","हो सकता है।"));
        replacer_list.add(new Replacer(Where.CONTAINS, "बिस्वास है", 23));
        list_of_list_of_phrases.add( Arrays.asList("क्या सच में आप ऐसा सोचते हैं?","क्या आपको पूरा यकीन है?"));
        replacer_list.add(new Replacer(Where.CONTAINS, "याद नहीं", 24));
        list_of_list_of_phrases.add( Arrays.asList("क्या आपको बिल्कुल याद नहीं अरहा है?","कृपया और सोचिए।","कुछ और याद आए तो बताना।"));
        replacer_list.add(new Replacer(Where.CONTAINS, "परेशान",25));
        replacer_list.add(new Replacer(Where.CONTAINS, "परेशानी",25));
        replacer_list.add(new Replacer(Where.CONTAINS, "हैरानी",25));
        replacer_list.add(new Replacer(Where.CONTAINS, "चिंतित",25));
        replacer_list.add(new Replacer(Where.CONTAINS, "हैरान",25));
        list_of_list_of_phrases.add( Arrays.asList( "क्या आप अवसर ऐसे परेशान रहते है?","आपकी हैरानी मैं समझ सकती हूं?","कृपया अधिक चिंता ना करे।"));
        replacer_list.add(new Replacer(Where.CONTAINS, "क्या आप", 26));
        replacer_list.add(new Replacer(Where.CONTAINS, "क्या तुम", 26));
        list_of_list_of_phrases.add( Arrays.asList( "आप ये क्यों जानना चाहते हैं?",
                "शायद मैं आपकी कल्पनाओं में हूं।", "चलिए आपके बारे में बात करते।"));
        replacer_list.add(new Replacer(Where.CONTAINS, "आपके", 27));
        replacer_list.add(new Replacer(Where.CONTAINS, "तुम्हारे", 27));
        replacer_list.add(new Replacer(Where.CONTAINS, "आपको", 27));
        list_of_list_of_phrases.add( Arrays.asList( " आप मेरे बारे में क्यों चिंतित हैं?","क्या आपको लगता है कि ये सच है?"));
        replacer_list.add(new Replacer(Where.STARTS, "नहीं", 28));
        replacer_list.add(new Replacer(Where.STARTS, "ना", 28));
        replacer_list.add(new Replacer(Where.STARTS, "कभी नहीं", 28));
        replacer_list.add(new Replacer(Where.STARTS, "ऐसा नहीं है", 28));
        list_of_list_of_phrases.add(Arrays.asList("क्यों नहीं ?", "आप माना क्यों कर रहे हो?", "क्या आप निश्चित हो?"));
        replacer_list.add(new Replacer(Where.STARTS, "हा", 29));
        replacer_list.add(new Replacer(Where.STARTS, "बिल्कुल सही", 29));
        replacer_list.add(new Replacer(Where.STARTS, "सही बात", 29));
        replacer_list.add(new Replacer(Where.STARTS, "सही कहा", 29));
        replacer_list.add(new Replacer(Where.STARTS, "एकदम सही", 29));
        list_of_list_of_phrases.add( Arrays.asList( "आपकी सोच काफी सकारात्मक है।", "सही में?", "तो ये सच है।", "क्या आप निश्चित हो?",
                "मैं समझ रही हूँ।", "मैं समझती हूँ।"));
        replacer_list.add(new Replacer(Where.CONTAINS, "कुछ लोग", 30));
        replacer_list.add(new Replacer(Where.CONTAINS, "कुछ बच्चे", 30));
        list_of_list_of_phrases.add( Arrays.asList("आप किसके बारे में बात कर रहे है?"," क्या आप इन्हे जानते है?","ये लोग को है?"));
        replacer_list.add(new Replacer(Where.CONTAINS, "हर कोई", 31));
        replacer_list.add(new Replacer(Where.CONTAINS, "सबकी", 31));
        replacer_list.add(new Replacer(Where.CONTAINS, "सबको", 31));
        replacer_list.add(new Replacer(Where.CONTAINS, "सब कोई", 31));
        replacer_list.add(new Replacer(Where.CONTAINS, "हर किसी को", 31));
        replacer_list.add(new Replacer(Where.CONTAINS, "सबलोग", 31));
        list_of_list_of_phrases.add( Arrays.asList("क्या आप किसी के बारे में विशेष रूप से सोच सकते हैं?","किसी का उदाहरण दीजिये ?","क्या मैं पूछ सकती हूँ कौन है ये लोग?","आप किसी विशेष व्यक्ति के बारे में सोच रहे हैं ना ?"));
        replacer_list.add(new Replacer(Where.CONTAINS, "हमेशा", 32));
        list_of_list_of_phrases.add( Arrays.asList( "इसके लिए क्या आप कोई उदाहरण देना चाहेंगे ?","आप किस घटना के बारे में सोच रहे हैं ?",
                "कब ?", "वास्तव में, हमेशा ही  ?"));
        replacer_list.add(new Replacer(Where.STARTS, "क्या", 33));
        replacer_list.add(new Replacer(Where.STARTS, "क्यों", 33));
        replacer_list.add(new Replacer(Where.STARTS, "कौन", 33));
        replacer_list.add(new Replacer(Where.STARTS, "कब", 33));
        replacer_list.add(new Replacer(Where.STARTS, "कहा", 33));
        list_of_list_of_phrases.add( Arrays.asList( " आपने ये क्यों पूछा ?", "क्या इस सवाल में आपकी कोई विशेष रुचि है ?",
                " यह आप वास्तव में क्या जानना चाहते हैं ?","क्या इस तरह के प्रश्न आपके दिमाग में ज्यादा हैं ?", "आप क्या सोचते हैं ?", "क्या आपने यह किसी और से पूछा है?"));
        replacer_list.add(new Replacer(Where.CONTAINS, "सायद", 34));
        replacer_list.add(new Replacer(Where.CONTAINS, "हो सकता है", 34));
        list_of_list_of_phrases.add( Arrays.asList("आप कुछ निश्चित नहीं लग रहे।"," इतनी अनिश्चितता क्यों ?",
                "क्या आपको यकीन नहीं है ?", "लगता है आपको इस बात पे यकीन नहीं है।"));
        list_of_list_of_phrases.add( Arrays.asList("मुझे पता नहीं कि मैं आपको समझ पा रही हूँ या नहीं।","कृपया। और बताइये। ","यह आपको क्या बताता है ?","क्या आप सच में इसके बारे में और बात करना चाहते हैं ?","और बताइये।","कृपया आगे कहें ।"));
    }

    private void initializeWordsToReplace() {
        phrase_conversion.put("आपके "," मेरे ");
        phrase_conversion.put("आप "," मैं ");
        phrase_conversion.put("आपने "," मैंने ");
        phrase_conversion.put("आपसे "," मुझसे ");
        phrase_conversion.put("तुम ", " मैं ");
        phrase_conversion.put("तुमने "," मैंने ");
        phrase_conversion.put("तुमसे "," मुझसे ");
        phrase_conversion.put("तुम्हारे "," मेरे ");
        phrase_conversion.put("हमने "," तुमने ");
        phrase_conversion.put("हमको "," तुमको ");
        phrase_conversion.put("हमलोग "," तुमलोग ");
        phrase_conversion.put("हमारा "," तुम्हारा ");
        phrase_conversion.put("मेरा "," तुम्हारा ");
        phrase_conversion.put("मेरी "," तुम्हारी ");
        phrase_conversion.put("मेरे "," तुम्हारे ");
        phrase_conversion.put("मुझसे "," तुमसे ");
        phrase_conversion.put("मुझे "," तुम्हे ");
        phrase_conversion.put("मुझको "," तुमको ");
    }
}

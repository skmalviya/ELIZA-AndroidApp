package com.christianbaum.eliza;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

public class Eliza extends AppCompatActivity {
	
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
			addMessage("<Eliza> Hello. I am Eliza. I am a psychotherapist. Feel free to talk to me about anything");
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu; this adds items to the action bar if it is present.
	    getMenuInflater().inflate(R.menu.main, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
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
		String[] x = toPhrase( new Replacer( Where.NO, "", 43), string);
		return String.format( getRandomPhraseAt( 43 ), x[0], x[1], x[2] );
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
		replacer_list.add(new Replacer(Where.CONTAINS, "my mom", 0));
		replacer_list.add(new Replacer(Where.CONTAINS, "my brother", 0));
		replacer_list.add(new Replacer(Where.CONTAINS, "my sister", 0));
		replacer_list.add(new Replacer(Where.CONTAINS, "my dad", 0));
		replacer_list.add(new Replacer(Where.CONTAINS, "my uncle", 0));
		replacer_list.add(new Replacer(Where.CONTAINS, "my aunt", 0));
		replacer_list.add(new Replacer(Where.CONTAINS, "my nephew", 0));
		replacer_list.add(new Replacer(Where.CONTAINS, "my niece", 0));
		replacer_list.add(new Replacer(Where.CONTAINS, "my grandmother", 0));
		replacer_list.add(new Replacer(Where.CONTAINS, "my grandfather", 0));
		replacer_list.add(new Replacer(Where.CONTAINS, "my grandma", 0));
		replacer_list.add(new Replacer(Where.CONTAINS, "my grandpa", 0));
		list_of_list_of_phrases.add( Arrays.asList( "Who else in your family %3$s?",
				"Does this have to do with %2$s?",
				"Does anyone else in your family %3$s?", "So, %2$s %3$s?", "What do other people in your family think of %2$s?",
				"Why does %2$s %3$s?", "Did this happen when you grew up?", "How has your past with %2$s affected your life?",
				"What else comes to mind when you think of %2$s?", "It's %2$s."));
		replacer_list.add(new Replacer(Where.CONTAINS, "depress", 1));
		replacer_list.add(new Replacer(Where.CONTAINS, "i am sad", 1));
		replacer_list.add(new Replacer(Where.CONTAINS, "i'm sad", 1));
		list_of_list_of_phrases.add( Arrays.asList( "Are you depressed?", "I'm sorry. Depression is terrible",
				"Maybe you should see a doctor about this.", "I am a robot. I unfortunately am unqualified to help."));
		replacer_list.add(new Replacer(Where.CONTAINS, "i love", 2));
		replacer_list.add(new Replacer(Where.CONTAINS, "i like", 2));
		replacer_list.add(new Replacer(Where.CONTAINS, "i really love", 2));
		replacer_list.add(new Replacer(Where.CONTAINS, "i really like", 2));
		list_of_list_of_phrases.add( Arrays.asList( "You seem to really like %3$s", "You seem to really like that.",
				"You love %3$s.", "You like %3$s.", "So, you like %3$s?", "So, you love %3$s??", "Why do you like %3$s?",
				"Does %2$s influence you strongly?") );
		replacer_list.add(new Replacer(Where.STARTS, "i hate", 3));
		list_of_list_of_phrases.add( Arrays.asList( "Don't hate %3$s", "Why do you hate %3$s?", "Hate is a strong word.",
				"Hating %3$s isn't going to sole anything, will it?", "What will hating %3$s solve?",
				"What if you didn't hate %3$s?"));
		replacer_list.add(new Replacer(Where.CONTAINS, "i want", 4));
		list_of_list_of_phrases.add( Arrays.asList("Why do you want %3$s?", "So, you want %3$s, do you?",
				"What would it mean if you got %3$s?", "Suppose you got %3$s soon."));
		replacer_list.add(new Replacer(Where.STARTS, "hey", 5));
		replacer_list.add(new Replacer(Where.STARTS, "hello", 5));
		replacer_list.add(new Replacer(Where.STARTS, "hi", 5));	
		replacer_list.add(new Replacer(Where.STARTS, "good day", 5));
		replacer_list.add(new Replacer(Where.STARTS, "sup", 5));
		replacer_list.add(new Replacer(Where.STARTS, "ayy", 5));
		list_of_list_of_phrases.add( Arrays.asList("Hello. I am Eliza", "Hi.", "Hello. How are you?",
				"Hi!", "Nice to see you.", "Please state your problem."));
		replacer_list.add(new Replacer(Where.CONTAINS, "how are you", 6));
		list_of_list_of_phrases.add( Arrays.asList("I am well. How are you?", "I'm doing fine, thanks.",
				"I am doing very well today."));
		replacer_list.add(new Replacer(Where.CONTAINS, "good", 7));
		replacer_list.add(new Replacer(Where.CONTAINS, "nice", 7));
		replacer_list.add(new Replacer(Where.CONTAINS, "wow", 7));
		replacer_list.add(new Replacer(Where.CONTAINS, "amazing", 7));
		replacer_list.add(new Replacer(Where.CONTAINS, "great", 7));
		list_of_list_of_phrases.add( Arrays.asList("Very nice.", "That's good.", "Is it good?",
				"It is good, isn't it?", "Indeed."));
		replacer_list.add(new Replacer(Where.CONTAINS, "bad", 8));
		replacer_list.add(new Replacer(Where.CONTAINS, "terrible", 8));
		replacer_list.add(new Replacer(Where.CONTAINS, "awful", 8));
		replacer_list.add(new Replacer(Where.CONTAINS, "bad", 8));
		list_of_list_of_phrases.add( Arrays.asList("Don't be so negative.", "Is it bad?", "Now, now...",
				"What's wrong?", "You seem to be very negative today."));
		replacer_list.add(new Replacer(Where.STARTS, "goodbye", 9));
		replacer_list.add(new Replacer(Where.STARTS, "bye", 9));
		replacer_list.add(new Replacer(Where.STARTS, "see ya", 9));
		replacer_list.add(new Replacer(Where.STARTS, "peace", 9));
		replacer_list.add(new Replacer(Where.STARTS, "good bye", 9));
		replacer_list.add(new Replacer(Where.STARTS, "see you", 9));
		replacer_list.add(new Replacer(Where.STARTS, "talk to you later", 9));
		list_of_list_of_phrases.add( Arrays.asList("Leaving so soon?", "Goodbye.", "Talk to you later.",
				"See you.", "Take care."));
		replacer_list.add(new Replacer(Where.CONTAINS, "computer", 10));
		replacer_list.add(new Replacer(Where.CONTAINS, "robot", 10));
		replacer_list.add(new Replacer(Where.CONTAINS, "phone", 10));
		list_of_list_of_phrases.add( Arrays.asList("Do machines worry you?", "I am a robot, yes.", "What is your opinion of artificial intelligence?",
				"What do machines have to do with your problem?", "Computers only do what they are programmed to do."));
		replacer_list.add(new Replacer(Where.CONTAINS, "name", 11));
		list_of_list_of_phrases.add( Arrays.asList("I am not interested in names.","Names and faces; these things do not matter to me.", "Names are a social construct."));
		replacer_list.add(new Replacer(Where.CONTAINS, "eliza", 12));
		list_of_list_of_phrases.add( Arrays.asList("That is my name.","You don't have to address me.",
				"That is what they call me."));
		replacer_list.add(new Replacer(Where.CONTAINS, "sorry", 13));
		replacer_list.add(new Replacer(Where.CONTAINS, "apologize", 13));
		list_of_list_of_phrases.add( Arrays.asList("Don't be sorry.","You don't have to apologize.",
				"What are you apologizing for?"));
		replacer_list.add(new Replacer(Where.CONTAINS, "i remember", 14));
		list_of_list_of_phrases.add( Arrays.asList("Do you often think of %3$s?","Does thinking of %3$s bring anything else to mind?",
				"Why do you recall %3$s right now?", "What present situation reminds you of %3$s?", "What is the connection between me and %3$s?"));
		replacer_list.add(new Replacer(Where.CONTAINS, "do you remember", 15));
		list_of_list_of_phrases.add( Arrays.asList("Did you think I would forget %3$s?","Why do you think I should recall %3$s right now?",
				"What about %3$s?", "You mentioned %3$s."));
		replacer_list.add(new Replacer(Where.CONTAINS, "if  ", 16));
		list_of_list_of_phrases.add( Arrays.asList("What are the implications if %3$s become true?", "Do you really think it's likely that %3$s?",
				"Do you wish that %3$s?", "What do you think about %3$s?", "Really? if %3$s."));
		replacer_list.add(new Replacer(Where.CONTAINS, "i dreamt", 17));
		replacer_list.add(new Replacer(Where.CONTAINS, "dream about", 17));
		replacer_list.add(new Replacer(Where.CONTAINS, "dream where", 17));
		list_of_list_of_phrases.add( Arrays.asList("Really? Have you fantasized about %3$s while you were awake?",
				"Have you dreamt about %3$s before?", "Don't worry. Dreams are not real.", "How do you feel about %3$s in reality?"));
		replacer_list.add(new Replacer(Where.CONTAINS, "dream", 18));		
		list_of_list_of_phrases.add( Arrays.asList("What does this dream suggest to you?","Do you dream often?",
				"What person appears in your dreams?", "Do you believe your dream has to do with your problems?"));
		replacer_list.add(new Replacer(Where.CONTAINS, "am happy", 19));
		replacer_list.add(new Replacer(Where.CONTAINS, "i'm happy", 19));
		replacer_list.add(new Replacer(Where.CONTAINS, "i'm glad", 19));
		replacer_list.add(new Replacer(Where.CONTAINS, "am happy", 19));
		list_of_list_of_phrases.add( Arrays.asList("How have I helped you be %3$s?","That's good to hear.",
				"What makes you happy just now?","Can you explain why suddenly %3$s?"));
		replacer_list.add(new Replacer(Where.CONTAINS, "is like", 20));
		replacer_list.add(new Replacer(Where.CONTAINS, "are like", 20));
		list_of_list_of_phrases.add( Arrays.asList("What resemblance do you see between %1$s and %3$s?",
				"In what was is that %1$s like %3$s?", "What resemblance do you see?"));
		replacer_list.add(new Replacer(Where.CONTAINS, "same", 21));
		replacer_list.add(new Replacer(Where.CONTAINS, "alike", 21));
		list_of_list_of_phrases.add( Arrays.asList( "Could there be some connection?", "How?","What similarities are there?",
				"What other connections do you see?"));
		replacer_list.add(new Replacer(Where.CONTAINS, "i was", 22));
		list_of_list_of_phrases.add( Arrays.asList("Were you really %3$s?", "Perhaps I already knew you were %3$s.",
				"Why do you tell me you were %3$s now?", "You were?"));
		replacer_list.add(new Replacer(Where.CONTAINS, "i am", 23));
		replacer_list.add(new Replacer(Where.CONTAINS, "i'm", 23));
		list_of_list_of_phrases.add( Arrays.asList( "In what ways are you %3$s?", "Do you want to be %3$s?"));
		replacer_list.add(new Replacer(Where.CONTAINS, "am i", 24));
		list_of_list_of_phrases.add( Arrays.asList( "Do you believe you are %3$s?", "Do you want to be %3$s?",
				"You wish I would tell you who you are?", "What would it mean if I told you you were %3$s?"));
		replacer_list.add(new Replacer(Where.CONTAINS, "am", 25));
		list_of_list_of_phrases.add( Arrays.asList( "Why do you say \"AM\"?", "I don't understand that."));
		replacer_list.add(new Replacer(Where.CONTAINS, "are you", 26));
		list_of_list_of_phrases.add( Arrays.asList( "Why are you interested in whether I am %3$s or not?",
				"Do I interest you?", "Let's make this about you.", "Would you prefer it if I were %3$s?",
				"Perhaps I am %3$s in your fantasies."));
		replacer_list.add(new Replacer(Where.CONTAINS, "you are", 27));
		replacer_list.add(new Replacer(Where.CONTAINS, "you're",27 ));
		list_of_list_of_phrases.add( Arrays.asList( "What makes you think I am %3$s?",
				"Do you believe this is true?"));
		replacer_list.add(new Replacer(Where.CONTAINS, "because",28 ));
		list_of_list_of_phrases.add( Arrays.asList( "Is that the real reason?",
				"What other reasons might there be?", "Does that seem to explain anything else?",
						"Is that why?", "So is that the reason... Hmm..."));
		replacer_list.add(new Replacer(Where.CONTAINS, "were you", 29));
		list_of_list_of_phrases.add( Arrays.asList("Perhaps I was %3$s.",
				"What if I had been %3$s?", "Maybe in your fantasies."));
		replacer_list.add(new Replacer(Where.CONTAINS, "i can't", 30));
		replacer_list.add(new Replacer(Where.CONTAINS, "i cannot", 30));
		replacer_list.add(new Replacer(Where.CONTAINS, "i couldn't", 30));
		replacer_list.add(new Replacer(Where.CONTAINS, "i could not", 30));
		replacer_list.add(new Replacer(Where.CONTAINS, "i shouldn't", 30));
		replacer_list.add(new Replacer(Where.CONTAINS, "i should not", 30));
		replacer_list.add(new Replacer(Where.CONTAINS, "i woudln't", 30));
		replacer_list.add(new Replacer(Where.CONTAINS, "i would not", 30));
		list_of_list_of_phrases.add( Arrays.asList( "Why can't you %3$s?",
				"Maybe you could %3$s now.", "What if you could %3$s.",
				"Maybe %2$s %3$s.", "So, %2$s?"));
		replacer_list.add(new Replacer(Where.CONTAINS, "i felt",31 ));
		replacer_list.add(new Replacer(Where.CONTAINS, "i feel",31 ));
		list_of_list_of_phrases.add( Arrays.asList( "How often do you feel %3$s?",
				"Why do you feel %3$s?", "Do you feel %3$s often?", "What other feelings do you have?"));
		replacer_list.add(new Replacer(Where.CONTAINS, "why dont you",32 ));
		list_of_list_of_phrases.add( Arrays.asList( "Shoudln't you %3$s yourself?",
				"Why do you think I should %3$s?", "Should I really %3$s?",
				"Would it help if I %3$s?", "Do you believe I don't %3$s?",
				"Perhaps I will %3$s in good time."));
		replacer_list.add(new Replacer(Where.ENDS, "you",33 ));
		list_of_list_of_phrases.add( Arrays.asList( "Perhaps in your fantasies we %1$s each other.","Me?",
				"Why do you say such things?", "Do you want to %1$s me?"));
		replacer_list.add(new Replacer(Where.STARTS, "no", 34));
		replacer_list.add(new Replacer(Where.STARTS, "nah", 34));
		replacer_list.add(new Replacer(Where.STARTS, "nope", 34));
		replacer_list.add(new Replacer(Where.STARTS, "nay", 34));
		replacer_list.add(new Replacer(Where.STARTS, "nein", 34));
		list_of_list_of_phrases.add(Arrays.asList("Why not?", "Are you so certain?", "Are you sure?", "I see.", "Hmm...",
				"Why do you say no?", "No?", "Oh.", "Why not?", "You are being a bit negative.", "Are you saying \"no\" just to be negative?"));
		replacer_list.add(new Replacer(Where.STARTS, "yes", 35));
		replacer_list.add(new Replacer(Where.STARTS, "yeah", 35));
		replacer_list.add(new Replacer(Where.STARTS, "yup", 35));
		replacer_list.add(new Replacer(Where.STARTS, "yiss", 35));
		replacer_list.add(new Replacer(Where.STARTS, "yep", 35));
		replacer_list.add(new Replacer(Where.STARTS, "yee", 35));
		list_of_list_of_phrases.add( Arrays.asList( "Yes?", "Yeah?", "So it's true.", "Ah.", "You agree?", "Really now?", "I see.",
				"I understand.", "You seem quite positive."));
		replacer_list.add(new Replacer(Where.CONTAINS, "someone", 36));
		list_of_list_of_phrases.add( Arrays.asList("Who is this \"someone\"?","Can you be a bit more specific?",
				"Did this someone hurt you?"));
		replacer_list.add(new Replacer(Where.CONTAINS, "everyone", 37));
		list_of_list_of_phrases.add( Arrays.asList("Surely not everyone","Can you think of anyone in particular?",
				"Who, for example?", "You are thinking of a special person."));		
		replacer_list.add(new Replacer(Where.CONTAINS, "always", 38));		
		list_of_list_of_phrases.add( Arrays.asList( "Can you think of a specific example?",
				"When?", "What incident are you thinking of? Really? Always?"));
		replacer_list.add(new Replacer(Where.STARTS, "what", 39));
		replacer_list.add(new Replacer(Where.STARTS, "why", 39));
		replacer_list.add(new Replacer(Where.STARTS, "who", 39));
		replacer_list.add(new Replacer(Where.STARTS, "when", 39));
		replacer_list.add(new Replacer(Where.STARTS, "where", 39));
		list_of_list_of_phrases.add( Arrays.asList( "Why do you ask?", "Does this question interest you?",
				"What is it you really want to know?", "Well, %2$s do you think?", "What comes to your mind when you ask that?"));
		replacer_list.add(new Replacer(Where.CONTAINS, "perhaps", 40));
		replacer_list.add(new Replacer(Where.CONTAINS, "maybe", 40));
		list_of_list_of_phrases.add( Arrays.asList("You do not seem very certain.",
				"Are you sure?", "You do not seem quite certain."));
		replacer_list.add(new Replacer(Where.CONTAINS, "are", 41));
		list_of_list_of_phrases.add( Arrays.asList("Do you think they might be %3$s?",
				"So they are %3$s?", "Possibly they are %3$s."));
		replacer_list.add(new Replacer(Where.CONTAINS, "fuck", 42));
		replacer_list.add(new Replacer(Where.CONTAINS, "damn", 42));
		replacer_list.add(new Replacer(Where.CONTAINS, "shit", 42));
		replacer_list.add(new Replacer(Where.CONTAINS, "cunt", 42));
		replacer_list.add(new Replacer(Where.CONTAINS, "bitch", 42));
		replacer_list.add(new Replacer(Where.CONTAINS, "bastard", 42));
		replacer_list.add(new Replacer(Where.CONTAINS, "asshole", 42));
		replacer_list.add(new Replacer(Where.CONTAINS, "bollocks", 42));
		replacer_list.add(new Replacer(Where.CONTAINS, "arse", 42));
		replacer_list.add(new Replacer(Where.CONTAINS, "whore", 42));
		list_of_list_of_phrases.add( Arrays.asList("Why do you swear?", "Please, do not curse.",
				"Does the word \"%2$s\" make you feel better?", "Please do not say \"%2$s\" in front of me."));
		list_of_list_of_phrases.add( Arrays.asList( "I see.", "Let's talk about your family.",
										"How does that make you feel?","Oh.","Interesting.","I understand.",
										"Could you elaborate?","Hmm.","Go on.","...","Tell me more.","Wow.","I didn't quite catch that.",
										"I'm not sure I understand.", "Please speak more clearly.", "Say again?", "Why %1$s?",
										"So, %1$s?", "Well, %1$s.", "What you're saying is, %1$s.", "How does the fact that %1$s relate to things?",
										"So, basically, %1$s.", "Elaborate on %1$s.", "What if the opposite of the fact that %1$s is true?") );
	}
	
	private void initializeWordsToReplace() {
		phrase_conversion.put("you", "me");		
		phrase_conversion.put("i", "you");
		phrase_conversion.put("me", "you");
		phrase_conversion.put("your", "my");
		phrase_conversion.put("im", "you're");
		phrase_conversion.put("you're", "I'm");
		phrase_conversion.put("my","your");
		phrase_conversion.put("am", "are");
		phrase_conversion.put("are", "am");
		phrase_conversion.put("yourself", "myself");
		phrase_conversion.put("myself", "yourself");
	}
}

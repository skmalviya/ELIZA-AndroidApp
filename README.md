# ELIZA-AndroidApp
ELIZA is an early natural language processing computer program created from 1964 to 1966 at the MIT Artificial Intelligence Laboratory by Joseph Weizenbaum. It was Created to demonstrate the superficiality of communication between humans and machines. Eliza simulated conversation by using a pattern matching and substitution methodology to produce surprisingly life-like responses. It is a simulation of a psychotherapist in an initial psychiatric interview.

Eliza is a rule-based chatbot where input sentences are analyzed on the basis of decomposition rules which are triggered by key words appearing in the input text. Responses are generated by reassembly rules associated with selected decomposition rules.

In this project, We developed a complete speech enabled  chatbot where the user will interact with the chatbot in Hindi language. The chatbot development can be classified into 3 categories: Android Application Development, Generating Eliza Responses and Speech Interaction with user.

## Android Application Development -

I used the software – Android Studio for developing the Android Application for Hindi Eliza.
The Android App Api can be downloaded from the link: Hindi_Eliza. The App has to be installed and used in an Android Smartphone.

The user needs to change the keyboard to Hindi Language which translates english to hindi while typing. If the option is not available, the user has to download the Hindi Keyboard from settings. 

On opening the application, the user is greeted by Eliza with the following message: आप कैसे हैं। कृपया मुझे अपनी समस्या बताएं।

The User needs to talk with the Chatbot either by typing in the message box or by speaking using 
the speech icon to the right with the keyboard set to Hindi language.

The User Interface of the App can be seen from the picture.

![image](https://user-images.githubusercontent.com/40790714/88953498-a7d73080-d2b6-11ea-8dc5-5b924621b1f2.png)

## Generating Eliza Responses -

The input given by the user is first prepared by removing the punctuations (if any) and then the phrases from the text are searched in the list of keywords/phrases serially from index 0 to higher indices. The keywords are stored in a List according to the importance of the words/phrases and the various possible responses to the phrases are stored in another list with the same index value.

For example:
replacer_list.add(new Replacer(Where.STARTS, "नमस्ते", 4));

replacer_list.add(new Replacer(Where.STARTS, "सुप्रवत", 4));

list_of_list_of_phrases.add( Arrays.asList("नमस्ते।", "आप कैसे है?"," कृपया मुझे अपनी समस्या बताएं।"));

If the user starts the statement with "नमस्ते" or "सुप्रवत", then the Eliza responds with one randomly selected statement from list_of_list_of phrases.
The randomness in selection of responses ensures that the responses are not redundant when the keywords are repeated.

Another example:
replacer_list.add(new Replacer(Where.CONTAINS, "कंप्यूटर", 10));

list_of_list_of_phrases.add( Arrays.asList("क्या कंप्यूटर आपको परेशान करते हैं ?", "आपने कंप्यूटरों की चर्चा क्यों की ?","क्या आपको नहीं लगता कि कंप्यूटर लोगों की मदद कर सकता है ?"))

If the user mentions "कंप्यूटर", one of the available responses is randomly generated.

replacer_list.add(new Replacer(Where.CONTAINS, "यदि", 16));

replacer_list.add(new Replacer(Where.CONTAINS, "अगर", 16));

list_of_list_of_phrases.add( Arrays.asList("क्या आपको लगता है कि इसकी संभावना है।","आप ऐसा क्यों सोच रहे है?", "क्या आप चाहते हैं कि ऐसा हो?", "वास्तव में, अगर ऐसा हुआ तो?"))

The above example illustrates the possible response that will be generated when the user’s statement contains "यदि" or "अगर".

In case, the user’s statement doesn’t match with the keywords/phrases, then the following generic responses will be generated.
"मुझे पता नहीं कि मैं आपको समझ पा रही हूँ या नहीं।"

"कृपया। और बताइये। "

"यह आपको क्या बताता है ?"

"क्या आप सच में इसके बारे में और बात करना चाहते हैं ?"

"और बताइये।"

"कृपया आगे कहें।"

There is scope for improvement in the Hindi Script which matches the keywords/phrases and can enhance the performance of the Chatbot.

The following conversations with Eliza will give a better idea about its working.

![image](https://user-images.githubusercontent.com/40790714/88953779-0ac8c780-d2b7-11ea-8f38-f34e52b2f324.png)

![image](https://user-images.githubusercontent.com/40790714/88953809-13210280-d2b7-11ea-98ac-a216d407d79d.png)

## Speech Interaction with User -

The User’s Speech is converted to Hindi Text using the Hindi Keyboard and Android’s Automatic Speech Recognition Feature.

The responses generated are converted to Hindi Speech using the TextToSpeech Module available for Hindi Language. Every generated response is converted to speech using this module. 

This speech conversion feature adds more comfort in using the Chatbot via conversation instead of typing the messages. 

## References -

[1] Joseph Weizenbaum “ELIZA – A Computer Program for
the Study of Natural Language Communication between
Man and Machine” : Communications of the ACM, January
1966, Vol. 9, No. 1.

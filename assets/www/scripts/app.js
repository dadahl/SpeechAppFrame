/*Copyright 2013 Conversational Technologies
 * MIT License
 */

/* Initialization Section*/

function startSession(){
    document.addEventListener("deviceready", onDeviceReady, false);
}
	
function onDeviceReady() {
    //start Google engine
    initDictation();
}

function fail(error) {
    console.log(error.code);    
}

//receive result from Google recognition and do further processing
function finishedRecording(response,currentDictationTokens) {
        //do whatever additional processing is required for the application
	    // for example, repeat the recognized speech and put it in the "Said" line, as here
	    speak(currentDictationTokens);
	    spokenText.innerHTML = currentDictationTokens;
    }

/*TTS output*/

function speak( say ){
        speakAndroidTTS(say);
}
	


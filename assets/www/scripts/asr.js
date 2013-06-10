/*Copyright 2013 Conversational Technologies
 * MIT License
 */

//for speech recognition

var currentFileSystem;
var dictation = new SpeechRecognizerPlugin();
var filename = "";
var listening = false;
var currentDictationTokens = "";
var currentDictationResult ="";

function initDictation(){
        dictation.init(initDictationSuccess,initDictationFail);
}

function initDictationSuccess(){
	 statusLine.innerHTML = "Ready";
}

function initDictationFail(){
    speechFail();
}

//recording
function toggleRecording(){
    if(listening == false){
        stopPlaying(); //stop any ongoing TTS
        listening = true;
        statusLine.innerHTML = "Working";
        toggleListening.className ="speechControlButton redSpeech";
            recognizeDictation();  
    }
    else{
        toggleListening.className ="speechControlButton greenSpeech";
        statusLine.innerHTML = "Ready";
        listening = false;
    }
}

function recognizeDictation(){ //use Google's dictation engine
    var requestCode = 1234;
    var maxMatches = 1;
    var promptString = "";  // optional
    var language = "en-US";                     // optional, you could try different languages here
    console.log("starting dictation from recognizeDictation to "+ filename);
    dictation.startRecognize(speechOk, speechFail, requestCode, maxMatches,promptString,language,filename);
}

// the response comes back from the Google recognizer
function speechOk(response){
    toggleRecording(); //turn button to green "ready" state
    if (response) {
        console.log("got speech result");
        var stringResponse = response.toString();
        currentDictationResult = response;
        var parser = new DOMParser();
        var xmlDoc=parser.parseFromString(stringResponse,"text/xml");
        var documentElement = xmlDoc.documentElement;
        var interpretations = documentElement.childNodes;
        var number = interpretations.length;
        currentDictationTokens = "";
        //for start timestamp from EMMA for logging
        var start = 0;
        combined=stringResponse;
        for (var i=0;i<number;i++){
            if(interpretations.item(i).tagName == "emma:one-of"){
                currentDictationTokens = interpretations.item(i).firstChild.getAttribute("emma:tokens");
            }
        }
        asrResult.innerHTML = currentDictationTokens; //just the string result
            finishedRecording(response,currentDictationTokens); 
    }        
}

function speechFail(error){
    toggleListening.className ="speechControlButton greenSpeech";
    statusLine.innerHTML = "Couldn't process your request, try again.";
    asrResult.innerHTML = "";
    listening = false;
}




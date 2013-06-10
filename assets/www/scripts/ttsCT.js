/*Copyright 2013 Conversational Technologies
 * MIT License
 */

var textToSay = "hi there";
var ttsReady = false;
var myMedia;
var playing = true;
var androidStarted = false;



//for using Android TTS

function speakAndroidTTS(text){
    textToSay = text;
    androidStarted = true;
    startupAndroid();
}

function closeAndroid(){
    if(getVoice == "BasicAndroidVoice"){
        shutdownAndroid();
    }
}

function startupAndroid(){
    window.plugins.tts.startup(startupWin, fail);
}

function shutdownAndroid(){
    window.plugins.tts.shutdown(shutdownWin, shutdownFail);
}

function shutdownWin(result) {
}

function shutdownFail(result) {
    console.log("Error = " + result);
}

function startupWin(result) {
    if (result == TTS.STARTED) {
        ttsReady = true;
    }
    if(ttsReady){
        window.plugins.tts.speak(textToSay);
    }
    else{
        console.log("tts not ready");
    }
}

function stopWin(result) {
}

function fail(result) {
    console.log("Error = " + result);
}

function stopPlaying(){
         if (androidStarted){
            window.plugins.tts.stop(stopWin, fail);
        }
}



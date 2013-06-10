/** *  SpeechRecognizer.js 
 * *  Speech Recognizer PhoneGap plugin (Android) **/
function SpeechRecognizerPlugin() {}
/** * Initialize *  * @param successCallback * @param errorCallback */
SpeechRecognizerPlugin.prototype.init = function(successCallback, errorCallback) {
    console.log("calling init");
    return cordova.exec(successCallback, errorCallback, "SpeechRecognizerPlugin", "init",[]);
};
/** * Recognize speech and return a list of matches *  
 ** @param successCallback * @param errorCallback 
 ** @param reqCode User-defined integer request code which will be returned when recognition is complete * 
 *@param maxMatches The maximum number of matches to return. 0 means the service decides how many to return. 
 ** @param promptString An optional string to prompt the user during recognition 
 ** @param language is an optional string to pass a language name in IETF BCP 47. If nothing is specified, the currrent phone language is used */

SpeechRecognizerPlugin.prototype.stop = function(successCallback, errorCallback) {
    return cordova.exec(successCallback, errorCallback, "SpeechRecognizerPlugin", "stop",[]);
};
SpeechRecognizerPlugin.prototype.startRecognize = function(successCallback, errorCallback, reqCode, maxMatches, promptString, language,filename) {
    console.log("starting dictation recognition");
    return cordova.exec(successCallback, errorCallback, "SpeechRecognizerPlugin", "startRecognize", [reqCode, maxMatches, promptString, language,filename]);
};
    
/** * Get the list of the supported languages in IETF BCP 47 format *  * @param successCallback * @param errorCallback * * Returns an array of codes in the success callback */
SpeechRecognizerPlugin.prototype.getSupportedLanguages = function(successCallback, errorCallback) {
    return cordova.exec(successCallback, errorCallback, "SpeechRecognizerPlugin", "getSupportedLanguages", []);
};/** * Load  */
cordova.addConstructor(function() {
    cordova.addPlugin("SpeechRecognizerPlugin", new SpeechRecognizerPlugin());
});
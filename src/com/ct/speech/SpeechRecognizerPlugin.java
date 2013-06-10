/**
 *  SpeechRecognizerPlugin.java
 *  Speech Recognition PhoneGap plugin (Android)
 *
 *  @author Colin Turner
 *  @author Guillaume Charhon
 *  @author Deborah Dahl
 *  
 *  Copyright (c) 2011, Colin Turner, Guillaume Charhon
 * 
 *  MIT Licensed
 */
package com.ct.speech;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;

import org.apache.cordova.api.*;
import org.apache.cordova.api.PluginResult.Status;

import android.util.Log;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

class HintReceiver extends BroadcastReceiver {
	com.ct.speech.SpeechRecognizerPlugin speechRecognizerPlugin;
	String callBackId = "";

	@Override
	public void onReceive(Context context, Intent intent) {

		if (getResultCode() != Activity.RESULT_OK) {
			return;
		}
		// the list of supported languages.
		ArrayList<CharSequence> hints = getResultExtras(true)
				.getCharSequenceArrayList(
						RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES);

		// Convert the map to json
		JSONArray languageArray = new JSONArray(hints);
		PluginResult result = new PluginResult(PluginResult.Status.OK,
				languageArray);
		result.setKeepCallback(false);
		// speechRecognizer.callbackId = "";
		speechRecognizerPlugin.success(result, "");
	}

	public void setSpeechRecognizer(
			SpeechRecognizerPlugin speechRecognizerPlugin) {
		this.speechRecognizerPlugin = speechRecognizerPlugin;
	}

	public void setCallBackId(String id) {
		this.callBackId = id;
	}
}

/**
 * access to Google speech recognition
 */

public class SpeechRecognizerPlugin extends Plugin {
	public static final String ACTION_INIT = "init";
	public static final String ACTION_SPEECH_RECOGNIZE = "startRecognize";
	public static final String ACTION_STOP = "stop";
	public static final String NOT_PRESENT_MESSAGE = "Speech recognition is not present or enabled";
	public static final String NO_MATCH = "nomatch";
	public static final String NO_INPUT = "noinput";
	public static final String GOT_RESULTS = "got results";
	protected static final String TAG = SpeechRecognizerPlugin.class
			.getSimpleName();
	public int reqCode = 42;
	public String callbackId = "";
	private boolean recognizerPresent = false;
	
	byte[] sig = new byte[500000]; //not used
	int sigPos = 0;
	SpeechRecognizer recognizer;
	int payloadSize = 0;
	public long speechStart = 0;
	public long speechEnd = 0;
	JSONArray jsonResults;
	String audioURL = "";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.phonegap.api.Plugin#execute(java.lang.String,
	 * org.json.JSONArray, java.lang.String)
	 */
	@Override
	public PluginResult execute(String action, JSONArray args, String callbackId) {
		// Dispatcher
		if (ACTION_INIT.equals(action)) {
			// init
			if (doInit())
				return new PluginResult(Status.OK);
			else
				return new PluginResult(Status.ERROR, NOT_PRESENT_MESSAGE);
		} else if (ACTION_SPEECH_RECOGNIZE.equals(action)) {
			// recognize speech
			if (!recognizerPresent) {
				return new PluginResult(PluginResult.Status.ERROR,
						NOT_PRESENT_MESSAGE);
			}

			if (!this.callbackId.isEmpty()) {
				return new PluginResult(PluginResult.Status.ERROR,
						"Speech recognition is in progress.");
			}

			this.callbackId = callbackId;
			startSpeechRecognitionActivity(args);
			PluginResult res = new PluginResult(Status.NO_RESULT);
			res.setKeepCallback(true);
			return res;
				}else if ("getSupportedLanguages".equals(action)) {
			// save the call back id
			// this.callbackId = callbackId;
			// Get the list of supported languages
			getSupportedLanguages();
			// wait for the intent callback
			PluginResult res = new PluginResult(Status.NO_RESULT);
			res.setKeepCallback(true);
			return res;
		} else if ("stop".equals(action)) {
			stopRecognition();
			// wait for the intent callback
			PluginResult res = new PluginResult(Status.NO_RESULT);
			res.setKeepCallback(true);
			return res;
		} else {
			// Invalid action
			String res = "Unknown action: " + action;
			return new PluginResult(PluginResult.Status.INVALID_ACTION, res);
		}
	}

	/**
	 * Request the supported languages
	 */
	private void getSupportedLanguages() {
		// Create and launch get languages intent
		Intent intent = new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
		HintReceiver hintReceiver = new HintReceiver();
		hintReceiver.setSpeechRecognizer(this);
		// hintReceiver.setCallBackId(this.callbackId);
		ctx.getApplicationContext().sendOrderedBroadcast(intent, null,
				hintReceiver, null, Activity.RESULT_OK, null, null);
	}

	/**
	 * Initialize the speech recognizer by checking if one exists.
	 */
	private boolean doInit() {
		this.recognizerPresent = isSpeechRecognizerPresent();
		return this.recognizerPresent;
	}

	private void stopRecognition() {

	}

	/**
	 * Checks if a recognizer is present on this device
	 */
	private boolean isSpeechRecognizerPresent() {
		PackageManager pm = ((ContextWrapper) ctx).getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		return !activities.isEmpty();
	}

	/**
	 * Fire an intent to start the speech recognition activity.
	 * 
	 * @param args
	 *            Argument array with the following string args: [req
	 *            code][number of matches][prompt string] Google speech
	 *            recognizer
	 */

	private void startSpeechRecognitionActivity(JSONArray args) {
		// int reqCode = 42; // Hitchhiker? // global now
		int maxMatches = 2;
		String prompt = "";
		String language = "";
		try {
			if (args.length() > 0) {
				// Request code - passed back to the caller on a successful
				// operation
				String temp = args.getString(0);
				reqCode = Integer.parseInt(temp);
			}
			if (args.length() > 1) {
				// Maximum number of matches, 0 means the recognizer decides
				String temp = args.getString(1);
				maxMatches = Integer.parseInt(temp);
			}
			if (args.length() > 2) {
				// Optional text prompt
				prompt = args.getString(2);
			}
			if (args.length() > 3) {
				// Optional language specified
				language = args.getString(3);
			}
		} catch (Exception e) {
			Log.e(TAG, String.format(
					"startSpeechRecognitionActivity exception: %s",
					e.toString()));
		}
		final Intent intent = new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra("calling_package", "com.ct.BasicAppFrame");
		// If specific language
		if (!language.equals("")) {
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
		}
		if (maxMatches > 0)
			intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, maxMatches);
		if (!(prompt.length() == 0))
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
		// ctx.startActivityForResult(this, intent, reqCode); //removed to try
		// using recognizer directly
		try {
			this.ctx.runOnUiThread(new Runnable() {
				public void run() {
					final SpeechRecognizer recognizer = SpeechRecognizer
							.createSpeechRecognizer((Context) ctx);
					RecognitionListener listener = new RecognitionListener() {
						@Override
						public void onResults(Bundle results) {
							//closeRecordedFile();
							sendBackResults(results);
							ArrayList<String> voiceResults = results
									.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
							if (voiceResults == null) {
								Log.e(TAG, "No voice results");
							} else {
								// Log.d(TAG, "Printing matches: ");
								for (@SuppressWarnings("unused") String match : voiceResults) {
									// Log.d(TAG, match);
								}
							}
							recognizer.destroy();
						}

						@Override
						public void onReadyForSpeech(Bundle params) {
							// Log.d(TAG, "Ready for speech");
						}

						@Override
						public void onError(int error) {
							Log.d(TAG, "Error listening for speech: " +
							error);
							if(error == SpeechRecognizer.ERROR_NO_MATCH){
								sendBackResults(NO_MATCH);
							}
							else if(error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT){
								sendBackResults(NO_INPUT);
							}
							else{
							speechFailure("unknown error");
							}
							recognizer.destroy();
						}

						@Override
						public void onBeginningOfSpeech() {
							// Log.d(TAG, "Speech starting");
							setStartOfSpeech();
						}

						@Override
						//doesn't fire in Android after Ice Cream Sandwich
						public void onBufferReceived(byte[] buffer) {
						}

						@Override
						public void onEndOfSpeech() {
							setEndOfSpeech();
						}

						@Override
						public void onEvent(int eventType, Bundle params) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onPartialResults(Bundle partialResults) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onRmsChanged(float rmsdB) {
							// TODO Auto-generated method stub

						}
					};
					recognizer.setRecognitionListener(listener);
					Log.d(TAG,"starting speech recognition activity"); 
					recognizer.startListening(intent);
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void sendBackResults(Bundle results) {
		ArrayList<String> voiceResults = results
				.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		speechResults(reqCode, voiceResults);
	}
	
	//here we create emma for noinput and nomatch and send back the results
	public void sendBackResults(String error) {
		String emmaBoilerplate = "<emma:emma xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:emma='http://www.w3.org/2003/04/emma' xsi:schemaLocation='http://www.w3.org/2003/04/emma http://www.w3.org/TR/2007/CR-emma-20071211/emma.xsd' version='1.0'>";
		StringBuilder sb = new StringBuilder();
		sb.append(emmaBoilerplate);
		sb.append("<emma:interpretation id="
				+ "\""
				+ "request"
				+ reqCode
				+ "\" emma:medium='acoustic' emma:mode='voice' emma:process=\"googleASR\"");
		if(error == NO_INPUT){
			sb.append("emma:no-input='true'");
		}
		else if(error == NO_MATCH){
			String startString = "emma:start=\"" + speechStart + "\"";
			String endString = "emma:end=" + "\"" + speechEnd + "\"";
			sb.append("emma:uninterpreted='true' " + startString + endString);
		}
		sb.append("/>");
		sb.append("</emma:emma>");
		String finalEmma = sb.toString();
		PluginResult result = new PluginResult(PluginResult.Status.OK,
				finalEmma);
		result.setKeepCallback(false);
		this.success(result, this.callbackId);
		this.callbackId = "";
	}
	

	/**
	 * Handle the results from the recognition activity.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			// Fill the list view with the strings the recognizer thought it
			// could have heard
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			speechResults(requestCode, matches);

		} else if (resultCode == Activity.RESULT_CANCELED) {
			// cancelled by user
			speechFailure("Cancelled");
		} else {
			speechFailure("Unknown error");
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void speechResults(int requestCode, ArrayList<String> matches) {
		String emmaBoilerplate = "<emma:emma xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:emma='http://www.w3.org/2003/04/emma' xsi:schemaLocation='http://www.w3.org/2003/04/emma http://www.w3.org/TR/2007/CR-emma-20071211/emma.xsd' version='1.0'>";
		StringBuilder sb = new StringBuilder();
		sb.append(emmaBoilerplate);
		String startString = "emma:start=\"" + speechStart + "\"";
		String endString = "emma:end=" + "\"" + speechEnd + "\"";
		sb.append("<emma:one-of id="
				+ "\""
				+ "request"
				+ requestCode
				+ "\" emma:medium='acoustic' emma:mode='voice' emma:process=\"googleASR\""
				+ " " + startString + " " + endString + ">");
		Iterator<String> iterator = matches.iterator();
		int matchCounter = 0;
		while (iterator.hasNext()) {
			matchCounter++;
			String match = iterator.next();
			String id = "id= \"result" + matchCounter + "\"";
			String tokenIntro = " emma:tokens=";
			String confidenceString = " emma:confidence=\"0.0\"";
			sb.append("<emma:interpretation " + id + tokenIntro + "\"" + match
					+ "\"" + confidenceString + ">");
			sb.append("<emma:literal>" + match
					+ "</emma:literal></emma:interpretation>");
		}
		sb.append("</emma:one-of>");
		sb.append("</emma:emma>");
		String finalEmma = sb.toString();
		PluginResult result = new PluginResult(PluginResult.Status.OK,
				finalEmma);
		result.setKeepCallback(false);
		this.success(result, this.callbackId);
		this.callbackId = "";
	}
	
	//for EMMA timestamps
	private void setEndOfSpeech() {
		this.speechEnd = System.currentTimeMillis();
	}

	private void setStartOfSpeech() {
		this.speechStart = System.currentTimeMillis();
	}

	/*
	 * private void speechResults(int requestCode, ArrayList<String> matches) {
	 * boolean firstValue = true; StringBuilder sb = new StringBuilder();
	 * sb.append("{\"speechMatches\": {"); sb.append("\"requestCode\": ");
	 * sb.append(Integer.toString(requestCode));
	 * sb.append(", \"speechMatch\": [");
	 * 
	 * Iterator<String> iterator = matches.iterator(); while
	 * (iterator.hasNext()) { String match = iterator.next(); if (firstValue ==
	 * false) sb.append(", "); firstValue = false;
	 * sb.append(JSONObject.quote(match)); } sb.append("]}}");
	 * 
	 * PluginResult result = new PluginResult(PluginResult.Status.OK,
	 * sb.toString()); result.setKeepCallback(false); this.success(result,
	 * this.callbackId); this.callbackId = ""; }
	 */

	private void speechFailure(String message) {
		PluginResult result = new PluginResult(PluginResult.Status.ERROR,
				message);
		result.setKeepCallback(false);
		this.error(result, this.callbackId);
		this.callbackId = "";
	}
}




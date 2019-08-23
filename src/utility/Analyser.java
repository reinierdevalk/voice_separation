package utility;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.uos.fmt.musitech.data.score.NotationChord;
import de.uos.fmt.musitech.data.score.NotationStaff;
import de.uos.fmt.musitech.data.score.NotationSystem;
import de.uos.fmt.musitech.data.score.NotationVoice;
import de.uos.fmt.musitech.data.structure.Note;
import de.uos.fmt.musitech.data.structure.container.NoteSequence;
import de.uos.fmt.musitech.utility.math.Rational;
import featureExtraction.FeatureGenerator;
import representations.Tablature;
import representations.Transcription;
import tbp.TabSymbol;
import ui.Runner;

public class Analyser {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
    // 1. Tablature data
		List<String> pieceNames = new ArrayList<String>();    
    pieceNames.add("Ochsenkun 1558 - Absolon fili mi");
  	pieceNames.add("Ochsenkun 1558 - In exitu Israel de Egipto");
  	pieceNames.add("Ochsenkun 1558 - Qui habitat");
  	pieceNames.add("Rotta 1546 - Bramo morir per non patir piu morte");
  	pieceNames.add("Phalese 1547 - Tant que uiuray [a4]");
  	pieceNames.add("Ochsenkun 1558 - Herr Gott lass dich erbarmen");
  	pieceNames.add("Abondante 1548 - mais mamignone"); 
  	pieceNames.add("Phalese 1563 - LAs on peult");
  	pieceNames.add("Barbetta 1582 - Il nest plaisir");
	
//  	pieceNames.add("Judenkunig 1523 - Elslein liebes Elslein");
//  	pieceNames.add("Newsidler 1536 - Disant adiu madame");
//  	pieceNames.add("Newsidler 1544 - Nun volget Lalafete");
//  	pieceNames.add("Phalese 1547 - Tant que uiuray [a3]");
//  	pieceNames.add("Pisador 1552 - Pleni de la missa misma");
//  	pieceNames.add("Ochsenkun 1558 - Cum Sancto spiritu");
//  	pieceNames.add("Adriansen 1584 - D'Vn si bel foco");
//  	pieceNames.add("Ochsenkun 1558 - Inuiolata integra");


//  	for (String s : pieceNames) {
//  		File encodingFile = new File(ExperimentRunner.encodingsPath + s + ".txt"); 
//  		File midiFile = new File(ExperimentRunner.tabMidiPath + s);		
//  		Tablature tablature = new Tablature(encodingFile);
//      Transcription transcription = new Transcription(midiFile, encodingFile);
//      System.out.println(transcription.getVoiceCrossingInformation(tablature));
//  	}
//    Integer[] result = hasChordsAtGivenDistances(pieceNames);    
//    System.out.println(getDurationsInfo(pieceNames));
//  	System.exit(0);

  	
		// 2. Bach data
  	String folderName = null;
		String pieceName = null;		
		// WTC, Book I
//		folderName = ExperimentRunner.bachMidiPath + "WTC/2vv/";
//    pieceName = "Bach - WTC1, Fuga 10 in e minor (BWV 855)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC1, Fuga 10 in e minor (BWV 855)";
  
//	  folderName = ExperimentRunner.bachMidiPath + "WTC/3vv/";
//    pieceName = "Bach - WTC1, Fuga 2 in c minor (BWV 847)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC1, Fuga 2 in c minor (BWV 847)";
//    pieceName = "Bach - WTC1, Fuga 3 in C# major (BWV 848)/musedata.org/Unedited/";
//	  pieceName = "Bach - WTC1, Fuga 3 in C# major (BWV 848)";
//    pieceName = "Bach - WTC1, Fuga 6 in d minor (BWV 851)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC1, Fuga 6 in d minor (BWV 851)";
//    pieceName = "Bach - WTC1, Fuga 7 in Eb major (BWV 852)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC1, Fuga 7 in Eb major (BWV 852)";
//    pieceName = "Bach - WTC1, Fuga 8 in d# minor (BWV 853)/musedata.org/Unedited/";
//	  pieceName = "Bach - WTC1, Fuga 8 in d# minor (BWV 853)";
//    pieceName = "Bach - WTC1, Fuga 9 in E major (BWV 854)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC1, Fuga 9 in E major (BWV 854)";
//    pieceName = "Bach - WTC1, Fuga 11 in F major (BWV 856)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC1, Fuga 11 in F major (BWV 856)";
//    pieceName = "Bach - WTC1, Fuga 13 in F# major (BWV 858)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC1, Fuga 13 in F# major (BWV 858)";
//    pieceName = "Bach - WTC1, Fuga 15 in G major (BWV 860)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC1, Fuga 15 in G major (BWV 860)";
//    pieceName = "Bach - WTC1, Fuga 19 in A major (BWV 864)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC1, Fuga 19 in A major (BWV 864)";
//    pieceName = "Bach - WTC1, Fuga 21 in Bb major (BWV 866)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC1, Fuga 21 in Bb major (BWV 866)";
	
//	  folderName = ExperimentRunner.bachMidiPath + "WTC/4vv/";
//    pieceName = "Bach - WTC1, Fuga 1 in C major (BWV 846)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC1, Fuga 1 in C major (BWV 846)";
//    pieceName = "Bach - WTC1, Fuga 5 in D major (BWV 850)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC1, Fuga 5 in D major (BWV 850)";
//    pieceName = "Bach - WTC1, Fuga 12 in f minor (BWV 857)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC1, Fuga 12 in f minor (BWV 857)";
//    pieceName = "Bach - WTC1, Fuga 14 in f# minor (BWV 859)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC1, Fuga 14 in f# minor (BWV 859)";
//    pieceName = "Bach - WTC1, Fuga 16 in g minor (BWV 861)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC1, Fuga 16 in g minor (BWV 861)";
//    pieceName = "Bach - WTC1, Fuga 17 in Ab major (BWV 862)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC1, Fuga 17 in Ab major (BWV 862)";
//    pieceName = "Bach - WTC1, Fuga 18 in g# minor (BWV 863)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC1, Fuga 18 in g# minor (BWV 863)";
//    pieceName = "Bach - WTC1, Fuga 20 in a minor (BWV 865)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC1, Fuga 20 in a minor (BWV 865)";
//    pieceName = "Bach - WTC1, Fuga 23 in B major (BWV 868)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC1, Fuga 23 in B major (BWV 868)";
//    pieceName = "Bach - WTC1, Fuga 24 in b minor (BWV 869)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC1, Fuga 24 in b minor (BWV 869)";

//    folderName = ExperimentRunner.bachMidiPath + "WTC/5vv/";
//    pieceName = "Bach - WTC1, Fuga 4 in c# minor (BWV 849)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC1, Fuga 4 in c# minor (BWV 849)";
//    pieceName = "Bach - WTC1, Fuga 22 in bb minor (BWV 867)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC1, Fuga 22 in bb minor (BWV 867)"; 
	
	  // WTC, Book II
//    folderName = ExperimentRunner.bachMidiPath + "WTC/3vv/";
//    pieceName = "Bach - WTC2, Fuga 1 in C major (BWV 870)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 1 in C major (BWV 870)";
//    pieceName = "Bach - WTC2, Fuga 3 in C# major (BWV 872)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 3 in C# major (BWV 872)";
//    pieceName = "Bach - WTC2, Fuga 4 in c# minor (BWV 873)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 4 in c# minor (BWV 873)";
//    pieceName = "Bach - WTC2, Fuga 6 in d minor (BWV 875)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 6 in d minor (BWV 875)";
//    pieceName = "Bach - WTC2, Fuga 10 in e minor (BWV 879)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 10 in e minor (BWV 879)";
//    pieceName = "Bach - WTC2, Fuga 11 in F major (BWV 880)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 11 in F major (BWV 880)";
//    pieceName = "Bach - WTC2, Fuga 12 in f minor (BWV 881)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 12 in f minor (BWV 881)";
//    pieceName = "Bach - WTC2, Fuga 13 in F# major (BWV 882)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 13 in F# major (BWV 882)";
//    pieceName = "Bach - WTC2, Fuga 14 in f# minor (BWV 883)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 14 in f# minor (BWV 883)";
//    pieceName = "Bach - WTC2, Fuga 15 in G major (BWV 884)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 15 in G major (BWV 884)";
//    pieceName = "Bach - WTC2, Fuga 18 in g# minor (BWV 887)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 18 in g# minor (BWV 887)";
//    pieceName = "Bach - WTC2, Fuga 19 in A major (BWV 888)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 19 in A major (BWV 888)";
//    pieceName = "Bach - WTC2, Fuga 20 in a minor (BWV 889)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 20 in a minor (BWV 889)";
//    pieceName = "Bach - WTC2, Fuga 21 in Bb major (BWV 890)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 21 in Bb major (BWV 890)";
//    pieceName = "Bach - WTC2, Fuga 24 in b minor (BWV 893)/musedata.org/Unedited";
//    pieceName = "Bach - WTC2, Fuga 24 in b minor (BWV 893)";

		folderName = Runner.midiPath + "bach-WTC/thesis/4vv/";
//    pieceName = "Bach - WTC2, Fuga 2 in c minor (BWV 871)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 2 in c minor (BWV 871)";
//    pieceName = "Bach - WTC2, Fuga 5 in D major (BWV 874)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 5 in D major (BWV 874)";
//    pieceName = "Bach - WTC2, Fuga 7 in Eb major (BWV 876)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 7 in Eb major (BWV 876)";
//    pieceName = "Bach - WTC2, Fuga 8 in d# minor (BWV 877)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 8 in d# minor (BWV 877)";
//    pieceName = "Bach - WTC2, Fuga 9 in E major (BWV 878)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 9 in E major (BWV 878)";
//    pieceName = "Bach - WTC2, Fuga 16 in g minor (BWV 885)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 16 in g minor (BWV 885)";
//    pieceName = "Bach - WTC2, Fuga 17 in Ab major (BWV 886)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 17 in Ab major (BWV 886)";
//    pieceName = "Bach - WTC2, Fuga 22 in bb minor (BWV 891)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 22 in bb minor (BWV 891)";
//    pieceName = "Bach - WTC2, Fuga 23 in B major (BWV 892)/musedata.org/Unedited/";
//    pieceName = "Bach - WTC2, Fuga 23 in B major (BWV 892)";
   		
		// Inventions
//		folderName = ExperimentRunner.bachMidiPath + "inventions/2vv/";
//		pieceName = "Bach - Inventio 1 in C major (BWV 772)/musedata.org/Unedited/";
//		pieceName = "Bach - Inventio 1 in C major (BWV 772)";
//		pieceName = "Bach - Inventio 2 in c minor (BWV 773)/musedata.org/Unedited/";
//		pieceName = "Bach - Inventio 2 in c minor (BWV 773)";
//		pieceName = "Bach - Inventio 3 in D major (BWV 774)/musedata.org/Unedited/";
//		pieceName = "Bach - Inventio 3 in D major (BWV 774)";
//		pieceName = "Bach - Inventio 4 in d minor (BWV 775)/musedata.org/Unedited/";
//		pieceName = "Bach - Inventio 4 in d minor (BWV 775)";
//		pieceName = "Bach - Inventio 5 in Eb major (BWV 776)/musedata.org/Unedited/";
//		pieceName = "Bach - Inventio 5 in Eb major (BWV 776)";
//		pieceName = "Bach - Inventio 6 in E major (BWV 777)/musedata.org/Unedited/";
//		pieceName = "Bach - Inventio 6 in E major (BWV 777)";
//		pieceName = "Bach - Inventio 7 in e minor (BWV 778)/musedata.org/Unedited/";
//		pieceName = "Bach - Inventio 7 in e minor (BWV 778)";
//		pieceName = "Bach - Inventio 8 in F major (BWV 779)/musedata.org/Unedited/";
//		pieceName = "Bach - Inventio 8 in F major (BWV 779)";
//		pieceName = "Bach - Inventio 9 in f minor (BWV 780)/musedata.org/Unedited/";
//		pieceName = "Bach - Inventio 9 in f minor (BWV 780)";
//		pieceName = "Bach - Inventio 10 in G major (BWV 781)/musedata.org/Unedited/";
//		pieceName = "Bach - Inventio 10 in G major (BWV 781)";
//		pieceName = "Bach - Inventio 11 in g minor (BWV 782)/musedata.org/Unedited/";
//		pieceName = "Bach - Inventio 11 in g minor (BWV 782)";
		pieceName = "Bach - Inventio 12 in A major (BWV 783)/musedata.org/Unedited/";
//		pieceName = "Bach - Inventio 12 in A major (BWV 783)";
//		pieceName = "Bach - Inventio 13 in a minor (BWV 784)/musedata.org/Unedited/";
//		pieceName = "Bach - Inventio 13 in a minor (BWV 784)";
//		pieceName = "Bach - Inventio 14 in Bb major (BWV 785)/musedata.org/Unedited/";
//		pieceName = "Bach - Inventio 14 in Bb major (BWV 785)";
//		pieceName = "Bach - Inventio 15 in b minor (BWV 786)/musedata.org/Unedited/";
//		pieceName = "Bach - Inventio 15 in b minor (BWV 786)";
		
//    checkOnsetTimeLastNote(folderName, pieceName);
//    Transcription transcription = new Transcription(new File(TrainingManager.prefix + folderName + pieceName), null);
//    System.out.println(transcription.getVoiceCrossingInformation(null));
//    System.out.println(hasDoubleNote(folderName, pieceName));
//    System.out.println(chordSizeChecker(folderName, pieceName));
//    System.exit(0);
		
 	 	List<String> threeVoiceFuguesPieceNames = null; // Dataset.getBachThreeVoiceFugues();
 	  List<String> fourVoiceFuguesPieceNames = null; // Dataset.getBachFourVoiceFugues();
 	  List<String> threeVoiceIntabsPieceNames = null; // Dataset.getTabThreeVoices();
 	  List<String> fourVoiceIntabsPieceNames = null; // Dataset.getTabFourVoices();
 	  
 	  List<String> bla = new ArrayList<String>();
 	  bla.add("Abondante 1548 - mais mamignone");
 	  
 	  folderName = Runner.midiPath + "intabulations/4vv/";
 	  
 	  for (String s : threeVoiceFuguesPieceNames) {
 	  	getNumberOfVoiceCrossingPairs(folderName, s, 0);
 	  }
 	  System.exit(0);
 	  
// 	  folderName = "MIDI/Bach/WTC/Four-voice fugues/";
// 	  pieceName = "Bach - WTC1, Fuga 20 in a minor (BWV 865)";
 	  
 	 
// 	  String results = "";
// 	  results = results.concat(hasMoreThanOneUnison(folderName, pieceName));
//    for (String s : fourVoiceFuguesPieceNames) {
//    	results = results.concat(hasMoreThanOneUnison(folderName, s));
//    }
//    System.out.println(results);
    


	}
	
	
	/**
	 * Chekcs whether the given piece has 
	 * (1) Any chords with more than one unison
	 * (2) Any chords with more than two of the same pitches.
	 * 
	 * NB: Non-tablature case only.
	 * 
	 * @param pieceName
	 * @return
	 */
	private static String hasMoreThanOneUnison(String folderName, String pieceName) {
		String results = folderName + pieceName + ":" + "\n";
		
		Transcription transcription = new Transcription(new File(folderName + pieceName), null);
		List<Integer[]> meterInfo = transcription.getMeterInfo();
		Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
		int totalNumUnisons = 0;
		int numUnisonsEqualDuration = 0;
		int numUnisonsDifferentDuration = 0;
		
//		System.out.println(transcription.getNoteSequence().size());
//		System.out.println(transcription.getVoiceLabels().size());
//		System.out.println(transcription.getBasicNoteProperties().length);
//		List<Integer[]> eqDur = transcription.getEqualDurationUnisonsInfo();
//		System.out.println(eqDur.size());
//		
//		for (int i = 0; i < eqDur.size(); i++) {
//		  System.out.println(i + " " + eqDur.get(i));
//		}
//		System.exit(0);
		
		
		for (int i = 0; i < transcription.getTranscriptionChords().size(); i++) {
			List<Note> currentChord = transcription.getTranscriptionChords().get(i);
			
			Rational currentOnsetTime = currentChord.get(0).getMetricTime();
			Rational[] metricPos = Tablature.getMetricPosition(currentOnsetTime, meterInfo);
			String barNum = String.valueOf(metricPos[0].getNumer());
			String posInBar = "";
			if (metricPos[1].getNumer() != 0) {
			  posInBar = " " + metricPos[1];
			}
			String metricPosAsString = barNum + posInBar;
					
			// a. Determine number of unisons
			if (transcription.getUnisonInfo(i) != null) {
				totalNumUnisons++;
			  if (transcription.getUnisonInfo(i).length == 1) {
			  	results = results.concat("  chord at index " + i + " (metric position " + metricPosAsString + 
//			  		") has more than one unison." + "\n");
			  			") has a unison." + "\n");
			  }
			  int indexLower = transcription.getUnisonInfo(i)[0][1];
			  int indexUpper = transcription.getUnisonInfo(i)[0][2];
			  int notesPreceding = 0;
			  for (int j = 0; j < basicNoteProperties.length; j++) {
			  	 int num = basicNoteProperties[j][Transcription.ONSET_TIME_NUMER];
			  	 int denom = basicNoteProperties[j][Transcription.ONSET_TIME_DENOM];
			  	 Rational onsetTime = new Rational(num, denom);
			  	 if (onsetTime.equals(currentOnsetTime)) {
			  		 notesPreceding = j;
			  		 break;
			  	 }
			  }
			  indexLower += notesPreceding;
			  indexUpper += notesPreceding;
			  Rational durationLower = transcription.getNoteSequence().getNoteAt(indexLower).getMetricDuration();
			  Rational durationUpper = transcription.getNoteSequence().getNoteAt(indexUpper).getMetricDuration();
			  if (durationLower.equals(durationUpper)) {
			  	numUnisonsEqualDuration++;
			  }
			  else {
			  	numUnisonsDifferentDuration++;
			  }
			   
			  //b. Determine number of equal pitches
			  List<Integer> pitchesInChord = new ArrayList<Integer>();
			  for (Note n : currentChord) {
		  		pitchesInChord.add(n.getMidiPitch());
	  		}
	  		for (int pitch : pitchesInChord) {
	  			if (Collections.frequency(pitchesInChord, pitch) == 2) {
	  				results = results.concat("  chord at index " + i + " (metric position " + metricPosAsString + 
//	  					") has more than two notes with pitch " + pitch + "." + "\n");
    					") has two notes with pitch " + pitch + "." + "\n");}	
	  		}
	 		}
		}
		results = results.concat("  Total number of unisons: " + totalNumUnisons + " (" + numUnisonsEqualDuration + 
			" with equal durations; " + numUnisonsDifferentDuration + " with different durations)." + "\n");	
		results = results.concat("\n");
		return results;
	}
	
	
	/**
	 * Checks the size of each chord and returns a String with information on which chords contain more notes
	 * (including sustained notes) than the maximum number of voices. 
	 * 
	 * Non-tablature case only.
	 * 
	 * @param folderName
	 * @param pieceName
	 * @return
	 */
  private static String chordSizeChecker(String folderName, String pieceName) {	
		String chordSizeInformation = "";
			  
    Transcription transcription = new Transcription(new File(folderName + pieceName), null);
//    preprocessor.preprocess(null, transcription, false, new Integer(0));
  
    List<Integer[]> meterInfo = transcription.getMeterInfo();
    
    int maxNumVoices = -1;
//    if (folderName.contains("fugues")) { // for checking for overlap in individual voices
//    	maxNumVoices = 1;
//    }
    if (folderName.contains("Two") || folderName.contains("Inventiones")) {
    	maxNumVoices = 2;
    }
    else if (folderName.contains("Three")) {
    	maxNumVoices = 3;
    }
    else if (folderName.contains("Four")) {
    	maxNumVoices = 4;
    }
    else if (folderName.contains("Five")) {
    	maxNumVoices = 5;
    }
    
    // For each chord
    Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
    List<List<Note>> transcriptionChords = transcription.getTranscriptionChords();
//    int numChords = transcription.getNumberOfChords();
    chordSizeInformation = chordSizeInformation.concat(pieceName + "\r\n");
    chordSizeInformation = chordSizeInformation.concat("number of notes = " + basicNoteProperties.length + "\r\n");
    chordSizeInformation = chordSizeInformation.concat("number of chords = " + transcriptionChords.size() + "\r\n");
//    chordSizeInformation = chordSizeInformation.concat("number of chords = " + numChords + "\r\n");
    int lowestNoteIndex = 0;
    int numNotes = 0;
  	boolean addedVoiceFound = false;
//    for (int i = 0; i < numChords; i++) {
    for (int i = 0; i < transcriptionChords.size(); i++) {
    	int onsetTimeNum = basicNoteProperties[lowestNoteIndex][Transcription.ONSET_TIME_NUMER];
    	int onsetTimeDenom = basicNoteProperties[lowestNoteIndex][Transcription.ONSET_TIME_DENOM]; 
    	Rational onsetTimeCurrentNote = new Rational(onsetTimeNum, onsetTimeDenom);     	
    	int numNewOnsets = basicNoteProperties[lowestNoteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
    	int numSustainedNotes = 
    		Transcription.getIndicesOfSustainedPreviousNotes(null, null, basicNoteProperties, lowestNoteIndex).size();
    	int size = numNewOnsets + numSustainedNotes;
    	numNotes += numNewOnsets;
    	
   		if (size > maxNumVoices) {
   		  // Determine onsetTimeAsString
   			Rational[] metricPosition = Tablature.getMetricPosition(onsetTimeCurrentNote, meterInfo);
   			String onsetTimeAsString = "" + metricPosition[0].getNumer();
   			if (metricPosition[1].getNumer() != 0) {
   				onsetTimeAsString += " " + metricPosition[1]; 
   			}
   			chordSizeInformation = chordSizeInformation.concat("chord of size " + size + " at chordIndex " + i +
   				" (bar " + onsetTimeAsString + ")" + "\r\n");
      	addedVoiceFound = true;
   	  }
    	// Go to next chord
    	lowestNoteIndex += numNewOnsets;
    }
    if (addedVoiceFound == false) {
    	chordSizeInformation = chordSizeInformation.concat("No temporarily added voices.");
    }
    System.out.println("numNotes = " + numNotes);
    return chordSizeInformation;
	}
  
  
  /**
   * Gets the onset time of the last note of the piece. 
   * 
   * Non-tablature case only.
   * 
   * @param folderName
   * @param pieceName
   */
  private static void checkOnsetTimeLastNote(String folderName, String pieceName) {
  	Transcription transcription = new Transcription(new File(folderName + pieceName), null);
//  	preprocessor.preprocess(null, transcription, false, new Integer(0));
    
  	List<Integer[]> meterInfo = transcription.getMeterInfo();
  	  
  	Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
  	int numNotes = basicNoteProperties.length;
  	
  	Rational mt = new Rational(basicNoteProperties[numNotes - 1][Transcription.ONSET_TIME_NUMER],
  		basicNoteProperties[numNotes - 1][Transcription.ONSET_TIME_DENOM]);

  	Rational[] metricPosition = Tablature.getMetricPosition(mt, meterInfo);
  	System.out.println("bar no   = " + (double) metricPosition[0].getNumer() / metricPosition[0].getDenom());
  	System.out.println("position = " + (double) metricPosition[1].getNumer() / metricPosition[1].getDenom());
  }
  
  
  /**
   * Finds the position of any double notes in a voice. A double note occurs where two notes in a voice have the same 
   * onset time (but not necessarily the same offset time).
   * 
   * NB: Non-tablature case only.
   * 
   * @param folderName
   * @param pieceName
   * @param meter
   */
  private static String hasDoubleNote(String folderName, String pieceName) {	
  	String doubleNoteInformation = "";
  	Transcription transcription = new Transcription(new File(folderName + pieceName), null);
//  	preprocessor.preprocess(null, transcription, false, new Integer(0));

  	List<Integer[]> meterInfo = transcription.getMeterInfo();
  	    	
  	// For each voice
  	NotationSystem notationSystem = transcription.getPiece().getScore();
  	for (int i = 0; i < notationSystem.size(); i++) {
  	  NotationStaff staff = notationSystem.get(i);
	    if (staff.size() != 1) {
	    	System.out.println("STAFF SIZE ERROR");
	    	System.exit(0);
	    }
	    NotationVoice voice = staff.get(0);
	    	    
	    // List for each NotationChord all the Notes in it as well as all their metric times
	    List<Note> allNotes = new ArrayList<Note>();
	    List<Rational> allMetricTimesCurrentVoice = new ArrayList<Rational>();
	    for (int j = 0; j < voice.size(); j++) {
	     	NotationChord notationChord = voice.get(j);
	     	for (Note n : notationChord) {
	     	  allNotes.add(n);
	     		allMetricTimesCurrentVoice.add(n.getMetricTime());
	     	}
	    }
	    // Determine which notes have the same metric time, these are the double notes sought
	    List<Note> metricTimeMoreThanOnce = new ArrayList<Note>();
	    for (Note n : allNotes) {
	    	Rational metricTime = n.getMetricTime();
	    	if (Collections.frequency(allMetricTimesCurrentVoice, metricTime) > 1) {
	    		metricTimeMoreThanOnce.add(n);
	    	}
	    }
      // Print out
	    doubleNoteInformation += "Voice = " + i + "\r\n";
	    for (Note n: metricTimeMoreThanOnce) {
	    	doubleNoteInformation += "More than one note in voice " + i + " in bar " + 
		      Tablature.getMetricPosition(n.getMetricTime(), meterInfo)[0].getNumer() + ", beat " + 
		    	Tablature.getMetricPosition(n.getMetricTime(), meterInfo)[1] + " (pitch = " + n.getMidiPitch() + ")" + "\r\n";
	    }    
  	}
  	return doubleNoteInformation;
  }
  
  
  /**
   * Returns a String containing duration information of the list of given pieces.
   * 
   * @param pieceNames
   * @return
   */
  private static String getDurationsInfo(List<String> pieceNames) {
  	String durationsInfo = "";
  			
  	List<Rational> allDurations = new ArrayList<Rational>();
	  List<Rational> durationsEncountered = new ArrayList<Rational>();
//	  List<Rational[]> allDurationsInclCoDs = new ArrayList<Rational[]>();
	  Integer[] freqOfAllDurations = new Integer[32]; 
	  Arrays.fill(freqOfAllDurations, 0);
	
	  int numNotes = 0;
	  for (int i = 0; i < pieceNames.size(); i++) {
    	String pieceName = pieceNames.get(i);
    	String folderName = "dataset/";
	  	if (pieceName.equals("testpiece")) {
	  		folderName = "tests/"; 
	  	}
      File midiFile = new File("F/PhD/data" + "MIDI/" + folderName + pieceName);
      Transcription transcription = new Transcription(midiFile, null);
      
      NoteSequence noteSeq = transcription.getNoteSequence();
      Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
      
      // Security check
      if (noteSeq.size() != basicNoteProperties.length) {
        System.out.println("noteSeq and basicNoteProperties do not have the same size");
        System.exit(0);
      }
      
      for (int j = 0; j < noteSeq.size(); j++) {
  	    Note currentNote = noteSeq.getNoteAt(j);
  	    numNotes++;
    	  Rational currentDuration = currentNote.getMetricDuration();
    	  Integer[] currentBNP = basicNoteProperties[j];
    	  Rational currentDurationFromBNP = new Rational(currentBNP[Transcription.DUR_NUMER],
    	  	currentBNP[Transcription.DUR_DENOM]);
    	  // Security check
    	  if (!currentDuration.equals(currentDurationFromBNP)) {
    	  	System.out.println(j);
    	  	System.out.println(pieceName);
    	  	System.out.println(currentNote);
    	  	System.out.println(currentBNP[0] + " " + currentBNP[1] + " " + currentBNP[2] + " " + 
    	  			currentBNP[3] + " " + currentBNP[4] + " " + currentBNP[5] + " " + currentBNP[6] + " " +
    	  			currentBNP[7]);
    	  	System.out.println(currentDuration);
    	  	System.out.println(currentDurationFromBNP);
    	  	System.out.println("Duration from NoteSequence and basicNoteProperties is not the same");
          System.exit(0);
    	  }
        // Get the duration for each note and add it to allDurations (if it is not in there yet). Also
        // keep track of how often each duration occurs  
    	  else {
//    	  	if (currentDuration.equals(new Rational(3, 4))) {
//    	  		System.out.println("3/4 found in " + pieceName + " at metric time " + 
//    	  	  new Rational(currentBNP[Transcription.ONSET_TIME_NUMER],	currentBNP[Transcription.ONSET_TIME_DENOM]));
//    	  	}
    	  	if (!allDurations.contains(currentDuration)) {
    	  		allDurations.add(currentDuration);
    	  	}
    	  	
    	  	int numer = currentDuration.getNumer();
    	    int denom = currentDuration.getDenom();
    	    if (denom != Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom()) {
    	    	numer *= Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom() / denom;
    	    }
    	    freqOfAllDurations[numer - 1]++; 	  	
    	  }	  
      }
	  }
    
    Collections.sort(allDurations);
//    String durationsEncounteredAsString = "";
    durationsInfo = durationsInfo.concat("\r\n" + "pieces = " + pieceNames + "\r\n");
    durationsInfo = durationsInfo.concat("total number of notes: " + numNotes + "\r\n");
    durationsInfo = durationsInfo.concat("different durations encountered:") + "\r\n";
//    for (Rational r : durationsEncountered) {
//	    durationsEncounteredAsString += (r + "\t");
//    }	
//    durationsInfo = durationsInfo.concat(durationsEncounteredAsString) + "\r\n";
  
//    durationsInfo = durationsInfo.concat("Frequency of different durations encountered:" + "\r\n");
    durationsInfo = durationsInfo.concat("duration" + "\t" + "frequency" + "\t" + "percentage (frequency/total " 
      + "number of notes)" + "\r\n");
    for (int j = 0; j < freqOfAllDurations.length; j++) {
    	int freq = freqOfAllDurations[j];
    	Rational r = new Rational(j+1, 32);
    	r.reduce();
    	if (freq != 0) {
    		durationsInfo = durationsInfo.concat(r + "\t\t" + freq + "\t\t" + ((double)freq/numNotes)*100 + "\r\n");
    	}
    }
  
//    String allDurationsAsString = "";
//    durationsInfo = durationsInfo.concat("Durations per note:" + "\r\n");
//    int counter = 0;
//    for (Rational[] durations : allDurationsInclCoDs) {
//    	String curr = "note " + counter + ": ";
//    	for (Rational r : durations) {
//    	  curr += r + "  ";
//    	}
//    	allDurationsAsString += curr + "\n";
//    	counter++;
//    }
//    for (Rational r : allDurations) {
//    	String curr = "note " + counter + ": ";    	
//    	allDurationsAsString += curr + r + "\n";
//    	counter++;
//    }
//    durationsInfo = durationsInfo.concat(allDurationsAsString + "\r\n");

//    durationsInfo = durationsInfo.concat(durationsInfo + "number of notes: " + numNotes + "\r\n" + "\r\n");
    return durationsInfo;
  }
  
  
  /**
   * Checks the given piece for the presence of chords with more than the specified number of voice crossing
   * pairs.
   */
  private static void getNumberOfVoiceCrossingPairs(String folderName, String pieceName, int maxNumVoiceCrossingPairs) { 
  	System.out.println(pieceName);
  	
  	File midiFile = new File(folderName + pieceName);
  	Transcription transcription = new Transcription(midiFile, null);
  	List<Integer[]> meterInfo = transcription.getMeterInfo();
  	Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
  	List<List<Note>> transChords = transcription.getTranscriptionChords();
  	List<List<Double>> allVoiceLabels = transcription.getVoiceLabels();
  	List<List<List<Double>>> chordVoiceLabels = transcription.getChordVoiceLabels();
  	int lowestNoteIndex = 0;
  	for (int i = 0; i < transChords.size(); i++) {
  		List<Integer> pitchesInChord = FeatureGenerator.getPitchesInChord(null, basicNoteProperties, lowestNoteIndex);
  		List<List<Double>> currentChordVoiceLabels = chordVoiceLabels.get(i);
			List<List<Integer>> voicesInChord = DataConverter.getVoicesInChord(currentChordVoiceLabels);
  	  List<List<Integer>> pAndV = Transcription.getAllPitchesAndVoicesInChord(basicNoteProperties, pitchesInChord, 
  	  	voicesInChord, allVoiceLabels, lowestNoteIndex);
  	  List<Integer> currentPitchesInChord = pAndV.get(0);
  	  // currentVoicesInChord must be a List<List>>
			List<List<Integer>> currentVoicesInChord = new ArrayList<List<Integer>>();
			for (int j : pAndV.get(1)) {
				int currentVoice = j;
				List<Integer> voiceWrapped = Arrays.asList(new Integer[]{currentVoice});
				currentVoicesInChord.add(voiceWrapped);
			}
  	  List<List<Integer>> voiceCrossingInfo = 
  		Transcription.getVoiceCrossingInformationInChord(currentPitchesInChord, currentVoicesInChord);
  	  if ((voiceCrossingInfo.get(1).size() / 2) > maxNumVoiceCrossingPairs) {
  	  	int noteIndex = -1;
  	  	for (int j = 0; j < basicNoteProperties.length; j++) {
  	  		if (basicNoteProperties[j][Transcription.CHORD_SEQ_NUM] == i) {
  	  			noteIndex = j;
  	  		}
  	  	}
  	  	Rational onsetTime = new Rational(basicNoteProperties[noteIndex][Transcription.ONSET_TIME_NUMER],
  	  		basicNoteProperties[noteIndex][Transcription.ONSET_TIME_DENOM]);	
  	  	Rational[] metricPos = Tablature.getMetricPosition(onsetTime, meterInfo);
  	    System.out.println("More than " + maxNumVoiceCrossingPairs + " voice crossing pair(s) in chord " + i + " (bar " + metricPos[0].getNumer() + 
  	      " " + metricPos[1] + ")");	
  	  }
  	  lowestNoteIndex += transChords.get(i).size();
  	}
  }
 
  
  /**
   * Checks for all the given pieces how many chords there are between each chord and the next chord at a
   * semibreve distance.
   * 
   * @param pieceNames
   * @return
   */
  private static Integer[] hasChordsAtGivenDistances(List<String> pieceNames) {
  	Integer[] result = new Integer[33];
  	
  	int numberOfChords = 0;
//  	int numberOfChordsWithChordsAtGivenDistances = 0;
  	
    Integer[] chordsBetween = new Integer[32];
    Arrays.fill(chordsBetween, 0);      
  	
  	for (int i = 0; i < pieceNames.size(); i++) {
    	String pieceName = pieceNames.get(i);
    	File tablatureEncoding = new File(Runner.encodingsPath + pieceName + ".tbp");
	    Tablature tablature = new Tablature(tablatureEncoding, false);
//      preprocessor.prepareInitialInformation(tablature, null, true);
      Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
      List<List<TabSymbol>> tablatureChords = tablature.getTablatureChords();
      
      // For each chord
      int lowestNoteIndex = 0;
      for (int j = 0; j < tablatureChords.size() - 1; j++) {      	
      	numberOfChords++;     	
      	int numberOfChordsBetween = 0;
      	      	
       	int currentOnsetTime = basicTabSymbolProperties[lowestNoteIndex][Tablature.ONSET_TIME];
       	// For the next chords within a semibreve
       	for (int k = j + 1; k < tablatureChords.size(); k++) {
       	  // Find onset time of chord at index k
       		int nextOnsetTime = 0;
       		for (int l = 0; l < basicTabSymbolProperties.length; l++) {
       			if (basicTabSymbolProperties[l][Tablature.CHORD_SEQ_NUM] == k) {
       				nextOnsetTime = basicTabSymbolProperties[l][Tablature.ONSET_TIME];
       				break;
       			}
       		}
       	  if (nextOnsetTime > currentOnsetTime && nextOnsetTime < (currentOnsetTime + 16)) {
       	  	numberOfChordsBetween++;
       	  }
       	  else if (nextOnsetTime >= currentOnsetTime + 16) {
       	  	break;
       	  }
       	}
       	
       	chordsBetween[numberOfChordsBetween]++;
       	
        int currentChordSize = basicTabSymbolProperties[lowestNoteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
       	lowestNoteIndex += currentChordSize;
      }
    }
  	
  	result[0] = numberOfChords;
  	for (int i = 0; i < 32; i++) {
  		result[i + 1] = chordsBetween[i];
  	}
  	return result;
  }
  
}

package machineLearning;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import representations.Tablature;
import representations.Transcription;

public class TablatureTranscriptionPair {
	Tablature tablature;
	Transcription transcription;
	Transcription secondTranscription;


	public TablatureTranscriptionPair(Tablature argTablature, Transcription argTranscription) {
		tablature = argTablature;
		transcription = argTranscription;
	}


	public TablatureTranscriptionPair(Tablature argTablature, Transcription predictedTranscription, Transcription 
		groundTruthTranscription) {
		tablature = argTablature;
		transcription = predictedTranscription;
		secondTranscription = groundTruthTranscription;
	}
	
	
//	public TablatureTranscriptionPair(String pieceName) {
//	  File encodingFile = new File(ExperimentRunner.pathPrefix + "Encodings/Dataset/" + pieceName + ".txt");
//    File midiFile = new File(ExperimentRunner.pathPrefix + "MIDI/Dataset/" + pieceName);
//    tablature = new Tablature(encodingFile);
//    transcription = new Transcription(midiFile, encodingFile);
//	}


	/**
	 * Creates a TablatureTranscriptionPair for the tablature case.
	 * 
	 * @param pieceName
	 * @param tabEncodingsFolder
	 * @param midiFilesFolder
	 */
	public TablatureTranscriptionPair(String pieceName, String tabEncodingsFolder, String midiFilesFolder) {
		File encodingFile = new File(tabEncodingsFolder + pieceName + ".tbp");
		File midiFile = new File(midiFilesFolder + pieceName);
		tablature = new Tablature(encodingFile, true);
		transcription = new Transcription(midiFile, encodingFile);
	}
	
	
	public int getNumberOfNotes() {
		if (tablature != null) {
			return tablature.getBasicTabSymbolProperties().length;
		}
		else {
			return transcription.getBasicNoteProperties().length;
		}
	}
	
	
	public String getPieceName() {
		if (tablature != null) {
			return tablature.getPiecename();
		}
		else {
			return transcription.getPieceName();
		}
	}


	/**
	 * Creates a set of TablatureTranscriptionPairs using the piece names in the given List. The tablature 
	 * tuning is normalised to G.
	 * 
	 * @param pieceNames
	 */
	private static List<TablatureTranscriptionPair> createSetOfTablatureTranscriptionPairs(List<String> pieceNames,
		String tabEncodingsFolder, String midiFilesFolder, boolean isTablatureCase) {
		List<TablatureTranscriptionPair> allPairs = new ArrayList<TablatureTranscriptionPair>();
		for (String pieceName : pieceNames) {    
			File encodingFile = null;
			File midiFile = new File(midiFilesFolder + pieceName + ".mid");
			Tablature tab = null;

			// a. In the tablature case
			if (isTablatureCase) {
				encodingFile =	new File(tabEncodingsFolder + pieceName + ".tbp");
//				new File(ExperimentRunner.pathPrefix + ExperimentRunner.encodingsPath + pieceName + ".txt");				
//				new File(ExperimentRunner.pathPrefix + ExperimentRunner.tabMidiPath + pieceName);

//				Tablature tablature = new Tablature(encodingFile, true);
				tab = new Tablature(encodingFile, true);
//				Transcription transcription = new Transcription(midiFile, encodingFile);
//				trans = new Transcription(midiFile, encodingFile);
//				allPairs.add(new TablatureTranscriptionPair(tab, trans));
			}
			// b. In the non-tablature case
//			else {
//				trans = new Transcription(midiFile, encodingFile);
//			}
			Transcription trans = new Transcription(midiFile, encodingFile);
			allPairs.add(new TablatureTranscriptionPair(tab, trans));
		}
		return allPairs;
	}


	public Tablature setTablature(Tablature tablature) {
		return tablature;
	}


	public Transcription setTranscription() {
		return transcription;
	}


	public Tablature getTablature() {
		return tablature;
	}


	public Transcription getTranscription() {
		return transcription;
	}


	public Transcription getSecondTranscription() {
		return secondTranscription;
	}

}

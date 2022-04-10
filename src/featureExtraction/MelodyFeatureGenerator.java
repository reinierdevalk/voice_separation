package featureExtraction;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import n_grams.KylmModel;
import representations.Tablature;
import representations.Transcription;
import tools.ToolBox;
import ui.Runner;
import de.uos.fmt.musitech.data.score.NotationSystem;
import de.uos.fmt.musitech.data.score.NotationVoice;
import de.uos.fmt.musitech.data.structure.Note;
import de.uos.fmt.musitech.utility.math.Rational;

public class MelodyFeatureGenerator {

//	private FeatureGenerator fg;
	
//	public MelodyFeatureGenerator(FeatureGenerator argFg) {
//		fg = argFg;
//	}


	public static enum MelodyModelFeature {
		PITCH(KylmModel.PITCH), DUR(KylmModel.DUR), 
		REL_PITCH(KylmModel.REL_PITCH),	IOI(KylmModel.IOI);
		
		private int sliceIndex;
		MelodyModelFeature(int si) {
			this.sliceIndex = si;
		}
		
		public int getSliceIndex() {
			return sliceIndex;
		}
	}


	public static List<Integer> getSliceIndices(List<MelodyModelFeature> mmfs) {
		if (mmfs == null || mmfs.size() == 0) {
			return null;
		}
		else {
			List<Integer> res = new ArrayList<Integer>();
			for (MelodyModelFeature mmf : mmfs) {
				res.add(mmf.getSliceIndex());
			}
			return res;
		}
	}


	public MelodyFeatureGenerator() {
		
	}


	public static void main(String[] args) {	

//		MelodyFeatureGenerator mfg = new MelodyFeatureGenerator(new FeatureGenerator()); 
		MelodyFeatureGenerator mfg = new MelodyFeatureGenerator();
		List<String> pieceNames = null; //Dataset.getTabThreeVoices();
		String set = "intab_3vv";  	
//		List<String> pieceNames = Dataset.getTabFourVoices();
//		String set = "intab_4vv";
//		List<String> pieceNames = Dataset.getBachThreeVoiceFugues();
//		String set = "fugues_3vv";
//		List<String> pieceNames = Dataset.getBachFourVoiceFugues();
//		String set = "fugues_4vv";

		for (String s : pieceNames) {		
			Integer[][] btp = null;
			File encoding = null;
			File midi = null;
			if (!pieceNames.get(0).contains("WTC")) {
				encoding = new File(Runner.encodingsPath + "intabulations/" + s);
				midi = new File(Runner.midiPath + "intabulations/" + s);
//				Tablature tab = new Tablature(encoding);
//				btp = tab.getBasicTabSymbolProperties();
			}
			else {
				midi = new File(Runner.midiPath + "bach-WTC/thesis/" + s);
			}
			Transcription trans = new Transcription(midi, encoding);
			NotationSystem system = trans.getPiece().getScore();

			for (int voice = 0; voice < trans.getNumberOfVoices(); voice++) {
				NotationVoice nv = system.get(voice).get(0);
//				List<List<Double>> features = mfg.getMelodyModelFeatureVectors(btp, nv, null);
				List<List<Double>> features = new FeatureGenerator().generateMelodyFeatureVectors(btp, nv, null);
				ToolBox.storeObject(features, new File("F:/research/data/melody_feat/" + 
					set + "/" + s + " (voice " + voice + ").xml"));
			}
		} 	
		System.exit(0);
	}


	/**
	 * Gets, for every note in the given voice, a feature vector size 2 + 2*n, containing
	 *   (0) the pitch of the note
	 *   (1) the duration of the note (in whole notes); in the tablature case this is the minimum duration
	 * and, for all n previous notes in the voice,
	 *   (2) the relative pitch to the previous note (in semitones, negative if the previous note is higher)
	 *   (3) the inter-onset interval to the previous note (in whole notes)
	 *   
	 * The features are calculated only for the notes whose onset time < metricTime. If metricTime is <code>null</code>,
	 * the features are calculated for every note in the voice.  
	 *       
	 * @param basicTabSymbolProperties      
	 * @param transcription
	 * @param voice
	 * @param n
	 * @param metricTime
	 * @return 
	 */
	// TESTED for both tablature- and non-tablature case
	List<List<Double>> getMelodyModelFeatureVectors(Integer[][] basicTabSymbolProperties, Transcription transcription, 
		int voice, int n, Rational metricTime) {
		List<List<Double>> featureVector = new ArrayList<List<Double>>();

		NotationSystem system = transcription.getPiece().getScore();
		NotationVoice nv = system.get(voice).get(0);
		// For each Note in the voice
		for (int i = 0; i < nv.size(); i++) {
			List<Double> current = new ArrayList<Double>();
			Note currentNote = nv.get(i).get(0);
			int currentPitch = currentNote.getMidiPitch();
			Rational currentMetricTime = currentNote.getMetricTime();

			if (metricTime == null || metricTime != null && currentMetricTime.isLess(metricTime)) {
				// 1. Pitch
				current.add((double) currentPitch);
				// 2. Duration
				// a. In the tablature case
				if (basicTabSymbolProperties != null) {
					int currentMetricTimeAsInt = 
						currentMetricTime.getNumer() * (Tablature.SRV_DEN / currentMetricTime.getDenom());
					// Find the minimum duration of the note that has currentMetricTimeAsInt and currentPitch as btp
					for (Integer[] currentBtp : basicTabSymbolProperties) {
						if (currentBtp[Tablature.ONSET_TIME] == currentMetricTimeAsInt &&	currentBtp[Tablature.PITCH] == currentPitch) {
							Rational minDuration = 
								new Rational(currentBtp[Tablature.MIN_DURATION], Tablature.SRV_DEN); 
							current.add(minDuration.toDouble());
						}
					}
				}
				// b. In the non-tablature case
				else {
					current.add(currentNote.getMetricDuration().toDouble());
				}
				// For all n previous notes
				for (int j = 1; j <= n; j++) {
					int previousIndex = i - j;
					// If there is a previous note
					if (previousIndex >= 0) {
						Note previousNote = nv.get(previousIndex).get(0); 
						// 3. Relative pitch
						current.add((double) (currentPitch - previousNote.getMidiPitch()));
						// 4. Inter-onset interval
						current.add(currentMetricTime.sub(previousNote.getMetricTime()).toDouble());
					}
					// If not
					else {
						current.addAll(Arrays.asList(new Double[]{0.0, -1.0}));
					}
				}
				featureVector.add(current);
			}
			else {
				break;
			}
		}
		return featureVector;
	}


//	/**
//	 * Gets for the given Note in the given NotationVoice the melody feature vector, containing
//	 *   (0) the pitch of the Note
//	 *   (1) the duration of the Note (in whole notes); in the tablature case this is the minimum duration
//	 *   (2) the relative pitch (in semitones, negative if the previous Note is higher) to the previous Note
//	 *   (3) the inter-onset interval (in whole notes) to the previous Note
//	 *   
//	 * If there is no previous Note in the given NotationVoice, relative pitch and inter-onset interval are set to 0.0
//	 * and -1.0, respectively.
//	 *       
//	 * @param basicTabSymbolProperties      
//	 * @param notationVoice
//	 * @param note
//	 * @return
//	 */
//	// TESTED for both tablature- and non-tablature case
//	List<Double> generateMelodyFeatureVector(Integer[][] basicTabSymbolProperties, NotationVoice notationVoice, Note note) {
//		List<Double> mfv = new ArrayList<Double>();
//		
//		// Get the pitch, onset time, and duration of note 
//		int pitch = note.getMidiPitch();
//		Rational metricTime = note.getMetricTime();
//		Rational metricDuration = null;
//		// a. In the tablature case: the minimum duration
//		if (basicTabSymbolProperties != null) {
//			metricDuration = Tablature.getMinimumDurationOfNote(basicTabSymbolProperties, note);
//		}
//		// b. In the non-tablature case: the full duration
//		else {
//			metricDuration = note.getMetricDuration();
//		}
//
//		// 2. Fill mfv
//		// Add pitch and duration of the current note
//		mfv.add((double) pitch);
//		mfv.add(metricDuration.toDouble());
//		// Add relative pitch and ioi to the previous note
//		FeatureGenerator fg = new FeatureGenerator();
//		Note previousNote = fg.getAdjacentNoteInVoice(notationVoice, note, Direction.LEFT);
//		if (previousNote != null) {
//			mfv.add((double) (pitch - previousNote.getMidiPitch()));
//			mfv.add(metricTime.sub(previousNote.getMetricTime()).toDouble());
//		}
//		else {
//			mfv.addAll(Arrays.asList(new Double[]{0.0, -1.0}));
//		}
//
//		return mfv;
//	}
	
	
//	/**
//	 * Gets the melody feature vectors in the given NotationVoice. There are two options:
//	 * 1. If note is <code>null</code>: returns the mfv for all notes in the given NotationVoice; 
//	 * 2. If note is not <code>null</code>: returns the mfv for all Notes (if any) in the given NotationVoice whose 
//	 *    metric time is smaller than the metric time of the given Note, plus the melody feature vector for the given
//	 *    Note, as if it were also in the given NotationVoice.
//	 *     
//	 * @param basicTabSymbolProperties
//	 * @param notationVoice
//	 * @param note
//	 * @return
//	 */
//	// TESTED for both tablature- and non-tablature case
//	public List<List<Double>> generateMelodyFeatureVectors(Integer[][] basicTabSymbolProperties, NotationVoice notationVoice,
//		Note note, FeatureGenerator fg) {
//		List<List<Double>> allMfv = new ArrayList<List<Double>>();
//		
//		
//		// All mfv
//		if (note == null) {
//			for (int i = 0; i < notationVoice.size(); i++) {				
//				Note n = notationVoice.get(i).get(0);
//				allMfv.add(generateMelodyFeatureVector(basicTabSymbolProperties, notationVoice, n));
//			}
//		}		
//		// All mfv up to note
//		else  {
//  		// 0. Get the pitch, onset time, and duration of note
//  		int pitch = note.getMidiPitch();
//  		Rational metricTime = note.getMetricTime(); 
//  		Rational metricDuration = null;
//  	  // a. In the tablature case
//  		if (basicTabSymbolProperties != null) {
//  			metricDuration = Tablature.getMinimumDurationOfNote(basicTabSymbolProperties, note);
//  		}
//  		// b. In the non-tablature case
//  		else {
//  			metricDuration = note.getMetricDuration();
//  		}
//  		
//  		// 1. If notationVoice is not empty: create the mfvs for all notes before note (if any) and add them to allMfv
//  		if (notationVoice.size() != 0) { 
//  			for (int i = 0; i < notationVoice.size(); i++) {
//  				Note previousNote = notationVoice.get(i).get(0);
//  				// Only if previousNote is before the note at noteIndex
//  				if (previousNote.getMetricTime().isLess(metricTime)) {
//  					allMfv.add(generateMelodyFeatureVector(basicTabSymbolProperties, notationVoice, previousNote));
//  				}
//  				else {
//  					break;
//  				}		
//  			}
//  		}
//  		// 2. Create the mfv for note, assuming that it is in voice
//  		List<Double> mfv = new ArrayList<Double>();
//  		mfv.add((double) pitch);
//  		mfv.add(metricDuration.toDouble());
//  		// a. If there are no Notes before note: add default values
//  		if (allMfv.size() == 0) {
//  			mfv.addAll(Arrays.asList(new Double[]{0.0, -1.0}));
//  		}
//  		// b. If there are Notes before note
//  		else {
//  			Note previousNote = fg.getAdjacentNoteInVoice(notationVoice, note, Direction.LEFT);
//  			mfv.add((double) (pitch - previousNote.getMidiPitch()));
//  		  mfv.add(metricTime.sub(previousNote.getMetricTime()).toDouble());	
//  		}
//  		
//  		// 3. Add mfv to allMfv
//  		allMfv.add(mfv);
//		}
//		
//		return allMfv;
//	}
	
}

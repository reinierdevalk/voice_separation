package featureExtraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.uos.fmt.musitech.data.score.NotationSystem;
import de.uos.fmt.musitech.data.score.NotationVoice;
import de.uos.fmt.musitech.data.structure.Note;
import de.uos.fmt.musitech.data.structure.container.NoteSequence;
import de.uos.fmt.musitech.utility.math.Rational;
import machineLearning.MelodyPredictor;
import representations.Tablature;
import representations.Transcription;
import tbp.RhythmSymbol;
import tools.ToolBox;
import ui.Runner;
import ui.Runner.FeatureVector;
import ui.Runner.Model;
import ui.Runner.ModellingApproach;
import ui.Runner.ProcessingMode;
import utility.DataConverter;

public class FeatureGenerator {

//	public List<Double[]> allVoiceProb = new ArrayList<Double[]>();

	public static final int FEATURE_SET_A = 0; // TODO remove
	public static final int FEATURE_SET_B = 1; // TODO remove
	public static final int FEATURE_SET_C = 2; // TODO remove
	public static final int FEATURE_SET_D = 3; // TODO remove

	public static int featureSetSwitch = FEATURE_SET_D; // TODO remove
	public static boolean tabInfoSwitch = true; // TODO remove

	static final int PITCH = 0;
	static final int COURSE = 1;
	static final int FRET = 2;
	static final int MIN_DURATION = 3;
	static final int MAX_DURATION = 4;
	static final int IS_ORNAMENTATION = 5;
	static final int POSITION_WITHIN_BAR = 6;
	static final int INFO_AHEAD = 7;
	static final int SIZE = 8;

	static final int PITCH_NON_TAB = 0;
	static final int DURATION_NON_TAB = 1;
	static final int IS_ORNAMENTATION_NON_TAB = 2;
	static final int POSITION_WITHIN_BAR_NON_TAB = 3;
	static final int INFO_AHEAD_NON_TAB = 4;
	static final int SIZE_NON_TAB = 3;

	public static enum Direction {LEFT, RIGHT};
	
//	public static int windowSize;

//	private DataConverter dataConverter = new DataConverterTab();
//	private MelodyFeatureGenerator melodyFeatureGenerator = new MelodyFeatureGenerator(); // gives stackOverflowError because each new MelodyFeatureGenerator creates a new FeatureGenerator, etc. 
//	private MelodyPredictor melodyPredictor; // = new MelodyPredictor(ModelType.SIMPLE_LM, SimpleLM.Type.SHORT); 

//	private static boolean modelDuration;
//	private static boolean isBidirectional;
	public static final int NUM_NEXT_CHORDS = 3;
	public static final int NUM_FEATURES_HL_ABC_TAB = 41;
	public static final int NUM_FEATURES_HL_ABC_NON_TAB = 33;


//	private static void setModelDuration(boolean arg) {
//		modelDuration = arg;
//	}
	
//	private static void setIsBidirectional(boolean arg) {
//		isBidirectional = arg;
//	}


	// =================================== FEATURE EXTRACTION ===================================  
	// tabFeatures
	/**
	 * Gets the basic features of the note at noteIndex. Returns a double[] containing
	 *   in the tablature case:
	 *     as element 0:     the pitch (as a MIDInumber) of the tablature note;
	 *     as element 1:     the course the tablature note is on;
	 *     as element 2:     the fret the tablature note is on;
	 *     as element 3:     the minimum duration (as given in the tablature, in whole notes) of the tablature note;
	 *     as element 4:     the maximum duration of the tablature note, i.e., the difference between its onset time and the
	 *                       onset time of the next tablature note on the same course. When the note is in the last chord, the
	 *                       maximum duration is equal to the chord's minimum duration;
	 *     as element 5:     whether the tablature note has ornamentation characteristics, which is so if it is the only onset
	 *                       (not necessarily the only note!) within the chord and has a duration of a 16th note or smaller.
	 *     as element 6:     the metric position of the tablature note within the bar;  
	 *     as elements 7-24: the onset time proximity, the pitch proximity and course information for the next 
	 *                       NUM_NEXT_CHORDS chords (where x = (6 + (NUM_NEXT_CHORDS * 6)) )
	 *     as element 25:    the number of new onsets in the next chord 
	 *   
	 *   in the non-tablature case:
	 *     as element 0:     the pitch (as a MIDInumber) of the note;
	 *     as element 1:     the full duration (in whole notes) of the note;
	 *     as element 2:     whether the note has ornamentation characteristics, which is so if it is the only onset (not 
	 *                       necessarily the only note!) within the chord and has a duration of a 16th note or smaller;
	 *     as element 3:     the metric position of the note within the bar;
	 *     as elements 4-15: the onset time proximity and pitch proximity information for the next NUM_NEXT_CHORDS
	 *                       chords (where x = (3 + (NUM_NEXT_CHORDS*4)) ) 
	 *     as element 16:    the number of new onsets in the next chord
	 *     
	 * @param btp Must be <code>null</code> in the non-tablature case
	 * @param bnp Must be <code>null</code> in the tablature case
	 * @param meterInfo
	 * @param noteIndex
	 * 
	 * @return
	 */
	// TESTED for both tablature- and non-tablature case; for fwd (= bwd) model 
	static double[] getBasicNoteFeatures(Integer[][] btp, Integer[][] bnp,	List<Integer[]> meterInfo, int noteIndex) {

		Transcription.verifyCase(btp, bnp);	  
		double[] basicNoteFeatures = null;

		// a. In the tablature case
		if (btp != null) {
			basicNoteFeatures = new double[7 + (NUM_NEXT_CHORDS * 6) + 1];
			// 0. The pitch (as a MIDI number) of the note
			basicNoteFeatures[PITCH] = btp[noteIndex][Tablature.PITCH];
			// 1. The course the onset is on
			basicNoteFeatures[COURSE] = btp[noteIndex][Tablature.COURSE];
			// 2. The fret the onset is on 
			basicNoteFeatures[FRET] = btp[noteIndex][Tablature.FRET];
			// 3. The minimum duration (in whole notes) of the note 
			basicNoteFeatures[MIN_DURATION] = 
				(double) btp[noteIndex][Tablature.MIN_DURATION] / Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom();
			// 4. The maximum duration (in whole notes) of the note
			basicNoteFeatures[MAX_DURATION] = 
				(double) btp[noteIndex][Tablature.MAX_DURATION] / Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom();
			// 5. Whether the note has ornamentation characteristics, yes or no
			basicNoteFeatures[IS_ORNAMENTATION] = 0.0;
			if (btp[noteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS] == 1 && 
				btp[noteIndex][Tablature.MIN_DURATION] < RhythmSymbol.semiminim.getDuration()) {
				basicNoteFeatures[IS_ORNAMENTATION]= 1.0;
			}
			// 6. The metric position of the note within the bar		  
			Rational metricTime = new Rational(btp[noteIndex][Tablature.ONSET_TIME],
				Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());	
			Rational[] metricPosition = Tablature.getMetricPosition(metricTime, meterInfo);
			basicNoteFeatures[POSITION_WITHIN_BAR] = 
				(double) metricPosition[1].getNumer() / metricPosition[1].getDenom();
			// 7-24. The onset time proximity, the pitch proximity and course information for the next NUM_NEXT_CHORDS chords
			double[] infoAhead = getProximitiesAndCourseInfoAhead(btp, bnp,
				noteIndex, NUM_NEXT_CHORDS);
			for (int i = 0; i < infoAhead.length; i++) {
				basicNoteFeatures[INFO_AHEAD + i] = infoAhead[i]; 
			}
			// 25. The number of new onsets in the next chord
			basicNoteFeatures[INFO_AHEAD + (NUM_NEXT_CHORDS * 6)] = 
				getNumberOfNewOnsetsInNextChord(btp, bnp, noteIndex);
		}
		// b. In the non-tablature case
		else if (bnp != null) {
			basicNoteFeatures = new double[4 + (NUM_NEXT_CHORDS * 4) + 1];
//			basicNoteFeatures = new double[4];
			// 0. The pitch (as a MIDI number) of the Note
			basicNoteFeatures[PITCH_NON_TAB] = bnp[noteIndex][Transcription.PITCH];
			// 1. The duration (in whole notes) of the Note
			double duration = (double) bnp[noteIndex][Transcription.DUR_NUMER] / 
				bnp[noteIndex][Transcription.DUR_DENOM]	;
			basicNoteFeatures[DURATION_NON_TAB] = duration; 
			// 2. Whether the Note has ornamentation characteristics, yes or no
			// NB: MIDI can be imprecise, i.e., the notes' actual MIDI onset- and offset times may be a little before or  
			// after the notated (from the score) onset- and offset times. It is therefore safer to choose <= 16th than
			// < 1/8 here, as the latter will count single notes whose duration is slightly under 1/8 because of 
			// fluctuation in the MIDI as ornamental notes 
			basicNoteFeatures[IS_ORNAMENTATION_NON_TAB] = 0.0;
			if (bnp[noteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS] == 1 && duration <= 1/16.0) {
				basicNoteFeatures[IS_ORNAMENTATION_NON_TAB]= 1.0;
			}
			// 3. The metric position of the note within the bar
			Rational metricTime = new Rational(bnp[noteIndex][Transcription.ONSET_TIME_NUMER],
				bnp[noteIndex][Transcription.ONSET_TIME_DENOM]);	
			Rational[] metricPosition = Tablature.getMetricPosition(metricTime, meterInfo);
			basicNoteFeatures[POSITION_WITHIN_BAR_NON_TAB] = 
				(double) metricPosition[1].getNumer() / metricPosition[1].getDenom();
			// 4-15. The onset time proximity and the pitch proximity information for the next NUM_NEXT_CHORDS chords
			double[] infoAhead = getProximitiesAndCourseInfoAhead(btp, bnp, noteIndex, NUM_NEXT_CHORDS);
			for (int i = 0; i < infoAhead.length; i++) {
				basicNoteFeatures[INFO_AHEAD_NON_TAB + i] = infoAhead[i]; 
			}
			// 16. The number of new onsets in the next chord
			basicNoteFeatures[INFO_AHEAD_NON_TAB + (NUM_NEXT_CHORDS * 4)] = 
				getNumberOfNewOnsetsInNextChord(btp, bnp, noteIndex);
		}
		return basicNoteFeatures;
	}


	/**
	 * Checks the following x chords (where x is determined by the argument numNextChords) and returns a double[]
	 * containing
	 *   in the tablature case:
	 *     0.   The inter-onset time proximity between the chord the note at noteIndex is in and the following chord;
	 *     1-2. The pitch proximity and the course for the note whose pitch is directly below that of the note at 
	 *          noteIndex; 
	 *     3.   The course for the note whose pitch is the same as that of the note at noteIndex; 
	 *     4-5. The pitch proximity and the course for the note whose pitch is directly above that of the note at 
	 *          noteIndex.  
	 *   in the non-tablature case:
	 *     0. The inter-onset time proximity between the chord the note at noteIndex is in and the following chord;
	 *     1. The pitch proximity for the note whose pitch is directly below that of the note at noteIndex; 
	 *     2. Whether there is a note whose pitch is the same as that of the note at noteIndex; 
	 *     3. The pitch proximity for the note whose pitch is directly above that of the note at noteIndex.  
	 * 
	 * If:
	 *   a) any of these notes does not exist, default values of -1.0 will be returned; 
	 *   b) any of these notes is part of a unison: in the tablature case, the course of the unison note whose
	 *      course is closest to that of the note at noteIndex is listed   
	 *   c) there are fewer than numNextChords remaining (the final chord will be numNextChords short of numNextChords,
	 *      the penultimate chord numNextChords - 1, etc.), default values of -1.0 are returned for each missing chord.
	 *       
	 * @param btp
	 * @param noteIndex
	 * @param numNextChords
	 * @return
	 */
	// TESTED for both tablature- and non-tablature case
	static double[] getProximitiesAndCourseInfoAhead(Integer[][]btp, Integer[][] bnp, int noteIndex, int numNextChords) {

		Transcription.verifyCase(btp, bnp);

		// 0. Initialise pitchProxAndCourse and variables
		double[] pitchProxAndCourse = null;
		int numNotes = -1;
		int numChords = -1;
		int pitchCurrentNote = -1;
		int courseCurrentNote = -1;
		Rational onsetTimeCurrentNote = null;
		int chordSeqNumCurrentNote = -1;
		// a. In the tablature case
		if (btp != null) {
			pitchProxAndCourse = new double[6 * numNextChords];
			pitchCurrentNote = btp[noteIndex][Tablature.PITCH];
			courseCurrentNote = btp[noteIndex][Tablature.COURSE];
			onsetTimeCurrentNote = new Rational(btp[noteIndex][Tablature.ONSET_TIME],
				Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
			chordSeqNumCurrentNote = btp[noteIndex][Tablature.CHORD_SEQ_NUM];
			numNotes = btp.length;
			numChords = btp[numNotes - 1][Tablature.CHORD_SEQ_NUM] + 1;
		}
		// b. In the non-tablature case
		if (bnp != null) {
			pitchProxAndCourse = new double[4 * numNextChords];
			pitchCurrentNote = bnp[noteIndex][Transcription.PITCH];
			onsetTimeCurrentNote = new Rational(bnp[noteIndex][Transcription.ONSET_TIME_NUMER],
				bnp[noteIndex][Transcription.ONSET_TIME_DENOM]);
			chordSeqNumCurrentNote = bnp[noteIndex][Transcription.CHORD_SEQ_NUM];
			numNotes = bnp.length;
			numChords = bnp[numNotes - 1][Transcription.CHORD_SEQ_NUM] + 1;
		}
		Arrays.fill(pitchProxAndCourse, -1.0);

		// 1. Represent each of the numNextChords next chords as a List and add it to allNextChords
		List<List<List<Double>>> allNextChords = new ArrayList<List<List<Double>>>();
		for (int i = 1; i <= numNextChords; i++) {
			List<List<Double>> currentNextChord = new ArrayList<List<Double>>();
			int seqNumCurrentNextChord = chordSeqNumCurrentNote + i;
//			if (modelBackward) {
//				seqNumCurrentNextChord = chordSeqNumCurrentNote - i;
//			}
			int lowestNoteIndexNextChord = -1;
			int sizeNextChord = -1; 
			// Only if seqNumCurrentNextChord exists
			if (seqNumCurrentNextChord < numChords) {
//				if ((!modelBackward && seqNumCurrentNextChord < numChords) || (modelBackward && seqNumCurrentNextChord >= 0)) {
				// a. In the tablature case
				if (btp != null) {
					// Determine the index of the lowest note and the size of the next chord 
					for (int j = 0; j < btp.length; j++) {
						if (btp[j][Tablature.CHORD_SEQ_NUM] == seqNumCurrentNextChord) {
							lowestNoteIndexNextChord = j;
							sizeNextChord = btp[j][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
							break;
						}
					}
					// Represent the chord as a List of onset time-pitch-course-triplets
					int highestNoteIndexNextChord = lowestNoteIndexNextChord + (sizeNextChord - 1);
					for (int j = lowestNoteIndexNextChord; j <= highestNoteIndexNextChord; j++) {
						List<Double> currentRepresentation = new ArrayList<Double>();
						currentRepresentation.add((double) btp[j][Tablature.ONSET_TIME] / 
							Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
						currentRepresentation.add((double) btp[j][Tablature.PITCH]);
						currentRepresentation.add((double) btp[j][Tablature.COURSE]);
						currentNextChord.add(currentRepresentation);
					}
				}
				// b. In the non-tablature case
				if (bnp != null) {
					// Determine the index of the lowest note and the size of the next chord
					for (int j = 0; j < bnp.length; j++) {
						if (bnp[j][Transcription.CHORD_SEQ_NUM] == seqNumCurrentNextChord) {
							lowestNoteIndexNextChord = j;
							sizeNextChord = bnp[j][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
							break;
						}
					}	
					// Represent the chord as a List of onset time-pitch pairs
					int highestNoteIndexNextChord = lowestNoteIndexNextChord + (sizeNextChord - 1);
					for (int j = lowestNoteIndexNextChord; j <= highestNoteIndexNextChord; j++) {
						List<Double> currentRepresentation = new ArrayList<Double>();
						currentRepresentation.add((double) bnp[j][Transcription.ONSET_TIME_NUMER] / 
							bnp[j][Transcription.ONSET_TIME_DENOM]);
						currentRepresentation.add((double) bnp[j][Transcription.PITCH]);
						currentNextChord.add(currentRepresentation);
					}
				}
				allNextChords.add(currentNextChord);
			}
		}

		// 2. Set pitchProxAndCourse
		// For each next chord
		for (int i = 0; i < allNextChords.size(); i++) {
			List<List<Double>> currentNextChord = allNextChords.get(i);

			// Determine the inter-onset time between the note at noteIndex and the current next chord, as well as
			// the closest lower and closest higher pitch in the current next chord 
			double interOnsetTime = Math.abs(currentNextChord.get(0).get(0) - onsetTimeCurrentNote.toDouble());
			List<Double> pitchesInChord = new ArrayList<Double>();
			double closestLowerPitch = -Double.MAX_VALUE; // Changed 21-1-2015 from Double.MIN_VALUE; this could affect EM results
//			double closestLowerPitch = Double.MIN_VALUE;
			double closestHigherPitch = Double.MAX_VALUE;
			for (List<Double> l : currentNextChord) {
				pitchesInChord.add(l.get(1));
			}
			for (double pitch : pitchesInChord) {
				if (pitch > closestLowerPitch && pitch < pitchCurrentNote) {
					closestLowerPitch = pitch;
				}
				if (pitch < closestHigherPitch && pitch > pitchCurrentNote) {
					closestHigherPitch = pitch;
				} 
			}

			// For each note in the current next chord
			double courseDistLower = Double.MAX_VALUE;
			double courseDistSame = Double.MAX_VALUE;
			double courseDistHigher = Double.MAX_VALUE;
			for (List<Double> currentRepresentation : currentNextChord) {
				double pitchCurrentNextNote = currentRepresentation.get(1);
				int pitchDistToCurrentNextNote = Math.abs(pitchCurrentNote - (int) pitchCurrentNextNote); 
				// a. In the tablature case
				if (btp != null) {
					int arrayIndex = i * 6;
					// 1. Set the onset time proximity of the current next chord 
					pitchProxAndCourse[arrayIndex] = 1.0 / (interOnsetTime + 1);
					// 2. If the note is the note at the closest lower, same, or closest higher pitch: set pitch proximity
					// and course  
					// NB: If any of these pitches appears twice (i.e., is part of a unison), the "if" comparing the course 
					// distances ensures that the note at the shortest course distance is listed
					double courseCurrentNextNote = currentRepresentation.get(2);
					double currentCourseDist = Math.abs(courseCurrentNote - courseCurrentNextNote);
					// a. If pitchCurrentNextNote is the closest lower pitch
					if (pitchCurrentNextNote == closestLowerPitch) {
						if (currentCourseDist < courseDistLower) {
							courseDistLower = currentCourseDist;
							pitchProxAndCourse[arrayIndex + 1] = 1.0 / (pitchDistToCurrentNextNote + 1);
							pitchProxAndCourse[arrayIndex + 2] = courseCurrentNextNote;
						}
					}
					// b. If pitchCurrentNextNote is the same pitch  
					if (pitchCurrentNextNote == pitchCurrentNote) {
						if (currentCourseDist < courseDistSame) {
							courseDistSame = currentCourseDist;
							pitchProxAndCourse[arrayIndex + 3] = courseCurrentNextNote;
						}
					}
					// c. If pitchCurrentNextNote is the closest higher pitch
					if (pitchCurrentNextNote == closestHigherPitch) {  
						if (currentCourseDist < courseDistHigher) {
							courseDistHigher = currentCourseDist;
							pitchProxAndCourse[arrayIndex + 4] = 1.0 / (pitchDistToCurrentNextNote + 1);
							pitchProxAndCourse[arrayIndex + 5] = courseCurrentNextNote;
						}
					}
				}
				// b. In the non-tablature case
				if (bnp != null) {
					int arrayStartIndex = i * 4;
					// 1. Set the onset time proximity of the current next chord 
					pitchProxAndCourse[arrayStartIndex] = 1.0 / (interOnsetTime + 1);
					// 2. If the note is the note at the closest lower, same, or closest higher pitch: set pitch proximity
					// a. If pitchCurrentNextNote is the closest lower pitch
					if (pitchCurrentNextNote == closestLowerPitch) {
					 	pitchProxAndCourse[arrayStartIndex + 1] = 1.0 / (pitchDistToCurrentNextNote + 1);
					}
					// b. If pitchCurrentNextNote is the same pitch  
					if (pitchCurrentNextNote == pitchCurrentNote) {
						pitchProxAndCourse[arrayStartIndex + 2] = 1.0;
					}
					// c. If pitchCurrentNextNote is the closest higher pitch
					if (pitchCurrentNextNote == closestHigherPitch) { 
						pitchProxAndCourse[arrayStartIndex + 3] = 1.0 / (pitchDistToCurrentNextNote + 1);
					}
				}
			}
		}
		return pitchProxAndCourse;	
	}


	/**
	 * Gets the number of new onsets in the chord after the chord the note at noteIndex is in. Returns -1
	 * if there is no next chord.
	 *  
	 * @param btp
	 * @param noteIndex
	 * @return
	 */
	// TESTED for both tablature- and non-tablature case
	static double getNumberOfNewOnsetsInNextChord(Integer[][] btp, Integer[][] bnp, int noteIndex) {

		Transcription.verifyCase(btp, bnp);

		double numNewNotesInNextChord = -1.0;

		int chordIndex = 0;
		int numberOfNotes = 0;
		int numberOfChords = 0; 
		// a. In the tablature case
		if (btp != null) {
			chordIndex = btp[noteIndex][Tablature.CHORD_SEQ_NUM];
			numberOfNotes = btp.length;
			numberOfChords = btp[numberOfNotes - 1][Tablature.CHORD_SEQ_NUM];
		}
		// b. In the non-tablature case
		else {
			chordIndex = bnp[noteIndex][Transcription.CHORD_SEQ_NUM];
			numberOfNotes = bnp.length;
			numberOfChords = bnp[numberOfNotes - 1][Transcription.CHORD_SEQ_NUM];
		}

		// Do only if the note at noteIndex is not in the last chord
		if (chordIndex != numberOfChords) {
			int indexNextChord = chordIndex + 1;	
			// a. In the tablature case 
			if (btp != null) {
				for (int i = 0; i < btp.length; i++) {	
					if (btp[i][Tablature.CHORD_SEQ_NUM] == indexNextChord) {
						numNewNotesInNextChord = btp[i][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
						break;
					}
				}
			}
			// b. In the non-tablature case
			else {
				for (int i = 0; i < bnp.length; i++) {
					if (bnp[i][Transcription.CHORD_SEQ_NUM] == indexNextChord) {
						numNewNotesInNextChord = bnp[i][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
						break;
					}
				}
			}
		}
		return numNewNotesInNextChord;
	}


	// tabDurFeatures	
	/**     
	 * Gets the position of the note within the chord. Sustained previous notes are included only when modelDuration is
	 * set to <code>true</code> or in the non-tablature case. Returns a double[] containing
	 *   as element 0:     the size of the chord;
	 *   as element 1:     the index (based on pitch) of the note within the chord. Unisons within the chord get the 
	 *                     same index; when modelling duration, only sustained notes lower in pitch than the note at 
	 *                     noteIndex are counted;
	 *   as element 2-3:   the pitch distances to the note below and above the note at noteIndex (ignoring unison
	 *                     notes). Set to -1.0 if either or both are absent;
	 *   as element 4-7:   the intervals in the chord; set to -1.0 for each "missing" interval; 
	 *   
	 * @param btp Must be <code>null</code> in the non-tablature case
	 * @param durationLabels
	 * @param bnp Must be <code>null</code> in the tablature case
	 * @param direction
	 * @param noteIndex
	 * @return
	 */
	// TESTED for both tablature- (non-dur and dur) and non-tablature case; for fwd (= bwd) model
	static double[] getPositionWithinChord(Integer[][] btp, List<List<Double>> durationLabels,	Integer[][] bnp,
		Direction direction, int noteIndex, boolean modelDuration, boolean isBidirectional ) {

		Transcription.verifyCase(btp, bnp);

		double[] valuesExcl = new double[4 + (Transcription.MAXIMUM_NUMBER_OF_VOICES - 1)];
		Arrays.fill(valuesExcl, -1.0);
		double[] valuesIncl = new double[4 + (Transcription.MAXIMUM_NUMBER_OF_VOICES - 1)];
		Arrays.fill(valuesIncl, -1.0);

		final int CHORD_SIZE = 0;
		final int INDEX = 1;
		final int PITCH_DIST_BELOW = 2;
		final int PITCH_DIST_ABOVE = 3;
		final int FIRST_INTERVAL = 4;

		// 0. Determine variables
		int	pitchOfCurrentNote = -1;
		int seqNumInChordOfCurrentNote = -1;
		int lowestNoteIndex;
		int numNewOnsets = -1;		
		// a. In the tablature case
		if (btp != null) {  
		 	pitchOfCurrentNote = btp[noteIndex][Tablature.PITCH];
		 	seqNumInChordOfCurrentNote = btp[noteIndex][Tablature.NOTE_SEQ_NUM];
		 	numNewOnsets = btp[noteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
		}
		// b. In the non-tablature case
		if (bnp != null) {  
			pitchOfCurrentNote = bnp[noteIndex][Transcription.PITCH];
			seqNumInChordOfCurrentNote = bnp[noteIndex][Transcription.NOTE_SEQ_NUM];
			numNewOnsets = bnp[noteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
		}
		lowestNoteIndex = noteIndex - seqNumInChordOfCurrentNote;  	

		// 1. Determine the size of the chord
		// a. Excluding any sustained previous notes
		valuesExcl[CHORD_SIZE] = numNewOnsets;
		// b. Including any sustained previous notes
		if ((btp != null && !isBidirectional && direction == Direction.LEFT && modelDuration) || 
			(btp != null && isBidirectional && modelDuration) || bnp != null) { // EEND +  && modelDuration
//			if ((basicTabSymbolProperties != null && !modelBackward && modelDuration) || basicNoteProperties != null) {
			valuesIncl[CHORD_SIZE] = getSizeOfChordInclusive(btp, durationLabels, bnp, noteIndex);
		}

		// 2. Determine the indices of the note within the chord  
		// a. Excluding any sustained previous notes
		valuesExcl[INDEX] = getIndexExclusive(btp, bnp,	noteIndex);
		// b. Including any sustained previous notes
		if ((btp != null && !isBidirectional && direction == Direction.LEFT && modelDuration) || 
			(btp != null && isBidirectional && modelDuration) || bnp != null) { // EEND +  && modelDuration
//			if ((basicTabSymbolProperties != null && !modelBackward && modelDuration) || basicNoteProperties != null) {
			valuesIncl[INDEX] = getIndexInclusive(btp, durationLabels, bnp, noteIndex);
		}

		// 3. Determine the pitch distances to the notes below and above
		// Determine the new pitches in the chord
		List<Integer> newPitchesInChord; // = getPitchesInChord(btp, bnp, lowestNoteIndex);
		if (btp != null) {
			newPitchesInChord = Tablature.getPitchesInChord(btp, lowestNoteIndex);
		}
		else {
			newPitchesInChord = Transcription.getPitchesInChord(bnp, lowestNoteIndex);
		}
		// a. Excluding any sustained previous notes
		int minPitchDistanceToLowerExcl = Integer.MAX_VALUE;
		int minPitchDistanceToHigherExcl = Integer.MAX_VALUE;
		for (int i : newPitchesInChord) {
			int currentPitchDist = Math.abs(pitchOfCurrentNote - i);
			if (i < pitchOfCurrentNote && currentPitchDist < minPitchDistanceToLowerExcl) {
				minPitchDistanceToLowerExcl = currentPitchDist;
				valuesExcl[PITCH_DIST_BELOW] = currentPitchDist;
			}
			if (i > pitchOfCurrentNote && currentPitchDist < minPitchDistanceToHigherExcl) {
				minPitchDistanceToHigherExcl = currentPitchDist;
				valuesExcl[PITCH_DIST_ABOVE] = currentPitchDist;
			}	
		}
		// b. Including any sustained previous notes
		if ((btp != null && !isBidirectional && direction == Direction.LEFT && modelDuration) || 
			(btp != null && isBidirectional && modelDuration) || bnp != null) { // EEND +  && modelDuration
//			if ((basicTabSymbolProperties != null && !modelBackward && modelDuration) || basicNoteProperties != null) {
			// Determine any sustained pitches in the chord and create allPitchesInChord
			List<Integer> sustainedPitches = 
				Transcription.getPitchesOfSustainedPreviousNotesInChord(btp,
				durationLabels, bnp, lowestNoteIndex);
			List<Integer> allPitchesInChord = new ArrayList<Integer>(newPitchesInChord);
			allPitchesInChord.addAll(sustainedPitches);
			int minPitchDistanceToLowerIncl = Integer.MAX_VALUE;
			int minPitchDistanceToHigherIncl = Integer.MAX_VALUE;
			for (int i : allPitchesInChord) {
				int currentPitchDist = Math.abs(pitchOfCurrentNote - i);
				if (i < pitchOfCurrentNote && currentPitchDist < minPitchDistanceToLowerIncl) {
					minPitchDistanceToLowerIncl = currentPitchDist;
					valuesIncl[PITCH_DIST_BELOW] = currentPitchDist;
				}
				if (i > pitchOfCurrentNote && currentPitchDist < minPitchDistanceToHigherIncl) {
					minPitchDistanceToHigherIncl = currentPitchDist;
					valuesIncl[PITCH_DIST_ABOVE] = currentPitchDist;
				}	
			}
		}

		// 4. Determine the intervals in the chord and add them to allValues 
		// a. Exluding any sustained previous notes 
		Collections.sort(newPitchesInChord);
		for (int i = 0; i < newPitchesInChord.size() - 1; i++) {
			int interval = Math.abs(newPitchesInChord.get(i + 1) - newPitchesInChord.get(i));
			valuesExcl[FIRST_INTERVAL + i] = interval;
		}
		// b. Including any sustained previous notes
		if ((btp != null && !isBidirectional && direction == Direction.LEFT && modelDuration) || 
			(btp != null && isBidirectional && modelDuration) || bnp != null) { // EEND +  && modelDuration
//			if ((basicTabSymbolProperties != null && !modelBackward && modelDuration) || basicNoteProperties != null) {
			double[] intervalsIncl = getIntervalsInChord(btp, durationLabels, bnp, lowestNoteIndex);
			for (int i = 0; i < intervalsIncl.length; i++) {
				valuesIncl[FIRST_INTERVAL + i] = intervalsIncl[i];
			}
		}

		// 5. Create and return positionWithinChord	  
		// a. In the tablature case 
		if (btp != null) { 
			if (!modelDuration) {
				return valuesExcl;
			}
			else {
				if ((!isBidirectional && direction == Direction.LEFT) || isBidirectional) {
//					if (!modelBackward) {
					return valuesIncl;
				}
				else {
					return valuesExcl;
				}
			}
		}
		// b. In the non-tablature case
		else {
			return valuesIncl;
		}	
	}


	/**
	 * Gets the size of the chord the note at noteIndex is in, including any sustained notes.
	 *  
	 * @param btp
	 * @param durationLabels
	 * @param bnp
	 * @param noteIndex
	 */
	// TESTED for both tablature- and non-tablature case 
	public static int getSizeOfChordInclusive(Integer[][] btp, List<List<Double>> durationLabels,	
		Integer[][] bnp, int noteIndex) {

		Transcription.verifyCase(btp, bnp);

		// Determine the number of new onsets in the chord
		// a. In the tablature case
		int numNewOnsets = 0;
		if (btp != null) {
			numNewOnsets = btp[noteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS];  
		}
		// b. In the non-tablature case
		if (bnp != null) {
			numNewOnsets = bnp[noteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
		}
		// Determine the number of sustained notes in the chord
		int numSustainedNotes = 
			Transcription.getIndicesOfSustainedPreviousNotes(btp, durationLabels, 
			bnp, noteIndex).size();

		return numNewOnsets + numSustainedNotes;
	}


	/**
	 * Determines the index (based on pitch) of the note within the chord, excluding any sustained previous notes.
	 * In the case of a unison in the chord, both unison notes get the same index. In the tablature case, if the 
	 * chord contains a course crossing, the index will be different from btp[noteIndex][Tablature.NOTE_SEQ_NUM] 
	 *     
	 * @param btp
	 * @param bnp
	 * @param noteIndex  
	 * @return
	 */
	// TESTED for both tablature- and non-tablature case
	static int getIndexExclusive(Integer[][] btp, Integer[][] bnp, int noteIndex) {

		Transcription.verifyCase(btp, bnp);

		// 0. Determine the pitch of the note at noteIndex as well as the index of the lowest note in the chord
		int	pitchOfCurrentNote = -1;
		int lowestNoteIndex = -1;
		List<Integer> pitchesInChord;
		// a. In the tablature case
		if (btp != null) {  
			pitchOfCurrentNote = btp[noteIndex][Tablature.PITCH];
			lowestNoteIndex = noteIndex - btp[noteIndex][Tablature.NOTE_SEQ_NUM];
			pitchesInChord = Tablature.getPitchesInChord(btp, lowestNoteIndex);
		}    
		// b. In the non-tablature case
		else {
//		if (bnp != null) {  
			pitchOfCurrentNote = bnp[noteIndex][Transcription.PITCH];
			lowestNoteIndex = noteIndex - bnp[noteIndex][Transcription.NOTE_SEQ_NUM];
			pitchesInChord = Transcription.getPitchesInChord(bnp, lowestNoteIndex);
		}
		// Sort in order to take into account any course crossings
		Collections.sort(pitchesInChord);

		// 1. Determine indexExclusive
		int indexExclusive = -1;
		// Remove any duplicate pitches (unisons)
		List<Integer> pitchesInChordNoUnisons = new ArrayList<Integer>();
		for (int i : pitchesInChord) {
			if (!pitchesInChordNoUnisons.contains(i)) {
				pitchesInChordNoUnisons.add(i);
			}
		}
		// Determine indexExclusive
		for (int i = 0; i < pitchesInChordNoUnisons.size(); i++) {
			if (pitchesInChordNoUnisons.get(i) == pitchOfCurrentNote) {
				indexExclusive = i;
				break;
			}
		}
		return indexExclusive;
	}


	/**
	 * Determines the index (based on pitch) of the note within the chord, including any sustained previous notes.
	 * In the case of a unison in the chord, both unison notes get the same index. Only sustained notes lower in 
	 * pitch than the note at noteIndex are counted. 
	 *     
	 * @param btp
	 * @param durationLabels
	 * @param bnp
	 * @param noteIndex  
	 * @return
	 */
	// TESTED for both tablature- and non-tablature case
	static int getIndexInclusive(Integer[][] btp, List<List<Double>> durationLabels, Integer[][] bnp, int noteIndex) {

		Transcription.verifyCase(btp, bnp);

		// 0. Determine the pitch of the note at noteIndex as well as the index of the lowest note in the chord
		int	pitchOfCurrentNote = -1;
		int lowestNoteIndex = -1;
		// a. In the tablature case
		if (btp != null) {  
			pitchOfCurrentNote = btp[noteIndex][Tablature.PITCH];
			lowestNoteIndex = noteIndex - btp[noteIndex][Tablature.NOTE_SEQ_NUM];
		}    
		// b. In the non-tablature case
		if (bnp != null) {  
			pitchOfCurrentNote = bnp[noteIndex][Transcription.PITCH];
			lowestNoteIndex = noteIndex - bnp[noteIndex][Transcription.NOTE_SEQ_NUM];; 
		}

		// 1. Determine indexExclusive
		int indexExclusive = getIndexExclusive(btp, bnp, noteIndex);

		// 2. Determine indexInclusive
		int indexInclusive = indexExclusive;
		// Determine whether there are sustained notes in the chord. If not, indexInclusive remains unchanged;
		// If so, increase indexInclusive for every sustained note with a pitch LOWER than currentPitch   
		List<Integer> sustainedPitches = 
			Transcription.getPitchesOfSustainedPreviousNotesInChord(btp, 
			durationLabels, bnp, lowestNoteIndex);
		if (sustainedPitches.size() != 0) {
			for (int p : sustainedPitches) {
				if (p < pitchOfCurrentNote) {
					indexInclusive++;
				}
			}
		}
		return indexInclusive;
	}


	/**
	 * Gets the pitches in the chord. Element 0 of the List represents the lowest note's pitch, element 1 the
	 * second-lowest note's, etc. Sustained previous notes are NOT included. 
	 * NB: In the tablature case, if the chord contains course crossings, the List will not be in numerical order.
	 *     In the non-tablature case, the List will always be in numerical order. 
	 * 
	 * @param btp
	 * @param bnp
	 * @param lowestNoteIndex
	 * @return
	 */
	// TESTED (for both tablature- and non-tablature case)
//	public static List<Integer> getPitchesInChord(Integer[][] btp, Integer[][] bnp, int lowestNoteIndex) {
//
//		Transcription.verifyCase(btp, bnp);
//
//		List<Integer> pitchesInChord = new ArrayList<Integer>();	
//		// a. In the tablature case
//		if (btp != null) {
//			int chordSize = btp[lowestNoteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
//			for (int i = lowestNoteIndex; i < lowestNoteIndex + chordSize; i++) {
//				Integer[] currentBasicTabSymbolProperties = btp[i];
//				int currentPitch = currentBasicTabSymbolProperties[Tablature.PITCH];
//				pitchesInChord.add(currentPitch);
//			}
//		}
//		// b. In the non-tablature case
//		else if (bnp != null) {	     
//			int chordSize = bnp[lowestNoteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
//			for (int i = lowestNoteIndex; i < lowestNoteIndex + chordSize; i++) {
//				Integer[] currentBasicNoteProperties = bnp[i];
//				int currentPitch = currentBasicNoteProperties[Transcription.PITCH];
//				pitchesInChord.add(currentPitch);
//			}
//			// Get the pitches for any sustained previous notes
////			List<Integer> sustainedPitches = 
////				getPitchesOfSustainedPreviousNotesInChord(basicNoteProperties, lowestNoteIndex);
////			// Combine and sort
////			pitchesInChord.addAll(sustainedPitches);
////			Collections.sort(pitchesInChord);
//		}
//		return pitchesInChord;
//	}


	/**
	 * Gets the intervals in the chord, also taking into consideration any sustained previous notes in the 
	 * tablature case if durationLabels != <code>null</code> and in the non-tablature case. Returns a 
	 * double[] the size of the maximum number of intervals in a chord (i.e., largestChordSize - 1). When the 
	 * chord contains fewer onsets than Transcription.MAXIMUM_NUMBER_OF_VOICES, -1.0 is added for each missing
	 * interval.
	 * Example: a chord with pitches [0, 10, 20] (from low to high) will return [10.0, 10.0, -1.0, -1.0] 
	 * 
	 * @param btp
	 * @param durationLabels
	 * @param bnp
	 * @param lowestNoteIndex
	 * @return
	 */
	// TESTED (for both tablature- and non-tablature case)
	static double[] getIntervalsInChord(Integer[][] btp, List<List<Double>> durationLabels, Integer[][] bnp, 
		int lowestNoteIndex) {

		Transcription.verifyCase(btp, bnp);

		// 0. Initialise intervalsInChord with all -1.0s
		double[] intervalsInChord = new double[Transcription.MAXIMUM_NUMBER_OF_VOICES - 1];
		Arrays.fill(intervalsInChord, -1.0);

		// 1. List the pitches in the chord
		// a. Get the pitches of the new onsets in the chord
		List<Integer> pitchesInChord; // = getPitchesInChord(btp, bnp, lowestNoteIndex);
		if (btp != null) {
			pitchesInChord = Tablature.getPitchesInChord(btp, lowestNoteIndex);
		}
		else {
			pitchesInChord = Transcription.getPitchesInChord(bnp, lowestNoteIndex);
		}
		// b. Add the pitches of any sustained previous notes and add them to pitchesInChord
		if ((btp != null && durationLabels != null) || bnp != null) { // TODO Hack for CIM paper 28-10-2014
			pitchesInChord.addAll(Transcription.getPitchesOfSustainedPreviousNotesInChord(btp, durationLabels, bnp, lowestNoteIndex));
		}
		// c. Sort numerically
		Collections.sort(pitchesInChord);

		// 2. Get the intervals in the chord
		// For every pitch: get the intervallic distances
//		if (lowestNoteIndex == 671) {
//			System.out.println("pitchesInChord = " + pitchesInChord);
//		}
		for (int i = 0; i < pitchesInChord.size() - 1; i++) {
			double currentPitch = pitchesInChord.get(i);
			double nextPitch = pitchesInChord.get(i + 1);
			intervalsInChord[i] = Math.abs(nextPitch - currentPitch); 
		}	

		return intervalsInChord;
	}


	// tabDurvoiceFeatures
	/** 
	 * Checks for each voice whether the previous or next (depending on the value of the argument direction) note in 
	 * that voice (i.e., the note at the onset time closest to that of the note at noteIndex) is on the same course as
	 * the note at noteIndex. Returns a binary double[] vector indicating with 1.0s for which voice(s) this is the case.
	 * 
	 * NB: Tablature case only; tablature-specific.
	 * 
	 * @param btp
	 * @param transcription
	 * @param direction
	 * @param noteIndex
	 * @return
	 */
	// TESTED for both fwd and bwd model
	static double[] getVoicesWithAdjacentNoteOnSameCourse(Integer[][] btp, Transcription transcription, 
		Direction direction, int noteIndex) {

		// Initialise voicesWithPreviousNoteOnSameCourse with only 0.0s
		double[] voicesWithPreviousNoteOnSameCourse = new double[Transcription.MAXIMUM_NUMBER_OF_VOICES];
		Arrays.fill(voicesWithPreviousNoteOnSameCourse, 0.0);

		Note currentNote = Tablature.convertTabSymbolToNote(btp, noteIndex); 
		int courseCurrentNote = btp[noteIndex][Tablature.COURSE];

		// For all theoretically possible voices 
		for (int voiceNumber = 0; voiceNumber < Transcription.MAXIMUM_NUMBER_OF_VOICES; voiceNumber++) {
			// If the transcription contains the voice with voiceNumber: find the previous note in that voice
			if (voiceNumber < transcription.getPiece().getScore().size()) {
				NotationVoice currentVoice = 
					transcription.getPiece().getScore().get(voiceNumber).get(0);
  
//				// 1. Get the index of the previous Note
//				int previousNoteIndex = getIndexOfPreviousNoteInVoice(currentVoice, currentNote);

				// 1. Determine the previous Note
				Note previousNote = 
					Transcription.getAdjacentNoteInVoice(currentVoice, currentNote, direction==Direction.LEFT);

				// 2. Determine the course previousNote is on; do so only if currentVoice contains notes before currentNote
				if (previousNote != null) {
					int pitchPreviousNote = previousNote.getMidiPitch();
					int onsetTimePreviousNote = 
						previousNote.getMetricTime().mul(Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom()).getNumer();

					// 1. Search in btp for the TabSymbol with the same pitch and onset time as previousNote
					// NB: In the case of a unison there are two such TabSymbols. In order not to overlook the lower one, stop 
					// searching only when onsetTimeCurrentPreviousTabSymbol becomes smaller than onsetTimePreviousNote
					for (int i = 0; i < btp.length; i++) {
						// If the current previous TabSymbol has the same pitch and onset time as previousNote 
						if (btp[i][Tablature.PITCH] == pitchPreviousNote && 
							btp[i][Tablature.ONSET_TIME] == onsetTimePreviousNote) {		  				
							// If (one of) the voice(s) the current previous TabSymbol is in the same as the current voice
							List<Integer> voicesCurrentPreviousTabSymbol = 
								DataConverter.convertIntoListOfVoices(transcription.getVoiceLabels().get(i));
							if (voicesCurrentPreviousTabSymbol.contains(voiceNumber)) {
								// If the course of the current previous TabSymbol is the same as that of the note at noteIndex: 
								// set the appropriate element of voicesWithPreviousNoteOnSameCourse to 1.0 		  				
								if (btp[i][Tablature.COURSE] == courseCurrentNote) {
									voicesWithPreviousNoteOnSameCourse[voiceNumber] = 1.0;
								}
							}
						}
						// Stop searching when the onset time of the current previous TabSymbol gets larger than that of previousNote
						if (btp[i][Tablature.ONSET_TIME] > onsetTimePreviousNote) {
							break;
						}
					}
				}
			}
		}
		return voicesWithPreviousNoteOnSameCourse;
	}



//	Note getAdjacentNoteInVoiceTillman(NotationVoice voice, Note note, Direction direction) {
//		Note adjacentNote = null;
//		if (true) {
//			int index = voice.find(note.getMetricTime()); 
//			if (direction == Direction.LEFT) {
//				if (index == 0) {
//					return null;
//				} else if (index > 0) {
//					return voice.get(index-1).getLowestNote();
//				} else { // (index < 0) 
//					int insertPos = -index - 1;
//					if(insertPos==0)
//						return null;
//					else
//						return voice.get(insertPos-1).getLowestNote();
//				}
//			} else { // direction == Direction.RIGHT
//				if(index == voice.size()-1)
//					return null;
//				else if(index > 0)
//					return voice.get(index+1).getLowestNote();
//				else { // index < 0
//					int insertPos = -index - 1;
//					if(insertPos == voice.size()-1)
//						return null;
//					else 
//						return voice.get(insertPos).getLowestNote();
//				}
//			}
//		} else {
//			// List all the Notes in voice
//			List<Note> notesInVoice = new ArrayList<Note>();
//			int seqNumOfCurrNote = -1;
//			for (int i = 0; i < voice.size(); i++) {
//				Note currNote = voice.get(i).get(0);
//				notesInVoice.add(currNote);
//				if (currNote.getMidiPitch() == note.getMidiPitch()
//						&& currNote.getMetricTime().equals(note.getMetricTime())) {
//					seqNumOfCurrNote = i;
//				}
//			}
//
//			// Get the adjacent Note. There are two possibilities:
//			// a. If seqNumOfCurrNote is not -1, note is in voice; determine adjacentNote
//			// b. If seqNumOfCurrNote is -1, note is not in voice. In this case, a fictional adjacent note, i.e., the note
//			// closest in onset time to note, must be returned
//			if (direction == Direction.LEFT) {
//				// a.
//				if (seqNumOfCurrNote != -1) {
//					// adjacentNote only exists if note is not the first Note in the voice
//					if (seqNumOfCurrNote != 0) {
//						adjacentNote = notesInVoice.get(seqNumOfCurrNote - 1);
//					}
//				}
//				// b.
//				else {
//					Note closest = null;
//					for (Note n : notesInVoice) {
//						if (n.getMetricTime().isLess(note.getMetricTime())) {
//							closest = n;
//						}
//						// Break when the onset time of n becomes equal to or greater than that of note; closest is now the last 
//						// smaller onset time
//						else {
//							break;
//						}
//					}
//					adjacentNote = closest;
//				}
//			} else if (direction == Direction.RIGHT) {
//				// a.
//				if (seqNumOfCurrNote != -1) {
//					// adjacentNote only exists if note is not the last Note in the voice
//					if (seqNumOfCurrNote != (voice.size() - 1)) {
//						adjacentNote = notesInVoice.get(seqNumOfCurrNote + 1);
//					}
//				}
//				// b. 
//				else {
//					Note closest = null;
//					for (Note n : notesInVoice) {
//						// Break when the onset time of n becomes greater than that of note; closest is now the first greater 
//						// onset time
//						if (n.getMetricTime().isGreater(note.getMetricTime())) {
//							closest = n;
//							break;
//						}
//					}
//					adjacentNote = closest;
//				}
//			}
//
//			return adjacentNote;
//		}
//	}
		
		
	/**
	 * Compares the note given as argument to the adjacent Note (previous or next, depending
	 * on the value of direction) in each voice, and calculates their pitch proximity, 
	 * inter-onset time proximity, and offset-onset time proximity. 
	 * If decisionContextSize > 1, does the same for each next adjacent note within the 
	 * given decision context, and also calculates the pitch movement (1.0 if up; 0.0 if same
	 * or down).  
	 *  
	 * For each adjacent note within the decision context, returns a double[][] containing
	 *   as element 0: a double[] containing the pitch proximities of the current Note to the 
	 *                 adjacent Note in each voice, where element 0 is the proximity to voice 0
	 *                 (the top voice), element 1 that to voice 1 (the second from the top), etc.;
	 *   as element 1: a double[] containing the inter-onset time proximities of the current 
	 *                 Note to the adjacent Note in each voice;
	 *   as element 2: a double[] containing the offset-onset time proximities of the current 
	 *                 Note to the adjacent Note in each voice, i.e, for the tablature case:
	 *                   if direction = Direction.LEFT:
	 *                     not modelling duration: onset currentNote minus offset previous Note,
	 *                                             using the minimum duration of the previous note;
	 *                     modelling duration:     onset currentNote minus offset previous Note,
	 *                                             using the full duration of the previous note;
	 *                   if direction = Direction.RIGHT:
	 *                     bwd model:              offset currentNote minus onset next Note, 
	 *                                             using the minimum duration of the current note;
	 *                     bi-directional model:   offset currentNote minus onset next Note, 
	 *                                             using the full duration of the current note;
	 *                  and for the non-tablature case, where there are no minimum durations:
	 *                    if direction = Direction.LEFT:  onset currentNote minus offset previous Note
	 *                    if direction = Direction.RIGHT: offset currentNote minus onset next Note 
	 *   and, if decisionContextSize > 1
	 *   as element 3	a double[] containing the pitch movement of the current Note to the 
	 *                  adjacent Note in each voice (1.0 if up and 0.0 if same or down).
	 *              
	 * @param btp
	 * @param transcription
	 * @param currentNote
	 * @param direction
	 * @param modelDuration
	 * @param isBidirectional
	 * @param decisionContextSize
	 * @return
	 */
	 // TESTED for both tablature- (non-dur and dur) and non-tablature case; for both fwd and bwd model
	public static List<double[][]> getPitchAndTimeProximitiesToAllVoices(Integer[][] btp, Transcription transcription, 
		Note currentNote, Direction direction, boolean modelDuration, boolean isBidirectional,
		int decisionContextSize) {
		
		int arrElements = (decisionContextSize == 1) ? 3 : 4;
		int arrSize = (btp != null) ? 4 : 3;

		List<double[][]> res = new ArrayList<>();
		for (int i = 0; i < decisionContextSize; i++) {
			// Initialise with the default values also used when a note is the first
			// one in a voice (see getProximitiesAndMovementToVoice()). The elements that 
			// correspond with voices not in the transcription retain these values  
			double[][] r = new double[arrElements][Transcription.MAXIMUM_NUMBER_OF_VOICES];
			for (double[] d : r) {
				Arrays.fill(d, -1.0);
			}
			res.add(r);
//			// Pitch movement: each voice has two bits representing up and down; the position
//			// of the 1.0 indicates which it is. No 1.0 in a bit pair indicates no change (same)
//			double[] mvmts = new double[2*Transcription.MAXIMUM_NUMBER_OF_VOICES];
//			// Pitch movement: 1.0 indicates up, 0.0 indicates same or down (pitchProx disambiguates:
//			// if it is 1.0 (i.e. 1 / (0+1)) it is same; else down 
//			double[] mvmts = new double[Transcription.MAXIMUM_NUMBER_OF_VOICES];
//			Arrays.fill(mvmts, -1.0);
//			resMvmts.add(mvmts);
		}

		// Traverse all the theoretically possible voices 
		for (int voiceNumber = 0; voiceNumber < Transcription.MAXIMUM_NUMBER_OF_VOICES; voiceNumber++) {
			// a. If transcription contains the voice with voiceNumber: calculate the proximities of currentNote
			// to the previous note in that voice and set the appropriate element of pitchAndTimeProximities
			if (voiceNumber < transcription.getPiece().getScore().size()) {
				NotationVoice currentVoice = transcription.getPiece().getScore().get(voiceNumber).get(0);
				List<double[]> allPAndTProxCurrNote = 
					getProximitiesAndMovementToVoice(btp, currentVoice, currentNote, 
					direction, decisionContextSize);
				for (int i = 0; i < decisionContextSize; i++) {
					double[] pAndTProxCurrNote = allPAndTProxCurrNote.get(i);
					// a. In the tablature case
					if (btp != null) {
						res.get(i)[0][voiceNumber] = pAndTProxCurrNote[0];
						res.get(i)[1][voiceNumber] = pAndTProxCurrNote[1];
						if (direction == Direction.LEFT) {
							// Not modelling duration, where the full duration of the previous note is unknown
							if (!modelDuration) {
								res.get(i)[2][voiceNumber] = pAndTProxCurrNote[2];
							}
							// Modelling duration, where the full duration of the previous note is known
							else {
								res.get(i)[2][voiceNumber] = pAndTProxCurrNote[3];
							}
						}
						else {
							// Using the bwd model, where the full duration of the current note is unknown
							if (!isBidirectional) {
								res.get(i)[2][voiceNumber] = pAndTProxCurrNote[2];
							}
							// Using the bi-directional model, where the full duration of the current note is known 
							// if modelDuration is true
							else  {
								if (!modelDuration) {
									res.get(i)[2][voiceNumber] = pAndTProxCurrNote[2];
								}
								else if (modelDuration) {
									res.get(i)[2][voiceNumber] = pAndTProxCurrNote[3];
								}
							}
						}
						if (decisionContextSize > 1) {
//							// Set both bits of currentVoice to 0
//							resMvmts.get(i)[voiceNumber*2] = 0.0;
//							resMvmts.get(i)[(voiceNumber*2) + 1] = 0.0;
//							// Pitch movement positive: set first bit to 1.0
//							if (pAndTProxCurrNote[4] > 0.0) {
//								resMvmts.get(i)[voiceNumber*2] = 1.0;
//							}
//							// Pitch movement negative: set second bit to 1.0
//							else if (pAndTProxCurrNote[4] < 0.0) {
//								resMvmts.get(i)[(voiceNumber*2) + 1] = 1.0;
//							}
							
//							if (pAndTProxCurrNote[4] > 0.0) {
//								res.get(i)[3][voiceNumber] = 1.0;
//							}
//							else {
//								// Only if there is an adjacent note
//								if (pAndTProxCurrNote[0] != -1) {
//									res.get(i)[3][voiceNumber] = 0.0;
//								}
//							}

						}
					}
					// b. In the non-tablature case
					else {
						res.get(i)[0][voiceNumber] = pAndTProxCurrNote[0];
						res.get(i)[1][voiceNumber] = pAndTProxCurrNote[1];
						res.get(i)[2][voiceNumber] = pAndTProxCurrNote[2];
//						if (decisionContextSize > 1) {
//							if (pAndTProxCurrNote[4] > 0.0) {
//								res.get(i)[3][voiceNumber] = 1.0;
//							}
//							else {
//								// Only if there is an adjacent note
//								if (pAndTProxCurrNote[0] != -1) {
//									res.get(i)[3][voiceNumber] = 0.0;
//								}
//							}
//						}
					}
					if (decisionContextSize > 1) {
						if (pAndTProxCurrNote[arrSize] > 0.0) {
							res.get(i)[3][voiceNumber] = 1.0;
						}
						else {
							// Only if there is an adjacent note
							if (pAndTProxCurrNote[0] != -1) {
								res.get(i)[3][voiceNumber] = 0.0;
							}
						}
					}
				}
			}
		}

		return res;
	}


	/**
	 * Compares the Note given as argument to the adjacent Note (previous or next, depending on the value of direction)
	 * in the voice given as argument, and calculates their pitch proximity, their inter-onset time proximity, their 
	 * offset-onset time proximity, and the pitch movement. 
	 * (1) Proximities are defined as inverted distances: 
	 *     -pitch proximity is > 0 and <= 1 (where a pitch proximity of 1.0 means no distance)
	 *     -inter-onset proximity is > 0 and < 1
	 *     -offset-onset proximity is
	 *      a. > 0 and < 1 if the offset time is before the onset time (i.e., if the offset-onset time is positive)
	 *      b. 1 if the offset time equals the onset time (i.e., if the offset-onset time is 0)
	 *      c. < 0 and > -1 if the offset time is after the onset time (i.e., if the offset-onset time is negative)
	 * NB: Proximities to non-existing voices are indicated with -1.0. 
	 * (2) Pitch movements are defined in semitones, and can be positive (ascending) and negative (descending). 
	 * 
	 * Returns a List<double[]>, containing, for each previous note within the given 
	 * decisionContextSize, a double[] containing:
	 *   as element 0: the pitch proximity of currentNote to the adjacent Note in voiceToCompareTo;
	 *   as element 1: the inter-onset time proximity of currentNote to the adjacent Note in voiceToCompareTo;  
	 *   as element 2: the offset-onset time proximity of currentNote to the adjacent Note in voiceToCompareTo using
	 *                 minimum durations, i.e.,
	 *                   if direction = Direction.LEFT:
	 *                     onset currentNote minus offset previous Note, using the minimum duration of the previous note;
	 *                   if direction = Direction.RIGHT:
	 *                     offset currentNote minus onset next Note, using the minimum duration of the current note; 
	 *   as element 3: the offset-onset time proximity of currentNote to the adjacent Note in voiceToCompareTo using
	 *                 full durations of the previous (Direction.LEFT) and current (Direction.RIGHT) note; 
	 *   as element 4: the pitch movement (in semitones) of currentNote with regard to the adjacent Note in
	 *                 voiceToCompareTo.                
	 * NB: In the non-tablature case, element 2 above is not included and a double[] of size 4 is returned.
	 *
	 * @param btp 
	 * @param voiceToCompareTo
	 * @param currentNote
	 * @param direction
	 * @param decisionContextSize
	 * @return
	 */
	// TESTED for both tablature- and non-tablature case; for both fwd and bwd model
	public static List<double[]> getProximitiesAndMovementToVoice(Integer[][] btp, 
		NotationVoice voiceToCompareTo, Note currentNote, Direction direction, 
		int decisionContextSize) {

		// 1. Determine the previous Note
//		Note previousNote = 
//			Transcription.getAdjacentNoteInVoice(voiceToCompareTo, currentNote, direction==Direction.LEFT);
		List<Note> previousNotes = new ArrayList<>();
		Note origCurrentNote = currentNote;
		for (int i = 0; i < decisionContextSize; i++) {
			// If currentNote is null, the previous currentNote is the first in the voice
			if (currentNote != null) {
				Note prevNote = 
					Transcription.getAdjacentNoteInVoice(voiceToCompareTo, currentNote, direction==Direction.LEFT);
				previousNotes.add(prevNote);
				currentNote = prevNote;
			}
			else {
				previousNotes.add(null);
			}
		}
		currentNote = origCurrentNote;

		List<double[]> res = new ArrayList<>();
		for (Note previousNote : previousNotes) {
			double[] proximitiesAndMovementToVoice = (btp != null) ? new double[5] : new double[4];

			// 2. Determine the pitch difference, inter-onset time, offset-onset time, and 
			// pitch movement between currentNote and the previous Note in voiceToCompareTo
			// a. If voiceToCompareTo contains Notes before currentNote
			if (previousNote != null) {
				// 1. Determine the pitch difference and the pitch movement
				int pitchDif = Math.abs(previousNote.getMidiPitch() - currentNote.getMidiPitch());
				int pitchMovement = currentNote.getMidiPitch() - previousNote.getMidiPitch();

				// 2. Determine the inter-onset time
				Rational onsetTimeCurrentNote = currentNote.getMetricTime();
				Rational onsetTimePreviousNote = previousNote.getMetricTime();
				double interOnsetTime = onsetTimeCurrentNote.sub(onsetTimePreviousNote).toDouble();

				// 3. Determine the offset-onset time
				double offsetOnsetTimeExcl = -1.0;
				double offsetOnsetTimeIncl = -1.0;
				// a. In the tablature case
				if (btp != null) {
					// a. Fwd model
					if (direction == Direction.LEFT) {
						// 1. Determine offsetOnsetTimeExcl
						int gridXPreviousNote = 
							onsetTimePreviousNote.mul(Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom()).getNumer();
						Rational minDurPreviousNote = null;
						for (Integer[] b : btp) {
							if (b[Tablature.ONSET_TIME] == gridXPreviousNote) {
								minDurPreviousNote = new Rational(b[Tablature.MIN_DURATION], Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom()); 
								break;
							}
						}
						Rational offsetTimePreviousNoteExcl = 
							onsetTimePreviousNote.add(minDurPreviousNote);
						offsetOnsetTimeExcl = 
							onsetTimeCurrentNote.sub(offsetTimePreviousNoteExcl).toDouble();
						// 2. Determine offsetOnsetTimeIncl
						Rational offsetTimePreviousNoteIncl = 
							onsetTimePreviousNote.add(previousNote.getMetricDuration());
						offsetOnsetTimeIncl = 
							onsetTimeCurrentNote.sub(offsetTimePreviousNoteIncl).toDouble();
					}
					// b. Bwd model
					else {
						// 1. Determine offsetOnsetTimeExcl
						int gridXCurrentNote = 
							onsetTimeCurrentNote.mul(Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom()).getNumer();
						Rational minDurCurrentNote = null;
						for (Integer[] b : btp) {
							if (b[Tablature.ONSET_TIME] == gridXCurrentNote) {
								minDurCurrentNote = 
									new Rational(b[Tablature.MIN_DURATION], Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom()); 
								break;
							}
						}
						Rational offsetTimeCurrentNoteExcl = 
							onsetTimeCurrentNote.add(minDurCurrentNote);
						offsetOnsetTimeExcl = 
							offsetTimeCurrentNoteExcl.sub(onsetTimePreviousNote).toDouble();
						// 2. Determine offsetOnsetTimeIncl
						Rational offsetTimeCurrentNoteIncl = 
							onsetTimeCurrentNote.add(currentNote.getMetricDuration());
						offsetOnsetTimeIncl = 
							offsetTimeCurrentNoteIncl.sub(onsetTimePreviousNote).toDouble();
					}
				}
				// b. In the non-tablature case
				else {
					// a. Fwd model
					if (direction == Direction.LEFT) {
						Rational offsetTimePreviousNote = 
							onsetTimePreviousNote.add(previousNote.getMetricDuration());
						offsetOnsetTimeIncl = onsetTimeCurrentNote.sub(offsetTimePreviousNote).toDouble();
					}
					// b. Bwd model
					else {
						Rational offsetTimeCurrentNote = 
							onsetTimeCurrentNote.add(currentNote.getMetricDuration());
						offsetOnsetTimeIncl = 
							offsetTimeCurrentNote.sub(onsetTimePreviousNote).toDouble();
					}
				}

				// 4. Create and set proximitiesAndMovementToVoice
				// pitchDif (values >= 0)
				proximitiesAndMovementToVoice[0] = calculateProximity((double)pitchDif);
				// interOnsetTime (values > 0) 
				proximitiesAndMovementToVoice[1] = calculateProximity(interOnsetTime);
				// a. In the tablature case
				if (btp != null) {
					// offsetOnsetTimeExcl (values >= 0)
					proximitiesAndMovementToVoice[2] = calculateProximity(offsetOnsetTimeExcl);
					// offsetOnsetTimeIncl (values between -inf and +inf (in theory))
					proximitiesAndMovementToVoice[3] = calculateProximity(offsetOnsetTimeIncl); 
					// pitchMovement (values between -inf and +inf (in theory))
					proximitiesAndMovementToVoice[4] = (double) pitchMovement;
				}
				// b. In the non-tablature case
				else {
					// offsetOnsetTime (values between -inf and +inf (in theory))
					proximitiesAndMovementToVoice[2] = calculateProximity(offsetOnsetTimeIncl); 
					// pitchMovement (values between -inf and +inf (in theory))
					proximitiesAndMovementToVoice[3] = (double) pitchMovement;
				}
			}
			// b. If voiceToCompareTo contains no Notes before currentNote (i.e., if currentNote is the first Note in 
			// voiceToCompareTo): set "does-not-apply" values of -1.0 (proximities) and 0.0 (movement)
			else {
				Arrays.fill(proximitiesAndMovementToVoice, -1.0);
				proximitiesAndMovementToVoice[proximitiesAndMovementToVoice.length - 1] = 0.0;
			}
			res.add(proximitiesAndMovementToVoice);
		}

		// 3. Return proximitiesAndMovementToVoice
		return res;
	}


	/**
	 * Turns the given distance into a proximity (i.e., inverse distance). Returns a positive number if the 
	 * given distance is positive, 1.0 if it is 0.0, and a negative number if it is negative.
	 * The formula is:
	 *          1 / (x + 1) if x >= 0
	 * f(x) = {
	 *          1 / (x - 1) if x < 0 
	 * Alternative notation: f(x) = [ (sgn(x) + 1) - ((sgn(x))^2) ] / [ (sgn(x) * x) + 1 ]
	 * 
	 * Example: x = 2 gives 1/3; x = 1 gives 1/2; x = 0 gives 1; x = -1 gives -1/2; x = -2 gives -1/3; etc.
	 *  
	 * @param distance
	 * @return
	 */
	// TESTED
	static double calculateProximity(double distance) {
		if (distance >= 0) {
			return 1.0 / (distance + 1); 
		}
		else {
			return 1.0 / (distance - 1);
		}
//		double num = (Math.signum(distance) + 1) - Math.pow(Math.signum(distance), 2.0);
//		double denom = (Math.signum(distance)*distance) + 1;
//		return num/denom;
	}


	/**
	 * Determines which voices are already occupied in the current chord. These include 
	 *   (1) voices assigned to any notes in the chord below the note at noteIndex. These include any lower unison
	 *     notes, which in the tablature case will be those on the lower course, and in the non-tablature case 
	 *     those (a) with the longer duration or (b) when the durations are equal, those in the lower voice;
	 * and, if direction = Direction.LEFT and modelDuration is <code>true</code> (in the tablature case)     
	 *   (2) voices assigned to any sustained previous notes.
	 * 
	 * Returns a double[], where the position of the 1.0s indicates which voices (if any) have already been 
	 * taken (where position 0 represents the top voice, position 1 the second highest, etc.).
	 *       
	 * @param btp
	 * @param durationLabels
	 * @param voicesCoDNotes
	 * @param bnp
	 * @param voiceLabels
	 * @param direction
	 * @param noteIndex
	 * @return 
	 */
	// TESTED for both tablature- (non-dur and dur) and non-tablature case; for both fwd and bwd model
	public static double[] getVoicesAlreadyOccupied(Integer[][] btp, List<List<Double>> durationLabels, 
		List<Integer[]>	voicesCoDNotes, Integer[][] bnp, List<List<Double>> voiceLabels, 
		Direction direction, int noteIndex, boolean modelDuration, boolean isBidirectional) {

		Transcription.verifyCase(btp, bnp);

		// Create voicesAlreadyOccupied and initialise it with only 0.0s
		double[] voicesAlreadyOccupied = new double[Transcription.MAXIMUM_NUMBER_OF_VOICES];
		Arrays.fill(voicesAlreadyOccupied, 0.0);

		// a. In the tablature case
		if (btp != null) {			
			int startIndex = noteIndex - btp[noteIndex][Tablature.NOTE_SEQ_NUM];
			// 1. Set the voices for any notes in the chord below the note at noteIndex in voicesAlreadyOccupied
			for (int i = startIndex; i < noteIndex; i++) {
				List<Double> currentVoiceLabel = voiceLabels.get(i);
				// For-loop needed so that if the note at index i is a CoD, both voices are added
				for (int j = 0; j < currentVoiceLabel.size(); j++) {
					if (currentVoiceLabel.get(j) == 1) {
						voicesAlreadyOccupied[j] = 1.0;
					}
				}
			}
			// 2. Set the voices for any sustained notes in voicesAlreadyOccupied (only in the fwd model when 
			// modelling duration and in the bi-directional model)
			if ((!isBidirectional && direction == Direction.LEFT && modelDuration) || isBidirectional && modelDuration) { // EEND + "&& modelDuration"
//				if ((direction == Direction.LEFT && modelDuration) || direction == Direction.BOTH) {
//				if (!modelBackward && modelDuration) {
				Rational onsetTimeCurrentNote = new Rational(btp[noteIndex][Tablature.ONSET_TIME],
					Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
				List<Integer> indicesOfSustainedNotes = 
					Transcription.getIndicesOfSustainedPreviousNotes(btp, 
					durationLabels, bnp, noteIndex);
				for (int i = 0; i < indicesOfSustainedNotes.size(); i++) {
					int currentIndex = indicesOfSustainedNotes.get(i);
					List<Double> currentVoiceLabel = voiceLabels.get(currentIndex); 

					// a. If the note at currentIndex is a CoD
					if (Collections.frequency(currentVoiceLabel, 1.0) > 1) {	
						// Determine the onset time and offset time(s) of the CoDNotes
						Rational onsetTimeCoDNote =	new Rational(btp[currentIndex][Tablature.ONSET_TIME],
							Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
						List<Double> durationLabelCoDNote = durationLabels.get(currentIndex);
						Rational[] durationsCoDNote = 
							DataConverter.convertIntoDuration(durationLabelCoDNote);
						Rational offsetTimeLongerCoDNote = onsetTimeCoDNote.add(durationsCoDNote[0]);
						Rational offsetTimeShorterCoDNote = null;
						// If durationsCoDNote contains two elements, i.e., if the CoDnotes have different durations:
						// calculate offsetTimeShorterCoDNote
						if (durationsCoDNote.length > 1) {
							offsetTimeShorterCoDNote = onsetTimeCoDNote.add(durationsCoDNote[1]);
						}
						// If durationsCoDNote contain only one element, i.e., if the CoDnotes have the same duration:
						// set offsetTimeShorterCoDNote to the duration of offsetTimeLongerCoDNote
						else {
							offsetTimeShorterCoDNote = offsetTimeLongerCoDNote;
						}
						// Determine the voices of the CoDnotes
						int voiceLongerCoDNote = voicesCoDNotes.get(currentIndex)[0];
						int voiceShorterCoDNote = voicesCoDNotes.get(currentIndex)[1];

						// Set the voice for each CoDnote whose offset time is greater than the onset time of the note at 
						// noteIndex in voicesAlreadyOccupied
						// a. For the longer CoDnote
						if (offsetTimeLongerCoDNote.isGreater(onsetTimeCurrentNote)) {
							voicesAlreadyOccupied[voiceLongerCoDNote] = 1.0;
						}
						// b. For the shorter CoDnote
						if (offsetTimeShorterCoDNote.isGreater(onsetTimeCurrentNote)) {
							voicesAlreadyOccupied[voiceShorterCoDNote] = 1.0;
						} 
					}
					// b. If the note at the current index is not a CoD 
					else {
						voicesAlreadyOccupied[currentVoiceLabel.indexOf(1.0)] = 1.0;
					}
				}
			}
		}
		// b. In the non-tablature case
		else if (bnp != null) {
			int startIndex = noteIndex - bnp[noteIndex][Transcription.NOTE_SEQ_NUM];

			// 1. Set the voices for any notes in the chord below the note at noteIndex in voicesAlreadyOccupied
			for (int i = startIndex; i < noteIndex; i++) {
				List<Double> currentVoiceLabel = voiceLabels.get(i);
				voicesAlreadyOccupied[currentVoiceLabel.indexOf(1.0)] = 1.0;
			}
			// 2. Set the voices for any sustained notes in voicesAlreadyOccupied (only in the fwd model and in the 
			// bi-directional model) 	
			if ((!isBidirectional && direction == Direction.LEFT) || isBidirectional) {
				List<Integer> indicesOfSustainedNotes = 
					Transcription.getIndicesOfSustainedPreviousNotes(btp, 
					durationLabels, bnp, noteIndex);
				for (int i = 0; i < indicesOfSustainedNotes.size(); i++) {
					int currentIndex = indicesOfSustainedNotes.get(i);
					List<Double> currentVoiceLabel = voiceLabels.get(currentIndex); 
					voicesAlreadyOccupied[currentVoiceLabel.indexOf(1.0)] = 1.0;
				}
			}
		}
		return voicesAlreadyOccupied;	  
	}


	// =================================== FEATURE VECTOR GENERATION ===================================
	/**
	 * Generates the feature vector for the note at the given index.
	 * 
	 * @param btp
	 * @param durationLabels
	 * @param voicesCoDNotes
	 * @param bnp
	 * @param transcription
	 * @param currentNote
	 * @param voiceLabels
	 * @param meterInfo
	 * @param noteIndex
	 * @param argModelDuration
	 * @param argModelBackward
	 * @return 
	 */
	// TESTED for both tablature- (non-dur and dur) and non-tablature case; for fwd and bwd model 
	static List<Double> generateNoteFeatureVector(Integer[][] btp, List<List<Double>> durationLabels, 
		List<Integer[]> voicesCoDNotes, Integer[][] bnp, Transcription transcription, Note currentNote,
		List<List<Double>> voiceLabels, List<Integer[]> meterInfo, int noteIndex, boolean argModelDuration,
		boolean argModelBackward, int decisionContextSize) { 
	
		Transcription.verifyCase(btp, bnp);

		List<Double> noteFeatureVector = new ArrayList<Double>();

		boolean argIsBidir = false;
		// Set modelDuration and direction
//		setModelDuration(argModelDuration);
//		setModelBackward(argModelBackward);
		Direction direction = Direction.LEFT;
		if (argModelBackward) {
			direction = Direction.RIGHT;
		}

		// List the three feature sets. The correct features are automatically calculated for tablature- (non-dur/dur) and 
		// non-tablature case depending on values of btp and bnp and argModelDuration)
		// 1. Tablature features 
		double[] tabFeatures = getBasicNoteFeatures(btp, bnp, meterInfo, noteIndex); 

		// 2. Tablature + duration features
		double[] tabDurFeatures = 
			getPositionWithinChord(btp, durationLabels, bnp, direction, noteIndex,
			argModelDuration, argIsBidir);

		// 3. Tablature + duration + voice features
		// Gather all double[]s in a List
		List<double[]> allAsList = new ArrayList<double[]>();
		if (btp != null) {
			// Voices with previous note on same course
			allAsList.add(getVoicesWithAdjacentNoteOnSameCourse(btp, transcription, direction, noteIndex));
		}
		// Pitch- and time proximities
		List<double[][]> pitchAndTimeProximities = 
			getPitchAndTimeProximitiesToAllVoices(btp, transcription, currentNote, 
			direction, argModelDuration, argIsBidir, decisionContextSize);
		for (double[][] pAndTProx : pitchAndTimeProximities) {
			for (double[] d : pAndTProx) {
				allAsList.add(d);
			}
		}
//		for (double[] d : pitchAndTimeProximities) {
//			allAsList.add(d);
//		}
		// Voices already occupied
		allAsList.add(getVoicesAlreadyOccupied(btp, durationLabels, voicesCoDNotes, 
			bnp, voiceLabels, direction, noteIndex, argModelDuration, argIsBidir));
		// Concatenate the double[]s in the List
		double[] tabDurVoiceFeatures = ToolBox.concatDoubleArrays(allAsList);

		// Combine the three feature sets in noteFeatureVector and return it  
		double[] allFeatures = ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{tabFeatures, 
			tabDurFeatures, tabDurVoiceFeatures}));
		for (double d : allFeatures) {
			noteFeatureVector.add(d);
		}
		return noteFeatureVector;
	}
	
	
	static List<Double> generateNoteFeatureVectorDISSFirst(Integer[][] btp, List<List<Double>> durationLabels, 
		List<Integer[]> voicesCoDNotes, Integer[][] bnp, Transcription transcription, Note currentNote,
		List<List<Double>> voiceLabels, List<Integer[]> meterInfo, int noteIndex, boolean argModelDuration,
		boolean argModelBackward, int decisionContextSize) { 
		
		Transcription.verifyCase(btp, bnp);
		
		List<Double> fv = new ArrayList<Double>();
		
		// Set modelDuration and direction
//		setModelDuration(argModelDuration);
//		setModelBackward(argModelBackward);
		boolean argIsBidir = false;
		Direction direction = Direction.LEFT;
		if (argModelBackward) {
			direction = Direction.RIGHT;
		}
		
		// a. In the tablature case
		if (btp != null) {
			// 1. Note-level features 
			double[] basicNote = getBasicNoteFeatures(btp, bnp, meterInfo, noteIndex);
			for (int i = 0; i < 6; i++) {
				fv.add(basicNote[i]);
			}
			
			// 2. Note-chord features
			double[] posInChord = 
				getPositionWithinChord(btp, durationLabels, bnp, direction, 
				noteIndex, argModelDuration, argIsBidir);
			// indexInChord, distToNoteBelow, distToNoteAbove
			for (int i = 1; i < 4; i++) {
				fv.add(posInChord[i]);
			}
			
			// 3. Chord-level features
			// chordSize
			fv.add(posInChord[0]);
			// metPos
			fv.add(basicNote[6]);
			// numNewOnsets
			fv.add(basicNote[basicNote.length - 1]);
			// intervals
			for (int i = 4; i < posInChord.length; i++) {
				fv.add(posInChord[i]);
			}
			
			// 4. Polyphonic embedding features
			// adjacentNoteOnSameCourse
			double[] adj = getVoicesWithAdjacentNoteOnSameCourse(btp, transcription, direction, noteIndex); 
			for (double d : adj) {
				fv.add(d);
			}
			// proximities
			List<double[][]> prox = 
				getPitchAndTimeProximitiesToAllVoices(btp, transcription, 
				currentNote, direction, argModelDuration, argIsBidir, decisionContextSize);
			for (double[][] pAndTProx : prox) {
				for (double[] p : pAndTProx) {
					for (double d : p) {
						fv.add(d);
					}
				}
			}
//			for (double[] p : prox) {
//				for (double d : p) {
//					fv.add(d);
//				}
//			}
			// alreadyOcc
			double[] alrOcc = 
				getVoicesAlreadyOccupied(btp, durationLabels, voicesCoDNotes, bnp,
				voiceLabels, direction, noteIndex, argModelDuration, argIsBidir);
			for (double d : alrOcc) {
				fv.add(d);
			}	
		}
		// b. In the non-tablature case
		else if (bnp != null) {
			// 1. Note-level features 
			double[] basicNote = getBasicNoteFeatures(btp, bnp, meterInfo, noteIndex);
			for (int i = 0; i < 3; i++) {
				fv.add(basicNote[i]);
			}
			
			// 2. Note-chord features
			double[] posInChord = 
				getPositionWithinChord(btp, durationLabels, bnp, direction, 
				noteIndex, argModelDuration, argIsBidir);
			// indexInChord, distToNoteBelow, distToNoteAbove
			for (int i = 1; i < 4; i++) {
				fv.add(posInChord[i]);
			}
			
			// 3. Chord-level features
			// chordSize
			fv.add(posInChord[0]);
			// metPos
			fv.add(basicNote[3]);
			// numNewOnsets
			fv.add(basicNote[basicNote.length - 1]);
			// intervals
			for (int i = 4; i < posInChord.length; i++) {
				fv.add(posInChord[i]);
			}
			
			// 4. Polyphonic embedding features
			// proximities
			List<double[][]> prox = 
				getPitchAndTimeProximitiesToAllVoices(btp, transcription, currentNote, 
				direction, argModelDuration, argIsBidir, decisionContextSize);
			for (double[][] pAndTProx : prox) {
				for (double[] p : pAndTProx) {
					for (double d : p) {
						fv.add(d);
					}
				}
			}
//			for (double[] p : prox) {
//				for (double d : p) {
//					fv.add(d);
//				}
//			}
			// alreadyOcc
			double[] alrOcc = 
				getVoicesAlreadyOccupied(btp, durationLabels, voicesCoDNotes, bnp,
				voiceLabels, direction, noteIndex, argModelDuration, argIsBidir);
			for (double d : alrOcc) {
				fv.add(d);
			}	
		}
		return fv;
	}
	
	
	/**
	 * Generates the feature vector for the note at the given index (unidir model).
	 * 
	 * @param btp
	 * @param durationLabels
	 * @param voicesCoDNotes
	 * @param bnp
	 * @param transcription
	 * @param currentNote
	 * @param voiceLabels
	 * @param meterInfo
	 * @param noteIndex
	 * @param argModelDuration
	 * @param procMode
	 * @return
	 */
	// TESTED
	public static List<Double> generateNoteFeatureVectorDISS(Integer[][] btp, 
		List<List<Double>> durationLabels, List<Integer[]> voicesCoDNotes, Integer[][] bnp,
		Transcription transcription, Note currentNote, List<List<Double>> voiceLabels,
		List<Integer[]> meterInfo, int noteIndex, boolean argModelDuration,
		ProcessingMode procMode, FeatureVector featVec, int decisionContextSize) {

		Transcription.verifyCase(btp, bnp);
		
//		Map<String, Double> modelParameters = Runner.getModelParams();
//		FeatureVector featVec = 
//			Runner.ALL_FEATURE_VECTORS[modelParameters.get(Runner.FEAT_VEC).intValue()];

		List<Double> fv = new ArrayList<Double>();
//		List<Double> fvNoTabInfo = new ArrayList<Double>();

		// Set modelDuration and direction
//		setModelDuration(argModelDuration);
//		setModelBackward(argModelBackward);
		boolean isBidirectional = false;
		Direction direction = Direction.LEFT;
		if (procMode == ProcessingMode.BWD) {
			direction = Direction.RIGHT;
		}

		// a. In the tablature case
		if (btp != null) {
			// 1. Note-level features 
			double[] basicNote = getBasicNoteFeatures(btp, bnp, meterInfo, noteIndex);
			for (int i = 0; i < 6; i++) {
				if (i != 3) {
					fv.add(basicNote[i]);
				}
			}

			// 2. Note-chord features
			double[] posInChord = 
				getPositionWithinChord(btp, durationLabels, bnp, direction, 
				noteIndex, argModelDuration, isBidirectional);
			if (featVec.getIntRep() > Runner.FeatureVector.PHD_A.getIntRep()) {
				// indexInChord, distToNoteBelow, distToNoteAbove
				for (int i = 1; i < 4; i++) {
					fv.add(posInChord[i]);
				}
			}

			// 3. Chord-level features
			if (featVec.getIntRep() > Runner.FeatureVector.PHD_B.getIntRep()) {
				// chordSize
				fv.add(posInChord[0]);
				// minDuration
				fv.add(basicNote[3]);
				// metPos
				fv.add(basicNote[6]);
				// numNewOnsets
				fv.add(basicNote[basicNote.length - 1]);
				// intervals
				for (int i = 4; i < posInChord.length; i++) {
					fv.add(posInChord[i]);
				}
			}

			// 4. Polyphonic embedding features
			if (featVec.getIntRep() > Runner.FeatureVector.PHD_C.getIntRep()) {
				// adjacentNoteOnSameCourse
				double[] adj = 
					getVoicesWithAdjacentNoteOnSameCourse(btp, transcription, direction, 
					noteIndex); 
				for (double d : adj) {
					fv.add(d);
				}
				// proximities
				List<double[][]> prox = 
					getPitchAndTimeProximitiesToAllVoices(btp, transcription, currentNote, 
					direction, argModelDuration, isBidirectional, decisionContextSize);
				for (double[][] pAndTProx : prox) {
					for (double[] p : pAndTProx) {
						for (double d : p) {
							fv.add(d);
						}
					}
				}
//				for (double[] p : prox) {
//					for (double d : p) {
//						fv.add(d);
//					}
//				}
				// alreadyOcc
				double[] alrOcc = 
					getVoicesAlreadyOccupied(btp, durationLabels, voicesCoDNotes, 
					bnp, voiceLabels, direction, noteIndex, argModelDuration, isBidirectional);
				for (double d : alrOcc) {
					fv.add(d);
				}	
			}
			// In case of D*: remove tablature features
			if (featVec == FeatureVector.PHD_D_STAR) {
				// Remove course, fret, maxDuration (features 1-3) 
				for (int i = 0; i < 3; i++) {
					fv.remove(1);
				}
				// Remove adjNoteOnSameCourse 
				List<Double> newFv = fv.subList(0, 16-3);
				newFv.addAll(fv.subList(21-3, fv.size()));
				fv = newFv;
		
//				fvNoTabInfo.add(fv.get(0));
////				fvNoTabInfo.add(fv.get(9));
//				for (int i = 4; i <= 15; i++) {
//					fvNoTabInfo.add(fv.get(i));
//				}
////				for (int i = 10; i <= 15; i++) {
////					fvNoTabInfo.add(fv.get(i));
////				}
//				for (int i = 21; i < fv.size(); i++) {
//					fvNoTabInfo.add(fv.get(i));
//				}
			}
		}
		// b. In the non-tablature case
		else if (bnp != null) {
			// 1. Note-level features 
			double[] basicNote = getBasicNoteFeatures(btp, bnp, meterInfo, noteIndex);
			for (int i = 0; i < 3; i++) {
				fv.add(basicNote[i]);
			}

			// 2. Note-chord features
			double[] posInChord = 
				getPositionWithinChord(btp, durationLabels, bnp, direction, 
				noteIndex, argModelDuration, isBidirectional);
			if (featVec.getIntRep() > Runner.FeatureVector.PHD_A.getIntRep()) {
				// indexInChord, distToNoteBelow, distToNoteAbove
				for (int i = 1; i < 4; i++) {
					fv.add(posInChord[i]);
				}
			}

			// 3. Chord-level features
			if (featVec.getIntRep() > Runner.FeatureVector.PHD_B.getIntRep()) {
				// chordSize
				fv.add(posInChord[0]);
				// metPos
				fv.add(basicNote[3]);
				// numNewOnsets
				fv.add(basicNote[basicNote.length - 1]);
				// intervals
				for (int i = 4; i < posInChord.length; i++) {
					fv.add(posInChord[i]);
				}
			}

			// 4. Polyphonic embedding features
			if (featVec.getIntRep() > Runner.FeatureVector.PHD_C.getIntRep()) {
				// proximities
				List<double[][]> prox = 
					getPitchAndTimeProximitiesToAllVoices(btp, transcription, currentNote, 
					direction, argModelDuration, isBidirectional, decisionContextSize);
				for (double[][] pAndTProx : prox) {
					for (double[] p : pAndTProx) {
						for (double d : p) {
							fv.add(d);
						}
					}
				}
//				for (double[] p : prox) {
//					for (double d : p) {
//						fv.add(d);
//					}
//				}
				// alreadyOcc
				double[] alrOcc = 
					getVoicesAlreadyOccupied(btp, durationLabels, voicesCoDNotes, 
					bnp, voiceLabels, direction, noteIndex, argModelDuration, isBidirectional);
				for (double d : alrOcc) {
					fv.add(d);
				}
			}
		}
//		return fv.subList(21, 41);
		return fv;		
	}

	
	/**
	 * 
	 * @param btp
	 * @param bnp
	 * @param transcription
	 * @param currentNote
	 * @param highestNumberOfVoicesTraining
	 * @param mp
	 * @return
	 */
	public static double[] generateMelodyModelOutput(Integer[][] btp, Integer[][] bnp,	Transcription transcription, 
		Note currentNote, int hiNumVoicesTraining, MelodyPredictor mp) { 

		Transcription.verifyCase(btp, bnp);

		// For every possible voice: model the probability of currentNote belonging to voice
		double[] voiceProbabilities = new double[Transcription.MAXIMUM_NUMBER_OF_VOICES];
		NotationSystem system = transcription.getPiece().getScore();
		for (int voice = 0; voice < hiNumVoicesTraining; voice++) {
//		for (int voice = 0; voice < Transcription.MAXIMUM_NUMBER_OF_VOICES; voice++) {
			List<List<Double>> allMfv = new ArrayList<List<Double>>();
//			if (voice < highestNumberOfVoicesTraining) {

			NotationVoice nv = system.get(voice).get(0); 
			// Get the melodyFeatureVectors for each note in voice up to currentNote
			allMfv = generateMelodyFeatureVectors(btp, nv, currentNote);
//			}
//			// If not: create allMfv like currentNote is the first note in that voice 
//			else { // added 23-3-16, but this gives trouble and therefore removed 7-4-16
//				NotationVoice nv = new NotationVoice();
//				allMfv = generateMelodyFeatureVectors(btp, nv, currentNote);
//			}

			// Model the probability of currentNote belonging to voice and set in voiceProbabilities			
//			voiceProbabilities[voice] = mp.modelProbability(allMfv, voice);
			voiceProbabilities[voice] = 
				mp.modelProbability(ToolBox.convertToListString(allMfv), voice);
		}
		for (int i = hiNumVoicesTraining; i < Transcription.MAXIMUM_NUMBER_OF_VOICES; i++) {
			voiceProbabilities[i] = 0.0;
		}

		// 3. Add voiceProbabilities to nfvPlus and return
//		List<Double> normalised = AuxiliaryTool.normalise(Arrays.asList(voiceProbabilities));
		double[] normalised = ToolBox.normaliseDoubleArray(voiceProbabilities);
		return normalised;
		
//		nfvPlus.addAll(Arrays.asList(voiceProbabilities));
//		mmOutput.addAll(normalised);
//		allVoiceProb.add(voiceProbabilities);

//		return nfvPlus;
//		return mmOutput;
	}
	
	
//	public List<Double> generateMelodyModelOutputOLD(Integer[][] btp, Integer[][] bnp, 
//		Transcription transcription, Note currentNote, int highestNumberOfVoicesTraining,
//		MelodyPredictor mp) { 
//
//		verifyCase(btp, bnp);
//
//		// 1. Get the note feature vector 
////		List<Double> nfvPlus = null;
////			generateNoteFeatureVectorDISS(btp, durationLabels, voicesCoDNotes,
////			bnp, transcription, currentNote, voiceLabels, meterInfo, noteIndex, argModelDuration, 
////			argModelBackward);
//		List<Double> mmOutput = new ArrayList<Double>();
//
//		// 2. For every possible voice: model the probability of currentNote belonging to voice
////		MelodyFeatureGenerator melodyFeatureGenerator = new MelodyFeatureGenerator(this);
//		Double[] voiceProbabilities = new Double[Transcription.MAXIMUM_NUMBER_OF_VOICES];
//		NotationSystem system = transcription.getPiece().getScore();
//		for (int voice = 0; voice < Transcription.MAXIMUM_NUMBER_OF_VOICES; voice++) {
//			List<List<Double>> allMfv = new ArrayList<List<Double>>();
////			// Determine numberOfVoices
////			int numberOfVoices = 0;
////			// In training or test mode, where highestNumberOfVoicesTraining == -1 
////			if (highestNumberOfVoicesTraining == -1) {
////				numberOfVoices = transcription.getNumberOfVoices();
////			}
////			// In application mode
////			else {
////				numberOfVoices = highestNumberOfVoicesTraining;
////			}
//			// If transcription contains voice: set allMfv; if not, allMfv remains an empty List
//	// zoek	
////			System.out.println("voice = " + voice);
////			if (voice < numberOfVoices) {
//			if (voice < highestNumberOfVoicesTraining) {
//				NotationVoice nv = system.get(voice).get(0); 
//				// Get the melodyFeatureVectors for each note in voice up to currentNote
//				allMfv = generateMelodyFeatureVectors(btp, nv, currentNote);
//			}
//			// If not: create allMfv like currentNote is the first note in that voice 
//			else { // TODO added 23-3
//				NotationVoice nv = new NotationVoice();
//				allMfv = generateMelodyFeatureVectors(btp, nv, currentNote);
//			}
//// zoek			
////			System.out.println("allMfv for this note (size " + allMfv.size() + ") = ");
////			System.out.println(allMfv);
//
//			// Model the probability of currentNote belonging to voice and set the corresponding element in 
//			// voiceProbabilities			
////			voiceProbabilities[voice] = mp.modelProbability(allMfv, voice);
//			voiceProbabilities[voice] = 
//				mp.modelProbability(AuxiliaryTool.convertToListString(allMfv), voice);
////			System.out.println(voiceProbabilities[voice]);
//		}
//
//		// 3. Add voiceProbabilities to nfvPlus and return
//		List<Double> normalised = AuxiliaryTool.normalise(Arrays.asList(voiceProbabilities));
//			
////		nfvPlus.addAll(Arrays.asList(voiceProbabilities));
//		mmOutput.addAll(normalised);
////		allVoiceProb.add(voiceProbabilities);
//
////		return nfvPlus;
//		return mmOutput;
//	}


	/**
	 * Generates the feature vector for the note at the given index.
	 * 
	 * @param btp
	 * @param durationLabels
	 * @param voicesCoDNotes
	 * @param bnp
	 * @param transcription
	 * @param currentNote
	 * @param voiceLabels
	 * @param meterInfo
	 * @param noteIndex
	 * @param highestNumberOfVoicesTraining
	 * @param argModelDuration
	 * @param argModelBackward

	 * @return
	 */
	private List<Double> generateNoteFeatureVectorPlus(Integer[][] btp, List<List<Double>> durationLabels,
		List<Integer[]> voicesCoDNotes, Integer[][] bnp, Transcription transcription, Note currentNote, 
		List<List<Double>> voiceLabels, List<Integer[]> meterInfo, int noteIndex, int highestNumberOfVoicesTraining,
		boolean argModelDuration, ProcessingMode procMode, FeatureVector featVec, 
		MelodyPredictor mp, int decisionContextSize) { 

		Transcription.verifyCase(btp, bnp);

		// 1. Get the note feature vector 
		List<Double> nfvPlus = generateNoteFeatureVectorDISS(btp, durationLabels, voicesCoDNotes,
			bnp, transcription, currentNote, voiceLabels, meterInfo, noteIndex, argModelDuration, 
			procMode, featVec, decisionContextSize);

		// 2. For every possible voice: model the probability of currentNote belonging to voice
//		MelodyFeatureGenerator melodyFeatureGenerator = new MelodyFeatureGenerator(this);
		Double[] voiceProbabilities = new Double[Transcription.MAXIMUM_NUMBER_OF_VOICES];
		NotationSystem system = transcription.getPiece().getScore();
		for (int voice = 0; voice < Transcription.MAXIMUM_NUMBER_OF_VOICES; voice++) {
			List<List<Double>> allMfv = new ArrayList<List<Double>>();
			// Determine numberOfVoices
			int numberOfVoices = 0;
			// In training or test mode, where highestNumberOfVoicesTraining == -1 
			if (highestNumberOfVoicesTraining == -1) {
				numberOfVoices = transcription.getNumberOfVoices();
			}
			// In application mode
			else {
				numberOfVoices = highestNumberOfVoicesTraining;
			}
			// If transcription contains voice: set allMfv; if not, allMfv remains an empty List
			if (voice < numberOfVoices) {
//			if (voice < highestNumberOfVoices) {
				NotationVoice nv = system.get(voice).get(0); 
				// Get the melodyFeatureVectors for each note in voice up to currentNote
//				allMfv = melodyFeatureGenerator.getMelodyModelFeatureVectors(btp, nv, currentNote);
				allMfv = generateMelodyFeatureVectors(btp, nv, currentNote);
			}
			// COMMENT IN
//			System.out.println("noteIndex = " + noteIndex + "; application: voice = " + voice);
//			System.out.println(allMfv);

			// Model the probability of currentNote belonging to voice and set the corresponding element in 
			// voiceProbabilities
//			voiceProbabilities[voice] = melodyPredictor.modelProbability(allMfv, voice);
			
//			voiceProbabilities[voice] = mp.modelProbability(allMfv, voice);
			voiceProbabilities[voice] = 
				mp.modelProbability(ToolBox.convertToListString(allMfv), voice);
//			System.out.println(voiceProbabilities[voice]);
		}

		// 3. Add voiceProbabilities to nfvPlus and return
		nfvPlus.addAll(Arrays.asList(voiceProbabilities));
//		allVoiceProb.add(voiceProbabilities);

		return nfvPlus;
	}


	/**
	 * Generates the feature vectors (unidir model).
	 *
	 * @param modelParameters
	 * @param btp
	 * @param voicesCoDNotes
	 * @param bnp
	 * @param transcription
	 * @param labels
	 * @param meterInfo
	 * @param chordSizes
	 * @return
	 */ 
	public static List<List<Double>> generateAllNoteFeatureVectors(Map<String, Double>
		modelParameters, Integer[][] btp, List<Integer[]> voicesCoDNotes, Integer[][] bnp,
		Transcription transcription, List<List<Double>> labels, List<Integer[]> meterInfo,
		List<Integer> chordSizes) {

		Transcription.verifyCase(btp, bnp);
		
		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()]; 
//		ModelType mt = m.getModelType();
//		DecisionContext dc = m.getDecisionContext(); 
		boolean modelDuration = m.getModelDuration();		
		ProcessingMode pm = 
			Runner.ALL_PROC_MODES[modelParameters.get(Runner.PROC_MODE).intValue()];
		FeatureVector featVec = 
			Runner.ALL_FEATURE_VECTORS[modelParameters.get(Runner.FEAT_VEC).intValue()];
		int decContSize = Runner.getModelParams().get(Runner.DECISION_CONTEXT_SIZE).intValue();
		
		List<List<Double>> allNoteFeatureVectors = new ArrayList<List<Double>>();  

		// Create voiceLabels and durationLabels
		List<List<Double>> voiceLabels = new ArrayList<List<Double>>();
		// When using the single-pass model not modelling duration, durationLabels remains null (as voicesCoDNotes)
		List<List<Double>> durationLabels = null;
		if (modelDuration) {
//		if (!argIsBidirectional && argModelDuration || argIsBidirectional) {
			durationLabels = new ArrayList<List<Double>>();
		}
		for (List<Double> currentLabel : labels) {
			voiceLabels.add(new ArrayList<Double>(currentLabel.subList(0, Transcription.MAXIMUM_NUMBER_OF_VOICES)));
			if (modelDuration) {
//			if (!argIsBidirectional && argModelDuration || argIsBidirectional) {
				durationLabels.add(new ArrayList<Double>(currentLabel.subList(Transcription.MAXIMUM_NUMBER_OF_VOICES,
					currentLabel.size())));
			}
		}

		// NB: Use of pre-set NoteSequence possible because this method is not used in application mode
		NoteSequence noteSeq = transcription.getNoteSequence(); 
		int numberOfNotes = noteSeq.size();
//		// a. In the tablature case
//		if (btp != null) {
//			numberOfNotes = btp.length; 
//		}
//		// b. In the non-tablature case
//		else {
//			numberOfNotes = bnp.length; 
//		}

		// For each note 
		for (int i = 0; i < numberOfNotes; i++) {
			// Determine noteIndex and currentNote
			int noteIndex = i;
			if (pm == ProcessingMode.BWD) {
				List<Integer> backwardsMapping = getBackwardsMapping(chordSizes);
				noteIndex = backwardsMapping.get(i);
			}
			Note currentNote = noteSeq.getNoteAt(noteIndex);
			// a. Single-pass model
//			if (!argIsBidirectional) {
//			if (mp == null) {
			allNoteFeatureVectors.add(generateNoteFeatureVectorDISS(btp, durationLabels, voicesCoDNotes,
				bnp, transcription, currentNote, voiceLabels, meterInfo, noteIndex, modelDuration, 
				pm, featVec, decContSize));
//			}
//			else {
//				int highestNumberOfVoicesTraining = -1;
//				allNoteFeatureVectors.add(generateNoteFeatureVectorPlus(btp, durationLabels, 
//					voicesCoDNotes, bnp, transcription, currentNote, voiceLabels, meterInfo, 
//					noteIndex, highestNumberOfVoicesTraining, argModelDuration, argModelBackward,
//					mp));
			
// zoek				
//				System.out.println("note = " + i); 
//				List<Double> bla = generateMelodyModelOutput(btp, bnp, transcription,
//					currentNote, highestNumberOfVoicesTraining, mp);
//				System.out.println("MM output for this note = ");
//				System.out.println(bla);
//				allNoteFeatureVectors.add(generateMelodyModelOutput(btp, bnp, transcription,
//					currentNote, highestNumberOfVoicesTraining, mp));
//			}
//			}
//			// b. Bi-directional model
//			else {
//				allNoteFeatureVectors.add(generateBidirectionalNoteFeatureVector(btp, durationLabels, voicesCoDNotes,
//					bnp, transcription, currentNote, voiceLabels, meterInfo, noteIndex, argModelDuration));
//			}
		}
//		if (mp != null) {
//			System.out.println(allNoteFeatureVectors);
//		}
		return allNoteFeatureVectors;
	}
	
	
	public static List<double[]> generateAllMMOutputs(Map<String, Double> modelParameters,
		Integer[][] btp, Integer[][] bnp, Transcription transcription, List<Integer> chordSizes, 
		MelodyPredictor mp) {
		
		List<double[]> allMMOutputs = new ArrayList<double[]>();
		
		ProcessingMode pm = 
			Runner.ALL_PROC_MODES[modelParameters.get(Runner.PROC_MODE).intValue()];
		
//		int highestNumberOfVoicesTraining =
//			modelParameters.get(Runner.HIGHEST_NUM_VOICES).intValue();
		int highestNumberOfVoicesTraining = Runner.getDataset().getHighestNumVoices();
		if (ToolBox.toBoolean(modelParameters.get(Runner.APPL_TO_NEW_DATA).intValue())) {
			highestNumberOfVoicesTraining = Runner.getDatasetTrain().getNumVoices();
		}
		
//		boolean modelBackward = 
//			ToolBox.toBoolean(modelParameters.get(Runner.MODEL_BACKWARD).intValue());

		// NB: Use of pre-set NoteSequence possible because this method is not used in application mode
		NoteSequence noteSeq = transcription.getNoteSequence(); 
		int numberOfNotes = noteSeq.size();
//		// a. In the tablature case
//		if (btp != null) {
//			numberOfNotes = btp.length; 
//		}
//		// b. In the non-tablature case
//		else {
//			numberOfNotes = bnp.length; 
//		}

		// For each note
		for (int i = 0; i < numberOfNotes; i++) {
			// Determine noteIndex and currentNote
			int noteIndex = i;
			if (pm == ProcessingMode.BWD) {
				List<Integer> backwardsMapping = getBackwardsMapping(chordSizes);
				noteIndex = backwardsMapping.get(i);
			}
			Note currentNote = noteSeq.getNoteAt(noteIndex);
			allMMOutputs.add(generateMelodyModelOutput(btp, bnp, transcription, currentNote, 
				highestNumberOfVoicesTraining, mp));
		}
		return allMMOutputs;
	}


	/**
	 * Gets for the given Note in the given NotationVoice the melody feature vector, containing
	 *   (0) the pitch of the Note
	 *   (1) the duration of the Note (in whole notes); in the tablature case this is the minimum duration
	 *   (2) the relative pitch (in semitones, negative if the previous Note is higher) to the previous Note
	 *   (3) the inter-onset interval (in whole notes) to the previous Note
	 *   
	 * If there is no previous Note in the given NotationVoice, relative pitch and inter-onset interval are set to 0.0
	 * and -1.0, respectively.
	 *       
	 * @param basicTabSymbolProperties      
	 * @param notationVoice
	 * @param note
	 * @return
	 */
	// TESTED for both tablature- and non-tablature case
	static List<Double> generateMelodyFeatureVector(Integer[][] btp, NotationVoice notationVoice, 
		Note note) {
		List<Double> mfv = new ArrayList<Double>();

		// Get the pitch, onset time, and duration of note 
		int pitch = note.getMidiPitch();
		Rational metricTime = note.getMetricTime();
		Rational metricDuration = null;
		// a. In the tablature case: the minimum duration
		if (btp != null) {
			metricDuration = Tablature.getMinimumDurationOfNote(btp, note);
		}
		// b. In the non-tablature case: the full duration
		else {
			metricDuration = note.getMetricDuration();
		}

		// 2. Fill mfv
		// Add pitch and duration of the current note
		mfv.add((double) pitch);
		mfv.add(metricDuration.toDouble());
		// Add relative pitch and ioi to the previous note
//		Note previousNote = fg.getAdjacentNoteInVoice(notationVoice, note, Direction.LEFT);
		Note previousNote = 
			Transcription.getAdjacentNoteInVoice(notationVoice, note, true);
		if (previousNote != null) {
			mfv.add((double) (pitch - previousNote.getMidiPitch()));
//			mfv.add((double) 2*((pitch - previousNote.getMidiPitch())));
//			mfv.add((double) Math.abs((pitch - previousNote.getMidiPitch())));
			mfv.add(metricTime.sub(previousNote.getMetricTime()).toDouble());
		}
		else {
			mfv.addAll(Arrays.asList(new Double[]{0.0, -1.0}));
		}
//		Collections.swap(mfv, 0, 2);

		return mfv;
	}


	/**
	 * Gets the melody feature vectors in the given NotationVoice. There are two options:<br>
	 * 1. If note is <code>null</code>: returns the mfv for all notes in the given NotationVoice. 
	 *    This is the training case. <br> 
	 * 2. If note is not <code>null</code>: returns the mfv for all Notes (if any) in the given NotationVoice whose 
	 *    metric time is smaller than the metric time of the given Note, plus the melody feature vector for the given
	 *    Note, as if it were also in the given NotationVoice. This is the test/application case.
	 *     
	 * @param btp
	 * @param notationVoice
	 * @param note
	 * @return
	 */
	// TESTED for both tablature- and non-tablature case
	public static List<List<Double>> generateMelodyFeatureVectors(Integer[][] btp, 
		NotationVoice notationVoice, Note note) {
		List<List<Double>> allMfv = new ArrayList<List<Double>>();

		// All mfv
		if (note == null) {
			for (int i = 0; i < notationVoice.size(); i++) {				
				Note n = notationVoice.get(i).get(0);
				allMfv.add(generateMelodyFeatureVector(btp, notationVoice, n));
			}
		}		
		// All mfv up to note
		else  {
			// 0. Get the pitch, onset time, and duration of note
			int pitch = note.getMidiPitch();
			Rational metricTime = note.getMetricTime(); 
			Rational metricDuration = null;
			// a. In the tablature case
			if (btp != null) {
				metricDuration = Tablature.getMinimumDurationOfNote(btp, note);
			}
			// b. In the non-tablature case
			else {
				metricDuration = note.getMetricDuration();
			}

			// 1. If notationVoice is not empty: create the mfvs for all notes before note (if any) and add them to allMfv
			if (notationVoice.size() != 0) { 
				for (int i = 0; i < notationVoice.size(); i++) {
					Note previousNote = notationVoice.get(i).get(0);
					// Only if previousNote is before the note at noteIndex
					if (previousNote.getMetricTime().isLess(metricTime)) {
						allMfv.add(generateMelodyFeatureVector(btp, notationVoice, previousNote));
					}
					else {
						break;
					}		
				}
			}
			// 2. Create the mfv for note, assuming that it is in voice
			List<Double> mfv = new ArrayList<Double>();
			mfv.add((double) pitch);
			mfv.add(metricDuration.toDouble());
			// a. If there are no Notes before note: add default values
			if (allMfv.size() == 0) {
				mfv.addAll(Arrays.asList(new Double[]{0.0, -1.0}));
			}
			// b. If there are Notes before note
			else {
//				Note previousNote = fg.getAdjacentNoteInVoice(notationVoice, note, Direction.LEFT);
				Note previousNote = 
					Transcription.getAdjacentNoteInVoice(notationVoice, note, true);
				mfv.add((double) (pitch - previousNote.getMidiPitch()));
				mfv.add(metricTime.sub(previousNote.getMetricTime()).toDouble());	
			}

			// 3. Add mfv to allMfv
			allMfv.add(mfv);
		}
		return allMfv;
	}


	/**
	 * Generates the feature vector for the note at the given index (bidir model).
	 * 
	 * @param btp
	 * @param predictedDurationLabels
	 * @param predictedVoicesCoDNotes
	 * @param bnp
	 * @param predictedTranscription
	 * @param currentNote
	 * @param predictedVoiceLabels
	 * @param meterInfo
	 * @param noteIndex
	 * @param argModelBackward
	 * @param decisionContextSize
	 * @return 
	 */
	// TESTED for both tablature and non-tablature case
	public static List<Double> generateBidirectionalNoteFeatureVector(Integer[][] btp, 
		List<List<Double>> predictedDurationLabels, List<Integer[]> predictedVoicesCoDNotes, 
		Integer[][] bnp, Transcription predictedTranscription, Note currentNote, 
		List<List<Double>> predictedVoiceLabels, List<Integer[]> meterInfo, int noteIndex,
		boolean argModelDuration, int decisionContextSize) { 

		Transcription.verifyCase(btp, bnp);

		List<Double> noteFeatureVector = new ArrayList<Double>();

		boolean isBidirectional = true;
//		setModelDuration(argModelDuration);
		
		// List the four feature sets. The correct features are automatically calculated for tablature- (non-dur/dur) 
		// and non-tablature case depending on values of btp and bnp and argModelDuration)
		// 1. Note-level features
		List<Double> noteLevel = new ArrayList<Double>();
//		double[] tabFeatures = getBasicNoteFeatures(btp, bnp, meterInfo, noteIndex);
		double[] basicNoteFeatures = getBasicNoteFeatures(btp, bnp, meterInfo, noteIndex);
		if (btp != null) {
			for (int i = 0; i < 6; i++) {
				if (i != 3) {
					noteLevel.add(basicNoteFeatures[i]);
				}
			}
		}
		else {
			for (int i = 0; i < 3; i++) {
				noteLevel.add(basicNoteFeatures[i]);
			}
		}

		// 2. Note-chord features
		List<Double> noteChord = new ArrayList<Double>();
		double[] posInChord = 
			getPositionWithinChord(btp, predictedDurationLabels, bnp, null, 
			noteIndex, argModelDuration, isBidirectional);
		for (int i = 1; i < 4; i++) {
			noteChord.add(posInChord[i]);
		}
		
		// 3. Chord-level features
		List<Double> chordLevel = new ArrayList<Double>();
		if (btp != null) {
			chordLevel.add(posInChord[0]); // chordSize
			chordLevel.add(basicNoteFeatures[3]); // minDuration;
			chordLevel.add(basicNoteFeatures[6]); // metPos;
			chordLevel.add(basicNoteFeatures[basicNoteFeatures.length - 1]); // numNewOnsets;
			for (int i = 4; i < posInChord.length; i++) {
				chordLevel.add(posInChord[i]); // intervals;
			}
		}
		else {
			chordLevel.add(posInChord[0]); // chordSize
			chordLevel.add(basicNoteFeatures[3]); // metPos;
			chordLevel.add(basicNoteFeatures[basicNoteFeatures.length - 1]); // numNewOnsets;
			for (int i = 4; i < posInChord.length; i++) {
				chordLevel.add(posInChord[i]); // intervals;
			}
		}
		
		// 4. Polyphonic embedding features
		List<double[]> polyEmb = new ArrayList<double[]>();
		// a. To previous notes
		// Voices with previous note on same course
		if (btp != null) {  
			polyEmb.add(getVoicesWithAdjacentNoteOnSameCourse(btp, predictedTranscription, Direction.LEFT, 
				noteIndex));
		}
		// Pitch- and time proximities to previous notes
//		System.out.println("noteIndex = " + noteIndex); bla
		List<double[][]> pitchAndTimeProximitiesToPrevious = 
			getPitchAndTimeProximitiesToAllVoices(btp, predictedTranscription, currentNote, 
			Direction.LEFT, argModelDuration, isBidirectional, decisionContextSize);
		for (double[][] pAndTProx : pitchAndTimeProximitiesToPrevious) {
			for (double[] d : pAndTProx) {
				polyEmb.add(d);
			}
		}
//		for (double[] d : pitchAndTimeProximitiesToPrevious) {
//			polyEmb.add(d);
//		}
		// b. To next notes
		// Voices with next note on same course
		if (btp != null) {  
			polyEmb.add(getVoicesWithAdjacentNoteOnSameCourse(btp, predictedTranscription, Direction.RIGHT, 
				noteIndex));
		}
		// Pitch- and time proximities to next notes
		List<double[][]> pitchAndTimeProximitiesToNext = 
			getPitchAndTimeProximitiesToAllVoices(btp, predictedTranscription, currentNote, 
			Direction.RIGHT, argModelDuration, isBidirectional, decisionContextSize);
		for (double[][] pAndTProx : pitchAndTimeProximitiesToNext) {
			for (double[] d : pAndTProx) {
				polyEmb.add(d);
			}
		}
//		for (double[] d : pitchAndTimeProximitiesToNext) {
//			polyEmb.add(d);
//		}
		// Voices already occupied
		polyEmb.add(getVoicesAlreadyOccupied(btp, predictedDurationLabels, 
			predictedVoicesCoDNotes, bnp, predictedVoiceLabels, null, 
			noteIndex, argModelDuration, isBidirectional));
		// Concatenate the double[]s in the List
		double[] polyphonicEmbedding = ToolBox.concatDoubleArrays(polyEmb);

		// Combine the three feature sets in noteFeatureVector and return it  
//		double[] allFeatures = AuxiliaryTool.concatDoubleArrays(Arrays.asList(new double[][]{tabFeatures, 
//			tabDurFeatures, tabDurVoiceFeatures}));
//		for (double d : allFeatures) {
//			noteFeatureVector.add(d);
//		}
		noteFeatureVector.addAll(noteLevel);
		noteFeatureVector.addAll(noteChord);
		noteFeatureVector.addAll(chordLevel);
		for (double d: polyphonicEmbedding) {
			noteFeatureVector.add(d);
		}
		
//		if (noteIndex == 179) {
//			System.out.println(noteFeatureVector);
//			System.exit(0);
//		}
		return noteFeatureVector;
	}


	/**
	 * Generates the bi-directional feature vector for the note at the given index.
	 * 
	 * @param btp
	 * @param predictedDurationLabels
	 * @param predictedVoicesCoDNotes
	 * @param bnp
	 * @param predictedTranscription
	 * @param currentNote
	 * @param predictedVoiceLabels
	 * @param meterInfo
	 * @param noteIndex
	 * @param argModelBackward
	 * @return 
	 */
	// TESTED for both tablature and non-tablature case
	public static List<Double> generateBidirectionalNoteFeatureVectorOLD(Integer[][] btp, List<List<Double>>
		predictedDurationLabels, List<Integer[]> predictedVoicesCoDNotes, Integer[][] bnp, Transcription 
		predictedTranscription, Note currentNote, List<List<Double>> predictedVoiceLabels, List<Integer[]> 
		meterInfo, int noteIndex, int decisionContextSize) { //, boolean argModelBackward) { 

		Transcription.verifyCase(btp, bnp);

		List<Double> noteFeatureVector = new ArrayList<Double>();

		// Set isBiDirectional and modelDuration 
//		setIsBidirectional(true);
		boolean argIsBidir = true;
//		setModelDuration(true);
		boolean modelDuration = true;
//		setModelBackward(argModelBackward);
		
		// List the three feature sets. The correct features are automatically calculated for tablature- (non-dur/dur) and 
		// non-tablature case depending on values of btp and bnp and argModelDuration)
		// 1. Tablature features 
		double[] tabFeatures = getBasicNoteFeatures(btp, bnp, meterInfo, noteIndex); 

		// 2. Tablature + duration features
		double[] tabDurFeatures = 
			getPositionWithinChord(btp, predictedDurationLabels, bnp, null, 
			noteIndex, modelDuration, argIsBidir);

		// 3. Tablature + duration + voice features
		List<double[]> tabDurVoice = new ArrayList<double[]>();
		// a. To previous notes
		// Voices with previous note on same course
		if (btp != null) {  
			tabDurVoice.add(getVoicesWithAdjacentNoteOnSameCourse(btp, 
				predictedTranscription, Direction.LEFT, noteIndex));
		}
		// Pitch- and time proximities to previous notes
		List<double[][]> pitchAndTimeProximitiesToPrevious = 
			getPitchAndTimeProximitiesToAllVoices(btp, predictedTranscription, currentNote, 
			Direction.LEFT, modelDuration, argIsBidir, decisionContextSize);
		for (double[][] pAndTProx : pitchAndTimeProximitiesToPrevious) {
			for (double[] d : pAndTProx) {
				tabDurVoice.add(d);
			}
		}
//		for (double[] d : pitchAndTimeProximitiesToPrevious) {
//			tabDurVoice.add(d);
//		}
		// b. To next notes
		// Voices with next note on same course
		if (btp != null) {  
			tabDurVoice.add(getVoicesWithAdjacentNoteOnSameCourse(btp, predictedTranscription, Direction.RIGHT, 
				noteIndex));
		}
		// Pitch- and time proximities to next notes
		List<double[][]> pitchAndTimeProximitiesToNext = 
			getPitchAndTimeProximitiesToAllVoices(btp, predictedTranscription, currentNote, 
			Direction.RIGHT, modelDuration, argIsBidir, decisionContextSize);
		for (double[][] pAndTProx : pitchAndTimeProximitiesToNext) {
			for (double[] d : pAndTProx) {
				tabDurVoice.add(d);
			}
		}
//		for (double[] d : pitchAndTimeProximitiesToNext) {
//			tabDurVoice.add(d);
//		}
		// Voices already occupied
		tabDurVoice.add(getVoicesAlreadyOccupied(btp, predictedDurationLabels, 
			predictedVoicesCoDNotes, bnp, predictedVoiceLabels, null, 
			noteIndex, modelDuration, argIsBidir));
		// Concatenate the double[]s in the List
		double[] tabDurVoiceFeatures = ToolBox.concatDoubleArrays(tabDurVoice);

		// Combine the three feature sets in noteFeatureVector and return it  
		double[] allFeatures = ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{tabFeatures, 
			tabDurFeatures, tabDurVoiceFeatures}));
		for (double d : allFeatures) {
			noteFeatureVector.add(d);
		}
		return noteFeatureVector;
	}


	/**
	 * Generates the feature vectors (bidir model).
	 *
	 * @param btp
	 * @param predictedVoicesCoDNotes
	 * @param bnp
	 * @param predictedTranscription
	 * @param predictedLabels
	 * @param meterInfo
	 * @param chordSizes
	 * @param argModelDuration 
	 * @param decisionContextSize
	 * @return
	 */ 
	public static List<List<Double>> generateAllBidirectionalNoteFeatureVectors(
		Integer[][] btp, List<Integer[]> predictedVoicesCoDNotes, Integer[][] bnp, 
		Transcription predictedTranscription, List<List<Double>> predictedLabels, 
		List<Integer[]> meterInfo, List<Integer> chordSizes, boolean argModelDuration,
		int decisionContextSize) {

		Transcription.verifyCase(btp, bnp);

		List<List<Double>> allBidirNoteFeatureVectors = new ArrayList<List<Double>>();  

	 	// Create voiceLabels and durationLabels
		List<List<Double>> predictedVoiceLabels = new ArrayList<List<Double>>();
		List<List<Double>> predictedDurationLabels = null;
		if (argModelDuration) {
			predictedDurationLabels = new ArrayList<List<Double>>();
		}
		for (List<Double> currentLabel : predictedLabels) {
			predictedVoiceLabels.add(new ArrayList<Double>(currentLabel.subList(0, Transcription.MAXIMUM_NUMBER_OF_VOICES)));
			if (argModelDuration) {
				predictedDurationLabels.add(new ArrayList<Double>(currentLabel.subList(Transcription.MAXIMUM_NUMBER_OF_VOICES,
					currentLabel.size())));
			}
		}

		// NB: Use of pre-set NoteSequence possible because this method is not used in application mode
		NoteSequence noteSeq = predictedTranscription.getNoteSequence(); 
		int numberOfNotes;
		// a. In the tablature case
		if (btp != null) {
			numberOfNotes = btp.length; 
		}
		// b. In the non-tablature case
		else {
			numberOfNotes = bnp.length; 
		}

		// For each note 
		for (int i = 0; i < numberOfNotes; i++) {
			// Determine noteIndex and currentNote
			int noteIndex; 
//			if (argModelBackward) {
//				List<Integer> backwardsMapping = Transcription.getBackwardsMapping(chordSizes);
//				noteIndex = backwardsMapping.get(i);
//			}
//			else {
				noteIndex = i;
//			}
			Note currentNote = noteSeq.getNoteAt(noteIndex);
			// Add the current feature vector to allBidirNoteFeatureVectors
			allBidirNoteFeatureVectors.add(generateBidirectionalNoteFeatureVector(btp, 
				predictedDurationLabels, predictedVoicesCoDNotes, bnp, predictedTranscription,
				currentNote, predictedVoiceLabels, meterInfo, noteIndex, argModelDuration,
				decisionContextSize));
		}

		return allBidirNoteFeatureVectors;
	}
	
	
	// =================================== SCALING ===================================	
	static List<Integer> indicesOfPitchMvmt = null;
	static int indexOfPitchVoiceRel = -1;
	public static void determineScalingSettings(ModellingApproach argModellingApproach, 
		boolean argIsTablatureCase) { // TODO fix hardcoded indices
		
		if (argModellingApproach == ModellingApproach.C2C) {
			// a. In the tablature case
			if (argIsTablatureCase) {
				if (Transcription.MAXIMUM_NUMBER_OF_VOICES == 5) {
					indicesOfPitchMvmt = Arrays.asList(new Integer[]{47, 48, 49, 50, 51});
					indexOfPitchVoiceRel = 57;
				}
				else if (Transcription.MAXIMUM_NUMBER_OF_VOICES == 4) {
					indicesOfPitchMvmt = Arrays.asList(new Integer[]{38, 39, 40, 41});
					indexOfPitchVoiceRel = 46;
				}
			}
			// b. In the non-tablature case
			else {
				if (Transcription.MAXIMUM_NUMBER_OF_VOICES == 5) {
					indicesOfPitchMvmt = Arrays.asList(new Integer[]{36, 37, 38, 39, 40});
					indexOfPitchVoiceRel = 46;
				}
				else if (Transcription.MAXIMUM_NUMBER_OF_VOICES == 4) {
					indicesOfPitchMvmt = Arrays.asList(new Integer[]{29, 30, 31, 32});
					indexOfPitchVoiceRel = 37;
				}
			}
		}
	}
	
	
	static List<Integer> indicesInMapping = null;
	static int indexOfMetPos = -1;
	static List<Integer> indicesOfProx = null;
	static List<Integer> indicesOfAlreadyOcc = null;
	private static void determineScalingSettingsEuroMAC(int argLearningApproach, boolean argIsTablatureCase, int config) {
//		learningApproach = argLearningApproach;
		
		// Config 1: 4vv (max 4vv), full proximities
		// Config 3: 4vv (max 4vv), avg proximities
		if (config == 1 || config == 3) {
			indicesInMapping = Arrays.asList(new Integer[]{0, 4, 8, 12});
			indexOfMetPos = 18;
			if (config == 1) {
				indicesOfProx = new ArrayList<Integer>();
				for (int i = 22; i < 34; i++) {
					indicesOfProx.add(i);
				}
				indicesOfPitchMvmt = Arrays.asList(new Integer[]{34, 35, 36, 37});
				indicesOfAlreadyOcc = Arrays.asList(new Integer[]{38, 39, 40, 41});
				indexOfPitchVoiceRel = 42;
			}
			if (config == 3) {
				indicesOfProx = Arrays.asList(new Integer[]{22, 23, 24});
				indicesOfPitchMvmt = Arrays.asList(new Integer[]{25, 26, 27, 28});
				indicesOfAlreadyOcc = Arrays.asList(new Integer[]{29, 30, 31, 32});
				indexOfPitchVoiceRel = 33;
			}
		}
		// Config 2: 4vv (max 5vv), full proximities
		// Config 4: 4vv (max 5vv), avg proximities
		else if (config == 2 || config == 4) {
			indicesInMapping = Arrays.asList(new Integer[]{0, 4, 8, 12, 16});
			indexOfMetPos = 22;
			if (config == 2) {
				indicesOfProx = new ArrayList<Integer>();
//				for (int i = 32; i < 42; i++) {
				for (int i = 27; i < 42; i++) {
					indicesOfProx.add(i);
				}
				indicesOfPitchMvmt = Arrays.asList(new Integer[]{42, 43, 44, 45, 46});
				indicesOfAlreadyOcc = Arrays.asList(new Integer[]{47, 48, 49, 50, 51});
				indexOfPitchVoiceRel = 52;
			}
			if (config == 4) {
				indicesOfProx = Arrays.asList(new Integer[]{27, 28, 29});
				indicesOfPitchMvmt = Arrays.asList(new Integer[]{30, 31, 32, 33, 34});
				indicesOfAlreadyOcc = Arrays.asList(new Integer[]{35, 36, 37, 38, 39});
				indexOfPitchVoiceRel = 40;
			}
		}
	}
	
		
	/**
	 * Scales every feature value in the given feature vector so that it ranges between 0 and 1, using the 
	 * following formula:
	 * 
	 *   f' = (f - min(f)) / (max(f) - min(f)) 
	 * 
	 * where f is the unadapted value of the feature, and f' its scaled value.  
	 * 
	 * @param featureVector 
	 * @param minAndMaxFeatureValues 
	 */
	// TESTED
	private static void scaleFeatureVectorEuroMAC(List<Double> featureVector, double[][] minAndMaxFeatureValues,
		ModellingApproach argModellingApproach) {

		// OLD VERSION: scale everything, also -1 default values
//		for (int i = 0; i < featureVector.size(); i++) {
//			double f = featureVector.get(i);
//			double minValue = minAndMaxFeatureValues[0][i];
//			double maxValue = minAndMaxFeatureValues[1][i];
//			double fScaled = 0.0;
//			// If min- and maxValue are equal, the denominator will be 0. In this case, fScaled retains its initial
//			// value 0.0. In all other cases: calculate fScaled
//			if (minValue != maxValue) {
//				fScaled = (f - minValue) / (maxValue - minValue);
//			}
//			featureVector.set(i, fScaled);
//		}
		
		// NEW VERSION: N2N
		if (argModellingApproach == ModellingApproach.N2N) {			
			for (int i = 0; i < featureVector.size(); i++) {
				double f = featureVector.get(i);
				double fScaled = -1;
				// Scale only if f is not -1
				if (f != -1) {
					double minF = minAndMaxFeatureValues[0][i];
					double maxF = minAndMaxFeatureValues[1][i];
					// If minF and maxF are not equal
					if (minF != maxF) {
						fScaled = (f - minF) / (maxF - minF);
					}
					// If minF and maxF are equal
					// NB: This is only so at the last items of voicesWithAdjNoteOnSameCourse (if applicable) and
					// voiceAlreadyOccupied
					else {
						fScaled = 0.0;
//						System.out.println("i = " + i "; minF = " + minF + "; maxF = " + maxF");
//						System.out.println("Division by 0 in feature scaling.");			    		
//						throw new RuntimeException("ERROR: Scaling error (see console).");
					}	
				}
				// Replace f with fScaled
				featureVector.set(i, fScaled);	
			}
		}

		// C2C
		if (argModellingApproach == ModellingApproach.C2C) {
			for (int i = 0; i < featureVector.size(); i++) {
				double f = featureVector.get(i);
				double minF = minAndMaxFeatureValues[0][i];
				double maxF = minAndMaxFeatureValues[1][i];
				double fScaled;
				// 1. If f is an indexInMapping: scale by max index
				if (indicesInMapping.contains(i)) {
//					int maxIndex = Transcription.MAXIMUM_NUMBER_OF_VOICES - 1 - 1; // TODO
					int maxIndex = Transcription.MAXIMUM_NUMBER_OF_VOICES - 1; // TODO
					if (f == -1) {
						fScaled = f;
					}
					else {
						fScaled = f/maxIndex; 
//						fScaled = ( (f/maxIndex) - 0.5) * 2;
					}
				}
				// 2. If f is one of the other features
				else {
					// a. If f can legitimately be -1: scale
					if (indicesOfPitchMvmt.contains(i)) {
						if (minF != maxF) {
							fScaled = (f - minF) / (maxF - minF);
//							fScaled = ( (f - minF) / (maxF - minF) - 0.5) * 2;
						}
						// If minF == maxF == 0 (when the voice is not active): scale to -1.0
						// NB: If f == 0, this can mean:
						// a. The voice is active but has no previous note (first chord)
						// b. The voice is active and has a previous note with the same pitch
						// c. The voice is inactive
						// Inconsistency: a. and b. above get scaled to scaled to (0.0 - minF) / (maxF - minF);
						// c. gets scaled automatically to -1.0
						else {
//							System.out.println("fMin = fMax = " + minF);
							fScaled = -1.0;  
						}
					}
					else if (i == indexOfPitchVoiceRel) {
						fScaled = (f - minF) / (maxF - minF);
//						fScaled = ( (f - minF) / (maxF - minF) - 0.5) * 2;
					}
					// b. If not: scale only if f is not -1
					else {
						if (f == -1) {
							fScaled = f; 
						}
						else {
							if (minF != maxF) {
								fScaled = (f - minF) / (maxF - minF);
//								fScaled = ( (f - minF) / (maxF - minF) - 0.5) * 2;
							}
							// NB: else happens only in alreadyOccupied; set to equivalent of 0
							else {
//								System.out.println("i = " + i);
//								System.out.println("minF = " + minF);
//								System.out.println("maxF = " + maxF);
								fScaled = -1.0;
//					    		System.out.println("Division by 0 in feature scaling.");
//					    		throw new RuntimeException("ERROR: Scaling error (see console).");
							}
						}
					}
				}
				// Replace f with fScaled
				featureVector.set(i, fScaled);
			}
		}
	}

	
	/**
	 * Scales every feature value in the given feature vector so that it ranges between 0 and 1, using the 
	 * following formula:
	 * 
	 *   f' = (f - min(f)) / (max(f) - min(f)) 
	 * 
	 * where f is the unadapted value of the feature, and f' its scaled value.  
	 * 
	 * @param featureVector 
	 * @param minAndMaxFeatureValues 
	 */
	// TESTED
	public static List<Double> scaleFeatureVector(List<Double> featureVector, double[][] minAndMaxFeatureValues,
		ModellingApproach argModellingApproach) {

		List<Double> featureVectorScaled = new ArrayList<Double>(); // toegevoegd
		for (int i = 0; i < featureVector.size(); i++) {
			featureVectorScaled.add(-1.0);
		}

		// N2N
		if (argModellingApproach == ModellingApproach.N2N) {
			for (int i = 0; i < featureVector.size(); i++) {
				double f = featureVector.get(i);
				double fScaled = -1;
				// Scale only if f is not -1
				if (f != -1) {
					double minF = minAndMaxFeatureValues[0][i];
					double maxF = minAndMaxFeatureValues[1][i];
					// If minF and maxF are not equal
					if (minF != maxF) {
						fScaled = (f - minF) / (maxF - minF);
					}
					// If minF and maxF are equal
					// NB: This is only so at the last items of voicesWithAdjNoteOnSameCourse (if applicable) and
					// voiceAlreadyOccupied
					else {
						fScaled = 0.0;
//						System.out.println("i = " + i "; minF = " + minF + "; maxF = " + maxF");
//						System.out.println("Division by 0 in feature scaling.");			    		
//						throw new RuntimeException("ERROR: Scaling error (see console).");
					}	
				}
				// Replace f with fScaled
//				featureVector.set(i, fScaled); vervangen 1/9
				featureVectorScaled.set(i, fScaled);
			}
		}

		// C2C
		if (argModellingApproach == ModellingApproach.C2C) {
			for (int i = 0; i < featureVector.size(); i++) {
				double f = featureVector.get(i);
				double minF = minAndMaxFeatureValues[0][i];
				double maxF = minAndMaxFeatureValues[1][i];
				double fScaled = -1;
				// a. If f can legitimately be -1: scale
				if (indicesOfPitchMvmt.contains(i)) {
					if (minF != maxF) {
						fScaled = (f - minF) / (maxF - minF);
					}
					// If minF == maxF == 0 (when the voice is not active): scale to -1.0
					// NB: If f == 0, this can mean:
					// a. The voice is active but has no previous note (first chord)
					// b. The voice is active and has a previous note with the same pitch
					// c. The voice is inactive
					// Inconsistency: a. and b. above are scaled to (0.0 - minF) / (maxF - minF); c. is
					// scaled automatically to -1.0
					else {
//						System.out.println("fMin = fMax = " + minF);
						fScaled = -1.0;  
					}
				}
				else if (i == indexOfPitchVoiceRel) {
					fScaled = (f - minF) / (maxF - minF);
				}
				// b. If f cannot be -1 legitimately (i.e., gets -1 as n/a value or cannot be -1 at all):
				// get min and max only if f is not -1
				else {
					if (f != -1) {
						if (minF != maxF) {
							fScaled = (f - minF) / (maxF - minF);
						}
						// NB: else happens only in alreadyOccupied; set to equivalent of 0
						else {
//								System.out.println("i = " + i);
//								System.out.println("minF = " + minF);
//								System.out.println("maxF = " + maxF);
							fScaled = 0.0;
//					    		System.out.println("Division by 0 in feature scaling.");
//					    		throw new RuntimeException("ERROR: Scaling error (see console).");
						}
					}
				}
//				}
				// Replace f with fScaled
//				featureVector.set(i, fScaled); // vervangen 1/9
				featureVectorScaled.set(i, fScaled);
			}
		}
		return featureVectorScaled;
	}

	
//	/**
//	 * Using the given minimum and maximum values, scales every feature value in the given set of feature vectors
//	 * so that it ranges between 0 and 1. 
//	 *  
//	 * @param set 
//	 * @param minMax 
//	 */
//	// TESTED
//	private static void scaleSetOfFeatureVectorsOLD(List<List<Double>> set, double[][] minMax, int argLearningApproach) {								
//		for (List<Double> l : set) {
//			scaleFeatureVector(l, minMax, argLearningApproach);
//		}
//	}
	
	
	/**
	 * Using the given minimum and maximum values, scales every feature value in the given set of feature vectors
	 * so that it ranges between 0 and 1. 
	 *  
	 * @param set 
	 * @param minMax 
	 */
	// TESTED
	public static List<List<Double>> scaleSetOfFeatureVectors(List<List<Double>> set, 
		double[][] minMax, ModellingApproach argModellingApproach) {								
		List<List<Double>> scaled = new ArrayList<List<Double>>();
		for (List<Double> l : set) {
			scaled.add(scaleFeatureVector(l, minMax, argModellingApproach));
		}
		return scaled;
	}


//	/**
//	 * Using the given minimum and maximum values, scales every feature value in the given set of sets of feature vectors
//	 * so that it ranges between 0 and 1. 
//	 * 
//	 * @param setOfSets 
//	 * @param minMax
//	 */
//	// TESTED
//	private static void scaleSetOfSetsOfFeatureVectorsOLD(List<List<List<Double>>> setOfSets, double[][] minMax,
//		int argLearningApproach) {
//		for (List<List<Double>> l : setOfSets) {
//			scaleSetOfFeatureVectors(l, minMax, argLearningApproach);
//		}
//	}
	
	
	/**
	 * Using the given minimum and maximum values, scales every feature value in the given set of sets of feature vectors
	 * so that it ranges between 0 and 1. 
	 * 
	 * @param setOfSets 
	 * @param minMax
	 */
	// TESTED
	public static List<List<List<Double>>> scaleSetOfSetsOfFeatureVectors(List<List<List<Double>>> setOfSets, double[][] minMax,
		ModellingApproach argModellingApproach) {
		List<List<List<Double>>> scaled = new ArrayList<List<List<Double>>>();
		for (List<List<Double>> l : setOfSets) {
			scaled.add(scaleSetOfFeatureVectors(l, minMax, argModellingApproach));
		}
		return scaled;
	}


	/**
	 * Gets the minimum and maximum value for each feature from the given List of feature vectors. Returns a double[][]
	 * containing as the first element a double[] with the minimum values for each feature, and as the second a double[]
	 * with the maximum values. 
	 * 
	 * @param set
	 * @param argLearningApproach
	 * @return 
	 */
	// TESTED
	public static double[][] getMinAndMaxValuesSetOfFeatureVectors(List<List<Double>> set, 
		ModellingApproach argModellingApproach) {

		int numFeatures = set.get(0).size();
		double[][] minAndMax = new double[2][numFeatures];

		if (argModellingApproach == ModellingApproach.N2N) {
			// For each feature at index i 
			for (int i = 0; i < numFeatures; i++) {
				double minValue = Double.MAX_VALUE;
				double maxValue = -Double.MAX_VALUE;
				// For each feature vector
				for (List<Double> fv : set) {
					double f = fv.get(i); 
					// Only if the current value is not -1 
					if (f != -1) {
						if (f < minValue) {
							minValue = f;
						}
						if (f > maxValue) {
							maxValue = f;
						}
					}
				}	
				
				// All feature vectors traversed? Set the min and max feature values at index i in minAndMax.
				// Features that are always -1 get Double.MAX_VALUE as minValue and -Double.MAX_VALUE as maxValue
				minAndMax[0][i] = minValue;
				minAndMax[1][i] = maxValue;
			}
		}
		
		if (argModellingApproach == ModellingApproach.C2C) {
			// List features that can legitimately be -1
//			List<Integer> indicesOfMvmts = new ArrayList<Integer>();
//			int indexOfPitchVoiceRelation = -1;
			// For each feature at index i 
			for (int i = 0; i < numFeatures; i++) {
				double minValue = Double.MAX_VALUE;
				double maxValue = -Double.MAX_VALUE;
				// For each feature vector
				for (List<Double> fv : set) {
					double f = fv.get(i); 
					// a. If f can legitimately be -1: get min and max always
					if (indicesOfPitchMvmt.contains(i) || i == indexOfPitchVoiceRel) {
						if (f < minValue) {
							minValue = f;
						}
						if (f > maxValue) {
							maxValue = f;
						}
					}
					// b. If f cannot be -1 legitimately (i.e., gets -1 as n/a value or cannot be -1 at all):
					// get min and max only if f is not -1
					else {
						if (f != -1) {
							if (f < minValue) {
								minValue = f;
							}
							if (f > maxValue) {
								maxValue = f;
							}
						}
					}
				}	
				
				// All feature vectors traversed? Set the min and max feature values at index i in minAndMax.
				// Features that are always -1 get Double.MAX_VALUE as minValue and -Double.MAX_VALUE as maxValue
				minAndMax[0][i] = minValue;
				minAndMax[1][i] = maxValue;
			}
		}
		
			
//		if (learningApproach == ExperimentRunner.CHORD_TO_CHORD) {
//			// For each feature at index i 
//			for (int i = 0; i < numFeatures; i++) {
//				double minValue = Double.MAX_VALUE;
//				double maxValue = -Double.MAX_VALUE;
//				if (isTablatureCase) {
//					//
//				}
//				if (!isTablatureCase) {	
//					// For each feature vector
//					for (List<Double> fv : set) {
//						double f = fv.get(i); 
//						
//						// If f is a pitch movement: scale absolute values
//						if (indicesOfPitchMvmt.contains(i)) {
//							if (f < minValue) {
//								minValue = f;
//							}
//							if (f > maxValue) {
//								maxValue = f;
//							}
//						}
//						// If f is the pitch-voice relation
//						else if (i == indexOfPitchVoiceRel) {
//							if (f < minValue) {
//								minValue = f;
//							}
//							if (f > maxValue) {
//								maxValue = f;
//							}
//						}
//						// If f is one of the other features and its value is not -1
//						else {
//							if (f != -1) {
//								if (f < minValue) {
//									minValue = f;
//								}
//								if (f > maxValue) {
//									maxValue = f;
//								}
//							}	
//						}
//					}
//				}
//				// All feature vectors traversed? Set the min and max feature values at index i in minAndMax 
//				minAndMax[0][i] = minValue;
//				minAndMax[1][i] = maxValue;
//			}
//		}
					
		return minAndMax;
	}


	/**
	 * Gets the minimum and maximum value for each feature from the given List of List of feature vectors. Returns a
	 * double[][] containing as the first element a double[] with the minimum values for each feature, and as the second
	 * a double[] with the maximum values.
	 * 
	 * NB: C2C approach only.
	 * 
	 * @param setOfSets
	 * @param argLearningApproach
	 * @return 
	 */
	// TESTED
	public static double[][] getMinAndMaxValuesSetOfSetsOfFeatureVectors(List<List<List<Double>>> setOfSets,
		ModellingApproach argModellingApproach) {
		List<List<Double>> asSingleSet = new ArrayList<List<Double>>();
		for (List<List<Double>> l : setOfSets) {
			asSingleSet.addAll(l);
		}
		return getMinAndMaxValuesSetOfFeatureVectors(asSingleSet, argModellingApproach); 
	}


	// =================================== ISMIR AND MUSCI ===================================
	// FEATURE_SET_A
	/**
	 * Gets the basic features of the note at noteIndex. Returns a double[] containing
	 *   in the tablature case:
	 *     as element 0: the pitch (as a MIDInumber) of the tablature note;
	 *     as element 1: the course the tablature note is on;
	 *     as element 2: the fret the tablature note is on;
	 *     as element 3: the minimum duration (as given in the tablature, in whole notes) of the tablature note;
	 *     as element 4: the maximum duration of the tablature note, i.e., the difference between its onset time and the
	 *                   onset time of the next tablature note on the same course. When the onset is in the last chord, the
	 *                   maximum duration is equal to the chord's minimum duration;
	 *     as element 5: the size of the chord containing the note (in number of new onsets)
	 *     as element 6: whether the tablature note has ornamentation characteristics, which is so if it is the only 
	 *                   onset within the chord and has a duration of a 16th note or smaller.
	 *     as element 7: whether the note is produced by an open course
	 *   
	 *   in the non-tablature case:
	 *     as element 0: the pitch (as a MIDInumber) of the note;
	 *     as element 1: the full duration (in whole notes) of the note;
	 *     as element 2: the size of the chord, including any sustained previous notes
	 *     as element 3: whether the note has ornamentation characteristics, which is so if it is the only onset (not 
	 *                   necessarily the only note!) within the chord and has a duration of a 16th note or smaller;             
	 *     
	 * @param basicTabSymbolProperties Must be <code>null</code> in the non-tablature case
	 * @param basicNoteProperties Must be <code>null</code> in the tablature case
	 * @param meterInfo
	 * @param noteIndex
	 * 
	 * @return
	 */
	// TESTED for both tablature- and non-tablature case
	static double[] getBasicNoteFeaturesMUSCI(Integer[][] basicTabSymbolProperties, Integer[][] basicNoteProperties, 
		int noteIndex) {
		
	  // Verify that either basicTabSymbolProperties or basicNoteProperties == null
		if ((basicTabSymbolProperties != null && basicNoteProperties != null) ||
		  (basicTabSymbolProperties == null && basicNoteProperties == null)) {
		  System.out.println("ERROR: if basicTabSymbolProperties == null, basicNoteProperties must not be, and vice versa" + "\n");
		   throw new RuntimeException("ERROR (see console for details)");
		}
		
		double[] basicNoteFeatures = null;
	  // a. In the tablature case
		// NB: When using no tablature information, return only: 
   // 0. The pitch (as a MIDI number) of the onset
   // 3. The minimum duration (in whole notes) of the onset 
   // 5. The size of the chord containing the onset
   // 6. Whether the onset has ornamentation characteristics, yes or no
		if (basicTabSymbolProperties != null) {
			basicNoteFeatures = new double[8];
		  // 0. The pitch (as a MIDI number) of the note 
		  basicNoteFeatures[0] = basicTabSymbolProperties[noteIndex][Tablature.PITCH];
		  // 1. The course the onset is on
		  basicNoteFeatures[1] = basicTabSymbolProperties[noteIndex][Tablature.COURSE];
		  // 2. The fret the onset is on 
		  basicNoteFeatures[2] = basicTabSymbolProperties[noteIndex][Tablature.FRET];
		  // 3. The minimum duration (in whole notes) of the note 
		  basicNoteFeatures[3] = 
		  	(double) basicTabSymbolProperties[noteIndex][Tablature.MIN_DURATION] / Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom();		  
		  // 4. The maximum duration (in whole notes) of the note
		  basicNoteFeatures[4] = 
		  	(double) basicTabSymbolProperties[noteIndex][Tablature.MAX_DURATION] / Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom();
		  // 5. The size of the chord containing the note
		  basicNoteFeatures[5] = basicTabSymbolProperties[noteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
		  // 6. Whether the note has ornamentation characteristics, yes or no
		  basicNoteFeatures[6] = 0.0;
		  if (basicTabSymbolProperties[noteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS] == 1 && 
		    basicTabSymbolProperties[noteIndex][Tablature.MIN_DURATION] < RhythmSymbol.semiminim.getDuration()) {
		    basicNoteFeatures[6]= 1.0;
		  }
		  // 7. Whether the note is produced by plucking an open course, yes or no
		  basicNoteFeatures[7] = 0.0;
		  if (basicTabSymbolProperties[noteIndex][Tablature.FRET] == 0) {
			  basicNoteFeatures[7] = 1.0;
		  }
		}
		// b. In the non-tablature case
		else if (basicNoteProperties != null) {
			basicNoteFeatures = new double[4];
		  // 0. The pitch (as a MIDI number) of the Note
			basicNoteFeatures[0] = basicNoteProperties[noteIndex][Transcription.PITCH];
			// 1. The duration (in whole notes) of the Note
			double duration = (double) basicNoteProperties[noteIndex][Transcription.DUR_NUMER] / 
				basicNoteProperties[noteIndex][Transcription.DUR_DENOM];
			basicNoteFeatures[1] = duration; 
			// 2. The size of the chord containing the Note, including any sustained Notes
			int numNewOnsets = basicNoteProperties[noteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
			int numSustainedNotes =	getIndicesOfSustainedPreviousNotesMUSCI(basicNoteProperties, noteIndex).size();
			basicNoteFeatures[2] = (double) numNewOnsets + numSustainedNotes; 
			// 3. Whether the Note has ornamentation characteristics, yes or no
			// NB: MIDI can be imprecise, i.e., the notes' actual MIDI onset- and offset times may be a little before or  
			// after the notated (from the score) onset- and offset times. It is therefore safer to choose <= 16th than
			// < 1/8 here, as the latter will count single notes whose duration is slightly under 1/8 because of 
			// fluctuation in the MIDI as ornamental notes 
			basicNoteFeatures[3] = 0.0;
			if (basicNoteProperties[noteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS] == 1 && duration <= 1/16.0) {
				basicNoteFeatures[3]= 1.0;
			}
		}
		return basicNoteFeatures;
	}
	
	
	/**
	 * Gets the indices of all previous Notes that are still sounding at the onset time of the Note at noteIndex,
	 * i.e., all previous Notes whose offset time is greater than the onset time of the Note at noteIndex.
	 * 
	 * NB: This method applies to the non-tablature case only. 
	 * 
	 * @param noteIndex
	 * @param basicNoteProperties
	 * @return
	 */
	// TESTED 
	static List<Integer> getIndicesOfSustainedPreviousNotesMUSCI(Integer[][] basicNoteProperties, int noteIndex) {
 	List<Integer> indicesOfSustainedPreviousNotes = new ArrayList<Integer>();
		
		// 1. Determine the onset time and the sequence number in the chord of the Note at noteIndex
		Rational onsetTimeCurrentNote = 
 		new Rational(basicNoteProperties[noteIndex][Transcription.ONSET_TIME_NUMER],
 		basicNoteProperties[noteIndex][Transcription.ONSET_TIME_DENOM]);		
		int seqNumInChordCurrentNote = basicNoteProperties[noteIndex][Transcription.NOTE_SEQ_NUM];
		int indexOfFirstNoteInCurrentChord = noteIndex - seqNumInChordCurrentNote;
   
		// 2. For all Notes in the previous chord(s)
 	for (int i = 0; i < indexOfFirstNoteInCurrentChord; i++) {
 		// Determine the offset time, i.e., the Note's MetricTime + MetricDuration
 		Integer[] currentPreviousBasicNoteProperties = basicNoteProperties[i];
 		Rational metricTimeCurrentPreviousNote = 
 			new Rational(currentPreviousBasicNoteProperties[Transcription.ONSET_TIME_NUMER],
     	currentPreviousBasicNoteProperties[Transcription.ONSET_TIME_DENOM]);
 		Rational metricDurationCurrentPreviousNote = 
 			new Rational(currentPreviousBasicNoteProperties[Transcription.DUR_NUMER],
 			currentPreviousBasicNoteProperties[Transcription.DUR_DENOM]);
 		Rational offsetTimeCurrentPreviousNote = metricTimeCurrentPreviousNote.add(metricDurationCurrentPreviousNote);
 		// Is offsetTimeCurrentPreviousNote > onsetTimeCurrentNote? Previous note still sounding; add index
 		// to indicesOfSustainedPreviousNotes
 		if (offsetTimeCurrentPreviousNote.isGreater(onsetTimeCurrentNote)) {
 			indicesOfSustainedPreviousNotes.add(i);
 		}
 	}
 	return indicesOfSustainedPreviousNotes;
 }
	
	
 // FEATURE_SET_B
	/**
	 * Gets the position in the chord of the note at noteIndex. Returns a double containing 
	 *   in the tablature case: 
	 *     as element 0: the number of notes below the note at noteIndex; 
	 *     as element 1: the number of notes above the note at noteIndex,
	 *   in the non-tablature case: 
	 *     as element 0 the number of notes below the note at noteIndex; 
	 *     as element 1 the number of notes simultaneous with the note at noteIndex;
	 *     as element 2 the number of notes above the note at noteIndex.
	 *     For all three cases, sustained previous notes are counted as well.
	 * 
	 * @param basicTabSymbolProperties Must be <code>null</code> in the non-tablature case
	 * @param basicNoteProperties Must be <code>null</code> in the tablature case
	 * @param noteIndex
	 * @return
	 */
	// TESTED for both tablature- and non-tablature case
	static double[] getPositionWithinChordMUSCI(Integer[][] basicTabSymbolProperties, Integer[][] basicNoteProperties, 
		int noteIndex) {
	
	  // Verify that either basicTabSymbolProperties or basicNoteProperties == null
		if ((basicTabSymbolProperties != null && basicNoteProperties != null) ||
		  (basicTabSymbolProperties == null && basicNoteProperties == null)) {
		  System.out.println("ERROR: if basicTabSymbolProperties == null, basicNoteProperties must not be, and vice versa" + "\n");
		   throw new RuntimeException("ERROR (see console for details)");
		}

		double[] positionWithinChord = null; 
				 
   // a. In the tablature case 
   if (basicTabSymbolProperties != null) {
   	positionWithinChord = new double[2]; 
   	int seqNumInChord = basicTabSymbolProperties[noteIndex][Tablature.NOTE_SEQ_NUM];
     int numOnsetsInChord = basicTabSymbolProperties[noteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
   	// Determine the number of notes below and above
   	int numberOfNotesBelowCurrentNote = seqNumInChord;
   	int numberOfNotesAboveCurrentNote = numOnsetsInChord - (seqNumInChord + 1);
     // Set positionWithinChord
     positionWithinChord[0] = numberOfNotesBelowCurrentNote;
     positionWithinChord[1] = numberOfNotesAboveCurrentNote;
   } 
   // b. In the non-tablature case 
   else if (basicNoteProperties != null) {
   	positionWithinChord = new double[3]; 
   	int seqNumInChord = basicNoteProperties[noteIndex][Transcription.NOTE_SEQ_NUM];
   	int	numOnsetsInChord = basicNoteProperties[noteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
   	int	pitchOfCurrentNote = basicNoteProperties[noteIndex][Transcription.PITCH];
  		// 1. Determine the initial number of notes below and above 
  		int numberOfNotesBelowCurrentNote = seqNumInChord;
  		int numberOfNotesAboveCurrentNote = numOnsetsInChord - (seqNumInChord + 1);
  		int numberOfNotesSimultaneous = 0;
  		// 2. The initial numbers of notes will include any notes with the same pitch as pitchOfCurrentNote. These
  		// notes must be counted as simultaneous notes
  		// a. Check the notes below for any notes with the same pitch; in/decrement variables accordingly
  		int startIndex = noteIndex - numberOfNotesBelowCurrentNote;
  		int endIndex = noteIndex;
  		for (int i = startIndex; i < endIndex; i++) {
  			int currentPitchBelow = basicNoteProperties[i][Transcription.PITCH];
  			if (currentPitchBelow == pitchOfCurrentNote) {
  				numberOfNotesBelowCurrentNote--;
  				numberOfNotesSimultaneous++;
  			}
  		}
  		// b. Check the notes above for any notes with the same pitch; in/decrement variables accordingly
  		startIndex = noteIndex + 1;
  		endIndex = noteIndex + (numberOfNotesAboveCurrentNote + 1);
  		for (int i = startIndex; i < endIndex; i++) {
  			int currentPitchAbove = basicNoteProperties[i][Transcription.PITCH];
  			if (currentPitchAbove == pitchOfCurrentNote) {
  				numberOfNotesAboveCurrentNote--;
  				numberOfNotesSimultaneous++;
  			}
  		}
  		// 3. Check the sustained previous notes and increment numberOfNotesBelowCurrentNote, numberOfNotesAboveCurrentNote,
  		// and numberOfNotesSimultaneous accordingly
//  		List<Integer> indicesOfSustainedPreviousNotes =
//  			getIndicesOfSustainedPreviousNotes(null, null, basicNoteProperties, noteIndex);
  		List<Integer> indicesOfSustainedPreviousNotes =
    			getIndicesOfSustainedPreviousNotesMUSCI(basicNoteProperties, noteIndex);
  		for (int i : indicesOfSustainedPreviousNotes) {
  			int currentPitchOfSustainedPreviousNote = basicNoteProperties[i][Transcription.PITCH];
  			if (currentPitchOfSustainedPreviousNote < pitchOfCurrentNote) {
  				numberOfNotesBelowCurrentNote++;
  			}
  			else if (currentPitchOfSustainedPreviousNote > pitchOfCurrentNote) {
  				numberOfNotesAboveCurrentNote++;
  			}
  			else if(currentPitchOfSustainedPreviousNote == pitchOfCurrentNote) {
  			  numberOfNotesSimultaneous++;
  			} 
  		}
     // 4. Set positionWithinChord
     positionWithinChord[0] = numberOfNotesBelowCurrentNote;
     positionWithinChord[1] = numberOfNotesSimultaneous; 
     positionWithinChord[2] = numberOfNotesAboveCurrentNote;
   }
  
   return positionWithinChord;
	}
	
		
 /**
	 * Gets the pitch distance to the notes below and above the note at the given noteIndex. Returns a
	 * double[] containing
	 *   in the tablature case: 
	 *     as element 0: the pitch distance to the note below the note at noteIndex;
	 *     as element 1: the pitch distance to the note above the note at noteIndex. 
	 *     Returns -1 if there is no note below or above the note at noteIndex.
	 *   in the non-tablature case:
	 *     as element 0: the pitch distance to the note below the note at noteIndex;
	 *     as element 1: the pitch distance to any simultaneous note (i.e., 0.0 if there is one; -1.0 if not); TODO meaningless feature?, remove?
	 *     as element 2: the pitch distance to the note above the note at noteIndex.
	 *     For all three cases, sustained previous notes are counted as well.
	 *   If there is no note below, simultaneous, or above, the default value -1 is used.
	 *     
	 * @param basicTabSymbolProperties Must be <code>null</code> in the non-tablature case
	 * @param basicNoteProperties Must be <code>null</code> in the tablature case
	 * @param noteIndex
	 * @return
	 */
 // TESTED for both tablature- and non-tablature case
	static double[] getPitchDistancesWithinChordMUSCI(Integer[][] basicTabSymbolProperties, Integer[][] basicNoteProperties,
		int noteIndex) {
   
	  // Verify that either basicTabSymbolProperties or basicNoteProperties == null
		if ((basicTabSymbolProperties != null && basicNoteProperties != null) ||
		  (basicTabSymbolProperties == null && basicNoteProperties == null)) {
		  System.out.println("ERROR: if basicTabSymbolProperties == null, basicNoteProperties must not be, and vice versa" + "\n");
		   throw new RuntimeException("ERROR (see console for details)");
		}
		
		double[] pitchDistancesWithinChord = null;
		
   // a. In the tablature case 
   if (basicTabSymbolProperties != null) {
     // Initialise pitchDistancesWithinChord with default (does-not-apply) values
     pitchDistancesWithinChord = new double[]{-1.0, -1.0}; 
   	
   	int seqNumInChord = basicTabSymbolProperties[noteIndex][Tablature.NOTE_SEQ_NUM];
     int numOnsetsInChord = basicTabSymbolProperties[noteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
     int pitchOfCurrentNote = basicTabSymbolProperties[noteIndex][Tablature.PITCH];
     
     // Determine the pitches of any notes below and above
     // a. Only if the note is not the first note in the chord: determine pitch below
     if (seqNumInChord != 0) {
       int pitchOfNoteBelow = basicTabSymbolProperties[noteIndex - 1][Tablature.PITCH];
       pitchDistancesWithinChord[0] = Math.abs(pitchOfCurrentNote - pitchOfNoteBelow);
     }
     // Only of the note is not the last note in the chord: determine pitch above
     if (seqNumInChord != numOnsetsInChord - 1) {
     	int pitchOfNoteAbove = basicTabSymbolProperties[noteIndex + 1][Tablature.PITCH];
     	pitchDistancesWithinChord[1] = Math.abs(pitchOfCurrentNote - pitchOfNoteAbove);
     }
   }
   // b. In the non-tablature case 
   else if (basicNoteProperties != null) {
     // Initialise pitchDistancesWithinChord with default (does-not-apply) values
 	  pitchDistancesWithinChord = new double[]{-1.0, -1.0, -1.0}; 
 	  
 	  int seqNumInChord = basicNoteProperties[noteIndex][Transcription.NOTE_SEQ_NUM];
 	  int	numOnsetsInChord = basicNoteProperties[noteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
 	  int	pitchOfCurrentNote = basicNoteProperties[noteIndex][Transcription.PITCH];
 	    	  
		  // 1. Determine the number of notes below and above; initialise the number of simultaneous notes
		  int numberOfNotesBelowCurrentNote = seqNumInChord;
		  int numberOfNotesAboveCurrentNote = numOnsetsInChord - (seqNumInChord + 1);
		  int numberOfNotesSimultaneous = 0;
		  
		  // 2. Determine pitches below and above and add to the Lists. If a note below/above has the same pitch as
		  // pitchOfCurrentNote, do not add but increment numberOfNotesSimultaneous 
		  // a. Notes below 
		  int startIndex = noteIndex - numberOfNotesBelowCurrentNote;
		  int endIndex = noteIndex;
		  List<Integer> pitchesOfNotesBelow = new ArrayList<Integer>(); 
		  for (int i = startIndex; i < endIndex; i++) {
	  		int currentPitchBelow = basicNoteProperties[i][Transcription.PITCH];
	  		if (currentPitchBelow != pitchOfCurrentNote) {
	  			pitchesOfNotesBelow.add(currentPitchBelow);
  			}
	  		else {
	  			numberOfNotesSimultaneous++;
	  		}
  		}
	    // b. Notes above
  		startIndex = noteIndex + 1;
  		endIndex = noteIndex + (numberOfNotesAboveCurrentNote + 1);
  		List<Integer> pitchesOfNotesAbove = new ArrayList<Integer>(); 
  		for (int i = startIndex; i < endIndex; i++) {
  			int currentPitchAbove = basicNoteProperties[i][Transcription.PITCH];
  			if (currentPitchAbove != pitchOfCurrentNote) {
  				pitchesOfNotesAbove.add(currentPitchAbove);
  			}
  			else {
  				numberOfNotesSimultaneous++;
  			}
  		}
     // 2. Determine pitches for any sustained notes and add to the Lists. If a sustained note has the same pitch
  		// as pitchOfCurrentNote, do not add but increment numberOfNotesSimultaneous 
  		List<Integer> indicesOfSustainedPreviousNotes =
  			getIndicesOfSustainedPreviousNotesMUSCI(basicNoteProperties, noteIndex);
  		for (int i : indicesOfSustainedPreviousNotes) {
	  		int currentPitchOfSustainedPreviousNote = basicNoteProperties[i][Transcription.PITCH];
  			if (currentPitchOfSustainedPreviousNote < pitchOfCurrentNote) {
  				pitchesOfNotesBelow.add(currentPitchOfSustainedPreviousNote);
  			}
  			else if (currentPitchOfSustainedPreviousNote > pitchOfCurrentNote) {
  				pitchesOfNotesAbove.add(currentPitchOfSustainedPreviousNote);
  			}
  			else if (currentPitchOfSustainedPreviousNote == pitchOfCurrentNote) {
  			  numberOfNotesSimultaneous++;
  			} 
  		}
  		// 4. Determine the closest notes below and above and any simultaneous notes, and set pitchDistancesWithinChord
  		if (pitchesOfNotesBelow.size() != 0) {
  		  int pitchDistanceToNoteBelow = Math.abs(pitchOfCurrentNote - Collections.max(pitchesOfNotesBelow));
  		  pitchDistancesWithinChord[0] = pitchDistanceToNoteBelow;
  		}
  		if (numberOfNotesSimultaneous != 0) {
  			pitchDistancesWithinChord[1] = 0.0;
  		}
  		if (pitchesOfNotesAbove.size() != 0) {
  		  int pitchDistanceToNoteAbove = Math.abs(pitchOfCurrentNote - Collections.min(pitchesOfNotesAbove));
  		  pitchDistancesWithinChord[2] = pitchDistanceToNoteAbove;
  		}
   }
   return pitchDistancesWithinChord;
	}
	
	
 // FEATURE_SET_C
	/**
	 * Compares the note given as argument to the previous note in each voice, and calculates their pitch proximity,
	 * their inter-onset time proximity, and their offset-onset time proximity. 
	 * 
	 * NB: In the tablature case, the previous Note's offset time is determined by its minimum duration as given
	 *     in the tablature. In the non-tablature case, the previous Note's offset time is determined by its 
	 *     full duration.        
	 * 
	 * Returns a double[][] containing:
	 *   as element 0, a double[] containing the pitch proximities of the current Note to the previous Note in each voice
	 *   as element 1, a double[] containing the inter-onset time proximities of the current Note to the previous Note in each voice
	 *   as element 2, a double[] containing the offset-onset time proximities of the current Note to the previous Note in each voice
	 * NB: element 0 is the proximity to voice 0 (the top voice), element 1 that to voice 1 (the second from the top), etc.
	 *       
	 * @param basicTabSymbolProperties       
	 * @param transcription
	 * @param currentNote
	 * @return
	 */
 // TESTED for both tablature- and non-tablature case
	static double[][] getPitchAndTimeProximitiesToAllVoicesMUSCI(Integer[][] basicTabSymbolProperties, Transcription transcription,
		Note currentNote) {			
		double[][] pitchAndTimeProximities = new double[3][Transcription.MAXIMUM_NUMBER_OF_VOICES];
		
		// 1. Traverse all the theoretically possible voices 
		for (int voiceNumber = 0; voiceNumber < Transcription.MAXIMUM_NUMBER_OF_VOICES; voiceNumber++) {
		  // a. If the Transcription contains the voice with voiceNumber: calculate the proximities of currentNote
			// to the previous note in that voice and set the appropriate element of pitchAndTimeProximities
		  if (voiceNumber < transcription.getPiece().getScore().size()) {
			  NotationVoice currentVoice = transcription.getPiece().getScore().get(voiceNumber).get(0);
			  double[] pitchAndTimeProximitiesOfCurrentNote = 
			  	getProximitiesAndMovementToVoiceMUSCI(basicTabSymbolProperties, currentVoice, currentNote);
			  pitchAndTimeProximities[0][voiceNumber] = pitchAndTimeProximitiesOfCurrentNote[0];
			  pitchAndTimeProximities[1][voiceNumber] = pitchAndTimeProximitiesOfCurrentNote[1];
			  pitchAndTimeProximities[2][voiceNumber] = pitchAndTimeProximitiesOfCurrentNote[2]; 
		  }
		  // b. If the Transcription does not contain the voice with voiceNumber: set the elements representing 
		  // that voice to the default values also used when a Note is the first one in a voice (see 
		  // getProximitiesAndMovementToVoice())
		  else { 			  
			  pitchAndTimeProximities[0][voiceNumber] = -1.0;
			  pitchAndTimeProximities[1][voiceNumber] = -1.0;
			  pitchAndTimeProximities[2][voiceNumber] = -1.0;
		  }	
	  }
	  // 2. All voices traversed? Set and return pitchAndTimeProximities
	  return pitchAndTimeProximities;
	}
	
	
	/**
	 * Compares the Note given as argument to the previous Note in the voice given as argument, and calculates
	 * their pitch proximity, their inter-onset time proximity, their offset-onset time proximity, and the pitch
	 * movement. 
	 * (1) Proximities are defined as inverted distances: 
	 *     -pitch proximity is > 0 and <= 1 (where a pitch proximity of 1.0 means no distance)
	 *     -inter-onset proximity is > 0 and < 1
	 *     -offset-onset proximity is
	 *      a. > 0 and < 1 if the offset time is before the onset time (i.e., if the offset-onset time is positive)
	 *      b. 1 if the offset time equals the onset time (i.e., if the offset-onset time is 0)
	 *      c. < 0 and > -1 if the offset time is after the onset time (i.e., if the offset-onset time is negative)
	 *  NB1: Proximities to non-existing voices are indicated with -1.0. 
	 *  NB2: In the tablature case, the previous Note's offset time is determined by its minimum duration as given
	 *       in the tablature if USE_FULL_DURATION is set to false, and by its full duration if it is set to true.
	 *       In the non-tablature case, the previous Note's offset time is determined by its full duration.                 
	 * (2) Pitch movements are defined in semitones, and can be positive (ascending) and negative (descending). 
	 * 
	 * Returns a double[] containing:
	 * (1) as element 0, the pitch proximity of currentNote to the previous Note in voiceToCompareTo
	 * (2) as element 1, the inter-onset time proximity of currentNote to the previous Note in voiceToCompareTo
	 * (3) as element 2, the offset-onset time proximity of currentNote to the previous Note in voiceToCompareTo
	 * (4) as element 3, the pitch movement (in semitones) of currentNote with regard to to the previous Note in voiceToCompareTo 
	 * 
	 * @param voiceToCompareTo
	 * @param currentNote
	 * @param basicTabSymbolProperties Must be <code>null</code> in the non-tablature case
	 * 
	 * @return
	 */
 // TESTED for both tablature- and non-tablature case
	static double[] getProximitiesAndMovementToVoiceMUSCI(Integer[][] basicTabSymbolProperties, NotationVoice voiceToCompareTo,
		Note currentNote) {
		
		double[] proximitiesAndMovementToVoice = new double[4];
				
		// 1. Determine the index of currentNote in voiceToCompareTo
	  // NB: findFirst() looks in voiceToCompareTo for a Note at the onset time of currentNote. What it returns
		// depends on whether or not there is a Note in voiceToCompareTo at the onset time of currentNote:
		// a. If so: the actual position of that Note is returned; no further action needs to be taken 
		// b. If not: a negative number is returned, and the "fictional" (insert) position of currentNote must be calculated
		int currentNoteIndex = voiceToCompareTo.findFirst(currentNote); // TODO get rid of findFirst() and do using getPreviousNoteInVoice() (see below)?
		if (currentNoteIndex < 0) {
			currentNoteIndex = (currentNoteIndex + 1) * -1;
		}
		
		// 2. Determine the pitch difference, inter-onset time, offset-onset time, and pitch movement between 
		// currentNote and the previous Note in voiceToCompareTo
		// a. If voiceToCompareTo contains Notes before currentNote
		if (currentNoteIndex > 0) {
			int pitchDif;
			double interOnsetTime;
			double offsetOnsetTime;
			int pitchMovement;
			// 1. Get previousNote, which is at currentNoteIndex - 1. NotationVoices contain NotationChords, in which 
			// Notes are wrapped; get(0) gets the first (and in our case only) Note of the NotationChord
			Note previousNote = voiceToCompareTo.get(currentNoteIndex - 1).get(0); 
			// 2. Determine the pitch difference and the pitch movement
			pitchDif = Math.abs(previousNote.getMidiPitch() - currentNote.getMidiPitch());
			pitchMovement = currentNote.getMidiPitch() - previousNote.getMidiPitch();
			// 3. Determine the inter-onset time 
			Rational onsetTimeCurrentNote = currentNote.getMetricTime();
			Rational onsetTimePreviousNote = previousNote.getMetricTime();
			interOnsetTime = onsetTimeCurrentNote.sub(onsetTimePreviousNote).toDouble();
			// 4. Determine the offset-onset time
			// a. In the tablature case: depending on the value of USE_FULL_DURATION, use previousNote's 
			// minimum duration to determine its offset time
			if (basicTabSymbolProperties != null) {
//				// a. If only the minimum duration is taken into account
//				if (!USE_FULL_DURATION) {
				// 1. Calculate the minimum duration of previous note 
 		  // (i) Calculate the gridX value that corresponds to onsetTimePreviousNote 
				// NB: The 32nd is the smallest rhythmic value in the tablature case, so onsetTimePreviousNote will always
				// be a multiple of 1/32. Multiplying it by 32 and then getting the numerator thus gives the gridX value 
 		  int gridXPositionPreviousNote = 
 		  	onsetTimePreviousNote.mul(Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom()).getNumer();
 		  // (ii) Find the tablature note with gridXPositionPreviousNote and determine the minimum duration
 		  // (as a Rational) of previousNote
 		  Rational minimumDurationPreviousNote = null;
 		  for (Integer[] btp : basicTabSymbolProperties) {
	      	if (btp[Tablature.ONSET_TIME] == gridXPositionPreviousNote) {
	      		minimumDurationPreviousNote = 
	      			new Rational(btp[Tablature.MIN_DURATION], Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom()); 
	      		break;
	  	   	}
		    }
		    // 2. Calculate offSetTimePreviousNote and offsetOnsetTime
			  Rational offsetTimePreviousNote = onsetTimePreviousNote.add(minimumDurationPreviousNote); 
			  offsetOnsetTime = onsetTimeCurrentNote.sub(offsetTimePreviousNote).toDouble();
//		  	}
//				// b. If the full duration is taken into account
//				else {
//					Rational durationPreviousNote = previousNote.getMetricDuration();
//					Rational offsetTimePreviousNote = onsetTimePreviousNote.add(durationPreviousNote); 
//					offsetOnsetTime = onsetTimeCurrentNote.sub(offsetTimePreviousNote).toDouble();
//				}
			}
			// b. In the non-tablature case: use previousNote's full duration to determine its offset time
			else {
				Rational durationPreviousNote = previousNote.getMetricDuration();
				Rational offsetTimePreviousNote = onsetTimePreviousNote.add(durationPreviousNote); 
				offsetOnsetTime = onsetTimeCurrentNote.sub(offsetTimePreviousNote).toDouble();
			}
		  // 5. Create and set proximitiesAndMovementToVoice
		  // a. pitchDif (values >= 0)
			proximitiesAndMovementToVoice[0] = calculateProximity((double)pitchDif);
		  // b. interOnsetTime (values > 0) 
			proximitiesAndMovementToVoice[1] = calculateProximity(interOnsetTime);
		  // c. offsetOnsetTime (values between -inf and +inf (in theory))
			proximitiesAndMovementToVoice[2] = calculateProximity(offsetOnsetTime); 
			// d. pitchMovement (values between -inf and +inf (in theory))
			proximitiesAndMovementToVoice[3] = (double) pitchMovement;
		}
	  // b. If voiceToCompareTo contains no Notes before currentNote (i.e., if currentNote is the first Note in 
		// voiceToCompareTo): set "does-not-apply" values of -1.0 (proximities) and 0.0 (movement)
		else {
			proximitiesAndMovementToVoice[0] = -1.0;
			proximitiesAndMovementToVoice[1] = -1.0;
			proximitiesAndMovementToVoice[2] = -1.0;
			proximitiesAndMovementToVoice[3] =  0.0;
		}
		
		// 3. Return proximitiesAndMovementToVoice
		return proximitiesAndMovementToVoice;
	}
	
	
	// FEATURE_SET_D
	/**
	 * Determines which voices are already occupied in the current chord by notes before the note at noteIndex.
	 *   In the tablature case, where the offset time of the notes in the previous chord(s) is unknown:
	 *     only notes positioned at a lower index than noteIndex within the chord are taken into consideration.
	 *     If the note at noteIndex is an upper unison note (i.e., the one on the higher course), the lower unison
	 *     note is taken into consideration as well.
	 *   In the non-tablature case, where the offset time of the notes in the previous chord(s) is known:
	 *     both the notes positioned at a lower index than noteIndex within the chord and any previous sustained 
	 *     notes are taken into consideration. If the note at noteIndex is an upper unison note (i.e., the one with
	 *     the longer duration if the unison notes have inequal durations)on the higher course), the lower unison
	 *     note is taken into account as well.   
	 *      
	 * Returns a List<Double>, where the position of the 1.0s indicates which voices (if any) have already been 
	 * taken (where position 0 represents the top voice, position 1 the second highest, etc.) 
	 * 
	 * @param basicTabSymbolProperties Must be <code>null</code> in the non-tablature case
	 * @param basicNoteProperties Must be <code>null</code> in the tablature case
	 * @param noteIndex
	 * @param transcription
	 * @return 
	 */
 // TESTED for both tablature- and non-tablature case
	static List<Double> getVoicesAlreadyOccupiedMUSCI(Integer[][] basicTabSymbolProperties, Integer[][] basicNoteProperties,
		int noteIndex, Transcription transcription) { // TODO change arg transcription into List<List<Double>> voiceLabels?
		
		// Verify that either basicTabSymbolProperties or basicNoteProperties == null
		if ((basicTabSymbolProperties != null && basicNoteProperties != null) ||
		  (basicTabSymbolProperties == null && basicNoteProperties == null)) {
		  System.out.println("ERROR: if basicTabSymbolProperties == null, basicNoteProperties must not be, and vice versa" + "\n");
	    throw new RuntimeException("ERROR (see console for details)");
		}
		
	  // Initialise voicesAlreadyOccupied with only 0.0s
		List<Double> voicesAlreadyOccupied = new ArrayList<Double>();	
	  for (int i = 0; i < Transcription.MAXIMUM_NUMBER_OF_VOICES; i++) {
			voicesAlreadyOccupied.add(0.0);
		}
	  
	  // 1. Determine the indices of all notes whose voices are already occupied
	  List<Integer> indicesOfNotesWithVoiceAlreadyOccupied = new ArrayList<Integer>();
	  // a. In the tablature case
	  if (basicTabSymbolProperties != null) {
	    int seqNumInChord = basicTabSymbolProperties[noteIndex][Tablature.NOTE_SEQ_NUM];
	    int numberOfNotesBelow = seqNumInChord;
	    // Add the indices of any note below the note at noteIndex to indicesOfNotesWithVoiceAlreadyOccupied
	    int startIndex = noteIndex - numberOfNotesBelow;
	    int endIndex = noteIndex;
	    for (int i = startIndex; i < endIndex; i++) {
	    	indicesOfNotesWithVoiceAlreadyOccupied.add(i);	
	    }
	  }
	  // b. In the non-tablature case
	  else if (basicNoteProperties != null) {
	  	int seqNumInChord = basicNoteProperties[noteIndex][Transcription.NOTE_SEQ_NUM];
	  	int numberOfNotesBelow = seqNumInChord;
	    // 1. Add the indices of any notes below the note at noteIndex to indicesOfNotesWithVoiceAlreadyOccupied
	    int startIndex = noteIndex - numberOfNotesBelow;
	    int endIndex = noteIndex;
	   	for (int i = startIndex; i < endIndex; i++) {
	   		 indicesOfNotesWithVoiceAlreadyOccupied.add(i);
	   	}
	    
	    // 2. Add the indices of any sustained previous notes to indicesOfNotesWithVoiceAlreadyOccupied 
		  List<Integer> indicesOfSustainedPreviousNotes = 
			  getIndicesOfSustainedPreviousNotesMUSCI(basicNoteProperties, noteIndex);
		  indicesOfNotesWithVoiceAlreadyOccupied.addAll(indicesOfSustainedPreviousNotes);
	  }
	  
	  // 2. Determine the voices assigned to the notes at the indices in indicesOfNotesWithVoiceAlreadyOccupied and
	  // set voicesAlreadyOccupied accordingly
		List<List<Double>> voiceLabels = transcription.getVoiceLabels();
	  for (int i = 0; i < indicesOfNotesWithVoiceAlreadyOccupied.size(); i++) {
	   	int currentIndexOfNoteAlreadyOccupied = indicesOfNotesWithVoiceAlreadyOccupied.get(i);
	 	  List<Double> voiceLabelOfNoteAlreadyOccupied = voiceLabels.get(currentIndexOfNoteAlreadyOccupied);
	 	  // Add the voice assigned to the note at currentIndexOfNoteBelow to voicesAlreadyOccupied  
	 	  for (int j = 0; j < voiceLabelOfNoteAlreadyOccupied.size(); j++) {
	 	  	if (voiceLabelOfNoteAlreadyOccupied.get(j) == 1.0) {
	 	  		voicesAlreadyOccupied.set(j, 1.0);
	 	  	}
	 	  } 
   }
	  return voicesAlreadyOccupied;
	}
	
	
	/**
	 * Generates the feature vector for the tablature note at the given index.
	 * 
	 * @param basicTabSymbolProperties
	 * @param basicNoteProperties
	 * @param transcription
	 * @param currentNote
	 * @param noteIndex
	 * @param featureSet
	 * @param useTablatureInformation 
	 * @return 
	 */
	// TESTED through testing generateAllNoteFeatureVectors()
	public static List<Double> generateNoteFeatureVectorMUSCI(Integer[][] basicTabSymbolProperties, 
		Integer[][] basicNoteProperties, Transcription transcription, Note currentNote, int noteIndex, int featureSet,
		boolean useTablatureInformation) { // TODO Activate argument useTablatureInformation 
		List<Double> noteFeatureVector = new ArrayList<Double>();
		
	  // Verify that either basicTabSymbolProperties or basicNoteProperties == null
		if ((basicTabSymbolProperties != null && basicNoteProperties != null) ||
		  (basicTabSymbolProperties == null && basicNoteProperties == null)) {
		  System.out.println("ERROR: if basicTabSymbolProperties == null, basicNoteProperties must not be, and vice versa" + "\n");
		   throw new RuntimeException("ERROR (see console for details)");
		}
				
	  // Get featureSet A, the basic featureSet containing only tablature information
		// 0-7
		double[] featureSetA = getBasicNoteFeaturesMUSCI(basicTabSymbolProperties, basicNoteProperties, noteIndex); 
		for (int i = 0; i < featureSetA.length; i++) {
			noteFeatureVector.add(featureSetA[i]);
		}
		
	  // If featureSet is B, C, or D: add features to make featureSet B, which complements featureSet A with 
		// information on the position of the note within the event and the pitch distance to its neighbouring
		// note(s)
		if (featureSet == FeatureGenerator.FEATURE_SET_B || featureSet == FeatureGenerator.FEATURE_SET_C || 
			featureSet == FeatureGenerator.FEATURE_SET_D) {
		  // 8-9: The number of notes below and above the current note within the event
		  double[] positionWithinVerticalSonority = getPositionWithinChordMUSCI(basicTabSymbolProperties, 
		  	basicNoteProperties, noteIndex); 
		  for (int i = 0; i < positionWithinVerticalSonority.length; i++) {
		  	noteFeatureVector.add(positionWithinVerticalSonority[i]); 
		  }
		  // 10-11. The pitch distance to the notes below and above the current note
		  double[] pitchDistancesWithinVerticalSonority = getPitchDistancesWithinChordMUSCI(basicTabSymbolProperties,
		  	basicNoteProperties, noteIndex);
		  for (int i = 0; i < pitchDistancesWithinVerticalSonority.length; i++) {
		  	noteFeatureVector.add(pitchDistancesWithinVerticalSonority[i]); 
		  }
		}
			
		// If featureSet is C or D: add features to make featureSet C, which complements featureSet B with 
		// information on the pitch and time proximity of the current note to each last Note of all voices  
		if (featureSet == FeatureGenerator.FEATURE_SET_C || featureSet == FeatureGenerator.FEATURE_SET_D) {
	    // 12-16. The pitch proximity of the current note to the last assigned Note in each voice, where the 
			// element in pitchProximities with index 0 represents the proximity to the top voice, the element with
			// index 1 that to the second voice from the top, etc. 
			double[] pitchProximities = 
				getPitchAndTimeProximitiesToAllVoicesMUSCI(basicTabSymbolProperties, transcription, currentNote)[0];
			for (int i = 0; i < pitchProximities.length; i++) {
				noteFeatureVector.add(pitchProximities[i]);
			}
			// 17-21. The inter-onset time proximity of the current note to last assigned Note in each voice, where 
			// the element in interOnsetTimes with index 0 represents the proximity to the top voice, the element 
			// with index 1 that to the second voice from the top, etc.  
			double[] interOnsetTimeProximities = 
				getPitchAndTimeProximitiesToAllVoicesMUSCI(basicTabSymbolProperties, transcription, currentNote)[1];
			for (int i = 0; i < interOnsetTimeProximities.length; i++) {
				noteFeatureVector.add(interOnsetTimeProximities[i]);
			}
			// 22-26. The offset-onset time proximity of the current note to last assigned Note in each voice, where 
			// the element in offsetOnsetTimes with index 0 represents the proximity to the top voice, the element 
			// with index 1 that to the second voice from the top, etc.  
			double[] offsetOnsetTimeProximities = 
				getPitchAndTimeProximitiesToAllVoicesMUSCI(basicTabSymbolProperties, transcription, currentNote)[2];
			for (int i = 0; i < offsetOnsetTimeProximities.length; i++) {
				noteFeatureVector.add(offsetOnsetTimeProximities[i]);
			}
		}
					
		// If featureSet is D: add features to make featureSet D, which complements featureSet C with information
		// on the voices already assigned to any lower notes in the current event 
		if (featureSet == FeatureGenerator.FEATURE_SET_D) {
	    // 27-31. Whether a voice is already occupied in the current event, yes or no, where the element in
	    // voicesAlreadyOccupied with index 0 represents whether the top voice is already occupied, the element
	    // with index 1 whether the second voice from the top is already occupied, etc. 
	    List<Double> voicesAlreadyOccupied = getVoicesAlreadyOccupiedMUSCI(basicTabSymbolProperties, basicNoteProperties,
	    	noteIndex, transcription);
	    noteFeatureVector.addAll(voicesAlreadyOccupied);
		}			
		return noteFeatureVector;
	}
	
	
	/**
	 * Generates the feature vectors for all the training examples in the given Tablature-Transcription pair.
	 *
	 * @param tablature  
	 * @param transcription
	 * @param featureSet
	 * @return
	 */
	// TESTED for both tablature- and non-tablature case (not with concrete numbers) 
	static public List<List<Double>> generateAllNoteFeatureVectorsMUSCI(Integer[][] basicTabSymbolProperties,
		Integer[][] basicNoteProperties, Transcription transcription, int featureSet, boolean useTablatureInformation) {
		List<List<Double>> allNoteFeatureVectors = new ArrayList<List<Double>>();  
		
	  // Verify that either basicTabSymbolProperties or basicNoteProperties == null
	 	if ((basicTabSymbolProperties != null && basicNoteProperties != null) ||
	 	  (basicTabSymbolProperties == null && basicNoteProperties == null)) {
	 	  System.out.println("ERROR: if basicTabSymbolProperties == null, basicNoteProperties must not be, and vice versa" + "\n");
	 	   throw new RuntimeException("ERROR (see console for details)");
	 	}
		
		int numberOfNotes;
		// a. In the tablature case
   if (basicTabSymbolProperties != null) {
		  numberOfNotes = basicTabSymbolProperties.length; 
   }
   // b. In the non-tablature case
   else {
   	numberOfNotes = basicNoteProperties.length; 
   }
    
	  // Determine which noteSeq to use 
		// NB: pre-set NoteSequence possible because this method is not used in application mode
		NoteSequence noteSeq = transcription.getNoteSequence(); 
				
		// For each note at index i: 
		for (int i = 0; i < numberOfNotes; i++) {
			Note currentNote = noteSeq.getNoteAt(i);
			
     // Get the current feature vector
			List<Double> currentNoteFeatureVector = generateNoteFeatureVectorMUSCI(basicTabSymbolProperties,
				basicNoteProperties, transcription, currentNote, i, featureSet, useTablatureInformation);
			// Add currentNoteFeatureVector to allNoteFeatureVectors
     allNoteFeatureVectors.add(currentNoteFeatureVector);
		}
		return allNoteFeatureVectors;
	}
	
	
	/**
	 * Gets the note indices sequence needed for the backwards approach. Returns a List<Integer> containing the indices
	 * per chord, starting with the last chord and per chord with the index of the bottom note.
	 *  
	 * @param chordSizes
	 * @return
	 */
	// TESTED (for both tablature- and non-tablature case)
	public static List<Integer> getBackwardsMapping(List<Integer> chordSizes) {
		List<Integer> backwardsMapping = new ArrayList<Integer>();

		int numberOfNotes = 0;
		for (int i : chordSizes) {
			numberOfNotes += i;
		}
		int highestNoteIndex = numberOfNotes - 1;
		int numberOfChords = chordSizes.size();
		for (int i = numberOfChords - 1; i >= 0; i--) {
			int currentChordSize = chordSizes.get(i);
			int lowestNoteIndex = highestNoteIndex - (currentChordSize - 1);
			for (int j = lowestNoteIndex; j <= highestNoteIndex; j++) {
				backwardsMapping.add(j);
			}
			highestNoteIndex = lowestNoteIndex - 1; 
		}
		return backwardsMapping;
	}


	// =================================== MISCELLANEOUS/OBSOLETE ===================================
	
	public static void scaleFeatureVectorDifferentTests(List<Double> featureVector, double[][] minAndMaxFeatureValues,
		ModellingApproach argModellingApproach) {
		
		// OLD
//		for (int i = 0; i < featureVector.size(); i++) {
//			double f = featureVector.get(i);
//			double minValue = minAndMaxFeatureValues[0][i];
//			double maxValue = minAndMaxFeatureValues[1][i];
//			double fScaled = 0.0;
//			// If min- and maxValue are equal, the denominator will be 0. In this case, fScaled retains its initial
//			// value 0.0. In all other cases: calculate fScaled
//			if (minValue != maxValue) {
//				fScaled = (f - minValue) / (maxValue - minValue);
//			}
//			featureVector.set(i, fScaled);
//		}
		
		// N2N
		if (argModellingApproach == ModellingApproach.N2N) {
			int indexOfIsOrnam = -1;
			int indexOfMetPos = -1;
			List<Integer> indicesOfAlreadyOcc = null; 	
			int indexOffOnProx; // = -1;
			int firstIndexOfProx = -1;
			int lastIndexOfProx = -1;
			int indexOfMaxDur = 4; 
//			if (isTablatureCase) {
//				indexOffOnProx = 31;
//				firstIndexOfProx = 21;
//				lastIndexOfProx = 35;
//			}
//			else {
//				indexOffOnProx = 22;
//				indexOfIsOrnam = 2;
//				indexOfMetPos = 3;
//				firstIndexOfProx = 12;
//				lastIndexOfProx = 26;
//				indicesOfAlreadyOcc = Arrays.asList(new Integer[]{27, 28, 29, 30, 31});
//				indexOfProx = 12;
//				indexOfOcc = 27;
//			}
			
//			for (int i = 0; i < featureVector.size(); i++) {
//				double f = featureVector.get(i);
//				double fScaled = -1;
//				double minF = minAndMaxFeatureValues[0][i];
//				double maxF = minAndMaxFeatureValues[1][i];
//				if (i < firstIndexOfProx && i != indexOfIsOrnam && i != indexOfMetPos) {
//					if (f != -1) {
//						if (minF != maxF) {
//							if (zeroToOne) {
//								fScaled = (f - minF) / (maxF - minF);
//							}
//							else {
//								fScaled = ( (f - minF) / (maxF - minF) - 0.5) * 2;
//							}
//						}
//						else {
//							throw new RuntimeException("ERROR: Scaling error (see console).");
//						}
//					}
//					else {
//						fScaled = f;
//					}
//				
//					// Replace f with fScaled
//					featureVector.set(i, fScaled);
//				}
//			}
			
			
			for (int i = 0; i < featureVector.size(); i++) {
				double f = featureVector.get(i);
				double fScaled = -1;
				// Scale only if value is not -1
				if (f != -1) {
					double minF = minAndMaxFeatureValues[0][i];
					double maxF = minAndMaxFeatureValues[1][i];
					if (minF != maxF) {
//						if (zeroToOne) {
							fScaled = (f - minF) / (maxF - minF);
//						}
//						else {
//							fScaled = ( (f - minF) / (maxF - minF) - 0.5) * 2;
//						}
					}
					// NB: else happens only at last items of voicesWithAdjNoteOnSameCourse and voiceAlreadyOccupied
					else {
//						System.out.println("i = " + i);
//						System.out.println("minF = " + minF);
//						System.out.println("maxF = " + maxF);
//						if (zeroToOne) {
							fScaled = 0.0;
//							fScaled = -1.0;
//						}
//						else {
//							fScaled = 0.0;
//							fScaled = -1.0;
//						}	
//			    		System.out.println("Division by 0 in feature scaling.");			    		
//			        	throw new RuntimeException("ERROR: Scaling error (see console).");
					}
					// Replace f with fScaled
					featureVector.set(i, fScaled);		
				}
				else {
//					if (zeroToOne) {
						featureVector.set(i, -1.0);
//					}
//					else {
//						featureVector.set(i, -1.0);
//					}
				}
			}
//			System.out.println(featureVector);

			// ZOALS EERST
//			for (int i = 0; i < featureVector.size(); i++) {
//				double f = featureVector.get(i);
//				double fScaled = -1;
//				// Scale only if not an offset-onset prox / a prox
////				if (i < 31 || i > 35) {
//				if (i < firstIndexOfProx || i > lastIndexOfProx) {
////				if (i < indexOffOnProx || i > indexOffOnProx + (Transcription.MAXIMUM_NUMBER_OF_VOICES - 1)) {
//					// If maxDuration exceeds 8: set to max value
////					if (isTablatureCase && i == 4 && f > 8) {
////						fScaled = 1.0;
////					}
//					// Scale only if value is not -1
//					if (f != -1) {
////					else if (f != -1) {
//						double minF = minAndMaxFeatureValues[0][i];
//						double maxF = minAndMaxFeatureValues[1][i];
//						if (minF != maxF) {
//							if (zeroToOne) {
//								fScaled = (f - minF) / (maxF - minF);
//							}
//							else {
//								fScaled = ( (f - minF) / (maxF - minF) - 0.5) * 2;
//							}
//						}
//						// NB: else happens only at last items of voicesWithAdjNoteOnSameCourse and voiceAlreadyOccupied
//						else {
////							System.out.println("i = " + i);
////							System.out.println("minF = " + minF);
////							System.out.println("maxF = " + maxF);
////								fScaled = 0.0;
//							if (zeroToOne) {
//								fScaled = 0.0;
//							}
//							else {
//								fScaled = -1.0;
//							}	
////			    			System.out.println("Division by 0 in feature scaling.");
////			          		throw new RuntimeException("ERROR: Scaling error (see console).");
//						}
//						// Replace f with fScaled
//						featureVector.set(i, fScaled);		
//					}
//				}
//			}
		}

		// C2C
		if (argModellingApproach == ModellingApproach.C2C) {
			for (int i = 0; i < featureVector.size(); i++) {
				double f = featureVector.get(i);
				double minF = minAndMaxFeatureValues[0][i];
				double maxF = minAndMaxFeatureValues[1][i];
				double fScaled;
				// 1. If f is an indexInMapping: scale by max index
				if (indicesInMapping.contains(i)) {
//					int maxIndex = Transcription.MAXIMUM_NUMBER_OF_VOICES - 1 - 1; // TODO
					int maxIndex = Transcription.MAXIMUM_NUMBER_OF_VOICES - 1; // TODO
					if (f == -1) {
						fScaled = f;
					}
					else {
						fScaled = f/maxIndex; 
//						fScaled = ( (f/maxIndex) - 0.5) * 2;
					}
				}
				// 2. If f is one of the other features
				else {
					// a. If f can legitimately be -1: scale
					if (indicesOfPitchMvmt.contains(i)) {
						if (minF != maxF) {
							fScaled = (f - minF) / (maxF - minF);
//							fScaled = ( (f - minF) / (maxF - minF) - 0.5) * 2;
						}
						// If minF == maxF == 0 (when the voice is not active): scale to -1.0
						// NB: If f == 0, this can mean:
						// a. The voice is active but has no previous note (first chord)
						// b. The voice is active and has a previous note with the same pitch
						// c. The voice is inactive
						// Inconsistency: a. and b. above get scaled to scaled to (0.0 - minF) / (maxF - minF);
						// c. gets scaled automatically to -1.0
						else {
//							System.out.println("fMin = fMax = " + minF);
							fScaled = -1.0;  
						}
					}
					else if (i == indexOfPitchVoiceRel) {
						fScaled = (f - minF) / (maxF - minF);
//						fScaled = ( (f - minF) / (maxF - minF) - 0.5) * 2;
					}
					// b. If not: scale only if f is not -1
					else {
						if (f == -1) {
							fScaled = f; 
						}
						else {
							if (minF != maxF) {
								fScaled = (f - minF) / (maxF - minF);
//								fScaled = ( (f - minF) / (maxF - minF) - 0.5) * 2;
							}
							// NB: else happens only in alreadyOccupied; set to equivalent of 0
							else {
//								System.out.println("i = " + i);
//								System.out.println("minF = " + minF);
//								System.out.println("maxF = " + maxF);
								fScaled = -1.0;
//					    		System.out.println("Division by 0 in feature scaling.");
//					    		throw new RuntimeException("ERROR: Scaling error (see console).");
							}
						}
					}
				}
				// Replace f with fScaled
				featureVector.set(i, fScaled);
			}
		}
		
		// ZO WAS HET EERDER MET SMART SCALING 0-1
//		if (learningApproach == ExperimentRunner.CHORD_TO_CHORD) {
//			for (int i = 0; i < featureVector.size(); i++) {
//				double f = featureVector.get(i);
//				double minF = minAndMaxFeatureValues[0][i];
//				double maxF = minAndMaxFeatureValues[1][i];
//				double fScaled;
//				// 1. Special cases
//				// a. If f is an indexInMapping: scale by max index
//				if (indicesInMapping.contains(i)) {
//					int maxIndex = Transcription.MAXIMUM_NUMBER_OF_VOICES - 1 - 1; // TODO
//					if (f == -1) {
//						fScaled = f;
//					}
//					else {
//						fScaled = f/maxIndex;
//					}
//				}
//				// b.-c. If f is metPos or a proximity: do not scale
//				else if (i == indexOfMetPos || indicesOfProx.contains(i)) {
//					fScaled = f;
//				}
//				// d. If f is a pitch movement: scale positive values to [0, 1] and negative values to [-1, 0].
//				// If minF = maxF, i.e., when the voice is not active: scale to 0.0
//				else if (indicesOfPitchMvmt.contains(i)) {
//					if (minF != maxF) {
//						fScaled = (f - minF) / (maxF - minF);
//					}
//					else {
//						fScaled = 0.0;
//					}
//				}
//				// e. If f is voiceAlreadyOccupied or pitchVoiceRelation: do not scale
//				else if (indicesOfAlreadyOcc.contains(i) || i == indexOfPitchVoiceRel) {
//					fScaled = f;
////					if (minF != maxF) {
////						fScaled = (f - minF) / (maxF - minF);
////					}
////					// 
////					else {
////						fScaled = 0.0;
////					}
//				}
//				// 2. Normal cases: if f is one of the other features
//				else {
//					// If f = -1: do not scale
//					if (f == -1) {
//						fScaled = f;
//					}
//					else {
//						if (minF != maxF) {
//								fScaled = (f - minF) / (maxF - minF);
//						}
//						else {
//							System.out.println("i = " + i);
//							System.out.println("minF = " + minF);
//							System.out.println("maxF = " + maxF);
//							fScaled = -1.0;
//				    		System.out.println("Division by 0 in feature scaling.");
//				          	throw new RuntimeException("ERROR: Scaling error (see console).");
//						}
//					}	
//				}
//				// Replace f with fScaled
//				featureVector.set(i, fScaled);
//			}
//		}
	}
	
	/** 
	 * Returns 1.0 if the note at noteIndex is on the grid of eights and 0.0 if not. A note is not on the grid 
	 * if it is
	 * (1) at the position of the second or fourth 16th note within a beat
	 * (2) at the position of the second, fourth, sixth, or eighth 32nd note within a beat
	 * 
	 * NB: This method applies to the tablature case only // TODO change?
	 *     
	 * @param basicTabSymbolProperties
	 * @param noteIndex
	 * @return
	 */
	private double isOnGridOfEights(Integer[][] basicTabSymbolProperties, int noteIndex) {
		
		int onsetTime = basicTabSymbolProperties[noteIndex][Tablature.ONSET_TIME];
		Rational metricTime = new Rational(onsetTime, Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
		
		// 1. If the note is at the position of the second, fourth, sixth, or eighth 32nd note within a beat, 
		// the numerator of metricTime will be odd (i.e., numerator modulo 2 != 0)
		if (metricTime.getNumer() % 2 != 0) {
			return 0.0;
		}
		// 2. If the note is at the position of the second or fourth 16th note within a beat, the numerator of
		// metricTime will be even (i.e., numerator mod 2 = 0), but divided by 2 odd (i.e., (numerator/2) mod 2 != 0)
		else if (metricTime.getNumer() % 2 == 0 && (metricTime.getNumer() / 2) % 2 != 0) {
			return 0.0;
		}
		// 3. In all other cases, the note is on the grid of eights
		else {
			return 1.0;
		}
	}
	
	
	/**
	 * If the note at noteIndex falls on the grid of 8th notes: gets for the chords at the distances of 1/8, 2/8, 
	 * and 3/8 (thus covering the area of the note's maximum duration) the pitch proximity and the course for (1) 
	 * the note whose pitch is directly below that of the note at noteIndex; (2) the note whose pitch is the same
	 * as that of the note at noteIndex; and (3) the note whose pitch is directly above that of the note at noteIndex.
	 * If:
	 * a) any of these notes is part of a unison, the pitch and course of the unison note whose course is closest 
	 *    to that of the note at noteIndex is listed
	 * b) any of these notes is not present, two default values of -1.0 are listed
	 * c) the note at noteIndex is in the last chord of the piece, a double[] containing only -1.0s is returned 
	 * d) the note at noteIndex does not fall on the grid of 8th notes, a double[] containing only -1.0s is returned  
	 * 
	 * NB: This method applies to the tablature case only and is tablature-specific.
	 *  
	 * @param basicTabSymbolProperties
	 * @param noteIndex
	 * @return
	 */
	private double[] getProximitiesAndCourseInfoAheadOfChordOnGrid(Integer[][]basicTabSymbolProperties, int noteIndex) {
		
		double[] pitchProxAndCourse = new double[18];
		int onsetTimeCurrentNote = basicTabSymbolProperties[noteIndex][Tablature.ONSET_TIME];
		int pitchCurrentNote = basicTabSymbolProperties[noteIndex][Tablature.PITCH];
		int courseCurrentNote = basicTabSymbolProperties[noteIndex][Tablature.COURSE];
				
		// 1. Check whether pitchProxAndCourse must be set completely with its default values, which is the case if
		// a. The note is in the last chord
		int onsetTimeOfLastNote = 
				basicTabSymbolProperties[basicTabSymbolProperties.length - 1][Tablature.ONSET_TIME];
		if (onsetTimeCurrentNote == onsetTimeOfLastNote) {
			Arrays.fill(pitchProxAndCourse, -1.0);
		}
		// b. The note is not on the grid of 8th notes 
		else if (isOnGridOfEights(basicTabSymbolProperties, noteIndex) == 0.0) {
			Arrays.fill(pitchProxAndCourse, -1.0);
		}
		// 2. If the note is not in the last chord and on the grid of 8th notes: set pitchProxAndCourse
		else {
			// 1. Find any chord at 1/8, 2/8, and 3/8 distance and represent it as a list of pitch-course pairs. If 
			// there is no chord at any of these distances, the corresponding list remains empty
			List<List<Integer>> chordAtOneEight = new ArrayList<List<Integer>>();
			List<List<Integer>> chordAtTwoEights = new ArrayList<List<Integer>>();
			List<List<Integer>> chordAtThreeEights = new ArrayList<List<Integer>>();
			List<List<List<Integer>>> allChords = new ArrayList<List<List<Integer>>>();
			for (int i = noteIndex + 1; i < basicTabSymbolProperties.length; i++) {
				int onsetTimeNextNote = basicTabSymbolProperties[i][Tablature.ONSET_TIME];
				int pitchNextNote = basicTabSymbolProperties[i][Tablature.PITCH]; 
				int courseNextNote = basicTabSymbolProperties[i][Tablature.COURSE];
				// Skip any notes in the same chord as the current note
				if (onsetTimeNextNote != onsetTimeCurrentNote) {
					// a. For a chord at 1/8 distance
				  if (onsetTimeNextNote == onsetTimeCurrentNote + 4) {
					  chordAtOneEight.add(Arrays.asList(new Integer[]{pitchNextNote, courseNextNote}));
				  }
				  // b. For a chord at 2/8 distance
				  else if (onsetTimeNextNote == onsetTimeCurrentNote + 8) {
					  chordAtTwoEights.add(Arrays.asList(new Integer[]{pitchNextNote, courseNextNote}));
				  }
				  // c. For a chord at 3/8 distance
				  else if (onsetTimeNextNote == onsetTimeCurrentNote + 12) {
					  chordAtThreeEights.add(Arrays.asList(new Integer[]{pitchNextNote, courseNextNote}));
				  }
				  // d. 3/8 distance exceeded? Add the lists to allChords and break from for-loop
				  else if (onsetTimeNextNote > onsetTimeCurrentNote + 12) {
				  	allChords.add(chordAtOneEight); 
						allChords.add(chordAtTwoEights); 
						allChords.add(chordAtThreeEights); 
					  break;
				  }
				}
			}
			
			// 2. Set appropriate elements of pitchProxAndCourse
			// For each chord at each distance 
      for (int i = 0; i < allChords.size(); i++) {
      	List<List<Integer>> chordAtCurrentDistance = allChords.get(i);		
      	
      	// 0. Set the index in pitchProxAndCourse (which is either 0, 6, or 12)
      	int arrayIndex = i * 6;  	
      	
      	// 1. Make lists for the values of the note at the closest lower, same, and closest higher pitch and initialise
      	// the lists with default values. They retain these values if there is no chord at the current distance; if
      	// there is, the default values will be replaced in the if below
      	List<Integer> valuesNoteAtClosestLowerPitch = Arrays.asList(new Integer[]{-1, -1});
			  List<Integer> valuesNoteAtSamePitch = Arrays.asList(new Integer[]{-1, -1});
			  List<Integer> valuesNoteAtClosestHigherPitch = Arrays.asList(new Integer[]{-1, -1});
			  // If there is a chord at the current distance
      	if (chordAtCurrentDistance.size() != 0) {
				  // 1. Get all the pitches in the chord and determine closetLowerPitch and closestHigherPitch
      	  List<Integer> pitchesInChord = new ArrayList<Integer>();
      	  int closestLowerPitch = Integer.MIN_VALUE;
      	  int closestHigherPitch = Integer.MAX_VALUE;
      	  for (List<Integer> l : chordAtCurrentDistance) {
				  	pitchesInChord.add(l.get(0));
				  }
				  for (int pitch : pitchesInChord) {
				  	if (pitch > closestLowerPitch && pitch < pitchCurrentNote) {
				  	  closestLowerPitch = pitch;
				  	}
				  	if (pitch < closestHigherPitch && pitch > pitchCurrentNote) {
				  	  closestHigherPitch = pitch;
				  	} 
				  }									
				  // 2. Select the pitch and course of the appropriate notes in the current next chord. If the closest lower 
				  // pitch, the same pitch, or the closest higher pitch appears twice (i.e., if they are part of a unison),
				  // the if comparing the course distances makes sure that the note at the shortest course distance is listed
		  		int courseDistLower = Integer.MAX_VALUE;
		  		int courseDistSame = Integer.MAX_VALUE;
			  	int courseDistHigher = Integer.MAX_VALUE;
		  		for (List<Integer> pitchAndCourseCurrentNextNote : chordAtCurrentDistance) {
		  			int pitchCurrentNextNote = pitchAndCourseCurrentNextNote.get(0);
		  			int courseCurrentNextNote = pitchAndCourseCurrentNextNote.get(1);
		  			int currentCourseDist = Math.abs(courseCurrentNote - courseCurrentNextNote);
		  			// a. If pitchCurrentNextNote is the closest lower pitch
		  			if (pitchCurrentNextNote == closestLowerPitch) {
		  				if (currentCourseDist < courseDistLower) {
		  					courseDistLower = currentCourseDist;
		  					valuesNoteAtClosestLowerPitch = pitchAndCourseCurrentNextNote;
		  				}
		  			}
		  			// b. If pitchCurrentNextNote is the same pitch
		  			if (pitchCurrentNextNote == pitchCurrentNote) {
		  			  if (currentCourseDist < courseDistSame) {
		  			  	courseDistSame = currentCourseDist;
		  				  valuesNoteAtSamePitch = pitchAndCourseCurrentNextNote;
		  			  }
		  			}
		  			// c. If pitchCurrentNextNote is the closest higher pitch
		  			if (pitchCurrentNextNote == closestHigherPitch) {
		  			  if (currentCourseDist < courseDistHigher) {
		  			  	courseDistHigher = currentCourseDist;
		  				  valuesNoteAtClosestHigherPitch = pitchAndCourseCurrentNextNote;
		  			  }
		  			}
		  		}
      	}
				// 2. Combine the lists and set the appropriate elements of pitchProxAndCourse
				List<Integer> valuesForChordAtCurrentDistance = new ArrayList<Integer>();
				valuesForChordAtCurrentDistance.addAll(valuesNoteAtClosestLowerPitch);
				valuesForChordAtCurrentDistance.addAll(valuesNoteAtSamePitch);
				valuesForChordAtCurrentDistance.addAll(valuesNoteAtClosestHigherPitch);
				for (int j = 0; j < valuesForChordAtCurrentDistance.size(); j++) {
					int currentValue = valuesForChordAtCurrentDistance.get(j);
					// a. If there is no note at the closest lower, same, or closest higher pitch: set elements to default value
					if (currentValue == -1.0) {
						pitchProxAndCourse[arrayIndex + j] = -1.0;
					}
					// b. If there is: set elements to pitch proximity or course
					else {
						if (j == 0 || j == 2 || j == 4) {
							int pitchDist = Math.abs(pitchCurrentNote - currentValue); 
							pitchProxAndCourse[arrayIndex + j] = 1.0 / (pitchDist + 1);
						}
						else {
							pitchProxAndCourse[arrayIndex + j] = currentValue;
						}
					}
			  }
      }
		}	
		// 3. Return pitchProxAndCourse
		return pitchProxAndCourse;	
	}
		
}

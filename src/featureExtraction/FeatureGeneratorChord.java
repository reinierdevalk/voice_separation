package featureExtraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import representations.Tablature;
import representations.Transcription;
import tools.ToolBox;
import utility.DataConverter;
import de.uos.fmt.musitech.data.score.NotationVoice;
import de.uos.fmt.musitech.data.structure.Note;
import de.uos.fmt.musitech.utility.math.Rational;
import featureExtraction.FeatureGenerator.Direction;

public class FeatureGeneratorChord {

	private FeatureGenerator fg = new FeatureGenerator();
	private boolean modelDuration;

	// =================================== FEATURE EXTRACTION ===================================
	/**
	 * Gets the note-specific features for each note in the chord. Returns a List<List<Double>> whose first
	 * element represents the lowest note and that contains for each note 
	 *   as element 0: the index (sequence number) within the chord, excluding sustained previous notes (= the
	 *                 index in the NoteSequence = the index in the voice assignment vector)
	 *   as element 1: the index (sequence number) within the chord, including sustained previous notes (if
	 *                 modelling duration and in the non-tablature case)
	 *                 In the case of pitch overlap within the chord (unison, sustained previous note, or any
	 *                 combination), any sustained previous notes are listed first (in the order they appear
	 *                 in the NoteSequence), and any notes in the current chord second (again, in the order 
	 *                 they appear in the NoteSequence).
	 *   as element 2: the pitch (as a MIDI number) 
	 * in the tablature case:  
	 *   as element 3: the course
	 *   as element 4: the fret
	 *   as element 5: the maximum duration (in whole notes)     
	 * in the non-tablature case:
	 *   as element 3: the full duration (in whole notes) of the Note
	 *
	 * If the chord has fewer notes than Transcription.MAXIMUM_NUMBER_OF_VOICES, a List containing only -1.0s 
	 * is added for each "missing" note. 
	 *
	 * @param btp
	 * @param bnp
	 * @param meterInfo
	 * @param lowestNoteIndex
	 * @return
	 */
	// TESTED (for both tablature- and non-tablature case)
	static List<Double> getNoteSpecificFeaturesChord(Integer[][] btp, Integer[][] bnp, /*Transcription transcription,*/
		List<Integer[]> meterInfo, int lowestNoteIndex /*boolean useTablatureInformation*/) {
		
		Transcription.verifyCase(btp, bnp);
	  List<Double> features = new ArrayList<Double>();
	  
		for (int i = 0; i < Transcription.MAXIMUM_NUMBER_OF_VOICES; i++) {
			// a. In the tablature case
		  if (btp != null) {
			  int numNotesInChord = btp[lowestNoteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
			  if (i < numNotesInChord) {
					int noteIndex = lowestNoteIndex + i;
		    	double[] basicNoteFeaturesAsArray =	FeatureGenerator.getBasicNoteFeatures(btp, bnp, meterInfo, noteIndex);
		    	// 0. Index in voice assignment
		    	features.add((double)i);
		    	// 1. Index inclusive
		    	boolean modelDuration = false;
		    	if (modelDuration) { // TODO
		    		List<List<Double>> durationLabels = null;
		    		features.add((double) FeatureGenerator.getIndexInclusive(btp, durationLabels, bnp, noteIndex));
		    	}
		    	else {
		    		features.add((double) FeatureGenerator.getIndexExclusive(btp, bnp, noteIndex));
		    	}
		    	// 2. Pitch
		    	features.add(basicNoteFeaturesAsArray[FeatureGenerator.PITCH]);
		    	// 3. Course
		    	features.add(basicNoteFeaturesAsArray[FeatureGenerator.COURSE]);
			  	// 4. Fret
		    	features.add(basicNoteFeaturesAsArray[FeatureGenerator.FRET]);
			  	// 5. Maximum duration
		    	features.add(basicNoteFeaturesAsArray[FeatureGenerator.MAX_DURATION]);
			  } 
			  else {
			    features.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0, -1.0}));
			  }
			}
	    // b. In the non-tablature case
		  else {
			  int numNotesInChord = bnp[lowestNoteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
			  if (i < numNotesInChord) {
  				int noteIndex = lowestNoteIndex + i;
  		  	// 0. Index in voice assignment 
  		  	features.add((double) i);
  		    // 1. Index inclusive
  		  	features.add((double) FeatureGenerator.getIndexInclusive(btp, null, bnp, noteIndex));
  		  	// 2. Pitch
  		  	features.add((double) bnp[noteIndex][Transcription.PITCH]);
  		  	// 3. Duration
  	    	features.add(new Rational(bnp[noteIndex][Transcription.DUR_NUMER],
    		  	bnp[noteIndex][Transcription.DUR_DENOM]).toDouble());
			  }
			  else {
			    features.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0}));	
			  }
			}
		}
	
		return features;
	}
	
	/**
	 * Gets the chord-level features. Returns a List<List<Double>> containing for each note 
	 * in the tablature case:
	 *   as element 0:     the size of the chord, including any sustained previous notes (if modelling duration)
	 *   as element 1:     the minimum duration of the chord, as indicated in the tablature
	 *   as element 2:     whether the tablature note has ornamentation characteristics, which is so if it is the only
	 *                     onset (not necessarily the only note!) in the chord and has a duration of a 16th or smaller 
	 *   as element 3:     the metric position within the bar
	 *   as element 4-7:   the intervals in the chord, including any sustained previous notes (if modelling duration)  
	 * in the non tablature case:  
	 *   as element 0:     the size of the chord, including any sustained previous notes
	 *   as element 1:     whether the tablature note has ornamentation characteristics, which is so if it is the only
	 *                     onset (not necessarily the only note!) in the chord and has a duration of a 16th or smaller 
	 *   as element 2:     the metric position within the bar
	 *   as element 3-6:   the intervals in the chord, including any sustained previous notes
	 *
	 * If the chord has fewer notes than Transcription.MAXIMUM_NUMBER_OF_VOICES, a List containing only -1.0s 
	 * is added for each "missing" note.
	 * 
	 * @param btp
	 * @param bnp
	 * @param transcription
	 * @param meterInfo
	 * @param lowestNoteIndex
	 * @return
	 */
	// TESTED for both tablature- and non-tablature case
	static List<Double> getChordLevelFeaturesChord(Integer[][] btp, Integer[][] bnp, /*Transcription transcription,*/ 
		List<Integer[]> meterInfo, int lowestNoteIndex) {
		
		Transcription.verifyCase(btp, bnp);
		List<Double> features = new ArrayList<Double>();
		
    double[] basicNoteFeaturesAsArray = FeatureGenerator.getBasicNoteFeatures(btp, bnp, meterInfo, lowestNoteIndex);
  	// a. In the tablature case
    if (btp != null) {
    	boolean modelDuration = false;
      // 0. Chord size
    	if (modelDuration) {
    		// TODO
    	}
    	else {
    	  features.add((double) btp[lowestNoteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS]);
    	}
    	// 1. Minimum duration
    	features.add(basicNoteFeaturesAsArray[FeatureGenerator.MIN_DURATION]);
    	// 2. Has ornamentation characteristics
    	features.add(basicNoteFeaturesAsArray[FeatureGenerator.IS_ORNAMENTATION]);
    	// 3. Metric position
    	features.add(basicNoteFeaturesAsArray[FeatureGenerator.POSITION_WITHIN_BAR]);
    	// 4-7. Intervals
    	List<List<Double>> durationLabels = null;
    	if (modelDuration) {
    	  // TODO give durationLabels value 	
    	}
    	double[] intervals = FeatureGenerator.getIntervalsInChord(btp, durationLabels, bnp, lowestNoteIndex);
    	for (Double d : intervals) {
    	  features.add(d);
    	}
    	// 8-12. Voices with adjacent note on same course
//    	double[] voicesWithAdjNoteOnSameCourse = getVoicesWithAdjacentNoteOnSameCourse(btp, transcription, lowestNoteIndex);
//    	for (Double d: voicesWithAdjNoteOnSameCourse) {
//    		features.add(d);
//    	}
    	
    	// 9-12. Sustained voices already assigned
//    	if (modelDuration) {
//    		// TODO
//    	}
//    	else {
//    		Double[] sustVoicesAssigned = new Double[Transcription.MAXIMUM_NUMBER_OF_VOICES];
//    		Arrays.fill(sustVoicesAssigned, 0.0);
//    		features.addAll(Arrays.asList(sustVoicesAssigned));
//    	}
    	
    	// 13-45. Info ahead + numNotesNextChord
//  		double[] prox = getProximitiesAndCourseInfoAheadChord(btp, bnp, lowestNoteIndex, NUM_NEXT_CHORDS);
//  		for (double d : prox) {
//  			features.add(d);
//  		} 			
    }
    // b. In the non-tablature case
    else {
      // 0. Chord size
    	features.add((double) FeatureGenerator.getSizeOfChordInclusive(btp, null, bnp, lowestNoteIndex));
    	// 1. Has ornamentation characteristics
    	features.add(basicNoteFeaturesAsArray[FeatureGenerator.IS_ORNAMENTATION_NON_TAB]);
    	// 2. Metric position
    	features.add(basicNoteFeaturesAsArray[FeatureGenerator.POSITION_WITHIN_BAR_NON_TAB]);
    	// 3-6. Intervals
    	double[] intervals = FeatureGenerator.getIntervalsInChord(btp, null, bnp, lowestNoteIndex);
    	for (Double d : intervals) {
    	  features.add(d);
    	}
    	// 7-11. Sustained voices already assigned 
//    	List<Double> voicesAlreadyAssigned = new ArrayList<Double>();
//  		for (int i = 0; i < Transcription.MAXIMUM_NUMBER_OF_VOICES; i++) {
//  			voicesAlreadyAssigned.add(0.0);
//  		}
//    	List<Integer> v = getVoicesOfSustainedPreviousNotesInChord(btp, null, null, bnp, 
//    		transcription.getVoiceLabels(),	lowestNoteIndex);
//  		for (int i : v) {
//  			voicesAlreadyAssigned.set(i, 1.0);
//  		}
//  		features.addAll(voicesAlreadyAssigned);
    	
  		// 12-29. Info ahead + numNotesNextChord
//  		double[] prox = getProximitiesAndCourseInfoAheadChord(btp, bnp, lowestNoteIndex, NUM_NEXT_CHORDS);
//  		for (double d : prox) {
//  			features.add(d);
//  		}
    }
    return features;
	}
	
	
	/**
	 * Returns a double[] of size Transcription.MAXIMUM_NUMBER_OF_VOICES, each element of which is either -1 
	 * (indicating that the chord has no adjacent note in the corresponding voice) or n (where 0 < n <= 4)
	 * (indicating that the note at index in the chord n has an adjacent note in the corresponding voice).
	 * 
	 * @param btp
	 * @param transcription
	 * @param lowestNoteIndex
	 * @return
	 */
	// TESTED
	double[] getVoicesWithAdjacentNoteOnSameCourse(Integer[][] btp, Transcription transcription, int lowestNoteIndex) {
		
		double[] voicesWithAdjacentNotes = new double[Transcription.MAXIMUM_NUMBER_OF_VOICES];
		Arrays.fill(voicesWithAdjacentNotes, -1.0);
					
	  int numNotesInChord = btp[lowestNoteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
	  for (int i = 0; i < numNotesInChord; i++) {
	  	int noteIndex = lowestNoteIndex + i;
	  	double[] voicesWithAdjacentNoteOnSameCourse = 
	  		FeatureGenerator.getVoicesWithAdjacentNoteOnSameCourse(btp, transcription, Direction.LEFT, noteIndex); // TODO Change Direction.LEFT into method argument
	  	for (int j = 0; j < voicesWithAdjacentNoteOnSameCourse.length; j++) {
	  		if (voicesWithAdjacentNoteOnSameCourse[j] == 1.0) {
  		 		voicesWithAdjacentNotes[j] = i;
  		 	}
	  	}
	  }  
		return voicesWithAdjacentNotes;
	}
	
	
	/**
	 * Compares each note in the chord to the previous Note in the voice that note is assigned to under the given
	 * voiceAssigment and calculates the pitch proximity, inter-onset time proximity, offset-onset time proximity,
	 * and the pitch movement for the voice.
	 * Returns a double[] containing:
	 *   as element 0-4:   the pitch proximity for each voice 
	 *   as element 5-9:   inter-onset time proximity for each voice
	 *   as element 10-14: the offset-onset time proximity values for each voice (in the tablature case, the
	 *                     previous Note's offset time is determined by the minimum duration as given in the 
	 *                     tablature) 
	 *   as element 15-19: the pitch movement of each voice
	 * If the voice is not represented in the chord, a default value of -1.0 is used for the proximities and
	 * one of 0.0 for the movement.   
	 * 
	 * NB: In the tablature case, when a note is a CoD, the pitch proximities and movement to the previous Note
	 *     in both CoD voices are calculated.  
	 * 
	 * @param btp
	 * @param bnp
	 * @param transcription
	 * @param lowestNoteIndex
	 * @param voiceAssignment
	 * @return
	 */
	// TESTED (for both tablature- and non-tablature case)
	static double[] getProximitiesAndMovementsOfChord(Integer[][] btp, Integer[][] bnp, Transcription transcription,
		int lowestNoteIndex, List<Integer> voiceAssignment) { 	

		Transcription.verifyCase(btp, bnp);

		// Create the proximity arrays and initialise with all -1.0 (when the voice is not active in the chord)  
		double[] pitchProx = new double[Transcription.MAXIMUM_NUMBER_OF_VOICES];
		Arrays.fill(pitchProx, -1.0);
		double[] interOnsetProx = new double[Transcription.MAXIMUM_NUMBER_OF_VOICES];
		Arrays.fill(interOnsetProx, -1.0);
		double[] offsetOnsetProx = new double[Transcription.MAXIMUM_NUMBER_OF_VOICES];
		Arrays.fill(offsetOnsetProx, -1.0);

		// Create the pitch movements array and initialise with all 0.0 (when the voice is not active in the chord)
		// NB: distinguish:
		// (i) If voice x is not active in the chord, i.e., if element x of voiceAssignment is -1, pitchMovements[x] will
		// be 0.0;
		// (ii) If voice x is active in the chord, i.e., if element x of voiceAssignment is not -1, pitchMovements[x] will
		// be 0.0 if there is no previous Note in voice x before the current chord; 0.0 if there is a previous Note in 
		// voice x with the same pitch as the note in voice x in the current chord, or some positive or negative number if
		// there is a previous Note in voice x with another pitch than the note in voice x in the current chord
		// This means that:
		// In the tablature case, 0.0 can indicate that voice x 
		//   (a) has no new note in the chord (is not active); 
		//   (b) has a new note in the chord, which is the first note in that voice;
		//   (c) has a new note in the chord that is of the same pitch as the previous note in that voice
		// In the non-tablature case, 0.0 can indicate that voice x
		//   (a) has no new note in the chord (is not active); 
		//   (b) has no new note in the chord but is sustained; 
		//   (c) has a new note in the chord, which is the first note in that voice; 
		//   (d) has a new note in the chord that is of the same pitch as the previous note in that voice
		double[] pitchMovements = new double[Transcription.MAXIMUM_NUMBER_OF_VOICES];
		Arrays.fill(pitchMovements, 0.0);

		// Get the voice labels that go with the given voice assignment; then get the voices
		List<List<Double>> chordVoiceLabels = DataConverter.getChordVoiceLabels(voiceAssignment);
		List<List<Integer>> voices = DataConverter.getVoicesInChord(chordVoiceLabels);

		// Determine the size of the chord
		// a. In the tablature case
		int chordSize = 0;
		if (btp != null) {
			chordSize = btp[lowestNoteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS]; 
		}
		// b. In the non-tablature case
		else {
			chordSize = bnp[lowestNoteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
		}

		// For each note in the chord
		for (int i = 0; i < chordSize; i++) {
			int currentNoteIndex = lowestNoteIndex + i; 
			Note currentNote = null;
			// a. In the tablature case
			if (btp != null) {
				currentNote = Tablature.convertTabSymbolToNote(btp, currentNoteIndex);
			}
			// b. In the non-tablature case
			else {
				int pitch = bnp[currentNoteIndex][Transcription.PITCH];
				Rational metricTime = new Rational(bnp[currentNoteIndex][Transcription.ONSET_TIME_NUMER], 
					bnp[currentNoteIndex][Transcription.ONSET_TIME_DENOM]);
				Rational metricDuration = new Rational(bnp[currentNoteIndex][Transcription.DUR_NUMER], 
					bnp[currentNoteIndex][Transcription.DUR_DENOM]);
				currentNote = Transcription.createNote(pitch, metricTime, metricDuration);
			}
			List<Integer> currentVoices = voices.get(i);

			// For each voice the note is assigned to: calculate the proximities to the previous Note in that voice
			// and the pitch movement, and set the corresponding elements in the Arrays
			for (int j = 0; j < currentVoices.size(); j++) {
				int voice = currentVoices.get(j);
				NotationVoice currentVoice = transcription.getPiece().getScore().get(voice).get(0);
				double[] pitchAndTimeProximities = 
				FeatureGenerator.getProximitiesAndMovementToVoiceAll(btp, currentVoice, 
					currentNote, Direction.LEFT, 1, false).get(0); // TODO turn Direction.LEFT into method argument 
				pitchProx[voice] = pitchAndTimeProximities[0];
				interOnsetProx[voice] = pitchAndTimeProximities[1];		  
				offsetOnsetProx[voice] = pitchAndTimeProximities[2];
				// a. In the tablature case
				if (btp != null) {
					pitchMovements[voice] = pitchAndTimeProximities[4];
				}
				// b. In the non-tablature case 
				else {
					pitchMovements[voice] = pitchAndTimeProximities[3];
				}
			}
		}

		List<double[]> all = Arrays.asList(new double[][]{pitchProx, interOnsetProx, offsetOnsetProx, pitchMovements}); 
		return ToolBox.concatDoubleArrays(all);
	}


	/**
	 * Gets the voices thatare already occupied, i.e., the voices for any notes sustained beyond the onset time 
	 * of the chord at lowestNoteIndex. Returns a binary vector indicating with 1.0s which voices are occupied. 
	 * 
	 * @param btp
	 * @param bnp
	 * @param transcription
	 * @param lowestNoteIndex
	 * @return
	 */
	// TESTED for both tablature- and non-tablature case
	static List<Double> getVoicesAlreadyOccupied(Integer[][] btp, List<List<Double>> durationLabels, List<Integer[]> 
	  voicesCoDNotes,	Integer[][] bnp, Transcription transcription, int lowestNoteIndex) {
	  
		Transcription.verifyCase(btp, bnp);
		
		List<Double> voicesAlreadyAssigned = new ArrayList<Double>();
	  for (int i = 0; i < Transcription.MAXIMUM_NUMBER_OF_VOICES; i++) {
		  voicesAlreadyAssigned.add(0.0);
	  }
	  
	  List<Integer> v = 
			 Transcription.getVoicesOfSustainedPreviousNotesInChord(btp, durationLabels, voicesCoDNotes, bnp, 
	  	transcription.getVoiceLabels(),lowestNoteIndex);
	  for (int i : v) {
	  	voicesAlreadyAssigned.set(i, 1.0);
	  }
	  return voicesAlreadyAssigned;
	}


	/**
	 * Expresses the relation between the pitches in the chord and the given voice assignment as Pearson's product-
	 * moment correlation coefficient. If the chord contains only one note, 0.0 (no correlation) is returned.
	 * 
	 * NB: In the tablature case when modelling duration and in the non-tablature case, sustained previous notes
	 *     are taken into consideration as well. TODO Implement tablature case
	 *  
	 * @param btp
	 * @param bnp
	 * @param allVoiceLabels
	 * @param lowestNoteIndex
	 * @param voiceAssignment
	 * @return
	 */ 
	// TESTED (for both tablature- and non-tablature case)
	static double getPitchVoiceRelationInChord(Integer[][] btp, Integer[][] bnp, List<List<Double>> allVoiceLabels,
		int lowestNoteIndex, List<Integer> voiceAssignment) {
		
		Transcription.verifyCase(btp, bnp);
		
		double pitchVoiceRelation;
		
		// 0. Determine the size of the chord 
		int chordSize = 0;
		// a. In the tablature case
		if (btp != null) {
			chordSize = btp[lowestNoteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
//			chordSize += getPitchesOfSustainedPreviousNotesInChord(btp, durationLabels, bnp, lowestNoteIndex).size(); TODO
		}
		// b. In the non-tablature case
		if (bnp != null) {
			chordSize = bnp[lowestNoteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
			chordSize += Transcription.getPitchesOfSustainedPreviousNotesInChord(null, null, bnp, lowestNoteIndex).size();
		}
		
	  // 1. If the chord consists of a single note (even if that note is a CoD): pitchVoiceRelation = 0.0. See 
		// also AuxiliaryTool.calculateCorrelationCoefficient(), where 0.0 is returned if the denominator is 0.0, 
		// which happens when a chord has only two notes and these have the same pitch (so if the chord consists of
		// a unison only)
		if (chordSize == 1) {	
			pitchVoiceRelation = 0.0; // changed from 1.0 to 0.0 on 21-12-2012 (this is probably why the world didn't end)
		}
		// 2. If not: calculate pitchVoiceRelation
		else {
			List<List<Double>> voiceLabels = 
				DataConverter.getChordVoiceLabels(voiceAssignment);
			List<Integer> pitchesInChord = null;
			List<List<Integer>> voicesInChord = 
				DataConverter.getVoicesInChord(voiceLabels);
			// 1. Get pitchesInChord and voicesInChord
			// a. In the tablature case
			// NB: For the alignment of the voice assigment and the pitches, the exact pitch sequence as in the chord
			// (including any possible course crossings) is needed--so no numerical sorting of pitchesInChord is necessary
			if (btp != null) {
				pitchesInChord = Tablature.getPitchesInChord(btp, lowestNoteIndex);
//				pitchesInChord = FeatureGenerator.getPitchesInChord(btp, bnp, lowestNoteIndex);
//				voicesInChord = dataConverter.getVoicesInChord(voiceLabels);
			}
			// b. In the non-tablature case
			if (bnp != null) {
				// Get the pitches and voices of the new onsets in the chord
				pitchesInChord = Transcription.getPitchesInChord(bnp, lowestNoteIndex);
//				pitchesInChord = FeatureGenerator.getPitchesInChord(btp, bnp, lowestNoteIndex);
//				voicesInChord = dataConverter.getVoicesInChord(voiceLabels);
				// Get all the pitches and voices in the chord
				List<List<Integer>> allPitchesAndVoices = Transcription.getAllPitchesAndVoicesInChord(bnp, pitchesInChord,
					voicesInChord, allVoiceLabels, lowestNoteIndex);
				// voicesInChord must be a List<List>>
				pitchesInChord = allPitchesAndVoices.get(0);
				voicesInChord = new ArrayList<List<Integer>>();
				for (int i : allPitchesAndVoices.get(1)) {
					int currentVoice = i;
					List<Integer> voiceWrapped = Arrays.asList(new Integer[]{currentVoice});
					voicesInChord.add(voiceWrapped);
				}
			}
			
		  // 2. Make the list of xs (pitches) and ys (voices)
		  List<Double> xList = new ArrayList<Double>();
		  List<Double> yList = new ArrayList<Double>();
		  for (int i = 0; i < pitchesInChord.size(); i++) {
		  	int currentPitch = pitchesInChord.get(i);
		  	List<Integer> currentVoices = voicesInChord.get(i);
		    // Does currentVoices contain only one element? The current note is no CoD; add currentPitch once
		  	// to xList; add the only element of currentVoices to yList
			  if (currentVoices.size() == 1) {
			  	xList.add((double)currentPitch);
			  	int currentVoice = currentVoices.get(0);
			  	yList.add((double)currentVoice);
			  }
			  // Does currentVoices contain multiple elements? The current note is a CoD; add currentPitch as often 
			  // to xList as currentVoices is long (in practice this will be twice); add all elements of currentVoices 
			  // to yList
			  if (currentVoices.size() > 1) {
			  	for (int j = 0; j < currentVoices.size(); j++) {
			  		xList.add((double)currentPitch);
			  		int currentVoice = currentVoices.get(j);
			  		yList.add((double)currentVoice);
			  	}
			  }
		  }
		  
		  // 3. xList and yList filled? Now determine the correlation coefficient
		  pitchVoiceRelation = ToolBox.calculateCorrelationCoefficient(xList, yList);
		}	
		return pitchVoiceRelation;
	}


	/**
	 * Creates, for each chord of the given Tablature, a List<List<Integer>> containing all the possible voice 
	 * assignments for that chord, ordered such that the ground truth voice assignment is listed first. The
	 * highest possible number of voices is determined by the given number.
	 *  	 	 
	 * @param btp
	 * @param bnp
	 * @param allVoiceLabels
	 * @param highestNumberOfVoices
	 * @return
	 */
  // TESTED (for both tablature- and non-tablature case) (not with concrete numbers)
	public static List<List<List<Integer>>> getOrderedVoiceAssignments(Integer[][] btp, Integer[][] bnp,
		List<List<Double>> allVoiceLabels, int highestNumberOfVoices){	
				
		Transcription.verifyCase(btp, bnp);
				
		List<List<List<Integer>>> orderedVoiceAssignments = new ArrayList<List<List<Integer>>>();
			
		int numberOfChords = 0;
		// a. In the tablature case
		if (btp != null) {
			int indexOfLastNote = btp.length - 1;
		  numberOfChords = btp[indexOfLastNote][Tablature.CHORD_SEQ_NUM] + 1;
		}
		// b. In the non-tablature case
		if (bnp != null) {
			int indexOfLastNote = bnp.length - 1;
		  numberOfChords = bnp[indexOfLastNote][Transcription.CHORD_SEQ_NUM] + 1;
		}
		
		// For each chord
		int lowestNoteIndex = 0;
		for (int i = 0; i < numberOfChords; i++) {
			// a. In the tablature case
			int chordSize = 0;
			if (btp != null) {
			  chordSize = btp[lowestNoteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
			}
			// b. In the non-tablature case
			if (bnp != null) {
				chordSize = bnp[lowestNoteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
			}
		  // Generate all the possible voice assignments
			List<List<Integer>> allPossibleVoiceAssignmentsCurrentChord = 
//				enumerateVoiceAssignmentPossibilitiesForChord(basicTabSymbolProperties,	basicNoteProperties, allVoiceLabels,
//				lowestNoteIndex, highestNumberOfVoices);
				FeatureGeneratorChord.enumerateVoiceAssignmentPossibilitiesForChord(btp, bnp, allVoiceLabels, lowestNoteIndex, 
					highestNumberOfVoices);
			// Determine the ground truth voice assignment
			List<List<Double>> voiceLabelsCurrentChord = 
				new ArrayList<List<Double>>(allVoiceLabels.subList(lowestNoteIndex, lowestNoteIndex + chordSize));
			List<Integer> groundTruthVoiceAssignmentCurrentChord = 
//				dataConverter.getVoiceAssignment(voiceLabelsCurrentChord, highestNumberOfVoices); 	 
				DataConverter.getVoiceAssignment(voiceLabelsCurrentChord, Transcription.MAXIMUM_NUMBER_OF_VOICES); 	
			// The ground thruth voice assignment should be the first element in allPossibleVoiceAssignmentsCurrentChord. Thus,
			// remove it from the List (regardless of its position within) and then re-add it as the first element  
			allPossibleVoiceAssignmentsCurrentChord.remove(groundTruthVoiceAssignmentCurrentChord);
			allPossibleVoiceAssignmentsCurrentChord.add(0, groundTruthVoiceAssignmentCurrentChord);	
			// Add allPossibleVoiceAssignmentsCurrentChord to orderedVoiceAssignmentPossibilitiesAllChords 
			orderedVoiceAssignments.add(allPossibleVoiceAssignmentsCurrentChord);
			// Increment lowestNoteIndex
			lowestNoteIndex += chordSize;
		}
		return orderedVoiceAssignments;
	}


	/**
	 * Given a  maximum number of voices, enumerates all the musically possible voice 
	 * assignments for the chord at lowestNoteIndex. Returns a List of Lists, each element
	 * of which has length maxVoiceNumber and represents a musically possible voice 
	 * assignment. The position in each List represents the voice (where 0 is the highest 
	 * voice); the value at that position the note index (as in the NoteSequence) assigned to
	 * that voice (where 0 is the first note of the chord).<br><br> 
	 * 
	 * Only the musically possible voice assignments are returned, where all the notes must be represented in the
	 * voice assignment, and where
	 * <ul>
	 * <li> in the tablature case </li>
	 *      <ul>
	 *      <li> a note can be assigned to two voices at most (in which case it is a SNU)</li>
	 *      </ul>
	 *      <li> in the non-tablature case </li>
	 *      <ul>
	 *      <li> a note can be assigned to one voice at most </li>
	 *      <li> a note cannot be assigned to a voice that is already assigned to a 
	 *           sustained previous note </li>
	 *      </ul>
	 * </ul>          
	 * Additionally, voice assignments containing more than two voice crossing pairs (including, if applicable,
	 * sustained notes), are not included in the List that is returned.<br><br>
	 * 
	 * Example:<br>
	 * [2, 0, 0, 1, -1] is the voice assignment representing a chord consisting of three notes assigned to four
	 * voices (out of a maximum of five voices): note 2 is assigned to voice 0; note 0 to voices 1 and 2 (which
	 * means that it is a CoD); and note 1 to voice 3. No note is assigned to voice 4; this is indicated with -1.
	 * Assuming the pitches 10, 20, and 30 for notes 0, 1, and 2, respectively, this voice assignment contains 
	 * two voice crossing pairs: [1, 3]  and [2, 3]
	 * 
	 * @param btp
	 * @param bnp
	 * @param allVoiceLabels
	 * @param lowestNoteIndex 
	 * @param maxVoiceNumber
	 * @return 
	 */
	// TESTED (for both tablature- and non-tablature case)
	public static List<List<Integer>> enumerateVoiceAssignmentPossibilitiesForChord(Integer[][] btp, Integer[][] bnp,
		List<List<Double>> allVoiceLabels, int lowestNoteIndex, int maxVoiceNumber) { 

		Transcription.verifyCase(btp, bnp);
		boolean modelDuration = false; // Jukedeck
		List<List<Integer>> voiceAssignments = new ArrayList<List<Integer>>();

		// 0. Determine the chordSize and set the number of voices a note is allowed to be assigned (two in the tablature
		// case (for a CoD) and one for the non-tablature case) and the maximum number of voice crossing pairs allowed
		int chordSize = 0;
		int numVoicesAllowedToBeAssigned = 0;
		int maxNumberOfVoiceCrossingPairsAllowed = 2; // TODO put as argument?
		// a. In the tablature case
		if (btp != null) {
			chordSize = btp[lowestNoteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
			numVoicesAllowedToBeAssigned = 2;
		}
		// b. In the non-tablature case
		if (bnp != null) {
			chordSize = bnp[lowestNoteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
			numVoicesAllowedToBeAssigned = 1;
		}

		// 1. List all possible notes. Add -1 (no note assigned) as the first element to the List if the number of
		// notes in the chord is smaller than maxVoiceNumber
		List<Integer> allPossibilitiesPerVoice = new ArrayList<Integer>();
		if (chordSize < maxVoiceNumber) {
			allPossibilitiesPerVoice.add(-1);
		}
		for (int i = 0; i < chordSize; i++) {
			allPossibilitiesPerVoice.add(i);
		}

		// 2. List all the mathematical possibilities layer by layer, starting with the empty voice assignment. A voice
		// can either have no note (indicated with -1), meaning that it is not active, or a note (where there are 
		// n possibilities and where n = chordSize). In total, for n notes with v voices (i.e., maxVoiceNumber = v),
		// there are thus (n + 1)^v possibilities. 
		// previousLayer and nextLayer are List<List<Integer>>, i.e., Lists of voice assignments. To make each nextLayer,
		// replace, for each voice assignment in previousLayer, the element at indexToSet by all elements in 
		// allPossibilitiesPerVoice, each time adding the newly created voiceAssignment to nextLayer. The layers thus 
		// grow exponentially.
		// Example for chordSize = 2 and maxVoiceNumber = 3:
		// layer 0 is the starting point, the "empty" voice assignment; layer 1 contains 3 voice assignments; layer 2
		// contains 9 voice assignments; and layer 3 contains 27 voice assignments:
		// layer 0 = [0 0 0] 
		//   elementToSet = 0; replace and make layer 1:
		// layer 1 = [[-1 0 0], [0 0 0], [1 0 0]]
		//   elementToSet = 1; replace and make layer 2:
		// layer 2 = [-1 -1 0], [-1 0 0], [-1 1 0], [0 -1 0], [0 0 0 ], [0 1 0], [1 -1 0], [1 0 0], [1 1 0]]
		// elementToSet = 2; replace and make layer 2:
		// layer 3 = [[-1 -1 -1], [-1 -1 0], ... , [1 1 0], [1 1 1]]
		// Create the 'empty' voice assignment (all zeroes) to start with
		List<Integer> emptyVoiceAssignment = new ArrayList<Integer>();
		for (int i = 0; i < maxVoiceNumber; i++) {
			emptyVoiceAssignment.add(0);
		}
		// Create the first version of previousLayer, which contains only emptyVoiceAssignment
		List<List<Integer>> previousLayer = new ArrayList<List<Integer>>();
		previousLayer.add(emptyVoiceAssignment);
		int elementToSet = 0;
		// Create all versions of nextLayers; then set allPossibleVoiceAssignments to the last version
		while (elementToSet < maxVoiceNumber) {
			List<List<Integer>> nextLayer = new ArrayList<List<Integer>>();
			for (int i = 0; i < previousLayer.size(); i++) {
				List<Integer> currentVoiceAssignmentInPreviousLayer = previousLayer.get(i);
				for (int j = 0; j < allPossibilitiesPerVoice.size(); j++) {
					int currentPossibleNote = allPossibilitiesPerVoice.get(j);
					List<Integer> currentVoiceAssignmentForNextLayer = new ArrayList<Integer>(currentVoiceAssignmentInPreviousLayer);
					currentVoiceAssignmentForNextLayer.set(elementToSet, currentPossibleNote);
					nextLayer.add(currentVoiceAssignmentForNextLayer);
				}
			}
			previousLayer = nextLayer;
			elementToSet++;
		}
		List<List<Integer>> allMathematicalPossibilities = new ArrayList<List<Integer>>(previousLayer);
//		System.out.println("1. allMathematicalPossibilities");
//		for (List<Integer> l : allMathematicalPossibilities) {
//			System.out.println(l);
//		}

		// Assign voiceAssignments, which is the List<List>> that is returned by the method
		voiceAssignments = allMathematicalPossibilities;

		// 3. Detect and remove the musically impossible voice assignments 	
		List<Integer> allPossibleNotes; 
		if (allPossibilitiesPerVoice.get(0) == -1) {
			allPossibleNotes = new ArrayList<Integer>(allPossibilitiesPerVoice.subList(1, allPossibilitiesPerVoice.size()));
		}
		else {
			allPossibleNotes =	new ArrayList<Integer>(allPossibilitiesPerVoice);
		}
		// 1. List the indices of the musically impossible voice assignments, i.e., those to which a, b, or c below apply 
		List<Integer> indicesToRemove = new ArrayList<Integer>();
		for (int i = 0; i < voiceAssignments.size(); i++) {
			List<Integer> currentVoiceAssignment = voiceAssignments.get(i);
			// a. Not all the notes in the chord are represented
			if (!currentVoiceAssignment.containsAll(allPossibleNotes)) {
				indicesToRemove.add(i);
			}
			// b. A note is assigned to more voices than allowed, i.e., it appears more than twice (tablature) or once
			// (non-tablature). This does not apply to not assigned (-1)
			else {
				for (int j = 0; j < currentVoiceAssignment.size(); j++) {
					int currentNote = currentVoiceAssignment.get(j);
					if (currentNote != -1 && Collections.frequency(currentVoiceAssignment, currentNote) > numVoicesAllowedToBeAssigned) {
						indicesToRemove.add(i);
						break;
					}
				}
			}
			// c. If the voice assignment contains any voices that go with sustained notes 
			if (btp != null && modelDuration || bnp != null) {
				List<Integer> sustainedVoices = 
//					getVoicesOfSustainedPreviousNotesInChordMUSCI(basicNoteProperties, allVoiceLabels, lowestNoteIndex);
					Transcription.getVoicesOfSustainedPreviousNotesInChord(btp, null, null, bnp, allVoiceLabels, lowestNoteIndex); // TODO all the nulls work because the method is only called in the non-tablature case
				for (int j = 0; j < currentVoiceAssignment.size(); j++) {
					int currentVoice = j;
					// If the currentVoice is a sustained voice but has an onset
					if (sustainedVoices.contains(currentVoice) && currentVoiceAssignment.get(currentVoice) != -1) {
						// Only if indicesToRemove does not yet contain i
						if (!indicesToRemove.contains(i)) {
							indicesToRemove.add(i);
						}
						break;
					}
				}
			}			
		}

//		System.out.println(indicesToRemove);
//		System.out.println("size before removal of impossible = " + voiceAssignments.size());
		// 2. Remove the musically impossible voice assignments; start at the back of voiceAssignments in 
		// order to prevent shifting indices problems
		Collections.reverse(indicesToRemove);
		for (int i = 0; i < indicesToRemove.size(); i++) {
			int indexToRemove = indicesToRemove.get(i);
			voiceAssignments.remove(indexToRemove);
		}

//		System.out.println("size after removal = " + voiceAssignments.size());

//		System.out.println("2. after removal of musically impossible");
//		for (List<Integer> l : voiceAssignments) {
//			System.out.println(l);
//		}

//		System.out.println("  " + indicesToRemove.size() + " musically impossible voice assignment(s) filtered out: \r\n" + 
//		  "    " + notAllNotesRepresented + " in which not all notes are represented \r\n" + "    " + 
//			noteAssignedToMoreVoices + " in which one or more notes are assigned to more than two voices");

		// 4. Detect and remove voice assignments containing more voice crossing pairs than allowed (as specified in 
		// maxNumberOfVoiceCrossingPairsAllowed)
		// 1. List the indices of all voice assignments that contain more voice crossing pairs than allowed 
		// Clear indicesToRemove first 
		indicesToRemove.clear();
		for (int i = 0; i < voiceAssignments.size(); i++) {
			// Get the pitches in the chord
			List<Integer> pitchesInChord; // = FeatureGenerator.getPitchesInChord(btp, bnp, lowestNoteIndex);
			if (btp != null) {
				pitchesInChord = Tablature.getPitchesInChord(btp, lowestNoteIndex);
			}
			else {
				pitchesInChord = Transcription.getPitchesInChord(bnp, lowestNoteIndex);
			}
			
			List<Integer> currentVoiceAssignment = voiceAssignments.get(i);
			// Get the voices in the chord under the current voice assignment
			List<List<Double>> currentVoiceLabels = 
				DataConverter.getChordVoiceLabels(currentVoiceAssignment);
			List<List<Integer>> currentVoicesInChord = 
				DataConverter.getVoicesInChord(currentVoiceLabels);

			// Insert pitches and voices of sustained previous notes
			if (btp != null && modelDuration || bnp != null) {
				List<List<Integer>> allPitchesAndVoices = 
					Transcription.getAllPitchesAndVoicesInChord(bnp, pitchesInChord,
					currentVoicesInChord, allVoiceLabels, lowestNoteIndex);
				pitchesInChord = allPitchesAndVoices.get(0);
				// voicesInChord must be a List<List>>
				currentVoicesInChord = new ArrayList<List<Integer>>();
				for (int j : allPitchesAndVoices.get(1)) {
					int currentVoice = j;
					List<Integer> voiceWrapped = Arrays.asList(new Integer[]{currentVoice});
					currentVoicesInChord.add(voiceWrapped);
				}
			}

			List<List<Integer>> currentVoiceCrossingInformation = 
				Transcription.getVoiceCrossingInformationInChord(pitchesInChord,
				currentVoicesInChord);
			List<Integer> voiceCrossingPairs = currentVoiceCrossingInformation.get(1);
			int numberOfVoiceCrossingPairs = voiceCrossingPairs.size() / 2;
			// Comment out to include also voice assignments with more than two voice 
			// crossing pairs
			if (numberOfVoiceCrossingPairs > maxNumberOfVoiceCrossingPairsAllowed) {
				indicesToRemove.add(i);
			}
		}

//		System.out.println("vc to remove: " + indicesToRemove.size());

		// 2. Remove the voice assignments that contain more voice crossing pairs than allowed; start at the back of 
		// voiceAssignments in order to prevent shifting indices problems. Do this only if there are fewer indices 
		// to remove than there are voice assignment possibilities left: if voiceAssignments at this point only 
		// contains voice assignments with more voice crossing pairs than allowed (i.e., if indicesToRemove and 
		// voiceAssignments have the same size) this step must be skipped as it will result in no voice assignment 
		// possibilities at all (this can happen only in application mode)
		if (indicesToRemove.size() < voiceAssignments.size()) {                               
			Collections.reverse(indicesToRemove);
			for (int i = 0; i < indicesToRemove.size(); i++) {
				int indexToRemove = indicesToRemove.get(i);
				voiceAssignments.remove(indexToRemove);
			}
		}
		else { 
			System.out.println("Only va poss with more vc allowed at " + ToolBox.getTimeStamp());
		}

//		System.out.println("  " + indicesToRemove.size() + " voice assignment(s) containing more voice crossing pairs " +
//		  "than allowed (" + maxNumberOfVoiceCrossingPairsAllowed + ") filtered out");
//		System.out.println("--> " + voiceAssignments.size() + " voice assignments remain after filtering");

		// 5. If necessary: make each voice assignment size Transcription.MAXIMUM_NUMBER_OF_VOICES
		List<List<Integer>> voiceAssignmentsFinal = new ArrayList<List<Integer>>();
		if (maxVoiceNumber < Transcription.MAXIMUM_NUMBER_OF_VOICES) {
			for (List<Integer> l : voiceAssignments) {
				List<Integer> finalVoiceAssignment = new ArrayList<Integer>(l); 
				for (int i = maxVoiceNumber; i < Transcription.MAXIMUM_NUMBER_OF_VOICES; i++) {
					finalVoiceAssignment.add(-1);
				}
				voiceAssignmentsFinal.add(finalVoiceAssignment);
			}
		}
		else { 
			voiceAssignmentsFinal = voiceAssignments;
		}

		// 6. Return voiceAssignments
		return voiceAssignmentsFinal;
	}
	
	
	/**
	 * Checks the following x chords (where x is determined by the argument numNextChords) and returns a double[]
	 * containing   
	 *   the inter-onset time proximity between the chord at lowestNoteIndex and the following chord;
	 *   the pitch (and, in the tablature case, the course) for all notes in the chord; 
	 *     
	 * For each note the chord is short of the maximum number of notes, n/a values of -1.0 are returned. If there 
	 * are fewer than numNextChords remaining (the final chord will be numNextChords short of numNextChords, the
	 * penultimate chord numNextChords - 1, etc.), n/a values of -1.0 are returned for each missing chord.
	 *       
	 * @param btp
	 * @param bnp
	 * @param lowestNoteIndex
	 * @param numNextChords
	 * @return
	 */
	// TESTED for both tablature- and non-tablature case
	double[] getProximitiesAndCourseInfoAheadChord(Integer[][]btp, Integer[][] bnp,	int lowestNoteIndex, 
		int numNextChords) {

		Transcription.verifyCase(btp, bnp);

		// 0. Initialise pitchProxAndCourse and variables
		double[] pitchProxAndCourse = null;
		int chordSize = -1;
		int numChords = -1;
		int chordSeqNum = -1;
		Rational onsetTime = null;
		// a. In the tablature case
		if (btp != null) {
			pitchProxAndCourse = new double[11 * numNextChords];
			chordSize = btp[lowestNoteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
			onsetTime = new Rational(btp[lowestNoteIndex][Tablature.ONSET_TIME], 
				Tablature.SRV_DEN);
			chordSeqNum = btp[lowestNoteIndex][Tablature.CHORD_SEQ_NUM];
			numChords = btp[btp.length - 1][Tablature.CHORD_SEQ_NUM] + 1;
		}
		// b. In the non-tablature case
		else {
			pitchProxAndCourse = new double[6 * numNextChords];
			chordSize = bnp[lowestNoteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
			onsetTime = new Rational(bnp[lowestNoteIndex][Transcription.ONSET_TIME_NUMER],
				bnp[lowestNoteIndex][Transcription.ONSET_TIME_DENOM]);
			chordSeqNum = bnp[lowestNoteIndex][Transcription.CHORD_SEQ_NUM];
			numChords = bnp[bnp.length - 1][Transcription.CHORD_SEQ_NUM] + 1;
		}
		Arrays.fill(pitchProxAndCourse, -1.0);

		// 1. Fill pitchProxAndCourse		
		int lowestNoteIndexNext = lowestNoteIndex + chordSize; 
		for (int i = 1; i <= numNextChords; i++) {
			int currLowestNoteIndex = lowestNoteIndexNext;
			int chordSeqNumNext = chordSeqNum + i;
			// Only if there is a next chord
			if (chordSeqNumNext < numChords) {	
				int currLowestNoteIndexNext = -1;
				// a. In the tablature case		
				if (btp != null) {
					int arrayIndex = (i-1) * 11;  		  	
					// Set inter-onset time proximity and increment arrayIndex
					Rational currOnsetTime = new Rational(btp[currLowestNoteIndex][Tablature.ONSET_TIME], 
						Tablature.SRV_DEN);
					pitchProxAndCourse[arrayIndex] = 1.0 / ((currOnsetTime.toDouble() - onsetTime.toDouble()) + 1);
					arrayIndex++;
					// Set pitches and courses and increment arrayIndex
					int currChordSize = btp[currLowestNoteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS]; 
					currLowestNoteIndexNext = currLowestNoteIndex + currChordSize;
					for (int j = currLowestNoteIndex; j < currLowestNoteIndexNext; j++) {
						pitchProxAndCourse[arrayIndex] = (double) btp[j][Tablature.PITCH];
						arrayIndex ++;
						pitchProxAndCourse[arrayIndex] = (double) btp[j][Tablature.COURSE];
						arrayIndex ++;
					}
				}
				// b. In the non-tablature case
				else {
					int arrayIndex = (i-1) * 6;
					// Set inter-onset time proximity and increment arrayIndex 
					Rational currOnsetTime = new Rational(bnp[currLowestNoteIndex][Transcription.ONSET_TIME_NUMER], 
						bnp[currLowestNoteIndex][Transcription.ONSET_TIME_DENOM]);
					pitchProxAndCourse[arrayIndex] = 1.0 / ((currOnsetTime.toDouble() - onsetTime.toDouble()) + 1);
					arrayIndex++;
					// Set pitches
					int currChordSize = bnp[currLowestNoteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS]; 
					currLowestNoteIndexNext = currLowestNoteIndex + currChordSize;
					for (int j = currLowestNoteIndex; j < currLowestNoteIndexNext; j++) {
						pitchProxAndCourse[arrayIndex] = (double) bnp[j][Transcription.PITCH];
						arrayIndex ++;
					}
				}
				lowestNoteIndexNext = currLowestNoteIndexNext;
			}
			// If there is no next chord
			else {
				break;
			}
		}
		return pitchProxAndCourse;	
	}


	// =================================== FEATURE VECTOR GENERATION ===================================
	/**
	 * Given a voice assignment, generates the chord feature vector for the chord at lowestNoteIndex.
	 * 
	 * @param btp
	 * @param bnp
	 * @param transcription
	 * @param meterInfo
	 * @param lowestNoteIndex
	 * @param voiceAssignment
	 * @return
	 */
	// TESTED for both tablature- and non-tablature case
	public List<Double> generateChordFeatureVector(Integer[][] btp, Integer[][] bnp, Transcription 
		transcription, List<Integer[]> meterInfo, int lowestNoteIndex, List<Integer> voiceAssignment) { 

		Transcription.verifyCase(btp, bnp);
		List<Double> chordFeatureVector = new ArrayList<Double>();

		// 1. Note-specific features
		List<Double> noteSpec = getNoteSpecificFeaturesChord(btp, bnp, /*transcription,*/ meterInfo, lowestNoteIndex);
		chordFeatureVector.addAll(noteSpec);
		// 2. Chord-level features
		List<Double> chordLevel = 
			getChordLevelFeaturesChord(btp, bnp, /*transcription,*/ meterInfo, lowestNoteIndex);
		chordFeatureVector.addAll(chordLevel);

		// 3. Polyphonic embedding features
		// 1. Voices with adjacent note on same course
		if (btp != null) {
			double[] voicesWithAdjNoteOnSameCourse = getVoicesWithAdjacentNoteOnSameCourse(btp, transcription, lowestNoteIndex);
			for (Double d : voicesWithAdjNoteOnSameCourse) {
				chordFeatureVector.add(d);
			}
		}
		// 2. Proximities and movements TODO Enable sustained notes for tablature case
		List<Double> proximitiesAndMovements = new ArrayList<Double>();

//		double[] p = getAverageProximitiesAndMovementsOfChord(btp, bnp, transcription, lowestNoteIndex, voiceAssignment);
		double[] p = getProximitiesAndMovementsOfChord(btp, bnp, transcription, lowestNoteIndex, voiceAssignment);
		for (double d : p) {
			proximitiesAndMovements.add(d);
		}
		chordFeatureVector.addAll(proximitiesAndMovements);
		// 3. Voices already occupied TODO Enable sustained notes for tablature case
		if (btp != null) {
			for (int i = 0; i < Transcription.MAXIMUM_NUMBER_OF_VOICES; i++) {
				chordFeatureVector.add(0.0);
			}
		}
		else {
			chordFeatureVector.addAll(getVoicesAlreadyOccupied(btp, null, null, bnp, transcription, lowestNoteIndex)); 
		}
		// 4. Pitch-voice relation TODO Enable sustained notes for tablature case
		List<List<Double>> allVoiceLabels = transcription.getVoiceLabels();
		List<Double> pitchVoiceRelation = Arrays.asList(new Double[]{getPitchVoiceRelationInChord(btp, bnp, 
			allVoiceLabels, lowestNoteIndex, voiceAssignment)});
		chordFeatureVector.addAll(pitchVoiceRelation);
		// 5-7. Voice crossing information TODO Enable sustained notes for tablature case
		List<Double> voiceCrossingInfo = new ArrayList<Double>();
		List<Integer> pitchesInChord; // = FeatureGenerator.getPitchesInChord(btp, bnp, lowestNoteIndex);
		if (btp != null) {
			pitchesInChord = Tablature.getPitchesInChord(btp, lowestNoteIndex);
		}
		else {
			pitchesInChord = Transcription.getPitchesInChord(bnp, lowestNoteIndex);
		}
		List<List<Double>> voiceLabels = 
			DataConverter.getChordVoiceLabels(voiceAssignment);
		List<List<Integer>> voicesInChord = 
			DataConverter.getVoicesInChord(voiceLabels);
		// In the non-tablature case: include pitches and voices of sustained previous notes in Lists
		if (bnp != null) {			  
			List<List<Integer>> allPitchesAndVoices = 
				Transcription.getAllPitchesAndVoicesInChord(bnp, pitchesInChord, 
				voicesInChord, allVoiceLabels, lowestNoteIndex);
			// voicesInChord must be a List<List>>
			pitchesInChord = allPitchesAndVoices.get(0);
			voicesInChord = new ArrayList<List<Integer>>();
			for (int i : allPitchesAndVoices.get(1)) {
				int currentVoice = i;
				List<Integer> voiceWrapped = Arrays.asList(new Integer[]{currentVoice});
				voicesInChord.add(voiceWrapped);
			}
		}
		List<List<Integer>> voiceCrossingInformation = 
			Transcription.getVoiceCrossingInformationInChord(pitchesInChord, voicesInChord);
		// Number of voice crossing pairs
		double numberOfVCPairs = voiceCrossingInformation.get(1).size() / 2.0;
		voiceCrossingInfo.add(numberOfVCPairs);
		// Summed pitch distance between the voice crossing pairs
		List<Integer> pitchDistances = voiceCrossingInformation.get(2);
		double pitchDistSummed = ToolBox.sumListInteger(pitchDistances);
		voiceCrossingInfo.add(pitchDistSummed);
		// Average pitch distance between the voice crossing pairs
		if (pitchDistances.size() == 0) {
			voiceCrossingInfo.add(0.0);
		}
		else {
			voiceCrossingInfo.add(pitchDistSummed / pitchDistances.size());
		}
		chordFeatureVector.addAll(voiceCrossingInfo);

		// 8. Voice assignment vector
		List<Double> voiceAssignmentVector = new ArrayList<Double>();
		for (int i : voiceAssignment) {
			voiceAssignmentVector.add((double) i);
		}
		chordFeatureVector.addAll(voiceAssignmentVector);

		return chordFeatureVector;
	}


	/**
	 * Given a voice assignment, generates the chord feature vector for the chord at lowestNoteIndex.
	 * 
	 * @param btp
	 * @param bnp
	 * @param transcription
	 * @param meterInfo
	 * @param lowestNoteIndex
	 * @param voiceAssignment
	 * @return
	 */
	// TESTED for both tablature- and non-tablature case
	public static List<Double> generateChordFeatureVectorDISS(Integer[][] btp, Integer[][] bnp, Transcription 
		transcription, List<Integer[]> meterInfo, int lowestNoteIndex, List<Integer> voiceAssignment) { 

		Transcription.verifyCase(btp, bnp);
		List<Double> chordFeatureVector = new ArrayList<Double>();
		
		// a. In the tablature case
		if (btp != null) {
			// 1. Note-specific features
			List<Double> noteSpec = getNoteSpecificFeaturesChord(btp, bnp, /*transcription,*/ meterInfo, lowestNoteIndex);
			chordFeatureVector.addAll(noteSpec.subList(1, 6));
			chordFeatureVector.addAll(noteSpec.subList(7, 12));
			chordFeatureVector.addAll(noteSpec.subList(13, 18));
			chordFeatureVector.addAll(noteSpec.subList(19, 24));
			if (Transcription.MAXIMUM_NUMBER_OF_VOICES == 5) {
				chordFeatureVector.addAll(noteSpec.subList(25, 30)); // HIERRR
			}

			// 2. Chord-level features
			List<Double> chordLevel = 
				getChordLevelFeaturesChord(btp, bnp, /*transcription,*/ meterInfo, lowestNoteIndex);
			chordFeatureVector.addAll(chordLevel.subList(0, 2));
			chordFeatureVector.addAll(chordLevel.subList(3, chordLevel.size()));

			// 3. Polyphonic embedding features
//			// 1. Voices with adjacent note on same course
//			if (btp != null) {
//				double[] voicesWithAdjNoteOnSameCourse = getVoicesWithAdjacentNoteOnSameCourse(btp, transcription, lowestNoteIndex);
//				for (Double d : voicesWithAdjNoteOnSameCourse) {
//					chordFeatureVector.add(d);
//				}
//			}
			// 1. Proximities and movements TODO Enable sustained notes for tablature case
			List<Double> proximitiesAndMovements = new ArrayList<Double>();
//			double[] p = getAverageProximitiesAndMovementsOfChord(btp, bnp, transcription, lowestNoteIndex, voiceAssignment);
			double[] p = getProximitiesAndMovementsOfChord(btp, bnp, transcription, lowestNoteIndex, voiceAssignment);
			for (double d : p) {
				proximitiesAndMovements.add(d);
			}
			chordFeatureVector.addAll(proximitiesAndMovements);
			// 2. Voices already occupied TODO Enable sustained notes for tablature case
//			if (btp != null) {
			for (int i = 0; i < Transcription.MAXIMUM_NUMBER_OF_VOICES; i++) {
				chordFeatureVector.add(0.0);
			}
//			}
//			else {
//				chordFeatureVector.addAll(getVoicesAlreadyOccupied(btp, null, null, bnp, transcription, lowestNoteIndex)); 
//			}
			// 4. Pitch-voice relation TODO Enable sustained notes for tablature case
			List<List<Double>> allVoiceLabels = transcription.getVoiceLabels();
			List<Double> pitchVoiceRelation = Arrays.asList(new Double[]{getPitchVoiceRelationInChord(btp, bnp, 
				allVoiceLabels, lowestNoteIndex, voiceAssignment)});
			chordFeatureVector.addAll(pitchVoiceRelation);
			// 5-7. Voice crossing information TODO Enable sustained notes for tablature case
			List<Double> voiceCrossingInfo = new ArrayList<Double>();
			List<Integer> pitchesInChord = Tablature.getPitchesInChord(btp, lowestNoteIndex);
//			List<Integer> pitchesInChord = FeatureGenerator.getPitchesInChord(btp, bnp, lowestNoteIndex);
			List<List<Double>> voiceLabels = 
				DataConverter.getChordVoiceLabels(voiceAssignment);
			List<List<Integer>> voicesInChord = 
				DataConverter.getVoicesInChord(voiceLabels);
			// In the non-tablature case: include pitches and voices of sustained previous notes in Lists
			if (bnp != null) {			  
				List<List<Integer>> allPitchesAndVoices = 
					Transcription.getAllPitchesAndVoicesInChord(bnp, pitchesInChord, 
					voicesInChord, allVoiceLabels, lowestNoteIndex);
				// voicesInChord must be a List<List>>
				pitchesInChord = allPitchesAndVoices.get(0);
				voicesInChord = new ArrayList<List<Integer>>();
				for (int i : allPitchesAndVoices.get(1)) {
					int currentVoice = i;
					List<Integer> voiceWrapped = Arrays.asList(new Integer[]{currentVoice});
					voicesInChord.add(voiceWrapped);
				}
			}
			List<List<Integer>> voiceCrossingInformation = 
				Transcription.getVoiceCrossingInformationInChord(pitchesInChord, voicesInChord);
			// Number of voice crossing pairs
			double numberOfVCPairs = voiceCrossingInformation.get(1).size() / 2.0;
			voiceCrossingInfo.add(numberOfVCPairs);
			// Summed pitch distance between the voice crossing pairs
			List<Integer> pitchDistances = voiceCrossingInformation.get(2);
			double pitchDistSummed = ToolBox.sumListInteger(pitchDistances);
			voiceCrossingInfo.add(pitchDistSummed);
			// Average pitch distance between the voice crossing pairs
			if (pitchDistances.size() == 0) {
				voiceCrossingInfo.add(0.0);
			}
			else {
				voiceCrossingInfo.add(pitchDistSummed / pitchDistances.size());
			}
			chordFeatureVector.addAll(voiceCrossingInfo);

			// 8. Voice assignment vector
			List<Double> voiceAssignmentVector = new ArrayList<Double>();
			for (int i : voiceAssignment) {
				voiceAssignmentVector.add((double) i);
			}
			chordFeatureVector.addAll(voiceAssignmentVector);
		}
		// b. In the non-tablature case
		else {
			// 1. Note-specific features
			List<Double> noteSpec = getNoteSpecificFeaturesChord(btp, bnp, /*transcription,*/ meterInfo, lowestNoteIndex);
			chordFeatureVector.addAll(noteSpec.subList(1, 4));
			chordFeatureVector.addAll(noteSpec.subList(5, 8));
			chordFeatureVector.addAll(noteSpec.subList(9, 12));
			chordFeatureVector.addAll(noteSpec.subList(13, 16));
			if (Transcription.MAXIMUM_NUMBER_OF_VOICES == 5) {
				chordFeatureVector.addAll(noteSpec.subList(17, 20));
			}

			// 2. Chord-level features
			List<Double> chordLevel = 
				getChordLevelFeaturesChord(btp, bnp, /*transcription,*/ meterInfo, lowestNoteIndex);
			chordFeatureVector.add(chordLevel.get(0));
			chordFeatureVector.addAll(chordLevel.subList(2, chordLevel.size()));

			// 3. Polyphonic embedding features
			// 1. Proximities and movements
			List<Double> proximitiesAndMovements = new ArrayList<Double>();
//				double[] p = getAverageProximitiesAndMovementsOfChord(btp, bnp, transcription, lowestNoteIndex, voiceAssignment);
			double[] p = getProximitiesAndMovementsOfChord(btp, bnp, transcription, lowestNoteIndex, voiceAssignment);
			for (double d : p) {
				proximitiesAndMovements.add(d);
			}
			chordFeatureVector.addAll(proximitiesAndMovements);
			// 2. Voices already occupied
//			if (btp != null) {
//				for (int i = 0; i < Transcription.MAXIMUM_NUMBER_OF_VOICES; i++) {
//					chordFeatureVector.add(0.0);
//				}
//			}
//			else {
			chordFeatureVector.addAll(getVoicesAlreadyOccupied(btp, null, null, bnp, transcription, lowestNoteIndex)); 
//			}
			// 3. Pitch-voice relation
			List<List<Double>> allVoiceLabels = transcription.getVoiceLabels();
			List<Double> pitchVoiceRelation = Arrays.asList(new Double[]{getPitchVoiceRelationInChord(btp, bnp, 
				allVoiceLabels, lowestNoteIndex, voiceAssignment)});
			chordFeatureVector.addAll(pitchVoiceRelation);
			// 4-6. Voice crossing information
			List<Double> voiceCrossingInfo = new ArrayList<Double>();
			List<Integer> pitchesInChord = Transcription.getPitchesInChord(bnp, lowestNoteIndex);
//			List<Integer> pitchesInChord = FeatureGenerator.getPitchesInChord(btp, bnp, lowestNoteIndex);
			List<List<Double>> voiceLabels = 
				DataConverter.getChordVoiceLabels(voiceAssignment);
			List<List<Integer>> voicesInChord = 
				DataConverter.getVoicesInChord(voiceLabels);
			// In the non-tablature case: include pitches and voices of sustained previous notes in Lists
			if (bnp != null) {			  
				List<List<Integer>> allPitchesAndVoices = 
					Transcription.getAllPitchesAndVoicesInChord(bnp, pitchesInChord, 
					voicesInChord, allVoiceLabels, lowestNoteIndex);
				// voicesInChord must be a List<List>>
				pitchesInChord = allPitchesAndVoices.get(0);
				voicesInChord = new ArrayList<List<Integer>>();
				for (int i : allPitchesAndVoices.get(1)) {
					int currentVoice = i;
					List<Integer> voiceWrapped = Arrays.asList(new Integer[]{currentVoice});
					voicesInChord.add(voiceWrapped);
				}
			}
			List<List<Integer>> voiceCrossingInformation = 
				Transcription.getVoiceCrossingInformationInChord(pitchesInChord, voicesInChord);
			// Number of voice crossing pairs
			double numberOfVCPairs = voiceCrossingInformation.get(1).size() / 2.0;
			voiceCrossingInfo.add(numberOfVCPairs);
			// Summed pitch distance between the voice crossing pairs
			List<Integer> pitchDistances = voiceCrossingInformation.get(2);
			double pitchDistSummed = ToolBox.sumListInteger(pitchDistances);
			voiceCrossingInfo.add(pitchDistSummed);
			// Average pitch distance between the voice crossing pairs
			if (pitchDistances.size() == 0) {
				voiceCrossingInfo.add(0.0);
			}
			else {
				voiceCrossingInfo.add(pitchDistSummed / pitchDistances.size());
			}
			chordFeatureVector.addAll(voiceCrossingInfo);

			// 7. Voice assignment vector
			List<Double> voiceAssignmentVector = new ArrayList<Double>();
			for (int i : voiceAssignment) {
				voiceAssignmentVector.add((double) i);
			}
			chordFeatureVector.addAll(voiceAssignmentVector);
		}
		return chordFeatureVector;
	}


	/**
	 * Given a List of ordered voice assignments, generates the set of chord feature vectors for the chord at 
	 * lowestNoteIndex. The ground truth feature vector (that one that goes with the ground truth voice assignment)
	 * is placed at the top of the List returned. 
	 *  
	 * @param btp
	 * @param bnp
	 * @param transcription
	 * @param meterInfo
	 * @param lowestNoteIndex
	 * @param orderedVoiceAssignments
	 * @return
	 */	
	private static List<List<Double>> generateChordFeatureVectors(Integer[][] btp, Integer[][] bnp, Transcription
		transcription, List<Integer[]> meterInfo, int lowestNoteIndex, List<List<Integer>> orderedVoiceAssignments) {
				
		Transcription.verifyCase(btp, bnp);	
		List<List<Double>> chordFeatureVectors = new ArrayList<List<Double>>();
			 
		// Generate for each voice assignment the set of chord feature vectors. Since the first element of 
		// orderedVoiceAssignments is the ground truth voice assignment, the first element of each element of 
		// chordFeatureVectors will be the ground truth feature vector
	 	for (List<Integer> va : orderedVoiceAssignments) {
//	 		List<Integer> currentVoiceAssignment = orderedVoiceAssignments.get(i);
//	 		List<Double> currentCompleteChordFeatureVector = generateCompleteChordFeatureVector(btp, 
//	 			bnp, transcription, lowestNoteIndex, largestChordSizeTraining, highestNumberOfVoicesTraining, 
//	 			currentVoiceAssignment,	useTablatureInformation);

//	 		List<Double> currentChordFeatureVector = 
//	 			generateChordFeatureVector(btp, bnp, transcription,	meterInfo, lowestNoteIndex,	va);
	 		List<Double> currentChordFeatureVector = 
		 		generateChordFeatureVectorDISS(btp, bnp, transcription,	meterInfo, lowestNoteIndex,	va);
	 		chordFeatureVectors.add(currentChordFeatureVector);	
	 	}	
	 	
	 	return chordFeatureVectors;
	}
	
	
	/**
	 * Generates all sets of chord feature vectors for the given piece.
	 *  
	 * @param btp
	 * @param bnp
	 * @param transcription
	 * @param orderedVoiceAssignments
	 * @param meterInfo
	 * @return
	 */
	public static List<List<List<Double>>> generateAllChordFeatureVectors(Integer[][] btp,	Integer[][] bnp, 
		Transcription transcription, List<Integer[]> meterInfo, List<List<List<Integer>>> 
		orderedVoiceAssignments/*, int highestNumberOfVoices*/) {

		Transcription.verifyCase(btp, bnp);
		List<List<List<Double>>> allChordFeatureVectors = new ArrayList<List<List<Double>>>();

		int numberOfChords = 0;
		// a. In the tablature case
		if (btp != null) {
			numberOfChords = btp[btp.length - 1][Tablature.CHORD_SEQ_NUM] + 1;
		}
		// b. In the non-tablature case
		else {
			numberOfChords = bnp[bnp.length - 1][Transcription.CHORD_SEQ_NUM] + 1;
		}

		// NB: pre-set voiceLabels possible because this method is only used in training and test mode 
//		List<List<Double>> voiceLabels = transcription.getVoiceLabels();
//		List<List<List<Integer>>> orderedVoiceAssignments =	
//			getOrderedVoiceAssignments(btp, bnp, voiceLabels, highestNumberOfVoices);
		
		// For each chord: generate the set of feature vectors and add those to allChordFeatureVectors  
		int lowestNoteIndex = 0;
		for (int i = 0; i < numberOfChords; i++) {  
			List<List<Integer>> orderedVoiceAssignmentsCurrentChord = orderedVoiceAssignments.get(i);
//			List<List<Double>> currentCompleteChordFeatureVectors =	
//				generateCompleteChordFeatureVectorsOUD(btp, bnp, transcription, lowestNoteIndex, 
//				largestChordSizeTraining, highestNumberOfVoicesTraining, orderedVoiceAssignmentsCurrentChord, 
//				useTablatureInformation);
			List<List<Double>> currentChordFeatureVectors = generateChordFeatureVectors(btp, bnp, transcription, 
				meterInfo, lowestNoteIndex, orderedVoiceAssignmentsCurrentChord);
			allChordFeatureVectors.add(currentChordFeatureVectors);

//			lowestNoteIndex += chordSize;  		
//			int chordSize = 0;
			// Update lowestNoteIndex
			// a. In the tablature case
			if (btp != null) {
				int currentChordSize = btp[lowestNoteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
				lowestNoteIndex += currentChordSize;
//				// Find the size of the chord with sequence number i
//				for (int j = 0; j < btp.length; j++) {
//					if (btp[j][Tablature.CHORD_SEQ_NUM] == i) {
//						chordSize = btp[j][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
//						break;
//					}
//				}
			}
			if (bnp != null) {
				int currentChordSize = bnp[lowestNoteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
				lowestNoteIndex += currentChordSize;
//				// Find the size of the chord with sequence number i
//				for (int j = 0; j < bnp.length; j++) {
//					if (bnp[j][Transcription.CHORD_SEQ_NUM] == i) {
//						chordSize = bnp[j][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
//						break;
//					}
//				}
			}
		}
		return allChordFeatureVectors;
	}
	
	
	public List<List<List<Double>>> generateAllChordFeatureVectorsOLD(Integer[][] btp,	Integer[][] bnp, 
			Transcription transcription, List<Integer[]> meterInfo, /*List<List<List<Integer>>> 
			orderedVoiceAssignments,*/ int highestNumberOfVoices) {

			Transcription.verifyCase(btp, bnp);
			List<List<List<Double>>> allChordFeatureVectors = new ArrayList<List<List<Double>>>();

			int numberOfChords = 0;
			// a. In the tablature case
			if (btp != null) {
				numberOfChords = btp[btp.length - 1][Tablature.CHORD_SEQ_NUM] + 1;
			}
			// b. In the non-tablature case
			else {
				numberOfChords = bnp[bnp.length - 1][Transcription.CHORD_SEQ_NUM] + 1;
			}

			// NB: pre-set voiceLabels possible because this method is only used in training and test mode 
			List<List<Double>> voiceLabels = transcription.getVoiceLabels();
			List<List<List<Integer>>> orderedVoiceAssignments =	
				getOrderedVoiceAssignments(btp, bnp, voiceLabels, highestNumberOfVoices);
			
			// For each chord: generate the set of feature vectors and add those to allChordFeatureVectors  
			int lowestNoteIndex = 0;
			for (int i = 0; i < numberOfChords; i++) {  
				List<List<Integer>> orderedVoiceAssignmentsCurrentChord = orderedVoiceAssignments.get(i);
//				List<List<Double>> currentCompleteChordFeatureVectors =	
//					generateCompleteChordFeatureVectorsOUD(btp, bnp, transcription, lowestNoteIndex, 
//					largestChordSizeTraining, highestNumberOfVoicesTraining, orderedVoiceAssignmentsCurrentChord, 
//					useTablatureInformation);
				List<List<Double>> currentChordFeatureVectors = generateChordFeatureVectors(btp, bnp, transcription, 
					meterInfo, lowestNoteIndex, orderedVoiceAssignmentsCurrentChord);
				allChordFeatureVectors.add(currentChordFeatureVectors);

//				lowestNoteIndex += chordSize;  		
//				int chordSize = 0;
				// Update lowestNoteIndex
				// a. In the tablature case
				if (btp != null) {
					int currentChordSize = btp[lowestNoteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
					lowestNoteIndex += currentChordSize;
//					// Find the size of the chord with sequence number i
//					for (int j = 0; j < btp.length; j++) {
//						if (btp[j][Tablature.CHORD_SEQ_NUM] == i) {
//							chordSize = btp[j][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
//							break;
//						}
//					}
				}
				if (bnp != null) {
					int currentChordSize = bnp[lowestNoteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
					lowestNoteIndex += currentChordSize;
//					// Find the size of the chord with sequence number i
//					for (int j = 0; j < bnp.length; j++) {
//						if (bnp[j][Transcription.CHORD_SEQ_NUM] == i) {
//							chordSize = bnp[j][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
//							break;
//						}
//					}
				}
			}
			return allChordFeatureVectors;
		}


	// =================================== ISMIR AND MUSCI ===================================
	/**
	 * Gets the individual note features for each note in the chord, i.e., the features that are different for
	 * each note in the chord (and independent of the voice assignment). Returns a List<List<Double>>, whose
	 * first element represents the lowest note, containing 
	 *   in the tablature case:
	 *     as element 0: the index (sequence number within the chord) of the note
	 *     as element 1: the pitch (as a MIDI number) of the note
	 *     as element 2: the course the note is on
	 *     as element 3: the fret the note is on
	 *     as element 4: the maximum duration of the note
	 *   NB: if useTablatureInformation == false, features 2-4 (course, fret, and maximum duration) are not 
	 *       included
	 *     
	 *   in the non-tablature case:
	 *     as element 0: the index (sequence number within the chord) of the note, excluding any sustained previous 
	 *                   notes; 
	 *                   NB: In the case of pitch overlap within the chord (unison), the note that comes first in the
	 *                   NoteSequence is listed first.
	 *     as element 1: the index (sequence number within the chord) of the note, including any sustained previous
	 *     							 notes;
	 *                   NB: In the case of pitch overlap within the chord (unison, sustained previous note, or any
	 *                   combination), any sustained previous notes are listed first (in the order they appear
	 *                   in the NoteSequence), and any notes in the current chord second (again, in the order 
	 *                   they appear in the noteSequence). 
	 *     as element 2: the pitch (as a MIDInumber) of the Note;
	 *     as element 3: the full duration (in whole notes) of the Note;
	 *
	 * NB: In order to keep all constant chord feature vectors the same size, if the chord has fewer notes than 
	 * largestChordSizeTraining, a List containing only -1.0s is added for each "missing" note. The List<List>>
	 * returned, in other words, always has size largestChordSizeTraining.
	 *
	 * @param tablature
	 * @param basicTabSymbolProperties
	 * @param lowestNoteIndex
	 * @return
	 */
	// TESTED (for both tablature- and non-tablature case)
	List<List<Double>> getIndividualNoteFeaturesChord(Integer[][] basicTabSymbolProperties, Integer[][]
		basicNoteProperties, int largestChordSizeTraining, int lowestNoteIndex, boolean useTablatureInformation) {
		
		Transcription.verifyCase(basicTabSymbolProperties, basicNoteProperties);
		
	  // 1. For each note: add the individual note features to a List, and add that List to
		// individualNoteFeaturesChord
		List<List<Double>> individualNoteFeaturesChord = new ArrayList<List<Double>>();
		int numNotesInChord = 0;
		
		// a. In the tablature case
		if (basicTabSymbolProperties != null) {
			numNotesInChord = basicTabSymbolProperties[lowestNoteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
			// For each note	  
		  for (int i = 0; i < numNotesInChord; i++) {
	  		List<Double> currentIndividualNoteFeatures = new ArrayList<Double>();
	    	double[] basicNoteFeaturesCurrentNoteAsArray = 
	    		FeatureGenerator.getBasicNoteFeaturesMUSCI(basicTabSymbolProperties, null, lowestNoteIndex + i);
	    	// 0. Index
	    	currentIndividualNoteFeatures.add((double)i);
	    	// 1. Pitch
	    	currentIndividualNoteFeatures.add(basicNoteFeaturesCurrentNoteAsArray[FeatureGenerator.PITCH]);
	    	if (useTablatureInformation) {
	    	  // 2. Course
	    	  currentIndividualNoteFeatures.add(basicNoteFeaturesCurrentNoteAsArray[FeatureGenerator.COURSE]);
		  	  // 3. Fret
	    	  currentIndividualNoteFeatures.add(basicNoteFeaturesCurrentNoteAsArray[FeatureGenerator.FRET]);
		  	  // 4. Maximum duration
	    	  currentIndividualNoteFeatures.add(basicNoteFeaturesCurrentNoteAsArray[FeatureGenerator.MAX_DURATION]);
	    	}
	      // Add currentIndividualNoteFeatures to individualNoteFeaturesChord
        individualNoteFeaturesChord.add(currentIndividualNoteFeatures);
		  }
		}
	  // b. In the non-tablature case
		else if (basicNoteProperties != null) {
			numNotesInChord = basicNoteProperties[lowestNoteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
			// For each note	  
		  for (int i = 0; i < numNotesInChord; i++) {
		  	int noteNumber = lowestNoteIndex + i;
	  		List<Double> currentIndividualNoteFeatures = new ArrayList<Double>();
		  	// 0. Index (exclusive) 
	  		int indexExcl = i;
		  	currentIndividualNoteFeatures.add((double) indexExcl);
		    // 1. Index (inclusive)
		  	int indexIncl = indexExcl;
		  	int currentPitch = basicNoteProperties[noteNumber][Transcription.PITCH]; 
		  	// Determine whether there are sustained notes in the chord. If not, indexIncl == indexExcl. If so,
		  	// increase indexIncl for every sustained note with a pitch lower than or equal to currentPitch   
		  	List<Integer> sustainedPitches = 
			  	getPitchesOfSustainedPreviousNotesInChordMUSCI(basicNoteProperties, lowestNoteIndex);
		  	if (sustainedPitches.size() != 0) {
		  		for (int p : sustainedPitches) {
		  			if (p <= currentPitch) {
		  				indexIncl++;
		  			}
		  		}
		  	}
		  	currentIndividualNoteFeatures.add((double) indexIncl);
		  	// 2. Pitch
		  	currentIndividualNoteFeatures.add((double) currentPitch);
		  	// 3. Duration
		  	int numDuration = basicNoteProperties[noteNumber][Transcription.DUR_NUMER];
	    	int denomDuration = basicNoteProperties[noteNumber][Transcription.DUR_DENOM];
	    	currentIndividualNoteFeatures.add((double) numDuration / denomDuration);
	      // Add currentIndividualNoteFeatures to individualNoteFeaturesChord
        individualNoteFeaturesChord.add(currentIndividualNoteFeatures);
		  }
		}
		      		
	  // 2. If the chord has fewer notes than largestChordSizeTraining: add for all "missing" notes
		// feature values of -1.0
		if (numNotesInChord < largestChordSizeTraining) {
			// Make the individual features for a missing note
			int numberOfMissingNotes = largestChordSizeTraining - numNotesInChord;
			List<Double> currentMissingNoteFeatures = new ArrayList<Double>();
			int numberOfIndividualNoteFeatures = individualNoteFeaturesChord.get(0).size();
			for (int i = 0; i < numberOfIndividualNoteFeatures; i++) {
		  	currentMissingNoteFeatures.add(-1.0); 
		  }
			// Add currentMissingNoteFeatures as often as needed to individualNoteFeaturesChord 
			for (int i = 0; i < numberOfMissingNotes; i++) {
				individualNoteFeaturesChord.add(currentMissingNoteFeatures);
			}
		}	
		return individualNoteFeaturesChord;
	}
	
	
	/** 
	 * Gets the shared note features for each note in the chord, i.e., the features that are the same for each
	 * note in the chord (and independent of the voice assignment). Returns a List<Double> containing 
	 *   in the tablature case:
	 *     as element 0: the size of the chord
	 *     as element 1: the minimum duration (in whole notes) of the chord
	 *     as element 2: the range of the chord
	 *     as element 3: the intervals in the chord
	 *     
	 *   in the non-tablature case:
	 *     as element 0: the size of the chord the note is in (including any sustained notes)
	 *     as element 1: the number of new onsets in the chord (i.e., the size excluding any sustained notes)
	 *     as element 2: the voices that are sustained (as a binary vector) 
	 *     as element 3: the range of the chord (including any sustained notes)
	 *     as element 4: the intervals in the chord (including any sustained notes)
	 *     
	 * @param basicTabSymbolProperties
	 * @return
	 */
	// TESTED (for both tablature- and non-tablature case) 
	List<Double> getSharedNoteFeaturesChord(Integer[][] basicTabSymbolProperties, Integer[][]	basicNoteProperties, 
		List<List<Double>> allVoiceLabels, int largestChordSizeTraining, int highestNumberOfVoicesTraining,
		int lowestNoteIndex) {

		Transcription.verifyCase(basicTabSymbolProperties, basicNoteProperties);
		
		List<Double> sharedOnsetFeaturesChord = new ArrayList<Double>();
		
	  // a. In the tablature case
	  if (basicTabSymbolProperties != null) {
//		  double[] basicNoteFeaturesFirstOnset = getBasicNoteFeatures(basicTabSymbolProperties, null, 
//		  	lowestNoteIndex, useTablatureInformation); 
		  
	    // 0. Size  
//		  sharedOnsetFeaturesChord.add(basicNoteFeaturesFirstOnset[SIZE]);
		  sharedOnsetFeaturesChord.add((double) basicTabSymbolProperties[lowestNoteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS]);
		  // 1. Minimum duration
//		  sharedOnsetFeaturesChord.add(basicNoteFeaturesFirstOnset[MIN_DURATION]);
		  double minDuration = 
		  	(double) basicTabSymbolProperties[lowestNoteIndex][Tablature.MIN_DURATION] / Tablature.SRV_DEN;
		  sharedOnsetFeaturesChord.add((double) minDuration);		  
		  // 2. Range
		  sharedOnsetFeaturesChord.add(getRangeOfChord(basicTabSymbolProperties, basicNoteProperties, lowestNoteIndex));
      // 3. Intervals in chord
		  double[] intervalsInChord = getIntervalsInChordMUSCI(basicTabSymbolProperties, basicNoteProperties, 
		  	largestChordSizeTraining, lowestNoteIndex);
		  for (int i = 0; i < intervalsInChord.length; i++) {
		    sharedOnsetFeaturesChord.add(intervalsInChord[i]);
		  }
	  }
	  // b. In the non-tablature case
	  if (basicNoteProperties != null) {
//		  double[] basicNoteFeaturesFirstOnset = getBasicNoteFeatures(basicTabSymbolProperties, basicNoteProperties,
//		  	lowestNoteIndex, useTablatureInformation); 
	    // 0. Size
//		  int chordSize = 0;
		  int numNewOnsets = basicNoteProperties[lowestNoteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
		  int allNotes = 
		  	numNewOnsets + getPitchesOfSustainedPreviousNotesInChordMUSCI(basicNoteProperties, lowestNoteIndex).size();
		  sharedOnsetFeaturesChord.add((double) allNotes);
		  // 1. Number of new onsets  
		  sharedOnsetFeaturesChord.add((double) numNewOnsets);
		  // 2. Sustained voices
		  List<Double> sustainedVoices = Arrays.asList(new Double[highestNumberOfVoicesTraining]);
		  Collections.fill(sustainedVoices, 0.0);
		  List<Integer> sustainedPreviousVoices = 
		  	getVoicesOfSustainedPreviousNotesInChordMUSCI(basicNoteProperties, allVoiceLabels, lowestNoteIndex);
		  for (int i : sustainedPreviousVoices) {
		  	sustainedVoices.set(i, 1.0);
		  }
		  sharedOnsetFeaturesChord.addAll(sustainedVoices);
		  // 3. Range
		  sharedOnsetFeaturesChord.add(getRangeOfChord(null, basicNoteProperties, lowestNoteIndex));
      // 4. Intervals in chord
		  double[] intervalsInChord =	
		  	getIntervalsInChordMUSCI(null, basicNoteProperties, largestChordSizeTraining, lowestNoteIndex);
		  for (int i = 0; i < intervalsInChord.length; i++) {
		    sharedOnsetFeaturesChord.add(intervalsInChord[i]);
		  }
	  }
	  return sharedOnsetFeaturesChord;	
	}
	
			
	/**
	 * Returns the range (in semitones) of the chord. 
	 * NB: in the non-tablature case, sustained previous notes are taken in consideration as well.
	 * 
	 * @param btp 
	 * @return
	 */
	// TESTED (for both tablature- and non-tablature case)
	double getRangeOfChord(Integer[][] btp, Integer[][] bnp, int lowestNoteIndex) { 

		Transcription.verifyCase(btp, bnp);

		// 0. Set range to default value 0.0 for a single-note chord
		double range = 0.0;

		// 1. Determine the size of the chord
		int chordSize = 0;
		// a. In the tablature case
		if (btp != null) {
			chordSize = btp[lowestNoteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
		}
		// b. In the non-tablature case
		else if (bnp != null) {
			chordSize = bnp[lowestNoteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
			chordSize += getPitchesOfSustainedPreviousNotesInChordMUSCI(bnp, lowestNoteIndex).size();
		}

		// 2. If the the chord consists of multiple onsets: calculate range
		if (chordSize > 1) {
			List<Integer> pitchesInChord = null;
			// a. In the tablature case
			if (btp != null) {
				pitchesInChord = Tablature.getPitchesInChord(btp, lowestNoteIndex);
//				pitchesInChord = FeatureGenerator.getPitchesInChord(basicTabSymbolProperties, basicNoteProperties, lowestNoteIndex);
			}
			// b. In the non-tablature case
			else if (bnp != null) {
				// List the new pitches in the chord
				pitchesInChord = Transcription.getPitchesInChord(bnp, lowestNoteIndex);
//				pitchesInChord = FeatureGenerator.getPitchesInChord(basicTabSymbolProperties, basicNoteProperties, lowestNoteIndex);
				// Add any previous sustained pitches and to pitchesInChord 
				pitchesInChord.addAll(getPitchesOfSustainedPreviousNotesInChordMUSCI(bnp, lowestNoteIndex));		
			}
			// Calculate the range
			range = Collections.max(pitchesInChord) - Collections.min(pitchesInChord);
		}
		return range;
	}


	/**
	 * Gets the intervals in the chord. Returns a double[] the size of the maximum number of intervals in a chord,
	 * i.e., largestChordSizeTraining - 1. When the chord contains fewer onsets than largestChordSizeTraining, 
	 * -1.0 is added for each "missing" interval.
	 * Example: when largestChordSizeTraining = 5, a chord with pitches [0, 10, 20] (from low to high) will
	 * return [10.0, 10.0, -1.0, -1.0] 
	 * 
	 * NB: In the non-tablature case, sustained previous notes are taken into consideration as well. 
	 * 
	 * @param btp
	 * @return
	 */
	// TESTED (for both tablature- and non-tablature case)
	double[] getIntervalsInChordMUSCI(Integer[][] btp, Integer[][] bnp,
		int largestChordSizeTraining, int lowestNoteIndex) { 

		Transcription.verifyCase(btp, bnp);

		double[] intervalsInChord = new double[largestChordSizeTraining - 1];

		// 0. Initialise intervalsInChord with all -1.0s
		Arrays.fill(intervalsInChord, -1.0);

		// 1. List the pitches in the chord
		List<Integer> pitchesInChord = null;
		// a. In the tablature case
		// NB: For the determination of the intervals between the onsets in the chord, the exact pitch sequence 
		// as in the chord (including any possible course crossings) is needed--so no numerical sorting of 
		// pitchesInChord is necessary
		if (btp != null) {
			pitchesInChord = Tablature.getPitchesInChord(btp, lowestNoteIndex);
		}
		// b. In the non-tablature case
		if (bnp != null) {
			pitchesInChord = Transcription.getPitchesInChord(bnp, lowestNoteIndex);
			// Get any pitches of sustained previous notes and add them to pitchesInChord, then sort numerically
			pitchesInChord.addAll(getPitchesOfSustainedPreviousNotesInChordMUSCI(bnp, lowestNoteIndex));
			Collections.sort(pitchesInChord);
		}

		// 2. Get the intervals in the chord
		// For every pitch: get the intervallic distances
		for (int i = 0; i < pitchesInChord.size() - 1; i++) {
			double currentPitch = pitchesInChord.get(i);
			double nextPitch = pitchesInChord.get(i + 1);
			intervalsInChord[i] = Math.abs(nextPitch - currentPitch); 
		}

		return intervalsInChord;
	}


	/**
	 * Compares each note in the chord to the previous Note in the voice that note is assigned to under the given
	 * voiceAssigment, and calculates their average pitch proximity, their average inter-onset time proximity, 
	 * their average offset-onset time proximity (where, in the tablature case, the previous Note's offset time
	 * is determined by the minimum duration as given in the tablature), and the pitch movement.
	 * Returns a double[] containing:
	 * (1) as element 0, the average of the pitch proximity values of all individual notes
	 * (2) as element 1, the average of the inter-onset time proximity values of all individual notes
	 * (3) as element 2, the average of the offset-onset time proximity values of all individual notes 
	 * (4) as element 3-7, the pitch movement of each voice
	 * 
	 * NB: In the tablature case, when a note contains a CoD, the pitch proximity, inter-onset time proximity, and
	 * offset-onset time proximity to the previous Note in both CoD voices are calculated.  
	 * 
	 * @param btp
	 * @param bnp
	 * @param transcription
	 * @param lowestNoteIndex
	 * @param voiceAssignment
	 * @return
	 */
	// TESTED (for both tablature- and non-tablature case)
	double[] getAverageProximitiesAndMovementsOfChord(Integer[][] btp, Integer[][] bnp,	Transcription 
		transcription, /*int highestNumberOfVoicesTraining,*/ int lowestNoteIndex, List<Integer> voiceAssignment) { 	
			
		Transcription.verifyCase(btp, bnp);
		
		// Make the Lists that will contain the proximities for each onset in the chord 
		List<Double> pitchProximities = new ArrayList<Double>();
		List<Double> interOnsetTimeProximities = new ArrayList<Double>();
		List<Double> offsetOnsetTimeProximities = new ArrayList<Double>();
		
		// Make the Array that will contain the pitch movements of the voices; initialise with all 0.0 (when the voice
		// is not active in the chord)
		// NB: distinguish:
		// (i) If voice x is not active in the chord, i.e., if element x of voiceAssignment is -1, pitchMovements[x] will
		// be 0.0;
		// (ii) If voice x is active in the chord, i.e., if element x of voiceAssignment is not -1, pitchMovements[x] will
		// be 0.0 if there is no previous Note in voice x before the current chord; 0.0 if there is a previous Note in 
		// voice x with the same pitch as the onset in voice x in the current chord, or some positive or negative number if
		// there is a previous Note in voice x with another pitch than the onset in voice x in the current chord
		// This means that:
		// In the tablature case, 0.0 can indicate that voice x 
		//   (a) has no new onset in the chord (is not active); 
		//   (b) has a new onset in the chord, which is the first onset in that voice;
		//   (c) has a new onset in the chord that is of the same pitch as the previous onset in that voice
		// In the non-tablature case, 0.0 can indicate that voice x
		//   (a) has no new onset in the chord (is not active); 
		//   (b) has no new onset in the chord but is sustained; 
		//   (c) has a new onset in the chord, which is the first onset in that voice; 
		//   (d) has a new onset in the chord that is of the same pitch as the previous onset in that voice
		double[] pitchMovements = new double[Transcription.MAXIMUM_NUMBER_OF_VOICES];
//		double[] pitchMovements = new double[highestNumberOfVoicesTraining];
		Arrays.fill(pitchMovements, 0.0);
		
	  // Get the voice labels that go with the given voice assignment; then get the voices
		List<List<Double>> chordVoiceLabels = 
			DataConverter.getChordVoiceLabels(voiceAssignment);
		List<List<Integer>> voices = 
			DataConverter.getVoicesInChord(chordVoiceLabels);
		
		// Determine the size of the chord
		// a. In the tablature case
		int chordSize = 0;
		if (btp != null) {
			chordSize = btp[lowestNoteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS]; 
		}
		// b. In the non-tablature case
		else {
			chordSize = bnp[lowestNoteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
		}
		
		// For each onset in the chord
		for (int i = 0; i < chordSize; i++) {
	  	int currentOnsetIndex = lowestNoteIndex + i; 
	  	Note currentNote = null;
	  	// a. In the tablature case
	  	if (btp != null) {
	  	  currentNote = Tablature.convertTabSymbolToNote(btp, currentOnsetIndex);
	  	}
	  	// b. In the non-tablature case
	  	else {
	  		int pitch = bnp[currentOnsetIndex][Transcription.PITCH];
	  		Rational metricTime = new Rational(bnp[currentOnsetIndex][Transcription.ONSET_TIME_NUMER], 
	  			bnp[currentOnsetIndex][Transcription.ONSET_TIME_DENOM]);
	  		Rational metricDuration = new Rational(bnp[currentOnsetIndex][Transcription.DUR_NUMER], 
	  			bnp[currentOnsetIndex][Transcription.DUR_DENOM]);
	  		currentNote = Transcription.createNote(pitch, metricTime, metricDuration);
	  	}
	  	List<Integer> currentVoices = voices.get(i);
	  	
	  	// For each voice the note is assigned to: calculate the proximities to the previous Note in that 
	  	// voice and the pitch movement, and add the numbers to the lists
	  	for (int j = 0; j < currentVoices.size(); j++) {
	  		int voiceAssignedToNote = currentVoices.get(j);
	  		NotationVoice currentVoice = transcription.getPiece().getScore().get(voiceAssignedToNote).get(0);
			  double[] pitchAndTimeProximities = FeatureGenerator.getProximitiesAndMovementToVoiceMUSCI(btp, currentVoice, currentNote); 
				pitchProximities.add(pitchAndTimeProximities[0]);
			  interOnsetTimeProximities.add(pitchAndTimeProximities[1]);
			  offsetOnsetTimeProximities.add(pitchAndTimeProximities[2]);
			  pitchMovements[voiceAssignedToNote] = pitchAndTimeProximities[3];
	    }
		}
	  
		// Create, set, and return averagePitchAndTimeProximities
		double[] averagePitchAndTimeProximities = new double[3 + Transcription.MAXIMUM_NUMBER_OF_VOICES];
//	  double[] averagePitchAndTimeProximities = new double[3 + highestNumberOfVoicesTraining];
	  averagePitchAndTimeProximities[0] = ToolBox.getAverage(pitchProximities); 
		averagePitchAndTimeProximities[1] = ToolBox.getAverage(interOnsetTimeProximities);
		averagePitchAndTimeProximities[2] = ToolBox.getAverage(offsetOnsetTimeProximities);
		for (int i = 0; i < pitchMovements.length; i++) {
			averagePitchAndTimeProximities[3 + i] = pitchMovements[i]; 
		}
		return averagePitchAndTimeProximities;
	}
	
	
	/**
	 * Gets the pitches of all previous notes that are still sounding at the onset time of the chord, i.e., all
	 * previous notes whose offset time is greater than the onset time of the note at lowestNoteIndex. These
	 * pitches are listed in the sequence in which the notes are encountered; thus, the List returned is not 
	 * necessarily sorted numerically.
	 * 
	 * NB: Non-tablature case only.
	 * 
	 * @param basicNoteProperties
	 * @param lowestNoteIndex
	 * @return
	 */
	// TESTED
	List<Integer> getPitchesOfSustainedPreviousNotesInChordMUSCI(Integer[][] basicNoteProperties, int lowestNoteIndex) {
		List<Integer> pitchesOfSustainedPreviousNotes = new ArrayList<Integer>();
			 
		List<Integer> indicesOfSustainedPreviousNotes = 
			FeatureGenerator.getIndicesOfSustainedPreviousNotesMUSCI(basicNoteProperties, lowestNoteIndex);
		
		for (int i : indicesOfSustainedPreviousNotes) {
			int pitchToAdd = basicNoteProperties[i][Transcription.PITCH];
  		pitchesOfSustainedPreviousNotes.add(pitchToAdd);
		}
		
		return pitchesOfSustainedPreviousNotes;
	}
	
	
	/**
	 * Gets the voices of all previous Notes that are still sounding at the onset time of the chord, i.e., all
	 * previous Notes whose offset time is greater than the onset time of the Note at lowestNoteIndex. These
	 * pitches are listed in the sequence in which they Notes are encountered; thus, the List returned is not 
	 * necessarily sorted numerically.
	 * 
	 * NB: Non-tablature case only.
	 * 
	 * @param basicNoteProperties
	 * @param lowestNoteIndex
	 * @return
	 */
	// TESTED
	List<Integer> getVoicesOfSustainedPreviousNotesInChordMUSCI(Integer[][] basicNoteProperties, List<List<Double>> 
	  allVoiceLabels, int lowestNoteIndex) {
		List<Integer> voicesOfSustainedPreviousNotes = new ArrayList<Integer>();
			 
		List<Integer> indicesOfSustainedPreviousNotes = 
			FeatureGenerator.getIndicesOfSustainedPreviousNotesMUSCI(basicNoteProperties, lowestNoteIndex);
		
		for (int i : indicesOfSustainedPreviousNotes) {
			List<Double> currentVoiceLabel = allVoiceLabels.get(i);
  		List<Integer> currentVoice = 
  			DataConverter.convertIntoListOfVoices(currentVoiceLabel);
  		// currentVoice will contain only one element as CoDs do not occur
  		voicesOfSustainedPreviousNotes.add(currentVoice.get(0));
		}
		
		return voicesOfSustainedPreviousNotes;
	}
	
		
	/**
	 * Generates the constant feature vector for the chord, containing the features that are independent of the 
	 * voice assignment. The constant feature vector consists of the chord's individual note features (see
	 * getIndividualNoteFeaturesChord()) and its shared onset features (see getSharedNoteFeaturesChord()).   
	 *             
	 * @param tablature
	 * @param basicTabSymbolPropertiesChord
	 * @param lowestNoteIndex
	 * @return
	 */
	// TESTED (for both tablature- and non-tablature case)
	List<Double> generateConstantChordFeatureVector(Integer[][] basicTabSymbolProperties,	Integer[][]	basicNoteProperties,
		List<List<Double>> allVoiceLabels, int largestChordSizeTraining, int highestNumberOfVoicesTraining,
		int lowestNoteIndex, boolean useTablatureInformation) { 
		
		Transcription.verifyCase(basicTabSymbolProperties, basicNoteProperties);
		
		List<Double> constantChordFeatureVector = new ArrayList<Double>();
		
		// 1. Get the features that are different for every note in the chord and add them to constantChordFeatureVector 
		List<List<Double>> individualNoteFeaturesChord = getIndividualNoteFeaturesChord(basicTabSymbolProperties,
			basicNoteProperties, largestChordSizeTraining, lowestNoteIndex, useTablatureInformation);
	  for (int i = 0; i < individualNoteFeaturesChord.size(); i++) {
	  	List<Double> individualOnsetFeaturesCurrentOnset = individualNoteFeaturesChord.get(i); 
	  	constantChordFeatureVector.addAll(individualOnsetFeaturesCurrentOnset);
	  }		
		// 2. Get the features that are the same for (every note in) the complete chord, and add them to 
	  // constantChordFeatureVector	 	
	 	List<Double> sharedNoteFeatures = getSharedNoteFeaturesChord(basicTabSymbolProperties, basicNoteProperties, 
		  allVoiceLabels, largestChordSizeTraining, highestNumberOfVoicesTraining, lowestNoteIndex);
	 	
	 	constantChordFeatureVector.addAll(sharedNoteFeatures);
	 	
		return constantChordFeatureVector;		
	}


	/**
	 * Generates a feature vector for the chord represented by the given chordOnsetProperties under the given voice
	 * assignment, containing the chord's variable features (i.e., those features that are different for each voice
	 * assignment):
	 * 
	 * The average pitch- and time proximities and the pitch movements (see getAverageProximitiesAndMovementsOfChord())
	 * The pitch-voice relation (see getPitchVoiceRelationInChord())
	 * Voice crossing information (see getVoiceCrossingInformationInChord()): 
	 *   The number of voice crossing pairs
	 *	 The sum of the pitch distances between the voice crossing pairs
	 *	 The average distance between the voice crossing pairs
	 * The voice assignment   
	 * 
	 * @param chordOnsetProperties
	 * @param voiceAssignment
	 * @param tablature
	 * @param transcription
	 * @param lowestNoteIndex
	 * @return
	 */
	// TESTED
	List<Double> generateVariableChordFeatureVector(Integer[][] btp, Integer[][] bnp,
		Transcription transcription, int lowestNoteIndex, int highestNumberOfVoicesTraining, 
		List<Integer> voiceAssignment) {
		List<Double> variableChordFeatureVector = new ArrayList<Double>();

		Transcription.verifyCase(btp, bnp);

		// Add average pitch and time proximities information and pitch movements
		double [] averagePitchAndTimeProxOfChord = 
			getAverageProximitiesAndMovementsOfChord(btp, 
			bnp, transcription, /*highestNumberOfVoicesTraining,*/ lowestNoteIndex, voiceAssignment);
		variableChordFeatureVector.add(averagePitchAndTimeProxOfChord[0]);
		variableChordFeatureVector.add(averagePitchAndTimeProxOfChord[1]);
		variableChordFeatureVector.add(averagePitchAndTimeProxOfChord[2]);
		for (int i = 0; i < averagePitchAndTimeProxOfChord.length - 3; i++) {
			variableChordFeatureVector.add(averagePitchAndTimeProxOfChord[3 + i]);
		}

		// Add pitch-voice relation information
//		List<List<Double>> allVoiceLabels = transcription.getMostRecentVoiceLabels();
		List<List<Double>> allVoiceLabels = transcription.getVoiceLabels();
		variableChordFeatureVector.add(getPitchVoiceRelationInChord(btp, bnp, 
			allVoiceLabels, lowestNoteIndex, voiceAssignment));

		// Add voice crossing information
		// 1. Get pitchesInChord and voicesInChord
		List<Integer> pitchesInChord = null;
		List<List<Integer>> voicesInChord = null;
		List<List<Double>> voiceLabels = DataConverter.getChordVoiceLabels(voiceAssignment);
		// a. In the tablature case
		if (btp != null) {
			pitchesInChord = Tablature.getPitchesInChord(btp, lowestNoteIndex);
			voicesInChord = DataConverter.getVoicesInChord(voiceLabels);
		}
		// b. In the non-tablature case
		if (bnp != null) {			  
			// Get the pitches and the voices in the chord, including those of sustained previous notes
			pitchesInChord = Transcription.getPitchesInChord(bnp, lowestNoteIndex);
			voicesInChord = DataConverter.getVoicesInChord(voiceLabels);
			List<List<Integer>> allPitchesAndVoices = 
				Transcription.getAllPitchesAndVoicesInChord(bnp, 
				pitchesInChord, voicesInChord, allVoiceLabels, lowestNoteIndex);
			// voicesInChord must be a List<List>>
			pitchesInChord = allPitchesAndVoices.get(0);
			voicesInChord = new ArrayList<List<Integer>>();
			for (int i : allPitchesAndVoices.get(1)) {
				int currentVoice = i;
				List<Integer> voiceWrapped = Arrays.asList(new Integer[]{currentVoice});
				voicesInChord.add(voiceWrapped);
			}
		}
		// 2. Get voiceCrossingInformation
		List<List<Integer>> voiceCrossingInformation = 
			Transcription.getVoiceCrossingInformationInChord(pitchesInChord, voicesInChord);
		// a. Number of voice crossing pairs
		double numberOfVoiceCrossingPairs = voiceCrossingInformation.get(1).size() / 2;
		variableChordFeatureVector.add(numberOfVoiceCrossingPairs);
		// b. The sum of the pitch distances between the voice crossing pairs
		double sumOfPitchDistancesOfVoiceCrossingPairs = ToolBox.sumListInteger(voiceCrossingInformation.get(2));
		variableChordFeatureVector.add(sumOfPitchDistancesOfVoiceCrossingPairs);
		// c. The average distance between the voice crossing pairs
		double averagePitchDistancesOfVoiceCrossingPairs = 0.0;
		if (voiceCrossingInformation.get(2).size() > 0) {
			averagePitchDistancesOfVoiceCrossingPairs = 
				sumOfPitchDistancesOfVoiceCrossingPairs / voiceCrossingInformation.get(2).size();
		}
		variableChordFeatureVector.add(averagePitchDistancesOfVoiceCrossingPairs);

		// 3. Add voice assignment
		List<Double> voiceAssignmentAsDoubles = ToolBox.convertToListDouble(voiceAssignment);
		variableChordFeatureVector.addAll(voiceAssignmentAsDoubles);

		return variableChordFeatureVector;
	}


	/**
	 * Returns, for the chord represented by the given onsetProperties, the feature vector that goes with the given
	 * voice assignment.
	 * 
	 * @param tablature
	 * @param basicTabSymbolProperties
	 * @param transcription
	 * @param chordOnsetProperties
	 * @param lowestNoteIndex
	 * @param voiceAssignment
	 * @return
	 */
  // TESTED through testing generateCompleteChordFeatureVectorsMUSCI()
	public List<Double> generateCompleteChordFeatureVectorMUSCI(Integer[][] basicTabSymbolProperties, Integer[][]
		basicNoteProperties, Transcription transcription, int lowestNoteIndex, int largestChordSizeTraining, 
		int highestNumberOfVoicesTraining, List<Integer> voiceAssignment,	boolean useTablatureInformation) { // TODO Activate argument useTablatureInformation
						
		Transcription.verifyCase(basicTabSymbolProperties, basicNoteProperties);
			
		List<Double> completeChordFeatureVector = new ArrayList<Double>();

		List<List<Double>> allVoiceLabels = transcription.getVoiceLabels();
		
		// Generate the constant feature vector
		List<Double> constantChordFeatureVector = generateConstantChordFeatureVector(basicTabSymbolProperties,
			basicNoteProperties, allVoiceLabels, largestChordSizeTraining, highestNumberOfVoicesTraining, lowestNoteIndex, useTablatureInformation);
		// Generate the variable feature vector
		List<Double> variableChordFeatureVector = generateVariableChordFeatureVector(basicTabSymbolProperties, 
			basicNoteProperties, transcription, lowestNoteIndex, highestNumberOfVoicesTraining, voiceAssignment);

		// Create and return the complete chord feature vector
		completeChordFeatureVector.addAll(constantChordFeatureVector);
	  completeChordFeatureVector.addAll(variableChordFeatureVector);
		
	  return completeChordFeatureVector;
	}
	
	
	/**
	 * Calculates all possible voice assignments for the chord at chordIndex in tablature, and returns a List of the 
	 * feature vectors that go with those voice assignments. The first element of the List returned is the feature
	 * vector that goes with the ground truth voice assignment.
	 *  
	 * @param tablature
	 * @param basicTabSymbolProperties
	 * @param lowestNoteIndex
	 * @param transcription
	 * @param orderedVoiceAssignmentPossibilitiesCurrentChord
	 * @return
	 */	
	// TESTED through testing generateAllCompleteChordFeatureVectorsMUSCI()
	private List<List<Double>> generateCompleteChordFeatureVectorsMUSCI(Integer[][] basicTabSymbolProperties, 
		Integer[][] basicNoteProperties, Transcription transcription, int lowestNoteIndex, int largestChordSizeTraining, 
		int highestNumberOfVoicesTraining, List<List<Integer>> orderedVoiceAssignmentPossibilitiesCurrentChord, 
		boolean useTablatureInformation) {
				
		Transcription.verifyCase(basicTabSymbolProperties, basicNoteProperties);
		
		List<List<Double>> completeChordFeatureVectors = new ArrayList<List<Double>>();
			 
		// Generate, for each voice assignment, the complete chord feature vector. Since the first element of 
		// orderedVoiceAssignmentPossibilitiesCurrentChord is the ground truth voice assignment, the first element of each
		// element of completeChordFeatureVectors will be the ground truth feature vector
	 	for (int i = 0; i < orderedVoiceAssignmentPossibilitiesCurrentChord.size(); i++) {
	 		List<Integer> currentVoiceAssignment = orderedVoiceAssignmentPossibilitiesCurrentChord.get(i);
	 		List<Double> currentCompleteChordFeatureVector = generateCompleteChordFeatureVectorMUSCI(basicTabSymbolProperties, 
	 			basicNoteProperties, transcription, lowestNoteIndex, largestChordSizeTraining, highestNumberOfVoicesTraining, 
	 			currentVoiceAssignment,	useTablatureInformation);
	 		completeChordFeatureVectors.add(currentCompleteChordFeatureVector);	
	 	}	
	 	
	 	return completeChordFeatureVectors;
	}
	
		
	/**
	 * Returns a List of all possible feature vectors for each chord. The first element of each List represents the 
	 * feature vector that goes with the ground truth voice assignment; the others the feature vectors that go with
	 * all the other possible voice assignments. 
	 *  
	 * @param tablature
	 * @param transcription
	 * @param highestNumberOfVoicesTraining
	 * @return
	 */
	// TESTED (for both tablature- and non-tablature case)
	public List<List<List<Double>>> generateAllCompleteChordFeatureVectorsMUSCI(Integer[][] basicTabSymbolProperties,
		Integer[][] basicNoteProperties, Transcription transcription, int largestChordSizeTraining, 
		int highestNumberOfVoicesTraining, boolean useTablatureInformation) {
	
		Transcription.verifyCase(basicTabSymbolProperties, basicNoteProperties);
		
		List<List<List<Double>>> allCompleteChordFeatureVectors = new ArrayList<List<List<Double>>>();
		
		int numberOfChords = 0;
		// a. In the tablature case
		if (basicTabSymbolProperties != null) {
			int indexOfLastNote = basicTabSymbolProperties.length - 1;
		  numberOfChords = basicTabSymbolProperties[indexOfLastNote][Tablature.CHORD_SEQ_NUM] + 1;
		}
	  // b. In the non-tablature case
		else {
			int indexOfLastNote = basicNoteProperties.length - 1;
		  numberOfChords = basicNoteProperties[indexOfLastNote][Transcription.CHORD_SEQ_NUM] + 1;
		}
			    
    // NB: pre-set voiceLabels possible because this method is only used in training and test mode
//  	List<List<Double>> voiceLabels = transcription.getMostRecentVoiceLabels(); 
		List<List<Double>> voiceLabels = transcription.getVoiceLabels();
  	List<List<List<Integer>>> orderedVoiceAssignmentPossibilitiesAllChords = 
  		getOrderedVoiceAssignments(basicTabSymbolProperties, basicNoteProperties,	voiceLabels, 
  		highestNumberOfVoicesTraining);
  	
  	// For each chord: generate the list of complete feature vectors and add those to allCompleteChordFeatureVectors  
		int lowestNoteIndex = 0;
  	for (int i = 0; i < numberOfChords; i++) {  
  		int chordSize = 0;
  		// a. In the tablature case
  		if (basicTabSymbolProperties != null) {
  		  // Find the size of the chord with sequence number i
  			for (int j = 0; j < basicTabSymbolProperties.length; j++) {
  		  	if (basicTabSymbolProperties[j][Tablature.CHORD_SEQ_NUM] == i) {
  		  		chordSize = basicTabSymbolProperties[j][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
  		  		break;
  		  	}
  		  }
  	  }
  		if (basicNoteProperties != null) {
  		  // Find the size of the chord with sequence number i
  			for (int j = 0; j < basicNoteProperties.length; j++) {
  		  	if (basicNoteProperties[j][Transcription.CHORD_SEQ_NUM] == i) {
  		  		chordSize = basicNoteProperties[j][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
  		  		break;
  		  	}
  		  }
  		}
  		List<List<Integer>> orderedVoiceAssignmentPossibilitiesCurrentChord = 
  			orderedVoiceAssignmentPossibilitiesAllChords.get(i);
  	  List<List<Double>> currentCompleteChordFeatureVectors =	generateCompleteChordFeatureVectorsMUSCI(basicTabSymbolProperties, 
  	  	basicNoteProperties, transcription, lowestNoteIndex, largestChordSizeTraining, highestNumberOfVoicesTraining, 
  	  	orderedVoiceAssignmentPossibilitiesCurrentChord, useTablatureInformation);
  	  allCompleteChordFeatureVectors.add(currentCompleteChordFeatureVectors);
  	  lowestNoteIndex += chordSize;
		}
		return allCompleteChordFeatureVectors;
	}


  // =================================== MISCELLANEOUS/OBSOLETE ===================================
	/**
	 * Adapts the given voice assignment by checking, for each new onset, how many sustained pitches are below it,
	 * and then increasing the onset by that number. 
	 * Example: given a chord with two new onsets (pitch 30 and 40), voice assignment [1, 0, -1, -1], and two
	 * sustained pitches (10 and 20), the voice assignment will be adapted to [3, 2, -1, -1].
	 *   
	 * @param basicNoteProperties
	 * @param voiceAssignment
	 * @param lowestNoteIndex
	 * @return
	 */
	private List<Integer> adaptVoiceAssignment(Integer[][] basicNoteProperties, List<Integer> voiceAssignment, 
		List<Integer> sustainedPitches, int lowestNoteIndex) {
		List<Integer> adaptedVoiceAssignment = new ArrayList<Integer>(voiceAssignment);
		
//		// 1. Get the pitches of the sustained notes
//		List<Integer> sustainedPitches = 
//			getPitchesOfSustainedPreviousNotesInChord(basicNoteProperties, lowestNoteIndex);
		
		// 2. Get the pitch of each new onset in the chord and check how many sustained pitches are below it;
		// increase the onset number accordingly
		int chordSize = basicNoteProperties[lowestNoteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
    Integer[][] basicNotePropertiesChord = 
			Arrays.copyOfRange(basicNoteProperties, lowestNoteIndex, lowestNoteIndex + chordSize);	
    for (int i = 0; i < basicNotePropertiesChord.length; i++) {
      int currentOnsetNumber = i;
      int indexInVoiceAssignment = voiceAssignment.indexOf(currentOnsetNumber);
      int pitchCurrentNote = basicNotePropertiesChord[i][Transcription.PITCH];

      // Determine how many lower sustained pitches there are 
      int numLowerSustainedPitches = 0;
      for (int sustainedPitch : sustainedPitches) {
      	if (sustainedPitch < pitchCurrentNote) {
      		numLowerSustainedPitches++;
      	}
      }
      
      // Increase currentOnsetNumber by the number of sustained pitches below it; then reset the element at index
      // i in voiceAssignment
      int newOnsetNumber = currentOnsetNumber + numLowerSustainedPitches;
      adaptedVoiceAssignment.set(indexInVoiceAssignment, newOnsetNumber);
		}
		return adaptedVoiceAssignment;
	}
	
	
	/**
	 * Returns the skewness of the chord; i.e., whether its inner pitches are perfectly centered, inclined
	 * towards the bottom, or inclined towards the top. In the first case the chord is not skewed and 0.5 is returned; in
	 * the second a number < 0.5; and in the third a number > 0.5. If there are no inner pitches (i.e., if the chord 
	 * consists of only one or two onsets), the chord is also not skewed, and 0.5 is returned as well.    
	 * 
	 * @param basicTabSymbolProperties
	 * @return
	 */
	private double getSkewnessOfChord(Integer[][] basicTabSymbolProperties, int lowestNoteIndex) {
		// a. If the chord consists of one or two onsets: skewness = 0.5
		double skewness = 0.5;
		
		int chordSize = basicTabSymbolProperties[lowestNoteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
		Integer[][] basicTabSymbolPropertiesChord = 
			Arrays.copyOfRange(basicTabSymbolProperties, lowestNoteIndex, lowestNoteIndex + chordSize);	
		
		// b. If the chord consists of more than two onsets: calculate skewness
		if (basicTabSymbolPropertiesChord.length > 2) {
			// 1. List the pitches in the chord
			// NB: For the calculation of the skewness, the exact pitch sequence as in the chord (including any possible
			// course crossings) will give errors when a course crossing involves one of the outer onsets--so a numerical 
			// sorting of pitchesInChord is necessary.
			List<Integer> pitchesInChord = Tablature.getPitchesInChord(basicTabSymbolProperties, lowestNoteIndex);

		  // 2. Determine the distribution of the inner pitches
			// The chord may contain course crossings; therefore, pitchesInChord must be sorted numerically first
		  Collections.sort(pitchesInChord);
		  double rangeInverted = 1.0 / getRangeOfChord(basicTabSymbolProperties, null, lowestNoteIndex);
		  List < Double> distributionOfPitches = new ArrayList<Double>();
		  // For all pitches inbetween the lowest and the highest:
		  for (int i = 1; i < pitchesInChord.size() - 1; i++) {
		    // Determine the current pitch's distance to the lowest; then multiply that by rangeInverted to determine the
		  	// distribution within the chord of the current pitch, and add this number to distributionOfPitches 
		  	double currentDistanceToLowestPitch = pitchesInChord.get(i) - pitchesInChord.get(0);
		  	distributionOfPitches.add(currentDistanceToLowestPitch * rangeInverted);
		  }
		  // 3. Calculate the skewness by averaging the pitch distributions
		  skewness = ToolBox.getAverage(distributionOfPitches); 
		}
		return skewness;
	}
	
	
	/**
	 * Gets the number of active voices (i.e., the voices that have a new onset) in the given voice assignment.
	 * 
	 * @param voiceAssignment
	 * @return
	 */ 
	private double getNumberOfActiveVoicesInChord(List<Integer> voiceAssignment) {
		double activeVoices = 0.0;
		for (int i = 0; i < voiceAssignment.size(); i++) {
			// If voice i contains a number that is not -1: increase activeVoices
			if(voiceAssignment.get(i) != -1) {
				activeVoices++;
			}
		}
		return activeVoices;
	}
	
	
	/**
	 * Retrieves a unique ID for the given voice assignment.
	 * 
	 * @param voiceAssignment
	 * @return
	 */
	private int getID(List<Integer> voiceAssignment) {
		// Add 1 to all elements of voiceAssignment to get rid of -1s
		for (int i = 0; i < voiceAssignment.size(); i++) {
  		voiceAssignment.set(i, voiceAssignment.get(i) + 1);
  	}
		// Reverse voiceAssignment
		List<Integer> reversedVoiceAssignment = new ArrayList<Integer>(voiceAssignment);
		Collections.reverse(reversedVoiceAssignment);
		// Get the ID by doing, for each element:
		// (1) multiply the value of the element by possibilitiesPerVoice^the index of the element
		// (2) sum the outcomes for all elements
		int ID = 0;
		int possibilitiesPerVoice = Tablature.MAXIMUM_NUMBER_OF_NOTES + 1;
		for (int i = 0; i < reversedVoiceAssignment.size(); i++) {
  		int currentIndex = i;
  		int currentOnset = reversedVoiceAssignment.get(i);
  		int toAdd = (int)(currentOnset * Math.pow(possibilitiesPerVoice, currentIndex));
  		ID += toAdd;
  	}
		return ID;
	}
	
	
	/**
	 * Gets the spread of the intervals within the chord. Standard deviation of intervals, regular/irregular
	 * 
	 * @param chordOnsetProperties
	 * @return
	 */
	private double getSpreadOfIntervalsInChord(List<List<Integer>> chordOnsetProperties) {
	  double spreadOfIntervals = 0.0;
	  return spreadOfIntervals;
	}
	
}

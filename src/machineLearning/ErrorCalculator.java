package machineLearning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import tools.ToolBox;
import utility.DataConverter;
import de.uos.fmt.musitech.utility.math.Rational;
import representations.Transcription;

public class ErrorCalculator {

	public static final int NUM_NOTES = 0;
	public static final int NUM_INCORRECT = 1;
	public static final int NUM_OVERLOOKED = 2;
	public static final int NUM_SUPERFLUOUS = 3;
	public static final int NUM_HALF = 4;

	public static final int GENERAL_VOICE = 0;
	public static final int INCORRECT_VOICE = 1;
	public static final int OVERLOOKED_VOICE = 2;
	public static final int SUPERFLUOUS_VOICE = 3;
	public static final int HALF_VOICE = 4;
	public static final int GENERAL_DUR = 5;
	public static final int INCORRECT_DUR = 6;
	public static final int OVERLOOKED_DUR = 7;

	private static final double weightOfSpecialErrors = 0.5;
	public static final double CRE_SUM_DIV = Math.log(2); 

	/**
	 * Calculates all the voices assignment errors for the given predicted voices. Only one error per note is 
	 * allowed; there are four kinds of errors:
	 * (1) Incorrect assignment: both predictedVoices and actualVoices contain one element, and they are not the same
	 * (2) Overlooked CoD assignment: predictedVoices contains one element, actualVoices two (i.e., is a CoD), and
	 *     the predicted voice is one of the actual voices. If the latter is not true: incorrect assignment 
	 * (3) Superfluous CoD assignment: predictedVoices contains two elements (i.e., is a CoD), actualVoices one, and
	 *     the actual voice is one of the predicted voices. If the latter is not true: incorrect assignment
	 * (4) Half CoD assignment: both predictedVoices and actualVoices contain two elements (i.e., are CoDs), and 
	 *     only one of the predicted voices is an actual voice. If the latter is not true, i.e., if neither of the
	 *     predicted voices is an actual voice: incorrect assignment
	 *     
	 * Returns a List<List>> with all the assignment errors, containing:
	 *   as element 0: a List of size 5, containing general high-level information, i.e.: 
	 *                 (0) the total number of notes; 
	 *                 (1) the number of incorrect assignments; 
	 *                 (2) the number of overlooked CoD assignments; 
	 *                 (3) the number of superfluous CoD assignments; 
	 *                 (4) the number of half CoD assignments.
	 *   as element 1: the indices of all incorrect assignments;
	 *   as element 2: the indices of all overlooked CoD assignments;
	 *   as element 3: the indices of all superfluous CoD assignments;
	 *   as element 4: the indices of all half CoD assignments;
	 * and, if modelling duration,
	 *   as element 5: a List of size 3, containing general high-level information, i.e.: 
	 *                 (0) the total number of notes; 
	 *                 (1) the number of incorrect duration assignments; 
	 *                 (2) the number of overlooked CoD duration assignments; 
	 *   as element 6: the indices of all incorrect duration assignments;
	 *   as element 7: the indices of all overlooked CoD duration assignments.  
	 * 
	 * @param allPredictedVoices
	 * @param groundTruthVoiceLabels
	 * @param allPredictedDurations
	 * @param groundTruthDurationLabels
	 * @param equalDurationUnisonsInfo
	 * @return 
	 */
	// TESTED for both tablature- and non-tablature case; deals with EDU currectly
	public static List<List<Integer>> calculateAssignmentErrors(List<List<Integer>> allPredictedVoices, 
		List<List<Double>> groundTruthVoiceLabels, List<Rational[]> allPredictedDurations,
		List<List<Double>> groundTruthDurationLabels, List<Integer[]> equalDurationUnisonsInfo) {			
		List<List<Integer>> assignmentErrors = new ArrayList<List<Integer>>();

		// 1. Determine the indices of any notes falling within one of the four categories (tablature case)
		// or falling in the incorrect category (non-tablature case)
		List<Integer> indicesOfIncorr = new ArrayList<Integer>();
		List<Integer> indicesOfOverl = new ArrayList<Integer>();
		List<Integer> indicesOfSuperfl = new ArrayList<Integer>();
		List<Integer> indicesOfHalf = new ArrayList<Integer>();

		List<Integer> indicesOfIncorrDur = new ArrayList<Integer>();
		List<Integer> indicesOfOverlDur = new ArrayList<Integer>();

		int numberOfNotes = groundTruthVoiceLabels.size(); // TODO is this value used when this method is called?
		for (int i = 0; i < numberOfNotes; i++) {
			// Get the predicted voices and the actual voices for the note at index i
			List<Integer> predictedVoices = allPredictedVoices.get(i);
			List<Double> actualVoiceLabel = groundTruthVoiceLabels.get(i);
			List<Integer> actualVoices = DataConverter.convertIntoListOfVoices(actualVoiceLabel);

			// a. In the tablature case
			if (equalDurationUnisonsInfo == null) {
				// 1. Voice assignments
				// Check whether the note is assigned correctly. There are four possibilities:
				// 1. predictedVoices contains one element and actualVoices one
				if (predictedVoices.size() == 1 && actualVoices.size() == 1) { 
					// If the elements are not the same: incorrect assignment
					if (actualVoices.get(0) != predictedVoices.get(0)) {
						indicesOfIncorr.add(i); // --> NOK in TA and in SA
					}	
				}
				// 2. predictedvoices contains one element and actualVoices two (i.e., if the note is a CoD) 
				if (predictedVoices.size() == 1 && actualVoices.size() == 2) {
					// If actualVoices contains predictedVoices.get(0): correct assignment but one CoDnote overlooked
					if (actualVoices.contains(predictedVoices.get(0))) {
						indicesOfOverl.add(i); // --> OK in TA; NOK in SA (in SA added to incorrect)
					}
					// If actualVoices does not contain predictedVoices.get(0): incorrect assignment
					else {
						indicesOfIncorr.add(i);	// --> NOK in TA and in SA
					}
				}
				// 3. predictedVoices contains two elements (i.e., if a CoD is predicted for the note) and actualVoices one
				if (predictedVoices.size() == 2 && actualVoices.size() == 1) {
					// If predictedVoices contains actualVoices.get(0): correct assignment but one CoDnote assigned superfluously
					if (predictedVoices.contains(actualVoices.get(0))) {
						indicesOfSuperfl.add(i); // --> OK in TA; NOK in SA (in SA added to incorrect)
					}	
					// If predictedVoices does not contain actualVoices.get(0): incorrect assignment
					else {
						indicesOfIncorr.add(i);	// --> NOK in TA and in SA
					}
				}
				// 4. predictedVoices contains two elements (i.e., if a CoD is predicted for the note) and actualVoices
				// two (i.e., if the note is a CoD)
				if (predictedVoices.size() == 2 && actualVoices.size() == 2) {
					// If predictedVoices contains only one of the elements in actualVoices: correct assignment but one
					// CoDNote assigned wrongly
					if (predictedVoices.contains(actualVoices.get(0)) || predictedVoices.contains(actualVoices.get(1))) { 
						// Only if predictedVoices does not contain both elements of actualVoices  
						if (!(predictedVoices.contains(actualVoices.get(0)) && predictedVoices.contains(actualVoices.get(1)))) {
							indicesOfHalf.add(i); // --> OK in TA, NOK in SA (in SA added to incorrect)
						}
					}
					// If predictedVoices contains neither of the elements in actualVoices: incorrect assignment
					if (!predictedVoices.contains(actualVoices.get(0)) && !predictedVoices.contains(actualVoices.get(1))) {
						indicesOfIncorr.add(i); // --> NOK in TA and in SA
					}
				}
				// 2. When modelling duration: duration assignments
				if (groundTruthDurationLabels != null) {
					// Get the predicted durations and the actual durations for the note at index i
//					List<Double> predictedDurationLabel = allPredictedDurationLabels.get(i);
					Rational[] predictedDurations = allPredictedDurations.get(i);
					List<Double> actualDurationLabel = groundTruthDurationLabels.get(i);
					Rational[] actualDurations = DataConverter.convertIntoDuration(actualDurationLabel);

					// NB: predictedDurations will currently always contain only one element
					// If actualDurations contains one element (i.e., the note at index i is no CoD or a CoD whose notes have
					// the same duration)
					if (actualDurations.length == 1) {
						// If the the predicted duration does not equal the actual duration: incorrect duration assignment 
						if (!predictedDurations[0].equals(actualDurations[0])) {
							indicesOfIncorrDur.add(i);
						}
					}
					// If actualDurations contains two element (i.e., the note at index i is a CoD whose notes have different
					// durations)
					else if (actualDurations.length == 2) {
						// If the predicted durations equals one of the actual durations: half CoD duration assignment
						if (predictedDurations[0].equals(actualDurations[0]) || predictedDurations[0].equals(actualDurations[1])) {
							indicesOfOverlDur.add(i);
						}
						// If the predicted duration equals none of the actual durations: incorrect duration assignment
						if (!predictedDurations[0].equals(actualDurations[0]) && !predictedDurations[0].equals(actualDurations[1])) {
							indicesOfIncorrDur.add(i);
						}
					}
				}
			}

			// b. In the non-tablature case (where there are no CoDs and both predictedVoices.size() and 
			// actualVoices.size() therefore will be 1)
			if (equalDurationUnisonsInfo != null) {
				// a. If the note is not the lower unison note of an equal duration unison
				if (equalDurationUnisonsInfo.get(i) == null)	{
					// If the elements are not the same: incorrect assignment
					if (actualVoices.get(0) != predictedVoices.get(0)) {
						indicesOfIncorr.add(i);
					}	
				}
				// b. If the note is the lower unison note of an equal duration unison
				else {
					// 1. Determine the voices allowed
//					List<Double> allowedVoicesAsVoiceLabel = equalDurationUnisonsInfo.get(i);
//					List<Integer> allowedVoices = dataConverter.convertIntoListOfVoices(allowedVoicesAsVoiceLabel);
					Integer[] eDUInfo = equalDurationUnisonsInfo.get(i);
					List<Integer> allowedVoices = Arrays.asList(new Integer[]{eDUInfo[1], eDUInfo[0]});
					// 2. Get the predicted voice for the lower and upper unison note (at index i + 1)
					int predictedVoiceLower = predictedVoices.get(0);
					int predictedVoiceUpper = allPredictedVoices.get(i + 1).get(0);
					List<Integer> indicesOfIncorrectEDUNotes = getIndicesOfIncorrectEDUNotes(predictedVoiceLower, 
						predictedVoiceUpper, allowedVoices, i); 
					indicesOfIncorr.addAll(indicesOfIncorrectEDUNotes);		  
					// 3. Increment i to skip the upper equal duration unison note, which is already dealt with
					i++;
				}
			}
		}

		// 3. Set assignmentErrors
		// Determine the general, high-level information
		List<Integer> general = new ArrayList<Integer>();
		general.add(NUM_NOTES, numberOfNotes);
		general.add(NUM_INCORRECT, indicesOfIncorr.size());
		general.add(NUM_OVERLOOKED, indicesOfOverl.size());
		general.add(NUM_SUPERFLUOUS, indicesOfSuperfl.size());
		general.add(NUM_HALF, indicesOfHalf.size());
		// Set and return assignmentErrors
		assignmentErrors.add(GENERAL_VOICE, general); 
		assignmentErrors.add(INCORRECT_VOICE, indicesOfIncorr);
		assignmentErrors.add(OVERLOOKED_VOICE, indicesOfOverl);
		assignmentErrors.add(SUPERFLUOUS_VOICE, indicesOfSuperfl);
		assignmentErrors.add(HALF_VOICE, indicesOfHalf);
		// When modelling duration
		if (groundTruthDurationLabels != null) {
			// Determine the general, high-level information
			List<Integer> generalDur = new ArrayList<Integer>();
			generalDur.add(NUM_NOTES, numberOfNotes);
			generalDur.add(NUM_INCORRECT, indicesOfIncorrDur.size());
			generalDur.add(NUM_OVERLOOKED, indicesOfOverlDur.size());
			assignmentErrors.add(GENERAL_DUR, generalDur);
			assignmentErrors.add(INCORRECT_DUR, indicesOfIncorrDur);
			assignmentErrors.add(OVERLOOKED_DUR, indicesOfOverlDur);
		}
		return assignmentErrors;
	}


	/**
	 * Calculates the accuracy, i.e., the number of note assigned correctly divided by
	 * the total number of notes. In the tablature case, both numerator and denominator
	 * are multiplied by two to get rid of .5 values resulting from SNUs. 
	 * 
	 * @param assigErrs
	 * @param isTabCase
	 * @param isDur
	 * @return
	 */
	// TESTED for both tablature- and non-tablature case
	public static ErrorFraction calculateAccuracy(List<List<Integer>> assigErrs, 
		boolean isTabCase, boolean isDur) {
		
		int numNotes = assigErrs.get(GENERAL_VOICE).get(NUM_NOTES);
		double misass = getMisassignments(assigErrs)[0];
		if (isDur) {
			misass = getMisassignments(assigErrs)[1];
		}
		
		int num, den;
		if (isTabCase) {
			num = (2*numNotes) - (int)(2*misass);
			den = (2*numNotes);
		}
		else {
			num = numNotes - (int) misass;
			den = numNotes;
		}
		return new ErrorFraction(num, den);
	}


	/**
	 * Returns the classification error, i.e., the number of notes assigned incorrectly divided by the total 
	 * number of notes, as a double. 
	 * 
	 * @param assignmentErrors
	 * @param errorMeasurementIsStrict
	 * @return
	 * 
	 */
	// TESTED for both tablature- and non-tablature case (same); deals with EDU correctly (gets
	// argument assignmentErrors from calculateAssignmentErrors(), which deals with EDU correctly)
	public static double calculateClassificationError(List<List<Integer>> assignmentErrors) {
		int numNotes = assignmentErrors.get(GENERAL_VOICE).get(NUM_NOTES); 
		double numberMisassigned = getMisassignments(assignmentErrors)[0];
		return numberMisassigned / numNotes;
	}


	/**
	 * Calculates the number of notes assigned to an incorrect voice, and, if 
	 * applicable, to an incorrect duration.
	 * 
	 * @param assignmentErrors
	 * @return A double[] containing <br>
	 *         as element 0: the number of notes assigned to an incorrect voice <br>
	 *         as element 1: the number of notes assigned to an incorrect duration
	 *                       (if applicable; else -1.0 is returned)
	 */
	// TESTED for both tablature- and non-tablature case
	static double[] getMisassignments(List<List<Integer>> assignmentErrors) {
		// Voice
		double incorrect = assignmentErrors.get(GENERAL_VOICE).get(NUM_INCORRECT);
		double overlooked = assignmentErrors.get(GENERAL_VOICE).get(NUM_OVERLOOKED); 
		double superfluous = assignmentErrors.get(GENERAL_VOICE).get(NUM_SUPERFLUOUS);
		double half = assignmentErrors.get(GENERAL_VOICE).get(NUM_HALF);
		double misassVoice = 
			incorrect + (weightOfSpecialErrors * (overlooked + superfluous + half));

		// Duration
		double misassDur = -1.0;
		if (assignmentErrors.size() > GENERAL_DUR) {
			int incorrectDur = assignmentErrors.get(GENERAL_DUR).get(NUM_INCORRECT);
			int overlookedDur = assignmentErrors.get(GENERAL_DUR).get(NUM_OVERLOOKED);
			misassDur = incorrectDur + (weightOfSpecialErrors * overlookedDur);
		}

		return new double[]{misassVoice, misassDur};
	}


	/**
	 * Calculates the (weighted) average precision, recall, and F1-score over all 
	 * predicted voices.
	 * 
	 * @param allPredictedVoices
	 * @param groundTruthVoiceLabels
	 * @param EDUInfo
	 * @param highestNumVoices
	 * @return An ErrorFraction[][] containing <br>
	 * 		   as element 0: the weighted average prc [0], rcl [1], and F1-score [2] 
	 *                       (NB: the latter is currently set to <code>null</code>) <br>
	 *         as element 1: the average prc [0], rcl [1], and F1-score [2]
	 */
	// TESTED for both tablature- and non-tablature case (through getPositivesAndNegativesPerVoice());
	//  deals with EDU correctly
	public static ErrorFraction[][] calculateAvgPrecisionRecallF1(List<List<Integer>> allPredictedVoices, 
		List<List<Double>> groundTruthVoiceLabels, List<Integer[]> EDUInfo, int highestNumVoices) {

		List<List<List<Integer>>> errorsPerVoice = getPositivesAndNegativesPerVoice(allPredictedVoices, 
			groundTruthVoiceLabels, EDUInfo, highestNumVoices);

		ErrorFraction[][] prcRclF1 = new ErrorFraction[2][3];
		
		List<ErrorFraction> precisions = new ArrayList<ErrorFraction>();
		List<ErrorFraction> recalls = new ArrayList<ErrorFraction>();
		
		Rational sumPrc = new Rational(0, 1);
		Rational sumRcl = new Rational(0, 1);
		Rational sumF1 = new Rational(0, 1);

		for (int i = 0; i < highestNumVoices; i++) {
			List<List<Integer>> currentErrorsPerVoice = errorsPerVoice.get(i);
			int truePos = currentErrorsPerVoice.get(0).size();
			int falsePos = currentErrorsPerVoice.get(1).size();
			int falseNeg = currentErrorsPerVoice.get(2).size();

			ErrorFraction[] curr = calculatePrecisionRecallF1(truePos, falsePos, falseNeg);

			precisions.add(curr[0]);
			recalls.add(curr[1]);
			
			sumPrc = sumPrc.add(curr[0].toRational());
			sumRcl = sumRcl.add(curr[1].toRational());
			sumF1 = sumF1.add(curr[2].toRational());
		}

		Rational avgPrc = sumPrc.div(highestNumVoices);
		Rational avgRcl = sumRcl.div(highestNumVoices);
		Rational avgF1 = sumF1.div(highestNumVoices);
		
		prcRclF1[0] = new ErrorFraction[]{ErrorFraction.getWeightedAverage(precisions),
			ErrorFraction.getWeightedAverage(recalls), null};
		prcRclF1[1] = new ErrorFraction[]{
			new ErrorFraction(avgPrc.getNumer(), avgPrc.getDenom()),
			new ErrorFraction(avgRcl.getNumer(), avgRcl.getDenom()), 
			new ErrorFraction(avgF1.getNumer(), avgF1.getDenom())
		};
		
		return prcRclF1;		
	}
	
	
	/**
	 * Returns, per voice, a List<List<> containing:
	 * as element 0: the indices of the notes that are true positives (the notes that were assigned 
	 * 				 to the voice and indeed belong to it)
	 * at element 1: the indices of the notes that are false positives (the notes that were assigned
	 *               to the voice but do not belong to it)
	 * at element 2: the indices of the notes that are false negatives (the notes that belong to the 
	 *               voice but were not assigned to it)
	 * 
	 * @param allPredictedVoices
	 * @param groundTruthVoiceLabels
	 * @param equalDurationUnisonsInfo
	 * @param highestNumberOfVoices
	 * @return
	 */
	// TESTED for both tablature- and non-tablature case; deals with EDU correctly
	static List<List<List<Integer>>> getPositivesAndNegativesPerVoice(List<List<Integer>> allPredictedVoices, 
		List<List<Double>> groundTruthVoiceLabels, List<Integer[]> equalDurationUnisonsInfo,
		int highestNumberOfVoices) {

		final int TRUE_POS = 0;
		final int FALSE_POS = 1;
		final int FALSE_NEG = 2;

		// Make positivesAndNegatives, a List<List<List<Integer>>> the size of highestNumberOfVoices
		List<List<List<Integer>>> positivesAndNegatives = new ArrayList<List<List<Integer>>>();
		for (int i = 0; i < highestNumberOfVoices; i++) {
			positivesAndNegatives.add(new ArrayList<List<Integer>>());
		}

		// Get the notes per voice
		// a. Get the notes per voice as in the ground truth
		List<List<Integer>> notesPerVoiceActual = Transcription.listNotesPerVoice(groundTruthVoiceLabels);
		// b. Get the notes per voice as predicted
		// First turn allPredictedVoices into a List of voice labels
		List<List<Double>> predictedVoiceLabels = new ArrayList<List<Double>>();
		for (int i = 0; i < allPredictedVoices.size(); i++) {
			List<Double> currentPredictedVoiceLabel = DataConverter.convertIntoVoiceLabel(allPredictedVoices.get(i));
			predictedVoiceLabels.add(currentPredictedVoiceLabel);
		}
		List<List<Integer>> notesPerVoicePredicted = Transcription.listNotesPerVoice(predictedVoiceLabels);

		// For each voice
		for (int i = 0; i < highestNumberOfVoices; i++) {
			int currentVoice = i;
			List<Integer> currentNotesPerVoiceActual = notesPerVoiceActual.get(currentVoice);
			List<Integer> currentNotesPerVoicePredicted = notesPerVoicePredicted.get(currentVoice);
			// 1. Determine the true positives: the notes that were assigned to a voice and indeed belong to it
			// (i.e., the elements shared by notesPerVoiceActual and notesPerVoicePredicted)
			List<Integer> currentTruePositives = new ArrayList<Integer>();
			for (int c : currentNotesPerVoiceActual) {
				// a. In the tablature case or in the non-tablature case if the note is not part of an EDU
				if (equalDurationUnisonsInfo == null || (equalDurationUnisonsInfo != null && 
					equalDurationUnisonsInfo.get(c) == null)) {
					if (currentNotesPerVoicePredicted.contains(c)) {
						currentTruePositives.add(c);
					}
				}
			}
			// 2. Determine the false positives: the notes that were assigned to a voice but do not belong to it 
			// (i.e., the elements that are in notesPerVoicePredicted but not in notesPerVoiceActual)
			List<Integer> currentFalsePositives = new ArrayList<Integer>();
			for (int c : currentNotesPerVoicePredicted) {
				// a. In the tablature case or in the non-tablature case if the note is not part of an EDU
				if (equalDurationUnisonsInfo == null || (equalDurationUnisonsInfo != null && 
					equalDurationUnisonsInfo.get(c) == null)) {
					if (!currentNotesPerVoiceActual.contains(c)) {
						currentFalsePositives.add(c);
					}
				}
			}
			// 3. Determine the false negatives: the notes that belong to a voice but were not assigned to it 
			// (i.e., the elements that are in notesPerVoiceActual but not in notesPerVoicePredicted)
			List<Integer> currentFalseNegatives = new ArrayList<Integer>();
			for (int c : currentNotesPerVoiceActual) {
				// a. In the tablature case or in the non-tablature case if the note is not part of an EDU
				if (equalDurationUnisonsInfo == null || (equalDurationUnisonsInfo != null && 
					equalDurationUnisonsInfo.get(c) == null)) {
					if (!currentNotesPerVoicePredicted.contains(c)) {
						currentFalseNegatives.add(c);
					}
				}
			}

			// 4. Add the Lists to the appropriate element of positivesAndNegatives
			positivesAndNegatives.get(currentVoice).add(TRUE_POS, currentTruePositives);
			positivesAndNegatives.get(currentVoice).add(FALSE_POS, currentFalsePositives);
			positivesAndNegatives.get(currentVoice).add(FALSE_NEG, currentFalseNegatives);
		}

		// In the non-tablature case: handle EDU notes
		if (equalDurationUnisonsInfo != null) {
			// For each note
			for (int i = 0; i < equalDurationUnisonsInfo.size(); i++) {
				// If the note is a lower EDUnote (the i++ increment at the end of the for-loop skips upper EDUnotes)
				if (equalDurationUnisonsInfo.get(i) != null) {
					// 1. Determine the indices of any incorrect EDunotes
//					List<Double> allowedVoicesLabel = equalDurationUnisonsInfo.get(i);
//					List<Integer> allowedVoices = dataConverter.convertIntoListOfVoices(allowedVoicesLabel);
					Integer[] eDUInfo = equalDurationUnisonsInfo.get(i);
					List<Integer> allowedVoices = Arrays.asList(new Integer[]{eDUInfo[1], eDUInfo[0]}); 
					int predictedVoiceLower = allPredictedVoices.get(i).get(0); // get(0) allowed because CoD not possible
					int predictedVoiceUpper = allPredictedVoices.get(i + 1).get(0); // get(0) allowed because CoD not possible
					List<Integer> indicesOfIncorrectEDUNotes = getIndicesOfIncorrectEDUNotes(predictedVoiceLower, 
						predictedVoiceUpper, allowedVoices, i);
					// 2. Add i and i+1 to the correct List in positivesAndNegatives
					// a. If indicesOfIncorrectEDUNotes is empty, the notes at indices i and i+1 are TP in their 
					// predicted voices
					if (indicesOfIncorrectEDUNotes.size() == 0) {
						positivesAndNegatives.get(predictedVoiceLower).get(TRUE_POS).add(i);
						positivesAndNegatives.get(predictedVoiceUpper).get(TRUE_POS).add(i + 1);
					}
					// b. If indicesOfIncorrectEDUNotes contains i, the note at index i is a FP in its predicted voice and 
					// a FN in its actual voice (which is the EDU complement of the voice predicted for i+1), and the note
					// at index i+1 is a TP in its predicted voice
					if (indicesOfIncorrectEDUNotes.size() == 1 && indicesOfIncorrectEDUNotes.contains(i)) {
						positivesAndNegatives.get(predictedVoiceUpper).get(TRUE_POS).add(i + 1);
						positivesAndNegatives.get(predictedVoiceLower).get(FALSE_POS).add(i);
						// Determine the EDU complement of the voice predicted for the upper EDU note
						int eDUComplement = allowedVoices.get(0);
						if (allowedVoices.get(0) == predictedVoiceUpper) {
							eDUComplement = allowedVoices.get(1);
						}
						positivesAndNegatives.get(eDUComplement).get(FALSE_NEG).add(i);	
					}
					// c. If indicesOfIncorrectEDUNotes contains i+1, the note at index i is a TP in its predicted voice,
					// and the note at index i+1 a FP in its predicted voice and a FN in its actual voice (which is the 
					// EDU complement of the voice predicted for i)
					if (indicesOfIncorrectEDUNotes.size() == 1 && indicesOfIncorrectEDUNotes.contains(i + 1)) {
						positivesAndNegatives.get(predictedVoiceLower).get(TRUE_POS).add(i);
						positivesAndNegatives.get(predictedVoiceUpper).get(FALSE_POS).add(i + 1);
						// Determine the EDU complement of the voice predicted for the upper EDU note
						int eDUComplement = allowedVoices.get(0);
						if (allowedVoices.get(0) == predictedVoiceLower) {
							eDUComplement = allowedVoices.get(1);
						}
						positivesAndNegatives.get(eDUComplement).get(FALSE_NEG).add(i + 1);	
					}
					// d. If indicesOfIncorrectEDUNotes contains i and i+1, both notes are FP in their predicted voices 
					// and FN in their actual voices (the allowed voices, ordered with the lowest first (as in the noteSequence)) 
					if (indicesOfIncorrectEDUNotes.size() == 2) {
						positivesAndNegatives.get(predictedVoiceLower).get(FALSE_POS).add(i);
						positivesAndNegatives.get(predictedVoiceUpper).get(FALSE_POS).add(i + 1);
						positivesAndNegatives.get(allowedVoices.get(1)).get(FALSE_NEG).add(i);
						positivesAndNegatives.get(allowedVoices.get(0)).get(FALSE_NEG).add(i + 1);
					}
					// Increment i to skip the upper equal duration unison note, which is already dealt with
					i++;
				}
			}	
			// Because indices of EDUnotes are added to the end of the lists, these need to be sorted numerically
			for (List<List<Integer>> voice : positivesAndNegatives) {
				for (List<Integer> values : voice) {
					Collections.sort(values);
				}
			}
		}
		return positivesAndNegatives;
	}


	/**
	 * Calculates the precision, recall, and F1-score given the true positives (tP), 
	 * false positives (fP), and false negatives (fN).
	 * 
	 * @param truePos
	 * @param falsePos
	 * @param falseNeg
	 * @return An ErrorFraction[] containing <br>
	 *   	   as element 0: the precision (tP / (tP + fP)) <br>
	 *         as element 1: the recall (tP / (tP + fN)) <br>
	 *         as element 2: the F1 score (2PR / (P + R)) (as a reduced ErrorFraction)
	 *         
	 */
	// TESTED for both tablature- and non-tablature case (through getPositivesAndNegativesPerVoice());
	// deals with EDU correctly (does not apply to method directly)
	static ErrorFraction[] calculatePrecisionRecallF1(int truePos, int falsePos, int falseNeg) {
		ErrorFraction prc = new ErrorFraction(truePos, truePos + falsePos);
		ErrorFraction rcl = new ErrorFraction(truePos, truePos + falseNeg);

		Rational numF1 = (prc.toRational()).mul(rcl.toRational()).mul(2);
		Rational denF1 = prc.toRational().add(rcl.toRational());
		Rational f1AsRational = numF1.div(denF1);
		ErrorFraction f1 = new ErrorFraction(f1AsRational.getNumer(), f1AsRational.getDenom());

		return new ErrorFraction[]{prc, rcl, f1};
	}

	
	/**
	 * Calculates the (weighted) average soundness and completeness over all predicted
	 * voices. These metrics are defined by Kirlin & Utgoff (2005, section 5.2) as follows: <br> 
	 *  
	 * "soundness is calculated by finding, for each predicted voice V, the percentage of 
	 * adjacent notes n1, n2 E V that also satisfy g(n1) = g(n2) [where g is the
	 * ground truth voice of a note]." <br>
	 * "[completeness] is calculated by finding, for each ground-truth voice V, the percentage of
	 * adjacent notes n1, n2 E V that also satisfy f(n1) = f(n2) [where f is the 
	 * predicted voice of a note]." 
	 *    
	 * @param allPredictedVoices
	 * @param groundTruthVoiceLabels
	 * @param equalDurationUnisonsInfo
	 * @param highestNumberOfVoices
	 * @return An ErrorFraction[][] containing <br>
	 *         as element 0: the weighted average soundness [0] and completeness [1] <br>
	 *         as element 1: the average soundness [0] and completeness [1]        
	 */
	// TESTED for both tablature- and non-tablature case; deals with EDU correctly
	public static ErrorFraction[][] calculateAvgSoundnessAndCompleteness(List<List<Integer>> allPredictedVoices, 
		List<List<Double>> groundTruthVoiceLabels, List<Integer[]> equalDurationUnisonsInfo,
		int highestNumberOfVoices) {
		ErrorFraction[][] soundnessAndCompleteness = new ErrorFraction[2][2];
	
		// 1. Get the notes per voice
		// Get the notes per voice as in the ground truth
		List<List<Integer>> notesPerVoiceActual = Transcription.listNotesPerVoice(groundTruthVoiceLabels);
		// Get the notes per voice as predicted
		List<List<Double>> predictedVoiceLabels = new ArrayList<List<Double>>();
		for (int i = 0; i < allPredictedVoices.size(); i++) {
			List<Double> currentPredictedVoiceLabel = DataConverter.convertIntoVoiceLabel(allPredictedVoices.get(i));
			predictedVoiceLabels.add(currentPredictedVoiceLabel);
		}
		List<List<Integer>> notesPerVoicePredicted = Transcription.listNotesPerVoice(predictedVoiceLabels);

		// 2. In the non-tablature case: swap back all swapped EDUnotes
		if (equalDurationUnisonsInfo != null) {
			notesPerVoicePredicted = swapEDUnotes(notesPerVoicePredicted, groundTruthVoiceLabels, equalDurationUnisonsInfo);
		}

		// 3. For each voice v: calculate soundness and completeness
		List<ErrorFraction> soundnesses = new ArrayList<ErrorFraction>();
		List<ErrorFraction> completenesses = new ArrayList<ErrorFraction>();
		Rational soundnessSum = Rational.ZERO;
		Rational completenessSum = Rational.ZERO;
		for (int v = 0; v < highestNumberOfVoices; v++) {				
			// Get the notes that were predicted for the current voice and the actual notes in that voice
			List<Integer> notesPredictedForCurrentVoice = notesPerVoicePredicted.get(v);
			List<Integer> notesActualForCurrentVoice = notesPerVoiceActual.get(v);
//			if (v == 1) {
//				System.out.println(notesPredictedForCurrentVoice); fwd-bwd soundness
//			}

			// 1. Soundness
			// For each adjacent note pair in the predicted voice
			int numberOfAdjacentNotePairs = 0;
			if (notesPredictedForCurrentVoice.size() > 1) {
				numberOfAdjacentNotePairs = notesPredictedForCurrentVoice.size() - 1;
			}
			int bothNotesOfPairAreInActualVoice = 0; // remains 0 if numberOfAdjacentNotePairs == 0
			for (int j = 0; j < numberOfAdjacentNotePairs; j++) {
				int leftNote = notesPredictedForCurrentVoice.get(j);
				int rightNote = notesPredictedForCurrentVoice.get(j + 1);
				// For each actual voice
				for (int k = 0; k < highestNumberOfVoices; k++) {
					List<Integer> currentNotesPerVoiceActual = notesPerVoiceActual.get(k);
					// Are both notes in the same actual voice? 
					if (currentNotesPerVoiceActual.contains(leftNote) && currentNotesPerVoiceActual.contains(rightNote)) {
						bothNotesOfPairAreInActualVoice++;
//						System.out.println(leftNote + " " + rightNote); // fwd-bwd soundness
						break;
					}
				}
			}			
			if (numberOfAdjacentNotePairs == 0) {  
				soundnessSum = soundnessSum.add(Rational.ZERO);
				soundnesses.add(new ErrorFraction(0, 0));
			}
			else {
				soundnessSum = soundnessSum.add(new Rational(bothNotesOfPairAreInActualVoice, numberOfAdjacentNotePairs));
				soundnesses.add(new ErrorFraction(bothNotesOfPairAreInActualVoice, numberOfAdjacentNotePairs));
			}

//			System.out.println(soundnesses.get(v)); // fwd-bwd soundness

			// 2. Completeness
			// For each adjacent note pair in the actual voice (such pairs only exist if it contains more than one note)
			numberOfAdjacentNotePairs = 0;
			if (notesActualForCurrentVoice.size() > 1) {
				numberOfAdjacentNotePairs = notesActualForCurrentVoice.size() - 1;
			}			
			int bothNotesOfPairAreInPredictedVoice = 0; // remains 0 if numberOfAdjacentNotePairs == 0
			for (int j = 0; j < numberOfAdjacentNotePairs; j++) {
				int leftNote = notesActualForCurrentVoice.get(j);
				int rightNote = notesActualForCurrentVoice.get(j + 1);
				// For each predicted voice
				for (int k = 0; k < highestNumberOfVoices; k++) {
					List<Integer> currentNotesPerVoicePredicted = notesPerVoicePredicted.get(k);
					// Are both notes in the same predicted voice? 
					if (currentNotesPerVoicePredicted.contains(leftNote) && currentNotesPerVoicePredicted.contains(rightNote)) {
						bothNotesOfPairAreInPredictedVoice++;
						break;
					}
				}
			}
			if (numberOfAdjacentNotePairs == 0) {
				completenessSum = completenessSum.add(Rational.ZERO);
				completenesses.add(new ErrorFraction(0, 0));
			}
			else {
				completenessSum = completenessSum.add(new Rational(bothNotesOfPairAreInPredictedVoice, numberOfAdjacentNotePairs));
				completenesses.add(new ErrorFraction(bothNotesOfPairAreInPredictedVoice, numberOfAdjacentNotePairs));
			}
		}

		// 4. Determine the (weighted) averages per voice and set and return soundnessAndCompleteness
		// a. Wtd avg
		soundnessAndCompleteness[0][0] = ErrorFraction.getWeightedAverage(soundnesses);
		soundnessAndCompleteness[0][1] = ErrorFraction.getWeightedAverage(completenesses);
		// b. Avg
		Rational avgSoundness = soundnessSum.div(highestNumberOfVoices);
		Rational avgCompleteness = completenessSum.div(highestNumberOfVoices);
		soundnessAndCompleteness[1][0] = new ErrorFraction(avgSoundness.getNumer(), avgSoundness.getDenom());
		soundnessAndCompleteness[1][1] = new ErrorFraction(avgCompleteness.getNumer(), avgCompleteness.getDenom());

//		System.out.println("S = " + soundnessAndCompleteness[1][0]); fwd-bwd soundness
//		System.out.println("C = " + soundnessAndCompleteness[1][1]); fwd-bwd soundness
		
		return soundnessAndCompleteness;
	}


	/**
	 * Calculates, for the given predicted voices, the average voice consistency AVC (in %).
	 * AVC is defined by Chew & Wu (2005, p. 15), and <br>
	 * 
	 * "measures how well the notes in the piece have been properly assigned to their appropriate
	 * voices. This quantity measures, on average, the proportion of notes from the same voice that have been assigned
	 * by the algorithm to the same voice." 
	 * 
	 * @param allPredictedVoices
	 * @param groundTruthVoiceLabels
	 * @param equalDurationUnisonsInfo
	 * @param highestNumberOfVoices
	 * @return
	 */
	// TESTED; deals with EDU correctly
	public static double calculateAVC(List<List<Integer>> allPredictedVoices, 
		List<List<Double>> groundTruthVoiceLabels, List<Integer[]> equalDurationUnisonsInfo, 
		int highestNumberOfVoices) {
		
//		List<Rational> voiceConsistenciesForEachVoice = new ArrayList<Rational>();
		List<Double> voiceConsistenciesForEachVoiceDouble = new ArrayList<Double>();

		// 1. Get the notes per voice
		// Get the notes per voice as in the ground truth
		List<List<Integer>> notesPerVoiceActual = Transcription.listNotesPerVoice(groundTruthVoiceLabels);
		// Get the notes per voice as predicted
		List<List<Double>> predictedVoiceLabels = new ArrayList<List<Double>>();
		for (int i = 0; i < allPredictedVoices.size(); i++) {
			List<Double> currentPredictedVoiceLabel = DataConverter.convertIntoVoiceLabel(allPredictedVoices.get(i));
			predictedVoiceLabels.add(currentPredictedVoiceLabel);
		}
		List<List<Integer>> notesPerVoicePredicted = Transcription.listNotesPerVoice(predictedVoiceLabels);

		// 2. In the non-tablature case: swap back all swapped EDUnotes
		if (equalDurationUnisonsInfo != null) {
			notesPerVoicePredicted = swapEDUnotes(notesPerVoicePredicted, groundTruthVoiceLabels, equalDurationUnisonsInfo);
		}

		// 3. For each voice v: calculate the voice consistency
		for (int v = 0; v < highestNumberOfVoices; v++) {
			List<Integer> notesPredictedForCurrentVoice = notesPerVoicePredicted.get(v);
			int max = 0;
			// For each possible voice u
			for (int u = 0; u < highestNumberOfVoices; u++) {
				// Determine how many notes in the current predicted voice have u as the actual voice 
				List<Integer> notesActualInCurrentVoice = notesPerVoiceActual.get(u);
				// For each note
				int notesWithUAsActualVoice = 0;
				for (int note : notesPredictedForCurrentVoice) {
					// If the current actual voice contains the note: increase notesWithUAsActualVoice 
					if (notesActualInCurrentVoice.contains(note)) {
						notesWithUAsActualVoice++;
					}	
				}
				// Reset max if necessary
				if (notesWithUAsActualVoice > max) {
					max = notesWithUAsActualVoice;
				}
			}
			// Calculate the voice consistency for the current predicted voice
//			Rational VC = new Rational(100*max, notesPredictedForCurrentVoice.size());
//			Rational VC = new Rational(max, notesPredictedForCurrentVoice.size());
//			voiceConsistenciesForEachVoice.add(VC);
			double VC = (100.0 / notesPredictedForCurrentVoice.size()) * max;
			voiceConsistenciesForEachVoiceDouble.add(VC);
		}

		// 4. Calculate the average voice consistency
//		Rational avcR = new Rational(1, highestNumberOfVoices).mul(ToolBox.sumListRational(voiceConsistenciesForEachVoice));
		double avc = (1.0/highestNumberOfVoices) * ToolBox.sumListDouble(voiceConsistenciesForEachVoiceDouble);
//		System.out.println("avc Rat = " + avcR.toDouble()*100 + " = " + avc);
//		System.out.println("avc dbl = " + avc);
//		System.out.println(avcR.toDouble()*100 == avc); 
		return avc;
	}


	/** 
	 * Calculates cross-entropy following the formula given in Pearce (2004), p. 369: <br><br>
	 * 
	 * H = (-1/j) * i=1_sum^j(log_2(p)) <br><br>
	 * which equals (see http://www.purplemath.com/modules/logrules5.htm) <br><br>
	 * H = (-1/j) * i=1_sum^j(log_e(p)/log_e(2)) <br><br>
	 *   
	 * where p is the probability of the note beloning to a voice, as given by the activation 
	 * values of the softmax function contained in the model output.
	 * 
	 * NB: sum(x/y) == sum(x)/y; thus, sum(log_e(p) / log_e(2)) == sum(log_e(p)) / log_e(2) 
	 * 
	 * @param outputs The network outputs (probabilities)
	 * @param labels The voice labels (binary vectors)
	 * @param dc Needed to convert each label into a voice class
	 * @return A double, containing </br>
	 *         as element 0: the CE </br>
	 *         as element 1: the summed log_e(p) over all j </br>
	 *         as element 2: log_e(2), by which element 1 must be divided to get the
	 *                       numerator of the CE </br>              
	 *         as element 3: the denominator of the CE, i.e., j </br>
	 */
	// TESTED for both tablature- and non-tablature case
	public static double[] calculateCrossEntropy(List<double[]> outputs, List<List<Double>> labels) {
		double[] H = new double[4];
		double sum = 0.0;
		int j = outputs.size();
		for (int i = 0; i < j; i++) {
			double[] currOutp = outputs.get(i);
			List<Double> currLabel = labels.get(i);
//			int actualVoice = dc.convertIntoListOfVoices(currLabel).get(0); 
			List<Integer> actualVoices = DataConverter.convertIntoListOfVoices(currLabel);
			for (int v : actualVoices) {
				double p = currOutp[v];
				if (actualVoices.size() == 2) {
					p /= 2;
				}
				// +1.0E-6
				if (p == 0) {
					p += 0.000001; 	
				}
				sum += Math.log(p);
			}
		}
		H[0] = (-1.0/j) * (sum/CRE_SUM_DIV);
		H[1] = sum;
		H[2] = CRE_SUM_DIV;
		H[3] = j;
		return H;
	}


	/**
	 * Determines whether the given predicted voices are correct compared to the given 
	 * ground truth voices, i.e., whether they meet one of the following criteria 
	 * (see also calculateAssignmentErrors()):
	 * (1) predictedVoices and actualVoices contain one element: correct if they are the same
	 * (2) predictedVoices contains one element, actualVoices two: incorrect 
	 * (3) predictedVoices contains two elements, actualVoices one: incorrect
	 * (4) predictedVoices and actualVoices contain two elements: correct if the elements
	 *     are the same
	 * 
	 * @param errorMeasurementIsStrict
	 * @param predictedVoices
	 * @param groundTruthVoices
	 * @return
	 */
	// TESTED; applies to non-EDU case only so EDU not relevant
	// TODO this is an auxiliary method for EvaluationManager.updateConflictIndices() - move?
	public static boolean assertCorrectness(List<Integer> predictedVoices, 
		List<Integer> groundTruthVoices) {
		boolean predictedVoicesAreCorrect = false;

		// 1. If predictedVoices contains one element and actualVoices one
		if (predictedVoices.size() == 1 && groundTruthVoices.size() == 1) {
			// True if the elements are the same 
			if (predictedVoices.get(0) == groundTruthVoices.get(0)) {
				predictedVoicesAreCorrect = true;   
			} 
		}
		// 2. If predictedVoices contains one element and actualVoices two (i.e., CoD overlooked) 
		else if (predictedVoices.size() == 1 && groundTruthVoices.size() == 2) {
			// Always false
		}
		// 3. If predictedVoices contains two elements and actualVoices one (i.e., superfluous)
		else if (predictedVoices.size() == 2 && groundTruthVoices.size() == 1) {
			// Always false
		}
		// 4. If predictedVoices contains two elements and actualVoices two (i.e., both are a CoD) 
		else if (predictedVoices.size() == 2 && groundTruthVoices.size() == 2) {
			if (predictedVoices.contains(groundTruthVoices.get(0)) && 
				predictedVoices.contains(groundTruthVoices.get(1))) {
				predictedVoicesAreCorrect = true;
			}				
		}	  
		return predictedVoicesAreCorrect;
	}


	/**
	 * Determines for both notes of an equal duration unison (EDU) pair whether the given 
	 * predicted voices have been adapted correctly after a conflict has been detected.
	 *  
	 * NB: Non-tablature case only. 
	 *   
	 * @param predictedAndAdaptedVoices Consists of two Integer[]s, the first of which gives
	 *        the predicted voices for the lower [0] and the upper [1] unison note, and the 
	 *        second the adapted voices for the lower [0] and the upper [1] unison note.
	 * @param allowedVoices
	 * @param index
	 * @return A boolean[][], containing <br>
	 *         as element 0: whether the lower [0] and upper [1] EDU note have been predicted
	 *                       correctly <br>
	 *         as element 1: whether the lower [0] and upper [1] EDU note have been adapted
	 *                       correctly
	 */ 
	// TESTED; deals with EDU currectly
	// TODO this is an auxiliary method for TestManager.getConflictInfo() - move?
	public static boolean[][] assertCorrectnessEDUNotes(List<Integer[]> predictedAndAdaptedVoices, 
		List<Integer> allowedVoices/*, int index*/) {
		boolean[][] assignmentsOfPredAndAdapVoices = new boolean[2][2];

		// Determine the allowed voices
		int voiceA = allowedVoices.get(0);
		int voiceB = allowedVoices.get(1);

		// Determine first for the predicted voices and then for the adapted voices whether 
		// the lower and upper notes have been assigned to the correct voices, and set 
		// assignmentsOfPredAndAdapVoices accordingly
		for (int i = 0; i < predictedAndAdaptedVoices.size(); i++) {
			int lowerVoice = predictedAndAdaptedVoices.get(i)[0];
			int upperVoice = predictedAndAdaptedVoices.get(i)[1];

			// a. If both voices are correct
			if ((lowerVoice == voiceA && upperVoice == voiceB) || (lowerVoice == voiceB && upperVoice == voiceA)) {
				assignmentsOfPredAndAdapVoices[i] = new boolean[]{true, true};
			}
			// b. If the voice for the lower note is correct and that for the upper incorrect
			if ((lowerVoice == voiceA || lowerVoice == voiceB) && ((upperVoice != voiceA) && (upperVoice != voiceB))) {
				assignmentsOfPredAndAdapVoices[i] = new boolean[]{true, false};
			}
			// c. If the voice for the lower note is incorrect and that for the upper correct
			if ((lowerVoice != voiceA && lowerVoice != voiceB) && (upperVoice == voiceA || upperVoice == voiceB)) {
				assignmentsOfPredAndAdapVoices[i] = new boolean[]{false, true};
			}
			// d. If both voices are incorrect
			if ((lowerVoice != voiceA && lowerVoice != voiceB) && (upperVoice != voiceA && upperVoice != voiceB)) {
				assignmentsOfPredAndAdapVoices[i] = new boolean[]{false, false};
			}
			// e. If both voices are correct but they are the same voice
			if ((lowerVoice == voiceA && upperVoice == voiceA) || (lowerVoice == voiceB && upperVoice == voiceB)) {
				assignmentsOfPredAndAdapVoices[i] = new boolean[]{true, false};
			}
		}
		return assignmentsOfPredAndAdapVoices;
	}


	/**
	 * Given two predicted voices for a lower and an upper EDUnote, determines which of the notes (if any) have 
	 * been assigned incorrectly. Returns the index or indices of the notes assigned incorrectly.
	 * Let the allowed voices be A and B, and any of the other voices O. There are three scenarios: <br>
	 * (1) If predLower == A: correct <br>  
	 *       if predUpper == A: incorrect / if predUpper == B: correct / if predUpper == O: incorrect <br>
	 * (2) If predLower == B: correct <br>
	 *       if predUpper == A: correct / if predUpper == B: incorrect / if predUpper == O: incorrect <br>
	 * (3) If predLower == O: incorrect <br>
	 *       if predUpper == A: correct / if predUpper == B: correct / if predUpper == O: incorrect <br>
	 * Similarly, <br> 
	 * (4) If predUpper == A: correct <br>    						              
	 *       if predLower == A: incorrect / if predLower == B: correct / if predLower == O: incorrect <br>
	 * (5) If predUpper == B: correct <br>
	 *       if predLower == A: correct / if predLower == B: incorrect / if predLower == O: incorrect <br>
	 * (6) If predUpper == O: incorrect <br>
	 *       if predLower == A: correct / if predLower == B: correct / if predLower == O: incorrect <br>
	 * This covers all scenarios in (1)-(3). Note that if both predicted voices are A or both are B, 
	 * the choice which one is the incorrect one is arbitrary.	
	 * 
	 * @param predictedVoiceLower
	 * @param predictedVoiceUpper
	 * @param allowedVoices
	 * @param noteIndex
	 * @return
	 */
	// TODO TEST
	static List<Integer> getIndicesOfIncorrectEDUNotes(int predictedVoiceLower, int predictedVoiceUpper, 
		List<Integer> allowedVoices, int noteIndex) {
		List<Integer> indicesOfIncorrectAssignments = new ArrayList<Integer>();

		int voiceA = allowedVoices.get(0);
		int voiceB = allowedVoices.get(1);
		// 3. Determine the indices of the incorrect notes 
		// Let the allowed voices be A and B, and any of the other voices O. There are three scenarios:
		// (1) If predicted lower == A: correct    						              
		//     a. If predicted upper == A: incorrect
		//     b. If predicted upper == B: correct
		//     c. If predicted upper == O: incorrect
		if (predictedVoiceLower == voiceA) {
			if (predictedVoiceUpper == voiceA) {
				indicesOfIncorrectAssignments.add(noteIndex + 1);
			}
			if (predictedVoiceUpper != voiceA && predictedVoiceUpper != voiceB ) {
				indicesOfIncorrectAssignments.add(noteIndex + 1);
			}
		}
		// (2) If predicted lower == B: correct
		//     a. If predicted upper == A: correct
		//     b. If predicted upper == B: incorrect
		//     c. If predicted upper == O: incorrect
		if (predictedVoiceLower == voiceB) {
			if (predictedVoiceUpper == voiceB) {
				indicesOfIncorrectAssignments.add(noteIndex + 1);
			}
			if (predictedVoiceUpper != voiceA && predictedVoiceUpper != voiceB ) {
				indicesOfIncorrectAssignments.add(noteIndex + 1);
			}
		}
		// (3) If predicted lower == O: incorrect
		//     a. If predicted upper == A: correct
		//     b. If predicted upper == B: correct
		//     c. If predicted upper == O: incorrect
		if (predictedVoiceLower != voiceA && predictedVoiceLower != voiceB) {
			indicesOfIncorrectAssignments.add(noteIndex);
			if (predictedVoiceUpper != voiceA && predictedVoiceUpper != voiceB ) {
				indicesOfIncorrectAssignments.add(noteIndex + 1);
			}
		}
		// Similarly, 
		// (4) If predicted upper == A: correct    						              
		//     a. If predicted lower == A: incorrect
		//     b. If predicted lower == B: correct
		//     c. If predicted lower == O: incorrect
		// (5) If predicted upper == B: correct
		//     a. If predicted lower == A: correct
		//     b. If predicted lower == B: incorrect
		//     c. If predicted lower == O: incorrect
		// (6) If predicted upper == O: incorrect
		//     a. If predicted lower == A: correct
		//     b. If predicted lower == B: correct
		//     c. If predicted lower == O: incorrect
		// This covers all scenarios in (1)-(3). Note that if both predicted voices are A or 
		// both are B, the choice which one is the incorrect one is arbitrary.	

		return indicesOfIncorrectAssignments;
	}


	/**
	 * Swaps the indices for the notes of each EDUnote pair whose voices have been swapped with respect to
	 * the ground truth in the given list and returns that list.   
	 * 
	 * Non-tablature case only.
	 * 
	 * @param notesPerVoicePredicted
	 * @param groundTruthVoiceLabels
	 * @param equalDurationUnisonsInfo
	 * @return
	 */
	// TESTED
	static List<List<Integer>> swapEDUnotes(List<List<Integer>> notesPerVoicePredicted, List<List<Double>>  groundTruthVoiceLabels,
		List<Integer[]> equalDurationUnisonsInfo) {
		// For each predicted voice i
		for (int i = 0; i < notesPerVoicePredicted.size(); i++) {
			List<Integer> indicesCurrentPredictedVoice = notesPerVoicePredicted.get(i); 
			// For each note at index j
			for (int j = 0; j < indicesCurrentPredictedVoice.size(); j++) {
				int currentIndex = indicesCurrentPredictedVoice.get(j);
				// Check whether the note at currentIndex is an EDUnote
				if (equalDurationUnisonsInfo.get(currentIndex) != null) {
					// Get the predicted voice and the ground truth voice for the note
					int predictedVoice = i;
					int groundTruthVoice = 
						DataConverter.convertIntoListOfVoices(groundTruthVoiceLabels.get(currentIndex)).get(0);
					// Get the other allowed voice (i.e., not the ground truth voice) for the note 
					int otherAllowedVoice = equalDurationUnisonsInfo.get(currentIndex)[0];
					if (equalDurationUnisonsInfo.get(currentIndex)[0] == groundTruthVoice) {
						otherAllowedVoice = equalDurationUnisonsInfo.get(currentIndex)[1]; 
					}
					// Get the complementary EDUnote
					int complementaryEDUNoteIndex = equalDurationUnisonsInfo.get(currentIndex)[2];
					// If the predicted voice is the other allowed voice and the complementary EDU note is predicted for
					// the voice that is the ground truth voice: swith the indices in the appropriate predicted voices 
					List<Integer> groundTruthVoiceInPredicted = notesPerVoicePredicted.get(groundTruthVoice);
					if (predictedVoice == otherAllowedVoice && groundTruthVoiceInPredicted.contains(complementaryEDUNoteIndex)) {
						// Set index j (the index of currentIndex) in indicesCurrentPredictedVoice voice to complementaryEDUNoteIndex
						indicesCurrentPredictedVoice.set(j, complementaryEDUNoteIndex);
						// Set index k (the index of complementaryEDUNoteIndex) in groundTruthVoiceInPredicted to currentIndex
						int k = groundTruthVoiceInPredicted.indexOf(complementaryEDUNoteIndex);
						groundTruthVoiceInPredicted.set(k, currentIndex);	
					}
				}
			}
		}	 
		return notesPerVoicePredicted;
	}

}
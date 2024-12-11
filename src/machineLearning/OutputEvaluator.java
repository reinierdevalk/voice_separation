package machineLearning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import tools.ToolBox;
import tools.labels.LabelTools;
import ui.Runner;
import ui.Runner.ModellingApproach;
import de.uos.fmt.musitech.utility.math.Rational;
import external.Transcription;

public class OutputEvaluator {
	
	static boolean ignoreExceptionForTest = false;


	public static List<Double> createAllHighestNetworkOutputs(List<List<Double>> 
		allNetworkOutputsForAllChords) {		
		// allHighestNetworkOutputs, as well as allBestVoiceAssignments, must be recreated every time 
		// this method is called in the training case. In the test case, this method is only called once, so
		// recreation is not strictly necessary -- but it is harmless nevertheless
		List<Double> allHiNetwOutp = new ArrayList<Double>();
		// For each chord
		for (List<Double> currentNetworkOutputs : allNetworkOutputsForAllChords) {
//			int numberOfChords = argChordFeatures.size();
//			for (int chordIndex = 0; chordIndex < numberOfChords; chordIndex++) { 			  
			// a. For all possible feature vectors for this chord: evaluate the network and
			// add the result, the network output, to currentNetworkOutputs
//			List<List<Double>> currentChordFeatures = argChordFeatures.get(chordIndex);

//			List<Double> currentNetworkOutputs = 
//				createAllNetworkOutputsForChord(currentChordFeatures);
//			List<Double> currentNetworkOutputs = 
//				allNetworkOutputsForAllChords.get(chordIndex);

			// b. Add the highest network output to the list; it does not matter whether 
			// it appears more than once (if this happens, it is solved within
			// determineBestVoiceAssignment(), which is called in training, test, and
			// application case)
			double currentHighestNetworkOutput = Collections.max(currentNetworkOutputs);
			allHiNetwOutp.add(currentHighestNetworkOutput);
		}	
		return allHiNetwOutp;
	}


	public static List<List<Integer>> createAllBestVoiceAssignments(List<List<Double>> 
		allNetworkOutputsForAllChords, List<List<List<Integer>>> 
		argPossibleVoiceAssignmentsAllChords) { 

		// allBestVoiceAssignments must be recreated every time this method is called 
		// in the training case. In the test case, this method is only called once, so
		// recreation is not strictly necessary -- but it is harmless nevertheless
		List<List<Integer>> allBestVoiceAss = new ArrayList<List<Integer>>();
		// For each chord
//		int numberOfChords = argChordFeatures.size();
		int numberOfChords = allNetworkOutputsForAllChords.size();
		for (int chordIndex = 0; chordIndex < numberOfChords; chordIndex++) { 			  
			// a. For all possible feature vectors for this chord: evaluate the network and
			// add the result, the network output, to currentNetworkOutputs
//			List<List<Double>> currentChordFeatures = argChordFeatures.get(chordIndex);
			List<List<Integer>> currentPossibleVoiceAssignments = 
				argPossibleVoiceAssignmentsAllChords.get(chordIndex);  	

//			List<Double> currentNetworkOutputs =
//				createAllNetworkOutputsForChord(currentChordFeatures);
			List<Double> currentNetworkOutputs =
				allNetworkOutputsForAllChords.get(chordIndex);

			// b. Determine the best voice assignment and add it to the list. Because it is
			// possible that different voice assignments result in the same network output,
			// the highest output may occur multiple times. If this is the case, less likely
			// candidates must be filtered out; this happens inside determineBestVoiceAssignment(). TODO?
			List<Integer> predictedBestVoiceAssignment = 
				determineBestVoiceAssignment(currentNetworkOutputs, 
				currentPossibleVoiceAssignments); 
			allBestVoiceAss.add(predictedBestVoiceAssignment);
		}
		return allBestVoiceAss;
	}


	/**
	 * Gets the voices the network predicts for the given network outputs. 
	 *   
	 * @param modelParameters
	 * @param argAllNetworkOutputs Only used in N2N case; <code>null</code> in C2C case 
	 * @param argAllBestMappings Only used in C2C case; <code>null</code> in N2N case
	 * @return A List<List<Integer>> containing all the predicted voices 
	 */
	// TESTED (for both N2N and C2C)
	public static List<List<Integer>> determinePredictedVoices(Map<String, Double> modelParameters, 
		List<double[]> argAllNetworkOutputs, List<List<Integer>> argAllBestMappings, int maxNumVoices) { 

//		List<List<List<Integer>>> allPredictedVoicesAndDurations = new ArrayList<List<List<Integer>>>();
		List<List<Integer>> allPredictedVoices = new ArrayList<List<Integer>>();
//		List<List<Integer>> allPredictedDurations = new ArrayList<List<Integer>>();

//		int modellingApproach = modelParameters.get(Runner.MODELLING_APPROACH).intValue();
		ModellingApproach ma = 
			Runner.ALL_MODELLING_APPROACHES[modelParameters.get(Runner.MODELLING_APPROACH).intValue()];
		
//		double allowCoD = modelParameters.get(TrainingManager.ALLOW_COD);
		boolean allowCoD = 
			ToolBox.toBoolean(modelParameters.get(Runner.SNU).intValue());

		if (ma == ModellingApproach.N2N) { 
//			// allNetworkOutputs must be recreated every time this method is called in the training case. In the test
//			// case, this method is only called once, so recreation is not strictly necessary -- but it is harmless nevertheless
//			allNetworkOutputs = new ArrayList<double[]>();
			
			double deviationThreshold = -1.0;
			if (Runner.getDataset().isTablatureSet()) {
				deviationThreshold = modelParameters.get(Runner.DEV_THRESHOLD);
			}

//			for (int i = 0; i < noteFeatures.size(); i++) {
			for (int i = 0; i < argAllNetworkOutputs.size(); i++) {
//				// Evaluate the network for the current onset
//				double[] predictedLabel = evalNetwork(noteFeatures.get(i));
//				allNetworkOutputs.add(predictedLabel);
//				double[] predictedLabel = allNetworkOutputs.get(i);
				double[] predictedLabel = argAllNetworkOutputs.get(i);
				// Interpret the output
				// a. Get the predicted voice(s) and add them to allPredictedVoices 
//				List<Integer> predictedVoices = outputEvaluator.interpretNetworkOutput(predictedLabel, allowCoD, 
//				deviationThreshold).get(0);
				allPredictedVoices.add(interpretNetworkOutput(predictedLabel, allowCoD, 
					deviationThreshold, maxNumVoices).get(0));
//				// b. If applicable: get the predicted duration(s) and add them to allPredictedDurations
//				if (predictedLabel.length > EncogNNManager.MAX_NUM_VOICES_N2N) {
//				if (predictedLabel.length > Transcription.MAXIMUM_NUMBER_OF_VOICES) {
//					allPredictedDurations.add(interpretNetworkOutput(predictedLabel,
//						allowCoD, deviationThreshold).get(1));
//				}
			}
		}
		if (ma == ModellingApproach.C2C) {
//			int numberOfChords = chordFeatures.size();
//			// allHighestNetworkOutputs, as well as allBestVoiceAssignments, must be recreated every time 
//			// this method is called in the training case. In the test case, this method is only called once, so
//			// recreation is not strictly necessary -- but it is harmless nevertheless
//			allHighestNetworkOutputs = new ArrayList<Double>(); 
//			allBestVoiceAssignments = new ArrayList<List<Integer>>();
//			// For each chord
//			for (int chordIndex = 0; chordIndex < numberOfChords; chordIndex++) { 			  
//				// a. For all possible feature vectors for this chord: evaluate the network and
//				// add the result, the network output, to currentNetworkOutputs
//				List<List<Double>> currentChordFeatures = chordFeatures.get(chordIndex);
//				List<List<Integer>> currentPossibleVoiceAssignments = 
//					possibleVoiceAssignmentsAllChords.get(chordIndex);  	
//				List<Double> currentNetworkOutputs = new ArrayList<Double>();
//				for (int j = 0; j < currentChordFeatures.size(); j++) {
//					List<Double> currentChordFeatureVector = currentChordFeatures.get(j);
//					double[] currentNetworkOutput = evalNetwork(currentChordFeatureVector);
//					currentNetworkOutputs.add(currentNetworkOutput[0]);
//					if (Double.isNaN(currentNetworkOutput[0])) { // TODO remove
//						System.out.println("Network output is NaN.");
//						System.exit(0);
//					}
//				}
//				// b. Add the highest network output to allHighestNetworkOutputs; it does not 
//				// matter whether it appears more than once (if this happens, it is solved 
//				// within determineBestVoiceAssignment(), which is called in training, test,
//				// and application case)
//				double currentHighestNetworkOutput = Collections.max(currentNetworkOutputs);
////			if (Collections.frequency(currentNetworkOutputs, currentHighestNetworkOutput) > 1) {
////				System.out.println("Highest network output appears more than once.");
////				System.exit(0);
////			}
//				allHighestNetworkOutputs.add(currentHighestNetworkOutput);
//
//				// c. Determine the best voice assignment and add it to allBestVoiceAssignments. Because it is possible 
//				// that different voice assignments result in the same network output, the highest output may occur
//				// multiple times. If this is the case, less likely candidates must be filtered out; this happens 
//				// inside determineBestVoiceAssignment()
//				List<Integer> predictedBestVoiceAssignment = 
////				outputEvaluator.determineBestVoiceAssignment(currentBasicTabSymbolPropertiesChord, currentNetworkOutputs,
////				currentPossibleVoiceAssignments, isTrainingOrTestMode);
//				outputEvaluator.determineBestVoiceAssignment(currentNetworkOutputs, currentPossibleVoiceAssignments); 
//				allBestVoiceAssignments.add(predictedBestVoiceAssignment);

//			DataConverter dc = new DataConverterTab();
			for (int i = 0; i < argAllBestMappings.size(); i++) {
				List<Integer> predictedBestMapping = argAllBestMappings.get(i);
				// d. Convert predictedBestMapping into a List of voices, and add it to 
				// allPredictedVoices
				List<List<Double>> predictedChordVoiceLabels = LabelTools.getChordVoiceLabels(
					predictedBestMapping, maxNumVoices // Schmier
				); 
				List<List<Integer>> predictedChordVoices = 
					LabelTools.getVoicesInChord(predictedChordVoiceLabels); 
				allPredictedVoices.addAll(predictedChordVoices);
			}
		}
//		allPredictedVoicesAndDurations.add(allPredictedVoices);
//		allPredictedVoicesAndDurations.add(allPredictedDurations);
//		return allPredictedVoicesAndDurations;
		return allPredictedVoices;
	}
	
	
	/**
	 * Gets the durations the network predicts for the given network outputs. 
	 *   
	 * @param modelParameters
	 * @param argAllNetworkOutputs Only used in N2N case; <code>null</code> in C2C case 
	 * @param argAllBestVoiceAssignments Only used in C2C case; <code>null</code> in N2N case
	 * @return A List<Rational[]> containing all the predicted durations
	 */
	// TODO test
	public static List<Rational[]> determinePredictedDurations(Map<String, Double> modelParameters,
		List<double[]> argAllNetworkOutputs, List<List<Integer>> argAllBestVoiceAssignments, int maxNumVoices, int maxTabSymDur) { 

//		List<List<List<Integer>>> allPredictedDurations = new ArrayList<List<List<Integer>>>();
//		List<List<Integer>> allPredictedVoices = new ArrayList<List<Integer>>();
//		List<List<Integer>> allPredictedDurations = new ArrayList<List<Integer>>();
		List<Rational[]> allPredictedDurations = new ArrayList<Rational[]>();

//		int modellingApproach = modelParameters.get(Runner.MODELLING_APPROACH).intValue();
		ModellingApproach ma = 
			Runner.ALL_MODELLING_APPROACHES[modelParameters.get(Runner.MODELLING_APPROACH).intValue()];
//		double allowCoD = modelParameters.get(TrainingManager.ALLOW_COD);
		boolean allowCoD = 
			ToolBox.toBoolean(modelParameters.get(Runner.SNU).intValue());
		double deviationThreshold = modelParameters.get(Runner.DEV_THRESHOLD);

		if (ma == ModellingApproach.N2N) { 
//			// allNetworkOutputs must be recreated every time this method is called in the training case. In the test
//			// case, this method is only called once, so recreation is not strictly necessary -- but it is harmless nevertheless
//			allNetworkOutputs = new ArrayList<double[]>();

			for (int i = 0; i < argAllNetworkOutputs.size(); i++) {
				double[] predictedLabel = argAllNetworkOutputs.get(i);
				// Interpret the output
				// a. Get the predicted voice(s) and add them to allPredictedVoices 
//				List<Integer> predictedVoices = outputEvaluator.interpretNetworkOutput(predictedLabel, allowCoD, 
//				deviationThreshold).get(0);
//				allPredictedVoices.add(interpretNetworkOutput(predictedLabel, allowCoD, 
//					deviationThreshold).get(0));
				// b. If applicable: get the predicted duration(s) and add them to allPredictedDurations
//				if (predictedLabel.length > EncogNNManager.MAX_NUM_VOICES_N2N) {
//				if (predictedLabel.length > Transcription.MAXIMUM_NUMBER_OF_VOICES) {
//				allPredictedDurations.add(interpretNetworkOutput(predictedLabel,
//					allowCoD, deviationThreshold).get(1));
//				System.out.println(Arrays.toString(predictedLabel));
				List<Integer> predDur = interpretNetworkOutput(
					predictedLabel, allowCoD, deviationThreshold, maxNumVoices
				).get(1);
				List<Double> durLabel = LabelTools.convertIntoDurationLabel(predDur, maxTabSymDur);
				allPredictedDurations.add(LabelTools.convertIntoDuration(durLabel));
//				}
			}
		}
		if (ma == ModellingApproach.C2C) {
//			DataConverter dc = new DataConverterTab();
			for (int i = 0; i < argAllBestVoiceAssignments.size(); i++) {
//				List<Integer> predictedBestVoiceAssignment = argAllBestVoiceAssignments.get(i);
//				// d. Convert predictedBestVoiceAssignment into a List of voices, and add it to allPredictedVoices
//				List<List<Double>> predictedChordVoiceLabels = 
//					DataConverterTab.getChordVoiceLabels(predictedBestVoiceAssignment); 
//				List<List<Integer>> predictedChordVoices = 
//					DataConverterTab.getVoicesInChord(predictedChordVoiceLabels); 
//				allPredictedVoices.addAll(predictedChordVoices);
			}
		}
//		allPredictedVoicesAndDurations.add(allPredictedVoices);
//		allPredictedVoicesAndDurations.add(allPredictedDurations);
//		return allPredictedVoicesAndDurations;
		return allPredictedDurations;
	}


	/**
	 * Given a List of all possible voice assignments for the chord, as well as the the network output for each
	 * assignment, determines the best voice assignment for that chord. 
	 * The procedure is as follows:
	 * (a) If the highest output occurs only once in the List of network outputs: the voice assignment that goes
	 *     with this output is returned
	 * (b) If the highest output occurs multiple times in the List of network outputs: the best voice assignment
	 *     is selected using the following prioritisation criteria:  
	 *     (1) Selects the voice assignment(s) with the fewest voice crossings. 
	 *         If this yields only one voice assignment: best voice assignment found.
	 *         If not: 
	 *         (2) Find, within the latest selection, the voice assignments with the fewest CoDs.
	 *             If this yields only one voice assignment: best voice assignment found.
	 *             If not:
	 *             (3) Find, within the latest selection, the voice assignments with the best pitch distribution (i.e., 
	 *                 those which contain the fewest assignments of onsets to voices where an onset's pitch does not 
	 *                 fall within the average pitch range of the voice it is assigned to).
	 *                 If this yields only one voice assignment: best voice assignment found.
	 *                 If not:
	 *                 (4) Randomly pick a voice assignment from the latest selection.  
	 * 
	 * NB: in the case of training and in test mode, this method is called by 
	 * EncogNeuralNetworkManager.determinePredictedVoices() (which, in turn, is called by 
	 * EncogNeuralNetworkManager.trainMultipleRuns() and TestManager.testInTestMode(), respectively); in
	 * application mode it is called by TestManager.testInApplicationMode(). 
	 * 
	 * @param basicTabSymbolPropertiesChord
	 * @param bestVoiceAssignments
	 * @return
	 */
	// TESTED
	public static List<Integer> determineBestVoiceAssignment(List<Double> allNetworkOutputs, List<List<Integer>>
		allPossibleVoiceAssignments) {

		List<Integer> bestVoiceAssignment = new ArrayList<Integer>();

		// 1. Determine the highest network output and how often it occurs in allNetworkOutputs
		double hiNetwOutp = Collections.max(allNetworkOutputs);
		int freqOfHiOutp = Collections.frequency(allNetworkOutputs, hiNetwOutp);
		// 2. Find which voice assignment(s) give(s) the highest output and add them to bestVoiceAssignments 
		List<List<Integer>> bestVoiceAssignments = new ArrayList<List<Integer>>();
		List<Integer> indicesOfHiNetwOutp = new ArrayList<Integer>(); 
		for (int i = 0; i < allNetworkOutputs.size(); i++) {
			if (allNetworkOutputs.get(i) == hiNetwOutp) {
				indicesOfHiNetwOutp.add(i);
				bestVoiceAssignments.add(allPossibleVoiceAssignments.get(i));
			}
		}
		// a. If the highest output occurs only once: best voice assignment found 
		if (freqOfHiOutp == 1) {
			bestVoiceAssignment = bestVoiceAssignments.get(0);
		}
		// b. If the highest output occurs more than once: determine the best voice assignment
		else {
//			System.out.println("IS MORE");
//			System.out.println("highest network output occurs more than once (" + frequencyOfHighestOutput + " times)");
//			System.out.println("... highest network output undecided; filtering out unlikely voice assignments ...");
//			System.out.println("highest network output = " + highestNetworkOutput);
//			System.out.println("frequency of highest network output = " + frequencyOfHighestOutput);

			bestVoiceAssignment = bestVoiceAssignments.get(0); // TODO?
			String ss = ToolBox.getTimeStamp();
			ss = ss.replace(":", "_");		
//			File f = new File("F/PhD/data/" + "results/Multiples/" + ss + ".txt");
//			String content = ToolBox.getTimeStamp() + "\r\n" + 
//				"best voice assignment: " + bestVoiceAssignment + "\r\n" + 
//				"allNetworkOutputs: " + allNetworkOutputs + "\r\n" + 
//				"allPossibleVoiceAssignments: " + allPossibleVoiceAssignments;
//			ToolBox.storeTextFile(content, f);

//			Random random = new Random();
//			int randomIndex = random.nextInt(bestVoiceAssignments.size());
//			bestVoiceAssignment = bestVoiceAssignments.get(randomIndex);
//			String s = "Best voice assignment (= " + bestVoiceAssignment +") randomly picked from voice assignments";
//			System.out.println(s);
//			message += s;

			// OUD VERVOLG VAN ELSE
//    	// 0. Only in case of training or in test mode, where the ground truth voice assignment is the first in 
//    	// allPossibleVoiceAssignments: if indicesOfHighestNetwork contains 0, bestVoiceAssignment is the one
//    	// at index 0 in allPossibleVoiceAssignments 
//    	if (isTrainingOrTestMode) { // TODO is this cheating?
//    		if (indicesOfHighestNetworkOutput.contains(0)) { 
//    			System.out.println("OOPS!");
//    			bestVoiceAssignment = allPossibleVoiceAssignments.get(0);
//    			String s = "Training case or test mode, filtering unnecessary: ground truth voice assignment " + 
//    			  "(index 0 in allPossibleVoiceAssignments) among best voice assignments; this voice assignment selected.";
//    			System.out.println(s);
//    			message += s;
//    			
//    			// copy-paste from end of method -- now duplicate code! -->
//    			String currentTimeStamp = AuxiliaryTool.getTimeStamp();
//    			currentTimeStamp = currentTimeStamp.replace(':', '-');
//    		  Random random = new Random();
//    		  double randomDouble1 = random.nextDouble();
//    		  double randomDouble2 = random.nextDouble();
//    		  double randomDouble3 = random.nextDouble();
//    		  double randomExtension = (randomDouble1 / randomDouble2) * randomDouble3; 
//    			String fileName = currentTimeStamp.substring(currentTimeStamp.length() - 8, currentTimeStamp.length()) +
//    				" " + randomExtension + ".txt";
//    			String text = "highestNetworkOutput = " + highestNetworkOutput + "\r\n";
//    			text += "frequency of highest network output = "  + frequencyOfHighestOutput + "\r\n";
//    			text += message;
//    			AuxiliaryTool.storeTextFile(text, new File("F:/research/data/Files out/Neural network/" + fileName));
//    			// <-- end duplicate code
//    			return bestVoiceAssignment; 
//    		}
//    	}
//    	 
//		  // 1. Filter out all voice assignments in bestVoiceAssignments with more than the lowest number of voice
//		  // crossing pairs 
//		  // a. For each voice assignment in bestVoiceAssignments: determine the number of voice crossing pairs
//		  List<Integer> numbersOfVoiceCrossingPairs = new ArrayList<Integer>();		
//		  for (List<Integer> currentBestVoiceAssignment : bestVoiceAssignments) {
//			  List<List<Integer>> voiceCrossingInformation = 
//		      featureGenerator.getVoiceCrossingInformationInChord(basicTabSymbolProperties, basicNoteProperties, 
//		    	currentBestVoiceAssignment);
//	  	 int currentNumberOfVoiceCrossingPairs = (voiceCrossingInformation.get(1).size()) / 2;
//  		 numbersOfVoiceCrossingPairs.add(currentNumberOfVoiceCrossingPairs);
//  		}
//      // b. Remove all the voice assignments that contain more than the lowest number of voice crossing pairs 
//		  // from bestVoiceAssignments
//		  int lowestNumberOfVoiceCrossingPairs = Collections.min(numbersOfVoiceCrossingPairs); 
//		  List<Integer> indicesToRemove = new ArrayList<Integer>();
//		  for (int i = 0; i < numbersOfVoiceCrossingPairs.size(); i++) { 
//		  	int currentNumberOfVoiceCrossingPairs = numbersOfVoiceCrossingPairs.get(i);
//		  	if (currentNumberOfVoiceCrossingPairs > lowestNumberOfVoiceCrossingPairs) {
//		  	  indicesToRemove.add(i);
//	  	  }
//	   	}
//		  Collections.reverse(indicesToRemove);
//		  for (int i = 0; i < indicesToRemove.size(); i++) {
//	  	  int indexToRemove = indicesToRemove.get(i);
//  		 	bestVoiceAssignments.remove(indexToRemove);  
//		  }
//		  // c. Does bestVoiceAssignments contain only one element after filtering? bestVoiceAssignment found
//		  if (bestVoiceAssignments.size() == 1) {
//		   	bestVoiceAssignment = bestVoiceAssignments.get(0);
//		   	String s = "Filtering on lowest number of voice crossing pairs successful: best voice " + 
//			   	"assignment = " + bestVoiceAssignment;
//		   	System.out.println(s);
//		   	message += s;
//  		}
// 	    // 2. Does bestVoiceAssignments still contain multiple elements after filtering? Filter out all voice
// 		  // assignments in bestVoiceAssignments with more than the lowest number of CoDs	
// 		  else {
// 		    // a. For each voice assignment in bestVoiceAssignments: determine the number of CoDs
//   		  List<Integer> numbersOfCoDs = new ArrayList<Integer>();		
//   		  for (List<Integer> currentBestVoiceAssignment : bestVoiceAssignments) {
//   		  	int numberOfCoDs = 0; 
//   			  for (int onset : currentBestVoiceAssignment) {
//   			    if (onset != -1) {
//   	  		  	int currentFrequency = Collections.frequency(currentBestVoiceAssignment, onset);
//   	  		  	if (currentFrequency > 1) {
//   		  	  		numberOfCoDs++;
//   			    	}
//   			    }
//   			  }
//   			  numberOfCoDs = numberOfCoDs / 2;
//   			  numbersOfCoDs.add(numberOfCoDs);
//   		  }
//   	    // b. Remove all the voice assignments that contain more than the lowest number of CoDs from
//   		  // bestVoiceAssignments. First clear indicesToRemove
//   		  indicesToRemove.clear(); 
//   		  int lowestNumberOfCoDs = Collections.min(numbersOfCoDs); 
//   		  for (int i = 0; i < numbersOfCoDs.size(); i++) { 
//   	  		int currentNumberOfCoDs = numbersOfCoDs.get(i);
//   	  		if (currentNumberOfCoDs > lowestNumberOfCoDs) {
//   	  			indicesToRemove.add(i);
//   	  		}
//   	  	}
//   	  	Collections.reverse(indicesToRemove);
//   	  	for (int i = 0; i < indicesToRemove.size(); i++) {
//   	  		int indexToRemove = indicesToRemove.get(i);
//   	  		bestVoiceAssignments.remove(indexToRemove);
//   	  	}
//   	    // c. Does bestVoiceAssignments contain only one element after filtering? bestVoiceAssignment found
//   		  if (bestVoiceAssignments.size() == 1) {
//   		  	bestVoiceAssignment = bestVoiceAssignments.get(0);
//   		  	String s = "Filtering on lowest number of CoDs within voice assignments with lowest number of " + 
//     		  	"voice crossing pairs successful: best voice assignment = " + bestVoiceAssignment;
//   		  	System.out.println(s);
//   		  	message += s; 
// 	  	  }
// 	      // 3. Does bestVoiceAssignments still contain multiple elements after filtering? Filter out the voice
// 	  	  // assignments in bestVoiceAssignments that contain the most onsets assigned to a voice whose average
//   		  // pitch range does not include those onsets' pitches. The following emperically determined average
//   		  // ranges are used:
//   	    // Average range for voice 0: 56 - ...
//	 		  // Average range for voice 1: 53-67
//	 	    // Average range for voice 2: 48-65
//	 	    // Average range for voice 3: ... - 60
//   		  else {
//   		    // a. For each voice assignment in bestVoiceAssignments: determine the penalty
//   	  		List<Integer> pitchesInChord = 
//   	  			featureGenerator.getPitchesInChord(basicTabSymbolProperties, basicNoteProperties);
//     			List<Integer> penalties = new ArrayList<Integer>();
//     			for (List<Integer> currentBestVoiceAssignment : bestVoiceAssignments) {
//     				List<List<Double>> currentChordVoiceLabels = dataConverter.getChordVoiceLabels(currentBestVoiceAssignment);
//     				List<List<Integer>> currentVoices = dataConverter.getVoicesInChord(currentChordVoiceLabels);
//     				int currentPenalty = 0;
//     				for (int i = 0; i < currentVoices.size(); i++) {
//     					List<Integer> voicesCurrentOnset = currentVoices.get(i);
//     					int pitchCurrentOnset = pitchesInChord.get(i);
//     					if (voicesCurrentOnset.contains(0) && pitchCurrentOnset < 56) {
//     						currentPenalty++;
//     					}
//     					if (voicesCurrentOnset.contains(1) && (pitchCurrentOnset < 53 || pitchCurrentOnset > 67)) {
//     						currentPenalty++;
//     					}
//     					if (voicesCurrentOnset.contains(2) && (pitchCurrentOnset < 48 || pitchCurrentOnset > 65)) {
//      					currentPenalty++;
//  	    			}
//  	  	  		if (voicesCurrentOnset.contains(3) && (pitchCurrentOnset > 60)) {
//  	  		  		currentPenalty++;
//  	  			  }
//  	  	  	}
//  	  	  	System.out.println("currentPenalty = " + currentPenalty);
//  	  	  	penalties.add(currentPenalty);
//  	  	  }
//  	      // b. Remove all the voice assignments that give a penalty higher than lowestPenalty from
//  		    // bestVoiceAssignments. First clear indicesToRemove
//   		    indicesToRemove.clear(); 
//   		    int lowestPenalty = Collections.min(penalties); 
//   	  	  for (int i = 0; i < penalties.size(); i++) { 
//   	    		int currentPenalty = penalties.get(i);
//       			if (currentPenalty > lowestPenalty) {
//       				indicesToRemove.add(i);
//       			}
//       		}
//       		Collections.reverse(indicesToRemove);
//       		for (int i = 0; i < indicesToRemove.size(); i++) {
//       			int indexToRemove = indicesToRemove.get(i);
//       			bestVoiceAssignments.remove(indexToRemove);
//       		}
//       	  // c. Does bestVoiceAssignments contain only one element after filtering? bestVoiceAssignment found
//       		if (bestVoiceAssignments.size() == 1) {
//       			bestVoiceAssignment = bestVoiceAssignments.get(0);
//       			String s = "Filtering on best pitch distribution within voice assignments with lowest " + 
//         			"number of CoDs within voice assignments with lowest number of voice crossing pairs successful: " + 
//         			"best voice assignment = " + bestVoiceAssignment;
//       			System.out.println(s);
//       			message += s;
//     		  }
//       		// 4. Does bestVoiceAssignments still contain multiple elements after filtering? Randomly select a
//	        // voice assignment from the remaining elements of bestVoiceAssignments
//     	  	else {
//    	  	  Random random = new Random();
//    	  	  int finalInt = random.nextInt(bestVoiceAssignments.size());
//    	  	  bestVoiceAssignment = bestVoiceAssignments.get(finalInt);
//  	 	    	String s = "Filtering on best pitch distribution within voice assignments with lowest " + 
//       			  "number of CoDs within voice assignments with lowest number of voice crossing pairs " + 
//      	  		"unsuccessful: best voice assignment (= " + bestVoiceAssignment +") randomly picked from " + 
//       			  "remaining voice assignments";
//  	 	  	  System.out.println(s);
//  	    	  message += s;
//        	}
//  	    }
//  	  }
//		  
//		  String currentTimeStamp = AuxiliaryTool.getTimeStamp();
//		  currentTimeStamp = currentTimeStamp.replace(':', '-');
//		  Random random = new Random();
//		  double randomDouble1 = random.nextDouble();
//		  double randomDouble2 = random.nextDouble();
//		  double randomDouble3 = random.nextDouble();
//		  double randomExtension = (randomDouble1 / randomDouble2) * randomDouble3; 
//			String fileName = currentTimeStamp.substring(currentTimeStamp.length() - 8, currentTimeStamp.length()) +
//				" " + randomExtension + ".txt";
//			String text = "highestNetworkOutput = " + highestNetworkOutput + "\r\n";
//			text += "frequency of highest network output = "  + frequencyOfHighestOutput + "\r\n";
//			text += message;
//			AuxiliaryTool.storeTextFile(text, new File("F:/research/data/Files out/Neural network/" + fileName));  
		}
		return bestVoiceAssignment;
	}

	/**
	 * Interprets the given network output, an array of activation values, and returns a List<List<<Integer>> containing: <br>
	 * as element 0: the voice(s) the network output corresponds to. <br>
	 *               a. If CoDs are allowed and the network output represents no CoD, the List will contain only
	 *                  one element, corresponding to the highest activation value in the network output.<br>
	 *               b. If CoDs are allowed and the network output represents a CoD, the List will contain two
	 *                  elements, corresponding to the highest and second highest activation values, respectively.
	 *                  NB: The highest and second highest values may appear multiple times; for details see code.<br>
	 *               c. If CoDs are not allowed, the list will contain only on element, corresponding to (the first
	 *                  appearance of) the highest activation value in the network output.<br>
	 * as element 1: the duration(s) the network output corresponds to. Currently a single duration, corresponding 
	 *               to index + 1 of (the first appearance of) the highest activation value in the network output. 
	 *               If the length of networkOutput is Transcription.MAXIMUM_NUMBER_OF_VOICES, i.e., if no duration
	 *               was predicted, this element is <code>null</code>.<br><br> 
	 *   
	 * NB: (Rare) cases solved arbitrarily are:<br>
	 * (a) When CoDs are not allowed and the highest activation value appears more than once in the network output <br>
	 * (b) When CoDs are allowed and the highest activation value appears more than twice in the network output <br>
	 * (c) When CoDs are allowed, the network output contains a CoD, the highest activation value appears once, 
	 *     and the second highest more than once
	 *  
	 * @param networkOutput The NN's output
	 * @param allowCoD Whether or not to allow the label to contain a CoD
	 * @param deviationThreshold The deviation threshold to use to decide the presence of a CoD
	 * @return  	
	 */
	// TESTED
	public static List<List<Integer>> interpretNetworkOutput(double[] networkOutput, 
		boolean allowCoD, double deviationThreshold, int maxNumVoices) { 
		List<List<Integer>> predictedValues = new ArrayList<List<Integer>>();

		// 0. Split networkOutput into a list representing the predicted voice(s) and the
		// predicted duration
		List<Double> outputVoices = new ArrayList<Double>();
		List<Double> outputDuration = new ArrayList<Double>();
		for (int i = 0; i < networkOutput.length; i++) {
			if (i < maxNumVoices) { // Schmier
				outputVoices.add(networkOutput[i]);
			}
			else {
				outputDuration.add(networkOutput[i]);
			}
		}

		// a. For the network output representing the voices
		// 1. Get the information on the two highest values in outputVoices
		double[][] highestValuesInfo = getTwoHighestValuesInformation(outputVoices);
		double highestValue = highestValuesInfo[0][0];
		double secondHighestValue = highestValuesInfo[1][0];
		double freqOfHighestValue = highestValuesInfo[0][1];
		double freqOfSecondHighestValue = highestValuesInfo[1][1];

		// 2. Make the List of predicted voices 
		List<Integer> predictedVoices = new ArrayList<Integer>();
		// a. If CoDs are not allowed: get the position of highestValue and add that to 
		// predictedVoices. If highestValue appears more than once, its first occurrence is 
		// returned.
		// TODO Arbitrary choice, fix? How likely is this to happen?
		if (allowCoD == false) {
			int posOfHighestValue = outputVoices.indexOf(highestValue);
			predictedVoices.add(posOfHighestValue);
		}
		// b. If CoDs are allowed
		else {
			// Determine whether networkOutput contains a CoD
			boolean containsCoD = 
				determinePresenceOfCoD(outputVoices, highestValuesInfo, deviationThreshold);
			// a. No CoD: highestValue will appear only once
			if (containsCoD == false) {
				int positionOfHighestValue = outputVoices.indexOf(highestValue); 
				predictedVoices.add(positionOfHighestValue);
			}
			// b. CoD: two possible situations 
			else {
				// (i) highestValue appears twice or more (in which case only highestValue is
				// relevant)
				if (freqOfHighestValue >= 2.0) {
					// 1. Get all the positions of highestValue
					List<Integer> positionsOfHighestValue = new ArrayList<Integer>();
					for (int i = 0; i < networkOutput.length; i++) {
						if (networkOutput[i] == highestValue) {
							positionsOfHighestValue.add(i);
						}
					}
					// 2. Add the first two elements of positionsOfHighestValue to predictedVoices. 
					// The sequence of adding does not matter as both elements are equally 
					// important. Note that:
					// a. If highestValue appears twice, all elements of positionsOfHighestValue 
					// are added
					// b. If highestValue appears more than twice, only the first two elements 
					// of positionsOfHighestValue are added. 
					// TODO Arbitrary choice, fix? How likely is this to happen? 
					List<Integer> firstTwoElements = 
						new ArrayList<Integer>(positionsOfHighestValue.subList(0, 2));
					predictedVoices.addAll(firstTwoElements);
				}
				// (ii) highestValue appears only once and secondHighestValue once or more 
				// (in which case both highestValue and secondHighestValue are relevant)
				if (freqOfHighestValue == 1.0) {
					// 1. Get the position of highestValue and add it as the first element to
					// predictedVoices
					int positionOfHighestValue = outputVoices.indexOf(highestValue);
					predictedVoices.add(positionOfHighestValue);
					// 2. Get the position of secondHighestValue and add it as the second 
					// element to predictedVoices
					// a. If secondHighestValue appears once: get its position and add it to 
					// predictedVoices
					if (freqOfSecondHighestValue == 1.0) {
						int positionOfSecondHighestValue = 
							outputVoices.indexOf(secondHighestValue);
						predictedVoices.add(positionOfSecondHighestValue);
					}	
					// b. If secondHighestValue appears twice or more: get the positions and
					// add the one that is closest to positionOfHighestValue to predictedVoices.
					// TODO Arbitrary choice, fix? How likely is this to happen?
					if (freqOfSecondHighestValue >= 2.0) {
						List<Integer> positionsOfSecondHighestValue = new ArrayList<Integer>();
						// List all positions of secondHighestValue
						for (int i = 0; i < networkOutput.length; i++) {
							if (networkOutput[i] == secondHighestValue) {
								positionsOfSecondHighestValue.add(i);
							}
						}
						// Find the element of positionsOfSecondHighestValue that is closest 
						// to positionOfHighestValue and add that to predictedVoices 
						// NB: If there are two positions equally close to positionOfHighestValue, 
						// closestPosition will be the the last of these found. E.g., if 
						// positionOfHighestValue is 1 and positionsOfSecondHighestValue is 
						// [0, 2, 3], closestPosition is 2. By changing <= to < in the if, 
						// closestPosition becomes the first found (in the case of this 
						// example, 0)
						int smallestDistance = Integer.MAX_VALUE;
						int closestPosition = -1;
						for (int i = 0; i < positionsOfSecondHighestValue.size(); i++) {
							int currentPositionOfSecondHighestValue = 
								positionsOfSecondHighestValue.get(i);
							int currentDistance = 
								Math.abs(currentPositionOfSecondHighestValue - 
								positionOfHighestValue);
							if (currentDistance <= smallestDistance) {
								smallestDistance = currentDistance;
								closestPosition = currentPositionOfSecondHighestValue; 
							}
						} 
						predictedVoices.add(closestPosition);
						// In non-unit test case, throw RuntimeException
						if (ignoreExceptionForTest == false) {
							throw new RuntimeException("Irregularity in OutputEvaluatorTab.interpretNetworkOutput().");
						}
					}
				}
			} 
		}
		predictedValues.add(predictedVoices);

		// b. For the network output representing the duration (if any)
		List<Integer> predictedDurations = null; 
		if (networkOutput.length > maxNumVoices) { // Schmier
			predictedDurations = new ArrayList<Integer>();
			// Get the position of highestValue and add that to predictedDurations. If 
			// highestValue appears more than once, its first occurrence is returned. 
			// TODO Arbitrary choice, fix? How likely is this to happen?
			double highestValueDur = Collections.max(outputDuration);
			int positionOfHighestValue = outputDuration.indexOf(highestValueDur);
			predictedDurations.add(positionOfHighestValue + 1);

			boolean containsCoD = false;
			// TODO change into:
			if (!containsCoD) {
				// Only one duration needed; get the position of highestValue and add that to predictedDurations
			}
			else {
				// Two durations needed; get the positions of the two highest values and add them to predictedDurations 
			}
		}
		predictedValues.add(predictedDurations);

		return predictedValues;  	  
	}


	/**
	 * Gets the values of the highest and the second highest value in the given network output, 
	 * as well as their frequency. If every element of the network output has the same value, 
	 * -1 is returned as the second highest value (this is safe since the network output will 
	 * contain only values between and including 0 and 1).
	 *  
	 * @param networkOutputAsList A List representation of the network output to get the 
	 * information from
	 * @return A double[][] containing:
	 *         as element [0][0] the highest value;
	 *         as element [0][1] the frequency of the highest value;
	 *         as element [1][0] the second highest value; 
	 *         as element [1][1] the frequency of the second highest value  
	 */
	// TESTED
	static double[][] getTwoHighestValuesInformation(List<Double> networkOutputAsList) {
		double[][] twoHighestValuesInformation = new double[2][2];

		// Get the highest and second highest value. Initialise secondHighestValue as a negative value (any 
		// negative value is OK since the elements of networkOutput are always >= 0 and <= 1). If every element of
		// networkOutput has the same value, i.e., if there is no second highest value, secondHighestValue will 
		// retain this initial value.
		double highestValue = Collections.max(networkOutputAsList); 
		double secondHighestValue = -1; 
		for (int i = 0; i < networkOutputAsList.size(); i++) {
			double currentValue = networkOutputAsList.get(i);
			if (currentValue > secondHighestValue && currentValue < highestValue) {
				secondHighestValue = currentValue;
			}
		}

		// Determine the frequency of highestValue and secondHighestValue. If every element of networkOutput has the
		// same value: set frequencyOfSecondHighestValue to 0
		int frequencyOfHighestValue = Collections.frequency(networkOutputAsList, highestValue); 
		int frequencyOfSecondHighestValue;
		if (secondHighestValue == -1) { // OR: if (frequencyOfHighestValue == networkOutput.length) {
			frequencyOfSecondHighestValue = 0;
		}
		else {
			frequencyOfSecondHighestValue = Collections.frequency(networkOutputAsList, secondHighestValue);
		}
		// For checking whether the highest value ever appears more than once
		// remove -->
		if (frequencyOfHighestValue > 1) {
			boolean deployTrainedUserModel = Runner.getDeployTrainedUserModel();
//				ToolBox.toBoolean(Runner.getModelParams().get(Runner.DEPLOY_TRAINED_USER_MODEL).intValue());
			if (!deployTrainedUserModel) {	
				System.out.println("The highest value appears more than once in the network output.");
				System.out.println(highestValue);
				System.out.println(networkOutputAsList);
//				throw new RuntimeException("The highest value appears more than once in the network output."); reactivate!
			}
		}
		// <-- remove

		// Set and return twoHighestValuesInformation
		twoHighestValuesInformation[0][0] = highestValue;
		twoHighestValuesInformation[0][1] = frequencyOfHighestValue;
		twoHighestValuesInformation[1][0] = secondHighestValue;
		twoHighestValuesInformation[1][1] = frequencyOfSecondHighestValue;
		return twoHighestValuesInformation;
	}


//  /**
//	 * Gets the values of the highest and the second highest value in the given network output, as well as their
//	 * frequency. If every element of the network output has the same value, -1 is returned as the second highest
//	 * value (this is safe since the network output will contain only values between and including 0 and 1).
//	 *  
//	 * @param networkOutput The network output to get the information from
//	 * @return A double[][] containing:
//	 *         as element [0][0] the highest value;
//	 *         as element [0][1] the frequency of the highest value;
//	 *         as element [1][0] the second highest value; 
//	 *         as element [1][1] the frequency of the second highest value  
//	 */
//	public double[][] getTwoHighestValuesInformationOLD(double[] networkOutput) {
//		double[][] twoHighestValuesInformation = new double[2][2];
//		
//		// Turn networkOutput into a List
//		List<Double> networkOutputAsList = new ArrayList<Double>();
//   	for (int j = 0; j < networkOutput.length; j++) {
//			networkOutputAsList.add(networkOutput[j]);
//		}
//   	
//    // Get the highest and second highest value. Initialise secondHighestValue as a negative value (any 
//   	// negative value is OK since the elements of networkOutput are always >= 0 and <= 1). If every element of
//   	// networkOutput has the same value, i.e., if there is no second highest value, secondHighestValue will 
//   	// retain this initial value.
//   	double highestValue = Collections.max(networkOutputAsList); 
//   	double secondHighestValue = -1; 
//   	for (int i = 0; i < networkOutputAsList.size(); i++) {
//   		double currentValue = networkOutputAsList.get(i);
//   		if (currentValue > secondHighestValue && currentValue < highestValue) {
//   			secondHighestValue = currentValue;
//   		}
//   	}
//   	
//   	// Determine the frequency of highestValue and secondHighestValue. If every element of networkOutput has the
//   	// same value: set frequencyOfSecondHighestValue to 0
//   	int frequencyOfHighestValue = Collections.frequency(networkOutputAsList, highestValue); 
//   	
//   	// For checking whether the highest value ever appears more than once, remove -->
//   	if (frequencyOfHighestValue > 1) {
//   		System.out.println("The highest value appears more than once in the network output.");
////	  	throw new RuntimeException("The highest value appears more than once in the network output.");
//   	}
//   	// <-- remove
//   	
//   	int frequencyOfSecondHighestValue;
//   	if (secondHighestValue == -1) { // OR: if (frequencyOfHighestValue == networkOutput.length) {
//   	  frequencyOfSecondHighestValue = 0;
//   	}
//   	else {
//   		frequencyOfSecondHighestValue = Collections.frequency(networkOutputAsList, secondHighestValue);
//   	}
//   	
//   	// Set and return twoHighestValuesInformation
//   	twoHighestValuesInformation[0][0] = highestValue;
//   	twoHighestValuesInformation[0][1] = frequencyOfHighestValue;
//   	twoHighestValuesInformation[1][0] = secondHighestValue;
//   	twoHighestValuesInformation[1][1] = frequencyOfSecondHighestValue;
//   	return twoHighestValuesInformation;
//	}


	/**
	 * Determines whether the given network output contains a CoD. This is the case when the 
	 * second highest activation value in the network output deviates by no more than the 
	 * allowed maximum deviation (determined by the given deviation threshold) from the highest 
	 * activation value.
	 * Examples: 
	 * (i)  deviationThreshold == 0.05, highestValue == 1.0. 
	 *      A secondHighestValue >= 0.95 will yield a CoD, but one < 0.95 will not.
	 * (ii) deviationThreshold == 0.00, highestValue == 1.0. 
	 *      This is the strictest case, in which a CoD is only allowed when highestValue and 
	 *      secondhighestValue are equal, i.e., when highestValue appears twice or more.
	 *          
	 * @param networkOutputAsList
	 * @param deviationThreshold
	 * @return <code>true</code> if, according to the threshold criterion, the predicted label contains a CoD;
	 *         <code>false</code> if not.
	 */
	// TESTED
	static boolean determinePresenceOfCoD(List<Double> networkOutputAsList, double[][] twoHighestValuesInfo,
		double deviationThreshold) {
		boolean containsCoD = false;
//		double[][] twoHighestValuesInfo = getTwoHighestValuesInformation(networkOutputAsList); // TODO give as argument to skip for-loop in method?

		// Get highestValue and determine how often it appears in networkOutputAsList
		double highestValue = twoHighestValuesInfo[0][0];
		double frequencyOfHighestValue = twoHighestValuesInfo[0][1];
		// Determine secondHighestValue
		double secondHighestValue;
		// If highestValue appears only once: get secondHighestValue 
		if (frequencyOfHighestValue == 1) { 
			secondHighestValue = twoHighestValuesInfo[1][0];
		}
		// If highestValue appears more than once: secondHighestValue is equal to highestValue 
		else { 
			secondHighestValue = highestValue;
		}

		// Determine the maximum deviation allowed; if secondHighestValue deviates by no more 
		// than that maximum deviation, a CoD is allowed  
		double maximumDeviation = deviationThreshold * highestValue;
		if (secondHighestValue >= (highestValue - maximumDeviation)) {
			containsCoD = true;
		}

		return containsCoD;
	}
	
	
	/**
	 * Calculates, for all notes as represented by n network outputs and x*n MM outputs, 
	 * the combined network output (see combineNetworkOutputs()).
	 * 
	 * @param networkOutputs
	 * @param allMelodyModelOutputs
	 * @param argModelOutputWeights
	 * @return For each note, the combined network output.
	 */
	// TESTED
	public static List<double[]> createCombinedNetworkOutputs(List<double[]> networkOutputs, 
		List<List<double[]>> allMelodyModelOutputs, List<Double> argModelOutputWeights) {
		List<double[]> combinedOutputs = new ArrayList<double[]>();
		// For each note
		for (int i = 0; i < networkOutputs.size(); i++) {
			List<double[]> allCurrOutputs = new ArrayList<double[]>();
			// Add network output
			allCurrOutputs.add(networkOutputs.get(i));
			// Add 
			for (List<double[]> l : allMelodyModelOutputs) {
				allCurrOutputs.add(l.get(i));
			}
//			System.out.println("i = " + i);
			combinedOutputs.add(combineNetworkOutputs(allCurrOutputs, argModelOutputWeights));
		}
		return combinedOutputs;
	}


	/**
	 * Calculates, for a single note for which there are x model outputs, the weighted 
	 * geometric mean for each output element over all given outputs. 
	 * See https://en.wikipedia.org/wiki/Weighted_geometric_mean
	 * 
	 * @param outputs Contains: <br>
	 *                at index 0: the NN output <br>
	 *                at index 1: the output for MM1 <br>
	 *                at index n: the output for MMn
	 * @param weights Contains the weights for NN, MM1, ... MMn
	 * @return The weighted geometric mean for each output element over all outputs.
	 */
	// TESTED
	public static double[] combineNetworkOutputs(List<double[]> outputs, List<Double> weights) {		
		if (outputs.size() != weights.size()) {
			throw new RuntimeException("Size of arguments does not match.");
		}

		double[] combinedOutp = new double[outputs.get(0).length];
		// For each output element
		for (int i = 0; i < combinedOutp.length; i++) {
			// List for each model the output at index i
			List<Double> outputsAtIndex = new ArrayList<Double>();
			for (double[] currOutp : outputs) {
				double x = currOutp[i];
				outputsAtIndex.add(x);
			}
			combinedOutp[i] = ToolBox.weightedGeometricMean(outputsAtIndex, weights);
			if (Double.isNaN(combinedOutp[i])) {
				System.err.println("NaN occurred");
				combinedOutp[i] = 0;
			}
		}
		return combinedOutp;
	}

}
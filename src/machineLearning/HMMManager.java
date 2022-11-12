package machineLearning;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import data.Dataset;
import data.Dataset.DatasetID;
import de.uos.fmt.musitech.data.structure.Note;
import representations.Tablature;
import representations.Transcription;
import tools.ToolBox;
import ui.Runner;
import utility.DataConverter;

public class HMMManager {

	public static String HMMPath = Runner.experimentsPath;
	private static boolean useFullSizeMapping;
	private static boolean storeAlsoAsSerialised = false;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HMMManager hMMManager = new HMMManager();

		// =============== USER-ADAPTABLE SETTINGS ===============
		// a. Choose dataset
//		DatasetID id = DatasetID.TAB_INT_3VV;
//		String id = DatasetID.INT_3vv; 
		String id = Dataset.THESIS_INT_4VV; 
//		String id = DatasetID.WTC_3VV;
//		String id = DatasetID.WTC_4VV;
		Dataset ds = new Dataset(id);
		boolean isTablatureCase = ds.isTablatureSet();  
		List<String> pieceNames = ds.getPieceNames(); 
		String vv = ds.getNumVoices() + Runner.voices;
		// b. Create a folder in results/HMM/ for the current occasion and the current experiment (optional) 
		String occasion = "Thesis - Copy 1/";
		String experiment = "optimisation/";

		// c. Determine settings
//		int boost = 10; // see generateObservationProbabilityMatrix() TODO EB
		int highestNumberOfVoicesAssumed = 4; // determines the size of the mappings
		useFullSizeMapping = false; // sets the size of the mappings to Transcription.MAXIMUM_NUMBER_OF_VOICES
		storeAlsoAsSerialised = false; // stores matrices and test data also as .xml file
//		boolean smooth = true; // TODO EB
		String configuration = "1-uni_TPM-uni_ISM/"; // WAS "1. Output (with uniform priors and transitions)"
//		String configuration = "2-uni_TPM-data_ISM/"; // WAS "2. Output (with prior probability matrix and uniform transitions)"
//		String configuration = "3-data_TPM-uni_ISM/"; // WAS "3. Output (with uniform priors)"
//		String configuration = "4-data_TPM-data_ISM/"; // WAS "4. Output (with prior probability matrix)"		
		
		// d. Set generate to true to generate and store dictionaries, matrices, and test data; set
		// to false to evaluate the results	
		boolean generate = true;
		boolean getSummaryOnly = false;

//		// e. Only if generate == false: choose the outputFolder and the configuration	
//		String outputFolder = "smoothed"; // WAS "Outputs (keeping ones)"
//		String outputFolder = "unsmoothed"; // WAS "Outputs (setting to zero)"
	
		// =======================================================
				
//		String smoothed = "smoothed/"; // TODO EB
//		if (!smooth) {
//			smoothed = "unsmoothed/";
//		}
		
//		String folderName = occasion + dataset + vv + experiment + "boost = " + boost + "/" + smoothed + configuration; // TODO EB
//		String folderName = occasion + dataset + vv + experiment;
//		String folderName = occasion + id.getName() + "/" + vv + "/" + experiment;
		String folderName = 
			"thesis/prl_2/" + ds.getName() + "/" + vv + "/" + "H/" + configuration; // TODO EB
		
//		String path = "F:/research/data" + HMMPath + folderName + "/data/";
		String path = Runner.experimentsPath + folderName + "data/"; 
		System.out.println(path);
		System.exit(0);

		if (generate) {
			hMMManager.generateDictionariesAndMatrices(new Dataset(id), path, /*boost,*/ highestNumberOfVoicesAssumed); // TODO EB
		}
		else {
//			hMMManager.evaluate(folderName, vv, /*boost, outputFolder,*/ configuration,
//				getSummaryOnly); // TODO EB
		}
	}

	

	
	
	


	/**
	 * For the given dataset, generates and stores
	 * 
	 * <ul><li> the chord dictionary </li>
	 *   <li> the mapping dictionary </li></ul>
	 * 
	 * and, for each fold (training set), <br><br> 
	 * 
	 *   <ul><li> the initial state matrix (ISM) </li>
	 *   <li> the observation probability matrix (OPM) </li>
	 *   <li> the transition probability matrix (TPM)</li>
	 *   <li> the test data (chords) </li></ul> 
	 * 
	 * @param dataset
	 * @param path
	 * @param highestNumberOfVoicesAssumed Determines the size of the mappings.
	 */
	private void generateDictionariesAndMatrices(Dataset dataset, String path, /*int boost,*/ 
		int highestNumberOfVoicesAssumed) {
		
		List<String> pieceNames = dataset.getPieceNames();

		// 1. Get the pieces
//		// NB: The tablature tuning is normalised to G
//		String encodingsDir = Runner.encodingsPath + dataset.getName() + "/" + dataset.getNumVoices() + Runner.voices + "/";
//		String midiDir = Runner.midiPath + dataset.getName() + "/" + dataset.getNumVoices() + Runner.voices + "/";
//		List<TablatureTranscriptionPair> pieces =
//			TablatureTranscriptionPair.createSetOfTablatureTranscriptionPairs(pieceNames, 
//			encodingsDir, midiDir, dataset.isTablatureSet());
		List<TablatureTranscriptionPair> pieces = new ArrayList<TablatureTranscriptionPair>();
		List<Tablature> allTabs = null;
		if (dataset.isTablatureSet()) {
			allTabs = dataset.getAllTablatures();
		}
		List<Transcription> allTrans = dataset.getAllTranscriptions();
		for (int i = 0; i < dataset.getNumPieces(); i++) {
			Tablature tab = null;
			if (dataset.isTablatureSet()) {
				tab = allTabs.get(i); 
			}
			pieces.add(new TablatureTranscriptionPair(tab, allTrans.get(i)));
		}
				
		// 2. Make the dictionaries and store them	
		// a. The chord dictionary    
		List<List<Integer>> chordDictionary = null; // generateChordDictionary(pieces);
		ToolBox.storeListOfListsAsCSVFile(chordDictionary, new File(path + "chord_dictionary.csv"));
		if (storeAlsoAsSerialised) {
			ToolBox.storeObjectBinary(chordDictionary, new File(path + "chord_dictionary.ser"));
		}
		// b. The mapping dictionary
		List<List<Integer>> mappingDictionary = null; // generateMappingDictionary(pieces, highestNumberOfVoicesAssumed);
		if (useFullSizeMapping) { 
			if (highestNumberOfVoicesAssumed < Transcription.MAX_NUM_VOICES) {
				int diff = Transcription.MAX_NUM_VOICES - highestNumberOfVoicesAssumed; 
				for (List<Integer> l : mappingDictionary) {
					for (int i = 0; i < diff; i++) {
						l.add(-1);
					}
				}
			}
		}
		ToolBox.storeListOfListsAsCSVFile(mappingDictionary, new File(path + "mapping_dictionary.csv"));
		if (storeAlsoAsSerialised) {
			ToolBox.storeObjectBinary(mappingDictionary, new File(path + "mapping_dictionary.ser"));
		}
		
		// 3. For each fold
		int numberOfFolds = pieceNames.size();
		for (int i = 0; i < numberOfFolds; i++) {
			int currFold = i + 1;
			String foldString = "fold_" + ToolBox.zerofy(currFold, 2) + "/";
			
			// 1. Determine the training pieces for the current fold
			List<TablatureTranscriptionPair> currTrainingPieces = new ArrayList<TablatureTranscriptionPair>();
			// Determine the index of the test piece. If fold = 0, the test piece is at the last index; if
			// fold = 1, it is at the last index - 1, etc.
			int testPieceIndex = numberOfFolds - currFold; 	  	
			for (int j = 0; j < pieces.size(); j++) {
				if (j != testPieceIndex) {  	    	    
					currTrainingPieces.add(pieces.get(j));
				}
			}

			// 2. Generate the matrices and store them 
			// a. The initial state matrix
			String ism = Runner.ISM + Runner.train;
			Integer[] currISM = null; 
//				generateInitialStateMatrix(currTrainingPieces,
//				mappingDictionary, highestNumberOfVoicesAssumed);
			Integer[][] currISMWrapped = new Integer[1][currISM.length];
			currISMWrapped[0] = currISM;
			ToolBox.storeMatrixAsCSVFile(currISMWrapped, new File(path + foldString + ism + ".csv"));
			if (storeAlsoAsSerialised) {
				ToolBox.storeObjectBinary(currISM, new File(path + foldString + ism + ".ser"));	
			}
			// b. The observation probability matrix 
			String opm = Runner.OPM + Runner.train;
			Integer[][] currOPM = null;
//				generateObservationProbabilityMatrix(currTrainingPieces, chordDictionary,
//				mappingDictionary, highestNumberOfVoicesAssumed/*, boost*/); // TODO EB
			ToolBox.storeMatrixAsCSVFile(currOPM, new File(path + foldString + opm + ".csv"));
			if (storeAlsoAsSerialised) {
				ToolBox.storeObjectBinary(currOPM, new File(path + foldString + opm + ".ser"));
			}
			// c. The transition probability matrix
			String tpm = Runner.TPM + Runner.train;
			Integer[][] currTPM = null;
//				generateTransitionProbabilityMatrix(currTrainingPieces, 
//				mappingDictionary, highestNumberOfVoicesAssumed);
			ToolBox.storeMatrixAsCSVFile(currTPM, new File(path + foldString + tpm + ".csv")); 
			if (storeAlsoAsSerialised) {
				ToolBox.storeObjectBinary(currTPM, new File(path + foldString + tpm + ".ser"));
			}
			
			// 3. Determine the test piece for the current fold, extract the test data (chords), 
			// and store them
			TablatureTranscriptionPair currTestPiece = pieces.get(testPieceIndex);
			Tablature currTab = currTestPiece.getTablature();
			Transcription currTrans = currTestPiece.getTranscription();
			List<List<Integer>> testData = new ArrayList<List<Integer>>();
			int numberOfChords = -1;
			List<List<Note>> chords = currTrans.getChords();
			// a. In the tablature case
			if (currTestPiece.getTablature() != null) {
				numberOfChords = currTab.getChords().size();
			}
			// b. In the non-tablature case
			else {
				numberOfChords = chords.size();
			}
			for (int j = 0; j < numberOfChords; j++) {
				List<Integer> pitchesInChord = null;
				// a. In the tablature case
				if (currTestPiece.getTablature() != null) {
					pitchesInChord = currTab.getPitchesInChord(j);
				}
				// b. In the non-tablature case
				else {
					pitchesInChord = Transcription.getPitchesInChord(chords.get(j));
//					pitchesInChord = currTrans.getPitchesInChord(j);
				}
				Collections.sort(pitchesInChord);
				testData.add(pitchesInChord);
			}
			String obs = Runner.observations + Runner.application;
			ToolBox.storeListOfListsAsCSVFile(testData, new File(path + foldString + obs + ".csv"));
			if (storeAlsoAsSerialised) {
				ToolBox.storeObjectBinary(testData, new File(path + foldString + obs + ".ser"));
			}
		}
	}


	static List<List<Double>> groundTruthVoiceLabels;
	static List<Integer[]> equalDurationUnisonsInfo;
	/**
	 * Calculates, for each piece in pieceNames, the error specifications and stores these, together with the
	 * predicted indices for that piece, in a file.
	 * 
	 * @param folderName
	 * @param vv
	 * @param boost
	 * @param outputFolder
	 * @param configuration
	 * @param isTablatureCase
	 * @param getSummaryOnly
	 */
	public static List<List<List<Integer>>> evaluate(int fold, String[] argPaths) {
				
//		String[] paths = Runner.getPaths();
//		String path = "F:/research/data/" + HMMPath + folderName + "/output/" + configuration; // TODO EB
		String path = argPaths[0];

//		Dataset dataset = Runner.getDataset();
//		List<String> pieceNames = dataset.getPieceNames();
		
//		int testPieceIndex = dataset.getNumPieces() - fold;
//		String pieceName = pieceNames.get(numberOfFolds - fold);
//		String pieceName = pieceNames.get(testPieceIndex);
				
		// 1. Get the predicted indices
//		String path = ExperimentRunner.pathPrefix + HMMPath + folderName + "/boost = " + boost + "/";
//		String path = ExperimentRunner.pathPrefix + HMMPath + folderName + "/output/" + configuration; // TODO EB
//		String suffix = "Output MATLAB/Voice assignment index fold " + fold + ".txt";
//		String suffix = "Voice assignment index fold " + fold + ".txt";
//		File outputFile = new File(path + outputFolder + "/" + configuration + "/" + suffix);
//		File outputFile = new File(path + "Voice assignment index fold " + fold + ".txt"); // TODO EB
		File outputFile = 
			new File(path + "fold_" + ToolBox.zerofy(fold, 2) + "/" + 
			Runner.outputs + "-" + Runner.application + ".csv");
//		List<Integer> predictedIndices2 = ToolBox.parseStringOfIntegers(contents, "\r\n");
		List<List<Integer>> predictedIndicesWrapped = 
			ToolBox.readCSVFile(ToolBox.readTextFile(outputFile), ",", false);
		List<Integer> predictedIndices = new ArrayList<Integer>();
		for (List<Integer> l : predictedIndicesWrapped) {
			predictedIndices.add(l.get(0));
		}

//		System.out.println("Predicted indices (1-based as in Matlab output):");
//		System.out.println(predictedIndices);
	
//		// Subtract 1 from all predicted indices
//		for (int j = 0; j < predictedIndices.size(); j++) {
//			int currValue = predictedIndices.get(j);
//			predictedIndices.set(j, currValue - 1);
//		}
//		System.out.println("Predicted indices (0-based):");
//		System.out.println(predictedIndices);
	
		// 2. Get ground truth voice labels
//		Transcription transcription = dataset.getAllTranscriptions().get(testPieceIndex);
//		List<List<Double>> groundTruthVoiceLabels = transcription.getVoiceLabels();
//		List<Integer[]> equalDurationUnisonsInfo = null;
//		if (!dataset.isTablatureSet()) {
//			equalDurationUnisonsInfo = transcription.getEqualDurationUnisonsInfo();
//		}
//		// a. In the tablature case
//		if (isTablatureCase) {
//			File encodingFile = new File(ExperimentRunner.rootDir + ExperimentRunner.encodingsDir + 
//				vv + pieceName + ".txt");
//			File encodingFile = new File(Runner.encodingsPath + vv + pieceName + ".txt"); 
//			File midiFile = 
//				new File(ExperimentRunner.rootDir + ExperimentRunner.tabMidiDir + vv + pieceName);
//			File midiFile = new File(Runner.midiPath + vv + pieceName);					
//			Transcription transcription = new Transcription(midiFile, encodingFile);
//			groundTruthVoiceLabels = transcription.getVoiceLabels();				
//		}
//		// b. In the non-tablature case
//		else {
//			File midiFile = 
//				new File(ExperimentRunner.rootDir + ExperimentRunner.bachWTCMidiDir + vv + pieceName);
//			File midiFile = new File(Runner.midiPath + "WTC/" + vv + pieceName);
//			Transcription transcription = new Transcription(midiFile, null);
//			groundTruthVoiceLabels = transcription.getVoiceLabels();
//			equalDurationUnisonsInfo = transcription.getEqualDurationUnisonsInfo();
//		}

//		List<List<Double>> groundTruthVoiceLabels = AuxiliaryTool.getStoredObject(new ArrayList<List<Double>>(),
//			new File(ExperimentRunner.pathPrefix + "Files out/Ground truth/Ground truth voice labels " + pieceName + ".xml"));   
//		List<Integer[]> equalDurationUnisonsInfo = null;
//		if (!isTablatureCase) {
//			equalDurationUnisonsInfo = AuxiliaryTool.getStoredObject(new ArrayList<Integer[]>(),
//			new File(ExperimentRunner.pathPrefix + "Files out/Ground truth/Equal duration unisons info " + pieceName + ".xml"));
//		}
				
		// 3. Get the mapping dictionary
//		List<List<Integer>> mappingDictionary = AuxiliaryTool.getStoredObject(new ArrayList<List<Integer>>(),
//			new File(ExperimentRunner.pathPrefix + HMMPath + folderName + "/boost = " + boost +
//			"/data/Mapping dictionary.xml"));
//		List<List<Integer>> mappingDictionary = 
//			ToolBox.getStoredObjectBinary(new ArrayList<List<Integer>>(),
//			new File("F/PhD/data/" + HMMPath + folderName + 
//			"/data/voiceAssignmentDictionary.ser"));
								
//		List<List<Integer>> mappingDictionary = 
//			ToolBox.getStoredObjectBinary(new ArrayList<List<Integer>>(),
//			new File(path + Runner.mappingDict + ".csv"));				
		File mappingDictFile = new File(path + Runner.mappingDict + ".csv");
		List<List<Integer>> mappingDictionary = 
			ToolBox.readCSVFile(ToolBox.readTextFile(mappingDictFile), ",", true);
				
		// 4. Evaluate and store
//		String evaluation = evaluate(predictedIndices, groundTruthVoiceLabels, mappingDictionary,
//			highestNumberOfVoicesActual, equalDurationUnisonsInfo);		
	
		// Convert predictedIndices into a List of voices
		List<List<Integer>> allPredictedVoices = null;
//			getVoicesFromListOfMappingIndices(predictedIndices, mappingDictionary);
//		System.out.println(allPredictedVoices);

		List<List<Integer>> assignmentErrors =
			ErrorCalculator.calculateAssignmentErrors(allPredictedVoices, 
			groundTruthVoiceLabels, null, null, equalDurationUnisonsInfo);
		
		List<List<List<Integer>>> testResults = new ArrayList<List<List<Integer>>>();
		testResults.add(assignmentErrors);
		testResults.add(allPredictedVoices);
		return testResults; 
				
//		int highestNumberOfVoicesActual = Integer.parseInt(vv.substring(0, 1));
//		int highestNumberOfVoicesActual = dataset.getHighestNumVoices();

//		ErrorFraction[] evalNN = 
//			EvaluationManager.getMetricsSingleFold(assignmentErrors, allPredictedVoices, 
//			null, groundTruthVoiceLabels, equalDurationUnisonsInfo);
				
				
//		String evaluation = 
////			ErrorCalculator.getErrorSpecifications(assignmentErrors, allPredictedVoices,
////			groundTruthVoiceLabels, equalDurationUnisonsInfo, highestNumberOfVoicesActual, false, false);
//			null;
		// TODO getErrorSpecifications() does not exist anymore. Look in saved file 
		//(below) what it did and reconstruct
		// Store
//		System.out.println(evaluation);
//		String configuration = "blablabla";
//		String s = 
////			"folderName = " + folderName + "\r\n" + // TODO EB
////			"boost = " + boost + "\r\n" + // TODO EB
////			outputFolder + "\r\n" + // TODO EB
//			"configuration = " + configuration.substring(0, configuration.length() - 1) + "\r\n" +	
//			"test set = " + pieceName +	"\r\n" + "\r\n" +
//			"predictedIndices = " + predictedIndices + "\r\n" + "\r\n" + 
//			evaluation;
//		String fileName = "Fold " + fold + " - application process record HMM " + pieceName;
//		String fileName = "Application process record HMM fold " + fold + " " + pieceName;
//		AuxiliaryTool.storeTextFile(s, new File(ExperimentRunner.pathPrefix + HMMPath + folderName + "/boost = " + 
//			boost + "/" + outputFolder + "/" + configuration + "/Evaluation files/" + fileName + ".txt"));
//		ToolBox.storeTextFile(s, new File(path + fileName + ".txt")); // TODO EB
		
		
//		else {
//			HMMManager hMMManager = new HMMManager();
//			hMMManager.getSummaryOverAllFolds(pieceNames, path, dataset.isTablatureSet());
//		}
	}


	private void getSummaryOverAllFolds(List<String> pieceNames, String path, boolean isTablatureCase) {
				
		List<Double> accNums = new ArrayList<Double>();
		List<Double> accDens = new ArrayList<Double>();
		List<Double> soundNums = new ArrayList<Double>();
		List<Double> soundDens = new ArrayList<Double>();
		List<Double> compNums = new ArrayList<Double>();
		List<Double> compDens = new ArrayList<Double>();
		
		File f = new File(path);
		String[] fileNames = f.list();
		
		// List the Application process record files in the correct order
		List<String> applProcRec = new ArrayList<String>();
		List<String> testPieceNames = new ArrayList<String>(pieceNames);
		Collections.reverse(testPieceNames);	
		for (String testPieceName : testPieceNames) {
			for (String s : fileNames) {
				if (s.startsWith("Application process record") && s.contains(testPieceName)) {
					applProcRec.add(s);
				}
			}
		}
		
//		for (String s: applProcRec) {
//			System.out.println(s);
//		}
//		System.exit(0);
		
		
		for (String s : applProcRec) {
//			if (s.startsWith("Application process record")) {
			File comp = new File(f + "/" + s);
			String content = ToolBox.readTextFile(comp);
			
			List<String> keys = new ArrayList<String>();
			keys.add("(strict) on the test set: ");
			keys.add("weighted average soundness per voice: ");
			keys.add("weighted average completeness per voice: ");
			
			for (int i = 0; i < keys.size(); i++) {
				String key = keys.get(i);
				int first = content.indexOf(key);
				int last = content.indexOf(" =", first);
				String frac = content.substring(first + key.length(), last);
				
				String[] fr = frac.split("/");
				double n = Double.parseDouble(fr[0].trim());
				double d = Double.parseDouble(fr[1].trim());
				
				// Accuracy
				if (i == 0) {
					// In tablature case: deal with possible halves
					if (isTablatureCase) {
						n *= 2;
						d *= 2;
					}
					accNums.add(d - n);
					accDens.add(d);
				}
				// Soundness
				else if (i == 1) {
					soundNums.add(n);
					soundDens.add(d);
				}
				// Completeness
				else {
					compNums.add(n);
					compDens.add(d);
				}
			}
//			}
		}

		double acc = ToolBox.sumListDouble(accNums)/ToolBox.sumListDouble(accDens);
		System.out.println(accNums);
		System.out.println(accDens);
		System.out.println("accuracy: " + acc);
		System.out.println();
		double snd = ToolBox.sumListDouble(soundNums)/ToolBox.sumListDouble(soundDens);
		System.out.println(soundNums);
		System.out.println(soundDens);		
		System.out.println("soundness: " + snd);
		System.out.println();
		double cmp = ToolBox.sumListDouble(compNums)/ToolBox.sumListDouble(compDens);
		System.out.println(compNums);
		System.out.println(compDens);		
		System.out.println("completeness: " + cmp);
		
		String pieces = "pieces" + "\r\n";
		for (String s : pieceNames) {
			pieces = pieces.concat("  " + s + "\r\n");
		}
		
		String details = "APPLICATION" + "\r\n";
		for (int i = 0; i < accNums.size(); i++) {
			int fold = i + 1;
			details = details.concat("fold = " + fold + "\r\n");
			int testPieceIndex = pieceNames.size() - fold;
			details = details.concat("testpiece = " + pieceNames.get(testPieceIndex) + "\r\n");
			details = details.concat("(1) accuracy" + "\r\n");
			details = details.concat("    " + accNums.get(i).intValue() + "/" + accDens.get(i).intValue() + " = " + accNums.get(i)/accDens.get(i) + "\r\n");
			details = details.concat("(2) soundness" + "\r\n");
			details = details.concat("    " + soundNums.get(i).intValue() + "/" + soundDens.get(i).intValue() + " = " + soundNums.get(i)/soundDens.get(i) + "\r\n");
			details = details.concat("(3) completeness" + "\r\n");
			details = details.concat("    " + compNums.get(i).intValue() + "/" + compDens.get(i).intValue() + " = " + compNums.get(i)/compDens.get(i) + "\r\n");
		}

		String values = "";
		values = values.concat("accuracy, numerators" + "\r\n");
		for (double d : accNums) {
			values = values.concat((int) d + "\r\n");
		}
		values = values.concat("accuracy, denominators" + "\r\n");
		for (double d : accDens) {
			values = values.concat((int) d + "\r\n");
		}
		values = values.concat("\r\n");
		values = values.concat("soundness, numerators" + "\r\n");
		for (double d : soundNums) {
			values = values.concat((int) d + "\r\n");
		}
		values = values.concat("soundness, denominators" + "\r\n");
		for (double d : soundDens) {
			values = values.concat((int) d + "\r\n");
		}
		values = values.concat("\r\n");
		values = values.concat("completeness, numerators" + "\r\n");
		for (double d : compNums) {
			values = values.concat((int) d + "\r\n");
		}
		values = values.concat("completeness, denominators" + "\r\n");
		for (double d : compDens) {
			values = values.concat((int) d + "\r\n");
		}
		
		List<Double> perc = new ArrayList<Double>();
		for (int i = 0; i < accNums.size(); i++) {
			perc.add(accNums.get(i)/accDens.get(i));
		}
		String percAndStdDev = "accuracy percentages for all folds" + "\r\n";
		for (double d : perc) {
			percAndStdDev = percAndStdDev.concat("" + d + "\r\n");
		}
		percAndStdDev = percAndStdDev.concat("standard deviation" + "\r\n");
		percAndStdDev = percAndStdDev.concat("" + ToolBox.stDev(perc) + "\r\n");
		
		String avg = "(weighted) averages over all folds" + "\r\n";
		avg = avg.concat("APPLICATION" + "\r\n");
		avg = avg.concat("acc" + "\t" + "sound" + "\t" + "comp" + "\r\n");
		String accStr;
		if (("" + acc).length() < 6) {
			accStr = "" + acc;
		}
		else {
			accStr = ("" + acc).substring(0, 6);
		}
		String sndStr;
		if (("" + snd).length() < 6) {
			sndStr = "" + snd; 
		}
		else {
			sndStr = ("" + snd).substring(0, 6);
		}
		String cmpStr;
		if (("" + cmp).length() < 6) {
			cmpStr = "" + cmp;
		}
		else {
			cmpStr = ("" + cmp).substring(0, 6);
		}
		avg = avg.concat(accStr + "\t" + sndStr + "\t" + cmpStr + "\r\n");
		
		// Store avgs 
		String s = pieces + "\r\n" + details + "\r\n" + values + "\r\n" + percAndStdDev + "\r\n" + avg;
		ToolBox.storeTextFile(s, new File(path + "Summary over all folds.txt")); 		
	}


	/**
	 * Evaluates the given List of predicted voice assignment indices.
	 * 
	 * @param predictedMappingIndices
	 * @param groundTruthVoiceLabels
	 * @param mappingDictionary
	 * @param highestNumberOfVoicesActual
	 * @param eqDurUnisonsInfo
	 * @return
	 */
	private String evaluate(List<Integer> predictedMappingIndices, List<List<Double>> groundTruthVoiceLabels, 
		List<List<Integer>> mappingDictionary, int highestNumberOfVoicesActual, List<Integer[]> eqDurUnisonsInfo) { 
		String errorSpecifications = null; 

		// Convert the List of indices into a List of voices
		List<List<Integer>> allPredictedVoices = null;
//			getVoicesFromListOfMappingIndices(predictedMappingIndices, mappingDictionary);
		System.out.println(allPredictedVoices);
		System.out.println(allPredictedVoices.size());
		
		// Calculate the assignment errors
		List<List<Integer>> assignmentErrors = ErrorCalculator.calculateAssignmentErrors(allPredictedVoices,
			groundTruthVoiceLabels, null, null, eqDurUnisonsInfo);
		// Determine the error specifications
		errorSpecifications = 
//			ErrorCalculator.getErrorSpecifications(assignmentErrors, allPredictedVoices,
//			groundTruthVoiceLabels, eqDurUnisonsInfo, highestNumberOfVoicesActual, false, false);
			null;
		// TODO getErrorSpecifications() does not exist anymore (see also other evaluate()
		// method)
		return errorSpecifications;
	}

}

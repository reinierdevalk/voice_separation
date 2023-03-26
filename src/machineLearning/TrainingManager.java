package machineLearning;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;

import data.Dataset;
import de.uos.fmt.musitech.data.score.NotationStaff;
import de.uos.fmt.musitech.data.score.NotationVoice;
import de.uos.fmt.musitech.data.structure.Note;
import de.uos.fmt.musitech.utility.math.Rational;
import featureExtraction.FeatureGenerator;
import featureExtraction.FeatureGeneratorChord;
import interfaces.PythonInterface;
import machineLearning.NNManager.ActivationFunction;
import representations.Tablature;
import representations.Transcription;
import tbp.RhythmSymbol;
import tbp.Symbol;
import tools.ToolBox;
import ui.Runner;
import ui.Runner.DecisionContext;
import ui.Runner.FeatureVector;
import ui.Runner.Model;
import ui.Runner.ModelType;
import ui.Runner.ModellingApproach;
import ui.Runner.ProcessingMode;
import ui.Runner.WeightsInit;
import utility.DataConverter;

public class TrainingManager {

	private List<List<List<Double>>> noteFeaturesPerPiece;
	private List<List<List<List<String>>>> MFVsPerPiece;
	private List<List<List<Double>>> voiceDurLabelsPerPiece;
	private List<List<List<Double>>> voiceDurLabelsGTPerPiece;
	private List<List<List<List<Double>>>> chordFeaturesPerPiece; 
	private List<List<List<List<Integer>>>> possibleVoiceAssignmentsAllChordsPerPiece;
	private List<List<Integer>> backwardsMappingPerPiece;
	private int highestNumVoicesAssumed;
	private List<List<Integer>> chordDictionary;
	private List<List<Integer>> mappingDictionary;
//	private List<List<List<Double>>> noteFeaturesPerFold; // ISMIR 2017
//	private List<List<List<Double>>> voiceLabelsPerFold; // ISMIR 2017
	private List<double[]> allNetworkOutputs;
	private List<Double> allHighestNetworkOutputs;
	private List<List<Integer>> allBestVoiceAssignments;
	private double smallestNetworkError;
//	private String allClassificationErrors;
	private List<Double> allClassificationErrors;
	private List<Double> allClassificationErrorsVal;
	private List<List<Double>> classificationErrorsAllFolds;
	private List<List<Double>> classificationErrorsValAllFolds;
	private List<Integer> optimalMetaCyclesAllFolds;
	
	int numNotesTotal = 0;
	int piecesTotal = 0;
	static boolean augment = false;
	static int augmentFactor = 2;
	boolean deduplicate = false;
//	Rational ornThresh = new Rational(1, 8);
	int ornThresh = RhythmSymbol.SEMIMINIM.getDuration();


	public void prepareTraining(String start) {		
		Map<String, Double> mp = Runner.getModelParams();
		boolean useCV = ToolBox.toBoolean(mp.get(Runner.CROSS_VAL).intValue());
		boolean trainUserModel = Runner.getTrainUserModel();
		ModellingApproach ma = Runner.ALL_MODELLING_APPROACHES[mp.get(Runner.MODELLING_APPROACH).intValue()];
		ProcessingMode pm = Runner.ALL_PROC_MODES[mp.get(Runner.PROC_MODE).intValue()];
		String[] paths = Runner.getPaths();
		String storePath = paths[0];
		String pathPredTransFirstPass = paths[1];
		Dataset dataset = Runner.getDataset();
		boolean isTablatureCase = dataset.isTablatureSet();
		int datasetSize = dataset.getNumPieces();		
		Model m = Runner.ALL_MODELS[mp.get(Runner.MODEL).intValue()]; 
		boolean modelDuration = m.getModelDuration();
		boolean modelDurationAgain = ToolBox.toBoolean(mp.get(Runner.MODEL_DURATION_AGAIN).intValue());
		ModelType mt = m.getModelType();
		DecisionContext dc = m.getDecisionContext();
		int maxMetaCycles = mt != ModelType.DNN ? mp.get(Runner.META_CYCLES).intValue() : -1;
		int highestNumberOfVoices =	Runner.getHighestNumVoicesTraining(Runner.getDeployTrainedUserModel());
		int valPerc = mp.get(Runner.VALIDATION_PERC).intValue();
		boolean bidirAsInThesis = true;
		boolean firstPassIsBwd = // TODO add mFirstPass and pmFirstPass to UI
			dc == DecisionContext.BIDIR && pathPredTransFirstPass.contains("bwd");

		classificationErrorsAllFolds = new ArrayList<>();
		classificationErrorsValAllFolds = new ArrayList<>();
		optimalMetaCyclesAllFolds = new ArrayList<>();
		
		// Initialise the super-superlists, representing the entire dataset, with nulls
		if (ma != ModellingApproach.HMM) { 
			noteFeaturesPerPiece = new ArrayList<List<List<Double>>>();
			voiceDurLabelsPerPiece = new ArrayList<List<List<Double>>>();
			voiceDurLabelsGTPerPiece = new ArrayList<List<List<Double>>>();
//			noteFeaturesPerFold = new ArrayList<List<List<Double>>>(); // ISMIR 2017
//			voiceLabelsPerFold = new ArrayList<List<List<Double>>>(); // ISMIR 2017
			chordFeaturesPerPiece = new ArrayList<List<List<List<Double>>>>();
			possibleVoiceAssignmentsAllChordsPerPiece = new ArrayList<List<List<List<Integer>>>>();
			backwardsMappingPerPiece = new ArrayList<List<Integer>>();
			MFVsPerPiece = new ArrayList<List<List<List<String>>>>();
//			for (int k = 0; k < (!augment ? datasetSize : datasetSize*augmentFactor); k++) {
//				noteFeaturesPerPiece.add(null);
//				voiceDurLabelsPerPiece.add(null);
//				voiceDurLabelsGTPerPiece.add(null);
//				chordFeaturesPerPiece.add(null);
//				possibleVoiceAssignmentsAllChordsPerPiece.add(null);
//				backwardsMappingPerPiece.add(null);
//				MFVsPerPiece.add(null);
//			}
		}

		// Create the complete training set
		List<TablatureTranscriptionPair> allPieces = new ArrayList<TablatureTranscriptionPair>();
		List<String> pieceNames = dataset.getPieceNames();
		List<Integer> pieceSizes = dataset.getIndividualPieceSizes(ma);
		List<List<List<Double>>> voiceLabelsGTPerPiece = new ArrayList<List<List<Double>>>();
		System.out.println("pieceSizes: " + pieceSizes);
		System.out.println(ToolBox.sumListInteger(pieceSizes));
		for (int i = 0; i < datasetSize; i++) {
			String currPieceName = pieceNames.get(i);
//			System.out.println(currPieceName);
			Tablature currTab = 
				dataset.isTablatureSet() ? dataset.getAllTablatures().get(i) : null;			
			Transcription currTranscr = dataset.getAllTranscriptions().get(i);
			voiceLabelsGTPerPiece.add(currTranscr.getVoiceLabels());
			
			numNotesTotal += currTab != null ? currTab.getNumberOfNotes() :
				currTranscr.getNumberOfNotes();
			piecesTotal += 1;
			
			// Make TablatureTranscriptionPairs
			TablatureTranscriptionPair currTabTransPair = null;
			TablatureTranscriptionPair currTabTransPairRev = null;
			TablatureTranscriptionPair currTabTransPairDeorn = null;
			TablatureTranscriptionPair currTabTransPairRevDeorn = null;
			if (dc == DecisionContext.UNIDIR) {
				currTabTransPair = new TablatureTranscriptionPair(currTab, currTranscr);
				if (augment) {
					System.out.println("augmenting " + currTab.getName());
						
					// Reversed
					System.out.println("R E V E R S I N G");									
					Tablature currTabRev = new Tablature(currTab); 
					currTabRev.augment(-1, -1, "reverse");
//					currTabRev.reverse();
//					Tablature currTabRev = Tablature.reverse(currTab);
		
					Transcription currTranscrRev = new Transcription(currTranscr);
					currTranscrRev.augment(currTabRev.getEncoding(), null, -1, "reverse");
//					Transcription currTranscrRev = Transcription.reverse(currTranscr, currTab);
					System.out.println("first of given:");
					System.out.println("undapted: " + currTranscr.getUnaugmentedScorePiece().getScore().get(0).get(0).get(0).get(0));
					System.out.println("apted:    " + currTranscr.getScorePiece().getScore().get(0).get(0).get(0).get(0));
					System.out.println("first of reversed:");
					System.out.println("undapted: " + currTranscrRev.getUnaugmentedScorePiece().getScore().get(0).get(0).get(0).get(0));
					System.out.println("apted:    " + currTranscrRev.getScorePiece().getScore().get(0).get(0).get(0).get(0));
						
					// Deornamented
					System.out.println("D E O R N A M E N T I N G");										
					Tablature currTabDeorn = new Tablature(currTab);
					currTabDeorn.augment(ornThresh, -1, "deornament");
//					currTabDeorn.deornament(ornThresh);
//					Tablature currTabDeorn = Tablature.deornament(currTab, ornThresh);
					
					Transcription currTranscrDeorn = new Transcription(currTranscr);
					currTranscrDeorn.augment(currTabDeorn.getEncoding(), 
						new Rational(ornThresh, Symbol.BREVIS.getDuration()), -1, "deornament");
//					Transcription currTranscrDeorn = Transcription.deornament(currTranscr, currTab, ornThresh);
					// Reversed and deornamented
					System.out.println("R E V E R S E  +  D E O R N A M E N T");
					Tablature currTabDeornRev = new Tablature(currTabDeorn);
					currTabDeornRev.augment(-1, -1, "reverse");
//					currTabDeornRev.reverse();

//					for (int ii = 35; ii < 40; ii++) {
//					System.out.println(Arrays.asList(currTabDeorn.getBasicTabSymbolProperties()[ii]));
//					}
//					System.out.println("----");	
//					for (int ii = 35; ii < 40; ii++) {
//					System.out.println(Arrays.asList(currTabDeorn.getBasicTabSymbolProperties()[ii]));
//					}
//					System.out.println("----");	
//					for (int ii = 35; ii < 40; ii++) {
//					System.out.println(Arrays.asList(currTabRevDeorn.getBasicTabSymbolProperties()[ii]));
//					}
//					System.out.println("----");	
//					System.exit(0);

					Transcription currTranscrDeornRev = new Transcription(currTranscrDeorn);
					currTranscrDeornRev.augment(currTabDeornRev.getEncoding(), null, -1, "reverse");
//					Tablature currTabRevDeorn = currTabDeorn.reverse();
//					Tablature currTabRevDeorn = Tablature.reverse(currTabDeorn);
//					Transcription currTranscrDeornRev = 
//						Transcription.reverse(currTranscrDeorn, currTabDeorn);

					System.out.println("first of given:");
					System.out.println("undapted: " + currTranscr.getUnaugmentedScorePiece().getScore().get(0).get(0).get(0).get(0));
					System.out.println("adapted:  " + currTranscr.getScorePiece().getScore().get(0).get(0).get(0).get(0));
					System.out.println("first of deornamented:");
					System.out.println("undapted: " + currTranscrDeorn.getUnaugmentedScorePiece().getScore().get(0).get(0).get(0).get(0));
					System.out.println("adapted:  " + currTranscrDeorn.getScorePiece().getScore().get(0).get(0).get(0).get(0));
					System.out.println("first of reversed:");
					System.out.println("undapted: " + currTranscrRev.getUnaugmentedScorePiece().getScore().get(0).get(0).get(0).get(0));
					System.out.println("apted:    " + currTranscrRev.getScorePiece().getScore().get(0).get(0).get(0).get(0));
					System.out.println("first of reversed and deornamented:");
					System.out.println("undapted: " + currTranscrDeornRev.getUnaugmentedScorePiece().getScore().get(0).get(0).get(0).get(0));
					System.out.println("apted:    " + currTranscrDeornRev.getScorePiece().getScore().get(0).get(0).get(0).get(0));
					System.out.println("first of deornamented:");
					System.out.println("undapted: " + currTranscrDeorn.getUnaugmentedScorePiece().getScore().get(0).get(0).get(0).get(0));
					System.out.println("adapted:  " + currTranscrDeorn.getScorePiece().getScore().get(0).get(0).get(0).get(0));

					// implement transposition
						
					currTabTransPairRev = 
						new TablatureTranscriptionPair(currTabRev, currTranscrRev);
					currTabTransPairDeorn = 
						new TablatureTranscriptionPair(currTabDeorn, currTranscrDeorn);					
					currTabTransPairRevDeorn = 
						new TablatureTranscriptionPair(currTabDeornRev, currTranscrDeornRev);
				}
			}
			if (dc == DecisionContext.BIDIR) {
				Transcription predTranscr = null;
				if (m == Model.B_STAR || m == Model.B_PRIME_STAR) {
					predTranscr = currTranscr;
				}
				else {
					int fold = i+1;
					int testPieceIndex = datasetSize - i;
//					int testPieceIndex = datasetSize - (i+1);
					if (bidirAsInThesis) {
						String foldStr = 
							"fold_" + ToolBox.zerofy(testPieceIndex, ToolBox.maxLen(testPieceIndex));
						predTranscr = 
							ToolBox.getStoredObjectBinary(new Transcription(), 
							new File(pathPredTransFirstPass + Runner.output + foldStr + 
							"-" + currPieceName + ".ser"));
					}
					else {
						predTranscr = null;
					}
//					System.out.println("i = " + i);
//					System.out.println(pathPredTransFirstPass);
//					System.out.println(Runner.output + "fold_" +
//						ToolBox.zerofy(testPieceIndex, ToolBox.maxLen(testPieceIndex)) + "-" + 
//						currPieceName + ".ser");
//					System.out.println(predTranscr);
//					List<List<Double>> vl = predTranscr.getVoiceLabels();
//					System.out.println("currPieceName = " + currPieceName);
//					System.out.println("first ten ----");
//					for (int ii = 0; ii < 10; ii++) {
//						System.out.println(vl.get(ii));
//					}
//					System.out.println("last five ----");
//					for (int ii = vl.size() - 5; ii < vl.size(); ii++) {
//						System.out.println(vl.get(ii));
//					}
//					System.out.println("############");
				}
				currTabTransPair = 
					new TablatureTranscriptionPair(currTab, predTranscr, currTranscr);
			}
			allPieces.add(currTabTransPair);
			if (augment) {
				allPieces.add(currTabTransPairRev);
//				allPieces.add(currTabTransPairDeorn);
//				allPieces.add(currTabTransPairRevDeorn);
			}

			// Fill superlists
			// NB: When using the unidir model, currTrans (and currVoiceLabels, currDurLabels, 
			// currVoicesCoDNotes) are GT; when using the bidir model, they are predicted, and 
			// currTransGT (and currVoiceLabelsGT, currDurLabelsGT, currVoicesCoDNotesGT) are GT
			currTab = currTabTransPair.getTablature();
			Integer[][] currBTP = isTablatureCase ? currTab.getBasicTabSymbolProperties() : null;
			//
			Transcription currTrans = currTabTransPair.getTranscription();
			Transcription currTransGT = 
				dc == DecisionContext.BIDIR ? currTabTransPair.getSecondTranscription() : null;
			// Non-voice information is the same for currTrans and currTransGT, and can be
			// taken from both
			Integer[][] currBNP = isTablatureCase ? null : currTrans.getBasicNoteProperties(); // was currTransGT.getBasicNoteProperties() 06.05	
			List<Integer> currChordSizes = 
				isTablatureCase ? currTab.getNumberOfNotesPerChord() : currTrans.getNumberOfNewNotesPerChord(); // was currTransGT.getNumberOfNewNotesPerChord() 06.05	
			List<Integer[]> currMeterInfo = 
//				isTablatureCase ? currTab.getTimeline().getMeterInfoOBS() : currTrans.getMeterInfo(); // was currTransGT.getMeterInfo() 06.05	
				isTablatureCase ? currTab.getMeterInfo() : currTrans.getMeterInfo(); // was currTransGT.getMeterInfo() 06.05	
			// Voice information is different for currTrans and currTransGT, and must be 
			// taken from the former
			List<List<Double>> currVoiceLabels = null;
			List<List<Double>> currDurLabels = null;
			List<Integer[]> currVoicesCoDNotes = null;
			if (dc == DecisionContext.UNIDIR || bidirAsInThesis) {
				currVoiceLabels = currTrans.getVoiceLabels();
				currDurLabels = 
					isTablatureCase && modelDuration ? currTrans.getDurationLabels() : null;
				currVoicesCoDNotes = 
					isTablatureCase && modelDuration ? currTrans.getVoicesSNU() : null;
			}
			// 
			List<List<Double>> currVoiceLabelsGT = 
				dc == DecisionContext.BIDIR ? currTransGT.getVoiceLabels() : null;
			List<List<Double>> currDurLabelsGT = 
				dc == DecisionContext.BIDIR ? currTransGT.getDurationLabels() : null;
			if (ma == ModellingApproach.N2N) {
				// Labels
				// a. Voice-duration labels
				List<List<Double>> currVoiceDurLabels = new ArrayList<List<Double>>();
				if (dc == DecisionContext.UNIDIR || bidirAsInThesis) {
					if (isTablatureCase && modelDuration) {
						for (int j = 0; j < currVoiceLabels.size(); j++) {
							List<Double> currVoiceDurLabel = new ArrayList<Double>(currVoiceLabels.get(j));
							currVoiceDurLabel.addAll(currDurLabels.get(j));
							currVoiceDurLabels.add(currVoiceDurLabel);  	
						}
						voiceDurLabelsPerPiece.add(currVoiceDurLabels);
					}
				}
				// b. Ground truth voice-duration labels (bidir model)
				if (dc == DecisionContext.BIDIR) {
					if (isTablatureCase && modelDuration && modelDurationAgain) {
						List<List<Double>> currVoiceDurLabelsGT = new ArrayList<List<Double>>();
						for (int j = 0; j < currVoiceLabelsGT.size(); j++) {
							List<Double> currVoiceDurLabelGT = new ArrayList<Double>(currVoiceLabelsGT.get(j));
							currVoiceDurLabelGT.addAll(currDurLabelsGT.get(j));
							currVoiceDurLabelsGT.add(currVoiceDurLabelGT);  	
						}
						voiceDurLabelsGTPerPiece.add(currVoiceDurLabelsGT);
					}					
				}
				// Features
				if (dc == DecisionContext.UNIDIR || bidirAsInThesis) {
					if (mt == ModelType.NN || mt == ModelType.DNN || mt == ModelType.OTHER) {
						// NB: When using the bwd model, the features will be ordered accordingly; this is done inside
						// generateAllNoteFeatureVectors(), so all arguments can remain in their original (fwd) order
						// and the features will be returned in bwd order
						// TODO make single call to generateAllNoteFeatureVectors() that decides which one to call
						// based on given featVec
						List<List<Double>> currNoteFeatures = null;
						if (dc == DecisionContext.UNIDIR) {
							currNoteFeatures =
								FeatureGenerator.generateAllNoteFeatureVectors(
								mp, currBTP, currVoicesCoDNotes, currBNP, currTrans, 
								isTablatureCase && modelDuration ? currVoiceDurLabels : currVoiceLabels,
//								currLabels, 
								currMeterInfo, currChordSizes);
						}
						else {
							currNoteFeatures = 
								FeatureGenerator.generateAllBidirectionalNoteFeatureVectors(
								mp, currBTP, currVoicesCoDNotes, currBNP, currTrans, 
								isTablatureCase && modelDuration ? currVoiceDurLabels : currVoiceLabels,
//								currLabels,
								currMeterInfo, currChordSizes);
						}
						noteFeaturesPerPiece.add(currNoteFeatures);
					}
				}
				// Melody feature vectors
				if (mt == ModelType.MM) {
					List<List<List<String>>> currMFVs = new ArrayList<List<List<String>>>();
					// For each voice in currentTranscription
					for (NotationStaff nst : currTrans.getScorePiece().getScore()) {
						NotationVoice nv = nst.get(0);
						// Only if nv contains notes
						if (nv.size() != 0) {
							List<List<Double>> MFVsCurrVoice = 
								FeatureGenerator.generateMelodyFeatureVectors(currBTP, nv, null);
							currMFVs.add(ToolBox.convertToListString(MFVsCurrVoice));
						}
					}
					MFVsPerPiece.add(currMFVs);
				}
				// Backwards mappings
				if (pm == ProcessingMode.BWD || firstPassIsBwd) {
					backwardsMappingPerPiece.add(FeatureGenerator.getBackwardsMapping(currChordSizes));
				}
			}
			if (ma == ModellingApproach.C2C) {
				// Possible voice assignments 
				List<List<List<Integer>>> currPossibleVoiceAssignmentsAllChords =
					FeatureGeneratorChord.getOrderedVoiceAssignments(currBTP, currBNP,
					currVoiceLabels, highestNumberOfVoices);
				possibleVoiceAssignmentsAllChordsPerPiece.add(currPossibleVoiceAssignmentsAllChords);
				// Features
				List<List<List<Double>>> currentChordFeatures =  
					FeatureGeneratorChord.generateAllChordFeatureVectors(currBTP,
					currBNP, currTrans, currMeterInfo, currPossibleVoiceAssignmentsAllChords);
				chordFeaturesPerPiece.add(currentChordFeatures);
			}
		}
		
		// For each piece, get the predicted outputs for that piece for each fold (or null 
		// if the piece is the testpiece for the fold)
		// predOutpAllFoldsPerPiece has as many elements as there are pieces in the dataset, 
		// and contains per element (piece) the predicted outputs for that piece for each 
		// fold (or null if the piece is the testpiece for the fold)
		List<List<List<double[]>>> predOutpAllFoldsPerPiece = new ArrayList<>();
		if (dc == DecisionContext.BIDIR && !bidirAsInThesis) {
			for (int i = 0; i < datasetSize; i++) {
				// For each fold, get the predicted outputs for the current piece
				List<List<double[]>> predOutpAllFoldsCurrPiece = new ArrayList<>();
				for (int fold = 1; fold <= datasetSize; fold++) {
					int testPieceFold = (datasetSize - fold) + 1;
					int testPieceInd = testPieceFold - 1;
//					System.out.println("fold = " + fold + ";testPieceFold = " + testPieceFold);
//					if (fold == testPieceFold) {
//						predOutpAllFoldsCurrPiece.add(null);
//					}
//					else {
					String path = 
						pathPredTransFirstPass + "fold_" + ToolBox.zerofy(fold, 
						ToolBox.maxLen(fold)) + "/";
					List<Integer> pieceSizesCurrFold = new ArrayList<>(pieceSizes);
					pieceSizesCurrFold.remove(testPieceInd);
						
					// a. Get predicted outputs for all pieces in current fold
					String[][] outpTrnCsv = 
						ToolBox.retrieveCSVTable(ToolBox.readTextFile(
						new File(path + Runner.outpExt + Runner.train + ".csv")));
					List<double[]> predOutpTrn = ToolBox.convertCSVTable(outpTrnCsv);
					List<double[]> predOutpVld = null;
					List<double[]> predOutpComb = null;
					if (valPerc == 0) {
						predOutpComb = new ArrayList<>(predOutpTrn);
					}
					else {
						String[][] outpVldCsv = 
							ToolBox.retrieveCSVTable(ToolBox.readTextFile(
							new File(path + Runner.outpExt + Runner.validation + ".csv")));
						predOutpVld = ToolBox.convertCSVTable(outpVldCsv);
						predOutpComb = reintegrateVldData(predOutpTrn, predOutpVld, valPerc);
					}
					// Split predOutpComb into individual pieces
					List<List<double[]>> predOutpCombPerPiece = new ArrayList<>();
					int from = 0;
					for (int size : pieceSizesCurrFold) {
						predOutpCombPerPiece.add(predOutpComb.subList(from, from + size));
						from += size;
					}
					// Reverse bwd mapping (if applicable) 
					if (firstPassIsBwd) {
						List<List<Integer>> bwdMapCurrFold = new ArrayList<>(backwardsMappingPerPiece);
						bwdMapCurrFold.remove(testPieceInd);
						// Reorder predicted outputs for each piece into fwd order
						List<List<double[]>> reordered = new ArrayList<>(); 
						for (int j = 0; j < predOutpCombPerPiece.size(); j++) {
							reordered.add(ToolBox.reorderByIndex(predOutpCombPerPiece.get(j), 
							bwdMapCurrFold.get(j)));
						}
						predOutpCombPerPiece = reordered;
					}
					// Insert null for the fold in which the piece is the tst piece
					predOutpCombPerPiece.add(testPieceInd, null);

					// b. Add predicted outputs for current piece in current fold to list
					predOutpAllFoldsCurrPiece.add(predOutpCombPerPiece.get(i));

					// c. Perform check
					// Flatten predictedOutputsComb
					List<double[]> predOutpCombFlat = new ArrayList<>();
					for (List<double[]> l : predOutpCombPerPiece) {
						if (l != null) {
							predOutpCombFlat.addAll(l);
						}
					}
					// Get flattened voice labels for training pieces current fold							
					List<List<Double>> currVoiceLabelsGTPerPieceFlat = new ArrayList<>();
					for (int j = 0; j < voiceLabelsGTPerPiece.size(); j++) {
						if (j+1 != testPieceFold) {
							currVoiceLabelsGTPerPieceFlat.addAll(voiceLabelsGTPerPiece.get(j));
						}
					}
					// Check predOutpCombFlat is made by combining out-trn.csv and
					// out-vld.csv; the check is done by (i) calculating total acc on the 
					// combined result (using GT labels); (ii) extracting total acc from
					// details-trn.csv and best_epoch.csv (which were made independently
					// from out-trn.csv and out-vld.csv); and (iii) comparing the accs
					String[][] detailsTrnCsv = 
						ToolBox.retrieveCSVTable(ToolBox.readTextFile(
						new File(path + Runner.details + "-" + Runner.train + ".csv")));
					String[][] bestEpochCsv = 
						ToolBox.retrieveCSVTable(ToolBox.readTextFile(
						new File(path + "best_epoch.csv")));
					String[][] yVldCsv =
						ToolBox.retrieveCSVTable(ToolBox.readTextFile(
						new File(path + Runner.lblExt + Runner.validation + ".csv")));
					List<double[]> yVld = ToolBox.convertCSVTable(yVldCsv);
					boolean checkPassed = 
						checkIntegration(detailsTrnCsv, bestEpochCsv,
						predOutpVld, yVld, // both in bwd order (if applicable)
						predOutpCombFlat, // in fwd order
						currVoiceLabelsGTPerPieceFlat); // in fwd order
					if (!checkPassed) {
						System.exit(0);
					}
					
					boolean dothis = false;
					if (dothis) {
						// Save the combined predicted output as labels
						List<List<Double>> asList = new ArrayList<>();
//						List<List<Double>> predictedLabelsTrnAndVld = new ArrayList<>();
						for (double[] d : predOutpComb) {
//							List<Integer> predVoices = 
//								OutputEvaluator.interpretNetworkOutput(d, 
//								isTablatureCase ? true : false, 
//							modelParameters.get(Runner.DEV_THRESHOLD)).get(0);
//							predictedLabelsTrnAndVld.add(DataConverter.convertIntoVoiceLabel(predVoices));
							asList.add(Arrays.asList(ArrayUtils.toObject(d)));
						}
						ToolBox.storeListOfListsAsCSVFile(asList, new File(storePath + 
							Runner.outpExt + Runner.train + "-" + Runner.validation + ".csv"));
					}	
//					}
				}
				predOutpAllFoldsPerPiece.add(predOutpAllFoldsCurrPiece);
			}
			System.out.println("----------");
			System.out.println(predOutpAllFoldsPerPiece.size()); // 13
			for (int j = 0; j < predOutpAllFoldsPerPiece.size(); j++) {
				System.out.println(predOutpAllFoldsPerPiece.get(j).size()); // piece; 13
				String s = "";
				for (int k = 0; k < predOutpAllFoldsPerPiece.get(j).size(); k++) {
					if (predOutpAllFoldsPerPiece.get(j).get(k) != null) { // outp for piece
						s += predOutpAllFoldsPerPiece.get(j).get(k).size() + " "; 
					}
					else {
						s += "null ";
					}
				}
				System.out.println(s);
			}
			System.exit(0);
			// Turn pred outp into transcriptions to be set in for-loop below (non-CV and CV)
		}
		

		// Create the chord and mapping dictionaries
		if (ma == ModellingApproach.HMM) {
			highestNumVoicesAssumed = Transcription.MAX_NUM_VOICES;
			boolean useFullSizeMapping = false;
			chordDictionary = generateChordDictionary(allPieces);
			ToolBox.storeListOfListsAsCSVFile(chordDictionary, new File(storePath + Runner.chordDict + ".csv"));
//			ToolBox.storeObjectBinary(chordDictionary, new File(paths[0] + Runner.chordDict + ".ser"));
			mappingDictionary = generateMappingDictionary(allPieces, highestNumVoicesAssumed);
			if (useFullSizeMapping) { 
				if (highestNumVoicesAssumed < Transcription.MAX_NUM_VOICES) {
					int diff = Transcription.MAX_NUM_VOICES - highestNumVoicesAssumed; 
					for (List<Integer> l : mappingDictionary) {
						for (int i = 0; i < diff; i++) {
							l.add(-1);
						}
					}
				}
			}
			ToolBox.storeListOfListsAsCSVFile(mappingDictionary, new File(storePath + Runner.mappingDict + ".csv"));
//			ToolBox.storeObjectBinary(mappingDictionary, new File(paths[0] + Runner.mappingDict + ".ser"));
		}

		// Start training
		String trPreProcTime = 
			String.valueOf(ToolBox.getTimeDiffPrecise(start, ToolBox.getTimeStampPrecise()));
		if (!useCV) {
			String startFoldTr = ToolBox.getTimeStampPrecise();
			startTrainingProcess(0, allPieces, -1, paths, new String[]{trPreProcTime, startFoldTr});
		}
		else {
			for (int k = 1; k <= datasetSize; k++) {
				String startFoldTr = ToolBox.getTimeStampPrecise();
				System.out.println("fold = " + k);
				// Add foldstring to paths
				String[] currPaths = new String[paths.length];
				for (int i = 0; i < paths.length; i++) {
					String s = paths[i];
					if (s != null) {
						currPaths[i] = s;
						// Add fold for all cases but MM path for ENS model  
						if (!(mt == ModelType.ENS && i == 2)) {
							currPaths[i] += "fold_" + ToolBox.zerofy(k, ToolBox.maxLen(k)) + "/";
						}
					}
				}

				// Make the training set for the current fold
				int testPieceIndex = datasetSize - k;
				int valPieceIndex = ((datasetSize-k-1) + datasetSize) % datasetSize;
				if (augment) {
					testPieceIndex = augmentFactor*datasetSize - augmentFactor*k;
					valPieceIndex = (( (augmentFactor*datasetSize)-(augmentFactor*k)-augmentFactor) 
						+ (augmentFactor*datasetSize)) % (augmentFactor*datasetSize);
				}
//				System.out.println("testPieceIndex = " + testPieceIndex);
				List<TablatureTranscriptionPair> trainingPieces = new ArrayList<TablatureTranscriptionPair>();
//				List<Integer> testPieceIndices = new ArrayList<>();
//				for (int i = 0; i < augmentFactor; i++) {
//					testPieceIndices.add(testPieceIndex + i);
//				}
				List<Integer> testPieceIndices = 
					IntStream.rangeClosed(testPieceIndex, 
					((testPieceIndex + augmentFactor)-1)).boxed().collect(Collectors.toList());
				for (int l = 0; l < allPieces.size(); l++) {
					if (!augment && l != testPieceIndex || 
						augment && !testPieceIndices.contains(l)) { // --> remove the if to fake non-cross-validation (for pre-testing, on a single piece, whether training and testing yield the same result)
						trainingPieces.add(allPieces.get(l));
					} // <-- remove the if to fake non-cross-validation
				}
				startTrainingProcess(k, trainingPieces, testPieceIndex, currPaths, 
					new String[]{trPreProcTime, startFoldTr});
			}
		}

		if (!trainUserModel) {
			List<List<Double>> classErrPerMetaCyclePerFold = new ArrayList<>();
			List<List<Double>> classErrValPerMetaCyclePerFold = new ArrayList<>();
			// For each metaCycle: get value for each fold
			for (int i = 0; i < maxMetaCycles; i++) {
				List<Double> currMetaCycle = new ArrayList<>();
				List<Double> currMetaCycleVal = new ArrayList<>();
				for (int j = 0; j < datasetSize; j++) {
					currMetaCycle.add(classificationErrorsAllFolds.get(j).get(i));
					currMetaCycleVal.add(classificationErrorsValAllFolds.get(j).get(i));
				}
				classErrPerMetaCyclePerFold.add(currMetaCycle);
				classErrValPerMetaCyclePerFold.add(currMetaCycleVal);
			}

			// For each metaCycle: average
			List<Double> avgClassErrPerMetaCycle = new ArrayList<>();
			List<Double> avgClassErrValPerMetaCycle = new ArrayList<>();
			for (int i = 0; i < maxMetaCycles; i++) {
				avgClassErrPerMetaCycle.add(ToolBox.getAverage(classErrPerMetaCyclePerFold.get(i)));
				avgClassErrValPerMetaCycle.add(ToolBox.getAverage(classErrValPerMetaCyclePerFold.get(i)));
			}

			String avgErrs = "avg trn and val errors" + "\r\n";
			for (int i = 0; i < avgClassErrPerMetaCycle.size(); i++) {
				avgErrs += i + "\t" + avgClassErrPerMetaCycle.get(i) + "\t" + 
					avgClassErrValPerMetaCycle.get(i) + "\r\n";  
			}
			System.out.println(avgErrs);
			System.out.println();
			System.out.println("optimal metaCycles" + "\r\n");
			System.out.println(optimalMetaCyclesAllFolds);
		}
	}


	/**
	 * 
	 * @param fold Fold number when using cross-validation; 0 when not.
	 * @param testPieceIndex 
	 * @param trainingPieces
	 * @param argPaths
	 * @param times 
	 */
	private void startTrainingProcess(int fold, List<TablatureTranscriptionPair> trainingPieces, 
		int testPieceIndex, String[] argPaths, String[] times) {
		long trPreProcTime = Integer.parseInt(times[0]);
		String start = times[1];

		Map<String, Double> modelParameters = Runner.getModelParams();		
		boolean modelDurationAgain = 
			ToolBox.toBoolean(modelParameters.get(Runner.MODEL_DURATION_AGAIN).intValue());
		int highestNumberOfVoices =	
			Runner.getHighestNumVoicesTraining(Runner.getDeployTrainedUserModel());
		int valPerc = modelParameters.get(Runner.VALIDATION_PERC).intValue();
		ModellingApproach ma = 
			Runner.ALL_MODELLING_APPROACHES[modelParameters.get(Runner.MODELLING_APPROACH).intValue()];
		ProcessingMode pm = 
			Runner.ALL_PROC_MODES[modelParameters.get(Runner.PROC_MODE).intValue()];

		Dataset dataset = Runner.getDataset();
		boolean isTablatureCase = dataset.isTablatureSet();
		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()]; 
		ModelType mt = m.getModelType();
		boolean modelDuration = m.getModelDuration();
		DecisionContext dc = m.getDecisionContext();
//		int decisionContextSize = modelParameters.get(Runner.DECISION_CONTEXT_SIZE).intValue();
		boolean verbose = Runner.getVerbose();
		List<Integer> sliceIndices = 
			(mt == ModelType.MM || mt == ModelType.ENS) ? ToolBox.decodeListOfIntegers(
			modelParameters.get(Runner.SLICE_IND_ENC_SINGLE_DIGIT).intValue(), 1) : null;
		List<Integer> ns = 
			mt == ModelType.ENS ? ToolBox.decodeListOfIntegers(
			modelParameters.get(Runner.NS_ENC_SINGLE_DIGIT).intValue(), 1) : null;

		String storePath = argPaths[0];
		String pathStoredNN = argPaths[3];
		String pathStoredMM = argPaths[4];

		// 1. Initialise the superlists representing the entire training set
		// a. For N2N
		List<List<Double>> allNoteFeatures = new ArrayList<List<Double>>(); // set in bwd order when using bwd model
		List<List<Double>> allLabels = new ArrayList<List<Double>>(); // set in bwd order when using bwd model
		List<Integer> allChordSizes = new ArrayList<Integer>(); // reversed when using bwd model
		List<Integer[]> allVoicesCoDNotes = null; // set in bwd order when using bwd model
		if (modelDuration) {
			allVoicesCoDNotes = new ArrayList<Integer[]>();
		}
		List<List<List<String>>> allMFVs = null;
		if (mt == ModelType.MM) {
			allMFVs = new ArrayList<List<List<String>>>();
		}
		List<List<double[]>> allMelodyModelOutputsPerModel = null;
		if (mt == ModelType.MM || mt == ModelType.ENS) {
			// allMelodyModelOutputsPerModel will contain the MM outputs for this fold *not*
			// arranged by piece
			allMelodyModelOutputsPerModel = new ArrayList<List<double[]>>();
		}
		// b. For C2C 
		List<List<List<Double>>> allChordFeatures = new ArrayList<List<List<Double>>>();
		List<List<List<Double>>> allChordVoiceLabels = new ArrayList<List<List<Double>>>();
		List<List<List<Integer>>> allVoiceAssignmentPossibilities =	new ArrayList<List<List<Integer>>>();
		// c. For both N2N and C2C
		List<Integer[]> allEDUInfo = null; // set in bwd order when using bwd model
		if (!isTablatureCase) {
			allEDUInfo = new ArrayList<Integer[]>();
		}

		// 2. Fill the superlists (NN, MM, OTHER, and ENS case only)
		List<Integer> pieceSizes = new ArrayList<>();
		List<List<Double>> allYFwdBeforeVldSplit = new ArrayList<>();
		if (ma != ModellingApproach.HMM) {
			for (int i = 0; i < trainingPieces.size(); i++) {			
				// 1. Make current tablature and transcription
				Tablature currTab = trainingPieces.get(i).getTablature();
				Transcription currTrans = trainingPieces.get(i).getTranscription();
				Transcription currTransGT =
					dc == DecisionContext.BIDIR ? 
					trainingPieces.get(i).getSecondTranscription() : null;

				if (currTab != null) {
					pieceSizes.add(currTab.getNumberOfNotes());
				}
				else {
					pieceSizes.add(currTrans.getNumberOfNotes());
				}
				
				int indexInAll = dataset.getPieceNames().indexOf(currTrans.getName());
				if (verbose) System.out.println("i = " + i);
				if (verbose) System.out.println("piece = " + currTab.getName());
				if (verbose) System.out.println("piece = " + currTrans.getName());
				if (verbose) System.out.println("size  = " + pieceSizes.get(pieceSizes.size()-1));
				if (augment) { // TODO is indexInAll not simply i?
					indexInAll = indexInAll*augmentFactor;
					// Augmented piece
					if (i % augmentFactor != 0) {
						indexInAll += (i % augmentFactor); 
					}
				}
				if (verbose) System.out.println("indexInAll = " + indexInAll);

				// 2. Get additional information
				// NB: When using the unidir model, currTrans, currVoiceLabels, currDurLabels, and 
				// currVoicesCoDNotes are GT; when using the bidir model, they are predicted, and 
				// currTransGT, currVoiceLabelsGT, currDurLabelsGT, and currVoicesCoDNotesGT are 
				// GT. When using the bidir model, the EDU info is the same for the predicted and
				// for the GT transcription, so no GT version is needed
				if (verbose) System.out.println("   ... determining additional training information ...");
				Integer[][] currBTP = null;
				Integer[][] currBNP = null;
				List<Integer[]> currMeterInfo = null;
				List<Integer> currChordSizes = null;
				List<List<Double>> currVoiceLabels = currTrans.getVoiceLabels();
				List<List<Double>> currDurLabels = null;
				List<Integer[]> currVoicesCoDNotes = null;
				List<Integer[]> currEDUInfo = null;
				List<List<Double>> currVoiceLabelsGT = null;
				List<List<Double>> currDurLabelsGT = null;
				List<Integer[]> currVoicesCoDNotesGT = null;
				if (isTablatureCase) {
					currBTP = currTab.getBasicTabSymbolProperties();
					currMeterInfo = currTab.getMeterInfo();
//					currMeterInfo = currTab.getTimeline().getMeterInfoOBS();
					currChordSizes = currTab.getNumberOfNotesPerChord();
					if (modelDuration) { 
						currDurLabels = currTrans.getDurationLabels();
						currVoicesCoDNotes = currTrans.getVoicesSNU();
					}
				}
				else {
					currBNP = currTrans.getBasicNoteProperties();
					currMeterInfo = currTrans.getMeterInfo();
					currChordSizes = currTrans.getNumberOfNewNotesPerChord();
					if (dc == DecisionContext.UNIDIR) {
						currEDUInfo = currTrans.getVoicesEDU();
					}
					else if (dc == DecisionContext.BIDIR) {
						currEDUInfo = currTransGT.getVoicesEDU();
					}
				}
				if (dc == DecisionContext.BIDIR) {
					currVoiceLabelsGT = currTransGT.getVoiceLabels();
					currDurLabelsGT = currTransGT.getDurationLabels();
					if (modelDuration) {
						currVoicesCoDNotesGT = currTransGT.getVoicesSNU();
					}
				}

				// 3. Fill superlists
				// a. N2N-specific superlists
				if (ma == ModellingApproach.N2N) {
					if (verbose) System.out.println("   ... calculating the labels ...");
					// 1. Make labels, feature vectors, and melody feature vectors
					// Labels
					// NB: When using the unidir model, currLabels is GT; when using the bidir
					// model, it is predicted, and currLabelsGT is GT 
					List<List<Double>> currLabels = null;
					List<List<Double>> currLabelsGT = null;
					// a. If the labels are voice-duration labels
					if (isTablatureCase && modelDuration) {
						List<List<Double>> currVoiceDurLabels = new ArrayList<List<Double>>();	
						// Retrieve or calculate and set in per-piece list
						if (voiceDurLabelsPerPiece.get(indexInAll) != null) {
							currVoiceDurLabels = voiceDurLabelsPerPiece.get(indexInAll);
						}
						else {
							System.exit(0);
							for (int j = 0; j < currVoiceLabels.size(); j++) {
								List<Double> currVoiceDurLabel = new ArrayList<Double>(currVoiceLabels.get(j));
								currVoiceDurLabel.addAll(currDurLabels.get(j));
								currVoiceDurLabels.add(currVoiceDurLabel);  	
							}
							voiceDurLabelsPerPiece.set(indexInAll, currVoiceDurLabels);
						}
						currLabels = currVoiceDurLabels;
					}
					// b. If the labels are voice labels
					else {
						currLabels = currVoiceLabels;
//						// ISMIR 2017
////					// Retrieve or calculate and set in per-piece list
////					if (voiceLabelsPerPiece.get(indexInAll) != null) {
////						currentVoiceLabels = voiceLabelsPerPiece.get(indexInAll);
////					}
//						// If not: calculate and set in voiceDurLabelsPerPiece
////					else {
////						for (int j = 0; j < currentVoiceLabels.size(); j++) {
////							List<Double> currentVoiceDurationLabel = new ArrayList<Double>(currentVoiceLabels.get(j));
////							currentVoiceDurationLabel.addAll(currentDurationLabels.get(j));
////							currentVoiceDurationLabels.add(currentVoiceDurationLabel);  	
////						}
//							// Set in per-piece-list
//							voiceLabelsPerPiece.set(indexInAll, currentVoiceLabels);
//						}
					}
					// If appropriate, repeat for the bidir GT labels
					if (dc == DecisionContext.BIDIR) {
						// a. If the labels are voice-duration labels
						if (isTablatureCase && modelDuration && modelDurationAgain) {
							List<List<Double>> currVoiceDurLabelsGT = new ArrayList<List<Double>>();
							// Retrieve or calculate and set in per-piece list
							if (voiceDurLabelsGTPerPiece.get(indexInAll) != null) {
								currVoiceDurLabelsGT = voiceDurLabelsGTPerPiece.get(indexInAll);
							}
							else {
								System.exit(0);
								for (int j = 0; j < currVoiceLabelsGT.size(); j++) {
									List<Double> currVoiceDurLabelGT = new ArrayList<Double>(currVoiceLabelsGT.get(j));
									currVoiceDurLabelGT.addAll(currDurLabelsGT.get(j));
									currVoiceDurLabelsGT.add(currVoiceDurLabelGT);  	
								}
								voiceDurLabelsGTPerPiece.set(indexInAll, currVoiceDurLabelsGT);
							}
							currLabelsGT = currVoiceDurLabelsGT; 
						}
						// b. If the labels are voice labels
						else {
							currLabelsGT = currVoiceLabelsGT;
						}	
					}
					// Feature vectors
					List<List<Double>> currNoteFeatures = null;
					if (mt == ModelType.NN || mt == ModelType.DNN || mt == ModelType.OTHER) {
						if (verbose) System.out.println("   ... calculating the feature vectors ...");
						// Retrieve or calculate and set in per-piece list
						if (noteFeaturesPerPiece.get(indexInAll) != null) {
							currNoteFeatures = noteFeaturesPerPiece.get(indexInAll);
						}
						else {
							System.exit(0);
							// NB: When using the bwd model, the features will be ordered accordingly; this is done inside
							// generateAllNoteFeatureVectors(), so all arguments can remain in their original (fwd) order
							// and the features will be returned in bwd order
							// TODO make single call to generateAllNoteFeatureVectors() that decides which one to call
							// based on given featVec
							if (dc == DecisionContext.UNIDIR) {
								currNoteFeatures =
									FeatureGenerator.generateAllNoteFeatureVectors(
									modelParameters, 
									currBTP, currVoicesCoDNotes, currBNP, currTrans, currLabels, 
									currMeterInfo, currChordSizes);
							}
							else {
								currNoteFeatures = 
									FeatureGenerator.generateAllBidirectionalNoteFeatureVectors(
									modelParameters,
									currBTP, currVoicesCoDNotes, currBNP, currTrans, currLabels,
									currMeterInfo, currChordSizes);
							}
							noteFeaturesPerPiece.set(indexInAll, currNoteFeatures);
						}
						int dupl = 0;
						List<List<Double>> alreadyAdded = new ArrayList<>();
						for (List<Double> l : currNoteFeatures) {
							if (!alreadyAdded.contains(l)) {
								alreadyAdded.add(l);
							}
							else {
								dupl++;
							}
						}
						if (verbose) System.out.println("       duplicates = " + dupl);
						if (verbose) System.out.println("       uniques    = " + alreadyAdded.size());
					}
					// Melody feature vectors
					List<List<List<String>>> currMFVs = null;
					if (mt == ModelType.MM) {
						if (verbose) System.out.println("   ... calculating the melody feature vectors ...");
						// Retrieve or calculate and set in per-piece list
						if (MFVsPerPiece.get(indexInAll) != null) {
							currMFVs = MFVsPerPiece.get(indexInAll);
						}
						else {
							System.exit(0);
							currMFVs = new ArrayList<List<List<String>>>();
							// For each voice in currentTranscription
							for (NotationStaff nst : currTrans.getScorePiece().getScore()) {
								NotationVoice nv = nst.get(0);
								// Only if nv contains notes
								if (nv.size() != 0) {
									List<List<Double>> MFVsCurrVoice = 
										FeatureGenerator.generateMelodyFeatureVectors(currBTP, nv, null);
									currMFVs.add(ToolBox.convertToListString(MFVsCurrVoice));
								}
							}
							MFVsPerPiece.set(indexInAll, currMFVs);
						}
					}

					// 2. Set lists in bwd order where necessary
					// When using the bwd model, some lists must be set in bwd order: // TODO fix?
					// (1) currLabels (and, if applicable, currLabelsGT, currVoicesCoDNotes,
					// currVoicesCoDNotesGT, and currEDUInfo (used in ErrorCalculatorTab.calculateAssignmentErrors())
					// must first be ordered as the features are ordered, i.e., in bwd order
					// (2) currChordSizes must be reversed
					// NB: The reordering must be done after the feature calculation, as 
					// featureGenerator.generateAllNoteFeatureVectors() takes the lists in fwd order
					if (pm == ProcessingMode.BWD) {
						if (verbose) System.out.println("   ... setting lists in backward order ...");
						List<Integer> currBackwardsMapping;
						// Retrieve or calculate and set in per-piece list
						if (backwardsMappingPerPiece.get(indexInAll) != null) {
							currBackwardsMapping = backwardsMappingPerPiece.get(indexInAll);
						}
						else {
							System.exit(0);
							currBackwardsMapping = FeatureGenerator.getBackwardsMapping(currChordSizes); // TODO is re-calculated every time rather than retrieved with a get()-method (slower)	
							backwardsMappingPerPiece.set(indexInAll, currBackwardsMapping);
						}

						// currLabels
						List<List<Double>> currLabelsBwd = new ArrayList<List<Double>>(); 
						for (int index : currBackwardsMapping) {
							currLabelsBwd.add(currLabels.get(index));
						}
						allYFwdBeforeVldSplit.addAll(new ArrayList<>(currLabels));
						currLabels = currLabelsBwd;

						// currLabelsGT
						if (dc == DecisionContext.BIDIR) {
							List<List<Double>> currLabelsGTBwd = new ArrayList<List<Double>>(); 
							for (int index : currBackwardsMapping) {
								currLabelsGTBwd.add(currLabelsGT.get(index));
							}
							currLabelsGT = currLabelsGTBwd;
						}
						// currVoicesCoDNotes and currVoicesCoDNotesGT
						if (modelDuration) {
							List<Integer[]> currVoicesCoDNotesBwd = new ArrayList<Integer[]>();
							List<Integer[]> currVoicesCoDNotesGTBwd = new ArrayList<Integer[]>();
							for (int index : currBackwardsMapping) {
								if (dc == DecisionContext.UNIDIR) {
									currVoicesCoDNotesBwd.add(currVoicesCoDNotes.get(index));
								}
								else if (dc == DecisionContext.BIDIR){
//									currentVoicesCoDNotesBwd.add(currentVoicesCoDNotesGT.get(index));
									currVoicesCoDNotesGTBwd.add(currVoicesCoDNotesGT.get(index));
								}
							}
							// NB: In the bidirectional model, from here on this list is different from
							// what was given for the feature calculation
							currVoicesCoDNotes = currVoicesCoDNotesBwd; 
							currVoicesCoDNotesGT = currVoicesCoDNotesGTBwd;
						}
						// currEDUInfo
						if (!isTablatureCase) {
							List<Integer[]> currEDUInfoBwd = new ArrayList<Integer[]>();
							for (int index : currBackwardsMapping) {
								currEDUInfoBwd.add(currEDUInfo.get(index));
							}
							currEDUInfo = currEDUInfoBwd;
						}
						// currChordSizes
						List<Integer> currChordSizesReversed = new ArrayList<Integer>(currChordSizes); 
						Collections.reverse(currChordSizesReversed);
						currChordSizes = currChordSizesReversed;
					}

					// 3. Add all lists to the superlists
					if (verbose) System.out.println("   ... creating the training superlists ... ");
					// Features
					if (mt == ModelType.NN || mt == ModelType.DNN || mt == ModelType.OTHER) {
						allNoteFeatures.addAll(currNoteFeatures);
					}
					// Labels
					if (dc == DecisionContext.UNIDIR) {
						allLabels.addAll(currLabels);
					}
					else {
						allLabels.addAll(currLabelsGT);
					}
					// allChordSizes and allVoicesCoDNotes
					if (mt == ModelType.NN || mt == ModelType.DNN || mt == ModelType.OTHER || mt == ModelType.ENS) {	
						allChordSizes.addAll(currChordSizes);
						if (modelDuration) {
							if (dc == DecisionContext.UNIDIR) {
								allVoicesCoDNotes.addAll(currVoicesCoDNotes);
							}
							else {
								allVoicesCoDNotes.addAll(currVoicesCoDNotesGT);
							}
						}		
					}
					// Melody model features
					if (mt == ModelType.MM) {
						allMFVs.addAll(currMFVs);
					}
					if (mt == ModelType.MM || mt == ModelType.ENS) {
						// In case of the MM model, the list is set per model; in case of the ENS 
						// model, the list is set per n per model (x = highest n):
						// MM0 & n=1, MM0 & n=2, ... MM0 & n=x; MM1 & n=1; ... ; MMx & n=x
						int numSublists = sliceIndices.size();
						if (mt == ModelType.ENS) {
							numSublists = sliceIndices.size()*ns.size();
						}
						for (int j = 0; j < numSublists; j++) {
							allMelodyModelOutputsPerModel.add(new ArrayList<double[]>());
						}
					}
				}
				// b. C2C-specific superlists
				if (ma == ModellingApproach.C2C) {
					// 1. Determine the chord voice labels (each label is a List<List<Double>>) 
					if (verbose) System.out.println("   ... calculating the labels ...");
					List<List<List<Double>>> currChordVoiceLabels = currTrans.getChordVoiceLabels();

					// 2. Determine the possible voice assignments
					if (verbose) System.out.println("   ... calculating the feature vectors ...");
					List<List<List<Integer>>> currPossibleVoiceAssignmentsAllChords;	
					// Retrieve or calculate and set in per-piece list
					if (possibleVoiceAssignmentsAllChordsPerPiece.get(indexInAll) != null) {
						currPossibleVoiceAssignmentsAllChords = possibleVoiceAssignmentsAllChordsPerPiece.get(indexInAll);
					}
					else {
						System.exit(0);
						currPossibleVoiceAssignmentsAllChords =
							FeatureGeneratorChord.getOrderedVoiceAssignments(currBTP, currBNP,
							currVoiceLabels, highestNumberOfVoices);
						possibleVoiceAssignmentsAllChordsPerPiece.set(indexInAll, currPossibleVoiceAssignmentsAllChords);	
					}

					// 3. Determine the sets of chord feature vectors (with the ground truth chord feature vector 
					// always listed first) (each set of chord feature vectors is a List<List<Double>>)
					List<List<List<Double>>> currentChordFeatures = null;
					// Calculate or retrieve and set
					if (chordFeaturesPerPiece.get(indexInAll) != null) {
						currentChordFeatures = chordFeaturesPerPiece.get(indexInAll);
					}
					else {
						System.exit(0);
						currentChordFeatures =  
							FeatureGeneratorChord.generateAllChordFeatureVectors(currBTP,
							currBNP, currTrans, currMeterInfo, currPossibleVoiceAssignmentsAllChords);
						chordFeaturesPerPiece.set(indexInAll, currentChordFeatures);
					}

					// 4. Add all lists to the superlists
					if (verbose) System.out.println("   ... creating the training superlists ... ");
					allChordFeatures.addAll(currentChordFeatures);
					allChordVoiceLabels.addAll(currChordVoiceLabels);
					allVoiceAssignmentPossibilities.addAll(currPossibleVoiceAssignmentsAllChords);
				}
				// c. Learning approach-independent superlist 
				if (!isTablatureCase) {
					allEDUInfo.addAll(currEDUInfo);
				}
			} // end for-loop over training pieces
		}
		if (verbose) System.out.println(pieceSizes + " = total of " + ToolBox.sumListInteger(pieceSizes));

		// 3. From the superlists, create trainingData, validationData, and groundTruths
		List<List<List<Double>>> trainingData = null;
		List<List<List<Double>>> validationData = null;
		List<List<List<Double>>> groundTruths = new ArrayList<List<List<Double>>>();
		List<List<List<Double>>> groundTruthsVal = new ArrayList<List<List<Double>>>();
		List<Integer[]> allEDUInfoVal = null;
		if (ma != ModellingApproach.HMM) {
			// Extract the voice labels and the duration labels
			List<List<Double>> voiceLabelsForGTs = new ArrayList<List<Double>>();
			List<List<Double>> durLabelsForGTs = null;
			if (ma == ModellingApproach.N2N) {
//				if (modelDuration && (dc == DecisionContext.UNIDIR || dc == DecisionContext.BIDIR)) {
				if (modelDuration && dc == DecisionContext.UNIDIR) {
					durLabelsForGTs = new ArrayList<List<Double>>();
				}
				for (List<Double> l : allLabels) {
					voiceLabelsForGTs.add(new ArrayList<Double>(
						l.subList(0, Transcription.MAX_NUM_VOICES)));
					if (modelDuration && dc == DecisionContext.UNIDIR) {
						durLabelsForGTs.add(new ArrayList<Double>(
							l.subList(Transcription.MAX_NUM_VOICES, l.size())));
					}
				}
			}
			else if (ma == ModellingApproach.C2C) {
				for (List<List<Double>> l : allChordVoiceLabels) {
					voiceLabelsForGTs.addAll(l);
				}
			}
			
			// Lists that have both a trn and a val counterpart
			// added to groundTruths(groundTruthsVal) are
			// - voiceLabelsForGTs (voiceLabelsForGTsVal)
			// - durLabelsForGTs (durLabelsForGTsVal) (if modelDuration)
			// added to trainingData (validationData) are
			// - allNoteFeatures (allNoteFeaturesVal)
			// - allLabels (allLabelsVal)
			// allVoicesCoDNotes (if modelDuration)
			// allEDUInfo (if !isTablatureCase)
			// allChordSizes

//			tabwords.removeIf(t -> t == null);

			// Get the start and end indices in the training set of the individual pieces
			List<Integer[]> indivPieceInds = new ArrayList<>();
			int startInd = 0;
			for (int i = 0; i < pieceSizes.size(); i++) {
				indivPieceInds.add(new Integer[]{startInd, startInd + (pieceSizes.get(i) - 1)});
				startInd += pieceSizes.get(i);
			}
			if (verbose) System.out.println("indivPieceInds before deduplication:");
			for (Integer[] in : indivPieceInds) {
				if (verbose) System.out.println(Arrays.toString(in));	
			}

			// Get the duplicate counts, duplicate indices, and examples to remove per piece
//			List<Object> dedupInfo = getDeduplicationInfo(allNoteFeatures, indivPieceInds);
//			Integer[] counts = (Integer[]) dedupInfo.get(0);
//			List<Integer> indOfDuplicates = (List<Integer>) dedupInfo.get(1);
//			Integer[] examplesToRemovePerPiece = (Integer[]) dedupInfo.get(2);

			// Deduplicate lists
			if (deduplicate) {
				List<Object> toAdapt = Arrays.asList(new Object[]{
					voiceLabelsForGTs,
					durLabelsForGTs,
					allNoteFeatures,
					allLabels,
					allVoicesCoDNotes,
					allEDUInfo
//					allChordSizes
				});
				List<Object> dedup = deduplicate(toAdapt, allNoteFeatures, indivPieceInds);
				List<Object> toAdaptDedup = (List<Object>) dedup.get(0);
				voiceLabelsForGTs = (List<List<Double>>) toAdaptDedup.get(0);
				durLabelsForGTs = (List<List<Double>>) toAdaptDedup.get(1);
				allNoteFeatures = (List<List<Double>>) toAdaptDedup.get(2);
				allLabels = (List<List<Double>>) toAdaptDedup.get(3);
				allVoicesCoDNotes = (List<Integer[]>) toAdaptDedup.get(4);
				allEDUInfo = (List<Integer[]>) toAdaptDedup.get(5);
			
				indivPieceInds = (List<Integer[]>) dedup.get(1);
				Integer[] counts = (Integer[]) dedup.get(2);
			
				System.out.println("new list sizes:");
				System.out.println(voiceLabelsForGTs.size());
//				System.out.println(durLabelsForGTs.size());
				System.out.println(allNoteFeatures.size());
				System.out.println(allLabels.size());
//				System.out.println(allVoicesCoDNotes.size());
//				System.out.println(allEDUInfo.size());
				System.out.println("counts:");
				System.out.println(Arrays.toString(counts));
				System.out.println("indivPieceInds after deduplication:");
				for (Integer[] in : indivPieceInds) {
					System.out.println(Arrays.toString(in));	
				}
			}
			
			// Split lists into training and validation parts (which remain empty if valPerc == 0) 
			List<List<Double>> allNoteFeaturesVal = null;
			List<List<Double>> allLabelsVal = null;
			List<List<Double>> voiceLabelsForGTsVal = new ArrayList<List<Double>>();
			List<List<Double>> durLabelsForGTsVal = null;
			List<Integer[]> allVoicesCoDNotesVal = null;
//			List<Integer[]> allEDUInfoVal = null;
			List<Integer> allChordSizesVal = null; 			
			// For groundTruths
			List<List<List<Double>>> vlSets = createTrainAndValSet(voiceLabelsForGTs, valPerc, indivPieceInds);
			voiceLabelsForGTs = vlSets.get(0);
			voiceLabelsForGTsVal = vlSets.get(1);
			if (modelDuration && dc == DecisionContext.UNIDIR) {
				List<List<List<Double>>> dlSets = createTrainAndValSet(durLabelsForGTs, valPerc, indivPieceInds);
				durLabelsForGTs = dlSets.get(0);
				durLabelsForGTsVal = dlSets.get(1);
			}
			// For training and validation data
			int before = allNoteFeatures.size();
			List<List<List<Double>>> nfSets = createTrainAndValSet(allNoteFeatures, valPerc, indivPieceInds);
			allNoteFeatures = nfSets.get(0);
			allNoteFeaturesVal = nfSets.get(1);
			if (verbose) System.out.println("       validation set of " + (before - allNoteFeatures.size()) + 
				" data points created" );

			List<List<List<Double>>> lblSets = createTrainAndValSet(allLabels, valPerc, indivPieceInds);
			allLabels = lblSets.get(0);
			allLabelsVal = lblSets.get(1);
			// Other
			if (modelDuration) {
				List<List<Integer[]>> vcnSets = createTrainAndValSet(allVoicesCoDNotes, valPerc, indivPieceInds);
				allVoicesCoDNotes = vcnSets.get(0);
				allVoicesCoDNotesVal = vcnSets.get(1);
			}
			if (!isTablatureCase) {
				List<List<Integer[]>> eduSets = createTrainAndValSet(allEDUInfo, valPerc, indivPieceInds);
				allEDUInfo = eduSets.get(0);
				allEDUInfoVal = eduSets.get(1);
			}
			// TODO allChordSizes is only used in EvaluationManager.getOutputDetails() (to display
			// the chord index), and gives incorrect results when a validation set is used. Solution:
			// the chords sizes must be decreased with each note that is moved to the validation set
			List<List<Integer>> csSets = createTrainAndValSet(allChordSizes, valPerc, null);
			allChordSizes = csSets.get(0); // TODO not correct when validation set is used (but only used in EvaluationManager.getOutputDetails() to display chord index)
			allChordSizesVal = csSets.get(1);
			
//			System.out.println(voiceLabelsForGTs.size());
//			System.out.println(durLabelsForGTs.size());
//			System.out.println(allNoteFeatures.size());
//			System.out.println(allLabels.size());
//			System.out.println(allVoicesCoDNotes.size());
//			System.out.println(allEDUInfo.size());
//			System.out.println(allChordSizes.size());
//			System.exit(0);

			// Create ground truths
			groundTruths.add(voiceLabelsForGTs);
			groundTruths.add(durLabelsForGTs);
			groundTruthsVal.add(voiceLabelsForGTsVal);
			groundTruthsVal.add(durLabelsForGTsVal);

			// Create training and validation data 
			if (mt == ModelType.NN || mt == ModelType.DNN || mt == ModelType.OTHER) {
				// Scale the features
				double[][] minAndMax = null;
				if (ma == ModellingApproach.N2N) {
					minAndMax = 
						FeatureGenerator.getMinAndMaxValuesSetOfFeatureVectors(allNoteFeatures, ma);
					allNoteFeatures =
						FeatureGenerator.scaleSetOfFeatureVectors(allNoteFeatures, minAndMax, ma);					
					if (valPerc != 0) {
						allNoteFeaturesVal =
							FeatureGenerator.scaleSetOfFeatureVectors(allNoteFeaturesVal, minAndMax, ma);
					}
					// Simple ensemble model: for each MM, append the (unscaled) MM outputs to the 
					// (scaled) note features TODO try 
					boolean useSimpleENS = false;
					if (useSimpleENS) { 
						for (int i = 0; i < allNoteFeatures.size(); i++) {
							List<Double> currFV = allNoteFeatures.get(i);
							// For each MM: retrieve outputs from file and add them to currFV
							// TODO alternative: add average over all MM outputs
							for (int j = 0; j < sliceIndices.size(); j++) {
								int n = -1; // TODO this is a dummy value; see down below how it should be done (search for currEnd) 									
								pathStoredMM = pathStoredMM.concat("/n=" + n + "/" + "fold_" + 
									ToolBox.zerofy(fold, ToolBox.maxLen(fold)) + "/");
								String model = Runner.melodyModel + "-" +
									MelodyPredictor.getSliceIndexString(sliceIndices.get(j)) + "-";
								List<double[]> currAllMMOutp = 
									ToolBox.getStoredObjectBinary(new ArrayList<double[]>(), 
									new File(pathStoredMM + model + Runner.outputMM + "-" + 
									Runner.train + ".ser"));
								// Add the i-th output of the j-th MM
								for (double d : currAllMMOutp.get(i)) {
									currFV.add(d);
								}
							}
							allNoteFeatures.set(i, currFV);
						}
					}
//					// ISMIR2017
//					noteFeaturesPerFold.add(allNoteFeatures);
//					voiceLabelsPerFold.add(groundTruths.get(0)); 
				}
				if (ma == ModellingApproach.C2C) {
					minAndMax = 
						FeatureGenerator.getMinAndMaxValuesSetOfSetsOfFeatureVectors(allChordFeatures,
						ma);
					allChordFeatures = 
						FeatureGenerator.scaleSetOfSetsOfFeatureVectors(allChordFeatures, 
						minAndMax, ma);
				}
				ToolBox.storeObjectBinary(minAndMax, 
					new File(storePath + Runner.minMaxFeatVals + ".ser"));

				// Create the training and validation data
				if (ma == ModellingApproach.N2N) {
					trainingData = new ArrayList<List<List<Double>>>();
					trainingData.add(allNoteFeatures);
					trainingData.add(allLabels);
					if (valPerc != 0) {
						validationData = new ArrayList<List<List<Double>>>();
						validationData.add(allNoteFeaturesVal);
						validationData.add(allLabelsVal);
					}
				}
				if (ma == ModellingApproach.C2C) {
					trainingData = allChordFeatures;
				}
			}
		}

		// 4. Train and evaluate; store results in .csv file
		List<String[][]> csvTables = new ArrayList<String[][]>();
		// a. NN, DNN, OTHER, and ENS case
		if (mt == ModelType.NN || mt == ModelType.DNN || mt == ModelType.OTHER || mt == ModelType.ENS) {
			// 1. Determine output variables
			List<double[]> networkOutputs = null;
			List<double[]> combinedOutputs = null;
			List<List<Integer>> assignmentErrors = null;
			List<List<Integer>> predictedVoices = null;
			double netwErr = -1.0;
			List<Double> allClassErrs = null;
			List<Double> allClassErrsVal = null;
			List<double[]> ntwOutputsForCE = null;			
			List<List<Integer>> allBestVoiceAss = null;	
			List<Double> allHighestNetwOutp = null;
			// NN and OTHER case: determine output variables
			if (mt == ModelType.NN || mt == ModelType.DNN || mt == ModelType.OTHER) {
				int numFeatures = 0;
				int numOutputNeurons = 0;
				if (ma == ModellingApproach.N2N) {
					numFeatures = trainingData.get(0).get(0).size();
					numOutputNeurons = trainingData.get(1).get(0).size();
				}
				else if (ma == ModellingApproach.C2C) {
					numFeatures = trainingData.get(0).get(0).size();
					numOutputNeurons = 1;
				}
				
				// Train           
				System.out.println("Starting the training.");
				if (mt == ModelType.NN) {
					int numHiddenNeurons = 
						getNumberOfHiddenNeurons(modelParameters, numFeatures, isTablatureCase);	
					Integer[] layerSizes = 
						new Integer[]{numFeatures, numHiddenNeurons, numOutputNeurons};
					System.out.println("network layer sizes (input-hidden-output): " + Arrays.toString(layerSizes));
					List<List<List<Integer>>> trainingResults = 
						train(layerSizes, storePath, 
						trainingData, groundTruths, 
						validationData, groundTruthsVal, allEDUInfo, allEDUInfoVal, 
						allVoiceAssignmentPossibilities);

					// Set networkOutputs, predictedVoices, and assignmentErrors (allNetworkOutputs is set in train())
					networkOutputs = allNetworkOutputs;
					assignmentErrors = trainingResults.get(0);
					predictedVoices = trainingResults.get(1);
				}
				if (mt == ModelType.DNN || mt == ModelType.OTHER) {		
					// Store features and labels
					storeData(storePath, Runner.train + ".csv", trainingData);
					if (valPerc != 0) {
						storeData(storePath, Runner.validation + ".csv", validationData);
					}

					// Train
					String[] cmd;
					boolean isScikit = false;
					boolean smoothen = false;
					// For scikit (ISMIR 2017)
					if (isScikit) {
						smoothen = true;
						cmd = new String[]{
							"python", Runner.scriptPythonPath + Runner.scriptScikit, 
							m.name(), 
							Runner.train, 
							storePath,			
							Runner.fvExt, Runner.clExt, Runner.outpExt, 
							Runner.getOtherParam(modelParameters)};
					}
					// For TensorFlow
					else {
						List<String> argStrings = 
							getArgumentStrings(Runner.TRAIN, modelParameters, numFeatures, 
							allNoteFeatures.size(), storePath, null);
						cmd = new String[]{
							"python", Runner.scriptPythonPath + Runner.scriptTensorFlow, 
							Runner.train, 
							argStrings.get(0), 
							argStrings.get(1)};
					}
//					System.out.println("cmd = " + Arrays.toString(cmd));
					// Run Python code as a script
					PythonInterface.runPythonFileAsScript(cmd);

					// Set networkOutputs, predictedVoices, and assignmentErrors
					String[][] outpCsv = 
						ToolBox.retrieveCSVTable(ToolBox.readTextFile(
						new File(storePath + Runner.outpExt + Runner.train + ".csv")));
					List<double[]> predictedOutputs = ToolBox.convertCSVTable(outpCsv);
					if (smoothen) {
						predictedOutputs = smoothenOutput(predictedOutputs, 0.000001);
					}

					networkOutputs = predictedOutputs;
					predictedVoices = 
						OutputEvaluator.determinePredictedVoices(modelParameters, 
						predictedOutputs, null);
					assignmentErrors = 
						ErrorCalculator.calculateAssignmentErrors(predictedVoices, 
						groundTruths.get(0), null, null, allEDUInfo);

//					// If there is validation data: reconstruct combined trn and vld outputs
//					if (valPerc != 0) {
////						List<double[]> predictedOutputsComb = new ArrayList<>();
//						// Integrate vld output into trn output
//						String[][] outpVldCsv = 
//							ToolBox.retrieveCSVTable(ToolBox.readTextFile(
//							new File(storePath + Runner.outpExt + Runner.validation + ".csv")));
//						List<double[]> predictedOutputsVld = ToolBox.convertCSVTable(outpVldCsv);
//						List<double[]> predictedOutputsComb = 
//							reintegrateVldData(predictedOutputs, predictedOutputsVld, valPerc);
//						if (pm == ProcessingMode.BWD) {
//							// Split into individual pieces
//							List<List<double[]>> perPiece = new ArrayList<>();
//							int from = 0;
//							for (int size : pieceSizes) {
//								perPiece.add(predictedOutputsComb.subList(from, from + size));
//								from += size;
//							}
//							// Reverse bwd mapping
//							List<List<Integer>> bwdMapCurrFold = new ArrayList<>(backwardsMappingPerPiece);
//							// Remove testPiece from list
//							bwdMapCurrFold.remove(testPieceIndex);
//							// Reorder each element of predOutCombPerPiece according to bwdMapCurrFold
//							List<List<double[]>> reordered = new ArrayList<>(); 
//							for (int j = 0; j < perPiece.size(); j++) {
//								reordered.add(ToolBox.reorderByIndex(perPiece.get(j), 
//									bwdMapCurrFold.get(j)));
//							}
//							perPiece = reordered;
//							// Flatten
//							predictedOutputsComb = new ArrayList<>();
//							for (List<double[]> l : perPiece) {
//								predictedOutputsComb.addAll(l);
//							}
//						}
//						// Save the combined predicted output as labels
//						List<List<Double>> asList = new ArrayList<>();
////						List<List<Double>> predictedLabelsTrnAndVld = new ArrayList<>();
//						for (double[] d : predictedOutputsComb) {
////							List<Integer> predVoices = 
////								OutputEvaluator.interpretNetworkOutput(d, 
////								isTablatureCase ? true : false, 
////								modelParameters.get(Runner.DEV_THRESHOLD)).get(0);
////							predictedLabelsTrnAndVld.add(DataConverter.convertIntoVoiceLabel(predVoices));
//							asList.add(Arrays.asList(ArrayUtils.toObject(d)));
//						}
//						ToolBox.storeListOfListsAsCSVFile(asList, new File(storePath + 
//							Runner.outpExt + Runner.train + "-" + Runner.validation + ".csv"));
//
//						// Check
//						boolean checkPassed = 
//							checkIntegration(storePath, assignmentErrors, predictedOutputs, 
//							predictedOutputsComb, allYFwdBeforeVldSplit);
//						if (!checkPassed) {
//							System.exit(0);
//						}
//						else {
////							System.exit(0);
//						}
//					}
				}
				// Set remaining output variables
				if (mt == ModelType.NN) {
					allClassErrs = allClassificationErrors;
					allClassErrsVal = allClassificationErrorsVal;
					String res = "TRAIN and VALIDATION ERRORS fold " + fold + "\r\n";
					for (int i = 0; i < allClassificationErrors.size(); i++) {
						res += i + "\t" + allClassErrs.get(i) + "\t" + allClassErrsVal.get(i) + "\r\n";  
					}
					System.out.println(res);
				}

				netwErr = smallestNetworkError;
				if (ma == ModellingApproach.N2N) {
					ntwOutputsForCE = networkOutputs;
				}
				if (ma == ModellingApproach.C2C) {
					allBestVoiceAss = allBestVoiceAssignments;
					allHighestNetwOutp = allHighestNetworkOutputs;
				}
				
				// Store original NN outputs
				if (Runner.storeNetworkOutputs) {
					List<List<double[]>> outputs = new ArrayList<List<double[]>>();
					outputs.add(null);
					outputs.add(null);
					outputs.add(null);
					outputs.set(Runner.TRAIN, networkOutputs);
					ToolBox.storeObjectBinary(outputs, new File(
						storePath + Runner.outputs + ".ser"));
				}
			}
			// ENS case (where there is no actual training): determine output variables
			if (mt == ModelType.ENS) {
				// Get stored NN and MM outputs and combine them in a single list
				List<List<double[]>> outputsNNandMM = new ArrayList<List<double[]>>();
				// NN
				List<double[]> originalNetworkOutputs =
					ToolBox.getStoredObjectBinary(new ArrayList<List<double[]>>(), 
					new File(pathStoredNN + Runner.outputs + ".ser")).get(Runner.TRAIN);
				// MM
				for (int i = 0; i < sliceIndices.size(); i++) {
					String model = Runner.melodyModel + "-" +
						MelodyPredictor.getSliceIndexString(sliceIndices.get(i)) + "-";
					for (int nInd = 0; nInd < ns.size(); nInd++) {
						String currEnd = 
							"/n=" + ns.get(nInd) + "/" + "fold_" +
							ToolBox.zerofy(fold, ToolBox.maxLen(fold)) + "/";
						pathStoredMM = pathStoredMM.concat(currEnd);
						List<double[]> currAllMMOutp =
							ToolBox.getStoredObjectBinary(new ArrayList<double[]>(), 
							new File(pathStoredMM + model + Runner.outputMM + "-" + Runner.train + ".ser"));
						pathStoredMM = pathStoredMM.substring(0, pathStoredMM.indexOf(currEnd)); 
						// Determine index. Example:
						// Four sliceIndices (MMs), five ns
						// sI = 0, nInd = 0 -> index = 0*5 + 0 = 0
						// sI = 0, nInd = 4 -> index = 0*5 + 4 = 4
						// sI = 2, nInd = 2 -> index = 2*5 + 2 = 12
						int index = i*ns.size() + nInd;
						allMelodyModelOutputsPerModel.set(index, currAllMMOutp);
					}
				}
				outputsNNandMM.add(originalNetworkOutputs);
				for (int i = 0; i < sliceIndices.size()*ns.size(); i++) { 
					outputsNNandMM.add(allMelodyModelOutputsPerModel.get(i));
				}

				// Calculate weights based on cross-entropy for each model (see Pearce et al. 2004,
				// pp. 369-373) and store them
				List<Double> modelWeights = new ArrayList<Double>();
				int xi = highestNumberOfVoices; // Transcription.MAXIMUM_NUMBER_OF_VOICES TODO
				double Hmax = (Math.log(xi) / Math.log(2));
				double b = 1; // TODO try different bs
				for (List<double[]> currOutps : outputsNNandMM) {
					double H = ErrorCalculator.calculateCrossEntropy(currOutps, 
						groundTruths.get(0))[0]; // TODO NB: for the MMs, H has already been calculated and stored
					double Hrel = H/Hmax;
					double w = Math.pow(Hrel, -b);
					// Exclude useless models by giving them a weight of 0
					if (Hrel == 1.0) {
						w = 0.0;
					}
//					// Ternary conditional operator; see https://stackoverflow.com/questions/10336899/what-is-a-question-mark-and-colon-operator-used-for
//					w = (Hrel == 1 ? 0 : Math.pow(Hrel, -b));
					modelWeights.add(w);
				}
				ToolBox.storeObjectBinary(modelWeights, 
					new File(storePath + //pathComb + 
						Runner.modelWeighting + ".ser"));

				// Combine model outputs
				combinedOutputs = 
					OutputEvaluator.createCombinedNetworkOutputs(originalNetworkOutputs, 
					allMelodyModelOutputsPerModel, modelWeights);
				
				// Set networkOutputs, predictedVoices, and assignmentErrors
				networkOutputs = originalNetworkOutputs;
				predictedVoices = OutputEvaluator.determinePredictedVoices(modelParameters, 
					combinedOutputs, allBestVoiceAss);
				assignmentErrors = 
					ErrorCalculator.calculateAssignmentErrors(predictedVoices, 
					groundTruths.get(0), null, null, allEDUInfo);
				
				// Set remaining output variables
				netwErr = -1; // TODO 
				ntwOutputsForCE = combinedOutputs;
				
				// Store combined outputs
				if (Runner.storeNetworkOutputs) {
					List<List<double[]>> outputs = new ArrayList<List<double[]>>();
					outputs.add(null);
					outputs.add(null);
					outputs.add(null);
					outputs.set(Runner.TRAIN, combinedOutputs);
					ToolBox.storeObjectBinary(outputs, new File(storePath + //pathComb + 
						Runner.outputs + ".ser"));
				}
			}

			// 3. Store details 
			List<List<Integer[]>> additionalGroundTruths = new ArrayList<List<Integer[]>>();
			additionalGroundTruths.add(allEDUInfo); // TODO not used
			additionalGroundTruths.add(allVoicesCoDNotes); // TODO not used
			String[][] detailsArr = EvaluationManager.getOutputDetails( // TODO tidy up args
				groundTruths,
				/*additionalGroundTruths,*/
				null, // allPredictedVoices
				null, // allPredictedDurationLabels
				null, // allMetricPositions
				networkOutputs, // always original NN outputs
				allMelodyModelOutputsPerModel,
				combinedOutputs,
				null, // conflictIndices
				allChordSizes,
				null, // backwardsMapping
				allChordVoiceLabels, 
				allVoiceAssignmentPossibilities,	
				allBestVoiceAss,	
				allHighestNetwOutp,
				assignmentErrors
			);
			ToolBox.storeTextFile(ToolBox.createCSVTableString(detailsArr), 
				new File(storePath + Runner.details + "-" + Runner.train + ".csv"));

			// 4. Create csv table(s)
			ErrorFraction[] evalNN = 
				EvaluationManager.getMetricsSingleFold(assignmentErrors, predictedVoices,
				ntwOutputsForCE, groundTruths.get(0), allEDUInfo, false);
			evalNN[EvaluationManager.NTW_ERR_IND] = new ErrorFraction(netwErr);
			boolean isForDur = false;
			int iter = 1;
			if (modelDuration && dc == DecisionContext.UNIDIR) {
				iter = 2;
			}
			for (int i = 0; i < iter; i++) {
				String[][] csvTable = 
					EvaluationManager.createCSVTableSingleFold(fold, Runner.TRAIN, evalNN, 
					assignmentErrors, null, null, isForDur, new long[]{trPreProcTime, 
					ToolBox.getTimeDiffPrecise(start, ToolBox.getTimeStampPrecise())});
				csvTables.add(csvTable);
				isForDur = true;
			}
		}
		// b. HHM case (training is creation of ISM, OPM, and TPM)
		if (ma == ModellingApproach.HMM) {
			// ISM
			Integer[] currISM = generateInitialStateMatrix(trainingPieces,
				mappingDictionary, highestNumVoicesAssumed);
			Integer[][] currISMWrapped = new Integer[1][currISM.length];
			currISMWrapped[0] = currISM;
			ToolBox.storeMatrixAsCSVFile(currISMWrapped, new File(storePath + Runner.ISM + ".csv"));
//			ToolBox.storeObjectBinary(currISM, new File(path + Runner.ISM + ".ser"));				

			// OPM
			Integer[][] currOPM = generateObservationProbabilityMatrix(trainingPieces, 
				chordDictionary, mappingDictionary, highestNumVoicesAssumed);
			ToolBox.storeMatrixAsCSVFile(currOPM, new File(storePath + Runner.OPM + ".csv"));
//			ToolBox.storeObjectBinary(currOPM, new File(path + Runner.OPM + ".ser"));

			// TPM
			Integer[][] currTPM = generateTransitionProbabilityMatrix(trainingPieces, 
				mappingDictionary, highestNumVoicesAssumed);
			ToolBox.storeMatrixAsCSVFile(currTPM, new File(storePath + Runner.TPM + ".csv")); 
//			ToolBox.storeObjectBinary(currTPM, new File(path + Runner.TPM + ".ser"));
			
			// Create empty csv table
			String[][] csvTable = 
				EvaluationManager.createCSVTableSingleFold(fold, Runner.TRAIN, null, null, 
				null, null, false, new long[]{trPreProcTime, ToolBox.getTimeDiffPrecise(start, 
				ToolBox.getTimeStampPrecise())});
			csvTables.add(csvTable);
		}
		// c. MM case
		if (mt == ModelType.MM) {
			// For each model: train and store each melody model
			for (int i = 0; i < sliceIndices.size(); i++) {
				int currSI = sliceIndices.get(i);
				MelodyPredictor mp = 
					new MelodyPredictor(MelodyPredictor.getMelModelType(), 
					MelodyPredictor.getTermType(), modelParameters.get(Runner.N).intValue(), currSI);
				mp.trainModel(allMFVs);
				mp.saveModel(new File(
					storePath + Runner.melodyModel + "-" +
					MelodyPredictor.getSliceIndexString(currSI) + ".ser"));
				
				// Gather the MM outputs for the training set
				for (int j = 0; j < trainingPieces.size(); j++) {
					Tablature currTab = trainingPieces.get(j).getTablature();
					Transcription currTrans = trainingPieces.get(j).getTranscription();
					Integer[][] currentBTP = null;
					Integer[][] currentBNP = null;
					List<Integer> currentChordSizes = null;
					if (isTablatureCase) {
						currentBTP = currTab.getBasicTabSymbolProperties();
						currentChordSizes = currTab.getNumberOfNotesPerChord();
					}
					else {
						currentBNP = currTrans.getBasicNoteProperties();
						currentChordSizes = currTrans.getNumberOfNewNotesPerChord();
					}
					List<double[]> currentMelodyModelOutputs = 
						FeatureGenerator.generateAllMMOutputs(modelParameters, currentBTP, 
						currentBNP, currTrans, currentChordSizes, mp);
					allMelodyModelOutputsPerModel.get(i).addAll(currentMelodyModelOutputs);
					// Reset the short-term model for the current piece
					mp.resetSTM();
				}
			}
			
			// Evaluate and store MM and outputs
			for (int i = 0; i < sliceIndices.size(); i++) { 
				List<double[]> currMMOutp = allMelodyModelOutputsPerModel.get(i); 
				double[][] evalNum = 
					EvaluationManager.getMetricsSingleFoldMM(currMMOutp, groundTruths.get(0));
				String evalString = 
					EvaluationManager.getPerformanceSingleFoldMM(evalNum, currMMOutp, groundTruths.get(0));
				String model = 
					Runner.melodyModel + "-" + 
					MelodyPredictor.getSliceIndexString(sliceIndices.get(i)) + "-";
				ToolBox.storeObjectBinary(evalNum, new File(
					storePath + model + Runner.evalMM + "-" + Runner.train + ".ser"));
				ToolBox.storeTextFile(evalString, new File(
					storePath + model + Runner.evalMM + "-" + Runner.train + ".txt"));
				ToolBox.storeObjectBinary(currMMOutp, new File(
					storePath + model + Runner.outputMM + "-" + Runner.train + ".ser"));
			}
		}

		// Store the csv table(s). The time this takes is not included in the total runtime
		int indToUpdate = 
			Arrays.asList(csvTables.get(0)[0]).indexOf(EvaluationManager.Metric.RUNTIME.getStringRep());
		String s = Runner.perf;
		String end = ToolBox.getTimeStampPrecise();
		long totalTime = ToolBox.getTimeDiffPrecise(start, end);
		for (String[][] t : csvTables) {
			// Do not update the duration csv (which does not contain the runtimes)
			if (s.equals(Runner.perf)) {
				t[1+Runner.TRAIN][indToUpdate] = String.valueOf(totalTime);
			}
			ToolBox.storeTextFile(ToolBox.createCSVTableString(t), 
				new File(storePath + s + ".csv"));
			s = Runner.perfDur;
		}
		Runner.addToPerfCsvFilesStoreTimes(ToolBox.getTimeDiffPrecise(end, 
			ToolBox.getTimeStampPrecise()));
	}


	private boolean checkIntegration(String[][] detailsTrnCsv, String[][] bestEpochCsv,
		List<double[]> predOutpVld, List<double[]> yVld,
		List<double[]> predictedOutputsComb, List<List<Double>> yComb) {

		int numCombEx = yComb.size();
		// The arguments predictedOutputsComb and yComb are in fwd order; predOutpVld and
		// yVld are in bwd order (if applicable)

		// Check integration and reordering: calculate the accuracy of out-trn with out-vld
		// integrated) on the original labels (i.e., in fwd order, before splitting into 
		// trn and vld). This accuracy should be the weighted sum of the trn acc (adapted;
		// see below) and the vld acc (both of which are known at this point):
		//
		// (num(trn) + num(vld)) / (den(trn) + den(vld))
		// 
		// NB: TensorFlow does not take into account SNU notes when calculating the vld acc,
		// and only looks at the highest value in the output. SNU notes are only correct if 
		// this highest value is at the same index as the first (highest) 1.0 in the label. 
		// When iterating over the integrated list, it is not directly known if an example 
		// is from the trn or the vld set; thus, any SNU must be treated as it is in the 
		// vld set, and the known trn acc must be adapted accordingly
		//
		// 1. Calculate adapted trn acc
		int corrInd = Arrays.asList(detailsTrnCsv[0]).indexOf("correct");
		int catInd = Arrays.asList(detailsTrnCsv[0]).indexOf("category");
		int outpInd = Arrays.asList(detailsTrnCsv[0]).indexOf("model output");
		int numTrnEx = detailsTrnCsv.length - 1; // -1 is for header
		int misassignmentsTrn = 0;
		for (String[] detailsLine : detailsTrnCsv) {
			// Incorrect
			if (detailsLine[catInd].equals(EvaluationManager.Metric.INCORR.getStringRep())) {
				misassignmentsTrn++;
			}
			// Overlooked, superfluous, half: only incorrect if highest value in predicted 
			// outputs is not at the same index as first 1.0 in labels
			if (detailsLine[catInd].equals(EvaluationManager.Metric.OVERL.getStringRep()) || 
				detailsLine[catInd].equals(EvaluationManager.Metric.SUPERFL.getStringRep()) || 
				detailsLine[catInd].equals(EvaluationManager.Metric.HALF.getStringRep())) {
				// Get voice label
				List<Integer> corr = new ArrayList<>();
				corr.add(Integer.parseInt(detailsLine[corrInd]));
				// If SNU
				if (!detailsLine[corrInd+1].equals("")) {
					corr.add(Integer.parseInt(detailsLine[corrInd+1]));
				}
				List<Double> label = DataConverter.convertIntoVoiceLabel(corr);
				// Get predicted output
				List<Double> predOutp = new ArrayList<>();
				for (int i = 0; i < Transcription.MAX_NUM_VOICES; i++) {
					predOutp.add(Double.parseDouble(detailsLine[outpInd + i]));
				}
				// Check index of highest value
				if (predOutp.indexOf(Collections.max(predOutp)) != label.indexOf(1.0)) {
					misassignmentsTrn++;
				}
			}
		}
		Rational adaptedTrnAcc = new Rational(numTrnEx - misassignmentsTrn, numTrnEx);

		// 2. Calculate vld acc
		int numVldEx = numCombEx - numTrnEx;
		int misassignmentsVld = 0;
		// a. Method 1: uses same file (predOutpVld) from which predictedOutputsComb is created
		if (predOutpVld.size() != yVld.size()) {
			System.out.println("Error: different list sizes.");
			return false; 
		}
		for (int j = 0; j < predOutpVld.size(); j++) {
			List<Double> pred = Arrays.asList(ArrayUtils.toObject(predOutpVld.get(j)));
			List<Double> y = Arrays.asList(ArrayUtils.toObject(yVld.get(j)));
			if (pred.indexOf(Collections.max(pred)) != y.indexOf(1.0)) {
				misassignmentsVld++;
			}
		}
		Rational vldAcc = new Rational(numVldEx - misassignmentsVld, numVldEx);
		// b. Method 2: uses different file as from which combined outputs are created
//		String[][] bestEpoch = 
//			ToolBox.retrieveCSVTable(ToolBox.readTextFile(bestEpochFile));
		List<double[]> epochAndAcc = ToolBox.convertCSVTable(bestEpochCsv);
		double accVld = epochAndAcc.get(0)[1];
		// acc = (|vld ex| - misAss) / |vld ex|
		// acc * |vld ex| = |vld ex| - misAss
		// misAss = -((acc * |vld ex|) - |vld ex|)
		int misassignmentsVldAlt = (int) Math.round(-((accVld * numVldEx) - numVldEx));
		Rational vldAccAlt = new Rational(numVldEx - misassignmentsVldAlt, numVldEx);

		// 3. Calculate combined acc
		Rational expectedCombAcc = 
			new Rational(adaptedTrnAcc.getNumer() + vldAcc.getNumer(),
			adaptedTrnAcc.getDenom() + vldAcc.getDenom());
		int misassignmentsComb = 0;
		if (predictedOutputsComb.size() != yComb.size()) {
			System.out.println("Error: different list sizes.");
			return false; 
		}
		for (int j = 0; j < predictedOutputsComb.size(); j++) {
			List<Double> pred = Arrays.asList(ArrayUtils.toObject(predictedOutputsComb.get(j)));
			List<Double> y = yComb.get(j);
			if (pred.indexOf(Collections.max(pred)) != y.indexOf(1.0)) {
				misassignmentsComb++;
			}
		}
		Rational combAcc = new Rational(numCombEx - misassignmentsComb, numCombEx);
//		System.out.println("expected: " + expectedCombAcc);
//		System.out.println("combined: " + combAcc);
		// Assert equality of expected and calculated combined accuracies
		if (!expectedCombAcc.equals(combAcc)) {
			System.out.println("Error: accuracies are not the same.");
			return true;
		}
		else {
			System.out.println("Accuracies are the same!");
			return true;
		}
	}


	/**
	 * Finds duplicate features in the given list of features, and deduplicates them in this
	 * list as well as in all corresponding lists. Adapts indivPieceInds accordingly.
	 * 
	 * @param toAdapt The lists to deduplicate.
	 * @param features The features still containing duplicates. 
	 * @param indivPieceInds The start and end indices of all pieces in the individual lists in 
	 *        toAdapt.
	 * @return <ul> 
	 * 		   <li>toAdapt, deduplicated</li>
	 *         <li>indivPieceInds, adapted accordingly</li>
	 * 		   <li>counts, an Integer[] where the value at index i indicates how many i-fold 
	 *         duplicates occur in the features</li>
	 *         </ul>
	 */
	// TESTED
	static <T> List<Object> deduplicate(List<Object> toAdapt, List<List<Double>> features, 
		List<Integer[]> indivPieceInds) {

		// Get the duplicate counts, duplicate indices, and examples to remove per piece
		List<Object> dedupInfo = getDeduplicationInfo(features, indivPieceInds);
		Integer[] counts = (Integer[]) dedupInfo.get(0);
		List<Integer> indOfDuplicates = (List<Integer>) dedupInfo.get(1);
		Integer[] examplesToRemovePerPiece = (Integer[]) dedupInfo.get(2);
		System.out.println("num duplicates:");
		System.out.println(indOfDuplicates.size());
		System.out.println("to remove per piece:");
		System.out.println(Arrays.toString(examplesToRemovePerPiece) + " = total of " + 
			ToolBox.sumListInteger(Arrays.asList(examplesToRemovePerPiece)));

		// Deduplicate lists in toAdapt
		for (int i = 0; i < toAdapt.size(); i++) {
			List<T> l = (List<T>) toAdapt.get(i);
			if (l!= null) {
				l = ToolBox.removeItemsAtIndices(l, indOfDuplicates);
			}			
			toAdapt.set(i, l);
		}

		// Adapt indivPieceInds
		int total = 0;
		for (int j = 0; j < indivPieceInds.size(); j++) {
			Integer[] in = indivPieceInds.get(j);
			in[0] -= total;
			total += examplesToRemovePerPiece[j];
			in[1] -= total;
		}

		return Arrays.asList(new Object[]{toAdapt, indivPieceInds, counts});
	}


	/**
	 * Given a list of features, gets the deduplication information.
	 * 
	 * @param features 
	 * @param indivPieceInds
	 * @return <ul>    
	 * 		   <li>An Integer[] where the value at index i indicates how many i-fold duplicates 
	 *         occur in the features</li>
	 *         <li>A List containing the indices of all duplicates</li>
	 *         <li>An Integer[] where the value at index i indicates how many examples must be
	 *             removed from the piece at index i</li>
	 *         </ul>  
	 */
	// TESTED
	static List<Object> getDeduplicationInfo(List<List<Double>> features, List<Integer[]> indivPieceInds) {
		Integer[] counts = new Integer[30];
		Arrays.fill(counts, 0);
		List<Integer> indsOfDuplicates = new ArrayList<>();
		Integer[] examplesToRemovePerPiece = new Integer[indivPieceInds.size()];
		Arrays.fill(examplesToRemovePerPiece, 0);

		List<List<Double>> uniques = new ArrayList<>();
		for (int i = 0; i < features.size(); i++) {
			List<Double> l = features.get(i);
			// Increment frequency counter
			counts[Collections.frequency(features, l)]++;
			// First encounter of fv: add to uniques 
			if (!uniques.contains(l)) {
				uniques.add(l);
			}
			// Not first encounter of fv: add index to list
			else {
				indsOfDuplicates.add(i);
				// Increment the element representing the piece the example at i is removed from
				for (int j = 0; j < indivPieceInds.size(); j++) {
					Integer[] pieceInds = indivPieceInds.get(j);
					if (i >= pieceInds[0] && i <= pieceInds[1]) {
						examplesToRemovePerPiece[j]++;
						break;
					}
				}
			}	
		}
		System.out.println("num uniques:");
		System.out.println(uniques.size());
		System.out.println("indsOfDuplicates + " + indsOfDuplicates.size());
		return Arrays.asList(new Object[]{counts, indsOfDuplicates, examplesToRemovePerPiece});
	}


	/**
	 * Trains the network according to the given parameters and stores, for each run,
	 * (1) the weights that go with the lowest classification error obtained in that run
	 *     in an .xml file;
	 * 
	 * Runs - metacycles - cycles
	 * Each run, a fresh network that trains anew is created
	 * Each metacycle is a measurement step within the training process, where we determine the training and
	 * classification errors to see how the learning goes
	 * Each cycle is one forward-backward propagation step of the NN over all training examples where we adjust
	 * the weights.
	 * --> 3 runs with 20 metacycles of each 10 cycles means that the network is trained (i.e., the weights are 
	 * changed) 3 * 20 * 10 = 600 times.
	 *  
	 * @param modelParameters
	 * @param layerInfo Contains the number of input neurons (element 0), hidden neurons 
	 *                  (element 1), and output neurons (element 2). Bias neurons are always
	 *                   excluded. 
	 * @param path The path to store the weights at and retrieve the stored weights from.
	 * @param trainingData N2N case: contains two elements: 
	 *                               at index 0: features 
                                     at index 1: full labels (i.e., including duration, if applicable)
	 *                     C2C case: contains the n chordFeatures, where n is the number of chords 
	 * @param argGroundTruths N2N case: contains two elements: 
	 *                                  at index 0: voice labels 
                                        at index 1: duration labels (if applicable) 
	 * @param validationData N2N case: contains two elements: 
	 *                                 at idex 0: features  
	 *                                 at index 1: full labels (i.e., including duration, if applicable)
	 * @param argGroundTruthsVal N2N case: contains two elements: 
	 *                                     at index 0: voice labels 
                                           at index 1: duration labels (if applicable)
	 * @param argEDUInfo
	 * @param argEDUInfoVal
	 * @param argPossibleVoiceAssignmentsAllChords
	 */
	public List<List<List<Integer>>> train(Integer[] layerSizes, String path, 
		List<List<List<Double>>> trainingData, List<List<List<Double>>> argGroundTruths,
		List<List<List<Double>>> validationData, List<List<List<Double>>> argGroundTruthsVal,
		List<Integer[]> argEDUInfo, List<Integer[]> argEDUInfoVal,		
		List<List<List<Integer>>> argPossibleVoiceAssignmentsAllChords) {

		List<List<List<Integer>>> trainingResults = new ArrayList<List<List<Integer>>>();

		Map<String, Double> modelParameters = Runner.getModelParams();
		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()]; 
		boolean modelDuration = m.getModelDuration();
		ActivationFunction af = 
			NNManager.ALL_ACT_FUNCT[modelParameters.get(NNManager.ACT_FUNCTION).intValue()];
		ModellingApproach ma = 
			Runner.ALL_MODELLING_APPROACHES[modelParameters.get(Runner.MODELLING_APPROACH).intValue()];
		WeightsInit wi = 
			Runner.ALL_WEIGHTS_INIT[modelParameters.get(Runner.WEIGHTS_INIT).intValue()];
		int maxMetaCycles = modelParameters.get(Runner.META_CYCLES).intValue();
		int valPerc = modelParameters.get(Runner.VALIDATION_PERC).intValue();

		NNManager nm = new NNManager();
		nm.initialiseNetwork(af, layerSizes);

		// Create training data
		List<double[][]> trainingSetN2N = null;
		List<RelativeTrainingExample> argRelativeTrainingExamples = null;
		if (ma == ModellingApproach.N2N) {
			trainingSetN2N = createDataset(trainingData.get(0), trainingData.get(1));
		}
		else if (ma == ModellingApproach.C2C) {
			argRelativeTrainingExamples = 
				generateAllRelativeTrainingExamples(trainingData);
		}
		List<List<Double>> argGTVoiceLabels = null; // = argGroundTruths.get(0);
		List<List<Double>> argGTVoiceLabelsVal = null;
		// In the N2N case, trainingData.get(1) and argGroundTruths.get(0) both contain the 
		// GT voice labels for the training data. Sanity check that they are indeed the same.
		// The same applies to the validation data.
		// NB Is only true when not modelling duration TODO remove?
		if (ma == ModellingApproach.N2N) {
			if (!modelDuration) {
				if (!trainingData.get(1).equals(argGroundTruths.get(0))) {
					throw new RuntimeException("ERROR: the lists of voice labels are not the same.");
				}
				if (valPerc != 0 && !validationData.get(1).equals(argGroundTruthsVal.get(0))) {
					throw new RuntimeException("ERROR: the lists of voice labels are not the same.");
				}
			}
//			argGTVoiceLabels = trainingData.get(1);
			argGTVoiceLabels = argGroundTruths.get(0);
			if (valPerc != 0) {
//				argGTVoiceLabelsVal = validationData.get(1);
				argGTVoiceLabelsVal = argGroundTruthsVal.get(0);
			}
		}
		else if (ma == ModellingApproach.C2C) {
			argGTVoiceLabels = argGroundTruths.get(0);
		}
		List<List<Double>> argGTDurLabels = argGroundTruths.get(1);
		List<List<Double>> argGTDurLabelsVal = argGroundTruthsVal.get(1);

		// Initialise the network weights 
		List<List<Double>> weights = new ArrayList<List<Double>>();
		weights.add(null);
		weights.add(null);
		if (wi == WeightsInit.INIT_RANDOM) {
			nm.initWeights(null);
			weights.set(Runner.INIT_WEIGHTS, nm.getNetworkWeights());
		}
		else if (wi == WeightsInit.INIT_FROM_LIST) {
			List<Double> initialWeights = 
				ToolBox.getStoredObjectBinary(new ArrayList<List<Double>>(), 
				new File(path + Runner.weights + ".ser")).get(Runner.INIT_WEIGHTS);
			weights.set(Runner.INIT_WEIGHTS, initialWeights);
			nm.initWeights(initialWeights);
		}

		// For each metaCycle:
		double[] currTrainingError = null; // NB: trainingError[1] is in both N2N and C2C the error after training 
		double currRelativeError = -1.0; // C2C only; remains -1.0 in N2N
		double[] smallestTrainingError = null;
		double smallestRelativeError = -1.0;
		double currClassError = -1.0;
		double currClassErrorVal = -1.0;
		int optimalMetaCycle = -1;
		double smallestClassError = 0.0;
		double smallestClassErrorVal = 0.0;
		List<List<Integer>> bestPredictedVoices = null;
		List<List<Integer>> currAssignmentErrors = null;
//		List<List<Integer>> currAssignmentErrorsVal = null;
		List<List<Integer>> smallestAssignmentErrors = null;
		List<double[]> bestAllNetworkOutputs = null;
		List<Double> bestAllHighestNetworkOutputs = null;
		List<List<Integer>> bestAllBestVoiceAssignments = null;
		List<Double> currWeights = null;
		List<Double> bestWeights = null;
//		String allClassErrs = "TRAINING ERRORS PER METACYCLE" + "\r\n";
		List<Double> allClassErrs = new ArrayList<>();
//		String allClassErrsVal = "VALIDATION ERRORS PER METACYCLE" + "\r\n";
		List<Double> allClassErrsVal = new ArrayList<>();
		int metaCycles = 0; 
		boolean fixFlatSpot;
		// 1. Train the network x metaCycles. Each metaCycle consists of y training cycles
		do {
			System.out.println("\nmetaCycle = " + metaCycles);
			if (ma == ModellingApproach.N2N) {
				fixFlatSpot = true; // was false before 10-2-15
				currTrainingError = 
					nm.trainNetwork(modelParameters, fixFlatSpot, trainingSetN2N);
			}
			if (ma == ModellingApproach.C2C) {
				fixFlatSpot = true; // was false before 10-2-15
				currTrainingError = 
					nm.trainNetworkRelative(modelParameters, fixFlatSpot, 
					argRelativeTrainingExamples);
				currRelativeError = currTrainingError[3]; 
			}

			// Set allNetworkOutputs
			ArrayList<double[]> argAllNetworkOutputs = null;
//			ArrayList<double[]> argAllNetworkOutputsVal = null;
			List<Double> argAllHighestNetworkOutputs = null;
			List<List<Integer>> argAllBestVoiceAssignments = null;
			if (ma == ModellingApproach.N2N) {
				argAllNetworkOutputs = nm.createAllNetworkOutputs(trainingData.get(0));
//				if (valPerc != 0) {
//					argAllNetworkOutputsVal = nm.createAllNetworkOutputs(validationData.get(0));
//				}
			}
			if (ma == ModellingApproach.C2C) {
				List<List<Double>> allNetwOutpAllChords =
					nm.createAllNetworkOutputsForAllChords(trainingData);
				argAllHighestNetworkOutputs	= 
					OutputEvaluator.createAllHighestNetworkOutputs(allNetwOutpAllChords);
				argAllBestVoiceAssignments = 
					OutputEvaluator.createAllBestVoiceAssignments(allNetwOutpAllChords,
					argPossibleVoiceAssignmentsAllChords);
			}

			// 2. Determine the assignmentErrors and the classificationError of the current
			// trained network
			List<List<Integer>> currentAllPredictedVoices = 
				OutputEvaluator.determinePredictedVoices(modelParameters, 
				argAllNetworkOutputs, argAllBestVoiceAssignments);
//			List<List<Integer>> currentAllPredictedVoicesVal = null;
//			if (valPerc != 0) {
//				currentAllPredictedVoicesVal = 
//					OutputEvaluator.determinePredictedVoices(modelParameters, 
//					argAllNetworkOutputsVal, argAllBestVoiceAssignments);
//			}

			List<Rational[]> currentAllPredictedDurations = null;
//			List<Rational[]> currentAllPredictedDurationsVal = null;
			if (argGTDurLabels != null) { 
				currentAllPredictedDurations = 
					OutputEvaluator.determinePredictedDurations(modelParameters, 
					argAllNetworkOutputs, argAllBestVoiceAssignments);
//				if (valPerc != 0) {
//					currentAllPredictedDurationsVal = 
//						OutputEvaluator.determinePredictedDurations(modelParameters, 
//						argAllNetworkOutputsVal, argAllBestVoiceAssignments);
//				}
			}

			// When using bwd model:
			// currentAllPredictedVoices = bwd 
			// groundTruthVoiceLabels = bwd
			// currentAllPredictedDurationLabels = bwd 
			// groundTruthDurationLabels = bwd
			// equalDurationUnisonsInfo = bwd
			currAssignmentErrors = 
				ErrorCalculator.calculateAssignmentErrors(currentAllPredictedVoices, 
				argGTVoiceLabels, currentAllPredictedDurations, 
				argGTDurLabels, argEDUInfo);
			currClassError = 
				ErrorCalculator.calculateClassificationError(currAssignmentErrors);
			if (valPerc != 0) {
				ArrayList<double[]> argAllNetworkOutputsVal = 
					nm.createAllNetworkOutputs(validationData.get(0));
				List<List<Integer>> currentAllPredictedVoicesVal = 
					OutputEvaluator.determinePredictedVoices(modelParameters, 
					argAllNetworkOutputsVal, argAllBestVoiceAssignments);
				List<Rational[]> currentAllPredictedDurationsVal = null;
				if (argGTDurLabels != null) {
					currentAllPredictedDurationsVal = 
						OutputEvaluator.determinePredictedDurations(modelParameters, 
						argAllNetworkOutputsVal, argAllBestVoiceAssignments);
				}
				List<List<Integer>> currAssignmentErrorsVal = 
					ErrorCalculator.calculateAssignmentErrors(currentAllPredictedVoicesVal, 
					argGTVoiceLabelsVal, currentAllPredictedDurationsVal, 
					argGTDurLabelsVal, argEDUInfoVal);
				currClassErrorVal = 
					ErrorCalculator.calculateClassificationError(currAssignmentErrorsVal);
			}

			currWeights = nm.getNetworkWeights();
			System.out.println("Network error: " + currTrainingError[1]);
			System.out.println("Classification error             : " + currClassError);
			System.out.println("Classification error (validation): " + currClassErrorVal);
			
//			allClassErrs = allClassErrs.concat("" + currClassError + "\r\n");
			allClassErrs.add(currClassError);
//			allClassErrsVal = allClassErrsVal.concat("" + currClassErrorVal + "\r\n");
			allClassErrsVal.add(currClassErrorVal);
			
			// 3. (Re)set the appropriate variables and store the network weights
			// Determine which error to use as measure. In the N2N approach, this should be the
			// classification error; in the C2C approach, it can also be the relative error
			double currErrorToTrack;
			double smallestErrorSoFar;
			if (ma == ModellingApproach.N2N) {
				currErrorToTrack = currClassError; // has val equivalent
				smallestErrorSoFar = smallestClassError; // has val equivalent
				if (valPerc != 0) {
					currErrorToTrack = currClassErrorVal;
					smallestErrorSoFar = smallestClassErrorVal;
				}
			}
			else {
				currErrorToTrack = currClassError;
				smallestErrorSoFar = smallestClassError;
//				currErrorToTrack = currentRelativeError; 
//				smallestErrorSoFar = smallestRelativeError;
			}
			// a. Set and store if this is the first metaCycle
			// b. Reset and store if 
			// (1) the current error is smaller than the smallest error so far, or 
			// (2) the current error is equal to the smallest error so far but the current
			// network error is smaller
			if (metaCycles == 0 || 
				(currErrorToTrack < smallestErrorSoFar) ||
				(currErrorToTrack == smallestErrorSoFar && 
				(currTrainingError[1] < smallestTrainingError[1]))) {     	
				smallestClassError = currClassError; // has val equivalent
				smallestClassErrorVal = currClassErrorVal;
				smallestTrainingError = currTrainingError; // network error
				
				bestPredictedVoices = currentAllPredictedVoices; // has val equivalent; NR here
				smallestAssignmentErrors = currAssignmentErrors; // has val equivalent; NR here
				bestWeights = currWeights; 
				bestAllNetworkOutputs = argAllNetworkOutputs; // has val equivalent; NR here
				bestAllHighestNetworkOutputs = argAllHighestNetworkOutputs;
				bestAllBestVoiceAssignments = argAllBestVoiceAssignments;
				if (ma == ModellingApproach.C2C) {
					smallestRelativeError = currRelativeError;
				}
				optimalMetaCycle = metaCycles;
			}
			metaCycles++;
		} while (metaCycles < maxMetaCycles); 

		// 3. After each training run: add the final values of smallestClassificationError, smallestTrainingError,
		// bestPredictedVoices, and smallestAssignmentsErrors to the Lists
//		smallestClassificationErrors.add(smallestClassificationError);
//		smallestTrainingErrors.add(smallestTrainingError[1]);
//		allBestPredictedVoices.add(bestPredictedVoices);
//		allSmallestAssignmentErrors.add(smallestAssignmentErrors);
//		setAllNetworkOutputs(bestAllNetworkOutputs);
		allNetworkOutputs = bestAllNetworkOutputs;
//		setAllHighestNetworkOutputs(bestAllHighestNetworkOutputs);
		allHighestNetworkOutputs = bestAllHighestNetworkOutputs;
//		setAllBestVoiceAssignments(bestAllBestVoiceAssignments);
		allBestVoiceAssignments = bestAllBestVoiceAssignments;
		
		// Set the network with the best weights (for calculating training results) 
		nm.initWeights(bestWeights);
		// Store the initial and best weights
		weights.set(Runner.BEST_WEIGHTS, bestWeights);
//		ToolBox.storeObjectBinary(bestWeights, bestWeightsFile);		
		ToolBox.storeObjectBinary(weights, new File(path + Runner.weights + ".ser"));

		trainingResults.add(smallestAssignmentErrors);
		trainingResults.add(bestPredictedVoices);

//		setSmallestNetworkError(smallestTrainingError[1]);
		smallestNetworkError = smallestTrainingError[1];
		
//		setAllClassificationErrors(allClassErrs);
		allClassificationErrors = allClassErrs;
		allClassificationErrorsVal = allClassErrsVal;
		System.out.println("optimal metaCycle                : " + optimalMetaCycle);

		
		classificationErrorsAllFolds.add(allClassErrs);
		classificationErrorsValAllFolds.add(allClassErrsVal);
		optimalMetaCyclesAllFolds.add(optimalMetaCycle);
		
//		}
		return trainingResults;
	}


	/**
	 * Takes every nth example from the given list and puts it into a second list, the validation
	 * set. n is determined by the given percentage as follows: n = 100/perc.
	 * 
	 * @param data
	 * @param dataAlt
	 * @param perc
	 * @return A list containing as element 0 the pruned original list, and as element 1 the 
	 * 		   new list
	 */
	private static List<List<List<Double>>> createTrainAndValSetOLD(List<List<Double>> data, int perc) {
		
		int n = (int) Math.round(100.0/perc);
		
		List<List<Double>> prunedOrig = new ArrayList<List<Double>>();
		List<List<Double>> val = new ArrayList<List<Double>>();
		for (int i = 0; i < data.size(); i++) {
			List<Double> curr = data.get(i);
			// Add every nth example to the validation set
			if (i % n == 0) {
				val.add(curr);
			}
			// Add every other example to the pruned original set
			else {
				prunedOrig.add(curr);
			}
		}
		List<List<List<Double>>> res = new ArrayList<List<List<Double>>>();
		res.add(prunedOrig);
		res.add(val);
		return res;
	}


	/**
	 * Splits the given collection into the original collection, pruned, and a new collection, 
	 * where the given percentage determines the size of the second collection (compared to the 
	 * given list).
	 * 
	 * The new collection is created by taking every nth example from the given list; n is 
	 * determined by the given percentage as follows: n = 100/perc.
	 * 
	 * @param data
	 * @param perc 
	 * @param indivIndices
	 * @return A collection containing as element 0 the pruned original collection, and as 
	 *         element 1 the new collection
	 */
	// TESTED
	static <T> List<List<T>> createTrainAndValSet(List<T> data, int perc, List<Integer[]> indivIndices) {
		List<T> prunedOrig = new ArrayList<>();
		List<T> val = new ArrayList<>();
		// If the validation set is not 0% of the original
		if (perc != 0) {
			int n = (int) Math.round(100.0/perc);
			// Augmented data case
			if (augment && indivIndices != null) {
				int count = 0;
				// For each piece (non-augmented pieces at indices for which i % augmentFactor == 0; 
				// augmented pieces at other indices)
				for (int i = 0; i < indivIndices.size(); i++) {
					Integer[] currIndices = indivIndices.get(i); 
					// Non-augmented piece
					if (i % augmentFactor == 0) { 
						for (int j = currIndices[0]; j <= currIndices[1]; j++) {
							T element = data.get(j);
							// Add every n-th example to the validation set
							if (count % n == 0) {
								val.add(element);
							}
							// Add every other example to the pruned original set
							else {
								prunedOrig.add(element);
							}
							count++;
						}
					}
					// Augmented piece
					else {
//					if (i % 2 == 1) {
						// Add every example to the pruned original set
						for (int j = currIndices[0]; j <= currIndices[1]; j++) {
							prunedOrig.add(data.get(j));
						}
					}
				}
			}
			// Non-augmented data case
			else {
				for (int i = 0; i < data.size(); i++) {
					T element = data.get(i);
					// Add every nth example to the validation set
					if (i % n == 0) {
						val.add(element);
					}
					// Add every other example to the pruned original set
					else {
						prunedOrig.add(element);
					}
				}
			}
		}
		// If the validation set is 0% of the original
		else {
			prunedOrig = new ArrayList<T>(data);
			val = new ArrayList<T>();
		}
		return Arrays.asList(prunedOrig, val);
	}


	/**
	 * Reintegrates the vld data back into the training data using the given validation 
	 * percentage. 
	 * 
	 * @param trn
	 * @param vld
	 * @param valPerc
	 * @return
	 */
	// TESTED
	static List<double[]> reintegrateVldData(List<double[]> trn, List<double[]> vld, int valPerc) {	
		// If valPerc is n, every n-th data point (starting at the first) is taken from the 
		// original trn data and added to the vld data. There are three scenarios (example 
		// for n = 5)
		// a. The last data point is a trn data point and the next would also be a trn data point
		//	  --> the split trn list has the same size as the vld list (3 and 3)
		//    full: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13]
		//    trn:	[   1, 2, 3, 4,    6, 7, 8, 9,     11, 12, 13]
		//    vld:  [0,             5,             10,           ]
		// b. The last data point is a trn data point and the next would be a vld data point
		//    --> the split trn list has the same size as the vld list (3 and 3)
		//    full: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14]
		//    trn:	[   1, 2, 3, 4,    6, 7, 8, 9,     11, 12, 13, 14]
		//    vld:  [0,             5,             10,               ]
		// c. The last data point is a vld data point and the next would be a trn data point
		//    --> the split trn list has one element less than the vld list (3 and 4)
		//    full: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
		//    trn:	[   1, 2, 3, 4,    6, 7, 8, 9,     11, 12, 13, 14,   ]
		//    vld:  [0,             5,             10,                 15]
		List<double[]> reintegrated = new ArrayList<>();

		// Split trn up into data point groups of size valPerc - 1
		List<List<double[]>> trnSplit = new ArrayList<>();
		List<double[]> curr = new ArrayList<>();
		for (int i = 0; i < trn.size(); i++) {
			curr.add(trn.get(i));
			if (curr.size() == valPerc - 1 || i == trn.size() - 1) {
				trnSplit.add(curr);
				curr = new ArrayList<>();
			}
		}

		// Insert trn group after each vld data point
		for (int i = 0; i < vld.size(); i++) {
			reintegrated.add(vld.get(i));
			// Only if there is an element at index i in trnSplit (exception: c. above)
			if (i < trnSplit.size()) {
				reintegrated.addAll(trnSplit.get(i));
			}
		}
		return reintegrated;
	}


	private static List<List<Integer[]>> createTrainAndValSetALT(List<Integer[]> data, int perc) {
		
		int n = (int) Math.round(100.0/perc);
		
		List<Integer[]> prunedOrig = new ArrayList<Integer[]>();
		List<Integer[]> val = new ArrayList<Integer[]>();
		for (int i = 0; i < data.size(); i++) {
			Integer[] curr = data.get(i);
			// Add every nth example to the validation set
			if (i % n == 0) {
				val.add(curr);
			}
			// Add every other example to the pruned original set
			else {
				prunedOrig.add(curr);
			}
		}
		List<List<Integer[]>> res = new ArrayList<List<Integer[]>>();
		res.add(prunedOrig);
		res.add(val);
		return res;
	}


	/**
	 * Creates a placeholder for the BasicMLDataset to be used in EncogNNManager. 
	 * @param argNoteFeatures
	 * @param argLabels
	 * @return A List containing two double[][]: <br>
	 *         as element 0, the inputs <br>
	 *         as element 1, the outputs
	 */
	private List<double[][]> createDataset(List<List<Double>> argNoteFeatures, 
		List<List<Double>> argLabels) {
		List<double[][]> basicMLDataSetAsList = new ArrayList<double[][]>();
		
		double[][] inputs = new double[argNoteFeatures.size()][argNoteFeatures.get(0).size()];
		for (int i = 0; i < argNoteFeatures.size(); i++) {
			for (int j = 0; j < argNoteFeatures.get(i).size(); j++) {
				inputs[i][j] = argNoteFeatures.get(i).get(j);	
			}
		}
		double[][] outputs = new double[argLabels.size()][argLabels.get(0).size()];
		for (int i = 0; i < argLabels.size(); i++) {
			for (int j = 0; j < argLabels.get(i).size(); j++) {
				outputs[i][j] = argLabels.get(i).get(j);	
			}
		}
//		dataset = new BasicMLDataSet(inputs, outputs);
//		setDataset(new BasicMLDataSet(inputs, outputs));
//		return new BasicMLDataSet(inputs, outputs);
		basicMLDataSetAsList.add(inputs);
		basicMLDataSetAsList.add(outputs);
		return basicMLDataSetAsList;	
	}


	/**
	 * Generates, for the entire piece, a List of RelativeTrainingExamples from the given List<List<List<>>> of all 
	 * possible chord feature vectors for each chord in the piece. Each RelativeTrainingExample consists of two elements:
	 * the first will always be the chord feature vector that goes with the ground truth voice assignment (i.e., the first 
	 * one from the List<List<Double>> representing all possible feature vectors for that chord), and the second will be
	 * one of the remaining possibilities, in ascending order. For each chord, this yields a List<RelativeTrainingExample>
	 * that looks like [[fv0, fv1], [fv0, fv2], ... , [fv0, vfn]], where n is the index of the last possible chord feature
	 * vector. For each chord, this List is added to the end of the List<RelativeTrainingExample> that is returned by the 
	 * method after the last chord is dealt with.   
	 * 
	 * @param allCompleteChordFeatureVectors
	 * @return
	 */
	// TESTED
	static List<RelativeTrainingExample> generateAllRelativeTrainingExamples(
		List<List<List<Double>>> allCompleteChordFeatureVectors) {
		List<RelativeTrainingExample> allRelativeTrainingExamples = new ArrayList<RelativeTrainingExample>();

		for (int i = 0; i < allCompleteChordFeatureVectors.size(); i++) {
			List<List<Double>> currentCompleteChordFeatureVectors = allCompleteChordFeatureVectors.get(i);  
			// Get the chord feature vector for the ground truth, which is always the first element of currentCompleteChordFeatureVectors
			List<Double> currentGroundTruthChordFeatureVector = currentCompleteChordFeatureVectors.get(0);
			// Pair currentGroundTruthChordFeatureVector up with all other possible chord feature vectors to form the  
			// RelativeTrainingExamples, and add each of these to relativeTrainingExamples
			for (int j = 1; j < currentCompleteChordFeatureVectors.size(); j++) {
				List<Double> currentChordFeatureVector = currentCompleteChordFeatureVectors.get(j);
				RelativeTrainingExample currentRelativeTrainingExample = 
					new RelativeTrainingExample(currentGroundTruthChordFeatureVector, currentChordFeatureVector);
				allRelativeTrainingExamples.add(currentRelativeTrainingExample);	
			}
		}
		return allRelativeTrainingExamples;
	}
	
	
	/**
	 * Smoothen the given list of outputs as to avoid 0 probabilities by adding the given
	 * smoothing value to it.
	 * 
	 * @param outputs
	 * @param smoothingValue
	 * @return
	 */
	// TESTED
	static List<double[]> smoothenOutput(List<double[]> outputs, double smoothingValue) {
		for (double[] d : outputs) {
			for (int i = 0; i < d.length; i++) {
				if (d[i] == 0.0) {
					d[i] += smoothingValue;
				}
			}
		}
		return outputs;
	}


	/**
	 * Gets the number of hidden neurons by multiplying the given number of features by the given
	 * hidden layer factor. The results is rounded
	 * <ul>
	 * <li> down to the nearest integer when the part after the decimal point < .5 </li>
	 * <li> up to the nearest integer when the part after the decimal point >= .5 </li>
	 * </ul>
	 *
	 * @param numFeatures
	 * @param HLFactor
	 * @return
	 */
	// TESTED
	public static int getNumberOfHiddenNeurons(Map<String, Double> modelParameters, 
		int numFeatures, boolean isTablatureCase) {

		ModellingApproach ma = 
			Runner.ALL_MODELLING_APPROACHES[modelParameters.get(Runner.MODELLING_APPROACH).intValue()];
		FeatureVector featVec = null;
		if (ma == ModellingApproach.N2N) {
			featVec = Runner.ALL_FEATURE_VECTORS[modelParameters.get(Runner.FEAT_VEC).intValue()];
		}

		// Exception: in the case of feature vectors A, B, and C, numHiddenNeurons is equal to the 
		// the number of features in feature vector D 
		if (ma == ModellingApproach.N2N && (featVec == FeatureVector.PHD_A ||
			featVec == FeatureVector.PHD_B || featVec == FeatureVector.PHD_C)) {
			if (isTablatureCase) {
				return FeatureGenerator.NUM_FEATURES_HL_ABC_TAB;
			}
			else {
				return FeatureGenerator.NUM_FEATURES_HL_ABC_NON_TAB;
			}
		}
		// Else: calculate
		else {
			double exactNum = numFeatures * modelParameters.get(Runner.HIDDEN_LAYER_FACTOR);
			return (int) Math.round(exactNum);
		}
	}


	/**
	 * Stores the given data (feature vectors and ground truth labels) at the given path.
	 * 
	 * @param path
	 * @param ext
	 * @param data Contains two elements: <br>
	 * 			   (1) at index 0: the feature vectors <br>
	 * 			   (2) at index 1: the ground truth labels (or <code>null</code> when 
	 * 							   employing a trained user model)
	 */
	public static void storeData(String path, String ext, List<List<List<Double>>> data) {
		// Store features, labels, and classes as csv files 
		for (int ind = 0; ind < data.size(); ind++) {
			List<List<Double>> curr = data.get(ind);
			if (curr != null) {
				int numEx = curr.size();
				int vectorSize = curr.get(0).size();
				String[][] csvTbl = new String[numEx][vectorSize];
//				String[][] csvTblCl = new String[1][numEx];
				for (int i = 0; i < numEx; i++) {
					// fv or label
					List<Double> currFvOrLbl = curr.get(i);
					String[] currRow = new String[vectorSize];
					for (int j = 0; j < currFvOrLbl.size(); j++) {
						currRow[j] = "" + currFvOrLbl.get(j);
					}
					csvTbl[i] = currRow;
					// class ISMIR 2017
//					if (ind == 1) {	
//						csvTblCl[0][i] = 
//							"" + DataConverter.convertIntoListOfVoices(currFvOrLbl).get(0); // TODO account for SNUs
//					}
				}
				if (ind == 0) {
					ToolBox.storeTextFile(ToolBox.createCSVTableString(csvTbl), 
						new File(path + Runner.fvExt + ext));
				}
				if (ind == 1) {
					ToolBox.storeTextFile(ToolBox.createCSVTableString(csvTbl), 
						new File(path + Runner.lblExt + ext));
//					// ISMIR 2017
//					ToolBox.storeTextFile(ToolBox.createCSVTableString(csvTblCl), 
//						new File(path + Runner.clExt + ext));
				}
			}
		}
	}


	public static List<String> getArgumentStrings(int mode, Map<String, Double> modelParameters, int numFeatures, 
		int numTrainingExamples, String storePath, String pathTrainedUserModel) {

		WeightsInit wi = Runner.ALL_WEIGHTS_INIT[modelParameters.get(Runner.WEIGHTS_INIT).intValue()];
		int mbSize = modelParameters.get(Runner.MINI_BATCH_SIZE).intValue();
		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()]; 
		DecisionContext dc = m.getDecisionContext();
		boolean trn = mode == Runner.TRAIN; 
		String hyperparams = String.join(",", Arrays.asList(
			"ismir_2018=" + Boolean.toString(ToolBox.toBoolean(modelParameters.get(Runner.ISMIR_2018).intValue())),
			"use_stored_weights=" + (trn ? Boolean.toString(wi == WeightsInit.INIT_FROM_LIST) : "true"),
			"user_model=" + Boolean.toString(Runner.getDeployTrainedUserModel()),
			"layer_sizes=" + 
				"[" + numFeatures + " " + 
				(modelParameters.get(Runner.HIDDEN_LAYER_SIZE).intValue() + 
				" ").repeat(modelParameters.get(Runner.NUM_HIDDEN_LAYERS).intValue()) + 
				Transcription.MAX_NUM_VOICES + "]",
			"val_perc=" + (trn ? modelParameters.get(Runner.VALIDATION_PERC).intValue() : "-1"),
			"mini_batch_size=" + (trn ? (mbSize == -1 ? numTrainingExamples : mbSize) : "-1"),
			"epochs=" + (trn ? modelParameters.get(Runner.EPOCHS).intValue() : "-1"), 
			"seed=" + modelParameters.get(Runner.SEED).intValue(),
			"lrn_rate=" + (trn ? modelParameters.get(Runner.LEARNING_RATE) : "-1"),
			"kp=" + (trn ? modelParameters.get(Runner.KEEP_PROB) : "1.0")));
		String extensionEnd = 
			trn ? Runner.train : 
			(mode == Runner.TEST ? (dc != DecisionContext.BIDIR ? Runner.test : Runner.application) : 
			Runner.application); 
		String pathsExtensions = String.join(",", Arrays.asList(
			"store_path=" + storePath,
			"path_trained_user_model=" + (pathTrainedUserModel != null ? pathTrainedUserModel : ""),
			"fv_ext=" + Runner.fvExt + extensionEnd + ".csv", 
			"lbl_ext=" + Runner.lblExt + extensionEnd + ".csv", 
			"out_ext=" + Runner.outpExt + extensionEnd + ".csv"));

		return Arrays.asList(new String[]{hyperparams, pathsExtensions});
	}


	/**
	 * Lists all the unique chords in the given pieces in the sequence they are encountered.
	 * 
	 * @param pieces
	 * @return
	 */
	// TESTED (for both tablature and non-tablature case)
	List<List<Integer>> generateChordDictionary(List<TablatureTranscriptionPair> pieces) { 
		List<List<Integer>> chordDictionary = new ArrayList<List<Integer>>();

		for (TablatureTranscriptionPair t : pieces) {
			List<List<Integer>> currChordDictionary = null;
			// a. In the tablature case
			if (t.getTablature() != null) {
				currChordDictionary = t.getTablature().generateChordDictionary();
			}
			// b. In the non-tablature case
			else {
				currChordDictionary = t.getTranscription().generateChordDictionary();
			}
			// Add the chords that are not yet in chordDictionary to it
			for (List<Integer> chord : currChordDictionary) {
				if (!chordDictionary.contains(chord)) {
					chordDictionary.add(chord);
				}
			}
		}	
		return chordDictionary;
	}


	/**
	 * Lists all the unique voice assignments in the given pieces in the sequence they are encountered.
	 * 
	 * @param pieces
	 * @param highestNumberOfVoicesAssumed
	 * @return
	 */
	// TESTED (for both tablature and non-tablature case)
	List<List<Integer>> generateMappingDictionary(List<TablatureTranscriptionPair> pieces, 
		int highestNumberOfVoicesAssumed) {
		List<List<Integer>> mappingDictionary = new ArrayList<List<Integer>>();

		for (TablatureTranscriptionPair t : pieces) {
			List<List<Integer>> currMappingDictionary = 
				t.getTranscription().generateVoiceAssignmentDictionary(highestNumberOfVoicesAssumed);
			// Add the mappings that are not yet in mappingDictionary to it
			for (List<Integer> mapping : currMappingDictionary) {
				if (!mappingDictionary.contains(mapping)) {
					mappingDictionary.add(mapping);
				}
			}
		}  	
		return mappingDictionary;
	}


	/**
	 * Generates the initial state matrix, in which for each existing voice assignment in the given 
	 * pieces, the probability of that voice assignment being the first voice assignment is listed.
	 * 
	 * @param pieces
	 * @param mappingDictionary
	 * @param highestNumberOfVoicesAssumed
	 * @return
	 */
	// TESTED (for both tablature and non-tablature case)
	Integer[] generateInitialStateMatrix(List<TablatureTranscriptionPair> pieces, 
		List<List<Integer>> mappingDictionary, int highestNumberOfVoicesAssumed) {

		// Create the matrix and initialise it with all ones (Laplacian smoothing)
		Integer[] ism = new Integer[mappingDictionary.size()];
		Arrays.fill(ism, 1);
		
		// Populate the matrix
		for (TablatureTranscriptionPair piece : pieces) {
			List<List<Integer>> mappings = 
				piece.getTranscription().getVoiceAssignments(highestNumberOfVoicesAssumed);
			List<Integer> firstMapping = mappings.get(0);
			int mappingIndex = mappingDictionary.indexOf(firstMapping);
			ism[mappingIndex]++;
		}
		return ism;
	}


	/**
	 * Generates the observation probability matrix, in which for each existing observation (chord, 
	 * represented by a series of pitches with the lowest first) in the given pieces, the probability of
	 * that observation being assigned any of the voice assignments from the given voice assignment
	 * dictionary are listed.
	 * Boosts all chord-voice assignment combinations in which the number of onsets in the voice assignment
	 * equals the number of onsets in the chord by the given number.
	 * 
	 * @param pieces
	 * @param chordDictionary
	 * @param mappingDictionary
	 * @param highestNumberOfVoicesAssumed
	 * @return
	 */
	// TESTED (for both tablature and non-tablature case)
	Integer[][] generateObservationProbabilityMatrix(List<TablatureTranscriptionPair> pieces, 
		List<List<Integer>> chordDictionary, List<List<Integer>> mappingDictionary, 
		int highestNumberOfVoicesAssumed) {

		// Create the matrix and initialise it with all ones (Laplacian smoothing)
		int numberOfRows = chordDictionary.size();
		int numberOfColumns = mappingDictionary.size();
		Integer[][] matrix = new Integer[numberOfRows][numberOfColumns];
		for (Integer[] row : matrix) {
			Arrays.fill(row, 1);	
		}

		// Populate the matrix
		for (TablatureTranscriptionPair piece : pieces) {
			Tablature currTablature = null;
			Transcription currTranscription = piece.getTranscription();
			List<List<Note>> chords = currTranscription.getChords();
			int currNumberOfChords = -1;
			// a. In the tablature case
			if (piece.getTablature() != null) {
				currTablature = piece.getTablature();
				currNumberOfChords = currTablature.getChords().size();
			}
			// b. In the non-tablature case
			else {
				currNumberOfChords = chords.size();
//				currNumberOfChords = currTranscription.getChords().size();
			}
			List<List<Integer>> currMappings = 
				currTranscription.getVoiceAssignments(highestNumberOfVoicesAssumed);

			// For each chord 
			for (int i = 0; i < currNumberOfChords; i++) {
				// Get the current chord (as pitches, sorted) and the current voice assignment
				List<Integer> currChord = new ArrayList<Integer>();
				// a. In the tablature case
				if (piece.getTablature() != null) {
					currChord = currTablature.getPitchesInChord(i);
				}
				// b. In the non-tablature case
				else {
					currChord = Transcription.getPitchesInChord(chords.get(i));
//					currChord = currTranscription.getPitchesInChord(i);
				}
				Collections.sort(currChord);

				List<Integer> currMapping = currMappings.get(i); 
				// Get index in chordDictionary for currentChord
				int indexOfCurrChord = chordDictionary.indexOf(currChord);
				// Get index in mappingDictionary for previousMapping
				int indexOfCurrMapping = mappingDictionary.indexOf(currMapping);
				// Increment (row indexOfCurrentChord, column indexOfCurrentMapping) in matrix
				matrix[indexOfCurrChord][indexOfCurrMapping]++;
			}	
		}

		// Unsmooth (i.e., revert to 0) all impossible chord-voice assignment combinations 
		// (combinations in which the number of onsets encoded in the voice assignment does not 
		// equal the number of onsets in the chord)
		for (int row = 0; row < numberOfRows; row++) {
			int chordIndex = row;
			List<Integer> currChord = chordDictionary.get(chordIndex);
			int numberOfOnsetsInChord = currChord.size();
			for (int column = 0; column < numberOfColumns; column++) {
				int mappingIndex = column;
				List<Integer> currMapping = mappingDictionary.get(mappingIndex);  
				int numberOfOnsetsInMapping = Collections.max(currMapping) + 1;
//				if (numberOfOnsetsInChord == numberOfOnsetsInMapping) {
//					matrix[row][column] += boost;
//				}
				if (numberOfOnsetsInChord != numberOfOnsetsInMapping) { // TODO EB
					matrix[row][column] = 0;
				} 
			}
		}	
		return matrix;
	}


	/**
	 * Generates the transition probability matrix, in which for each existing voice assignment in the
	 * given pieces, the probability of that voice assignment being followed by any of the voice 
	 * assignments from the mappingDictionary are listed.  
	 * 
	 * @param pieces
	 * @param mappingDictionary
	 * @param highestNumberOfVoicesAssumed
	 * @return
	 */
	// TESTED (for both tablature and non-tablature case)
	Integer[][] generateTransitionProbabilityMatrix(List<TablatureTranscriptionPair> pieces, 
		List<List<Integer>> mappingDictionary, int highestNumberOfVoicesAssumed) {

		// Create the matrix and initialise it with all ones (Laplacian smoothing)
		int dimension = mappingDictionary.size();
		Integer[][] matrix = new Integer[dimension][dimension];
		for (Integer[] row : matrix) {
			Arrays.fill(row, 1);	
		}

		// Populate the matrix
		for (TablatureTranscriptionPair piece : pieces) {
			Transcription currTranscription = piece.getTranscription();
			int currNumberOfChords = -1;
			// a. In the tablature case
			if (piece.getTablature() != null) {
				currNumberOfChords = piece.getTablature().getChords().size();
			}
			// b. In the non-tablature case
			else {
				currNumberOfChords = currTranscription.getChords().size();
			}	
			List<List<Integer>> currMappings = 
				currTranscription.getVoiceAssignments(highestNumberOfVoicesAssumed);

			// For each chord but the last
			for (int i = 0; i < currNumberOfChords - 1; i++) {
				// Get the current voice assignment and the next voice assignment
				List<Integer> currMapping = currMappings.get(i); 
				List<Integer> nextMapping = currMappings.get(i + 1);
				// Get index in mappingDictionary for currMapping and nextMapping
				int indexOfCurr = mappingDictionary.indexOf(currMapping);
				int indexOfNext = mappingDictionary.indexOf(nextMapping);
				// Increment (row indexOfCurrent, column indexOfNext) in matrix
				matrix[indexOfCurr][indexOfNext]++;
			}
		}
		return matrix;
	}

}
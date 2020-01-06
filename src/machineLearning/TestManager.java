package machineLearning;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import data.Dataset;
import de.uos.fmt.musitech.data.score.NotationChord;
import de.uos.fmt.musitech.data.score.NotationStaff;
import de.uos.fmt.musitech.data.score.NotationSystem;
import de.uos.fmt.musitech.data.score.NotationVoice;
import de.uos.fmt.musitech.data.score.ScoreNote;
import de.uos.fmt.musitech.data.score.ScorePitch;
import de.uos.fmt.musitech.data.structure.Note;
import de.uos.fmt.musitech.data.structure.Piece;
import de.uos.fmt.musitech.data.structure.harmony.KeyMarker;
import de.uos.fmt.musitech.data.structure.harmony.KeyMarker.Mode;
import de.uos.fmt.musitech.data.time.MetricalTimeLine;
import de.uos.fmt.musitech.utility.math.Rational;
import exports.MEIExport;
import exports.MIDIExport;
import featureExtraction.FeatureGenerator;
import featureExtraction.FeatureGeneratorChord;
import machineLearning.NNManager.ActivationFunction;
import python.PythonInterface;
import representations.Tablature;
import representations.Transcription;
import tools.ToolBox;
import ui.Runner;
import ui.Runner.Configuration;
import ui.Runner.DecisionContext;
import ui.Runner.FeatureVector;
import ui.Runner.Model;
import ui.Runner.ModelType;
import ui.Runner.ModellingApproach;
import ui.Runner.ProcessingMode;
import utility.DataConverter;

public class TestManager {
	
//	private enum EvalMode {TEST_MODE, APPLICATION_MODE};
	
	// 1. Created before testing: data and derived information // TODO make all fields that are protected private (only needed in TestManagerTest) and create get()-methods
	// a. Tablature, GT Transcription, predicted Transcription
	Tablature tablature; // tab / N2N and C2C
	Transcription groundTruthTranscription; // tab and non-tab / N2N and C2C
	private Transcription predictedTranscription; // tab and non-tab / N2N
	// b. Non-voice related (in tab case derived from tablature; in non-tab case from GT groundTruthTranscription)
	Integer[][] basicTabSymbolProperties; // tab / N2N and C2C
	Integer[][] basicNoteProperties; // non-tab / N2N and C2C
	List<Integer[]> meterInfo; // tab and non-tab / N2N and C2C	
	List<Rational[]> allMetricPositions; // tab and non-tab / N2N and C2C
	private List<Integer> chordSizes; // tab and non-tab / N2N
	List<Integer> backwardsMapping; // tab and non-tab / N2N and C2C (but not used in C2C)
	// c. GT/voice-related (always derived from groundTruthTranscription)
	private List<List<Double>> groundTruthVoiceLabels; // tab and non-tab / N2N and C2C	
	private List<List<Double>> groundTruthDurationLabels; // tab / N2N; 
	private List<List<Double>> groundTruthLabels; // tab and non-tab / N2N
	private List<Integer[]> groundTruthVoicesCoDNotes; // tab / N2N  
	private List<Integer[]> equalDurationUnisonsInfo; // non-tab / N2N and C2C
	
	// 2. Created before testing: tools
	private NNManager networkManager; // N2N and C2C
//	private ErrorCalculator errorCalculator; // N2N and C2C
//	OutputEvaluator outputEvaluator; // N2N and C2C
	private FeatureGenerator featureGenerator; // N2N only
	private FeatureGeneratorChord featureGeneratorChord; // C2C only
	
	// 3. Created during testing (all at once in test mode and incrementally in application mode)
	// a. Lists used in both test and application mode
	List<List<Integer>> allPredictedVoices; // N2N and C2C
	List<double[]> allNetworkOutputs; // N2N
	private List<List<Double>> noteFeatures; // N2N
	private List<double[]> allCombinedOutputs; // N2N
	private List<List<double[]>> allMelodyModelOutputsPerModel; // N2N
	private List<Double> allHighestNetworkOutputs; // C2C/HMM (is predictedIndices in HMM case) 
	private List<List<Integer>> allBestVoiceAssignments; // C2C/HMM (is predictedMappings in HMM case)
	private List<List<List<Integer>>> allPossibleVoiceAssignmentsAllChords; // C2C
	private List<List<List<Double>>> chordFeatures; // C2C
	// b. Lists used in application mode only
	Transcription newTranscription; // N2N and C2C
	List<List<Double>> allVoiceLabels; // N2N and C2C
	List<List<Double>> allDurationLabels; // N2N
	List<Rational[]> allPredictedDurations; // N2N
	List<Integer[]> allVoicesCoDNotes; // N2N
	List<List<Integer>> conflictIndices; // N2N
	List<List<Note>> allNotes; // N2N
	List<double[]> allNetworkOutputsAdapted; // N2N
	String conflictsRecord; // N2N
	private List<MelodyPredictor> allMelodyPredictors; // N2N
	private String conflictsRecordTest; // N2N (bidirectional test mode)
	//
	private boolean verbose;
	List<List<Integer>> mappingDictionary;
	
	// Not in use TODO
	private int notesAddedToOccupiedVoice; // N2N only
	private List<Integer> indicesOfNotesAddedToOccupiedVoice; // N2N only
	private String applicationProcess = "";

	List<List<List<Integer>>> allObervations;
	public void prepareTesting(String start) {
		Map<String, Double> modelParameters = Runner.getModelParams();
		Dataset dataset = Runner.getDataset();
		String[] paths = Runner.getPaths();

		boolean useCV = ToolBox.toBoolean(modelParameters.get(Runner.CROSS_VAL).intValue());
		boolean applToNewData = 
			ToolBox.toBoolean(modelParameters.get(Runner.APPL_TO_NEW_DATA).intValue());
		ModellingApproach ma = 
			Runner.ALL_MODELLING_APPROACHES[modelParameters.get(Runner.MODELLING_APPROACH).intValue()];
		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()]; 
		ModelType mt = m.getModelType();
		DecisionContext dc = m.getDecisionContext();
				
		int datasetSize = dataset.getNumPieces();
		String tePreProcTime = 
			String.valueOf(ToolBox.getTimeDiffPrecise(start, ToolBox.getTimeStampPrecise()));
		// a. Without cross-validation
		if (!useCV) {
//			String[] currentPaths = new String[paths.length];
//			for (int i = 0; i < paths.length; i++) {
//				String s = paths[i];
//				if (s != null) {
//					currentPaths[i] = s.concat("no_CV/");
//				}
//			}

			// Run startTestProcess() in both modes on all pieces in the dataset
			for (int k = 0; k < datasetSize; k++) {
				String startFoldTe = ToolBox.getTimeStampPrecise();
				// If the ground truth is known: also test in test mode
				if (!applToNewData) {
					new TestManager().startTestProcess(k, paths, Runner.TEST, 
						new String[]{tePreProcTime, startFoldTe}); // TODO new tm necessary to reset class vars
				}
				String startFoldAppl = ToolBox.getTimeStampPrecise();
				new TestManager().startTestProcess(k, paths, Runner.APPL, 
					new String[]{null, startFoldAppl}); // TODO new tm necessary to reset class vars
			}
		}
		// b. With cross-validation
		else {
			// For each fold
			for (int k = 1; k <= datasetSize; k++) {
				String startFoldTe = ToolBox.getTimeStampPrecise();
				System.out.println("Fold = " + k);
				String[] currentPaths = new String[paths.length];
				for (int i = 0; i < paths.length; i++) {
					String s = paths[i];
					if (s != null) {
						// Add fold for all cases but MM path for ENS model  
						if (!(mt == ModelType.ENS && i == 2)) {
							currentPaths[i] = 
								s.concat("fold_").concat(ToolBox.zerofy(k, 
								ToolBox.maxLen(k))).concat("/");
						}
						else {
							currentPaths[i] = s;
						}
					}
				}
				
				// 1. Test in test mode (previously predicted information is not used in the feature
				// calculation)
				TestManager tm = new TestManager();	// necessary to reset class variables
				tm.startTestProcess(k, currentPaths, Runner.TEST, new String[]{tePreProcTime, startFoldTe});

				// 2. If applicable: test in application mode (previously predicted information is 
				// used in the feature calculation))
				String startFoldAppl = ToolBox.getTimeStampPrecise();
				if (dc == DecisionContext.UNIDIR && ma != ModellingApproach.HMM && !Runner.ignoreAppl) {
					tm = new TestManager(); // necessary to reset class variablesfix
					if (mt != ModelType.MM) {
						tm.startTestProcess(k, currentPaths, Runner.APPL, 
							new String[]{null, startFoldAppl});
					}
				}
			}
		}
	}
	
	
	private void initialiseNetwork(String path, int numFeatures) {
		Map<String, Double> modelParameters = Runner.getModelParams();
		ModellingApproach ma = 
			Runner.ALL_MODELLING_APPROACHES[modelParameters.get(Runner.MODELLING_APPROACH).intValue()];
//		FeatureVector featVec = 
//			Runner.ALL_FEATURE_VECTORS[modelParameters.get(Runner.FEAT_VEC).intValue()];
		boolean modelDurationAgain = 
			ToolBox.toBoolean(modelParameters.get(Runner.MODEL_DURATION_AGAIN).intValue());
		boolean isTablatureCase = Runner.getDataset().isTablatureSet();
		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()]; 
		boolean modelDuration = m.getModelDuration();
		DecisionContext dc = m.getDecisionContext();
		ActivationFunction af = 
			NNManager.ALL_ACT_FUNCT[modelParameters.get(NNManager.ACT_FUNCTION).intValue()];

		// Determine number of hidden neurons and output neurons
		int numHiddenNeurons = 
			TrainingManager.getNumberOfHiddenNeurons(
//				numFeatures, modelParameters.get(Runner.HIDDEN_LAYER_FACTOR));
				modelParameters, numFeatures, isTablatureCase);
//		// In the case of feature vectors A, B, and C: numhiddenNeurons is equal 
//		// to the the number of features in feature vector D 
//		if (ma == ModellingApproach.N2N && featVec != FeatureVector.D) {
//			if (isTablatureCase) {
//				numHiddenNeurons = FeatureGenerator.NUM_FEATURES_HL_ABC_TAB;
//			}
//			else {
//				numHiddenNeurons = FeatureGenerator.NUM_FEATURES_HL_ABC_NON_TAB;
//			}
//		}
		int numOutputNeurons = Transcription.MAXIMUM_NUMBER_OF_VOICES;
		if (ma == ModellingApproach.N2N) {
			if (isTablatureCase && modelDuration) {	
				if (dc == DecisionContext.UNIDIR) {
					numOutputNeurons += Transcription.DURATION_LABEL_SIZE;
				}
				else if (dc == DecisionContext.BIDIR) {
					if (modelDurationAgain) {
						numOutputNeurons += Transcription.DURATION_LABEL_SIZE;
					}
				}
			}
		}
		else if (ma == ModellingApproach.C2C) {
			numOutputNeurons = 1;
		}

		networkManager = new NNManager();
		networkManager.initialiseNetwork(af, new Integer[]{numFeatures, numHiddenNeurons, numOutputNeurons});

		List<Double> bestWeights = 
			ToolBox.getStoredObjectBinary(new ArrayList<List<Double>>(), 
			new File(path + Runner.weights + ".ser")).get(Runner.BEST_WEIGHTS);
		networkManager.initWeights(bestWeights);
	}


	/**
	 * 
	 * @param fold Fold number when using cross-validation; piece index when not.
	 * @param argPaths
	 * @param mode
	 * @param times
	 */
	private void startTestProcess(int fold, String[] argPaths, int mode, String[] times) {		
		long tePreProcTime = 0;
		if (mode == Runner.TEST) { 
			tePreProcTime = (long) Integer.parseInt(times[0]);
		}
		String start = times[1];

		Dataset dataset = Runner.getDataset();
		Map<String, Double> modelParameters = Runner.getModelParams();
//		List<Integer> sliceIndices =
//			ToolBox.decodeListOfIntegers(modelParameters.get(Runner.SLICE_IND_ENC_SINGLE_DIGIT).intValue(), 1);
//		verbose = Runner.getVerbose();
//		List<Integer> ns = 
//			ToolBox.decodeListOfIntegers(modelParameters.get(Runner.NS_ENC_SINGLE_DIGIT).intValue(), 1);

		ModellingApproach ma = 
			Runner.ALL_MODELLING_APPROACHES[modelParameters.get(Runner.MODELLING_APPROACH).intValue()];		
		boolean modelDurationAgain = 
			ToolBox.toBoolean(modelParameters.get(Runner.MODEL_DURATION_AGAIN).intValue());
		int highestNumVoicesTraining = Runner.getHighestNumVoicesTraining();
//			modelParameters.get(Runner.HIGHEST_NUM_VOICES).intValue();
		boolean applToNewData = 
			ToolBox.toBoolean(modelParameters.get(Runner.APPL_TO_NEW_DATA).intValue());
		boolean useCV = ToolBox.toBoolean(modelParameters.get(Runner.CROSS_VAL).intValue());
		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()]; 
		ModelType mt = m.getModelType();
		DecisionContext dc = m.getDecisionContext(); 
		boolean modelDuration = m.getModelDuration();	
		ProcessingMode pm = 
			Runner.ALL_PROC_MODES[modelParameters.get(Runner.PROC_MODE).intValue()];
		boolean isTablatureCase = Runner.getDataset().isTablatureSet();

//		FeatureVector featVec = 
//			Runner.ALL_FEATURE_VECTORS[modelParameters.get(Runner.FEAT_VEC).intValue()];
		List<Integer> sliceIndices = null;
		if (mt == ModelType.MM || mt == ModelType.ENS) {
			sliceIndices = 
				ToolBox.decodeListOfIntegers(modelParameters.get(Runner.SLICE_IND_ENC_SINGLE_DIGIT).intValue(), 1);
		}
		List<Integer> ns = null;
		if (mt == ModelType.ENS) {		
			ns = ToolBox.decodeListOfIntegers(modelParameters.get(Runner.NS_ENC_SINGLE_DIGIT).intValue(), 1);
		}

		String pathNN = null;
		String pathMM = null;
		String pathPredBidir = null;
		String pathComb = null;
		String storePath = null;
		if (mt == ModelType.NN || mt == ModelType.HMM || mt == ModelType.DNN || mt == ModelType.OTHER) {
			pathNN = argPaths[0];
			storePath = pathNN;
			pathPredBidir = argPaths[1];
		}
		else if (mt == ModelType.MM) {
			pathMM = argPaths[0];
		}
		else if (mt == ModelType.ENS) {
			pathComb = argPaths[0];
			pathNN = argPaths[1];
//			pathMM = argPaths[2];
			storePath = pathComb;
		}
		if (!useCV && applToNewData) {
			pathComb = argPaths[0];
			pathNN = argPaths[1];
			storePath = pathComb;
		}

		int pieceIndex;
		if (useCV) {
			pieceIndex = dataset.getNumPieces() - fold; // numFolds - fold
		}
		else {
			pieceIndex = fold;
		}
		String testPieceName = dataset.getPieceNames().get(pieceIndex);
		
		System.out.println("... processing " + testPieceName + " ...");

		// 1. Set tablature and GT transcription as well as derived information
		if (isTablatureCase) {
			tablature = dataset.getAllTablatures().get(pieceIndex);
		}
		groundTruthTranscription = dataset.getAllTranscriptions().get(pieceIndex);

		// Non-voice-related information (derived from tablature or groundTruthTranscription)
		// a. tab
		if (isTablatureCase) {
			basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
			meterInfo = tablature.getMeterInfo();
			allMetricPositions = tablature.getAllMetricPositions();
			// When using the bwd model, chordSizes must be set in reverse order. This must be 
			// done *after* the feature generation, as generateAllNoteFeatureVectors() takes the 
			// list in fwd order as argument
			chordSizes = tablature.getNumberOfNotesPerChord();
		}
		// b. non-tab
		else {
//			System.out.println("... creating ground truth transcription ...");
			if (dataset.isTabAsNonTabSet()) {
				int currInterval = 0;
				if (testPieceName.equals("ochsenkun-1558-absolon_fili-shorter")) {
					currInterval = 7;
				}
				else if (testPieceName.equals("rotta-1546-bramo_morir")) {
					currInterval = -2;
				}
				else if (testPieceName.equals("phalese-1563-las_on")) {
					currInterval = -2;
				}
				groundTruthTranscription.transposeNonTab(currInterval);
			}
			basicNoteProperties = groundTruthTranscription.getBasicNoteProperties();
			meterInfo = groundTruthTranscription.getMeterInfo();
			allMetricPositions = groundTruthTranscription.getAllMetricPositions();
			// When using the bwd model, chordSizes must be set in reverse order. This must be 
			// done *after* the feature generation, as generateAllNoteFeatureVectors() takes the 
			// list in fwd order as argument
			chordSizes = groundTruthTranscription.getNumberOfNewNotesPerChord();
		}
		// c. Both
		if (pm == ProcessingMode.BWD) {
			backwardsMapping = FeatureGenerator.getBackwardsMapping(chordSizes);
		}
		if (ma == ModellingApproach.HMM && mappingDictionary == null) {
			String parent = new File(argPaths[0]).getParent() + "/";
			mappingDictionary = 
				ToolBox.readCSVFile(ToolBox.readTextFile(new File(parent + 
				Runner.mappingDict + ".csv")), ",", true);
		}

		// 2. Set GT/voice-related information (derived from GT transcription)
		// a. For N2N, C2C, and HMM
		groundTruthVoiceLabels = groundTruthTranscription.getVoiceLabels();
		if (!isTablatureCase) {
			equalDurationUnisonsInfo = groundTruthTranscription.getEqualDurationUnisonsInfo();
			// When using the bwd model, equalDurationUnisonsInfo must be set in bwd order
			if (pm == ProcessingMode.BWD) {
				List<Integer[]> equalDurationUnisonsInfoBwd = new ArrayList<Integer[]>();
				for (int i : backwardsMapping) {
					equalDurationUnisonsInfoBwd.add(equalDurationUnisonsInfo.get(i));
				}
				equalDurationUnisonsInfo = equalDurationUnisonsInfoBwd;
			}
		}
		// b. Only for N2N and HMM
		if (ma == ModellingApproach.N2N || ma == ModellingApproach.HMM) {
			if (!modelDuration) {
				groundTruthLabels = groundTruthVoiceLabels;
			}
			if (isTablatureCase && modelDuration) {
				if (dc == DecisionContext.UNIDIR || dc == DecisionContext.BIDIR && modelDurationAgain) {
					groundTruthDurationLabels = groundTruthTranscription.getDurationLabels();
					groundTruthVoicesCoDNotes = groundTruthTranscription.getVoicesCoDNotes();
				}
				groundTruthLabels = new ArrayList<List<Double>>();
				for (int i = 0; i < groundTruthVoiceLabels.size(); i++) {
					List<Double> combined = new ArrayList<Double>();
					combined.addAll(groundTruthVoiceLabels.get(i));
					if (dc == DecisionContext.UNIDIR || dc == DecisionContext.BIDIR && modelDurationAgain) {
						combined.addAll(groundTruthDurationLabels.get(i));
					}
					groundTruthLabels.add(combined);
				}
			}
			// When using the bwd model, groundTruthVoiceLabels (and groundTruthDurationLabels) 
			// must be set in bwd order. This does not apply to groundTruthLabels and 
			// groundTruthVoicesCoDNotes, which are needed in fwd order for the feature generation,
			// and are given so as argument to generateAllNoteFeatureVectors(). groundTruthLabels
			// is no longer needed after the feature calculation and therefore need not be set in 
			// bwd order; groundTruthVoicesCoDNotes is set in bwd order after the feature calculation.
			if (pm == ProcessingMode.BWD) { // TODO this could go outside of the N2N-if
				List<List<Double>> groundTruthVoiceLabelsBwd = new ArrayList<List<Double>>();
				for (int index : backwardsMapping) {
					groundTruthVoiceLabelsBwd.add(groundTruthVoiceLabels.get(index));
				}
				groundTruthVoiceLabels = groundTruthVoiceLabelsBwd;
				if (dc == DecisionContext.UNIDIR && modelDuration || dc == DecisionContext.BIDIR && modelDuration && modelDurationAgain) {
					List<List<Double>> groundTruthDurationLabelsBwd = new ArrayList<List<Double>>();
					for (int index : backwardsMapping) {
				  		groundTruthDurationLabelsBwd.add(groundTruthDurationLabels.get(index));
					}
					groundTruthDurationLabels = groundTruthDurationLabelsBwd;
				}
			}
		}

		// 3. Set predicted transcription
		if (dc == DecisionContext.BIDIR) {
			if (m == Model.B_STAR || m == Model.B_PRIME_STAR) {
				predictedTranscription = groundTruthTranscription;
			}
			else {
				predictedTranscription =	
					ToolBox.getStoredObjectBinary(new Transcription(), 
					new File((new File(pathPredBidir)).getParent() + "/" + Runner.output + 
					"fold_" + ToolBox.zerofy(fold, ToolBox.maxLen(fold)) + "-" + 
					testPieceName + ".ser"));
			}
		}

		// 4. Test
		// a. Only MM: test in test mode only
		if (mt == ModelType.MM) {
			// Prepare allMelodyModelOutputsPerModel for setting
			allMelodyModelOutputsPerModel = new ArrayList<List<double[]>>();
			for (int i = 0; i < sliceIndices.size(); i++) {
				allMelodyModelOutputsPerModel.add(new ArrayList<double[]>());
			}

			// Evaluate and store MM outputs
			int n = modelParameters.get(Runner.N).intValue();
			for (int i = 0; i < sliceIndices.size(); i++) {
				int sliceInd = sliceIndices.get(i);
				MelodyPredictor mp = new MelodyPredictor(MelodyPredictor.getMelModelType(), 
					MelodyPredictor.getTermType(), n, sliceInd);
				mp.loadModel(new File(pathMM + Runner.melodyModel + "-" +
					MelodyPredictor.getSliceIndexString(sliceInd) + ".ser"));
				
				double[][] evalNum = testMM(mp, i);
				List<double[]> melodyModelOutputs = allMelodyModelOutputsPerModel.get(i);
				String evalString = EvaluationManager.getPerformanceSingleFoldMM(evalNum, 
					melodyModelOutputs, groundTruthVoiceLabels);
				String model = Runner.melodyModel + "-" +
					MelodyPredictor.getSliceIndexString(sliceIndices.get(i)) + "-";
				ToolBox.storeObjectBinary(evalNum, 
					new File(pathMM + model + Runner.evalMM + "-" + Runner.test + ".ser"));
				ToolBox.storeTextFile(evalString,
					new File(pathMM + model + Runner.evalMM + "-" + Runner.test + ".txt"));
				ToolBox.storeObjectBinary(melodyModelOutputs, 
					new File(pathMM + model + Runner.outputMM + "-" + Runner.test + ".ser"));
			}			
		}
		// b. Only NN or combined model: test in test and (possibly) in application mode
		if (mt == ModelType.NN || mt == ModelType.DNN || mt == ModelType.OTHER || mt == ModelType.ENS
			|| mt == ModelType.HMM) {
			double[][] minAndMaxFeatureValues = null;
			if (mt != ModelType.HMM) {
				minAndMaxFeatureValues = 
					ToolBox.getStoredObjectBinary(new double[][]{}, 
					new File(pathNN + Runner.minMaxFeatVals + ".ser"));
			}

			// Create the network. The network is not needed when using the combined model in 
			// test mode, or when using a non-NN model 
			if (!(mt == ModelType.ENS && mode == Runner.TEST) && mt != ModelType.DNN && 
				mt != ModelType.OTHER && mt != ModelType.HMM) {
				initialiseNetwork(pathNN, minAndMaxFeatureValues[0].length);
			}
			List<Double> modelWeighting = null;
			if (mt == ModelType.ENS) {
				// Prepare allMelodyModelOutputsPerModel for setting
				allMelodyModelOutputsPerModel = new ArrayList<List<double[]>>();
				for (int i = 0; i < sliceIndices.size()*ns.size(); i++) {
					allMelodyModelOutputsPerModel.add(new ArrayList<double[]>());
				}
				modelWeighting = 
					ToolBox.getStoredObjectBinary(new ArrayList<Double>(), 
					new File(pathComb + Runner.modelWeighting + ".ser"));
			}

			// Get the test results
			List<List<List<Integer>>> testResults = null;
			String tstOrAppRec = null;
			if (mode == Runner.TEST) {
				if (ma == ModellingApproach.N2N || ma == ModellingApproach.C2C) {
					testResults = 
						testInTestMode(fold, minAndMaxFeatureValues, modelWeighting, argPaths);
				}
				else if (ma == ModellingApproach.HMM) {
					testResults = testHMM(fold, argPaths, 
						Runner.ALL_CONFIGS[modelParameters.get(Runner.CONFIG).intValue()]);
				}
				tstOrAppRec = Runner.test;
				if (dc == DecisionContext.BIDIR || ma == ModellingApproach.HMM) {
					tstOrAppRec = Runner.application;
				}
			}
			else if (mode == Runner.APPL){
				testResults = 
					testInApplicationMode(fold, minAndMaxFeatureValues, modelWeighting,	argPaths);
				tstOrAppRec = Runner.application;
//				// Reset STM
//				if (combinedModel) {
//					for (int i = 0; i < sliceIndices.size(); i++) {
//						allMelodyPredictors.get(i).resetSTM(); 
//					}
//				}
			}

			// If the bwd model is used: chordSizes and groundTruthVoicesCoDNotes, both needed in
			// fwd order for the feature calculation, must still be reversed/set in bwd order
			if (pm == ProcessingMode.BWD) {
				// chordSizes: reverse
				List<Integer> chordSizesReversed = new ArrayList<Integer>(chordSizes);
				Collections.reverse(chordSizesReversed);
				chordSizes = chordSizesReversed;
				// groundTruthVoicesCoDNotes: set in bwd order
				if (dc == DecisionContext.UNIDIR && modelDuration ||
					dc == DecisionContext.BIDIR && modelDuration && modelDurationAgain) {
					List<Integer[]> groundTruthVoicesCoDNotesBwd = new ArrayList<Integer[]>();
					for (int i : backwardsMapping) {
						groundTruthVoicesCoDNotesBwd.add(groundTruthVoicesCoDNotes.get(i));
					}
					groundTruthVoicesCoDNotes = groundTruthVoicesCoDNotesBwd;
				}
			}

//			double[] overallCE = null;
			List<double[]> ntwOutputsForCE = null;
			if (mt == ModelType.NN || mt == ModelType.DNN || mt == ModelType.OTHER ) { 
				if (ma == ModellingApproach.N2N) {
//					overallCE = 
//						ErrorCalculator.calculateCrossEntropy(allNetworkOutputs, 
//						groundTruthVoiceLabels);
					ntwOutputsForCE = allNetworkOutputs;
				}
			}
			else if (mt == ModelType.ENS) { 
//				overallCE = 
//					ErrorCalculator.calculateCrossEntropy(allCombinedOutputs, 
//					groundTruthVoiceLabels);
				ntwOutputsForCE = allCombinedOutputs;
			}
//			if (ma == ModellingApproach.N2N) {
//				String strCE = 
//					"CE = " + overallCE[0] + "\r\n" + 
//					"num summed log(p) = " + overallCE[1] + "\r\n" +
//					"num full = " + overallCE[1]/overallCE[2] + "\r\n" +		
//					"den = " + overallCE[3];  
//				if (mode == Runner.TEST) {
//					ToolBox.storeTextFile(strCE, new File(storePath + "overall_CE-test-NEW.txt"));
//				}
//				else {
//					ToolBox.storeTextFile(strCE, new File(storePath + "overall_CE-appl-NEW.txt"));
//				}
//			}
			
			// Store the predicted Transcription	
			if ((mode == Runner.TEST && m.getDecisionContext() == DecisionContext.BIDIR) ||
				mode == Runner.TEST && ma == ModellingApproach.HMM || mode == Runner.APPL) {
				File encodingFile = null;
				if (isTablatureCase) {
					encodingFile = dataset.getAllEncodingFiles().get(pieceIndex);
				}
				Transcription predictedTranscr =
					new Transcription(dataset.getAllMidiFiles().get(pieceIndex).getName(), 
					encodingFile, basicTabSymbolProperties, basicNoteProperties, highestNumVoicesTraining, 
					allVoiceLabels, allDurationLabels, groundTruthTranscription.getPiece().getMetricalTimeLine(),
					groundTruthTranscription.getPiece().getHarmonyTrack());

				// Element 0 of testResults contains the assignment errors, which has
				// as element 0: general high-level voice assignment information
				// as element 1: the indices of all incorrect assignments
				// as element 2: the indices of all overlooked CoD assignments
				// as element 3: the indices of all superfluous CoD assignments
				// as element 4: the indices of all half CoD assignments
				// and, if modelling duration
				// as element 5: general high-level duration assignment information
				// as element 6: the indices of all incorrect assignments
				// as element 7: the indices of all overlooked CoD assignments
				// (see ErrorCalculator.calculateAssignmentErrors())
				List<List<Integer>> assigErrs = testResults.get(0);
				List<List<Integer>> indiv = 
					new ArrayList<List<Integer>>(assigErrs.subList(1, /*assigErrs.size()*/ 5));
				// In case of the bwd model, indices in are bwd indices and need to be reverted to fwd   
				if (pm == ProcessingMode.BWD) {
					for (int i = 0; i < indiv.size(); i++) {
						List<Integer> reverted = new ArrayList<>();
						indiv.get(i).forEach(ind -> reverted.add(backwardsMapping.get(ind)));
						indiv.set(i, reverted);
					}
				}
				List<Integer> combined = new ArrayList<>();
				indiv.forEach(l -> combined.addAll(l));
				Collections.sort(combined);
				List<List<Integer>> colInd = new ArrayList<>();
				colInd.add(combined);
				colInd.addAll(indiv);
				predictedTranscr.setColourIndices(colInd);

				String dir;
				if (useCV) {
					dir = new File(pathNN).getParent() + "/" + Runner.output + "fold_" +	
						ToolBox.zerofy(fold, ToolBox.maxLen(fold)) + "-"; 
				}
				else {
					if (applToNewData) {
						dir = pathComb;						
					}
					else {
						dir = pathNN + Runner.output;
					}
				}
				if (!applToNewData) {
					ToolBox.storeObjectBinary(predictedTranscr, new File(dir + testPieceName + ".ser"));
				}
//				Piece p = predictedTranscr.getPiece();
				String expPath = dir + testPieceName;
				List<Integer> instruments = Arrays.asList(new Integer[]{MIDIExport.TRUMPET});
				MIDIExport.exportMidiFile(predictedTranscr.getPiece(), instruments, expPath + ".mid");
				Transcription t = new Transcription(new File(expPath + ".mid"), null);
				List<Integer[]> mi = (tablature == null) ? t.getMeterInfo() : tablature.getMeterInfo();
				MEIExport.exportMEIFile(t, tablature.getBasicTabSymbolProperties(), mi,
					t.getKeyInfo(), colInd, false, expPath);
			}
			if (!applToNewData) {
				System.out.println("... storing the test results ...");

				List<List<Integer>> assigErrs = testResults.get(0);
				ErrorFraction[] evalNN = 
					EvaluationManager.getMetricsSingleFold(assigErrs, testResults.get(1), 
					ntwOutputsForCE, groundTruthVoiceLabels, equalDurationUnisonsInfo);

				// Details
				boolean applUnidirCase = (dc == DecisionContext.UNIDIR && mode == Runner.APPL); 
				boolean applBidirCase = (dc == DecisionContext.BIDIR && mode == Runner.TEST);
				boolean applHMMCase = (ma == ModellingApproach.HMM);
				List<List<List<Double>>> groundTruths = new ArrayList<List<List<Double>>>();
				groundTruths.add(groundTruthVoiceLabels);
				groundTruths.add(groundTruthDurationLabels);
				List<List<Integer[]>> additionalGroundTruths = new ArrayList<List<Integer[]>>();
				additionalGroundTruths.add(equalDurationUnisonsInfo); // TODO no longer needed
				additionalGroundTruths.add(groundTruthVoicesCoDNotes);
				String conflictRec = null;
				List<List<List<Integer>>> conflictInfo = null;
				if (ma == ModellingApproach.N2N) {		 
					if (applUnidirCase) {	
						conflictRec = conflictsRecord;
					}
					else if (applBidirCase) {
						conflictRec = conflictsRecordTest;
					}
					if (applUnidirCase || applBidirCase) {
						conflictInfo = 
							getConflictInfo(conflictIndices, allPredictedVoices, 
							allDurationLabels, equalDurationUnisonsInfo, allNetworkOutputs,
							groundTruths, backwardsMapping);
					}
				}
				String[][] detailsArr = EvaluationManager.getOutputDetails(
					groundTruths, 
//					additionalGroundTruths,
					allPredictedVoices,
					allDurationLabels,
					allMetricPositions,
					allNetworkOutputs, // always original network outputs 
					allMelodyModelOutputsPerModel,
					allCombinedOutputs,
					conflictIndices,
					chordSizes, 
					backwardsMapping,
					groundTruthTranscription.getChordVoiceLabels(),
					allPossibleVoiceAssignmentsAllChords,
					allBestVoiceAssignments,
					allHighestNetworkOutputs,
					assigErrs
				);
				
				ToolBox.storeTextFile(ToolBox.createCSVTableString(detailsArr), 
					new File(storePath + Runner.details + "-" + tstOrAppRec + ".csv"));

				// Create the new csv table(s)
				List<String[][]> newCsvTables = new ArrayList<String[][]>();
				String perf = Runner.perf;
				boolean isForDur = false;
				int iter = 1;
				if (modelDuration && dc == DecisionContext.UNIDIR) {
					iter = 2;
				}
				for (int i = 0; i < iter; i++) {				
					String[][] existingRows = null;
//					if (ma != ModellingApproach.HMM) {
					String[][] oldCsvTable = 
						ToolBox.retrieveCSVTable(ToolBox.readTextFile(new File(
						storePath + perf + ".csv")));
//					System.out.println("HIER EERSTE");
//					for (String[] str : oldCsvTable) {
//						System.out.println(Arrays.toString(str));
//					}
					int rows = 2;
					if (fold == 1) {
						rows = 4;
					}
//					String[][] existingRows = new String[rows][oldCsvTable[0].length];
					existingRows = new String[rows][oldCsvTable[0].length];
					existingRows[0] = oldCsvTable[Runner.TRAIN + 1];
					if (mode == Runner.APPL) {
						existingRows[1] = oldCsvTable[Runner.TEST + 1];
					}
					if (fold == 1) {
						existingRows[2] = oldCsvTable[oldCsvTable.length-2];
						if (mode == Runner.APPL) {
							existingRows[3] = oldCsvTable[oldCsvTable.length-1];
						}
					}
//					}
					String[][] newCsvTable = 
						EvaluationManager.createCSVTableSingleFold(fold, mode, evalNN, 
						/*otherMetr,*/ assigErrs, conflictIndices, existingRows, isForDur, 
						new long[]{tePreProcTime, ToolBox.getTimeDiffPrecise(start, 
						ToolBox.getTimeStampPrecise())});
//					System.out.println("HIER TWEEDE");
//					for (String[] str : newCsvTable) {
//						System.out.println(Arrays.toString(str));
//					}
//					System.exit(0);
					newCsvTables.add(newCsvTable);
					perf = Runner.perfDur;
					isForDur = true;
				}
				// Store the new csv table(s)
				// NB The time this takes is not included in the total runtime
				int indToUpdate = 
					Arrays.asList(newCsvTables.get(0)[0]).indexOf(EvaluationManager.Metric.RUNTIME.getStringRep());
				String s = Runner.perf;
				String end = ToolBox.getTimeStampPrecise();
				long totalTime = ToolBox.getTimeDiffPrecise(start, end);
				for (String[][] t : newCsvTables) {
					// Do not update the duration csv (which does not contain the runtimes)
					if (s.equals(Runner.perf)) {
						t[1+mode][indToUpdate] = String.valueOf(totalTime);
					}
					ToolBox.storeTextFile(ToolBox.createCSVTableString(t), 
						new File(storePath + s + ".csv"));
					s = Runner.perfDur;
				}
				Runner.addToPerfCsvFilesStoreTimes(ToolBox.getTimeDiffPrecise(end, 
					ToolBox.getTimeStampPrecise()));

				// Create performance record and store it (only in 'application' cases) 
				// NB The time this takes is not included in the total runtime
				if (applUnidirCase || applBidirCase || applHMMCase) {
					String startPrintTxtFiles = ToolBox.getTimeStampPrecise();
//					// a. Header
//					String header = EvaluationManager.makeHeader(times, "evaluation"); 
					// a. Data and parameters info
					String dataAndParams = EvaluationManager.getDataAndParamsInfo(mode, fold);
					// b. Performance info
					List<Integer> modes = new ArrayList<Integer>();
					modes.add(Runner.TRAIN);
					modes.add(Runner.TEST);
					if (dc == DecisionContext.UNIDIR && ma != ModellingApproach.HMM) {
						modes.add(Runner.APPL);
					}
					String performance = 
						EvaluationManager.getPerformanceInfo(newCsvTables, modes, -1, assigErrs, null);

					String rec = dataAndParams.concat("\r\n").concat(performance);		
//						performance).concat("\r\n").concat(
//						details); 
//					ToolBox.storeTextFile(rec, new File(storePath + testOrApplRec + ".txt"));
					ToolBox.storeTextFile(rec, new File(storePath + Runner.perf + ".txt"));
					
					// Store details files
					if (Runner.textify) {
						// Details train (if it does not exist yet)
						if (ma != ModellingApproach.HMM) {
							File f = new File(storePath + Runner.details + "-" + Runner.train + ".txt"); 
							if (!(f.exists())) {
								String[][] detailsArrTr = 
									ToolBox.retrieveCSVTable(ToolBox.readTextFile(new File(storePath + 
									Runner.details + "-" + Runner.train + ".csv")));
								String detailsTr = EvaluationManager.listDetails(detailsArrTr, null);
								ToolBox.storeTextFile(detailsTr, f);
							}
						}
						// Details test (does not apply for B and H models)
//						String tstOrApp = Runner.test;
//						if (dc == DecisionContext.BIDIR && mode == Runner.TEST) {
//							tstOrApp = Runner.application;
//						}
//						String tstOrApp = tstOrAppRec;
						if (applUnidirCase) {
							String[][] detailsArrTe = 
								ToolBox.retrieveCSVTable(ToolBox.readTextFile(
								new File(storePath + Runner.details + "-" + Runner.test + ".csv")));
							String detailsTe = 
								EvaluationManager.listDetails(detailsArrTe, null);
							ToolBox.storeTextFile(detailsTe, new File(storePath + 
								Runner.details + "-" + Runner.test + ".txt"));
						}
//						String[][] detailsArrTe = 
//							ToolBox.retrieveCSVTable(ToolBox.readTextFile(
//							new File(storePath + Runner.details + "-" + tstOrAppRec + ".csv")));

//						String detailsTe = null;
//						if (applUnidirCase || applHMMCase) {
//							detailsTe = EvaluationManager.listDetails(detailsArrTe, null);
//						}
//						else if (applBidirCase) {
//							detailsTe = EvaluationManager.listDetails(detailsArrTe, conflictRec);
//						}
//						ToolBox.storeTextFile(detailsTe, 
//							new File(storePath + Runner.details + "-" + tstOrAppRec + ".txt"));
						
						// Details application
						String[][] detailsArrApp = 
							ToolBox.retrieveCSVTable(ToolBox.readTextFile(new File(storePath + 
							Runner.details + "-" + Runner.application + ".csv")));
						// conflictsRec is null for H model
						String detailsApp = EvaluationManager.listDetails(detailsArrApp, conflictRec);
						ToolBox.storeTextFile(detailsApp, new File(storePath + 
							Runner.details + "-" + Runner.application + ".txt"));
//						if (applUnidirCase) {
//							String[][] detailsArrApp = 
//								ToolBox.retrieveCSVTable(ToolBox.readTextFile(new File(storePath + 
//								Runner.details + "-" + tstOrAppRec /*Runner.application*/ + ".csv")));
//							String detailsApp = 
//								EvaluationManager.listDetails(detailsArrApp, conflictRec);
//							ToolBox.storeTextFile(detailsApp, new File(storePath + 
//								Runner.details + "-" + tstOrAppRec /*Runner.application*/ + ".txt"));
//						}
					}	
					Runner.addToPerfAndDetailsTxtFilesStoreTimes(ToolBox.getTimeDiffPrecise(startPrintTxtFiles, 
						ToolBox.getTimeStampPrecise()));
				}
			}
//			if (applToNewData) {
//				System.out.println(conflictsRecord);
//			}
		}
	}
	
	
	/**
	 * Generates the observations for the HMM.
	 * 
	 */
	// TESTED for both tablature- and non-tablature case
	public List<List<Integer>> generateObservations() {
		List<List<Integer>> observations = new ArrayList<List<Integer>>();		

		int numberOfChords = -1;
		if (tablature != null) {
			numberOfChords = tablature.getTablatureChords().size();
		}
		else {
			numberOfChords = groundTruthTranscription.getTranscriptionChords().size();
		}
		for (int j = 0; j < numberOfChords; j++) {
			List<Integer> pitchesInChord = null;
			if (tablature != null) {
				pitchesInChord = tablature.getPitchesInChord(j);
			}
			else {
				pitchesInChord = groundTruthTranscription.getPitchesInChord(j);
			}
			Collections.sort(pitchesInChord);
			observations.add(pitchesInChord);
		}
		return observations;
	}


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
	private List<List<List<Integer>>> evaluate(int fold, String[] argPaths) {
		String path = argPaths[0];

//		File outputFile =
//			new File(path + "fold_" + ToolBox.zerofy(fold, 2) + "/" + 
//			Runner.outputs + "-" + Runner.application + ".csv");
//		List<List<Integer>> predictedIndicesWrapped = 
//			ToolBox.readCSVFile(ToolBox.readTextFile(outputFile), ",", false);
//		List<Integer> predictedIndices = new ArrayList<Integer>();
//		for (List<Integer> l : predictedIndicesWrapped) {
//			predictedIndices.add(l.get(0));
//		}
//
//		File mappingDictFile = new File(path + Runner.mappingDict + ".csv");
//		List<List<Integer>> mappingDictionary = 
//			ToolBox.readCSVFile(ToolBox.readTextFile(mappingDictFile), ",", true);

		// 4. Evaluate and store	
		// Convert predictedIndices into a List of voices
//		List<List<Integer>> allPredictedVoices = 
//			getVoicesFromMappingIndices(predictedIndices, mappingDictionary);
////		System.out.println(allPredictedVoices);
//
//		List<List<Integer>> assignmentErrors =
//			ErrorCalculator.calculateAssignmentErrors(allPredictedVoices, 
//			groundTruthVoiceLabels, null, null, equalDurationUnisonsInfo);
//		
		List<List<List<Integer>>> testResults = new ArrayList<List<List<Integer>>>();
//		testResults.add(assignmentErrors);
//		testResults.add(allPredictedVoices);
		return testResults; 
	}
	
	
	/**
	 * Given a list of predicted mapping indices, finds for each index the corresponding mapping in
	 * the given mapping dictionary, and returns, for each note in the piece the mapping indices 
	 * were predicted for, the voice(s) assigned to that note. 
	 * 
	 * @param mappingIndices
	 * @param mappingDictionary
	 * @return
	 */
	// TESTED
	List<List<Integer>> getVoicesFromMappingIndices(List<Integer> mappingIndices) { 
//		List<List<Integer>> mappingDictionary) {
		List<List<Integer>> allVoices = new ArrayList<List<Integer>>();
		for (int index : mappingIndices) {
			List<List<Double>> currChordVoiceLabels = 
				DataConverter.getChordVoiceLabels(mappingDictionary.get(index));
			allVoices.addAll(DataConverter.getVoicesInChord(currChordVoiceLabels));    	
		}
		return allVoices;
	}


	private List<List<List<Integer>>> testHMM(int fold, String[] argPaths, Configuration config) {		
		List<List<List<Integer>>> testResults = new ArrayList<List<List<Integer>>>();

		// Generate and store observations.csv so that they can be read by evaluate_HMM.m
		// TODO Ideally, the observations should not be stored and then loaded inside the Matlab
		// script, but given directly as input to the Matlab script
		List<List<Integer>> observations = generateObservations();
		ToolBox.storeListOfListsAsCSVFile(observations, 
			new File(argPaths[0] + Runner.observations + ".csv"));

		// Create the Matlab command
		String cmd = "matlab -wait -nosplash -nodesktop -r \"";
		cmd += "code_path = '" + Runner.scriptPathMatlab + "'; ";
		cmd += "exp_path = '" + Paths.get(argPaths[0]).getParent().toString().replace("\\", "/") + "/" + "'; ";
		cmd += "addpath(genpath(code_path)); "; // make all dirs in the code path availalbe to Matlab
		cmd += "addpath(genpath(exp_path)); "; // make all dirs in the experiment path available to Matlab
		// NB: the order of the elements in fileNamesList must be the same as in the MATLAB code
		String fileNamesList = "{" +
			"'" + Runner.TPM + ".csv'" + ", " +
			"'" + Runner.OPM + ".csv'" + ", " +
			"'" + Runner.ISM + ".csv'" + ", " +
			"'" + Runner.observations + ".csv'" + ", " +
			"'" + Runner.chordDict + ".csv'" + ", " +
			"'" + Runner.outputs + ".csv'" + 
			"}";
		cmd += "evaluate_HMM(" + fold + ", exp_path, " + config.getUniformTPM() + ", " + 
			config.getUniformISM() + ", " + fileNamesList + "); ";
		cmd += "exit\""; // exit Matlab     
		System.out.println(cmd);
		
		// Call Matlab to evaluate the HMM and generate and store model_output.csv
		// TODO Ideally, the outputs should not be stored and then loaded by the Java code, but 
		// returned directly as output of the Matlab script (and then stored as .csv/.ser files)
		boolean matlabInstalled = false;
		if (matlabInstalled) {
			Runtime rt = Runtime.getRuntime();
			try {
//				System.out.println("Process OPENED");
				Process proc = rt.exec(cmd);
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				String line = null;
				while((line = stdInput.readLine()) != null) {
					System.out.println(line);
				}
				BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
				StringBuilder err = new StringBuilder();
				String e = null;
				while ((e = stdError.readLine()) != null) {
					err.append(e + "\n");
				}
				if (err.length() != 0) {
					throw new IOException(err.toString());
				}
//				System.out.println("Process CLOSED");
			} catch (IOException e1) {
				e1.printStackTrace();
			} //catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}

		// Load indices predicted by the HMM and add them, together with the accompanying 
		// mappings, to the lists
		File outputFile = new File(argPaths[0] + Runner.outputs + ".csv");
		List<List<Integer>> predictedIndicesWrapped = 
			ToolBox.readCSVFile(ToolBox.readTextFile(outputFile), ",", false);
		List<Integer> predictedIndices = new ArrayList<Integer>();
		allHighestNetworkOutputs = new ArrayList<Double>();
		allBestVoiceAssignments = new ArrayList<List<Integer>>();
		for (List<Integer> l : predictedIndicesWrapped) {
			int predIndex = l.get(0);
			predictedIndices.add(predIndex);
			allHighestNetworkOutputs.add((double)predIndex);
			allBestVoiceAssignments.add(mappingDictionary.get(predIndex));
		}

		// Get predicted voices and voice labels 
		allPredictedVoices = getVoicesFromMappingIndices(predictedIndices);
		allVoiceLabels = new ArrayList<List<Double>>();
		for (List<Integer> l : allPredictedVoices) {
			allVoiceLabels.add(DataConverter.convertIntoVoiceLabel(l));
		}

		// Get test results
		List<List<Integer>> assignmentErrors =
			ErrorCalculator.calculateAssignmentErrors(allPredictedVoices, groundTruthVoiceLabels, 
			null, null, equalDurationUnisonsInfo);

		testResults.add(assignmentErrors);
		testResults.add(allPredictedVoices);
		return testResults;
	}


	private double[][] testMM(MelodyPredictor mp, int i) {
//		MelodyPredictor mp = allMelodyPredictors.get(i);
		Map<String, Double> modelParameters = Runner.getModelParams();
		
		List<double[]> melodyModelOutputs = 
			FeatureGenerator.generateAllMMOutputs(modelParameters, basicTabSymbolProperties, 
			basicNoteProperties, groundTruthTranscription, chordSizes, mp);
		allMelodyModelOutputsPerModel.set(i, melodyModelOutputs);		
		// Evaluate
		double[][] evalNum = EvaluationManager.getMetricsSingleFoldMM(melodyModelOutputs, 
			groundTruthVoiceLabels);
		// Reset STM
		mp.resetSTM(); // TODO necessary? Doesn't this happen automatically because new MMs are loaded in the next fold (next call to this method) anyway?
		return evalNum;
	}


	/**
	 * Tests the network in test mode, where the features are generated using the ground truth voice assignments.
	 * Returns the assignment errors.
	 * 
	 * @param modelParameters
	 * @param minAndMaxFeatureValues
	 * @param pieceName
	 * @return
	 */
	private List<List<List<Integer>>> testInTestMode(int fold, double[][] minAndMaxFeatureValues,
		List<Double> modelWeighting, String[] argPaths) {
		List<List<List<Integer>>> testResults = new ArrayList<List<List<Integer>>>();

		Map<String, Double> modelParameters = Runner.getModelParams();
//		List<Integer> sliceIndices = 
//			ToolBox.decodeListOfIntegers(modelParameters.get(Runner.SLICE_IND_ENC_SINGLE_DIGIT).intValue(), 1);
		ModellingApproach ma = 
			Runner.ALL_MODELLING_APPROACHES[modelParameters.get(Runner.MODELLING_APPROACH).intValue()];	
		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()]; 
//		List<Integer> ns = 
//			ToolBox.decodeListOfIntegers(modelParameters.get(Runner.NS_ENC_SINGLE_DIGIT).intValue(), 1);
		ModelType mt = m.getModelType();
		DecisionContext dc = m.getDecisionContext();
		int decisionContextSize = modelParameters.get(Runner.DECISION_CONTEXT_SIZE).intValue();
		boolean modelDuration = m.getModelDuration();
		boolean modelDurationAgain = 
			ToolBox.toBoolean(modelParameters.get(Runner.MODEL_DURATION_AGAIN).intValue());
		int highestNumberOfVoicesTraining = Runner.getHighestNumVoicesTraining();
//			modelParameters.get(Runner.HIGHEST_NUM_VOICES).intValue();
//		Implementation im = 
//			Runner.ALL_IMPLEMENTATIONS[modelParameters.get(Runner.IMPLEMENTATION).intValue()];

		List<Integer> sliceIndices = null;
		if (mt == ModelType.MM || mt == ModelType.ENS) {
			sliceIndices = 
				ToolBox.decodeListOfIntegers(modelParameters.get(Runner.SLICE_IND_ENC_SINGLE_DIGIT).intValue(), 1);
		}
		List<Integer> ns = null;
		if (mt == ModelType.ENS) {		
			ns = ToolBox.decodeListOfIntegers(modelParameters.get(Runner.NS_ENC_SINGLE_DIGIT).intValue(), 1);
		}
		
		// Set paths
		String pathNN = null;
		String pathMM = null;
//		String pathComb = null;
		if (mt == ModelType.NN || mt == ModelType.DNN || mt == ModelType.OTHER) {
			pathNN = argPaths[0];
		}
		else if (mt == ModelType.ENS) {		
//			pathComb = argPaths[0];
			pathNN = argPaths[1];
			pathMM = argPaths[2];
		}

		// 1.  Calculate features, get networkoutputs, determine predicted voices (and durations)
		List<Rational[]> allPredictedDurationsTest = null; 
		if (mt == ModelType.NN || mt == ModelType.DNN || mt == ModelType.OTHER) {
			// a. Get the features and scale them, using the minimum and maximum feature values  
			// from the training
			if (ma == ModellingApproach.N2N) {   
//				if (im == Implementation.PHD) {
				// TODO make single call to generateAllNoteFeatureVectors() that decides which one to call
				// based on given featVec?
				if (dc == DecisionContext.UNIDIR) {
					noteFeatures = 
						FeatureGenerator.generateAllNoteFeatureVectors(modelParameters, 
						basicTabSymbolProperties, groundTruthVoicesCoDNotes, basicNoteProperties, 
						groundTruthTranscription, groundTruthLabels, meterInfo, chordSizes);
				}
				else {					
					List<Integer[]> predictedVoicesCoDNotes = predictedTranscription.getVoicesCoDNotes();
					// Determine predictedLabels
					List<List<Double>> predictedVoiceLabels = predictedTranscription.getVoiceLabels();
					List<List<Double>> predictedDurationLabels = predictedTranscription.getDurationLabels();
					List<List<Double>> predictedLabels = new ArrayList<List<Double>>();
					for (int i = 0; i < predictedVoiceLabels.size(); i++) {
						List<Double> currPredictedLabel = new ArrayList<Double>(predictedVoiceLabels.get(i));
						if (modelDuration) {
							currPredictedLabel.addAll(predictedDurationLabels.get(i));
						}
						predictedLabels.add(currPredictedLabel);
					}
					noteFeatures = 
						FeatureGenerator.generateAllBidirectionalNoteFeatureVectors(modelParameters,
						basicTabSymbolProperties, predictedVoicesCoDNotes, basicNoteProperties, 
						predictedTranscription, predictedLabels, meterInfo, chordSizes, 
						modelDuration, decisionContextSize);
				}
//				}
				boolean isMuSci = false; // TODO
				if (isMuSci) {
//				else if (im == Implementation.MU_SCI) {
					int featureSetUsedForTraining = FeatureGenerator.featureSetSwitch;
					boolean useTablatureInformation = FeatureGenerator.tabInfoSwitch;
					noteFeatures = 
						FeatureGenerator.generateAllNoteFeatureVectorsMUSCI(basicTabSymbolProperties,
						basicNoteProperties, groundTruthTranscription, featureSetUsedForTraining,
						useTablatureInformation);
				}
				noteFeatures = FeatureGenerator.scaleSetOfFeatureVectors(noteFeatures, 
					minAndMaxFeatureValues,	ma);

				boolean mmAsInput = false;
				if (mmAsInput) {
//					// Append the (unscaled) melody model outputs to the scaled note features 
//					for (int i = 0; i < noteFeatures.size(); i++) {
//						List<Double> currFV = noteFeatures.get(i);
//						List<double[]> currMMOutp = new ArrayList<double[]>();
//						for (List<double[]> l : allMelodyModelOutputsPerModel) {
//							currMMOutp.add(l.get(i));
//						}
//						noteFeatures.set(i, currFV);
//						// TODO combine 
//					}
//					for (List<Double> fv : noteFeatures) {
//						allNetworkOutputsTest.add(networkManager.evalNetwork(fv));
//					}
				}
			}
			else if (ma == ModellingApproach.C2C) {
				List<List<List<Integer>>> orderedVoiceAssignments = 
					FeatureGeneratorChord.getOrderedVoiceAssignments(basicTabSymbolProperties,
					basicNoteProperties, groundTruthVoiceLabels, highestNumberOfVoicesTraining);
				chordFeatures = 
					FeatureGeneratorChord.generateAllChordFeatureVectors(basicTabSymbolProperties, 
					basicNoteProperties, groundTruthTranscription, meterInfo, orderedVoiceAssignments);
				chordFeatures = FeatureGenerator.scaleSetOfSetsOfFeatureVectors(chordFeatures, 
					minAndMaxFeatureValues, ma);
			} 

			// b. Get the network outputs
			if (ma == ModellingApproach.N2N) {
				if (mt == ModelType.NN) {
					allNetworkOutputs = networkManager.createAllNetworkOutputs(noteFeatures);
					File f = new File(pathNN + Runner.outputs + ".ser");
					List<List<double[]>> outputs = 
						ToolBox.getStoredObjectBinary(new ArrayList<List<double[]>>(), f);
					outputs.set(Runner.TEST, new ArrayList<double[]>(allNetworkOutputs));
					if (Runner.storeNetworkOutputs) {
						ToolBox.storeObjectBinary(outputs, f);
					}
				}
				if (mt == ModelType.DNN || mt == ModelType.OTHER) {
					// Store features and classes
					List<List<List<Double>>> data = new ArrayList<List<List<Double>>>();
					data.add(noteFeatures);
					data.add(groundTruthVoiceLabels);
					TrainingManager.storeData(pathNN, Runner.test + ".csv", data);

					boolean doThis = true;
					if (doThis) {
					// Apply the model
					boolean isScikit = false;
					// For scikit
					if (isScikit) {
						String[] cmd = new String[]{
							"python", Runner.scriptPathPython, m.name(), "test", pathNN, 
							Runner.fvExt, Runner.clExt, Runner.outpExt,
							Runner.getOtherParam(modelParameters)};
					}
					// For TensorFlow
					String extensions = Runner.fvExt + "," + Runner.lblExt + "," + Runner.outpExt;
//					int param = modelParameters.get(Runner.NUM_HIDDEN_LAYERS).intValue();
					String paramsAndHyperparams = 
						"hidden layers=" + modelParameters.get(Runner.NUM_HIDDEN_LAYERS).intValue() + "," +
						"input layer size=" + noteFeatures.get(0).size() + "," +
						"hidden layer size=" + modelParameters.get(Runner.HIDDEN_LAYER_SIZE).intValue() + "," +
						"output layer size=" + Transcription.MAXIMUM_NUMBER_OF_VOICES + "," +
						"learning rate=" + modelParameters.get(Runner.LEARNING_RATE) + "," +
						"keep probability=" + modelParameters.get(Runner.KEEP_PROB) + "," +
						"epochs=" + modelParameters.get(Runner.EPOCHS).intValue();
					String[] cmd = new String[]{
						"python", 
						Runner.scriptPathPython + Runner.script, 
						m.name(), 
						Runner.test, 
						pathNN, 
						extensions, 
						paramsAndHyperparams, 
						"true"};
					System.out.println(Arrays.toString(cmd));
					PythonInterface.applyModel(cmd);
					
					// Retrieve the model output
					String[][] outpCsv = 
						ToolBox.retrieveCSVTable(ToolBox.readTextFile(new File(pathNN + 
						Runner.outpExt + "tst.csv")));
					List<double[]> predictedOutputs = ToolBox.convertCSVTable(outpCsv);
					
					// +1.0E-6
//					for (double[] d : predictedOutputs) {
//						for (int i = 0; i < d.length; i++) {
//							if (d[i] == 0.0) {
//								d[i] += 0.000001;
//							}
//						}
//					}
					
					allNetworkOutputs = predictedOutputs;
					} // doThis
				}
			}
			else if (ma == ModellingApproach.C2C) {
				List<List<Double>> allNetwOutpAllChords =
					networkManager.createAllNetworkOutputsForAllChords(chordFeatures);
				allHighestNetworkOutputs =
					OutputEvaluator.createAllHighestNetworkOutputs(allNetwOutpAllChords);
				allPossibleVoiceAssignmentsAllChords = 
					FeatureGeneratorChord.getOrderedVoiceAssignments(basicTabSymbolProperties,
					basicNoteProperties, groundTruthVoiceLabels, highestNumberOfVoicesTraining);
				allBestVoiceAssignments = 
					OutputEvaluator.createAllBestVoiceAssignments(allNetwOutpAllChords,
					allPossibleVoiceAssignmentsAllChords);
			}

			// c. Determine the predicted voices and durations
			allPredictedVoices = OutputEvaluator.determinePredictedVoices(modelParameters, 
				allNetworkOutputs, allBestVoiceAssignments);
			if (dc == DecisionContext.UNIDIR && modelDuration || dc == DecisionContext.BIDIR && modelDuration &&
				modelDurationAgain) {
				allPredictedDurationsTest = OutputEvaluator.determinePredictedDurations(modelParameters, 
					allNetworkOutputs, allBestVoiceAssignments);
			}
		}
		else if (mt == ModelType.ENS) {
			// a. Get stored NN and MM outputs
			allNetworkOutputs = ToolBox.getStoredObjectBinary(new ArrayList<List<double[]>>(), 
				new File(pathNN + Runner.outputs + ".ser")).get(Runner.TEST);
			for (int i = 0; i < sliceIndices.size(); i++) {
				String model = Runner.melodyModel + "-" +
					MelodyPredictor.getSliceIndexString(sliceIndices.get(i)) + "-";
				for (int nInd = 0; nInd < ns.size(); nInd++) {
					String currEnd = "/n=" + ns.get(nInd) + "/" + "fold_" +
						ToolBox.zerofy(fold, ToolBox.maxLen(fold)) + "/";
					pathMM = pathMM.concat(currEnd);
					List<double[]> curr = 
						ToolBox.getStoredObjectBinary(new ArrayList<double[]>(), 
						new File(pathMM + model + Runner.outputMM + "-" + Runner.test + ".ser"));
					pathMM = pathMM.substring(0, pathMM.indexOf(currEnd)); 
					// Determine index
					int index = i*ns.size() + nInd;
					allMelodyModelOutputsPerModel.set(index, curr);
				}
			}	
			// Combine outputs
			allCombinedOutputs =
				OutputEvaluator.createCombinedNetworkOutputs(allNetworkOutputs, 
				allMelodyModelOutputsPerModel, modelWeighting);

			// b. Determine the predicted voices and durations
			allPredictedVoices = OutputEvaluator.determinePredictedVoices(modelParameters, 
				allCombinedOutputs, allBestVoiceAssignments);
			if (dc == DecisionContext.UNIDIR && modelDuration || dc == DecisionContext.BIDIR && modelDuration
				&& modelDurationAgain) {
				allPredictedDurationsTest = OutputEvaluator.determinePredictedDurations(modelParameters, 
					allCombinedOutputs, allBestVoiceAssignments);
			}
		}

		// 2. Determine assignment errors and set testResults	
		// If applicable: resolve conflicts (adapts allPredictedVoices where necessary)
		if (dc == DecisionContext.BIDIR) {
			conflictIndices = new ArrayList<List<Integer>>();
			conflictIndices.add(new ArrayList<Integer>());
			conflictIndices.add(new ArrayList<Integer>());
			conflictsRecordTest = "";
			List<Integer> considerSolved = new ArrayList<Integer>();
			boolean resolved = false;
			while (resolved == false) {
				resolved = 
					resolveConflictsBidirectional(allNetworkOutputs, 
					considerSolved, highestNumberOfVoicesTraining);
			}
		}

		// When using bwd model:
		// allPredictedVoices = bwd
		// groundTruthVoiceLabels = bwd
		// allPredictedDurationsTest = bwd
		// groundTruthDurationLabels = bwd
		// equalDurationUnisonsInfo = bwd
		List<List<Integer>> assignmentErrors =
			ErrorCalculator.calculateAssignmentErrors(allPredictedVoices, groundTruthVoiceLabels, 
			allPredictedDurationsTest, groundTruthDurationLabels, equalDurationUnisonsInfo);

		// When using B or H model: set allVoiceLabels and allDurationLabels (needed for storing
		// the Transcription created)
		if (dc == DecisionContext.BIDIR) {
			allVoiceLabels = new ArrayList<List<Double>>();
			for (List<Integer> l : allPredictedVoices) {
				allVoiceLabels.add(DataConverter.convertIntoVoiceLabel(l));
			}
			if (modelDuration && modelDurationAgain) {
				allDurationLabels = new ArrayList<List<Double>>();
				for (Rational[] durs : allPredictedDurationsTest) {
					List<Integer> durations = new ArrayList<Integer>();
					for (Rational r : durs) {
						durations.add(DataConverter.getIntegerEncoding(r) - 1);
					} 
					allDurationLabels.add(DataConverter.convertIntoDurationLabel(durations));
				}
			}
		}

		testResults.add(assignmentErrors);
		testResults.add(allPredictedVoices);
		return testResults;
	}


	private boolean resolveConflictsBidirectional(List<double[]> allNetworkOutputsTest, 
		List<Integer> considerSolved, int highestNumberOfVoices) {

		Map<String, Double> modelParameters = Runner.getModelParams();
		
		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()]; 
		boolean modelDuration = m.getModelDuration();
		boolean isTablatureCase = Runner.getDataset().isTablatureSet();		
		ProcessingMode pm = 
			Runner.ALL_PROC_MODES[modelParameters.get(Runner.PROC_MODE).intValue()];

		boolean printOut = true;
		boolean alsoFixSustained = false;
		if (!isTablatureCase) {
			alsoFixSustained = true;
		}

		// 0. List the chords and the indices per chords
		List<List<Integer>> indicesPerChord = null;
		if (isTablatureCase) {
//			indicesPerChord = tablature.getIndicesPerChord(pm); // reverse if bwd
			indicesPerChord = tablature.getIndicesPerChord((pm == ProcessingMode.BWD)); // reverse if bwd
		}
		else {
//			indicesPerChord = groundTruthTranscription.getIndicesPerChord(pm); // reverse if bwd
			indicesPerChord = groundTruthTranscription.getIndicesPerChord((pm == ProcessingMode.BWD)); // reverse if bwd
		}
		
		// 1. List the predicted and GT voices per chord
		List<List<List<Integer>>> voicesPerChord = new ArrayList<List<List<Integer>>>(); // reverse if bwd
		List<List<List<Integer>>> voicesPerChordGT = new ArrayList<List<List<Integer>>>(); // reverse if bwd
		// For each chord
		int numChords = indicesPerChord.size();
		int startIndex = 0;
		for (int i = 0; i < numChords; i++) {
			List<List<Integer>> voicesCurrChord = new ArrayList<List<Integer>>();
			List<List<Integer>> voicesCurrChordGT = new ArrayList<List<Integer>>();
			
			int endIndex = startIndex + indicesPerChord.get(i).size();
			// For each note in the chord at index i: add the voices predicted for it to voicesCurrChord
			// NB: works in both fwd and bwd mode because indicesPerChord is in bwd order when modelling backwards
			for (int j = startIndex; j < endIndex; j++) {
				voicesCurrChord.add(allPredictedVoices.get(j));
				voicesCurrChordGT.add(DataConverter.convertIntoListOfVoices(groundTruthVoiceLabels.get(j)));
			}
			// When the chord is traversed: add voicesCurrChord to voicesPerChord
			voicesPerChord.add(voicesCurrChord);
			voicesPerChordGT.add(voicesCurrChordGT);
			startIndex = endIndex;	
		}
				
		// 2. List all incorrect chords. There are two sorts of incorrect chords:
		// (1) sustained chords contain a note that is assigned to a voice that is 
		//     sustained from a previous chord
		// (2) double chords contain two notes that are assigned to the same voice 
		List<Integer> indicesOfIncorrChords = new ArrayList<Integer>();
		List<Integer> indicesOfChordsWithSustained = new ArrayList<Integer>(); // reverse if bwd
		List<Integer> indicesOfChordsWithDoubles = new ArrayList<Integer>(); // reverse if bwd
		List<List<Integer>> indicesOfSustainedNotesPerChord = new ArrayList<List<Integer>>();
		// allSustainedVoices/allDoublesVoices are size numChords and contain for each
		// element either a List<Integer> indicating the sustained/double voices for the note 
		// at that index, or null
		List<List<Integer>> allSustainedVoices = new ArrayList<List<Integer>>(); // reverse if bwd
		List<List<Integer>> allDoublesVoices = new ArrayList<List<Integer>>(); // reverse if bwd
		for (int i = 0; i < numChords; i++) {
			allSustainedVoices.add(null);
			allDoublesVoices.add(null);
			indicesOfSustainedNotesPerChord.add(null);
		}
		for (int i = 0; i < voicesPerChord.size(); i++) {
			int currentIndex = i;
			List<List<Integer>> currChord = voicesPerChord.get(currentIndex);
			// Make a single list with all the voices in the currentChord, including any duplicates
			List<Integer> allVoicesInCurrChord = new ArrayList<Integer>();
			for (List<Integer> n : currChord) {
				allVoicesInCurrChord.addAll(n);
			}
			
			// a. If the chord contains sustained voices
			if (isTablatureCase && modelDuration || !isTablatureCase) {
				List<Integer> sustainedVoices = new ArrayList<Integer>();
				List<Integer> noteIndicesCurrChord = indicesPerChord.get(currentIndex);
				// indexLowestNoteInChord must be in fwd order
				int indexLowestNoteInChord = noteIndicesCurrChord.get(0);
				if (pm == ProcessingMode.BWD) {
					indexLowestNoteInChord = backwardsMapping.get(noteIndicesCurrChord.get(0));
				}
				// Get the indices (these are in fwd order) of the sustained notes
				List<Integer> indicesOfSustainedNotes = 
					Transcription.getIndicesOfSustainedPreviousNotes(basicTabSymbolProperties, 
					predictedTranscription.getDurationLabels(), basicNoteProperties, 
					indexLowestNoteInChord);
				indicesOfSustainedNotesPerChord.set(currentIndex, indicesOfSustainedNotes);
				// Get the sustained voices
				for (int ind : indicesOfSustainedNotes) {
					// In the tablature case: use the voice information as predicted by the 
					// first pass model 
					if (isTablatureCase) {
//						List<Double> vl = predictedTranscription.getVoiceLabels().get(ind);// TODO: klopt dit??
//						sustainedVoices.addAll(dataConverter.convertIntoListOfVoices(vl));
						sustainedVoices.addAll(allPredictedVoices.get(ind));
					}
					// In the non-tablature case
					else {
						sustainedVoices.addAll(allPredictedVoices.get(ind));
					}
				}
				Collections.sort(sustainedVoices);
				Collections.reverse(sustainedVoices);				
				
				// For all voices v in the current chord
				for (int v : allVoicesInCurrChord) {
					// If v is a sustained voice: add, set, and break
					if (sustainedVoices.contains(v)) {
						if (!indicesOfIncorrChords.contains(currentIndex)) {// && !considerSolved.contains(currentIndex)) {
							indicesOfIncorrChords.add(currentIndex);
						}
//						if (!considerSolved.contains(currentIndex)) {
							indicesOfChordsWithSustained.add(currentIndex);
//						}
						allSustainedVoices.set(currentIndex, sustainedVoices);
						break;
					}
				}		
			}
			
			// b. If the chord contains double voices  
			List<Integer> currentDoublesVoices = new ArrayList<Integer>();
			// For all voices v in the current chord
			for (int v : allVoicesInCurrChord) {
				// Add all v occurring more than once
				if (Collections.frequency(allVoicesInCurrChord, v) > 1) {
					if (!currentDoublesVoices.contains(v)) {
						currentDoublesVoices.add(v);
					}
				}
			}
			Collections.sort(currentDoublesVoices);
			Collections.reverse(currentDoublesVoices);
			// Doubles found? Add and set
			if (currentDoublesVoices.size() != 0) {
				if (!indicesOfIncorrChords.contains(currentIndex)) {
					indicesOfIncorrChords.add(currentIndex);
				}
				indicesOfChordsWithDoubles.add(currentIndex);
				allDoublesVoices.set(currentIndex, currentDoublesVoices);
			}
		}
		
		if (printOut) {
			System.out.println("METHOD STARTED");
			System.out.println("indicesOfIncorrChords " + indicesOfIncorrChords);
			System.out.println("indicesOfChordsWithDoubles (" + indicesOfChordsWithDoubles.size() + ") " 
				+ indicesOfChordsWithDoubles);
			System.out.println("allDoublesVoices " + allDoublesVoices);	
			System.out.println("indicesOfChordsWithSustained (" + indicesOfChordsWithSustained.size() + ") " 
				+ indicesOfChordsWithSustained);
			System.out.println("allSustainedVoices " + allSustainedVoices);
			System.out.println("indicesOfSustainedNotesPerChord " + indicesOfSustainedNotesPerChord);
		}
						
		// 3. Resolve conflicts
		// For each incorrect chord
		for (int i = 0; i < indicesOfIncorrChords.size(); i++) {
			
			int currentIndex = indicesOfIncorrChords.get(i);	
			List<List<Integer>> voicesIncorrChord = voicesPerChord.get(currentIndex); // size = numChords
			List<List<Integer>> voicesCorrChord = voicesPerChordGT.get(currentIndex); // size = numChords
			List<Integer> noteIndicesIncorrChord = indicesPerChord.get(currentIndex); // size = numChords
			if (printOut) {
				System.out.println("  currentIndex:      " + currentIndex);
				System.out.println("  voicesIncorrChord: " + voicesIncorrChord);
				System.out.println("  voicesCorrChord:   " + voicesCorrChord);
				System.out.println("  note indices:      " + noteIndicesIncorrChord);
				System.out.println("  ----------");
			}
			// 1. Determine the voices for any sustained notes
			List<Integer> currentSustainedVoices = new ArrayList<Integer>();
			List<Integer> voicesAvailableWithSustained = new ArrayList<Integer>();
			List<Integer> indicesOfNoteWithSustainedVoice = new ArrayList<Integer>();
			List<Integer> voicesForOtherChordNotes = new ArrayList<Integer>();
			if (isTablatureCase && modelDuration || !isTablatureCase) {
				if (indicesOfChordsWithSustained.contains(currentIndex)) {
					currentSustainedVoices = allSustainedVoices.get(currentIndex);

					// Determine the indices of the chord notes with the sustained voice
					for (int j = 0; j < voicesIncorrChord.size(); j++) {
						for (int v : currentSustainedVoices) {
							if (voicesIncorrChord.get(j).contains(v)) {
								int index = noteIndicesIncorrChord.get(j);
								if (!indicesOfNoteWithSustainedVoice.contains(index)) {
									indicesOfNoteWithSustainedVoice.add(index);
								}
							}
						}
					}
				}
				
				// Determine the voices for the chord notes that do not have a sustained voice (which are voices
				// not available for the notes that have the sustained voice)
				for (int j = 0; j < noteIndicesIncorrChord.size(); j++) {
					int index = noteIndicesIncorrChord.get(j);
					if (!indicesOfNoteWithSustainedVoice.contains(index)) {
						voicesForOtherChordNotes.addAll(voicesIncorrChord.get(j));
					}
				}
				
				// Determine the available voices
				for (int v = 0; v < highestNumberOfVoices; v++) {
					// Do not add sustained voices and voices for other chord notes
					if (!currentSustainedVoices.contains(v) && !voicesForOtherChordNotes.contains(v)) {
						voicesAvailableWithSustained.add(v);
					}
				}										
			}
			if (printOut) {
				System.out.println("  currentSustainedVoices:          " + currentSustainedVoices);
				System.out.println("  voicesForOtherChordNotes         " + voicesForOtherChordNotes);
				System.out.println("  indicesOfNoteWithSustainedVoice: " + indicesOfNoteWithSustainedVoice);
				System.out.println("  voicesAvailableWithSustained:    " + voicesAvailableWithSustained);
				System.out.println("  ----------");
			}
			
			// 2. Determine the voices for any double notes
			List<Integer> currentDoublesVoices = new ArrayList<Integer>();
			List<Integer> voicesAvailableWithDouble = new ArrayList<Integer>();
			List<Integer> indicesOfNoteWithDoubleVoice = new ArrayList<Integer>();
			if (indicesOfChordsWithDoubles.contains(currentIndex)) {
				currentDoublesVoices = allDoublesVoices.get(currentIndex);
				
				// Determine the indices of the chord notes with the double voice
				for (int j = 0; j < voicesIncorrChord.size(); j++) {
					for (int v : currentDoublesVoices) {
						if (voicesIncorrChord.get(j).contains(v)) {
							int index = noteIndicesIncorrChord.get(j);
							if (!indicesOfNoteWithDoubleVoice.contains(index)) {
								indicesOfNoteWithDoubleVoice.add(index);
							}
						}
					}
				}
				
				// ASSUMPTION 2: No more than two notes are assigned to the voice occurring more than once
				if (indicesOfNoteWithDoubleVoice.size() > 2) {
//						throw new RuntimeException("ERROR: More than two notes are assigned to the voice occuring " + 
//							"more than once in chord " + currentIndex);
				}
													
				// Determine all voices in the chord
				List<Integer> allCurrentVoices = new ArrayList<Integer>();
				for (List<Integer> l : voicesIncorrChord) {
					allCurrentVoices.addAll(l);
				}
				// In the non-tablature case: determine also all voices for sustained notes
				List<Integer> sustainedVoices = new ArrayList<Integer>();
				if (!isTablatureCase) {
					List<Integer> noteIndicesCurrChord = indicesPerChord.get(currentIndex);
					// indexLowestNoteInChord must be in fwd order
					int indexLowestNoteInChord = noteIndicesCurrChord.get(0);
					if (pm == ProcessingMode.BWD) {
						indexLowestNoteInChord = backwardsMapping.get(noteIndicesCurrChord.get(0));
					}
					List<Integer> indicesOfSustainedNotes = 
						Transcription.getIndicesOfSustainedPreviousNotes(basicTabSymbolProperties, 
						null, basicNoteProperties, indexLowestNoteInChord);
					// Get the sustained voices
					for (int ind : indicesOfSustainedNotes) {	
						// In the tablature case: use the voice information as predicted by the first pass model 
						if (isTablatureCase) {
//							List<Double> vl = predictedTranscription.getVoiceLabels().get(ind);// TODO: klopt dit??
//							sustainedVoices.addAll(dataConverter.convertIntoListOfVoices(vl));
							sustainedVoices.addAll(allPredictedVoices.get(ind));
						}
						// In the non-tablature case
						else {
							sustainedVoices.addAll(allPredictedVoices.get(ind));
						}
					}
				}
						
				// Determine the available voices
				for (int v = 0; v < highestNumberOfVoices; v++) {
					if (!allCurrentVoices.contains(v) && !sustainedVoices.contains(v)) {
//					if (!allCurrentVoices.contains(v)) {
						voicesAvailableWithDouble.add(v);
					}
				}			
			}
					
			if (printOut) {
				System.out.println("  currentDoublesVoices:         " + currentDoublesVoices);
				System.out.println("  indicesOfNoteWithDoubleVoice: " + indicesOfNoteWithDoubleVoice);
				System.out.println("  voicesAvailableWithDouble:    " + voicesAvailableWithDouble);
				System.out.println("  ----------");
			}	
			
			if (!isTablatureCase && indicesOfNoteWithSustainedVoice.size() > 1) {
//				throw new RuntimeException("ERROR: More than one note with a sustained voice");
			}
			if (currentDoublesVoices.size() > 2) {
				throw new RuntimeException("ERROR: More than two notes with a double voice");
			}
			
			// Determine voicesAvailable
			List<Integer> voicesAvailable = new ArrayList<Integer>();
			boolean containsSustained = indicesOfChordsWithSustained.contains(currentIndex);
			boolean containsDouble = indicesOfChordsWithDoubles.contains(currentIndex);
			// In the case of a sustained voice
			if (containsSustained && !containsDouble) {
				voicesAvailable = voicesAvailableWithSustained;
				if (printOut) {
					System.out.println("  Contains SUSTAINED");
				}
			}
			// In the case of a double voice
			else if (!containsSustained && containsDouble) {
				voicesAvailable = voicesAvailableWithDouble;
				if (printOut) {
					System.out.println("  Contains DOUBLE");
				}
			}
			// In the case of both
			else if (containsSustained && containsDouble) {
				List<Integer> availableSustained = new ArrayList<Integer>();
				// Sustained notes not taken into consideration? Add all voices to availableSustained
				if (alsoFixSustained == false) {
					for (int j = 0; j < highestNumberOfVoices; j++) {
						availableSustained.add(j); 
					}
				}
				// Else: set availableSustained to voicesAvailableWithSustained
				else {
					availableSustained = new ArrayList<Integer>(voicesAvailableWithSustained);
				}
				
				// Add only elements that are in both availableSustained and voicesAvailableWithDouble to 
				// voicesAvailable
				for (int v : availableSustained) {
					if(voicesAvailableWithDouble.contains(v)) {
						voicesAvailable.add(v);
					}
				}
				if (printOut) {
					System.out.println("  Contains SUSTAINED AND DOUBLE");
				}
			}
			Collections.sort(voicesAvailable);
			Collections.reverse(voicesAvailable);
			if (printOut) {
				System.out.println("  voicesAvailable = " + voicesAvailable);
			}
			
			// ASSUMPTION 1: There are never more available voices than the highest number of voices - 1
			if (voicesAvailable.size() > (highestNumberOfVoices - 1)) {
				throw new RuntimeException("ERROR: There are too many (" + voicesAvailable.size() + 
					") available voices in chord " + currentIndex); 
			}
								
			// a. If the chord contains a sustained note	
			if (containsSustained && alsoFixSustained && !considerSolved.contains(currentIndex)) {						
				// For each note that has a sustained voice
				for (int susIndex : indicesOfNoteWithSustainedVoice) {
					int indexToReset = susIndex;		
					List<Integer> correctedVoice = null;
					double[] networkOutput = allNetworkOutputsTest.get(susIndex);
					double highestOutput = -Double.MAX_VALUE;
					
					// Only in tablature case (?): if there are no available voices because there are too
					// many incorrectly sustained voices: reinstall all sustained notes
					// NB: The problem of having too many incorrectly sustained notes will remain (the durations 
					// are never adapted). Thus, susIndex must be added to considerSolved, so that upon restart
					// of the method the chord is no longer considered containing sustained voices
					if (voicesAvailable.isEmpty()) {	
						for (int v : currentSustainedVoices) {
							voicesAvailable.add(v);
						}
						considerSolved.add(currentIndex);
	
//						List<Integer> newVoicesAvailable = new ArrayList<Integer>();
//						// First check if any of the sustained notes is a CoD
//						List<Integer> curr = indicesOfSustainedNotesPerChord.get(currentIndex);
//						for (int c : curr) {
//							List<Double> origPredVoiceLabel = predictedTranscription.getVoiceLabels().get(c);
//							// Get the new predicted label (allPredictedVoices is in bwd order when
//							// modelling bwd) 
//							List<Double> newPredVoiceLabel =
//								dataConverter.convertIntoVoiceLabel(allPredictedVoices.get(c));
//							if (modelBackward) {
//								newPredVoiceLabel = 
//									dataConverter.convertIntoVoiceLabel(allPredictedVoices.get(backwardsMapping.indexOf(c)));
//							}
//							
//							if (Transcription.containsCoD(origPredVoiceLabel)) {
//								// Only if the newly predicted voice does not contain a CoD
//								if (!Transcription.containsCoD(newPredVoiceLabel)) {
//									// Add the first note to newVoicesAvailable
//									// NB It is better to add the firstPredicted voice (i.e., the one
//									// with the higher network output), but this information is not 
//									// available at this point
//									newVoicesAvailable.add(origPredVoiceLabel.indexOf(1.0));
//									
//								}
//							}
//						}
						
					}
					// Determine correctedVoice
					for (int av: voicesAvailable) {
						double outputForAV = networkOutput[av];
						if (outputForAV > highestOutput) {
							highestOutput = outputForAV; 
							correctedVoice = Arrays.asList(new Integer[]{av});
						}
					}
					List<Integer> oldVoice = allPredictedVoices.get(indexToReset);
					allPredictedVoices.set(indexToReset, correctedVoice);
//					String conflictText = 
//						getConflictText(indexToReset, oldVoice, correctedVoice, 
//						containsSustained, containsDouble);

					if (!conflictIndices.get(0).contains(indexToReset)) {
						conflictIndices.get(0).add(indexToReset);			
					}
					
					List<Integer> susInd = indicesOfSustainedNotesPerChord.get(currentIndex);
//					String susIndStr = "";
//					if (susInd.size() == 1) {
//						susIndStr = String.valueOf(susInd.get(0));
//					}
//					else {
//						susIndStr = ToolBox.StringifyList(susInd);
//					}
					String conflictText = 
						"type (vi) conflict between notes " + indexToReset + " and " +
						String.valueOf(susInd).replace("[", "").replace("]", "") + 
						": note " + indexToReset + " reassigned to voice " + 
						correctedVoice + "\r\n";	
					
					if (printOut) {
						System.out.println("  --> " + conflictText);
					}
					conflictsRecordTest = conflictsRecordTest.concat(conflictText);
					return false;
				}
			}

			// b. If the chord contains (also) a double note
			if (containsDouble) {
				boolean escapeMethod = false;

				// For each note that has a double voice
				int indexToReset = -1;
				List<Integer> correctedVoice = null;
				
				// Always start with the first doublesVoice; if there are more, the next will
				// be dealt with in the next iteration of the method
				int currentDoubleVoice = currentDoublesVoices.get(0);
				
				// a. If voicesAvailable is empty: this is due to a CoD that is not a double
				// (e.g. [3+2, 1, 1, 0]. Remove the second predicted voice of that CoD
				if (isTablatureCase && voicesAvailable.isEmpty()) {
					for (int j = 0; j < voicesIncorrChord.size(); j++) {
						List<Integer> currentVoices = voicesIncorrChord.get(j);
						if (currentVoices.size() == 2 && !currentVoices.contains(currentDoubleVoice)) {
							indexToReset = noteIndicesIncorrChord.get(j);
							correctedVoice = Arrays.asList(new Integer[]{currentVoices.get(0)});
//							List<Integer> oldVoice = allPredictedVoices.get(indexToReset);
							allPredictedVoices.set(indexToReset, correctedVoice);
							escapeMethod = true;
							break;
						}
					}
				}
				
				if (escapeMethod == false) {
					// b. If one of the notes in the chord has a CoD with a voice that is a 
					// double: find this CoDnote and determine indexToReset and correctedVoice
//					for (int v : currentDoublesVoices) {	
					// Find the CoDnote and adapt it
					for (int j = 0; j < voicesIncorrChord.size(); j++) {
						List<Integer> currentVoices = voicesIncorrChord.get(j);
						if (currentVoices.size() == 2) {
							indexToReset = noteIndicesIncorrChord.get(j);
							// Is v a 2nd predicted CoDnote? Remove
							if (currentVoices.get(1) == currentDoubleVoice) { // v) {
								correctedVoice = 
									Arrays.asList(new Integer[]{currentVoices.get(0)});
							}
							// Is v a 1st predicted CoDnote? Change into the second predicted CoDnote
							else if (currentVoices.get(0) == currentDoubleVoice) { // v) {
								correctedVoice = 
									Arrays.asList(new Integer[]{currentVoices.get(1)});	
							}
							// Only if correctedVoice is not null anymore, i.e., if the CoDnote was indeed a double note.
							// E.g. CoD in [2, 2, 1+0] when currentDoubleVoice = 2 is not corrected  
							if (correctedVoice != null) {
//								List<Integer> oldVoice = allPredictedVoices.get(indexToReset);
								allPredictedVoices.set(indexToReset, correctedVoice);							
								escapeMethod = true;
								break;
							}
						}				
					}
				}
//				}
				
				if (escapeMethod == false) {
					// c. If there are no conflicts with CoDnotes
					// Set doubleIndicesToCheck to indicesOfNoteWithDoubleVoice
					List<Integer> doubleIndicesToCheck = 
						new ArrayList<Integer>(indicesOfNoteWithDoubleVoice);
					// If there is more than one double voice (i.e., if there is more than one
					// pair of indices): determine the indices going with the first double voice
					if (currentDoublesVoices.size() > 1) {
						// Empty doubleIndicesToCheck; then add only the indices that have voice v to it 
						doubleIndicesToCheck = new ArrayList<Integer>();
						for (int v : currentDoublesVoices) {
							for (int in : indicesOfNoteWithDoubleVoice) {
								if (allPredictedVoices.get(in).contains(v)) {
									doubleIndicesToCheck.add(in);
								}
							}
							// First pair of indices found: break
							break;
						}
					}
								
					// Check the first (or only) pair of indices
					double highestOutput = -Double.MAX_VALUE;
					// For all double notes
					for (int doubleIndex : doubleIndicesToCheck) {					
						// For all available voices 
						for (int av : voicesAvailable) {
							double outputForCurrCombination = 
								allNetworkOutputsTest.get(doubleIndex)[av];
							if (outputForCurrCombination > highestOutput) {
								highestOutput = outputForCurrCombination;
								indexToReset = doubleIndex;
								correctedVoice = Arrays.asList(new Integer[]{av});
							}
						}
					}

//					List<Integer> oldVoice = allPredictedVoices.get(indexToReset);
					allPredictedVoices.set(indexToReset, correctedVoice);
					escapeMethod = true;
				}

				if (escapeMethod == true) {
					if (!conflictIndices.get(0).contains(indexToReset)) {
						conflictIndices.get(0).add(indexToReset);			
					}
					String conflictText = 
						"type (vii) conflict between notes " + 
						String.valueOf(indicesOfNoteWithDoubleVoice).replace("[", "").replace("]", "")	+ 
						": note " + indexToReset + " reassigned to voice " + 
						correctedVoice + "\r\n";	
					
					if (printOut) {
						System.out.println("  --> " + conflictText);
					}
					conflictsRecordTest = conflictsRecordTest.concat(conflictText);
					return false;
				}
			}				
		}			
		return true;
	}


	private String getConflictText(int indexToReset, List<Integer> oldVoice, 
		List<Integer> correctedVoice, boolean containsSustained, boolean containsDouble	) {

		List<Integer> gtVoices = 
			DataConverter.convertIntoListOfVoices(groundTruthVoiceLabels.get(indexToReset));

		String conflict = "";
		String type = "";
		if (containsSustained && !containsDouble) {
			conflict = " (sustained voice conflict)";
			type = "type (vi) conflict";
		}
		if (containsDouble && !containsSustained) {
			conflict = " (double voice conflict)";
			type = "type (vii) conflict";
		}
		if (containsDouble && containsSustained) {
			conflict = " (sustained and double voice conflict)";
		}

		// correctedVoices always contains only one element (see above)
		int corrected = correctedVoice.get(0);
		String gtInfo = "(ground truth voices = " + gtVoices + " --> ";
		// If GT is not a CoD
		if (gtVoices.size() == 1 && gtVoices.get(0) == corrected) {
			gtInfo = gtInfo.concat("correct adaptation) " + conflict);
		}
		// If GT is a CoD
		else if (gtVoices.size() == 2 && gtVoices.contains(corrected)) {
			gtInfo = gtInfo.concat("semi-correct adaptation) " + conflict);
		}
		else {
			gtInfo = gtInfo.concat("incorrect adaptation) " + conflict);
		}
		
		// type (ii) conflict between notes 1 and 0: note 0 reassigned to voice [2]
		
		int prev = -1;
		conflictIndices.get(0).add(indexToReset);
		return type + " between notes " + indexToReset + " and " + prev + ": note " + 
			indexToReset + " reassigned to voice " + correctedVoice + "\r\n";	
					
//		return "Voices at noteIndex = " + indexToReset + " (" + oldVoice + ") adapted to " +
//			correctedVoice + " " + gtInfo + "\r\n";	
	}


	private String getConflictTextOLD(int indexToReset, List<Integer> oldVoice, 
		List<Integer> correctedVoice, boolean containsSustained, boolean containsDouble	) {
		List<Integer> gtVoices = DataConverter.convertIntoListOfVoices(groundTruthVoiceLabels.get(indexToReset));
		
		String conflict = "";
		if (containsSustained && !containsDouble) {
			conflict = " (sustained voice conflict)";
		}
		if (containsDouble && !containsSustained) {
			conflict = " (double voice conflict)";
		}
		if (containsDouble && containsSustained) {
			conflict = " (sustained and double voice conflict)";
		}
		
		// correctedVoices always contains only one element (see above)
		int corrected = correctedVoice.get(0);
		String gtInfo = "(ground truth voices = " + gtVoices + " --> ";
		// If GT is not a CoD
		if (gtVoices.size() == 1 && gtVoices.get(0) == corrected) {
			gtInfo = gtInfo.concat("correct adaptation) " + conflict);
		}
		// If GT is a CoD
		else if (gtVoices.size() == 2 && gtVoices.contains(corrected)) {
			gtInfo = gtInfo.concat("semi-correct adaptation) " + conflict);
		}
		else {
			gtInfo = gtInfo.concat("incorrect adaptation) " + conflict);
		}
					
		return "Voices at noteIndex = " + indexToReset + " (" + oldVoice + ") adapted to " +
			correctedVoice + " " + gtInfo + "\r\n";	
	}
	
	
	/**
	 * Makes an empty Transcription with the given time signature, key signature, and number of voices.
	 *
	 * @param timeSig
	 * @param keyMarker
	 * @param numberOfVoices
	 * @return
	 */
	private Transcription makeEmptyTranscription(MetricalTimeLine mtl, /*TimeSignature timeSig,
	 	KeyMarker keyMarker,*/ int numberOfVoices) {
//		newTranscription = new Transcription();
		Transcription newTranscription = new Transcription();
//		NotationSystem notationSystem = newTranscription.createNotationSystem();

		Piece p = new Piece();
		p.setMetricalTimeLine(mtl);
		newTranscription.setPiece(p);
		NotationSystem notationSystem = newTranscription.getPiece().createNotationSystem();

		// Add time and key signatures
//		MetricalTimeLine mtl = newTranscription.getPiece().getMetricalTimeLine();

//		TimeSignatureMarker timeSigMarker = 
//			new TimeSignatureMarker(timeSig.getNumerator(), timeSig.getDenominator(), 
//			new Rational(0, 1));
//		timeSigMarker.setTimeSignature(timeSig);
//		mtl.add(timeSigMarker);
//		mtl.add(keyMarker);

		// Create staves
		for (int i = 0; i < numberOfVoices; i++) { 
			NotationStaff staff = new NotationStaff(notationSystem);
			// Ensure correct cleffing for each staff: G-clef for the upper two and F-clef for the lower three
			if (i < 2) {
				staff.setClefType('g', -1, 0);
			}
			else {
				staff.setClefType('f', 1, 0);
			}
			notationSystem.add(staff);
			NotationVoice notationVoice = new NotationVoice(staff);
			staff.add(notationVoice);
		}

		// Set the intial NoteSequence and voice labels
		newTranscription.initialiseNoteSequence();
		newTranscription.initialiseVoiceLabels();

		return newTranscription;
	}	

	boolean overruleHeuristic = false;
	double threshold = 1.5;
	/**
	 * Predicts the voice(s) for the note at the given noteIndex. More precisely:
	 * 1. converts the note to a Note and adds it to allNotes;
	 * 2. generates the note feature vector and adds it to noteFeatures;
	 * 3. evaluates the note feature vector and adds the result, the network output (i.e., activation values), to  
	 *    allNetworkOutputs and allNetworkOutputsAdapted;
	 * After having interpreted the network output and having resolved any conflicts:
	 * 4. adds the predicted voice(s) to allPredictedVoices;
	 * 5. sets the predicted voice(s)'s representation as a voice label in allVoiceLabels;
	 * and, in the tablature case when modelDuration == <code>true</code>:
	 * 6. adds the predicted duration(s) to allPredictedDurations;
	 * 7. sets the predicted duration(s)'s representation as a duration label in allDurationLabels;
	 * 8. sets the predicted voices for CoDNotes in allVoicesCoDNotes.
	 * 
	 * NB1: 4.-8. happen inside resolveConflicts(); in this method allNetworkOutputsAdapted gets its final
	 *     content as well. 
	 * NB2: When interpreting the network output, i.e., when deciding on the predicted voices, conflicts with any
	 *     voices already taken in the chord may arise, and these must be resolved. As a result, the predicted
	 *     voice(s) may end up being not a direct interpretation of the network output, but one of an adapted
	 *     version of it, in which those elements corresponding to voices already taken in the chord are given
	 *     an activation value of 0.0, which makes them unavailable for the current prediction.  
	 * 
	 * @param path
	 * @param minAndMaxFeatureValues
	 * @param noteIndex
	 * @param voiceEntryInfo
	 * @param modelWeighting
	 */
	private void predictVoices(String path, double[][] minAndMaxFeatureValues, int noteIndex, 
		List<List<Integer>> voiceEntryInfo, List<Double> modelWeighting) {
//		int featureSetUsedForTraining = 
//			trainingSettingsAndParameters.get(EncogNNManager.FEATURE_SET).intValue();

		Map<String, Double> modelParameters = Runner.getModelParams();
		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()];
		boolean avgProc = ToolBox.toBoolean(modelParameters.get(Runner.AVERAGE_PROX).intValue());
		
//		List<Integer> sliceIndices = 
//			ToolBox.decodeListOfIntegers(modelParameters.get(Runner.SLICE_IND_ENC_SINGLE_DIGIT).intValue(), 1);
//		List<Integer> ns = 
//			ToolBox.decodeListOfIntegers(modelParameters.get(Runner.NS_ENC_SINGLE_DIGIT).intValue(), 1);

		ModelType mt = m.getModelType();
		DecisionContext dc = m.getDecisionContext();
		int decisionContextSize = modelParameters.get(Runner.DECISION_CONTEXT_SIZE).intValue();
		boolean modelDuration = m.getModelDuration();		
//		DatasetID di = 
//			Dataset.ALL_DATASET_IDS[modelParameters.get(Dataset.DATASET_ID).intValue()];
		boolean isTablatureCase = Runner.getDataset().isTablatureSet();
		ProcessingMode pm = 
			Runner.ALL_PROC_MODES[modelParameters.get(Runner.PROC_MODE).intValue()];
//		Implementation im = 
//			Runner.ALL_IMPLEMENTATIONS[modelParameters.get(Runner.IMPLEMENTATION).intValue()];	
//		int highestNumVoicesTraining = modelParameters.get(Runner.HIGHEST_NUM_VOICES).intValue();
		int highestNumVoicesTraining = Runner.getHighestNumVoicesTraining();
		FeatureVector featVec = 
			Runner.ALL_FEATURE_VECTORS[modelParameters.get(Runner.FEAT_VEC).intValue()];
		List<Integer> sliceIndices = null;
		if (mt == ModelType.MM || mt == ModelType.ENS) {
			sliceIndices = 
				ToolBox.decodeListOfIntegers(modelParameters.get(Runner.SLICE_IND_ENC_SINGLE_DIGIT).intValue(), 1);
		}
		List<Integer> ns = null;
		if (mt == ModelType.ENS) {		
			ns = ToolBox.decodeListOfIntegers(modelParameters.get(Runner.NS_ENC_SINGLE_DIGIT).intValue(), 1);
		}
		
		// 1. Get the current Note, add that Note to currentNoteList, and add currentNoteList to allNotes
		// NB: If a CoD will be predicted for currentNote, the second Note is added in addNote(). The element at index
		// noteIndex in allNotes will thus contain a List<Note> of size 1 at this point
		Note currentNote = null;
		// a. In the tablature case: first convert the TabSymbol into a Note 
		if (isTablatureCase) {
			currentNote = Tablature.convertTabSymbolToNote(basicTabSymbolProperties, noteIndex);
		}
		// b. In the non-tablature case
		else {
			currentNote = groundTruthTranscription.getNoteSequence().getNoteAt(noteIndex);
		}
//		// Set the ticks for currentNote
//		if (noteIndex < 10) {
//			System.out.println(noteIndex);
//			PerformanceNote pn = currentNote.getPerformanceNote();
//			ScoreNote sn = currentNote.getScoreNote();
//			Rational ons = sn.getMetricTime();
//			Rational dur = sn.getMetricDuration();
//			long onTick = (long) ons.mul(1024).toDouble();
//			long offTick = onTick + ((long) dur.mul(1024).toDouble());
////			System.out.println(onTick);
////			System.out.println(offTick);
////			System.exit(0);
//			pn.setTime(onTick);
//			pn.setDuration(offTick);
//		}
//		if (noteIndex >= 10) {
//			System.exit(0);
//		}

		List<Note> currentNoteList = new ArrayList<Note>();
		currentNoteList.add(currentNote);
		allNotes.add(currentNoteList);

		if (voiceEntryInfo == null) {
//			System.out.println("IS NU NULL!!");
		}

		// HIER BEGIN1
//		else {
		// 2. Generate the note feature vector for currentNote and scale it; then add it to noteFeatures
		List<Double> currentNoteFeatureVector =	null; 
//		List<Double> currentMelodyModelOutput = null;
//		double[] currentMelodyModelOutput = null;
		List<double[]> currentMelodyModelOutputs = null;
//		if (im == Implementation.PHD) {
		// TODO make single call to generateNoteFeatureVector() that decides which one to call
		// based on given featVec
		if (dc == DecisionContext.UNIDIR) {
			currentNoteFeatureVector = 
				FeatureGenerator.generateNoteFeatureVectorDISS(basicTabSymbolProperties,
				allDurationLabels, allVoicesCoDNotes, basicNoteProperties, newTranscription, 
				currentNote, allVoiceLabels, meterInfo, noteIndex, modelDuration, pm, featVec,
				decisionContextSize, avgProc);
		}
		else {
			// NB: allDurationLabels, allVoicesCoDNotes, and allVoiceLabels are predicted (see testInApplicationMode())
			currentNoteFeatureVector = 
				FeatureGenerator.generateBidirectionalNoteFeatureVector(basicTabSymbolProperties,
				allDurationLabels, allVoicesCoDNotes, basicNoteProperties, newTranscription, 
				currentNote, allVoiceLabels, meterInfo, noteIndex, modelDuration,
				decisionContextSize, avgProc);
		}
		if (mt == ModelType.ENS) {
//			currentNoteFeatureVector = featureGenerator.generateNoteFeatureVectorPlus(basicTabSymbolProperties,
//				allDurationLabels, allVoicesCoDNotes, basicNoteProperties, newTranscription, currentNote, allVoiceLabels, 
//				meterInfo, noteIndex, highestNumberOfVoicesTraining, modelDuration, 
//				modelBackward, melodyPredictor);

//			currentMelodyModelOutput = featureGenerator.generateMelodyModelOutput(basicTabSymbolProperties, 
//				basicNoteProperties, newTranscription, currentNote, highestNumberOfVoicesTraining,
//				melodyPredictor);
			currentMelodyModelOutputs = new ArrayList<double[]>();
			for (int i = 0; i < sliceIndices.size()*ns.size(); i++) { // new
				MelodyPredictor currMp = allMelodyPredictors.get(i);
				double[] currentMelodyModelOutput = 
					FeatureGenerator.generateMelodyModelOutput(basicTabSymbolProperties, basicNoteProperties, 
					newTranscription, currentNote, highestNumVoicesTraining, currMp);
				allMelodyModelOutputsPerModel.get(i).add(currentMelodyModelOutput);
				currentMelodyModelOutputs.add(currentMelodyModelOutput);
			}
//			melodyModelOutputs.add(currentMelodyModelOutput);
		}
//		}
		boolean isMuSci = false; // TODO
		if (isMuSci) {
//		else if (im == Implementation.MU_SCI) {
//			int featureSetUsedForTraining = 
//				trainingSettingsAndParameters.get(EncogNNManager.FEATURE_SET).intValue();
			int featureSetUsedForTraining = FeatureGenerator.featureSetSwitch; // TODO
			boolean useTablatureInformation = FeatureGenerator.tabInfoSwitch; // TODO
			currentNoteFeatureVector = FeatureGenerator.generateNoteFeatureVectorMUSCI(basicTabSymbolProperties,
				basicNoteProperties, newTranscription, currentNote, noteIndex, featureSetUsedForTraining,
				useTablatureInformation); 
		}
//		FeatureGenerator.scaleFeatureVector(currentNoteFeatureVector, minAndMaxFeatureValues, ExperimentRunner.NOTE_TO_NOTE);
		currentNoteFeatureVector = FeatureGenerator.scaleFeatureVector(currentNoteFeatureVector, 
			minAndMaxFeatureValues, ModellingApproach.N2N);
		noteFeatures.add(currentNoteFeatureVector);

		// 3. Evaluate currentNoteFeatureVector and add the result, the network output, to 
		// allNetworkOutputs as well as to allNetworkOutputsAdapted
		double[] currentNetworkOutput = null;
		if (mt == ModelType.NN) {
			currentNetworkOutput = networkManager.evalNetwork(currentNoteFeatureVector);
		}
		else if (mt == ModelType.DNN || mt == ModelType.OTHER){
			if (noteIndex % 50 == 0) {
				System.out.println("processing note " + noteIndex);
			}

			// Apply the model
//			String[] cmd = new String[]{
//				"python", Runner.scriptPath, m.name(), "appl", path, 
//				/*fv,*/ Runner.fvExt, Runner.clExt, Runner.outpExt};
//			String ext = Runner.fvExt + "_appl.csv";

//			currentNetworkOutput = PythonInterface.applyModel(cmd);
//			System.out.println(Arrays.toString(currentNetworkOutput));

			boolean storeFiles = true;

			if (storeFiles) {
				// Store feature vector
				List<List<List<Double>>> data = new ArrayList<List<List<Double>>>();
				List<List<Double>> fvWrapped = new ArrayList<List<Double>>();
				fvWrapped.add(currentNoteFeatureVector);
				data.add(fvWrapped);
				List<List<Double>> lblWrapped = new ArrayList<List<Double>>();
				lblWrapped.add(groundTruthVoiceLabels.get(noteIndex));
				data.add(lblWrapped);
				TrainingManager.storeData(path, Runner.application + ".csv", data);
				boolean isScikit = false;
				// For scikit
				if (isScikit) {
					PythonInterface.predict(new String[]{
						path, m.name(), 
						Runner.fvExt + "app.csv",
						Runner.outpExt + "app.csv"
					});
				}
				// For TensorFlow
				String fvAsStr = currentNoteFeatureVector.toString();
				String fv = fvAsStr.substring(1, fvAsStr.length()-1);
				PythonInterface.predict(new String[]{
					path, m.name(), fv, Runner.application
				});
					
				// Retrieve the model output
				String[][] outpCsv = 
					ToolBox.retrieveCSVTable(ToolBox.readTextFile(new File(path + 
					Runner.outpExt + "app.csv")));
					
				List<double[]> predictedOutputs = ToolBox.convertCSVTable(outpCsv);
//				System.out.println(Arrays.toString(predictedOutputs.get(0)));

				// +1.0E-6
//				for (double[] d : predictedOutputs) {
//					for (int i = 0; i < d.length; i++) {
//						if (d[i] == 0.0) {
//							d[i] += 0.000001;
//						}
//					}
//				}
				currentNetworkOutput = predictedOutputs.get(0);
			}
			else {
				String asStr = currentNoteFeatureVector.toString();
				String fv = asStr.substring(1, asStr.length()-1);

//				String fv = ""; 
//				for (int i = 0; i < currentNoteFeatureVector.size(); i++) {
//					double d = currentNoteFeatureVector.get(i);
//					fv = fv.concat(Double.toString(d));
//					if (i != currentNoteFeatureVector.size() - 1) {
//						fv = fv.concat(",");
//					}
//				}
				currentNetworkOutput = PythonInterface.predictNoLoading(fv);
			}
		}
			
		// Check whether the heuristic must be overruled 
		if (noteIndex == 0 && voiceEntryInfo != null) {
			List<Double> asList = new ArrayList<Double>();
			for (double d : currentNetworkOutput) {
				asList.add(d);
			}
			overruleHeuristic = Collections.max(asList) > threshold;
		}
		if (overruleHeuristic) {
			System.out.println("HEURISTIC OVERRULED!");
			System.exit(0);
		}
		
		// Do not use heuristic outcome if the heuristic is not used, if it is used but 
		// overruled, or if it is used but is not applicable to the note
		if (voiceEntryInfo == null || 
			(voiceEntryInfo != null && overruleHeuristic) ||
			(voiceEntryInfo != null && !overruleHeuristic && !voiceEntryInfo.get(1).contains(noteIndex))) {
//			double[] currentNetworkOutput = networkManager.evalNetwork(currentNoteFeatureVector);		
			allNetworkOutputs.add(currentNetworkOutput); // always original NN output
			// output must be cloned so that the original network outputs (in allNetworkOutputs) remain 
			// unchanged and the adapted network outputs are added to allNetworkOutputsAdapted
			double[] copyOfCurrentNetworkOutput = null;
			if (mt == ModelType.NN || mt == ModelType.DNN || mt == ModelType.OTHER) {
				copyOfCurrentNetworkOutput = currentNetworkOutput.clone();
				allNetworkOutputsAdapted.add(currentNetworkOutput); // TODO allNetworkOutputsAdapted is never actually used
			}
			if (mt == ModelType.ENS) {
//				allNetworkOutputsBeforeCombining.add(currentNetworkOutput); 
				// Calculate combined output
				List<double[]> allOutputs = new ArrayList<double[]>();
				allOutputs.add(currentNetworkOutput);
				allOutputs.addAll(currentMelodyModelOutputs);
				double[] currCombinedNetwOutp = 
					OutputEvaluator.combineNetworkOutputs(allOutputs, modelWeighting);
//				currentNetworkOutput = currCombinedNetwOutp;
				copyOfCurrentNetworkOutput = currCombinedNetwOutp.clone();
				allNetworkOutputsAdapted.add(currCombinedNetwOutp); // TODO allNetworkOutputsAdapted is never actually used
				allCombinedOutputs.add(currCombinedNetwOutp);
			}

//			allNetworkOutputs.add(currentNetworkOutput); // always original NN output
//			if (onlyNN) {
//				allNetworkOutputsAdapted.add(currentNetworkOutput);
//			}
//			else if (combinedModel) {
//				allNetworkOutputsAdapted.add(currCombinedNetwOutp);
//			}
 
//			double[] copyOfCurrentNetworkOutput = null;
//			if (onlyNN) {
//				copyOfCurrentNetworkOutput = currentNetworkOutput.clone();
//			}
//			else if (combinedModel) {
//				copyOfCurrentNetworkOutput = currCombinedNetwOutp.clone();
//			}

			// 4. Interpret currentNetworkOutput: resolve any conflicts with voices already taken in the
			// chord and then determine the predicted voice(s) for the current note. Add the predicted 
			// voice(s) to allPredictedVoices and the predicted voice(s)'s representation as a voice label 
			// to allVoiceLabels.
			resolveConflicts(copyOfCurrentNetworkOutput, noteIndex);
		} // NEWWWW
		// Use heuristic outcome if the heuristic is used and is applicable to the note
		if (voiceEntryInfo != null && !overruleHeuristic && voiceEntryInfo.get(1).contains(noteIndex)) {	
			int v = voiceEntryInfo.get(2).get(voiceEntryInfo.get(1).indexOf(noteIndex));
				
			// Lists otherwise handled in predictVoices()
			// .add(): (allNotes), noteFeatures, allNetworkOutputs, allNetworkOutputsAdapted
			// and, if mt == ModelType.ENS: allCombinedOutputs, allMelodyModelOutputsPerModel
			// .set(): -
			noteFeatures.add(null);
			double[] outp = new double[Transcription.MAXIMUM_NUMBER_OF_VOICES];
			Arrays.fill(outp, 0.0);
			outp[v] = 1.0;
			allNetworkOutputs.add(outp);
			allNetworkOutputsAdapted.add(outp);
			if (mt == ModelType.ENS) {
				allCombinedOutputs.add(null);
				for (int i = 0; i < sliceIndices.size()*ns.size(); i++) {
					allMelodyModelOutputsPerModel.get(i).add(null);			
				}
			}
			// Lists otherwise handled in resolveConflicts()
			// .add(): allPredictedVoices, allPredictedDurations
			// .set(): (allNotes), (allPredictedVoices), (allPredictedDurations), allVoiceLabels,
			// allDurationLabels, allVoicesCoDNotes, (allNetworkOutputs), (allNetworkOutputsAdapted)
			List<Integer> voice = Arrays.asList(new Integer[]{v});
			allPredictedVoices.add(voice); 
			allVoiceLabels.set(noteIndex, DataConverter.convertIntoVoiceLabel(voice));
			if (modelDuration) { 
				int d = -1; // TODO figure out duration now that there is no network output
				Rational dAsRat = new Rational(d, Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
				Rational[] dur = new Rational[]{dAsRat}; 
				allPredictedDurations.add(dur);
				allDurationLabels.set(noteIndex, 
					DataConverter.convertIntoDurationLabel(Arrays.asList(new Integer[]{d})));
				// The element in allVoicesCoDNotes is null by default and only needs to be
				// reset if the note at noteIndex is a CoD
				if (voice.size() > 1) {
					// See Transcription.handleCoDNotes()
					int voiceLonger = -1;
					int voiceShorter = -1;
					Integer[] VoicesCoDNotes = new Integer[]{voiceLonger, voiceShorter};
					allVoicesCoDNotes.set(noteIndex, VoicesCoDNotes);
				}
			}
		}
//		}
		// HIER EINDE1
	}


	// TODO commented out on 9-3-2016 - remove
	//	private boolean resolveConflictsBidirectionalOUD(int highestNumberOfVoices) {
	//		List<double[]> allNetworkOutputsTest = networkManager.getAllNetworkOutputs(); // Set in EncogNeuralNetworkManager.determinePredictedVoicesAndDurations()
	//		
	////		for (double[] d : allNetworkOutputsTest) {
	////			System.out.println(Arrays.toString(d));
	////		}
	////		System.exit(0);
	//		
	//		System.out.println("NEW PIECE");
	//		if (isTablatureCase) {
	//			List<List<TabSymbol>> tabChords = tablature.getTablatureChords(); // reverse if bwd
	//			// allPredictedVoices is in bwd order when modelling backwards; thus, so must tabChords be
	//			if (modelBackward) {
	//				Collections.reverse(tabChords);
	//			}
	//			List<List<Integer>> indicesPerChord = tablature.getIndicesPerChord(modelBackward); // reverse if bwd
	//			int numChords = tabChords.size();
	//			
	//			// 1. List the predicted and GT voices per chord
	//			List<List<List<Integer>>> voicesPerChord = new ArrayList<List<List<Integer>>>(); // reverse if bwd
	//			List<List<List<Integer>>> voicesPerChordGT = new ArrayList<List<List<Integer>>>(); // reverse if bwd
	//			int startIndex = 0;
	//			// For each chord
	//			for (int i = 0; i < numChords; i++) {
	//				List<List<Integer>> voicesCurrChord = new ArrayList<List<Integer>>();
	//				List<List<Integer>> voicesCurrChordGT = new ArrayList<List<Integer>>();
	//				int endIndex = startIndex + tabChords.get(i).size();
	//				// For each note in the chord at index i: add the voices predicted for it to voicesCurrChord
	//				// NB: works in both fwd and bwd mode because tabChords is in bwd order when modelling backwards
	//				for (int j = startIndex; j < endIndex; j++) {
	//					voicesCurrChord.add(allPredictedVoices.get(j));
	//					voicesCurrChordGT.add(dataConverter.convertIntoListOfVoices(groundTruthVoiceLabels.get(j)));
	//				}
	//				// When the chord is traversed: add voicesCurrChord to voicesPerChord
	//				voicesPerChord.add(voicesCurrChord);
	//				voicesPerChordGT.add(voicesCurrChordGT);
	//				startIndex = endIndex;	
	//			}
	//			
	////			for (int i = 0; i < predictedTranscription.getDurationLabels().size(); i++) {
	////				System.out.println(i + " " + dataConverter.convertIntoDuration(predictedTranscription.getDurationLabels().get(i))[0]);
	////			}
	////			System.exit(0);
	//			
	//			// 2. List the chords and all voices in them
	//			List<Integer> indicesOfIncorrChords = new ArrayList<Integer>();
	//			List<Integer> indicesOfChordsWithDoubles = new ArrayList<Integer>(); // reverse if bwd
	//			List<List<Integer>> allDoublesVoices = new ArrayList<List<Integer>>(); // reverse if bwd 
	//			List<Integer> indicesOfChordsWithSustained = new ArrayList<Integer>(); // reverse if bwd
	//			List<List<Integer>> allSustainedVoices = new ArrayList<List<Integer>>();
	//			// Make allDoublesVoices and allSustainedVoices the size of the number of chords
	//			for (int i = 0; i < numChords; i++) {
	//				allDoublesVoices.add(null);
	//				allSustainedVoices.add(null);
	//			}
	//			for (int i = 0; i < voicesPerChord.size(); i++) {
	//				// Make single list 
	//				List<List<Integer>> currChord = voicesPerChord.get(i);
	//				List<Integer> asOneList = new ArrayList<Integer>();
	//				for (List<Integer> n : currChord) {
	//					asOneList.addAll(n);
	//				}
	//				// If one of the voices occurs more than once 
	//				for (int v : asOneList) {
	//					if (Collections.frequency(asOneList, v) > 1) {
	//						indicesOfIncorrChords.add(i);
	//						indicesOfChordsWithDoubles.add(i);
	////						allDoublesVoices.add(asOneList);
	//						allDoublesVoices.set(i, asOneList);
	//						break;
	//					}
	//				}
	//				
	//				// If one of the voices is a sustained voice
	//				if (modelDuration) {
	//					List<Integer> sustainedVoices = new ArrayList<Integer>();
	//					List<Integer> noteIndicesCurrChord = indicesPerChord.get(i);
	//					// indexLowestNoteInChord must be in fwd order
	//					int indexLowestNoteInChord = noteIndicesCurrChord.get(0);
	//					if (modelBackward) {
	//						indexLowestNoteInChord = backwardsMapping.get(noteIndicesCurrChord.get(0));
	//					}
	//					// Get the indices (these are in fwd order) of the sustained notes
	//					List<Integer> indOfSusNotes = 
	//						featureGenerator.getIndicesOfSustainedPreviousNotes(basicTabSymbolProperties, 
	//						predictedTranscription.getDurationLabels(), null, indexLowestNoteInChord);
	//					// Get the sustained voices; use the voice information as predicted by the first pass model
	//					for (int ind : indOfSusNotes) {
	//						List<Double> vl = predictedTranscription.getVoiceLabels().get(ind);
	//						sustainedVoices.addAll(dataConverter.convertIntoListOfVoices(vl));
	//					}
	//					
	//					// If one of the voices is a sustained voice
	//					if (sustainedVoices.size() != 0) {
	//						for (int v : asOneList) {
	//							if (sustainedVoices.contains(v)) {
	//								if (!indicesOfIncorrChords.contains(i)) {
	//									indicesOfIncorrChords.add(i);
	//								}
	//								indicesOfChordsWithSustained.add(i);
	//								allSustainedVoices.set(i, sustainedVoices);
	//								break;
	//							}
	//						}		
	//					}
	//				}
	//				
	//			}
	//			
	//			System.out.println("indicesOfIncorrChords " + indicesOfIncorrChords);
	//			System.out.println("indicesOfIncorrChordsWithDoubles (" + indicesOfChordsWithDoubles.size() + ") " 
	//				+ indicesOfChordsWithDoubles);
	//			System.out.println("allDoublesVoices (" + allDoublesVoices.size() + ") " + allDoublesVoices);
	//			
	//			System.out.println("indicesOfIncorrChordsWithSustained (" + indicesOfChordsWithSustained.size() + ") " 
	//				+ indicesOfChordsWithSustained);
	//			System.out.println("allSustainedVoices (" + allSustainedVoices.size() + ") " + allSustainedVoices);
	//							
	//			// 3. Resolve conflicts
	//			// For each incorrect chord
	//			for (int i = 0; i < indicesOfIncorrChords.size(); i++) {
	//				
	//				int currentIndex = indicesOfIncorrChords.get(i);	
	//				List<List<Integer>> incorrChord = voicesPerChord.get(currentIndex); // size = numChords
	//				List<List<Integer>> corrChord = voicesPerChordGT.get(currentIndex); // size = numChords
	//				List<Integer> noteIndicesIncorrChord = indicesPerChord.get(currentIndex); // size = numChords
	//				System.out.println("  currentIndex =  " + currentIndex);
	//				System.out.println("  incorrChord =  " + incorrChord);
	//				System.out.println("  corrChord =    " + corrChord);
	//				System.out.println("  note indices = " + noteIndicesIncorrChord);
	//				
	//				
	//				// 1. Determine the voices for any sustained notes. The list remains empty when not modelling duration
	//				List<Integer> currentSustainedVoices = new ArrayList<Integer>();
	//				List<Integer> voicesAvailableWithSustained = new ArrayList<Integer>();
	//				List<Integer> indicesOfNoteWithSustainedConflict = new ArrayList<Integer>();
	//				if (modelDuration) {
	//					if (indicesOfChordsWithSustained.contains(currentIndex)) {
	//						System.out.println("  = SUSTAINED");
	//						currentSustainedVoices = allSustainedVoices.get(currentIndex);
	//	
	//						for (int j = 0; j < incorrChord.size(); j++) {
	//							for (int v : currentSustainedVoices) {
	//								if (incorrChord.get(j).contains(v)) {
	//									indicesOfNoteWithSustainedConflict.add(noteIndicesIncorrChord.get(j));
	//								}
	//							}
	//						}
	//					}
	//					// Determine the available voices
	//					for (int v = 0; v < highestNumberOfVoices; v++) {
	//						if (!currentSustainedVoices.contains(v)) {
	//							voicesAvailableWithSustained.add(v);
	//						}
	//					}			
	//				}
	//				System.out.println("  currentSustainedVoices = " + currentSustainedVoices);
	////				System.out.println("  indicesOfNoteWithSustainedConflict " + indicesOfNoteWithSustainedConflict);
	//				System.out.println("  voicesAvailableWithSustained = " + voicesAvailableWithSustained);
	//				
	////				// 2. Determine the voices for any double notes
	////				List<List<Integer>> incorrChord = voicesPerChord.get(currentIndex); // size = numChords
	////				List<List<Integer>> corrChord = voicesPerChordGT.get(currentIndex); // size = numChords
	////				List<Integer> noteIndicesIncorrChord = indicesPerChord.get(currentIndex); // size = numChords
	////				
	////				System.out.println("  incorrChord =  " + incorrChord);
	////				System.out.println("  corrChord =    " + corrChord);
	////				System.out.println("  note indices = " + noteIndicesIncorrChord);
	//				
	//				// 2. Determine the voices for any double notes
	//				List<Integer> currentDoubleVoices = new ArrayList<Integer>();
	//				List<Integer> voicesAvailableWithDouble = new ArrayList<Integer>();
	//				if (indicesOfChordsWithDoubles.contains(currentIndex)) {
	//					System.out.println("  = DOUBLES");
	//					List<Integer> allVoicesIncorrChord = allDoublesVoices.get(currentIndex); // size = numChords
	//					System.out.println("  all voices =   " + allVoicesIncorrChord);
	//					
	//					// 1. Determine which voice occurs more than once (the list starts with the lowest)
	//					for (int v = 0; v < highestNumberOfVoices; v++) {
	//						if (Collections.frequency(allVoicesIncorrChord, v) > 1) {
	//							currentDoubleVoices.add(v);
	//						}
	//					}
	//					Collections.reverse(currentDoubleVoices);
	//										
	//					// Determine the available voices
	//					for (int v = 0; v < highestNumberOfVoices; v++) {
	//						if (!allVoicesIncorrChord.contains(v)) {
	//							voicesAvailableWithDouble.add(v);
	//						}
	//					}				
	//				}
	//				System.out.println("  currentDoubleVoices = " + currentDoubleVoices);
	//				System.out.println("  voicesAvailableWithDouble = " + voicesAvailableWithDouble);
	//								
	//				// Determine voicesAvailable and voicesUnavailable
	//				List<Integer> voicesAvailable = new ArrayList<Integer>();
	//				List<Integer> voicesUnavailable = new ArrayList<Integer>();
	//				boolean containsSustained = indicesOfChordsWithSustained.contains(currentIndex);
	//				boolean containsDouble = indicesOfChordsWithDoubles.contains(currentIndex);
	//				// In the case of a sustained voice
	//				if (containsSustained && !containsDouble) {
	//					voicesAvailable = voicesAvailableWithSustained;
	//					voicesUnavailable = currentSustainedVoices;
	//				}
	//				// In the case of a double voice
	//				else if (!containsSustained && containsDouble) {
	//					voicesAvailable = voicesAvailableWithDouble;
	//					voicesUnavailable = currentDoubleVoices;
	//				}
	//				// In the case of both
	//				else if (containsSustained && containsDouble) {
	//					// voicesAvailable: add only elements that are in both lists
	//					for (int v : voicesAvailableWithSustained) {
	//						if(voicesAvailableWithDouble.contains(v)) {
	//							voicesAvailable.add(v);
	//						}
	//					}
	//					// voicesUnavailable
	//					voicesUnavailable.addAll(currentSustainedVoices);
	//					for (int v : currentDoubleVoices) {
	//						if (!voicesUnavailable.contains(v)) {
	//							voicesUnavailable.add(v);
	//						}
	//					}
	//				}
	//				Collections.reverse(voicesAvailable);
	//				System.out.println("  voicesAvailable = " + voicesAvailable);
	//				System.out.println("  voicesUnavailable = " + voicesUnavailable);
	//				
	//				// ASSUMPTION 1: There are never more than two available voices 
	//				if (voicesAvailable.size() > 2) {
	//					throw new RuntimeException("ERROR: There ar emore than two available voices in chord " + 
	//						currentIndex); 
	//				}
	//								
	//				// If the chord contains either a sustained note or a double note
	//				if (!(containsSustained && containsDouble)) {
	////					List<List<Integer>> allLists = new ArrayList<List<Integer>>();
	////					allLists.add(currentSustainedVoices);
	////					allLists.add(currentDoubleVoices);
	//					
	////					// 3. If listNum == 0: for each voice that is sustained
	////					//    If listNum == 1: for each voice that occurs more than once
	////					for (int v : voicesUnavailable) {
	////					for (int listNum = 0; listNum < allLists.size(); listNum++) {
	////						System.out.println("  LISTNUM = " + listNum);
	////						System.out.println("currentIndex = " + currentIndex);
	////						List<Integer> currentList = allLists.get(listNum);
	////						System.out.println("  currentList = " + currentList);
	////						for (int k = 0; k < currentList.size(); k++) {
	//					for (int v : voicesUnavailable) {
	////						int v = currentList.get(k);
	////						for (int v : voicesMoreThanOnce) {
	//						// 1. In both cases: first check for CoD conflicts. If one is found: resolve and return false 
	//						// For each note in the chord
	//						for (int j = 0; j < incorrChord.size(); j++) {
	//							List<Integer> currentVoices = incorrChord.get(j);
	//							int indexToReset = noteIndicesIncorrChord.get(j);
	//							// In case of a CoD
	//							if (currentVoices.size() == 2) {
	//								System.out.println("  IS COD");
	//								List<Integer> corrected = null;
	//								// Is v a 2nd predicted CoDnote? Remove
	//								if (currentVoices.get(1) == v) {
	//									corrected = Arrays.asList(new Integer[]{currentVoices.get(0)});
	//									allPredictedVoices.set(indexToReset, corrected);
	//								}
	//								// Is v a 1st predicted CoDnote? Change into the second predicted CoDnote
	//								else if (currentVoices.get(0) == v) {
	//									corrected = Arrays.asList(new Integer[]{currentVoices.get(1)});
	//									allPredictedVoices.set(indexToReset, corrected);
	//									
	//								}
	//								// If correction was made: add to conflictsRecordTest and return false
	//								if (corrected != null) {
	//									System.out.println("  indexToReset = " + indexToReset);
	//									conflictsRecordTest = conflictsRecordTest.concat("Voices at noteIndex = " +
	//										indexToReset + " (" + currentVoices + ") adapted to " +	corrected + "\r\n");
	//									return false;
	//								}
	//							}
	//						}
	//							
	//						System.out.println("  IS NOT COD");
	//							
	//						// 2. No CoD conflicts? Check for other conflicts 
	//						// a. In the case of a sustained voice: determine the index of the note that has the same voice 
	//						// as the sustained voice (after having checked for CoD conflicts as often as necessary, this
	//						// note can no longer be part of a CoD; therefore, .get(0) suffices)
	//						int indexOfNoteWithSustainedVoice = -1;
	//						if (containsSustained) {
	//							if (currentIndex == 70) {
	//								System.out.println(voicesUnavailable);
	////								System.exit(0);
	//							}
	//							for (int j = 0; j < incorrChord.size(); j++) {
	//								if (incorrChord.get(j).contains(v)) {
	////								if (incorrChord.get(j).get(0) == v) {
	//									indexOfNoteWithSustainedVoice = noteIndicesIncorrChord.get(j);
	//									// Break so that in the case of a chord that has doubles and a sustained voice,
	//									// the index of the first note with the sustained voice is taken
	//									// For example: sustained voice = 1, voices assigned to notes with indices [1, 2]
	//									// = both 1
	//									break;
	//								}
	//							}
	//	
	//							// If indexOfNoteWithSustainedVoice is still -1, the conflict is with the next note in 
	//							// voicesUnavailable
	//							if (indexOfNoteWithSustainedVoice == -1) {
	//								System.out.println("NU");
	////								listNum--;
	////								break;
	//								outer:for (int voice : voicesUnavailable) { // currentList) {
	//									for (int j = 0; j < incorrChord.size(); j++) {
	//										if (incorrChord.get(j).contains(voice)) {
	////										if (incorrChord.get(j).get(0) == voice) {
	//											indexOfNoteWithSustainedVoice = noteIndicesIncorrChord.get(j);
	//											break outer;
	//										}
	//									}
	//								}
	//							}
	//							System.out.println("  indexOfNoteWithSustainedVoice = " + indexOfNoteWithSustainedVoice);
	//						}
	//						// b. In the case of a voice occurring more than once: determine the indices of the notes that 
	//						// have the same voice
	//						List<Integer> indicesOfDoubles = new ArrayList<Integer>();
	//						if (containsDouble) {
	//							// Get the indices of the notes that have the same voice (after having checked for CoD 
	//							// conflicts as often as necessary, these notes can no longer be part of a CoD; therefore,
	//							// .get(0) suffices)
	////							List<Integer> indicesOfDoubles = new ArrayList<Integer>();
	//							for (int j = 0; j < incorrChord.size(); j++) {
	//								if (incorrChord.get(j).contains(v)) {
	////								if (incorrChord.get(j).get(0) == v) {
	//									indicesOfDoubles.add(noteIndicesIncorrChord.get(j));
	//								}
	//							}
	//							System.out.println("  indicesOfDoubles = " + indicesOfDoubles);
	//							
	//							// ASSUMPTION 2: No more than two notes are assigned to the voice occurring more than once
	//							if (indicesOfDoubles.size() > 2) {
	//								throw new RuntimeException("ERROR: More than two notes are assigned to the voice occuring " + 
	//									"more than once in chord " + currentIndex); //indexOfIncorrectChord);
	//							}
	//							// ASSUMPTION 3: The notes assigned to the voice occurring more than once are adjacent
	////							if (Math.abs(indicesOfDoubles.get(0) - indicesOfDoubles.get(1)) != 1) {
	////								throw new RuntimeException("ERROR: The notes assigned to the voice occuring more than once " + 
	////								"in chord " + indexOfIncorrectChord + " are not adjacent");
	////							}
	//						}
	//	
	//						// 3. In both cases: determine the closest available voice. Only if there are available voices
	//						int closestAvailableVoice = Integer.MAX_VALUE;
	//						if (voicesAvailable.size() != 0) {
	//							int noteIndex = -1;
	//							if (containsSustained) {
	//								noteIndex = indexOfNoteWithSustainedVoice; 
	//							}
	//							if (containsDouble) {
	//								noteIndex = -1; // TODO
	//							}
	//							double[] currentNetworkOutput = allNetworkOutputsTest.get(noteIndex);
	//							double highestOutput = -Double.MAX_VALUE;
	//							for (int av: voicesAvailable) {
	//								double outputForAV = currentNetworkOutput[av];
	//								if (outputForAV > highestOutput) {
	////									System.out.println("bggd = " + outputForAV);
	//									highestOutput = outputForAV; 
	//									closestAvailableVoice = av;
	//								}
	//							}
	//							System.out.println("  --> closestAvailableVoice = " + closestAvailableVoice);
	////							System.exit(0);
	//
	////							// Get for each available voice the distance to v
	////							List<Integer> distancesToV = new ArrayList<Integer>();
	////							for (int av : voicesAvailable) {
	////								distancesToV.add(Math.abs(av-v));
	////							}
	////							// Get the minimum of the distances
	////							int min = Collections.min(distancesToV);
	////							// Add all available voices that have the minimum distance to closestAvailableVoices
	////							List<Integer> closestAvailableVoices = new ArrayList<Integer>();
	////							for (int j = 0; j < distancesToV.size(); j++) {
	////								if (distancesToV.get(j) == min) {
	////									closestAvailableVoices.add(voicesAvailable.get(j));
	////								}
	////							}
	////											
	////							if (closestAvailableVoices.size() == 1) {
	////								closestAvailableVoice = closestAvailableVoices.get(0); 
	////							}
	////							else {
	////								closestAvailableVoice = closestAvailableVoices.get(0 ); // TODO
	////							}
	////							System.out.println("  closestAvailableVoices = " + closestAvailableVoices);
	//						}
	//							
	//						// 4. Determine indexToReset
	//						int indexToReset = -1;
	//						// a. In the case of a sustained note 
	//						if (containsSustained) {
	//							indexToReset = indexOfNoteWithSustainedVoice;
	//							System.out.println("  indexToReset = " + indexToReset);
	//						}
	//						// In the case of a voice occurring more than once
	//						if (containsDouble) {
	//							// a. If the closest available voice is lower (and thus is a higher int): replace the lower of 
	//							// indicesOfDoubles with closestAvailableVoice
	//							if (closestAvailableVoice > v) {
	//								indexToReset = indicesOfDoubles.get(0); 
	//							}
	//							// b. If the closest available voice is higher (and thus is a lower int): replace the higher of 
	//							// indicesOfDoubles with closestAvailableVoice
	//							else if (closestAvailableVoice < v) {
	//								indexToReset = indicesOfDoubles.get(1);
	//							}
	//							System.out.println("  indexToReset = " + indexToReset);
	//						}
	//						allPredictedVoices.set(indexToReset, Arrays.asList(new Integer[]{closestAvailableVoice}));
	//							
	//						conflictsRecordTest = conflictsRecordTest.concat("Voice at noteIndex = " +
	//							indexToReset + " (" + v + ") adapted to " +	closestAvailableVoice + "\r\n");
	//						
	//						// In the case of a sustained note: return false (the if is only read if voicesUnavailable is not
	//						// empty. i.e., if adaptations were made)
	////							if (listNum == 0) {
	//						System.out.println("TADAAAA");
	//						return false;
	////							}
	//					}
	////					}	
	//				}
	//				// If the chord contains both a sustained note and a double note
	//				else {
	//					// Determine the current voices in the chord					
	//					System.out.println("  BOTH");
	//					System.out.println("  incorrChord =  " + incorrChord);
	//					System.out.println("  corrChord =    " + corrChord);
	//					System.out.println("  note indices = " + noteIndicesIncorrChord);
	//					System.out.println("  voices available = " + voicesAvailable);
	//					System.out.println("  voices unavailable = " + voicesUnavailable);
	////					System.exit(0);
	//					
	//					for (int v : voicesUnavailable) {
	//						// 1. In both cases: first check for CoD conflicts. If one is found: resolve and return false 
	//						// For each note in the chord
	//						for (int j = 0; j < incorrChord.size(); j++) {
	//							List<Integer> currentVoices = incorrChord.get(j);
	//							int indexToReset = noteIndicesIncorrChord.get(j);
	//							// In case of a CoD
	//							if (currentVoices.size() == 2) {
	//								System.out.println("  IS COD");
	//								List<Integer> corrected = null;
	//								// Is v a 2nd predicted CoDnote? Remove
	//								if (currentVoices.get(1) == v) {
	//									corrected = Arrays.asList(new Integer[]{currentVoices.get(0)});
	//									allPredictedVoices.set(indexToReset, corrected);
	//								}
	//								// Is v a 1st predicted CoDnote? Change into the second predicted CoDnote
	//								else if (currentVoices.get(0) == v) {
	//									corrected = Arrays.asList(new Integer[]{currentVoices.get(1)});
	//									allPredictedVoices.set(indexToReset, corrected);
	//									
	//								}
	//								// If correction was made: add to conflictsRecordTest and return false
	//								if (corrected != null) {
	//									System.out.println("  indexToReset = " + indexToReset);
	//									conflictsRecordTest = conflictsRecordTest.concat("Voices at noteIndex = " +
	//										indexToReset + " (" + currentVoices + ") adapted to " +	corrected + "\r\n");
	//									return false;
	//								}
	//							}
	//						}
	//					}
	//					System.out.println("  IS NOT COD");
	//					
	//					// 2. No CoD conflicts? Check for other conflicts
	//					int indexLower = noteIndicesIncorrChord.get(0);
	//					int indexHigher = noteIndicesIncorrChord.get(1);
	//					double[] outputLower = allNetworkOutputsTest.get(indexLower);
	//					double[] outputHigher = allNetworkOutputsTest.get(indexHigher);
	//										
	//					// (1) There will always be two (but no more: assumption 2) notes assigned to the double voice
	//					// (2) There will be either one or two (but no more: assumption 1) voices available
	//					// Thus:
	//					// If only one voice is available
	//					if (voicesAvailable.size() == 1) {
	//						int indexToReset = -1;
	//						List<Integer> corrected = Arrays.asList(new Integer[]{voicesAvailable.get(0)});
	//						// Determine which note has the higher output for this voice
	//						// If it is the lower note
	//						if (outputLower[voicesAvailable.get(0)] >= outputHigher[voicesAvailable.get(0)]) {
	//							indexToReset = indexLower;
	//							allPredictedVoices.set(indexLower, corrected);
	//						}
	//						// If it is the higher note
	//						else {
	//							indexToReset = indexHigher;
	//							allPredictedVoices.set(indexHigher, corrected);
	//						}
	//						conflictsRecordTest = conflictsRecordTest.concat("Voices at noteIndex = " +
	//							indexToReset + " (" + "SOMETHING" + ") adapted to " + corrected + "\r\n");
	//						return false;
	//						
	//					}
	//					// If two voices are available
	//					else if (voicesAvailable.size() == 2) {
	////						int indexToReset1 = -1;
	////						int indexToReset2 = -1;
	////						List<Integer> corrected1 = new ArrayList<Integer>();
	////						List<Integer> corrected2 = new ArrayList<Integer>();
	//						
	//						List<Integer> lowerVoice = Arrays.asList(new Integer[]{voicesAvailable.get(0)});
	//						List<Integer> higherVoice = Arrays.asList(new Integer[]{voicesAvailable.get(1)});
	////						// Determine which note has the higher output for the lower voice
	////						// If it is the lower note: set the lower note to the lower voice and the higher to the higher
	////						if (outputLower[voicesAvailable.get(0)] >= outputHigher[voicesAvailable.get(0)]) {
	////							allPredictedVoices.set(indexLower, lowerVoice);
	////							allPredictedVoices.set(indexHigher, higherVoice);
	////							
	////							conflictsRecordTest = conflictsRecordTest.concat("Voices at noteIndex = " +
	////								indexLower + " (" + "SOMETHING" + ") adapted to " + lowerVoice + "\r\n");
	////							conflictsRecordTest = conflictsRecordTest.concat("Voices at noteIndex = " +
	////								indexHigher + " (" + "SOMETHING" + ") adapted to " + higherVoice + "\r\n");
	////							return false;
	////						}
	////						// If it is the higher voice: do the opposite
	////						else {
	////							allPredictedVoices.set(indexLower, higherVoice);
	////							allPredictedVoices.set(indexHigher, lowerVoice);
	////							
	////							conflictsRecordTest = conflictsRecordTest.concat("Voices at noteIndex = " +
	////								indexLower + " (" + "SOMETHING" + ") adapted to " + lowerVoice + "\r\n");
	////							conflictsRecordTest = conflictsRecordTest.concat("Voices at noteIndex = " +
	////								indexHigher + " (" + "SOMETHING" + ") adapted to " + higherVoice + "\r\n");
	////							return false;
	////						}
	//						
	//						// Determine which note has the higher output for the lower voice
	//						double outputLoNoteForLoVoice = outputLower[voicesAvailable.get(0)];
	//						double outputHiNoteForLoVoice = outputHigher[voicesAvailable.get(0)];
	//						int noteWithHighestOutputForLoVoice = -1;
	//						if (outputLoNoteForLoVoice > outputHiNoteForLoVoice) {
	//							noteWithHighestOutputForLoVoice = indexLower;
	//						}
	//						else {
	//							noteWithHighestOutputForLoVoice = indexHigher;
	//						}
	//						// Determine which note has the higher output for the higher voice
	//						double outputLoNoteForHiVoice = outputLower[voicesAvailable.get(1)];
	//						double outputHiNoteForHiVoice = outputHigher[voicesAvailable.get(1)];
	//						int noteWithHighestOutputForHiVoice = -1;
	//						if (outputLoNoteForHiVoice > outputHiNoteForHiVoice) {
	//							noteWithHighestOutputForHiVoice = indexLower;
	//						}
	//						else {
	//							noteWithHighestOutputForHiVoice = indexHigher;
	//						}
	//						
	////						System.out.println("GVD");
	////						System.out.println(noteWithHighestOutputForLoVoice);
	////						System.out.println(noteWithHighestOutputForHiVoice);
	//						
	//						// Not the same: done
	//						if (noteWithHighestOutputForLoVoice != noteWithHighestOutputForHiVoice) {
	//							allPredictedVoices.set(noteWithHighestOutputForLoVoice, lowerVoice);
	//							allPredictedVoices.set(noteWithHighestOutputForHiVoice, higherVoice);
	//							
	//							conflictsRecordTest = conflictsRecordTest.concat("Voices at noteIndex = " +
	//								noteWithHighestOutputForLoVoice + " (" + "SOMETHING" + ") adapted to " + lowerVoice + "\r\n");
	//							conflictsRecordTest = conflictsRecordTest.concat("Voices at noteIndex = " +
	//								noteWithHighestOutputForHiVoice + " (" + "SOMETHING" + ") adapted to " + higherVoice + "\r\n");
	//							return false;
	//						}
	////						System.out.println(highestForLoVoice);
	////						System.out.println(lowerVoice);
	////						System.out.println(highestForHiVoice);
	////						System.out.println(higherVoice);					
	////						System.exit(0);
	//						
	//						// If the same: determine for which voice the difference is the largest, and assign
	//						// the note that has the highest value for this voice to it
	//						else {
	//							double diffLoVoice = Math.abs(outputLoNoteForLoVoice - outputHiNoteForLoVoice);
	//							double diffHiVoice = Math.abs(outputLoNoteForHiVoice - outputHiNoteForHiVoice);
	//							// If the difference is larger for the lower voice than for the higher: set the note
	//							// that has the higher value for the lower voice to this voice
	//							if (diffLoVoice > diffHiVoice) {
	//								if (outputLoNoteForLoVoice > outputHiNoteForLoVoice) {
	//									allPredictedVoices.set(indexLower, lowerVoice);
	//									allPredictedVoices.set(indexHigher, higherVoice);
	//									
	//									conflictsRecordTest = conflictsRecordTest.concat("Voices at noteIndex = " +
	//										indexLower + " (" + "SOMETHING" + ") adapted to " + lowerVoice + "\r\n");
	//									conflictsRecordTest = conflictsRecordTest.concat("Voices at noteIndex = " +
	//										indexHigher + " (" + "SOMETHING" + ") adapted to " + higherVoice + "\r\n");
	//									return false;
	//								}
	//								else if (outputHiNoteForLoVoice > outputLoNoteForLoVoice) {
	//									allPredictedVoices.set(indexHigher, lowerVoice);
	//									allPredictedVoices.set(indexLower, higherVoice);
	//									
	//									conflictsRecordTest = conflictsRecordTest.concat("Voices at noteIndex = " +
	//										indexHigher + " (" + "SOMETHING" + ") adapted to " + lowerVoice + "\r\n");
	//									conflictsRecordTest = conflictsRecordTest.concat("Voices at noteIndex = " +
	//										indexLower + " (" + "SOMETHING" + ") adapted to " + higherVoice + "\r\n");
	//									return false;
	//								}
	//							}
	//							// If the difference is larger for the higher voice than for the lower: set the note
	//							// that has the higher value for the higher voice to this voice
	//							else if (diffHiVoice > diffLoVoice) {
	//								if (outputLoNoteForHiVoice > outputHiNoteForHiVoice) {
	//									allPredictedVoices.set(indexLower, higherVoice);
	//									allPredictedVoices.set(indexHigher, lowerVoice);
	//									
	//									conflictsRecordTest = conflictsRecordTest.concat("Voices at noteIndex = " +
	//										indexLower + " (" + "SOMETHING" + ") adapted to " + higherVoice + "\r\n");
	//									conflictsRecordTest = conflictsRecordTest.concat("Voices at noteIndex = " +
	//										indexHigher + " (" + "SOMETHING" + ") adapted to " + lowerVoice + "\r\n");
	//									return false;
	//								
	//								}
	//								else if (outputHiNoteForHiVoice > outputLoNoteForHiVoice) {
	//									allPredictedVoices.set(indexHigher, higherVoice);
	//									allPredictedVoices.set(indexLower, lowerVoice);	
	//									
	//									conflictsRecordTest = conflictsRecordTest.concat("Voices at noteIndex = " +
	//										indexHigher + " (" + "SOMETHING" + ") adapted to " + higherVoice + "\r\n");
	//									conflictsRecordTest = conflictsRecordTest.concat("Voices at noteIndex = " +
	//										indexLower + " (" + "SOMETHING" + ") adapted to " + lowerVoice + "\r\n");
	//									return false;
	//								}
	//							}
	//							
	////							System.out.println(diffLoVoice);
	////							System.out.println(diffHiVoice);
	////							System.exit(0);
	//							
	//						}
	//							
	//					}					
	//				}
	//				
	//				
	//			}
	//			
	//			System.out.println(conflictsRecordTest);
	//		}
	//		else {
	//			basicNoteProperties = null;
	//		}		
	//		return true;
	//	}
	
	
	/**
	 * Tests the network in application mode, where the features are generated using the voice assignments 
	 * created incrementally by the network. Returns the assignment errors. 
	 *
	 * @param trainingSettingsAndParameters
	 * @param minAndMaxFeatureValues
	 * @param useTablatureInformation
	 * @return
	 */
	private List<List<List<Integer>>> testInApplicationMode(int fold, double[][] minAndMaxFeatureValues, 
		List<Double> modelWeighting, String[] argPaths/*, String pieceName 
		int numTestExamples*/) {

		List<List<List<Integer>>> testResults = new ArrayList<List<List<Integer>>>();
		
		Map<String, Double> modelParameters = Runner.getModelParams();
		ModellingApproach ma = 
			Runner.ALL_MODELLING_APPROACHES[modelParameters.get(Runner.MODELLING_APPROACH).intValue()];
//		List<Integer> sliceIndices =
//			ToolBox.decodeListOfIntegers(modelParameters.get(Runner.SLICE_IND_ENC_SINGLE_DIGIT).intValue(), 1);	
//		List<Integer> ns = 
//			ToolBox.decodeListOfIntegers(modelParameters.get(Runner.NS_ENC_SINGLE_DIGIT).intValue(), 1);

		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()]; 
		ModelType mt = m.getModelType();
		DecisionContext dc = m.getDecisionContext(); 
		boolean modelDuration = m.getModelDuration();

//		DatasetID di = 
//			Dataset.ALL_DATASET_IDS[modelParameters.get(Dataset.DATASET_ID).intValue()];
		boolean isTablatureCase = Runner.getDataset().isTablatureSet();
		ProcessingMode pm = 
			Runner.ALL_PROC_MODES[modelParameters.get(Runner.PROC_MODE).intValue()];
		boolean modelDurationAgain = 
			ToolBox.toBoolean(modelParameters.get(Runner.MODEL_DURATION_AGAIN).intValue());
//		boolean giveFirst = 
//			ToolBox.toBoolean(modelParameters.get(Runner.GIVE_FIRST).intValue());
		int highestNumVoicesTraining = Runner.getHighestNumVoicesTraining();
//			modelParameters.get(Runner.HIGHEST_NUM_VOICES).intValue();
		boolean useCV = ToolBox.toBoolean(modelParameters.get(Runner.CROSS_VAL).intValue());
		boolean applToNewData = 
			ToolBox.toBoolean(modelParameters.get(Runner.APPL_TO_NEW_DATA).intValue());
		boolean estimateEntries = 
			ToolBox.toBoolean(modelParameters.get(Runner.ESTIMATE_ENTRIES).intValue());
		List<Integer> sliceIndices = null;
		if (mt == ModelType.MM || mt == ModelType.ENS) {
			sliceIndices = 
				ToolBox.decodeListOfIntegers(modelParameters.get(Runner.SLICE_IND_ENC_SINGLE_DIGIT).intValue(), 1);
		}
		List<Integer> ns = null;
		if (mt == ModelType.ENS) {		
			ns = ToolBox.decodeListOfIntegers(modelParameters.get(Runner.NS_ENC_SINGLE_DIGIT).intValue(), 1);
		}

		Dataset dataset = Runner.getDataset();
		int pieceIndex;
		if (useCV) {
			pieceIndex = dataset.getNumPieces() - fold;
		}
		else {
			pieceIndex = fold;
		}
		int numTestExamples = dataset.getIndividualPieceSizes(ma).get(pieceIndex);
		
		// Set paths
		String pathNN = null;
		String pathMM = null;
		String pathComb = null;
		if (mt == ModelType.NN || mt == ModelType.DNN || mt == ModelType.OTHER) {
			pathNN = argPaths[0];
		}
//		else if (onlyMM) {
//			pathMM = fullPaths[0];
//		}
		else if (mt == ModelType.ENS) {		
			pathComb = argPaths[0];
			pathNN = argPaths[1];
			pathMM = argPaths[2];
		}
		if (!useCV && applToNewData) {
			pathComb = argPaths[0];
			pathNN = argPaths[1];
		}
			
		// Create newTranscription
//		System.out.println("... creating empty new transcription with the given number of voices (" + 
// 			highestNumVoicesTraining + ") and the given key and time signature ...");
	//	TimeSignature timeSignature = 
		long[][] ts = groundTruthTranscription.getPiece().getMetricalTimeLine().getTimeSignature(); // TODO
		MetricalTimeLine mtl = groundTruthTranscription.getPiece().getMetricalTimeLine();
//		TimeSignature timeSignature = new TimeSignature((int)ts[0][0], (int) ts[0][1]);
		KeyMarker keyMarker = new KeyMarker();
		keyMarker.setMode(Mode.MODE_MINOR); // TODO
		keyMarker.setAlterationNum(-1); // TODO
//		newTranscription = makeEmptyTranscription(timeSignature, keyMarker, highestNumVoicesTraining);
		newTranscription = 
			makeEmptyTranscription(groundTruthTranscription.getPiece().getMetricalTimeLine(),
			highestNumVoicesTraining);

		// Initialise lists
		allPredictedVoices = new ArrayList<List<Integer>>();
		allVoiceLabels = new ArrayList<List<Double>>();
		if (ma == ModellingApproach.N2N) {
			allNetworkOutputs = new ArrayList<double[]>(); 
			noteFeatures = new ArrayList<List<Double>>();
			indicesOfNotesAddedToOccupiedVoice = new ArrayList<Integer>();
			if (dc == DecisionContext.UNIDIR) {	
				if (modelDuration) {
					allDurationLabels = new ArrayList<List<Double>>();
					allVoicesCoDNotes = new ArrayList<Integer[]>();
					allPredictedDurations = new ArrayList<Rational[]>();
				}
				// Pre-set lists with dummy values 
				for (int i = 0 ; i < numTestExamples; i++) {
					allVoiceLabels.add(null);
					// NB: Do not fill allPredictedVoices and allPredictedDurations, as these are 
					// filled by adding their elements at the end of resolveConflicts() 
					if (modelDuration) {
						allDurationLabels.add(null);
						allVoicesCoDNotes.add(null);
//						allPredictedDurations.add(null);
					}
				}
				conflictsRecord = "";
				conflictIndices = new ArrayList<List<Integer>>();
				conflictIndices.add(new ArrayList<Integer>());
				conflictIndices.add(new ArrayList<Integer>());
				allNetworkOutputsAdapted = new ArrayList<double[]>();
				allNotes = new ArrayList<List<Note>>();
			}
			if (dc == DecisionContext.BIDIR) {
				allVoiceLabels = predictedTranscription.getVoiceLabels();
				if (modelDuration) {
					// When modelling duration again: initialise allPredictedDurations, which is filled by adding 
					// the elements at the end of resolveConflicts() (the same goes for allPredictedVoices, which 
					// has already been initialised on creation)
					if (modelDurationAgain) {
						allPredictedDurations = new ArrayList<Rational[]>();
					}
					// Set allDurationLabels, allVoicesCoDNotes, and allVoiceLabels (which are used in the
					// feature vector calculation) to the predicted values. These predicted values are replaced
					// one by one during the application process (in resolveConflicts())
					allDurationLabels = predictedTranscription.getDurationLabels();
					allVoicesCoDNotes = predictedTranscription.getVoicesCoDNotes();	
				}
			}
			if (mt == ModelType.ENS) {
				allCombinedOutputs = new ArrayList<double[]>();	
//				int n = modelParameters.get(Runner.N).intValue();
				allMelodyPredictors = new ArrayList<MelodyPredictor>();
				for (int i : sliceIndices) {
					String model = 
						Runner.melodyModel + "-" + MelodyPredictor.getSliceIndexString(i);
					for (int nInd = 0; nInd < ns.size(); nInd++) {
						int n = ns.get(nInd);
						MelodyPredictor mp = 
							new MelodyPredictor(MelodyPredictor.getMelModelType(), 
							MelodyPredictor.getTermType(), n, i);
						String currEnd = 
							"/n=" + n + "/" + "fold_" +
							ToolBox.zerofy(fold, ToolBox.maxLen(fold)) + "/";
						pathMM = pathMM.concat(currEnd);	
						mp.loadModel(new File(pathMM + model + ".ser"));
						pathMM = pathMM.substring(0, pathMM.indexOf(currEnd));
						allMelodyPredictors.add(mp);
					}
				}
			}
		}
		else if (ma == ModellingApproach.C2C) {
			chordFeatures = new ArrayList<List<List<Double>>>();
			allHighestNetworkOutputs = new ArrayList<Double>();
			allBestVoiceAssignments = new ArrayList<List<Integer>>();
			allPossibleVoiceAssignmentsAllChords = new ArrayList<List<List<Integer>>>(); 
		}

		// 1. Assign the notes to voices
		// a. N2N
		if (ma == ModellingApproach.N2N) {  
			if (mt == ModelType.DNN || mt == ModelType.OTHER) {
				boolean isScikit = false;
				// For scikit
				if (isScikit) {
					String[] s = new String[]{pathNN, m.name(), Runner.fvExt + "app.csv"};
				}
				// For TensorFlow
//				int param = modelParameters.get(Runner.NUM_HIDDEN_LAYERS).intValue();
				// TODO fix retrieving number of features
				String paramsAndHyperparams = 
					"hidden layers=" + modelParameters.get(Runner.NUM_HIDDEN_LAYERS).intValue() + "," +
					"input layer size=" + minAndMaxFeatureValues[0].length + "," +
					"hidden layer size=" + modelParameters.get(Runner.HIDDEN_LAYER_SIZE).intValue() + "," +
					"output layer size=" + Transcription.MAXIMUM_NUMBER_OF_VOICES;
//					"learning rate=" + modelParameters.get(Runner.LEARNING_RATE) + "," +
//					"keep probability=" + modelParameters.get(Runner.KEEP_PROB) + "," +
//					"epochs=" + modelParameters.get(Runner.EPOCHS).intValue();
				String[] s = new String[]{pathNN, m.name(), paramsAndHyperparams};
				System.out.println(Arrays.toString(s));	
				PythonInterface.init(s);
			}
//			allNetworkOutputs = new ArrayList<double[]>();
			
			List<List<Integer>> voiceEntryInfo = null;
			if (estimateEntries) {
//				voiceEntryInfo =
//					groundTruthTranscription.getImitativeVoiceEntries(highestNumVoicesTraining, 3);
				voiceEntryInfo =
					groundTruthTranscription.determineVoiceEntriesHIGHLEVEL(basicNoteProperties, 
					highestNumVoicesTraining, 3);	
			}
//			System.out.println(voiceEntryInfo);
			// 1. Traverse the piece note for note, and do for each note
			for (int i = 0; i < numTestExamples; i++) {
				// TODO in ENS model this loop gets slower as i increases
//				for (int noteIndex = 0; noteIndex < numberOfTestExamples; noteIndex++) {
				int noteIndex = i;
				if (pm == ProcessingMode.BWD) {
					noteIndex = backwardsMapping.get(i);
				}

//				System.out.println("noteIndexBwd = " + noteIndex);
//				System.out.println(allVoiceLabels);

				predictVoices(pathNN, minAndMaxFeatureValues, noteIndex, voiceEntryInfo, modelWeighting);
				// Add the note to the predicted voices
				addNote(noteIndex);
				// Set the adapted voice labels to the Transcription 
				newTranscription.setVoiceLabels(allVoiceLabels);
			}
			if (mt == ModelType.DNN || mt == ModelType.OTHER) {
				// notes in fold processed; exit Python process
			}

			// 2. Set class variables for EncogNeuralNetworkManager
//			networkManager.setAllNetworkOutputs(allNetworkOutputs); 6 maart
//			networkManager.createTrainingExamplesNoteToNote(noteFeatures, groundTruthLabels);
//			networkManager.setNoteFeatures(noteFeatures); // 1 feb
//			networkManager.setGroundTruthVoiceLabels(groundTruthVoiceLabels, null);
//			networkManager.setGroundTruthDurationLabels(groundTruthDurationLabels);
//			// If the bwd model is used, groundTruthVoicesCoDNotes must still be set in bwd order 
//			// (see also testInTestMode()))
//			if (!isBidirectional && modelBackward && modelDuration ||
//				isBidirectional && modelBackward && modelDuration && modelDurationAgain) {
//				List<Integer[]> groundTruthVoicesCoDNotesBwd = new ArrayList<Integer[]>();
//				for (int i : backwardsMapping) {
//					groundTruthVoicesCoDNotesBwd.add(groundTruthVoicesCoDNotes.get(i));
//				}
//				groundTruthVoicesCoDNotes = groundTruthVoicesCoDNotesBwd;
//			}
//			networkManager.setVoicesCoDNotes(groundTruthVoicesCoDNotes);
//			networkManager.setEqualDurationUnisonsInfo(equalDurationUnisonsInfo);
//			// chordSizes still must be reversed
//			if (modelBackward) {
//				Collections.reverse(chordSizes);
//			}
//			networkManager.setChordSizes(chordSizes);
//			networkManager.setAllNetworkOutputs(allNetworkOutputs);
		}

		// b. C2C
//		List<Double> allHighestNetworkOutputs = new ArrayList<Double>();
//		List<List<Integer>> allBestVoiceAssignments = new ArrayList<List<Integer>>();
		if (ma == ModellingApproach.C2C) {           	 	
//			allHighestNetworkOutputs = new ArrayList<Double>(); // is not longer empty after testing in test mode and must thus be reset
//			allBestVoiceAssignments = new ArrayList<List<Integer>>(); // is not longer empty after testing in test mode and must thus be reset
			// 1. Traverse the piece chord for chord (from left to right), and do for each chord
			int lowestNoteIndex = 0;
//			List<List<List<Integer>>> allPossibleVoiceAssignmentsEntirePiece = new ArrayList<List<List<Integer>>>(); 6 maart 
//			allPossibleVoiceAssignmentsAllChords = new ArrayList<List<List<Integer>>>(); 
			for (int chordIndex = 0; chordIndex < numTestExamples; chordIndex++) {       	
				
//				System.out.println("... predicting the voice(s) for chord no. " + chordIndex + " ...");
				
				// 1. Determine the size of the chord
				int sizeOfCurrentChord = 0;
				// a. In the tablature case
				if (isTablatureCase) {
					sizeOfCurrentChord = basicTabSymbolProperties[lowestNoteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
				}
				// b. In the non-tablature case
				else {
					sizeOfCurrentChord = basicNoteProperties[lowestNoteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
				}

				// 2. Determine all possible voice assignments for the chord and add them to allPossibleVoiceAssignmentsEntirePiece 
				List<List<Integer>> currentAllPossibleVoiceAssignments = 
					FeatureGeneratorChord.enumerateVoiceAssignmentPossibilitiesForChord(basicTabSymbolProperties, 
					basicNoteProperties, allVoiceLabels, lowestNoteIndex, highestNumVoicesTraining);
//				allPossibleVoiceAssignmentsEntirePiece.add(currentAllPossibleVoiceAssignments); 6 maart 
				allPossibleVoiceAssignmentsAllChords.add(currentAllPossibleVoiceAssignments);

				// 3. Determine the set of feature vectors for the chord and add it to chordFeatures. Also evaluate each
				// individual chord feature vector and add the result, the network output, to currentNetworkOutputs
				List<List<Double>> currentChordFeatureVectors = new ArrayList<List<Double>>();
				List<Double> currentNetworkOutputs = new ArrayList<Double>();
				// For each voice assignment
				for (int i = 0; i < currentAllPossibleVoiceAssignments.size(); i++) {
					List<Integer> currentVoiceAssignment = currentAllPossibleVoiceAssignments.get(i);
					// 1. Generate the feature vector, scale it, and add to currentChordFeatureVectors
//					List<Double> currentChordFeatureVector = 
//						featureGenerator.generateCompleteChordFeatureVectorOUD(basicTabSymbolProperties, basicNoteProperties,
//						newTranscription, lowestNoteIndex, largestChordSizeTraining, highestNumberOfVoicesTraining,
//						currentVoiceAssignment, useTablatureInformation);

//					List<Double> currentChordFeatureVector = 
//						featureGeneratorChord.generateChordFeatureVector(basicTabSymbolProperties, basicNoteProperties,
//						newTranscription, meterInfo, lowestNoteIndex, currentVoiceAssignment);
					List<Double> currentChordFeatureVector = 
						FeatureGeneratorChord.generateChordFeatureVectorDISS(basicTabSymbolProperties, basicNoteProperties,
						newTranscription, meterInfo, lowestNoteIndex, currentVoiceAssignment);

//					FeatureGenerator.scaleFeatureVector(currentChordFeatureVector, minAndMaxFeatureValues, learningApproach);
					currentChordFeatureVector = FeatureGenerator.scaleFeatureVector(currentChordFeatureVector, 
						minAndMaxFeatureValues, ma);
					currentChordFeatureVectors.add(currentChordFeatureVector); 

					// 2. Evaluate currentChordFeatureVector and add to currentNetworkOutputs
					double[] currentNetworkOutput = networkManager.evalNetwork(currentChordFeatureVector);
					currentNetworkOutputs.add(currentNetworkOutput[0]);
				}
				chordFeatures.add(currentChordFeatureVectors); 

				// 4. Determine the best voice assignment and convert it into a List of voices, as well as the highest network
				// output; add bestVoiceAssignment and highestNetworkOutput to allBestVoiceAssignments and allHighestNetworkOutputs  
				// NB: The best voice assignment is the one that is rated highest by the network, i.e., the one that gives 
				// the highest network output. Because it is possible that different voice assignments result in the same 
				// network output, the highest output may occur multiple times. If this is the case, less likely candidates
				// must be filtered out; this happens inside determineBestVoiceAssignment()
				List<Integer> bestVoiceAssignment = 
					OutputEvaluator.determineBestVoiceAssignment(currentNetworkOutputs, currentAllPossibleVoiceAssignments);
				double highestNetworkOutput = Collections.max(currentNetworkOutputs);

				boolean giveFirst = false; // TODO remove
				if (giveFirst) {
					if (pm == ProcessingMode.FWD && chordIndex == 0) {
						List<List<Double>> groundTruthChordVoiceLabels = 
//							groundTruthTranscription.getChordVoiceLabels(tablature).get(0); 
							groundTruthTranscription.getChordVoiceLabels().get(0);
						List<Integer> groundTruthVoiceAssignment = 
							DataConverter.getVoiceAssignment(groundTruthChordVoiceLabels, Transcription.MAXIMUM_NUMBER_OF_VOICES); 
						if (!groundTruthVoiceAssignment.equals(bestVoiceAssignment)) {
							bestVoiceAssignment = groundTruthVoiceAssignment;
							highestNetworkOutput = 1.0;
						}
					}
				}

				List<List<Double>> predictedChordVoiceLabels = DataConverter.getChordVoiceLabels(bestVoiceAssignment); 
				List<List<Integer>> predictedChordVoices = DataConverter.getVoicesInChord(predictedChordVoiceLabels); 
//				double highestNetworkOutput = Collections.max(currentNetworkOutputs);
				allHighestNetworkOutputs.add(highestNetworkOutput);
				allBestVoiceAssignments.add(bestVoiceAssignment);

				// 5. Assign the notes in the chord to the predicted voices
				for (int j = 0; j < sizeOfCurrentChord; j++) {
					int currentNoteIndex = lowestNoteIndex + j;
					// Get the current note
					Note currentNote = null;
					// a. In the tablature case
					if (isTablatureCase) {
						currentNote = Tablature.convertTabSymbolToNote(basicTabSymbolProperties, currentNoteIndex); 
					}
					// b. In the non-tablature case
					else {
						currentNote = groundTruthTranscription.getNoteSequence().getNoteAt(currentNoteIndex);
					}

					// Add currentNote to each voice that is predicted for it
					List<Integer> currentPredictedVoices = predictedChordVoices.get(j);
					Rational currentMetricTime = currentNote.getMetricTime();
					for (int k = 0; k < currentPredictedVoices.size(); k++) {
						int currentPredictedVoice = currentPredictedVoices.get(k); 
						newTranscription.addNote(currentNote, currentPredictedVoice, currentMetricTime);
						// Add information to applicationProcess  
						String message = 
							"Note at noteIndex " + currentNoteIndex + " (chordIndex " + chordIndex +
							") added to voice " + currentPredictedVoice +
							" at metricTime " + currentMetricTime + "\r\n";					
//						System.out.println(message);
						applicationProcess = applicationProcess.concat(message);
					}

					// Wrap currentNote in a List and add it to allNotes; also add the predicted voices and the predicted
					// voice labels to allPredictedVoices and predictedChordVoiceLabels
					List<Note> currentNotes = new ArrayList<Note>();
					currentNotes.add(currentNote);
					// In the tablature case: if a CoD is predicted for this note, create the CoDNote and add it to
					// currentNotes before adding the latter to allNotes
					if (isTablatureCase) {
						if (currentPredictedVoices.size() > 1) { // TODO Or just == 2?
							Note currentCoDNote = Tablature.convertTabSymbolToNote(basicTabSymbolProperties, currentNoteIndex);
							currentNotes.add(currentCoDNote);
						}
					}
					allPredictedVoices.add(currentPredictedVoices);
				}
				allVoiceLabels.addAll(predictedChordVoiceLabels);

				// 6. Store the adapted voice labels with the Transcription 
				newTranscription.setVoiceLabels(allVoiceLabels);
					
				// 7. Increment lowestNoteIndex for the next iteration of the for-loop
				lowestNoteIndex += sizeOfCurrentChord; // currentChord.size();
			}
	
			// 2. Set class variables in EncogNeuralNetworkManager
//			networkManager.createTrainingExamplesChordToChord(chordFeatures, groundTruthChordVoiceLabels);
//			networkManager.setGroundTruthChordVoiceLabels(groundTruthChordVoiceLabels);
//			networkManager.setGroundTruthVoiceLabels(null, groundTruthChordVoiceLabels);

//			networkManager.setPossibleVoiceAssignmentsAllChords(allPossibleVoiceAssignmentsEntirePiece); 6 maart
//			networkManager.setPossibleVoiceAssignmentsAllChords(allPossibleVoiceAssignmentsAllChords); 6 maart
//			networkManager.setAllHighestNetworkOutputs(allHighestNetworkOutputs); 6 maart
//			networkManager.setAllBestVoiceAssignments(allBestVoiceAssignments); 6 maart
//			networkManager.setEqualDurationUnisonsInfo(equalDurationUnisonsInfo);
		}

		// 2. Add durations (all notes still have their minimum duration as given in the tablature)
//		if (!useFullDuration) {
		if (isTablatureCase && !modelDuration) {
//			addDurations(highestNumberOfVoicesTraining); // TODO restore?
		}
		// Colour the Notes assigned wrongly TODO
//		ErrorCalculatorTab.colourNotes(allNotes);		
	  
//		// NB: When using the bwd model, allDurationLabels is in fwd order and must thus be reordered
//		List<List<Double>> allDurationLabelsApplication = new ArrayList<List<Double>>();
//		if (modelBackward && modelDuration) {
//			for (int i : backwardsMapping) {
//				allDurationLabelsApplication.add(allDurationLabels.get(i));
//		  	}
//		}
			
		// 3. Store
		// Store new Transcription (its voice labels (allVoiceLabels) and duration labels 
		// (allDurationLabels) are always in fwd order)
		
//		newTranscription.setPieceName(pieceName);
		
//		ToolBox.storeObjectBinary(newTranscription, 
//		new File(parent + "/" + Runner.output + ToolBox.zerofy(fold, ToolBox.maxLen(fold)) +
//		"-" + pieceName + ".ser"));
		
//		String parent = new File(pathNN).getParent();
//		String dir;
//		if (useCV) {
//			dir = new File(pathNN).getParent() + "/" + Runner.output +
//				ToolBox.zerofy(fold, ToolBox.maxLen(fold)) + "-"; 
//		}
//		else {
//			if (applToNewData) {
//				dir = pathComb + Runner.output;
//			}
//			else {
//				dir = pathNN + Runner.output;
//			}
//		}
		
//		// Create a full-fledged Transcription
//		String pieceName = dataset.getPieceNames().get(pieceIndex);
//		File encodingFile = null;
//		if (isTablatureCase) {
//			encodingFile = dataset.getAllEncodingFiles().get(pieceIndex);
//		}
//		File midiFile = dataset.getAllMidiFiles().get(pieceIndex);
////		Piece piece = 
////			Transcription.createPiece(basicTabSymbolProperties, basicNoteProperties, 
////			allVoiceLabels, allDurationLabels, highestNumVoicesTraining);
//		Transcription predictedTranscr = 
//			new Transcription(midiFile, encodingFile,  basicTabSymbolProperties, 
//			basicNoteProperties, highestNumVoicesTraining, allVoiceLabels, allDurationLabels);
//		
////		ToolBox.storeObjectBinary(newTranscription, new File(dir + pieceName + ".ser"));
//		ToolBox.storeObjectBinary(predictedTranscr, new File(dir + pieceName + ".ser"));
	
		// Do not store outputs in the real world application case
		if (!(!useCV && applToNewData) && mt == ModelType.NN) {
			File f = new File(pathNN + Runner.outputs + ".ser"); 
			if (ma == ModellingApproach.N2N) {
				List<List<double[]>> outps = 
					ToolBox.getStoredObjectBinary(new ArrayList<List<double[]>>(), f);
				outps.set(Runner.APPL, new ArrayList<double[]>(allNetworkOutputs));
				if (Runner.storeNetworkOutputs) {
					ToolBox.storeObjectBinary(outps, f);
				}
			}
			else if (ma == ModellingApproach.C2C) {
				List<List<Double>> outps = 
					ToolBox.getStoredObjectBinary(new ArrayList<List<Double>>(), f);
				outps.set(Runner.APPL, new ArrayList<Double>(allHighestNetworkOutputs));
				if (Runner.storeNetworkOutputs) {
					ToolBox.storeObjectBinary(outps, f);
				}
			}
		}

//		if (modelDuration) {
//			if (dc == DecisionContext.UNIDIR || dc == DecisionContext.BIDIR && modelDurationAgain) {
//				ToolBox.storeObjectBinary(allDurationLabels, 
//					new File(fullPaths[0] +	Runner.genDurLabels + pieceName + ".ser"));
//			}
//		}
	
		// Evaluate and store MM outputs 
		if (mt == ModelType.ENS) {
			for (int i = 0; i < sliceIndices.size(); i++) {
//				allMelodyPredictors.get(i).resetSTM();
				String model = Runner.melodyModel + "-" +
					MelodyPredictor.getSliceIndexString(sliceIndices.get(i)) + "-";
				for (int nInd = 0; nInd < ns.size(); nInd++) { // new
					int index = i*ns.size() + nInd; // new
//					List<double[]> outputs = allMelodyModelOutputsPerModel.get(i);
					List<double[]> outputs = allMelodyModelOutputsPerModel.get(index); // new
					double[][] evalNum = EvaluationManager.getMetricsSingleFoldMM(outputs, 
						groundTruthVoiceLabels);
					String evalString =	EvaluationManager.getPerformanceSingleFoldMM(evalNum, 
						outputs, groundTruthVoiceLabels);
//					model = model.concat("_n=" + n);
					String subPathComb = pathComb.concat("n=" + ns.get(nInd) + "/");
					ToolBox.storeObjectBinary(evalNum, 
						new File(subPathComb + model + Runner.evalMM + "-" + Runner.application + ".ser")); // new: subPathComb was pathComb
					ToolBox.storeTextFile(evalString, 	
						new File(subPathComb + model + Runner.evalMM + "-" + Runner.application + ".txt")); // new: subPathComb was pathComb
					ToolBox.storeObjectBinary(outputs, 
						new File(subPathComb + model + Runner.outputMM + "-" + Runner.application +".ser")); // new: subPathComb was pathComb
				}
			}
			// Reset STM
			for (int i = 0; i < sliceIndices.size()*ns.size(); i++) { // new
				allMelodyPredictors.get(i).resetSTM(); // TODO necessary? Doesn't this happen automatically because new MMs are loaded in the next fold (next call to this method) anyway?
			}
		}
		
		// 4. Determine and return the assignment errors
		// When using bwd model:
		// allPredictedVoices = bwd
		// groundTruthVoiceLabels = bwd
		// allPredictedDurations = bwd
		// groundTruthDurationLabels = bwd
		// equalDurationUnisonsInfo = bwd
//		List<List<Double>> gtvl = new ArrayList<List<Double>>();
//		gtvl.add(groundTruthVoiceLabels.get(0));
//		List<Integer[]> edui = new ArrayList<Integer[]>();
//		edui.add(equalDurationUnisonsInfo.get(0));
//		List<List<Integer>> assignmentErrors = // HOERO
//			ErrorCalculator.calculateAssignmentErrors(allPredictedVoices, gtvl, 
//			allPredictedDurations, groundTruthDurationLabels, edui);

		List<List<Integer>> assignmentErrors =
			ErrorCalculator.calculateAssignmentErrors(allPredictedVoices, groundTruthVoiceLabels, 
			allPredictedDurations, groundTruthDurationLabels, equalDurationUnisonsInfo);
//		assignmentErrors = errorCalculator.calculateAssignmentErrors(allPredictedVoices, groundTruthVoiceLabels, 
//		  equalDurationUnisonsInfo);
			
		testResults.add(assignmentErrors);
		testResults.add(allPredictedVoices);

//		return assignmentErrors;
		return testResults;
	}


	private String getConflictText(String type, int noteIndex, String metPos, int indexInChord, 
		int noteIndexPrevious, String metPosPrevious, int indexInChordPrevious, 
		List<Integer> predictedVoices, List<Integer> predictedVoicesPrevious,
		boolean sustained, boolean interrupted) {

		String voiceNum = "first";
		if (type.equals("(ii)")) {
			voiceNum = "second";
		}

		String sustainedText = "";
		if (sustained) {
			sustainedText = "sustained ";
		}
//		// Special case for interrupting next notes
//		if (sustained == false && type.equals("(i)")) {
////		sustainedText = "next ";
//			sustainedText = "";
//		}
		if (interrupted) {
			sustainedText = "interrupting ";
		}

		String conflictText = 
			"noteIndex = " + noteIndex + "\r\n" +
			"  type " + type + " conflict between note " + noteIndex + " (bar: " + metPos + ", index in chord: " + indexInChord +
			") and note " + noteIndexPrevious	+ " (bar: " + metPosPrevious + ", index in chord: " + indexInChordPrevious + "):\r\n";	
		if (type.equals("(i)") || type.equals("(ii)")) { // || type.equals("(vi)")) {
			conflictText = conflictText.concat( 
				"  the first predicted voice for note " + noteIndex + " (predicted voices: " + predictedVoices + 
				") is the same as the " + voiceNum + " predicted voice for " + sustainedText + "note " + noteIndexPrevious 
				+	" (predicted voices: " + predictedVoicesPrevious + ") \r\n"
			);
		}
		else if (type.equals("(iii)")) {
			conflictText = conflictText.concat( 
				"  the second predicted voice for note " + noteIndex + " (predicted voices: " + predictedVoices + 
				") is the same as (one of) the predicted voice(s) for " + sustainedText + "note " + noteIndexPrevious +
				" (predicted voices: " + predictedVoicesPrevious + ") \r\n"
			);
		}

		return conflictText;
	}





	/**
	 * Gets the maximum duration for a note at the given metric time with the given predicted voices. The maximum 
	 * duration is determined by
	 *   1. If there is one predicted voice: the onset time of the closest next note in that voice;
	 *   2. If there are two predicted voices: the closest of the two onset times of the closest next note in the 
	 *    predicted voices. 
	 * If none of the predicted voices contains a next note, i.e., if the note is the last note in all predicted voices,
	 * or if there are no notes yet in the predicted voices, <code>null</code> is returned.
	 * 
	 * NB: This method is called only in the tablature case, and only in the case of 
	 *     (1) type (i) and (ii) conflicts with sustained notes, in which case metricTimeNext will be the onset time
	 *         of the note that gives a duration conflict with the note at metricTime;
	 *     (2) type (iv) conflicts, in which case metricTimeNext will be <code>null</code>.
	 * 
	 * @param metricTime
	 * @param metricTimeNext
	 * @param transcription
	 * @param predictedVoices
	 * @return
	 */
	// TESTED For type (iv) conflicts only
	static Rational getMaximumDuration(Rational metricTime, Rational metricTimeNext, Transcription transcription, 
		List<Integer> predictedVoices) {
		Rational maxDur = null;

		// For each predicted voice: determine the onset time of the next note in that voice
		List<Rational> metricTimesNext = new ArrayList<Rational>();
		for (int i : predictedVoices) {
			NotationVoice nv = transcription.getPiece().getScore().get(i).get(0);
			// a. For type (i) or (ii) conflicts, which only occur when using the fwd model, and where the predicted voices
			// will contain at least one note (the one at metricTime) and nv.size() is thus never null
			if (metricTimeNext != null) {
				// If metricTime is the onset time of the note that has been added last to voice i: add metricTimeNext
				if (nv.get(nv.size() - 1).getMetricTime().equals(metricTime)) {
					metricTimesNext.add(metricTimeNext);
				}
				// If not: add the metric time of the closest next note in voice i
				else {
					for (NotationChord nc : nv) { 
						if (nc.getMetricTime().isGreater(metricTime)) {
							metricTimesNext.add(nc.getMetricTime());
							break;
						}
					}
				}
				// TODO: Or, because the else can never happen (?), just: 
				// metricTimesNext.add(metricTimeNext);
			}
			// b. For type (iv) conflicts, which only occur when using the bwd model, and where the predicted voices may 
			// not contain a note yet
			else {
				// If no note has been added to voice i yet: add null (i.e., there is no next note)
				if (nv.size() == 0) {
					metricTimesNext.add(null);
				}
				// If notes have been added to voice i
				else {
					// If the note at metricTime is the last note in the voice: add null (i.e., there is no next note)
//					Rational lastMetricTimeInVoice = nv.get(nv.size() - 1).get(0).getMetricTime();
					if (metricTime.equals(nv.get(nv.size() - 1).get(0).getMetricTime())) {
						metricTimesNext.add(null);
					}
					// If not: add the metric time of the closest next note
					else {
						for (NotationChord nc : nv) { 
							if (nc.getMetricTime().isGreater(metricTime)) {
								metricTimesNext.add(nc.getMetricTime());
								break;
							}
						}
					}
				}
			}
		}

		// 2. Determine the closest metric time
		Rational closestMetricTimeNext = null;	
		// a. If there is only one predicted voice
		if (metricTimesNext.size() == 1 && metricTimesNext.get(0) != null) {
			closestMetricTimeNext = metricTimesNext.get(0);
		}
		// b. If there are two predicted voices
		else if (metricTimesNext.size() == 2) {
			// If the note at noteIndex is the last note in neither of the predicted voices
			if (!metricTimesNext.contains(null)) {
				closestMetricTimeNext = Collections.min(metricTimesNext);
			}
			// If the note at noteIndex is the last note in one or in both of the predicted voices
			else {
				// In both
				if (metricTimesNext.get(0) == null && metricTimesNext.get(1) == null) {
					closestMetricTimeNext = null;
				}
				// In one
				else {
					int indexOfNull = metricTimesNext.indexOf(null);
					closestMetricTimeNext = metricTimesNext.get(Math.abs(indexOfNull - 1));
				}
			}
		}

		// 3. Determine the maximum duration
		if (closestMetricTimeNext != null) {
			maxDur = closestMetricTimeNext.sub(metricTime); 
		}
		return maxDur;
	}


	/**
	 * Resolves any conflicts between the predicted voice(s) (or duration) for the note at noteIndex 
	 * and the predicted voices (or duration) for any preceding notes. These are conflicts between the
	 * note at noteIndex and
	 * <ul>
	 * 		<li>any lower notes in the same chord (in the tablature case when not modelling duration,
	 * 			fwd and bwd model)</li>
	 *    	<li>any sustained previous notes and any lower notes in the same chord (in the tablature 
	 *      	case when modelling duration and in the non-tablature case, fwd model)</li>
	 * 		<li>any interrupting next notes and any lower notes in the same chord (in the tablature 
	 *      	case when modelling duration and in the non-tablature case, bwd model).</li>
	 * </ul>
	 * 
	 * There are four types of conflicts:
	 * <ul>
	 * 		<li>type (i)<br>
	 *  		the first predicted voice for the note at noteIndex is the same as the first predicted
	 * 			voice for a sustained, interrupting, or lower chord note</li>
	 * 		<li>type (ii) (tablature case only)<br> 
	 * 			the first predicted voice for the note at noteIndex is the same as the second predicted
	 * 			voice for a sustained, interrupting, or lower chord note</li>
	 * 		<li>type (iii) (tablature case case only)<br> 
	 * 			the second predicted voice for the note at noteIndex is the same as the first or second
	 * 			predicted voice for a sustained, interrupting, or lower chord note </li>
	 * 		<li>type (iv) (tablature case using the fwd model and modelling duration only)<br> 
	 * 			the note at noteIndex is the last note in the chord, and in any of the voices there are
	 * 			notes x whose offset time exceeds the onset time of the notes y in the next chord such
	 * 			that x + y > the number of voices in the piece.</li>                       
	 * </ul>
	 * 
	 * The conflicts are resolved in the following sequence:
	 * <ol>
	 * 		<li>type (i) conflicts with any sustained previous (fwd)/interrupting next (bwd) notes<br></li> 
	 * 		<li>type (ii) conflicts with any sustained previous (fwd)/interrupting next (bwd) notes</li>
	 * 		<li>type (i) conflicts with any lower chord notes; if found: resolve and restart from 1.</li>
	 * 		<li>type (ii) conflicts with any lower chord notes</li>
	 * 		<li>type (iii) conflicts with any sustained previous (fwd)/interrupting next (bwd) notes 
	 * 			and any lower chord notes (in one go)</li> 
	 * 		<li>type (iv) conflicts.</li>
	 * </ol>    
	 *  
	 * This method does the following:
	 * <ul>	
	 * 		<li>determines the initially predicted voice(s) and duration label</li>
	 * 		<li>checks for conflicts, and, for every conflict encountered</li>
	 * 		<ul> 
	 *     		<li>adds the index of the note to which the adaptations apply to conflictIndices</li>
	 *     		<li>makes the following adaptations:</li>
	 *     		<ul> 
	 *        		<li>in case of a type (i) or (iii) conflict<br>
	 *        			adapts the network output and resets the corresponding element in 
	 *        			allNetworkOutputsAdapted; then redetermines	the predicted voice(s) or duration</li> 
	 *        		<li>in case of a type (ii) conflict<br>
	 *        			removes the Note from newTranscription, redetermines the predicted voice(s), 
	 *        			and resets the corresponding element in allPredictedVoices, allVoiceLabels, 
	 *        			and allVoicesCoDNotes</li>
	 *        		<li>in case of a type (iv) conflict<br>
	 *        			redetermines the predicted durations of the notes to their maximum duration, 
	 *        			and, for all Notes that are not the note at noteIndex, adapts them in 
	 *        			newTranscription and resets the corresponding elements in allDurationLabels, 
	 *        			allPredictedDurations, and allNotes</li>
	 *        	</ul>
	 * 		</ul>       
	 * 		In the tablature case when modelling duration, in the case of type (i) and (ii) conflicts 
	 * 		with sustained notes (which occur only when using the fwd model), instead of the above, 
	 * 		the duration of the sustained note is adapted to its maximum duration in newTranscription,
	 * 		and the corresponding element in allPredictedDurations,	allDurationLabels, and allNotes is
	 * 		reset.
	 * 		<li>adds to</li>
	 * 		<ul>
	 *     		<li>allPredictedVoices: the (redetermined) predicted voice(s)</li>
	 *     		<li>allVoiceLabels: the voice label that goes with the (redetermined) predicted voice(s)</li>
	 *     		<li>allVoicesCoDNotes (tablature case when modelling duration): in the case of a CoD, the 
	 *     			(redetermined) predicted voices for the CoDnote; in the case of no CoD,	<code>null</code></li>
	 *     		<li>allDurationLabels: the (redetermined) predicted duration label</li>
	 * 		</ul>
	 * </ul>
	 *      
	 * @param copyOfNetworkOutputCurrentNote
	 * @param noteIndex 
	 */
	// TESTED for both tablature- (non-dur and dur) and non-tablature case; for both fwd and bwd model
	void resolveConflicts(double[] copyOfNetworkOutputCurrentNote, int noteIndex) {
		Map<String, Double> modelParameters = Runner.getModelParams();
		boolean isTablatureCase = Runner.getDataset().isTablatureSet();		
		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()]; 
		DecisionContext dc = m.getDecisionContext(); 
		boolean modelDuration = m.getModelDuration();

		ProcessingMode pm = 
			Runner.ALL_PROC_MODES[modelParameters.get(Runner.PROC_MODE).intValue()];
//		Implementation ipm = 
//			Runner.ALL_IMPLEMENTATIONS[modelParameters.get(Runner.IMPLEMENTATION).intValue()];		
		boolean modelDurationAgain = 
			ToolBox.toBoolean(modelParameters.get(Runner.MODEL_DURATION_AGAIN).intValue());
//		boolean giveFirst = 
//			ToolBox.toBoolean(modelParameters.get(Runner.GIVE_FIRST).intValue());
		int highestNumVoicesTraining = Runner.getHighestNumVoicesTraining();
//			modelParameters.get(Runner.HIGHEST_NUM_VOICES).intValue();
		
		boolean allowCoD = ToolBox.toBoolean(modelParameters.get(Runner.SNU).intValue());
		double deviationThreshold = -1-0;
		if (Runner.getDataset().isTablatureSet()) {
			deviationThreshold = modelParameters.get(Runner.DEV_THRESHOLD);
		}
		
		// Determine noteIndexBwd, which is needed when using the bwd model for the lists in bwd order. When using the 
		// fwd model, it will always be the same as noteIndex
		int noteIndexBwd = noteIndex;
		if (pm == ProcessingMode.BWD) {
			for (int i = 0; i < backwardsMapping.size(); i++) {
				if (backwardsMapping.get(i) == noteIndex) {
					noteIndexBwd = i;
					break;
				}
			}
		}

		// 1. Get the predicted voices, and, if applicable, the predicted duration and durationLabel
		List<Integer> predictedVoices = 
			OutputEvaluator.interpretNetworkOutput(copyOfNetworkOutputCurrentNote, 
			allowCoD, deviationThreshold).get(0);
		List<Integer> originalPredictedVoices = new ArrayList<Integer>(predictedVoices);
		
		int firstPredictedVoice = predictedVoices.get(0);
		List<Double> predictedDurationLabel = null;
		Rational predictedDuration = null;
		if (isTablatureCase /*&& im == Implementation.PHD*/ && modelDuration) {
			List<Integer> predictedDurationCurrentNote = null;
			if (dc == DecisionContext.UNIDIR || dc == DecisionContext.BIDIR && modelDurationAgain) {
				predictedDurationCurrentNote = 
					OutputEvaluator.interpretNetworkOutput(copyOfNetworkOutputCurrentNote,
					allowCoD, deviationThreshold).get(1);
				predictedDurationLabel = DataConverter.convertIntoDurationLabel(predictedDurationCurrentNote);
			}
			if (dc == DecisionContext.BIDIR && !modelDurationAgain) {
				predictedDurationLabel = allDurationLabels.get(noteIndex); 
			}
//			predictedDurationLabel = dataConverter.convertIntoDurationLabel(predictedDurationCurrentNote);
			predictedDuration = DataConverter.convertIntoDuration(predictedDurationLabel)[0];
			predictedDuration.reduce();
		}

		// 2. If only one Note per voice is allowed at a given metric time: resolve any conflicts
		boolean allowNonMonophonic = false;
		if (allowNonMonophonic == false) {
			// 0. Get the pitch, the metric position, the index in the chord, and the metric time of the note at noteIndex
//			String metPos = ToolBox.getMetricPositionAsString(allMetricPositions.get(noteIndex));
			int indexInChord = -1;
			Rational metricTime = null;
			Rational offsetTime = null;

			// a. In the tablature case
			if (isTablatureCase) { 
				indexInChord = basicTabSymbolProperties[noteIndex][Tablature.NOTE_SEQ_NUM];
				metricTime = new Rational(basicTabSymbolProperties[noteIndex][Tablature.ONSET_TIME], 
					Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
				offsetTime = metricTime.add(predictedDuration);
			}
			// b. In the non-tablature case
			else {
				indexInChord = basicNoteProperties[noteIndex][Transcription.NOTE_SEQ_NUM];
				metricTime = new Rational(basicNoteProperties[noteIndex][Transcription.ONSET_TIME_NUMER],
			  		basicNoteProperties[noteIndex][Transcription.ONSET_TIME_DENOM]);
				Rational metricDuration = new Rational(basicNoteProperties[noteIndex][Transcription.DUR_NUMER],
					basicNoteProperties[noteIndex][Transcription.DUR_DENOM]);
				offsetTime = metricTime.add(metricDuration);
			}	
			int lowestNoteIndex = noteIndex - indexInChord;

			// 1. List the indices of any sustained previous/interrupting next notes and any
			// lower chord notes and add the lists to listsOfIndices
			// NB1: indicesRelatedToSustainedNotes contains 
			//      (a) in the tablature case when using the fwd model and when modelling duration, and in the non-tablature 
			//      case when using the fwd model: the indices of any sustained previous notes
			//      (b) in the tablature case when using the bwd model and when modelling duration, and in the non-tablature
			//      case when using the bwd model: the indices of any interrupting next notes
			List<List<Integer>> listsOfIndices = new ArrayList<List<Integer>>();
			// a. Sustained previous notes exists only 
			//    (1) in the tablature case when modelling duration and using the fwd model
			//    (2) in the non-tablature case when using the fwd model
			List<Integer> indicesRelatedToSustainedNotes = new ArrayList<Integer>();
//			if (!isBidirectional && isTablatureCase && modelDuration && !modelBackward || 
//				!isBidirectional && !isTablatureCase && !modelBackward ||
//				 isBidirectional && isTablatureCase && modelDuration || 
//				 isBidirectional && !isTablatureCase) {
			if ((isTablatureCase && modelDuration && pm == ProcessingMode.FWD) || 
				!isTablatureCase && pm == ProcessingMode.FWD) {
				for (int i = 0; i < lowestNoteIndex; i ++) {
					// The previous note may be a CoD; therefore, previousNote must be a List
					// NB: In the non-tablature case, where there are no CoDs, previousNote will always contain only one element
					List<Note> previousNote = allNotes.get(i);
					Rational metricTimePrevious = previousNote.get(0).getMetricTime(); 
					Rational durationPrevious = Rational.ZERO;
					for (Note n : previousNote) {
						if (n.getMetricDuration().isGreater(durationPrevious)) {
							durationPrevious = n.getMetricDuration();
						}
					}
					Rational offsetTimePrevious = metricTimePrevious.add(durationPrevious);
					if (offsetTimePrevious.isGreater(metricTime)) {
						indicesRelatedToSustainedNotes.add(i);
					}
				}
			}
//			listsOfIndices.add(indicesRelatedToSustainedNotes);

			// b. Interrupting next notes exist only 
			//    (1) in the tablature case when using the bwd model and when modelling duration
			//    (2) in the non-tablature case when using the bwd model
//			if (!isBidirectional && isTablatureCase && modelDuration && modelBackward || 
//				!isBidirectional && !isTablatureCase && modelBackward ||
//				isBidirectional && isTablatureCase && modelDuration ||
//				isBidirectional && !isTablatureCase) {
			if ((isTablatureCase && modelDuration && pm == ProcessingMode.BWD) || 
				!isTablatureCase && pm == ProcessingMode.BWD) { // JOA
				int numNotes = 0;			
//				int numNotes = basicNoteProperties.length;
				if (isTablatureCase) {
					numNotes = basicTabSymbolProperties.length;
				}
				else {
					numNotes = basicNoteProperties.length;
				}

				// For all notes after the note at noteIndex
				for (int i = noteIndex + 1; i < numNotes; i++) {
					// If the next note is an interrupting next note, i.e., if its onset > metricTime and < offsetTime: 
					// add i to indicesRelatedToSustainedNotes
					Rational onsetNext = null; 
					if (isTablatureCase) {
						onsetNext = new Rational(basicTabSymbolProperties[i][Tablature.ONSET_TIME],
							Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
					}
					else {
						onsetNext = new Rational(basicNoteProperties[i][Transcription.ONSET_TIME_NUMER],
							basicNoteProperties[i][Transcription.ONSET_TIME_DENOM]);
					}
//					Rational onsetNext = new Rational(basicNoteProperties[i][Transcription.ONSET_TIME_NUMER],
//						basicNoteProperties[i][Transcription.ONSET_TIME_DENOM]);	
					if (onsetNext.isGreater(metricTime) && onsetNext.isLess(offsetTime)) {
						indicesRelatedToSustainedNotes.add(i);
					}
					// Break if the next note falls at or after the offset of the current note
					if (onsetNext.isGreaterOrEqual(offsetTime)) {
						break;
					}
				}

				// indicesRelatedToSustainedNotes now contains ALL notes falling within the 
				// duration of the note at noteIndex. Make a new list, containing only indices
				// for notes with a voice that has not been listed yet 
				List<Integer> voices = new ArrayList<Integer>();
				List<Integer> newList = new ArrayList<Integer>();
				for (int i = 0; i < indicesRelatedToSustainedNotes.size(); i++) {
					int noteIndexPrevious = indicesRelatedToSustainedNotes.get(i);
//					if (!isBidirectional) {
						for (int j = 0; j < backwardsMapping.size(); j++) {
							if (backwardsMapping.get(j) == noteIndexPrevious) {
								int noteIndexPreviousBwd = j;
								int firstPredictedVoicePrevious = allPredictedVoices.get(noteIndexPreviousBwd).get(0); 
								if (!voices.contains(firstPredictedVoicePrevious)) {
									voices.add(firstPredictedVoicePrevious);
									newList.add(noteIndexPrevious);
								}
								break;
							}
						}
//					}
//					else { // NIEUW
//						int firstPredictedVoicePrevious = allPredictedVoices.get(noteIndexPrevious).get(0);
//						if (!voices.contains(firstPredictedVoicePrevious)) {
//							voices.add(firstPredictedVoicePrevious);
//							newList.add(noteIndexPrevious);
//						}
//						break;
//					}
				}
				// Reset indicesRelatedToSustainedNotes
				indicesRelatedToSustainedNotes = newList;
			}			
			listsOfIndices.add(indicesRelatedToSustainedNotes);
			
			// c. Lower chord notes
			List<Integer> indicesOfLowerChordNotes = new ArrayList<Integer>(); 
			for (int i = lowestNoteIndex; i < noteIndex; i++) {
				indicesOfLowerChordNotes.add(i);
			}	
			listsOfIndices.add(indicesOfLowerChordNotes);

			// 2. Resolve any type (i) and (ii) conflicts. Type (i) conflicts apply to both the 
			// tablature and the non-tablature case; type (ii) conflicts to the tablature case only
			for (int listNum = 0; listNum < listsOfIndices.size(); listNum++) {
				// listsOfIndices will always have two elements (which may be empty); if listNum == 0, currentListOfIndices
				// will be indicesRelatedToSustainedNotes; if listNum == 1, it will be indicesOfLowerChordNotes
				List<Integer> currentListOfIndices = listsOfIndices.get(listNum);

		      	// 1. Resolve any type (i) conflicts (conflicts between the first predicted voice for
				// the note at noteIndex and the first predicted voices for any previous sustained/
				// interrupting next notes or any lower chord notes) 
				boolean checkSustainedNotesAgain = false;
				boolean skipLowerNotes = false;
				List<Integer> typeFiveList = new ArrayList<Integer>();
				for (int i = 0; i < currentListOfIndices.size(); i++) {
					int noteIndexPrevious = currentListOfIndices.get(i);

					// Determine noteIndexPreviousBwd, which is needed when using the bwd model for
					// the lists in bwd order. When using the fwd model, it will always be the same
					// as noteIndexPrevious
					int noteIndexPreviousBwd = noteIndexPrevious;
					if (pm == ProcessingMode.BWD) {
						for (int j = 0; j < backwardsMapping.size(); j++) {
							if (backwardsMapping.get(j) == noteIndexPrevious) {
								noteIndexPreviousBwd = j;
								break;
							}
						}
					}
					List<Integer> predictedVoicesPrevious = allPredictedVoices.get(noteIndexPreviousBwd);
					int firstPredictedVoicePrevious = predictedVoicesPrevious.get(0);

					// If firstPredictedVoicePrevious is the same as firstPredictedVoice 
					//dndeze1
					if (firstPredictedVoicePrevious == firstPredictedVoice) {
						// 0. Get the metric position and the index in the chord of the previous note
						String metPosPrevious = ToolBox.getMetricPositionAsString(allMetricPositions.get(noteIndexPrevious)); 
						int indexInChordPrevious = -1;
						if (isTablatureCase) {
							indexInChordPrevious = basicTabSymbolProperties[noteIndexPrevious][Tablature.NOTE_SEQ_NUM]; 
						}
						else {
							indexInChordPrevious = basicNoteProperties[noteIndexPrevious][Transcription.NOTE_SEQ_NUM]; 
						}	

						// a. For sustained previous notes in the tablature case (which only exist when modelling duration 
						// and using the fwd model)
//						if (!isBidirectional && listNum == 0 && isTablatureCase && modelDuration && !modelBackward ||
//							 isBidirectional && listNum == 0 && isTablatureCase && modelDuration) {
						if (listNum == 0 && isTablatureCase && modelDuration && pm == ProcessingMode.FWD) { // JOA
							boolean sustained = true;
							boolean interrupted = false; 
							List<Double> predictedDurationLabelPrevious = allDurationLabels.get(noteIndexPrevious);
							
							Rational predictedDurationPrevious = null; // allPredictedDurations.get(noteIndexPreviousBwd)[0];
							if (dc == DecisionContext.UNIDIR || dc == DecisionContext.BIDIR && modelDurationAgain) {
								predictedDurationPrevious = allPredictedDurations.get(noteIndexPreviousBwd)[0];
							}
							else if (dc == DecisionContext.BIDIR && !modelDurationAgain) {
								predictedDurationPrevious = 
									DataConverter.convertIntoDuration(predictedDurationLabelPrevious)[0]; 
							}
							predictedDurationPrevious.reduce();
							
							// In the unidir case always; in the bidir case only when modelling duration again
							if (dc == DecisionContext.UNIDIR || dc == DecisionContext.BIDIR && modelDurationAgain) { // NIEUW
								// 1. Add index to conflictIndices
								if (!conflictIndices.get(1).contains(noteIndexPreviousBwd)) {
									conflictIndices.get(1).add(noteIndexPreviousBwd);
								}  	  	  	
	
								// 2. Reset the Note's duration in Transcription
								// Determine the maximum duration and the pitch of the note at noteIndexPrevious. Because this note 
								// is followed by at least one note (the note at noteIndex), maxDurPrevious can never be null
								Rational metricTimePrevious = new Rational(basicTabSymbolProperties[noteIndexPrevious][Tablature.ONSET_TIME],	
									Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
								Rational maxDurPrevious = 
									getMaximumDuration(metricTimePrevious, metricTime, newTranscription, predictedVoicesPrevious);
	
								int pitchPrevious = basicTabSymbolProperties[noteIndexPrevious][Tablature.PITCH];
								// Reset the note at noteIndexPrevious in the Transcription. The for-loop is needed for when the note 
								// at noteIndexPrevious is a CoD, in which case the duration of its second predicted voice must be
								// adapted as well 
	
								ScoreNote adaptedScoreNotePrevious = 
									new ScoreNote(new ScorePitch(pitchPrevious), metricTimePrevious, maxDurPrevious);
								for (int predictedVoicePrevious : predictedVoicesPrevious) {
									NotationVoice nv = 
										newTranscription.getPiece().getScore().get(predictedVoicePrevious).get(0);
									for (NotationChord nc : nv) {
										if (nc.getMetricTime().equals(metricTimePrevious)) {
											nc.get(0).setScoreNote(adaptedScoreNotePrevious);
										}
									}
								}
	
								// 3. Record conflict and add to conflictsRecord
//								conflictsRecord = conflictsRecord.concat(getConflictText("(i)", noteIndexBwd, 
//									metPos, indexInChord, noteIndexPreviousBwd, metPosPrevious, 
//									indexInChordPrevious, predictedVoices, predictedVoicesPrevious,
//									sustained, interrupted));
//								conflictsRecord = conflictsRecord.concat("  --> predicted duration " + predictedDurationPrevious + 
//									" for note " + noteIndexPreviousBwd + " adapted; ");

								// 4. Redetermine predictedDurationLabelPrevious and reset the corresponding elements in the Lists
								int maxDurAsInt = 
								  	maxDurPrevious.getNumer() *	(Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom() / maxDurPrevious.getDenom());
								predictedDurationLabelPrevious = Transcription.createDurationLabel(maxDurAsInt);
								allDurationLabels.set(noteIndexPrevious, predictedDurationLabelPrevious);
								predictedDurationPrevious = maxDurPrevious;
								predictedDurationPrevious.reduce();
//								if (!isBidirectional || isBidirectional && modelDurationAgain) {
								allPredictedDurations.set(noteIndexPreviousBwd, new Rational[]{predictedDurationPrevious});
//								}
								List<Note> previousNote = allNotes.get(noteIndexPreviousBwd);
								for (Note n : previousNote) {
									n.setScoreNote(adaptedScoreNotePrevious);
								}
								allNotes.set(noteIndexPreviousBwd, previousNote);
								// Add new predicted duration to conflictsRecords
//								conflictsRecord = 
//									conflictsRecord.concat("new predicted duration: " + predictedDurationPrevious + "\r\n" + "\r\n");							
								String s = Arrays.asList(new Rational[]{predictedDurationPrevious}).toString(); // 30-5
								conflictsRecord = 
									conflictsRecord.concat("type (i) conflict between notes ").concat(
									String.valueOf(noteIndexBwd)).concat(" and ").concat(
									String.valueOf(noteIndexPreviousBwd)).concat(
									": note ").concat(String.valueOf(noteIndexPreviousBwd)).concat(
//									" reassigned to duration ").concat(predictedDurationPrevious.toString()).concat("\r\n");
									" reassigned to duration ").concat(s).concat("\r\n");
							}
						}

						// b. For interrupting next notes in the tablature case (which only exist when modelling duration 
						// and using the bwd model)
//						else if (!isBidirectional && listNum == 0 && isTablatureCase && modelDuration && modelBackward ||
//								  isBidirectional && listNum == 0 && isTablatureCase && modelDuration) {	
						else if (listNum == 0 && isTablatureCase && modelDuration && pm == ProcessingMode.BWD) { // JOA     				
							// NB: nv will always contain notes as this else is only read when currentListOfIndices is not empty 
							NotationVoice nv = newTranscription.getPiece().getScore().get(firstPredictedVoice).get(0);
							// Find the pitch and the onset time of the next Note in nv
							Rational metricTimeNext = null;
							int pitchNext = -1;
							for (NotationChord nc : nv) {
								if (nc.getMetricTime().isGreater(metricTime)) {
									metricTimeNext = nc.getMetricTime();
									pitchNext = nc.get(0).getMidiPitch();
									break;
								}
							}

							// If the offset time of the note at noteIndex is greater than the onset time of the next note
							if (offsetTime.isGreater(metricTimeNext)) {
								// In the unidir case always; in the bidir case only when modelling duration again
								if (dc == DecisionContext.UNIDIR || dc == DecisionContext.BIDIR && modelDurationAgain) { // NIEUW
									// 1. Add index to conflictIndices
									if (!conflictIndices.get(1).contains(noteIndexBwd)) {
										conflictIndices.get(1).add(noteIndexBwd);
									}
	
									// 2. List the index, the metric position, and the sequence number in the chord of the next note 
									// in nv
									int noteIndexBwdNext = -1;
									String metPosNext = null;
									int indexInChordNext = -1;
									for (int j = 0; j < basicTabSymbolProperties.length; j++) { 
										int p = basicTabSymbolProperties[j][Tablature.PITCH];
										Rational met = new Rational(basicTabSymbolProperties[j][Tablature.ONSET_TIME], 
											Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
										if (p == pitchNext && met.equals(metricTimeNext)) {
											int noteIndexNext = j;
											// Determine the bwd counterpart of noteIndexNext and set noteIndexBwdNext to it
											for (int k = 0; k < backwardsMapping.size(); k++) {
												if (backwardsMapping.get(k) == noteIndexNext) {
													noteIndexBwdNext = k; 
													break;
												}
											}
											// Set metPosNext and indexInChordNext 
											metPosNext = ToolBox.getMetricPositionAsString(allMetricPositions.get(noteIndexNext));
											indexInChordNext = basicTabSymbolProperties[j][Tablature.NOTE_SEQ_NUM];
											break;
										}
									}
	
									// 3. Record conflict and add to conflictsRecord
//									String voiceNum = "the first predicted voice";
//									conflictsRecord = conflictsRecord.concat(
//										"noteIndex = " + noteIndexBwd + "\r\n" +
//										"  type (i) conflict between note " + noteIndexBwd + " (bar: " + metPos + ", index in chord: " 
//										+ indexInChord +
//										") and note " + noteIndexBwdNext + " (bar: " + metPosNext + ", index in chord: " +  		
//										indexInChordNext + "):\r\n" +
//										"  the predicted duration for note " + noteIndexBwd + " (predicted duration: " + predictedDuration + 
//										") gives an offset time greater than the onset time of the next note in " + voiceNum +  
//										" for note " + noteIndexBwd + " (predictedVoices: " + predictedVoices + ")\r\n"
//									);
//									conflictsRecord = conflictsRecord.concat("  --> predicted duration " + predictedDuration + " for note " +
//										noteIndexBwd + " adapted; ");
									
									// 4. Redetermine predictedDurationLabel and offsetTime
									Rational maxDur = metricTimeNext.sub(metricTime);
									int maxDurAsInt = maxDur.getNumer() * (Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom() / maxDur.getDenom());
									predictedDurationLabel = Transcription.createDurationLabel(maxDurAsInt);
									predictedDuration = maxDur;
									predictedDuration.reduce();
									offsetTime = metricTime.add(predictedDuration);;
									// Add new predicted duration to conflictsRecords
//									conflictsRecord = 
//										conflictsRecord.concat("new predicted duration: " +	predictedDuration + "\r\n" + "\r\n");									
									String s = Arrays.asList(new Rational[]{predictedDuration}).toString(); // 30-5
									conflictsRecord = 
										conflictsRecord.concat("type (i) conflict between notes ").concat(
										String.valueOf(noteIndexBwd)).concat(" and ").concat(
										String.valueOf(noteIndexBwdNext)).concat(
										": note ").concat(String.valueOf(noteIndexBwd)).concat(
//										" reassigned to duration ").concat(predictedDuration.toString()).concat("\r\n");
										" reassigned to duration ").concat(s).concat("\r\n");
								}
							}
						}

						// c. 
						// (1) For sustained previous notes in the non-tablature case (listNum == 0 && fwd model)
						// (2) For interrupting next notes in the non-tablature case (listNum == 0 && bwd model)
						// (3) For lower chord notes in both tablature- and non-tablature case (listNum == 1)
						//dndeze2
						else if (
//							!isBidirectional && listNum == 0 && !isTablatureCase && !modelBackward ||	
//							!isBidirectional && listNum == 0 && !isTablatureCase && modelBackward ||
//							isBidirectional && listNum == 0 && !isTablatureCase || // NIEUW
//							listNum == 1) {	
							listNum == 0 && !isTablatureCase && pm == ProcessingMode.FWD ||	
							listNum == 0 && !isTablatureCase && pm == ProcessingMode.BWD ||
							listNum == 1) { // JOA
										
							boolean sustained = false;
							boolean interrupted = false;

							if (listNum == 0 && pm == ProcessingMode.FWD) {
//							if (!isBidirectional && listNum == 0 && !modelBackward) {
								sustained = true;
								interrupted = false;
							}
							if (listNum == 0 && pm == ProcessingMode.BWD) {
//							if (!isBidirectional && listNum == 0 && modelBackward) {
								sustained = false;
								interrupted = true;
							}
//							if (isBidirectional && listNum == 0) { // NIEUW
//								// fwd and noteIndex > noteIndexPrevious: sustained note
//								if (!modelBackward && noteIndexBwd > noteIndexPreviousBwd) {
//									sustained = true;
//									interrupted = false;
//								}
//								// fwd and noteIndex < noteIndexPrevious: interrupted note
//								else if (!modelBackward && noteIndexBwd < noteIndexPreviousBwd) {
//									sustained = false;
//									interrupted = true;
//								}
//								// bwd and noteIndex > noteIndexPrevious: interrupted note
//								else if (modelBackward && noteIndexBwd > noteIndexPreviousBwd) {
//									sustained = false;
//									interrupted = true;
//								}
//								// bwd and noteIndex < noteIndexPrevious: sustained note
//								else if (modelBackward && noteIndexBwd < noteIndexPreviousBwd) {
//									sustained = true;
//									interrupted = false;
//								}
//							}
							if (listNum == 1) {
								sustained = false;
								interrupted = false;
							}

							// 1. Add index to conflictIndices
							if (!conflictIndices.get(0).contains(noteIndexBwd)) {
								conflictIndices.get(0).add(noteIndexBwd);
							}

							// 2. Set to 0.0 in copyOfNetworkOutputCurrentNote and reset allNetworkOutputsAdapted
							copyOfNetworkOutputCurrentNote[firstPredictedVoicePrevious] = 0.0;
							allNetworkOutputsAdapted.set(noteIndexBwd, copyOfNetworkOutputCurrentNote);	
							// 3. Record conflict and add to conflictsRecord 
//							conflictsRecord = conflictsRecord.concat(getConflictText("(i)", noteIndexBwd, metPos, indexInChord, 
//								noteIndexPreviousBwd, metPosPrevious, indexInChordPrevious, predictedVoices, predictedVoicesPrevious,
//								sustained, interrupted));
//							conflictsRecord = conflictsRecord.concat("  --> voice " + firstPredictedVoice + 
//								" made unavailable for note " + noteIndexBwd + "; ");

//							// In the 5vv case: check whether all voices have been zeroed out
//							boolean onlyZeroes = true;
//							for (double d : copyOfNetworkOutputCurrentNote) {
//								if (d != 0.0) {
//									onlyZeroes = false;
//								}
//							}
//							// If all voices have been zeroed out: reset predictedVoices to its original
//							// value and skip the conflict
//							if (highestNumVoicesTraining == 5 && onlyZeroes) {
////								predictedVoices = new ArrayList<Integer>();
//								predictedVoices = originalPredictedVoices;
//								skipLowerNotes = true;
//								break; // from inner for
//							}
							
							// If all available voices have been zeroed out: restore original predicted
							// voices and give type (v) conflict
							// Add the index of the previous note to the list of notes that the current
							// note conflicts with
							typeFiveList.add(noteIndexPreviousBwd);
							if (ToolBox.sumDoubleArray(Arrays.copyOfRange(copyOfNetworkOutputCurrentNote, 
								0, highestNumVoicesTraining)) == 0.0) {
								predictedVoices = new ArrayList<Integer>();
								predictedVoices = originalPredictedVoices;
								String v = "voice ";
								if (originalPredictedVoices.size() > 1) {
									v = "voices ";
								}
								conflictsRecord = // 30-5
									conflictsRecord.concat("type (v) conflict between notes ").concat(
									String.valueOf(noteIndexBwd)).concat(" and ").concat(
									String.valueOf(typeFiveList).replace("[", "").replace("]", "")).concat(
									": note ").concat(String.valueOf(noteIndexBwd)).concat(
									" reassigned to " + v).concat(originalPredictedVoices.toString()).concat(
									"\r\n");
								skipLowerNotes = true;
								System.out.println("type (v) conflict between notes ".concat(
									String.valueOf(noteIndexBwd)).concat(" and ").concat(
									String.valueOf(typeFiveList).replace("[", "").replace("]", "")).concat(
									": note ").concat(String.valueOf(noteIndexBwd)).concat(
									" reassigned to " + v).concat(originalPredictedVoices.toString()).concat(
									"\r\n"));
								break; // from inner for
							}
							
							// 4. Redetermine predictedVoices and do another iteration over all previous notes 
							// to see whether the new first predicted voice is the same as any of the first
							// predicted voices for the previous notes. If so, resolve and repeat until all 
							// type (i) conflicts are resolved --> i.e., adapt the predicted label for the note
							// at noteIndex as many times as is needed for its highest activation value to 
							// represent a voice not yet taken by a previous note
							predictedVoices = 
								OutputEvaluator.interpretNetworkOutput(copyOfNetworkOutputCurrentNote, 
								allowCoD, deviationThreshold).get(0);
							firstPredictedVoice = predictedVoices.get(0);
							
							// 20.03.2018: hack added to avoid having a non-existing voice being
							// selected over an available existing voice 
							// Example: in a 4vv piece, after a few voices have been zeroed out, the
							// network output is [0.01, 0.0, 0.0, 0.0, 0.02]. This leads to the 
							// non-existing voice 4 being selected over the also available voice 0 
							if (!isTablatureCase && firstPredictedVoice > highestNumVoicesTraining - 1) {
//							if (!isTablatureCase && firstPredictedVoice >= Transcription.MAXIMUM_NUMBER_OF_VOICES-1) {
								int firstPredVoiceNonEx = firstPredictedVoice;
								// Set to 0.0 in copyOfNetworkOutputCurrentNote and reset allNetworkOutputsAdapted
								copyOfNetworkOutputCurrentNote[firstPredictedVoice] = 0.0;
								allNetworkOutputsAdapted.set(noteIndexBwd, copyOfNetworkOutputCurrentNote);
								predictedVoices = 
									OutputEvaluator.interpretNetworkOutput(copyOfNetworkOutputCurrentNote, 
									allowCoD, deviationThreshold).get(0);
								firstPredictedVoice = predictedVoices.get(0);
								conflictsRecord = 
									conflictsRecord.concat("hack at note " + String.valueOf(noteIndexBwd) + 
									": note assigned to non-existing voice " + firstPredVoiceNonEx + 
									" reassigned to voice " + String.valueOf(firstPredictedVoice) + "\r\n");
							}

							// Add new predicted voices to conflictsRecords
//							conflictsRecord = conflictsRecord.concat("new predicted voices: " +	predictedVoices + "\r\n" + "\r\n");
							conflictsRecord = // 30-5
								conflictsRecord.concat("type (i) conflict between notes ").concat(
								String.valueOf(noteIndexBwd)).concat(" and ").concat(
								String.valueOf(noteIndexPreviousBwd)).concat(
								": note ").concat(String.valueOf(noteIndexBwd)).concat(
								" reassigned to voice ").concat(predictedVoices.toString()).concat(
								"\r\n");

							i = -1;
							// In the tablature case when modelling duration (i.e., when listNum == 1): set checkSustainedNotesAgain
							// to true 
							if (isTablatureCase && modelDuration) { // && !modelBackward) {
								checkSustainedNotesAgain = true;
							}   			
						} //dndeze2 end
					} //dndeze1 end 
				}
				// If checkSustainedNotesAgain is true, i.e., if type (i) conflicts with any lower chordNotes have been 
				// found and predictedVoices has been changed: re-check for type (i) and (ii) conflicts with sustained/
				// interrupting notes
				if (checkSustainedNotesAgain) {
					listNum = -1;
				}
				
				// If skipLowerNotes is true, i.e., if all voices have been zeroed out already in the conflict
				// resolution with interrupting next notes (non-tablature case, 5vv pieces only): break from 
				// outer for (so skip listNum == 1 (lower chord notes)) and proceed to end method
				if (skipLowerNotes) {
					break; // from outer for
				}

				// 2. Resolve any type (ii) conflicts (conflicts between the first predicted voice for the note at noteIndex 
				// and any second predicted voices for any previous notes). Type (ii) conflicts can only occur if a CoD was 
				// predicted for the previous note.
				// Do only if checkSustainedNotesAgain is false, i.e., if no type (i) conflicts with any lower chordNotes 
				// have been found and predictedVoices has not been changed (meaning that firstPredictedVoice now has its 
				// final value)
				else if (!checkSustainedNotesAgain) {
					if (isTablatureCase) {
						for (int i = 0; i < currentListOfIndices.size(); i++) {
							int noteIndexPrevious = currentListOfIndices.get(i);
							// Determine noteIndexPreviousBwd, which is needed when using the bwd model for the lists in bwd order. 
							// When using the fwd model, it will always be the same as noteIndexPrevious
							int noteIndexPreviousBwd = noteIndexPrevious;
							if (pm == ProcessingMode.BWD) {
								for (int j = 0; j < backwardsMapping.size(); j++) {
									if (backwardsMapping.get(j) == noteIndexPrevious) {
										noteIndexPreviousBwd = j;
										break;
									}
								}
							}
							List<Integer> predictedVoicesPrevious = allPredictedVoices.get(noteIndexPreviousBwd);
							if (predictedVoicesPrevious.size() == 2) {
								int firstPredictedVoicePrevious = predictedVoicesPrevious.get(0);
								int secondPredictedVoicePrevious = predictedVoicesPrevious.get(1);
								// If secondPredictedVoicePrevious is the same as firstPredictedVoice
								if (secondPredictedVoicePrevious == firstPredictedVoice) {
									// 0. Get the metric position and the index in the chord of the previous note
									String metPosPrevious = ToolBox.getMetricPositionAsString(allMetricPositions.get(noteIndexPrevious));
									int indexInChordPrevious = basicTabSymbolProperties[noteIndexPrevious][Tablature.NOTE_SEQ_NUM];  	  	
	
									// a. For sustained previous notes (which only exist when modelling duration using the fwd model)
									if (dc == DecisionContext.UNIDIR && listNum == 0 && modelDuration && pm == ProcessingMode.FWD ||
										dc == DecisionContext.BIDIR && listNum == 0 && modelDuration) {
//										if (listNum == 0 && modelDuration && !modelBackward) { OUD
										boolean sustained = true;
										boolean interrupted = false;
										List<Double> predictedDurationLabelPrevious = allDurationLabels.get(noteIndexPrevious);
										Rational predictedDurationPrevious = null; // allPredictedDurations.get(noteIndexPreviousBwd)[0];
										if (dc == DecisionContext.UNIDIR || dc == DecisionContext.BIDIR && modelDurationAgain) {
											predictedDurationPrevious = allPredictedDurations.get(noteIndexPreviousBwd)[0];
										}
										else if (dc == DecisionContext.BIDIR && !modelDurationAgain) {
											predictedDurationPrevious = 
												DataConverter.convertIntoDuration(predictedDurationLabelPrevious)[0]; 
										}
										predictedDurationPrevious.reduce();
	
										// In the unidir case always; in the bidir case only when modelling duration again
										if (dc == DecisionContext.UNIDIR || dc == DecisionContext.BIDIR && modelDurationAgain) { // NIEUW
											// 1. Add index to conflictIndices
											if (!conflictIndices.get(1).contains(noteIndexPreviousBwd)) {
												conflictIndices.get(1).add(noteIndexPreviousBwd);
											}  	  	  	
		
											// 2. Reset the Note's duration in Transcription
											// Determine the maximum duration and the pitch of the note at noteIndexPrevious. Because this note 
											// is followed by at least one note (the note at noteIndex), maxDurPrevious can never be null
											Rational metricTimePrevious = 
												new Rational(basicTabSymbolProperties[noteIndexPrevious][Tablature.ONSET_TIME],	
												Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
											Rational maxDurPrevious = 
												getMaximumDuration(metricTimePrevious, metricTime, newTranscription, predictedVoicesPrevious);
		
											int pitchPrevious = basicTabSymbolProperties[noteIndexPrevious][Tablature.PITCH];
											// Reset the note at noteIndexPrevious in the Transcription. The for-loop is needed because the 
											// duration of its first predicted voice must be adapted as well 
											ScoreNote adaptedScoreNotePrevious = 
												new ScoreNote(new ScorePitch(pitchPrevious), metricTimePrevious, maxDurPrevious);
											for (int predictedVoicePrevious : predictedVoicesPrevious) {
												NotationVoice nv = newTranscription.getPiece().getScore().get(predictedVoicePrevious).get(0);
												for (NotationChord nc : nv) {
													if (nc.getMetricTime().equals(metricTimePrevious)) {
											 	  		nc.get(0).setScoreNote(adaptedScoreNotePrevious);
													}
												}
											}
		
											// 3. Record conflict and add to conflictsRecord
//											conflictsRecord = conflictsRecord.concat(getConflictText("(ii)", 
//												noteIndexBwd, metPos, indexInChord, noteIndexPreviousBwd,
//												metPosPrevious, indexInChordPrevious, predictedVoices, 
//												predictedVoicesPrevious, sustained, interrupted));
//											conflictsRecord = conflictsRecord.concat("  --> predicted duration " +
//												predictedDurationPrevious +	" for note " + noteIndexPreviousBwd +
//												" adapted; ");

											// 4. Redetermine predictedDurationLabelPrevious and reset the corresponding elements in the Lists
											int maxDurAsInt = 
												maxDurPrevious.getNumer() *	(Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom() / maxDurPrevious.getDenom());
											predictedDurationLabelPrevious = Transcription.createDurationLabel(maxDurAsInt);
											allDurationLabels.set(noteIndexPrevious, predictedDurationLabelPrevious);
											predictedDurationPrevious = maxDurPrevious;	
											predictedDurationPrevious.reduce();
											if (dc == DecisionContext.UNIDIR || dc == DecisionContext.BIDIR && modelDurationAgain) {
												allPredictedDurations.set(noteIndexPreviousBwd, new Rational[]{predictedDurationPrevious});
											}
											List<Note> previousNote = allNotes.get(noteIndexPreviousBwd);
											for (Note n : previousNote) {
												n.setScoreNote(adaptedScoreNotePrevious);
											}
											allNotes.set(noteIndexPreviousBwd, previousNote);
											// Add new predicted duration to conflictsRecords
//											conflictsRecord = 
//												conflictsRecord.concat("new predicted duration: " +	predictedDurationPrevious + "\r\n" + "\r\n");
											String s = Arrays.asList(new Rational[]{predictedDurationPrevious}).toString(); // 30-5
											conflictsRecord = 
												conflictsRecord.concat("type (ii) conflict between notes ").concat(
												String.valueOf(noteIndexBwd)).concat(" and ").concat(
												String.valueOf(noteIndexPreviousBwd)).concat(
												": note ").concat(String.valueOf(noteIndexPreviousBwd)).concat(
//												" reassigned to duration ").concat(predictedDurationPrevious.toString()).concat("\r\n");
												" reassigned to duration ").concat(s).concat("\r\n");		
										}
									}
	
									// b. For interrupting next notes (listNum == 0 && model duration && bwd model)
									else if (dc == DecisionContext.UNIDIR && listNum == 0 && modelDuration && pm == ProcessingMode.BWD ||
										dc == DecisionContext.BIDIR && listNum == 0 && modelDuration) {
//										else if (listNum == 0 && modelDuration && modelBackward) { OUD
										// NB: nv will always contain notes as this else is only read when currentListOfIndices is not 
										// empty 
										NotationVoice nv = 
											newTranscription.getPiece().getScore().get(secondPredictedVoicePrevious).get(0);
										// Find the pitch and the onset time of the next Note in nv
										Rational metricTimeNext = null;
										int pitchNext = -1;
										for (NotationChord nc : nv) {
											if (nc.getMetricTime().isGreater(metricTime)) {
												metricTimeNext = nc.getMetricTime();
												pitchNext = nc.get(0).getMidiPitch();
												break;
											}
										}
	
										// If the offset time of the note at noteIndex is greater than the onset time of the next note
										if (offsetTime.isGreater(metricTimeNext)) {
											
											// In the unidir case always; in the bidir case only when modelling duration again
											if (dc == DecisionContext.UNIDIR || dc == DecisionContext.BIDIR && modelDurationAgain) { // NIEUW
												// 1. Add index to conflictIndices
												if (!conflictIndices.get(1).contains(noteIndexBwd)) {
													conflictIndices.get(1).add(noteIndexBwd);
												}
		
												// 2. Reset the Note's duration in Transcription 
												// List the index, the metric position, and the sequence number in the chord of the next  
												// note in nv
												int noteIndexBwdNext = -1;
												String metPosNext = null;
												int indexInChordNext = -1;
												for (int j = 0; j < basicTabSymbolProperties.length; j++) { 
													int p = basicTabSymbolProperties[j][Tablature.PITCH];
													Rational met = 
														new Rational(basicTabSymbolProperties[j][Tablature.ONSET_TIME], 
														Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
													if (p == pitchNext && met.equals(metricTimeNext)) {
														int noteIndexNext = j;
														// Determine the bwd counterpart of noteIndexNext and set noteIndexBwdNext to it
														for (int k = 0; k < backwardsMapping.size(); k++) {
															if (backwardsMapping.get(k) == noteIndexNext) {
																noteIndexBwdNext = k; 
																break;
															}
														}
														// Set metPosNext and indexInChordNext 
														metPosNext = ToolBox.getMetricPositionAsString(allMetricPositions.get(noteIndexNext));
														indexInChordNext = basicTabSymbolProperties[j][Tablature.NOTE_SEQ_NUM];
														break;
													}
												}
		
												// 3. Record conflict and add to conflictsRecord
//												String voiceNum = "the second predicted voice";
//												conflictsRecord = conflictsRecord.concat(
//													"noteIndex = " + noteIndexBwd + "\r\n" +
//													"  type (ii) conflict between note " + noteIndexBwd + " (bar: " + metPos + ", index in chord: " 
//													+ indexInChord +
//													") and note " + noteIndexBwdNext	+ " (bar: " + metPosNext + ", index in chord: " +  		
//													indexInChordNext + "):\r\n" +
//													"  the predicted duration for note " + noteIndexBwd + " (predicted duration: " + predictedDuration + 
//													") gives an offset time greater than the onset time of the next note in " + voiceNum +  
//													" for note " + noteIndexBwd + " (predictedVoices: " + predictedVoices + ")\r\n"
//												);	
//												conflictsRecord = conflictsRecord.concat("  --> predicted duration " + predictedDuration + " for note " +
//													noteIndexBwd + " adapted; ");
												
												// 4. Redetermine predictedDurationLabel and offsetTime
												Rational maxDur = metricTimeNext.sub(metricTime);
												int maxDurAsInt = maxDur.getNumer() * (Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom() / maxDur.getDenom());
												predictedDurationLabel = Transcription.createDurationLabel(maxDurAsInt);
												predictedDuration = maxDur;
												predictedDuration.reduce();
												offsetTime = metricTime.add(predictedDuration);;
												// Add new predicted duration to conflictsRecords
//												conflictsRecord = 
//													conflictsRecord.concat("new predicted duration: " +	predictedDuration + "\r\n" + "\r\n");
												String s = Arrays.asList(new Rational[]{predictedDuration}).toString(); // 30-5
												conflictsRecord = 
													conflictsRecord.concat("type (ii) conflict between notes ").concat(
													String.valueOf(noteIndexBwd)).concat(" and ").concat(
													String.valueOf(noteIndexBwdNext)).concat(
													": note ").concat(String.valueOf(noteIndexBwd)).concat(
//													" reassigned to duration ").concat(predictedDuration.toString()).concat("\r\n");
													" reassigned to duration ").concat(s).concat("\r\n");
											}
										}
									}
	
									// c. For lower chord notes 
									else if (listNum == 1) {
										boolean sustained = false;
										boolean interrupted = false;
										// 1. Add index to conflictIndices
										if (!conflictIndices.get(0).contains(noteIndexPreviousBwd)) {
											  conflictIndices.get(0).add(noteIndexPreviousBwd);
										}  	  	  	
										// 2. Remove the Note from the Transcription 
										int pitchPrevious = basicTabSymbolProperties[noteIndexPrevious][Tablature.PITCH];
										Rational metricTimePrevious = new Rational(basicTabSymbolProperties[noteIndexPrevious][Tablature.ONSET_TIME], 
											Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
										newTranscription.removeNote(pitchPrevious, secondPredictedVoicePrevious, metricTimePrevious);      
										// 3. Record conflict and add to conflictsRecord
//										conflictsRecord = conflictsRecord.concat(getConflictText("(ii)", noteIndexBwd, metPos, indexInChord,
//											noteIndexPreviousBwd, metPosPrevious, indexInChordPrevious, predictedVoices, predictedVoicesPrevious,
//											sustained, interrupted));
//										conflictsRecord = conflictsRecord.concat("  --> assignment to voice " + secondPredictedVoicePrevious + 
//											" made undone for note " + noteIndexPreviousBwd + "; ");  		

										// 4. Redetermine predictedVoicePrevious and reset the corresponding elements in the Lists
										predictedVoicesPrevious = Arrays.asList(new Integer[]{firstPredictedVoicePrevious});
										allPredictedVoices.set(noteIndexPreviousBwd, predictedVoicesPrevious);
										allVoiceLabels.set(noteIndexPrevious, DataConverter.convertIntoVoiceLabel(predictedVoicesPrevious));
	
//										if (isTablatureCase && isNewModel && modelDuration) {//2016
										if (isTablatureCase /*&& im == Implementation.PHD*/ && modelDuration) {
											allVoicesCoDNotes.set(noteIndexPrevious, null);
											// NB: The durationLabel need not be adapted as currently only one duration is predicted for both 
											// CoDNotes TODO Change?
										}
										// Add new predicted voices to conflictsRecords
//										conflictsRecord = 
//											conflictsRecord.concat("new predicted voices: " + predictedVoicesPrevious + "\r\n" + "\r\n");
										conflictsRecord = // 30-5
											conflictsRecord.concat(
											"type (ii) conflict between notes ").concat(
											String.valueOf(noteIndexBwd)).concat(" and ").concat(
											String.valueOf(noteIndexPreviousBwd)).concat(
											": note ").concat(String.valueOf(noteIndexPreviousBwd)).concat(
											" reassigned to voice ").concat(predictedVoicesPrevious.toString()).concat(
											"\r\n");
									}
								}
							}
						}
					}
				}
//				// If checkSustainedNotesAgain is true, i.e., if type (i) conflicts with any lower chordNotes have been 
//				// found and predictedVoices has been changed: re-check for type (i) and (ii) conflicts with sustained/
//				// interrupting notes
//				else {
//					listNum = -1;
//				}
			}

			// 3. Resolve any type (iii) and (iv) conflicts, both of which apply to the tablature case only
			if (isTablatureCase) {
				// 1. Resolve any type (iii) conflicts (conflicts between the second predicted voice for the note at noteIndex
				// and any first or second predicted voices for any preceding notes). Type (iii) conflicts can only occur 
				// if a CoD was predicted for the note at noteIndex
				if (predictedVoices.size() == 2) { 
					int secondPredictedVoice = predictedVoices.get(1);
					// Gather the indices of all preceding notes (sustained previous/interrupting next and lower chord 
					// notes) in a list
					List<Integer> indicesRelatedToSustainedAndLowerChordNotes = new ArrayList<Integer>(indicesRelatedToSustainedNotes);
					indicesRelatedToSustainedAndLowerChordNotes.addAll(indicesOfLowerChordNotes);
					for (int i = 0; i < indicesRelatedToSustainedAndLowerChordNotes.size(); i++) {
						int noteIndexPrevious = indicesRelatedToSustainedAndLowerChordNotes.get(i);
						// Determine noteIndexPreviousBwd, which is needed when using the bwd model for the lists in bwd order. 
						// When using the fwd model, it will always be the same as noteIndexPrevious
						int noteIndexPreviousBwd = noteIndexPrevious;
						if (pm == ProcessingMode.BWD) {
							for (int j = 0; j < backwardsMapping.size(); j++) {
								if (backwardsMapping.get(j) == noteIndexPrevious) {
									noteIndexPreviousBwd = j;
									break;
								}
							}
						}
						List<Integer> predictedVoicesPrevious = allPredictedVoices.get(noteIndexPreviousBwd);
						// If predictedVoicesPrevious contains secondPredictedVoice
						if (predictedVoicesPrevious.contains(secondPredictedVoice)) {
							boolean sustained = false;
							boolean interrupted = false;

							if (dc == DecisionContext.UNIDIR && pm == ProcessingMode.FWD && indicesRelatedToSustainedNotes.contains(noteIndexPrevious)) {
								sustained = true;
								interrupted = false;
							}
							if (dc == DecisionContext.UNIDIR && pm == ProcessingMode.BWD && indicesRelatedToSustainedNotes.contains(noteIndexPrevious)) {
								sustained = false;
								interrupted = true;
							}
							if (dc == DecisionContext.BIDIR && indicesRelatedToSustainedNotes.contains(noteIndexPrevious)) { // NIEUW
								// fwd and noteIndex > noteIndexPrevious: sustained note
								if (pm == ProcessingMode.FWD && noteIndexBwd > noteIndexPreviousBwd) {
									sustained = true;
									interrupted = false;
								}
								// fwd and noteIndex < noteIndexPrevious: interrupted note
								else if (pm == ProcessingMode.FWD && noteIndexBwd < noteIndexPreviousBwd) {
									sustained = false;
									interrupted = true;
								}
								// bwd and noteIndex > noteIndexPrevious: interrupted note
								else if (pm == ProcessingMode.BWD && noteIndexBwd > noteIndexPreviousBwd) {
									sustained = false;
									interrupted = true;
								}
								// bwd and noteIndex < noteIndexPrevious: sustained note
								else if (pm == ProcessingMode.BWD && noteIndexBwd < noteIndexPreviousBwd) {
									sustained = true;
									interrupted = false;
								}
							}
							if (indicesOfLowerChordNotes.contains(noteIndexPrevious)) {
								sustained = false;
								interrupted = false;
							}

							// 0. Get the metric position and the index in the chord of the previous note
							String metPosPrevious = ToolBox.getMetricPositionAsString(allMetricPositions.get(noteIndexPrevious));
							int indexInChordPrevious = basicTabSymbolProperties[noteIndexPrevious][Tablature.NOTE_SEQ_NUM];  	
							// 1. Add index to conflictIndices
							if (!conflictIndices.get(0).contains(noteIndexBwd)) {
								conflictIndices.get(0).add(noteIndexBwd);
							}

							// 2. Set to 0.0 in copyOfNetworkOutputCurrentNote and reset allNetworkOutputsAdapted
							copyOfNetworkOutputCurrentNote[secondPredictedVoice] = 0.0;
							allNetworkOutputsAdapted.set(noteIndexBwd, copyOfNetworkOutputCurrentNote);        
							// 3. Record conflict and add to conflictsRecord
//							conflictsRecord = conflictsRecord.concat(getConflictText("(iii)", noteIndexBwd, metPos, indexInChord, 
//								noteIndexPreviousBwd, metPosPrevious, indexInChordPrevious, predictedVoices, predictedVoicesPrevious, 
//								sustained, interrupted));
//							conflictsRecord = conflictsRecord.concat("  --> voice " + secondPredictedVoice + 
//								" made unavailable for note " + noteIndexBwd + "; ");

							// 4. Redetermine predictedVoices and break
							// NB: Using outputEvaluator.interpretNetworkOutput() here may result in a new second predicted 
							// voice for the note at noteIndex
							predictedVoices = Arrays.asList(new Integer[]{firstPredictedVoice});
							// Add new predicted voices to conflictsRecords
//							conflictsRecord = conflictsRecord.concat("new predicted voices: " +	predictedVoices + "\r\n" + "\r\n");							
							conflictsRecord = // 30-5
								conflictsRecord.concat(
								"type (iii) conflict between notes ").concat(
								String.valueOf(noteIndexBwd)).concat(" and ").concat(
								String.valueOf(noteIndexPreviousBwd)).concat(
								": note ").concat(String.valueOf(noteIndexBwd)).concat(
								" reassigned to voice ").concat(predictedVoices.toString()).concat(
								"\r\n");
							break;
						} 
					}	
				}

				// 2. Resolve any type (iv) conflicts (conflicts between all notes x that, at the onset time of the note at
				// noteIndex, have an offset time that exceeds the onset time of the notes y in the next chord (i.e, the 
				// chord after the note at noteIndex) if the sum of x and y is greater than maxNumVoices)
//				if (!isBidirectional && isTablatureCase && modelDuration && !modelBackward ||
//					isBidirectional && isTablatureCase && modelDuration) {
				if (isTablatureCase && modelDuration && pm == ProcessingMode.FWD) { // JOA

					// Determine sizeChord, sizeNextChord, onsetTimeNextChord, metPosNextChord, and maxNumVoices
					int sizeChord = basicTabSymbolProperties[noteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
					int sizeNextChord = -1;
					Rational onsetTimeNextChord = null;
					List<Integer> indicesOfExceeded = new ArrayList<Integer>();
//					String metPosNextChord = null;
					int chordIndex = basicTabSymbolProperties[noteIndex][Tablature.CHORD_SEQ_NUM];
					int lastChordIndex = basicTabSymbolProperties[basicTabSymbolProperties.length - 1][Tablature.CHORD_SEQ_NUM];
					if (chordIndex != lastChordIndex) {
						for (int i = 0; i < basicTabSymbolProperties.length; i++) {
//						for (Integer[] btp : basicTabSymbolProperties) {
							Integer[] btp = basicTabSymbolProperties[i];
							if (btp[Tablature.CHORD_SEQ_NUM] == chordIndex + 1) {
								sizeNextChord = btp[Tablature.CHORD_SIZE_AS_NUM_ONSETS];
								for (int j = 0; j < sizeNextChord; j++) {
									indicesOfExceeded.add(i + j);
								}
								onsetTimeNextChord = 
									new Rational(btp[Tablature.ONSET_TIME], Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
//								metPosNextChord = 
//									ToolBox.getMetricPositionAsString(Tablature.getMetricPosition(onsetTimeNextChord, meterInfo));
								break;
							}
						}
					}
					int maxNumVoices = newTranscription.getPiece().getScore().size(); // TODO this is possible because newTranscription has highestNumberOfVoicesTraining voices

					// If the note at noteIndex is the last note in the chord and there is a next chord
					if ((indexInChord == sizeChord - 1) && sizeNextChord != -1) {
						// Get the indices of any sustained notes
						List<Integer> indicesOfSustained = 
							Transcription.getIndicesOfSustainedPreviousNotes(basicTabSymbolProperties, 
							allDurationLabels, null, lowestNoteIndex);
						// Get the indices of any lower chord notes
						List<Integer> indicesOfChordNotes = new ArrayList<Integer>();
						for (int i = lowestNoteIndex; i <= noteIndex; i++) {
							indicesOfChordNotes.add(i);
						}
						// Combine into list with all indices
						List<Integer> allIndices = new ArrayList<Integer>();
						allIndices.addAll(indicesOfSustained);
						allIndices.addAll(indicesOfChordNotes);

						// List all offset times exceeding the onset time of the next chord
						List<Integer> indicesOfExceeding = new ArrayList<Integer>();
						for (int i : allIndices) {
							Rational onset = new Rational(basicTabSymbolProperties[i][Tablature.ONSET_TIME], 
						    	Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
							Rational duration = null;
							// If i != noteIndex, get the duration from allPredictedDurations 
							if (i != noteIndex) {
								if (dc == DecisionContext.UNIDIR || dc == DecisionContext.BIDIR && modelDurationAgain) {
									duration = allPredictedDurations.get(i)[0];
								}
								else if (dc == DecisionContext.BIDIR && !modelDurationAgain) {
									duration = DataConverter.convertIntoDuration(allDurationLabels.get(i))[0];
								}
							}
							// If i == noteIndex, allPredictedDurations does not yet contain a value at index i
							else {
								duration = predictedDuration;
							}
							Rational offset = new Rational(onset.add(duration));
							if (offset.isGreater(onsetTimeNextChord)) {
								indicesOfExceeding.add(i);
							}
						}
						int numExceeding = indicesOfExceeding.size();
						// If the number of exceeding notes and the size of the next chord is greater than the number of voices
						if ((numExceeding + sizeNextChord) > maxNumVoices) {  	    
							// Determine the number of notes whose duration must be adapted so that maxNumVoices is not exceeded, 
							// and remove the necessary elements from indicesOfExceeding. Later indices are more likely to have an
							// incorrect duration because they may not have gone through duration conflicts yet; therefore, remove
							// elements from the start of the list
							int numToBeAdapted = (numExceeding + sizeNextChord) - maxNumVoices;
							int listSize = indicesOfExceeding.size();
							indicesOfExceeding = indicesOfExceeding.subList(listSize - numToBeAdapted, listSize); 

							List<String> metPoss = new ArrayList<String>();
							List<Integer> indicesInChord = new ArrayList<Integer>(); 
							List<Rational> durations = new ArrayList<Rational>();
							List<Rational> durationsAdapted = new ArrayList<Rational>();
							
							// In the unidir case always; in the bidir case only when modelling duration again
							if (dc == DecisionContext.UNIDIR || dc == DecisionContext.BIDIR && modelDurationAgain) { // NIEUW
								// For each note at index i
								for (int i : indicesOfExceeding) {
									// 0. Get the pitch and onset of the note; also add to Lists
									int pitch = basicTabSymbolProperties[i][Tablature.PITCH];
									Rational onset = new Rational(basicTabSymbolProperties[i][Tablature.ONSET_TIME],
										Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
									metPoss.add(ToolBox.getMetricPositionAsString(Tablature.getMetricPosition(onset, meterInfo)));
									indicesInChord.add(basicTabSymbolProperties[i][Tablature.NOTE_SEQ_NUM]);
									// If i != noteIndex, get the duration from allPredictedDurations
									if (i != noteIndex) {  
										if (dc == DecisionContext.UNIDIR || dc == DecisionContext.BIDIR && modelDurationAgain) {
											durations.add(allPredictedDurations.get(i)[0]);
										}
										else if (dc == DecisionContext.BIDIR && !modelDurationAgain) {
											durations.add(DataConverter.convertIntoDuration(allDurationLabels.get(i))[0]);
										}
									}
									// If i == noteIndex, allPredictedDurations does not yet contain a value at index i
									else {
									    durations.add(predictedDuration);
									}
	
									
									// 1. Add to conflictIndices
									if (!conflictIndices.get(1).contains(i)) {
										conflictIndices.get(1).add(i);
									}
	
									// 2. Adapt
									// a. If i != noteIndex
									if (i != noteIndex) {
										List<Integer> currentVoices = allPredictedVoices.get(i);
										Rational maxDur = null;
										ScoreNote adaptedScoreNote = null;
										// 1. Adapt the Note's duration in newTranscription
										for (int voice : currentVoices) {
											NotationVoice nv = newTranscription.getPiece().getScore().get(voice).get(0);
											for (NotationChord nc : nv) {
												Note n = nc.get(0);
												if (n.getMidiPitch() == pitch && n.getMetricTime().equals(onset)) {
													// Determine the maximum duration and reset n with it
													maxDur = onsetTimeNextChord.sub(onset);
													adaptedScoreNote = new ScoreNote(new ScorePitch(pitch), onset, maxDur); 
													n.setScoreNote(adaptedScoreNote);
													break;
												}
											}
										}
										// Add to durationsAdapted (this has to happen outside the for-loop, because in the case of a CoD 
										// the duration must be added only once)
										durationsAdapted.add(maxDur);
	
										// 2. Reset allDurationLabels, allPredictedDurations, and allNotes 
										int maxDurAsInt = maxDur.getNumer() * (Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom() / maxDur.getDenom());
										List<Double> predictedDurationLabelAdapted = Transcription.createDurationLabel(maxDurAsInt);
										allDurationLabels.set(i, predictedDurationLabelAdapted);
										if (dc == DecisionContext.UNIDIR || dc == DecisionContext.BIDIR && modelDurationAgain) {
											allPredictedDurations.set(i, new Rational[]{maxDur});
										}
										List<Note> previousNote = allNotes.get(i);
										for (Note n : previousNote) {
											n.setScoreNote(adaptedScoreNote);
										}
										allNotes.set(i, previousNote);
									}
	
									// b. If i == noteIndex 
									if (i == noteIndex) {
										// Adapt predictedDurationLabel and predictedDuration
										Rational maxDur = onsetTimeNextChord.sub(onset);
										int maxDurAsInt = maxDur.getNumer() * (Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom() / maxDur.getDenom());
										predictedDurationLabel = Transcription.createDurationLabel(maxDurAsInt);
										predictedDuration = maxDur;
										predictedDuration.reduce();
										// Add to durationsAdapted
										durationsAdapted.add(predictedDuration);
									}
								}
	
								// Add to conflictsRecord
//								conflictsRecord = conflictsRecord.concat(
//									"noteIndex = " + noteIndex + "\r\n" +	
//									"  type (iv) conflict between notes " + indicesOfExceeding + " (bars: " + metPoss + ", indices in chord: " + 
//									indicesInChord + ") and the chord at bar " + metPosNextChord + ":\r\n" +
//									"  the predicted durations for notes " + indicesOfExceeding + " (predicted durations: " + durations + 
//									") give offset times greater than the onset time of the chord at bar " + metPosNextChord + 
//									", resulting in a larger number of voices than allowed (" + maxNumVoices + ") \r\n" + 
//									"  --> predicted durations " + durations +	" for notes " + indicesOfExceeding + " adapted; "
//								);	
								// Add new predicted durations to conflictsRecords
//								conflictsRecord = 
//									conflictsRecord.concat("new predicted durations: " + durationsAdapted + "\r\n" + "\r\n");
								String n = "note ";
								String d = "duration ";
								if (indicesOfExceeding.size() > 1) {
									n = "notes ";
									d = "durations ";
								}
								conflictsRecord = 
									conflictsRecord.concat("type (iv) conflict between notes ").concat(
									String.valueOf(indicesOfExceeded).replace("[", "").replace("]", "")).concat(" and ").concat(
									String.valueOf(indicesOfExceeding).replace("[", "").replace("]", "")).concat(
									": " + n).concat(String.valueOf(indicesOfExceeding).replace("[", "").replace("]", "")).concat(
									" reassigned to " + d).concat(durationsAdapted.toString()).concat(
									"\r\n");
							}
						}	    
					}      		
				}
			}
		}

		// Only for first note, which cannot have caused any conflicts
		boolean giveFirst = false; // TODO remove
		if (giveFirst) {
			if (pm == ProcessingMode.FWD && noteIndex == 0) {
				List<Double> groundTruthVoiceLabel = groundTruthTranscription.getVoiceLabels().get(noteIndex);
				List<Integer> groundTruthVoices = DataConverter.convertIntoListOfVoices(groundTruthVoiceLabel);
				if (!predictedVoices.equals(groundTruthVoices)) {
					predictedVoices = groundTruthVoices;
					double[] correctedPredictedVoices = new double[groundTruthVoiceLabel.size()];
					for (int i = 0; i < groundTruthVoiceLabel.size(); i++) {
						correctedPredictedVoices[i] = groundTruthVoiceLabel.get(i);
					} 
					allNetworkOutputs.set(noteIndex, correctedPredictedVoices); // For EvaluationManager.getActualAndPredictedVoicesDetails()
				}
			}
		}

		// 3. predictedVoices (and, if applicable, predictedDurationLabel and allPredictedDurations) now have their 
		// final value: add to lists
		allPredictedVoices.add(predictedVoices);
		allVoiceLabels.set(noteIndex, DataConverter.convertIntoVoiceLabel(predictedVoices));
//		if (isTablatureCase && isNewModel && modelDuration) {//2016
		if (isTablatureCase /*&& im == Implementation.PHD*/ && modelDuration) {
			if (predictedVoices.size() == 1) {
				allVoicesCoDNotes.set(noteIndex, null); // TODO actually not necessary
			}
			else {
				// TODO Currently, in the case of a CoD only one duration is predicted. In this case, the lower 
				// CoDnote (i.e., the one in the lower voice that comes first in the NoteSequence) must be placed
				// at element 0 of voicesCoDNotes (see JavaDoc above Transcription.handleCoDnotes())
				List<Integer> sortedAndReversed = new ArrayList<Integer>(predictedVoices);
				// Sort and reverse, so that the lowest voice comes first
				Collections.sort(sortedAndReversed);
				Collections.reverse(sortedAndReversed);
				Integer[] voicesCoDNotes = new Integer[]{sortedAndReversed.get(0), sortedAndReversed.get(1)};
				allVoicesCoDNotes.set(noteIndex, voicesCoDNotes);
			}
//			allDurationLabels.set(noteIndex, predictedDurationLabel); OUD
			// In the unidir case always; in the bidir case only when modelling duration again
			if (dc == DecisionContext.UNIDIR || dc == DecisionContext.BIDIR && modelDurationAgain) {
				allPredictedDurations.add(DataConverter.convertIntoDuration(predictedDurationLabel));
				allDurationLabels.set(noteIndex, predictedDurationLabel); // NIEUW
			}
		}
	}


//  /**
//   * Resolves any conflicts between the predicted voice(s) (or duration) for the note at noteIndex and the predicted 
//   * voices (or duration) for any preceding notes. These are conflicts between the note at noteIndex and
//   *   in the tablature case when not modelling duration
//   *     any lower notes in the same chord
//   *   in the tablature case when modelling duration and in the non-tablature case
//   *     any sustained previous notes and any lower notes in the same chord
//   *    
//   * There are five types of conflicts, arising either when any of the preceding notes has
//   * (i)   a first predicted voice that is equal to the first predicted voice for the note at noteIndex;
//   * (ii)  a second predicted voice that is equal to the first predicted voice for the note at noteIndex 
//   *       (tablature case only);
//   * (iii) a first or second predicted voice that is equal to the second predicted voice for the note at noteIndex,
//   *       if a CoD is predicted for the note at noteIndex (tablature case only);
//   * or when
//   * (iv)  the predicted duration for the note at noteIndex gives an offset time that exceeds the onset time of the
//   *       closest next note in its predicted voices (tablature case using the bwd model only); 
//   * (v)   the note at noteIndex is the last note in the chord, and in any of the voices there are notes x whose offset
//   *       time exceeds the onset time of the notes y in the next chord such that x + y > the number of voices in the
//   *       piece (tablature case using the fwd model and modelling duration only).
//   * (vi)  the duration for the note at noteIndex gives an offset time that exceeds the onset time of the closest
//   *       next note in its predicted voice (non-tablature case only);     
//   *       
//   * The conflicts are resolved in the following sequence: 
//   * (1) type (i) and (ii) conflicts with any sustained notes (if applicable); 
//   * (2) type (i) and (ii) conflicts with any lower notes in the same chord;
//   * (3) type (iii) conflicts;
//   * (4) type (iv) conflicts;
//   * (5) type (v) conflicts;
//   * (6) type (vi) conflicts;
//   *  
//   * This method does the following:
//   * (1) Determines the initially predicted voice(s) and duration label
//   * (2) If only one note per voice is allowed: checks for conflicts, and, for every conflict encountered: 
//   *     1. adds the index of the note to which the adaptations apply to conflictIndices
//   *     2. makes the following adaptations: 
//   *        a. in case of a type (i) or (iii) conflict: adapts the network output and resets the corresponding 
//   *           element in allNetworkOutputsAdapted; then redetermines the predicted voice(s); 
//   *        b. in case of a type (ii) conflict: removes the Note from newTranscription, redetermines the predicted 
//   *           voice(s), and resets the corresponding element in allPredictedVoices, allVoiceLabels, and allVoicesCoDNotes
//   *        c. in case of a type (iv) conflict: redetermines the predicted duration of the note to its maximum duration
//   *        d. in case of a type (v) conflict: redetermines the predicted durations of the notes to their maximum 
//   *           duration, and, for all Notes that are not the note at noteIndex, adapts them in newTranscription and 
//   *           resets the corresponding elements in allDurationLabels, allPredictedDurations, and allNotes
//   *        e. in case of a type (vi) conflict:       
//   *     In the tablature case when modelling duration, in the case of type (i) and (ii) conflicts with sustained 
//   *     notes (which occur only when using the fwd model), instead of the above, the duration of the sustained note
//   *     is adapted to its maximum duration in newTranscription, and the corresponding element in allPredictedDurations,
//   *     allDurationLabels, and allNotes is reset. 
//   * (3) Adds to
//   *     1. allPredictedVoices: the (redetermined) predicted voice(s)
//   *     2. allVoiceLabels: the voice label that goes with the (redetermined) predicted voice(s)
//   *     and, in the tablature case when modelling duration, to
//   *     3. allVoicesCoDNotes: in the case of a CoD, the (redetermined) predicted voices for the CoDnote; in the 
//   *        case of no CoD, <code>null</code>) 
//   *     4. allDurationLabels: the (redetermined) predicted duration label 
//   *      
//   * @param trainingSettingsAndParameters 
//   * @param copyOfNetworkOutputCurrentNote
//   * @param noteIndex 
//   */
//  // TESTED for both tablature- (non-dur and dur) and non-tablature case; for both fwd and bwd model
//  void resolveConflictsOLD(Map<String, Double> trainingSettingsAndParameters, double[] copyOfNetworkOutputCurrentNote,
//  	int noteIndex) {
//    	
//    // If the method is called for the first time: initialise conflictIndices 
//    if (conflictIndices == null) {
//    	conflictIndices = new ArrayList<List<Integer>>();
//    	conflictIndices.add(new ArrayList<Integer>());
//    	conflictIndices.add(new ArrayList<Integer>());
//    }
//      
//    // Determine noteIndexBwd, which is needed when using the bwd model for the lists in bwd order. When using the 
//    // fwd model, it will always be the same as noteIndex
//    int noteIndexBwd = noteIndex;
//    if (modelBackward) {
//    	for (int i = 0; i < backwardsMapping.size(); i++) {
//    		if (backwardsMapping.get(i) == noteIndex) {
//    			noteIndexBwd = i;
//    			break;
//    		}
//    	}
//    }
//        
//   	// 1. Get the predicted voices and the predicted durationLabel
////    double allowCoD = trainingSettingsAndParameters.get(TrainingManager.ALLOW_COD);
//    boolean allowCoD = AuxiliaryTool.toBoolean(trainingSettingsAndParameters.get(ExperimentRunner.ALLOW_COD).intValue());
//    double deviationThreshold = trainingSettingsAndParameters.get(ExperimentRunner.DEVIATION_THRESHOLD);
//    List<Integer> predictedVoices = 
//   		outputEvaluator.interpretNetworkOutput(copyOfNetworkOutputCurrentNote, allowCoD, deviationThreshold).get(0);
//    int firstPredictedVoice = predictedVoices.get(0);
//    List<Double> predictedDurationLabel = null;
//    Rational predictedDuration = null;
////    if (isTablatureCase && isNewModel && modelDuration) {//2016
//    if (isTablatureCase && ExperimentRunner.imp == ExperimentRunner.Implementation.DISS 
//    	&& modelDuration) { 	
//    	List<Integer> predictedDurationCurrentNote = 
//    		outputEvaluator.interpretNetworkOutput(copyOfNetworkOutputCurrentNote, allowCoD, deviationThreshold).get(1);
//    	predictedDurationLabel = dataConverter.convertIntoDurationLabel(predictedDurationCurrentNote);
//    	predictedDuration = dataConverter.convertIntoDuration(predictedDurationLabel)[0];
//    	predictedDuration.reduce();
//   	}
//    
//  	// 2. If only one Note per voice is allowed at a given metric time: resolve any conflicts
//    boolean allowNonMonophonic = false;
//    if (allowNonMonophonic == false) {
//  		// 0. Get the pitch, the metric position, the index in the chord, and the metric time of the note at noteIndex
//  		String metPos = AuxiliaryTool.getMetricPositionAsString(allMetricPositions.get(noteIndex));
//  		Rational metricTime = null;
//  		Rational metricDuration = null;
//  		int indexInChord = -1;
//  
//      // a. In the tablature case
//      if (isTablatureCase) { 
//      	indexInChord = basicTabSymbolProperties[noteIndex][Tablature.NOTE_SEQ_NUM];
//      	metricTime = new Rational(basicTabSymbolProperties[noteIndex][Tablature.ONSET_TIME], 
//    	  	Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
//      }
//      // b. In the non-tablature case
//      else {
//      	indexInChord = basicNoteProperties[noteIndex][Transcription.NOTE_SEQ_NUM];
//      	metricTime = new Rational(basicNoteProperties[noteIndex][Transcription.ONSET_TIME_NUMER],
//      		basicNoteProperties[noteIndex][Transcription.ONSET_TIME_DENOM]);
//      	metricDuration = new Rational(basicNoteProperties[noteIndex][Transcription.DURATION_NUMER],
//        	basicNoteProperties[noteIndex][Transcription.DURATION_DENOM]);
//      }	
//      int lowestNoteIndex = noteIndex - indexInChord;
//        
//  	  // 1. List the indices of any sustained previous and any lower chord notes and add the lists to listsOfIndices
//   		// NB: indicesOfSustainedPreviousNotes can only contain elements (a) in the tablature case when using the fwd 
//      // model case and when modelling duration and (b) in the non-tablature case; indicesOfLowerNotes can always 
//      // contain elements but only does so if the note at noteIndex is not the first note in the chord
//      List<List<Integer>> listsOfIndices = new ArrayList<List<Integer>>();
//  		// a. Sustained previous notes exists only (1) in the tablature case when modelling duration using the fwd model 
//      // and (2) in the non-tablature case
//  		List<Integer> indicesOfSustainedPreviousNotes = new ArrayList<Integer>();
//  		if ((isTablatureCase && modelDuration && !modelBackward) || !isTablatureCase && !modelBackward) {
//  		  for (int i = 0; i < lowestNoteIndex; i ++) {
//  		    // The previous note may be a CoD; therefore, previousNote must be a List
//  		  	// NB: In the non-tablature case, where there are no CoDs, previousNote will always contain only one element
//  		  	List<Note> previousNote = allNotes.get(i);
//  		  	Rational metricTimePrevious = previousNote.get(0).getMetricTime(); 
//  		  	Rational durationPrevious = Rational.ZERO;
//  		  	for (Note n : previousNote) {
//  		  	  if (n.getMetricDuration().isGreater(durationPrevious)) {
//  		  	    durationPrevious = n.getMetricDuration();
//  		  	  }
//  		  	}
//  		  	Rational offsetTimePrevious = metricTimePrevious.add(durationPrevious);
//  		  	if (offsetTimePrevious.isGreater(metricTime)) {
//  		  		indicesOfSustainedPreviousNotes.add(i);
//  		  	}
//  		  }
//  		}
//  		listsOfIndices.add(indicesOfSustainedPreviousNotes);
////  	  }
//  		// Lower chord notes
//   		List<Integer> indicesOfLowerChordNotes = new ArrayList<Integer>(); 
//   		for (int i = lowestNoteIndex; i < noteIndex; i++) {
//   			indicesOfLowerChordNotes.add(i);
//   		}	
//   		listsOfIndices.add(indicesOfLowerChordNotes);
//   		  
//     	
//   		// 2. Resolve any type (i) and (ii) conflicts. Type (i) conflicts apply to both tablature and non-tablature
//   		// case; type (ii) conflicts to tablature case only
//   		for (int listNum = 0; listNum < listsOfIndices.size(); listNum++) {
//   			
////      for (List<Integer> currentListOfIndices : listsOfIndices) {
//   			// listsOfIndices will always have two elements; if listNum == 0, currentListOfIndices will be 
//   			// indicesOfSustainedPreviousNotes; if listNum == 1, it will be indicesOfLowerChordNotes
//      	List<Integer> currentListOfIndices = listsOfIndices.get(listNum);
//      	
//      	// 1. Resolve any type (i) conflicts (conflicts between the first predicted voice for the note at noteIndex 
//      	// and the first predicted voices for any previous notes) 
//  		  boolean checkSustainedNotesAgain = false;
//      	for (int i = 0; i < currentListOfIndices.size(); i++) {
//          int noteIndexPrevious = currentListOfIndices.get(i);
//          // Determine noteIndexPreviousBwd, which is needed when using the bwd model for the lists in bwd order. 
//          // When using the fwd model, it will always be the same as noteIndexPrevious
//          int noteIndexPreviousBwd = noteIndexPrevious;
//          if (modelBackward) {
//          	for (int j = 0; j < backwardsMapping.size(); j++) {
//          		if (backwardsMapping.get(j) == noteIndexPrevious) {
//          			noteIndexPreviousBwd = j;
//          			break;
//          		}
//          	}
//          }
//     	  	List<Integer> predictedVoicesPrevious = allPredictedVoices.get(noteIndexPreviousBwd);
//     	  	int firstPredictedVoicePrevious = predictedVoicesPrevious.get(0);
//     	  	
//     	    // If firstPredictedVoicePrevious is the same as firstPredictedVoice 
//       		if (firstPredictedVoicePrevious == firstPredictedVoice) {
//       			// 0. Get the metric position and the index in the chord of the previous note
//       			String metPosPrevious = AuxiliaryTool.getMetricPositionAsString(allMetricPositions.get(noteIndexPrevious)); 
//       			int indexInChordPrevious = -1;
//       			if (isTablatureCase) {
//          	  indexInChordPrevious = basicTabSymbolProperties[noteIndexPrevious][Tablature.NOTE_SEQ_NUM]; 
//            }
//            else {
//            	indexInChordPrevious = basicNoteProperties[noteIndexPrevious][Transcription.NOTE_SEQ_NUM]; 
//            }	
//       			
//            // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//       			// a. For sustained notes in the tablature case (which only exist when modelling duration (and using 
//       			// the fwd model))
//       			if (listNum == 0 && isTablatureCase && modelDuration) {
//       				boolean sustained = true;
////       			if (modelDuration && currentListOfIndices.equals(indicesOfSustainedPreviousNotes)) {
//       				
//       		    // DIT NU
////       				List<Double> predictedDurationLabelPrevious = allDurationLabels.get(noteIndexPrevious);
////       				Rational predictedDurationPrevious = new Rational((predictedDurationLabelPrevious.indexOf(1.0) + 1),
////       					Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
////       				predictedDurationPrevious.reduce();
//       				//
//       				List<Double> predictedDurationLabelPrevious = allDurationLabels.get(noteIndexPrevious);
//       				
////       				System.out.println(allPredictedDurations);
//       				Rational predictedDurationPrevious = allPredictedDurations.get(noteIndexPreviousBwd)[0];
//         			predictedDurationPrevious.reduce();
//       				
//       		    // 1. Add index to conflictIndices
//     	  	  	if (!conflictIndices.get(1).contains(noteIndexPreviousBwd)) {
//         			  conflictIndices.get(1).add(noteIndexPreviousBwd);
//         			}  	  	  	
//       		    
//       				// 2. Reset the Note's duration in Transcription
//       		    // Determine the maximum duration and the pitch of the note at noteIndexPrevious. Because this note is 
//       				// followed by at least one note (the note at noteIndex), maxDurPrevious can never be null
//       				Rational metricTimePrevious = new Rational(basicTabSymbolProperties[noteIndexPrevious][Tablature.ONSET_TIME],	
//          	    Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
//       				Rational maxDurPrevious = 
//       					getMaximumDuration(metricTimePrevious, metricTime, newTranscription, predictedVoicesPrevious);
//       				
////       				List<Rational> metricTimesNext = getMaximumDuration(metricTimePrevious, newTranscription, 
////       					predictedVoicesPrevious);
////       				// Because the note at noteIndexPrevious is followed by at least one note (the note at noteIndex), 
////       				// metricTimesNext has always at least one element that is not null. The maximum duration of the note
////       				// at noteIndexPrevious is determined by the smallest value in metricTimesNext
////       				Rational closest = null;
////       				if (!metricTimesNext.contains(null)) {
////       					closest = Collections.min(metricTimesNext);
////       				}
////       				else {
////     	 			    int indexOfNull = metricTimesNext.indexOf(null);
////     	 			    closest = metricTimesNext.get(Math.abs(indexOfNull - 1));
////     	 			  } 
////       				Rational maxDurPrevious = closest.sub(metricTime);
//       				
//       				int pitchPrevious = basicTabSymbolProperties[noteIndexPrevious][Tablature.PITCH];
//     	  	    // Reset the note at noteIndexPrevious in the Transcription. The for-loop is needed for when the note 
//       				// at noteIndexPrevious is a CoD, in which case the duration of its second predicted voice must be
//       				// adapted as well 
//       				
//       				ScoreNote adaptedScoreNotePrevious = 
//     	  	    	new ScoreNote(new ScorePitch(pitchPrevious), metricTimePrevious, maxDurPrevious);
//     	  	    for (int predictedVoicePrevious : predictedVoicesPrevious) {
//       	  	    NotationVoice nv = newTranscription.getPiece().getScore().get(predictedVoicePrevious).get(0);
//       	  	    for (NotationChord nc : nv) {
//      	     	  	if (nc.getMetricTime().equals(metricTimePrevious)) {
//      	     	  		nc.get(0).setScoreNote(adaptedScoreNotePrevious);
//      	     	  	}
//      	     	  }
//     	  	    }
//     	  	    
//     	        // 3. Record conflict and add to conflictsRecord
////   	   	      String conflictText = getConflictText("(i)", noteIndexBwd, metPos, indexInChord, noteIndexPreviousBwd,
////   	   	      	metPosPrevious, indexInChordPrevious, predictedVoices, predictedVoicesPrevious);
//   	   	      conflictsRecord = conflictsRecord.concat(getConflictTextOLD("(i)", noteIndexBwd, metPos, indexInChord, 
//   	   	      	noteIndexPreviousBwd,	metPosPrevious, indexInChordPrevious, predictedVoices, predictedVoicesPrevious,
//   	   	      	sustained));
//   	   	      conflictsRecord = conflictsRecord.concat("  --> predicted duration " + predictedDurationPrevious + 
//   	   	      	" for note " + noteIndexPreviousBwd + " adapted; ");
////   	   	      conflictsRecord = conflictsRecord.concat(
////         				"noteIndex = " + noteIndexBwd + "\r\n" +
////        			  "  type (i) conflict between note " + noteIndexBwd + " (bar: " + metPos + ", index in chord: " + indexInChord +
////        			  ") and note " + noteIndexPreviousBwd	+ " (bar: " + metPosPrevious + ", index in chord: " + indexInChordPrevious + "):\r\n" +
////        				"  the first predicted voice for note " + noteIndexBwd + " (predicted voices: " + predictedVoices + 
////        				") is the same as the first predicted voice for note " + noteIndexPreviousBwd +	" (predicted voices: " +
////        				predictedVoicesPrevious + ") \r\n" + 
////         			  "  --> assignment of duration " + predictedDurationPrevious + " made undone for note " + noteIndexPreviousBwd + "; "  
////         			); 
//   	   	    
//   	   	      // 4. Redetermine predictedDurationLabelPrevious and reset the corresponding elements in the Lists
////   	   	      predictedDurationLabelPrevious = Transcription.createDurationLabel(minDurPrevious);
//   	   	      int maxDurAsInt = 
//   	   	      	maxDurPrevious.getNumer() *	(Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom() / maxDurPrevious.getDenom());
//   	   	      predictedDurationLabelPrevious = Transcription.createDurationLabel(maxDurAsInt);
//   	   	      allDurationLabels.set(noteIndexPrevious, predictedDurationLabelPrevious);
//   	   	      predictedDurationPrevious = maxDurPrevious;
//   	   	      predictedDurationPrevious.reduce();
//     	  	  	allPredictedDurations.set(noteIndexPreviousBwd, new Rational[]{predictedDurationPrevious});
//     	  	  	List<Note> previousNote = allNotes.get(noteIndexPreviousBwd);
//     	  	    for (Note n : previousNote) {
//     	  	    	n.setScoreNote(adaptedScoreNotePrevious);
//     	  	    }
//     	  	    allNotes.set(noteIndexPreviousBwd, previousNote);
//     	  	    // Add new predicted duration to conflictsRecords
////           		conflictsRecord = conflictsRecord.concat("new predicted duration for note " + noteIndexPreviousBwd + ": " + 
////           		  predictedDurationPrevious + "\r\n" + "\r\n");
//           		conflictsRecord = 
//           			conflictsRecord.concat("new predicted duration: " + predictedDurationPrevious + "\r\n" + "\r\n");
//       			}
//       	    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//       			
//       			// b. For sustained notes in the non-tablature case and for lower chord notes in both tablature- and 
//       			// non-tablature case
//       			else if ((listNum == 0 && !isTablatureCase) || listNum == 1) {
//       		    boolean sustained = true;
//       		    if (listNum == 1) {
//       		    	sustained = false;
//       		    }
//       		    
//       				// 1. Add index to conflictIndices
//         			if (!conflictIndices.get(0).contains(noteIndexBwd)) {
//         			  conflictIndices.get(0).add(noteIndexBwd);
//         			}
//         			// 2. Set to 0.0 in copyOfNetworkOutputCurrentNote and reset allNetworkOutputsAdapted
//         			copyOfNetworkOutputCurrentNote[firstPredictedVoicePrevious] = 0.0;
//         			allNetworkOutputsAdapted.set(noteIndexBwd, copyOfNetworkOutputCurrentNote);	
//         			// 3. Record conflict and add to conflictsRecord 
////         			String conflictText = getConflictText("(i)", noteIndexBwd, metPos, indexInChord, noteIndexPreviousBwd, 
////         				metPosPrevious, indexInChordPrevious, predictedVoices, predictedVoicesPrevious);
//         			conflictsRecord = conflictsRecord.concat(getConflictTextOLD("(i)", noteIndexBwd, metPos, indexInChord, 
//         				noteIndexPreviousBwd,	metPosPrevious, indexInChordPrevious, predictedVoices, predictedVoicesPrevious,
//         				sustained));
//         			conflictsRecord = conflictsRecord.concat("  --> voice " + firstPredictedVoice + 
//         				" made unavailable for note " + noteIndexBwd + "; ");     			
////         			conflictsRecord = conflictsRecord.concat(
////         				"noteIndex = " + noteIndexBwd + "\r\n" +
////        			  "  type (i) conflict between note " + noteIndexBwd + " (bar: " + metPos + ", index in chord: " + indexInChord +
////        			  ") and note " + noteIndexPreviousBwd	+ " (bar: " + metPosPrevious + ", index in chord: " + indexInChordPrevious + "):\r\n" +
////        				"  the first predicted voice for note " + noteIndexBwd + " (predicted voices: " + predictedVoices + 
////        				") is the same as the first predicted voice for note " + noteIndexPreviousBwd + " (predicted voices: " +
////        				predictedVoicesPrevious + ") \r\n" + 
////         			  "  --> voice " + firstPredictedVoice + " made unavailable for note " + noteIndexBwd + "; "
////         			);       									
//         	    // 4. Redetermine predictedVoices and do another iteration over all previous notes to see whether the 
//         			// new first predicted voice is the same as any of the first predicted voices for the previous notes. 
//         			// If so, resolve and repeat until all type (i) conflicts are resolved
//         			// --> i.e., adapt the predicted label for the note at noteIndex as many times as is needed for its 
//         			// highest activation value to represent a voice not yet taken by a previous note
//           		predictedVoices = 
//           		  outputEvaluator.interpretNetworkOutput(copyOfNetworkOutputCurrentNote, allowCoD, deviationThreshold).get(0);
//           		firstPredictedVoice = predictedVoices.get(0);
//           			
//           		// Add new predicted voices to conflictsRecords
////           		conflictsRecord = conflictsRecord.concat("new predicted voices for note " + noteIndexBwd + ": " + 
////           		  predictedVoices + "\r\n" + "\r\n");
//           		conflictsRecord = conflictsRecord.concat("new predicted voices: " +	predictedVoices + "\r\n" + "\r\n");
//           		
//           		i = -1;
//           		// In the tablature case when modelling duration: set checkSustainedNotesAgain to true 
//           		if (isTablatureCase && modelDuration) {
//           			checkSustainedNotesAgain = true;
////           			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@" + noteIndex);
////           			System.exit(0);
//           		}
//       			}
//       		}  
//       	}
//  		  // 2. Resolve any type (ii) conflicts (conflicts between the first predicted voice for the note at noteIndex 
//  		  // and any second predicted voices for any previous notes). Type (ii) conflicts can only occur if a CoD was 
//  		  // predicted for the previous note.
//      	// Do only if checkSustainedNotesAgain is false, i.e., if no type (i) conflicts with any lower chordNotes have  
//      	// been found and predictedVoices has not been changed (meaning that firstPredictedVoice now has its final 
//      	// value)
//     	  if (!checkSustainedNotesAgain) {
//    		  if (isTablatureCase) {
//      		  for (int i = 0; i < currentListOfIndices.size(); i++) {
//      		  	int noteIndexPrevious = currentListOfIndices.get(i);
//         	    // Determine noteIndexPreviousBwd, which is needed when using the bwd model for the lists in bwd order. 
//              // When using the fwd model, it will always be the same as noteIndexPrevious
//              int noteIndexPreviousBwd = noteIndexPrevious;
//              if (modelBackward) {
//              	for (int j = 0; j < backwardsMapping.size(); j++) {
//              		if (backwardsMapping.get(j) == noteIndexPrevious) {
//              			noteIndexPreviousBwd = j;
//              			break;
//              		}
//              	}
//              }
//         	  	List<Integer> predictedVoicesPrevious = allPredictedVoices.get(noteIndexPreviousBwd);
//         	  	if (predictedVoicesPrevious.size() == 2) {
//         	  	  int firstPredictedVoicePrevious = predictedVoicesPrevious.get(0);
//         	  		int secondPredictedVoicePrevious = predictedVoicesPrevious.get(1);
//            	  // If secondPredictedVoicePrevious is the same as firstPredictedVoice
//         	  	  if (secondPredictedVoicePrevious == firstPredictedVoice) {
//         	  	    // 0. Get the metric position and the index in the chord of the previous note
//         	  	  	String metPosPrevious = AuxiliaryTool.getMetricPositionAsString(allMetricPositions.get(noteIndexPrevious));
//             			int indexInChordPrevious = basicTabSymbolProperties[noteIndexPrevious][Tablature.NOTE_SEQ_NUM];  	  	
//         	  	  	
//             	    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//             	    // a. For sustained notes (which only exist when modelling duration using the fwd model)
//             			if (listNum == 0 && modelDuration && !modelBackward) {
//             				boolean sustained = true;
//  //           			if (modelDuration && currentListOfIndices.equals(indicesOfSustainedPreviousNotes)) {
//             				
//             				// DIT NU
////             				List<Double> predictedDurationLabelPrevious = allDurationLabels.get(noteIndexPrevious);
////             				Rational predictedDurationPrevious = new Rational((predictedDurationLabelPrevious.indexOf(1.0) + 1),
////             					Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
////             				predictedDurationPrevious.reduce();
//             				//
//             				List<Double> predictedDurationLabelPrevious = allDurationLabels.get(noteIndexPrevious);
//             				Rational predictedDurationPrevious = allPredictedDurations.get(noteIndexPreviousBwd)[0];
//             				predictedDurationPrevious.reduce();
//             				
//             		    // 1. Add index to conflictIndices
//           	  	  	if (!conflictIndices.get(1).contains(noteIndexPreviousBwd)) {
//               			  conflictIndices.get(1).add(noteIndexPreviousBwd);
//               			}  	  	  	
//             				
//             		    // 2. Reset the Note's duration in Transcription
//             		    // Determine the maximum duration and the pitch of the note at noteIndexPrevious. Because this note 
//             				// is followed by at least one note (the note at noteIndex), maxDurPrevious can never be null
//             				Rational metricTimePrevious = new Rational(basicTabSymbolProperties[noteIndexPrevious][Tablature.ONSET_TIME],	
//                	    Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
//             				Rational maxDurPrevious = 
//             					getMaximumDuration(metricTimePrevious, metricTime, newTranscription, predictedVoicesPrevious);
//             				
//  //           				// 2. Reset the Note's duration in Transcription
//  //         	  	    // Determine the maximum duration of the note at noteIndexPrevious
//  //         				  Rational metricTimePrevious = new Rational(basicTabSymbolProperties[noteIndexPrevious][Tablature.ONSET_TIME],	
//  //          	     	  Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
//  //         				  List<Rational> metricTimesNext = getMaximumDuration(metricTimePrevious, newTranscription, 
//  //         					  predictedVoicesPrevious);
//  //         				  // Because the note at noteIndexPrevious is followed by at least one note (the note at noteIndex), 
//  //         				  // metricTimesNext has always at least one element that is not null. The maximum duration of the note
//  //         				  // at noteIndexPrevious is determined by the smallest value in metricTimesNext
//  //         				  Rational closest = null;
//  //         				  if (!metricTimesNext.contains(null)) {
//  //         				  	closest = Collections.min(metricTimesNext);
//  //         				  }
//  //         				  else {
//  //       	 			      int indexOfNull = metricTimesNext.indexOf(null);
//  //       	 			      closest = metricTimesNext.get(Math.abs(indexOfNull - 1));
//  //       	 			    } 
//  //         				  Rational maxDurPrevious = closest.sub(metricTime);
//           	  	    
//           				  int pitchPrevious = basicTabSymbolProperties[noteIndexPrevious][Tablature.PITCH];
//           	  	    // Reset the note at noteIndexPrevious in the Transcription. The for-loop is needed because the 
//           	  	    // duration of its first predicted voice must be adapted as well 
//           	  	    ScoreNote adaptedScoreNotePrevious = 
//            	  	    new ScoreNote(new ScorePitch(pitchPrevious), metricTimePrevious, maxDurPrevious);
//           	  	    for (int predictedVoicePrevious : predictedVoicesPrevious) {
//             	  	    NotationVoice nv = newTranscription.getPiece().getScore().get(predictedVoicePrevious).get(0);
//             	  	    for (NotationChord nc : nv) {
//            	     	  	if (nc.getMetricTime().equals(metricTimePrevious)) {
//            	     	  		nc.get(0).setScoreNote(adaptedScoreNotePrevious);
//            	     	  	}
//            	     	  }
//           	  	    }
//           	  	    
//           	        // 3. Record conflict and add to conflictsRecord
//  //         	  	    String conflictText = getConflictText("(ii)", noteIndexBwd, metPos, indexInChord, noteIndexPreviousBwd, 
//  //              			metPosPrevious, indexInChordPrevious, predictedVoices, predictedVoicesPrevious);
//                		conflictsRecord = conflictsRecord.concat(getConflictTextOLD("(ii)", noteIndexBwd, metPos, indexInChord,
//                			noteIndexPreviousBwd,	metPosPrevious, indexInChordPrevious, predictedVoices, predictedVoicesPrevious,
//                			sustained));
//  //              		conflictsRecord = conflictsRecord.concat("  --> assignment of duration " + predictedDurationPrevious + 
//  //              			" made undone for note " + noteIndexPreviousBwd + "; ");  
//                		conflictsRecord = conflictsRecord.concat("  --> predicted duration " + predictedDurationPrevious +
//                			" for note " +	noteIndexPreviousBwd + " adapted; ");
//  //         	  	    conflictsRecord = conflictsRecord.concat(
//  //             				"noteIndex = " + noteIndexBwd + "\r\n" +
//  //            			  "  type (ii) conflict between note " + noteIndexBwd + " (bar: " + metPos + ", index in chord: " + indexInChord +
//  //            			  ") and note " + noteIndexPreviousBwd	+ " (bar: " + metPosPrevious + ", index in chord: " + indexInChordPrevious + "):\r\n" +
//  //            				"  the first predicted voice for note " + noteIndexBwd + " (predicted voices: " + predictedVoices + 
//  //            				") is the same as the second predicted voice for note " + noteIndexPreviousBwd +	" (predicted voices: " +
//  //            				predictedVoicesPrevious + ") \r\n" + 
//  //             			  "  --> assignment of duration " + predictedDurationPrevious + " made undone for note " + noteIndexPreviousBwd + "; "
//  //             			);
//                		
//         	   	      // 4. Redetermine predictedDurationLabelPrevious and reset the corresponding elements in the Lists
//  //       	   	      predictedDurationLabelPrevious = Transcription.createDurationLabel(minDurPrevious);
//                		int maxDurAsInt = 
//           	   	      maxDurPrevious.getNumer() *	(Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom() / maxDurPrevious.getDenom());
//           	   	    predictedDurationLabelPrevious = Transcription.createDurationLabel(maxDurAsInt);
//           	   	    allDurationLabels.set(noteIndexPrevious, predictedDurationLabelPrevious);
//           	   	    predictedDurationPrevious = maxDurPrevious;	
//         	   	      predictedDurationPrevious.reduce();
//           	  	  	allPredictedDurations.set(noteIndexPreviousBwd, new Rational[]{predictedDurationPrevious});
//           	  	  	List<Note> previousNote = allNotes.get(noteIndexPreviousBwd);
//           	  	    for (Note n : previousNote) {
//           	  	    	n.setScoreNote(adaptedScoreNotePrevious);
//           	  	    }
//           	  	    allNotes.set(noteIndexPreviousBwd, previousNote);
//           	  	    // Add new predicted duration to conflictsRecords
////                 		conflictsRecord = conflictsRecord.concat("new predicted duration for note " + noteIndexPreviousBwd + ": " + 
////                 		  predictedDurationPrevious + "\r\n" + "\r\n");
//                 		conflictsRecord = 
//                 			conflictsRecord.concat("new predicted duration: " +	predictedDurationPrevious + "\r\n" + "\r\n");
//             			}
//             	    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//             		  // b. For lower chord notes 
//               		else if (listNum == 1) {
//               			boolean sustained = false;
//               			// 1. Add index to conflictIndices
//           	  	  	if (!conflictIndices.get(0).contains(noteIndexPreviousBwd)) {
//               			  conflictIndices.get(0).add(noteIndexPreviousBwd);
//               			}  	  	  	
//           	  	  	// 2. Remove the Note from the Transcription 
//           	  	  	int pitchPrevious = basicTabSymbolProperties[noteIndexPrevious][Tablature.PITCH];
//           	  	  	Rational metricTimePrevious = new Rational(basicTabSymbolProperties[noteIndexPrevious][Tablature.ONSET_TIME], 
//           	  	  		Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
//            	  	  newTranscription.removeNote(pitchPrevious, secondPredictedVoicePrevious, metricTimePrevious);      
//            	  	  // 3. Record conflict and add to conflictsRecord
//  //          	  	  String conflictText = getConflictText("(ii)", noteIndexBwd, metPos, indexInChord, noteIndexPreviousBwd, 
//  //                			metPosPrevious, indexInChordPrevious, predictedVoices, predictedVoicesPrevious);
//                  	conflictsRecord = conflictsRecord.concat(getConflictTextOLD("(ii)", noteIndexBwd, metPos, indexInChord,
//                  		noteIndexPreviousBwd,	metPosPrevious, indexInChordPrevious, predictedVoices, predictedVoicesPrevious,
//                  		sustained));
//                  	conflictsRecord = conflictsRecord.concat("  --> assignment to voice " + secondPredictedVoicePrevious + 
//                  		" made undone for note " + noteIndexPreviousBwd + "; ");          	  	  
//  //          	  	  conflictsRecord = conflictsRecord.concat(
//  //             				"noteIndex = " + noteIndexBwd + "\r\n" +
//  //            			  "  type (ii) conflict between note " + noteIndexBwd + " (bar: " + metPos + ", index in chord: " + indexInChord +
//  //            			  ") and note " + noteIndexPreviousBwd	+ " (bar: " + metPosPrevious + ", index in chord: " + indexInChordPrevious + "):\r\n" +
//  //            				"  the first predicted voice for note " + noteIndexBwd + " (predicted voices: " + predictedVoices + 
//  //            				") is the same as the second predicted voice for note " + noteIndexPreviousBwd +
//  //            				" (predicted voices: " + predictedVoicesPrevious + ") \r\n" + 
//  //             			  "  --> assignment to voice " + secondPredictedVoicePrevious + " made undone for note " + noteIndexPreviousBwd + "; "
//  //             			);  	      	  	  
//            	  	  // 4. Redetermine predictedVoicePrevious and reset the corresponding elements in the Lists
//            	  	  predictedVoicesPrevious = Arrays.asList(new Integer[]{firstPredictedVoicePrevious});
//           	  	  	allPredictedVoices.set(noteIndexPreviousBwd, predictedVoicesPrevious);
//    //     	   	    	allVoiceLabels.set(noteIndexPreviousBwd, dataConverter.convertIntoVoiceLabel(predictedVoicesPrevious)); FUK 23-6
//         	   	      allVoiceLabels.set(noteIndexPrevious, dataConverter.convertIntoVoiceLabel(predictedVoicesPrevious));
//         	   	    	
////         	   	    	if (isTablatureCase && isNewModel && modelDuration) { //2016
//         	   	  if (isTablatureCase && ExperimentRunner.imp == ExperimentRunner.Implementation.DISS 
//         	   			&& modelDuration) {
//    //     	   	    		allVoicesCoDNotes.set(noteIndexPreviousBwd, null);
//         	   	    	  allVoicesCoDNotes.set(noteIndexPrevious, null); // FUK 23-6
//         	   	    		// NB: The durationLabel need not be adapted as currently only one duration is predicted for both 
//         	   	    		// CoDNotes TODO Change?
//         	   	    	}
//         	   	      // Add new predicted voices to conflictsRecords
////                 		conflictsRecord = conflictsRecord.concat("new predicted voices for note " + noteIndexPreviousBwd + ": " + 
////                 		  predictedVoicesPrevious + "\r\n" + "\r\n");
//                 		conflictsRecord = 
//                 			conflictsRecord.concat("new predicted voices: " +	predictedVoicesPrevious + "\r\n" + "\r\n");
//             			}
//       		      }
//       		    }
//       		  }
//       	  }
//   		  }
//     	  // If checkSustainedNotesAgain is true, i.e., if type (i) conflicts with any lower chordNotes have been 
//      	// found and predictedVoices has been changed: re-check for type (i) and (ii) conflicts with sustained notes
//     	  else {
//     	  	listNum = -1;
//     	  }
//  		}
//  		
//      // 3. Resolve any type (iii), (iv), and (v) conflicts, all of which apply to the tablature case only
//  		if (isTablatureCase) {
//  		  // 1. Resolve any type (iii) conflicts (conflicts between the second predicted voice for the note at noteIndex
//        // and any first or second predicted voices for any preceding notes). Type (iii) conflicts can only occur if 
//        // a CoD was predicted for the note at noteIndex
//  			if (predictedVoices.size() == 2) { 
//     		 	int secondPredictedVoice = predictedVoices.get(1);
//     		 	// Gather the indices of all preceding notes (sustained previous and lower chord notes) in a list
//     		  List<Integer> indicesOfSustainedPreviousAndLowerChordNotes = new ArrayList<Integer>(indicesOfSustainedPreviousNotes);
//      	  indicesOfSustainedPreviousAndLowerChordNotes.addAll(indicesOfLowerChordNotes);
//     		 	for (int i = 0; i < indicesOfSustainedPreviousAndLowerChordNotes.size(); i++) {
//       	  	int noteIndexPrevious = indicesOfSustainedPreviousAndLowerChordNotes.get(i);
//       	    // Determine noteIndexPreviousBwd, which is needed when using the bwd model for the lists in bwd order. 
//            // When using the fwd model, it will always be the same as noteIndexPrevious
//            int noteIndexPreviousBwd = noteIndexPrevious;
//            if (modelBackward) {
//            	for (int j = 0; j < backwardsMapping.size(); j++) {
//            		if (backwardsMapping.get(j) == noteIndexPrevious) {
//            			noteIndexPreviousBwd = j;
//            			break;
//            		}
//            	}
//            }
//       	  	List<Integer> predictedVoicesPrevious = allPredictedVoices.get(noteIndexPreviousBwd);
//       	  	// If predictedVoicesPrevious contains secondPredictedVoice
//     		    if (predictedVoicesPrevious.contains(secondPredictedVoice)) {
//     		    	boolean sustained = true;
//     		    	if (indicesOfLowerChordNotes.contains(noteIndexPrevious)) {
//     		    		sustained = false;
//     		    	}
//     		      // 0. Get the metric position and the index in the chord of the previous note
//     		    	String metPosPrevious = AuxiliaryTool.getMetricPositionAsString(allMetricPositions.get(noteIndexPrevious));
//         			int indexInChordPrevious = basicTabSymbolProperties[noteIndexPrevious][Tablature.NOTE_SEQ_NUM];  	
//     		      // 1. Add index to conflictIndices
//     		    	if (!conflictIndices.get(0).contains(noteIndexBwd)) {
//         			  conflictIndices.get(0).add(noteIndexBwd);
//         			}
//     		    	
//     		    	// 2. Set to 0.0 in copyOfNetworkOutputCurrentNote and reset allNetworkOutputsAdapted
//   	  	 	    copyOfNetworkOutputCurrentNote[secondPredictedVoice] = 0.0;
//   	  	 	    allNetworkOutputsAdapted.set(noteIndexBwd, copyOfNetworkOutputCurrentNote);        
//   	  	 	    // 3. Record conflict and add to conflictsRecord
////   	  	 	    String conflictText = getConflictText("(iii)", noteIndexBwd, metPos, indexInChord, noteIndexPreviousBwd, 
////          			metPosPrevious, indexInChordPrevious, predictedVoices, predictedVoicesPrevious);
//          	  conflictsRecord = conflictsRecord.concat(getConflictTextOLD("(iii)", noteIndexBwd, metPos, indexInChord, 
//          	  	noteIndexPreviousBwd,	metPosPrevious, indexInChordPrevious, predictedVoices, predictedVoicesPrevious, 
//          	  	sustained));
//          	  conflictsRecord = conflictsRecord.concat("  --> voice " + secondPredictedVoice + 
//          	  	" made unavailable for note " + noteIndexBwd + "; ");    
////   	  	 	    conflictsRecord = conflictsRecord.concat(
////         				"noteIndex = " + noteIndexBwd + "\r\n" +
////        			  "  type (iii) conflict between note " + noteIndexBwd + " (bar: " + metPos + ", index in chord: " + indexInChord +
////        			  ") and note " + noteIndexPreviousBwd	+ " (bar: " + metPosPrevious + ", index in chord: " + indexInChordPrevious + "):\r\n" +
////        				"  the second predicted voice for note " + noteIndexBwd + " (predicted voices: " + predictedVoices + 
////        				") is the same as (one of) the predicted voice(s) for note " + noteIndexPreviousBwd + " (predicted voices: " +
////        				predictedVoicesPrevious + ") \r\n" + 
////         			  "  --> voice " + secondPredictedVoice + " made unavailable for note " + noteIndexBwd + "; "
////         			);	    
//              // 4. Redetermine predictedVoices and break
//   	  	 	    // NB: Using outputEvaluator.interpretNetworkOutput() here may result in a new second predicted 
//           		// voice for the note at noteIndex
//   	  	 	    predictedVoices = Arrays.asList(new Integer[]{firstPredictedVoice});
//   	  	 	    // Add new predicted voices to conflictsRecords
////           		conflictsRecord = conflictsRecord.concat("new predicted voices for note " + noteIndexBwd + ": " + 
////           		  predictedVoices + "\r\n" + "\r\n");
//           		conflictsRecord = conflictsRecord.concat("new predicted voices: " +	predictedVoices + "\r\n" + "\r\n");
//   	  	 	    break;
//     			  } 
//     		  }	
//     		}
//  		  // 2. Resolve any type (iv) conflicts (conflicts between the predicted duration for the note at noteIndex
//        // and the onset time of the next note in the predicted voices for the note at noteIndex). Type (iv) conflicts 
//  			// can only occur when using the bwd model and when modelling duration
//     	  if (modelBackward && modelDuration) {
////     	  	List<Rational> metricTimesNext = getMaximumDuration(metricTime, newTranscription, predictedVoices);
//     	  	
////     	  	// Taking into account CoDs, list the metric time and pitch of the next note in each predicted voice
////     	  	List<Rational> metricTimesNext = new ArrayList<Rational>();
////     	  	List<Integer> pitchesNext = new ArrayList<Integer>();
////     	  	boolean isNextNote = false;
////     	  	for (int i : predictedVoices) {
////     	  		NotationVoice nv = newTranscription.getPiece().getScore().get(i).get(0);
////     	  		Rational lastMetricTimeInVoice = nv.get(nv.size() - 1).get(0).getMetricTime();
////     	      // If the note at noteIndex is the last note in the voice: set lists to null and -1 
////   	  			if (metricTime.equals(lastMetricTimeInVoice)) {
////   	  				metricTimesNext.add(null);
////   	  				pitchesNext.add(-1);
//// 	  	      }
////   	  	    // If not: add the metric time and the pitch of the closest next note
////   	  			else {
////     	  		  for (NotationChord nc : nv) { 
////       	  			if (nc.getMetricTime().isGreater(metricTime)) {
////     	  	    		isNextNote = true;
////       	  				metricTimesNext.add(nc.getMetricTime());
////     	  	    		pitchesNext.add(nc.get(0).getMidiPitch());
////     	  	    		break;
////     	  	    	}
////     	  		  }
////     	  	  }
////     	  	}
//     	  	
////      		// 2. Determine the closest metric time, if any
////      		Rational closestMetricTimeNext = null;	
////       		// a. If there is only one predicted voice
////      		if (metricTimesNext.size() == 1 && metricTimesNext.get(0) != null) {
////       			closestMetricTimeNext = metricTimesNext.get(0);
////       		}
////       		// b. If there are two predicted voices
////       		else if (metricTimesNext.size() == 2) {
////       			// If the note at noteIndex is the last note in neither of the predicted voices
////       			if (!metricTimesNext.contains(null)) {
////       	 			closestMetricTimeNext = Collections.min(metricTimesNext);
////       	 		}
////       			// If the note at noteIndex is the last note in one or in both of the predicted voices
////       	 		else {
////       	 	    // In both
////       	 			if (metricTimesNext.get(0) == null && metricTimesNext.get(1) == null) {
////       	 				closestMetricTimeNext = null;
////       	 			}
////       	 			// In one
////       	 			else {
////       	 			  int indexOfNull = metricTimesNext.indexOf(null);
////       	 			  closestMetricTimeNext = metricTimesNext.get(Math.abs(indexOfNull - 1));
////       	 			}
////       	 		}
////       		}
//     	  	
//     	  	
////     	  	// Determine whether there is a next note in the predicted voice(s)
////     	  	boolean isNextNote = false;
////     	  	Rational closest = null;
////     	    if (metricTimesNext.size() == 1 && metricTimesNext.get(0) != null) {
////     	    	isNextNote = true;
////     	    	closest = metricTimesNext.get(0);
////     	    }
////     	    else if (metricTimesNext.size() == 2 && (metricTimesNext.get(0) != null) || metricTimesNext.get(1) != null ) {
////     	    	isNextNote = true;fff
////     	    }
////     	    
////     	    if (!metricTimesNext.contains(null)) {
////				  	closest = Collections.min(metricTimesNext);
////				  }
////				  else {
//// 			      int indexOfNull = metricTimesNext.indexOf(null);
//// 			      closest = metricTimesNext.get(Math.abs(indexOfNull - 1));
//// 			    }
//     	    
//     	  	// Determine the maximum duration of the note at noteIndex
//   	  	  Rational maxDur = getMaximumDuration(metricTime, null, newTranscription, predictedVoices);
//     	  	
//     	  	// If the predicted voice(s) contain notes and there is a next note in them
//   	  	  if (maxDur != null) {
////     	  	if (closestMetricTimeNext != null) {
////      		if (isNextNote) { 
//     	      // Determine the maximum allowed duration of the note at noteIndex
////     	  		Rational closestMetricTimeNext = null;
////     	  		int indexOfNull = -1;
////     	  		System.out.println(metricTimesNext);
////     	  		if (!metricTimesNext.contains(null)) {
////     	  			closestMetricTimeNext = Collections.min(metricTimesNext);
////     	  		}
////     	  		else {
////     	  			indexOfNull = metricTimesNext.indexOf(null);
////     	  			closestMetricTimeNext = metricTimesNext.get(Math.abs(indexOfNull - 1));
////     	  		}
////     	  		System.out.println(indexOfNull);
////     	  		System.out.println(closestMetricTimeNext);
//     	  		
////     	  		Rational maxDur = closestMetricTimeNext.sub(metricTime);
//       	  	
//   	  	  	// If predictedDuration exceeds maxDur
//       	  	if (predictedDuration.isGreater(maxDur)) {
//       	  		Rational closestMetricTime = metricTime.add(maxDur);
//       	  		
//       	      // 1. Add index to conflictIndices
//     		    	if (!conflictIndices.get(1).contains(noteIndexBwd)) {
//         			  conflictIndices.get(1).add(noteIndexBwd);
//         			}
//       	  		
//       	      // 2. List the index, the metric position, and the sequence number in the chord of the next note in
//       	  		// predictedVoices that is closest in onset time to the note at noteIndex. If the note at noteIndex is
//       	  		// a CoD and the next notes in both voices are equally close, both are taken into account
////       	  		List<Rational> metricTimesNext = new ArrayList<Rational>();
////       	  		List<Integer> pitchesNext = new ArrayList<Integer>();
//       	  		List<Integer> noteIndicesBwdNext = new ArrayList<Integer>();
//         	  	List<String> metPosNext = new ArrayList<String>();
//         	  	List<Integer> indicesInChordNext = new ArrayList<Integer>();
//         	  	List<Integer> predictedVoicesNext = new ArrayList<Integer>();
//       	  		for (int i = 0; i < predictedVoices.size(); i++) {
//       	  			int currentPredictedVoice = predictedVoices.get(i);
//         	  		NotationVoice nv = newTranscription.getPiece().getScore().get(currentPredictedVoice).get(0);         	  		
////         	  		Rational lastMetricTimeInVoice = nv.get(nv.size() - 1).get(0).getMetricTime();
////         	      // If the note at noteIndex is the last note in the voice: set lists to null and -1 
////       	  			if (metricTime.equals(lastMetricTimeInVoice)) {
////       	  				metricTimesNext.add(null);
////       	  				pitchesNext.add(-1);
////     	  	      }
////       	  	    // If not: add the metric time and the pitch of the closest next note
////       	  			else {	
//         	  		  for (NotationChord nc : nv) { 
//       	  	    	  if (nc.getMetricTime().equals(closestMetricTime)) {
////           	  			if (nc.getMetricTime().isGreater(metricTime)) {
////         	  	    		isNextNote = true;
////           	  				metricTimesNext.add(nc.getMetricTime());
////         	  	    		pitchesNext.add(nc.get(0).getMidiPitch());
//         	  	    		
//       	  	    	  	int currentPitchNext = nc.get(0).getMidiPitch();
//       	  	    	  	Rational currentMetricTimeNext = nc.getMetricTime();
////         	  	    		if (nc.getMetricTime().equals(closestMetricTime)) {
//                 	  		// Add to predictedVoicesNext
//                    	predictedVoicesNext.add(currentPredictedVoice);
//                    	for (int j = 0; j < basicTabSymbolProperties.length; j++) { 
//            	    			int p = basicTabSymbolProperties[j][Tablature.PITCH];
//            	    			Rational mt = new Rational(basicTabSymbolProperties[j][Tablature.ONSET_TIME], 
//           	  	    			Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
//            	    			if (p == currentPitchNext && mt.equals(currentMetricTimeNext)) {
//            	    				int noteIndexNext = j;
//            	  	    		// Determine the bwd counterpart of noteIndex and add to noteIndicesBwdNext
//            	    				for (int k = 0; k < backwardsMapping.size(); k++) {
//            	          		if (backwardsMapping.get(k) == noteIndexNext) {
//            	          			noteIndicesBwdNext.add(k);
//            	          			break;
//            	          		}
//            	          	}
//            	    				// Add to metPosNext and indicesInChordNext 
//            	    				metPosNext.add(AuxiliaryTool.getMetricPositionAsString(allMetricPositions.get(noteIndexNext)));
//            	    				indicesInChordNext.add(basicTabSymbolProperties[j][Tablature.NOTE_SEQ_NUM]);
//            	    				break;
//            	    			}
//             	  	    }
////                      }
//         	  	    		
//         	  	    		
//         	  	    		
//         	  	    		break;
//         	  	    	}
//         	  		  }
////         	  	  }
//       	  			
////       	  		// Only if there is a next note in currentPredictedVoice
////       	  		if (metricTimesNext.get(i) != null) {
////       	  			for (NotationChord nc : nv) { 
////         	  			if (nc.getMetricTime().isGreater(metricTime)) {
////       	  	    		pitchesNext.add(nc.get(0).getMidiPitch());
////       	  	    		break;
////       	  	    	}
////       	  		  }
////       	  		}
//       	  	  }
//       	  		
////         	    // 1. List the index, the metric position, and the sequence number in the chord of the next note in
////       	  		// predictedVoices that is closest in onset time to the note at noteIndex. If the note at noteIndex is
////       	  		// a CoD and the next notes in both voices are equally close, both are taken into account
////       	  		List<Integer> noteIndicesBwdNext = new ArrayList<Integer>();
////         	  	List<String> metPosNext = new ArrayList<String>();
////         	  	List<Integer> indicesInChordNext = new ArrayList<Integer>();
////         	  	List<Integer> predictedVoicesNext = new ArrayList<Integer>();
////         	  	for (int i = 0; i < predictedVoices.size(); i++) {
////         	      // NB: predictedVoices contains the voices in order of prediction (i.e., element 0 is the first predicted
////           	  	// voice and any element 1 the second). Thus, metricTimesNext and pitchesNext are ordered accordingly
////         	  		Rational currentMetricTimeNext = metricTimesNext.get(i);
////                int currentPitchNext = pitchesNext.get(i);
////                // Only if currentMetricTimeNext is the metric time of the closest next note
//////                if (currentMetricTimeNext != null && currentMetricTimeNext.equals(closestMetricTimeNext)) {
////                Rational closestMetricTime = metricTime.add(maxDur);
////                
////                if (currentMetricTimeNext != null && currentMetricTimeNext.equals(closestMetricTime)) {
////           	  		// Add to predictedVoicesNext
////                	predictedVoicesNext.add(predictedVoices.get(i));
////                	for (int j = 0; j < basicTabSymbolProperties.length; j++) { 
////        	    			int p = basicTabSymbolProperties[j][Tablature.PITCH];
////        	    			Rational mt = new Rational(basicTabSymbolProperties[j][Tablature.ONSET_TIME], 
////       	  	    			Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
////        	    			if (p == currentPitchNext && mt.equals(currentMetricTimeNext)) {
////        	    				int noteIndexNext = j;
////        	  	    		// Determine the bwd counterpart of noteIndex and add to noteIndicesBwdNext
////        	    				for (int k = 0; k < backwardsMapping.size(); k++) {
////        	          		if (backwardsMapping.get(k) == noteIndexNext) {
////        	          			noteIndicesBwdNext.add(k);
////        	          			break;
////        	          		}
////        	          	}
////        	    				// Add to metPosNext and indicesInChordNext 
////        	    				metPosNext.add(AuxiliaryTool.getMetricPositionAsString(allMetricPositions.get(noteIndexNext)));
////        	    				indicesInChordNext.add(basicTabSymbolProperties[j][Tablature.NOTE_SEQ_NUM]);
////        	    				break;
////        	    			}
////         	  	    }
////                }
////                
////         	  	}
//       	  		
//         	
//       	  		// 3. Record conflict and add to conflictsRecord
//       	  		String voiceNums = "";
//         	  	if (predictedVoicesNext.equals(Arrays.asList(new Integer[]{predictedVoices.get(0)}))) {
//         	  		voiceNums = "the first predicted voice";
//         	  	}
//         	  	else if (predictedVoicesNext.equals(Arrays.asList(new Integer[]{predictedVoices.get(1)}))) {
//         	  		voiceNums = "the second predicted voice";
//         	  	}
//         	  	else if (predictedVoicesNext.equals(predictedVoices)) {
//         	  		voiceNums = "all predicted voices";
//         	  	}
//         	  	conflictsRecord = conflictsRecord.concat(
//         	   		"noteIndex = " + noteIndexBwd + "\r\n" +
//         	   		"  type (iv) conflict between note " + noteIndexBwd + " (bar: " + metPos + ", index in chord: " + indexInChord +
//         	   		") and notes " + noteIndicesBwdNext	+ " (bars: " + metPosNext + ", indices in chord: " + 
//         	   		indicesInChordNext + "):\r\n" +
//         	   	  "  the predicted duration for note " + noteIndexBwd + " (predicted duration: " + predictedDuration + 
//       				  ") gives an offset time greater than the onset time of the next note in " + voiceNums +  
//         	   	  " for note " + noteIndexBwd + " (predictedVoices: " + predictedVoices + ")\r\n"
//         	  	);
//         	  	
//  //       	  	conflictsRecord = conflictsRecord.concat("  --> assignment of duration " + predictedDuration + 
//  //          		" made undone for note " + noteIndexBwd + "; ");
//         	  	conflictsRecord = conflictsRecord.concat("  --> predicted duration " + predictedDuration + " for note " +
//     	   	      noteIndexBwd + " adapted; ");
//         	    
//         	  	// 4. Redetermine predictedDurationLabel
//         	  	int maxDurAsInt = maxDur.getNumer() *	(Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom() / maxDur.getDenom());
////       	  		int maxDurAsInt = basicTabSymbolProperties[noteIndex][Tablature.MIN_DURATION];
//       	  		predictedDurationLabel = Transcription.createDurationLabel(maxDurAsInt);
////       	  		predictedDurationLabel = 
////       	  			Transcription.createDurationLabel(basicTabSymbolProperties[noteIndex][Tablature.MIN_DURATION]);
//       	  		predictedDuration = maxDur;
////       	  		predictedDuration =
////       	     		new Rational((predictedDurationLabel.indexOf(1.0) + 1),	Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
//       	  		predictedDuration.reduce();
//         	  	// Add new predicted duration to conflictsRecords
////           		conflictsRecord = conflictsRecord.concat("new predicted duration for note " + noteIndexBwd + ": " + 
////           		  predictedDuration + "\r\n" + "\r\n");
//           		conflictsRecord = 
//           			conflictsRecord.concat("new predicted duration: " +	predictedDuration + "\r\n" + "\r\n");
//           		
//           // 4. Redetermine predictedDurationLabelPrevious and reset the corresponding elements in the Lists
//// 	   	      predictedDurationLabelPrevious = Transcription.createDurationLabel(minDurPrevious);
////        		int maxDurAsInt = 
////   	   	      maxDurPrevious.getNumer() *	(Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom() / maxDurPrevious.getDenom());
////   	   	    predictedDurationLabelPrevious = Transcription.createDurationLabel(maxDurAsInt);
////     				predictedDurationPrevious = maxDurPrevious;	
//// 	   	      predictedDurationPrevious.reduce();
////   	  	  	allDurationLabels.set(noteIndexPrevious, predictedDurationLabelPrevious);
////   	  	  	List<Note> previousNote = allNotes.get(noteIndexPreviousBwd);
////   	  	    for (Note n : previousNote) {
////   	  	    	n.setScoreNote(adaptedScoreNotePrevious);
////   	  	    }
////   	  	    allNotes.set(noteIndexPreviousBwd, previousNote);
////   	  	    // Add new predicted duration to conflictsRecords
////         		conflictsRecord = conflictsRecord.concat("new predicted duration for note " + noteIndexPreviousBwd + ": " + 
////         		  predictedDurationPrevious + "\r\n" + "\r\n");
//     	  	  }
//     	  	}
//     	  }
//             	  
//        // 3. Resolve any type (v) conflicts (conflicts between all notes x that, at the onset time of the note at
//     	  // noteIndex, have an offset time that exceeds the onset time of the notes y in the next chord (i.e, the 
//     	  // chord after the note at noteIndex) if the sum of x and y is greater than maxNumVoices)
//     	  if (isTablatureCase && modelDuration && !modelBackward) {
//      		// Determine sizeChord, sizeNextChord, onsetTimeNextChord, metPosNextChord, and maxNumVoices
//      		int sizeChord = basicTabSymbolProperties[noteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
//     	  	int sizeNextChord = -1;
//          Rational onsetTimeNextChord = null;
//          String metPosNextChord = null;
//          int chordIndex = basicTabSymbolProperties[noteIndex][Tablature.CHORD_SEQ_NUM];
//      	  int lastChordIndex = basicTabSymbolProperties[basicTabSymbolProperties.length - 1][Tablature.CHORD_SEQ_NUM];
//      	  if (chordIndex != lastChordIndex) {
//      	    for (Integer[] btp : basicTabSymbolProperties) {
//      	    	if (btp[Tablature.CHORD_SEQ_NUM] == chordIndex + 1) {
//      	    		sizeNextChord = btp[Tablature.CHORD_SIZE_AS_NUM_ONSETS];
//      	    		onsetTimeNextChord = new Rational(btp[Tablature.ONSET_TIME], Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
//      	    		metPosNextChord = 
//      	    			AuxiliaryTool.getMetricPositionAsString(Tablature.getMetricPosition(onsetTimeNextChord, meterInfo));
//      	    		break;
//      	    	}
//      	    }
//      	  }
//      		int maxNumVoices = newTranscription.getPiece().getScore().size(); // TODO this is possible because newTranscription has highestNumberOfVoicesTraining voices
//      		      	  
//      		// If the note at noteIndex is the last note in the chord and there is a next chord
//      		if ((indexInChord == sizeChord - 1) && sizeNextChord != -1) {
//      			// Get the indices of any sustained notes
//      			List<Integer> indicesOfSustained = featureGenerator.getIndicesOfSustainedPreviousNotes(basicTabSymbolProperties, 
//       	  		allDurationLabels, null, lowestNoteIndex);
//      			// Get the indices of any lower chord notes
//      			List<Integer> indicesOfChordNotes = new ArrayList<Integer>();
//   	  	    for (int i = lowestNoteIndex; i <= noteIndex; i++) {
//   	  	    	indicesOfChordNotes.add(i);
//   	  	    }
//   	  	    // Combine into list with all indices
//   	  	    List<Integer> allIndices = new ArrayList<Integer>();
//   	  	    allIndices.addAll(indicesOfSustained);
//   	  	    allIndices.addAll(indicesOfChordNotes);
// 	  	    	
// 	  	    	// List all offset times exceeding the onset time of the next chord
//   	  	    List<Integer> indicesOfExceeding = new ArrayList<Integer>();
//   	  	    for (int i : allIndices) {
//   	  	    	Rational onset = new Rational(basicTabSymbolProperties[i][Tablature.ONSET_TIME], 
//     	  	    	Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
//   	  	    	Rational duration = null;
//   	  	    	// If i != noteIndex, get the duration from allPredictedDurations 
//   	  	    	if (i != noteIndex) {
//   	  	    		duration = allPredictedDurations.get(i)[0];
//   	  	    	}
//   	  	    	// If i == noteIndex, allPredictedDurations does not yet contain a value at index i
//   	  	    	else {
//   	  	    		duration = predictedDuration;
//   	  	    	}
//   	  	    	Rational offset = new Rational(onset.add(duration));
//   	  	    	if (offset.isGreater(onsetTimeNextChord)) {
// 	  	    			indicesOfExceeding.add(i);
// 	  	    		}
//   	  	    }
//   	  	    int numExceeding = indicesOfExceeding.size();
//   	  	    // If the number of exceeding notes and the size of the next chord is greater than the number of voices
//   	  	    if ((numExceeding + sizeNextChord) > maxNumVoices) {  	    
//   	  	    	// Determine the number of notes whose duration must be adapted so that maxNumVoices is not exceeded, 
//   	  	    	// and remove the necessary elements from indicesOfExceeding. Later indices are more likely to have an
//   	  	    	// incorrect duration because they may not have gone through duration conflicts yet; therefore, remove
//   	  	    	// elements from the start of the list
//   	  	    	int numToBeAdapted = (numExceeding + sizeNextChord) - maxNumVoices;
//   	  	    	int listSize = indicesOfExceeding.size();
//     	  	    indicesOfExceeding = indicesOfExceeding.subList(listSize - numToBeAdapted, listSize); 
//   	  	    	
//   	  	    	List<String> metPoss = new ArrayList<String>();
//   	  	    	List<Integer> indicesInChord = new ArrayList<Integer>(); 
//     	  	    List<Rational> durations = new ArrayList<Rational>();
//     	  	    List<Rational> durationsAdapted = new ArrayList<Rational>();
//   	  	    	// For each note at index i
//     	  	    for (int i : indicesOfExceeding) {
//     	  	      // 0. Get the pitch and onset of the note; also add to Lists
//     	  	    	int pitch = basicTabSymbolProperties[i][Tablature.PITCH];
//    	  	    	Rational onset = new Rational(basicTabSymbolProperties[i][Tablature.ONSET_TIME],
//    	  	    		Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
//    	  	    	metPoss.add(AuxiliaryTool.getMetricPositionAsString(Tablature.getMetricPosition(onset, meterInfo)));
//    	  	    	indicesInChord.add(basicTabSymbolProperties[i][Tablature.NOTE_SEQ_NUM]);
//    	  	      // If i != noteIndex, get the duration from allPredictedDurations
//    	  	    	if (i != noteIndex) {  
//	  	    		    durations.add(allPredictedDurations.get(i)[0]);
//	  	    	    }
//    	  	      // If i == noteIndex, allPredictedDurations does not yet contain a value at index i
//	  	    	    else {
//	  	    		    durations.add(predictedDuration);
//	  	    	    }
//    	  	    	
//     	  	    	// 1. Add to conflictIndices
//    	  	    	if (!conflictIndices.get(1).contains(i)) {
//         			    conflictIndices.get(1).add(i);
//         			  }
//    	  	    	    	  	    	
//    	  	      // 2. Adapt
//    	  	    	// a. If i != noteIndex
//    	  	    	if (i != noteIndex) {
//      	  	    	List<Integer> currentVoices = allPredictedVoices.get(i);
//       	  	    	Rational maxDur = null;
//       	  	    	ScoreNote adaptedScoreNote = null;
//       	  	    	// 1. Adapt the Note's duration in newTranscription
//       	  	    	for (int voice : currentVoices) {
//       	  	    		NotationVoice nv = newTranscription.getPiece().getScore().get(voice).get(0);
//       	  	    		for (NotationChord nc : nv) {
//       	  	    			Note n = nc.get(0);
//       	  	    			if (n.getMidiPitch() == pitch && n.getMetricTime().equals(onset)) {
//       	  	    				// Determine the maximum duration and reset n with it
//       	  	    				maxDur = onsetTimeNextChord.sub(onset);
//       	  	    				adaptedScoreNote = new ScoreNote(new ScorePitch(pitch), onset, maxDur); 
//             	  	    	n.setScoreNote(adaptedScoreNote);
//             	  	    	break;
//       	  	    			}
//       	  	    		}
//       	  	    	}
//       	  	      // Add to durationsAdapted (this has to happen outside the for-loop, because in the case of a CoD 
//       	  	    	// the duration must be added only once)
//  	  	    			durationsAdapted.add(maxDur);
//  	  	    			
//  	  	    		  // 2. Reset allDurationLabels, allPredictedDurations, and allNotes 
//     	  	    	  int maxDurAsInt =	maxDur.getNumer() *	(Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom() / maxDur.getDenom());
//       	   	      List<Double> predictedDurationLabelAdapted = Transcription.createDurationLabel(maxDurAsInt);
//       	   	      allDurationLabels.set(i, predictedDurationLabelAdapted);
//         	  	  	allPredictedDurations.set(i, new Rational[]{maxDur});
//         	  	  	List<Note> previousNote = allNotes.get(i);
//         	  	    for (Note n : previousNote) {
//         	  	    	n.setScoreNote(adaptedScoreNote);
//         	  	    }
//         	  	    allNotes.set(i, previousNote);
//     	  	      }
//	  	    			
//	  	    			// b. If i == noteIndex 
//	  	    			if (i == noteIndex) {
//	  	    				// Adapt predictedDurationLabel and predictedDuration
//	  	    				Rational maxDur = onsetTimeNextChord.sub(onset);
//	  	    				int maxDurAsInt =	maxDur.getNumer() *	(Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom() / maxDur.getDenom());
//           	  		predictedDurationLabel = Transcription.createDurationLabel(maxDurAsInt);
//           	  		predictedDuration = maxDur;
//           	  		predictedDuration.reduce();
//           	  		// Add to durationsAdapted
//           	  		durationsAdapted.add(predictedDuration);
//	  	    			}
//    	  	    }
//     	  	         	  	    
//     	  	    // Add to conflictsRecord
//        			conflictsRecord = conflictsRecord.concat(
//          			"noteIndex = " + noteIndex + "\r\n" +	
//        				"  type (v) conflict between notes " + indicesOfExceeding + " (bars: " + metPoss + ", indices in chord: " + 
//        				indicesInChord + ") and the chord at bar " + metPosNextChord + ":\r\n" +
//        				"  the predicted durations for notes " + indicesOfExceeding + " (predicted durations: " + durations + 
//           			") give offset times greater than the onset time of the chord at bar " + metPosNextChord + 
//           			", resulting in a larger number of voices than allowed (" + maxNumVoices + ") \r\n" + 
//           			"  --> predicted durations " + durations +	" for notes " + indicesOfExceeding + " adapted; "
//             	);	
//        		  // Add new predicted durations to conflictsRecords
//        			conflictsRecord = 
//        				conflictsRecord.concat("new predicted durations: " + durationsAdapted + "\r\n" + "\r\n"); 
//   	  	    }	    
//      		}
//      		      		
////      		// If there is a next chord and it is a full chord
////      		if (sizeNextChord != -1 && sizeNextChord == maxNumVoices) {
////      			List<Integer> noteIndicesLast = new ArrayList<Integer>();
////      			List<String> metPosLast = new ArrayList<String>();
////      			List<Integer> indicesInChordLast = new ArrayList<Integer>();
////      			List<Rational> predictedDurationsLast = new ArrayList<Rational>();
////      			List<Rational> predictedDurationsLastAdapted = new ArrayList<Rational>();
////      		  // For each voice
////      			for (int i = 0; i < maxNumVoices; i++) {
////      				NotationVoice nv = newTranscription.getPiece().getScore().get(i).get(0);
//////     	  	    int indexOfLast = nv.size() - 1; 	  	    
//////     	  	    if (indexOfLast != -1) {
//////        				Note last = nv.get(indexOfLast).get(0);
////     	  	    
////      			  // Only if nv is not empty
////      				if (nv.size() != 0) {
////     	  	    	// Create the Note for the note at noteIndex 
////     	  	    	// NB: metricDuration can be its minimum duration as it is not taken into consideration when 
////      					// determining last
////      					int pitch = basicTabSymbolProperties[noteIndex][Tablature.PITCH];
////                Rational metricDuration = new Rational(basicTabSymbolProperties[noteIndex][Tablature.MIN_DURATION], 
////             	    Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());	
////     	  	    	Note currentNote = Transcription.createNote(pitch, metricTime, metricDuration);
////     	  	      // Find the last Note before metricTime in nv 
////     	  	    	Note last = featureGenerator.getPreviousNoteInVoice(nv, currentNote);
////       	  	    int pitchLast = last.getMidiPitch();
////        				Rational metricTimeLast = last.getMetricTime();
////       	  	    Rational durationLast = last.getMetricDuration();
////       	  	    Rational offsetTimeLast = metricTimeLast.add(durationLast); 
////       	  	    // If the offset time of last exceeds onsetTimeNextChord
////       	  	    if (offsetTimeLast.isGreater(onsetTimeNextChord)) {
////                  // Determine the noteIndex, the metric position, and the index in the chord of last
////       	  	    	int noteIndexLast = -1;   	
////       	  	    	for (int j = 0; j < basicTabSymbolProperties.length; j++) {
////       	  	    		int p = basicTabSymbolProperties[j][Tablature.PITCH];
////       	  	    		Rational mt = new Rational(basicTabSymbolProperties[j][Tablature.ONSET_TIME], 
////       	  	    			Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom()); 
////       	  	    		if (p == pitchLast &&	mt.equals(metricTimeLast)) {
////       	  	    			noteIndexLast = j;
////       	  	    			// Add to noteIndicesLast, metPosLast, and indicesInChordLast
////       	  	    			noteIndicesLast.add(j);
////       	  	    			metPosLast.add(AuxiliaryTool.getMetricPositionAsString(allMetricPositions.get(j)));
////       	  	    			indicesInChordLast.add(basicTabSymbolProperties[j][Tablature.NOTE_SEQ_NUM]);
////       	  	    			break;
////       	  	    		}
////       	  	    	}
////       	  	      // 1. Add noteIndexLast to conflictIndices
////       	  	    	if (!conflictIndices.get(1).contains(noteIndexLast)) {
////             			  conflictIndices.get(1).add(noteIndexLast);
////             			}
////       	  	    	
////       	  	    	// 2. Adapt the Note's duration in Transcription
////       	  	    	Rational difference = offsetTimeLast.sub(onsetTimeNextChord);
////       	  	    	Rational maxDurLast = durationLast.sub(difference);
//////       	  	    	System.exit(0);
////       	  	    	ScoreNote adaptedScoreNoteLast = new ScoreNote(new ScorePitch(pitchLast), metricTimeLast, maxDurLast);
////       	  	    	// Find the index of last in nv
////       	  	    	int indexOfLast = -1;
////       	  	    	for (int j = 0; j < nv.size(); j++) {
////       	  	    		NotationChord nc = nv.get(j);
////       	  	    		if (nc.get(0).getMidiPitch() == pitchLast && nc.getMetricTime().equals(metricTimeLast)) {
////       	  	    			indexOfLast = j;
////       	  	    		}
////       	  	    	}
////       	  	    	nv.get(indexOfLast).get(0).setScoreNote(adaptedScoreNoteLast);
////       	  	    	
////       	   	              	  	    		
////       	  	    	// DIT NU
//////       	  	    	Rational predictedDurationLast = new Rational(allDurationLabels.get(index).indexOf(1.0) + 1, 
//////       	   	      	Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
//////       	  	    	System.out.println("KLERE");
//////       	  	    	System.out.println(indexBwd);
////       	  	    	
////       	  	      // 3. Add to predictedDurationsLast and predictedDurationsLastAdapted
////       	  	    	Rational predictedDurationLast = allPredictedDurations.get(noteIndexLast)[0]; 
////       	   	      predictedDurationLast.reduce();
////       	   	      predictedDurationsLast.add(predictedDurationLast);
////         	   	    Rational predictedDurationLastAdapted = maxDurLast;
////       	   	      predictedDurationLastAdapted.reduce(); 
////       	   	      predictedDurationsLastAdapted.add(predictedDurationLastAdapted);
////       	   	      
////       	   	      // 4. Reset allDurationLabels, allPredictedDurations, and allNotes 
////       	  	    	int maxDurLastAsInt = 
////       	   	      	maxDurLast.getNumer() *	(Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom() / maxDurLast.getDenom());
////       	   	      List<Double> predictedDurationLabelLastAdapted = Transcription.createDurationLabel(maxDurLastAsInt);
////       	   	      allDurationLabels.set(noteIndexLast, predictedDurationLabelLastAdapted);
////         	  	  	allPredictedDurations.set(noteIndexLast, new Rational[]{predictedDurationLastAdapted});
////         	  	  	List<Note> previousNote = allNotes.get(noteIndexLast);
////         	  	    for (Note n : previousNote) {
////         	  	    	n.setScoreNote(adaptedScoreNoteLast);
////         	  	    }
////         	  	    allNotes.set(noteIndexLast, previousNote);
////     	  	      }	
////      				}
////      			}
////      			
////        	  // If a conflict was encountered: record and add to conflictsRecord
////      			if (noteIndicesLast.size() != 0) {
//////        			String metPosNextChord = AuxiliaryTool.getMetricPositionAsString(allMetricPositions.get(noteIndex + 1));
////        			conflictsRecord = conflictsRecord.concat(
////          			"noteIndex = " + noteIndex + "\r\n" +
//////        				"noteIndices = " + noteIndicesLast + "\r\n" +	
////        				"  type (v) conflict between notes " + noteIndicesLast + " (bars: " + metPosLast + ", indices in chord: " + 
////        				indicesInChordLast + ") and the full chord at bar " + metPosNextChord + ":\r\n" +
////        				"  the predicted durations for notes " + noteIndicesLast + " (predicted durations: " + predictedDurationsLast + 
////           			") give offset times greater than the onset time of the full chord at bar " + metPosNextChord + "\r\n" + 
////           			"  --> predicted durations " + predictedDurationsLast +	" for notes " + noteIndicesLast + " adapted; "
////             	);	
////        		  // Add new predicted durations to conflictsRecords
////  //         		conflictsRecord = conflictsRecord.concat("new predicted durations for notes " + noteIndicesLast + ": " + 
////  //         		  predictedDurationsLastAdapted + "\r\n" + "\r\n");
////        			conflictsRecord = 
////        				conflictsRecord.concat("new predicted durations: " + predictedDurationsLastAdapted + "\r\n" + "\r\n");
////      		  }
////      		}
//      		
//      	}
//  	  } 
//  	}
//  	
//  	// 3. predictedVoices (and, if applicable, predictedDurationLabel and allPredictedDurations) now have their final 
//  	// value: add to lists
//  	allPredictedVoices.add(predictedVoices);
////  	allVoiceLabels.add(dataConverter.convertIntoVoiceLabel(predictedVoices)); // FUK 23-6
//  	allVoiceLabels.set(noteIndex, dataConverter.convertIntoVoiceLabel(predictedVoices));
////    if (isTablatureCase && isNewModel && modelDuration) {//2016
//  	if (isTablatureCase && ExperimentRunner.imp == ExperimentRunner.Implementation.DISS 
//  		&& modelDuration) {
//    	if (predictedVoices.size() == 1) {
////        allVoicesCoDNotes.add(null); // FUK 23-6
//    		allVoicesCoDNotes.set(noteIndex, null); // TODO actually not necessary
//    	}
//    	else {
//    		// TODO Currently, in the case of a CoD only one duration is predicted. In this case, the lower 
//    		// CoDnote (i.e., the one in the lower voice that comes first in the NoteSequence) must be placed
//    		// at element 0 of voicesCoDNotes (see JavaDoc above Transcription.handleCoDnotes())
//    		List<Integer> sortedAndReversed = new ArrayList<Integer>(predictedVoices);
//    		// Sort and reverse, so that the lowest voice comes first
//    		Collections.sort(sortedAndReversed);
//    		Collections.reverse(sortedAndReversed);
//    		Integer[] voicesCoDNotes = new Integer[]{sortedAndReversed.get(0), sortedAndReversed.get(1)};
//    		allVoicesCoDNotes.set(noteIndex, voicesCoDNotes);
//    	}
//    	allDurationLabels.set(noteIndex, predictedDurationLabel);
//    	allPredictedDurations.add(dataConverter.convertIntoDuration(predictedDurationLabel));
//    }
//    
//    
////		if (noteIndex == 670) {
//////			System.out.println(conflictsRecord);
////			System.out.println("pitch = " + basicTabSymbolProperties[670][Tablature.PITCH]);
////			System.out.println("note seq no = " + basicTabSymbolProperties[670][Tablature.NOTE_SEQ_NUM]);
//////			System.out.println("onset time = " + basicTabSymbolProperties[430][Tablature.ONSET_TIME]);
////			
////			Rational mt = new Rational(basicTabSymbolProperties[670][Tablature.ONSET_TIME], 32);
////			System.out.println("metric time = " + mt);
////			System.out.println("metric position = " + 
////			  AuxiliaryTool.getMetricPositionAsString(Tablature.getMetricPosition(mt, meterInfo)));
////			for (int j = 667; j < 671; j++) {
////				Rational hz = allPredictedDurations.get(j)[0];
////				hz.reduce();
////				System.out.println(j + ": " + hz + ", voice " + allPredictedVoices.get(j));
////			}
////		}
//  }

	
	/**
	 * Returns, for all voice and duration (if applicable) conflicts, a list containing 
	 * <ul>
	 * 		<li>as element 0: a list of the indices of all notes initially assigned correctly and 
	 * 			reassigned correctly</li>
	 * 		<li>as element 1: a list of the indices of all notes initially assigned correctly and 
	 * 			reassigned incorrectly</li>
	 * 		<li>as element 2: a list of the indices of all notes initially assigned incorrectly and 
	 * 			reassigned correctly</li>
	 * 		<li>as element 3: a list of the indices of all notes initially assigned incorrectly and 
	 * 			reassigned incorrectly.</li>
	 * </ul>  
	 * 
	 * @param conflictIndices
	 * @param allPredictedVoices
	 * @param allPredictedDurationLabels
	 * @param argEqualDurationUnisonsInfo
	 * @param argAllNetworkOutputs
	 * @param groundTruths
	 * @param backwardsMapping
	 * @return
	 */
	private static List<List<List<Integer>>> getConflictInfo(List<List<Integer>> conflictIndices, 
		List<List<Integer>> allPredictedVoices,	List<List<Double>> allPredictedDurationLabels, 
		List<Integer[]> argEqualDurationUnisonsInfo, List<double[]> argAllNetworkOutputs, 
		List<List<List<Double>>> groundTruths, List<Integer> backwardsMapping) {

		List<List<Double>> argGroundTruthVoiceLabels = groundTruths.get(0);
		List<List<Double>> argGroundTruthDurationLabels = groundTruths.get(1);
		
		Map<String, Double> modelParameters = Runner.getModelParams();
		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()]; 
		boolean modelDuration = m.getModelDuration();
		boolean allowCoD = 
			ToolBox.toBoolean(modelParameters.get(Runner.SNU).intValue());
		double devThresh = -1.0;
		if (Runner.getDataset().isTablatureSet()) {
			devThresh = modelParameters.get(Runner.DEV_THRESHOLD);
		}

		List<Integer> indicesOfCorrToCorr = new ArrayList<Integer>();
		List<Integer> indicesOfCorrToIncorr = new ArrayList<Integer>();
		List<Integer> indicesOfIncorrToCorr = new ArrayList<Integer>();
		List<Integer> indicesOfIncorrToIncorr = new ArrayList<Integer>();
		
		List<Integer> indicesOfCorrToCorrDur = new ArrayList<Integer>();
		List<Integer> indicesOfCorrToIncorrDur = new ArrayList<Integer>();
		List<Integer> indicesOfIncorrToCorrDur = new ArrayList<Integer>();
		List<Integer> indicesOfIncorrToIncorrDur = new ArrayList<Integer>();
		
		boolean voiceConflFound = false;
		boolean durConflFound = false;
		
		int numNotes = argGroundTruthVoiceLabels.size();
		for (int i = 0; i < numNotes; i++) {
			int noteIndex = i;
			if (backwardsMapping != null) {
				noteIndex = backwardsMapping.get(i);
			}
			
			double[] output = argAllNetworkOutputs.get(i); // HIER OK
			
			// For voice conflicts
			if (conflictIndices.get(0).contains(i)) { // HIER OK
				if (voiceConflFound == false) {
					voiceConflFound = true;
				}
			
//				double[] output = argAllNetworkOutputs.get(i); // HIER OK
				double[] outputArray = 
					Arrays.copyOfRange(output, 0, Transcription.MAXIMUM_NUMBER_OF_VOICES);
				List<Integer> predictedVoices = 
					OutputEvaluator.interpretNetworkOutput(outputArray, allowCoD, devThresh).get(0);
				List<Integer> adaptedVoices = allPredictedVoices.get(i); // HIER OK
				List<Double> actualVoiceLabel = argGroundTruthVoiceLabels.get(i); // HIER OK
				List<Integer> actualVoices = DataConverter.convertIntoListOfVoices(actualVoiceLabel);

				// Keep track of the conflict reassignments
				boolean voicesPredictedInitiallyCorrectly = false;
				boolean predictedVoicesAdaptedCorrectly = false;
				boolean voicesPredictedInitiallyCorrectlyUpper = false;
				boolean predictedVoicesAdaptedCorrectlyUpper = false;
				boolean lowerAdapted = false;
				boolean upperAdapted = false;
				
				// a. In the tablature case, where equal duration unisons do not apply
				if (argEqualDurationUnisonsInfo == null) {
					voicesPredictedInitiallyCorrectly = 
						ErrorCalculator.assertCorrectness(predictedVoices, actualVoices);
					predictedVoicesAdaptedCorrectly = 
						ErrorCalculator.assertCorrectness(adaptedVoices, actualVoices);
				}
				// b. In the non-tablature case, when the note at index i is not part of an EDU
				else if (argEqualDurationUnisonsInfo != null && argEqualDurationUnisonsInfo.get(i) == null) { // HIER OK
					voicesPredictedInitiallyCorrectly = 
						ErrorCalculator.assertCorrectness(predictedVoices, actualVoices);
					predictedVoicesAdaptedCorrectly = 
						ErrorCalculator.assertCorrectness(adaptedVoices, actualVoices);
				}
				// c. In the non-tablature case, when the note at index i is part of an EDU
				else if (argEqualDurationUnisonsInfo != null && argEqualDurationUnisonsInfo.get(i) != null) { // HIER OK
					// Only if the note at index i is not the last note
					if (i != numNotes - 1) {
						// Determine the predicted and adapted voices for the lower and upper EDUnotes (.get(0) can be used
						// because in the non-tablature case there are no CoDs and the lists will contain only one element)
						int voicePredInitiallyLowerNote = predictedVoices.get(0);
						double[] outputNextNote = argAllNetworkOutputs.get(i + 1); // HIER OK
						int voicePredInitiallyUpperNote = 
							OutputEvaluator.interpretNetworkOutput(outputNextNote, allowCoD,
							devThresh).get(0).get(0);
						int adaptedVoiceLowerNote = adaptedVoices.get(0);
						int adaptedVoiceUpperNote = allPredictedVoices.get(i + 1).get(0); // HIER OK
						if (voicePredInitiallyLowerNote != adaptedVoiceLowerNote) {
							lowerAdapted = true;
						}
						if (voicePredInitiallyUpperNote != adaptedVoiceUpperNote) {
							upperAdapted = true;
						}
						
						// Determine for both EDUnotes whether the predicted and adapted voices are correct
						List<Integer[]> predictedAndAdaptedVoices = new ArrayList<Integer[]>();
						predictedAndAdaptedVoices.add(new Integer[]{voicePredInitiallyLowerNote, voicePredInitiallyUpperNote});
						predictedAndAdaptedVoices.add(new Integer[]{adaptedVoiceLowerNote, adaptedVoiceUpperNote});
						List<Integer> allowedVoices = Arrays.asList(new Integer[]{argEqualDurationUnisonsInfo.get(i)[1], 
							argEqualDurationUnisonsInfo.get(i)[0]}); // HIER OK
						boolean[][] unisonNotesPredictedCorrectly = 
							ErrorCalculator.assertCorrectnessEDUNotes(predictedAndAdaptedVoices, 
							allowedVoices/*, i*/); // 05.08.2017 i verwijderd 
						boolean lowerUnisonNotePredictedCorrectly = unisonNotesPredictedCorrectly[0][0];
						boolean upperUnisonNotePredictedCorrectly = unisonNotesPredictedCorrectly[0][1];
						boolean lowerUnisonNoteAdaptedCorrectly = unisonNotesPredictedCorrectly[1][0];
						boolean upperUnisonNoteAdaptedCorrectly = unisonNotesPredictedCorrectly[1][1];

						// Set variables
						voicesPredictedInitiallyCorrectly = lowerUnisonNotePredictedCorrectly;
						predictedVoicesAdaptedCorrectly = lowerUnisonNoteAdaptedCorrectly;
						voicesPredictedInitiallyCorrectlyUpper = upperUnisonNotePredictedCorrectly;
						predictedVoicesAdaptedCorrectlyUpper = upperUnisonNoteAdaptedCorrectly;
					}
				}

				// Determine correctness of reassignments
				// 1. In the tablature case, and in the non-tablature case for non EDUnotes 
				// and lower EDUnotes
				if (argEqualDurationUnisonsInfo == null || 
					(argEqualDurationUnisonsInfo != null && argEqualDurationUnisonsInfo.get(i) == null) ||
					(argEqualDurationUnisonsInfo != null && argEqualDurationUnisonsInfo.get(i) != null
					&& lowerAdapted)) { // HIER OK
					// a. If the voice(s) were initially predicted correctly and were adapted into the correct voice(s)
					if (voicesPredictedInitiallyCorrectly == true && predictedVoicesAdaptedCorrectly == true) {
						indicesOfCorrToCorr.add(i); // HIER OK
					}
					// b. If the voice(s) were initially predicted correctly but were adapted into the incorrect voice(s)
					if (voicesPredictedInitiallyCorrectly == true && predictedVoicesAdaptedCorrectly == false) {
						indicesOfCorrToIncorr.add(i); // HIER OK
					}
					// c. If the voice(s) were initially predicted incorrectly but were adapted into the correct voice(s) 
					if (voicesPredictedInitiallyCorrectly == false && predictedVoicesAdaptedCorrectly == true) {
						indicesOfIncorrToCorr.add(i); // HIER OK
					}
					// d. If the voice(s) were initially predicted incorrectly and were adapted into the incorrect voice(s)
					if (voicesPredictedInitiallyCorrectly == false && predictedVoicesAdaptedCorrectly == false) {
						indicesOfIncorrToIncorr.add(i); // HIER OK
					}
				}
				// 2. In the non-tablature case for upper EDUnotes 
				// NB: To avoid the index of the upper EDUnote being added twice (once when i is the index of the lower 
				// EDUnote and once that of the upper), it must only be added when i is the index of the lower EDUnote
				// TODO How is this implemented -- by means of the i + 1?
				if (argEqualDurationUnisonsInfo != null && argEqualDurationUnisonsInfo.get(i) != null) { // HIER OK
					// Only if the voice for the upper unison note was actually adapted  
					if (upperAdapted) {
						// a. If the voice(s) were initially predicted correctly and were adapted into the correct voice(s)
						if (voicesPredictedInitiallyCorrectlyUpper == true && predictedVoicesAdaptedCorrectlyUpper == true) {
							if (!indicesOfCorrToCorr.contains(i + 1)) { // HIER OK
								indicesOfCorrToCorr.add(i + 1); // HIER OK
							}
						}
						// b. If the voice(s) were initially predicted correctly but were adapted into the incorrect voice(s)
						if (voicesPredictedInitiallyCorrectlyUpper == true && predictedVoicesAdaptedCorrectlyUpper == false) {
							if (!indicesOfCorrToIncorr.contains(i + 1)) { // HIER OK
								indicesOfCorrToIncorr.add(i + 1); // HIER OK
							}
						}
						// c. If the voice(s) were initially predicted incorrectly but were adapted into the correct voice(s) 
						if (voicesPredictedInitiallyCorrectlyUpper == false && predictedVoicesAdaptedCorrectlyUpper == true) {
							if (!indicesOfIncorrToCorr.contains(i + 1)) { // HIER OK
								indicesOfIncorrToCorr.add(i + 1); // HIER OK
							}
						}
						// d. If the voice(s) were initially predicted incorrectly and were adapted into the incorrect voice(s)
						if (voicesPredictedInitiallyCorrectlyUpper == false && predictedVoicesAdaptedCorrectlyUpper == false) {
							if (!indicesOfIncorrToIncorr.contains(i + 1)) { // HIER OK
								indicesOfIncorrToIncorr.add(i + 1); // HIER OK
							}
						}
					}
				}
			}
			
			// For duration conflicts
			if (modelDuration && conflictIndices.get(1).contains(i)) { // HIER OK
				if (durConflFound == false) {
					durConflFound = true;
				}
				
				List<Integer> predictedDurationsAsIndex = 
					OutputEvaluator.interpretNetworkOutput(output, allowCoD, devThresh).get(1);
				List<Double> predictedDurationsAsLabel = 
					DataConverter.convertIntoDurationLabel(predictedDurationsAsIndex);
				Rational[] predictedDurations = 
					DataConverter.convertIntoDuration(predictedDurationsAsLabel);
				for (Rational r : predictedDurations) {
					r.reduce();
				}
					
				List<Double> predictedDurationLabelAdapted = allPredictedDurationLabels.get(noteIndex); // HIER OK
				Rational[] adaptedDurations = DataConverter.convertIntoDuration(predictedDurationLabelAdapted); 
				for (Rational r : adaptedDurations) {
					r.reduce();
				}
					
				List<Double> actualDurationLabel = argGroundTruthDurationLabels.get(i); // HIER OK
				Rational[]	actualDurations = DataConverter.convertIntoDuration(actualDurationLabel);
				for (Rational r : actualDurations) {
					r.reduce();
				}

				// Keep track of the conflict reassignments
				boolean durationsPredictedInitiallyCorrectlyOrHalfCorrectly = false;
				boolean predictedDurationsAdaptedCorrectlyOrHalfCorrectly = false;
				// a. If currentActualDurations contains one element (i.e., if it goes with 
				// a note that is not a CoD, or with a note that is a CoD whose notes have
				// the same duration)
				if (actualDurations.length == 1) {
					// Durations predicted initially
					if (predictedDurations[0].equals(actualDurations[0])) {
						durationsPredictedInitiallyCorrectlyOrHalfCorrectly = true;
					}
					// Adapted durations
					if (adaptedDurations[0].equals(actualDurations[0])) {
						predictedDurationsAdaptedCorrectlyOrHalfCorrectly = true;
					}
				}
				// b. If currentActualDurations contains two elements (i.e., if it goes with
				// a note that is CoD whose notes have different durations) 
				if (actualDurations.length == 2) {
					// Durations predicted initially
					// If the duration predicted initially is the same as one of the actual durations 
					if (predictedDurations[0].equals(actualDurations[0]) || 
						predictedDurations[0].equals(actualDurations[1])) {
						durationsPredictedInitiallyCorrectlyOrHalfCorrectly = true;
					}
					// Adapted durations
					// If the adapted duration is the same as one of the actual durations
					if (adaptedDurations[0].equals(actualDurations[0]) || 
						adaptedDurations[0].equals(actualDurations[1])) {
						predictedDurationsAdaptedCorrectlyOrHalfCorrectly = true;
					}
				}

				// Determine correctness of reassignments
				// a. If the duration(s) were initially predicted (half) correctly and were adapted (half) correctly
				if (durationsPredictedInitiallyCorrectlyOrHalfCorrectly == true && 
					predictedDurationsAdaptedCorrectlyOrHalfCorrectly == true) {
					indicesOfCorrToCorrDur.add(i); // HIER OK
				}
				// b. If the duration(s) were initially predicted (half) correctly but were adapted incorrectly 
				if (durationsPredictedInitiallyCorrectlyOrHalfCorrectly == true && 
					predictedDurationsAdaptedCorrectlyOrHalfCorrectly == false) {
					indicesOfCorrToIncorrDur.add(i); // HIER OK
				}
				// c. If the duration(s) were initially predicted incorrectly but were adapted (half) correctly 
				if (durationsPredictedInitiallyCorrectlyOrHalfCorrectly == false && 
					predictedDurationsAdaptedCorrectlyOrHalfCorrectly == true) {
					indicesOfIncorrToCorrDur.add(i); // HIER OK
				}
				// d. If the duration(s) were initially predicted incorrectly and were adapted incorrectly
				if (durationsPredictedInitiallyCorrectlyOrHalfCorrectly == false && 
					predictedDurationsAdaptedCorrectlyOrHalfCorrectly == false) {
					indicesOfIncorrToIncorrDur.add(i); // HIER OK
				}
			}
		}
		List<List<List<Integer>>> conflictLists = new ArrayList<List<List<Integer>>>();
		conflictLists.add(new ArrayList<List<Integer>>());
		conflictLists.get(0).add(indicesOfCorrToCorr);
		conflictLists.get(0).add(indicesOfCorrToIncorr);
		conflictLists.get(0).add(indicesOfIncorrToCorr);
		conflictLists.get(0).add(indicesOfIncorrToIncorr);
		if (modelDuration) {
			conflictLists.add(new ArrayList<List<Integer>>());
			conflictLists.get(1).add(indicesOfCorrToCorrDur);
			conflictLists.get(1).add(indicesOfCorrToIncorrDur);
			conflictLists.get(1).add(indicesOfIncorrToCorrDur);
			conflictLists.get(1).add(indicesOfIncorrToIncorrDur);
		}
		
		class Helper {
			private String getIndices(List<Integer> arg) {
				if (arg.size() == 0) {
					return "\r\n";
				} 
				else {
					return "  at indices " + arg + "\r\n";
				}
			}
		}
		StringBuffer ci = new StringBuffer(); 
//		ci.append("C O N F L I C T S" + "\r\n");
		if (voiceConflFound == true || durConflFound == true) {
//			ci.append(conflictsRecord);
			// Voice conflict
			ci.append("number of notes initially predicted correctly and reassigned correctly: " +
				indicesOfCorrToCorr.size() + "\r\n");
			ci.append(new Helper().getIndices(indicesOfCorrToCorr));
			ci.append("number of notes initially predicted correctly and reassigned incorrectly: " + 
				indicesOfCorrToIncorr.size() + "\r\n");
			ci.append(new Helper().getIndices(indicesOfCorrToIncorr));
			ci.append("number of notes initially predicted incorrectly and reassigned correctly: " +
				indicesOfIncorrToCorr.size() + "\r\n");
			ci.append(new Helper().getIndices(indicesOfIncorrToCorr));
			ci.append("number of notes initially predicted incorrectly and reassigned incorrectly: " + 
				indicesOfIncorrToIncorr.size() + "\r\n");
			ci.append(new Helper().getIndices(indicesOfIncorrToIncorr));	
		
			if (modelDuration) {	
				ci.append("number of durations initially predicted correctly and reassigned correctly: " +
					indicesOfCorrToCorrDur.size() + "\r\n");
				ci.append(new Helper().getIndices(indicesOfCorrToCorrDur));
				ci.append("number of durations initially predicted correctly and reassigned incorrectly: " + 
					indicesOfCorrToIncorrDur.size() + "\r\n"); 
				ci.append(new Helper().getIndices(indicesOfCorrToIncorrDur));
				ci.append("number of durations initially predicted incorrectly and reassigned correctly: " +
					indicesOfIncorrToCorrDur.size() + "\r\n");
				ci.append(new Helper().getIndices(indicesOfIncorrToCorrDur));
				ci.append("number of durations initially predicted incorrectly and reassigned incorrectly: " + 
					indicesOfIncorrToIncorrDur.size() + "\r\n"); 
				ci.append(new Helper().getIndices(indicesOfIncorrToIncorrDur));				
			}
		}
		else {
			ci.append("(none)\r\n");
		}
			
		ci.append("\r\n");

		return conflictLists;
	}


	/**
	 * Adds the Note(s) (listed in allNotes) to the voice(s) predicted for that Note (listed in 
	 * allPredictedVoices) at the onset time of the note in newTranscription. In the tablature case
	 * when modelling duration, the Note's predicted duration (listed in allDurationLabels) is set
	 * before the Note is added. Stores the adding details of each Note in applicationProcessRecord. 
	 * 
	 * @param noteIndex
	 */
	private void addNote(int noteIndex) {
		Map<String, Double> modelParameters = Runner.getModelParams();
		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()]; 
		ModelType mt = m.getModelType();
		DecisionContext dc = m.getDecisionContext(); 
		boolean modelDuration = m.getModelDuration();
		boolean isTablatureCase = Runner.getDataset().isTablatureSet();		
		ProcessingMode pm = 
			Runner.ALL_PROC_MODES[modelParameters.get(Runner.PROC_MODE).intValue()];
		boolean modelDurationAgain = 
			ToolBox.toBoolean(modelParameters.get(Runner.MODEL_DURATION_AGAIN).intValue());

		// Determine noteIndexBwd, which is needed when using the bwd model for the lists in
		// bwd order. When using the fwd model, it will always be the same as noteIndex
		int noteIndexBwd = noteIndex;
		if (pm == ProcessingMode.BWD) {
			for (int i = 0; i < backwardsMapping.size(); i++) {
				if (backwardsMapping.get(i) == noteIndex) {
					noteIndexBwd = i;
					break;
				}
			}
		}

		NotationSystem notationSystem = newTranscription.getPiece().getScore();
		// Get noteList, which at this point only contains one element, and get that element
		List<Note> noteList = allNotes.get(noteIndexBwd);
		Note firstNote = noteList.get(0);
		Rational onsetTime = firstNote.getMetricTime();
		// In the tablature case if duration is modelled: set the duration of the Note in noteList 
		if (dc == DecisionContext.UNIDIR && isTablatureCase && modelDuration || 
			dc == DecisionContext.BIDIR && isTablatureCase && modelDuration && modelDurationAgain) {
			// Get the predicted duration, which will only contain one element (see resolveConflicts())
			Rational[] predictedDuration = 
				DataConverter.convertIntoDuration(allDurationLabels.get(noteIndex));
			// Reset firstNote's scoreNote with the predicted duration
			ScoreNote scoreNote = 
				new ScoreNote(new ScorePitch(firstNote.getMidiPitch()), onsetTime, predictedDuration[0]);
			firstNote.setScoreNote(scoreNote);
		}
		List<Integer> predictedVoices = allPredictedVoices.get(noteIndexBwd);

		boolean ditKanWeg = true;
		if (!ditKanWeg) {
//			int highestNumVoicesTraining = modelParameters.get(Runner.HIGHEST_NUM_VOICES).intValue();
			int highestNumVoicesTraining = Runner.getHighestNumVoicesTraining();
			
			// If predictedVoices contains a voice that exceeds the number of voices from 
			// the training (which can happen because of resolved conflicts): remove that voice
			boolean printThis = true;
			for (int i = 0; i < predictedVoices.size(); i++) {
				int currentPredictedVoice = predictedVoices.get(i);
				if (currentPredictedVoice >= highestNumVoicesTraining) {
					if (verbose) {
						if (printThis) {
							System.out.println("    note " + noteIndex + 
								" (p=" + firstNote.getMidiPitch() +
								"; on=" + firstNote.getMetricTime() + 
								"; dur=" + firstNote.getMetricDuration() + ")");
							System.out.println("    predicted voice(s): " + predictedVoices);
							printThis = false;
						}
						System.out.println("    predicted voice " + currentPredictedVoice + 
							" exceeds voice range");
					}
					predictedVoices.remove(i);
					i--;
				}
			}	  
			// If predictedVoices contains no voices because of:
			// (1) the removal above 
			// (2) skipLowerNotes in resolveConflicts() (5vv non-tablature case only)
			// This can happen in the non-tablature case when modelling backward in the case of a 
			// conflict, when interrupting next notes were misassigned, which, together with any
			// lower notes, then causes all voices to be occupied
			if (predictedVoices.size() == 0) {
				// Get the original, unadapted network output. This is the one added last to 
				// allNetworkOutputs (in predictVoices()), i.e., the one added at noteIndexBwd
				double[] lastNetworkOutput = null;
				if (mt == ModelType.NN) {
					lastNetworkOutput = allNetworkOutputs.get(noteIndexBwd);
//					double[] also = allNetworkOutputs.get(allNetworkOutputs.size() - 1);
				}
				if (mt == ModelType.ENS) {
					lastNetworkOutput = allCombinedOutputs.get(noteIndexBwd);
//					double[] also = allCombinedOutputs.get(allCombinedOutputs.size() - 1);
				}

				// Get the voices that go with lastNetworkOutput and reset allPredictedVoices and 
				// allVoiceLabels, which were set to a non-existing voice in resolveConflicts() 
				List<Integer> originalVoices = 
					OutputEvaluator.interpretNetworkOutput(lastNetworkOutput, false, -1.0).get(0);
				predictedVoices.add(originalVoices.get(0));
				allPredictedVoices.set(noteIndexBwd, originalVoices);
				allVoiceLabels.set(noteIndex, DataConverter.convertIntoVoiceLabel(originalVoices));

				if (verbose) {
					System.out.println("    predicted voice(s) reset to " + originalVoices);
				}
//				throw new RuntimeException("There are no predicted voices for the note at index " + noteIndexBwd + ".");
			}
		}

		// 1. If a CoD is predicted: create a second Note (which is an exact copy of the first), add
		// that to noteList, and replace the element at noteIndex in allNotes with the augmented 
		// noteList
		if (predictedVoices.size() == 2) {
			Note secondNote = Transcription.createNote(firstNote.getMidiPitch(), 
				firstNote.getMetricTime(), firstNote.getMetricDuration());
			noteList.add(secondNote);
			allNotes.set(noteIndexBwd, noteList);
		}

		// 2. Add the Note(s) in noteList to the Transcription 	
		for (int i = 0; i < noteList.size(); i++) {
			int predictedVoice = predictedVoices.get(i);  	  		
			Note noteToAdd = noteList.get(i);

			// Check whether the predicted voice already contains a note at onsetTime
			NotationStaff notationStaff = notationSystem.get(predictedVoice);
			NotationVoice notationVoice = notationStaff.get(0);
			int seqNum = notationVoice.find(onsetTime);
			// Is seqNum non-negative? predictedVoice already contains a Note at onsetTime
			if (seqNum >= 0) {
				notesAddedToOccupiedVoice++;
				indicesOfNotesAddedToOccupiedVoice.add(noteIndexBwd);
				String message = 
					"    warning: voice " + predictedVoice + " already occupied at on=" + onsetTime;
				if (verbose) {
					System.out.println(message);
				}
				applicationProcess = applicationProcess.concat(message);
//				throw new RuntimeException(message + "."); // Removed to enable type (v) conflicts 12-3-2015
			}

			// Add Note and store information in applicationProcessRecord
			newTranscription.addNote(noteToAdd, predictedVoice, onsetTime);	  	  		  
			
			if (!ditKanWeg) {
				int chordIndex = 0; // TODO chordIndex incorrect when using bwd model
				// a. In the tablature case
				if (isTablatureCase) {
					chordIndex = basicTabSymbolProperties[noteIndex][Tablature.CHORD_SEQ_NUM];
				}
				// a. In the non-tablature case
				else {
					chordIndex = basicNoteProperties[noteIndex][Transcription.CHORD_SEQ_NUM];
				}
				String message = "Note at noteIndex " + noteIndexBwd + " (chordIndex " + chordIndex + 
					") added to voice " + predictedVoice +	" at metricTime " + onsetTime;
				if (conflictIndices.contains(noteIndexBwd)) {
					message = message.concat(" (see CONFLICTS)");
				}
				message = message.concat("\r\n");
//				System.out.println(message);
				applicationProcess = applicationProcess.concat(message);
			}
		}
	}


	/**
	 * Adds the Note(s) (listed in allNotes) to the voice(s) predicted for that Note (listed in allPredictedVoices) at
	 * the onset time of the note in newTranscription. In the tablature case when modelling duration, the Note's 
	 * predicted duration (listed in allDurationLabels) is set before the Note is added.   
	 * Stores the adding details of each Note in applicationProcessRecord. 
	 * 
	 * @param noteIndex
	 * @param highestNumberOfVoicesTraining
	 */
	private void addNoteOLD(int highestNumberOfVoicesTraining, int noteIndex) {

		Map<String, Double> modelParameters = Runner.getModelParams();
		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()]; 
		ModelType mt = m.getModelType();
		DecisionContext dc = m.getDecisionContext(); 
		boolean modelDuration = m.getModelDuration();
		
//		DatasetID di = 
//			Dataset.ALL_DATASET_IDS[modelParameters.get(Dataset.DATASET_ID).intValue()];
		boolean isTablatureCase = Runner.getDataset().isTablatureSet();		
		ProcessingMode pm = 
			Runner.ALL_PROC_MODES[modelParameters.get(Runner.PROC_MODE).intValue()];
		boolean modelDurationAgain = 
			ToolBox.toBoolean(modelParameters.get(Runner.MODEL_DURATION_AGAIN).intValue());

		// Determine noteIndexBwd, which is needed when using the bwd model for the lists in
		// bwd order. When using the fwd model, it will always be the same as noteIndex
		int noteIndexBwd = noteIndex;
		if (pm == ProcessingMode.BWD) {
			for (int i = 0; i < backwardsMapping.size(); i++) {
				if (backwardsMapping.get(i) == noteIndex) {
					noteIndexBwd = i;
					break;
				}
			}
		}

		NotationSystem notationSystem = newTranscription.getPiece().getScore();
		// Get noteList, which at this point only contains one element, and get that element
		List<Note> noteList = allNotes.get(noteIndexBwd);
		Note firstNote = noteList.get(0);
		Rational onsetTime = firstNote.getMetricTime();
		// In the tablature case if duration is modelled: set the duration of the Note in noteList 
		if (dc == DecisionContext.UNIDIR && isTablatureCase && modelDuration || 
			dc == DecisionContext.BIDIR && isTablatureCase && modelDuration && modelDurationAgain) { // EEND
//			if (isTablatureCase && modelDuration) {
			// Get the predicted duration, which will only contain one element (see resolveConflicts())
//			Rational[] predictedDuration = dataConverter.convertIntoDuration(allDurationLabels.get(noteIndexBwd)); // FUK 23-6
			Rational[] predictedDuration = DataConverter.convertIntoDuration(allDurationLabels.get(noteIndex));
			// Reset firstNote's scoreNote with the predicted duration
			ScoreNote scoreNote = new ScoreNote(new ScorePitch(firstNote.getMidiPitch()), onsetTime, predictedDuration[0]);
			firstNote.setScoreNote(scoreNote);
		}
		List<Integer> predictedVoices = allPredictedVoices.get(noteIndexBwd);

		// ********************************************************************************************
		// TEST: For quick visual comparison, print actual and predicted voice for the note at noteIndex
		// Get the actual voice(s) the current note is assigned to
//		List<Double> actVoiceLabel = groundTruthVoiceLabels.get(noteIndex);
//		List<Integer> actVoices = dataConverter.convertIntoListOfVoices(actVoiceLabel);   	  	
//		System.out.println("----> noteIndex = " + noteIndex);
//		System.out.println("----> actualVoices = " + actVoices);
//		System.out.println("----> predictedVoices = " + predictedVoices);
		// ********************************************************************************************

		boolean ditKanWeg = true;
		if (!ditKanWeg) {
			// If predictedVoices contains a voice that exceeds the number of voices from the training
			// (which can happen because of solved conflicts): remove that voice TODO OK?
			boolean printThis = true;
			for (int i = 0; i < predictedVoices.size(); i++) {
//				System.out.println("    note " + noteIndex + 
//					" (p=" + firstNote.getMidiPitch() +
//					"; on=" + firstNote.getMetricTime() + 
//					"; dur=" + firstNote.getMetricDuration() + ")");
				int currentPredictedVoice = predictedVoices.get(i);
				if (currentPredictedVoice >= highestNumberOfVoicesTraining) {
					if (verbose) {
						if (printThis) {
							System.out.println("    note " + noteIndex + 
								" (p=" + firstNote.getMidiPitch() +
								"; on=" + firstNote.getMetricTime() + 
								"; dur=" + firstNote.getMetricDuration() + ")");
							System.out.println("    predicted voice(s): " + predictedVoices);
							printThis = false;
						}
						System.out.println("    predicted voice " + currentPredictedVoice + 
							" exceeds voice range");
					}
//					System.out.println("noteIndex = " + noteIndex + "; predictedVoices = " + predictedVoices);
					predictedVoices.remove(i);
					i--;
				}
			}	  
			// If predictedVoices contains no voices because of:
			// (1) the removal above 
			// (2) skipLowerNotes in resolveConflicts() (5vv non-tablature case only)
			// This can happen in the non-tablature case when modelling backward in the case of a 
			// conflict, when interrupting next notes were misassigned, which, together with any
			// lower notes, then causes all voices to be occupied
			if (predictedVoices.size() == 0) {
//				System.out.println("        no voice predicted");
//				System.out.println("noteIndex = " + noteIndexBwd);
//				System.out.println("onsetTime = " + onsetTime);
//				predictedVoices.add(0);
				// Get the original, unadapted network output. This is the one added last to 
				// allNetworkOutputs (in predictVoices()), i.e., the one added at noteIndexBwd
				double[] lastNetworkOutput = null;
				if (mt == ModelType.NN) {
//					double[] lastNetworkOutput = allNetworkOutputs.get(noteIndexBwd);
					lastNetworkOutput = allNetworkOutputs.get(noteIndexBwd);
//					System.out.println(Arrays.toString(lastNetworkOutput));
//					double[] also = allNetworkOutputs.get(allNetworkOutputs.size() - 1);
//					System.out.println(Arrays.toString(also));
				}
				if (mt == ModelType.ENS) {
					lastNetworkOutput = allCombinedOutputs.get(noteIndexBwd);
//					System.out.println(Arrays.toString(lastNetworkOutput));
//					double[] also = allCombinedOutputs.get(allCombinedOutputs.size() - 1);
//					System.out.println(Arrays.toString(also));
				}

				// Get the voices that go with lastNetworkOutput and reset allPredictedVoices and 
				// allVoiceLabels, which were set to a non-existing voice in resolveConflicts() 
				List<Integer> originalVoices = 
					OutputEvaluator.interpretNetworkOutput(lastNetworkOutput, false, -1.0).get(0);
				predictedVoices.add(originalVoices.get(0));
				allPredictedVoices.set(noteIndexBwd, originalVoices);
				allVoiceLabels.set(noteIndex, DataConverter.convertIntoVoiceLabel(originalVoices));

//				System.out.println(Arrays.toString(lastNetworkOutput));
				if (verbose) {
					System.out.println("    predicted voice(s) reset to " + originalVoices);
				}
//				throw new RuntimeException("There are no predicted voices for the note at index " + noteIndexBwd + ".");
			}
		}

		// 1. If a CoD is predicted: create a second Note (which is an exact copy of the first), add that to noteList,
		// and replace the element at noteIndex in allNotes with the augmented noteList
		if (predictedVoices.size() == 2) {
			Note secondNote = Transcription.createNote(firstNote.getMidiPitch(), firstNote.getMetricTime(),
				firstNote.getMetricDuration());
			noteList.add(secondNote);
			allNotes.set(noteIndexBwd, noteList);
		}

		// 2. Add the Note(s) in currentNoteList to the Transcription 	
		// NB: testCorrectFunctioning is used to test whether the TestManager functions correctly in application mode TODO remove?
		boolean testCorrectFunctioning = false;
		// a. If testCorrectFunctioning is false: add the Note(s) to the voice(s) predicted for the current note
		if (testCorrectFunctioning == false) {			
			for (int i = 0; i < noteList.size(); i++) {
				int predictedVoice = predictedVoices.get(i);  	  		
				Note noteToAdd = noteList.get(i);
//				System.out.println(predictedVoice);
//				System.out.println(noteToAdd);

				// ****************************************************************************************
				// TEST: Check whether the predicted voice already contains a Note at currentNoteOnsetTime
				NotationStaff notationStaff = notationSystem.get(predictedVoice);
				NotationVoice notationVoice = notationStaff.get(0);
				int seqNum = notationVoice.find(onsetTime);
				// Is seqNum non-negative? predictedVoice already contains a Note at currentNoteOnsetTime
				if (seqNum >= 0) {
					notesAddedToOccupiedVoice++;
					indicesOfNotesAddedToOccupiedVoice.add(noteIndexBwd);
//					String message = "        note " + noteIndexBwd + ": predictedVoice (" + predictedVoice + 
//						") already contains a Note at " + onsetTime + "\r\n";
					String message = "    warning: voice " + predictedVoice + 
						" already occupied at on=" + onsetTime;
					
					if (verbose) {
//						System.out.println(noteToAdd);
						System.out.println(message);
					}
					applicationProcess = applicationProcess.concat(message);

//					System.out.println(applicationProcess);
//					throw new RuntimeException(message + "."); // Removed to enable type (v) conflicts in bwd mode for Bach data 12-3-2015
				}
				// ****************************************************************************************

				// Add Note and store information in applicationProcessRecord
//				System.out.println("noteIndex = " + noteIndex);
//				System.out.println("noteIndexBwd = " + noteIndexBwd);
//				System.out.println("noteToAdd = " + noteToAdd);
//				System.out.println("predictedVoice = " + predictedVoice);
//				System.out.println("onsetTime = " + onsetTime);

				newTranscription.addNote(noteToAdd, predictedVoice, onsetTime);	  	  		  
				
				int chordIndex = 0; // TODO chordIndex incorrect when using bwd model
				// a. In the tablature case
				if (isTablatureCase) {
					chordIndex = basicTabSymbolProperties[noteIndex][Tablature.CHORD_SEQ_NUM];
				}
				// a. In the non-tablature case
				else {
					chordIndex = basicNoteProperties[noteIndex][Transcription.CHORD_SEQ_NUM];
				}
				String message = "Note at noteIndex " + noteIndexBwd + " (chordIndex " + chordIndex + 
					") added to voice " + predictedVoice +	" at metricTime " + onsetTime;
				if (conflictIndices.contains(noteIndexBwd)) {
					message = message.concat(" (see CONFLICTS)");
				}
				message = message.concat("\r\n");
//				System.out.println(message);
				applicationProcess = applicationProcess.concat(message);
			}
		}
		// b. If testCorrectFunctioning is true: 
		//    (i)  if the predicted voice(s) are the actual voice(s): add the Note(s) to the predicted voice(s)
		//    (ii) if not: add the Note(s) to the actual voice(s), reset the voiceLabels, and list the misassignment.
		//         Since information on the voices of previous Notes is used in both training and application mode, by
		//         doing this it is made sure that in application mode the same information is used when predicting
		//         the voices for the next note(s), which leads to the same voice(s) being predicted as in training mode.
		//         In other words: by doing this, error propagation is avoided, and the applicationManager will 
		//         misassign the exact same notes as the trainingManager. In essence, it now 
		else { // TODO vereenvoudigen!  		
			List<Double> actualVoiceLabel = groundTruthVoiceLabels.get(noteIndexBwd);
			List<Integer> actualVoices = DataConverter.convertIntoListOfVoices(actualVoiceLabel); 	  	
			// Create otherNote (which is an exact copy of firstNote and secondNote)
			Note otherNote = Transcription.createNote(firstNote.getMidiPitch(), firstNote.getMetricTime(),
				firstNote.getMetricDuration());

			// 1. predictedVoices has no CoD  
			if (predictedVoices.size() == 1) {
				int predictedVoice = predictedVoices.get(0);
				// 1. If actualVoices has no CoD
				if (actualVoices.size() == 1) {
					int actualVoice = actualVoices.get(0);
					// a. If actualVoices contains the predicted voice: add firstNote to that predicted voice
					if (actualVoices.contains(predictedVoice)) {
						newTranscription.addNote(firstNote, predictedVoice, onsetTime);
					}
					// b. If not: add firstNote to the actual voice and reset allVoiceLabels
					else {
						newTranscription.addNote(firstNote, actualVoice, onsetTime);
//							allVoiceLabels.set(noteIndexBwd, groundTruthVoiceLabels.get(noteIndexBwd)); // FUK 23-6
						allVoiceLabels.set(noteIndex, groundTruthVoiceLabels.get(noteIndexBwd));
					}
				}
				// 2. If actualVoices has a CoD
				if (actualVoices.size() == 2) {
					// a. If actualVoices contains the predicted voice: add firstNote to that predicted voice; also add
					// otherNote to the other voice in actualVoices and reset allVoiceLabels
					if (actualVoices.contains(predictedVoice)) {
						newTranscription.addNote(firstNote, predictedVoice, onsetTime);
						// Determine the other voice in actualVoices
						int otherVoice = -1;
						for (int i = 0; i < actualVoices.size(); i++) {
							if (actualVoices.get(i) != predictedVoice) {
								otherVoice = actualVoices.get(i);
							}
						}
						newTranscription.addNote(otherNote, otherVoice, onsetTime);
//						allVoiceLabels.set(noteIndexBwd, groundTruthVoiceLabels.get(noteIndexBwd)); // FUK 23-6
						allVoiceLabels.set(noteIndex, groundTruthVoiceLabels.get(noteIndexBwd));
					}
					// b. If not: add firstNote and otherNote to the voices in actualVoices and reset allVoiceLabels
					else {
						newTranscription.addNote(firstNote, actualVoices.get(0), onsetTime);
						newTranscription.addNote(otherNote, actualVoices.get(1), onsetTime);
//						allVoiceLabels.set(noteIndexBwd, groundTruthVoiceLabels.get(noteIndexBwd)); // FUK 23-6
						allVoiceLabels.set(noteIndex, groundTruthVoiceLabels.get(noteIndexBwd));
					}
				}
			}
			// 2. predictedVoices has a CoD 
			if (predictedVoices.size() == 2) {
				int firstPredictedVoice = predictedVoices.get(0); 
				// 1. If actualVoices has no CoD
				if (actualVoices.size() == 1) {
					int actualVoice = actualVoices.get(0);
					// a. If actualVoices contains the first predicted voice: add firstNote to that predicted voice and
					// reset allVoiceLabels
					if (actualVoices.contains(firstPredictedVoice)) {
						newTranscription.addNote(firstNote, firstPredictedVoice, onsetTime);
//						allVoiceLabels.set(noteIndexBwd, groundTruthVoiceLabels.get(noteIndexBwd)); // FUK 23-6
						allVoiceLabels.set(noteIndex, groundTruthVoiceLabels.get(noteIndexBwd));
					}
					// b. If not: add firstNote to the actual voice and reset allVoiceLabels
					else {
						newTranscription.addNote(firstNote, actualVoice, onsetTime);
//						allVoiceLabels.set(noteIndexBwd, groundTruthVoiceLabels.get(noteIndexBwd)); // FUK 23-6
						allVoiceLabels.set(noteIndex, groundTruthVoiceLabels.get(noteIndexBwd));
					}
				}
				// 2. If actualVoices has a CoD
				if (actualVoices.size() == 2) {
					// a. If actualVoices contains the first predicted voice: add firstNote to that predicted voice; 
					// also add otherNote to the other voice in actualVoices and reset allVoiceLabels
					if (actualVoices.contains(firstPredictedVoice)) {
						newTranscription.addNote(firstNote, firstPredictedVoice, onsetTime);
						// Determine the other voice in actualVoices
						int otherVoice = -1;
						for (int i = 0; i < actualVoices.size(); i++) {
							if (actualVoices.get(i) != firstPredictedVoice) {
								otherVoice = actualVoices.get(i);
							}
						}
						newTranscription.addNote(otherNote, otherVoice, onsetTime);
//						allVoiceLabels.set(noteIndexBwd, groundTruthVoiceLabels.get(noteIndexBwd)); // FUK 23-6
						allVoiceLabels.set(noteIndex, groundTruthVoiceLabels.get(noteIndexBwd));
					}
					// b. If not: add firstNote and otherNote to the voices in actualVoices and reset allVoiceLabels
					else {
						newTranscription.addNote(firstNote, actualVoices.get(0), onsetTime);
						newTranscription.addNote(otherNote, actualVoices.get(1), onsetTime);
//						allVoiceLabels.set(noteIndexBwd, groundTruthVoiceLabels.get(noteIndexBwd)); // FUK 23-6
						allVoiceLabels.set(noteIndex, groundTruthVoiceLabels.get(noteIndexBwd));
					}
				}
			}
		}
	}
	
	
	private boolean resolveConflictsBidirectionalOUD(List<double[]> allNetworkOutputsTest, 
		List<Integer> considerSolved, int highestNumberOfVoices) {

		Map<String, Double> modelParameters = Runner.getModelParams();
			
		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()]; 
		boolean modelDuration = m.getModelDuration();
		boolean isTablatureCase = Runner.getDataset().isTablatureSet();		
		ProcessingMode pm = 
			Runner.ALL_PROC_MODES[modelParameters.get(Runner.PROC_MODE).intValue()];
		boolean printOut = true;
		boolean alsoFixSustained = false;
		if (!isTablatureCase) {
			alsoFixSustained = true;
		}

		// 0. List the chords and the indices per chords
		List<List<Integer>> indicesPerChord = null;
		if (isTablatureCase) {
//			indicesPerChord = tablature.getIndicesPerChord(pm); // reverse if bwd
			indicesPerChord = tablature.getIndicesPerChord((pm == ProcessingMode.BWD)); // reverse if bwd
		}
		else {
//			indicesPerChord = groundTruthTranscription.getIndicesPerChord(pm); // reverse if bwd
			indicesPerChord = groundTruthTranscription.getIndicesPerChord((pm == ProcessingMode.BWD)); // reverse if bwd
		}
		
		// 1. List the predicted and GT voices per chord
		List<List<List<Integer>>> voicesPerChord = new ArrayList<List<List<Integer>>>(); // reverse if bwd
		List<List<List<Integer>>> voicesPerChordGT = new ArrayList<List<List<Integer>>>(); // reverse if bwd
		// For each chord
		int numChords = indicesPerChord.size();
		int startIndex = 0;
		for (int i = 0; i < numChords; i++) {
			List<List<Integer>> voicesCurrChord = new ArrayList<List<Integer>>();
			List<List<Integer>> voicesCurrChordGT = new ArrayList<List<Integer>>();
			
			int endIndex = startIndex + indicesPerChord.get(i).size();
			// For each note in the chord at index i: add the voices predicted for it to voicesCurrChord
			// NB: works in both fwd and bwd mode because indicesPerChord is in bwd order when modelling backwards
			for (int j = startIndex; j < endIndex; j++) {
				voicesCurrChord.add(allPredictedVoices.get(j));
				voicesCurrChordGT.add(DataConverter.convertIntoListOfVoices(groundTruthVoiceLabels.get(j)));
			}
			// When the chord is traversed: add voicesCurrChord to voicesPerChord
			voicesPerChord.add(voicesCurrChord);
			voicesPerChordGT.add(voicesCurrChordGT);
			startIndex = endIndex;	
		}
				
		// 2. List all incorrect chords. There are two sorts of incorrect chords:
		// (1) sustained chords contain a note that is assigned to a voice that is 
		//     sustained from a previous chord
		// (2) double chords contain two notes that are assigned to the same voice 
		List<Integer> indicesOfIncorrChords = new ArrayList<Integer>();
		List<Integer> indicesOfChordsWithSustained = new ArrayList<Integer>(); // reverse if bwd
		List<Integer> indicesOfChordsWithDoubles = new ArrayList<Integer>(); // reverse if bwd
		List<List<Integer>> indicesOfSustainedNotesPerChord = new ArrayList<List<Integer>>();
		// allSustainedVoices/allDoublesVoices are size numChords and contain for each
		// element either a List<Integer> indicating the sustained/double voices for the note 
		// at that index, or null
		List<List<Integer>> allSustainedVoices = new ArrayList<List<Integer>>(); // reverse if bwd
		List<List<Integer>> allDoublesVoices = new ArrayList<List<Integer>>(); // reverse if bwd
		for (int i = 0; i < numChords; i++) {
			allSustainedVoices.add(null);
			allDoublesVoices.add(null);
			indicesOfSustainedNotesPerChord.add(null);
		}
		for (int i = 0; i < voicesPerChord.size(); i++) {
			int currentIndex = i;
			List<List<Integer>> currChord = voicesPerChord.get(currentIndex);
			// Make a single list with all the voices in the currentChord, including any duplicates
			List<Integer> allVoicesInCurrChord = new ArrayList<Integer>();
			for (List<Integer> n : currChord) {
				allVoicesInCurrChord.addAll(n);
			}
			
			// a. If the chord contains sustained voices
			if (isTablatureCase && modelDuration || !isTablatureCase) {
				List<Integer> sustainedVoices = new ArrayList<Integer>();
				List<Integer> noteIndicesCurrChord = indicesPerChord.get(currentIndex);
				// indexLowestNoteInChord must be in fwd order
				int indexLowestNoteInChord = noteIndicesCurrChord.get(0);
				if (pm == ProcessingMode.BWD) {
					indexLowestNoteInChord = backwardsMapping.get(noteIndicesCurrChord.get(0));
				}
				// Get the indices (these are in fwd order) of the sustained notes
				List<Integer> indicesOfSustainedNotes = 
					Transcription.getIndicesOfSustainedPreviousNotes(basicTabSymbolProperties, 
					predictedTranscription.getDurationLabels(), basicNoteProperties, 
					indexLowestNoteInChord);
				indicesOfSustainedNotesPerChord.set(currentIndex, indicesOfSustainedNotes);
				// Get the sustained voices
				for (int ind : indicesOfSustainedNotes) {
					// In the tablature case: use the voice information as predicted by the 
					// first pass model 
					if (isTablatureCase) {
//						List<Double> vl = predictedTranscription.getVoiceLabels().get(ind);// TODO: klopt dit??
//						sustainedVoices.addAll(dataConverter.convertIntoListOfVoices(vl));
						sustainedVoices.addAll(allPredictedVoices.get(ind));
					}
					// In the non-tablature case
					else {
						sustainedVoices.addAll(allPredictedVoices.get(ind));
					}
				}
				Collections.sort(sustainedVoices);
				Collections.reverse(sustainedVoices);				
				
				// For all voices v in the current chord
				for (int v : allVoicesInCurrChord) {
					// If v is a sustained voice: add, set, and break
					if (sustainedVoices.contains(v)) {
						if (!indicesOfIncorrChords.contains(currentIndex)) {// && !considerSolved.contains(currentIndex)) {
							indicesOfIncorrChords.add(currentIndex);
						}
//						if (!considerSolved.contains(currentIndex)) {
							indicesOfChordsWithSustained.add(currentIndex);
//						}
						allSustainedVoices.set(currentIndex, sustainedVoices);
						break;
					}
				}		
			}
			
			// b. If the chord contains double voices  
			List<Integer> currentDoublesVoices = new ArrayList<Integer>();
			// For all voices v in the current chord
			for (int v : allVoicesInCurrChord) {
				// Add all v occurring more than once
				if (Collections.frequency(allVoicesInCurrChord, v) > 1) {
					if (!currentDoublesVoices.contains(v)) {
						currentDoublesVoices.add(v);
					}
				}
			}
			Collections.sort(currentDoublesVoices);
			Collections.reverse(currentDoublesVoices);
			// Doubles found? Add and set
			if (currentDoublesVoices.size() != 0) {
				if (!indicesOfIncorrChords.contains(currentIndex)) {
					indicesOfIncorrChords.add(currentIndex);
				}
				indicesOfChordsWithDoubles.add(currentIndex);
				allDoublesVoices.set(currentIndex, currentDoublesVoices);
			}
		}
		
		if (printOut) {
			System.out.println("METHOD STARTED");
			System.out.println("indicesOfIncorrChords " + indicesOfIncorrChords);
			System.out.println("indicesOfChordsWithDoubles (" + indicesOfChordsWithDoubles.size() + ") " 
				+ indicesOfChordsWithDoubles);
			System.out.println("allDoublesVoices " + allDoublesVoices);	
			System.out.println("indicesOfChordsWithSustained (" + indicesOfChordsWithSustained.size() + ") " 
				+ indicesOfChordsWithSustained);
			System.out.println("allSustainedVoices " + allSustainedVoices);
		}
						
		// 3. Resolve conflicts
		// For each incorrect chord
		for (int i = 0; i < indicesOfIncorrChords.size(); i++) {
			
			int currentIndex = indicesOfIncorrChords.get(i);	
			List<List<Integer>> voicesIncorrChord = voicesPerChord.get(currentIndex); // size = numChords
			List<List<Integer>> voicesCorrChord = voicesPerChordGT.get(currentIndex); // size = numChords
			List<Integer> noteIndicesIncorrChord = indicesPerChord.get(currentIndex); // size = numChords
			if (printOut) {
				System.out.println("  currentIndex:      " + currentIndex);
				System.out.println("  voicesIncorrChord: " + voicesIncorrChord);
				System.out.println("  voicesCorrChord:   " + voicesCorrChord);
				System.out.println("  note indices:      " + noteIndicesIncorrChord);
				System.out.println("  ----------");
			}
			// 1. Determine the voices for any sustained notes
			List<Integer> currentSustainedVoices = new ArrayList<Integer>();
			List<Integer> voicesAvailableWithSustained = new ArrayList<Integer>();
			List<Integer> indicesOfNoteWithSustainedVoice = new ArrayList<Integer>();
			List<Integer> voicesForOtherChordNotes = new ArrayList<Integer>();
			if (isTablatureCase && modelDuration || !isTablatureCase) {
				if (indicesOfChordsWithSustained.contains(currentIndex)) {
					currentSustainedVoices = allSustainedVoices.get(currentIndex);

					// Determine the indices of the chord notes with the sustained voice
					for (int j = 0; j < voicesIncorrChord.size(); j++) {
						for (int v : currentSustainedVoices) {
							if (voicesIncorrChord.get(j).contains(v)) {
								int index = noteIndicesIncorrChord.get(j);
								if (!indicesOfNoteWithSustainedVoice.contains(index)) {
									indicesOfNoteWithSustainedVoice.add(index);
								}
							}
						}
					}
				}
				
				// Determine the voices for the chord notes that do not have a sustained voice (which are voices
				// not available for the notes that have the sustained voice)
				for (int j = 0; j < noteIndicesIncorrChord.size(); j++) {
					int index = noteIndicesIncorrChord.get(j);
					if (!indicesOfNoteWithSustainedVoice.contains(index)) {
						voicesForOtherChordNotes.addAll(voicesIncorrChord.get(j));
					}
				}
				
				// Determine the available voices
				for (int v = 0; v < highestNumberOfVoices; v++) {
					// Do not add sustained voices and voices for other chord notes
					if (!currentSustainedVoices.contains(v) && !voicesForOtherChordNotes.contains(v)) {
						voicesAvailableWithSustained.add(v);
					}
				}										
			}
			if (printOut) {
				System.out.println("  currentSustainedVoices:          " + currentSustainedVoices);
				System.out.println("  voicesForOtherChordNotes         " + voicesForOtherChordNotes);
				System.out.println("  indicesOfNoteWithSustainedVoice: " + indicesOfNoteWithSustainedVoice);
				System.out.println("  voicesAvailableWithSustained:    " + voicesAvailableWithSustained);
				System.out.println("  ----------");
			}
			
			// 2. Determine the voices for any double notes
			List<Integer> currentDoublesVoices = new ArrayList<Integer>();
			List<Integer> voicesAvailableWithDouble = new ArrayList<Integer>();
			List<Integer> indicesOfNoteWithDoubleVoice = new ArrayList<Integer>();
			if (indicesOfChordsWithDoubles.contains(currentIndex)) {
				currentDoublesVoices = allDoublesVoices.get(currentIndex);
				
				// Determine the indices of the chord notes with the double voice
				for (int j = 0; j < voicesIncorrChord.size(); j++) {
					for (int v : currentDoublesVoices) {
						if (voicesIncorrChord.get(j).contains(v)) {
							int index = noteIndicesIncorrChord.get(j);
							if (!indicesOfNoteWithDoubleVoice.contains(index)) {
								indicesOfNoteWithDoubleVoice.add(index);
							}
						}
					}
				}
				
				// ASSUMPTION 2: No more than two notes are assigned to the voice occurring more than once
				if (indicesOfNoteWithDoubleVoice.size() > 2) {
//						throw new RuntimeException("ERROR: More than two notes are assigned to the voice occuring " + 
//							"more than once in chord " + currentIndex);
				}
														
				// Determine all voices in the chord
				List<Integer> allCurrentVoices = new ArrayList<Integer>();
				for (List<Integer> l : voicesIncorrChord) {
					allCurrentVoices.addAll(l);
				}
				// In the non-tablature case: determine also all voices for sustained notes
				List<Integer> sustainedVoices = new ArrayList<Integer>();
				if (!isTablatureCase) {
					List<Integer> noteIndicesCurrChord = indicesPerChord.get(currentIndex);
					// indexLowestNoteInChord must be in fwd order
					int indexLowestNoteInChord = noteIndicesCurrChord.get(0);
					if (pm == ProcessingMode.BWD) {
						indexLowestNoteInChord = backwardsMapping.get(noteIndicesCurrChord.get(0));
					}
					List<Integer> indicesOfSustainedNotes = 
						Transcription.getIndicesOfSustainedPreviousNotes(basicTabSymbolProperties, 
						null, basicNoteProperties, indexLowestNoteInChord);
					// Get the sustained voices
					for (int ind : indicesOfSustainedNotes) {	
						// In the tablature case: use the voice information as predicted by the first pass model 
						if (isTablatureCase) {
//							List<Double> vl = predictedTranscription.getVoiceLabels().get(ind);// TODO: klopt dit??
//							sustainedVoices.addAll(dataConverter.convertIntoListOfVoices(vl));
							sustainedVoices.addAll(allPredictedVoices.get(ind));
						}
						// In the non-tablature case
						else {
							sustainedVoices.addAll(allPredictedVoices.get(ind));
						}
					}
				}
						
				// Determine the available voices
				for (int v = 0; v < highestNumberOfVoices; v++) {
					if (!allCurrentVoices.contains(v) && !sustainedVoices.contains(v)) {
//					if (!allCurrentVoices.contains(v)) {
						voicesAvailableWithDouble.add(v);
					}
				}			
			}
					
			if (printOut) {
				System.out.println("  currentDoublesVoices:         " + currentDoublesVoices);
				System.out.println("  indicesOfNoteWithDoubleVoice: " + indicesOfNoteWithDoubleVoice);
				System.out.println("  voicesAvailableWithDouble:    " + voicesAvailableWithDouble);
				System.out.println("  ----------");
			}	
			
			if (!isTablatureCase && indicesOfNoteWithSustainedVoice.size() > 1) {
//				throw new RuntimeException("ERROR: More than one note with a sustained voice");
			}
			if (currentDoublesVoices.size() > 2) {
				throw new RuntimeException("ERROR: More than two notes with a double voice");
			}
			
			// Determine voicesAvailable
			List<Integer> voicesAvailable = new ArrayList<Integer>();
			boolean containsSustained = indicesOfChordsWithSustained.contains(currentIndex);
			boolean containsDouble = indicesOfChordsWithDoubles.contains(currentIndex);
			// In the case of a sustained voice
			if (containsSustained && !containsDouble) {
				voicesAvailable = voicesAvailableWithSustained;
				if (printOut) {
					System.out.println("  Contains SUSTAINED");
				}
			}
			// In the case of a double voice
			else if (!containsSustained && containsDouble) {
				voicesAvailable = voicesAvailableWithDouble;
				if (printOut) {
					System.out.println("  Contains DOUBLE");
				}
			}
			// In the case of both
			else if (containsSustained && containsDouble) {
				List<Integer> availableSustained = new ArrayList<Integer>();
				// Sustained notes not taken into consideration? Add all voices to availableSustained
				if (alsoFixSustained == false) {
					for (int j = 0; j < highestNumberOfVoices; j++) {
						availableSustained.add(j); 
					}
				}
				// Else: set availableSustained to voicesAvailableWithSustained
				else {
					availableSustained = new ArrayList<Integer>(voicesAvailableWithSustained);
				}
				
				// Add only elements that are in both availableSustained and voicesAvailableWithDouble to 
				// voicesAvailable
				for (int v : availableSustained) {
					if(voicesAvailableWithDouble.contains(v)) {
						voicesAvailable.add(v);
					}
				}
				if (printOut) {
					System.out.println("  Contains SUSTAINED AND DOUBLE");
				}
			}
			Collections.sort(voicesAvailable);
			Collections.reverse(voicesAvailable);
			if (printOut) {
				System.out.println("  voicesAvailable = " + voicesAvailable);
			}
			
			// ASSUMPTION 1: There are never more available voices than the highest number of voices - 1
			if (voicesAvailable.size() > (highestNumberOfVoices - 1)) {
				throw new RuntimeException("ERROR: There are too many (" + voicesAvailable.size() + 
					") available voices in chord " + currentIndex); 
			}
								
			// a. If the chord contains a sustained note	
			if (containsSustained && alsoFixSustained && !considerSolved.contains(currentIndex)) {						
				// For each note that has a sustained voice
				for (int susIndex : indicesOfNoteWithSustainedVoice) {
					int indexToReset = susIndex;		
					List<Integer> correctedVoice = null;
					double[] networkOutput = allNetworkOutputsTest.get(susIndex);
					double highestOutput = -Double.MAX_VALUE;
					
					// Only in tablature case (?): if there are no available voices because there are too
					// many incorrectly sustained voices: reinstall all sustained notes
					// NB: The problem of having too many incorrectly sustained notes will remain (the durations 
					// are never adapted). Thus, susIndex must be added to considerSolved, so that upon restart
					// of the method the chord is no longer considered containing sustained voices
					if (voicesAvailable.isEmpty()) {	
						for (int v : currentSustainedVoices) {
							voicesAvailable.add(v);
						}
						considerSolved.add(currentIndex);
		
//							List<Integer> newVoicesAvailable = new ArrayList<Integer>();
//							// First check if any of the sustained notes is a CoD
//							List<Integer> curr = indicesOfSustainedNotesPerChord.get(currentIndex);
//							for (int c : curr) {
//								List<Double> origPredVoiceLabel = predictedTranscription.getVoiceLabels().get(c);
//								// Get the new predicted label (allPredictedVoices is in bwd order when
//								// modelling bwd) 
//								List<Double> newPredVoiceLabel =
//									dataConverter.convertIntoVoiceLabel(allPredictedVoices.get(c));
//								if (modelBackward) {
//									newPredVoiceLabel = 
//										dataConverter.convertIntoVoiceLabel(allPredictedVoices.get(backwardsMapping.indexOf(c)));
//								}
//								
//								if (Transcription.containsCoD(origPredVoiceLabel)) {
//									// Only if the newly predicted voice does not contain a CoD
//									if (!Transcription.containsCoD(newPredVoiceLabel)) {
//										// Add the first note to newVoicesAvailable
//										// NB It is better to add the firstPredicted voice (i.e., the one
//										// with the higher network output), but this information is not 
//										// available at this point
//										newVoicesAvailable.add(origPredVoiceLabel.indexOf(1.0));
//										
//									}
//								}
//							}
							
					}
					// Determine correctedVoice
					for (int av: voicesAvailable) {
						double outputForAV = networkOutput[av];
						if (outputForAV > highestOutput) {
							highestOutput = outputForAV; 
							correctedVoice = Arrays.asList(new Integer[]{av});
						}
					}
					List<Integer> oldVoice = allPredictedVoices.get(indexToReset);
					allPredictedVoices.set(indexToReset, correctedVoice);
					String conflictText = 
						getConflictText(indexToReset, oldVoice, correctedVoice, 
						containsSustained, containsDouble);
					if (printOut) {
						System.out.println("  --> " + conflictText);
					}
					conflictsRecordTest = conflictsRecordTest.concat(conflictText);
					return false;
				}
			}
				
			// b. If the chord contains (also) a double note
			if (containsDouble) {

				// For each note that has a double voice
				int indexToReset = -1;
				List<Integer> correctedVoice = null;
				
				// Always start with the first doublesVoice; if there are more, the next will
				// be dealt with in the next iteration of the method
				int currentDoubleVoice = currentDoublesVoices.get(0);
				
				// a. If voicesAvailable is empty: this is due to a CoD that is not a double
				// (e.g. [3+2, 1, 1, 0]. Remove the second predicted voice of that CoD
				if (isTablatureCase && voicesAvailable.isEmpty()) {
					for (int j = 0; j < voicesIncorrChord.size(); j++) {
						List<Integer> currentVoices = voicesIncorrChord.get(j);
						if (currentVoices.size() == 2 && !currentVoices.contains(currentDoubleVoice)) {
							indexToReset = noteIndicesIncorrChord.get(j);
							correctedVoice = Arrays.asList(new Integer[]{currentVoices.get(0)});
							List<Integer> oldVoice = allPredictedVoices.get(indexToReset);
							allPredictedVoices.set(indexToReset, correctedVoice);
								String conflictText = getConflictText(indexToReset, oldVoice, correctedVoice, 
									containsSustained, containsDouble);
																
							if (printOut) {
								System.out.println("  --> " + conflictText);
							}
							conflictsRecordTest = conflictsRecordTest.concat(conflictText);
							return false;
						}
					}
				}
					
				// b. If one of the notes in the chord has a CoD with a voice that is a 
				// double: find this CoDnote and determine indexToReset and correctedVoice
//				for (int v : currentDoublesVoices) {	
				// Find the CoDnote and adapt it
				for (int j = 0; j < voicesIncorrChord.size(); j++) {
					List<Integer> currentVoices = voicesIncorrChord.get(j);
					if (currentVoices.size() == 2) {
						indexToReset = noteIndicesIncorrChord.get(j);
						// Is v a 2nd predicted CoDnote? Remove
						if (currentVoices.get(1) == currentDoubleVoice) { // v) {
							correctedVoice = 
								Arrays.asList(new Integer[]{currentVoices.get(0)});
						}
						// Is v a 1st predicted CoDnote? Change into the second predicted CoDnote
						else if (currentVoices.get(0) == currentDoubleVoice) { // v) {
							correctedVoice = 
								Arrays.asList(new Integer[]{currentVoices.get(1)});	
						}
						// Only if correctedVoice is not null anymore, i.e., if the CoDnote was indeed a double note.
						// E.g. CoD in [2, 2, 1+0] when currentDoubleVoice = 2 is not corrected  
						if (correctedVoice != null) {
							List<Integer> oldVoice = allPredictedVoices.get(indexToReset);
							allPredictedVoices.set(indexToReset, correctedVoice);
															
							String conflictText = 
								getConflictText(indexToReset, oldVoice, correctedVoice, 
								containsSustained, containsDouble);
							if (printOut) {
								System.out.println("  --> " + conflictText);
							}
							conflictsRecordTest = conflictsRecordTest.concat(conflictText);
							return false;
						}				
					}
				}
					
//					}
					

				// c. If there are no conflicts with CoDnotes
				// Set doubleIndicesToCheck to indicesOfNoteWithDoubleVoice
				List<Integer> doubleIndicesToCheck = 
					new ArrayList<Integer>(indicesOfNoteWithDoubleVoice);
				// If there is more than one double voice (i.e., if there is more than one
				// pair of indices): determine the indices going with the first double voice
				if (currentDoublesVoices.size() > 1) {
					// Empty doubleIndicesToCheck; then add only the indices that have voice v to it 
					doubleIndicesToCheck = new ArrayList<Integer>();
					for (int v : currentDoublesVoices) {
						for (int in : indicesOfNoteWithDoubleVoice) {
							if (allPredictedVoices.get(in).contains(v)) {
								doubleIndicesToCheck.add(in);
							}
						}
						// First pair of indices found: break
						break;
					}
				}
								
				// Check the first (or only) pair of indices
				double highestOutput = -Double.MAX_VALUE;
				// For all double notes
				for (int doubleIndex : doubleIndicesToCheck) {					
					// For all available voices 
					for (int av : voicesAvailable) {
						double outputForCurrCombination = 
							allNetworkOutputsTest.get(doubleIndex)[av];
						if (outputForCurrCombination > highestOutput) {
							highestOutput = outputForCurrCombination;
							indexToReset = doubleIndex;
							correctedVoice = Arrays.asList(new Integer[]{av});
						}
					}
				}
					
				List<Integer> oldVoice = allPredictedVoices.get(indexToReset);
				allPredictedVoices.set(indexToReset, correctedVoice);
						String conflictText = getConflictText(indexToReset, oldVoice, correctedVoice, 
							containsSustained, containsDouble);

				if (printOut) {
					System.out.println("  --> " + conflictText);
				}
				conflictsRecordTest = conflictsRecordTest.concat(conflictText);
				return false;
			}	
//					// Initialise doubleIndicesToCheck and currentDoublesVoice
//					List<Integer> doubleIndicesToCheck = new ArrayList<Integer>(indicesOfNoteWithDoubleVoice);
//					int currentDoublesVoice = currentDoublesVoices.get(0);
//					// If there is more than one double voice (i.e., if there is more than one pair of indices):
//					// determine the first double voice and the indices going with it
//					if (currentDoublesVoices.size() > 1) {
//						// Empty doubleIndicesToCheck; then add only the indices that have voice v to it 
//						doubleIndicesToCheck = new ArrayList<Integer>();
//						for (int v : currentDoublesVoices) {
//							for (int in : indicesOfNoteWithDoubleVoice) {
//								if (allPredictedVoices.get(in).contains(v)) {
//									doubleIndicesToCheck.add(in);
//								}
//							}
//							// First pair of indices found: break
//							currentDoublesVoice = v;
//							break;
//						}
////							System.out.println(doubleIndicesToCheck);
//					}
//					
//					// Check if there is a CoD involved
//					int indexWithCoD = -1;
//					boolean involvesCoD = false;
//					List<Integer> coDVoices = null;
//					for (int doubleIndex : doubleIndicesToCheck) {
//						if (allPredictedVoices.get(doubleIndex).size() == 2) {
//							coDVoices = allPredictedVoices.get(doubleIndex);
//							indexWithCoD = doubleIndex;
//							involvesCoD = true;
//							break;
//						}
//					}
//					
//					// For each note that has a double voice
//					int indexToReset = -1;
//					List<Integer> correctedVoice = null;
//					
//					// If a CoD is involved
//					if (involvesCoD) {
//						indexToReset = indexWithCoD;
//						// Is currentDoubleVoice a 2nd predicted CoDnote? Remove
//						if (coDVoices.get(1) == currentDoublesVoice) {
//							correctedVoice = Arrays.asList(new Integer[]{coDVoices.get(0)});
//						}
//						// Is currentDoubleVoice a 1st predicted CoDnote? Change into the 2nd predicted CoDnote
//						if (coDVoices.get(0) == currentDoublesVoice) {
//							correctedVoice = Arrays.asList(new Integer[]{coDVoices.get(1)});
//						}
//														
//						List<Integer> oldVoice = allPredictedVoices.get(indexToReset);
//						allPredictedVoices.set(indexToReset, correctedVoice);
//						String conflictText = getConflictText(indexToReset, oldVoice, correctedVoice, 
//							containsSustained, containsDouble);
//						if (printOut) {
//							System.out.println("  --> " + conflictText);
//						}
//						conflictsRecordTest = conflictsRecordTest.concat(conflictText);
//						return false;	
//					}
//					
//					// If not
//					else {
//						
//					
//						for (int doubleIndex : doubleIndicesToCheck) {
//						
//						
//						// If not: check the first (or only) pair of indices
//						
//							double highestOutput = -Double.MAX_VALUE;
//							// For all available voices 
//							for (int av : voicesAvailable) {
//								double outputForCurrCombination = allNetworkOutputsTest.get(doubleIndex)[av];
//								if (outputForCurrCombination > highestOutput) {
//									highestOutput = outputForCurrCombination;
//									indexToReset = doubleIndex;
//									correctedVoice = Arrays.asList(new Integer[]{av});
//								}
//							}
//						}
//						List<Integer> oldVoice = allPredictedVoices.get(indexToReset);
//						allPredictedVoices.set(indexToReset, correctedVoice);
//						String conflictText = getConflictText(indexToReset, oldVoice, correctedVoice, 
//							containsSustained, containsDouble);
//						if (printOut) {
//							System.out.println("  --> " + conflictText);
//						}
//						conflictsRecordTest = conflictsRecordTest.concat(conflictText);
//						return false;	
//					}
				
		}			
		System.out.println(conflictsRecordTest);
		return true;
	}


  /**
   * When the voice assignment process is completed, all Notes will have the minimum duration (as given in 
   * the tablature) of the tablature notes they correspond to. Durations are added according to the following
   * rules, which take into consideration a maximum duration of a semibreve (half note):
   * 1. If two Notes are a semibreve apart, the first Note is extended up to the second.
   * 2. If two Notes are more than a semibreve apart, the first Note is extended up to a semibreve; the 
   *    remainder of the gap is filled with rests.
   * 3. The Notes in the final chord (which usually has a corona in the tablature) are extended so
   *    that the chord fills the remainder of the measure. 
   * 
   * NB: This method does not take into account any rests inbetween notes, which may be necessary to avoid
   * harmonic clashes or for other reasons (such as motif preservation or the appearance of rests in the 
   * model). Such rests will have to be added manually in a postprocessing phase. TODO: fix this?
   *  
   * @param numberOfVoices
   */
  private void addDurations(int numberOfVoices) {
//  	NotationSystem notationSystem = newTranscription.getScore();
  	NotationSystem notationSystem = newTranscription.getPiece().getScore();
//  	MetricalTimeLine metricalTimeLine = newTranscription.getMetricalTimeLine();
  	MetricalTimeLine metricalTimeLine = newTranscription.getPiece().getMetricalTimeLine();		
  	long[][] timeSignature = metricalTimeLine.getTimeSignature();
  	
    // For each voice in newTranscription
  	for (int i = 0; i < numberOfVoices; i++) {
  		NotationStaff currentNotationStaff = notationSystem.get(i);
   	  NotationVoice currentNotationVoice = currentNotationStaff.get(0);
   	  int numberOfNotationChordsInCurrentVoice = currentNotationVoice.size();
   	  // For each NotationChord in currentNotationVoice 
   	  for (int j = 0; j < numberOfNotationChordsInCurrentVoice; j++) {
   	  	Rational durationToAdd;
   	  	Rational semibreve = new Rational(1, 2);
   	  	
   	  	// 1. Get the NotationChord and its onset time
   	  	NotationChord currentNotationChord = currentNotationVoice.get(j);
   	  	Rational currentMetricTime = currentNotationChord.getMetricTime();
   	  	    	  	
   	  	// 2. Determine durationToAdd
   	  	// a. If currentNotationChord is not the last NotationChord in currentVoice: determine the next
   	  	// NotationChord, calculate their time difference, and set durationToAdd
   	  	if (j != numberOfNotationChordsInCurrentVoice - 1) {
   	  		// Get the onset time of the next NotationChord and calculate the time difference
   	  		NotationChord nextNotationChord = currentNotationVoice.get(j + 1);
   	  		// Determine nextMetricTime	
   	  		Rational nextMetricTime = nextNotationChord.getMetricTime();
   	  		
   	  		// TODO verwijdering terugzetten?
//     	    // Determine nextMetricTime. A loop is necessary for when nextNotationChord contains more than one
//   	  		// Note, in which case the onset time of each of these needs to be determined, and nextMetricTime
//   	  		// must be set to the largest value (i.e., the one furthest away from currentMetricTime)
//   	  		Rational nextMetricTime = new Rational(0, 1);
//   	  		for (int k = 0; k < nextNotationChord.size(); k++) {
//   	  			Note note = nextNotationChord.get(k);
//   	  			Rational metricTime = note.getMetricTime();
///    	  			if (currentMetricTime.equals(new Rational(512/32))) {
///    	  			  System.out.println("hier zoeken");
///    	  			  System.out.println("metric time of Note " + k + " = " + metricTime);
///    	  			}
//   	  			if (metricTime.isGreater(nextMetricTime)) {  	  					
//   	  				nextMetricTime = metricTime;
//   	  			}
//   	  		}
//   	  		Rational nextMetricTime = nextNotationChord.getMetricTime();
   	  		
   	  		Rational timeDifference = nextMetricTime.sub(currentMetricTime);
   	  	      	  	  
   	  		// Is timeDifference less than a semibreve? Set durationToAdd to timeDifference
   	  		if (timeDifference.isLess(semibreve)) {
   	  	    durationToAdd = timeDifference;
   	  	  }
   	  	  // Is timeDifference a semibreve or greater? Maximum note duration reached; set durationToAdd to
   	  		// that maximum value 
   	  	  else {
   	  	  	durationToAdd = semibreve; 
   	  	  }
   	  	}
   	    // b. If currentNotationChord is the last NotationChord in currentVoice: calculate the duration 
   	  	// needed to complete the measure and set durationToAdd. Calculation can be done using the formula:
   	  	// (timeSignatureNumerator - (currentBeat - 1))/timeSignatureDenominator
   	  	// Examples:
    		// on beat 1 in a 2/2 meter, this results in a duration of (2-(1-1))/2 = 2/2 = a whole note 
   	  	// on beat 2 in a 2/2 meter: this results in a duration of (2-(2-1))/2 = 1/2 = a half note    
   	  	// op beat 3 in a 4/4 meter, this results in a duration of (4-(3-1))/4 = 2/4 = a half note
   	  	// op beat 3 in a 3/4 meter, this results in a duration of (3-(3-1))/4 = 1/4 = a quarter note
   	  	else {
   	  		int timeSignatureNumerator = (int) timeSignature[0][0];
   	  		int timeSignatureDenominator = (int) timeSignature[0][1];
   	  		int currentBeat = metricalTimeLine.toMeasureBeatRemainder(currentMetricTime)[1];
     	  	durationToAdd = new Rational(timeSignatureNumerator - (currentBeat - 1), timeSignatureDenominator);
   	  	}
   	  	
   	  	// 3. Set the duration of each Note in currentNotationChord to durationToAdd
   	  	for (int k = 0; k < currentNotationChord.size(); k++) {
   	  	  Note currentNote = currentNotationChord.get(k);
   	  	  currentNote.getScoreNote().setMetricDuration(durationToAdd);
//   	  	  System.out.println("----> (1) new duration of Note " + k + " = " + currentNote.getMetricDuration());
   	  	}	   
   	  }
    }		  	  	
  }
  
  
  //TODO commented out on 9-3-2016 - remove
//  private String getConflictTextOLD(String type, int noteIndex, String metPos, int indexInChord, int noteIndexPrevious,
//		    String metPosPrevious, int indexInChordPrevious, List<Integer> predictedVoices, List<Integer> predictedVoicesPrevious,
//		    boolean sustained) {
//		    	
//		  	String voiceNum = "first";
//		  	if (type.equals("(ii)")) {
//		  		voiceNum = "second";
//		  	}
//		  	
//		  	String sustainedText = "";
//		  	if (sustained) {
//		  		sustainedText = "sustained ";
//		  	}
////		    	// Special case for interrupting next notes
//		    	if (sustained == false && type.equals("(i)")) {
////		    		sustainedText = "next ";
//		    		sustainedText = "";
//		    	}
////		  	if (interrupted) {
////		  		sustainedText = "interrupting ";
////		  	}
//		  	
//		  	String conflictText = 
//		  		"noteIndex = " + noteIndex + "\r\n" +
//		  		"  type " + type + " conflict between note " + noteIndex + " (bar: " + metPos + ", index in chord: " + indexInChord +
//		  		") and note " + noteIndexPrevious	+ " (bar: " + metPosPrevious + ", index in chord: " + indexInChordPrevious + "):\r\n";	
//		  	if (type.equals("(i)") || type.equals("(ii)")) { // || type.equals("(vi)")) {
//		  		conflictText = conflictText.concat( 
//					  "  the first predicted voice for note " + noteIndex + " (predicted voices: " + predictedVoices + 
//					  ") is the same as the " + voiceNum + " predicted voice for " + sustainedText + "note " + noteIndexPrevious 
//					  +	" (predicted voices: " + predictedVoicesPrevious + ") \r\n"
//					);
//		  	}
//		  	else if (type.equals("(iii)")) {
//		  		conflictText = conflictText.concat( 
//		  			"  the second predicted voice for note " + noteIndex + " (predicted voices: " + predictedVoices + 
//		  			") is the same as (one of) the predicted voice(s) for " + sustainedText + "note " + noteIndexPrevious +
//		  			" (predicted voices: " + predictedVoicesPrevious + ") \r\n"
//		  		);
//		  	}
//		  	
//		  	return conflictText;
//		  }
  
  
//TODO commented out on 9-3-2016 - remove
//  /**
//   * Gets the maximum duration for a note at the given metric time with the given predicted voices. The maximum 
//   * duration is determined by
//   *   1. If there is one predicted voice: the onset time of the closest next note in that voice;
//   *   2. If there are two predicted voices: the closest of the two onset times of the closest next note in the 
//   *    predicted voices. 
//   * If none of the predicted voices contains a next note, i.e., if the note is the last note in all predicted voices,
//   * or if there are no notes yet in the predicted voices, <code>null</code> is returned.   
//   * 
//   * @param metricTime
//   * @param transcription
//   * @param predictedVoices
//   * @return
//   */
//  // TESTED
//  static Rational getMaximumDurationOLD(Rational metricTime, Transcription transcription, List<Integer> 
//    predictedVoices) {
//  	Rational maxDur = null;
//  	
//    // For each predicted voice: determine the onset time of the next note in that voice
//  	List<Rational> metricTimesNext = new ArrayList<Rational>();
//		for (int i : predictedVoices) {
//	  	NotationVoice nv = transcription.getPiece().getScore().get(i).get(0);
//
//	  	// If there is no note yet in the voice: add null 
//	  	if (nv.size() == 0) {
//	  		metricTimesNext.add(null);
//	  	}
//	  	// If there are notes in the voice
//	  	else {
//  	  	Rational lastMetricTimeInVoice = nv.get(nv.size() - 1).get(0).getMetricTime();
//  	    // If the note at noteIndex is the last note in the voice: add null 
//   			if (metricTime.equals(lastMetricTimeInVoice)) {
//   				metricTimesNext.add(null);
//  	    }
//   	    // If not: add the metric time of the closest next note
//   			else {
//  	  	  for (NotationChord nc : nv) { 
//   	  		  if (nc.getMetricTime().isGreater(metricTime)) {
//   	  			  metricTimesNext.add(nc.getMetricTime());
//  	  	 		  break;
//  	  	 	  }
//  	  	  }
//  	  	}
//	  	}
//	  }
//		
//	  // 2. Determine the closest metric time
//		Rational closestMetricTimeNext = null;	
// 		// a. If there is only one predicted voice
//		if (metricTimesNext.size() == 1 && metricTimesNext.get(0) != null) {
// 			closestMetricTimeNext = metricTimesNext.get(0);
// 		}
// 		// b. If there are two predicted voices
// 		else if (metricTimesNext.size() == 2) {
// 			// If the note at noteIndex is the last note in neither of the predicted voices
// 			if (!metricTimesNext.contains(null)) {
// 	 			closestMetricTimeNext = Collections.min(metricTimesNext);
// 	 		}
// 			// If the note at noteIndex is the last note in one or in both of the predicted voices
// 	 		else {
// 	 	    // In both
// 	 			if (metricTimesNext.get(0) == null && metricTimesNext.get(1) == null) {
// 	 				closestMetricTimeNext = null;
// 	 			}
// 	 			// In one
// 	 			else {
// 	 			  int indexOfNull = metricTimesNext.indexOf(null);
// 	 			  closestMetricTimeNext = metricTimesNext.get(Math.abs(indexOfNull - 1));
// 	 			}
// 	 		}
// 		}
//	  
//		// 3. Determine the maximum duration
//		if (closestMetricTimeNext != null) {
//			maxDur = closestMetricTimeNext.sub(metricTime); 
//		}
//		return maxDur;
//  }

}
//BEGIN1
//	if (voiceEntryInfo == null) {
////System.out.println("IS NU NULL!!");
//}
//if (voiceEntryInfo != null && voiceEntryInfo.get(1).contains(noteIndex)) {	
//int v = voiceEntryInfo.get(2).get(voiceEntryInfo.get(1).indexOf(noteIndex));
//
//// Lists otherwise handled in predictVoices()
//// .add(): (allNotes), noteFeatures, allNetworkOutputs, allNetworkOutputsAdapted
//// and, if mt == ModelType.ENS: allCombinedOutputs, allMelodyModelOutputsPerModel
//// .set(): -
//noteFeatures.add(null);
//double[] outp = new double[Transcription.MAXIMUM_NUMBER_OF_VOICES];
//Arrays.fill(outp, 0.0);
//outp[v] = 1.0;
//allNetworkOutputs.add(outp);
//allNetworkOutputsAdapted.add(outp);
//if (mt == ModelType.ENS) {
//	allCombinedOutputs.add(null);
//	for (int i = 0; i < sliceIndices.size()*Runner.getNs().size(); i++) {
//		allMelodyModelOutputsPerModel.get(i).add(null);			
//	}
//}
//// Lists otherwise handled in resolveConflicts()
//// .add(): allPredictedVoices, allPredictedDurations
//// .set(): (allNotes), (allPredictedVoices), (allPredictedDurations), allVoiceLabels,
//// allDurationLabels, allVoicesCoDNotes, (allNetworkOutputs), (allNetworkOutputsAdapted)
//List<Integer> voice = Arrays.asList(new Integer[]{v});
//allPredictedVoices.add(voice); 
//allVoiceLabels.set(noteIndex, DataConverter.convertIntoVoiceLabel(voice));
//if (modelDuration) { 
//	int d = -1; // TODO figure out duration now that there is no network output
//	Rational dAsRat = new Rational(d, Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
//	Rational[] dur = new Rational[]{dAsRat}; 
//	allPredictedDurations.add(dur);
//	allDurationLabels.set(noteIndex, 
//		DataConverter.convertIntoDurationLabel(Arrays.asList(new Integer[]{d})));
//	// The element in allVoicesCoDNotes is null by default and only needs to be
//	// reset if the note at noteIndex is a CoD
//	if (voice.size() > 1) {
//		// See Transcription.handleCoDNotes()
//		int voiceLonger = -1;
//		int voiceShorter = -1;
//		Integer[] VoicesCoDNotes = new Integer[]{voiceLonger, voiceShorter};
//		allVoicesCoDNotes.set(noteIndex, VoicesCoDNotes);
//	}
//}
//}
//else {
//// 2. Generate the note feature vector for currentNote and scale it; then add it to noteFeatures
//List<Double> currentNoteFeatureVector =	null; 
////List<Double> currentMelodyModelOutput = null;
////double[] currentMelodyModelOutput = null;
//List<double[]> currentMelodyModelOutputs = null;
//if (im == Implementation.PHD) {
//	if (dc == DecisionContext.UNIDIR) {
//		currentNoteFeatureVector = 
//			featureGenerator.generateNoteFeatureVectorDISS(basicTabSymbolProperties,
//			allDurationLabels, allVoicesCoDNotes, basicNoteProperties, newTranscription, 
//			currentNote, allVoiceLabels, meterInfo, noteIndex, modelDuration, pm);
//	}
//	else {
//		// NB: allDurationLabels, allVoicesCoDNotes, and allVoiceLabels are predicted (see testInApplicationMode())
//		currentNoteFeatureVector = 
//			featureGenerator.generateBidirectionalNoteFeatureVector(basicTabSymbolProperties,
//			allDurationLabels, allVoicesCoDNotes, basicNoteProperties, newTranscription, 
//			currentNote, allVoiceLabels, meterInfo, noteIndex, modelDuration);
//	}
//	if (mt == ModelType.ENS) {
////		currentNoteFeatureVector = featureGenerator.generateNoteFeatureVectorPlus(basicTabSymbolProperties,
////			allDurationLabels, allVoicesCoDNotes, basicNoteProperties, newTranscription, currentNote, allVoiceLabels, 
////			meterInfo, noteIndex, highestNumberOfVoicesTraining, modelDuration, 
////			modelBackward, melodyPredictor);
//
////		currentMelodyModelOutput = featureGenerator.generateMelodyModelOutput(basicTabSymbolProperties, 
////			basicNoteProperties, newTranscription, currentNote, highestNumberOfVoicesTraining,
////			melodyPredictor);
//		currentMelodyModelOutputs = new ArrayList<double[]>();
//		for (int i = 0; i < sliceIndices.size()*Runner.getNs().size(); i++) { // new
//			MelodyPredictor currMp = allMelodyPredictors.get(i);
//			double[] currentMelodyModelOutput = 
//				featureGenerator.generateMelodyModelOutput(basicTabSymbolProperties, basicNoteProperties, 
//				newTranscription, currentNote, highestNumVoicesTraining, currMp);
//			allMelodyModelOutputsPerModel.get(i).add(currentMelodyModelOutput);
//			currentMelodyModelOutputs.add(currentMelodyModelOutput);
//		}
////		melodyModelOutputs.add(currentMelodyModelOutput);
//	}
//}
//else if (im == Implementation.MU_SCI) {
////	int featureSetUsedForTraining = 
////		trainingSettingsAndParameters.get(EncogNNManager.FEATURE_SET).intValue();
//	int featureSetUsedForTraining = FeatureGenerator.featureSetSwitch; // TODO
//	boolean useTablatureInformation = FeatureGenerator.tabInfoSwitch; // TODO
//	currentNoteFeatureVector = featureGenerator.generateNoteFeatureVectorMUSCI(basicTabSymbolProperties,
//		basicNoteProperties, newTranscription, currentNote, noteIndex, featureSetUsedForTraining,
//		useTablatureInformation); 
//}
////FeatureGenerator.scaleFeatureVector(currentNoteFeatureVector, minAndMaxFeatureValues, ExperimentRunner.NOTE_TO_NOTE);
//currentNoteFeatureVector = FeatureGenerator.scaleFeatureVector(currentNoteFeatureVector, 
//	minAndMaxFeatureValues, ModellingApproach.N2N);
//noteFeatures.add(currentNoteFeatureVector);
//
//// 3. Evaluate currentNoteFeatureVector and add the result, the network output, to 
//// allNetworkOutputs as well as to allNetworkOutputsAdapted
//double[] currentNetworkOutput = null;
//if (mt == ModelType.NN) {
//	currentNetworkOutput = networkManager.evalNetwork(currentNoteFeatureVector);
//}
//else if (mt == ModelType.OTHER){
//	if (noteIndex % 50 == 0) {
//		System.out.println("processing note " + noteIndex);
//	}
//
//	// Apply the model
////	String[] cmd = new String[]{
////		"python", Runner.scriptPath, m.name(), "appl", path, 
////		/*fv,*/ Runner.fvExt, Runner.clExt, Runner.outpExt};
////	String ext = Runner.fvExt + "_appl.csv";
//
////	currentNetworkOutput = PythonInterface.applyModel(cmd);
////	System.out.println(Arrays.toString(currentNetworkOutput));
//
//	boolean storeFiles = true;
//
//	if (storeFiles) {
//		// Store feature vector
//		List<List<List<Double>>> data = new ArrayList<List<List<Double>>>();
//		List<List<Double>> fvWrapped = new ArrayList<List<Double>>();
//		fvWrapped.add(currentNoteFeatureVector);
//		data.add(fvWrapped);
//		List<List<Double>> lblWrapped = new ArrayList<List<Double>>();
//		lblWrapped.add(groundTruthVoiceLabels.get(noteIndex));
//		data.add(lblWrapped);
//		TrainingManager.storeData(path, Runner.APPL, data);
//		boolean isScikit = false;
//		// For scikit
//		if (isScikit) {
//			PythonInterface.predict(new String[]{
//				path, m.name(), 
//				Runner.fvExt + "app.csv",
//				Runner.outpExt + "app.csv"
//			});
//		}
//		// For TensorFlow
//		String fvAsStr = currentNoteFeatureVector.toString();
//		String fv = fvAsStr.substring(1, fvAsStr.length()-1);
//		PythonInterface.predict(new String[]{
//			path, m.name(), fv, Runner.application
//		});
//		
//		// Retrieve the model output
//		String[][] outpCsv = 
//			ToolBox.retrieveCSVTable(ToolBox.readTextFile(new File(path + 
//			Runner.outpExt + "app.csv")));
//		List<double[]> predictedOutputs = ToolBox.convertCSVTable(outpCsv);
////		System.out.println(Arrays.toString(predictedOutputs.get(0)));
//
//		// +1.0E-6
////		for (double[] d : predictedOutputs) {
////			for (int i = 0; i < d.length; i++) {
////				if (d[i] == 0.0) {
////					d[i] += 0.000001;
////				}
////			}
////		}
//		currentNetworkOutput = predictedOutputs.get(0);			
//	}
//	else {
//		String asStr = currentNoteFeatureVector.toString();
//		String fv = asStr.substring(1, asStr.length()-1);
//
////		String fv = ""; 
////		for (int i = 0; i < currentNoteFeatureVector.size(); i++) {
////			double d = currentNoteFeatureVector.get(i);
////			fv = fv.concat(Double.toString(d));
////			if (i != currentNoteFeatureVector.size() - 1) {
////				fv = fv.concat(",");
////			}
////		}
//		currentNetworkOutput = PythonInterface.predictNoLoading(fv);
//	}
//}
//
////double[] currentNetworkOutput = networkManager.evalNetwork(currentNoteFeatureVector);		
//allNetworkOutputs.add(currentNetworkOutput); // always original NN output
//// output must be cloned so that the original network outputs (in allNetworkOutputs) remain 
//// unchanged and the adapted network outputs are added to allNetworkOutputsAdapted
//double[] copyOfCurrentNetworkOutput = null;
//if (mt == ModelType.NN || mt == ModelType.OTHER) {
//	copyOfCurrentNetworkOutput = currentNetworkOutput.clone();
//	allNetworkOutputsAdapted.add(currentNetworkOutput); // TODO allNetworkOutputsAdapted is never actually used
//}
//if (mt == ModelType.ENS) {
////	allNetworkOutputsBeforeCombining.add(currentNetworkOutput); 
//	// Calculate combined output
//	List<double[]> allOutputs = new ArrayList<double[]>();
//	allOutputs.add(currentNetworkOutput);
//	allOutputs.addAll(currentMelodyModelOutputs);
//	double[] currCombinedNetwOutp = 
//		OutputEvaluator.combineNetworkOutputs(allOutputs, modelWeighting);
////	currentNetworkOutput = currCombinedNetwOutp;
//	copyOfCurrentNetworkOutput = currCombinedNetwOutp.clone();
//	allNetworkOutputsAdapted.add(currCombinedNetwOutp); // TODO allNetworkOutputsAdapted is never actually used
//	allCombinedOutputs.add(currCombinedNetwOutp);
//}
//
////allNetworkOutputs.add(currentNetworkOutput); // always original NN output
////if (onlyNN) {
////	allNetworkOutputsAdapted.add(currentNetworkOutput);
////}
////else if (combinedModel) {
////	allNetworkOutputsAdapted.add(currCombinedNetwOutp);
////}
//
////double[] copyOfCurrentNetworkOutput = null;
////if (onlyNN) {
////	copyOfCurrentNetworkOutput = currentNetworkOutput.clone();
////}
////else if (combinedModel) {
////	copyOfCurrentNetworkOutput = currCombinedNetwOutp.clone();
////}
//
//// 4. Interpret currentNetworkOutput: resolve any conflicts with voices already taken in the
//// chord and then determine the predicted voice(s) for the current note. Add the predicted 
//// voice(s) to allPredictedVoices and the predicted voice(s)'s representation as a voice label 
//// to allVoiceLabels.
//resolveConflicts(copyOfCurrentNetworkOutput, noteIndex);
//}
//EINDE1
  

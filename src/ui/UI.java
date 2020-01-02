package ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import data.Dataset;
import data.Dataset.DatasetID;
import machineLearning.EvaluationManager;
import machineLearning.EvaluationManager.Metric;
import machineLearning.MelodyPredictor;
import machineLearning.MelodyPredictor.MelModelType;
import machineLearning.NNManager;
import machineLearning.NNManager.ActivationFunction;
import representations.Transcription;
import tools.ToolBox;
import ui.Runner.Configuration;
import ui.Runner.DecisionContext;
import ui.Runner.DecodingAlgorithm;
import ui.Runner.FeatureVector;
import ui.Runner.Model;
import ui.Runner.ModelType;
import ui.Runner.ModellingApproach;
import ui.Runner.ProcessingMode;
import ui.Runner.WeightsInit;
import featureExtraction.MelodyFeatureGenerator;
import featureExtraction.MelodyFeatureGenerator.MelodyModelFeature;

public class UI {

	private static String rootDir = "F:/research/"; // TODO also defined in MEIExport; should be in one place only

	private static boolean appliedToNewData, repeatExp, useCV, trainUserModel, verbose, estimateEntries;
	private static String expName, storedExpName, userDefinedName, datasetVersion, hyperParams;
	private static Model m, storedM;
	private static ProcessingMode pm, storedPm;
	private static Configuration config;
	private static DatasetID datasetID, datasetIDTrain; 

	private static FeatureVector fv;
	private static double lambda, hiddenLayerFactor, epsilon, keepProbability, C;
	private static int hiddenLayers, hiddenLayerSize, matrixConfiguration, n, neighbours, trees;;
	private static List<MelodyModelFeature> mmfs;
	private static List<Integer> ns;

	private static void setRootDir(String arg) {
		rootDir = arg;
	}

	public static String getRootDir() {
		return rootDir; 
	}

	public static void main(String[] args) {
		// Scenarios
		//  useCV && !appliedToNewData:	model selection phase
		//  useCV &&  appliedToNewData:	never
		// !useCV && !appliedToNewData:	application case with known GT (transfer learning) 
		// !useCV &&  appliedToNewData:	application case with unknown GT (real-world)
		
		appliedToNewData = 
			(args.length == 0 || args.length == 1 && args[0].startsWith("path=") ? false : true);
		
		// If repeating an existing experiment or conducting a new one: set parameters and settings
		if (!appliedToNewData) {
			// Settings
			boolean gridSearch = false;
			repeatExp = true;
			useCV = true;
			trainUserModel = false;
			estimateEntries = false;
			verbose = false;

//			datasetID = DatasetID.WTC_4vv;
			datasetID = DatasetID.BYRD_4vv;
//			datasetID = DatasetID.JOSQ_4vv;
//			datasetID = DatasetID.INT_3vv;

			datasetVersion = "thesis"; // only for this if

//			expName = "thesis/exp_3.2"; // publication + experiment (if applicable)
//			expName = "ISMIR-2019/";
			expName = "byrd/";

			m = Model.N;
			fv = FeatureVector.PHD_D;
			pm = ProcessingMode.BWD; // NB: bidir case must always be fwd 
			storedExpName = "thesis/exp_1";
			storedM = Model.N;
			storedPm = ProcessingMode.FWD;
//			config = Configuration.ONE; // cnf 1; "1-uni_TPM-uni_ISM/"; // WAS "1. Output (with uniform priors and transitions)" 
			config = Configuration.TWO; // cnf 2; "2-uni_TPM-data_ISM/"; // WAS "2. Output (with prior probability matrix and uniform transitions)"
//			config = Configuration.THREE; // cnf 3; "3-data_TPM-uni_ISM/"; // WAS "3. Output (with uniform priors)"
//			config = Configuration.FOUR; // cnf 4; "4-data_TPM-data_ISM/"; // WAS "4. Output (with prior probability matrix)"
//			hyperParams = "cnf=" + config.getStringRep(); // only for this if // "cnf=" + config.getStringRep(); "HLS=" + hiddenLayerSize; "KP=" + keepProbability;
//			hyperParams = "HLF=1.0/lmb=0.001/";
//			hyperParams = "eps=0.05/";
			hyperParams = "";

			mmfs = Arrays.asList(new MelodyModelFeature[]{ // used both for MM and ENS
				MelodyModelFeature.PITCH,
				MelodyModelFeature.DUR,
				MelodyModelFeature.REL_PITCH,
				MelodyModelFeature.IOI	
			});

			// Tuned hyperparameters
			// Shallow network
			lambda = 0.001; // byrd: 0.0001 with 60-80 iterations works best
			hiddenLayerFactor = 1.0;
			epsilon = 0.05;
			// DNN
			keepProbability = 0.875;
			hiddenLayers = 2;
			hiddenLayerSize = 66;
			// MM
			n = 2;
			// ENS
			// ns contains all n values used to train the MM and is only used for the ENS model
			ns = Arrays.asList(new Integer[]{1, 2, 3, 4, 5});
			// Other
			// regularisation parameter (alt.) C (LR, linear SVC); n_neighbors (kNN); n_estimators (RF)
			C = 1.0; // 0.03, 0.1, 0.3, 1.0, 3.0, 10.0, 30.0, 100.0
			neighbours = 5; // 1, 3, 5, 10, 30, 100, 300
			trees = 10; // 1, 3, 10, 30, 100, 300

			// In case of grid search: set hyperparameter space (if no hyperparameter space
			// is specified, the full space is assumed). Any values set above are overwritten
			if (gridSearch) { 
//				List<Object> hyperParameters = Arrays.asList(new Object[]{
//					hiddenLayers, 
//					hiddenLayerSize, 
//					keepProbability
//				});
				List<List<Double>> hyperParamSpace = new ArrayList<List<Double>>();
				if (hyperParamSpace.size() == 0) {
					hyperParamSpace = ToolBox.createGrid(Arrays.asList(new List[]{
						Arrays.asList(new Double[]{1., 2., 3.}), // num HL		
						Arrays.asList(new Double[]{4., 5., 6.}), // HL size
						Arrays.asList(new Double[]{7., 8., 9.}) // keep probabilities
					}));
				}
				for (List<Double> combination : hyperParamSpace) {
					hiddenLayers = combination.get(0).intValue(); 
					hiddenLayerSize = combination.get(1).intValue(); 
					keepProbability = combination.get(2);
					set(args);
				}
			}
			else {
				set(args);
			}
		}
		// If applying a trained model to new data: deduce parameters and settings  	
		else {
			// User-defined settings
			m = storedM = Model.valueOf(args[0]);
			pm = storedPm = ProcessingMode.valueOf(args[1].toUpperCase());
			datasetIDTrain = DatasetID.valueOf(args[2]); // only for this if
			rootDir = args[3];
			verbose = Boolean.parseBoolean(args[4]);
			userDefinedName = args[5]; // only for this if

			// Fixed settings
			repeatExp = false;
			useCV = false;
			trainUserModel = false;
			datasetID = DatasetID.USER;
			expName = "out";
			storedExpName = "models";

			set(args);
		}
	}


	/**
	 * 
	 * @param args If of length 1, contains the alternative root directory. If of length > 1, contains
	 *        as element 0: the model
	 *        as element 1: the processing mode  
	 *        as element 2: the training data
	 *        as element 3: the root directory or an empty string, in which case the directory
	 *                      from which this class is called is set as the root directory
	 *		  as element 4: whether the output is verbose
	 *		  as element 5: the name of the user dataset 
	 */
	public static void set(String[] args) {
		ModelType mt = m.getModelType();
		ModellingApproach ma = m.getModellingApproach();
		int	sliceIndEncoding = ToolBox.encodeListOfIntegers(MelodyFeatureGenerator.getSliceIndices(mmfs));
		int	nsEncoding = ToolBox.encodeListOfIntegers(ns);
		
		// 0. Set predefined and derived settings, and non-tuned hyperparameters
		boolean modelDurationAgain = false;
		WeightsInit wi = repeatExp ? WeightsInit.INIT_FROM_LIST : WeightsInit.INIT_RANDOM;
		ActivationFunction actFunc = (mt == ModelType.DNN) ? ActivationFunction.RELU : ActivationFunction.SIGMOID; // TODO C2C: comparator neuron is semilinear (see NNManager) 
		DecodingAlgorithm decAlg = DecodingAlgorithm.VITERBI;
		//
		int validationPercentage = (mt == ModelType.DNN) ? 20 : 0; // TODO or 20 : 20; 
		int decisionContextSize = 1;
		boolean averageProx = false;
		double maxMetaCycles = (m == Model.C) ? 60 : 40; // TODO or 60 : 80;
		double cycles = 10.0;
		int epochs = 600;
		double learningRate = (mt == ModelType.DNN) ? 0.01 : 1.0;
		double deviationThreshold = 0.05;
		int maxNumVoices = (ma == ModellingApproach.N2N) ? 5 : 4;

		// 1. Set rootDir and paths to code and data
		if (!appliedToNewData) {
			// rootDir is the directory containing the directories data/ and software/code/. Any
			// alternative rootDir is given in args[0], which must start with "path=". This 
			// alternative rootDir must also contain the directories data/ and software/code/
			if (args.length == 1) {
				setRootDir(pathify(new String[]{args[0].substring(args[0].indexOf("=")+1)}));
			}
			Runner.setPathsToCodeAndData(rootDir, false);
		}
		// "N" "FWD" "WTC_4vv" "Users/reinierdevalk/" "" "joepie"
		else {
			// rootDir is the directory containing the directory user/ (see the halcyon README). 
			// If no rootDir is provided, it is set to the current path. See
			// http://stackoverflow.com/questions/2837263/how-do-i-get-the-directory-that-the-currently-executing-jar-file-is-in
			// http://stackoverflow.com/questions/8275499/how-to-call-getclass-from-a-static-method-in-java
			if (rootDir.equals("")) {
				rootDir = new File("").getAbsolutePath();
			}
			setRootDir(pathify(new String[]{rootDir}));
			Runner.setPathsToCodeAndData(rootDir, true);
		}

		// 2. Set dataset(s)
		Dataset ds, dsTrain;
		if (!appliedToNewData) {
			ds = new Dataset(datasetID);
			dsTrain = null;
		}
		else {
			dsTrain = new Dataset(datasetIDTrain);
			datasetID.setNumVoices(datasetIDTrain.getNumVoices()); // TODO use alg that calculates num voices
			datasetID.setPieceNames(getFileNamesWithExtension(new File(Runner.midiPathUser), ".mid"));
			ds = new Dataset(datasetID);
			ds.setName(userDefinedName);
		}

		// 3. Set paths to model output
		// The paths consist of (a) Runner.resultsPath (if repeating an existing experiment or 
		// conducting a new one) or (b) Runner.userPath (if applying a trained model to new data), 
		// both of which are completed as follows (* = optional)	
		// publ			exp*		dataset		voices		model		proc mode*	hyperparams*
		// Examples: (a) 1-2; (b) 3-4 (path where the trained model is stored; output path)
		// thesis/		exp_1/		bach-WTC/	4vv/		N/			fwd/		-
		// ISMIR-2018/				bach-WTC/	4vv/		D/			fwd/		HL=2/HLS=25/KP=0.75/
		// -			models/		bach-WTC/	4vv/		N/			fwd/		-
		// -			out/		myDataset/	4vv/		N/			fwd/		WTC_4vv 
		String path = null;
		String pathStoredNN = null;
		String pathStoredMM = null;
		if (!appliedToNewData) {
			String pref = Runner.resultsPath + expName + "/" + ds.getName() + "/" + ds.getNumVoices() + "vv";
			String prefStored = pref.replace(expName, storedExpName);
			path = pathify(new String[]{pref, m.toString(), pm.getStringRep(), hyperParams});
			if (m.getDecisionContext() == DecisionContext.BIDIR) {
				pathStoredNN = pathify(new String[]{prefStored, storedM.toString(), storedPm.getStringRep()});
			}
			if (mt == ModelType.ENS) {
				pathStoredNN = pathify(new String[]{prefStored, Model.N.toString(), pm.getStringRep()});
				pathStoredMM = pathify(new String[]{prefStored, m.getMelodyModel().toString()}); 
			}
		}
		else {
			String pref = Runner.userPath + expName + "/" + ds.getName() + "/" + ds.getNumVoices() + "vv";
			String prefStored = pref.replace(expName, storedExpName).replace(ds.getName(), dsTrain.getName());
			path = pathify(new String[]{pref, m.toString(), pm.getStringRep(), dsTrain.getDatasetID().name()});	
			pathStoredNN = pathify(new String[]{prefStored, storedM.toString(), storedPm.getStringRep()});
		}

//		// 4. If applying a trained model to new data: get missing model parameters
//		if (appliedToNewData) {
//			Map<String, Double> stored =
//				ToolBox.getStoredObjectBinary(new LinkedHashMap<String, Double>(), 
//				new File(pathStoredNN + Runner.modelParameters + ".ser"));
//
//			estimateEntries = ToolBox.toBoolean(stored.get(Runner.ESTIMATE_ENTRIES).intValue());
//			fv = Runner.ALL_FEATURE_VECTORS[stored.get(Runner.FEAT_VEC).intValue()];
//			matrixConfiguration = stored.get(Runner.CONFIG).intValue();
//			//
//			lambda = stored.get(NNManager.REGULARISATION_PARAMETER);
//			hiddenLayerFactor = stored.get(Runner.HIDDEN_LAYER_FACTOR);
//			epsilon = stored.get(NNManager.MARGIN);
//			keepProbability = stored.get(Runner.KEEP_PROB);
//			hiddenLayers = stored.get(Runner.NUM_HIDDEN_LAYERS).intValue();
//			hiddenLayerSize = stored.get(Runner.HIDDEN_LAYER_SIZE).intValue();
//			n = stored.get(Runner.N).intValue();
//			sliceIndEncoding = stored.get(Runner.SLICE_IND_ENC_SINGLE_DIGIT).intValue();
//			nsEncoding = stored.get(Runner.NS_ENC_SINGLE_DIGIT).intValue();
//			C = stored.get(Runner.C);
//			neighbours = stored.get(Runner.N_NGH).intValue();
//			trees = stored.get(Runner.N_EST).intValue();
//		}

		// 4. Set model parameters  
		// The order in which items are added to a LinkedHashMap is the order in which they
		// are returned, and also in which they are printed in performance.txt
		Map<String, Double> modelParams = new LinkedHashMap<String, Double>();
		if (!appliedToNewData) {
			// a. Settings (model)
			modelParams.put(Runner.MODEL, (double) m.getIntRep());
			modelParams.put(Runner.FEAT_VEC, (m == Model.H || mt == ModelType.MM) ? null : (double) fv.getIntRep());
			modelParams.put(Runner.PROC_MODE, (double) pm.getIntRep());
			modelParams.put(Runner.SNU, (double) ToolBox.toInt(ds.isTablatureSet()));
			modelParams.put(Runner.ESTIMATE_ENTRIES, (double) ToolBox.toInt(estimateEntries));
//			modelParams.put(Runner.CONFIG, (m == Model.H) ? (double) matrixConfiguration : null);
			modelParams.put(Runner.CONFIG, (m == Model.H) ? (double) config.getIntRep() : null);
//			modelParams.put(Runner.UNIFORM_TPM, (m == Model.H) ? (double) ToolBox.toInt(uniformTPM) : null);
//			modelParams.put(Runner.UNIFORM_ISM, (m == Model.H) ? (double) ToolBox.toInt(uniformISM) : null);
			modelParams.put(Runner.SLICE_IND_ENC_SINGLE_DIGIT, (mt == ModelType.MM || mt == ModelType.ENS) ? (double) sliceIndEncoding : null);	
			// b. Settings (ML)
			modelParams.put(NNManager.ACT_FUNCTION, (mt == ModelType.NN || mt == ModelType.DNN || mt == ModelType.ENS) ? (double) actFunc.getIntRep() : null);
			modelParams.put(Runner.DECODING_ALG, (m == Model.H) ? (double) decAlg.getIntRep() : null);
			// c. Non-tuned hyperparameters 
			modelParams.put(Runner.VALIDATION_PERC, (double) validationPercentage);
			modelParams.put(Runner.DECISION_CONTEXT_SIZE, (double) decisionContextSize);
			modelParams.put(Runner.META_CYCLES, (mt == ModelType.NN || mt == ModelType.ENS) ? (double) maxMetaCycles : null);
			modelParams.put(NNManager.CYCLES, (mt == ModelType.NN || mt == ModelType.ENS) ? (double) cycles : null);
			modelParams.put(Runner.EPOCHS, (mt == ModelType.DNN) ? (double) epochs : null);
			modelParams.put(Runner.LEARNING_RATE, (mt == ModelType.NN || mt == ModelType.DNN || mt == ModelType.ENS) ? learningRate : null);
			modelParams.put(Runner.DEV_THRESHOLD, (ds.isTablatureSet() && (mt == ModelType.NN || mt == ModelType.DNN || mt == ModelType.ENS)) ? deviationThreshold : null);
			// d. Tuned hyperparameters
			modelParams.put(NNManager.REGULARISATION_PARAMETER, (mt == ModelType.NN || mt == ModelType.ENS) ? lambda : null);
			modelParams.put(Runner.KEEP_PROB, (mt == ModelType.DNN) ? keepProbability : null);
			modelParams.put(Runner.C, (m == Model.LR || m == Model.LR_CL || m == Model.LSVC_CL) ? C : null);
			modelParams.put(Runner.HIDDEN_LAYER_FACTOR, (mt == ModelType.NN || mt == ModelType.ENS) ? hiddenLayerFactor : null);
			modelParams.put(Runner.HIDDEN_LAYER_SIZE, (mt == ModelType.DNN) ? (double) hiddenLayerSize : null);
			modelParams.put(Runner.NUM_HIDDEN_LAYERS, (mt == ModelType.DNN) ? (double) hiddenLayers : null);
			modelParams.put(NNManager.MARGIN, (m == Model.C) ? epsilon : null);
			modelParams.put(Runner.N_NGH, (m == Model.kNN || m == Model.kNN_CL) ? (double) neighbours : null);
			modelParams.put(Runner.N_EST, (m == Model.RF || m == Model.RF_CL) ? (double) trees : null);
			modelParams.put(Runner.N, (mt == ModelType.MM) ? (double) n : null);
			modelParams.put(Runner.NS_ENC_SINGLE_DIGIT, (mt == ModelType.ENS) ? (double) nsEncoding : null);
			// e. Other (not printed)
			modelParams.put(Runner.MODELLING_APPROACH, (double) ma.getIntRep());
			modelParams.put(Runner.WEIGHTS_INIT, (double) wi.getIntRep());
			modelParams.put(Runner.APPL_TO_NEW_DATA, (double) ToolBox.toInt(appliedToNewData));
			modelParams.put(Runner.CROSS_VAL, (double) ToolBox.toInt(useCV));
			modelParams.put(Runner.TRAIN_USER_MODEL, (double) ToolBox.toInt(trainUserModel));
			modelParams.put(Runner.MODEL_DURATION_AGAIN, (double) ToolBox.toInt(modelDurationAgain));
			modelParams.put(Runner.AVERAGE_PROX, (double) ToolBox.toInt(averageProx));
		}
		else {
			modelParams = ToolBox.getStoredObjectBinary(new LinkedHashMap<String, Double>(), 
				new File(pathStoredNN + Runner.modelParameters + ".ser"));
//			modelParams.put(Runner.ESTIMATE_ENTRIES, (double) ToolBox.toInt(estimateEntries)); // TODO necessary?
		}

		// 5. Set metrics
		List<Metric> metricsUsed = new ArrayList<Metric>();
		metricsUsed.add(Metric.NTW_ERR);
		if (ds.isTablatureSet()) { // TODO add CRE both to if and else
			metricsUsed.add(Metric.ACC);
			metricsUsed.add(Metric.SND);
			metricsUsed.add(Metric.CMP);
//			metricsUsed.add(Metric.CRE);
			metricsUsed.add(Metric.INCORR);
			metricsUsed.add(Metric.OVERL);
			metricsUsed.add(Metric.SUPERFL);
			metricsUsed.add(Metric.HALF);
			metricsUsed.add(Metric.CNF);
			metricsUsed.add(Metric.RUNTIME);
		}
		else {
			metricsUsed.add(Metric.ACC);
			metricsUsed.add(Metric.PRC);
			metricsUsed.add(Metric.RCL);
//			metricsUsed.add(Metric.F1);
			metricsUsed.add(Metric.SND);
			metricsUsed.add(Metric.CMP);
			metricsUsed.add(Metric.AVC);
//			metricsUsed.add(Metric.CRE);
			metricsUsed.add(Metric.INCORR);
			metricsUsed.add(Metric.CNF);	
			metricsUsed.add(Metric.RUNTIME);
		}

		// 6. Run experiment
		Transcription.setMaximumNumberOfVoices(maxNumVoices);
		MelodyPredictor.setMelModelType(MelModelType.SIMPLE_LM);
		MelodyPredictor.setTermType(m.getKylmModelType());
		EvaluationManager.setMetricsUsed(metricsUsed);
		System.out.println("path         = " + path);
		System.out.println("pathStoredNN = " + pathStoredNN);
		System.out.println("pathStoredMM = " + pathStoredMM);
//		System.exit(0);
		Runner.runExperiment(modelParams, new String[]{path, pathStoredNN, pathStoredMM}, ds, dsTrain, datasetVersion, verbose);
	}


	/**
	 * Returns a list containing the names of all files with the given extension in 
	 * the given folder.
	 * 
	 * @param f The folder
	 * @param e The extension
	 * @return
	 */
	 public static List<String> getFileNamesWithExtension(File f, String e) {
		if (!e.startsWith(".")) {
			throw new RuntimeException("ERROR: The extension must start with a dot.");
		}

		List<String> fileNames = new ArrayList<String>();
		for (String s : f.list()) {
			if (s.endsWith(e)) {
				fileNames.add(s.substring(0, s.indexOf(e)));
			}
		}
		return fileNames;
	}


	public static String pathify(String[] dirs) {
		String path = "";
		for (int i = 0; i < dirs.length; i++) {
			String s = dirs[i];
			path = path.concat(s);
			// Add slash only if s does not end with one already, and, in the case of the final s,
			// additionally only if trailingSlash is true
			if ((i < dirs.length - 1 && !s.endsWith("/")) || 
				(i == dirs.length - 1 && !s.endsWith("/"))) {
				path = path.concat("/");
			}
		}
		if (path.contains("//")) {
			path = path.replace("//", "/");
		}
		return path;
	} 

}
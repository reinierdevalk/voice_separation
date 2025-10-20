package ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import data.Dataset;
import external.Transcription;
import featureExtraction.MelodyFeatureGenerator;
import featureExtraction.MelodyFeatureGenerator.MelodyModelFeature;
import interfaces.CLInterface;
import machineLearning.EvaluationManager;
import machineLearning.EvaluationManager.Metric;
import machineLearning.MelodyPredictor;
import machineLearning.MelodyPredictor.MelModelType;
import machinelearning.NNManager;
import machinelearning.NNManager.ActivationFunction;
import tools.ToolBox;

import tools.text.StringTools;
import ui.Runner.Configuration;
import ui.Runner.DecisionContext;
import ui.Runner.DecodingAlgorithm;
import ui.Runner.FeatureVector;
import ui.Runner.Model;
import ui.Runner.ModelType;
import ui.Runner.ModellingApproach;
import ui.Runner.ProcessingMode;
import ui.Runner.WeightsInit;

public class UI {

	private static enum Mode {MODEL_DEV, USER_MODEL_TRAINING, INFERENCE};
	private static Mode mode;
	private static boolean repeat;
	private static final String MODEL_PARAMS_KEY = "model_parameters";
	private static final String DATASET_KEY = "dataset";

	public final static String STORE_PATH = "STORE_PATH";
	public final static String FIRST_PASS_PATH = "FIRST_PASS_PATH";
	public final static String TRAINED_USER_MODEL_PATH = "TRAINED_USER_MODEL_PATH";
	public final static String STORED_NN_PATH = "STORED_NN_PATH";
	public final static String STORED_MM_PATH = "STORED_MM_PATH";

	public static void main(String[] args) throws IOException {
		boolean dev = args.length == 0 ? true : args[CLInterface.DEV_IND].equals(String.valueOf(true));
		Map<String, String> paths = CLInterface.getPaths(dev);
		
//		ToolBox.printMap(paths);
//		System.exit(0);

		if (args.length != 0) {
			mode = Mode.INFERENCE;
		}

		Map<String, String> runnerPaths;
		Dataset[] datasets;
		Map<String, Double> modelParams;
		List<Metric> metricsUsed;
		boolean verbose;
		boolean skipTraining = false;
		Map<String, String> cliOptsVals = null;
		if (mode != Mode.INFERENCE) {
			boolean gridSearch = false; 
			String modelDevDir = null; 
			String hyperparamsDir = null; 
			String userModelDir = null;

			System.exit(0);
			
			////////////////////////////////////////////////////////////
			
			// Choose mode and set variables
			// a. MODEL-DEV case
			mode = Mode.MODEL_DEV;
			repeat = true;
			gridSearch = false;
			skipTraining = true;
			modelDevDir = "thesis/exp_3.3.1/thesis-int/3vv/B/fwd/";
//			modelDevDir = "thesis/exp_1/thesis-int/3vv/N/fwd/";
			hyperparamsDir = "";
//			hyperparamsDir = "HL=2/HLS=66/KP=0.875-no_heur/"; 
//			hyperparamsDir = "cnf=" + config.getStringRep();
//			hyperparamsDir = "HLF=1.0/lmb=0.001/";
//			hyperparamsDir = "eps=0.05/";
//			hyperparamsDir = "LR=0.003/HL=1/HLS=66/KP=0.875/";
//			hyperparamsDir = "final/";
			verbose = true;

			// b. USER_MODEL_TRAINING case
			// mode = Mode.USER_MODEL_TRAINING;
			// userModelDir = "N-bwd-thesis-int-4vv/";
			// verbose = true;
			
			////////////////////////////////////////////////////////////

			// Set mock cliOptsVals needed for transParams
			cliOptsVals = new LinkedHashMap<String, String>();
			cliOptsVals.put(CLInterface.TUNING, CLInterface.INPUT);
			cliOptsVals.put(CLInterface.STAFF, "d");
			cliOptsVals.put(CLInterface.TABLATURE, "y");
			cliOptsVals.put(CLInterface.TYPE, CLInterface.INPUT);
			cliOptsVals.put(CLInterface.PLACEMENT, "b");

			// Get paths, datasets, modelParams, and metrics
			String jsonPath = StringTools.getPathString(
				mode == Mode.MODEL_DEV ? Arrays.asList(paths.get("EXPERIMENTS_PATH"), modelDevDir + hyperparamsDir) :
				Arrays.asList(paths.get("MODELS_PATH"), userModelDir)
			);
			Map<String, Map<String, String>> paramsFromJson = StringTools.readJSONFile(
				jsonPath + paths.get("MODEL_PARAMETERS")
			);
			runnerPaths = getPaths(paramsFromJson, paths, jsonPath);
			datasets = getDatasets(paramsFromJson, paths);
			modelParams = getModelParameters(paramsFromJson, runnerPaths);
			metricsUsed = getMetrics(paramsFromJson);

			// In case of grid search: set hyperparameter space (if no hyperparameter space is 
			// specified, the full space is assumed). Values in modelParams are overwritten
			//
			// Q: I want to write a function that creates, given n lists of values for n hyperparameters, all possible 
			// combinations of these hyperparameters. I know how to implement this if I know n beforehand (I just 
			// nest n loops), but I want the method to work for any value of n.
			// A: It’s definitely quite easy to do with recursion
			// ```
			// def combinations(lists): 
			//     return [[x] + y for x in lists[0] for y in combinations(lists[1:])] if len(lists)>1 else [[x] for x in lists[0]]
			// ```
			if (gridSearch) {
				List<List<Double>> hyperParamSpace = new ArrayList<List<Double>>();
				if (hyperParamSpace.size() == 0) {
					hyperParamSpace = ToolBox.createGrid(List.of(
//						List.of(0.003, 0.001, 0.0003),		// learningRate
						List.of(1.0, 2.0, 3.0),				// hiddenLayers
					    List.of(33.0, 50.0, 66.0, 75.0),	// hiddenLayerSize
					    List.of(0.5, 0.75, 0.875, 0.9375)	// keepProbability
					));
					for (List<Double> l : hyperParamSpace) {
						System.out.println(l);
					}
				}
				for (List<Double> combination : hyperParamSpace) {
					double alpha = modelParams.get(Runner.LEARNING_RATE);
//					double alpha = combination.get(0);
//					modelParams.put(Runner.LEARNING_RATE, alpha);
					double hiddenLayers = combination.get(0);
					modelParams.put(Runner.NUM_HIDDEN_LAYERS, hiddenLayers);
					double hiddenLayerSize = combination.get(1);
					modelParams.put(Runner.HIDDEN_LAYER_SIZE, hiddenLayerSize);
					double keepProbability = combination.get(2);
					modelParams.put(Runner.KEEP_PROB, keepProbability);

					hyperparamsDir = StringTools.getPathString(Arrays.asList(
						"LR=" + alpha, 
						"HL=" + hiddenLayers, 
						"HLS=" + hiddenLayerSize, 
						"KP=" + keepProbability
					));
				}
			}
		}
		// If deploying a trained model: deduce parameters and settings		
		// CLI usage:
		// $ java -cp <classPaths> ui.UI '<modelID>' '<rootPath>' '<verbose>' '<userDefinedName>' '<filename>' '<transParams>'
		//
		// The class ui.UI takes four arguments: 
		// - <modelID>			the trained model's ID, consisting of the model, pm, and 
		//						datasetID of the dataset it was trained on, separated by dashes
		// 						(e.g., N-bwd-bach-WTC-4vv). In case of a bidirectional model, the
		// 						unidirectional model whose output is used is added, separated
		// 						by a colon (e.g., N_B-fwd-bach-WTC-4vv:N-bwd-bach-WTC-4vv) 
		// - <rootPath>			the path from which the code is run. Should contain three dirs 
		//						- user/, containing the input and output files // TODO tool_data
		//                      - models/, containing the trained models
		// 						- code/, containing the code // TODO software/code/eclipse (dev) or nothing (user case)
		//						Is set to pwd if not provided (empty string) or specified to pwd (dot).
		//						NB: TODO A special case is the 'dev root path' F:/research/data, which
		//                      can be used locally for development and testing of a trained model, 
		//						and which does not require a code/ dir, but instead accesses the code 
		//						in its default dir F:/research/software/code/eclipse/ 
		// - <verbose>			true if verbose output is wanted, false if not (optional; can
		// 						be empty string (i.e., false)) 
		// - <userDefinedName>  a user-defined name given to the data that is processed 
		//                      (optional; can be empty string (i.e., none))
		// - <filename>			a specific .tbp file from the user/in/ folder to be transcribed; if not 
		//						provided, all .tbp files in the folder are transcribed
		//						(optional; can empty string (i.e., none)
		// - <transParams>		a string of the transcription parameters, split by |. The parameters are
		//						-tn (tuning), -k (key), -m (mode), -tb (retain tab), -tp (tab type shown). TODO names
		//						E.g. -u=A|-k=-2|-m=0|-t=y|-y=ILT 
		//
		// The -cp arg <classPaths> contains all paths to .class (in the bin/ dirs) and .jar 
		// (in the lib/ dirs) files, and these paths must be added separately. Depending on the 
		// number of dirs in <codePath>, this can result in a very long and repetitive command
		// $ java -cp '<codePath>/<dir_1>/bin;
		//             <codePath>/<dir_1>/lib/*;
		//             ...
		//             <codePath>/<dir_n>/bin;
		//             <codePath>/<dir_n>/lib/*;
		//        ui.UI <modelID> <rootPath> <verbose> <userDefinedName> <filename> <transParams>
		//
		// However, this arg can be generated using the following bash command (to be plugged 
		// into the java command), which simply adds a 'bin/' and a 'lib/*' extension to *each*
		// path p in <codePath>, and echoes a semicolon-separated concatenation of all of them
		// (even if they are non-existent)
		// $ for p in <codePath>/* ; do echo -n $p"/bin"";"$p"/lib/*"";" ; done
		//
		// where the variable $p is a path in <codePath>; $p"/bin"";"$p"/lib/*"";" is a 
		// concatenation of six strings; and the arg -n removes the newline after each echo.
		// NB: It is also possible to add the 'lib/*' extension only if p contains a lib/ dir
		// $ for p in <codePath>/* ; do echo -n $p"/bin"";" ; if [ -d $p"/lib" ] ; then echo -n $p"/lib/*"";" ; fi ; done
		//
		// 1. Example CLI command with absolute class paths
		// a. pwd is rootPath (rootPath does not have to be provided as arg)
		//    $ cd <rootPath>
		//    $ java -cp $(for p in <codePath>/* ; do echo -n $p"/bin"";"$p"/lib/*"";" ; done) 
		//           ui.UI <modelID> '' <verbose> <userDefinedName> <filename> <transParams>
		//    e.g.,
		//    $ cd /cygdrive/f/research/data/
		//    $ java -cp $(for p in F:/research/software/code/eclipse/* ; do echo -n $p"/bin"";"$p"/lib/*"";" ; done) 
		//           ui.UI D-bwd-byrd-int-4vv '' '' '' <transParams>
		// b. pwd is not rootPath (rootPath must be provided as arg)
		//    $ cd <somePath>
		//    $ java -cp $(for p in <codePath>/* ; do echo -n $p"/bin"";"$p"/lib/*"";" ; done) 
		//           ui.UI <modelID> <rootPath> <verbose> <userDefinedName> <filename> <transParams>
		//  
		// 2. Example (fictional) CLI command with relative (to the pwd) class paths; folder 
		//    structure is 
		//    <path>
		//     |-- <dirA>
		//     |    |-- <subDirA>
		//     |         |-- <dataDir> // the end of <rootPath>; from here it is three dirs up to <path>
		//     |-- <dirB>
		// 	   |    |-- <codeDir>      // the dir containing the code dirs
		//     |-- <dirC>
		//          |-- <subDirC>
		//               |-- <someDir> // a random dir; from here it is three dirs up to <path>
		// c. pwd is rootPath (rootPath does not have to be provided as arg)
		//    $ cd <path>/dirA>/<subdirA>/<dataDir>
		//    $ java -cp $(for p in <path>/<dirB>/<codeDir>/* ; do echo -n "../../../""<dirB>/<codeDir>/"${p##*/}"/bin"";""../../../""<dirB>/<codeDir>/"${p##*/}"/lib/*"";" ; done) 
		//           ui.UI <modelID> '' <verbose> <userDefinedName> <filename> <transParams>
		//    e.g.,
		//    $ cd /cygdrive/f/research/data/
		//    $ java -cp $(for p in F:/research/software/code/eclipse/* ; do echo -n "../""software/code/eclipse/"${p##*/}"/bin"";""../""software/code/eclipse/"${p##*/}"/lib/*"";" ; done) 
		//           ui.UI D-bwd-byrd-int-4vv '' '' '' <transParams>
		// d. pwd is not rootPath (rootPath must be provided as arg)
		//    $ cd <path>/dirC>/<subdirC>/<someDir>
		//    $ java -cp $(for p in <path>/<dirB>/<codeDir>/* ; do echo -n "../../../""<dirB>/<codeDir>/"${p##*/}"/bin"";""../../../""<dirB>/<codeDir>/"${p##*/}"/lib/*"";" ; done) 
		//           ui.UI <modelID> <rootPath> <verbose> <userDefinedName> <filename> <transParams>
		//
		// NB: Note the use of ${p##*/} (and not $p, as under 1a. and 1b.) to get only the last 
		//     dir in p and not the full path. See https://stackoverflow.com/questions/2107945/how-to-loop-over-directories-in-linux 
		else {
			// Parse CLI args and set variables
			List<Object> parsed = CLInterface.parseCLIArgs(
				args, StringTools.getPathString(
					Arrays.asList(paths.get("POLYPHONIST_PATH"), "in")
				)
			);
			cliOptsVals = (Map<String, String>) parsed.get(0);
			List<String> pieces = (List<String>) parsed.get(1);

			verbose = cliOptsVals.get(CLInterface.VERBOSE).equals("y") ? true : false;

//			ToolBox.printMap(cliOptsVals);
//			pieces.forEach(s -> System.out.println(s));
//			System.exit(0);

			// Get paths, datasets, modelParams, and metrics
			String jsonPath = StringTools.getPathString(
				Arrays.asList(paths.get("MODELS_PATH"), cliOptsVals.get(CLInterface.MODEL))		 
			);
			Map<String, Map<String, String>> paramsFromJson = StringTools.readJSONFile(
				jsonPath + paths.get("MODEL_PARAMETERS")
			);
			runnerPaths = getPaths(paramsFromJson, paths, jsonPath);
			Dataset.setUserPiecenames(Dataset.USER, pieces);
			datasets = getDatasets(paramsFromJson, paths);	
			modelParams = getModelParameters(paramsFromJson, runnerPaths);
			metricsUsed = getMetrics(paramsFromJson);
		}

		// Make args for setters args for runExperiment(), or make the variables superfluous 
		Model m = Runner.ALL_MODELS[modelParams.get(Runner.MODEL).intValue()]; 
		Transcription.setMaxNumVoices((m.getModellingApproach() == ModellingApproach.N2N) ? 5 : 4);
		MelodyPredictor.setMelModelType(MelModelType.SIMPLE_LM);
		MelodyPredictor.setTermType(m.getKylmModelType());
		EvaluationManager.setMetricsUsed(metricsUsed);
		
//		System.out.println("dataset                 = " + datasets[0].getPiecenames());
//		System.out.println("datasetTrain            = " + (datasets[1] == null ? datasets[1] : datasets[1].getName()));
//		System.out.println("STORE_PATH              = " + runnerPaths.get(STORE_PATH));
//		System.out.println("FIRST_PASS_PATH         = " + runnerPaths.get(FIRST_PASS_PATH));
//		System.out.println("TRAINED_USER_MODEL_PATH = " + runnerPaths.get(TRAINED_USER_MODEL_PATH));
//		System.out.println("STORED_NN_PATH          = " + runnerPaths.get(STORED_NN_PATH));
//		System.out.println("STORED_MM_PATH          = " + runnerPaths.get(STORED_MM_PATH));
//		System.exit(0);
		
		Runner.runExperiment(
			mode == Mode.USER_MODEL_TRAINING, 
			skipTraining, 
			mode == Mode.INFERENCE,
			verbose, 
			paths, 
			runnerPaths, 
			cliOptsVals,
			modelParams,
			datasets,
			Transcription.MAX_NUM_VOICES
		);
	}


	private static String getModelID(Map<String, Map<String, String>> paramsFromJson) {
		Map<String, String> dataset = paramsFromJson.get(DATASET_KEY);
		Map<String, String> params = paramsFromJson.get(MODEL_PARAMS_KEY);

		return String.join(
			"-", Arrays.asList(
				params.get("model"), params.get("processing_mode"), dataset.get("dataset_ID")
			)
		);
	}


	public static Map<String, String> getPaths(Map<String, Map<String, String>> paramsFromJson, 
		Map<String, String> paths, String jsonPath) {
		Map<String, String> runnerPaths = new LinkedHashMap<String, String>();
		
		// storePath:              where the output of the model currently run is stored 
		//                         (empty; files are added)
		//						   --> all Modes
		// pathFirstPass: 		   where the voice labels (predicted by a unidir model) needed 
		//                         for a bidir model are stored (not empty; no files are added)
		//						   --> all Modes; null when not using bidir model
		// pathTrainedUserModel:   where the weights/modelparameters of a model to reuse are 
		//                         stored (not empty; no files are added) 
		//						   --> Mode.INFERENCE only; null in other Modes 
		// pathStoredNN: 		   where the weights/modelparameters of the NN model to reuse 
		//						   are stored (not empty; no files are added)
		//						   --> Mode.MODEL_DEV and Mode.USER_MODEL_TRAINING only; null when 
		//                             not using ensemble model
		// pathStoredMM:           where the weights/modelparameters of the melody model to reuse 
		//						   are stored (not empty; no files are added) 
		//						   --> Mode.MODEL_DEV and Mode.USER_MODEL_TRAINING only; null when 
		//                             not using ensemble model
		String storePath;
		String pathFirstPass = null;
		String pathTrainedUserModel = null;
		String pathStoredNN = null;
		String pathStoredMM = null;

		Map<String, String> params = paramsFromJson.get(MODEL_PARAMS_KEY);
		Model m = Model.getModel(params.get("model"));
		if (mode != Mode.INFERENCE) {
			String expPath = paths.get("EXPERIMENTS_PATH");
			String modelsPath = paths.get("MODELS_PATH");
			String dirFirstPass = params.get("dir_first_pass");
			String dirFirstPassMM = params.get("dir_first_pass_MM");
			String modelIDFirstPass = params.get("model_ID_first_pass");

			// storePath
			storePath = jsonPath;

			// pathFirstPass, pathStoredNN, pathStoredMM
			if (mode == Mode.MODEL_DEV) {
				if (m.getDecisionContext() == DecisionContext.BIDIR) {
					pathFirstPass = StringTools.getPathString(Arrays.asList(expPath, dirFirstPass));
				}
				if (m.getModelType() == ModelType.ENS) {
					pathStoredNN = StringTools.getPathString(Arrays.asList(expPath, dirFirstPass));
					pathStoredMM = StringTools.getPathString(Arrays.asList(expPath, dirFirstPassMM));
				}
			}
			else {
				if (m.getDecisionContext() == DecisionContext.BIDIR) {
					pathFirstPass = StringTools.getPathString(Arrays.asList(modelsPath, modelIDFirstPass));
//					pathFirstPass = CLInterface.getPathString(Arrays.asList(expPath, dirFirstPass));
				}
				if (m.getModelType() == ModelType.ENS) {
					pathStoredNN = ""; // not implemented
					pathStoredMM = ""; // not implemented
				}
			}
		}
		else {
			String polyPath = StringTools.getPathString(Arrays.asList(
				paths.get("POLYPHONIST_PATH"), Runner.OUTPUT_DIR
			));
//			String modelsPath = paths.get("MODELS_PATH");
			String modelID = getModelID(paramsFromJson);
			String modelIDFirstPass = params.get("model_ID_first_pass");

			// storePath
			storePath = StringTools.getPathString(Arrays.asList(polyPath, modelID)); // current model; ex. bidir case: .../transcriber/out/D_B-fwd-byrd-int-4vv/
			
			// pathFirstPass, pathTrainedUserModel 
			if (m.getDecisionContext() == DecisionContext.BIDIR) {
//				pathFirstPass = CLInterface.getPathString(Arrays.asList(modelsPath, modelIDFirstPass)); // first-pass model, ex. bidir case: .../models/D-bwd-byrd-int-4vv/
				pathFirstPass = StringTools.getPathString(Arrays.asList(polyPath, modelIDFirstPass)); // first-pass model, ex. bidir case: .../transcriber/out/D-bwd-byrd-int-4vv/
			}
			pathTrainedUserModel = jsonPath; // trained model; ex. bidir case: .../models/D_B-fwd-byrd-int-4vv/

			// Approach bidir model with Mode.INFERENCE
			// Steps (i) and (ii): train user models (--> done with Mode.USER_MODEL_TRAINING)
			// step (i)  : train best-performing unidir model on complete dataset
			//             --> store model in models/<model_ID>/
			// step (ii) : train best-performing bidir model on complete dataset, using the labels
			//             predicted with CV (new idea: use labels predicted in training, i.e., (i))
			//			   --> store model in models/<B_model_ID>/; pathFirstPass == models/<model_ID>/
			// Steps (iii) and (iv): deploy trained user models (--> done with Mode.INFERENCE)
			// step (iii) : apply the trained unidir model from (i) to new piece(s)
			//              --> store results in transcriber/polyphonic/out/<model_ID>
			// step (iv)  : apply the trained bidir model from (ii), using the labels predicted in (iii)
			//              --> store results in transcriber/polyphonic/out/<B_model_ID>; pathFirstPass == transcriber/polyphonic/out/<model_ID>
		}

		runnerPaths.put(STORE_PATH, storePath);
		runnerPaths.put(FIRST_PASS_PATH, pathFirstPass);
		runnerPaths.put(TRAINED_USER_MODEL_PATH, pathTrainedUserModel);
		runnerPaths.put(STORED_NN_PATH, pathStoredNN);
		runnerPaths.put(STORED_MM_PATH, pathStoredMM);
		return runnerPaths;
	}


	public static Dataset[] getDatasets(Map<String, Map<String, String>> paramsFromJson, Map<String, String> paths) {
		Dataset ds;
		Dataset dsTrain = null;
		if (mode != Mode.INFERENCE) {
			String datasetID = paramsFromJson.get(DATASET_KEY).get("dataset_ID");
			ds = new Dataset(datasetID, Dataset.isTablatureSet(datasetID));
		}
		else {
			// Get the modelID of the trained model; get the ID of the dataset trained on
			String modelIDTrain = getModelID(paramsFromJson);
			String[] s = modelIDTrain.split("-");
			String datasetIDTrain = String.join("-", Arrays.asList(s[2], s[3], s[4]));

			// ds (will be populated)
			String datasetID = String.join("-", Dataset.USER, s[4]);
			ds = new Dataset(datasetID, Dataset.isTablatureSet(datasetIDTrain));

			// dsTrain (will not be populated)
			dsTrain = new Dataset(datasetIDTrain, Dataset.isTablatureSet(datasetIDTrain));
		}

		return new Dataset[]{ds, dsTrain};
	}


	public static Map<String, Double> getModelParameters(Map<String, Map<String, String>> paramsFromJson, 
		Map<String, String> runnerPaths) {
		Map<String, Double> mp = new LinkedHashMap<String, Double>();
		if (mode != Mode.INFERENCE) {
			Map<String, String> params = paramsFromJson.get(MODEL_PARAMS_KEY);

			// NB: Items must be added to mp in the order they appear in params.json (using
			// those for the N model as guide), so that the same order is retained when they 
			// are printed in performance.txt (the order in which items are added to a LinkedHashMap 
			// is the order in which they are returned). In EvaluationManager.paramsNotPrinted is 
			// specified which ones are not printed
			//
			// 1. Add items from params
			// Model
			Model m = Model.getModel(params.get("model")); // N/B, C, H, D
			mp.put(Runner.MODEL, (double) m.getIntRep());
			if (params.containsKey("feature_vector")) { // N/B, C, D
				mp.put(Runner.FEAT_VEC, (double) FeatureVector.getFeatureVector(params.get("feature_vector")).getIntRep());
			}
			if (params.containsKey("hidden_layer_factor")) { // N/B, C
				mp.put(Runner.HIDDEN_LAYER_FACTOR, Double.parseDouble(params.get("hidden_layer_factor")));
			}
			if (params.containsKey("hidden_layers")) { // D
				mp.put(Runner.NUM_HIDDEN_LAYERS, Double.parseDouble((params.get("hidden_layers"))));
			}
			if (params.containsKey("hidden_layer_size")) { // D
				mp.put(Runner.HIDDEN_LAYER_SIZE, Double.parseDouble((params.get("hidden_layer_size"))));
			}
			if (params.containsKey("activation_function")) { // N/B, C, D
				mp.put(NNManager.ACT_FUNCTION, (double) ActivationFunction.getActivationFunction(params.get("activation_function")).getIntRep()); // C: comparator neuron is semilinear (see NNManager)
			}
			// Training
			if (params.containsKey("learning_rate")) { // N/B, C, D
				mp.put(Runner.LEARNING_RATE, Double.parseDouble(params.get("learning_rate"))); // always 1.0 in shallow network case
			}
			if (params.containsKey("regularisation_parameter")) { // N/B, C
				mp.put(NNManager.REGULARISATION_PARAMETER, Double.parseDouble(params.get("regularisation_parameter")));
			}
			if (params.containsKey("margin")) { // C
				mp.put(NNManager.MARGIN, Double.parseDouble(params.get("margin")));
			}
			if (params.containsKey("keep_probability")) { // D
				mp.put(Runner.KEEP_PROB, Double.parseDouble(params.get("keep_probability")));
			}
			if (params.containsKey("metacycles")) { // N/B, C
				mp.put(Runner.META_CYCLES, Double.parseDouble(params.get("metacycles")));
			}
			if (params.containsKey("cycles")) { // N/B, C
				mp.put(NNManager.CYCLES, Double.parseDouble(params.get("cycles")));
			}
			if (params.containsKey("epochs")) { // D
				mp.put(Runner.EPOCHS, Double.parseDouble(params.get("epochs")));
			}
			if (params.containsKey("mini_batch_size")) { // N/B, C, D
				mp.put(Runner.MINI_BATCH_SIZE, Double.parseDouble(params.get("mini_batch_size")));
			}
			if (params.containsKey("validation_percentage")) { // N/B, C, H, D
				mp.put(Runner.VALIDATION_PERC, Double.parseDouble(params.get("validation_percentage"))); // must be 0 if trainUserModel
			}
			// Training, testing, inference
			if (params.containsKey("processing_mode")) { // N/B, C, H, D
				mp.put(Runner.PROC_MODE, (double) ProcessingMode.getProcessingMode(params.get("processing_mode")).getIntRep()); // NB: bidir case must always be fwd
			}
			if (params.containsKey("decision_context_size")) { // N/B, C, D
				mp.put(Runner.DECISION_CONTEXT_SIZE, Double.parseDouble(params.get("decision_context_size")));
			}
			if (params.containsKey("deviation_threshold")) { // N/B, C
				mp.put(Runner.DEV_THRESHOLD, Double.parseDouble(params.get("deviation_threshold")));
			}
			if (params.containsKey("decoding_algorithm")) { // H
				mp.put(Runner.DECODING_ALG, (double) DecodingAlgorithm.getDecodingAlgorithm(params.get("decoding_algorithm")).getIntRep());
			}
			if (params.containsKey("configuration")) { // H
				mp.put(Runner.CONFIG, (double) Configuration.getConfiguration(params.get("configuration").substring(0, 1)).getIntRep());
			}
			if (params.containsKey("voice_entry_estimation")) { // N/B, C, H, D
				mp.put(Runner.ESTIMATE_ENTRIES, (double) ToolBox.toInt(Boolean.parseBoolean(params.get("voice_entry_estimation"))));
			}
			if (params.containsKey("seed")) { // D
				mp.put(Runner.SEED, Double.parseDouble(params.get("seed"))); // 0 used for all experiments ISMIR 2018 paper
			}

			// 2. Add items derived logically
			mp.put(Runner.CROSS_VAL, (double) ToolBox.toInt((mode == Mode.USER_MODEL_TRAINING) ? false : true));
			mp.put(Runner.WEIGHTS_INIT, (double) (mode == Mode.MODEL_DEV && repeat ? WeightsInit.INIT_FROM_LIST : WeightsInit.INIT_RANDOM).getIntRep());
			mp.put(Runner.SNU, (double) ToolBox.toInt(Dataset.isTablatureSet(paramsFromJson.get(DATASET_KEY).get("dataset_ID"))));
			mp.put(Runner.ISMIR_2018, (double) ToolBox.toInt(mode == Mode.MODEL_DEV && runnerPaths.get(STORE_PATH).contains("ISMIR-2018")));
//			mp.put(Runner.ISMIR_2018, (double) ToolBox.toInt(modelDevDir.startsWith("ISMIR-2018")));
			mp.put(Runner.MODELLING_APPROACH, (double) m.getModellingApproach().getIntRep());

			// 3. Add items that are fixed
			mp.put(Runner.MODEL_DURATION_AGAIN, (double) ToolBox.toInt(false));
			mp.put(Runner.AVERAGE_PROX, (double) ToolBox.toInt(false));

			// 4. Add items that are WIP/under construction
			// MM
			if (params.containsKey("n")) {
				int n = 2;
				mp.put(Runner.N, (double) n);	
			}
			if (params.containsKey("mmfs")) {
				List<MelodyModelFeature> mmfs = Arrays.asList(new MelodyModelFeature[]{ // used both for MM and ENS 
					MelodyModelFeature.PITCH,
					MelodyModelFeature.DUR,
					MelodyModelFeature.REL_PITCH,
					MelodyModelFeature.IOI	
				});
				mp.put(Runner.SLICE_IND_ENC_SINGLE_DIGIT, (double) ToolBox.encodeListOfIntegers(MelodyFeatureGenerator.getSliceIndices(mmfs))
				);	
			}		
			// ENS
			// ns contains all n values used to train the MM and is only used for the ENS model
			if (params.containsKey("ns")) {
				List<Integer> ns = Arrays.asList(1, 2, 3, 4, 5);
				mp.put(Runner.NS_ENC_SINGLE_DIGIT, (double) ToolBox.encodeListOfIntegers(ns));
			}
			// Other
			if (params.containsKey("C")) {
				// regularisation parameter (alt.) C (LR, linear SVC); n_neighbors (kNN); n_estimators (RF)
				double C = 1.0; // 0.03, 0.1, 0.3, 1.0, 3.0, 10.0, 30.0, 100.0
				mp.put(Runner.C, C);	
			}
			if (params.containsKey("neighbours")) {
				int neighbours = 5; // 1, 3, 5, 10, 30, 100, 300
				mp.put(Runner.N_NGH, (double) neighbours);	
			}
			if (params.containsKey("trees")) {
				int trees = 10; // 1, 3, 10, 30, 100, 300
				mp.put(Runner.N_EST, (double) trees);		
			}
		}
		else {
			mp = ToolBox.getStoredObjectBinary(
				new LinkedHashMap<String, Double>(), new File(
					runnerPaths.get(TRAINED_USER_MODEL_PATH) + Runner.modelParameters + ".ser"
				)
			);
			mp.put(Runner.CROSS_VAL, (double) ToolBox.toInt(false));
			mp.put(Runner.WEIGHTS_INIT, (double) WeightsInit.INIT_FROM_LIST.getIntRep());
		}
		return mp;
	}


	public static List<Metric> getMetrics(Map<String, Map<String, String>> paramsFromJson) {
		List<Metric> metricsUsed = new ArrayList<Metric>();
		metricsUsed.add(Metric.NTW_ERR);
		if (Dataset.isTablatureSet(paramsFromJson.get(DATASET_KEY).get("dataset_ID"))) {
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

		return metricsUsed;
	}


//	/**
//	 * 
//	 * @throws IOException 
//	 */
//	public static void set(Map<String, String> paths, Map<String, String> transcriptionParams) throws IOException {
////		// 1. Set rootPath and paths to code and data
////		// a. If repeating an existing experiment or conducting a new one 
////		// rootPath is F:/research/, which contains the dirs data/annotated/ (GT data) and 
////		// software/code/eclipse/ (code)
////		// b. If deploying a trained model 
////		// rootPath is F:/research/data/, which contains the dirs user/ (unseen input data 
////		// and output of the trained model) and models/ (trained models). The paths to the code
////		// are provided via the classPath in the CLI. rootPath need not be provided if the pwd
////		// is rootPath, i.e., F:/research/data/
////		if (deployTrainedUserModel) {
////			// Do nothing if rootPath is rootPathUser; else
////			if (!rootPath.equals(rootPathUser)) {
////				// If <rootPath> arg is empty string: OK if pwd is rootPathUser
////				if (rootPath.equals("")) {
////					// http://stackoverflow.com/questions/2837263/how-do-i-get-the-directory-that-the-currently-executing-jar-file-is-in
////					String pwd = ToolBox.pathify(new String[]{new File("").getAbsolutePath()});
////					if (pwd.equals(rootPathUser)) {
////						rootPath = pwd;
////					}
////					else {
////						throw new RuntimeException("No <rootPath> provided, pwd must be " + rootPathUser);
////					}
////				}
////				// If <rootPath> arg is incorrect
////				else {
////					throw new RuntimeException("Incorrect <rootPath> provided; <rootPath> provided must be " + rootPathUser);
////				}
////			}
////		}
////		setRootPath(ToolBox.pathify(new String[]{rootPath}));
////		Runner.setPathsToCodeAndData(rootPath, deployTrainedUserModel);
//
//		// 1. Set dataset(s)
//		Dataset dsTrain, ds;
//		if (!deployTrainedUserModel) {
//			ds = new Dataset(datasetID);
//			dsTrain = null;
//		}
//		else {
//			// dsTrain remains empty (i.e., is not populated)
//			dsTrain = new Dataset(datasetIDTrain);
//
//			// ds is populated
//			ds = new Dataset(
//				Dataset.isTablatureSet(datasetIDTrain) ? Dataset.USER_TAB : Dataset.USER
//			);
//			// Set name (now default), numVoices (now 0), and pieceNames (now empty list),
//			if (!datasetName.equals("")) {
//				ds.setName(datasetName);
//			}
//			ds.setNumVoices(dsTrain.getNumVoices());
//			if (filename.equals("")) {
//				ds.addPieceNames(ToolBox.getFileNamesWithExtension(new File(
//					PathTools.getPathString(Arrays.asList(paths.get("POLYPHONIST_PATH"), "in"))),
//					ds.isTablatureSet() ? Encoding.EXTENSION : MIDIImport.EXTENSION)
//				);
//			}
//			else {
//				String filenameNoExt = filename.substring(0, filename.indexOf("."));
//				ds.addPieceNames(Arrays.asList(filenameNoExt));
//			}
//		}
//
//		// 2. Set paths for storing and retrieving
//		// Cases
//		// 1. (Re)do experiment: !deployTrainedUserModel && !trainUserModel
//		// 2. Train user model: !deployTrainedUserModel && trainUserModel
//		// 3. Do inference: deployTrainedUserModel
//		// storePath:              where the output of the model currently run is stored 
//		//                         (empty; files are added)
//		//						   --> used in all cases
//		// pathPredTransFirstPass: where the voice labels (predicted by a unidir model) needed 
//		//                         for a bidir model are stored (not empty; no files are added)
//		//						   --> used in all cases; remains null when not using bidir model
//		// pathTrainedUserModel:   where the weights/modelparameters of a model to reuse are 
//		//                         stored (not empty; no files are added) 
//		//						  --> used in case 3 only; remains null in cases 1, 2 
//		// pathStoredNN: 		   where the weights/modelparameters of the NN model to reuse 
//		//						   are stored (not empty; no files are added)
//		//						   --> used in cases 1, 2 only; remains null when not using ensemble model
//		// pathStoredMM:           where the weights/modelparameters of the melody model to reuse 
//		//						   are stored (not empty; no files are added) 
//		//						   --> used in cases 1, 2 only; remains null when not using ensemble model
//		String storePath;
//		String pathPredTransFirstPass = null;
//		String pathTrainedUserModel = null;
//		String pathStoredNN = null;
//		String pathStoredMM = null;
//		String modelsPath = paths.get("MODELS_PATH");
//		// a. If repeating an existing experiment or conducting a new one
//		if (!deployTrainedUserModel) {
//			String expPath = paths.get("EXPERIMENTS_PATH");
//			// storePath
//			if (!trainUserModel) {
//				storePath = PathTools.getPathString(
//					Arrays.asList(expPath, modelDevDir, datasetModelDir, hyperparamsDir)
//				);
//			}
//			else {
//				storePath = PathTools.getPathString(Arrays.asList(modelsPath, userModelDir));
//			}
//			// pathPredTransFirstPass
//			if (m.getDecisionContext() == DecisionContext.BIDIR) {
//				pathPredTransFirstPass = PathTools.getPathString(
//					Arrays.asList(expPath, expDirFirstPass)
//				);
//			}
//			// pathStoredNN, pathStoredMM
//			if (m.getModelType() == ModelType.ENS) {				
//				pathStoredNN = PathTools.getPathString(
//					Arrays.asList(expPath, expDirFirstPass)
//				);
//				pathStoredMM = PathTools.getPathString(
//					Arrays.asList(expPath, expDirFirstPassMM)
//				);
//			}
//		}
//		// b. If deploying a trained model
//		else {
//			String pp = PathTools.getPathString(
//				Arrays.asList(paths.get("POLYPHONIST_PATH"), "out")
//			);
//			// storePath
//			storePath = ToolBox.pathify(new String[]{pp, modelID}); // current model; ex. bidir case: .../transcriber/out/D_B-fwd-byrd-int-4vv/
//			// pathPredTransFirstPass
//			if (m.getDecisionContext() == DecisionContext.BIDIR) {
////			if (modelIDFirstPass != null) {
//				pathPredTransFirstPass = ToolBox.pathify(new String[]{pp, modelIDFirstPass}); // first-pass model, ex. bidir case: .../transcriber/out/D-bwd-byrd-int-4vv/
//			}
//			// pathTrainedUserModel
//			pathTrainedUserModel = ToolBox.pathify(new String[]{modelsPath, modelID}); // trained model; ex. bidir case: .../models/D_B-fwd-byrd-int-4vv/ 
//			
//			// Approach bidir model
//			// Steps (i) and (ii): train user models
//			// step (i)  : train best-performing unidir model on complete dataset
//			// step (ii) : train best-performing bidir model on complete dataset, using
//			//             the labels predicted with CV
//			// Steps (iii) and (iv): deploy trained user models
//			// step (iii) : apply the trained unidir model from (i) to new piece(s)
//			// step (iv)  : apply the trained bidir model from (ii), using the labels 
//			//              predicted in (iii)
//		}
//
//		// 3. Set model parameters  
//		// The order in which items are added to a LinkedHashMap is the order in which they
//		// are returned, and also in which they are printed in performance.txt
//		// In EvaluationManager.paramsNotPrinted is specified which one are not printed 
//		Map<String, Double> modelParams = new LinkedHashMap<String, Double>();
//		if (!deployTrainedUserModel) {
//			ModelType mt = m.getModelType();
//			// a. Settings (model -- own concepts)
//			modelParams.put(Runner.MODEL, (double) m.getIntRep());
//			modelParams.put(Runner.MODELLING_APPROACH, (double) m.getModellingApproach().getIntRep());
//			modelParams.put(Runner.FEAT_VEC, (m == Model.H || mt == ModelType.MM) ? null : (double) fv.getIntRep());
//			modelParams.put(Runner.PROC_MODE, (double) pm.getIntRep());
//			modelParams.put(Runner.SNU, (double) ToolBox.toInt(ds.isTablatureSet()));
//			modelParams.put(Runner.ESTIMATE_ENTRIES, (double) ToolBox.toInt(estimateEntries));
//			modelParams.put(Runner.MODEL_DURATION_AGAIN, (double) ToolBox.toInt(modelDurationAgain));
//			modelParams.put(Runner.CONFIG, (m == Model.H) ? (double) config.getIntRep() : null);
//			modelParams.put(Runner.SLICE_IND_ENC_SINGLE_DIGIT, 
//				(mt == ModelType.MM || mt == ModelType.ENS) ? (double) ToolBox.encodeListOfIntegers(MelodyFeatureGenerator.getSliceIndices(mmfs)) : null);	
//			modelParams.put(Runner.AVERAGE_PROX, (double) ToolBox.toInt(averageProx));
//			modelParams.put(Runner.ISMIR_2018, (double) ToolBox.toInt(modelDevDir.equals("ISMIR-2018")));
//			// b. Settings (ML -- existing concepts)
//			modelParams.put(NNManager.ACT_FUNCTION, 
//				(mt == ModelType.NN || mt == ModelType.DNN || mt == ModelType.ENS) ? (double) activationFunc.getIntRep() : null);
//			modelParams.put(Runner.DECODING_ALG, (m == Model.H) ? (double) decodingAlg.getIntRep() : null);
//			modelParams.put(Runner.SEED, (double) seed);
//			modelParams.put(Runner.WEIGHTS_INIT, (double) weightsInit.getIntRep());
//			modelParams.put(Runner.CROSS_VAL, (double) ToolBox.toInt(useCV));
//			// c. Tuned hyperparameters
//			modelParams.put(NNManager.REGULARISATION_PARAMETER, (mt == ModelType.NN || mt == ModelType.ENS) ? lambda : null);
//			modelParams.put(Runner.KEEP_PROB, (mt == ModelType.DNN) ? keepProbability : null);
//			modelParams.put(Runner.C, (m == Model.LR || m == Model.LR_CL || m == Model.LSVC_CL) ? C : null);
//			modelParams.put(Runner.HIDDEN_LAYER_FACTOR, (mt == ModelType.NN || mt == ModelType.ENS) ? hiddenLayerFactor : null);
//			modelParams.put(Runner.HIDDEN_LAYER_SIZE, (mt == ModelType.DNN) ? (double) hiddenLayerSize : null);
//			modelParams.put(Runner.NUM_HIDDEN_LAYERS, (mt == ModelType.DNN) ? (double) hiddenLayers : null);
//			modelParams.put(NNManager.MARGIN, (m == Model.C) ? epsilon : null);
//			modelParams.put(Runner.N_NGH, (m == Model.kNN || m == Model.kNN_CL) ? (double) neighbours : null);
//			modelParams.put(Runner.N_EST, (m == Model.RF || m == Model.RF_CL) ? (double) trees : null);
//			modelParams.put(Runner.N, (mt == ModelType.MM) ? (double) n : null);
//			modelParams.put(Runner.NS_ENC_SINGLE_DIGIT, (mt == ModelType.ENS) ? (double) ToolBox.encodeListOfIntegers(ns) : null);
//			// d. Non-tuned hyperparameters
//			modelParams.put(Runner.MINI_BATCH_SIZE, (double) miniBatchSize);
//			modelParams.put(Runner.VALIDATION_PERC, (double) validationPercentage);
//			modelParams.put(Runner.DECISION_CONTEXT_SIZE, (double) decisionContextSize);
//			modelParams.put(Runner.META_CYCLES, (mt == ModelType.NN || mt == ModelType.ENS) ? (double) maxMetaCycles : null);
//			modelParams.put(NNManager.CYCLES, (mt == ModelType.NN || mt == ModelType.ENS) ? (double) cycles : null);
//			modelParams.put(Runner.EPOCHS, (mt == ModelType.DNN) ? (double) epochs : null);
//			modelParams.put(Runner.LEARNING_RATE, (mt == ModelType.NN || mt == ModelType.DNN || mt == ModelType.ENS) ? alpha : null);
//			modelParams.put(Runner.DEV_THRESHOLD, (ds.isTablatureSet() && (mt == ModelType.NN || mt == ModelType.DNN || mt == ModelType.ENS)) ? deviationThreshold : null);
//		}
//		else {
//			modelParams = 
//				ToolBox.getStoredObjectBinary(new LinkedHashMap<String, Double>(), 
//				new File(pathTrainedUserModel + Runner.modelParameters + ".ser"));
//			// Reset necessary settings
//			modelParams.put(Runner.CROSS_VAL, (double) ToolBox.toInt(false));
//			modelParams.put(Runner.WEIGHTS_INIT, (double) WeightsInit.INIT_FROM_LIST.getIntRep());
//		}
//
//		// 4. Set metrics
//		List<Metric> metricsUsed = new ArrayList<Metric>();
//		metricsUsed.add(Metric.NTW_ERR);
//		if (ds.isTablatureSet()) { // add CRE both to if and else
//			metricsUsed.add(Metric.ACC);
//			metricsUsed.add(Metric.SND);
//			metricsUsed.add(Metric.CMP);
////			metricsUsed.add(Metric.CRE);
//			metricsUsed.add(Metric.INCORR);
//			metricsUsed.add(Metric.OVERL);
//			metricsUsed.add(Metric.SUPERFL);
//			metricsUsed.add(Metric.HALF);
//			metricsUsed.add(Metric.CNF);
//			metricsUsed.add(Metric.RUNTIME);
//		}
//		else {
//			metricsUsed.add(Metric.ACC);
//			metricsUsed.add(Metric.PRC);
//			metricsUsed.add(Metric.RCL);
////			metricsUsed.add(Metric.F1);
//			metricsUsed.add(Metric.SND);
//			metricsUsed.add(Metric.CMP);
//			metricsUsed.add(Metric.AVC);
////			metricsUsed.add(Metric.CRE);
//			metricsUsed.add(Metric.INCORR);
//			metricsUsed.add(Metric.CNF);	
//			metricsUsed.add(Metric.RUNTIME);
//		}
//
//		// 5. Run experiment
////		Model mdl = 
////			!deployTrainedUserModel ? m : Runner.ALL_MODELS[modelParams.get(Runner.MODEL).intValue()];
//		Transcription.setMaxNumVoices((m.getModellingApproach() == ModellingApproach.N2N) ? 5 : 4);
////		Transcription.setMaxNumVoices((mdl.getModellingApproach() == ModellingApproach.N2N) ? 5 : 4);
//		MelodyPredictor.setMelModelType(MelModelType.SIMPLE_LM);
//		MelodyPredictor.setTermType(m.getKylmModelType());
////		MelodyPredictor.setTermType(mdl.getKylmModelType());
//		EvaluationManager.setMetricsUsed(metricsUsed);
//		System.out.println("storePath              = " + storePath);
//		System.out.println("pathPredTransFirstPass = " + pathPredTransFirstPass);
//		System.out.println("pathTrainedUserModel   = " + pathTrainedUserModel);
//		System.out.println("pathStoredNN           = " + pathStoredNN);
//		System.out.println("pathStoredMM           = " + pathStoredMM);
////		System.exit(0);
//		Runner.setPaths(new String[]{
//			storePath, pathPredTransFirstPass, pathTrainedUserModel, pathStoredNN, pathStoredMM
//		});
//		Runner.setModelParams(modelParams);
//		Runner.setDataset(ds);
//		Runner.setDatasetTrain(dsTrain);
////		System.out.println(Runner.encodingsPath);
////		System.out.println(ds.getPieceNames());
////		System.out.println(dsTrain.getName());
////		System.exit(0);
////		System.out.println(Arrays.asList(transcriptionParams));
//
//		Runner.runExperiment(
//			trainUserModel, skipTraining, deployTrainedUserModel, verbose, paths, transcriptionParams
//		);
//	}

}
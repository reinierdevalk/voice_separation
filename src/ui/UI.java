package ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import data.Dataset;
import featureExtraction.MelodyFeatureGenerator;
import featureExtraction.MelodyFeatureGenerator.MelodyModelFeature;
import imports.MIDIImport;
import machineLearning.EvaluationManager;
import machineLearning.EvaluationManager.Metric;
import machineLearning.MelodyPredictor;
import machineLearning.MelodyPredictor.MelModelType;
import machineLearning.NNManager;
import machineLearning.NNManager.ActivationFunction;
import path.Path;
import representations.Transcription;
import tbp.Encoding;
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

public class UI {

	private static String rootPath = Path.getRootPath();
	private static String rootPathUser = Path.getRootPathUser();

	// Runner settings
	private static boolean deployTrainedUserModel, skipTraining, trainUserModel, verbose;

	// Settings and hyperparameters, model training
	private static boolean useCV, estimateEntries, modelDurationAgain, averageProx;
	private static double lambda, hiddenLayerFactor, epsilon, keepProbability, C, 
		learningRate, deviationThreshold;
	private static int hiddenLayers, hiddenLayerSize, n, neighbours, trees, cycles, 
		maxMetaCycles, decisionContextSize, epochs, validationPercentage, seed;
	private static Model m;
	private static ProcessingMode pm;
	private static FeatureVector fv;
	private static Configuration config;
	private static WeightsInit weightsInit;
	private static ActivationFunction activationFunc;
	private static DecodingAlgorithm decodingAlg;
	private static List<MelodyModelFeature> mmfs;
	private static List<Integer> ns;
	
	// Strings for path creation, model training (top) and deployment (bottom)
	private static String expDir, expDirFirstPass, hyperparams;
	private static String modelID, modelIDFirstPass;
		
	// Strings for dataset creation, model training (top) and deployment (bottom)
	private static String datasetID;
	private static String datasetIDTrain, datasetName;


	public static void main(String[] args) throws IOException {
		// Scenarios
		//  useCV && !deployTrainedModel: model selection phase
		//  useCV &&  deployTrainedModel: never
		// !useCV && !deployTrainedModel: application case with known GT (transfer learning)
		// !useCV &&  deployTrainedModel: application case with unknown GT (real-world/deploy)
		deployTrainedUserModel = args.length == 0 ? false : true;

		// If repeating an existing experiment or conducting a new one: set parameters and settings
		if (!deployTrainedUserModel) {
			
			// Settings
			// a. Runner settings
			skipTraining = false;
			trainUserModel = false;
			verbose = false;
			
			// b. Variable settings
			boolean gridSearch = false;
			useCV = true;
			estimateEntries = false;
			weightsInit = WeightsInit.INIT_FROM_LIST;
//			weightsInit = WeightsInit.INIT_RANDOM;
			//
			datasetID = Dataset.BACH_WTC_5VV;
			m = Model.D;
			pm = ProcessingMode.FWD; // NB: bidir case must always be fwd
			fv = FeatureVector.PHD_D;
			expDir = "ISMIR-2018"; // publication + experiment (if applicable)
			expDirFirstPass = "byrd/byrd-int/4vv/D/bwd/";
			//
//			config = Configuration.ONE; // cnf 1; "1-uni_TPM-uni_ISM/"; // WAS "1. Output (with uniform priors and transitions)" 
			config = Configuration.TWO; // cnf 2; "2-uni_TPM-data_ISM/"; // WAS "2. Output (with prior probability matrix and uniform transitions)"
//			config = Configuration.THREE; // cnf 3; "3-data_TPM-uni_ISM/"; // WAS "3. Output (with uniform priors)"
//			config = Configuration.FOUR; // cnf 4; "4-data_TPM-data_ISM/"; // WAS "4. Output (with prior probability matrix)"
			//
			hyperparams = "HL=2/HLS=66/KP=0.875-no_heur/";
//			hyperparams = "cnf=" + config.getStringRep(); // "HLS=" + hiddenLayerSize; "KP=" + keepProbability;
//			hyperparams = "HLF=1.0/lmb=0.001/";
//			hyperparams = "eps=0.05/";
//			hyperparams = "LR=0.003/HL=1/HLS=66/KP=0.875/";
//			hyperparams = "final/";
			//
			mmfs = Arrays.asList(new MelodyModelFeature[]{ // used both for MM and ENS
				MelodyModelFeature.PITCH,
				MelodyModelFeature.DUR,
				MelodyModelFeature.REL_PITCH,
				MelodyModelFeature.IOI	
			});
			
			// c. Predefined settings
			modelDurationAgain = false;
			averageProx = false;
			activationFunc = 
				(m.getModelType() == ModelType.DNN) ? ActivationFunction.RELU : 
				ActivationFunction.SIGMOID; // TODO C2C: comparator neuron is semilinear (see NNManager) 
			decodingAlg = DecodingAlgorithm.VITERBI;

			// Hyperparameters
			// a. Tuned hyperparameters
			// Shallow network
			lambda = 0.001; // regularisation parameter  
			hiddenLayerFactor = 1.0;
			epsilon = 0.05;
			// DNN
			keepProbability = 0.875;
			hiddenLayers = 2;
			hiddenLayerSize = 66;
			double alpha = 0.01; // learning rate (1.0 in shallow network case)
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

			// b. Non-tuned hyperparameters 
			// Shallow network
			maxMetaCycles = (m == Model.C) ? 60 : 40;
			cycles = 10;
			// DNN
			seed = 0; // seed = 0 used for all experiments ISMIR 2018 paper
			epochs = 600;
			// General
			learningRate = (m.getModelType() == ModelType.DNN) ? alpha : 1.0;
			deviationThreshold = 0.05; // 0.05;
			validationPercentage = (m.getModelType() == ModelType.DNN) ? 20 : 0; // 10 : 0;
			if (trainUserModel) {
				validationPercentage = 0;
			}
			decisionContextSize = 1;

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
//						Arrays.asList(new Double[]{0.003, 0.001, 0.0003}), // learningRate
						Arrays.asList(new Double[]{1., 2., 3.}), // hiddenLayers
						Arrays.asList(new Double[]{33., 50., 66., 75.}), // hiddenLayerSize
						Arrays.asList(new Double[]{.5, .75, .875, .9375}), // keepProbability
					}));
					for (List<Double> l : hyperParamSpace) {
						System.out.println(l);
					}
				}
				for (List<Double> combination : hyperParamSpace) {
//					alpha = combination.get(0);
					hiddenLayers = combination.get(0).intValue();
					hiddenLayerSize = combination.get(1).intValue(); 
					keepProbability = combination.get(2);

					hyperparams = ToolBox.pathify(new String[]{
						"LR=" + alpha, "HL=" + hiddenLayers, "HLS=" + hiddenLayerSize, 
						"KP=" + keepProbability});
					set();
				}
			}
			else {
				set();
			}
		}
		// If deploying a trained model: deduce parameters and settings		
		// CLI usage:
		// $ java -cp <classPaths> ui.UI <model> <pm> <dsIDTrain> '<rootPath>' '<verbose>' '<userDefinedName>'
		//
		// The class ui.UI takes four arguments: 
		// - <modelID>			the trained model's ID, consisting of the model, pm, and 
		//						datasetID of the dataset it was trained on, separated by dashes
		// 						(e.g., N-bwd-bach-WTC-4vv). In case of a bidirectional model, the
		// 						unidirectional model whose output is used is added, separated
		// 						by a colon (e.g., N_B-fwd-bach-WTC-4vv:N-bwd-bach-WTC-4vv) 
		// - <rootPath>			the path containing the user/ dir; fixed to F:/research/data/
		// 						(optional; can be empty string if pwd is rootPath)
		// - <verbose>			true if verbose output is wanted, false if not (optional; can
		// 						be empty string (i.e., false)) 
		// - <userDefinedName>  a user-defined name given to the data that is processed 
		//                      (optional; can be empty string (i.e., none))  
		//
		// The -cp arg <classPaths> contains all paths to .java (in bin/ dirs) and .jar 
		// (in lib/ dirs) files, and these paths must be added separately. Depending on the 
		// number of dirs in <codePath>, this can result in a very long and repetitive command
		// $ java -cp '<codePath>/<dir_1>/bin;<codePath>/<dir_1>/lib/*;
		//             ...
		//             <codePath>/<dir_n>/bin;<codePath>/<dir_n>/lib/*;
		//        ui.UI <modelID> <rootPath> <verbose> <userDefinedName>
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
		//           ui.UI <modelID> '' <verbose> <userDefinedName>
		//    e.g.,
		//    $ cd /cygdrive/f/research/data/
		//    $ java -cp $(for p in F:/research/software/code/eclipse/* ; do echo -n $p"/bin"";"$p"/lib/*"";" ; done) 
		//           ui.UI D-bwd-byrd-int-4vv '' '' '' 
		// b. pwd is not rootPath (rootPath must be provided as arg)
		//    $ cd <somePath>
		//    $ java -cp $(for p in <codePath>/* ; do echo -n $p"/bin"";"$p"/lib/*"";" ; done) 
		//           ui.UI <modelID> <rootPath> <verbose> <userDefinedName>
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
		//           ui.UI <modelID> '' <verbose> <userDefinedName>
		//    e.g.,
		//    $ cd /cygdrive/f/research/data/
		//    $ java -cp $(for p in F:/research/software/code/eclipse/* ; do echo -n "../""software/code/eclipse/"${p##*/}"/bin"";""../""software/code/eclipse/"${p##*/}"/lib/*"";" ; done) 
		//           ui.UI D-bwd-byrd-int-4vv '' '' ''
		// d. pwd is not rootPath (rootPath must be provided as arg)
		//    $ cd <path>/dirC>/<subdirC>/<someDir>
		//    $ java -cp $(for p in <path>/<dirB>/<codeDir>/* ; do echo -n "../../../""<dirB>/<codeDir>/"${p##*/}"/bin"";""../../../""<dirB>/<codeDir>/"${p##*/}"/lib/*"";" ; done) 
		//           ui.UI <modelID> <rootPath> <verbose> <userDefinedName>
		//
		// NB: Note the use of ${p##*/} (and not $p, as under 1a. and 1b.) to get only the last 
		//     dir in p and not the full path. See https://stackoverflow.com/questions/2107945/how-to-loop-over-directories-in-linux 
		else {
			// User-defined settings				
			// Get the ID of the trained model now applied and, in the bidir case, the ID
			// of the first-pass model whose predicted labels are used
			modelID = args[0].split(":")[0];			
			modelIDFirstPass = args[0].split(":").length == 1 ? null : args[0].split(":")[1];
			datasetIDTrain = modelID.split("-")[2] + "-" + modelID.split("-")[3] + "-" + modelID.split("-")[4];
			rootPath = args[1];
			verbose = Boolean.parseBoolean(args[2]);
			datasetName = args[3];

			set();
		}
	}


	private static void setRootPath(String arg) {
		rootPath = arg;
	}


	public static String getRootPath() {
		return rootPath; 
	}


	/**
	 * 
	 * @throws IOException 
	 */
	public static void set() throws IOException {
		// 1. Set rootPath and paths to code and data
		// a. If repeating an existing experiment or conducting a new one 
		// rootPath is F:/research/, which contains the dirs data/annotated/ (GT data) and 
		// software/code/eclipse/ (code)
		// b. If deploying a trained model 
		// rootPath is F:/research/data/, which contains the dirs user/ (unseen input data 
		// and output of the trained model) and models/ (trained models). The paths to the code
		// are provided via the classPath in the CLI. rootPath need not be provided if the pwd
		// is rootPath, i.e., F:/research/data/
		if (deployTrainedUserModel) {
			// Do nothing if rootPath is rootPathUser; else
			if (!rootPath.equals(rootPathUser)) {
				// If <rootPath> arg is empty string: OK if pwd is rootPathUser
				if (rootPath.equals("")) {
					// http://stackoverflow.com/questions/2837263/how-do-i-get-the-directory-that-the-currently-executing-jar-file-is-in
					String pwd = ToolBox.pathify(new String[]{new File("").getAbsolutePath()});
					if (pwd.equals(rootPathUser)) {
						rootPath = pwd;
					}
					else {
						throw new RuntimeException("No <rootPath> provided, pwd must be " + rootPathUser);
					}
				}
				// If <rootPath> arg is incorrect
				else {
					throw new RuntimeException("Incorrect <rootPath> provided; <rootPath> provided must be " + rootPathUser);
				}
			}
		}
		setRootPath(ToolBox.pathify(new String[]{rootPath}));
		Runner.setPathsToCodeAndData(rootPath, deployTrainedUserModel);

		// 2. Set dataset(s)
		Dataset dsTrain, ds;
		if (!deployTrainedUserModel) {
			ds = new Dataset(datasetID);
			dsTrain = null;
		}
		else {
			dsTrain = new Dataset(datasetIDTrain);
			ds = new Dataset(Dataset.isTablatureSet(datasetIDTrain) ? Dataset.USER_TAB : 
				Dataset.USER);
			// Set name (now default), pieceNames (now empty list), and numVoices (now 0)
			if (!datasetName.equals("")) {
				ds.setName(datasetName);
			}
			ds.setNumVoices(dsTrain.getNumVoices());
			ds.addPieceNames(ToolBox.getFileNamesWithExtension(new File(
				ds.isTablatureSet() ? Runner.encodingsPath : Runner.midiPath),
				ds.isTablatureSet() ? Encoding.EXTENSION : MIDIImport.EXTENSION));
		}

		// 3. Set paths for storing and retrieving
		// storePath:              where the output of the model currently run is stored 
		//                         (empty; files are added) 
		// pathPredTransFirstPass: where the voice labels (predicted by a unidir model) needed 
		//                         for a bidir model are stored (not empty; no files are added)
		// pathTrainedUserModel:   where the weights/modelparameters of a model to reuse are 
		//                         stored (not empty; no files are added) 
		// pathStoredNN: 		   
		// pathStoredMM:           
		// a. If repeating an existing experiment or conducting a new one
		// Path format (* = optional)
		// - Runner.experimentsPath + publication, *experiment, dataset name, voices, model, 
		//   processing mode, *hyperparams (when not training a user model)
		//   Examples 
		//   - F:/research/experiments/thesis/exp_1/bach-WTC/4vv/N/fwd/ (no hyperparams)
		//   - F:/research/experiments/ISMIR-2018/bach-WTC/4vv/D/fwd/HL=2/HLS=25/KP=0.75/
		//     (no experiment)
		// - Runner.modelsPath + model ID (when training a user model; storePath only)
		//   Example
		//   - F:/research/data/models/D-bwd-byrd-int-4vv/
		String storePath = null;
		String pathPredTransFirstPass = null; // null when not using bidir model
		String pathTrainedUserModel = null; // null when not deploying trained user model
		String pathStoredNN = null; // where the weights of the model to reuse are stored (not empty; no files are added); null when not using ensemble model
		String pathStoredMM = null; // null when not using ensemble model
		if (!deployTrainedUserModel) {
			storePath = !trainUserModel ? 
				ToolBox.pathify(new String[]{Runner.experimentsPath, 
				expDir, ds.getName(), ds.getNumVoices() + Runner.voices, 
				m.toString(), pm.getStringRep(), hyperparams}) 
				: 
				ToolBox.pathify(new String[]{Runner.modelsPath, 
				m.toString() + "-" + pm.getStringRep() + "-" + ds.getDatasetID()});
			if (m.getDecisionContext() == DecisionContext.BIDIR) {
				pathPredTransFirstPass = 
					ToolBox.pathify(new String[]{Runner.experimentsPath, expDirFirstPass});
			}
			if (m.getModelType() == ModelType.ENS) {
				pathStoredNN = ToolBox.pathify(new String[]{Runner.experimentsPath, 
					expDirFirstPass, ds.getName(), ds.getNumVoices() + Runner.voices, 
					Model.N.toString(), pm.getStringRep()});
				pathStoredMM = ToolBox.pathify(new String[]{Runner.experimentsPath, 
					expDirFirstPass, ds.getName(), ds.getNumVoices() + Runner.voices, 
					m.getMelodyModel().toString()}); 
			}
		}
		// b. If deploying a trained model
		// Path format
		// - Runner.outPath + model ID (for the current model and first-pass model)
		//   Examples
		//   - F:/research/data/user/out/D_B-fwd-byrd-int-4vv/ (current model) 
		//   - F:/research/data/user/out/D-bwd-byrd-int-4vv/ (first-pass model) 
		// - Runner.modelsPath + model ID (for the trained model)
		//   Example
		//   - F:/research/models/D_B-fwd-byrd-int-4vv/
		else {
			storePath = ToolBox.pathify(new String[]{Runner.outPath, modelID});
			pathTrainedUserModel = ToolBox.pathify(new String[]{Runner.modelsPath, modelID});
			if (modelIDFirstPass != null) {
				pathPredTransFirstPass = ToolBox.pathify(new String[]{Runner.outPath, modelIDFirstPass});
			}
			
			// Steps (i) and (ii): train user models
			// step (i)  : train best-performing unidir model on complete dataset
			// step (ii) : train best-performing bidir model on complete dataset, using
			//             the labels predicted with CV
			// Steps (iii) and (iv): deploy trained user models
			// step (iii) : apply the trained unidir model from (i) to new piece(s)
			// step (iv)  : apply the trained bidir model from (ii), using the labels 
			//              predicted in (iii)
//			System.out.println("storePath              = " + storePath); // user/out/D_B-fwd-byrd-int-4vv/
//			System.out.println("pathPredTransFirstPass = " + pathPredTransFirstPass); // user/out/D-bwd-byrd-int-4vv
//			System.out.println("pathTrainedUserModel   = " + pathTrainedUserModel); // models/D_B-fwd-byrd-int-4vv/
//			System.exit(0);
		}

		// 4. Set model parameters  
		// The order in which items are added to a LinkedHashMap is the order in which they
		// are returned, and also in which they are printed in performance.txt
		// In EvaluationManager.paramsNotPrinted is specified which one are not printed 
		Map<String, Double> modelParams = new LinkedHashMap<String, Double>();
		if (!deployTrainedUserModel) {
			ModelType mt = m.getModelType();

			// a. Settings (model - own concepts)
			modelParams.put(Runner.MODEL, (double) m.getIntRep());
			modelParams.put(Runner.MODELLING_APPROACH, (double) m.getModellingApproach().getIntRep());
			modelParams.put(Runner.FEAT_VEC, (m == Model.H || mt == ModelType.MM) ? null : (double) fv.getIntRep());
			modelParams.put(Runner.PROC_MODE, (double) pm.getIntRep());
			modelParams.put(Runner.SNU, (double) ToolBox.toInt(ds.isTablatureSet()));
			modelParams.put(Runner.ESTIMATE_ENTRIES, (double) ToolBox.toInt(estimateEntries));
			modelParams.put(Runner.MODEL_DURATION_AGAIN, (double) ToolBox.toInt(modelDurationAgain));
//			modelParams.put(Runner.CONFIG, (m == Model.H) ? (double) matrixConfiguration : null);
			modelParams.put(Runner.CONFIG, (m == Model.H) ? (double) config.getIntRep() : null);
//			modelParams.put(Runner.UNIFORM_TPM, (m == Model.H) ? (double) ToolBox.toInt(uniformTPM) : null);
//			modelParams.put(Runner.UNIFORM_ISM, (m == Model.H) ? (double) ToolBox.toInt(uniformISM) : null);
			modelParams.put(Runner.SLICE_IND_ENC_SINGLE_DIGIT, (mt == ModelType.MM || mt == ModelType.ENS) ? (double) ToolBox.encodeListOfIntegers(MelodyFeatureGenerator.getSliceIndices(mmfs)) : null);	
			modelParams.put(Runner.AVERAGE_PROX, (double) ToolBox.toInt(averageProx));
			// b. Settings (ML - existing concepts)
			modelParams.put(NNManager.ACT_FUNCTION, (mt == ModelType.NN || mt == ModelType.DNN || mt == ModelType.ENS) ? (double) activationFunc.getIntRep() : null);
			modelParams.put(Runner.DECODING_ALG, (m == Model.H) ? (double) decodingAlg.getIntRep() : null);
			modelParams.put(Runner.SEED, (double) seed);
			modelParams.put(Runner.WEIGHTS_INIT, (double) weightsInit.getIntRep());
			modelParams.put(Runner.CROSS_VAL, (double) ToolBox.toInt(useCV));
			// c. Tuned hyperparameters
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
			modelParams.put(Runner.NS_ENC_SINGLE_DIGIT, (mt == ModelType.ENS) ? (double) ToolBox.encodeListOfIntegers(ns) : null);
			// d. Non-tuned hyperparameters 
			modelParams.put(Runner.VALIDATION_PERC, (double) validationPercentage);
			modelParams.put(Runner.DECISION_CONTEXT_SIZE, (double) decisionContextSize);
			modelParams.put(Runner.META_CYCLES, (mt == ModelType.NN || mt == ModelType.ENS) ? (double) maxMetaCycles : null);
			modelParams.put(NNManager.CYCLES, (mt == ModelType.NN || mt == ModelType.ENS) ? (double) cycles : null);
			modelParams.put(Runner.EPOCHS, (mt == ModelType.DNN) ? (double) epochs : null);
			modelParams.put(Runner.LEARNING_RATE, (mt == ModelType.NN || mt == ModelType.DNN || mt == ModelType.ENS) ? learningRate : null);
			modelParams.put(Runner.DEV_THRESHOLD, (ds.isTablatureSet() && (mt == ModelType.NN || mt == ModelType.DNN || mt == ModelType.ENS)) ? deviationThreshold : null);
		}
		else {
			modelParams = 
				ToolBox.getStoredObjectBinary(new LinkedHashMap<String, Double>(), 
				new File(pathTrainedUserModel + Runner.modelParameters + ".ser"));
			// Reset necessary settings
			modelParams.put(Runner.CROSS_VAL, (double) ToolBox.toInt(false));
			modelParams.put(Runner.WEIGHTS_INIT, (double) WeightsInit.INIT_FROM_LIST.getIntRep());
		}
		
//		for (Entry<String, Double> e : modelParams.entrySet()) {
//			System.out.println(e.getKey() + ": " + e.getValue());
//		}

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
		Model mdl = 
			!deployTrainedUserModel ? m : Runner.ALL_MODELS[modelParams.get(Runner.MODEL).intValue()];
		Transcription.setMaximumNumberOfVoices( 
			(mdl.getModellingApproach() == ModellingApproach.N2N) ? 5 : 4);
		MelodyPredictor.setMelModelType(MelModelType.SIMPLE_LM);
		MelodyPredictor.setTermType(mdl.getKylmModelType());
		EvaluationManager.setMetricsUsed(metricsUsed);
		System.out.println("storePath              = " + storePath);
		System.out.println("pathPredTransFirstPass = " + pathPredTransFirstPass);
		System.out.println("pathTrainedUserModel   = " + pathTrainedUserModel);
		System.out.println("pathStoredNN           = " + pathStoredNN);
		System.out.println("pathStoredMM           = " + pathStoredMM);
//		System.exit(0);
		Runner.setPaths(new String[]{storePath, pathPredTransFirstPass, pathTrainedUserModel, 
			pathStoredNN, pathStoredMM});
		Runner.setModelParams(modelParams);
		Runner.setDataset(ds);
		Runner.setDatasetTrain(dsTrain);
		Runner.runExperiment(trainUserModel, skipTraining, deployTrainedUserModel, verbose);
	}

}
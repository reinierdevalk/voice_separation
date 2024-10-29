package ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import conversion.exports.MEIExport;
import conversion.imports.MIDIImport;
import data.Dataset;
import external.Transcription;
import featureExtraction.MelodyFeatureGenerator;
import featureExtraction.MelodyFeatureGenerator.MelodyModelFeature;
import internal.core.Encoding;
import machineLearning.EvaluationManager;
import machineLearning.EvaluationManager.Metric;
import machineLearning.MelodyPredictor;
import machineLearning.MelodyPredictor.MelModelType;
import machinelearning.NNManager;
import machinelearning.NNManager.ActivationFunction;
import tools.ToolBox;
import tools.path.PathTools;
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
	private static String rootPath;

	// Runner settings
	private static boolean deployTrainedUserModel, skipTraining, trainUserModel, verbose;

	// Settings and hyperparameters, model training
	private static boolean useCV, estimateEntries, modelDurationAgain, averageProx;
	private static double lambda, hiddenLayerFactor, epsilon, keepProbability, C, 
		learningRate, deviationThreshold;
	private static int hiddenLayers, hiddenLayerSize, n, neighbours, trees, cycles, 
		maxMetaCycles, decisionContextSize, epochs, miniBatchSize, validationPercentage, seed;
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
	private static String datasetIDTrain, datasetName, filename;


	public static void main(String[] args) throws IOException {
		boolean dev = args.length == 0 ? true : args[0].equals(String.valueOf(true));
		Map<String, String> paths = PathTools.getPaths(dev);

		// Scenarios
		//  useCV && !deployTrainedModel: model selection phase
		//  useCV &&  deployTrainedModel: never
		// !useCV && !deployTrainedModel: application case with known GT (transfer learning)
		// !useCV &&  deployTrainedModel: application case with unknown GT (real-world/deploy)
		deployTrainedUserModel = args.length == 0 ? false : true;

		// If repeating an existing experiment or conducting a new one: set parameters and settings
		if (!deployTrainedUserModel) {
			System.out.println("EXIT FOR SAFETY REASONS -- uncomment below");
			System.exit(0);
			
			// Settings
			// a. Runner settings
			skipTraining = false;
			trainUserModel = false;
			verbose = true;
			
			// b. Variable settings
			boolean gridSearch = false;
			useCV = true;
			estimateEntries = false;
			weightsInit = WeightsInit.INIT_FROM_LIST;
//			weightsInit = WeightsInit.INIT_RANDOM;
			//
			datasetID = Dataset.BACH_INV_3VV;
			m = Model.D;
			pm = ProcessingMode.FWD; // NB: bidir case must always be fwd
			fv = FeatureVector.PHD_D;
//			expDir = "ISMIR-2018"; // publication + experiment (if applicable)
//			expDir = "thesis/exp_3.3.1/";
			expDir = "ISMIR-2018/";
//			expDirFirstPass = "byrd/byrd-int/4vv/D/bwd/";
			expDirFirstPass = "thesis/exp_3.1/thesis-int/3vv/N/bwd/";
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
//			hyperparams = "";
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
			lambda = 0.0001; // regularisation parameter  
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
			maxMetaCycles = (m == Model.C) ? 60 : 80; // 40
			cycles = 10;
			// DNN
			seed = 0; // seed = 0 used for all experiments ISMIR 2018 paper
			epochs = 600; // 460;
			// General
			learningRate = (m.getModelType() == ModelType.DNN) ? alpha : 1.0;
			deviationThreshold = 0.05;
			miniBatchSize = -1;
			validationPercentage = (m.getModelType() == ModelType.DNN) ? 20 : 20; // 10 : 20;
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
					set(paths, null);
				}
			}
			else {
				set(paths, null);
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
			// User-defined settings				
			// Get the ID of the trained model now applied and, in the bidir case, the ID
			// of the first-pass model whose predicted labels are used
			modelID = args[1].split(":")[0];			
			modelIDFirstPass = args[1].split(":").length == 1 ? null : args[1].split(":")[1];
			datasetIDTrain = modelID.split("-")[2] + "-" + modelID.split("-")[3] + "-" + modelID.split("-")[4];
			// rootPath is the pwd if args[1] is an empty string or a dot; else the string given in args[1].
			rootPath = new File(args[2]).getCanonicalFile().toString();

//			// Set codePath if rootPath is not F:/research/data/
//			if (!rootPath.equals(new File(Path.DEPLOYMENT_DEV_PATH).getCanonicalFile().toString())) {
//				codeRelPath = Path.CODE_DIR;
//			}
//			System.out.println(rootPath);
//			System.out.println(codeRelPath);
//			System.exit(0);

			verbose = Boolean.parseBoolean(args[3]);
			datasetName = args[4];

			filename = new File(args[5]).getName();
			String transParams = args[6];

//l			System.out.println("modelID: " + modelID); // args[0]
//l			System.out.println("modelIDFirstPass: " + modelIDFirstPass); // from args[0]
//l			System.out.println("datasetIDTrain: " + datasetIDTrain); // from args[0]
//l			System.out.println("rootPath: " + rootPath); // args[1]
//l			System.out.println("verbose: " + verbose); // args[2]
//l			System.out.println("datasetname: " + datasetName); // args[3]
//l			System.out.println("filename: " + filename); // args[4]
//l			System.out.println("transParams: " + transParams); // args[5]

			// Valid values: k, m
			Map<String, String> transcriptionParams = new LinkedHashMap<String, String>();
			// TODO make method
			for (String s : transParams.split(",")) {
				transcriptionParams.put(s.split(" ")[0].trim(), s.split(" ")[1].trim());
			}
//			for (String s : transParams.split("\\|")) {
//				transcriptionParams.put(s.split("=")[0].trim(), s.split("=")[1].trim());
//			}
			set(paths, transcriptionParams);
		}
	}


	/**
	 * 
	 * @throws IOException 
	 */
	public static void set(Map<String, String> paths, Map<String, String> transcriptionParams) throws IOException {
		// 1. Set rootPath and paths to code and data
		// a. If repeating an existing experiment or conducting a new one 
		// rootPath is F:/research/, which contains the dirs data/annotated/ (GT data) and 
		// software/code/eclipse/ (code)
		// b. If deploying a trained model 
		// rootPath is F:/research/data/, which contains the dirs user/ (unseen input data 
		// and output of the trained model) and models/ (trained models). The paths to the code
		// are provided via the classPath in the CLI. rootPath need not be provided if the pwd
		// is rootPath, i.e., F:/research/data/
//		if (deployTrainedUserModel) {
//			// Do nothing if rootPath is rootPathUser; else
//			if (!rootPath.equals(rootPathUser)) {
//				// If <rootPath> arg is empty string: OK if pwd is rootPathUser
//				if (rootPath.equals("")) {
//					// http://stackoverflow.com/questions/2837263/how-do-i-get-the-directory-that-the-currently-executing-jar-file-is-in
//					String pwd = ToolBox.pathify(new String[]{new File("").getAbsolutePath()});
//					if (pwd.equals(rootPathUser)) {
//						rootPath = pwd;
//					}
//					else {
//						throw new RuntimeException("No <rootPath> provided, pwd must be " + rootPathUser);
//					}
//				}
//				// If <rootPath> arg is incorrect
//				else {
//					throw new RuntimeException("Incorrect <rootPath> provided; <rootPath> provided must be " + rootPathUser);
//				}
//			}
//		}
//		setRootPath(ToolBox.pathify(new String[]{rootPath}));

//		Runner.setPathsToCodeAndData(rootPath, deployTrainedUserModel);

		// 2. Set dataset(s)
		Dataset dsTrain, ds;
		if (!deployTrainedUserModel) {
			ds = new Dataset(datasetID);
			dsTrain = null;
		}
		else {
			// dsTrain is not populated
			dsTrain = new Dataset(datasetIDTrain);
//			System.out.println(dsTrain.getDatasetID());
//			System.out.println(dsTrain.getName());
//			System.out.println(dsTrain.getPieceNames());
//			System.out.println(dsTrain.getNumPieces());
//			System.out.println(dsTrain.getNumVoices());
//			System.out.println(dsTrain.isTablatureSet());
//			System.out.println(dsTrain.isTabAsNonTabSet());
//			System.out.println(dsTrain.getLargestChordSize());
//			System.out.println(dsTrain.getHighestNumVoices());
//			System.out.println(dsTrain.getAllEncodingFiles());
//			System.out.println(dsTrain.getAllMidiFiles());
//			System.out.println(dsTrain.getAllTablatures());
//			System.out.println(dsTrain.getAllTranscriptions());
//			System.out.println(dsTrain.getNumDataExamples(ModellingApproach.N2N));
//			System.out.println(dsTrain.getIndividualPieceSizes(ModellingApproach.N2N));
//			System.out.println("FUUUUCK");
//			System.exit(0);
			
			// ds is populated
			ds = new Dataset(
				Dataset.isTablatureSet(datasetIDTrain) ? Dataset.USER_TAB : Dataset.USER
			);
			// Set name (now default), pieceNames (now empty list), and numVoices (now 0)
			if (!datasetName.equals("")) {
				ds.setName(datasetName);
			}
			ds.setNumVoices(dsTrain.getNumVoices());
			if (filename.equals("")) {
				ds.addPieceNames(ToolBox.getFileNamesWithExtension(new File(
					PathTools.getPathString(Arrays.asList(paths.get("POLYPHONIST_PATH"), "in"))),
//					ds.isTablatureSet() ? Runner.encodingsPath : Runner.midiPath),
					ds.isTablatureSet() ? Encoding.EXTENSION : MIDIImport.EXTENSION)
				);
			}
			else {
				String filenameNoExt = filename.substring(0, filename.indexOf("."));
				ds.addPieceNames(Arrays.asList(filenameNoExt));
			}
//			System.out.println(ds.getDatasetID());
//			System.out.println(ds.getName());
//			System.out.println(ds.getPieceNames());
//			System.out.println(ds.getNumPieces());
//			System.out.println(ds.getNumVoices());
//			System.out.println(ds.isTablatureSet());
//			System.out.println(ds.isTabAsNonTabSet());
//			System.out.println(ds.getLargestChordSize());
//			System.out.println(ds.getHighestNumVoices());
//			System.out.println(ds.getAllEncodingFiles());
//			System.out.println(ds.getAllMidiFiles());
//			System.out.println(ds.getAllTablatures());
//			System.out.println(ds.getAllTranscriptions());
//			System.out.println(ds.getNumDataExamples(ModellingApproach.N2N));
//			System.out.println(ds.getIndividualPieceSizes(ModellingApproach.N2N));
//			System.out.println("FUUUUCK");
//			System.exit(0);
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
		// - EXPERIMENTS_PATH + publication, *experiment, dataset name, voices, model, 
		//   processing mode, *hyperparams (when not training a user model)
		//   Examples 
		//   - F:/research/experiments/thesis/exp_1/bach-WTC/4vv/N/fwd/ (no hyperparams)
		//   - F:/research/experiments/ISMIR-2018/bach-WTC/4vv/D/fwd/HL=2/HLS=25/KP=0.75/
		//     (no experiment)
		// - MODELS_PATH + model ID (when training a user model; storePath only)
		//   Example
		//   - F:/research/data/models/D-bwd-byrd-int-4vv/
		String storePath = null;
		String pathPredTransFirstPass = null; // null when not using bidir model
		String pathTrainedUserModel = null; // null when not deploying trained user model
		String pathStoredNN = null; // where the weights of the model to reuse are stored (not empty; no files are added); null when not using ensemble model
		String pathStoredMM = null; // null when not using ensemble model
		String modelsPath = PathTools.getPathString(Arrays.asList(paths.get("MODELS_PATH")));
		// TODO put these two in main()s of all abtab modules
		MEIExport.setTemplatesPath(PathTools.getPathString(Arrays.asList(paths.get("TEMPLATES_PATH"))));
		MEIExport.setPythonPath(PathTools.getPathString(Arrays.asList(paths.get("UTILS_PYTHON_PATH"))));
		if (!deployTrainedUserModel) {
			String experimentsPath = PathTools.getPathString(
				Arrays.asList(paths.get("EXPERIMENTS_PATH"))
			);
			
			storePath = !trainUserModel ? 
				ToolBox.pathify(new String[]{experimentsPath, 
				expDir, ds.getName(), ds.getNumVoices() + Runner.voices, 
				m.toString(), pm.getStringRep(), hyperparams}) 
				: 
				ToolBox.pathify(new String[]{modelsPath, 
				m.toString() + "-" + pm.getStringRep() + "-" + ds.getDatasetID()});
			if (m.getDecisionContext() == DecisionContext.BIDIR) {
				pathPredTransFirstPass = 
					ToolBox.pathify(new String[]{experimentsPath, expDirFirstPass});
			}
			if (m.getModelType() == ModelType.ENS) {
				pathStoredNN = ToolBox.pathify(new String[]{experimentsPath, 
					expDirFirstPass, ds.getName(), ds.getNumVoices() + Runner.voices, 
					Model.N.toString(), pm.getStringRep()});
				pathStoredMM = ToolBox.pathify(new String[]{experimentsPath, 
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
		// - MODELS_PATH + model ID (for the trained model)
		//   Example
		//   - F:/research/models/D_B-fwd-byrd-int-4vv/
		else {
			String op = PathTools.getPathString(
				Arrays.asList(paths.get("POLYPHONIST_PATH"), "out")
			);
			storePath = ToolBox.pathify(new String[]{op, modelID});
//			storePath = ToolBox.pathify(new String[]{Runner.outPath, modelID});
			pathTrainedUserModel = ToolBox.pathify(new String[]{modelsPath, modelID});
			if (modelIDFirstPass != null) {
				pathPredTransFirstPass = ToolBox.pathify(new String[]{op, modelIDFirstPass});
//				pathPredTransFirstPass = ToolBox.pathify(new String[]{Runner.outPath, modelIDFirstPass});
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
			modelParams.put(Runner.ISMIR_2018, (double) ToolBox.toInt(expDir.equals("ISMIR-2018")));
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
			modelParams.put(Runner.MINI_BATCH_SIZE, (double) miniBatchSize);
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
		
//			modelParams.put(Runner.MINI_BATCH_SIZE, (double) miniBatchSize);
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
		Transcription.setMaxNumVoices( 
			(mdl.getModellingApproach() == ModellingApproach.N2N) ? 5 : 4);
		MelodyPredictor.setMelModelType(MelModelType.SIMPLE_LM);
		MelodyPredictor.setTermType(mdl.getKylmModelType());
		EvaluationManager.setMetricsUsed(metricsUsed);
//l		System.out.println("storePath              = " + storePath);
//l		System.out.println("pathPredTransFirstPass = " + pathPredTransFirstPass);
//l		System.out.println("pathTrainedUserModel   = " + pathTrainedUserModel);
//l		System.out.println("pathStoredNN           = " + pathStoredNN);
//l		System.out.println("pathStoredMM           = " + pathStoredMM);
//		System.exit(0);
		Runner.setPaths(new String[]{storePath, pathPredTransFirstPass, pathTrainedUserModel, 
			pathStoredNN, pathStoredMM});
		Runner.setModelParams(modelParams);
		Runner.setDataset(ds);
		Runner.setDatasetTrain(dsTrain);
//		System.out.println(Runner.encodingsPath);
//		System.out.println(ds.getPieceNames());
//		System.out.println(dsTrain.getName());
//		System.exit(0);
//		System.out.println(Arrays.asList(transcriptionParams));

		Runner.runExperiment(
			trainUserModel, skipTraining, deployTrainedUserModel, verbose, paths, transcriptionParams
		);
	}

}
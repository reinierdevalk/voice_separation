package ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import data.Dataset;
import featureExtraction.FeatureGenerator;
import machineLearning.EvaluationManager;
import machineLearning.MelodyPredictor;
import machineLearning.NNManager;
import machineLearning.TestManager;
import machineLearning.TrainingManager;
import n_grams.KylmModel;
import tools.ToolBox;

public class Runner {
	
	// General
	public static final int TRAIN = 0;
	public static final int TEST = 1;
	public static final int APPL = 2;
	public static final int VALID = 3;
	public static final int ASSESS = 3;
	public static final int INIT_WEIGHTS = 0;
	public static final int BEST_WEIGHTS = 1;
	
	// Directory names and paths
	public static String pathToData; // = UI.getRootDir() + "data/";
	public static String pathToCode; // = UI.getRootDir() + "software/code/";
//	private static String encodingsDir = "encodings/dataset/";
//	private static String tabMidiDir = "MIDI/tab/";
//	private static String bachMidiDir = "MIDI/bach/";
//	private static String inventionsDir = "inventions/";
//	private static String wtcDir = "WTC/";
//	private static String storedMM = "stored/MM/";

//	public static String encodingsPath = rootDir + "encodings/clean/";
	public static String encodingsPath; // = pathToData + "encodings/"; 
	public static String encodingsPathTest; // = encodingsPath + "test/";
//	public static String tabMidiPath = rootDir + "MIDI/clean/tab/";
//	public static String bachMidiPath = rootDir + "MIDI/clean/bach/";
	public static String midiPath; // = pathToData + "MIDI/";
	public static String midiPathTest; // = midiPath + "test/";
//	public static String userEncodingsPath = rootDir + "encodings/user/";
//	public static String encodingsPathUser = encodingsPath + "user/";
	public static String encodingsPathUser; // = pathToData + "user/in/encodings/";
//	public static String userMidiPath = rootDir + "MIDI/user/";
//	public static String midiPathUser = midiPath + "user/";
	public static String midiPathUser; // = pathToData + "user/in/MIDI/";
//	public static String encodingsPathTest = "F:/research/data/" + "encodings/test/";
//	public static String midiPathTest = "F:/research/data/" + "MIDI/test/";
//	public static String scriptPath = "F:/research/software/code/python/train_and_test_models.py";
//	public static String scriptPath = "C:/Users/Reinier/Dropbox/";
//	public static String scriptPath = "F:/research/software/code/python/";
	public static String scriptPathPython; // = pathToCode + "python/";
	public static String scriptPathMatlab;
	public static String scriptScikit = "train_and_test_models.py";
	public static String script = "train_test_tensorflow.py";

//	public static String[] testPaths; // = new String[]{encodingsPathTest, midiPathTest, midiPathTest};
	public static String resultsPath; // = pathToData + "experiments/";
	public static String userPath; // = pathToData + "user/"; 
//	public static String storedNNPath; // = pathToData + "stored/NN/";
//	public static String storedMMPath; // = pathToData + "stored/MM/";
//	private static String storedOutputsPath = pathToData + "stored/outputs/";
	public static String storedDatasetsPath; // = pathToData + "stored/datasets/";
//	public static String MEITemplatePath;
//	private static String storedTranscriptionsPath = pathToData + "stored/transcriptions/";
	public static String output = "out/";
	
	// Naming conventions
	// Abbreviate all metrics and self-defined names/terms, using 
	// a. three-letter codes for single-word terms
	//    1. if abbreviation exist, use it (e.g., avg, fwd)
	//    2. if not, use first three letters when the term starts with a vowel (e.g., acc)
	//       and first three consonants when it starts with a consonant (e.g., prc)
	// b. capitalised acronyms for multi-word terms
	//    1. if abbreviation exist, use it (e.g., SD, PhD)
	//    2. if not, use capitalised first letter of each word (e.g., NE), if applicable with 
	//       number added (e.g., MA1)
	// c. capital letters for self-defined names and categories such as model names 
	//    (e.g., N, B), feature vector names (e.g., A, B), or voice assignment categories
	//    (e.g., I, O)
	//
	// Types of files saved
	// .ser files are files that are read by the system only and that need not be human-readable
	// .csv files are files that are read by the system and that are also human-readable
	// .txt files are human-readable convenience files 
	// The H model reads and writes only .csv files, as it cannot serialise Java-readable files
	public static String minMaxFeatVals = "feature_ranges";
	public static String melodyModel = "MM";
	public static String neuralNetwork = "NN";
	public static String genVoiceLabels = "voice_lab-"; // TODO remove
	public static String genDurLabels = "dur_lab-"; // TODO remove
	private static String overallPerf = "performance";
	private static String overallPerfDur = "overall_performance-dur";
	public static String modelWeighting = "weighting";
//	public static String initWeightsFileName = "init_weights";
//	public static String bestWeightsFileName = "best_weights";
	public static String weights = "weights";
	
	public static String train = "trn";
	public static String test = "tst";
	public static String application = "app";
	public static String validation = "vld";
	public static String evalMM = "eval";
	public static String outputMM = "model_output-MM";
	public static String perf = "performance";
	public static String perfDur = "performance-dur";
	public static String outputs = "model_output";
	public static String details = "details";
	public static String info = "info";
	public static String prePr = "prp";
	public static String postPr = "pst";
	public static String average = "avg";
	public static String stDev = "SD";
	public static String total = "tot";
	public static String ISM = "ISM";
	public static String OPM = "OPM";
	public static String TPM = "TPM";
	public static String observations = "observations";
//	public static String mappings = "mappings";
	public static String mappingDict = "mapping_dictionary";
	public static String chordDict = "chord_dictionary";
	public static String modelParameters = "model_parameters";
	
//	public static String fvExt = "fv_"; // scikit
	public static String fvExt = "x_";
	public static String clExt = "cl_";
//	public static String lblExt = "lbl_"; // scikit
	public static String lblExt = "y_";
//	public static String outpExt = "outp_"; // scikit
	public static String outpExt = "out_";
	
	public static boolean ignoreAppl = false;
	public static boolean storeNetworkOutputs = true;
	public static boolean textify = true;
//	public static double parameterToTune;

	// Keys
	// Enums
	public static final String MODELLING_APPROACH = "modelling approach";
	public static final String MODEL = "model";
	private static final String MODEL_TYPE = "model_type";
//	public static final String IMPLEMENTATION = "implementation";
	public static final String PROC_MODE = "processing mode";
	public static final String FEAT_VEC = "feature vector";
	private static final String DECISION_CONTEXT = "decision context";
	public static final String WEIGHTS_INIT = "weights initialisation";
	// ints
	public static final String VALIDATION_PERC = "validation percentage";
	public static final String SEED = "seed";
	public static final String DECISION_CONTEXT_SIZE = "decision context size";
	public static final String META_CYCLES = "metacycles";
	public static final String HIDDEN_LAYER_FACTOR = "hidden layer factor";
	public static final String HIGHEST_NUM_VOICES = "highest number of voices";
	public static final String LARGEST_CHORD_SIZE = "largest chord size";
	public static final String N = "n";
//	public static final String TOTAL_NUM_EXAMPLES = "total number of examples";
	public static final String C = "regularisation parameter (alt.)";
	public static final String N_NGH = "neighbours";
	public static final String N_EST = "decision trees";
	public static final String SLICE_IND_ENC_SINGLE_DIGIT = "slice indices";
	public static final String NS_ENC_SINGLE_DIGIT = "n values";

//	public static final String NEIGHBOURS = "neighbours";
//	public static final String TREES = "decision trees";
	
	// DNN

	public static final String EPOCHS = "epochs";
	public static final String NUM_HIDDEN_LAYERS = "hidden layers";
	public static final String HIDDEN_LAYER_SIZE = "hidden layer size";
	// HMM
	public static final String CONFIG = "configuration";
	public static final String UNIFORM_TPM = "uniform TPM";
	public static final String UNIFORM_ISM = "uniform ISM";
	public static final String DECODING_ALG = "decoding algorithm";
	
	// doubles
	public static final String DEV_THRESHOLD = "deviation threshold";
	// DNN
	public static final String LEARNING_RATE = "learning rate";
	public static final String KEEP_PROB = "keep probability";
	
	// booleans
	public static final String SNU = "single-note unisons";
	public static final String CROSS_VAL = "cross validation";
	public static final String TRAIN_USER_MODEL = "train user model";
	public static final String APPL_TO_NEW_DATA = "applied to new data";
//	public static final String TAB_AS_NON_TAB = "tablature as non-tablature";
	public static final String ESTIMATE_ENTRIES = "voice entry estimation";
//	public static final String GIVE_FIRST = "give first";
	public static final String MODEL_DURATION_AGAIN = "model duration again";
	public static final String AVERAGE_PROX = "average proximities";
//	public static final String USE_VALIDATION_SET = "use validation set";

	// Enum lists
	public static final int ARR_SIZE = 50; // TODO 
	public static ModellingApproach[] ALL_MODELLING_APPROACHES = new ModellingApproach[ARR_SIZE];
	public static Model[] ALL_MODELS = new Model[ARR_SIZE];
	private static ModelType[] ALL_MODEL_TYPES = new ModelType[ARR_SIZE];
	public static Implementation[] ALL_IMPLEMENTATIONS = new Implementation[ARR_SIZE];
	public static ProcessingMode[] ALL_PROC_MODES = new ProcessingMode[ARR_SIZE];
	public static FeatureVector[] ALL_FEATURE_VECTORS = new FeatureVector[ARR_SIZE];
	private static DecisionContext[] ALL_DECISION_CONTEXTS = new DecisionContext[ARR_SIZE];
	public static WeightsInit[] ALL_WEIGHTS_INIT = new WeightsInit[ARR_SIZE];

	public static enum ModellingApproach {
		N2N("MA1", 0), C2C("MA2", 1), HMM("MA3", 2), OTHER("OTHER", 3);

		private int intRep;
		private String stringRep;
		ModellingApproach(String s, int i) {
			this.stringRep = s;
			this.intRep = i;
			ALL_MODELLING_APPROACHES[i] = this;
		}
		
		@Override
	    public String toString() {
	        return getStringRep();
	    }
		
		public String getStringRep() {
			return stringRep;
		}
		
		public int getIntRep() {
			return intRep;
		}
	};

	// ISMIR13, EM15, PHD, CMA16, ISMIR17, ISMIR18
	public static enum Model {
		N("N", 0, ModelType.NN, DecisionContext.UNIDIR, false, ModellingApproach.N2N), 
		N_PRIME("N_prime", 1, ModelType.NN, DecisionContext.UNIDIR, true, ModellingApproach.N2N), 
		B("B", 2, ModelType.NN, DecisionContext.BIDIR, false, ModellingApproach.N2N),
		B_PRIME("B_prime", 3, ModelType.NN, DecisionContext.BIDIR, true, ModellingApproach.N2N),
		B_STAR("B_star", 4, ModelType.NN, DecisionContext.BIDIR, false, ModellingApproach.N2N),
		B_PRIME_STAR("B_prime_star", 5, ModelType.NN, DecisionContext.BIDIR, true, ModellingApproach.N2N),
		
		C("C", 6, ModelType.NN, DecisionContext.UNIDIR, false, ModellingApproach.C2C),

		H("H", 7, ModelType.HMM, DecisionContext.UNIDIR, false, ModellingApproach.HMM),

		STM("STM", 8, ModelType.MM, KylmModel.Type.SHORT, ModellingApproach.OTHER),
		LTM("LTM", 9, ModelType.MM, KylmModel.Type.LONG, ModellingApproach.OTHER), 
		MIX("mix", 10, ModelType.MM, KylmModel.Type.MIX, ModellingApproach.OTHER), 
		PROD("pro", 11, ModelType.MM, KylmModel.Type.PROD, ModellingApproach.OTHER),

		ENS_STM("ens_STM", 12, ModelType.ENS, Model.STM, KylmModel.Type.SHORT, ModellingApproach.N2N),
		ENS_LTM("ens_LTM", 13, ModelType.ENS, Model.LTM, KylmModel.Type.LONG, ModellingApproach.N2N),

		LR("LR", 14, ModelType.OTHER, DecisionContext.UNIDIR, false, ModellingApproach.N2N),
		LR_CL("LR_cls",15, ModelType.OTHER, DecisionContext.UNIDIR, false, ModellingApproach.N2N),
		LSVC_CL("LSVC_cls",16, ModelType.OTHER, DecisionContext.UNIDIR, false, ModellingApproach.N2N),
		RF("RF",17, ModelType.OTHER, DecisionContext.UNIDIR, false, ModellingApproach.N2N),
		RF_CL("RF_cls",18, ModelType.OTHER, DecisionContext.UNIDIR, false, ModellingApproach.N2N),
		kNN("kNN",19, ModelType.OTHER, DecisionContext.UNIDIR, false, ModellingApproach.N2N),
		kNN_CL("kNN_cls", 20, ModelType.OTHER, DecisionContext.UNIDIR, false, ModellingApproach.N2N),
		
		D("D", 21, ModelType.DNN, DecisionContext.UNIDIR, false, ModellingApproach.N2N),
		D_B("D_B", 22, ModelType.DNN, DecisionContext.BIDIR, false, ModellingApproach.N2N),
		;

		private String stringRep;
		private int intRep;
		private ModelType type;
		private DecisionContext decisionContext;
		private boolean modelDuration;
		private ModellingApproach modellingApproach;
		private KylmModel.Type kmType;
		private Model melodyModel;
		
		
//		Model(String s, int i, ModelType mt) {
//			this.stringRep = s;
//			this.intRep = i;
//			this.type = mt;
//			ALL_MODELS[i] = this;
//		}
		
		@Override
	    public String toString() {
	        return getStringRep();
	    }
		
		Model(String s, int i, ModelType mt, DecisionContext dc, boolean md, ModellingApproach ma) {
			this.stringRep = s;
			this.intRep = i;
			this.type = mt;
			this.decisionContext = dc;
			this.modelDuration = md;
			this.modellingApproach = ma;
			ALL_MODELS[i] = this;
		}

		Model(String s, int i, ModelType mt, KylmModel.Type kmt, ModellingApproach ma) {
			this.stringRep = s;
			this.intRep = i;
			this.type = mt;
			this.kmType = kmt;
			this.modellingApproach = ma;
			ALL_MODELS[i] = this;
		}

		Model(String s, int i, ModelType mt, Model mm, KylmModel.Type kmt, ModellingApproach ma) {
			this.stringRep = s;
			this.intRep = i;
			this.type = mt;
			this.melodyModel = mm;
			this.kmType = kmt;
			this.modellingApproach = ma;
			ALL_MODELS[i] = this;
		}

		public int getIntRep() {
			return intRep;
		}
		
		public String getStringRep() {
			return stringRep;
		}
		
		public ModelType getModelType() {
			return type;
		}
		
		public DecisionContext getDecisionContext() {
			return decisionContext;
		}
		
		public boolean getModelDuration() {
			return modelDuration;
		}
		
		public ModellingApproach getModellingApproach() {
			return modellingApproach;
		}

// 		31.03		
		public KylmModel.Type getKylmModelType() {
			return kmType;
		}

		public Model getMelodyModel() {
			return melodyModel;
		}	
	};

	
	public static enum ModelType{
		NN("NN", 0), HMM("HMM", 1), MM("MM", 2), ENS("ens", 3), OTHER("other", 4), DNN("DNN", 5);
		
		private int intRep;
		private String stringRep;
		ModelType(String s, int i) {
			this.stringRep = s;
			this.intRep = i;
			ALL_MODEL_TYPES[i] = this;
		}
		
		@Override
	    public String toString() {
	        return getStringRep();
	    }
		
		public int getIntRep() {
			return intRep;
		}
		
		public String getStringRep() {
			return stringRep;
		}
	};
	
	
	public static enum Implementation {	
		MU_SCI("MS", 0), ISMIR13("ISMIR13", 1), EM15("EM15", 2), CMA16("CMA16", 3), PHD("PhD", 4);
		
		static int ID = 0;
		
		private int intRep;
		private String stringRep;
		Implementation(String s, int i) {
			this.stringRep = s;
			this.intRep = i;
			ALL_IMPLEMENTATIONS[i] = this;
		}
		
		@Override
	    public String toString() {
	        return getStringRep();
	    }
		
		public int getIntRep() {
			return intRep;
		}
		
		public String getStringRep() {
			return stringRep;
		}
	}

	
	public static enum ProcessingMode {
		FWD("fwd", 0), BWD("bwd", 1);  
		
		private String asString;
		private int intRep;
		ProcessingMode(String s, int i) {
			this.asString = s;
			this.intRep = i;
			ALL_PROC_MODES[i] = this;
		}
		
		@Override
	    public String toString() {
	        return getStringRep();
	    }
		
		public String getStringRep() {
			return asString;
		}
		
		public int getIntRep() {
			return intRep;
		}
	};
	
	
	public static Configuration[] ALL_CONFIGS = new Configuration[10];
	public static enum Configuration {
		ONE("1", true, true, 0), 
		TWO("2", true, false, 1), 
		THREE("3", false ,true, 2), 
		FOUR("4", false, false, 3);  

		private String asString;
		private boolean uniformTPM;
		private boolean uniformISM;
		private int intRep;
		Configuration(String s, boolean unifTPM, boolean unifISM, int i) {
			this.asString = s;
			this.uniformTPM = unifTPM;
			this.uniformISM = unifISM;
			this.intRep = i;
			ALL_CONFIGS[i] = this;
		}

		@Override
	    public String toString() {
//			return String.valueOf(getIntRep());
			return getStringRep();        
	    }

		public String getStringRep() {
			return asString;
		}

		public int getIntRep() {
			return intRep;
		}
		
		public boolean getUniformTPM() {
			return uniformTPM;
		}
		
		public boolean getUniformISM() {
			return uniformISM;
		}

		public String getDescription() {
			String desc = ""; 
			if (getUniformTPM() && getUniformISM()) {
				desc = "uniform TPM; uniform ISM";
			}
			else if (getUniformTPM() && !getUniformISM()) {
				desc = "uniform TPM; data-extracted ISM";
			}
			else if (!getUniformTPM() && getUniformISM()) {
				desc = "data-extracted TPM; uniform ISM";
			}
			else if (!getUniformTPM() && !getUniformISM()) {
				desc = "data-extracted TPM; data-extracted ISM";
			}
			return desc;
		}
	}

	public static enum FeatureVector {
		ISMIR13("ISMIR_2013", 0), 
		EM15("EM_2015", 1),
		PHD("PhD", 2),
		PHD_A("PhD, A", 3), PHD_B("PhD, B", 4), PHD_C("PhD, C", 5), PHD_D("PhD, D", 6), PHD_D_STAR("PhD, D_star", 7),
		CMA16("CMA_2016", 8), 
		ISMIR17("ISMIR_2017", 9), 
		ISMIR18("ISMIR_2018", 10);
		
		private String stringRep;
		private int intRep;
		FeatureVector(String s, int i) {
			this.intRep = i;
			this.stringRep = s;
			ALL_FEATURE_VECTORS[i] = this;
		}
		
		@Override
	    public String toString() {
	        return getStringRep();
	    }

		public String getStringRep() {
			return stringRep;
		}

		public int getIntRep() {
			return intRep;
		}
	};

	public static enum DecisionContext {
		UNIDIR("uni", 0), BIDIR("bi", 1);

		private String stringRep; 
		private int intRep;
		DecisionContext(String s, int i) {
			this.stringRep = s;
			this.intRep = i;
			ALL_DECISION_CONTEXTS[i] = this;
		}
		
		@Override
	    public String toString() {
	        return getStringRep();
	    }

		public int getIntRep() {
			return intRep;
		}
		
		public String getStringRep() {
			return stringRep;
		}
	}

	
	public static enum WeightsInit {
		INIT_RANDOM("random", 0), INIT_FROM_LIST("list", 1);
		
		private int intRep;
		private String stringRep;
		WeightsInit(String s, int i) {
			this.stringRep = s;
			this.intRep = i;
			ALL_WEIGHTS_INIT[i] = this;
		}
		
		@Override
	    public String toString() {
	        return getStringRep();
	    }
		
		public String getStringRep() {
			return stringRep;
		}
		
		public int getIntRep() {
			return intRep;
		}
	};
	
	public static DecodingAlgorithm[] ALL_DEC_ALG = new DecodingAlgorithm[10]; 
	public static enum DecodingAlgorithm {
		VITERBI("vit", 0); 
		
		private int intRep;
		private String stringRep;
		DecodingAlgorithm(String s, int i) {
			this.stringRep = s;
			this.intRep = i;
			ALL_DEC_ALG[i] = this;
		}
		
		@Override
	    public String toString() {
	        return getStringRep();
	    }
		
		public String getStringRep() {
			return stringRep;
		}
		
		public int getIntRep() {
			return intRep;
		}
	};

	public static Map<String, Object[]> getEnumKeys(/*ModelType mt*/) {
		Map<String, Object[]> keys = new LinkedHashMap<String, Object[]>();
//		keys.put(Dataset.DATASET_ID, Dataset.ALL_DATASET_IDS);
		keys.put(MODELLING_APPROACH, ALL_MODELLING_APPROACHES);
		keys.put(MODEL, ALL_MODELS);
//		keys.put(IMPLEMENTATION, ALL_IMPLEMENTATIONS);
		keys.put(PROC_MODE, ALL_PROC_MODES);
		keys.put(MODEL_TYPE, ALL_MODEL_TYPES);
		keys.put(DECISION_CONTEXT, ALL_DECISION_CONTEXTS);
//		if (mt != ModelType.OTHER) {
		keys.put(NNManager.ACT_FUNCTION, NNManager.ALL_ACT_FUNCT);
		keys.put(DECODING_ALG, ALL_DEC_ALG);
		keys.put(CONFIG, ALL_CONFIGS);
//		}
		keys.put(WEIGHTS_INIT, ALL_WEIGHTS_INIT);
		keys.put(FEAT_VEC, ALL_FEATURE_VECTORS);
		return keys;
	}


	public static List<String> getBooleanKeys() {
		List<String> keys = new ArrayList<String>();
		keys.add(SNU);
		keys.add(CROSS_VAL);
		keys.add(MODEL_DURATION_AGAIN);
		keys.add(TRAIN_USER_MODEL);
//		keys.add(TAB_AS_NON_TAB);
//		keys.add(GIVE_FIRST);
		keys.add(APPL_TO_NEW_DATA);
		keys.add(ESTIMATE_ENTRIES);
		keys.add(UNIFORM_TPM);
		keys.add(UNIFORM_ISM);
		return keys;
	}


	public static List<String> getIntKeys() {
		List<String> keys = new ArrayList<String>();
		keys.add(META_CYCLES);
		keys.add(NNManager.CYCLES);
//		keys.add(HIGHEST_NUM_VOICES);
//		keys.add(LARGEST_CHORD_SIZE);
		keys.add(N);
		keys.add(NUM_HIDDEN_LAYERS);
		keys.add(EPOCHS);
		keys.add(VALIDATION_PERC);
		keys.add(SEED);
//		keys.add(CONFIG);
		keys.add(HIDDEN_LAYER_SIZE);
		keys.add(DECISION_CONTEXT_SIZE);
		return keys;
	}
	
	
	public static List<String> getDoubleKeys() {
		List<String> keys = new ArrayList<String>();
		keys.add(HIDDEN_LAYER_FACTOR);
		keys.add(DECISION_CONTEXT);
		keys.add(NNManager.LEARNING_RATE);
		keys.add(NNManager.MARGIN);
		keys.add(NNManager.REGULARISATION_PARAMETER);
		keys.add(NNManager.LEARNING_RATE);
		keys.add(KEEP_PROB);
		return keys;
	}


	private static Dataset dataset;
	private static Dataset datasetTrain;
	public static void setDataset(Dataset d) {
		dataset = d;
	}

	public static Dataset getDataset() {
		return dataset;
	}
	
	public static void setDatasetTrain(Dataset d) {
		datasetTrain = d;
	}

	public static Dataset getDatasetTrain() {
		return datasetTrain;
	}
	
	public static int getHighestNumVoicesTraining() {
		int highestNumVoices = getDataset().getHighestNumVoices();
		if (ToolBox.toBoolean(modelParams.get(APPL_TO_NEW_DATA).intValue())) {
			highestNumVoices = getDatasetTrain().getNumVoices();
		}
		return highestNumVoices;
	}
	
	

	private static Map<String, Double> modelParams;
	public static void setModelParams(Map<String, Double> arg) {
		modelParams = arg;
	}

	public static Map<String, Double> getModelParams() {
		return modelParams;
	}

	private static String[] paths;
	private static void setPaths(String[] arg) {
		paths = arg;
	}

	public static String[] getPaths() {
		return paths;
	}

//	private static List<Integer> sliceIndices;
//	public static void setSliceIndices(List<Integer> arg) {
//		sliceIndices = arg;
//	}
//
//	public static List<Integer> getSliceIndices() {
//		return sliceIndices;		
//	}

	private static boolean verbose;
	private static void setVerbose(boolean arg) {
		verbose = arg;
	}

	public static boolean getVerbose() {
		return verbose;
	}
	
	public static void setPathsToCodeAndData(String argRootDir, boolean appliedToNewData) {
		if (!appliedToNewData) {
			pathToData = argRootDir + "data/data/";
			pathToCode = argRootDir + "software/code/";
//			scriptPathPython = pathToCode + "python/";
			scriptPathPython = pathToCode + "eclipse/voice_separation/py/";
//			scriptPathMatlab = pathToCode + "MATLAB/";
			scriptPathMatlab = pathToCode + "eclipse/voice_separation/m/";
			encodingsPath = pathToData + "encodings/";
			midiPath = pathToData + "MIDI/";
			encodingsPathTest = encodingsPath + "test/";
			midiPathTest = midiPath + "test/";
//			resultsPath = pathToData + "experiments/";
			resultsPath = argRootDir + "experiments/";
//			storedNNPath = pathToData + "stored/NN/";
//			storedMMPath = pathToData + "stored/MM/";
//			storedDatasetsPath = pathToData + "datasets/";
			storedDatasetsPath = argRootDir + "data/" + "datasets/";
//			MEITemplatePath = pathToData + "templates/MEI/";
		}
		else {
			pathToData = argRootDir;
			pathToCode = argRootDir;
			scriptPathPython = argRootDir;
			encodingsPathUser = pathToData + "user/in/encodings/";
			midiPathUser = pathToData + "user/in/MIDI/";
			userPath = pathToData + "user/";
		}
	}


	public static String getOtherParam(Map<String, Double> argModelParams) {
		Model m = ALL_MODELS[argModelParams.get(Runner.MODEL).intValue()];
		if (m == Model.LR || m == Model.LR_CL || m == Model.LSVC_CL) { 
			return String.valueOf(argModelParams.get(Runner.C));
		}
		else if (m == Model.kNN || m == Model.kNN_CL) {
			return String.valueOf(argModelParams.get(Runner.N_NGH));
		}
		else if (m == Model.RF || m == Model.RF_CL) {
			return String.valueOf(argModelParams.get(Runner.N_EST));
		}
		else {
			return null;
		}
	}


	private static List<Long> perfAndDetailsTxtFilesStoreTimes = new ArrayList<Long>();
	public static void addToPerfAndDetailsTxtFilesStoreTimes(long l) {
		perfAndDetailsTxtFilesStoreTimes.add(l);
	}

	private static List<Long> perfCsvFilesStoreTimes = new ArrayList<Long>();
	public static void addToPerfCsvFilesStoreTimes(long l) {
		perfCsvFilesStoreTimes.add(l);
	}

	public static void runExperiment(Map<String, Double> argModelParams, String[] argPaths, 
		Dataset ds, Dataset dsTrain, /*List<Integer> argSliceIndices, 
		List<Integer> argNs,*/ String datasetVersion, boolean verbose) {
		
//		System.out.println("ptd = " + pathToData);
//		System.out.println("ptc = " + pathToCode);
//		System.out.println("sp = " + scriptPath);
//		System.exit(0);
		
		String startPreProc = ToolBox.getTimeStampPrecise();
				
//		String[] argPaths = getPaths();
//		Map<String, Double> argModelParams = getModelParams();
//		List<Integer> argSliceIndices = getSliceIndices();

		ModellingApproach ma = 
			ALL_MODELLING_APPROACHES[argModelParams.get(MODELLING_APPROACH).intValue()];	
		Model m = ALL_MODELS[argModelParams.get(MODEL).intValue()]; 
		boolean modelDuration = m.getModelDuration();
		ModelType mt = m.getModelType();
//		boolean useCrossVal = ToolBox.toBoolean(argModelParams.get(Runner.CROSS_VAL).intValue());
		boolean applToNewData = ToolBox.toBoolean(argModelParams.get(APPL_TO_NEW_DATA).intValue());

//		System.out.println("\n>> preparing the experiment");

		// Create and set dataset; update modelParams
		if (applToNewData) {
			System.out.println("\nstarting halcyon.");
		}
		System.out.println("\ncreating the dataset.");		
		if (!applToNewData) {
			File datasetFile = new File(storedDatasetsPath + ds.getDatasetID() + ".ser");
			if (!datasetFile.exists()) {
				ds.populateDataset(/*argModelParams,*/ datasetVersion, null, applToNewData);
				ToolBox.storeObjectBinary(ds, datasetFile);
			}
			else {
				ds = ToolBox.getStoredObjectBinary(new Dataset(), datasetFile);
			}
		}
		else {
			ds.populateDataset(/*argModelParams,*/ datasetVersion, null, applToNewData);
		}
		setDataset(ds);
		setDatasetTrain(dsTrain);
		
//		int largestChordSize = Runner.getDataset().getLargestChordSize();	
//		argModelParams.put(LARGEST_CHORD_SIZE, (double) ds.getLargestChordSize());
//		argModelParams.put(HIGHEST_NUM_VOICES, (double) ds.getHighestNumVoices());
//		if (applToNewData) {
//			argModelParams.put(HIGHEST_NUM_VOICES, (double) dsTrain.getNumVoices());
//		}
//		argModelParams.put(TOTAL_NUM_EXAMPLES, (double) dataset.getNumDataExamples(ma));
		setModelParams(argModelParams);
		
		setPaths(argPaths);
//		setSliceIndices(argSliceIndices);
//		setNs(argNs);
		setVerbose(verbose);
		
		// If the HMM is trained and tested
//		if (mt == ModelType.HMM) {

			
//			String path = argPaths[0];
			
			// 1. Train, i.e., generate matrices and test data
//			String startTr = ToolBox.getTimeStampPrecise();
//			int hiNumVoicesAssumed = 4;
//			hm.generateDictionariesAndMatrices(dataset, path, hiNumVoicesAssumed);
			
			// 2. Test, i.e, predict most likely mapping sequence
//			String startTe = ToolBox.getTimeStampPrecise();
			// Integrate Matlab code
//			new TestManager().prepareTesting(null);

//			hm.evaluate(null, null, null, false);
			// 3. Evaluate

//			System.exit(0);
//		}
		// If only the MM is trained and tested
		if (mt == ModelType.MM) {				
			int n = argModelParams.get(Runner.N).intValue();
			String path = argPaths[0];
			path = path.concat("n=").concat(String.valueOf(n)).concat("/");
			System.out.println(path);
			argPaths[0] = path;
			setPaths(argPaths);			
//			System.exit(0);

			// 1. Train
			String startTraining = ToolBox.getTimeStamp();
			System.out.println("\n>> Starting the training.");
			new TrainingManager().prepareTraining(null);
			String endTraining = ToolBox.getTimeStamp();
//			System.exit(0);

			// 2. Test
			String startTe = ToolBox.getTimeStamp();
			new TestManager().prepareTesting(startTe);
			String endEval = ToolBox.getTimeStamp();
//			System.exit(0);
			
			// 3. Evaluate
			List<Integer> argSliceIndices = 
				ToolBox.decodeListOfIntegers(modelParams.get(Runner.SLICE_IND_ENC_SINGLE_DIGIT).intValue(), 1);
			String[] evaluations = 
				EvaluationManager.getPerformanceAllFoldsMM(path, ds.getNumPieces(),
				argSliceIndices);
			for (int j = 0; j < argSliceIndices.size(); j++) {	
				String mod = 
					Runner.melodyModel + "-" +
					MelodyPredictor.getSliceIndexString(argSliceIndices.get(j)) + "-";
				ToolBox.storeTextFile(evaluations[j],	
					new File(path + mod + "eval-all_folds.txt"));
			}

//			String runtimes = 
//				EvaluationManager.makeHeader(new String[]{/*start,*/ null, startTraining, 
//				endTraining, startEval, endEval, ToolBox.getTimeStamp()}, null);
//			ToolBox.storeTextFile(runtimes, new File(path + "runtimes.txt"));
		}
		// If only the NN, a combined model, or the HMM is trained and tested
		else {
			boolean isTablatureCase = ds.isTablatureSet();
			String path = argPaths[0];
			
			// Determine scaling settings
			FeatureGenerator.determineScalingSettings(ma, isTablatureCase); // TODO fix hardcoded nums

//			double lmbd = argModelParams.get(NNManager.REGULARISATION_PARAMETER);
//			String lmbdAsString = ToolBox.convertToStringNoTrailingZeros(lmbd);
//			double HLSize = argModelParams.get(HIDDEN_LAYER_FACTOR);
			List<Integer[]> indicesOfFirstPieceOfTestSets = null;	
			if (applToNewData) { // TODO
				indicesOfFirstPieceOfTestSets = new ArrayList<Integer[]>();
				List<String> imi = null; //new Dataset(DatasetID.IMI_SHORTER).makePieceNames();
				List<String> free = null; // new Dataset(DatasetID.FREE_MORE).makePieceNames();
				List<String> semi = null; // new Dataset(DatasetID.SEMI).makePieceNames();

				if (ds.equals(imi)) {
					indicesOfFirstPieceOfTestSets.add(new Integer[]{0, free.size()});
					indicesOfFirstPieceOfTestSets.add(new Integer[]{free.size(), free.size() + semi.size()});
				}
				else if (ds.equals(free)) {
					indicesOfFirstPieceOfTestSets.add(new Integer[]{0, imi.size()});
					indicesOfFirstPieceOfTestSets.add(new Integer[]{imi.size(), imi.size() + semi.size()});
				}
				else if (ds.equals(semi)) {
					indicesOfFirstPieceOfTestSets.add(new Integer[]{0, imi.size()});
					indicesOfFirstPieceOfTestSets.add(new Integer[]{imi.size(), imi.size() + free.size()});
				}
			}

//			String endPre = ToolBox.getTimeStampPrecise();
			// End of preprocessing
			String endPreProc = ToolBox.getTimeStampPrecise();
//			addToTimes(new String[]{startPre, ToolBox.getTimeStampPrecise()});
//			System.out.println("###    endPre = " + endPre);
			
			// 1. Train
			String startTr = ToolBox.getTimeStampPrecise();
//			String startTraining = "";
//			String endTraining = "";
			if (!applToNewData) {
//				startTraining = ToolBox.getTimeStamp();
//				System.out.println("### 1. startTraining = " + startTraining);
				System.out.println("\nstarting the training.");						
				new TrainingManager().prepareTraining(startTr);
//				endTraining = ToolBox.getTimeStamp();
//				System.out.println("### 2. endTraining = " + endTraining);
			}
			if (ToolBox.toBoolean(argModelParams.get(TRAIN_USER_MODEL).intValue())) {
				ToolBox.storeTextFile(EvaluationManager.getDataAndParamsInfo(Runner.TRAIN, -1),
					new File(path + modelParameters + ".txt"));
				ToolBox.storeObjectBinary(modelParams, new File(path + modelParameters + ".ser"));
				System.exit(0);
			}

			// 2. Evaluate
			String startTe = ToolBox.getTimeStampPrecise();
//			String startEval = ToolBox.getTimeStamp();
//			System.out.println("### 3. startEval = " + startEval);
			if (!applToNewData) {
				System.out.println("\nstarting the evaluation.");
			}
			else {
				System.out.println("\napplying the model.");
			}
			new TestManager().prepareTesting(startTe);

//			String endEval = ToolBox.getTimeStamp();
//			System.out.println("### 4. endEval = " + endEval);
//			System.exit(0);
			
			// 3. Calculate overall performance and store
			String startPostProc = ToolBox.getTimeStampPrecise();
//			System.out.println("### 5. startPost = " + startPost);
			if (!applToNewData) {
				System.out.println("\n>> calculating the overall performance");
				// Create the csv table(s)
				String[] times = new String[]{null, ToolBox.getTimeStamp()};
				List<String[][]> csvTables = new ArrayList<String[][]>();
//				String perf = overallPerf;
				boolean isForDur = false;
				int iter = 1;
				if (modelDuration && m.getDecisionContext() == DecisionContext.UNIDIR) {
					iter = 2;
				}
				for (int i = 0; i < iter; i++) {
//					if (i == 1) {
//						perf = overallPerfDur;
//						isForDur = true;
//					}
					
//					preProcDeltas.set(TRAIN,(long) 3000);
//					preProcDeltas.set(TEST, (long) 4000);
					
					String[][] csvTable = 
						EvaluationManager.createCSVTableAllFolds(ds.getNumPieces(), 
						path, isForDur, new long[]{
							ToolBox.getTimeDiffPrecise(startPreProc, endPreProc),
//							preProcDeltas.get(TRAIN),
//							preProcDeltas.get(TEST),
							ToolBox.getTimeDiffPrecise(startPostProc, 
								ToolBox.getTimeStampPrecise())
						});
//					ToolBox.storeTextFile(ToolBox.createCSVTableString(csvTable), 
//						new File(path + perf + ".csv"));
					csvTables.add(csvTable);
//					perf = overallPerfDur;
					isForDur = true;
				}

				if (ignoreAppl) {
					List<String[][]> newCsvTables = new ArrayList<String[][]>();
					String[][] bla = 
						new String[csvTables.get(0).length-2][csvTables.get(0)[0].length];
					for (int i = 0; i < csvTables.get(0).length -2; i++) {
						bla[i] = csvTables.get(0)[i];
						
					}
					newCsvTables.add(bla);
//					for (String[] s : newCsvTables.get(0)) {
//						System.out.println(Arrays.toString(s));
//					}
					csvTables = newCsvTables;
				}
	
				ToolBox.storeObjectBinary(modelParams, new File(path + modelParameters + ".ser"));
				// Store the csv table(s)
				// NB The time this takes is not included in the total runtime
				int indToUpdate = 
					Arrays.asList(csvTables.get(0)[0]).indexOf(EvaluationManager.Metric.RUNTIME.getStringRep());
				String s = Runner.perf;
				String endPostProc = ToolBox.getTimeStampPrecise(); 
				long totalTime = ToolBox.getTimeDiffPrecise(startPostProc, endPostProc);  
				for (String[][] t : csvTables) {
					// Do not update the duration csv (which does not contain the runtimes)
					if (s.equals(Runner.perf)) {
						t[t.length-1][indToUpdate] = String.valueOf(totalTime);
					}
					ToolBox.storeTextFile(ToolBox.createCSVTableString(t), 
						new File(path + s + ".csv"));
					s = Runner.perfDur;
				}
				Runner.addToPerfCsvFilesStoreTimes(ToolBox.getTimeDiffPrecise(endPostProc, 
					ToolBox.getTimeStampPrecise()));
				
				System.out.println("TOTAL TIME: " + 
//				ToolBox.getTimeDiffPrecise(startPreProc, endPostProc));
				ToolBox.getTimeDiffPrecise(startPreProc, ToolBox.getTimeStampPrecise()));
				
				System.out.println("storing of performance .csv files: ");
				System.out.println(Runner.perfCsvFilesStoreTimes);
				long sum2 = 0;
				for (long l : Runner.perfCsvFilesStoreTimes) {
					sum2 += l;
				}
				System.out.println(sum2);
				System.out.println("storing of performance and details .txt files: ");
				System.out.println(perfAndDetailsTxtFilesStoreTimes);
				long sum = 0;
				for (long l : perfAndDetailsTxtFilesStoreTimes) {
					sum += l;
				}
				System.out.println(sum);
				
				// Create overall performance record and store it
				// NB The time this takes is not included in the total runtime
//				String header = 
//					EvaluationManager.makeHeader(new String[]{start, startTraining, 
//					endTraining, startEval, endEval, ToolBox.getTimeStamp()}, null);
				String dataAndParams = EvaluationManager.getDataAndParamsInfo(Runner.ASSESS, -1);
				String results = 
					EvaluationManager.getPerformanceInfo(csvTables, null, ds.getNumPieces(), 
					null, /*null,*/ indicesOfFirstPieceOfTestSets);
				String rec = dataAndParams.concat("\r\n").concat(results);
				ToolBox.storeTextFile(rec, new File(path + overallPerf + ".txt"));
			}
			// Store info
			else {
				String s =
					"model =      " + ALL_MODELS[argModelParams.get(MODEL).intValue()] + "\r\n" +
					"proc_mode =  " + ALL_PROC_MODES[argModelParams.get(PROC_MODE).intValue()] + "\r\n" +
					"train_data = " + dsTrain.getDatasetID();
				
				System.out.println("\n" + ds.getNumPieces() 
					+ " files processed successfully.");
//				ToolBox.storeTextFile(s, new File(path + info + ".txt"));
			}
		}
	}


//	folderName = "EuroMAC 2014/N2N/Bach/4vv (new scaling, all, 0,1, na = 0)";
//	folderName = "EuroMAC 2014/C2C/Bach/4vv (max 5vv) FINAL/epsilon = 0.05";
//	folderName = "EuroMAC 2014/C2C/Bach/4vv (max 4vv) (avg prox) FINAL met alrOcc = -1/epsilon = 0.05";
//	folderName = "EuroMAC 2014/C2C/Bach/4vv (max 5vv) (scaling all 0,1; na = -1)/epsilon = 0.05";
//	folderName = "EuroMAC 2014/N2N/Bach/4vv (smart scaling; 0,1; na = -1)";
//	folderName = "EuroMAC 2014/N2N/Bach/4vv (new scaling, all, 0,1, na = -1)";
		
}

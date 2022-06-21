package machineLearning;

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import data.Dataset;
import de.uos.fmt.musitech.utility.math.Rational;
import representations.Transcription;
import tools.ToolBox;
import ui.Runner;
import ui.Runner.DecisionContext;
import ui.Runner.Model;
import ui.Runner.ModelType;
import ui.Runner.ModellingApproach;
import utility.DataConverter;

public class EvaluationManager {

	private static final int FULLY = 0;
	private static final int PARTLY = 1;
	private static final int INCORRECT = 2;
	private static final int WTD_PARTLY = 3;
	private static final int COMBINED = 4;
	private static final int CROSS_ENT = 5;

	private static final int ACC_IND = 0;
	private static final int PRC_IND = 1;
	private static final int RCL_IND = 2;
	private static final int F1_IND = 3;
	private static final int SND_IND = 4;
	private static final int CMP_IND = 5;
	private static final int AVC_IND = 6;
	private static final int CRE_IND = 7;
	private static final int ACC_DUR_IND = 8;
	public static final int NTW_ERR_IND = 9;
	private static final int RUNTIME_IND = 10;

	private static List<Metric> metricsUsed;
	static List<String> csvLegend;
	static {
		csvLegend = Arrays.asList(new String[]{
//			Metric.MODE, Metric.FOLD,
			"mode", "fold",
			Metric.NTW_ERR.getStringRep(),
			Metric.ACC.getStringRep(), Metric.NONE.getStringRep(), 
			Metric.PRC.getStringRep(), Metric.NONE.getStringRep(), 
			Metric.RCL.getStringRep(), Metric.NONE.getStringRep(), 
			Metric.F1.getStringRep(), Metric.NONE.getStringRep(),
			Metric.SND.getStringRep(), Metric.NONE.getStringRep(), 
			Metric.CMP.getStringRep(), Metric.NONE.getStringRep(), 
			Metric.AVC.getStringRep(),
			Metric.CRE.getStringRep(),
			Metric.INCORR.getStringRep(), Metric.OVERL.getStringRep(), 
			Metric.SUPERFL.getStringRep(), Metric.HALF.getStringRep(), 
			Metric.CNF.getStringRep(),
			Metric.RUNTIME.getStringRep()
		});
	}
	
	private static List<String> paramsNotPrinted; 
	static {
		paramsNotPrinted = Arrays.asList(new String[]{
			Runner.MODELLING_APPROACH, 
			Runner.WEIGHTS_INIT, 
			Runner.CROSS_VAL, 
			Runner.MODEL_DURATION_AGAIN,
			Runner.AVERAGE_PROX,
			Runner.ISMIR_2018
//			Runner.DEPLOY_TRAINED_USER_MODEL,
//			Runner.TRAIN_USER_MODEL,
//			Runner.VERBOSE,
//			Runner.SKIP_TRAINING,
		});
	}
	
	static final String MODE = "mode";
	static final String FOLD = "fold";
	private static final int MODE_IND = 0;
	private static final int FOLD_IND = 1;
	public static enum Metric {
//		MODE("mode"),
//		FOLD("fold"),
//		NTW_ERR("ntw_err", true, NTW_ERR_IND),
		NTW_ERR("NE", true, NTW_ERR_IND),
		ACC("acc", true, ACC_IND),
		PRC("prc", true, PRC_IND),
		RCL("rcl", true, RCL_IND),
		F1("F1", true, F1_IND),
		SND("snd", true, SND_IND),
		CMP("cmp", true, CMP_IND),
		AVC("AVC", true, AVC_IND),
//		CRE("cre", false, true, false, SUMMED_LOG_P_IND),
		CRE("CE", true, CRE_IND),
		INCORR("I", false, ErrorCalculator.NUM_INCORRECT),
		OVERL("O", false, ErrorCalculator.NUM_OVERLOOKED),
		SUPERFL("S", false, ErrorCalculator.NUM_SUPERFLUOUS),
		HALF("H", false, ErrorCalculator.NUM_HALF),
		CNF("cnf", false, -1),
		RUNTIME("rnt", false, RUNTIME_IND),
		NONE("");

		private String stringRep;
		Metric(String s) {
			this.stringRep = s;
		}

		private boolean isEF;
		private int arrInd;
		Metric(String s, boolean isEF, int ind) {
			this.stringRep = s;
			this.isEF = isEF;
			this.arrInd = ind;
		}

		public String getStringRep() {
			return stringRep;
		}

		private boolean isEF() {
			return isEF;
		}

		private int arrInd() {
			return arrInd;
		}
	};


	public static void setMetricsUsed(List<Metric> arg) {
		metricsUsed = arg;
	}


	public static void setCsvLegend(List<String> arg) {
		csvLegend = arg;
	}


	public static String makeHeader(String[] times, String trOrEv) {
		StringBuffer header = new StringBuffer();
		header.append("R U N T I M E".concat("\r\n"));

		String start = times[0];
		String end = times[times.length - 1];
		int numTabs = 4;
		// Per-fold case
		if (times.length == 2) {
			header.append(ToolBox.tabify(trOrEv.concat(" started"), numTabs).concat(start).concat("\r\n"));  
			header.append(ToolBox.tabify(trOrEv.concat(" completed"), numTabs).concat(end).concat("\r\n"));
		}
		// Overall case
		else {
			header.append(ToolBox.tabify("experiment started", numTabs).concat(start).concat("\r\n"));  
			header.append(ToolBox.tabify("training started", numTabs).concat(times[1]).concat("\r\n")); 
			header.append(ToolBox.tabify("training completed", numTabs).concat(times[2]).concat("\r\n"));
			header.append(ToolBox.tabify("evaluation started", numTabs).concat(times[3]).concat("\r\n")); 
			header.append(ToolBox.tabify("evaluation completed", numTabs).concat(times[4]).concat("\r\n"));
			header.append(ToolBox.tabify("experiment completed", numTabs).concat(end).concat("\r\n"));
		}
		
		String timeDiff;
		long sec = ToolBox.getTimeDiff(start, end);
		if (sec == 0) {
			timeDiff = "<1";
		}
		else {
			timeDiff = String.valueOf(sec);
		}
		header.append(ToolBox.tabify("runtime in seconds", numTabs).concat(timeDiff).concat("\r\n"));

		return header.toString();
	}

	/**
	 * Creates a String containing data and model parameters information.
	 * 
	 * @param mode 
	 * @param fold N/a when called during overall assessment.
	 * @param paramsToExcl
	 * @return
	 */
	public static String getDataAndParamsInfo(int mode, int fold) {
		Map<String, Double> modelParameters = Runner.getModelParams();
		Dataset dataset = Runner.getDataset();
		StringBuffer dataAndParams = new StringBuffer();
		
		List<String> allPieceNames = dataset.getPieceNames();
		ModellingApproach ma = 
			Runner.ALL_MODELLING_APPROACHES[modelParameters.get(Runner.MODELLING_APPROACH).intValue()];
		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()];
		ModelType mt = m.getModelType();
		int totalNumExamples = dataset.getNumDataExamples(ma);

		// Data
		dataAndParams.append("D A T A S E T" + "\r\n");
		dataAndParams.append(ToolBox.tabify(dataset.getName(), 4) +
//		dataAndParams.append(ToolBox.tabify(Dataset.DATASET_ID, 4) + 		
			dataset.getDatasetID().toString().toLowerCase() + "\r\n");
		// Per-fold case
		if (fold != 0 && fold != -1) {
			int testPieceIndex = dataset.getNumPieces() - fold; // numFolds - fold
			String testPieceName = allPieceNames.get(testPieceIndex);
			List<String> trainingPieceNames = new ArrayList<String>();
			for (String s : allPieceNames) {
				if (!s.equals(testPieceName)) { // remove the if to fake non-cross-validation (for pre-testing, on a single piece, whether training and testing yield the same result)
					trainingPieceNames.add(s);
				}
			}
			int numTestExamples = dataset.getIndividualPieceSizes(ma).get(testPieceIndex);
			int numTrainingExamples = totalNumExamples - numTestExamples;
			
			if (mode == Runner.TEST || mode == Runner.APPL) {
				dataAndParams.append(ToolBox.tabify("test set", 4) + testPieceName + "\r\n");
				dataAndParams.append(ToolBox.tabify("test examples", 4) + numTestExamples + "\r\n");
			}
//			System.out.println(trainingPieceNames);
			dataAndParams.append(ToolBox.tabify("training set", 4) + trainingPieceNames.get(0) + "\r\n");
			for (int i = 1; i < trainingPieceNames.size(); i++) {
				dataAndParams.append(ToolBox.tabify("", 4) + trainingPieceNames.get(i) + "\r\n");
			}
			dataAndParams.append(ToolBox.tabify("training examples", 4) + numTrainingExamples + "\r\n");
		}
		// Overall case
		else {
			dataAndParams.append(ToolBox.tabify("dataset", 4) + allPieceNames.get(0) + "\r\n");
			for (int i = 1; i < allPieceNames.size(); i++) {
				dataAndParams.append(ToolBox.tabify("", 4) + allPieceNames.get(i) + "\r\n");
			}
			dataAndParams.append(ToolBox.tabify("data examples", 4) + totalNumExamples + "\r\n");
		}
		
		int largestChordSize = dataset.getLargestChordSize();
		int highestNumVoices = Runner.getHighestNumVoicesTraining(Runner.getDeployTrainedUserModel());

		dataAndParams.append(ToolBox.tabify(Runner.LARGEST_CHORD_SIZE, 4) + largestChordSize + "\r\n");
//			modelParameters.get(Runner.LARGEST_CHORD_SIZE).intValue() + "\r\n"); 		
		dataAndParams.append(ToolBox.tabify(Runner.HIGHEST_NUM_VOICES, 4) + highestNumVoices + "\r\n");
//			modelParameters.get(Runner.HIGHEST_NUM_VOICES).intValue() + "\r\n");	
		
		// Model parameters
		if (fold == -1) {
			dataAndParams.append("\r\n");
			dataAndParams.append("M O D E L  P A R A M E T E R S" + "\r\n");
			Map<String, Object[]> enumKeys = Runner.getEnumKeys();
			List<String> booleanKeys = Runner.getBooleanKeys();
			List<String> intKeys = Runner.getIntKeys();
			for (Entry<String, Double> e : modelParameters.entrySet()) {
				String key = e.getKey();
				Double value = e.getValue(); 
				if (!paramsNotPrinted.contains(key) && value != null) {
					dataAndParams.append(ToolBox.tabify(key, 4));
					if (enumKeys.containsKey(key)) {
						if (key.equals(Runner.CONFIG)) {
							dataAndParams.append(enumKeys.get(key)[value.intValue()] + 
								" (" + Runner.ALL_CONFIGS[modelParameters.get(Runner.CONFIG).intValue()].getDescription() + ")\r\n");
						}
						else {
							dataAndParams.append(enumKeys.get(key)[value.intValue()] + "\r\n");
						}
					}
					else if (booleanKeys.contains(key)) {
						if (key.equals(Runner.SNU) || key.equals(Runner.ESTIMATE_ENTRIES)) {
//							dataAndParams.append(((value == 1.0) ? "yes" : "no") + "\r\n"); // TODO restore?
							dataAndParams.append(ToolBox.toBoolean(value.intValue()) + "\r\n");
						}
						else {
							dataAndParams.append(ToolBox.toBoolean(value.intValue()) + "\r\n");
						}
					}
					else if (intKeys.contains(key)){
						if (!key.equals(Runner.SEED) || (key.equals(Runner.SEED) && mt == ModelType.DNN)) {
							dataAndParams.append(value.intValue() + "\r\n");
						}
						else if (key.equals(Runner.SEED) && mt != ModelType.DNN) {
//							dataAndParams.append("n/a" + "\r\n");
							dataAndParams = new StringBuffer().append(
								dataAndParams.substring(0, dataAndParams.indexOf(Runner.SEED)));
						}
					}
					else {
						dataAndParams.append(value + "\r\n");
					}
				}
			}
		}
		return dataAndParams.toString();
	}


	/**
	 * Evaluates the given predicted voices.
	 * 
	 * @param assignmentErrors
	 * @param allPredictedVoices
	 * @param ntwOutputsForCE
	 * @param groundTruthVoiceLabels
	 * @param equalDurationUnisonsInfo
	 * @param isTestOrAppMode
	 * 
	 * @returns An Array of ErrorFractions, with at index <br>
	 *          [0] accuracy (with, in the tablature case, num and denom multiplied by 2 to get rid of halves) <br>
	 *          [1] precision, if applicable <br>
	 *          [2] recall, if applicable <br>
	 *          [3] F1-score, if applicable <br>
	 *          [4] soundness <br>
	 *          [5] completeness <br>
	 *          [6] Average Voice Consistency AVC, if applicable <br>
	 *          [7] cross-entropy <br>
	 *          [8] duration accuracy, if applicable (with num and denom multiplied by 2 to get rid of halves)
	 */
	public static ErrorFraction[] getMetricsSingleFold(List<List<Integer>> assignmentErrors, 
		List<List<Integer>> allPredictedVoices, List<double[]> ntwOutputsForCE, 
		List<List<Double>> groundTruthVoiceLabels, List<Integer[]> equalDurationUnisonsInfo,
		boolean isTestOrAppMode) {

		ErrorFraction[] results = new ErrorFraction[10];
		Arrays.fill(results, null);
				
		Map<String, Double> modelParameters = Runner.getModelParams();
		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()]; 
		boolean modelDuration = m.getModelDuration();
		boolean isTablatureCase = Runner.getDataset().isTablatureSet();
//		int highestNumberOfVoices = modelParameters.get(Runner.HIGHEST_NUM_VOICES).intValue();
		int highestNumberOfVoices = Runner.getHighestNumVoicesTraining(Runner.getDeployTrainedUserModel());

		// acc, acc_dur
		results[ACC_IND] = 
			ErrorCalculator.calculateAccuracy(assignmentErrors, isTablatureCase, false);
		if (modelDuration) {
			results[ACC_DUR_IND] = 
				ErrorCalculator.calculateAccuracy(assignmentErrors, isTablatureCase, true);
		}
		
		
//		System.out.println("allPredictedVoices:");
//		System.out.println(allPredictedVoices);
//		System.out.println("groundTruthVoiceLabels:");
//		System.out.println(groundTruthVoiceLabels);
//		System.out.println("equalDurationUnisonsInfo:");
//		System.out.println(equalDurationUnisonsInfo);
//		System.out.println("highestNumberOfVoices:");
//		System.out.println(highestNumberOfVoices);
		
		// Added 28.01.2020 to print prc and rcl per voice for tablature (only in test/app mode)
		List<ErrorFraction[]> prf = 
			ErrorCalculator.calculatePrecisionRecallF1PerVoice(allPredictedVoices, 
			groundTruthVoiceLabels,	equalDurationUnisonsInfo, highestNumberOfVoices);
		if (isTestOrAppMode) {
			for (int i = 0; i < prf.size(); i++) {
				ErrorFraction[] curr = prf.get(i);
				TestManager.prcRcl += "voice = " + i + "\r\n";
				TestManager.prcRcl += 
					"prc" + "\t" + curr[0].getNumer() + "\t" + curr[0].getDenom() + "\t" + 
					curr[0].toDouble() + "\r\n";
				TestManager.prcRcl += 
					"rcl" + "\t" + curr[1].getNumer() + "\t" + curr[1].getDenom() + "\t" +
					curr[1].toDouble() + "\r\n";
				TestManager.prcRcl += 
					"F1"  + "\t" + curr[2].getNumer() + "\t" + curr[2].getDenom() + "\t" + 
					curr[2].toDouble() + "\r\n";
			}
		}

		// prc, rcl, snd, cmp, AVC, cre
		if (!isTablatureCase) {
			ErrorFraction[] precRec = 
				ErrorCalculator.calculateAvgPrecisionRecallF1(allPredictedVoices, 
				groundTruthVoiceLabels,	equalDurationUnisonsInfo, highestNumberOfVoices)[0];
			results[PRC_IND] = precRec[0];
			results[RCL_IND] = precRec[1];
		}
		ErrorFraction[] sndCmp = 
			ErrorCalculator.calculateAvgSoundnessAndCompleteness(allPredictedVoices, 
			groundTruthVoiceLabels,	equalDurationUnisonsInfo, highestNumberOfVoices)[0];
		results[SND_IND] = sndCmp[0];
		results[CMP_IND] = sndCmp[1];	
		if (!isTablatureCase) {	// TODO or if (metricsUsed.contains(Metric.AVC))?
			double avc = ErrorCalculator.calculateAVC(allPredictedVoices, 
				groundTruthVoiceLabels, equalDurationUnisonsInfo, highestNumberOfVoices);
			results[AVC_IND] = new ErrorFraction(avc);
		}
		if (metricsUsed.contains(Metric.CRE)) {
			double[] CE = 
				ErrorCalculator.calculateCrossEntropy(ntwOutputsForCE, groundTruthVoiceLabels);
			results[CRE_IND] = new ErrorFraction(CE[1]);
		}
		return results;
	}


	/**
	 * Creates a String[][] table containing the performance information for the given fold.
	 * 
	 * @param fold
	 * @param mode
	 * @param resultsEF
	 * @param resultsDbl
	 * @param assigErrs
	 * @param conflictIndices
	 * @param existingRows Is <code>null</code> in training mode. <br> 
	 *                     Contains in test and application mode: <br>
	 *                     as element 0: the training results for this fold <br> 
	 *                     Contains in application mode: <br> 
	 *                     as element 1: the test results for this fold <br>
	 * @param isForDuration
	 * @param times
	 * @return
	 */
	public static String[][] createCSVTableSingleFold(int fold, int mode, 
		ErrorFraction[] resultsEF, List<List<Integer>> assigErrs, List<List<Integer>> 
		conflictIndices, String[][] existingRows, boolean isForDuration, long[] times) {

		boolean isTablatureCase = Runner.getDataset().isTablatureSet();
		Map<String, Double> modelParameters = Runner.getModelParams();
		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()]; 
		DecisionContext dc = m.getDecisionContext();
		ModellingApproach ma = 
			Runner.ALL_MODELLING_APPROACHES[modelParameters.get(Runner.MODELLING_APPROACH).intValue()]; 
		boolean useCV = ToolBox.toBoolean(modelParameters.get(Runner.CROSS_VAL).intValue());
		
		long trTePrePrTime = times[0];
		long time = times[1];
		
		int rows = 4;
		if (m.getDecisionContext() == DecisionContext.BIDIR || ma == ModellingApproach.HMM) {
			rows = 3;
		}
		
		// Add rows for trn and tst preprocessing times
		int rowsToAdd = 0;
		if (fold == 1 && !isForDuration) {
			rowsToAdd = 3;
		}

		String[][] csvTable = new String[rows + rowsToAdd][csvLegend.size()];
		for (int i = 0; i < csvLegend.size(); i++) {
			csvTable[0][i] = csvLegend.get(i);
		}

		// Determine the relevant sublist (i.e., voice or duration) of assigErrs and conflictIndices
		// and set all metrics calculated and their indices in resultsEF (resultsDbl, relSublistAssigErrs) 
		Map<Metric, Integer> allMetrics = new LinkedHashMap<Metric, Integer>();
		List<Integer> relSublistAssigErrs = new ArrayList<Integer>();
		List<Integer> relSublistConflInd = null;
		if (!isForDuration) {
			// Skip if training mode in HMM case
			if (!(ma == ModellingApproach.HMM && mode == Runner.TRAIN)) {
				relSublistAssigErrs = assigErrs.get(ErrorCalculator.GENERAL_VOICE);
				if (ma == ModellingApproach.N2N &&  
					((mode == Runner.TEST && dc == DecisionContext.BIDIR) || mode == Runner.APPL)){
					relSublistConflInd = conflictIndices.get(0);
				}
			}
			for (String s : csvLegend) {
				Metric mtr = null;
				for (Metric curr : metricsUsed) {
					if (curr.getStringRep().equals(s)) {
						mtr = curr;
						break;
					}
				}
				if (mtr != null) {
					// EFs, I, and runtime always; O, S, H only in tab case; CNF only in appl mode 
					if (mtr.isEF() || mtr == Metric.INCORR || mtr == Metric.RUNTIME ||
						isTablatureCase && (mtr == Metric.OVERL || mtr == Metric.SUPERFL || 
						mtr == Metric.HALF) || 
						mtr == Metric.CNF && ((mode == Runner.TEST && dc == DecisionContext.BIDIR)
						|| mode == Runner.APPL)) {
						allMetrics.put(mtr, mtr.arrInd());	
					}
				}
			}
		}
		else {
			relSublistAssigErrs = assigErrs.get(ErrorCalculator.GENERAL_DUR);
			allMetrics.put(Metric.ACC, ACC_DUR_IND);
			allMetrics.put(Metric.INCORR, ErrorCalculator.NUM_INCORRECT);
			allMetrics.put(Metric.OVERL, ErrorCalculator.NUM_OVERLOOKED);
			allMetrics.put(Metric.RUNTIME, RUNTIME_IND);
			if (ma == ModellingApproach.N2N &&  
				((mode == Runner.TEST && dc == DecisionContext.BIDIR) || mode == Runner.APPL)){
				relSublistConflInd = conflictIndices.get(1);
				allMetrics.put(Metric.CNF, Metric.CNF.arrInd());
			}
		}

		// Fill currentRow
		String[] currRow = new String[csvLegend.size()];
		Arrays.fill(currRow, "");
		// a. Mode and fold
		int displayedMode = mode;
		if ((mode == Runner.TEST && dc == DecisionContext.BIDIR) || 
			mode == Runner.TEST && ma == ModellingApproach.HMM) {
			displayedMode = Runner.APPL;
		}
		currRow[csvLegend.indexOf(MODE)] = 
//			new String[]{Runner.train, Runner.test, Runner.application}[mode];
			new String[]{Runner.train, Runner.test, Runner.application}[displayedMode];
		String foldStr = "";
		if (useCV) {
			foldStr = ToolBox.zerofy(fold, ToolBox.maxLen(fold));
		}
		currRow[csvLegend.indexOf(FOLD)] = foldStr;
		// b. Metrics
		for (Map.Entry<Metric, Integer> entry : allMetrics.entrySet()) {
			Metric metr = entry.getKey();
			int ind = entry.getValue();
			int currInd = csvLegend.indexOf(metr.getStringRep());
			// Only if not training mode in HMM case
			if (!(ma == ModellingApproach.HMM && mode == Runner.TRAIN)) {
				if (metr.isEF()) {
					// Only if the EF metric is not null, i.e., if it is calculated (see 
					// getMetricsSingleFold() for implementation),
					if (resultsEF[ind] != null) { // TODO always calculate all and remove if
						if ((metr == Metric.NTW_ERR && mode == Runner.TRAIN) || metr == Metric.AVC ||
							metr == Metric.CRE) {
							currRow[currInd] = String.valueOf(resultsEF[ind].getNumerDbl());
						}
						else {
							currRow[currInd] = String.valueOf(resultsEF[ind].getNumer());
							currRow[currInd + 1] = String.valueOf(resultsEF[ind].getDenom());
						}
					}
				}
				else {
					if (metr != Metric.CNF) {
						if (metr == Metric.RUNTIME) {
							if (!isForDuration) {
								currRow[currInd] = String.valueOf(time); 
							}
						}
						else {
							currRow[currInd] = String.valueOf(relSublistAssigErrs.get(ind));
						}
					}
					else {
						if (ma == ModellingApproach.N2N) {
							currRow[currInd] = String.valueOf(relSublistConflInd.size());
						}
					} 
				}
			}
			// If training mode in HMM case: set only runtime
			else {
				if (metr == Metric.RUNTIME) {
					currRow[currInd] = String.valueOf(time); 
				}
//				else {
//					currRow[currInd] = "";
//				}
			}
		}

		// 2. Set current row
		int rntInd = csvLegend.indexOf(Metric.RUNTIME.getStringRep());
		csvTable[mode + 1] = currRow;

		// Set trn and tst preprocessing time
		if (fold == 1 && !isForDuration) {	
			Arrays.fill(csvTable[rows], "");
			if (mode != Runner.APPL) {
				Arrays.fill(csvTable[mode + rows+1], "");
				csvTable[mode + rows+1][rntInd] = String.valueOf(trTePrePrTime);
			}
		}

		// 3. Set any existing row(s)
//		if (!(ma == ModellingApproach.HMM && mode == Runner.TRAIN)) {
			// Test and appl mode: set train results 
			if (mode == Runner.TEST || mode == Runner.APPL) {
				csvTable[Runner.TRAIN + 1] = existingRows[0];
				if (fold == 1 && !isForDuration) {
					csvTable[(Runner.TRAIN + 1) + rows] = existingRows[existingRows.length-2];
				}
			}
			// Appl mode: also set test results
			if (mode == Runner.APPL) {
				csvTable[Runner.TEST + 1] = existingRows[1];
				if (fold == 1 && !isForDuration) {
					csvTable[(Runner.TEST + 1) + rows] = existingRows[existingRows.length-1];
				}
			}
//		}
		return csvTable;
	}


	/**
	 * Creates a String[][] table containing the performance information over all folds.
	 * 
	 * @param numFolds
	 * @param path
	 * @return
	 */
	public static String[][] createCSVTableAllFolds(int numFolds, String path, 
		boolean isForDuration, long[] times) {
		Map<String, Double> modelParameters = Runner.getModelParams();
		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()];
		DecisionContext dc = m.getDecisionContext();
		ModellingApproach ma = 
			Runner.ALL_MODELLING_APPROACHES[modelParameters.get(Runner.MODELLING_APPROACH).intValue()];
		
		int numRows = 1 + 2 + 2*(numFolds + 2) + (numFolds + 1) + 1;
		if (dc == DecisionContext.BIDIR) {
			numRows = 1 + 2 + 2*(numFolds + 2) + 1;
		}
		String[][] csvTable = new String[numRows][csvLegend.size()];
		for (int i = 0; i < csvLegend.size(); i++) {
			csvTable[0][i] = csvLegend.get(i);
		}
		for (int i = 1; i < csvTable.length; i++) {
			Arrays.fill(csvTable[i], "");
		}
		
		// Add prp and pst runtimes
		int rntInd = csvLegend.indexOf(Metric.RUNTIME.getStringRep());
		csvTable[1][0] = Runner.prePr;
		csvTable[csvTable.length-1][0] = Runner.postPr; 
		if (!isForDuration) {	
			csvTable[1][rntInd] = String.valueOf(times[0]);		
			csvTable[csvTable.length-1][rntInd] = String.valueOf(times[times.length-1]);
		}
		
		String perf = Runner.perf;
		if (isForDuration) {
			perf = Runner.perfDur;
		}

		int to = Runner.APPL;
		if (dc == DecisionContext.BIDIR || ma == ModellingApproach.HMM) {
			to = Runner.TEST;
		}
		
		String[] trTePrePrTimes = new String[]{"", ""};
		for (int i = Runner.TRAIN; i <= to; i++) {
			// Determine the first row for the current block of fold results
//			int firstRow = (i + 1) + (i * numFolds);
			int firstFoldRow = (i + 1+2) + (i * numFolds) + (i+1);
			if (i == Runner.APPL) {
				firstFoldRow--;
			}
			// Results per fold
			String mode = "";
			for (int j = 0; j < numFolds; j++) {
				String foldNum = ToolBox.zerofy((j+1), ToolBox.maxLen(numFolds));
				// Retrieve the String[] with the metrics for the current mode/fold
				String csvStr = 	
					ToolBox.readTextFile(new File(path + "fold_" + foldNum + "/" + 
					perf + ".csv"));
				String[][] curr = ToolBox.retrieveCSVTable(csvStr);
				System.out.println("fold = " + (j+1));
				for (String[] s : curr) {
					System.out.println(Arrays.toString(s));
				}
				String[] currValues = curr[i + 1];
				// Remove mode from all folds (trn, tst) or all folds other than 1 (app) and set
				mode = currValues[0];
				if (i != Runner.APPL || (i == Runner.APPL && (j != 0))) {
					currValues[0] = "";
				}
				csvTable[firstFoldRow + j] = currValues;
				
				// Fold 1, trn/tst: get preprocessing times
				// NB The preprocessing times are stored only in fold 1, int the rnt column
				// below the app (unidir) or tst (bidir) results (separated by one empty row)
				if (j == 0 && !isForDuration && i != Runner.APPL) {
					if (dc == DecisionContext.UNIDIR && ma != ModellingApproach.HMM) {
						trTePrePrTimes[i] = curr[i + 5][rntInd];
					}
					else if (dc == DecisionContext.BIDIR || ma == ModellingApproach.HMM) {
						trTePrePrTimes[i] = curr[i + 4][rntInd];
					}	
				}
			}
			// Add preRow for trn and tst
			if (i != Runner.APPL) {
				int preRow = firstFoldRow - 1;
				csvTable[preRow][0] = mode;
				csvTable[preRow][1] = Runner.prePr;
				csvTable[preRow][rntInd] = String.valueOf(trTePrePrTimes[i]);
			}
		}	
		return csvTable;
	}
	
	
	/**
	 * Creates a String[][] table containing the performance information over all folds.
	 * 
	 * @param numFolds
	 * @param path
	 * @return
	 */
	private static String[][] createCSVTableAllFoldsOLD(int numFolds, String path, 
		boolean isForDuration, long[] times) {

		String[][] csvTable = new String[(numFolds * 3) + 3][csvLegend.size()];
		for (int i = 0; i < csvLegend.size(); i++) {
//			csvTable[0][i] = CSV_LEGEND.get(i).getStringRep();
			csvTable[0][i] = csvLegend.get(i);
		}
		for (int i = 1; i < csvTable.length; i++) {
			Arrays.fill(csvTable[i], "");
		}

		String perf = Runner.perf;
		if (isForDuration) {
			perf = Runner.perfDur;
		}

		int to = Runner.APPL;
		Model m = Runner.ALL_MODELS[Runner.getModelParams().get(Runner.MODEL).intValue()];
		if (m.getDecisionContext() == DecisionContext.BIDIR) {
			to = Runner.TEST;
		}
		for (int i = Runner.TRAIN; i <= to; i++) {
			// Determine the first row for the current block of fold results
			int firstRow = (i + 1) + (i * numFolds);
			// Results per fold
			for (int j = 0; j < numFolds; j++) {
				String foldNum = ToolBox.zerofy((j+1), ToolBox.maxLen(numFolds));
				// Retrieve the String[] with the metrics for the current mode/fold
				String csvStr = 	
					ToolBox.readTextFile(new File(path + "fold_" + foldNum + "/" + 
					perf + ".csv"));
				String[] currValues = ToolBox.retrieveCSVTable(csvStr)[i + 1];
				// Remove the mode from folds other than 1 and set
				if (j != 0) {
					currValues[0] = "";
				}
				csvTable[firstRow + j] = currValues;
			}
		}	
		return csvTable;
	}


	/**
	 * Gets the performance information.
	 * 
	 * @param csvTables A List containing
	 * 					<lu>
	 * 						<li>as element 0: the csv table for voice</li> 
	 * 						<li>as element 1: the csv table for duration, or, if not applicable,
	 * 							<code>null</code>.</li>
	 * 					</lu>
	 * @param mode N/a when called during overall assessment.
	 * @param numFolds N/a when called during training, test, or application mode.
	 * @param assigErrs <code>null</code> when called during overall assessment.
	 * @param conflictIndices <code>null</code> when called during training and test mode and 
	 * 						during overall assessment.
	 * @param indicesOfFirstPieceOfTestSets N/a when called during training or evaluation.
	 * @return
	 */
	public static String getPerformanceInfo(List<String[][]> csvTables, List<Integer> modes,
		int numFolds, List<List<Integer>> assigErrs, List<Integer[]> indicesOfFirstPieceOfTestSets) {

//		Map<String, Double> modelParameters = Runner.getModelParams();
//		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()]; 
//		DecisionContext dc = m.getDecisionContext();  
//		boolean modelDuration = m.getModelDuration();
//		boolean isTablatureCase = Runner.getDataset().isTablatureSet();
//		ModellingApproach ma = 
//			Runner.ALL_MODELLING_APPROACHES[modelParameters.get(Runner.MODELLING_APPROACH).intValue()];

		StringBuffer txt = new StringBuffer();
		txt.append("P E R F O R M A N C E\r\n");

		// Per-fold case
		if (assigErrs != null) {
			// Table
			txt.append(getPerformanceTable(csvTables, modes, -1, null));
//			txt.append("\r\n");
//			// Misassignment indices
//			txt.append(breakList(Metric.INCORR.getStringRep(),
//				assigErrs.get(ErrorCalculator.INCORRECT_VOICE), 1, metricsUsed.size()));
//			if (isTablatureCase) {
//				txt.append(breakList(Metric.OVERL.getStringRep(),
//					assigErrs.get(ErrorCalculator.OVERLOOKED_VOICE), 1, metricsUsed.size()));			
//				txt.append(breakList(Metric.SUPERFL.getStringRep(),
//					assigErrs.get(ErrorCalculator.SUPERFLUOUS_VOICE), 1, metricsUsed.size()));
//				txt.append(breakList(Metric.HALF.getStringRep(),
//					assigErrs.get(ErrorCalculator.HALF_VOICE), 1, metricsUsed.size()));
//			}			
//			if (ma == ModellingApproach.N2N &&  
//				((mode == Runner.TEST && dc == DecisionContext.BIDIR) 
//				|| mode == Runner.APPL)){
//					txt.append(breakList(Metric.CNF.getStringRep(), conflictIndices.get(0), 
//						1, metricsUsed.size()));
//				}
//			if (modelDuration && dc == DecisionContext.UNIDIR) {
//				txt.append(breakList(Metric.INCORR.getStringRep() + "*",
//					assigErrs.get(ErrorCalculator.INCORRECT_DUR), 1, metricsUsed.size()));
//				txt.append(breakList(Metric.OVERL.getStringRep() + "*",
//					assigErrs.get(ErrorCalculator.OVERLOOKED_DUR), 1, metricsUsed.size()));
//				if (ma == ModellingApproach.N2N && mode == Runner.APPL) {
//					txt.append(breakList(Metric.CNF.getStringRep() + "*", 
//						conflictIndices.get(1), 1, metricsUsed.size()));
//				}
//			}
		}
		// Overall case
		else {
			txt.append(getPerformanceTable(csvTables, null, numFolds, indicesOfFirstPieceOfTestSets));
		}
		return txt.toString();
	}


	private static String getPerformanceTable(List<String[][]> csvTables, List<Integer> modes, 
		int numFolds, List<Integer[]> indicesOfFirstPieceOfTestSets) {	
		StringBuffer txt = new StringBuffer();

		Map<String, Double> modelParameters = Runner.getModelParams();
		ModellingApproach ma = 
			Runner.ALL_MODELLING_APPROACHES[modelParameters.get(Runner.MODELLING_APPROACH).intValue()];
		boolean deployTrainedUserModel = Runner.getDeployTrainedUserModel();
//			ToolBox.toBoolean(modelParameters.get(Runner.DEPLOY_TRAINED_USER_MODEL).intValue());				
		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()];
		boolean modelDuration = m.getModelDuration();

		String[][] csvTable = csvTables.get(0);
		String[][] csvTableDur = null;
		if (modelDuration && m.getDecisionContext() == DecisionContext.UNIDIR) {
			csvTableDur = csvTables.get(1);
		}

		// Make elements
		String lineSeg = "--------";
		StringBuffer line = new StringBuffer();
		line.append(lineSeg);
		line.append(lineSeg);
		StringBuffer legendLineOne = new StringBuffer();
		legendLineOne.append(ToolBox.tabify(MODE, 1));
		legendLineOne.append(ToolBox.tabify(FOLD, 1));
		legendLineOne.append(ToolBox.tabify("metric", 1));
		StringBuffer legendLineTwo = new StringBuffer();
		legendLineTwo.append(ToolBox.tabify("", 1));
		legendLineTwo.append(ToolBox.tabify("", 1));
		StringBuffer footnote = new StringBuffer();
		for (Metric mtr : metricsUsed) {
			line.append(lineSeg);
			if (mtr != metricsUsed.get(metricsUsed.size()-1)) {
				legendLineOne.append(ToolBox.tabify("", 1));
			}
			legendLineTwo.append(ToolBox.tabify(mtr.getStringRep(), 1));
		}
		line.append("\r\n");
		legendLineOne.append("\r\n");
		legendLineTwo.append("\r\n");
		if (modelDuration && m.getDecisionContext() == DecisionContext.UNIDIR) {
			// Adapt legend
			for (String s : new String[]{Metric.ACC.getStringRep(), Metric.INCORR.getStringRep(),
				Metric.OVERL.getStringRep(), Metric.CNF.getStringRep()}) {
				String r = ToolBox.tabify(s, 1);
				int start = legendLineTwo.indexOf(r);
				legendLineTwo.replace(start, start + r.length(), ToolBox.tabify(s + "*", 1));  
			}			
			footnote.append("* top values apply to voice, bottom values to duration\r\n");
		}
		StringBuffer indentedLineOne = 
			new StringBuffer(line).replace(0, 2*lineSeg.length(), ToolBox.tabify("", 2));
		StringBuffer indentedLineTwo = 
			new StringBuffer(line).replace(0, lineSeg.length(), ToolBox.tabify("", 1));

		// Combine elements
		txt.append(line);
		txt.append(legendLineOne);
		txt.append(indentedLineOne);
		txt.append(legendLineTwo);
		txt.append(line);

		List<Metric> metricsUsedPadded = new ArrayList<Metric>(metricsUsed);
		metricsUsedPadded.add(0, null);
		metricsUsedPadded.add(0, null);
		// Single fold case
		if (numFolds == -1) {			
			for (int mode : modes) {
				String[] csvRow = csvTable[1 + mode];
				String[] csvRowDur = null;
				if (modelDuration && m.getDecisionContext() == DecisionContext.UNIDIR) {
					csvRowDur = csvTableDur[1 + mode];
				}
				txt.append(getPerformanceTableEntry(metricsUsedPadded, csvRow, csvRowDur));
			}
			txt.append(line);
			txt.append(footnote);
		}
		// Overall case
		else {
			// Pre- and postprocessing times
			int rntInd = csvLegend.indexOf(Metric.RUNTIME.getStringRep());
			int sumTime = 0;
			StringBuffer prePr = new StringBuffer();
			StringBuffer postPr = new StringBuffer();
			StringBuffer totalTime = new StringBuffer();
			for (int i = 0; i < metricsUsedPadded.size(); i++) {
				if (i == MODE_IND) {
					prePr.append(ToolBox.tabify(Runner.prePr, 1));
					postPr.append(ToolBox.tabify(Runner.postPr, 1));
					totalTime.append(ToolBox.tabify("", 1));
				}
				else {
					if (metricsUsedPadded.get(i) != Metric.RUNTIME) {
						prePr.append(ToolBox.tabify("", 1));
						postPr.append(ToolBox.tabify("", 1));
						if (i == FOLD_IND) {
							totalTime.append(ToolBox.tabify(Runner.total, 1));
						}
						else {
							totalTime.append(ToolBox.tabify("", 1));
						}
					}
					else {	
						String pre = csvTable[1][rntInd];
						String post = csvTable[csvTable.length-1][rntInd];
						sumTime += Integer.parseInt(pre);
						sumTime += Integer.parseInt(post);
						prePr.append(ToolBox.tabify(pre, 1)).append("\r\n");
						postPr.append(ToolBox.tabify(post, 1)).append("\r\n");
					}
				}
			}
			txt.append(prePr);
			txt.append(line);

			List<double[][]> avgsAndStdevs = 
				getAvgStDevTotals(metricsUsed, csvTable, numFolds, false);
			List<double[][]> avgsAndStdevsDur = null;
			if (modelDuration && m.getDecisionContext() == DecisionContext.UNIDIR) {
				avgsAndStdevsDur = 
					getAvgStDevTotals(metricsUsed, csvTableDur, numFolds, true);
			}
			
			int to = Runner.APPL;
			if (m.getDecisionContext() == DecisionContext.BIDIR || ma == ModellingApproach.HMM
				|| Runner.ignoreAppl) {
				to = Runner.TEST;
			}
			List<Integer[]> avgs = new ArrayList<Integer[]>();
			List<Integer[]> stDevs = new ArrayList<Integer[]>();
			for (int i = Runner.TRAIN; i <= to; i++) {
				avgs.add(new Integer[metricsUsed.size()]);
				stDevs.add(new Integer[metricsUsed.size()]);
			}
			
			// For the non-transfer learning experiments and for the transfer learning 
			// experiments with only one test set
			if (!deployTrainedUserModel || (deployTrainedUserModel && indicesOfFirstPieceOfTestSets == null)) {
				// Get results per mode per fold		
				for (int i = Runner.TRAIN; i <= to; i++) {	
					// Determine the first row for the current block of fold results
//					int firstFoldRow = (i + 1) + (i*numFolds);			
					int firstFoldRow = (i + 1+2) + (i * numFolds) + (i+1);
					if (i == Runner.APPL) {
						firstFoldRow--;
					}
					
					// 0. Preprocessing
					if (i != Runner.APPL) {
						StringBuffer prp = new StringBuffer();
						for (int j = 0; j < metricsUsedPadded.size(); j++) {
							Metric mtr = metricsUsedPadded.get(j);
							if (j == MODE_IND) {
								prp.append(ToolBox.tabify(csvTable[firstFoldRow-1][MODE_IND], 1));
							}
							else if (j == FOLD_IND) {
								prp.append(ToolBox.tabify(Runner.prePr, 1));
							}
							else if (mtr == Metric.RUNTIME) {
								double d = Double.parseDouble(csvTable[firstFoldRow-1][rntInd]);
								prp.append(ToolBox.tabify(String.valueOf((int)d), 1));
							}
							else {
								prp.append(ToolBox.tabify("", 1));
							}
						}
						prp.append("\r\n");
						txt.append(prp);
					}
					
					// 1. Results per fold
					for (int j = 0; j < numFolds; j++) { 
						// Make the current row in txt
						String[] currCsvRow = csvTable[firstFoldRow + j];
						String[] currCsvRowDur = null;
						if (modelDuration && m.getDecisionContext() == DecisionContext.UNIDIR) { // 29 januari
							currCsvRowDur = csvTableDur[firstFoldRow + j];
						}
						txt.append(getPerformanceTableEntry(metricsUsedPadded, currCsvRow, 
							currCsvRowDur));
					}
					txt.append(indentedLineTwo);

					// 2. Averages, standard deviations, and totals
					StringBuffer avgStr = new StringBuffer();
					StringBuffer stdevStr = new StringBuffer();	
					StringBuffer totalsStr = new StringBuffer();
					List<double[][]> allCurrAvgsAndStdevs = new ArrayList<double[][]>();
					allCurrAvgsAndStdevs.add(avgsAndStdevs.get(i)); // values for curr mode
					if (modelDuration && m.getDecisionContext() == DecisionContext.UNIDIR) {
						allCurrAvgsAndStdevs.add(avgsAndStdevsDur.get(i));
					}
					for (int j = 0; j < allCurrAvgsAndStdevs.size(); j++) {
						double[][] curr = allCurrAvgsAndStdevs.get(j);
						String[][] currCsvTable = csvTables.get(j);
						for (int k = 0; k < metricsUsedPadded.size(); k++) {
							Metric mtr = metricsUsedPadded.get(k);
							boolean isEmpty = false;
							if (mtr != null) {
								isEmpty = currCsvTable[firstFoldRow][csvLegend.indexOf(mtr.getStringRep())].equals("");
							}
//							if (mtr == Metric.MODE || (!mtr.isEF() && mtr != Metric.FOLD) 
//								|| isEmpty) {
							if (k == MODE_IND || (mtr != null && !mtr.isEF() && k != FOLD_IND) 
								|| isEmpty) {
								String s = ToolBox.tabify("", 1);
								avgStr.append(s);
								stdevStr.append(s);
								if (j == 0) {
									if (mtr == Metric.RUNTIME) {
										int totVal = (int) curr[2][csvLegend.indexOf(mtr.getStringRep())];
										sumTime += totVal;
										totalsStr.append(ToolBox.tabify(String.valueOf(totVal), 1));
									}
									else {
										totalsStr.append(s);
									}
								}
							}
							else if (k == FOLD_IND) {
								String a = Runner.average;
								String s = Runner.stDev;
								String t = Runner.total;
								if (j == 1) {
									a = "";
									s = "";
//									t = "";
								}
								avgStr.append(ToolBox.tabify(a, 1));
								stdevStr.append(ToolBox.tabify(s, 1));
								if (j == 0) {
									totalsStr.append(ToolBox.tabify(t, 1));
								}
							}
							else if (mtr.isEF() && !isEmpty) {
								int maxLen = 2;
								if (mtr == Metric.NTW_ERR) {
									maxLen = 0;
								}
								double avgVal = curr[0][csvLegend.indexOf(mtr.getStringRep())];
								double stdevVal = curr[1][csvLegend.indexOf(mtr.getStringRep())];
								avgStr.append(ToolBox.tabify(ToolBox.formatDouble(avgVal, maxLen, 5), 1));
								stdevStr.append(ToolBox.tabify(ToolBox.formatDouble(stdevVal, maxLen, 5), 1));
								if (j == 0) {
									totalsStr.append(ToolBox.tabify("", 1));
								}
							}
						}
						avgStr.append("\r\n");
						stdevStr.append("\r\n");
						if (j == 0) {
							totalsStr.append("\r\n");
						}
					}				
					txt.append(avgStr);
					txt.append(stdevStr);
					txt.append(totalsStr);
					txt.append(line);
				}
//				if (modelDuration) {
				txt.append(postPr);
				txt.append(line); 
				// Complete totalTime
				totalTime.append(ToolBox.tabify(String.valueOf(sumTime), 1)).append("\r\n");
				txt.append(totalTime);
				txt.append(line); 
				txt.append(footnote);
//				}
			}
			// For the transfer learning experiments with multiple test sets: test on all
			else {
				for	(int i = 0; i < indicesOfFirstPieceOfTestSets.size(); i++) {
					Integer[] currIndices = indicesOfFirstPieceOfTestSets.get(i);
//					List<String> currentTestSetPieceNames = 
//						testSetPieceNames.subList(currIndices[0], currIndices[1]);
//					evaluation = 
//						evaluation.concat(ErrorCalculatorTab.getErrorsFromFiles(modelParameters, 
//						trainingSetPieceNames, currentTestSetPieceNames, path));	 
				}
			}
		}
		return txt.toString();
	}


	/**
	 * 
	 * @param metricsUsedPadded
	 * @param csvRow
	 * @param csvRowDur
	 * @return
	 */
	private static String getPerformanceTableEntry(List<Metric> metricsUsedPadded, 
		String[] csvRow, String[] csvRowDur) {

		Model m = Runner.ALL_MODELS[Runner.getModelParams().get(Runner.MODEL).intValue()];  
		boolean modelDuration = m.getModelDuration();

		StringBuffer txt = new StringBuffer();
		
		// List the applicable csv rows (i.e., only voice or voice and duration) 
		List<String[]> allCsvRows = new ArrayList<String[]>();
		allCsvRows.add(csvRow);
		if (modelDuration && m.getDecisionContext() == DecisionContext.UNIDIR) {
			String[] csvRowDurAdp = csvRowDur.clone();
			csvRowDurAdp[csvLegend.indexOf(MODE)] = "";
			csvRowDurAdp[csvLegend.indexOf(FOLD)] = "";
			csvRowDurAdp[csvLegend.indexOf(Metric.NTW_ERR.getStringRep())] = "";
			allCsvRows.add(csvRowDurAdp);
		}

		// Follow the same procedure for each csv row
		for (String[] currRow : allCsvRows) {
			for (int i = 0; i < metricsUsedPadded.size(); i++) {
				Metric mtr = metricsUsedPadded.get(i);	
				int ind;				
				// If not metric (mode or fold)
				if (mtr == null) {
					if (i == MODE_IND) {
						ind = csvLegend.indexOf(MODE); 
					}
					else {
						ind = csvLegend.indexOf(FOLD);
					}	
				}
				// If metric
				else {
					ind = csvLegend.indexOf(mtr.getStringRep());
				}
				String val = currRow[ind]; // can be an int, a double, or an empty String
				String formatted;

				// Empty Strings or non-EF ints (misassignment or conflict)
				if (mtr == null || val.equals("") || !mtr.isEF()) {
					formatted = val;
				}
				// EF ints and doubles
				else {
					double d;
					int maxLen = 2;
					if (mtr == Metric.NTW_ERR || mtr == Metric.AVC || mtr == Metric.CRE) {
						d = Double.parseDouble(val);
						if (mtr == Metric.NTW_ERR) {
							maxLen = 0; // TODO it is assumed that value < 0
						}
						if (mtr == Metric.CRE) {
							double sum = d / ErrorCalculator.CRE_SUM_DIV;
							int j = Integer.parseInt(currRow[csvLegend.indexOf(Metric.ACC) + 1]);
							if (Runner.getDataset().isTablatureSet()) {
								j /= 2;
							}							
							d = (-1.0/j) * sum;
						}
					}
					else {
						d = ((double) Integer.parseInt(val) / 
							Integer.parseInt(currRow[ind + 1])) * 100;
					}
					formatted = ToolBox.formatDouble(d, maxLen, 5);
				}
				txt.append(ToolBox.tabify(formatted, 1));
			}
			txt.append("\r\n");
		}
		return txt.toString();
	}


	/**
	 * Given a csv table and the metrics used, calculates for training, test, and application
	 * mode the averages and the standard deviations for all metrics.
	 *  
	 * @param argMetricsUsed
	 * @param csvTable
	 * @param numFolds
	 * @return A List<double[][]>, containing <br>
	 *         as element 0 (1, 2): a double[][] containing the averages (element 0) and standard 
	 *                             deviations (element 1) in training (test, application) mode
	 */
	// TESTED
	static List<double[][]> getAvgStDevTotals(List<Metric> argMetricsUsed, String[][] csvTable,
		int numFolds, boolean isForDur) {
		List<double[][]> avgsAndStDevs = new ArrayList<double[][]>();
	
		Map<String, Double> modelParameters = Runner.getModelParams();
		ModellingApproach ma = 
			Runner.ALL_MODELLING_APPROACHES[modelParameters.get(Runner.MODELLING_APPROACH).intValue()];
		Model mdl = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()];
		
		int to = Runner.APPL;
		if (mdl.getDecisionContext() == DecisionContext.BIDIR || ma == ModellingApproach.HMM 
			|| Runner.ignoreAppl) {
			to = Runner.TEST;
		}
		for (int i = Runner.TRAIN; i <= to; i++) {
//			int firstRow = (i + 1) + (i * numFolds);
			int firstRow = (i + 1+2) + (i * numFolds) + (i);
			int lastRow = firstRow + (numFolds+1);
			if (i == Runner.APPL) {
				lastRow--;
			}

			String[][] csvSubTable = Arrays.copyOfRange(csvTable, firstRow, lastRow);
			// List the values per fold for calculation of avgs and stDevs; also list the
			// number of notes per fold (needed for the cre and AVC calculation)
			List<ErrorFraction>[] EFs = 
				(ArrayList<ErrorFraction>[]) new ArrayList<?>[csvLegend.size()];
			List<Integer> totals = new ArrayList<Integer>();
			for (int j = 0; j < EFs.length; j++) { // NB: do not use Arrays.fill(): gives the same list to each element
				EFs[j] = new ArrayList<ErrorFraction>();
			}
			List<Integer> notesPerFold = new ArrayList<Integer>();
			for (Metric m : argMetricsUsed) {
				int ind = csvLegend.indexOf(m.getStringRep());
				// For each fold
				for (int j = 0; j < csvSubTable.length; j++) {
					String val = csvSubTable[j][ind];
					if (m.isEF() && !val.equals("")){
						if (m == Metric.NTW_ERR || m == Metric.AVC || m == Metric.CRE) {
							double num = Double.parseDouble(val);  
							EFs[ind].add(new ErrorFraction(num));
						}
						else {
							int num = Integer.parseInt(val);
							int den = Integer.parseInt(csvSubTable[j][ind + 1]);
							EFs[ind].add(new ErrorFraction(num, den));
							// If m is acc: determine the number of notes in the current fold
							if (m == Metric.ACC) {
								int numNotes = den;
								if (Runner.getDataset().isTablatureSet()) {
									numNotes /= 2;
								}
								notesPerFold.add(numNotes);
							}
						}
					}
					else if (m == Metric.RUNTIME && !isForDur) {
						totals.add(Integer.parseInt(val));
					}
				}
			}

			// Calculate avg and stDevs
			double[][] currAvgsAndStDevs = new double[3][csvLegend.size()];
			// For each metric
			for (Metric m : argMetricsUsed) {
				int ind = csvLegend.indexOf(m.getStringRep());
				String valFold01 = csvSubTable[1][ind];
				if (m.isEF() && !valFold01.equals("")){
					List<ErrorFraction> currList = EFs[ind];					
					int sumNum = 0;
					int sumDen = 0;
					List<Double> errsPerFold = new ArrayList<Double>();
					double sumNumDbl = 0;
					List<Double> summedLogPs = new ArrayList<Double>();
					for (int j = 0; j < currList.size(); j++) {
						ErrorFraction ef = currList.get(j);
						if (m == Metric.NTW_ERR) { 
							errsPerFold.add(ef.getNumerDbl());
						}
						else if (m == Metric.AVC) {
							double avc = ef.getNumerDbl();
							sumNumDbl += avc * (notesPerFold.get(j));
							errsPerFold.add(avc);
						}
						else if (m == Metric.CRE) {
							double cre = ef.getNumerDbl();
							summedLogPs.add(cre);
							errsPerFold.add((-1.0/notesPerFold.get(j)) * (cre / Math.log(2)));
						}
						else {
							sumNum += ef.getNumer();
							sumDen += ef.getDenom();
							errsPerFold.add(ef.toDouble() * 100);
						}
					}
					double avg;
					int sumNotes = ToolBox.sumListInteger(notesPerFold);
					if (m == Metric.NTW_ERR) {
						avg = ToolBox.getAverage(errsPerFold);
					}
					else if (m == Metric.AVC) {
						avg = (sumNumDbl / sumNotes);
					}
					else if (m == Metric.CRE) {
						avg = (-1.0/sumNotes) * (ToolBox.sumListDouble(summedLogPs) / Math.log(2));
					}
					else {
						avg = ((double) sumNum / sumDen) * 100;	
					}
					currAvgsAndStDevs[0][ind] = avg;
					currAvgsAndStDevs[1][ind] = ToolBox.stDev(errsPerFold);
				}
				else if (m == Metric.RUNTIME && !isForDur) {
					currAvgsAndStDevs[2][ind] = (double) ToolBox.sumListInteger(totals);
				}
			}
			avgsAndStDevs.add(currAvgsAndStDevs);
		}
		return avgsAndStDevs;
	}


	/**
	 * Breaks the given list.
	 *  
	 * @param list
	 * @param numTabsIndent
	 * @param numTabsTotal Includes numTabsIndent
	 * @return
	 */
	// TESTED
	static String breakList(String prefix, List<Integer> list, int numTabsIndent, int numTabsTotal) {
		StringBuffer res = new StringBuffer();

		int tabLen = 8; // TODO assumes that a tab is 8 chars wide
		int len = (numTabsTotal * tabLen) - (numTabsIndent * tabLen);
		String linePrefix = ToolBox.tabify(prefix, numTabsIndent);
		String suffix = ",";
		String postSuffix = " ";
		StringBuffer currLine = new StringBuffer();
		int last = list.size() - 1;
		
		if (list.size() == 0) {
			res.append(linePrefix);
			res.append("\r\n");
		}
		else {
			for (int i = 0; i < list.size(); i++) {
				String s = String.valueOf(list.get(i)); 
				if (i == last) {
					suffix = "";
					postSuffix = "\r\n";
				}
	
				// Does s + suffix fit on currLine? Add s to currLine
				if (currLine.length() + s.concat(suffix).length() < len) {
					currLine.append(s);
				}
				// Does s + suffix not fit on currLine? Complete currLine and add s to next line
				else {
					currLine.append("\r\n");
					res.append(linePrefix).append(currLine);
					linePrefix = ToolBox.tabify("", numTabsIndent);
					currLine = new StringBuffer();
					currLine.append(s);
				}

				currLine.append(suffix).append(postSuffix);
				if (i == last) {
					res.append(linePrefix).append(currLine);
				}
			}
		}
		return res.toString();
	}
	
	
	public static String listDetails(String[][] detailsArr, String conflictRec/*,
		List<List<Integer>> conflictIndices, List<List<List<Integer>>> conflictInfo*/) {
		
		Map<String, Double> modelParameters = Runner.getModelParams();
		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()];
		ModellingApproach ma = 
			Runner.ALL_MODELLING_APPROACHES[modelParameters.get(Runner.MODELLING_APPROACH).intValue()];
		
		DecisionContext dc = m.getDecisionContext(); 
		boolean argModelDuration = m.getModelDuration();
		boolean argModelDurationAgain = 
			ToolBox.toBoolean(modelParameters.get(Runner.MODEL_DURATION_AGAIN).intValue());
		boolean modelDur = 
			(dc == DecisionContext.UNIDIR && argModelDuration || 
			dc == DecisionContext.BIDIR && argModelDuration && argModelDurationAgain);
		boolean isTablatureCase = Runner.getDataset().isTablatureSet();
		int labelSize = Transcription.MAXIMUM_NUMBER_OF_VOICES;
		int numTabsForModelOutput = 5;
		int mappingSize = 1;
		if (modelDur) {
			labelSize += Transcription.DURATION_LABEL_SIZE; 
			numTabsForModelOutput = 33;
		}
		if (ma == ModellingApproach.C2C || ma == ModellingApproach.HMM) {
			labelSize = 1;
			mappingSize = 4;
		}
		
		// Make a DecimalFormat for formatting long double values to four decimal places
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.UK);
		otherSymbols.setDecimalSeparator('.'); 
		DecimalFormat decFormOutput = new DecimalFormat(".0000", otherSymbols);
		decFormOutput.setRoundingMode(RoundingMode.DOWN);
//		DecimalFormat decFormLabel = new DecimalFormat("0.0   ", otherSymbols);

		// Write out labels and network output details for each note
		StringBuffer details = new StringBuffer();
		details.append("D E T A I L S" + "\r\n");

		List<String> cols = new ArrayList<String>();
		cols.add("note"); cols.add("chord"); cols.add("bar"); cols.add(""); 
		cols.add("voice"); 
		if (ma == ModellingApproach.N2N) {
			cols.add("model output");
			int empties = 3;
			if (modelDur) {
				empties = 31;
			}
			for (int j = 0; j < empties; j++) {
				cols.add("");
			}
		}
		else if (ma == ModellingApproach.C2C || ma == ModellingApproach.HMM) {
			cols.add("model output and mapping");
			cols.add(""); cols.add("");  
		}
		
		cols.add("voice"); cols.add(""); cols.add("category");

		String lineSeg = "--------";
		StringBuffer line = new StringBuffer();
		StringBuffer legend = new StringBuffer();
		StringBuffer footnote = new StringBuffer();
		for (String s : cols) {
			int i = 1;
			if (s.equals("model output")) {
				i = 2;
			}
			if (s.equals("model output and mapping")) {
				i = 3;
			}
			for (int j = 0; j < i; j++) {
				line.append(lineSeg);
			}
			legend.append(ToolBox.tabify(s, i));
		}

		// Adapt legend for duration
		if (modelDur) {
//		if (dc == DecisionContext.UNIDIR && argModelDuration || 
//			dc == DecisionContext.BIDIR && argModelDuration && argModelDurationAgain) {			
			for (String s : new String[]{"voice", "model output"}) {
				String r = ToolBox.tabify(s, 1);
				int start = legend.indexOf(r);
				legend.replace(start, start + r.length(), ToolBox.tabify(s + "*", 1));
				if (s.equals("voice")) {
					start = legend.lastIndexOf(r);
					legend.replace(start, start + r.length(), ToolBox.tabify(s + "*", 1));
				}
			}
			footnote.append("* top values apply to voice, bottom values to duration\r\n");
		}
		details.append(line).append("\r\n");
		details.append(legend).append("\r\n");
		details.append(line).append("\r\n");

		for (int i = 1; i < detailsArr.length; i++) {
			String[] s = detailsArr[i];
			StringBuffer detailsLine = new StringBuffer();
			String note = ""; 
			String chord = ""; 
			String bar = ""; 
			String correct = ""; 
			String assigned = ""; 
//			String cnf = ""; 
			String category = ""; 
			String modelOutput = "";
			String[] lgd = detailsArr[0];
			for (int j = 0; j < lgd.length; j++) {
				String currLgd = lgd[j];
				String curr = s[j];
				if (currLgd.equals("note")) {
					note = curr; 
				}
				else if (currLgd.equals("chord")) {
					chord = curr;
					
				}
				else if (currLgd.equals("bar")) {
					bar = curr;
				}
				else if (currLgd.equals("correct")) {
					if (curr.contains("0 ")) {
						curr = curr.substring("0 ".length());
					}
					correct = "[".concat(curr);
					if (isTablatureCase) {
						String next = s[j+1];
						if (!next.equals("")) {
							correct = correct.concat(", ".concat(next));
						}
					}
					correct = correct.concat("]");
				}
				else if (currLgd.equals("assigned")) {
					if (curr.contains("0 ")) {
						curr = curr.substring("0 ".length());
					}
					assigned = "[".concat(curr);
					if (isTablatureCase) {
						String next = s[j+1];
						if (!next.equals("")) {
							assigned = assigned.concat(", ".concat(next));
						}
					}
					assigned = assigned.concat("]");
				}
				else if (currLgd.equals("reassigned")) {
					if (!curr.equals("")) {
						if (curr.contains("0 ")) {
							curr = curr.substring("0 ".length());
						}
						String cnf = "[".concat(curr);
						if (isTablatureCase) {
							String next = s[j+1];
							if (!next.equals("")) {
								cnf = cnf.concat(", ".concat(next));
							}
						}
						cnf = cnf.concat("]");
						assigned = assigned.concat(" : " + cnf);
					}
				}
				else if (currLgd.equals("category")) {
					category = curr;
				}
				else if (currLgd.equals("model output")) {
					// In the C2C case, curr only exists for the lowest chord note. In the N2N
					// case, curr always exists when not modelling duration, and only for the
					// voice line when modelling duration
					if (!curr.equals("")) {
						modelOutput = "[";
						for (int k = 0; k < labelSize; k++) {
//							modelOutput = modelOutput.concat(s[j+k].substring(1, 6));
							String o = s[j+k];
							if (o.length() >= 6) {
								modelOutput = modelOutput.concat(o.substring(1, 6));
							}
							else {
								if (ma != ModellingApproach.HMM) {
									if (o.equals("1")) {
										modelOutput = modelOutput.concat(o + ".   ");
									}
									else {
										modelOutput = modelOutput.concat(o.substring(1));
										for (int l = o.substring(1).length(); l < 5; l++) {
											modelOutput = modelOutput.concat(" ");
										}
									}
								}
								else {
									modelOutput = modelOutput.concat(o);
								}
							}
							if (k == labelSize-1) {
								modelOutput = modelOutput.concat("]");
							}
							else {
								modelOutput = modelOutput.concat(", ");
							}
						}
					}
				}
				else if (currLgd.equals("mapping index") && !curr.equals("")) {
					modelOutput = modelOutput.concat(
						" for mapping ").concat(String.valueOf(curr)).concat(": ");
				}
				else if (currLgd.equals("mapping") && !curr.equals("")) {
					modelOutput = modelOutput.concat("[");
					for (int k = 0; k < mappingSize; k++) {
						String mp = s[j+k];
						modelOutput = modelOutput.concat(String.valueOf(mp));
						if (k == mappingSize-1) {
							modelOutput = modelOutput.concat("]");
						}
						else {
							modelOutput = modelOutput.concat(", ");
						}
					}
				}
			}
			detailsLine.append(ToolBox.tabify(note, 1));
			detailsLine.append(ToolBox.tabify(chord, 1));
			detailsLine.append(ToolBox.tabify(bar, 2));
			detailsLine.append(ToolBox.tabify(correct, 1));
			detailsLine.append(ToolBox.tabify(modelOutput, numTabsForModelOutput));
			detailsLine.append(ToolBox.tabify(assigned, 2));
			detailsLine.append(ToolBox.tabify(category, 1));	
			details.append(detailsLine).append("\r\n");
		}
		details.append(line).append("\r\n");
		details.append(footnote);
		
		// For all notes: add information on conflict reassignments (only if there 
		// are conflicts to report)
		if (conflictRec != null && !conflictRec.isEmpty()) {
//		if (conflictIndices != null && !conflictRec.isEmpty()) {
			
			details.append("\r\n");
			details.append(conflictRec).append("\r\n");
//			String[] adaptations = new String[]{
//				"crr --> crr", "crr --> inc", 
//				"inc --> crr", "inc --> inc"};
//			for (int i = 0; i < conflictInfo.size(); i++) {
//				String add = "cnf ";
//				if (i == 1) {
//					add = "cnf* ";
//				}
//				List<List<Integer>> l = conflictInfo.get(i); 
//				for (int j = 0; j < l.size(); j++) {
//					List<Integer> curr = l.get(j);
//					String s = 
//						add.concat(adaptations[j]).concat(" (").concat(
//						String.valueOf(curr.size())).concat(")");
//					details.append(breakList(s, curr, 4, metricsUsed.size()));
//				}
//			}
		}
		return details.toString();
	}


	/**
	 * Returns a String containing (1) for each note or chord, the ground truth label(s) + the actual voice(s) and
	 * the network output + the predicted voice(s). In note-to-note application mode, to this are added (2) conflict
	 * reassignments; (3) conflicts; (4) application process; also, any adaptations made to predicted voices due to 
	 * conflicts are added in step (1).
	 *  
	 * @param modelParameters
	 * @param conflictIndices Application mode only; in bwd order when using bwd model
	 * @param allPredictedVoices Test and application mode only; in bwd order when using bwd model
	 * @param allPredictedDurationLabels Application mode only; in fwd order when using bwd model
	 * @param allMetricPositions Is <code>null</code> in training mode; in fwd order in test/application mode
	 * @param backwardsMapping Is <code>null</code> in training mode and when using fwd model in test/application mode
	 * @return
	 */
	public static String[][] getOutputDetails(
		List<List<List<Double>>> groundTruths,
		/*List<List<Integer[]>> additionalGroundTruths,*/
		List<List<Integer>>	allPredictedVoices, 
		List<List<Double>> allPredictedDurationLabels,
		List<Rational[]> allMetricPositions,
		List<double[]> argAllNetworkOutputs,
		List<List<double[]>> argAllMMOutputsPerModel,
		List<double[]> argAllCombinedOutputs,
		/*List<Integer> sliceIndices,*/
		List<List<Integer>> conflictIndices, 
		/*String conflictRec,*/ 
		/*List<List<List<Integer>>> conflictInfo,*/
		List<Integer> argChordSizes,
		List<Integer> backwardsMapping,
		List<List<List<Double>>> argGroundTruthChordVoiceLabels,
		List<List<List<Integer>>> argPossibleVoiceAssignmentsAllChords,
		List<List<Integer>> argAllBestVoiceAssignments, // is predictedMappings in HMM case
		List<Double> argAllHighestNetworkOutputs, // is predictedIndices in HMM case
		List<List<Integer>> assigErrs
		) { 

		Map<String, Double> modelParameters = Runner.getModelParams();
		boolean argModelDurationAgain = 
			ToolBox.toBoolean(modelParameters.get(Runner.MODEL_DURATION_AGAIN).intValue());
		boolean isTablatureCase = Runner.getDataset().isTablatureSet();
		Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()]; 
		DecisionContext dc = m.getDecisionContext(); 
		boolean argModelDuration = m.getModelDuration();
		ModelType mt = m.getModelType();
//		List<Integer> sliceIndices = 
//			ToolBox.decodeListOfIntegers(modelParameters.get(Runner.SLICE_IND_ENC_SINGLE_DIGIT).intValue(), 1);
		List<Integer> sliceIndices = null;
		if (mt == ModelType.MM || mt == ModelType.ENS) {
			sliceIndices = 
				ToolBox.decodeListOfIntegers(modelParameters.get(Runner.SLICE_IND_ENC_SINGLE_DIGIT).intValue(), 1);
		}

		ModellingApproach ma = 
			Runner.ALL_MODELLING_APPROACHES[modelParameters.get(Runner.MODELLING_APPROACH).intValue()];
		
		boolean modelDur = 
			(dc == DecisionContext.UNIDIR && argModelDuration 
			|| dc == DecisionContext.BIDIR && argModelDuration && argModelDurationAgain);
		
		List<Integer> inc = assigErrs.get(ErrorCalculator.INCORRECT_VOICE);
		List<Integer> incDur = new ArrayList<Integer>();
		List<Integer> overl = new ArrayList<Integer>();
		List<Integer> overlDur = new ArrayList<Integer>();
		List<Integer> superfl = new ArrayList<Integer>();
		List<Integer> half = new ArrayList<Integer>();
		if (isTablatureCase) {
			overl = assigErrs.get(ErrorCalculator.OVERLOOKED_VOICE);			
			superfl = assigErrs.get(ErrorCalculator.SUPERFLUOUS_VOICE);
			half = assigErrs.get(ErrorCalculator.HALF_VOICE);
			if (modelDur) {
				incDur = assigErrs.get(ErrorCalculator.INCORRECT_DUR);
				overlDur = assigErrs.get(ErrorCalculator.OVERLOOKED_DUR);
			}
		}

		List<List<Double>> argGroundTruthVoiceLabels = groundTruths.get(0);
		List<List<Double>> argGroundTruthDurationLabels = groundTruths.get(1);
//		List<Integer[]> argEqualDurationUnisonsInfo = additionalGroundTruths.get(0); // 08.2018
//		List<Integer[]> argVoicesCoDNotes = additionalGroundTruths.get(1); // 08.2018

		// Make a DecimalFormat for formatting long double values to four decimal places
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.UK);
		otherSymbols.setDecimalSeparator('.'); 
		// https://stackoverflow.com/questions/16098046/how-to-print-double-value-without-scientific-notation-using-java
		DecimalFormat dfOutp = new DecimalFormat("0", otherSymbols);
		dfOutp.setMaximumFractionDigits(340); //340 = DecimalFormat.DOUBLE_FRACTION_DIGITS
//		DecimalFormat decFormMMOutput = new DecimalFormat(".0000", otherSymbols);
//		decFormMMOutput.setRoundingMode(RoundingMode.DOWN);
//		DecimalFormat decFormLabel = new DecimalFormat("0.0   ", otherSymbols);
				
		// Determine number of rows and output size
		int rows = -1;
		int outputSize = -1;
		if (ma == ModellingApproach.N2N) {
			rows = argAllNetworkOutputs.size();
			if (isTablatureCase && modelDur) {
				rows *= 2; 
			}
			outputSize = argAllNetworkOutputs.get(0).length;
		}
		else if (ma == ModellingApproach.C2C || ma == ModellingApproach.HMM) {
			int numNotes = 0;
			for (List<List<Double>> l : argGroundTruthChordVoiceLabels) {
				numNotes += l.size();
			}
			rows = numNotes;
			outputSize = 1 + 1 + 4; // highest model output, mapping index, and mapping
		}
		
		List<String> rowHeader = new ArrayList<String>();
		rowHeader.add("note"); rowHeader.add("chord"); rowHeader.add("bar");
		rowHeader.add("correct"); rowHeader.add("assigned");  rowHeader.add("reassigned");  
		rowHeader.add("category"); rowHeader.add("model output");
		if (isTablatureCase) {
			rowHeader.add(4, ""); rowHeader.add(6, ""); rowHeader.add(8, "");
		}
		if (ma == ModellingApproach.N2N) {
			for (int i = 0; i < outputSize-1; i++) {
				rowHeader.add("");
			}
		}
		else if (ma == ModellingApproach.C2C || ma == ModellingApproach.HMM) {
			rowHeader.add("mapping index");
			rowHeader.add("mapping"); rowHeader.add(""); rowHeader.add(""); rowHeader.add("");
		}
		
		String[][] detailsArr = new String[rows+1][rowHeader.size()];
		for (int i = 0; i < rowHeader.size(); i++) {
			detailsArr[0][i] = rowHeader.get(i);
		}

		// Write out labels and network output details for each note
		if (ma == ModellingApproach.N2N) {
			boolean allowCoD = ToolBox.toBoolean(modelParameters.get(Runner.SNU).intValue());
			double deviationThreshold = -1.0;
			if (Runner.getDataset().isTablatureSet()) {
				deviationThreshold = modelParameters.get(Runner.DEV_THRESHOLD);
			}

			// For each note
			int chordIndex = 0; // TODO chordIndex, which depends on argChordSizes, is not correctly determined when using validation set
			int notesLeftInChord = 0; // = argChordSizes.get(chordIndex);
			
			boolean doThis = modelParameters.get(Runner.VALIDATION_PERC).intValue() == 0;
			if (doThis) { // TODO chordSizes is not correctly determined when using validation set 
				notesLeftInChord = argChordSizes.get(chordIndex);
			}

			int numNotes = argGroundTruthVoiceLabels.size();
			for (int i = 0; i < numNotes; i++) {
				// There are two indices: 
				// (1) i is used when the list the element is taken from is ordered in the same manner as noteFeatures (when 
				//     using the fwd model, this goes for all lists). The lists ordered in the same manner as noteFeatures
				//     are argGroundTruthVoiceLabels, argGroundTruthDurationLabels, argVoicesCoDNotes, and argEqualDurationUnisonsInfo
				// (2) noteIndex is used when the list the element is taken from is always ordered as when using the fwd model
				//     (when using the fwd model, noteIndex will thus always be the same as i)
				//     Lists indexed with noteIndex can only be used in test/application mode, as in training mode multiple 
				//     occurrences of noteIndex may occur
				int noteIndex = i;
				if (backwardsMapping != null) {
					noteIndex = backwardsMapping.get(i);
				}

				List<String> curr = new ArrayList<String>();
				// 1. Note, chord, bar
				curr.add(String.valueOf(i));
				curr.add(String.valueOf(chordIndex));
				if (allMetricPositions != null) {
					curr.add(ToolBox.getMetricPositionAsString(allMetricPositions.get(noteIndex)));
				}
				else {
					curr.add("");
				}
				
				// 2. Voice information
				StringBuffer voiceInfo = new StringBuffer();
				// a. Correct
				List<Double> actualVoiceLabel = argGroundTruthVoiceLabels.get(i); // HIER OK
				List<Integer> actualVoices = DataConverter.convertIntoListOfVoices(actualVoiceLabel);
				curr.add(String.valueOf(actualVoices.get(0)));
				if (actualVoices.size() == 2) {
					curr.add(String.valueOf(actualVoices.get(1)));
				}
				else {
					if (isTablatureCase) {
						curr.add("");
					}
				}

				// b. Predicted				
				// 1. (Original) network output
				double[] output = argAllNetworkOutputs.get(i); // HIER OK
				double[] outputVoice = 
					Arrays.copyOfRange(output, 0, Transcription.MAXIMUM_NUMBER_OF_VOICES);
				// Determine the predicted voices, i.e., the voice predicted pre-conflict (NN only)
				// or pre-combining (combined model). In both cases, they must thus NOT be retrieved
				// from allPredictedVoices.
				List<Integer> predictedVoices = 
					OutputEvaluator.interpretNetworkOutput(outputVoice, allowCoD, 
					deviationThreshold).get(0);

				curr.add(String.valueOf(predictedVoices.get(0)));
				if (predictedVoices.size() == 2) {
					curr.add(String.valueOf(predictedVoices.get(1)));
				}
				else {
					if (isTablatureCase) {
						curr.add("");
					}
				}
				
				// 2. Output(s) for MM models and combined output
				if (argAllCombinedOutputs != null) {
					// MM models
					for (int j = 0; j < sliceIndices.size(); j++) {
						String model = 
							MelodyPredictor.getSliceIndexString(sliceIndices.get(j));
						List<Integer> ns = 
							ToolBox.decodeListOfIntegers(
							modelParameters.get(Runner.NS_ENC_SINGLE_DIGIT).intValue(), 1);
						for (int nInd = 0; nInd < ns.size(); nInd++) {	
							int index = j*ns.size() + nInd;
							double[] currMMOutp = argAllMMOutputsPerModel.get(index).get(i);
							// NB: do not use outputEvaluator.interpretNetworkOutput() to determine 
							// predicted voices here, as MM outputs often contain the highest value 
							// more than twice
							List<Integer> currPredVoices = ToolBox.maxIndices(currMMOutp);
							StringBuffer outputType = new StringBuffer();					
							outputType.append(Runner.melodyModel.concat("-").concat(model).concat(
								" (n=" + ns.get(nInd) + ")").concat(" output"));
							voiceInfo.append(getDetailsLine(outputType.toString(), currMMOutp,
								currPredVoices,	null, null, null, dfOutp));
						}
					}
					// Combined output
					double[] currCombOutp = argAllCombinedOutputs.get(i); 
					// Determine the predicted voices, i.e., the voices predicted pre-combining. 
					// They must thus NOT be retrieved from allPredictedVoices
					List<Integer> currPredVoices = OutputEvaluator.interpretNetworkOutput(currCombOutp,
						allowCoD, deviationThreshold).get(0);
					voiceInfo.append(getDetailsLine("combined output", currCombOutp, 
						currPredVoices, null, null, null, dfOutp));
				}

				// c. Reassigned due to conflict
				if (conflictIndices != null && conflictIndices.get(0).contains(i)) { // HIER OK
					// Get the adapted voices, as contained in allPredictedVoices
					List<Integer> adaptedVoices = allPredictedVoices.get(i); // HIER OK
					// See http://stackoverflow.com/questions/5884353/insert-a-character-in-a-string-at-a-certain-position/13503345#13503345
					curr.add(String.valueOf(adaptedVoices.get(0)));
					if (adaptedVoices.size() == 2) {
						curr.add(String.valueOf(adaptedVoices.get(1)));
					}
					else {
						if (isTablatureCase) {
							curr.add("");
						}
					}
				}
				else {
					curr.add("");
					if (isTablatureCase) {
						curr.add("");
					}
				}
				
				// d. Misassignment category
				if (inc.contains(i)) {
					curr.add(Metric.INCORR.getStringRep());
				}
				else if (overl.contains(i)) {
					curr.add(Metric.OVERL.getStringRep());
				} 
				else if (superfl.contains(i)) {
					curr.add(Metric.SUPERFL.getStringRep());
				}
				else if (half.contains(i)) {
					curr.add(Metric.HALF.getStringRep());
				}
				else {
					curr.add("");
				}
				
				// e. Model output
				for (double d: output) {		
					curr.add(dfOutp.format(d));					
				}

				// 3. Duration information
				List<String> currDur = new ArrayList<String>();
				if (modelDur) {					
					currDur.add(""); currDur.add(""); currDur.add("");

					// a. Correct
					List<Double> actualDurLabel = argGroundTruthDurationLabels.get(i); // HIER OK
					List<Rational> actualDurations = 
						Arrays.asList(DataConverter.convertIntoDuration(actualDurLabel));
					// NB The prefix "0 " is needed to show the duration as a fraction in the .csv
					// file. Note: 0 1/1 is shown as 1, not as 1/1
					currDur.add("0 " + String.valueOf(actualDurations.get(0)));
					if (actualDurations.size() > 1) {
						currDur.add("0 " + String.valueOf(actualDurations.get(1)));
					}
					else {
						currDur.add("");
					}
					
					// b. Predicted
					List<Integer> predictedDurationsAsIndex = 
						OutputEvaluator.interpretNetworkOutput(output, allowCoD, 
						deviationThreshold).get(1);
					List<Double> predictedDurationsAsLabel = 
						DataConverter.convertIntoDurationLabel(predictedDurationsAsIndex);
					List<Rational> predictedDurations = 
						Arrays.asList(DataConverter.convertIntoDuration(predictedDurationsAsLabel));
					// NB: predictedDurations will have only one element as currently only one 
					// duration per note is predicted. Thus, the argument currentVoicesCoDNotes
					// can remain null TODO

					currDur.add("0 " + String.valueOf(predictedDurations.get(0)));
					if (predictedDurations.size() > 1) {
						currDur.add("0 " + String.valueOf(predictedDurations.get(1)));
					}
					else {
						currDur.add("");
					}
					
					// c. Reassigned due to conflict
					if (conflictIndices != null && conflictIndices.get(1).contains(i)) { // HIER OK		 	
						// Get the adapted durations, as encoded in allPredictedDurationLabels
						List<Double> predictedDurationLabelAdapted = 
							allPredictedDurationLabels.get(noteIndex); // HIER OK
						List<Rational> adaptedDurations = 
							Arrays.asList(DataConverter.convertIntoDuration(predictedDurationLabelAdapted));

						currDur.add("0 " + String.valueOf(adaptedDurations.get(0)));
						if (adaptedDurations.size() > 1) {
							currDur.add("0 " + String.valueOf(adaptedDurations.get(1)));
						}
						else {
							currDur.add("");
						}
					}
					else {
						currDur.add("");
						currDur.add("");
					}

					// d. Misassignment category
					if (incDur.contains(i)) {
						currDur.add(Metric.INCORR.getStringRep());
					}
					else if (overlDur.contains(i)) {
						currDur.add(Metric.OVERL.getStringRep());
					}
				}

				// Add information for the current note to detailsArr 
				if (!modelDur) {
					for (int j = 0; j < curr.size(); j++) {
						detailsArr[i+1][j] = curr.get(j);
					}
				}
				else {
					// Complete currDur to have the same size as curr
					int diff = curr.size() - currDur.size();
					for (int j = 0; j < diff; j++) {	
						currDur.add("");
					}
					for (int j = 0; j < curr.size(); j++) {
						detailsArr[(i*2)+1][j] = curr.get(j);
					}
					for (int j = 0; j < currDur.size(); j++) {
						detailsArr[((i*2)+1)+1][j] = currDur.get(j);
					}
				}

				if (doThis) {
					// Decrement notesLeftInChord for the next iteration of the for-loop:
					notesLeftInChord--;
					// Are there no more notes left in the chord? Increment chordIndex; reset notesLeftInchord
					if (notesLeftInChord == 0) {
						// Only if i is not the index of the last note
						if (i + 1 != numNotes) {
							chordIndex++;
							notesLeftInChord = argChordSizes.get(chordIndex);
						}
					}
				}
			}
		}
		else if (ma == ModellingApproach.C2C || ma == ModellingApproach.HMM) {
			// For each chord
			int lowestNoteIndex = 0;
			int numChords = argGroundTruthChordVoiceLabels.size(); // chordSizes does not exist in C2C
			for (int i = 0; i < numChords; i++) {				
				// Actual labels
				List<List<Double>> chordLabels = argGroundTruthChordVoiceLabels.get(i);
				// Predicted labels
				// HMM: bestVoiceAssignment = predictedMapping
				List<Integer> bestVoiceAssignment = argAllBestVoiceAssignments.get(i);
				int bestEval = -1;
				if (ma == ModellingApproach.C2C) {
					List<List<Integer>> possibleVoiceAssignmentsCurrentChord = 
						argPossibleVoiceAssignmentsAllChords.get(i);
					bestEval = 
						possibleVoiceAssignmentsCurrentChord.indexOf(bestVoiceAssignment);
				}
				List<List<Double>> bestChordLabels = 
					DataConverter.getChordVoiceLabels(bestVoiceAssignment);
				List<List<Integer>> predictedChordVoices = 
					DataConverter.getVoicesInChord(bestChordLabels);
				// HMM: highestNetworkOutput = predictedIndex
				double highestNetworkOutput = argAllHighestNetworkOutputs.get(i); // 1x
				
				for (int j = 0; j < chordLabels.size(); j++) {
					List<String> curr = new ArrayList<String>();
					// 1. Note, chord, bar
					int noteIndex = lowestNoteIndex + j;
					if (j == chordLabels.size()-1) {
						lowestNoteIndex += chordLabels.size();
					}
					curr.add(String.valueOf(noteIndex));
					curr.add(String.valueOf(i));
					if (allMetricPositions != null) {
						curr.add(ToolBox.getMetricPositionAsString(allMetricPositions.get(noteIndex)));
					}
					else {
						curr.add("");
					}
					
					// 2. Voice information
					// a. Correct
					List<Double> actualVoiceLabel = chordLabels.get(j);
					List<Integer> actualVoices = DataConverter.convertIntoListOfVoices(actualVoiceLabel);
					curr.add(String.valueOf(actualVoices.get(0)));
					if (actualVoices.size() == 2) {
						curr.add(String.valueOf(actualVoices.get(1)));
					}
					else {
						if (isTablatureCase) {
							curr.add("");
						}
					} 
					
					// b. Predicted
					List<Integer> predictedVoices = predictedChordVoices.get(j);
					curr.add(String.valueOf(predictedVoices.get(0)));
					if (predictedVoices.size() == 2) {
						curr.add(String.valueOf(predictedVoices.get(1)));
					}
					else {
						if (isTablatureCase) {
							curr.add("");
						}
					}
					
					// c. Reassigned due to conflict: does not apply
					curr.add("");
					if (isTablatureCase) {
						curr.add("");
					}
					
					// d. Misassignment category
					if (inc.contains(noteIndex)) {
						curr.add(Metric.INCORR.getStringRep());
					}
					else if (overl.contains(noteIndex)) {
						curr.add(Metric.OVERL.getStringRep());
					} 
					else if (superfl.contains(noteIndex)) {
						curr.add(Metric.SUPERFL.getStringRep());
					}
					else if (half.contains(noteIndex)) {
						curr.add(Metric.HALF.getStringRep());
					}
					else {
						curr.add("");
					}
					
					// e. Model output (highest network output, mapping index, mapping) 
					if (j == 0) {
						if (ma == ModellingApproach.C2C) {
							curr.add(dfOutp.format(highestNetworkOutput));
							curr.add(String.valueOf(bestEval));
						}
						// HMM case: the network output is the predicted mapping index
						else if (ma == ModellingApproach.HMM) {
							curr.add("" + (int)highestNetworkOutput);
							curr.add("" + (int)highestNetworkOutput);
						}
						for (int k : bestVoiceAssignment) {
							curr.add(String.valueOf(k));
						}
					}
					else {
						curr.add("");
						curr.add("");
						curr.add(""); curr.add(""); curr.add(""); curr.add("");
					}
					
					// Add information for the current note to detailsArr 
					for (int k = 0; k < curr.size(); k++) {
						detailsArr[noteIndex+1][k] = curr.get(k);
					}
				}
			}
		}
		return detailsArr;
	}


	/**
	 * Returns a String containing (1) for each note or chord, the ground truth label(s) + the actual voice(s) and
	 * the network output + the predicted voice(s). In note-to-note application mode, to this are added (2) conflict
	 * reassignments; (3) conflicts; (4) application process; also, any adaptations made to predicted voices due to 
	 * conflicts are added in step (1).
	 *  
	 * @param modelParameters
	 * @param conflictIndices Application mode only; in bwd order when using bwd model
	 * @param allPredictedVoices Test and application mode only; in bwd order when using bwd model
	 * @param allPredictedDurationLabels Application mode only; in fwd order when using bwd model
	 * @param allMetricPositions Is <code>null</code> in training mode; in fwd order in test/application mode
	 * @param backwardsMapping Is <code>null</code> in training mode and when using fwd model in test/application mode
	 * @return
	 */
	private static String getOutputDetailsOLD(
		List<List<List<Double>>> groundTruths,
		List<List<Integer[]>> additionalGroundTruths,
		List<List<Integer>>	allPredictedVoices, 
		List<List<Double>> allPredictedDurationLabels,
		List<Rational[]> allMetricPositions,
		List<double[]> argAllNetworkOutputs,
		List<List<double[]>> argAllMMOutputsPerModel,
		List<double[]> argAllCombinedOutputs,
		List<Integer> sliceIndices,
		List<List<Integer>> conflictIndices, 
		String conflictRec, 
		List<List<List<Integer>>> conflictInfo,
		List<Integer> argChordSizes,
		List<Integer> backwardsMapping,
		List<List<List<Double>>> argGroundTruthChordVoiceLabels,
		List<List<List<Integer>>> argPossibleVoiceAssignmentsAllChords,
		List<List<Integer>> argAllBestVoiceAssignments,
		List<Double> argAllHighestNetworkOutputs
		) { 

		List<List<Double>> argGroundTruthVoiceLabels = groundTruths.get(0);
		List<List<Double>> argGroundTruthDurationLabels = groundTruths.get(1);
		List<Integer[]> argEqualDurationUnisonsInfo = additionalGroundTruths.get(0);
		List<Integer[]> argVoicesCoDNotes = additionalGroundTruths.get(1);

		Map<String, Double> modelParameters = Runner.getModelParams();
		ModellingApproach ma = 
			Runner.ALL_MODELLING_APPROACHES[modelParameters.get(Runner.MODELLING_APPROACH).intValue()];

		// Make a DecimalFormat for formatting long double values to four decimal places
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.UK);
		otherSymbols.setDecimalSeparator('.'); 
		DecimalFormat decFormOutput = new DecimalFormat("0.0000", otherSymbols);
		decFormOutput.setRoundingMode(RoundingMode.DOWN);
		DecimalFormat decFormLabel = new DecimalFormat("0.0   ", otherSymbols);

		// Write out labels and network output details for each note
		StringBuffer details = new StringBuffer();
		details.append("D E T A I L S" + "\r\n");
		if (ma == ModellingApproach.N2N) {
			boolean allowCoD = 
				ToolBox.toBoolean(modelParameters.get(Runner.SNU).intValue());
			double deviationThreshold = modelParameters.get(Runner.DEV_THRESHOLD);

			Model m = Runner.ALL_MODELS[modelParameters.get(Runner.MODEL).intValue()]; 
			DecisionContext dc = m.getDecisionContext(); 
			boolean argModelDuration = m.getModelDuration();
			boolean argModelDurationAgain = 
				ToolBox.toBoolean(modelParameters.get(Runner.MODEL_DURATION_AGAIN).intValue());

			// For each note
			int chordIndex = 0;
			int notesLeftInChord = argChordSizes.get(chordIndex);
			int numNotes = argGroundTruthVoiceLabels.size(); 
			for (int i = 0; i < numNotes; i++) {
				// There are two indices: 
				// (1) i is used when the list the element is taken from is ordered in the same manner as noteFeatures (when 
				//     using the fwd model, this goes for all lists). The lists ordered in the same manner as noteFeatures
				//     are argGroundTruthVoiceLabels, argGroundTruthDurationLabels, argVoicesCoDNotes, and argEqualDurationUnisonsInfo
				// (2) noteIndex is used when the list the element is taken from is always ordered as when using the fwd model
				//     (when using the fwd model, noteIndex will thus always be the same as i)
				//     Lists indexed with noteIndex can only be used in test/application mode, as in training mode multiple 
				//     occurrences of noteIndex may occur
				int noteIndex = i;
				if (backwardsMapping != null) {
					noteIndex = backwardsMapping.get(i);
				}

				// 1. Note- and chord indices
				StringBuffer noteAndChordIndices = new StringBuffer();
				noteAndChordIndices.append("note ".concat(String.valueOf(i)).concat(
					" (chord ").concat(String.valueOf(chordIndex)).concat(")")); // HIER OK
				// Test and application mode: add metric position
				if (allMetricPositions != null) {
					noteAndChordIndices.append(", bar ".concat( 
						ToolBox.getMetricPositionAsString(allMetricPositions.get(noteIndex)))); // HIER OK
				}
				noteAndChordIndices.append("\r\n");

				// 2. Voice information
				StringBuffer voiceInfo = new StringBuffer();
				// Correct voice label
				List<Double> actualVoiceLabel = argGroundTruthVoiceLabels.get(i); // HIER OK
				double[] labelArray = ToolBox.listToArray(actualVoiceLabel);
				List<Integer> actualVoices = DataConverter.convertIntoListOfVoices(actualVoiceLabel);
				voiceInfo.append(getDetailsLine("voice label", labelArray, actualVoices, 
					null, null, null, decFormLabel));

				// Predicted voice label(s)				
				// a. (Original) network output
				double[] output = argAllNetworkOutputs.get(i); // HIER OK
				double[] outputVoice = 
					Arrays.copyOfRange(output, 0, Transcription.MAXIMUM_NUMBER_OF_VOICES);
				// Determine the predicted voices, i.e., the voice predicted pre-conflict (NN only)
				// or pre-combining (combined model). In both cases, they must thus NOT be retrieved
				// from allPredictedVoices.
				List<Integer> predictedVoices = 
					OutputEvaluator.interpretNetworkOutput(outputVoice, allowCoD, 
					deviationThreshold).get(0);
				voiceInfo.append(getDetailsLine("model output", outputVoice, predictedVoices,
					null, null,	null, decFormOutput));

				// b. Output(s) for MM models and combined output
				if (argAllCombinedOutputs != null) {
					// MM models
					for (int j = 0; j < sliceIndices.size(); j++) {
						String model = 
							MelodyPredictor.getSliceIndexString(sliceIndices.get(j));
						List<Integer> ns = null; // Runner.getNs();
						for (int nInd = 0; nInd < ns.size(); nInd++) {	
							int index = j*ns.size() + nInd;
							double[] currMMOutp = argAllMMOutputsPerModel.get(index).get(i);
							// NB: do not use outputEvaluator.interpretNetworkOutput() to determine 
							// predicted voices here, as MM outputs often contain the highest value 
							// more than twice
							List<Integer> currPredVoices = ToolBox.maxIndices(currMMOutp);
							StringBuffer outputType = new StringBuffer();					
							outputType.append(Runner.melodyModel.concat("-").concat(model).concat(
								" (n=" + ns.get(nInd) + ")").concat(" output"));
							voiceInfo.append(getDetailsLine(outputType.toString(), currMMOutp,
								currPredVoices,	null, null, null, decFormOutput));
						}
					}
					// Combined output
					double[] currCombOutp = argAllCombinedOutputs.get(i); 
					// Determine the predicted voices, i.e., the voices predicted pre-combining. 
					// They must thus NOT be retrieved from allPredictedVoices
					List<Integer> currPredVoices = OutputEvaluator.interpretNetworkOutput(currCombOutp,
						allowCoD, deviationThreshold).get(0);
					voiceInfo.append(getDetailsLine("combined output", currCombOutp, 
						currPredVoices, null, null, null, decFormOutput));
				}

				// In case of a conflict: add adaptation
				if (conflictIndices != null && conflictIndices.get(0).contains(i)) { // HIER OK
					// conflix
					// Get the adapted voices, as contained in allPredictedVoices
					List<Integer> adaptedVoices = allPredictedVoices.get(i); // HIER OK
					// See http://stackoverflow.com/questions/5884353/insert-a-character-in-a-string-at-a-certain-position/13503345#13503345
					voiceInfo.insert(voiceInfo.lastIndexOf("]") + 1, 
						new StringBuffer().append(" --> ".concat(adaptedVoices.toString())));
				}

				// 3. Duration information				
				StringBuffer durationInfo = new StringBuffer();
				if (dc == DecisionContext.UNIDIR && argModelDuration || dc == DecisionContext.BIDIR && argModelDuration && argModelDurationAgain) {
					// Correct duration label
					List<Double> actualDurLabel = argGroundTruthDurationLabels.get(i); // HIER OK
					double[] labelArrayDur = ToolBox.listToArray(actualDurLabel);
					List<Rational> actualDurations = 
						Arrays.asList(DataConverter.convertIntoDuration(actualDurLabel));
					Integer[] currentVoicesCoDNotes = argVoicesCoDNotes.get(i);
					durationInfo.append(getDetailsLine("duration label", labelArray, actualVoices, 
						labelArrayDur, actualDurations, currentVoicesCoDNotes, decFormLabel));

					// Predicted duration label
					double[] outputDur = Arrays.copyOfRange(output, 
						Transcription.MAXIMUM_NUMBER_OF_VOICES, output.length);
					String[] predictedDurationLabel = new String[outputDur.length]; 
					for (int j = 0; j < outputDur.length; j++) {
						String df = decFormOutput.format(outputDur[j]);
						predictedDurationLabel[j] = df;
					}

					List<Integer> predictedDurationsAsIndex = 
						OutputEvaluator.interpretNetworkOutput(output, allowCoD, 
						deviationThreshold).get(1);
					List<Double> predictedDurationsAsLabel = 
						DataConverter.convertIntoDurationLabel(predictedDurationsAsIndex);
					List<Rational> predictedDurations = 
						Arrays.asList(DataConverter.convertIntoDuration(predictedDurationsAsLabel));
					// NB: predictedDurations will have only one element as currently only one 
					// duration per note is predicted. Thus, the argument currentVoicesCoDNotes
					// can remain null TODO
					durationInfo.append(getDetailsLine("model output", outputVoice, predictedVoices,
						outputDur, predictedDurations, null, decFormOutput));

					// In case of a conflict: add adaptation
					if (conflictIndices != null && conflictIndices.get(1).contains(i)) { // HIER OK		 	
						// Get the adapted durations, as encoded in allPredictedDurationLabels
						List<Double> predictedDurationLabelAdapted = 
							allPredictedDurationLabels.get(noteIndex); // HIER OK
						List<Rational> adaptedDurations = 
							Arrays.asList(DataConverter.convertIntoDuration(predictedDurationLabelAdapted));
						String end = "]";
						if (predictedDurations.size() == 2) {
							end = "])";
						}
						durationInfo.insert(durationInfo.lastIndexOf(end) + 1, 
							new StringBuffer().append(" --> ".concat(adaptedDurations.toString())));
					}
				}

				// 4. Combine the above into the labelAndNetworkOutputDetails for the note at index i 
				details.append(noteAndChordIndices);
				details.append(voiceInfo);
				details.append(durationInfo);
				if (i != numNotes - 1) {
					details.append("\r\n");
				}

				// 5. For the next iteration of the for-loop:
				// Decrement notesLeftInChord 
				notesLeftInChord--;
				// Are there no more notes left in the chord? Increment chordIndex; reset notesLeftInchord
				if (notesLeftInChord == 0) {
					// Only if i is not the index of the last note
					if (i + 1 != numNotes) {
						chordIndex++;
						notesLeftInChord = argChordSizes.get(chordIndex);
					}
				} 	  
			}

			// For all notes: add information on conflict reassignments (only if there 
			// are conflicts to report)
			if (conflictIndices != null && !conflictRec.isEmpty()) {
				// conflix
				details.append("\r\n");
				details.append(conflictRec).append("\r\n");
				String[] adaptations = new String[]{
					"crr --> crr", "crr --> inc", 
					"inc --> crr", "inc --> inc"};
				for (int i = 0; i < conflictInfo.size(); i++) {
					String add = "cnf ";
					if (i == 1) {
						add = "cnf* ";
					}
					List<List<Integer>> l = conflictInfo.get(i); 
					for (int j = 0; j < l.size(); j++) {
						List<Integer> curr = l.get(j);
						String s = 
							add.concat(adaptations[j]).concat(" (").concat(
							String.valueOf(curr.size())).concat(")");
						details.append(breakList(s, curr, 4, metricsUsed.size()));
					}
				}
			}
		}
		else if (ma == ModellingApproach.C2C) {
			// For each chord
			int lowestNoteIndex = 0;
			int numChords = argGroundTruthChordVoiceLabels.size(); // (chordSizes does not exist in C2C)
			for (int i = 0; i < numChords; i++) {				
				List<List<Double>> chordLabels = argGroundTruthChordVoiceLabels.get(i);
				
				// 1. Chord- and note indices
				StringBuffer chordAndNoteIndices = new StringBuffer();
				int chordSize = chordLabels.size();
				String noteIndices = "";
				if (chordSize == 1) {
					noteIndices = 
						noteIndices.concat(" (note ".concat(String.valueOf(lowestNoteIndex)).
						concat(")")); 
				}
				else {
					noteIndices = 
						noteIndices.concat(" (notes ".concat(String.valueOf(lowestNoteIndex)).
						concat("-").concat(String.valueOf(lowestNoteIndex + chordSize - 1)).
						concat(")"));
				}
				chordAndNoteIndices.append("chord ".concat(String.valueOf(i)).concat(
					noteIndices));
				// Test and application mode: add metric position
				if (allMetricPositions != null) {
					chordAndNoteIndices.append(", bar ".concat( 
						ToolBox.getMetricPositionAsString(allMetricPositions.get(lowestNoteIndex))));
				}
				chordAndNoteIndices.append("\r\n");
				lowestNoteIndex += chordSize;

				// 2. Voice information
				StringBuffer voiceInfo = new StringBuffer();
				// Correct voice label(s)
				String tag = "voice label";
				if (chordSize > 1) {
					tag = tag.concat("s"); 
				}
				for (List<Double> actualVoiceLabel : chordLabels) {
					double[] labelArray = ToolBox.listToArray(actualVoiceLabel);
					List<Integer> actualVoices = 
						DataConverter.convertIntoListOfVoices(actualVoiceLabel);
					voiceInfo.append(getDetailsLine(tag, labelArray, actualVoices, 
						null, null, null, decFormLabel));
					if (!tag.isEmpty()) {
						tag = "";
					}
				}

				String one = "highest model output";
				voiceInfo.append(ToolBox.tabify(one, 4));

				// Predicted voice label(s)
				List<Integer> bestVoiceAssignment = argAllBestVoiceAssignments.get(i);
				List<List<Integer>> possibleVoiceAssignmentsCurrentChord = argPossibleVoiceAssignmentsAllChords.get(i);
				int bestEval = possibleVoiceAssignmentsCurrentChord.indexOf(bestVoiceAssignment);
				List<List<Double>> bestChordLabels = DataConverter.getChordVoiceLabels(bestVoiceAssignment);
				List<List<Integer>> predictedChordVoices = DataConverter.getVoicesInChord(bestChordLabels);
				double highestNetworkOutput = argAllHighestNetworkOutputs.get(i);

				String pv = "voice  ";
				if (predictedChordVoices.size() > 1 || (predictedChordVoices.size() == 1 &&
					predictedChordVoices.get(0).size() == 2)) {
					pv = "voices ";
				}					

				String two = "[".concat(decFormOutput.format(highestNetworkOutput)).concat(
					"] for mapping ").concat(String.valueOf(bestEval)).concat(": ").concat(
					String.valueOf(bestVoiceAssignment));
				voiceInfo.append(ToolBox.tabify(two, 6));

				String three = pv.concat(String.valueOf(predictedChordVoices)); 
				voiceInfo.append(ToolBox.tabify(three, 2)).append("\r\n");

				details.append(chordAndNoteIndices).append(voiceInfo);
				if (i != numChords - 1) {
					details.append("\r\n");
				}
			}
		}
		return details.toString();
	}


	/**
	 * Returns a details item.
	 * 
	 * @param labelOrOutput
	 * @param voices
	 * @param labelOrOutputDur
	 * @param durations
	 * @param voicesCoDNotes
	 * @param decForm
	 * @param outputType
	 * @return
	 */
	private static String getDetailsLine(String outputType, double[] labelOrOutput, 
		List<Integer> voices, double[] labelOrOutputDur, List<Rational> durations, 
		Integer[] voicesCoDNotes, DecimalFormat decForm) { 

		StringBuffer sb = new StringBuffer();

		// Voice case
		if (labelOrOutputDur == null) {	
			String[] formatted = new String[labelOrOutput.length]; 
			for (int i = 0; i < labelOrOutput.length; i++) {
				formatted[i] = decForm.format(labelOrOutput[i]); 
			}
			sb.append(ToolBox.tabify(outputType, 4));
			sb.append(ToolBox.tabify(Arrays.toString(formatted), 6));
			StringBuffer vv = new StringBuffer();
			if (voices.size() == 1) {
				vv.append("voice  ");
			}
			else {
				vv.append("voices ");
			}
			vv.append(voices);
			sb.append(ToolBox.tabify(vv.toString(), 2));
		}
		// Duration case
		else {
			String[] formatted = new String[labelOrOutputDur.length]; 
			for (int i = 0; i < labelOrOutputDur.length; i++) {
				formatted[i] = decForm.format(labelOrOutputDur[i]); 
			}
			
			String[][] broken = new String[8][4];
			int len = 4;
			for (int i = 0; i < 8; i++) {
				int start = len*i;
				broken[i] = Arrays.copyOfRange(formatted, start, start + len);
			}
			for (int i = 0; i < broken.length; i++) {
				String curr = Arrays.toString(broken[i]);
				if (i == 0) {
					sb.append(ToolBox.tabify(outputType, 4));
					curr = curr.replace("]", ",");
					sb.append(ToolBox.tabify(curr, 4).concat("\r\n"));
				}
				else {
					sb.append(ToolBox.tabify("", 4));
					curr = curr.replace("[", " ");
					if (i != broken.length - 1) {
						curr = curr.replace("]", ",");
						sb.append(ToolBox.tabify(curr, 4).concat("\r\n"));
					}
					else {
						sb.append(ToolBox.tabify(curr, 6));
					}
				}
			}
			StringBuffer dd = new StringBuffer();
			if (durations.size() == 1) {
				dd.append("duration  ");
			}
			else {
				dd.append("durations ");
			}
			
			// In the case of a CoD with one duration or no CoD: add the duration
			if ((voices.size() == 2 && durations.size() == 1) || voices.size() == 1) {
				dd.append(durations);
			}
			// In the case of a CoD with two durations: add the durations (in the correct
			// order) and the corresponding voices
			if (voices.size() == 2 && durations.size() == 2) { 						
				// voices is ordered with the higher voice first; durations with the longer 
				// duration first. Thus, if the higher voice has the longer duration: durations 
				// is in the right order; if not: its elements must be swapped
				// Get the voice of the shorter CoDnote. voicesCoDNotes is ordered with the voice
				// of the longer first
				int voiceOfShorter = voicesCoDNotes[1];
				if (voices.get(0) == voiceOfShorter) {
					Collections.reverse(durations);
				}
				dd.append(durations.toString().concat(" (for voices ").concat(voices.toString()).concat(
					")"));
			}
			sb.append(ToolBox.tabify(dd.toString(), 2));	
		}
		sb.append("\r\n");
		return sb.toString();
	}


	public static String getPerformanceSingleFoldMM(double[][] numEval, List<double[]> melodyModelOutputs, 
		List<List<Double>> voiceLabels) {

		StringBuffer result = new StringBuffer();
		result.append("RESULTS\r\n"); 		
		result.append(getResultsSingleFoldMM(numEval));
		result.append("DETAILS\r\n");
		for (int i = 0; i < voiceLabels.size(); i++) {
			List<Double> currVoiceLabel = voiceLabels.get(i);
			double[] currMMOutp = melodyModelOutputs.get(i);
			result.append("note = "); result.append(i);	result.append("\r\n");
			result.append("  label = "); result.append(currVoiceLabel);	
			result.append("\r\n");
			result.append("  MM output = "); result.append(Arrays.toString(currMMOutp));
			result.append("\r\n");

			Integer[] correctness = 
				assertCorrectnessMMOutput(currVoiceLabel, currMMOutp);	
			int corr = correctness[0];	
			if (corr == 2) {
				result.append("  --> FULLY CORRECT\r\n\r\n");
			}
			else if (corr == 1) {
				result.append("  --> PARTLY CORRECT (1/");
				result.append(correctness[1]); result.append(")\r\n\r\n");
			}
			else if (corr == 0) {
				result.append("  --> INCORRECT\r\n\r\n");
			}
		}
		return result.toString();
	}


	private static StringBuffer getResultsSingleFoldMM(double[][] eval) {
		StringBuffer result = new StringBuffer();
		// NB: In numEval, casting to int is safe for the elements at all indices but CROSS_ENT
		// (see JavaDoc for evaluateMelodyModel())
		int num = 0;
		int den = 1;
		int fullyCorr = (int) eval[num][FULLY];
		int partCorr = (int) eval[num][PARTLY];
		int incorr = (int) eval[num][INCORRECT];
		Rational wtdPartlyCorrect = 
			new Rational((int) eval[num][WTD_PARTLY], (int) eval[den][WTD_PARTLY]);
		Rational comb = 
			new Rational((int) eval[num][COMBINED], (int) eval[den][COMBINED]);
		int numNotes = fullyCorr + partCorr + incorr;
		double acc = (comb.div(numNotes).toDouble()) * 100;
		double sum = eval[num][CROSS_ENT];
		int j = (int) eval[den][CROSS_ENT];
		double H = (-1.0/j)*sum;
		result.append("notes                : "); result.append(numNotes); result.append("\r\n");
		result.append("  fully correct (f)  : "); result.append(fullyCorr); result.append("\r\n");
		result.append("  partly correct (p) : "); result.append(partCorr);	result.append("\r\n");
		result.append("    weighted (w)     : "); result.append(wtdPartlyCorrect); result.append("\r\n");
		result.append("  incorrect (i)      : "); result.append(incorr); result.append("\r\n");
		result.append("  acc ((f+w)/(f+p+i)): (("); result.append(comb); result.append(")/"); 
			result.append(numNotes); result.append(") * 100 = "); result.append(acc); result.append("\r\n");
		result.append("  cross-entropy      : "); result.append(H); result.append("\r\n\r\n");
		
		return result;
	}


	public static String[] getPerformanceAllFoldsMM(String path, int numFolds, List<Integer> sliceIndices) {
		String[] evalsPerModel = new String[sliceIndices.size()];
				
		// For each model
		for (int i = 0; i < sliceIndices.size(); i++) {
			StringBuffer res = new StringBuffer();
			String model = Runner.melodyModel + "-" +
				MelodyPredictor.getSliceIndexString(sliceIndices.get(i)) + "-";
						
			int tr = 0;
			int te = 1;
			int num = 0;
			int den = 1;
			// Get results, first for training (j == tr) and then for test (j == te) 
			for (int j = 0; j < 2; j++) { 				
				String evalStr = null;
				if (j == tr) {
					res.append("TRAINING\r\n");
					evalStr = Runner.evalMM + "-" + Runner.train + ".ser";
				}
				else if (j == te) {
					res.append("TEST\r\n");
					evalStr = Runner.evalMM + "-" + Runner.test + ".ser";
				}
				
				Rational sumOfComb = Rational.ZERO;
				int totalNotes = 0;
				double sumOfSums = 0;
				int sumOfJs = 0;
				// For each fold
				for (int k = 1; k <= numFolds; k++) {
					// Get eval and append to res
					String fold = "fold_" + ToolBox.zerofy(k, ToolBox.maxLen(k)) + "/";
					double[][] currEval = ToolBox.getStoredObjectBinary(new double[][]{}, 
						new File(path + fold + model + evalStr)); 
					res.append("fold " + (k) + "\r\n");
					res.append(getResultsSingleFoldMM(currEval));
					// Add to totals
					Rational currComb = 
						new Rational((int) currEval[num][COMBINED], (int) currEval[den][COMBINED]);
					sumOfComb = sumOfComb.add(currComb);
					int currTotal = (int) currEval[num][FULLY] + (int) currEval[num][PARTLY] +
						(int) currEval[num][INCORRECT];
					totalNotes += currTotal;
					sumOfSums += currEval[num][CROSS_ENT];
					sumOfJs += (int) currEval[den][CROSS_ENT];
				}
				Rational wtdAvg = sumOfComb.div(totalNotes);
				res.append("accuracy over all folds = " + wtdAvg + " * 100 = " +
					(wtdAvg.toDouble())*100 + "\r\n");
				double H = (-1.0/sumOfJs) * sumOfSums;
				res.append("cross-entropy over all folds = " + H);
				if (j == tr) {
					res.append("\r\n\r\n");
				}
			}
			evalsPerModel[i] = res.toString();
		}
		return evalsPerModel;	
	}


	/**
	 * Evaluates the melody model.
	 * 
	 * @param melodyModelOutputs
	 * @param voiceLabels
	 * @return A double[][] containing the numerators (at element 0) and the denominators
	 *         (at element 1) of </br> 
	 *         0: the fully correct outputs (num and den are ints; den is always 1.0) </br>
	 *         1: the partly correct outputs (num and den are ints; den is always 1.0) </br>
	 *         2: the incorrect outputs (num and den are ints; den is always 1.0) </br>
	 *         3: the weighted partly correct outputs (num and den are ints) </br>
	 *         4: the fully and weighted partly correct outputs combined (num and den are ints )</br>
	 *         5: the cross-entropy sum (num; double) and j (den; int) </br>
	 */
	// TESTED
	public static double[][] getMetricsSingleFoldMM(List<double[]> melodyModelOutputs, 
		List<List<Double>> voiceLabels) {

		int fullyCorr = 0;
		int partCorr = 0;
		Rational weightedPartlyCorrect = Rational.ZERO;
		int incorr = 0;
		for (int i = 0; i < melodyModelOutputs.size(); i++) {
			List<Double> currLabel = voiceLabels.get(i);
			double[] currMMo = melodyModelOutputs.get(i);
			Integer[] correctness = assertCorrectnessMMOutput(currLabel, currMMo);
			int corr = correctness[0];
			if (corr == 2) {
				fullyCorr++;
			}
			else if (corr == 1) {
				partCorr++;
				int freq = correctness[1];
				weightedPartlyCorrect = weightedPartlyCorrect.add(new Rational(1, freq));
			}
			else if (corr == 0) {
				incorr++;
			}
		}
		int num = 0;
		int den = 1;
		double[][] numbersCurrentFold = new double[2][6];
		numbersCurrentFold[num][FULLY] = fullyCorr; 
		numbersCurrentFold[den][FULLY] = 1;
		numbersCurrentFold[num][PARTLY] = partCorr;
		numbersCurrentFold[den][PARTLY] = 1;
		numbersCurrentFold[num][INCORRECT] = incorr;
		numbersCurrentFold[den][INCORRECT] = 1;
		numbersCurrentFold[num][WTD_PARTLY] = weightedPartlyCorrect.getNumer();
		numbersCurrentFold[den][WTD_PARTLY] = weightedPartlyCorrect.getDenom();
		Rational comb = new Rational(fullyCorr, 1).add(weightedPartlyCorrect);
		numbersCurrentFold[num][COMBINED] = comb.getNumer();
		numbersCurrentFold[den][COMBINED] = comb.getDenom();
		double[] H = ErrorCalculator.calculateCrossEntropy(melodyModelOutputs, voiceLabels);
		numbersCurrentFold[num][CROSS_ENT] = H[1]/H[2];
		numbersCurrentFold[den][CROSS_ENT] = H[3];
		
		return numbersCurrentFold;
	}


	/**
	 * Asserts the correctness of the melody model output given the label: </br>
	 * 0 = incorrect </br>
	 * 1 = partly correct </br>
	 * 2 = fully correct </br>
	 *  
	 * @param voiceLabel
	 * @param outputMM
	 * @return An Integer array of two elements: </br>
	 *         as element 0: 0, 1, or 2 </br>
	 *         as element 1: the frequency of the hightest value in the melody model output
	 *         (if element 0 contains 1) or <code>null</code> (if element 0 contains 0 or 2). 
	 */
	// TESTED
	static Integer[] assertCorrectnessMMOutput(List<Double> voiceLabel, double[] outputMM) {
		Integer[] res = new Integer[2];
		
		int currVoice = DataConverter.convertIntoListOfVoices(voiceLabel).get(0);
		List<Double> currMMoAsList = ToolBox.arrayToList(outputMM);
		double max = Collections.max(currMMoAsList);
		// If the correct voice has the highest network output: decide whether fully or partly
		if (currMMoAsList.get(currVoice) == max) {
			int freq = Collections.frequency(currMMoAsList, max);
			// Fully
			if (freq == 1) {
				res[0] = 2;
				res[1] = null;
			}
			// Partly
			else {
				res[0] = 1;
				res[1] = freq;
			}
		}
		// If not: incorrect
		else {
			res[0] = 0;
			res[1] = null;
		}	
		return res;
	}


	/**
	 * Returns, for all voice and duration (if applicable) conflicts, a list containing 
	 * <ul>
	 * 		<li>as element 0: a list of the indices of all notes initially assigned correctly and 
	 * 			reassigned correctly</li>
	 * 		<li>as element 1: a list of the indices of all notes initially assigned correctly and 
	 * 			reassignedincorrectly</li>
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
		double devThresh = modelParameters.get(Runner.DEV_THRESHOLD);

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
//				List<Integer> predictedVoices = predAdapActVoices.get(0);
//				List<Integer> adaptedVoices = predAdapActVoices.get(1);
//				List<Integer> actualVoices = predAdapActVoices.get(2);
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
//						boolean allowCoD = false;						
//						double devThresh = -1.0;
						int voicePredInitiallyUpperNote = 
							OutputEvaluator.interpretNetworkOutput(outputNextNote, allowCoD,
							devThresh).get(0).get(0);
						int adaptedVoiceLowerNote = adaptedVoices.get(0);
						int adaptedVoiceUpperNote = allPredictedVoices.get(i + 1).get(0); // HIER OK
						// Determine for both EDUnotes whether the predicted and adapted voices are correct
						List<Integer[]> predictedAndAdaptedVoices = new ArrayList<Integer[]>();
						predictedAndAdaptedVoices.add(new Integer[]{voicePredInitiallyLowerNote, voicePredInitiallyUpperNote});
						predictedAndAdaptedVoices.add(new Integer[]{adaptedVoiceLowerNote, adaptedVoiceUpperNote});
						List<Integer> allowedVoices = Arrays.asList(new Integer[]{argEqualDurationUnisonsInfo.get(i)[1], 
							argEqualDurationUnisonsInfo.get(i)[0]}); // HIER OK
						boolean[][] unisonNotesPredictedCorrectly = 
							ErrorCalculator.assertCorrectnessEDUNotes(predictedAndAdaptedVoices, 
							allowedVoices/*, -1*/); 
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
				// 1. In the tablature case, and in the non-tablature case for non EDUnotes and lower EDUnotes
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
				// 2. In the non-tablature case for upper EDUnotes 
				// NB: To avoid the index of the upper EDUnote being added twice (once when i is the index of the lower 
				// EDUnote and once that of the upper), it must only be added when i is the index of the lower EDUnote
				// TODO How is this implemented -- by means of the i + 1?
				if (argEqualDurationUnisonsInfo != null && argEqualDurationUnisonsInfo.get(i) != null) { // HIER OK
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
				// a. If currentActualDurations contains one element (i.e., if it goes with a note that is not a CoD, or with
				// a note that is a CoD whose notes have the same duration)
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
				// b. If currentActualDurations contains two elements (i.e., if it goes with a note that is CoD whose 
				// notes have different durations) 
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
//					indicesOfCorrToCorr.add(i); // HIER OK
					indicesOfCorrToCorrDur.add(i); 
				}
				// b. If the duration(s) were initially predicted (half) correctly but were adapted incorrectly 
				if (durationsPredictedInitiallyCorrectlyOrHalfCorrectly == true && 
					predictedDurationsAdaptedCorrectlyOrHalfCorrectly == false) {
//					indicesOfCorrToIncorr.add(i); // HIER OK
					indicesOfCorrToIncorrDur.add(i);
				}
				// c. If the duration(s) were initially predicted incorrectly but were adapted (half) correctly 
				if (durationsPredictedInitiallyCorrectlyOrHalfCorrectly == false && 
					predictedDurationsAdaptedCorrectlyOrHalfCorrectly == true) {
//					indicesOfIncorrToCorr.add(i); // HIER OK
					indicesOfIncorrToCorrDur.add(i);
				}
				// d. If the duration(s) were initially predicted incorrectly and were adapted incorrectly
				if (durationsPredictedInitiallyCorrectlyOrHalfCorrectly == false && 
					predictedDurationsAdaptedCorrectlyOrHalfCorrectly == false) {
//					indicesOfIncorrToIncorr.add(i); // HIER OK
					indicesOfIncorrToIncorrDur.add(i);
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


	private static List<List<Integer>> updateConflictIndices(List<List<Integer>> conflictIndicesLists, 
		List<List<Integer>> allPredictedVoices, List<Integer[]> argEqualDurationUnisonsInfo, 
		List<double[]> argAllNetworkOutputs, List<List<Integer>> predAdapActVoices, 
		List<Rational[]> predAdapActDur, int numNotes, int i) {

		List<Integer> indicesOfCorrToCorr = conflictIndicesLists.get(0);
		List<Integer> indicesOfCorrToIncorr = conflictIndicesLists.get(1);
		List<Integer> indicesOfIncorrToCorr = conflictIndicesLists.get(2);
		List<Integer> indicesOfIncorrToIncorr = conflictIndicesLists.get(3);

		// For voice conflicts
		if (predAdapActVoices != null) {
			List<Integer> predictedVoices = predAdapActVoices.get(0);
			List<Integer> adaptedVoices = predAdapActVoices.get(1);
			List<Integer> actualVoices = predAdapActVoices.get(2);

			// Keep track of the conflict reassignments
			boolean voicesPredictedInitiallyCorrectly = false;
			boolean predictedVoicesAdaptedCorrectly = false;
			boolean voicesPredictedInitiallyCorrectlyUpper = false;
			boolean predictedVoicesAdaptedCorrectlyUpper = false;

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
					boolean allowCoD = false;
					double deviationThreshold  =-1.0;
					int voicePredInitiallyUpperNote = 
						OutputEvaluator.interpretNetworkOutput(outputNextNote, allowCoD,
					  	deviationThreshold).get(0).get(0);
					int adaptedVoiceLowerNote = adaptedVoices.get(0);
					int adaptedVoiceUpperNote = allPredictedVoices.get(i + 1).get(0); // HIER OK
					// Determine for both EDUnotes whether the predicted and adapted voices are correct
					List<Integer[]> predictedAndAdaptedVoices = new ArrayList<Integer[]>();
					predictedAndAdaptedVoices.add(new Integer[]{voicePredInitiallyLowerNote, voicePredInitiallyUpperNote});
					predictedAndAdaptedVoices.add(new Integer[]{adaptedVoiceLowerNote, adaptedVoiceUpperNote});
					List<Integer> allowedVoices = Arrays.asList(new Integer[]{argEqualDurationUnisonsInfo.get(i)[1], 
						argEqualDurationUnisonsInfo.get(i)[0]}); // HIER OK
					boolean[][] unisonNotesPredictedCorrectly = 
						ErrorCalculator.assertCorrectnessEDUNotes(predictedAndAdaptedVoices, 
						allowedVoices/*, -1*/); 
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
			// 1. In the tablature case, and in the non-tablature case for non EDUnotes and lower EDUnotes
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
			// 2. In the non-tablature case for upper EDUnotes 
			// NB: To avoid the index of the upper EDUnote being added twice (once when i is the index of the lower 
			// EDUnote and once that of the upper), it must only be added when i is the index of the lower EDUnote
			// TODO How is this implemented -- by means of the i + 1?
			if (argEqualDurationUnisonsInfo != null && argEqualDurationUnisonsInfo.get(i) != null) { // HIER OK
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
		// For duration conflicts
		else if (predAdapActDur != null) {
			Rational[] predictedDurations = predAdapActDur.get(0);
			Rational[] adaptedDurations = predAdapActDur.get(1);
			Rational[] actualDurations = predAdapActDur.get(2);

			// Keep track of the conflict reassignments
			boolean durationsPredictedInitiallyCorrectlyOrHalfCorrectly = false;
			boolean predictedDurationsAdaptedCorrectlyOrHalfCorrectly = false;
			// a. If currentActualDurations contains one element (i.e., if it goes with a note that is not a CoD, or with
			// a note that is a CoD whose notes have the same duration)
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
			// b. If currentActualDurations contains two elements (i.e., if it goes with a note that is CoD whose 
			// notes have different durations) 
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
				indicesOfCorrToCorr.add(i); // HIER OK
			}
			// b. If the duration(s) were initially predicted (half) correctly but were adapted incorrectly 
			if (durationsPredictedInitiallyCorrectlyOrHalfCorrectly == true && 
				predictedDurationsAdaptedCorrectlyOrHalfCorrectly == false) {
				indicesOfCorrToIncorr.add(i); // HIER OK
			}
			// c. If the duration(s) were initially predicted incorrectly but were adapted (half) correctly 
			if (durationsPredictedInitiallyCorrectlyOrHalfCorrectly == false && 
				predictedDurationsAdaptedCorrectlyOrHalfCorrectly == true) {
				indicesOfIncorrToCorr.add(i); // HIER OK
			}
			// d. If the duration(s) were initially predicted incorrectly and were adapted incorrectly
			if (durationsPredictedInitiallyCorrectlyOrHalfCorrectly == false && 
				predictedDurationsAdaptedCorrectlyOrHalfCorrectly == false) {
				indicesOfIncorrToIncorr.add(i); // HIER OK
			}
		}
		return conflictIndicesLists;
	}

}

package machineLearning;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.Dataset;
import external.Transcription;
import tools.ToolBox;
import ui.Runner;
import ui.Runner.Model;
import ui.Runner.ModellingApproach;


public class EvaluationManagerTest {
	
	private static final List<Double> V_0 = Transcription.createVoiceLabel(new Integer[]{0});
	private static final List<Double> V_1 = Transcription.createVoiceLabel(new Integer[]{1});
	private static final List<Double> V_2 = Transcription.createVoiceLabel(new Integer[]{2});
	private static final List<Double> V_3 = Transcription.createVoiceLabel(new Integer[]{3});
	private static final List<Double> V_4 = Transcription.createVoiceLabel(new Integer[]{4});

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void testBreakList() {	
		List<Integer> list1 = Arrays.asList(new Integer[]{14, 15, 144, 146, 1028});
		List<Integer> list2 = Arrays.asList(new Integer[]{14, 15, 144, 145, 146, 1027, 1028});
		int numTabsIndent = 1;
		int numTabsTotal = 3;
		String expected1 = 
			ToolBox.tabify("O", 1, true) + "14, 15, 144, " + "\r\n" + 
			ToolBox.tabify("", 1, true) + "146, 1028" + "\r\n";
		String expected2 = 
			ToolBox.tabify("O", 1, true) + "14, 15, 144, " + "\r\n" + 
			ToolBox.tabify("", 1, true) + "145, 146, 1027, " + "\r\n" + 
			ToolBox.tabify("", 1, true) + "1028" + "\r\n";
		
		String actual1 = EvaluationManager.breakList("O", list1, numTabsIndent, numTabsTotal);
		String actual2 = EvaluationManager.breakList("O", list2, numTabsIndent, numTabsTotal);

		assertEquals(expected1, actual1);
		assertEquals(expected2, actual2);
	}


	@Test
	public void testGetAvgStDevTotals() {
//		List<EvaluationManager.Metric> altCSVTable = Arrays.asList(new EvaluationManager.Metric[]{
//			EvaluationManager.Metric.MODE, EvaluationManager.Metric.NTW_ERR,
//			EvaluationManager.Metric.ACC, EvaluationManager.Metric.NONE,
//			EvaluationManager.Metric.AVC, 
//			EvaluationManager.Metric.CRE, 
//			EvaluationManager.Metric.INCORR
//		});
		List<String> altCSVTable = Arrays.asList(new String[]{
			EvaluationManager.MODE, EvaluationManager.Metric.NTW_ERR.getStringRep(),
			EvaluationManager.Metric.ACC.getStringRep(), EvaluationManager.Metric.NONE.getStringRep(),
			EvaluationManager.Metric.AVC.getStringRep(), 
			EvaluationManager.Metric.CRE.getStringRep(), 
			EvaluationManager.Metric.INCORR.getStringRep(),
			EvaluationManager.Metric.RUNTIME.getStringRep()
		});
		EvaluationManager.setCsvLegend(altCSVTable);
//		Runner.setDataset(new Dataset(DatasetID.TESTPIECE_SET));
		Runner.setDataset(new Dataset(Dataset.BACH_WTC_4VV));
		
		// modelParameters
		Map<String, Double> modelParameters = new HashMap<String, Double>();
		modelParameters.put(Runner.MODEL, (double) Model.N.getIntRep());
		modelParameters.put(Runner.MODELLING_APPROACH, (double) ModellingApproach.N2N.getIntRep());
		Runner.setModelParams(modelParameters);
		
		String[][] csvTable = new String[16][EvaluationManager.csvLegend.size()];
		// element 0-2 contains only empty Strings
		csvTable[3] = new String[]{"trn", "", "", "", "", "", "", "11"};
		csvTable[4] = new String[]{"", "0.1234", "80", "160", "96.5", "2.0", "2", "11"};
		csvTable[5] = new String[]{"",    "0.2563", "20", "160", "97.0", "4.0", "2", "11"};
		csvTable[6] = new String[]{"",    "0.1525", "20", "160", "98.5", "8.0", "2", "11"};
		// element 7 contains only empty Strings
		csvTable[8] = new String[]{"tst", "", "", "", "", "", "", "22"};
		csvTable[9] = new String[]{"", "", "20", "160", "98.5", "2.0", "3", "22"};
		csvTable[10] = new String[]{"",    "", "80", "160", "96.5", "8.0", "3", "22"};
		csvTable[11] = new String[]{"",    "", "20", "160", "97.0", "4.0", "3", "22"};
		// element 12 contains only empty Strings
		csvTable[13] = new String[]{"app", "", "20", "160", "96.5", "8.0", "4", "33"};
		csvTable[14] = new String[]{"",   "", "20", "160", "97.0", "4.0", "4", "33"};
		csvTable[15] = new String[]{"",   "", "80", "160", "98.5", "2.0", "4", "33"};
		
		double avgNE = 0.1774; // (0.1234 + 0.2563 + 0.1525) / 3
		double stdevNE = ToolBox.stDev(Arrays.asList(new Double[]{0.1234, 0.2563, 0.1525}));
		double avgAcc = 25.0; // ( (80 + 20 + 20) / (160 + 160 + 160) ) * 100
		double stdevAcc = ToolBox.stDev(Arrays.asList(new Double[]{50.0, 12.5, 12.5}));
		double avgAVC = (96.5 * 160 + 97.0 * 160 + 98.5 * 160) / (160 + 160 + 160); 
		double stdevAVC = ToolBox.stDev(Arrays.asList(new Double[]{96.5, 97.0, 98.5}));
		double avgCE = (-1.0/(160 + 160 + 160)) * ((2.0+4.0+8.0) / Math.log(2));
		double CE1 = (-1.0/160) * (2.0 / Math.log(2));
		double CE2 = (-1.0/160) * (4.0 / Math.log(2));
		double CE3 = (-1.0/160) * (8.0 / Math.log(2));
		double stdevCE = ToolBox.stDev(Arrays.asList(new Double[]{CE1, CE2, CE3}));
		double sum1 = 44.0;
		double sum2 = 88.0;
		double sum3 = 99.0;

		List<double[][]> expected = new ArrayList<double[][]>();
		double[][] train = new double[3][8];
		train[0] = new double[]{0.0, avgNE, avgAcc, 0.0, avgAVC, avgCE, 0.0, 0.0};
		train[1] = new double[]{0.0, stdevNE, stdevAcc, 0.0, stdevAVC, stdevCE, 0.0, 0.0};
		train[2] = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, sum1};
		expected.add(train);
		double[][] test = new double[3][8];
		test[0] = new double[]{0.0, 0.0, avgAcc, 0.0, avgAVC, avgCE, 0.0, 0.0};
		test[1] = new double[]{0.0, 0.0, stdevAcc, 0.0, stdevAVC, stdevCE, 0.0, 0.0};
		test[2] = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, sum2};
		expected.add(test);
		double[][] appl = new double[3][8];
		appl[0] = new double[]{0.0, 0.0, avgAcc, 0.0, avgAVC, avgCE, 0.0, 0.0};
		appl[1] = new double[]{0.0, 0.0, stdevAcc, 0.0, stdevAVC, stdevCE, 0.0, 0.0};
		appl[2] = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, sum3};
		expected.add(appl);

		List<EvaluationManager.Metric> metrics = Arrays.asList(new EvaluationManager.Metric[]{
			/*EvaluationManager.Metric.MODE,*/ EvaluationManager.Metric.NTW_ERR,
			EvaluationManager.Metric.ACC, EvaluationManager.Metric.AVC,
			EvaluationManager.Metric.CRE,
			EvaluationManager.Metric.INCORR,
			EvaluationManager.Metric.RUNTIME
		});
		
		List<double[][]> actual = EvaluationManager.getAvgStDevTotals(metrics, csvTable, 3, false);
//		System.out.println(Arrays.toString(actual.get(0)[0]));
//		System.out.println(Arrays.toString(actual.get(0)[1]));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j].length, actual.get(i)[j].length);
				for (int k = 0; k < expected.get(i)[j].length; k++) {
					assertEquals(expected.get(i)[j][k], actual.get(i)[j][k], 10E-6);
				}
			}
		}
	}


	@Test
	public void testGetMetricsSingleFoldMM() {
		List<double[]> outputs = new ArrayList<double[]>();
		outputs.add(new double[]{0.1, 0.2, 0.3, 0.4, 0.3}); // correct
		outputs.add(new double[]{0.1, 0.4, 0.4, 0.4, 0.3}); // partly correct (1/3)
		outputs.add(new double[]{0.1, 0.2, 0.3, 0.1, 0.3}); // incorrect 
		outputs.add(new double[]{0.1, 0.2, 0.3, 0.4, 0.3}); // correct
		outputs.add(new double[]{0.1, 0.4, 0.4, 0.4, 0.3}); // partly correct (1/3)
		outputs.add(new double[]{0.1, 0.2, 0.3, 0.1, 0.3}); // incorrect 
		
		List<List<Double>> labels = new ArrayList<List<Double>>();
		labels.add(V_3); labels.add(V_3);
		labels.add(V_3); labels.add(V_3);
		labels.add(V_3); labels.add(V_3);
		
		double[][] expected = new double[2][6];
		double a = Math.log(0.4) / Math.log(2);
		double b = Math.log(0.1) / Math.log(2);
		double numH = 4*a + 2*b;
		double denH = 6;
		// 0 = fully; 1 = partly; 2 = incorr; 3 = wtd partly; 4 = combined; 5 = CE
//		double[] H = at.crossEntropy(outputs, labels);
		expected[0] = new double[]{2.0, 2.0, 2.0, 2.0, 8.0, numH};
		expected[1] = new double[]{1.0, 1.0, 1.0, 3.0, 3.0, denH};
		
		double[][] actual = EvaluationManager.getMetricsSingleFoldMM(outputs, labels);
		
		assertEquals(expected.length, actual.length);
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i].length, actual[i].length);
			for (int j = 0; j < expected[i].length; j++) {
				assertEquals(expected[i][j], actual[i][j], 10E-6);
			}
		}	
	}


	@Test
	public void testAssertCorrectnessMMOutput() {
		List<Double> voiceLabel = Arrays.asList(new Double[]{0.0, 0.0, 1.0, 0.0, 0.0});
		List<double[]> outputs = new ArrayList<double[]>();
		outputs.add(new double[]{0.1, 0.2, 0.3, 0.1, 0.2}); // FC
		outputs.add(new double[]{0.1, 0.3, 0.3, 0.1, 0.3}); // PC: 1/3
		outputs.add(new double[]{0.3, 0.2, 0.1, 0.1, 0.2}); // I
		outputs.add(new double[]{0.3, 0.3, 0.2, 0.1, 0.3}); // I
		
		List<Integer[]> expected = new ArrayList<Integer[]>();
		expected.add(new Integer[]{2, null});
		expected.add(new Integer[]{1, 3});
		expected.add(new Integer[]{0, null});
		expected.add(new Integer[]{0, null});
		
		List<Integer[]> actual = new ArrayList<Integer[]>();
		for (double[] outp : outputs) {
			actual.add(EvaluationManager.assertCorrectnessMMOutput(voiceLabel, outp));
		}
		
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j], actual.get(i)[j]);
			}
		}
	}

}

package featureExtraction;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import representations.Tablature;
import representations.Transcription;
import ui.Runner;
import ui.UI;
import de.uos.fmt.musitech.utility.math.Rational;
import featureExtraction.MelodyFeatureGenerator;

public class MelodyFeatureGeneratorTest extends TestCase {

	private File encodingTestpiece1;
	private File midiTestpiece1;
//	private MelodyFeatureGenerator mfg = new MelodyFeatureGenerator(new FeatureGenerator());
	private MelodyFeatureGenerator mfg = new MelodyFeatureGenerator();
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		Runner.setPathsToCodeAndData(UI.getRootPath(), false);
		midiTestpiece1 = new File(Runner.midiPath + "test/" + "testpiece.mid");
		encodingTestpiece1 = new File(Runner.encodingsPath + "test/" + "testpiece.tbp");
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}


	public void testGetMelodyModelFeatureVectors() {
		Tablature tablature = new Tablature(encodingTestpiece1, true);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

		List<List<List<Double>>> expected = new ArrayList<List<List<Double>>>();
		// metricTime == null
		List<List<Double>> expected0 = new ArrayList<List<Double>>();
		expected0.add(Arrays.asList(new Double[]{67.0, 1/4.0, 0.0, -1.0, 0.0, -1.0}));
		//
		expected0.add(Arrays.asList(new Double[]{70.0, 3/16.0, 3.0, 1/4.0, 0.0, -1.0}));
		expected0.add(Arrays.asList(new Double[]{63.0, 1/8.0, -7.0, 1/4.0, -4.0, 1/2.0}));
		expected0.add(Arrays.asList(new Double[]{67.0, 1/4.0, 4.0, 1/4.0, -3.0, 1/2.0}));
		expected0.add(Arrays.asList(new Double[]{62.0, 1/8.0, -5.0, 1/4.0, -1.0, 1/2.0}));
		expected0.add(Arrays.asList(new Double[]{66.0, 1/8.0, 4.0, 1/8.0, -1.0, 3/8.0}));
		//
		expected0.add(Arrays.asList(new Double[]{67.0, 1/16.0, 1.0, 1/8.0, 5.0, 1/4.0}));
		expected0.add(Arrays.asList(new Double[]{66.0, 1/16.0, -1.0, 1/16.0, 0.0, 3/16.0}));
		expected0.add(Arrays.asList(new Double[]{67.0, 1/32.0, 1.0, 1/16.0, 0.0, 1/8.0}));
		expected0.add(Arrays.asList(new Double[]{66.0, 1/32.0, -1.0, 1/32.0, 0.0, 3/32.0}));
		expected0.add(Arrays.asList(new Double[]{64.0, 1/32.0, -2.0, 1/32.0, -3.0, 1/16.0}));
		expected0.add(Arrays.asList(new Double[]{66.0, 1/32.0, 2.0, 1/32.0, 0.0, 1/16.0}));
		expected0.add(Arrays.asList(new Double[]{67.0, 1/4.0, 1.0, 1/32.0, 3.0, 1/16.0}));
		expected0.add(Arrays.asList(new Double[]{67.0, 1/4.0, 0.0, 1/2.0, 1.0, 17/32.0}));

		// metricTime == 17/8
		List<List<Double>> expected1 = new ArrayList<List<Double>>(expected0.subList(0, 8));

		expected.add(expected0); expected.add(expected1);

		List<List<List<Double>>> actual = new ArrayList<List<List<Double>>>();
		Integer[][] btp = tablature.getBasicTabSymbolProperties();
		List<List<Double>> actual0 = mfg.getMelodyModelFeatureVectors(btp, transcription, 0, 2, null);
		List<List<Double>> actual1 = mfg.getMelodyModelFeatureVectors(btp, transcription, 0, 2, new Rational(17, 8));
		actual.add(actual0); actual.add(actual1);

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j).size(), actual.get(i).get(j).size());
				for (int k = 0; k < expected.get(i).get(j).size(); k++) {
					assertEquals(expected.get(i).get(j).get(k), actual.get(i).get(j).get(k));
				}
			}
		}
	}


	public void testGetMelodyModelFeatureVectorsNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1);

		List<List<List<Double>>> expected = new ArrayList<List<List<Double>>>();
		// metricTime == null 
		List<List<Double>> expected0 = new ArrayList<List<Double>>();
		expected0.add(Arrays.asList(new Double[]{69.0, 1/4.0, 0.0, -1.0, 0.0, -1.0}));
		//
		expected0.add(Arrays.asList(new Double[]{72.0, 1/4.0, 3.0, 1/4.0, 0.0, -1.0}));
		expected0.add(Arrays.asList(new Double[]{65.0, 1/4.0, -7.0, 1/4.0, -4.0, 1/2.0}));
		expected0.add(Arrays.asList(new Double[]{69.0, 1/4.0, 4.0, 1/4.0, -3.0, 1/2.0}));
		expected0.add(Arrays.asList(new Double[]{64.0, 1/8.0, -5.0, 1/4.0, -1.0, 1/2.0}));
		expected0.add(Arrays.asList(new Double[]{68.0, 1/8.0, 4.0, 1/8.0, -1.0, 3/8.0}));
		//
		expected0.add(Arrays.asList(new Double[]{69.0, 1/16.0, 1.0, 1/8.0, 5.0, 1/4.0}));
		expected0.add(Arrays.asList(new Double[]{68.0, 1/16.0, -1.0, 1/16.0, 0.0, 3/16.0}));
		expected0.add(Arrays.asList(new Double[]{69.0, 1/32.0, 1.0, 1/16.0, 0.0, 1/8.0}));
		expected0.add(Arrays.asList(new Double[]{68.0, 1/32.0, -1.0, 1/32.0, 0.0, 3/32.0}));
		expected0.add(Arrays.asList(new Double[]{66.0, 1/32.0, -2.0, 1/32.0, -3.0, 1/16.0}));
		expected0.add(Arrays.asList(new Double[]{68.0, 1/32.0, 2.0, 1/32.0, 0.0, 1/16.0}));
		expected0.add(Arrays.asList(new Double[]{69.0, 1/4.0, 1.0, 1/32.0, 3.0, 1/16.0}));
		expected0.add(Arrays.asList(new Double[]{69.0, 1/4.0, 0.0, 1/2.0, 1.0, 17/32.0}));
		// metricTime == 17/8
		List<List<Double>> expected1 = new ArrayList<List<Double>>(expected0.subList(0, 8));
		expected.add(expected0); expected.add(expected1);

		List<List<List<Double>>> actual = new ArrayList<List<List<Double>>>();
		List<List<Double>> actual0 = mfg.getMelodyModelFeatureVectors(null, transcription, 0, 2, null);
		List<List<Double>> actual1 = mfg.getMelodyModelFeatureVectors(null, transcription, 0, 2, new Rational(17, 8));
		actual.add(actual0); actual.add(actual1);

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j).size(), actual.get(i).get(j).size());
				for (int k = 0; k < expected.get(i).get(j).size(); k++) {
					assertEquals(expected.get(i).get(j).get(k), actual.get(i).get(j).get(k));
				}
			}
		}
	}

}

package featureExtraction;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.uos.fmt.musitech.utility.math.Rational;
import external.Tablature;
import external.Transcription;
import interfaces.CLInterface;
import tools.text.StringTools;

public class MelodyFeatureGeneratorTest {

	private File encodingTestpiece1;
	private File midiTestpiece1;
//	private MelodyFeatureGenerator mfg = new MelodyFeatureGenerator(new FeatureGenerator());
	private MelodyFeatureGenerator mfg = new MelodyFeatureGenerator();
	
	@Before
	public void setUp() throws Exception {
		Map<String, String> paths = CLInterface.getPaths(true);
		midiTestpiece1 = new File(
			StringTools.getPathString(Arrays.asList(paths.get("MIDI_PATH"), "test")) + 
			"testpiece.mid"
		);
		encodingTestpiece1 = new File(
			StringTools.getPathString(Arrays.asList(paths.get("ENCODINGS_PATH"), "test")) + 
			"testpiece.tbp"
		);
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void testGetMelodyModelFeatureVectors() {
		Tablature tablature = new Tablature(encodingTestpiece1);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

		List<List<List<Double>>> expected = new ArrayList<List<List<Double>>>();
		// metricTime == null
		List<List<Double>> expected0 = new ArrayList<List<Double>>();
		expected0.add(Arrays.asList(new Double[]{69.0, 1/4.0, 0.0, -1.0, 0.0, -1.0}));
		//
		expected0.add(Arrays.asList(new Double[]{72.0, 3/16.0, 3.0, 1/4.0, 0.0, -1.0}));
		expected0.add(Arrays.asList(new Double[]{65.0, 1/8.0, -7.0, 1/4.0, -4.0, 1/2.0}));
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


	@Test
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

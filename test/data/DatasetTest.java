package data;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import tools.path.PathTools;
import ui.Runner.ModellingApproach;

public class DatasetTest {

	private String encodingsPath;
	private String midiPath;
	private Map<String, String> paths;
	
	@Before
	public void setUp() throws Exception {
		paths = PathTools.getPaths(true);
		encodingsPath = PathTools.getPathString(Arrays.asList(paths.get("ENCODINGS_PATH")));
		midiPath = PathTools.getPathString(Arrays.asList(paths.get("MIDI_PATH")));
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void testGetNumDataExamples() {
		List<Integer> expected = Arrays.asList(new Integer[]{3*39, 3*16, 3*40, 3*16});

		String[] argPaths = new String[]{
			encodingsPath + "/test/", 
			midiPath + "/test/", 
			midiPath + "/test/"
		};

		Dataset ds = new Dataset(Dataset.TEST_TAB);
		ds.populateDataset(null, paths, argPaths, false);
		Dataset dsNonTab = new Dataset(Dataset.TEST);
		dsNonTab.populateDataset(null, paths, argPaths, false);

		List<Integer> actual = new ArrayList<Integer>();
		actual.add(ds.getNumDataExamples(ModellingApproach.N2N));
		actual.add(ds.getNumDataExamples(ModellingApproach.C2C));
		actual.add(dsNonTab.getNumDataExamples(ModellingApproach.N2N));
		actual.add(dsNonTab.getNumDataExamples(ModellingApproach.C2C));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}


	@Test
	public void testGetIndividualPieceSizes() {		
		List<List<Integer>> expected = new ArrayList<List<Integer>>(); 
		// N2N
		expected.add(Arrays.asList(new Integer[]{39, 39, 39}));
		// C2C
		expected.add(Arrays.asList(new Integer[]{16, 16, 16}));
		// N2N non-tab
		expected.add(Arrays.asList(new Integer[]{40, 40, 40}));
		// C2C non-tab
		expected.add(Arrays.asList(new Integer[]{16, 16, 16}));

		String[] argPaths = new String[]{
			encodingsPath + "test/", 
			midiPath + "test/", 
			midiPath + "test/"
		};

		Dataset ds = new Dataset(Dataset.TEST_TAB);
		ds.populateDataset(null, paths, argPaths, false);
		Dataset dsNonTab = new Dataset(Dataset.TEST);
		dsNonTab.populateDataset(null, paths, argPaths, false);
		List<List<Integer>> actual = new ArrayList<List<Integer>>();
		actual.add(ds.getIndividualPieceSizes(ModellingApproach.N2N));
		actual.add(ds.getIndividualPieceSizes(ModellingApproach.C2C));
		actual.add(dsNonTab.getIndividualPieceSizes(ModellingApproach.N2N));
		actual.add(dsNonTab.getIndividualPieceSizes(ModellingApproach.C2C));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
	}


	@Test
	public void testGetBeginIndices() {
//		// disant (331): 0-330, t=331
//		// mess (706): (0+331)-(705+331), t=1037 
//		// pleni (334): (0+1037)-(333+1037); t=1371
//		// elslein (167): (0+1371)-(166+1371); t=1538
//		// nun (965): (0+1538)-(964+1538); t=2503 
//		// tant (246): (0+2503)-(245+2503); t=2749
//		List<Integer> expected = Arrays.asList(new Integer[]{0, 331, 1037, 1371, 1538, 2503});
//		
//		String[] dirs = new String[]{ExperimentRunner.rootDir, "n/a", "3vv/"};
//		ExperimentRunner e = new ExperimentRunner(dirs); // needed to set Paths: TODO
//		Dataset d = new Dataset(DatasetID.INTABULATIONS_3VV);
//		List<Integer> actual = d.getBeginIndices();
//		assertEquals(expected.size(), actual.size());
//		for (int i = 0; i < expected.size(); i++) {
//			assertEquals(expected.get(i), actual.get(i));		
//		}
//		assertEquals(expected, actual);	
	}

}

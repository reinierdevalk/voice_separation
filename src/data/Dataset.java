package data;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import representations.Tablature;
import representations.Transcription;
import tools.ToolBox;
import ui.Runner;
import ui.Runner.ModellingApproach;

public class Dataset implements Serializable {
	
	public static final List<String> INTAB_3VV_NAMES = Arrays.asList(new String[]{
		"newsidler-1536_7-disant_adiu",
		"newsidler-1536_7-mess_pensees",
		"pisador-1552_7-pleni_de",
		"judenkuenig-1523_2-elslein_liebes",
		"newsidler-1544_2-nun_volget",
		"phalese-1547_7-tant_que-3vv"
	});
			
	public static final List<String> INTAB_4VV_NAMES = Arrays.asList(new String[]{
		"ochsenkun-1558_5-absolon_fili",
		"ochsenkun-1558_5-in_exitu",
		"ochsenkun-1558_5-qui_habitat",
		"rotta-1546_15-bramo_morir",
		"phalese-1547_7-tant_que-4vv",
		"ochsenkun-1558_5-herr_gott",
		"abondante-1548_1-mais_mamignone",
		"phalese-1563_12-las_on",
		"barbetta-1582_1-il_nest",
//		"barbetta-1582_1-il_nest-corrected"
	});
	
	public static final List<String> JOSQ_4VV_NAMES = Arrays.asList(new String[]{
//		"ochsenkun-1558_5-absolon_fili",
//		"ochsenkun-1558_5-in_exitu",
//		"ochsenkun-1558_5-qui_habitat",
//		"rotta-1546_15-bramo_morir",
//		"phalese-1547_7-tant_que-4vv",
//		"ochsenkun-1558_5-herr_gott",
//		"abondante-1548_1-mais_mamignone",
//		"phalese-1563_12-las_on",
//		"barbetta-1582_1-il_nest",
		//	
//		"5265_14_absalon_fili_me_desprez-cropped",
//		"5263_12_in_exitu_israel_de_egipto_desprez-1-cropped",
//		"5263_12_in_exitu_israel_de_egipto_desprez-2-cropped",
////		"5263_12_in_exitu_israel_de_egipto_desprez-3", // m is too low (ternary section(s))
//		"4465_33-34_memor_esto-1-cropped",
////		"4465_33-34_memor_esto-2", // m is too low (ternary section(s))
//		"1274_12_qui_habitat_in_adjutorio-1-cropped",
//		"1274_12_qui_habitat_in_adjutorio-2-cropped",
//		"5264_13_qui_habitat_in_adjutorio_desprez-1-cropped",
//		"5264_13_qui_habitat_in_adjutorio_desprez-2-cropped",
//		//
//		"4471_40_cum_sancto_spiritu-cropped",
//		"5266_15_cum_sancto_spiritu_desprez-cropped",
//		"5106_10_misa_de_faysan_regres_2_gloria-cropped",
//		"5189_16_sanctus_and_hosanna_from_missa_faisant_regrets-1-cropped",
//		"5189_16_sanctus_and_hosanna_from_missa_faisant_regrets-2-cropped",		
//		"5188_15_sanctus_and_hosanna_from_missa_hercules-1-cropped",
//		"5188_15_sanctus_and_hosanna_from_missa_hercules-2-cropped",
//		"5190_17_cum_spiritu_sanctu_from_missa_sine_nomine-cropped",
//		//
//		"4400_45_ach_unfall_was-cropped",
//		"4481_49_ach_unfal_wes_zeigst_du_mich-cropped",
//		"4406_51_adieu_mes_amours-cropped",
////		"4467_37_adieu_mes_amours", // m is too low
//		"1025_adieu_mes_amours-cropped",
//		"1030_coment_peult_avoir_joye-cropped",		
//		"5191_18_mille_regres-cropped",
//		"4482_50_mille_regrets_P-cropped",
//		"4469_39_plus_nulz_regrets_P-cropped", // Chord at bar 57 contains three C4 (MIDIpitch 60): removed from Altus
//		"922_milano_098_que_voulez_vous_dire_de_moi-cropped"

		//
		"5265_14_absalon_fili_me_desprez",
		"5263_12_in_exitu_israel_de_egipto_desprez-1",
		"5263_12_in_exitu_israel_de_egipto_desprez-2",
		"5263_12_in_exitu_israel_de_egipto_desprez-3", // m is too low (ternary section(s))
		"4465_33-34_memor_esto-1",
		"4465_33-34_memor_esto-2", // m is too low (ternary section(s))
		"1274_12_qui_habitat_in_adjutorio-1",
		"1274_12_qui_habitat_in_adjutorio-2",
		"5264_13_qui_habitat_in_adjutorio_desprez-1",
		"5264_13_qui_habitat_in_adjutorio_desprez-2",
		//
		"4471_40_cum_sancto_spiritu",
		"5266_15_cum_sancto_spiritu_desprez",
		"5106_10_misa_de_faysan_regres_2_gloria",
		"5189_16_sanctus_and_hosanna_from_missa_faisant_regrets-1",
		"5189_16_sanctus_and_hosanna_from_missa_faisant_regrets-2",		
		"5188_15_sanctus_and_hosanna_from_missa_hercules-1",
		"5188_15_sanctus_and_hosanna_from_missa_hercules-2",
		"5190_17_cum_spiritu_sanctu_from_missa_sine_nomine",
		//
		"4400_45_ach_unfall_was",
		"4481_49_ach_unfal_wes_zeigst_du_mich",
		"4406_51_adieu_mes_amours",
		"4467_37_adieu_mes_amours", // m is too low
		"1025_adieu_mes_amours",
		"1030_coment_peult_avoir_joye",		
		"5191_18_mille_regres",
		"4482_50_mille_regrets_P",
		"4469_39_plus_nulz_regrets_P", // Chord at bar 57 contains three C4 (MIDIpitch 60): removed from Altus
		"922_milano_098_que_voulez_vous_dire_de_moi"
	});
	
	public static final List<String> BYRD_4VV_NAMES = Arrays.asList(new String[]{
		"ah_golden_hairs-NEW",
		"an_aged_dame-II", //
		"as_caesar_wept-II",
		"blame_i_confess-II", //
		"in_angels_weed-II",
		"o_lord_bow_down-II", //
		"o_that_we_woeful_wretches-NEW", //
		"quis_me_statim-II", //
		"rejoyce_unto_the_lord-NEW", // 
		"sith_death-NEW", //
		"the_lord_is_only_my_support-NEW", //
		"the_man_is_blest-NEW", //
		"while_phoebus-II" //
		
//		"ah_golden_hairs-manual",
//		"an_aged_dame-manual",
//		"as_caesar_wept-manual",
//		"blame_i_confess-manual",
//		"in_angels_weed-manual",
//		"o_lord_bow_down-manual",
//		"o_that_we_woeful_wretches-manual",
//		"quis_me_statim-manual",
//		"rejoyce_unto_the_lord-manual",
//		"sith_death-manual",
//		"the_lord_is_only_my_support-manual",
//		"the_man_is_blest-manual",
//		"while_phoebus-manual"
	});
	
			
	private static final List<String> INTAB_5VV_NAMES = Arrays.asList(new String[]{
		"adriansen-1584_6-d_vn_si",
		"ochsenkun-1558_5-inuiolata_integra"
	});
		
	public static final List<String> FUGUES_2VV_NAMES = Arrays.asList(new String[]{
//		"bach-WTC1-fuga_10-BWV_855",
		"bach-WTC1-fuga_10-BWV_855_1",
		"bach-WTC1-fuga_10-BWV_855_2"	
	});
	
	public static final List<String> FUGUES_3VV_NAMES = Arrays.asList(new String[]{
		"bach-WTC1-fuga_2-BWV_847",
		"bach-WTC1-fuga_3-BWV_848",
		"bach-WTC1-fuga_6-BWV_851",
		"bach-WTC1-fuga_7-BWV_852",
		"bach-WTC1-fuga_8-BWV_853",
		"bach-WTC1-fuga_9-BWV_854",
		"bach-WTC1-fuga_11-BWV_856",
		"bach-WTC1-fuga_13-BWV_858",
		"bach-WTC1-fuga_15-BWV_860",
		"bach-WTC1-fuga_19-BWV_864",
		"bach-WTC1-fuga_21-BWV_866",
		"bach-WTC2-fuga_1-BWV_870",
		"bach-WTC2-fuga_3-BWV_872",
		"bach-WTC2-fuga_4-BWV_873",
		"bach-WTC2-fuga_6-BWV_875",
		"bach-WTC2-fuga_10-BWV_879",
		"bach-WTC2-fuga_11-BWV_880",
		"bach-WTC2-fuga_12-BWV_881",
		"bach-WTC2-fuga_13-BWV_882",
		"bach-WTC2-fuga_14-BWV_883",
		"bach-WTC2-fuga_15-BWV_884",
		"bach-WTC2-fuga_18-BWV_887",
		"bach-WTC2-fuga_19-BWV_888",
		"bach-WTC2-fuga_20-BWV_889",
		"bach-WTC2-fuga_21-BWV_890",
		"bach-WTC2-fuga_24-BWV_893"
	});
		
	public static final List<String> FUGUES_4VV_NAMES = Arrays.asList(new String[]{
		"bach-WTC1-fuga_1-BWV_846",
		"bach-WTC1-fuga_5-BWV_850",
		"bach-WTC1-fuga_12-BWV_857",
		"bach-WTC1-fuga_14-BWV_859",
		"bach-WTC1-fuga_16-BWV_861",
		"bach-WTC1-fuga_17-BWV_862",
		"bach-WTC1-fuga_18-BWV_863",
		"bach-WTC1-fuga_20-BWV_865",
		"bach-WTC1-fuga_23-BWV_868",
		"bach-WTC1-fuga_24-BWV_869",
		"bach-WTC2-fuga_2-BWV_871",
		"bach-WTC2-fuga_5-BWV_874",
		"bach-WTC2-fuga_7-BWV_876",
		"bach-WTC2-fuga_8-BWV_877",
		"bach-WTC2-fuga_9-BWV_878",
		"bach-WTC2-fuga_16-BWV_885",
		"bach-WTC2-fuga_17-BWV_886",
		"bach-WTC2-fuga_22-BWV_891",
		"bach-WTC2-fuga_23-BWV_892"
	});
	
	public static final List<String> FUGUES_5VV_NAMES = Arrays.asList(new String[]{
//		"bach-WTC1-fuga_4-BWV_849",
//		"bach-WTC1-fuga_22-BWV_867",
			
		// Thesis: 1-57, 58-115
//		"bach-WTC1-fuga_4-BWV_849_1",
//		"bach-WTC1-fuga_4-BWV_849_2",
		// Thesis: 1-37, 38-75 
//		"bach-WTC1-fuga_22-BWV_867_1",
//		"bach-WTC1-fuga_22-BWV_867_2",

		// ISMIR 2018
		// initial split in four equal parts: 0-330, 331-660 (from b. 41 3/4), 661-991 
		// (from b. 65 1/2), 992-1321 (from b. 88 3/4); final split at closest points
		// where theme or motif enters in 5vv texture: from bb. 44, 65, 86 
		"bach-WTC1-fuga_4-BWV_849-split_at_44-65-86_1", 
		"bach-WTC1-fuga_4-BWV_849-split_at_44-65-86_2",
		"bach-WTC1-fuga_4-BWV_849-split_at_44-65-86_3",
		"bach-WTC1-fuga_4-BWV_849-split_at_44-65-86_4",
		// initial split in two equal parts: 0-376, 377-753 (from b. 42), final split at 
		// closest points where theme or motif enters in 5vv texture: b. 37 
		"bach-WTC1-fuga_22-BWV_867-split_at_37_1",
		"bach-WTC1-fuga_22-BWV_867-split_at_37_2",
		// initial split in three equal parts: 0-251, 252-502 (from b. 28 3/4), 503-753 
		// (from b. 52 1/2); final split at closest points where theme or motif enters 
		// in 5vv texture: b. 32, 53 
//		"bach-WTC1-fuga_22-BWV_867-split_at_32-53_1",
//		"bach-WTC1-fuga_22-BWV_867-split_at_32-53_2",
//		"bach-WTC1-fuga_22-BWV_867-split_at_32-53_3",		
		
//		"bach-WTC1-fuga_4-BWV_849_1-split_at_65", // 2 same-size parts, beginning of bar 
//		"bach-WTC1-fuga_4-BWV_849_2-split_at_65", // 2 same-size parts, beginning of bar 
//		"bach-WTC1-fuga_4-BWV_849-3s_1",
//		"bach-WTC1-fuga_4-BWV_849-3s_2",	
//		"bach-WTC1-fuga_4-BWV_849-3s_3",
//		"bach-WTC1-fuga_4-BWV_849-3s_1-exact",
//		"bach-WTC1-fuga_4-BWV_849-3s_2-exact",	
//		"bach-WTC1-fuga_4-BWV_849-3s_3-exact",
//		"bach-WTC1-fuga_4-BWV_849_1-split_at_41-65-88", // same size parts, beginning of bar
//		"bach-WTC1-fuga_4-BWV_849_2-split_at_41-65-88", // same size parts, beginning of bar 
//		"bach-WTC1-fuga_4-BWV_849_3-split_at_41-65-88", // same size parts, beginning of bar
//		"bach-WTC1-fuga_4-BWV_849_4-split_at_41-65-88", // same size parts, beginning of bar
//		"bach-WTC1-fuga_22-BWV_867_1-exact",
//		"bach-WTC1-fuga_22-BWV_867_2-exact"
//		"bach-WTC1-fuga_22-BWV_867_1-split_at_42", // 2 same-size parts, beginning of bar 
//		"bach-WTC1-fuga_22-BWV_867_2-split_at_42", // 2 same-size parts, beginning of bar
//		"bach-WTC1-fuga_22-BWV_867_1-split_at_28-52", // 3 same-size parts, beginning of bar
//		"bach-WTC1-fuga_22-BWV_867_2-split_at_28-52", // 3 same-size parts, beginning of bar
//		"bach-WTC1-fuga_22-BWV_867_3-split_at_28-52", // 3 same-size parts, beginning of bar
	});
	
	public static final List<String> INV_2VV_NAMES = Arrays.asList(new String[]{
		"bach-inv-inventio_1-BWV_772",
		"bach-inv-inventio_2-BWV_773",
		"bach-inv-inventio_3-BWV_774",
		"bach-inv-inventio_4-BWV_775",
		"bach-inv-inventio_5-BWV_776",
		"bach-inv-inventio_6-BWV_777",
		"bach-inv-inventio_7-BWV_778",
		"bach-inv-inventio_8-BWV_779",
		"bach-inv-inventio_9-BWV_780",
		"bach-inv-inventio_10-BWV_781",
		"bach-inv-inventio_11-BWV_782",
		"bach-inv-inventio_12-BWV_783",
		"bach-inv-inventio_13-BWV_784",
		"bach-inv-inventio_14-BWV_785",
		"bach-inv-inventio_15-BWV_786"
	});
	
	public static final List<String> INV_3VV_NAMES = Arrays.asList(new String[]{
		"bach-inv-inventio_1-BWV_787",
		"bach-inv-inventio_2-BWV_788",
		"bach-inv-inventio_3-BWV_789",
		"bach-inv-inventio_4-BWV_790",
		"bach-inv-inventio_5-BWV_791",
		"bach-inv-inventio_6-BWV_792",
		"bach-inv-inventio_7-BWV_793",
		"bach-inv-inventio_8-BWV_794",
		"bach-inv-inventio_9-BWV_795",
		"bach-inv-inventio_10-BWV_796",
		"bach-inv-inventio_11-BWV_797",
		"bach-inv-inventio_12-BWV_798",
		"bach-inv-inventio_13-BWV_799",
		"bach-inv-inventio_14-BWV_800",
		"bach-inv-inventio_15-BWV_801"	
	});
	
	public static final List<String> TESTPIECE_SET_NAMES = Arrays.asList(new String[]{
		"testpiece", "testpiece", "testpiece"	
	});
	
	private static final Map<DatasetID, List<String>> pieceNamesMap;
	static {
		pieceNamesMap = new LinkedHashMap<DatasetID, List<String>>();
		pieceNamesMap.put(DatasetID.INT_3vv, INTAB_3VV_NAMES);
		pieceNamesMap.put(DatasetID.INT_4vv, INTAB_4VV_NAMES);
		pieceNamesMap.put(DatasetID.INT_5vv, INTAB_5VV_NAMES);
		pieceNamesMap.put(DatasetID.WTC_4vv, FUGUES_4VV_NAMES);
	}

	public static final String DATASET_ID = "dataset ID";	
//	public static DatasetID[] ALL_DATASET_IDS = new DatasetID[3*Runner.ARR_SIZE];
	public enum DatasetID {
		INT_3vv("tab-int-3vv", 0, true, false, 3, "tab-int", INTAB_3VV_NAMES),
		INT_4vv("tab-int-4vv", 1, true, false, 4, "tab-int", INTAB_4VV_NAMES),  
		INT_5vv("tab-int-5vv", 2, true, false, 5, "tab-int", INTAB_5VV_NAMES), 
		
		tINT_3vv("tab-int_t-3vv", 3, false, true, 3, "tab-int", INTAB_3VV_NAMES),
		tINT_4vv("tab-int_t-4vv", 4, false, true, 4, "tab-int", INTAB_4VV_NAMES),
		tINT_5vv("tab-int_t-5vv", 5, false, true, 5, "tab-int", INTAB_5VV_NAMES),
		
		WTC_2vv("bach-WTC-2vv", 6, false, false, 2, "bach-WTC", FUGUES_2VV_NAMES), 
		WTC_3vv("bach-WTC-3vv", 7, false, false, 3, "bach-WTC", FUGUES_3VV_NAMES), 
		WTC_4vv("bach-WTC-4vv", 8, false, false, 4, "bach-WTC", FUGUES_4VV_NAMES), 
		WTC_5vv("bach-WTC-5vv", 9, false, false, 5, "bach-WTC", FUGUES_5VV_NAMES), 
		INV_2vv("bach-inv-2vv", 10, false, false, 2, "bach-inv", INV_2VV_NAMES), 
		INV_3vv("bach-inv-3vv", 11, false, false, 3, "bach-inv", INV_3VV_NAMES),
		
		IMI("tab-int-imi-4vv", 12, true, false, 4, "tab-int", null), 
		IMI_SHORTER("tab-int-imi_s-4vv", 13, true, false, 4, "tab-int", null), 
		SEMI("tab-int-semi-4vv", 14, true, false, 4, "tab-int", null), 
		FREE("tab-int-free-4vv", 15, true, false, 4, "tab-int", null), 
		FREE_MORE("tab-int-free_m-4vv", 16, true, false, 4, "tab-int", null), 
		
		IMI_NON_TAB("tab-int-imi_t-4vv", 17, false, true, 4, "tab-int", null), 
		IMI_SHORTER_NON_TAB("tab-int-imi_s_t-4vv", 18, false, true, 4, "tab-int", null),
		SEMI_NON_TAB("tab-int-semi_t-4vv", 19, false, true, 4, "tab-int", null),
		FREE_NON_TAB("tab-int-free_t-4vv", 20, false, true, 4, "tab-int", null),
		FREE_MORE_NON_TAB("tab-int-free_m_t-4vv", 21, false, true, 4, "tab-int", null),
		
		TESTPIECE_SET("tab-test", 22, true, false, 5, "test", TESTPIECE_SET_NAMES),
		TESTPIECE_SET_NON_TAB("test_t", 23, false, false, 5, "test", TESTPIECE_SET_NAMES),
		
		USER_TAB("user", 24, true, false, 0, "user_def_name", null),
		USER("user_t", 25, false, false, 0, "user_def_name", null),
		
		JOSQ_4vv("josq-4vv", 26, true, false, 4, "josq-int", JOSQ_4VV_NAMES),
		
		BYRD_4vv("byrd-int-4vv", 27, true, false, 4, "byrd-int", BYRD_4VV_NAMES),
		tBYRD_4vv("byrd-int_t-4vv", 28, false, true, 4, "byrd-int", BYRD_4VV_NAMES);
		
		
		private int intRep;
		private String stringRep;
		private boolean isTablatureSet;
		private boolean isTabAsNonTabSet;
		private int numVoices ;
		private String name;
		private List<String> pieceNames;
		DatasetID(String s, int i, boolean isTab, boolean isTabAsNonTab, int vv, String name, 
			List<String> argPieceNames) {
			this.stringRep = s;
			this.intRep = i;
			this.isTablatureSet = isTab;
			this.isTabAsNonTabSet = isTabAsNonTab;
			this.numVoices = vv;
			this.name = name;
			this.pieceNames = argPieceNames;
		}
		
		public String getStringRep() {
			return stringRep;
		}
		
		@Override
	    public String toString() {
	        return getStringRep();
	    }
		
		public boolean isTablatureSet() {
			return isTablatureSet;
		}
		
		private boolean isTabAsNonTabSet() {
			return isTabAsNonTabSet;
		}
				
		public String getName() {
			return name;
		}
		
		public void setName(String arg) {
			name = arg;
		}
		
		private int getIntRep() {
			return intRep;
		}
		
		public int getNumVoices() {
			return numVoices;
		}
		
		public void setNumVoices(int arg) {
			numVoices = arg;
		}

		private List<String> makePieceNames() {
//			List<String> res = new ArrayList<String>();
//			System.out.println(ALL_NAMES);
//			System.exit(0);
//			for (List<String> l : ALL_NAMES) { 
//				if (l.get(0).equals(this.toString())) {
//					res = l.subList(1, l.size());
//					break;
//				}
//			}
//			return res; 
		
			return pieceNamesMap.get(this);
			
//			System.out.println("mot det weer");
//			System.out.println(this);
//			System.out.println("this = " + this); 
//			System.out.println(bla.size());

//			System.out.println(bla.get(this));
			
//			System.exit(0);
//			return bla.get(this);
		}
		
		public List<String> getPieceNames() {
			return pieceNames;
		}
		
		public void setPieceNames(List<String> arg) {
			pieceNames = arg;
		}
		
	}


	// From http://stackoverflow.com/questions/6232986/how-do-i-initialize-a-two-dimensional-list-statically
	// See also https://coderanch.com/t/524994/java/java/Declaration-List-class-final-variable
	public static final List<List<String>> ALL_NAMES = Arrays.asList(
		INTAB_3VV_NAMES, INTAB_4VV_NAMES, INTAB_5VV_NAMES, FUGUES_4VV_NAMES
	);
	
	
//	public static enum Parts {
//		TWO("2vv", 2), THREE("3vv", 3), FOUR("4vv", 4), FIVE("5vv", 5);
//		
//		private String stringRep;
//		private int intRep;
//		Parts(String s, int i) {
//			this.stringRep = s;
//			this.intRep = i;
//		}
//		
//		public String getStringRep() {
//			return stringRep;
//		}
//		
//		public int getIntRep() {
//			return intRep;
//		}
//	};
	
	private DatasetID datasetID;
	private List<String> pieceNames;
	private int numVoices;
	private boolean isTablatureSet;
	private boolean isTabAsNonTabSet;
	private String name;
	
	private int largestChordSize;
	private int highestNumVoices;
	private List<Tablature> allTablatures; 
	private List<Transcription> allTranscriptions; 
	private List<File> allEncodingFiles;
	private List<File> allMidiFiles;


	public Dataset() {
	}


	public Dataset(DatasetID id) {
		// Set high-level information
		this.datasetID = id;
		this.pieceNames = id.getPieceNames();
		this.numVoices = id.getNumVoices();
		this.isTablatureSet = id.isTablatureSet();
		this.isTabAsNonTabSet = id.isTabAsNonTabSet();	
		this.name = id.getName();
	}


	public DatasetID getDatasetID() {
		return datasetID;
	}
	
	public List<String> getPieceNames() {
		return pieceNames;
	}

	public int getNumPieces() {
		return pieceNames.size();
	}
	
	public int getNumVoices() {
		return numVoices;
	}
	
	public boolean isTablatureSet() {
		return isTablatureSet;
	}
	
	public boolean isTabAsNonTabSet() {
		return isTabAsNonTabSet;
	}
	
	public String getName() {
		return name;
	}


	public void setName(String s) {
		name = s;
	}


	public void populateDataset(/*Map<String, Double> modelParams,*/ String version, 
		String[] altPaths, boolean appliedToNewData) {
		boolean isTablatureCase = isTablatureSet();
		boolean isTabAsNonTab = isTabAsNonTabSet();
//		boolean useCrossVal = 
//			ToolBox.toBoolean(Runner.getModelParams().get(Runner.CROSS_VAL).intValue());
		
//		boolean appliedToNewData =
//			ToolBox.toBoolean(Runner.getModelParams().get(Runner.APPL_TO_NEW_DATA).intValue());
//		boolean appliedToNewData =
//			ToolBox.toBoolean(modelParams.get(Runner.APPL_TO_NEW_DATA).intValue());

		String numVoices = getNumVoices() + "vv/";
		String argEncodingsPath, argMidiPath;
		String argTabMidiPath = null;

		if (altPaths == null) {
//			argEncodingsPath = Runner.encodingsPath.concat(getName().concat("/").concat(numVoices));
//			argTabMidiPath = Runner.midiPath.concat(getName().concat("/").concat(numVoices));

			if (!appliedToNewData) {
				argEncodingsPath = 
					Runner.encodingsPath.concat(getName().concat("/").concat(numVoices));
				argTabMidiPath = 
					Runner.midiPath.concat(getName().concat("/").concat(numVoices));				
				argMidiPath = Runner.midiPath.concat(getName().concat("/").
					concat(version).concat("/").concat(numVoices));
			}
			else {
				argEncodingsPath = Runner.encodingsPathUser; //.concat(getName().concat(numVoices));
				if (!argEncodingsPath.endsWith("/")) {
					argEncodingsPath = argEncodingsPath.concat("/");
//					argMidiPath = argEncodingsPath.concat("/");
				}
				argMidiPath = Runner.midiPathUser; //.concat(getName().concat("/").concat(numVoices));
				if (!argMidiPath.endsWith("/")) {
					argMidiPath = argMidiPath.concat("/");
				}		
			}
		}
		else {
			argEncodingsPath = altPaths[0];
			argTabMidiPath = altPaths[1];
			argMidiPath = altPaths[2];;
		}

		List<String> argPieceNames = getPieceNames();

		int argLargestChordSize = 0;
		int argHighestNumVoices = 0;
		List<Tablature> argAllTabs = new ArrayList<Tablature>();
		List<Transcription> argAllTrans = new ArrayList<Transcription>();
		List<File> argAllEncodingFiles = new ArrayList<File>();
		List<File> argAllMidiFiles = new ArrayList<File>();
		for (String currentPieceName : argPieceNames) {
			System.out.println("... creating " + currentPieceName + " ...");
			File encodingFile = null;
			Tablature currentTablature = null;
			File midiFile = null;
			if (isTablatureCase) {
				encodingFile = new File(argEncodingsPath + currentPieceName + ".tbp");
				midiFile = new File(argTabMidiPath + currentPieceName + ".mid");
				currentTablature = new Tablature(encodingFile, true);
			}
			else {
				midiFile = new File(argMidiPath + currentPieceName + ".mid");
				if (isTabAsNonTab) {
					midiFile = new File(argTabMidiPath + currentPieceName + ".mid");
				}
			}
//			// If midiFile is not a directory: add extension
//			if (!useCrossVal && appliedToNewData) {
//				midiFile = new File(midiFile + ".mid");
//			}
			Transcription currentTranscription = // zondag
				!appliedToNewData ? new Transcription(midiFile, encodingFile) : null;
//			Transcription currentTranscription = new Transcription(midiFile, encodingFile);
			
			if (isTabAsNonTab) {
				int currInterval = 0;
				if (currentPieceName.equals("ochsenkun-1558-absolon_fili-shorter")) {
					currInterval = 7;
				}
				else if (currentPieceName.equals("rotta-1546-bramo_morir")) {
					currInterval = -2;
				}
				else if (currentPieceName.equals("phalese-1563-las_on")) {
					currInterval = -2;
				}
				currentTranscription.transposeNonTab(currInterval);
			}
			
			// Determine the current largest chord
			int currentLargestChordSize = 0;
			if (isTablatureCase) {
				currentLargestChordSize = currentTablature.getLargestTablatureChord();
			}
			else {
				currentLargestChordSize = currentTranscription.getLargestTranscriptionChord();
			}
			// Determine the current highest number of voices
			int currentNumberOfVoices = // zondag
				!appliedToNewData ? currentTranscription.getNumberOfVoices() : 
				currentLargestChordSize; // TODO this assumption might not always hold true
//			int currentNumberOfVoices = currentTranscription.getNumberOfVoices();	
			if (currentNumberOfVoices > argHighestNumVoices) {
				argHighestNumVoices = currentNumberOfVoices;
			}
			if (currentLargestChordSize > argLargestChordSize) {
				argLargestChordSize = currentLargestChordSize;
			}
			// Add to the lists
			argAllEncodingFiles.add(encodingFile);
			argAllMidiFiles.add(midiFile);
			argAllTabs.add(currentTablature);
			argAllTrans.add(currentTranscription);

//			System.out.println("Creation of " + currentPieceName + " successful.");
		}

		largestChordSize = argLargestChordSize;
		highestNumVoices = argHighestNumVoices;
		allEncodingFiles = argAllEncodingFiles;
		allMidiFiles = argAllMidiFiles;
		allTablatures = argAllTabs;
		allTranscriptions = argAllTrans;
//		System.out.println(getIndividualPieceSizes(ModellingApproach.N2N));
//		pieceNames = argPieceNames;
	}
	
	public int getLargestChordSize() {
		return largestChordSize;
	}
	
	public int getHighestNumVoices() {
		return highestNumVoices;
	}

	public List<File> getAllEncodingFiles() {
		return allEncodingFiles;
	}

	public List<File> getAllMidiFiles() {
		return allMidiFiles; 
	}

	public List<Tablature> getAllTablatures() {
		return allTablatures; 
	}

	public List<Transcription> getAllTranscriptions() {
		return allTranscriptions; 
	}


//	public Dataset(DatasetID id, String[] dataPaths) {
//		
//		boolean isTablatureCase = id.isTablatureSet();
//		boolean isTabAsNonTab = id.isTabAsNonTabSet();
//		
////		setID(id);
//		String argEncodingsPath = dataPaths[0];
//		String argTabMidiPath = dataPaths[1];
//		String argBachMidiPath = dataPaths[2];
//		List<String> argPieceNames = makePieceNames();
//		
//		int argLargestChordSize = 0;
//		int argHighestNumVoices = 0;
//		List<Tablature> argAllTabs = new ArrayList<Tablature>();
//		List<Transcription> argAllTrans = new ArrayList<Transcription>();
//		List<File> argAllEncodingFiles = new ArrayList<File>();
//		List<File> argAllMidiFiles = new ArrayList<File>();
//		for (String currentPieceName : argPieceNames) {
//			System.out.println("   ... creating " + currentPieceName + " ...");
//			File encodingFile = null;
//			Tablature currentTablature = null;
//			File midiFile = null;
//			if (isTablatureCase) {
//				encodingFile = new File(argEncodingsPath + currentPieceName + ".tbp");
//				midiFile = new File(argTabMidiPath + currentPieceName);
//				currentTablature = new Tablature(encodingFile, true);
//			}
//			else {
//				midiFile = new File(argBachMidiPath + currentPieceName);
//				if (isTabAsNonTab) {
//					midiFile = new File(argTabMidiPath + currentPieceName);
//				}
//			}
//			Transcription currentTranscription = new Transcription(midiFile, encodingFile);
//			
//			if (isTabAsNonTab) {
//				int currInterval = 0;
//				if (currentPieceName.equals("ochsenkun-1558-absolon_fili-shorter")) {
//					currInterval = 7;
//				}
//				else if (currentPieceName.equals("rotta-1546-bramo_morir")) {
//					currInterval = -2;
//				}
//				else if (currentPieceName.equals("phalese-1563-las_on")) {
//					currInterval = -2;
//				}
//				currentTranscription.transposeNonTab(currInterval);
//			}
//			
////			// Determine the current largest chord
//			int currentLargestChordSize = 0;
//			// a. In the tablature case 
//			if (isTablatureCase) {
//				currentLargestChordSize = currentTablature.getLargestTablatureChord();
//			}
//			// b. In the non-tablature case
//			else {
//				currentLargestChordSize = currentTranscription.getLargestTranscriptionChord();
//			}
//			// Determine the current highest number of voices
//			int currentNumberOfVoices = currentTranscription.getNumberOfVoices(); 
//			// Reset if necessary
//			if (currentNumberOfVoices > argHighestNumVoices) {
//				argHighestNumVoices = currentNumberOfVoices;
//			}
//			if (currentLargestChordSize > argLargestChordSize) {
//				argLargestChordSize = currentLargestChordSize;
//			}
//			// Add to the lists
//			argAllEncodingFiles.add(encodingFile);
//			argAllMidiFiles.add(midiFile);
//			argAllTabs.add(currentTablature);
//			argAllTrans.add(currentTranscription);
//
//			System.out.println("Creation of " + currentPieceName + " successful.");
//		}
//
//		setLargestChordSize(argLargestChordSize);
//		setHighestNumVoices(argHighestNumVoices);
//		setAllEncodingFiles(argAllEncodingFiles);
//		setAllMidiFiles(argAllMidiFiles);
//		setAllTablatures(argAllTabs);
//		setAllTranscriptions(argAllTrans);
//		setPieceNames(argPieceNames);
//	}


	// TESTED
	public int getNumDataExamples(ModellingApproach ma) {
		return ToolBox.sumListInteger(getIndividualPieceSizes(ma));
	}


	/**
	 * Gets the size of the individual pieces in the dataset. Depending on the given 
	 * ModellingApproach, size is measured either as the number of notes or as the 
	 * number of chords.
	 * 
	 * @param ma
	 * @return
	 */
	// TESTED
	public List<Integer> getIndividualPieceSizes(ModellingApproach ma) {
		List<Integer> sizes = new ArrayList<Integer>();
		// Tablature case
		if (getDatasetID().isTablatureSet == true) {
			List<Tablature> allTabs = getAllTablatures();
			for (Tablature t : allTabs) {
				if (ma == ModellingApproach.N2N) {
					sizes.add(t.getNumberOfNotes());
				}
				else if (ma == ModellingApproach.C2C || ma == ModellingApproach.HMM) {
					sizes.add(t.getTablatureChords().size());
				}
			}
		}
		// Non-tablature case
		else {
			List<Transcription> allTrans = getAllTranscriptions();
			for (Transcription t : allTrans) {
				if (ma == ModellingApproach.N2N) {
					sizes.add(t.getNumberOfNotes());
				}
				else if (ma == ModellingApproach.C2C || ma == ModellingApproach.HMM) {
					sizes.add(t.getTranscriptionChords().size());
				}
			}
		}	
		return sizes;
	}


	private List<Integer> getBeginIndices(ModellingApproach ma) {
		List<Integer> beginIndices = new ArrayList<Integer>();

		int totalSize = 0;
		for (int i : getIndividualPieceSizes(ma)) {
			int currSize = i;
			totalSize += currSize;
			beginIndices.add(totalSize - currSize);
		}
		return beginIndices;
	}


//	public static List<String> getTabThreeVoices() {
//		List<String> tabThree = new ArrayList<String>();
//		tabThree.add("newsidler-1536-disant_adiu");
//		tabThree.add("newsidler-1536-mess_pensees");
//		tabThree.add("pisador-1552-pleni_de");
//		tabThree.add("judenkuenig-1523-elslein_liebes");
//		tabThree.add("newsidler-1544-nun_volget");
//		tabThree.add("phalese-1547-tant_que-a3");
//		return tabThree;
//	}
	
	
//	public static List<String> getTabFourVoices() {
//		List<String> tabFour = new ArrayList<String>();
//		tabFour.add("ochsenkun-1558-absolon_fili");
//		tabFour.add("ochsenkun-1558-in_exitu");
//		tabFour.add("ochsenkun-1558-qui_habitat");
//		tabFour.add("rotta-1546-bramo_morir");
//		tabFour.add("phalese-1547-tant_que-a4");
//		tabFour.add("ochsenkun-1558-herr_gott");
//		tabFour.add("abondante-1548-mais_mamignone");
//		tabFour.add("phalese-1563-las_on");
//		tabFour.add("barbetta-1582_1-il_nest");
//		return tabFour;
//	}
	
//	public static List<String> getTabFiveVoices() {
//		List<String> tabFive = new ArrayList<String>();
//		tabFive.add("adriansen-1584-d_vn_si");
//		tabFive.add("ochsenkun-1558-inuiolata_integra");
//		return tabFive;
//	}
	
	
	public static List<String> getBachTwoVoiceFugues() {
		List<String> bachFuguesTwo = new ArrayList<String>();
		bachFuguesTwo.add("Bach - WTC1, Fuga 10 in e minor (BWV 855) (1)");
		bachFuguesTwo.add("Bach - WTC1, Fuga 10 in e minor (BWV 855) (2)");
		return bachFuguesTwo;
	}
	
	public static List<String> getBachThreeVoiceFugues() {
		List<String> bachFuguesThree = new ArrayList<String>();
		bachFuguesThree.add("Bach - WTC1, Fuga 2 in c minor (BWV 847)");
		bachFuguesThree.add("Bach - WTC1, Fuga 3 in C# major (BWV 848)");
		bachFuguesThree.add("Bach - WTC1, Fuga 6 in d minor (BWV 851)");
		bachFuguesThree.add("Bach - WTC1, Fuga 7 in Eb major (BWV 852)");
		bachFuguesThree.add("Bach - WTC1, Fuga 8 in d# minor (BWV 853)");
		bachFuguesThree.add("Bach - WTC1, Fuga 9 in E major (BWV 854)");
		bachFuguesThree.add("Bach - WTC1, Fuga 11 in F major (BWV 856)");
		bachFuguesThree.add("Bach - WTC1, Fuga 13 in F# major (BWV 858)");
		bachFuguesThree.add("Bach - WTC1, Fuga 15 in G major (BWV 860)");
		bachFuguesThree.add("Bach - WTC1, Fuga 19 in A major (BWV 864)");
		bachFuguesThree.add("Bach - WTC1, Fuga 21 in Bb major (BWV 866)");
		bachFuguesThree.add("Bach - WTC2, Fuga 1 in C major (BWV 870)");
		bachFuguesThree.add("Bach - WTC2, Fuga 3 in C# major (BWV 872)");
		bachFuguesThree.add("Bach - WTC2, Fuga 4 in c# minor (BWV 873)");
		bachFuguesThree.add("Bach - WTC2, Fuga 6 in d minor (BWV 875)");
		bachFuguesThree.add("Bach - WTC2, Fuga 10 in e minor (BWV 879)");
		bachFuguesThree.add("Bach - WTC2, Fuga 11 in F major (BWV 880)");
		bachFuguesThree.add("Bach - WTC2, Fuga 12 in f minor (BWV 881)");
		bachFuguesThree.add("Bach - WTC2, Fuga 13 in F# major (BWV 882)");
		bachFuguesThree.add("Bach - WTC2, Fuga 14 in f# minor (BWV 883)");
		bachFuguesThree.add("Bach - WTC2, Fuga 15 in G major (BWV 884)");
		bachFuguesThree.add("Bach - WTC2, Fuga 18 in g# minor (BWV 887)");
		bachFuguesThree.add("Bach - WTC2, Fuga 19 in A major (BWV 888)");
		bachFuguesThree.add("Bach - WTC2, Fuga 20 in a minor (BWV 889)");
		bachFuguesThree.add("Bach - WTC2, Fuga 21 in Bb major (BWV 890)");
		bachFuguesThree.add("Bach - WTC2, Fuga 24 in b minor (BWV 893)");
		return bachFuguesThree;
	}


//	public static List<String> getBachFourVoiceFugues() {
//		List<String> bachFuguesFour = new ArrayList<String>();
//
//		bachFuguesFour.add("bach-WTC1-fuga_1-BWV_846");
//		bachFuguesFour.add("bach-WTC1-fuga_5-BWV_850");
//		bachFuguesFour.add("bach-WTC1-fuga_12-BWV_857");
//		bachFuguesFour.add("bach-WTC1-fuga_14-BWV_859");
//		bachFuguesFour.add("bach-WTC1-fuga_16-BWV_861");	
//		bachFuguesFour.add("Bach - WTC1, Fuga 5 in D major (BWV 850)");
//		bachFuguesFour.add("Bach - WTC1, Fuga 12 in f minor (BWV 857)");
//		bachFuguesFour.add("Bach - WTC1, Fuga 14 in f# minor (BWV 859)");
//		bachFuguesFour.add("Bach - WTC1, Fuga 16 in g minor (BWV 861)");
//		bachFuguesFour.add("Bach - WTC1, Fuga 17 in Ab major (BWV 862)");
//		bachFuguesFour.add("Bach - WTC1, Fuga 18 in g# minor (BWV 863)");
//		bachFuguesFour.add("Bach - WTC1, Fuga 20 in a minor (BWV 865)");
//		bachFuguesFour.add("Bach - WTC1, Fuga 23 in B major (BWV 868)");
//		bachFuguesFour.add("Bach - WTC1, Fuga 24 in b minor (BWV 869)");
//		
//		bachFuguesFour.add("Bach - WTC2, Fuga 2 in c minor (BWV 871)");
//		bachFuguesFour.add("Bach - WTC2, Fuga 5 in D major (BWV 874)");
//		bachFuguesFour.add("Bach - WTC2, Fuga 7 in Eb major (BWV 876)");
//		bachFuguesFour.add("Bach - WTC2, Fuga 8 in d# minor (BWV 877)");
//		bachFuguesFour.add("Bach - WTC2, Fuga 9 in E major (BWV 878)");
//		bachFuguesFour.add("Bach - WTC2, Fuga 16 in g minor (BWV 885)");
//		bachFuguesFour.add("Bach - WTC2, Fuga 17 in Ab major (BWV 886)");
//		bachFuguesFour.add("Bach - WTC2, Fuga 22 in bb minor (BWV 891)");
//		bachFuguesFour.add("Bach - WTC2, Fuga 23 in B major (BWV 892)");
//		
//		return bachFuguesFour;
//	}
	
	public static List<String> getBachFiveVoiceFugues() {
		List<String> bachFuguesFive = new ArrayList<String>();
		bachFuguesFive.add("Bach - WTC1, Fuga 4 in c# minor (BWV 849) (1)");
		bachFuguesFive.add("Bach - WTC1, Fuga 4 in c# minor (BWV 849) (2)");
		bachFuguesFive.add("Bach - WTC1, Fuga 22 in bb minor (BWV 867) (1)");
		bachFuguesFive.add("Bach - WTC1, Fuga 22 in bb minor (BWV 867) (2)");
		return bachFuguesFive;
	}
	
	public static List<String> getBachTwoVoiceInventions() {
		List<String> bachInventionsTwo = new ArrayList<String>();
		bachInventionsTwo.add("Bach - Inventio 1 in C major (BWV 772)");
		bachInventionsTwo.add("Bach - Inventio 2 in c minor (BWV 773)");
		bachInventionsTwo.add("Bach - Inventio 3 in D major (BWV 774)");
		bachInventionsTwo.add("Bach - Inventio 4 in d minor (BWV 775)");
		bachInventionsTwo.add("Bach - Inventio 5 in Eb major (BWV 776)");
		bachInventionsTwo.add("Bach - Inventio 6 in E major (BWV 777)");
		bachInventionsTwo.add("Bach - Inventio 7 in e minor (BWV 778)");
		bachInventionsTwo.add("Bach - Inventio 8 in F major (BWV 779)");
		bachInventionsTwo.add("Bach - Inventio 9 in f minor (BWV 780)");
		bachInventionsTwo.add("Bach - Inventio 10 in G major (BWV 781)");
		bachInventionsTwo.add("Bach - Inventio 11 in g minor (BWV 782)");
		bachInventionsTwo.add("Bach - Inventio 12 in A major (BWV 783)");
		bachInventionsTwo.add("Bach - Inventio 13 in a minor (BWV 784)");
		bachInventionsTwo.add("Bach - Inventio 14 in Bb major (BWV 785)");
		bachInventionsTwo.add("Bach - Inventio 15 in b minor (BWV 786)");
		return bachInventionsTwo;
	}
	
	public static List<String> getBachThreeVoiceInventions() {
		List<String> bachInventionsThree = new ArrayList<String>();
		bachInventionsThree.add("Bach - Sinfonia 1 in C Major (BWV 787)");
		bachInventionsThree.add("Bach - Sinfonia 2 in c minor (BWV 788)");
		bachInventionsThree.add("Bach - Sinfonia 3 in D Major (BWV 789)");
		bachInventionsThree.add("Bach - Sinfonia 4 in d minor (BWV 790)");
		bachInventionsThree.add("Bach - Sinfonia 5 in Eb Major (BWV 791)");
		bachInventionsThree.add("Bach - Sinfonia 6 in E Major (BWV 792)");
		bachInventionsThree.add("Bach - Sinfonia 7 in e minor (BWV 793)");
		bachInventionsThree.add("Bach - Sinfonia 8 in F Major (BWV 794)");
		bachInventionsThree.add("Bach - Sinfonia 9 in f minor (BWV 795)");
		bachInventionsThree.add("Bach - Sinfonia 10 in G Major (BWV 796)");
		bachInventionsThree.add("Bach - Sinfonia 11 in g minor (BWV 797)");
		bachInventionsThree.add("Bach - Sinfonia 12 in A Major (BWV 798)");
		bachInventionsThree.add("Bach - Sinfonia 13 in a minor (BWV 799)");
		bachInventionsThree.add("Bach - Sinfonia 14 in Bb Major (BWV 800)");
		bachInventionsThree.add("Bach - Sinfonia 15 in b minor (BWV 801)");
		return bachInventionsThree;
	}

	
	public static List<String> getTestpieceSet() {
		List<String> testSet = new ArrayList<String>();
		testSet.add("testpiece");
		testSet.add("testpiece");
		testSet.add("testpiece");
		return testSet;
	}
	
	
	private List<String> makePieceNames() {
		DatasetID name = getDatasetID();
		
		// TODO generate lists automatically by reading folders?
//		List<String> tabThree = getTabThreeVoices();
//		List<String> tabFour = getTabFourVoices();
//		List<String> tabFive = getTabFiveVoices();
		
		List<String> bachFuguesTwo = getBachTwoVoiceFugues();
		List<String> bachFuguesThree = getBachThreeVoiceFugues();
//		List<String> bachFuguesFour = getBachFourVoiceFugues();
		List<String> bachFuguesFive = getBachFiveVoiceFugues();
		
		List<String> bachInventionsTwo = getBachTwoVoiceInventions();
		List<String> bachInventionsThree = getBachThreeVoiceInventions();
		
		List<String> testpieceSet = getTestpieceSet();
		
		List<String> argPieceNames = new ArrayList<String>();
		switch (name) {
//			case TAB_3VV: case TAB_3VV_NON_TAB:
//				argPieceNames.addAll(tabThree);
//				break;
//			case TAB_4VV: case TAB_4VV_NON_TAB:
//				argPieceNames.addAll(tabFour);
//				break;
//			case TAB_5VV: case TAB_5VV_NON_TAB:
//				argPieceNames.addAll(tabFive);
//				break;
//			case IMI: case IMI_NON_TAB:
//				argPieceNames.addAll(tabFour.subList(0, 3));
//				break;
//			case IMI_SHORTER: case IMI_SHORTER_NON_TAB:
//				List<String> imi = tabFour.subList(0, 3);
//				for (String s: imi) {
//					argPieceNames.add(s.concat("-shorter"));
//				}
//				break;
//			case FREE: case FREE_NON_TAB:
//				argPieceNames.addAll(tabFour.subList(3, 6));
//				break;
//			case FREE_MORE: case FREE_MORE_NON_TAB:
//				argPieceNames.addAll(tabFour.subList(3, 6));
//				argPieceNames.add("phalese-1563-il_estoit");
//				break;
//			case SEMI: case SEMI_NON_TAB:
//				argPieceNames.addAll(tabFour.subList(6, 9));
//				break; 
//			case OCHSENKUN:
//				argPieceNames.add("Ochsenkun 1558 - Absolon fili mi");
//				argPieceNames.add("Ochsenkun 1558 - In exitu Israel de Egipto");
//				argPieceNames.add("Ochsenkun 1558 - Qui habitat");
//				argPieceNames.add("Ochsenkun 1558 - Herr Gott lass dich erbarmen");
//				break;
//			case OTHER_INTABULATORS:
//				argPieceNames.add("Rotta 1546 - Bramo morir per non patir piu morte");
//				argPieceNames.add("Phalese 1547 - Tant que uiuray [a4]");
//				argPieceNames.add("Abondante 1548 - mais mamignone");
//				argPieceNames.add("Phalese 1563 - LAs on peult");
//				argPieceNames.add("Barbetta 1582 - Il nest plaisir");
//				break;
//			case FUGUES_2VV:
//				argPieceNames.addAll(bachFuguesTwo);
//				break;
//			case FUGUES_3VV:
//				argPieceNames.addAll(bachFuguesThree);
//				break;	
//			case FUGUES_4VV:
//				argPieceNames.addAll(bachFuguesFour);
//				break;
//			case FUGUES_5VV:
//				argPieceNames.addAll(bachFuguesFive);
//				break;
//			case INV_2VV:
//				argPieceNames.addAll(bachInventionsTwo);
//				break;
//			case INV_3VV:
//				argPieceNames.addAll(bachInventionsThree);
//				break;
//			case TESTPIECE_SET:
//				argPieceNames.addAll(testpieceSet);
//				break;
//			case TESTPIECE_SET_NON_TAB:
//				argPieceNames.addAll(testpieceSet);
//				break;
		}		
		return argPieceNames;	
	}


	private List<Integer> getTranspositionIntervals(DatasetID n) {
	  List<Integer> transpositionIntervals = null; 
		switch (n) {  
	    case WTC_4vv:
	    	transpositionIntervals = 
	    	  Arrays.asList(new Integer[]{new Integer(4), new Integer(2), new Integer(-1), new Integer(-2), new Integer(-3)});	    	  
	    	break;
	    case WTC_3vv:
	    	transpositionIntervals = 
	    	  Arrays.asList(new Integer[]{new Integer(2), new Integer(1), new Integer(0), new Integer(-1), new Integer(-1)});
	    	break;
		}
		return transpositionIntervals;
	}
	
}

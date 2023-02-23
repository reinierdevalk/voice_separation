package ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;

import data.Dataset;
import data.Dataset.DatasetID;
import ui.Runner.Model;
import ui.Runner.ProcessingMode;



public class GUI {
	
	public static String experiment;
	public static DatasetID datasetID;
	public static Model model;
	public static ProcessingMode pm;
	private static void initialize() {
		JFrame jf = new JFrame();
		jf.setSize(717, 354);
		jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		// getEncodingWindowMenubar() -->
		JMenuBar mb = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		mb.add(fileMenu);   
		JMenuItem openFile = new JMenuItem("Open"); 
		fileMenu.add(openFile); 
		openFile.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
//				openFileAction();
			}
		});
		JMenuItem saveFile = new JMenuItem("Save");
		fileMenu.add(saveFile);
		saveFile.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
//				saveFileAction();
			}
		});
		// <-- getEncodingWindowMenubar()
		
		// --> getEncodingWindowPanel()
		JPanel encodingWindowPanel = new JPanel();
		encodingWindowPanel.setLayout(null);
		encodingWindowPanel.setSize(new Dimension(586, 413));
		// <-- getEncodingWindowPanel()
		
		jf.setJMenuBar(mb);
		jf.setContentPane(encodingWindowPanel);
				
		JRadioButton btnFwd = new JRadioButton("FWD");
		
		btnFwd.setSelected(true);
		btnFwd.setBounds(569, 55, 109, 23);
		btnFwd.addActionListener(new ActionListener() {
    		@Override
			public void actionPerformed(ActionEvent e) {
    			// TODO Auto-generated Event stub actionPerformed()
    			pm = ProcessingMode.FWD;
    			System.out.println(pm);
    		}
    	});
		encodingWindowPanel.add(btnFwd);
		
		JRadioButton btnBwd = new JRadioButton("BWD");
		btnBwd.setBounds(569, 81, 109, 23);
		btnBwd.addActionListener(new ActionListener() {
    		@Override
			public void actionPerformed(ActionEvent e) {
    			// TODO Auto-generated Event stub actionPerformed()
    			pm = ProcessingMode.BWD;
    			System.out.println(pm);
    		}
    	});
		encodingWindowPanel.add(btnBwd);
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(btnFwd);
		bg.add(btnBwd);
				
		JLabel lblProcMode = new JLabel("Processing mode");
		lblProcMode.setBounds(454, 50, 109, 32);
		encodingWindowPanel.add(lblProcMode);
		
		JLabel lblModel = new JLabel("Model");
		lblModel.setBounds(454, 16, 74, 23);
		encodingWindowPanel.add(lblModel);
		
		JLabel lblDataset = new JLabel("Dataset");
		lblDataset.setBounds(234, 16, 99, 23);
		encodingWindowPanel.add(lblDataset);
		
		JLabel lblExperiment = new JLabel("Experiment");
		lblExperiment.setBounds(10, 16, 200, 23);
		encodingWindowPanel.add(lblExperiment);
		
		JComboBox<Model> cbMod = new JComboBox<Model>();
		
//		cbMod.setSelectedItem((Model) Model.N);
//		model = (Model) cbMod.getSelectedItem();
//
		model = Model.B_PRIME_STAR;
//		cbMod.setSelectedItem(model);
		
		System.out.println("1. model = " + model);
		List<Model> selMod = new ArrayList<Model>();
		for (Model m : Model.values()) {
			if (!excludeModels().contains(m)) {
				selMod.add(m);
			}
		}
//		JComboBox<Model> cbMod = new JComboBox<Model>(selMod.toArray(new Model[0]));
		cbMod.setModel(new DefaultComboBoxModel(selMod.toArray(new Model[0])));
		cbMod.setBounds(567, 16, 124, 23);
		cbMod.setSelectedIndex(4);
		cbMod.setSelectedItem(model);
		cbMod.addActionListener(new ActionListener() {
			 @Override
			public void actionPerformed(ActionEvent e) {
				 Model m = (Model) cbMod.getSelectedItem();
				 model = m;
				 System.out.println(m);
			 }
		});
		System.out.println("2. model = " + model);
		encodingWindowPanel.add(cbMod);
		
		JComboBox<DatasetID> cbDataset = new JComboBox<DatasetID>();
		List<DatasetID> selID = new ArrayList<DatasetID>();
		for (DatasetID id : DatasetID.values()) {
			if (!excludeDatasetIDs().contains(id)) {
				selID.add(id);
			}
		}
//		JComboBox<DatasetID> cbDataset = new JComboBox<DatasetID>(selMod.toArray(new DatasetID[0]));
		Dataset di = null;
		
		cbDataset.setModel(new DefaultComboBoxModel(selID.toArray(new DatasetID[0])));
		cbDataset.setBounds(290, 17, 124, 23);
		cbDataset.addActionListener(new ActionListener() {
			 @Override
			public void actionPerformed(ActionEvent e) {
				 DatasetID di = (DatasetID) cbDataset.getSelectedItem();
				 datasetID = di;
				 System.out.println(di);
				 System.out.println(datasetID);
			 }
		});
		encodingWindowPanel.add(cbDataset);
		System.out.println("datasetId = " + datasetID);
		
		JComboBox<String> cbExp = new JComboBox<String>();
		cbExp.setModel(new DefaultComboBoxModel(new String[] {"1", "2.1", "2.2", "3.1", "3.2", "3.3.1", "3.3.2", "4", "other"}));
		cbExp.setBounds(82, 17, 137, 22);
		ActionListener alExp = new ActionListener() {
			 @Override
			public void actionPerformed(ActionEvent e) {
				 String exp = (String) cbExp.getSelectedItem();
				 experiment = exp;
			 }
		};
		cbExp.addActionListener(alExp);
		encodingWindowPanel.add(cbExp);
		
		JButton btnGo = new JButton("GO!");
		btnGo.setBounds(244, 155, 124, 53);
		encodingWindowPanel.add(btnGo);
		btnGo.addActionListener(new ActionListener()  {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				jf.setVisible(false);
				test();
			}
		});
		
		jf.setTitle("SketchPad");
		jf.setVisible(true);
	}
	
	
	public static void test() {
		System.out.println("Henk zegt OK!");
		System.out.println(model);
		System.out.println(datasetID);
		System.out.println(experiment);
	};
	
	public static List<DatasetID> excludeDatasetIDs() {
		List<DatasetID> l = Arrays.asList(new DatasetID[]{
			DatasetID.TAB_INT_5VV,
			DatasetID.TAB_INT_3VV,
			DatasetID.TAB_INT_ANT_4VV,
			DatasetID.TAB_INT_ANT_5VV,	
			DatasetID.TAB_INT_IMI_4VV, 
			DatasetID.TAB_INT_IMI_SHORT_4VV, 
			DatasetID.TAB_INT_SEMI_4VV, 
			DatasetID.TAB_INT_FREE_4VV, 
			DatasetID.TAB_INT_FREE_MORE_4VV, 
			DatasetID.TAB_INT_IMI_ANT_4VV, 
			DatasetID.TAB_INT_IMI_SHORT_ANT_4VV, 
			DatasetID.TAB_INT_SEMI_ANT_4VV,
			DatasetID.TAB_INT_FREE_ANT_4VV,	
			DatasetID.TAB_INT_FREE_MORE_ANT_4VV,
			DatasetID.TAB_TEST,
			DatasetID.TEST
		});	
		return l;
	}
	
	
	public static List<Model> excludeModels() {
		List<Model> l = Arrays.asList(new Model[]{
				Model.STM,
				Model.LTM,
				Model.MIX,
				Model.PROD,	
				Model.ENS_STM, 
				Model.ENS_LTM 
		});	
		return l;
	}
}

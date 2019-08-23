package machineLearning;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.ISequenceModelS;
import n_grams.KylmModel;


public class MelodyPredictor implements Serializable{

	public enum MelModelType{SIMPLE_LM};
	private static MelModelType melModelType;
	private static KylmModel.Type termType;
	public static List<Double> modelOutputWeights;
//	public static final double W_NN = Math.pow(0.97, 2);
//	public static final double W_MM_p = Math.pow(0.85, 2);
//	public static final double W_MM_d = Math.pow(0.85, 2);
//	public static final double W_MM_rp = Math.pow(0.85, 2);
//	public static final double W_MM_ioi = Math.pow(0.85, 2);
	
	public static final String W_NN = "weight_NN";
	public static final String W_MM_P = "weight_MM_pitch";
	public static final String W_MM_D = "weight_MM_duration";
	public static final String W_MM_RP = "weight_MM_relative_pitch";
	public static final String W_MM_IOI = "weight_MM_ioi";

//	ISequenceModel smod = new SequenceModel();
//	ISequenceModel smod; // = new SimpleLM(SimpleLM.Type.SHORT);
	ISequenceModelS smod; // = new SimpleLM(SimpleLM.Type.SHORT);

	public MelodyPredictor(MelModelType argModel, KylmModel.Type argType, int n, int feature) {
		if (argModel == MelModelType.SIMPLE_LM) {
			smod = new KylmModel(argType, n, feature);
		}
	}


	public double modelProbability(List<List<String>> subMelody, int voiceNum){
		return smod.modelProbability(subMelody, voiceNum); 
	}
	
	
	public static void setTermType(KylmModel.Type arg) {
		termType = arg;
	}
	
	public static KylmModel.Type getTermType() {
		return termType;
	}
	
	public static void setMelModelType(MelModelType arg) {
		melModelType = arg;
	}
	
	public static MelModelType getMelModelType() {
		return melModelType;
	}


	public void trainModel(List<List<List<String>>> melodyList){
		smod.trainModel(melodyList); 
	}


	public void saveModel(File f){
		smod.saveModel(f);
	}


	public void loadModel(File f){
		smod.loadModel(f);
	}


	public void resetSTM() {
		smod.resetShortTermModel();
	}


	public void setSliceIndex(int arg) {
		smod.setSliceIndex(arg);
	}


//	public String getSliceIndexString(int arg) {
//		return smod.getSliceIndexString(arg);
//	}
	
	public static String getSliceIndexString(int arg) {
		return KylmModel.getSliceIndexString(arg);
	}
	
	
	public static void setModelOutputWeights(Map<String, Double> modelParameters, 
		List<Integer> sliceIndices) {
		modelOutputWeights = new ArrayList<Double>();
		modelOutputWeights.add(modelParameters.get(W_NN));
		for (int i : sliceIndices) {	
			if (i == KylmModel.PITCH) {
				modelOutputWeights.add(modelParameters.get(W_MM_P));
			}
			else if (i == KylmModel.DUR) {
				modelOutputWeights.add(modelParameters.get(W_MM_D));
			}
			else if (i == KylmModel.REL_PITCH) {
				modelOutputWeights.add(modelParameters.get(W_MM_RP));
			}
			else if (i == KylmModel.IOI) {
				modelOutputWeights.add(modelParameters.get(W_MM_IOI));
			}
		}
	}
	
	
	private static List<Double> getModelOutputWeights() {
		return modelOutputWeights;
	}

}

package utility;

import java.util.ArrayList;
import java.util.List;

// Each object of this class should represent one training example and consist of
// 1) an ArrayList of Doubles that represent the training example's feature
// 2) an ArrayList of Doubles that represents a training example's voice label

public class TabTrainingExample {
  
  private List<Double> features = new ArrayList<Double>();
  private List<Double> voiceLabel = new ArrayList<Double>();

  
  public List<Double> getFeatures() {
    return features;
  }

  
  public List<Double> getVoiceLabel() {
    return voiceLabel;
  }
  
  
  public void setFeatures(int index, double feature) {
    this.features.set(index, feature); 
  }
  
  
  public void setFeatures(List<Double> featuresOfOnset) {
    features = featuresOfOnset;
  }
  
  
  public void setVoiceLabel(List<Double> voiceLabelOfOnset) {
    voiceLabel = voiceLabelOfOnset;
  }
  
  
  public double getBias() {
    return 1.0;
  }

  
  public double getPitch() {
    return features.get(0);
  }

  
  public double getGivenDuration() {
    return features.get(1);
  }
  

  public double getCourse() {
    return features.get(2);
  }

  
  public double getPitchDistToFirst() {
    return features.get(3);
  }
 
  
  public double getPitchDistToSecond() {
    return features.get(4);
  }
 
  
  public double getPitchDistToThird() {
    return features.get(5);
  }
 
  
  public double getPitchDistToFourth() {
    return features.get(6);
  }

  
  public double getPitchDistToFifth() {
    return features.get(7);
  }
  
  
  public double getMaximumPhysicalDuration() {
    return features.get(8);
  } 
  
  
  public String toString() {
   StringBuffer sb = new StringBuffer("[");
   for (int i = 0; i < features.size(); i++) {
     sb.append(features.get(i));
     sb.append(" ");
   }
   sb.append("] [");
   for (int i = 0; i < voiceLabel.size(); i++) {
     sb.append(voiceLabel.get(i));
     sb.append(" ");
   }
   sb.append("]");
   return sb.toString();
 }
  
}

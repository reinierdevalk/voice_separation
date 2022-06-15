package python;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import representations.Transcription;


public class PythonInterface {

	// See https://norwied.wordpress.com/2012/03/28/call-python-script-from-java-app/ and
	// https://norwied.wordpress.com/2012/07/23/pass-arguments-from-java-to-python-app/
	private static BufferedReader bfrdr;
	private static Writer bfwtr;
	private static final boolean VERBOSE = true;


	/**
	 * Runs a Python file as a script. 
	 *    
	 * @param cmd The command to run the script: <code>python path/to/file.py arg_1 arg_2 ... arg_n</code> 
	 * @throws IOException
	 */
	public static void runPythonFileAsScript(String[] cmd) {
		if (VERBOSE) System.out.println("\r\n>>> PythonInterface.runPythonFileAsScript() called");
		try {
			// Create Runtime to interface with the environment the Java application is running
			// in, and execute command (run Python file as a script) to start Process
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec(cmd);

			// bfr reads Process output (i.e., reads any output given by the commands passed to Process)
			BufferedReader bfr = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			if (VERBOSE) System.out.println("\r\n>>> output received from Process (Python):");
			String line = null;
			while((line = bfr.readLine()) != null) {
				if (VERBOSE) System.out.println(line);
			}

			// bfrErr reads Process) errors (i.e., reads any errors given by the commands passed to Process). 
			// See Listing 4.3 at http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html
			BufferedReader bfrErr = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
			if (VERBOSE) System.out.println("\r\n>>> errors received from Process (Python):");
			line = null;
			while ((line = bfrErr.readLine()) != null) {
				if (VERBOSE) System.out.println(line);
			}
			if (VERBOSE) System.out.println("Process exitValue: " + pr.waitFor());
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}


	/**
	 * Runs an IPython session. Python files can be imported as modules.
	 * 
	 * @param cmds The commands to pass to the IPython session.
	 * @return 
	 */
	public static Process runIPythonSession(String cmds) {
		if (VERBOSE) System.out.println("\r\n>>> PythonInterface.runIPythonSession() called");

		Process pr = null;
		try {
			// Create Runtime to interface with the environment the Java application is running
			// in, and execute command (run IPython) to start Process
			Runtime rt = Runtime.getRuntime();		
			pr = rt.exec("ipython");

			// Add printing of special token to cmds so that bfr knows when to stop reading
			cmds += "print('#')" + "\r\n";

			// bfw writes to Process (i.e., passes commands to Process and executes them)
			BufferedWriter bfw = new BufferedWriter(new OutputStreamWriter(pr.getOutputStream()));
			if (VERBOSE) System.out.println("\r\n>>> commands passed to Process (IPython):" + "\r\n" + cmds);
			bfw.write(cmds);
			bfw.flush();

			// bfr reads Process output (i.e., reads any output given by the commands passed to Process).
			// For the n-th IPython command, this results in a line looking as follows 
			// In [n]: <output string>
			BufferedReader bfr = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			if (VERBOSE) System.out.println("\r\n>>> output received from Process (IPython):");
			
			String line = null;
			while (!(line = bfr.readLine()).endsWith("#")) {
				if (VERBOSE) System.out.println(line);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return pr;
	}


	/**
	 * Adds to an active IPython session and returns its output.
	 * 
	 * @param pr The Process containing the IPython session.
	 * @param cmds The commands to pass to the IPython session.
	 * @return The IPython session output.
	 */
	public static String addToIPythonSession(Process pr, String cmds) {
		if (VERBOSE) {
			System.out.println("\r\n>>> PythonInterface.addToIPythonSession() called");
		}
		StringBuilder output = new StringBuilder();
		try {
			// Add printing of special token to cmds so that bfr knows when to stop reading
			cmds += "print('#')" + "\r\n";

			// bfw writes to Process (i.e., passes commands to Process and executes them)
			BufferedWriter bfw = new BufferedWriter(new OutputStreamWriter(pr.getOutputStream()));
			
			if (VERBOSE) System.out.println("\r\n>>> commands passed to Process (IPython):" + "\r\n" + cmds);
			bfw.write(cmds);
			bfw.flush();

			// bfr reads Process output (i.e., reads any output given by the commands passed to Process).
			// For the n-th IPython command, this results in a line looking as follows 
			// In [n]: <output string>
			BufferedReader bfr = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			if (VERBOSE) System.out.println("\r\n>>> output received from Process (IPython):");
			String line = null;
			while (!(line = bfr.readLine()).endsWith("#")) {
//			while ((line = bfr.readLine()) != null) {
				if (VERBOSE) System.out.println(line);
				output.append(line + "\r\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return output.toString();
	}


//	public static double[] getPrediction(String output) {
//		String predStr = output.substring(output.indexOf("@[") + 1, output.indexOf("]@") + 1);
//		System.out.println(predStr);
//		System.exit(0);
//		
//		// The prediction can be one line, or broken up into multiple (usually two) lines
//		// Check for start of prediction		
//		String predictionStr = "";
//		if (predictionStr.equals("")) {	
//			// a. Line contains complete prediction (one and only line)
//			if (line.contains("@[") && line.contains("]@")) {		
//				predictionStr = line.substring(line.indexOf("@[") + 1, line.indexOf("]@") + 1);
//				break;
//			}
//			// b. Line contains start of prediction, but not end (first of multiple lines)
//			if (line.contains("@[") && !line.contains("]@")) {
//				predictionStr = line.substring(line.indexOf("@[") + 1);
//			}
//		}
//		// Check for continuation or end of prediction 
//		else {
//			// a. Line contains end of prediction (last of multiple lines)
//			if (line.contains("]@")) {
//				predictionStr += line.substring(0, line.indexOf("@"));
//				break;
//			}
//			// b. Line contains middle of prediction (middle of multiple lines)
//			else {
//				predictionStr += line;
//			}
//		}
//		
//		// Convert stringified prediction to double
//		predictionStr = predictionStr.substring(predictionStr.indexOf("[") + 1, predictionStr.indexOf("]"));
//		String[] indiv = predictionStr.contains(",") ? predictionStr.split(",") : predictionStr.split(" ");
//		for (int i = 0; i < indiv.length; i++) {
//			prediction[i] = Double.parseDouble(indiv[i].trim());
//		}
//	}


	public static double[] predictNoLoading(String argFv) {
		double[] output = new double[Transcription.MAXIMUM_NUMBER_OF_VOICES];
		
		String cr = "\r\n";
//		String path = cmd[0]; // path
//		String argFv = cmd[1]; // String rep of fv
//		String outpExt = cmd[2]; // Runner.outpExt + "appl.csv"
		try {					
			String cmdStr = 
//				"print('loading features and applying model')" + cr +
//				"X = np.loadtxt('" + path + fvExt + "', delimiter=\",\")" + cr +
				"arg_fv = '" + argFv + "'" + cr + 
				"X_list = [float(s.strip()) for s in arg_fv.split(',')]" + cr +
//				"print(X_list)" + cr +
				"X = np.array(X_list)" + cr +
				"X = X.reshape(1, -1)" + cr +
				"classes = m.predict(X)" + cr +
				"probs = m.predict_proba(X)" + cr +
				"max_num_voices = 5" + cr +
				"num_cols_to_add = max_num_voices - len(probs[0])" + cr + 
				"num_ex = len(probs)" + cr +
				"z = np.zeros((num_ex, num_cols_to_add), dtype=probs.dtype)" + cr +
				"probs = np.append(probs, z, axis=1)" + cr +
//				"print(X)" + cr +
//				"print(classes)" + cr +
//				"print(probs)" + cr +
//				"print('saving model output')" + cr +
				"output = ','.join([str(p) for p in probs[0]])" + cr +
				"print('@' + output + '@')" + cr +
//				"df_probs = pd.DataFrame(probs)" + cr +
//				"df_probs.to_csv('" + path + outpExt + "', header=False, index=False)" + cr +			
				"print('#')" + cr
			;
			bfwtr.write(cmdStr);
			bfwtr.flush(); // needed to execute the above commands
							
			int c = -1;
			String outp = "";
			char prev = '#';
			boolean addToOutp = false;
			while((c = bfrdr.read()) != '#') {
//				System.out.print((char)c);
				// If previous char is begin marker: start adding
				if (prev == '@') {
					addToOutp = true;
				}
				// If char is end marker: stop adding
				if (c == '@' && addToOutp) {
					addToOutp = false;
					// Prevent further adding by resetting prev
					prev = '#';
				}
				else {
					prev = (char) c;
				}
				if (addToOutp) {
					outp += (char)c;
				}
			}
				
			String[] indiv = outp.split(",");
			for (int i = 0; i < indiv.length; i++) {
				output[i] = Double.parseDouble(indiv[i]);
			}
//			System.out.println(cmdStr);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}


	/**
	 * Gets the imports from a Python module (.py file).
	 * 
	 * @param f
	 * @return
	 */
	public static String getImports(File f) {
		String imports = "";
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("import") || line.startsWith("from")) {
					imports += line + "\r\n";
				}
				else {
					break;
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return imports;
	}


	private static void test() {
		try {
			Runtime rt = Runtime.getRuntime();		
			Process proc = rt.exec("ipython");

			BufferedReader bfrdr = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			// Stop reading after 'In [1]: ' 
			int c = -1;
			char prePrev = '#';
			char prev = '#';
			String line = null;
			while ((c = bfrdr.read()) != -1) {
				System.out.print((char)c);
				if (c == ' ' && prev == ':' && prePrev == ']') {
					break;
				}
				prePrev = prev;
				prev = (char)c;
			}

			// Writer writes to terminal (i.e., to process)
			BufferedWriter bfwtr = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));

			String[] cmds = new String[]{ 
				"import numpy as np",
				"import pandas as pd",
				"x = [1.01, 1.02, 1.03]",
				"y = [2.01, 2.02, 2.03]",
				"for i in x: print(i)",
				"print(type(x))",
				"print(type(y))",
				"z = np.array([x, y])",
				"df = pd.DataFrame(z)",
				"df.to_csv('C:/Users/Reinier/Desktop/foddel.csv', header=False, index=False)",
				"print('stuff saved')",
				"print('#')"
			};
			for (String s : cmds) {
				bfwtr.write(s + "\r\n");
			}
			bfwtr.flush();

			line = null;
			while (!(line = bfrdr.readLine()).endsWith("#")) {
				System.out.println(line);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}


	private static void initPython(String[] cmd) {
		String path = cmd[0];
		String model = cmd[1];
		String ext = cmd[2];
		try {
			// Create runtime to execute external command
			Runtime rt = Runtime.getRuntime();		
			Process proc = rt.exec("ipython");
			
			// Writer writes to terminal (i.e., to process)
			// Reader reads from terminal answer
			// problem w/ reader: we don't know when it stops
			bfrdr = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			// Stop reading after 'In [1]: ' 
			int c = -1;
			char prePrev = '#';
			char prev = '#';
			while ((c = bfrdr.read()) != -1) {
				System.out.print((char)c);
				if (c == ' ' && prev == ':' && prePrev == ']') {
					break;
				}
				prePrev = prev;
				prev = (char)c;
			}
			
			bfwtr = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));

//			bfw.write("%cd C:/Users/Reinier/Desktop/ISMIR2017/" + cr);
//			bfw.write("pwd\r\n");
//			bfw.write("import init\r\n");

//			bfw.write("import numpy as np\r\n");
			
			// Import everything
			String[] imports = new String[]{ 
				"import os" + "\r\n",
				"from stat import *",
				"from sys import argv",
				"from sklearn.linear_model import LogisticRegression",
				"from sklearn.externals import joblib",
				"import numpy as np",
				"import pandas as pd"
			};
			for (String s : imports) {
				bfwtr.write(s + "\r\n");
			}
			
			// Init model
			bfwtr.write("m = joblib.load('" + path + model + ".pkl')" + "\r\n");
							
			String[] cmds = new String[]{
//				"m = LogisticRegression()",
//				"m = joblib.load('F:/research/data/experiments/ISMIR-2017/intabulations/3vv/LR/fwd/LR.pkl')",	
//				"m = joblib.load('" + path + model + ".pkl')",					
				"X = np.loadtxt('" + path + ext + "', delimiter=\",\")",
//				"X = np.loadtxt('F:/research/data/experiments/ISMIR-2017/intabulations/3vv/LR/fwd/fv_train.csv', delimiter=\",\")",
//				"y = np.loadtxt('F:/research/data/experiments/ISMIR-2017/intabulations/3vv/LR/fwd/cl_train.csv', delimiter=\",\")",
//				"m.fit(X[:120],y[:120])",
//				"classes = m.predict(X)",
//				"probs = m.predict_proba(X)",
			};
//			for (String s : cmds) {
//				bfw.write(s + cr);
//			}

//			bfw.write("probs = m.predict_proba(X[120:])" + cr);

//			bfw.write("\r\n");
//			bfw.write("\r\n");
//			bfw.write("print('#')\r\n");
//			bfw.flush();

//			while((c = bfr.read()) != '#') {
//				System.out.print((char)c);
//			}
			
			// Retrieve output from Python script
//			bfr = new BufferedReader(new InputStreamReader(pr.getInputStream()));
//			while((c = bfr.read()) != -1) {
//				System.out.print((char)c);
//			}
			
//			// Retrieve error (if any) from Python script. See Listing 4.3 at 
//			// http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html
////			InputStream stderr = pr.getErrorStream();
////			InputStreamReader isr = new InputStreamReader(stderr);
//			BufferedReader br = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
//			line = null;
////			System.out.println("<ERROR>");
//			boolean errorFound = false;
//			while ( (line = br.readLine()) != null) {
//				errorFound = true;
//				System.out.println(line);
//			}
////			System.out.println("</ERROR>");
//			if (errorFound) { 
//				int exitVal = pr.waitFor();
//				System.out.println("Process exitValue: " + exitVal);
//			}
			System.out.println("Python started.");

			bfwtr.write("exit()\r\n");
			bfwtr.flush();

			while((c = bfrdr.read()) != '#') {
				System.out.print((char)c);
			}
//			System.out.println("Python exited.");
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}


}

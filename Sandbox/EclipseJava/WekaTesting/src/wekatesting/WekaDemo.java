package wekatesting;



public class WekaDemo { 
	
	public static void main(String[] args) throws Exception{
	
		String rootPathmodel="C:\\Users\\LG\\Documents\\GitHub\\MDP\\Sandbox\\EclipseJava\\WekaTesting\\J48modelLocationWide.model"; 
		String rootPathtest ="LocationWidetest.arff";
		
    for(int i =0;i<50;i++)
		WekaMethods.PrintPredictedDistribution(rootPathmodel,rootPathtest,i);
	

	}
	
 
}



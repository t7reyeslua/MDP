package wekatesting;


import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by t7 on 20-10-14.
 */
public class WekaSensorsRawDataObject {


    private ArrayList<String> mSensorReadings = new ArrayList<String>();

    public WekaSensorsRawDataObject() {
    }

    public WekaSensorsRawDataObject(ArrayList<String> sensorReadings) {
        mSensorReadings = sensorReadings;
    }

    public ArrayList<String> getSensorReadings() {
        return mSensorReadings;
    }

    public void setSensorReadings(ArrayList<String> sensorReadings) {
        mSensorReadings = sensorReadings;
    }

    public HashMap<String, ArrayList<Double>> getSensorsArrays() {
        return mSensorsArrays;
    }

    public void setSensorsArrays(HashMap<String, ArrayList<Double>> sensorsArrays) {
        mSensorsArrays = sensorsArrays;
    }


    public void buildSensorArrays(){
        for (String record : mSensorReadings){
            separateBySensors(record);
        }
    }

    private HashMap<String, ArrayList<Double>> mSensorsArrays = new HashMap<String, ArrayList<Double>>();
    public void addValueToSensorArray(String sensorName, Double value){
        if(!mSensorsArrays.containsKey(sensorName)){
            ArrayList<Double> array = new ArrayList<Double>();
            mSensorsArrays.put(sensorName, array);
        }
        ArrayList<Double> current = mSensorsArrays.get(sensorName);
        current.add(value);
        mSensorsArrays.put(sensorName, current);
    }



    public void separateBySensors(String record){
        String[] parts = record.split("\t");

        for ( int i = 0; i < parts.length; i++){
            String sensorName = getSensorName(i);
            addValueToSensorArray(sensorName, Double.valueOf(parts[i]));
        }
    }

    public String getSensorName(int i){
        String sensorName = "";

        switch (i){
        
            case 0:
                sensorName = Constants.SENSOR_LINE;
                break;
            case 1:
                sensorName = Constants.SENSOR_TIME;
                break;
            case 2:
                sensorName = Constants.SENSOR_ACCX;
                break;
            case 3:
                sensorName = Constants.SENSOR_ACCY;
                break;
            case 4:
                sensorName = Constants.SENSOR_ACCZ;
                break;
            case 5:
                sensorName = Constants.SENSOR_GYROX;
                break;
            case 6:
                sensorName = Constants.SENSOR_GYROY;
                break;
            case 7:
                sensorName = Constants.SENSOR_GYROZ;
                break;
            case 8:
                sensorName = Constants.SENSOR_MAGX;
                break;
            case 9:
                sensorName = Constants.SENSOR_MAGY;
                break;
            case 10:
                sensorName = Constants.SENSOR_MAGZ;
                break;
            case 11:
                sensorName = Constants.SENSOR_LACCX;
                break;
            case 12:
                sensorName = Constants.SENSOR_LACCY;
                break;
            case 13:
                sensorName = Constants.SENSOR_LACCZ;
                break;
            case 14:
                sensorName = Constants.SENSOR_TILTX;
                break;
            case 15:
                sensorName = Constants.SENSOR_TILTY;
                break;
            case 16:
                sensorName = Constants.SENSOR_TILTZ;
                break;
            case 17:
                sensorName = Constants.SENSOR_ROTX;
                break;
            case 18:
                sensorName = Constants.SENSOR_ROTY;
                break;
            case 19:
                sensorName = Constants.SENSOR_ROTZ;
                break;     
                
            default:
                break;
        }


        return sensorName;
    }
    
	/**
	 * @author Luis Gonzalez
	 * @version 1, 29/10/14
	 * 
	 * @brief it returns a string (line) with all the features for motion sensors
	 */
    public String getFeatures(int nSamples){
    	String features="";
    	String Precision=Constants.FT_PRESICION;
    	ArrayList<Double> tArray = new ArrayList<Double>();
    	ArrayList<Double> xArray = new ArrayList<Double>();
    	ArrayList<Double> yArray = new ArrayList<Double>();
    	ArrayList<Double> zArray = new ArrayList<Double>();
    	ArrayList<Double> xFilArray = new ArrayList<Double>();
    	ArrayList<Double> yFilArray = new ArrayList<Double>();
    	ArrayList<Double> zFilArray = new ArrayList<Double>();
    	double UL=Constants.STEP_UP_THRESHOLD;
    	double LL=Constants.STEP_DOWN_THRESHOLD;
    	double SampleFreq=0;
    	double x,y,z;
    	
    	tArray=mSensorsArrays.get(Constants.SENSOR_TIME);
    	SampleFreq=SensorFeatures.GetSampleFreq(tArray);
    	
    	
    	for(int i=2;i<mSensorsArrays.size()-2;i=i+3){

    		
    		
    		xArray=mSensorsArrays.get(getSensorName(i));
    		yArray=mSensorsArrays.get(getSensorName(i+1));
    		zArray=mSensorsArrays.get(getSensorName(i+2));
    		
    		int tmpsize=xArray.size()-nSamples;
    		
    		//TODO make possible to change the sample number nSamples,

    		for(int r=1;r<=tmpsize;r++){
    			xArray.remove(xArray.size()-r);
    			yArray.remove(xArray.size()-r);
    			zArray.remove(xArray.size()-r);
    		}
    		
    		//Time domain fts
    		x = SensorFeatures.Mean(xArray);
    		y = SensorFeatures.Mean(yArray);
    		z = SensorFeatures.Mean(zArray);
    		
//    		for(int p=0;p<5;p++){
//    			System.out.println(p+ " index "+xArray.get(p));
//    		}
//    		
//    		System.out.println(x);
//    		System.out.println(y);
//    		System.out.println(z);
    		
    		features = features+String.format(Precision,x)+",";
    		features = features+String.format(Precision,y)+",";
    		features = features+String.format(Precision,z)+",";
    		features = features+String.format(Precision,SensorFeatures.Magnitud(x,y,z))+",";
    		
    		x = SensorFeatures.StandardDeviation(xArray);
    		y = SensorFeatures.StandardDeviation(yArray);
    		z = SensorFeatures.StandardDeviation(zArray);
  		
    		features = features+String.format(Precision,x)+",";
    		features = features+String.format(Precision,y)+",";
    		features = features+String.format(Precision,z)+",";
    		features = features+String.format(Precision,SensorFeatures.Magnitud(x,y,z))+","; 		
    		
    		x = SensorFeatures.Variance(xArray);
    		y = SensorFeatures.Variance(yArray);
    		z = SensorFeatures.Variance(zArray);
  		
    		features = features+String.format(Precision,x)+",";
    		features = features+String.format(Precision,y)+",";
    		features = features+String.format(Precision,z)+",";
    		features = features+String.format(Precision,SensorFeatures.Magnitud(x,y,z))+",";   		

    		//fre. domain fts.
    		features = features+String.format(Precision,SensorFeatures.FirstComponentFFT(xArray, SampleFreq))+",";
    		features = features+String.format(Precision,SensorFeatures.FirstComponentFFT(yArray, SampleFreq))+",";
    		features = features+String.format(Precision,SensorFeatures.FirstComponentFFT(zArray, SampleFreq))+",";

    		features = features+String.valueOf(SensorFeatures.ZeroXing(SensorFeatures.ZeroNormal(xArray)))+",";
    		features = features+String.valueOf(SensorFeatures.ZeroXing(SensorFeatures.ZeroNormal(yArray)))+",";
    		features = features+String.valueOf(SensorFeatures.ZeroXing(SensorFeatures.ZeroNormal(zArray)))+",";
    		
    		/**
    		 * 
    		 * Maybe, maybe is computationally too much
    		 * 
    		 * **/
    		xFilArray =XCounter.ButtFilterArray(SensorFeatures.ZeroNormal(xArray),2,5.333,SampleFreq);
    		yFilArray =XCounter.ButtFilterArray(SensorFeatures.ZeroNormal(yArray),2,5.333,SampleFreq);
    		zFilArray =XCounter.ButtFilterArray(SensorFeatures.ZeroNormal(zArray),2,5.333,SampleFreq);
    		
    		//**This might need to be filtered!!!**/
    		features = features+String.valueOf(XCounter.stepCounter(xFilArray,UL,LL))+",";
    		features = features+String.valueOf(XCounter.stepCounter(yFilArray,UL,LL))+",";
    		features = features+String.valueOf(XCounter.stepCounter(zFilArray,UL,LL))+",";

    		
    		
    	}

    	
    	return features;
    } 
    
	/**
	 * @author Luis Gonzalez
	 * @version 1, 30/10/14
	 * 
	 * @brief it returns a Arraylist of strings (line) with all attributes for printing
	 */
    public ArrayList<String> getAttributes(){
    	ArrayList<String> AttributesList = new ArrayList<String>();
    	

        
//    	for(int i=2;i<mSensorsArrays.size()-2;i=i+3){
    		for(int i=2;i<18;i++){
    		
    		String[] sensornameX = getSensorName(i).split("_");
    		String[] sensornameY = getSensorName(i+1).split("_");
    		String[] sensornameZ = getSensorName(i+2).split("_");
    		
    		AttributesList.add("@attribute mean_"+sensornameX[1]+sensornameX[2]+" numeric");
    		AttributesList.add("@attribute mean_"+sensornameY[1]+sensornameX[2]+" numeric");
    		AttributesList.add("@attribute mean_"+sensornameZ[1]+sensornameX[2]+" numeric");
    		AttributesList.add("@attribute meanMag_"+sensornameX[2]+" numeric");
    		
    		AttributesList.add("@attribute std_"+sensornameX[1]+sensornameX[2]+" numeric");
    		AttributesList.add("@attribute std_"+sensornameY[1]+sensornameX[2]+" numeric");
    		AttributesList.add("@attribute std_"+sensornameZ[1]+sensornameX[2]+" numeric");   		
    		AttributesList.add("@attribute stdMag_"+sensornameX[2]+" numeric");
    		
    		AttributesList.add("@attribute var_"+sensornameX[1]+sensornameX[2]+" numeric");
    		AttributesList.add("@attribute var_"+sensornameY[1]+sensornameX[2]+" numeric");
    		AttributesList.add("@attribute var_"+sensornameZ[1]+sensornameX[2]+" numeric");   		
    		AttributesList.add("@attribute varMag_"+sensornameX[2]+" numeric");
    		
    		AttributesList.add("@attribute FundF_"+sensornameX[1]+sensornameX[2]+" numeric");
    		AttributesList.add("@attribute FundF_"+sensornameY[1]+sensornameX[2]+" numeric");
    		AttributesList.add("@attribute FundF_"+sensornameZ[1]+sensornameX[2]+" numeric");   		

    		AttributesList.add("@attribute ZXing_"+sensornameX[1]+sensornameX[2]+" numeric");
    		AttributesList.add("@attribute ZXing_"+sensornameY[1]+sensornameX[2]+" numeric");
    		AttributesList.add("@attribute ZXing_"+sensornameZ[1]+sensornameX[2]+" numeric");  
    		
    		AttributesList.add("@attribute SCount_"+sensornameX[1]+sensornameX[2]+" numeric");
    		AttributesList.add("@attribute SCount_"+sensornameY[1]+sensornameX[2]+" numeric");
    		AttributesList.add("@attribute SCount_"+sensornameZ[1]+sensornameX[2]+" numeric");
    		
    	}
    	
    	
    	return AttributesList;
    }
}

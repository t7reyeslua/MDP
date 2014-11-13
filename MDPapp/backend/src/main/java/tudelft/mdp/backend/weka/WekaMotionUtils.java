package tudelft.mdp.backend.weka;

import java.util.ArrayList;

import tudelft.mdp.backend.enums.Constants;

public class WekaMotionUtils {

    public WekaMotionUtils() {
    }


    public static String getAxisName(int position){
        String sensorName = "";
        switch (position) {
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
     * @version 1, 30/10/14
     *
     * @brief it returns a Arraylist of strings (line) with all attributes for printing
     */
    public static ArrayList<String> getAttributes(){
        ArrayList<String> AttributesList = new ArrayList<String>();

        for(int i=2;i<(18)+2;i=i+3){
//    		for(int i=2;i<18;i++){

            String[] sensornameX = getAxisName(i).split("_");
            String[] sensornameY = getAxisName(i + 1).split("_");
            String[] sensornameZ = getAxisName(i + 2).split("_");

            AttributesList.add("@attribute mean_"+sensornameX[1]+sensornameX[2]+" numeric");
            AttributesList.add("@attribute mean_"+sensornameY[1]+sensornameY[2]+" numeric");
            AttributesList.add("@attribute mean_"+sensornameZ[1]+sensornameZ[2]+" numeric");
            AttributesList.add("@attribute meanMag_"+sensornameX[1]+sensornameX[2]+sensornameY[2]+sensornameZ[2]+" numeric");

            AttributesList.add("@attribute std_"+sensornameX[1]+sensornameX[2]+" numeric");
            AttributesList.add("@attribute std_"+sensornameY[1]+sensornameY[2]+" numeric");
            AttributesList.add("@attribute std_"+sensornameZ[1]+sensornameZ[2]+" numeric");
            AttributesList.add("@attribute stdMag_"+sensornameX[1]+sensornameX[2]+sensornameY[2]+sensornameZ[2]+" numeric");

            AttributesList.add("@attribute var_"+sensornameX[1]+sensornameX[2]+" numeric");
            AttributesList.add("@attribute var_"+sensornameY[1]+sensornameY[2]+" numeric");
            AttributesList.add("@attribute var_"+sensornameZ[1]+sensornameZ[2]+" numeric");
            AttributesList.add("@attribute varMag_"+sensornameX[1]+sensornameX[2]+sensornameY[2]+sensornameZ[2]+" numeric");

            AttributesList.add("@attribute FundF_"+sensornameX[1]+sensornameX[2]+" numeric");
            AttributesList.add("@attribute FundF_"+sensornameY[1]+sensornameY[2]+" numeric");
            AttributesList.add("@attribute FundF_"+sensornameZ[1]+sensornameZ[2]+" numeric");

            AttributesList.add("@attribute ZXing_"+sensornameX[1]+sensornameX[2]+" numeric");
            AttributesList.add("@attribute ZXing_"+sensornameY[1]+sensornameY[2]+" numeric");
            AttributesList.add("@attribute ZXing_"+sensornameZ[1]+sensornameZ[2]+" numeric");

            AttributesList.add("@attribute SCount_"+sensornameX[1]+sensornameX[2]+" numeric");
            AttributesList.add("@attribute SCount_"+sensornameY[1]+sensornameY[2]+" numeric");
            AttributesList.add("@attribute SCount_"+sensornameZ[1]+sensornameZ[2]+" numeric");

        }

        return AttributesList;
    }




}

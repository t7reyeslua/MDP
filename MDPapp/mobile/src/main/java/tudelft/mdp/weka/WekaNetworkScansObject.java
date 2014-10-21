package tudelft.mdp.weka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import tudelft.mdp.enums.Constants;
import tudelft.mdp.fileManagement.FileCreator;
import tudelft.mdp.locationTracker.NetworkInfoObject;
import tudelft.mdp.utils.Utils;

public class WekaNetworkScansObject {

    private ArrayList<ArrayList<NetworkInfoObject>> mNetworkScans = new ArrayList<ArrayList<NetworkInfoObject>>();
    private HashMap<String, ArrayList<Double>> mNetworksValues = new HashMap<String, ArrayList<Double>>();
    private HashMap<String, ArrayList<Double>> mNetworksFeatures = new HashMap<String, ArrayList<Double>>();

    public WekaNetworkScansObject() {
    }

    public WekaNetworkScansObject(
            ArrayList<ArrayList<NetworkInfoObject>> networkScans) {
        mNetworkScans = networkScans;
    }

    public ArrayList<ArrayList<NetworkInfoObject>> getNetworkScans() {
        return mNetworkScans;
    }

    public void setNetworkScans(ArrayList<ArrayList<NetworkInfoObject>> networkScans) {
        mNetworkScans = networkScans;
    }

    public void buildArraysByNetworks(){
        for (ArrayList<NetworkInfoObject> singleNetworkScan : mNetworkScans){
            for (NetworkInfoObject networkInfoFromScan : singleNetworkScan){
                ArrayList<Double> networkInfo = mNetworksValues.get(networkInfoFromScan.getBSSID());
                if ( networkInfo == null) {
                    networkInfo = new ArrayList<Double>();
                }
                networkInfo.add(networkInfoFromScan.getRSSI());
                mNetworksValues.put(networkInfoFromScan.getBSSID(), networkInfo);
            }
        }
    }


    public void buildNetworkFeatures(){
        buildArraysByNetworks();
        for (String networkBSSID : mNetworksValues.keySet()){
            ArrayList<Double> networkFeatures = new ArrayList<Double>();

            //Add mean
            networkFeatures.add(Utils.getMean(mNetworksValues.get(networkBSSID)));
            //Add std
            networkFeatures.add(Utils.getStd(mNetworksValues.get(networkBSSID)));
            //Add min
            networkFeatures.add(Collections.min(mNetworksValues.get(networkBSSID)));
            //Add max
            networkFeatures.add(Collections.max(mNetworksValues.get(networkBSSID)));

            mNetworksFeatures.put(networkBSSID, networkFeatures);

        }
    }


    public void saveToFile(String event){
        buildNetworkFeatures();

        if (mNetworksFeatures.size() == 0){
            return;
        }

        FileCreator mFileCreator = new FileCreator("LOCATION_" + event + "_", Constants.DIRECTORY_TRAINING);
        mFileCreator.openFileWriter();

        //Write Header
        String header = "";
        for (String networkBSSID : mNetworksFeatures.keySet()){
             header += networkBSSID + "_mean," +
                          networkBSSID + "_std,"  +
                          networkBSSID + "_min,"  +
                          networkBSSID + "_max,";
        }
        header = header.substring(0, header.length()-1);
        mFileCreator.saveData(header + "\n");


        String values = "";
        for (String networkBSSID : mNetworksFeatures.keySet()){

            ArrayList<Double> features = mNetworksFeatures.get(networkBSSID);
            for (Double feature : features){
                values += feature + ",";
            }

        }
        values = values.substring(0, header.length()-1);
        mFileCreator.saveData(values + "\n");

        mFileCreator.closeFileWriter();

    }




}

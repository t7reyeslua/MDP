package tudelft.mdp.ui;

import android.content.Context;

import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import tudelft.mdp.ui.DeviceCard;
import tudelft.mdp.ui.DeviceCardExpand;
import tudelft.mdp.ui.DeviceCardHeader;

/**
 * Class that wraps the functionality to build a Device UI card
 * with all the related information to the particular appliance that is
 * shown in the DeviceManager Fragment.
 */
public class DeviceCardBuilder {

    private Context mContext;
    private DeviceCard mDeviceCard;
    private DeviceCardHeader mDeviceCardHeader;
    private DeviceCardExpand mDeviceCardExpand;


    private String mDeviceTagId;
    private String mDeviceType;
    private String mDeviceDesc;
    private String mDeviceLocation;
    private Integer mCurrentUsers;

    private Double mTotalTime;
    private Double mUserTime;
    private Double mTotalPower;
    private Double mUserPower;

    private Double mPercentage;

    private Boolean hasExpandInfo = false;

    public DeviceCardBuilder(Context context) {
        mContext = context;
    }

    public DeviceCardBuilder(Context context, String deviceTagId, String deviceType, String deviceDesc,
            String deviceLocation, Integer currentUsers) {

        mContext = context;
        mDeviceTagId = deviceTagId;
        mDeviceType = deviceType;
        mDeviceDesc = deviceDesc;
        mDeviceLocation = deviceLocation;
        mCurrentUsers = currentUsers;
    }

    public DeviceCardBuilder(Context context, String deviceTagId, String deviceType, String deviceDesc,
            String deviceLocation, Integer currentUsers, Double totalTime, Double userTime,
            Double totalPower, Double userPower, Double percentage) {
        mContext = context;
        mDeviceTagId = deviceTagId;
        mDeviceType = deviceType;
        mDeviceDesc = deviceDesc;
        mDeviceLocation = deviceLocation;
        mCurrentUsers = currentUsers;
        mTotalTime = totalTime;
        mUserTime = userTime;
        mTotalPower = totalPower;
        mUserPower = userPower;
        mPercentage = percentage;

        hasExpandInfo = true;
    }

    public DeviceCard buildDeviceCard() {

        if (mDeviceDesc == null){
            mDeviceDesc = "";
        }

        Boolean activeDevice = false;
        if (mCurrentUsers > 0){
            activeDevice = true;
        }

        mDeviceCard = new DeviceCard(
                mContext,
                mDeviceTagId,
                mCurrentUsers.toString(),
                activeDevice);

        mDeviceCardHeader = new DeviceCardHeader(
                mContext,
                mDeviceType,
                mDeviceDesc + "@" + mDeviceLocation);


        mDeviceCardHeader.setButtonExpandVisible(true);
        mDeviceCard.addCardHeader(mDeviceCardHeader);
        mDeviceCard.setShadow(true);

        if (hasExpandInfo){
            buildDeviceExpand();
        }


        return mDeviceCard;
    }

    public void buildDeviceExpand(){

        if (!hasExpandInfo){
            return;
        }

        mDeviceCardExpand = new DeviceCardExpand(
                mContext,
                formatTime(mTotalTime),
                formatTime(mUserTime),
                mTotalPower.toString() + " kw",
                mUserPower.toString()  + " kw ",
                mPercentage.toString() + "%");

        mDeviceCard.addCardExpand(mDeviceCardExpand);
    }

    public void setExpand(Double totalTime, Double userTime,
            Double totalPower, Double userPower, Double percentage){

        mTotalTime = totalTime;
        mUserTime = userTime;
        mTotalPower = totalPower;
        mUserPower = userPower;
        mPercentage = percentage;

        hasExpandInfo = true;

    }

    private String formatTime(Double timeInSeconds){
        String result;

        int hours = (int) (timeInSeconds/3600);
        int minutes = (int ) ((timeInSeconds % 3600) / 60);
        int seconds = (int ) (timeInSeconds % 60);

        result = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return result;
    }


    public DeviceCard getDeviceCard() {
        return mDeviceCard;
    }

    public void setDeviceCard(DeviceCard deviceCard) {
        mDeviceCard = deviceCard;
    }

    public DeviceCardHeader getDeviceCardHeader() {
        return mDeviceCardHeader;
    }

    public void setDeviceCardHeader(DeviceCardHeader deviceCardHeader) {
        mDeviceCardHeader = deviceCardHeader;
    }

    public DeviceCardExpand getDeviceCardExpand() {
        return mDeviceCardExpand;
    }

    public void setDeviceCardExpand(DeviceCardExpand deviceCardExpand) {
        mDeviceCardExpand = deviceCardExpand;
    }

    public String getDeviceTagId() {
        return mDeviceTagId;
    }

    public void setDeviceTagId(String deviceTagId) {
        mDeviceTagId = deviceTagId;
    }

    public String getDeviceType() {
        return mDeviceType;
    }

    public void setDeviceType(String deviceType) {
        mDeviceType = deviceType;
    }

    public String getDeviceDesc() {
        return mDeviceDesc;
    }

    public void setDeviceDesc(String deviceDesc) {
        mDeviceDesc = deviceDesc;
    }

    public String getDeviceLocation() {
        return mDeviceLocation;
    }

    public void setDeviceLocation(String deviceLocation) {
        mDeviceLocation = deviceLocation;
    }

    public Integer getCurrentUsers() {
        return mCurrentUsers;
    }

    public void setCurrentUsers(Integer currentUsers) {
        mCurrentUsers = currentUsers;
    }

    public Double getTotalTime() {
        return mTotalTime;
    }

    public void setTotalTime(Double totalTime) {
        mTotalTime = totalTime;
    }

    public Double getUserTime() {
        return mUserTime;
    }

    public void setUserTime(Double userTime) {
        mUserTime = userTime;
    }

    public Double getTotalPower() {
        return mTotalPower;
    }

    public void setTotalPower(Double totalPower) {
        mTotalPower = totalPower;
    }

    public Double getUserPower() {
        return mUserPower;
    }

    public void setUserPower(Double userPower) {
        mUserPower = userPower;
    }

    public Double getPercentage() {
        return mPercentage;
    }

    public void setPercentage(Double percentage) {
        mPercentage = percentage;
    }
}

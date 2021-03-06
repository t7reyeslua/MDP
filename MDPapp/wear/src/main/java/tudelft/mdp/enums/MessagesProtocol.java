package tudelft.mdp.enums;

/**
 * Created by t7 on 9-10-14.
 */
public class MessagesProtocol {

    /* PATH URIs */
    public static final String DATAPATH = "/data_path";
    public static final String MSGPATH = "/message_path";
    public static final String FILEPATH = "/file_path";
    public static final String NOTIFICATIONPATH = "/notification_path";

    /* Notifications */
    public static final String NOTIFICATIONTITLE = "NOTIFICATIONTITLE";
    public static final String NOTIFICATIONCONTENT = "NOTIFICATIONCONTENT";
    public static final String NOTIFICATIONTIMESTAMP = "NOTIFICATIONTIMESTAMP";
    public static final String NOTIFICATIONCOMMAND = "NOTIFICATIONCOMMAND";

    public static final String STARTSENSINGSERVICE = "START SENSING SERVICE";
    public static final String STOPSENSINGSERVICE = "STOP SENSING SERVICE";
    public static final String TIMESTAMP = "TIMESTAMP";
    public static final String SENSOR_RECORDING_SECONDS = "SENSOR_RECORDING_SECONDS";

    /* SENDER/RECEIVER */
    public static final String SENDER = "SENDER";
    public static final String RECEIVER = "RECEIVER";
    public static final int ID_MOBILE = 1;
    public static final int ID_WEAR = 2;


    /* MSG TYPES*/
    public static final String MSGTYPE = "MSGTYPE";
    //Data Bundle
    public static final int SENSOREVENT = 1;
    public static final int SNDMESSAGE = 2;
    public static final int STARTSENSING = 0;
    public static final int STOPSENSING = 1;
    public static final int KILLSERVICE = 2;
    public static final int QUERYSENSORLIST = 3;
    public static final int QUERYSENSORLISTRESPONSE = 4;
    //Messages
    public static final int SENDSENSEORSNAPSHOTREC = 1000;
    public static final int SENDSENSEORSNAPSHOTREC_START  = 1001;
    public static final int SENDSENSEORSNAPSHOTREC_FINISH = 1002;
    public static final int SENDSENSEORSNAPSHOTUPDATE = 1003;
    public static final int SENDSENSEORSNAPSHOTHEADER = 1004;
    public static final int SENDSENSEORSNAPSHOT_END = 1005;

    /* MSG Payloads */
    //Data Bundle
    public static final String SENSORTYPE = "SENSORTYPE";
    public static final String SENSORVALUE = "SENSORVALUE";
    public static final String MESSAGE = "MESSAGE";
    public static final String RECORDEDSENSORS = "RECORDEDSENSORS";
    public static final String SENSORLISTRESULT = "SENSORLISTRESULT";
    public static final String SENSORHZ = "SENSORHZ";
    public static final String SENSORSCONSOLIDATED = "SENSORSCONSOLIDATED";
    public static final String SENSORSTORECORD = "SENSORSTORECORD";

    /* Intent Action Filters */
    public static final String WEARSENSORSMSG = "WEARSENSORSMSG";
    public static final String WEARSENSORSBUNDLE = "WEARSENSORSBUNDLE";
}

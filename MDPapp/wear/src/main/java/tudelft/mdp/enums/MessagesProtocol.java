package tudelft.mdp.enums;

/**
 * Created by t7 on 9-10-14.
 */
public class MessagesProtocol {

    public static final String DATAPATH = "/data_path";
    public static final String MSGPATH = "/message_path";

    public static final String TIMESTAMP = "TIMESTAMP";

    public static final String SENDER = "SENDER";
    public static final String RECEIVER = "RECEIVER";
    public static final int ID_MOBILE = 1;
    public static final int ID_WEAR = 2;


    public static final String MSGTYPE = "MSGTYPE";
    public static final int SENSOREVENT = 1;
    public static final int SNDMESSAGE = 2;


    public static final String SENSORTYPE = "SENSORTYPE";
    public static final String SENSORVALUE = "SENSORVALUE";
    public static final String MESSAGE = "MESSAGE";

    public static final String WEARSENSORSMSG = "WEARSENSORSMSG";
    public static final String WEARSENSORSBUNDLE = "WEARSENSORSBUNDLE";

    public static final int STARTSENSING = 0;
    public static final int STOPSENSING = 1;

}

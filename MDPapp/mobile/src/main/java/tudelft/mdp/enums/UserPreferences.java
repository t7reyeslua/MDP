package tudelft.mdp.enums;

/**
 * Created by t7 on 28-9-14.
 */
public class UserPreferences {

    public static final String USERNAME = "USERNAME";
    public static final String LOCATIONMODE = "LOCATIONMODE";
    public static final String USERID = "USERID";
    public static final String USERMAIL = "USEREMAIL";
    public static final String USERPIC = "USERPIC";
    public static final String WEARCONNECTED = "WEARCONNECTED";

    public static final String TESTMODE = "TESTMODE";
    public static final String TESTPLACE = "TESTPLACE";
    public static final String TESTZONE = "TESTZONE";
    public static final String TESTMOTION = "TESTMOTION";
    public static final String TESTEVENT = "TESTEVENT";
    public static final String TESTMOTION_USE = "TESTMOTION_USE";


    public static final String CALIBRATED = "CALIBRATED";
    public static final String CALIBRATION_M = "CALIBRATION_M";
    public static final String CALIBRATION_B = "CALIBRATION_B";
    public static final String CALIBRATION_M_PREF = "CALIBRATION_M_PREF";
    public static final String CALIBRATION_B_PREF = "CALIBRATION_B_PREF";
    public static final String CALIBRATION_SCANS = "CALIBRATION_SCANS";
    public static final String TRAINING_PHASE = "TRAINING_PHASE";
    public static final String LOCATION_TECHNIQUE = "LOCATION_TECHNIQUE";
    public static final String TIME_BETWEEN_LOCATION_DETECTIONS = "TIME_BETWEEN_LOCATION_DETECTIONS";
    public static final String ALPHA_TRIMMER_COEFFICIENT = "ALPHA_TRIMMER_COEFFICIENT";
    public static final String MOTION_SAMPLE_SECONDS = "MOTION_SAMPLE_SECONDS";
    public static final String TARGET_KWH_GROUP = "TARGET_KWH_GROUP";
    public static final String TARGET_KWH_INDIVIDUAL = "TARGET_KWH_INDIVIDUAL";

    public static final String SCANSAMPLES = "SCANS_PER_SAMPLE";

    public static int SCANWINDOW = 30;
    public static int FINGERPRINT_SAMPLES = 50;

    public static final Integer TODAY = 0;
    public static final Integer WEEK = 1;
    public static final Integer MONTH = 2;
    public static final Integer YEAR = 3;
    public static final Integer ALLTIME = 4;

    public static double ALPHA_TRIMMER_COEFF_VALUE = 0.2;
    public static int CALIBRATION_NUM_SCANS = 10;



}

package tudelft.mdp.backend;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

import tudelft.mdp.backend.records.ApGaussianRecord;
import tudelft.mdp.backend.records.ApHistogramRecord;
import tudelft.mdp.backend.records.DeviceMotionLocationRecord;
import tudelft.mdp.backend.records.DeviceUsageRecord;
import tudelft.mdp.backend.records.EnergyConsumptionRecord;
import tudelft.mdp.backend.records.LocationFingerprintRecord;
import tudelft.mdp.backend.records.LocationFingerprintRecordWrapper;
import tudelft.mdp.backend.records.LocationLogRecord;
import tudelft.mdp.backend.records.NfcLogRecord;
import tudelft.mdp.backend.records.NfcRecord;
import tudelft.mdp.backend.records.RegistrationRecord;
import tudelft.mdp.backend.records.SensorFingerprintRecord;
import tudelft.mdp.backend.records.SensorFingerprintRecordWrapper;
import tudelft.mdp.backend.records.WekaObjectRecord;

/**
 * Objectify service wrapper so we can statically register our persistence classes
 * More on Objectify here : https://code.google.com/p/objectify-appengine/
 *
 */
public class OfyService {

    static {
        ObjectifyService.register(RegistrationRecord.class);
        ObjectifyService.register(NfcRecord.class);
        ObjectifyService.register(NfcLogRecord.class);
        ObjectifyService.register(ApGaussianRecord.class);
        ObjectifyService.register(ApHistogramRecord.class);
        ObjectifyService.register(LocationLogRecord.class);
        ObjectifyService.register(LocationFingerprintRecord.class);
        ObjectifyService.register(DeviceMotionLocationRecord.class);
        ObjectifyService.register(EnergyConsumptionRecord.class);
        ObjectifyService.register(DeviceUsageRecord.class);
        ObjectifyService.register(WekaObjectRecord.class);
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
}

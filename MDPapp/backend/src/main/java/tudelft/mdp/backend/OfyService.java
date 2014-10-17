package tudelft.mdp.backend;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

import tudelft.mdp.backend.records.ApGaussianRecord;
import tudelft.mdp.backend.records.ApHistogramRecord;
import tudelft.mdp.backend.records.LocationFingerprintRecord;
import tudelft.mdp.backend.records.LocationLogRecord;
import tudelft.mdp.backend.records.NfcLogRecord;
import tudelft.mdp.backend.records.NfcRecord;
import tudelft.mdp.backend.records.RegistrationRecord;

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
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
}

/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Backend with Google Cloud Messaging" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/GcmEndpoints
*/

package tudelft.mdp.backend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;

import java.util.List;
import java.util.logging.Logger;
import javax.inject.Named;

import tudelft.mdp.backend.records.RegistrationRecord;

import static tudelft.mdp.backend.OfyService.ofy;

/**
 * A registration endpoint class we are exposing for a device's GCM registration id on the backend
 *
 * For more information, see
 * https://developers.google.com/appengine/docs/java/endpoints/
 *
 * NOTE: This endpoint does not use any form of authorization or
 * authentication! If this app is deployed, anyone can access this endpoint! If
 * you'd like to add authentication, take a look at the documentation.
 */
@Api(name = "registration", description = "An API to manage the device GCM registration", version = "v1", namespace = @ApiNamespace(ownerDomain = "endpoints.backend.mdp.tudelft", ownerName = "endpoints.backend.mdp.tudelft", packagePath=""))
public class RegistrationEndpoint {

    private static final Logger log = Logger.getLogger(RegistrationEndpoint.class.getName());

    /**
     * Register a device to the backend
     *
     */
    @ApiMethod(name = "register")
    public void registerDevice(RegistrationRecord record) {
        if(findRecord(record.getRegId()) != null) {
            log.info("Device " + record.getRegId() + " already registered, skipping register");
            return;
        }

        ofy().save().entity(record).now();
    }

    /**
     * Unregister a device from the backend
     *
     * @param regId The Google Cloud Messaging registration Id to remove
     */
    @ApiMethod(name = "unregister")
    public void unregisterDevice(@Named("regId") String regId) {
        RegistrationRecord record = findRecord(regId);
        if(record == null) {
            log.info("Device " + regId + " not registered, skipping unregister");
            return;
        }
        ofy().delete().entity(record).now();

    }

    /**
     * Return a collection of registered devices
     *
     * @param count The number of devices to list
     * @return a list of Google Cloud Messaging registration Ids
     */
    @ApiMethod(name = "listDevices")
    public CollectionResponse<RegistrationRecord> listDevices(@Named("count") int count) {
        List<RegistrationRecord> records = ofy().load().type(RegistrationRecord.class).limit(count).list();
        return CollectionResponse.<RegistrationRecord>builder().setItems(records).build();
    }

    private RegistrationRecord findRecord(String regId) {
        return ofy().load().type(RegistrationRecord.class).filter("regId", regId).first().now();
    }

}

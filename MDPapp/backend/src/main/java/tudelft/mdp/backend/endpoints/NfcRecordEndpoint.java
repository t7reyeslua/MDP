package tudelft.mdp.backend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.ReadPolicy;

import com.googlecode.objectify.Work;

import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import tudelft.mdp.backend.records.NfcRecord;

import static tudelft.mdp.backend.OfyService.ofy;

/** An endpoint class we are exposing */
@Api(name = "deviceEndpoint", description = "An API to manage the devices (NFC tags)", version = "v1", namespace = @ApiNamespace(ownerDomain = "endpoints.backend.mdp.tudelft", ownerName = "endpoints.backend.mdp.tudelft", packagePath=""))
public class NfcRecordEndpoint {


    private static final Logger LOG = Logger.getLogger(NfcRecordEndpoint.class.getName());

    /**
     * This method gets the <code>NfcRecord</code> object associated with the specified <code>id</code>.
     * @param id The id of the object to be returned.
     * @return The <code>NfcRecord</code> associated with <code>id</code>.
     */
    @ApiMethod(name = "getDevice", path = "get_device")
    public NfcRecord getNFC(@Named("id") String id) throws NotFoundException {

        LOG.info("Calling getNFC method");
        NfcRecord record = findRecord(id);
        if(record == null) {
            throw new NotFoundException("NFC Record does not exist. ");
        }

        ofy().clear();
        return record;
    }

    /**
     * This inserts a new <code>NfcRecord</code> object.
     * @param nfcRecord The object to be added.
     * @return The object to be added.
     */
    @ApiMethod(name = "insertDevice")
    public NfcRecord insertNFC(NfcRecord nfcRecord) throws ConflictException {
        // Implement this function

        LOG.info("Calling insertNFC method");
        //If if is not null, then check if it exists. If yes, throw an Exception
        //that it is already present
        String id = nfcRecord.getNfcId();
        if (id != null) {
            if (findRecord(id) != null) {
                throw new ConflictException("Object already exists");
            }
        }
        nfcRecord.setState(0);
        //Since our @Id field is a Long, Objectify will generate a unique value for us
        //when we use put
        ofy().save().entity(nfcRecord).now();
        return nfcRecord;
    }

    /**
     * This updates an existing <code>NfcRecord</code> object.
     * @param nfcRecord The object to be added.
     * @return The object to be updated.
     */
    @ApiMethod(name = "updateDevice")
    public NfcRecord updateNFC(NfcRecord nfcRecord)throws NotFoundException {

        LOG.info("Calling updateNFC method");
        if (findRecord(nfcRecord.getNfcId()) == null) {
            throw new NotFoundException("NFC Record does not exist");
        }
        ofy().save().entity(nfcRecord).now();
        return nfcRecord;
    }

    /**
     * This increases by 1 the total count of users making use of of an existing <code>NfcRecord</code> object.
     * @param id The object to be updated.
     * @return The object to be updated.
     */
    @ApiMethod(name = "increaseDeviceUsers")
    public NfcRecord increaseNFC(@Named("id") String id)throws NotFoundException {

        LOG.info("Calling increaseNFC method");

        final String recordId = id;
        NfcRecord nfcRecord = ofy().transact(new Work<NfcRecord>() {
              @Override
              public NfcRecord run() {
                  NfcRecord nr = findRecord(recordId);
                  if (nr == null){
                      return null;
                  }

                  LOG.info("Increasing. Old value: " + nr.getState());
                  nr.setState(nr.getState()+1);
                  LOG.info("Increasing. New value: " + nr.getState());
                  ofy().consistency(ReadPolicy.Consistency.STRONG).save().entity(nr).now();

                  return nr;
              }
          }
        );

        /*
        NfcRecord nfcRecord = findRecord(id);
        if (findRecord(id) == null) {
            throw new NotFoundException("NFC Record does not exist");
        }
        LOG.info("Increasing. Old value: " + nfcRecord.getState());
        nfcRecord.setState(nfcRecord.getState()+1);
        LOG.info("Increasing. New value: " + nfcRecord.getState());
        ofy().consistency(ReadPolicy.Consistency.STRONG).save().entity(nfcRecord).now();*/

        return nfcRecord;
    }

    /**
     * This decreases by 1 the total count of users making use of of an existing <code>NfcRecord</code> object.
     * @param id The object to be updated.
     * @return The object to be updated.
     */
    @ApiMethod(name = "decreaseDeviceUsers")
    public NfcRecord decreaseNFC(@Named("id") String id)throws NotFoundException {

        LOG.info("Calling decreaseNFC method");

        final String recordId = id;
        NfcRecord nfcRecord = ofy().transact(new Work<NfcRecord>() {
                                                 @Override
                                                 public NfcRecord run() {
                                                     NfcRecord nr = findRecord(recordId);
                                                     if (nr == null){
                                                         return null;
                                                     }

                                                     LOG.info("Decreasing. Old value: " + nr.getState());
                                                     nr.setState(nr.getState()-1);
                                                     LOG.info("Decreasing. New value: " + nr.getState());
                                                     ofy().consistency(ReadPolicy.Consistency.STRONG).save().entity(nr).now();

                                                     return nr;
                                                 }
                                             }
        );

        /*
        NfcRecord nfcRecord = findRecord(id);
        if (findRecord(id) == null) {
            throw new NotFoundException("NFC Record does not exist");
        }

        LOG.info("Decreasing. Old value: " + nfcRecord.getState());
        nfcRecord.setState(nfcRecord.getState()-1);
        LOG.info("Decreasing. New value: " + nfcRecord.getState());

        ofy().consistency(ReadPolicy.Consistency.STRONG).save().entity(nfcRecord).now();*/

        return nfcRecord;
    }

    /**
     * This deletes an existing <code>NfcRecord</code> object.
     * @param id The id of the object to be deleted.
     */
    @ApiMethod(name = "deleteDevice")
    public void deleteNFC(@Named("id") String id) throws NotFoundException {

        LOG.info("Calling deleteNFC method");
        NfcRecord record = findRecord(id);
        if(record == null) {
            throw new NotFoundException("NFC Record does not exist");
        }
        ofy().delete().entity(record).now();
    }

    /**
     * Return a collection of registered NFC tags
     *
     * @param count The number of NFC to list
     * @return a list of NFC registered tags
     */
    @ApiMethod(name = "listDevices")
    public CollectionResponse<NfcRecord> listNFC(@Named("count") int count) {
        List<NfcRecord> records = ofy().load().type(NfcRecord.class).limit(count).list();
        return CollectionResponse.<NfcRecord>builder().setItems(records).build();
    }

    private NfcRecord findRecord(String id) {
        return ofy().load().type(NfcRecord.class).id(id).now();
    }
}
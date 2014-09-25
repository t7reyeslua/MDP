package tudelft.mdp.backend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.NotFoundException;

import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import tudelft.mdp.backend.records.NfcRecord;

import static tudelft.mdp.backend.OfyService.ofy;

/** An endpoint class we are exposing */
@Api(name = "nfcRecordEndpoint", description = "An API to manage the NFC tags", version = "v1", namespace = @ApiNamespace(ownerDomain = "endpoints.backend.mdp.tudelft", ownerName = "endpoints.backend.mdp.tudelft", packagePath=""))
public class NfcRecordEndpoint {


    private static final Logger LOG = Logger.getLogger(NfcRecordEndpoint.class.getName());

    /**
     * This method gets the <code>NfcRecord</code> object associated with the specified <code>id</code>.
     * @param id The id of the object to be returned.
     * @return The <code>NfcRecord</code> associated with <code>id</code>.
     */
    @ApiMethod(name = "getNFC", path = "get_nfc")
    public NfcRecord getNFC(@Named("id") String id) throws NotFoundException {

        LOG.info("Calling getNFC method");
        NfcRecord record = findRecord(id);
        if(record == null) {
            throw new NotFoundException("NFC Record does not exist");
        }
        return record;
    }

    /**
     * This inserts a new <code>NfcRecord</code> object.
     * @param nfcRecord The object to be added.
     * @return The object to be added.
     */
    @ApiMethod(name = "insertNFC")
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
    @ApiMethod(name = "updateNFC")
    public NfcRecord updateNFC(NfcRecord nfcRecord)throws NotFoundException {

        LOG.info("Calling updateNFC method");
        if (findRecord(nfcRecord.getNfcId()) == null) {
            throw new NotFoundException("NFC Record does not exist");
        }
        ofy().save().entity(nfcRecord).now();
        return nfcRecord;
    }

    /**
     * This toggles the ON/OFF status of an existing <code>NfcRecord</code> object.
     * @param id The object to be updated.
     * @return The object to be updated.
     */
    @ApiMethod(name = "toggleNFC")
    public NfcRecord toggleNFC(@Named("id") String id)throws NotFoundException {

        LOG.info("Calling toggleNFC method");

        NfcRecord nfcRecord = findRecord(id);
        if (findRecord(id) == null) {
            throw new NotFoundException("NFC Record does not exist");
        }
        nfcRecord.setState(!nfcRecord.getState());
        ofy().save().entity(nfcRecord).now();
        return nfcRecord;
    }

    /**
     * This deletes an existing <code>NfcRecord</code> object.
     * @param id The id of the object to be deleted.
     */
    @ApiMethod(name = "deleteNFC")
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
    @ApiMethod(name = "listNFC")
    public CollectionResponse<NfcRecord> listNFC(@Named("count") int count) {
        List<NfcRecord> records = ofy().load().type(NfcRecord.class).limit(count).list();
        return CollectionResponse.<NfcRecord>builder().setItems(records).build();
    }

    private NfcRecord findRecord(String id) {
        return ofy().load().type(NfcRecord.class).id(id).now();
    }
}
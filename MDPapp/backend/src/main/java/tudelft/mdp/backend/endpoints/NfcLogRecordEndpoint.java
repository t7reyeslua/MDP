package tudelft.mdp.backend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.NotFoundException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import tudelft.mdp.backend.records.NfcLogRecord;

import static tudelft.mdp.backend.OfyService.ofy;

/** An endpoint class we are exposing */
@Api(name = "nfcLogRecordEndpoint", description = "An API to manage the historical log of NFC tags", version = "v1", namespace = @ApiNamespace(ownerDomain = "endpoints.backend.mdp.tudelft", ownerName = "endpoints.backend.mdp.tudelft", packagePath=""))
public class NfcLogRecordEndpoint {


    private static final Logger LOG = Logger.getLogger(NfcLogRecordEndpoint.class.getName());

    /**
     * This method gets the <code>NfcLogRecord</code> object associated with the specified <code>id</code>.
     * @param nfcId The id of the NFC tag of the object to be returned.
     * @return The <code>NfcLogRecord</code> associated with <code>id</code>.
     */
    @ApiMethod(name = "listNFClogByTag")
    public CollectionResponse<NfcLogRecord> listNFClogByTag(@Named("nfcId") String nfcId) {
        // Implement this function
        LOG.info("Calling listNFClogByTag method");
        List<NfcLogRecord> records = ofy().load().type(NfcLogRecord.class).filter("nfcId", nfcId).list();
        return CollectionResponse.<NfcLogRecord>builder().setItems(records).build();

    }

    @ApiMethod(name = "listNFClogByTagDate")
    public CollectionResponse<NfcLogRecord> listNFClogByTagDate(@Named("nfcId") String nfcId, @Named("minDate") Double minDate, @Named("maxDate") Double maxDate) {
        // Implement this function
        LOG.info("Calling listNFClogByTagDate method");
        List<NfcLogRecord> records = ofy().load().type(NfcLogRecord.class)
                .filter("nfcId", nfcId)
                .filter("timestamp >=", minDate)
                .filter("timestamp <=", maxDate)
                .list();
        return CollectionResponse.<NfcLogRecord>builder().setItems(records).build();

    }



    /**
     * This inserts a new <code>NfcLogRecord</code> object.
     * @param nfcLogRecord The object to be added.
     * @return The object to be added.
     */
    @ApiMethod(name = "insertNFClog")
    public NfcLogRecord insertNFClog(NfcLogRecord nfcLogRecord) throws ConflictException {
        // Implement this function

        LOG.info("Calling insertNfcLogRecord method");

        Long id = nfcLogRecord.getId();
        if (id != null) {
            if (findRecord(id) != null) {
                throw new ConflictException("Object already exists");
            }
        }

        // 1) create a java calendar instance
        Calendar calendar = Calendar.getInstance();
        // 2) get a java.util.Date from the calendar instance.
        //    this date will represent the current instant, or "now".
        java.util.Date now = calendar.getTime();
        // 3) a java current time (now) instance
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());

        String stringTS = new SimpleDateFormat("yyyyMMddHHmmss").format(currentTimestamp);

        nfcLogRecord.setTimestamp(Double.valueOf(stringTS));

        //Since our @Id field is a Long, Objectify will generate a unique value for us
        //when we use put
        ofy().save().entity(nfcLogRecord).now();

        return nfcLogRecord;
    }

    /**
     * This deletes an existing <code>NfcLogRecord</code> object.
     * @param id The id of the object to be deleted.
     */
    @ApiMethod(name = "deleteNFClog")
    public void deleteNFClog(@Named("id") Long id) throws NotFoundException {

        LOG.info("Calling deleteNFC method");
        NfcLogRecord record = findRecord(id);
        if(record == null) {
            throw new NotFoundException("NFC Log Record does not exist");
        }
        ofy().delete().entity(record).now();
    }

    private NfcLogRecord findRecord(Long id) {
        return ofy().load().type(NfcLogRecord.class).id(id).now();
    }
}
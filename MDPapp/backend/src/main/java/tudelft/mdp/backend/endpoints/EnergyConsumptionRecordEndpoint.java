package tudelft.mdp.backend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;

import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import tudelft.mdp.backend.Utils;
import tudelft.mdp.backend.records.EnergyConsumptionRecord;

import static tudelft.mdp.backend.OfyService.ofy;

/** An endpoint class we are exposing */
@Api(name = "energyConsumptionRecordEndpoint",
        description = "An API to manage the energy consumption history of each user",
        version = "v1", namespace = @ApiNamespace(ownerDomain = "endpoints.backend.mdp.tudelft", ownerName = "endpoints.backend.mdp.tudelft", packagePath=""))
public class EnergyConsumptionRecordEndpoint {

    // Make sure to add this endpoint to your web.xml file if this is a web application.

    private static final Logger LOG = Logger.getLogger(EnergyConsumptionRecordEndpoint.class.getName());

    /**
     * This method gets the <code>EnergyConsumptionRecord</code> object associated with the specified <code>id</code>.
     * @param id The id of the object to be returned.
     * @return The <code>EnergyConsumptionRecord</code> associated with <code>id</code>.
     */
    @ApiMethod(name = "getEnergyConsumptionRecord")
    public EnergyConsumptionRecord getEnergyConsumptionRecord(@Named("id") Long id) {
        // Implement this function

        LOG.info("Calling getEnergyConsumptionRecord method");
        return null;
    }

    /**
     * This inserts a new <code>EnergyConsumptionRecord</code> object.
     * @param energyConsumptionRecord The object to be added.
     * @return The object to be added.
     */
    @ApiMethod(name = "insertEnergyConsumptionRecord", path = "insert_energy_record")
    public EnergyConsumptionRecord insertEnergyConsumptionRecord(EnergyConsumptionRecord energyConsumptionRecord) {
        LOG.info("Calling insertEnergyConsumptionRecord method");
        ofy().save().entity(energyConsumptionRecord).now();
        return energyConsumptionRecord;
    }

    @ApiMethod(name = "listEnergyRecordsByUserDate", path = "list_energy_records_user_date")
    public CollectionResponse<EnergyConsumptionRecord> listEnergyRecordsByUserDate(
            @Named("username") String user,
            @Named("minDate") String minDate,
            @Named("maxDate") String maxDate) {

        LOG.info("Calling listEnergyRecordsByUserDate method for " + user  );

        List<EnergyConsumptionRecord> records= ofy().load().type(EnergyConsumptionRecord.class)
                .filter("username", user)
                .filter("timestamp >=", minDate)
                .filter("timestamp <=", maxDate)
                .order("timestamp")
                .list();
        //records = sortByTimestamp(records);
        LOG.info("Records:" + records.size());

        return CollectionResponse.<EnergyConsumptionRecord>builder().setItems(records).build();
    }






}
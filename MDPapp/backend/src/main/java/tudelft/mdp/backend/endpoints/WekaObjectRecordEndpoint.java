package tudelft.mdp.backend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import tudelft.mdp.backend.Utils;
import tudelft.mdp.backend.gcs.GcsHelper;
import tudelft.mdp.backend.records.WekaObjectRecord;
import weka.classifiers.Classifier;
import weka.core.Instances;

import static tudelft.mdp.backend.OfyService.ofy;

/** An endpoint class we are exposing */
@Api(name = "wekaObjectRecordEndpoint",
        description = "An API to manage the records containing the weka objects",
        version = "v1",
        namespace = @ApiNamespace(ownerDomain = "endpoints.backend.mdp.tudelft",
        ownerName = "endpoints.backend.mdp.tudelft", packagePath=""))
public class WekaObjectRecordEndpoint {

    // Make sure to add this endpoint to your web.xml file if this is a web application.

    private static final Logger LOG = Logger.getLogger(WekaObjectRecordEndpoint.class.getName());

    @ApiMethod(name = "getWekaObjectRecord", path = "get_weka_object")
    public WekaObjectRecord getWekaObjectRecord(@Named("id") Long id) {
        LOG.info("Calling getWekaObjectRecord method");
        return ofy().load().type(WekaObjectRecord.class).id(id).now();
    }

    @ApiMethod(name = "getLatestWekaObjectRecord", path = "get_latest_weka_object")
    public WekaObjectRecord getLatestWekaObjectRecord() {

        LOG.info("Calling getLatestWekaObjectRecord method");

        List<WekaObjectRecord> records= ofy().load().type(WekaObjectRecord.class)
                .order("timestamp")
                .list();

        //records = sortByTimestamp(records);
        LOG.info("Records:" + records.size());

        if (records.size()>0){
            return records.get(0);
        } else {
            return null;
        }
    }

    @ApiMethod(name = "insertWekaObjectRecord", path = "insert_weka_object")
    public WekaObjectRecord insertWekaObjectRecord(WekaObjectRecord wekaObjectRecord) {
        LOG.info("Calling insertWekaObjectRecord method");
        ofy().save().entity(wekaObjectRecord).now();
        return wekaObjectRecord;
    }

    @ApiMethod(name = "testGcs", path = "test_gcs")
    public void testGCS(
            @Named("filename") String filename) {

        LOG.info("Calling testGCS method");
        ArrayList<String> text = new ArrayList<String>();
        text.add("This ");
        text.add(" is");
        text.add(" a");
        text.add(" test. ");
        text.add(Utils.getCurrentTimestamp());
        GcsHelper gcsHelper = new GcsHelper();
        gcsHelper.writeToGCS(filename + ".txt", text);
    }

    @ApiMethod(name = "readInstances", path = "read_instances")
    public WekaObjectRecord readInstances(
            @Named("filename") String filename) {

        LOG.info("Calling readInstances method");
        GcsHelper gcsHelper = new GcsHelper();
        Instances instances = gcsHelper.readInstancesFromGCS(filename);
        LOG.info("No. Attributes: " + instances.numAttributes()
                + "|No. Instances: " + instances.numInstances()
                + "|No. Classes: " + instances.numClasses());

        WekaObjectRecord wekaObjectRecord = new WekaObjectRecord();
        wekaObjectRecord.setFilename(filename);
        wekaObjectRecord.setDescription("No. Attributes: " + instances.numAttributes()
                + "|No. Instances: " + instances.numInstances()
                + "|No. Classes: " + instances.numClasses());
        return wekaObjectRecord;
    }

    @ApiMethod(name = "readCls", path = "read_cls")
    public WekaObjectRecord readCls(
            @Named("filename") String filename) {

        LOG.info("Calling readInstances method");
        GcsHelper gcsHelper = new GcsHelper();
        Classifier cls = gcsHelper.readClsFromGCS(filename);

        WekaObjectRecord wekaObjectRecord = new WekaObjectRecord();
        wekaObjectRecord.setFilename(filename);
        wekaObjectRecord.setDescription(cls.toString());
        return wekaObjectRecord;

    }



}
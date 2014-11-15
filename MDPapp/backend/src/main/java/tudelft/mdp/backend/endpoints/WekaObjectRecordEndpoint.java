package tudelft.mdp.backend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import tudelft.mdp.backend.Utils;
import tudelft.mdp.backend.gcs.GcsHelper;
import tudelft.mdp.backend.records.LocationFeaturesRecord;
import tudelft.mdp.backend.records.WekaObjectRecord;
import tudelft.mdp.backend.weka.WekaMethods;
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
    public WekaObjectRecord getLatestWekaObjectRecord(@Named("type") String objectType, @Named("filter") String filter) {

        LOG.info("Calling getLatestWekaObjectRecord method");

        List<WekaObjectRecord> records= ofy().load().type(WekaObjectRecord.class)
                .filter("objectType", objectType)
                .order("timestamp")
                .list();

        //records = sortByTimestamp(records);
        LOG.info("Records:" + records.size());

        WekaObjectRecord result = new WekaObjectRecord();
        result.setFilename("");
        result.setObjectType(objectType);
        result.setDescription("No match found");
        for (WekaObjectRecord record : records){
            if (record.getDescription().contains(filter)){
                result = record;
                break;
            }
        }

        return result;
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

    @ApiMethod(name = "evaluateLocation", path = "evaluate_location")
    public LocationFeaturesRecord evaluateLocation(
            @Named("instanceFilename") String instanceFilename,
            @Named("clsFilename") String clsFilename,
            LocationFeaturesRecord eval) {

        LOG.info("Calling evaluateLocation method using: " + instanceFilename + "|" + clsFilename);
        GcsHelper gcsHelper = new GcsHelper();
        Classifier cls = gcsHelper.readClsFromGCS(clsFilename);
        Instances instances = gcsHelper.readInstancesFromGCS(instanceFilename);
        Instances instanceToEval = WekaMethods.CreateLocationInstanceToEval(instances,
                eval.getLocationFeatures().getValue());

        String prediction = WekaMethods.GetPredictionDistributionOnline(instanceToEval, cls);
        Text result = new Text(prediction);

        LOG.info("Result: " + prediction);
        eval.setLocationFeatures(result);
        return eval;
    }



}
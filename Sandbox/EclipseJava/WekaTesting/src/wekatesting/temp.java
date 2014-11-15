public void createInstanceSet(List<DeviceMotionLocationRecord> records, String minDate, String maxDate){
        String relation  = "Events";
        ArrayList<String> classAttributes;
        ArrayList<String> locationAttributes;
        ArrayList<String> motionAttributes;
        ArrayList<String> features = new ArrayList<String>();


        HashSet<String> classAttributesSet = new HashSet<String>();
        HashSet<String> locationAttributesSet = new HashSet<String>();

        // Build the class and location attributes. (Motion Attributes are always fixed)
        for (DeviceMotionLocationRecord deviceMotionLocationRecord : records){
            classAttributesSet.add(deviceMotionLocationRecord.getEvent().replaceAll("\\s",""));

            String locationData = deviceMotionLocationRecord.getLocationFeatures().getValue();
            String lines[] = locationData.split("\\n");
            String locationAttributesStr = lines[0];

            String allNetworksWithSuffix[] = locationAttributesStr.split(",");
            locationAttributesSet.addAll(Arrays.asList(allNetworksWithSuffix));
        }

        classAttributes     = new ArrayList<String>(classAttributesSet);
        motionAttributes    = new ArrayList<String>(WekaUtils.getAttributes());
        locationAttributes  = new ArrayList<String>(locationAttributesSet);
        Collections.sort(locationAttributes);


        LOG.info("Distinct Class Records:" + classAttributes.size());
        LOG.info("Distinct Networks:" + (locationAttributes.size()/4));
        LOG.info("Distinct Location Attributes:" + locationAttributes.size());
        LOG.info("Distinct Motion Attributes:" + motionAttributes.size());

        for (String attr : classAttributes){
            LOG.info("Class: " + attr);
        }
        for (String attr : motionAttributes){
            LOG.info("Motion: " + attr);
        }
        for (String attr : locationAttributes){
            LOG.info("Location: " + attr);
        }

        // Once you know all existing location attributes, build the location features
        for (DeviceMotionLocationRecord deviceMotionLocationRecord : records){
            String locationData = deviceMotionLocationRecord.getLocationFeatures().getValue();
            String motionFeatures = deviceMotionLocationRecord.getMotionFeatures().getValue();
            String lines[] = locationData.split("\\n");
            String locationAttributesStr = lines[0];
            String locationFeaturesStr = lines[1];

            String attributes[] = locationAttributesStr.split(",");
            String values[]     = locationFeaturesStr.split(",");

            HashMap<String,String> recordFeatures = new HashMap<String, String>();
            for (int i = 0; i < attributes.length; i++){
                recordFeatures.put(attributes[i], values[i]);
            }

            String locationFeatures = "";
            for (String locationAttribute : locationAttributes){
                if (recordFeatures.containsKey(locationAttribute)){
                    locationFeatures += recordFeatures.get(locationAttribute) + ",";
                } else {
                    locationFeatures += "?,";
                }
            }
            //remove the last comma
            locationFeatures = locationFeatures.substring(0, locationFeatures.length()-1);
            String classAttribute = deviceMotionLocationRecord.getEvent().replaceAll("\\s", "");
            //features.add(locationFeatures + "," + motionFeatures);

            //Hack to solve bug in ugly way
            if (motionFeatures.length() < 50){
                motionFeatures =  motionFeatures + ","
                        + motionFeatures + ","
                        + motionFeatures + ","
                        + motionFeatures + ","
                        + motionFeatures + ","
                        + motionFeatures;
            }
            features.add(locationFeatures + "," + motionFeatures + "," + classAttribute);
        }

        Instances wekaInstances = WekaMethods.CreateInstanceSet(relation,
                motionAttributes,
                locationAttributes,
                classAttributes,
                features);

        storeWekaObjects(wekaInstances, minDate, maxDate);
    }
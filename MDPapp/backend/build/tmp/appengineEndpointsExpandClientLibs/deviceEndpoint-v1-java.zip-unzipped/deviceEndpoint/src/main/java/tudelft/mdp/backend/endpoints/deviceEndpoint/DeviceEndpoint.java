/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://code.google.com/p/google-apis-client-generator/
 * (build: 2014-10-28 17:08:27 UTC)
 * on 2014-11-08 at 00:46:04 UTC 
 * Modify at your own risk.
 */

package tudelft.mdp.backend.endpoints.deviceEndpoint;

/**
 * Service definition for DeviceEndpoint (v1).
 *
 * <p>
 * An API to manage the devices (NFC tags)
 * </p>
 *
 * <p>
 * For more information about this service, see the
 * <a href="" target="_blank">API Documentation</a>
 * </p>
 *
 * <p>
 * This service uses {@link DeviceEndpointRequestInitializer} to initialize global parameters via its
 * {@link Builder}.
 * </p>
 *
 * @since 1.3
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public class DeviceEndpoint extends com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient {

  // Note: Leave this static initializer at the top of the file.
  static {
    com.google.api.client.util.Preconditions.checkState(
        com.google.api.client.googleapis.GoogleUtils.MAJOR_VERSION == 1 &&
        com.google.api.client.googleapis.GoogleUtils.MINOR_VERSION >= 15,
        "You are currently running with version %s of google-api-client. " +
        "You need at least version 1.15 of google-api-client to run version " +
        "1.19.0 of the deviceEndpoint library.", com.google.api.client.googleapis.GoogleUtils.VERSION);
  }

  /**
   * The default encoded root URL of the service. This is determined when the library is generated
   * and normally should not be changed.
   *
   * @since 1.7
   */
  public static final String DEFAULT_ROOT_URL = "https://tudelft-mdp.appspot.com/_ah/api/";

  /**
   * The default encoded service path of the service. This is determined when the library is
   * generated and normally should not be changed.
   *
   * @since 1.7
   */
  public static final String DEFAULT_SERVICE_PATH = "deviceEndpoint/v1/";

  /**
   * The default encoded base URL of the service. This is determined when the library is generated
   * and normally should not be changed.
   */
  public static final String DEFAULT_BASE_URL = DEFAULT_ROOT_URL + DEFAULT_SERVICE_PATH;

  /**
   * Constructor.
   *
   * <p>
   * Use {@link Builder} if you need to specify any of the optional parameters.
   * </p>
   *
   * @param transport HTTP transport, which should normally be:
   *        <ul>
   *        <li>Google App Engine:
   *        {@code com.google.api.client.extensions.appengine.http.UrlFetchTransport}</li>
   *        <li>Android: {@code newCompatibleTransport} from
   *        {@code com.google.api.client.extensions.android.http.AndroidHttp}</li>
   *        <li>Java: {@link com.google.api.client.googleapis.javanet.GoogleNetHttpTransport#newTrustedTransport()}
   *        </li>
   *        </ul>
   * @param jsonFactory JSON factory, which may be:
   *        <ul>
   *        <li>Jackson: {@code com.google.api.client.json.jackson2.JacksonFactory}</li>
   *        <li>Google GSON: {@code com.google.api.client.json.gson.GsonFactory}</li>
   *        <li>Android Honeycomb or higher:
   *        {@code com.google.api.client.extensions.android.json.AndroidJsonFactory}</li>
   *        </ul>
   * @param httpRequestInitializer HTTP request initializer or {@code null} for none
   * @since 1.7
   */
  public DeviceEndpoint(com.google.api.client.http.HttpTransport transport, com.google.api.client.json.JsonFactory jsonFactory,
      com.google.api.client.http.HttpRequestInitializer httpRequestInitializer) {
    this(new Builder(transport, jsonFactory, httpRequestInitializer));
  }

  /**
   * @param builder builder
   */
  DeviceEndpoint(Builder builder) {
    super(builder);
  }

  @Override
  protected void initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest<?> httpClientRequest) throws java.io.IOException {
    super.initialize(httpClientRequest);
  }

  /**
   * Create a request for the method "decreaseDeviceUsers".
   *
   * This request holds the parameters needed by the deviceEndpoint server.  After setting any
   * optional parameters, call the {@link DecreaseDeviceUsers#execute()} method to invoke the remote
   * operation.
   *
   * @param id
   * @return the request
   */
  public DecreaseDeviceUsers decreaseDeviceUsers(java.lang.String id) throws java.io.IOException {
    DecreaseDeviceUsers result = new DecreaseDeviceUsers(id);
    initialize(result);
    return result;
  }

  public class DecreaseDeviceUsers extends DeviceEndpointRequest<tudelft.mdp.backend.endpoints.deviceEndpoint.model.NfcRecord> {

    private static final String REST_PATH = "decreaseNFC/{id}";

    /**
     * Create a request for the method "decreaseDeviceUsers".
     *
     * This request holds the parameters needed by the the deviceEndpoint server.  After setting any
     * optional parameters, call the {@link DecreaseDeviceUsers#execute()} method to invoke the remote
     * operation. <p> {@link DecreaseDeviceUsers#initialize(com.google.api.client.googleapis.services.
     * AbstractGoogleClientRequest)} must be called to initialize this instance immediately after
     * invoking the constructor. </p>
     *
     * @param id
     * @since 1.13
     */
    protected DecreaseDeviceUsers(java.lang.String id) {
      super(DeviceEndpoint.this, "POST", REST_PATH, null, tudelft.mdp.backend.endpoints.deviceEndpoint.model.NfcRecord.class);
      this.id = com.google.api.client.util.Preconditions.checkNotNull(id, "Required parameter id must be specified.");
    }

    @Override
    public DecreaseDeviceUsers setAlt(java.lang.String alt) {
      return (DecreaseDeviceUsers) super.setAlt(alt);
    }

    @Override
    public DecreaseDeviceUsers setFields(java.lang.String fields) {
      return (DecreaseDeviceUsers) super.setFields(fields);
    }

    @Override
    public DecreaseDeviceUsers setKey(java.lang.String key) {
      return (DecreaseDeviceUsers) super.setKey(key);
    }

    @Override
    public DecreaseDeviceUsers setOauthToken(java.lang.String oauthToken) {
      return (DecreaseDeviceUsers) super.setOauthToken(oauthToken);
    }

    @Override
    public DecreaseDeviceUsers setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (DecreaseDeviceUsers) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public DecreaseDeviceUsers setQuotaUser(java.lang.String quotaUser) {
      return (DecreaseDeviceUsers) super.setQuotaUser(quotaUser);
    }

    @Override
    public DecreaseDeviceUsers setUserIp(java.lang.String userIp) {
      return (DecreaseDeviceUsers) super.setUserIp(userIp);
    }

    @com.google.api.client.util.Key
    private java.lang.String id;

    /**

     */
    public java.lang.String getId() {
      return id;
    }

    public DecreaseDeviceUsers setId(java.lang.String id) {
      this.id = id;
      return this;
    }

    @Override
    public DecreaseDeviceUsers set(String parameterName, Object value) {
      return (DecreaseDeviceUsers) super.set(parameterName, value);
    }
  }

  /**
   * Create a request for the method "deleteDevice".
   *
   * This request holds the parameters needed by the deviceEndpoint server.  After setting any
   * optional parameters, call the {@link DeleteDevice#execute()} method to invoke the remote
   * operation.
   *
   * @param id
   * @return the request
   */
  public DeleteDevice deleteDevice(java.lang.String id) throws java.io.IOException {
    DeleteDevice result = new DeleteDevice(id);
    initialize(result);
    return result;
  }

  public class DeleteDevice extends DeviceEndpointRequest<Void> {

    private static final String REST_PATH = "nfc/{id}";

    /**
     * Create a request for the method "deleteDevice".
     *
     * This request holds the parameters needed by the the deviceEndpoint server.  After setting any
     * optional parameters, call the {@link DeleteDevice#execute()} method to invoke the remote
     * operation. <p> {@link
     * DeleteDevice#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)}
     * must be called to initialize this instance immediately after invoking the constructor. </p>
     *
     * @param id
     * @since 1.13
     */
    protected DeleteDevice(java.lang.String id) {
      super(DeviceEndpoint.this, "DELETE", REST_PATH, null, Void.class);
      this.id = com.google.api.client.util.Preconditions.checkNotNull(id, "Required parameter id must be specified.");
    }

    @Override
    public DeleteDevice setAlt(java.lang.String alt) {
      return (DeleteDevice) super.setAlt(alt);
    }

    @Override
    public DeleteDevice setFields(java.lang.String fields) {
      return (DeleteDevice) super.setFields(fields);
    }

    @Override
    public DeleteDevice setKey(java.lang.String key) {
      return (DeleteDevice) super.setKey(key);
    }

    @Override
    public DeleteDevice setOauthToken(java.lang.String oauthToken) {
      return (DeleteDevice) super.setOauthToken(oauthToken);
    }

    @Override
    public DeleteDevice setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (DeleteDevice) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public DeleteDevice setQuotaUser(java.lang.String quotaUser) {
      return (DeleteDevice) super.setQuotaUser(quotaUser);
    }

    @Override
    public DeleteDevice setUserIp(java.lang.String userIp) {
      return (DeleteDevice) super.setUserIp(userIp);
    }

    @com.google.api.client.util.Key
    private java.lang.String id;

    /**

     */
    public java.lang.String getId() {
      return id;
    }

    public DeleteDevice setId(java.lang.String id) {
      this.id = id;
      return this;
    }

    @Override
    public DeleteDevice set(String parameterName, Object value) {
      return (DeleteDevice) super.set(parameterName, value);
    }
  }

  /**
   * Create a request for the method "getDevice".
   *
   * This request holds the parameters needed by the deviceEndpoint server.  After setting any
   * optional parameters, call the {@link GetDevice#execute()} method to invoke the remote operation.
   *
   * @param id
   * @return the request
   */
  public GetDevice getDevice(java.lang.String id) throws java.io.IOException {
    GetDevice result = new GetDevice(id);
    initialize(result);
    return result;
  }

  public class GetDevice extends DeviceEndpointRequest<tudelft.mdp.backend.endpoints.deviceEndpoint.model.NfcRecord> {

    private static final String REST_PATH = "get_device";

    /**
     * Create a request for the method "getDevice".
     *
     * This request holds the parameters needed by the the deviceEndpoint server.  After setting any
     * optional parameters, call the {@link GetDevice#execute()} method to invoke the remote
     * operation. <p> {@link
     * GetDevice#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)}
     * must be called to initialize this instance immediately after invoking the constructor. </p>
     *
     * @param id
     * @since 1.13
     */
    protected GetDevice(java.lang.String id) {
      super(DeviceEndpoint.this, "GET", REST_PATH, null, tudelft.mdp.backend.endpoints.deviceEndpoint.model.NfcRecord.class);
      this.id = com.google.api.client.util.Preconditions.checkNotNull(id, "Required parameter id must be specified.");
    }

    @Override
    public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
      return super.executeUsingHead();
    }

    @Override
    public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
      return super.buildHttpRequestUsingHead();
    }

    @Override
    public GetDevice setAlt(java.lang.String alt) {
      return (GetDevice) super.setAlt(alt);
    }

    @Override
    public GetDevice setFields(java.lang.String fields) {
      return (GetDevice) super.setFields(fields);
    }

    @Override
    public GetDevice setKey(java.lang.String key) {
      return (GetDevice) super.setKey(key);
    }

    @Override
    public GetDevice setOauthToken(java.lang.String oauthToken) {
      return (GetDevice) super.setOauthToken(oauthToken);
    }

    @Override
    public GetDevice setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (GetDevice) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public GetDevice setQuotaUser(java.lang.String quotaUser) {
      return (GetDevice) super.setQuotaUser(quotaUser);
    }

    @Override
    public GetDevice setUserIp(java.lang.String userIp) {
      return (GetDevice) super.setUserIp(userIp);
    }

    @com.google.api.client.util.Key
    private java.lang.String id;

    /**

     */
    public java.lang.String getId() {
      return id;
    }

    public GetDevice setId(java.lang.String id) {
      this.id = id;
      return this;
    }

    @Override
    public GetDevice set(String parameterName, Object value) {
      return (GetDevice) super.set(parameterName, value);
    }
  }

  /**
   * Create a request for the method "increaseDeviceUsers".
   *
   * This request holds the parameters needed by the deviceEndpoint server.  After setting any
   * optional parameters, call the {@link IncreaseDeviceUsers#execute()} method to invoke the remote
   * operation.
   *
   * @param id
   * @return the request
   */
  public IncreaseDeviceUsers increaseDeviceUsers(java.lang.String id) throws java.io.IOException {
    IncreaseDeviceUsers result = new IncreaseDeviceUsers(id);
    initialize(result);
    return result;
  }

  public class IncreaseDeviceUsers extends DeviceEndpointRequest<tudelft.mdp.backend.endpoints.deviceEndpoint.model.NfcRecord> {

    private static final String REST_PATH = "increaseNFC/{id}";

    /**
     * Create a request for the method "increaseDeviceUsers".
     *
     * This request holds the parameters needed by the the deviceEndpoint server.  After setting any
     * optional parameters, call the {@link IncreaseDeviceUsers#execute()} method to invoke the remote
     * operation. <p> {@link IncreaseDeviceUsers#initialize(com.google.api.client.googleapis.services.
     * AbstractGoogleClientRequest)} must be called to initialize this instance immediately after
     * invoking the constructor. </p>
     *
     * @param id
     * @since 1.13
     */
    protected IncreaseDeviceUsers(java.lang.String id) {
      super(DeviceEndpoint.this, "POST", REST_PATH, null, tudelft.mdp.backend.endpoints.deviceEndpoint.model.NfcRecord.class);
      this.id = com.google.api.client.util.Preconditions.checkNotNull(id, "Required parameter id must be specified.");
    }

    @Override
    public IncreaseDeviceUsers setAlt(java.lang.String alt) {
      return (IncreaseDeviceUsers) super.setAlt(alt);
    }

    @Override
    public IncreaseDeviceUsers setFields(java.lang.String fields) {
      return (IncreaseDeviceUsers) super.setFields(fields);
    }

    @Override
    public IncreaseDeviceUsers setKey(java.lang.String key) {
      return (IncreaseDeviceUsers) super.setKey(key);
    }

    @Override
    public IncreaseDeviceUsers setOauthToken(java.lang.String oauthToken) {
      return (IncreaseDeviceUsers) super.setOauthToken(oauthToken);
    }

    @Override
    public IncreaseDeviceUsers setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (IncreaseDeviceUsers) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public IncreaseDeviceUsers setQuotaUser(java.lang.String quotaUser) {
      return (IncreaseDeviceUsers) super.setQuotaUser(quotaUser);
    }

    @Override
    public IncreaseDeviceUsers setUserIp(java.lang.String userIp) {
      return (IncreaseDeviceUsers) super.setUserIp(userIp);
    }

    @com.google.api.client.util.Key
    private java.lang.String id;

    /**

     */
    public java.lang.String getId() {
      return id;
    }

    public IncreaseDeviceUsers setId(java.lang.String id) {
      this.id = id;
      return this;
    }

    @Override
    public IncreaseDeviceUsers set(String parameterName, Object value) {
      return (IncreaseDeviceUsers) super.set(parameterName, value);
    }
  }

  /**
   * Create a request for the method "insertDevice".
   *
   * This request holds the parameters needed by the deviceEndpoint server.  After setting any
   * optional parameters, call the {@link InsertDevice#execute()} method to invoke the remote
   * operation.
   *
   * @param content the {@link tudelft.mdp.backend.endpoints.deviceEndpoint.model.NfcRecord}
   * @return the request
   */
  public InsertDevice insertDevice(tudelft.mdp.backend.endpoints.deviceEndpoint.model.NfcRecord content) throws java.io.IOException {
    InsertDevice result = new InsertDevice(content);
    initialize(result);
    return result;
  }

  public class InsertDevice extends DeviceEndpointRequest<tudelft.mdp.backend.endpoints.deviceEndpoint.model.NfcRecord> {

    private static final String REST_PATH = "nfcrecord";

    /**
     * Create a request for the method "insertDevice".
     *
     * This request holds the parameters needed by the the deviceEndpoint server.  After setting any
     * optional parameters, call the {@link InsertDevice#execute()} method to invoke the remote
     * operation. <p> {@link
     * InsertDevice#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)}
     * must be called to initialize this instance immediately after invoking the constructor. </p>
     *
     * @param content the {@link tudelft.mdp.backend.endpoints.deviceEndpoint.model.NfcRecord}
     * @since 1.13
     */
    protected InsertDevice(tudelft.mdp.backend.endpoints.deviceEndpoint.model.NfcRecord content) {
      super(DeviceEndpoint.this, "POST", REST_PATH, content, tudelft.mdp.backend.endpoints.deviceEndpoint.model.NfcRecord.class);
    }

    @Override
    public InsertDevice setAlt(java.lang.String alt) {
      return (InsertDevice) super.setAlt(alt);
    }

    @Override
    public InsertDevice setFields(java.lang.String fields) {
      return (InsertDevice) super.setFields(fields);
    }

    @Override
    public InsertDevice setKey(java.lang.String key) {
      return (InsertDevice) super.setKey(key);
    }

    @Override
    public InsertDevice setOauthToken(java.lang.String oauthToken) {
      return (InsertDevice) super.setOauthToken(oauthToken);
    }

    @Override
    public InsertDevice setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (InsertDevice) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public InsertDevice setQuotaUser(java.lang.String quotaUser) {
      return (InsertDevice) super.setQuotaUser(quotaUser);
    }

    @Override
    public InsertDevice setUserIp(java.lang.String userIp) {
      return (InsertDevice) super.setUserIp(userIp);
    }

    @Override
    public InsertDevice set(String parameterName, Object value) {
      return (InsertDevice) super.set(parameterName, value);
    }
  }

  /**
   * Create a request for the method "listDevices".
   *
   * This request holds the parameters needed by the deviceEndpoint server.  After setting any
   * optional parameters, call the {@link ListDevices#execute()} method to invoke the remote
   * operation.
   *
   * @param count
   * @return the request
   */
  public ListDevices listDevices(java.lang.Integer count) throws java.io.IOException {
    ListDevices result = new ListDevices(count);
    initialize(result);
    return result;
  }

  public class ListDevices extends DeviceEndpointRequest<tudelft.mdp.backend.endpoints.deviceEndpoint.model.CollectionResponseNfcRecord> {

    private static final String REST_PATH = "nfcrecord/{count}";

    /**
     * Create a request for the method "listDevices".
     *
     * This request holds the parameters needed by the the deviceEndpoint server.  After setting any
     * optional parameters, call the {@link ListDevices#execute()} method to invoke the remote
     * operation. <p> {@link
     * ListDevices#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)}
     * must be called to initialize this instance immediately after invoking the constructor. </p>
     *
     * @param count
     * @since 1.13
     */
    protected ListDevices(java.lang.Integer count) {
      super(DeviceEndpoint.this, "GET", REST_PATH, null, tudelft.mdp.backend.endpoints.deviceEndpoint.model.CollectionResponseNfcRecord.class);
      this.count = com.google.api.client.util.Preconditions.checkNotNull(count, "Required parameter count must be specified.");
    }

    @Override
    public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
      return super.executeUsingHead();
    }

    @Override
    public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
      return super.buildHttpRequestUsingHead();
    }

    @Override
    public ListDevices setAlt(java.lang.String alt) {
      return (ListDevices) super.setAlt(alt);
    }

    @Override
    public ListDevices setFields(java.lang.String fields) {
      return (ListDevices) super.setFields(fields);
    }

    @Override
    public ListDevices setKey(java.lang.String key) {
      return (ListDevices) super.setKey(key);
    }

    @Override
    public ListDevices setOauthToken(java.lang.String oauthToken) {
      return (ListDevices) super.setOauthToken(oauthToken);
    }

    @Override
    public ListDevices setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (ListDevices) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public ListDevices setQuotaUser(java.lang.String quotaUser) {
      return (ListDevices) super.setQuotaUser(quotaUser);
    }

    @Override
    public ListDevices setUserIp(java.lang.String userIp) {
      return (ListDevices) super.setUserIp(userIp);
    }

    @com.google.api.client.util.Key
    private java.lang.Integer count;

    /**

     */
    public java.lang.Integer getCount() {
      return count;
    }

    public ListDevices setCount(java.lang.Integer count) {
      this.count = count;
      return this;
    }

    @Override
    public ListDevices set(String parameterName, Object value) {
      return (ListDevices) super.set(parameterName, value);
    }
  }

  /**
   * Create a request for the method "updateDevice".
   *
   * This request holds the parameters needed by the deviceEndpoint server.  After setting any
   * optional parameters, call the {@link UpdateDevice#execute()} method to invoke the remote
   * operation.
   *
   * @param content the {@link tudelft.mdp.backend.endpoints.deviceEndpoint.model.NfcRecord}
   * @return the request
   */
  public UpdateDevice updateDevice(tudelft.mdp.backend.endpoints.deviceEndpoint.model.NfcRecord content) throws java.io.IOException {
    UpdateDevice result = new UpdateDevice(content);
    initialize(result);
    return result;
  }

  public class UpdateDevice extends DeviceEndpointRequest<tudelft.mdp.backend.endpoints.deviceEndpoint.model.NfcRecord> {

    private static final String REST_PATH = "nfcrecord";

    /**
     * Create a request for the method "updateDevice".
     *
     * This request holds the parameters needed by the the deviceEndpoint server.  After setting any
     * optional parameters, call the {@link UpdateDevice#execute()} method to invoke the remote
     * operation. <p> {@link
     * UpdateDevice#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)}
     * must be called to initialize this instance immediately after invoking the constructor. </p>
     *
     * @param content the {@link tudelft.mdp.backend.endpoints.deviceEndpoint.model.NfcRecord}
     * @since 1.13
     */
    protected UpdateDevice(tudelft.mdp.backend.endpoints.deviceEndpoint.model.NfcRecord content) {
      super(DeviceEndpoint.this, "PUT", REST_PATH, content, tudelft.mdp.backend.endpoints.deviceEndpoint.model.NfcRecord.class);
    }

    @Override
    public UpdateDevice setAlt(java.lang.String alt) {
      return (UpdateDevice) super.setAlt(alt);
    }

    @Override
    public UpdateDevice setFields(java.lang.String fields) {
      return (UpdateDevice) super.setFields(fields);
    }

    @Override
    public UpdateDevice setKey(java.lang.String key) {
      return (UpdateDevice) super.setKey(key);
    }

    @Override
    public UpdateDevice setOauthToken(java.lang.String oauthToken) {
      return (UpdateDevice) super.setOauthToken(oauthToken);
    }

    @Override
    public UpdateDevice setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (UpdateDevice) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public UpdateDevice setQuotaUser(java.lang.String quotaUser) {
      return (UpdateDevice) super.setQuotaUser(quotaUser);
    }

    @Override
    public UpdateDevice setUserIp(java.lang.String userIp) {
      return (UpdateDevice) super.setUserIp(userIp);
    }

    @Override
    public UpdateDevice set(String parameterName, Object value) {
      return (UpdateDevice) super.set(parameterName, value);
    }
  }

  /**
   * Builder for {@link DeviceEndpoint}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   *
   * @since 1.3.0
   */
  public static final class Builder extends com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient.Builder {

    /**
     * Returns an instance of a new builder.
     *
     * @param transport HTTP transport, which should normally be:
     *        <ul>
     *        <li>Google App Engine:
     *        {@code com.google.api.client.extensions.appengine.http.UrlFetchTransport}</li>
     *        <li>Android: {@code newCompatibleTransport} from
     *        {@code com.google.api.client.extensions.android.http.AndroidHttp}</li>
     *        <li>Java: {@link com.google.api.client.googleapis.javanet.GoogleNetHttpTransport#newTrustedTransport()}
     *        </li>
     *        </ul>
     * @param jsonFactory JSON factory, which may be:
     *        <ul>
     *        <li>Jackson: {@code com.google.api.client.json.jackson2.JacksonFactory}</li>
     *        <li>Google GSON: {@code com.google.api.client.json.gson.GsonFactory}</li>
     *        <li>Android Honeycomb or higher:
     *        {@code com.google.api.client.extensions.android.json.AndroidJsonFactory}</li>
     *        </ul>
     * @param httpRequestInitializer HTTP request initializer or {@code null} for none
     * @since 1.7
     */
    public Builder(com.google.api.client.http.HttpTransport transport, com.google.api.client.json.JsonFactory jsonFactory,
        com.google.api.client.http.HttpRequestInitializer httpRequestInitializer) {
      super(
          transport,
          jsonFactory,
          DEFAULT_ROOT_URL,
          DEFAULT_SERVICE_PATH,
          httpRequestInitializer,
          false);
    }

    /** Builds a new instance of {@link DeviceEndpoint}. */
    @Override
    public DeviceEndpoint build() {
      return new DeviceEndpoint(this);
    }

    @Override
    public Builder setRootUrl(String rootUrl) {
      return (Builder) super.setRootUrl(rootUrl);
    }

    @Override
    public Builder setServicePath(String servicePath) {
      return (Builder) super.setServicePath(servicePath);
    }

    @Override
    public Builder setHttpRequestInitializer(com.google.api.client.http.HttpRequestInitializer httpRequestInitializer) {
      return (Builder) super.setHttpRequestInitializer(httpRequestInitializer);
    }

    @Override
    public Builder setApplicationName(String applicationName) {
      return (Builder) super.setApplicationName(applicationName);
    }

    @Override
    public Builder setSuppressPatternChecks(boolean suppressPatternChecks) {
      return (Builder) super.setSuppressPatternChecks(suppressPatternChecks);
    }

    @Override
    public Builder setSuppressRequiredParameterChecks(boolean suppressRequiredParameterChecks) {
      return (Builder) super.setSuppressRequiredParameterChecks(suppressRequiredParameterChecks);
    }

    @Override
    public Builder setSuppressAllChecks(boolean suppressAllChecks) {
      return (Builder) super.setSuppressAllChecks(suppressAllChecks);
    }

    /**
     * Set the {@link DeviceEndpointRequestInitializer}.
     *
     * @since 1.12
     */
    public Builder setDeviceEndpointRequestInitializer(
        DeviceEndpointRequestInitializer deviceendpointRequestInitializer) {
      return (Builder) super.setGoogleClientRequestInitializer(deviceendpointRequestInitializer);
    }

    @Override
    public Builder setGoogleClientRequestInitializer(
        com.google.api.client.googleapis.services.GoogleClientRequestInitializer googleClientRequestInitializer) {
      return (Builder) super.setGoogleClientRequestInitializer(googleClientRequestInitializer);
    }
  }
}

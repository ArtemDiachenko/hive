/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hive.service.cli;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.conf.HiveConf.ConfVars;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.IMetaStoreClient;
import org.apache.hadoop.hive.ql.metadata.Hive;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.shims.ShimLoader;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hive.service.CompositeService;
import org.apache.hive.service.ServiceException;
import org.apache.hive.service.auth.HiveAuthFactory;
import org.apache.hive.service.cli.log.OperationLog;
import org.apache.hive.service.cli.operation.Operation;
import org.apache.hive.service.cli.session.HiveSession;
import org.apache.hive.service.cli.session.SessionManager;
import org.apache.hive.service.cli.thrift.TProtocolVersion;
import org.apache.hive.service.server.HiveServer2;

/**
 * CLIService.
 *
 */
public class CLIService extends CompositeService implements ICLIService {

  public static final TProtocolVersion SERVER_VERSION;

  static {
    TProtocolVersion[] protocols = TProtocolVersion.values();
    SERVER_VERSION = protocols[protocols.length - 1];
  }

  private final Log LOG = LogFactory.getLog(CLIService.class.getName());

  private HiveConf hiveConf;
  private SessionManager sessionManager;
  private IMetaStoreClient metastoreClient;
  private UserGroupInformation serviceUGI;
  private UserGroupInformation httpUGI;
  // The HiveServer2 instance running this service
  private final HiveServer2 hiveServer2;

  public CLIService(HiveServer2 hiveServer2) {
    super(CLIService.class.getSimpleName());
    this.hiveServer2 = hiveServer2;
  }

  @Override
  public synchronized void init(HiveConf hiveConf) {
    this.hiveConf = hiveConf;
    sessionManager = new SessionManager(hiveServer2);
    addService(sessionManager);
    /**
     * If auth mode is Kerberos, do a kerberos login for the service from the keytab
     */
    if (hiveConf.getVar(ConfVars.HIVE_SERVER2_AUTHENTICATION).equalsIgnoreCase(
        HiveAuthFactory.AuthTypes.KERBEROS.toString())) {
      try {
        HiveAuthFactory.loginFromKeytab(hiveConf);
        this.serviceUGI = ShimLoader.getHadoopShims().getUGIForConf(hiveConf);
      } catch (IOException e) {
        throw new ServiceException("Unable to login to kerberos with given principal/keytab", e);
      } catch (LoginException e) {
        throw new ServiceException("Unable to login to kerberos with given principal/keytab", e);
      }

      // Also try creating a UGI object for the SPNego principal
      String principal = hiveConf.getVar(ConfVars.HIVE_SERVER2_SPNEGO_PRINCIPAL);
      String keyTabFile = hiveConf.getVar(ConfVars.HIVE_SERVER2_SPNEGO_KEYTAB);
      if (principal.isEmpty() || keyTabFile.isEmpty()) {
        LOG.info("SPNego httpUGI not created, spNegoPrincipal: " + principal +
            ", ketabFile: " + keyTabFile);
      } else {
        try {
          this.httpUGI = HiveAuthFactory.loginFromSpnegoKeytabAndReturnUGI(hiveConf);
          LOG.info("SPNego httpUGI successfully created.");
        } catch (IOException e) {
          LOG.warn("SPNego httpUGI creation failed: ", e);
        }
      }
    }
    super.init(hiveConf);
  }

  public UserGroupInformation getServiceUGI() {
    return this.serviceUGI;
  }

  public UserGroupInformation getHttpUGI() {
    return this.httpUGI;
  }

  @Override
  public synchronized void start() {
    super.start();

    // Initialize and test a connection to the metastore
    IMetaStoreClient metastoreClient = null;

    try {
      // Initialize and test a connection to the metastore
      metastoreClient = new HiveMetaStoreClient(hiveConf);
      metastoreClient.getDatabases("default");
    } catch (Exception e) {
      throw new ServiceException("Unable to connect to MetaStore!", e);
    }
    finally {
      if (metastoreClient != null) {
        metastoreClient.close();
      }
    }
  }

  @Override
  public synchronized void stop() {
    super.stop();
  }

  public SessionHandle openSession(TProtocolVersion protocol, String username, String password,
      Map<String, String> configuration) throws HiveSQLException {
    SessionHandle sessionHandle = sessionManager.openSession(protocol, username, password, configuration, false, null);
    LOG.debug(sessionHandle + ": openSession()");
    sessionManager.clearIpAddress();
    return sessionHandle;
  }

  public SessionHandle openSessionWithImpersonation(TProtocolVersion protocol, String username,
      String password, Map<String, String> configuration, String delegationToken)
          throws HiveSQLException {
    SessionHandle sessionHandle = sessionManager.openSession(protocol, username, password, configuration,
        true, delegationToken);
    LOG.debug(sessionHandle + ": openSession()");
    return sessionHandle;
  }

  /* (non-Javadoc)
   * @see org.apache.hive.service.cli.ICLIService#openSession(java.lang.String, java.lang.String, java.util.Map)
   */
  @Override
  public SessionHandle openSession(String username, String password, Map<String, String> configuration)
      throws HiveSQLException {
    SessionHandle sessionHandle = sessionManager.openSession(SERVER_VERSION, username, password, configuration, false, null);
    LOG.debug(sessionHandle + ": openSession()");
    return sessionHandle;
  }

  /* (non-Javadoc)
   * @see org.apache.hive.service.cli.ICLIService#openSession(java.lang.String, java.lang.String, java.util.Map)
   */
  @Override
  public SessionHandle openSessionWithImpersonation(String username, String password, Map<String, String> configuration,
      String delegationToken) throws HiveSQLException {
    SessionHandle sessionHandle = sessionManager.openSession(SERVER_VERSION, username, password, configuration,
        true, delegationToken);
    LOG.debug(sessionHandle + ": openSession()");
    return sessionHandle;
  }

  /* (non-Javadoc)
   * @see org.apache.hive.service.cli.ICLIService#closeSession(org.apache.hive.service.cli.SessionHandle)
   */
  @Override
  public void closeSession(SessionHandle sessionHandle)
      throws HiveSQLException {
    sessionManager.closeSession(sessionHandle);
    LOG.debug(sessionHandle + ": closeSession()");
    sessionManager.clearIpAddress();
  }

  /* (non-Javadoc)
   * @see org.apache.hive.service.cli.ICLIService#getInfo(org.apache.hive.service.cli.SessionHandle, java.util.List)
   */
  @Override
  public GetInfoValue getInfo(SessionHandle sessionHandle, GetInfoType getInfoType)
      throws HiveSQLException {
    GetInfoValue infoValue = sessionManager.getSession(sessionHandle)
        .getInfo(getInfoType);
    LOG.debug(sessionHandle + ": getInfo()");
    sessionManager.clearIpAddress();
    return infoValue;
  }

  /* (non-Javadoc)
   * @see org.apache.hive.service.cli.ICLIService#executeStatement(org.apache.hive.service.cli.SessionHandle,
   *  java.lang.String, java.util.Map)
   */
  @Override
  public OperationHandle executeStatement(SessionHandle sessionHandle, String statement,
      Map<String, String> confOverlay)
          throws HiveSQLException {
    OperationHandle opHandle = sessionManager.getSession(sessionHandle)
        .executeStatement(statement, confOverlay);
    LOG.debug(sessionHandle + ": executeStatement()");
    sessionManager.clearIpAddress();
    return opHandle;
  }

  /* (non-Javadoc)
   * @see org.apache.hive.service.cli.ICLIService#executeStatementAsync(org.apache.hive.service.cli.SessionHandle,
   *  java.lang.String, java.util.Map)
   */
  @Override
  public OperationHandle executeStatementAsync(SessionHandle sessionHandle, String statement,
      Map<String, String> confOverlay) throws HiveSQLException {
    OperationHandle opHandle = sessionManager.getSession(sessionHandle)
        .executeStatementAsync(statement, confOverlay);
    LOG.debug(sessionHandle + ": executeStatementAsync()");
    return opHandle;
  }


  /* (non-Javadoc)
   * @see org.apache.hive.service.cli.ICLIService#getTypeInfo(org.apache.hive.service.cli.SessionHandle)
   */
  @Override
  public OperationHandle getTypeInfo(SessionHandle sessionHandle)
      throws HiveSQLException {
    OperationHandle opHandle = sessionManager.getSession(sessionHandle)
        .getTypeInfo();
    LOG.debug(sessionHandle + ": getTypeInfo()");
    sessionManager.clearIpAddress();
    return opHandle;
  }

  /* (non-Javadoc)
   * @see org.apache.hive.service.cli.ICLIService#getCatalogs(org.apache.hive.service.cli.SessionHandle)
   */
  @Override
  public OperationHandle getCatalogs(SessionHandle sessionHandle)
      throws HiveSQLException {
    OperationHandle opHandle = sessionManager.getSession(sessionHandle)
        .getCatalogs();
    LOG.debug(sessionHandle + ": getCatalogs()");
    sessionManager.clearIpAddress();
    return opHandle;
  }

  /* (non-Javadoc)
   * @see org.apache.hive.service.cli.ICLIService#getSchemas(org.apache.hive.service.cli.SessionHandle, java.lang.String, java.lang.String)
   */
  @Override
  public OperationHandle getSchemas(SessionHandle sessionHandle,
      String catalogName, String schemaName)
          throws HiveSQLException {
    OperationHandle opHandle = sessionManager.getSession(sessionHandle)
        .getSchemas(catalogName, schemaName);
    LOG.debug(sessionHandle + ": getSchemas()");
    sessionManager.clearIpAddress();
    return opHandle;
  }

  /* (non-Javadoc)
   * @see org.apache.hive.service.cli.ICLIService#getTables(org.apache.hive.service.cli.SessionHandle, java.lang.String, java.lang.String, java.lang.String, java.util.List)
   */
  @Override
  public OperationHandle getTables(SessionHandle sessionHandle,
      String catalogName, String schemaName, String tableName, List<String> tableTypes)
          throws HiveSQLException {
    OperationHandle opHandle = sessionManager.getSession(sessionHandle)
        .getTables(catalogName, schemaName, tableName, tableTypes);
    LOG.debug(sessionHandle + ": getTables()");
    sessionManager.clearIpAddress();
    return opHandle;
  }

  /* (non-Javadoc)
   * @see org.apache.hive.service.cli.ICLIService#getTableTypes(org.apache.hive.service.cli.SessionHandle)
   */
  @Override
  public OperationHandle getTableTypes(SessionHandle sessionHandle)
      throws HiveSQLException {
    OperationHandle opHandle = sessionManager.getSession(sessionHandle)
        .getTableTypes();
    LOG.debug(sessionHandle + ": getTableTypes()");
    sessionManager.clearIpAddress();
    return opHandle;
  }

  /* (non-Javadoc)
   * @see org.apache.hive.service.cli.ICLIService#getColumns(org.apache.hive.service.cli.SessionHandle)
   */
  @Override
  public OperationHandle getColumns(SessionHandle sessionHandle,
      String catalogName, String schemaName, String tableName, String columnName)
          throws HiveSQLException {
    OperationHandle opHandle = sessionManager.getSession(sessionHandle)
        .getColumns(catalogName, schemaName, tableName, columnName);
    LOG.debug(sessionHandle + ": getColumns()");
    sessionManager.clearIpAddress();
    return opHandle;
  }

  /* (non-Javadoc)
   * @see org.apache.hive.service.cli.ICLIService#getFunctions(org.apache.hive.service.cli.SessionHandle)
   */
  @Override
  public OperationHandle getFunctions(SessionHandle sessionHandle,
      String catalogName, String schemaName, String functionName)
          throws HiveSQLException {
    OperationHandle opHandle = sessionManager.getSession(sessionHandle)
        .getFunctions(catalogName, schemaName, functionName);
    LOG.debug(sessionHandle + ": getFunctions()");
    sessionManager.clearIpAddress();
    return opHandle;
  }

  /* (non-Javadoc)
   * @see org.apache.hive.service.cli.ICLIService#getOperationStatus(org.apache.hive.service.cli.OperationHandle)
   */
  @Override
  public OperationStatus getOperationStatus(OperationHandle opHandle)
      throws HiveSQLException {
    Operation operation = sessionManager.getOperationManager().getOperation(opHandle);
    /**
     * If this is a background operation run asynchronously,
     * we block for a configured duration, before we return
     * (duration: HIVE_SERVER2_LONG_POLLING_TIMEOUT).
     * However, if the background operation is complete, we return immediately.
     */
    if (operation.shouldRunAsync()) {
      long timeout = operation.getParentSession().getHiveConf().getLongVar(
          HiveConf.ConfVars.HIVE_SERVER2_LONG_POLLING_TIMEOUT);
      try {
        operation.getBackgroundHandle().get(timeout, TimeUnit.MILLISECONDS);
      } catch (TimeoutException e) {
        // No Op, return to the caller since long polling timeout has expired
        LOG.trace(opHandle + ": Long polling timed out");
      } catch (CancellationException e) {
        // The background operation thread was cancelled
        LOG.trace(opHandle + ": The background operation was cancelled", e);
      } catch (ExecutionException e) {
        // The background operation thread was aborted
        LOG.trace(opHandle + ": The background operation was aborted", e);
      } catch (InterruptedException e) {
        // No op, this thread was interrupted
        // In this case, the call might return sooner than long polling timeout
      }
    }
    OperationStatus opStatus = operation.getStatus();
    LOG.debug(opHandle + ": getOperationStatus()");
    sessionManager.clearIpAddress();
    return opStatus;
  }

  /* (non-Javadoc)
   * @see org.apache.hive.service.cli.ICLIService#cancelOperation(org.apache.hive.service.cli.OperationHandle)
   */
  @Override
  public void cancelOperation(OperationHandle opHandle)
      throws HiveSQLException {
    startLogCapture(opHandle);
    sessionManager.getOperationManager().getOperation(opHandle)
    .getParentSession().cancelOperation(opHandle);
    LOG.debug(opHandle + ": cancelOperation()");
    sessionManager.clearIpAddress();
    sessionManager.getLogManager().destroyOperationLog(opHandle);
    stopLogCapture();
  }

  /* (non-Javadoc)
   * @see org.apache.hive.service.cli.ICLIService#closeOperation(org.apache.hive.service.cli.OperationHandle)
   */
  @Override
  public void closeOperation(OperationHandle opHandle)
      throws HiveSQLException {
    startLogCapture(opHandle);
    sessionManager.getOperationManager().getOperation(opHandle)
    .getParentSession().closeOperation(opHandle);
    sessionManager.clearIpAddress();
    LOG.debug(opHandle + ": closeOperation");
    sessionManager.getLogManager().destroyOperationLog(opHandle);
    stopLogCapture();
  }

  /* (non-Javadoc)
   * @see org.apache.hive.service.cli.ICLIService#getResultSetMetadata(org.apache.hive.service.cli.OperationHandle)
   */
  @Override
  public TableSchema getResultSetMetadata(OperationHandle opHandle)
      throws HiveSQLException {
    startLogCapture(opHandle);
    TableSchema tableSchema = sessionManager.getOperationManager()
        .getOperation(opHandle).getParentSession().getResultSetMetadata(opHandle);
    LOG.debug(opHandle + ": getResultSetMetadata()");
    stopLogCapture();
    sessionManager.clearIpAddress();
    return tableSchema;
  }

  /* (non-Javadoc)
   * @see org.apache.hive.service.cli.ICLIService#fetchResults(org.apache.hive.service.cli.OperationHandle, org.apache.hive.service.cli.FetchOrientation, long)
   */
  @Override
  public RowSet fetchResults(OperationHandle opHandle, FetchOrientation orientation, long maxRows)
      throws HiveSQLException {
    startLogCapture(opHandle);
    RowSet rowSet = sessionManager.getOperationManager().getOperation(opHandle)
        .getParentSession().fetchResults(opHandle, orientation, maxRows);
    LOG.debug(opHandle + ": fetchResults()");
    stopLogCapture();
    sessionManager.clearIpAddress();
    return rowSet;
  }

  /* (non-Javadoc)
   * @see org.apache.hive.service.cli.ICLIService#fetchResults(org.apache.hive.service.cli.OperationHandle)
   */
  @Override
  public RowSet fetchResults(OperationHandle opHandle)
      throws HiveSQLException {
    startLogCapture(opHandle);
    RowSet rowSet = sessionManager.getOperationManager().getOperation(opHandle)
        .getParentSession().fetchResults(opHandle);
    LOG.debug(opHandle + ": fetchResults()");
    stopLogCapture();
    sessionManager.clearIpAddress();
    return rowSet;
  }

  // obtain delegation token for the give user from metastore
  public synchronized String getDelegationTokenFromMetaStore(String owner)
      throws HiveSQLException, UnsupportedOperationException, LoginException, IOException {
    if (!hiveConf.getBoolVar(HiveConf.ConfVars.METASTORE_USE_THRIFT_SASL) ||
        !hiveConf.getBoolVar(HiveConf.ConfVars.HIVE_SERVER2_ENABLE_DOAS)) {
      throw new UnsupportedOperationException(
          "delegation token is can only be obtained for a secure remote metastore");
    }

    try {
      Hive.closeCurrent();
      return Hive.get(hiveConf).getDelegationToken(owner, owner);
    } catch (HiveException e) {
      if (e.getCause() instanceof UnsupportedOperationException) {
        throw (UnsupportedOperationException)e.getCause();
      } else {
        throw new HiveSQLException("Error connect metastore to setup impersonation", e);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.apache.hive.service.cli.ICLIService#getLog(org.apache.hive.service.cli.OperationHandle)
   */
   @Override
   public String getLog(OperationHandle opHandle)
      throws HiveSQLException {
     OperationLog log = sessionManager.getLogManager().getOperationLogByOperation(opHandle, false);
     LOG.info(opHandle  + ": getLog()");
     return log.readOperationLog();
   }

  private void startLogCapture(OperationHandle operationHandle) throws HiveSQLException {
    sessionManager.getLogManager().unregisterCurrentThread();
    sessionManager.getLogManager().registerCurrentThread(operationHandle);
  }

  private void stopLogCapture() {
    sessionManager.getLogManager().unregisterCurrentThread();
  }

  @Override
  public String getDelegationToken(SessionHandle sessionHandle, HiveAuthFactory authFactory,
      String owner, String renewer) throws HiveSQLException {
    String delegationToken = sessionManager.getSession(sessionHandle).
        getDelegationToken(authFactory, owner, renewer);
    LOG.info(sessionHandle  + ": getDelegationToken()");
    return delegationToken;
  }

  @Override
  public void cancelDelegationToken(SessionHandle sessionHandle, HiveAuthFactory authFactory,
      String tokenStr) throws HiveSQLException {
    sessionManager.getSession(sessionHandle).
    cancelDelegationToken(authFactory, tokenStr);
    LOG.info(sessionHandle  + ": cancelDelegationToken()");
  }

  @Override
  public void renewDelegationToken(SessionHandle sessionHandle, HiveAuthFactory authFactory,
      String tokenStr) throws HiveSQLException {
    sessionManager.getSession(sessionHandle).renewDelegationToken(authFactory, tokenStr);
    LOG.info(sessionHandle  + ": renewDelegationToken()");
  }

  public void setIpAddress(SessionHandle sessionHandle, String ipAddress) {
      try {
          HiveSession session = sessionManager.getSession(sessionHandle);
          session.setIpAddress(ipAddress);
      } catch (HiveSQLException e) {
          // This should not happen
          LOG.error("Unable to get session to set ipAddress", e);
      }
  }
}

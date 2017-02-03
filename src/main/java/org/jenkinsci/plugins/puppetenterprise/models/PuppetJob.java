package org.jenkinsci.plugins.puppetenterprise.models;

import java.io.*;
import java.util.*;
import org.jenkinsci.plugins.puppetenterprise.apimanagers.puppetorchestratorv1.PuppetOrchestratorException;
import org.jenkinsci.plugins.puppetenterprise.apimanagers.puppetorchestratorv1.PuppetCommandDeployV1;
import org.jenkinsci.plugins.puppetenterprise.apimanagers.puppetorchestratorv1.PuppetJobsIDV1;
import org.jenkinsci.plugins.puppetenterprise.apimanagers.puppetorchestratorv1.puppetjobreportv1.*;
import org.jenkinsci.plugins.puppetenterprise.apimanagers.puppetorchestratorv1.puppetnodev1.*;
import org.jenkinsci.plugins.puppetenterprise.apimanagers.PERequest;
import org.jenkinsci.plugins.puppetenterprise.models.UnknownPuppetJobReportType;
import com.google.gson.internal.LinkedTreeMap;

public class PuppetJob implements Serializable {
  private String state = null;
  private String name = null;
  private String token = null;
  private ArrayList<PuppetNodeItemV1> nodes = null;
  private ArrayList<PuppetJobReportNodeV1> report = null;
  private Integer nodeCount = null;
  private LinkedTreeMap scope = new LinkedTreeMap();
  private String target = null;
  private String environment = null;
  private Integer concurrency = null;
  private Boolean enforceEnvironment = null;
  private Boolean debug = null;
  private Boolean trace = null;
  private Boolean noop = null;
  private Boolean evalTrace = null;
  private PuppetJobsIDV1 job = null;
  private PrintStream logger = null;

  public PuppetJob() { }

  public void setScope(String application, ArrayList nodes, String query) {
    this.scope.put("application", application);
    this.scope.put("nodes", nodes);
    this.scope.put("query", query);
  }

  public void setLogger(PrintStream logger) {
    this.logger = logger;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public void setConcurrency(Integer concurrency) {
    this.concurrency = concurrency;
  }

  public void setEnforceEnvironment(Boolean enforcement) {
    this.enforceEnvironment = enforcement;
  }

  public void setDebug(Boolean debug) {
    this.debug = debug;
  }

  public void setTrace(Boolean trace) {
    this.trace = trace;
  }

  public void setNoop(Boolean noop) {
    this.noop = noop;
  }

  public void setEvalTrace(Boolean evalTrace) {
    this.evalTrace = evalTrace;
  }

  public String getState() {
    return this.state;
  }

  public String getName() {
    return this.name;
  }

  public void run() throws PuppetOrchestratorException, Exception {
    start();

    do {
      try {
        Thread.sleep(500);
      } catch(InterruptedException ex) {
        Thread.currentThread().interrupt();
      }

      updateState();
    } while(isRunning());

    updateNodes();
    updateReport();
  }

  public void start() throws PuppetOrchestratorException, Exception {
    PuppetCommandDeployV1 deployCommand = new PuppetCommandDeployV1();

    if (this.scope.isEmpty() && this.target != null) {
      deployCommand.setTarget(this.target);
    } else {
      deployCommand.setScope((String) this.scope.get("application"),
        (ArrayList<PuppetNodeItemV1>) this.scope.get("nodes"),
        (String) this.scope.get("query"));
    }

    deployCommand.setConcurrency(this.concurrency);
    deployCommand.setEnvironment(this.environment);
    deployCommand.setToken(this.token);
    deployCommand.setEnforceEnvironment(this.enforceEnvironment);
    deployCommand.setDebug(this.debug);
    deployCommand.setTrace(this.trace);
    deployCommand.setNoop(this.noop);
    deployCommand.setEvalTrace(this.evalTrace);
    deployCommand.execute();

    this.name = deployCommand.getName();
    this.job = new PuppetJobsIDV1(this.name); //Create the JobID object

    this.logger.println("Successfully started Puppet job " + this.name);
  }

  public void stop() throws PuppetOrchestratorException, Exception {
    //TODO: Add ability to stop a running job
    updateNodes();
    updateReport();
  }

  public Boolean failed() {
    return (this.state.equals("failed"));
  }

  public Boolean stopped() {
    return (this.state.equals("stopped"));
  }

  public Boolean isRunning() {
    return (!this.state.equals("finished") && !this.state.equals("stopped") && !this.state.equals("failed"));
  }

  public Integer getNodeCount() {
    return this.nodeCount;
  }

  public LinkedTreeMap getScope() {
    return this.scope;
  }

  public String getTarget() {
    return this.target;
  }

  public String getEnvironment() {
    return this.environment;
  }

  public Integer getConcurrency() {
    return this.concurrency;
  }

  public Boolean getEnforceEnvironment() {
    return this.enforceEnvironment;
  }

  public Boolean getDebug() {
    return this.debug;
  }

  public Boolean getTrace() {
    return this.trace;
  }

  public Boolean getNoop() {
    return this.noop;
  }

  public Boolean getEvalTrace() {
    return this.evalTrace;
  }

  public PuppetJobReport generateRunReport() {
    return new PuppetJobReport(this);
  }

  public void updateState() throws PuppetOrchestratorException, Exception {
    this.job.setToken(this.token);
    this.job.execute();

    this.state = this.job.getState();
    this.nodeCount = this.job.getNodeCount();
  }

  public void update() throws PuppetOrchestratorException, Exception {
    updateState();
    updateNodes();
    updateReport();
  }

  public ArrayList<PuppetNodeItemV1> getNodes() {
    return this.nodes;
  }

  public ArrayList<PuppetJobReportNodeV1> getNodeReports() {
    return this.report;
  }

  public String generateReport(ArrayList<String> reports) throws UnknownPuppetJobReportType {
    PuppetJobReport report = new PuppetJobReport(this);
    report.setReports(reports);
    return report.generateReport();
  }

  private void updateNodes() throws PuppetOrchestratorException, Exception {
    this.nodes = this.job.getNodes();
  }

  private void updateReport() throws PuppetOrchestratorException, Exception {
    this.report = this.job.getReport();
  }
}

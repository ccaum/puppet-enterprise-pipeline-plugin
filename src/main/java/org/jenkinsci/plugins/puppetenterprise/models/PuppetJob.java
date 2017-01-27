package org.jenkinsci.plugins.puppetenterprise.models;

import java.io.*;
import java.util.*;
import org.jenkinsci.plugins.puppetenterprise.apimanagers.puppetorchestratorv1.PuppetOrchestratorException;
import org.jenkinsci.plugins.puppetenterprise.apimanagers.puppetorchestratorv1.PuppetCommandDeployV1;
import org.jenkinsci.plugins.puppetenterprise.apimanagers.puppetorchestratorv1.PuppetJobsIDV1;
import org.jenkinsci.plugins.puppetenterprise.apimanagers.puppetorchestratorv1.puppetjobreportv1.*;
import org.jenkinsci.plugins.puppetenterprise.apimanagers.puppetorchestratorv1.puppetnodev1.*;
import org.jenkinsci.plugins.puppetenterprise.apimanagers.PERequest;
import com.google.gson.internal.LinkedTreeMap;

public class PuppetJob {
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

  private void updateNodes() throws PuppetOrchestratorException, Exception {
    this.nodes = this.job.getNodes();
  }

  private void updateReport() throws PuppetOrchestratorException, Exception {
    this.report = this.job.getReport();
  }

  private Boolean isEnvironmentEnforced() {
    //The orchestrator defaults to true if null, so null is true
    return (this.enforceEnvironment == null || this.enforceEnvironment);
  }

  public String formatReport() {
    StringBuilder formattedReport = new StringBuilder();

    formattedReport.append("Puppet Job Name: " + this.name + "\n");
    formattedReport.append("State: " + this.state + "\n");

    if (!isEnvironmentEnforced()) {
      formattedReport.append("Environment: node's assigned environment\n");
    } else {
      formattedReport.append("Environment: " + this.environment + "\n");
    }

    formattedReport.append("Nodes: " + this.nodeCount + "\n\n");

    for (PuppetNodeItemV1 node : this.nodes) {
      formattedReport.append(node.getName() + "\n");

      if (node.getEnvironment() != null && !isEnvironmentEnforced()) {
        formattedReport.append("  Environment: " + node.getEnvironment() + "\n");
      }

      if (node.getMetrics() != null) {
        PuppetNodeMetricsV1 metrics = node.getMetrics();

        formattedReport.append("  Resource Events: ");
        formattedReport.append(metrics.getFailed().toString() + " failed   ");
        formattedReport.append(metrics.getChanged().toString() + " changed   ");

        //PE versions prior to 2016.4 do not include corrective changes
        if (metrics.getCorrectiveChanged() != null) {
          formattedReport.append(metrics.getCorrectiveChanged().toString() + " corrective   ");
        }

        formattedReport.append(metrics.getSkipped().toString() + " skipped    ");
        formattedReport.append("\n");

        formattedReport.append("  Report URL: " + node.getReportURL().toString() + "\n");
        formattedReport.append("\n");

      } else {
        //There's always a message, but it's only useful if the run was not able to take place,
        //  which we'll know if there are no metrics.
        if (node.getMessage() != null) {
          formattedReport.append("  " + node.getMessage() + "\n");
          formattedReport.append("\n");
        }
      }
    }

    return formattedReport.toString();
  }
}

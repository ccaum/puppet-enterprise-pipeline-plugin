package org.jenkinsci.plugins.puppetenterprise.models;

import java.io.*;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.puppetenterprise.apimanagers.puppetorchestratorv1.puppetjobreportv1.*;
import org.jenkinsci.plugins.puppetenterprise.models.PuppetJob;
import com.google.gson.internal.LinkedTreeMap;

public class PuppetJobReport implements Serializable {
  private String name = null;
  private String state = null;
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
  private ArrayList<PuppetJobReportNodeV1> nodeReports = null;

  public PuppetJobReport(PuppetJob job) {
    this.nodeReports = job.getNodeReports();
    this.name = job.getName();
    this.state = job.getState();
    this.nodeCount = job.getNodeCount();
    this.scope = job.getScope();
    this.target = job.getTarget();
    this.environment = job.getEnvironment();
    this.concurrency = job.getConcurrency();
    this.enforceEnvironment = job.getEnforceEnvironment();
    this.debug = job.getDebug();
    this.trace = job.getTrace();
    this.evalTrace = job.getEvalTrace();
  }

  public String getName() {
    return this.name;
  }

  public String getState() {
    return this.state;
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

  public String formatResourcesReport() {
    StringBuilder formattedReport = new StringBuilder();

    formattedReport.append("Resources with changes:\n\n");

    for (PuppetResource reportResource : collectResourceEvents()) {
      for (PuppetJobReportNodeEventV1 event: reportResource.getEvents()) {
        formattedReport.append("    " + event.getResourceName()  + "\n");
        formattedReport.append("      Certname: " + event.getCertname() + "\n");
        formattedReport.append("      Property: " + event.getProperty() + "\n");
        formattedReport.append("      Old value: " + event.getOldValue() + "\n");
        formattedReport.append("      New value: " + event.getNewValue() + "\n");
        formattedReport.append("      Message: " + event.getMessage() + "\n\n");
      }
    }

    return formattedReport.toString();
  }

  public String formatNodesReport() {
    StringBuilder formattedReport = new StringBuilder();

    formattedReport.append("Nodes with changes:\n\n");

    for (PuppetJobReportNodeV1 reportnode : this.nodeReports) {
      formattedReport.append("  " + reportnode.getNode() + "\n");

      if (reportnode.getEvents().size() > 0) {
        for (PuppetJobReportNodeEventV1 event: reportnode.getEvents()) {
          formattedReport.append("    " + event.getResourceName()  + "\n");
          formattedReport.append("      Property: " + event.getProperty() + "\n");
          formattedReport.append("      Old value: " + event.getOldValue() + "\n");
          formattedReport.append("      New value: " + event.getNewValue() + "\n");
          formattedReport.append("      Message: " + event.getMessage() + "\n\n");
        }
      } else {
        formattedReport.append("    0 resource events");
      }
    }

    return formattedReport.toString();
  }

  private ArrayList<PuppetResource> collectResourceEvents() {
    ArrayList<PuppetResource> resources = new ArrayList();

    for (PuppetJobReportNodeV1 reportnode: this.nodeReports) {
      for (PuppetJobReportNodeEventV1 nodeEvent : reportnode.getEvents()) {
        PuppetResource resource = new PuppetResource(nodeEvent.getResourceName());

        if (resources.contains(resource)) {
          resources.get(resources.indexOf(resource)).addEvent(nodeEvent);
        }
      }
    }

    return resources;
  }

  class PuppetResource implements Serializable {
    private ArrayList<PuppetJobReportNodeEventV1> events = new ArrayList();
    private String name = null;

    public PuppetResource(String name) {
      this.name = name;
    }

    public String getName() {
      return this.name;
    }

    public ArrayList<PuppetJobReportNodeEventV1> getEvents() {
      return this.events;
    }

    public void addEvent(PuppetJobReportNodeEventV1 event) {
      events.add(event);
    }
  }
}

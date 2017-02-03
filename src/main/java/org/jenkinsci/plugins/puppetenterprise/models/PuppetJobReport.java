package org.jenkinsci.plugins.puppetenterprise.models;

import java.io.*;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.puppetenterprise.apimanagers.puppetorchestratorv1.puppetnodev1.*;
import org.jenkinsci.plugins.puppetenterprise.apimanagers.puppetorchestratorv1.puppetjobreportv1.*;
import org.jenkinsci.plugins.puppetenterprise.models.PuppetJob;
import org.jenkinsci.plugins.puppetenterprise.models.UnknownPuppetJobReportType;
import com.google.gson.internal.LinkedTreeMap;

public class PuppetJobReport implements Serializable {
  private ArrayList<String> reportTypes = new ArrayList<String>();
  private PuppetJob job = null;

  public PuppetJobReport(PuppetJob job) {
    this.job = job;
  }

  public void setReports(ArrayList<String> reports) {
    if (reports != null) {
      this.reportTypes = reports;
    } else {
      //Default to nodeSummary
      this.reportTypes.add("nodeSummary");
    }
  }

  public String generateReport() throws UnknownPuppetJobReportType {
    StringBuilder formattedReport = new StringBuilder();

    formattedReport.append("Puppet Job Name: " + job.getName() + "\n");
    formattedReport.append("State: " + job.getState() + "\n");

    if (!isEnvironmentEnforced()) {
      formattedReport.append("Environment: node's assigned environment\n");
    } else {
      formattedReport.append("Environment: " + job.getEnvironment() + "\n");
    }

    formattedReport.append("Nodes: " + job.getNodeCount() + "\n\n");

    //Generate each of the requested reports
    for (String reportType : this.reportTypes) {
      switch (reportType) {
        case "nodeSummary": formattedReport.append(formatSummaryReport()); break;
        case "nodeChanges": formattedReport.append(formatNodesReport()); break;
        case "resourceChanges": formattedReport.append(formatResourcesReport()); break;
        default: throw new UnknownPuppetJobReportType("Cannot find report type: " + reportType);
      }

      formattedReport.append("\n\n");
    }

    return formattedReport.toString();
  }

  private String formatSummaryReport() {
    StringBuilder formattedReport = new StringBuilder();

    for (PuppetNodeItemV1 node : job.getNodes() ) {
      formattedReport.append(node.getName() + "\n");

      if (node.getEnvironment() != null && !isEnvironmentEnforced()) {
        formattedReport.append("  Environment: " + node.getEnvironment() + "\n");
      }

      //There will be no metrics if the run failed
      if (!node.getState().equals("failed")) {
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

  public String formatResourcesReport() {
    StringBuilder formattedReport = new StringBuilder();
    ArrayList<PuppetResource> resourceEvents = collectResourceEvents();

    formattedReport.append("Resources with changes:\n");
    formattedReport.append("-----------------------\n\n");

    if (resourceEvents.size() > 0) {
      for (PuppetResource reportResource : resourceEvents) {
        formattedReport.append("    " + reportResource.getName()  + "\n");

        for (PuppetJobReportNodeEventV1 event: reportResource.getEvents()) {
          formattedReport.append("      Certname:  " + event.getCertname() + "\n");
          formattedReport.append("      Property:  " + event.getProperty() + "\n");
          formattedReport.append("      Old value: " + event.getOldValue() + "\n");
          formattedReport.append("      New value: " + event.getNewValue() + "\n");
          formattedReport.append("      Message:  " + event.getMessage() + "\n\n");
        }
      }
    } else {
      formattedReport.append("  0 resource events");
    }

    return formattedReport.toString();
  }

  public String formatNodesReport() {
    StringBuilder formattedReport = new StringBuilder();

    formattedReport.append("Nodes with changes:\n");
    formattedReport.append("-------------------\n\n");

    for (PuppetJobReportNodeV1 reportnode : job.getNodeReports()) {
      formattedReport.append("  " + reportnode.getNode() + "\n");

      if (reportnode.getEvents().size() > 0) {
        for (PuppetJobReportNodeEventV1 event: reportnode.getEvents()) {
          formattedReport.append("    " + event.getResourceName()  + "\n");
          formattedReport.append("      Property:  " + event.getProperty() + "\n");
          formattedReport.append("      Old value: " + event.getOldValue() + "\n");
          formattedReport.append("      New value: " + event.getNewValue() + "\n");
          formattedReport.append("      Message:   " + event.getMessage() + "\n\n");
        }
      } else {
        formattedReport.append("    0 resource events");
      }
    }

    return formattedReport.toString();
  }

  //Returns -1 if not found
  private Integer containsResource(ArrayList<PuppetResource> resources, PuppetResource resource) {
    Integer index = -1;

    //Don't bother looking if there's nothing to look for
    if (resources.size() == 0) {
      return index;
    }

    for (Integer i = 0; i < resources.size(); i = i + 1) {
      if (resource.getName().equals(resources.get(i).getName())) {
        index = i;
      }
    }

    return index;
  }

  private ArrayList<PuppetResource> collectResourceEvents() {
    ArrayList<PuppetResource> resources = new ArrayList();

    for (PuppetJobReportNodeV1 reportnode: job.getNodeReports()) {
      for (PuppetJobReportNodeEventV1 nodeEvent : reportnode.getEvents()) {
        PuppetResource resource = new PuppetResource(nodeEvent.getResourceName());

        Integer resourceIndex = containsResource(resources, resource);
        if (resourceIndex >= 0) {
          resources.get(resourceIndex).addEvent(nodeEvent);
        } else {
          resource.addEvent(nodeEvent);
          resources.add(resource);
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

  private Boolean isEnvironmentEnforced() {
    //The orchestrator defaults to true if null, so null is true
    return (job.getEnforceEnvironment() == null || job.getEnforceEnvironment());
  }
}

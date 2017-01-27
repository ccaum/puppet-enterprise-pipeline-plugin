package org.jenkinsci.plugins.puppetenterprise.apimanagers.puppetorchestratorv1.puppetjobreportv1;

import java.io.*;
import java.util.*;
import java.util.Date;
import com.google.gson.internal.LinkedTreeMap;

public class PuppetJobReportNodeEventV1 implements Serializable {
  private String new_value = null;
  private String report = null;
  private Boolean corrective_change = null;
  private Date run_start_time = null;
  private String property = null;
  private String file = null;
  private String old_value = null;
  private String containing_class = null;
  private String line = null;
  private String resource_type = null;
  private String status = null;
  private String configuration_version = null;
  private String resource_title = null;
  private String environment = null;
  private Date timestamp = null;
  private Date run_end_time = null;
  private Date report_receive_time = null;
  private ArrayList<String> containment_path = null;
  private String certname = null;
  private String message = null;

  public String getResourceName() {
    String resource_type = (getResourceType().substring(0,1).toUpperCase() + getResourceType().substring(1));
    return (resource_type + "[" + getResourceTitle() + "]");
  }

  public String getNewValue() {
    return this.new_value;
  }

  public String getReport() {
    return this.report;
  }

  public Boolean getCorrectiveChange() {
    return this.corrective_change;
  }

  public Date getRunStartTime() {
    return this.run_start_time;
  }

  public String getProperty() {
    return this.property;
  }

  public String getFile() {
    return this.file;
  }

  public String getOldValue() {
    return this.old_value;
  }

  public String getContainingClass() {
    return this.containing_class;
  }

  public String getLine() {
    return this.line;
  }

  public String getResourceType() {
    return this.resource_type;
  }

  public String getStatus() {
    return this.status;
  }

  public String getConfigurationVersion() {
    return this.configuration_version;
  }

  public String getResourceTitle() {
    return this.resource_title;
  }

  public String getEnvironment() {
    return this.environment;
  }

  public Date getTimestamp() {
    return this.timestamp;
  }

  public Date getRunEndTime() {
    return this.run_end_time;
  }

  public Date getReportReceiveTime() {
    return this.report_receive_time;
  }

  public ArrayList<String> getContainmentPath() {
    return this.containment_path;
  }

  public String getCertname() {
    return this.certname;
  }

  public String getMessage() {
    return this.message;
  }
}

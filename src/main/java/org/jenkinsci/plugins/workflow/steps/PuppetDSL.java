package org.jenkinsci.plugins.workflow.steps;

import groovy.lang.Binding;
import hudson.Extension;
import java.io.IOException;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.ProxyWhitelist;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.StaticWhitelist;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;

@Extension public class PuppetDSL extends GlobalVariable {

  @Override public String getName() {
    return "puppet";
  }

  @Override public Object getValue(CpsScript script) throws Exception {
    Binding binding = script.getBinding();
    Object puppet;
    if (binding.hasVariable(getName())) {
      puppet = binding.getVariable(getName());
    } else {
      // Note that if this were a method rather than a constructor, we would need to mark it @NonCPS lest it throw CpsCallableInvocation.
      puppet = script.getClass().getClassLoader().loadClass("org.jenkinsci.plugins.workflow.Puppet").getConstructor(CpsScript.class).newInstance(script);
      binding.setVariable(getName(), puppet);
    }
    return puppet;
  }

  @Extension public static class MiscWhitelist extends ProxyWhitelist {
    public MiscWhitelist() throws IOException {
      super(new StaticWhitelist(
        "method groovy.lang.GroovyObject getProperty java.lang.String",
        "method java.lang.Class isInstance java.lang.Object",
        "method java.lang.Throwable getMessage",
        "staticMethod java.lang.System getenv java.lang.String",
        "method org.jenkinsci.plugins.puppetenterprise.models.PuppetJobReport formatResourcesReport",
        "method org.jenkinsci.plugins.puppetenterprise.models.PuppetJobReport formatNodesReport",
        "method org.jenkinsci.plugins.puppetenterprise.models.PuppetJobReport getName",
        "method org.jenkinsci.plugins.puppetenterprise.models.PuppetJobReport getState",
        "method org.jenkinsci.plugins.puppetenterprise.models.PuppetJobReport getNodeCount",
        "method org.jenkinsci.plugins.puppetenterprise.models.PuppetJobReport getScope",
        "method org.jenkinsci.plugins.puppetenterprise.models.PuppetJobReport getTarget",
        "method org.jenkinsci.plugins.puppetenterprise.models.PuppetJobReport getEnvironment",
        "method org.jenkinsci.plugins.puppetenterprise.models.PuppetJobReport getConcurrency",
        "method org.jenkinsci.plugins.puppetenterprise.models.PuppetJobReport getEnforceEnvironment",
        "method org.jenkinsci.plugins.puppetenterprise.models.PuppetJobReport getDebug",
        "method org.jenkinsci.plugins.puppetenterprise.models.PuppetJobReport getTrace",
        "method org.jenkinsci.plugins.puppetenterprise.models.PuppetJobReport getNoop",
        "method org.jenkinsci.plugins.puppetenterprise.models.PuppetJobReport getEvalTrace",
        "method groovy.lang.GroovyObject invokeMethod java.lang.String java.lang.Object"));
    }
  }
}

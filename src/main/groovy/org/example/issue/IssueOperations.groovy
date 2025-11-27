package org.example.issue

import com.atlassian.jira.bc.issue.IssueService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.config.ConstantsManager
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.issue.customfields.manager.OptionsManager
import com.atlassian.jira.issue.customfields.option.Option
import com.atlassian.jira.issue.customfields.option.Options
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.fields.config.FieldConfig
import com.atlassian.jira.issue.issuetype.IssueType
import com.atlassian.jira.issue.priority.Priority
import com.atlassian.jira.project.Project
import com.atlassian.jira.project.ProjectManager
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.user.util.UserManager
import groovy.transform.Field

import java.text.DateFormat
import java.text.SimpleDateFormat

@Field
CustomFieldManager cfm = ComponentAccessor.customFieldManager
@Field
CustomField textCF = cfm.getCustomFieldObjectsByName("Text test field")?.first()
@Field
CustomField numberCF = cfm.getCustomFieldObjectsByName("Number test field")?.first()
@Field
CustomField dateCF = cfm.getCustomFieldObjectsByName("Date test field")?.first()
@Field
CustomField singleOptionCF = cfm.getCustomFieldObjectsByName("Single select test field")?.first()
@Field
CustomField multiSelectCF = cfm.getCustomFieldObjectsByName("Multi select test field")?.first()
@Field
CustomField userCF = cfm.getCustomFieldObjectsByName("User test field")?.first()
@Field
CustomField labelsCF = cfm.getCustomFieldObjectsByName("Labels test field")?.first()

Issue createIssueWithIssueService() {
    IssueService issueService = ComponentAccessor.issueService
    ApplicationUser author = ComponentAccessor.jiraAuthenticationContext.loggedInUser
//    ApplicationUser admin = getUser("venam")
    IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
    issueInputParameters.setProjectId(getProject("TEST2")?.id)
            .setIssueTypeId(getIssueType("Task")?.id)
            .setPriorityId(getPriority("Low")?.id)
            .setSummary("Test issue")
            .setDescription("Test issue description")
            .setReporterId(getUser("venam")?.username)
    IssueService.CreateValidationResult ivr = issueService.validateCreate(author, issueInputParameters)
    if (ivr.valid) {
        IssueService.IssueResult ir = issueService.create(author, ivr)
        if (ir.valid) {
            return ir.issue
        } else {
            //log.error("Issue creating failed. Reasons (getErrorMessages): ${ir.errorCollection.errorMessages.join(" | ")}")
            //log.error("Issue creating failed. Reasons (getErrorMessages): ${ir.errorCollection.errors.collect { it.key + ":" + it.value }.join(" | ")}")
            log.error("Issue creating failed. Reasons (getErrorMessages): ${ir.errorCollection.errors.values().join(" | ")}")
            return null
        }
    } else {
        //log.error("Issue creating failed. Reasons (getErrorMessages): ${ivr.errorCollection.errorMessages.join(" | ")}")
//        log.error("Issue creating failed. Reasons (getErrorMessages): ${ivr.errorCollection.errors.collect { it.key + ":" + it.value }.join(" | ")}")
        log.error("Issue creating failed. Reasons (getErrorMessages): ${ivr.errorCollection.errors.values().join(" | ")}")
        return null
    }
}

Issue updateIssueWithIssueService(String issueKey) {
    IssueService issueService = ComponentAccessor.issueService
    ApplicationUser author = ComponentAccessor.jiraAuthenticationContext.loggedInUser
    DateFormat df = new SimpleDateFormat("d/MMM/yy")
    Date date = new Date()
    IssueManager im = ComponentAccessor.issueManager
    Issue issue = im.getIssueByKeyIgnoreCase(issueKey)
    IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
            .setPriorityId(getPriority("High")?.id)
            .setSummary("New Summary")
            .addCustomFieldValue(textCF.id, "Text cf value")
            .addCustomFieldValue(numberCF.id, 123.45d.toString())
            .addCustomFieldValue(dateCF.id, df.format(date))
            .addCustomFieldValue(singleOptionCF.id, getOptions(issue, singleOptionCF, ["1"]).first().optionId.toString())
    List<String> optionIds = getOptions(issue, multiSelectCF, ["1", "2"])
            .collect { it.optionId.toString() }
    issueInputParameters.addCustomFieldValue(multiSelectCF.id, optionIds.toArray(new String[optionIds.size()]))
            .addCustomFieldValue(userCF.id, getUser("venam").username)
            .addCustomFieldValue(labelsCF.id, "123", "456", "789")
    IssueService.UpdateValidationResult uvr = issueService.validateUpdate(author, issue.id, issueInputParameters)
    if (uvr.valid) {
        IssueService.IssueResult ir = issueService.update(author, uvr)
        if (ir.valid) {
            return ir.issue
        } else {
            //log.error("Issue creating failed. Reasons (getErrorMessages): ${ir.errorCollection.errorMessages.join(" | ")}")
            //log.error("Issue creating failed. Reasons (getErrorMessages): ${ir.errorCollection.errors.collect { it.key + ":" + it.value }.join(" | ")}")
            log.error("Issue updating failed. Reasons (getErrorMessages): ${ir.errorCollection.errors.values().join(" | ")}")
            return null
        }
    } else {
        //log.error("Issue creating failed. Reasons (getErrorMessages): ${ivr.errorCollection.errorMessages.join(" | ")}")
//        log.error("Issue creating failed. Reasons (getErrorMessages): ${ivr.errorCollection.errors.collect { it.key + ":" + it.value }.join(" | ")}")
        log.error("Issue updating failed. Reasons (getErrorMessages): ${uvr.errorCollection.errors.values().join(" | ")}")
        return null
    }
}

Issue editIssueWithIssueService(String issueKey) {
    IssueService issueService = ComponentAccessor.issueService
    ApplicationUser author = ComponentAccessor.jiraAuthenticationContext.loggedInUser
    IssueManager im = ComponentAccessor.issueManager
    Issue issue = im.getIssueByKeyIgnoreCase(issueKey)
    IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
    List<String> optionIds = getOptions(issue, multiSelectCF, ["3"])
            .collect { it.optionId.toString() }
    issueInputParameters.addCustomFieldValue(multiSelectCF.id, optionIds.toArray(new String[optionIds.size()]))
    IssueService.UpdateValidationResult uvr = issueService.validateUpdate(author, issue.id, issueInputParameters)
    if (uvr.valid) {
        IssueService.IssueResult ir = issueService.update(author, uvr)
        if (ir.valid) {
            return ir.issue
        } else {
            //log.error("Issue creating failed. Reasons (getErrorMessages): ${ir.errorCollection.errorMessages.join(" | ")}")
            //log.error("Issue creating failed. Reasons (getErrorMessages): ${ir.errorCollection.errors.collect { it.key + ":" + it.value }.join(" | ")}")
            log.error("Issue updating failed. Reasons (getErrorMessages): ${ir.errorCollection.errors.values().join(" | ")}")
            return null
        }
    } else {
        //log.error("Issue creating failed. Reasons (getErrorMessages): ${ivr.errorCollection.errorMessages.join(" | ")}")
//        log.error("Issue creating failed. Reasons (getErrorMessages): ${ivr.errorCollection.errors.collect { it.key + ":" + it.value }.join(" | ")}")
        log.error("Issue updating failed. Reasons (getErrorMessages): ${uvr.errorCollection.errors.values().join(" | ")}")
        return null
    }
}



Project getProject(String key) {
    ProjectManager projectManager = ComponentAccessor.projectManager
    return projectManager.getProjectByCurrentKeyIgnoreCase(key)
}

IssueType getIssueType(String name) {
    ConstantsManager cm = ComponentAccessor.constantsManager
    return cm.getAllIssueTypeObjects().find { it.name.equalsIgnoreCase(name) }
}

Priority getPriority(String name) {
    return ComponentAccessor.constantsManager.getPriorities().find { it.name.equalsIgnoreCase(name) }
}

ApplicationUser getUser(String username) {
    UserManager userManager = ComponentAccessor.userManager
    userManager.getUserByName(username)
}

List<Option> getOptions(Issue issue, CustomField cf, List<String> options) {
    FieldConfig fc = cf.getRelevantConfig(issue)
    OptionsManager om = ComponentAccessor.optionsManager
    Options o = om.getOptions(fc)
    return o.findAll { it.value in options }
}

Issue issue = createIssueWithIssueService()
if (issue) {
    updateIssueWithIssueService(issue.key)
    editIssueWithIssueService(issue.key)
    log.error "Issue: ${issue?.key}"
}

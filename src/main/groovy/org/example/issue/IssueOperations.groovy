package org.example.issue

import com.atlassian.jira.bc.issue.IssueService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.config.ConstantsManager
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.issue.customfields.manager.OptionsManager
import com.atlassian.jira.issue.customfields.option.Option
import com.atlassian.jira.issue.customfields.option.Options
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.fields.config.FieldConfig
import com.atlassian.jira.issue.index.IssueIndexingService
import com.atlassian.jira.issue.issuetype.IssueType
import com.atlassian.jira.issue.label.Label
import com.atlassian.jira.issue.label.LabelManager
import com.atlassian.jira.issue.priority.Priority
import com.atlassian.jira.project.Project
import com.atlassian.jira.project.ProjectManager
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.user.util.UserManager
import com.atlassian.jira.util.ErrorCollection
import com.atlassian.jira.util.ImportUtils
import com.atlassian.jira.workflow.IssueWorkflowManager
import com.opensymphony.workflow.loader.ActionDescriptor
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

//https://community.atlassian.com/forums/Agile-articles/Three-ways-to-update-an-issue-in-Jira-Java-Api/ba-p/736585

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

void deleteIssueWithIssueService(String key) {
    IssueService issueService = ComponentAccessor.issueService
    ApplicationUser user = ComponentAccessor.jiraAuthenticationContext.loggedInUser
    Issue issue = ComponentAccessor.issueManager.getIssueByKeyIgnoreCase(key)
    IssueService.DeleteValidationResult vr = issueService.validateDelete(user, issue.id)
    if (vr.valid) {
        ErrorCollection er = issueService.delete(user, vr)
        if (er.hasAnyErrors()) {
            log.error("Issue updating failed. Reasons (getErrorMessages): ${er.errors.values().join(" | ")}")
        } else {
            log.error "Issue deleted"
        }
    } else {
        //log.error("Issue creating failed. Reasons (getErrorMessages): ${ivr.errorCollection.errorMessages.join(" | ")}")
//        log.error("Issue creating failed. Reasons (getErrorMessages): ${ivr.errorCollection.errors.collect { it.key + ":" + it.value }.join(" | ")}")
        log.error("Issue updating failed. Reasons (getErrorMessages): ${vr.errorCollection.errors.values().join(" | ")}")
    }
}

void deleteIssueWithIssueManager(String key) {
    IssueManager issueManager = ComponentAccessor.issueManager
    ApplicationUser user = ComponentAccessor.jiraAuthenticationContext.loggedInUser
    MutableIssue mutableIssue = issueManager.getIssueByKeyIgnoreCase(key)
    issueManager.deleteIssue(user, mutableIssue, EventDispatchOption.ISSUE_DELETED, false)
}

Issue updateIssueWithIssueManager(String key) {
    IssueManager issueManager = ComponentAccessor.issueManager
    ApplicationUser user = ComponentAccessor.jiraAuthenticationContext.loggedInUser
    MutableIssue mutableIssue = issueManager.getIssueByKeyIgnoreCase(key)
    mutableIssue.setPriority(getPriority("High"))
    mutableIssue.setAssignee(getUser("venam"))
    mutableIssue.setSummary("New summary")
    mutableIssue.setCustomFieldValue(textCF, "111111111")
    mutableIssue.setCustomFieldValue(numberCF, 345d)
    mutableIssue.setCustomFieldValue(dateCF, new java.sql.Date(new Date().getTime()))
    mutableIssue.setCustomFieldValue(singleOptionCF, getOptions(mutableIssue, singleOptionCF, ["1"])?.first())
    mutableIssue.setCustomFieldValue(multiSelectCF, getOptions(mutableIssue, multiSelectCF, ["1", "2", "5"]))
    mutableIssue.setCustomFieldValue(userCF, getUser("venam"))
    mutableIssue.setCustomFieldValue(labelsCF, [new Label(null, mutableIssue.id, labelsCF.getIdAsLong(), "mylabel"),
                                                new Label(null, mutableIssue.id, labelsCF.getIdAsLong(), "mylabel_2")] as Set)
    issueManager.updateIssue(user, mutableIssue,  EventDispatchOption.DO_NOT_DISPATCH, false)
}

Label updateIssueAddLabel(String issueKey, String label) {
    IssueManager issueManager = ComponentAccessor.issueManager
    ApplicationUser user = ComponentAccessor.jiraAuthenticationContext.loggedInUser
    Issue issue = issueManager.getIssueByKeyIgnoreCase(issueKey)
    LabelManager labelManager = ComponentAccessor.getComponent(LabelManager)
    labelManager.addLabel(user, issue.id, label, false)
}

void transitionIssue(String issueKey, int transitionId) {
    IssueService issueService = ComponentAccessor.issueService
    ApplicationUser user = ComponentAccessor.jiraAuthenticationContext.loggedInUser
    Issue issue = ComponentAccessor.issueManager.getIssueByKeyIgnoreCase(issueKey)
    ActionDescriptor descriptor = getActionById(issue, user, transitionId)
    if (descriptor) {
        IssueService.TransitionValidationResult vr = issueService.validateTransition(user, issue.id, descriptor.id, issueService.newIssueInputParameters())
        if (vr.valid) {
            IssueService.IssueResult ir = issueService.transition(user, vr)
            if (ir.valid) {
                log.error "Success"
            } else {
                log.error("Issue updating failed. Reasons (getErrorMessages): ${ir.errorCollection.errors.values().join(" | ")}")
            }
        } else {
            log.error("Issue updating failed. Reasons (getErrorMessages): ${vr.errorCollection.errors.values().join(" | ")}")
        }
    }
}

ActionDescriptor getActionById(Issue issue, ApplicationUser user, Integer transitionId) {
    IssueWorkflowManager issueWorkflowManager = ComponentAccessor.getComponent(IssueWorkflowManager)
    List<ActionDescriptor> descriptors = issueWorkflowManager.getAvailableActions(issue, user)
    return descriptors.find {it.id.equals(transitionId)}
}

Date buildDate(int year, int month, int day, int hour, int minute, int second) {
    Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+03:00"))
    c.setTime(new Date())
    c.set(Calendar.YEAR, year)
    c.set(Calendar.MONTH, month)
    c.set(Calendar.DAY_OF_MONTH, day)
    c.set(Calendar.HOUR_OF_DAY, hour)
    c.set(Calendar.MINUTE, minute)
    c.set(Calendar.SECOND, second)
    return c.getTime()
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

void reindexIssue(String issueKey) {
    Issue issue = ComponentAccessor.issueManager.getIssueByKeyIgnoreCase(issueKey)
    boolean indexing = ImportUtils.isIndexIssues()
    try {
        ImportUtils.setIndexIssues(true)
        IssueIndexingService issueIndexingService = ComponentAccessor.getComponent(IssueIndexingService)
        issueIndexingService.reIndex(issue)
    } finally {
        ImportUtils.setIndexIssues(indexing)
    }
}
updateIssueAddLabel("TEST2-24", "newlabel")
/*Issue issue = createIssueWithIssueService()
if (issue) {
    updateIssueWithIssueService(issue.key)
    editIssueWithIssueService(issue.key)
    log.error "Issue: ${issue?.key}"
}*/
//updateIssueWithIssueManager("TEST2-24")
//updateIssueAddLabel("TEST2-24", "newlabel")
//deleteIssueWithIssueService("TEST2-16")
//deleteIssueWithIssueManager("TEST2-17")
//transitionIssue("TEST2-24", 21)
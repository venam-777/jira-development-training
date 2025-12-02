package org.example.issue

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.worklog.Worklog
import com.atlassian.jira.issue.worklog.WorklogImpl
import com.atlassian.jira.issue.worklog.WorklogManager
import com.atlassian.jira.user.ApplicationUser

Worklog createWorklog(Issue issue, Long timeSpent, String comment) {
    WorklogManager worklogManager = ComponentAccessor.worklogManager
    ApplicationUser user = ComponentAccessor.jiraAuthenticationContext.loggedInUser
    Worklog worklog = new WorklogImpl(worklogManager, issue, null, user.key, comment, new Date(), null, null, timeSpent)
    return worklogManager.create(user, worklog, null, false)
}

Worklog createWorklog(Issue issue, String timeSpent, String comment) {
    WorklogManager worklogManager = ComponentAccessor.worklogManager
    ApplicationUser user = ComponentAccessor.jiraAuthenticationContext.loggedInUser
    Long time = ComponentAccessor.jiraDurationUtils.parseDuration(timeSpent, Locale.default)
    Worklog worklog = new WorklogImpl(worklogManager, issue, null, user.key, comment, new Date(), null, null, time)
    return worklogManager.create(user, worklog, null, false)
}

boolean deleteWorklog(Long worklogId) {
    WorklogManager worklogManager = ComponentAccessor.worklogManager
    ApplicationUser user = ComponentAccessor.jiraAuthenticationContext.loggedInUser
    Worklog worklog = worklogManager.getById(worklogId)
    worklogManager.delete(user, worklog, null, false)
}

//Issue issue = ComponentAccessor.issueManager.getIssueByKeyIgnoreCase("TEST2-24")
//Worklog worklog = createWorklog(issue, "2h 5m", "Ворклог на 2 часа 5 минут")
if (deleteWorklog(13505L)) {
    log.error "Success"
} else {
    log.error "Failed"
}
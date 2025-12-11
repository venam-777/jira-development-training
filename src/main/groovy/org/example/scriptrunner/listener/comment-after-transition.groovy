package org.example.scriptrunner.listener

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.issue.IssueEvent
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.comments.CommentManager
import com.atlassian.jira.user.ApplicationUser
import org.ofbiz.core.entity.GenericValue

import java.text.DateFormat
import java.text.SimpleDateFormat

IssueEvent event
Issue issue = event.issue
if (issue) {
    List<GenericValue> changeItems = event.changeLog.getRelated("ChildChangeItem")
    if (changeItems) {
        GenericValue statusChange = changeItems.find { it.getString("field") == "status" }
        if (statusChange) {
            String oldStatus = statusChange.getString("oldstring")
            String newStatus = statusChange.getString("newstring")
            ApplicationUser employee = ComponentAccessor.userManager.getUserByKey(event.changeLog.getString("author"))
            if (oldStatus != newStatus && newStatus.equalsIgnoreCase("Done")) {
                ApplicationUser admin = ComponentAccessor.userManager.getUserByName("venam")
                DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm")
                CommentManager commentManager = ComponentAccessor.commentManager
                String body = "[~${issue.reporter.username}], задача была решена ${df.format(new Date())} сотрудником [~${employee.username}]"
                commentManager.create(issue, admin, body, false)
            }
        }
    }
}
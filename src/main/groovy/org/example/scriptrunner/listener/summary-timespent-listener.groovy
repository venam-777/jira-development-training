package org.example.scriptrunner.listener

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.issue.IssueEvent
import com.atlassian.jira.event.issue.link.IssueLinkCreatedEvent
import com.atlassian.jira.event.issue.link.IssueLinkDeletedEvent
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.link.IssueLinkType
import com.atlassian.jira.user.ApplicationUser
import groovy.transform.Field

/**
 * Скрипт должен считать суммарное потраченное время по всем своим дочерним задачам + время самого родителя.
 * Время нужно округлить до часов (с округлением вверх)
 * 0.2h -> 1h
 * дочерние задачи - задачи, связанные ссылкой is child of
 * Скрипт может сработать как в родительской задаче, так и в дочерней
 * Примечание: скрипт должен корректно отрабатывать в том числе и при создании и удалении связей
 * Примечание 2: скрипт должен считать суммарное время только по задачам с определенными типами задач
 * Вектор связи: parent -- is parent of --> child
 */

//constants
@Field
final String PARENT_TYPE = "Improvement"
@Field
final List<String> CHILD_TYPES = ["bug", "task"]
@Field
final Long CF_ID = 11607L
@Field
final Long LINK_ID = 10500

Issue getParent(Issue child) {
    return ComponentAccessor.issueLinkManager.getInwardLinks(child.id)
            .findAll { it.issueLinkType.id == LINK_ID }
            .collect { it.sourceObject }
            .first()
}

List<Issue> getChildren(Issue parent) {
    return ComponentAccessor.issueLinkManager.getOutwardLinks(parent.id)
            .findAll { it.issueLinkType.id == LINK_ID }
            .collect { it.destinationObject }
            .findAll { CHILD_TYPES.contains( it.issueType.name.toLowerCase()) }
}

double calculateTimeSpent(Issue parent, List<Issue> children) {
    double timeSpent = parent.timeSpent ?: 0d
    if (children) {
        children.each { child ->
            timeSpent += child.timeSpent ?: 0d
        }
    }
    timeSpent = timeSpent / 3600d
    timeSpent = timeSpent > 0 && timeSpent != (double) Math.round(timeSpent) ? Math.round(timeSpent + 0.5d) : timeSpent
    return timeSpent
}

void updateParentIssue(Issue parent, ApplicationUser user, double timeSpent) {
    IssueManager issueManager = ComponentAccessor.issueManager
    MutableIssue issue = issueManager.getIssueObject(parent.id)
    CustomField cf = ComponentAccessor.customFieldManager.getCustomFieldObject(CF_ID)
    issue.setCustomFieldValue(cf, timeSpent)
    issueManager.updateIssue(user, issue, EventDispatchOption.DO_NOT_DISPATCH, false)
}

if (event instanceof IssueEvent) {
    Issue issue = event.issue
    if (issue.issueType.name.equalsIgnoreCase(PARENT_TYPE)) {
        List<Issue> children = getChildren(issue)
        double timeSpent = calculateTimeSpent(issue, children)
        updateParentIssue(issue, ComponentAccessor.jiraAuthenticationContext.loggedInUser, timeSpent)
    } else {
        Issue parent = getParent(issue)
        List<Issue> children = getChildren(parent)
        double timeSpent = calculateTimeSpent(parent, children)
        updateParentIssue(parent, ComponentAccessor.jiraAuthenticationContext.loggedInUser, timeSpent)
    }
} else if (event instanceof IssueLinkCreatedEvent || event instanceof IssueLinkDeletedEvent) {
    IssueLinkType linkType = event.issueLink.issueLinkType
    if (linkType.id == LINK_ID) {
        Issue parent = event.issueLink.sourceObject
        List<Issue> children = getChildren(parent)
        double timeSpent = calculateTimeSpent(parent, children)
        updateParentIssue(parent, ComponentAccessor.jiraAuthenticationContext.loggedInUser, timeSpent)
    }
}
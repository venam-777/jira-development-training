package org.example.issue

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.link.IssueLink
import com.atlassian.jira.issue.link.IssueLinkManager
import com.atlassian.jira.issue.link.IssueLinkType
import com.atlassian.jira.issue.link.IssueLinkTypeManager
import com.atlassian.jira.user.ApplicationUser

IssueLinkType getIssueLinkType(String name) {
    IssueLinkTypeManager linkTypeManager = ComponentAccessor.getComponent(IssueLinkTypeManager)
    return linkTypeManager.getIssueLinkTypes(true).findAll {
        it.name.equals(name)
    }?.first()
}

//A <- B
List<Issue> getInwardIssues(Issue issue, String linkName) {
    IssueLinkType type = getIssueLinkType(linkName)
    IssueLinkManager issueLinkManager = ComponentAccessor.issueLinkManager
    List<IssueLink> inwardLinks = issueLinkManager.getInwardLinks(issue.id)
    inwardLinks.findAll {
        it.issueLinkType.id == type.id
        //it.issueLinkType.name.equals(linkName)
    }.collect { it.sourceObject }
}

List<Issue> getOutwardIssues(Issue issue, String linkName) {
    IssueLinkType type = getIssueLinkType(linkName)
    IssueLinkManager issueLinkManager = ComponentAccessor.issueLinkManager
    List<IssueLink> outwardLinks = issueLinkManager.getOutwardLinks(issue.id)
    outwardLinks.findAll {
        it.issueLinkType.id == type.id
        //it.issueLinkType.name.equals(linkName)
    }.collect { it.destinationObject }
}

void createLink(Issue source, Issue destination, String linkName) {
    IssueLinkType type = getIssueLinkType(linkName)
    IssueLinkManager issueLinkManager = ComponentAccessor.issueLinkManager
    ApplicationUser user = ComponentAccessor.jiraAuthenticationContext.loggedInUser
    issueLinkManager.createIssueLink(source.id, destination.id, type.id, 1, user)
}

void deleteLink(Issue source, Issue target, String linkName) {
    IssueLinkType type = getIssueLinkType(linkName)
    IssueLinkManager issueLinkManager = ComponentAccessor.issueLinkManager
    IssueLink link = issueLinkManager.getIssueLink(source.id, target.id, type.id)
    if (link) {
        issueLinkManager.removeIssueLink(link, ComponentAccessor.jiraAuthenticationContext.loggedInUser)
    }
}

void deleteAllOutwardsLInks(Issue source, String linkName) {
    IssueLinkType type = getIssueLinkType(linkName)
    IssueLinkManager issueLinkManager = ComponentAccessor.issueLinkManager
    issueLinkManager.getOutwardLinks(source.id)
        .findAll {link -> link.linkTypeId == type.id}
        .each {
            issueLinkManager.removeIssueLink(it, ComponentAccessor.jiraAuthenticationContext.loggedInUser)
        }
}

final String linkName = "Влияет на выполнение КР"
Issue source = ComponentAccessor.issueManager.getIssueByKeyIgnoreCase("TEST2-24")
Issue destination = ComponentAccessor.issueManager.getIssueByKeyIgnoreCase("TEST2-23")
/*createLink(source, destination, linkName)
getInwardIssues(source, linkName).each {
    log.error it.key
}*/
//deleteLink(source, destination, linkName)
//createLink(source, destination, linkName)
deleteAllOutwardsLInks(source, linkName)
package org.example.issue

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.issue.comments.CommentManager
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.util.json.JSONObject

com.atlassian.jira.issue.comments.Comment createComment(String issueKey, String text) {
    CommentManager commentManager = ComponentAccessor.commentManager
    ApplicationUser user = ComponentAccessor.jiraAuthenticationContext.loggedInUser
    Issue issue = ComponentAccessor.issueManager.getIssueByKeyIgnoreCase(issueKey)
    return commentManager.create(issue, user, text, false)
}

com.atlassian.jira.issue.comments.Comment createCommentServiceDesk(String issueKey, String comment) {
    IssueManager im = ComponentAccessor.issueManager
    CommentManager cm = ComponentAccessor.commentManager
    Issue issue = im.getIssueByKeyIgnoreCase(issueKey)
    JSONObject prop = new JSONObject(["value": ["internal": false]])
    return cm.create(issue, ComponentAccessor.jiraAuthenticationContext.loggedInUser,
            comment, null as String, null as Long, new Date(), Map.of("sd.public.comment", prop), false)
}

void updateComment(Long commentId, String newText) {
    CommentManager commentManager = ComponentAccessor.commentManager
    com.atlassian.jira.issue.comments.Comment comment = commentManager.getMutableComment(commentId)
    comment.setBody(newText)
    commentManager.update(comment, false)
}

void deleteComment(Long commentId) {
    CommentManager commentManager = ComponentAccessor.commentManager
    com.atlassian.jira.issue.comments.Comment comment = commentManager.getMutableComment(commentId)
    commentManager.delete(comment)
}

//com.atlassian.jira.issue.comments.CommentOperations created = createComment("TEST2-24", "Новый комментарий")
//updateComment(created.id, "qqqqqqqqqqqqqqqqqqq")
//deleteComment(58007L)
package org.example.issue

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.AttachmentManager
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.attachment.Attachment
import com.atlassian.jira.issue.attachment.CreateAttachmentParamsBean
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.web.util.AttachmentException

import java.nio.file.Files

void createAttachment(Issue issue, String dirName, String fileName) {
    ApplicationUser user = ComponentAccessor.jiraAuthenticationContext.loggedInUser
    //File dir = new File(dirName)
    File file = new File(dirName, fileName)
    if (file.exists()) {
        CreateAttachmentParamsBean paramsBean = new CreateAttachmentParamsBean.Builder()
                .author(user)
                .file(file)
                .filename(file.name)
                .contentType(Files.probeContentType(file.toPath()))
                .issue(issue)
                .createdTime(new Date())
                .copySourceFile(true)
                .build()
        AttachmentManager attachmentManager = ComponentAccessor.attachmentManager
        try {
            attachmentManager.createAttachment(paramsBean)
        } catch (AttachmentException e) {
            log.error("Unable to create attachment", e)
        }
    }
}

List<Attachment> getAttachments(Issue issue, String name) {
    AttachmentManager attachmentManager = ComponentAccessor.attachmentManager
    return attachmentManager.getAttachments(issue)
            .findAll { it.filename.equals(name) }
}

void deleteAttachment(Attachment attachment) {
    AttachmentManager attachmentManager = ComponentAccessor.attachmentManager
    attachmentManager.deleteAttachment(attachment)
}

Issue issue = ComponentAccessor.issueManager.getIssueByKeyIgnoreCase("TEST2-24")
//createAttachment(issue, ".", "jirabanner.txt")

Set cache = []
getAttachments(issue, "jirabanner.txt").each {
    if (!cache.contains(it.filename)) {
        cache.add(it.filename)
    } else {
        deleteAttachment(it)
    }
}
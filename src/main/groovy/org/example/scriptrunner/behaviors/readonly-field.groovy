package org.example.scriptrunner.behaviors

import com.atlassian.crowd.embedded.api.Group
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.security.groups.GroupManager
import com.atlassian.jira.user.ApplicationUser

/**
 * Делает поле readonly, если значение в поле заполнено и пользователь находится в группе jira-administrators
 */
ApplicationUser user = ComponentAccessor.jiraAuthenticationContext.loggedInUser
GroupManager groupManager = ComponentAccessor.groupManager
Group group = groupManager.getGroup("jira-administrators")
def field = getFieldById(getFieldChanged())
def value = field.getFormValue()
//getFieldById('description').setFormValue(value)
if (Long.valueOf(value.toString()) > 0) {
    if (groupManager.isUserInGroup(user, group)) {
        field.setReadOnly(true)
    }
}
package org.example.scriptrunner.behaviors

import com.atlassian.crowd.embedded.api.Group
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.security.groups.GroupManager
import com.atlassian.jira.user.ApplicationUser

/*
Делает поле обязательным, если пользователь состоит в определенной группе
 */

ApplicationUser user = ComponentAccessor.jiraAuthenticationContext.loggedInUser
GroupManager groupManager = ComponentAccessor.groupManager
Group group = groupManager.getGroup("jira-administrators")
def field = getFieldById(getFieldChanged())
field.setRequired(groupManager.isUserInGroup(user, group))
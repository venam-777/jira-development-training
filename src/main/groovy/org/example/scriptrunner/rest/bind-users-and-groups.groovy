package org.example.scriptrunner.rest

import com.atlassian.crowd.embedded.api.Group
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.security.groups.GroupManager
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.user.util.UserManager
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.BaseScript

import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response

@BaseScript CustomEndpointDelegate delegate

bindUsersAndGroups(
        httpMethod: "POST", groups: ["jira-administrators"]
) { MultivaluedMap queryParams, String body ->
    JsonSlurper parser = new JsonSlurper()
    UserManager userManager = ComponentAccessor.userManager
    GroupManager groupManager = ComponentAccessor.groupManager
    List messages = []
    try {
        List groups = parser.parseText(body)
        groups.each { g ->
            Group group = groupManager.getGroup(g.name)
            if (group) {
                g.users.each { u ->
                    ApplicationUser user = userManager.getUserByName(u)
                    if (user && user.isActive() && !groupManager.isUserInGroup(user, group)) {
                        try {
                            groupManager.addUserToGroup(user, group)
                        } catch (Exception e) {
                            messages.add("Failed to add user ${user.username} to group ${group.name}")
                            //log.error
                        }
                    }
                }
            }
        }
    } catch (Exception e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(new JsonBuilder(["message" : "Invalid json body"]).toString()).build()
    }
    return Response.ok(new JsonBuilder(messages).toString()).build()
}
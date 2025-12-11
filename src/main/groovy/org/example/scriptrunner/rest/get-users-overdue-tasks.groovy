package org.example.scriptrunner.rest

import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.search.SearchResults
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.web.bean.PagerFilter
import com.atlassian.query.Query
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.json.JsonBuilder
import groovy.transform.BaseScript

import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response

@BaseScript CustomEndpointDelegate delegate

getUserOverdueTasks(
        httpMethod: "GET", groups: ["jira-administrators"]
) { MultivaluedMap queryParams, String body ->
    String username = queryParams.containsKey("user") ? queryParams.get("user").get(0) : null
    if (!username) {
        Map response = ["message" : "Request param 'user' is required"]
        return Response.status(Response.Status.BAD_REQUEST).entity(new JsonBuilder(response).toString()).build()
    }
    ApplicationUser user = ComponentAccessor.userManager.getUserByName(username)
    if (!user) {
        return Response.status(Response.Status.NOT_FOUND).entity("User with username '${username}' not found").build()
    }
    List<Issue> issues = findIssuesByJql("project = test2 and status = \"To Do\" and assignee = ${user.username}",
            ComponentAccessor.userManager.getUserByName("venam"))
    //issuekey : summary
    Map<String, String> response = [:]
    issues.each { issue ->
        response.put(issue.key, issue.summary)
    }
    return Response.ok(new JsonBuilder(response).toString()).build()
}

List<Issue> findIssuesByJql(String jql, ApplicationUser user) {
    JqlQueryParser jqlQueryParser = ComponentAccessor.getComponent(JqlQueryParser)
    SearchService searchService = ComponentAccessor.getComponent(SearchService)
    Query query = jqlQueryParser.parseQuery(jql)
    SearchResults<Issue> results = searchService.search(user, query, PagerFilter.getUnlimitedFilter())
    return results.results
}
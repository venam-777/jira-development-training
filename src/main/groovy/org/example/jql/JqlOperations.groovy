package org.example.jql

import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.search.SearchResults
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.web.bean.PagerFilter
import com.atlassian.query.Query

List<Issue> findIssuesByJql(String jql, ApplicationUser user) {
    JqlQueryParser jqlQueryParser = ComponentAccessor.getComponent(JqlQueryParser)
    SearchService searchService = ComponentAccessor.getComponent(SearchService)
    Query query = jqlQueryParser.parseQuery(jql)
    SearchResults<Issue> results = searchService.search(user, query, PagerFilter.getUnlimitedFilter())
    return results.results
}

assert findIssuesByJql("project = TEST2", ComponentAccessor.jiraAuthenticationContext.loggedInUser) == 21
package org.example.history

import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.changehistory.ChangeHistory
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager
import com.atlassian.jira.issue.search.SearchResults
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.web.bean.PagerFilter
import com.atlassian.query.Query

Date modifyDate(Date date, int unit, int delta) {
    Calendar c = Calendar.getInstance()
    c.setTime(date)
    c.add(unit, delta)
    return c.getTime()
}

Date buildDate(int year, int month, int day, int hour, int minute, int second) {
    Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+03:00"))
    c.setTime(new Date())
    c.set(Calendar.YEAR, year)
    c.set(Calendar.MONTH, month)
    c.set(Calendar.DAY_OF_MONTH, day)
    c.set(Calendar.HOUR_OF_DAY, hour)
    c.set(Calendar.MINUTE, minute)
    c.set(Calendar.SECOND, second)
    return c.getTime()
}

List<Issue> findIssuesByJql(String jql, ApplicationUser user) {
    JqlQueryParser jqlQueryParser = ComponentAccessor.getComponent(JqlQueryParser)
    SearchService searchService = ComponentAccessor.getComponent(SearchService)
    Query query = jqlQueryParser.parseQuery(jql)
    SearchResults<Issue> results = searchService.search(user, query, PagerFilter.getUnlimitedFilter())
    return results.results
}

ChangeHistoryManager historyManager = ComponentAccessor.changeHistoryManager
Issue issue = ComponentAccessor.issueManager.getIssueByKeyIgnoreCase("TEST2-24")

/*Date date = modifyDate(new Date(), Calendar.DAY_OF_YEAR, -1)
//ищем все измененные поля в задаче пользователем за последние сутки
Set<String> fields = []
ApplicationUser user = ComponentAccessor.userManager.getUserByName("venam")
historyManager.getChangeHistories(issue)
        .findAll { history ->
            history.authorKey.equals(user.key) && history.timePerformed.after(date)
        }.each { h ->
    h.changeItemBeans.each { ci ->
        fields.add(ci.field)
    }
}
fields.each {
    log.error "field: ${it}"
}*/



/*Date date1 = buildDate(2025, 0, 1, 12, 0, 0)
date1 = modifyDate(date1, Calendar.DAY_OF_MONTH, -1)
log.error date1*/

package org.example.history

import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.issue.changehistory.ChangeHistory
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager
import com.atlassian.jira.issue.history.ChangeItemBean
import com.atlassian.jira.issue.search.SearchResults
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.web.bean.PagerFilter
import com.atlassian.query.Query

import java.sql.Timestamp

/**
 * Задача: найти все изменения конкретного поля конкретным пользователем за определенный промежуток времени и откатить эти изменения назад
 * Бонус: пользователь мог несколько раз за период поменять значение поля, необходимо откатиться на исходное, которое было до всех этих событий
 */

List<Issue> findIssuesByJql(String jql, ApplicationUser user) {
    JqlQueryParser jqlQueryParser = ComponentAccessor.getComponent(JqlQueryParser)
    SearchService searchService = ComponentAccessor.getComponent(SearchService)
    Query query = jqlQueryParser.parseQuery(jql)
    SearchResults<Issue> results = searchService.search(user, query, PagerFilter.getUnlimitedFilter())
    return results.results
}

Map<Long, Object> getSourceFieldValues(List<Issue> issues, ApplicationUser author, Timestamp start, Timestamp end, String field, Closure extractor) {
    ChangeHistoryManager changeHistoryManager = ComponentAccessor.changeHistoryManager
    List<ChangeHistory> history = changeHistoryManager.getChangeItemsWithFieldsForIssues(issues, [field])
    if (author) {
        history = history.findAll { it.authorKey.equals(author.key) }
    }
    if (start) {
        history = history.findAll { it.timePerformed.after(start) }
    }
    if (end) {
        history = history.findAll { it.timePerformed.before(end) }
    }
    history = history.sort { h1, h2 -> h1.timePerformed.compareTo(h2.timePerformed)}
    Map<Long, Object> result = [:]
    history.each { h->
        if (!h.changeItemBeans.isEmpty()) {
            if (!result.containsKey(h.issueId)) {
                ChangeItemBean ci = h.changeItemBeans.find {ci -> ci.field.equals(field)}
                if (ci) {
                    if (extractor != null) {
                        result.put(h.issueId, extractor(ci))
                    } else {
                        result.put(h.issueId, ci.fromString)
                    }
                }
            }
        }
    }
    return result
}

Date modifyDate(Date date, int unit, int delta) {
    Calendar c = Calendar.getInstance()
    c.setTime(date)
    c.add(unit, delta)
    return c.getTime()
}

//1. найти все интересующие нас заявки
ApplicationUser user = ComponentAccessor.userManager.getUserByName("venam")
List<Issue> issues = findIssuesByJql("project = TEST2", user)
Timestamp start = new Timestamp(modifyDate(new Date(), Calendar.MINUTE, -15).getTime())
Timestamp end = new Timestamp(new Date().getTime())
//2. получить по каждой заявке исходные значения полей
Map<Long, Object> fieldValues = getSourceFieldValues(issues, user, start, end, "summary", { ChangeItemBean ci ->
//    Long optionId = Long.valueOf(ci.from)
//    Option option //ComponentAccessor.optionsManager.getOption(optionId)
//    return option
    return ci.fromString
})
//3. нужно заполнить поля в заявке исходными значениями
IssueManager issueManager = ComponentAccessor.issueManager
fieldValues.each { key, value ->
    MutableIssue issue = issueManager.getIssueObject(key)
    issue.setSummary(value)
//    issue.setCustomFieldValue(cf, value)
    issueManager.updateIssue(user, issue, EventDispatchOption.DO_NOT_DISPATCH, false)
}
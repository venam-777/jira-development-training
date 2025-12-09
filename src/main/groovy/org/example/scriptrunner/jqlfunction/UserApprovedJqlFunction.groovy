package org.example.scriptrunner.jqlfunction

import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.comments.Comment
import com.atlassian.jira.issue.comments.CommentManager
import com.atlassian.jira.issue.search.SearchResults
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.jql.query.LuceneQueryBuilder
import com.atlassian.jira.jql.query.QueryCreationContext
import com.atlassian.jira.jql.validator.NumberOfArgumentsValidator
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.util.MessageSet
import com.atlassian.jira.web.bean.PagerFilter
import com.atlassian.query.clause.TerminalClause
import com.atlassian.query.operand.FunctionOperand
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.Query
import com.onresolve.jira.groovy.jql.JqlQueryFunction
import com.onresolve.scriptrunner.runner.customisers.WithPlugin
import org.apache.lucene.search.TermQuery

//issueFunction in userApproved(...)

/*
ищет заявки, в которых пользователь оставил комментарий определенного содержания ИЛИ любой комментарий
 */
@WithPlugin("com.onresolve.jira.groovy.groovyrunner")
class UserApprovedJqlFunction extends AbstractScriptedJqlFunction implements JqlQueryFunction {

    List<Issue> findIssuesByJql(String jql, ApplicationUser user) {
        JqlQueryParser jqlQueryParser = ComponentAccessor.getComponent(JqlQueryParser)
        SearchService searchService = ComponentAccessor.getComponent(SearchService)
        com.atlassian.query.Query query = jqlQueryParser.parseQuery(jql)
        SearchResults<Issue> results = searchService.search(user, query, PagerFilter.getUnlimitedFilter())
        return results.results
    }

    @Override
    String getFunctionName() {
        return "userApproved"
    }

    @Override
    String getDescription() {
        return "Возвращает задачи по JQL, в которых пользователь X оставил комментарий Y"
    }

    @Override
    List<Map> getArguments() {
        return [
                [
                        description: "JQL отбора заявок, в которых ищем комментарий",
                        optional   : false
                ],
                [
                        description: "Username пользователя, который оставил комментарий",
                        optional   : false
                ],
                [
                        description: "Текст комментария, который оставил пользователь",
                        optional   : true
                ]
        ]
    }

    @Override
    Query getQuery(QueryCreationContext queryCreationContext, FunctionOperand operand, TerminalClause terminalClause) {
        String jql = operand.args.get(0)
        String user = operand.args.get(1)
        String comment
        if (operand.args.size() == 3) {
            comment = operand.args.get(2)
        }
        CommentManager commentManager = ComponentAccessor.commentManager
        log.error "Projects: ${queryCreationContext.determinedProjects}"
        List<Issue> issues = findIssuesByJql("project in (${queryCreationContext.determinedProjects.join(", ")})", queryCreationContext.applicationUser)
                .findAll { issue ->
                    List<Comment> comments = commentManager.getComments(issue)
                    return comments.any { c ->
                        c.authorApplicationUser.username.equalsIgnoreCase(user)
                                && (comment ? c.body.equalsIgnoreCase(comment) : true)
                    }
                }
        BooleanQuery.Builder builder = new BooleanQuery.Builder()
        issues.each { issue ->
            builder.add(new TermQuery(new Term("issue_id", issue.id.toString())), BooleanClause.Occur.SHOULD)
        }
        return builder.build()
    }

}

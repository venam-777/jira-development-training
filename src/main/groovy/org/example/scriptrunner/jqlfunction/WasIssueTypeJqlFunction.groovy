package org.example.scriptrunner.jqlfunction

import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.config.ConstantsManager
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.comments.Comment
import com.atlassian.jira.issue.comments.CommentManager
import com.atlassian.jira.issue.issuetype.IssueType
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
import com.onresolve.scriptrunner.db.DatabaseUtil

/**
 * Функция ищет все заявки, у которых когда-либо был тип заявки X и затем он изменился на тип заявки Y
 * Уточнение: изменение типа могло быть не прямое, а косвенное
 */
@WithPlugin("com.onresolve.jira.groovy.groovyrunner")
class WasIssueTypeJqlFunction extends AbstractScriptedJqlFunction implements JqlQueryFunction {

    @Override
    String getFunctionName() {
        return "wasIssueType"
    }

    @Override
    String getDescription() {
        return "Функция ищет все заявки, у которых когда-либо был тип заявки X и затем он изменился на тип заявки Y"
    }

    @Override
    List<Map> getArguments() {
        return [
                [
                        description: "Source issue type",
                        optional   : false
                ],
                [
                        description: "Target issue type",
                        optional   : true
                ],
                [
                        description: "Strict search",
                        optional   : true
                ]
        ]
    }

    boolean isIssueType(String type) {
        ConstantsManager constantsManager = ComponentAccessor.constantsManager
        return constantsManager.getAllIssueTypeObjects().any { it.name.equalsIgnoreCase(type)}
    }

    @Override
    MessageSet validate(ApplicationUser user, FunctionOperand operand, TerminalClause terminalClause) {
        def messageSet = new NumberOfArgumentsValidator(1, 3, getI18n()).validate(operand)
        if (messageSet.hasAnyErrors()) {
            return messageSet
        }
        if (operand.args.size() == 1) {
            String sourceType = operand.args.get(0)
            if (!isIssueType(sourceType)) {
                messageSet.addErrorMessage("${sourceType} is not valid issue type")
            }
        }
        if (operand.args.size() >= 2) {
            String sourceType = operand.args.get(0)
            String targetType = operand.args.get(1)
            if (!isIssueType(sourceType)) {
                messageSet.addErrorMessage("${sourceType} is not valid issue type")
            }
            if (!isIssueType(targetType)) {
                messageSet.addErrorMessage("${targetType} is not valid issue type")
            }
        }
        return messageSet
    }

    Collection<Long> getIssueIds(String sourceType) {
        DatabaseUtil.withSql("local") { sql ->
            sql.rows("""
                select cg.ISSUEID ISSUE_ID from changegroup cg
                    join changeitem ci on cg.id = ci.groupid
                    where ci.field = 'issuetype'
                        and (ci.oldstring is not null and cast(ci.oldstring as varchar(100)) = :source)
            """, [source: sourceType])
        }.collect { it.ISSUE_ID } as Collection<Long>
    }

    Collection<Long> getIssueIds(String sourceType, String targetType, boolean strict) {
        if (strict) {
            DatabaseUtil.withSql("local") { sql ->
                sql.rows("""
                select cg.ISSUEID ISSUE_ID from changegroup cg
                    join changeitem ci on cg.id = ci.groupid
                    where ci.field = 'issuetype'
                        and (ci.oldstring is not null and cast(ci.oldstring as varchar(100)) = :source and ci.newstring is not null and
                            cast(ci.newstring as varchar(100)) = :target)
            """, [source: sourceType, target: targetType])
            }.collect { it.ISSUE_ID } as Collection<Long>
        } else {
            Collection<Long> sourceIds = DatabaseUtil.withSql("local") { sql ->
                sql.rows("""
                select cg.ISSUEID ISSUE_ID from changegroup cg
                    join changeitem ci on cg.id = ci.groupid
                    where ci.field = 'issuetype'
                        and (ci.oldstring is not null and cast(ci.oldstring as varchar(100)) = :source)
            """, [source: sourceType])
            }.collect { it.ISSUE_ID } as Collection<Long>
            Collection<Long> targetIds = DatabaseUtil.withSql("local") { sql ->
                sql.rows("""
                select cg.ISSUEID ISSUE_ID from changegroup cg
                    join changeitem ci on cg.id = ci.groupid
                    where ci.field = 'issuetype'
                        and (ci.newstring is not null and cast(ci.newstring as varchar(100)) = :target)
            """, [source: sourceType, target: targetType])
            }.collect { it.ISSUE_ID } as Collection<Long>
            return sourceIds.intersect(targetIds)
        }
    }

    @Override
    Query getQuery(QueryCreationContext queryCreationContext, FunctionOperand operand, TerminalClause terminalClause) {
        Collection<Long> ids = []
        String sourceType = operand.args.get(0)
        if (operand.args.size() == 1) {
            ids = getIssueIds(sourceType)
        } else if (operand.args.size() == 2) {
            String targetType = operand.args.get(1)
            boolean strict = true
            ids = getIssueIds(sourceType, targetType, strict)
        } else if (operand.args.size() == 3) {
            String targetType = operand.args.get(1)
            boolean strict = Boolean.valueOf(operand.args.get(2))
            ids = getIssueIds(sourceType, targetType, strict)
        }
        BooleanQuery.Builder builder = new BooleanQuery.Builder()
        ids.each { id ->
            builder.add(new TermQuery(new Term("issue_id", id.toString())), BooleanClause.Occur.SHOULD)
        }
        return builder.build()
    }
}
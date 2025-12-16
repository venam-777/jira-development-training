package org.example.scriptrunner.behaviors

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.customfields.manager.OptionsManager
import com.atlassian.jira.issue.customfields.option.Option
import com.atlassian.jira.issue.customfields.option.Options
import com.atlassian.jira.issue.fields.CustomField

List<Option> getOptions(String fieldId, Closure filter) {
    CustomField cf = ComponentAccessor.customFieldManager.getCustomFieldObject(fieldId)
    def config = cf.getRelevantConfig(getIssueContext())
    OptionsManager optionsManager = ComponentAccessor.optionsManager
    Options options = optionsManager.getOptions(config)
    return options.findAll { filter(it) }
}

def field = getFieldById(getFieldChanged())
def value = field.getValue()
if (value) {
    def multiCF = getFieldById('customfield_11604')
    multiCF.setFieldOptions(getOptions(multiCF.getFieldId(), o -> o.value.equals(value)))
}

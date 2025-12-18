package org.example.scriptrunner.other

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Пример редактирования\создания web-resource Для ScriptRunner, если нет прямого доступа на сервер
 */

String fileName = "test1.js"
String content = """
var interval = setInterval(function() {
    if (\$("#update-page-restrictions-dialog").length > 0) {
        //диалог показан
        \$("#page-restrictions-add-button").click(function(event) {
            if (\$("table.restrictions-dialog-table").find("tr.entity-row").length > 20) {
                event.preventDefault()
                alert("Нельзя больше добавлять рестрикшены")
            }
        })
        clearInterval(interval)
    }
}, 100)
"""

String getContent(String fileName) {
    Path filePath = Paths.get("/opt/atlassian/application-data/confluence/web-resources/com.onresolve.confluence.groovy.groovyrunner/${fileName}");
    return Files.readString(filePath)
}

void setContent(String fileName, String content) {
    File file = new File("/opt/atlassian/application-data/confluence/web-resources/com.onresolve.confluence.groovy.groovyrunner/${fileName}")
    if (file.exists() || file.createNewFile()) {
        PrintWriter writer = new PrintWriter(file, "UTF-8");
        writer.println(content)
        writer.close()
    }
}
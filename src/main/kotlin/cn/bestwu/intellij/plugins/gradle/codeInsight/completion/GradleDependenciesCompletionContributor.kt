package cn.bestwu.intellij.plugins.gradle.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupElementWeigher
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.jetbrains.plugins.gradle.codeInsight.AbstractGradleCompletionContributor
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrNamedArgument
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrStringContent
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression
import org.jetbrains.plugins.groovy.lang.psi.api.util.GrNamedArgumentsOwner

class GradleDependenciesCompletionContributor : AbstractGradleCompletionContributor() {

    init {
        // map-style notation:
        // e.g.:
        //    compile group: 'com.google.code.guice', name: 'guice', version: '1.0'
        //    runtime([group:'junit', name:'junit-dep', version:'4.7'])
        //    compile(group:'junit', name:'junit-dep', version:'4.7')
        extend(CompletionType.SMART, IN_MAP_DEPENDENCY_NOTATION, object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(params: CompletionParameters,
                                        context: ProcessingContext,
                                        result: CompletionResultSet) {
                val parent = params.position.parent?.parent
                if (parent !is GrNamedArgument || parent.parent !is GrNamedArgumentsOwner) {
                    return
                }
                result.stopHere()
                val searchText = CompletionUtil.findReferenceOrAlphanumericPrefix(params)
                val searchParam: SearchParam
                if (GROUP_LABEL == parent.labelName) {
                    searchParam = SearchParam(searchText)
                } else if (NAME_LABEL == parent.labelName) {
                    val groupId = findNamedArgumentValue(parent.parent as GrNamedArgumentsOwner, GROUP_LABEL) ?: return
                    searchParam = SearchParam(groupId, searchText)
                } else if (VERSION_LABEL == parent.labelName) {
                    val namedArgumentsOwner = parent.parent as GrNamedArgumentsOwner
                    val groupId = findNamedArgumentValue(namedArgumentsOwner, GROUP_LABEL) ?: return
                    val artifactId = findNamedArgumentValue(namedArgumentsOwner, NAME_LABEL) ?: return
                    searchParam = SearchParam(groupId, artifactId)
                } else {
                    return
                }
                if (searchParam.id.length < 2)
                    return
                val searchResult = artifactSearcher.search(searchParam)
                if (searchResult.isEmpty()) {
                    show("find dependencies fail", "<a href=\"https://bintray.com/search?query=${searchParam.id}\">search in jcenter</a><br/>", NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER)
                }

                var completionResultSet = result
                val resultSet: List<ArtifactInfo>
                if (GROUP_LABEL == parent.labelName) {
                    resultSet = searchResult.filter { it.groupId.isNotBlank() }.distinctBy { it.groupId }
                    resultSet.forEach {
                        completionResultSet.addElement(LookupElementBuilder.create(it.groupId).withIcon(AllIcons.Nodes.PpLib).withTypeText(it.type(), repoIcon, true))
                    }
                } else if (NAME_LABEL == parent.labelName) {
                    resultSet = searchResult.filter { it.artifactId.isNotBlank() }.distinctBy { it.artifactId }
                    resultSet.forEach {
                        completionResultSet.addElement(LookupElementBuilder.create(it.artifactId).withIcon(AllIcons.Nodes.PpLib).withTypeText(it.type(), repoIcon, true))
                    }
                } else if (VERSION_LABEL == parent.labelName) {
                    resultSet = searchResult.filter { it.version.isNotBlank() }.distinctBy { it.version }
                    completionResultSet = result.withRelevanceSorter(
                            CompletionSorter.emptySorter().weigh(object : LookupElementWeigher("gradleDependencyWeigher") {
                                override fun weigh(element: LookupElement): Comparable<*> {
                                    return VersionComparator(resultSet.indexOfFirst { it.version == element.lookupString })
                                }
                            })
                    )
                    resultSet.forEach {
                        completionResultSet.addElement(LookupElementBuilder.create(it.version).withIcon(AllIcons.Nodes.PpLib).withTypeText(it.type(), repoIcon, true))
                    }
                } else {
                    return
                }


            }
        })

        // group:name:version notation
        // e.g.:
        //    compile 'junit:junit:4.11'
        //    compile('junit:junit:4.11')
        extend(CompletionType.SMART, IN_METHOD_DEPENDENCY_NOTATION, object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(params: CompletionParameters,
                                        context: ProcessingContext,
                                        result: CompletionResultSet) {
                val parent = params.position.parent
                if (parent !is GrLiteral && parent !is GrStringContent) return

                result.stopHere()
                val searchText: String
                if (parent is GrStringContent) {
                    searchText = params.position.text.substringBefore("IntellijIdeaRulezzz ")
                } else
                    searchText = CompletionUtil.findReferenceOrAlphanumericPrefix(params)
                val searchParam = SearchParam(searchText)
                if (searchParam.id.length < 2)
                    return
                val searchResult = artifactSearcher.search(searchParam)
                if (searchResult.isEmpty()) {
                    show("find dependencies fail", "<a href=\"https://bintray.com/search?query=${searchParam.id}\">search in jcenter</a><br/>", NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER)
                }

                val resultSet = searchResult.distinctBy { it.presentableText }
                val completionResultSet = result.withRelevanceSorter(
                        CompletionSorter.emptySorter().weigh(object : LookupElementWeigher("gradleDependencyWeigher") {
                            override fun weigh(element: LookupElement): Comparable<*> {
                                return VersionComparator(resultSet.indexOfFirst { it.presentableText == element.lookupString })
                            }
                        })
                )
                resultSet.forEach {
                    completionResultSet.addElement(LookupElementBuilder.create(it.presentableText).withIcon(AllIcons.Nodes.PpLib).withTypeText(it.type(), repoIcon, true))
                }
            }
        })
    }

    companion object {
        private val GROUP_LABEL = "group"
        private val NAME_LABEL = "name"
        private val VERSION_LABEL = "version"
        private val DEPENDENCIES_SCRIPT_BLOCK = "dependencies"
        private val artifactSearcher = GradleArtifactSearcher()

        private val DEPENDENCIES_CALL_PATTERN = psiElement()
                .inside(true, psiElement(GrMethodCallExpression::class.java).with(
                        object : PatternCondition<GrMethodCallExpression>("withInvokedExpressionText") {
                            override fun accepts(expression: GrMethodCallExpression, context: ProcessingContext): Boolean {
                                if (checkExpression(expression)) return true
                                return checkExpression(PsiTreeUtil.getParentOfType(expression, GrMethodCallExpression::class.java))
                            }

                            private fun checkExpression(expression: GrMethodCallExpression?): Boolean {
                                if (expression == null) return false
                                val grExpression = expression.invokedExpression
                                return DEPENDENCIES_SCRIPT_BLOCK == grExpression.text || "imports" == grExpression.text
                            }
                        }))

        private val IN_MAP_DEPENDENCY_NOTATION = psiElement()
                .and(GRADLE_FILE_PATTERN)
                .withParent(GrLiteral::class.java)
                .withSuperParent(2, psiElement(GrNamedArgument::class.java))
                .and(DEPENDENCIES_CALL_PATTERN)

        private val IN_METHOD_DEPENDENCY_NOTATION = psiElement()
                .and(GRADLE_FILE_PATTERN)
                .and(DEPENDENCIES_CALL_PATTERN)
    }


}


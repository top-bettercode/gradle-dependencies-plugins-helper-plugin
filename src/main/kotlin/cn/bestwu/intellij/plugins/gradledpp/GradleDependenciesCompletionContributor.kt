package cn.bestwu.intellij.plugins.gradle.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupElementWeigher
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PlatformPatterns.psiFile
import com.intellij.patterns.StandardPatterns.string
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.jetbrains.plugins.gradle.util.GradleConstants
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrNamedArgument
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrStringContent
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression
import org.jetbrains.plugins.groovy.lang.psi.api.util.GrNamedArgumentsOwner
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.literals.GrLiteralImpl

class GradleDependenciesCompletionContributor : CompletionContributor() {

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
                searchParam = when {
                    GROUP_LABEL == parent.labelName -> SearchParam(searchText, "", false, false)
                    NAME_LABEL == parent.labelName -> {
                        val groupId = findNamedArgumentValue(parent.parent as GrNamedArgumentsOwner, GROUP_LABEL) ?: return
                        SearchParam(groupId, searchText, true, false)
                    }
                    VERSION_LABEL == parent.labelName -> {
                        val namedArgumentsOwner = parent.parent as GrNamedArgumentsOwner
                        val groupId = findNamedArgumentValue(namedArgumentsOwner, GROUP_LABEL) ?: return
                        val artifactId = findNamedArgumentValue(namedArgumentsOwner, NAME_LABEL) ?: return
                        SearchParam(groupId, artifactId, true, true)
                    }
                    else -> return
                }
                if (searchParam.id.length < 2)
                    return
                val searchResult = artifactSearcher.search(searchParam, params.position.project)
                if (searchResult.isEmpty()) {
                    show(params.position.project, searchParam.failContent, "find dependencies fail", NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER)
                }

                var completionResultSet = result
                when {
                    GROUP_LABEL == parent.labelName -> {
                        searchResult.forEach {
                            completionResultSet.addElement(LookupElementBuilder.create(it.groupId).withIcon(AllIcons.Nodes.PpLib).withTypeText(it.type(), repoIcon, true).withInsertHandler(INSERT_HANDLER))
                        }
                    }
                    NAME_LABEL == parent.labelName -> {
                        searchResult.forEach {
                            completionResultSet.addElement(LookupElementBuilder.create(it.artifactId).withIcon(AllIcons.Nodes.PpLib).withTypeText(it.type(), repoIcon, true).withInsertHandler(INSERT_HANDLER))
                        }
                    }
                    VERSION_LABEL == parent.labelName -> {
                        completionResultSet = result.withRelevanceSorter(
                                CompletionSorter.emptySorter().weigh(object : LookupElementWeigher("gradleDependencyWeigher") {
                                    override fun weigh(element: LookupElement): Comparable<*> {
                                        return VersionComparator(searchResult.indexOfFirst { it.version == element.lookupString })
                                    }
                                })
                        )
                        searchResult.forEach {
                            completionResultSet.addElement(LookupElementBuilder.create(it.version).withIcon(AllIcons.Nodes.PpLib).withTypeText(it.type(), repoIcon, true).withInsertHandler(INSERT_HANDLER))
                        }
                    }
                    else -> return
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
                val prefix: String
                if (parent is GrStringContent) {
                    prefix = params.position.text.substringBefore("IntellijIdeaRulezzz ")
                } else
                    prefix = CompletionUtil.findReferenceOrAlphanumericPrefix(params)
                val text = trim(params.originalPosition?.text ?: "")
                val searchText = if (!text.startsWith("c:", true) && !text.startsWith("fc:", true)) prefix else text
                val searchParam: SearchParam
                searchParam = if (text.contains(":") && !searchText.contains(":")) {
                    SearchParam(searchText, "", false, false)
                } else {
                    SearchParam(searchText)
                }
                if (searchParam.id.length < 2)
                    return
                val searchResult = artifactSearcher.search(searchParam, params.position.project)
                if (searchResult.isEmpty()) {
                    show(params.position.project, searchParam.failContent, "find dependencies fail", NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER)
                }

                var completionResultSet = result.withRelevanceSorter(
                        CompletionSorter.emptySorter().weigh(object : LookupElementWeigher("gradleDependencyWeigher") {
                            override fun weigh(element: LookupElement): Comparable<*> {
                                return VersionComparator(searchResult.indexOfFirst { it.gav == element.lookupString })
                            }
                        })
                )
                if (searchParam.advancedSearch.isNotEmpty()) {
                    completionResultSet = completionResultSet.withPrefixMatcher(PrefixMatcher.ALWAYS_TRUE)
                }
                searchResult.forEach {
                    completionResultSet.addElement(LookupElementBuilder.create("${it.gav}${if (it.artifactId.isEmpty()) ":" else ""}").withPresentableText(it.gav).withIcon(AllIcons.Nodes.PpLib).withTypeText(it.type(), repoIcon, true).withInsertHandler(INSERT_HANDLER))
                }
            }
        })
    }

    override fun duringCompletion(context: CompletionInitializationContext) = contributorDuringCompletion(context)

    companion object {
        private val GROUP_LABEL = "group"
        private val NAME_LABEL = "name"
        private val VERSION_LABEL = "version"
        private val DEPENDENCIES_SCRIPT_BLOCK = "dependencies"
        private val artifactSearcher = GradleArtifactSearcher()
        private val GRADLE_FILE_PATTERN: ElementPattern<PsiElement> = psiElement()
                .inFile(psiFile().withName(string().endsWith('.' + GradleConstants.EXTENSION)))

        private fun findNamedArgumentValue(namedArgumentsOwner: GrNamedArgumentsOwner?, label: String): String? {
            val namedArgument = namedArgumentsOwner?.findNamedArgument(label) ?: return null
            return (namedArgument.expression as? GrLiteralImpl)?.value?.toString()
        }

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


package org.jetbrains.plugins.scala
package codeInsight.intention.expression

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil.getParentOfType
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScExpression
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunction
import org.jetbrains.plugins.scala.util.IntentionUtils.replaceWithExplicit

/**
  * @author Ksenia.Sautina
  * @since 5/4/12
  */

object InlineImplicitConversionIntention {
  def familyName = "Provide implicit conversion"
}

class InlineImplicitConversionIntention extends PsiElementBaseIntentionAction {
  def getFamilyName: String = InlineImplicitConversionIntention.familyName

  override def getText: String = getFamilyName

  def isAvailable(project: Project, editor: Editor, element: PsiElement): Boolean =
    parent(element).flatMap {
      _.implicitElement(fromUnderscore = true)
    }.isDefined

  override def invoke(project: Project, editor: Editor, element: PsiElement): Unit =
    parent(element).foreach { expression =>
      expression.implicitElement(fromUnderscore = true).collect {
        case function: ScFunction => function
      }.foreach { function =>
        val (regularConversions, companionConversions) = expression.getImplicitConversions(fromUnderscore = true)
        replaceWithExplicit(expression, function, project, editor, regularConversions ++ companionConversions)
      }
    }

  private def parent(element: PsiElement) =
    Option(getParentOfType(element, classOf[ScExpression], false)).filter {
      _.isValid
    }
}

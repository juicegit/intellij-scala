package org.jetbrains.plugins.scala.lang.resolve

import com.intellij.psi.scope._
import com.intellij.psi._
import java.util.Set
import java.util.HashSet

class ResolveProcessor(override val kinds: Set[ResolveTargets], val name: String) extends BaseProcessor(kinds) {

  def execute(element: PsiElement, state: ResolveState): Boolean = {
    val named = element.asInstanceOf[PsiNamedElement]
    if (named != null && named.getName == name) {
      candidates add new ScalaResolveResult(named)
      return false //todo: for locals it is ok to terminate the walkup, later need more elaborate check
    }
    return true
  }
}
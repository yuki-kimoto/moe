package org.moe.parser

import ParserUtils._

import scala.util.parsing.combinator._
import org.moe.ast._

trait Statements extends Expressions {

  lazy val tryBlockRule =
    "try" ~> block
  lazy val catchBlockRule =
    ("catch" ~ "(") ~> typeLiteral ~ varname ~ (")" ~> block)
  lazy val finallyBlockRule =
    "finally" ~> block

  // FIXME I feel skipping over blank statements doesn't
  // portray the AST completely but I might just be splitting hairs
  def statementDelim: Parser[List[String]] = rep1(";")
  def statements: Parser[StatementsNode] =
    repsep(statement, statementDelim) ^^ StatementsNode
  def blockContent: Parser[StatementsNode] =
    statements <~ statementDelim.?
  def block: Parser[StatementsNode] =
    "{" ~> blockContent <~ "}"

  def doBlock: Parser[StatementsNode] = "do".r ~> block
  def scopeBlock: Parser[ScopeNode] = block ^^ { ScopeNode(_) }

  def packageBlock = ("package" ~> namespacedIdentifier) ~ block ^^ {
    case p ~ b => PackageDeclarationNode(p, b)
  }
  // def classBlock = "class" ~> className ~ block

  // awwaiid's experimental structures
  def loop: Parser[AST] = ifLoop // | forLoop | foreachLoop | whileLoop

  def ifLoop: Parser[AST] =
    (("if" ~ "(") ~> expression) ~ (")" ~> block) ^^ { case a ~ b => IfNode(new IfStruct(a,b)) }

  // def forLoop = "for" ~ "(" ~> expression <~ ";" ~> expression <~ ";" ~> expression <~ ")" ~ block
  // def whileLoop = "if" ~ "(" ~> expression <~ ")" ~ block
  // def foreachLoop = "for(each)?".r ~ varDeclare ~ "(" ~> expression <~ ")" ~ block
  // def packageName = bareName
  // def className = bareName

  def tryBlock: Parser[TryNode] =
    tryBlockRule ~ rep(catchBlock) ~ rep(finallyBlock) ^^ {
      case a ~ b ~ c => TryNode(a, b, c)
    }

  def catchBlock: Parser[CatchNode] =
    catchBlockRule ^^ {
      case a ~ b ~ c => CatchNode(a, b, c)
    }

  def finallyBlock: Parser[FinallyNode] = finallyBlockRule ^^ FinallyNode

  def statement: Parser[AST] = (
      loop
    | expression
    | doBlock
    | scopeBlock
    | tryBlock
    | packageBlock
  )
}

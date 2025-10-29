package io.github.gnush.profiletweaker.data.ini

import scala.io.Source.*
import scala.util.parsing.combinator.RegexParsers
import scala.collection.mutable

type Section = String
type Key = String
type Value = String
type Ini = mutable.Map[Section, mutable.Map[Key, Value]]

extension (ini: Ini)
  /**
   * Returns a textual representation of the [[Ini]].
   * Sorts the sections and the keys of each section alphabetically (ascending).
   *
   * Example:
   * {{{
   * [A]
   * a=value
   * b=value
   * c=value
   *
   * [B]
   * a=value
   * b=value
   * }}}
   *
   * @return the textual representation of the [[Ini]]
   */
  def format: String = ini.toSeq.sortBy(_._1).foldRight("") {
    case ((section, entries), sectionAcc) =>
      s"[$section]\n${format(section) getOrElse ""}\n$sectionAcc"
  }.stripTrailing

  /**
   * Returns a textual representation of the given [[Section]].
   * The result won't contain the [[Section]] heading.
   * Sorts the keys alphabetically (ascending).
   * Example:
   * {{{
   * a=value
   * b=value
   * c=value
   * }}}
   *
   * @param section [[Section]] to format
   * @return [[Some]] textual representation of the given section if the section exists
   *
   *         [[None]] otherwise
   */
  def format(section: Section): Option[String] =
    if (hasSection(section)) {
      Some(
        ini(section).toSeq.sorted.foldRight("") {
          case ((key, value), acc) => s"$key=$value\n$acc"
        }.stripTrailing
      )
    } else
      None
  
  def hasSection: Section => Boolean = ini.contains
  def hasKey(section: Section, key: Key): Boolean = ini.contains(section) && ini(section).contains(key)

  def put(section: Section, content: mutable.Map[Key, Value]): Boolean =
    if (!section.isBlank)
      ini += section -> content
      true
    else
      false

  def put(section: Section, key: Key, value: Value): Boolean =
    if (!section.isBlank && !key.isBlank && !value.isBlank)
      if (hasKey(section, key))
        ini(section)(key) = value
      else if (hasSection(section))
        ini(section) += key -> value
      else
        ini += section -> mutable.Map(key -> value)
      true
    else
      false

object Ini:
  def apply(): Ini = mutable.Map()

  def from(source: String): Option[Ini] = Parser.parse(Parser.ini, source) match {
    case Success(result, _) => Some(result)
    case _: NoSuccess => None
  }

// https://www.baeldung.com/scala/try-with-resources
// import scala.util.Using
//  def from(path: String): Option[Ini] = {
//    Using(fromFile(path)) { source =>
//      from(source.getLines() mkString "\n")
//    }
//  }

  def read(file: String): Option[Ini] = {
    val source = fromFile(file)
    try from(source.getLines() mkString "\n") finally source.close()
  }

//object Section:
//  def apply(name: String): Section = name
//
//object Key:
//  def apply(name: String): Key = name
//
//object Value:
//  def apply(value: String): Value = value


object Parser extends RegexParsers {
  private def id: Parser[String]   = """[\w_]+""".r
  private def value: Parser[Value] = """[\w!"#$%&'(),./:;<>?@^_`{|}~*+\-\[\\\] ]+""".r  // TODO: remove # from match?
  private def entry: Parser[(Key, Value)] = id ~ """[ \t]*=[ \t]*""".r ~ value ^^ { case key ~ _ ~ value => (key, value) }
  private def sectionHeader: Parser[Section] = "[" ~ id ~ "]" ^^ { case _ ~ name ~ _ => name }
  private def section: Parser[(Section, mutable.Map[Key, Value])] = sectionHeader ~ entry.* ^^ { case name ~ entries => (name, mutable.Map.from(entries)) }
  def ini: Parser[Ini] = section.+ ^^ { mutable.Map.from(_) }
}


val input: String =
  """[settings]
    |GUI_Foo_Bar=\[bar.123]
    |bar=baz;;123;dsa;f;;
    |baz=-856489
    |lorem=[object Object]
    |
    |[other]
    |one=is a number
    |two=medium_sized
    |three=$3""".stripMargin

import io.github.gnush.profiletweaker.data.ini.Parser.*

// Default GUI Ini location %localappdata%\SWTOR\swtor\settings\GUIProfiles

@main def main(): Unit =
  //println(input)

  val parsed = Ini.from(input)

  parse(ini, input) match
    case Success(result, _) => println(result)
    case Failure(msg, _) => println(s"FAILURE: $msg")
    case Error(msg, _) => println(s"ERROR: $msg")

  parsed match {
    case Some(ini) => println(ini.format)
    case None => println("nothing")
  }
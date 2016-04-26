package io.github.otbproject.otb.core.message.in.filter

sealed trait FilterResult extends Ordered[FilterResult] {
  val name: String
  protected val severity: Int

  override def compare(that: FilterResult): Int = this.severity compare that.severity
  override def toString: String = name
}

object FilterResult {
  val ordering = Ordering.ordered[FilterResult]
  private val minStrikeTotal = 2

  class Warning(val warningMessage: String) extends FilterResult {
    override val name: String = "Warning"
    override protected val severity: Int = 1
  }

  class Strike(val warningMessage: String, val outOfTotalStrikes: Int) extends FilterResult {
    if (outOfTotalStrikes < minStrikeTotal) {
      throw new IllegalArgumentException("A total of less than " + minStrikeTotal + " strikes is a timeout")
    }

    override val name: String = "Strike"
    override protected val severity: Int = 3

    override def compare(that: FilterResult): Int =
      (this, that) match {
        case (_: Purge, _) | (_, _: Purge) =>
          // If either term is a Purge
          super.compare(that)
        case (_, other: Strike) =>
          // Terms of comparison are reversed, so that a lower
          // outOfTotalStrikes compares as "larger"
          other.outOfTotalStrikes compare outOfTotalStrikes
        case _ => super.compare(that)
      }
  }

  class Purge(warningMessage: String) extends Strike(warningMessage, minStrikeTotal) {
    override val name: String = "Purge"
    override protected val severity: Int = 4

    override def compare(that: FilterResult): Int = super.compare(that)
  }

  class Timeout extends FilterResult {
    override val name: String = "Timeout"
    override protected val severity: Int = 7
  }

  class Ban extends FilterResult {
    override val name: String = "Ban"
    override protected val severity: Int = 10
  }

  object Warning { def apply(warningMessage: String): Warning = new Warning(warningMessage) }

  object Strike {
    def apply(warningMessage: String, outOfTotalStrikes: Int): Strike = new Strike(warningMessage, outOfTotalStrikes)
  }

  object Purge { def apply(warningMessage: String): Purge = new Purge(warningMessage)}

  object Timeout { def apply(): Timeout = new Timeout }

  object Ban { def apply(): Ban = new Ban }

}

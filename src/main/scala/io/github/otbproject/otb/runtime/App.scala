package io.github.otbproject.otb.runtime

object App {
  def main(args: Array[String]) {
    // TODO: set system properties for logger properly
    System.setProperty("OTBCONF", ".otbproject")
  }
}

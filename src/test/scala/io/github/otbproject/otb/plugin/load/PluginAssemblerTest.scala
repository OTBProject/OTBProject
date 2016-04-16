package io.github.otbproject.otb.plugin.load

import io.github.otbproject.otb.plugin.base.{Plugin, PluginIdentifier}
import io.github.otbproject.otb.plugin.test.{Helper, PDepChain0, PSimpleValid}
import org.junit.Test
import org.junit.Assert._

class PluginAssemblerTest {
  @Test
  @throws[Exception]
  def assembleFrom(): Unit = {
    val plugins = PluginAssembler.assembleFrom(Helper.infoSet)
    assertEquals(2, plugins.size)

    val identifiers: List[PluginIdentifier[_ <: Plugin]] = plugins.map(_.info.identifier)

    assertTrue(identifiers contains Helper.identifier(classOf[PSimpleValid]))
    assertTrue(identifiers contains Helper.identifier(classOf[PDepChain0]))
  }
}

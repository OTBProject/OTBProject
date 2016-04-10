package io.github.otbproject.otb.plugin.load

import java.net.URL

import io.github.otbproject.otb.plugin.base.PluginInfo

import scala.collection.mutable
import scala.reflect.internal.util.ScalaClassLoader.URLClassLoader
import scala.reflect.runtime.{universe => ru}

object PluginFinder {

  /**
    * MAY OR MAY NOT DESTROY THE WORLD COMPLETELY UNTESTED
    * @param dir the directory to load plugins from
    * @return the plugins
    */
  def findPlugins(dir: String): Set[PluginInfo] = {
    val plugins = mutable.Set[PluginInfo]()
    val urls: Seq[URL] = new java.io.File(dir).listFiles.filter(_.getName.endsWith(".jar")).map(_.toURI.toURL)
    val classloader =new URLClassLoader(urls, getClass.getClassLoader)
    val mirror = ru.runtimeMirror(classloader)
    val f = mirror.typeOf[URLClassLoader].getClass.getDeclaredField("classes")
    f.setAccessible(true)
    val classes = f.get(classloader).asInstanceOf[Seq[Class[_]]]
    classes.foreach(clazz=>{
      if(mirror.typeOf[clazz.type ].getClass.getInterfaces.contains(mirror.typeOf[PluginInfo].getClass)){
        val cm = mirror.reflectClass(ru.typeOf[clazz.type ].typeSymbol.asClass)
        val ctor = ru.typeOf[clazz.type ].decl(ru.termNames.CONSTRUCTOR).asMethod
        val ctorm = cm.reflectConstructor(ctor)
        plugins.add(ctorm().asInstanceOf)
      }
    })
    plugins.toSet
  }

}

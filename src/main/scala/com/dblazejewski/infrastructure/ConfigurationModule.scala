package com.dblazejewski.infrastructure

import java.io.File

import com.typesafe.config.{ Config, ConfigFactory }

trait Configuration {
  def config: Config
}

trait ConfigurationModuleImpl extends Configuration {
  private val internalConfig: Config = {
    val configDefaults = ConfigFactory.load(this.getClass.getClassLoader, "reference.conf")

    scala.sys.props.get("reference.config") match {
      case Some(filename) => ConfigFactory.parseFile(new File(filename)).withFallback(configDefaults)
      case None => configDefaults
    }
  }

  def config: Config = internalConfig
}

package http.ignite

import http.json.UserTest
import org.apache.ignite.configuration.{CacheConfiguration, IgniteConfiguration}
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder
import org.apache.ignite.{Ignite, IgniteCache, Ignition}

object IgniteHelper {

  def igniteDataGrid(name: String): IgniteCache[String, UserTest] = {
    igniteClusterWithMulticast(name)
  }

  private def configuration(dataGridName: String) = {
    val igniteConfig = new IgniteConfiguration()
    // Setting the size of the default memory region to 1GB to achieve this.
    val cacheConfig: CacheConfiguration[String, UserTest] = new CacheConfiguration(dataGridName)
    igniteConfig.setCacheConfiguration(cacheConfig)
    cacheConfig.setStatisticsEnabled(true)
    igniteConfig
  }

  private def igniteClusterWithMulticast(dataGridName: String): IgniteCache[String, UserTest] = {
    val cfg: IgniteConfiguration = configuration(dataGridName)
    val spi = new TcpDiscoverySpi()
    spi.setLocalPort(48500)
    spi.setLocalPortRange(20)
    val ipFinder = new TcpDiscoveryMulticastIpFinder()
    ipFinder.setMulticastGroup("228.10.10.157")
    spi.setIpFinder(ipFinder)
    cfg.setDiscoverySpi(spi)

    val ignite: Ignite = Ignition.start(cfg)
    ignite.cluster().ignite().getOrCreateCache(dataGridName)
  }
}

package ws.frontier.core

import com.twitter.util.Future
import beans.BeanProperty
import ws.frontier.core.util.{Logging, Banner}

/**
 * Trails represent conditional executions of Finagle services.  Unlike a Finagle service which _must_ process calls to
 * #apply, a trail can optionally say that the request cannot be handled by itself by returning None
 *
 * @author matt
 */
abstract class Trail[IN, OUT] extends Logging {
  /**
   * @return None if this trail cannot handled the provided request; Some(Future[OUT]) if the action was handled
   */
  def apply(request: IN): Option[Future[OUT]]

  @BeanProperty
  var id: String = null

  @BeanProperty
  var tags: Array[String] = null

  /**
   * @return a future that allows us to mark when initialization is complete
   */
  def initialize(): Future[Unit] = Future()

  def banner(log: Banner) {
    throw new UnsupportedOperationException("#banner not implemented for class, %s" format this.getClass.getCanonicalName)
  }

  def start(registry: Registry[IN, OUT]): Future[Unit]

  def shutdown(): Future[Unit]
}

class EmptyTrail[IN, OUT] extends Trail[IN, OUT] {
  /**
   * @return None if this trail cannot handled the provided request; Some(Future[OUT]) if the action was handled
   */
  def apply(request: IN): Option[Future[OUT]] = {
    throw new UnsupportedOperationException("#apply not implemented in the EmptyTrail")
  }

  def start(registry: Registry[IN, OUT]): Future[Unit] = {
    Future()
  }

  def shutdown(): Future[Unit] = {
    Future()
  }
}

/**
 * TrailAggregator provides a way to prioritize trail selection among a number of trails.  First trail to match wins.
 * If no trail matches, then None will be returned.
 *
 * @param trails the universe of potential options to choose from
 */
class AggregatingTrail[IN, OUT](@BeanProperty val trails: Array[Trail[IN, OUT]]) extends Trail[IN, OUT] {
  def apply(request: IN): Option[Future[OUT]] = {
    var index = 0
    while (index < trails.length) {
      val result = trails(index)(request)
      if (result.isDefined) {
        return result
      }
      index = index + 1
    }

    None
  }

  override def banner(log: Banner) {
    trails.foreach {
      trail =>
        log() // put a spacer line before each trail
        trail.banner(log)
    }
  }

  override def initialize(): Future[Unit] = {
    Future.join {
      trails.map(_.initialize())
    }
  }

  override def start(registry: Registry[IN, OUT]): Future[Unit] = {
    Future.join {
      trails.map(_.start(registry))
    }
  }

  override def shutdown(): Future[Unit] = {
    Future.join {
      trails.map(_.shutdown())
    }
  }
}

object AggregatingTrail {
  def apply[IN, OUT](trails: Trail[IN, OUT]*): AggregatingTrail[IN, OUT] = {
    new AggregatingTrail[IN, OUT](trails.toArray)
  }
}

case class TrailGuide[IN, OUT](trail: Trail[IN, OUT], params: Map[String, String])

abstract class RoutingTrail[IN, OUT](guides: TrailGuide[IN, OUT]*) extends Trail[IN, OUT] {

}

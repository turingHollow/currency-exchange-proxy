package forex.services.caching

import cats.effect.{Resource, Temporal}
import forex.services.caching.intepreters.InMemoryCachingService

object Interpreters {
  def inMemory[F[_]: Temporal]: Resource[F, Algebra[F]] =
    InMemoryCachingService.resource[F]
}
